package uk.gov.hmcts.reform.pip.data.management.errorhandling.exceptions;

public class LocationNameValidationException extends RuntimeException {
    private static final long serialVersionUID = -3429120152730345448L;

    public LocationNameValidationException(String message) {
        super(message);
    }
}
