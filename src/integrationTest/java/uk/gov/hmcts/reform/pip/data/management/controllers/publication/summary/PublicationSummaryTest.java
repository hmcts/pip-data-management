package uk.gov.hmcts.reform.pip.data.management.controllers.publication.summary;

import com.azure.core.util.BinaryData;
import io.zonky.test.db.AutoConfigureEmbeddedDatabase;
import org.apache.commons.math3.random.RandomDataGenerator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import uk.gov.hmcts.reform.pip.data.management.Application;
import uk.gov.hmcts.reform.pip.data.management.config.PublicationConfiguration;
import uk.gov.hmcts.reform.pip.data.management.errorhandling.ExceptionResponse;
import uk.gov.hmcts.reform.pip.data.management.models.publication.Artefact;
import uk.gov.hmcts.reform.pip.data.management.utils.PublicationIntegrationTestBase;
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
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = {Application.class}, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles({"integration", "disable-async"})
@AutoConfigureMockMvc
@AutoConfigureEmbeddedDatabase(type = AutoConfigureEmbeddedDatabase.DatabaseType.POSTGRES)
@WithMockUser(username = "admin", authorities = {"APPROLE_api.request.admin"})
class PublicationSummaryTest extends PublicationIntegrationTestBase {
    private static final String ROOT_URL = "/publication";
    private static final String GET_ARTEFACT_SUMMARY = ROOT_URL + "/%s/summary";
    private static final String ARTEFACT_ID = UUID.randomUUID().toString();
    private static final String ARTEFACT_ID_NOT_FOUND = UUID.randomUUID().toString();
    private static final String ARTEFACT_NOT_FOUND_MESSAGE = "No artefact found with the ID: ";
    private static final String NOT_FOUND_RESPONSE_MESSAGE = "Artefact not found message does not match";
    private static final String CONTENT_MISMATCH_ERROR = "Artefact summary content should match";
    private static final String UNAUTHORIZED_USERNAME = "unauthorized_username";
    private static final String UNAUTHORIZED_ROLE = "APPROLE_unknown.role";
    private static final String CASE_REFERENCE_FIELD = "Case reference - 12341234";
    private static final String CASE_NAME_FIELD = "Case name - This is a case name";
    private static final String HEARING_TYPE_FIELD = "Hearing type - Directions";

    private static final LocalDateTime DISPLAY_TO = LocalDateTime.now()
        .truncatedTo(ChronoUnit.SECONDS);
    private static final LocalDateTime DISPLAY_FROM = LocalDateTime.now().plusDays(1)
        .truncatedTo(ChronoUnit.SECONDS);
    private static final LocalDateTime CONTENT_DATE = LocalDateTime.now().toLocalDate().atStartOfDay()
        .truncatedTo(ChronoUnit.SECONDS);
    private static final String PROVENANCE = "MANUAL_UPLOAD";

    private static final String SJP_MOCK = "data/sjp-public-list/sjpPublicList.json";
    private static final String SJP_PRESS_MOCK = "data/sjp-press-list/sjpPressList.json";

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
}
