package uk.gov.hmcts.reform.pip.data.management.errorhandling.exceptions;

/**
 * This exception captures errors persisting to the Azure storage for publications.
 */
public class AzureServerException extends RuntimeException {

    private static final long serialVersionUID = -3678523910058431435L;

    /**
     * Constructor for the Publication exception.
     * @param message The message to return to the end user.
     */
    public AzureServerException(String message) {
        super(message);
    }
}
