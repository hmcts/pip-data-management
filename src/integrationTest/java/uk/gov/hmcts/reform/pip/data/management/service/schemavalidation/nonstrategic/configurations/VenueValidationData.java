package uk.gov.hmcts.reform.pip.data.management.service.schemavalidation.nonstrategic.configurations;

import uk.gov.hmcts.reform.pip.model.publication.ListType;

import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.pip.data.management.service.schemavalidation.nonstrategic.NonStrategicListTestConstants.FUTURE_JUDGMENTS_NODE;
import static uk.gov.hmcts.reform.pip.data.management.service.schemavalidation.nonstrategic.NonStrategicListTestConstants.HEARING_LIST_NODE;
import static uk.gov.hmcts.reform.pip.data.management.service.schemavalidation.nonstrategic.NonStrategicListTestConstants.LONDON_ADMINISTRATIVE_COURT_NODE;
import static uk.gov.hmcts.reform.pip.data.management.service.schemavalidation.nonstrategic.NonStrategicListTestConstants.PLANNING_COURT_NODE;
import static uk.gov.hmcts.reform.pip.data.management.service.schemavalidation.nonstrategic.NonStrategicListTestConstants.VENUE;

public final class VenueValidationData extends AbstractSchemaValidationTestDataProvider {
    @Override
    protected Map<ListType, String> getListTypeJsonFile() {
        return Map.ofEntries(
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
            ListTypeEntries.CST_WEEKLY_HEARING_LIST_ENTRY,
            ListTypeEntries.FAMILY_DIVISION_HIGH_COURT_DAILY_CAUSE_LIST_ENTRY,
            ListTypeEntries.FINANCIAL_LIST_CHD_KB_DAILY_CAUSE_LIST_ENTRY,
            ListTypeEntries.GRC_WEEKLY_HEARING_LIST_ENTRY,
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
            ListTypeEntries.PHT_WEEKLY_HEARING_LIST_ENTRY,
            ListTypeEntries.PROPERTY_TRUSTS_PROBATE_LIST_CHD_DAILY_CAUSE_LIST_ENTRY,
            ListTypeEntries.RPT_EASTERN_WEEKLY_HEARING_LIST_ENTRY,
            ListTypeEntries.RPT_LONDON_WEEKLY_HEARING_LIST_ENTRY,
            ListTypeEntries.RPT_MIDLANDS_WEEKLY_HEARING_LIST_ENTRY,
            ListTypeEntries.RPT_NORTHERN_WEEKLY_HEARING_LIST_ENTRY,
            ListTypeEntries.RPT_SOUTHERN_WEEKLY_HEARING_LIST_ENTRY,
            ListTypeEntries.SEND_DAILY_HEARING_LIST_ENTRY,
            ListTypeEntries.SENIOR_COURTS_COSTS_OFFICE_DAILY_CAUSE_LIST_ENTRY,
            ListTypeEntries.SSCS_LONDON_DAILY_HEARING_LIST_ENTRY,
            ListTypeEntries.SSCS_MIDLANDS_DAILY_HEARING_LIST_ENTRY,
            ListTypeEntries.SSCS_NORTH_EAST_DAILY_HEARING_LIST_ENTRY,
            ListTypeEntries.SSCS_NORTH_WEST_DAILY_HEARING_LIST_ENTRY,
            ListTypeEntries.SSCS_SCOTLAND_DAILY_HEARING_LIST_ENTRY,
            ListTypeEntries.SSCS_SOUTH_EAST_DAILY_HEARING_LIST_ENTRY,
            ListTypeEntries.SSCS_WALES_AND_SOUTH_WEST_DAILY_HEARING_LIST_ENTRY,
            ListTypeEntries.TECHNOLOGY_AND_CONSTRUCTION_COURT_KB_DAILY_CAUSE_LIST_ENTRY,
            ListTypeEntries.UT_IAC_JR_BIRMINGHAM_DAILY_HEARING_LIST_ENTRY,
            ListTypeEntries.UT_IAC_JR_CARDIFF_DAILY_HEARING_LIST_ENTRY,
            ListTypeEntries.UT_IAC_JR_LEEDS_DAILY_HEARING_LIST_ENTRY,
            ListTypeEntries.UT_IAC_JR_MANCHESTER_DAILY_HEARING_LIST_ENTRY,
            ListTypeEntries.UT_LC_DAILY_HEARING_LIST_ENTRY,
            ListTypeEntries.UT_T_AND_CC_DAILY_HEARING_LIST_ENTRY,
            ListTypeEntries.WPAFCC_WEEKLY_HEARING_LIST_ENTRY
        );
    }

    @Override
    protected Map<ListType, List<String>> getListTypeJsonFileParentNodes() {
        return Map.of(
            ListType.COURT_OF_APPEAL_CIVIL_DAILY_CAUSE_LIST, List.of(HEARING_LIST_NODE, FUTURE_JUDGMENTS_NODE),
            ListType.LONDON_ADMINISTRATIVE_COURT_DAILY_CAUSE_LIST,
            List.of(LONDON_ADMINISTRATIVE_COURT_NODE, PLANNING_COURT_NODE),
            ListType.INTERIM_APPLICATIONS_CHD_DAILY_CAUSE_LIST, List.of(HEARING_LIST_NODE)
        );
    }

    @Override
    protected String getAttributeToValidate() {
        return VENUE;
    }
}
