package uk.gov.hmcts.reform.pip.data.management.controllers.publication.summary.configurations;

import uk.gov.hmcts.reform.pip.model.publication.ListType;

import java.util.List;
import java.util.stream.Stream;

public final class NonStrategicTribunalListTestCases {
    private static final String TIME_FIELD = "Time - 10am";
    private static final String HEARING_TYPE_FIELD = "Hearing type - Directions";
    private static final String CASE_NAME_FIELD =
        "Case name - This is a case name";
    private static final String CASE_REFERENCE_NUMBER_FIELD =
        "Case reference number - 1234";
    private static final String HEARING_TIME_FIELD =
        "Hearing time - 10am";
    private static final String DATE_FIELD =
        "Date - 16 December 2024";
    private static final String APPEAL_REFERENCE_NUMBER_FIELD =
        "Appeal reference number - 1234567";
    private static final String HEARING_TIME_FIELD_2 =
        "Hearing time - 10:30am";

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

    public static Stream<ListTestCaseSettings> provideTribunalTestCases() {
        return Stream.of(
            new ListTestCaseSettings(
                ListType.CST_WEEKLY_HEARING_LIST,
                "cst-weekly-hearing-list/cstWeeklyHearingList.xlsx",
                "cst-weekly-hearing-list/cstWeeklyHearingList.json",
                List.of("Date - 10 December 2024", CASE_NAME_FIELD)
            ),
            new ListTestCaseSettings(
                ListType.PHT_WEEKLY_HEARING_LIST,
                "pht-weekly-hearing-list/phtWeeklyHearingList.xlsx",
                "pht-weekly-hearing-list/phtWeeklyHearingList.json",
                List.of("Date - 10 December 2024", CASE_NAME_FIELD)
            ),
            new ListTestCaseSettings(
                ListType.GRC_WEEKLY_HEARING_LIST,
                "grc-weekly-hearing-list/grcWeeklyHearingList.xlsx",
                "grc-weekly-hearing-list/grcWeeklyHearingList.json",
                List.of(DATE_FIELD, HEARING_TIME_FIELD, CASE_REFERENCE_NUMBER_FIELD)
            ),
            new ListTestCaseSettings(
                ListType.WPAFCC_WEEKLY_HEARING_LIST,
                "wpafcc-weekly-hearing-list/wpafccWeeklyHearingList.xlsx",
                "wpafcc-weekly-hearing-list/wpafccWeeklyHearingList.json",
                List.of(DATE_FIELD, HEARING_TIME_FIELD, CASE_REFERENCE_NUMBER_FIELD)
            ),
            new ListTestCaseSettings(
                ListType.UT_IAC_JR_LONDON_DAILY_HEARING_LIST,
                "ut-iac-judicial-review-london-daily-hearing-list/utIacJudicialReviewLondonDailyHearingList.xlsx",
                "ut-iac-judicial-review-london-daily-hearing-list/utIacJudicialReviewLondonDailyHearingList.json",
                List.of(HEARING_TIME_FIELD_2, CASE_REFERENCE_NUMBER_FIELD)
            ),
            new ListTestCaseSettings(
                ListType.UT_IAC_JR_LEEDS_DAILY_HEARING_LIST,
                UT_IAC_JR_LISTS_EXCEL_FILE,
                UT_IAC_JR_LISTS_JSON_FILE,
                List.of(HEARING_TIME_FIELD_2, CASE_REFERENCE_NUMBER_FIELD)
            ),
            new ListTestCaseSettings(
                ListType.UT_IAC_JR_MANCHESTER_DAILY_HEARING_LIST,
                UT_IAC_JR_LISTS_EXCEL_FILE,
                UT_IAC_JR_LISTS_JSON_FILE,
                List.of(HEARING_TIME_FIELD_2, CASE_REFERENCE_NUMBER_FIELD)
            ),
            new ListTestCaseSettings(
                ListType.UT_IAC_JR_BIRMINGHAM_DAILY_HEARING_LIST,
                UT_IAC_JR_LISTS_EXCEL_FILE,
                UT_IAC_JR_LISTS_JSON_FILE,
                List.of(HEARING_TIME_FIELD_2, CASE_REFERENCE_NUMBER_FIELD)
            ),
            new ListTestCaseSettings(
                ListType.UT_IAC_JR_CARDIFF_DAILY_HEARING_LIST,
                UT_IAC_JR_LISTS_EXCEL_FILE,
                UT_IAC_JR_LISTS_JSON_FILE,
                List.of(HEARING_TIME_FIELD_2, CASE_REFERENCE_NUMBER_FIELD)
            ),
            new ListTestCaseSettings(
                ListType.UT_IAC_STATUTORY_APPEALS_DAILY_HEARING_LIST,
                "ut-iac-statutory-appeals-daily-hearing-list/utIacStatutoryAppealsDailyHearingList.xlsx",
                "ut-iac-statutory-appeals-daily-hearing-list/utIacStatutoryAppealsDailyHearingList.json",
                List.of(HEARING_TIME_FIELD_2, "Appeal reference number - 1234")
            ),
            new ListTestCaseSettings(
                ListType.UT_AAC_DAILY_HEARING_LIST,
                "ut-administrative-appeals-chamber-daily-hearing-list/"
                    + "utAdministrativeAppealsChamberDailyHearingList.xlsx",
                "ut-administrative-appeals-chamber-daily-hearing-list/"
                    + "utAdministrativeAppealsChamberDailyHearingList.json",
                List.of(TIME_FIELD, CASE_REFERENCE_NUMBER_FIELD, "Appellant - Appellant 1")
            ),
            new ListTestCaseSettings(
                ListType.UT_LC_DAILY_HEARING_LIST,
                "ut-lands-chamber-daily-hearing-list/utLandsChamberDailyHearingList.xlsx",
                "ut-lands-chamber-daily-hearing-list/utLandsChamberDailyHearingList.json",
                List.of(TIME_FIELD, CASE_REFERENCE_NUMBER_FIELD, CASE_NAME_FIELD)
            ),
            new ListTestCaseSettings(
                ListType.UT_T_AND_CC_DAILY_HEARING_LIST,
                "ut-tax-and-chancery-chamber-daily-hearing-list/utTaxAndChanceryChamberDailyHearingList.xlsx",
                "ut-tax-and-chancery-chamber-daily-hearing-list/utTaxAndChanceryChamberDailyHearingList.json",
                List.of(TIME_FIELD, CASE_REFERENCE_NUMBER_FIELD, CASE_NAME_FIELD)
            ),
            new ListTestCaseSettings(
                ListType.SIAC_WEEKLY_HEARING_LIST,
                SIAC_LISTS_EXCEL_FILE,
                SIAC_LISTS_JSON_FILE,
                List.of("Date - 11 December 2024", TIME_FIELD, "Case reference number - 123451")
            ),
            new ListTestCaseSettings(
                ListType.POAC_WEEKLY_HEARING_LIST,
                SIAC_LISTS_EXCEL_FILE,
                SIAC_LISTS_JSON_FILE,
                List.of("Date - 11 December 2024", TIME_FIELD, "Case reference number - 123451")
            ),
            new ListTestCaseSettings(
                ListType.PAAC_WEEKLY_HEARING_LIST,
                SIAC_LISTS_EXCEL_FILE,
                SIAC_LISTS_JSON_FILE,
                List.of("Date - 11 December 2024", TIME_FIELD, "Case reference number - 123451")
            ),
            new ListTestCaseSettings(
                ListType.SSCS_MIDLANDS_DAILY_HEARING_LIST,
                SSCS_LISTS_EXCEL_FILE,
                SSCS_LISTS_JSON_FILE,
                List.of(HEARING_TIME_FIELD, HEARING_TYPE_FIELD, APPEAL_REFERENCE_NUMBER_FIELD)
            ),
            new ListTestCaseSettings(
                ListType.SSCS_SOUTH_EAST_DAILY_HEARING_LIST,
                SSCS_LISTS_EXCEL_FILE,
                SSCS_LISTS_JSON_FILE,
                List.of(HEARING_TIME_FIELD, HEARING_TYPE_FIELD, APPEAL_REFERENCE_NUMBER_FIELD)
            ),
            new ListTestCaseSettings(
                ListType.SSCS_WALES_AND_SOUTH_WEST_DAILY_HEARING_LIST,
                SSCS_LISTS_EXCEL_FILE,
                SSCS_LISTS_JSON_FILE,
                List.of(HEARING_TIME_FIELD, HEARING_TYPE_FIELD, APPEAL_REFERENCE_NUMBER_FIELD)
            ),
            new ListTestCaseSettings(
                ListType.SSCS_SCOTLAND_DAILY_HEARING_LIST,
                SSCS_LISTS_EXCEL_FILE,
                SSCS_LISTS_JSON_FILE,
                List.of(HEARING_TIME_FIELD, HEARING_TYPE_FIELD, APPEAL_REFERENCE_NUMBER_FIELD)
            ),
            new ListTestCaseSettings(
                ListType.SSCS_NORTH_EAST_DAILY_HEARING_LIST,
                SSCS_LISTS_EXCEL_FILE,
                SSCS_LISTS_JSON_FILE,
                List.of(HEARING_TIME_FIELD, HEARING_TYPE_FIELD, APPEAL_REFERENCE_NUMBER_FIELD)
            ),
            new ListTestCaseSettings(
                ListType.SSCS_NORTH_WEST_DAILY_HEARING_LIST,
                SSCS_LISTS_EXCEL_FILE,
                SSCS_LISTS_JSON_FILE,
                List.of(HEARING_TIME_FIELD, HEARING_TYPE_FIELD, APPEAL_REFERENCE_NUMBER_FIELD)
            ),
            new ListTestCaseSettings(
                ListType.SSCS_LONDON_DAILY_HEARING_LIST,
                SSCS_LISTS_EXCEL_FILE,
                SSCS_LISTS_JSON_FILE,
                List.of(HEARING_TIME_FIELD, HEARING_TYPE_FIELD, APPEAL_REFERENCE_NUMBER_FIELD)
            ),
            new ListTestCaseSettings(
                ListType.SEND_DAILY_HEARING_LIST,
                "send-daily-hearing-list/sendDailyHearingList.xlsx",
                "send-daily-hearing-list/sendDailyHearingList.json",
                List.of(TIME_FIELD, CASE_REFERENCE_NUMBER_FIELD, "Venue - Venue A")
            ),
            new ListTestCaseSettings(
                ListType.CIC_WEEKLY_HEARING_LIST,
                "cic-weekly-hearing-list/cicWeeklyHearingList.xlsx",
                "cic-weekly-hearing-list/cicWeeklyHearingList.json",
                List.of("Date - 26 June 2025",
                HEARING_TIME_FIELD,
                CASE_REFERENCE_NUMBER_FIELD,
                CASE_NAME_FIELD)
            ),
            new ListTestCaseSettings(
                ListType.AST_DAILY_HEARING_LIST,
                "ast-daily-hearing-list/astDailyHearingList.xlsx",
                "ast-daily-hearing-list/astDailyHearingList.json",
                List.of("Appellant - Appellant A", "Appeal reference number - 12345", HEARING_TIME_FIELD_2)
            ),
            new ListTestCaseSettings(
                ListType.RPT_EASTERN_WEEKLY_HEARING_LIST,
                RPT_LISTS_EXCEL_FILE,
                RPT_LISTS_JSON_FILE,
                List.of(DATE_FIELD, TIME_FIELD, CASE_REFERENCE_NUMBER_FIELD)
            ),
            new ListTestCaseSettings(
                ListType.RPT_LONDON_WEEKLY_HEARING_LIST,
                RPT_LISTS_EXCEL_FILE,
                RPT_LISTS_JSON_FILE,
                List.of(DATE_FIELD, TIME_FIELD, CASE_REFERENCE_NUMBER_FIELD)
            ),
            new ListTestCaseSettings(
                ListType.RPT_MIDLANDS_WEEKLY_HEARING_LIST,
                RPT_LISTS_EXCEL_FILE,
                RPT_LISTS_JSON_FILE,
                List.of(DATE_FIELD, TIME_FIELD, CASE_REFERENCE_NUMBER_FIELD)
            ),
            new ListTestCaseSettings(
                ListType.RPT_NORTHERN_WEEKLY_HEARING_LIST,
                RPT_LISTS_EXCEL_FILE,
                RPT_LISTS_JSON_FILE,
                List.of(DATE_FIELD, TIME_FIELD, CASE_REFERENCE_NUMBER_FIELD)
            ),
            new ListTestCaseSettings(
                ListType.RPT_SOUTHERN_WEEKLY_HEARING_LIST,
                RPT_LISTS_EXCEL_FILE,
                RPT_LISTS_JSON_FILE,
                List.of(DATE_FIELD, TIME_FIELD, CASE_REFERENCE_NUMBER_FIELD)
            ),
            new ListTestCaseSettings(
                ListType.FTT_TAX_WEEKLY_HEARING_LIST,
                "ftt-tax-tribunal-weekly-hearing-list/fttTaxWeeklyHearingList.xlsx",
                "ftt-tax-tribunal-weekly-hearing-list/fttTaxWeeklyHearingList.json",
                List.of(DATE_FIELD, HEARING_TIME_FIELD, CASE_REFERENCE_NUMBER_FIELD)
            ),
            new ListTestCaseSettings(
                ListType.FTT_LR_WEEKLY_HEARING_LIST,
                "ftt-land-registry-tribunal-weekly-hearing-list/fttLandRegistryTribunalWeeklyHearingList.xlsx",
                "ftt-land-registry-tribunal-weekly-hearing-list/fttLandRegistryTribunalWeeklyHearingList.json",
                List.of(DATE_FIELD, HEARING_TIME_FIELD, CASE_REFERENCE_NUMBER_FIELD)
            )
        );
    }

    private NonStrategicTribunalListTestCases() {
    }
}
