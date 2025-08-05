package uk.gov.hmcts.reform.pip.data.management.controllers.publication.summary;

import com.azure.core.util.BinaryData;
import io.zonky.test.db.AutoConfigureEmbeddedDatabase;
import org.apache.commons.math3.random.RandomDataGenerator;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
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
import java.util.stream.Stream;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
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
class NonStrategicRcjPublicationSummaryTestUsingAi extends PublicationIntegrationTestBase {

    private static final String ROOT_URL = "/publication";
    private static final String GET_ARTEFACT_SUMMARY = ROOT_URL + "/%s/summary";
    private static final String CONTENT_MISMATCH_ERROR = "Artefact summary content should match";

    private static final String NON_STRATEGIC_FILES_LOCATION = "data/non-strategic/";
    private static final LocalDateTime DISPLAY_TO = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
    private static final LocalDateTime DISPLAY_FROM = LocalDateTime.now().plusDays(1).truncatedTo(ChronoUnit.SECONDS);
    private static final LocalDateTime CONTENT_DATE = LocalDateTime.now().toLocalDate().atStartOfDay().truncatedTo(ChronoUnit.SECONDS);
    private static final String PROVENANCE = "MANUAL_UPLOAD";
    private static final String EXCEL_FILE_TYPE = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";

    private static Stream<TestCase> provideTestCases() {
        return Stream.of(
            new TestCase(ListType.LONDON_ADMINISTRATIVE_COURT_DAILY_CAUSE_LIST,
                         "london-administrative-court-daily-cause-list/londonAdministrativeCourtDailyCauseList.xlsx",
                         "london-administrative-court-daily-cause-list/londonAdministrativeCourtDailyCauseList.json",
                         new String[]{"Time - 9am", "Case number - 12345", "Case details - Case details A"}),

            new TestCase(ListType.COUNTY_COURT_LONDON_CIVIL_DAILY_CAUSE_LIST,
                         "county-court-london-civil-daily-cause-list/countyCourtLondonCivilDailyCauseList.xlsx",
                         "county-court-london-civil-daily-cause-list/countyCourtLondonCivilDailyCauseList.json",
                         new String[]{"Time - 9am", "Case number - 12345", "Case details - Case details A"}),

            new TestCase(ListType.CIVIL_COURTS_RCJ_DAILY_CAUSE_LIST,
                         "civil-courts-rcj-daily-cause-list/civilCourtsRcjDailyCauseList.xlsx",
                         "civil-courts-rcj-daily-cause-list/civilCourtsRcjDailyCauseList.json",
                         new String[]{"Time - 9am", "Case number - 12345", "Case details - Case details A"}),

            new TestCase(ListType.COURT_OF_APPEAL_CRIMINAL_DAILY_CAUSE_LIST,
                         "court-of-appeal-criminal-daily-cause-list/courtOfAppealCriminalDailyCauseList.xlsx",
                         "court-of-appeal-criminal-daily-cause-list/courtOfAppealCriminalDailyCauseList.json",
                         new String[]{"Time - 9am", "Case number - 12345", "Case details - Case details A"}),

            new TestCase(ListType.FAMILY_DIVISION_HIGH_COURT_DAILY_CAUSE_LIST,
                         "family-division-high-court-daily-cause-list/familyDivisionHighCourtDailyCauseList.xlsx",
                         "family-division-high-court-daily-cause-list/familyDivisionHighCourtDailyCauseList.json",
                         new String[]{"Time - 9am", "Case number - 12345", "Case details - Case details A"}),

            new TestCase(ListType.KINGS_BENCH_DIVISION_DAILY_CAUSE_LIST,
                         "kings-bench-division-daily-cause-list/kingsBenchDivisionDailyCauseList.xlsx",
                         "kings-bench-division-daily-cause-list/kingsBenchDivisionDailyCauseList.json",
                         new String[]{"Time - 9am", "Case number - 12345", "Case details - Case details A"}),

            new TestCase(ListType.KINGS_BENCH_MASTERS_DAILY_CAUSE_LIST,
                         "kings-bench-masters-daily-cause-list/kingsBenchMastersDailyCauseList.xlsx",
                         "kings-bench-masters-daily-cause-list/kingsBenchMastersDailyCauseList.json",
                         new String[]{"Time - 9am", "Case number - 12345", "Case details - Case details A"}),

            new TestCase(ListType.SENIOR_COURTS_COSTS_OFFICE_DAILY_CAUSE_LIST,
                         "senior-courts-costs-office-daily-cause-list/seniorCourtsCostsOfficeDailyCauseList.xlsx",
                         "senior-courts-costs-office-daily-cause-list/seniorCourtsCostsOfficeDailyCauseList.json",
                         new String[]{"Time - 9am", "Case number - 12345", "Case details - Case details A"}),

            new TestCase(ListType.MAYOR_AND_CITY_CIVIL_DAILY_CAUSE_LIST,
                         "mayor-and-city-civil-daily-cause-list/mayorAndCityCivilDailyCauseList.xlsx",
                         "mayor-and-city-civil-daily-cause-list/mayorAndCityCivilDailyCauseList.json",
                         new String[]{"Time - 9am", "Case number - 12345", "Case details - Case details A"}),

            new TestCase(ListType.INTELLECTUAL_PROPERTY_AND_ENTERPRISE_COURT_DAILY_CAUSE_LIST,
                         "intellectual-property-and-enterprise-court-daily-cause-list/intellectualPropertyAndEnterpriseCourtDailyCauseList.xlsx",
                         "intellectual-property-and-enterprise-court-daily-cause-list/intellectualPropertyAndEnterpriseCourtDailyCauseList.json",
                         new String[]{"Time - 9am", "Case number - 12345", "Case name - Case name A"}),

            new TestCase(ListType.COURT_OF_APPEAL_CIVIL_DAILY_CAUSE_LIST,
                         "court-of-appeal-civil-daily-cause-list/courtOfAppealCivilDailyCauseList.xlsx",
                         "court-of-appeal-civil-daily-cause-list/courtOfAppealCivilDailyCauseList.json",
                         new String[]{"Time - 9am", "Case number - 12345", "Case name - Case name A"}),

            new TestCase(ListType.INTELLECTUAL_PROPERTY_LIST_CHD_DAILY_CAUSE_LIST,
                         "intellectual-property-list-chd-daily-cause-list/intellectualPropertyListChdDailyCauseList.xlsx",
                         "intellectual-property-list-chd-daily-cause-list/intellectualPropertyListChdDailyCauseList.json",
                         new String[]{"Time - 9am", "Case number - 12345", "Case name - Case name A"}),

            new TestCase(ListType.LONDON_CIRCUIT_COMMERCIAL_COURT_KB_DAILY_CAUSE_LIST,
                         "london-circuit-commercial-court-kb-daily-cause-list/londonCircuitCommercialCourtKbDailyCauseList.xlsx",
                         "london-circuit-commercial-court-kb-daily-cause-list/londonCircuitCommercialCourtKbDailyCauseList.json",
                         new String[]{"Time - 9am", "Case number - 12345", "Case name - Case name A"}),

            new TestCase(ListType.PATENTS_COURT_CHD_DAILY_CAUSE_LIST,
                         "patents-court-chd-daily-cause-list/patentsCourtChdDailyCauseList.xlsx",
                         "patents-court-chd-daily-cause-list/patentsCourtChdDailyCauseList.json",
                         new String[]{"Time - 9am", "Case number - 12345", "Case name - Case name A"}),

            new TestCase(ListType.PENSIONS_LIST_CHD_DAILY_CAUSE_LIST,
                         "pensions-list-chd-daily-cause-list/pensionsListChdDailyCauseList.xlsx",
                         "pensions-list-chd-daily-cause-list/pensionsListChdDailyCauseList.json",
                         new String[]{"Time - 9am", "Case number - 12345", "Case name - Case name A"}),

            new TestCase(ListType.PROPERTY_TRUSTS_PROBATE_LIST_CHD_DAILY_CAUSE_LIST,
                         "property-trusts-probate-list-chd-daily-cause-list/propertyTrustsProbateListChdDailyCauseList.xlsx",
                         "property-trusts-probate-list-chd-daily-cause-list/propertyTrustsProbateListChdDailyCauseList.json",
                         new String[]{"Time - 9am", "Case number - 12345", "Case name - Case name A"}),

            new TestCase(ListType.REVENUE_LIST_CHD_DAILY_CAUSE_LIST,
                         "revenue-list-chd-daily-cause-list/revenueListChdDailyCauseList.xlsx",
                         "revenue-list-chd-daily-cause-list/revenueListChdDailyCauseList.json",
                         new String[]{"Time - 9am", "Case number - 12345", "Case name - Case name A"}),

            new TestCase(ListType.TECHNOLOGY_AND_CONSTRUCTION_COURT_KB_DAILY_CAUSE_LIST,
                         "technology-and-construction-court-kb-daily-cause-list/technologyAndConstructionCourtKbDailyCauseList.xlsx",
                         "technology-and-construction-court-kb-daily-cause-list/technologyAndConstructionCourtKbDailyCauseList.json",
                         new String[]{"Time - 9am", "Case number - 12345", "Case name - Case name A"}),

            new TestCase(ListType.ADMIRALTY_COURT_KB_DAILY_CAUSE_LIST,
                         "admiralty_court_kb_daily_cause_list/admiraltyCourtKbDailyCauseList.xlsx",
                         "admiralty_court_kb_daily_cause_list/admiraltyCourtKbDailyCauseList.json",
                         new String[]{"Time - 9am", "Case number - 12345", "Case name - Case name A"}),

            new TestCase(ListType.BUSINESS_LIST_CHD_DAILY_CAUSE_LIST,
                         "business_list_chd_daily_cause_list/businessListChdDailyCauseList.xlsx",
                         "business_list_chd_daily_cause_list/businessListChdDailyCauseList.json",
                         new String[]{"Time - 9am", "Case number - 12345", "Case name - Case name A"}),

            new TestCase(ListType.CHANCERY_APPEALS_CHD_DAILY_CAUSE_LIST,
                         "chancery_appeals_chd_daily_cause_list/chanceryAppealsChdDailyCauseList.xlsx",
                         "chancery_appeals_chd_daily_cause_list/chanceryAppealsChdDailyCauseList.json",
                         new String[]{"Time - 9am", "Case number - 12345", "Case name - Case name A"}),

            new TestCase(ListType.COMMERCIAL_COURT_KB_DAILY_CAUSE_LIST,
                         "commercial_court_kb_daily_cause_list/commercialCourtKbDailyCauseList.xlsx",
                         "commercial_court_kb_daily_cause_list/commercialCourtKbDailyCauseList.json",
                         new String[]{"Time - 9am", "Case number - 12345", "Case name - Case name A"}),

            new TestCase(ListType.COMPANIES_WINDING_UP_CHD_DAILY_CAUSE_LIST,
                         "companies_winding_up_chd_daily_cause_list/companiesWindingUpChdDailyCauseList.xlsx",
                         "companies_winding_up_chd_daily_cause_list/companiesWindingUpChdDailyCauseList.json",
                         new String[]{"Time - 9am", "Case number - 12345", "Case name - Case name A"}),

            new TestCase(ListType.COMPETITION_LIST_CHD_DAILY_CAUSE_LIST,
                         "competition_list_chd_daily_cause_list/competitionListChdDailyCauseList.xlsx",
                         "competition_list_chd_daily_cause_list/competitionListChdDailyCauseList.json",
                         new String[]{"Time - 9am", "Case number - 12345", "Case name - Case name A"}),

            new TestCase(ListType.FINANCIAL_LIST_CHD_KB_DAILY_CAUSE_LIST,
                         "financial_list_chd_kb_daily_cause_list/financialListChdKbDailyCauseList.xlsx",
                         "financial_list_chd_kb_daily_cause_list/financialListChdKbDailyCauseList.json",
                         new String[]{"Time - 9am", "Case number - 12345", "Case name - Case name A"}),

            new TestCase(ListType.INSOLVENCY_AND_COMPANIES_COURT_CHD_DAILY_CAUSE_LIST,
                         "insolvency_and_companies_court_chd_daily_cause_list/insolvencyAndCompaniesCourtChdDailyCauseList.xlsx",
                         "insolvency_and_companies_court_chd_daily_cause_list/insolvencyAndCompaniesCourtChdDailyCauseList.json",
                         new String[]{"Time - 9am", "Case number - 12345", "Case name - Case name A"}),

            new TestCase(ListType.BIRMINGHAM_ADMINISTRATIVE_COURT_DAILY_CAUSE_LIST,
                         "administrative-court-daily-cause-list/administrativeCourtDailyCauseList.xlsx",
                         "administrative-court-daily-cause-list/administrativeCourtDailyCauseList.json",
                         new String[]{"Time - 10am", "Case number - 1234", "Hearing type - Directions", "Case name - Case name A"}),

            new TestCase(ListType.BRISTOL_AND_CARDIFF_ADMINISTRATIVE_COURT_DAILY_CAUSE_LIST,
                         "administrative-court-daily-cause-list/administrativeCourtDailyCauseList.xlsx",
                         "administrative-court-daily-cause-list/administrativeCourtDailyCauseList.json",
                         new String[]{"Time - 10am", "Case number - 1234", "Hearing type - Directions", "Case name - Case name A"}),

            new TestCase(ListType.LEEDS_ADMINISTRATIVE_COURT_DAILY_CAUSE_LIST,
                         "administrative-court-daily-cause-list/administrativeCourtDailyCauseList.xlsx",
                         "administrative-court-daily-cause-list/administrativeCourtDailyCauseList.json",
                         new String[]{"Time - 10am", "Case number - 1234", "Hearing type - Directions", "Case name - Case name A"}),

            new TestCase(ListType.MANCHESTER_ADMINISTRATIVE_COURT_DAILY_CAUSE_LIST,
                         "administrative-court-daily-cause-list/administrativeCourtDailyCauseList.xlsx",
                         "administrative-court-daily-cause-list/administrativeCourtDailyCauseList.json",
                         new String[]{"Time - 10am", "Case number - 1234", "Hearing type - Directions", "Case name - Case name A"})
        );
    }

    @ParameterizedTest
    @MethodSource("provideTestCases")
    void testGenerateArtefactSummary(TestCase testCase) throws Exception {
        Artefact artefact = createNonStrategicPublication(
            testCase.listType,
            NON_STRATEGIC_FILES_LOCATION + testCase.excelFilePath
        );

        byte[] jsonData = getTestData(NON_STRATEGIC_FILES_LOCATION + testCase.jsonFilePath);
        when(blobClient.downloadContent()).thenReturn(BinaryData.fromBytes(jsonData));

        MvcResult response = mockMvc.perform(get(String.format(GET_ARTEFACT_SUMMARY, artefact.getArtefactId())))
            .andExpect(status().isOk()).andReturn();

        String responseContent = response.getResponse().getContentAsString();
        for (String expectedField : testCase.expectedFields) {
            assertThat(responseContent).contains(expectedField);
        }
    }

    private byte[] getTestData(String resourceName) throws IOException {
        try (InputStream mockFile = this.getClass().getClassLoader().getResourceAsStream(resourceName)) {
            return mockFile.readAllBytes();
        }
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

        return OBJECT_MAPPER.readValue(response.getResponse().getContentAsString(), Artefact.class);
    }

    private MockMultipartFile getMockMultipartFile(String filePath) throws IOException {
        try (InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream(filePath)) {
            return new MockMultipartFile(
                "file", "TestFileName.xlsx", EXCEL_FILE_TYPE,
                inputStream.readAllBytes()
            );
        }
    }

    private static class TestCase {
        private final ListType listType;
        private final String excelFilePath;
        private final String jsonFilePath;
        private final String[] expectedFields;

        TestCase(ListType listType, String excelFilePath, String jsonFilePath, String[] expectedFields) {
            this.listType = listType;
            this.excelFilePath = excelFilePath;
            this.jsonFilePath = jsonFilePath;
            this.expectedFields = expectedFields;
        }
    }
}
