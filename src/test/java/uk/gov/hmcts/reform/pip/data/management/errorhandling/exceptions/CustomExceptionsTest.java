package uk.gov.hmcts.reform.pip.data.management.errorhandling.exceptions;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CustomExceptionsTest {

    private static final String TEST_MESSAGE = "This is a test message";
    private static final String ASSERTION_MESSAGE = "The message should match the message passed in";

    @Test
    void testCreationOfReflectionException() {
        ReflectionException reflectionException = new ReflectionException(TEST_MESSAGE);
        assertEquals(TEST_MESSAGE, reflectionException.getMessage(), ASSERTION_MESSAGE);
    }

    @Test
    void testCreationOfCourtNotFoundException() {
        LocationNotFoundException locationNotFoundException = new LocationNotFoundException(TEST_MESSAGE);
        assertEquals(TEST_MESSAGE, locationNotFoundException.getMessage(), ASSERTION_MESSAGE);
    }

    @Test
    void testCreationOfHeaderValidationException() {
        HeaderValidationException headerValidationException = new HeaderValidationException(TEST_MESSAGE);
        assertEquals(TEST_MESSAGE, headerValidationException.getMessage(), ASSERTION_MESSAGE);
    }

    @Test
    void testCreationOfNotFoundException() {
        NotFoundException notFoundException = new NotFoundException(TEST_MESSAGE);
        assertEquals(TEST_MESSAGE, notFoundException.getMessage(), ASSERTION_MESSAGE);
    }

    @Test
    void testCreationOfDateValidationException() {
        DateValidationException dateValidationException = new DateValidationException(TEST_MESSAGE);
        assertEquals(TEST_MESSAGE, dateValidationException.getMessage(), ASSERTION_MESSAGE);
    }

    @Test
    void testCreationOfEmptyRequiredHeaderException() {
        EmptyRequiredHeaderException emptyRequiredHeaderException = new EmptyRequiredHeaderException(TEST_MESSAGE);
        assertEquals(TEST_MESSAGE,emptyRequiredHeaderException.getMessage(), ASSERTION_MESSAGE);
    }

    @Test
    void testCreationOfFlatFileIoException() {
        FlatFileException flatFileException = new FlatFileException(TEST_MESSAGE);
        assertEquals(TEST_MESSAGE,
                     flatFileException.getMessage(), ASSERTION_MESSAGE);
    }

    @Test
    void testCreationOfArtefactNotFoundException() {
        ArtefactNotFoundException artefactNotFoundException = new ArtefactNotFoundException(TEST_MESSAGE);
        assertEquals(TEST_MESSAGE, artefactNotFoundException.getMessage(), ASSERTION_MESSAGE);
    }

    @Test
    void testCreateArtefactConflictException() {
        CreateArtefactConflictException conflictException = new CreateArtefactConflictException(TEST_MESSAGE);
        assertEquals(TEST_MESSAGE, conflictException.getMessage(), ASSERTION_MESSAGE);
    }

    @Test
    void testCreateLocationConflictException() {
        CreateLocationConflictException conflictException = new CreateLocationConflictException(TEST_MESSAGE);
        assertEquals(TEST_MESSAGE, conflictException.getMessage(), ASSERTION_MESSAGE);
    }

    @Test
    void testPublicationFilesNotFoundException() {
        PublicationFileNotFoundException notFoundException = new PublicationFileNotFoundException(TEST_MESSAGE);
        assertEquals(TEST_MESSAGE, notFoundException.getMessage(), ASSERTION_MESSAGE);
    }

    @Test
    void testFileSizeLimitExceptionException() {
        FileSizeLimitException fileSizeLimitException = new FileSizeLimitException(TEST_MESSAGE);
        assertEquals(TEST_MESSAGE, fileSizeLimitException.getMessage(), ASSERTION_MESSAGE);
    }

    @Test
    void testProcessingException() {
        ProcessingException processingException = new ProcessingException(TEST_MESSAGE);
        assertEquals(TEST_MESSAGE, processingException.getMessage(), ASSERTION_MESSAGE);
    }

    @Test
    void testExcelConversionException() {
        ExcelConversionException excelConversionException = new ExcelConversionException(TEST_MESSAGE);
        assertEquals(TEST_MESSAGE, excelConversionException.getMessage(), ASSERTION_MESSAGE);
    }
}
