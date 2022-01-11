package uk.gov.hmcts.reform.pip.data.management.errorhandling.exceptions;

/**
 * Exception that captures when date from/ date to headers are not included when required.
 */
public class DateHeaderValidationException extends RuntimeException {

    private static final long serialVersionUID = 4330033210493138402L;

    /**
     * Constructor for the Exception.
     * @param message The message to return to the end user
     */
    public DateHeaderValidationException(String message) {
        super(message);
    }
}
