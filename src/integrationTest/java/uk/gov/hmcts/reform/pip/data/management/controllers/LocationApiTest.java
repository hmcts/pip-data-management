package uk.gov.hmcts.reform.pip.data.management.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.zonky.test.db.AutoConfigureEmbeddedDatabase;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import uk.gov.hmcts.reform.pip.data.management.Application;
import uk.gov.hmcts.reform.pip.data.management.errorhandling.ExceptionResponse;
import uk.gov.hmcts.reform.pip.data.management.models.location.Location;
import uk.gov.hmcts.reform.pip.data.management.models.location.LocationReference;
import uk.gov.hmcts.reform.pip.data.management.utils.IntegrationTestBase;
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
import java.util.function.BiPredicate;

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
@SuppressWarnings({"PMD.TooManyMethods", "PMD.ExcessiveImports"})
class LocationApiTest extends IntegrationTestBase {

    @Autowired
    private MockMvc mockMvc;

    private static ObjectMapper objectMapper;
    private static final String ROOT_URL = "/locations";
    private static final String GET_LOCATION_BY_ID_ENDPOINT = ROOT_URL + "/";
    private static final String GET_LOCATION_BY_NAME_ENDPOINT = ROOT_URL + "/name/%s/language/%s";
    private static final String GET_LOCATION_BY_FILTER_ENDPOINT = ROOT_URL + "/filter";
    private static final String DOWNLOAD_LOCATIONS_ENDPOINT = ROOT_URL + "/download/csv";
    public static final String UPLOAD_API = ROOT_URL + "/upload";
    private static final String LOCATIONS_CSV = "location/ValidCsv.csv";
    private static final String CSV_WITH_EXISTING_LOCATION_NAME = "location/ValidCsvWithExistingLocationName.csv";
    private static final String CSV_WITHOUT_LOCATION_TYPE = "location/InvalidCsvWithoutLocationType.csv";
    private static final String DELETE_LOCATIONS_CSV = "location/ValidCsvForDeleteCourt.csv";
    private static final String UPDATED_CSV = "location/UpdatedCsv.csv";

    private static final String REGIONS_PARAM = "regions";
    private static final String JURISDICTIONS_PARAM = "jurisdictions";
    private static final String LANGUAGE_PARAM = "language";
    private static final String ENGLISH_LANGUAGE_PARAM_VALUE = "eng";
    private static final String WELSH_LANGUAGE_PARAM_VALUE = "cy";

    private static final String VALIDATION_UNKNOWN_LOCATION = "Unexpected location has been returned";
    private static final String VALIDATION_UNEXPECTED_NUMBER_OF_LOCATIONS =
        "Unexpected number of locations has been returned";
    private static final String VALIDATION_LOCATION_NAME_NOT_AS_EXPECTED = "Location name is not as expected";
    private static final int ALL_LOCATIONS = 3;
    private static final String LOCATION_RESULT = "Location has been returned when not expected";

    private static final String USERNAME = "admin";
    private static final String VALID_ROLE = "APPROLE_api.request.admin";
    private static final String LOCATION_LIST = "locationList";
    private static final String EMAIL = "test@justice.gov.uk";
    private static final String REQUESTER_ID_HEADER = "x-requester-id";
    private static final String SYSTEM_ADMIN_ID = UUID.randomUUID().toString();

    private final BiPredicate<Location, Location> compareLocationWithoutReference = (location, otherLocation) ->
        location.getLocationId().equals(otherLocation.getLocationId())
            && location.getName().equals(otherLocation.getName())
            && location.getRegion().equals(otherLocation.getRegion())
            && location.getJurisdiction().equals(otherLocation.getJurisdiction());

    private static PiUser piUser;

    @BeforeAll
    public static void setup() {
        piUser = new PiUser();
        piUser.setUserId(SYSTEM_ADMIN_ID);
        piUser.setEmail(EMAIL);
        piUser.setRoles(SYSTEM_ADMIN);

        objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();
    }

    private List<Location> createLocations(String locationsFile) throws Exception {

        try (InputStream csvInputStream = this.getClass().getClassLoader()
            .getResourceAsStream(locationsFile)) {
            when(accountManagementService.getUserById(any())).thenReturn(piUser);
            MockMultipartFile csvFile
                = new MockMultipartFile(LOCATION_LIST, csvInputStream);

            MvcResult mvcResult = mockMvc.perform(multipart(UPLOAD_API).file(csvFile)
                                                      .header(REQUESTER_ID_HEADER, SYSTEM_ADMIN_ID))
                .andExpect(status().isOk()).andReturn();

            return Arrays.asList(
                objectMapper.readValue(mvcResult.getResponse().getContentAsString(), Location[].class));
        }
    }

    @Test
    @WithMockUser(username = USERNAME, authorities = {VALID_ROLE})
    void testGetAllLocationsReturnsCorrectLocations() throws Exception {
        List<Location> locations = createLocations(LOCATIONS_CSV);

        MvcResult mvcResult = mockMvc.perform(get(ROOT_URL)
            .header(REQUESTER_ID_HEADER, SYSTEM_ADMIN_ID))
            .andExpect(status().isOk())
            .andReturn();

        Location[] arrayLocations =
            objectMapper.readValue(mvcResult.getResponse().getContentAsString(), Location[].class);

        List<Location> returnedLocations = Arrays.asList(arrayLocations);

        assertEquals(ALL_LOCATIONS, returnedLocations.size(), VALIDATION_UNEXPECTED_NUMBER_OF_LOCATIONS);

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

        MvcResult mvcResult = mockMvc.perform(get(GET_LOCATION_BY_ID_ENDPOINT + location.getLocationId())
                                                  .header(REQUESTER_ID_HEADER, SYSTEM_ADMIN_ID))
            .andExpect(status().isOk())
            .andReturn();

        Location returnedLocation =
            objectMapper.readValue(mvcResult.getResponse().getContentAsString(), Location.class);

        assertEquals(location, returnedLocation, "Returned location matches expected location");
    }

    @Test
    void testGetLocationByIdReturnsNotFound() throws Exception {
        int unknownID = 1234;

        MvcResult mvcResult = mockMvc.perform(get(GET_LOCATION_BY_ID_ENDPOINT + unknownID)
                                                  .header(REQUESTER_ID_HEADER, SYSTEM_ADMIN_ID))
            .andExpect(status().isNotFound())
            .andReturn();

        ExceptionResponse exceptionResponse = objectMapper.readValue(
            mvcResult.getResponse().getContentAsString(), ExceptionResponse.class);

        assertEquals("No location found with the id: " + unknownID, exceptionResponse.getMessage(),
                     "Unexpected error message returned when location by ID not found"
        );
    }

    @Test
    @WithMockUser(username = USERNAME, authorities = {VALID_ROLE})
    void testGetLocationByNameReturnsSuccess() throws Exception {
        List<Location> locations = createLocations(LOCATIONS_CSV);

        Location location = locations.get(0);

        MvcResult mvcResult = mockMvc.perform(get(String.format(GET_LOCATION_BY_NAME_ENDPOINT,
                                                                location.getName(), ENGLISH_LANGUAGE_PARAM_VALUE
            )).header(REQUESTER_ID_HEADER, SYSTEM_ADMIN_ID))
            .andExpect(status().isOk())
            .andReturn();

        Location returnedLocation = objectMapper.readValue(
            mvcResult.getResponse().getContentAsString(),
            Location.class
        );

        assertEquals(location, returnedLocation, VALIDATION_UNKNOWN_LOCATION);
    }

    @Test
    void testGetLocationByNameReturnsNotFound() throws Exception {
        String invalidName = "invalid";

        MvcResult mvcResult = mockMvc.perform(get(String.format(GET_LOCATION_BY_NAME_ENDPOINT,
                                                                invalidName, ENGLISH_LANGUAGE_PARAM_VALUE
            )).header(REQUESTER_ID_HEADER, SYSTEM_ADMIN_ID))
            .andExpect(status().isNotFound())
            .andReturn();

        ExceptionResponse exceptionResponse =
            objectMapper.readValue(mvcResult.getResponse().getContentAsString(), ExceptionResponse.class);

        assertEquals("No location found with the name: " + invalidName, exceptionResponse.getMessage(),
                     "Unexpected error message returned when location by name not found"
        );
    }

    @Test
    @WithMockUser(username = USERNAME, authorities = {VALID_ROLE})
    void testGetWelshLocationByNameReturnsSuccess() throws Exception {
        List<Location> locations = createLocations(LOCATIONS_CSV);

        Location location = locations.get(0);

        MvcResult mvcResult = mockMvc.perform(get(String.format(GET_LOCATION_BY_NAME_ENDPOINT,
                                                                location.getWelshName(), WELSH_LANGUAGE_PARAM_VALUE
            )).header(REQUESTER_ID_HEADER, SYSTEM_ADMIN_ID))
            .andExpect(status().isOk())
            .andReturn();

        Location returnedLocation = objectMapper.readValue(
            mvcResult.getResponse().getContentAsString(),
            Location.class
        );

        assertEquals(location, returnedLocation, VALIDATION_UNKNOWN_LOCATION);
    }

    @Test
    void testGetWelshLocationByNameReturnsNotFound() throws Exception {
        String invalidName = "invalid";

        MvcResult mvcResult = mockMvc.perform(get(String.format(GET_LOCATION_BY_NAME_ENDPOINT,
                                                                invalidName, WELSH_LANGUAGE_PARAM_VALUE
            )).header(REQUESTER_ID_HEADER, SYSTEM_ADMIN_ID))
            .andExpect(status().isNotFound())
            .andReturn();

        ExceptionResponse exceptionResponse =
            objectMapper.readValue(mvcResult.getResponse().getContentAsString(), ExceptionResponse.class);

        assertEquals("No location found with the name: " + invalidName, exceptionResponse.getMessage(),
                     "Unexpected error message returned when location by name not found"
        );
    }

    @Test
    @WithMockUser(username = USERNAME, authorities = {VALID_ROLE})
    void testFilterLocationsByRegionReturnsNoResults() throws Exception {
        createLocations(LOCATIONS_CSV);

        MvcResult mvcResult = mockMvc.perform(get(GET_LOCATION_BY_FILTER_ENDPOINT)
                                                  .header(REQUESTER_ID_HEADER, SYSTEM_ADMIN_ID)
                                                  .param("regions", "North South")
                                                  .param(LANGUAGE_PARAM, ENGLISH_LANGUAGE_PARAM_VALUE))
            .andExpect(status().isOk())
            .andReturn();

        List<Location> returnedLocations =
            Arrays.asList(objectMapper.readValue(mvcResult.getResponse().getContentAsString(), Location[].class));

        assertEquals(0, returnedLocations.size(), LOCATION_RESULT);
    }

    @Test
    @WithMockUser(username = USERNAME, authorities = {VALID_ROLE})
    void testWelshFilterLocationsByRegionReturnsNoResults() throws Exception {
        createLocations(LOCATIONS_CSV);

        MvcResult mvcResult = mockMvc.perform(get(GET_LOCATION_BY_FILTER_ENDPOINT)
                                                  .header(REQUESTER_ID_HEADER, SYSTEM_ADMIN_ID)
                                                  .param("regions", "Welsh north South")
                                                  .param(LANGUAGE_PARAM, WELSH_LANGUAGE_PARAM_VALUE))
            .andExpect(status().isOk())
            .andReturn();

        List<Location> returnedLocations =
            Arrays.asList(objectMapper.readValue(mvcResult.getResponse().getContentAsString(), Location[].class));

        assertEquals(0, returnedLocations.size(), LOCATION_RESULT);
    }

    @Test
    @WithMockUser(username = USERNAME, authorities = {VALID_ROLE})
    void testFilterLocationsByJurisdictionReturnsNoResults() throws Exception {
        createLocations(LOCATIONS_CSV);

        MvcResult mvcResult = mockMvc.perform(get(GET_LOCATION_BY_FILTER_ENDPOINT)
                                                  .header(REQUESTER_ID_HEADER, SYSTEM_ADMIN_ID)
                                                  .param("jurisdictions", "Test Jurisdiction")
                                                  .param(LANGUAGE_PARAM, ENGLISH_LANGUAGE_PARAM_VALUE))
            .andExpect(status().isOk())
            .andReturn();

        List<Location> returnedLocations =
            Arrays.asList(objectMapper.readValue(mvcResult.getResponse().getContentAsString(), Location[].class));

        assertEquals(0, returnedLocations.size(), LOCATION_RESULT);
    }

    @Test
    @WithMockUser(username = USERNAME, authorities = {VALID_ROLE})
    void testWelshFilterLocationsByJurisdictionReturnsNoResults() throws Exception {
        createLocations(LOCATIONS_CSV);

        MvcResult mvcResult = mockMvc.perform(get(GET_LOCATION_BY_FILTER_ENDPOINT)
                                                  .header(REQUESTER_ID_HEADER, SYSTEM_ADMIN_ID)
                                                  .param("jurisdictions", "Welsh magistrates")
                                                  .param(LANGUAGE_PARAM, WELSH_LANGUAGE_PARAM_VALUE))
            .andExpect(status().isOk())
            .andReturn();

        List<Location> returnedLocations =
            Arrays.asList(objectMapper.readValue(mvcResult.getResponse().getContentAsString(), Location[].class));

        assertEquals(0, returnedLocations.size(), LOCATION_RESULT);
    }

    @Test
    @WithMockUser(username = USERNAME, authorities = {VALID_ROLE})
    void testFilterLocationsByJurisdictionAndRegion() throws Exception {
        List<Location> locations = createLocations(LOCATIONS_CSV);

        MvcResult mvcResult = mockMvc.perform(get(GET_LOCATION_BY_FILTER_ENDPOINT)
                                                  .header(REQUESTER_ID_HEADER, SYSTEM_ADMIN_ID)
                                                  .param(REGIONS_PARAM, "North West")
                                                  .param(JURISDICTIONS_PARAM, "Magistrates Location")
                                                  .param(LANGUAGE_PARAM, ENGLISH_LANGUAGE_PARAM_VALUE))
            .andExpect(status().isOk())
            .andReturn();

        List<Location> returnedLocations =
            Arrays.asList(objectMapper.readValue(mvcResult.getResponse().getContentAsString(), Location[].class));

        assertEquals(1, returnedLocations.size(), VALIDATION_UNEXPECTED_NUMBER_OF_LOCATIONS);

        assertTrue(
            compareLocationWithoutReference.test(locations.get(0), returnedLocations.get(0)),
            VALIDATION_UNKNOWN_LOCATION
        );
    }

    @Test
    @WithMockUser(username = USERNAME, authorities = {VALID_ROLE})
    void testWelshFilterLocationsByJurisdictionAndRegion() throws Exception {
        List<Location> locations = createLocations(LOCATIONS_CSV);

        MvcResult mvcResult = mockMvc.perform(get(GET_LOCATION_BY_FILTER_ENDPOINT)
                                                  .header(REQUESTER_ID_HEADER, SYSTEM_ADMIN_ID)
                                                  .param(REGIONS_PARAM, "Welsh north west")
                                                  .param(JURISDICTIONS_PARAM, "welsh magistrates")
                                                  .param(LANGUAGE_PARAM, WELSH_LANGUAGE_PARAM_VALUE))
            .andExpect(status().isOk())
            .andReturn();

        List<Location> returnedLocations =
            Arrays.asList(objectMapper.readValue(mvcResult.getResponse().getContentAsString(), Location[].class));

        assertEquals(1, returnedLocations.size(), VALIDATION_UNEXPECTED_NUMBER_OF_LOCATIONS);

        assertTrue(
            compareLocationWithoutReference.test(locations.get(0), returnedLocations.get(0)),
            VALIDATION_UNKNOWN_LOCATION
        );
    }

    @Test
    @WithMockUser(username = USERNAME, authorities = {VALID_ROLE})
    void testFilterByOnlyRegion() throws Exception {
        List<Location> locations = createLocations(LOCATIONS_CSV);

        MvcResult mvcResult = mockMvc.perform(get(GET_LOCATION_BY_FILTER_ENDPOINT)
                                                  .header(REQUESTER_ID_HEADER, SYSTEM_ADMIN_ID)
                                                  .param(REGIONS_PARAM, "South West")
                                                  .param(LANGUAGE_PARAM, ENGLISH_LANGUAGE_PARAM_VALUE))
            .andExpect(status().isOk())
            .andReturn();

        List<Location> returnedLocations =
            Arrays.asList(objectMapper.readValue(mvcResult.getResponse().getContentAsString(), Location[].class));

        assertEquals(1, returnedLocations.size(), VALIDATION_UNEXPECTED_NUMBER_OF_LOCATIONS);

        assertTrue(
            compareLocationWithoutReference.test(locations.get(1), returnedLocations.get(0)),
            VALIDATION_UNKNOWN_LOCATION
        );
    }

    @Test
    @WithMockUser(username = USERNAME, authorities = {VALID_ROLE})
    void testWelshFilterByOnlyRegion() throws Exception {
        createLocations(LOCATIONS_CSV);

        MvcResult mvcResult = mockMvc.perform(get(GET_LOCATION_BY_FILTER_ENDPOINT)
                                                  .header(REQUESTER_ID_HEADER, SYSTEM_ADMIN_ID)
                                                  .param(REGIONS_PARAM, "Welsh north west")
                                                  .param(LANGUAGE_PARAM, WELSH_LANGUAGE_PARAM_VALUE))
            .andExpect(status().isOk())
            .andReturn();

        List<Location> returnedLocations =
            Arrays.asList(objectMapper.readValue(mvcResult.getResponse().getContentAsString(), Location[].class));

        assertEquals(1, returnedLocations.size(), VALIDATION_UNEXPECTED_NUMBER_OF_LOCATIONS);
    }

    @Test
    @WithMockUser(username = USERNAME, authorities = {VALID_ROLE})
    void testFilterByMultipleRegions() throws Exception {
        List<Location> locations = createLocations(LOCATIONS_CSV);

        MvcResult mvcResult = mockMvc.perform(get(GET_LOCATION_BY_FILTER_ENDPOINT)
                                                  .header(REQUESTER_ID_HEADER, SYSTEM_ADMIN_ID)
                                                  .param(REGIONS_PARAM, "South West,North West")
                                                  .param(LANGUAGE_PARAM, ENGLISH_LANGUAGE_PARAM_VALUE))
            .andExpect(status().isOk())
            .andReturn();

        List<Location> returnedLocations =
            Arrays.asList(objectMapper.readValue(mvcResult.getResponse().getContentAsString(), Location[].class));

        assertEquals(2, returnedLocations.size(), VALIDATION_UNEXPECTED_NUMBER_OF_LOCATIONS);

        assertTrue(
            compareLocationWithoutReference.test(locations.get(0), returnedLocations.get(0)),
            VALIDATION_UNKNOWN_LOCATION
        );
        assertTrue(
            compareLocationWithoutReference.test(locations.get(1), returnedLocations.get(1)),
            VALIDATION_UNKNOWN_LOCATION
        );
    }

    @Test
    @WithMockUser(username = USERNAME, authorities = {VALID_ROLE})
    void testWelshFilterByMultipleRegions() throws Exception {
        List<Location> locations = createLocations(LOCATIONS_CSV);

        MvcResult mvcResult = mockMvc.perform(get(GET_LOCATION_BY_FILTER_ENDPOINT)
                                                  .header(REQUESTER_ID_HEADER, SYSTEM_ADMIN_ID)
                                                  .param(REGIONS_PARAM, "Welsh north west,Welsh south west")
                                                  .param(LANGUAGE_PARAM, WELSH_LANGUAGE_PARAM_VALUE))
            .andExpect(status().isOk())
            .andReturn();

        List<Location> returnedLocations =
            Arrays.asList(objectMapper.readValue(mvcResult.getResponse().getContentAsString(), Location[].class));

        assertEquals(2, returnedLocations.size(), VALIDATION_UNEXPECTED_NUMBER_OF_LOCATIONS);

        assertTrue(
            compareLocationWithoutReference.test(locations.get(0), returnedLocations.get(0)),
            VALIDATION_UNKNOWN_LOCATION
        );
        assertTrue(
            compareLocationWithoutReference.test(locations.get(1), returnedLocations.get(1)),
            VALIDATION_UNKNOWN_LOCATION
        );
    }

    @Test
    @WithMockUser(username = USERNAME, authorities = {VALID_ROLE})
    void testFilterByOnlyJurisdiction() throws Exception {
        List<Location> locations = createLocations(LOCATIONS_CSV);

        MvcResult mvcResult = mockMvc.perform(get(GET_LOCATION_BY_FILTER_ENDPOINT)
                                                  .header(REQUESTER_ID_HEADER, SYSTEM_ADMIN_ID)
                                                  .param(JURISDICTIONS_PARAM, "Magistrates Location")
                                                  .param(LANGUAGE_PARAM, ENGLISH_LANGUAGE_PARAM_VALUE))
            .andExpect(status().isOk())
            .andReturn();

        List<Location> returnedLocations =
            Arrays.asList(objectMapper.readValue(mvcResult.getResponse().getContentAsString(), Location[].class));

        assertEquals(1, returnedLocations.size(), VALIDATION_UNEXPECTED_NUMBER_OF_LOCATIONS);

        assertTrue(
            compareLocationWithoutReference.test(locations.get(0), returnedLocations.get(0)),
            VALIDATION_UNKNOWN_LOCATION
        );
    }

    @Test
    @WithMockUser(username = USERNAME, authorities = {VALID_ROLE})
    void testWelshFilterByOnlyJurisdiction() throws Exception {
        List<Location> locations = createLocations(LOCATIONS_CSV);

        MvcResult mvcResult = mockMvc.perform(get(GET_LOCATION_BY_FILTER_ENDPOINT)
                                                  .header(REQUESTER_ID_HEADER, SYSTEM_ADMIN_ID)
                                                  .param(JURISDICTIONS_PARAM, "welsh magistrates")
                                                  .param(LANGUAGE_PARAM, WELSH_LANGUAGE_PARAM_VALUE))
            .andExpect(status().isOk())
            .andReturn();

        List<Location> returnedLocations =
            Arrays.asList(objectMapper.readValue(mvcResult.getResponse().getContentAsString(), Location[].class));

        assertEquals(1, returnedLocations.size(), VALIDATION_UNEXPECTED_NUMBER_OF_LOCATIONS);

        assertTrue(
            compareLocationWithoutReference.test(locations.get(0), returnedLocations.get(0)),
            VALIDATION_UNKNOWN_LOCATION
        );
    }

    @Test
    @WithMockUser(username = USERNAME, authorities = {VALID_ROLE})
    void testFilterByMultipleJurisdictions() throws Exception {
        List<Location> locations = createLocations(LOCATIONS_CSV);

        MvcResult mvcResult = mockMvc.perform(get(GET_LOCATION_BY_FILTER_ENDPOINT)
                                                  .header(REQUESTER_ID_HEADER, SYSTEM_ADMIN_ID)
                                                  .param(
                                                      JURISDICTIONS_PARAM,
                                                      "Magistrates Location,Family Location"
                                                  )
                                                  .param(LANGUAGE_PARAM, ENGLISH_LANGUAGE_PARAM_VALUE))
            .andExpect(status().isOk())
            .andReturn();

        List<Location> returnedLocations =
            Arrays.asList(objectMapper.readValue(mvcResult.getResponse().getContentAsString(), Location[].class));

        assertEquals(2, returnedLocations.size(), VALIDATION_UNEXPECTED_NUMBER_OF_LOCATIONS);

        assertTrue(
            compareLocationWithoutReference.test(locations.get(0), returnedLocations.get(0)),
            VALIDATION_UNKNOWN_LOCATION
        );
        assertTrue(
            compareLocationWithoutReference.test(locations.get(1), returnedLocations.get(1)),
            VALIDATION_UNKNOWN_LOCATION
        );
    }

    @Test
    @WithMockUser(username = USERNAME, authorities = {VALID_ROLE})
    void testWelshFilterByMultipleJurisdictions() throws Exception {
        List<Location> locations = createLocations(LOCATIONS_CSV);

        MvcResult mvcResult = mockMvc.perform(get(GET_LOCATION_BY_FILTER_ENDPOINT)
                                                  .header(REQUESTER_ID_HEADER, SYSTEM_ADMIN_ID)
                                                  .param(JURISDICTIONS_PARAM, "welsh magistrates,welsh family")
                                                  .param(LANGUAGE_PARAM, WELSH_LANGUAGE_PARAM_VALUE))
            .andExpect(status().isOk())
            .andReturn();

        List<Location> returnedLocations =
            Arrays.asList(objectMapper.readValue(mvcResult.getResponse().getContentAsString(), Location[].class));

        assertEquals(2, returnedLocations.size(), VALIDATION_UNEXPECTED_NUMBER_OF_LOCATIONS);

        assertTrue(
            compareLocationWithoutReference.test(locations.get(0), returnedLocations.get(0)),
            VALIDATION_UNKNOWN_LOCATION
        );
        assertTrue(
            compareLocationWithoutReference.test(locations.get(1), returnedLocations.get(1)),
            VALIDATION_UNKNOWN_LOCATION
        );
    }

    @Test
    @WithMockUser(username = USERNAME, authorities = {VALID_ROLE})
    void testFilterByNoRegionOrJurisdiction() throws Exception {
        final List<Location> locations = createLocations(LOCATIONS_CSV);

        MvcResult mvcResult = mockMvc.perform(get(GET_LOCATION_BY_FILTER_ENDPOINT)
                                                  .header(REQUESTER_ID_HEADER, SYSTEM_ADMIN_ID))
            .andExpect(status().isOk())
            .andReturn();

        List<Location> returnedLocations =
            Arrays.asList(objectMapper.readValue(mvcResult.getResponse().getContentAsString(), Location[].class));

        assertEquals(ALL_LOCATIONS, returnedLocations.size(), VALIDATION_UNEXPECTED_NUMBER_OF_LOCATIONS);

        assertTrue(
            compareLocationWithoutReference.test(locations.get(0), returnedLocations.get(0)),
            VALIDATION_UNKNOWN_LOCATION
        );
        assertTrue(
            compareLocationWithoutReference.test(locations.get(1), returnedLocations.get(1)),
            VALIDATION_UNKNOWN_LOCATION
        );
        assertTrue(
            compareLocationWithoutReference.test(locations.get(2), returnedLocations.get(2)),
            VALIDATION_UNKNOWN_LOCATION
        );
    }

    @Test
    @WithMockUser(username = USERNAME, authorities = {VALID_ROLE})
    void testWelshFilterByNoRegionOrJurisdiction() throws Exception {
        final List<Location> locations = createLocations(LOCATIONS_CSV);

        MvcResult mvcResult = mockMvc.perform(get(GET_LOCATION_BY_FILTER_ENDPOINT)
                                                  .header(REQUESTER_ID_HEADER, SYSTEM_ADMIN_ID)
                                                  .param(LANGUAGE_PARAM, WELSH_LANGUAGE_PARAM_VALUE))
            .andExpect(status().isOk())
            .andReturn();

        List<Location> returnedLocations =
            Arrays.asList(objectMapper.readValue(mvcResult.getResponse().getContentAsString(), Location[].class));

        assertEquals(ALL_LOCATIONS, returnedLocations.size(), VALIDATION_UNEXPECTED_NUMBER_OF_LOCATIONS);

        assertTrue(
            compareLocationWithoutReference.test(locations.get(0), returnedLocations.get(0)),
            VALIDATION_UNKNOWN_LOCATION
        );
        assertTrue(
            compareLocationWithoutReference.test(locations.get(1), returnedLocations.get(1)),
            VALIDATION_UNKNOWN_LOCATION
        );
        assertTrue(
            compareLocationWithoutReference.test(locations.get(2), returnedLocations.get(2)),
            VALIDATION_UNKNOWN_LOCATION
        );
    }

    @Test
    @WithMockUser(username = USERNAME, authorities = {VALID_ROLE})
    void testCreateLocationsCoreData() throws Exception {
        List<Location> createdLocations = createLocations(LOCATIONS_CSV);

        assertEquals(3, createdLocations.size(), VALIDATION_UNEXPECTED_NUMBER_OF_LOCATIONS);

        Location locationA = createdLocations.get(0);
        assertEquals("Test Location", locationA.getName(), VALIDATION_LOCATION_NAME_NOT_AS_EXPECTED);
        assertEquals(List.of("North West"), locationA.getRegion(), "Location region is not as expected");
        assertEquals(LocationType.VENUE, locationA.getLocationType(), "Location type is not as expected");

        List<String> jurisdictions = locationA.getJurisdiction();
        assertEquals(2, jurisdictions.size(), "Unexpected number of jurisdictions returned");
        assertTrue(
            jurisdictions.contains("Magistrates Location"),
            "Magistrates Location not within jurisdiction field"
        );
        assertTrue(jurisdictions.contains("Family Location"), "Family Location not within jurisdiction field");
    }

    @Test
    @WithMockUser(username = USERNAME, authorities = {VALID_ROLE})
    void testCreateLocationsReferenceData() throws Exception {
        List<Location> createdLocations = createLocations(LOCATIONS_CSV);

        assertEquals(3, createdLocations.size(), VALIDATION_UNEXPECTED_NUMBER_OF_LOCATIONS);

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

        MvcResult mvcResult = mockMvc.perform(get(ROOT_URL)
            .header(REQUESTER_ID_HEADER, SYSTEM_ADMIN_ID))
            .andExpect(status().isOk())
            .andReturn();

        Location[] arrayLocations =
            objectMapper.readValue(mvcResult.getResponse().getContentAsString(), Location[].class);

        assertEquals(3, arrayLocations.length, VALIDATION_UNEXPECTED_NUMBER_OF_LOCATIONS);

        mvcResult = mockMvc.perform(get(GET_LOCATION_BY_ID_ENDPOINT + "1"))
            .andExpect(status().isOk())
            .andReturn();

        Location returnedLocation =
            objectMapper.readValue(mvcResult.getResponse().getContentAsString(), Location.class);

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

        Location locationB = arrayLocations[0];
        assertEquals("Test Location Other", locationB.getName(), VALIDATION_LOCATION_NAME_NOT_AS_EXPECTED);

        Location locationC = arrayLocations[1];
        assertEquals("Unknown Location", locationC.getName(), VALIDATION_LOCATION_NAME_NOT_AS_EXPECTED);
    }

    @Test
    @WithMockUser(username = USERNAME, authorities = {VALID_ROLE})
    void testCreateLocationsWithCsvContainingExistingLocationName() throws Exception {
        List<Location> createdLocations = createLocations(LOCATIONS_CSV);
        assertEquals(3, createdLocations.size(), VALIDATION_UNEXPECTED_NUMBER_OF_LOCATIONS);

        createdLocations = createLocations(CSV_WITH_EXISTING_LOCATION_NAME);
        assertEquals(1, createdLocations.size(), VALIDATION_UNEXPECTED_NUMBER_OF_LOCATIONS);

        MvcResult mvcResult = mockMvc.perform(get(ROOT_URL))
            .andExpect(status().isOk())
            .andReturn();

        Location[] returnedLocations = objectMapper.readValue(mvcResult.getResponse().getContentAsString(),
                                                              Location[].class);

        assertEquals(4, returnedLocations.length, VALIDATION_UNEXPECTED_NUMBER_OF_LOCATIONS);
        assertEquals(1, returnedLocations[0].getLocationId(), VALIDATION_UNKNOWN_LOCATION);
        assertEquals(2, returnedLocations[1].getLocationId(), VALIDATION_UNKNOWN_LOCATION);
        assertEquals(3, returnedLocations[2].getLocationId(), VALIDATION_UNKNOWN_LOCATION);
        assertEquals(13, returnedLocations[3].getLocationId(), VALIDATION_UNKNOWN_LOCATION);
    }

    @Test
    @WithMockUser(username = USERNAME, authorities = {VALID_ROLE})
    void testInvalidCsv() throws Exception {
        when(accountManagementService.getUserById(SYSTEM_ADMIN_ID)).thenReturn(piUser);
        try (InputStream csvInputStream = this.getClass().getClassLoader()
            .getResourceAsStream("location/InvalidCsv.txt")) {
            MockMultipartFile csvFile
                = new MockMultipartFile(LOCATION_LIST, csvInputStream);

            mockMvc.perform(multipart(UPLOAD_API).file(csvFile)
                                .header(REQUESTER_ID_HEADER, SYSTEM_ADMIN_ID))
                .andExpect(status().isBadRequest()).andReturn();
        }
    }

    @Test
    @WithMockUser(username = USERNAME, authorities = {VALID_ROLE})
    void testUploadLocations() throws Exception {
        when(accountManagementService.getUserById(SYSTEM_ADMIN_ID)).thenReturn(piUser);
        try (InputStream csvInputStream = this.getClass().getClassLoader()
            .getResourceAsStream(LOCATIONS_CSV)) {
            MockMultipartFile csvFile
                = new MockMultipartFile(LOCATION_LIST, csvInputStream);

            MvcResult mvcResult = mockMvc.perform(multipart(UPLOAD_API).file(csvFile)
                                                      .header(REQUESTER_ID_HEADER, SYSTEM_ADMIN_ID))
                .andExpect(status().isOk()).andReturn();

            List<String> inputCsv;
            try (InputStream is = this.getClass().getClassLoader().getResourceAsStream(LOCATIONS_CSV)) {
                inputCsv = new BufferedReader(new InputStreamReader(
                    Objects.requireNonNull(is)))
                    .lines().toList();
            }

            Location[] locationsFromResponse = objectMapper.readValue(
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
        when(accountManagementService.getUserById(SYSTEM_ADMIN_ID)).thenReturn(piUser);
        try (InputStream csvInputStream = this.getClass().getClassLoader()
            .getResourceAsStream(CSV_WITHOUT_LOCATION_TYPE)) {
            MockMultipartFile csvFile
                = new MockMultipartFile(LOCATION_LIST, csvInputStream);

            mockMvc.perform(multipart(UPLOAD_API).file(csvFile)
                                .header(REQUESTER_ID_HEADER, SYSTEM_ADMIN_ID))
                .andExpect(status().isBadRequest());
        }
    }

    @Test
    void testUploadLocationsUnauthorised() throws Exception {
        when(accountManagementService.getUserById(SYSTEM_ADMIN_ID)).thenReturn(piUser);
        try (InputStream csvInputStream = this.getClass().getClassLoader()
            .getResourceAsStream(LOCATIONS_CSV)) {
            MockMultipartFile csvFile
                = new MockMultipartFile(LOCATION_LIST, csvInputStream);

            mockMvc.perform(multipart(UPLOAD_API).file(csvFile)
                                .header(REQUESTER_ID_HEADER, UUID.randomUUID().toString()))
                .andExpect(status().isUnauthorized());
        }
    }

    @Test
    @WithMockUser(username = USERNAME, authorities = {VALID_ROLE})
    void testDeleteLocation() throws Exception {
        when(accountManagementService.getUserById(SYSTEM_ADMIN_ID)).thenReturn(piUser);
        when(accountManagementService.getAllAccounts(anyString(), eq(SYSTEM_ADMIN.toString())))
            .thenReturn(List.of(EMAIL));
        when(subscriptionManagementService.findSubscriptionsByLocationId(any()))
            .thenReturn(Collections.emptyList().toString());

        List<Location> createdLocations = createLocations(DELETE_LOCATIONS_CSV);

        MockHttpServletRequestBuilder mockHttpServletRequestBuilder = MockMvcRequestBuilders
            .delete(GET_LOCATION_BY_ID_ENDPOINT + createdLocations.get(0).getLocationId())
            .header(REQUESTER_ID_HEADER, SYSTEM_ADMIN_ID);

        mockMvc.perform(mockHttpServletRequestBuilder).andExpect(status().isOk()).andReturn();

        mockMvc.perform(
                get(GET_LOCATION_BY_ID_ENDPOINT + createdLocations.get(0).getLocationId()))
            .andExpect(status().isNotFound())
            .andReturn();
    }


    @Test
    @WithMockUser(username = USERNAME, authorities = {VALID_ROLE})
    void testDeleteLocationNotFound() throws Exception {
        when(accountManagementService.getUserById(SYSTEM_ADMIN_ID)).thenReturn(piUser);
        MockHttpServletRequestBuilder mockHttpServletRequestBuilder = MockMvcRequestBuilders
            .delete(GET_LOCATION_BY_ID_ENDPOINT + "1234")
            .header(REQUESTER_ID_HEADER, SYSTEM_ADMIN_ID);

        mockMvc.perform(mockHttpServletRequestBuilder).andExpect(status().isNotFound()).andReturn();
    }

    @Test
    @WithMockUser(username = "unauthorized_account", authorities = {"APPROLE_unknown.account"})
    void testDeleteLocationNotAuthorised() throws Exception {
        when(accountManagementService.getUserById(SYSTEM_ADMIN_ID)).thenReturn(piUser);
        MockHttpServletRequestBuilder mockHttpServletRequestBuilder = MockMvcRequestBuilders
            .delete(GET_LOCATION_BY_ID_ENDPOINT + "1234")
            .header(REQUESTER_ID_HEADER, SYSTEM_ADMIN_ID);

        mockMvc.perform(mockHttpServletRequestBuilder).andExpect(status().isForbidden()).andReturn();
    }

    @Test
    @WithMockUser(username = USERNAME, authorities = {VALID_ROLE})
    void testDownloadLocations() throws Exception {
        when(accountManagementService.getUserById(SYSTEM_ADMIN_ID)).thenReturn(piUser);
        List<Location> createdLocations = createLocations(LOCATIONS_CSV);

        MvcResult mvcResult = mockMvc.perform(
                get(DOWNLOAD_LOCATIONS_ENDPOINT)
                    .header(REQUESTER_ID_HEADER, SYSTEM_ADMIN_ID))
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
                get(DOWNLOAD_LOCATIONS_ENDPOINT)
                    .header(REQUESTER_ID_HEADER, SYSTEM_ADMIN_ID))
            .andExpect(status().isUnauthorized());
    }
}


