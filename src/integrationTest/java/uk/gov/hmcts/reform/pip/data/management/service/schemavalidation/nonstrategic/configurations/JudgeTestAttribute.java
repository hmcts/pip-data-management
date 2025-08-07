package uk.gov.hmcts.reform.pip.data.management.service.schemavalidation.nonstrategic.configurations;

import org.junit.jupiter.params.provider.Arguments;
import uk.gov.hmcts.reform.pip.model.publication.ListType;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static java.util.Map.entry;
import static uk.gov.hmcts.reform.pip.data.management.service.schemavalidation.nonstrategic.NonStrategicListTestConstants.FUTURE_JUDGMENTS_NODE;
import static uk.gov.hmcts.reform.pip.data.management.service.schemavalidation.nonstrategic.NonStrategicListTestConstants.HEARING_LIST_NODE;
import static uk.gov.hmcts.reform.pip.data.management.service.schemavalidation.nonstrategic.NonStrategicListTestConstants.JUDGE;
import static uk.gov.hmcts.reform.pip.data.management.service.schemavalidation.nonstrategic.NonStrategicListTestConstants.LONDON_ADMINISTRATIVE_COURT_NODE;
import static uk.gov.hmcts.reform.pip.data.management.service.schemavalidation.nonstrategic.NonStrategicListTestConstants.PLANNING_COURT_NODE;

public final class JudgeTestAttribute {
    private static final Map<ListType, String> LIST_TYPE_JSON_FILE = Map.ofEntries(
        ListTypeEntries.ADMIRALTY_COURT_KB_DAILY_CAUSE_LIST_ENTRY,
        ListTypeEntries.BIRMINGHAM_ADMINISTRATIVE_COURT_DAILY_CAUSE_LIST_ENTRY,
        ListTypeEntries.BRISTOL_AND_CARDIFF_ADMINISTRATIVE_COURT_DAILY_CAUSE_LIST_ENTRY,
        ListTypeEntries.BUSINESS_LIST_CHD_DAILY_CAUSE_LIST_ENTRY,
        ListTypeEntries.CHANCERY_APPEALS_CHD_DAILY_CAUSE_LIST_ENTRY,
        ListTypeEntries.CIVIL_COURTS_RCJ_DAILY_CAUSE_LIST_ENTRY,
        ListTypeEntries.COMMERCIAL_COURT_KB_DAILY_CAUSE_LIST_ENTRY,
        ListTypeEntries.COMPANIES_WINDING_UP_CHD_DAILY_CAUSE_LIST_ENTRY,
        ListTypeEntries.COMPETITION_LIST_CHD_DAILY_CAUSE_LIST_ENTRY,
        ListTypeEntries.COUNTY_COURT_LONDON_CIVIL_DAILY_CAUSE_LIST_ENTRY,
        ListTypeEntries.COURT_OF_APPEAL_CIVIL_DAILY_CAUSE_LIST_ENTRY,
        ListTypeEntries.COURT_OF_APPEAL_CRIMINAL_DAILY_CAUSE_LIST_ENTRY,
        ListTypeEntries.FAMILY_DIVISION_HIGH_COURT_DAILY_CAUSE_LIST_ENTRY,
        ListTypeEntries.FINANCIAL_LIST_CHD_KB_DAILY_CAUSE_LIST_ENTRY,
        ListTypeEntries.FTT_LR_WEEKLY_HEARING_LIST_ENTRY,
        ListTypeEntries.INSOLVENCY_AND_COMPANIES_COURT_CHD_DAILY_CAUSE_LIST_ENTRY,
        ListTypeEntries.INTELLECTUAL_PROPERTY_AND_ENTERPRISE_COURT_DAILY_CAUSE_LIST_ENTRY,
        ListTypeEntries.INTELLECTUAL_PROPERTY_LIST_CHD_DAILY_CAUSE_LIST_ENTRY,
        ListTypeEntries.INTERIM_APPLICATIONS_CHD_DAILY_CAUSE_LIST_ENTRY,
        ListTypeEntries.KINGS_BENCH_DIVISION_DAILY_CAUSE_LIST_ENTRY,
        ListTypeEntries.KINGS_BENCH_MASTERS_DAILY_CAUSE_LIST_ENTRY,
        ListTypeEntries.LEEDS_ADMINISTRATIVE_COURT_DAILY_CAUSE_LIST_ENTRY,
        ListTypeEntries.LONDON_ADMINISTRATIVE_COURT_DAILY_CAUSE_LIST_ENTRY,
        ListTypeEntries.MANCHESTER_ADMINISTRATIVE_COURT_DAILY_CAUSE_LIST_ENTRY,
        ListTypeEntries.LONDON_CIRCUIT_COMMERCIAL_COURT_KB_DAILY_CAUSE_LIST_ENTRY,
        ListTypeEntries.MAYOR_AND_CITY_CIVIL_DAILY_CAUSE_LIST_ENTRY,
        ListTypeEntries.PATENTS_COURT_CHD_DAILY_CAUSE_LIST_ENTRY,
        ListTypeEntries.PENSIONS_LIST_CHD_DAILY_CAUSE_LIST_ENTRY,
        ListTypeEntries.PROPERTY_TRUSTS_PROBATE_LIST_CHD_DAILY_CAUSE_LIST_ENTRY,
        ListTypeEntries.SENIOR_COURTS_COSTS_OFFICE_DAILY_CAUSE_LIST_ENTRY,
        ListTypeEntries.TECHNOLOGY_AND_CONSTRUCTION_COURT_KB_DAILY_CAUSE_LIST_ENTRY
    );

    private static final Map<ListType, List<String>> LIST_TYPE_JSON_FILE_PARENT_NODES = Map.ofEntries(
        entry(ListType.COURT_OF_APPEAL_CIVIL_DAILY_CAUSE_LIST, List.of(HEARING_LIST_NODE, FUTURE_JUDGMENTS_NODE)),
        entry(ListType.LONDON_ADMINISTRATIVE_COURT_DAILY_CAUSE_LIST,
              List.of(LONDON_ADMINISTRATIVE_COURT_NODE, PLANNING_COURT_NODE)),
        entry(ListType.INTERIM_APPLICATIONS_CHD_DAILY_CAUSE_LIST, List.of(HEARING_LIST_NODE))
    );

    public static Stream<Arguments> judgeMandatoryAttribute() {
        return SchemaValidationTestInput.generateListTypeTestInputsForAttribute(LIST_TYPE_JSON_FILE,
            LIST_TYPE_JSON_FILE_PARENT_NODES, JUDGE)
            .stream()
            .map(Arguments::of);
    }

    private JudgeTestAttribute() {
    }
}
