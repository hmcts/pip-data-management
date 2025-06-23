package uk.gov.hmcts.reform.pip.data.management.errorhandling.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.CONFLICT, reason = "Location metadata already exists")
public class CreateLocationMetadataConflictException extends RuntimeException {
    private static final long serialVersionUID = 3662821754485634512L;

    /**
     * Constructor for the Exception.
     * @param message The message to return to the end user
     */
    public CreateLocationMetadataConflictException(String message) {
        super(message);
    }
}
