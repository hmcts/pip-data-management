package uk.gov.hmcts.reform.pip.data.management.controllers.publication.summary.configurations;

import uk.gov.hmcts.reform.pip.model.publication.ListType;

import java.util.stream.Stream;

public final class NonStrategicListTestCases {
    private static final String TIME_FIELD = "Time - 10am";
    private static final String RCJ_TIME_FIELD = "Time - 9am";
    private static final String RCJ_CASE_NUMBER_FIELD = "Case number - 12345";
    private static final String CASE_NUMBER_FIELD = "Case number - 1234";
    private static final String CASE_NAME_FIELD = "Case name - Case name A";
    private static final String CASE_DETAILS_FIELD = "Case details - Case details A";
    private static final String HEARING_TYPE_FIELD = "Hearing type - Directions";
    private static final String TRIBUNAL_CASE_NAME_FIELD =
        "Case name - This is a case name";
    private static final String TRIBUNAL_CASE_REFERENCE_NUMBER_FIELD =
        "Case reference number - 1234";
    private static final String TRIBUNAL_HEARING_TIME_FIELD =
        "Hearing time - 10am";
    private static final String TRIBUNAL_DATE_FIELD =
        "Date - 16 December 2024";
    private static final String TRIBUNAL_APPEAL_REFERENCE_NUMBER_FIELD =
        "Appeal reference number - 1234567";
    private static final String TRIBUNAL_HEARING_TIME_FIELD_2 =
        "Hearing time - 10:30am";

    private static final String ADMINISTRATIVE_COURT_DAILY_CAUSE_LISTS_JSON_FILE =
        "administrative-court-daily-cause-list/administrativeCourtDailyCauseList.json";
    private static final String ADMINISTRATIVE_COURT_DAILY_CAUSE_LISTS_EXCEL_FILE =
        "administrative-court-daily-cause-list/administrativeCourtDailyCauseList.xlsx";

    private static final String RPT_LISTS_EXCEL_FILE = "ftt-residential-property-tribunal-weekly-hearing-list/"
        + "fttResidentialPropertyTribunalWeeklyHearingList.xlsx";
    private static final String RPT_LISTS_JSON_FILE = "ftt-residential-property-tribunal-weekly-hearing-list/"
        + "fttResidentialPropertyTribunalWeeklyHearingList.json";
    private static final String SIAC_LISTS_EXCEL_FILE = "siac-weekly-hearing-list/"
        + "siacWeeklyHearingList.xlsx";
    private static final String SIAC_LISTS_JSON_FILE = "siac-weekly-hearing-list/"
        + "siacWeeklyHearingList.json";
    private static final String SSCS_LISTS_EXCEL_FILE = "sscs-daily-hearing-list/"
        + "sscsDailyHearingList.xlsx";
    private static final String SSCS_LISTS_JSON_FILE = "sscs-daily-hearing-list/"
        + "sscsDailyHearingList.json";
    private static final String UT_IAC_JR_LISTS_EXCEL_FILE = "ut-iac-judicial-review-daily-hearing-list/"
        + "utIacJudicialReviewDailyHearingList.xlsx";
    private static final String UT_IAC_JR_LISTS_JSON_FILE = "ut-iac-judicial-review-daily-hearing-list/"
        + "utIacJudicialReviewDailyHearingList.json";

    public static Stream<ListTestCaseSettings> provideRcjTestCases() {
        return Stream.of(
            new ListTestCaseSettings(
                ListType.LONDON_ADMINISTRATIVE_COURT_DAILY_CAUSE_LIST,
                "london-administrative-court-daily-cause-list/londonAdministrativeCourtDailyCauseList.xlsx",
                "london-administrative-court-daily-cause-list/londonAdministrativeCourtDailyCauseList.json",
                RCJ_TIME_FIELD, RCJ_CASE_NUMBER_FIELD, CASE_DETAILS_FIELD
            ),

            new ListTestCaseSettings(
                ListType.COUNTY_COURT_LONDON_CIVIL_DAILY_CAUSE_LIST,
                "county-court-london-civil-daily-cause-list/countyCourtLondonCivilDailyCauseList.xlsx",
                "county-court-london-civil-daily-cause-list/countyCourtLondonCivilDailyCauseList.json",
                RCJ_TIME_FIELD, RCJ_CASE_NUMBER_FIELD, CASE_DETAILS_FIELD
            ),

            new ListTestCaseSettings(
                ListType.CIVIL_COURTS_RCJ_DAILY_CAUSE_LIST,
                "civil-courts-rcj-daily-cause-list/civilCourtsRcjDailyCauseList.xlsx",
                "civil-courts-rcj-daily-cause-list/civilCourtsRcjDailyCauseList.json",
                RCJ_TIME_FIELD, RCJ_CASE_NUMBER_FIELD, CASE_DETAILS_FIELD
            ),

            new ListTestCaseSettings(
                ListType.COURT_OF_APPEAL_CRIMINAL_DAILY_CAUSE_LIST,
                "court-of-appeal-criminal-daily-cause-list/courtOfAppealCriminalDailyCauseList.xlsx",
                "court-of-appeal-criminal-daily-cause-list/courtOfAppealCriminalDailyCauseList.json",
                RCJ_TIME_FIELD, RCJ_CASE_NUMBER_FIELD, CASE_DETAILS_FIELD
            ),

            new ListTestCaseSettings(
                ListType.FAMILY_DIVISION_HIGH_COURT_DAILY_CAUSE_LIST,
                "family-division-high-court-daily-cause-list/familyDivisionHighCourtDailyCauseList.xlsx",
                "family-division-high-court-daily-cause-list/familyDivisionHighCourtDailyCauseList.json",
                RCJ_TIME_FIELD, RCJ_CASE_NUMBER_FIELD, CASE_DETAILS_FIELD
            ),

            new ListTestCaseSettings(
                ListType.KINGS_BENCH_DIVISION_DAILY_CAUSE_LIST,
                "kings-bench-division-daily-cause-list/kingsBenchDivisionDailyCauseList.xlsx",
                "kings-bench-division-daily-cause-list/kingsBenchDivisionDailyCauseList.json",
                RCJ_TIME_FIELD, RCJ_CASE_NUMBER_FIELD, CASE_DETAILS_FIELD
            ),

            new ListTestCaseSettings(
                ListType.KINGS_BENCH_MASTERS_DAILY_CAUSE_LIST,
                "kings-bench-masters-daily-cause-list/kingsBenchMastersDailyCauseList.xlsx",
                "kings-bench-masters-daily-cause-list/kingsBenchMastersDailyCauseList.json",
                RCJ_TIME_FIELD, RCJ_CASE_NUMBER_FIELD, CASE_DETAILS_FIELD
            ),

            new ListTestCaseSettings(
                ListType.SENIOR_COURTS_COSTS_OFFICE_DAILY_CAUSE_LIST,
                "senior-courts-costs-office-daily-cause-list/seniorCourtsCostsOfficeDailyCauseList.xlsx",
                "senior-courts-costs-office-daily-cause-list/seniorCourtsCostsOfficeDailyCauseList.json",
                RCJ_TIME_FIELD, RCJ_CASE_NUMBER_FIELD, CASE_DETAILS_FIELD
            ),

            new ListTestCaseSettings(
                ListType.MAYOR_AND_CITY_CIVIL_DAILY_CAUSE_LIST,
                "mayor-and-city-civil-daily-cause-list/mayorAndCityCivilDailyCauseList.xlsx",
                "mayor-and-city-civil-daily-cause-list/mayorAndCityCivilDailyCauseList.json",
                RCJ_TIME_FIELD, RCJ_CASE_NUMBER_FIELD, CASE_DETAILS_FIELD
            ),

            new ListTestCaseSettings(
                ListType.INTELLECTUAL_PROPERTY_AND_ENTERPRISE_COURT_DAILY_CAUSE_LIST,
                "intellectual-property-and-enterprise-court-daily-cause-list/"
                    + "intellectualPropertyAndEnterpriseCourtDailyCauseList.xlsx",
                "intellectual-property-and-enterprise-court-daily-cause-list/"
                    + "intellectualPropertyAndEnterpriseCourtDailyCauseList.json",
                RCJ_TIME_FIELD, RCJ_CASE_NUMBER_FIELD, CASE_NAME_FIELD
            ),

            new ListTestCaseSettings(
                ListType.COURT_OF_APPEAL_CIVIL_DAILY_CAUSE_LIST,
                "court-of-appeal-civil-daily-cause-list/courtOfAppealCivilDailyCauseList.xlsx",
                "court-of-appeal-civil-daily-cause-list/courtOfAppealCivilDailyCauseList.json",
                RCJ_TIME_FIELD, RCJ_CASE_NUMBER_FIELD, CASE_DETAILS_FIELD
            ),

            new ListTestCaseSettings(
                ListType.INTELLECTUAL_PROPERTY_LIST_CHD_DAILY_CAUSE_LIST,
                "intellectual-property-list-chd-daily-cause-list/intellectualPropertyListChdDailyCauseList.xlsx",
                "intellectual-property-list-chd-daily-cause-list/intellectualPropertyListChdDailyCauseList.json",
                RCJ_TIME_FIELD, RCJ_CASE_NUMBER_FIELD, CASE_NAME_FIELD
            ),

            new ListTestCaseSettings(
                ListType.LONDON_CIRCUIT_COMMERCIAL_COURT_KB_DAILY_CAUSE_LIST,
                "london-circuit-commercial-court-kb-daily-cause-list/"
                    + "londonCircuitCommercialCourtKbDailyCauseList.xlsx",
                "london-circuit-commercial-court-kb-daily-cause-list/"
                    + "londonCircuitCommercialCourtKbDailyCauseList.json",
                RCJ_TIME_FIELD, RCJ_CASE_NUMBER_FIELD, CASE_NAME_FIELD
            ),

            new ListTestCaseSettings(
                ListType.PATENTS_COURT_CHD_DAILY_CAUSE_LIST,
                "patents-court-chd-daily-cause-list/patentsCourtChdDailyCauseList.xlsx",
                "patents-court-chd-daily-cause-list/patentsCourtChdDailyCauseList.json",
                RCJ_TIME_FIELD, RCJ_CASE_NUMBER_FIELD, CASE_NAME_FIELD
            ),

            new ListTestCaseSettings(
                ListType.PENSIONS_LIST_CHD_DAILY_CAUSE_LIST,
                "pensions-list-chd-daily-cause-list/pensionsListChdDailyCauseList.xlsx",
                "pensions-list-chd-daily-cause-list/pensionsListChdDailyCauseList.json",
                RCJ_TIME_FIELD, RCJ_CASE_NUMBER_FIELD, CASE_NAME_FIELD
            ),

            new ListTestCaseSettings(
                ListType.PROPERTY_TRUSTS_PROBATE_LIST_CHD_DAILY_CAUSE_LIST,
                "property-trusts-probate-list-chd-daily-cause-list/propertyTrustsProbateListChdDailyCauseList.xlsx",
                "property-trusts-probate-list-chd-daily-cause-list/propertyTrustsProbateListChdDailyCauseList.json",
                RCJ_TIME_FIELD, RCJ_CASE_NUMBER_FIELD, CASE_NAME_FIELD
            ),

            new ListTestCaseSettings(
                ListType.REVENUE_LIST_CHD_DAILY_CAUSE_LIST,
                "revenue-list-chd-daily-cause-list/revenueListChdDailyCauseList.xlsx",
                "revenue-list-chd-daily-cause-list/revenueListChdDailyCauseList.json",
                RCJ_TIME_FIELD, RCJ_CASE_NUMBER_FIELD, CASE_NAME_FIELD
            ),

            new ListTestCaseSettings(
                ListType.TECHNOLOGY_AND_CONSTRUCTION_COURT_KB_DAILY_CAUSE_LIST,
                "technology-and-construction-court-kb-daily-cause-list/"
                    + "technologyAndConstructionCourtKbDailyCauseList.xlsx",
                "technology-and-construction-court-kb-daily-cause-list/"
                    + "technologyAndConstructionCourtKbDailyCauseList.json",
                RCJ_TIME_FIELD, RCJ_CASE_NUMBER_FIELD, CASE_NAME_FIELD
            ),

            new ListTestCaseSettings(
                ListType.ADMIRALTY_COURT_KB_DAILY_CAUSE_LIST,
                "admiralty_court_kb_daily_cause_list/admiraltyCourtKbDailyCauseList.xlsx",
                "admiralty_court_kb_daily_cause_list/admiraltyCourtKbDailyCauseList.json",
                RCJ_TIME_FIELD, RCJ_CASE_NUMBER_FIELD, CASE_NAME_FIELD
            ),

            new ListTestCaseSettings(
                ListType.BUSINESS_LIST_CHD_DAILY_CAUSE_LIST,
                "business_list_chd_daily_cause_list/businessListChdDailyCauseList.xlsx",
                "business_list_chd_daily_cause_list/businessListChdDailyCauseList.json",
                RCJ_TIME_FIELD, RCJ_CASE_NUMBER_FIELD, CASE_NAME_FIELD
            ),

            new ListTestCaseSettings(
                ListType.CHANCERY_APPEALS_CHD_DAILY_CAUSE_LIST,
                "chancery_appeals_chd_daily_cause_list/chanceryAppealsChdDailyCauseList.xlsx",
                "chancery_appeals_chd_daily_cause_list/chanceryAppealsChdDailyCauseList.json",
                RCJ_TIME_FIELD, RCJ_CASE_NUMBER_FIELD, CASE_NAME_FIELD
            ),

            new ListTestCaseSettings(
                ListType.COMMERCIAL_COURT_KB_DAILY_CAUSE_LIST,
                "commercial_court_kb_daily_cause_list/commercialCourtKbDailyCauseList.xlsx",
                "commercial_court_kb_daily_cause_list/commercialCourtKbDailyCauseList.json",
                RCJ_TIME_FIELD, RCJ_CASE_NUMBER_FIELD, CASE_NAME_FIELD
            ),

            new ListTestCaseSettings(
                ListType.COMPANIES_WINDING_UP_CHD_DAILY_CAUSE_LIST,
                "companies_winding_up_chd_daily_cause_list/companiesWindingUpChdDailyCauseList.xlsx",
                "companies_winding_up_chd_daily_cause_list/companiesWindingUpChdDailyCauseList.json",
                RCJ_TIME_FIELD, RCJ_CASE_NUMBER_FIELD, CASE_NAME_FIELD
            ),

            new ListTestCaseSettings(
                ListType.COMPETITION_LIST_CHD_DAILY_CAUSE_LIST,
                "competition_list_chd_daily_cause_list/competitionListChdDailyCauseList.xlsx",
                "competition_list_chd_daily_cause_list/competitionListChdDailyCauseList.json",
                RCJ_TIME_FIELD, RCJ_CASE_NUMBER_FIELD, CASE_NAME_FIELD
            ),

            new ListTestCaseSettings(
                ListType.FINANCIAL_LIST_CHD_KB_DAILY_CAUSE_LIST,
                "financial_list_chd_kb_daily_cause_list/financialListChdKbDailyCauseList.xlsx",
                "financial_list_chd_kb_daily_cause_list/financialListChdKbDailyCauseList.json",
                RCJ_TIME_FIELD, RCJ_CASE_NUMBER_FIELD, CASE_NAME_FIELD
            ),

            new ListTestCaseSettings(
                ListType.INSOLVENCY_AND_COMPANIES_COURT_CHD_DAILY_CAUSE_LIST,
                "insolvency_and_companies_court_chd_daily_cause_list/"
                    + "insolvencyAndCompaniesCourtChdDailyCauseList.xlsx",
                "insolvency_and_companies_court_chd_daily_cause_list/"
                    + "insolvencyAndCompaniesCourtChdDailyCauseList.json",
                RCJ_TIME_FIELD, RCJ_CASE_NUMBER_FIELD, CASE_NAME_FIELD
            ),

            new ListTestCaseSettings(
                ListType.BIRMINGHAM_ADMINISTRATIVE_COURT_DAILY_CAUSE_LIST,
                ADMINISTRATIVE_COURT_DAILY_CAUSE_LISTS_EXCEL_FILE,
                ADMINISTRATIVE_COURT_DAILY_CAUSE_LISTS_JSON_FILE,
                TIME_FIELD, CASE_NUMBER_FIELD, HEARING_TYPE_FIELD, CASE_DETAILS_FIELD
            ),

            new ListTestCaseSettings(
                ListType.BRISTOL_AND_CARDIFF_ADMINISTRATIVE_COURT_DAILY_CAUSE_LIST,
                ADMINISTRATIVE_COURT_DAILY_CAUSE_LISTS_EXCEL_FILE,
                ADMINISTRATIVE_COURT_DAILY_CAUSE_LISTS_JSON_FILE,
                TIME_FIELD, CASE_NUMBER_FIELD, HEARING_TYPE_FIELD, CASE_DETAILS_FIELD
            ),

            new ListTestCaseSettings(
                ListType.LEEDS_ADMINISTRATIVE_COURT_DAILY_CAUSE_LIST,
                ADMINISTRATIVE_COURT_DAILY_CAUSE_LISTS_EXCEL_FILE,
                ADMINISTRATIVE_COURT_DAILY_CAUSE_LISTS_JSON_FILE,
                TIME_FIELD, CASE_NUMBER_FIELD, HEARING_TYPE_FIELD, CASE_DETAILS_FIELD
            ),

            new ListTestCaseSettings(
                ListType.MANCHESTER_ADMINISTRATIVE_COURT_DAILY_CAUSE_LIST,
                ADMINISTRATIVE_COURT_DAILY_CAUSE_LISTS_EXCEL_FILE,
                ADMINISTRATIVE_COURT_DAILY_CAUSE_LISTS_JSON_FILE,
                TIME_FIELD, CASE_NUMBER_FIELD, HEARING_TYPE_FIELD, CASE_DETAILS_FIELD
            )
        );
    }

    public static Stream<ListTestCaseSettings> provideTribunalTestCases() {
        return Stream.of(
            // CST Weekly Hearing List
            new ListTestCaseSettings(
                ListType.CST_WEEKLY_HEARING_LIST,
                "cst-weekly-hearing-list/cstWeeklyHearingList.xlsx",
                "cst-weekly-hearing-list/cstWeeklyHearingList.json",
                "Date - 10 December 2024", TRIBUNAL_CASE_NAME_FIELD
            ),
            // PHT Weekly Hearing List
            new ListTestCaseSettings(
                ListType.PHT_WEEKLY_HEARING_LIST,
                "pht-weekly-hearing-list/phtWeeklyHearingList.xlsx",
                "pht-weekly-hearing-list/phtWeeklyHearingList.json",
                "Date - 10 December 2024", TRIBUNAL_CASE_NAME_FIELD
            ),
            // GRC Weekly Hearing List
            new ListTestCaseSettings(
                ListType.GRC_WEEKLY_HEARING_LIST,
                "grc-weekly-hearing-list/grcWeeklyHearingList.xlsx",
                "grc-weekly-hearing-list/grcWeeklyHearingList.json",
                TRIBUNAL_DATE_FIELD, TRIBUNAL_HEARING_TIME_FIELD, TRIBUNAL_CASE_REFERENCE_NUMBER_FIELD
            ),
            // WPAFCC Weekly Hearing List
            new ListTestCaseSettings(
                ListType.WPAFCC_WEEKLY_HEARING_LIST,
                "wpafcc-weekly-hearing-list/wpafccWeeklyHearingList.xlsx",
                "wpafcc-weekly-hearing-list/wpafccWeeklyHearingList.json",
                TRIBUNAL_DATE_FIELD, TRIBUNAL_HEARING_TIME_FIELD, TRIBUNAL_CASE_REFERENCE_NUMBER_FIELD
            ),
            // UT IAC Judicial Review Daily Hearing Lists
            new ListTestCaseSettings(
                ListType.UT_IAC_JR_LONDON_DAILY_HEARING_LIST,
                "ut-iac-judicial-review-london-daily-hearing-list/utIacJudicialReviewLondonDailyHearingList.xlsx",
                "ut-iac-judicial-review-london-daily-hearing-list/utIacJudicialReviewLondonDailyHearingList.json",
                TRIBUNAL_HEARING_TIME_FIELD_2, TRIBUNAL_CASE_REFERENCE_NUMBER_FIELD
            ),
            new ListTestCaseSettings(
                ListType.UT_IAC_JR_LEEDS_DAILY_HEARING_LIST,
                UT_IAC_JR_LISTS_EXCEL_FILE,
                UT_IAC_JR_LISTS_JSON_FILE,
                TRIBUNAL_HEARING_TIME_FIELD_2, TRIBUNAL_CASE_REFERENCE_NUMBER_FIELD
            ),
            new ListTestCaseSettings(
                ListType.UT_IAC_JR_MANCHESTER_DAILY_HEARING_LIST,
                UT_IAC_JR_LISTS_EXCEL_FILE,
                UT_IAC_JR_LISTS_JSON_FILE,
                TRIBUNAL_HEARING_TIME_FIELD_2, TRIBUNAL_CASE_REFERENCE_NUMBER_FIELD
            ),
            new ListTestCaseSettings(
                ListType.UT_IAC_JR_BIRMINGHAM_DAILY_HEARING_LIST,
                UT_IAC_JR_LISTS_EXCEL_FILE,
                UT_IAC_JR_LISTS_JSON_FILE,
                TRIBUNAL_HEARING_TIME_FIELD_2, TRIBUNAL_CASE_REFERENCE_NUMBER_FIELD
            ),
            new ListTestCaseSettings(
                ListType.UT_IAC_JR_CARDIFF_DAILY_HEARING_LIST,
                UT_IAC_JR_LISTS_EXCEL_FILE,
                UT_IAC_JR_LISTS_JSON_FILE,
                TRIBUNAL_HEARING_TIME_FIELD_2, TRIBUNAL_CASE_REFERENCE_NUMBER_FIELD
            ),
            // UT IAC Statutory Appeals Daily Hearing List
            new ListTestCaseSettings(
                ListType.UT_IAC_STATUTORY_APPEALS_DAILY_HEARING_LIST,
                "ut-iac-statutory-appeals-daily-hearing-list/utIacStatutoryAppealsDailyHearingList.xlsx",
                "ut-iac-statutory-appeals-daily-hearing-list/utIacStatutoryAppealsDailyHearingList.json",
                TRIBUNAL_HEARING_TIME_FIELD_2, "Appeal reference number - 1234"
            ),
            // UT Administrative Appeals Chamber Daily Hearing List
            new ListTestCaseSettings(
                ListType.UT_AAC_DAILY_HEARING_LIST,
                "ut-administrative-appeals-chamber-daily-hearing-list/"
                    + "utAdministrativeAppealsChamberDailyHearingList.xlsx",
                "ut-administrative-appeals-chamber-daily-hearing-list/"
                    + "utAdministrativeAppealsChamberDailyHearingList.json",
                TIME_FIELD, TRIBUNAL_CASE_REFERENCE_NUMBER_FIELD, "Appellant - Appellant 1"
            ),
            // UT Lands Chamber Daily Hearing List
            new ListTestCaseSettings(
                ListType.UT_LC_DAILY_HEARING_LIST,
                "ut-lands-chamber-daily-hearing-list/utLandsChamberDailyHearingList.xlsx",
                "ut-lands-chamber-daily-hearing-list/utLandsChamberDailyHearingList.json",
                TIME_FIELD, TRIBUNAL_CASE_REFERENCE_NUMBER_FIELD, TRIBUNAL_CASE_NAME_FIELD
            ),
            // UT Tax and Chancery Chamber Daily Hearing List
            new ListTestCaseSettings(
                ListType.UT_T_AND_CC_DAILY_HEARING_LIST,
                "ut-tax-and-chancery-chamber-daily-hearing-list/utTaxAndChanceryChamberDailyHearingList.xlsx",
                "ut-tax-and-chancery-chamber-daily-hearing-list/utTaxAndChanceryChamberDailyHearingList.json",
                TIME_FIELD, TRIBUNAL_CASE_REFERENCE_NUMBER_FIELD, TRIBUNAL_CASE_NAME_FIELD
            ),
            // SIAC Weekly Hearing Lists
            new ListTestCaseSettings(
                ListType.SIAC_WEEKLY_HEARING_LIST,
                SIAC_LISTS_EXCEL_FILE,
                SIAC_LISTS_JSON_FILE,
                "Date - 11 December 2024", TIME_FIELD, "Case reference number - 123451"
            ),
            new ListTestCaseSettings(
                ListType.POAC_WEEKLY_HEARING_LIST,
                SIAC_LISTS_EXCEL_FILE,
                SIAC_LISTS_JSON_FILE,
                "Date - 11 December 2024", TIME_FIELD, "Case reference number - 123451"
            ),
            new ListTestCaseSettings(
                ListType.PAAC_WEEKLY_HEARING_LIST,
                SIAC_LISTS_EXCEL_FILE,
                SIAC_LISTS_JSON_FILE,
                "Date - 11 December 2024", TIME_FIELD, "Case reference number - 123451"
            ),
            // SSCS Daily Hearing Lists
            new ListTestCaseSettings(
                ListType.SSCS_MIDLANDS_DAILY_HEARING_LIST,
                SSCS_LISTS_EXCEL_FILE,
                SSCS_LISTS_JSON_FILE,
                TRIBUNAL_HEARING_TIME_FIELD, HEARING_TYPE_FIELD, TRIBUNAL_APPEAL_REFERENCE_NUMBER_FIELD
            ),
            new ListTestCaseSettings(
                ListType.SSCS_SOUTH_EAST_DAILY_HEARING_LIST,
                SSCS_LISTS_EXCEL_FILE,
                SSCS_LISTS_JSON_FILE,
                TRIBUNAL_HEARING_TIME_FIELD, HEARING_TYPE_FIELD, TRIBUNAL_APPEAL_REFERENCE_NUMBER_FIELD
            ),
            new ListTestCaseSettings(
                ListType.SSCS_WALES_AND_SOUTH_WEST_DAILY_HEARING_LIST,
                SSCS_LISTS_EXCEL_FILE,
                SSCS_LISTS_JSON_FILE,
                TRIBUNAL_HEARING_TIME_FIELD, HEARING_TYPE_FIELD, TRIBUNAL_APPEAL_REFERENCE_NUMBER_FIELD
            ),
            new ListTestCaseSettings(
                ListType.SSCS_SCOTLAND_DAILY_HEARING_LIST,
                SSCS_LISTS_EXCEL_FILE,
                SSCS_LISTS_JSON_FILE,
                TRIBUNAL_HEARING_TIME_FIELD, HEARING_TYPE_FIELD, TRIBUNAL_APPEAL_REFERENCE_NUMBER_FIELD
            ),
            new ListTestCaseSettings(
                ListType.SSCS_NORTH_EAST_DAILY_HEARING_LIST,
                SSCS_LISTS_EXCEL_FILE,
                SSCS_LISTS_JSON_FILE,
                TRIBUNAL_HEARING_TIME_FIELD, HEARING_TYPE_FIELD, TRIBUNAL_APPEAL_REFERENCE_NUMBER_FIELD
            ),
            new ListTestCaseSettings(
                ListType.SSCS_NORTH_WEST_DAILY_HEARING_LIST,
                SSCS_LISTS_EXCEL_FILE,
                SSCS_LISTS_JSON_FILE,
                TRIBUNAL_HEARING_TIME_FIELD, HEARING_TYPE_FIELD, TRIBUNAL_APPEAL_REFERENCE_NUMBER_FIELD
            ),
            new ListTestCaseSettings(
                ListType.SSCS_LONDON_DAILY_HEARING_LIST,
                SSCS_LISTS_EXCEL_FILE,
                SSCS_LISTS_JSON_FILE,
                TRIBUNAL_HEARING_TIME_FIELD, HEARING_TYPE_FIELD, TRIBUNAL_APPEAL_REFERENCE_NUMBER_FIELD
            ),
            // SEND Daily Hearing List
            new ListTestCaseSettings(
                ListType.SEND_DAILY_HEARING_LIST,
                "send-daily-hearing-list/sendDailyHearingList.xlsx",
                "send-daily-hearing-list/sendDailyHearingList.json",
                TIME_FIELD, TRIBUNAL_CASE_REFERENCE_NUMBER_FIELD, "Venue - Venue A"
            ),
            // CIC Weekly Hearing List
            new ListTestCaseSettings(
                ListType.CIC_WEEKLY_HEARING_LIST,
                "cic-weekly-hearing-list/cicWeeklyHearingList.xlsx",
                "cic-weekly-hearing-list/cicWeeklyHearingList.json",
                "Date - 26 June 2025",
                TRIBUNAL_HEARING_TIME_FIELD,
                TRIBUNAL_CASE_REFERENCE_NUMBER_FIELD,
                TRIBUNAL_CASE_NAME_FIELD
            ),
            // AST Daily Hearing List
            new ListTestCaseSettings(
                ListType.AST_DAILY_HEARING_LIST,
                "ast-daily-hearing-list/astDailyHearingList.xlsx",
                "ast-daily-hearing-list/astDailyHearingList.json",
                "Appellant - Appellant A", "Appeal reference number - 12345", TRIBUNAL_HEARING_TIME_FIELD_2
            ),
            // RPT Weekly Hearing Lists
            new ListTestCaseSettings(
                ListType.RPT_EASTERN_WEEKLY_HEARING_LIST,
                RPT_LISTS_EXCEL_FILE,
                RPT_LISTS_JSON_FILE,
                TRIBUNAL_DATE_FIELD, TIME_FIELD, TRIBUNAL_CASE_REFERENCE_NUMBER_FIELD
            ),
            new ListTestCaseSettings(
                ListType.RPT_LONDON_WEEKLY_HEARING_LIST,
                RPT_LISTS_EXCEL_FILE,
                RPT_LISTS_JSON_FILE,
                TRIBUNAL_DATE_FIELD, TIME_FIELD, TRIBUNAL_CASE_REFERENCE_NUMBER_FIELD
            ),
            new ListTestCaseSettings(
                ListType.RPT_MIDLANDS_WEEKLY_HEARING_LIST,
                RPT_LISTS_EXCEL_FILE,
                RPT_LISTS_JSON_FILE,
                TRIBUNAL_DATE_FIELD, TIME_FIELD, TRIBUNAL_CASE_REFERENCE_NUMBER_FIELD
            ),
            new ListTestCaseSettings(
                ListType.RPT_NORTHERN_WEEKLY_HEARING_LIST,
                RPT_LISTS_EXCEL_FILE,
                RPT_LISTS_JSON_FILE,
                TRIBUNAL_DATE_FIELD, TIME_FIELD, TRIBUNAL_CASE_REFERENCE_NUMBER_FIELD
            ),
            new ListTestCaseSettings(
                ListType.RPT_SOUTHERN_WEEKLY_HEARING_LIST,
                RPT_LISTS_EXCEL_FILE,
                RPT_LISTS_JSON_FILE,
                TRIBUNAL_DATE_FIELD, TIME_FIELD, TRIBUNAL_CASE_REFERENCE_NUMBER_FIELD
            ),
            // FTT Weekly Hearing Lists
            new ListTestCaseSettings(
                ListType.FTT_TAX_WEEKLY_HEARING_LIST,
                "ftt-tax-tribunal-weekly-hearing-list/fttTaxWeeklyHearingList.xlsx",
                "ftt-tax-tribunal-weekly-hearing-list/fttTaxWeeklyHearingList.json",
                TRIBUNAL_DATE_FIELD, TRIBUNAL_HEARING_TIME_FIELD, TRIBUNAL_CASE_REFERENCE_NUMBER_FIELD
            ),
            new ListTestCaseSettings(
                ListType.FTT_LR_WEEKLY_HEARING_LIST,
                "ftt-land-registry-tribunal-weekly-hearing-list/fttLandRegistryTribunalWeeklyHearingList.xlsx",
                "ftt-land-registry-tribunal-weekly-hearing-list/fttLandRegistryTribunalWeeklyHearingList.json",
                TRIBUNAL_DATE_FIELD, TRIBUNAL_HEARING_TIME_FIELD, TRIBUNAL_CASE_REFERENCE_NUMBER_FIELD
            )
        );
    }

    private NonStrategicListTestCases() {
    }
}
