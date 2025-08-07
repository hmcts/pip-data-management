package uk.gov.hmcts.reform.pip.data.management.controllers.publication.summary.configurations;

import uk.gov.hmcts.reform.pip.model.publication.ListType;

import java.util.List;
import java.util.stream.Stream;

public final class NonStrategicRcjListTestCases {
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

    public static Stream<PublicationSummaryTestInput> provideRcjTestCases() {
        return Stream.of(
            new PublicationSummaryTestInput(
                ListType.LONDON_ADMINISTRATIVE_COURT_DAILY_CAUSE_LIST,
                "london-administrative-court-daily-cause-list/londonAdministrativeCourtDailyCauseList.xlsx",
                "london-administrative-court-daily-cause-list/londonAdministrativeCourtDailyCauseList.json",
                List.of(RCJ_TIME_FIELD, RCJ_CASE_NUMBER_FIELD, CASE_DETAILS_FIELD)
            ),

            new PublicationSummaryTestInput(
                ListType.COUNTY_COURT_LONDON_CIVIL_DAILY_CAUSE_LIST,
                "county-court-london-civil-daily-cause-list/countyCourtLondonCivilDailyCauseList.xlsx",
                "county-court-london-civil-daily-cause-list/countyCourtLondonCivilDailyCauseList.json",
                List.of(RCJ_TIME_FIELD, RCJ_CASE_NUMBER_FIELD, CASE_DETAILS_FIELD)
            ),

            new PublicationSummaryTestInput(
                ListType.CIVIL_COURTS_RCJ_DAILY_CAUSE_LIST,
                "civil-courts-rcj-daily-cause-list/civilCourtsRcjDailyCauseList.xlsx",
                "civil-courts-rcj-daily-cause-list/civilCourtsRcjDailyCauseList.json",
                List.of(RCJ_TIME_FIELD, RCJ_CASE_NUMBER_FIELD, CASE_DETAILS_FIELD)
            ),

            new PublicationSummaryTestInput(
                ListType.COURT_OF_APPEAL_CRIMINAL_DAILY_CAUSE_LIST,
                "court-of-appeal-criminal-daily-cause-list/courtOfAppealCriminalDailyCauseList.xlsx",
                "court-of-appeal-criminal-daily-cause-list/courtOfAppealCriminalDailyCauseList.json",
                List.of(RCJ_TIME_FIELD, RCJ_CASE_NUMBER_FIELD, CASE_DETAILS_FIELD)
            ),

            new PublicationSummaryTestInput(
                ListType.FAMILY_DIVISION_HIGH_COURT_DAILY_CAUSE_LIST,
                "family-division-high-court-daily-cause-list/familyDivisionHighCourtDailyCauseList.xlsx",
                "family-division-high-court-daily-cause-list/familyDivisionHighCourtDailyCauseList.json",
                List.of(RCJ_TIME_FIELD, RCJ_CASE_NUMBER_FIELD, CASE_DETAILS_FIELD)
            ),

            new PublicationSummaryTestInput(
                ListType.KINGS_BENCH_DIVISION_DAILY_CAUSE_LIST,
                "kings-bench-division-daily-cause-list/kingsBenchDivisionDailyCauseList.xlsx",
                "kings-bench-division-daily-cause-list/kingsBenchDivisionDailyCauseList.json",
                List.of(RCJ_TIME_FIELD, RCJ_CASE_NUMBER_FIELD, CASE_DETAILS_FIELD)
            ),

            new PublicationSummaryTestInput(
                ListType.KINGS_BENCH_MASTERS_DAILY_CAUSE_LIST,
                "kings-bench-masters-daily-cause-list/kingsBenchMastersDailyCauseList.xlsx",
                "kings-bench-masters-daily-cause-list/kingsBenchMastersDailyCauseList.json",
                List.of(RCJ_TIME_FIELD, RCJ_CASE_NUMBER_FIELD, CASE_DETAILS_FIELD)
            ),

            new PublicationSummaryTestInput(
                ListType.SENIOR_COURTS_COSTS_OFFICE_DAILY_CAUSE_LIST,
                "senior-courts-costs-office-daily-cause-list/seniorCourtsCostsOfficeDailyCauseList.xlsx",
                "senior-courts-costs-office-daily-cause-list/seniorCourtsCostsOfficeDailyCauseList.json",
                List.of(RCJ_TIME_FIELD, RCJ_CASE_NUMBER_FIELD, CASE_DETAILS_FIELD)
            ),

            new PublicationSummaryTestInput(
                ListType.MAYOR_AND_CITY_CIVIL_DAILY_CAUSE_LIST,
                "mayor-and-city-civil-daily-cause-list/mayorAndCityCivilDailyCauseList.xlsx",
                "mayor-and-city-civil-daily-cause-list/mayorAndCityCivilDailyCauseList.json",
                List.of(RCJ_TIME_FIELD, RCJ_CASE_NUMBER_FIELD, CASE_DETAILS_FIELD)
            ),

            new PublicationSummaryTestInput(
                ListType.INTELLECTUAL_PROPERTY_AND_ENTERPRISE_COURT_DAILY_CAUSE_LIST,
                "intellectual-property-and-enterprise-court-daily-cause-list/"
                    + "intellectualPropertyAndEnterpriseCourtDailyCauseList.xlsx",
                "intellectual-property-and-enterprise-court-daily-cause-list/"
                    + "intellectualPropertyAndEnterpriseCourtDailyCauseList.json",
                List.of(RCJ_TIME_FIELD, RCJ_CASE_NUMBER_FIELD, CASE_NAME_FIELD)
            ),

            new PublicationSummaryTestInput(
                ListType.COURT_OF_APPEAL_CIVIL_DAILY_CAUSE_LIST,
                "court-of-appeal-civil-daily-cause-list/courtOfAppealCivilDailyCauseList.xlsx",
                "court-of-appeal-civil-daily-cause-list/courtOfAppealCivilDailyCauseList.json",
                List.of(RCJ_TIME_FIELD, RCJ_CASE_NUMBER_FIELD, CASE_DETAILS_FIELD)
            ),

            new PublicationSummaryTestInput(
                ListType.INTELLECTUAL_PROPERTY_LIST_CHD_DAILY_CAUSE_LIST,
                "intellectual-property-list-chd-daily-cause-list/intellectualPropertyListChdDailyCauseList.xlsx",
                "intellectual-property-list-chd-daily-cause-list/intellectualPropertyListChdDailyCauseList.json",
                List.of(RCJ_TIME_FIELD, RCJ_CASE_NUMBER_FIELD, CASE_NAME_FIELD)
            ),

            new PublicationSummaryTestInput(
                ListType.LONDON_CIRCUIT_COMMERCIAL_COURT_KB_DAILY_CAUSE_LIST,
                "london-circuit-commercial-court-kb-daily-cause-list/"
                    + "londonCircuitCommercialCourtKbDailyCauseList.xlsx",
                "london-circuit-commercial-court-kb-daily-cause-list/"
                    + "londonCircuitCommercialCourtKbDailyCauseList.json",
                List.of(RCJ_TIME_FIELD, RCJ_CASE_NUMBER_FIELD, CASE_NAME_FIELD)
            ),

            new PublicationSummaryTestInput(
                ListType.PATENTS_COURT_CHD_DAILY_CAUSE_LIST,
                "patents-court-chd-daily-cause-list/patentsCourtChdDailyCauseList.xlsx",
                "patents-court-chd-daily-cause-list/patentsCourtChdDailyCauseList.json",
                List.of(RCJ_TIME_FIELD, RCJ_CASE_NUMBER_FIELD, CASE_NAME_FIELD)
            ),

            new PublicationSummaryTestInput(
                ListType.PENSIONS_LIST_CHD_DAILY_CAUSE_LIST,
                "pensions-list-chd-daily-cause-list/pensionsListChdDailyCauseList.xlsx",
                "pensions-list-chd-daily-cause-list/pensionsListChdDailyCauseList.json",
                List.of(RCJ_TIME_FIELD, RCJ_CASE_NUMBER_FIELD, CASE_NAME_FIELD)
            ),

            new PublicationSummaryTestInput(
                ListType.PROPERTY_TRUSTS_PROBATE_LIST_CHD_DAILY_CAUSE_LIST,
                "property-trusts-probate-list-chd-daily-cause-list/propertyTrustsProbateListChdDailyCauseList.xlsx",
                "property-trusts-probate-list-chd-daily-cause-list/propertyTrustsProbateListChdDailyCauseList.json",
                List.of(RCJ_TIME_FIELD, RCJ_CASE_NUMBER_FIELD, CASE_NAME_FIELD)
            ),

            new PublicationSummaryTestInput(
                ListType.REVENUE_LIST_CHD_DAILY_CAUSE_LIST,
                "revenue-list-chd-daily-cause-list/revenueListChdDailyCauseList.xlsx",
                "revenue-list-chd-daily-cause-list/revenueListChdDailyCauseList.json",
                List.of(RCJ_TIME_FIELD, RCJ_CASE_NUMBER_FIELD, CASE_NAME_FIELD)
            ),

            new PublicationSummaryTestInput(
                ListType.TECHNOLOGY_AND_CONSTRUCTION_COURT_KB_DAILY_CAUSE_LIST,
                "technology-and-construction-court-kb-daily-cause-list/"
                    + "technologyAndConstructionCourtKbDailyCauseList.xlsx",
                "technology-and-construction-court-kb-daily-cause-list/"
                    + "technologyAndConstructionCourtKbDailyCauseList.json",
                List.of(RCJ_TIME_FIELD, RCJ_CASE_NUMBER_FIELD, CASE_NAME_FIELD)
            ),

            new PublicationSummaryTestInput(
                ListType.ADMIRALTY_COURT_KB_DAILY_CAUSE_LIST,
                "admiralty-court-kb-daily-cause-list/admiraltyCourtKbDailyCauseList.xlsx",
                "admiralty-court-kb-daily-cause-list/admiraltyCourtKbDailyCauseList.json",
                List.of(RCJ_TIME_FIELD, RCJ_CASE_NUMBER_FIELD, CASE_NAME_FIELD)
            ),

            new PublicationSummaryTestInput(
                ListType.BUSINESS_LIST_CHD_DAILY_CAUSE_LIST,
                "business-list-chd-daily-cause-list/businessListChdDailyCauseList.xlsx",
                "business-list-chd-daily-cause-list/businessListChdDailyCauseList.json",
                List.of(RCJ_TIME_FIELD, RCJ_CASE_NUMBER_FIELD, CASE_NAME_FIELD)
            ),

            new PublicationSummaryTestInput(
                ListType.CHANCERY_APPEALS_CHD_DAILY_CAUSE_LIST,
                "chancery-appeals-chd-daily-cause-list/chanceryAppealsChdDailyCauseList.xlsx",
                "chancery-appeals-chd-daily-cause-list/chanceryAppealsChdDailyCauseList.json",
                List.of(RCJ_TIME_FIELD, RCJ_CASE_NUMBER_FIELD, CASE_NAME_FIELD)
            ),

            new PublicationSummaryTestInput(
                ListType.COMMERCIAL_COURT_KB_DAILY_CAUSE_LIST,
                "commercial-court-kb-daily-cause-list/commercialCourtKbDailyCauseList.xlsx",
                "commercial-court-kb-daily-cause-list/commercialCourtKbDailyCauseList.json",
                List.of(RCJ_TIME_FIELD, RCJ_CASE_NUMBER_FIELD, CASE_NAME_FIELD)
            ),

            new PublicationSummaryTestInput(
                ListType.COMPANIES_WINDING_UP_CHD_DAILY_CAUSE_LIST,
                "companies-winding-up-chd-daily-cause-list/companiesWindingUpChdDailyCauseList.xlsx",
                "companies-winding-up-chd-daily-cause-list/companiesWindingUpChdDailyCauseList.json",
                List.of(RCJ_TIME_FIELD, RCJ_CASE_NUMBER_FIELD, CASE_NAME_FIELD)
            ),

            new PublicationSummaryTestInput(
                ListType.COMPETITION_LIST_CHD_DAILY_CAUSE_LIST,
                "competition-list-chd-daily-cause-list/competitionListChdDailyCauseList.xlsx",
                "competition-list-chd-daily-cause-list/competitionListChdDailyCauseList.json",
                List.of(RCJ_TIME_FIELD, RCJ_CASE_NUMBER_FIELD, CASE_NAME_FIELD)
            ),

            new PublicationSummaryTestInput(
                ListType.FINANCIAL_LIST_CHD_KB_DAILY_CAUSE_LIST,
                "financial-list-chd-kb-daily-cause-list/financialListChdKbDailyCauseList.xlsx",
                "financial-list-chd-kb-daily-cause-list/financialListChdKbDailyCauseList.json",
                List.of(RCJ_TIME_FIELD, RCJ_CASE_NUMBER_FIELD, CASE_NAME_FIELD)
            ),

            new PublicationSummaryTestInput(
                ListType.INSOLVENCY_AND_COMPANIES_COURT_CHD_DAILY_CAUSE_LIST,
                "insolvency-and-companies-court-chd-daily-cause-list/"
                    + "insolvencyAndCompaniesCourtChdDailyCauseList.xlsx",
                "insolvency-and-companies-court-chd-daily-cause-list/"
                    + "insolvencyAndCompaniesCourtChdDailyCauseList.json",
                List.of(RCJ_TIME_FIELD, RCJ_CASE_NUMBER_FIELD, CASE_NAME_FIELD)
            ),

            new PublicationSummaryTestInput(
                ListType.BIRMINGHAM_ADMINISTRATIVE_COURT_DAILY_CAUSE_LIST,
                ADMINISTRATIVE_COURT_DAILY_CAUSE_LISTS_EXCEL_FILE,
                ADMINISTRATIVE_COURT_DAILY_CAUSE_LISTS_JSON_FILE,
                List.of(TIME_FIELD, CASE_NUMBER_FIELD, HEARING_TYPE_FIELD, CASE_DETAILS_FIELD)
            ),

            new PublicationSummaryTestInput(
                ListType.BRISTOL_AND_CARDIFF_ADMINISTRATIVE_COURT_DAILY_CAUSE_LIST,
                ADMINISTRATIVE_COURT_DAILY_CAUSE_LISTS_EXCEL_FILE,
                ADMINISTRATIVE_COURT_DAILY_CAUSE_LISTS_JSON_FILE,
                List.of(TIME_FIELD, CASE_NUMBER_FIELD, HEARING_TYPE_FIELD, CASE_DETAILS_FIELD)
            ),

            new PublicationSummaryTestInput(
                ListType.LEEDS_ADMINISTRATIVE_COURT_DAILY_CAUSE_LIST,
                ADMINISTRATIVE_COURT_DAILY_CAUSE_LISTS_EXCEL_FILE,
                ADMINISTRATIVE_COURT_DAILY_CAUSE_LISTS_JSON_FILE,
                List.of(TIME_FIELD, CASE_NUMBER_FIELD, HEARING_TYPE_FIELD, CASE_DETAILS_FIELD)
            ),

            new PublicationSummaryTestInput(
                ListType.MANCHESTER_ADMINISTRATIVE_COURT_DAILY_CAUSE_LIST,
                ADMINISTRATIVE_COURT_DAILY_CAUSE_LISTS_EXCEL_FILE,
                ADMINISTRATIVE_COURT_DAILY_CAUSE_LISTS_JSON_FILE,
                List.of(TIME_FIELD, CASE_NUMBER_FIELD, HEARING_TYPE_FIELD, CASE_DETAILS_FIELD)
            )
        );
    }

    private NonStrategicRcjListTestCases() {
    }
}
