package uk.gov.hmcts.reform.pip.data.management.controllers;

import com.azure.core.util.BinaryData;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import io.zonky.test.db.AutoConfigureEmbeddedDatabase;
import org.apache.commons.math3.random.RandomDataGenerator;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
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
import uk.gov.hmcts.reform.pip.data.management.config.PublicationConfiguration;
import uk.gov.hmcts.reform.pip.data.management.errorhandling.ExceptionResponse;
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
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.UUID;

import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.pip.model.publication.FileType.PDF;

@SpringBootTest(classes = {Application.class}, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles({"integration", "disable-async"})
@AutoConfigureMockMvc
@AutoConfigureEmbeddedDatabase(type = AutoConfigureEmbeddedDatabase.DatabaseType.POSTGRES)
@WithMockUser(username = "admin", authorities = {"APPROLE_api.request.admin"})
@SuppressWarnings({"PMD.TooManyMethods", "PMD.ExcessiveImports"})
class PublicationManagementTest extends IntegrationTestBase {
    private static final String ROOT_URL = "/publication";
    private static final String GET_ARTEFACT_SUMMARY = ROOT_URL + "/%s/summary";
    private static final String GET_FILE_URL = ROOT_URL + "/%s/%s";
    private static final String ARTEFACT_ID = UUID.randomUUID().toString();
    private static final String ARTEFACT_ID_NOT_FOUND = UUID.randomUUID().toString();
    private static final String ARTEFACT_NOT_FOUND_MESSAGE = "No artefact found with the ID: ";
    private static final String NOT_FOUND_RESPONSE_MESSAGE = "Artefact not found message does not match";
    private static final String CONTENT_MISMATCH_ERROR = "Artefact summary content should match";
    private static final String FILE_TYPE_HEADER = "x-file-type";
    private static final String MAX_FILE_SIZE_HEADER = "maxFileSize";
    private static final String UNAUTHORIZED_USERNAME = "unauthorized_username";
    private static final String UNAUTHORIZED_ROLE = "APPROLE_unknown.role";
    private static final String SYSTEM_HEADER = "x-system";
    private static final String REQUESTER_HEADER = "x-requester-id";
    private static final String CASE_REFERENCE_FIELD = "Case reference - 12341234";
    private static final String CASE_NAME_FIELD = "Case name - This is a case name";
    private static final String HEARING_TYPE_FIELD = "Hearing type - Directions";
    private static final String DATE_FIELD = "Date - 16 December 2024";
    private static final String HEARING_TIME_FIELD = "Hearing time - 10am";
    private static final String CASE_REFERENCE_NUMBER_FIELD = "Case reference number - 1234";
    private static final String TIME_FIELD = "Time - 10am";
    private static final String APPELLANT_NUMBER_FIELD = "Appellant - Appellant 1";
    private static final String NON_STRATEGIC_FILES_LOCATION = "data/non-strategic/";
    private static final String RPT_LISTS_EXCEL_FILE = NON_STRATEGIC_FILES_LOCATION
        + "ftt-residential-property-tribunal-weekly-hearing-list/"
        + "fttResidentialPropertyTribunalWeeklyHearingList.xlsx";
    private static final String RPT_LISTS_JSON_FILE = NON_STRATEGIC_FILES_LOCATION
        + "ftt-residential-property-tribunal-weekly-hearing-list/"
        + "fttResidentialPropertyTribunalWeeklyHearingList.json";
    private static final String SIAC_LISTS_EXCEL_FILE = NON_STRATEGIC_FILES_LOCATION
        + "siac-weekly-hearing-list/"
        + "siacWeeklyHearingList.xlsx";
    private static final String SIAC_LISTS_JSON_FILE = NON_STRATEGIC_FILES_LOCATION
        + "siac-weekly-hearing-list/"
        + "siacWeeklyHearingList.json";
    private static final String SSCS_LISTS_EXCEL_FILE = NON_STRATEGIC_FILES_LOCATION
        + "sscs-daily-hearing-list/"
        + "sscsDailyHearingList.xlsx";
    private static final String SSCS_LISTS_JSON_FILE = NON_STRATEGIC_FILES_LOCATION
        + "sscs-daily-hearing-list/"
        + "sscsDailyHearingList.json";

    private static final LocalDateTime DISPLAY_TO = LocalDateTime.now()
        .truncatedTo(ChronoUnit.SECONDS);
    private static final LocalDateTime DISPLAY_FROM = LocalDateTime.now().plusDays(1)
        .truncatedTo(ChronoUnit.SECONDS);
    private static final LocalDateTime CONTENT_DATE = LocalDateTime.now().toLocalDate().atStartOfDay()
        .truncatedTo(ChronoUnit.SECONDS);
    private static final String PROVENANCE = "MANUAL_UPLOAD";
    private static final String USER_ID = UUID.randomUUID().toString();
    private static final String SYSTEM_ADMIN_ID = UUID.randomUUID().toString();

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private static final String SJP_MOCK = "data/sjp-public-list/sjpPublicList.json";

    private static MockMultipartFile file;
    private static final String EXCEL_FILE_TYPE = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";

    private static PiUser piUser;

    @Autowired
    private MockMvc mockMvc;

    @BeforeAll
    static void setup() {
        file = new MockMultipartFile("file", "test.pdf",
                                     MediaType.APPLICATION_PDF_VALUE, "test content".getBytes(
            StandardCharsets.UTF_8)
        );

        piUser = new PiUser();
        piUser.setUserId(UUID.randomUUID().toString());
        piUser.setEmail("test@justice.gov.uk");
        piUser.setRoles(Roles.SYSTEM_ADMIN);

        OBJECT_MAPPER.findAndRegisterModules();
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
        return createPublication(listType, Sensitivity.PUBLIC, data);
    }

    private Artefact createPublication(ListType listType, Sensitivity sensitivity, byte[] data) throws Exception {
        when(accountManagementService.getUserById(any())).thenReturn(piUser);
        MockHttpServletRequestBuilder mockHttpServletRequestBuilder = MockMvcRequestBuilders
            .post("/publication")
            .header(PublicationConfiguration.TYPE_HEADER, ArtefactType.LIST)
            .header(PublicationConfiguration.PROVENANCE_HEADER, PROVENANCE)
            .header(PublicationConfiguration.DISPLAY_FROM_HEADER, DISPLAY_FROM)
            .header(PublicationConfiguration.DISPLAY_TO_HEADER, DISPLAY_TO)
            .header(PublicationConfiguration.COURT_ID, "1")
            .header(PublicationConfiguration.LIST_TYPE, listType)
            .header(PublicationConfiguration.CONTENT_DATE,
                    CONTENT_DATE.plusDays(new RandomDataGenerator().nextLong(1, 100_000)))
            .header(PublicationConfiguration.SENSITIVITY_HEADER, sensitivity)
            .header(PublicationConfiguration.LANGUAGE_HEADER, Language.ENGLISH)
            .header(PublicationConfiguration.REQUESTER_ID_HEADER, SYSTEM_ADMIN_ID)
            .content(data)
            .contentType(MediaType.APPLICATION_JSON);

        MvcResult response = mockMvc.perform(mockHttpServletRequestBuilder)
            .andExpect(status().isCreated()).andReturn();

        return OBJECT_MAPPER.readValue(
            response.getResponse().getContentAsString(), Artefact.class);
    }

    private Artefact createNonStrategicPublication(ListType listType, String filePath) throws Exception {
        when(accountManagementService.getUserById(any())).thenReturn(piUser);
        MockHttpServletRequestBuilder mockHttpServletRequestBuilder = multipart("/publication/non-strategic")
            .file(getMockMultipartFile(filePath))
            .header(PublicationConfiguration.TYPE_HEADER, ArtefactType.LIST)
            .header(PublicationConfiguration.PROVENANCE_HEADER, PROVENANCE)
            .header(PublicationConfiguration.DISPLAY_FROM_HEADER, DISPLAY_FROM)
            .header(PublicationConfiguration.DISPLAY_TO_HEADER, DISPLAY_TO)
            .header(PublicationConfiguration.COURT_ID, "1")
            .header(PublicationConfiguration.LIST_TYPE, listType)
            .header(PublicationConfiguration.CONTENT_DATE,
                    CONTENT_DATE.plusDays(new RandomDataGenerator().nextLong(1, 100_000)))
            .header(PublicationConfiguration.SENSITIVITY_HEADER, Sensitivity.PUBLIC)
            .header(PublicationConfiguration.LANGUAGE_HEADER, Language.ENGLISH)
            .header(PublicationConfiguration.REQUESTER_ID_HEADER, SYSTEM_ADMIN_ID)
            .contentType(MediaType.MULTIPART_FORM_DATA);

        MvcResult response = mockMvc.perform(mockHttpServletRequestBuilder)
            .andExpect(status().isCreated()).andReturn();

        return OBJECT_MAPPER.readValue(
            response.getResponse().getContentAsString(), Artefact.class);
    }

    private MockMultipartFile getMockMultipartFile(String filePath) throws IOException {
        try (InputStream inputStream = this.getClass().getClassLoader()
            .getResourceAsStream(filePath)) {
            return new MockMultipartFile(
                "file", "TestFileName.xlsx", EXCEL_FILE_TYPE,
                org.testcontainers.shaded.org.apache.commons.io.IOUtils.toByteArray(inputStream)
            );
        }
    }

    private void createLocations() throws Exception {
        try (InputStream csvInputStream = this.getClass().getClassLoader()
            .getResourceAsStream("location/ValidCsv.csv")) {
            MockMultipartFile csvFile
                = new MockMultipartFile("locationList", csvInputStream);

            mockMvc.perform(multipart("/locations/upload").file(csvFile)
                                .header(REQUESTER_HEADER, SYSTEM_ADMIN_ID))
                .andExpect(status().isOk()).andReturn();

        }
    }

    private Artefact createSjpPublicListPublication() throws Exception {
        byte[] testPublication = getTestData(SJP_MOCK);
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
        assertTrue(responseContent.contains(CASE_NAME_FIELD), CONTENT_MISMATCH_ERROR);
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
        assertTrue(responseContent.contains(CASE_NAME_FIELD), CONTENT_MISMATCH_ERROR);
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
        assertTrue(responseContent.contains("Prosecuting authority - Pro_Auth"), CONTENT_MISMATCH_ERROR);
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
        assertTrue(responseContent.contains("Prosecuting authority - Prosecutor org"), CONTENT_MISMATCH_ERROR);
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
        assertTrue(responseContent.contains("Prosecuting authority - OrganisationName2"), CONTENT_MISMATCH_ERROR);
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
        assertTrue(responseContent.contains(CASE_NAME_FIELD), CONTENT_MISMATCH_ERROR);
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
            responseContent.contains("Appellant/Applicant - Surname"),
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
        assertTrue(responseContent.contains("Prosecuting authority - Authority org name"), CONTENT_MISMATCH_ERROR);
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
        assertTrue(responseContent.contains("Prosecuting authority - Test1234"), CONTENT_MISMATCH_ERROR);
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
        assertEquals("", responseContent, CONTENT_MISMATCH_ERROR);
    }

    @ParameterizedTest
    @EnumSource(value = ListType.class, names = {"SJP_PUBLIC_LIST", "SJP_DELTA_PUBLIC_LIST"})
    void testGenerateArtefactSummarySingleJusticeProcedurePublicList(ListType listType) throws Exception {
        byte[] data = getTestData(SJP_MOCK);
        Artefact artefact = createPublication(listType, data);

        when(blobClient.downloadContent()).thenReturn(BinaryData.fromBytes(data));

        MvcResult response = mockMvc.perform(get(String.format(GET_ARTEFACT_SUMMARY, artefact.getArtefactId())))
            .andExpect(status().isOk()).andReturn();

        String responseContent = response.getResponse().getContentAsString();
        assertEquals("", responseContent, CONTENT_MISMATCH_ERROR);
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
        assertTrue(responseContent.contains("Case reference - 12341235"), CONTENT_MISMATCH_ERROR);
    }

    @Test
    void testGenerateArtefactSummaryCstWeeklyHearingList() throws Exception {
        Artefact artefact = createNonStrategicPublication(
            ListType.CST_WEEKLY_HEARING_LIST, NON_STRATEGIC_FILES_LOCATION
                + "cst-weekly-hearing-list/cstWeeklyHearingList.xlsx"
        );

        byte[] jsonData = getTestData(NON_STRATEGIC_FILES_LOCATION
                                          + "cst-weekly-hearing-list/cstWeeklyHearingList.json");
        when(blobClient.downloadContent()).thenReturn(BinaryData.fromBytes(jsonData));

        MvcResult response = mockMvc.perform(get(String.format(GET_ARTEFACT_SUMMARY, artefact.getArtefactId())))
            .andExpect(status().isOk()).andReturn();

        String responseContent = response.getResponse().getContentAsString();
        assertTrue(responseContent.contains("Date - 10 December 2024"), CONTENT_MISMATCH_ERROR);
        assertTrue(responseContent.contains(CASE_NAME_FIELD), CONTENT_MISMATCH_ERROR);
    }

    @Test
    void testGenerateArtefactSummaryPhtWeeklyHearingList() throws Exception {
        Artefact artefact = createNonStrategicPublication(
            ListType.PHT_WEEKLY_HEARING_LIST, NON_STRATEGIC_FILES_LOCATION
                + "pht-weekly-hearing-list/phtWeeklyHearingList.xlsx"
        );

        byte[] jsonData = getTestData(NON_STRATEGIC_FILES_LOCATION
                                          + "pht-weekly-hearing-list/phtWeeklyHearingList.json");
        when(blobClient.downloadContent()).thenReturn(BinaryData.fromBytes(jsonData));

        MvcResult response = mockMvc.perform(get(String.format(GET_ARTEFACT_SUMMARY, artefact.getArtefactId())))
            .andExpect(status().isOk()).andReturn();

        String responseContent = response.getResponse().getContentAsString();
        assertTrue(responseContent.contains("Date - 10 December 2024"), CONTENT_MISMATCH_ERROR);
        assertTrue(responseContent.contains(CASE_NAME_FIELD), CONTENT_MISMATCH_ERROR);
    }

    @Test
    void testGenerateArtefactSummaryGrcWeeklyHearingList() throws Exception {
        Artefact artefact = createNonStrategicPublication(
            ListType.GRC_WEEKLY_HEARING_LIST, NON_STRATEGIC_FILES_LOCATION
                + "grc-weekly-hearing-list/grcWeeklyHearingList.xlsx"
        );

        byte[] jsonData = getTestData(NON_STRATEGIC_FILES_LOCATION
                                          + "grc-weekly-hearing-list/grcWeeklyHearingList.json");
        when(blobClient.downloadContent()).thenReturn(BinaryData.fromBytes(jsonData));

        MvcResult response = mockMvc.perform(get(String.format(GET_ARTEFACT_SUMMARY, artefact.getArtefactId())))
            .andExpect(status().isOk()).andReturn();

        String responseContent = response.getResponse().getContentAsString();
        assertTrue(responseContent.contains(DATE_FIELD), CONTENT_MISMATCH_ERROR);
        assertTrue(responseContent.contains(HEARING_TIME_FIELD), CONTENT_MISMATCH_ERROR);
        assertTrue(responseContent.contains(CASE_REFERENCE_NUMBER_FIELD), CONTENT_MISMATCH_ERROR);
    }

    @Test
    void testGenerateArtefactSummaryWpafccWeeklyHearingList() throws Exception {
        Artefact artefact = createNonStrategicPublication(
            ListType.WPAFCC_WEEKLY_HEARING_LIST,
            NON_STRATEGIC_FILES_LOCATION + "wpafcc-weekly-hearing-list/wpafccWeeklyHearingList.xlsx"
        );

        byte[] jsonData = getTestData(
            NON_STRATEGIC_FILES_LOCATION + "wpafcc-weekly-hearing-list/wpafccWeeklyHearingList.json"
        );
        when(blobClient.downloadContent()).thenReturn(BinaryData.fromBytes(jsonData));

        MvcResult response = mockMvc.perform(get(String.format(GET_ARTEFACT_SUMMARY, artefact.getArtefactId())))
            .andExpect(status().isOk()).andReturn();

        String responseContent = response.getResponse().getContentAsString();
        assertTrue(responseContent.contains(DATE_FIELD), CONTENT_MISMATCH_ERROR);
        assertTrue(responseContent.contains(HEARING_TIME_FIELD), CONTENT_MISMATCH_ERROR);
        assertTrue(responseContent.contains(CASE_REFERENCE_NUMBER_FIELD), CONTENT_MISMATCH_ERROR);
    }

    @ParameterizedTest
    @EnumSource(
        value = ListType.class,
        names = {
            "UT_IAC_JR_LONDON_DAILY_HEARING_LIST",
            "UT_IAC_JR_MANCHESTER_DAILY_HEARING_LIST",
            "UT_IAC_JR_BIRMINGHAM_DAILY_HEARING_LIST",
            "UT_IAC_JR_CARDIFF_DAILY_HEARING_LIST"
        })
    void testGenerateArtefactSummaryUtIacJudicialReviewDailyHearingList(ListType listType) throws Exception {
        Artefact artefact = createNonStrategicPublication(
            listType,
            NON_STRATEGIC_FILES_LOCATION
                + "ut-iac-judicial-review-daily-hearing-list/utIacJudicialReviewDailyHearingList.xlsx"
        );

        byte[] jsonData = getTestData(
            NON_STRATEGIC_FILES_LOCATION
                 + "ut-iac-judicial-review-daily-hearing-list/utIacJudicialReviewDailyHearingList.json"
        );
        when(blobClient.downloadContent()).thenReturn(BinaryData.fromBytes(jsonData));

        MvcResult response = mockMvc.perform(get(String.format(GET_ARTEFACT_SUMMARY, artefact.getArtefactId())))
            .andExpect(status().isOk()).andReturn();

        String responseContent = response.getResponse().getContentAsString();
        assertTrue(responseContent.contains("Hearing time - 10:30am"), CONTENT_MISMATCH_ERROR);
        assertTrue(responseContent.contains(CASE_REFERENCE_NUMBER_FIELD), CONTENT_MISMATCH_ERROR);
    }

    @Test
    void testGenerateArtefactSummaryUtIacStatutoryAppealsDailyHearingList() throws Exception {
        Artefact artefact = createNonStrategicPublication(
            ListType.UT_IAC_STATUTORY_APPEALS_DAILY_HEARING_LIST,
            NON_STRATEGIC_FILES_LOCATION
                + "ut-iac-statutory-appeals-daily-hearing-list/utIacStatutoryAppealsDailyHearingList.xlsx"
        );

        byte[] jsonData = getTestData(
            NON_STRATEGIC_FILES_LOCATION
                + "ut-iac-statutory-appeals-daily-hearing-list/utIacStatutoryAppealsDailyHearingList.json"
        );
        when(blobClient.downloadContent()).thenReturn(BinaryData.fromBytes(jsonData));

        MvcResult response = mockMvc.perform(get(String.format(GET_ARTEFACT_SUMMARY, artefact.getArtefactId())))
            .andExpect(status().isOk()).andReturn();

        String responseContent = response.getResponse().getContentAsString();
        assertTrue(responseContent.contains("Hearing time - 10:30am"), CONTENT_MISMATCH_ERROR);
        assertTrue(responseContent.contains("Appeal reference number - 1234"), CONTENT_MISMATCH_ERROR);
    }

    @ParameterizedTest
    @EnumSource(
        value = ListType.class,
        names = {
            "SIAC_WEEKLY_HEARING_LIST",
            "POAC_WEEKLY_HEARING_LIST",
            "PAAC_WEEKLY_HEARING_LIST"
        })
    void testGenerateArtefactSummarySiacWeeklyHearingList(ListType listType) throws Exception {
        Artefact artefact = createNonStrategicPublication(
            listType, SIAC_LISTS_EXCEL_FILE
        );

        byte[] jsonData = getTestData(SIAC_LISTS_JSON_FILE);
        when(blobClient.downloadContent()).thenReturn(BinaryData.fromBytes(jsonData));

        MvcResult response = mockMvc.perform(get(String.format(GET_ARTEFACT_SUMMARY, artefact.getArtefactId())))
            .andExpect(status().isOk()).andReturn();

        String responseContent = response.getResponse().getContentAsString();
        assertTrue(responseContent.contains("Date - 11 December 2024"), CONTENT_MISMATCH_ERROR);
        assertTrue(responseContent.contains(TIME_FIELD), CONTENT_MISMATCH_ERROR);
        assertTrue(responseContent.contains("Case reference number - 123451"), CONTENT_MISMATCH_ERROR);
    }

    @ParameterizedTest
    @EnumSource(
        value = ListType.class,
        names = {
            "SSCS_MIDLANDS_DAILY_HEARING_LIST",
            "SSCS_SOUTH_EAST_DAILY_HEARING_LIST",
            "SSCS_WALES_AND_SOUTH_WEST_DAILY_HEARING_LIST",
            "SSCS_SCOTLAND_DAILY_HEARING_LIST",
            "SSCS_NORTH_EAST_DAILY_HEARING_LIST",
            "SSCS_NORTH_WEST_DAILY_HEARING_LIST",
            "SSCS_LONDON_DAILY_HEARING_LIST"
        })
    void testGenerateArtefactSummarySscsDailyHearingList(ListType listType) throws Exception {
        Artefact artefact = createNonStrategicPublication(
            listType, SSCS_LISTS_EXCEL_FILE
        );

        byte[] jsonData = getTestData(SSCS_LISTS_JSON_FILE);
        when(blobClient.downloadContent()).thenReturn(BinaryData.fromBytes(jsonData));

        MvcResult response = mockMvc.perform(get(String.format(GET_ARTEFACT_SUMMARY, artefact.getArtefactId())))
            .andExpect(status().isOk()).andReturn();

        String responseContent = response.getResponse().getContentAsString();
        assertTrue(responseContent.contains(HEARING_TIME_FIELD), CONTENT_MISMATCH_ERROR);
        assertTrue(responseContent.contains(HEARING_TYPE_FIELD), CONTENT_MISMATCH_ERROR);
        assertTrue(responseContent.contains("Appeal reference number - 1234567"), CONTENT_MISMATCH_ERROR);
    }

    @Test
    void testGenerateArtefactFttTaxSummaryWeeklyHearingList() throws Exception {
        Artefact artefact = createNonStrategicPublication(
            ListType.FTT_TAX_WEEKLY_HEARING_LIST,
            NON_STRATEGIC_FILES_LOCATION
                + "ftt-tax-tribunal-weekly-hearing-list/fttTaxWeeklyHearingList.xlsx"
        );

        byte[] jsonData = getTestData(
            NON_STRATEGIC_FILES_LOCATION
                + "ftt-tax-tribunal-weekly-hearing-list/fttTaxWeeklyHearingList.json");
        when(blobClient.downloadContent()).thenReturn(BinaryData.fromBytes(jsonData));

        MvcResult response = mockMvc.perform(get(String.format(GET_ARTEFACT_SUMMARY, artefact.getArtefactId())))
            .andExpect(status().isOk()).andReturn();

        String responseContent = response.getResponse().getContentAsString();
        assertTrue(responseContent.contains(DATE_FIELD), CONTENT_MISMATCH_ERROR);
        assertTrue(responseContent.contains(HEARING_TIME_FIELD), CONTENT_MISMATCH_ERROR);
        assertTrue(responseContent.contains(CASE_REFERENCE_NUMBER_FIELD), CONTENT_MISMATCH_ERROR);
    }

    @Test
    void testGenerateArtefactFttLandRegistrySummaryWeeklyHearingList() throws Exception {
        Artefact artefact = createNonStrategicPublication(
            ListType.FTT_LR_WEEKLY_HEARING_LIST,
            NON_STRATEGIC_FILES_LOCATION + "ftt-land-registry-tribunal-weekly-hearing-list/"
                 + "fttLandRegistryTribunalWeeklyHearingList.xlsx"
        );

        byte[] jsonData = getTestData(
            NON_STRATEGIC_FILES_LOCATION + "ftt-land-registry-tribunal-weekly-hearing-list/"
                 + "fttLandRegistryTribunalWeeklyHearingList.json");
        when(blobClient.downloadContent()).thenReturn(BinaryData.fromBytes(jsonData));

        MvcResult response = mockMvc.perform(get(String.format(GET_ARTEFACT_SUMMARY, artefact.getArtefactId())))
            .andExpect(status().isOk()).andReturn();

        String responseContent = response.getResponse().getContentAsString();
        assertTrue(responseContent.contains(DATE_FIELD), CONTENT_MISMATCH_ERROR);
        assertTrue(responseContent.contains(HEARING_TIME_FIELD), CONTENT_MISMATCH_ERROR);
        assertTrue(responseContent.contains(CASE_REFERENCE_NUMBER_FIELD), CONTENT_MISMATCH_ERROR);
    }

    @Test
    void testGenerateArtefactRptEasternLandRegistrySummaryWeeklyHearingList() throws Exception {
        Artefact artefact = createNonStrategicPublication(
            ListType.RPT_EASTERN_WEEKLY_HEARING_LIST, RPT_LISTS_EXCEL_FILE);

        byte[] jsonData = getTestData(RPT_LISTS_JSON_FILE);
        when(blobClient.downloadContent()).thenReturn(BinaryData.fromBytes(jsonData));

        MvcResult response = mockMvc.perform(get(String.format(GET_ARTEFACT_SUMMARY, artefact.getArtefactId())))
            .andExpect(status().isOk()).andReturn();

        String responseContent = response.getResponse().getContentAsString();
        assertTrue(responseContent.contains(DATE_FIELD), CONTENT_MISMATCH_ERROR);
        assertTrue(responseContent.contains(TIME_FIELD), CONTENT_MISMATCH_ERROR);
        assertTrue(responseContent.contains(CASE_REFERENCE_NUMBER_FIELD), CONTENT_MISMATCH_ERROR);
    }

    @Test
    void testGenerateArtefactRptLondonLandRegistrySummaryWeeklyHearingList() throws Exception {
        Artefact artefact = createNonStrategicPublication(
            ListType.RPT_LONDON_WEEKLY_HEARING_LIST, RPT_LISTS_EXCEL_FILE);

        byte[] jsonData = getTestData(RPT_LISTS_JSON_FILE);
        when(blobClient.downloadContent()).thenReturn(BinaryData.fromBytes(jsonData));

        MvcResult response = mockMvc.perform(get(String.format(GET_ARTEFACT_SUMMARY, artefact.getArtefactId())))
            .andExpect(status().isOk()).andReturn();

        String responseContent = response.getResponse().getContentAsString();
        assertTrue(responseContent.contains(DATE_FIELD), CONTENT_MISMATCH_ERROR);
        assertTrue(responseContent.contains(TIME_FIELD), CONTENT_MISMATCH_ERROR);
        assertTrue(responseContent.contains(CASE_REFERENCE_NUMBER_FIELD), CONTENT_MISMATCH_ERROR);
    }

    @Test
    void testGenerateArtefactRptMidlandsLandRegistrySummaryWeeklyHearingList() throws Exception {
        Artefact artefact = createNonStrategicPublication(
            ListType.RPT_MIDLANDS_WEEKLY_HEARING_LIST, RPT_LISTS_EXCEL_FILE);

        byte[] jsonData = getTestData(RPT_LISTS_JSON_FILE);
        when(blobClient.downloadContent()).thenReturn(BinaryData.fromBytes(jsonData));

        MvcResult response = mockMvc.perform(get(String.format(GET_ARTEFACT_SUMMARY, artefact.getArtefactId())))
            .andExpect(status().isOk()).andReturn();

        String responseContent = response.getResponse().getContentAsString();
        assertTrue(responseContent.contains(DATE_FIELD), CONTENT_MISMATCH_ERROR);
        assertTrue(responseContent.contains(TIME_FIELD), CONTENT_MISMATCH_ERROR);
        assertTrue(responseContent.contains(CASE_REFERENCE_NUMBER_FIELD), CONTENT_MISMATCH_ERROR);
    }

    @Test
    void testGenerateArtefactRptNorthernLandRegistrySummaryWeeklyHearingList() throws Exception {
        Artefact artefact = createNonStrategicPublication(
            ListType.RPT_NORTHERN_WEEKLY_HEARING_LIST, RPT_LISTS_EXCEL_FILE);

        byte[] jsonData = getTestData(RPT_LISTS_JSON_FILE);
        when(blobClient.downloadContent()).thenReturn(BinaryData.fromBytes(jsonData));

        MvcResult response = mockMvc.perform(get(String.format(GET_ARTEFACT_SUMMARY, artefact.getArtefactId())))
            .andExpect(status().isOk()).andReturn();

        String responseContent = response.getResponse().getContentAsString();
        assertTrue(responseContent.contains(DATE_FIELD), CONTENT_MISMATCH_ERROR);
        assertTrue(responseContent.contains(TIME_FIELD), CONTENT_MISMATCH_ERROR);
        assertTrue(responseContent.contains(CASE_REFERENCE_NUMBER_FIELD), CONTENT_MISMATCH_ERROR);
    }

    @Test
    void testGenerateArtefactRptSouthernLandRegistrySummaryWeeklyHearingList() throws Exception {
        Artefact artefact = createNonStrategicPublication(
            ListType.RPT_SOUTHERN_WEEKLY_HEARING_LIST, RPT_LISTS_EXCEL_FILE);

        byte[] jsonData = getTestData(RPT_LISTS_JSON_FILE);
        when(blobClient.downloadContent()).thenReturn(BinaryData.fromBytes(jsonData));

        MvcResult response = mockMvc.perform(get(String.format(GET_ARTEFACT_SUMMARY, artefact.getArtefactId())))
            .andExpect(status().isOk()).andReturn();

        String responseContent = response.getResponse().getContentAsString();
        assertTrue(responseContent.contains(DATE_FIELD), CONTENT_MISMATCH_ERROR);
        assertTrue(responseContent.contains(TIME_FIELD), CONTENT_MISMATCH_ERROR);
        assertTrue(responseContent.contains(CASE_REFERENCE_NUMBER_FIELD), CONTENT_MISMATCH_ERROR);
    }

    @Test
    void testGenerateArtefactUtAdministrativeAppealsSummaryDailyHearingList() throws Exception {
        Artefact artefact = createNonStrategicPublication(
            ListType.UT_AAC_DAILY_HEARING_LIST,
            "data/non-strategic/ut-administrative-appeals-chamber-daily-hearing-list/"
                + "utAdministrativeAppealsChamberDailyHearingList.xlsx"
        );

        byte[] jsonData = getTestData(
            "data/non-strategic/ut-administrative-appeals-chamber-daily-hearing-list/"
                + "utAdministrativeAppealsChamberDailyHearingList.json");
        when(blobClient.downloadContent()).thenReturn(BinaryData.fromBytes(jsonData));

        MvcResult response = mockMvc.perform(get(String.format(GET_ARTEFACT_SUMMARY, artefact.getArtefactId())))
            .andExpect(status().isOk()).andReturn();

        String responseContent = response.getResponse().getContentAsString();
        assertTrue(responseContent.contains(TIME_FIELD), CONTENT_MISMATCH_ERROR);
        assertTrue(responseContent.contains(CASE_REFERENCE_NUMBER_FIELD), CONTENT_MISMATCH_ERROR);
        assertTrue(responseContent.contains(APPELLANT_NUMBER_FIELD), CONTENT_MISMATCH_ERROR);
    }

    @Test
    void testGenerateArtefactUtLandsChamberSummaryDailyHearingList() throws Exception {
        Artefact artefact = createNonStrategicPublication(
            ListType.UT_LC_DAILY_HEARING_LIST,
            "data/non-strategic/ut-lands-chamber-daily-hearing-list/"
                + "utLandsChamberDailyHearingList.xlsx"
        );

        byte[] jsonData = getTestData(
            "data/non-strategic/ut-lands-chamber-daily-hearing-list/"
                + "utLandsChamberDailyHearingList.json");
        when(blobClient.downloadContent()).thenReturn(BinaryData.fromBytes(jsonData));

        MvcResult response = mockMvc.perform(get(String.format(GET_ARTEFACT_SUMMARY, artefact.getArtefactId())))
            .andExpect(status().isOk()).andReturn();

        String responseContent = response.getResponse().getContentAsString();
        assertTrue(responseContent.contains(TIME_FIELD), CONTENT_MISMATCH_ERROR);
        assertTrue(responseContent.contains(CASE_REFERENCE_NUMBER_FIELD), CONTENT_MISMATCH_ERROR);
        assertTrue(responseContent.contains(CASE_NAME_FIELD), CONTENT_MISMATCH_ERROR);
    }

    @Test
    void testGenerateArtefactUtTaxAndChanceryChamberSummaryDailyHearingList() throws Exception {
        Artefact artefact = createNonStrategicPublication(
            ListType.UT_T_AND_CC_DAILY_HEARING_LIST,
            "data/non-strategic/ut-tax-and-chancery-chamber-daily-hearing-list/"
                + "utTaxAndChanceryChamberDailyHearingList.xlsx"
        );

        byte[] jsonData = getTestData(
            "data/non-strategic/ut-tax-and-chancery-chamber-daily-hearing-list/"
                + "utTaxAndChanceryChamberDailyHearingList.json");
        when(blobClient.downloadContent()).thenReturn(BinaryData.fromBytes(jsonData));

        MvcResult response = mockMvc.perform(get(String.format(GET_ARTEFACT_SUMMARY, artefact.getArtefactId())))
            .andExpect(status().isOk()).andReturn();

        String responseContent = response.getResponse().getContentAsString();
        assertTrue(responseContent.contains(TIME_FIELD), CONTENT_MISMATCH_ERROR);
        assertTrue(responseContent.contains(CASE_REFERENCE_NUMBER_FIELD), CONTENT_MISMATCH_ERROR);
        assertTrue(responseContent.contains(CASE_NAME_FIELD), CONTENT_MISMATCH_ERROR);
    }

    @Test
    void testGenerateArtefactSummaryAstDailyHearingList() throws Exception {
        Artefact artefact = createNonStrategicPublication(
            ListType.AST_DAILY_HEARING_LIST, NON_STRATEGIC_FILES_LOCATION
                + "ast-daily-hearing-list/astDailyHearingList.xlsx"
        );

        byte[] jsonData = getTestData(NON_STRATEGIC_FILES_LOCATION
                                          + "ast-daily-hearing-list/astDailyHearingList.json");
        when(blobClient.downloadContent()).thenReturn(BinaryData.fromBytes(jsonData));

        MvcResult response = mockMvc.perform(get(String.format(GET_ARTEFACT_SUMMARY, artefact.getArtefactId())))
            .andExpect(status().isOk()).andReturn();

        String responseContent = response.getResponse().getContentAsString();
        assertTrue(responseContent.contains("Appellant - Appellant A"), CONTENT_MISMATCH_ERROR);
        assertTrue(responseContent.contains("Appeal reference number - 12345"), CONTENT_MISMATCH_ERROR);
        assertTrue(responseContent.contains("Hearing time - 10:30am"), CONTENT_MISMATCH_ERROR);
    }

    @Test
    void testGenerateArtefactSummaryNotFound() throws Exception {
        MvcResult mvcResult = mockMvc.perform(get(String.format(GET_ARTEFACT_SUMMARY, ARTEFACT_ID_NOT_FOUND)))
            .andExpect(status().isNotFound()).andReturn();

        ExceptionResponse exceptionResponse = OBJECT_MAPPER.readValue(
            mvcResult.getResponse().getContentAsString(), ExceptionResponse.class);

        assertEquals(
            ARTEFACT_NOT_FOUND_MESSAGE + ARTEFACT_ID_NOT_FOUND,
            exceptionResponse.getMessage(),
            NOT_FOUND_RESPONSE_MESSAGE
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
        UUID artefactId = createSjpPublicListPublication().getArtefactId();

        when(blobClient.downloadContent()).thenReturn(
            BinaryData.fromString(new String(file.getBytes())));
        when(accountManagementService.getUserById(any())).thenReturn(piUser);
        MvcResult response = mockMvc.perform(
                get(ROOT_URL + "/" + artefactId + "/exists")
                    .header(REQUESTER_HEADER, SYSTEM_ADMIN_ID))
            .andExpect(status().isOk()).andReturn();

        assertNotNull(
            response.getResponse().getContentAsString(),
            "Response should not be null"
        );
    }

    @Test
    void testGetFileSizes() throws Exception {
        UUID artefactId = createSjpPublicListPublication().getArtefactId();

        when(blobClient.downloadContent()).thenReturn(
            BinaryData.fromString(new String(file.getBytes())));
        when(accountManagementService.getUserById(any())).thenReturn(piUser);

        MvcResult response = mockMvc.perform(
                get(ROOT_URL + "/" + artefactId + "/sizes")
                    .header(REQUESTER_HEADER, SYSTEM_ADMIN_ID))
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
        when(accountManagementService.getUserById(any())).thenReturn(piUser);

        MvcResult response = mockMvc.perform(
                get(String.format(GET_FILE_URL, artefactId, PDF))
                    .header(SYSTEM_HEADER, "true")
                    .header(FILE_TYPE_HEADER, PDF)
                    .header(REQUESTER_HEADER, SYSTEM_ADMIN_ID)
                    .param(MAX_FILE_SIZE_HEADER, "2048000"))

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
    void testGetFileWithAuthorisedUser() throws Exception {
        when(accountManagementService.getUserById(any())).thenReturn(piUser);
        when(accountManagementService.getIsAuthorised(
            UUID.fromString(SYSTEM_ADMIN_ID), ListType.SJP_PRESS_LIST, Sensitivity.CLASSIFIED
        )).thenReturn(true);
        when(blobClient.downloadContent()).thenReturn(BinaryData.fromString(new String(file.getBytes())));

        byte[] data = getTestData("data/sjp-press-list/sjpPressList.json");
        Artefact artefact = createPublication(ListType.SJP_PRESS_LIST, Sensitivity.CLASSIFIED, data);
        MockHttpServletRequestBuilder request =
            get(String.format(GET_FILE_URL, artefact.getArtefactId(), PDF))
                .header(REQUESTER_HEADER, SYSTEM_ADMIN_ID)
                .header(SYSTEM_HEADER, "false")
                .param(MAX_FILE_SIZE_HEADER, "2048000");

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
    void testGetFileWithUnauthorisedUser() throws Exception {
        when(accountManagementService.getUserById(any())).thenReturn(piUser);
        when(accountManagementService.getIsAuthorised(
            UUID.fromString(SYSTEM_ADMIN_ID), ListType.SJP_PRESS_LIST, Sensitivity.CLASSIFIED
        )).thenReturn(false);

        byte[] data = getTestData("data/sjp-press-list/sjpPressList.json");
        Artefact artefact = createPublication(ListType.SJP_PRESS_LIST, Sensitivity.CLASSIFIED, data);
        MockHttpServletRequestBuilder request =
            get(String.format(GET_FILE_URL, artefact.getArtefactId(), PDF))
                .header(REQUESTER_HEADER, SYSTEM_ADMIN_ID)
                .header(SYSTEM_HEADER, "false")
                .param(MAX_FILE_SIZE_HEADER, "2048000");

        MvcResult response = mockMvc.perform(request)
            .andExpect(status().isForbidden()).andReturn();

        assertNotNull(
            response.getResponse().getContentAsString(),
            "Response does not match");
    }

    @Test
    void testGetFileSizeTooLarge() throws Exception {
        UUID artefactId = createSjpPublicListPublication().getArtefactId();
        when(accountManagementService.getUserById(any())).thenReturn(piUser);
        when(blobClient.downloadContent()).thenReturn(
            BinaryData.fromString(new String(file.getBytes())));

        MockHttpServletRequestBuilder request =
            get(String.format(GET_FILE_URL, artefactId, PDF))
                .header(REQUESTER_HEADER, SYSTEM_ADMIN_ID)
                .header(SYSTEM_HEADER, "false")
                .param(MAX_FILE_SIZE_HEADER, "10");

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
            ARTEFACT_NOT_FOUND_MESSAGE + ARTEFACT_ID_NOT_FOUND,
            exceptionResponse.getMessage(),
            NOT_FOUND_RESPONSE_MESSAGE
        );
    }

    @Test
    @WithMockUser(username = UNAUTHORIZED_USERNAME, authorities = {UNAUTHORIZED_ROLE})
    void testGetFileUnauthorized() throws Exception {
        mockMvc.perform(get(String.format(GET_FILE_URL, ARTEFACT_ID, PDF))
                            .header(REQUESTER_HEADER, SYSTEM_ADMIN_ID))
            .andExpect(status().isForbidden());
    }

    @Test
    void testGenerateNoExcelWhenFileTooBig() throws Exception {
        when(accountManagementService.getUserById(any())).thenReturn(piUser);
        createLocations();

        try (InputStream mockFile = this.getClass().getClassLoader().getResourceAsStream(SJP_MOCK)) {

            JsonElement jsonParser = JsonParser.parseReader(new InputStreamReader(mockFile));
            JsonArray jsonArray = jsonParser.getAsJsonObject().get("courtLists").getAsJsonArray();
            JsonElement jsonElement = jsonArray.get(0);
            for (int i = 0; i <= 400; i++) {
                jsonArray.add(jsonElement);
            }

            Artefact artefact =
                createPublication(ListType.SJP_PUBLIC_LIST, jsonParser.toString().getBytes(StandardCharsets.UTF_8));

            verify(publicationBlobContainerClient, never()).getBlobClient(artefact.getArtefactId() + ".pdf");
            verify(publicationBlobContainerClient, never()).getBlobClient(artefact.getArtefactId() + ".xlsx");

        }
    }

    @Test
    void testGenerateNoPdfWhenFileTooBig() throws Exception {
        when(accountManagementService.getUserById(any())).thenReturn(piUser);
        createLocations();

        try (InputStream mockFile = this.getClass().getClassLoader().getResourceAsStream(SJP_MOCK)) {

            JsonElement jsonParser = JsonParser.parseReader(new InputStreamReader(mockFile));
            JsonArray jsonArray = jsonParser.getAsJsonObject().get("courtLists").getAsJsonArray();
            JsonElement jsonElement = jsonArray.get(0);
            for (int i = 0; i <= 200; i++) {
                jsonArray.add(jsonElement);
            }

            Artefact artefact =
                createPublication(ListType.SJP_PUBLIC_LIST, jsonParser.toString().getBytes(StandardCharsets.UTF_8));

            verify(publicationBlobContainerClient, never()).getBlobClient(artefact.getArtefactId() + ".pdf");
            verify(publicationBlobContainerClient, times(1))
                .getBlobClient(artefact.getArtefactId() + ".xlsx");
        }
    }
}
