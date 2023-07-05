package uk.gov.hmcts.reform.pip.data.management.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.opencsv.CSVWriter;
import com.opencsv.bean.CsvToBeanBuilder;
import com.opencsv.bean.StatefulBeanToCsv;
import com.opencsv.bean.StatefulBeanToCsvBuilder;
import com.opencsv.exceptions.CsvDataTypeMismatchException;
import com.opencsv.exceptions.CsvRequiredFieldEmptyException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import uk.gov.hmcts.reform.pip.data.management.database.ArtefactRepository;
import uk.gov.hmcts.reform.pip.data.management.database.LocationRepository;
import uk.gov.hmcts.reform.pip.data.management.errorhandling.exceptions.CsvParseException;
import uk.gov.hmcts.reform.pip.data.management.errorhandling.exceptions.LocationNotFoundException;
import uk.gov.hmcts.reform.pip.data.management.helpers.TestingSupportLocationHelper;
import uk.gov.hmcts.reform.pip.data.management.models.location.Location;
import uk.gov.hmcts.reform.pip.data.management.models.location.LocationDeletion;
import uk.gov.hmcts.reform.pip.data.management.models.location.LocationReference;
import uk.gov.hmcts.reform.pip.data.management.models.publication.Artefact;
import uk.gov.hmcts.reform.pip.model.account.AzureAccount;
import uk.gov.hmcts.reform.pip.model.enums.UserActions;
import uk.gov.hmcts.reform.pip.model.location.LocationCsv;
import uk.gov.hmcts.reform.pip.model.location.LocationType;
import uk.gov.hmcts.reform.pip.model.system.admin.ActionResult;
import uk.gov.hmcts.reform.pip.model.system.admin.ChangeType;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static uk.gov.hmcts.reform.pip.model.LogBuilder.writeLog;
import static uk.gov.hmcts.reform.pip.model.account.Roles.SYSTEM_ADMIN;

/**
 * Service to handle the retrieval and filtering of locations.
 */
@Service
@Slf4j
@SuppressWarnings({"PMD"})
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
        return locationRepository.getLocationByLocationId(locationId)
            .orElseThrow(() -> new LocationNotFoundException(
                String.format("No location found with the id: %s", locationId)
            ));
    }

    /**
     * Handles request to search for a Location by the Location name.
     *
     * @param locationName the location name to search for.
     * @return Location of the found location.
     * @throws LocationNotFoundException when no location was found with the given search input.
     */
    public Location getLocationByName(String locationName, String language) {
        Optional<Location> foundLocation = "cy".equals(language)
            ? locationRepository.getLocationByWelshName(locationName)
            : locationRepository.getLocationByName(locationName);

        return foundLocation
            .orElseThrow(() -> new LocationNotFoundException(
                String.format("No location found with the name: %s", locationName))
            );
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
        String region = regions == null ? "" : StringUtils.join(regions, ',');
        String jurisdiction = jurisdictions == null ? "" : StringUtils.join(jurisdictions, ',');

        return "cy".equals(language)
            ? locationRepository.findByWelshRegionAndJurisdictionOrderByName(region, jurisdiction)
            : locationRepository.findByRegionAndJurisdictionOrderByName(region, jurisdiction);
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

    public String createLocation(Integer locationId, String locationName) {
        locationRepository.save(TestingSupportLocationHelper.createLocation(locationId, locationName));
        return String.format("Location with ID %s and name %s created successfully", locationId, locationName);
    }

    /**
     * Creates a csv of the current reference data.
     *
     * @return Returns the created CSV in byte array format.
     */
    public byte[] downloadLocations() throws IOException {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        OutputStreamWriter streamWriter = new OutputStreamWriter(stream);
        CSVWriter writer = new CSVWriter(streamWriter);

        List<Location> allLocations = locationRepository.findAll();
        StatefulBeanToCsv<LocationCsv> beanToCsv = new StatefulBeanToCsvBuilder<LocationCsv>(writer).build();
        allLocations.forEach(location ->
            location.getLocationReferenceList().forEach(locationReference -> {
                try {
                    beanToCsv.write(new LocationCsv(
                        location.getLocationId(),
                        location.getName(),
                        location.getRegion(),
                        location.getJurisdiction(),
                        locationReference.getProvenance(),
                        locationReference.getProvenanceLocationId(),
                        locationReference.getProvenanceLocationType().csvInput,
                        location.getWelshName(),
                        location.getWelshRegion(),
                        location.getWelshJurisdiction(),
                        location.getEmail(),
                        location.getContactNo()
                    ));
                } catch (CsvDataTypeMismatchException | CsvRequiredFieldEmptyException e) {
                    throw new CsvParseException(String.format("Failed to create CSV with message: %s",
                                                              e.getMessage()));
                }
            }));

        streamWriter.flush();
        streamWriter.close();
        return stream.toByteArray();
    }

    /**
     * This method will delete a location from the database.
     * @param locationId The ID of the location to delete.
     */
    public LocationDeletion deleteLocation(Integer locationId, String provenanceUserId)
        throws JsonProcessingException {
        LocationDeletion locationDeletion;
        Location location = locationRepository.getLocationByLocationId(locationId)
            .orElseThrow(() -> new LocationNotFoundException(
                String.format("No location found with the id: %s", locationId)
            ));

        AzureAccount userInfo = accountManagementService.getUserInfo(provenanceUserId);
        locationDeletion = checkActiveArtefactForLocation(location, userInfo.getDisplayName());
        if (!locationDeletion.isExists()) {
            locationDeletion = checkActiveSubscriptionForLocation(location, userInfo.getDisplayName());
            if (!locationDeletion.isExists()) {
                locationRepository.deleteById(locationId);
                sendEmailToAllSystemAdmins(userInfo.getDisplayName(), ActionResult.SUCCEEDED,
                    String.format("Location %s with Id %s has been deleted.",
                            location.getName(), location.getLocationId()));
            }
        }

        return locationDeletion;
    }

    public String deleteAllLocationsWithNamePrefix(String prefix) {
        List<Integer> locationIds = getAllLocationsWithNamePrefix(prefix);

        if (!locationIds.isEmpty()) {
            locationRepository.deleteByLocationIdIn(locationIds);
        }
        return String.format("%s location(s) deleted with name starting with %s",
                             locationIds.size(), prefix);
    }

    public List<Integer> getAllLocationsWithNamePrefix(String prefix) {
        return locationRepository.findAllByNameStartingWithIgnoreCase(prefix).stream()
            .map(Location::getLocationId)
            .toList();
    }

    private void sendEmailToAllSystemAdmins(String requesterName, ActionResult actionResult,
                                            String additionalDetails) throws JsonProcessingException {
        List<String> systemAdmins = accountManagementService.getAllAccounts("PI_AAD", SYSTEM_ADMIN.toString());
        publicationService.sendSystemAdminEmail(systemAdmins, requesterName, actionResult, additionalDetails,
                                                ChangeType.DELETE_LOCATION);
    }

    private LocationDeletion checkActiveArtefactForLocation(Location location, String requesterName)
        throws JsonProcessingException {
        LocalDateTime searchDateTime = LocalDateTime.now();
        LocationDeletion locationDeletion = new LocationDeletion();
        List<Artefact> activeArtefacts =
            artefactRepository.findActiveArtefactsForLocation(searchDateTime, location.getLocationId().toString());
        if (!activeArtefacts.isEmpty()) {
            locationDeletion = new LocationDeletion("There are active artefacts for the given location.",
                                                    true);
            sendEmailToAllSystemAdmins(requesterName, ActionResult.ATTEMPTED,
                String.format("There are active artefacts for following location: %s", location.getName()));
        }
        return locationDeletion;
    }

    private LocationDeletion checkActiveSubscriptionForLocation(Location location, String requesterName)
        throws JsonProcessingException {
        LocationDeletion locationDeletion = new LocationDeletion();
        String result =
            subscriptionManagementService.findSubscriptionsByLocationId(location.getLocationId().toString());
        if (!result.isEmpty()
            && !result.contains("404")) {
            JsonNode node = new ObjectMapper().readTree(result);
            if (!node.isEmpty()) {
                locationDeletion = new LocationDeletion("There are active subscriptions for the given location.",
                                                        true);
                sendEmailToAllSystemAdmins(requesterName, ActionResult.ATTEMPTED,
                    String.format("There are active subscriptions for the following location: %s", location.getName()));
            }
        }
        return locationDeletion;
    }
}
