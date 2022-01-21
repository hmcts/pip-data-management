package uk.gov.hmcts.reform.pip.data.management.errorhandling.exceptions;

/**
 * Exception for handling files that could retrieve input streams.
 */
public class FlatFileIoException extends RuntimeException {

    private static final long serialVersionUID = 8574604777519490260L;
    private static final String MESSAGE =
        "Could not parse provided file, please check supported file types and try again";

    /**
     * Constructor for the exception.
     */
    public FlatFileIoException() {
        super(MESSAGE);
    }
}
