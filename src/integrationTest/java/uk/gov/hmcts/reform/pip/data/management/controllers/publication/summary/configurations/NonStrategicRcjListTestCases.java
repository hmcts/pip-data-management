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
                ListType.COURT_OF_APPEAL_CIVIL_DAILY_CAUSE_LIST,
                "court-of-appeal-civil-daily-cause-list/courtOfAppealCivilDailyCauseList.xlsx",
                "court-of-appeal-civil-daily-cause-list/courtOfAppealCivilDailyCauseList.json",
                List.of(RCJ_TIME_FIELD, RCJ_CASE_NUMBER_FIELD, CASE_DETAILS_FIELD)
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
            ),

            new PublicationSummaryTestInput(
                ListType.BUSINESS_AND_PROPERTY_DIVISION_ROLLS_BUILDING_DAILY_CAUSE_LIST,
                "business-and-property-divisions-rolls-building-daily-cause-list/businessAndPropertyDivisionRollsBuildingDailyCauseList.xlsx",
                "business-and-property-divisions-rolls-building-daily-cause-list/businessAndPropertyDivisionRollsBuildingDailyCauseList.json",
                List.of(RCJ_TIME_FIELD, RCJ_CASE_NUMBER_FIELD, CASE_NAME_FIELD)
            )
        );
    }

    private NonStrategicRcjListTestCases() {
    }
}
