package uk.gov.hmcts.reform.pip.data.management.errorhandling;

import com.azure.storage.blob.models.BlobStorageException;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import uk.gov.hmcts.reform.pip.data.management.errorhandling.exceptions.CreateArtefactConflictException;
import uk.gov.hmcts.reform.pip.data.management.errorhandling.exceptions.CreateLocationConflictException;
import uk.gov.hmcts.reform.pip.data.management.errorhandling.exceptions.CsvParseException;
import uk.gov.hmcts.reform.pip.data.management.errorhandling.exceptions.ExcelConversionException;
import uk.gov.hmcts.reform.pip.data.management.errorhandling.exceptions.FileSizeLimitException;
import uk.gov.hmcts.reform.pip.data.management.errorhandling.exceptions.FlatFileException;
import uk.gov.hmcts.reform.pip.data.management.errorhandling.exceptions.HeaderValidationException;
import uk.gov.hmcts.reform.pip.data.management.errorhandling.exceptions.LocationNameValidationException;
import uk.gov.hmcts.reform.pip.data.management.errorhandling.exceptions.LocationNotFoundException;
import uk.gov.hmcts.reform.pip.data.management.errorhandling.exceptions.NotFoundException;
import uk.gov.hmcts.reform.pip.data.management.errorhandling.exceptions.PayloadValidationException;
import uk.gov.hmcts.reform.pip.data.management.errorhandling.exceptions.ProcessingException;
import uk.gov.hmcts.reform.pip.data.management.errorhandling.exceptions.PublicationFileNotFoundException;
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
    private static final String BAD_REQUEST_ASSERTION = "Status code should be of type: Bad Request";
    private static final String NOT_FOUND_ASSERTION = "Status code should be of type: Not Found";
    private static final String CONFLICT_ASSERTION = "Status code should be of type: Conflict";
    private static final String PAYLOAD_TOO_LARGE_ASSERTION = "Status code should be of type: Payload Too Large";
    private static final String INTERNAL_SERVER_ERROR_ASSERTION = "Status code should be of type: Internal Server "
        + "Error";
    private static final String EXCEPTION_BODY_NOT_MATCH = "Exception body doesn't match test message";
    static final String ASSERTION_RESPONSE_BODY = "Response should contain a body";
    private static final String NOT_NULL_MESSAGE = "Exception body should not be null";
    private final GlobalExceptionHandler globalExceptionHandler = new GlobalExceptionHandler();

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
                     BAD_REQUEST_ASSERTION
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

    @Test
    void testCreateArtefactConflictException() {
        CreateArtefactConflictException conflictException = new CreateArtefactConflictException(TEST_MESSAGE);
        ResponseEntity<ExceptionResponse> responseEntity = globalExceptionHandler.handle(conflictException);

        assertEquals(HttpStatus.CONFLICT, responseEntity.getStatusCode(), CONFLICT_ASSERTION);
        assertNotNull(responseEntity.getBody(), NOT_NULL_MESSAGE);
        assertTrue(responseEntity.getBody().getMessage().contains(TEST_MESSAGE), EXCEPTION_BODY_NOT_MATCH);
    }

    @Test
    void testCreateLocationConflictException() {
        CreateLocationConflictException conflictException = new CreateLocationConflictException(TEST_MESSAGE);
        ResponseEntity<ExceptionResponse> responseEntity = globalExceptionHandler.handle(conflictException);

        assertEquals(HttpStatus.CONFLICT, responseEntity.getStatusCode(), CONFLICT_ASSERTION);
        assertNotNull(responseEntity.getBody(), NOT_NULL_MESSAGE);
        assertTrue(responseEntity.getBody().getMessage().contains(TEST_MESSAGE), EXCEPTION_BODY_NOT_MATCH);
    }

    @Test
    void testHandleNPublicationFilesNotFoundException() {
        PublicationFileNotFoundException notFoundException = new PublicationFileNotFoundException(TEST_MESSAGE);

        ResponseEntity<ExceptionResponse> responseEntity =
            globalExceptionHandler.handle(notFoundException);

        assertEquals(HttpStatus.NOT_FOUND, responseEntity.getStatusCode(), NOT_FOUND_ASSERTION);
        assertNotNull(responseEntity.getBody(), ASSERTION_RESPONSE_BODY);
        assertEquals(TEST_MESSAGE, responseEntity.getBody().getMessage(),
                     ASSERTION_MESSAGE
        );
    }

    @Test
    void testHandleFileSizeLimitException() {
        FileSizeLimitException exception = new FileSizeLimitException(TEST_MESSAGE);

        ResponseEntity<ExceptionResponse> responseEntity = globalExceptionHandler.handle(exception);

        assertEquals(HttpStatus.PAYLOAD_TOO_LARGE, responseEntity.getStatusCode(), PAYLOAD_TOO_LARGE_ASSERTION);
        assertNotNull(responseEntity.getBody(), ASSERTION_RESPONSE_BODY);
        assertEquals(TEST_MESSAGE, responseEntity.getBody().getMessage(), ASSERTION_MESSAGE);
    }

    @Test
    void testHandleProcessingException() {
        ProcessingException exception = new ProcessingException(TEST_MESSAGE);

        ResponseEntity<ExceptionResponse> responseEntity = globalExceptionHandler.handle(exception);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, responseEntity.getStatusCode(), INTERNAL_SERVER_ERROR_ASSERTION);
        assertNotNull(responseEntity.getBody(), ASSERTION_RESPONSE_BODY);
        assertEquals(TEST_MESSAGE, responseEntity.getBody().getMessage(),
                     ASSERTION_MESSAGE);
    }

    @Test
    void testHandleExcelConversionException() {
        ExcelConversionException exception = new ExcelConversionException(TEST_MESSAGE);

        ResponseEntity<ExceptionResponse> responseEntity = globalExceptionHandler.handle(exception);

        assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode(), BAD_REQUEST_ASSERTION);
        assertNotNull(responseEntity.getBody(), ASSERTION_RESPONSE_BODY);
        assertEquals(TEST_MESSAGE, responseEntity.getBody().getMessage(), ASSERTION_MESSAGE);
    }

    @Test
    void testHandleLocationNameValidationException() {
        LocationNameValidationException exception = new LocationNameValidationException(TEST_MESSAGE);

        ResponseEntity<ExceptionResponse> responseEntity = globalExceptionHandler.handle(exception);

        assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode(), BAD_REQUEST_ASSERTION);
        assertNotNull(responseEntity.getBody(), ASSERTION_RESPONSE_BODY);
        assertEquals(TEST_MESSAGE, responseEntity.getBody().getMessage(),
                     ASSERTION_MESSAGE);
    }
}
