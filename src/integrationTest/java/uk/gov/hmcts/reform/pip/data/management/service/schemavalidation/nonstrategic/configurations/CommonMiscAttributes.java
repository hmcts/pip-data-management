package uk.gov.hmcts.reform.pip.data.management.service.schemavalidation.nonstrategic.configurations;

import org.junit.jupiter.params.provider.Arguments;
import uk.gov.hmcts.reform.pip.model.publication.ListType;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static java.util.Map.entry;
import static uk.gov.hmcts.reform.pip.data.management.service.schemavalidation.nonstrategic.NonStrategicListTestConstants.APPEAL_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.pip.data.management.service.schemavalidation.nonstrategic.NonStrategicListTestConstants.APPELLANT;
import static uk.gov.hmcts.reform.pip.data.management.service.schemavalidation.nonstrategic.NonStrategicListTestConstants.CASE_TITLE;
import static uk.gov.hmcts.reform.pip.data.management.service.schemavalidation.nonstrategic.NonStrategicListTestConstants.CASE_TYPE;
import static uk.gov.hmcts.reform.pip.data.management.service.schemavalidation.nonstrategic.NonStrategicListTestConstants.COURT_ROOM;
import static uk.gov.hmcts.reform.pip.data.management.service.schemavalidation.nonstrategic.NonStrategicListTestConstants.EMAIL;
import static uk.gov.hmcts.reform.pip.data.management.service.schemavalidation.nonstrategic.NonStrategicListTestConstants.FTA_RESPONDENT;
import static uk.gov.hmcts.reform.pip.data.management.service.schemavalidation.nonstrategic.NonStrategicListTestConstants.HEARING_LENGTH;
import static uk.gov.hmcts.reform.pip.data.management.service.schemavalidation.nonstrategic.NonStrategicListTestConstants.HEARING_LIST_NODE;
import static uk.gov.hmcts.reform.pip.data.management.service.schemavalidation.nonstrategic.NonStrategicListTestConstants.HEARING_METHOD;
import static uk.gov.hmcts.reform.pip.data.management.service.schemavalidation.nonstrategic.NonStrategicListTestConstants.LOCATION;
import static uk.gov.hmcts.reform.pip.data.management.service.schemavalidation.nonstrategic.NonStrategicListTestConstants.MEMBERS;
import static uk.gov.hmcts.reform.pip.data.management.service.schemavalidation.nonstrategic.NonStrategicListTestConstants.MODE_OF_HEARING;
import static uk.gov.hmcts.reform.pip.data.management.service.schemavalidation.nonstrategic.NonStrategicListTestConstants.NAME_TO_BE_DISPLAYED;
import static uk.gov.hmcts.reform.pip.data.management.service.schemavalidation.nonstrategic.NonStrategicListTestConstants.OPEN_JUSTICE_DETAILS_NODE;
import static uk.gov.hmcts.reform.pip.data.management.service.schemavalidation.nonstrategic.NonStrategicListTestConstants.PANEL;
import static uk.gov.hmcts.reform.pip.data.management.service.schemavalidation.nonstrategic.NonStrategicListTestConstants.REPRESENTATIVE;
import static uk.gov.hmcts.reform.pip.data.management.service.schemavalidation.nonstrategic.NonStrategicListTestConstants.RESPONDENT;
import static uk.gov.hmcts.reform.pip.data.management.service.schemavalidation.nonstrategic.NonStrategicListTestConstants.SEND_DAILY_HEARING_LIST_JSON_FILE_PATH;
import static uk.gov.hmcts.reform.pip.data.management.service.schemavalidation.nonstrategic.NonStrategicListTestConstants.TIME_ESTIMATE;
import static uk.gov.hmcts.reform.pip.data.management.service.schemavalidation.nonstrategic.NonStrategicListTestConstants.VENUE_PLATFORM;

public final class CommonMiscAttributes {
    private CommonMiscAttributes() {
    }

    public static Stream<Arguments> appellantMandatoryAttribute() {
        Map<ListType, String> listTypeJsonFile = Map.ofEntries(
            ListTypeEntries.AST_DAILY_HEARING_LIST_ENTRY,
            ListTypeEntries.SSCS_LONDON_DAILY_HEARING_LIST_ENTRY,
            ListTypeEntries.SSCS_MIDLANDS_DAILY_HEARING_LIST_ENTRY,
            ListTypeEntries.SSCS_NORTH_EAST_DAILY_HEARING_LIST_ENTRY,
            ListTypeEntries.SSCS_NORTH_WEST_DAILY_HEARING_LIST_ENTRY,
            ListTypeEntries.SSCS_SCOTLAND_DAILY_HEARING_LIST_ENTRY,
            ListTypeEntries.SSCS_SOUTH_EAST_DAILY_HEARING_LIST_ENTRY,
            ListTypeEntries.SSCS_WALES_AND_SOUTH_WEST_DAILY_HEARING_LIST_ENTRY,
            ListTypeEntries.UT_AAC_DAILY_HEARING_LIST_ENTRY,
            ListTypeEntries.UT_IAC_STATUTORY_APPEALS_DAILY_HEARING_LIST_ENTRY
        );

        return SchemaValidationTestInput.generateListTypeTestInputsForAttribute(listTypeJsonFile,
            new EnumMap<>(ListType.class), APPELLANT)
            .stream()
            .map(Arguments::of);
    }

    public static Stream<Arguments> appealReferenceNumberMandatoryAttribute() {
        Map<ListType, String> listTypeJsonFile = Map.ofEntries(
            ListTypeEntries.AST_DAILY_HEARING_LIST_ENTRY,
            ListTypeEntries.SSCS_LONDON_DAILY_HEARING_LIST_ENTRY,
            ListTypeEntries.SSCS_MIDLANDS_DAILY_HEARING_LIST_ENTRY,
            ListTypeEntries.SSCS_NORTH_EAST_DAILY_HEARING_LIST_ENTRY,
            ListTypeEntries.SSCS_NORTH_WEST_DAILY_HEARING_LIST_ENTRY,
            ListTypeEntries.SSCS_SCOTLAND_DAILY_HEARING_LIST_ENTRY,
            ListTypeEntries.SSCS_SOUTH_EAST_DAILY_HEARING_LIST_ENTRY,
            ListTypeEntries.SSCS_WALES_AND_SOUTH_WEST_DAILY_HEARING_LIST_ENTRY,
            ListTypeEntries.UT_IAC_STATUTORY_APPEALS_DAILY_HEARING_LIST_ENTRY
        );

        return SchemaValidationTestInput.generateListTypeTestInputsForAttribute(listTypeJsonFile,
            new EnumMap<>(ListType.class), APPEAL_REFERENCE_NUMBER)
            .stream()
            .map(Arguments::of);
    }

    public static Stream<Arguments> caseTypeMandatoryAttribute() {
        Map<ListType, String> listTypeJsonFile = Map.ofEntries(
            ListTypeEntries.AST_DAILY_HEARING_LIST_ENTRY,
            ListTypeEntries.RPT_EASTERN_WEEKLY_HEARING_LIST_ENTRY,
            ListTypeEntries.RPT_LONDON_WEEKLY_HEARING_LIST_ENTRY,
            ListTypeEntries.RPT_MIDLANDS_WEEKLY_HEARING_LIST_ENTRY,
            ListTypeEntries.RPT_NORTHERN_WEEKLY_HEARING_LIST_ENTRY,
            ListTypeEntries.RPT_SOUTHERN_WEEKLY_HEARING_LIST_ENTRY
        );

        return SchemaValidationTestInput.generateListTypeTestInputsForAttribute(listTypeJsonFile,
            new EnumMap<>(ListType.class), CASE_TYPE)
            .stream()
            .map(Arguments::of);
    }

    public static Stream<Arguments> venuePlatformMandatoryAttribute() {
        Map<ListType, String> listTypeJsonFile = Map.ofEntries(
            ListTypeEntries.CIC_WEEKLY_HEARING_LIST_ENTRY,
            ListTypeEntries.FTT_LR_WEEKLY_HEARING_LIST_ENTRY,
            ListTypeEntries.FTT_TAX_WEEKLY_HEARING_LIST_ENTRY
        );

        return SchemaValidationTestInput.generateListTypeTestInputsForAttribute(listTypeJsonFile,
            new EnumMap<>(ListType.class), VENUE_PLATFORM)
            .stream()
            .map(Arguments::of);
    }

    public static Stream<Arguments> membersMandatoryAttribute() {
        Map<ListType, String> listTypeJsonFile = Map.ofEntries(
            ListTypeEntries.CIC_WEEKLY_HEARING_LIST_ENTRY,
            ListTypeEntries.FTT_TAX_WEEKLY_HEARING_LIST_ENTRY,
            ListTypeEntries.GRC_WEEKLY_HEARING_LIST_ENTRY,
            ListTypeEntries.RPT_EASTERN_WEEKLY_HEARING_LIST_ENTRY,
            ListTypeEntries.RPT_LONDON_WEEKLY_HEARING_LIST_ENTRY,
            ListTypeEntries.RPT_MIDLANDS_WEEKLY_HEARING_LIST_ENTRY,
            ListTypeEntries.RPT_NORTHERN_WEEKLY_HEARING_LIST_ENTRY,
            ListTypeEntries.RPT_SOUTHERN_WEEKLY_HEARING_LIST_ENTRY,
            ListTypeEntries.UT_AAC_DAILY_HEARING_LIST_ENTRY,
            ListTypeEntries.UT_LC_DAILY_HEARING_LIST_ENTRY,
            ListTypeEntries.UT_T_AND_CC_DAILY_HEARING_LIST_ENTRY
        );

        return SchemaValidationTestInput.generateListTypeTestInputsForAttribute(listTypeJsonFile,
            new EnumMap<>(ListType.class), MEMBERS)
            .stream()
            .map(Arguments::of);
    }

    public static Stream<Arguments> hearingLengthMandatoryAttribute() {
        Map<ListType, String> listTypeJsonFile = Map.ofEntries(
            ListTypeEntries.CST_WEEKLY_HEARING_LIST_ENTRY,
            ListTypeEntries.PHT_WEEKLY_HEARING_LIST_ENTRY
        );

        return SchemaValidationTestInput.generateListTypeTestInputsForAttribute(listTypeJsonFile,
            new EnumMap<>(ListType.class), HEARING_LENGTH)
            .stream()
            .map(Arguments::of);
    }

    public static Stream<Arguments> modeOfHearingMandatoryAttribute() {
        Map<ListType, String> listTypeJsonFile = Map.ofEntries(
            ListTypeEntries.GRC_WEEKLY_HEARING_LIST_ENTRY,
            ListTypeEntries.UT_AAC_DAILY_HEARING_LIST_ENTRY,
            ListTypeEntries.UT_LC_DAILY_HEARING_LIST_ENTRY,
            ListTypeEntries.WPAFCC_WEEKLY_HEARING_LIST_ENTRY
        );

        return SchemaValidationTestInput.generateListTypeTestInputsForAttribute(listTypeJsonFile,
            new EnumMap<>(ListType.class), MODE_OF_HEARING)
            .stream()
            .map(Arguments::of);
    }

    public static Stream<Arguments> hearingListMandatoryAttributes() {
        Map<ListType, String> listTypeJsonFile = Map.ofEntries(
            ListTypeEntries.INTERIM_APPLICATIONS_CHD_DAILY_CAUSE_LIST_ENTRY
        );

        Map<ListType, List<String>> listTypeJsonFileParentNode = Map.of(
            ListType.INTERIM_APPLICATIONS_CHD_DAILY_CAUSE_LIST,
            List.of(HEARING_LIST_NODE)
        );

        return SchemaValidationTestInput.generateListTypeTestInputsForAttribute(listTypeJsonFile,
            listTypeJsonFileParentNode, HEARING_LIST_NODE)
            .stream()
            .map(Arguments::of);
    }

    public static Stream<Arguments> openJusticeStatementDetailsMandatoryAttributes() {
        Map<ListType, String> listTypeJsonFile = Map.ofEntries(
            ListTypeEntries.INTERIM_APPLICATIONS_CHD_DAILY_CAUSE_LIST_ENTRY
        );

        Map<ListType, List<String>> listTypeJsonFileParentNode = Map.of(
            ListType.INTERIM_APPLICATIONS_CHD_DAILY_CAUSE_LIST,
            List.of(OPEN_JUSTICE_DETAILS_NODE)
        );

        return SchemaValidationTestInput.generateListTypeTestInputsForAttribute(listTypeJsonFile,
            listTypeJsonFileParentNode, OPEN_JUSTICE_DETAILS_NODE)
            .stream()
            .map(Arguments::of);
    }

    public static Stream<Arguments> nameToBeDisplayedMandatoryAttribute() {
        Map<ListType, String> listTypeJsonFile = Map.ofEntries(
            ListTypeEntries.INTERIM_APPLICATIONS_CHD_DAILY_CAUSE_LIST_ENTRY
        );

        Map<ListType, List<String>> listTypeJsonFileParentNode = Map.of(
            ListType.INTERIM_APPLICATIONS_CHD_DAILY_CAUSE_LIST,
            List.of(OPEN_JUSTICE_DETAILS_NODE)
        );

        return SchemaValidationTestInput.generateListTypeTestInputsForAttribute(listTypeJsonFile,
            listTypeJsonFileParentNode, NAME_TO_BE_DISPLAYED)
            .stream()
            .map(Arguments::of);
    }

    public static Stream<Arguments> emailMandatoryAttribute() {
        Map<ListType, String> listTypeJsonFile = Map.ofEntries(
            ListTypeEntries.INTERIM_APPLICATIONS_CHD_DAILY_CAUSE_LIST_ENTRY
        );

        Map<ListType, List<String>> listTypeJsonFileParentNode = Map.of(
            ListType.INTERIM_APPLICATIONS_CHD_DAILY_CAUSE_LIST,
            List.of(OPEN_JUSTICE_DETAILS_NODE)
        );

        return SchemaValidationTestInput.generateListTypeTestInputsForAttribute(listTypeJsonFile,
            listTypeJsonFileParentNode, EMAIL)
            .stream()
            .map(Arguments::of);
    }

    public static Stream<Arguments> courtRoomMandatoryAttribute() {
        Map<ListType, String> listTypeJsonFile = Map.ofEntries(
            ListTypeEntries.PAAC_WEEKLY_HEARING_LIST_ENTRY,
            ListTypeEntries.SIAC_WEEKLY_HEARING_LIST_ENTRY,
            ListTypeEntries.POAC_WEEKLY_HEARING_LIST_ENTRY,
            ListTypeEntries.SSCS_LONDON_DAILY_HEARING_LIST_ENTRY,
            ListTypeEntries.SSCS_MIDLANDS_DAILY_HEARING_LIST_ENTRY,
            ListTypeEntries.SSCS_NORTH_EAST_DAILY_HEARING_LIST_ENTRY,
            ListTypeEntries.SSCS_NORTH_WEST_DAILY_HEARING_LIST_ENTRY,
            ListTypeEntries.SSCS_SCOTLAND_DAILY_HEARING_LIST_ENTRY,
            ListTypeEntries.SSCS_SOUTH_EAST_DAILY_HEARING_LIST_ENTRY,
            ListTypeEntries.SSCS_WALES_AND_SOUTH_WEST_DAILY_HEARING_LIST_ENTRY
        );

        return SchemaValidationTestInput.generateListTypeTestInputsForAttribute(listTypeJsonFile,
            new EnumMap<>(ListType.class), COURT_ROOM)
            .stream()
            .map(Arguments::of);
    }

    public static Stream<Arguments> hearingMethodMandatoryAttribute() {
        Map<ListType, String> listTypeJsonFile = Map.ofEntries(
            ListTypeEntries.RPT_EASTERN_WEEKLY_HEARING_LIST_ENTRY,
            ListTypeEntries.RPT_MIDLANDS_WEEKLY_HEARING_LIST_ENTRY,
            ListTypeEntries.RPT_NORTHERN_WEEKLY_HEARING_LIST_ENTRY,
            ListTypeEntries.RPT_SOUTHERN_WEEKLY_HEARING_LIST_ENTRY,
            ListTypeEntries.RPT_LONDON_WEEKLY_HEARING_LIST_ENTRY
        );

        return SchemaValidationTestInput.generateListTypeTestInputsForAttribute(listTypeJsonFile,
            new EnumMap<>(ListType.class), HEARING_METHOD)
            .stream()
            .map(Arguments::of);
    }

    public static Stream<Arguments> respondentMandatoryAttribute() {
        Map<ListType, String> listTypeJsonFile = Map.ofEntries(
            entry(ListType.SEND_DAILY_HEARING_LIST, SEND_DAILY_HEARING_LIST_JSON_FILE_PATH)
        );

        return SchemaValidationTestInput.generateListTypeTestInputsForAttribute(listTypeJsonFile,
            new EnumMap<>(ListType.class), RESPONDENT)
            .stream()
            .map(Arguments::of);
    }

    public static Stream<Arguments> timeEstimateMandatoryAttribute() {
        Map<ListType, String> listTypeJsonFile = Map.ofEntries(
            entry(ListType.SEND_DAILY_HEARING_LIST, SEND_DAILY_HEARING_LIST_JSON_FILE_PATH)
        );

        return SchemaValidationTestInput.generateListTypeTestInputsForAttribute(listTypeJsonFile,
            new EnumMap<>(ListType.class), TIME_ESTIMATE)
            .stream()
            .map(Arguments::of);
    }

    public static Stream<Arguments> panelMandatoryAttribute() {
        Map<ListType, String> listTypeJsonFile = Map.ofEntries(
            ListTypeEntries.SSCS_LONDON_DAILY_HEARING_LIST_ENTRY,
            ListTypeEntries.SSCS_MIDLANDS_DAILY_HEARING_LIST_ENTRY,
            ListTypeEntries.SSCS_NORTH_EAST_DAILY_HEARING_LIST_ENTRY,
            ListTypeEntries.SSCS_NORTH_WEST_DAILY_HEARING_LIST_ENTRY,
            ListTypeEntries.SSCS_SCOTLAND_DAILY_HEARING_LIST_ENTRY,
            ListTypeEntries.SSCS_SOUTH_EAST_DAILY_HEARING_LIST_ENTRY,
            ListTypeEntries.SSCS_WALES_AND_SOUTH_WEST_DAILY_HEARING_LIST_ENTRY,
            ListTypeEntries.WPAFCC_WEEKLY_HEARING_LIST_ENTRY
        );

        return SchemaValidationTestInput.generateListTypeTestInputsForAttribute(listTypeJsonFile,
            new EnumMap<>(ListType.class), PANEL)
            .stream()
            .map(Arguments::of);
    }

    public static Stream<Arguments> ftaRespondentMandatoryAttribute() {
        Map<ListType, String> listTypeJsonFile = Map.ofEntries(
            ListTypeEntries.SSCS_LONDON_DAILY_HEARING_LIST_ENTRY,
            ListTypeEntries.SSCS_MIDLANDS_DAILY_HEARING_LIST_ENTRY,
            ListTypeEntries.SSCS_NORTH_EAST_DAILY_HEARING_LIST_ENTRY,
            ListTypeEntries.SSCS_NORTH_WEST_DAILY_HEARING_LIST_ENTRY,
            ListTypeEntries.SSCS_SCOTLAND_DAILY_HEARING_LIST_ENTRY,
            ListTypeEntries.SSCS_SOUTH_EAST_DAILY_HEARING_LIST_ENTRY,
            ListTypeEntries.SSCS_WALES_AND_SOUTH_WEST_DAILY_HEARING_LIST_ENTRY
        );

        return SchemaValidationTestInput.generateListTypeTestInputsForAttribute(listTypeJsonFile,
            new EnumMap<>(ListType.class), FTA_RESPONDENT)
            .stream()
            .map(Arguments::of);
    }

    public static Stream<Arguments> caseTitleMandatoryAttribute() {
        Map<ListType, String> listTypeJsonFile = Map.ofEntries(
            ListTypeEntries.UT_IAC_JR_BIRMINGHAM_DAILY_HEARING_LIST_ENTRY,
            ListTypeEntries.UT_IAC_JR_CARDIFF_DAILY_HEARING_LIST_ENTRY,
            ListTypeEntries.UT_IAC_JR_LEEDS_DAILY_HEARING_LIST_ENTRY,
            ListTypeEntries.UT_IAC_JR_LONDON_DAILY_HEARING_LIST_ENTRY,
            ListTypeEntries.UT_IAC_JR_MANCHESTER_DAILY_HEARING_LIST_ENTRY
        );

        return SchemaValidationTestInput.generateListTypeTestInputsForAttribute(listTypeJsonFile,
            new EnumMap<>(ListType.class), CASE_TITLE)
            .stream()
            .map(Arguments::of);
    }

    public static Stream<Arguments> representativeMandatoryAttribute() {
        Map<ListType, String> listTypeJsonFile = Map.ofEntries(
            ListTypeEntries.UT_IAC_JR_LONDON_DAILY_HEARING_LIST_ENTRY,
            ListTypeEntries.UT_IAC_STATUTORY_APPEALS_DAILY_HEARING_LIST_ENTRY
        );

        return SchemaValidationTestInput.generateListTypeTestInputsForAttribute(listTypeJsonFile,
            new EnumMap<>(ListType.class), REPRESENTATIVE)
            .stream()
            .map(Arguments::of);
    }

    public static Stream<Arguments> locationMandatoryAttribute() {
        Map<ListType, String> listTypeJsonFile = Map.ofEntries(
            ListTypeEntries.UT_IAC_STATUTORY_APPEALS_DAILY_HEARING_LIST_ENTRY
        );

        return SchemaValidationTestInput.generateListTypeTestInputsForAttribute(listTypeJsonFile,
            new EnumMap<>(ListType.class), LOCATION)
            .stream()
            .map(Arguments::of);
    }

}
