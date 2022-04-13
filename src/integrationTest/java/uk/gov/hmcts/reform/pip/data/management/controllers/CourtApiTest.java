package uk.gov.hmcts.reform.pip.data.management.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.zonky.test.db.AutoConfigureEmbeddedDatabase;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import uk.gov.hmcts.reform.pip.data.management.Application;
import uk.gov.hmcts.reform.pip.data.management.errorhandling.ExceptionResponse;
import uk.gov.hmcts.reform.pip.data.management.models.court.Court;
import uk.gov.hmcts.reform.pip.data.management.models.court.CourtReference;

import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.function.BiPredicate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.annotation.DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = {Application.class},
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles(profiles = "test")
@AutoConfigureEmbeddedDatabase(type = AutoConfigureEmbeddedDatabase.DatabaseType.POSTGRES)
@DirtiesContext(classMode = BEFORE_EACH_TEST_METHOD)
class CourtApiTest {

    @Autowired
    private MockMvc mockMvc;

    private static ObjectMapper objectMapper;

    private static final String GET_ALL_COURTS_ENDPOINT = "/courts";
    private static final String GET_COURT_BY_ID_ENDPOINT = "/courts/";
    private static final String GET_COURT_BY_NAME_ENDPOINT = "/courts/name/";
    private static final String GET_COURT_BY_FILTER_ENDPOINT = "/courts/filter";

    private static final String REGIONS_PARAM = "regions";
    private static final String JURISDICTIONS_PARAM = "jurisdictions";

    private static final String VALIDATION_UNKNOWN_COURT = "Unexpected court has been returned";
    private static final String VALIDATION_UNEXPECTED_NUMBER_OF_COURTS =
        "Unexpected number of courts has been returned";

    private final BiPredicate<Court, Court> compareCourtWithoutReference = (court, otherCourt) ->
        court.getCourtId().equals(otherCourt.getCourtId())
            && court.getName().equals(otherCourt.getName())
            && court.getRegion().equals(otherCourt.getRegion())
            && court.getJurisdiction().equals(otherCourt.getJurisdiction());

    @BeforeAll
    public static void setup() {
        objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();
    }

    private List<Court> createCourts() throws Exception {

        try (InputStream csvInputStream = this.getClass().getClassLoader()
            .getResourceAsStream("courts/ValidCsv.csv")) {
            MockMultipartFile csvFile
                = new MockMultipartFile("courtList", csvInputStream);

            MvcResult mvcResult = mockMvc.perform(multipart("/courts/upload").file(csvFile))
                .andExpect(status().isOk()).andReturn();

            return Arrays.asList(
                objectMapper.readValue(mvcResult.getResponse().getContentAsString(), Court[].class));
        }
    }

    @Test
    void testGetAllCourtsReturnsCorrectCourts() throws Exception {
        List<Court> courts = createCourts();

        MvcResult mvcResult = mockMvc.perform(get(GET_ALL_COURTS_ENDPOINT))
            .andExpect(status().isOk())
            .andReturn();

        Court[] arrayCourts =
            objectMapper.readValue(mvcResult.getResponse().getContentAsString(), Court[].class);

        List<Court> returnedCourts = Arrays.asList(arrayCourts);

        assertEquals(courts.size(), returnedCourts.size(), VALIDATION_UNEXPECTED_NUMBER_OF_COURTS);

        for (Court court : courts) {
            assertTrue(returnedCourts.stream().anyMatch(x -> compareCourtWithoutReference.test(x, court)),
                       "Expected court not displayed in list");
        }

    }

    @Test
    void testGetCourtByIdReturnsSuccess() throws Exception {
        List<Court> courts = createCourts();

        Court court = courts.get(0);

        MvcResult mvcResult = mockMvc.perform(get(GET_COURT_BY_ID_ENDPOINT + court.getCourtId()))
            .andExpect(status().isOk())
            .andReturn();

        Court returnedCourt =
            objectMapper.readValue(mvcResult.getResponse().getContentAsString(), Court.class);

        assertEquals(court, returnedCourt, "Returned court matches expected court");
    }

    @Test
    void testGetCourtByIdReturnsNotFound() throws Exception {
        int unknownID = 1234;

        MvcResult mvcResult = mockMvc.perform(get(GET_COURT_BY_ID_ENDPOINT + unknownID))
            .andExpect(status().isNotFound())
            .andReturn();

        ExceptionResponse exceptionResponse = objectMapper.readValue(
            mvcResult.getResponse().getContentAsString(), ExceptionResponse.class);

        assertEquals("No court found with the id: " + unknownID, exceptionResponse.getMessage(),
                     "Unexpected error message returned when court by ID not found");
    }

    @Test
    void testGetCourtByNameReturnsSuccess() throws Exception {
        List<Court> courts = createCourts();

        Court court = courts.get(0);

        MvcResult mvcResult = mockMvc.perform(get(GET_COURT_BY_NAME_ENDPOINT + court.getName()))
            .andExpect(status().isOk())
            .andReturn();

        Court returnedCourt = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), Court.class);

        assertEquals(court, returnedCourt, VALIDATION_UNKNOWN_COURT);
    }

    @Test
    void testGetCourtByNameReturnsNotFound() throws Exception {
        String invalidName = "invalid";

        MvcResult mvcResult = mockMvc.perform(get(GET_COURT_BY_NAME_ENDPOINT + invalidName))
            .andExpect(status().isNotFound())
            .andReturn();

        ExceptionResponse exceptionResponse =
            objectMapper.readValue(mvcResult.getResponse().getContentAsString(), ExceptionResponse.class);

        assertEquals("No court found with the name: " + invalidName, exceptionResponse.getMessage(),
                     "Unexpected error message returned when court by name not found");
    }

    @Test
    void testFilterCourtsByRegionReturnsNoResults() throws Exception {
        createCourts();

        MvcResult mvcResult = mockMvc.perform(get(GET_COURT_BY_FILTER_ENDPOINT)
                                                  .param("regions", "North South"))
            .andExpect(status().isOk())
            .andReturn();

        List<Court> returnedCourts =
            Arrays.asList(objectMapper.readValue(mvcResult.getResponse().getContentAsString(), Court[].class));

        assertEquals(0, returnedCourts.size(), "Court has been returned when not expected");
    }

    @Test
    void testFilterCourtsByJurisdictionReturnsNoResults() throws Exception {
        createCourts();

        MvcResult mvcResult = mockMvc.perform(get(GET_COURT_BY_FILTER_ENDPOINT)
                                                  .param("jurisdictions", "Test Jurisdiction"))
            .andExpect(status().isOk())
            .andReturn();

        List<Court> returnedCourts =
            Arrays.asList(objectMapper.readValue(mvcResult.getResponse().getContentAsString(), Court[].class));

        assertEquals(0, returnedCourts.size(), "Court has been returned when not expected");
    }


    @Test
    void testFilterCourtsByJurisdictionAndRegion() throws Exception {
        List<Court> courts = createCourts();

        MvcResult mvcResult = mockMvc.perform(get(GET_COURT_BY_FILTER_ENDPOINT)
                                                  .param(REGIONS_PARAM, "North West")
                                                  .param(JURISDICTIONS_PARAM, "Magistrates Court"))
            .andExpect(status().isOk())
            .andReturn();

        List<Court> returnedCourts =
            Arrays.asList(objectMapper.readValue(mvcResult.getResponse().getContentAsString(), Court[].class));

        assertEquals(1, returnedCourts.size(), VALIDATION_UNEXPECTED_NUMBER_OF_COURTS);

        assertTrue(compareCourtWithoutReference.test(courts.get(0), returnedCourts.get(0)), VALIDATION_UNKNOWN_COURT);
    }

    @Test
    void testFilterByOnlyRegion() throws Exception {
        List<Court> courts = createCourts();

        MvcResult mvcResult = mockMvc.perform(get(GET_COURT_BY_FILTER_ENDPOINT)
                                                  .param(REGIONS_PARAM, "South West"))
            .andExpect(status().isOk())
            .andReturn();

        List<Court> returnedCourts =
            Arrays.asList(objectMapper.readValue(mvcResult.getResponse().getContentAsString(), Court[].class));

        assertEquals(1, returnedCourts.size(), VALIDATION_UNEXPECTED_NUMBER_OF_COURTS);

        assertTrue(compareCourtWithoutReference.test(courts.get(1), returnedCourts.get(0)), VALIDATION_UNKNOWN_COURT);
    }

    @Test
    void testFilterByMultipleRegions() throws Exception {
        List<Court> courts = createCourts();

        MvcResult mvcResult = mockMvc.perform(get(GET_COURT_BY_FILTER_ENDPOINT)
                                                  .param(REGIONS_PARAM, "South West,North West"))
            .andExpect(status().isOk())
            .andReturn();

        List<Court> returnedCourts =
            Arrays.asList(objectMapper.readValue(mvcResult.getResponse().getContentAsString(), Court[].class));

        assertEquals(2, returnedCourts.size(), VALIDATION_UNEXPECTED_NUMBER_OF_COURTS);

        assertTrue(compareCourtWithoutReference.test(courts.get(0), returnedCourts.get(0)), VALIDATION_UNKNOWN_COURT);
        assertTrue(compareCourtWithoutReference.test(courts.get(1), returnedCourts.get(1)), VALIDATION_UNKNOWN_COURT);
    }

    @Test
    void testFilterByOnlyJurisdiction() throws Exception {
        List<Court> courts = createCourts();

        MvcResult mvcResult = mockMvc.perform(get(GET_COURT_BY_FILTER_ENDPOINT)
                                                  .param(JURISDICTIONS_PARAM, "Magistrates Court"))
            .andExpect(status().isOk())
            .andReturn();

        List<Court> returnedCourts =
            Arrays.asList(objectMapper.readValue(mvcResult.getResponse().getContentAsString(), Court[].class));

        assertEquals(1, returnedCourts.size(), VALIDATION_UNEXPECTED_NUMBER_OF_COURTS);

        assertTrue(compareCourtWithoutReference.test(courts.get(0), returnedCourts.get(0)), VALIDATION_UNKNOWN_COURT);
    }

    @Test
    void testFilterByMultipleJurisdictions() throws Exception {
        List<Court> courts = createCourts();

        MvcResult mvcResult = mockMvc.perform(get(GET_COURT_BY_FILTER_ENDPOINT)
                                                  .param(JURISDICTIONS_PARAM, "Magistrates Court,Family Court"))
            .andExpect(status().isOk())
            .andReturn();

        List<Court> returnedCourts =
            Arrays.asList(objectMapper.readValue(mvcResult.getResponse().getContentAsString(), Court[].class));

        assertEquals(2, returnedCourts.size(), VALIDATION_UNEXPECTED_NUMBER_OF_COURTS);

        assertTrue(compareCourtWithoutReference.test(courts.get(0), returnedCourts.get(0)), VALIDATION_UNKNOWN_COURT);
        assertTrue(compareCourtWithoutReference.test(courts.get(1), returnedCourts.get(1)), VALIDATION_UNKNOWN_COURT);
    }

    @Test
    void testFilterByNoRegionOrJurisdiction() throws Exception {
        List<Court> courts = createCourts();

        MvcResult mvcResult = mockMvc.perform(get(GET_COURT_BY_FILTER_ENDPOINT))
            .andExpect(status().isOk())
            .andReturn();

        List<Court> returnedCourts =
            Arrays.asList(objectMapper.readValue(mvcResult.getResponse().getContentAsString(), Court[].class));

        assertEquals(3, returnedCourts.size(), VALIDATION_UNEXPECTED_NUMBER_OF_COURTS);

        assertTrue(compareCourtWithoutReference.test(courts.get(0), returnedCourts.get(0)), VALIDATION_UNKNOWN_COURT);
        assertTrue(compareCourtWithoutReference.test(courts.get(1), returnedCourts.get(1)), VALIDATION_UNKNOWN_COURT);
        assertTrue(compareCourtWithoutReference.test(courts.get(2), returnedCourts.get(2)), VALIDATION_UNKNOWN_COURT);
    }

    @Test
    void testCreateCourtsCoreData() throws Exception {
        List<Court> createdCourts = createCourts();

        assertEquals(3, createdCourts.size(), VALIDATION_UNEXPECTED_NUMBER_OF_COURTS);

        Court courtA = createdCourts.get(0);
        assertEquals("Test Court", courtA.getName(), "Court name is not as expected");
        assertEquals("North West", courtA.getRegion(), "Court region is not as expected");

        List<String> jurisdictions = courtA.getJurisdiction();
        assertEquals(2, jurisdictions.size(), "Unexpected number of jurisdictions returned");
        assertTrue(jurisdictions.contains("Magistrates Court"), "Magistrates Court not within jurisdiction field");
        assertTrue(jurisdictions.contains("Family Court"), "Family Court not within jurisdiction field");
    }

    @Test
    void testCreateCourtsReferenceData() throws Exception {
        List<Court> createdCourts = createCourts();

        assertEquals(3, createdCourts.size(), VALIDATION_UNEXPECTED_NUMBER_OF_COURTS);

        Court courtA = createdCourts.get(0);
        List<CourtReference> courtReferenceList = courtA.getCourtReferenceList();

        assertEquals(2, courtReferenceList.size(), "Unexpected number of court references returned");

        CourtReference courtReferenceOne = courtReferenceList.get(0);
        assertEquals("TestProvenance", courtReferenceOne.getProvenance(), "Unexpected provenance name returned");
        assertEquals("1", courtReferenceOne.getProvenanceId(), "Unexpected provenance id returned");

        CourtReference courtReferenceTwo = courtReferenceList.get(1);
        assertEquals("TestProvenanceOther", courtReferenceTwo.getProvenance(), "Unexpected provenance name returned");
        assertEquals("2", courtReferenceTwo.getProvenanceId(), "Unexpected provenance id returned");
    }

    @Test
    void testInvalidCsv() throws Exception {
        try (InputStream csvInputStream = this.getClass().getClassLoader()
            .getResourceAsStream("courts/InvalidCsv.txt")) {
            MockMultipartFile csvFile
                = new MockMultipartFile("courtList", csvInputStream);

            mockMvc.perform(multipart("/courts/upload").file(csvFile))
                .andExpect(status().isBadRequest()).andReturn();
        }
    }

}
