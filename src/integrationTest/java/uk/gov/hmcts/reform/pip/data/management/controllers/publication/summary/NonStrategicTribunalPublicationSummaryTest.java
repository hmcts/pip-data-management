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
class NonStrategicTribunalPublicationSummaryTest extends PublicationIntegrationTestBase {
    private static final String ROOT_URL = "/publication";
    private static final String GET_ARTEFACT_SUMMARY = ROOT_URL + "/%s/summary";
    private static final String CONTENT_MISMATCH_ERROR = "Artefact summary content should match";
    private static final String CASE_NAME_FIELD = "Case name - This is a case name";
    private static final String HEARING_TYPE_FIELD = "Hearing type - Directions";
    private static final String DATE_FIELD = "Date - 16 December 2024";
    private static final String HEARING_TIME_FIELD = "Hearing time - 10am";
    private static final String IAC_HEARING_TIME_FIELD = "Hearing time - 10:30am";
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
    void testGenerateArtefactSummaryCicWeeklyHearingList() throws Exception {
        Artefact artefact = createNonStrategicPublication(
            ListType.CIC_WEEKLY_HEARING_LIST, NON_STRATEGIC_FILES_LOCATION
                + "cic-weekly-hearing-list/cicWeeklyHearingList.xlsx"
        );

        byte[] jsonData = getTestData(NON_STRATEGIC_FILES_LOCATION
                                          + "cic-weekly-hearing-list/cicWeeklyHearingList.json");
        when(blobClient.downloadContent()).thenReturn(BinaryData.fromBytes(jsonData));

        MvcResult response = mockMvc.perform(get(String.format(GET_ARTEFACT_SUMMARY, artefact.getArtefactId())))
            .andExpect(status().isOk()).andReturn();

        String responseContent = response.getResponse().getContentAsString();
        assertTrue(responseContent.contains("Date - 26 June 2025"), CONTENT_MISMATCH_ERROR);
        assertTrue(responseContent.contains(HEARING_TIME_FIELD), CONTENT_MISMATCH_ERROR);
        assertTrue(responseContent.contains(CASE_REFERENCE_NUMBER_FIELD), CONTENT_MISMATCH_ERROR);
        assertTrue(responseContent.contains(CASE_NAME_FIELD), CONTENT_MISMATCH_ERROR);
    }
}
