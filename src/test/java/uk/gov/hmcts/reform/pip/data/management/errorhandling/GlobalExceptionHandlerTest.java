package uk.gov.hmcts.reform.pip.data.management.errorhandling;

import com.azure.storage.blob.models.BlobStorageException;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import uk.gov.hmcts.reform.pip.data.management.errorhandling.exceptions.CsvParseException;
import uk.gov.hmcts.reform.pip.data.management.errorhandling.exceptions.DataStorageNotFoundException;
import uk.gov.hmcts.reform.pip.data.management.errorhandling.exceptions.FlatFileException;
import uk.gov.hmcts.reform.pip.data.management.errorhandling.exceptions.HeaderValidationException;
import uk.gov.hmcts.reform.pip.data.management.errorhandling.exceptions.HearingNotFoundException;
import uk.gov.hmcts.reform.pip.data.management.errorhandling.exceptions.LocationNotFoundException;
import uk.gov.hmcts.reform.pip.data.management.errorhandling.exceptions.NotFoundException;
import uk.gov.hmcts.reform.pip.data.management.errorhandling.exceptions.PayloadValidationException;
import uk.gov.hmcts.reform.pip.data.management.errorhandling.exceptions.UnauthorisedRequestException;

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

    @Mock
    BlobStorageException blobStorageException;

    private static final String TEST_MESSAGE = "This is a test message";
    private static final String TEST_NAME = "TestName";
    private static final String ASSERTION_MESSAGE = "The message should match the message passed in";
    private static final String BAD_REQUEST_ASSERTION = "Status code should be of type: Not Found";
    private static final String NOT_FOUND_ASSERTION = "Status code should be of type: Bad Request";
    private static final String EXCEPTION_BODY_NOT_MATCH = "Exception body doesn't match test message";
    static final String ASSERTION_RESPONSE_BODY = "Response should contain a body";
    private static final String NOT_NULL_MESSAGE = "Exception body should not be null";
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
                     ASSERTION_MESSAGE
        );
    }

    @Test
    void testHandleNotFoundException() {
        NotFoundException notFoundException = new NotFoundException(TEST_MESSAGE);

        ResponseEntity<ExceptionResponse> responseEntity =
            globalExceptionHandler.handle(notFoundException);

        assertEquals(HttpStatus.NOT_FOUND, responseEntity.getStatusCode(), NOT_FOUND_ASSERTION);
        assertNotNull(responseEntity.getBody(), ASSERTION_RESPONSE_BODY);
        assertEquals(TEST_MESSAGE, responseEntity.getBody().getMessage(),
                     ASSERTION_MESSAGE
        );
    }

    @Test
    void testHandleCourtNotFoundException() {
        LocationNotFoundException locationNotFoundException = new LocationNotFoundException(TEST_MESSAGE);

        ResponseEntity<ExceptionResponse> responseEntity =
            globalExceptionHandler.handle(locationNotFoundException);

        assertEquals(HttpStatus.NOT_FOUND, responseEntity.getStatusCode(), NOT_FOUND_ASSERTION);
        assertNotNull(responseEntity.getBody(), ASSERTION_RESPONSE_BODY);
        assertEquals(TEST_MESSAGE, responseEntity.getBody().getMessage(),
                     ASSERTION_MESSAGE
        );
    }

    @Test
    void testHandlePayloadValidationException() {
        PayloadValidationException payloadValidationException = new PayloadValidationException(
            TEST_MESSAGE);

        ResponseEntity<ExceptionResponse> responseEntity =
            globalExceptionHandler.handle(payloadValidationException);

        assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode(), BAD_REQUEST_ASSERTION);
        assertNotNull(responseEntity.getBody(), ASSERTION_RESPONSE_BODY);
        assertEquals(TEST_MESSAGE, responseEntity.getBody().getMessage(),
                     ASSERTION_MESSAGE
        );
    }

    @Test
    void testHandleHeaderValidationException() {
        HeaderValidationException headerValidationException = new HeaderValidationException(
            TEST_MESSAGE);

        ResponseEntity<ExceptionResponse> responseEntity =
            globalExceptionHandler.handle(headerValidationException);

        assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode(), BAD_REQUEST_ASSERTION);
        assertNotNull(responseEntity.getBody(), ASSERTION_RESPONSE_BODY);
        assertEquals(TEST_MESSAGE, responseEntity.getBody().getMessage(),
                     ASSERTION_MESSAGE
        );
    }

    @Test
    void testHandleHearingNotFoundException() {
        HearingNotFoundException hearingNotFoundException = new HearingNotFoundException(TEST_MESSAGE);

        ResponseEntity<ExceptionResponse> responseEntity =
            globalExceptionHandler.handle(hearingNotFoundException);

        assertEquals(HttpStatus.NOT_FOUND, responseEntity.getStatusCode(), NOT_FOUND_ASSERTION);
        assertNotNull(responseEntity.getBody(), ASSERTION_RESPONSE_BODY);
        assertEquals(TEST_MESSAGE, responseEntity.getBody().getMessage(),
                     ASSERTION_MESSAGE
        );
    }

    @Test
    void testMissingRequestHeaderException() {

        when(missingRequestHeaderException.getHeaderName()).thenReturn(TEST_MESSAGE);

        ResponseEntity<ExceptionResponse> responseEntity =
            globalExceptionHandler.handle(missingRequestHeaderException);

        assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode(), BAD_REQUEST_ASSERTION);
        assertNotNull(responseEntity.getBody(), ASSERTION_RESPONSE_BODY);
        assertEquals(TEST_MESSAGE + " is mandatory however an empty value is provided",
                     responseEntity.getBody().getMessage(),
                     ASSERTION_MESSAGE
        );
    }

    @Test
    void testMethodArgumentTypeMismatchException() {
        when(methodArgumentTypeMismatchException.getName()).thenReturn(TEST_NAME);

        ResponseEntity<ExceptionResponse> responseEntity =
            globalExceptionHandler.handle(methodArgumentTypeMismatchException);

        assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode(), BAD_REQUEST_ASSERTION);
        assertNotNull(responseEntity.getBody(), ASSERTION_RESPONSE_BODY);
        assertTrue(
            responseEntity.getBody().getMessage().contains(
                String.format("Unable to parse %s. Please check that the value is of the correct format for the field "

                                  + "(See Swagger documentation for correct formats)", TEST_NAME)),
            "The exception response should contain the name of the field"
        );
    }

    @Test
    void testBlobStorageException() {
        when(blobStorageException.getMessage()).thenReturn(TEST_MESSAGE);

        ResponseEntity<ExceptionResponse> responseEntity =
            globalExceptionHandler.handle(blobStorageException);

        assertEquals(HttpStatus.NOT_FOUND, responseEntity.getStatusCode(), BAD_REQUEST_ASSERTION);
        assertNotNull(responseEntity.getBody(), ASSERTION_RESPONSE_BODY);
        assertTrue(
            responseEntity.getBody().getMessage().contains(TEST_MESSAGE),
            EXCEPTION_BODY_NOT_MATCH
        );

    }

    @Test
    void testUnauthorisedRequestException() {
        UnauthorisedRequestException unauthorisedRequestException = new UnauthorisedRequestException(TEST_MESSAGE);
        ResponseEntity<ExceptionResponse> responseEntity =
            globalExceptionHandler.handle(unauthorisedRequestException);
        assertEquals(HttpStatus.UNAUTHORIZED, responseEntity.getStatusCode(),
                     "Should be unauthorised exception"
        );
        assertNotNull(responseEntity.getBody(), NOT_NULL_MESSAGE);
        assertTrue(
            responseEntity.getBody().getMessage().contains(TEST_MESSAGE),
            EXCEPTION_BODY_NOT_MATCH
        );
    }

    @Test
    void testFlatFileIoException() {
        FlatFileException flatFileException = new FlatFileException(TEST_MESSAGE);
        ResponseEntity<ExceptionResponse> responseEntity =
            globalExceptionHandler.handle(flatFileException);
        assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode(),
                     "Should be unauthorised exception"
        );
        assertNotNull(responseEntity.getBody(), NOT_NULL_MESSAGE);
        assertTrue(
            responseEntity.getBody().getMessage()
                .contains(TEST_MESSAGE),
            EXCEPTION_BODY_NOT_MATCH
        );
    }

    @Test
    void testCsvParseException() {
        CsvParseException csvParseException = new CsvParseException(TEST_MESSAGE);

        ResponseEntity<ExceptionResponse> responseEntity =
            globalExceptionHandler.handle(csvParseException);

        assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode(),
                     "Should be bad request exception"
        );
        assertNotNull(responseEntity.getBody(), NOT_NULL_MESSAGE);
        assertTrue(
            responseEntity.getBody().getMessage()
                .contains(TEST_MESSAGE),
            EXCEPTION_BODY_NOT_MATCH
        );
    }

    @Test
    void testConstrainViolationException() {
        ConstraintViolationException constraintViolationException =
            new ConstraintViolationException(TEST_MESSAGE, null);

        ResponseEntity<ExceptionResponse> responseEntity = globalExceptionHandler.handle(constraintViolationException);

        assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode(),
                     "Should be bad request exception");
        assertNotNull(responseEntity.getBody(), NOT_NULL_MESSAGE);
        assertTrue(responseEntity.getBody().getMessage().contains(TEST_MESSAGE), EXCEPTION_BODY_NOT_MATCH);
    }

}
