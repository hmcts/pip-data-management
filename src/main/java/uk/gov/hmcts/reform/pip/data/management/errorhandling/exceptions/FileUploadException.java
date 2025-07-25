package uk.gov.hmcts.reform.pip.data.management.errorhandling.exceptions;

public class FileUploadException extends RuntimeException {
    private static final long serialVersionUID = -290658816976936274L;

    /**
     * Constructor for the Exception.
     *
     * @param message The message to return to the end user
     */
    public FileUploadException(String message) {
        super(message);
    }
}
