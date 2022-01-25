package uk.gov.hmcts.reform.pip.data.management.errorhandling.exceptions;

/**
 * Exception that captures when an input is not validated against the criteria.
 */
public class ValidationException extends RuntimeException {

    private static final long serialVersionUID = -7073660693904036960L;

    /**
     * Constructor for the Exception.
     * @param message The message to return to the end user
     */
    public ValidationException(String message) {
        super(message);
    }
}
