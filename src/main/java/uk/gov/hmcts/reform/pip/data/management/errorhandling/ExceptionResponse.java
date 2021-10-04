package uk.gov.hmcts.reform.pip.data.management.errorhandling;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ExceptionResponse {

    /*
    The error message to return
    */
    private  String message;

    /*
    The timestamp when error occurs
    */
    private LocalDateTime timestamp;
}
