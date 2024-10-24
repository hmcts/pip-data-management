package uk.gov.hmcts.reform.pip.data.management.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.pip.data.management.Application;
import uk.gov.hmcts.reform.pip.data.management.config.AzureBlobConfigurationTestConfiguration;
import uk.gov.hmcts.reform.pip.data.management.database.AzurePublicationBlobService;
import uk.gov.hmcts.reform.pip.data.management.errorhandling.exceptions.FileSizeLimitException;
import uk.gov.hmcts.reform.pip.data.management.errorhandling.exceptions.NotFoundException;
import uk.gov.hmcts.reform.pip.data.management.errorhandling.exceptions.UnauthorisedRequestException;
import uk.gov.hmcts.reform.pip.data.management.models.PublicationFileSizes;
import uk.gov.hmcts.reform.pip.data.management.models.location.Location;
import uk.gov.hmcts.reform.pip.data.management.models.publication.Artefact;
import uk.gov.hmcts.reform.pip.data.management.service.publication.ArtefactService;
import uk.gov.hmcts.reform.pip.model.publication.Language;
import uk.gov.hmcts.reform.pip.model.publication.ListType;
import uk.gov.hmcts.reform.pip.model.publication.Sensitivity;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collections;
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
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.pip.model.publication.FileType.EXCEL;
import static uk.gov.hmcts.reform.pip.model.publication.FileType.PDF;

@SpringBootTest(classes = {Application.class, AzureBlobConfigurationTestConfiguration.class})
@ActiveProfiles(profiles = "test")
@SuppressWarnings({"PMD.TooManyMethods", "PMD.ExcessiveImports"})
class PublicationManagementServiceTest {
    @MockBean
    private ArtefactService artefactService;

    @MockBean
    private LocationService locationService;

    @MockBean
    private AccountManagementService accountManagementService;

    @MockBean
    private AzurePublicationBlobService azureBlobService;

    @Autowired
    private PublicationManagementService publicationManagementService;

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final Artefact ARTEFACT = new Artefact();
    private static final Artefact WELSH_ARTEFACT = new Artefact();
    private static final Location LOCATION = new Location();

    private static final String RESPONSE_MESSAGE = "Response didn't contain expected text";
    private static final String NOT_FOUND_MESSAGE = "File not found";
    private static final String BYTES_NO_MATCH = "Bytes didn't match";
    private static final String EXCEPTION_NOT_MATCH = "Exception message should contain expected";
    private static final String FILE_EXISTS_FLAG_MESSAGE = "File exists flag does not match";
    private static final String FILE_SIZE_MESSAGE = "File size does not match";
    private static final String TEST = "test";
    private static final byte[] TEST_BYTE = TEST.getBytes();

    private static final UUID TEST_ARTEFACT_ID = UUID.randomUUID();
    private static final String TEST_USER_ID = UUID.randomUUID().toString();

    private static final String UPLOADED = "uploaded";
    private static final String WELSH_PDF_SUFFIX = "_cy";

    private static String sjpPublicListInput;
    private static String civilDailyListInput;

    private static String getInput(String resourcePath) throws IOException {
        try (InputStream inputStream = PublicationManagementServiceTest.class.getResourceAsStream(resourcePath)) {
            return IOUtils.toString(inputStream, Charset.defaultCharset());
        }
    }

    @BeforeAll
    static void startup() throws IOException {
        OBJECT_MAPPER.findAndRegisterModules();
        sjpPublicListInput = getInput("/mocks/sjpPublicList.json");
        civilDailyListInput = getInput("/mocks/civilDailyCauseList.json");
    }

    @BeforeEach
    void setup() {
        ARTEFACT.setArtefactId(TEST_ARTEFACT_ID);
        ARTEFACT.setContentDate(LocalDateTime.now());
        ARTEFACT.setLocationId("1");
        ARTEFACT.setProvenance("france");
        ARTEFACT.setLanguage(Language.ENGLISH);
        ARTEFACT.setListType(ListType.SJP_PUBLIC_LIST);
        ARTEFACT.setPayloadSize(100F);

        WELSH_ARTEFACT.setArtefactId(TEST_ARTEFACT_ID);
        WELSH_ARTEFACT.setContentDate(LocalDateTime.now());
        WELSH_ARTEFACT.setLocationId("1");
        WELSH_ARTEFACT.setProvenance("france");
        WELSH_ARTEFACT.setLanguage(Language.WELSH);
        WELSH_ARTEFACT.setListType(ListType.SJP_PUBLIC_LIST);
        WELSH_ARTEFACT.setPayloadSize(100F);

        LOCATION.setLocationId(1);
        LOCATION.setName("Test");
        LOCATION.setWelshName("Test");
        LOCATION.setRegion(Collections.singletonList("Region"));
        LOCATION.setWelshRegion(Collections.singletonList("Welsh region"));

        when(artefactService.payloadWithinExcelLimit(argThat(arg -> arg <= 2048))).thenReturn(true);
        when(artefactService.payloadWithinPdfLimit(argThat(arg -> arg <= 256))).thenReturn(true);
    }

    @Test
    void testGenerateFilesSjpEnglish() {
        when(artefactService.getMetadataByArtefactId(any())).thenReturn(ARTEFACT);
        when(locationService.getLocationById(any())).thenReturn(LOCATION);
        when(azureBlobService.uploadFile(any(), any())).thenReturn(UPLOADED);

        publicationManagementService.generateFiles(TEST_ARTEFACT_ID, sjpPublicListInput);

        verify(azureBlobService).uploadFile(eq(TEST_ARTEFACT_ID + PDF.getExtension()), any());
        verify(azureBlobService).uploadFile(eq(TEST_ARTEFACT_ID + EXCEL.getExtension()), any());
        verify(azureBlobService, never())
            .uploadFile(eq(TEST_ARTEFACT_ID + WELSH_PDF_SUFFIX + PDF.getExtension()), any());
    }

    @Test
    void testGenerateFilesSjpWelsh() {
        when(artefactService.getMetadataByArtefactId(any())).thenReturn(WELSH_ARTEFACT);
        when(locationService.getLocationById(any())).thenReturn(LOCATION);
        when(azureBlobService.uploadFile(any(), any())).thenReturn(UPLOADED);

        publicationManagementService.generateFiles(TEST_ARTEFACT_ID, sjpPublicListInput);

        verify(azureBlobService).uploadFile(eq(TEST_ARTEFACT_ID + PDF.getExtension()), any());
        verify(azureBlobService).uploadFile(eq(TEST_ARTEFACT_ID + EXCEL.getExtension()), any());
        verify(azureBlobService, never())
            .uploadFile(eq(TEST_ARTEFACT_ID + WELSH_PDF_SUFFIX + PDF.getExtension()), any());
    }

    @Test
    void testGenerateFilesNonSjpEnglish() {
        ARTEFACT.setListType(ListType.CIVIL_DAILY_CAUSE_LIST);
        when(artefactService.getMetadataByArtefactId(any())).thenReturn(ARTEFACT);
        when(locationService.getLocationById(any())).thenReturn(LOCATION);
        when(azureBlobService.uploadFile(any(), any())).thenReturn(UPLOADED);

        publicationManagementService.generateFiles(TEST_ARTEFACT_ID, civilDailyListInput);

        verify(azureBlobService).uploadFile(eq(TEST_ARTEFACT_ID + PDF.getExtension()), any());
        verify(azureBlobService, never())
            .uploadFile(eq(TEST_ARTEFACT_ID + WELSH_PDF_SUFFIX + PDF.getExtension()), any());
        verify(azureBlobService, never()).uploadFile(eq(TEST_ARTEFACT_ID + EXCEL.getExtension()), any());
    }

    @Test
    void testGenerateFilesNonSjpWelsh() {
        WELSH_ARTEFACT.setListType(ListType.CIVIL_DAILY_CAUSE_LIST);
        when(artefactService.getMetadataByArtefactId(any())).thenReturn(WELSH_ARTEFACT);
        when(locationService.getLocationById(any())).thenReturn(LOCATION);
        when(azureBlobService.uploadFile(any(), any())).thenReturn(UPLOADED);

        publicationManagementService.generateFiles(TEST_ARTEFACT_ID, civilDailyListInput);

        verify(azureBlobService).uploadFile(eq(TEST_ARTEFACT_ID + PDF.getExtension()), any());
        verify(azureBlobService).uploadFile(eq(TEST_ARTEFACT_ID + WELSH_PDF_SUFFIX + PDF.getExtension()), any());
        verify(azureBlobService, never()).uploadFile(eq(TEST_ARTEFACT_ID + EXCEL.getExtension()), any());
    }

    @Test
    void testGenerateFilesWhenWithinExcelOutsidePdf() {
        ARTEFACT.setPayloadSize(1000F);
        when(artefactService.getMetadataByArtefactId(any())).thenReturn(ARTEFACT);
        when(locationService.getLocationById(any())).thenReturn(LOCATION);
        when(azureBlobService.uploadFile(any(), any())).thenReturn(UPLOADED);

        publicationManagementService.generateFiles(TEST_ARTEFACT_ID, sjpPublicListInput);

        verify(azureBlobService, never()).uploadFile(eq(TEST_ARTEFACT_ID + PDF.getExtension()), any());
        verify(azureBlobService, never()).uploadFile(
            eq(TEST_ARTEFACT_ID + WELSH_PDF_SUFFIX + PDF.getExtension()), any());
        verify(azureBlobService).uploadFile(eq(TEST_ARTEFACT_ID + EXCEL.getExtension()), any());
    }

    @Test
    void testGenerateFilesWhenOutsideExcelAndPdf() {
        ARTEFACT.setPayloadSize(4000F);
        when(artefactService.getMetadataByArtefactId(any())).thenReturn(ARTEFACT);
        when(locationService.getLocationById(any())).thenReturn(LOCATION);
        when(azureBlobService.uploadFile(any(), any())).thenReturn(UPLOADED);

        publicationManagementService.generateFiles(TEST_ARTEFACT_ID, sjpPublicListInput);

        verify(azureBlobService, never()).uploadFile(eq(TEST_ARTEFACT_ID + PDF.getExtension()), any());
        verify(azureBlobService, never()).uploadFile(
            eq(TEST_ARTEFACT_ID + WELSH_PDF_SUFFIX + PDF.getExtension()), any());
        verify(azureBlobService, never()).uploadFile(eq(TEST_ARTEFACT_ID + EXCEL.getExtension()), any());
    }

    @Test
    void testGenerateArtefactSummary() {
        ARTEFACT.setListType(ListType.CIVIL_DAILY_CAUSE_LIST);
        when(artefactService.getPayloadByArtefactId(any())).thenReturn(civilDailyListInput);
        when(artefactService.getMetadataByArtefactId(any())).thenReturn(ARTEFACT);

        String response = publicationManagementService.generateArtefactSummary(TEST_ARTEFACT_ID);

        assertTrue(response.contains("Case reference - This is case number"), RESPONSE_MESSAGE);
        assertTrue(response.contains("Case name - This is case name"), RESPONSE_MESSAGE);
        assertTrue(response.contains("Case type - This is a case type"), RESPONSE_MESSAGE);
        assertTrue(response.contains("Hearing type - This is hearing type"), RESPONSE_MESSAGE);
    }

    @Test
    void testGenerateArtefactSummaryWhenSummaryNotPresent() {
        when(artefactService.getPayloadByArtefactId(any())).thenReturn(sjpPublicListInput);
        when(artefactService.getMetadataByArtefactId(any())).thenReturn(ARTEFACT);

        String response = publicationManagementService.generateArtefactSummary(TEST_ARTEFACT_ID);

        assertTrue(response.isEmpty(), "Response message should be empty when no summary generator available");
    }

    @Test
    void testGenerateArtefactSummaryWithoutConverter() {
        ARTEFACT.setListType(ListType.SJP_PRESS_REGISTER);
        when(artefactService.getMetadataByArtefactId(any())).thenReturn(ARTEFACT);

        assertEquals("", publicationManagementService.generateArtefactSummary(TEST_ARTEFACT_ID),
                     RESPONSE_MESSAGE);
        verify(artefactService, never()).getPayloadByArtefactId(TEST_ARTEFACT_ID);
    }

    @Test
    void testGetStoredPdfPublicationSjp() {
        when(artefactService.getMetadataByArtefactId(TEST_ARTEFACT_ID)).thenReturn(ARTEFACT);
        when(azureBlobService.getBlobFile(TEST_ARTEFACT_ID + PDF.getExtension())).thenReturn(TEST_BYTE);

        String response = publicationManagementService.getStoredPublication(
            TEST_ARTEFACT_ID, PDF, null, TEST, true, false
        );

        byte[] decodedBytes = Base64.getDecoder().decode(response);
        assertEquals(Arrays.toString(TEST_BYTE), Arrays.toString(decodedBytes), BYTES_NO_MATCH);
    }

    @Test
    void testGetStoredAdditionalPdfPublicationSjp() {
        when(artefactService.getMetadataByArtefactId(TEST_ARTEFACT_ID)).thenReturn(ARTEFACT);
        doThrow(new NotFoundException(NOT_FOUND_MESSAGE)).when(azureBlobService)
            .getBlobFile(TEST_ARTEFACT_ID + WELSH_PDF_SUFFIX + PDF.getExtension());

        NotFoundException ex = assertThrows(NotFoundException.class, () ->
            publicationManagementService.getStoredPublication(
                TEST_ARTEFACT_ID, PDF, null, TEST, true, true
            ));

        assertTrue(ex.getMessage().contains(NOT_FOUND_MESSAGE), EXCEPTION_NOT_MATCH);
    }

    @ParameterizedTest
    @MethodSource("sjpParameters")
    void testGetStoredExcelPublicationSjp(Artefact artefact) {
        when(artefactService.getMetadataByArtefactId(TEST_ARTEFACT_ID)).thenReturn(artefact);
        when(azureBlobService.getBlobFile(TEST_ARTEFACT_ID + EXCEL.getExtension())).thenReturn(TEST_BYTE);

        String response = publicationManagementService.getStoredPublication(
            TEST_ARTEFACT_ID, EXCEL, null, TEST, true, false
        );

        byte[] decodedBytes = Base64.getDecoder().decode(response);
        assertEquals(Arrays.toString(TEST_BYTE), Arrays.toString(decodedBytes), BYTES_NO_MATCH);
    }

    @Test
    void testGetStoredPdfPublicationNonSjp() {
        ARTEFACT.setListType(ListType.CIVIL_DAILY_CAUSE_LIST);
        when(artefactService.getMetadataByArtefactId(TEST_ARTEFACT_ID)).thenReturn(ARTEFACT);
        when(azureBlobService.getBlobFile(TEST_ARTEFACT_ID + PDF.getExtension())).thenReturn(TEST_BYTE);

        String response = publicationManagementService.getStoredPublication(
            TEST_ARTEFACT_ID, PDF, null, TEST, true, false
        );

        byte[] decodedBytes = Base64.getDecoder().decode(response);
        assertEquals(Arrays.toString(TEST_BYTE), Arrays.toString(decodedBytes), BYTES_NO_MATCH);
    }

    @Test
    void testGetStoredAdditionalPdfPublicationNonSjp() {
        ARTEFACT.setListType(ListType.CIVIL_DAILY_CAUSE_LIST);
        when(artefactService.getMetadataByArtefactId(TEST_ARTEFACT_ID)).thenReturn(ARTEFACT);
        when(azureBlobService.getBlobFile(TEST_ARTEFACT_ID + WELSH_PDF_SUFFIX + PDF.getExtension()))
            .thenReturn(TEST_BYTE);

        String response = publicationManagementService.getStoredPublication(
            TEST_ARTEFACT_ID, PDF, null, TEST, true, true
        );

        byte[] decodedBytes = Base64.getDecoder().decode(response);
        assertEquals(Arrays.toString(TEST_BYTE), Arrays.toString(decodedBytes), BYTES_NO_MATCH);
    }

    @Test
    void testGetStoredExcelPublicationNonSjp() {
        ARTEFACT.setListType(ListType.CIVIL_DAILY_CAUSE_LIST);
        when(artefactService.getMetadataByArtefactId(TEST_ARTEFACT_ID)).thenReturn(ARTEFACT);
        doThrow(new NotFoundException(NOT_FOUND_MESSAGE)).when(azureBlobService)
            .getBlobFile(TEST_ARTEFACT_ID + EXCEL.getExtension());

        NotFoundException ex = assertThrows(NotFoundException.class, () ->
            publicationManagementService.getStoredPublication(
                TEST_ARTEFACT_ID, EXCEL, null, TEST, true, false
            ));

        assertTrue(ex.getMessage().contains(NOT_FOUND_MESSAGE), EXCEPTION_NOT_MATCH);
    }

    @Test
    void testGetStoredPublicationWithinFileSizeLimit() {
        when(artefactService.getMetadataByArtefactId(any())).thenReturn(ARTEFACT);
        when(azureBlobService.getBlobFile(any())).thenReturn(TEST_BYTE);

        String response = publicationManagementService.getStoredPublication(
            TEST_ARTEFACT_ID, PDF, 20, TEST, true, false
        );

        byte[] decodedBytes = Base64.getDecoder().decode(response);
        assertEquals(Arrays.toString(TEST_BYTE), Arrays.toString(decodedBytes), BYTES_NO_MATCH);
    }

    @Test
    void testGetStoredPublicationOverFileSizeLimit() {
        when(artefactService.getMetadataByArtefactId(any())).thenReturn(ARTEFACT);
        when(azureBlobService.getBlobFile(any())).thenReturn(TEST_BYTE);

        FileSizeLimitException ex = assertThrows(FileSizeLimitException.class, () ->
            publicationManagementService.getStoredPublication(
                TEST_ARTEFACT_ID, PDF, 2, TEST, true, false
            ));

        assertTrue(ex.getMessage().contains("File with type PDF for artefact with id " + TEST_ARTEFACT_ID
                                                + " has size over the limit of 2 bytes"), EXCEPTION_NOT_MATCH);
    }

    @Test
    void testGetStoredPublicationAuthorisedPublic() {
        ARTEFACT.setListType(ListType.CIVIL_DAILY_CAUSE_LIST);
        ARTEFACT.setSensitivity(Sensitivity.PUBLIC);
        when(artefactService.getMetadataByArtefactId(any())).thenReturn(ARTEFACT);
        when(azureBlobService.getBlobFile(any())).thenReturn(TEST_BYTE);

        String response = publicationManagementService.getStoredPublication(
            TEST_ARTEFACT_ID, PDF, null, TEST, false, false
        );

        byte[] decodedBytes = Base64.getDecoder().decode(response);
        assertEquals(Arrays.toString(TEST_BYTE), Arrays.toString(decodedBytes), BYTES_NO_MATCH);
    }

    @Test
    void testGetStoredPublicationAuthorisedUserIdNull() {
        ARTEFACT.setListType(ListType.CIVIL_DAILY_CAUSE_LIST);
        ARTEFACT.setSensitivity(Sensitivity.CLASSIFIED);
        when(artefactService.getMetadataByArtefactId(any())).thenReturn(ARTEFACT);
        when(azureBlobService.getBlobFile(any())).thenReturn(TEST_BYTE);

        UnauthorisedRequestException ex = assertThrows(UnauthorisedRequestException.class, () ->
            publicationManagementService.getStoredPublication(
                TEST_ARTEFACT_ID, PDF, null, null, false, false
            ));

        assertTrue(ex.getMessage().contains("User with id null is not authorised to access artefact with id"),
                   EXCEPTION_NOT_MATCH);
    }

    @Test
    void testGetStoredPublicationAuthorisedFalse() {
        ARTEFACT.setListType(ListType.CIVIL_DAILY_CAUSE_LIST);
        ARTEFACT.setSensitivity(Sensitivity.CLASSIFIED);
        when(artefactService.getMetadataByArtefactId(any())).thenReturn(ARTEFACT);
        when(azureBlobService.getBlobFile(any())).thenReturn(TEST_BYTE);
        when(accountManagementService.getIsAuthorised(any(), any(), any())).thenReturn(false);

        UnauthorisedRequestException ex = assertThrows(UnauthorisedRequestException.class, () ->
            publicationManagementService.getStoredPublication(
                TEST_ARTEFACT_ID, PDF, null, TEST_USER_ID, false, false
            ), "Expected exception to be thrown");

        assertTrue(ex.getMessage().contains("is not authorised to access artefact with id"),
                   EXCEPTION_NOT_MATCH);
    }

    @Test
    void testDeleteFilesV2SjpEnglish() {
        publicationManagementService.deleteFiles(TEST_ARTEFACT_ID, ListType.SJP_PUBLIC_LIST, Language.ENGLISH);

        verify(azureBlobService).deleteBlobFile(TEST_ARTEFACT_ID + PDF.getExtension());
        verify(azureBlobService, never()).deleteBlobFile(TEST_ARTEFACT_ID + WELSH_PDF_SUFFIX + PDF.getExtension());
        verify(azureBlobService).deleteBlobFile(TEST_ARTEFACT_ID + EXCEL.getExtension());
    }

    @Test
    void testDeleteFilesV2SjpWelsh() {
        publicationManagementService.deleteFiles(TEST_ARTEFACT_ID, ListType.SJP_PUBLIC_LIST, Language.WELSH);

        verify(azureBlobService).deleteBlobFile(TEST_ARTEFACT_ID + PDF.getExtension());
        verify(azureBlobService, never()).deleteBlobFile(TEST_ARTEFACT_ID + WELSH_PDF_SUFFIX + PDF.getExtension());
        verify(azureBlobService).deleteBlobFile(TEST_ARTEFACT_ID + EXCEL.getExtension());
    }

    @Test
    void testDeleteFilesV2NonSjpEnglish() {
        publicationManagementService.deleteFiles(TEST_ARTEFACT_ID, ListType.CIVIL_DAILY_CAUSE_LIST, Language.ENGLISH);

        verify(azureBlobService).deleteBlobFile(TEST_ARTEFACT_ID + PDF.getExtension());
        verify(azureBlobService, never()).deleteBlobFile(TEST_ARTEFACT_ID + WELSH_PDF_SUFFIX + PDF.getExtension());
        verify(azureBlobService, never()).deleteBlobFile(TEST_ARTEFACT_ID + EXCEL.getExtension());
    }

    @Test
    void testDeleteFilesV2NonSjpWelsh() {
        publicationManagementService.deleteFiles(TEST_ARTEFACT_ID, ListType.CIVIL_DAILY_CAUSE_LIST, Language.WELSH);

        verify(azureBlobService).deleteBlobFile(TEST_ARTEFACT_ID + PDF.getExtension());
        verify(azureBlobService).deleteBlobFile(TEST_ARTEFACT_ID + WELSH_PDF_SUFFIX + PDF.getExtension());
        verify(azureBlobService, never()).deleteBlobFile(TEST_ARTEFACT_ID + EXCEL.getExtension());
    }

    @Test
    void testFileExistsReturnTrueIfAllFilesExist() {
        when(azureBlobService.blobFileExists(TEST_ARTEFACT_ID + PDF.getExtension()))
            .thenReturn(true);
        when(azureBlobService.blobFileExists(TEST_ARTEFACT_ID  + WELSH_PDF_SUFFIX + PDF.getExtension()))
            .thenReturn(true);
        when(azureBlobService.blobFileExists(TEST_ARTEFACT_ID + EXCEL.getExtension()))
            .thenReturn(true);

        assertTrue(publicationManagementService.fileExists(TEST_ARTEFACT_ID), FILE_EXISTS_FLAG_MESSAGE);
    }

    @Test
    void testFileExistsReturnTrueIfOnlyOneFileExists() {
        when(azureBlobService.blobFileExists(TEST_ARTEFACT_ID + PDF.getExtension()))
            .thenReturn(false);
        when(azureBlobService.blobFileExists(TEST_ARTEFACT_ID  + WELSH_PDF_SUFFIX + PDF.getExtension()))
            .thenReturn(false);
        when(azureBlobService.blobFileExists(TEST_ARTEFACT_ID + EXCEL.getExtension()))
            .thenReturn(true);

        assertTrue(publicationManagementService.fileExists(TEST_ARTEFACT_ID), FILE_EXISTS_FLAG_MESSAGE);
    }

    @Test
    void testFileExistsReturnFalseIfNoFileExist() {
        when(azureBlobService.blobFileExists(TEST_ARTEFACT_ID + PDF.getExtension()))
            .thenReturn(false);
        when(azureBlobService.blobFileExists(TEST_ARTEFACT_ID  + WELSH_PDF_SUFFIX + PDF.getExtension()))
            .thenReturn(false);
        when(azureBlobService.blobFileExists(TEST_ARTEFACT_ID + EXCEL.getExtension()))
            .thenReturn(false);

        assertFalse(publicationManagementService.fileExists(TEST_ARTEFACT_ID), FILE_EXISTS_FLAG_MESSAGE);
    }

    @Test
    void testGetFileSizesReturnFileSizeIfPresent() {
        when(azureBlobService.getBlobSize(TEST_ARTEFACT_ID + PDF.getExtension()))
            .thenReturn(1234L);
        when(azureBlobService.getBlobSize(TEST_ARTEFACT_ID  + WELSH_PDF_SUFFIX + PDF.getExtension()))
            .thenReturn(null);
        when(azureBlobService.getBlobSize(TEST_ARTEFACT_ID + EXCEL.getExtension()))
            .thenReturn(123L);

        PublicationFileSizes fileSizes = publicationManagementService.getFileSizes(TEST_ARTEFACT_ID);

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
