package uk.gov.hmcts.reform.pip.data.management.publication;

import io.restassured.response.Response;
import io.zonky.test.db.AutoConfigureEmbeddedDatabase;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.pip.data.management.Application;
import uk.gov.hmcts.reform.pip.data.management.config.PublicationConfiguration;
import uk.gov.hmcts.reform.pip.data.management.models.publication.Artefact;
import uk.gov.hmcts.reform.pip.data.management.utils.FunctionalTestBase;
import uk.gov.hmcts.reform.pip.data.management.utils.OAuthClient;
import uk.gov.hmcts.reform.pip.model.publication.ArtefactType;
import uk.gov.hmcts.reform.pip.model.publication.Language;
import uk.gov.hmcts.reform.pip.model.publication.ListType;
import uk.gov.hmcts.reform.pip.model.publication.Sensitivity;
import uk.gov.hmcts.reform.pip.model.subscription.SearchType;

import java.io.File;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static com.github.dockerjava.zerodep.shaded.org.apache.hc.core5.http.HttpHeaders.AUTHORIZATION;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.OK;
import static uk.gov.hmcts.reform.pip.data.management.utils.TestUtil.BEARER;
import static uk.gov.hmcts.reform.pip.data.management.utils.TestUtil.randomLocationId;

@ExtendWith(SpringExtension.class)
@ActiveProfiles(profiles = "functional")
@SpringBootTest(classes = {OAuthClient.class})
class PublicationTest extends FunctionalTestBase {

    @Value("${test-user-id}")
    private String userId;

    private static final String PUBLICATION_URL = "/publication";
    private static final String ARTEFACT_BY_LOCATION_ID_URL = PUBLICATION_URL + "/locationId/";
    private static final String ARTEFACT_BY_SEARCH_VALUE_URL = PUBLICATION_URL + "/search/";

    private static final String TESTING_SUPPORT_LOCATION_URL = "/testing-support/location/";
    private static final String TESTING_SUPPORT_PUBLICATION_URL = "/testing-support/publication/";
    private static final ArtefactType ARTEFACT_TYPE = ArtefactType.LIST;
    private static final Sensitivity SENSITIVITY = Sensitivity.PUBLIC;
    private static final String PROVENANCE = "MANUAL_UPLOAD";
    private static final String SOURCE_ARTEFACT_ID = "sourceArtefactId";
    private static final LocalDateTime DISPLAY_FROM = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
    private static final Language LANGUAGE = Language.ENGLISH;
    private static final ListType LIST_TYPE = ListType.CIVIL_DAILY_CAUSE_LIST;
    private static final String COURT_ID = randomLocationId();
    private static final String COURT_ID_UPLOAD_JSON = randomLocationId();
    private static final String USER_ID_HEADER = "x-user-id";
    private static final String ISSUER_HEADER = "x-issuer-id";
    private static final String COURT_NAME = "TestLocation" + COURT_ID;
    private static final String COURT_NAME_UPLOAD_JSON = COURT_NAME + "One";
    private static final String CASE_NUMBER = "4568454842";
    private static final String EMAIL = "test@hmcts.net";

    private static final LocalDateTime CONTENT_DATE = LocalDateTime.now().toLocalDate().atStartOfDay()
        .truncatedTo(ChronoUnit.SECONDS);

    @BeforeAll
    public void setup() {
        doPostRequest(
            TESTING_SUPPORT_LOCATION_URL + COURT_ID,
            Map.of(AUTHORIZATION, BEARER + accessToken), COURT_NAME
        );
    }

    @AfterAll
    public void teardown() {
        doDeleteRequest(TESTING_SUPPORT_PUBLICATION_URL + COURT_NAME, Map.of(AUTHORIZATION, BEARER + accessToken));
        doDeleteRequest(TESTING_SUPPORT_LOCATION_URL + COURT_NAME, Map.of(AUTHORIZATION, BEARER + accessToken));
    }

    @Test
    void testPublicationEndpointsWithJsonFileUpload() throws Exception {
        doPostRequest(
            TESTING_SUPPORT_LOCATION_URL + COURT_ID_UPLOAD_JSON,
            Map.of(AUTHORIZATION, BEARER + accessToken), COURT_NAME_UPLOAD_JSON
        );

        Map<String, String> headerMapUploadJsonFile = new ConcurrentHashMap<>();
        headerMapUploadJsonFile.put(AUTHORIZATION, BEARER + accessToken);
        headerMapUploadJsonFile.put(PublicationConfiguration.TYPE_HEADER, ARTEFACT_TYPE.toString());
        headerMapUploadJsonFile.put(PublicationConfiguration.PROVENANCE_HEADER, PROVENANCE);
        headerMapUploadJsonFile.put(PublicationConfiguration.SOURCE_ARTEFACT_ID_HEADER, SOURCE_ARTEFACT_ID);
        headerMapUploadJsonFile.put(PublicationConfiguration.DISPLAY_FROM_HEADER, DISPLAY_FROM.toString());
        headerMapUploadJsonFile.put(PublicationConfiguration.DISPLAY_TO_HEADER, DISPLAY_FROM.plusDays(1).toString());
        headerMapUploadJsonFile.put(PublicationConfiguration.COURT_ID, COURT_ID_UPLOAD_JSON);
        headerMapUploadJsonFile.put(PublicationConfiguration.LIST_TYPE, LIST_TYPE.toString());
        headerMapUploadJsonFile.put(PublicationConfiguration.CONTENT_DATE, CONTENT_DATE.toString());
        headerMapUploadJsonFile.put(PublicationConfiguration.SENSITIVITY_HEADER, SENSITIVITY.toString());
        headerMapUploadJsonFile.put(PublicationConfiguration.LANGUAGE_HEADER, LANGUAGE.toString());
        headerMapUploadJsonFile.put("Content-Type", "application/json");

        Map<String, String> headerMap = new ConcurrentHashMap<>();
        headerMap.put(AUTHORIZATION, BEARER + accessToken);
        headerMap.put(USER_ID_HEADER, userId);

        String artefactId;
        try (InputStream jsonFile = this.getClass().getClassLoader()
            .getResourceAsStream("data/civilDailyCauseList.json")) {
            final String jsonString = new String(jsonFile.readAllBytes(), StandardCharsets.UTF_8);

            final Response responseUploadJson = doPostRequest(
                PUBLICATION_URL,
                headerMapUploadJsonFile, jsonString
            );

            assertThat(responseUploadJson.getStatusCode()).isEqualTo(CREATED.value());
            Artefact returnedArtefact = responseUploadJson.as(Artefact.class);
            artefactId = returnedArtefact.getArtefactId().toString();
            assertThat(returnedArtefact.getContentDate()).isEqualTo(CONTENT_DATE);
            assertThat(returnedArtefact.getListType()).isEqualTo(LIST_TYPE);
            assertThat(returnedArtefact.toString()).contains(CASE_NUMBER, "A Vs B");

            final Response responseGetArtefactPayload = doGetRequest(
                PUBLICATION_URL + '/' + artefactId + "/payload", headerMap
            );
            assertThat(responseGetArtefactPayload.getStatusCode()).isEqualTo(OK.value());
            assertThat(responseGetArtefactPayload.asString()).isEqualTo(jsonString);
        }

        final Response responseGetAllRelevantArtefactsBySearchValue = doGetRequest(
            ARTEFACT_BY_SEARCH_VALUE_URL + SearchType.CASE_ID + '/' + CASE_NUMBER, headerMap
        );
        assertThat(responseGetAllRelevantArtefactsBySearchValue.getStatusCode()).isEqualTo(OK.value());
        Artefact[] returnedGetAllRelevantArtefactsBySearchValue = responseGetAllRelevantArtefactsBySearchValue.as(
            Artefact[].class);
        assertThat(returnedGetAllRelevantArtefactsBySearchValue[0].getArtefactId().toString()).isEqualTo(artefactId);

        final Response responseGetArtefactMetadata = doGetRequest(
            PUBLICATION_URL + '/' + artefactId, headerMap
        );
        assertThat(responseGetArtefactMetadata.getStatusCode()).isEqualTo(OK.value());
        Artefact returnedGetArtefactMetadata = responseGetArtefactMetadata.as(Artefact.class);
        assertThat(returnedGetArtefactMetadata.getArtefactId().toString()).isEqualTo(artefactId);

        Map<String, String> deleteArtefactHeaderMap = new ConcurrentHashMap<>();
        deleteArtefactHeaderMap.put(AUTHORIZATION, BEARER + accessToken);
        deleteArtefactHeaderMap.put(ISSUER_HEADER, EMAIL);

        final Response responseGetAllRelevantArtefactsByLocationId = doGetRequest(
            ARTEFACT_BY_LOCATION_ID_URL + COURT_ID_UPLOAD_JSON, headerMap
        );
        assertThat(responseGetAllRelevantArtefactsByLocationId.getStatusCode()).isEqualTo(OK.value());
        Artefact[] returnedGetAllRelevantArtefacts = responseGetAllRelevantArtefactsByLocationId.as(Artefact[].class);

        assertThat(returnedGetAllRelevantArtefacts.length).isEqualTo(1);
        assertThat(returnedGetAllRelevantArtefacts[0].getLocationId()).isEqualTo(COURT_ID_UPLOAD_JSON);

        final Response responseDeleteArtefact = doDeleteRequest(
            PUBLICATION_URL + '/' + artefactId, deleteArtefactHeaderMap
        );
        assertThat(responseDeleteArtefact.getStatusCode()).isEqualTo(OK.value());
        assertThat(responseDeleteArtefact.asString()).isEqualTo("Successfully deleted artefact: "
                                                                    + artefactId);
    }

    @Test
    void testPublicationEndpointsWithFlatFileUpload() throws Exception {
        Map<String, String> headerMapUploadFlatFile = new ConcurrentHashMap<>();
        headerMapUploadFlatFile.put(AUTHORIZATION, BEARER + accessToken);
        headerMapUploadFlatFile.put(PublicationConfiguration.TYPE_HEADER, ARTEFACT_TYPE.toString());
        headerMapUploadFlatFile.put(PublicationConfiguration.PROVENANCE_HEADER, PROVENANCE);
        headerMapUploadFlatFile.put(PublicationConfiguration.SOURCE_ARTEFACT_ID_HEADER, SOURCE_ARTEFACT_ID);
        headerMapUploadFlatFile.put(PublicationConfiguration.DISPLAY_FROM_HEADER, DISPLAY_FROM.toString());
        headerMapUploadFlatFile.put(PublicationConfiguration.DISPLAY_TO_HEADER, DISPLAY_FROM.plusDays(1).toString());
        headerMapUploadFlatFile.put(PublicationConfiguration.COURT_ID, COURT_ID);
        headerMapUploadFlatFile.put(PublicationConfiguration.LIST_TYPE, ListType.ET_DAILY_LIST.toString());
        headerMapUploadFlatFile.put(PublicationConfiguration.CONTENT_DATE, CONTENT_DATE.toString());
        headerMapUploadFlatFile.put(PublicationConfiguration.SENSITIVITY_HEADER, SENSITIVITY.toString());
        headerMapUploadFlatFile.put(PublicationConfiguration.LANGUAGE_HEADER, LANGUAGE.toString());
        headerMapUploadFlatFile.put("Content-Type", MediaType.MULTIPART_FORM_DATA_VALUE);

        String filePath = this.getClass().getClassLoader().getResource("data/testFlatFile.pdf").getPath();
        File pdfFile = new File(filePath);

        final Response responseUploadFlatFile = doPostRequestMultiPart(
            PUBLICATION_URL,
            headerMapUploadFlatFile, "", pdfFile
        );
        assertThat(responseUploadFlatFile.getStatusCode()).isEqualTo(CREATED.value());
        Artefact returnedFlatFileArtefact = responseUploadFlatFile.as(Artefact.class);
        assertThat(returnedFlatFileArtefact.getContentDate()).isEqualTo(CONTENT_DATE);
        assertThat(returnedFlatFileArtefact.getListType()).isEqualTo(ListType.ET_DAILY_LIST);
        assertThat(returnedFlatFileArtefact.getLocationId()).contains(COURT_ID);

        String artefactId = returnedFlatFileArtefact.getArtefactId().toString();
        Map<String, String> headerMap = new ConcurrentHashMap<>();
        headerMap.put(AUTHORIZATION, BEARER + accessToken);
        headerMap.put(USER_ID_HEADER, userId);

        try (InputStream pdfTestFile = this.getClass().getClassLoader()
            .getResourceAsStream("data/testFlatFile.pdf")) {
            final String pdfFileContent = new String(pdfTestFile.readAllBytes(), StandardCharsets.UTF_8);

            final Response responseGetArtefactFile = doGetRequest(
                PUBLICATION_URL + '/' + artefactId + "/file", headerMap
            );
            assertThat(responseGetArtefactFile.getStatusCode()).isEqualTo(OK.value());
            assertThat(responseGetArtefactFile.asString()).isEqualTo(pdfFileContent);
        }

        final Response responseGetArtefactMetadata = doGetRequest(
            PUBLICATION_URL + '/' + artefactId, headerMap
        );
        assertThat(responseGetArtefactMetadata.getStatusCode()).isEqualTo(OK.value());
        Artefact returnedGetArtefactMetadata = responseGetArtefactMetadata.as(Artefact.class);
        assertThat(returnedGetArtefactMetadata.getArtefactId().toString()).isEqualTo(artefactId);

        Map<String, String> deleteArtefactHeaderMap = new ConcurrentHashMap<>();
        deleteArtefactHeaderMap.put(AUTHORIZATION, BEARER + accessToken);
        deleteArtefactHeaderMap.put(ISSUER_HEADER, EMAIL);

        final Response responseDeleteArtefact = doDeleteRequest(
            PUBLICATION_URL + '/' + artefactId, deleteArtefactHeaderMap
        );
        assertThat(responseDeleteArtefact.getStatusCode()).isEqualTo(OK.value());
        assertThat(responseDeleteArtefact.asString()).isEqualTo("Successfully deleted artefact: "
                                                                    + artefactId);
    }
}
