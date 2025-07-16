package uk.gov.hmcts.reform.pip.data.management.controllers.publication.summary;

import com.azure.core.util.BinaryData;
import io.zonky.test.db.AutoConfigureEmbeddedDatabase;
import org.apache.commons.math3.random.RandomDataGenerator;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import uk.gov.hmcts.reform.pip.data.management.Application;
import uk.gov.hmcts.reform.pip.data.management.config.PublicationConfiguration;
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

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = {Application.class}, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles({"integration", "disable-async"})
@AutoConfigureMockMvc
@AutoConfigureEmbeddedDatabase(type = AutoConfigureEmbeddedDatabase.DatabaseType.POSTGRES)
@WithMockUser(username = "admin", authorities = {"APPROLE_api.request.admin"})
@SuppressWarnings("PMD.TooManyMethods")
class NonStrategicRcjPublicationSummaryTest extends PublicationIntegrationTestBase {
    private static final String ROOT_URL = "/publication";
    private static final String GET_ARTEFACT_SUMMARY = ROOT_URL + "/%s/summary";
    private static final String CONTENT_MISMATCH_ERROR = "Artefact summary content should match";
    private static final String HEARING_TYPE_FIELD = "Hearing type - Directions";

    private static final String TIME_FIELD = "Time - 10am";
    private static final String RCJ_TIME_FIELD = "Time - 9am";
    private static final String RCJ_CASE_NUMBER_FIELD = "Case number - 12345";
    private static final String CASE_NUMBER_FIELD = "Case number - 1234";
    private static final String CASE_NAME_FIELD = "Case name - Case name A";
    private static final String CASE_DETAILS_FIELD = "Case details - Case details A";

    private static final String NON_STRATEGIC_FILES_LOCATION = "data/non-strategic/";
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

    private static final String EXCEL_FILE_TYPE = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";

    private byte[] getTestData(String resourceName) throws IOException {
        byte[] data;
        try (InputStream mockFile = this.getClass().getClassLoader()
            .getResourceAsStream(resourceName)) {
            data = mockFile.readAllBytes();
        }
        return data;
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
        assertTrue(responseContent.contains(CASE_DETAILS_FIELD), CONTENT_MISMATCH_ERROR);
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
        assertTrue(responseContent.contains(CASE_DETAILS_FIELD), CONTENT_MISMATCH_ERROR);
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
        assertTrue(responseContent.contains(CASE_DETAILS_FIELD), CONTENT_MISMATCH_ERROR);
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
        assertTrue(responseContent.contains(CASE_DETAILS_FIELD), CONTENT_MISMATCH_ERROR);
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
        assertTrue(responseContent.contains(CASE_DETAILS_FIELD), CONTENT_MISMATCH_ERROR);
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
        assertTrue(responseContent.contains(CASE_DETAILS_FIELD), CONTENT_MISMATCH_ERROR);
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
        assertTrue(responseContent.contains(CASE_DETAILS_FIELD), CONTENT_MISMATCH_ERROR);
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
        assertTrue(responseContent.contains(CASE_DETAILS_FIELD), CONTENT_MISMATCH_ERROR);
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
        assertTrue(responseContent.contains(CASE_DETAILS_FIELD), CONTENT_MISMATCH_ERROR);
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
        assertTrue(responseContent.contains(CASE_DETAILS_FIELD), CONTENT_MISMATCH_ERROR);
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

        assertTrue(responseContent.contains(RCJ_TIME_FIELD), CONTENT_MISMATCH_ERROR);
        assertTrue(responseContent.contains(RCJ_CASE_NUMBER_FIELD), CONTENT_MISMATCH_ERROR);
        assertTrue(responseContent.contains(CASE_NAME_FIELD), CONTENT_MISMATCH_ERROR);
    }

    @Test
    void testGenerateArtefactSummaryCourtOfAppealCivilDailyCauseList() throws Exception {
        Artefact artefact = createNonStrategicPublication(
            ListType.COURT_OF_APPEAL_CIVIL_DAILY_CAUSE_LIST, NON_STRATEGIC_FILES_LOCATION
                + "court-of-appeal-civil-daily-cause-list/courtOfAppealCivilDailyCauseList.xlsx"
        );

        byte[] jsonData = getTestData(NON_STRATEGIC_FILES_LOCATION
                                          + "court-of-appeal-civil-daily-cause-list/"
                                          + "courtOfAppealCivilDailyCauseList.json");

        when(blobClient.downloadContent()).thenReturn(BinaryData.fromBytes(jsonData));

        MvcResult response = mockMvc.perform(get(String.format(GET_ARTEFACT_SUMMARY, artefact.getArtefactId())))
            .andExpect(status().isOk()).andReturn();

        String responseContent = response.getResponse().getContentAsString();

        assertTrue(responseContent.contains(RCJ_TIME_FIELD), CONTENT_MISMATCH_ERROR);
        assertTrue(responseContent.contains(RCJ_CASE_NUMBER_FIELD), CONTENT_MISMATCH_ERROR);
        assertTrue(responseContent.contains(CASE_DETAILS_FIELD), CONTENT_MISMATCH_ERROR);
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
        assertTrue(responseContent.contains(RCJ_TIME_FIELD), CONTENT_MISMATCH_ERROR);
        assertTrue(responseContent.contains(RCJ_CASE_NUMBER_FIELD), CONTENT_MISMATCH_ERROR);
        assertTrue(responseContent.contains(CASE_NAME_FIELD), CONTENT_MISMATCH_ERROR);
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
        assertTrue(responseContent.contains(RCJ_TIME_FIELD), CONTENT_MISMATCH_ERROR);
        assertTrue(responseContent.contains(RCJ_CASE_NUMBER_FIELD), CONTENT_MISMATCH_ERROR);
        assertTrue(responseContent.contains(CASE_NAME_FIELD), CONTENT_MISMATCH_ERROR);
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
        assertTrue(responseContent.contains(RCJ_TIME_FIELD), CONTENT_MISMATCH_ERROR);
        assertTrue(responseContent.contains(RCJ_CASE_NUMBER_FIELD), CONTENT_MISMATCH_ERROR);
        assertTrue(responseContent.contains(CASE_NAME_FIELD), CONTENT_MISMATCH_ERROR);
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
        assertTrue(responseContent.contains(RCJ_TIME_FIELD), CONTENT_MISMATCH_ERROR);
        assertTrue(responseContent.contains(RCJ_CASE_NUMBER_FIELD), CONTENT_MISMATCH_ERROR);
        assertTrue(responseContent.contains(CASE_NAME_FIELD), CONTENT_MISMATCH_ERROR);
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
        assertTrue(responseContent.contains(RCJ_TIME_FIELD), CONTENT_MISMATCH_ERROR);
        assertTrue(responseContent.contains(RCJ_CASE_NUMBER_FIELD), CONTENT_MISMATCH_ERROR);
        assertTrue(responseContent.contains(CASE_NAME_FIELD), CONTENT_MISMATCH_ERROR);
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
        assertTrue(responseContent.contains(RCJ_TIME_FIELD), CONTENT_MISMATCH_ERROR);
        assertTrue(responseContent.contains(RCJ_CASE_NUMBER_FIELD), CONTENT_MISMATCH_ERROR);
        assertTrue(responseContent.contains(CASE_NAME_FIELD), CONTENT_MISMATCH_ERROR);
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
        assertTrue(responseContent.contains(RCJ_TIME_FIELD), CONTENT_MISMATCH_ERROR);
        assertTrue(responseContent.contains(RCJ_CASE_NUMBER_FIELD), CONTENT_MISMATCH_ERROR);
        assertTrue(responseContent.contains(CASE_NAME_FIELD), CONTENT_MISMATCH_ERROR);
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
        assertTrue(responseContent.contains(RCJ_TIME_FIELD), CONTENT_MISMATCH_ERROR);
        assertTrue(responseContent.contains(RCJ_CASE_NUMBER_FIELD), CONTENT_MISMATCH_ERROR);
        assertTrue(responseContent.contains(CASE_NAME_FIELD), CONTENT_MISMATCH_ERROR);
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
        assertTrue(responseContent.contains(RCJ_TIME_FIELD), CONTENT_MISMATCH_ERROR);
        assertTrue(responseContent.contains(RCJ_CASE_NUMBER_FIELD), CONTENT_MISMATCH_ERROR);
        assertTrue(responseContent.contains(CASE_NAME_FIELD), CONTENT_MISMATCH_ERROR);
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
        assertTrue(responseContent.contains(RCJ_TIME_FIELD), CONTENT_MISMATCH_ERROR);
        assertTrue(responseContent.contains(RCJ_CASE_NUMBER_FIELD), CONTENT_MISMATCH_ERROR);
        assertTrue(responseContent.contains(CASE_NAME_FIELD), CONTENT_MISMATCH_ERROR);
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
        assertTrue(responseContent.contains(RCJ_TIME_FIELD), CONTENT_MISMATCH_ERROR);
        assertTrue(responseContent.contains(RCJ_CASE_NUMBER_FIELD), CONTENT_MISMATCH_ERROR);
        assertTrue(responseContent.contains(CASE_NAME_FIELD), CONTENT_MISMATCH_ERROR);
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
        assertTrue(responseContent.contains(RCJ_TIME_FIELD), CONTENT_MISMATCH_ERROR);
        assertTrue(responseContent.contains(RCJ_CASE_NUMBER_FIELD), CONTENT_MISMATCH_ERROR);
        assertTrue(responseContent.contains(CASE_NAME_FIELD), CONTENT_MISMATCH_ERROR);
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
        assertTrue(responseContent.contains(RCJ_TIME_FIELD), CONTENT_MISMATCH_ERROR);
        assertTrue(responseContent.contains(RCJ_CASE_NUMBER_FIELD), CONTENT_MISMATCH_ERROR);
        assertTrue(responseContent.contains(CASE_NAME_FIELD), CONTENT_MISMATCH_ERROR);
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
        assertTrue(responseContent.contains(RCJ_TIME_FIELD), CONTENT_MISMATCH_ERROR);
        assertTrue(responseContent.contains(RCJ_CASE_NUMBER_FIELD), CONTENT_MISMATCH_ERROR);
        assertTrue(responseContent.contains(CASE_NAME_FIELD), CONTENT_MISMATCH_ERROR);
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
        assertTrue(responseContent.contains(RCJ_TIME_FIELD), CONTENT_MISMATCH_ERROR);
        assertTrue(responseContent.contains(RCJ_CASE_NUMBER_FIELD), CONTENT_MISMATCH_ERROR);
        assertTrue(responseContent.contains(CASE_NAME_FIELD), CONTENT_MISMATCH_ERROR);
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
        assertTrue(responseContent.contains(CASE_DETAILS_FIELD), CONTENT_MISMATCH_ERROR);
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
        assertTrue(responseContent.contains(CASE_DETAILS_FIELD), CONTENT_MISMATCH_ERROR);
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
        assertTrue(responseContent.contains(CASE_DETAILS_FIELD), CONTENT_MISMATCH_ERROR);
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
        assertTrue(responseContent.contains(CASE_DETAILS_FIELD), CONTENT_MISMATCH_ERROR);
    }
}
