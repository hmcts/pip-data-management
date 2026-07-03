package uk.gov.hmcts.reform.pip.data.management.errorhandling.exceptions;

public class DataConflictException extends RuntimeException {
    private static final long serialVersionUID = 2913966089987676872L;

    public DataConflictException(String message) {
        super(message);
    }
}
