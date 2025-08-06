package uk.gov.hmcts.reform.pip.data.management.controllers.publication.summary.configurations;

import uk.gov.hmcts.reform.pip.model.publication.ListType;

import java.util.List;
import java.util.stream.Stream;

public final class PublicationSummaryTestCases {
    private static final String CASE_REFERENCE_FIELD = "Case reference - 12341234";
    private static final String CASE_NAME_FIELD = "Case name - This is a case name";
    private static final String HEARING_TYPE_FIELD = "Hearing type - Directions";

    private static final String SJP_MOCK = "data/sjp-public-list/sjpPublicList.json";
    private static final String SJP_PRESS_MOCK = "data/sjp-press-list/sjpPressList.json";

    public static Stream<ListTestCaseSettings> providePublicationSummaryTestCases() {
        return Stream.of(
            ListTestCaseSettings.withoutExcel(
                ListType.CIVIL_AND_FAMILY_DAILY_CAUSE_LIST,
                "data/civil-and-family-cause-list/civilAndFamilyDailyCauseList.json",
                List.of("Applicant - Surname",
                CASE_REFERENCE_FIELD,
                CASE_NAME_FIELD,
                "Case type - Case type",
                HEARING_TYPE_FIELD)
            ),
            ListTestCaseSettings.withoutExcel(
                ListType.CIVIL_DAILY_CAUSE_LIST,
                "data/civil-daily-cause-list/civilDailyCauseList.json",
                List.of("Case reference - 45684548",
                CASE_NAME_FIELD,
                "Case type - Case Type",
                "Hearing type - Hearing Type")
            ),
            ListTestCaseSettings.withoutExcel(
                ListType.COP_DAILY_CAUSE_LIST,
                "data/cop-daily-cause-list/copDailyCauseList.json",
                List.of(CASE_REFERENCE_FIELD,
                "Case details - ThisIsACaseSuppressionName",
                "Hearing type - Criminal")
            ),
            ListTestCaseSettings.withoutExcel(
                ListType.CROWN_DAILY_LIST,
                "data/crown-daily-list/crownDailyList.json",
                List.of("Defendant - Surname 1, Forename 1",
                "Prosecuting authority - Pro_Auth",
                "Case reference - 1234",
                HEARING_TYPE_FIELD)
            ),
            ListTestCaseSettings.withoutExcel(
                ListType.CROWN_FIRM_LIST,
                "data/crown-firm-list/crownFirmList.json",
                List.of("Defendant - Surname 1, Forename 1",
                "Prosecuting authority - Prosecutor org",
                "Case reference - 1234",
                HEARING_TYPE_FIELD)
            ),
            ListTestCaseSettings.withoutExcel(
                ListType.CROWN_WARNED_LIST,
                "data/crown-warned-list/crownWarnedList.json",
                List.of("Defendant - Surname, Forenames",
                "Prosecuting authority - OrganisationName2",
                CASE_REFERENCE_FIELD,
                "Hearing date - 21/07/2024")
            ),
            ListTestCaseSettings.withoutExcel(
                ListType.ET_DAILY_LIST,
                "data/et-daily-list/etDailyList.json",
                List.of("Claimant - Claimant surname",
                "Respondent - Capt. T Test Surname",
                CASE_REFERENCE_FIELD,
                "Hearing type - This is a hearing type")
            ),
            ListTestCaseSettings.withoutExcel(
                ListType.ET_FORTNIGHTLY_PRESS_LIST,
                "data/et-fortnightly-press-list/etFortnightlyPressList.json",
                List.of("Claimant - Ms T Test",
                "Respondent - Lord T Test Surname",
                CASE_REFERENCE_FIELD,
                "Hearing type - Hearing Type 1")
            ),
            ListTestCaseSettings.withoutExcel(
                ListType.FAMILY_DAILY_CAUSE_LIST,
                "data/family-daily-cause-list/familyDailyCauseList.json",
                List.of("Applicant - Applicant surname",
                CASE_REFERENCE_FIELD,
                CASE_NAME_FIELD,
                "Case type - Case type",
                HEARING_TYPE_FIELD)
            ),
            ListTestCaseSettings.withoutExcel(
                ListType.IAC_DAILY_LIST,
                "data/iac-daily-list/iacDailyList.json",
                List.of("Bail List",
                "Appellant/Applicant - Surname",
                "Prosecuting authority - Authority surname",
                CASE_REFERENCE_FIELD)
            ),
            ListTestCaseSettings.withoutExcel(
                ListType.IAC_DAILY_LIST_ADDITIONAL_CASES,
                "data/iac-daily-list/iacDailyList.json",
                List.of("Bail List",
                "Appellant/Applicant - Surname",
                "Prosecuting authority - Authority surname",
                CASE_REFERENCE_FIELD)
            ),
            ListTestCaseSettings.withoutExcel(
                ListType.MAGISTRATES_PUBLIC_LIST,
                "data/magistrates-public-list/magistratesPublicList.json",
                List.of("Defendant - Surname, Forename",
                "Prosecuting authority - Authority org name",
                CASE_REFERENCE_FIELD,
                HEARING_TYPE_FIELD)
            ),
            ListTestCaseSettings.withoutExcel(
                ListType.MAGISTRATES_STANDARD_LIST,
                "data/magistrates-standard-list/magistratesStandardList.json",
                List.of("Defendant - Surname1, Forename1",
                "Prosecuting authority - Test1234",
                "Case reference - 45684548",
                "Hearing type - mda",
                "Offence - drink driving, Assault by beating")
            ),
            ListTestCaseSettings.withoutExcel(
                ListType.SJP_PUBLIC_LIST,
                SJP_MOCK,
                List.of("")
            ),
            ListTestCaseSettings.withoutExcel(
                ListType.SJP_DELTA_PUBLIC_LIST,
                SJP_MOCK,
                List.of("")
            ),
            ListTestCaseSettings.withoutExcel(
                ListType.SJP_PRESS_LIST,
                SJP_PRESS_MOCK,
                List.of("")
            ),
            ListTestCaseSettings.withoutExcel(
                ListType.SJP_DELTA_PRESS_LIST,
                SJP_PRESS_MOCK,
                List.of("")
            ),
            ListTestCaseSettings.withoutExcel(
                ListType.SSCS_DAILY_LIST,
                "data/sscs-daily-list/sscsDailyList.json",
                List.of("Appellant - Surname",
                "Respondent - Respondent Organisation, Respondent Organisation 2",
                "Case reference - 12341235")
            ),
            ListTestCaseSettings.withoutExcel(
                ListType.SSCS_DAILY_LIST_ADDITIONAL_HEARINGS,
                "data/sscs-daily-list/sscsDailyList.json",
                List.of("Appellant - Surname",
                "Respondent - Respondent Organisation, Respondent Organisation 2",
                "Case reference - 12341235")
            )
        );
    }

    private PublicationSummaryTestCases() {
    }
}
