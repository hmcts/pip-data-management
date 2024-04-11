package uk.gov.hmcts.reform.pip.data.management.controllers;

import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.zonky.test.db.AutoConfigureEmbeddedDatabase;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
import uk.gov.hmcts.reform.pip.model.publication.ArtefactType;
import uk.gov.hmcts.reform.pip.model.publication.Language;
import uk.gov.hmcts.reform.pip.model.publication.ListType;
import uk.gov.hmcts.reform.pip.model.publication.Sensitivity;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = {Application.class},
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@ActiveProfiles(profiles = "functional")
@SuppressWarnings({"PMD.ExcessiveImports", "PMD.ExcessiveClassLength",
    "PMD.CyclomaticComplexity", "PMD.TooManyMethods", "PMD.LawOfDemeter"})
@AutoConfigureEmbeddedDatabase(type = AutoConfigureEmbeddedDatabase.DatabaseType.POSTGRES)
@WithMockUser(username = "admin", authorities = {"APPROLE_api.request.admin"})
class PublicationSubscriptionSearchTest {

    @Autowired
    BlobContainerClient blobContainerClient;

    @Autowired
    BlobClient blobClient;

    @Autowired
    private MockMvc mockMvc;

    @Value("${test-user-id}")
    private String userId;

    private static final String PARTY_NAME_SEARCH = "/PARTY_NAME/Applicant";
    private static final String PARTY_NAME_SEARCH_REPRESENTATIVE = "/PARTY_NAME/Rep";
    private static final String VALID_CASE_ID_SEARCH = "/CASE_ID/45684548";
    private static final String PROVENANCE = "MANUAL_UPLOAD";
    private static final Sensitivity SENSITIVITY = Sensitivity.PUBLIC;
    private static final String SHOULD_RETURN_EXPECTED_ARTEFACT = "Should return expected artefact";
    private static final LocalDateTime CONTENT_DATE = LocalDateTime.now().toLocalDate().atStartOfDay()
        .truncatedTo(ChronoUnit.SECONDS);
    private static final String PAYLOAD_URL = "/payload";
    private static final String SEARCH_URL = "/publication/search";
    private static final String USER_ID_HEADER = "x-user-id";
    private static final String VALID_CASE_NAME_SEARCH = "/CASE_NAME/Smith";
    private static final String PUBLICATION_URL = "/publication";
    private static final String ISSUER_HEADER = "x-issuer-id";
    private static final String EMAIL = "test@email.com";
    private static final String SOURCE_ARTEFACT_ID = "sourceArtefactId";
    private static final String COURT_ID = "123";
    private static final Language LANGUAGE = Language.ENGLISH;
    private static final ArtefactType ARTEFACT_TYPE = ArtefactType.LIST;
    private static final LocalDateTime DISPLAY_TO = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
    private static final LocalDateTime DISPLAY_FROM = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
    private static final String UNAUTHORIZED_USERNAME = "unauthorized_username";
    private static final String UNAUTHORIZED_ROLE = "APPROLE_unknown.role";
    private static final String FORBIDDEN_STATUS_CODE = "Status code does not match forbidden";
    private static ObjectMapper objectMapper;

    @BeforeAll
    public static void setup() throws IOException {
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

    Artefact createDailyList(LocalDateTime displayFrom, LocalDateTime displayTo)
        throws Exception {
        return this.createDailyList(SENSITIVITY, displayFrom, displayTo, CONTENT_DATE, PROVENANCE);
    }

    public Artefact createDailyList(Sensitivity sensitivity, LocalDateTime displayFrom, LocalDateTime displayTo,
                                    LocalDateTime contentDate,
                                    String provenance)
        throws Exception {
        try (InputStream mockFile = this.getClass().getClassLoader()
            .getResourceAsStream("data/civil-daily-cause-list/civilDailyCauseList.json")) {

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
                .content(mockFile.readAllBytes())
                .contentType(MediaType.APPLICATION_JSON);

            MvcResult response = mockMvc.perform(mockHttpServletRequestBuilder)
                .andExpect(status().isCreated()).andReturn();

            return objectMapper.readValue(
                response.getResponse().getContentAsString(), Artefact.class);
        }
    }

    @Test
    void testGetArtefactByCaseIdSearchVerified() throws Exception {
        when(blobContainerClient.getBlobClient(any())).thenReturn(blobClient);
        when(blobContainerClient.getBlobContainerUrl()).thenReturn(PAYLOAD_URL);

        Artefact artefact = createDailyList(Sensitivity.PRIVATE);

        MockHttpServletRequestBuilder mockHttpServletRequestBuilder1 =
            MockMvcRequestBuilders.get(SEARCH_URL + VALID_CASE_ID_SEARCH);

        mockHttpServletRequestBuilder1
            .header(USER_ID_HEADER, userId);

        MvcResult getResponse =
            mockMvc.perform(mockHttpServletRequestBuilder1).andExpect(status().isOk()).andReturn();

        assertTrue(
            getResponse.getResponse().getContentAsString().contains(artefact.getArtefactId().toString()),
            SHOULD_RETURN_EXPECTED_ARTEFACT
        );
    }

    @Test
    void testGetArtefactByCaseIdSearchUnverified() throws Exception {
        when(blobContainerClient.getBlobClient(any())).thenReturn(blobClient);
        when(blobContainerClient.getBlobContainerUrl()).thenReturn(PAYLOAD_URL);

        Artefact artefact = createDailyList(Sensitivity.PUBLIC);

        MockHttpServletRequestBuilder mockHttpServletRequestBuilder1 =
            MockMvcRequestBuilders.get(SEARCH_URL + VALID_CASE_ID_SEARCH);

        MvcResult getResponse =
            mockMvc.perform(mockHttpServletRequestBuilder1).andExpect(status().isOk()).andReturn();

        assertTrue(
            getResponse.getResponse().getContentAsString().contains(artefact.getArtefactId().toString()),
            SHOULD_RETURN_EXPECTED_ARTEFACT
        );
    }

    @Test
    void testGetArtefactByCaseIdSearchUnverifiedNotFound() throws Exception {
        when(blobContainerClient.getBlobClient(any())).thenReturn(blobClient);
        when(blobContainerClient.getBlobContainerUrl()).thenReturn(PAYLOAD_URL);

        createDailyList(Sensitivity.CLASSIFIED);

        MockHttpServletRequestBuilder mockHttpServletRequestBuilder1 =
            MockMvcRequestBuilders.get(SEARCH_URL + VALID_CASE_ID_SEARCH);

        mockMvc.perform(mockHttpServletRequestBuilder1).andExpect(status().isNotFound()).andReturn();

    }

    @Test
    void testGetArtefactByCaseNameSearchVerified() throws Exception {
        when(blobContainerClient.getBlobClient(any())).thenReturn(blobClient);
        when(blobContainerClient.getBlobContainerUrl()).thenReturn(PAYLOAD_URL);

        Artefact artefact = createDailyList(Sensitivity.PRIVATE);

        MockHttpServletRequestBuilder mockHttpServletRequestBuilder1 =
            MockMvcRequestBuilders.get(SEARCH_URL + VALID_CASE_NAME_SEARCH);

        mockHttpServletRequestBuilder1
            .header(USER_ID_HEADER, userId);

        MvcResult getResponse =
            mockMvc.perform(mockHttpServletRequestBuilder1).andExpect(status().isOk()).andReturn();

        assertTrue(
            getResponse.getResponse().getContentAsString().contains(artefact.getArtefactId().toString()),
            SHOULD_RETURN_EXPECTED_ARTEFACT
        );
    }

    @Test
    void testGetArtefactByCaseNameSearchUnverified() throws Exception {
        when(blobContainerClient.getBlobClient(any())).thenReturn(blobClient);
        when(blobContainerClient.getBlobContainerUrl()).thenReturn(PAYLOAD_URL);

        Artefact artefact = createDailyList(Sensitivity.PUBLIC);

        MockHttpServletRequestBuilder mockHttpServletRequestBuilder1 =
            MockMvcRequestBuilders.get(SEARCH_URL + VALID_CASE_NAME_SEARCH);

        MvcResult getResponse =
            mockMvc.perform(mockHttpServletRequestBuilder1).andExpect(status().isOk()).andReturn();

        assertTrue(
            getResponse.getResponse().getContentAsString().contains(artefact.getArtefactId().toString()),
            SHOULD_RETURN_EXPECTED_ARTEFACT
        );
    }

    @Test
    void testGetArtefactByCaseNameSearchUnverifiedNotFound() throws Exception {
        when(blobContainerClient.getBlobClient(any())).thenReturn(blobClient);
        when(blobContainerClient.getBlobContainerUrl()).thenReturn(PAYLOAD_URL);

        createDailyList(Sensitivity.CLASSIFIED);

        MockHttpServletRequestBuilder mockHttpServletRequestBuilder1 =
            MockMvcRequestBuilders.get(SEARCH_URL + VALID_CASE_NAME_SEARCH);

        mockMvc.perform(mockHttpServletRequestBuilder1).andExpect(status().isNotFound()).andReturn();
    }

    @Test
    void testGetArtefactByPartiesSearchVerified() throws Exception {
        when(blobContainerClient.getBlobClient(any())).thenReturn(blobClient);
        when(blobContainerClient.getBlobContainerUrl()).thenReturn(PAYLOAD_URL);

        Artefact artefact = createDailyList(Sensitivity.PRIVATE);

        MockHttpServletRequestBuilder mockHttpServletRequestBuilder1 =
            MockMvcRequestBuilders.get(SEARCH_URL + PARTY_NAME_SEARCH);

        mockHttpServletRequestBuilder1
            .header(USER_ID_HEADER, userId);

        MvcResult getResponse =
            mockMvc.perform(mockHttpServletRequestBuilder1).andExpect(status().isOk()).andReturn();

        assertTrue(
            getResponse.getResponse().getContentAsString().contains(artefact.getArtefactId().toString()),
            SHOULD_RETURN_EXPECTED_ARTEFACT
        );
    }

    @Test
    void testGetArtefactByPartiesSearchUnverified() throws Exception {
        when(blobContainerClient.getBlobClient(any())).thenReturn(blobClient);
        when(blobContainerClient.getBlobContainerUrl()).thenReturn(PAYLOAD_URL);

        Artefact artefact = createDailyList(Sensitivity.PUBLIC);

        MockHttpServletRequestBuilder mockHttpServletRequestBuilder1 =
            MockMvcRequestBuilders.get(SEARCH_URL + PARTY_NAME_SEARCH);

        MvcResult getResponse =
            mockMvc.perform(mockHttpServletRequestBuilder1).andExpect(status().isOk()).andReturn();

        assertTrue(
            getResponse.getResponse().getContentAsString().contains(artefact.getArtefactId().toString()),
            SHOULD_RETURN_EXPECTED_ARTEFACT
        );
    }

    @Test
    void testGetArtefactByPartiesSearchUnverifiedRepresentative() throws Exception {
        when(blobContainerClient.getBlobClient(any())).thenReturn(blobClient);
        when(blobContainerClient.getBlobContainerUrl()).thenReturn(PAYLOAD_URL);

        createDailyList(Sensitivity.CLASSIFIED);

        MockHttpServletRequestBuilder mockHttpServletRequestBuilder1 =
            MockMvcRequestBuilders.get(SEARCH_URL + PARTY_NAME_SEARCH_REPRESENTATIVE);

        mockMvc.perform(mockHttpServletRequestBuilder1).andExpect(status().isNotFound()).andReturn();
    }

    @Test
    void testGetArtefactByPartiesSearchUnverifiedNotFound() throws Exception {
        when(blobContainerClient.getBlobClient(any())).thenReturn(blobClient);
        when(blobContainerClient.getBlobContainerUrl()).thenReturn(PAYLOAD_URL);

        createDailyList(Sensitivity.CLASSIFIED);

        MockHttpServletRequestBuilder mockHttpServletRequestBuilder1 =
            MockMvcRequestBuilders.get(SEARCH_URL + PARTY_NAME_SEARCH);

        mockMvc.perform(mockHttpServletRequestBuilder1).andExpect(status().isNotFound()).andReturn();
    }

    @Test
    void testGetArtefactByPartiesSearchOutOfDateRangePast() throws Exception {
        when(blobContainerClient.getBlobClient(any())).thenReturn(blobClient);
        when(blobContainerClient.getBlobContainerUrl()).thenReturn(PAYLOAD_URL);

        createDailyList(LocalDateTime.now().minusMonths(2), LocalDateTime.now().minusMonths(2));

        MockHttpServletRequestBuilder mockHttpServletRequestBuilder1 =
            MockMvcRequestBuilders.get(SEARCH_URL + PARTY_NAME_SEARCH);

        mockMvc.perform(mockHttpServletRequestBuilder1).andExpect(status().isNotFound()).andReturn();
    }

    @Test
    void testGetArtefactByPartiesSearchOutOfDateRangeFuture() throws Exception {
        when(blobContainerClient.getBlobClient(any())).thenReturn(blobClient);
        when(blobContainerClient.getBlobContainerUrl()).thenReturn(PAYLOAD_URL);

        createDailyList(LocalDateTime.now().plusMonths(1), LocalDateTime.now().plusMonths(2));

        MockHttpServletRequestBuilder mockHttpServletRequestBuilder1 =
            MockMvcRequestBuilders.get(SEARCH_URL + PARTY_NAME_SEARCH);

        mockMvc.perform(mockHttpServletRequestBuilder1).andExpect(status().isNotFound()).andReturn();
    }

    @Test
    void testGetArtefactByPartiesSearchIsArchived() throws Exception {
        when(blobContainerClient.getBlobClient(any())).thenReturn(blobClient);
        when(blobContainerClient.getBlobContainerUrl()).thenReturn(PAYLOAD_URL);

        Artefact artefact = createDailyList(Sensitivity.PUBLIC);

        MockHttpServletRequestBuilder deleteRequest = MockMvcRequestBuilders
            .delete(PUBLICATION_URL + "/" + artefact.getArtefactId())
            .header(ISSUER_HEADER, EMAIL);

        mockMvc.perform(deleteRequest).andExpect(status().isOk());

        MockHttpServletRequestBuilder mockHttpServletRequestBuilder1 =
            MockMvcRequestBuilders.get(SEARCH_URL + PARTY_NAME_SEARCH);

        mockMvc.perform(mockHttpServletRequestBuilder1).andExpect(status().isNotFound()).andReturn();
    }

    @Test
    void testGetArtefactByPartiesSearchDisplayToBlank() throws Exception {
        when(blobContainerClient.getBlobClient(any())).thenReturn(blobClient);
        when(blobContainerClient.getBlobContainerUrl()).thenReturn(PAYLOAD_URL);

        try (InputStream mockFile = this.getClass().getClassLoader()
            .getResourceAsStream("data/civil-daily-cause-list/civilDailyCauseList.json")) {

            MockHttpServletRequestBuilder mockHttpServletRequestBuilder = MockMvcRequestBuilders
                .post(PUBLICATION_URL)
                .header(PublicationConfiguration.TYPE_HEADER, ArtefactType.GENERAL_PUBLICATION)
                .header(PublicationConfiguration.PROVENANCE_HEADER, PROVENANCE)
                .header(PublicationConfiguration.SOURCE_ARTEFACT_ID_HEADER, SOURCE_ARTEFACT_ID)
                .header(PublicationConfiguration.DISPLAY_FROM_HEADER, LocalDateTime.now())
                .header(PublicationConfiguration.COURT_ID, COURT_ID)
                .header(PublicationConfiguration.LIST_TYPE, ListType.CIVIL_DAILY_CAUSE_LIST)
                .header(PublicationConfiguration.CONTENT_DATE, CONTENT_DATE)
                .header(PublicationConfiguration.SENSITIVITY_HEADER, SENSITIVITY)
                .header(PublicationConfiguration.LANGUAGE_HEADER, LANGUAGE)
                .content(mockFile.readAllBytes())
                .contentType(MediaType.APPLICATION_JSON);

            MvcResult artefactResponse = mockMvc.perform(mockHttpServletRequestBuilder)
                .andExpect(status().isCreated()).andReturn();

            Artefact artefact = objectMapper.readValue(
                artefactResponse.getResponse().getContentAsString(), Artefact.class);

            MockHttpServletRequestBuilder mockHttpServletRequestBuilder1 =
                MockMvcRequestBuilders.get(SEARCH_URL + PARTY_NAME_SEARCH);

            MvcResult getResponse =
                mockMvc.perform(mockHttpServletRequestBuilder1).andExpect(status().isOk()).andReturn();

            assertTrue(
                getResponse.getResponse().getContentAsString().contains(artefact.getArtefactId().toString()),
                SHOULD_RETURN_EXPECTED_ARTEFACT
            );
        }
    }

    @Test
    @WithMockUser(username = UNAUTHORIZED_USERNAME, authorities = {UNAUTHORIZED_ROLE})
    void testUnauthorizedGetAllRelevantArtefactsBySearchValue() throws Exception {
        MockHttpServletRequestBuilder request =
            MockMvcRequestBuilders.get(SEARCH_URL + VALID_CASE_NAME_SEARCH);

        MvcResult archiveResponse = mockMvc.perform(request).andExpect(status().isForbidden()).andReturn();

        assertEquals(HttpStatus.FORBIDDEN.value(), archiveResponse.getResponse().getStatus(),
                     FORBIDDEN_STATUS_CODE
        );
    }

}
