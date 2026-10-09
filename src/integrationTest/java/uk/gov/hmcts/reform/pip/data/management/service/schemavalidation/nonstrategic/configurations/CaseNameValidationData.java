package uk.gov.hmcts.reform.pip.data.management.service.schemavalidation.nonstrategic.configurations;

import uk.gov.hmcts.reform.pip.model.publication.ListType;

import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.pip.data.management.service.schemavalidation.nonstrategic.NonStrategicListTestConstants.CASE_NAME;
import static uk.gov.hmcts.reform.pip.data.management.service.schemavalidation.nonstrategic.NonStrategicListTestConstants.HEARING_LIST_NODE;

public class CaseNameValidationData extends AbstractSchemaValidationTestDataProvider {
    @Override
    protected Map<ListType, String> getListTypeJsonFile() {
        return Map.ofEntries(ListTypeEntries.CIC_WEEKLY_HEARING_LIST_ENTRY,
            ListTypeEntries.CST_WEEKLY_HEARING_LIST_ENTRY,
            ListTypeEntries.FTT_LR_WEEKLY_HEARING_LIST_ENTRY,
            ListTypeEntries.FTT_TAX_WEEKLY_HEARING_LIST_ENTRY,
            ListTypeEntries.GRC_WEEKLY_HEARING_LIST_ENTRY,
            ListTypeEntries.INTERIM_APPLICATIONS_CHD_DAILY_CAUSE_LIST_ENTRY,
            ListTypeEntries.PHT_WEEKLY_HEARING_LIST_ENTRY,
            ListTypeEntries.UT_LC_DAILY_HEARING_LIST_ENTRY,
            ListTypeEntries.UT_T_AND_CC_DAILY_HEARING_LIST_ENTRY,
            ListTypeEntries.WPAFCC_WEEKLY_HEARING_LIST_ENTRY
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
        return CASE_NAME;
    }
}
