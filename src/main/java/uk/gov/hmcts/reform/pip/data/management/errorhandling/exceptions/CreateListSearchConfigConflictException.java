package uk.gov.hmcts.reform.pip.data.management.errorhandling.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.CONFLICT, reason = "List search config for list type already exists")
public class CreateListSearchConfigConflictException extends RuntimeException {
    private static final long serialVersionUID = -4007619532877988562L;

    public CreateListSearchConfigConflictException(String message) {
        super(message);
    }
}
