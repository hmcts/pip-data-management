package uk.gov.hmcts.reform.pip.data.management.errorhandling;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UiExceptionResponse extends ExceptionResponse {
    private boolean uiError;
}
