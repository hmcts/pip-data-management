package uk.gov.hmcts.reform.pip.data.management.service.publication;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import uk.gov.hmcts.reform.pip.data.management.database.AzurePublicationBlobService;
import uk.gov.hmcts.reform.pip.data.management.models.location.Location;
import uk.gov.hmcts.reform.pip.data.management.models.publication.Artefact;
import uk.gov.hmcts.reform.pip.data.management.service.location.LocationService;
import uk.gov.hmcts.reform.pip.data.management.utils.IntegrationBasicTestBase;
import uk.gov.hmcts.reform.pip.model.publication.Language;
import uk.gov.hmcts.reform.pip.model.publication.ListType;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.pip.model.publication.FileType.EXCEL;
import static uk.gov.hmcts.reform.pip.model.publication.FileType.PDF;

@ActiveProfiles("integration-basic")
@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class PublicationFileManagementServiceTest extends IntegrationBasicTestBase {
    @MockitoBean
    private PublicationRetrievalService publicationRetrievalService;

    @MockitoBean
    private LocationService locationService;

    @MockitoBean
    private AzurePublicationBlobService azureBlobService;

    @Autowired
    private PublicationFileManagementService publicationFileManagementService;

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final Artefact ARTEFACT = new Artefact();
    private static final Artefact WELSH_ARTEFACT = new Artefact();
    private static final Location LOCATION = new Location();

    private static final UUID TEST_ARTEFACT_ID = UUID.randomUUID();
    private static final String UPLOADED = "uploaded";
    private static final String WELSH_PDF_SUFFIX = "_cy";

    private static String sjpPublicListInput;
    private static String civilDailyListInput;

    private String getInput(String resourcePath) throws IOException {
        try (InputStream inputStream = this.getClass()
            .getClassLoader()
            .getResourceAsStream(resourcePath)) {
            return IOUtils.toString(inputStream, Charset.defaultCharset());
        }
    }

    @BeforeAll
    void startup() throws IOException {
        OBJECT_MAPPER.findAndRegisterModules();
        sjpPublicListInput = getInput("data/sjp-public-list/sjpPublicList.json");
        civilDailyListInput = getInput("data/civil-daily-cause-list/civilDailyCauseList.json");
    }

    @BeforeEach
    void setup() {
        ARTEFACT.setArtefactId(TEST_ARTEFACT_ID);
        ARTEFACT.setContentDate(LocalDateTime.now());
        ARTEFACT.setLocationId("1");
        ARTEFACT.setProvenance("france");
        ARTEFACT.setLanguage(Language.ENGLISH);
        ARTEFACT.setListType(ListType.SJP_PUBLIC_LIST);
        ARTEFACT.setLastReceivedDate(LocalDateTime.now());
        ARTEFACT.setPayloadSize(100F);

        WELSH_ARTEFACT.setArtefactId(TEST_ARTEFACT_ID);
        WELSH_ARTEFACT.setContentDate(LocalDateTime.now());
        WELSH_ARTEFACT.setLocationId("1");
        WELSH_ARTEFACT.setProvenance("france");
        WELSH_ARTEFACT.setLanguage(Language.WELSH);
        WELSH_ARTEFACT.setListType(ListType.SJP_PUBLIC_LIST);
        WELSH_ARTEFACT.setLastReceivedDate(LocalDateTime.now());
        WELSH_ARTEFACT.setPayloadSize(100F);

        LOCATION.setLocationId(1);
        LOCATION.setName("Test");
        LOCATION.setWelshName("Test");
        LOCATION.setRegion(Collections.singletonList("Region"));
        LOCATION.setWelshRegion(Collections.singletonList("Welsh region"));

        when(publicationRetrievalService.payloadWithinExcelLimit(argThat(arg -> arg <= 2048))).thenReturn(true);
        when(publicationRetrievalService.payloadWithinPdfLimit(argThat(arg -> arg <= 256))).thenReturn(true);
    }

    @Test
    void testGenerateFilesSjpEnglish() {
        when(publicationRetrievalService.getMetadataByArtefactId(any())).thenReturn(ARTEFACT);
        when(locationService.getLocationById(any())).thenReturn(LOCATION);
        when(azureBlobService.uploadFile(any(), any())).thenReturn(UPLOADED);

        publicationFileManagementService.generateFiles(TEST_ARTEFACT_ID, sjpPublicListInput);

        verify(azureBlobService).uploadFile(eq(TEST_ARTEFACT_ID + PDF.getExtension()), any());
        verify(azureBlobService).uploadFile(eq(TEST_ARTEFACT_ID + EXCEL.getExtension()), any());
        verify(azureBlobService, never())
            .uploadFile(eq(TEST_ARTEFACT_ID + WELSH_PDF_SUFFIX + PDF.getExtension()), any());
    }

    @Test
    void testGenerateFilesSjpWelsh() {
        when(publicationRetrievalService.getMetadataByArtefactId(any())).thenReturn(WELSH_ARTEFACT);
        when(locationService.getLocationById(any())).thenReturn(LOCATION);
        when(azureBlobService.uploadFile(any(), any())).thenReturn(UPLOADED);

        publicationFileManagementService.generateFiles(TEST_ARTEFACT_ID, sjpPublicListInput);

        verify(azureBlobService).uploadFile(eq(TEST_ARTEFACT_ID + PDF.getExtension()), any());
        verify(azureBlobService).uploadFile(eq(TEST_ARTEFACT_ID + EXCEL.getExtension()), any());
        verify(azureBlobService, never())
            .uploadFile(eq(TEST_ARTEFACT_ID + WELSH_PDF_SUFFIX + PDF.getExtension()), any());
    }

    @Test
    void testGenerateFilesNonSjpEnglish() {
        ARTEFACT.setListType(ListType.CIVIL_DAILY_CAUSE_LIST);
        when(publicationRetrievalService.getMetadataByArtefactId(any())).thenReturn(ARTEFACT);
        when(locationService.getLocationById(any())).thenReturn(LOCATION);
        when(azureBlobService.uploadFile(any(), any())).thenReturn(UPLOADED);

        publicationFileManagementService.generateFiles(TEST_ARTEFACT_ID, civilDailyListInput);

        verify(azureBlobService).uploadFile(eq(TEST_ARTEFACT_ID + PDF.getExtension()), any());
        verify(azureBlobService, never())
            .uploadFile(eq(TEST_ARTEFACT_ID + WELSH_PDF_SUFFIX + PDF.getExtension()), any());
        verify(azureBlobService, never()).uploadFile(eq(TEST_ARTEFACT_ID + EXCEL.getExtension()), any());
    }

    @Test
    void testGenerateFilesNonSjpWelsh() {
        WELSH_ARTEFACT.setListType(ListType.CIVIL_DAILY_CAUSE_LIST);
        when(publicationRetrievalService.getMetadataByArtefactId(any())).thenReturn(WELSH_ARTEFACT);
        when(locationService.getLocationById(any())).thenReturn(LOCATION);
        when(azureBlobService.uploadFile(any(), any())).thenReturn(UPLOADED);

        publicationFileManagementService.generateFiles(TEST_ARTEFACT_ID, civilDailyListInput);

        verify(azureBlobService).uploadFile(eq(TEST_ARTEFACT_ID + PDF.getExtension()), any());
        verify(azureBlobService).uploadFile(eq(TEST_ARTEFACT_ID + WELSH_PDF_SUFFIX + PDF.getExtension()), any());
        verify(azureBlobService, never()).uploadFile(eq(TEST_ARTEFACT_ID + EXCEL.getExtension()), any());
    }
}
