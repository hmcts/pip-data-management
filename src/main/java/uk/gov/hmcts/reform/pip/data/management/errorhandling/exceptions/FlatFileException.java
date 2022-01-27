package uk.gov.hmcts.reform.pip.data.management.errorhandling.exceptions;

/**
 * Exception for handling files that could retrieve input streams.
 */
public class FlatFileException extends RuntimeException {

    private static final long serialVersionUID = 8574604777519490260L;

    /**
     * Constructor for the exception.
     */
    public FlatFileException(String message) {
        super(message);
    }
}
