package uk.gov.hmcts.reform.pip.data.management.service.schemavalidation.nonstrategic.configurations;

import uk.gov.hmcts.reform.pip.model.publication.ListType;

import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.pip.data.management.service.schemavalidation.nonstrategic.NonStrategicListTestConstants.HEARING_LIST_NODE;
import static uk.gov.hmcts.reform.pip.data.management.service.schemavalidation.nonstrategic.NonStrategicListTestConstants.TYPE;

public final class TypeValidationData extends AbstractSchemaValidationTestDataProvider {
    @Override
    protected Map<ListType, String> getListTypeJsonFile() {
        return Map.ofEntries(
            ListTypeEntries.INTERIM_APPLICATIONS_CHD_DAILY_CAUSE_LIST_ENTRY
        );
    }

    @Override
    protected Map<ListType, List<String>> getListTypeJsonFileParentNodes() {
        return Map.of(
            ListType.INTERIM_APPLICATIONS_CHD_DAILY_CAUSE_LIST, List.of(HEARING_LIST_NODE)
        );
    }

    @Override
    protected String getAttributeToValidate() {
        return TYPE;
    }
}
