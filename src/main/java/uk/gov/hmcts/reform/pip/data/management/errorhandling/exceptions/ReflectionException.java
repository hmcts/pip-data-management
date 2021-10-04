package uk.gov.hmcts.reform.pip.data.management.errorhandling.exceptions;

/**
 * Exception that captures the message when a reflection fails.
 */
public class ReflectionException extends RuntimeException {

    private static final long serialVersionUID = 3125481215397065095L;

    /**
     * Constructor for the Exception.
     * @param message The message to return to the end user
     */
    public ReflectionException(String message) {
        super(message);
    }
}
