package uk.gov.hmcts.reform.pip.data.management;

import io.restassured.response.Response;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.pip.data.management.utils.OAuthClient;
import uk.gov.hmcts.reform.pip.data.management.utils.SmokeTestBase;
import uk.gov.hmcts.reform.pip.model.publication.ArtefactType;
import uk.gov.hmcts.reform.pip.model.publication.Language;
import uk.gov.hmcts.reform.pip.model.publication.ListType;
import uk.gov.hmcts.reform.pip.model.publication.Sensitivity;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.OK;
import static uk.gov.hmcts.reform.pip.data.management.config.PublicationConfiguration.CONTENT_DATE;
import static uk.gov.hmcts.reform.pip.data.management.config.PublicationConfiguration.COURT_ID;
import static uk.gov.hmcts.reform.pip.data.management.config.PublicationConfiguration.DISPLAY_FROM_HEADER;
import static uk.gov.hmcts.reform.pip.data.management.config.PublicationConfiguration.DISPLAY_TO_HEADER;
import static uk.gov.hmcts.reform.pip.data.management.config.PublicationConfiguration.LANGUAGE_HEADER;
import static uk.gov.hmcts.reform.pip.data.management.config.PublicationConfiguration.LIST_TYPE;
import static uk.gov.hmcts.reform.pip.data.management.config.PublicationConfiguration.PROVENANCE_HEADER;
import static uk.gov.hmcts.reform.pip.data.management.config.PublicationConfiguration.REQUESTER_ID;
import static uk.gov.hmcts.reform.pip.data.management.config.PublicationConfiguration.SENSITIVITY_HEADER;
import static uk.gov.hmcts.reform.pip.data.management.config.PublicationConfiguration.TYPE_HEADER;

@SpringBootTest(classes = {OAuthClient.class})
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ActiveProfiles("smoke")
class SmokeTest extends SmokeTestBase {
    private static final String PUBLICATION_URL = "/publication";
    private static final String TESTING_SUPPORT_BASE_URL = "/testing-support";
    private static final String TESTING_SUPPORT_LOCATION_URL = TESTING_SUPPORT_BASE_URL + "/location/";
    private static final String TESTING_SUPPORT_PUBLICATION_URL = TESTING_SUPPORT_BASE_URL + "/publication/";

    private static final String BASE_LOCATION_NAME = "SmokeTestLocation-";
    private static final String STATUS_CODE_MATCH = "Status code does not match";
    private static final String RESPONSE_BODY_MATCH = "Response body does not match";

    private String locationId;
    private String locationName;

    @Value("${test-system-admin-id}")
    private String systemAdminUserId;

    @BeforeAll
    void startup() {
        Integer randomNumber = 10_000 + new Random().nextInt();
        locationId = randomNumber.toString();
        locationName = BASE_LOCATION_NAME + locationId;

        doPostRequest(TESTING_SUPPORT_LOCATION_URL + locationId, Collections.emptyMap(), locationName);
    }

    @AfterAll
    void teardown() {
        doDeleteRequest(TESTING_SUPPORT_PUBLICATION_URL + locationName);
        doDeleteRequest(TESTING_SUPPORT_LOCATION_URL + locationName);
    }

    @Test
    void testHealthCheck() {
        Response response = doGetRequest("");

        assertThat(response.statusCode())
            .as(STATUS_CODE_MATCH)
            .isEqualTo(OK.value());

        assertThat(response.body().asString())
            .as(RESPONSE_BODY_MATCH)
            .isEqualTo("Welcome to pip-data-management");
    }

    @Test
    void testUploadPublication() throws IOException {
        String jsonData;
        try (InputStream jsonFile = this.getClass().getClassLoader()
            .getResourceAsStream("civilDailyCauseList.json")) {
            jsonData = new String(jsonFile.readAllBytes(), StandardCharsets.UTF_8);
        }

        Map<String, String> headerMapUploadJsonFile = new ConcurrentHashMap<>();
        headerMapUploadJsonFile.put(TYPE_HEADER, ArtefactType.LIST.toString());
        headerMapUploadJsonFile.put(PROVENANCE_HEADER, "MANUAL_UPLOAD");
        headerMapUploadJsonFile.put(DISPLAY_FROM_HEADER, LocalDateTime.now().toString());
        headerMapUploadJsonFile.put(DISPLAY_TO_HEADER, LocalDateTime.now().plusDays(1).toString());
        headerMapUploadJsonFile.put(COURT_ID, locationId);
        headerMapUploadJsonFile.put(LIST_TYPE, ListType.CIVIL_DAILY_CAUSE_LIST.toString());
        headerMapUploadJsonFile.put(CONTENT_DATE, LocalDateTime.now().toString());
        headerMapUploadJsonFile.put(SENSITIVITY_HEADER, Sensitivity.PUBLIC.toString());
        headerMapUploadJsonFile.put(LANGUAGE_HEADER, Language.ENGLISH.toString());
        headerMapUploadJsonFile.put(REQUESTER_ID, systemAdminUserId);

        Response response = doPostRequest(PUBLICATION_URL, headerMapUploadJsonFile, jsonData);

        assertThat(response.statusCode())
            .as(STATUS_CODE_MATCH)
            .isEqualTo(CREATED.value());
    }
}
