package uk.gov.hmcts.reform.pip.data.management.controllers.tests;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.reform.pip.data.management.controllers.RootController;

import static org.junit.jupiter.api.Assertions.assertEquals;

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

}
