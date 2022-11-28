package uk.gov.hmcts.reform.pip.data.management.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.opencsv.bean.CsvToBeanBuilder;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import uk.gov.hmcts.reform.pip.data.management.database.ArtefactRepository;
import uk.gov.hmcts.reform.pip.data.management.database.LocationRepository;
import uk.gov.hmcts.reform.pip.data.management.errorhandling.exceptions.CsvParseException;
import uk.gov.hmcts.reform.pip.data.management.errorhandling.exceptions.LocationNotFoundException;
import uk.gov.hmcts.reform.pip.data.management.models.admin.Action;
import uk.gov.hmcts.reform.pip.data.management.models.admin.ActionResult;
import uk.gov.hmcts.reform.pip.data.management.models.admin.ChangeType;
import uk.gov.hmcts.reform.pip.data.management.models.location.Location;
import uk.gov.hmcts.reform.pip.data.management.models.location.LocationCsv;
import uk.gov.hmcts.reform.pip.data.management.models.location.LocationDeletion;
import uk.gov.hmcts.reform.pip.data.management.models.location.LocationReference;
import uk.gov.hmcts.reform.pip.data.management.models.location.LocationType;
import uk.gov.hmcts.reform.pip.data.management.models.publication.Artefact;
import uk.gov.hmcts.reform.pip.model.enums.UserActions;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.Reader;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static uk.gov.hmcts.reform.pip.data.management.models.request.Roles.SYSTEM_ADMIN;
import static uk.gov.hmcts.reform.pip.model.LogBuilder.writeLog;

/**
 * Service to handle the retrieval and filtering of locations.
 */
@Service
@Slf4j
public class LocationService {
    private final LocationRepository locationRepository;

    private final ArtefactRepository artefactRepository;

    private final SubscriptionManagementService subscriptionManagementService;

    private final AccountManagementService accountManagementService;

    private final PublicationServicesService publicationService;

    public LocationService(LocationRepository locationRepository,
                           ArtefactRepository artefactRepository,
                           SubscriptionManagementService subscriptionManagementService,
                           AccountManagementService accountManagementService,
                           PublicationServicesService publicationService) {
        this.locationRepository = locationRepository;
        this.artefactRepository = artefactRepository;
        this.subscriptionManagementService = subscriptionManagementService;
        this.accountManagementService = accountManagementService;
        this.publicationService = publicationService;
    }

    /**
     * Gets all locations.

     * @return List of Locations
     */
    public List<Location> getAllLocations() {
        log.info(writeLog("Retrieve all locations"));

        return locationRepository.findAll();
    }

    /**
     * Handles request to search for a location by location id.
     *
     * @param locationId The location ID to search for.
     * @return Location of the found location
     * @throws LocationNotFoundException when no locations were found with the given location ID.
     */
    public Location getLocationById(Integer locationId) {
        Optional<Location> foundLocation = locationRepository.getLocationByLocationId(locationId);

        if (foundLocation.isEmpty()) {
            throw new LocationNotFoundException(String.format("No location found with the id: %s", locationId));
        } else {
            return foundLocation.get();
        }
    }

    /**
     * Handles request to search for a Location by the Location name.
     *
     * @param locationName the location name to search for.
     * @return Location of the found location.
     * @throws LocationNotFoundException when no location was found with the given search input.
     */
    public Location getLocationByName(String locationName, String language) {
        Optional<Location> foundLocation;
        if ("cy".equals(language)) {
            foundLocation = locationRepository.getLocationByWelshName(locationName);
        } else {
            foundLocation = locationRepository.getLocationByName(locationName);
        }

        if (foundLocation.isEmpty()) {
            throw new LocationNotFoundException(String.format("No location found with the name: %s", locationName));
        } else {
            return foundLocation.get();
        }
    }

    /**
     * Handles filtering the locations based on region and jurisdiction.
     *
     * @param regions The list of regions to filter against.
     * @param jurisdictions The list of jurisdictions to filter against.
     * @return List of Location objects, can return empty List
     */
    public List<Location> searchByRegionAndJurisdiction(List<String> regions, List<String> jurisdictions,
                                                        String language) {
        if ("cy".equals(language)) {
            return locationRepository.findByWelshRegionAndJurisdictionOrderByName(
                regions == null ? "" : StringUtils.join(regions, ','),
                jurisdictions == null ? "" : StringUtils.join(jurisdictions, ',')
            );
        } else {
            return locationRepository.findByRegionAndJurisdictionOrderByName(
                regions == null ? "" : StringUtils.join(regions, ','),
                jurisdictions == null ? "" : StringUtils.join(jurisdictions, ',')
            );
        }
    }

    /**
     * This method will upload locations into the database. It uses the P&I id in the CSV file as the unique identifier
     * If the ID already exists, then the data will be overwritten, including all of the reference data. It will
     * not delete records for added safety. Deleting records should be done via the DELETE endpoint instead.
     * @param locationList The location list file to upload.
     * @return The collection of new locations that have been created.
     */
    public Collection<Location> uploadLocations(MultipartFile locationList) {
        log.info(writeLog(UserActions.LOCATION_UPLOAD, "via CSV"));

        try (InputStreamReader inputStreamReader = new InputStreamReader(locationList.getInputStream());
             Reader reader = new BufferedReader(inputStreamReader)) {

            List<LocationCsv> locationCsvList = new CsvToBeanBuilder<LocationCsv>(reader).withType(LocationCsv.class)
                .build().parse();

            Map<Integer, List<LocationCsv>> locations = locationCsvList.stream()
                .collect(Collectors.groupingBy(LocationCsv::getUniqueId));

            List<Location> savedLocations = new ArrayList<>();
            locations.values().forEach(groupedLocation -> {

                Location location = new Location(groupedLocation.get(0));

                groupedLocation.stream().skip(1).forEach(locationCsv ->
                    location.addLocationReference(new LocationReference(
                        locationCsv.getProvenance(),
                        locationCsv.getProvenanceLocationId(),
                        LocationType.valueOfCsv(locationCsv.getProvenanceLocationType()))));

                savedLocations.add(locationRepository.save(location));
            });

            return savedLocations;

        } catch (Exception exception) {
            throw new CsvParseException(exception.getMessage());
        }
    }

    /**
     * This method will delete a location from the database.
     * @param locationId The ID of the location to delete.
     */
    public LocationDeletion deleteLocation(Integer locationId, String requesterName) throws JsonProcessingException {
        LocationDeletion locationDeletion;
        Optional<Location> location = locationRepository.getLocationByLocationId(locationId);

        if (location.isPresent()) {
            locationDeletion = checkActiveArtefactForLocation(locationId.toString(), requesterName);
            if (!locationDeletion.getIsExists()) {
                locationDeletion = checkActiveSubscriptionForLocation(locationId.toString(), requesterName);
                if (!locationDeletion.getIsExists()) {
                    locationRepository.deleteById(locationId);
                }
            }
        } else {
            throw new LocationNotFoundException(
                String.format("No location found with the id: %s", locationId));
        }

        return locationDeletion;
    }

    private void sendEmailToAllSystemAdmins(Action action, String requesterName) throws JsonProcessingException {
        String result = accountManagementService.getAllAccounts("0",
                                                                "1000", "PI_AAD");
        List<String> systemAdmins = findAllSystemAdmins(result);
        for (String systemAdminEmail :systemAdmins) {
            publicationService.sendSystemAdminEmail(systemAdminEmail, requesterName, action);
        }
    }

    private List<String> findAllSystemAdmins(String result) throws JsonProcessingException {
        List<String> systemAdmins = new ArrayList<>();
        if (!result.isEmpty()) {
            JsonNode node = new ObjectMapper().readTree(result);
            if (!node.isEmpty()) {
                JsonNode content = node.get("content");
                content.forEach(jsonObject -> {
                    if (jsonObject.has("roles")
                        && jsonObject.get("roles").asText().equals(SYSTEM_ADMIN.toString())) {
                        systemAdmins.add(jsonObject.get("email").asText());
                    }
                });
            }
        }
        return systemAdmins;
    }

    private LocationDeletion checkActiveArtefactForLocation(String locationId, String requesterName)
        throws JsonProcessingException {
        LocalDateTime searchDateTime = LocalDateTime.now();
        LocationDeletion locationDeletion = new LocationDeletion();
        List<Artefact> activeArtefacts = artefactRepository.findActiveArtefactsForLocation(searchDateTime,
                                                                                           locationId);
        if (!activeArtefacts.isEmpty()) {
            Action action = buildErrorAndAdminAction(locationDeletion, ChangeType.DELETE_COURT,
                ActionResult.ATTEMPTED,"There are active artefacts for the given court");
            sendEmailToAllSystemAdmins(action, requesterName);
        }
        return locationDeletion;
    }

    private LocationDeletion checkActiveSubscriptionForLocation(String locationId, String requesterName)
        throws JsonProcessingException {
        LocationDeletion locationDeletion = new LocationDeletion();
        String result = subscriptionManagementService.findSubscriptionsByLocationId(locationId);
        if (!result.isEmpty()) {
            JsonNode node = new ObjectMapper().readTree(result);
            if (!node.isEmpty()) {
                Action action = buildErrorAndAdminAction(locationDeletion, ChangeType.DELETE_COURT,
                    ActionResult.ATTEMPTED,"There are active subscriptions for the given court");
                sendEmailToAllSystemAdmins(action, requesterName);
            }
        }
        return locationDeletion;
    }

    private Action buildErrorAndAdminAction(LocationDeletion locationDeletion, ChangeType changeType,
                                          ActionResult actionResult, String message) {
        locationDeletion.setIsExists(true);
        locationDeletion.setErrorMessage(message);
        return new Action(changeType, actionResult, message);
    }
}
