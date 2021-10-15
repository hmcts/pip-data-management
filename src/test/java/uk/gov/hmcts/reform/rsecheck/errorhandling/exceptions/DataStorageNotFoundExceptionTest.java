package uk.gov.hmcts.reform.rsecheck.errorhandling.exceptions;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.demo.errorhandling.DataStorageNotFoundException;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DataStorageNotFoundExceptionTest {

    @Test
    @DisplayName("Test that the creation of the custom exception, populates the relevant exception fields")
    void testCreationOfDataStorageNotFoundException() {

        DataStorageNotFoundException publicationNotFoundException
            = new DataStorageNotFoundException("This is a test message");
        assertEquals("This is a test message", publicationNotFoundException.getMessage(),
                     "The message should match the message passed in");

    }

}
