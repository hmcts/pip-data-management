package uk.gov.hmcts.reform.pip.data.management.service.schemavalidation.nonstrategic.configurations;

import org.junit.jupiter.params.provider.Arguments;
import uk.gov.hmcts.reform.pip.model.publication.ListType;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static uk.gov.hmcts.reform.pip.data.management.service.schemavalidation.nonstrategic.NonStrategicListTestConstants.HEARING_TIME;

public final class HearingTimeAttribute {
    private static final Map<ListType, String> LIST_TYPE_JSON_FILE = Map.ofEntries(
        ListTypeEntries.AST_DAILY_HEARING_LIST_ENTRY,
        ListTypeEntries.CIC_WEEKLY_HEARING_LIST_ENTRY,
        ListTypeEntries.FTT_LR_WEEKLY_HEARING_LIST_ENTRY,
        ListTypeEntries.FTT_TAX_WEEKLY_HEARING_LIST_ENTRY,
        ListTypeEntries.GRC_WEEKLY_HEARING_LIST_ENTRY,
        ListTypeEntries.SSCS_LONDON_DAILY_HEARING_LIST_ENTRY,
        ListTypeEntries.SSCS_MIDLANDS_DAILY_HEARING_LIST_ENTRY,
        ListTypeEntries.SSCS_NORTH_EAST_DAILY_HEARING_LIST_ENTRY,
        ListTypeEntries.SSCS_NORTH_WEST_DAILY_HEARING_LIST_ENTRY,
        ListTypeEntries.SSCS_SCOTLAND_DAILY_HEARING_LIST_ENTRY,
        ListTypeEntries.SSCS_SOUTH_EAST_DAILY_HEARING_LIST_ENTRY,
        ListTypeEntries.SSCS_WALES_AND_SOUTH_WEST_DAILY_HEARING_LIST_ENTRY,
        ListTypeEntries.UT_IAC_JR_BIRMINGHAM_DAILY_HEARING_LIST_ENTRY,
        ListTypeEntries.UT_IAC_JR_CARDIFF_DAILY_HEARING_LIST_ENTRY,
        ListTypeEntries.UT_IAC_JR_LEEDS_DAILY_HEARING_LIST_ENTRY,
        ListTypeEntries.UT_IAC_JR_LONDON_DAILY_HEARING_LIST_ENTRY,
        ListTypeEntries.UT_IAC_JR_MANCHESTER_DAILY_HEARING_LIST_ENTRY,
        ListTypeEntries.UT_IAC_STATUTORY_APPEALS_DAILY_HEARING_LIST_ENTRY,
        ListTypeEntries.WPAFCC_WEEKLY_HEARING_LIST_ENTRY
    );

    private static final Map<ListType, List<String>> LIST_TYPE_JSON_FILE_PARENT_NODES =
        Collections.emptyMap();

    public static Stream<Arguments> hearingTimeMandatoryAttribute() {
        return SchemaValidationTestInput.generateListTypeTestInputsForAttribute(LIST_TYPE_JSON_FILE,
            LIST_TYPE_JSON_FILE_PARENT_NODES, HEARING_TIME)
            .stream()
            .map(Arguments::of);
    }

    private HearingTimeAttribute() {
    }
}
