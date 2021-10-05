package uk.gov.hmcts.reform.pip.data.management.errorhandling.exceptions;

/**
 * Exception that captures when a live case is not found.
 */
public class LiveCaseStatusException extends NotFoundException {

    private static final long serialVersionUID = -2419760863911247578L;

    /**
     * Constructor for the Exception.
     * @param message The message to return to the end user
     */
    public LiveCaseStatusException(String message) {
        super(message);
    }
}
