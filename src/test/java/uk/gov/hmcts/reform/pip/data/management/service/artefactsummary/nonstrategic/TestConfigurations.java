package uk.gov.hmcts.reform.pip.data.management.service.artefactsummary.nonstrategic;

import uk.gov.hmcts.reform.pip.model.publication.ListType;

import java.util.List;
import java.util.stream.Stream;

public class TestConfigurations {

    public static Stream<ListTestCaseSettings> provideTestCases() {
        return Stream.of(
            new ListTestCaseSettings(
                "admiraltyCourtKbDailyCauseList.json",
                ListType.ADMIRALTY_COURT_KB_DAILY_CAUSE_LIST,
                1,
                2,
                3,
                List.of("Time", "Case number", "Case name"),
                List.of("9am", "12345", "Case name A")
            ),
            new ListTestCaseSettings(
                "astDailyHearingList.json",
                ListType.AST_DAILY_HEARING_LIST,
                1,
                2,
                3,
                List.of("Appellant", "Appeal reference number", "Hearing time"),
                List.of("Appellant A", "12345", "10:30am")
            ),
            new ListTestCaseSettings(
                "administrativeCourtDailyCauseList.json",
                ListType.BIRMINGHAM_ADMINISTRATIVE_COURT_DAILY_CAUSE_LIST,
                1,
                2,
                4,
                List.of("Time", "Case number", "Hearing type", "Case details"),
                List.of("10am", "12345", "Directions A", "Case details A")
            ),
            new ListTestCaseSettings(
                "administrativeCourtDailyCauseList.json",
                ListType.BRISTOL_AND_CARDIFF_ADMINISTRATIVE_COURT_DAILY_CAUSE_LIST,
                1,
                2,
                4,
                List.of("Time", "Case number", "Hearing type", "Case details"),
                List.of("10am", "12345", "Directions A", "Case details A")
            ),
            new ListTestCaseSettings(
                "businessListChdDailyCauseList.json",
                ListType.BUSINESS_LIST_CHD_DAILY_CAUSE_LIST,
                1,
                2,
                3,
                List.of("Time", "Case number", "Case name"),
                List.of("9am", "12345", "Case name A")
            ),
            new ListTestCaseSettings(
                "chanceryAppealsChdDailyCauseList.json",
                ListType.CHANCERY_APPEALS_CHD_DAILY_CAUSE_LIST,
                1,
                2,
                3,
                List.of("Time", "Case number", "Case name"),
                List.of("9am", "12345", "Case name A")
            ),
            new ListTestCaseSettings(
                "cicWeeklyHearingList.json",
                ListType.CIC_WEEKLY_HEARING_LIST,
                1,
                2,
                4,
                List.of("Date", "Hearing time", "Case reference number", "Case name"),
                List.of("26 June 2025", "10am", "1234", "This is a case name")
            ),
            new ListTestCaseSettings(
                "civilCourtsRcjDailyCauseList.json",
                ListType.CIVIL_COURTS_RCJ_DAILY_CAUSE_LIST,
                1,
                2,
                3,
                List.of("Time", "Case number", "Case details"),
                List.of("9am", "12345", "Case details A")
            ),
            new ListTestCaseSettings(
                "commercialCourtKbDailyCauseList.json",
                ListType.COMMERCIAL_COURT_KB_DAILY_CAUSE_LIST,
                1,
                2,
                3,
                List.of("Time", "Case number", "Case name"),
                List.of("9am", "12345", "Case name A")
            ),
            new ListTestCaseSettings(
                "companiesWindingUpChdDailyCauseList.json",
                ListType.COMPANIES_WINDING_UP_CHD_DAILY_CAUSE_LIST,
                1,
                2,
                3,
                List.of("Time", "Case number", "Case name"),
                List.of("9am", "12345", "Case name A")
            ),
            new ListTestCaseSettings(
                "competitionListChdDailyCauseList.json",
                ListType.COMPETITION_LIST_CHD_DAILY_CAUSE_LIST,
                1,
                2,
                3,
                List.of("Time", "Case number", "Case name"),
                List.of("9am", "12345", "Case name A")
            ),
            new ListTestCaseSettings(
                "countyCourtLondonCivilDailyCauseList.json",
                ListType.COUNTY_COURT_LONDON_CIVIL_DAILY_CAUSE_LIST,
                1,
                2,
                3,
                List.of("Time", "Case number", "Case details"),
                List.of("9am", "12345", "Case details A")
            ),
            new ListTestCaseSettings(
                "courtOfAppealCivilDailyCauseList.json",
                ListType.COURT_OF_APPEAL_CIVIL_DAILY_CAUSE_LIST,
                1,
                2,
                3,
                List.of("Time", "Case number", "Case details"),
                List.of("9am", "12345", "Case details A")
            ),
            new ListTestCaseSettings(
                "courtOfAppealCriminalDailyCauseList.json",
                ListType.COURT_OF_APPEAL_CRIMINAL_DAILY_CAUSE_LIST,
                1,
                2,
                3,
                List.of("Time", "Case number", "Case details"),
                List.of("9am", "12345", "Case details A")
            ),
            new ListTestCaseSettings(
                "cstWeeklyHearingList.json",
                ListType.CST_WEEKLY_HEARING_LIST,
                1,
                2,
                2,
                List.of("Date", "Case name"),
                List.of("10 December 2024", "This is a case name")
            ),
            new ListTestCaseSettings(
                "familyDivisionHighCourtDailyCauseList.json",
                ListType.FAMILY_DIVISION_HIGH_COURT_DAILY_CAUSE_LIST,
                1,
                2,
                3,
                List.of("Time", "Case number", "Case details"),
                List.of("9am", "12345", "Case details A")
            ),
            new ListTestCaseSettings(
                "financialListChdKbDailyCauseList.json",
                ListType.FINANCIAL_LIST_CHD_KB_DAILY_CAUSE_LIST,
                1,
                2,
                3,
                List.of("Time", "Case number", "Case name"),
                List.of("9am", "12345", "Case name A")
            ),
            new ListTestCaseSettings(
                "fttLandRegistryTribunalWeeklyHearingList.json",
                ListType.FTT_LR_WEEKLY_HEARING_LIST,
                1,
                2,
                3,
                List.of("Date", "Hearing time", "Case reference number"),
                List.of("16 December 2024", "10:15am", "1234")
            ),
            new ListTestCaseSettings(
                "fttResidentialPropertyTribunalWeeklyHearingList.json",
                ListType.RPT_EASTERN_WEEKLY_HEARING_LIST,
                1,
                2,
                3,
                List.of("Date", "Time", "Case reference number"),
                List.of("16 December 2024", "10:15am", "1234")
            ),
            new ListTestCaseSettings(
                "fttResidentialPropertyTribunalWeeklyHearingList.json",
                ListType.RPT_LONDON_WEEKLY_HEARING_LIST,
                1,
                2,
                3,
                List.of("Date", "Time", "Case reference number"),
                List.of("16 December 2024", "10:15am", "1234")
            ),
            new ListTestCaseSettings(
                "fttResidentialPropertyTribunalWeeklyHearingList.json",
                ListType.RPT_MIDLANDS_WEEKLY_HEARING_LIST,
                1,
                2,
                3,
                List.of("Date", "Time", "Case reference number"),
                List.of("16 December 2024", "10:15am", "1234")
            ),
            new ListTestCaseSettings(
                "fttResidentialPropertyTribunalWeeklyHearingList.json",
                ListType.RPT_NORTHERN_WEEKLY_HEARING_LIST,
                1,
                2,
                3,
                List.of("Date", "Time", "Case reference number"),
                List.of("16 December 2024", "10:15am", "1234")
            ),
            new ListTestCaseSettings(
                "fttResidentialPropertyTribunalWeeklyHearingList.json",
                ListType.RPT_SOUTHERN_WEEKLY_HEARING_LIST,
                1,
                2,
                3,
                List.of("Date", "Time", "Case reference number"),
                List.of("16 December 2024", "10:15am", "1234")
            ),
            new ListTestCaseSettings(
                "fttTaxWeeklyHearingList.json",
                ListType.FTT_TAX_WEEKLY_HEARING_LIST,
                1,
                2,
                3,
                List.of("Date", "Hearing time", "Case reference number"),
                List.of("16 December 2024", "10:15am", "1234")
            ),
            new ListTestCaseSettings(
                "grcWeeklyHearingList.json",
                ListType.GRC_WEEKLY_HEARING_LIST,
                1,
                2,
                3,
                List.of("Date", "Hearing time", "Case reference number"),
                List.of("16 December 2024", "10:15am", "1234")
            ),
            new ListTestCaseSettings(
                "insolvencyAndCompaniesCourtChdDailyCauseList.json",
                ListType.INSOLVENCY_AND_COMPANIES_COURT_CHD_DAILY_CAUSE_LIST,
                1,
                2,
                3,
                List.of("Time", "Case number", "Case name"),
                List.of("9am", "12345", "Case name A")
            ),
            new ListTestCaseSettings(
                "intellectualPropertyAndEnterpriseCourtDailyCauseList.json",
                ListType.INTELLECTUAL_PROPERTY_AND_ENTERPRISE_COURT_DAILY_CAUSE_LIST,
                1,
                2,
                3,
                List.of("Time", "Case number", "Case name"),
                List.of("9am", "12345", "Case name A")
            ),
            new ListTestCaseSettings(
                "intellectualPropertyListChdDailyCauseList.json",
                ListType.INTELLECTUAL_PROPERTY_LIST_CHD_DAILY_CAUSE_LIST,
                1,
                2,
                3,
                List.of("Time", "Case number", "Case name"),
                List.of("9am", "12345", "Case name A")
            ),
            new ListTestCaseSettings(
                "interimApplicationsChanceryDivisionDailyCauseList.json",
                ListType.INTERIM_APPLICATIONS_CHD_DAILY_CAUSE_LIST,
                1,
                3,
                3,
                List.of("Time", "Case number", "Case name"),
                List.of("10:30am", "1234", "This is a case name")
            ),
            new ListTestCaseSettings(
                "kingsBenchDivisionDailyCauseList.json",
                ListType.KINGS_BENCH_DIVISION_DAILY_CAUSE_LIST,
                1,
                2,
                3,
                List.of("Time", "Case number", "Case details"),
                List.of("9am", "12345", "Case details A")
            ),
            new ListTestCaseSettings(
                "kingsBenchMastersDailyCauseList.json",
                ListType.KINGS_BENCH_MASTERS_DAILY_CAUSE_LIST,
                1,
                2,
                3,
                List.of("Time", "Case number", "Case details"),
                List.of("9am", "12345", "Case details A")
            ),
            new ListTestCaseSettings(
                "administrativeCourtDailyCauseList.json",
                ListType.LEEDS_ADMINISTRATIVE_COURT_DAILY_CAUSE_LIST,
                1,
                2,
                4,
                List.of("Time", "Case number", "Hearing type", "Case details"),
                List.of("10am", "12345", "Directions A", "Case details A")
            ),
            new ListTestCaseSettings(
                "londonAdministrativeCourtDailyCauseList.json",
                ListType.LONDON_ADMINISTRATIVE_COURT_DAILY_CAUSE_LIST,
                1,
                2,
                3,
                List.of("Time", "Case number", "Case details"),
                List.of("9am", "12345", "Case details A")
            ),
            new ListTestCaseSettings(
                "londonCircuitCommercialCourtKbDailyCauseList.json",
                ListType.LONDON_CIRCUIT_COMMERCIAL_COURT_KB_DAILY_CAUSE_LIST,
                1,
                2,
                3,
                List.of("Time", "Case number", "Case name"),
                List.of("9am", "12345", "Case name A")
            ),
            new ListTestCaseSettings(
                "administrativeCourtDailyCauseList.json",
                ListType.MANCHESTER_ADMINISTRATIVE_COURT_DAILY_CAUSE_LIST,
                1,
                2,
                4,
                List.of("Time", "Case number", "Hearing type", "Case details"),
                List.of("10am", "12345", "Directions A", "Case details A")
            ),
            new ListTestCaseSettings(
                "mayorAndCityCivilDailyCauseList.json",
                ListType.MAYOR_AND_CITY_CIVIL_DAILY_CAUSE_LIST,
                1,
                2,
                3,
                List.of("Time", "Case number", "Case details"),
                List.of("9am", "12345", "Case details A")
            ),
            new ListTestCaseSettings(
                "patentsCourtChdDailyCauseList.json",
                ListType.PATENTS_COURT_CHD_DAILY_CAUSE_LIST,
                1,
                2,
                3,
                List.of("Time", "Case number", "Case name"),
                List.of("9am", "12345", "Case name A")
            ),
            new ListTestCaseSettings(
                "pensionsListChdDailyCauseList.json",
                ListType.PENSIONS_LIST_CHD_DAILY_CAUSE_LIST,
                1,
                2,
                3,
                List.of("Time", "Case number", "Case name"),
                List.of("9am", "12345", "Case name A")
            ),
            new ListTestCaseSettings(
                "phtWeeklyHearingList.json",
                ListType.PHT_WEEKLY_HEARING_LIST,
                1,
                2,
                2,
                List.of("Date", "Case name"),
                List.of("10 December 2024", "This is a case name")
            ),
            new ListTestCaseSettings(
                "propertyTrustsProbateListChdDailyCauseList.json",
                ListType.PROPERTY_TRUSTS_PROBATE_LIST_CHD_DAILY_CAUSE_LIST,
                1,
                2,
                3,
                List.of("Time", "Case number", "Case name"),
                List.of("9am", "12345", "Case name A")
            ),
            new ListTestCaseSettings(
                "revenueListChdDailyCauseList.json",
                ListType.REVENUE_LIST_CHD_DAILY_CAUSE_LIST,
                1,
                2,
                3,
                List.of("Time", "Case number", "Case name"),
                List.of("9am", "12345", "Case name A")
            ),
            new ListTestCaseSettings(
                "sendDailyHearingList.json",
                ListType.SEND_DAILY_HEARING_LIST,
                1,
                2,
                3,
                List.of("Time", "Case reference number", "Venue"),
                List.of("10am", "1234", "Venue A")
            ),
            new ListTestCaseSettings(
                "seniorCourtsCostsOfficeDailyCauseList.json",
                ListType.SENIOR_COURTS_COSTS_OFFICE_DAILY_CAUSE_LIST,
                1,
                2,
                3,
                List.of("Time", "Case number", "Case details"),
                List.of("9am", "12345", "Case details A")
            ),
            new ListTestCaseSettings(
                "siacWeeklyHearingList.json",
                ListType.SIAC_WEEKLY_HEARING_LIST,
                1,
                1,
                3,
                List.of("Date", "Time", "Case reference number"),
                List.of("11 December 2024", "10:15am", "1234")
            ),
            new ListTestCaseSettings(
                "siacWeeklyHearingList.json",
                ListType.POAC_WEEKLY_HEARING_LIST,
                1,
                1,
                3,
                List.of("Date", "Time", "Case reference number"),
                List.of("11 December 2024", "10:15am", "1234")
            ),
            new ListTestCaseSettings(
                "siacWeeklyHearingList.json",
                ListType.PAAC_WEEKLY_HEARING_LIST,
                1,
                1,
                3,
                List.of("Date", "Time", "Case reference number"),
                List.of("11 December 2024", "10:15am", "1234")
            ),
            new ListTestCaseSettings(
                "sscsDailyHearingList.json",
                ListType.SSCS_MIDLANDS_DAILY_HEARING_LIST,
                1,
                1,
                3,
                List.of("Hearing time", "Hearing type", "Appeal reference number"),
                List.of("10:30am", "Directions", "1234567")
            ),
            new ListTestCaseSettings(
                "sscsDailyHearingList.json",
                ListType.SSCS_SOUTH_EAST_DAILY_HEARING_LIST,
                1,
                1,
                3,
                List.of("Hearing time", "Hearing type", "Appeal reference number"),
                List.of("10:30am", "Directions", "1234567")
            ),
            new ListTestCaseSettings(
                "sscsDailyHearingList.json",
                ListType.SSCS_WALES_AND_SOUTH_WEST_DAILY_HEARING_LIST,
                1,
                1,
                3,
                List.of("Hearing time", "Hearing type", "Appeal reference number"),
                List.of("10:30am", "Directions", "1234567")
            ),
            new ListTestCaseSettings(
                "sscsDailyHearingList.json",
                ListType.SSCS_SCOTLAND_DAILY_HEARING_LIST,
                1,
                1,
                3,
                List.of("Hearing time", "Hearing type", "Appeal reference number"),
                List.of("10:30am", "Directions", "1234567")
            ),
            new ListTestCaseSettings(
                "sscsDailyHearingList.json",
                ListType.SSCS_NORTH_EAST_DAILY_HEARING_LIST,
                1,
                1,
                3,
                List.of("Hearing time", "Hearing type", "Appeal reference number"),
                List.of("10:30am", "Directions", "1234567")
            ),
            new ListTestCaseSettings(
                "sscsDailyHearingList.json",
                ListType.SSCS_NORTH_WEST_DAILY_HEARING_LIST,
                1,
                1,
                3,
                List.of("Hearing time", "Hearing type", "Appeal reference number"),
                List.of("10:30am", "Directions", "1234567")
            ),
            new ListTestCaseSettings(
                "sscsDailyHearingList.json",
                ListType.SSCS_LONDON_DAILY_HEARING_LIST,
                1,
                1,
                3,
                List.of("Hearing time", "Hearing type", "Appeal reference number"),
                List.of("10:30am", "Directions", "1234567")
            ),
            new ListTestCaseSettings(
                "technologyAndConstructionCourtKbDailyCauseList.json",
                ListType.TECHNOLOGY_AND_CONSTRUCTION_COURT_KB_DAILY_CAUSE_LIST,
                1,
                2,
                3,
                List.of("Time", "Case number", "Case name"),
                List.of("9am", "12345", "Case name A")
            ),
            new ListTestCaseSettings(
                "utAdministrativeAppealsChamberDailyHearingList.json",
                ListType.UT_AAC_DAILY_HEARING_LIST,
                1,
                1,
                3,
                List.of("Time", "Case reference number", "Appellant"),
                List.of("10:15am", "12345", "Appellant 1")
            ),
            new ListTestCaseSettings(
                "utIacJudicialReviewDailyHearingList.json",
                ListType.UT_IAC_JR_LEEDS_DAILY_HEARING_LIST,
                1,
                2,
                2,
                List.of("Hearing time", "Case reference number"),
                List.of("10:30am", "1234")
            ),
            new ListTestCaseSettings(
                "utIacJudicialReviewDailyHearingList.json",
                ListType.UT_IAC_JR_MANCHESTER_DAILY_HEARING_LIST,
                1,
                2,
                2,
                List.of("Hearing time", "Case reference number"),
                List.of("10:30am", "1234")
            ),
            new ListTestCaseSettings(
                "utIacJudicialReviewDailyHearingList.json",
                ListType.UT_IAC_JR_BIRMINGHAM_DAILY_HEARING_LIST,
                1,
                2,
                2,
                List.of("Hearing time", "Case reference number"),
                List.of("10:30am", "1234")
            ),
            new ListTestCaseSettings(
                "utIacJudicialReviewDailyHearingList.json",
                ListType.UT_IAC_JR_CARDIFF_DAILY_HEARING_LIST,
                1,
                2,
                2,
                List.of("Hearing time", "Case reference number"),
                List.of("10:30am", "1234")
            ),
            new ListTestCaseSettings(
                "utIacJudicialReviewLondonDailyHearingList.json",
                ListType.UT_IAC_JR_LONDON_DAILY_HEARING_LIST,
                1,
                2,
                2,
                List.of("Hearing time", "Case reference number"),
                List.of("10:30am", "1234")
            ),
            new ListTestCaseSettings(
                "utIacStatutoryAppealsDailyHearingList.json",
                ListType.UT_IAC_STATUTORY_APPEALS_DAILY_HEARING_LIST,
                1,
                2,
                2,
                List.of("Hearing time", "Appeal reference number"),
                List.of("10:30am", "1234")
            ),
            new ListTestCaseSettings(
                "utLandsChamberDailyHearingList.json",
                ListType.UT_LC_DAILY_HEARING_LIST,
                1,
                1,
                3,
                List.of("Time", "Case reference number", "Case name"),
                List.of("10:15am", "12345", "This is a case name")
            ),
            new ListTestCaseSettings(
                "utTaxAndChanceryChamberDailyHearingList.json",
                ListType.UT_T_AND_CC_DAILY_HEARING_LIST,
                1,
                1,
                3,
                List.of("Time", "Case reference number", "Case name"),
                List.of("10:15am", "12345", "This is a case name")
            ),
            new ListTestCaseSettings(
                "wpafccWeeklyHearingList.json",
                ListType.WPAFCC_WEEKLY_HEARING_LIST,
                1,
                2,
                3,
                List.of("Date", "Hearing time", "Case reference number"),
                List.of("16 December 2024", "10:15am", "1234")
            )
        );
    }
}
