package uk.gov.hmcts.reform.pip.data.management.service.schemavalidation.nonstrategic.configurations;

import uk.gov.hmcts.reform.pip.model.publication.ListType;

import java.util.Map;

import static uk.gov.hmcts.reform.pip.data.management.service.schemavalidation.nonstrategic.NonStrategicListTestConstants.ADMINISTRATIVE_COURT_DAILY_CAUSE_LIST_JSON_FILE_PATH;
import static uk.gov.hmcts.reform.pip.data.management.service.schemavalidation.nonstrategic.NonStrategicListTestConstants.ADMIRALTY_COURT_KB_DAILY_CAUSE_LIST_JSON_FILE_PATH;
import static uk.gov.hmcts.reform.pip.data.management.service.schemavalidation.nonstrategic.NonStrategicListTestConstants.AST_DAILY_HEARING_LIST_JSON_FILE_PATH;
import static uk.gov.hmcts.reform.pip.data.management.service.schemavalidation.nonstrategic.NonStrategicListTestConstants.BUSINESS_LIST_CHD_DAILY_CAUSE_LIST_JSON_FILE_PATH;
import static uk.gov.hmcts.reform.pip.data.management.service.schemavalidation.nonstrategic.NonStrategicListTestConstants.CHANCERY_APPEALS_CHD_DAILY_CAUSE_LIST_JSON_FILE_PATH;
import static uk.gov.hmcts.reform.pip.data.management.service.schemavalidation.nonstrategic.NonStrategicListTestConstants.CIC_WEEKLY_HEARING_LIST_JSON_FILE_PATH;
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
import static uk.gov.hmcts.reform.pip.data.management.service.schemavalidation.nonstrategic.NonStrategicListTestConstants.FTT_LR_WEEKLY_HEARING_LIST_JSON_FILE_PATH;
import static uk.gov.hmcts.reform.pip.data.management.service.schemavalidation.nonstrategic.NonStrategicListTestConstants.FTT_TAX_WEEKLY_HEARING_LIST_JSON_FILE_PATH;
import static uk.gov.hmcts.reform.pip.data.management.service.schemavalidation.nonstrategic.NonStrategicListTestConstants.GRC_WEEKLY_HEARING_LIST_JSON_FILE_PATH;
import static uk.gov.hmcts.reform.pip.data.management.service.schemavalidation.nonstrategic.NonStrategicListTestConstants.INSOLVENCY_AND_COMPANIES_COURT_CHD_DAILY_CAUSE_LIST_JSON_FILE_PATH;
import static uk.gov.hmcts.reform.pip.data.management.service.schemavalidation.nonstrategic.NonStrategicListTestConstants.INTELLECTUAL_PROPERTY_AND_ENTERPRISE_COURT_DAILY_CAUSE_LIST_JSON_FILE_PATH;
import static uk.gov.hmcts.reform.pip.data.management.service.schemavalidation.nonstrategic.NonStrategicListTestConstants.INTELLECTUAL_PROPERTY_LIST_CHD_DAILY_CAUSE_LIST_JSON_FILE_PATH;
import static uk.gov.hmcts.reform.pip.data.management.service.schemavalidation.nonstrategic.NonStrategicListTestConstants.INTERIM_APPLICATION_CHANCERY_LIST_CHD_DAILY_CAUSE_LIST_JSON_FILE_PATH;
import static uk.gov.hmcts.reform.pip.data.management.service.schemavalidation.nonstrategic.NonStrategicListTestConstants.KINGS_BENCH_DIVISION_DAILY_CAUSE_LIST_JSON_FILE_PATH;
import static uk.gov.hmcts.reform.pip.data.management.service.schemavalidation.nonstrategic.NonStrategicListTestConstants.KINGS_BENCH_MASTERS_DAILY_CAUSE_LIST_JSON_FILE_PATH;
import static uk.gov.hmcts.reform.pip.data.management.service.schemavalidation.nonstrategic.NonStrategicListTestConstants.LONDON_ADMINISTRATIVE_COURT_DAILY_CAUSE_LIST_JSON_FILE_PATH;
import static uk.gov.hmcts.reform.pip.data.management.service.schemavalidation.nonstrategic.NonStrategicListTestConstants.LONDON_CIRCUIT_COURT_DAILY_CAUSE_LIST_JSON_FILE_PATH;
import static uk.gov.hmcts.reform.pip.data.management.service.schemavalidation.nonstrategic.NonStrategicListTestConstants.MAYOR_AND_CITY_CIVIL_DAILY_CAUSE_LIST_JSON_FILE_PATH;
import static uk.gov.hmcts.reform.pip.data.management.service.schemavalidation.nonstrategic.NonStrategicListTestConstants.PATENTS_COURT_CHD_DAILY_CAUSE_LIST_JSON_FILE_PATH;
import static uk.gov.hmcts.reform.pip.data.management.service.schemavalidation.nonstrategic.NonStrategicListTestConstants.PENSIONS_LIST_CHD_DAILY_CAUSE_LIST_JSON_FILE_PATH;
import static uk.gov.hmcts.reform.pip.data.management.service.schemavalidation.nonstrategic.NonStrategicListTestConstants.PHT_WEEKLY_HEARING_LIST_JSON_FILE_PATH;
import static uk.gov.hmcts.reform.pip.data.management.service.schemavalidation.nonstrategic.NonStrategicListTestConstants.PROPERTY_TRUSTS_PROBATE_LIST_CHD_DAILY_CAUSE_LIST_JSON_FILE_PATH;
import static uk.gov.hmcts.reform.pip.data.management.service.schemavalidation.nonstrategic.NonStrategicListTestConstants.RPT_WEEKLY_HEARING_LIST_JSON_FILE_PATH;
import static uk.gov.hmcts.reform.pip.data.management.service.schemavalidation.nonstrategic.NonStrategicListTestConstants.SEND_DAILY_HEARING_LIST_JSON_FILE_PATH;
import static uk.gov.hmcts.reform.pip.data.management.service.schemavalidation.nonstrategic.NonStrategicListTestConstants.SENIOR_COURTS_COSTS_OFFICE_DAILY_CAUSE_LIST_JSON_FILE_PATH;
import static uk.gov.hmcts.reform.pip.data.management.service.schemavalidation.nonstrategic.NonStrategicListTestConstants.SIAC_WEEKLY_HEARING_LIST_JSON_FILE_PATH;
import static uk.gov.hmcts.reform.pip.data.management.service.schemavalidation.nonstrategic.NonStrategicListTestConstants.SSCS_DAILY_HEARING_LIST_JSON_FILE_PATH;
import static uk.gov.hmcts.reform.pip.data.management.service.schemavalidation.nonstrategic.NonStrategicListTestConstants.TECHNOLOGY_AND_CONSTRUCTION_COURT_KB_DAILY_CAUSE_LIST_JSON_FILE_PATH;
import static uk.gov.hmcts.reform.pip.data.management.service.schemavalidation.nonstrategic.NonStrategicListTestConstants.UT_ADMINISTRATIVE_APPEALS_CHAMBER_DAILY_HEARING_LIST_JSON_FILE_PATH;
import static uk.gov.hmcts.reform.pip.data.management.service.schemavalidation.nonstrategic.NonStrategicListTestConstants.UT_IAC_JUDICIAL_REVIEWS_DAILY_HEARING_LIST_JSON_FILE_PATH;
import static uk.gov.hmcts.reform.pip.data.management.service.schemavalidation.nonstrategic.NonStrategicListTestConstants.UT_IAC_JUDICIAL_REVIEWS_LONDON_DAILY_HEARING_LIST_JSON_FILE_PATH;
import static uk.gov.hmcts.reform.pip.data.management.service.schemavalidation.nonstrategic.NonStrategicListTestConstants.UT_IAC_STATUTORY_APPEALS_DAILY_HEARING_LIST_JSON_FILE_PATH;
import static uk.gov.hmcts.reform.pip.data.management.service.schemavalidation.nonstrategic.NonStrategicListTestConstants.UT_LANDS_CHAMBER_DAILY_HEARING_LIST_JSON_FILE_PATH;
import static uk.gov.hmcts.reform.pip.data.management.service.schemavalidation.nonstrategic.NonStrategicListTestConstants.UT_TAX_CHAMBER_DAILY_HEARING_LIST_JSON_FILE_PATH;
import static uk.gov.hmcts.reform.pip.data.management.service.schemavalidation.nonstrategic.NonStrategicListTestConstants.WPAFCC_WEEKLY_HEARING_LIST_JSON_FILE_PATH;

@SuppressWarnings("PMD.ExcessiveImports")
public final class ListTypeEntries {
    // Admiralty Court
    public static final Map.Entry<ListType, String> ADMIRALTY_COURT_KB_DAILY_CAUSE_LIST_ENTRY =
        Map.entry(ListType.ADMIRALTY_COURT_KB_DAILY_CAUSE_LIST,
                  ADMIRALTY_COURT_KB_DAILY_CAUSE_LIST_JSON_FILE_PATH);

    // AST
    public static final Map.Entry<ListType, String> AST_DAILY_HEARING_LIST_ENTRY =
        Map.entry(ListType.AST_DAILY_HEARING_LIST,
                  AST_DAILY_HEARING_LIST_JSON_FILE_PATH);

    // Administrative Courts
    public static final Map.Entry<ListType, String> BIRMINGHAM_ADMINISTRATIVE_COURT_DAILY_CAUSE_LIST_ENTRY =
        Map.entry(ListType.BIRMINGHAM_ADMINISTRATIVE_COURT_DAILY_CAUSE_LIST,
                  ADMINISTRATIVE_COURT_DAILY_CAUSE_LIST_JSON_FILE_PATH);

    public static final Map.Entry<ListType, String> BRISTOL_AND_CARDIFF_ADMINISTRATIVE_COURT_DAILY_CAUSE_LIST_ENTRY =
        Map.entry(ListType.BRISTOL_AND_CARDIFF_ADMINISTRATIVE_COURT_DAILY_CAUSE_LIST,
                  ADMINISTRATIVE_COURT_DAILY_CAUSE_LIST_JSON_FILE_PATH);

    public static final Map.Entry<ListType, String> LEEDS_ADMINISTRATIVE_COURT_DAILY_CAUSE_LIST_ENTRY =
        Map.entry(ListType.LEEDS_ADMINISTRATIVE_COURT_DAILY_CAUSE_LIST,
                  ADMINISTRATIVE_COURT_DAILY_CAUSE_LIST_JSON_FILE_PATH);

    public static final Map.Entry<ListType, String> LONDON_ADMINISTRATIVE_COURT_DAILY_CAUSE_LIST_ENTRY =
        Map.entry(ListType.LONDON_ADMINISTRATIVE_COURT_DAILY_CAUSE_LIST,
                  LONDON_ADMINISTRATIVE_COURT_DAILY_CAUSE_LIST_JSON_FILE_PATH);

    public static final Map.Entry<ListType, String> MANCHESTER_ADMINISTRATIVE_COURT_DAILY_CAUSE_LIST_ENTRY =
        Map.entry(ListType.MANCHESTER_ADMINISTRATIVE_COURT_DAILY_CAUSE_LIST,
                  ADMINISTRATIVE_COURT_DAILY_CAUSE_LIST_JSON_FILE_PATH);

    // Business/Chancery
    public static final Map.Entry<ListType, String> BUSINESS_LIST_CHD_DAILY_CAUSE_LIST_ENTRY =
        Map.entry(ListType.BUSINESS_LIST_CHD_DAILY_CAUSE_LIST,
                  BUSINESS_LIST_CHD_DAILY_CAUSE_LIST_JSON_FILE_PATH);

    public static final Map.Entry<ListType, String> CHANCERY_APPEALS_CHD_DAILY_CAUSE_LIST_ENTRY =
        Map.entry(ListType.CHANCERY_APPEALS_CHD_DAILY_CAUSE_LIST,
                  CHANCERY_APPEALS_CHD_DAILY_CAUSE_LIST_JSON_FILE_PATH);

    // CIC
    public static final Map.Entry<ListType, String> CIC_WEEKLY_HEARING_LIST_ENTRY =
        Map.entry(ListType.CIC_WEEKLY_HEARING_LIST,
                  CIC_WEEKLY_HEARING_LIST_JSON_FILE_PATH);

    // Civil Courts
    public static final Map.Entry<ListType, String> CIVIL_COURTS_RCJ_DAILY_CAUSE_LIST_ENTRY =
        Map.entry(ListType.CIVIL_COURTS_RCJ_DAILY_CAUSE_LIST,
                  CIVIL_COURTS_RCJ_DAILY_CAUSE_LIST_JSON_FILE_PATH);

    // Commercial Court
    public static final Map.Entry<ListType, String> COMMERCIAL_COURT_KB_DAILY_CAUSE_LIST_ENTRY =
        Map.entry(ListType.COMMERCIAL_COURT_KB_DAILY_CAUSE_LIST,
                  COMMERCIAL_COURT_KB_DAILY_CAUSE_LIST_JSON_FILE_PATH);

    // Companies/Insolvency
    public static final Map.Entry<ListType, String> COMPANIES_WINDING_UP_CHD_DAILY_CAUSE_LIST_ENTRY =
        Map.entry(ListType.COMPANIES_WINDING_UP_CHD_DAILY_CAUSE_LIST,
                  COMPANIES_WINDING_UP_CHD_DAILY_CAUSE_LIST_JSON_FILE_PATH);

    public static final Map.Entry<ListType, String> INSOLVENCY_AND_COMPANIES_COURT_CHD_DAILY_CAUSE_LIST_ENTRY =
        Map.entry(ListType.INSOLVENCY_AND_COMPANIES_COURT_CHD_DAILY_CAUSE_LIST,
                  INSOLVENCY_AND_COMPANIES_COURT_CHD_DAILY_CAUSE_LIST_JSON_FILE_PATH);

    // Competition
    public static final Map.Entry<ListType, String> COMPETITION_LIST_CHD_DAILY_CAUSE_LIST_ENTRY =
        Map.entry(ListType.COMPETITION_LIST_CHD_DAILY_CAUSE_LIST,
                  COMPETITION_LIST_CHD_DAILY_CAUSE_LIST_JSON_FILE_PATH);

    // County Court
    public static final Map.Entry<ListType, String> COUNTY_COURT_LONDON_CIVIL_DAILY_CAUSE_LIST_ENTRY =
        Map.entry(ListType.COUNTY_COURT_LONDON_CIVIL_DAILY_CAUSE_LIST,
                  COUNTY_COURT_LONDON_CIVIL_DAILY_CAUSE_LIST_JSON_FILE_PATH);

    // Court of Appeal
    public static final Map.Entry<ListType, String> COURT_OF_APPEAL_CIVIL_DAILY_CAUSE_LIST_ENTRY =
        Map.entry(ListType.COURT_OF_APPEAL_CIVIL_DAILY_CAUSE_LIST,
                  COURT_OF_APPEAL_CIVIL_DAILY_CAUSE_LIST_JSON_FILE_PATH);

    public static final Map.Entry<ListType, String> COURT_OF_APPEAL_CRIMINAL_DAILY_CAUSE_LIST_ENTRY =
        Map.entry(ListType.COURT_OF_APPEAL_CRIMINAL_DAILY_CAUSE_LIST,
                  COURT_OF_APPEAL_CRIMINAL_DAILY_CAUSE_LIST_JSON_FILE_PATH);

    // CST
    public static final Map.Entry<ListType, String> CST_WEEKLY_HEARING_LIST_ENTRY =
        Map.entry(ListType.CST_WEEKLY_HEARING_LIST,
                  CST_WEEKLY_HEARING_LIST_JSON_FILE_PATH);

    // Family Division
    public static final Map.Entry<ListType, String> FAMILY_DIVISION_HIGH_COURT_DAILY_CAUSE_LIST_ENTRY =
        Map.entry(ListType.FAMILY_DIVISION_HIGH_COURT_DAILY_CAUSE_LIST,
                  FAMILY_DIVISION_HIGH_COURT_DAILY_CAUSE_LIST_JSON_FILE_PATH);

    // Financial List
    public static final Map.Entry<ListType, String> FINANCIAL_LIST_CHD_KB_DAILY_CAUSE_LIST_ENTRY =
        Map.entry(ListType.FINANCIAL_LIST_CHD_KB_DAILY_CAUSE_LIST,
                  FINANCIAL_LISTS_CHD_DAILY_CAUSE_LIST_JSON_FILE_PATH);

    // GRC
    public static final Map.Entry<ListType, String> GRC_WEEKLY_HEARING_LIST_ENTRY =
        Map.entry(ListType.GRC_WEEKLY_HEARING_LIST,
                  GRC_WEEKLY_HEARING_LIST_JSON_FILE_PATH);

    // Intellectual Property
    public static final Map.Entry<ListType, String> INTELLECTUAL_PROPERTY_AND_ENTERPRISE_COURT_DAILY_CAUSE_LIST_ENTRY =
        Map.entry(ListType.INTELLECTUAL_PROPERTY_AND_ENTERPRISE_COURT_DAILY_CAUSE_LIST,
                  INTELLECTUAL_PROPERTY_AND_ENTERPRISE_COURT_DAILY_CAUSE_LIST_JSON_FILE_PATH);

    public static final Map.Entry<ListType, String> INTELLECTUAL_PROPERTY_LIST_CHD_DAILY_CAUSE_LIST_ENTRY =
        Map.entry(ListType.INTELLECTUAL_PROPERTY_LIST_CHD_DAILY_CAUSE_LIST,
                  INTELLECTUAL_PROPERTY_LIST_CHD_DAILY_CAUSE_LIST_JSON_FILE_PATH);

    public static final Map.Entry<ListType, String> PATENTS_COURT_CHD_DAILY_CAUSE_LIST_ENTRY =
        Map.entry(ListType.PATENTS_COURT_CHD_DAILY_CAUSE_LIST,
                  PATENTS_COURT_CHD_DAILY_CAUSE_LIST_JSON_FILE_PATH);

    // Interim Applications
    public static final Map.Entry<ListType, String> INTERIM_APPLICATIONS_CHD_DAILY_CAUSE_LIST_ENTRY =
        Map.entry(ListType.INTERIM_APPLICATIONS_CHD_DAILY_CAUSE_LIST,
                  INTERIM_APPLICATION_CHANCERY_LIST_CHD_DAILY_CAUSE_LIST_JSON_FILE_PATH);

    // King's Bench
    public static final Map.Entry<ListType, String> KINGS_BENCH_DIVISION_DAILY_CAUSE_LIST_ENTRY =
        Map.entry(ListType.KINGS_BENCH_DIVISION_DAILY_CAUSE_LIST,
                  KINGS_BENCH_DIVISION_DAILY_CAUSE_LIST_JSON_FILE_PATH);

    public static final Map.Entry<ListType, String> KINGS_BENCH_MASTERS_DAILY_CAUSE_LIST_ENTRY =
        Map.entry(ListType.KINGS_BENCH_MASTERS_DAILY_CAUSE_LIST,
                  KINGS_BENCH_MASTERS_DAILY_CAUSE_LIST_JSON_FILE_PATH);

    // London Circuit Commercial Court
    public static final Map.Entry<ListType, String> LONDON_CIRCUIT_COMMERCIAL_COURT_KB_DAILY_CAUSE_LIST_ENTRY =
        Map.entry(ListType.LONDON_CIRCUIT_COMMERCIAL_COURT_KB_DAILY_CAUSE_LIST,
                  LONDON_CIRCUIT_COURT_DAILY_CAUSE_LIST_JSON_FILE_PATH);

    // Mayor and City
    public static final Map.Entry<ListType, String> MAYOR_AND_CITY_CIVIL_DAILY_CAUSE_LIST_ENTRY =
        Map.entry(ListType.MAYOR_AND_CITY_CIVIL_DAILY_CAUSE_LIST,
                  MAYOR_AND_CITY_CIVIL_DAILY_CAUSE_LIST_JSON_FILE_PATH);

    // PAAC/SIAC/POAC
    public static final Map.Entry<ListType, String> PAAC_WEEKLY_HEARING_LIST_ENTRY =
        Map.entry(ListType.PAAC_WEEKLY_HEARING_LIST,
                  SIAC_WEEKLY_HEARING_LIST_JSON_FILE_PATH);

    public static final Map.Entry<ListType, String> SIAC_WEEKLY_HEARING_LIST_ENTRY =
        Map.entry(ListType.SIAC_WEEKLY_HEARING_LIST,
                  SIAC_WEEKLY_HEARING_LIST_JSON_FILE_PATH);

    public static final Map.Entry<ListType, String> POAC_WEEKLY_HEARING_LIST_ENTRY =
        Map.entry(ListType.POAC_WEEKLY_HEARING_LIST,
                  SIAC_WEEKLY_HEARING_LIST_JSON_FILE_PATH);

    // Pensions
    public static final Map.Entry<ListType, String> PENSIONS_LIST_CHD_DAILY_CAUSE_LIST_ENTRY =
        Map.entry(ListType.PENSIONS_LIST_CHD_DAILY_CAUSE_LIST,
                  PENSIONS_LIST_CHD_DAILY_CAUSE_LIST_JSON_FILE_PATH);

    // PHT
    public static final Map.Entry<ListType, String> PHT_WEEKLY_HEARING_LIST_ENTRY =
        Map.entry(ListType.PHT_WEEKLY_HEARING_LIST,
                  PHT_WEEKLY_HEARING_LIST_JSON_FILE_PATH);

    // Property/Trusts/Probate
    public static final Map.Entry<ListType, String> PROPERTY_TRUSTS_PROBATE_LIST_CHD_DAILY_CAUSE_LIST_ENTRY =
        Map.entry(ListType.PROPERTY_TRUSTS_PROBATE_LIST_CHD_DAILY_CAUSE_LIST,
                  PROPERTY_TRUSTS_PROBATE_LIST_CHD_DAILY_CAUSE_LIST_JSON_FILE_PATH);

    // RPT
    public static final Map.Entry<ListType, String> RPT_EASTERN_WEEKLY_HEARING_LIST_ENTRY =
        Map.entry(ListType.RPT_EASTERN_WEEKLY_HEARING_LIST,
                  RPT_WEEKLY_HEARING_LIST_JSON_FILE_PATH);

    public static final Map.Entry<ListType, String> RPT_LONDON_WEEKLY_HEARING_LIST_ENTRY =
        Map.entry(ListType.RPT_LONDON_WEEKLY_HEARING_LIST,
                  RPT_WEEKLY_HEARING_LIST_JSON_FILE_PATH);

    public static final Map.Entry<ListType, String> RPT_MIDLANDS_WEEKLY_HEARING_LIST_ENTRY =
        Map.entry(ListType.RPT_MIDLANDS_WEEKLY_HEARING_LIST,
                  RPT_WEEKLY_HEARING_LIST_JSON_FILE_PATH);

    public static final Map.Entry<ListType, String> RPT_NORTHERN_WEEKLY_HEARING_LIST_ENTRY =
        Map.entry(ListType.RPT_NORTHERN_WEEKLY_HEARING_LIST,
                  RPT_WEEKLY_HEARING_LIST_JSON_FILE_PATH);

    public static final Map.Entry<ListType, String> RPT_SOUTHERN_WEEKLY_HEARING_LIST_ENTRY =
        Map.entry(ListType.RPT_SOUTHERN_WEEKLY_HEARING_LIST,
                  RPT_WEEKLY_HEARING_LIST_JSON_FILE_PATH);

    // Senior Courts Costs Office
    public static final Map.Entry<ListType, String> SENIOR_COURTS_COSTS_OFFICE_DAILY_CAUSE_LIST_ENTRY =
        Map.entry(ListType.SENIOR_COURTS_COSTS_OFFICE_DAILY_CAUSE_LIST,
                  SENIOR_COURTS_COSTS_OFFICE_DAILY_CAUSE_LIST_JSON_FILE_PATH);

    // SSCS
    public static final Map.Entry<ListType, String> SSCS_LONDON_DAILY_HEARING_LIST_ENTRY =
        Map.entry(ListType.SSCS_LONDON_DAILY_HEARING_LIST,
                  SSCS_DAILY_HEARING_LIST_JSON_FILE_PATH);

    public static final Map.Entry<ListType, String> SSCS_MIDLANDS_DAILY_HEARING_LIST_ENTRY =
        Map.entry(ListType.SSCS_MIDLANDS_DAILY_HEARING_LIST,
                  SSCS_DAILY_HEARING_LIST_JSON_FILE_PATH);

    public static final Map.Entry<ListType, String> SSCS_NORTH_EAST_DAILY_HEARING_LIST_ENTRY =
        Map.entry(ListType.SSCS_NORTH_EAST_DAILY_HEARING_LIST,
                  SSCS_DAILY_HEARING_LIST_JSON_FILE_PATH);

    public static final Map.Entry<ListType, String> SSCS_NORTH_WEST_DAILY_HEARING_LIST_ENTRY =
        Map.entry(ListType.SSCS_NORTH_WEST_DAILY_HEARING_LIST,
                  SSCS_DAILY_HEARING_LIST_JSON_FILE_PATH);

    public static final Map.Entry<ListType, String> SSCS_SCOTLAND_DAILY_HEARING_LIST_ENTRY =
        Map.entry(ListType.SSCS_SCOTLAND_DAILY_HEARING_LIST,
                  SSCS_DAILY_HEARING_LIST_JSON_FILE_PATH);

    public static final Map.Entry<ListType, String> SSCS_SOUTH_EAST_DAILY_HEARING_LIST_ENTRY =
        Map.entry(ListType.SSCS_SOUTH_EAST_DAILY_HEARING_LIST,
                  SSCS_DAILY_HEARING_LIST_JSON_FILE_PATH);

    public static final Map.Entry<ListType, String> SSCS_WALES_AND_SOUTH_WEST_DAILY_HEARING_LIST_ENTRY =
        Map.entry(ListType.SSCS_WALES_AND_SOUTH_WEST_DAILY_HEARING_LIST,
                  SSCS_DAILY_HEARING_LIST_JSON_FILE_PATH);

    // Technology and Construction Court
    public static final Map.Entry<ListType, String> TECHNOLOGY_AND_CONSTRUCTION_COURT_KB_DAILY_CAUSE_LIST_ENTRY =
        Map.entry(ListType.TECHNOLOGY_AND_CONSTRUCTION_COURT_KB_DAILY_CAUSE_LIST,
                  TECHNOLOGY_AND_CONSTRUCTION_COURT_KB_DAILY_CAUSE_LIST_JSON_FILE_PATH);

    // Upper Tribunal
    public static final Map.Entry<ListType, String> UT_AAC_DAILY_HEARING_LIST_ENTRY =
        Map.entry(ListType.UT_AAC_DAILY_HEARING_LIST,
                  UT_ADMINISTRATIVE_APPEALS_CHAMBER_DAILY_HEARING_LIST_JSON_FILE_PATH);

    public static final Map.Entry<ListType, String> UT_IAC_JR_BIRMINGHAM_DAILY_HEARING_LIST_ENTRY =
        Map.entry(ListType.UT_IAC_JR_BIRMINGHAM_DAILY_HEARING_LIST,
                  UT_IAC_JUDICIAL_REVIEWS_DAILY_HEARING_LIST_JSON_FILE_PATH);

    public static final Map.Entry<ListType, String> UT_IAC_JR_CARDIFF_DAILY_HEARING_LIST_ENTRY =
        Map.entry(ListType.UT_IAC_JR_CARDIFF_DAILY_HEARING_LIST,
                  UT_IAC_JUDICIAL_REVIEWS_DAILY_HEARING_LIST_JSON_FILE_PATH);

    public static final Map.Entry<ListType, String> UT_IAC_JR_LEEDS_DAILY_HEARING_LIST_ENTRY =
        Map.entry(ListType.UT_IAC_JR_LEEDS_DAILY_HEARING_LIST,
                  UT_IAC_JUDICIAL_REVIEWS_DAILY_HEARING_LIST_JSON_FILE_PATH);

    public static final Map.Entry<ListType, String> UT_IAC_JR_LONDON_DAILY_HEARING_LIST_ENTRY =
        Map.entry(ListType.UT_IAC_JR_LONDON_DAILY_HEARING_LIST,
                  UT_IAC_JUDICIAL_REVIEWS_LONDON_DAILY_HEARING_LIST_JSON_FILE_PATH);

    public static final Map.Entry<ListType, String> UT_IAC_JR_MANCHESTER_DAILY_HEARING_LIST_ENTRY =
        Map.entry(ListType.UT_IAC_JR_MANCHESTER_DAILY_HEARING_LIST,
                  UT_IAC_JUDICIAL_REVIEWS_DAILY_HEARING_LIST_JSON_FILE_PATH);

    public static final Map.Entry<ListType, String> UT_IAC_STATUTORY_APPEALS_DAILY_HEARING_LIST_ENTRY =
        Map.entry(ListType.UT_IAC_STATUTORY_APPEALS_DAILY_HEARING_LIST,
                  UT_IAC_STATUTORY_APPEALS_DAILY_HEARING_LIST_JSON_FILE_PATH);

    public static final Map.Entry<ListType, String> UT_LC_DAILY_HEARING_LIST_ENTRY =
        Map.entry(ListType.UT_LC_DAILY_HEARING_LIST,
                  UT_LANDS_CHAMBER_DAILY_HEARING_LIST_JSON_FILE_PATH);

    public static final Map.Entry<ListType, String> UT_T_AND_CC_DAILY_HEARING_LIST_ENTRY =
        Map.entry(ListType.UT_T_AND_CC_DAILY_HEARING_LIST,
                  UT_TAX_CHAMBER_DAILY_HEARING_LIST_JSON_FILE_PATH);

    // WPAFCC
    public static final Map.Entry<ListType, String> WPAFCC_WEEKLY_HEARING_LIST_ENTRY =
        Map.entry(ListType.WPAFCC_WEEKLY_HEARING_LIST,
                  WPAFCC_WEEKLY_HEARING_LIST_JSON_FILE_PATH);

    //FT Tax
    public static final Map.Entry<ListType, String> FTT_TAX_WEEKLY_HEARING_LIST_ENTRY =
        Map.entry(ListType.FTT_TAX_WEEKLY_HEARING_LIST,
                  FTT_TAX_WEEKLY_HEARING_LIST_JSON_FILE_PATH);

    //FT Land Register
    public static final Map.Entry<ListType, String> FTT_LR_WEEKLY_HEARING_LIST_ENTRY =
        Map.entry(ListType.FTT_LR_WEEKLY_HEARING_LIST,
                  FTT_LR_WEEKLY_HEARING_LIST_JSON_FILE_PATH);

    //Send Daily Hearing List
    public static final Map.Entry<ListType, String> SEND_DAILY_HEARING_LIST_ENTRY =
        Map.entry(ListType.SEND_DAILY_HEARING_LIST,
                  SEND_DAILY_HEARING_LIST_JSON_FILE_PATH);

    private ListTypeEntries() {
    }
}
