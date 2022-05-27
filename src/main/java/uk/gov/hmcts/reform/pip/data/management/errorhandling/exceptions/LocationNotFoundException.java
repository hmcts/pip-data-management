package uk.gov.hmcts.reform.pip.data.management.errorhandling.exceptions;

/**
 * Exception that captures when a court is not found.
 */
public class LocationNotFoundException extends NotFoundException {

    private static final long serialVersionUID = 2919550131718075133L;

    /**
     * Constructor for the Exception.
     * @param message The message to return to the end user
     */
    public LocationNotFoundException(String message) {
        super(message);
    }
}
