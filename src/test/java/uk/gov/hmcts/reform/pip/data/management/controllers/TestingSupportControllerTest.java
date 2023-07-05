package uk.gov.hmcts.reform.pip.data.management.controllers;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.reform.pip.data.management.service.LocationService;
import uk.gov.hmcts.reform.pip.data.management.service.artefact.ArtefactDeleteService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TestingSupportControllerTest {
    private static final Integer LOCATION_ID = 123;
    private static final String LOCATION_NAME = "TEST_PIP_1234_Court123";
    private static final String LOCATION_NAME_PREFIX = "TEST_PIP_1234_";

    private static final String RESPONSE_STATUS_MESSAGE = "Response status does not match";
    private static final String RESPONSE_BODY_MESSAGE = "Response body does not match";

    @Mock
    private ArtefactDeleteService artefactDeleteService;

    @Mock
    private LocationService locationService;

    @InjectMocks
    TestingSupportController testingSupportController;

    @Test
    void testCreateLocationReturnsCreated() {
        String responseMessage = "Location with ID " + LOCATION_ID + " and name " + LOCATION_NAME
            + " created successfully";
        when(locationService.createLocation(LOCATION_ID, LOCATION_NAME)).thenReturn(responseMessage);

        ResponseEntity<String> response = testingSupportController.createLocation(LOCATION_ID, LOCATION_NAME);

        assertThat(response.getStatusCode())
            .as(RESPONSE_STATUS_MESSAGE)
            .isEqualTo(HttpStatus.CREATED);

        assertThat(response.getBody())
            .as(RESPONSE_BODY_MESSAGE)
            .isEqualTo(responseMessage);
    }

    @Test
    void testDeleteLocationsByNamePrefixReturnsOk() {
        String responseMessage = "5 location(s) deleted with name starting with " + LOCATION_NAME_PREFIX;
        when(locationService.deleteAllLocationsWithNamePrefix(LOCATION_NAME_PREFIX)).thenReturn(responseMessage);

        ResponseEntity<String> response = testingSupportController.deleteLocationsByNamePrefix(LOCATION_NAME_PREFIX);

        assertThat(response.getStatusCode())
            .as(RESPONSE_STATUS_MESSAGE)
            .isEqualTo(HttpStatus.OK);

        assertThat(response.getBody())
            .as(RESPONSE_BODY_MESSAGE)
            .isEqualTo(responseMessage);
    }

    @Test
    void testDeletePublicationsWithLocationNamePrefixReturnsOk() {
        String responseMessage = "5 artefacts(s) deleted for location name starting with " + LOCATION_NAME_PREFIX;
        when(artefactDeleteService.deleteAllArtefactsWithLocationNamePrefix(LOCATION_NAME_PREFIX))
            .thenReturn(responseMessage);

        ResponseEntity<String> response = testingSupportController.deletePublicationsWithLocationNamePrefix(
            LOCATION_NAME_PREFIX
        );

        assertThat(response.getStatusCode())
            .as(RESPONSE_STATUS_MESSAGE)
            .isEqualTo(HttpStatus.OK);

        assertThat(response.getBody())
            .as(RESPONSE_BODY_MESSAGE)
            .isEqualTo(responseMessage);
    }
}
