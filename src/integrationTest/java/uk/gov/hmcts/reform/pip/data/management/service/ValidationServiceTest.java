package uk.gov.hmcts.reform.pip.data.management.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.multipart.MultipartFile;
import uk.gov.hmcts.reform.pip.data.management.errorhandling.exceptions.ContainsForbiddenValuesException;
import uk.gov.hmcts.reform.pip.data.management.errorhandling.exceptions.FlatFileException;
import uk.gov.hmcts.reform.pip.data.management.errorhandling.exceptions.HeaderValidationException;
import uk.gov.hmcts.reform.pip.data.management.errorhandling.exceptions.PayloadValidationException;
import uk.gov.hmcts.reform.pip.data.management.models.publication.HeaderGroup;
import uk.gov.hmcts.reform.pip.data.management.utils.IntegrationBasicTestBase;
import uk.gov.hmcts.reform.pip.model.publication.ArtefactType;
import uk.gov.hmcts.reform.pip.model.publication.Language;
import uk.gov.hmcts.reform.pip.model.publication.ListType;
import uk.gov.hmcts.reform.pip.model.publication.Sensitivity;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;

@ActiveProfiles("integration-basic")
@SpringBootTest
@SuppressWarnings("PMD.TooManyMethods")
class ValidationServiceTest extends IntegrationBasicTestBase {

    @Autowired
    ValidationService validationService;

    private static final String SOURCE_ARTEFACT_ID = "sourceArtefactId";
    private static final LocalDateTime DISPLAY_FROM = LocalDateTime.now();
    private static final LocalDateTime DISPLAY_TO = LocalDateTime.now();
    private static final Language LANGUAGE = Language.ENGLISH;
    private static final String PROVENANCE = "provenance";
    private static final Sensitivity SENSITIVITY = Sensitivity.PUBLIC;
    private static final ArtefactType ARTEFACT_TYPE = ArtefactType.LIST;
    private static final String COURT_ID = "123";
    private static final String COURT_NAME = "Test Court Name";
    private static final String COURT_NAME_CONTAINS_HTML = "Test <p>Court Name</p>";
    private static final ListType LIST_TYPE = ListType.CIVIL_DAILY_CAUSE_LIST;
    private static final LocalDateTime CONTENT_DATE = LocalDateTime.now();
    private static final String VALIDATION_EXPECTED_MESSAGE =
        "The expected exception does not contain the correct message";
    private static final String NOT_NULL_MESSAGE = "The returned value is null, but was not expected to be.";
    private static final String UNKNOWN_EXCEPTION = "Unknown exception when opening the paylaod file";
    private static final String CONTAINS_FORBIDDEN_VALUES_EXCEPTION = "Input contains a html tag";
    private static final String NON_STRATEGIC_FOLDER = "data/non-strategic/";
    private static final String RPT_LIST_JSON = NON_STRATEGIC_FOLDER
        + "ftt-residential-property-tribunal-weekly-hearing-list/"
        + "fttResidentialPropertyTribunalWeeklyHearingList.json";
    private static final String SIAC_LIST_JSON = "data/non-strategic/siac-weekly-hearing-list/"
        + "siacWeeklyHearingList.json";
    private static final String UT_IAC_LIST_JSON = NON_STRATEGIC_FOLDER
        + "ut-iac-judicial-review-daily-hearing-list/"
        + "utIacJudicialReviewDailyHearingList.json";
    private static final String SSCS_LISTS_JSON_FILE = NON_STRATEGIC_FOLDER
        + "sscs-daily-hearing-list/"
        + "sscsDailyHearingList.json";
    private static final String ADMINISTRATIVE_COURT_DAILY_CAUSE_LISTS_JSON_FILE = NON_STRATEGIC_FOLDER
        + "administrative-court-daily-cause-list/administrativeCourtDailyCauseList.json";

    private HeaderGroup headerGroup;

    @BeforeEach
    void setup() {
        headerGroup = new HeaderGroup(PROVENANCE, SOURCE_ARTEFACT_ID, ARTEFACT_TYPE, SENSITIVITY, LANGUAGE,
                                      DISPLAY_FROM, DISPLAY_TO, LIST_TYPE, COURT_ID, CONTENT_DATE);
    }

    @Test
    void testCreationOfPublicationEmptyProvenance() {
        headerGroup.setProvenance("");

        HeaderValidationException dateHeaderValidationException =
            assertThrows(HeaderValidationException.class, () -> {
                validationService.validateHeaders(headerGroup);
            });
        assertEquals("x-provenance is mandatory however an empty value is provided",
                     dateHeaderValidationException.getMessage(), VALIDATION_EXPECTED_MESSAGE
        );
    }

    @Test
    void testCreationOfPublicationNullProvenance() {
        headerGroup.setProvenance(null);

        HeaderValidationException dateHeaderValidationException =
            assertThrows(HeaderValidationException.class, () -> {
                validationService.validateHeaders(headerGroup);
            });
        assertEquals("x-provenance is mandatory however an empty value is provided",
                     dateHeaderValidationException.getMessage(), VALIDATION_EXPECTED_MESSAGE
        );
    }

    @Test
    void testCreationOfPublicationEmptyCourtId() {
        headerGroup.setCourtId("");

        HeaderValidationException dateHeaderValidationException =
            assertThrows(HeaderValidationException.class, () -> {
                validationService.validateHeaders(headerGroup);
            });
        assertEquals("x-court-id is mandatory however an empty value is provided",
                     dateHeaderValidationException.getMessage(), VALIDATION_EXPECTED_MESSAGE
        );
    }

    @Test
    void testCreationOfPublicationNullCourtId() {
        headerGroup.setCourtId(null);

        HeaderValidationException dateHeaderValidationException =
            assertThrows(HeaderValidationException.class, () -> {
                validationService.validateHeaders(headerGroup);
            });
        assertEquals("x-court-id is mandatory however an empty value is provided",
                     dateHeaderValidationException.getMessage(), VALIDATION_EXPECTED_MESSAGE
        );
    }

    @Test
    void testCreationOfPublicationJudgementTypeAndEmptyDateTo() {
        headerGroup.setType(ArtefactType.JUDGEMENTS_AND_OUTCOMES);
        headerGroup.setDisplayTo(null);

        HeaderValidationException dateHeaderValidationException =
            assertThrows(HeaderValidationException.class, () -> {
                validationService.validateHeaders(headerGroup);
            });
        assertEquals("x-display-to Field is required for artefact type JUDGEMENTS_AND_OUTCOMES",
                     dateHeaderValidationException.getMessage(), VALIDATION_EXPECTED_MESSAGE
        );
    }

    @Test
    void testCreationOfPublicationListTypeAndEmptyDateTo() {
        headerGroup.setType(ArtefactType.LIST);
        headerGroup.setDisplayTo(null);

        HeaderValidationException dateHeaderValidationException =
            assertThrows(HeaderValidationException.class, () -> {
                validationService.validateHeaders(headerGroup);
            });
        assertEquals("x-display-to Field is required for artefact type LIST",
                     dateHeaderValidationException.getMessage(), VALIDATION_EXPECTED_MESSAGE
        );
    }

    @Test
    void testCreationOfPublicationListTypeAndEmptyDateFrom() {
        headerGroup.setDisplayFrom(null);

        HeaderValidationException dateHeaderValidationException =
            assertThrows(HeaderValidationException.class, () -> {
                validationService.validateHeaders(headerGroup);
            });
        assertEquals("x-display-from Field is required for artefact type LIST",
                     dateHeaderValidationException.getMessage(), VALIDATION_EXPECTED_MESSAGE
        );
    }

    @Test
    void testCreationOfPublicationJudgementAndOutcomeTypeAndEmptyDateFrom() {
        headerGroup.setType(ArtefactType.JUDGEMENTS_AND_OUTCOMES);
        headerGroup.setDisplayFrom(null);

        HeaderValidationException dateHeaderValidationException =
            assertThrows(HeaderValidationException.class, () -> {
                validationService.validateHeaders(headerGroup);
            });
        assertEquals("x-display-from Field is required for artefact type JUDGEMENTS_AND_OUTCOMES",
                     dateHeaderValidationException.getMessage(), VALIDATION_EXPECTED_MESSAGE
        );
    }

    @Test
    void testValidationOfGeneralPublicationTypeWithNoDateFrom() {
        headerGroup.setType(ArtefactType.GENERAL_PUBLICATION);
        headerGroup.setDisplayFrom(null);
        headerGroup.setDisplayTo(null);

        assertNotNull(
            validationService.validateHeaders(headerGroup).getDisplayFrom(),
            NOT_NULL_MESSAGE
        );
    }

    @Test
    void testValidationOfGeneralPublicationTypeWithNoDateTo() {
        headerGroup.setType(ArtefactType.GENERAL_PUBLICATION);
        headerGroup.setDisplayTo(null);

        assertNull(
            validationService.validateHeaders(headerGroup).getDisplayTo(),
            "The date to header should remain null"
        );
    }

    @Test
    void testDefaultSensitivity() {
        headerGroup.setSensitivity(null);

        assertEquals(SENSITIVITY, validationService.validateHeaders(headerGroup).getSensitivity(),
                     "Sensitivity should match");
    }

    @Test
    void testDefaultSensitivityIsNotOverwritten() {
        headerGroup.setSensitivity(Sensitivity.PRIVATE);

        assertEquals(Sensitivity.PRIVATE, validationService.validateHeaders(headerGroup).getSensitivity(),
                     "Sensitivity should match");
    }

    @Test
    void testEmptyFileThrows() {
        MultipartFile file = new MockMultipartFile("test", (byte[]) null);
        FlatFileException ex = assertThrows(FlatFileException.class, () -> {
            validationService.validateBody(file);
        });

        assertEquals("Empty file provided, please provide a valid file", ex.getMessage(),
                     VALIDATION_EXPECTED_MESSAGE);
    }

    @Test
    void testValidateMasterSchemaWithErrors() {
        try (InputStream jsonInput = this.getClass().getClassLoader()
            .getResourceAsStream("data/badJsonPayload.json")) {
            String text = new String(jsonInput.readAllBytes(), StandardCharsets.UTF_8);
            assertThrows(PayloadValidationException.class, () ->
                             validationService.validateBody(text, headerGroup, true),
                               "Valid JSON string marked as not valid");
        } catch (IOException exception) {
            fail(UNKNOWN_EXCEPTION);
        }
    }

    @Test
    void testExceptionWhenValidatingPayload() {
        PayloadValidationException payloadValidationException = assertThrows(PayloadValidationException.class, () ->
            validationService.validateBody("abcd", headerGroup, true), "Validation exception not thrown "
                                                                                 + "when value not JSON");

        assertEquals("Error while parsing JSON Payload", payloadValidationException.getMessage(),
                     "JSON Payload message does not match expected exception");

    }

    @ParameterizedTest
    @EnumSource(value = ListType.class, names = {"SJP_PRESS_REGISTER", "CIC_DAILY_HEARING_LIST"})
    void testValidateMasterSchemaWithoutErrors(ListType listType) throws IOException {
        try (InputStream jsonInput = this.getClass().getClassLoader()
            .getResourceAsStream("data/jsonPayload.json")) {
            headerGroup.setListType(listType);
            String text = new String(jsonInput.readAllBytes(), StandardCharsets.UTF_8);
            assertDoesNotThrow(() -> validationService.validateBody(text, headerGroup, true),
                               "Valid master schema marked as invalid");
        }
    }

    @ParameterizedTest
    @MethodSource("parameters")
    void testValidateWithoutErrorsForValidArtefact(ListType listType, String resource) throws IOException {
        try (InputStream jsonInput = this.getClass().getClassLoader().getResourceAsStream(resource)) {
            headerGroup.setListType(listType);
            String text = new String(jsonInput.readAllBytes(), StandardCharsets.UTF_8);

            assertDoesNotThrow(() -> validationService.validateBody(text, headerGroup, true),
                               String.format("Valid %s marked as invalid", listType));
        }
    }

    @ParameterizedTest
    @MethodSource("nonStrategicParameters")
    void testNonStrategicValidateWithoutErrorsForValidArtefact(ListType listType, String resource) throws IOException {
        try (InputStream jsonInput = this.getClass().getClassLoader().getResourceAsStream(resource)) {
            headerGroup.setListType(listType);
            String text = new String(jsonInput.readAllBytes(), StandardCharsets.UTF_8);

            assertDoesNotThrow(() -> validationService.validateBody(text, headerGroup, false),
                               String.format("Valid %s marked as invalid", listType));
        }
    }

    private static Stream<Arguments> parameters() {
        return Stream.of(
            Arguments.of(ListType.SJP_PUBLIC_LIST,
                         "data/sjp-public-list/sjpPublicList.json"),
            Arguments.of(ListType.SJP_DELTA_PUBLIC_LIST,
                         "data/sjp-public-list/sjpPublicList.json"),
            Arguments.of(ListType.SJP_PRESS_LIST,
                         "data/sjp-press-list/sjpPressList.json"),
            Arguments.of(ListType.SJP_DELTA_PRESS_LIST,
                         "data/sjp-press-list/sjpPressList.json"),
            Arguments.of(ListType.CROWN_DAILY_LIST,
                         "data/crown-daily-list/crownDailyList.json"),
            Arguments.of(ListType.CROWN_FIRM_LIST,
                         "data/crown-firm-list/crownFirmList.json"),
            Arguments.of(ListType.CROWN_WARNED_LIST,
                         "data/crown-warned-list/crownWarnedList.json"),
            Arguments.of(ListType.MAGISTRATES_PUBLIC_LIST,
                         "data/magistrates-public-list/magistratesPublicList.json"),
            Arguments.of(ListType.MAGISTRATES_STANDARD_LIST,
                         "data/magistrates-standard-list/magistratesStandardList.json"),
            Arguments.of(ListType.CIVIL_DAILY_CAUSE_LIST,
                         "data/civil-daily-cause-list/civilDailyCauseList.json"),
            Arguments.of(ListType.FAMILY_DAILY_CAUSE_LIST,
                         "data/family-daily-cause-list/familyDailyCauseList.json"),
            Arguments.of(ListType.CIVIL_AND_FAMILY_DAILY_CAUSE_LIST,
                         "data/civil-and-family-cause-list/civilAndFamilyDailyCauseList.json"),
            Arguments.of(ListType.COP_DAILY_CAUSE_LIST,
                         "data/cop-daily-cause-list/copDailyCauseList.json"),
            Arguments.of(ListType.ET_FORTNIGHTLY_PRESS_LIST,
                         "data/et-fortnightly-press-list/etFortnightlyPressList.json"),
            Arguments.of(ListType.ET_DAILY_LIST,
                         "data/et-daily-list/etDailyList.json"),
            Arguments.of(ListType.SSCS_DAILY_LIST,
                         "data/sscs-daily-list/sscsDailyList.json"),
            Arguments.of(ListType.SSCS_DAILY_LIST_ADDITIONAL_HEARINGS,
                         "data/sscs-daily-list/sscsDailyList.json"),
            Arguments.of(ListType.IAC_DAILY_LIST,
                         "data/iac-daily-list/iacDailyList.json"),
            Arguments.of(ListType.IAC_DAILY_LIST_ADDITIONAL_CASES,
                         "data/iac-daily-list/iacDailyList.json"),
            Arguments.of(ListType.CARE_STANDARDS_LIST,
                         "data/care-standards-list/careStandardsList.json"),
            Arguments.of(ListType.PRIMARY_HEALTH_LIST,
                         "data/primary-health-list/primaryHealthList.json")
        );
    }

    private static Stream<Arguments> nonStrategicParameters() {
        return Stream.of(
            Arguments.of(ListType.CST_WEEKLY_HEARING_LIST,
                         "data/non-strategic/cst-weekly-hearing-list/cstWeeklyHearingList.json"),
            Arguments.of(ListType.PHT_WEEKLY_HEARING_LIST,
                         "data/non-strategic/pht-weekly-hearing-list/phtWeeklyHearingList.json"),
            Arguments.of(ListType.GRC_WEEKLY_HEARING_LIST,
                         "data/non-strategic/grc-weekly-hearing-list/grcWeeklyHearingList.json"),
            Arguments.of(ListType.WPAFCC_WEEKLY_HEARING_LIST,
                         "data/non-strategic/wpafcc-weekly-hearing-list/wpafccWeeklyHearingList.json"),
            Arguments.of(ListType.UT_IAC_JR_LEEDS_DAILY_HEARING_LIST,
                         UT_IAC_LIST_JSON),
            Arguments.of(ListType.UT_IAC_JR_MANCHESTER_DAILY_HEARING_LIST,
                         UT_IAC_LIST_JSON),
            Arguments.of(ListType.UT_IAC_JR_BIRMINGHAM_DAILY_HEARING_LIST,
                         UT_IAC_LIST_JSON),
            Arguments.of(ListType.UT_IAC_JR_CARDIFF_DAILY_HEARING_LIST,
                         UT_IAC_LIST_JSON),
            Arguments.of(ListType.UT_IAC_JR_LONDON_DAILY_HEARING_LIST,
                          "data/non-strategic/ut-iac-judicial-review-london-daily-hearing-list/"
                              + "utIacJudicialReviewLondonDailyHearingList.json"),
            Arguments.of(ListType.UT_IAC_STATUTORY_APPEALS_DAILY_HEARING_LIST,
                         "data/non-strategic/ut-iac-statutory-appeals-daily-hearing-list/"
                             + "utIacStatutoryAppealsDailyHearingList.json"),
            Arguments.of(ListType.SIAC_WEEKLY_HEARING_LIST,
                         SIAC_LIST_JSON),
            Arguments.of(ListType.POAC_WEEKLY_HEARING_LIST,
                         SIAC_LIST_JSON),
            Arguments.of(ListType.PAAC_WEEKLY_HEARING_LIST,
                         SIAC_LIST_JSON),
            Arguments.of(ListType.FTT_TAX_WEEKLY_HEARING_LIST,
                         "data/non-strategic/ftt-tax-tribunal-weekly-hearing-list/"
                             + "fttTaxWeeklyHearingList.json"),
            Arguments.of(ListType.FTT_LR_WEEKLY_HEARING_LIST,
                         "data/non-strategic/ftt-land-registry-tribunal-weekly-hearing-list/"
                             + "fttLandRegistryTribunalWeeklyHearingList.json"),
            Arguments.of(ListType.RPT_EASTERN_WEEKLY_HEARING_LIST,
                         RPT_LIST_JSON),
            Arguments.of(ListType.RPT_LONDON_WEEKLY_HEARING_LIST,
                         RPT_LIST_JSON),
            Arguments.of(ListType.RPT_MIDLANDS_WEEKLY_HEARING_LIST,
                         RPT_LIST_JSON),
            Arguments.of(ListType.RPT_SOUTHERN_WEEKLY_HEARING_LIST,
                         RPT_LIST_JSON),
            Arguments.of(ListType.RPT_NORTHERN_WEEKLY_HEARING_LIST,
                         RPT_LIST_JSON),
            Arguments.of(ListType.UT_T_AND_CC_DAILY_HEARING_LIST,
                         "data/non-strategic/ut-tax-and-chancery-chamber-daily-hearing-list/"
                             + "utTaxAndChanceryChamberDailyHearingList.json"),
            Arguments.of(ListType.UT_LC_DAILY_HEARING_LIST,
                         "data/non-strategic/ut-lands-chamber-daily-hearing-list/"
                             + "utLandsChamberDailyHearingList.json"),
            Arguments.of(ListType.UT_AAC_DAILY_HEARING_LIST,
                         "data/non-strategic/ut-administrative-appeals-chamber-daily-hearing-list/"
                             + "utAdministrativeAppealsChamberDailyHearingList.json"),
            Arguments.of(ListType.AST_DAILY_HEARING_LIST,
                         "data/non-strategic/ast-daily-hearing-list/"
                             + "astDailyHearingList.json"),
            Arguments.of(ListType.SSCS_MIDLANDS_DAILY_HEARING_LIST,
                         SSCS_LISTS_JSON_FILE),
            Arguments.of(ListType.SSCS_SOUTH_EAST_DAILY_HEARING_LIST,
                         SSCS_LISTS_JSON_FILE),
            Arguments.of(ListType.SSCS_WALES_AND_SOUTH_WEST_DAILY_HEARING_LIST,
                         SSCS_LISTS_JSON_FILE),
            Arguments.of(ListType.SSCS_SCOTLAND_DAILY_HEARING_LIST,
                         SSCS_LISTS_JSON_FILE),
            Arguments.of(ListType.SSCS_NORTH_EAST_DAILY_HEARING_LIST,
                         SSCS_LISTS_JSON_FILE),
            Arguments.of(ListType.SSCS_NORTH_WEST_DAILY_HEARING_LIST,
                         SSCS_LISTS_JSON_FILE),
            Arguments.of(ListType.SSCS_LONDON_DAILY_HEARING_LIST,
                         SSCS_LISTS_JSON_FILE),
            Arguments.of(ListType.LONDON_ADMINISTRATIVE_COURT_DAILY_CAUSE_LIST,
                         "data/non-strategic/london-administrative-court-daily-cause-list/"
                             + "londonAdministrativeCourtDailyCauseList.json"),
            Arguments.of(ListType.PLANNING_COURT_DAILY_CAUSE_LIST,
                         "data/non-strategic/planning-court-daily-cause-list/planningCourtDailyCauseList.json"),
            Arguments.of(ListType.COUNTY_COURT_LONDON_CIVIL_DAILY_CAUSE_LIST,
                         "data/non-strategic/county-court-london-civil-daily-cause-list/"
                             + "countyCourtLondonCivilDailyCauseList.json"),
            Arguments.of(ListType.CIVIL_COURTS_RCJ_DAILY_CAUSE_LIST,
                         "data/non-strategic/civil-courts-rcj-daily-cause-list/civilCourtsRcjDailyCauseList.json"),
            Arguments.of(ListType.COURT_OF_APPEAL_CRIMINAL_DAILY_CAUSE_LIST,
                         "data/non-strategic/court-of-appeal-criminal-daily-cause-list/"
                             + "courtOfAppealCriminalDailyCauseList.json"),
            Arguments.of(ListType.FAMILY_DIVISION_HIGH_COURT_DAILY_CAUSE_LIST,
                         "data/non-strategic/family-division-high-court-daily-cause-list/"
                             + "familyDivisionHighCourtDailyCauseList.json"),
            Arguments.of(ListType.KINGS_BENCH_DIVISION_DAILY_CAUSE_LIST,
                         "data/non-strategic/kings-bench-division-daily-cause-list/"
                             + "kingsBenchDivisionDailyCauseList.json"),
            Arguments.of(ListType.KINGS_BENCH_MASTERS_DAILY_CAUSE_LIST,
                         "data/non-strategic/kings-bench-masters-daily-cause-list/"
                             + "kingsBenchMastersDailyCauseList.json"),
            Arguments.of(ListType.SENIOR_COURTS_COSTS_OFFICE_DAILY_CAUSE_LIST,
                         "data/non-strategic/senior-courts-costs-office-daily-cause-list/"
                             + "seniorCourtsCostsOfficeDailyCauseList.json"),
            Arguments.of(ListType.MAYOR_AND_CITY_CIVIL_DAILY_CAUSE_LIST,
                         "data/non-strategic/mayor-and-city-civil-daily-cause-list/"
                             + "mayorAndCityCivilDailyCauseList.json"),
            Arguments.of(ListType.INTERIM_APPLICATIONS_CHD_DAILY_CAUSE_LIST,
                         "data/non-strategic/interim-applications-chd-daily-cause-list/"
                             + "interimApplicationsChanceryDivisionDailyCauseList.json"),
            Arguments.of(ListType.COURT_OF_APPEAL_CIVIL_DAILY_CAUSE_LIST,
                         "data/non-strategic/court-of-appeal-civil-daily-cause-list/"
                             + "courtOfAppealCivilDailyCauseList.json"),
            Arguments.of(ListType.INTELLECTUAL_PROPERTY_AND_ENTERPRISE_COURT_DAILY_CAUSE_LIST,
                         "data/non-strategic/intellectual-property-and-enterprise-court-daily-cause-list/"
                             + "intellectualPropertyAndEnterpriseCourtDailyCauseList.json"),
            Arguments.of(ListType.INTELLECTUAL_PROPERTY_LIST_CHD_DAILY_CAUSE_LIST,
                         "data/non-strategic/intellectual-property-list-chd-daily-cause-list/"
                             + "intellectualPropertyListChdDailyCauseList.json"),
            Arguments.of(ListType.LONDON_CIRCUIT_COMMERCIAL_COURT_KB_DAILY_CAUSE_LIST,
                         "data/non-strategic/london-circuit-commercial-court-kb-daily-cause-list/"
                             + "londonCircuitCommercialCourtKbDailyCauseList.json"),
            Arguments.of(ListType.PATENTS_COURT_CHD_DAILY_CAUSE_LIST,
                         "data/non-strategic/patents-court-chd-daily-cause-list/patentsCourtChdDailyCauseList.json"),
            Arguments.of(ListType.PENSIONS_LIST_CHD_DAILY_CAUSE_LIST,
                         "data/non-strategic/pensions-list-chd-daily-cause-list/pensionsListChdDailyCauseList.json"),
            Arguments.of(ListType.PROPERTY_TRUSTS_PROBATE_LIST_CHD_DAILY_CAUSE_LIST,
                         "data/non-strategic/property-trusts-probate-list-chd-daily-cause-list/"
                             + "propertyTrustsProbateListChdDailyCauseList.json"),
            Arguments.of(ListType.REVENUE_LIST_CHD_DAILY_CAUSE_LIST,
                         "data/non-strategic/revenue-list-chd-daily-cause-list/revenueListChdDailyCauseList.json"),
            Arguments.of(ListType.TECHNOLOGY_AND_CONSTRUCTION_COURT_KB_DAILY_CAUSE_LIST,
                         "data/non-strategic/technology-and-construction-court-kb-daily-cause-list/"
                             + "technologyAndConstructionCourtKbDailyCauseList.json"),
            Arguments.of(ListType.ADMIRALTY_COURT_KB_DAILY_CAUSE_LIST,
                         "data/non-strategic/admiralty_court_kb_daily_cause_list/"
                             + "admiraltyCourtKbDailyCauseList.json"),
            Arguments.of(ListType.BUSINESS_LIST_CHD_DAILY_CAUSE_LIST,
                         "data/non-strategic/business_list_chd_daily_cause_list/"
                             + "businessListChdDailyCauseList.json"),
            Arguments.of(ListType.CHANCERY_APPEALS_CHD_DAILY_CAUSE_LIST,
                         "data/non-strategic/chancery_appeals_chd_daily_cause_list/"
                             + "chanceryAppealsChdDailyCauseList.json"),
            Arguments.of(ListType.COMMERCIAL_COURT_KB_DAILY_CAUSE_LIST,
                         "data/non-strategic/commercial_court_kb_daily_cause_list/"
                             + "commercialCourtKbDailyCauseList.json"),
            Arguments.of(ListType.COMPANIES_WINDING_UP_CHD_DAILY_CAUSE_LIST,
                         "data/non-strategic/companies_winding_up_chd_daily_cause_list/"
                             + "companiesWindingUpChdDailyCauseList.json"),
            Arguments.of(ListType.COMPETITION_LIST_CHD_DAILY_CAUSE_LIST,
                         "data/non-strategic/competition_list_chd_daily_cause_list/"
                             + "competitionListChdDailyCauseList.json"),
            Arguments.of(ListType.FINANCIAL_LIST_CHD_KB_DAILY_CAUSE_LIST,
                         "data/non-strategic/financial_list_chd_kb_daily_cause_list/"
                             + "financialListChdKbDailyCauseList.json"),
            Arguments.of(ListType.INSOLVENCY_AND_COMPANIES_COURT_CHD_DAILY_CAUSE_LIST,
                         "data/non-strategic/insolvency_and_companies_court_chd_daily_cause_list/"
                             + "insolvencyAndCompaniesCourtChdDailyCauseList.json"),
            Arguments.of(ListType.BIRMINGHAM_ADMINISTRATIVE_COURT_DAILY_CAUSE_LIST,
                         ADMINISTRATIVE_COURT_DAILY_CAUSE_LISTS_JSON_FILE),
            Arguments.of(ListType.BRISTOL_AND_CARDIFF_ADMINISTRATIVE_COURT_DAILY_CAUSE_LIST,
                         ADMINISTRATIVE_COURT_DAILY_CAUSE_LISTS_JSON_FILE),
            Arguments.of(ListType.MANCHESTER_ADMINISTRATIVE_COURT_DAILY_CAUSE_LIST,
                         ADMINISTRATIVE_COURT_DAILY_CAUSE_LISTS_JSON_FILE),
            Arguments.of(ListType.LEEDS_ADMINISTRATIVE_COURT_DAILY_CAUSE_LIST,
                         ADMINISTRATIVE_COURT_DAILY_CAUSE_LISTS_JSON_FILE),
            Arguments.of(ListType.CIC_WEEKLY_HEARING_LIST,
                         "data/non-strategic/cic-weekly-hearing-list/cicWeeklyHearingList.json")
        );
    }

    @Test
    void testContainsHtmlTagThrows() {
        assertThrows(ContainsForbiddenValuesException.class, () ->
                         validationService.containsHtmlTag(COURT_NAME_CONTAINS_HTML, COURT_NAME),
                     CONTAINS_FORBIDDEN_VALUES_EXCEPTION);
        assertThrows(ContainsForbiddenValuesException.class, () ->
                         validationService.containsHtmlTag(COURT_NAME, COURT_NAME_CONTAINS_HTML),
                     CONTAINS_FORBIDDEN_VALUES_EXCEPTION);
        assertThrows(ContainsForbiddenValuesException.class, () ->
                         validationService.containsHtmlTag(COURT_NAME_CONTAINS_HTML, COURT_NAME_CONTAINS_HTML),
                     CONTAINS_FORBIDDEN_VALUES_EXCEPTION);
    }

    @Test
    void testContainsHtmlTagDoesNotThrow() {
        assertDoesNotThrow(() -> validationService.containsHtmlTag(COURT_NAME, COURT_NAME),
                           CONTAINS_FORBIDDEN_VALUES_EXCEPTION);
    }
}
