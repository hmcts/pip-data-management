package uk.gov.hmcts.reform.pip.data.management.service.schemavalidation.nonstrategic.Configurations;

import org.junit.jupiter.params.provider.Arguments;
import uk.gov.hmcts.reform.pip.model.publication.ListType;

import java.util.stream.Stream;

import static uk.gov.hmcts.reform.pip.data.management.service.schemavalidation.nonstrategic.NonStrategicListTestConstants.AST_DAILY_HEARING_LIST_JSON_FILE_PATH;
import static uk.gov.hmcts.reform.pip.data.management.service.schemavalidation.nonstrategic.NonStrategicListTestConstants.CIC_WEEKLY_HEARING_LIST_JSON_FILE_PATH;
import static uk.gov.hmcts.reform.pip.data.management.service.schemavalidation.nonstrategic.NonStrategicListTestConstants.FTT_LR_WEEKLY_HEARING_LIST_JSON_FILE_PATH;
import static uk.gov.hmcts.reform.pip.data.management.service.schemavalidation.nonstrategic.NonStrategicListTestConstants.FTT_TAX_WEEKLY_HEARING_LIST_JSON_FILE_PATH;
import static uk.gov.hmcts.reform.pip.data.management.service.schemavalidation.nonstrategic.NonStrategicListTestConstants.GRC_WEEKLY_HEARING_LIST_JSON_FILE_PATH;
import static uk.gov.hmcts.reform.pip.data.management.service.schemavalidation.nonstrategic.NonStrategicListTestConstants.HEARING_TIME;
import static uk.gov.hmcts.reform.pip.data.management.service.schemavalidation.nonstrategic.NonStrategicListTestConstants.SSCS_DAILY_HEARING_LIST_JSON_FILE_PATH;
import static uk.gov.hmcts.reform.pip.data.management.service.schemavalidation.nonstrategic.NonStrategicListTestConstants.UT_IAC_JUDICIAL_REVIEWS_DAILY_HEARING_LIST_JSON_FILE_PATH;
import static uk.gov.hmcts.reform.pip.data.management.service.schemavalidation.nonstrategic.NonStrategicListTestConstants.UT_IAC_STATUTORY_APPEALS_DAILY_HEARING_LIST_JSON_FILE_PATH;
import static uk.gov.hmcts.reform.pip.data.management.service.schemavalidation.nonstrategic.NonStrategicListTestConstants.WPAFCC_WEEKLY_HEARING_LIST_JSON_FILE_PATH;

public class HearingTimeTestConfigurations {
    public static Stream<Arguments> hearingTimeMandatoryAttribute() {
        return Stream.of(
            Arguments.of(new ListTypeTest(
                ListType.AST_DAILY_HEARING_LIST,
                AST_DAILY_HEARING_LIST_JSON_FILE_PATH,
                HEARING_TIME)),
            Arguments.of(new ListTypeTest(
                ListType.CIC_WEEKLY_HEARING_LIST,
                CIC_WEEKLY_HEARING_LIST_JSON_FILE_PATH,
                HEARING_TIME)),
            Arguments.of(new ListTypeTest(
                ListType.FTT_LR_WEEKLY_HEARING_LIST,
                FTT_LR_WEEKLY_HEARING_LIST_JSON_FILE_PATH,
                HEARING_TIME)),
            Arguments.of(new ListTypeTest(
                ListType.FTT_TAX_WEEKLY_HEARING_LIST,
                FTT_TAX_WEEKLY_HEARING_LIST_JSON_FILE_PATH,
                HEARING_TIME)),
            Arguments.of(new ListTypeTest(
                ListType.GRC_WEEKLY_HEARING_LIST,
                GRC_WEEKLY_HEARING_LIST_JSON_FILE_PATH,
                HEARING_TIME)),
            Arguments.of(new ListTypeTest(
                ListType.SSCS_LONDON_DAILY_HEARING_LIST,
                SSCS_DAILY_HEARING_LIST_JSON_FILE_PATH,
                HEARING_TIME)),
            Arguments.of(new ListTypeTest(
                ListType.SSCS_MIDLANDS_DAILY_HEARING_LIST,
                SSCS_DAILY_HEARING_LIST_JSON_FILE_PATH,
                HEARING_TIME)),
            Arguments.of(new ListTypeTest(
                ListType.SSCS_NORTH_EAST_DAILY_HEARING_LIST,
                SSCS_DAILY_HEARING_LIST_JSON_FILE_PATH,
                HEARING_TIME)),
            Arguments.of(new ListTypeTest(
                ListType.SSCS_NORTH_WEST_DAILY_HEARING_LIST,
                SSCS_DAILY_HEARING_LIST_JSON_FILE_PATH,
                HEARING_TIME)),
            Arguments.of(new ListTypeTest(
                ListType.SSCS_SCOTLAND_DAILY_HEARING_LIST,
                SSCS_DAILY_HEARING_LIST_JSON_FILE_PATH,
                HEARING_TIME)),
            Arguments.of(new ListTypeTest(
                ListType.SSCS_SOUTH_EAST_DAILY_HEARING_LIST,
                SSCS_DAILY_HEARING_LIST_JSON_FILE_PATH,
                HEARING_TIME)),
            Arguments.of(new ListTypeTest(
                ListType.SSCS_WALES_AND_SOUTH_WEST_DAILY_HEARING_LIST,
                SSCS_DAILY_HEARING_LIST_JSON_FILE_PATH,
                HEARING_TIME)),
            Arguments.of(new ListTypeTest(
                ListType.UT_IAC_JR_BIRMINGHAM_DAILY_HEARING_LIST,
                UT_IAC_JUDICIAL_REVIEWS_DAILY_HEARING_LIST_JSON_FILE_PATH,
                HEARING_TIME)),
            Arguments.of(new ListTypeTest(
                ListType.UT_IAC_JR_CARDIFF_DAILY_HEARING_LIST,
                UT_IAC_JUDICIAL_REVIEWS_DAILY_HEARING_LIST_JSON_FILE_PATH,
                HEARING_TIME)),
            Arguments.of(new ListTypeTest(
                ListType.UT_IAC_JR_LEEDS_DAILY_HEARING_LIST,
                UT_IAC_JUDICIAL_REVIEWS_DAILY_HEARING_LIST_JSON_FILE_PATH,
                HEARING_TIME)),
            Arguments.of(new ListTypeTest(
                ListType.UT_IAC_JR_LONDON_DAILY_HEARING_LIST,
                UT_IAC_JUDICIAL_REVIEWS_DAILY_HEARING_LIST_JSON_FILE_PATH,
                HEARING_TIME)),
            Arguments.of(new ListTypeTest(
                ListType.UT_IAC_JR_MANCHESTER_DAILY_HEARING_LIST,
                UT_IAC_JUDICIAL_REVIEWS_DAILY_HEARING_LIST_JSON_FILE_PATH,
                HEARING_TIME)),
            Arguments.of(new ListTypeTest(
                ListType.UT_IAC_STATUTORY_APPEALS_DAILY_HEARING_LIST,
                UT_IAC_STATUTORY_APPEALS_DAILY_HEARING_LIST_JSON_FILE_PATH,
                HEARING_TIME)),
            Arguments.of(new ListTypeTest(
                ListType.WPAFCC_WEEKLY_HEARING_LIST,
                WPAFCC_WEEKLY_HEARING_LIST_JSON_FILE_PATH,
                HEARING_TIME))
        );
    }
}
