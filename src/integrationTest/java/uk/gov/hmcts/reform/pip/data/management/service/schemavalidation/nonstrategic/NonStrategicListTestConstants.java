package uk.gov.hmcts.reform.pip.data.management.service.schemavalidation.nonstrategic;

import uk.gov.hmcts.reform.pip.model.publication.ArtefactType;
import uk.gov.hmcts.reform.pip.model.publication.Language;
import uk.gov.hmcts.reform.pip.model.publication.Sensitivity;

import java.time.LocalDateTime;

public class NonStrategicListTestConstants {
    public static final String SOURCE_ARTEFACT_ID = "sourceArtefactId";
    public static final LocalDateTime DISPLAY_FROM = LocalDateTime.now();
    public static final LocalDateTime DISPLAY_TO = LocalDateTime.now().plusDays(1);
    public static final Language LANGUAGE = Language.ENGLISH;
    public static final String PROVENANCE = "provenance";
    public static final Sensitivity SENSITIVITY = Sensitivity.PUBLIC;
    public static final ArtefactType ARTEFACT_TYPE = ArtefactType.LIST;
    public static final String COURT_ID = "123";
    public static final LocalDateTime CONTENT_DATE = LocalDateTime.now();
    public static final String VENUE = "venue";
    public static final String JUDGE = "judge";
    public static final String TIME = "time";
    public static final String TYPE = "type";
    public static final String CASE_NUMBER = "caseNumber";
    public static final String CASE_NAME = "caseName";
    public static final String ADDITIONAL_INFORMATION = "additionalInformation";
    public static final String APPELLANT = "appellant";
    public static final String APPEAL_REFERENCE_NUMBER = "appealReferenceNumber";
    public static final String PARENT_JSON_FILE_PATH = "data/non-strategic";
    public static final String CASE_TYPE = "caseType";
    public static final String HEARING_TYPE = "hearingType";
    public static final String HEARING_TIME = "hearingTime";
    public static final String CASE_DETAILS = "caseDetails";
    public static final String DATE = "date";
    public static final String CASE_REFERENCE_NUMBER = "caseReferenceNumber";
    public static final String VENUE_PLATFORM = "venue/platform";
    public static final String MEMBERS = "members";
    public static final String JUDGES = "judges";
    public static final String HEARING_LENGTH = "hearingLength";
    public static final String HEARING_LIST_NODE = "hearingList";
    public static final String FUTURE_JUDGMENTS_NODE = "futureJudgments";
    public static final String MODE_OF_HEARING = "modeOfHearing";
    public static final String OPEN_JUSTICE_DETAILS_NODE = "openJusticeStatementDetails";
    public static final String NAME_TO_BE_DISPLAYED = "nameToBeDisplayed";
    public static final String EMAIL = "email";
    public static final String LONDON_ADMINISTRATIVE_COURT_NODE = "londonAdministrativeCourt";
    public static final String PLANNING_COURT_NODE = "planningCourt";
    public static final String COURT_ROOM = "courtroom";
    public static final String HEARING_METHOD = "hearingMethod";
    public static final String RESPONDENT = "respondent";
    public static final String TIME_ESTIMATE = "timeEstimate";
    public static final String PANEL = "panel";
    public static final String FTA_RESPONDENT = "fta/respondent";
    public static final String CASE_TITLE = "caseTitle";
    public static final String REPRESENTATIVE = "representative";
    public static final String LOCATION = "location";

    public static final String ADMIRALTY_COURT_KB_DAILY_CAUSE_LIST_JSON_FILE_PATH =
        PARENT_JSON_FILE_PATH + "/admiralty_court_kb_daily_cause_list/admiraltyCourtKbDailyCauseList.json";
    public static final String AST_DAILY_HEARING_LIST_JSON_FILE_PATH =
        PARENT_JSON_FILE_PATH + "/ast-daily-hearing-list/astDailyHearingList.json";
    public static final String ADMINISTRATIVE_COURT_DAILY_CAUSE_LIST_JSON_FILE_PATH =
        PARENT_JSON_FILE_PATH + "/administrative-court-daily-cause-list/administrativeCourtDailyCauseList.json";
    public static final String BUSINESS_LIST_CHD_DAILY_CAUSE_LIST_JSON_FILE_PATH =
        PARENT_JSON_FILE_PATH + "/business_list_chd_daily_cause_list/businessListChdDailyCauseList.json";
    public static final String CHANCERY_APPEALS_CHD_DAILY_CAUSE_LIST_JSON_FILE_PATH =
        PARENT_JSON_FILE_PATH + "/chancery_appeals_chd_daily_cause_list/chanceryAppealsChdDailyCauseList.json";
    public static final String CIC_WEEKLY_HEARING_LIST_JSON_FILE_PATH =
        PARENT_JSON_FILE_PATH + "/cic-weekly-hearing-list/cicWeeklyHearingList.json";
    public static final String CIVIL_COURTS_RCJ_DAILY_CAUSE_LIST_JSON_FILE_PATH =
        PARENT_JSON_FILE_PATH + "/civil-courts-rcj-daily-cause-list/civilCourtsRcjDailyCauseList.json";
    public static final String COMMERCIAL_COURT_KB_DAILY_CAUSE_LIST_JSON_FILE_PATH =
        PARENT_JSON_FILE_PATH + "/commercial_court_kb_daily_cause_list/commercialCourtKbDailyCauseList.json";
    public static final String COMPANIES_WINDING_UP_CHD_DAILY_CAUSE_LIST_JSON_FILE_PATH =
        PARENT_JSON_FILE_PATH + "/companies_winding_up_chd_daily_cause_list/companiesWindingUpChdDailyCauseList.json";
    public static final String COMPETITION_LIST_CHD_DAILY_CAUSE_LIST_JSON_FILE_PATH =
        PARENT_JSON_FILE_PATH + "/competition_list_chd_daily_cause_list/competitionListChdDailyCauseList.json";
    public static final String COUNTY_COURT_LONDON_CIVIL_DAILY_CAUSE_LIST_JSON_FILE_PATH =
        PARENT_JSON_FILE_PATH + "/county-court-london-civil-daily-cause-list/countyCourtLondonCivilDailyCauseList.json";
    public static final String COURT_OF_APPEAL_CIVIL_DAILY_CAUSE_LIST_JSON_FILE_PATH = PARENT_JSON_FILE_PATH
        + "/court-of-appeal-civil-daily-cause-list/courtOfAppealCivilDailyCauseList.json";
    public static final String COURT_OF_APPEAL_CRIMINAL_DAILY_CAUSE_LIST_JSON_FILE_PATH = PARENT_JSON_FILE_PATH
        + "/court-of-appeal-criminal-daily-cause-list/courtOfAppealCriminalDailyCauseList.json";
    public static final String CST_WEEKLY_HEARING_LIST_JSON_FILE_PATH = PARENT_JSON_FILE_PATH
        + "/cst-weekly-hearing-list/cstWeeklyHearingList.json";
    public static final String FAMILY_DIVISION_HIGH_COURT_DAILY_CAUSE_LIST_JSON_FILE_PATH =
        PARENT_JSON_FILE_PATH + "/family-division-high-court-daily-cause-list/familyDivisionHighCourtDailyCauseList.json";
    public static final String FINANCIAL_LISTS_CHD_DAILY_CAUSE_LIST_JSON_FILE_PATH =
        PARENT_JSON_FILE_PATH + "/financial_list_chd_kb_daily_cause_list/financialListChdKbDailyCauseList.json";
    public static final String FTT_LR_WEEKLY_HEARING_LIST_JSON_FILE_PATH =
        PARENT_JSON_FILE_PATH + "/ftt-land-registry-tribunal-weekly-hearing-list/fttLandRegistryTribunalWeeklyHearingList.json";
    public static final String FTT_TAX_WEEKLY_HEARING_LIST_JSON_FILE_PATH =
        PARENT_JSON_FILE_PATH + "/ftt-tax-tribunal-weekly-hearing-list/fttTaxWeeklyHearingList.json";
    public static final String GRC_WEEKLY_HEARING_LIST_JSON_FILE_PATH =
        PARENT_JSON_FILE_PATH + "/grc-weekly-hearing-list/grcWeeklyHearingList.json";
    public static final String INSOLVENCY_AND_COMPANIES_COURT_CHD_DAILY_CAUSE_LIST_JSON_FILE_PATH =
        PARENT_JSON_FILE_PATH + "/insolvency_and_companies_court_chd_daily_cause_list/"
            + "insolvencyAndCompaniesCourtChdDailyCauseList.json";
    public static final String INTELLECTUAL_PROPERTY_AND_ENTERPRISE_COURT_DAILY_CAUSE_LIST_JSON_FILE_PATH =
        PARENT_JSON_FILE_PATH + "/intellectual-property-and-enterprise-court-daily-cause-list/"
            + "intellectualPropertyAndEnterpriseCourtDailyCauseList.json";
    public static final String INTELLECTUAL_PROPERTY_LIST_CHD_DAILY_CAUSE_LIST_JSON_FILE_PATH =
        PARENT_JSON_FILE_PATH + "/intellectual-property-list-chd-daily-cause-list/"
            + "intellectualPropertyListChdDailyCauseList.json";
    public static final String INTERIM_APPLICATION_CHANCERY_LIST_CHD_DAILY_CAUSE_LIST_JSON_FILE_PATH =
        PARENT_JSON_FILE_PATH + "/interim-applications-chd-daily-cause-list/"
            + "interimApplicationsChanceryDivisionDailyCauseList.json";
    public static final String KINGS_BENCH_DIVISION_DAILY_CAUSE_LIST_JSON_FILE_PATH =
        PARENT_JSON_FILE_PATH + "/kings-bench-division-daily-cause-list/kingsBenchDivisionDailyCauseList.json";
    public static final String KINGS_BENCH_MASTERS_DAILY_CAUSE_LIST_JSON_FILE_PATH =
        PARENT_JSON_FILE_PATH + "/kings-bench-masters-daily-cause-list/kingsBenchMastersDailyCauseList.json";
    public static final String LONDON_ADMINISTRATIVE_COURT_DAILY_CAUSE_LIST_JSON_FILE_PATH =
        PARENT_JSON_FILE_PATH + "/london-administrative-court-daily-cause-list/londonAdministrativeCourtDailyCauseList.json";
    public static final String LONDON_CIRCUIT_COURT_DAILY_CAUSE_LIST_JSON_FILE_PATH =
        PARENT_JSON_FILE_PATH + "/london-circuit-commercial-court-kb-daily-cause-list/londonCircuitCommercialCourtKbDailyCauseList.json";
    public static final String MAYOR_AND_CITY_CIVIL_DAILY_CAUSE_LIST_JSON_FILE_PATH =
        PARENT_JSON_FILE_PATH + "/mayor-and-city-civil-daily-cause-list/mayorAndCityCivilDailyCauseList.json";
    public static final String SIAC_WEEKLY_HEARING_LIST_JSON_FILE_PATH =
        PARENT_JSON_FILE_PATH + "/siac-weekly-hearing-list/siacWeeklyHearingList.json";
    public static final String PATENTS_COURT_CHD_DAILY_CAUSE_LIST_JSON_FILE_PATH =
        PARENT_JSON_FILE_PATH + "/patents-court-chd-daily-cause-list/patentsCourtChdDailyCauseList.json";
    public static final String PENSIONS_LIST_CHD_DAILY_CAUSE_LIST_JSON_FILE_PATH =
        PARENT_JSON_FILE_PATH + "/pensions-list-chd-daily-cause-list/pensionsListChdDailyCauseList.json";
    public static final String PHT_WEEKLY_HEARING_LIST_JSON_FILE_PATH =
        PARENT_JSON_FILE_PATH + "/pht-weekly-hearing-list/phtWeeklyHearingList.json";
    public static final String PROPERTY_TRUSTS_PROBATE_LIST_CHD_DAILY_CAUSE_LIST_JSON_FILE_PATH =
        PARENT_JSON_FILE_PATH + "/property-trusts-probate-list-chd-daily-cause-list/"
            + "propertyTrustsProbateListChdDailyCauseList.json";
    public static final String RPT_WEEKLY_HEARING_LIST_JSON_FILE_PATH =
        PARENT_JSON_FILE_PATH + "/ftt-residential-property-tribunal-weekly-hearing-list/"
            + "fttResidentialPropertyTribunalWeeklyHearingList.json";
    public static final String SEND_DAILY_HEARING_LIST_JSON_FILE_PATH =
        PARENT_JSON_FILE_PATH + "/send-daily-hearing-list/sendDailyHearingList.json";
    public static final String SENIOR_COURTS_COSTS_OFFICE_DAILY_CAUSE_LIST_JSON_FILE_PATH =
        PARENT_JSON_FILE_PATH + "/senior-courts-costs-office-daily-cause-list/seniorCourtsCostsOfficeDailyCauseList.json";
    public static final String SSCS_DAILY_HEARING_LIST_JSON_FILE_PATH =
        PARENT_JSON_FILE_PATH + "/sscs-daily-hearing-list/sscsDailyHearingList.json";
    public static final String TECHNOLOGY_AND_CONSTRUCTION_COURT_KB_DAILY_CAUSE_LIST_JSON_FILE_PATH =
        PARENT_JSON_FILE_PATH + "/technology-and-construction-court-kb-daily-cause-list/"
            + "technologyAndConstructionCourtKbDailyCauseList.json";
    public static final String UT_ADMINISTRATIVE_APPEALS_CHAMBER_DAILY_HEARING_LIST_JSON_FILE_PATH =
        PARENT_JSON_FILE_PATH + "/ut-administrative-appeals-chamber-daily-hearing-list/"
            + "utAdministrativeAppealsChamberDailyHearingList.json";
    public static final String UT_IAC_JUDICIAL_REVIEWS_DAILY_HEARING_LIST_JSON_FILE_PATH =
        PARENT_JSON_FILE_PATH + "/ut-iac-judicial-review-daily-hearing-list/utIacJudicialReviewDailyHearingList.json";
    public static final String UT_IAC_STATUTORY_APPEALS_DAILY_HEARING_LIST_JSON_FILE_PATH =
        PARENT_JSON_FILE_PATH + "/ut-iac-statutory-appeals-daily-hearing-list/utIacStatutoryAppealsDailyHearingList.json";
    public static final String UT_LANDS_CHAMBER_DAILY_HEARING_LIST_JSON_FILE_PATH =
        PARENT_JSON_FILE_PATH + "/ut-lands-chamber-daily-hearing-list/utLandsChamberDailyHearingList.json";
    public static final String UT_TAX_CHAMBER_DAILY_HEARING_LIST_JSON_FILE_PATH =
        PARENT_JSON_FILE_PATH + "/ut-tax-and-chancery-chamber-daily-hearing-list/"
            + "utTaxAndChanceryChamberDailyHearingList.json";
    public static final String WPAFCC_WEEKLY_HEARING_LIST_JSON_FILE_PATH =
        PARENT_JSON_FILE_PATH + "/wpafcc-weekly-hearing-list/wpafccWeeklyHearingList.json";
}
