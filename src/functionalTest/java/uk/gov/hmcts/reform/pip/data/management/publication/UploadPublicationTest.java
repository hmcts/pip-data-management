package uk.gov.hmcts.reform.pip.data.management.publication;

import io.restassured.response.Response;
import io.zonky.test.db.AutoConfigureEmbeddedDatabase;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.pip.data.management.Application;
import uk.gov.hmcts.reform.pip.data.management.config.PublicationConfiguration;
import uk.gov.hmcts.reform.pip.data.management.utils.FunctionalTestBase;
import uk.gov.hmcts.reform.pip.data.management.utils.OAuthClient;
import uk.gov.hmcts.reform.pip.model.publication.ArtefactType;
import uk.gov.hmcts.reform.pip.model.publication.Language;
import uk.gov.hmcts.reform.pip.model.publication.ListType;
import uk.gov.hmcts.reform.pip.model.publication.Sensitivity;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static com.github.dockerjava.zerodep.shaded.org.apache.hc.core5.http.HttpHeaders.AUTHORIZATION;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.springframework.http.HttpStatus.CREATED;
import static uk.gov.hmcts.reform.pip.data.management.utils.TestUtil.BEARER;
import static uk.gov.hmcts.reform.pip.data.management.utils.TestUtil.randomLocationId;

@ExtendWith(SpringExtension.class)
@ActiveProfiles(profiles = "functional")
@AutoConfigureEmbeddedDatabase(type = AutoConfigureEmbeddedDatabase.DatabaseType.POSTGRES)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(classes = {Application.class, OAuthClient.class},
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class UploadPublicationTest extends FunctionalTestBase {

    private static final String UPLOAD_PUBLICATION_URL = "/publication";
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
    private static final String COURT_NAME = "TestLocation" + COURT_ID;

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
        doDeleteRequest(TESTING_SUPPORT_PUBLICATION_URL + COURT_NAME, Map.of(AUTHORIZATION, BEARER + accessToken), "");
        doDeleteRequest(TESTING_SUPPORT_LOCATION_URL + COURT_NAME, Map.of(AUTHORIZATION, BEARER + accessToken), "");
    }

    @Test
    void shouldAbleToUploadThePublication() throws Exception {

        Map<String, String> headerMap = new ConcurrentHashMap<>();
        headerMap.put(AUTHORIZATION, BEARER + accessToken);
        headerMap.put(PublicationConfiguration.TYPE_HEADER, ARTEFACT_TYPE.toString());
        headerMap.put(PublicationConfiguration.PROVENANCE_HEADER, PROVENANCE);
        headerMap.put(PublicationConfiguration.SOURCE_ARTEFACT_ID_HEADER, SOURCE_ARTEFACT_ID);
        headerMap.put(PublicationConfiguration.DISPLAY_FROM_HEADER, DISPLAY_FROM.toString());
        headerMap.put(PublicationConfiguration.DISPLAY_TO_HEADER, DISPLAY_FROM.plusDays(1).toString());
        headerMap.put(PublicationConfiguration.COURT_ID, COURT_ID);
        headerMap.put(PublicationConfiguration.LIST_TYPE, LIST_TYPE.toString());
        headerMap.put(PublicationConfiguration.CONTENT_DATE, CONTENT_DATE.toString());
        headerMap.put(PublicationConfiguration.SENSITIVITY_HEADER, SENSITIVITY.toString());
        headerMap.put(PublicationConfiguration.LANGUAGE_HEADER, LANGUAGE.toString());
        headerMap.put("Content-Type", "application/json");

        try (InputStream jsonFile = this.getClass().getClassLoader()
            .getResourceAsStream("data/civilDailyCauseList.json")) {
            final String jsonString = new String(jsonFile.readAllBytes(), StandardCharsets.UTF_8);

            final Response response = doPostRequest(
                UPLOAD_PUBLICATION_URL,
                headerMap, jsonString
            );
            assertThat(response.getStatusCode()).isEqualTo(CREATED.value());

        }
    }
}
