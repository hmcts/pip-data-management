package uk.gov.hmcts.reform.pip.data.management.controllers;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.reform.pip.data.management.errorhandling.exceptions.DataStorageNotFoundException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class RootControllerTest {

    private final RootController rootController = new RootController();

    @Test
    @DisplayName("Tests that a 200 code is returned, with the correct response message")
    void testWelcomeMessage() {
        ResponseEntity<String> welcomeResponse = rootController.welcome();
        assertEquals(HttpStatus.OK, welcomeResponse.getStatusCode(), "An OK response code is returned");
        assertEquals("Welcome to pip-data-management", welcomeResponse.getBody(),
                     "The correct response body is returned");
    }

    @Test
    @DisplayName("Check that an exception is thrown when the saveFile method is called")
    void testSaveFileReturnsExpectedException() {
        DataStorageNotFoundException dataStorageNotFoundException =
            assertThrows(DataStorageNotFoundException.class, () -> rootController.saveFile(),
                         "DataStorageNotFoundException has been thrown");

        assertNotNull(dataStorageNotFoundException.getMessage(), "Exception contains a message");
    }

}
