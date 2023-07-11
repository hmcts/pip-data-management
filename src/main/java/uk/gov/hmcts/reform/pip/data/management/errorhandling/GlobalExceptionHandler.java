package uk.gov.hmcts.reform.pip.data.management.errorhandling;

import com.azure.storage.blob.models.BlobStorageException;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import uk.gov.hmcts.reform.pip.data.management.errorhandling.exceptions.CsvParseException;
import uk.gov.hmcts.reform.pip.data.management.errorhandling.exceptions.DataStorageNotFoundException;
import uk.gov.hmcts.reform.pip.data.management.errorhandling.exceptions.FlatFileException;
import uk.gov.hmcts.reform.pip.data.management.errorhandling.exceptions.HeaderValidationException;
import uk.gov.hmcts.reform.pip.data.management.errorhandling.exceptions.NotFoundException;
import uk.gov.hmcts.reform.pip.data.management.errorhandling.exceptions.PayloadValidationException;
import uk.gov.hmcts.reform.pip.data.management.errorhandling.exceptions.UnauthorisedRequestException;

import java.time.LocalDateTime;

import static uk.gov.hmcts.reform.pip.model.LogBuilder.writeLog;

@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * Template exception handler, that handles a custom DataStorageNotFoundException,
     * and returns a 404 in the standard format.
     *
     * @param ex The exception that has been thrown.
     * @return The error response, modelled using the ExceptionResponse object.
     */

    @ExceptionHandler(DataStorageNotFoundException.class)
    public ResponseEntity<ExceptionResponse> handle(DataStorageNotFoundException ex) {

        log.error(writeLog("404, failure when connecting to Blob store"));

        ExceptionResponse exceptionResponse = new ExceptionResponse();
        exceptionResponse.setMessage(ex.getMessage());
        exceptionResponse.setTimestamp(LocalDateTime.now());

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(exceptionResponse);
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ExceptionResponse> handle(NotFoundException ex) {

        log.error(writeLog("404, unable to find artefact. Details: " + ex.getMessage()));

        ExceptionResponse exceptionResponse = new ExceptionResponse();
        exceptionResponse.setMessage(ex.getMessage());
        exceptionResponse.setTimestamp(LocalDateTime.now());

        return ResponseEntity.status(HttpStatus.NOT_FOUND).contentType(MediaType.APPLICATION_JSON)
            .body(exceptionResponse);
    }

    @ExceptionHandler(MissingRequestHeaderException.class)
    public ResponseEntity<ExceptionResponse> handle(MissingRequestHeaderException ex) {

        log.error(writeLog(String.format("400, request heading %s missing", ex.getHeaderName())));

        ExceptionResponse exceptionResponse = new ExceptionResponse();
        exceptionResponse.setMessage(
            String.format("%s is mandatory however an empty value is provided", ex.getHeaderName()));
        exceptionResponse.setTimestamp(LocalDateTime.now());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(exceptionResponse);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ExceptionResponse> handle(MethodArgumentTypeMismatchException ex) {

        log.error(writeLog("400, provided unknown type for field " + ex.getName()));

        ExceptionResponse exceptionResponse = new ExceptionResponse();
        exceptionResponse.setMessage(String.format(
            "Unable to parse %s. Please check that the value is of the correct format for the field "
                + "(See Swagger documentation for correct formats)", ex.getName()));
        exceptionResponse.setTimestamp(LocalDateTime.now());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(exceptionResponse);
    }

    @ExceptionHandler(HeaderValidationException.class)
    public ResponseEntity<ExceptionResponse> handle(HeaderValidationException ex) {

        log.error(writeLog("400, error while validating headers"));

        ExceptionResponse exceptionResponse = new ExceptionResponse();
        exceptionResponse.setMessage(ex.getMessage());
        exceptionResponse.setTimestamp(LocalDateTime.now());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(exceptionResponse);
    }

    @ExceptionHandler(PayloadValidationException.class)
    public ResponseEntity<ExceptionResponse> handle(PayloadValidationException ex) {

        log.error(writeLog("400, error while validating JSON payload"));

        ExceptionResponse exceptionResponse = new ExceptionResponse();
        exceptionResponse.setMessage(ex.getMessage());
        exceptionResponse.setTimestamp(LocalDateTime.now());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(exceptionResponse);
    }

    @ExceptionHandler(BlobStorageException.class)
    public ResponseEntity<ExceptionResponse> handle(BlobStorageException ex) {

        log.error(writeLog("404, error while communicating with blob store"));

        ExceptionResponse exceptionResponse = new ExceptionResponse();
        exceptionResponse.setMessage(ex.getMessage());
        exceptionResponse.setTimestamp(LocalDateTime.now());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(exceptionResponse);
    }

    @ExceptionHandler(UnauthorisedRequestException.class)
    public ResponseEntity<ExceptionResponse> handle(UnauthorisedRequestException ex) {

        log.error(writeLog("401, user has attempted to access a restricted endpoint"));

        ExceptionResponse exceptionResponse = new ExceptionResponse();
        exceptionResponse.setMessage(ex.getMessage());
        exceptionResponse.setTimestamp(LocalDateTime.now());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(exceptionResponse);
    }

    @ExceptionHandler(FlatFileException.class)
    public ResponseEntity<ExceptionResponse> handle(FlatFileException ex) {

        log.error(writeLog("400, failure while handling a flat file"));

        ExceptionResponse exceptionResponse = new ExceptionResponse();
        exceptionResponse.setMessage(ex.getMessage());
        exceptionResponse.setTimestamp(LocalDateTime.now());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(exceptionResponse);
    }

    @ExceptionHandler(CsvParseException.class)
    public ResponseEntity<ExceptionResponse> handle(CsvParseException ex) {

        log.error(writeLog("400, error while parsing locations CSV"));

        ExceptionResponse exceptionResponse = new ExceptionResponse();
        exceptionResponse.setMessage(ex.getMessage());
        exceptionResponse.setTimestamp(LocalDateTime.now());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(exceptionResponse);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ExceptionResponse> handle(ConstraintViolationException ex) {

        log.error(writeLog("400, error while validating headers / body"));

        ExceptionResponse exceptionResponse = new ExceptionResponse();
        exceptionResponse.setMessage(ex.getMessage());
        exceptionResponse.setTimestamp(LocalDateTime.now());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(exceptionResponse);
    }

}
