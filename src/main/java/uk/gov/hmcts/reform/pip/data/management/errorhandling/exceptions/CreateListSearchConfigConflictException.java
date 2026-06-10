package uk.gov.hmcts.reform.pip.data.management.errorhandling.exceptions;

public class CreateListSearchConfigConflictException extends DataConflictException {
    private static final long serialVersionUID = -4007619532877988562L;

    public CreateListSearchConfigConflictException(String message) {
        super(message);
    }
}
