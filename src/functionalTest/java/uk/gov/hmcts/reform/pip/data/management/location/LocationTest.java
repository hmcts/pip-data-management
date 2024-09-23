package uk.gov.hmcts.reform.pip.data.management.location;

import io.restassured.response.Response;
import io.zonky.test.db.AutoConfigureEmbeddedDatabase;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.pip.data.management.Application;
import uk.gov.hmcts.reform.pip.data.management.models.location.Location;
import uk.gov.hmcts.reform.pip.data.management.utils.FunctionalTestBase;
import uk.gov.hmcts.reform.pip.data.management.utils.OAuthClient;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static com.github.dockerjava.zerodep.shaded.org.apache.hc.core5.http.HttpHeaders.AUTHORIZATION;
import static io.restassured.RestAssured.given;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.http.HttpStatus.OK;
import static uk.gov.hmcts.reform.pip.data.management.utils.TestUtil.BEARER;

@ExtendWith(SpringExtension.class)
@ActiveProfiles(profiles = "functional")
@AutoConfigureEmbeddedDatabase(type = AutoConfigureEmbeddedDatabase.DatabaseType.POSTGRES)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(classes = {Application.class, OAuthClient.class},
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class LocationTest extends FunctionalTestBase {

    @Value("${system-admin-provenance-id}")
    private String systemAdminProvenanceId;

    private static final String LOCATION_LIST = "locationList";
    private static final String TEST_LOCATION_NAME = "TestLocationOne";
    private static final String TEST_LOCATION_ID = "2001";
    private static final String TEST_JURISDICTION1 = "Magistrates";
    private static final String TEST_JURISDICTION2 = "Family";
    private static final String TEST_REGION = "North West";

    private static final String BASE_LOCATIONS_URL = "/locations";
    private static final String UPLOAD_LOCATIONS_URL = BASE_LOCATIONS_URL + "/upload";
    private static final String GET_LOCATION_BY_ID_URL = BASE_LOCATIONS_URL + "/" + TEST_LOCATION_ID;
    private static final String GET_LOCATION_BY_NAME_URL = BASE_LOCATIONS_URL + "/name/" + TEST_LOCATION_NAME
        + "/language/ENGLISH";
    private static final String GET_LOCATION_BY_REGION_AND_JURISDICTION = BASE_LOCATIONS_URL + "/filter";
    private static final String DOWNLOAD_CSV_LOCATIONS_URL = BASE_LOCATIONS_URL + "/download/csv";

    @Test
    void locationControllerHappyPathTests() throws Exception {
        Map<String, String> headerMap = new ConcurrentHashMap<>();
        headerMap.put(AUTHORIZATION, BEARER + accessToken);

        Map<String, String> deleteHeaderMap = new ConcurrentHashMap<>();
        deleteHeaderMap.put(AUTHORIZATION, BEARER + accessToken);
        deleteHeaderMap.put("x-provenance-user-id", systemAdminProvenanceId);

        Map<String, String> headerMapUploadLocations = new ConcurrentHashMap<>();
        headerMapUploadLocations.put(AUTHORIZATION, BEARER + accessToken);
        headerMapUploadLocations.put("Content-Type", MediaType.MULTIPART_FORM_DATA_VALUE);

        String filePath = this.getClass().getClassLoader().getResource("location/ValidLocations.csv").getPath();
        File csvFile = new File(filePath);

        final Response responseUploadLocations = doPostRequestMultiPart(
            UPLOAD_LOCATIONS_URL,
            headerMapUploadLocations, LOCATION_LIST, csvFile
        );

        assertThat(responseUploadLocations.getStatusCode()).isEqualTo(OK.value());
        assertEquals(responseUploadLocations.as(Location[].class).length, 3,
                     "Failed to create all locations");
        assertThat(responseUploadLocations.asString().contains(TEST_LOCATION_NAME));

        final Response responseGetLocationById = doGetRequest(
            GET_LOCATION_BY_ID_URL,
            headerMap
        );
        assertThat(responseGetLocationById.getStatusCode()).isEqualTo(OK.value());
        assertThat(responseGetLocationById.asString().contains(TEST_LOCATION_NAME));

        final Response responseGetAllLocations = doGetRequest(
            BASE_LOCATIONS_URL,
            headerMap
        );
        assertThat(responseGetAllLocations.getStatusCode()).isEqualTo(OK.value());
        assertThat(responseGetAllLocations.asString().contains(TEST_LOCATION_NAME));
        assertThat(responseGetAllLocations.asString().contains("TestLocationTwo"));
        assertThat(responseGetAllLocations.asString().contains("TestLocationThree"));

        final Response responseGetLocationByName = doGetRequest(
            GET_LOCATION_BY_NAME_URL,
            headerMap
        );
        assertThat(responseGetLocationByName.getStatusCode()).isEqualTo(OK.value());
        assertThat(responseGetLocationByName.asString().contains("2001"));


        final Response responseGetLocationByRegionAndJurisdiction =
            given()
                .relaxedHTTPSValidation()
                .headers(getRequestHeaders(headerMap))
                .queryParam("jurisdictions", List.of(TEST_JURISDICTION1, TEST_JURISDICTION2))
                .queryParam("regions", List.of(TEST_REGION))
                .when()
                .get(GET_LOCATION_BY_REGION_AND_JURISDICTION)
                .thenReturn();

        assertThat(responseGetLocationByRegionAndJurisdiction.getStatusCode()).isEqualTo(OK.value());
        assertThat(responseGetLocationByRegionAndJurisdiction.asString().contains(TEST_LOCATION_NAME));

        final Response responseDownloadCsv = doGetRequest(
            DOWNLOAD_CSV_LOCATIONS_URL,
            headerMap
        );
        assertThat(responseDownloadCsv.getStatusCode()).isEqualTo(OK.value());
        assertThat(responseDownloadCsv.asString().contains(TEST_LOCATION_NAME));
        assertThat(responseDownloadCsv.asString().contains("TestLocationTwo"));
        assertThat(responseDownloadCsv.asString().contains("TestLocationThree"));

        final Response responseDeleteLocationById = doDeleteRequest(
            GET_LOCATION_BY_ID_URL,
            deleteHeaderMap
        );
        assertThat(responseDeleteLocationById.getStatusCode()).isEqualTo(OK.value());
        assertThat(responseDeleteLocationById.asString().contains("Location with id " + TEST_LOCATION_ID
                                                                      + " has been deleted"));
    }
}

