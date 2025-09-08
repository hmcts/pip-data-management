package uk.gov.hmcts.reform.pip.data.management.controllers.publication;

import io.zonky.test.db.AutoConfigureEmbeddedDatabase;
import org.json.JSONArray;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
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
import uk.gov.hmcts.reform.pip.data.management.utils.CaseSearchTerm;
import uk.gov.hmcts.reform.pip.data.management.utils.PublicationIntegrationTestBase;
import uk.gov.hmcts.reform.pip.model.account.PiUser;
import uk.gov.hmcts.reform.pip.model.publication.ListType;
import uk.gov.hmcts.reform.pip.model.publication.Sensitivity;

import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.pip.model.account.Roles.SYSTEM_ADMIN;
import static uk.gov.hmcts.reform.pip.model.account.Roles.VERIFIED;

@SpringBootTest(classes = {Application.class},
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@ActiveProfiles("integration")
@AutoConfigureEmbeddedDatabase(type = AutoConfigureEmbeddedDatabase.DatabaseType.POSTGRES)
@WithMockUser(username = "admin", authorities = {"APPROLE_api.request.admin"})
class PublicationSearchTest extends PublicationIntegrationTestBase {
    private static final String PUBLICATION_URL = "/publication";
    private static final String SEARCH_BY_COURT_URL = PUBLICATION_URL + "/locationId";
    private static final String SEARCH_URL = PUBLICATION_URL + "/search";
    private static final LocalDateTime DISPLAY_FROM = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
    private static final String COURT_ID = "1";
    private static final LocalDateTime CONTENT_DATE = LocalDateTime.now().toLocalDate().atStartOfDay()
        .truncatedTo(ChronoUnit.SECONDS);

    private static final String TRUE = "true";
    private static final String FALSE = "false";
    private static final String ADMIN_HEADER = "x-admin";
    private static final String VERIFICATION_HEADER = "verification";
    private static final String ADMIN = "admin";
    private static final String SEARCH_TERM_PARAM = "searchTerm";
    private static final String SEARCH_VALUE_PARAM = "searchValue";
    private static final String CASE_ID_SEARCH_VALUE = "45684548";
    private static final String CASE_NAME_SEARCH_VALUE = "Smith";

    private static final String ARTEFACT_NOT_FOUND_ERROR = "No Artefacts found";
    private static final String VALIDATION_DISPLAY_FROM = "The expected Display From has not been returned";
    private static final String SHOULD_RETURN_EXPECTED_ARTEFACT = "Should return expected artefact";

    private static final String UNAUTHORIZED_USERNAME = "unauthorized_username";
    private static final String UNAUTHORIZED_ROLE = "APPROLE_unknown.role";
    private static final String FORBIDDEN_STATUS_CODE = "Status code does not match forbidden";
    private static final String REQUESTER_ID_HEADER = "x-requester-id";
    private static final UUID SYSTEM_ADMIN_ID = UUID.randomUUID();
    private static final UUID VERIFIED_USER_ID = UUID.randomUUID();

    private static PiUser verifiedUser;
    private static PiUser systemAdminUser;

    @BeforeAll
    public void setup() throws Exception {
        systemAdminUser = new PiUser();
        systemAdminUser.setUserId(SYSTEM_ADMIN_ID.toString());
        systemAdminUser.setEmail("test@justice.gov.uk");
        systemAdminUser.setRoles(SYSTEM_ADMIN);

        verifiedUser = new PiUser();
        verifiedUser.setUserId(VERIFIED_USER_ID.toString());
        verifiedUser.setEmail("testVerified@justice.gov.uk");
        verifiedUser.setRoles(VERIFIED);

        try (InputStream csvInputStream = PublicationTest.class.getClassLoader()
                .getResourceAsStream("location/UpdatedCsv.csv")) {
            MockMultipartFile csvFile
                    = new MockMultipartFile("locationList", csvInputStream);

            when(accountManagementService.getUserById(SYSTEM_ADMIN_ID.toString()))
                .thenReturn(systemAdminUser);

            mockMvc.perform(MockMvcRequestBuilders.multipart("/locations/upload").file(csvFile)
                                .header(REQUESTER_ID_HEADER, SYSTEM_ADMIN_ID)
                            .with(user(ADMIN)
                                    .authorities(new SimpleGrantedAuthority("APPROLE_api.request.admin"))))
                    .andExpect(status().isOk()).andReturn();
        }
    }

    @BeforeEach
    public void setupBeforeEach() {
        lenient().when(accountManagementService.getUserById(SYSTEM_ADMIN_ID.toString()))
            .thenReturn(systemAdminUser);
        lenient().when(accountManagementService.getUserById(VERIFIED_USER_ID.toString()))
            .thenReturn(verifiedUser);
    }

    @Test
    void testAuthorisedGetArtefactByCaseIdSearchVerified() throws Exception {
        when(accountManagementService.getIsAuthorised(
            VERIFIED_USER_ID, ListType.CIVIL_DAILY_CAUSE_LIST, Sensitivity.PRIVATE
        )).thenReturn(true);

        Artefact artefact = createDailyList(Sensitivity.PRIVATE);

        MockHttpServletRequestBuilder mockHttpServletRequestBuilder = MockMvcRequestBuilders
            .get(SEARCH_URL)
            .param(SEARCH_TERM_PARAM, CaseSearchTerm.CASE_ID.name())
            .param(SEARCH_VALUE_PARAM, CASE_ID_SEARCH_VALUE)
            .header(REQUESTER_ID_HEADER, VERIFIED_USER_ID);

        mockHttpServletRequestBuilder.header(USER_ID_HEADER, USER_ID);

        MvcResult getResponse = mockMvc.perform(mockHttpServletRequestBuilder)
            .andExpect(status().isOk())
            .andReturn();

        assertTrue(
            getResponse.getResponse().getContentAsString().contains(artefact.getArtefactId().toString()),
            SHOULD_RETURN_EXPECTED_ARTEFACT
        );
    }

    @Test
    void testUnauthorisedGetArtefactByCaseIdSearchVerified() throws Exception {
        when(accountManagementService.getIsAuthorised(
            VERIFIED_USER_ID, ListType.CIVIL_DAILY_CAUSE_LIST, Sensitivity.PRIVATE
        )).thenReturn(false);

        createDailyList(Sensitivity.PRIVATE);

        MockHttpServletRequestBuilder mockHttpServletRequestBuilder = MockMvcRequestBuilders
            .get(SEARCH_URL)
            .param(SEARCH_TERM_PARAM, CaseSearchTerm.CASE_ID.name())
            .param(SEARCH_VALUE_PARAM, CASE_ID_SEARCH_VALUE)
            .header(REQUESTER_ID_HEADER, VERIFIED_USER_ID);

        mockHttpServletRequestBuilder.header(USER_ID_HEADER, USER_ID);

        MvcResult getResponse = mockMvc.perform(mockHttpServletRequestBuilder)
            .andExpect(status().isNotFound())
            .andReturn();

        assertTrue(
            getResponse.getResponse().getContentAsString().contains(ARTEFACT_NOT_FOUND_ERROR),
            SHOULD_RETURN_EXPECTED_ARTEFACT
        );
    }

    @Test
    void testGetArtefactByCaseIdSearchUnverified() throws Exception {
        Artefact artefact = createDailyList(Sensitivity.PUBLIC);

        MockHttpServletRequestBuilder mockHttpServletRequestBuilder = MockMvcRequestBuilders
            .get(SEARCH_URL)
            .param(SEARCH_TERM_PARAM, CaseSearchTerm.CASE_ID.name())
            .param(SEARCH_VALUE_PARAM, CASE_ID_SEARCH_VALUE)
            .header(REQUESTER_ID_HEADER, VERIFIED_USER_ID);

        MvcResult getResponse = mockMvc.perform(mockHttpServletRequestBuilder)
            .andExpect(status().isOk())
            .andReturn();

        assertTrue(
            getResponse.getResponse().getContentAsString().contains(artefact.getArtefactId().toString()),
            SHOULD_RETURN_EXPECTED_ARTEFACT
        );
    }

    @Test
    void testGetArtefactByCaseIdSearchUnverifiedNotFound() throws Exception {
        createDailyList(Sensitivity.CLASSIFIED);

        MockHttpServletRequestBuilder mockHttpServletRequestBuilder = MockMvcRequestBuilders
            .get(SEARCH_URL)
            .param(SEARCH_TERM_PARAM, CaseSearchTerm.CASE_ID.name())
            .param(SEARCH_VALUE_PARAM, CASE_ID_SEARCH_VALUE)
            .header(REQUESTER_ID_HEADER, VERIFIED_USER_ID);

        mockMvc.perform(mockHttpServletRequestBuilder)
            .andExpect(status().isNotFound());
    }

    @Test
    void testAuthorisedGetArtefactByCaseNameSearchVerified() throws Exception {
        when(accountManagementService.getIsAuthorised(
            VERIFIED_USER_ID, ListType.CIVIL_DAILY_CAUSE_LIST, Sensitivity.PRIVATE
        )).thenReturn(true);

        Artefact artefact = createDailyList(Sensitivity.PRIVATE);

        MockHttpServletRequestBuilder mockHttpServletRequestBuilder = MockMvcRequestBuilders
            .get(SEARCH_URL)
            .param(SEARCH_TERM_PARAM, CaseSearchTerm.CASE_NAME.name())
            .param(SEARCH_VALUE_PARAM, CASE_NAME_SEARCH_VALUE)
            .header(REQUESTER_ID_HEADER, VERIFIED_USER_ID);

        mockHttpServletRequestBuilder.header(USER_ID_HEADER, USER_ID);

        MvcResult getResponse = mockMvc.perform(mockHttpServletRequestBuilder)
            .andExpect(status().isOk())
            .andReturn();

        assertTrue(
            getResponse.getResponse().getContentAsString().contains(artefact.getArtefactId().toString()),
            SHOULD_RETURN_EXPECTED_ARTEFACT
        );
    }

    @Test
    void testUnauthorisedGetArtefactByCaseNameSearchVerified() throws Exception {
        when(accountManagementService.getIsAuthorised(
            VERIFIED_USER_ID, ListType.CIVIL_DAILY_CAUSE_LIST, Sensitivity.PRIVATE
        )).thenReturn(false);

        createDailyList(Sensitivity.PRIVATE);

        MockHttpServletRequestBuilder mockHttpServletRequestBuilder = MockMvcRequestBuilders
            .get(SEARCH_URL)
            .param(SEARCH_TERM_PARAM, CaseSearchTerm.CASE_NAME.name())
            .param(SEARCH_VALUE_PARAM, CASE_NAME_SEARCH_VALUE)
            .header(REQUESTER_ID_HEADER, VERIFIED_USER_ID);

        mockHttpServletRequestBuilder.header(USER_ID_HEADER, USER_ID);

        MvcResult getResponse = mockMvc.perform(mockHttpServletRequestBuilder)
            .andExpect(status().isNotFound())
            .andReturn();

        assertTrue(
            getResponse.getResponse().getContentAsString().contains(ARTEFACT_NOT_FOUND_ERROR),
            SHOULD_RETURN_EXPECTED_ARTEFACT
        );
    }

    @Test
    void testGetArtefactByCaseNameSearchUnverified() throws Exception {
        Artefact artefact = createDailyList(Sensitivity.PUBLIC);

        MockHttpServletRequestBuilder mockHttpServletRequestBuilder = MockMvcRequestBuilders
            .get(SEARCH_URL)
            .param(SEARCH_TERM_PARAM, CaseSearchTerm.CASE_NAME.name())
            .param(SEARCH_VALUE_PARAM, CASE_NAME_SEARCH_VALUE)
            .header(REQUESTER_ID_HEADER, VERIFIED_USER_ID);

        MvcResult getResponse = mockMvc.perform(mockHttpServletRequestBuilder)
            .andExpect(status().isOk())
            .andReturn();

        assertTrue(
            getResponse.getResponse().getContentAsString().contains(artefact.getArtefactId().toString()),
            SHOULD_RETURN_EXPECTED_ARTEFACT
        );
    }

    @Test
    void testGetArtefactByCaseNameSearchUnverifiedNotFound() throws Exception {
        createDailyList(Sensitivity.CLASSIFIED);

        MockHttpServletRequestBuilder mockHttpServletRequestBuilder = MockMvcRequestBuilders
            .get(SEARCH_URL)
            .param(SEARCH_TERM_PARAM, CaseSearchTerm.CASE_NAME.name())
            .param(SEARCH_VALUE_PARAM, CASE_NAME_SEARCH_VALUE)
            .header(REQUESTER_ID_HEADER, VERIFIED_USER_ID);

        mockMvc.perform(mockHttpServletRequestBuilder)
            .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = UNAUTHORIZED_USERNAME, authorities = {UNAUTHORIZED_ROLE})
    void testUnauthorizedGetAllRelevantArtefactsBySearchValue() throws Exception {
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
            .get(SEARCH_URL + VALID_CASE_NAME_SEARCH)
            .header(REQUESTER_ID_HEADER, VERIFIED_USER_ID);

        MvcResult archiveResponse = mockMvc.perform(request).andExpect(status().isForbidden()).andReturn();

        assertEquals(HttpStatus.FORBIDDEN.value(), archiveResponse.getResponse().getStatus(),
                     FORBIDDEN_STATUS_CODE
        );
    }

    @Test
    void testAuthorisedGetArtefactByCaseIdSearchV2Verified() throws Exception {
        when(accountManagementService.getIsAuthorised(
            VERIFIED_USER_ID, ListType.CIVIL_DAILY_CAUSE_LIST, Sensitivity.PRIVATE
        )).thenReturn(true);

        Artefact artefact = createDailyList(Sensitivity.PRIVATE);

        MockHttpServletRequestBuilder mockHttpServletRequestBuilder = MockMvcRequestBuilders
            .get(SEARCH_URL)
            .param(SEARCH_TERM_PARAM, CaseSearchTerm.CASE_ID.name())
            .param(SEARCH_VALUE_PARAM, CASE_ID_SEARCH_VALUE)
            .header(REQUESTER_ID_HEADER, VERIFIED_USER_ID);

        MvcResult getResponse = mockMvc.perform(mockHttpServletRequestBuilder)
            .andExpect(status().isOk())
            .andReturn();

        assertTrue(
            getResponse.getResponse().getContentAsString().contains(artefact.getArtefactId().toString()),
            SHOULD_RETURN_EXPECTED_ARTEFACT
        );
    }

    @Test
    void testUnauthorisedGetArtefactByCaseIdSearchV2Verified() throws Exception {
        when(accountManagementService.getIsAuthorised(
            VERIFIED_USER_ID, ListType.CIVIL_DAILY_CAUSE_LIST, Sensitivity.PRIVATE
        )).thenReturn(false);

        createDailyList(Sensitivity.PRIVATE);

        MockHttpServletRequestBuilder mockHttpServletRequestBuilder = MockMvcRequestBuilders
            .get(SEARCH_URL)
            .param(SEARCH_TERM_PARAM, CaseSearchTerm.CASE_ID.name())
            .param(SEARCH_VALUE_PARAM, CASE_ID_SEARCH_VALUE)
            .header(REQUESTER_ID_HEADER, VERIFIED_USER_ID);

        MvcResult getResponse = mockMvc.perform(mockHttpServletRequestBuilder)
            .andExpect(status().isNotFound())
            .andReturn();

        assertTrue(
            getResponse.getResponse().getContentAsString().contains(ARTEFACT_NOT_FOUND_ERROR),
            SHOULD_RETURN_EXPECTED_ARTEFACT
        );
    }

    @Test
    void testGetArtefactByCaseIdSearchV2Unverified() throws Exception {
        Artefact artefact = createDailyList(Sensitivity.PUBLIC);

        MockHttpServletRequestBuilder mockHttpServletRequestBuilder = MockMvcRequestBuilders
            .get(SEARCH_URL)
            .param(SEARCH_TERM_PARAM, CaseSearchTerm.CASE_ID.name())
            .param(SEARCH_VALUE_PARAM, CASE_ID_SEARCH_VALUE)
            .header(REQUESTER_ID_HEADER, VERIFIED_USER_ID);

        MvcResult getResponse = mockMvc.perform(mockHttpServletRequestBuilder)
            .andExpect(status().isOk())
            .andReturn();

        assertTrue(
            getResponse.getResponse().getContentAsString().contains(artefact.getArtefactId().toString()),
            SHOULD_RETURN_EXPECTED_ARTEFACT
        );
    }

    @Test
    void testGetArtefactByCaseIdSearchV2UnverifiedNotFound() throws Exception {
        createDailyList(Sensitivity.CLASSIFIED);

        MockHttpServletRequestBuilder mockHttpServletRequestBuilder = MockMvcRequestBuilders
            .get(SEARCH_URL)
            .param(SEARCH_TERM_PARAM, CaseSearchTerm.CASE_ID.name())
            .param(SEARCH_VALUE_PARAM, CASE_ID_SEARCH_VALUE)
            .header(REQUESTER_ID_HEADER, VERIFIED_USER_ID);

        mockMvc.perform(mockHttpServletRequestBuilder)
            .andExpect(status().isNotFound());
    }

    @Test
    void testAuthorisedGetArtefactByCaseNameSearchV2Verified() throws Exception {
        when(accountManagementService.getIsAuthorised(
            VERIFIED_USER_ID, ListType.CIVIL_DAILY_CAUSE_LIST, Sensitivity.PRIVATE
        )).thenReturn(true);

        Artefact artefact = createDailyList(Sensitivity.PRIVATE);

        MockHttpServletRequestBuilder mockHttpServletRequestBuilder = MockMvcRequestBuilders
            .get(SEARCH_URL)
            .param(SEARCH_TERM_PARAM, CaseSearchTerm.CASE_NAME.name())
            .param(SEARCH_VALUE_PARAM, CASE_NAME_SEARCH_VALUE)
            .header(REQUESTER_ID_HEADER, VERIFIED_USER_ID);

        MvcResult getResponse = mockMvc.perform(mockHttpServletRequestBuilder)
            .andExpect(status().isOk())
            .andReturn();

        assertTrue(
            getResponse.getResponse().getContentAsString().contains(artefact.getArtefactId().toString()),
            SHOULD_RETURN_EXPECTED_ARTEFACT
        );
    }

    @Test
    void testUnauthorisedGetArtefactByCaseNameSearchV2Verified() throws Exception {
        when(accountManagementService.getIsAuthorised(
            VERIFIED_USER_ID, ListType.CIVIL_DAILY_CAUSE_LIST, Sensitivity.PRIVATE
        )).thenReturn(false);

        createDailyList(Sensitivity.PRIVATE);

        MockHttpServletRequestBuilder mockHttpServletRequestBuilder = MockMvcRequestBuilders
            .get(SEARCH_URL)
            .param(SEARCH_TERM_PARAM, CaseSearchTerm.CASE_NAME.name())
            .param(SEARCH_VALUE_PARAM, CASE_NAME_SEARCH_VALUE)
            .header(REQUESTER_ID_HEADER, VERIFIED_USER_ID);

        MvcResult getResponse = mockMvc.perform(mockHttpServletRequestBuilder)
            .andExpect(status().isNotFound())
            .andReturn();

        assertTrue(
            getResponse.getResponse().getContentAsString().contains(ARTEFACT_NOT_FOUND_ERROR),
            SHOULD_RETURN_EXPECTED_ARTEFACT
        );
    }

    @Test
    void testGetArtefactByCaseNameSearchV2Unverified() throws Exception {
        Artefact artefact = createDailyList(Sensitivity.PUBLIC);

        MockHttpServletRequestBuilder mockHttpServletRequestBuilder = MockMvcRequestBuilders
            .get(SEARCH_URL)
            .param(SEARCH_TERM_PARAM, CaseSearchTerm.CASE_NAME.name())
            .param(SEARCH_VALUE_PARAM, CASE_NAME_SEARCH_VALUE)
            .header(REQUESTER_ID_HEADER, VERIFIED_USER_ID);

        MvcResult getResponse = mockMvc.perform(mockHttpServletRequestBuilder)
            .andExpect(status().isOk())
            .andReturn();

        assertTrue(
            getResponse.getResponse().getContentAsString().contains(artefact.getArtefactId().toString()),
            SHOULD_RETURN_EXPECTED_ARTEFACT
        );
    }

    @Test
    void testGetArtefactByCaseNameSearchV2UnverifiedNotFound() throws Exception {
        createDailyList(Sensitivity.CLASSIFIED);

        MockHttpServletRequestBuilder mockHttpServletRequestBuilder = MockMvcRequestBuilders
            .get(SEARCH_URL)
            .param(SEARCH_TERM_PARAM, CaseSearchTerm.CASE_NAME.name())
            .param(SEARCH_VALUE_PARAM, CASE_NAME_SEARCH_VALUE)
            .header(REQUESTER_ID_HEADER, VERIFIED_USER_ID);

        mockMvc.perform(mockHttpServletRequestBuilder)
            .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = UNAUTHORIZED_USERNAME, authorities = {UNAUTHORIZED_ROLE})
    void testUnauthorizedGetAllRelevantArtefactsBySearchValueV2() throws Exception {
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
            .get(SEARCH_URL)
            .param(SEARCH_TERM_PARAM, CaseSearchTerm.CASE_NAME.name())
            .param(SEARCH_VALUE_PARAM, CASE_NAME_SEARCH_VALUE)
            .header(REQUESTER_ID_HEADER, VERIFIED_USER_ID);

        mockMvc.perform(request)
            .andExpect(status().isForbidden());
    }

    @Test
    void testGetCourtByIdShowsAllCourtsForAdmin() throws Exception {
        Artefact inDateArtefact = createDailyList(Sensitivity.PUBLIC, DISPLAY_FROM.minusMonths(2), CONTENT_DATE);
        Artefact futureArtefact = createDailyList(Sensitivity.PUBLIC, DISPLAY_FROM.plusMonths(1),
                                                  CONTENT_DATE.plusDays(1));

        assertEquals(inDateArtefact.getDisplayFrom(), DISPLAY_FROM.minusMonths(2),
                     VALIDATION_DISPLAY_FROM);
        assertEquals(futureArtefact.getDisplayFrom(), DISPLAY_FROM.plusMonths(1),
                     VALIDATION_DISPLAY_FROM);

        MockHttpServletRequestBuilder mockHttpServletRequestBuilder = MockMvcRequestBuilders
            .get(SEARCH_BY_COURT_URL + "/" + COURT_ID)
            .header(ADMIN_HEADER, FALSE)
            .header(REQUESTER_ID_HEADER, SYSTEM_ADMIN_ID);

        MvcResult nonAdminResponse =
            mockMvc.perform(mockHttpServletRequestBuilder).andExpect(status().isOk()).andReturn();

        MockHttpServletRequestBuilder mockHttpServletRequestBuilder1 = MockMvcRequestBuilders
            .get(SEARCH_BY_COURT_URL + "/" + COURT_ID)
            .header(ADMIN_HEADER, TRUE);

        MvcResult adminResponse = mockMvc.perform(mockHttpServletRequestBuilder1)
            .andExpect(status().isOk())
            .andReturn();

        JSONArray nonAdminResults = new JSONArray(nonAdminResponse.getResponse().getContentAsString());
        JSONArray adminResults = new JSONArray(adminResponse.getResponse().getContentAsString());
        assertEquals(1, nonAdminResults.length(), "Should return 1 artefact for non admin");
        assertEquals(2, adminResults.length(), "Should return 2 artefacts for admins");
    }

    @Test
    @WithMockUser(username = ADMIN, authorities = { "APPROLE_api.request.unknown" })
    void testUnauthorizedGetByCourtId() throws Exception {
        MockHttpServletRequestBuilder mockHttpServletRequestBuilder = MockMvcRequestBuilders
            .get(SEARCH_BY_COURT_URL + "/" + COURT_ID)
            .header(VERIFICATION_HEADER, TRUE);

        mockMvc.perform(mockHttpServletRequestBuilder)
            .andExpect(status().isForbidden())
            .andReturn();
    }
}
