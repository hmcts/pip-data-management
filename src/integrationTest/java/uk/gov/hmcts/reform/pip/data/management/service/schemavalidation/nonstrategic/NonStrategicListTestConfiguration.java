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
                PARENT_JSON_FILE_PATH
                    + "/admiralty_court_kb_daily_cause_list/admiraltyCourtKbDailyCauseList.json",
                "venue")),
            Arguments.of(new ListTypeConfig(
                ListType.BIRMINGHAM_ADMINISTRATIVE_COURT_DAILY_CAUSE_LIST,
                PARENT_JSON_FILE_PATH
                    + "/administrative-court-daily-cause-list/administrativeCourtDailyCauseList.json",
                "venue")),
            Arguments.of(new ListTypeConfig(
                ListType.BRISTOL_AND_CARDIFF_ADMINISTRATIVE_COURT_DAILY_CAUSE_LIST,
                PARENT_JSON_FILE_PATH
                    + "/administrative-court-daily-cause-list/administrativeCourtDailyCauseList.json",
                "venue")),
            Arguments.of(new ListTypeConfig(
                ListType.BUSINESS_LIST_CHD_DAILY_CAUSE_LIST,
                PARENT_JSON_FILE_PATH
                    + "/administrative-court-daily-cause-list/administrativeCourtDailyCauseList.json",
                "venue")),
            Arguments.of(new ListTypeConfig(
                ListType.CHANCERY_APPEALS_CHD_DAILY_CAUSE_LIST,
                PARENT_JSON_FILE_PATH
                    + "/chancery_appeals_chd_daily_cause_list/chanceryAppealsChdDailyCauseList.json",
                "venue")),
            Arguments.of(new ListTypeConfig(
                ListType.CIVIL_COURTS_RCJ_DAILY_CAUSE_LIST,
                PARENT_JSON_FILE_PATH
                    + "/civil-courts-rcj-daily-cause-list/civilCourtsRcjDailyCauseList.json",
                "venue")),
            Arguments.of(new ListTypeConfig(
                ListType.COMMERCIAL_COURT_KB_DAILY_CAUSE_LIST,
                PARENT_JSON_FILE_PATH
                    + "/commercial_court_kb_daily_cause_list/commercialCourtKbDailyCauseList.json",
                "venue")),
            Arguments.of(new ListTypeConfig(
                ListType.COMPANIES_WINDING_UP_CHD_DAILY_CAUSE_LIST,
                PARENT_JSON_FILE_PATH
                    + "/companies_winding_up_chd_daily_cause_list/companiesWindingUpChdDailyCauseList.json",
                "venue")),
            Arguments.of(new ListTypeConfig(
                ListType.COMPETITION_LIST_CHD_DAILY_CAUSE_LIST,
                PARENT_JSON_FILE_PATH
                    + "/competition_list_chd_daily_cause_list/competitionListChdDailyCauseList.json",
                "venue"))
        );
    }

    public static Stream<Arguments> judgeListMandatoryAttributes() {
        return Stream.of(
            Arguments.of(new ListTypeConfig(
                ListType.BIRMINGHAM_ADMINISTRATIVE_COURT_DAILY_CAUSE_LIST,
                PARENT_JSON_FILE_PATH
                    + "/administrative-court-daily-cause-list/administrativeCourtDailyCauseList.json",
                "judge")),
            Arguments.of(new ListTypeConfig(
                ListType.BRISTOL_AND_CARDIFF_ADMINISTRATIVE_COURT_DAILY_CAUSE_LIST,
                PARENT_JSON_FILE_PATH
                    + "/administrative-court-daily-cause-list/administrativeCourtDailyCauseList.json",
                "judge")),
            Arguments.of(new ListTypeConfig(
                ListType.BUSINESS_LIST_CHD_DAILY_CAUSE_LIST,
                PARENT_JSON_FILE_PATH
                    + "/administrative-court-daily-cause-list/administrativeCourtDailyCauseList.json",
                "judge"))
        );
    }
}
