package uk.gov.hmcts.reform.pip.data.management.errorhandling;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ExceptionResponseTest {
    private static final String TEST_MESSAGE = "Test Message";

    @Test
    @DisplayName("Test that the creation of an ExceptionResponse, populates the expected fields")
    void testCreationOfExceptionResponse() {
        ExceptionResponse exceptionResponse = new ExceptionResponse();
        LocalDateTime localDateTime = LocalDateTime.now();

        exceptionResponse.setTimestamp(localDateTime);
        exceptionResponse.setMessage(TEST_MESSAGE);
        assertEquals(localDateTime, exceptionResponse.getTimestamp(),
                     "The timestamp should match the timestamp created");
        assertEquals(TEST_MESSAGE, exceptionResponse.getMessage(),
                     "The message should match the message passed in");
    }

    @Test
    void testCreationOfUiExceptionResponse() {
        UiExceptionResponse exceptionResponse = new UiExceptionResponse();
        LocalDateTime localDateTime = LocalDateTime.now();

        exceptionResponse.setTimestamp(localDateTime);
        exceptionResponse.setMessage(TEST_MESSAGE);
        exceptionResponse.setUiError(true);
        assertEquals(localDateTime, exceptionResponse.getTimestamp(),
                     "The timestamp should match the timestamp created");
        assertEquals(TEST_MESSAGE, exceptionResponse.getMessage(),
                     "The message should match the message passed in");
        assertTrue(exceptionResponse.isUiError(), "UI error should be true");
    }
}
