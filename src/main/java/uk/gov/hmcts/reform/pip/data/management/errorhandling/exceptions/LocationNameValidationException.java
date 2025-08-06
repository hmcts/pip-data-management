package uk.gov.hmcts.reform.pip.data.management.errorhandling.exceptions;

public class LocationNameValidationException extends RuntimeException {
    private static final long serialVersionUID = -9080898647446408327L;

    public LocationNameValidationException(String message) {
        super(message);
    }
}
