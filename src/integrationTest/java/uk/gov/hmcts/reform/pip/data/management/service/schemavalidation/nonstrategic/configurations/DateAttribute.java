package uk.gov.hmcts.reform.pip.data.management.service.schemavalidation.nonstrategic.configurations;

import org.junit.jupiter.params.provider.Arguments;
import uk.gov.hmcts.reform.pip.model.publication.ListType;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static uk.gov.hmcts.reform.pip.data.management.service.schemavalidation.nonstrategic.NonStrategicListTestConstants.DATE;

public final class DateAttribute {
    private static final Map<ListType, String> LIST_TYPE_JSON_FILE = Map.ofEntries(
        ListTypeEntries.CIC_WEEKLY_HEARING_LIST_ENTRY,
        ListTypeEntries.CST_WEEKLY_HEARING_LIST_ENTRY,
        ListTypeEntries.FTT_LR_WEEKLY_HEARING_LIST_ENTRY,
        ListTypeEntries.FTT_TAX_WEEKLY_HEARING_LIST_ENTRY,
        ListTypeEntries.GRC_WEEKLY_HEARING_LIST_ENTRY,
        ListTypeEntries.PAAC_WEEKLY_HEARING_LIST_ENTRY,
        ListTypeEntries.SIAC_WEEKLY_HEARING_LIST_ENTRY,
        ListTypeEntries.POAC_WEEKLY_HEARING_LIST_ENTRY,
        ListTypeEntries.PHT_WEEKLY_HEARING_LIST_ENTRY,
        ListTypeEntries.RPT_EASTERN_WEEKLY_HEARING_LIST_ENTRY,
        ListTypeEntries.RPT_LONDON_WEEKLY_HEARING_LIST_ENTRY,
        ListTypeEntries.RPT_MIDLANDS_WEEKLY_HEARING_LIST_ENTRY,
        ListTypeEntries.RPT_NORTHERN_WEEKLY_HEARING_LIST_ENTRY,
        ListTypeEntries.RPT_SOUTHERN_WEEKLY_HEARING_LIST_ENTRY,
        ListTypeEntries.WPAFCC_WEEKLY_HEARING_LIST_ENTRY
    );

    private static final Map<ListType, List<String>> LIST_TYPE_JSON_FILE_PARENT_NODES =
        Collections.emptyMap();

    public static Stream<Arguments> dateMandatoryAttribute() {
        return SchemaValidationTestInput.generateListTypeTestInputsForAttribute(LIST_TYPE_JSON_FILE,
            LIST_TYPE_JSON_FILE_PARENT_NODES, DATE)
            .stream()
            .map(Arguments::of);
    }

    private DateAttribute() {
    }
}
