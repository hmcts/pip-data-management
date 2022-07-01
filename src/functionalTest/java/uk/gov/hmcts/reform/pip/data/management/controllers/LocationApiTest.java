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
import uk.gov.hmcts.reform.pip.data.management.Application;
import uk.gov.hmcts.reform.pip.data.management.errorhandling.ExceptionResponse;
import uk.gov.hmcts.reform.pip.data.management.models.location.Location;
import uk.gov.hmcts.reform.pip.data.management.models.location.LocationReference;
import uk.gov.hmcts.reform.pip.data.management.models.location.LocationType;

import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.function.BiPredicate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.annotation.DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = {Application.class},
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles(profiles = "functional")
@AutoConfigureEmbeddedDatabase(type = AutoConfigureEmbeddedDatabase.DatabaseType.POSTGRES)
@DirtiesContext(classMode = AFTER_EACH_TEST_METHOD)
class LocationApiTest {

    @Autowired
    private MockMvc mockMvc;

    private static ObjectMapper objectMapper;

    private static final String ROOT_URL = "/locations";
    private static final String GET_LOCATION_BY_ID_ENDPOINT = ROOT_URL + "/";
    private static final String GET_LOCATION_BY_NAME_ENDPOINT = ROOT_URL + "/name/";
    private static final String GET_LOCAITON_BY_FILTER_ENDPOINT = ROOT_URL + "/filter";
    public static final String UPLOAD_API = ROOT_URL + "/upload";
    private static final String LOCATIONS_CSV = "location/ValidCsv.csv";
    private static final String UPDATED_CSV = "location/UpdatedCsv.csv";

    private static final String REGIONS_PARAM = "regions";
    private static final String JURISDICTIONS_PARAM = "jurisdictions";

    private static final String VALIDATION_UNKNOWN_LOCATION = "Unexpected location has been returned";
    private static final String VALIDATION_UNEXPECTED_NUMBER_OF_LOCATIONS =
        "Unexpected number of locations has been returned";
    private static final String VALIDATION_LOCATION_NAME_NOT_AS_EXPECTED = "Location name is not as expected";
    private static final int ALL_LOCATIONS = 3;

    private final BiPredicate<Location, Location> compareLocationWithoutReference = (location, otherLocation) ->
        location.getLocationId().equals(otherLocation.getLocationId())
            && location.getName().equals(otherLocation.getName())
            && location.getRegion().equals(otherLocation.getRegion())
            && location.getJurisdiction().equals(otherLocation.getJurisdiction());

    @BeforeAll
    public static void setup() {
        objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();
    }

    private List<Location> createLocations(String locationsFile) throws Exception {

        try (InputStream csvInputStream = this.getClass().getClassLoader()
            .getResourceAsStream(locationsFile)) {
            MockMultipartFile csvFile
                = new MockMultipartFile("locationList", csvInputStream);

            MvcResult mvcResult = mockMvc.perform(multipart(UPLOAD_API).file(csvFile))
                .andExpect(status().isOk()).andReturn();

            return Arrays.asList(
                objectMapper.readValue(mvcResult.getResponse().getContentAsString(), Location[].class));
        }
    }

    @Test
    void testGetAllLocationsReturnsCorrectLocations() throws Exception {
        List<Location> locations = createLocations(LOCATIONS_CSV);

        MvcResult mvcResult = mockMvc.perform(get(ROOT_URL))
            .andExpect(status().isOk())
            .andReturn();

        Location[] arrayLocations =
            objectMapper.readValue(mvcResult.getResponse().getContentAsString(), Location[].class);

        List<Location> returnedLocations = Arrays.asList(arrayLocations);

        assertEquals(ALL_LOCATIONS, returnedLocations.size(), VALIDATION_UNEXPECTED_NUMBER_OF_LOCATIONS);

        for (Location location : locations) {
            assertTrue(
                returnedLocations.stream().anyMatch(x -> compareLocationWithoutReference.test(x, location)),
                "Expected location not displayed in list");
        }

    }

    @Test
    void testGetLocationByIdReturnsSuccess() throws Exception {
        List<Location> locations = createLocations(LOCATIONS_CSV);

        Location location = locations.get(0);

        MvcResult mvcResult = mockMvc.perform(get(GET_LOCATION_BY_ID_ENDPOINT + location.getLocationId()))
            .andExpect(status().isOk())
            .andReturn();

        Location returnedLocation =
            objectMapper.readValue(mvcResult.getResponse().getContentAsString(), Location.class);

        assertEquals(location, returnedLocation, "Returned location matches expected location");
    }

    @Test
    void testGetLocationByIdReturnsNotFound() throws Exception {
        int unknownID = 1234;

        MvcResult mvcResult = mockMvc.perform(get(GET_LOCATION_BY_ID_ENDPOINT + unknownID))
            .andExpect(status().isNotFound())
            .andReturn();

        ExceptionResponse exceptionResponse = objectMapper.readValue(
            mvcResult.getResponse().getContentAsString(), ExceptionResponse.class);

        assertEquals("No location found with the id: " + unknownID, exceptionResponse.getMessage(),
                     "Unexpected error message returned when location by ID not found");
    }

    @Test
    void testGetLocationByNameReturnsSuccess() throws Exception {
        List<Location> locations = createLocations(LOCATIONS_CSV);

        Location location = locations.get(0);

        MvcResult mvcResult = mockMvc.perform(get(GET_LOCATION_BY_NAME_ENDPOINT + location.getName()))
            .andExpect(status().isOk())
            .andReturn();

        Location returnedLocation = objectMapper.readValue(mvcResult.getResponse().getContentAsString(),
                                                           Location.class);

        assertEquals(location, returnedLocation, VALIDATION_UNKNOWN_LOCATION);
    }

    @Test
    void testGetLocationByNameReturnsNotFound() throws Exception {
        String invalidName = "invalid";

        MvcResult mvcResult = mockMvc.perform(get(GET_LOCATION_BY_NAME_ENDPOINT + invalidName))
            .andExpect(status().isNotFound())
            .andReturn();

        ExceptionResponse exceptionResponse =
            objectMapper.readValue(mvcResult.getResponse().getContentAsString(), ExceptionResponse.class);

        assertEquals("No location found with the name: " + invalidName, exceptionResponse.getMessage(),
                     "Unexpected error message returned when location by name not found");
    }

    @Test
    void testFilterLocationsByRegionReturnsNoResults() throws Exception {
        createLocations(LOCATIONS_CSV);

        MvcResult mvcResult = mockMvc.perform(get(GET_LOCAITON_BY_FILTER_ENDPOINT)
                                                  .param("regions", "North South"))
            .andExpect(status().isOk())
            .andReturn();

        List<Location> returnedLocations =
            Arrays.asList(objectMapper.readValue(mvcResult.getResponse().getContentAsString(), Location[].class));

        assertEquals(0, returnedLocations.size(), "Location has been returned when not expected");
    }

    @Test
    void testFilterLocationsByJurisdictionReturnsNoResults() throws Exception {
        createLocations(LOCATIONS_CSV);

        MvcResult mvcResult = mockMvc.perform(get(GET_LOCAITON_BY_FILTER_ENDPOINT)
                                                  .param("jurisdictions", "Test Jurisdiction"))
            .andExpect(status().isOk())
            .andReturn();

        List<Location> returnedLocations =
            Arrays.asList(objectMapper.readValue(mvcResult.getResponse().getContentAsString(), Location[].class));

        assertEquals(0, returnedLocations.size(), "Location has been returned when not expected");
    }


    @Test
    void testFilterLocationsByJurisdictionAndRegion() throws Exception {
        List<Location> locations = createLocations(LOCATIONS_CSV);

        MvcResult mvcResult = mockMvc.perform(get(GET_LOCAITON_BY_FILTER_ENDPOINT)
                            .param(REGIONS_PARAM, "North West")
                            .param(JURISDICTIONS_PARAM, "Magistrates Location"))
            .andExpect(status().isOk())
            .andReturn();

        List<Location> returnedLocations =
            Arrays.asList(objectMapper.readValue(mvcResult.getResponse().getContentAsString(), Location[].class));

        assertEquals(1, returnedLocations.size(), VALIDATION_UNEXPECTED_NUMBER_OF_LOCATIONS);

        assertTrue(compareLocationWithoutReference.test(locations.get(0), returnedLocations.get(0)),
                   VALIDATION_UNKNOWN_LOCATION);
    }

    @Test
    void testFilterByOnlyRegion() throws Exception {
        List<Location> locations = createLocations(LOCATIONS_CSV);

        MvcResult mvcResult = mockMvc.perform(get(GET_LOCAITON_BY_FILTER_ENDPOINT)
                                                  .param(REGIONS_PARAM, "South West"))
            .andExpect(status().isOk())
            .andReturn();

        List<Location> returnedLocations =
            Arrays.asList(objectMapper.readValue(mvcResult.getResponse().getContentAsString(), Location[].class));

        assertEquals(1, returnedLocations.size(), VALIDATION_UNEXPECTED_NUMBER_OF_LOCATIONS);

        assertTrue(compareLocationWithoutReference.test(locations.get(1), returnedLocations.get(0)),
                   VALIDATION_UNKNOWN_LOCATION);
    }

    @Test
    void testFilterByMultipleRegions() throws Exception {
        List<Location> locations = createLocations(LOCATIONS_CSV);

        MvcResult mvcResult = mockMvc.perform(get(GET_LOCAITON_BY_FILTER_ENDPOINT)
                                                  .param(REGIONS_PARAM, "South West,North West"))
            .andExpect(status().isOk())
            .andReturn();

        List<Location> returnedLocations =
            Arrays.asList(objectMapper.readValue(mvcResult.getResponse().getContentAsString(), Location[].class));

        assertEquals(2, returnedLocations.size(), VALIDATION_UNEXPECTED_NUMBER_OF_LOCATIONS);

        assertTrue(compareLocationWithoutReference.test(locations.get(0), returnedLocations.get(0)),
                   VALIDATION_UNKNOWN_LOCATION);
        assertTrue(compareLocationWithoutReference.test(locations.get(1), returnedLocations.get(1)),
                   VALIDATION_UNKNOWN_LOCATION);
    }

    @Test
    void testFilterByOnlyJurisdiction() throws Exception {
        List<Location> locations = createLocations(LOCATIONS_CSV);

        MvcResult mvcResult = mockMvc.perform(get(GET_LOCAITON_BY_FILTER_ENDPOINT)
                                                  .param(JURISDICTIONS_PARAM, "Magistrates Location"))
            .andExpect(status().isOk())
            .andReturn();

        List<Location> returnedLocations =
            Arrays.asList(objectMapper.readValue(mvcResult.getResponse().getContentAsString(), Location[].class));

        assertEquals(1, returnedLocations.size(), VALIDATION_UNEXPECTED_NUMBER_OF_LOCATIONS);

        assertTrue(compareLocationWithoutReference.test(locations.get(0), returnedLocations.get(0)),
                   VALIDATION_UNKNOWN_LOCATION);
    }

    @Test
    void testFilterByMultipleJurisdictions() throws Exception {
        List<Location> locations = createLocations(LOCATIONS_CSV);

        MvcResult mvcResult = mockMvc.perform(get(GET_LOCAITON_BY_FILTER_ENDPOINT)
                                                  .param(JURISDICTIONS_PARAM, "Magistrates Location,Family Location"))
            .andExpect(status().isOk())
            .andReturn();

        List<Location> returnedLocations =
            Arrays.asList(objectMapper.readValue(mvcResult.getResponse().getContentAsString(), Location[].class));

        assertEquals(2, returnedLocations.size(), VALIDATION_UNEXPECTED_NUMBER_OF_LOCATIONS);

        assertTrue(compareLocationWithoutReference.test(locations.get(0), returnedLocations.get(0)),
                   VALIDATION_UNKNOWN_LOCATION);
        assertTrue(compareLocationWithoutReference.test(locations.get(1), returnedLocations.get(1)),
                   VALIDATION_UNKNOWN_LOCATION);
    }

    @Test
    void testFilterByNoRegionOrJurisdiction() throws Exception {
        final List<Location> locations = createLocations(LOCATIONS_CSV);

        MvcResult mvcResult = mockMvc.perform(get(GET_LOCAITON_BY_FILTER_ENDPOINT))
            .andExpect(status().isOk())
            .andReturn();

        List<Location> returnedLocations =
            Arrays.asList(objectMapper.readValue(mvcResult.getResponse().getContentAsString(), Location[].class));

        assertEquals(ALL_LOCATIONS, returnedLocations.size(), VALIDATION_UNEXPECTED_NUMBER_OF_LOCATIONS);

        assertTrue(compareLocationWithoutReference.test(locations.get(0), returnedLocations.get(0)),
                   VALIDATION_UNKNOWN_LOCATION);
        assertTrue(compareLocationWithoutReference.test(locations.get(1), returnedLocations.get(1)),
                   VALIDATION_UNKNOWN_LOCATION);
        assertTrue(compareLocationWithoutReference.test(locations.get(2), returnedLocations.get(2)),
                   VALIDATION_UNKNOWN_LOCATION);
    }

    @Test
    void testCreateLocationsCoreData() throws Exception {
        List<Location> createdLocations = createLocations(LOCATIONS_CSV);

        assertEquals(3, createdLocations.size(), VALIDATION_UNEXPECTED_NUMBER_OF_LOCATIONS);

        Location locationA = createdLocations.get(0);
        assertEquals("Test Location", locationA.getName(), VALIDATION_LOCATION_NAME_NOT_AS_EXPECTED);
        assertEquals(List.of("North West"), locationA.getRegion(), "Location region is not as expected");
        assertEquals(LocationType.VENUE, locationA.getLocationType(), "Location type is not as expected");

        List<String> jurisdictions = locationA.getJurisdiction();
        assertEquals(2, jurisdictions.size(), "Unexpected number of jurisdictions returned");
        assertTrue(jurisdictions.contains("Magistrates Location"),
                   "Magistrates Location not within jurisdiction field");
        assertTrue(jurisdictions.contains("Family Location"), "Family Location not within jurisdiction field");
    }

    @Test
    void testCreateLocationsReferenceData() throws Exception {
        List<Location> createdLocations = createLocations(LOCATIONS_CSV);

        assertEquals(3, createdLocations.size(), VALIDATION_UNEXPECTED_NUMBER_OF_LOCATIONS);

        Location locationA = createdLocations.get(0);
        List<LocationReference> locationReferenceList = locationA.getLocationReferenceList();

        assertEquals(2, locationReferenceList.size(), "Unexpected number of location references returned");

        LocationReference locationReferenceOne = locationReferenceList.get(0);
        assertEquals("TestProvenance", locationReferenceOne.getProvenance(), "Unexpected provenance name returned");
        assertEquals("1", locationReferenceOne.getProvenanceLocationId(), "Unexpected provenance id returned");
        assertEquals(LocationType.VENUE, locationReferenceOne.getProvenanceLocationType(),
                     "Unexpected provenance location type returned");

        LocationReference locationReferenceTwo = locationReferenceList.get(1);
        assertEquals("TestProvenanceOther", locationReferenceTwo.getProvenance(),
                     "Unexpected provenance name returned");
        assertEquals("2", locationReferenceTwo.getProvenanceLocationId(), "Unexpected provenance id returned");
    }

    @Test
    void testCreateLocationsDataWithSecondChange() throws Exception {
        createLocations(LOCATIONS_CSV);
        createLocations(UPDATED_CSV);

        MvcResult mvcResult = mockMvc.perform(get(ROOT_URL))
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

        assertEquals(1, locationReferenceList.size(), "Unexpected number of location references returned");

        LocationReference locationReferenceOne = locationReferenceList.get(0);
        assertEquals("TestProvenance", locationReferenceOne.getProvenance(), "Unexpected provenance name returned");
        assertEquals("1", locationReferenceOne.getProvenanceLocationId(), "Unexpected provenance id returned");
        assertEquals(LocationType.VENUE, locationReferenceOne.getProvenanceLocationType(),
                     "Unexpected provenance location type returned");

        Location locationB = arrayLocations[0];
        assertEquals("Test Location Other", locationB.getName(), VALIDATION_LOCATION_NAME_NOT_AS_EXPECTED);

        Location locationC = arrayLocations[1];
        assertEquals("Unknown Location", locationC.getName(), VALIDATION_LOCATION_NAME_NOT_AS_EXPECTED);
    }

    @Test
    void testInvalidCsv() throws Exception {
        try (InputStream csvInputStream = this.getClass().getClassLoader()
            .getResourceAsStream("location/InvalidCsv.txt")) {
            MockMultipartFile csvFile
                = new MockMultipartFile("locationList", csvInputStream);

            mockMvc.perform(multipart(UPLOAD_API).file(csvFile))
                .andExpect(status().isBadRequest()).andReturn();
        }
    }

    @Test
    @WithMockUser(username = "admin", authorities = { "APPROLE_api.request.admin" })
    void testDeleteLocation() throws Exception {
        List<Location> createdLocations = createLocations(LOCATIONS_CSV);

        MvcResult mvcResult = mockMvc.perform(
            delete(GET_LOCATION_BY_ID_ENDPOINT + createdLocations.get(0).getLocationId()))
            .andExpect(status().isOk())
            .andReturn();

        assertEquals("Location with id 1 has been deleted", mvcResult.getResponse().getContentAsString(),
                     "Response does not match expected response");

        mockMvc.perform(
            get(GET_LOCATION_BY_ID_ENDPOINT + createdLocations.get(0).getLocationId()))
            .andExpect(status().isNotFound())
            .andReturn();
    }

    @Test
    @WithMockUser(username = "admin", authorities = { "APPROLE_api.request.admin" })
    void testDeleteLocationNotFound() throws Exception {
        mockMvc.perform(
            get(GET_LOCATION_BY_ID_ENDPOINT + "1234"))
            .andExpect(status().isNotFound())
            .andReturn();
    }
}
