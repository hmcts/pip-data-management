package uk.gov.hmcts.reform.pip.data.management.errorhandling.exceptions;

import java.io.Serial;

/**
 * This exception class handles fields that contain unwanted elements such as html tags.
 */
public class ContainsForbiddenValuesException extends RuntimeException {
    @Serial
    private static final long serialVersionUID = -8267692849518367195L;

    /**
     * Constructor for the Exception.
     * @param message The message to return to the end user
     */
    public ContainsForbiddenValuesException(String message) {
        super(message);
    }
}
