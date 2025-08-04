package uk.gov.hmcts.reform.pip.data.management.service.schemavalidation.nonstrategic.configurations;

import uk.gov.hmcts.reform.pip.model.publication.ListType;

import java.util.List;
import java.util.Map;

import static java.util.Map.entry;
import static uk.gov.hmcts.reform.pip.data.management.service.schemavalidation.nonstrategic.NonStrategicListTestConstants.ADMINISTRATIVE_COURT_DAILY_CAUSE_LIST_JSON_FILE_PATH;
import static uk.gov.hmcts.reform.pip.data.management.service.schemavalidation.nonstrategic.NonStrategicListTestConstants.ADMIRALTY_COURT_KB_DAILY_CAUSE_LIST_JSON_FILE_PATH;
import static uk.gov.hmcts.reform.pip.data.management.service.schemavalidation.nonstrategic.NonStrategicListTestConstants.BUSINESS_LIST_CHD_DAILY_CAUSE_LIST_JSON_FILE_PATH;
import static uk.gov.hmcts.reform.pip.data.management.service.schemavalidation.nonstrategic.NonStrategicListTestConstants.CHANCERY_APPEALS_CHD_DAILY_CAUSE_LIST_JSON_FILE_PATH;
import static uk.gov.hmcts.reform.pip.data.management.service.schemavalidation.nonstrategic.NonStrategicListTestConstants.CIVIL_COURTS_RCJ_DAILY_CAUSE_LIST_JSON_FILE_PATH;
import static uk.gov.hmcts.reform.pip.data.management.service.schemavalidation.nonstrategic.NonStrategicListTestConstants.COMMERCIAL_COURT_KB_DAILY_CAUSE_LIST_JSON_FILE_PATH;
import static uk.gov.hmcts.reform.pip.data.management.service.schemavalidation.nonstrategic.NonStrategicListTestConstants.COMPANIES_WINDING_UP_CHD_DAILY_CAUSE_LIST_JSON_FILE_PATH;
import static uk.gov.hmcts.reform.pip.data.management.service.schemavalidation.nonstrategic.NonStrategicListTestConstants.COMPETITION_LIST_CHD_DAILY_CAUSE_LIST_JSON_FILE_PATH;
import static uk.gov.hmcts.reform.pip.data.management.service.schemavalidation.nonstrategic.NonStrategicListTestConstants.COUNTY_COURT_LONDON_CIVIL_DAILY_CAUSE_LIST_JSON_FILE_PATH;
import static uk.gov.hmcts.reform.pip.data.management.service.schemavalidation.nonstrategic.NonStrategicListTestConstants.COURT_OF_APPEAL_CIVIL_DAILY_CAUSE_LIST_JSON_FILE_PATH;
import static uk.gov.hmcts.reform.pip.data.management.service.schemavalidation.nonstrategic.NonStrategicListTestConstants.COURT_OF_APPEAL_CRIMINAL_DAILY_CAUSE_LIST_JSON_FILE_PATH;
import static uk.gov.hmcts.reform.pip.data.management.service.schemavalidation.nonstrategic.NonStrategicListTestConstants.FAMILY_DIVISION_HIGH_COURT_DAILY_CAUSE_LIST_JSON_FILE_PATH;
import static uk.gov.hmcts.reform.pip.data.management.service.schemavalidation.nonstrategic.NonStrategicListTestConstants.FINANCIAL_LISTS_CHD_DAILY_CAUSE_LIST_JSON_FILE_PATH;
import static uk.gov.hmcts.reform.pip.data.management.service.schemavalidation.nonstrategic.NonStrategicListTestConstants.FUTURE_JUDGMENTS_NODE;
import static uk.gov.hmcts.reform.pip.data.management.service.schemavalidation.nonstrategic.NonStrategicListTestConstants.HEARING_LIST_NODE;
import static uk.gov.hmcts.reform.pip.data.management.service.schemavalidation.nonstrategic.NonStrategicListTestConstants.INTELLECTUAL_PROPERTY_AND_ENTERPRISE_COURT_DAILY_CAUSE_LIST_JSON_FILE_PATH;
import static uk.gov.hmcts.reform.pip.data.management.service.schemavalidation.nonstrategic.NonStrategicListTestConstants.INTELLECTUAL_PROPERTY_LIST_CHD_DAILY_CAUSE_LIST_JSON_FILE_PATH;
import static uk.gov.hmcts.reform.pip.data.management.service.schemavalidation.nonstrategic.NonStrategicListTestConstants.INTERIM_APPLICATION_CHANCERY_LIST_CHD_DAILY_CAUSE_LIST_JSON_FILE_PATH;
import static uk.gov.hmcts.reform.pip.data.management.service.schemavalidation.nonstrategic.NonStrategicListTestConstants.KINGS_BENCH_DIVISION_DAILY_CAUSE_LIST_JSON_FILE_PATH;
import static uk.gov.hmcts.reform.pip.data.management.service.schemavalidation.nonstrategic.NonStrategicListTestConstants.KINGS_BENCH_MASTERS_DAILY_CAUSE_LIST_JSON_FILE_PATH;
import static uk.gov.hmcts.reform.pip.data.management.service.schemavalidation.nonstrategic.NonStrategicListTestConstants.LONDON_ADMINISTRATIVE_COURT_DAILY_CAUSE_LIST_JSON_FILE_PATH;
import static uk.gov.hmcts.reform.pip.data.management.service.schemavalidation.nonstrategic.NonStrategicListTestConstants.LONDON_ADMINISTRATIVE_COURT_NODE;
import static uk.gov.hmcts.reform.pip.data.management.service.schemavalidation.nonstrategic.NonStrategicListTestConstants.LONDON_CIRCUIT_COURT_DAILY_CAUSE_LIST_JSON_FILE_PATH;
import static uk.gov.hmcts.reform.pip.data.management.service.schemavalidation.nonstrategic.NonStrategicListTestConstants.MAYOR_AND_CITY_CIVIL_DAILY_CAUSE_LIST_JSON_FILE_PATH;
import static uk.gov.hmcts.reform.pip.data.management.service.schemavalidation.nonstrategic.NonStrategicListTestConstants.PATENTS_COURT_CHD_DAILY_CAUSE_LIST_JSON_FILE_PATH;
import static uk.gov.hmcts.reform.pip.data.management.service.schemavalidation.nonstrategic.NonStrategicListTestConstants.PENSIONS_LIST_CHD_DAILY_CAUSE_LIST_JSON_FILE_PATH;
import static uk.gov.hmcts.reform.pip.data.management.service.schemavalidation.nonstrategic.NonStrategicListTestConstants.PLANNING_COURT_NODE;
import static uk.gov.hmcts.reform.pip.data.management.service.schemavalidation.nonstrategic.NonStrategicListTestConstants.PROPERTY_TRUSTS_PROBATE_LIST_CHD_DAILY_CAUSE_LIST_JSON_FILE_PATH;
import static uk.gov.hmcts.reform.pip.data.management.service.schemavalidation.nonstrategic.NonStrategicListTestConstants.RPT_WEEKLY_HEARING_LIST_JSON_FILE_PATH;
import static uk.gov.hmcts.reform.pip.data.management.service.schemavalidation.nonstrategic.NonStrategicListTestConstants.SEND_DAILY_HEARING_LIST_JSON_FILE_PATH;
import static uk.gov.hmcts.reform.pip.data.management.service.schemavalidation.nonstrategic.NonStrategicListTestConstants.SENIOR_COURTS_COSTS_OFFICE_DAILY_CAUSE_LIST_JSON_FILE_PATH;
import static uk.gov.hmcts.reform.pip.data.management.service.schemavalidation.nonstrategic.NonStrategicListTestConstants.SIAC_WEEKLY_HEARING_LIST_JSON_FILE_PATH;
import static uk.gov.hmcts.reform.pip.data.management.service.schemavalidation.nonstrategic.NonStrategicListTestConstants.TECHNOLOGY_AND_CONSTRUCTION_COURT_KB_DAILY_CAUSE_LIST_JSON_FILE_PATH;
import static uk.gov.hmcts.reform.pip.data.management.service.schemavalidation.nonstrategic.NonStrategicListTestConstants.TIME;
import static uk.gov.hmcts.reform.pip.data.management.service.schemavalidation.nonstrategic.NonStrategicListTestConstants.UT_ADMINISTRATIVE_APPEALS_CHAMBER_DAILY_HEARING_LIST_JSON_FILE_PATH;
import static uk.gov.hmcts.reform.pip.data.management.service.schemavalidation.nonstrategic.NonStrategicListTestConstants.UT_LANDS_CHAMBER_DAILY_HEARING_LIST_JSON_FILE_PATH;
import static uk.gov.hmcts.reform.pip.data.management.service.schemavalidation.nonstrategic.NonStrategicListTestConstants.UT_TAX_CHAMBER_DAILY_HEARING_LIST_JSON_FILE_PATH;

@SuppressWarnings("PMD.ExcessiveImports")
public final class TimeFormatValidation {
    private static final Map<ListType, String> LIST_TYPE_JSON_FILE = Map.ofEntries(
        entry(ListType.ADMIRALTY_COURT_KB_DAILY_CAUSE_LIST, ADMIRALTY_COURT_KB_DAILY_CAUSE_LIST_JSON_FILE_PATH),
        entry(ListType.BUSINESS_LIST_CHD_DAILY_CAUSE_LIST, BUSINESS_LIST_CHD_DAILY_CAUSE_LIST_JSON_FILE_PATH),
        entry(ListType.CHANCERY_APPEALS_CHD_DAILY_CAUSE_LIST, CHANCERY_APPEALS_CHD_DAILY_CAUSE_LIST_JSON_FILE_PATH),
        entry(ListType.CIVIL_COURTS_RCJ_DAILY_CAUSE_LIST, CIVIL_COURTS_RCJ_DAILY_CAUSE_LIST_JSON_FILE_PATH),
        entry(ListType.COMMERCIAL_COURT_KB_DAILY_CAUSE_LIST, COMMERCIAL_COURT_KB_DAILY_CAUSE_LIST_JSON_FILE_PATH),
        entry(ListType.COMPANIES_WINDING_UP_CHD_DAILY_CAUSE_LIST,
              COMPANIES_WINDING_UP_CHD_DAILY_CAUSE_LIST_JSON_FILE_PATH),
        entry(ListType.COMPETITION_LIST_CHD_DAILY_CAUSE_LIST, COMPETITION_LIST_CHD_DAILY_CAUSE_LIST_JSON_FILE_PATH),
        entry(ListType.COUNTY_COURT_LONDON_CIVIL_DAILY_CAUSE_LIST,
              COUNTY_COURT_LONDON_CIVIL_DAILY_CAUSE_LIST_JSON_FILE_PATH),
        entry(ListType.COURT_OF_APPEAL_CIVIL_DAILY_CAUSE_LIST, COURT_OF_APPEAL_CIVIL_DAILY_CAUSE_LIST_JSON_FILE_PATH),
        entry(ListType.COURT_OF_APPEAL_CRIMINAL_DAILY_CAUSE_LIST,
              COURT_OF_APPEAL_CRIMINAL_DAILY_CAUSE_LIST_JSON_FILE_PATH),
        entry(ListType.FAMILY_DIVISION_HIGH_COURT_DAILY_CAUSE_LIST,
              FAMILY_DIVISION_HIGH_COURT_DAILY_CAUSE_LIST_JSON_FILE_PATH),
        entry(ListType.FINANCIAL_LIST_CHD_KB_DAILY_CAUSE_LIST, FINANCIAL_LISTS_CHD_DAILY_CAUSE_LIST_JSON_FILE_PATH),
        entry(ListType.INTELLECTUAL_PROPERTY_AND_ENTERPRISE_COURT_DAILY_CAUSE_LIST,
              INTELLECTUAL_PROPERTY_AND_ENTERPRISE_COURT_DAILY_CAUSE_LIST_JSON_FILE_PATH),
        entry(ListType.INTELLECTUAL_PROPERTY_LIST_CHD_DAILY_CAUSE_LIST,
              INTELLECTUAL_PROPERTY_LIST_CHD_DAILY_CAUSE_LIST_JSON_FILE_PATH),
        entry(ListType.INTERIM_APPLICATIONS_CHD_DAILY_CAUSE_LIST,
              INTERIM_APPLICATION_CHANCERY_LIST_CHD_DAILY_CAUSE_LIST_JSON_FILE_PATH),
        entry(ListType.KINGS_BENCH_DIVISION_DAILY_CAUSE_LIST, KINGS_BENCH_DIVISION_DAILY_CAUSE_LIST_JSON_FILE_PATH),
        entry(ListType.KINGS_BENCH_MASTERS_DAILY_CAUSE_LIST, KINGS_BENCH_MASTERS_DAILY_CAUSE_LIST_JSON_FILE_PATH),
        entry(ListType.LONDON_CIRCUIT_COMMERCIAL_COURT_KB_DAILY_CAUSE_LIST,
              LONDON_CIRCUIT_COURT_DAILY_CAUSE_LIST_JSON_FILE_PATH),
        entry(ListType.MAYOR_AND_CITY_CIVIL_DAILY_CAUSE_LIST, MAYOR_AND_CITY_CIVIL_DAILY_CAUSE_LIST_JSON_FILE_PATH),
        entry(ListType.PATENTS_COURT_CHD_DAILY_CAUSE_LIST, PATENTS_COURT_CHD_DAILY_CAUSE_LIST_JSON_FILE_PATH),
        entry(ListType.PENSIONS_LIST_CHD_DAILY_CAUSE_LIST, PENSIONS_LIST_CHD_DAILY_CAUSE_LIST_JSON_FILE_PATH),
        entry(ListType.PROPERTY_TRUSTS_PROBATE_LIST_CHD_DAILY_CAUSE_LIST,
              PROPERTY_TRUSTS_PROBATE_LIST_CHD_DAILY_CAUSE_LIST_JSON_FILE_PATH),
        entry(ListType.SEND_DAILY_HEARING_LIST, SEND_DAILY_HEARING_LIST_JSON_FILE_PATH),
        entry(ListType.SENIOR_COURTS_COSTS_OFFICE_DAILY_CAUSE_LIST,
              SENIOR_COURTS_COSTS_OFFICE_DAILY_CAUSE_LIST_JSON_FILE_PATH),
        entry(ListType.TECHNOLOGY_AND_CONSTRUCTION_COURT_KB_DAILY_CAUSE_LIST,
              TECHNOLOGY_AND_CONSTRUCTION_COURT_KB_DAILY_CAUSE_LIST_JSON_FILE_PATH),
        entry(ListType.UT_AAC_DAILY_HEARING_LIST, UT_ADMINISTRATIVE_APPEALS_CHAMBER_DAILY_HEARING_LIST_JSON_FILE_PATH),
        entry(ListType.UT_LC_DAILY_HEARING_LIST, UT_LANDS_CHAMBER_DAILY_HEARING_LIST_JSON_FILE_PATH),
        entry(ListType.UT_T_AND_CC_DAILY_HEARING_LIST, UT_TAX_CHAMBER_DAILY_HEARING_LIST_JSON_FILE_PATH),

        entry(ListType.BIRMINGHAM_ADMINISTRATIVE_COURT_DAILY_CAUSE_LIST,
              ADMINISTRATIVE_COURT_DAILY_CAUSE_LIST_JSON_FILE_PATH),
        entry(ListType.BRISTOL_AND_CARDIFF_ADMINISTRATIVE_COURT_DAILY_CAUSE_LIST,
              ADMINISTRATIVE_COURT_DAILY_CAUSE_LIST_JSON_FILE_PATH),
        entry(ListType.LEEDS_ADMINISTRATIVE_COURT_DAILY_CAUSE_LIST,
              ADMINISTRATIVE_COURT_DAILY_CAUSE_LIST_JSON_FILE_PATH),
        entry(ListType.LONDON_ADMINISTRATIVE_COURT_DAILY_CAUSE_LIST,
              LONDON_ADMINISTRATIVE_COURT_DAILY_CAUSE_LIST_JSON_FILE_PATH),
        entry(ListType.MANCHESTER_ADMINISTRATIVE_COURT_DAILY_CAUSE_LIST,
              ADMINISTRATIVE_COURT_DAILY_CAUSE_LIST_JSON_FILE_PATH),

        entry(ListType.PAAC_WEEKLY_HEARING_LIST, SIAC_WEEKLY_HEARING_LIST_JSON_FILE_PATH),
        entry(ListType.SIAC_WEEKLY_HEARING_LIST, SIAC_WEEKLY_HEARING_LIST_JSON_FILE_PATH),
        entry(ListType.POAC_WEEKLY_HEARING_LIST, SIAC_WEEKLY_HEARING_LIST_JSON_FILE_PATH),

        entry(ListType.RPT_EASTERN_WEEKLY_HEARING_LIST, RPT_WEEKLY_HEARING_LIST_JSON_FILE_PATH),
        entry(ListType.RPT_LONDON_WEEKLY_HEARING_LIST, RPT_WEEKLY_HEARING_LIST_JSON_FILE_PATH),
        entry(ListType.RPT_MIDLANDS_WEEKLY_HEARING_LIST, RPT_WEEKLY_HEARING_LIST_JSON_FILE_PATH),
        entry(ListType.RPT_NORTHERN_WEEKLY_HEARING_LIST, RPT_WEEKLY_HEARING_LIST_JSON_FILE_PATH),
        entry(ListType.RPT_SOUTHERN_WEEKLY_HEARING_LIST, RPT_WEEKLY_HEARING_LIST_JSON_FILE_PATH)
    );

    private static final Map<ListType, List<String>> LIST_TYPE_JSON_FILE_PARENT_NODES = Map.of(
        ListType.COURT_OF_APPEAL_CIVIL_DAILY_CAUSE_LIST,
        List.of(HEARING_LIST_NODE, FUTURE_JUDGMENTS_NODE),
        ListType.LONDON_ADMINISTRATIVE_COURT_DAILY_CAUSE_LIST,
        List.of(LONDON_ADMINISTRATIVE_COURT_NODE, PLANNING_COURT_NODE),
        ListType.INTERIM_APPLICATIONS_CHD_DAILY_CAUSE_LIST,
        List.of(HEARING_LIST_NODE)
    );

    public static List<ListTypeTestInput> getListTypesWithTimeFormatValidation() {
        return ListTypeTestInput.generateListTypeTestInputsForAttribute(LIST_TYPE_JSON_FILE,
                LIST_TYPE_JSON_FILE_PARENT_NODES, TIME);
    }

    private TimeFormatValidation() {
    }
}
