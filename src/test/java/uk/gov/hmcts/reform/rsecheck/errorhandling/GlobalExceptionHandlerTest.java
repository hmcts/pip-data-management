package uk.gov.hmcts.reform.rsecheck.errorhandling;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.reform.pip.data.management.errorhandling.DataStorageNotFoundException;
import uk.gov.hmcts.reform.pip.data.management.errorhandling.ExceptionResponse;
import uk.gov.hmcts.reform.pip.data.management.errorhandling.GlobalExceptionHandler;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class GlobalExceptionHandlerTest {

    @Test
    @DisplayName("Test that the response entity returned from the exception handler, "
        + "contains the expected status code and body")
    void testHandleSubscriptionNotFoundMethod() {

        GlobalExceptionHandler globalExceptionHandler = new GlobalExceptionHandler();

        DataStorageNotFoundException dataStorageNotFoundException
            = new DataStorageNotFoundException("This is a test message");

        ResponseEntity<ExceptionResponse> responseEntity =
            globalExceptionHandler.handleDataStorageNotFound(dataStorageNotFoundException);

        assertEquals(HttpStatus.NOT_FOUND, responseEntity.getStatusCode(), "Status code should be not found");
        assertNotNull(responseEntity.getBody(), "Response should contain a body");
        assertEquals("This is a test message", responseEntity.getBody().getMessage(),
                     "The message should match the message passed in");
    }
}
