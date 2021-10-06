package uk.gov.hmcts.reform.pip.data.management.errorhandling.exceptions;

/**
 * Exception that captures when a hearing is not found.
 */
public class HearingNotFoundException extends NotFoundException {

    private static final long serialVersionUID = -5431906982662313298L;

    /**
     * Constructor for the Exception.
     * @param message The message to return to the end user
     */
    public HearingNotFoundException(String message) {
        super(message);
    }
}
