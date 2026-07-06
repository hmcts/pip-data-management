package uk.gov.hmcts.reform.pip.data.management.publication;

import io.restassured.response.Response;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.pip.data.management.models.publication.ListSearchConfig;
import uk.gov.hmcts.reform.pip.data.management.utils.FunctionalTestBase;
import uk.gov.hmcts.reform.pip.data.management.utils.OAuthClient;
import uk.gov.hmcts.reform.pip.model.publication.ListType;

import java.util.Map;
import java.util.UUID;

import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.OK;

@ActiveProfiles(profiles = "functional")
@SpringBootTest(classes = {OAuthClient.class})
class PublicationSearchTest extends FunctionalTestBase {

    private static final String SEARCH_CONFIG_URL = "/publication/search/config";
    private static final String REQUESTER_ID_HEADER = "x-requester-id";
    private static final String CASE_NAME = "caseName";
    private static final String CASE_NUMBER = "caseNumber";

    @Value("${test-system-admin-id}")
    private String systemAdminUserId;


    @Test
    void testEndToEndListSearchConfig() {
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

            String originalCaseNumber = config.getCaseNumberFieldName();
            String originalCaseName = config.getCaseNameFieldName();

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
}
