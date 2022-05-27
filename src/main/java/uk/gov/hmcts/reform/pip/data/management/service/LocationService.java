package uk.gov.hmcts.reform.pip.data.management.service;

import com.opencsv.bean.CsvToBean;
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

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Service to handle the retrieval and filtering of courts.
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
        return locationRepository.findAll();
    }

    /**
     * Handles request to search for a court by court id.
     *
     * @param locationId The location ID to search for.
     * @return Location of the found location
     * @throws LocationNotFoundException when no court was found with the given court ID.
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
     * This method will upload location into the database.
     * @param locationList The location list file to upload.
     * @return The collection of new locations that have been created.
     */
    public Collection<Location> uploadLocations(MultipartFile locationList) {

        try (InputStreamReader inputStreamReader = new InputStreamReader(locationList.getInputStream());
             Reader reader = new BufferedReader(inputStreamReader)) {
            CsvToBean<LocationCsv> csvToBean = new CsvToBeanBuilder<LocationCsv>(reader)
                .withType(LocationCsv.class)
                .build();

            List<LocationCsv> locationCsvList = csvToBean.parse();

            Map<Integer, Location> locationCsvMap = new ConcurrentHashMap<>();
            for (LocationCsv locationCsv : locationCsvList) {
                if (locationCsvMap.containsKey(locationCsv.getUniqueId())) {
                    Location location = locationCsvMap.get(locationCsv.getUniqueId());
                    location.addLocationReference(new LocationReference(
                        locationCsv.getProvenance(),
                        locationCsv.getProvenanceLocationId(),
                        LocationType.valueOfCsv(locationCsv.getProvenanceLocationType())));
                } else {
                    Location location = new Location(locationCsv);
                    locationCsvMap.put(locationCsv.getUniqueId(), location);
                }
            }

            locationCsvMap.forEach((key, value) -> locationRepository.save(value));
            return locationCsvMap.values();

        } catch (Exception exception) {
            throw new CsvParseException(exception.getMessage());
        }
    }
}
