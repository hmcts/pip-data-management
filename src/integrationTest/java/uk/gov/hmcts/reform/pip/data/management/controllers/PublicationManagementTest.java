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
@SuppressWarnings({"PMD.TooManyMethods", "PMD.ExcessiveImports", "PMD.CyclomaticComplexity"})
class PublicationManagementTest extends IntegrationTestBase {
    private static final String ROOT_URL = "/publication";
    private static final String GET_ARTEFACT_SUMMARY = ROOT_URL + "/%s/summary";
    private static final String GET_FILE_URL = ROOT_URL + "/%s/%s";
    private static final String ARTEFACT_ID = UUID.randomUUID().toString();
    private static final String ARTEFACT_ID_NOT_FOUND = UUID.randomUUID().toString();
    private static final String ARTEFACT_NOT_FOUND_MESSAGE = "No artefact found with the ID: ";
    private static final String NOT_FOUND_RESPONSE_MESSAGE = "Artefact not found message does not match";
    private static final String CONTENT_MISMATCH_ERROR = "Artefact summary content should match";
    private static final String NULL_RESPONSE_ERROR = "Response should not be null";
    private static final String UNEXPECTED_RESPONSE_ERROR = "Response does not contain expected result";
    private static final String FILE_TYPE_HEADER = "x-file-type";
    private static final String USER_ID_HEADER = "x-user-id";
    private static final String MAX_FILE_SIZE_HEADER = "maxFileSize";
    private static final String MAX_FILE_SIZE =  "2048000";
    private static final String UNAUTHORIZED_USERNAME = "unauthorized_username";
    private static final String UNAUTHORIZED_ROLE = "APPROLE_unknown.role";
    private static final String SYSTEM_HEADER = "x-system";
    private static final String FALSE = "false";
    private static final String TEST_CONTENT = "test content";
    private static final String CASE_REFERENCE_FIELD = "Case reference - 12341234";
    private static final String CASE_NAME_FIELD = "Case name - This is a case name";
    private static final String HEARING_TYPE_FIELD = "Hearing type - Directions";
    private static final String DATE_FIELD = "Date - 16 December 2024";
    private static final String HEARING_TIME_FIELD = "Hearing time - 10am";
    private static final String IAC_HEARING_TIME_FIELD = "Hearing time - 10:30am";
    private static final String CASE_REFERENCE_NUMBER_FIELD = "Case reference number - 1234";
    private static final String TIME_FIELD = "Time - 10am";
    private static final String APPELLANT_NUMBER_FIELD = "Appellant - Appellant 1";
    private static final String RCJ_TIME_FIELD = "Time - 9am";
    private static final String RCJ_CASE_NUMBER_FIELD = "Case number - 12345";
    private static final String CASE_NUMBER_FIELD = "Case number - 1234";
    private static final String RB_TIME_FIELD = "Time - 9am";
    private static final String RB_CASE_NUMBER_FIELD = "Case number - 12345";
    private static final String RB_CASE_NAME_FIELD = "Case name - Case name A";

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
    private static final String ADMINISTRATIVE_COURT_DAILY_CAUSE_LISTS_JSON_FILE =
        "administrative-court-daily-cause-list/administrativeCourtDailyCauseList.json";
    private static final String ADMINISTRATIVE_COURT_DAILY_CAUSE_LISTS_EXCEL_FILE =
        "administrative-court-daily-cause-list/administrativeCourtDailyCauseList.xlsx";

    private static final LocalDateTime DISPLAY_TO = LocalDateTime.now()
        .truncatedTo(ChronoUnit.SECONDS);
    private static final LocalDateTime DISPLAY_FROM = LocalDateTime.now().plusDays(1)
        .truncatedTo(ChronoUnit.SECONDS);
    private static final LocalDateTime CONTENT_DATE = LocalDateTime.now().toLocalDate().atStartOfDay()
        .truncatedTo(ChronoUnit.SECONDS);
    private static final String PROVENANCE = "MANUAL_UPLOAD";
    private static final String USER_ID = UUID.randomUUID().toString();

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private static final String SJP_MOCK = "data/sjp-public-list/sjpPublicList.json";
    private static final String SJP_PRESS_MOCK = "data/sjp-press-list/sjpPressList.json";

    private static MockMultipartFile file;
    private static final String EXCEL_FILE_TYPE = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";

    @Autowired
    private MockMvc mockMvc;

    @BeforeAll
    static void setup() {
        file = new MockMultipartFile("file", "test.pdf",
                                     MediaType.APPLICATION_PDF_VALUE, TEST_CONTENT.getBytes(
            StandardCharsets.UTF_8)
        );

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
        MockHttpServletRequestBuilder mockHttpServletRequestBuilder = MockMvcRequestBuilders
            .post(ROOT_URL)
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
            .content(data)
            .contentType(MediaType.APPLICATION_JSON);

        MvcResult response = mockMvc.perform(mockHttpServletRequestBuilder)
            .andExpect(status().isCreated()).andReturn();

        return OBJECT_MAPPER.readValue(
            response.getResponse().getContentAsString(), Artefact.class);
    }

    private Artefact createNonStrategicPublication(ListType listType, String filePath) throws Exception {
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

            mockMvc.perform(multipart("/locations/upload").file(csvFile))
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
        byte[] data = getTestData(SJP_PRESS_MOCK);
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
            "UT_IAC_JR_LEEDS_DAILY_HEARING_LIST",
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
        assertTrue(responseContent.contains(IAC_HEARING_TIME_FIELD), CONTENT_MISMATCH_ERROR);
        assertTrue(responseContent.contains(CASE_REFERENCE_NUMBER_FIELD), CONTENT_MISMATCH_ERROR);
    }

    @Test
    void testGenerateArtefactSummaryUtIacJudicialReviewLondonDailyHearingList() throws Exception {
        Artefact artefact = createNonStrategicPublication(
            ListType.UT_IAC_JR_LONDON_DAILY_HEARING_LIST,
            NON_STRATEGIC_FILES_LOCATION
                + "ut-iac-judicial-review-london-daily-hearing-list/utIacJudicialReviewLondonDailyHearingList.xlsx"
        );

        byte[] jsonData = getTestData(
            NON_STRATEGIC_FILES_LOCATION
                + "ut-iac-judicial-review-london-daily-hearing-list/utIacJudicialReviewLondonDailyHearingList.json"
        );
        when(blobClient.downloadContent()).thenReturn(BinaryData.fromBytes(jsonData));

        MvcResult response = mockMvc.perform(get(String.format(GET_ARTEFACT_SUMMARY, artefact.getArtefactId())))
            .andExpect(status().isOk()).andReturn();

        String responseContent = response.getResponse().getContentAsString();
        assertTrue(responseContent.contains(IAC_HEARING_TIME_FIELD), CONTENT_MISMATCH_ERROR);
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
        assertTrue(responseContent.contains(IAC_HEARING_TIME_FIELD), CONTENT_MISMATCH_ERROR);
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
        assertTrue(responseContent.contains(IAC_HEARING_TIME_FIELD), CONTENT_MISMATCH_ERROR);
    }

    @Test
    void testGenerateArtefactSummaryLondonAdministrativeCourtDailyCauseList() throws Exception {
        Artefact artefact = createNonStrategicPublication(
            ListType.LONDON_ADMINISTRATIVE_COURT_DAILY_CAUSE_LIST, NON_STRATEGIC_FILES_LOCATION
                + "london-administrative-court-daily-cause-list/londonAdministrativeCourtDailyCauseList.xlsx"
        );

        byte[] jsonData = getTestData(NON_STRATEGIC_FILES_LOCATION
                                          + "london-administrative-court-daily-cause-list/"
                                          + "londonAdministrativeCourtDailyCauseList.json");
        when(blobClient.downloadContent()).thenReturn(BinaryData.fromBytes(jsonData));

        MvcResult response = mockMvc.perform(get(String.format(GET_ARTEFACT_SUMMARY, artefact.getArtefactId())))
            .andExpect(status().isOk()).andReturn();

        String responseContent = response.getResponse().getContentAsString();
        assertTrue(responseContent.contains(RCJ_TIME_FIELD), CONTENT_MISMATCH_ERROR);
        assertTrue(responseContent.contains(RCJ_CASE_NUMBER_FIELD), CONTENT_MISMATCH_ERROR);
    }

    @Test
    void testGenerateArtefactSummaryPlanningCourtDailyCauseList() throws Exception {
        Artefact artefact = createNonStrategicPublication(
            ListType.PLANNING_COURT_DAILY_CAUSE_LIST, NON_STRATEGIC_FILES_LOCATION
                + "planning-court-daily-cause-list/planningCourtDailyCauseList.xlsx"
        );

        byte[] jsonData = getTestData(NON_STRATEGIC_FILES_LOCATION
                                          + "planning-court-daily-cause-list/planningCourtDailyCauseList.json");
        when(blobClient.downloadContent()).thenReturn(BinaryData.fromBytes(jsonData));

        MvcResult response = mockMvc.perform(get(String.format(GET_ARTEFACT_SUMMARY, artefact.getArtefactId())))
            .andExpect(status().isOk()).andReturn();

        String responseContent = response.getResponse().getContentAsString();
        assertTrue(responseContent.contains(RCJ_TIME_FIELD), CONTENT_MISMATCH_ERROR);
        assertTrue(responseContent.contains(RCJ_CASE_NUMBER_FIELD), CONTENT_MISMATCH_ERROR);
    }

    @Test
    void testGenerateArtefactSummaryCountyCourtLondonCivilDailyCauseList() throws Exception {
        Artefact artefact = createNonStrategicPublication(
            ListType.COUNTY_COURT_LONDON_CIVIL_DAILY_CAUSE_LIST, NON_STRATEGIC_FILES_LOCATION
                + "county-court-london-civil-daily-cause-list/countyCourtLondonCivilDailyCauseList.xlsx"
        );

        byte[] jsonData = getTestData(NON_STRATEGIC_FILES_LOCATION
                                          + "county-court-london-civil-daily-cause-list/"
                                          + "countyCourtLondonCivilDailyCauseList.json");
        when(blobClient.downloadContent()).thenReturn(BinaryData.fromBytes(jsonData));

        MvcResult response = mockMvc.perform(get(String.format(GET_ARTEFACT_SUMMARY, artefact.getArtefactId())))
            .andExpect(status().isOk()).andReturn();

        String responseContent = response.getResponse().getContentAsString();
        assertTrue(responseContent.contains(RCJ_TIME_FIELD), CONTENT_MISMATCH_ERROR);
        assertTrue(responseContent.contains(RCJ_CASE_NUMBER_FIELD), CONTENT_MISMATCH_ERROR);
    }

    @Test
    void testGenerateArtefactSummaryCivilCourtsRcjDailyCauseList() throws Exception {
        Artefact artefact = createNonStrategicPublication(
            ListType.CIVIL_COURTS_RCJ_DAILY_CAUSE_LIST, NON_STRATEGIC_FILES_LOCATION
                + "civil-courts-rcj-daily-cause-list/civilCourtsRcjDailyCauseList.xlsx"
        );

        byte[] jsonData = getTestData(NON_STRATEGIC_FILES_LOCATION
                                          + "civil-courts-rcj-daily-cause-list/civilCourtsRcjDailyCauseList.json");
        when(blobClient.downloadContent()).thenReturn(BinaryData.fromBytes(jsonData));

        MvcResult response = mockMvc.perform(get(String.format(GET_ARTEFACT_SUMMARY, artefact.getArtefactId())))
            .andExpect(status().isOk()).andReturn();

        String responseContent = response.getResponse().getContentAsString();
        assertTrue(responseContent.contains(RCJ_TIME_FIELD), CONTENT_MISMATCH_ERROR);
        assertTrue(responseContent.contains(RCJ_CASE_NUMBER_FIELD), CONTENT_MISMATCH_ERROR);
    }

    @Test
    void testGenerateArtefactSummaryCourtOfAppealCriminalDailyCauseList() throws Exception {
        Artefact artefact = createNonStrategicPublication(
            ListType.COURT_OF_APPEAL_CRIMINAL_DAILY_CAUSE_LIST, NON_STRATEGIC_FILES_LOCATION
                + "court-of-appeal-criminal-daily-cause-list/courtOfAppealCriminalDailyCauseList.xlsx"
        );

        byte[] jsonData = getTestData(NON_STRATEGIC_FILES_LOCATION
                                          + "court-of-appeal-criminal-daily-cause-list/"
                                          + "courtOfAppealCriminalDailyCauseList.json");
        when(blobClient.downloadContent()).thenReturn(BinaryData.fromBytes(jsonData));

        MvcResult response = mockMvc.perform(get(String.format(GET_ARTEFACT_SUMMARY, artefact.getArtefactId())))
            .andExpect(status().isOk()).andReturn();

        String responseContent = response.getResponse().getContentAsString();
        assertTrue(responseContent.contains(RCJ_TIME_FIELD), CONTENT_MISMATCH_ERROR);
        assertTrue(responseContent.contains(RCJ_CASE_NUMBER_FIELD), CONTENT_MISMATCH_ERROR);
    }

    @Test
    void testGenerateArtefactSummaryFamilyDivisionHighCourtDailyCauseList() throws Exception {
        Artefact artefact = createNonStrategicPublication(
            ListType.FAMILY_DIVISION_HIGH_COURT_DAILY_CAUSE_LIST, NON_STRATEGIC_FILES_LOCATION
                + "family-division-high-court-daily-cause-list/familyDivisionHighCourtDailyCauseList.xlsx"
        );

        byte[] jsonData = getTestData(NON_STRATEGIC_FILES_LOCATION
                                          + "family-division-high-court-daily-cause-list/"
                                          + "familyDivisionHighCourtDailyCauseList.json");
        when(blobClient.downloadContent()).thenReturn(BinaryData.fromBytes(jsonData));

        MvcResult response = mockMvc.perform(get(String.format(GET_ARTEFACT_SUMMARY, artefact.getArtefactId())))
            .andExpect(status().isOk()).andReturn();

        String responseContent = response.getResponse().getContentAsString();
        assertTrue(responseContent.contains(RCJ_TIME_FIELD), CONTENT_MISMATCH_ERROR);
        assertTrue(responseContent.contains(RCJ_CASE_NUMBER_FIELD), CONTENT_MISMATCH_ERROR);
    }

    @Test
    void testGenerateArtefactSummaryKingsBenchDivisionDailyCauseList() throws Exception {
        Artefact artefact = createNonStrategicPublication(
            ListType.KINGS_BENCH_DIVISION_DAILY_CAUSE_LIST, NON_STRATEGIC_FILES_LOCATION
                + "kings-bench-division-daily-cause-list/kingsBenchDivisionDailyCauseList.xlsx"
        );

        byte[] jsonData = getTestData(NON_STRATEGIC_FILES_LOCATION
                                          + "kings-bench-division-daily-cause-list/"
                                          + "kingsBenchDivisionDailyCauseList.json");
        when(blobClient.downloadContent()).thenReturn(BinaryData.fromBytes(jsonData));

        MvcResult response = mockMvc.perform(get(String.format(GET_ARTEFACT_SUMMARY, artefact.getArtefactId())))
            .andExpect(status().isOk()).andReturn();

        String responseContent = response.getResponse().getContentAsString();
        assertTrue(responseContent.contains(RCJ_TIME_FIELD), CONTENT_MISMATCH_ERROR);
        assertTrue(responseContent.contains(RCJ_CASE_NUMBER_FIELD), CONTENT_MISMATCH_ERROR);
    }

    @Test
    void testGenerateArtefactSummaryKingsBenchMastersDailyCauseList() throws Exception {
        Artefact artefact = createNonStrategicPublication(
            ListType.KINGS_BENCH_MASTERS_DAILY_CAUSE_LIST, NON_STRATEGIC_FILES_LOCATION
                + "kings-bench-masters-daily-cause-list/kingsBenchMastersDailyCauseList.xlsx"
        );

        byte[] jsonData = getTestData(NON_STRATEGIC_FILES_LOCATION
                                          + "kings-bench-masters-daily-cause-list/"
                                          + "kingsBenchMastersDailyCauseList.json");
        when(blobClient.downloadContent()).thenReturn(BinaryData.fromBytes(jsonData));

        MvcResult response = mockMvc.perform(get(String.format(GET_ARTEFACT_SUMMARY, artefact.getArtefactId())))
            .andExpect(status().isOk()).andReturn();

        String responseContent = response.getResponse().getContentAsString();
        assertTrue(responseContent.contains(RCJ_TIME_FIELD), CONTENT_MISMATCH_ERROR);
        assertTrue(responseContent.contains(RCJ_CASE_NUMBER_FIELD), CONTENT_MISMATCH_ERROR);
    }

    @Test
    void testGenerateArtefactSummarySeniorCourtsCostsOfficeDailyCauseList() throws Exception {
        Artefact artefact = createNonStrategicPublication(
            ListType.SENIOR_COURTS_COSTS_OFFICE_DAILY_CAUSE_LIST, NON_STRATEGIC_FILES_LOCATION
                + "senior-courts-costs-office-daily-cause-list/seniorCourtsCostsOfficeDailyCauseList.xlsx"
        );

        byte[] jsonData = getTestData(NON_STRATEGIC_FILES_LOCATION
                                          + "senior-courts-costs-office-daily-cause-list/"
                                          + "seniorCourtsCostsOfficeDailyCauseList.json");
        when(blobClient.downloadContent()).thenReturn(BinaryData.fromBytes(jsonData));

        MvcResult response = mockMvc.perform(get(String.format(GET_ARTEFACT_SUMMARY, artefact.getArtefactId())))
            .andExpect(status().isOk()).andReturn();

        String responseContent = response.getResponse().getContentAsString();
        assertTrue(responseContent.contains(RCJ_TIME_FIELD), CONTENT_MISMATCH_ERROR);
        assertTrue(responseContent.contains(RCJ_CASE_NUMBER_FIELD), CONTENT_MISMATCH_ERROR);
    }

    @Test
    void testGenerateArtefactSummaryMayorAndCityCivilDailyCauseList() throws Exception {
        Artefact artefact = createNonStrategicPublication(
            ListType.MAYOR_AND_CITY_CIVIL_DAILY_CAUSE_LIST, NON_STRATEGIC_FILES_LOCATION
                + "mayor-and-city-civil-daily-cause-list/mayorAndCityCivilDailyCauseList.xlsx"
        );

        byte[] jsonData = getTestData(NON_STRATEGIC_FILES_LOCATION
                                          + "mayor-and-city-civil-daily-cause-list/"
                                          + "mayorAndCityCivilDailyCauseList.json");
        when(blobClient.downloadContent()).thenReturn(BinaryData.fromBytes(jsonData));

        MvcResult response = mockMvc.perform(get(String.format(GET_ARTEFACT_SUMMARY, artefact.getArtefactId())))
            .andExpect(status().isOk()).andReturn();

        String responseContent = response.getResponse().getContentAsString();
        assertTrue(responseContent.contains(RCJ_TIME_FIELD), CONTENT_MISMATCH_ERROR);
        assertTrue(responseContent.contains(RCJ_CASE_NUMBER_FIELD), CONTENT_MISMATCH_ERROR);
    }

    @Test
    void testGenerateArtefactSummaryIntellectualPropertyAndEnterpriseCourtDailyCauseList() throws Exception {
        Artefact artefact = createNonStrategicPublication(
            ListType.INTELLECTUAL_PROPERTY_AND_ENTERPRISE_COURT_DAILY_CAUSE_LIST, NON_STRATEGIC_FILES_LOCATION
                + "intellectual-property-and-enterprise-court-daily-cause-list/"
                + "intellectualPropertyAndEnterpriseCourtDailyCauseList.xlsx"
        );

        byte[] jsonData = getTestData(NON_STRATEGIC_FILES_LOCATION
                                          + "intellectual-property-and-enterprise-court-daily-cause-list/"
                                          + "intellectualPropertyAndEnterpriseCourtDailyCauseList.json");
        when(blobClient.downloadContent()).thenReturn(BinaryData.fromBytes(jsonData));

        MvcResult response = mockMvc.perform(get(String.format(GET_ARTEFACT_SUMMARY, artefact.getArtefactId())))
            .andExpect(status().isOk()).andReturn();

        String responseContent = response.getResponse().getContentAsString();
        assertTrue(responseContent.contains(RB_TIME_FIELD), CONTENT_MISMATCH_ERROR);
        assertTrue(responseContent.contains(RB_CASE_NUMBER_FIELD), CONTENT_MISMATCH_ERROR);
        assertTrue(responseContent.contains(RB_CASE_NAME_FIELD), CONTENT_MISMATCH_ERROR);
    }

    @Test
    void testGenerateArtefactSummaryIntellectualPropertyListChdDailyCauseList() throws Exception {
        Artefact artefact = createNonStrategicPublication(
            ListType.INTELLECTUAL_PROPERTY_LIST_CHD_DAILY_CAUSE_LIST, NON_STRATEGIC_FILES_LOCATION
                + "intellectual-property-list-chd-daily-cause-list/intellectualPropertyListChdDailyCauseList.xlsx"
        );

        byte[] jsonData = getTestData(NON_STRATEGIC_FILES_LOCATION
                                          + "intellectual-property-list-chd-daily-cause-list/"
                                          + "intellectualPropertyListChdDailyCauseList.json");
        when(blobClient.downloadContent()).thenReturn(BinaryData.fromBytes(jsonData));

        MvcResult response = mockMvc.perform(get(String.format(GET_ARTEFACT_SUMMARY, artefact.getArtefactId())))
            .andExpect(status().isOk()).andReturn();

        String responseContent = response.getResponse().getContentAsString();
        assertTrue(responseContent.contains(RB_TIME_FIELD), CONTENT_MISMATCH_ERROR);
        assertTrue(responseContent.contains(RB_CASE_NUMBER_FIELD), CONTENT_MISMATCH_ERROR);
        assertTrue(responseContent.contains(RB_CASE_NAME_FIELD), CONTENT_MISMATCH_ERROR);
    }

    @Test
    void testGenerateArtefactSummaryLondonCircuitCommercialCourtKbDailyCauseList() throws Exception {
        Artefact artefact = createNonStrategicPublication(
            ListType.LONDON_CIRCUIT_COMMERCIAL_COURT_KB_DAILY_CAUSE_LIST, NON_STRATEGIC_FILES_LOCATION
                + "london-circuit-commercial-court-kb-daily-cause-list/"
                + "londonCircuitCommercialCourtKbDailyCauseList.xlsx"
        );

        byte[] jsonData = getTestData(NON_STRATEGIC_FILES_LOCATION
                                          + "london-circuit-commercial-court-kb-daily-cause-list/"
                                          + "londonCircuitCommercialCourtKbDailyCauseList.json");
        when(blobClient.downloadContent()).thenReturn(BinaryData.fromBytes(jsonData));

        MvcResult response = mockMvc.perform(get(String.format(GET_ARTEFACT_SUMMARY, artefact.getArtefactId())))
            .andExpect(status().isOk()).andReturn();

        String responseContent = response.getResponse().getContentAsString();
        assertTrue(responseContent.contains(RB_TIME_FIELD), CONTENT_MISMATCH_ERROR);
        assertTrue(responseContent.contains(RB_CASE_NUMBER_FIELD), CONTENT_MISMATCH_ERROR);
        assertTrue(responseContent.contains(RB_CASE_NAME_FIELD), CONTENT_MISMATCH_ERROR);
    }

    @Test
    void testGenerateArtefactSummaryPatentsCourtChdDailyCauseList() throws Exception {
        Artefact artefact = createNonStrategicPublication(
            ListType.PATENTS_COURT_CHD_DAILY_CAUSE_LIST, NON_STRATEGIC_FILES_LOCATION
                + "patents-court-chd-daily-cause-list/patentsCourtChdDailyCauseList.xlsx"
        );

        byte[] jsonData = getTestData(NON_STRATEGIC_FILES_LOCATION
                                          + "patents-court-chd-daily-cause-list/patentsCourtChdDailyCauseList.json");
        when(blobClient.downloadContent()).thenReturn(BinaryData.fromBytes(jsonData));

        MvcResult response = mockMvc.perform(get(String.format(GET_ARTEFACT_SUMMARY, artefact.getArtefactId())))
            .andExpect(status().isOk()).andReturn();

        String responseContent = response.getResponse().getContentAsString();
        assertTrue(responseContent.contains(RB_TIME_FIELD), CONTENT_MISMATCH_ERROR);
        assertTrue(responseContent.contains(RB_CASE_NUMBER_FIELD), CONTENT_MISMATCH_ERROR);
        assertTrue(responseContent.contains(RB_CASE_NAME_FIELD), CONTENT_MISMATCH_ERROR);
    }

    @Test
    void testGenerateArtefactSummaryPensionsListChdDailyCauseList() throws Exception {
        Artefact artefact = createNonStrategicPublication(
            ListType.PENSIONS_LIST_CHD_DAILY_CAUSE_LIST, NON_STRATEGIC_FILES_LOCATION
                + "pensions-list-chd-daily-cause-list/pensionsListChdDailyCauseList.xlsx"
        );

        byte[] jsonData = getTestData(NON_STRATEGIC_FILES_LOCATION
                                          + "pensions-list-chd-daily-cause-list/pensionsListChdDailyCauseList.json");
        when(blobClient.downloadContent()).thenReturn(BinaryData.fromBytes(jsonData));

        MvcResult response = mockMvc.perform(get(String.format(GET_ARTEFACT_SUMMARY, artefact.getArtefactId())))
            .andExpect(status().isOk()).andReturn();

        String responseContent = response.getResponse().getContentAsString();
        assertTrue(responseContent.contains(RB_TIME_FIELD), CONTENT_MISMATCH_ERROR);
        assertTrue(responseContent.contains(RB_CASE_NUMBER_FIELD), CONTENT_MISMATCH_ERROR);
        assertTrue(responseContent.contains(RB_CASE_NAME_FIELD), CONTENT_MISMATCH_ERROR);
    }

    @Test
    void testGenerateArtefactSummaryPropertyTrustsProbateListChdDailyCauseList() throws Exception {
        Artefact artefact = createNonStrategicPublication(
            ListType.PROPERTY_TRUSTS_PROBATE_LIST_CHD_DAILY_CAUSE_LIST, NON_STRATEGIC_FILES_LOCATION
                + "property-trusts-probate-list-chd-daily-cause-list/propertyTrustsProbateListChdDailyCauseList.xlsx"
        );

        byte[] jsonData = getTestData(NON_STRATEGIC_FILES_LOCATION
                                          + "property-trusts-probate-list-chd-daily-cause-list/"
                                          + "propertyTrustsProbateListChdDailyCauseList.json");
        when(blobClient.downloadContent()).thenReturn(BinaryData.fromBytes(jsonData));

        MvcResult response = mockMvc.perform(get(String.format(GET_ARTEFACT_SUMMARY, artefact.getArtefactId())))
            .andExpect(status().isOk()).andReturn();

        String responseContent = response.getResponse().getContentAsString();
        assertTrue(responseContent.contains(RB_TIME_FIELD), CONTENT_MISMATCH_ERROR);
        assertTrue(responseContent.contains(RB_CASE_NUMBER_FIELD), CONTENT_MISMATCH_ERROR);
        assertTrue(responseContent.contains(RB_CASE_NAME_FIELD), CONTENT_MISMATCH_ERROR);
    }

    @Test
    void testGenerateArtefactSummaryRevenueListChdDailyCauseList() throws Exception {
        Artefact artefact = createNonStrategicPublication(
            ListType.REVENUE_LIST_CHD_DAILY_CAUSE_LIST, NON_STRATEGIC_FILES_LOCATION
                + "revenue-list-chd-daily-cause-list/revenueListChdDailyCauseList.xlsx"
        );

        byte[] jsonData = getTestData(NON_STRATEGIC_FILES_LOCATION
                                          + "revenue-list-chd-daily-cause-list/revenueListChdDailyCauseList.json");
        when(blobClient.downloadContent()).thenReturn(BinaryData.fromBytes(jsonData));

        MvcResult response = mockMvc.perform(get(String.format(GET_ARTEFACT_SUMMARY, artefact.getArtefactId())))
            .andExpect(status().isOk()).andReturn();

        String responseContent = response.getResponse().getContentAsString();
        assertTrue(responseContent.contains(RB_TIME_FIELD), CONTENT_MISMATCH_ERROR);
        assertTrue(responseContent.contains(RB_CASE_NUMBER_FIELD), CONTENT_MISMATCH_ERROR);
        assertTrue(responseContent.contains(RB_CASE_NAME_FIELD), CONTENT_MISMATCH_ERROR);
    }

    @Test
    void testGenerateArtefactSummaryTechnologyAndConstructionCourtKbDailyCauseList() throws Exception {
        Artefact artefact = createNonStrategicPublication(
            ListType.TECHNOLOGY_AND_CONSTRUCTION_COURT_KB_DAILY_CAUSE_LIST, NON_STRATEGIC_FILES_LOCATION
                + "technology-and-construction-court-kb-daily-cause-list/"
                + "technologyAndConstructionCourtKbDailyCauseList.xlsx"
        );

        byte[] jsonData = getTestData(NON_STRATEGIC_FILES_LOCATION
                                          + "technology-and-construction-court-kb-daily-cause-list/"
                                          + "technologyAndConstructionCourtKbDailyCauseList.json");
        when(blobClient.downloadContent()).thenReturn(BinaryData.fromBytes(jsonData));

        MvcResult response = mockMvc.perform(get(String.format(GET_ARTEFACT_SUMMARY, artefact.getArtefactId())))
            .andExpect(status().isOk()).andReturn();

        String responseContent = response.getResponse().getContentAsString();
        assertTrue(responseContent.contains(RB_TIME_FIELD), CONTENT_MISMATCH_ERROR);
        assertTrue(responseContent.contains(RB_CASE_NUMBER_FIELD), CONTENT_MISMATCH_ERROR);
        assertTrue(responseContent.contains(RB_CASE_NAME_FIELD), CONTENT_MISMATCH_ERROR);
    }

    @Test
    void testGenerateArtefactSummaryAdmiraltyCourtKbDailyCauseList() throws Exception {
        Artefact artefact = createNonStrategicPublication(
            ListType.ADMIRALTY_COURT_KB_DAILY_CAUSE_LIST, NON_STRATEGIC_FILES_LOCATION
                + "admiralty_court_kb_daily_cause_list/"
                + "admiraltyCourtKbDailyCauseList.xlsx"
        );

        byte[] jsonData = getTestData(NON_STRATEGIC_FILES_LOCATION
                                          + "admiralty_court_kb_daily_cause_list/"
                                          + "admiraltyCourtKbDailyCauseList.json");
        when(blobClient.downloadContent()).thenReturn(BinaryData.fromBytes(jsonData));

        MvcResult response = mockMvc.perform(get(String.format(GET_ARTEFACT_SUMMARY, artefact.getArtefactId())))
            .andExpect(status().isOk()).andReturn();

        String responseContent = response.getResponse().getContentAsString();
        assertTrue(responseContent.contains(RB_TIME_FIELD), CONTENT_MISMATCH_ERROR);
        assertTrue(responseContent.contains(RB_CASE_NUMBER_FIELD), CONTENT_MISMATCH_ERROR);
        assertTrue(responseContent.contains(RB_CASE_NAME_FIELD), CONTENT_MISMATCH_ERROR);
    }

    @Test
    void testGenerateArtefactSummaryBusinessListChdDailyCauseList() throws Exception {
        Artefact artefact = createNonStrategicPublication(
            ListType.BUSINESS_LIST_CHD_DAILY_CAUSE_LIST, NON_STRATEGIC_FILES_LOCATION
                + "business_list_chd_daily_cause_list/"
                + "businessListChdDailyCauseList.xlsx"
        );

        byte[] jsonData = getTestData(NON_STRATEGIC_FILES_LOCATION
                                          + "business_list_chd_daily_cause_list/"
                                          + "businessListChdDailyCauseList.json");
        when(blobClient.downloadContent()).thenReturn(BinaryData.fromBytes(jsonData));

        MvcResult response = mockMvc.perform(get(String.format(GET_ARTEFACT_SUMMARY, artefact.getArtefactId())))
            .andExpect(status().isOk()).andReturn();

        String responseContent = response.getResponse().getContentAsString();
        assertTrue(responseContent.contains(RB_TIME_FIELD), CONTENT_MISMATCH_ERROR);
        assertTrue(responseContent.contains(RB_CASE_NUMBER_FIELD), CONTENT_MISMATCH_ERROR);
        assertTrue(responseContent.contains(RB_CASE_NAME_FIELD), CONTENT_MISMATCH_ERROR);
    }

    @Test
    void testGenerateArtefactSummaryChanceryAppealsChdDailyCauseList() throws Exception {
        Artefact artefact = createNonStrategicPublication(
            ListType.CHANCERY_APPEALS_CHD_DAILY_CAUSE_LIST, NON_STRATEGIC_FILES_LOCATION
                + "chancery_appeals_chd_daily_cause_list/"
                + "chanceryAppealsChdDailyCauseList.xlsx"
        );

        byte[] jsonData = getTestData(NON_STRATEGIC_FILES_LOCATION
                                          + "chancery_appeals_chd_daily_cause_list/"
                                          + "chanceryAppealsChdDailyCauseList.json");
        when(blobClient.downloadContent()).thenReturn(BinaryData.fromBytes(jsonData));

        MvcResult response = mockMvc.perform(get(String.format(GET_ARTEFACT_SUMMARY, artefact.getArtefactId())))
            .andExpect(status().isOk()).andReturn();

        String responseContent = response.getResponse().getContentAsString();
        assertTrue(responseContent.contains(RB_TIME_FIELD), CONTENT_MISMATCH_ERROR);
        assertTrue(responseContent.contains(RB_CASE_NUMBER_FIELD), CONTENT_MISMATCH_ERROR);
        assertTrue(responseContent.contains(RB_CASE_NAME_FIELD), CONTENT_MISMATCH_ERROR);
    }

    @Test
    void testGenerateArtefactSummaryCommercialCourtKbDailyCauseList() throws Exception {
        Artefact artefact = createNonStrategicPublication(
            ListType.COMMERCIAL_COURT_KB_DAILY_CAUSE_LIST, NON_STRATEGIC_FILES_LOCATION
                + "commercial_court_kb_daily_cause_list/"
                + "commercialCourtKbDailyCauseList.xlsx"
        );

        byte[] jsonData = getTestData(NON_STRATEGIC_FILES_LOCATION
                                          + "commercial_court_kb_daily_cause_list/"
                                          + "commercialCourtKbDailyCauseList.json");
        when(blobClient.downloadContent()).thenReturn(BinaryData.fromBytes(jsonData));

        MvcResult response = mockMvc.perform(get(String.format(GET_ARTEFACT_SUMMARY, artefact.getArtefactId())))
            .andExpect(status().isOk()).andReturn();

        String responseContent = response.getResponse().getContentAsString();
        assertTrue(responseContent.contains(RB_TIME_FIELD), CONTENT_MISMATCH_ERROR);
        assertTrue(responseContent.contains(RB_CASE_NUMBER_FIELD), CONTENT_MISMATCH_ERROR);
        assertTrue(responseContent.contains(RB_CASE_NAME_FIELD), CONTENT_MISMATCH_ERROR);
    }

    @Test
    void testGenerateArtefactSummaryCompaniesWindingUpChdDailyCauseList() throws Exception {
        Artefact artefact = createNonStrategicPublication(
            ListType.COMPANIES_WINDING_UP_CHD_DAILY_CAUSE_LIST, NON_STRATEGIC_FILES_LOCATION
                + "companies_winding_up_chd_daily_cause_list/"
                + "companiesWindingUpChdDailyCauseList.xlsx"
        );

        byte[] jsonData = getTestData(NON_STRATEGIC_FILES_LOCATION
                                          + "companies_winding_up_chd_daily_cause_list/"
                                          + "companiesWindingUpChdDailyCauseList.json");
        when(blobClient.downloadContent()).thenReturn(BinaryData.fromBytes(jsonData));

        MvcResult response = mockMvc.perform(get(String.format(GET_ARTEFACT_SUMMARY, artefact.getArtefactId())))
            .andExpect(status().isOk()).andReturn();

        String responseContent = response.getResponse().getContentAsString();
        assertTrue(responseContent.contains(RB_TIME_FIELD), CONTENT_MISMATCH_ERROR);
        assertTrue(responseContent.contains(RB_CASE_NUMBER_FIELD), CONTENT_MISMATCH_ERROR);
        assertTrue(responseContent.contains(RB_CASE_NAME_FIELD), CONTENT_MISMATCH_ERROR);
    }

    @Test
    void testGenerateArtefactSummaryCompetitionListChdDailyCauseList() throws Exception {
        Artefact artefact = createNonStrategicPublication(
            ListType.COMPETITION_LIST_CHD_DAILY_CAUSE_LIST, NON_STRATEGIC_FILES_LOCATION
                + "competition_list_chd_daily_cause_list/"
                + "competitionListChdDailyCauseList.xlsx"
        );

        byte[] jsonData = getTestData(NON_STRATEGIC_FILES_LOCATION
                                          + "competition_list_chd_daily_cause_list/"
                                          + "competitionListChdDailyCauseList.json");
        when(blobClient.downloadContent()).thenReturn(BinaryData.fromBytes(jsonData));

        MvcResult response = mockMvc.perform(get(String.format(GET_ARTEFACT_SUMMARY, artefact.getArtefactId())))
            .andExpect(status().isOk()).andReturn();

        String responseContent = response.getResponse().getContentAsString();
        assertTrue(responseContent.contains(RB_TIME_FIELD), CONTENT_MISMATCH_ERROR);
        assertTrue(responseContent.contains(RB_CASE_NUMBER_FIELD), CONTENT_MISMATCH_ERROR);
        assertTrue(responseContent.contains(RB_CASE_NAME_FIELD), CONTENT_MISMATCH_ERROR);
    }

    @Test
    void testGenerateArtefactSummaryFinancialListChdKbDailyCauseList() throws Exception {
        Artefact artefact = createNonStrategicPublication(
            ListType.FINANCIAL_LIST_CHD_KB_DAILY_CAUSE_LIST, NON_STRATEGIC_FILES_LOCATION
                + "financial_list_chd_kb_daily_cause_list/"
                + "financialListChdKbDailyCauseList.xlsx"
        );

        byte[] jsonData = getTestData(NON_STRATEGIC_FILES_LOCATION
                                          + "financial_list_chd_kb_daily_cause_list/"
                                          + "financialListChdKbDailyCauseList.json");
        when(blobClient.downloadContent()).thenReturn(BinaryData.fromBytes(jsonData));

        MvcResult response = mockMvc.perform(get(String.format(GET_ARTEFACT_SUMMARY, artefact.getArtefactId())))
            .andExpect(status().isOk()).andReturn();

        String responseContent = response.getResponse().getContentAsString();
        assertTrue(responseContent.contains(RB_TIME_FIELD), CONTENT_MISMATCH_ERROR);
        assertTrue(responseContent.contains(RB_CASE_NUMBER_FIELD), CONTENT_MISMATCH_ERROR);
        assertTrue(responseContent.contains(RB_CASE_NAME_FIELD), CONTENT_MISMATCH_ERROR);
    }

    @Test
    void testGenerateArtefactSummaryInsolvencyCompaniesCourtChdDailyCauseList() throws Exception {
        Artefact artefact = createNonStrategicPublication(
            ListType.INSOLVENCY_AND_COMPANIES_COURT_CHD_DAILY_CAUSE_LIST, NON_STRATEGIC_FILES_LOCATION
                + "insolvency_and_companies_court_chd_daily_cause_list/"
                + "insolvencyAndCompaniesCourtChdDailyCauseList.xlsx"
        );

        byte[] jsonData = getTestData(NON_STRATEGIC_FILES_LOCATION
                                          + "insolvency_and_companies_court_chd_daily_cause_list/"
                                          + "insolvencyAndCompaniesCourtChdDailyCauseList.json");
        when(blobClient.downloadContent()).thenReturn(BinaryData.fromBytes(jsonData));

        MvcResult response = mockMvc.perform(get(String.format(GET_ARTEFACT_SUMMARY, artefact.getArtefactId())))
            .andExpect(status().isOk()).andReturn();

        String responseContent = response.getResponse().getContentAsString();
        assertTrue(responseContent.contains(RB_TIME_FIELD), CONTENT_MISMATCH_ERROR);
        assertTrue(responseContent.contains(RB_CASE_NUMBER_FIELD), CONTENT_MISMATCH_ERROR);
        assertTrue(responseContent.contains(RB_CASE_NAME_FIELD), CONTENT_MISMATCH_ERROR);
    }

    @Test
    void testGenerateArtefactBirminghamAdministrativeCourtDailyCauseList() throws Exception {
        Artefact artefact = createNonStrategicPublication(
            ListType.BIRMINGHAM_ADMINISTRATIVE_COURT_DAILY_CAUSE_LIST,
            NON_STRATEGIC_FILES_LOCATION
                + ADMINISTRATIVE_COURT_DAILY_CAUSE_LISTS_EXCEL_FILE
        );

        byte[] jsonData = getTestData(
            NON_STRATEGIC_FILES_LOCATION
                + ADMINISTRATIVE_COURT_DAILY_CAUSE_LISTS_JSON_FILE);
        when(blobClient.downloadContent()).thenReturn(BinaryData.fromBytes(jsonData));

        MvcResult response = mockMvc.perform(get(String.format(GET_ARTEFACT_SUMMARY, artefact.getArtefactId())))
            .andExpect(status().isOk()).andReturn();

        String responseContent = response.getResponse().getContentAsString();
        assertTrue(responseContent.contains(TIME_FIELD), CONTENT_MISMATCH_ERROR);
        assertTrue(responseContent.contains(CASE_NUMBER_FIELD), CONTENT_MISMATCH_ERROR);
        assertTrue(responseContent.contains(HEARING_TYPE_FIELD), CONTENT_MISMATCH_ERROR);
    }

    @Test
    void testGenerateArtefactBristolAndCardiffAdministrativeCourtDailyCauseList() throws Exception {
        Artefact artefact = createNonStrategicPublication(
            ListType.BRISTOL_AND_CARDIFF_ADMINISTRATIVE_COURT_DAILY_CAUSE_LIST,
            NON_STRATEGIC_FILES_LOCATION
                + ADMINISTRATIVE_COURT_DAILY_CAUSE_LISTS_EXCEL_FILE
        );

        byte[] jsonData = getTestData(
            NON_STRATEGIC_FILES_LOCATION
                + ADMINISTRATIVE_COURT_DAILY_CAUSE_LISTS_JSON_FILE);
        when(blobClient.downloadContent()).thenReturn(BinaryData.fromBytes(jsonData));

        MvcResult response = mockMvc.perform(get(String.format(GET_ARTEFACT_SUMMARY, artefact.getArtefactId())))
            .andExpect(status().isOk()).andReturn();

        String responseContent = response.getResponse().getContentAsString();
        assertTrue(responseContent.contains(TIME_FIELD), CONTENT_MISMATCH_ERROR);
        assertTrue(responseContent.contains(CASE_NUMBER_FIELD), CONTENT_MISMATCH_ERROR);
        assertTrue(responseContent.contains(HEARING_TYPE_FIELD), CONTENT_MISMATCH_ERROR);
    }

    @Test
    void testGenerateArtefactLeedsAdministrativeCourtDailyCauseList() throws Exception {
        Artefact artefact = createNonStrategicPublication(
            ListType.LEEDS_ADMINISTRATIVE_COURT_DAILY_CAUSE_LIST,
            NON_STRATEGIC_FILES_LOCATION
                + ADMINISTRATIVE_COURT_DAILY_CAUSE_LISTS_EXCEL_FILE
        );

        byte[] jsonData = getTestData(
            NON_STRATEGIC_FILES_LOCATION
                + ADMINISTRATIVE_COURT_DAILY_CAUSE_LISTS_JSON_FILE);
        when(blobClient.downloadContent()).thenReturn(BinaryData.fromBytes(jsonData));

        MvcResult response = mockMvc.perform(get(String.format(GET_ARTEFACT_SUMMARY, artefact.getArtefactId())))
            .andExpect(status().isOk()).andReturn();

        String responseContent = response.getResponse().getContentAsString();
        assertTrue(responseContent.contains(TIME_FIELD), CONTENT_MISMATCH_ERROR);
        assertTrue(responseContent.contains(CASE_NUMBER_FIELD), CONTENT_MISMATCH_ERROR);
        assertTrue(responseContent.contains(HEARING_TYPE_FIELD), CONTENT_MISMATCH_ERROR);
    }

    @Test
    void testGenerateArtefactManchesterAdministrativeCourtDailyCauseList() throws Exception {
        Artefact artefact = createNonStrategicPublication(
            ListType.MANCHESTER_ADMINISTRATIVE_COURT_DAILY_CAUSE_LIST,
            NON_STRATEGIC_FILES_LOCATION
                + ADMINISTRATIVE_COURT_DAILY_CAUSE_LISTS_EXCEL_FILE
        );

        byte[] jsonData = getTestData(
            NON_STRATEGIC_FILES_LOCATION
                + ADMINISTRATIVE_COURT_DAILY_CAUSE_LISTS_JSON_FILE);
        when(blobClient.downloadContent()).thenReturn(BinaryData.fromBytes(jsonData));

        MvcResult response = mockMvc.perform(get(String.format(GET_ARTEFACT_SUMMARY, artefact.getArtefactId())))
            .andExpect(status().isOk()).andReturn();

        String responseContent = response.getResponse().getContentAsString();
        assertTrue(responseContent.contains(TIME_FIELD), CONTENT_MISMATCH_ERROR);
        assertTrue(responseContent.contains(CASE_NUMBER_FIELD), CONTENT_MISMATCH_ERROR);
        assertTrue(responseContent.contains(HEARING_TYPE_FIELD), CONTENT_MISMATCH_ERROR);
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
        when(blobClient.downloadContent()).thenReturn(
            BinaryData.fromString(new String(file.getBytes())));

        MvcResult response = mockMvc.perform(
                get(ROOT_URL + "/" + ARTEFACT_ID + "/exists"))
            .andExpect(status().isOk()).andReturn();

        assertNotNull(
            response.getResponse().getContentAsString(),
            NULL_RESPONSE_ERROR
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
            NULL_RESPONSE_ERROR
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
                    .param(MAX_FILE_SIZE_HEADER, MAX_FILE_SIZE))

            .andExpect(status().isOk()).andReturn();

        assertNotNull(
            response.getResponse().getContentAsString(),
            NULL_RESPONSE_ERROR
        );
        byte[] decodedBytes = Base64.getDecoder().decode(response.getResponse().getContentAsString());
        String decodedResponse = new String(decodedBytes);

        assertTrue(
            decodedResponse.contains(TEST_CONTENT),
            UNEXPECTED_RESPONSE_ERROR
        );
    }

    @Test
    void testGetFileWithAuthorisedUser() throws Exception {

        when(accountManagementService.getIsAuthorised(
            UUID.fromString(USER_ID), ListType.SJP_PRESS_LIST, Sensitivity.CLASSIFIED
        )).thenReturn(true);
        when(blobClient.downloadContent()).thenReturn(BinaryData.fromString(new String(file.getBytes())));

        byte[] data = getTestData(SJP_PRESS_MOCK);
        Artefact artefact = createPublication(ListType.SJP_PRESS_LIST, Sensitivity.CLASSIFIED, data);
        MockHttpServletRequestBuilder request =
            get(String.format(GET_FILE_URL, artefact.getArtefactId(), PDF))
                .header(USER_ID_HEADER, USER_ID)
                .header(SYSTEM_HEADER, FALSE)
                .param(MAX_FILE_SIZE_HEADER, MAX_FILE_SIZE);

        MvcResult response = mockMvc.perform(request)
            .andExpect(status().isOk()).andReturn();

        assertNotNull(
            response.getResponse().getContentAsString(),
            "Null response"
        );
        byte[] decodedBytes = Base64.getDecoder().decode(response.getResponse().getContentAsString());
        String decodedResponse = new String(decodedBytes);

        assertTrue(
            decodedResponse.contains(TEST_CONTENT),
            UNEXPECTED_RESPONSE_ERROR
        );
    }

    @Test
    void testGetFileWithUnauthorisedUser() throws Exception {
        when(accountManagementService.getIsAuthorised(
            UUID.fromString(USER_ID), ListType.SJP_PRESS_LIST, Sensitivity.CLASSIFIED
        )).thenReturn(false);

        byte[] data = getTestData(SJP_PRESS_MOCK);
        Artefact artefact = createPublication(ListType.SJP_PRESS_LIST, Sensitivity.CLASSIFIED, data);
        MockHttpServletRequestBuilder request =
            get(String.format(GET_FILE_URL, artefact.getArtefactId(), PDF))
                .header(USER_ID_HEADER, USER_ID)
                .header(SYSTEM_HEADER, FALSE)
                .param(MAX_FILE_SIZE_HEADER, MAX_FILE_SIZE);

        MvcResult response = mockMvc.perform(request)
            .andExpect(status().isUnauthorized()).andReturn();

        assertTrue(
            response.getResponse().getContentAsString().contains("not authorised to access artefact with id "
                                                                     + artefact.getArtefactId()),
            "Response does not match"
        );
    }

    @Test
    void testGetFileSizeTooLarge() throws Exception {
        UUID artefactId = createSjpPublicListPublication().getArtefactId();

        when(blobClient.downloadContent()).thenReturn(
            BinaryData.fromString(new String(file.getBytes())));

        MockHttpServletRequestBuilder request =
            get(String.format(GET_FILE_URL, artefactId, PDF))
                .header(USER_ID_HEADER, USER_ID)
                .header(SYSTEM_HEADER, FALSE)
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
            UNEXPECTED_RESPONSE_ERROR
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
        mockMvc.perform(get(String.format(GET_FILE_URL, ARTEFACT_ID, PDF)))
            .andExpect(status().isForbidden());
    }

    @Test
    void testGenerateNoExcelWhenFileTooBig() throws Exception {
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
