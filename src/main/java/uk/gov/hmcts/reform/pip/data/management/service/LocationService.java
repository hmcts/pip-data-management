package uk.gov.hmcts.reform.pip.data.management.service;

import com.opencsv.bean.CsvToBeanBuilder;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import uk.gov.hmcts.reform.pip.data.management.database.LocationRepository;
import uk.gov.hmcts.reform.pip.data.management.errorhandling.exceptions.CsvParseException;
import uk.gov.hmcts.reform.pip.data.management.errorhandling.exceptions.LocationNotFoundException;
import uk.gov.hmcts.reform.pip.data.management.models.location.Location;
import uk.gov.hmcts.reform.pip.data.management.models.location.LocationCsv;
import uk.gov.hmcts.reform.pip.data.management.models.location.LocationReference;
import uk.gov.hmcts.reform.pip.data.management.models.location.LocationType;
import uk.gov.hmcts.reform.pip.model.enums.UserActions;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static uk.gov.hmcts.reform.pip.model.LogBuilder.writeLog;

/**
 * Service to handle the retrieval and filtering of locations.
 */
@Service
@Slf4j
public class LocationService {

    @Autowired
    private LocationRepository locationRepository;

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
    public Location getLocationByName(String locationName) {
        Optional<Location> foundLocation = locationRepository.getLocationByName(locationName);
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
    public List<Location> searchByRegionAndJurisdiction(List<String> regions, List<String> jurisdictions) {
        return locationRepository.findByRegionAndJurisdictionOrderByName(
            regions == null ? "" : StringUtils.join(regions, ','),
            jurisdictions == null ? "" : StringUtils.join(jurisdictions, ','));
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
    public void deleteLocation(Integer locationId) {
        Optional<Location> location = locationRepository.getLocationByLocationId(locationId);

        if (location.isPresent()) {
            locationRepository.deleteById(locationId);
        } else {
            throw new LocationNotFoundException(
                String.format("No location found with the id: %s", locationId));
        }
    }

}
