package uk.gov.hmcts.reform.pip.data.management.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.IOUtils;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import uk.gov.hmcts.reform.pip.data.management.models.PublicationFiles;
import uk.gov.hmcts.reform.pip.data.management.models.location.Location;
import uk.gov.hmcts.reform.pip.data.management.models.publication.Artefact;
import uk.gov.hmcts.reform.pip.data.management.service.publication.ArtefactService;
import uk.gov.hmcts.reform.pip.data.management.utils.IntegrationBasicTestBase;
import uk.gov.hmcts.reform.pip.model.publication.Language;
import uk.gov.hmcts.reform.pip.model.publication.ListType;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ActiveProfiles("integration-basic")
@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class PublicationFileGenerationServiceTest extends IntegrationBasicTestBase {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final Artefact ARTEFACT = new Artefact();
    private static final Artefact WELSH_ARTEFACT = new Artefact();
    private static final Location LOCATION = new Location();

    private static final UUID ARTEFACT_ID = UUID.randomUUID();
    private static final Integer LOCATION_ID = 1;

    private static final String FILE_PRESENT_MESSAGE = "Files should be present";
    private static final String FILE_NOT_PRESENT_MESSAGE = "Files should not be present";
    private static final String FILE_EMPTY_MESSAGE = "File should be empty";
    private static final String FILE_NOT_EMPTY_MESSAGE = "File should not be empty";

    private String sjpPublicListInput;
    private String civilDailyListInput;

    @MockitoBean
    private ArtefactService artefactService;

    @MockitoBean
    private LocationService locationService;

    @Autowired
    private PublicationFileGenerationService publicationFileGenerationService;

    @BeforeAll
    void startup() throws IOException {
        OBJECT_MAPPER.findAndRegisterModules();
        sjpPublicListInput = getInput("data/sjp-public-list/sjpPublicList.json");
        civilDailyListInput = getInput("data/civil-daily-cause-list/civilDailyCauseList.json");
    }

    private String getInput(String resourcePath) throws IOException {
        try (InputStream inputStream = this.getClass()
            .getClassLoader()
            .getResourceAsStream(resourcePath)) {
            return IOUtils.toString(inputStream, Charset.defaultCharset());
        }
    }

    @BeforeEach
    void setup() {
        ARTEFACT.setArtefactId(ARTEFACT_ID);
        ARTEFACT.setContentDate(LocalDateTime.now());
        ARTEFACT.setLocationId(String.valueOf(LOCATION_ID));
        ARTEFACT.setProvenance("france");
        ARTEFACT.setLanguage(Language.ENGLISH);
        ARTEFACT.setListType(ListType.SJP_PUBLIC_LIST);
        ARTEFACT.setLastReceivedDate(LocalDateTime.now());
        ARTEFACT.setPayloadSize(100F);

        WELSH_ARTEFACT.setArtefactId(ARTEFACT_ID);
        WELSH_ARTEFACT.setContentDate(LocalDateTime.now());
        WELSH_ARTEFACT.setLocationId(String.valueOf(LOCATION_ID));
        WELSH_ARTEFACT.setProvenance("france");
        WELSH_ARTEFACT.setLanguage(Language.WELSH);
        WELSH_ARTEFACT.setListType(ListType.SJP_PUBLIC_LIST);
        WELSH_ARTEFACT.setLastReceivedDate(LocalDateTime.now());
        WELSH_ARTEFACT.setPayloadSize(100F);

        LOCATION.setLocationId(LOCATION_ID);
        LOCATION.setName("Test");
        LOCATION.setWelshName("Test");
        LOCATION.setRegion(Collections.singletonList("Region"));
        LOCATION.setWelshRegion(Collections.singletonList("Welsh region"));

        when(artefactService.payloadWithinExcelLimit(argThat(arg -> arg <= 2048))).thenReturn(true);
        when(artefactService.payloadWithinPdfLimit(argThat(arg -> arg <= 256))).thenReturn(true);
    }

    @Test
    void testGenerateFilesSjpEnglish() {
        when(artefactService.getMetadataByArtefactId(ARTEFACT_ID)).thenReturn(ARTEFACT);
        when(locationService.getLocationById(LOCATION_ID)).thenReturn(LOCATION);

        Optional<PublicationFiles> files = publicationFileGenerationService.generate(ARTEFACT_ID, sjpPublicListInput);
        verify(artefactService, never()).getPayloadByArtefactId(ARTEFACT_ID);

        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(files)
            .as(FILE_PRESENT_MESSAGE)
            .isPresent();

        softly.assertThat(files.get().getPrimaryPdf())
            .as(FILE_NOT_EMPTY_MESSAGE)
            .isNotEmpty();

        softly.assertThat(files.get().getAdditionalPdf())
            .as(FILE_EMPTY_MESSAGE)
            .isEmpty();

        softly.assertThat(files.get().getExcel())
            .as(FILE_NOT_EMPTY_MESSAGE)
            .isNotEmpty();

        softly.assertAll();
    }

    @Test
    void testGenerateFilesSjpWelsh() {
        when(artefactService.getMetadataByArtefactId(ARTEFACT_ID)).thenReturn(WELSH_ARTEFACT);
        when(locationService.getLocationById(LOCATION_ID)).thenReturn(LOCATION);

        Optional<PublicationFiles> files = publicationFileGenerationService.generate(ARTEFACT_ID, sjpPublicListInput);
        verify(artefactService, never()).getPayloadByArtefactId(ARTEFACT_ID);

        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(files)
            .as(FILE_PRESENT_MESSAGE)
            .isPresent();

        softly.assertThat(files.get().getPrimaryPdf())
            .as(FILE_NOT_EMPTY_MESSAGE)
            .isNotEmpty();

        softly.assertThat(files.get().getAdditionalPdf())
            .as(FILE_EMPTY_MESSAGE)
            .isEmpty();

        softly.assertThat(files.get().getExcel())
            .as(FILE_NOT_EMPTY_MESSAGE)
            .isNotEmpty();

        softly.assertAll();
    }

    @Test
    void testGenerateFilesNonSjpEnglish() {
        ARTEFACT.setListType(ListType.CIVIL_DAILY_CAUSE_LIST);
        when(artefactService.getMetadataByArtefactId(ARTEFACT_ID)).thenReturn(ARTEFACT);
        when(locationService.getLocationById(LOCATION_ID)).thenReturn(LOCATION);

        Optional<PublicationFiles> files = publicationFileGenerationService.generate(ARTEFACT_ID, civilDailyListInput);
        verify(artefactService, never()).getPayloadByArtefactId(ARTEFACT_ID);

        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(files)
            .as(FILE_PRESENT_MESSAGE)
            .isPresent();

        softly.assertThat(files.get().getPrimaryPdf())
            .as(FILE_NOT_EMPTY_MESSAGE)
            .isNotEmpty();

        softly.assertThat(files.get().getAdditionalPdf())
            .as(FILE_EMPTY_MESSAGE)
            .isEmpty();

        softly.assertThat(files.get().getExcel())
            .as(FILE_EMPTY_MESSAGE)
            .isEmpty();

        softly.assertAll();
    }

    @Test
    void testGenerateFilesNonSjpWelsh() {
        WELSH_ARTEFACT.setListType(ListType.CIVIL_DAILY_CAUSE_LIST);
        when(artefactService.getMetadataByArtefactId(ARTEFACT_ID)).thenReturn(WELSH_ARTEFACT);
        when(locationService.getLocationById(LOCATION_ID)).thenReturn(LOCATION);

        Optional<PublicationFiles> files = publicationFileGenerationService.generate(ARTEFACT_ID, civilDailyListInput);
        verify(artefactService, never()).getPayloadByArtefactId(ARTEFACT_ID);

        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(files)
            .as(FILE_PRESENT_MESSAGE)
            .isPresent();

        softly.assertThat(files.get().getPrimaryPdf())
            .as(FILE_NOT_EMPTY_MESSAGE)
            .isNotEmpty();

        softly.assertThat(files.get().getAdditionalPdf())
            .as(FILE_NOT_EMPTY_MESSAGE)
            .isNotEmpty();

        softly.assertThat(files.get().getExcel())
            .as(FILE_EMPTY_MESSAGE)
            .isEmpty();

        softly.assertAll();
    }

    @Test
    void testGenerateFilesWhenWithinExcelOutsidePdf() {
        ARTEFACT.setPayloadSize(1000F);
        when(artefactService.getMetadataByArtefactId(ARTEFACT_ID)).thenReturn(ARTEFACT);
        when(locationService.getLocationById(LOCATION_ID)).thenReturn(LOCATION);

        Optional<PublicationFiles> files = publicationFileGenerationService.generate(ARTEFACT_ID, sjpPublicListInput);
        verify(artefactService, never()).getPayloadByArtefactId(ARTEFACT_ID);

        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(files)
            .as(FILE_PRESENT_MESSAGE)
            .isPresent();

        softly.assertThat(files.get().getPrimaryPdf())
            .as(FILE_NOT_EMPTY_MESSAGE)
            .isEmpty();

        softly.assertThat(files.get().getAdditionalPdf())
            .as(FILE_EMPTY_MESSAGE)
            .isEmpty();

        softly.assertThat(files.get().getExcel())
            .as(FILE_NOT_EMPTY_MESSAGE)
            .isNotEmpty();

        softly.assertAll();
    }

    @Test
    void testGenerateFilesWhenOutsideExcelAndPdf() {
        ARTEFACT.setPayloadSize(4000F);
        when(artefactService.getMetadataByArtefactId(ARTEFACT_ID)).thenReturn(ARTEFACT);
        when(locationService.getLocationById(LOCATION_ID)).thenReturn(LOCATION);

        Optional<PublicationFiles> files = publicationFileGenerationService.generate(ARTEFACT_ID, sjpPublicListInput);
        verify(artefactService, never()).getPayloadByArtefactId(ARTEFACT_ID);

        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(files)
            .as(FILE_PRESENT_MESSAGE)
            .isPresent();

        softly.assertThat(files.get().getPrimaryPdf())
            .as(FILE_NOT_EMPTY_MESSAGE)
            .isEmpty();

        softly.assertThat(files.get().getAdditionalPdf())
            .as(FILE_EMPTY_MESSAGE)
            .isEmpty();

        softly.assertThat(files.get().getExcel())
            .as(FILE_NOT_EMPTY_MESSAGE)
            .isEmpty();

        softly.assertAll();
    }

    @Test
    void testGenerateFilesWithoutPayload() {
        ARTEFACT.setListType(ListType.CIVIL_DAILY_CAUSE_LIST);
        when(artefactService.getPayloadByArtefactId(ARTEFACT_ID)).thenReturn(civilDailyListInput);
        when(artefactService.getMetadataByArtefactId(ARTEFACT_ID)).thenReturn(ARTEFACT);
        when(locationService.getLocationById(LOCATION_ID)).thenReturn(LOCATION);

        publicationFileGenerationService.generate(ARTEFACT_ID, null);
        verify(artefactService).getPayloadByArtefactId(ARTEFACT_ID);
    }

    @Test
    void testGenerateFilesWithoutConverter() {
        ARTEFACT.setListType(ListType.SJP_PRESS_REGISTER);
        when(artefactService.getMetadataByArtefactId(ARTEFACT_ID)).thenReturn(ARTEFACT);
        when(locationService.getLocationById(LOCATION_ID)).thenReturn(LOCATION);

        assertThat(publicationFileGenerationService.generate(ARTEFACT_ID, sjpPublicListInput))
            .as(FILE_NOT_PRESENT_MESSAGE)
            .isEmpty();
    }
}
