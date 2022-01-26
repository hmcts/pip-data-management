package uk.gov.hmcts.reform.pip.data.management.errorhandling.exceptions;


/**
 * Custom exception which handles all header validation issues.
 */

public class HeaderValidationException extends RuntimeException {
    private static final long serialVersionUID = 6994056611414432079L;

    /**
     * Constructor for the HeaderValidationException.
     * @param message The message relating to the header that was invalid.
     */
    public HeaderValidationException(String message) {
        super(message);
    }
}
