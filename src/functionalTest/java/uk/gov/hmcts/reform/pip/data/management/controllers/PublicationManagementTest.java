package uk.gov.hmcts.reform.pip.data.management.controllers;

import com.azure.core.util.BinaryData;
import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.zonky.test.db.AutoConfigureEmbeddedDatabase;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import uk.gov.hmcts.reform.pip.data.management.Application;
import uk.gov.hmcts.reform.pip.data.management.config.PublicationConfiguration;
import uk.gov.hmcts.reform.pip.data.management.errorhandling.ExceptionResponse;
import uk.gov.hmcts.reform.pip.data.management.models.publication.Artefact;
import uk.gov.hmcts.reform.pip.model.publication.ArtefactType;
import uk.gov.hmcts.reform.pip.model.publication.Language;
import uk.gov.hmcts.reform.pip.model.publication.ListType;
import uk.gov.hmcts.reform.pip.model.publication.Sensitivity;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.pip.model.publication.FileType.PDF;

@SpringBootTest(classes = Application.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("functional")
@AutoConfigureMockMvc
@AutoConfigureEmbeddedDatabase(type = AutoConfigureEmbeddedDatabase.DatabaseType.POSTGRES)
@WithMockUser(username = "admin", authorities = {"APPROLE_api.request.admin"})
@SuppressWarnings({"PMD.TooManyMethods", "PMD.ExcessiveImports"})
class PublicationManagementTest {
    private static final String ROOT_URL = "/publication";
    private static final String GET_ARTEFACT_SUMMARY = ROOT_URL + "/%s/summary";
    private static final String GET_FILE_URL = ROOT_URL + "/%s/%s";
    private static final String ARTEFACT_ID = UUID.randomUUID().toString();
    private static final String ARTEFACT_ID_NOT_FOUND = UUID.randomUUID().toString();
    private static final String ARTEFACT_NOT_FOUND_MESSAGE = "No artefact found with the ID: ";
    private static final String NOT_FOUND_RESPONSE_MESSAGE = "Artefact not found message does not match";
    private static final String CONTENT_MISMATCH_ERROR = "Artefact summary content should match";
    private static final String FILE_TYPE_HEADER = "x-file-type";
    private static final String UNAUTHORIZED_USERNAME = "unauthorized_username";
    private static final String UNAUTHORIZED_ROLE = "APPROLE_unknown.role";
    private static final String SYSTEM_HEADER = "x-system";
    private static final String CASE_REFERENCE_FIELD = "Case reference - 12341234";
    private static final String HEARING_TYPE_FIELD = "Hearing type - Directions";

    private static final LocalDateTime DISPLAY_TO = LocalDateTime.now()
        .truncatedTo(ChronoUnit.SECONDS);
    private static final LocalDateTime DISPLAY_FROM = LocalDateTime.now().plusDays(1)
        .truncatedTo(ChronoUnit.SECONDS);
    private static final LocalDateTime CONTENT_DATE = LocalDateTime.now().toLocalDate().atStartOfDay()
        .truncatedTo(ChronoUnit.SECONDS);
    private static final String PROVENANCE = "MANUAL_UPLOAD";

    private static final String BLOB_PAYLOAD_URL = "https://localhost";

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private static MockMultipartFile file;

    @Autowired
    private MockMvc mockMvc;

    @MockBean(name = "artefact")
    BlobContainerClient artefcatBlobContainerClient;

    @MockBean(name = "publications")
    BlobContainerClient publicationBlobContainerClient;

    @MockBean
    BlobClient blobClient;

    @Value("${TEST_USER_ID}")
    private String verifiedUserId;

    @BeforeAll
    static void setup() {
        file = new MockMultipartFile("file", "test.pdf",
                                     MediaType.APPLICATION_PDF_VALUE, "test content".getBytes(
            StandardCharsets.UTF_8)
        );

        OBJECT_MAPPER.findAndRegisterModules();
    }

    @BeforeEach
    void init() {
        when(artefcatBlobContainerClient.getBlobClient(any())).thenReturn(blobClient);
        when(artefcatBlobContainerClient.getBlobContainerUrl()).thenReturn(BLOB_PAYLOAD_URL);

        when(publicationBlobContainerClient.getBlobClient(any())).thenReturn(blobClient);
    }

    private byte[] getTestData(String resourceName) throws IOException {
        byte[] data;
        try (InputStream mockFile = this.getClass().getClassLoader()
            .getResourceAsStream(resourceName)) {
            data = mockFile.readAllBytes();
        }
        return data;
    }

    private Artefact createPublication(ListType listType, byte[] data) throws Exception {
        MockHttpServletRequestBuilder mockHttpServletRequestBuilder = MockMvcRequestBuilders
            .post("/publication")
            .header(PublicationConfiguration.TYPE_HEADER, ArtefactType.LIST)
            .header(PublicationConfiguration.PROVENANCE_HEADER, PROVENANCE)
            .header(PublicationConfiguration.DISPLAY_FROM_HEADER, DISPLAY_FROM)
            .header(PublicationConfiguration.DISPLAY_TO_HEADER, DISPLAY_TO)
            .header(PublicationConfiguration.COURT_ID, "5")
            .header(PublicationConfiguration.LIST_TYPE, listType)
            .header(PublicationConfiguration.CONTENT_DATE, CONTENT_DATE)
            .header(PublicationConfiguration.SENSITIVITY_HEADER, Sensitivity.PUBLIC)
            .header(PublicationConfiguration.LANGUAGE_HEADER, Language.ENGLISH)
            .content(data)
            .contentType(MediaType.APPLICATION_JSON);

        MvcResult response = mockMvc.perform(mockHttpServletRequestBuilder)
            .andExpect(status().isCreated()).andReturn();

        return OBJECT_MAPPER.readValue(
            response.getResponse().getContentAsString(), Artefact.class);
    }

    private Artefact createSjpPublicListPublication() throws Exception {
        byte[] testPublication = getTestData("data/sjp-public-list/sjpPublicList.json");
        return createPublication(ListType.SJP_PUBLIC_LIST, testPublication);
    }

    @Test
    void testGenerateArtefactSummaryCareStandardsList() throws Exception {
        byte[] data = getTestData("data/care-standards-list/careStandardsList.json");
        Artefact artefact = createPublication(ListType.CARE_STANDARDS_LIST, data);

        when(blobClient.downloadContent()).thenReturn(BinaryData.fromBytes(data));

        MvcResult response = mockMvc.perform(
                get(String.format(GET_ARTEFACT_SUMMARY, artefact.getArtefactId())))
            .andExpect(status().isOk()).andReturn();
        String responseContent = response.getResponse().getContentAsString();

        assertTrue(responseContent.contains("Case name - A Vs B"), CONTENT_MISMATCH_ERROR);
        assertTrue(responseContent.contains("Hearing date - 04 October"), CONTENT_MISMATCH_ERROR);
        assertTrue(responseContent.contains("Hearing type - mda"), CONTENT_MISMATCH_ERROR);
    }

    @Test
    void testGenerateArtefactSummaryCivilAndFamilyDailyCauseList() throws Exception {
        byte[] data = getTestData("data/civil-and-family-cause-list/civilAndFamilyDailyCauseList.json");
        Artefact artefact = createPublication(ListType.CIVIL_AND_FAMILY_DAILY_CAUSE_LIST, data);

        when(blobClient.downloadContent()).thenReturn(BinaryData.fromBytes(data));

        MvcResult response = mockMvc.perform(
                get(String.format(GET_ARTEFACT_SUMMARY, artefact.getArtefactId())))
            .andExpect(status().isOk()).andReturn();
        String responseContent = response.getResponse().getContentAsString();

        assertTrue(responseContent.contains("Applicant - Surname"), CONTENT_MISMATCH_ERROR);
        assertTrue(responseContent.contains(CASE_REFERENCE_FIELD), CONTENT_MISMATCH_ERROR);
        assertTrue(responseContent.contains("Case name - This is a case name"), CONTENT_MISMATCH_ERROR);
        assertTrue(responseContent.contains("Case type - Case type"), CONTENT_MISMATCH_ERROR);
        assertTrue(responseContent.contains(HEARING_TYPE_FIELD), CONTENT_MISMATCH_ERROR);
    }

    @Test
    void testGenerateArtefactSummaryCivilDailyCauseList() throws Exception {
        byte[] data = getTestData("data/civil-daily-cause-list/civilDailyCauseList.json");
        Artefact artefact = createPublication(ListType.CIVIL_DAILY_CAUSE_LIST, data);

        when(blobClient.downloadContent()).thenReturn(BinaryData.fromBytes(data));

        MvcResult response = mockMvc.perform(
                get(String.format(GET_ARTEFACT_SUMMARY, artefact.getArtefactId())))
            .andExpect(status().isOk()).andReturn();

        String responseContent = response.getResponse().getContentAsString();
        assertTrue(responseContent.contains("Case reference - 45684548"), CONTENT_MISMATCH_ERROR);
        assertTrue(responseContent.contains("Case name - This is a case name"), CONTENT_MISMATCH_ERROR);
        assertTrue(responseContent.contains("Case type - Case Type"), CONTENT_MISMATCH_ERROR);
        assertTrue(responseContent.contains("Hearing type - Hearing Type"), CONTENT_MISMATCH_ERROR);
    }

    @Test
    void testGenerateArtefactSummaryCourtOfProtectionDailyCauseList() throws Exception {
        byte[] data = getTestData("data/cop-daily-cause-list/copDailyCauseList.json");
        Artefact artefact = createPublication(ListType.COP_DAILY_CAUSE_LIST, data);

        when(blobClient.downloadContent()).thenReturn(BinaryData.fromBytes(data));

        MvcResult response = mockMvc.perform(
                get(String.format(GET_ARTEFACT_SUMMARY, artefact.getArtefactId())))
            .andExpect(status().isOk()).andReturn();

        String responseContent = response.getResponse().getContentAsString();
        assertTrue(responseContent.contains(CASE_REFERENCE_FIELD), CONTENT_MISMATCH_ERROR);
        assertTrue(responseContent.contains("Case details - ThisIsACaseSuppressionName"), CONTENT_MISMATCH_ERROR);
        assertTrue(responseContent.contains("Hearing type - Criminal"), CONTENT_MISMATCH_ERROR);
    }

    @Test
    void testGenerateArtefactSummaryCrownDailyList() throws Exception {
        byte[] data = getTestData("data/crown-daily-list/crownDailyList.json");
        Artefact artefact = createPublication(ListType.CROWN_DAILY_LIST, data);

        when(blobClient.downloadContent()).thenReturn(BinaryData.fromBytes(data));

        MvcResult response = mockMvc.perform(get(String.format(GET_ARTEFACT_SUMMARY, artefact.getArtefactId())))
            .andExpect(status().isOk()).andReturn();

        String responseContent = response.getResponse().getContentAsString();
        assertTrue(responseContent.contains("Defendant - Surname 1, Forename 1"), CONTENT_MISMATCH_ERROR);
        assertTrue(responseContent.contains("Prosecutor - Pro_Auth"), CONTENT_MISMATCH_ERROR);
        assertTrue(responseContent.contains("Case reference - 1234"), CONTENT_MISMATCH_ERROR);
        assertTrue(
            responseContent.contains(HEARING_TYPE_FIELD),
            CONTENT_MISMATCH_ERROR
        );
    }

    @Test
    void testGenerateArtefactSummaryCrownFirmList() throws Exception {
        byte[] data = getTestData("data/crown-firm-list/crownFirmList.json");
        Artefact artefact = createPublication(ListType.CROWN_FIRM_LIST, data);

        when(blobClient.downloadContent()).thenReturn(BinaryData.fromBytes(data));

        MvcResult response = mockMvc.perform(get(String.format(GET_ARTEFACT_SUMMARY, artefact.getArtefactId())))
            .andExpect(status().isOk()).andReturn();

        String responseContent = response.getResponse().getContentAsString();
        assertTrue(responseContent.contains("Defendant - Surname 1, Forename 1"), CONTENT_MISMATCH_ERROR);
        assertTrue(responseContent.contains("Prosecutor - Prosecutor org"), CONTENT_MISMATCH_ERROR);
        assertTrue(responseContent.contains("Case reference - 1234"), CONTENT_MISMATCH_ERROR);
        assertTrue(responseContent.contains(HEARING_TYPE_FIELD), CONTENT_MISMATCH_ERROR);
    }

    @Test
    void testGenerateArtefactSummaryCrownWarnedList() throws Exception {
        byte[] data = getTestData("data/crown-warned-list/crownWarnedList.json");
        Artefact artefact = createPublication(ListType.CROWN_WARNED_LIST, data);

        when(blobClient.downloadContent()).thenReturn(BinaryData.fromBytes(data));

        MvcResult response = mockMvc.perform(get(String.format(GET_ARTEFACT_SUMMARY, artefact.getArtefactId())))
            .andExpect(status().isOk()).andReturn();

        String responseContent = response.getResponse().getContentAsString();
        assertTrue(responseContent.contains("Defendant - Surname, Forenames"), CONTENT_MISMATCH_ERROR);
        assertTrue(responseContent.contains("Prosecutor - OrganisationName2"), CONTENT_MISMATCH_ERROR);
        assertTrue(responseContent.contains(CASE_REFERENCE_FIELD), CONTENT_MISMATCH_ERROR);
        assertTrue(responseContent.contains("Hearing date - 21/07/2024"), CONTENT_MISMATCH_ERROR);
    }

    @Test
    void testGenerateArtefactSummaryEmploymentTribunalsDailyList() throws Exception {
        byte[] data = getTestData("data/et-daily-list/etDailyList.json");
        Artefact artefact = createPublication(ListType.ET_DAILY_LIST, data);

        when(blobClient.downloadContent()).thenReturn(BinaryData.fromBytes(data));

        MvcResult response = mockMvc.perform(get(String.format(GET_ARTEFACT_SUMMARY, artefact.getArtefactId())))
            .andExpect(status().isOk()).andReturn();

        String responseContent = response.getResponse().getContentAsString();
        assertTrue(responseContent.contains("Claimant - Claimant surname"), CONTENT_MISMATCH_ERROR);
        assertTrue(responseContent.contains("Respondent - Capt. T Test Surname"), CONTENT_MISMATCH_ERROR);
        assertTrue(responseContent.contains(CASE_REFERENCE_FIELD), CONTENT_MISMATCH_ERROR);
        assertTrue(responseContent.contains("Hearing type - This is a hearing type"), CONTENT_MISMATCH_ERROR);
    }

    @Test
    void testGenerateArtefactSummaryEmploymentTribunalsFortnightlyPressList() throws Exception {
        byte[] data = getTestData("data/et-fortnightly-press-list/etFortnightlyPressList.json");
        Artefact artefact = createPublication(ListType.ET_FORTNIGHTLY_PRESS_LIST, data);

        when(blobClient.downloadContent()).thenReturn(BinaryData.fromBytes(data));

        MvcResult response = mockMvc.perform(get(String.format(GET_ARTEFACT_SUMMARY, artefact.getArtefactId())))
            .andExpect(status().isOk()).andReturn();

        String responseContent = response.getResponse().getContentAsString();
        assertTrue(responseContent.contains("Claimant - Ms T Test"), CONTENT_MISMATCH_ERROR);
        assertTrue(responseContent.contains("Respondent - Lord T Test Surname"), CONTENT_MISMATCH_ERROR);
        assertTrue(responseContent.contains(CASE_REFERENCE_FIELD), CONTENT_MISMATCH_ERROR);
        assertTrue(responseContent.contains("Hearing type - Hearing Type 1"), CONTENT_MISMATCH_ERROR);
    }

    @Test
    void testGenerateArtefactSummaryFamilyDailyCauseList() throws Exception {
        byte[] data = getTestData("data/family-daily-cause-list/familyDailyCauseList.json");
        Artefact artefact = createPublication(ListType.FAMILY_DAILY_CAUSE_LIST, data);

        when(blobClient.downloadContent()).thenReturn(BinaryData.fromBytes(data));

        MvcResult response = mockMvc.perform(get(String.format(GET_ARTEFACT_SUMMARY, artefact.getArtefactId())))
            .andExpect(status().isOk()).andReturn();

        String responseContent = response.getResponse().getContentAsString();
        assertTrue(responseContent.contains("Applicant - Applicant surname"), CONTENT_MISMATCH_ERROR);
        assertTrue(responseContent.contains(CASE_REFERENCE_FIELD), CONTENT_MISMATCH_ERROR);
        assertTrue(responseContent.contains("Case name - This is a case name"), CONTENT_MISMATCH_ERROR);
        assertTrue(responseContent.contains("Case type - Case type"), CONTENT_MISMATCH_ERROR);
        assertTrue(responseContent.contains(HEARING_TYPE_FIELD), CONTENT_MISMATCH_ERROR);
    }

    @ParameterizedTest
    @EnumSource(value = ListType.class, names = {"IAC_DAILY_LIST", "IAC_DAILY_LIST_ADDITIONAL_CASES"})
    void testGenerateArtefactSummaryImmigrationAndAsylumChamberDailyList(ListType listType) throws Exception {
        byte[] data = getTestData("data/iac-daily-list/iacDailyList.json");
        Artefact artefact = createPublication(listType, data);

        when(blobClient.downloadContent()).thenReturn(BinaryData.fromBytes(data));

        MvcResult response = mockMvc.perform(get(String.format(GET_ARTEFACT_SUMMARY, artefact.getArtefactId())))
            .andExpect(status().isOk()).andReturn();

        String responseContent = response.getResponse().getContentAsString();
        assertTrue(responseContent.contains("Bail List"), CONTENT_MISMATCH_ERROR);
        assertTrue(
            responseContent.contains("Appellant - Surname"),
            CONTENT_MISMATCH_ERROR
        );
        assertTrue(responseContent.contains("Prosecuting authority - Authority surname"), CONTENT_MISMATCH_ERROR);
        assertTrue(responseContent.contains(CASE_REFERENCE_FIELD), CONTENT_MISMATCH_ERROR);
    }

    @Test
    void testGenerateArtefactSummaryMagistratesPublicList() throws Exception {
        byte[] data = getTestData("data/magistrates-public-list/magistratesPublicList.json");
        Artefact artefact = createPublication(ListType.MAGISTRATES_PUBLIC_LIST, data);

        when(blobClient.downloadContent()).thenReturn(BinaryData.fromBytes(data));

        MvcResult response = mockMvc.perform(get(String.format(GET_ARTEFACT_SUMMARY, artefact.getArtefactId())))
            .andExpect(status().isOk()).andReturn();

        String responseContent = response.getResponse().getContentAsString();
        assertTrue(responseContent.contains("Defendant - Surname, Forename"), CONTENT_MISMATCH_ERROR);
        assertTrue(responseContent.contains("Prosecutor - Authority org name"), CONTENT_MISMATCH_ERROR);
        assertTrue(responseContent.contains(CASE_REFERENCE_FIELD), CONTENT_MISMATCH_ERROR);
        assertTrue(
            responseContent.contains(HEARING_TYPE_FIELD),
            CONTENT_MISMATCH_ERROR
        );
    }

    @Test
    void testGenerateArtefactSummaryMagistratesStandardList() throws Exception {
        byte[] data = getTestData("data/magistrates-standard-list/magistratesStandardList.json");
        Artefact artefact = createPublication(ListType.MAGISTRATES_STANDARD_LIST, data);

        when(blobClient.downloadContent()).thenReturn(BinaryData.fromBytes(data));

        MvcResult response = mockMvc.perform(get(String.format(GET_ARTEFACT_SUMMARY, artefact.getArtefactId())))
            .andExpect(status().isOk()).andReturn();
        String responseContent = response.getResponse().getContentAsString();
        assertTrue(responseContent.contains("Defendant - Surname1, Forename1"), CONTENT_MISMATCH_ERROR);
        assertTrue(responseContent.contains("Prosecutor - Test1234"), CONTENT_MISMATCH_ERROR);
        assertTrue(responseContent.contains("Case reference - 45684548"), CONTENT_MISMATCH_ERROR);
        assertTrue(responseContent.contains("Hearing type - mda"), CONTENT_MISMATCH_ERROR);
        assertTrue(responseContent.contains("Offence - drink driving, Assault by beating"), CONTENT_MISMATCH_ERROR);
    }

    @Test
    void testGenerateArtefactSummaryPrimaryHealthTribunalHearingList() throws Exception {
        byte[] data = getTestData("data/primary-health-list/primaryHealthList.json");
        Artefact artefact = createPublication(ListType.PRIMARY_HEALTH_LIST, data);

        when(blobClient.downloadContent()).thenReturn(BinaryData.fromBytes(data));

        MvcResult response = mockMvc.perform(get(String.format(GET_ARTEFACT_SUMMARY, artefact.getArtefactId())))
            .andExpect(status().isOk()).andReturn();

        String responseContent = response.getResponse().getContentAsString();
        assertTrue(responseContent.contains("Case name - A Vs B"), CONTENT_MISMATCH_ERROR);
        assertTrue(responseContent.contains("Hearing date - 04 October"), CONTENT_MISMATCH_ERROR);
        assertTrue(responseContent.contains("Hearing type - Remote - Teams"), CONTENT_MISMATCH_ERROR);
    }

    @ParameterizedTest
    @EnumSource(value = ListType.class, names = {"SJP_PRESS_LIST", "SJP_DELTA_PRESS_LIST"})
    void testGenerateArtefactSummarySingleJusticeProcedurePressList(ListType listType) throws Exception {
        byte[] data = getTestData("data/sjp-press-list/sjpPressList.json");
        Artefact artefact = createPublication(listType, data);

        when(blobClient.downloadContent()).thenReturn(BinaryData.fromBytes(data));

        MvcResult response = mockMvc.perform(get(String.format(GET_ARTEFACT_SUMMARY, artefact.getArtefactId())))
            .andExpect(status().isOk()).andReturn();

        String responseContent = response.getResponse().getContentAsString();
        assertTrue(
            responseContent.contains(
                "Name - This is a title This is a forename This is a middle name This is a surname"),
            CONTENT_MISMATCH_ERROR
        );
        assertTrue(responseContent.contains("Prosecutor - This is an organisation"), CONTENT_MISMATCH_ERROR);
        assertTrue(responseContent.contains("Postcode - AA1 AA1"), CONTENT_MISMATCH_ERROR);
        assertTrue(responseContent.contains("Case reference - ABC12345"), CONTENT_MISMATCH_ERROR);
        assertTrue(responseContent.contains("Offence - This is an offence title"), CONTENT_MISMATCH_ERROR);
    }

    @ParameterizedTest
    @EnumSource(value = ListType.class, names = {"SJP_PUBLIC_LIST", "SJP_DELTA_PUBLIC_LIST"})
    void testGenerateArtefactSummarySingleJusticeProcedurePublicList(ListType listType) throws Exception {
        byte[] data = getTestData("data/sjp-public-list/sjpPublicList.json");
        Artefact artefact = createPublication(listType, data);

        when(blobClient.downloadContent()).thenReturn(BinaryData.fromBytes(data));

        MvcResult response = mockMvc.perform(get(String.format(GET_ARTEFACT_SUMMARY, artefact.getArtefactId())))
            .andExpect(status().isOk()).andReturn();

        String responseContent = response.getResponse().getContentAsString();
        assertTrue(responseContent.contains("Name - A This is a surname"), CONTENT_MISMATCH_ERROR);
        assertTrue(responseContent.contains("Prosecutor - This is an organisation"), CONTENT_MISMATCH_ERROR);
        assertTrue(responseContent.contains("Postcode - AA"), CONTENT_MISMATCH_ERROR);
        assertTrue(responseContent.contains("Offence - This is an offence title, This is an offence title 2"),
                   CONTENT_MISMATCH_ERROR);
    }

    @ParameterizedTest
    @EnumSource(value = ListType.class, names = {"SSCS_DAILY_LIST", "SSCS_DAILY_LIST_ADDITIONAL_HEARINGS"})
    void testGenerateArtefactSummarySscsDailyList(ListType listType) throws Exception {
        byte[] data = getTestData("data/sscs-daily-list/sscsDailyList.json");
        Artefact artefact = createPublication(listType, data);

        when(blobClient.downloadContent()).thenReturn(BinaryData.fromBytes(data));

        MvcResult response = mockMvc.perform(get(String.format(GET_ARTEFACT_SUMMARY, artefact.getArtefactId())))
            .andExpect(status().isOk()).andReturn();

        String responseContent = response.getResponse().getContentAsString();
        assertTrue(responseContent.contains("Appellant - Surname"), CONTENT_MISMATCH_ERROR);
        assertTrue(responseContent.contains("Respondent - Respondent Organisation, Respondent Organisation 2"),
                   CONTENT_MISMATCH_ERROR);
        assertTrue(responseContent.contains("Appeal reference - 12341235"), CONTENT_MISMATCH_ERROR);
    }

    @Test
    void testGenerateArtefactSummaryOpaPressList() throws Exception {
        byte[] data = getTestData("data/opa-press-list/opaPressList.json");
        Artefact artefact = createPublication(ListType.OPA_PRESS_LIST, data);

        when(blobClient.downloadContent()).thenReturn(BinaryData.fromBytes(data));

        MvcResult response = mockMvc.perform(get(String.format(GET_ARTEFACT_SUMMARY, artefact.getArtefactId())))
            .andExpect(status().isOk()).andReturn();

        String responseContent = response.getResponse().getContentAsString();
        assertTrue(responseContent.contains("Defendant - Surname, Forename MiddleName"),
                   CONTENT_MISMATCH_ERROR);
        assertTrue(responseContent.contains("Prosecuting authority ref"), CONTENT_MISMATCH_ERROR);
        assertTrue(responseContent.contains("Postcode - BB1 1BB"), CONTENT_MISMATCH_ERROR);
        assertTrue(responseContent.contains("Case reference - URN8888"), CONTENT_MISMATCH_ERROR);
        assertTrue(responseContent.contains("Offence - Offence title 2"), CONTENT_MISMATCH_ERROR);
    }

    @Test
    void testGenerateArtefactSummaryOpaPublicList() throws Exception {
        byte[] data = getTestData("data/opa-public-list/opaPublicList.json");
        Artefact artefact = createPublication(ListType.OPA_PUBLIC_LIST, data);

        when(blobClient.downloadContent()).thenReturn(BinaryData.fromBytes(data));

        MvcResult response = mockMvc.perform(get(String.format(GET_ARTEFACT_SUMMARY, artefact.getArtefactId())))
            .andExpect(status().isOk()).andReturn();

        String responseContent = response.getResponse().getContentAsString();
        assertTrue(
            responseContent.contains("Defendant - individualFirstName individualMiddleName IndividualSurname"),
            CONTENT_MISMATCH_ERROR
        );
        assertTrue(responseContent.contains("Prosecutor - Prosecution Authority ref 1"), CONTENT_MISMATCH_ERROR);
        assertTrue(responseContent.contains("Case reference - URN1234"), CONTENT_MISMATCH_ERROR);
        assertTrue(responseContent.contains("Offence - Offence title"), CONTENT_MISMATCH_ERROR);
    }

    @Test
    void testGenerateArtefactSummaryOpaResults() throws Exception {
        byte[] data = getTestData("data/opa-results/opaResults.json");
        Artefact artefact = createPublication(ListType.OPA_RESULTS, data);

        when(blobClient.downloadContent()).thenReturn(BinaryData.fromBytes(data));

        MvcResult response = mockMvc.perform(get(String.format(GET_ARTEFACT_SUMMARY, artefact.getArtefactId())))
            .andExpect(status().isOk()).andReturn();

        String responseContent = response.getResponse().getContentAsString();
        assertTrue(responseContent.contains("Defendant - Organisation name"), CONTENT_MISMATCH_ERROR);
        assertTrue(responseContent.contains("Case reference - URN5678"), CONTENT_MISMATCH_ERROR);
        assertTrue(responseContent.contains("Offence - Offence title 2"), CONTENT_MISMATCH_ERROR);
    }

    @Test
    void testGenerateArtefactSummaryNotFound() throws Exception {
        MvcResult mvcResult = mockMvc.perform(get(String.format(GET_ARTEFACT_SUMMARY, ARTEFACT_ID_NOT_FOUND)))
            .andExpect(status().isNotFound()).andReturn();

        ExceptionResponse exceptionResponse = OBJECT_MAPPER.readValue(
            mvcResult.getResponse().getContentAsString(), ExceptionResponse.class);

        assertEquals(
            NOT_FOUND_RESPONSE_MESSAGE,
            ARTEFACT_NOT_FOUND_MESSAGE + ARTEFACT_ID_NOT_FOUND,
            exceptionResponse.getMessage()
        );
    }

    @Test
    @WithMockUser(username = UNAUTHORIZED_USERNAME, authorities = {UNAUTHORIZED_ROLE})
    void testGenerateArtefactSummaryUnauthorized() throws Exception {
        mockMvc.perform(get(String.format(GET_ARTEFACT_SUMMARY, ARTEFACT_ID)))
            .andExpect(status().isForbidden());
    }

    @Test
    void testGetFileExists() throws Exception {
        when(blobClient.downloadContent()).thenReturn(
            BinaryData.fromString(new String(file.getBytes())));

        MvcResult response = mockMvc.perform(
                get(ROOT_URL + "/" + ARTEFACT_ID + "/exists"))
            .andExpect(status().isOk()).andReturn();

        assertNotNull(
            response.getResponse().getContentAsString(),
            "Response should not be null"
        );
    }

    @Test
    void testGetFileSizes() throws Exception {
        when(blobClient.downloadContent()).thenReturn(
            BinaryData.fromString(new String(file.getBytes())));

        MvcResult response = mockMvc.perform(
                get(ROOT_URL + "/" + ARTEFACT_ID + "/sizes"))
            .andExpect(status().isOk()).andReturn();

        assertNotNull(
            response.getResponse().getContentAsString(),
            "Response should not be null"
        );
    }

    @Test
    void testGetFileOK() throws Exception {
        UUID artefactId = createSjpPublicListPublication().getArtefactId();

        when(blobClient.downloadContent()).thenReturn(
            BinaryData.fromString(new String(file.getBytes())));

        MvcResult response = mockMvc.perform(
                get(String.format(GET_FILE_URL, artefactId, PDF))
                    .header(SYSTEM_HEADER, "true")
                    .header(FILE_TYPE_HEADER, PDF)
                    .param("maxFileSize", "2048000"))

            .andExpect(status().isOk()).andReturn();

        assertNotNull(
            response.getResponse().getContentAsString(),
            "Response should not be null"
        );
        byte[] decodedBytes = Base64.getDecoder().decode(response.getResponse().getContentAsString());
        String decodedResponse = new String(decodedBytes);

        assertTrue(
            decodedResponse.contains("test content"),
            "Response does not contain expected result"
        );
    }

    @Test
    void testGetFileForUserId() throws Exception {
        UUID artefactId = createSjpPublicListPublication().getArtefactId();

        when(blobClient.downloadContent()).thenReturn(
            BinaryData.fromString(new String(file.getBytes())));

        MockHttpServletRequestBuilder request =
            get(String.format(GET_FILE_URL, artefactId, PDF))
                .header("x-user-id", verifiedUserId)
                .header(SYSTEM_HEADER, "false")
                .param("maxFileSize", "2048000");

        MvcResult response = mockMvc.perform(request)
            .andExpect(status().isOk()).andReturn();

        assertNotNull(
            response.getResponse().getContentAsString(),
            "Null response"
        );
        byte[] decodedBytes = Base64.getDecoder().decode(response.getResponse().getContentAsString());
        String decodedResponse = new String(decodedBytes);

        assertTrue(
            decodedResponse.contains("test content"),
            "Response does not contain expected result"
        );
    }

    @Test
    void testGetFileSizeTooLarge() throws Exception {
        UUID artefactId = createSjpPublicListPublication().getArtefactId();

        when(blobClient.downloadContent()).thenReturn(
            BinaryData.fromString(new String(file.getBytes())));

        MockHttpServletRequestBuilder request =
            get(String.format(GET_FILE_URL, artefactId, PDF))
                .header("x-user-id", verifiedUserId)
                .header(SYSTEM_HEADER, "false")
                .param("maxFileSize", "10");

        MvcResult response = mockMvc.perform(request)
            .andExpect(status().isPayloadTooLarge()).andReturn();

        assertNotNull(
            response.getResponse().getContentAsString(),
            "Null response"
        );

        assertTrue(
            response.getResponse().getContentAsString().contains("File with type PDF for artefact with id "
                                                         + artefactId + " has size over the limit of 10 bytes"),
            "Response does not contain expected result"
        );
    }

    @Test
    void testGetFileNotFound() throws Exception {
        MvcResult mvcResult = mockMvc.perform(get(String.format(GET_FILE_URL, ARTEFACT_ID_NOT_FOUND, PDF)))
            .andExpect(status().isNotFound())
            .andReturn();

        ExceptionResponse exceptionResponse = OBJECT_MAPPER.readValue(
            mvcResult.getResponse().getContentAsString(), ExceptionResponse.class);

        assertEquals(
            NOT_FOUND_RESPONSE_MESSAGE,
            ARTEFACT_NOT_FOUND_MESSAGE + ARTEFACT_ID_NOT_FOUND,
            exceptionResponse.getMessage()
        );
    }

    @Test
    @WithMockUser(username = UNAUTHORIZED_USERNAME, authorities = {UNAUTHORIZED_ROLE})
    void testGetFileUnauthorized() throws Exception {
        mockMvc.perform(get(String.format(GET_FILE_URL, ARTEFACT_ID, PDF)))
            .andExpect(status().isForbidden());
    }
}
