package uk.gov.hmcts.reform.pip.data.management.controllers.publication;

import io.zonky.test.db.AutoConfigureEmbeddedDatabase;
import org.json.JSONArray;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import uk.gov.hmcts.reform.pip.data.management.Application;
import uk.gov.hmcts.reform.pip.data.management.models.publication.Artefact;
import uk.gov.hmcts.reform.pip.data.management.utils.PublicationIntegrationTestBase;
import uk.gov.hmcts.reform.pip.model.account.PiUser;
import uk.gov.hmcts.reform.pip.model.location.LocationType;
import uk.gov.hmcts.reform.pip.model.publication.ListType;
import uk.gov.hmcts.reform.pip.model.publication.Sensitivity;

import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.pip.model.account.Roles.SYSTEM_ADMIN;

@SpringBootTest(classes = {Application.class},
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("integration")
@AutoConfigureEmbeddedDatabase(type = AutoConfigureEmbeddedDatabase.DatabaseType.POSTGRES)
@WithMockUser(username = "admin", authorities = {"APPROLE_api.request.admin"})
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class PublicationLocationTest extends PublicationIntegrationTestBase {
    private static final String PUBLICATION_URL = "/publication";
    private static final String LOCATION_TYPE_URL = PUBLICATION_URL + "/location-type/";
    private static final String COUNT_ENDPOINT = PUBLICATION_URL + "/count-by-location";
    private static final String USER_ID = UUID.randomUUID().toString();
    private static final String COURT_ID = "1";
    private static final String USER_ID_HEADER = "x-user-id";
    private static final String EMAIL = "test@email.com";
    private static final LocalDateTime DISPLAY_TO = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
    private static final LocalDateTime DISPLAY_FROM = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
    private static final LocalDateTime CONTENT_DATE = LocalDateTime.now().toLocalDate().atStartOfDay();
    private static final String ADMIN = "admin";

    @BeforeAll
    void setup() throws Exception {
        try (InputStream csvInputStream = PublicationTest.class.getClassLoader()
            .getResourceAsStream("location/UpdatedCsv.csv")) {
            MockMultipartFile csvFile
                = new MockMultipartFile("locationList", csvInputStream);

            mockMvc.perform(MockMvcRequestBuilders.multipart("/locations/upload").file(csvFile)
                                .with(user(ADMIN)
                                          .authorities(new SimpleGrantedAuthority("APPROLE_api.request.admin"))))
                .andExpect(status().isOk()).andReturn();
        }
    }

    @Test
    void testCountByLocationManualUpload() throws Exception {
        createDailyList(Sensitivity.PRIVATE);
        MockHttpServletRequestBuilder mockHttpServletRequestBuilder = MockMvcRequestBuilders.get(COUNT_ENDPOINT);
        MvcResult result = mockMvc.perform(mockHttpServletRequestBuilder)
            .andExpect(status().isOk())
            .andReturn();
        assertTrue(result.getResponse().getContentAsString().contains(COURT_ID), "headers not found");
    }

    @Test
    void testCountByLocationListAssist() throws Exception {
        MockHttpServletRequestBuilder mockHttpServletRequestBuilder = MockMvcRequestBuilders.get(COUNT_ENDPOINT);
        MvcResult result = mockMvc.perform(mockHttpServletRequestBuilder)
            .andExpect(status().isOk())
            .andReturn();
        assertEquals(
            HttpStatus.OK.value(), result.getResponse().getStatus(),
            "CountByLocation endpoint for ListAssist is not successful"
        );
    }

    @Test
    void testGetLocationTypeReturns() throws Exception {
        MockHttpServletRequestBuilder mockHttpServletRequestBuilder = MockMvcRequestBuilders
            .get(LOCATION_TYPE_URL + ListType.CIVIL_DAILY_CAUSE_LIST);

        MvcResult response = mockMvc.perform(mockHttpServletRequestBuilder).andExpect(status().isOk()).andReturn();

        assertTrue(
            response.getResponse().getContentAsString().contains(LocationType.VENUE.name()),
            "Location types should match"
        );
    }

    @Test
    void testGetLocationTypeReturnsBadRequest() throws Exception {
        MockHttpServletRequestBuilder mockHttpServletRequestBuilder =
            MockMvcRequestBuilders.get(LOCATION_TYPE_URL + "invalid");

        mockMvc.perform(mockHttpServletRequestBuilder).andExpect(status().isBadRequest());
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
    void testGetAllNoMatchArtefacts() throws Exception {
        Artefact expectedArtefact = createDailyList(Sensitivity.PRIVATE, DISPLAY_FROM.minusMonths(2), DISPLAY_TO,
                                                    CONTENT_DATE, "NoMatch", COURT_ID
        );

        MockHttpServletRequestBuilder mockHttpServletRequestBuilder =
            MockMvcRequestBuilders.get("/publication/no-match");

        MvcResult response = mockMvc.perform(mockHttpServletRequestBuilder).andExpect(status().isOk()).andReturn();

        String jsonOutput = response.getResponse().getContentAsString();
        JSONArray jsonArray = new JSONArray(jsonOutput);
        Artefact returnedArtefact = OBJECT_MAPPER.readValue(
            jsonArray.get(0).toString(), Artefact.class
        );

        assertTrue(
            compareArtefacts(expectedArtefact, returnedArtefact),
            "Expected and returned artefacts do not match"
        );
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
    void testDeleteArtefactsByLocation() throws Exception {
        PiUser piUser = new PiUser();
        piUser.setUserId(USER_ID);
        piUser.setEmail(EMAIL);

        when(accountManagementService.getUserById(USER_ID)).thenReturn(piUser);
        when(accountManagementService.getAllAccounts(anyString(), eq(SYSTEM_ADMIN.toString())))
            .thenReturn(List.of(EMAIL));

        Artefact artefactToDelete = createDailyList(Sensitivity.PUBLIC);

        MockHttpServletRequestBuilder preDeleteRequest = MockMvcRequestBuilders
            .get(PUBLICATION_URL + "/" + artefactToDelete.getArtefactId())
            .header(USER_ID_HEADER, USER_ID);

        mockMvc.perform(preDeleteRequest).andExpect(status().isOk());

        MockHttpServletRequestBuilder deleteRequest = MockMvcRequestBuilders
            .delete(PUBLICATION_URL + "/" + COURT_ID + "/deleteArtefacts")
            .header(USER_ID_HEADER, USER_ID);

        MvcResult deleteResponse = mockMvc.perform(deleteRequest).andExpect(status().isOk()).andReturn();

        assertEquals("Total 1 artefact deleted for location id " + COURT_ID,
                     deleteResponse.getResponse().getContentAsString(), "Should successfully delete artefact"
        );
    }

    @Test
    void testDeleteArtefactsByLocationNotFound() throws Exception {
        MockHttpServletRequestBuilder deleteRequest = MockMvcRequestBuilders
            .delete(PUBLICATION_URL + "/" + 11 + "/deleteArtefacts")
            .header(USER_ID_HEADER, USER_ID);

        MvcResult deleteResponse = mockMvc.perform(deleteRequest).andExpect(status().isNotFound()).andReturn();

        assertTrue(
            deleteResponse.getResponse().getContentAsString()
                .contains("No artefacts found with the location ID " + 11),
            "Artefact not found error message"
        );
    }

    @Test
    @WithMockUser(username = ADMIN, authorities = { "APPROLE_api.request.unknown" })
    void testUnauthorizedCountByLocation() throws Exception {
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.get(COUNT_ENDPOINT);

        mockMvc.perform(request)
            .andExpect(status().isForbidden())
            .andReturn();
    }

    @Test
    @WithMockUser(username = ADMIN, authorities = { "APPROLE_api.request.unknown" })
    void testUnauthorizedGetAllNoMatchArtefacts() throws Exception {
        MockHttpServletRequestBuilder mockHttpServletRequestBuilder =
            MockMvcRequestBuilders.get("/publication/no-match");

        mockMvc.perform(mockHttpServletRequestBuilder)
            .andExpect(status().isForbidden())
            .andReturn();
    }

    @Test
    @WithMockUser(username = ADMIN, authorities = { "APPROLE_api.request.unknown" })
    void testDeleteArtefactsByLocationUnauthorized() throws Exception {
        MockHttpServletRequestBuilder deleteRequest = MockMvcRequestBuilders
            .delete("/publication/1/deleteArtefacts")
            .header(USER_ID_HEADER, "123");

        mockMvc.perform(deleteRequest)
            .andExpect(status().isForbidden())
            .andReturn();
    }
}
