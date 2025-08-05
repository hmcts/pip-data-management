package uk.gov.hmcts.reform.pip.data.management.controllers.publication.summary.configurations;

import uk.gov.hmcts.reform.pip.model.publication.ListType;

import java.util.stream.Stream;

public final class RcjListTestCases {
    private static final String TIME_FIELD = "Time - 10am";
    private static final String RCJ_TIME_FIELD = "Time - 9am";
    private static final String RCJ_CASE_NUMBER_FIELD = "Case number - 12345";
    private static final String CASE_NUMBER_FIELD = "Case number - 1234";
    private static final String CASE_NAME_FIELD = "Case name - Case name A";
    private static final String CASE_DETAILS_FIELD = "Case details - Case details A";
    private static final String HEARING_TYPE_FIELD = "Hearing type - Directions";

    private static final String ADMINISTRATIVE_COURT_DAILY_CAUSE_LISTS_JSON_FILE =
        "administrative-court-daily-cause-list/administrativeCourtDailyCauseList.json";
    private static final String ADMINISTRATIVE_COURT_DAILY_CAUSE_LISTS_EXCEL_FILE =
        "administrative-court-daily-cause-list/administrativeCourtDailyCauseList.xlsx";

    public static Stream<ListTestCaseSettings> provideRcjTestCases() {
        return Stream.of(
            new ListTestCaseSettings(
                ListType.LONDON_ADMINISTRATIVE_COURT_DAILY_CAUSE_LIST,
                "london-administrative-court-daily-cause-list/londonAdministrativeCourtDailyCauseList.xlsx",
                "london-administrative-court-daily-cause-list/londonAdministrativeCourtDailyCauseList.json",
                RCJ_TIME_FIELD, RCJ_CASE_NUMBER_FIELD, CASE_DETAILS_FIELD
            ),

            new ListTestCaseSettings(ListType.COUNTY_COURT_LONDON_CIVIL_DAILY_CAUSE_LIST,
                                     "county-court-london-civil-daily-cause-list/"
                                         + "countyCourtLondonCivilDailyCauseList.xlsx",
                                     "county-court-london-civil-daily-cause-list/"
                                         + "countyCourtLondonCivilDailyCauseList.json",
                                     RCJ_TIME_FIELD, RCJ_CASE_NUMBER_FIELD, CASE_DETAILS_FIELD
            ),

            new ListTestCaseSettings(ListType.CIVIL_COURTS_RCJ_DAILY_CAUSE_LIST,
                                     "civil-courts-rcj-daily-cause-list/civilCourtsRcjDailyCauseList.xlsx",
                                     "civil-courts-rcj-daily-cause-list/civilCourtsRcjDailyCauseList.json",
                                     RCJ_TIME_FIELD, RCJ_CASE_NUMBER_FIELD, CASE_DETAILS_FIELD
            ),

            new ListTestCaseSettings(ListType.COURT_OF_APPEAL_CRIMINAL_DAILY_CAUSE_LIST,
                                     "court-of-appeal-criminal-daily-cause-list/"
                                         + "courtOfAppealCriminalDailyCauseList.xlsx",
                                     "court-of-appeal-criminal-daily-cause-list/"
                                         + "courtOfAppealCriminalDailyCauseList.json",
                                     RCJ_TIME_FIELD, RCJ_CASE_NUMBER_FIELD, CASE_DETAILS_FIELD
            ),

            new ListTestCaseSettings(ListType.FAMILY_DIVISION_HIGH_COURT_DAILY_CAUSE_LIST,
                                     "family-division-high-court-daily-cause-list/"
                                     + "familyDivisionHighCourtDailyCauseList.xlsx",
                                     "family-division-high-court-daily-cause-list/"
                                     + "familyDivisionHighCourtDailyCauseList.json",
                                     RCJ_TIME_FIELD, RCJ_CASE_NUMBER_FIELD, CASE_DETAILS_FIELD
            ),

            new ListTestCaseSettings(ListType.KINGS_BENCH_DIVISION_DAILY_CAUSE_LIST,
                                     "kings-bench-division-daily-cause-list/kingsBenchDivisionDailyCauseList.xlsx",
                                     "kings-bench-division-daily-cause-list/kingsBenchDivisionDailyCauseList.json",
                                     RCJ_TIME_FIELD, RCJ_CASE_NUMBER_FIELD, CASE_DETAILS_FIELD
            ),

            new ListTestCaseSettings(ListType.KINGS_BENCH_MASTERS_DAILY_CAUSE_LIST,
                                     "kings-bench-masters-daily-cause-list/kingsBenchMastersDailyCauseList.xlsx",
                                     "kings-bench-masters-daily-cause-list/kingsBenchMastersDailyCauseList.json",
                                     RCJ_TIME_FIELD, RCJ_CASE_NUMBER_FIELD, CASE_DETAILS_FIELD
            ),

            new ListTestCaseSettings(ListType.SENIOR_COURTS_COSTS_OFFICE_DAILY_CAUSE_LIST,
                                     "senior-courts-costs-office-daily-cause-list/"
                                     + "seniorCourtsCostsOfficeDailyCauseList.xlsx",
                                     "senior-courts-costs-office-daily-cause-list/"
                                     + "seniorCourtsCostsOfficeDailyCauseList.json",
                                     RCJ_TIME_FIELD, RCJ_CASE_NUMBER_FIELD, CASE_DETAILS_FIELD
            ),

            new ListTestCaseSettings(ListType.MAYOR_AND_CITY_CIVIL_DAILY_CAUSE_LIST,
                                     "mayor-and-city-civil-daily-cause-list/mayorAndCityCivilDailyCauseList.xlsx",
                                     "mayor-and-city-civil-daily-cause-list/mayorAndCityCivilDailyCauseList.json",
                                     RCJ_TIME_FIELD, RCJ_CASE_NUMBER_FIELD, CASE_DETAILS_FIELD
            ),

            new ListTestCaseSettings(ListType.INTELLECTUAL_PROPERTY_AND_ENTERPRISE_COURT_DAILY_CAUSE_LIST,
                                     "intellectual-property-and-enterprise-court-daily-cause-list/"
                             + "intellectualPropertyAndEnterpriseCourtDailyCauseList.xlsx",
                                     "intellectual-property-and-enterprise-court-daily-cause-list/"
                             + "intellectualPropertyAndEnterpriseCourtDailyCauseList.json",
                                     RCJ_TIME_FIELD, RCJ_CASE_NUMBER_FIELD, CASE_NAME_FIELD
            ),

            new ListTestCaseSettings(ListType.COURT_OF_APPEAL_CIVIL_DAILY_CAUSE_LIST,
                                     "court-of-appeal-civil-daily-cause-list/courtOfAppealCivilDailyCauseList.xlsx",
                                     "court-of-appeal-civil-daily-cause-list/courtOfAppealCivilDailyCauseList.json",
                                     RCJ_TIME_FIELD, RCJ_CASE_NUMBER_FIELD, CASE_DETAILS_FIELD
            ),

            new ListTestCaseSettings(ListType.INTELLECTUAL_PROPERTY_LIST_CHD_DAILY_CAUSE_LIST,
                                     "intellectual-property-list-chd-daily-cause-list/"
                             + "intellectualPropertyListChdDailyCauseList.xlsx",
                                     "intellectual-property-list-chd-daily-cause-list/"
                             + "intellectualPropertyListChdDailyCauseList.json",
                                     RCJ_TIME_FIELD, RCJ_CASE_NUMBER_FIELD, CASE_NAME_FIELD
            ),

            new ListTestCaseSettings(ListType.LONDON_CIRCUIT_COMMERCIAL_COURT_KB_DAILY_CAUSE_LIST,
                                     "london-circuit-commercial-court-kb-daily-cause-list/"
                             + "londonCircuitCommercialCourtKbDailyCauseList.xlsx",
                                     "london-circuit-commercial-court-kb-daily-cause-list/"
                             + "londonCircuitCommercialCourtKbDailyCauseList.json",
                                     RCJ_TIME_FIELD, RCJ_CASE_NUMBER_FIELD, CASE_NAME_FIELD
            ),

            new ListTestCaseSettings(ListType.PATENTS_COURT_CHD_DAILY_CAUSE_LIST,
                                     "patents-court-chd-daily-cause-list/patentsCourtChdDailyCauseList.xlsx",
                                     "patents-court-chd-daily-cause-list/patentsCourtChdDailyCauseList.json",
                                     RCJ_TIME_FIELD, RCJ_CASE_NUMBER_FIELD, CASE_NAME_FIELD
            ),

            new ListTestCaseSettings(ListType.PENSIONS_LIST_CHD_DAILY_CAUSE_LIST,
                                     "pensions-list-chd-daily-cause-list/pensionsListChdDailyCauseList.xlsx",
                                     "pensions-list-chd-daily-cause-list/pensionsListChdDailyCauseList.json",
                                     RCJ_TIME_FIELD, RCJ_CASE_NUMBER_FIELD, CASE_NAME_FIELD
            ),

            new ListTestCaseSettings(ListType.PROPERTY_TRUSTS_PROBATE_LIST_CHD_DAILY_CAUSE_LIST,
                                     "property-trusts-probate-list-chd-daily-cause-list/"
                             + "propertyTrustsProbateListChdDailyCauseList.xlsx",
                                     "property-trusts-probate-list-chd-daily-cause-list/"
                             + "propertyTrustsProbateListChdDailyCauseList.json",
                                     RCJ_TIME_FIELD, RCJ_CASE_NUMBER_FIELD, CASE_NAME_FIELD
            ),

            new ListTestCaseSettings(ListType.REVENUE_LIST_CHD_DAILY_CAUSE_LIST,
                                     "revenue-list-chd-daily-cause-list/revenueListChdDailyCauseList.xlsx",
                                     "revenue-list-chd-daily-cause-list/revenueListChdDailyCauseList.json",
                                     RCJ_TIME_FIELD, RCJ_CASE_NUMBER_FIELD, CASE_NAME_FIELD
            ),

            new ListTestCaseSettings(ListType.TECHNOLOGY_AND_CONSTRUCTION_COURT_KB_DAILY_CAUSE_LIST,
                                     "technology-and-construction-court-kb-daily-cause-list/"
                             + "technologyAndConstructionCourtKbDailyCauseList.xlsx",
                                     "technology-and-construction-court-kb-daily-cause-list/"
                             + "technologyAndConstructionCourtKbDailyCauseList.json",
                                     RCJ_TIME_FIELD, RCJ_CASE_NUMBER_FIELD, CASE_NAME_FIELD
            ),

            new ListTestCaseSettings(ListType.ADMIRALTY_COURT_KB_DAILY_CAUSE_LIST,
                                     "admiralty_court_kb_daily_cause_list/admiraltyCourtKbDailyCauseList.xlsx",
                                     "admiralty_court_kb_daily_cause_list/admiraltyCourtKbDailyCauseList.json",
                                     RCJ_TIME_FIELD, RCJ_CASE_NUMBER_FIELD, CASE_NAME_FIELD
            ),

            new ListTestCaseSettings(ListType.BUSINESS_LIST_CHD_DAILY_CAUSE_LIST,
                                     "business_list_chd_daily_cause_list/businessListChdDailyCauseList.xlsx",
                                     "business_list_chd_daily_cause_list/businessListChdDailyCauseList.json",
                                     RCJ_TIME_FIELD, RCJ_CASE_NUMBER_FIELD, CASE_NAME_FIELD
            ),

            new ListTestCaseSettings(ListType.CHANCERY_APPEALS_CHD_DAILY_CAUSE_LIST,
                                     "chancery_appeals_chd_daily_cause_list/chanceryAppealsChdDailyCauseList.xlsx",
                                     "chancery_appeals_chd_daily_cause_list/chanceryAppealsChdDailyCauseList.json",
                                     RCJ_TIME_FIELD, RCJ_CASE_NUMBER_FIELD, CASE_NAME_FIELD
            ),

            new ListTestCaseSettings(ListType.COMMERCIAL_COURT_KB_DAILY_CAUSE_LIST,
                                     "commercial_court_kb_daily_cause_list/commercialCourtKbDailyCauseList.xlsx",
                                     "commercial_court_kb_daily_cause_list/commercialCourtKbDailyCauseList.json",
                                     RCJ_TIME_FIELD, RCJ_CASE_NUMBER_FIELD, CASE_NAME_FIELD
            ),

            new ListTestCaseSettings(ListType.COMPANIES_WINDING_UP_CHD_DAILY_CAUSE_LIST,
                                     "companies_winding_up_chd_daily_cause_list/"
                                         + "companiesWindingUpChdDailyCauseList.xlsx",
                                     "companies_winding_up_chd_daily_cause_list/"
                                         + "companiesWindingUpChdDailyCauseList.json",
                                     RCJ_TIME_FIELD, RCJ_CASE_NUMBER_FIELD, CASE_NAME_FIELD
            ),

            new ListTestCaseSettings(ListType.COMPETITION_LIST_CHD_DAILY_CAUSE_LIST,
                                     "competition_list_chd_daily_cause_list/competitionListChdDailyCauseList.xlsx",
                                     "competition_list_chd_daily_cause_list/competitionListChdDailyCauseList.json",
                                     RCJ_TIME_FIELD, RCJ_CASE_NUMBER_FIELD, CASE_NAME_FIELD
            ),

            new ListTestCaseSettings(ListType.FINANCIAL_LIST_CHD_KB_DAILY_CAUSE_LIST,
                                     "financial_list_chd_kb_daily_cause_list/financialListChdKbDailyCauseList.xlsx",
                                     "financial_list_chd_kb_daily_cause_list/financialListChdKbDailyCauseList.json",
                                     RCJ_TIME_FIELD, RCJ_CASE_NUMBER_FIELD, CASE_NAME_FIELD
            ),

            new ListTestCaseSettings(ListType.INSOLVENCY_AND_COMPANIES_COURT_CHD_DAILY_CAUSE_LIST,
                                     "insolvency_and_companies_court_chd_daily_cause_list/"
                             + "insolvencyAndCompaniesCourtChdDailyCauseList.xlsx",
                                     "insolvency_and_companies_court_chd_daily_cause_list/"
                             + "insolvencyAndCompaniesCourtChdDailyCauseList.json",
                                     RCJ_TIME_FIELD, RCJ_CASE_NUMBER_FIELD, CASE_NAME_FIELD
            ),

            new ListTestCaseSettings(ListType.BIRMINGHAM_ADMINISTRATIVE_COURT_DAILY_CAUSE_LIST,
                                     ADMINISTRATIVE_COURT_DAILY_CAUSE_LISTS_EXCEL_FILE,
                                     ADMINISTRATIVE_COURT_DAILY_CAUSE_LISTS_JSON_FILE,
                                     TIME_FIELD, CASE_NUMBER_FIELD,
                                     HEARING_TYPE_FIELD, CASE_DETAILS_FIELD
            ),

            new ListTestCaseSettings(ListType.BRISTOL_AND_CARDIFF_ADMINISTRATIVE_COURT_DAILY_CAUSE_LIST,
                                     ADMINISTRATIVE_COURT_DAILY_CAUSE_LISTS_EXCEL_FILE,
                                     ADMINISTRATIVE_COURT_DAILY_CAUSE_LISTS_JSON_FILE,
                                     TIME_FIELD, CASE_NUMBER_FIELD,
                                     HEARING_TYPE_FIELD, CASE_DETAILS_FIELD
            ),

            new ListTestCaseSettings(ListType.LEEDS_ADMINISTRATIVE_COURT_DAILY_CAUSE_LIST,
                                     ADMINISTRATIVE_COURT_DAILY_CAUSE_LISTS_EXCEL_FILE,
                                     ADMINISTRATIVE_COURT_DAILY_CAUSE_LISTS_JSON_FILE,
                                     TIME_FIELD, CASE_NUMBER_FIELD,
                                     HEARING_TYPE_FIELD, CASE_DETAILS_FIELD
            ),

            new ListTestCaseSettings(ListType.MANCHESTER_ADMINISTRATIVE_COURT_DAILY_CAUSE_LIST,
                                     ADMINISTRATIVE_COURT_DAILY_CAUSE_LISTS_EXCEL_FILE,
                                     ADMINISTRATIVE_COURT_DAILY_CAUSE_LISTS_JSON_FILE,
                                     TIME_FIELD, CASE_NUMBER_FIELD,
                                     HEARING_TYPE_FIELD, CASE_DETAILS_FIELD
            )
        );
    }

    private RcjListTestCases() {
    }
}
