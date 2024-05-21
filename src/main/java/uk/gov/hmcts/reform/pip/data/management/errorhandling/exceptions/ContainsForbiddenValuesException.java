package uk.gov.hmcts.reform.pip.data.management.errorhandling.exceptions;

/**
 * This exception class handles fields that contain unwanted elements such as html tags.
 */
public class ContainsForbiddenValuesException extends RuntimeException {

    private static final long serialVersionUID = 4824563905241589732L;

    /**
     * Constructor for the Exception.
     * @param message The message to return to the end user
     */
    public ContainsForbiddenValuesException(String message) {
        super(message);
    }
}
