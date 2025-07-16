package uk.gov.hmcts.reform.pip.data.management.controllers.location;

import io.zonky.test.db.AutoConfigureEmbeddedDatabase;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MvcResult;
import uk.gov.hmcts.reform.pip.data.management.Application;
import uk.gov.hmcts.reform.pip.data.management.models.location.Location;
import uk.gov.hmcts.reform.pip.data.management.utils.LocationIntegrationTestBase;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = {Application.class},
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("integration")
@AutoConfigureEmbeddedDatabase(type = AutoConfigureEmbeddedDatabase.DatabaseType.POSTGRES)
class LocationSearchTest extends LocationIntegrationTestBase {
    private static final String GET_LOCATION_BY_FILTER_ENDPOINT = "/locations/filter";
    private static final String LOCATIONS_CSV = "location/ValidCsv.csv";

    private static final String REGIONS_PARAM = "regions";
    private static final String JURISDICTIONS_PARAM = "jurisdictions";
    private static final String LANGUAGE_PARAM = "language";
    private static final String ENGLISH_LANGUAGE_PARAM_VALUE = "eng";
    private static final String WELSH_LANGUAGE_PARAM_VALUE = "cy";

    private static final String VALIDATION_UNKNOWN_LOCATION = "Unexpected location has been returned";
    private static final String VALIDATION_UNEXPECTED_NUMBER_OF_LOCATIONS =
        "Unexpected number of locations has been returned";
    private static final String LOCATION_RESULT = "Location has been returned when not expected";

    private static final String USERNAME = "admin";
    private static final String VALID_ROLE = "APPROLE_api.request.admin";

    @Test
    @WithMockUser(username = USERNAME, authorities = {VALID_ROLE})
    void testFilterLocationsByRegionReturnsNoResults() throws Exception {
        createLocations(LOCATIONS_CSV);

        MvcResult mvcResult = mockMvc.perform(get(GET_LOCATION_BY_FILTER_ENDPOINT)
                                                  .param("regions", "North South")
                                                  .param(LANGUAGE_PARAM, ENGLISH_LANGUAGE_PARAM_VALUE))
            .andExpect(status().isOk())
            .andReturn();

        List<Location> returnedLocations =
            Arrays.asList(OBJECT_MAPPER.readValue(mvcResult.getResponse().getContentAsString(), Location[].class));

        assertEquals(0, returnedLocations.size(), LOCATION_RESULT);
    }

    @Test
    @WithMockUser(username = USERNAME, authorities = {VALID_ROLE})
    void testWelshFilterLocationsByRegionReturnsNoResults() throws Exception {
        createLocations(LOCATIONS_CSV);

        MvcResult mvcResult = mockMvc.perform(get(GET_LOCATION_BY_FILTER_ENDPOINT)
                                                  .param("regions", "Welsh north South")
                                                  .param(LANGUAGE_PARAM, WELSH_LANGUAGE_PARAM_VALUE))
            .andExpect(status().isOk())
            .andReturn();

        List<Location> returnedLocations =
            Arrays.asList(OBJECT_MAPPER.readValue(mvcResult.getResponse().getContentAsString(), Location[].class));

        assertEquals(0, returnedLocations.size(), LOCATION_RESULT);
    }

    @Test
    @WithMockUser(username = USERNAME, authorities = {VALID_ROLE})
    void testFilterLocationsByJurisdictionReturnsNoResults() throws Exception {
        createLocations(LOCATIONS_CSV);

        MvcResult mvcResult = mockMvc.perform(get(GET_LOCATION_BY_FILTER_ENDPOINT)
                                                  .param("jurisdictions", "Test Jurisdiction")
                                                  .param(LANGUAGE_PARAM, ENGLISH_LANGUAGE_PARAM_VALUE))
            .andExpect(status().isOk())
            .andReturn();

        List<Location> returnedLocations =
            Arrays.asList(OBJECT_MAPPER.readValue(mvcResult.getResponse().getContentAsString(), Location[].class));

        assertEquals(0, returnedLocations.size(), LOCATION_RESULT);
    }

    @Test
    @WithMockUser(username = USERNAME, authorities = {VALID_ROLE})
    void testWelshFilterLocationsByJurisdictionReturnsNoResults() throws Exception {
        createLocations(LOCATIONS_CSV);

        MvcResult mvcResult = mockMvc.perform(get(GET_LOCATION_BY_FILTER_ENDPOINT)
                                                  .param("jurisdictions", "Welsh magistrates")
                                                  .param(LANGUAGE_PARAM, WELSH_LANGUAGE_PARAM_VALUE))
            .andExpect(status().isOk())
            .andReturn();

        List<Location> returnedLocations =
            Arrays.asList(OBJECT_MAPPER.readValue(mvcResult.getResponse().getContentAsString(), Location[].class));

        assertEquals(0, returnedLocations.size(), LOCATION_RESULT);
    }

    @Test
    @WithMockUser(username = USERNAME, authorities = {VALID_ROLE})
    void testFilterLocationsByJurisdictionAndRegion() throws Exception {
        List<Location> locations = createLocations(LOCATIONS_CSV);

        MvcResult mvcResult = mockMvc.perform(get(GET_LOCATION_BY_FILTER_ENDPOINT)
                                                  .param(REGIONS_PARAM, "North West")
                                                  .param(JURISDICTIONS_PARAM, "Magistrates Location")
                                                  .param(LANGUAGE_PARAM, ENGLISH_LANGUAGE_PARAM_VALUE))
            .andExpect(status().isOk())
            .andReturn();

        List<Location> returnedLocations =
            Arrays.asList(OBJECT_MAPPER.readValue(mvcResult.getResponse().getContentAsString(), Location[].class));

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
                                                  .param(REGIONS_PARAM, "Welsh north west")
                                                  .param(JURISDICTIONS_PARAM, "welsh magistrates")
                                                  .param(LANGUAGE_PARAM, WELSH_LANGUAGE_PARAM_VALUE))
            .andExpect(status().isOk())
            .andReturn();

        List<Location> returnedLocations =
            Arrays.asList(OBJECT_MAPPER.readValue(mvcResult.getResponse().getContentAsString(), Location[].class));

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
                                                  .param(REGIONS_PARAM, "South West")
                                                  .param(LANGUAGE_PARAM, ENGLISH_LANGUAGE_PARAM_VALUE))
            .andExpect(status().isOk())
            .andReturn();

        List<Location> returnedLocations =
            Arrays.asList(OBJECT_MAPPER.readValue(mvcResult.getResponse().getContentAsString(), Location[].class));

        assertEquals(2, returnedLocations.size(), VALIDATION_UNEXPECTED_NUMBER_OF_LOCATIONS);

        assertTrue(
            compareLocationWithoutReference.test(locations.get(1), returnedLocations.get(0)),
            VALIDATION_UNKNOWN_LOCATION
        );
        assertTrue(
            compareLocationWithoutReference.test(locations.get(2), returnedLocations.get(1)),
            VALIDATION_UNKNOWN_LOCATION
        );
    }

    @Test
    @WithMockUser(username = USERNAME, authorities = {VALID_ROLE})
    void testWelshFilterByOnlyRegion() throws Exception {
        createLocations(LOCATIONS_CSV);

        MvcResult mvcResult = mockMvc.perform(get(GET_LOCATION_BY_FILTER_ENDPOINT)
                                                  .param(REGIONS_PARAM, "Welsh north west")
                                                  .param(LANGUAGE_PARAM, WELSH_LANGUAGE_PARAM_VALUE))
            .andExpect(status().isOk())
            .andReturn();

        List<Location> returnedLocations =
            Arrays.asList(OBJECT_MAPPER.readValue(mvcResult.getResponse().getContentAsString(), Location[].class));

        assertEquals(1, returnedLocations.size(), VALIDATION_UNEXPECTED_NUMBER_OF_LOCATIONS);
    }

    @Test
    @WithMockUser(username = USERNAME, authorities = {VALID_ROLE})
    void testFilterByMultipleRegions() throws Exception {
        List<Location> locations = createLocations(LOCATIONS_CSV);

        MvcResult mvcResult = mockMvc.perform(get(GET_LOCATION_BY_FILTER_ENDPOINT)
                                                  .param(REGIONS_PARAM, "South West,North West")
                                                  .param(LANGUAGE_PARAM, ENGLISH_LANGUAGE_PARAM_VALUE))
            .andExpect(status().isOk())
            .andReturn();

        List<Location> returnedLocations =
            Arrays.asList(OBJECT_MAPPER.readValue(mvcResult.getResponse().getContentAsString(), Location[].class));

        assertEquals(3, returnedLocations.size(), VALIDATION_UNEXPECTED_NUMBER_OF_LOCATIONS);

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
    void testWelshFilterByMultipleRegions() throws Exception {
        List<Location> locations = createLocations(LOCATIONS_CSV);

        MvcResult mvcResult = mockMvc.perform(get(GET_LOCATION_BY_FILTER_ENDPOINT)
                                                  .param(REGIONS_PARAM, "Welsh north west,Welsh south west")
                                                  .param(LANGUAGE_PARAM, WELSH_LANGUAGE_PARAM_VALUE))
            .andExpect(status().isOk())
            .andReturn();

        List<Location> returnedLocations =
            Arrays.asList(OBJECT_MAPPER.readValue(mvcResult.getResponse().getContentAsString(), Location[].class));

        assertEquals(3, returnedLocations.size(), VALIDATION_UNEXPECTED_NUMBER_OF_LOCATIONS);

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
    void testFilterByOnlyJurisdiction() throws Exception {
        List<Location> locations = createLocations(LOCATIONS_CSV);

        MvcResult mvcResult = mockMvc.perform(get(GET_LOCATION_BY_FILTER_ENDPOINT)
                                                  .param(JURISDICTIONS_PARAM, "Family Location")
                                                  .param(LANGUAGE_PARAM, ENGLISH_LANGUAGE_PARAM_VALUE))
            .andExpect(status().isOk())
            .andReturn();

        List<Location> returnedLocations =
            Arrays.asList(OBJECT_MAPPER.readValue(mvcResult.getResponse().getContentAsString(), Location[].class));

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
    void testWelshFilterByOnlyJurisdiction() throws Exception {
        List<Location> locations = createLocations(LOCATIONS_CSV);

        MvcResult mvcResult = mockMvc.perform(get(GET_LOCATION_BY_FILTER_ENDPOINT)
                                                  .param(JURISDICTIONS_PARAM, "welsh family")
                                                  .param(LANGUAGE_PARAM, WELSH_LANGUAGE_PARAM_VALUE))
            .andExpect(status().isOk())
            .andReturn();

        List<Location> returnedLocations =
            Arrays.asList(OBJECT_MAPPER.readValue(mvcResult.getResponse().getContentAsString(), Location[].class));

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
    void testFilterByMultipleJurisdictions() throws Exception {
        List<Location> locations = createLocations(LOCATIONS_CSV);

        MvcResult mvcResult = mockMvc.perform(get(GET_LOCATION_BY_FILTER_ENDPOINT)
                                                  .param(
                                                      JURISDICTIONS_PARAM,
                                                      "Tribunal,Family Location"
                                                  )
                                                  .param(LANGUAGE_PARAM, ENGLISH_LANGUAGE_PARAM_VALUE))
            .andExpect(status().isOk())
            .andReturn();

        List<Location> returnedLocations =
            Arrays.asList(OBJECT_MAPPER.readValue(mvcResult.getResponse().getContentAsString(), Location[].class));

        assertEquals(3, returnedLocations.size(), VALIDATION_UNEXPECTED_NUMBER_OF_LOCATIONS);

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
    void testWelshFilterByMultipleJurisdictions() throws Exception {
        List<Location> locations = createLocations(LOCATIONS_CSV);

        MvcResult mvcResult = mockMvc.perform(get(GET_LOCATION_BY_FILTER_ENDPOINT)
                                                  .param(JURISDICTIONS_PARAM, "welsh tribunal,welsh family")
                                                  .param(LANGUAGE_PARAM, WELSH_LANGUAGE_PARAM_VALUE))
            .andExpect(status().isOk())
            .andReturn();

        List<Location> returnedLocations =
            Arrays.asList(OBJECT_MAPPER.readValue(mvcResult.getResponse().getContentAsString(), Location[].class));

        assertEquals(3, returnedLocations.size(), VALIDATION_UNEXPECTED_NUMBER_OF_LOCATIONS);

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
    void testFilterByJurisdictionTribunalTypeAndCourtType() throws Exception {
        List<Location> locations = createLocations(LOCATIONS_CSV);

        MvcResult mvcResult = mockMvc.perform(get(GET_LOCATION_BY_FILTER_ENDPOINT)
                                                  .param(
                                                      JURISDICTIONS_PARAM,
                                                      "Family Location,SSCS,Magistrates Location"
                                                  )
                                                  .param(LANGUAGE_PARAM, ENGLISH_LANGUAGE_PARAM_VALUE))
            .andExpect(status().isOk())
            .andReturn();

        List<Location> returnedLocations =
            Arrays.asList(OBJECT_MAPPER.readValue(mvcResult.getResponse().getContentAsString(), Location[].class));

        assertEquals(3, returnedLocations.size(), VALIDATION_UNEXPECTED_NUMBER_OF_LOCATIONS);

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
    void testWelshFilterByJurisdictionTribunalTypeAndCrimeType() throws Exception {
        List<Location> locations = createLocations(LOCATIONS_CSV);

        MvcResult mvcResult = mockMvc.perform(get(GET_LOCATION_BY_FILTER_ENDPOINT)
                                                  .param(JURISDICTIONS_PARAM,
                                                         "welsh family,welsh SSCS,welsh magistrates")
                                                  .param(LANGUAGE_PARAM, WELSH_LANGUAGE_PARAM_VALUE))
            .andExpect(status().isOk())
            .andReturn();

        List<Location> returnedLocations =
            Arrays.asList(OBJECT_MAPPER.readValue(mvcResult.getResponse().getContentAsString(), Location[].class));

        assertEquals(3, returnedLocations.size(), VALIDATION_UNEXPECTED_NUMBER_OF_LOCATIONS);

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
    void testFilterByNoRegionOrJurisdiction() throws Exception {
        final List<Location> locations = createLocations(LOCATIONS_CSV);

        MvcResult mvcResult = mockMvc.perform(get(GET_LOCATION_BY_FILTER_ENDPOINT))
            .andExpect(status().isOk())
            .andReturn();

        List<Location> returnedLocations =
            Arrays.asList(OBJECT_MAPPER.readValue(mvcResult.getResponse().getContentAsString(), Location[].class));

        assertEquals(4, returnedLocations.size(), VALIDATION_UNEXPECTED_NUMBER_OF_LOCATIONS);

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
                                                  .param(LANGUAGE_PARAM, WELSH_LANGUAGE_PARAM_VALUE))
            .andExpect(status().isOk())
            .andReturn();

        List<Location> returnedLocations =
            Arrays.asList(OBJECT_MAPPER.readValue(mvcResult.getResponse().getContentAsString(), Location[].class));

        assertEquals(4, returnedLocations.size(), VALIDATION_UNEXPECTED_NUMBER_OF_LOCATIONS);

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
}


