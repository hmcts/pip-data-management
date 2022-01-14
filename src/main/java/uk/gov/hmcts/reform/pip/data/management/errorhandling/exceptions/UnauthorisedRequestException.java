package uk.gov.hmcts.reform.pip.data.management.errorhandling.exceptions;

/**
 * Exception that captures the message when an unauthorised request is made to the database.
 */
public class UnauthorisedRequestException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /**
     * Constructor for the Exception.
     *
     * @param message The message to return to the end user
     */
    public UnauthorisedRequestException(String message) {
        super(message);
    }
}
