package uk.gov.hmcts.reform.pip.data.management.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.zonky.test.db.AutoConfigureEmbeddedDatabase;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import uk.gov.hmcts.reform.pip.data.management.Application;
import uk.gov.hmcts.reform.pip.data.management.config.PublicationConfiguration;
import uk.gov.hmcts.reform.pip.data.management.models.publication.Artefact;
import uk.gov.hmcts.reform.pip.data.management.utils.IntegrationTestBase;
import uk.gov.hmcts.reform.pip.model.account.PiUser;
import uk.gov.hmcts.reform.pip.model.publication.ArtefactType;
import uk.gov.hmcts.reform.pip.model.publication.Language;
import uk.gov.hmcts.reform.pip.model.publication.ListType;
import uk.gov.hmcts.reform.pip.model.publication.Sensitivity;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.pip.model.account.Roles.SYSTEM_ADMIN;

@SpringBootTest(classes = {Application.class},
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@ActiveProfiles("integration")
@AutoConfigureEmbeddedDatabase(type = AutoConfigureEmbeddedDatabase.DatabaseType.POSTGRES)
@WithMockUser(username = "admin", authorities = {"APPROLE_api.request.admin"})
class PublicationSubscriptionSearchTest extends IntegrationTestBase {

    @Autowired
    private MockMvc mockMvc;

    private static final String VALID_CASE_ID_SEARCH = "/CASE_ID/45684548";
    private static final String PROVENANCE = "MANUAL_UPLOAD";
    private static final String SHOULD_RETURN_EXPECTED_ARTEFACT = "Should return expected artefact";
    private static final LocalDateTime CONTENT_DATE = LocalDateTime.now().toLocalDate().atStartOfDay()
        .truncatedTo(ChronoUnit.SECONDS);
    private static final String SEARCH_URL = "/publication/search";
    private static final String VALID_CASE_NAME_SEARCH = "/CASE_NAME/Smith";
    private static final String PUBLICATION_URL = "/publication";
    private static final String SOURCE_ARTEFACT_ID = "sourceArtefactId";
    private static final String COURT_ID = "123";
    private static final Language LANGUAGE = Language.ENGLISH;
    private static final ArtefactType ARTEFACT_TYPE = ArtefactType.LIST;
    private static final LocalDateTime DISPLAY_TO = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
    private static final LocalDateTime DISPLAY_FROM = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
    private static final String UNAUTHORIZED_USERNAME = "unauthorized_username";
    private static final String UNAUTHORIZED_ROLE = "APPROLE_unknown.role";
    private static final String FORBIDDEN_STATUS_CODE = "Status code does not match forbidden";
    private static final String REQUESTER_ID_HEADER = "x-requester-id";
    private static final String SYSTEM_ADMIN_ID = UUID.randomUUID().toString();

    private static ObjectMapper objectMapper;
    private static PiUser piUser;

    @BeforeAll
    public static void setup() throws IOException {
        piUser = new PiUser();
        piUser.setUserId(SYSTEM_ADMIN_ID);
        piUser.setEmail("test@justice.gov.uk");
        piUser.setRoles(SYSTEM_ADMIN);

        objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();
    }

    Artefact createDailyList(Sensitivity sensitivity) throws Exception {
        return this.createDailyList(sensitivity, DISPLAY_FROM.minusMonths(2), CONTENT_DATE);
    }

    Artefact createDailyList(Sensitivity sensitivity, LocalDateTime displayFrom, LocalDateTime contentDate)
        throws Exception {
        return this.createDailyList(sensitivity, displayFrom, DISPLAY_TO, contentDate, PROVENANCE);
    }

    public Artefact createDailyList(Sensitivity sensitivity, LocalDateTime displayFrom, LocalDateTime displayTo,
                                    LocalDateTime contentDate,
                                    String provenance)
        throws Exception {
        try (InputStream mockFile = this.getClass().getClassLoader()
            .getResourceAsStream("data/civil-daily-cause-list/civilDailyCauseList.json")) {

            when(accountManagementService.getUserById(any())).thenReturn(piUser);
            MockHttpServletRequestBuilder mockHttpServletRequestBuilder = MockMvcRequestBuilders
                .post(PUBLICATION_URL)
                .header(PublicationConfiguration.TYPE_HEADER, ARTEFACT_TYPE)
                .header(PublicationConfiguration.PROVENANCE_HEADER, provenance)
                .header(PublicationConfiguration.SOURCE_ARTEFACT_ID_HEADER, SOURCE_ARTEFACT_ID)
                .header(PublicationConfiguration.DISPLAY_FROM_HEADER, displayFrom)
                .header(PublicationConfiguration.DISPLAY_TO_HEADER, displayTo.plusMonths(1))
                .header(PublicationConfiguration.COURT_ID, COURT_ID)
                .header(PublicationConfiguration.LIST_TYPE, ListType.CIVIL_DAILY_CAUSE_LIST)
                .header(PublicationConfiguration.CONTENT_DATE, contentDate)
                .header(PublicationConfiguration.SENSITIVITY_HEADER, sensitivity)
                .header(PublicationConfiguration.LANGUAGE_HEADER, LANGUAGE)
                .header(PublicationConfiguration.REQUESTER_ID, SYSTEM_ADMIN_ID)
                .content(mockFile.readAllBytes())
                .contentType(MediaType.APPLICATION_JSON);

            MvcResult response = mockMvc.perform(mockHttpServletRequestBuilder)
                .andExpect(status().isCreated()).andReturn();

            return objectMapper.readValue(
                response.getResponse().getContentAsString(), Artefact.class);
        }
    }

    @Test
    void testAuthorisedGetArtefactByCaseIdSearchVerified() throws Exception {
        when(accountManagementService.getUserById(any())).thenReturn(piUser);
        when(accountManagementService.getIsAuthorised(
            UUID.fromString(piUser.getUserId()), ListType.CIVIL_DAILY_CAUSE_LIST, Sensitivity.PRIVATE
        )).thenReturn(true);

        Artefact artefact = createDailyList(Sensitivity.PRIVATE);

        MockHttpServletRequestBuilder mockHttpServletRequestBuilder1 =
            MockMvcRequestBuilders.get(SEARCH_URL + VALID_CASE_ID_SEARCH);

        mockHttpServletRequestBuilder1
            .header(REQUESTER_ID_HEADER, SYSTEM_ADMIN_ID);

        MvcResult getResponse = mockMvc.perform(mockHttpServletRequestBuilder1)
            .andExpect(status().isOk())
            .andReturn();

        assertTrue(
            getResponse.getResponse().getContentAsString().contains(artefact.getArtefactId().toString()),
            SHOULD_RETURN_EXPECTED_ARTEFACT
        );
    }

    @Test
    void testUnauthorisedGetArtefactByCaseIdSearchVerified() throws Exception {
        when(accountManagementService.getUserById(any())).thenReturn(piUser);
        when(accountManagementService.getIsAuthorised(
            UUID.fromString(piUser.getUserId()), ListType.CIVIL_DAILY_CAUSE_LIST, Sensitivity.PRIVATE
        )).thenReturn(false);

        createDailyList(Sensitivity.PRIVATE);

        MockHttpServletRequestBuilder mockHttpServletRequestBuilder1 =
            MockMvcRequestBuilders.get(SEARCH_URL + VALID_CASE_ID_SEARCH);

        mockHttpServletRequestBuilder1
            .header(REQUESTER_ID_HEADER, SYSTEM_ADMIN_ID);

        MvcResult getResponse = mockMvc.perform(mockHttpServletRequestBuilder1)
            .andExpect(status().isNotFound())
            .andReturn();

        assertTrue(
            getResponse.getResponse().getContentAsString().contains("No Artefacts found"),
            SHOULD_RETURN_EXPECTED_ARTEFACT
        );
    }

    @Test
    void testGetArtefactByCaseIdSearchUnverified() throws Exception {
        when(accountManagementService.getUserById(any())).thenReturn(piUser);
        Artefact artefact = createDailyList(Sensitivity.PUBLIC);

        MockHttpServletRequestBuilder mockHttpServletRequestBuilder1 =
            MockMvcRequestBuilders.get(SEARCH_URL + VALID_CASE_ID_SEARCH)
                .header(REQUESTER_ID_HEADER, SYSTEM_ADMIN_ID);

        MvcResult getResponse =
            mockMvc.perform(mockHttpServletRequestBuilder1).andExpect(status().isOk()).andReturn();

        assertTrue(
            getResponse.getResponse().getContentAsString().contains(artefact.getArtefactId().toString()),
            SHOULD_RETURN_EXPECTED_ARTEFACT
        );
    }

    @Test
    void testGetArtefactByCaseIdSearchUnverifiedNotFound() throws Exception {
        when(accountManagementService.getUserById(any())).thenReturn(piUser);
        createDailyList(Sensitivity.CLASSIFIED);

        MockHttpServletRequestBuilder mockHttpServletRequestBuilder1 =
            MockMvcRequestBuilders.get(SEARCH_URL + VALID_CASE_ID_SEARCH)
                .header(REQUESTER_ID_HEADER, SYSTEM_ADMIN_ID);

        mockMvc.perform(mockHttpServletRequestBuilder1).andExpect(status().isNotFound()).andReturn();

    }

    @Test
    void testAuthorisedGetArtefactByCaseNameSearchVerified() throws Exception {
        when(accountManagementService.getUserById(any())).thenReturn(piUser);
        when(accountManagementService.getIsAuthorised(
            UUID.fromString(piUser.getUserId()), ListType.CIVIL_DAILY_CAUSE_LIST, Sensitivity.PRIVATE
        )).thenReturn(true);

        Artefact artefact = createDailyList(Sensitivity.PRIVATE);

        MockHttpServletRequestBuilder mockHttpServletRequestBuilder1 =
            MockMvcRequestBuilders.get(SEARCH_URL + VALID_CASE_NAME_SEARCH);

        mockHttpServletRequestBuilder1
            .header(REQUESTER_ID_HEADER, SYSTEM_ADMIN_ID);

        MvcResult getResponse = mockMvc.perform(mockHttpServletRequestBuilder1)
            .andExpect(status().isOk())
            .andReturn();

        assertTrue(
            getResponse.getResponse().getContentAsString().contains(artefact.getArtefactId().toString()),
            SHOULD_RETURN_EXPECTED_ARTEFACT
        );
    }

    @Test
    void testUnauthorisedGetArtefactByCaseNameSearchVerified() throws Exception {
        when(accountManagementService.getUserById(any())).thenReturn(piUser);
        when(accountManagementService.getIsAuthorised(
            UUID.fromString(piUser.getUserId()), ListType.CIVIL_DAILY_CAUSE_LIST, Sensitivity.PRIVATE
        )).thenReturn(false);

        createDailyList(Sensitivity.PRIVATE);

        MockHttpServletRequestBuilder mockHttpServletRequestBuilder1 =
            MockMvcRequestBuilders.get(SEARCH_URL + VALID_CASE_NAME_SEARCH);

        mockHttpServletRequestBuilder1
            .header(REQUESTER_ID_HEADER, SYSTEM_ADMIN_ID);

        MvcResult getResponse = mockMvc.perform(mockHttpServletRequestBuilder1)
            .andExpect(status().isNotFound())
            .andReturn();

        assertTrue(
            getResponse.getResponse().getContentAsString().contains("No Artefacts found"),
            SHOULD_RETURN_EXPECTED_ARTEFACT
        );
    }

    @Test
    void testGetArtefactByCaseNameSearchUnverified() throws Exception {
        when(accountManagementService.getUserById(any())).thenReturn(piUser);
        Artefact artefact = createDailyList(Sensitivity.PUBLIC);

        MockHttpServletRequestBuilder mockHttpServletRequestBuilder1 =
            MockMvcRequestBuilders.get(SEARCH_URL + VALID_CASE_NAME_SEARCH)
                .header(REQUESTER_ID_HEADER, SYSTEM_ADMIN_ID);

        MvcResult getResponse =
            mockMvc.perform(mockHttpServletRequestBuilder1).andExpect(status().isOk()).andReturn();

        assertTrue(
            getResponse.getResponse().getContentAsString().contains(artefact.getArtefactId().toString()),
            SHOULD_RETURN_EXPECTED_ARTEFACT
        );
    }

    @Test
    void testGetArtefactByCaseNameSearchUnverifiedNotFound() throws Exception {
        createDailyList(Sensitivity.CLASSIFIED);

        MockHttpServletRequestBuilder mockHttpServletRequestBuilder1 =
            MockMvcRequestBuilders.get(SEARCH_URL + VALID_CASE_NAME_SEARCH);

        mockMvc.perform(mockHttpServletRequestBuilder1).andExpect(status().isNotFound()).andReturn();
    }


    @Test
    @WithMockUser(username = UNAUTHORIZED_USERNAME, authorities = {UNAUTHORIZED_ROLE})
    void testUnauthorizedGetAllRelevantArtefactsBySearchValue() throws Exception {
        when(accountManagementService.getUserById(any())).thenReturn(piUser);
        MockHttpServletRequestBuilder request =
            MockMvcRequestBuilders.get(SEARCH_URL + VALID_CASE_NAME_SEARCH)
                .header(REQUESTER_ID_HEADER, SYSTEM_ADMIN_ID);

        MvcResult archiveResponse = mockMvc.perform(request).andExpect(status().isForbidden()).andReturn();

        assertEquals(HttpStatus.FORBIDDEN.value(), archiveResponse.getResponse().getStatus(),
                     FORBIDDEN_STATUS_CODE
        );
    }

}
