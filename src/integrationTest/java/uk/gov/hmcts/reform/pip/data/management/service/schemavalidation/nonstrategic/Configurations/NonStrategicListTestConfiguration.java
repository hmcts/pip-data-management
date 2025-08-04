package uk.gov.hmcts.reform.pip.data.management.service.schemavalidation.nonstrategic.Configurations;

import org.junit.jupiter.params.provider.Arguments;
import uk.gov.hmcts.reform.pip.model.publication.ListType;

import java.util.stream.Stream;

import static uk.gov.hmcts.reform.pip.data.management.service.schemavalidation.nonstrategic.NonStrategicListTestConstants.APPEAL_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.pip.data.management.service.schemavalidation.nonstrategic.NonStrategicListTestConstants.APPELLANT;
import static uk.gov.hmcts.reform.pip.data.management.service.schemavalidation.nonstrategic.NonStrategicListTestConstants.AST_DAILY_HEARING_LIST_JSON_FILE_PATH;
import static uk.gov.hmcts.reform.pip.data.management.service.schemavalidation.nonstrategic.NonStrategicListTestConstants.CASE_TITLE;
import static uk.gov.hmcts.reform.pip.data.management.service.schemavalidation.nonstrategic.NonStrategicListTestConstants.CASE_TYPE;
import static uk.gov.hmcts.reform.pip.data.management.service.schemavalidation.nonstrategic.NonStrategicListTestConstants.CIC_WEEKLY_HEARING_LIST_JSON_FILE_PATH;
import static uk.gov.hmcts.reform.pip.data.management.service.schemavalidation.nonstrategic.NonStrategicListTestConstants.COURT_ROOM;
import static uk.gov.hmcts.reform.pip.data.management.service.schemavalidation.nonstrategic.NonStrategicListTestConstants.CST_WEEKLY_HEARING_LIST_JSON_FILE_PATH;
import static uk.gov.hmcts.reform.pip.data.management.service.schemavalidation.nonstrategic.NonStrategicListTestConstants.DATE;
import static uk.gov.hmcts.reform.pip.data.management.service.schemavalidation.nonstrategic.NonStrategicListTestConstants.EMAIL;
import static uk.gov.hmcts.reform.pip.data.management.service.schemavalidation.nonstrategic.NonStrategicListTestConstants.FTA_RESPONDENT;
import static uk.gov.hmcts.reform.pip.data.management.service.schemavalidation.nonstrategic.NonStrategicListTestConstants.FTT_LR_WEEKLY_HEARING_LIST_JSON_FILE_PATH;
import static uk.gov.hmcts.reform.pip.data.management.service.schemavalidation.nonstrategic.NonStrategicListTestConstants.FTT_TAX_WEEKLY_HEARING_LIST_JSON_FILE_PATH;
import static uk.gov.hmcts.reform.pip.data.management.service.schemavalidation.nonstrategic.NonStrategicListTestConstants.GRC_WEEKLY_HEARING_LIST_JSON_FILE_PATH;
import static uk.gov.hmcts.reform.pip.data.management.service.schemavalidation.nonstrategic.NonStrategicListTestConstants.HEARING_LENGTH;
import static uk.gov.hmcts.reform.pip.data.management.service.schemavalidation.nonstrategic.NonStrategicListTestConstants.HEARING_LIST_NODE;
import static uk.gov.hmcts.reform.pip.data.management.service.schemavalidation.nonstrategic.NonStrategicListTestConstants.HEARING_METHOD;
import static uk.gov.hmcts.reform.pip.data.management.service.schemavalidation.nonstrategic.NonStrategicListTestConstants.INTERIM_APPLICATION_CHANCERY_LIST_CHD_DAILY_CAUSE_LIST_JSON_FILE_PATH;
import static uk.gov.hmcts.reform.pip.data.management.service.schemavalidation.nonstrategic.NonStrategicListTestConstants.JUDGES;
import static uk.gov.hmcts.reform.pip.data.management.service.schemavalidation.nonstrategic.NonStrategicListTestConstants.LOCATION;
import static uk.gov.hmcts.reform.pip.data.management.service.schemavalidation.nonstrategic.NonStrategicListTestConstants.MEMBERS;
import static uk.gov.hmcts.reform.pip.data.management.service.schemavalidation.nonstrategic.NonStrategicListTestConstants.MODE_OF_HEARING;
import static uk.gov.hmcts.reform.pip.data.management.service.schemavalidation.nonstrategic.NonStrategicListTestConstants.NAME_TO_BE_DISPLAYED;
import static uk.gov.hmcts.reform.pip.data.management.service.schemavalidation.nonstrategic.NonStrategicListTestConstants.OPEN_JUSTICE_DETAILS_NODE;
import static uk.gov.hmcts.reform.pip.data.management.service.schemavalidation.nonstrategic.NonStrategicListTestConstants.PANEL;
import static uk.gov.hmcts.reform.pip.data.management.service.schemavalidation.nonstrategic.NonStrategicListTestConstants.PHT_WEEKLY_HEARING_LIST_JSON_FILE_PATH;
import static uk.gov.hmcts.reform.pip.data.management.service.schemavalidation.nonstrategic.NonStrategicListTestConstants.REPRESENTATIVE;
import static uk.gov.hmcts.reform.pip.data.management.service.schemavalidation.nonstrategic.NonStrategicListTestConstants.RESPONDENT;
import static uk.gov.hmcts.reform.pip.data.management.service.schemavalidation.nonstrategic.NonStrategicListTestConstants.RPT_WEEKLY_HEARING_LIST_JSON_FILE_PATH;
import static uk.gov.hmcts.reform.pip.data.management.service.schemavalidation.nonstrategic.NonStrategicListTestConstants.SEND_DAILY_HEARING_LIST_JSON_FILE_PATH;
import static uk.gov.hmcts.reform.pip.data.management.service.schemavalidation.nonstrategic.NonStrategicListTestConstants.SIAC_WEEKLY_HEARING_LIST_JSON_FILE_PATH;
import static uk.gov.hmcts.reform.pip.data.management.service.schemavalidation.nonstrategic.NonStrategicListTestConstants.SSCS_DAILY_HEARING_LIST_JSON_FILE_PATH;
import static uk.gov.hmcts.reform.pip.data.management.service.schemavalidation.nonstrategic.NonStrategicListTestConstants.TIME_ESTIMATE;
import static uk.gov.hmcts.reform.pip.data.management.service.schemavalidation.nonstrategic.NonStrategicListTestConstants.UT_ADMINISTRATIVE_APPEALS_CHAMBER_DAILY_HEARING_LIST_JSON_FILE_PATH;
import static uk.gov.hmcts.reform.pip.data.management.service.schemavalidation.nonstrategic.NonStrategicListTestConstants.UT_IAC_JUDICIAL_REVIEWS_DAILY_HEARING_LIST_JSON_FILE_PATH;
import static uk.gov.hmcts.reform.pip.data.management.service.schemavalidation.nonstrategic.NonStrategicListTestConstants.UT_IAC_STATUTORY_APPEALS_DAILY_HEARING_LIST_JSON_FILE_PATH;
import static uk.gov.hmcts.reform.pip.data.management.service.schemavalidation.nonstrategic.NonStrategicListTestConstants.UT_LANDS_CHAMBER_DAILY_HEARING_LIST_JSON_FILE_PATH;
import static uk.gov.hmcts.reform.pip.data.management.service.schemavalidation.nonstrategic.NonStrategicListTestConstants.UT_TAX_CHAMBER_DAILY_HEARING_LIST_JSON_FILE_PATH;
import static uk.gov.hmcts.reform.pip.data.management.service.schemavalidation.nonstrategic.NonStrategicListTestConstants.VENUE_PLATFORM;
import static uk.gov.hmcts.reform.pip.data.management.service.schemavalidation.nonstrategic.NonStrategicListTestConstants.WPAFCC_WEEKLY_HEARING_LIST_JSON_FILE_PATH;

public class NonStrategicListTestConfiguration {

    public static Stream<Arguments> appellantMandatoryAttribute() {
        return Stream.of(
            Arguments.of(new ListTypeTest(
                ListType.AST_DAILY_HEARING_LIST,
                AST_DAILY_HEARING_LIST_JSON_FILE_PATH,
                APPELLANT)),
            Arguments.of(new ListTypeTest(
                ListType.SSCS_LONDON_DAILY_HEARING_LIST,
                SSCS_DAILY_HEARING_LIST_JSON_FILE_PATH,
                APPELLANT)),
            Arguments.of(new ListTypeTest(
                ListType.SSCS_MIDLANDS_DAILY_HEARING_LIST,
                SSCS_DAILY_HEARING_LIST_JSON_FILE_PATH,
                APPELLANT)),
            Arguments.of(new ListTypeTest(
                ListType.SSCS_NORTH_EAST_DAILY_HEARING_LIST,
                SSCS_DAILY_HEARING_LIST_JSON_FILE_PATH,
                APPELLANT)),
            Arguments.of(new ListTypeTest(
                ListType.SSCS_NORTH_WEST_DAILY_HEARING_LIST,
                SSCS_DAILY_HEARING_LIST_JSON_FILE_PATH,
                APPELLANT)),
            Arguments.of(new ListTypeTest(
                ListType.SSCS_SCOTLAND_DAILY_HEARING_LIST,
                SSCS_DAILY_HEARING_LIST_JSON_FILE_PATH,
                APPELLANT)),
            Arguments.of(new ListTypeTest(
                ListType.SSCS_SOUTH_EAST_DAILY_HEARING_LIST,
                SSCS_DAILY_HEARING_LIST_JSON_FILE_PATH,
                APPELLANT)),
            Arguments.of(new ListTypeTest(
                ListType.SSCS_WALES_AND_SOUTH_WEST_DAILY_HEARING_LIST,
                SSCS_DAILY_HEARING_LIST_JSON_FILE_PATH,
                APPELLANT)),
            Arguments.of(new ListTypeTest(
                ListType.UT_AAC_DAILY_HEARING_LIST,
                UT_ADMINISTRATIVE_APPEALS_CHAMBER_DAILY_HEARING_LIST_JSON_FILE_PATH,
                APPELLANT)),
            Arguments.of(new ListTypeTest(
                ListType.UT_IAC_STATUTORY_APPEALS_DAILY_HEARING_LIST,
                UT_IAC_STATUTORY_APPEALS_DAILY_HEARING_LIST_JSON_FILE_PATH,
                APPELLANT))
        );
    }

    public static Stream<Arguments> appealReferenceNumberMandatoryAttribute() {
        return Stream.of(
            Arguments.of(new ListTypeTest(
                ListType.AST_DAILY_HEARING_LIST,
                AST_DAILY_HEARING_LIST_JSON_FILE_PATH,
                APPEAL_REFERENCE_NUMBER)),
            Arguments.of(new ListTypeTest(
                ListType.SSCS_LONDON_DAILY_HEARING_LIST,
                SSCS_DAILY_HEARING_LIST_JSON_FILE_PATH,
                APPEAL_REFERENCE_NUMBER)),
            Arguments.of(new ListTypeTest(
                ListType.SSCS_MIDLANDS_DAILY_HEARING_LIST,
                SSCS_DAILY_HEARING_LIST_JSON_FILE_PATH,
                APPEAL_REFERENCE_NUMBER)),
            Arguments.of(new ListTypeTest(
                ListType.SSCS_NORTH_EAST_DAILY_HEARING_LIST,
                SSCS_DAILY_HEARING_LIST_JSON_FILE_PATH,
                APPEAL_REFERENCE_NUMBER)),
            Arguments.of(new ListTypeTest(
                ListType.SSCS_NORTH_WEST_DAILY_HEARING_LIST,
                SSCS_DAILY_HEARING_LIST_JSON_FILE_PATH,
                APPEAL_REFERENCE_NUMBER)),
            Arguments.of(new ListTypeTest(
                ListType.SSCS_SCOTLAND_DAILY_HEARING_LIST,
                SSCS_DAILY_HEARING_LIST_JSON_FILE_PATH,
                APPEAL_REFERENCE_NUMBER)),
            Arguments.of(new ListTypeTest(
                ListType.SSCS_SOUTH_EAST_DAILY_HEARING_LIST,
                SSCS_DAILY_HEARING_LIST_JSON_FILE_PATH,
                APPEAL_REFERENCE_NUMBER)),
            Arguments.of(new ListTypeTest(
                ListType.SSCS_WALES_AND_SOUTH_WEST_DAILY_HEARING_LIST,
                SSCS_DAILY_HEARING_LIST_JSON_FILE_PATH,
                APPEAL_REFERENCE_NUMBER)),
            Arguments.of(new ListTypeTest(
                ListType.UT_IAC_STATUTORY_APPEALS_DAILY_HEARING_LIST,
                UT_IAC_STATUTORY_APPEALS_DAILY_HEARING_LIST_JSON_FILE_PATH,
                APPEAL_REFERENCE_NUMBER))
        );
    }

    public static Stream<Arguments> caseTypeMandatoryAttribute() {
        return Stream.of(
            Arguments.of(new ListTypeTest(
                ListType.AST_DAILY_HEARING_LIST,
                AST_DAILY_HEARING_LIST_JSON_FILE_PATH,
                CASE_TYPE)),
            Arguments.of(new ListTypeTest(
                ListType.RPT_EASTERN_WEEKLY_HEARING_LIST,
                RPT_WEEKLY_HEARING_LIST_JSON_FILE_PATH,
                CASE_TYPE)),
            Arguments.of(new ListTypeTest(
                ListType.RPT_LONDON_WEEKLY_HEARING_LIST,
                RPT_WEEKLY_HEARING_LIST_JSON_FILE_PATH,
                CASE_TYPE)),
            Arguments.of(new ListTypeTest(
                ListType.RPT_MIDLANDS_WEEKLY_HEARING_LIST,
                RPT_WEEKLY_HEARING_LIST_JSON_FILE_PATH,
                CASE_TYPE)),
            Arguments.of(new ListTypeTest(
                ListType.RPT_NORTHERN_WEEKLY_HEARING_LIST,
                RPT_WEEKLY_HEARING_LIST_JSON_FILE_PATH,
                CASE_TYPE)),
            Arguments.of(new ListTypeTest(
                ListType.RPT_SOUTHERN_WEEKLY_HEARING_LIST,
                RPT_WEEKLY_HEARING_LIST_JSON_FILE_PATH,
                CASE_TYPE))
        );
    }

    public static Stream<Arguments> dateMandatoryAttribute() {
        return Stream.of(
            Arguments.of(new ListTypeTest(
                ListType.CIC_WEEKLY_HEARING_LIST,
                CIC_WEEKLY_HEARING_LIST_JSON_FILE_PATH,
                DATE)),
            Arguments.of(new ListTypeTest(
                ListType.CST_WEEKLY_HEARING_LIST,
                CST_WEEKLY_HEARING_LIST_JSON_FILE_PATH,
                DATE)),
            Arguments.of(new ListTypeTest(
                ListType.FTT_LR_WEEKLY_HEARING_LIST,
                FTT_LR_WEEKLY_HEARING_LIST_JSON_FILE_PATH,
                DATE)),
            Arguments.of(new ListTypeTest(
                ListType.FTT_TAX_WEEKLY_HEARING_LIST,
                FTT_TAX_WEEKLY_HEARING_LIST_JSON_FILE_PATH,
                DATE)),
            Arguments.of(new ListTypeTest(
                ListType.GRC_WEEKLY_HEARING_LIST,
                GRC_WEEKLY_HEARING_LIST_JSON_FILE_PATH,
                DATE)),
            Arguments.of(new ListTypeTest(
                ListType.PAAC_WEEKLY_HEARING_LIST,
                SIAC_WEEKLY_HEARING_LIST_JSON_FILE_PATH,
                DATE)),
            Arguments.of(new ListTypeTest(
                ListType.SIAC_WEEKLY_HEARING_LIST,
                SIAC_WEEKLY_HEARING_LIST_JSON_FILE_PATH,
                DATE)),
            Arguments.of(new ListTypeTest(
                ListType.POAC_WEEKLY_HEARING_LIST,
                SIAC_WEEKLY_HEARING_LIST_JSON_FILE_PATH,
                DATE)),
            Arguments.of(new ListTypeTest(
                ListType.PHT_WEEKLY_HEARING_LIST,
                PHT_WEEKLY_HEARING_LIST_JSON_FILE_PATH,
                DATE)),
            Arguments.of(new ListTypeTest(
                ListType.RPT_EASTERN_WEEKLY_HEARING_LIST,
                RPT_WEEKLY_HEARING_LIST_JSON_FILE_PATH,
                DATE)),
            Arguments.of(new ListTypeTest(
                ListType.RPT_LONDON_WEEKLY_HEARING_LIST,
                RPT_WEEKLY_HEARING_LIST_JSON_FILE_PATH,
                DATE)),
            Arguments.of(new ListTypeTest(
                ListType.RPT_MIDLANDS_WEEKLY_HEARING_LIST,
                RPT_WEEKLY_HEARING_LIST_JSON_FILE_PATH,
                DATE)),
            Arguments.of(new ListTypeTest(
                ListType.RPT_NORTHERN_WEEKLY_HEARING_LIST,
                RPT_WEEKLY_HEARING_LIST_JSON_FILE_PATH,
                DATE)),
            Arguments.of(new ListTypeTest(
                ListType.RPT_SOUTHERN_WEEKLY_HEARING_LIST,
                RPT_WEEKLY_HEARING_LIST_JSON_FILE_PATH,
                DATE)),
            Arguments.of(new ListTypeTest(
                ListType.WPAFCC_WEEKLY_HEARING_LIST,
                WPAFCC_WEEKLY_HEARING_LIST_JSON_FILE_PATH,
                DATE))
        );
    }

    public static Stream<Arguments> venuePlatformMandatoryAttribute() {
        return Stream.of(
            Arguments.of(new ListTypeTest(
                ListType.CIC_WEEKLY_HEARING_LIST,
                CIC_WEEKLY_HEARING_LIST_JSON_FILE_PATH,
                VENUE_PLATFORM)),
            Arguments.of(new ListTypeTest(
                ListType.FTT_LR_WEEKLY_HEARING_LIST,
                FTT_LR_WEEKLY_HEARING_LIST_JSON_FILE_PATH,
                VENUE_PLATFORM)),
            Arguments.of(new ListTypeTest(
                ListType.FTT_TAX_WEEKLY_HEARING_LIST,
                FTT_TAX_WEEKLY_HEARING_LIST_JSON_FILE_PATH,
                VENUE_PLATFORM))
        );
    }

    public static Stream<Arguments> membersMandatoryAttribute() {
        return Stream.of(
            Arguments.of(new ListTypeTest(
                ListType.CIC_WEEKLY_HEARING_LIST,
                CIC_WEEKLY_HEARING_LIST_JSON_FILE_PATH,
                MEMBERS)),
            Arguments.of(new ListTypeTest(
                ListType.FTT_TAX_WEEKLY_HEARING_LIST,
                FTT_TAX_WEEKLY_HEARING_LIST_JSON_FILE_PATH,
                MEMBERS)),
            Arguments.of(new ListTypeTest(
                ListType.GRC_WEEKLY_HEARING_LIST,
                GRC_WEEKLY_HEARING_LIST_JSON_FILE_PATH,
                MEMBERS)),
            Arguments.of(new ListTypeTest(
                ListType.RPT_EASTERN_WEEKLY_HEARING_LIST,
                RPT_WEEKLY_HEARING_LIST_JSON_FILE_PATH,
                MEMBERS)),
            Arguments.of(new ListTypeTest(
                ListType.RPT_LONDON_WEEKLY_HEARING_LIST,
                RPT_WEEKLY_HEARING_LIST_JSON_FILE_PATH,
                MEMBERS)),
            Arguments.of(new ListTypeTest(
                ListType.RPT_MIDLANDS_WEEKLY_HEARING_LIST,
                RPT_WEEKLY_HEARING_LIST_JSON_FILE_PATH,
                MEMBERS)),
            Arguments.of(new ListTypeTest(
                ListType.RPT_NORTHERN_WEEKLY_HEARING_LIST,
                RPT_WEEKLY_HEARING_LIST_JSON_FILE_PATH,
                MEMBERS)),
            Arguments.of(new ListTypeTest(
                ListType.RPT_SOUTHERN_WEEKLY_HEARING_LIST,
                RPT_WEEKLY_HEARING_LIST_JSON_FILE_PATH,
                MEMBERS)),
            Arguments.of(new ListTypeTest(
                ListType.UT_AAC_DAILY_HEARING_LIST,
                UT_ADMINISTRATIVE_APPEALS_CHAMBER_DAILY_HEARING_LIST_JSON_FILE_PATH,
                MEMBERS)),
            Arguments.of(new ListTypeTest(
                ListType.UT_LC_DAILY_HEARING_LIST,
                UT_LANDS_CHAMBER_DAILY_HEARING_LIST_JSON_FILE_PATH,
                MEMBERS)),
            Arguments.of(new ListTypeTest(
                ListType.UT_T_AND_CC_DAILY_HEARING_LIST,
                UT_TAX_CHAMBER_DAILY_HEARING_LIST_JSON_FILE_PATH,
                MEMBERS))
        );
    }

    public static Stream<Arguments> judgesMandatoryAttribute() {
        return Stream.of(
            Arguments.of(new ListTypeTest(
                ListType.CIC_WEEKLY_HEARING_LIST,
                CIC_WEEKLY_HEARING_LIST_JSON_FILE_PATH,
                JUDGES)),
            Arguments.of(new ListTypeTest(
                ListType.FTT_TAX_WEEKLY_HEARING_LIST,
                FTT_TAX_WEEKLY_HEARING_LIST_JSON_FILE_PATH,
                JUDGES)),
            Arguments.of(new ListTypeTest(
                ListType.GRC_WEEKLY_HEARING_LIST,
                GRC_WEEKLY_HEARING_LIST_JSON_FILE_PATH,
                JUDGES)),
            Arguments.of(new ListTypeTest(
                ListType.RPT_EASTERN_WEEKLY_HEARING_LIST,
                RPT_WEEKLY_HEARING_LIST_JSON_FILE_PATH,
                JUDGES)),
            Arguments.of(new ListTypeTest(
                ListType.RPT_LONDON_WEEKLY_HEARING_LIST,
                RPT_WEEKLY_HEARING_LIST_JSON_FILE_PATH,
                JUDGES)),
            Arguments.of(new ListTypeTest(
                ListType.RPT_MIDLANDS_WEEKLY_HEARING_LIST,
                RPT_WEEKLY_HEARING_LIST_JSON_FILE_PATH,
                JUDGES)),
            Arguments.of(new ListTypeTest(
                ListType.RPT_NORTHERN_WEEKLY_HEARING_LIST,
                RPT_WEEKLY_HEARING_LIST_JSON_FILE_PATH,
                JUDGES)),
            Arguments.of(new ListTypeTest(
                ListType.RPT_SOUTHERN_WEEKLY_HEARING_LIST,
                RPT_WEEKLY_HEARING_LIST_JSON_FILE_PATH,
                JUDGES)),
            Arguments.of(new ListTypeTest(
                ListType.UT_AAC_DAILY_HEARING_LIST,
                UT_ADMINISTRATIVE_APPEALS_CHAMBER_DAILY_HEARING_LIST_JSON_FILE_PATH,
                JUDGES)),
            Arguments.of(new ListTypeTest(
                ListType.UT_IAC_JR_BIRMINGHAM_DAILY_HEARING_LIST,
                UT_IAC_JUDICIAL_REVIEWS_DAILY_HEARING_LIST_JSON_FILE_PATH,
                JUDGES)),
            Arguments.of(new ListTypeTest(
                ListType.UT_IAC_JR_CARDIFF_DAILY_HEARING_LIST,
                UT_IAC_JUDICIAL_REVIEWS_DAILY_HEARING_LIST_JSON_FILE_PATH,
                JUDGES)),
            Arguments.of(new ListTypeTest(
                ListType.UT_IAC_JR_LEEDS_DAILY_HEARING_LIST,
                UT_IAC_JUDICIAL_REVIEWS_DAILY_HEARING_LIST_JSON_FILE_PATH,
                JUDGES)),
            Arguments.of(new ListTypeTest(
                ListType.UT_IAC_JR_LONDON_DAILY_HEARING_LIST,
                UT_IAC_JUDICIAL_REVIEWS_DAILY_HEARING_LIST_JSON_FILE_PATH,
                JUDGES)),
            Arguments.of(new ListTypeTest(
                ListType.UT_IAC_JR_MANCHESTER_DAILY_HEARING_LIST,
                UT_IAC_JUDICIAL_REVIEWS_DAILY_HEARING_LIST_JSON_FILE_PATH,
                JUDGES)),
            Arguments.of(new ListTypeTest(
                ListType.UT_IAC_STATUTORY_APPEALS_DAILY_HEARING_LIST,
                UT_IAC_STATUTORY_APPEALS_DAILY_HEARING_LIST_JSON_FILE_PATH,
                JUDGES)),
            Arguments.of(new ListTypeTest(
                ListType.UT_LC_DAILY_HEARING_LIST,
                UT_LANDS_CHAMBER_DAILY_HEARING_LIST_JSON_FILE_PATH,
                JUDGES)),
            Arguments.of(new ListTypeTest(
                ListType.UT_T_AND_CC_DAILY_HEARING_LIST,
                UT_TAX_CHAMBER_DAILY_HEARING_LIST_JSON_FILE_PATH,
                JUDGES))
        );
    }

    public static Stream<Arguments> hearingLengthMandatoryAttribute() {
        return Stream.of(
            Arguments.of(new ListTypeTest(
                ListType.CST_WEEKLY_HEARING_LIST,
                CST_WEEKLY_HEARING_LIST_JSON_FILE_PATH,
                HEARING_LENGTH)),
            Arguments.of(new ListTypeTest(
                ListType.PHT_WEEKLY_HEARING_LIST,
                PHT_WEEKLY_HEARING_LIST_JSON_FILE_PATH,
                HEARING_LENGTH))
        );
    }

    public static Stream<Arguments> modeOfHearingMandatoryAttribute() {
        return Stream.of(
            Arguments.of(new ListTypeTest(
                ListType.GRC_WEEKLY_HEARING_LIST,
                GRC_WEEKLY_HEARING_LIST_JSON_FILE_PATH,
                MODE_OF_HEARING)),
            Arguments.of(new ListTypeTest(
                ListType.UT_AAC_DAILY_HEARING_LIST,
                UT_ADMINISTRATIVE_APPEALS_CHAMBER_DAILY_HEARING_LIST_JSON_FILE_PATH,
                MODE_OF_HEARING)),
            Arguments.of(new ListTypeTest(
                ListType.UT_LC_DAILY_HEARING_LIST,
                UT_LANDS_CHAMBER_DAILY_HEARING_LIST_JSON_FILE_PATH,
                MODE_OF_HEARING)),
            Arguments.of(new ListTypeTest(
                ListType.WPAFCC_WEEKLY_HEARING_LIST,
                WPAFCC_WEEKLY_HEARING_LIST_JSON_FILE_PATH,
                MODE_OF_HEARING))
        );
    }

    public static Stream<Arguments> hearingListMandatoryAttributes() {
        return Stream.of(
            Arguments.of(new ListTypeTest(
                ListType.INTERIM_APPLICATIONS_CHD_DAILY_CAUSE_LIST,
                INTERIM_APPLICATION_CHANCERY_LIST_CHD_DAILY_CAUSE_LIST_JSON_FILE_PATH,
                HEARING_LIST_NODE))
        );
    }

    public static Stream<Arguments> openJusticeStatementDetailsMandatoryAttributes() {
        return Stream.of(
            Arguments.of(new ListTypeTest(
                ListType.INTERIM_APPLICATIONS_CHD_DAILY_CAUSE_LIST,
                INTERIM_APPLICATION_CHANCERY_LIST_CHD_DAILY_CAUSE_LIST_JSON_FILE_PATH,
                OPEN_JUSTICE_DETAILS_NODE))
        );
    }

    public static Stream<Arguments> nameToBeDisplayedMandatoryAttribute() {
        return Stream.of(
            Arguments.of(new ListTypeTest(
                ListType.INTERIM_APPLICATIONS_CHD_DAILY_CAUSE_LIST,
                INTERIM_APPLICATION_CHANCERY_LIST_CHD_DAILY_CAUSE_LIST_JSON_FILE_PATH,
                NAME_TO_BE_DISPLAYED, OPEN_JUSTICE_DETAILS_NODE))
        );
    }

    public static Stream<Arguments> emailMandatoryAttribute() {
        return Stream.of(
            Arguments.of(new ListTypeTest(
                ListType.INTERIM_APPLICATIONS_CHD_DAILY_CAUSE_LIST,
                INTERIM_APPLICATION_CHANCERY_LIST_CHD_DAILY_CAUSE_LIST_JSON_FILE_PATH,
                EMAIL, OPEN_JUSTICE_DETAILS_NODE))
        );
    }

    public static Stream<Arguments> courtRoomMandatoryAttribute() {
        return Stream.of(
            Arguments.of(new ListTypeTest(
                ListType.PAAC_WEEKLY_HEARING_LIST,
                SIAC_WEEKLY_HEARING_LIST_JSON_FILE_PATH,
                COURT_ROOM)),
            Arguments.of(new ListTypeTest(
                ListType.SIAC_WEEKLY_HEARING_LIST,
                SIAC_WEEKLY_HEARING_LIST_JSON_FILE_PATH,
                COURT_ROOM)),
            Arguments.of(new ListTypeTest(
                ListType.POAC_WEEKLY_HEARING_LIST,
                SIAC_WEEKLY_HEARING_LIST_JSON_FILE_PATH,
                COURT_ROOM)),
            Arguments.of(new ListTypeTest(
                ListType.SSCS_LONDON_DAILY_HEARING_LIST,
                SSCS_DAILY_HEARING_LIST_JSON_FILE_PATH,
                COURT_ROOM)),
            Arguments.of(new ListTypeTest(
                ListType.SSCS_MIDLANDS_DAILY_HEARING_LIST,
                SSCS_DAILY_HEARING_LIST_JSON_FILE_PATH,
                COURT_ROOM)),
            Arguments.of(new ListTypeTest(
                ListType.SSCS_NORTH_EAST_DAILY_HEARING_LIST,
                SSCS_DAILY_HEARING_LIST_JSON_FILE_PATH,
                COURT_ROOM)),
            Arguments.of(new ListTypeTest(
                ListType.SSCS_NORTH_WEST_DAILY_HEARING_LIST,
                SSCS_DAILY_HEARING_LIST_JSON_FILE_PATH,
                COURT_ROOM)),
            Arguments.of(new ListTypeTest(
                ListType.SSCS_SCOTLAND_DAILY_HEARING_LIST,
                SSCS_DAILY_HEARING_LIST_JSON_FILE_PATH,
                COURT_ROOM)),
            Arguments.of(new ListTypeTest(
                ListType.SSCS_SOUTH_EAST_DAILY_HEARING_LIST,
                SSCS_DAILY_HEARING_LIST_JSON_FILE_PATH,
                COURT_ROOM)),
            Arguments.of(new ListTypeTest(
                ListType.SSCS_WALES_AND_SOUTH_WEST_DAILY_HEARING_LIST,
                SSCS_DAILY_HEARING_LIST_JSON_FILE_PATH,
                COURT_ROOM))
        );
    }

    public static Stream<Arguments> hearingMethodMandatoryAttribute() {
        return Stream.of(
            Arguments.of(new ListTypeTest(
                ListType.RPT_EASTERN_WEEKLY_HEARING_LIST,
                RPT_WEEKLY_HEARING_LIST_JSON_FILE_PATH,
                HEARING_METHOD)),
            Arguments.of(new ListTypeTest(
                ListType.RPT_LONDON_WEEKLY_HEARING_LIST,
                RPT_WEEKLY_HEARING_LIST_JSON_FILE_PATH,
                HEARING_METHOD)),
            Arguments.of(new ListTypeTest(
                ListType.RPT_MIDLANDS_WEEKLY_HEARING_LIST,
                RPT_WEEKLY_HEARING_LIST_JSON_FILE_PATH,
                HEARING_METHOD)),
            Arguments.of(new ListTypeTest(
                ListType.RPT_NORTHERN_WEEKLY_HEARING_LIST,
                RPT_WEEKLY_HEARING_LIST_JSON_FILE_PATH,
                HEARING_METHOD)),
            Arguments.of(new ListTypeTest(
                ListType.RPT_SOUTHERN_WEEKLY_HEARING_LIST,
                RPT_WEEKLY_HEARING_LIST_JSON_FILE_PATH,
                HEARING_METHOD))
        );
    }

    public static Stream<Arguments> respondentMandatoryAttribute() {
        return Stream.of(
            Arguments.of(new ListTypeTest(
                ListType.SEND_DAILY_HEARING_LIST,
                SEND_DAILY_HEARING_LIST_JSON_FILE_PATH,
                RESPONDENT))
        );
    }

    public static Stream<Arguments> timeEstimateMandatoryAttribute() {
        return Stream.of(
            Arguments.of(new ListTypeTest(
                ListType.SEND_DAILY_HEARING_LIST,
                SEND_DAILY_HEARING_LIST_JSON_FILE_PATH,
                TIME_ESTIMATE))
        );
    }

    public static Stream<Arguments> panelMandatoryAttribute() {
        return Stream.of(
            Arguments.of(new ListTypeTest(
                ListType.SSCS_LONDON_DAILY_HEARING_LIST,
                SSCS_DAILY_HEARING_LIST_JSON_FILE_PATH,
                PANEL)),
            Arguments.of(new ListTypeTest(
                ListType.SSCS_MIDLANDS_DAILY_HEARING_LIST,
                SSCS_DAILY_HEARING_LIST_JSON_FILE_PATH,
                PANEL)),
            Arguments.of(new ListTypeTest(
                ListType.SSCS_NORTH_EAST_DAILY_HEARING_LIST,
                SSCS_DAILY_HEARING_LIST_JSON_FILE_PATH,
                PANEL)),
            Arguments.of(new ListTypeTest(
                ListType.SSCS_NORTH_WEST_DAILY_HEARING_LIST,
                SSCS_DAILY_HEARING_LIST_JSON_FILE_PATH,
                PANEL)),
            Arguments.of(new ListTypeTest(
                ListType.SSCS_SCOTLAND_DAILY_HEARING_LIST,
                SSCS_DAILY_HEARING_LIST_JSON_FILE_PATH,
                PANEL)),
            Arguments.of(new ListTypeTest(
                ListType.SSCS_SOUTH_EAST_DAILY_HEARING_LIST,
                SSCS_DAILY_HEARING_LIST_JSON_FILE_PATH,
                PANEL)),
            Arguments.of(new ListTypeTest(
                ListType.WPAFCC_WEEKLY_HEARING_LIST,
                WPAFCC_WEEKLY_HEARING_LIST_JSON_FILE_PATH,
                PANEL))
        );
    }

    public static Stream<Arguments> ftaRespondentMandatoryAttribute() {
        return Stream.of(
            Arguments.of(new ListTypeTest(
                ListType.SSCS_LONDON_DAILY_HEARING_LIST,
                SSCS_DAILY_HEARING_LIST_JSON_FILE_PATH,
                FTA_RESPONDENT)),
            Arguments.of(new ListTypeTest(
                ListType.SSCS_MIDLANDS_DAILY_HEARING_LIST,
                SSCS_DAILY_HEARING_LIST_JSON_FILE_PATH,
                FTA_RESPONDENT)),
            Arguments.of(new ListTypeTest(
                ListType.SSCS_NORTH_EAST_DAILY_HEARING_LIST,
                SSCS_DAILY_HEARING_LIST_JSON_FILE_PATH,
                FTA_RESPONDENT)),
            Arguments.of(new ListTypeTest(
                ListType.SSCS_NORTH_WEST_DAILY_HEARING_LIST,
                SSCS_DAILY_HEARING_LIST_JSON_FILE_PATH,
                FTA_RESPONDENT)),
            Arguments.of(new ListTypeTest(
                ListType.SSCS_SCOTLAND_DAILY_HEARING_LIST,
                SSCS_DAILY_HEARING_LIST_JSON_FILE_PATH,
                FTA_RESPONDENT)),
            Arguments.of(new ListTypeTest(
                ListType.SSCS_SOUTH_EAST_DAILY_HEARING_LIST,
                SSCS_DAILY_HEARING_LIST_JSON_FILE_PATH,
                FTA_RESPONDENT)),
            Arguments.of(new ListTypeTest(
                ListType.SSCS_WALES_AND_SOUTH_WEST_DAILY_HEARING_LIST,
                SSCS_DAILY_HEARING_LIST_JSON_FILE_PATH,
                FTA_RESPONDENT))
        );
    }

    public static Stream<Arguments> caseTitleMandatoryAttribute() {
        return Stream.of(
            Arguments.of(new ListTypeTest(
                ListType.UT_IAC_JR_BIRMINGHAM_DAILY_HEARING_LIST,
                UT_IAC_JUDICIAL_REVIEWS_DAILY_HEARING_LIST_JSON_FILE_PATH,
                CASE_TITLE
            )),
            Arguments.of(new ListTypeTest(
                ListType.UT_IAC_JR_CARDIFF_DAILY_HEARING_LIST,
                UT_IAC_JUDICIAL_REVIEWS_DAILY_HEARING_LIST_JSON_FILE_PATH,
                CASE_TITLE
            )),
            Arguments.of(new ListTypeTest(
                ListType.UT_IAC_JR_LEEDS_DAILY_HEARING_LIST,
                UT_IAC_JUDICIAL_REVIEWS_DAILY_HEARING_LIST_JSON_FILE_PATH,
                CASE_TITLE
            )),
            Arguments.of(new ListTypeTest(
                ListType.UT_IAC_JR_LONDON_DAILY_HEARING_LIST,
                UT_IAC_JUDICIAL_REVIEWS_DAILY_HEARING_LIST_JSON_FILE_PATH,
                CASE_TITLE
            )),
            Arguments.of(new ListTypeTest(
                ListType.UT_IAC_JR_MANCHESTER_DAILY_HEARING_LIST,
                UT_IAC_JUDICIAL_REVIEWS_DAILY_HEARING_LIST_JSON_FILE_PATH,
                CASE_TITLE
            ))
        );
    }

    public static Stream<Arguments> representativeMandatoryAttribute() {
        return Stream.of(
            Arguments.of(new ListTypeTest(
                ListType.UT_IAC_JR_LONDON_DAILY_HEARING_LIST,
                UT_IAC_JUDICIAL_REVIEWS_DAILY_HEARING_LIST_JSON_FILE_PATH,
                REPRESENTATIVE
            )),
            Arguments.of(new ListTypeTest(
                ListType.UT_IAC_STATUTORY_APPEALS_DAILY_HEARING_LIST,
                UT_IAC_STATUTORY_APPEALS_DAILY_HEARING_LIST_JSON_FILE_PATH,
                REPRESENTATIVE))
        );
    }

    public static Stream<Arguments> locationMandatoryAttribute() {
        return Stream.of(
            Arguments.of(new ListTypeTest(
                ListType.UT_IAC_STATUTORY_APPEALS_DAILY_HEARING_LIST,
                UT_IAC_STATUTORY_APPEALS_DAILY_HEARING_LIST_JSON_FILE_PATH,
                LOCATION))
        );
    }

}
