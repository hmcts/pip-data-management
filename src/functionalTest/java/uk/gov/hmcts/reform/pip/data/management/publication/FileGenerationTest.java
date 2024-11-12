package uk.gov.hmcts.reform.pip.data.management.publication;

import io.restassured.response.Response;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.testcontainers.shaded.org.bouncycastle.util.encoders.Base64;
import uk.gov.hmcts.reform.pip.data.management.config.PublicationConfiguration;
import uk.gov.hmcts.reform.pip.data.management.models.PublicationFileSizes;
import uk.gov.hmcts.reform.pip.data.management.utils.FunctionalTestBase;
import uk.gov.hmcts.reform.pip.data.management.utils.OAuthClient;
import uk.gov.hmcts.reform.pip.model.publication.Artefact;
import uk.gov.hmcts.reform.pip.model.publication.ArtefactType;
import uk.gov.hmcts.reform.pip.model.publication.FileType;
import uk.gov.hmcts.reform.pip.model.publication.Language;
import uk.gov.hmcts.reform.pip.model.publication.ListType;
import uk.gov.hmcts.reform.pip.model.publication.Sensitivity;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import static com.github.dockerjava.zerodep.shaded.org.apache.hc.core5.http.HttpHeaders.AUTHORIZATION;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.OK;
import static uk.gov.hmcts.reform.pip.data.management.utils.TestUtil.BEARER;
import static uk.gov.hmcts.reform.pip.data.management.utils.TestUtil.randomLocationId;

@ExtendWith(SpringExtension.class)
@ActiveProfiles(profiles = "functional")
@SpringBootTest(classes = {OAuthClient.class})
@SuppressWarnings("PMD.ExcessiveImports")
class FileGenerationTest extends FunctionalTestBase {

    private static final String UPLOAD_PUBLICATION_URL = "/publication";
    private static final String FILE_EXISTS_URL = "/publication/%s/exists";
    private static final String FILE_SIZES_URL = "/publication/%s/sizes";
    private static final String SUMMARY_URL = "/publication/%s/summary";
    private static final String GET_FILE_URL = "/publication/%s/%s";

    private static final String TESTING_SUPPORT_LOCATION_URL = "/testing-support/location/";
    private static final String TESTING_SUPPORT_PUBLICATION_URL = "/testing-support/publication/";
    private static final ArtefactType ARTEFACT_TYPE = ArtefactType.LIST;
    private static final String PROVENANCE = "MANUAL_UPLOAD";
    private static final String SOURCE_ARTEFACT_ID = "sourceArtefactId";
    private static final LocalDateTime DISPLAY_FROM = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
    private static final String COURT_ID = randomLocationId();
    private static final String COURT_NAME = "TestLocation" + COURT_ID;
    private static final String CIVIL_CAUSE_LIST_FILE = "data/civilDailyCauseList.json";
    private static final String SJP_PUBLIC_LIST_FILE = "data/sjpPublicList.json";
    private static final String X_SYSTEM_FILE_HEADER = "x-system";

    private static LocalDateTime contentDate = LocalDateTime.now().toLocalDate().atStartOfDay()
        .truncatedTo(ChronoUnit.SECONDS);

    private Map<String, String> headerMap;

    @Value("${test-user-id}")
    public String testUserId;

    @BeforeAll
    public void setup() {
        doPostRequest(
            TESTING_SUPPORT_LOCATION_URL + COURT_ID,
            Map.of(AUTHORIZATION, BEARER + accessToken), COURT_NAME
        );
    }

    @BeforeEach
    public void setupEach() {
        headerMap = new ConcurrentHashMap<>();
        headerMap.put(AUTHORIZATION, BEARER + accessToken);
    }

    @AfterAll
    public void teardown() {
        doDeleteRequest(TESTING_SUPPORT_PUBLICATION_URL + COURT_NAME, Map.of(AUTHORIZATION, BEARER + accessToken));
        doDeleteRequest(TESTING_SUPPORT_LOCATION_URL + COURT_NAME, Map.of(AUTHORIZATION, BEARER + accessToken));
    }

    private Artefact uploadPublication(
        ListType listType, String file, Language language, Sensitivity sensitivity) throws IOException {
        Map<String, String> headerMap = new ConcurrentHashMap<>();
        headerMap.put(AUTHORIZATION, BEARER + accessToken);
        headerMap.put(PublicationConfiguration.TYPE_HEADER, ARTEFACT_TYPE.toString());
        headerMap.put(PublicationConfiguration.PROVENANCE_HEADER, PROVENANCE);
        headerMap.put(PublicationConfiguration.SOURCE_ARTEFACT_ID_HEADER, SOURCE_ARTEFACT_ID);
        headerMap.put(PublicationConfiguration.DISPLAY_FROM_HEADER, DISPLAY_FROM.toString());
        headerMap.put(PublicationConfiguration.DISPLAY_TO_HEADER, DISPLAY_FROM.plusDays(1).toString());
        headerMap.put(PublicationConfiguration.COURT_ID, COURT_ID);
        headerMap.put(PublicationConfiguration.LIST_TYPE, listType.toString());
        headerMap.put(PublicationConfiguration.CONTENT_DATE, contentDate.toString());
        headerMap.put(PublicationConfiguration.SENSITIVITY_HEADER, sensitivity.toString());
        headerMap.put(PublicationConfiguration.LANGUAGE_HEADER, language.toString());
        headerMap.put("Content-Type", "application/json");

        Artefact artefact;
        try (InputStream jsonFile = this.getClass().getClassLoader()
            .getResourceAsStream(file)) {
            final String jsonString = new String(jsonFile.readAllBytes(), StandardCharsets.UTF_8);

            final Response response = doPostRequest(
                UPLOAD_PUBLICATION_URL,
                headerMap, jsonString
            );
            assertThat(response.getStatusCode()).isEqualTo(CREATED.value());
            contentDate = contentDate.plusDays(1);
            artefact = response.getBody().as(Artefact.class);
        }

        Awaitility.with().pollInterval(1, TimeUnit.SECONDS).await().until(() -> {
            Response existsResponse = doGetRequest(String.format(FILE_EXISTS_URL, artefact.getArtefactId()), headerMap);
            return existsResponse.getStatusCode() == OK.value() && existsResponse.getBody().as(Boolean.class);
        });

        Response existsResponse = doGetRequest(String.format(FILE_EXISTS_URL, artefact.getArtefactId()), headerMap);
        assertThat(existsResponse.getStatusCode()).isEqualTo(OK.value());
        assertTrue(existsResponse.getBody().as(Boolean.class), "Files have not been generated");

        return artefact;
    }

    @Test
    void shouldGenerateAPdfOnPublicationUpload() throws Exception {
        Artefact artefact = uploadPublication(ListType.CIVIL_DAILY_CAUSE_LIST,
                                              CIVIL_CAUSE_LIST_FILE,
                                              Language.ENGLISH, Sensitivity.PUBLIC
        );

        Response sizesResponse = doGetRequest(String.format(FILE_SIZES_URL, artefact.getArtefactId()), headerMap);
        assertThat(sizesResponse.getStatusCode()).isEqualTo(OK.value());

        PublicationFileSizes publicationFileSizes = sizesResponse.getBody().as(PublicationFileSizes.class);
        assertNotNull(publicationFileSizes.getPrimaryPdf(), "Primary PDF has not been generated");
        assertNull(publicationFileSizes.getAdditionalPdf(), "Additional PDF has been generated");
        assertNull(publicationFileSizes.getExcel(), "Excel has been generated");

        headerMap.put(X_SYSTEM_FILE_HEADER, Boolean.TRUE.toString());

        Response fileResponse = doGetRequest(
            String.format(GET_FILE_URL, artefact.getArtefactId(), FileType.PDF.name()), headerMap);
        assertThat(fileResponse.getStatusCode()).isEqualTo(OK.value());

        assertDoesNotThrow(() -> {
            PDDocument pdDocument = PDDocument.load(Base64.decode(fileResponse.getBody().asByteArray()));
            pdDocument.close();
        });
    }

    @Test
    void shouldGenerateAnExcelOnPublicationUpload() throws Exception {
        Artefact artefact = uploadPublication(ListType.SJP_PUBLIC_LIST,
                                              SJP_PUBLIC_LIST_FILE,
                                              Language.ENGLISH,  Sensitivity.PUBLIC
        );

        Response sizesResponse = doGetRequest(String.format(FILE_SIZES_URL, artefact.getArtefactId()), headerMap);
        assertThat(sizesResponse.getStatusCode()).isEqualTo(OK.value());

        PublicationFileSizes publicationFileSizes = sizesResponse.getBody().as(PublicationFileSizes.class);
        assertNotNull(publicationFileSizes.getPrimaryPdf(), "Primary PDF has not been generated");
        assertNull(publicationFileSizes.getAdditionalPdf(), "Additional PDF has been generated");
        assertNotNull(publicationFileSizes.getExcel(), "Excel has not been generated");

        headerMap.put(X_SYSTEM_FILE_HEADER, Boolean.TRUE.toString());

        Response additionalPdfResponse = doGetRequest(
            String.format(GET_FILE_URL, artefact.getArtefactId(), "EXCEL"), headerMap);
        assertThat(additionalPdfResponse.getStatusCode()).isEqualTo(OK.value());

        assertDoesNotThrow(() -> {
            XSSFWorkbook workbook = new XSSFWorkbook(
                new ByteArrayInputStream(Base64.decode(additionalPdfResponse.getBody().asByteArray())));
            workbook.close();
        });
    }

    @Test
    void shouldGenerateAWelshPdfOnPublicationUpload() throws Exception {
        Artefact artefact = uploadPublication(ListType.CIVIL_DAILY_CAUSE_LIST,
                                              CIVIL_CAUSE_LIST_FILE,
                                              Language.WELSH, Sensitivity.PUBLIC);

        Response sizesResponse = doGetRequest(String.format(FILE_SIZES_URL, artefact.getArtefactId()), headerMap);
        assertThat(sizesResponse.getStatusCode()).isEqualTo(OK.value());

        PublicationFileSizes publicationFileSizes = sizesResponse.getBody().as(PublicationFileSizes.class);
        assertNotNull(publicationFileSizes.getPrimaryPdf(), "Primary PDF has not been generated");
        assertNotNull(publicationFileSizes.getAdditionalPdf(), "Additional PDF has not been generated");
        assertNull(publicationFileSizes.getExcel(), "Excel has been generated");

        headerMap.put(X_SYSTEM_FILE_HEADER, Boolean.TRUE.toString());
        headerMap.put("x-additional-pdf", Boolean.TRUE.toString());

        Response additionalPdfResponse = doGetRequest(
            String.format(GET_FILE_URL, artefact.getArtefactId(), FileType.PDF.name()), headerMap);
        assertThat(additionalPdfResponse.getStatusCode()).isEqualTo(OK.value());

        assertDoesNotThrow(() -> {
            PDDocument pdDocument = PDDocument.load(Base64.decode(additionalPdfResponse.getBody().asByteArray()));
            pdDocument.close();
        });
    }

    @Test
    void shouldGenerateArtefactSummary() throws Exception {
        Artefact artefact = uploadPublication(ListType.CIVIL_DAILY_CAUSE_LIST,
                                              CIVIL_CAUSE_LIST_FILE,
                                              Language.ENGLISH,
                                              Sensitivity.PUBLIC
        );

        Response summaryResponse = doGetRequest(String.format(SUMMARY_URL, artefact.getArtefactId()), headerMap);
        assertThat(summaryResponse.getStatusCode()).isEqualTo(OK.value());

        String summary = summaryResponse.getBody().print();
        assertNotNull(summary, "Artefact summary is null");
        assertTrue(summary.contains("Case reference - 45684548"), "Summary does not contain expected data");
    }

    @Test
    void shouldGetFilesWhenMaxSizeSet() throws Exception {
        Artefact artefact = uploadPublication(ListType.CIVIL_DAILY_CAUSE_LIST,
                                              CIVIL_CAUSE_LIST_FILE,
                                              Language.ENGLISH,
                                              Sensitivity.PUBLIC
        );

        headerMap.put(X_SYSTEM_FILE_HEADER, Boolean.TRUE.toString());
        Response additionalPdfResponse = doGetRequest(
            String.format(GET_FILE_URL + "?maxFileSize=%s", artefact.getArtefactId(),
                          FileType.PDF.name(), 100 * 1024), headerMap);
        assertThat(additionalPdfResponse.getStatusCode()).isEqualTo(OK.value());
    }

    @Test
    void shouldGetFilesWhenUserSetInsteadOfSystem() throws Exception {
        Artefact artefact = uploadPublication(ListType.SJP_PUBLIC_LIST,
                                              SJP_PUBLIC_LIST_FILE,
                                              Language.ENGLISH,
                                              Sensitivity.CLASSIFIED
        );

        headerMap.put("x-user-id", testUserId);
        Response additionalPdfResponse = doGetRequest(
            String.format(GET_FILE_URL, artefact.getArtefactId(), FileType.PDF.name()), headerMap);
        assertThat(additionalPdfResponse.getStatusCode()).isEqualTo(OK.value());

    }

}
