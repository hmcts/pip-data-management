package uk.gov.hmcts.reform.pip.data.management.errorhandling.exceptions;

public class CreateArtefactConflictException extends RuntimeException {
    private static final long serialVersionUID = 3662821754485634512L;

    /**
     * Constructor for the Exception.
     * @param message The message to return to the end user
     */
    public CreateArtefactConflictException(String message) {
        super(message);
    }
}
