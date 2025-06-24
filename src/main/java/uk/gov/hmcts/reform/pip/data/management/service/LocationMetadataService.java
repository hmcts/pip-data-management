package uk.gov.hmcts.reform.pip.data.management.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.pip.data.management.database.LocationMetadataRepository;
import uk.gov.hmcts.reform.pip.data.management.errorhandling.exceptions.CreateLocationMetadataConflictException;
import uk.gov.hmcts.reform.pip.data.management.errorhandling.exceptions.LocationMetadataNotFoundException;
import uk.gov.hmcts.reform.pip.data.management.models.location.LocationMetadata;
import uk.gov.hmcts.reform.pip.model.enums.UserActions;

import java.util.UUID;

import static uk.gov.hmcts.reform.pip.model.LogBuilder.writeLog;

@Service
@Slf4j
public class LocationMetadataService {
    private final LocationMetadataRepository locationMetadataRepository;

    public LocationMetadataService(LocationMetadataRepository locationMetadataRepository) {
        this.locationMetadataRepository = locationMetadataRepository;
    }

    /**
     * Handles request to add location metadata.
     *
     * @param locationMetadata The location Metadata object
     * @param actioningUserId The userId who is performing this action
     */
    public void createLocationMetadata(LocationMetadata locationMetadata,
                                                   String actioningUserId) {
        try {
            log.info(writeLog(actioningUserId, UserActions.ADD_LOCATION_METADATA,
                    locationMetadata.getLocationId().toString()));
            locationMetadataRepository.save(locationMetadata);
        } catch (DataIntegrityViolationException e) {
            String errorMessage = String.format(
                    "Location Metadata with location Id %d cannot be saved because it is exists already",
                    locationMetadata.getLocationId());
            log.error(writeLog(errorMessage));
            throw new CreateLocationMetadataConflictException(errorMessage);
        }
    }

    /**
     * Handles request to delete location metadata.
     *
     * @param id The location Metadata id
     * @param actioningUserId The userId who is performing this action
     */
    public void deleteById(String id, String actioningUserId) {
        UUID locationMetadataId = UUID.fromString(id);
        locationMetadataRepository.findById(locationMetadataId)
            .orElseThrow(() -> new LocationMetadataNotFoundException(
                String.format("No location metadata found with the id: %s", id)
            ));

        locationMetadataRepository.deleteById(locationMetadataId);

        log.info(writeLog(actioningUserId, UserActions.DELETE_LOCATION_METADATA,
                          locationMetadataId.toString()));
    }

    /**
     * Handles request to update location metadata.
     *
     *  @param locationMetadata The location Metadata object
     * @param actioningUserId The userId who is performing this action
     */
    public void updateLocationMetadata(LocationMetadata locationMetadata, String id,
                                       String actioningUserId) {
        log.info(writeLog(actioningUserId, UserActions.UPDATE_LOCATION_METADATA,
                          locationMetadata.getLocationId().toString()));
        UUID locationMetadataId = UUID.fromString(id);
        locationMetadataRepository.findById(locationMetadataId)
            .orElseThrow(() -> new LocationMetadataNotFoundException(
                String.format("No location metadata found with the id: %s", id)
            ));

        locationMetadataRepository.save(locationMetadata);
    }

    /**
     * Handles request to search for a location metadata by location id.
     *
     * @param locationId The location ID to search for.
     * @return Location metadata of the found location
     * @throws LocationMetadataNotFoundException when no locations were found with the given location ID.
     */
    public LocationMetadata getLocationById(String locationId) {
        return locationMetadataRepository.findByLocationId(Integer.parseInt(locationId))
            .orElseThrow(() -> new LocationMetadataNotFoundException(
                String.format("No location metadata found for location id: %s", locationId)
            ));
    }

    /**
     * Handles request to search for a location metadata by id.
     *
     * @param id The ID to search for.
     * @return Location metadata of the found location
     * @throws LocationMetadataNotFoundException when no locations were found with the given ID.
     */
    public LocationMetadata getById(String id) {
        return locationMetadataRepository.findById(UUID.fromString(id))
            .orElseThrow(() -> new LocationMetadataNotFoundException(
                String.format("No location metadata found with the id: %s", id)
            ));
    }
}
