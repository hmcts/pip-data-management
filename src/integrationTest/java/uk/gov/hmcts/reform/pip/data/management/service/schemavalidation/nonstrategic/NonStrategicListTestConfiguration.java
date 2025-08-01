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
    public static final String PARENT_JSON_FILE_PATH = "data/non-strategic";
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
                "venue")),
            Arguments.of(new ListTypeConfig(
                ListType.BIRMINGHAM_ADMINISTRATIVE_COURT_DAILY_CAUSE_LIST,
                ADMINISTRATIVE_COURT_DAILY_CAUSE_LIST_JSON_FILE_PATH,
                "venue")),
            Arguments.of(new ListTypeConfig(
                ListType.BRISTOL_AND_CARDIFF_ADMINISTRATIVE_COURT_DAILY_CAUSE_LIST,
                ADMINISTRATIVE_COURT_DAILY_CAUSE_LIST_JSON_FILE_PATH,
                "venue")),
            Arguments.of(new ListTypeConfig(
                ListType.BUSINESS_LIST_CHD_DAILY_CAUSE_LIST,
                BUSINESS_LIST_CHD_DAILY_CAUSE_LIST_JSON_FILE_PATH,
                "venue")),
            Arguments.of(new ListTypeConfig(
                ListType.CHANCERY_APPEALS_CHD_DAILY_CAUSE_LIST,
                CHANCERY_APPEALS_CHD_DAILY_CAUSE_LIST_JSON_FILE_PATH,
                "venue")),
            Arguments.of(new ListTypeConfig(
                ListType.CIVIL_COURTS_RCJ_DAILY_CAUSE_LIST,
                CIVIL_COURTS_RCJ_DAILY_CAUSE_LIST_JSON_FILE_PATH,
                "venue")),
            Arguments.of(new ListTypeConfig(
                ListType.COMMERCIAL_COURT_KB_DAILY_CAUSE_LIST,
                COMMERCIAL_COURT_KB_DAILY_CAUSE_LIST_JSON_FILE_PATH,
                "venue")),
            Arguments.of(new ListTypeConfig(
                ListType.COMPANIES_WINDING_UP_CHD_DAILY_CAUSE_LIST,
                COMPANIES_WINDING_UP_CHD_DAILY_CAUSE_LIST_JSON_FILE_PATH,
                "venue")),
            Arguments.of(new ListTypeConfig(
                ListType.COMPETITION_LIST_CHD_DAILY_CAUSE_LIST,
                COMPETITION_LIST_CHD_DAILY_CAUSE_LIST_JSON_FILE_PATH,
                "venue")),
            Arguments.of(new ListTypeConfig(
                ListType.COUNTY_COURT_LONDON_CIVIL_DAILY_CAUSE_LIST,
                COUNTY_COURT_LONDON_CIVIL_DAILY_CAUSE_LIST_JSON_FILE_PATH,
                "venue")),
            Arguments.of(new ListTypeConfig(
                ListType.COURT_OF_APPEAL_CIVIL_DAILY_CAUSE_LIST,
                COURT_OF_APPEAL_CIVIL_DAILY_CAUSE_LIST_JSON_FILE_PATH,
                "venue", "hearingList")),
            Arguments.of(new ListTypeConfig(
                ListType.COURT_OF_APPEAL_CIVIL_DAILY_CAUSE_LIST,
                COURT_OF_APPEAL_CIVIL_DAILY_CAUSE_LIST_JSON_FILE_PATH,
                "venue", "futureJudgments")),
            Arguments.of(new ListTypeConfig(
                ListType.COURT_OF_APPEAL_CRIMINAL_DAILY_CAUSE_LIST,
                PARENT_JSON_FILE_PATH
                    + "/court-of-appeal-criminal-daily-cause-list/courtOfAppealCriminalDailyCauseList.json",
                "venue")),
            Arguments.of(new ListTypeConfig(
                ListType.CST_WEEKLY_HEARING_LIST,
                PARENT_JSON_FILE_PATH
                    + "/cst-weekly-hearing-list/cstWeeklyHearingList.json",
                "venue"))
        );
    }

    public static Stream<Arguments> judgeListMandatoryAttributes() {
        return Stream.of(
            Arguments.of(new ListTypeConfig(
                ListType.ADMIRALTY_COURT_KB_DAILY_CAUSE_LIST,
                ADMIRALTY_COURT_KB_DAILY_CAUSE_LIST_JSON_FILE_PATH,
                "judge")),
            Arguments.of(new ListTypeConfig(
                ListType.BIRMINGHAM_ADMINISTRATIVE_COURT_DAILY_CAUSE_LIST,
                ADMINISTRATIVE_COURT_DAILY_CAUSE_LIST_JSON_FILE_PATH,
                "judge")),
            Arguments.of(new ListTypeConfig(
                ListType.BRISTOL_AND_CARDIFF_ADMINISTRATIVE_COURT_DAILY_CAUSE_LIST,
                ADMINISTRATIVE_COURT_DAILY_CAUSE_LIST_JSON_FILE_PATH,
                "judge")),
            Arguments.of(new ListTypeConfig(
                ListType.BUSINESS_LIST_CHD_DAILY_CAUSE_LIST,
                BUSINESS_LIST_CHD_DAILY_CAUSE_LIST_JSON_FILE_PATH,
                "judge")),
            Arguments.of(new ListTypeConfig(
                ListType.CHANCERY_APPEALS_CHD_DAILY_CAUSE_LIST,
                CHANCERY_APPEALS_CHD_DAILY_CAUSE_LIST_JSON_FILE_PATH,
                "judge")),
            Arguments.of(new ListTypeConfig(
                ListType.CIVIL_COURTS_RCJ_DAILY_CAUSE_LIST,
                CIVIL_COURTS_RCJ_DAILY_CAUSE_LIST_JSON_FILE_PATH,
                "judge")),
            Arguments.of(new ListTypeConfig(
                ListType.COMMERCIAL_COURT_KB_DAILY_CAUSE_LIST,
                COMMERCIAL_COURT_KB_DAILY_CAUSE_LIST_JSON_FILE_PATH,
                "judge")),
            Arguments.of(new ListTypeConfig(
                ListType.COMPANIES_WINDING_UP_CHD_DAILY_CAUSE_LIST,
                COMPANIES_WINDING_UP_CHD_DAILY_CAUSE_LIST_JSON_FILE_PATH,
                "judge")),
            Arguments.of(new ListTypeConfig(
                ListType.COMPETITION_LIST_CHD_DAILY_CAUSE_LIST,
                COMPETITION_LIST_CHD_DAILY_CAUSE_LIST_JSON_FILE_PATH,
                "judge")),
            Arguments.of(new ListTypeConfig(
                ListType.COUNTY_COURT_LONDON_CIVIL_DAILY_CAUSE_LIST,
                COUNTY_COURT_LONDON_CIVIL_DAILY_CAUSE_LIST_JSON_FILE_PATH,
                "judge")),
            Arguments.of(new ListTypeConfig(
                ListType.COURT_OF_APPEAL_CIVIL_DAILY_CAUSE_LIST,
                COURT_OF_APPEAL_CIVIL_DAILY_CAUSE_LIST_JSON_FILE_PATH,
                "judge", "hearingList")),
            Arguments.of(new ListTypeConfig(
                ListType.COURT_OF_APPEAL_CIVIL_DAILY_CAUSE_LIST,
                COURT_OF_APPEAL_CIVIL_DAILY_CAUSE_LIST_JSON_FILE_PATH,
                "judge", "futureJudgments"))
        );
    }

    public static Stream<Arguments> timeListMandatoryAttributes() {
        return Stream.of(
            Arguments.of(new ListTypeConfig(
                ListType.ADMIRALTY_COURT_KB_DAILY_CAUSE_LIST,
                ADMIRALTY_COURT_KB_DAILY_CAUSE_LIST_JSON_FILE_PATH,
                "time")),
            Arguments.of(new ListTypeConfig(
                ListType.BIRMINGHAM_ADMINISTRATIVE_COURT_DAILY_CAUSE_LIST,
                ADMIRALTY_COURT_KB_DAILY_CAUSE_LIST_JSON_FILE_PATH,
                "time")),
            Arguments.of(new ListTypeConfig(
                ListType.BRISTOL_AND_CARDIFF_ADMINISTRATIVE_COURT_DAILY_CAUSE_LIST,
                ADMIRALTY_COURT_KB_DAILY_CAUSE_LIST_JSON_FILE_PATH,
                "time")),
            Arguments.of(new ListTypeConfig(
                ListType.BUSINESS_LIST_CHD_DAILY_CAUSE_LIST,
                BUSINESS_LIST_CHD_DAILY_CAUSE_LIST_JSON_FILE_PATH,
                "time")),
            Arguments.of(new ListTypeConfig(
                ListType.CHANCERY_APPEALS_CHD_DAILY_CAUSE_LIST,
                CHANCERY_APPEALS_CHD_DAILY_CAUSE_LIST_JSON_FILE_PATH,
                "time")),
            Arguments.of(new ListTypeConfig(
                ListType.CIVIL_COURTS_RCJ_DAILY_CAUSE_LIST,
                CIVIL_COURTS_RCJ_DAILY_CAUSE_LIST_JSON_FILE_PATH,
                "time")),
            Arguments.of(new ListTypeConfig(
                ListType.COMMERCIAL_COURT_KB_DAILY_CAUSE_LIST,
                COMMERCIAL_COURT_KB_DAILY_CAUSE_LIST_JSON_FILE_PATH,
                "time")),
            Arguments.of(new ListTypeConfig(
                ListType.COMPANIES_WINDING_UP_CHD_DAILY_CAUSE_LIST,
                COMPANIES_WINDING_UP_CHD_DAILY_CAUSE_LIST_JSON_FILE_PATH,
                "time")),
            Arguments.of(new ListTypeConfig(
                ListType.COMPETITION_LIST_CHD_DAILY_CAUSE_LIST,
                COMPETITION_LIST_CHD_DAILY_CAUSE_LIST_JSON_FILE_PATH,
                "time")),
            Arguments.of(new ListTypeConfig(
                ListType.COUNTY_COURT_LONDON_CIVIL_DAILY_CAUSE_LIST,
                COUNTY_COURT_LONDON_CIVIL_DAILY_CAUSE_LIST_JSON_FILE_PATH,
                "time")),
            Arguments.of(new ListTypeConfig(
                ListType.COURT_OF_APPEAL_CIVIL_DAILY_CAUSE_LIST,
                COURT_OF_APPEAL_CIVIL_DAILY_CAUSE_LIST_JSON_FILE_PATH,
                "time", "hearingList")),
            Arguments.of(new ListTypeConfig(
                ListType.COURT_OF_APPEAL_CIVIL_DAILY_CAUSE_LIST,
                COURT_OF_APPEAL_CIVIL_DAILY_CAUSE_LIST_JSON_FILE_PATH,
                "time", "futureJudgments"))
        );
    }

    public static Stream<Arguments> typeListMandatoryAttributes() {
        return Stream.of(
            Arguments.of(new ListTypeConfig(
                ListType.ADMIRALTY_COURT_KB_DAILY_CAUSE_LIST,
                ADMIRALTY_COURT_KB_DAILY_CAUSE_LIST_JSON_FILE_PATH,
                "type")),
            Arguments.of(new ListTypeConfig(
                ListType.BUSINESS_LIST_CHD_DAILY_CAUSE_LIST,
                BUSINESS_LIST_CHD_DAILY_CAUSE_LIST_JSON_FILE_PATH,
                "type")),
            Arguments.of(new ListTypeConfig(
                ListType.CHANCERY_APPEALS_CHD_DAILY_CAUSE_LIST,
                CHANCERY_APPEALS_CHD_DAILY_CAUSE_LIST_JSON_FILE_PATH,
                "type")),
            Arguments.of(new ListTypeConfig(
                ListType.COMMERCIAL_COURT_KB_DAILY_CAUSE_LIST,
                COMMERCIAL_COURT_KB_DAILY_CAUSE_LIST_JSON_FILE_PATH,
                "type")),
            Arguments.of(new ListTypeConfig(
                ListType.COMPANIES_WINDING_UP_CHD_DAILY_CAUSE_LIST,
                COMPANIES_WINDING_UP_CHD_DAILY_CAUSE_LIST_JSON_FILE_PATH,
                "type")),
            Arguments.of(new ListTypeConfig(
                ListType.COMPETITION_LIST_CHD_DAILY_CAUSE_LIST,
                COMPETITION_LIST_CHD_DAILY_CAUSE_LIST_JSON_FILE_PATH,
                "type"))
        );
    }

    public static Stream<Arguments> caseNumberListMandatoryAttributes() {
        return Stream.of(
            Arguments.of(new ListTypeConfig(
                ListType.ADMIRALTY_COURT_KB_DAILY_CAUSE_LIST,
                ADMIRALTY_COURT_KB_DAILY_CAUSE_LIST_JSON_FILE_PATH,
                "caseNumber")),
            Arguments.of(new ListTypeConfig(
                ListType.BIRMINGHAM_ADMINISTRATIVE_COURT_DAILY_CAUSE_LIST,
                ADMINISTRATIVE_COURT_DAILY_CAUSE_LIST_JSON_FILE_PATH,
                "caseNumber")),
            Arguments.of(new ListTypeConfig(
                ListType.BRISTOL_AND_CARDIFF_ADMINISTRATIVE_COURT_DAILY_CAUSE_LIST,
                ADMINISTRATIVE_COURT_DAILY_CAUSE_LIST_JSON_FILE_PATH,
                "caseNumber")),
            Arguments.of(new ListTypeConfig(
                ListType.BUSINESS_LIST_CHD_DAILY_CAUSE_LIST,
                BUSINESS_LIST_CHD_DAILY_CAUSE_LIST_JSON_FILE_PATH,
                "caseNumber")),
            Arguments.of(new ListTypeConfig(
                ListType.CHANCERY_APPEALS_CHD_DAILY_CAUSE_LIST,
                CHANCERY_APPEALS_CHD_DAILY_CAUSE_LIST_JSON_FILE_PATH,
                "caseNumber")),
            Arguments.of(new ListTypeConfig(
                ListType.CIVIL_COURTS_RCJ_DAILY_CAUSE_LIST,
                CIVIL_COURTS_RCJ_DAILY_CAUSE_LIST_JSON_FILE_PATH,
                "caseNumber")),
            Arguments.of(new ListTypeConfig(
                ListType.COMMERCIAL_COURT_KB_DAILY_CAUSE_LIST,
                COMMERCIAL_COURT_KB_DAILY_CAUSE_LIST_JSON_FILE_PATH,
                "caseNumber")),
            Arguments.of(new ListTypeConfig(
                ListType.COMPANIES_WINDING_UP_CHD_DAILY_CAUSE_LIST,
                COMPANIES_WINDING_UP_CHD_DAILY_CAUSE_LIST_JSON_FILE_PATH,
                "caseNumber")),
            Arguments.of(new ListTypeConfig(
                ListType.COMPETITION_LIST_CHD_DAILY_CAUSE_LIST,
                COMPETITION_LIST_CHD_DAILY_CAUSE_LIST_JSON_FILE_PATH,
                "caseNumber")),
            Arguments.of(new ListTypeConfig(
                ListType.COUNTY_COURT_LONDON_CIVIL_DAILY_CAUSE_LIST,
                COUNTY_COURT_LONDON_CIVIL_DAILY_CAUSE_LIST_JSON_FILE_PATH,
                "caseNumber")),
            Arguments.of(new ListTypeConfig(
                ListType.COURT_OF_APPEAL_CIVIL_DAILY_CAUSE_LIST,
                COURT_OF_APPEAL_CIVIL_DAILY_CAUSE_LIST_JSON_FILE_PATH,
                "caseNumber", "hearingList")),
            Arguments.of(new ListTypeConfig(
                ListType.COURT_OF_APPEAL_CIVIL_DAILY_CAUSE_LIST,
                COURT_OF_APPEAL_CIVIL_DAILY_CAUSE_LIST_JSON_FILE_PATH,
                "caseNumber", "futureJudgments"))
        );
    }

    public static Stream<Arguments> caseNameListMandatoryAttributes() {
        return Stream.of(
            Arguments.of(new ListTypeConfig(
                ListType.ADMIRALTY_COURT_KB_DAILY_CAUSE_LIST,
                ADMIRALTY_COURT_KB_DAILY_CAUSE_LIST_JSON_FILE_PATH,
                "caseName")),
            Arguments.of(new ListTypeConfig(
                ListType.BUSINESS_LIST_CHD_DAILY_CAUSE_LIST,
                BUSINESS_LIST_CHD_DAILY_CAUSE_LIST_JSON_FILE_PATH,
                "caseName")),
            Arguments.of(new ListTypeConfig(
                ListType.CHANCERY_APPEALS_CHD_DAILY_CAUSE_LIST,
                CHANCERY_APPEALS_CHD_DAILY_CAUSE_LIST_JSON_FILE_PATH,
                "caseName")),
            Arguments.of(new ListTypeConfig(
                ListType.CIC_WEEKLY_HEARING_LIST,
                CIC_WEEKLY_HEARING_LIST_JSON_FILE_PATH,
                "caseName")),
            Arguments.of(new ListTypeConfig(
                ListType.COMMERCIAL_COURT_KB_DAILY_CAUSE_LIST,
                COMMERCIAL_COURT_KB_DAILY_CAUSE_LIST_JSON_FILE_PATH,
                "caseName")),
            Arguments.of(new ListTypeConfig(
                ListType.COMPANIES_WINDING_UP_CHD_DAILY_CAUSE_LIST,
                COMPANIES_WINDING_UP_CHD_DAILY_CAUSE_LIST_JSON_FILE_PATH,
                "caseName")),
            Arguments.of(new ListTypeConfig(
                ListType.COMPETITION_LIST_CHD_DAILY_CAUSE_LIST,
                COMPETITION_LIST_CHD_DAILY_CAUSE_LIST_JSON_FILE_PATH,
                "caseName"))
        );
    }

    public static Stream<Arguments> additionalInformationListMandatoryAttributes() {
        return Stream.of(
            Arguments.of(new ListTypeConfig(
                ListType.ADMIRALTY_COURT_KB_DAILY_CAUSE_LIST,
                ADMIRALTY_COURT_KB_DAILY_CAUSE_LIST_JSON_FILE_PATH,
                "additionalInformation")),
            Arguments.of(new ListTypeConfig(
                ListType.AST_DAILY_HEARING_LIST,
                AST_DAILY_HEARING_LIST_JSON_FILE_PATH,
                "additionalInformation")),
            Arguments.of(new ListTypeConfig(
                ListType.BIRMINGHAM_ADMINISTRATIVE_COURT_DAILY_CAUSE_LIST,
                ADMINISTRATIVE_COURT_DAILY_CAUSE_LIST_JSON_FILE_PATH,
                "additionalInformation")),
            Arguments.of(new ListTypeConfig(
                ListType.BRISTOL_AND_CARDIFF_ADMINISTRATIVE_COURT_DAILY_CAUSE_LIST,
                ADMINISTRATIVE_COURT_DAILY_CAUSE_LIST_JSON_FILE_PATH,
                "additionalInformation")),
            Arguments.of(new ListTypeConfig(
                ListType.BUSINESS_LIST_CHD_DAILY_CAUSE_LIST,
                BUSINESS_LIST_CHD_DAILY_CAUSE_LIST_JSON_FILE_PATH,
                "additionalInformation")),
            Arguments.of(new ListTypeConfig(
                ListType.CHANCERY_APPEALS_CHD_DAILY_CAUSE_LIST,
                CHANCERY_APPEALS_CHD_DAILY_CAUSE_LIST_JSON_FILE_PATH,
                "additionalInformation")),
            Arguments.of(new ListTypeConfig(
                ListType.CIC_WEEKLY_HEARING_LIST,
                CIC_WEEKLY_HEARING_LIST_JSON_FILE_PATH,
                "additionalInformation")),
            Arguments.of(new ListTypeConfig(
                ListType.CIVIL_COURTS_RCJ_DAILY_CAUSE_LIST,
                CIVIL_COURTS_RCJ_DAILY_CAUSE_LIST_JSON_FILE_PATH,
                "additionalInformation")),
            Arguments.of(new ListTypeConfig(
                ListType.COMMERCIAL_COURT_KB_DAILY_CAUSE_LIST,
                COMMERCIAL_COURT_KB_DAILY_CAUSE_LIST_JSON_FILE_PATH,
                "additionalInformation")),
            Arguments.of(new ListTypeConfig(
                ListType.COMPANIES_WINDING_UP_CHD_DAILY_CAUSE_LIST,
                COMPANIES_WINDING_UP_CHD_DAILY_CAUSE_LIST_JSON_FILE_PATH,
                "additionalInformation")),
            Arguments.of(new ListTypeConfig(
                ListType.COMPETITION_LIST_CHD_DAILY_CAUSE_LIST,
                COMPETITION_LIST_CHD_DAILY_CAUSE_LIST_JSON_FILE_PATH,
                "additionalInformation")),
            Arguments.of(new ListTypeConfig(
                ListType.COUNTY_COURT_LONDON_CIVIL_DAILY_CAUSE_LIST,
                COUNTY_COURT_LONDON_CIVIL_DAILY_CAUSE_LIST_JSON_FILE_PATH,
                "additionalInformation")),
            Arguments.of(new ListTypeConfig(
                ListType.COURT_OF_APPEAL_CIVIL_DAILY_CAUSE_LIST,
                COURT_OF_APPEAL_CIVIL_DAILY_CAUSE_LIST_JSON_FILE_PATH,
                "additionalInformation", "hearingList")),
            Arguments.of(new ListTypeConfig(
                ListType.COURT_OF_APPEAL_CIVIL_DAILY_CAUSE_LIST,
                COURT_OF_APPEAL_CIVIL_DAILY_CAUSE_LIST_JSON_FILE_PATH,
                "additionalInformation", "futureJudgments"))
        );
    }

    public static Stream<Arguments> appellantListMandatoryAttributes() {
        return Stream.of(
            Arguments.of(new ListTypeConfig(
                ListType.AST_DAILY_HEARING_LIST,
                AST_DAILY_HEARING_LIST_JSON_FILE_PATH,
                "appellant"))
        );
    }

    public static Stream<Arguments> appealReferenceNumberListMandatoryAttributes() {
        return Stream.of(
            Arguments.of(new ListTypeConfig(
                ListType.AST_DAILY_HEARING_LIST,
                AST_DAILY_HEARING_LIST_JSON_FILE_PATH,
                "appealReferenceNumber"))
        );
    }

    public static Stream<Arguments> caseTypeListMandatoryAttributes() {
        return Stream.of(
            Arguments.of(new ListTypeConfig(
                ListType.AST_DAILY_HEARING_LIST,
                AST_DAILY_HEARING_LIST_JSON_FILE_PATH,
                "caseType"))
        );
    }

    public static Stream<Arguments> hearingTypeListMandatoryAttributes() {
        return Stream.of(
            Arguments.of(new ListTypeConfig(
                ListType.AST_DAILY_HEARING_LIST,
                AST_DAILY_HEARING_LIST_JSON_FILE_PATH,
                "hearingType")),
            Arguments.of(new ListTypeConfig(
                ListType.BIRMINGHAM_ADMINISTRATIVE_COURT_DAILY_CAUSE_LIST,
                ADMINISTRATIVE_COURT_DAILY_CAUSE_LIST_JSON_FILE_PATH,
                "hearingType")),
            Arguments.of(new ListTypeConfig(
                ListType.BRISTOL_AND_CARDIFF_ADMINISTRATIVE_COURT_DAILY_CAUSE_LIST,
                ADMINISTRATIVE_COURT_DAILY_CAUSE_LIST_JSON_FILE_PATH,
                "hearingType")),
            Arguments.of(new ListTypeConfig(
                ListType.CIVIL_COURTS_RCJ_DAILY_CAUSE_LIST,
                CIVIL_COURTS_RCJ_DAILY_CAUSE_LIST_JSON_FILE_PATH,
                "hearingType")),
            Arguments.of(new ListTypeConfig(
                ListType.COUNTY_COURT_LONDON_CIVIL_DAILY_CAUSE_LIST,
                COUNTY_COURT_LONDON_CIVIL_DAILY_CAUSE_LIST_JSON_FILE_PATH,
                "hearingType")),
            Arguments.of(new ListTypeConfig(
                ListType.COURT_OF_APPEAL_CIVIL_DAILY_CAUSE_LIST,
                COURT_OF_APPEAL_CIVIL_DAILY_CAUSE_LIST_JSON_FILE_PATH,
                "hearingType", "hearingList")),
            Arguments.of(new ListTypeConfig(
                ListType.COURT_OF_APPEAL_CIVIL_DAILY_CAUSE_LIST,
                COURT_OF_APPEAL_CIVIL_DAILY_CAUSE_LIST_JSON_FILE_PATH,
                "hearingType", "futureJudgments"))
        );
    }

    public static Stream<Arguments> hearingTimeListMandatoryAttributes() {
        return Stream.of(
            Arguments.of(new ListTypeConfig(
                ListType.AST_DAILY_HEARING_LIST,
                AST_DAILY_HEARING_LIST_JSON_FILE_PATH,
                "hearingTime")),
            Arguments.of(new ListTypeConfig(
                ListType.CIC_WEEKLY_HEARING_LIST,
                CIC_WEEKLY_HEARING_LIST_JSON_FILE_PATH,
                "hearingTime"))
        );
    }

    public static Stream<Arguments> caseDetailsListMandatoryAttributes() {
        return Stream.of(
            Arguments.of(new ListTypeConfig(
                ListType.BIRMINGHAM_ADMINISTRATIVE_COURT_DAILY_CAUSE_LIST,
                ADMINISTRATIVE_COURT_DAILY_CAUSE_LIST_JSON_FILE_PATH,
                "caseDetails")),
            Arguments.of(new ListTypeConfig(
                ListType.BRISTOL_AND_CARDIFF_ADMINISTRATIVE_COURT_DAILY_CAUSE_LIST,
                ADMINISTRATIVE_COURT_DAILY_CAUSE_LIST_JSON_FILE_PATH,
                "caseDetails")),
            Arguments.of(new ListTypeConfig(
                ListType.CIVIL_COURTS_RCJ_DAILY_CAUSE_LIST,
                CIVIL_COURTS_RCJ_DAILY_CAUSE_LIST_JSON_FILE_PATH,
                "caseDetails")),
            Arguments.of(new ListTypeConfig(
                ListType.COUNTY_COURT_LONDON_CIVIL_DAILY_CAUSE_LIST,
                COUNTY_COURT_LONDON_CIVIL_DAILY_CAUSE_LIST_JSON_FILE_PATH,
                "caseDetails")),
            Arguments.of(new ListTypeConfig(
                ListType.COURT_OF_APPEAL_CIVIL_DAILY_CAUSE_LIST,
                COURT_OF_APPEAL_CIVIL_DAILY_CAUSE_LIST_JSON_FILE_PATH,
                "caseDetails", "hearingList")),
            Arguments.of(new ListTypeConfig(
                ListType.COURT_OF_APPEAL_CIVIL_DAILY_CAUSE_LIST,
                COURT_OF_APPEAL_CIVIL_DAILY_CAUSE_LIST_JSON_FILE_PATH,
                "caseDetails", "futureJudgments"))
        );
    }

    public static Stream<Arguments> dateListMandatoryAttributes() {
        return Stream.of(
            Arguments.of(new ListTypeConfig(
                ListType.CIC_WEEKLY_HEARING_LIST,
                CIC_WEEKLY_HEARING_LIST_JSON_FILE_PATH,
                "date"))
        );
    }

    public static Stream<Arguments> caseReferenceNumberListMandatoryAttributes() {
        return Stream.of(
            Arguments.of(new ListTypeConfig(
                ListType.CIC_WEEKLY_HEARING_LIST,
                CIC_WEEKLY_HEARING_LIST_JSON_FILE_PATH,
                "caseReferenceNumber"))
        );
    }

    public static Stream<Arguments> venuePlatformListMandatoryAttributes() {
        return Stream.of(
            Arguments.of(new ListTypeConfig(
                ListType.CIC_WEEKLY_HEARING_LIST,
                CIC_WEEKLY_HEARING_LIST_JSON_FILE_PATH,
                "venue/platform"))
        );
    }

    public static Stream<Arguments> membersListMandatoryAttributes() {
        return Stream.of(
            Arguments.of(new ListTypeConfig(
                ListType.CIC_WEEKLY_HEARING_LIST,
                CIC_WEEKLY_HEARING_LIST_JSON_FILE_PATH,
                "members"))
        );
    }

    public static Stream<Arguments> judgesListMandatoryAttributes() {
        return Stream.of(
            Arguments.of(new ListTypeConfig(
                ListType.CIC_WEEKLY_HEARING_LIST,
                CIC_WEEKLY_HEARING_LIST_JSON_FILE_PATH,
                "judges"))
        );
    }
}
