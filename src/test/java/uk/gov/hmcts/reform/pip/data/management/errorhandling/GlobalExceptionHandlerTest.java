package uk.gov.hmcts.reform.pip.data.management.errorhandling;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.reform.pip.data.management.errorhandling.exceptions.CourtNotFoundException;
import uk.gov.hmcts.reform.pip.data.management.errorhandling.exceptions.DataStorageNotFoundException;
import uk.gov.hmcts.reform.pip.data.management.errorhandling.exceptions.DuplicatePublicationException;
import uk.gov.hmcts.reform.pip.data.management.errorhandling.exceptions.HearingNotFoundException;
import uk.gov.hmcts.reform.pip.data.management.errorhandling.exceptions.NotFoundException;
import uk.gov.hmcts.reform.pip.data.management.errorhandling.exceptions.PublicationException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class GlobalExceptionHandlerTest {

    private static final String TEST_MESSAGE = "This is a test message";
    private static final String ASSERTION_MESSAGE = "The message should match the message passed in";
    private static final String NOT_FOUND_ASSERTION = "Status code should be of type: Not Found";
    private static final String PUBLICATION_ERROR = "Status code should be of type: Internal Server Error";
    private static final String DUPLICATE_PUBLICATION = "Status code should be of type: Internal Server Error";
    static final String ASSERTION_RESPONSE_BODY = "Response should contain a body";

    private final GlobalExceptionHandler globalExceptionHandler = new GlobalExceptionHandler();

    @Test
    @DisplayName("Test that the response entity returned from the exception handler, "
        + "contains the expected status code and body")
    void testHandleSubscriptionNotFoundMethod() {

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
    void testHandleNotFoundException() {
        NotFoundException notFoundException = new NotFoundException(TEST_MESSAGE);

        ResponseEntity<ExceptionResponse> responseEntity =
            globalExceptionHandler.handle(notFoundException);

        assertEquals(HttpStatus.NOT_FOUND, responseEntity.getStatusCode(), NOT_FOUND_ASSERTION);
        assertNotNull(responseEntity.getBody(), ASSERTION_RESPONSE_BODY);
        assertEquals(TEST_MESSAGE, responseEntity.getBody().getMessage(),
                     ASSERTION_MESSAGE);
    }

    @Test
    void testHandleCourtNotFoundException() {
        CourtNotFoundException courtNotFoundException = new CourtNotFoundException(TEST_MESSAGE);

        ResponseEntity<ExceptionResponse> responseEntity =
            globalExceptionHandler.handle(courtNotFoundException);

        assertEquals(HttpStatus.NOT_FOUND, responseEntity.getStatusCode(), NOT_FOUND_ASSERTION);
        assertNotNull(responseEntity.getBody(), ASSERTION_RESPONSE_BODY);
        assertEquals(TEST_MESSAGE, responseEntity.getBody().getMessage(),
                     ASSERTION_MESSAGE);
    }

    @Test
    void testHandleHearingNotFoundException() {
        HearingNotFoundException hearingNotFoundException = new HearingNotFoundException(TEST_MESSAGE);

        ResponseEntity<ExceptionResponse> responseEntity =
            globalExceptionHandler.handle(hearingNotFoundException);

        assertEquals(HttpStatus.NOT_FOUND, responseEntity.getStatusCode(), NOT_FOUND_ASSERTION);
        assertNotNull(responseEntity.getBody(), ASSERTION_RESPONSE_BODY);
        assertEquals(TEST_MESSAGE, responseEntity.getBody().getMessage(),
                     ASSERTION_MESSAGE);
    }

    @Test
    void testHandlePublicationException() {
        PublicationException publicationException = new PublicationException(TEST_MESSAGE);

        ResponseEntity<ExceptionResponse> responseEntity =
            globalExceptionHandler.handle(publicationException);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, responseEntity.getStatusCode(), PUBLICATION_ERROR);
        assertNotNull(responseEntity.getBody(), ASSERTION_RESPONSE_BODY);
        assertEquals(TEST_MESSAGE, responseEntity.getBody().getMessage(),
                     ASSERTION_MESSAGE);
    }

    @Test
    void testHandleDuplicatePublicationException() {
        DuplicatePublicationException duplicatePublicationException = new DuplicatePublicationException(TEST_MESSAGE);

        ResponseEntity<ExceptionResponse> responseEntity =
            globalExceptionHandler.handle(duplicatePublicationException);

        assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode(), DUPLICATE_PUBLICATION);
        assertNotNull(responseEntity.getBody(), ASSERTION_RESPONSE_BODY);
        assertEquals(TEST_MESSAGE, responseEntity.getBody().getMessage(),
                     ASSERTION_MESSAGE);
    }
}
