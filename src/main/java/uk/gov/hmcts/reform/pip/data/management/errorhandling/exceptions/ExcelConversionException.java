package uk.gov.hmcts.reform.pip.data.management.errorhandling.exceptions;

public class ExcelConversionException extends RuntimeException {
    private static final long serialVersionUID = -1243851818792839927L;

    /**
     * Constructor for the Exception.
     *
     * @param message The message to return to the end user
     */
    public ExcelConversionException(String message) {
        super(message);
    }
}
