package uk.gov.hmcts.reform.pip.data.management.service.schemavalidation.nonstrategic.configurations;

import org.junit.jupiter.params.provider.Arguments;
import uk.gov.hmcts.reform.pip.model.publication.ListType;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static java.util.Map.entry;
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

public final class HearingTimeAttribute {
    private static final Map<ListType, String> LIST_TYPE_JSON_FILE = Map.ofEntries(
        entry(ListType.AST_DAILY_HEARING_LIST, AST_DAILY_HEARING_LIST_JSON_FILE_PATH),
        entry(ListType.CIC_WEEKLY_HEARING_LIST, CIC_WEEKLY_HEARING_LIST_JSON_FILE_PATH),
        entry(ListType.FTT_LR_WEEKLY_HEARING_LIST, FTT_LR_WEEKLY_HEARING_LIST_JSON_FILE_PATH),
        entry(ListType.FTT_TAX_WEEKLY_HEARING_LIST, FTT_TAX_WEEKLY_HEARING_LIST_JSON_FILE_PATH),
        entry(ListType.GRC_WEEKLY_HEARING_LIST, GRC_WEEKLY_HEARING_LIST_JSON_FILE_PATH),
        entry(ListType.SSCS_LONDON_DAILY_HEARING_LIST, SSCS_DAILY_HEARING_LIST_JSON_FILE_PATH),
        entry(ListType.SSCS_MIDLANDS_DAILY_HEARING_LIST, SSCS_DAILY_HEARING_LIST_JSON_FILE_PATH),
        entry(ListType.SSCS_NORTH_EAST_DAILY_HEARING_LIST, SSCS_DAILY_HEARING_LIST_JSON_FILE_PATH),
        entry(ListType.SSCS_NORTH_WEST_DAILY_HEARING_LIST, SSCS_DAILY_HEARING_LIST_JSON_FILE_PATH),
        entry(ListType.SSCS_SCOTLAND_DAILY_HEARING_LIST, SSCS_DAILY_HEARING_LIST_JSON_FILE_PATH),
        entry(ListType.SSCS_SOUTH_EAST_DAILY_HEARING_LIST, SSCS_DAILY_HEARING_LIST_JSON_FILE_PATH),
        entry(ListType.SSCS_WALES_AND_SOUTH_WEST_DAILY_HEARING_LIST, SSCS_DAILY_HEARING_LIST_JSON_FILE_PATH),
        entry(ListType.UT_IAC_JR_BIRMINGHAM_DAILY_HEARING_LIST,
              UT_IAC_JUDICIAL_REVIEWS_DAILY_HEARING_LIST_JSON_FILE_PATH),
        entry(ListType.UT_IAC_JR_CARDIFF_DAILY_HEARING_LIST,
              UT_IAC_JUDICIAL_REVIEWS_DAILY_HEARING_LIST_JSON_FILE_PATH),
        entry(ListType.UT_IAC_JR_LEEDS_DAILY_HEARING_LIST,
              UT_IAC_JUDICIAL_REVIEWS_DAILY_HEARING_LIST_JSON_FILE_PATH),
        entry(ListType.UT_IAC_JR_LONDON_DAILY_HEARING_LIST,
              UT_IAC_JUDICIAL_REVIEWS_DAILY_HEARING_LIST_JSON_FILE_PATH),
        entry(ListType.UT_IAC_JR_MANCHESTER_DAILY_HEARING_LIST,
              UT_IAC_JUDICIAL_REVIEWS_DAILY_HEARING_LIST_JSON_FILE_PATH),
        entry(ListType.UT_IAC_STATUTORY_APPEALS_DAILY_HEARING_LIST,
              UT_IAC_STATUTORY_APPEALS_DAILY_HEARING_LIST_JSON_FILE_PATH),
        entry(ListType.WPAFCC_WEEKLY_HEARING_LIST, WPAFCC_WEEKLY_HEARING_LIST_JSON_FILE_PATH)
    );

    private static final Map<ListType, List<String>> LIST_TYPE_JSON_FILE_PARENT_NODES =
        Collections.emptyMap();

    public static Stream<Arguments> hearingTimeMandatoryAttribute() {
        return ListTypeTestInput.generateListTypeTestInputsForAttribute(LIST_TYPE_JSON_FILE,
            LIST_TYPE_JSON_FILE_PARENT_NODES, HEARING_TIME)
            .stream()
            .map(Arguments::of);
    }

    private HearingTimeAttribute() {
    }
}
