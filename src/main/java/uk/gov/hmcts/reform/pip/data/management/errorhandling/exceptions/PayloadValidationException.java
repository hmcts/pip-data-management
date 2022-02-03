package uk.gov.hmcts.reform.pip.data.management.errorhandling.exceptions;

/**
 * Exception that captures when an payload is not validated against the criteria.
 */
public class PayloadValidationException extends RuntimeException {

    private static final long serialVersionUID = -7073660693904036960L;

    /**
     * Constructor for the Exception.
     * @param message The message to return to the end user
     */
    public PayloadValidationException(String message) {
        super(message);
    }
}
