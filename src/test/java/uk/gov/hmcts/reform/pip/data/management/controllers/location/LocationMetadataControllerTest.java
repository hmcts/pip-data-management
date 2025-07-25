package uk.gov.hmcts.reform.pip.data.management.controllers.location;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.reform.pip.data.management.errorhandling.exceptions.CreateLocationMetadataConflictException;
import uk.gov.hmcts.reform.pip.data.management.errorhandling.exceptions.LocationMetadataNotFoundException;
import uk.gov.hmcts.reform.pip.data.management.models.location.LocationMetadata;
import uk.gov.hmcts.reform.pip.data.management.service.location.LocationMetadataService;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LocationMetadataControllerTest {

    private static final String REQUESTER_ID = "user-123";
    private static final String LOCATION_ID = "123";
    private static final String UUID_STRING = UUID.randomUUID().toString();
    private static final String NOT_FOUND = "Not found";

    @Mock
    private LocationMetadataService locationMetadataService;

    @InjectMocks
    private LocationMetadataController locationMetaDataController;

    private LocationMetadata createTestLocationMetadata() {
        LocationMetadata locationMetadata = new LocationMetadata();
        locationMetadata.setLocationId(Integer.parseInt(LOCATION_ID));
        return locationMetadata;
    }

    @Test
    void testAddLocationMetaDataSuccess() {
        LocationMetadata locationMetadata = createTestLocationMetadata();
        doNothing().when(locationMetadataService).createLocationMetadata(any(), anyString());

        ResponseEntity<String> response =
            locationMetaDataController.addLocationMetaData(REQUESTER_ID, locationMetadata);

        assertEquals(HttpStatus.CREATED, response.getStatusCode(),
                     "Response status should be CREATED (201) for successful addition");
        assertEquals("Location metadata successfully added by user user-123", response.getBody(),
                     "Response message should confirm successful addition with correct user ID");
    }

    @Test
    void testUpdateLocationMetaDataSuccess() {
        LocationMetadata locationMetadata = createTestLocationMetadata();
        doNothing().when(locationMetadataService).updateLocationMetadata(any(), any(), anyString());

        ResponseEntity<String> response =
            locationMetaDataController.updateLocationMetaData(REQUESTER_ID, "user-123", locationMetadata);

        assertEquals(HttpStatus.OK, response.getStatusCode(),
                     "Response status should be OK (200) for successful update");
        assertEquals("Location metadata successfully updated by user user-123", response.getBody(),
                     "Response message should confirm successful update with correct user ID");
    }

    @Test
    void testUpdateLocationMetaDataNotFound() {
        LocationMetadata locationMetadata = createTestLocationMetadata();
        doThrow(new LocationMetadataNotFoundException(NOT_FOUND))
            .when(locationMetadataService).updateLocationMetadata(any(), any(), anyString());

        LocationMetadataNotFoundException exception = assertThrows(
            LocationMetadataNotFoundException.class,
            () -> locationMetaDataController.updateLocationMetaData(REQUESTER_ID, UUID_STRING, locationMetadata),
            "Should throw LocationMetaDataNotFoundException when metadata not found"
        );

        assertEquals(NOT_FOUND, exception.getMessage(),
                     "Exception message should match the expected message");
    }

    @Test
    void testDeleteLocationMetaDataSuccess() {
        doNothing().when(locationMetadataService).deleteById(anyString(), anyString());

        ResponseEntity<String> response =
            locationMetaDataController.deleteLocationMetaData(REQUESTER_ID, UUID_STRING);

        assertEquals(HttpStatus.OK, response.getStatusCode(),
                     "Response status should be OK (200) for successful deletion");
        assertEquals("Location metadata successfully deleted by user user-123", response.getBody(),
                     "Response message should confirm successful deletion with correct user ID");
    }

    @Test
    void testDeleteLocationMetaDataNotFound() {
        doThrow(new LocationMetadataNotFoundException(NOT_FOUND))
            .when(locationMetadataService).deleteById(anyString(), anyString());

        LocationMetadataNotFoundException exception = assertThrows(
            LocationMetadataNotFoundException.class,
            () -> locationMetaDataController.deleteLocationMetaData(REQUESTER_ID, UUID_STRING),
            "Should throw LocationMetaDataNotFoundException when metadata not found"
        );

        assertEquals(NOT_FOUND, exception.getMessage(),
                     "Exception message should match the expected message");
    }

    @Test
    void testGetLocationMetaDataByLocationIdSuccess() {
        LocationMetadata expectedMetadata = createTestLocationMetadata();
        when(locationMetadataService.getLocationById(LOCATION_ID)).thenReturn(expectedMetadata);

        ResponseEntity<LocationMetadata> response =
            locationMetaDataController.getLocationMetaDataByLocationId(LOCATION_ID);

        assertEquals(HttpStatus.OK, response.getStatusCode(),
                     "Response status should be OK (200) for successful retrieval by location ID");
        assertEquals(expectedMetadata, response.getBody(),
                     "Returned metadata should match the expected metadata when searching by location ID");
    }

    @Test
    void testGetLocationMetaDataByLocationIdNotFound() {
        when(locationMetadataService.getLocationById(LOCATION_ID))
            .thenThrow(new LocationMetadataNotFoundException(NOT_FOUND));

        LocationMetadataNotFoundException exception = assertThrows(
            LocationMetadataNotFoundException.class,
            () -> locationMetaDataController.getLocationMetaDataByLocationId(LOCATION_ID),
            "Should throw LocationMetaDataNotFoundException when metadata not found by location ID"
        );

        assertEquals(NOT_FOUND, exception.getMessage(),
                     "Exception message should match the expected message");
    }

    @Test
    void testGetLocationMetaDataByLocationIdInvalidId() {
        when(locationMetadataService.getLocationById("invalid"))
            .thenThrow(new NumberFormatException("Invalid number"));

        NumberFormatException exception = assertThrows(
            NumberFormatException.class,
            () -> locationMetaDataController.getLocationMetaDataByLocationId("invalid"),
            "Should throw NumberFormatException when location ID is invalid"
        );

        assertEquals("Invalid number", exception.getMessage(),
                     "Exception message should indicate invalid number format");
    }

    @Test
    void testAddLocationMetaDataWithConflictException() {
        LocationMetadata locationMetadata = new LocationMetadata();
        locationMetadata.setLocationId(Integer.parseInt(LOCATION_ID));
        locationMetadata.setCautionMessage("Caution message");

        String expectedErrorMessage = "Location Metadata with location Id 123 "
            + "cannot be saved because it exists already";

        doThrow(new CreateLocationMetadataConflictException(expectedErrorMessage))
            .when(locationMetadataService)
            .createLocationMetadata(locationMetadata, REQUESTER_ID);

        CreateLocationMetadataConflictException exception = assertThrows(
            CreateLocationMetadataConflictException.class,
            () -> locationMetaDataController.addLocationMetaData(REQUESTER_ID, locationMetadata),
            "Expected Add Location Metadata method to throw CreateLocationMetadataConflictException"
        );

        assertEquals(expectedErrorMessage, exception.getMessage(),
                     "The exception message should match the expected error message");

        verify(locationMetadataService, times(1))
            .createLocationMetadata(locationMetadata, REQUESTER_ID);
    }
}
