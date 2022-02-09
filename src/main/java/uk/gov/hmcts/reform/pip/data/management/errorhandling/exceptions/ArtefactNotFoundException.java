package uk.gov.hmcts.reform.pip.data.management.errorhandling.exceptions;

public class ArtefactNotFoundException extends NotFoundException {

    private static final long serialVersionUID = 6701858414781216168L;

    /**
     * Constructor for the Exception.
     *
     * @param message The message to return to the end user
     */
    public ArtefactNotFoundException(String message) {
        super(message);
    }
}
