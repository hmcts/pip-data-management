package uk.gov.hmcts.reform.pip.data.management.publication;

import com.jayway.jsonpath.JsonPath;
import io.restassured.response.Response;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.pip.data.management.config.PublicationConfiguration;
import uk.gov.hmcts.reform.pip.data.management.models.publication.Artefact;
import uk.gov.hmcts.reform.pip.data.management.utils.FunctionalTestBase;
import uk.gov.hmcts.reform.pip.data.management.utils.OAuthClient;
import uk.gov.hmcts.reform.pip.model.publication.ArtefactType;
import uk.gov.hmcts.reform.pip.model.publication.Language;
import uk.gov.hmcts.reform.pip.model.publication.ListType;
import uk.gov.hmcts.reform.pip.model.publication.Sensitivity;
import uk.gov.hmcts.reform.pip.model.report.PublicationMiData;
import uk.gov.hmcts.reform.pip.model.subscription.SearchType;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

import static com.github.dockerjava.zerodep.shaded.org.apache.hc.core5.http.HttpHeaders.AUTHORIZATION;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.OK;
import static uk.gov.hmcts.reform.pip.data.management.utils.TestUtil.BEARER;
import static uk.gov.hmcts.reform.pip.data.management.utils.TestUtil.randomLocationId;

@ExtendWith(SpringExtension.class)
@ActiveProfiles(profiles = "functional")
@SpringBootTest(classes = {OAuthClient.class})
@SuppressWarnings({"PMD.ExcessiveImports"})
class PublicationTest extends FunctionalTestBase {

    @Value("${test-user-id}")
    private String userId;

    private static final String PUBLICATION_URL = "/publication";
    private static final String NON_STRATEGIC_UPLOAD_PUBLICATION_URL = PUBLICATION_URL + "/non-strategic";
    private static final String ARTEFACT_BY_LOCATION_ID_URL = PUBLICATION_URL + "/locationId/";
    private static final String ARTEFACT_BY_SEARCH_VALUE_URL = PUBLICATION_URL + "/search/";
    private static final String DELETE_ARTEFACTS_BY_LOCATION_ID = PUBLICATION_URL + "/%s/deleteArtefacts";
    private static final String MI_DATA_URL = PUBLICATION_URL + "/v2/mi-data";


    private static final String TESTING_SUPPORT_LOCATION_URL = "/testing-support/location/";
    private static final String TESTING_SUPPORT_PUBLICATION_URL = "/testing-support/publication/";

    private static final ArtefactType ARTEFACT_TYPE = ArtefactType.LIST;
    private static final String PROVENANCE = "MANUAL_UPLOAD";
    private static final String SOURCE_ARTEFACT_ID = "sourceArtefactId";
    private static final LocalDateTime DISPLAY_FROM = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
    private static final Language LANGUAGE = Language.ENGLISH;
    private static final ListType LIST_TYPE = ListType.CIVIL_DAILY_CAUSE_LIST;
    private static final String USER_ID_HEADER = "x-user-id";
    private static final String ISSUER_HEADER = "x-issuer-id";
    private static final String BASE_COURT_NAME = "TestLocation-PublicationTest";
    private static final String CASE_NUMBER = "4568454842";
    private static final String EMAIL = "test@hmcts.net";

    private static final LocalDateTime CONTENT_DATE = LocalDateTime.now().toLocalDate().atStartOfDay()
        .truncatedTo(ChronoUnit.SECONDS);

    private String courtId;
    private String noMatchArtefactId = "";

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

        Map<String, String> deleteArtefactHeaderMap = getBaseHeaderMap();
        deleteArtefactHeaderMap.put(ISSUER_HEADER, EMAIL);

        if (!noMatchArtefactId.isBlank()) {
            doDeleteRequest(
                PUBLICATION_URL + '/' + noMatchArtefactId, deleteArtefactHeaderMap
            );
        }
    }

    private String getJsonString() throws IOException {
        try (InputStream jsonFile = this.getClass().getClassLoader()
            .getResourceAsStream("data/civilDailyCauseList.json")) {
            return new String(jsonFile.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    private String getJsonString(String caseNumber) throws IOException {
        try (InputStream jsonFile = this.getClass().getClassLoader()
            .getResourceAsStream("data/civilDailyCauseList.json")) {
            String jsonString = new String(jsonFile.readAllBytes(), StandardCharsets.UTF_8);

            return JsonPath.parse(jsonString).set("$.courtLists[0].courtHouse"
                                                      + ".courtRoom[0].session[0].sittings[0]"
                                                      + ".hearing[0].case[0].caseNumber", caseNumber).jsonString();
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
        headerMapUploadJsonFile.put("Content-Type", "application/json");

        final Response responseUploadJson = doPostRequest(
            PUBLICATION_URL,
            headerMapUploadJsonFile, jsonString
        );

        assertThat(responseUploadJson.getStatusCode()).isEqualTo(CREATED.value());
        return responseUploadJson.as(Artefact.class);

    }

    private Artefact uploadFlatFile(String courtId, Sensitivity sensitivity) {
        Map<String, String> headerMapUploadFlatFile = getBaseHeaderMap();
        headerMapUploadFlatFile.put(PublicationConfiguration.TYPE_HEADER, ARTEFACT_TYPE.toString());
        headerMapUploadFlatFile.put(PublicationConfiguration.PROVENANCE_HEADER, PROVENANCE);
        headerMapUploadFlatFile.put(PublicationConfiguration.SOURCE_ARTEFACT_ID_HEADER, SOURCE_ARTEFACT_ID);
        headerMapUploadFlatFile.put(PublicationConfiguration.DISPLAY_FROM_HEADER, DISPLAY_FROM.toString());
        headerMapUploadFlatFile.put(PublicationConfiguration.DISPLAY_TO_HEADER, DISPLAY_FROM.plusDays(1).toString());
        headerMapUploadFlatFile.put(PublicationConfiguration.COURT_ID, courtId);
        headerMapUploadFlatFile.put(PublicationConfiguration.LIST_TYPE, ListType.ET_DAILY_LIST.toString());
        headerMapUploadFlatFile.put(PublicationConfiguration.CONTENT_DATE, CONTENT_DATE.toString());
        headerMapUploadFlatFile.put(PublicationConfiguration.SENSITIVITY_HEADER, sensitivity.toString());
        headerMapUploadFlatFile.put(PublicationConfiguration.LANGUAGE_HEADER, LANGUAGE.toString());
        headerMapUploadFlatFile.put("Content-Type", MediaType.MULTIPART_FORM_DATA_VALUE);

        String filePath = this.getClass().getClassLoader().getResource("data/testFlatFile.pdf").getPath();
        File pdfFile = new File(filePath);

        final Response responseUploadFlatFile = doPostRequestMultiPart(
            PUBLICATION_URL,
            headerMapUploadFlatFile, "", pdfFile
        );

        assertThat(responseUploadFlatFile.getStatusCode()).isEqualTo(CREATED.value());
        return responseUploadFlatFile.as(Artefact.class);
    }

    private Artefact uploadNonStrategicFile(String courtId, Sensitivity sensitivity, String listType) {
        Map<String, String> headerMapUploadNonStrategicFile = getBaseHeaderMap();
        headerMapUploadNonStrategicFile.put(PublicationConfiguration.TYPE_HEADER, ARTEFACT_TYPE.toString());
        headerMapUploadNonStrategicFile.put(PublicationConfiguration.PROVENANCE_HEADER, PROVENANCE);
        headerMapUploadNonStrategicFile.put(PublicationConfiguration.SOURCE_ARTEFACT_ID_HEADER, SOURCE_ARTEFACT_ID);
        headerMapUploadNonStrategicFile.put(PublicationConfiguration.DISPLAY_FROM_HEADER, DISPLAY_FROM.toString());
        headerMapUploadNonStrategicFile.put(
            PublicationConfiguration.DISPLAY_TO_HEADER,
            DISPLAY_FROM.plusDays(1).toString()
        );
        headerMapUploadNonStrategicFile.put(PublicationConfiguration.COURT_ID, courtId);
        headerMapUploadNonStrategicFile.put(PublicationConfiguration.LIST_TYPE, listType);
        headerMapUploadNonStrategicFile.put(PublicationConfiguration.CONTENT_DATE, CONTENT_DATE.toString());
        headerMapUploadNonStrategicFile.put(PublicationConfiguration.SENSITIVITY_HEADER, sensitivity.toString());
        headerMapUploadNonStrategicFile.put(PublicationConfiguration.LANGUAGE_HEADER, LANGUAGE.toString());

        String filePath = this.getClass().getClassLoader().getResource("data/testExcelFile.xlsx").getPath();
        File excelFile = new File(filePath);

        final Response responseUploadNonStrategicFile = doPostRequestMultiPartWithMimeType(
            NON_STRATEGIC_UPLOAD_PUBLICATION_URL,
            headerMapUploadNonStrategicFile,
            "file",
            excelFile,
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
        );

        assertThat(responseUploadNonStrategicFile.getStatusCode()).isEqualTo(CREATED.value());
        return responseUploadNonStrategicFile.as(Artefact.class);
    }

    @Test
    void testPublicationEndpointsWithJsonFileUpload() throws Exception {
        String randomCaseNumber = Integer.toString(ThreadLocalRandom.current().nextInt(100_000, 200_000));
        String jsonString = getJsonString(randomCaseNumber);
        Artefact returnedArtefact = uploadArtefact(jsonString, courtId, Sensitivity.PUBLIC, PROVENANCE);

        assertThat(returnedArtefact.getContentDate()).isEqualTo(CONTENT_DATE);
        assertThat(returnedArtefact.getListType()).isEqualTo(LIST_TYPE);
        assertThat(returnedArtefact.toString()).contains(CASE_NUMBER, "A Vs B");

        Map<String, String> headerMap = getBaseHeaderMap();
        headerMap.put(USER_ID_HEADER, userId);

        String artefactId = returnedArtefact.getArtefactId().toString();
        final Response responseGetArtefactPayload = doGetRequest(
            PUBLICATION_URL + '/' + artefactId + "/payload", headerMap
        );
        assertThat(responseGetArtefactPayload.getStatusCode()).isEqualTo(OK.value());
        assertThat(responseGetArtefactPayload.asString()).isEqualTo(jsonString);

        final Response responseGetAllRelevantArtefactsBySearchValue = doGetRequest(
            ARTEFACT_BY_SEARCH_VALUE_URL + SearchType.CASE_ID + '/' + randomCaseNumber, headerMap
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

        Map<String, String> deleteArtefactHeaderMap = getBaseHeaderMap();
        deleteArtefactHeaderMap.put(ISSUER_HEADER, EMAIL);

        final Response responseGetAllRelevantArtefactsByLocationId = doGetRequest(
            ARTEFACT_BY_LOCATION_ID_URL + courtId, headerMap
        );
        assertThat(responseGetAllRelevantArtefactsByLocationId.getStatusCode()).isEqualTo(OK.value());
        Artefact[] returnedGetAllRelevantArtefacts = responseGetAllRelevantArtefactsByLocationId.as(Artefact[].class);

        assertThat(returnedGetAllRelevantArtefacts.length).isEqualTo(1);
        assertThat(returnedGetAllRelevantArtefacts[0].getLocationId()).isEqualTo(courtId);

        final Response responseDeleteArtefact = doDeleteRequest(
            PUBLICATION_URL + '/' + artefactId, deleteArtefactHeaderMap
        );
        assertThat(responseDeleteArtefact.getStatusCode()).isEqualTo(OK.value());
        assertThat(responseDeleteArtefact.asString()).isEqualTo("Successfully deleted artefact: "
                                                                    + artefactId);
    }

    @Test
    void testPublicationEndpointsWithFlatFileUpload() throws Exception {
        Artefact returnedFlatFileArtefact = uploadFlatFile(courtId, Sensitivity.PUBLIC);

        assertThat(returnedFlatFileArtefact.getContentDate()).isEqualTo(CONTENT_DATE);
        assertThat(returnedFlatFileArtefact.getListType()).isEqualTo(ListType.ET_DAILY_LIST);
        assertThat(returnedFlatFileArtefact.getLocationId()).contains(courtId);

        String artefactId = returnedFlatFileArtefact.getArtefactId().toString();
        Map<String, String> headerMap = getBaseHeaderMap();
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

        Map<String, String> deleteArtefactHeaderMap = getBaseHeaderMap();
        deleteArtefactHeaderMap.put(ISSUER_HEADER, EMAIL);

        final Response responseDeleteArtefact = doDeleteRequest(
            PUBLICATION_URL + '/' + artefactId, deleteArtefactHeaderMap
        );
        assertThat(responseDeleteArtefact.getStatusCode()).isEqualTo(OK.value());
        assertThat(responseDeleteArtefact.asString()).isEqualTo("Successfully deleted artefact: "
                                                                    + artefactId);
    }

    @Test
    void testNonStrategicFileUpload() throws Exception {
        Artefact returnedNonStrategicFileArtefact = uploadNonStrategicFile(
            courtId,
            Sensitivity.PUBLIC,
            ListType.PHT_WEEKLY_HEARING_LIST.toString()
        );

        assertThat(returnedNonStrategicFileArtefact.getContentDate()).isEqualTo(CONTENT_DATE);
        assertThat(returnedNonStrategicFileArtefact.getListType()).isEqualTo(ListType.PHT_WEEKLY_HEARING_LIST);
        assertThat(returnedNonStrategicFileArtefact.getLocationId()).contains(courtId);
    }


    @Test
    void testDeleteArtefactByLocationId() throws Exception {
        Artefact returnedArtefact = uploadArtefact(getJsonString(), courtId, Sensitivity.PUBLIC, PROVENANCE);
        String artefactId = returnedArtefact.getArtefactId().toString();

        Response responseGetArtefactMetadata = doGetRequest(
            PUBLICATION_URL + '/' + artefactId, getBaseHeaderMap()
        );
        assertThat(responseGetArtefactMetadata.getStatusCode()).isEqualTo(OK.value());

        Map<String, String> deleteArtefactHeaderMap = getBaseHeaderMap();
        deleteArtefactHeaderMap.put(USER_ID_HEADER, userId);

        Response deleteByLocationResponse = doDeleteRequest(
            String.format(
                DELETE_ARTEFACTS_BY_LOCATION_ID,
                courtId
            ),
            deleteArtefactHeaderMap
        );

        assertThat(deleteByLocationResponse.getStatusCode()).isEqualTo(OK.value());
        assertThat(deleteByLocationResponse.asString()).isEqualTo(
            "Total 1 artefact deleted for location id " + courtId);

        responseGetArtefactMetadata = doGetRequest(
            PUBLICATION_URL + '/' + artefactId, getBaseHeaderMap()
        );
        assertThat(responseGetArtefactMetadata.getStatusCode()).isEqualTo(NOT_FOUND.value());
    }

    @Test
    void testGetMetadataWhenUserIsUnauthorised() throws Exception {
        Artefact returnedArtefact = uploadArtefact(getJsonString(), courtId, Sensitivity.CLASSIFIED, PROVENANCE);

        Map<String, String> headerMap = getBaseHeaderMap();
        headerMap.put(USER_ID_HEADER, userId);

        final Response responseGetArtefactMetadata = doGetRequest(
            PUBLICATION_URL + '/' + returnedArtefact.getArtefactId(), headerMap
        );

        assertThat(responseGetArtefactMetadata.getStatusCode()).isEqualTo(NOT_FOUND.value());
    }

    @Test
    void testGetMetadataWhenUserDoesNotExist() throws Exception {
        Artefact returnedArtefact = uploadArtefact(getJsonString(), courtId, Sensitivity.CLASSIFIED, PROVENANCE);

        Map<String, String> headerMap = getBaseHeaderMap();
        headerMap.put(USER_ID_HEADER, UUID.randomUUID().toString());

        final Response responseGetArtefactMetadata = doGetRequest(
            PUBLICATION_URL + '/' + returnedArtefact.getArtefactId(), headerMap
        );

        assertThat(responseGetArtefactMetadata.getStatusCode()).isEqualTo(NOT_FOUND.value());
    }

    @Test
    void testGetPayloadWhenUserIsUnauthorised() throws Exception {
        Artefact returnedArtefact = uploadArtefact(getJsonString(), courtId, Sensitivity.CLASSIFIED, PROVENANCE);

        Map<String, String> headerMap = getBaseHeaderMap();
        headerMap.put(USER_ID_HEADER, userId);

        final Response responseGetArtefactMetadata = doGetRequest(
            PUBLICATION_URL + '/' + returnedArtefact.getArtefactId() + "/payload", headerMap
        );

        assertThat(responseGetArtefactMetadata.getStatusCode()).isEqualTo(NOT_FOUND.value());
    }

    @Test
    void testGetPayloadWhenUserDoesNotExist() throws Exception {
        Artefact returnedArtefact = uploadArtefact(getJsonString(), courtId, Sensitivity.CLASSIFIED, PROVENANCE);

        Map<String, String> headerMap = getBaseHeaderMap();
        headerMap.put(USER_ID_HEADER, UUID.randomUUID().toString());

        final Response responseGetArtefactMetadata = doGetRequest(
            PUBLICATION_URL + '/' + returnedArtefact.getArtefactId() + "/payload", headerMap
        );

        assertThat(responseGetArtefactMetadata.getStatusCode()).isEqualTo(NOT_FOUND.value());
    }

    @Test
    void testGetFlatFileWhenUserIsUnauthorised() {
        Artefact returnedArtefact = uploadFlatFile(courtId, Sensitivity.CLASSIFIED);

        Map<String, String> headerMap = getBaseHeaderMap();
        headerMap.put(USER_ID_HEADER, userId);

        final Response responseGetArtefactMetadata = doGetRequest(
            PUBLICATION_URL + '/' + returnedArtefact.getArtefactId() + "/file", headerMap
        );

        assertThat(responseGetArtefactMetadata.getStatusCode()).isEqualTo(NOT_FOUND.value());
    }

    @Test
    void testGetFlatFileWhenUserDoesNotExist() {
        Artefact returnedArtefact = uploadFlatFile(courtId, Sensitivity.CLASSIFIED);

        Map<String, String> headerMap = getBaseHeaderMap();
        headerMap.put(USER_ID_HEADER, UUID.randomUUID().toString());

        final Response responseGetArtefactMetadata = doGetRequest(
            PUBLICATION_URL + '/' + returnedArtefact.getArtefactId() + "/file", headerMap
        );

        assertThat(responseGetArtefactMetadata.getStatusCode()).isEqualTo(NOT_FOUND.value());
    }

    @Test
    void testGetArtefactsBySearchValueWhenUserIsUnauthorised() throws IOException {
        String randomCaseNumber = Integer.toString(ThreadLocalRandom.current().nextInt(100_000, 200_000));
        uploadArtefact(getJsonString(randomCaseNumber), courtId, Sensitivity.CLASSIFIED, PROVENANCE);

        Map<String, String> headerMap = getBaseHeaderMap();
        headerMap.put(USER_ID_HEADER, userId);

        final Response searchValueResponse = doGetRequest(
            ARTEFACT_BY_SEARCH_VALUE_URL + SearchType.CASE_ID + '/' + randomCaseNumber, headerMap
        );

        assertThat(searchValueResponse.getStatusCode()).isEqualTo(NOT_FOUND.value());
    }

    @Test
    void testGetArtefactsBySearchValueWhenUserDoesNotExist() throws IOException {
        String randomCaseNumber = Integer.toString(ThreadLocalRandom.current().nextInt(100_000, 200_000));
        uploadArtefact(getJsonString(randomCaseNumber), courtId, Sensitivity.CLASSIFIED, PROVENANCE);

        Map<String, String> headerMap = getBaseHeaderMap();
        headerMap.put(USER_ID_HEADER, UUID.randomUUID().toString());

        final Response searchValueResponse = doGetRequest(
            ARTEFACT_BY_SEARCH_VALUE_URL + SearchType.CASE_ID + '/' + randomCaseNumber, headerMap
        );
        assertThat(searchValueResponse.getStatusCode()).isEqualTo(NOT_FOUND.value());
    }

    @Test
    void testGetArtefactsByLocationIdWhenUserIsUnauthorised() {
        uploadFlatFile(courtId, Sensitivity.CLASSIFIED);

        Map<String, String> headerMap = getBaseHeaderMap();
        headerMap.put(USER_ID_HEADER, userId);

        final Response responseGetArtefactMetadata = doGetRequest(
            PUBLICATION_URL + "/locationId/" + courtId, headerMap
        );

        assertThat(responseGetArtefactMetadata.getStatusCode()).isEqualTo(OK.value());
        assertThat(responseGetArtefactMetadata.as(Artefact[].class).length).isEqualTo(0);
    }

    @Test
    void testGetArtefactsByLocationIdWhenUserDoesNotExist() {
        uploadFlatFile(courtId, Sensitivity.CLASSIFIED);

        Map<String, String> headerMap = getBaseHeaderMap();
        headerMap.put(USER_ID_HEADER, UUID.randomUUID().toString());

        final Response responseGetArtefactMetadata = doGetRequest(
            PUBLICATION_URL + "/locationId/" + courtId, headerMap
        );

        assertThat(responseGetArtefactMetadata.getStatusCode()).isEqualTo(OK.value());
        assertThat(responseGetArtefactMetadata.as(Artefact[].class).length).isEqualTo(0);
    }

    /**
     * For now, this will return an OK Response code. Ticket to be raised to add a check for this.
     */
    @Test
    void testGetDeleteByLocationIdWhenUserDoesNotExist() {
        Artefact artefact = uploadFlatFile(courtId, Sensitivity.PUBLIC);

        Map<String, String> headerMap = getBaseHeaderMap();
        headerMap.put(USER_ID_HEADER, UUID.randomUUID().toString());

        final Response deleteArtefactsByLocationId = doDeleteRequest(
            PUBLICATION_URL + "/" + courtId + "/deleteArtefacts", headerMap
        );

        assertThat(deleteArtefactsByLocationId.getStatusCode()).isEqualTo(OK.value());

        final Response responseGetArtefactMetadata = doGetRequest(
            PUBLICATION_URL + '/' + artefact.getArtefactId().toString(), getBaseHeaderMap()
        );
        assertThat(responseGetArtefactMetadata.getStatusCode()).isEqualTo(NOT_FOUND.value());
    }

    @Test
    void testGetMiDataV2() throws IOException {
        Artefact returnedArtefact = uploadArtefact(getJsonString(), courtId, Sensitivity.CLASSIFIED, PROVENANCE);
        Artefact returnedNomatchArtefact = uploadArtefact(
            getJsonString(),
            courtId,
            Sensitivity.CLASSIFIED,
            "RandomProvenance"
        );
        UUID artefactId = returnedArtefact.getArtefactId();
        UUID artefactIdNoMatch = returnedNomatchArtefact.getArtefactId();
        noMatchArtefactId = artefactIdNoMatch.toString();

        Map<String, String> headerMap = getBaseHeaderMap();

        final Response responseGetArtefactMetadata = doGetRequest(
            MI_DATA_URL, headerMap
        );

        assertThat(responseGetArtefactMetadata.getStatusCode()).isEqualTo(OK.value());
        List<PublicationMiData> returnedArtefacts = Arrays.asList(responseGetArtefactMetadata.getBody()
                                                                      .as(PublicationMiData[].class));

        Assertions.assertThat(returnedArtefacts).anyMatch(
            artefact -> artefactId.equals(artefact.getArtefactId())
        );

        Assertions.assertThat(returnedArtefacts).anyMatch(
            noMatchArtefact -> artefactIdNoMatch.equals(noMatchArtefact.getArtefactId())
        );
    }
}
