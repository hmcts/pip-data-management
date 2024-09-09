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
import uk.gov.hmcts.reform.pip.data.management.errorhandling.exceptions.CreateArtefactConflictException;
import uk.gov.hmcts.reform.pip.data.management.errorhandling.exceptions.CreateLocationConflictException;
import uk.gov.hmcts.reform.pip.data.management.errorhandling.exceptions.CsvParseException;
import uk.gov.hmcts.reform.pip.data.management.errorhandling.exceptions.FileSizeLimitException;
import uk.gov.hmcts.reform.pip.data.management.errorhandling.exceptions.FlatFileException;
import uk.gov.hmcts.reform.pip.data.management.errorhandling.exceptions.HeaderValidationException;
import uk.gov.hmcts.reform.pip.data.management.errorhandling.exceptions.NotFoundException;
import uk.gov.hmcts.reform.pip.data.management.errorhandling.exceptions.PayloadValidationException;
import uk.gov.hmcts.reform.pip.data.management.errorhandling.exceptions.ProcessingException;
import uk.gov.hmcts.reform.pip.data.management.errorhandling.exceptions.UnauthorisedRequestException;

import java.time.LocalDateTime;

import static uk.gov.hmcts.reform.pip.model.LogBuilder.writeLog;

@ControllerAdvice
@Slf4j
@SuppressWarnings("PMD.SystemPrintln")
public class GlobalExceptionHandler {

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ExceptionResponse> handle(NotFoundException ex) {
        log.error(writeLog("404, unable to find entity. Details: " + ex.getMessage()));
        return ResponseEntity.status(HttpStatus.NOT_FOUND).contentType(MediaType.APPLICATION_JSON)
            .body(generateExceptionResponse(ex.getMessage()));
    }

    @ExceptionHandler(MissingRequestHeaderException.class)
    public ResponseEntity<ExceptionResponse> handle(MissingRequestHeaderException ex) {
        log.error(writeLog(String.format("400, request heading %s missing", ex.getHeaderName())));

        ExceptionResponse exceptionResponse = generateExceptionResponse(
            String.format("%s is mandatory however an empty value is provided", ex.getHeaderName())
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(exceptionResponse);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ExceptionResponse> handle(MethodArgumentTypeMismatchException ex) {
        log.error(writeLog("400, provided unknown type for field " + ex.getName()));

        ExceptionResponse exceptionResponse = generateExceptionResponse(String.format(
            "Unable to parse %s. Please check that the value is of the correct format for the field (See Swagger "
                + "documentation for correct formats)", ex.getName()
        ));
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(exceptionResponse);
    }

    @ExceptionHandler(HeaderValidationException.class)
    public ResponseEntity<ExceptionResponse> handle(HeaderValidationException ex) {
        log.error(writeLog("400, error while validating headers"));
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(generateExceptionResponse(ex.getMessage()));
    }

    @ExceptionHandler(PayloadValidationException.class)
    public ResponseEntity<ExceptionResponse> handle(PayloadValidationException ex) {
        log.error(writeLog("400, error while validating JSON payload"));
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(generateExceptionResponse(ex.getMessage()));
    }

    @ExceptionHandler(BlobStorageException.class)
    public ResponseEntity<ExceptionResponse> handle(BlobStorageException ex) {
        log.error(writeLog("404, error while communicating with blob store"));
        System.out.println(ex);
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(generateExceptionResponse(ex.getMessage()));
    }

    @ExceptionHandler(UnauthorisedRequestException.class)
    public ResponseEntity<ExceptionResponse> handle(UnauthorisedRequestException ex) {
        log.error(writeLog("401, user has attempted to access a restricted endpoint"));
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
            .body(generateExceptionResponse(ex.getMessage()));
    }

    @ExceptionHandler(FlatFileException.class)
    public ResponseEntity<ExceptionResponse> handle(FlatFileException ex) {
        log.error(writeLog("400, failure while handling a flat file"));
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(generateExceptionResponse(ex.getMessage()));
    }

    @ExceptionHandler(CsvParseException.class)
    public ResponseEntity<ExceptionResponse> handle(CsvParseException ex) {
        log.error(writeLog("400, error while parsing locations CSV"));
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(generateExceptionResponse(ex.getMessage()));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ExceptionResponse> handle(ConstraintViolationException ex) {
        log.error(writeLog("400, error while validating headers / body"));
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(generateExceptionResponse(ex.getMessage()));
    }

    @ExceptionHandler(CreateArtefactConflictException.class)
    public ResponseEntity<ExceptionResponse> handle(CreateArtefactConflictException ex) {
        log.error(writeLog("409, conflict while uploading publication"));
        return ResponseEntity.status(HttpStatus.CONFLICT)
            .body(generateExceptionResponse(ex.getMessage()));
    }

    @ExceptionHandler(CreateLocationConflictException.class)
    public ResponseEntity<ExceptionResponse> handle(CreateLocationConflictException ex) {
        log.error(writeLog("409, conflict while creating location"));
        return ResponseEntity.status(HttpStatus.CONFLICT)
            .body(generateExceptionResponse(ex.getMessage()));
    }

    @ExceptionHandler(FileSizeLimitException.class)
    public ResponseEntity<ExceptionResponse> handle(FileSizeLimitException ex) {
        log.error(writeLog("413, blob file too large"));
        return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE)
            .body(generateExceptionResponse(ex.getMessage()));
    }

    @ExceptionHandler(ProcessingException.class)
    public ResponseEntity<ExceptionResponse> handle(ProcessingException ex) {
        log.error(writeLog("500, error processing publication files/summary"));
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(generateExceptionResponse(ex.getMessage()));
    }

    private ExceptionResponse generateExceptionResponse(String message) {
        ExceptionResponse exceptionResponse = new ExceptionResponse();
        exceptionResponse.setMessage(message);
        exceptionResponse.setTimestamp(LocalDateTime.now());
        return exceptionResponse;
    }
}
