package uk.gov.hmcts.reform.pip.data.management.service.schemavalidation.nonstrategic.configurations;

import uk.gov.hmcts.reform.pip.model.publication.ListType;

import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.pip.data.management.service.schemavalidation.nonstrategic.NonStrategicListTestConstants.CASE_NAME;
import static uk.gov.hmcts.reform.pip.data.management.service.schemavalidation.nonstrategic.NonStrategicListTestConstants.HEARING_LIST_NODE;

public class CaseNameValidationData extends AbstractSchemaValidationTestDataProvider {
    @Override
    protected Map<ListType, String> getListTypeJsonFile() {
        return Map.ofEntries(
            ListTypeEntries.ADMIRALTY_COURT_KB_DAILY_CAUSE_LIST_ENTRY,
            ListTypeEntries.BUSINESS_LIST_CHD_DAILY_CAUSE_LIST_ENTRY,
            ListTypeEntries.CHANCERY_APPEALS_CHD_DAILY_CAUSE_LIST_ENTRY,
            ListTypeEntries.CIC_WEEKLY_HEARING_LIST_ENTRY,
            ListTypeEntries.COMMERCIAL_COURT_KB_DAILY_CAUSE_LIST_ENTRY,
            ListTypeEntries.COMPANIES_WINDING_UP_CHD_DAILY_CAUSE_LIST_ENTRY,
            ListTypeEntries.COMPETITION_LIST_CHD_DAILY_CAUSE_LIST_ENTRY,
            ListTypeEntries.CST_WEEKLY_HEARING_LIST_ENTRY,
            ListTypeEntries.FINANCIAL_LIST_CHD_KB_DAILY_CAUSE_LIST_ENTRY,
            ListTypeEntries.FTT_LR_WEEKLY_HEARING_LIST_ENTRY,
            ListTypeEntries.FTT_TAX_WEEKLY_HEARING_LIST_ENTRY,
            ListTypeEntries.GRC_WEEKLY_HEARING_LIST_ENTRY,
            ListTypeEntries.INSOLVENCY_AND_COMPANIES_COURT_CHD_DAILY_CAUSE_LIST_ENTRY,
            ListTypeEntries.INTELLECTUAL_PROPERTY_AND_ENTERPRISE_COURT_DAILY_CAUSE_LIST_ENTRY,
            ListTypeEntries.INTELLECTUAL_PROPERTY_LIST_CHD_DAILY_CAUSE_LIST_ENTRY,
            ListTypeEntries.INTERIM_APPLICATIONS_CHD_DAILY_CAUSE_LIST_ENTRY,
            ListTypeEntries.LONDON_CIRCUIT_COMMERCIAL_COURT_KB_DAILY_CAUSE_LIST_ENTRY,
            ListTypeEntries.PATENTS_COURT_CHD_DAILY_CAUSE_LIST_ENTRY,
            ListTypeEntries.PENSIONS_LIST_CHD_DAILY_CAUSE_LIST_ENTRY,
            ListTypeEntries.PHT_WEEKLY_HEARING_LIST_ENTRY,
            ListTypeEntries.PROPERTY_TRUSTS_PROBATE_LIST_CHD_DAILY_CAUSE_LIST_ENTRY,
            ListTypeEntries.TECHNOLOGY_AND_CONSTRUCTION_COURT_KB_DAILY_CAUSE_LIST_ENTRY,
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
