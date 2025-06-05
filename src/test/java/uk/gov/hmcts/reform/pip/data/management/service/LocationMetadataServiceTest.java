package uk.gov.hmcts.reform.pip.data.management.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.pip.data.management.database.LocationMetadataRepository;
import uk.gov.hmcts.reform.pip.data.management.errorhandling.exceptions.LocationMetadataNotFoundException;
import uk.gov.hmcts.reform.pip.data.management.models.location.LocationMetadata;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LocationMetadataServiceTest {

    private static final String ACTIONING_USER_ID = "user-123";
    private static final String LOCATION_ID = "123";
    private static final String UUID_STRING = UUID.randomUUID().toString();
    private static final UUID TEST_UUID = UUID.fromString(UUID_STRING);
    private static final String LOCATION_METADATA_NOT_FOUND_MESSAGE = "Location metadata not found";

    @Mock
    private LocationMetadataRepository locationMetadataRepository;

    @InjectMocks
    private LocationMetadataService locationMetaDataService;

    @Test
    void testCreateLocationMetadata() {
        LocationMetadata locationMetadata = new LocationMetadata();
        locationMetadata.setLocationMetadataId(TEST_UUID);
        locationMetadata.setLocationId(123);

        when(locationMetadataRepository.save(locationMetadata)).thenReturn(locationMetadata);

        locationMetaDataService.createLocationMetadata(locationMetadata, ACTIONING_USER_ID);

        verify(locationMetadataRepository).save(locationMetadata);
    }

    @Test
    void testDeleteByIdSuccess() {
        LocationMetadata locationMetadata = new LocationMetadata();
        when(locationMetadataRepository.findById(TEST_UUID)).thenReturn(Optional.of(locationMetadata));
        doNothing().when(locationMetadataRepository).deleteById(TEST_UUID);

        locationMetaDataService.deleteById(UUID_STRING, ACTIONING_USER_ID);

        verify(locationMetadataRepository).deleteById(TEST_UUID);
    }

    @Test
    void testDeleteByIdNotFound() {
        when(locationMetadataRepository.findById(TEST_UUID)).thenReturn(Optional.empty());

        LocationMetadataNotFoundException exception = assertThrows(
            LocationMetadataNotFoundException.class,
            () -> locationMetaDataService.deleteById(UUID_STRING, ACTIONING_USER_ID)
        );

        assertEquals(
            String.format("No location metadata found with the id: %s", UUID_STRING),
            exception.getMessage(),
            LOCATION_METADATA_NOT_FOUND_MESSAGE
        );
    }

    @Test
    void testUpdateLocationMetadata() {
        LocationMetadata locationMetadata = new LocationMetadata();
        locationMetadata.setLocationId(123);
        locationMetadata.setLocationMetadataId(TEST_UUID);

        when(locationMetadataRepository.findById(UUID.fromString(UUID_STRING)))
            .thenReturn(Optional.of(locationMetadata));
        when(locationMetadataRepository.save(locationMetadata)).thenReturn(locationMetadata);

        locationMetaDataService.updateLocationMetadata(locationMetadata, UUID_STRING, ACTIONING_USER_ID);

        verify(locationMetadataRepository).save(locationMetadata);
    }

    @Test
    void testUpdateByIdNotFound() {
        when(locationMetadataRepository.findById(TEST_UUID)).thenReturn(Optional.empty());

        LocationMetadata locationMetadata = new LocationMetadata();
        locationMetadata.setLocationId(123);
        locationMetadata.setLocationMetadataId(TEST_UUID);

        LocationMetadataNotFoundException exception = assertThrows(
            LocationMetadataNotFoundException.class,
            () -> locationMetaDataService.updateLocationMetadata(locationMetadata, UUID_STRING, ACTIONING_USER_ID)
        );

        assertEquals(
            String.format("No location metadata found with the id: %s", UUID_STRING),
            exception.getMessage(),
            LOCATION_METADATA_NOT_FOUND_MESSAGE
        );
    }

    @Test
    void testGetLocationByIdSuccess() {
        LocationMetadata expectedMetadata = new LocationMetadata();
        expectedMetadata.setLocationId(123);

        when(locationMetadataRepository.findByLocationId(123)).thenReturn(Optional.of(expectedMetadata));

        LocationMetadata result = locationMetaDataService.getLocationById(LOCATION_ID);

        assertEquals(expectedMetadata, result, LOCATION_METADATA_NOT_FOUND_MESSAGE);
    }

    @Test
    void testGetLocationByIdNotFound() {
        when(locationMetadataRepository.findByLocationId(123)).thenReturn(Optional.empty());

        LocationMetadataNotFoundException exception = assertThrows(
            LocationMetadataNotFoundException.class,
            () -> locationMetaDataService.getLocationById(LOCATION_ID)
        );

        assertEquals(
            String.format("No location metadata found for location id: %s", LOCATION_ID),
            exception.getMessage(),
            LOCATION_METADATA_NOT_FOUND_MESSAGE
        );
    }

    @Test
    void testGetByIdSuccess() {
        LocationMetadata expectedMetadata = new LocationMetadata();
        expectedMetadata.setLocationMetadataId(TEST_UUID);

        when(locationMetadataRepository.findById(TEST_UUID)).thenReturn(Optional.of(expectedMetadata));

        LocationMetadata result = locationMetaDataService.getById(UUID_STRING);

        assertEquals(expectedMetadata, result, LOCATION_METADATA_NOT_FOUND_MESSAGE);
    }

    @Test
    void testGetByIdNotFound() {
        when(locationMetadataRepository.findById(TEST_UUID)).thenReturn(Optional.empty());

        LocationMetadataNotFoundException exception = assertThrows(
            LocationMetadataNotFoundException.class,
            () -> locationMetaDataService.getById(UUID_STRING)
        );

        assertEquals(
            String.format("No location metadata found with the id: %s", UUID_STRING),
            exception.getMessage(),
            LOCATION_METADATA_NOT_FOUND_MESSAGE
        );
    }
}
