package uk.gov.hmcts.reform.pip.data.management.errorhandling.exceptions;

/**
 * Custom exception which handles all header validation issues.
 */
public class EmptyRequiredHeaderException extends HeaderValidationException {
    private static final long serialVersionUID = 3910456611414432879L;
    /**
     * Constructor for the HeaderValidationException.
     * @param message The message relating to the header that was invalid.
     */
    public EmptyRequiredHeaderException(String message) {
        super(message);
    }
}
