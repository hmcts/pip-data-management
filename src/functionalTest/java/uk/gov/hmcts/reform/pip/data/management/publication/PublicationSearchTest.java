package uk.gov.hmcts.reform.pip.data.management.publication;

import io.restassured.response.Response;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.pip.data.management.config.PublicationConfiguration;
import uk.gov.hmcts.reform.pip.data.management.models.publication.Artefact;
import uk.gov.hmcts.reform.pip.data.management.models.publication.ListSearchConfig;
import uk.gov.hmcts.reform.pip.data.management.utils.FunctionalTestBase;
import uk.gov.hmcts.reform.pip.data.management.utils.OAuthClient;
import uk.gov.hmcts.reform.pip.model.publication.ArtefactCaseInfo;
import uk.gov.hmcts.reform.pip.model.publication.ArtefactType;
import uk.gov.hmcts.reform.pip.model.publication.Language;
import uk.gov.hmcts.reform.pip.model.publication.ListType;
import uk.gov.hmcts.reform.pip.model.publication.Sensitivity;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static com.github.dockerjava.zerodep.shaded.org.apache.hc.core5.http.HttpHeaders.AUTHORIZATION;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.OK;
import static uk.gov.hmcts.reform.pip.data.management.utils.TestUtil.BEARER;
import static uk.gov.hmcts.reform.pip.data.management.utils.TestUtil.randomLocationId;

@ActiveProfiles(profiles = "functional")
@SpringBootTest(classes = {OAuthClient.class})
class PublicationSearchTest extends FunctionalTestBase {

    @Value("${test-system-admin-id}")
    private String systemAdminUserId;

    @Value("${b2c-test-account-id}")
    public String verifiedUserId;

    private static final String SEARCH_CONFIG_URL = "/publication/search/config";
    private static final String SEARCH_BY_CASENUMBER_URL = "/publication/search/caseNumber";
    private static final String SEARCH_BY_CASENAME_URL = "/publication/search/caseName";
    private static final String PUBLICATION_URL = "/publication";
    private static final String TESTING_SUPPORT_LOCATION_URL = "/testing-support/location/";
    private static final String TESTING_SUPPORT_PUBLICATION_URL = "/testing-support/publication/";

    private static final String REQUESTER_ID_HEADER = "x-requester-id";
    private static final String CASE_NAME = "caseName";
    private static final String CASE_NUMBER = "caseNumber";
    private static final ArtefactType ARTEFACT_TYPE = ArtefactType.LIST;
    private static final ArtefactType ARTEFACT_TYPE_LCSU = ArtefactType.LCSU;
    private static final String PROVENANCE = "MANUAL_UPLOAD";
    private static final String SOURCE_ARTEFACT_ID = "sourceArtefactId";
    private static final LocalDateTime DISPLAY_FROM = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
    private static final Language LANGUAGE = Language.ENGLISH;
    private static final ListType LIST_TYPE = ListType.CIVIL_DAILY_CAUSE_LIST;
    private static final String BASE_COURT_NAME = "TestLocation-PublicationTest";
    private static final String SEARCH_VALUE_PARAM = "searchValue";
    private static final String TEST_CASE_NUMBER = "12345678";
    private static final String TEST_CASE_NAME = "A Vs B";
    private static final LocalDateTime CONTENT_DATE = LocalDateTime.now().toLocalDate().atStartOfDay()
            .truncatedTo(ChronoUnit.SECONDS);

    private String courtId;

    @BeforeEach
    public void setupLocation() {
        courtId = randomLocationId();

        doPostRequest(
                TESTING_SUPPORT_LOCATION_URL + courtId,
                Map.of(AUTHORIZATION, BEARER + accessToken), BASE_COURT_NAME + "-" + courtId
        );
    }

    @AfterAll
    public void teardown() {
        doDeleteRequest(TESTING_SUPPORT_PUBLICATION_URL + BASE_COURT_NAME, getBaseHeaderMap());
        doDeleteRequest(TESTING_SUPPORT_LOCATION_URL + BASE_COURT_NAME, getBaseHeaderMap());
    }

    private String getJsonString() throws IOException {
        try (InputStream jsonFile = this.getClass().getClassLoader()
                .getResourceAsStream("data/civilDailyCauseList.json")) {
            return new String(jsonFile.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    private Artefact uploadArtefact(String jsonString, String courtId, Sensitivity sensitivity,
                                    String provenance) throws IOException {
        Map<String, String> headerMapUploadJsonFile = getBaseHeaderMap();
        headerMapUploadJsonFile.put(PublicationConfiguration.TYPE_HEADER, ARTEFACT_TYPE.toString());
        headerMapUploadJsonFile.put(PublicationConfiguration.PROVENANCE_HEADER, provenance);
        headerMapUploadJsonFile.put(PublicationConfiguration.SOURCE_ARTEFACT_ID_HEADER, SOURCE_ARTEFACT_ID);
        headerMapUploadJsonFile.put(PublicationConfiguration.DISPLAY_FROM_HEADER, DISPLAY_FROM.toString());
        headerMapUploadJsonFile.put(PublicationConfiguration.DISPLAY_TO_HEADER, DISPLAY_FROM.plusDays(1).toString());
        headerMapUploadJsonFile.put(PublicationConfiguration.COURT_ID, courtId);
        headerMapUploadJsonFile.put(PublicationConfiguration.LIST_TYPE, LIST_TYPE.toString());
        headerMapUploadJsonFile.put(PublicationConfiguration.CONTENT_DATE, CONTENT_DATE.toString());
        headerMapUploadJsonFile.put(PublicationConfiguration.SENSITIVITY_HEADER, sensitivity.toString());
        headerMapUploadJsonFile.put(PublicationConfiguration.LANGUAGE_HEADER, LANGUAGE.toString());
        headerMapUploadJsonFile.put(PublicationConfiguration.REQUESTER_ID_HEADER, systemAdminUserId);
        headerMapUploadJsonFile.put("Content-Type", "application/json");

        final Response responseUploadJson = doPostRequest(
                PUBLICATION_URL,
                headerMapUploadJsonFile, jsonString
        );

        assertThat(responseUploadJson.getStatusCode()).isEqualTo(CREATED.value());
        return responseUploadJson.as(Artefact.class);

    }

    @Test
    void testEndToEndListSearchConfig() throws Exception {
        Map<String, String> headerMap = getBaseHeaderMap();
        headerMap.put(REQUESTER_ID_HEADER, systemAdminUserId);

        Response response = doGetRequest(
            SEARCH_CONFIG_URL + "/" + ListType.CIVIL_DAILY_CAUSE_LIST,
            headerMap
        );

        if (response.getStatusCode() == NOT_FOUND.value()) {

            SoftAssertions softly = new SoftAssertions();

            ListSearchConfig createConfig = ListSearchConfig.builder()
                .listType(ListType.CIVIL_DAILY_CAUSE_LIST)
                .caseNumberFieldName(CASE_NUMBER)
                .caseNameFieldName(CASE_NAME)
                .build();

            Response responseCreateRequest = doPostRequest(
                SEARCH_CONFIG_URL,
                headerMap,
                createConfig
            );

            softly.assertThat(responseCreateRequest.getStatusCode()).isEqualTo(CREATED.value());

            UUID configId = responseCreateRequest.as(UUID.class);

            softly.assertThat(configId).isNotNull();

            ListSearchConfig updatedConfig = ListSearchConfig.builder()
                .id(configId)
                .listType(ListType.CIVIL_DAILY_CAUSE_LIST)
                .caseNumberFieldName(CASE_NUMBER)
                .caseNameFieldName(CASE_NAME)
                .build();

            Response updateResponse = doPutRequest(
                SEARCH_CONFIG_URL + "/" + configId,
                headerMap,
                updatedConfig
            );

            softly.assertThat(updateResponse.getStatusCode()).isEqualTo(OK.value());

            UUID returnedId = updateResponse.as(UUID.class);

            softly.assertThat(returnedId).isNotNull();

            Response getResponse = doGetRequest(
                SEARCH_CONFIG_URL + "/" + ListType.CIVIL_DAILY_CAUSE_LIST,
                headerMap
            );

            ListSearchConfig returnedConfig = getResponse.as(ListSearchConfig.class);

            softly.assertThat(returnedConfig.getCaseNumberFieldName())
                .isEqualTo(CASE_NUMBER);
            softly.assertThat(returnedConfig.getCaseNameFieldName())
                .isEqualTo(CASE_NAME);


            Artefact returnedArtefact = uploadArtefact(getJsonString(), courtId, Sensitivity.PUBLIC, PROVENANCE);
            softly.assertThat(returnedArtefact).isNotNull();

            Map<String, String> headerMapArtefactSearch = getBaseHeaderMap();
            headerMapArtefactSearch.put(REQUESTER_ID_HEADER, verifiedUserId);

            Response getByCaseNameResponse = doGetRequest(
                    SEARCH_BY_CASENAME_URL,
                    headerMapArtefactSearch, Map.of(SEARCH_VALUE_PARAM, TEST_CASE_NAME)
            );
            softly.assertThat(getByCaseNameResponse.getStatusCode()).isEqualTo(OK.value());
            List<ArtefactCaseInfo> caseInfoName = getByCaseNameResponse.jsonPath()
                    .getList(".", ArtefactCaseInfo.class);
            softly.assertThat(caseInfoName.get(0).getCaseName()).isEqualTo(TEST_CASE_NAME);

            Response getByCaseNumberResponse = doGetRequest(
                    SEARCH_BY_CASENUMBER_URL,
                    headerMapArtefactSearch,Map.of(SEARCH_VALUE_PARAM, TEST_CASE_NUMBER)
            );
            softly.assertThat(getByCaseNumberResponse.getStatusCode()).isEqualTo(OK.value());
            List<ArtefactCaseInfo> caseInfoNumber = getByCaseNumberResponse.jsonPath()
                    .getList(".", ArtefactCaseInfo.class);
            softly.assertThat(caseInfoNumber.get(0).getCaseNumber()).isEqualTo(TEST_CASE_NUMBER);


            Response deleteResponse = doDeleteRequest(
                SEARCH_CONFIG_URL + "/" + returnedId,
                headerMap
            );

            softly.assertThat(deleteResponse.getStatusCode()).isEqualTo(OK.value());

            Response getResponseAfterDelete = doGetRequest(
                SEARCH_CONFIG_URL + "/" + ListType.CIVIL_DAILY_CAUSE_LIST,
                headerMap
            );

            softly.assertThat(getResponseAfterDelete.getStatusCode()).isEqualTo(NOT_FOUND.value());

            softly.assertAll();
        } else {

            SoftAssertions softly = new SoftAssertions();
            softly.assertThat(response.getStatusCode()).isEqualTo(OK.value());
            ListSearchConfig config = response.as(ListSearchConfig.class);

            softly.assertThat(config).isNotNull();

            softly.assertThat(config.getListType()).isEqualTo(ListType.CIVIL_DAILY_CAUSE_LIST);

            final String originalCaseNumber = config.getCaseNumberFieldName();
            final String originalCaseName = config.getCaseNameFieldName();

            ListSearchConfig updatedConfig = ListSearchConfig.builder()
                .id(config.getId())
                .listType(ListType.CIVIL_DAILY_CAUSE_LIST)
                .caseNumberFieldName(CASE_NUMBER)
                .caseNameFieldName(CASE_NAME)
                .build();

            Response updateResponse = doPutRequest(
                SEARCH_CONFIG_URL + "/" + config.getId(),
                headerMap,
                updatedConfig
            );

            softly.assertThat(updateResponse.getStatusCode()).isEqualTo(OK.value());

            UUID returnedId = updateResponse.as(UUID.class);

            softly.assertThat(returnedId).isNotNull();

            Response getResponse = doGetRequest(
                SEARCH_CONFIG_URL + "/" + ListType.CIVIL_DAILY_CAUSE_LIST,
                headerMap
            );

            ListSearchConfig returnedConfig = getResponse.as(ListSearchConfig.class);

            softly.assertThat(returnedConfig.getCaseNumberFieldName())
                .isEqualTo(CASE_NUMBER);
            softly.assertThat(returnedConfig.getCaseNameFieldName())
                .isEqualTo(CASE_NAME);

            Artefact returnedArtefact = uploadArtefact(getJsonString(), courtId, Sensitivity.PUBLIC, PROVENANCE);
            softly.assertThat(returnedArtefact).isNotNull();

            Map<String, String> headerMapArtefactSearch = getBaseHeaderMap();
            headerMapArtefactSearch.put(REQUESTER_ID_HEADER, verifiedUserId);

            Response getByCaseNameResponse = doGetRequest(
                    SEARCH_BY_CASENAME_URL,
                    headerMapArtefactSearch, Map.of(SEARCH_VALUE_PARAM, TEST_CASE_NAME)
            );
            softly.assertThat(getByCaseNameResponse.getStatusCode()).isEqualTo(OK.value());
            List<ArtefactCaseInfo> caseInfoName = getByCaseNameResponse.jsonPath()
                    .getList(".", ArtefactCaseInfo.class);
            softly.assertThat(caseInfoName.get(0).getCaseName()).isEqualTo(TEST_CASE_NAME);

            Response getByCaseNumberResponse = doGetRequest(
                    SEARCH_BY_CASENUMBER_URL,
                    headerMapArtefactSearch,Map.of(SEARCH_VALUE_PARAM, TEST_CASE_NUMBER)
            );
            softly.assertThat(getByCaseNumberResponse.getStatusCode()).isEqualTo(OK.value());
            List<ArtefactCaseInfo> caseInfoNumber = getByCaseNumberResponse.jsonPath()
                    .getList(".", ArtefactCaseInfo.class);
            softly.assertThat(caseInfoNumber.get(0).getCaseNumber()).isEqualTo(TEST_CASE_NUMBER);

            Response deleteResponse = doDeleteRequest(
                SEARCH_CONFIG_URL + "/" + returnedId,
                headerMap
            );

            softly.assertThat(deleteResponse.getStatusCode()).isEqualTo(OK.value());

            Response getResponseAfterDelete = doGetRequest(
                SEARCH_CONFIG_URL + "/" + ListType.CIVIL_DAILY_CAUSE_LIST,
                headerMap
            );

            softly.assertThat(getResponseAfterDelete.getStatusCode()).isEqualTo(NOT_FOUND.value());

            ListSearchConfig createConfig = ListSearchConfig.builder()
                .listType(ListType.CIVIL_DAILY_CAUSE_LIST)
                .caseNumberFieldName(originalCaseNumber)
                .caseNameFieldName(originalCaseName)
                .build();

            Response responseCreateRequest = doPostRequest(
                SEARCH_CONFIG_URL,
                headerMap,
                createConfig
            );

            softly.assertThat(responseCreateRequest.getStatusCode()).isEqualTo(CREATED.value());

            softly.assertAll();

        }
    }

    @Test
    void shouldReturnForbiddenSearchByCaseNameIfUserDoesNotHavePermission() throws Exception {
        Map<String, String> headerMapArtefactSearch = getBaseHeaderMap();
        headerMapArtefactSearch.put(REQUESTER_ID_HEADER, systemAdminUserId);

        Response getByCaseNameResponse = doGetRequest(
                SEARCH_BY_CASENAME_URL,
                headerMapArtefactSearch, Map.of(SEARCH_VALUE_PARAM, TEST_CASE_NAME)
        );
        assertThat(getByCaseNameResponse.getStatusCode()).isEqualTo(FORBIDDEN.value());
    }

    @Test
    void shouldReturnForbiddenSearchByCaseNumberIfUserDoesNotHavePermission() throws Exception {
        Map<String, String> headerMapArtefactSearch = getBaseHeaderMap();
        headerMapArtefactSearch.put(REQUESTER_ID_HEADER, systemAdminUserId);

        Response getByCaseNumberResponse = doGetRequest(
                SEARCH_BY_CASENUMBER_URL,
                headerMapArtefactSearch, Map.of(SEARCH_VALUE_PARAM, TEST_CASE_NAME)
        );
        assertThat(getByCaseNumberResponse.getStatusCode()).isEqualTo(FORBIDDEN.value());
    }
}
