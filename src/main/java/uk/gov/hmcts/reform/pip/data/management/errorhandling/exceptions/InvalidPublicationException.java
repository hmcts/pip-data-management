package uk.gov.hmcts.reform.pip.data.management.errorhandling.exceptions;

/**
 * Class which represents validation errors on the publication.
 */
public class InvalidPublicationException extends RuntimeException {

    private static final long serialVersionUID = 9124234276234121259L;

    public InvalidPublicationException(String message) {
        super(message);
    }
}
