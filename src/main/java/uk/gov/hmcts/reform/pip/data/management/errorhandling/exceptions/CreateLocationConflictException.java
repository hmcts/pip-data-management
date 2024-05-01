package uk.gov.hmcts.reform.pip.data.management.errorhandling.exceptions;

/**
 * This exception will be thrown when creating location with a name/welsh name already exists in the database for
 * another location ID.
 */
public class CreateLocationConflictException extends RuntimeException {
    private static final long serialVersionUID = -2531452230863500559L;

    /**
     * Constructor for the Exception.
     * @param message The message to return to the end user
     */
    public CreateLocationConflictException(String message) {
        super(message);
    }
}
