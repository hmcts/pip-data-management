package uk.gov.hmcts.reform.pip.data.management.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.zonky.test.db.AutoConfigureEmbeddedDatabase;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import uk.gov.hmcts.reform.pip.data.management.Application;
import uk.gov.hmcts.reform.pip.data.management.config.AzureBlobConfigurationTestConfiguration;
import uk.gov.hmcts.reform.pip.data.management.config.PublicationConfiguration;
import uk.gov.hmcts.reform.pip.data.management.models.publication.Artefact;
import uk.gov.hmcts.reform.pip.data.management.utils.IntegrationTestBase;
import uk.gov.hmcts.reform.pip.model.account.PiUser;
import uk.gov.hmcts.reform.pip.model.account.Roles;
import uk.gov.hmcts.reform.pip.model.publication.ArtefactType;
import uk.gov.hmcts.reform.pip.model.publication.Language;
import uk.gov.hmcts.reform.pip.model.publication.ListType;
import uk.gov.hmcts.reform.pip.model.publication.Sensitivity;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = {Application.class, AzureBlobConfigurationTestConfiguration.class},
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("integration")
@AutoConfigureEmbeddedDatabase(type = AutoConfigureEmbeddedDatabase.DatabaseType.POSTGRES)
@WithMockUser(username = "admin", authorities = {"APPROLE_api.request.admin"})
@SuppressWarnings({"PMD.ExcessiveImports"})
class NonStrategicPublicationTest extends IntegrationTestBase {
    private static final String NON_STRATEGIC_PUBLICATION_URL = "/publication/non-strategic";
    private static final String PROVENANCE = "MANUAL_UPLOAD";
    private static MockMultipartFile file;
    private static MockMultipartFile excelFile;
    private static PiUser piUser;
    private static MockMultipartFile excelFileMultiSheet;
    private static final String SOURCE_ARTEFACT_ID = "sourceArtefactId";
    private static final LocalDateTime DISPLAY_TO = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
    private static final LocalDateTime DISPLAY_FROM = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
    private static final String COURT_ID = "123";
    private static final LocalDateTime CONTENT_DATE = LocalDateTime.now().toLocalDate().atStartOfDay()
        .truncatedTo(ChronoUnit.SECONDS);
    private static final String EXCEL_FILE_TYPE = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";

    private static final String VALIDATION_EMPTY_RESPONSE = "Response should contain a Artefact";
    private static final String ARTEFACT_ID_POPULATED_MESSAGE = "Artefact ID should be populated";
    private static final String SYSTEM_ADMIN_ID = UUID.randomUUID().toString();

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Autowired
    private MockMvc mockMvc;

    @BeforeAll
    public static void setup() throws IOException {
        file = new MockMultipartFile("file", "test.pdf",
                                     MediaType.APPLICATION_PDF_VALUE, "test content".getBytes(
            StandardCharsets.UTF_8)
        );
        excelFile = createExcelMultipartFile(
            "data/non-strategic/cst-weekly-hearing-list/cstWeeklyHearingList.xlsx");
        piUser = new PiUser();
        piUser.setUserId(UUID.randomUUID().toString());
        piUser.setEmail("test@justice.gov.uk");
        piUser.setRoles(Roles.SYSTEM_ADMIN);
        excelFileMultiSheet = createExcelMultipartFile(
            "data/non-strategic/interim-applications-chd-daily-cause-list/"
                + "interimApplicationsChanceryDivisionDailyCauseList.xlsx");
        OBJECT_MAPPER.findAndRegisterModules();
    }

    private static MockMultipartFile createExcelMultipartFile(String fileName) throws IOException {
        try (InputStream inputStream = PublicationTest.class.getClassLoader()
            .getResourceAsStream(fileName)) {
            return new MockMultipartFile(
                "file", "TestFileName.xlsx", EXCEL_FILE_TYPE,
                org.testcontainers.shaded.org.apache.commons.io.IOUtils.toByteArray(inputStream)
            );
        }
    }

    @Test
    void testNonStrategicPublicationUpload() throws Exception {
        when(accountManagementService.getUserById(any())).thenReturn(piUser);
        MockHttpServletRequestBuilder mockHttpServletRequestBuilder = MockMvcRequestBuilders
            .multipart(NON_STRATEGIC_PUBLICATION_URL)
            .file(excelFile);

        mockHttpServletRequestBuilder.header(PublicationConfiguration.TYPE_HEADER, ArtefactType.LIST)
            .header(PublicationConfiguration.SENSITIVITY_HEADER, Sensitivity.PUBLIC)
            .header(PublicationConfiguration.LANGUAGE_HEADER, Language.ENGLISH)
            .header(PublicationConfiguration.PROVENANCE_HEADER, PROVENANCE)
            .header(PublicationConfiguration.SOURCE_ARTEFACT_ID_HEADER, SOURCE_ARTEFACT_ID)
            .header(PublicationConfiguration.DISPLAY_TO_HEADER, DISPLAY_TO)
            .header(PublicationConfiguration.DISPLAY_FROM_HEADER, DISPLAY_FROM)
            .header(PublicationConfiguration.LIST_TYPE, ListType.CST_WEEKLY_HEARING_LIST)
            .header(PublicationConfiguration.COURT_ID, COURT_ID)
            .header(PublicationConfiguration.CONTENT_DATE, CONTENT_DATE)
            .header(PublicationConfiguration.LANGUAGE_HEADER, Language.ENGLISH)
            .header(PublicationConfiguration.REQUESTER_ID_HEADER, SYSTEM_ADMIN_ID)
            .contentType(MediaType.MULTIPART_FORM_DATA);

        MvcResult response = mockMvc.perform(mockHttpServletRequestBuilder)
            .andExpect(status().isCreated())
            .andReturn();

        assertNotNull(response.getResponse().getContentAsString(), VALIDATION_EMPTY_RESPONSE);

        Artefact artefact = OBJECT_MAPPER.readValue(response.getResponse().getContentAsString(), Artefact.class);

        assertNotNull(artefact.getArtefactId(), ARTEFACT_ID_POPULATED_MESSAGE);
        assertEquals(artefact.getSourceArtefactId(), SOURCE_ARTEFACT_ID, "Source artefact ID "
            + "does not match input source artefact id");
        assertEquals(artefact.getType(), ArtefactType.LIST, "Artefact type does not match input artefact type");
        assertEquals(artefact.getDisplayFrom(), DISPLAY_FROM, "Display from does not match input display from");
        assertEquals(artefact.getDisplayTo(), DISPLAY_TO, "Display to does not match input display to");
        assertEquals(artefact.getProvenance(), PROVENANCE, "Provenance does not match input provenance");
        assertEquals(artefact.getLanguage(), Language.ENGLISH, "Language does not match input language");
        assertEquals(artefact.getSensitivity(), Sensitivity.PUBLIC, "Sensitivity does not match input sensitivity");
        assertTrue(artefact.getSearch().isEmpty(), "Search value does not match");
    }

    @ParameterizedTest
    @ValueSource(strings = {"cstWeeklyHearingListWithBlankRows.xlsx", "cstWeeklyHearingListEndingInBlankRows.xlsx",
        "cstWeeklyHearingListWithOnlyBlankRows.xlsx"})
    void testNonStrategicPublicationUploadWhenFileContainsEmptyRows(String fileName) throws Exception {
        MockMultipartFile fileWithBlankRows = createExcelMultipartFile(
            "data/non-strategic/other-test-files/" + fileName);
        when(accountManagementService.getUserById(any())).thenReturn(piUser);

        MockHttpServletRequestBuilder mockHttpServletRequestBuilder = MockMvcRequestBuilders
            .multipart(NON_STRATEGIC_PUBLICATION_URL)
            .file(fileWithBlankRows);

        mockHttpServletRequestBuilder.header(PublicationConfiguration.TYPE_HEADER, ArtefactType.LIST)
            .header(PublicationConfiguration.SENSITIVITY_HEADER, Sensitivity.PUBLIC)
            .header(PublicationConfiguration.LANGUAGE_HEADER, Language.ENGLISH)
            .header(PublicationConfiguration.PROVENANCE_HEADER, PROVENANCE)
            .header(PublicationConfiguration.SOURCE_ARTEFACT_ID_HEADER, SOURCE_ARTEFACT_ID)
            .header(PublicationConfiguration.DISPLAY_TO_HEADER, DISPLAY_TO)
            .header(PublicationConfiguration.DISPLAY_FROM_HEADER, DISPLAY_FROM)
            .header(PublicationConfiguration.LIST_TYPE, ListType.CST_WEEKLY_HEARING_LIST)
            .header(PublicationConfiguration.COURT_ID, COURT_ID)
            .header(PublicationConfiguration.CONTENT_DATE, CONTENT_DATE)
            .header(PublicationConfiguration.LANGUAGE_HEADER, Language.ENGLISH)
            .header(PublicationConfiguration.REQUESTER_ID_HEADER, SYSTEM_ADMIN_ID)
            .contentType(MediaType.MULTIPART_FORM_DATA);

        MvcResult response = mockMvc.perform(mockHttpServletRequestBuilder)
            .andExpect(status().isCreated())
            .andReturn();

        assertNotNull(response.getResponse().getContentAsString(), VALIDATION_EMPTY_RESPONSE);
    }

    @Test
    void testNonStrategicUploadOfExistingPublication() throws Exception {
        when(accountManagementService.getUserById(any())).thenReturn(piUser);

        MockHttpServletRequestBuilder mockHttpServletRequestBuilder = MockMvcRequestBuilders
            .multipart(NON_STRATEGIC_PUBLICATION_URL)
            .file(excelFile);

        mockHttpServletRequestBuilder.header(PublicationConfiguration.TYPE_HEADER, ArtefactType.LIST);
        mockHttpServletRequestBuilder.header(PublicationConfiguration.SENSITIVITY_HEADER, Sensitivity.PUBLIC);
        mockHttpServletRequestBuilder.header(PublicationConfiguration.PROVENANCE_HEADER, PROVENANCE);
        mockHttpServletRequestBuilder.header(PublicationConfiguration.SOURCE_ARTEFACT_ID_HEADER, SOURCE_ARTEFACT_ID);
        mockHttpServletRequestBuilder.header(PublicationConfiguration.DISPLAY_TO_HEADER, DISPLAY_TO);
        mockHttpServletRequestBuilder.header(PublicationConfiguration.DISPLAY_FROM_HEADER, DISPLAY_FROM);
        mockHttpServletRequestBuilder.header(PublicationConfiguration.LIST_TYPE, ListType.CST_WEEKLY_HEARING_LIST);
        mockHttpServletRequestBuilder.header(PublicationConfiguration.COURT_ID, COURT_ID);
        mockHttpServletRequestBuilder.header(PublicationConfiguration.CONTENT_DATE, CONTENT_DATE);
        mockHttpServletRequestBuilder.header(PublicationConfiguration.LANGUAGE_HEADER, Language.ENGLISH);
        mockHttpServletRequestBuilder.header(PublicationConfiguration.REQUESTER_ID_HEADER, SYSTEM_ADMIN_ID);
        mockHttpServletRequestBuilder.contentType(MediaType.MULTIPART_FORM_DATA);

        final MvcResult createResponse = mockMvc.perform(mockHttpServletRequestBuilder)
            .andExpect(status().isCreated())
            .andReturn();

        mockHttpServletRequestBuilder = MockMvcRequestBuilders
            .multipart(NON_STRATEGIC_PUBLICATION_URL)
            .file(excelFile);

        // Update the Display To header and resend publication
        mockHttpServletRequestBuilder.header(PublicationConfiguration.TYPE_HEADER, ArtefactType.LIST);
        mockHttpServletRequestBuilder.header(PublicationConfiguration.SENSITIVITY_HEADER, Sensitivity.PUBLIC);
        mockHttpServletRequestBuilder.header(PublicationConfiguration.PROVENANCE_HEADER, PROVENANCE);
        mockHttpServletRequestBuilder.header(PublicationConfiguration.SOURCE_ARTEFACT_ID_HEADER, SOURCE_ARTEFACT_ID);
        mockHttpServletRequestBuilder.header(PublicationConfiguration.DISPLAY_TO_HEADER, DISPLAY_TO.plusMonths(1));
        mockHttpServletRequestBuilder.header(PublicationConfiguration.DISPLAY_FROM_HEADER, DISPLAY_FROM);
        mockHttpServletRequestBuilder.header(PublicationConfiguration.LIST_TYPE, ListType.CST_WEEKLY_HEARING_LIST);
        mockHttpServletRequestBuilder.header(PublicationConfiguration.COURT_ID, COURT_ID);
        mockHttpServletRequestBuilder.header(PublicationConfiguration.CONTENT_DATE, CONTENT_DATE);
        mockHttpServletRequestBuilder.header(PublicationConfiguration.LANGUAGE_HEADER, Language.ENGLISH);
        mockHttpServletRequestBuilder.header(PublicationConfiguration.REQUESTER_ID_HEADER, SYSTEM_ADMIN_ID);
        mockHttpServletRequestBuilder.contentType(MediaType.MULTIPART_FORM_DATA);

        final MvcResult updatedResponse = mockMvc.perform(mockHttpServletRequestBuilder)
            .andExpect(status().isCreated())
            .andReturn();

        Artefact createdArtefact = OBJECT_MAPPER.readValue(createResponse.getResponse().getContentAsString(),
                                                          Artefact.class);

        Artefact updatedArtefact = OBJECT_MAPPER.readValue(updatedResponse.getResponse().getContentAsString(),
                                                          Artefact.class);

        assertEquals(createdArtefact.getArtefactId(), updatedArtefact.getArtefactId(), "A new artefact has "
            + "been created rather than it being updated");

        assertEquals(DISPLAY_TO.plusMonths(1), updatedArtefact.getDisplayTo(), "The updated artefact does "
            + "not contain the new Display To value");
    }

    @Test
    void testNonStrategicPublicationUploadWithIncorrectFileType() throws Exception {
        when(accountManagementService.getUserById(any())).thenReturn(piUser);

        MockHttpServletRequestBuilder mockHttpServletRequestBuilder = MockMvcRequestBuilders
            .multipart(NON_STRATEGIC_PUBLICATION_URL)
            .file(file);

        mockHttpServletRequestBuilder.header(PublicationConfiguration.TYPE_HEADER, ArtefactType.LIST)
            .header(PublicationConfiguration.SENSITIVITY_HEADER, Sensitivity.PUBLIC)
            .header(PublicationConfiguration.PROVENANCE_HEADER, PROVENANCE)
            .header(PublicationConfiguration.SOURCE_ARTEFACT_ID_HEADER, SOURCE_ARTEFACT_ID)
            .header(PublicationConfiguration.DISPLAY_TO_HEADER, DISPLAY_TO)
            .header(PublicationConfiguration.DISPLAY_FROM_HEADER, DISPLAY_FROM)
            .header(PublicationConfiguration.LIST_TYPE, ListType.CIVIL_DAILY_CAUSE_LIST)
            .header(PublicationConfiguration.COURT_ID, COURT_ID)
            .header(PublicationConfiguration.CONTENT_DATE, CONTENT_DATE)
            .header(PublicationConfiguration.LANGUAGE_HEADER, Language.ENGLISH)
            .header(PublicationConfiguration.REQUESTER_ID_HEADER, SYSTEM_ADMIN_ID)
            .contentType(MediaType.MULTIPART_FORM_DATA);

        MvcResult response = mockMvc.perform(mockHttpServletRequestBuilder)
            .andExpect(status().isBadRequest())
            .andReturn();

        assertTrue(response.getResponse().getContentAsString().contains("Invalid Excel file type"),
                   "Returned message does not match");
    }

    @Test
    void testNonStrategicPublicationUploadWhenFileContainsMultipleSheets() throws Exception {
        MockHttpServletRequestBuilder mockHttpServletRequestBuilder = MockMvcRequestBuilders
            .multipart(NON_STRATEGIC_PUBLICATION_URL)
            .file(excelFileMultiSheet);

        mockHttpServletRequestBuilder.header(PublicationConfiguration.TYPE_HEADER, ArtefactType.LIST)
            .header(PublicationConfiguration.SENSITIVITY_HEADER, Sensitivity.PUBLIC)
            .header(PublicationConfiguration.LANGUAGE_HEADER, Language.ENGLISH)
            .header(PublicationConfiguration.PROVENANCE_HEADER, PROVENANCE)
            .header(PublicationConfiguration.SOURCE_ARTEFACT_ID_HEADER, SOURCE_ARTEFACT_ID)
            .header(PublicationConfiguration.DISPLAY_TO_HEADER, DISPLAY_TO)
            .header(PublicationConfiguration.DISPLAY_FROM_HEADER, DISPLAY_FROM)
            .header(PublicationConfiguration.LIST_TYPE, ListType.INTERIM_APPLICATIONS_CHD_DAILY_CAUSE_LIST)
            .header(PublicationConfiguration.COURT_ID, COURT_ID)
            .header(PublicationConfiguration.CONTENT_DATE, CONTENT_DATE)
            .header(PublicationConfiguration.LANGUAGE_HEADER, Language.ENGLISH)
            .contentType(MediaType.MULTIPART_FORM_DATA);

        MvcResult response = mockMvc.perform(mockHttpServletRequestBuilder)
            .andExpect(status().isCreated())
            .andReturn();

        assertNotNull(response.getResponse().getContentAsString(), VALIDATION_EMPTY_RESPONSE);

        Artefact artefact = OBJECT_MAPPER.readValue(response.getResponse().getContentAsString(), Artefact.class);

        assertNotNull(artefact.getArtefactId(), ARTEFACT_ID_POPULATED_MESSAGE);
        assertEquals(artefact.getSourceArtefactId(), SOURCE_ARTEFACT_ID, "Source artefact ID "
            + "does not match input source artefact id");
        assertEquals(artefact.getType(), ArtefactType.LIST, "Artefact type does not match input artefact type");
        assertEquals(artefact.getDisplayFrom(), DISPLAY_FROM, "Display from does not match input display from");
        assertEquals(artefact.getDisplayTo(), DISPLAY_TO, "Display to does not match input display to");
        assertEquals(artefact.getProvenance(), PROVENANCE, "Provenance does not match input provenance");
        assertEquals(artefact.getLanguage(), Language.ENGLISH, "Language does not match input language");
        assertEquals(artefact.getSensitivity(), Sensitivity.PUBLIC, "Sensitivity does not match input sensitivity");
        assertTrue(artefact.getSearch().isEmpty(), "Search value does not match");
    }

}
