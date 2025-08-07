package uk.gov.hmcts.reform.pip.data.management.service.schemavalidation.nonstrategic.configurations;

import uk.gov.hmcts.reform.pip.model.publication.ListType;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.pip.data.management.service.schemavalidation.nonstrategic.NonStrategicListTestConstants.CASE_REFERENCE_NUMBER;

public final class CaseReferenceNumberValidationData extends AbstractSchemaValidationTestDataProvider {
    @Override
    protected Map<ListType, String> getListTypeJsonFile() {
        return Map.ofEntries(
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
    }

    @Override
    protected Map<ListType, List<String>> getListTypeJsonFileParentNodes() {
        return Collections.emptyMap();
    }

    @Override
    protected String getAttributeToValidate() {
        return CASE_REFERENCE_NUMBER;
    }
}
