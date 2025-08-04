package uk.gov.hmcts.reform.pip.data.management.service.schemavalidation.nonstrategic;

import org.junit.jupiter.params.provider.Arguments;
import uk.gov.hmcts.reform.pip.model.publication.ArtefactType;
import uk.gov.hmcts.reform.pip.model.publication.Language;
import uk.gov.hmcts.reform.pip.model.publication.ListType;
import uk.gov.hmcts.reform.pip.model.publication.Sensitivity;

import java.time.LocalDateTime;
import java.util.stream.Stream;

public class NonStrategicListTestConfiguration {
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
    private static final String OPEN_JUSTICE_DETAILS_NODE = "openJusticeStatementDetails";
    private static final String NAME_TO_BE_DISPLAYED = "nameToBeDisplayed";
    private static final String EMAIL = "email";
    private static final String LONDON_ADMINISTRATIVE_COURT_NODE = "londonAdministrativeCourt";
    private static final String PLANNING_COURT_NODE = "planningCourt";

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

    public record ListTypeConfig(ListType listType, String jsonFilePath, String validationField, String parentNode) {
        public ListTypeConfig {
            parentNode = parentNode == null ? "" : parentNode;
        }

        public ListTypeConfig(
            ListType listType,
            String jsonFilePath,
            String validationField
        ) {
            this(listType, jsonFilePath, validationField, "");
        }
    }

    public static Stream<Arguments> venueListMandatoryAttributes() {
        return Stream.of(
            Arguments.of(new ListTypeConfig(
                ListType.ADMIRALTY_COURT_KB_DAILY_CAUSE_LIST,
                ADMIRALTY_COURT_KB_DAILY_CAUSE_LIST_JSON_FILE_PATH,
                VENUE)),
            Arguments.of(new ListTypeConfig(
                ListType.BIRMINGHAM_ADMINISTRATIVE_COURT_DAILY_CAUSE_LIST,
                ADMINISTRATIVE_COURT_DAILY_CAUSE_LIST_JSON_FILE_PATH,
                VENUE)),
            Arguments.of(new ListTypeConfig(
                ListType.BRISTOL_AND_CARDIFF_ADMINISTRATIVE_COURT_DAILY_CAUSE_LIST,
                ADMINISTRATIVE_COURT_DAILY_CAUSE_LIST_JSON_FILE_PATH,
                VENUE)),
            Arguments.of(new ListTypeConfig(
                ListType.BUSINESS_LIST_CHD_DAILY_CAUSE_LIST,
                BUSINESS_LIST_CHD_DAILY_CAUSE_LIST_JSON_FILE_PATH,
                VENUE)),
            Arguments.of(new ListTypeConfig(
                ListType.CHANCERY_APPEALS_CHD_DAILY_CAUSE_LIST,
                CHANCERY_APPEALS_CHD_DAILY_CAUSE_LIST_JSON_FILE_PATH,
                VENUE)),
            Arguments.of(new ListTypeConfig(
                ListType.CIVIL_COURTS_RCJ_DAILY_CAUSE_LIST,
                CIVIL_COURTS_RCJ_DAILY_CAUSE_LIST_JSON_FILE_PATH,
                VENUE)),
            Arguments.of(new ListTypeConfig(
                ListType.COMMERCIAL_COURT_KB_DAILY_CAUSE_LIST,
                COMMERCIAL_COURT_KB_DAILY_CAUSE_LIST_JSON_FILE_PATH,
                VENUE)),
            Arguments.of(new ListTypeConfig(
                ListType.COMPANIES_WINDING_UP_CHD_DAILY_CAUSE_LIST,
                COMPANIES_WINDING_UP_CHD_DAILY_CAUSE_LIST_JSON_FILE_PATH,
                VENUE)),
            Arguments.of(new ListTypeConfig(
                ListType.COMPETITION_LIST_CHD_DAILY_CAUSE_LIST,
                COMPETITION_LIST_CHD_DAILY_CAUSE_LIST_JSON_FILE_PATH,
                VENUE)),
            Arguments.of(new ListTypeConfig(
                ListType.COUNTY_COURT_LONDON_CIVIL_DAILY_CAUSE_LIST,
                COUNTY_COURT_LONDON_CIVIL_DAILY_CAUSE_LIST_JSON_FILE_PATH,
                VENUE)),
            Arguments.of(new ListTypeConfig(
                ListType.COURT_OF_APPEAL_CIVIL_DAILY_CAUSE_LIST,
                COURT_OF_APPEAL_CIVIL_DAILY_CAUSE_LIST_JSON_FILE_PATH,
                VENUE, HEARING_LIST_NODE)),
            Arguments.of(new ListTypeConfig(
                ListType.COURT_OF_APPEAL_CIVIL_DAILY_CAUSE_LIST,
                COURT_OF_APPEAL_CIVIL_DAILY_CAUSE_LIST_JSON_FILE_PATH,
                VENUE, FUTURE_JUDGMENTS_NODE)),
            Arguments.of(new ListTypeConfig(
                ListType.COURT_OF_APPEAL_CRIMINAL_DAILY_CAUSE_LIST,
                COURT_OF_APPEAL_CRIMINAL_DAILY_CAUSE_LIST_JSON_FILE_PATH,
                VENUE)),
            Arguments.of(new ListTypeConfig(
                ListType.CST_WEEKLY_HEARING_LIST,
                CST_WEEKLY_HEARING_LIST_JSON_FILE_PATH,
                VENUE)),
            Arguments.of(new ListTypeConfig(
                ListType.FAMILY_DIVISION_HIGH_COURT_DAILY_CAUSE_LIST,
                FAMILY_DIVISION_HIGH_COURT_DAILY_CAUSE_LIST_JSON_FILE_PATH,
                VENUE)),
            Arguments.of(new ListTypeConfig(
                ListType.FINANCIAL_LIST_CHD_KB_DAILY_CAUSE_LIST,
                FINANCIAL_LISTS_CHD_DAILY_CAUSE_LIST_JSON_FILE_PATH,
                VENUE)),
            Arguments.of(new ListTypeConfig(
                ListType.GRC_WEEKLY_HEARING_LIST,
                GRC_WEEKLY_HEARING_LIST_JSON_FILE_PATH,
                VENUE)),
            Arguments.of(new ListTypeConfig(
                ListType.INSOLVENCY_AND_COMPANIES_COURT_CHD_DAILY_CAUSE_LIST,
                INSOLVENCY_AND_COMPANIES_COURT_CHD_DAILY_CAUSE_LIST_JSON_FILE_PATH,
                VENUE)),
            Arguments.of(new ListTypeConfig(
                ListType.INTELLECTUAL_PROPERTY_AND_ENTERPRISE_COURT_DAILY_CAUSE_LIST,
                INTELLECTUAL_PROPERTY_AND_ENTERPRISE_COURT_DAILY_CAUSE_LIST_JSON_FILE_PATH,
                VENUE)),
            Arguments.of(new ListTypeConfig(
                ListType.INTELLECTUAL_PROPERTY_LIST_CHD_DAILY_CAUSE_LIST,
                INTELLECTUAL_PROPERTY_LIST_CHD_DAILY_CAUSE_LIST_JSON_FILE_PATH,
                VENUE)),
            Arguments.of(new ListTypeConfig(
                ListType.INTERIM_APPLICATIONS_CHD_DAILY_CAUSE_LIST,
                INTERIM_APPLICATION_CHANCERY_LIST_CHD_DAILY_CAUSE_LIST_JSON_FILE_PATH,
                VENUE, HEARING_LIST_NODE)),
            Arguments.of(new ListTypeConfig(
                ListType.KINGS_BENCH_DIVISION_DAILY_CAUSE_LIST,
                KINGS_BENCH_DIVISION_DAILY_CAUSE_LIST_JSON_FILE_PATH,
                VENUE)),
            Arguments.of(new ListTypeConfig(
                ListType.KINGS_BENCH_MASTERS_DAILY_CAUSE_LIST,
                KINGS_BENCH_MASTERS_DAILY_CAUSE_LIST_JSON_FILE_PATH,
                VENUE)),
            Arguments.of(new ListTypeConfig(
                ListType.LEEDS_ADMINISTRATIVE_COURT_DAILY_CAUSE_LIST,
                ADMINISTRATIVE_COURT_DAILY_CAUSE_LIST_JSON_FILE_PATH,
                VENUE)),
            Arguments.of(new ListTypeConfig(
                ListType.LONDON_ADMINISTRATIVE_COURT_DAILY_CAUSE_LIST,
                LONDON_ADMINISTRATIVE_COURT_DAILY_CAUSE_LIST_JSON_FILE_PATH,
                VENUE, LONDON_ADMINISTRATIVE_COURT_NODE)),
            Arguments.of(new ListTypeConfig(
                ListType.LONDON_ADMINISTRATIVE_COURT_DAILY_CAUSE_LIST,
                LONDON_ADMINISTRATIVE_COURT_DAILY_CAUSE_LIST_JSON_FILE_PATH,
                VENUE, PLANNING_COURT_NODE)),
            Arguments.of(new ListTypeConfig(
                ListType.MANCHESTER_ADMINISTRATIVE_COURT_DAILY_CAUSE_LIST,
                ADMINISTRATIVE_COURT_DAILY_CAUSE_LIST_JSON_FILE_PATH,
                VENUE))
        );
    }

    public static Stream<Arguments> judgeListMandatoryAttributes() {
        return Stream.of(
            Arguments.of(new ListTypeConfig(
                ListType.ADMIRALTY_COURT_KB_DAILY_CAUSE_LIST,
                ADMIRALTY_COURT_KB_DAILY_CAUSE_LIST_JSON_FILE_PATH,
                JUDGE)),
            Arguments.of(new ListTypeConfig(
                ListType.BIRMINGHAM_ADMINISTRATIVE_COURT_DAILY_CAUSE_LIST,
                ADMINISTRATIVE_COURT_DAILY_CAUSE_LIST_JSON_FILE_PATH,
                JUDGE)),
            Arguments.of(new ListTypeConfig(
                ListType.BRISTOL_AND_CARDIFF_ADMINISTRATIVE_COURT_DAILY_CAUSE_LIST,
                ADMINISTRATIVE_COURT_DAILY_CAUSE_LIST_JSON_FILE_PATH,
                JUDGE)),
            Arguments.of(new ListTypeConfig(
                ListType.BUSINESS_LIST_CHD_DAILY_CAUSE_LIST,
                BUSINESS_LIST_CHD_DAILY_CAUSE_LIST_JSON_FILE_PATH,
                JUDGE)),
            Arguments.of(new ListTypeConfig(
                ListType.CHANCERY_APPEALS_CHD_DAILY_CAUSE_LIST,
                CHANCERY_APPEALS_CHD_DAILY_CAUSE_LIST_JSON_FILE_PATH,
                JUDGE)),
            Arguments.of(new ListTypeConfig(
                ListType.CIVIL_COURTS_RCJ_DAILY_CAUSE_LIST,
                CIVIL_COURTS_RCJ_DAILY_CAUSE_LIST_JSON_FILE_PATH,
                JUDGE)),
            Arguments.of(new ListTypeConfig(
                ListType.COMMERCIAL_COURT_KB_DAILY_CAUSE_LIST,
                COMMERCIAL_COURT_KB_DAILY_CAUSE_LIST_JSON_FILE_PATH,
                JUDGE)),
            Arguments.of(new ListTypeConfig(
                ListType.COMPANIES_WINDING_UP_CHD_DAILY_CAUSE_LIST,
                COMPANIES_WINDING_UP_CHD_DAILY_CAUSE_LIST_JSON_FILE_PATH,
                JUDGE)),
            Arguments.of(new ListTypeConfig(
                ListType.COMPETITION_LIST_CHD_DAILY_CAUSE_LIST,
                COMPETITION_LIST_CHD_DAILY_CAUSE_LIST_JSON_FILE_PATH,
                JUDGE)),
            Arguments.of(new ListTypeConfig(
                ListType.COUNTY_COURT_LONDON_CIVIL_DAILY_CAUSE_LIST,
                COUNTY_COURT_LONDON_CIVIL_DAILY_CAUSE_LIST_JSON_FILE_PATH,
                JUDGE)),
            Arguments.of(new ListTypeConfig(
                ListType.COURT_OF_APPEAL_CIVIL_DAILY_CAUSE_LIST,
                COURT_OF_APPEAL_CIVIL_DAILY_CAUSE_LIST_JSON_FILE_PATH,
                JUDGE, HEARING_LIST_NODE)),
            Arguments.of(new ListTypeConfig(
                ListType.COURT_OF_APPEAL_CIVIL_DAILY_CAUSE_LIST,
                COURT_OF_APPEAL_CIVIL_DAILY_CAUSE_LIST_JSON_FILE_PATH,
                JUDGE, FUTURE_JUDGMENTS_NODE)),
            Arguments.of(new ListTypeConfig(
                ListType.COURT_OF_APPEAL_CRIMINAL_DAILY_CAUSE_LIST,
                COURT_OF_APPEAL_CRIMINAL_DAILY_CAUSE_LIST_JSON_FILE_PATH,
                JUDGE)),
            Arguments.of(new ListTypeConfig(
                ListType.FAMILY_DIVISION_HIGH_COURT_DAILY_CAUSE_LIST,
                FAMILY_DIVISION_HIGH_COURT_DAILY_CAUSE_LIST_JSON_FILE_PATH,
                JUDGE)),
            Arguments.of(new ListTypeConfig(
                ListType.FINANCIAL_LIST_CHD_KB_DAILY_CAUSE_LIST,
                FINANCIAL_LISTS_CHD_DAILY_CAUSE_LIST_JSON_FILE_PATH,
                JUDGE)),
            Arguments.of(new ListTypeConfig(
                ListType.FTT_LR_WEEKLY_HEARING_LIST,
                FTT_LR_WEEKLY_HEARING_LIST_JSON_FILE_PATH,
                JUDGE)),
            Arguments.of(new ListTypeConfig(
                ListType.INSOLVENCY_AND_COMPANIES_COURT_CHD_DAILY_CAUSE_LIST,
                INSOLVENCY_AND_COMPANIES_COURT_CHD_DAILY_CAUSE_LIST_JSON_FILE_PATH,
                JUDGE)),
            Arguments.of(new ListTypeConfig(
                ListType.INTELLECTUAL_PROPERTY_AND_ENTERPRISE_COURT_DAILY_CAUSE_LIST,
                INTELLECTUAL_PROPERTY_AND_ENTERPRISE_COURT_DAILY_CAUSE_LIST_JSON_FILE_PATH,
                JUDGE)),
            Arguments.of(new ListTypeConfig(
                ListType.INTELLECTUAL_PROPERTY_LIST_CHD_DAILY_CAUSE_LIST,
                INTELLECTUAL_PROPERTY_LIST_CHD_DAILY_CAUSE_LIST_JSON_FILE_PATH,
                JUDGE)),
            Arguments.of(new ListTypeConfig(
                ListType.INTERIM_APPLICATIONS_CHD_DAILY_CAUSE_LIST,
                INTERIM_APPLICATION_CHANCERY_LIST_CHD_DAILY_CAUSE_LIST_JSON_FILE_PATH,
                JUDGE, HEARING_LIST_NODE)),
            Arguments.of(new ListTypeConfig(
                ListType.KINGS_BENCH_DIVISION_DAILY_CAUSE_LIST,
                KINGS_BENCH_DIVISION_DAILY_CAUSE_LIST_JSON_FILE_PATH,
                JUDGE)),
            Arguments.of(new ListTypeConfig(
                ListType.KINGS_BENCH_MASTERS_DAILY_CAUSE_LIST,
                KINGS_BENCH_MASTERS_DAILY_CAUSE_LIST_JSON_FILE_PATH,
                JUDGE)),
            Arguments.of(new ListTypeConfig(
                ListType.LEEDS_ADMINISTRATIVE_COURT_DAILY_CAUSE_LIST,
                ADMINISTRATIVE_COURT_DAILY_CAUSE_LIST_JSON_FILE_PATH,
                JUDGE)),
            Arguments.of(new ListTypeConfig(
                ListType.LONDON_ADMINISTRATIVE_COURT_DAILY_CAUSE_LIST,
                LONDON_ADMINISTRATIVE_COURT_DAILY_CAUSE_LIST_JSON_FILE_PATH,
                JUDGE, LONDON_ADMINISTRATIVE_COURT_NODE)),
            Arguments.of(new ListTypeConfig(
                ListType.LONDON_ADMINISTRATIVE_COURT_DAILY_CAUSE_LIST,
                LONDON_ADMINISTRATIVE_COURT_DAILY_CAUSE_LIST_JSON_FILE_PATH,
                JUDGE, PLANNING_COURT_NODE)),
            Arguments.of(new ListTypeConfig(
                ListType.MANCHESTER_ADMINISTRATIVE_COURT_DAILY_CAUSE_LIST,
                ADMINISTRATIVE_COURT_DAILY_CAUSE_LIST_JSON_FILE_PATH,
                JUDGE))
        );
    }

    public static Stream<Arguments> timeListMandatoryAttributes() {
        return Stream.of(
            Arguments.of(new ListTypeConfig(
                ListType.ADMIRALTY_COURT_KB_DAILY_CAUSE_LIST,
                ADMIRALTY_COURT_KB_DAILY_CAUSE_LIST_JSON_FILE_PATH,
                TIME)),
            Arguments.of(new ListTypeConfig(
                ListType.BIRMINGHAM_ADMINISTRATIVE_COURT_DAILY_CAUSE_LIST,
                ADMIRALTY_COURT_KB_DAILY_CAUSE_LIST_JSON_FILE_PATH,
                TIME)),
            Arguments.of(new ListTypeConfig(
                ListType.BRISTOL_AND_CARDIFF_ADMINISTRATIVE_COURT_DAILY_CAUSE_LIST,
                ADMIRALTY_COURT_KB_DAILY_CAUSE_LIST_JSON_FILE_PATH,
                TIME)),
            Arguments.of(new ListTypeConfig(
                ListType.BUSINESS_LIST_CHD_DAILY_CAUSE_LIST,
                BUSINESS_LIST_CHD_DAILY_CAUSE_LIST_JSON_FILE_PATH,
                TIME)),
            Arguments.of(new ListTypeConfig(
                ListType.CHANCERY_APPEALS_CHD_DAILY_CAUSE_LIST,
                CHANCERY_APPEALS_CHD_DAILY_CAUSE_LIST_JSON_FILE_PATH,
                TIME)),
            Arguments.of(new ListTypeConfig(
                ListType.CIVIL_COURTS_RCJ_DAILY_CAUSE_LIST,
                CIVIL_COURTS_RCJ_DAILY_CAUSE_LIST_JSON_FILE_PATH,
                TIME)),
            Arguments.of(new ListTypeConfig(
                ListType.COMMERCIAL_COURT_KB_DAILY_CAUSE_LIST,
                COMMERCIAL_COURT_KB_DAILY_CAUSE_LIST_JSON_FILE_PATH,
                TIME)),
            Arguments.of(new ListTypeConfig(
                ListType.COMPANIES_WINDING_UP_CHD_DAILY_CAUSE_LIST,
                COMPANIES_WINDING_UP_CHD_DAILY_CAUSE_LIST_JSON_FILE_PATH,
                TIME)),
            Arguments.of(new ListTypeConfig(
                ListType.COMPETITION_LIST_CHD_DAILY_CAUSE_LIST,
                COMPETITION_LIST_CHD_DAILY_CAUSE_LIST_JSON_FILE_PATH,
                TIME)),
            Arguments.of(new ListTypeConfig(
                ListType.COUNTY_COURT_LONDON_CIVIL_DAILY_CAUSE_LIST,
                COUNTY_COURT_LONDON_CIVIL_DAILY_CAUSE_LIST_JSON_FILE_PATH,
                TIME)),
            Arguments.of(new ListTypeConfig(
                ListType.COURT_OF_APPEAL_CIVIL_DAILY_CAUSE_LIST,
                COURT_OF_APPEAL_CIVIL_DAILY_CAUSE_LIST_JSON_FILE_PATH,
                TIME, HEARING_LIST_NODE)),
            Arguments.of(new ListTypeConfig(
                ListType.COURT_OF_APPEAL_CIVIL_DAILY_CAUSE_LIST,
                COURT_OF_APPEAL_CIVIL_DAILY_CAUSE_LIST_JSON_FILE_PATH,
                TIME, FUTURE_JUDGMENTS_NODE)),
            Arguments.of(new ListTypeConfig(
                ListType.COURT_OF_APPEAL_CRIMINAL_DAILY_CAUSE_LIST,
                COURT_OF_APPEAL_CRIMINAL_DAILY_CAUSE_LIST_JSON_FILE_PATH,
                TIME)),
            Arguments.of(new ListTypeConfig(
                ListType.FAMILY_DIVISION_HIGH_COURT_DAILY_CAUSE_LIST,
                FAMILY_DIVISION_HIGH_COURT_DAILY_CAUSE_LIST_JSON_FILE_PATH,
                TIME)),
            Arguments.of(new ListTypeConfig(
                ListType.FINANCIAL_LIST_CHD_KB_DAILY_CAUSE_LIST,
                FINANCIAL_LISTS_CHD_DAILY_CAUSE_LIST_JSON_FILE_PATH,
                TIME)),
            Arguments.of(new ListTypeConfig(
                ListType.INTELLECTUAL_PROPERTY_AND_ENTERPRISE_COURT_DAILY_CAUSE_LIST,
                INTELLECTUAL_PROPERTY_AND_ENTERPRISE_COURT_DAILY_CAUSE_LIST_JSON_FILE_PATH,
                TIME)),
            Arguments.of(new ListTypeConfig(
                ListType.INTELLECTUAL_PROPERTY_LIST_CHD_DAILY_CAUSE_LIST,
                INTELLECTUAL_PROPERTY_LIST_CHD_DAILY_CAUSE_LIST_JSON_FILE_PATH,
                TIME)),
            Arguments.of(new ListTypeConfig(
                ListType.INTERIM_APPLICATIONS_CHD_DAILY_CAUSE_LIST,
                INTERIM_APPLICATION_CHANCERY_LIST_CHD_DAILY_CAUSE_LIST_JSON_FILE_PATH,
                TIME, HEARING_LIST_NODE)),
            Arguments.of(new ListTypeConfig(
                ListType.KINGS_BENCH_DIVISION_DAILY_CAUSE_LIST,
                KINGS_BENCH_DIVISION_DAILY_CAUSE_LIST_JSON_FILE_PATH,
                TIME)),
            Arguments.of(new ListTypeConfig(
                ListType.KINGS_BENCH_MASTERS_DAILY_CAUSE_LIST,
                KINGS_BENCH_MASTERS_DAILY_CAUSE_LIST_JSON_FILE_PATH,
                TIME)),
            Arguments.of(new ListTypeConfig(
                ListType.LEEDS_ADMINISTRATIVE_COURT_DAILY_CAUSE_LIST,
                ADMINISTRATIVE_COURT_DAILY_CAUSE_LIST_JSON_FILE_PATH,
                TIME)),
            Arguments.of(new ListTypeConfig(
                ListType.LONDON_ADMINISTRATIVE_COURT_DAILY_CAUSE_LIST,
                LONDON_ADMINISTRATIVE_COURT_DAILY_CAUSE_LIST_JSON_FILE_PATH,
                TIME, LONDON_ADMINISTRATIVE_COURT_NODE)),
            Arguments.of(new ListTypeConfig(
                ListType.LONDON_ADMINISTRATIVE_COURT_DAILY_CAUSE_LIST,
                LONDON_ADMINISTRATIVE_COURT_DAILY_CAUSE_LIST_JSON_FILE_PATH,
                TIME, PLANNING_COURT_NODE)),
            Arguments.of(new ListTypeConfig(
                ListType.MANCHESTER_ADMINISTRATIVE_COURT_DAILY_CAUSE_LIST,
                ADMINISTRATIVE_COURT_DAILY_CAUSE_LIST_JSON_FILE_PATH,
                TIME))
        );
    }

    public static Stream<Arguments> typeListMandatoryAttributes() {
        return Stream.of(
            Arguments.of(new ListTypeConfig(
                ListType.ADMIRALTY_COURT_KB_DAILY_CAUSE_LIST,
                ADMIRALTY_COURT_KB_DAILY_CAUSE_LIST_JSON_FILE_PATH,
                TYPE)),
            Arguments.of(new ListTypeConfig(
                ListType.BUSINESS_LIST_CHD_DAILY_CAUSE_LIST,
                BUSINESS_LIST_CHD_DAILY_CAUSE_LIST_JSON_FILE_PATH,
                TYPE)),
            Arguments.of(new ListTypeConfig(
                ListType.CHANCERY_APPEALS_CHD_DAILY_CAUSE_LIST,
                CHANCERY_APPEALS_CHD_DAILY_CAUSE_LIST_JSON_FILE_PATH,
                TYPE)),
            Arguments.of(new ListTypeConfig(
                ListType.COMMERCIAL_COURT_KB_DAILY_CAUSE_LIST,
                COMMERCIAL_COURT_KB_DAILY_CAUSE_LIST_JSON_FILE_PATH,
                TYPE)),
            Arguments.of(new ListTypeConfig(
                ListType.COMPANIES_WINDING_UP_CHD_DAILY_CAUSE_LIST,
                COMPANIES_WINDING_UP_CHD_DAILY_CAUSE_LIST_JSON_FILE_PATH,
                TYPE)),
            Arguments.of(new ListTypeConfig(
                ListType.COMPETITION_LIST_CHD_DAILY_CAUSE_LIST,
                COMPETITION_LIST_CHD_DAILY_CAUSE_LIST_JSON_FILE_PATH,
                TYPE)),
            Arguments.of(new ListTypeConfig(
                ListType.FINANCIAL_LIST_CHD_KB_DAILY_CAUSE_LIST,
                FINANCIAL_LISTS_CHD_DAILY_CAUSE_LIST_JSON_FILE_PATH,
                TYPE)),
            Arguments.of(new ListTypeConfig(
                ListType.INSOLVENCY_AND_COMPANIES_COURT_CHD_DAILY_CAUSE_LIST,
                INSOLVENCY_AND_COMPANIES_COURT_CHD_DAILY_CAUSE_LIST_JSON_FILE_PATH,
                TYPE)),
            Arguments.of(new ListTypeConfig(
                ListType.INTELLECTUAL_PROPERTY_AND_ENTERPRISE_COURT_DAILY_CAUSE_LIST,
                INTELLECTUAL_PROPERTY_AND_ENTERPRISE_COURT_DAILY_CAUSE_LIST_JSON_FILE_PATH,
                TYPE)),
            Arguments.of(new ListTypeConfig(
                ListType.INTELLECTUAL_PROPERTY_LIST_CHD_DAILY_CAUSE_LIST,
                INTELLECTUAL_PROPERTY_LIST_CHD_DAILY_CAUSE_LIST_JSON_FILE_PATH,
                TYPE)),
            Arguments.of(new ListTypeConfig(
                ListType.INTERIM_APPLICATIONS_CHD_DAILY_CAUSE_LIST,
                INTERIM_APPLICATION_CHANCERY_LIST_CHD_DAILY_CAUSE_LIST_JSON_FILE_PATH,
                TYPE, HEARING_LIST_NODE))
        );
    }

    public static Stream<Arguments> caseNumberListMandatoryAttributes() {
        return Stream.of(
            Arguments.of(new ListTypeConfig(
                ListType.ADMIRALTY_COURT_KB_DAILY_CAUSE_LIST,
                ADMIRALTY_COURT_KB_DAILY_CAUSE_LIST_JSON_FILE_PATH,
                CASE_NUMBER)),
            Arguments.of(new ListTypeConfig(
                ListType.BIRMINGHAM_ADMINISTRATIVE_COURT_DAILY_CAUSE_LIST,
                ADMINISTRATIVE_COURT_DAILY_CAUSE_LIST_JSON_FILE_PATH,
                CASE_NUMBER)),
            Arguments.of(new ListTypeConfig(
                ListType.BRISTOL_AND_CARDIFF_ADMINISTRATIVE_COURT_DAILY_CAUSE_LIST,
                ADMINISTRATIVE_COURT_DAILY_CAUSE_LIST_JSON_FILE_PATH,
                CASE_NUMBER)),
            Arguments.of(new ListTypeConfig(
                ListType.BUSINESS_LIST_CHD_DAILY_CAUSE_LIST,
                BUSINESS_LIST_CHD_DAILY_CAUSE_LIST_JSON_FILE_PATH,
                CASE_NUMBER)),
            Arguments.of(new ListTypeConfig(
                ListType.CHANCERY_APPEALS_CHD_DAILY_CAUSE_LIST,
                CHANCERY_APPEALS_CHD_DAILY_CAUSE_LIST_JSON_FILE_PATH,
                CASE_NUMBER)),
            Arguments.of(new ListTypeConfig(
                ListType.CIVIL_COURTS_RCJ_DAILY_CAUSE_LIST,
                CIVIL_COURTS_RCJ_DAILY_CAUSE_LIST_JSON_FILE_PATH,
                CASE_NUMBER)),
            Arguments.of(new ListTypeConfig(
                ListType.COMMERCIAL_COURT_KB_DAILY_CAUSE_LIST,
                COMMERCIAL_COURT_KB_DAILY_CAUSE_LIST_JSON_FILE_PATH,
                CASE_NUMBER)),
            Arguments.of(new ListTypeConfig(
                ListType.COMPANIES_WINDING_UP_CHD_DAILY_CAUSE_LIST,
                COMPANIES_WINDING_UP_CHD_DAILY_CAUSE_LIST_JSON_FILE_PATH,
                CASE_NUMBER)),
            Arguments.of(new ListTypeConfig(
                ListType.COMPETITION_LIST_CHD_DAILY_CAUSE_LIST,
                COMPETITION_LIST_CHD_DAILY_CAUSE_LIST_JSON_FILE_PATH,
                CASE_NUMBER)),
            Arguments.of(new ListTypeConfig(
                ListType.COUNTY_COURT_LONDON_CIVIL_DAILY_CAUSE_LIST,
                COUNTY_COURT_LONDON_CIVIL_DAILY_CAUSE_LIST_JSON_FILE_PATH,
                CASE_NUMBER)),
            Arguments.of(new ListTypeConfig(
                ListType.COURT_OF_APPEAL_CIVIL_DAILY_CAUSE_LIST,
                COURT_OF_APPEAL_CIVIL_DAILY_CAUSE_LIST_JSON_FILE_PATH,
                CASE_NUMBER, HEARING_LIST_NODE)),
            Arguments.of(new ListTypeConfig(
                ListType.COURT_OF_APPEAL_CIVIL_DAILY_CAUSE_LIST,
                COURT_OF_APPEAL_CIVIL_DAILY_CAUSE_LIST_JSON_FILE_PATH,
                CASE_NUMBER, FUTURE_JUDGMENTS_NODE)),
            Arguments.of(new ListTypeConfig(
                ListType.COURT_OF_APPEAL_CRIMINAL_DAILY_CAUSE_LIST,
                COURT_OF_APPEAL_CRIMINAL_DAILY_CAUSE_LIST_JSON_FILE_PATH,
                CASE_NUMBER)),
            Arguments.of(new ListTypeConfig(
                ListType.FAMILY_DIVISION_HIGH_COURT_DAILY_CAUSE_LIST,
                FAMILY_DIVISION_HIGH_COURT_DAILY_CAUSE_LIST_JSON_FILE_PATH,
                CASE_NUMBER)),
            Arguments.of(new ListTypeConfig(
                ListType.FINANCIAL_LIST_CHD_KB_DAILY_CAUSE_LIST,
                FINANCIAL_LISTS_CHD_DAILY_CAUSE_LIST_JSON_FILE_PATH,
                CASE_NUMBER)),
            Arguments.of(new ListTypeConfig(
                ListType.INTELLECTUAL_PROPERTY_AND_ENTERPRISE_COURT_DAILY_CAUSE_LIST,
                INTELLECTUAL_PROPERTY_AND_ENTERPRISE_COURT_DAILY_CAUSE_LIST_JSON_FILE_PATH,
                CASE_NUMBER)),
            Arguments.of(new ListTypeConfig(
                ListType.INTELLECTUAL_PROPERTY_LIST_CHD_DAILY_CAUSE_LIST,
                INTELLECTUAL_PROPERTY_LIST_CHD_DAILY_CAUSE_LIST_JSON_FILE_PATH,
                CASE_NUMBER)),
            Arguments.of(new ListTypeConfig(
                ListType.INTERIM_APPLICATIONS_CHD_DAILY_CAUSE_LIST,
                INTERIM_APPLICATION_CHANCERY_LIST_CHD_DAILY_CAUSE_LIST_JSON_FILE_PATH,
                CASE_NUMBER, HEARING_LIST_NODE)),
            Arguments.of(new ListTypeConfig(
                ListType.KINGS_BENCH_DIVISION_DAILY_CAUSE_LIST,
                KINGS_BENCH_DIVISION_DAILY_CAUSE_LIST_JSON_FILE_PATH,
                CASE_NUMBER)),
            Arguments.of(new ListTypeConfig(
                ListType.KINGS_BENCH_MASTERS_DAILY_CAUSE_LIST,
                KINGS_BENCH_MASTERS_DAILY_CAUSE_LIST_JSON_FILE_PATH,
                CASE_NUMBER)),
            Arguments.of(new ListTypeConfig(
                ListType.LEEDS_ADMINISTRATIVE_COURT_DAILY_CAUSE_LIST,
                ADMINISTRATIVE_COURT_DAILY_CAUSE_LIST_JSON_FILE_PATH,
                CASE_NUMBER)),
            Arguments.of(new ListTypeConfig(
                ListType.LONDON_ADMINISTRATIVE_COURT_DAILY_CAUSE_LIST,
                LONDON_ADMINISTRATIVE_COURT_DAILY_CAUSE_LIST_JSON_FILE_PATH,
                CASE_NUMBER, LONDON_ADMINISTRATIVE_COURT_NODE)),
            Arguments.of(new ListTypeConfig(
                ListType.LONDON_ADMINISTRATIVE_COURT_DAILY_CAUSE_LIST,
                LONDON_ADMINISTRATIVE_COURT_DAILY_CAUSE_LIST_JSON_FILE_PATH,
                CASE_NUMBER, PLANNING_COURT_NODE)),
            Arguments.of(new ListTypeConfig(
                ListType.MANCHESTER_ADMINISTRATIVE_COURT_DAILY_CAUSE_LIST,
                ADMINISTRATIVE_COURT_DAILY_CAUSE_LIST_JSON_FILE_PATH,
                CASE_NUMBER))
        );
    }

    public static Stream<Arguments> caseNameListMandatoryAttributes() {
        return Stream.of(
            Arguments.of(new ListTypeConfig(
                ListType.ADMIRALTY_COURT_KB_DAILY_CAUSE_LIST,
                ADMIRALTY_COURT_KB_DAILY_CAUSE_LIST_JSON_FILE_PATH,
                CASE_NAME)),
            Arguments.of(new ListTypeConfig(
                ListType.BUSINESS_LIST_CHD_DAILY_CAUSE_LIST,
                BUSINESS_LIST_CHD_DAILY_CAUSE_LIST_JSON_FILE_PATH,
                CASE_NAME)),
            Arguments.of(new ListTypeConfig(
                ListType.CHANCERY_APPEALS_CHD_DAILY_CAUSE_LIST,
                CHANCERY_APPEALS_CHD_DAILY_CAUSE_LIST_JSON_FILE_PATH,
                CASE_NAME)),
            Arguments.of(new ListTypeConfig(
                ListType.CIC_WEEKLY_HEARING_LIST,
                CIC_WEEKLY_HEARING_LIST_JSON_FILE_PATH,
                CASE_NAME)),
            Arguments.of(new ListTypeConfig(
                ListType.COMMERCIAL_COURT_KB_DAILY_CAUSE_LIST,
                COMMERCIAL_COURT_KB_DAILY_CAUSE_LIST_JSON_FILE_PATH,
                CASE_NAME)),
            Arguments.of(new ListTypeConfig(
                ListType.COMPANIES_WINDING_UP_CHD_DAILY_CAUSE_LIST,
                COMPANIES_WINDING_UP_CHD_DAILY_CAUSE_LIST_JSON_FILE_PATH,
                CASE_NAME)),
            Arguments.of(new ListTypeConfig(
                ListType.COMPETITION_LIST_CHD_DAILY_CAUSE_LIST,
                COMPETITION_LIST_CHD_DAILY_CAUSE_LIST_JSON_FILE_PATH,
                CASE_NAME)),
            Arguments.of(new ListTypeConfig(
                ListType.CST_WEEKLY_HEARING_LIST,
                CST_WEEKLY_HEARING_LIST_JSON_FILE_PATH,
                CASE_NAME)),
            Arguments.of(new ListTypeConfig(
                ListType.FINANCIAL_LIST_CHD_KB_DAILY_CAUSE_LIST,
                FINANCIAL_LISTS_CHD_DAILY_CAUSE_LIST_JSON_FILE_PATH,
                CASE_NAME)),
            Arguments.of(new ListTypeConfig(
                ListType.FTT_LR_WEEKLY_HEARING_LIST,
                FTT_LR_WEEKLY_HEARING_LIST_JSON_FILE_PATH,
                CASE_NAME)),
            Arguments.of(new ListTypeConfig(
                ListType.FTT_TAX_WEEKLY_HEARING_LIST,
                FTT_TAX_WEEKLY_HEARING_LIST_JSON_FILE_PATH,
                CASE_NAME)),
            Arguments.of(new ListTypeConfig(
                ListType.GRC_WEEKLY_HEARING_LIST,
                GRC_WEEKLY_HEARING_LIST_JSON_FILE_PATH,
                CASE_NAME)),
            Arguments.of(new ListTypeConfig(
                ListType.INSOLVENCY_AND_COMPANIES_COURT_CHD_DAILY_CAUSE_LIST,
                INSOLVENCY_AND_COMPANIES_COURT_CHD_DAILY_CAUSE_LIST_JSON_FILE_PATH,
                CASE_NAME)),
            Arguments.of(new ListTypeConfig(
                ListType.INTELLECTUAL_PROPERTY_AND_ENTERPRISE_COURT_DAILY_CAUSE_LIST,
                INTELLECTUAL_PROPERTY_AND_ENTERPRISE_COURT_DAILY_CAUSE_LIST_JSON_FILE_PATH,
                CASE_NAME)),
            Arguments.of(new ListTypeConfig(
                ListType.INTELLECTUAL_PROPERTY_LIST_CHD_DAILY_CAUSE_LIST,
                INTELLECTUAL_PROPERTY_LIST_CHD_DAILY_CAUSE_LIST_JSON_FILE_PATH,
                CASE_NAME)),
            Arguments.of(new ListTypeConfig(
                ListType.INTERIM_APPLICATIONS_CHD_DAILY_CAUSE_LIST,
                INTERIM_APPLICATION_CHANCERY_LIST_CHD_DAILY_CAUSE_LIST_JSON_FILE_PATH,
                CASE_NAME, HEARING_LIST_NODE))
        );
    }

    public static Stream<Arguments> additionalInformationListMandatoryAttributes() {
        return Stream.of(
            Arguments.of(new ListTypeConfig(
                ListType.ADMIRALTY_COURT_KB_DAILY_CAUSE_LIST,
                ADMIRALTY_COURT_KB_DAILY_CAUSE_LIST_JSON_FILE_PATH,
                ADDITIONAL_INFORMATION)),
            Arguments.of(new ListTypeConfig(
                ListType.AST_DAILY_HEARING_LIST,
                AST_DAILY_HEARING_LIST_JSON_FILE_PATH,
                ADDITIONAL_INFORMATION)),
            Arguments.of(new ListTypeConfig(
                ListType.BIRMINGHAM_ADMINISTRATIVE_COURT_DAILY_CAUSE_LIST,
                ADMINISTRATIVE_COURT_DAILY_CAUSE_LIST_JSON_FILE_PATH,
                ADDITIONAL_INFORMATION)),
            Arguments.of(new ListTypeConfig(
                ListType.BRISTOL_AND_CARDIFF_ADMINISTRATIVE_COURT_DAILY_CAUSE_LIST,
                ADMINISTRATIVE_COURT_DAILY_CAUSE_LIST_JSON_FILE_PATH,
                ADDITIONAL_INFORMATION)),
            Arguments.of(new ListTypeConfig(
                ListType.BUSINESS_LIST_CHD_DAILY_CAUSE_LIST,
                BUSINESS_LIST_CHD_DAILY_CAUSE_LIST_JSON_FILE_PATH,
                ADDITIONAL_INFORMATION)),
            Arguments.of(new ListTypeConfig(
                ListType.CHANCERY_APPEALS_CHD_DAILY_CAUSE_LIST,
                CHANCERY_APPEALS_CHD_DAILY_CAUSE_LIST_JSON_FILE_PATH,
                ADDITIONAL_INFORMATION)),
            Arguments.of(new ListTypeConfig(
                ListType.CIC_WEEKLY_HEARING_LIST,
                CIC_WEEKLY_HEARING_LIST_JSON_FILE_PATH,
                ADDITIONAL_INFORMATION)),
            Arguments.of(new ListTypeConfig(
                ListType.CIVIL_COURTS_RCJ_DAILY_CAUSE_LIST,
                CIVIL_COURTS_RCJ_DAILY_CAUSE_LIST_JSON_FILE_PATH,
                ADDITIONAL_INFORMATION)),
            Arguments.of(new ListTypeConfig(
                ListType.COMMERCIAL_COURT_KB_DAILY_CAUSE_LIST,
                COMMERCIAL_COURT_KB_DAILY_CAUSE_LIST_JSON_FILE_PATH,
                ADDITIONAL_INFORMATION)),
            Arguments.of(new ListTypeConfig(
                ListType.COMPANIES_WINDING_UP_CHD_DAILY_CAUSE_LIST,
                COMPANIES_WINDING_UP_CHD_DAILY_CAUSE_LIST_JSON_FILE_PATH,
                ADDITIONAL_INFORMATION)),
            Arguments.of(new ListTypeConfig(
                ListType.COMPETITION_LIST_CHD_DAILY_CAUSE_LIST,
                COMPETITION_LIST_CHD_DAILY_CAUSE_LIST_JSON_FILE_PATH,
                ADDITIONAL_INFORMATION)),
            Arguments.of(new ListTypeConfig(
                ListType.COUNTY_COURT_LONDON_CIVIL_DAILY_CAUSE_LIST,
                COUNTY_COURT_LONDON_CIVIL_DAILY_CAUSE_LIST_JSON_FILE_PATH,
                ADDITIONAL_INFORMATION)),
            Arguments.of(new ListTypeConfig(
                ListType.COURT_OF_APPEAL_CIVIL_DAILY_CAUSE_LIST,
                COURT_OF_APPEAL_CIVIL_DAILY_CAUSE_LIST_JSON_FILE_PATH,
                ADDITIONAL_INFORMATION, HEARING_LIST_NODE)),
            Arguments.of(new ListTypeConfig(
                ListType.COURT_OF_APPEAL_CIVIL_DAILY_CAUSE_LIST,
                COURT_OF_APPEAL_CIVIL_DAILY_CAUSE_LIST_JSON_FILE_PATH,
                ADDITIONAL_INFORMATION, FUTURE_JUDGMENTS_NODE)),
            Arguments.of(new ListTypeConfig(
                ListType.COURT_OF_APPEAL_CRIMINAL_DAILY_CAUSE_LIST,
                COURT_OF_APPEAL_CRIMINAL_DAILY_CAUSE_LIST_JSON_FILE_PATH,
                ADDITIONAL_INFORMATION)),
            Arguments.of(new ListTypeConfig(
                ListType.CST_WEEKLY_HEARING_LIST,
                CST_WEEKLY_HEARING_LIST_JSON_FILE_PATH,
                ADDITIONAL_INFORMATION)),
            Arguments.of(new ListTypeConfig(
                ListType.FAMILY_DIVISION_HIGH_COURT_DAILY_CAUSE_LIST,
                FAMILY_DIVISION_HIGH_COURT_DAILY_CAUSE_LIST_JSON_FILE_PATH,
                ADDITIONAL_INFORMATION)),
            Arguments.of(new ListTypeConfig(
                ListType.FINANCIAL_LIST_CHD_KB_DAILY_CAUSE_LIST,
                FINANCIAL_LISTS_CHD_DAILY_CAUSE_LIST_JSON_FILE_PATH,
                ADDITIONAL_INFORMATION)),
            Arguments.of(new ListTypeConfig(
                ListType.GRC_WEEKLY_HEARING_LIST,
                GRC_WEEKLY_HEARING_LIST_JSON_FILE_PATH,
                ADDITIONAL_INFORMATION)),
            Arguments.of(new ListTypeConfig(
                ListType.INSOLVENCY_AND_COMPANIES_COURT_CHD_DAILY_CAUSE_LIST,
                INSOLVENCY_AND_COMPANIES_COURT_CHD_DAILY_CAUSE_LIST_JSON_FILE_PATH,
                ADDITIONAL_INFORMATION)),
            Arguments.of(new ListTypeConfig(
                ListType.INTELLECTUAL_PROPERTY_AND_ENTERPRISE_COURT_DAILY_CAUSE_LIST,
                INTELLECTUAL_PROPERTY_AND_ENTERPRISE_COURT_DAILY_CAUSE_LIST_JSON_FILE_PATH,
                ADDITIONAL_INFORMATION)),
            Arguments.of(new ListTypeConfig(
                ListType.INTELLECTUAL_PROPERTY_LIST_CHD_DAILY_CAUSE_LIST,
                INTELLECTUAL_PROPERTY_LIST_CHD_DAILY_CAUSE_LIST_JSON_FILE_PATH,
                ADDITIONAL_INFORMATION)),
            Arguments.of(new ListTypeConfig(
                ListType.INTERIM_APPLICATIONS_CHD_DAILY_CAUSE_LIST,
                INTERIM_APPLICATION_CHANCERY_LIST_CHD_DAILY_CAUSE_LIST_JSON_FILE_PATH,
                ADDITIONAL_INFORMATION, HEARING_LIST_NODE)),
            Arguments.of(new ListTypeConfig(
                ListType.KINGS_BENCH_DIVISION_DAILY_CAUSE_LIST,
                KINGS_BENCH_DIVISION_DAILY_CAUSE_LIST_JSON_FILE_PATH,
                ADDITIONAL_INFORMATION)),
            Arguments.of(new ListTypeConfig(
                ListType.KINGS_BENCH_MASTERS_DAILY_CAUSE_LIST,
                KINGS_BENCH_MASTERS_DAILY_CAUSE_LIST_JSON_FILE_PATH,
                ADDITIONAL_INFORMATION)),
            Arguments.of(new ListTypeConfig(
                ListType.LEEDS_ADMINISTRATIVE_COURT_DAILY_CAUSE_LIST,
                ADMINISTRATIVE_COURT_DAILY_CAUSE_LIST_JSON_FILE_PATH,
                ADDITIONAL_INFORMATION)),
            Arguments.of(new ListTypeConfig(
                ListType.LONDON_ADMINISTRATIVE_COURT_DAILY_CAUSE_LIST,
                LONDON_ADMINISTRATIVE_COURT_DAILY_CAUSE_LIST_JSON_FILE_PATH,
                ADDITIONAL_INFORMATION, LONDON_ADMINISTRATIVE_COURT_NODE)),
            Arguments.of(new ListTypeConfig(
                ListType.LONDON_ADMINISTRATIVE_COURT_DAILY_CAUSE_LIST,
                LONDON_ADMINISTRATIVE_COURT_DAILY_CAUSE_LIST_JSON_FILE_PATH,
                ADDITIONAL_INFORMATION, PLANNING_COURT_NODE)),
            Arguments.of(new ListTypeConfig(
                ListType.MANCHESTER_ADMINISTRATIVE_COURT_DAILY_CAUSE_LIST,
                ADMINISTRATIVE_COURT_DAILY_CAUSE_LIST_JSON_FILE_PATH,
                ADDITIONAL_INFORMATION))
        );
    }

    public static Stream<Arguments> appellantListMandatoryAttributes() {
        return Stream.of(
            Arguments.of(new ListTypeConfig(
                ListType.AST_DAILY_HEARING_LIST,
                AST_DAILY_HEARING_LIST_JSON_FILE_PATH,
                APPELLANT))
        );
    }

    public static Stream<Arguments> appealReferenceNumberListMandatoryAttributes() {
        return Stream.of(
            Arguments.of(new ListTypeConfig(
                ListType.AST_DAILY_HEARING_LIST,
                AST_DAILY_HEARING_LIST_JSON_FILE_PATH,
                APPEAL_REFERENCE_NUMBER))
        );
    }

    public static Stream<Arguments> caseTypeListMandatoryAttributes() {
        return Stream.of(
            Arguments.of(new ListTypeConfig(
                ListType.AST_DAILY_HEARING_LIST,
                AST_DAILY_HEARING_LIST_JSON_FILE_PATH,
                CASE_TYPE))
        );
    }

    public static Stream<Arguments> hearingTypeListMandatoryAttributes() {
        return Stream.of(
            Arguments.of(new ListTypeConfig(
                ListType.AST_DAILY_HEARING_LIST,
                AST_DAILY_HEARING_LIST_JSON_FILE_PATH,
                HEARING_TYPE)),
            Arguments.of(new ListTypeConfig(
                ListType.BIRMINGHAM_ADMINISTRATIVE_COURT_DAILY_CAUSE_LIST,
                ADMINISTRATIVE_COURT_DAILY_CAUSE_LIST_JSON_FILE_PATH,
                HEARING_TYPE)),
            Arguments.of(new ListTypeConfig(
                ListType.BRISTOL_AND_CARDIFF_ADMINISTRATIVE_COURT_DAILY_CAUSE_LIST,
                ADMINISTRATIVE_COURT_DAILY_CAUSE_LIST_JSON_FILE_PATH,
                HEARING_TYPE)),
            Arguments.of(new ListTypeConfig(
                ListType.CIVIL_COURTS_RCJ_DAILY_CAUSE_LIST,
                CIVIL_COURTS_RCJ_DAILY_CAUSE_LIST_JSON_FILE_PATH,
                HEARING_TYPE)),
            Arguments.of(new ListTypeConfig(
                ListType.COUNTY_COURT_LONDON_CIVIL_DAILY_CAUSE_LIST,
                COUNTY_COURT_LONDON_CIVIL_DAILY_CAUSE_LIST_JSON_FILE_PATH,
                HEARING_TYPE)),
            Arguments.of(new ListTypeConfig(
                ListType.COURT_OF_APPEAL_CIVIL_DAILY_CAUSE_LIST,
                COURT_OF_APPEAL_CIVIL_DAILY_CAUSE_LIST_JSON_FILE_PATH,
                HEARING_TYPE, HEARING_LIST_NODE)),
            Arguments.of(new ListTypeConfig(
                ListType.COURT_OF_APPEAL_CIVIL_DAILY_CAUSE_LIST,
                COURT_OF_APPEAL_CIVIL_DAILY_CAUSE_LIST_JSON_FILE_PATH,
                HEARING_TYPE, FUTURE_JUDGMENTS_NODE)),
            Arguments.of(new ListTypeConfig(
                ListType.COURT_OF_APPEAL_CRIMINAL_DAILY_CAUSE_LIST,
                COURT_OF_APPEAL_CRIMINAL_DAILY_CAUSE_LIST_JSON_FILE_PATH,
                HEARING_TYPE)),
            Arguments.of(new ListTypeConfig(
                ListType.CST_WEEKLY_HEARING_LIST,
                CST_WEEKLY_HEARING_LIST_JSON_FILE_PATH,
                HEARING_TYPE)),
            Arguments.of(new ListTypeConfig(
                ListType.FAMILY_DIVISION_HIGH_COURT_DAILY_CAUSE_LIST,
                FAMILY_DIVISION_HIGH_COURT_DAILY_CAUSE_LIST_JSON_FILE_PATH,
                HEARING_TYPE)),
            Arguments.of(new ListTypeConfig(
                ListType.KINGS_BENCH_DIVISION_DAILY_CAUSE_LIST,
                KINGS_BENCH_DIVISION_DAILY_CAUSE_LIST_JSON_FILE_PATH,
                HEARING_TYPE)),
            Arguments.of(new ListTypeConfig(
                ListType.KINGS_BENCH_MASTERS_DAILY_CAUSE_LIST,
                KINGS_BENCH_MASTERS_DAILY_CAUSE_LIST_JSON_FILE_PATH,
                HEARING_TYPE)),
            Arguments.of(new ListTypeConfig(
                ListType.LEEDS_ADMINISTRATIVE_COURT_DAILY_CAUSE_LIST,
                ADMINISTRATIVE_COURT_DAILY_CAUSE_LIST_JSON_FILE_PATH,
                HEARING_TYPE)),
            Arguments.of(new ListTypeConfig(
                ListType.LONDON_ADMINISTRATIVE_COURT_DAILY_CAUSE_LIST,
                LONDON_ADMINISTRATIVE_COURT_DAILY_CAUSE_LIST_JSON_FILE_PATH,
                HEARING_TYPE, LONDON_ADMINISTRATIVE_COURT_NODE)),
            Arguments.of(new ListTypeConfig(
                ListType.LONDON_ADMINISTRATIVE_COURT_DAILY_CAUSE_LIST,
                LONDON_ADMINISTRATIVE_COURT_DAILY_CAUSE_LIST_JSON_FILE_PATH,
                HEARING_TYPE, PLANNING_COURT_NODE)),
            Arguments.of(new ListTypeConfig(
                ListType.MANCHESTER_ADMINISTRATIVE_COURT_DAILY_CAUSE_LIST,
                ADMINISTRATIVE_COURT_DAILY_CAUSE_LIST_JSON_FILE_PATH,
                HEARING_TYPE))
        );
    }

    public static Stream<Arguments> hearingTimeListMandatoryAttributes() {
        return Stream.of(
            Arguments.of(new ListTypeConfig(
                ListType.AST_DAILY_HEARING_LIST,
                AST_DAILY_HEARING_LIST_JSON_FILE_PATH,
                HEARING_TIME)),
            Arguments.of(new ListTypeConfig(
                ListType.CIC_WEEKLY_HEARING_LIST,
                CIC_WEEKLY_HEARING_LIST_JSON_FILE_PATH,
                HEARING_TIME)),
            Arguments.of(new ListTypeConfig(
                ListType.FTT_LR_WEEKLY_HEARING_LIST,
                FTT_LR_WEEKLY_HEARING_LIST_JSON_FILE_PATH,
                HEARING_TIME)),
            Arguments.of(new ListTypeConfig(
                ListType.FTT_TAX_WEEKLY_HEARING_LIST,
                FTT_TAX_WEEKLY_HEARING_LIST_JSON_FILE_PATH,
                HEARING_TIME)),
            Arguments.of(new ListTypeConfig(
                ListType.GRC_WEEKLY_HEARING_LIST,
                GRC_WEEKLY_HEARING_LIST_JSON_FILE_PATH,
                HEARING_TIME))
        );
    }

    public static Stream<Arguments> caseDetailsListMandatoryAttributes() {
        return Stream.of(
            Arguments.of(new ListTypeConfig(
                ListType.BIRMINGHAM_ADMINISTRATIVE_COURT_DAILY_CAUSE_LIST,
                ADMINISTRATIVE_COURT_DAILY_CAUSE_LIST_JSON_FILE_PATH,
                CASE_DETAILS)),
            Arguments.of(new ListTypeConfig(
                ListType.BRISTOL_AND_CARDIFF_ADMINISTRATIVE_COURT_DAILY_CAUSE_LIST,
                ADMINISTRATIVE_COURT_DAILY_CAUSE_LIST_JSON_FILE_PATH,
                CASE_DETAILS)),
            Arguments.of(new ListTypeConfig(
                ListType.CIVIL_COURTS_RCJ_DAILY_CAUSE_LIST,
                CIVIL_COURTS_RCJ_DAILY_CAUSE_LIST_JSON_FILE_PATH,
                CASE_DETAILS)),
            Arguments.of(new ListTypeConfig(
                ListType.COUNTY_COURT_LONDON_CIVIL_DAILY_CAUSE_LIST,
                COUNTY_COURT_LONDON_CIVIL_DAILY_CAUSE_LIST_JSON_FILE_PATH,
                CASE_DETAILS)),
            Arguments.of(new ListTypeConfig(
                ListType.COURT_OF_APPEAL_CIVIL_DAILY_CAUSE_LIST,
                COURT_OF_APPEAL_CIVIL_DAILY_CAUSE_LIST_JSON_FILE_PATH,
                CASE_DETAILS, HEARING_LIST_NODE)),
            Arguments.of(new ListTypeConfig(
                ListType.COURT_OF_APPEAL_CIVIL_DAILY_CAUSE_LIST,
                COURT_OF_APPEAL_CIVIL_DAILY_CAUSE_LIST_JSON_FILE_PATH,
                CASE_DETAILS, FUTURE_JUDGMENTS_NODE)),
            Arguments.of(new ListTypeConfig(
                ListType.COURT_OF_APPEAL_CRIMINAL_DAILY_CAUSE_LIST,
                COURT_OF_APPEAL_CRIMINAL_DAILY_CAUSE_LIST_JSON_FILE_PATH,
                CASE_DETAILS)),
            Arguments.of(new ListTypeConfig(
                ListType.FAMILY_DIVISION_HIGH_COURT_DAILY_CAUSE_LIST,
                FAMILY_DIVISION_HIGH_COURT_DAILY_CAUSE_LIST_JSON_FILE_PATH,
                CASE_DETAILS)),
            Arguments.of(new ListTypeConfig(
                ListType.KINGS_BENCH_DIVISION_DAILY_CAUSE_LIST,
                KINGS_BENCH_DIVISION_DAILY_CAUSE_LIST_JSON_FILE_PATH,
                CASE_DETAILS)),
            Arguments.of(new ListTypeConfig(
                ListType.KINGS_BENCH_MASTERS_DAILY_CAUSE_LIST,
                KINGS_BENCH_MASTERS_DAILY_CAUSE_LIST_JSON_FILE_PATH,
                CASE_DETAILS)),
            Arguments.of(new ListTypeConfig(
                ListType.LEEDS_ADMINISTRATIVE_COURT_DAILY_CAUSE_LIST,
                ADMINISTRATIVE_COURT_DAILY_CAUSE_LIST_JSON_FILE_PATH,
                CASE_DETAILS)),
            Arguments.of(new ListTypeConfig(
                ListType.LONDON_ADMINISTRATIVE_COURT_DAILY_CAUSE_LIST,
                LONDON_ADMINISTRATIVE_COURT_DAILY_CAUSE_LIST_JSON_FILE_PATH,
                CASE_DETAILS, LONDON_ADMINISTRATIVE_COURT_NODE)),
            Arguments.of(new ListTypeConfig(
                ListType.LONDON_ADMINISTRATIVE_COURT_DAILY_CAUSE_LIST,
                LONDON_ADMINISTRATIVE_COURT_DAILY_CAUSE_LIST_JSON_FILE_PATH,
                CASE_DETAILS, PLANNING_COURT_NODE)),
            Arguments.of(new ListTypeConfig(
                ListType.MANCHESTER_ADMINISTRATIVE_COURT_DAILY_CAUSE_LIST,
                ADMINISTRATIVE_COURT_DAILY_CAUSE_LIST_JSON_FILE_PATH,
                CASE_DETAILS))
        );
    }

    public static Stream<Arguments> dateListMandatoryAttributes() {
        return Stream.of(
            Arguments.of(new ListTypeConfig(
                ListType.CIC_WEEKLY_HEARING_LIST,
                CIC_WEEKLY_HEARING_LIST_JSON_FILE_PATH,
                DATE)),
            Arguments.of(new ListTypeConfig(
                ListType.CST_WEEKLY_HEARING_LIST,
                CST_WEEKLY_HEARING_LIST_JSON_FILE_PATH,
                DATE)),
            Arguments.of(new ListTypeConfig(
                ListType.FTT_LR_WEEKLY_HEARING_LIST,
                FTT_LR_WEEKLY_HEARING_LIST_JSON_FILE_PATH,
                DATE)),
            Arguments.of(new ListTypeConfig(
                ListType.FTT_TAX_WEEKLY_HEARING_LIST,
                FTT_TAX_WEEKLY_HEARING_LIST_JSON_FILE_PATH,
                DATE)),
            Arguments.of(new ListTypeConfig(
                ListType.GRC_WEEKLY_HEARING_LIST,
                GRC_WEEKLY_HEARING_LIST_JSON_FILE_PATH,
                DATE))
        );
    }

    public static Stream<Arguments> caseReferenceNumberListMandatoryAttributes() {
        return Stream.of(
            Arguments.of(new ListTypeConfig(
                ListType.CIC_WEEKLY_HEARING_LIST,
                CIC_WEEKLY_HEARING_LIST_JSON_FILE_PATH,
                CASE_REFERENCE_NUMBER)),
            Arguments.of(new ListTypeConfig(
                ListType.FTT_LR_WEEKLY_HEARING_LIST,
                FTT_LR_WEEKLY_HEARING_LIST_JSON_FILE_PATH,
                CASE_REFERENCE_NUMBER)),
            Arguments.of(new ListTypeConfig(
                ListType.FTT_TAX_WEEKLY_HEARING_LIST,
                FTT_TAX_WEEKLY_HEARING_LIST_JSON_FILE_PATH,
                CASE_REFERENCE_NUMBER)),
            Arguments.of(new ListTypeConfig(
                ListType.GRC_WEEKLY_HEARING_LIST,
                GRC_WEEKLY_HEARING_LIST_JSON_FILE_PATH,
                CASE_REFERENCE_NUMBER))
        );
    }

    public static Stream<Arguments> venuePlatformListMandatoryAttributes() {
        return Stream.of(
            Arguments.of(new ListTypeConfig(
                ListType.CIC_WEEKLY_HEARING_LIST,
                CIC_WEEKLY_HEARING_LIST_JSON_FILE_PATH,
                VENUE_PLATFORM)),
            Arguments.of(new ListTypeConfig(
                ListType.FTT_LR_WEEKLY_HEARING_LIST,
                FTT_LR_WEEKLY_HEARING_LIST_JSON_FILE_PATH,
                VENUE_PLATFORM)),
            Arguments.of(new ListTypeConfig(
                ListType.FTT_TAX_WEEKLY_HEARING_LIST,
                FTT_TAX_WEEKLY_HEARING_LIST_JSON_FILE_PATH,
                VENUE_PLATFORM))
        );
    }

    public static Stream<Arguments> membersListMandatoryAttributes() {
        return Stream.of(
            Arguments.of(new ListTypeConfig(
                ListType.CIC_WEEKLY_HEARING_LIST,
                CIC_WEEKLY_HEARING_LIST_JSON_FILE_PATH,
                MEMBERS)),
            Arguments.of(new ListTypeConfig(
                ListType.FTT_TAX_WEEKLY_HEARING_LIST,
                FTT_TAX_WEEKLY_HEARING_LIST_JSON_FILE_PATH,
                MEMBERS)),
            Arguments.of(new ListTypeConfig(
                ListType.GRC_WEEKLY_HEARING_LIST,
                GRC_WEEKLY_HEARING_LIST_JSON_FILE_PATH,
                MEMBERS))
        );
    }

    public static Stream<Arguments> judgesListMandatoryAttributes() {
        return Stream.of(
            Arguments.of(new ListTypeConfig(
                ListType.CIC_WEEKLY_HEARING_LIST,
                CIC_WEEKLY_HEARING_LIST_JSON_FILE_PATH,
                JUDGES)),
            Arguments.of(new ListTypeConfig(
                ListType.FTT_TAX_WEEKLY_HEARING_LIST,
                FTT_TAX_WEEKLY_HEARING_LIST_JSON_FILE_PATH,
                JUDGES)),
            Arguments.of(new ListTypeConfig(
                ListType.GRC_WEEKLY_HEARING_LIST,
                GRC_WEEKLY_HEARING_LIST_JSON_FILE_PATH,
                JUDGES))
        );
    }

    public static Stream<Arguments> hearingLengthMandatoryAttributes() {
        return Stream.of(
            Arguments.of(new ListTypeConfig(
                ListType.CST_WEEKLY_HEARING_LIST,
                CST_WEEKLY_HEARING_LIST_JSON_FILE_PATH,
                HEARING_LENGTH))
        );
    }

    public static Stream<Arguments> modeOfHearingMandatoryAttributes() {
        return Stream.of(
            Arguments.of(new ListTypeConfig(
                ListType.GRC_WEEKLY_HEARING_LIST,
                GRC_WEEKLY_HEARING_LIST_JSON_FILE_PATH,
                MODE_OF_HEARING))
        );
    }

    public static Stream<Arguments> hearingListMandatoryAttributes() {
        return Stream.of(
            Arguments.of(new ListTypeConfig(
                ListType.INTERIM_APPLICATIONS_CHD_DAILY_CAUSE_LIST,
                INTERIM_APPLICATION_CHANCERY_LIST_CHD_DAILY_CAUSE_LIST_JSON_FILE_PATH,
                HEARING_LIST_NODE))
        );
    }

    public static Stream<Arguments> openJusticeStatementDetailsMandatoryAttributes() {
        return Stream.of(
            Arguments.of(new ListTypeConfig(
                ListType.INTERIM_APPLICATIONS_CHD_DAILY_CAUSE_LIST,
                INTERIM_APPLICATION_CHANCERY_LIST_CHD_DAILY_CAUSE_LIST_JSON_FILE_PATH,
                OPEN_JUSTICE_DETAILS_NODE))
        );
    }

    public static Stream<Arguments> nameToBeDisplayedMandatoryAttributes() {
        return Stream.of(
            Arguments.of(new ListTypeConfig(
                ListType.INTERIM_APPLICATIONS_CHD_DAILY_CAUSE_LIST,
                INTERIM_APPLICATION_CHANCERY_LIST_CHD_DAILY_CAUSE_LIST_JSON_FILE_PATH,
                NAME_TO_BE_DISPLAYED, OPEN_JUSTICE_DETAILS_NODE))
        );
    }

    public static Stream<Arguments> emailMandatoryAttributes() {
        return Stream.of(
            Arguments.of(new ListTypeConfig(
                ListType.INTERIM_APPLICATIONS_CHD_DAILY_CAUSE_LIST,
                INTERIM_APPLICATION_CHANCERY_LIST_CHD_DAILY_CAUSE_LIST_JSON_FILE_PATH,
                EMAIL, OPEN_JUSTICE_DETAILS_NODE))
        );
    }
}
