package uk.gov.hmcts.reform.pip.data.management.service.schemavalidation.nonstrategic.Configurations;

import org.junit.jupiter.params.provider.Arguments;
import uk.gov.hmcts.reform.pip.model.publication.ListType;

import java.util.stream.Stream;

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
import static uk.gov.hmcts.reform.pip.data.management.service.schemavalidation.nonstrategic.NonStrategicListTestConstants.CST_WEEKLY_HEARING_LIST_JSON_FILE_PATH;
import static uk.gov.hmcts.reform.pip.data.management.service.schemavalidation.nonstrategic.NonStrategicListTestConstants.FAMILY_DIVISION_HIGH_COURT_DAILY_CAUSE_LIST_JSON_FILE_PATH;
import static uk.gov.hmcts.reform.pip.data.management.service.schemavalidation.nonstrategic.NonStrategicListTestConstants.FINANCIAL_LISTS_CHD_DAILY_CAUSE_LIST_JSON_FILE_PATH;
import static uk.gov.hmcts.reform.pip.data.management.service.schemavalidation.nonstrategic.NonStrategicListTestConstants.FUTURE_JUDGMENTS_NODE;
import static uk.gov.hmcts.reform.pip.data.management.service.schemavalidation.nonstrategic.NonStrategicListTestConstants.GRC_WEEKLY_HEARING_LIST_JSON_FILE_PATH;
import static uk.gov.hmcts.reform.pip.data.management.service.schemavalidation.nonstrategic.NonStrategicListTestConstants.HEARING_LIST_NODE;
import static uk.gov.hmcts.reform.pip.data.management.service.schemavalidation.nonstrategic.NonStrategicListTestConstants.INSOLVENCY_AND_COMPANIES_COURT_CHD_DAILY_CAUSE_LIST_JSON_FILE_PATH;
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
import static uk.gov.hmcts.reform.pip.data.management.service.schemavalidation.nonstrategic.NonStrategicListTestConstants.PHT_WEEKLY_HEARING_LIST_JSON_FILE_PATH;
import static uk.gov.hmcts.reform.pip.data.management.service.schemavalidation.nonstrategic.NonStrategicListTestConstants.PLANNING_COURT_NODE;
import static uk.gov.hmcts.reform.pip.data.management.service.schemavalidation.nonstrategic.NonStrategicListTestConstants.PROPERTY_TRUSTS_PROBATE_LIST_CHD_DAILY_CAUSE_LIST_JSON_FILE_PATH;
import static uk.gov.hmcts.reform.pip.data.management.service.schemavalidation.nonstrategic.NonStrategicListTestConstants.RPT_WEEKLY_HEARING_LIST_JSON_FILE_PATH;
import static uk.gov.hmcts.reform.pip.data.management.service.schemavalidation.nonstrategic.NonStrategicListTestConstants.SEND_DAILY_HEARING_LIST_JSON_FILE_PATH;
import static uk.gov.hmcts.reform.pip.data.management.service.schemavalidation.nonstrategic.NonStrategicListTestConstants.SENIOR_COURTS_COSTS_OFFICE_DAILY_CAUSE_LIST_JSON_FILE_PATH;
import static uk.gov.hmcts.reform.pip.data.management.service.schemavalidation.nonstrategic.NonStrategicListTestConstants.SSCS_DAILY_HEARING_LIST_JSON_FILE_PATH;
import static uk.gov.hmcts.reform.pip.data.management.service.schemavalidation.nonstrategic.NonStrategicListTestConstants.TECHNOLOGY_AND_CONSTRUCTION_COURT_KB_DAILY_CAUSE_LIST_JSON_FILE_PATH;
import static uk.gov.hmcts.reform.pip.data.management.service.schemavalidation.nonstrategic.NonStrategicListTestConstants.UT_IAC_JUDICIAL_REVIEWS_DAILY_HEARING_LIST_JSON_FILE_PATH;
import static uk.gov.hmcts.reform.pip.data.management.service.schemavalidation.nonstrategic.NonStrategicListTestConstants.UT_LANDS_CHAMBER_DAILY_HEARING_LIST_JSON_FILE_PATH;
import static uk.gov.hmcts.reform.pip.data.management.service.schemavalidation.nonstrategic.NonStrategicListTestConstants.UT_TAX_CHAMBER_DAILY_HEARING_LIST_JSON_FILE_PATH;
import static uk.gov.hmcts.reform.pip.data.management.service.schemavalidation.nonstrategic.NonStrategicListTestConstants.VENUE;
import static uk.gov.hmcts.reform.pip.data.management.service.schemavalidation.nonstrategic.NonStrategicListTestConstants.WPAFCC_WEEKLY_HEARING_LIST_JSON_FILE_PATH;

public class VenueTestAttribute {
    public static Stream<Arguments> venueMandatoryAttribute() {
        return Stream.of(
            Arguments.of(new ListTypeTest(
                ListType.ADMIRALTY_COURT_KB_DAILY_CAUSE_LIST,
                ADMIRALTY_COURT_KB_DAILY_CAUSE_LIST_JSON_FILE_PATH,
                VENUE)),
            Arguments.of(new ListTypeTest(
                ListType.BIRMINGHAM_ADMINISTRATIVE_COURT_DAILY_CAUSE_LIST,
                ADMINISTRATIVE_COURT_DAILY_CAUSE_LIST_JSON_FILE_PATH,
                VENUE)),
            Arguments.of(new ListTypeTest(
                ListType.BRISTOL_AND_CARDIFF_ADMINISTRATIVE_COURT_DAILY_CAUSE_LIST,
                ADMINISTRATIVE_COURT_DAILY_CAUSE_LIST_JSON_FILE_PATH,
                VENUE)),
            Arguments.of(new ListTypeTest(
                ListType.BUSINESS_LIST_CHD_DAILY_CAUSE_LIST,
                BUSINESS_LIST_CHD_DAILY_CAUSE_LIST_JSON_FILE_PATH,
                VENUE)),
            Arguments.of(new ListTypeTest(
                ListType.CHANCERY_APPEALS_CHD_DAILY_CAUSE_LIST,
                CHANCERY_APPEALS_CHD_DAILY_CAUSE_LIST_JSON_FILE_PATH,
                VENUE)),
            Arguments.of(new ListTypeTest(
                ListType.CIVIL_COURTS_RCJ_DAILY_CAUSE_LIST,
                CIVIL_COURTS_RCJ_DAILY_CAUSE_LIST_JSON_FILE_PATH,
                VENUE)),
            Arguments.of(new ListTypeTest(
                ListType.COMMERCIAL_COURT_KB_DAILY_CAUSE_LIST,
                COMMERCIAL_COURT_KB_DAILY_CAUSE_LIST_JSON_FILE_PATH,
                VENUE)),
            Arguments.of(new ListTypeTest(
                ListType.COMPANIES_WINDING_UP_CHD_DAILY_CAUSE_LIST,
                COMPANIES_WINDING_UP_CHD_DAILY_CAUSE_LIST_JSON_FILE_PATH,
                VENUE)),
            Arguments.of(new ListTypeTest(
                ListType.COMPETITION_LIST_CHD_DAILY_CAUSE_LIST,
                COMPETITION_LIST_CHD_DAILY_CAUSE_LIST_JSON_FILE_PATH,
                VENUE)),
            Arguments.of(new ListTypeTest(
                ListType.COUNTY_COURT_LONDON_CIVIL_DAILY_CAUSE_LIST,
                COUNTY_COURT_LONDON_CIVIL_DAILY_CAUSE_LIST_JSON_FILE_PATH,
                VENUE)),
            Arguments.of(new ListTypeTest(
                ListType.COURT_OF_APPEAL_CIVIL_DAILY_CAUSE_LIST,
                COURT_OF_APPEAL_CIVIL_DAILY_CAUSE_LIST_JSON_FILE_PATH,
                VENUE, HEARING_LIST_NODE)),
            Arguments.of(new ListTypeTest(
                ListType.COURT_OF_APPEAL_CIVIL_DAILY_CAUSE_LIST,
                COURT_OF_APPEAL_CIVIL_DAILY_CAUSE_LIST_JSON_FILE_PATH,
                VENUE, FUTURE_JUDGMENTS_NODE)),
            Arguments.of(new ListTypeTest(
                ListType.COURT_OF_APPEAL_CRIMINAL_DAILY_CAUSE_LIST,
                COURT_OF_APPEAL_CRIMINAL_DAILY_CAUSE_LIST_JSON_FILE_PATH,
                VENUE)),
            Arguments.of(new ListTypeTest(
                ListType.CST_WEEKLY_HEARING_LIST,
                CST_WEEKLY_HEARING_LIST_JSON_FILE_PATH,
                VENUE)),
            Arguments.of(new ListTypeTest(
                ListType.FAMILY_DIVISION_HIGH_COURT_DAILY_CAUSE_LIST,
                FAMILY_DIVISION_HIGH_COURT_DAILY_CAUSE_LIST_JSON_FILE_PATH,
                VENUE)),
            Arguments.of(new ListTypeTest(
                ListType.FINANCIAL_LIST_CHD_KB_DAILY_CAUSE_LIST,
                FINANCIAL_LISTS_CHD_DAILY_CAUSE_LIST_JSON_FILE_PATH,
                VENUE)),
            Arguments.of(new ListTypeTest(
                ListType.GRC_WEEKLY_HEARING_LIST,
                GRC_WEEKLY_HEARING_LIST_JSON_FILE_PATH,
                VENUE)),
            Arguments.of(new ListTypeTest(
                ListType.INSOLVENCY_AND_COMPANIES_COURT_CHD_DAILY_CAUSE_LIST,
                INSOLVENCY_AND_COMPANIES_COURT_CHD_DAILY_CAUSE_LIST_JSON_FILE_PATH,
                VENUE)),
            Arguments.of(new ListTypeTest(
                ListType.INTELLECTUAL_PROPERTY_AND_ENTERPRISE_COURT_DAILY_CAUSE_LIST,
                INTELLECTUAL_PROPERTY_AND_ENTERPRISE_COURT_DAILY_CAUSE_LIST_JSON_FILE_PATH,
                VENUE)),
            Arguments.of(new ListTypeTest(
                ListType.INTELLECTUAL_PROPERTY_LIST_CHD_DAILY_CAUSE_LIST,
                INTELLECTUAL_PROPERTY_LIST_CHD_DAILY_CAUSE_LIST_JSON_FILE_PATH,
                VENUE)),
            Arguments.of(new ListTypeTest(
                ListType.INTERIM_APPLICATIONS_CHD_DAILY_CAUSE_LIST,
                INTERIM_APPLICATION_CHANCERY_LIST_CHD_DAILY_CAUSE_LIST_JSON_FILE_PATH,
                VENUE, HEARING_LIST_NODE)),
            Arguments.of(new ListTypeTest(
                ListType.KINGS_BENCH_DIVISION_DAILY_CAUSE_LIST,
                KINGS_BENCH_DIVISION_DAILY_CAUSE_LIST_JSON_FILE_PATH,
                VENUE)),
            Arguments.of(new ListTypeTest(
                ListType.KINGS_BENCH_MASTERS_DAILY_CAUSE_LIST,
                KINGS_BENCH_MASTERS_DAILY_CAUSE_LIST_JSON_FILE_PATH,
                VENUE)),
            Arguments.of(new ListTypeTest(
                ListType.LEEDS_ADMINISTRATIVE_COURT_DAILY_CAUSE_LIST,
                ADMINISTRATIVE_COURT_DAILY_CAUSE_LIST_JSON_FILE_PATH,
                VENUE)),
            Arguments.of(new ListTypeTest(
                ListType.LONDON_ADMINISTRATIVE_COURT_DAILY_CAUSE_LIST,
                LONDON_ADMINISTRATIVE_COURT_DAILY_CAUSE_LIST_JSON_FILE_PATH,
                VENUE, LONDON_ADMINISTRATIVE_COURT_NODE)),
            Arguments.of(new ListTypeTest(
                ListType.LONDON_ADMINISTRATIVE_COURT_DAILY_CAUSE_LIST,
                LONDON_ADMINISTRATIVE_COURT_DAILY_CAUSE_LIST_JSON_FILE_PATH,
                VENUE, PLANNING_COURT_NODE)),
            Arguments.of(new ListTypeTest(
                ListType.MANCHESTER_ADMINISTRATIVE_COURT_DAILY_CAUSE_LIST,
                ADMINISTRATIVE_COURT_DAILY_CAUSE_LIST_JSON_FILE_PATH,
                VENUE)),
            Arguments.of(new ListTypeTest(
                ListType.LONDON_CIRCUIT_COMMERCIAL_COURT_KB_DAILY_CAUSE_LIST,
                LONDON_CIRCUIT_COURT_DAILY_CAUSE_LIST_JSON_FILE_PATH,
                VENUE)),
            Arguments.of(new ListTypeTest(
                ListType.MAYOR_AND_CITY_CIVIL_DAILY_CAUSE_LIST,
                MAYOR_AND_CITY_CIVIL_DAILY_CAUSE_LIST_JSON_FILE_PATH,
                VENUE)),
            Arguments.of(new ListTypeTest(
                ListType.PATENTS_COURT_CHD_DAILY_CAUSE_LIST,
                PATENTS_COURT_CHD_DAILY_CAUSE_LIST_JSON_FILE_PATH,
                VENUE)),
            Arguments.of(new ListTypeTest(
                ListType.PENSIONS_LIST_CHD_DAILY_CAUSE_LIST,
                PENSIONS_LIST_CHD_DAILY_CAUSE_LIST_JSON_FILE_PATH,
                VENUE)),
            Arguments.of(new ListTypeTest(
                ListType.PHT_WEEKLY_HEARING_LIST,
                PHT_WEEKLY_HEARING_LIST_JSON_FILE_PATH,
                VENUE)),
            Arguments.of(new ListTypeTest(
                ListType.PROPERTY_TRUSTS_PROBATE_LIST_CHD_DAILY_CAUSE_LIST,
                PROPERTY_TRUSTS_PROBATE_LIST_CHD_DAILY_CAUSE_LIST_JSON_FILE_PATH,
                VENUE)),
            Arguments.of(new ListTypeTest(
                ListType.RPT_EASTERN_WEEKLY_HEARING_LIST,
                RPT_WEEKLY_HEARING_LIST_JSON_FILE_PATH,
                VENUE)),
            Arguments.of(new ListTypeTest(
                ListType.RPT_LONDON_WEEKLY_HEARING_LIST,
                RPT_WEEKLY_HEARING_LIST_JSON_FILE_PATH,
                VENUE)),
            Arguments.of(new ListTypeTest(
                ListType.RPT_MIDLANDS_WEEKLY_HEARING_LIST,
                RPT_WEEKLY_HEARING_LIST_JSON_FILE_PATH,
                VENUE)),
            Arguments.of(new ListTypeTest(
                ListType.RPT_NORTHERN_WEEKLY_HEARING_LIST,
                RPT_WEEKLY_HEARING_LIST_JSON_FILE_PATH,
                VENUE)),
            Arguments.of(new ListTypeTest(
                ListType.RPT_SOUTHERN_WEEKLY_HEARING_LIST,
                RPT_WEEKLY_HEARING_LIST_JSON_FILE_PATH,
                VENUE)),
            Arguments.of(new ListTypeTest(
                ListType.SEND_DAILY_HEARING_LIST,
                SEND_DAILY_HEARING_LIST_JSON_FILE_PATH,
                VENUE)),
            Arguments.of(new ListTypeTest(
                ListType.SENIOR_COURTS_COSTS_OFFICE_DAILY_CAUSE_LIST,
                SENIOR_COURTS_COSTS_OFFICE_DAILY_CAUSE_LIST_JSON_FILE_PATH,
                VENUE)),
            Arguments.of(new ListTypeTest(
                ListType.SSCS_LONDON_DAILY_HEARING_LIST,
                SSCS_DAILY_HEARING_LIST_JSON_FILE_PATH,
                VENUE)),
            Arguments.of(new ListTypeTest(
                ListType.SSCS_MIDLANDS_DAILY_HEARING_LIST,
                SSCS_DAILY_HEARING_LIST_JSON_FILE_PATH,
                VENUE)),
            Arguments.of(new ListTypeTest(
                ListType.SSCS_NORTH_EAST_DAILY_HEARING_LIST,
                SSCS_DAILY_HEARING_LIST_JSON_FILE_PATH,
                VENUE)),
            Arguments.of(new ListTypeTest(
                ListType.SSCS_NORTH_WEST_DAILY_HEARING_LIST,
                SSCS_DAILY_HEARING_LIST_JSON_FILE_PATH,
                VENUE)),
            Arguments.of(new ListTypeTest(
                ListType.SSCS_SCOTLAND_DAILY_HEARING_LIST,
                SSCS_DAILY_HEARING_LIST_JSON_FILE_PATH,
                VENUE)),
            Arguments.of(new ListTypeTest(
                ListType.SSCS_SOUTH_EAST_DAILY_HEARING_LIST,
                SSCS_DAILY_HEARING_LIST_JSON_FILE_PATH,
                VENUE)),
            Arguments.of(new ListTypeTest(
                ListType.SSCS_WALES_AND_SOUTH_WEST_DAILY_HEARING_LIST,
                SSCS_DAILY_HEARING_LIST_JSON_FILE_PATH,
                VENUE)),
            Arguments.of(new ListTypeTest(
                ListType.TECHNOLOGY_AND_CONSTRUCTION_COURT_KB_DAILY_CAUSE_LIST,
                TECHNOLOGY_AND_CONSTRUCTION_COURT_KB_DAILY_CAUSE_LIST_JSON_FILE_PATH,
                VENUE)),
            Arguments.of(new ListTypeTest(
                ListType.UT_IAC_JR_BIRMINGHAM_DAILY_HEARING_LIST,
                UT_IAC_JUDICIAL_REVIEWS_DAILY_HEARING_LIST_JSON_FILE_PATH,
                VENUE)),
            Arguments.of(new ListTypeTest(
                ListType.UT_IAC_JR_CARDIFF_DAILY_HEARING_LIST,
                UT_IAC_JUDICIAL_REVIEWS_DAILY_HEARING_LIST_JSON_FILE_PATH,
                VENUE)),
            Arguments.of(new ListTypeTest(
                ListType.UT_IAC_JR_LEEDS_DAILY_HEARING_LIST,
                UT_IAC_JUDICIAL_REVIEWS_DAILY_HEARING_LIST_JSON_FILE_PATH,
                VENUE)),
            Arguments.of(new ListTypeTest(
                ListType.UT_IAC_JR_MANCHESTER_DAILY_HEARING_LIST,
                UT_IAC_JUDICIAL_REVIEWS_DAILY_HEARING_LIST_JSON_FILE_PATH,
                VENUE)),
            Arguments.of(new ListTypeTest(
                ListType.UT_LC_DAILY_HEARING_LIST,
                UT_LANDS_CHAMBER_DAILY_HEARING_LIST_JSON_FILE_PATH,
                VENUE)),
            Arguments.of(new ListTypeTest(
                ListType.UT_T_AND_CC_DAILY_HEARING_LIST,
                UT_TAX_CHAMBER_DAILY_HEARING_LIST_JSON_FILE_PATH,
                VENUE)),
            Arguments.of(new ListTypeTest(
                ListType.WPAFCC_WEEKLY_HEARING_LIST,
                WPAFCC_WEEKLY_HEARING_LIST_JSON_FILE_PATH,
                VENUE))
        );
    }
}
