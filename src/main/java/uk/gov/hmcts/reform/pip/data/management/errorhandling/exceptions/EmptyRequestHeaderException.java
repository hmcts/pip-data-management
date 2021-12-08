package uk.gov.hmcts.reform.pip.data.management.errorhandling.exceptions;

/**
 * Custom exception which handles empty request headers.
 */
public class EmptyRequestHeaderException extends RuntimeException {

    private static final long serialVersionUID = 6994056611414497079L;

    /**
     * Constructor for the EmptyRequestHeaderException.
     * @param fieldName The field name of the header that was missing.
     */
    public EmptyRequestHeaderException(String fieldName) {
        super(String.format("%s is mandatory however an empty value is provided", fieldName));
    }
}
