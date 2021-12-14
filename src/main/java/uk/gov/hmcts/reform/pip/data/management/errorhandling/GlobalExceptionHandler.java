package uk.gov.hmcts.reform.pip.data.management.errorhandling;

import com.azure.storage.blob.models.BlobStorageException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import uk.gov.hmcts.reform.pip.data.management.errorhandling.exceptions.DataStorageNotFoundException;
import uk.gov.hmcts.reform.pip.data.management.errorhandling.exceptions.EmptyRequestHeaderException;
import uk.gov.hmcts.reform.pip.data.management.errorhandling.exceptions.NotFoundException;

import java.time.LocalDateTime;
import java.util.Objects;

@ControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Template exception handler, that handles a custom DataStorageNotFoundException,
     * and returns a 404 in the standard format.
     * @param ex The exception that has been thrown.
     * @return The error response, modelled using the ExceptionResponse object.
     */

    @ExceptionHandler(DataStorageNotFoundException.class)
    public ResponseEntity<ExceptionResponse> handle(DataStorageNotFoundException ex) {

        ExceptionResponse exceptionResponse = new ExceptionResponse();
        exceptionResponse.setMessage(ex.getMessage());
        exceptionResponse.setTimestamp(LocalDateTime.now());

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(exceptionResponse);
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ExceptionResponse> handle(NotFoundException ex) {

        ExceptionResponse exceptionResponse = new ExceptionResponse();
        exceptionResponse.setMessage(ex.getMessage());
        exceptionResponse.setTimestamp(LocalDateTime.now());

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(exceptionResponse);
    }

    @ExceptionHandler(MissingRequestHeaderException.class)
    public ResponseEntity<ExceptionResponse> handle(MissingRequestHeaderException ex) {

        ExceptionResponse exceptionResponse = new ExceptionResponse();
        exceptionResponse.setMessage(ex.getMessage());
        exceptionResponse.setTimestamp(LocalDateTime.now());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(exceptionResponse);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ExceptionResponse> handle(MethodArgumentTypeMismatchException ex) {

        ExceptionResponse exceptionResponse = new ExceptionResponse();
        exceptionResponse.setMessage(String.format(
            "Unable to parse %s. Please check that the value is of the correct format for the field "
                + "(See Swagger documentation for correct formats)", ex.getName()));
        exceptionResponse.setTimestamp(LocalDateTime.now());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(exceptionResponse);
    }

    @ExceptionHandler(EmptyRequestHeaderException.class)
    public ResponseEntity<ExceptionResponse> handle(EmptyRequestHeaderException ex) {

        ExceptionResponse exceptionResponse = new ExceptionResponse();
        exceptionResponse.setMessage(ex.getMessage());
        exceptionResponse.setTimestamp(LocalDateTime.now());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(exceptionResponse);
    }

    @ExceptionHandler(BlobStorageException.class)
        public ResponseEntity<ExceptionResponse> handle(BlobStorageException ex) {
        ExceptionResponse exceptionResponse = new ExceptionResponse();
        if(Objects.equals(ex.getErrorCode().toString(), "BlobNotFound")) {
            exceptionResponse.setMessage("404: Unable to find a blob matching the given inputs");
        }
        else{
            exceptionResponse.setMessage(ex.getErrorCode().toString());
        }
        exceptionResponse.setTimestamp(LocalDateTime.now());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(exceptionResponse);
    }


}
