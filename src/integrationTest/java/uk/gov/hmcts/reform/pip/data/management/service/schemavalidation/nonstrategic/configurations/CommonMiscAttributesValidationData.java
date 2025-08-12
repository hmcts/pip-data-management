package uk.gov.hmcts.reform.pip.data.management.service.schemavalidation.nonstrategic.configurations;

import org.junit.jupiter.params.provider.Arguments;
import uk.gov.hmcts.reform.pip.model.publication.ListType;

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

public final class CommonMiscAttributesValidationData {
    private CommonMiscAttributesValidationData() {
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

        AbstractSchemaValidationTestDataProvider attributeValidationProvider =
                new CommonAttributesSchemaValidationTestDataProvider(listTypeJsonFile, APPELLANT);
        return attributeValidationProvider.attributeValidationTestInputs();
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

        AbstractSchemaValidationTestDataProvider attributeValidationProvider =
                new CommonAttributesSchemaValidationTestDataProvider(listTypeJsonFile, APPEAL_REFERENCE_NUMBER);
        return attributeValidationProvider.attributeValidationTestInputs();
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

        AbstractSchemaValidationTestDataProvider attributeValidationProvider =
            new CommonAttributesSchemaValidationTestDataProvider(listTypeJsonFile, CASE_TYPE);
        return attributeValidationProvider.attributeValidationTestInputs();
    }

    public static Stream<Arguments> venuePlatformMandatoryAttribute() {
        Map<ListType, String> listTypeJsonFile = Map.ofEntries(
            ListTypeEntries.CIC_WEEKLY_HEARING_LIST_ENTRY,
            ListTypeEntries.FTT_LR_WEEKLY_HEARING_LIST_ENTRY,
            ListTypeEntries.FTT_TAX_WEEKLY_HEARING_LIST_ENTRY
        );

        AbstractSchemaValidationTestDataProvider attributeValidationProvider =
            new CommonAttributesSchemaValidationTestDataProvider(listTypeJsonFile, VENUE_PLATFORM);
        return attributeValidationProvider.attributeValidationTestInputs();
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

        AbstractSchemaValidationTestDataProvider attributeValidationProvider =
            new CommonAttributesSchemaValidationTestDataProvider(listTypeJsonFile, MEMBERS);
        return attributeValidationProvider.attributeValidationTestInputs();
    }

    public static Stream<Arguments> hearingLengthMandatoryAttribute() {
        Map<ListType, String> listTypeJsonFile = Map.ofEntries(
            ListTypeEntries.CST_WEEKLY_HEARING_LIST_ENTRY,
            ListTypeEntries.PHT_WEEKLY_HEARING_LIST_ENTRY
        );

        AbstractSchemaValidationTestDataProvider attributeValidationProvider =
            new CommonAttributesSchemaValidationTestDataProvider(listTypeJsonFile, HEARING_LENGTH);
        return attributeValidationProvider.attributeValidationTestInputs();
    }

    public static Stream<Arguments> modeOfHearingMandatoryAttribute() {
        Map<ListType, String> listTypeJsonFile = Map.ofEntries(
            ListTypeEntries.GRC_WEEKLY_HEARING_LIST_ENTRY,
            ListTypeEntries.UT_AAC_DAILY_HEARING_LIST_ENTRY,
            ListTypeEntries.UT_LC_DAILY_HEARING_LIST_ENTRY,
            ListTypeEntries.WPAFCC_WEEKLY_HEARING_LIST_ENTRY
        );

        AbstractSchemaValidationTestDataProvider attributeValidationProvider =
            new CommonAttributesSchemaValidationTestDataProvider(listTypeJsonFile, MODE_OF_HEARING);
        return attributeValidationProvider.attributeValidationTestInputs();
    }

    public static Stream<Arguments> hearingListMandatoryAttributes() {
        Map<ListType, String> listTypeJsonFile = Map.ofEntries(
            ListTypeEntries.INTERIM_APPLICATIONS_CHD_DAILY_CAUSE_LIST_ENTRY
        );

        Map<ListType, List<String>> listTypeJsonFileParentNode = Map.of(
            ListType.INTERIM_APPLICATIONS_CHD_DAILY_CAUSE_LIST,
            List.of(HEARING_LIST_NODE)
        );

        AbstractSchemaValidationTestDataProvider attributeValidationProvider =
            new CommonAttributesSchemaValidationTestDataProvider(listTypeJsonFile, listTypeJsonFileParentNode,
                                                                 HEARING_LIST_NODE);
        return attributeValidationProvider.attributeValidationTestInputs();
    }

    public static Stream<Arguments> openJusticeStatementDetailsMandatoryAttributes() {
        Map<ListType, String> listTypeJsonFile = Map.ofEntries(
            ListTypeEntries.INTERIM_APPLICATIONS_CHD_DAILY_CAUSE_LIST_ENTRY
        );

        Map<ListType, List<String>> listTypeJsonFileParentNode = Map.of(
            ListType.INTERIM_APPLICATIONS_CHD_DAILY_CAUSE_LIST,
            List.of(OPEN_JUSTICE_DETAILS_NODE)
        );

        AbstractSchemaValidationTestDataProvider attributeValidationProvider =
            new CommonAttributesSchemaValidationTestDataProvider(listTypeJsonFile, listTypeJsonFileParentNode,
                                                                 OPEN_JUSTICE_DETAILS_NODE);
        return attributeValidationProvider.attributeValidationTestInputs();
    }

    public static Stream<Arguments> nameToBeDisplayedMandatoryAttribute() {
        Map<ListType, String> listTypeJsonFile = Map.ofEntries(
            ListTypeEntries.INTERIM_APPLICATIONS_CHD_DAILY_CAUSE_LIST_ENTRY
        );

        Map<ListType, List<String>> listTypeJsonFileParentNode = Map.of(
            ListType.INTERIM_APPLICATIONS_CHD_DAILY_CAUSE_LIST,
            List.of(OPEN_JUSTICE_DETAILS_NODE)
        );

        AbstractSchemaValidationTestDataProvider attributeValidationProvider =
            new CommonAttributesSchemaValidationTestDataProvider(listTypeJsonFile, listTypeJsonFileParentNode,
                                                                 NAME_TO_BE_DISPLAYED);
        return attributeValidationProvider.attributeValidationTestInputs();
    }

    public static Stream<Arguments> emailMandatoryAttribute() {
        Map<ListType, String> listTypeJsonFile = Map.ofEntries(
            ListTypeEntries.INTERIM_APPLICATIONS_CHD_DAILY_CAUSE_LIST_ENTRY
        );

        Map<ListType, List<String>> listTypeJsonFileParentNode = Map.of(
            ListType.INTERIM_APPLICATIONS_CHD_DAILY_CAUSE_LIST,
            List.of(OPEN_JUSTICE_DETAILS_NODE)
        );

        AbstractSchemaValidationTestDataProvider attributeValidationProvider =
            new CommonAttributesSchemaValidationTestDataProvider(listTypeJsonFile, listTypeJsonFileParentNode,
                                                                 EMAIL);
        return attributeValidationProvider.attributeValidationTestInputs();
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

        AbstractSchemaValidationTestDataProvider attributeValidationProvider =
            new CommonAttributesSchemaValidationTestDataProvider(listTypeJsonFile, COURT_ROOM);
        return attributeValidationProvider.attributeValidationTestInputs();
    }

    public static Stream<Arguments> hearingMethodMandatoryAttribute() {
        Map<ListType, String> listTypeJsonFile = Map.ofEntries(
            ListTypeEntries.RPT_EASTERN_WEEKLY_HEARING_LIST_ENTRY,
            ListTypeEntries.RPT_MIDLANDS_WEEKLY_HEARING_LIST_ENTRY,
            ListTypeEntries.RPT_NORTHERN_WEEKLY_HEARING_LIST_ENTRY,
            ListTypeEntries.RPT_SOUTHERN_WEEKLY_HEARING_LIST_ENTRY,
            ListTypeEntries.RPT_LONDON_WEEKLY_HEARING_LIST_ENTRY
        );

        AbstractSchemaValidationTestDataProvider attributeValidationProvider =
            new CommonAttributesSchemaValidationTestDataProvider(listTypeJsonFile, HEARING_METHOD);
        return attributeValidationProvider.attributeValidationTestInputs();
    }

    public static Stream<Arguments> respondentMandatoryAttribute() {
        Map<ListType, String> listTypeJsonFile = Map.ofEntries(
            entry(ListType.SEND_DAILY_HEARING_LIST, SEND_DAILY_HEARING_LIST_JSON_FILE_PATH)
        );

        AbstractSchemaValidationTestDataProvider attributeValidationProvider =
            new CommonAttributesSchemaValidationTestDataProvider(listTypeJsonFile, RESPONDENT);
        return attributeValidationProvider.attributeValidationTestInputs();
    }

    public static Stream<Arguments> timeEstimateMandatoryAttribute() {
        Map<ListType, String> listTypeJsonFile = Map.ofEntries(
            entry(ListType.SEND_DAILY_HEARING_LIST, SEND_DAILY_HEARING_LIST_JSON_FILE_PATH)
        );

        AbstractSchemaValidationTestDataProvider attributeValidationProvider =
            new CommonAttributesSchemaValidationTestDataProvider(listTypeJsonFile, TIME_ESTIMATE);
        return attributeValidationProvider.attributeValidationTestInputs();
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

        AbstractSchemaValidationTestDataProvider attributeValidationProvider =
            new CommonAttributesSchemaValidationTestDataProvider(listTypeJsonFile, PANEL);
        return attributeValidationProvider.attributeValidationTestInputs();
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

        AbstractSchemaValidationTestDataProvider attributeValidationProvider =
            new CommonAttributesSchemaValidationTestDataProvider(listTypeJsonFile, FTA_RESPONDENT);
        return attributeValidationProvider.attributeValidationTestInputs();
    }

    public static Stream<Arguments> caseTitleMandatoryAttribute() {
        Map<ListType, String> listTypeJsonFile = Map.ofEntries(
            ListTypeEntries.UT_IAC_JR_BIRMINGHAM_DAILY_HEARING_LIST_ENTRY,
            ListTypeEntries.UT_IAC_JR_CARDIFF_DAILY_HEARING_LIST_ENTRY,
            ListTypeEntries.UT_IAC_JR_LEEDS_DAILY_HEARING_LIST_ENTRY,
            ListTypeEntries.UT_IAC_JR_LONDON_DAILY_HEARING_LIST_ENTRY,
            ListTypeEntries.UT_IAC_JR_MANCHESTER_DAILY_HEARING_LIST_ENTRY
        );

        AbstractSchemaValidationTestDataProvider attributeValidationProvider =
            new CommonAttributesSchemaValidationTestDataProvider(listTypeJsonFile, CASE_TITLE);
        return attributeValidationProvider.attributeValidationTestInputs();
    }

    public static Stream<Arguments> representativeMandatoryAttribute() {
        Map<ListType, String> listTypeJsonFile = Map.ofEntries(
            ListTypeEntries.UT_IAC_JR_LONDON_DAILY_HEARING_LIST_ENTRY,
            ListTypeEntries.UT_IAC_STATUTORY_APPEALS_DAILY_HEARING_LIST_ENTRY
        );

        AbstractSchemaValidationTestDataProvider attributeValidationProvider =
            new CommonAttributesSchemaValidationTestDataProvider(listTypeJsonFile, REPRESENTATIVE);
        return attributeValidationProvider.attributeValidationTestInputs();
    }

    public static Stream<Arguments> locationMandatoryAttribute() {
        Map<ListType, String> listTypeJsonFile = Map.ofEntries(
            ListTypeEntries.UT_IAC_STATUTORY_APPEALS_DAILY_HEARING_LIST_ENTRY
        );

        AbstractSchemaValidationTestDataProvider attributeValidationProvider =
            new CommonAttributesSchemaValidationTestDataProvider(listTypeJsonFile, LOCATION);
        return attributeValidationProvider.attributeValidationTestInputs();
    }

}
