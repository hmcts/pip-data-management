package uk.gov.hmcts.reform.pip.data.management.controllers.publication;

import com.fasterxml.jackson.core.type.TypeReference;
import org.json.JSONArray;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
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
import uk.gov.hmcts.reform.pip.data.management.models.publication.CaseSearchResult;
import uk.gov.hmcts.reform.pip.data.management.models.publication.ListSearchConfig;
import uk.gov.hmcts.reform.pip.data.management.utils.CaseSearchTerm;
import uk.gov.hmcts.reform.pip.data.management.utils.PublicationIntegrationTestBase;
import uk.gov.hmcts.reform.pip.model.account.PiUser;
import uk.gov.hmcts.reform.pip.model.publication.ListType;
import uk.gov.hmcts.reform.pip.model.publication.Sensitivity;

import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.List;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.annotation.DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.pip.model.account.Roles.SYSTEM_ADMIN;
import static uk.gov.hmcts.reform.pip.model.account.Roles.VERIFIED;

@SpringBootTest(classes = {Application.class},
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("integration")
@DirtiesContext(classMode = AFTER_EACH_TEST_METHOD)
@WithMockUser(username = "admin", authorities = {"APPROLE_api.request.admin"})
class PublicationSearchTest extends PublicationIntegrationTestBase {
    private static final String PUBLICATION_URL = "/publication";
    private static final String SEARCH_BY_COURT_URL = PUBLICATION_URL + "/locationId";
    private static final String SEARCH_URL = PUBLICATION_URL + "/search";
    private static final String SEARCH_CONFIG_URL = PUBLICATION_URL + "/search/config";
    private static final String SEARCH_ARTEFACT_URL = PUBLICATION_URL + "/search/artefact";
    private static final String SEARCH_CASE_NUMBER_URL = SEARCH_URL + "/caseNumber";
    private static final String SEARCH_CASE_NAME_URL = SEARCH_URL + "/caseName";
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
    private static final String CASE_NUMBER_FIELD_NAME = "caseNumber";
    private static final String CASE_NAME_FIELD_NAME = "caseName";
    private static final String UPDATED_CASE_NUMBER_FIELD_NAME = "updatedCaseNumber";

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
        systemAdminUser.setEmail("TEST_EMAIL" + UUID.randomUUID() + "@justice.gov.uk");
        systemAdminUser.setRoles(SYSTEM_ADMIN);

        verifiedUser = new PiUser();
        verifiedUser.setUserId(VERIFIED_USER_ID.toString());
        verifiedUser.setEmail("testVerified@justice.gov.uk");
        verifiedUser.setRoles(VERIFIED);

        try (InputStream csvInputStream = PublicationTest.class.getClassLoader()
                .getResourceAsStream("location/UpdatedCsv.csv")) {
            MockMultipartFile csvFile
                    = new MockMultipartFile("locationList", csvInputStream);

            when(accountManagementService.getUserById(SYSTEM_ADMIN_ID))
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
        lenient().when(accountManagementService.getUserById(SYSTEM_ADMIN_ID))
            .thenReturn(systemAdminUser);
        lenient().when(accountManagementService.getUserById(VERIFIED_USER_ID))
            .thenReturn(verifiedUser);
    }

    private ListSearchConfig createTestListSearchConfig() {
        ListSearchConfig listSearchConfig = new ListSearchConfig();
        listSearchConfig.setListType(ListType.CIVIL_DAILY_CAUSE_LIST);
        listSearchConfig.setCaseNumberFieldName(CASE_NUMBER_FIELD_NAME);
        listSearchConfig.setCaseNameFieldName(CASE_NAME_FIELD_NAME);
        return listSearchConfig;
    }

    private void setupMockListSearchConfig() throws Exception {
        MockHttpServletRequestBuilder mappedListSearchConfig = post(SEARCH_CONFIG_URL)
            .content(OBJECT_MAPPER.writeValueAsString(createTestListSearchConfig()))
            .header(REQUESTER_ID_HEADER, SYSTEM_ADMIN_ID)
            .contentType(MediaType.APPLICATION_JSON);

        mockMvc.perform(mappedListSearchConfig)
            .andExpect(status().isCreated())
            .andReturn();
    }

    @Test
    void testCreateListSearchConfigSuccess() throws Exception {
        when(accountManagementService.getUserById(any())).thenReturn(systemAdminUser);
        ListSearchConfig listSearchConfig = createTestListSearchConfig();

        mockMvc.perform(post(SEARCH_CONFIG_URL)
                            .header(REQUESTER_ID_HEADER, SYSTEM_ADMIN_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(OBJECT_MAPPER.writeValueAsString(listSearchConfig)))
            .andExpect(status().isCreated())
            .andExpect(content().string(
                String.format("List search config successfully added by user %s", SYSTEM_ADMIN_ID)
            ));
    }

    @Test
    void testCreateListSearchConfigConflict() throws Exception {
        when(accountManagementService.getUserById(any())).thenReturn(systemAdminUser);
        setupMockListSearchConfig();

        ListSearchConfig listSearchConfig = createTestListSearchConfig();
        mockMvc.perform(post(SEARCH_CONFIG_URL)
                            .header(REQUESTER_ID_HEADER, SYSTEM_ADMIN_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(OBJECT_MAPPER.writeValueAsString(listSearchConfig)))
            .andExpect(status().isConflict());
    }

    @Test
    @WithMockUser(username = UNAUTHORIZED_USERNAME, authorities = {UNAUTHORIZED_ROLE})
    void testCreateListSearchConfigForbidden() throws Exception {
        when(accountManagementService.getUserById(any())).thenReturn(systemAdminUser);

        mockMvc.perform(post(SEARCH_CONFIG_URL)
                            .header(REQUESTER_ID_HEADER, SYSTEM_ADMIN_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(OBJECT_MAPPER.writeValueAsString(createTestListSearchConfig())))
            .andExpect(status().isForbidden());
    }

    @Test
    void testUpdateListSearchConfigSuccess() throws Exception {
        when(accountManagementService.getUserById(any())).thenReturn(systemAdminUser);
        setupMockListSearchConfig();

        MvcResult getResponse = mockMvc.perform(get(SEARCH_CONFIG_URL + "/" + ListType.CIVIL_DAILY_CAUSE_LIST)
                                                    .header(REQUESTER_ID_HEADER, SYSTEM_ADMIN_ID)
                                                    .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn();

        ListSearchConfig returnedListSearchConfig = OBJECT_MAPPER.readValue(
            getResponse.getResponse().getContentAsString(), ListSearchConfig.class
        );
        returnedListSearchConfig.setCaseNumberFieldName(UPDATED_CASE_NUMBER_FIELD_NAME);

        mockMvc.perform(put(SEARCH_CONFIG_URL + "/" + returnedListSearchConfig.getId())
                            .header(REQUESTER_ID_HEADER, SYSTEM_ADMIN_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(OBJECT_MAPPER.writeValueAsString(returnedListSearchConfig)))
            .andExpect(status().isOk())
            .andExpect(content().string(
                String.format("List search config successfully updated by user %s", SYSTEM_ADMIN_ID)
            ));

        getResponse = mockMvc.perform(get(SEARCH_CONFIG_URL + "/" + ListType.CIVIL_DAILY_CAUSE_LIST)
                                                    .header(REQUESTER_ID_HEADER, SYSTEM_ADMIN_ID)
                                                    .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn();

        ListSearchConfig updatedListSearchConfig = OBJECT_MAPPER.readValue(
            getResponse.getResponse().getContentAsString(), ListSearchConfig.class
        );

        assertThat(updatedListSearchConfig.getCaseNumberFieldName())
            .isEqualTo(UPDATED_CASE_NUMBER_FIELD_NAME);
    }

    @Test
    void testUpdateListSearchConfigNotFound() throws Exception {
        when(accountManagementService.getUserById(any())).thenReturn(systemAdminUser);
        setupMockListSearchConfig();

        MvcResult getResponse = mockMvc.perform(get(SEARCH_CONFIG_URL + "/" + ListType.CIVIL_DAILY_CAUSE_LIST)
                                                    .header(REQUESTER_ID_HEADER, SYSTEM_ADMIN_ID)
                                                    .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn();

        ListSearchConfig returnedListSearchConfig = OBJECT_MAPPER.readValue(
            getResponse.getResponse().getContentAsString(), ListSearchConfig.class
        );

        mockMvc.perform(put(SEARCH_CONFIG_URL + "/" + UUID.randomUUID())
                            .header(REQUESTER_ID_HEADER, SYSTEM_ADMIN_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(OBJECT_MAPPER.writeValueAsString(returnedListSearchConfig)))
            .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = UNAUTHORIZED_USERNAME, authorities = {UNAUTHORIZED_ROLE})
    void testUpdateListSearchConfigForbidden() throws Exception {
        when(accountManagementService.getUserById(any())).thenReturn(systemAdminUser);

        mockMvc.perform(put(SEARCH_CONFIG_URL + "/" + UUID.randomUUID())
                            .header(REQUESTER_ID_HEADER, SYSTEM_ADMIN_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(OBJECT_MAPPER.writeValueAsString(createTestListSearchConfig())))
            .andExpect(status().isForbidden());
    }

    @Test
    void testDeleteListSearchConfigSuccess() throws Exception {
        when(accountManagementService.getUserById(any())).thenReturn(systemAdminUser);
        setupMockListSearchConfig();

        MvcResult getResponse = mockMvc.perform(get(SEARCH_CONFIG_URL + "/" + ListType.CIVIL_DAILY_CAUSE_LIST)
                                                    .header(REQUESTER_ID_HEADER, SYSTEM_ADMIN_ID)
                                                    .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn();

        ListSearchConfig returnedListSearchConfig = OBJECT_MAPPER.readValue(
            getResponse.getResponse().getContentAsString(), ListSearchConfig.class
        );

        mockMvc.perform(delete(SEARCH_CONFIG_URL + "/" + returnedListSearchConfig.getId())
                            .header(REQUESTER_ID_HEADER, SYSTEM_ADMIN_ID)
                            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().string(
                String.format("List search config successfully deleted by user %s", SYSTEM_ADMIN_ID)
            ));

        mockMvc.perform(get(SEARCH_CONFIG_URL + "/" + ListType.CIVIL_DAILY_CAUSE_LIST)
                                          .header(REQUESTER_ID_HEADER, SYSTEM_ADMIN_ID)
                                          .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound());
    }

    @Test
    void testDeleteListSearchConfigNotFound() throws Exception {
        when(accountManagementService.getUserById(any())).thenReturn(systemAdminUser);
        setupMockListSearchConfig();

        mockMvc.perform(delete(SEARCH_CONFIG_URL + "/" + UUID.randomUUID())
                            .header(REQUESTER_ID_HEADER, SYSTEM_ADMIN_ID)
                            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = UNAUTHORIZED_USERNAME, authorities = {UNAUTHORIZED_ROLE})
    void testDeleteListSearchConfigForbidden() throws Exception {
        when(accountManagementService.getUserById(any())).thenReturn(systemAdminUser);

        mockMvc.perform(delete(SEARCH_CONFIG_URL + "/" + UUID.randomUUID())
                            .header(REQUESTER_ID_HEADER, SYSTEM_ADMIN_ID)
                            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isForbidden());
    }

    @Test
    void testGetListSearchConfigSuccess() throws Exception {
        when(accountManagementService.getUserById(any())).thenReturn(systemAdminUser);
        setupMockListSearchConfig();

        MvcResult getResponse = mockMvc.perform(get(SEARCH_CONFIG_URL + "/" + ListType.CIVIL_DAILY_CAUSE_LIST)
                                                    .header(REQUESTER_ID_HEADER, SYSTEM_ADMIN_ID)
                                                    .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn();

        ListSearchConfig returnedListSearchConfig = OBJECT_MAPPER.readValue(
            getResponse.getResponse().getContentAsString(), ListSearchConfig.class
        );

        assertThat(returnedListSearchConfig.getListType())
            .isEqualTo(ListType.CIVIL_DAILY_CAUSE_LIST);

        assertThat(returnedListSearchConfig.getCaseNumberFieldName())
            .isEqualTo(CASE_NUMBER_FIELD_NAME);

        assertThat(returnedListSearchConfig.getCaseNameFieldName())
            .isEqualTo(CASE_NAME_FIELD_NAME);
    }

    @Test
    void testGetListSearchConfigNotFound() throws Exception {
        when(accountManagementService.getUserById(any())).thenReturn(systemAdminUser);
        setupMockListSearchConfig();

        mockMvc.perform(get(SEARCH_CONFIG_URL + "/" + ListType.FAMILY_DAILY_CAUSE_LIST)
                                                    .header(REQUESTER_ID_HEADER, SYSTEM_ADMIN_ID)
                                                    .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = UNAUTHORIZED_USERNAME, authorities = {UNAUTHORIZED_ROLE})
    void testGetListSearchConfigForbidden() throws Exception {
        when(accountManagementService.getUserById(any())).thenReturn(systemAdminUser);

        mockMvc.perform(get(SEARCH_CONFIG_URL + "/" + ListType.CIVIL_DAILY_CAUSE_LIST)
                                                    .header(REQUESTER_ID_HEADER, SYSTEM_ADMIN_ID)
                                                    .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isForbidden());
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
            .get(SEARCH_URL)
            .param(SEARCH_TERM_PARAM, CaseSearchTerm.CASE_NAME.name())
            .param(SEARCH_VALUE_PARAM, CASE_NAME_SEARCH_VALUE)
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


    @Test
    void testFindArtefactSearchByArtefactIdSuccess() throws Exception {
        when(accountManagementService.getUserById(any())).thenReturn(systemAdminUser);
        setupMockListSearchConfig();

        Artefact artefact = createDailyList(Sensitivity.PUBLIC);

        mockMvc.perform(get(SEARCH_ARTEFACT_URL + "/" + artefact.getArtefactId())
                            .header(REQUESTER_ID_HEADER, SYSTEM_ADMIN_ID)
                            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());
    }

    @Test
    void testDeleteArtefactSearchByArtefactIdSuccess() throws Exception {
        when(accountManagementService.getUserById(any())).thenReturn(systemAdminUser);
        setupMockListSearchConfig();
        Artefact artefact = createDailyList(Sensitivity.PUBLIC);

        mockMvc.perform(delete(SEARCH_ARTEFACT_URL + "/" + artefact.getArtefactId())
                            .header(REQUESTER_ID_HEADER, SYSTEM_ADMIN_ID)
                            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().string(
                String.format("Artefact search rows successfully deleted for artefactId %s", artefact.getArtefactId())
            ));
    }

    @Test
    @WithMockUser(username = UNAUTHORIZED_USERNAME, authorities = {UNAUTHORIZED_ROLE})
    void testFindArtefactSearchByArtefactIdForbidden() throws Exception {
        when(accountManagementService.getUserById(any())).thenReturn(systemAdminUser);
        mockMvc.perform(get(SEARCH_ARTEFACT_URL + "/" + UUID.randomUUID())
                            .header(REQUESTER_ID_HEADER, SYSTEM_ADMIN_ID)
                            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = UNAUTHORIZED_USERNAME, authorities = {UNAUTHORIZED_ROLE})
    void testDeleteArtefactSearchByArtefactIdForbidden() throws Exception {
        when(accountManagementService.getUserById(any())).thenReturn(systemAdminUser);
        mockMvc.perform(delete(SEARCH_ARTEFACT_URL + "/" + UUID.randomUUID())
                            .header(REQUESTER_ID_HEADER, SYSTEM_ADMIN_ID)
                            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isForbidden());
    }

    @Test
    void testGetCasesByCaseNumberReturnsResults() throws Exception {
        setupMockListSearchConfig();
        createDailyList(Sensitivity.PUBLIC);

        MvcResult response = mockMvc.perform(get(SEARCH_CASE_NUMBER_URL)
                                               .param(SEARCH_VALUE_PARAM, CASE_ID_SEARCH_VALUE)
                                               .header(REQUESTER_ID_HEADER, VERIFIED_USER_ID))
            .andExpect(status().isOk())
            .andReturn();

        List<CaseSearchResult> results = OBJECT_MAPPER.readValue(
            response.getResponse().getContentAsString(), new TypeReference<>() {}
        );

        assertThat(results)
            .isNotEmpty();
        assertThat(results.get(0).getCaseNumber())
            .isEqualTo(CASE_ID_SEARCH_VALUE);
    }

    @Test
    void testGetCasesByCaseNumberReturnsEmptyListWhenNotFound() throws Exception {
        MvcResult response = mockMvc.perform(get(SEARCH_CASE_NUMBER_URL)
                            .param(SEARCH_VALUE_PARAM, "nonExistentCaseNumber")
                            .header(REQUESTER_ID_HEADER, VERIFIED_USER_ID))
            .andExpect(status().isOk())
            .andReturn();

        List<CaseSearchResult> results = OBJECT_MAPPER.readValue(
            response.getResponse().getContentAsString(), new TypeReference<>() {}
        );

        assertThat(results)
            .isEmpty();
    }

    @Test
    @WithMockUser(username = UNAUTHORIZED_USERNAME, authorities = {UNAUTHORIZED_ROLE})
    void testGetCasesByCaseNumberForbidden() throws Exception {
        mockMvc.perform(get(SEARCH_CASE_NUMBER_URL)
                            .param(SEARCH_VALUE_PARAM, CASE_ID_SEARCH_VALUE)
                            .header(REQUESTER_ID_HEADER, VERIFIED_USER_ID))
            .andExpect(status().isForbidden());
    }

    @Test
    void testGetCasesByCaseNameReturnsResults() throws Exception {
        setupMockListSearchConfig();
        createDailyList(Sensitivity.PUBLIC);

        MvcResult response = mockMvc.perform(get(SEARCH_CASE_NAME_URL)
                                               .param(SEARCH_VALUE_PARAM, CASE_NAME_SEARCH_VALUE)
                                               .header(REQUESTER_ID_HEADER, VERIFIED_USER_ID))
            .andExpect(status().isOk())
            .andReturn();

        List<CaseSearchResult> results = OBJECT_MAPPER.readValue(
            response.getResponse().getContentAsString(), new TypeReference<>() {}
        );

        assertThat(results)
            .isNotEmpty();
        assertThat(results.get(0).getCaseName())
            .containsIgnoringCase(CASE_NAME_SEARCH_VALUE);
    }

    @Test
    void testGetCasesByCaseNameReturnsEmptyListWhenNotFound() throws Exception {
        MvcResult response = mockMvc.perform(get(SEARCH_CASE_NAME_URL)
                            .param(SEARCH_VALUE_PARAM, "nonExistentCaseName")
                            .header(REQUESTER_ID_HEADER, VERIFIED_USER_ID))
            .andExpect(status().isOk())
            .andReturn();

        List<CaseSearchResult> results = OBJECT_MAPPER.readValue(
            response.getResponse().getContentAsString(), new TypeReference<>() {}
        );

        assertThat(results)
            .isEmpty();
    }

    @Test
    void testGetCasesByCaseNameReturnsBadRequestWhenTooShort() throws Exception {
        mockMvc.perform(get(SEARCH_CASE_NAME_URL)
                            .param(SEARCH_VALUE_PARAM, "ab")
                            .header(REQUESTER_ID_HEADER, VERIFIED_USER_ID))
            .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = UNAUTHORIZED_USERNAME, authorities = {UNAUTHORIZED_ROLE})
    void testGetCasesByCaseNameForbidden() throws Exception {
        mockMvc.perform(get(SEARCH_CASE_NAME_URL)
                            .param(SEARCH_VALUE_PARAM, CASE_NAME_SEARCH_VALUE)
                            .header(REQUESTER_ID_HEADER, VERIFIED_USER_ID))
            .andExpect(status().isForbidden());
    }

}
