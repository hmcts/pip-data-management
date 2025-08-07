package uk.gov.hmcts.reform.pip.data.management.service.schemavalidation.nonstrategic.configurations;

import org.junit.jupiter.params.provider.Arguments;
import uk.gov.hmcts.reform.pip.model.publication.ListType;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static uk.gov.hmcts.reform.pip.data.management.service.schemavalidation.nonstrategic.NonStrategicListTestConstants.HEARING_LIST_NODE;
import static uk.gov.hmcts.reform.pip.data.management.service.schemavalidation.nonstrategic.NonStrategicListTestConstants.TYPE;

public final class TypeTestAttribute {
    private static final Map<ListType, String> LIST_TYPE_JSON_FILE = Map.ofEntries(
        ListTypeEntries.ADMIRALTY_COURT_KB_DAILY_CAUSE_LIST_ENTRY,
        ListTypeEntries.BUSINESS_LIST_CHD_DAILY_CAUSE_LIST_ENTRY,
        ListTypeEntries.CHANCERY_APPEALS_CHD_DAILY_CAUSE_LIST_ENTRY,
        ListTypeEntries.COMMERCIAL_COURT_KB_DAILY_CAUSE_LIST_ENTRY,
        ListTypeEntries.COMPANIES_WINDING_UP_CHD_DAILY_CAUSE_LIST_ENTRY,
        ListTypeEntries.COMPETITION_LIST_CHD_DAILY_CAUSE_LIST_ENTRY,
        ListTypeEntries.FINANCIAL_LIST_CHD_KB_DAILY_CAUSE_LIST_ENTRY,
        ListTypeEntries.INSOLVENCY_AND_COMPANIES_COURT_CHD_DAILY_CAUSE_LIST_ENTRY,
        ListTypeEntries.INTELLECTUAL_PROPERTY_AND_ENTERPRISE_COURT_DAILY_CAUSE_LIST_ENTRY,
        ListTypeEntries.INTELLECTUAL_PROPERTY_LIST_CHD_DAILY_CAUSE_LIST_ENTRY,
        ListTypeEntries.INTERIM_APPLICATIONS_CHD_DAILY_CAUSE_LIST_ENTRY,
        ListTypeEntries.LONDON_CIRCUIT_COMMERCIAL_COURT_KB_DAILY_CAUSE_LIST_ENTRY,
        ListTypeEntries.PATENTS_COURT_CHD_DAILY_CAUSE_LIST_ENTRY,
        ListTypeEntries.PENSIONS_LIST_CHD_DAILY_CAUSE_LIST_ENTRY,
        ListTypeEntries.PROPERTY_TRUSTS_PROBATE_LIST_CHD_DAILY_CAUSE_LIST_ENTRY,
        ListTypeEntries.TECHNOLOGY_AND_CONSTRUCTION_COURT_KB_DAILY_CAUSE_LIST_ENTRY
    );

    private static final Map<ListType, List<String>> LIST_TYPE_JSON_FILE_PARENT_NODES = Map.of(
        ListType.INTERIM_APPLICATIONS_CHD_DAILY_CAUSE_LIST, List.of(HEARING_LIST_NODE)
    );

    public static Stream<Arguments> typeMandatoryAttribute() {
        return SchemaValidationTestInput.generateListTypeTestInputsForAttribute(LIST_TYPE_JSON_FILE,
            LIST_TYPE_JSON_FILE_PARENT_NODES, TYPE)
            .stream()
            .map(Arguments::of);
    }

    private TypeTestAttribute() {
    }
}
