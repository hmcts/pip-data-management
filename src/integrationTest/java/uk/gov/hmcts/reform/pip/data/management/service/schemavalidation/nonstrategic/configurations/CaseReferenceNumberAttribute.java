package uk.gov.hmcts.reform.pip.data.management.service.schemavalidation.nonstrategic.configurations;

import org.junit.jupiter.params.provider.Arguments;
import uk.gov.hmcts.reform.pip.model.publication.ListType;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static uk.gov.hmcts.reform.pip.data.management.service.schemavalidation.nonstrategic.NonStrategicListTestConstants.CASE_REFERENCE_NUMBER;

public final class CaseReferenceNumberAttribute {
    private static final Map<ListType, String> LIST_TYPE_JSON_FILE = Map.ofEntries(
        ListTypeEntries.CIC_WEEKLY_HEARING_LIST_ENTRY,
        ListTypeEntries.FTT_LR_WEEKLY_HEARING_LIST_ENTRY,
        ListTypeEntries.FTT_TAX_WEEKLY_HEARING_LIST_ENTRY,
        ListTypeEntries.GRC_WEEKLY_HEARING_LIST_ENTRY,
        ListTypeEntries.PAAC_WEEKLY_HEARING_LIST_ENTRY,
        ListTypeEntries.SIAC_WEEKLY_HEARING_LIST_ENTRY,
        ListTypeEntries.POAC_WEEKLY_HEARING_LIST_ENTRY,
        ListTypeEntries.RPT_EASTERN_WEEKLY_HEARING_LIST_ENTRY,
        ListTypeEntries.RPT_LONDON_WEEKLY_HEARING_LIST_ENTRY,
        ListTypeEntries.RPT_MIDLANDS_WEEKLY_HEARING_LIST_ENTRY,
        ListTypeEntries.RPT_NORTHERN_WEEKLY_HEARING_LIST_ENTRY,
        ListTypeEntries.RPT_SOUTHERN_WEEKLY_HEARING_LIST_ENTRY,
        ListTypeEntries.SEND_DAILY_HEARING_LIST_ENTRY,
        ListTypeEntries.UT_AAC_DAILY_HEARING_LIST_ENTRY,
        ListTypeEntries.UT_IAC_JR_BIRMINGHAM_DAILY_HEARING_LIST_ENTRY,
        ListTypeEntries.UT_IAC_JR_CARDIFF_DAILY_HEARING_LIST_ENTRY,
        ListTypeEntries.UT_IAC_JR_LEEDS_DAILY_HEARING_LIST_ENTRY,
        ListTypeEntries.UT_IAC_JR_LONDON_DAILY_HEARING_LIST_ENTRY,
        ListTypeEntries.UT_IAC_JR_MANCHESTER_DAILY_HEARING_LIST_ENTRY,
        ListTypeEntries.UT_LC_DAILY_HEARING_LIST_ENTRY,
        ListTypeEntries.UT_T_AND_CC_DAILY_HEARING_LIST_ENTRY,
        ListTypeEntries.WPAFCC_WEEKLY_HEARING_LIST_ENTRY
    );

    private static final Map<ListType, List<String>> LIST_TYPE_JSON_FILE_PARENT_NODES =
        Collections.emptyMap();

    public static Stream<Arguments> caseReferenceNumberMandatoryAttribute() {
        return SchemaValidationTestInput.generateListTypeTestInputsForAttribute(LIST_TYPE_JSON_FILE,
            LIST_TYPE_JSON_FILE_PARENT_NODES, CASE_REFERENCE_NUMBER)
            .stream()
            .map(Arguments::of);
    }

    private CaseReferenceNumberAttribute() {
    }
}
