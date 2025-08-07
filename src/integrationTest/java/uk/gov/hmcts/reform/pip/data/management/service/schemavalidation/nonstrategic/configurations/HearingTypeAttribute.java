package uk.gov.hmcts.reform.pip.data.management.service.schemavalidation.nonstrategic.configurations;

import org.junit.jupiter.params.provider.Arguments;
import uk.gov.hmcts.reform.pip.model.publication.ListType;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static uk.gov.hmcts.reform.pip.data.management.service.schemavalidation.nonstrategic.NonStrategicListTestConstants.FUTURE_JUDGMENTS_NODE;
import static uk.gov.hmcts.reform.pip.data.management.service.schemavalidation.nonstrategic.NonStrategicListTestConstants.HEARING_LIST_NODE;
import static uk.gov.hmcts.reform.pip.data.management.service.schemavalidation.nonstrategic.NonStrategicListTestConstants.HEARING_TYPE;
import static uk.gov.hmcts.reform.pip.data.management.service.schemavalidation.nonstrategic.NonStrategicListTestConstants.LONDON_ADMINISTRATIVE_COURT_NODE;
import static uk.gov.hmcts.reform.pip.data.management.service.schemavalidation.nonstrategic.NonStrategicListTestConstants.PLANNING_COURT_NODE;

public final class HearingTypeAttribute {
    private static final Map<ListType, String> LIST_TYPE_JSON_FILE = Map.ofEntries(
        ListTypeEntries.AST_DAILY_HEARING_LIST_ENTRY,
        ListTypeEntries.BIRMINGHAM_ADMINISTRATIVE_COURT_DAILY_CAUSE_LIST_ENTRY,
        ListTypeEntries.BRISTOL_AND_CARDIFF_ADMINISTRATIVE_COURT_DAILY_CAUSE_LIST_ENTRY,
        ListTypeEntries.CIVIL_COURTS_RCJ_DAILY_CAUSE_LIST_ENTRY,
        ListTypeEntries.COUNTY_COURT_LONDON_CIVIL_DAILY_CAUSE_LIST_ENTRY,
        ListTypeEntries.COURT_OF_APPEAL_CIVIL_DAILY_CAUSE_LIST_ENTRY,
        ListTypeEntries.COURT_OF_APPEAL_CRIMINAL_DAILY_CAUSE_LIST_ENTRY,
        ListTypeEntries.CST_WEEKLY_HEARING_LIST_ENTRY,
        ListTypeEntries.FAMILY_DIVISION_HIGH_COURT_DAILY_CAUSE_LIST_ENTRY,
        ListTypeEntries.KINGS_BENCH_DIVISION_DAILY_CAUSE_LIST_ENTRY,
        ListTypeEntries.KINGS_BENCH_MASTERS_DAILY_CAUSE_LIST_ENTRY,
        ListTypeEntries.LEEDS_ADMINISTRATIVE_COURT_DAILY_CAUSE_LIST_ENTRY,
        ListTypeEntries.LONDON_ADMINISTRATIVE_COURT_DAILY_CAUSE_LIST_ENTRY,
        ListTypeEntries.MANCHESTER_ADMINISTRATIVE_COURT_DAILY_CAUSE_LIST_ENTRY,
        ListTypeEntries.MAYOR_AND_CITY_CIVIL_DAILY_CAUSE_LIST_ENTRY,
        ListTypeEntries.PAAC_WEEKLY_HEARING_LIST_ENTRY,
        ListTypeEntries.SIAC_WEEKLY_HEARING_LIST_ENTRY,
        ListTypeEntries.POAC_WEEKLY_HEARING_LIST_ENTRY,
        ListTypeEntries.PHT_WEEKLY_HEARING_LIST_ENTRY,
        ListTypeEntries.SEND_DAILY_HEARING_LIST_ENTRY,
        ListTypeEntries.SENIOR_COURTS_COSTS_OFFICE_DAILY_CAUSE_LIST_ENTRY,
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
        ListTypeEntries.UT_LC_DAILY_HEARING_LIST_ENTRY,
        ListTypeEntries.UT_T_AND_CC_DAILY_HEARING_LIST_ENTRY
    );

    private static final Map<ListType, List<String>> LIST_TYPE_JSON_FILE_PARENT_NODES = Map.of(
        ListType.COURT_OF_APPEAL_CIVIL_DAILY_CAUSE_LIST, List.of(HEARING_LIST_NODE, FUTURE_JUDGMENTS_NODE),
        ListType.LONDON_ADMINISTRATIVE_COURT_DAILY_CAUSE_LIST,
        List.of(LONDON_ADMINISTRATIVE_COURT_NODE, PLANNING_COURT_NODE)
    );

    public static Stream<Arguments> hearingTypeMandatoryAttribute() {
        return SchemaValidationTestInput.generateListTypeTestInputsForAttribute(LIST_TYPE_JSON_FILE,
            LIST_TYPE_JSON_FILE_PARENT_NODES, HEARING_TYPE)
            .stream()
            .map(Arguments::of);
    }

    private HearingTypeAttribute() {
    }
}
