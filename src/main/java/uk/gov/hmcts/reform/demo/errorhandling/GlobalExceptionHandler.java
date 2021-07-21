package uk.gov.hmcts.reform.demo.errorhandling;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

import java.time.LocalDateTime;

@ControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Template exception handler, that handles a custom DataStorageNotFoundException,
     * and returns a 404 in the standard format.
     * @param ex The exception that has been thrown.
     * @param request The request made to the endpoint.
     * @return The error response, modelled using the ExceptionResponse object.
     */

    @ExceptionHandler(DataStorageNotFoundException.class)
    ResponseEntity<ExceptionResponse> handleDataStorageNotFound(DataStorageNotFoundException ex, WebRequest request) {

        ExceptionResponse exceptionResponse = new ExceptionResponse();
        exceptionResponse.setMessage(ex.getMessage());
        exceptionResponse.setTimestamp(LocalDateTime.now());

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(exceptionResponse);
    }
}
