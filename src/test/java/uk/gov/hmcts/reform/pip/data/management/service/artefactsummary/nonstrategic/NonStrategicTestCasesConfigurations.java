package uk.gov.hmcts.reform.pip.data.management.service.artefactsummary.nonstrategic;

import uk.gov.hmcts.reform.pip.model.publication.ListType;

import java.util.List;
import java.util.stream.Stream;

public final class NonStrategicTestCasesConfigurations {
    private static final String TIME_FIELD = "Time";
    private static final String CASE_NUMBER_FIELD = "Case number";
    private static final String CASE_NAME_FIELD = "Case name";
    private static final String CASE_DETAILS_FIELD = "Case details";
    private static final String HEARING_TIME_FIELD = "Hearing time";
    private static final String CASE_REFERENCE_NUMBER_FIELD = "Case reference number";
    private static final String DATE_FIELD = "Date";
    private static final String APPEAL_REFERENCE_NUMBER_FIELD = "Appeal reference number";
    private static final String HEARING_TYPE_FIELD = "Hearing type";
    private static final String DIRECTIONS_FIELD = "Directions";
    private static final String CASE_NAME_TEXT_1 = "This is a case name";
    private static final String CASE_NAME_TEXT_2 = "Case name A";
    private static final String CASE_DETAILS_TEXT = "Case details A";
    private static final String DIRECTIONS_TEXT = "Directions A";
    private static final String APPELLANT_TEXT = "Appellant A";
    private static final String VENUE_TEXT = "Venue A";
    private static final String CASE_NUMBER_TEXT = "12345";
    private static final String TIME_TEXT_1 = "9am";
    private static final String TIME_TEXT_2 = "10:30am";
    private static final String TIME_TEXT_3 = "10am";
    private static final String TIME_TEXT_4 = "10:15am";
    private static final String CASE_REFERENCE_NUMBER_TEXT_1 = "1234";
    private static final String CASE_REFERENCE_NUMBER_TEXT_2 = "1234567";
    private static final String DATE_TEXT = "16 December 2024";

    private static final String ADMINISTRATIVE_COURT_DAILY_CAUSE_LIST_JSON =
        "administrativeCourtDailyCauseList.json";
    private static final String FTT_RP_WEEKLY_HEARING_LIST_JSON =
        "fttResidentialPropertyTribunalWeeklyHearingList.json";
    private static final String SSCS_DAILY_HEARING_LIST_JSON =
        "sscsDailyHearingList.json";
    private static final String UT_IAC_JR_DAILY_HEARING_LIST_JSON =
        "utIacJudicialReviewDailyHearingList.json";

    public static Stream<ListTestCaseSettings> provideTestCases() {
        return Stream.of(
            new ListTestCaseSettings(
                "admiraltyCourtKbDailyCauseList.json",
                ListType.ADMIRALTY_COURT_KB_DAILY_CAUSE_LIST,
                1,
                2,
                3,
                List.of(TIME_FIELD, CASE_NUMBER_FIELD, CASE_NAME_FIELD),
                List.of(TIME_TEXT_1, CASE_NUMBER_TEXT, CASE_NAME_TEXT_2)
            ),
            new ListTestCaseSettings(
                "astDailyHearingList.json",
                ListType.AST_DAILY_HEARING_LIST,
                1,
                2,
                3,
                List.of("Appellant", APPEAL_REFERENCE_NUMBER_FIELD, HEARING_TIME_FIELD),
                List.of(APPELLANT_TEXT, CASE_NUMBER_TEXT, TIME_TEXT_2)
            ),
            new ListTestCaseSettings(
                ADMINISTRATIVE_COURT_DAILY_CAUSE_LIST_JSON,
                ListType.BIRMINGHAM_ADMINISTRATIVE_COURT_DAILY_CAUSE_LIST,
                1,
                2,
                4,
                List.of(TIME_FIELD, CASE_NUMBER_FIELD, HEARING_TYPE_FIELD, CASE_DETAILS_FIELD),
                List.of(TIME_TEXT_3, CASE_NUMBER_TEXT, DIRECTIONS_TEXT, CASE_DETAILS_TEXT)
            ),
            new ListTestCaseSettings(
                ADMINISTRATIVE_COURT_DAILY_CAUSE_LIST_JSON,
                ListType.BRISTOL_AND_CARDIFF_ADMINISTRATIVE_COURT_DAILY_CAUSE_LIST,
                1,
                2,
                4,
                List.of(TIME_FIELD, CASE_NUMBER_FIELD, HEARING_TYPE_FIELD, CASE_DETAILS_FIELD),
                List.of(TIME_TEXT_3, CASE_NUMBER_TEXT, DIRECTIONS_TEXT, CASE_DETAILS_TEXT)
            ),
            new ListTestCaseSettings(
                "businessListChdDailyCauseList.json",
                ListType.BUSINESS_LIST_CHD_DAILY_CAUSE_LIST,
                1,
                2,
                3,
                List.of(TIME_FIELD, CASE_NUMBER_FIELD, CASE_NAME_FIELD),
                List.of(TIME_TEXT_1, CASE_NUMBER_TEXT, CASE_NAME_TEXT_2)
            ),
            new ListTestCaseSettings(
                "chanceryAppealsChdDailyCauseList.json",
                ListType.CHANCERY_APPEALS_CHD_DAILY_CAUSE_LIST,
                1,
                2,
                3,
                List.of(TIME_FIELD, CASE_NUMBER_FIELD, CASE_NAME_FIELD),
                List.of(TIME_TEXT_1, CASE_NUMBER_TEXT, CASE_NAME_TEXT_2)
            ),
            new ListTestCaseSettings(
                "cicWeeklyHearingList.json",
                ListType.CIC_WEEKLY_HEARING_LIST,
                1,
                2,
                4,
                List.of(DATE_FIELD, HEARING_TIME_FIELD, CASE_REFERENCE_NUMBER_FIELD, CASE_NAME_FIELD),
                List.of("26 June 2025", TIME_TEXT_3, CASE_REFERENCE_NUMBER_TEXT_1, CASE_NAME_TEXT_1)
            ),
            new ListTestCaseSettings(
                "civilCourtsRcjDailyCauseList.json",
                ListType.CIVIL_COURTS_RCJ_DAILY_CAUSE_LIST,
                1,
                2,
                3,
                List.of(TIME_FIELD, CASE_NUMBER_FIELD, CASE_DETAILS_FIELD),
                List.of(TIME_TEXT_1, CASE_NUMBER_TEXT, CASE_DETAILS_TEXT)
            ),
            new ListTestCaseSettings(
                "commercialCourtKbDailyCauseList.json",
                ListType.COMMERCIAL_COURT_KB_DAILY_CAUSE_LIST,
                1,
                2,
                3,
                List.of(TIME_FIELD, CASE_NUMBER_FIELD, CASE_NAME_FIELD),
                List.of(TIME_TEXT_1, CASE_NUMBER_TEXT, CASE_NAME_TEXT_2)
            ),
            new ListTestCaseSettings(
                "companiesWindingUpChdDailyCauseList.json",
                ListType.COMPANIES_WINDING_UP_CHD_DAILY_CAUSE_LIST,
                1,
                2,
                3,
                List.of(TIME_FIELD, CASE_NUMBER_FIELD, CASE_NAME_FIELD),
                List.of(TIME_TEXT_1, CASE_NUMBER_TEXT, CASE_NAME_TEXT_2)
            ),
            new ListTestCaseSettings(
                "competitionListChdDailyCauseList.json",
                ListType.COMPETITION_LIST_CHD_DAILY_CAUSE_LIST,
                1,
                2,
                3,
                List.of(TIME_FIELD, CASE_NUMBER_FIELD, CASE_NAME_FIELD),
                List.of(TIME_TEXT_1, CASE_NUMBER_TEXT, CASE_NAME_TEXT_2)
            ),
            new ListTestCaseSettings(
                "countyCourtLondonCivilDailyCauseList.json",
                ListType.COUNTY_COURT_LONDON_CIVIL_DAILY_CAUSE_LIST,
                1,
                2,
                3,
                List.of(TIME_FIELD, CASE_NUMBER_FIELD, CASE_DETAILS_FIELD),
                List.of(TIME_TEXT_1, CASE_NUMBER_TEXT, CASE_DETAILS_TEXT)
            ),
            new ListTestCaseSettings(
                "courtOfAppealCivilDailyCauseList.json",
                ListType.COURT_OF_APPEAL_CIVIL_DAILY_CAUSE_LIST,
                1,
                2,
                3,
                List.of(TIME_FIELD, CASE_NUMBER_FIELD, CASE_DETAILS_FIELD),
                List.of(TIME_TEXT_1, CASE_NUMBER_TEXT, CASE_DETAILS_TEXT)
            ),
            new ListTestCaseSettings(
                "courtOfAppealCriminalDailyCauseList.json",
                ListType.COURT_OF_APPEAL_CRIMINAL_DAILY_CAUSE_LIST,
                1,
                2,
                3,
                List.of(TIME_FIELD, CASE_NUMBER_FIELD, CASE_DETAILS_FIELD),
                List.of(TIME_TEXT_1, CASE_NUMBER_TEXT, CASE_DETAILS_TEXT)
            ),
            new ListTestCaseSettings(
                "cstWeeklyHearingList.json",
                ListType.CST_WEEKLY_HEARING_LIST,
                1,
                2,
                2,
                List.of(DATE_FIELD, CASE_NAME_FIELD),
                List.of("10 December 2024", CASE_NAME_TEXT_1)
            ),
            new ListTestCaseSettings(
                "familyDivisionHighCourtDailyCauseList.json",
                ListType.FAMILY_DIVISION_HIGH_COURT_DAILY_CAUSE_LIST,
                1,
                2,
                3,
                List.of(TIME_FIELD, CASE_NUMBER_FIELD, CASE_DETAILS_FIELD),
                List.of(TIME_TEXT_1, CASE_NUMBER_TEXT, CASE_DETAILS_TEXT)
            ),
            new ListTestCaseSettings(
                "financialListChdKbDailyCauseList.json",
                ListType.FINANCIAL_LIST_CHD_KB_DAILY_CAUSE_LIST,
                1,
                2,
                3,
                List.of(TIME_FIELD, CASE_NUMBER_FIELD, CASE_NAME_FIELD),
                List.of(TIME_TEXT_1, CASE_NUMBER_TEXT, CASE_NAME_TEXT_2)
            ),
            new ListTestCaseSettings(
                "fttLandRegistryTribunalWeeklyHearingList.json",
                ListType.FTT_LR_WEEKLY_HEARING_LIST,
                1,
                2,
                3,
                List.of(DATE_FIELD, HEARING_TIME_FIELD, CASE_REFERENCE_NUMBER_FIELD),
                List.of(DATE_TEXT, TIME_TEXT_4, CASE_REFERENCE_NUMBER_TEXT_1)
            ),
            new ListTestCaseSettings(
                FTT_RP_WEEKLY_HEARING_LIST_JSON,
                ListType.RPT_EASTERN_WEEKLY_HEARING_LIST,
                1,
                2,
                3,
                List.of(DATE_FIELD, TIME_FIELD, CASE_REFERENCE_NUMBER_FIELD),
                List.of(DATE_TEXT, TIME_TEXT_4, CASE_REFERENCE_NUMBER_TEXT_1)
            ),
            new ListTestCaseSettings(
                FTT_RP_WEEKLY_HEARING_LIST_JSON,
                ListType.RPT_LONDON_WEEKLY_HEARING_LIST,
                1,
                2,
                3,
                List.of(DATE_FIELD, TIME_FIELD, CASE_REFERENCE_NUMBER_FIELD),
                List.of(DATE_TEXT, TIME_TEXT_4, CASE_REFERENCE_NUMBER_TEXT_1)
            ),
            new ListTestCaseSettings(
                FTT_RP_WEEKLY_HEARING_LIST_JSON,
                ListType.RPT_MIDLANDS_WEEKLY_HEARING_LIST,
                1,
                2,
                3,
                List.of(DATE_FIELD, TIME_FIELD, CASE_REFERENCE_NUMBER_FIELD),
                List.of(DATE_TEXT, TIME_TEXT_4, CASE_REFERENCE_NUMBER_TEXT_1)
            ),
            new ListTestCaseSettings(
                FTT_RP_WEEKLY_HEARING_LIST_JSON,
                ListType.RPT_NORTHERN_WEEKLY_HEARING_LIST,
                1,
                2,
                3,
                List.of(DATE_FIELD, TIME_FIELD, CASE_REFERENCE_NUMBER_FIELD),
                List.of(DATE_TEXT, TIME_TEXT_4, CASE_REFERENCE_NUMBER_TEXT_1)
            ),
            new ListTestCaseSettings(
                FTT_RP_WEEKLY_HEARING_LIST_JSON,
                ListType.RPT_SOUTHERN_WEEKLY_HEARING_LIST,
                1,
                2,
                3,
                List.of(DATE_FIELD, TIME_FIELD, CASE_REFERENCE_NUMBER_FIELD),
                List.of(DATE_TEXT, TIME_TEXT_4, CASE_REFERENCE_NUMBER_TEXT_1)
            ),
            new ListTestCaseSettings(
                "fttTaxWeeklyHearingList.json",
                ListType.FTT_TAX_WEEKLY_HEARING_LIST,
                1,
                2,
                3,
                List.of(DATE_FIELD, HEARING_TIME_FIELD, CASE_REFERENCE_NUMBER_FIELD),
                List.of(DATE_TEXT, TIME_TEXT_4, CASE_REFERENCE_NUMBER_TEXT_1)
            ),
            new ListTestCaseSettings(
                "grcWeeklyHearingList.json",
                ListType.GRC_WEEKLY_HEARING_LIST,
                1,
                2,
                3,
                List.of(DATE_FIELD, HEARING_TIME_FIELD, CASE_REFERENCE_NUMBER_FIELD),
                List.of(DATE_TEXT, TIME_TEXT_4, CASE_REFERENCE_NUMBER_TEXT_1)
            ),
            new ListTestCaseSettings(
                "insolvencyAndCompaniesCourtChdDailyCauseList.json",
                ListType.INSOLVENCY_AND_COMPANIES_COURT_CHD_DAILY_CAUSE_LIST,
                1,
                2,
                3,
                List.of(TIME_FIELD, CASE_NUMBER_FIELD, CASE_NAME_FIELD),
                List.of(TIME_TEXT_1, CASE_NUMBER_TEXT, CASE_NAME_TEXT_2)
            ),
            new ListTestCaseSettings(
                "intellectualPropertyAndEnterpriseCourtDailyCauseList.json",
                ListType.INTELLECTUAL_PROPERTY_AND_ENTERPRISE_COURT_DAILY_CAUSE_LIST,
                1,
                2,
                3,
                List.of(TIME_FIELD, CASE_NUMBER_FIELD, CASE_NAME_FIELD),
                List.of(TIME_TEXT_1, CASE_NUMBER_TEXT, CASE_NAME_TEXT_2)
            ),
            new ListTestCaseSettings(
                "intellectualPropertyListChdDailyCauseList.json",
                ListType.INTELLECTUAL_PROPERTY_LIST_CHD_DAILY_CAUSE_LIST,
                1,
                2,
                3,
                List.of(TIME_FIELD, CASE_NUMBER_FIELD, CASE_NAME_FIELD),
                List.of(TIME_TEXT_1, CASE_NUMBER_TEXT, CASE_NAME_TEXT_2)
            ),
            new ListTestCaseSettings(
                "interimApplicationsChanceryDivisionDailyCauseList.json",
                ListType.INTERIM_APPLICATIONS_CHD_DAILY_CAUSE_LIST,
                1,
                3,
                3,
                List.of(TIME_FIELD, CASE_NUMBER_FIELD, CASE_NAME_FIELD),
                List.of(TIME_TEXT_2, CASE_REFERENCE_NUMBER_TEXT_1, CASE_NAME_TEXT_1)
            ),
            new ListTestCaseSettings(
                "kingsBenchDivisionDailyCauseList.json",
                ListType.KINGS_BENCH_DIVISION_DAILY_CAUSE_LIST,
                1,
                2,
                3,
                List.of(TIME_FIELD, CASE_NUMBER_FIELD, CASE_DETAILS_FIELD),
                List.of(TIME_TEXT_1, CASE_NUMBER_TEXT, CASE_DETAILS_TEXT)
            ),
            new ListTestCaseSettings(
                "kingsBenchMastersDailyCauseList.json",
                ListType.KINGS_BENCH_MASTERS_DAILY_CAUSE_LIST,
                1,
                2,
                3,
                List.of(TIME_FIELD, CASE_NUMBER_FIELD, CASE_DETAILS_FIELD),
                List.of(TIME_TEXT_1, CASE_NUMBER_TEXT, CASE_DETAILS_TEXT)
            ),
            new ListTestCaseSettings(
                ADMINISTRATIVE_COURT_DAILY_CAUSE_LIST_JSON,
                ListType.LEEDS_ADMINISTRATIVE_COURT_DAILY_CAUSE_LIST,
                1,
                2,
                4,
                List.of(TIME_FIELD, CASE_NUMBER_FIELD, HEARING_TYPE_FIELD, CASE_DETAILS_FIELD),
                List.of(TIME_TEXT_3, CASE_NUMBER_TEXT, DIRECTIONS_TEXT, CASE_DETAILS_TEXT)
            ),
            new ListTestCaseSettings(
                "londonAdministrativeCourtDailyCauseList.json",
                ListType.LONDON_ADMINISTRATIVE_COURT_DAILY_CAUSE_LIST,
                1,
                2,
                3,
                List.of(TIME_FIELD, CASE_NUMBER_FIELD, CASE_DETAILS_FIELD),
                List.of(TIME_TEXT_1, CASE_NUMBER_TEXT, CASE_DETAILS_TEXT)
            ),
            new ListTestCaseSettings(
                "londonCircuitCommercialCourtKbDailyCauseList.json",
                ListType.LONDON_CIRCUIT_COMMERCIAL_COURT_KB_DAILY_CAUSE_LIST,
                1,
                2,
                3,
                List.of(TIME_FIELD, CASE_NUMBER_FIELD, CASE_NAME_FIELD),
                List.of(TIME_TEXT_1, CASE_NUMBER_TEXT, CASE_NAME_TEXT_2)
            ),
            new ListTestCaseSettings(
                ADMINISTRATIVE_COURT_DAILY_CAUSE_LIST_JSON,
                ListType.MANCHESTER_ADMINISTRATIVE_COURT_DAILY_CAUSE_LIST,
                1,
                2,
                4,
                List.of(TIME_FIELD, CASE_NUMBER_FIELD, HEARING_TYPE_FIELD, CASE_DETAILS_FIELD),
                List.of(TIME_TEXT_3, CASE_NUMBER_TEXT, DIRECTIONS_TEXT, CASE_DETAILS_TEXT)
            ),
            new ListTestCaseSettings(
                "mayorAndCityCivilDailyCauseList.json",
                ListType.MAYOR_AND_CITY_CIVIL_DAILY_CAUSE_LIST,
                1,
                2,
                3,
                List.of(TIME_FIELD, CASE_NUMBER_FIELD, CASE_DETAILS_FIELD),
                List.of(TIME_TEXT_1, CASE_NUMBER_TEXT, CASE_DETAILS_TEXT)
            ),
            new ListTestCaseSettings(
                "patentsCourtChdDailyCauseList.json",
                ListType.PATENTS_COURT_CHD_DAILY_CAUSE_LIST,
                1,
                2,
                3,
                List.of(TIME_FIELD, CASE_NUMBER_FIELD, CASE_NAME_FIELD),
                List.of(TIME_TEXT_1, CASE_NUMBER_TEXT, CASE_NAME_TEXT_2)
            ),
            new ListTestCaseSettings(
                "pensionsListChdDailyCauseList.json",
                ListType.PENSIONS_LIST_CHD_DAILY_CAUSE_LIST,
                1,
                2,
                3,
                List.of(TIME_FIELD, CASE_NUMBER_FIELD, CASE_NAME_FIELD),
                List.of(TIME_TEXT_1, CASE_NUMBER_TEXT, CASE_NAME_TEXT_2)
            ),
            new ListTestCaseSettings(
                "phtWeeklyHearingList.json",
                ListType.PHT_WEEKLY_HEARING_LIST,
                1,
                2,
                2,
                List.of(DATE_FIELD, CASE_NAME_FIELD),
                List.of("10 December 2024", CASE_NAME_TEXT_1)
            ),
            new ListTestCaseSettings(
                "propertyTrustsProbateListChdDailyCauseList.json",
                ListType.PROPERTY_TRUSTS_PROBATE_LIST_CHD_DAILY_CAUSE_LIST,
                1,
                2,
                3,
                List.of(TIME_FIELD, CASE_NUMBER_FIELD, CASE_NAME_FIELD),
                List.of(TIME_TEXT_1, CASE_NUMBER_TEXT, CASE_NAME_TEXT_2)
            ),
            new ListTestCaseSettings(
                "revenueListChdDailyCauseList.json",
                ListType.REVENUE_LIST_CHD_DAILY_CAUSE_LIST,
                1,
                2,
                3,
                List.of(TIME_FIELD, CASE_NUMBER_FIELD, CASE_NAME_FIELD),
                List.of(TIME_TEXT_1, CASE_NUMBER_TEXT, CASE_NAME_TEXT_2)
            ),
            new ListTestCaseSettings(
                "sendDailyHearingList.json",
                ListType.SEND_DAILY_HEARING_LIST,
                1,
                2,
                3,
                List.of(TIME_FIELD, CASE_REFERENCE_NUMBER_FIELD, "Venue"),
                List.of(TIME_TEXT_3, CASE_REFERENCE_NUMBER_TEXT_1, VENUE_TEXT)
            ),
            new ListTestCaseSettings(
                "seniorCourtsCostsOfficeDailyCauseList.json",
                ListType.SENIOR_COURTS_COSTS_OFFICE_DAILY_CAUSE_LIST,
                1,
                2,
                3,
                List.of(TIME_FIELD, CASE_NUMBER_FIELD, CASE_DETAILS_FIELD),
                List.of(TIME_TEXT_1, CASE_NUMBER_TEXT, CASE_DETAILS_TEXT)
            ),
            new ListTestCaseSettings(
                "siacWeeklyHearingList.json",
                ListType.SIAC_WEEKLY_HEARING_LIST,
                1,
                1,
                3,
                List.of(DATE_FIELD, TIME_FIELD, CASE_REFERENCE_NUMBER_FIELD),
                List.of("11 December 2024", TIME_TEXT_4, CASE_REFERENCE_NUMBER_TEXT_1)
            ),
            new ListTestCaseSettings(
                "siacWeeklyHearingList.json",
                ListType.POAC_WEEKLY_HEARING_LIST,
                1,
                1,
                3,
                List.of(DATE_FIELD, TIME_FIELD, CASE_REFERENCE_NUMBER_FIELD),
                List.of("11 December 2024", TIME_TEXT_4, CASE_REFERENCE_NUMBER_TEXT_1)
            ),
            new ListTestCaseSettings(
                "siacWeeklyHearingList.json",
                ListType.PAAC_WEEKLY_HEARING_LIST,
                1,
                1,
                3,
                List.of(DATE_FIELD, TIME_FIELD, CASE_REFERENCE_NUMBER_FIELD),
                List.of("11 December 2024", TIME_TEXT_4, CASE_REFERENCE_NUMBER_TEXT_1)
            ),
            new ListTestCaseSettings(
                SSCS_DAILY_HEARING_LIST_JSON,
                ListType.SSCS_MIDLANDS_DAILY_HEARING_LIST,
                1,
                1,
                3,
                List.of(HEARING_TIME_FIELD, HEARING_TYPE_FIELD, APPEAL_REFERENCE_NUMBER_FIELD),
                List.of(TIME_TEXT_2, DIRECTIONS_FIELD, CASE_REFERENCE_NUMBER_TEXT_2)
            ),
            new ListTestCaseSettings(
                SSCS_DAILY_HEARING_LIST_JSON,
                ListType.SSCS_SOUTH_EAST_DAILY_HEARING_LIST,
                1,
                1,
                3,
                List.of(HEARING_TIME_FIELD, HEARING_TYPE_FIELD, APPEAL_REFERENCE_NUMBER_FIELD),
                List.of(TIME_TEXT_2, DIRECTIONS_FIELD, CASE_REFERENCE_NUMBER_TEXT_2)
            ),
            new ListTestCaseSettings(
                SSCS_DAILY_HEARING_LIST_JSON,
                ListType.SSCS_WALES_AND_SOUTH_WEST_DAILY_HEARING_LIST,
                1,
                1,
                3,
                List.of(HEARING_TIME_FIELD, HEARING_TYPE_FIELD, APPEAL_REFERENCE_NUMBER_FIELD),
                List.of(TIME_TEXT_2, DIRECTIONS_FIELD, CASE_REFERENCE_NUMBER_TEXT_2)
            ),
            new ListTestCaseSettings(
                SSCS_DAILY_HEARING_LIST_JSON,
                ListType.SSCS_SCOTLAND_DAILY_HEARING_LIST,
                1,
                1,
                3,
                List.of(HEARING_TIME_FIELD, HEARING_TYPE_FIELD, APPEAL_REFERENCE_NUMBER_FIELD),
                List.of(TIME_TEXT_2, DIRECTIONS_FIELD, CASE_REFERENCE_NUMBER_TEXT_2)
            ),
            new ListTestCaseSettings(
                SSCS_DAILY_HEARING_LIST_JSON,
                ListType.SSCS_NORTH_EAST_DAILY_HEARING_LIST,
                1,
                1,
                3,
                List.of(HEARING_TIME_FIELD, HEARING_TYPE_FIELD, APPEAL_REFERENCE_NUMBER_FIELD),
                List.of(TIME_TEXT_2, DIRECTIONS_FIELD, CASE_REFERENCE_NUMBER_TEXT_2)
            ),
            new ListTestCaseSettings(
                SSCS_DAILY_HEARING_LIST_JSON,
                ListType.SSCS_NORTH_WEST_DAILY_HEARING_LIST,
                1,
                1,
                3,
                List.of(HEARING_TIME_FIELD, HEARING_TYPE_FIELD, APPEAL_REFERENCE_NUMBER_FIELD),
                List.of(TIME_TEXT_2, DIRECTIONS_FIELD, CASE_REFERENCE_NUMBER_TEXT_2)
            ),
            new ListTestCaseSettings(
                SSCS_DAILY_HEARING_LIST_JSON,
                ListType.SSCS_LONDON_DAILY_HEARING_LIST,
                1,
                1,
                3,
                List.of(HEARING_TIME_FIELD, HEARING_TYPE_FIELD, APPEAL_REFERENCE_NUMBER_FIELD),
                List.of(TIME_TEXT_2, DIRECTIONS_FIELD, CASE_REFERENCE_NUMBER_TEXT_2)
            ),
            new ListTestCaseSettings(
                "technologyAndConstructionCourtKbDailyCauseList.json",
                ListType.TECHNOLOGY_AND_CONSTRUCTION_COURT_KB_DAILY_CAUSE_LIST,
                1,
                2,
                3,
                List.of(TIME_FIELD, CASE_NUMBER_FIELD, CASE_NAME_FIELD),
                List.of(TIME_TEXT_1, CASE_NUMBER_TEXT, CASE_NAME_TEXT_2)
            ),
            new ListTestCaseSettings(
                "utAdministrativeAppealsChamberDailyHearingList.json",
                ListType.UT_AAC_DAILY_HEARING_LIST,
                1,
                1,
                3,
                List.of(TIME_FIELD, CASE_REFERENCE_NUMBER_FIELD, "Appellant"),
                List.of(TIME_TEXT_4, CASE_NUMBER_TEXT, "Appellant 1")
            ),
            new ListTestCaseSettings(
                UT_IAC_JR_DAILY_HEARING_LIST_JSON,
                ListType.UT_IAC_JR_LEEDS_DAILY_HEARING_LIST,
                1,
                2,
                2,
                List.of(HEARING_TIME_FIELD, CASE_REFERENCE_NUMBER_FIELD),
                List.of(TIME_TEXT_2, CASE_REFERENCE_NUMBER_TEXT_1)
            ),
            new ListTestCaseSettings(
                UT_IAC_JR_DAILY_HEARING_LIST_JSON,
                ListType.UT_IAC_JR_MANCHESTER_DAILY_HEARING_LIST,
                1,
                2,
                2,
                List.of(HEARING_TIME_FIELD, CASE_REFERENCE_NUMBER_FIELD),
                List.of(TIME_TEXT_2, CASE_REFERENCE_NUMBER_TEXT_1)
            ),
            new ListTestCaseSettings(
                UT_IAC_JR_DAILY_HEARING_LIST_JSON,
                ListType.UT_IAC_JR_BIRMINGHAM_DAILY_HEARING_LIST,
                1,
                2,
                2,
                List.of(HEARING_TIME_FIELD, CASE_REFERENCE_NUMBER_FIELD),
                List.of(TIME_TEXT_2, CASE_REFERENCE_NUMBER_TEXT_1)
            ),
            new ListTestCaseSettings(
                UT_IAC_JR_DAILY_HEARING_LIST_JSON,
                ListType.UT_IAC_JR_CARDIFF_DAILY_HEARING_LIST,
                1,
                2,
                2,
                List.of(HEARING_TIME_FIELD, CASE_REFERENCE_NUMBER_FIELD),
                List.of(TIME_TEXT_2, CASE_REFERENCE_NUMBER_TEXT_1)
            ),
            new ListTestCaseSettings(
                "utIacJudicialReviewLondonDailyHearingList.json",
                ListType.UT_IAC_JR_LONDON_DAILY_HEARING_LIST,
                1,
                2,
                2,
                List.of(HEARING_TIME_FIELD, CASE_REFERENCE_NUMBER_FIELD),
                List.of(TIME_TEXT_2, CASE_REFERENCE_NUMBER_TEXT_1)
            ),
            new ListTestCaseSettings(
                "utIacStatutoryAppealsDailyHearingList.json",
                ListType.UT_IAC_STATUTORY_APPEALS_DAILY_HEARING_LIST,
                1,
                2,
                2,
                List.of(HEARING_TIME_FIELD, APPEAL_REFERENCE_NUMBER_FIELD),
                List.of(TIME_TEXT_2, CASE_REFERENCE_NUMBER_TEXT_1)
            ),
            new ListTestCaseSettings(
                "utLandsChamberDailyHearingList.json",
                ListType.UT_LC_DAILY_HEARING_LIST,
                1,
                1,
                3,
                List.of(TIME_FIELD, CASE_REFERENCE_NUMBER_FIELD, CASE_NAME_FIELD),
                List.of(TIME_TEXT_4, CASE_NUMBER_TEXT, CASE_NAME_TEXT_1)
            ),
            new ListTestCaseSettings(
                "utTaxAndChanceryChamberDailyHearingList.json",
                ListType.UT_T_AND_CC_DAILY_HEARING_LIST,
                1,
                1,
                3,
                List.of(TIME_FIELD, CASE_REFERENCE_NUMBER_FIELD, CASE_NAME_FIELD),
                List.of(TIME_TEXT_4, CASE_NUMBER_TEXT, CASE_NAME_TEXT_1)
            ),
            new ListTestCaseSettings(
                "wpafccWeeklyHearingList.json",
                ListType.WPAFCC_WEEKLY_HEARING_LIST,
                1,
                2,
                3,
                List.of(DATE_FIELD, HEARING_TIME_FIELD, CASE_REFERENCE_NUMBER_FIELD),
                List.of(DATE_TEXT, TIME_TEXT_4, CASE_REFERENCE_NUMBER_TEXT_1)
            )
        );
    }

    private NonStrategicTestCasesConfigurations() {
    }
}
