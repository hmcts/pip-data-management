package uk.gov.hmcts.reform.pip.data.management.service.schemavalidation.nonstrategic.configurations;

import org.junit.jupiter.params.provider.Arguments;
import uk.gov.hmcts.reform.pip.model.publication.ListType;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static java.util.Map.entry;
import static uk.gov.hmcts.reform.pip.data.management.service.schemavalidation.nonstrategic.NonStrategicListTestConstants.CIC_WEEKLY_HEARING_LIST_JSON_FILE_PATH;
import static uk.gov.hmcts.reform.pip.data.management.service.schemavalidation.nonstrategic.NonStrategicListTestConstants.CST_WEEKLY_HEARING_LIST_JSON_FILE_PATH;
import static uk.gov.hmcts.reform.pip.data.management.service.schemavalidation.nonstrategic.NonStrategicListTestConstants.DATE;
import static uk.gov.hmcts.reform.pip.data.management.service.schemavalidation.nonstrategic.NonStrategicListTestConstants.FTT_LR_WEEKLY_HEARING_LIST_JSON_FILE_PATH;
import static uk.gov.hmcts.reform.pip.data.management.service.schemavalidation.nonstrategic.NonStrategicListTestConstants.FTT_TAX_WEEKLY_HEARING_LIST_JSON_FILE_PATH;
import static uk.gov.hmcts.reform.pip.data.management.service.schemavalidation.nonstrategic.NonStrategicListTestConstants.GRC_WEEKLY_HEARING_LIST_JSON_FILE_PATH;
import static uk.gov.hmcts.reform.pip.data.management.service.schemavalidation.nonstrategic.NonStrategicListTestConstants.PHT_WEEKLY_HEARING_LIST_JSON_FILE_PATH;
import static uk.gov.hmcts.reform.pip.data.management.service.schemavalidation.nonstrategic.NonStrategicListTestConstants.RPT_WEEKLY_HEARING_LIST_JSON_FILE_PATH;
import static uk.gov.hmcts.reform.pip.data.management.service.schemavalidation.nonstrategic.NonStrategicListTestConstants.SIAC_WEEKLY_HEARING_LIST_JSON_FILE_PATH;
import static uk.gov.hmcts.reform.pip.data.management.service.schemavalidation.nonstrategic.NonStrategicListTestConstants.WPAFCC_WEEKLY_HEARING_LIST_JSON_FILE_PATH;

public final class DateAttribute {
    private static final Map<ListType, String> LIST_TYPE_JSON_FILE = Map.ofEntries(
        entry(ListType.CIC_WEEKLY_HEARING_LIST, CIC_WEEKLY_HEARING_LIST_JSON_FILE_PATH),
        entry(ListType.CST_WEEKLY_HEARING_LIST, CST_WEEKLY_HEARING_LIST_JSON_FILE_PATH),
        entry(ListType.FTT_LR_WEEKLY_HEARING_LIST, FTT_LR_WEEKLY_HEARING_LIST_JSON_FILE_PATH),
        entry(ListType.FTT_TAX_WEEKLY_HEARING_LIST, FTT_TAX_WEEKLY_HEARING_LIST_JSON_FILE_PATH),
        entry(ListType.GRC_WEEKLY_HEARING_LIST, GRC_WEEKLY_HEARING_LIST_JSON_FILE_PATH),
        entry(ListType.PAAC_WEEKLY_HEARING_LIST, SIAC_WEEKLY_HEARING_LIST_JSON_FILE_PATH),
        entry(ListType.SIAC_WEEKLY_HEARING_LIST, SIAC_WEEKLY_HEARING_LIST_JSON_FILE_PATH),
        entry(ListType.POAC_WEEKLY_HEARING_LIST, SIAC_WEEKLY_HEARING_LIST_JSON_FILE_PATH),
        entry(ListType.PHT_WEEKLY_HEARING_LIST, PHT_WEEKLY_HEARING_LIST_JSON_FILE_PATH),
        entry(ListType.RPT_EASTERN_WEEKLY_HEARING_LIST, RPT_WEEKLY_HEARING_LIST_JSON_FILE_PATH),
        entry(ListType.RPT_LONDON_WEEKLY_HEARING_LIST, RPT_WEEKLY_HEARING_LIST_JSON_FILE_PATH),
        entry(ListType.RPT_MIDLANDS_WEEKLY_HEARING_LIST, RPT_WEEKLY_HEARING_LIST_JSON_FILE_PATH),
        entry(ListType.RPT_NORTHERN_WEEKLY_HEARING_LIST, RPT_WEEKLY_HEARING_LIST_JSON_FILE_PATH),
        entry(ListType.RPT_SOUTHERN_WEEKLY_HEARING_LIST, RPT_WEEKLY_HEARING_LIST_JSON_FILE_PATH),
        entry(ListType.WPAFCC_WEEKLY_HEARING_LIST, WPAFCC_WEEKLY_HEARING_LIST_JSON_FILE_PATH)
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
