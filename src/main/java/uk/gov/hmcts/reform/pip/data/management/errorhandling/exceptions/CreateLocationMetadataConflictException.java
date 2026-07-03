package uk.gov.hmcts.reform.pip.data.management.errorhandling.exceptions;

public class CreateLocationMetadataConflictException extends DataConflictException {
    private static final long serialVersionUID = 3662821754485634512L;

    /**
     * Constructor for the Exception.
     * @param message The message to return to the end user
     */
    public CreateLocationMetadataConflictException(String message) {
        super(message);
    }
}
