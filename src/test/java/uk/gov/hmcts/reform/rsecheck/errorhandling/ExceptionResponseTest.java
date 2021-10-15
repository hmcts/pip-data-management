package uk.gov.hmcts.reform.rsecheck.errorhandling;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.pip.data.management.errorhandling.ExceptionResponse;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ExceptionResponseTest {

    @Test
    @DisplayName("Test that the creation of an ExceptionResponse, populates the expected fields")
    public void testCreationOfExceptionResponse() {

        ExceptionResponse exceptionResponse = new ExceptionResponse();

        LocalDateTime localDateTime = LocalDateTime.now();

        exceptionResponse.setTimestamp(localDateTime);
        exceptionResponse.setMessage("This is a new message");
        assertEquals(localDateTime, exceptionResponse.getTimestamp(),
                     "The timestamp should match the timestamp created");
        assertEquals("This is a new message", exceptionResponse.getMessage(),
                     "The message should match the message passed in");
    }
}
