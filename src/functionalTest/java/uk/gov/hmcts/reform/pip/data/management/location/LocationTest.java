package uk.gov.hmcts.reform.pip.data.management.location;

import io.restassured.response.Response;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.pip.data.management.models.location.Location;
import uk.gov.hmcts.reform.pip.data.management.models.location.LocationDeletion;
import uk.gov.hmcts.reform.pip.data.management.utils.FunctionalTestBase;
import uk.gov.hmcts.reform.pip.data.management.utils.OAuthClient;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static com.github.dockerjava.zerodep.shaded.org.apache.hc.core5.http.HttpHeaders.AUTHORIZATION;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.OK;
import static uk.gov.hmcts.reform.pip.data.management.utils.TestUtil.BEARER;
import static uk.gov.hmcts.reform.pip.data.management.utils.TestUtil.randomLocationId;

@ExtendWith(SpringExtension.class)
@ActiveProfiles(profiles = "functional")
@SpringBootTest(classes = {OAuthClient.class})
class LocationTest extends FunctionalTestBase {

    @Value("${test-system-admin-id}")
    private String systemAdminUserId;

    private static final String LOCATION_LIST = "locationList";
    private static final String BASE_LOCATION_NAME = "LocationTestLocation";
    private static final String TEST_LOCATION_NAME_ONE = "LocationTestLocationOne";
    private static final Integer TEST_LOCATION_ID_ONE = 2001;
    private static final String TEST_LOCATION_NAME_TWO = "LocationTestLocationTwo";
    private static final Integer TEST_LOCATION_ID_TWO = 2002;
    private static final String TEST_LOCATION_NAME_THREE = "LocationTestLocationThree";
    private static final Integer TEST_LOCATION_ID_THREE = 2003;
    private static final String TEST_JURISDICTION = "Test Jurisdiction";
    private static final String TEST_REGION = "North East";
    private static final String REQUESTER_ID_HEADER = "x-requester-id";

    private static final String BASE_LOCATIONS_URL = "/locations";
    private static final String UPLOAD_LOCATIONS_URL = BASE_LOCATIONS_URL + "/upload";
    private static final String GET_LOCATION_BY_ID_URL = BASE_LOCATIONS_URL + "/" + TEST_LOCATION_ID_ONE;
    private static final String GET_LOCATION_BY_NAME_URL = BASE_LOCATIONS_URL + "/name/" + TEST_LOCATION_NAME_ONE
        + "/language/ENGLISH";
    private static final String GET_LOCATION_BY_REGION_AND_JURISDICTION_URL = BASE_LOCATIONS_URL + "/filter";
    private static final String DOWNLOAD_CSV_LOCATIONS_URL = BASE_LOCATIONS_URL + "/download/csv";
    private static final String TESTING_SUPPORT_LOCATION_URL = "/testing-support/location/";

    @AfterAll
    public void teardown() {
        doDeleteRequest(
            TESTING_SUPPORT_LOCATION_URL + "LocationTestLocation",
            Map.of(AUTHORIZATION, BEARER + accessToken)
        );
    }

    @Test
    void locationControllerHappyPathTests() throws Exception {
        Map<String, String> headerMapUploadLocations = Map.of(AUTHORIZATION, BEARER + accessToken,
                                                              "Content-Type", MediaType.MULTIPART_FORM_DATA_VALUE,
                                                              REQUESTER_ID_HEADER, systemAdminUserId
        );

        String filePath = this.getClass().getClassLoader().getResource("location/ValidLocations.csv").getPath();
        File csvFile = new File(filePath);

        final Response responseUploadLocations = doPostRequestMultiPart(
            UPLOAD_LOCATIONS_URL,
            headerMapUploadLocations, LOCATION_LIST, csvFile
        );

        assertThat(responseUploadLocations.getStatusCode()).isEqualTo(OK.value());
        assertThat(responseUploadLocations.as(Location[].class)).hasSize(3);

        List<Location> createdLocations = Arrays.asList(responseUploadLocations.getBody().as(Location[].class));

        assertThat(createdLocations.get(0).getLocationId()).isEqualTo(TEST_LOCATION_ID_ONE);
        assertThat(createdLocations.get(0).getName()).isEqualTo(TEST_LOCATION_NAME_ONE);
        assertThat(createdLocations.get(1).getLocationId()).isEqualTo(TEST_LOCATION_ID_TWO);
        assertThat(createdLocations.get(1).getName()).isEqualTo(TEST_LOCATION_NAME_TWO);
        assertThat(createdLocations.get(2).getLocationId()).isEqualTo(TEST_LOCATION_ID_THREE);
        assertThat(createdLocations.get(2).getName()).isEqualTo(TEST_LOCATION_NAME_THREE);

        final Response responseGetLocationById = doGetRequest(
            GET_LOCATION_BY_ID_URL,
            getBaseHeaderMap()
        );

        assertThat(responseGetLocationById.getStatusCode()).isEqualTo(OK.value());
        Location returnedLocation = responseGetLocationById.as(Location.class);
        assertThat(returnedLocation.getLocationId()).isEqualTo(TEST_LOCATION_ID_ONE);
        assertThat(returnedLocation.getName()).isEqualTo(TEST_LOCATION_NAME_ONE);

        final Response responseGetAllLocations = doGetRequest(
            BASE_LOCATIONS_URL,
            getBaseHeaderMap()
        );
        assertThat(responseGetAllLocations.getStatusCode()).isEqualTo(OK.value());
        List<Location> returnedLocations = Arrays.asList(responseGetAllLocations.getBody()
                                                                       .as(Location[].class));

        assertThat(returnedLocations).anyMatch(
            location -> TEST_LOCATION_NAME_ONE.equals(location.getName())
                && TEST_LOCATION_ID_ONE.equals(location.getLocationId())
        );
        assertThat(returnedLocations).anyMatch(
            location -> TEST_LOCATION_NAME_TWO.equals(location.getName())
                && TEST_LOCATION_ID_TWO.equals(location.getLocationId())
        );
        assertThat(returnedLocations).anyMatch(
            location -> TEST_LOCATION_NAME_THREE.equals(location.getName())
                && TEST_LOCATION_ID_THREE.equals(location.getLocationId())
        );

        final Response responseGetLocationByName = doGetRequest(
            GET_LOCATION_BY_NAME_URL,
            getBaseHeaderMap()
        );
        assertThat(responseGetLocationByName.getStatusCode()).isEqualTo(OK.value());
        Location returnedLocationByName = responseGetLocationByName.as(Location.class);
        assertThat(returnedLocationByName.getLocationId()).isEqualTo(TEST_LOCATION_ID_ONE);
        assertThat(returnedLocationByName.getName()).isEqualTo(TEST_LOCATION_NAME_ONE);


        Map<String, Object> queryParameters = Map.of(
            "jurisdictions",
            TEST_JURISDICTION,
            "regions",
            TEST_REGION
        );
        final Response responseGetLocationByRegionAndJurisdiction =
            doGetRequest(
                GET_LOCATION_BY_REGION_AND_JURISDICTION_URL,
                getBaseHeaderMap(), queryParameters
            );

        assertThat(responseGetLocationByRegionAndJurisdiction.getStatusCode()).isEqualTo(OK.value());
        Location[] returnedLocationByRegionAndJurisdiction = responseGetLocationByRegionAndJurisdiction
            .as(Location[].class);
        assertThat(returnedLocationByRegionAndJurisdiction[0].getLocationId()).isEqualTo(TEST_LOCATION_ID_THREE);
        assertThat(returnedLocationByRegionAndJurisdiction[0].getName()).isEqualTo(TEST_LOCATION_NAME_THREE);

        Map<String, String> headerMapDownloadCsv = getBaseHeaderMap();
        headerMapDownloadCsv.put(REQUESTER_ID_HEADER, systemAdminUserId);

        final Response responseDownloadCsv = doGetRequest(
            DOWNLOAD_CSV_LOCATIONS_URL,
            headerMapDownloadCsv
        );
        assertThat(responseDownloadCsv.getStatusCode()).isEqualTo(OK.value());
        assertThat(responseDownloadCsv.asString()).contains(TEST_LOCATION_NAME_ONE);
        assertThat(responseDownloadCsv.asString()).contains(TEST_LOCATION_NAME_TWO);
        assertThat(responseDownloadCsv.asString()).contains(TEST_LOCATION_NAME_THREE);

        Map<String, String> deleteHeaderMap = Map.of(AUTHORIZATION, BEARER + accessToken,
                                                     REQUESTER_ID_HEADER, systemAdminUserId
        );

        final Response responseDeleteLocationById = doDeleteRequest(
            GET_LOCATION_BY_ID_URL,
            deleteHeaderMap
        );
        assertThat(responseDeleteLocationById.getStatusCode()).isEqualTo(OK.value());
        assertThat(responseDeleteLocationById.as(LocationDeletion.class).isExists()).isFalse();
    }

    /**
     * For now, this will return an OK Response code. Ticket to be raised to add a check for this.
     */
    @Test
    void testDeleteLocationByLocationIdWhenUserDoesNotExist() {
        String courtId = randomLocationId();
        doPostRequest(
            TESTING_SUPPORT_LOCATION_URL + courtId,
            Map.of(AUTHORIZATION, BEARER + accessToken), BASE_LOCATION_NAME + "-" + courtId
        );

        Map<String, String> deleteHeaderMap = Map.of(AUTHORIZATION, BEARER + accessToken,
                                                     REQUESTER_ID_HEADER, systemAdminUserId
        );
        final Response responseDeleteLocationById = doDeleteRequest(
            "/locations/" + courtId,
            deleteHeaderMap
        );

        assertThat(responseDeleteLocationById.getStatusCode()).isEqualTo(OK.value());

        final Response responseGetLocationById = doGetRequest(
            "/locations/" + courtId,
            getBaseHeaderMap()
        );

        assertThat(responseGetLocationById.getStatusCode()).isEqualTo(NOT_FOUND.value());
    }

}
