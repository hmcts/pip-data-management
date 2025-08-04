package uk.gov.hmcts.reform.pip.data.management.service.schemavalidation.nonstrategic.configurations;

import org.junit.jupiter.params.provider.Arguments;
import uk.gov.hmcts.reform.pip.model.publication.ListType;

import java.util.stream.Stream;

import static uk.gov.hmcts.reform.pip.data.management.service.schemavalidation.nonstrategic.NonStrategicListTestConstants.ADMIRALTY_COURT_KB_DAILY_CAUSE_LIST_JSON_FILE_PATH;
import static uk.gov.hmcts.reform.pip.data.management.service.schemavalidation.nonstrategic.NonStrategicListTestConstants.BUSINESS_LIST_CHD_DAILY_CAUSE_LIST_JSON_FILE_PATH;
import static uk.gov.hmcts.reform.pip.data.management.service.schemavalidation.nonstrategic.NonStrategicListTestConstants.CASE_NAME;
import static uk.gov.hmcts.reform.pip.data.management.service.schemavalidation.nonstrategic.NonStrategicListTestConstants.CHANCERY_APPEALS_CHD_DAILY_CAUSE_LIST_JSON_FILE_PATH;
import static uk.gov.hmcts.reform.pip.data.management.service.schemavalidation.nonstrategic.NonStrategicListTestConstants.CIC_WEEKLY_HEARING_LIST_JSON_FILE_PATH;
import static uk.gov.hmcts.reform.pip.data.management.service.schemavalidation.nonstrategic.NonStrategicListTestConstants.COMMERCIAL_COURT_KB_DAILY_CAUSE_LIST_JSON_FILE_PATH;
import static uk.gov.hmcts.reform.pip.data.management.service.schemavalidation.nonstrategic.NonStrategicListTestConstants.COMPANIES_WINDING_UP_CHD_DAILY_CAUSE_LIST_JSON_FILE_PATH;
import static uk.gov.hmcts.reform.pip.data.management.service.schemavalidation.nonstrategic.NonStrategicListTestConstants.COMPETITION_LIST_CHD_DAILY_CAUSE_LIST_JSON_FILE_PATH;
import static uk.gov.hmcts.reform.pip.data.management.service.schemavalidation.nonstrategic.NonStrategicListTestConstants.CST_WEEKLY_HEARING_LIST_JSON_FILE_PATH;
import static uk.gov.hmcts.reform.pip.data.management.service.schemavalidation.nonstrategic.NonStrategicListTestConstants.FINANCIAL_LISTS_CHD_DAILY_CAUSE_LIST_JSON_FILE_PATH;
import static uk.gov.hmcts.reform.pip.data.management.service.schemavalidation.nonstrategic.NonStrategicListTestConstants.FTT_LR_WEEKLY_HEARING_LIST_JSON_FILE_PATH;
import static uk.gov.hmcts.reform.pip.data.management.service.schemavalidation.nonstrategic.NonStrategicListTestConstants.FTT_TAX_WEEKLY_HEARING_LIST_JSON_FILE_PATH;
import static uk.gov.hmcts.reform.pip.data.management.service.schemavalidation.nonstrategic.NonStrategicListTestConstants.GRC_WEEKLY_HEARING_LIST_JSON_FILE_PATH;
import static uk.gov.hmcts.reform.pip.data.management.service.schemavalidation.nonstrategic.NonStrategicListTestConstants.HEARING_LIST_NODE;
import static uk.gov.hmcts.reform.pip.data.management.service.schemavalidation.nonstrategic.NonStrategicListTestConstants.INSOLVENCY_AND_COMPANIES_COURT_CHD_DAILY_CAUSE_LIST_JSON_FILE_PATH;
import static uk.gov.hmcts.reform.pip.data.management.service.schemavalidation.nonstrategic.NonStrategicListTestConstants.INTELLECTUAL_PROPERTY_AND_ENTERPRISE_COURT_DAILY_CAUSE_LIST_JSON_FILE_PATH;
import static uk.gov.hmcts.reform.pip.data.management.service.schemavalidation.nonstrategic.NonStrategicListTestConstants.INTELLECTUAL_PROPERTY_LIST_CHD_DAILY_CAUSE_LIST_JSON_FILE_PATH;
import static uk.gov.hmcts.reform.pip.data.management.service.schemavalidation.nonstrategic.NonStrategicListTestConstants.INTERIM_APPLICATION_CHANCERY_LIST_CHD_DAILY_CAUSE_LIST_JSON_FILE_PATH;
import static uk.gov.hmcts.reform.pip.data.management.service.schemavalidation.nonstrategic.NonStrategicListTestConstants.LONDON_CIRCUIT_COURT_DAILY_CAUSE_LIST_JSON_FILE_PATH;
import static uk.gov.hmcts.reform.pip.data.management.service.schemavalidation.nonstrategic.NonStrategicListTestConstants.PATENTS_COURT_CHD_DAILY_CAUSE_LIST_JSON_FILE_PATH;
import static uk.gov.hmcts.reform.pip.data.management.service.schemavalidation.nonstrategic.NonStrategicListTestConstants.PENSIONS_LIST_CHD_DAILY_CAUSE_LIST_JSON_FILE_PATH;
import static uk.gov.hmcts.reform.pip.data.management.service.schemavalidation.nonstrategic.NonStrategicListTestConstants.PHT_WEEKLY_HEARING_LIST_JSON_FILE_PATH;
import static uk.gov.hmcts.reform.pip.data.management.service.schemavalidation.nonstrategic.NonStrategicListTestConstants.PROPERTY_TRUSTS_PROBATE_LIST_CHD_DAILY_CAUSE_LIST_JSON_FILE_PATH;
import static uk.gov.hmcts.reform.pip.data.management.service.schemavalidation.nonstrategic.NonStrategicListTestConstants.TECHNOLOGY_AND_CONSTRUCTION_COURT_KB_DAILY_CAUSE_LIST_JSON_FILE_PATH;
import static uk.gov.hmcts.reform.pip.data.management.service.schemavalidation.nonstrategic.NonStrategicListTestConstants.UT_LANDS_CHAMBER_DAILY_HEARING_LIST_JSON_FILE_PATH;
import static uk.gov.hmcts.reform.pip.data.management.service.schemavalidation.nonstrategic.NonStrategicListTestConstants.UT_TAX_CHAMBER_DAILY_HEARING_LIST_JSON_FILE_PATH;
import static uk.gov.hmcts.reform.pip.data.management.service.schemavalidation.nonstrategic.NonStrategicListTestConstants.WPAFCC_WEEKLY_HEARING_LIST_JSON_FILE_PATH;

public final class CaseNameAttribute {
    private CaseNameAttribute() {
    }

    public static Stream<Arguments> caseNameMandatoryAttribute() {
        return Stream.of(
            Arguments.of(new ListTypeTestInput(
                ListType.ADMIRALTY_COURT_KB_DAILY_CAUSE_LIST,
                ADMIRALTY_COURT_KB_DAILY_CAUSE_LIST_JSON_FILE_PATH,
                CASE_NAME)),
            Arguments.of(new ListTypeTestInput(
                ListType.BUSINESS_LIST_CHD_DAILY_CAUSE_LIST,
                BUSINESS_LIST_CHD_DAILY_CAUSE_LIST_JSON_FILE_PATH,
                CASE_NAME)),
            Arguments.of(new ListTypeTestInput(
                ListType.CHANCERY_APPEALS_CHD_DAILY_CAUSE_LIST,
                CHANCERY_APPEALS_CHD_DAILY_CAUSE_LIST_JSON_FILE_PATH,
                CASE_NAME)),
            Arguments.of(new ListTypeTestInput(
                ListType.CIC_WEEKLY_HEARING_LIST,
                CIC_WEEKLY_HEARING_LIST_JSON_FILE_PATH,
                CASE_NAME)),
            Arguments.of(new ListTypeTestInput(
                ListType.COMMERCIAL_COURT_KB_DAILY_CAUSE_LIST,
                COMMERCIAL_COURT_KB_DAILY_CAUSE_LIST_JSON_FILE_PATH,
                CASE_NAME)),
            Arguments.of(new ListTypeTestInput(
                ListType.COMPANIES_WINDING_UP_CHD_DAILY_CAUSE_LIST,
                COMPANIES_WINDING_UP_CHD_DAILY_CAUSE_LIST_JSON_FILE_PATH,
                CASE_NAME)),
            Arguments.of(new ListTypeTestInput(
                ListType.COMPETITION_LIST_CHD_DAILY_CAUSE_LIST,
                COMPETITION_LIST_CHD_DAILY_CAUSE_LIST_JSON_FILE_PATH,
                CASE_NAME)),
            Arguments.of(new ListTypeTestInput(
                ListType.CST_WEEKLY_HEARING_LIST,
                CST_WEEKLY_HEARING_LIST_JSON_FILE_PATH,
                CASE_NAME)),
            Arguments.of(new ListTypeTestInput(
                ListType.FINANCIAL_LIST_CHD_KB_DAILY_CAUSE_LIST,
                FINANCIAL_LISTS_CHD_DAILY_CAUSE_LIST_JSON_FILE_PATH,
                CASE_NAME)),
            Arguments.of(new ListTypeTestInput(
                ListType.FTT_LR_WEEKLY_HEARING_LIST,
                FTT_LR_WEEKLY_HEARING_LIST_JSON_FILE_PATH,
                CASE_NAME)),
            Arguments.of(new ListTypeTestInput(
                ListType.FTT_TAX_WEEKLY_HEARING_LIST,
                FTT_TAX_WEEKLY_HEARING_LIST_JSON_FILE_PATH,
                CASE_NAME)),
            Arguments.of(new ListTypeTestInput(
                ListType.GRC_WEEKLY_HEARING_LIST,
                GRC_WEEKLY_HEARING_LIST_JSON_FILE_PATH,
                CASE_NAME)),
            Arguments.of(new ListTypeTestInput(
                ListType.INSOLVENCY_AND_COMPANIES_COURT_CHD_DAILY_CAUSE_LIST,
                INSOLVENCY_AND_COMPANIES_COURT_CHD_DAILY_CAUSE_LIST_JSON_FILE_PATH,
                CASE_NAME)),
            Arguments.of(new ListTypeTestInput(
                ListType.INTELLECTUAL_PROPERTY_AND_ENTERPRISE_COURT_DAILY_CAUSE_LIST,
                INTELLECTUAL_PROPERTY_AND_ENTERPRISE_COURT_DAILY_CAUSE_LIST_JSON_FILE_PATH,
                CASE_NAME)),
            Arguments.of(new ListTypeTestInput(
                ListType.INTELLECTUAL_PROPERTY_LIST_CHD_DAILY_CAUSE_LIST,
                INTELLECTUAL_PROPERTY_LIST_CHD_DAILY_CAUSE_LIST_JSON_FILE_PATH,
                CASE_NAME)),
            Arguments.of(new ListTypeTestInput(
                ListType.INTERIM_APPLICATIONS_CHD_DAILY_CAUSE_LIST,
                INTERIM_APPLICATION_CHANCERY_LIST_CHD_DAILY_CAUSE_LIST_JSON_FILE_PATH,
                CASE_NAME, HEARING_LIST_NODE)),
            Arguments.of(new ListTypeTestInput(
                ListType.LONDON_CIRCUIT_COMMERCIAL_COURT_KB_DAILY_CAUSE_LIST,
                LONDON_CIRCUIT_COURT_DAILY_CAUSE_LIST_JSON_FILE_PATH,
                CASE_NAME)),
            Arguments.of(new ListTypeTestInput(
                ListType.PATENTS_COURT_CHD_DAILY_CAUSE_LIST,
                PATENTS_COURT_CHD_DAILY_CAUSE_LIST_JSON_FILE_PATH,
                CASE_NAME)),
            Arguments.of(new ListTypeTestInput(
                ListType.PENSIONS_LIST_CHD_DAILY_CAUSE_LIST,
                PENSIONS_LIST_CHD_DAILY_CAUSE_LIST_JSON_FILE_PATH,
                CASE_NAME)),
            Arguments.of(new ListTypeTestInput(
                ListType.PHT_WEEKLY_HEARING_LIST,
                PHT_WEEKLY_HEARING_LIST_JSON_FILE_PATH,
                CASE_NAME)),
            Arguments.of(new ListTypeTestInput(
                ListType.PROPERTY_TRUSTS_PROBATE_LIST_CHD_DAILY_CAUSE_LIST,
                PROPERTY_TRUSTS_PROBATE_LIST_CHD_DAILY_CAUSE_LIST_JSON_FILE_PATH,
                CASE_NAME)),
            Arguments.of(new ListTypeTestInput(
                ListType.TECHNOLOGY_AND_CONSTRUCTION_COURT_KB_DAILY_CAUSE_LIST,
                TECHNOLOGY_AND_CONSTRUCTION_COURT_KB_DAILY_CAUSE_LIST_JSON_FILE_PATH,
                CASE_NAME)),
            Arguments.of(new ListTypeTestInput(
                ListType.UT_LC_DAILY_HEARING_LIST,
                UT_LANDS_CHAMBER_DAILY_HEARING_LIST_JSON_FILE_PATH,
                CASE_NAME)),
            Arguments.of(new ListTypeTestInput(
                ListType.UT_T_AND_CC_DAILY_HEARING_LIST,
                UT_TAX_CHAMBER_DAILY_HEARING_LIST_JSON_FILE_PATH,
                CASE_NAME)),
            Arguments.of(new ListTypeTestInput(
                ListType.WPAFCC_WEEKLY_HEARING_LIST,
                WPAFCC_WEEKLY_HEARING_LIST_JSON_FILE_PATH,
                CASE_NAME))
        );
    }
}
