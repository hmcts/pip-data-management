package uk.gov.hmcts.reform.pip.data.management.errorhandling.exceptions;

/**
 * Exception that captures when an input is not validate against the criteria.
 */
public class ValidationException extends RuntimeException {

    private static final long serialVersionUID = -5431906982662313298L;

    /**
     * Constructor for the Exception.
     * @param message The message to return to the end user
     */
    public ValidationException(String message) {
        super(message);
    }
}
