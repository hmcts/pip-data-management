package uk.gov.hmcts.reform.pip.data.management.errorhandling.exceptions;

/**
 * Exception class that represents the error when a duplicate publication is present.
 */
public class DuplicatePublicationException extends RuntimeException {

    private static final long serialVersionUID = -4369281283815917610L;

    /**
     * Constructor for the Duplicate Publication exception.
     * @param message The message to return to the end user.
     */
    public DuplicatePublicationException(String message) {
        super(message);
    }

}
