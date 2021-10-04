package uk.gov.hmcts.reform.pip.data.management.errorhandling;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.reform.pip.data.management.errorhandling.exceptions.CourtNotFoundException;
import uk.gov.hmcts.reform.pip.data.management.errorhandling.exceptions.DataStorageNotFoundException;
import uk.gov.hmcts.reform.pip.data.management.errorhandling.exceptions.HearingNotFoundException;
import uk.gov.hmcts.reform.pip.data.management.errorhandling.exceptions.NotFoundException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class GlobalExceptionHandlerTest {

    private static final String TEST_MESSAGE = "This is a test message";
    private static final String ASSERTION_MESSAGE = "The message should match the message passed in";
    private static final String NOT_FOUND_ASSERTION = "Status code should be of type: Not Found";
    public static final String ASSERTION_RESPONSE_BODY = "Response should contain a body";

    private final GlobalExceptionHandler globalExceptionHandler = new GlobalExceptionHandler();

    @Test
    @DisplayName("Test that the response entity returned from the exception handler, "
        + "contains the expected status code and body")
    public void testHandleSubscriptionNotFoundMethod() {

        DataStorageNotFoundException dataStorageNotFoundException
            = new DataStorageNotFoundException(TEST_MESSAGE);

        ResponseEntity<ExceptionResponse> responseEntity =
            globalExceptionHandler.handle(dataStorageNotFoundException);

        assertEquals(HttpStatus.NOT_FOUND, responseEntity.getStatusCode(), NOT_FOUND_ASSERTION);
        assertNotNull(responseEntity.getBody(), ASSERTION_RESPONSE_BODY);
        assertEquals(TEST_MESSAGE, responseEntity.getBody().getMessage(),
                     ASSERTION_MESSAGE);
    }

    @Test
    public void testHandleNotFoundException() {
        NotFoundException notFoundException = new NotFoundException(TEST_MESSAGE);

        ResponseEntity<ExceptionResponse> responseEntity =
            globalExceptionHandler.handle(notFoundException);

        assertEquals(HttpStatus.NOT_FOUND, responseEntity.getStatusCode(), NOT_FOUND_ASSERTION);
        assertNotNull(responseEntity.getBody(), ASSERTION_RESPONSE_BODY);
        assertEquals(TEST_MESSAGE, responseEntity.getBody().getMessage(),
                     ASSERTION_MESSAGE);
    }

    @Test
    public void testHandleCourtNotFoundException() {
        CourtNotFoundException courtNotFoundException = new CourtNotFoundException(TEST_MESSAGE);

        ResponseEntity<ExceptionResponse> responseEntity =
            globalExceptionHandler.handle(courtNotFoundException);

        assertEquals(HttpStatus.NOT_FOUND, responseEntity.getStatusCode(), NOT_FOUND_ASSERTION);
        assertNotNull(responseEntity.getBody(), ASSERTION_RESPONSE_BODY);
        assertEquals(TEST_MESSAGE, responseEntity.getBody().getMessage(),
                     ASSERTION_MESSAGE);
    }

    @Test
    public void testHandleHearingNotFoundException() {
        HearingNotFoundException hearingNotFoundException = new HearingNotFoundException(TEST_MESSAGE);

        ResponseEntity<ExceptionResponse> responseEntity =
            globalExceptionHandler.handle(hearingNotFoundException);

        assertEquals(HttpStatus.NOT_FOUND, responseEntity.getStatusCode(), NOT_FOUND_ASSERTION);
        assertNotNull(responseEntity.getBody(), ASSERTION_RESPONSE_BODY);
        assertEquals(TEST_MESSAGE, responseEntity.getBody().getMessage(),
                     ASSERTION_MESSAGE);
    }
}
