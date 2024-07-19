package uk.gov.hmcts.reform.pip.data.management.controllers;

import com.azure.core.util.BinaryData;
import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.zonky.test.db.AutoConfigureEmbeddedDatabase;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
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
import uk.gov.hmcts.reform.pip.data.management.Application;
import uk.gov.hmcts.reform.pip.data.management.errorhandling.ExceptionResponse;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.stream.Stream;

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
@SuppressWarnings("PMD.TooManyMethods")
class PublicationManagementTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    BlobContainerClient blobContainerClient;

    @MockBean
    BlobClient blobClient;

    @Value("${TEST_USER_ID}")
    private String verifiedUserId;

    private static final String ROOT_URL = "/publication";
    private static final String GET_ARTEFACT_SUMMARY = ROOT_URL + "/summary";
    private static final String GET_FILE_URL = ROOT_URL + "/file";
    private static final String ARTEFACT_ID = "48732761-5ab5-482a-ad98-3aa91e4d5d5a";
    private static final String ARTEFACT_ID_NOT_FOUND = "11111111-1111-1111-1111-111111111111";
    private static final String INPUT_PARAMETERS = "parameters";
    private static final String ARTEFACT_NOT_FOUND_MESSAGE = "Artefact with id %s not found";
    private static final String NOT_FOUND_RESPONSE_MESSAGE = "Artefact not found message does not match";
    private static final String ARTEFACT_ID_CARE_STANDARDS_LIST = "877033f5-1435-461d-9f0b-eefaeb394b1b";
    private static final String ARTEFACT_ID_CIVIL_DAILY_CAUSE_LIST = "3b757c55-413b-4da5-ab88-1611a61b6252";
    private static final String ARTEFACT_ID_COP_DAILY_CAUSE_LIST = "9b5ea026-56fa-4b4e-a990-327ceec19841";
    private static final String ARTEFACT_ID_CROWN_DAILY_LIST = "8d798ae9-0770-48a3-a615-18363ec2af41";
    private static final String ARTEFACT_ID_CROWN_FIRM_LIST = "3b2f6c2d-e0ee-46f0-83cd-f048862e3e84";
    private static final String ARTEFACT_ID_CROWN_WARNED_LIST = "ca685a57-dc4c-404c-9966-bd92b0953bd7";
    private static final String ARTEFACT_ID_ET_DAILY_LIST = "026deb0e-c5ba-4c55-85ef-368e848712fa";
    private static final String ARTEFACT_ID_ET_FORTNIGHTLY_PRESS_LIST = "50fb5f6a-c08b-4ea8-821d-d7b2e6b36e13";
    private static final String ARTEFACT_ID_FAMILY_DAILY_CAUSE_LIST = "f0d3fd42-3a99-4bc1-bd24-975246a15422";
    private static final String ARTEFACT_ID_IAC_DAILY_LIST = "1fe1a207-dc73-4481-b018-b2f3718f50a2";
    private static final String ARTEFACT_ID_IAC_DAILY_LIST_ADDITIONAL_CASES = "2e7a6937-80b5-4e3e-b9f9-a97708001965";
    private static final String ARTEFACT_ID_MAGISTRATES_PUBLIC_LIST = "a46d2ae9-22ff-4707-a83d-708ef5264bc3";
    private static final String ARTEFACT_ID_MAGISTRATES_STANDARD_LIST = "d2a77de9-9af9-4256-ba05-ba9fad36745d";
    private static final String ARTEFACT_ID_PRIMARY_HEALTH_LIST = "295179be-2437-45a7-9e3f-691b964f9f65";
    private static final String ARTEFACT_ID_SJP_PRESS_LIST = "5dea6753-7a1d-4b91-b3c7-06721e3332cd";
    private static final String ARTEFACT_ID_SSCS_DAILY_LIST = "b412f2b9-db2c-4e6a-8e7f-56087a6829ed";
    private static final String ARTEFACT_ID_SSCS_DAILY_LIST_ADDITIONAL_HEARINGS
        = "2c55ce4a-6323-4e8b-aa98-1ddf5d824407";
    private static final String ARTEFACT_ID_OPA_PRESS_LIST = "c71bcbb7-08b1-4b2a-a0cd-5ae9137f855e";
    private static final String ARTEFACT_ID_OPA_PUBLIC_LIST = "4a4d7c2c-612b-451d-a150-d7224e956ee4";
    private static final String ARTEFACT_ID_OPA_RESULTS = "a6cbd858-2cb1-46a4-9052-34c3a2301772";
    private static final String ARTEFACT_ID_CIVIL_AND_FAMILY_DAILY_CAUSE_LIST_ENGLISH
        = "30304c47-942e-40aa-9134-35bb40386a0b";
    private static final String ARTEFACT_ID_SJP_PUBLIC_LIST_ENGLISH = "e4bdc96d-a438-4f04-8475-1da17733f453";
    private static final String ARTEFACT_ID_SJP_DELTA_PUBLIC_LIST = "f60f5568-0efd-4de3-b5aa-2e6418a9e878";
    private static final String CONTENT_MISMATCH_ERROR = "Artefact summary content should match";
    private static final String FILE_TYPE_HEADER = "x-file-type";
    private static final String UNAUTHORIZED_USERNAME = "unauthorized_username";
    private static final String UNAUTHORIZED_ROLE = "APPROLE_unknown.role";
    private static final String SYSTEM_HEADER = "x-system";
    private static final String CASE_REFERENCE_FIELD = "Case reference - 12341234";

    private static ObjectMapper objectMapper;
    private static MockMultipartFile file;

    @BeforeAll
    public static void setup() {
        file = new MockMultipartFile("file", "test.pdf",
                                     MediaType.APPLICATION_PDF_VALUE, "test content".getBytes(
            StandardCharsets.UTF_8)
        );

        objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();
    }

    @SuppressWarnings("PMD.UnusedPrivateMethod")
    private static Stream<Arguments> parameters() {
        return Stream.of(
            Arguments.of(ARTEFACT_ID_CARE_STANDARDS_LIST), //Care Standards Tribunal Hearing List
            Arguments.of(ARTEFACT_ID_CIVIL_AND_FAMILY_DAILY_CAUSE_LIST_ENGLISH), //Civil and Family Daily Cause List
            Arguments.of(ARTEFACT_ID_CIVIL_DAILY_CAUSE_LIST), //Civil Daily Cause List
            Arguments.of(ARTEFACT_ID_COP_DAILY_CAUSE_LIST), //Court of Protection Daily Cause List
            Arguments.of(ARTEFACT_ID_CROWN_DAILY_LIST), //Crown Daily List
            Arguments.of(ARTEFACT_ID_CROWN_FIRM_LIST), //Crown Firm List
            Arguments.of(ARTEFACT_ID_CROWN_WARNED_LIST), //Crown Warned List
            Arguments.of(ARTEFACT_ID_ET_DAILY_LIST), //Employment Tribunals Daily List
            Arguments.of(ARTEFACT_ID_ET_FORTNIGHTLY_PRESS_LIST), //Employment Tribunals Fortnightly Press List
            Arguments.of(ARTEFACT_ID_FAMILY_DAILY_CAUSE_LIST), //Family Daily Cause List
            Arguments.of(ARTEFACT_ID_IAC_DAILY_LIST), //Immigration and Asylum Chamber Daily List
            Arguments.of(ARTEFACT_ID_IAC_DAILY_LIST_ADDITIONAL_CASES),
            //Immigration and Asylum Chamber Daily List Additional Cases
            Arguments.of(ARTEFACT_ID_MAGISTRATES_PUBLIC_LIST), //Magistrates Public List
            Arguments.of(ARTEFACT_ID_MAGISTRATES_STANDARD_LIST), //Magistrates Standard List
            Arguments.of(ARTEFACT_ID_PRIMARY_HEALTH_LIST), //Primary Health Tribunal Hearing List
            Arguments.of(ARTEFACT_ID_SJP_PRESS_LIST), //Single Justice Procedure Press List
            Arguments.of(ARTEFACT_ID_SJP_PUBLIC_LIST_ENGLISH), //Single Justice Procedure Public List
            Arguments.of(ARTEFACT_ID_SJP_DELTA_PUBLIC_LIST), //Single Justice Procedure Delta Public List
            Arguments.of(ARTEFACT_ID_SSCS_DAILY_LIST), //SSCS Daily List
            Arguments.of(ARTEFACT_ID_SSCS_DAILY_LIST_ADDITIONAL_HEARINGS), //SSCS Daily List - Additional Hearings
            Arguments.of(ARTEFACT_ID_OPA_PRESS_LIST), //OPA Press List
            Arguments.of(ARTEFACT_ID_OPA_PUBLIC_LIST), //OPA Public List
            Arguments.of(ARTEFACT_ID_OPA_RESULTS) //OPA Results
        );
    }

    @Test
    void testGenerateArtefactSummaryCareStandardsList() throws Exception {
        MvcResult response = mockMvc.perform(
                get(GET_ARTEFACT_SUMMARY + "/" + ARTEFACT_ID_CARE_STANDARDS_LIST))
            .andExpect(status().isOk()).andReturn();
        String responseContent = response.getResponse().getContentAsString();

        assertTrue(responseContent.contains("Case name - A Vs B"), CONTENT_MISMATCH_ERROR);
        assertTrue(responseContent.contains("Hearing date - 04 October"), CONTENT_MISMATCH_ERROR);
        assertTrue(responseContent.contains("Hearing type - Remote - Teams"), CONTENT_MISMATCH_ERROR);
    }

    @Test
    void testGenerateArtefactSummaryCivilAndFamilyDailyCauseList() throws Exception {
        MvcResult response = mockMvc.perform(
                get(GET_ARTEFACT_SUMMARY + "/" + ARTEFACT_ID_CIVIL_AND_FAMILY_DAILY_CAUSE_LIST_ENGLISH))
            .andExpect(status().isOk()).andReturn();
        String responseContent = response.getResponse().getContentAsString();

        assertTrue(responseContent.contains("Applicant - Applicant org name"), CONTENT_MISMATCH_ERROR);
        assertTrue(responseContent.contains("Case reference - 12341235"), CONTENT_MISMATCH_ERROR);
        assertTrue(responseContent.contains("Case name - This is a case name 2"), CONTENT_MISMATCH_ERROR);
        assertTrue(responseContent.contains("Case type - normal"), CONTENT_MISMATCH_ERROR);
        assertTrue(responseContent.contains("Hearing type - Directions"), CONTENT_MISMATCH_ERROR);
    }

    @Test
    void testGenerateArtefactSummaryCivilDailyCauseList() throws Exception {
        MvcResult response = mockMvc.perform(
                get(GET_ARTEFACT_SUMMARY + "/" + ARTEFACT_ID_CIVIL_DAILY_CAUSE_LIST))
            .andExpect(status().isOk()).andReturn();

        String responseContent = response.getResponse().getContentAsString();
        assertTrue(responseContent.contains("Case reference - 112233445"), CONTENT_MISMATCH_ERROR);
        assertTrue(responseContent.contains("Case name - A2 Vs B2"), CONTENT_MISMATCH_ERROR);
        assertTrue(responseContent.contains("Case type - type"), CONTENT_MISMATCH_ERROR);
        assertTrue(responseContent.contains("Hearing type - FMPO"), CONTENT_MISMATCH_ERROR);
    }

    @Test
    void testGenerateArtefactSummaryCourtOfProtectionDailyCauseList() throws Exception {
        MvcResult response = mockMvc.perform(
                get(GET_ARTEFACT_SUMMARY + "/" + ARTEFACT_ID_COP_DAILY_CAUSE_LIST))
            .andExpect(status().isOk()).andReturn();

        String responseContent = response.getResponse().getContentAsString();
        assertTrue(responseContent.contains("Case reference - 12341235"), CONTENT_MISMATCH_ERROR);
        assertTrue(responseContent.contains("Case details - ThisIsACaseSuppressionName"), CONTENT_MISMATCH_ERROR);
        assertTrue(responseContent.contains("Hearing type - Criminal"), CONTENT_MISMATCH_ERROR);
    }

    @Test
    void testGenerateArtefactSummaryCrownDailyList() throws Exception {
        MvcResult response = mockMvc.perform(get(GET_ARTEFACT_SUMMARY + "/" + ARTEFACT_ID_CROWN_DAILY_LIST))
            .andExpect(status().isOk()).andReturn();

        String responseContent = response.getResponse().getContentAsString();
        assertTrue(responseContent.contains("Defendant - Surname 1, Forename 1"), CONTENT_MISMATCH_ERROR);
        assertTrue(responseContent.contains("Prosecutor - Pro_Auth"), CONTENT_MISMATCH_ERROR);
        assertTrue(responseContent.contains("Case reference - 12345678"), CONTENT_MISMATCH_ERROR);
        assertTrue(
            responseContent.contains("Hearing type - FHDRA1 (First Hearing and Dispute Resolution Appointment)"),
            CONTENT_MISMATCH_ERROR
        );
    }

    @Test
    void testGenerateArtefactSummaryCrownFirmList() throws Exception {
        MvcResult response = mockMvc.perform(get(GET_ARTEFACT_SUMMARY + "/" + ARTEFACT_ID_CROWN_FIRM_LIST))
            .andExpect(status().isOk()).andReturn();

        String responseContent = response.getResponse().getContentAsString();
        assertTrue(responseContent.contains("Defendant - Surname 2, Forename 2"), CONTENT_MISMATCH_ERROR);
        assertTrue(responseContent.contains("Prosecutor - Queen"), CONTENT_MISMATCH_ERROR);
        assertTrue(responseContent.contains("Case reference - I4Y416QE"), CONTENT_MISMATCH_ERROR);
        assertTrue(responseContent.contains("Directions"), CONTENT_MISMATCH_ERROR);
    }

    @Test
    void testGenerateArtefactSummaryCrownWarnedList() throws Exception {
        MvcResult response = mockMvc.perform(get(GET_ARTEFACT_SUMMARY + "/" + ARTEFACT_ID_CROWN_WARNED_LIST))
            .andExpect(status().isOk()).andReturn();

        String responseContent = response.getResponse().getContentAsString();
        assertTrue(responseContent.contains("Defendant - Surname 1, Forename 1"), CONTENT_MISMATCH_ERROR);
        assertTrue(responseContent.contains("Prosecutor - Prosecutor"), CONTENT_MISMATCH_ERROR);
        assertTrue(responseContent.contains("Case reference - 12345678"), CONTENT_MISMATCH_ERROR);
        assertTrue(responseContent.contains("Hearing date - 27/07/2022"), CONTENT_MISMATCH_ERROR);
    }

    @Test
    void testGenerateArtefactSummaryEmploymentTribunalsDailyList() throws Exception {
        MvcResult response = mockMvc.perform(get(GET_ARTEFACT_SUMMARY + "/" + ARTEFACT_ID_ET_DAILY_LIST))
            .andExpect(status().isOk()).andReturn();

        String responseContent = response.getResponse().getContentAsString();
        assertTrue(responseContent.contains("Claimant - Mr T Test Surname"), CONTENT_MISMATCH_ERROR);
        assertTrue(responseContent.contains("Respondent - Capt. T Test Surname 2"), CONTENT_MISMATCH_ERROR);
        assertTrue(responseContent.contains(CASE_REFERENCE_FIELD), CONTENT_MISMATCH_ERROR);
        assertTrue(responseContent.contains("Hearing type - This is a hearing type"), CONTENT_MISMATCH_ERROR);
    }

    @Test
    void testGenerateArtefactSummaryEmploymentTribunalsFortnightlyPressList() throws Exception {
        MvcResult response = mockMvc.perform(
                get(GET_ARTEFACT_SUMMARY + "/" + ARTEFACT_ID_ET_FORTNIGHTLY_PRESS_LIST))
            .andExpect(status().isOk()).andReturn();

        String responseContent = response.getResponse().getContentAsString();
        assertTrue(responseContent.contains("Claimant - Ms T Test"), CONTENT_MISMATCH_ERROR);
        assertTrue(responseContent.contains("Respondent - Lord T Test Surname"), CONTENT_MISMATCH_ERROR);
        assertTrue(responseContent.contains(CASE_REFERENCE_FIELD), CONTENT_MISMATCH_ERROR);
        assertTrue(responseContent.contains("Hearing type - Hearing Type 1"), CONTENT_MISMATCH_ERROR);
    }

    @Test
    void testGenerateArtefactSummaryFamilyDailyCauseList() throws Exception {
        MvcResult response = mockMvc.perform(
                get(GET_ARTEFACT_SUMMARY + "/" + ARTEFACT_ID_FAMILY_DAILY_CAUSE_LIST))
            .andExpect(status().isOk()).andReturn();

        String responseContent = response.getResponse().getContentAsString();
        assertTrue(responseContent.contains("Applicant - Applicant org name"), CONTENT_MISMATCH_ERROR);
        assertTrue(responseContent.contains("Case reference - 12341235"), CONTENT_MISMATCH_ERROR);
        assertTrue(responseContent.contains("Case name - This is a case name 2"), CONTENT_MISMATCH_ERROR);
        assertTrue(responseContent.contains("Case type - normal"), CONTENT_MISMATCH_ERROR);
        assertTrue(responseContent.contains("Hearing type - Directions"), CONTENT_MISMATCH_ERROR);
    }

    @Test
    void testGenerateArtefactSummaryImmigrationAndAsylumChamberDailyList() throws Exception {
        MvcResult response = mockMvc.perform(get(GET_ARTEFACT_SUMMARY + "/" + ARTEFACT_ID_IAC_DAILY_LIST))
            .andExpect(status().isOk()).andReturn();

        String responseContent = response.getResponse().getContentAsString();
        assertTrue(responseContent.contains("Bail list"), CONTENT_MISMATCH_ERROR);
        assertTrue(
            responseContent.contains("Appellant - Mr Individual Forenames Individual Middlename Individual Surname"),
            CONTENT_MISMATCH_ERROR
        );
        assertTrue(responseContent.contains("Prosecuting authority - Test Name"), CONTENT_MISMATCH_ERROR);
        assertTrue(responseContent.contains(CASE_REFERENCE_FIELD), CONTENT_MISMATCH_ERROR);
    }

    @Test
    void testGenerateArtefactSummaryImmigrationAndAsylumChamberDailyListAdditionalCases() throws Exception {
        MvcResult response = mockMvc.perform(get(GET_ARTEFACT_SUMMARY + "/"
                                                     + ARTEFACT_ID_IAC_DAILY_LIST_ADDITIONAL_CASES))
            .andExpect(status().isOk()).andReturn();

        String responseContent = response.getResponse().getContentAsString();
        assertTrue(responseContent.contains("Bail list"), CONTENT_MISMATCH_ERROR);
        assertTrue(
            responseContent.contains("Appellant - Mr Individual Forenames Individual Middlename Individual Surname"),
            CONTENT_MISMATCH_ERROR
        );
        assertTrue(responseContent.contains("Prosecuting authority - Test Name"), CONTENT_MISMATCH_ERROR);
        assertTrue(responseContent.contains(CASE_REFERENCE_FIELD), CONTENT_MISMATCH_ERROR);
    }

    @Test
    void testGenerateArtefactSummaryMagistratesPublicList() throws Exception {
        MvcResult response = mockMvc.perform(
                get(GET_ARTEFACT_SUMMARY + "/" + ARTEFACT_ID_MAGISTRATES_PUBLIC_LIST))
            .andExpect(status().isOk()).andReturn();

        String responseContent = response.getResponse().getContentAsString();
        assertTrue(responseContent.contains("Defendant - Surname 3"), CONTENT_MISMATCH_ERROR);
        assertTrue(responseContent.contains("Prosecutor - PROSECUTING_AUTHORITY"), CONTENT_MISMATCH_ERROR);
        assertTrue(responseContent.contains("Case reference - 12345678"), CONTENT_MISMATCH_ERROR);
        assertTrue(
            responseContent.contains("Hearing type - FHDRA1 (First Hearing and Dispute Resolution Appointment)"),
            CONTENT_MISMATCH_ERROR
        );
    }

    @Test
    void testGenerateArtefactSummaryMagistratesStandardList() throws Exception {
        MvcResult response = mockMvc.perform(
                get(GET_ARTEFACT_SUMMARY + "/" + ARTEFACT_ID_MAGISTRATES_STANDARD_LIST))
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
        MvcResult response = mockMvc
            .perform(get(GET_ARTEFACT_SUMMARY + "/" + ARTEFACT_ID_PRIMARY_HEALTH_LIST))
            .andExpect(status().isOk()).andReturn();

        String responseContent = response.getResponse().getContentAsString();
        assertTrue(responseContent.contains("Case name - A Vs B"), CONTENT_MISMATCH_ERROR);
        assertTrue(responseContent.contains("Hearing date - 04 October"), CONTENT_MISMATCH_ERROR);
        assertTrue(responseContent.contains("Hearing type - Remote - Teams"), CONTENT_MISMATCH_ERROR);
    }

    @Test
    void testGenerateArtefactSummarySingleJusticeProcedurePressList() throws Exception {
        MvcResult response = mockMvc.perform(get(GET_ARTEFACT_SUMMARY + "/" + ARTEFACT_ID_SJP_PRESS_LIST))
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

    @Test
    void testGenerateArtefactSummarySingleJusticeProcedurePublicList() throws Exception {
        MvcResult response = mockMvc.perform(get(GET_ARTEFACT_SUMMARY + "/" + ARTEFACT_ID_SJP_PUBLIC_LIST_ENGLISH))
            .andExpect(status().isOk()).andReturn();

        String responseContent = response.getResponse().getContentAsString();
        assertTrue(responseContent.contains("Name - A This is a surname"), CONTENT_MISMATCH_ERROR);
        assertTrue(responseContent.contains("Prosecutor - This is a prosecutor organisation"), CONTENT_MISMATCH_ERROR);
        assertTrue(responseContent.contains("Postcode - A1"), CONTENT_MISMATCH_ERROR);
        assertTrue(responseContent.contains("Offence - This is an offence title, This is an offence title 2"),
                   CONTENT_MISMATCH_ERROR);
    }

    @Test
    void testGenerateArtefactSummarySingleJusticeProcedureDeltaPublicList() throws Exception {
        MvcResult response = mockMvc.perform(get(GET_ARTEFACT_SUMMARY + "/" + ARTEFACT_ID_SJP_DELTA_PUBLIC_LIST))
            .andExpect(status().isOk()).andReturn();

        String responseContent = response.getResponse().getContentAsString();
        assertTrue(responseContent.contains("Name - A This is a surname"), CONTENT_MISMATCH_ERROR);
        assertTrue(responseContent.contains("Prosecutor - This is a prosecutor organisation"), CONTENT_MISMATCH_ERROR);
        assertTrue(responseContent.contains("Postcode - A1"), CONTENT_MISMATCH_ERROR);
        assertTrue(responseContent.contains("Offence - This is an offence title, This is an offence title 2"),
                   CONTENT_MISMATCH_ERROR);
    }

    @Test
    void testGenerateArtefactSummarySscsDailyList() throws Exception {
        MvcResult response = mockMvc.perform(get(GET_ARTEFACT_SUMMARY + "/" + ARTEFACT_ID_SSCS_DAILY_LIST))
            .andExpect(status().isOk()).andReturn();

        String responseContent = response.getResponse().getContentAsString();
        assertTrue(responseContent.contains("Appellant - Surname"), CONTENT_MISMATCH_ERROR);
        assertTrue(responseContent.contains("Respondent - Respondent Organisation, Respondent Organisation 2"),
                   CONTENT_MISMATCH_ERROR);
        assertTrue(responseContent.contains("Appeal reference - 12341235"), CONTENT_MISMATCH_ERROR);
    }

    @Test
    void testGenerateArtefactSummarySscsDailyListAdditionalHearings() throws Exception {
        MvcResult response = mockMvc.perform(
                get(GET_ARTEFACT_SUMMARY + "/" + ARTEFACT_ID_SSCS_DAILY_LIST_ADDITIONAL_HEARINGS))
            .andExpect(status().isOk()).andReturn();

        String responseContent = response.getResponse().getContentAsString();
        assertTrue(responseContent.contains("Appellant - Surname"), CONTENT_MISMATCH_ERROR);
        assertTrue(responseContent.contains("Respondent - Respondent Organisation, Respondent Organisation 2"),
                   CONTENT_MISMATCH_ERROR);
        assertTrue(responseContent.contains("Appeal reference - 12341235"), CONTENT_MISMATCH_ERROR);
    }

    @Test
    void testGenerateArtefactSummaryOpaPressList() throws Exception {
        MvcResult response = mockMvc.perform(
                get(GET_ARTEFACT_SUMMARY + "/" + ARTEFACT_ID_OPA_PRESS_LIST))
            .andExpect(status().isOk()).andReturn();

        String responseContent = response.getResponse().getContentAsString();
        assertTrue(responseContent.contains("Defendant - Surname2, Forename2 MiddleName2"),
                   CONTENT_MISMATCH_ERROR);
        assertTrue(responseContent.contains("Prosecuting authority ref"), CONTENT_MISMATCH_ERROR);
        assertTrue(responseContent.contains("Postcode - BB1 1BB"), CONTENT_MISMATCH_ERROR);
        assertTrue(responseContent.contains("Case reference - URN8888"), CONTENT_MISMATCH_ERROR);
        assertTrue(responseContent.contains("Offence - Offence title 2"), CONTENT_MISMATCH_ERROR);
    }

    @Test
    void testGenerateArtefactSummaryOpaPublicList() throws Exception {
        MvcResult response = mockMvc.perform(
                get(GET_ARTEFACT_SUMMARY + "/" + ARTEFACT_ID_OPA_PUBLIC_LIST))
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
        MvcResult response = mockMvc.perform(
                get(GET_ARTEFACT_SUMMARY + "/" + ARTEFACT_ID_OPA_RESULTS))
            .andExpect(status().isOk()).andReturn();

        String responseContent = response.getResponse().getContentAsString();
        assertTrue(responseContent.contains("Defendant - Organisation name"), CONTENT_MISMATCH_ERROR);
        assertTrue(responseContent.contains("Case reference - URN5678"), CONTENT_MISMATCH_ERROR);
        assertTrue(responseContent.contains("Offence - Offence title 2"), CONTENT_MISMATCH_ERROR);
    }

    @Test
    void testGenerateArtefactSummaryNotFound() throws Exception {
        MvcResult mvcResult = mockMvc.perform(get(GET_ARTEFACT_SUMMARY + "/" + ARTEFACT_ID_NOT_FOUND))
            .andExpect(status().isNotFound()).andReturn();

        ExceptionResponse exceptionResponse = objectMapper.readValue(
            mvcResult.getResponse().getContentAsString(), ExceptionResponse.class);

        assertEquals(
            exceptionResponse.getMessage(),
            String.format(ARTEFACT_NOT_FOUND_MESSAGE, ARTEFACT_ID_NOT_FOUND),
            NOT_FOUND_RESPONSE_MESSAGE
        );
    }

    @Test
    @WithMockUser(username = UNAUTHORIZED_USERNAME, authorities = {UNAUTHORIZED_ROLE})
    void testGenerateArtefactSummaryUnauthorized() throws Exception {
        mockMvc.perform(get(GET_ARTEFACT_SUMMARY + "/" + ARTEFACT_ID))
            .andExpect(status().isForbidden());
    }

    @Test
    void testGetFileExists() throws Exception {
        when(blobContainerClient.getBlobClient(any())).thenReturn(blobClient);
        when(blobClient.downloadContent()).thenReturn(
            BinaryData.fromString(new String(file.getBytes())));

        MvcResult response = mockMvc.perform(
                get(ROOT_URL + "/" + ARTEFACT_ID_SJP_PRESS_LIST + "/exists"))
            .andExpect(status().isOk()).andReturn();

        assertNotNull(
            response.getResponse().getContentAsString(),
            "Response should not be null"
        );
    }

    @Test
    void testGetFileSizes() throws Exception {
        when(blobContainerClient.getBlobClient(any())).thenReturn(blobClient);
        when(blobClient.downloadContent()).thenReturn(
            BinaryData.fromString(new String(file.getBytes())));

        MvcResult response = mockMvc.perform(
                get(ROOT_URL + "/" + ARTEFACT_ID_SJP_PRESS_LIST + "/sizes"))
            .andExpect(status().isOk()).andReturn();

        assertNotNull(
            response.getResponse().getContentAsString(),
            "Response should not be null"
        );
    }

    @ParameterizedTest
    @MethodSource(INPUT_PARAMETERS)
    void testGetFileOK(String listArtefactId) throws Exception {
        when(blobContainerClient.getBlobClient(any())).thenReturn(blobClient);
        when(blobClient.downloadContent()).thenReturn(
            BinaryData.fromString(new String(file.getBytes())));

        MvcResult response = mockMvc.perform(
                get(GET_FILE_URL + "/" + listArtefactId)
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

    @ParameterizedTest
    @MethodSource(INPUT_PARAMETERS)
    void testGetFileForUserId(String listArtefactId) throws Exception {
        when(blobContainerClient.getBlobClient(any())).thenReturn(blobClient);
        when(blobClient.downloadContent()).thenReturn(
            BinaryData.fromString(new String(file.getBytes())));

        MockHttpServletRequestBuilder request =
            get(GET_FILE_URL + "/" + listArtefactId)
                .header("x-user-id", verifiedUserId)
                .header(SYSTEM_HEADER, "false")
                .header(FILE_TYPE_HEADER, PDF)
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

    @ParameterizedTest
    @MethodSource(INPUT_PARAMETERS)
    void testGetFileSizeTooLarge(String listArtefactId) throws Exception {
        when(blobContainerClient.getBlobClient(any())).thenReturn(blobClient);
        when(blobClient.downloadContent()).thenReturn(
            BinaryData.fromString(new String(file.getBytes())));

        MockHttpServletRequestBuilder request =
            get(GET_FILE_URL + "/" + listArtefactId)
                .header("x-user-id", verifiedUserId)
                .header(SYSTEM_HEADER, "false")
                .header(FILE_TYPE_HEADER, PDF)
                .param("maxFileSize", "10");

        MvcResult response = mockMvc.perform(request)
            .andExpect(status().isPayloadTooLarge()).andReturn();

        assertNotNull(
            response.getResponse().getContentAsString(),
            "Null response"
        );

        assertTrue(
            response.getResponse().getContentAsString().contains("File with type PDF for artefact with id "
                                                         + listArtefactId + " has size over the limit of 10 bytes"),
            "Response does not contain expected result"
        );
    }

    @Test
    void testGetFileNotFound() throws Exception {
        MvcResult mvcResult = mockMvc.perform(get(GET_FILE_URL + "/" + ARTEFACT_ID_NOT_FOUND)
                                                  .header(FILE_TYPE_HEADER, PDF))
            .andExpect(status().isNotFound())
            .andReturn();

        ExceptionResponse exceptionResponse = objectMapper.readValue(
            mvcResult.getResponse().getContentAsString(), ExceptionResponse.class);

        assertEquals(
            exceptionResponse.getMessage(),
            String.format(ARTEFACT_NOT_FOUND_MESSAGE, ARTEFACT_ID_NOT_FOUND),
            NOT_FOUND_RESPONSE_MESSAGE
        );
    }

    @Test
    @WithMockUser(username = UNAUTHORIZED_USERNAME, authorities = {UNAUTHORIZED_ROLE})
    void testGetFileUnauthorized() throws Exception {
        mockMvc.perform(get(GET_FILE_URL + "/" + ARTEFACT_ID)
                            .header(FILE_TYPE_HEADER, PDF))
            .andExpect(status().isForbidden());
    }
}
