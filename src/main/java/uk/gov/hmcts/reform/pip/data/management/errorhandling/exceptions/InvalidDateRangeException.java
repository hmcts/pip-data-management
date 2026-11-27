package uk.gov.hmcts.reform.pip.data.management.errorhandling.exceptions;

import lombok.experimental.StandardException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
@StandardException
public class InvalidDateRangeException extends RuntimeException {
}
