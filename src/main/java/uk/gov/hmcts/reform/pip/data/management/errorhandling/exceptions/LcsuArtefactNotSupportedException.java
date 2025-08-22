package uk.gov.hmcts.reform.pip.data.management.errorhandling.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class LcsuArtefactNotSupportedException extends RuntimeException {

    private static final long serialVersionUID = 2919550131718075133L;

    public LcsuArtefactNotSupportedException(String message) {
        super(message);
    }
}
