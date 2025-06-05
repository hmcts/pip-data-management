package uk.gov.hmcts.reform.pip.data.management.location;

import io.restassured.response.Response;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.pip.data.management.models.location.LocationMetadata;
import uk.gov.hmcts.reform.pip.data.management.utils.FunctionalTestBase;
import uk.gov.hmcts.reform.pip.data.management.utils.OAuthClient;

import java.util.Map;
import java.util.UUID;

import static com.github.dockerjava.zerodep.shaded.org.apache.hc.core5.http.HttpHeaders.AUTHORIZATION;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.OK;
import static uk.gov.hmcts.reform.pip.data.management.utils.TestUtil.BEARER;
import static uk.gov.hmcts.reform.pip.data.management.utils.TestUtil.randomLocationId;

@ExtendWith(SpringExtension.class)
@ActiveProfiles(profiles = "functional")
@SpringBootTest(classes = {OAuthClient.class})
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class LocationMetadataTest extends FunctionalTestBase {

    @Value("${test-system-admin-id}")
    private String systemAdminUserId;

    private static final String TESTING_SUPPORT_LOCATION_URL = "/testing-support/location/";

    private static final String BASE_LOCATION_METADATA_URL = "/location-metadata";
    private static final String LOCATION_METADATA_GET_LOCATION_ID_URL =
        BASE_LOCATION_METADATA_URL + "/location";

    private static final String BASE_COURT_NAME = "TestLocation-PublicationTest";
    private static final String ENGLISH_CAUTION_MESSAGE = "English Caution Message";
    private static final String WELSH_CAUTION_MESSAGE = "Welsh Caution Message";
    private static final String ENGLISH_NO_LIST_MESSAGE = "English No List Message";
    private static final String WELSH_NO_LIST_MESSAGE = "Welsh No List Message";
    private static final String UPDATED_TEXT = "Updated Text";
    private static final String CONTENT_TYPE = "Content-Type";
    private static final String REQUESTER_ID_HEADER = "x-requester-id";

    private String locationId;
    private UUID locationMetadataId;

    @BeforeAll
    void setUp() {
        locationId = randomLocationId();

        doPostRequest(
            TESTING_SUPPORT_LOCATION_URL + locationId,
            Map.of(AUTHORIZATION, BEARER + accessToken), BASE_COURT_NAME + "-" + locationId
        );
    }

    @AfterAll
    public void teardown() {
        doDeleteRequest(TESTING_SUPPORT_LOCATION_URL + BASE_COURT_NAME, getBaseHeaderMap());
    }

    private LocationMetadata createLocationMetadata(String locationId, String englishCautionMessage,
            String welshCautionMessage, String englishNoListMessage, String welshNoListMessage) {
        LocationMetadata locationMetadata = new LocationMetadata();
        locationMetadata.setLocationId(Integer.parseInt(locationId));
        locationMetadata.setCautionMessage(englishCautionMessage);
        locationMetadata.setWelshCautionMessage(welshCautionMessage);
        locationMetadata.setNoListMessage(englishNoListMessage);
        locationMetadata.setWelshNoListMessage(welshNoListMessage);
        return locationMetadata;
    }


    @Test
    @Order(1)
    void testAddLocationMetadataControllerHappyPath() {
        Map<String, String> headerMap = Map.of(AUTHORIZATION, BEARER + accessToken,
                CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE,
                REQUESTER_ID_HEADER, systemAdminUserId
        );

        Response responseCreateLocationMetadata = doPostRequest(
            BASE_LOCATION_METADATA_URL,
            headerMap,
            createLocationMetadata(locationId, ENGLISH_CAUTION_MESSAGE,
                                   WELSH_CAUTION_MESSAGE, ENGLISH_NO_LIST_MESSAGE,
                                   WELSH_NO_LIST_MESSAGE)
        );

        assertThat(responseCreateLocationMetadata.getStatusCode()).isEqualTo(CREATED.value());
    }

    @Test
    @Order(2)
    void testGetLocationMetadataByLocationIdControllerHappyPath() {
        Map<String, String> headerMap = Map.of(AUTHORIZATION, BEARER + accessToken,
                                               CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE,
                                               REQUESTER_ID_HEADER, systemAdminUserId
        );

        Response responseGetLocationMetadataByLocationId = doGetRequest(
            LOCATION_METADATA_GET_LOCATION_ID_URL + "/" + locationId,
            headerMap
        );

        assertThat(responseGetLocationMetadataByLocationId.getStatusCode()).isEqualTo(OK.value());
        LocationMetadata returnedLocationMetadata = responseGetLocationMetadataByLocationId
            .as(LocationMetadata.class);
        locationMetadataId = returnedLocationMetadata.getLocationMetadataId();
        assertThat(returnedLocationMetadata.getLocationId().toString()).isEqualTo(locationId);
        assertThat(returnedLocationMetadata.getCautionMessage()).isEqualTo(ENGLISH_CAUTION_MESSAGE);
        assertThat(returnedLocationMetadata.getWelshCautionMessage()).isEqualTo(WELSH_CAUTION_MESSAGE);
        assertThat(returnedLocationMetadata.getNoListMessage()).isEqualTo(ENGLISH_NO_LIST_MESSAGE);
        assertThat(returnedLocationMetadata.getWelshNoListMessage()).isEqualTo(WELSH_NO_LIST_MESSAGE);
    }

    @Test
    @Order(3)
    void testLocationMetadataByIdControllerHappyPath() {
        Map<String, String> headerMap = Map.of(AUTHORIZATION, BEARER + accessToken,
                                               CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE,
                                               REQUESTER_ID_HEADER, systemAdminUserId
        );

        Response responseGetLocationMetadataByLocationId = doGetRequest(
            BASE_LOCATION_METADATA_URL + "/" + locationMetadataId,
            headerMap
        );

        assertThat(responseGetLocationMetadataByLocationId.getStatusCode()).isEqualTo(OK.value());
        LocationMetadata returnedLocationMetadata = responseGetLocationMetadataByLocationId
            .as(LocationMetadata.class);
        assertThat(returnedLocationMetadata.getLocationMetadataId()).isEqualTo(locationMetadataId);
        assertThat(returnedLocationMetadata.getLocationId().toString()).isEqualTo(locationId);
        assertThat(returnedLocationMetadata.getCautionMessage()).isEqualTo(ENGLISH_CAUTION_MESSAGE);
        assertThat(returnedLocationMetadata.getWelshCautionMessage()).isEqualTo(WELSH_CAUTION_MESSAGE);
        assertThat(returnedLocationMetadata.getNoListMessage()).isEqualTo(ENGLISH_NO_LIST_MESSAGE);
        assertThat(returnedLocationMetadata.getWelshNoListMessage()).isEqualTo(WELSH_NO_LIST_MESSAGE);
    }

    @Test
    @Order(4)
    void testUpdateLocationMetadataControllerHappyPath() {
        Map<String, String> headerMap = Map.of(AUTHORIZATION, BEARER + accessToken,
                                               CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE,
                                               REQUESTER_ID_HEADER, systemAdminUserId
        );

        Response responseGetLocationMetadata = doGetRequest(
            LOCATION_METADATA_GET_LOCATION_ID_URL + "/" + locationId,
            headerMap
        );

        assertThat(responseGetLocationMetadata.getStatusCode()).isEqualTo(OK.value());
        LocationMetadata returnedLocationMetadata = responseGetLocationMetadata
            .as(LocationMetadata.class);
        returnedLocationMetadata.setCautionMessage(ENGLISH_CAUTION_MESSAGE + UPDATED_TEXT);
        returnedLocationMetadata.setWelshCautionMessage(WELSH_CAUTION_MESSAGE + UPDATED_TEXT);
        returnedLocationMetadata.setNoListMessage(ENGLISH_NO_LIST_MESSAGE + UPDATED_TEXT);
        returnedLocationMetadata.setWelshNoListMessage(WELSH_NO_LIST_MESSAGE + UPDATED_TEXT);

        Response responseUpdateLocationMetadata = doPutRequest(
            BASE_LOCATION_METADATA_URL,
            headerMap,
            returnedLocationMetadata
        );

        assertThat(responseUpdateLocationMetadata.getStatusCode()).isEqualTo(OK.value());

        responseGetLocationMetadata = doGetRequest(
            LOCATION_METADATA_GET_LOCATION_ID_URL + "/" + locationId,
            headerMap
        );

        assertThat(responseGetLocationMetadata.getStatusCode()).isEqualTo(OK.value());
        returnedLocationMetadata = responseGetLocationMetadata
            .as(LocationMetadata.class);
        locationMetadataId = returnedLocationMetadata.getLocationMetadataId();
        assertThat(returnedLocationMetadata.getLocationId().toString()).isEqualTo(locationId);
        assertThat(returnedLocationMetadata.getCautionMessage()).isEqualTo(ENGLISH_CAUTION_MESSAGE + UPDATED_TEXT);
        assertThat(returnedLocationMetadata.getWelshCautionMessage()).isEqualTo(WELSH_CAUTION_MESSAGE + UPDATED_TEXT);
        assertThat(returnedLocationMetadata.getNoListMessage()).isEqualTo(ENGLISH_NO_LIST_MESSAGE + UPDATED_TEXT);
        assertThat(returnedLocationMetadata.getWelshNoListMessage()).isEqualTo(WELSH_NO_LIST_MESSAGE + UPDATED_TEXT);
    }

    @Test
    @Order(5)
    void testDeleteLocationMetadataByIdControllerHappyPath() {
        Map<String, String> headerMap = Map.of(AUTHORIZATION, BEARER + accessToken,
                                               CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE,
                                               REQUESTER_ID_HEADER, systemAdminUserId
        );

        Response responseGetLocationMetadataByLocationId = doDeleteRequest(
            BASE_LOCATION_METADATA_URL + "/" + locationMetadataId,
            headerMap
        );

        assertThat(responseGetLocationMetadataByLocationId.getStatusCode()).isEqualTo(OK.value());
    }
}
