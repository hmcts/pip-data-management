package uk.gov.hmcts.reform.pip.data.management.errorhandling.exceptions;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CustomExceptionsTest {

    private static final String TEST_MESSAGE = "This is a test message";
    private static final String ASSERTION_MESSAGE = "The message should match the message passed in";

    @Test
    @DisplayName("Test that the creation of the custom exception, populates the relevant exception fields")
    void testCreationOfDataStorageNotFoundException() {

        DataStorageNotFoundException publicationNotFoundException
            = new DataStorageNotFoundException(TEST_MESSAGE);
        assertEquals(TEST_MESSAGE, publicationNotFoundException.getMessage(),
                     ASSERTION_MESSAGE);

    }

    @Test
    void testCreationOfReflectionException() {
        ReflectionException reflectionException = new ReflectionException(TEST_MESSAGE);
        assertEquals(TEST_MESSAGE, reflectionException.getMessage(), ASSERTION_MESSAGE);
    }

    @Test
    void testCreationOfCourtNotFoundException() {
        CourtNotFoundException courtNotFoundException = new CourtNotFoundException(TEST_MESSAGE);
        assertEquals(TEST_MESSAGE, courtNotFoundException.getMessage(), ASSERTION_MESSAGE);
    }

    @Test
    void testCreationOfHearingNotFoundException() {
        HearingNotFoundException hearingNotFoundException = new HearingNotFoundException(TEST_MESSAGE);
        assertEquals(TEST_MESSAGE, hearingNotFoundException.getMessage(), ASSERTION_MESSAGE);
    }

    @Test
    void testCreationOfDateHeaderValidationException() {
        DateHeaderValidationException dateHeaderValidationException = new DateHeaderValidationException(TEST_MESSAGE);
        assertEquals(TEST_MESSAGE, dateHeaderValidationException.getMessage(), ASSERTION_MESSAGE);
    }

    @Test
    void testCreationOfNotFoundException() {
        NotFoundException notFoundException = new NotFoundException(TEST_MESSAGE);
        assertEquals(TEST_MESSAGE, notFoundException.getMessage(), ASSERTION_MESSAGE);
    }

}
