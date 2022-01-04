package uk.gov.hmcts.reform.pip.data.management.errorhandling;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import uk.gov.hmcts.reform.pip.data.management.errorhandling.exceptions.CourtNotFoundException;
import uk.gov.hmcts.reform.pip.data.management.errorhandling.exceptions.DataStorageNotFoundException;
import uk.gov.hmcts.reform.pip.data.management.errorhandling.exceptions.EmptyRequestHeaderException;
import uk.gov.hmcts.reform.pip.data.management.errorhandling.exceptions.HearingNotFoundException;
import uk.gov.hmcts.reform.pip.data.management.errorhandling.exceptions.NotFoundException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GlobalExceptionHandlerTest {

    @Mock
    MissingRequestHeaderException missingRequestHeaderException;

    @Mock
    MethodArgumentTypeMismatchException methodArgumentTypeMismatchException;

    private static final String TEST_MESSAGE = "This is a test message";
    private static final String TEST_NAME = "TestName";
    private static final String ASSERTION_MESSAGE = "The message should match the message passed in";
    private static final String BAD_REQUEST_ASSERTION = "Status code should be of type: Not Found";
    private static final String NOT_FOUND_ASSERTION = "Status code should be of type: Bad Request";
    static final String ASSERTION_RESPONSE_BODY = "Response should contain a body";

    private final GlobalExceptionHandler globalExceptionHandler = new GlobalExceptionHandler();

    @BeforeAll
    public static void setup() {
        MockitoAnnotations.openMocks(MissingRequestHeaderException.class);
        MockitoAnnotations.openMocks(MethodArgumentTypeMismatchException.class);
    }

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
    void testMissingRequestHeaderException() {

        when(missingRequestHeaderException.getMessage()).thenReturn(TEST_MESSAGE);

        ResponseEntity<ExceptionResponse> responseEntity =
            globalExceptionHandler.handle(missingRequestHeaderException);

        assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode(), BAD_REQUEST_ASSERTION);
        assertNotNull(responseEntity.getBody(), ASSERTION_RESPONSE_BODY);
        assertEquals(TEST_MESSAGE, responseEntity.getBody().getMessage(),
                     ASSERTION_MESSAGE);
    }

    @Test
    void testMethodArgumentTypeMismatchException() {
        when(methodArgumentTypeMismatchException.getName()).thenReturn(TEST_NAME);

        ResponseEntity<ExceptionResponse> responseEntity =
            globalExceptionHandler.handle(methodArgumentTypeMismatchException);

        assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode(), BAD_REQUEST_ASSERTION);
        assertNotNull(responseEntity.getBody(), ASSERTION_RESPONSE_BODY);
        assertTrue(responseEntity.getBody().getMessage().contains(
            String.format("Unable to parse %s. Please check that the value is of the correct format for the field "
            + "(See Swagger documentation for correct formats)", TEST_NAME)),
                   "The exception response should contain the name of the field");
    }

    @Test
    void testEmptyRequestHandlerException() {

        EmptyRequestHeaderException emptyRequestHeaderException =
            new EmptyRequestHeaderException(TEST_MESSAGE);

        ResponseEntity<ExceptionResponse> responseEntity =
            globalExceptionHandler.handle(emptyRequestHeaderException);

        assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode(), BAD_REQUEST_ASSERTION);
        assertNotNull(responseEntity.getBody(), ASSERTION_RESPONSE_BODY);
        assertTrue(responseEntity.getBody().getMessage().contains(TEST_MESSAGE),
                   "The exception response should contain the message");
    }

}
