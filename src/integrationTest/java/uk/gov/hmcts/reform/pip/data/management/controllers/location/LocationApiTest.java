package uk.gov.hmcts.reform.pip.data.management.controllers.location;

import io.zonky.test.db.AutoConfigureEmbeddedDatabase;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import uk.gov.hmcts.reform.pip.data.management.Application;
import uk.gov.hmcts.reform.pip.data.management.errorhandling.ExceptionResponse;
import uk.gov.hmcts.reform.pip.data.management.errorhandling.UiExceptionResponse;
import uk.gov.hmcts.reform.pip.data.management.models.location.Location;
import uk.gov.hmcts.reform.pip.data.management.models.location.LocationReference;
import uk.gov.hmcts.reform.pip.data.management.utils.LocationIntegrationTestBase;
import uk.gov.hmcts.reform.pip.model.account.PiUser;
import uk.gov.hmcts.reform.pip.model.location.LocationType;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.annotation.DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.pip.model.account.Roles.SYSTEM_ADMIN;

@SpringBootTest(classes = {Application.class},
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("integration")
@AutoConfigureEmbeddedDatabase(type = AutoConfigureEmbeddedDatabase.DatabaseType.POSTGRES)
@DirtiesContext(classMode = AFTER_EACH_TEST_METHOD)
class LocationApiTest extends LocationIntegrationTestBase {
    private static final String ROOT_URL = "/locations";
    private static final String GET_LOCATION_BY_ID_ENDPOINT = ROOT_URL + "/";
    private static final String GET_LOCATION_BY_NAME_ENDPOINT = ROOT_URL + "/name/%s/language/%s";
    private static final String GET_LOCATION_BY_NAME_V2_ENDPOINT = ROOT_URL + "/name";
    private static final String DOWNLOAD_LOCATIONS_ENDPOINT = ROOT_URL + "/download/csv";
    private static final String UPLOAD_API = ROOT_URL + "/upload";
    private static final String LOCATIONS_CSV = "location/ValidCsv.csv";
    private static final String CSV_WITH_EXISTING_LOCATION_NAME = "location/ValidCsvWithExistingLocationName.csv";
    private static final String CSV_WITH_DUPLICATED_LOCATION_NAME = "location/ValidCsvWithDuplicatedLocationName.csv";
    private static final String CSV_WITHOUT_LOCATION_TYPE = "location/InvalidCsvWithoutLocationType.csv";
    private static final String DELETE_LOCATIONS_CSV = "location/ValidCsvForDeleteCourt.csv";
    private static final String UPDATED_CSV = "location/UpdatedCsv.csv";

    private static final String USER_ID = UUID.randomUUID().toString();
    private static final String ENGLISH_LANGUAGE_PARAM_VALUE = "eng";
    private static final String WELSH_LANGUAGE_PARAM_VALUE = "cy";
    private static final String LOCATION_NAME_PARAM = "locationName";
    private static final String LANGUAGE_PARAM = "language";
    private static final String INVALID_LOCATION_NAME = "invalid";
    private static final String NO_LOCATION_FOUND_ERROR = "No location found with the name: " + INVALID_LOCATION_NAME;
    private static final String LOCATION_NAME_SEARCH_MESSAGE = "Unexpected error message returned when location by "
        + "name not found";
    private static final String VALIDATION_UNKNOWN_LOCATION = "Unexpected location has been returned";
    private static final String VALIDATION_UNEXPECTED_NUMBER_OF_LOCATIONS =
        "Unexpected number of locations has been returned";
    private static final String VALIDATION_LOCATION_NAME_NOT_AS_EXPECTED = "Location name is not as expected";

    private static final String USERNAME = "admin";
    private static final String VALID_ROLE = "APPROLE_api.request.admin";
    private static final String LOCATION_LIST = "locationList";
    private static final String USER_ID_HEADER = "x-user-id";
    private static final String EMAIL = "test@justice.gov.uk";

    @Test
    @WithMockUser(username = USERNAME, authorities = {VALID_ROLE})
    void testGetAllLocationsReturnsCorrectLocations() throws Exception {
        List<Location> locations = createLocations(LOCATIONS_CSV);

        MvcResult mvcResult = mockMvc.perform(get(ROOT_URL))
            .andExpect(status().isOk())
            .andReturn();

        Location[] arrayLocations =
            OBJECT_MAPPER.readValue(mvcResult.getResponse().getContentAsString(), Location[].class);

        List<Location> returnedLocations = Arrays.asList(arrayLocations);

        assertEquals(4, returnedLocations.size(), VALIDATION_UNEXPECTED_NUMBER_OF_LOCATIONS);

        for (Location location : locations) {
            assertTrue(
                returnedLocations.stream().anyMatch(x -> compareLocationWithoutReference.test(x, location)),
                "Expected location not displayed in list"
            );
        }
    }

    @Test
    @WithMockUser(username = USERNAME, authorities = {VALID_ROLE})
    void testGetLocationByIdReturnsSuccess() throws Exception {
        List<Location> locations = createLocations(LOCATIONS_CSV);

        Location location = locations.get(0);

        MvcResult mvcResult = mockMvc.perform(get(GET_LOCATION_BY_ID_ENDPOINT + location.getLocationId()))
            .andExpect(status().isOk())
            .andReturn();

        Location returnedLocation =
            OBJECT_MAPPER.readValue(mvcResult.getResponse().getContentAsString(), Location.class);

        assertEquals(location, returnedLocation, "Returned location matches expected location");
    }

    @Test
    void testGetLocationByIdReturnsNotFound() throws Exception {
        int unknownID = 1234;

        MvcResult mvcResult = mockMvc.perform(get(GET_LOCATION_BY_ID_ENDPOINT + unknownID))
            .andExpect(status().isNotFound())
            .andReturn();

        ExceptionResponse exceptionResponse = OBJECT_MAPPER.readValue(
            mvcResult.getResponse().getContentAsString(), ExceptionResponse.class);

        assertEquals("No location found with the id: " + unknownID, exceptionResponse.getMessage(),
                     "Unexpected error message returned when location by ID not found"
        );
    }

    @Test
    @WithMockUser(username = USERNAME, authorities = {VALID_ROLE})
    @Deprecated
    void testGetLocationByNameReturnsSuccess() throws Exception {
        List<Location> locations = createLocations(LOCATIONS_CSV);

        Location location = locations.get(0);

        MvcResult mvcResult = mockMvc.perform(get(String.format(GET_LOCATION_BY_NAME_ENDPOINT,
                                                                location.getName(), ENGLISH_LANGUAGE_PARAM_VALUE
            )))
            .andExpect(status().isOk())
            .andReturn();

        Location returnedLocation = OBJECT_MAPPER.readValue(
            mvcResult.getResponse().getContentAsString(),
            Location.class
        );

        assertEquals(location, returnedLocation, VALIDATION_UNKNOWN_LOCATION);
    }

    @Test
    @Deprecated
    void testGetLocationByNameReturnsNotFound() throws Exception {
        MvcResult mvcResult = mockMvc.perform(get(String.format(GET_LOCATION_BY_NAME_ENDPOINT,
                                                                INVALID_LOCATION_NAME, ENGLISH_LANGUAGE_PARAM_VALUE
            )))
            .andExpect(status().isNotFound())
            .andReturn();

        ExceptionResponse exceptionResponse =
            OBJECT_MAPPER.readValue(mvcResult.getResponse().getContentAsString(), ExceptionResponse.class);

        assertEquals(NO_LOCATION_FOUND_ERROR, exceptionResponse.getMessage(), LOCATION_NAME_SEARCH_MESSAGE);
    }

    @Test
    @WithMockUser(username = USERNAME, authorities = {VALID_ROLE})
    @Deprecated
    void testGetWelshLocationByNameReturnsSuccess() throws Exception {
        List<Location> locations = createLocations(LOCATIONS_CSV);

        Location location = locations.get(0);

        MvcResult mvcResult = mockMvc.perform(get(String.format(GET_LOCATION_BY_NAME_ENDPOINT,
                                                                location.getWelshName(), WELSH_LANGUAGE_PARAM_VALUE
            )))
            .andExpect(status().isOk())
            .andReturn();

        Location returnedLocation = OBJECT_MAPPER.readValue(
            mvcResult.getResponse().getContentAsString(),
            Location.class
        );

        assertEquals(location, returnedLocation, VALIDATION_UNKNOWN_LOCATION);
    }

    @Test
    @Deprecated
    void testGetWelshLocationByNameReturnsNotFound() throws Exception {
        MvcResult mvcResult = mockMvc.perform(get(String.format(GET_LOCATION_BY_NAME_ENDPOINT,
                                                                INVALID_LOCATION_NAME, WELSH_LANGUAGE_PARAM_VALUE
            )))
            .andExpect(status().isNotFound())
            .andReturn();

        ExceptionResponse exceptionResponse =
            OBJECT_MAPPER.readValue(mvcResult.getResponse().getContentAsString(), ExceptionResponse.class);

        assertEquals(NO_LOCATION_FOUND_ERROR, exceptionResponse.getMessage(), LOCATION_NAME_SEARCH_MESSAGE);
    }

    @Test
    @WithMockUser(username = USERNAME, authorities = {VALID_ROLE})
    void testGetLocationByNameV2ReturnsSuccess() throws Exception {
        List<Location> locations = createLocations(LOCATIONS_CSV);

        Location location = locations.get(0);

        MvcResult mvcResult = mockMvc.perform(
                get(GET_LOCATION_BY_NAME_V2_ENDPOINT)
                    .param(LOCATION_NAME_PARAM, location.getName())
                    .param(LANGUAGE_PARAM, ENGLISH_LANGUAGE_PARAM_VALUE))
            .andExpect(status().isOk())
            .andReturn();

        Location returnedLocation = OBJECT_MAPPER.readValue(
            mvcResult.getResponse().getContentAsString(),
            Location.class
        );

        assertEquals(location, returnedLocation, VALIDATION_UNKNOWN_LOCATION);
    }

    @Test
    void testGetLocationByNameV2ReturnsNotFound() throws Exception {

        MvcResult mvcResult = mockMvc.perform(
            get(GET_LOCATION_BY_NAME_V2_ENDPOINT)
                .param(LOCATION_NAME_PARAM, INVALID_LOCATION_NAME)
                .param(LANGUAGE_PARAM, ENGLISH_LANGUAGE_PARAM_VALUE))
            .andExpect(status().isNotFound())
            .andReturn();

        ExceptionResponse exceptionResponse =
            OBJECT_MAPPER.readValue(mvcResult.getResponse().getContentAsString(), ExceptionResponse.class);

        assertEquals(NO_LOCATION_FOUND_ERROR, exceptionResponse.getMessage(), LOCATION_NAME_SEARCH_MESSAGE);
    }

    @Test
    @WithMockUser(username = USERNAME, authorities = {VALID_ROLE})
    void testGetWelshLocationByNameV2ReturnsSuccess() throws Exception {
        List<Location> locations = createLocations(LOCATIONS_CSV);

        Location location = locations.get(0);

        MvcResult mvcResult = mockMvc.perform(
                get(GET_LOCATION_BY_NAME_V2_ENDPOINT)
                    .param(LOCATION_NAME_PARAM, location.getWelshName())
                    .param(LANGUAGE_PARAM, WELSH_LANGUAGE_PARAM_VALUE))
            .andExpect(status().isOk())
            .andReturn();

        Location returnedLocation = OBJECT_MAPPER.readValue(
            mvcResult.getResponse().getContentAsString(),
            Location.class
        );

        assertEquals(location, returnedLocation, VALIDATION_UNKNOWN_LOCATION);
    }

    @Test
    void testGetWelshLocationByNameV2ReturnsNotFound() throws Exception {
        MvcResult mvcResult = mockMvc.perform(
                get(GET_LOCATION_BY_NAME_V2_ENDPOINT)
                    .param(LOCATION_NAME_PARAM, INVALID_LOCATION_NAME)
                    .param(LANGUAGE_PARAM, WELSH_LANGUAGE_PARAM_VALUE))
            .andExpect(status().isNotFound())
            .andReturn();

        ExceptionResponse exceptionResponse =
            OBJECT_MAPPER.readValue(mvcResult.getResponse().getContentAsString(), ExceptionResponse.class);

        assertEquals(NO_LOCATION_FOUND_ERROR, exceptionResponse.getMessage(), LOCATION_NAME_SEARCH_MESSAGE);
    }

    @Test
    @WithMockUser(username = USERNAME, authorities = {VALID_ROLE})
    void testCreateLocationsCoreData() throws Exception {
        List<Location> createdLocations = createLocations(LOCATIONS_CSV);

        assertEquals(4, createdLocations.size(), VALIDATION_UNEXPECTED_NUMBER_OF_LOCATIONS);

        Location locationA = createdLocations.get(0);
        assertEquals("Test Location", locationA.getName(), VALIDATION_LOCATION_NAME_NOT_AS_EXPECTED);
        assertEquals(List.of("North West"), locationA.getRegion(), "Location region is not as expected");
        assertEquals(LocationType.VENUE, locationA.getLocationType(), "Location type is not as expected");

        List<String> jurisdictions = locationA.getJurisdiction();
        assertEquals(2, jurisdictions.size(), "Unexpected number of jurisdictions returned");
        assertTrue(jurisdictions.contains("Family Location"), "Family Location not within jurisdiction field");
        assertTrue(jurisdictions.contains("Crime"), "Crime not within jurisdiction field");

        List<String> jurisdictionTypes = locationA.getJurisdictionType();
        assertTrue(
            jurisdictionTypes.contains("Magistrates Location"),
            "Magistrates Location not within jurisdiction type field"
        );
    }

    @Test
    @WithMockUser(username = USERNAME, authorities = {VALID_ROLE})
    void testCreateLocationsReferenceData() throws Exception {
        List<Location> createdLocations = createLocations(LOCATIONS_CSV);

        assertEquals(4, createdLocations.size(), VALIDATION_UNEXPECTED_NUMBER_OF_LOCATIONS);

        Location locationA = createdLocations.get(0);
        List<LocationReference> locationReferenceList = locationA.getLocationReferenceList();

        assertEquals(2, locationReferenceList.size(),
                     "Unexpected number of location references returned"
        );

        LocationReference locationReferenceOne = locationReferenceList.get(0);
        assertEquals("TestProvenance", locationReferenceOne.getProvenance(),
                     "Unexpected provenance name returned"
        );
        assertEquals("1", locationReferenceOne.getProvenanceLocationId(),
                     "Unexpected provenance id returned"
        );
        assertEquals(LocationType.VENUE, locationReferenceOne.getProvenanceLocationType(),
                     "Unexpected provenance location type returned"
        );

        LocationReference locationReferenceTwo = locationReferenceList.get(1);
        assertEquals("TestProvenanceOther", locationReferenceTwo.getProvenance(),
                     "Unexpected provenance name returned"
        );
        assertEquals("2", locationReferenceTwo.getProvenanceLocationId(),
                     "Unexpected provenance id returned"
        );
    }

    @Test
    @WithMockUser(username = USERNAME, authorities = {VALID_ROLE})
    void testCreateLocationsDataWithSecondChange() throws Exception {
        createLocations(LOCATIONS_CSV);
        createLocations(UPDATED_CSV);

        MvcResult mvcResult = mockMvc.perform(get(ROOT_URL))
            .andExpect(status().isOk())
            .andReturn();

        Location[] arrayLocations =
            OBJECT_MAPPER.readValue(mvcResult.getResponse().getContentAsString(), Location[].class);

        assertEquals(4, arrayLocations.length, VALIDATION_UNEXPECTED_NUMBER_OF_LOCATIONS);

        mvcResult = mockMvc.perform(get(GET_LOCATION_BY_ID_ENDPOINT + "1"))
            .andExpect(status().isOk())
            .andReturn();

        Location returnedLocation =
            OBJECT_MAPPER.readValue(mvcResult.getResponse().getContentAsString(), Location.class);

        assertEquals("Updated Location", returnedLocation.getName(), VALIDATION_LOCATION_NAME_NOT_AS_EXPECTED);
        List<LocationReference> locationReferenceList = returnedLocation.getLocationReferenceList();

        assertEquals(1, locationReferenceList.size(),
                     "Unexpected number of location references returned"
        );

        LocationReference locationReferenceOne = locationReferenceList.get(0);
        assertEquals("TestProvenance", locationReferenceOne.getProvenance(),
                     "Unexpected provenance name returned"
        );
        assertEquals("1", locationReferenceOne.getProvenanceLocationId(),
                     "Unexpected provenance id returned"
        );
        assertEquals(LocationType.VENUE, locationReferenceOne.getProvenanceLocationType(),
                     "Unexpected provenance location type returned"
        );

        Location location1 = arrayLocations[0];
        assertEquals("Test Location Other", location1.getName(), VALIDATION_LOCATION_NAME_NOT_AS_EXPECTED);

        Location location2 = arrayLocations[1];
        assertEquals("Test Location Other 2", location2.getName(), VALIDATION_LOCATION_NAME_NOT_AS_EXPECTED);

        Location location3 = arrayLocations[2];
        assertEquals("Unknown Location", location3.getName(), VALIDATION_LOCATION_NAME_NOT_AS_EXPECTED);
    }

    @Test
    @WithMockUser(username = USERNAME, authorities = {VALID_ROLE})
    void testCreateLocationsWithCsvContainingExistingLocationName() throws Exception {
        List<Location> createdLocations = createLocations(LOCATIONS_CSV);
        assertEquals(4, createdLocations.size(), VALIDATION_UNEXPECTED_NUMBER_OF_LOCATIONS);

        try (InputStream csvInputStream = this.getClass().getClassLoader()
            .getResourceAsStream(CSV_WITH_EXISTING_LOCATION_NAME)) {
            MockMultipartFile csvFile = new MockMultipartFile(LOCATION_LIST, csvInputStream);

            MvcResult mvcResult = mockMvc.perform(multipart(UPLOAD_API).file(csvFile))
                .andExpect(status().isBadRequest()).andReturn();

            UiExceptionResponse exceptionResponse = OBJECT_MAPPER.readValue(
                mvcResult.getResponse().getContentAsString(), UiExceptionResponse.class
            );

            assertEquals("Failed to upload locations. Location name(s) Test Location already exist",
                         exceptionResponse.getMessage(), "Error message does not match");
            assertTrue(exceptionResponse.isUiError(), "UI error flag does not match");

            mvcResult = mockMvc.perform(get(ROOT_URL))
                .andExpect(status().isOk())
                .andReturn();

            Location[] returnedLocations = OBJECT_MAPPER.readValue(mvcResult.getResponse().getContentAsString(),
                                                                  Location[].class);
            assertEquals(4, returnedLocations.length, VALIDATION_UNEXPECTED_NUMBER_OF_LOCATIONS);
        }
    }

    @Test
    @WithMockUser(username = USERNAME, authorities = {VALID_ROLE})
    void testCreateLocationsWithCsvContainingDuplicatedLocationName() throws Exception {
        try (InputStream csvInputStream = this.getClass().getClassLoader()
                .getResourceAsStream(CSV_WITH_DUPLICATED_LOCATION_NAME)) {
            MockMultipartFile csvFile = new MockMultipartFile(LOCATION_LIST, csvInputStream);

            MvcResult mvcResult = mockMvc.perform(multipart(UPLOAD_API).file(csvFile))
                    .andExpect(status().isBadRequest()).andReturn();

            UiExceptionResponse exceptionResponse = OBJECT_MAPPER.readValue(
                    mvcResult.getResponse().getContentAsString(), UiExceptionResponse.class
            );

            assertEquals("Failed to upload locations. Location name(s) Test Location New already exist",
                    exceptionResponse.getMessage(), "Error message does not match");
            assertTrue(exceptionResponse.isUiError(), "UI error flag does not match");
        }
    }

    @Test
    @WithMockUser(username = USERNAME, authorities = {VALID_ROLE})
    void testInvalidCsv() throws Exception {
        try (InputStream csvInputStream = this.getClass().getClassLoader()
            .getResourceAsStream("location/InvalidCsv.txt")) {
            MockMultipartFile csvFile
                = new MockMultipartFile(LOCATION_LIST, csvInputStream);

            mockMvc.perform(multipart(UPLOAD_API).file(csvFile))
                .andExpect(status().isBadRequest()).andReturn();
        }
    }

    @Test
    @WithMockUser(username = USERNAME, authorities = {VALID_ROLE})
    void testUploadLocations() throws Exception {
        try (InputStream csvInputStream = this.getClass().getClassLoader()
            .getResourceAsStream(LOCATIONS_CSV)) {
            MockMultipartFile csvFile
                = new MockMultipartFile(LOCATION_LIST, csvInputStream);

            MvcResult mvcResult = mockMvc.perform(multipart(UPLOAD_API).file(csvFile))
                .andExpect(status().isOk()).andReturn();

            List<String> inputCsv;
            try (InputStream is = this.getClass().getClassLoader().getResourceAsStream(LOCATIONS_CSV)) {
                inputCsv = new BufferedReader(new InputStreamReader(
                    Objects.requireNonNull(is)))
                    .lines().toList();
            }

            Location[] locationsFromResponse = OBJECT_MAPPER.readValue(
                mvcResult.getResponse().getContentAsString(),
                Location[].class
            );
            List<String> locationsInInputCsv = inputCsv.stream().map(s -> s.split(",")[0]).toList();
            for (Location locationFromResponse : locationsFromResponse) {
                assertTrue(
                    locationsInInputCsv.contains(locationFromResponse.getLocationId().toString()),
                    "Returned location matches input location"
                );
            }
        }
    }

    @Test
    @WithMockUser(username = USERNAME, authorities = {VALID_ROLE})
    void testUploadLocationsBadRequest() throws Exception {
        try (InputStream csvInputStream = this.getClass().getClassLoader()
            .getResourceAsStream(CSV_WITHOUT_LOCATION_TYPE)) {
            MockMultipartFile csvFile
                = new MockMultipartFile(LOCATION_LIST, csvInputStream);

            mockMvc.perform(multipart(UPLOAD_API).file(csvFile))
                .andExpect(status().isBadRequest());
        }
    }

    @Test
    void testUploadLocationsUnauthorised() throws Exception {
        try (InputStream csvInputStream = this.getClass().getClassLoader()
            .getResourceAsStream(LOCATIONS_CSV)) {
            MockMultipartFile csvFile
                = new MockMultipartFile(LOCATION_LIST, csvInputStream);

            mockMvc.perform(multipart(UPLOAD_API).file(csvFile))
                .andExpect(status().isUnauthorized());
        }
    }

    @Test
    @WithMockUser(username = USERNAME, authorities = {VALID_ROLE})
    void testDeleteLocation() throws Exception {
        PiUser piUser = new PiUser();
        piUser.setUserId(USER_ID);
        piUser.setEmail(EMAIL);

        when(accountManagementService.getUserById(USER_ID)).thenReturn(piUser);
        when(accountManagementService.getAllAccounts(anyString(), eq(SYSTEM_ADMIN.toString())))
            .thenReturn(List.of(EMAIL));
        when(accountManagementService.findSubscriptionsByLocationId(any()))
            .thenReturn(Collections.emptyList().toString());

        List<Location> createdLocations = createLocations(DELETE_LOCATIONS_CSV);

        MockHttpServletRequestBuilder mockHttpServletRequestBuilder = MockMvcRequestBuilders
            .delete(GET_LOCATION_BY_ID_ENDPOINT + createdLocations.get(0).getLocationId())
            .header(USER_ID_HEADER, USER_ID);

        mockMvc.perform(mockHttpServletRequestBuilder).andExpect(status().isOk()).andReturn();

        mockMvc.perform(
                get(GET_LOCATION_BY_ID_ENDPOINT + createdLocations.get(0).getLocationId()))
            .andExpect(status().isNotFound())
            .andReturn();
    }

    @Test
    @WithMockUser(username = USERNAME, authorities = {VALID_ROLE})
    void testDeleteLocationNotFound() throws Exception {

        MockHttpServletRequestBuilder mockHttpServletRequestBuilder = MockMvcRequestBuilders
            .delete(GET_LOCATION_BY_ID_ENDPOINT + "1234")
            .header(USER_ID_HEADER, USER_ID);

        mockMvc.perform(mockHttpServletRequestBuilder).andExpect(status().isNotFound()).andReturn();
    }

    @Test
    @WithMockUser(username = "unauthorized_account", authorities = {"APPROLE_unknown.account"})
    void testDeleteLocationNotAuthorised() throws Exception {
        MockHttpServletRequestBuilder mockHttpServletRequestBuilder = MockMvcRequestBuilders
            .delete(GET_LOCATION_BY_ID_ENDPOINT + "1234")
            .header(USER_ID_HEADER, USER_ID);

        mockMvc.perform(mockHttpServletRequestBuilder).andExpect(status().isForbidden()).andReturn();
    }

    @Test
    @WithMockUser(username = USERNAME, authorities = {VALID_ROLE})
    void testDownloadLocations() throws Exception {
        List<Location> createdLocations = createLocations(LOCATIONS_CSV);

        MvcResult mvcResult = mockMvc.perform(
                get(DOWNLOAD_LOCATIONS_ENDPOINT))
            .andExpect(status().isOk())
            .andReturn();
        String returnedLocations = mvcResult.getResponse().getContentAsString();

        for (Location createdLocation : createdLocations) {
            assertTrue(
                returnedLocations.contains(createdLocation.getName()),
                "Location names should match"
            );
        }
    }

    @Test
    void testDownloadLocationsNotAuthorised() throws Exception {
        mockMvc.perform(
                get(DOWNLOAD_LOCATIONS_ENDPOINT))
            .andExpect(status().isUnauthorized());
    }
}


