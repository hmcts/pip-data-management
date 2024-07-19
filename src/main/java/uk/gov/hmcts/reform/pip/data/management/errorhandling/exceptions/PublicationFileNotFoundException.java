package uk.gov.hmcts.reform.pip.data.management.errorhandling.exceptions;

public class PublicationFileNotFoundException extends NotFoundException {
    private static final long serialVersionUID = 4330033210493138404L;

    /**
     * Constructor for the Exception.
     *
     * @param message The message to return to the end user
     */
    public PublicationFileNotFoundException(String message) {
        super(message);
    }
}
