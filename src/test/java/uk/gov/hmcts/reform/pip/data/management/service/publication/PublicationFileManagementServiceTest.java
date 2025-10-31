package uk.gov.hmcts.reform.pip.data.management.service.publication;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.pip.data.management.database.AzurePublicationBlobService;
import uk.gov.hmcts.reform.pip.data.management.errorhandling.exceptions.FileSizeLimitException;
import uk.gov.hmcts.reform.pip.data.management.errorhandling.exceptions.NotFoundException;
import uk.gov.hmcts.reform.pip.data.management.errorhandling.exceptions.UnauthorisedRequestException;
import uk.gov.hmcts.reform.pip.data.management.models.PublicationFileSizes;
import uk.gov.hmcts.reform.pip.data.management.models.PublicationFiles;
import uk.gov.hmcts.reform.pip.data.management.models.publication.Artefact;
import uk.gov.hmcts.reform.pip.data.management.service.AccountManagementService;
import uk.gov.hmcts.reform.pip.model.publication.Language;
import uk.gov.hmcts.reform.pip.model.publication.ListType;
import uk.gov.hmcts.reform.pip.model.publication.Sensitivity;

import java.util.Arrays;
import java.util.Base64;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.pip.model.publication.FileType.EXCEL;
import static uk.gov.hmcts.reform.pip.model.publication.FileType.PDF;

@ActiveProfiles(profiles = "test")
@ExtendWith(MockitoExtension.class)
@SuppressWarnings({"PMD.ExcessiveImports"})
class PublicationFileManagementServiceTest {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final Artefact ARTEFACT = new Artefact();

    private static final String NOT_FOUND_MESSAGE = "File not found";
    private static final String BYTES_NO_MATCH = "Bytes didn't match";
    private static final String EXCEPTION_NOT_MATCH = "Exception message should contain expected";
    private static final String FILE_EXISTS_FLAG_MESSAGE = "File exists flag does not match";
    private static final String FILE_SIZE_MESSAGE = "File size does not match";
    private static final byte[] TEST_BYTE = "test".getBytes();

    private static final UUID TEST_ARTEFACT_ID = UUID.randomUUID();
    private static final UUID TEST_USER_ID = UUID.randomUUID();

    private static final String WELSH_PDF_SUFFIX = "_cy";

    private static final String PAYLOAD = "Test payload";
    private static final byte[] EMPTY_BYTES = new byte[0];
    private static final byte[] BYTE_DATA = { 1 };

    @Mock
    private PublicationRetrievalService publicationRetrievalService;

    @Mock
    private AccountManagementService accountManagementService;

    @Mock
    private AzurePublicationBlobService azureBlobService;

    @Mock
    private PublicationFileGenerationService publicationFileGenerationService;

    @InjectMocks
    private PublicationFileManagementService publicationFileManagementService;

    @BeforeAll
    static void startup() {
        OBJECT_MAPPER.findAndRegisterModules();
    }

    @BeforeEach
    void setup() {
        ARTEFACT.setArtefactId(TEST_ARTEFACT_ID);
        ARTEFACT.setListType(ListType.SJP_PUBLIC_LIST);
        ARTEFACT.setPayloadSize(100F);

        lenient().when(publicationRetrievalService.payloadWithinExcelLimit(argThat(arg -> arg <= 2048)))
            .thenReturn(true);
        lenient().when(publicationRetrievalService.payloadWithinPdfLimit(argThat(arg -> arg <= 256)))
            .thenReturn(true);
    }

    @Test
    void testGenerateFilesWithPrimaryPdfOnly() {
        when(publicationFileGenerationService.generate(TEST_ARTEFACT_ID, PAYLOAD))
            .thenReturn(Optional.of(new PublicationFiles(BYTE_DATA, EMPTY_BYTES, EMPTY_BYTES)));

        publicationFileManagementService.generateFiles(TEST_ARTEFACT_ID, PAYLOAD);

        verify(azureBlobService).uploadFile(eq(TEST_ARTEFACT_ID + PDF.getExtension()), any());
        verify(azureBlobService, never())
            .uploadFile(eq(TEST_ARTEFACT_ID + WELSH_PDF_SUFFIX + PDF.getExtension()), any());
        verify(azureBlobService, never()).uploadFile(eq(TEST_ARTEFACT_ID + EXCEL.getExtension()), any());
    }

    @Test
    void testGenerateFilesWithPrimaryAndAdditionalPdfs() {
        when(publicationFileGenerationService.generate(TEST_ARTEFACT_ID, PAYLOAD))
            .thenReturn(Optional.of(new PublicationFiles(BYTE_DATA, BYTE_DATA, EMPTY_BYTES)));

        publicationFileManagementService.generateFiles(TEST_ARTEFACT_ID, PAYLOAD);

        verify(azureBlobService).uploadFile(eq(TEST_ARTEFACT_ID + PDF.getExtension()), any());
        verify(azureBlobService).uploadFile(eq(TEST_ARTEFACT_ID + WELSH_PDF_SUFFIX + PDF.getExtension()), any());
        verify(azureBlobService, never()).uploadFile(eq(TEST_ARTEFACT_ID + EXCEL.getExtension()), any());
    }

    @Test
    void testGenerateFilesWithPdfAndExcel() {
        when(publicationFileGenerationService.generate(TEST_ARTEFACT_ID, PAYLOAD))
            .thenReturn(Optional.of(new PublicationFiles(BYTE_DATA, EMPTY_BYTES, BYTE_DATA)));

        publicationFileManagementService.generateFiles(TEST_ARTEFACT_ID, PAYLOAD);

        verify(azureBlobService).uploadFile(eq(TEST_ARTEFACT_ID + PDF.getExtension()), any());
        verify(azureBlobService, never())
            .uploadFile(eq(TEST_ARTEFACT_ID + WELSH_PDF_SUFFIX + PDF.getExtension()), any());
        verify(azureBlobService).uploadFile(eq(TEST_ARTEFACT_ID + EXCEL.getExtension()), any());
    }

    @Test
    void testGenerateFilesWithExcelOnly() {
        when(publicationFileGenerationService.generate(TEST_ARTEFACT_ID, PAYLOAD))
            .thenReturn(Optional.of(new PublicationFiles(EMPTY_BYTES, EMPTY_BYTES, BYTE_DATA)));

        publicationFileManagementService.generateFiles(TEST_ARTEFACT_ID, PAYLOAD);

        verify(azureBlobService, never()).uploadFile(eq(TEST_ARTEFACT_ID + PDF.getExtension()), any());
        verify(azureBlobService, never())
            .uploadFile(eq(TEST_ARTEFACT_ID + WELSH_PDF_SUFFIX + PDF.getExtension()), any());
        verify(azureBlobService).uploadFile(eq(TEST_ARTEFACT_ID + EXCEL.getExtension()), any());
    }

    @Test
    void testGenerateFilesWhenFailed() {
        when(publicationFileGenerationService.generate(TEST_ARTEFACT_ID, PAYLOAD))
            .thenReturn(Optional.empty());

        publicationFileManagementService.generateFiles(TEST_ARTEFACT_ID, PAYLOAD);

        verify(azureBlobService, never()).uploadFile(eq(TEST_ARTEFACT_ID + PDF.getExtension()), any());
        verify(azureBlobService, never())
            .uploadFile(eq(TEST_ARTEFACT_ID + WELSH_PDF_SUFFIX + PDF.getExtension()), any());
        verify(azureBlobService, never()).uploadFile(eq(TEST_ARTEFACT_ID + EXCEL.getExtension()), any());
    }

    @Test
    void testGetStoredPdfPublicationSjp() {
        when(publicationRetrievalService.getMetadataByArtefactId(TEST_ARTEFACT_ID)).thenReturn(ARTEFACT);
        when(azureBlobService.getBlobFile(TEST_ARTEFACT_ID + PDF.getExtension())).thenReturn(TEST_BYTE);

        String response = publicationFileManagementService.getStoredPublication(
            TEST_ARTEFACT_ID, PDF, null, TEST_USER_ID, true, false
        );

        byte[] decodedBytes = Base64.getDecoder().decode(response);
        assertEquals(Arrays.toString(TEST_BYTE), Arrays.toString(decodedBytes), BYTES_NO_MATCH);
    }

    @Test
    void testGetStoredAdditionalPdfPublicationSjp() {
        when(publicationRetrievalService.getMetadataByArtefactId(TEST_ARTEFACT_ID)).thenReturn(ARTEFACT);
        doThrow(new NotFoundException(NOT_FOUND_MESSAGE)).when(azureBlobService)
            .getBlobFile(TEST_ARTEFACT_ID + WELSH_PDF_SUFFIX + PDF.getExtension());

        NotFoundException ex = assertThrows(NotFoundException.class, () ->
            publicationFileManagementService.getStoredPublication(
                TEST_ARTEFACT_ID, PDF, null, TEST_USER_ID, true, true
            ));

        assertTrue(ex.getMessage().contains(NOT_FOUND_MESSAGE), EXCEPTION_NOT_MATCH);
    }

    @ParameterizedTest
    @MethodSource("sjpParameters")
    void testGetStoredExcelPublicationSjp(Artefact artefact) {
        when(publicationRetrievalService.getMetadataByArtefactId(TEST_ARTEFACT_ID)).thenReturn(artefact);
        when(azureBlobService.getBlobFile(TEST_ARTEFACT_ID + EXCEL.getExtension())).thenReturn(TEST_BYTE);

        String response = publicationFileManagementService.getStoredPublication(
            TEST_ARTEFACT_ID, EXCEL, null, TEST_USER_ID, true, false
        );

        byte[] decodedBytes = Base64.getDecoder().decode(response);
        assertEquals(Arrays.toString(TEST_BYTE), Arrays.toString(decodedBytes), BYTES_NO_MATCH);
    }

    @Test
    void testGetStoredPdfPublicationNonSjp() {
        ARTEFACT.setListType(ListType.CIVIL_DAILY_CAUSE_LIST);
        when(publicationRetrievalService.getMetadataByArtefactId(TEST_ARTEFACT_ID)).thenReturn(ARTEFACT);
        when(azureBlobService.getBlobFile(TEST_ARTEFACT_ID + PDF.getExtension())).thenReturn(TEST_BYTE);

        String response = publicationFileManagementService.getStoredPublication(
            TEST_ARTEFACT_ID, PDF, null, TEST_USER_ID, true, false
        );

        byte[] decodedBytes = Base64.getDecoder().decode(response);
        assertEquals(Arrays.toString(TEST_BYTE), Arrays.toString(decodedBytes), BYTES_NO_MATCH);
    }

    @Test
    void testGetStoredAdditionalPdfPublicationNonSjp() {
        ARTEFACT.setListType(ListType.CIVIL_DAILY_CAUSE_LIST);
        when(publicationRetrievalService.getMetadataByArtefactId(TEST_ARTEFACT_ID)).thenReturn(ARTEFACT);
        when(azureBlobService.getBlobFile(TEST_ARTEFACT_ID + WELSH_PDF_SUFFIX + PDF.getExtension()))
            .thenReturn(TEST_BYTE);

        String response = publicationFileManagementService.getStoredPublication(
            TEST_ARTEFACT_ID, PDF, null, TEST_USER_ID, true, true
        );

        byte[] decodedBytes = Base64.getDecoder().decode(response);
        assertEquals(Arrays.toString(TEST_BYTE), Arrays.toString(decodedBytes), BYTES_NO_MATCH);
    }

    @Test
    void testGetStoredExcelPublicationNonSjp() {
        ARTEFACT.setListType(ListType.CIVIL_DAILY_CAUSE_LIST);
        when(publicationRetrievalService.getMetadataByArtefactId(TEST_ARTEFACT_ID)).thenReturn(ARTEFACT);
        doThrow(new NotFoundException(NOT_FOUND_MESSAGE)).when(azureBlobService)
            .getBlobFile(TEST_ARTEFACT_ID + EXCEL.getExtension());

        NotFoundException ex = assertThrows(NotFoundException.class, () ->
            publicationFileManagementService.getStoredPublication(
                TEST_ARTEFACT_ID, EXCEL, null, TEST_USER_ID, true, false
            ));

        assertTrue(ex.getMessage().contains(NOT_FOUND_MESSAGE), EXCEPTION_NOT_MATCH);
    }

    @Test
    void testGetStoredPublicationWithinFileSizeLimit() {
        when(publicationRetrievalService.getMetadataByArtefactId(any())).thenReturn(ARTEFACT);
        when(azureBlobService.getBlobFile(any())).thenReturn(TEST_BYTE);

        String response = publicationFileManagementService.getStoredPublication(
            TEST_ARTEFACT_ID, PDF, 20, TEST_USER_ID, true, false
        );

        byte[] decodedBytes = Base64.getDecoder().decode(response);
        assertEquals(Arrays.toString(TEST_BYTE), Arrays.toString(decodedBytes), BYTES_NO_MATCH);
    }

    @Test
    void testGetStoredPublicationOverFileSizeLimit() {
        when(publicationRetrievalService.getMetadataByArtefactId(any())).thenReturn(ARTEFACT);
        when(azureBlobService.getBlobFile(any())).thenReturn(TEST_BYTE);

        FileSizeLimitException ex = assertThrows(FileSizeLimitException.class, () ->
            publicationFileManagementService.getStoredPublication(
                TEST_ARTEFACT_ID, PDF, 2, TEST_USER_ID, true, false
            ));

        assertTrue(ex.getMessage().contains("File with type PDF for artefact with id " + TEST_ARTEFACT_ID
                                                + " has size over the limit of 2 bytes"), EXCEPTION_NOT_MATCH);
    }

    @Test
    void testGetStoredPublicationAuthorisedPublic() {
        ARTEFACT.setListType(ListType.CIVIL_DAILY_CAUSE_LIST);
        ARTEFACT.setSensitivity(Sensitivity.PUBLIC);
        when(publicationRetrievalService.getMetadataByArtefactId(any())).thenReturn(ARTEFACT);
        when(azureBlobService.getBlobFile(any())).thenReturn(TEST_BYTE);

        String response = publicationFileManagementService.getStoredPublication(
            TEST_ARTEFACT_ID, PDF, null, TEST_USER_ID, false, false
        );

        byte[] decodedBytes = Base64.getDecoder().decode(response);
        assertEquals(Arrays.toString(TEST_BYTE), Arrays.toString(decodedBytes), BYTES_NO_MATCH);
    }

    @Test
    void testGetStoredPublicationAuthorisedUserIdNull() {
        ARTEFACT.setListType(ListType.CIVIL_DAILY_CAUSE_LIST);
        ARTEFACT.setSensitivity(Sensitivity.CLASSIFIED);
        when(publicationRetrievalService.getMetadataByArtefactId(any())).thenReturn(ARTEFACT);

        UnauthorisedRequestException ex = assertThrows(UnauthorisedRequestException.class, () ->
            publicationFileManagementService.getStoredPublication(
                TEST_ARTEFACT_ID, PDF, null, null, false, false
            ));

        assertTrue(ex.getMessage().contains("User with id null is not authorised to access artefact with id"),
                   EXCEPTION_NOT_MATCH);
    }

    @Test
    void testGetStoredPublicationAuthorisedFalse() {
        ARTEFACT.setListType(ListType.CIVIL_DAILY_CAUSE_LIST);
        ARTEFACT.setSensitivity(Sensitivity.CLASSIFIED);
        when(publicationRetrievalService.getMetadataByArtefactId(any())).thenReturn(ARTEFACT);
        when(accountManagementService.getIsAuthorised(any(), any(), any())).thenReturn(false);

        UnauthorisedRequestException ex = assertThrows(UnauthorisedRequestException.class, () ->
            publicationFileManagementService.getStoredPublication(
                TEST_ARTEFACT_ID, PDF, null, TEST_USER_ID, false, false
            ), "Expected exception to be thrown");

        assertTrue(ex.getMessage().contains("is not authorised to access artefact with id"),
                   EXCEPTION_NOT_MATCH);
    }

    @Test
    void testDeleteFilesV2SjpEnglish() {
        publicationFileManagementService.deleteFiles(TEST_ARTEFACT_ID, ListType.SJP_PUBLIC_LIST, Language.ENGLISH);

        verify(azureBlobService).deleteBlobFile(TEST_ARTEFACT_ID + PDF.getExtension());
        verify(azureBlobService, never()).deleteBlobFile(TEST_ARTEFACT_ID + WELSH_PDF_SUFFIX + PDF.getExtension());
        verify(azureBlobService).deleteBlobFile(TEST_ARTEFACT_ID + EXCEL.getExtension());
    }

    @Test
    void testDeleteFilesV2SjpWelsh() {
        publicationFileManagementService.deleteFiles(TEST_ARTEFACT_ID, ListType.SJP_PUBLIC_LIST, Language.WELSH);

        verify(azureBlobService).deleteBlobFile(TEST_ARTEFACT_ID + PDF.getExtension());
        verify(azureBlobService, never()).deleteBlobFile(TEST_ARTEFACT_ID + WELSH_PDF_SUFFIX + PDF.getExtension());
        verify(azureBlobService).deleteBlobFile(TEST_ARTEFACT_ID + EXCEL.getExtension());
    }

    @Test
    void testDeleteFilesV2NonSjpEnglish() {
        publicationFileManagementService.deleteFiles(TEST_ARTEFACT_ID, ListType.CIVIL_DAILY_CAUSE_LIST,
                                                     Language.ENGLISH);

        verify(azureBlobService).deleteBlobFile(TEST_ARTEFACT_ID + PDF.getExtension());
        verify(azureBlobService, never()).deleteBlobFile(TEST_ARTEFACT_ID + WELSH_PDF_SUFFIX + PDF.getExtension());
        verify(azureBlobService, never()).deleteBlobFile(TEST_ARTEFACT_ID + EXCEL.getExtension());
    }

    @Test
    void testDeleteFilesV2NonSjpWelsh() {
        publicationFileManagementService.deleteFiles(TEST_ARTEFACT_ID, ListType.CIVIL_DAILY_CAUSE_LIST, Language.WELSH);

        verify(azureBlobService).deleteBlobFile(TEST_ARTEFACT_ID + PDF.getExtension());
        verify(azureBlobService).deleteBlobFile(TEST_ARTEFACT_ID + WELSH_PDF_SUFFIX + PDF.getExtension());
        verify(azureBlobService, never()).deleteBlobFile(TEST_ARTEFACT_ID + EXCEL.getExtension());
    }

    @Test
    void testFileExistsReturnTrueIfAllFilesExist() {
        when(azureBlobService.blobFileExists(TEST_ARTEFACT_ID + PDF.getExtension()))
            .thenReturn(true);

        assertTrue(publicationFileManagementService.fileExists(TEST_ARTEFACT_ID), FILE_EXISTS_FLAG_MESSAGE);
    }

    @Test
    void testFileExistsReturnTrueIfOnlyOneFileExists() {
        when(azureBlobService.blobFileExists(TEST_ARTEFACT_ID + PDF.getExtension()))
            .thenReturn(false);
        when(azureBlobService.blobFileExists(TEST_ARTEFACT_ID  + WELSH_PDF_SUFFIX + PDF.getExtension()))
            .thenReturn(false);
        when(azureBlobService.blobFileExists(TEST_ARTEFACT_ID + EXCEL.getExtension()))
            .thenReturn(true);

        assertTrue(publicationFileManagementService.fileExists(TEST_ARTEFACT_ID), FILE_EXISTS_FLAG_MESSAGE);
    }

    @Test
    void testFileExistsReturnFalseIfNoFileExist() {
        when(azureBlobService.blobFileExists(TEST_ARTEFACT_ID + PDF.getExtension()))
            .thenReturn(false);
        when(azureBlobService.blobFileExists(TEST_ARTEFACT_ID  + WELSH_PDF_SUFFIX + PDF.getExtension()))
            .thenReturn(false);
        when(azureBlobService.blobFileExists(TEST_ARTEFACT_ID + EXCEL.getExtension()))
            .thenReturn(false);

        assertFalse(publicationFileManagementService.fileExists(TEST_ARTEFACT_ID), FILE_EXISTS_FLAG_MESSAGE);
    }

    @Test
    void testGetFileSizesReturnFileSizeIfPresent() {
        when(azureBlobService.getBlobSize(TEST_ARTEFACT_ID + PDF.getExtension()))
            .thenReturn(1234L);
        when(azureBlobService.getBlobSize(TEST_ARTEFACT_ID  + WELSH_PDF_SUFFIX + PDF.getExtension()))
            .thenReturn(null);
        when(azureBlobService.getBlobSize(TEST_ARTEFACT_ID + EXCEL.getExtension()))
            .thenReturn(123L);

        PublicationFileSizes fileSizes = publicationFileManagementService.getFileSizes(TEST_ARTEFACT_ID);

        assertNull(fileSizes.getAdditionalPdf(), FILE_SIZE_MESSAGE);
        assertEquals(1234L, fileSizes.getPrimaryPdf(), FILE_SIZE_MESSAGE);
        assertEquals(123L, fileSizes.getExcel(), FILE_SIZE_MESSAGE);
    }

    private static Stream<Arguments> sjpParameters() throws JsonProcessingException {
        Artefact sjpPublicArtefact = ARTEFACT;
        sjpPublicArtefact.setListType(ListType.SJP_PUBLIC_LIST);

        Artefact sjpPressArtefact = OBJECT_MAPPER.readValue(
            OBJECT_MAPPER.writeValueAsString(ARTEFACT), Artefact.class
        );
        sjpPressArtefact.setListType(ListType.SJP_PRESS_LIST);

        return Stream.of(
            Arguments.of(sjpPublicArtefact),
            Arguments.of(sjpPressArtefact)
        );
    }
}
