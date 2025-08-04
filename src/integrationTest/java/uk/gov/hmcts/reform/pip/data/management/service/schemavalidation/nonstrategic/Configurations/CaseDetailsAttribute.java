package uk.gov.hmcts.reform.pip.data.management.service.schemavalidation.nonstrategic.Configurations;

import org.junit.jupiter.params.provider.Arguments;
import uk.gov.hmcts.reform.pip.model.publication.ListType;

import java.util.stream.Stream;

import static uk.gov.hmcts.reform.pip.data.management.service.schemavalidation.nonstrategic.NonStrategicListTestConstants.ADMINISTRATIVE_COURT_DAILY_CAUSE_LIST_JSON_FILE_PATH;
import static uk.gov.hmcts.reform.pip.data.management.service.schemavalidation.nonstrategic.NonStrategicListTestConstants.CASE_DETAILS;
import static uk.gov.hmcts.reform.pip.data.management.service.schemavalidation.nonstrategic.NonStrategicListTestConstants.CIVIL_COURTS_RCJ_DAILY_CAUSE_LIST_JSON_FILE_PATH;
import static uk.gov.hmcts.reform.pip.data.management.service.schemavalidation.nonstrategic.NonStrategicListTestConstants.COUNTY_COURT_LONDON_CIVIL_DAILY_CAUSE_LIST_JSON_FILE_PATH;
import static uk.gov.hmcts.reform.pip.data.management.service.schemavalidation.nonstrategic.NonStrategicListTestConstants.COURT_OF_APPEAL_CIVIL_DAILY_CAUSE_LIST_JSON_FILE_PATH;
import static uk.gov.hmcts.reform.pip.data.management.service.schemavalidation.nonstrategic.NonStrategicListTestConstants.COURT_OF_APPEAL_CRIMINAL_DAILY_CAUSE_LIST_JSON_FILE_PATH;
import static uk.gov.hmcts.reform.pip.data.management.service.schemavalidation.nonstrategic.NonStrategicListTestConstants.FAMILY_DIVISION_HIGH_COURT_DAILY_CAUSE_LIST_JSON_FILE_PATH;
import static uk.gov.hmcts.reform.pip.data.management.service.schemavalidation.nonstrategic.NonStrategicListTestConstants.FUTURE_JUDGMENTS_NODE;
import static uk.gov.hmcts.reform.pip.data.management.service.schemavalidation.nonstrategic.NonStrategicListTestConstants.HEARING_LIST_NODE;
import static uk.gov.hmcts.reform.pip.data.management.service.schemavalidation.nonstrategic.NonStrategicListTestConstants.KINGS_BENCH_DIVISION_DAILY_CAUSE_LIST_JSON_FILE_PATH;
import static uk.gov.hmcts.reform.pip.data.management.service.schemavalidation.nonstrategic.NonStrategicListTestConstants.KINGS_BENCH_MASTERS_DAILY_CAUSE_LIST_JSON_FILE_PATH;
import static uk.gov.hmcts.reform.pip.data.management.service.schemavalidation.nonstrategic.NonStrategicListTestConstants.LONDON_ADMINISTRATIVE_COURT_DAILY_CAUSE_LIST_JSON_FILE_PATH;
import static uk.gov.hmcts.reform.pip.data.management.service.schemavalidation.nonstrategic.NonStrategicListTestConstants.LONDON_ADMINISTRATIVE_COURT_NODE;
import static uk.gov.hmcts.reform.pip.data.management.service.schemavalidation.nonstrategic.NonStrategicListTestConstants.MAYOR_AND_CITY_CIVIL_DAILY_CAUSE_LIST_JSON_FILE_PATH;
import static uk.gov.hmcts.reform.pip.data.management.service.schemavalidation.nonstrategic.NonStrategicListTestConstants.PLANNING_COURT_NODE;
import static uk.gov.hmcts.reform.pip.data.management.service.schemavalidation.nonstrategic.NonStrategicListTestConstants.SENIOR_COURTS_COSTS_OFFICE_DAILY_CAUSE_LIST_JSON_FILE_PATH;

public class CaseDetailsAttribute {
    public static Stream<Arguments> caseDetailsMandatoryAttribute() {
        return Stream.of(
            Arguments.of(new ListTypeTest(
                ListType.BIRMINGHAM_ADMINISTRATIVE_COURT_DAILY_CAUSE_LIST,
                ADMINISTRATIVE_COURT_DAILY_CAUSE_LIST_JSON_FILE_PATH,
                CASE_DETAILS)),
            Arguments.of(new ListTypeTest(
                ListType.BRISTOL_AND_CARDIFF_ADMINISTRATIVE_COURT_DAILY_CAUSE_LIST,
                ADMINISTRATIVE_COURT_DAILY_CAUSE_LIST_JSON_FILE_PATH,
                CASE_DETAILS)),
            Arguments.of(new ListTypeTest(
                ListType.CIVIL_COURTS_RCJ_DAILY_CAUSE_LIST,
                CIVIL_COURTS_RCJ_DAILY_CAUSE_LIST_JSON_FILE_PATH,
                CASE_DETAILS)),
            Arguments.of(new ListTypeTest(
                ListType.COUNTY_COURT_LONDON_CIVIL_DAILY_CAUSE_LIST,
                COUNTY_COURT_LONDON_CIVIL_DAILY_CAUSE_LIST_JSON_FILE_PATH,
                CASE_DETAILS)),
            Arguments.of(new ListTypeTest(
                ListType.COURT_OF_APPEAL_CIVIL_DAILY_CAUSE_LIST,
                COURT_OF_APPEAL_CIVIL_DAILY_CAUSE_LIST_JSON_FILE_PATH,
                CASE_DETAILS, HEARING_LIST_NODE)),
            Arguments.of(new ListTypeTest(
                ListType.COURT_OF_APPEAL_CIVIL_DAILY_CAUSE_LIST,
                COURT_OF_APPEAL_CIVIL_DAILY_CAUSE_LIST_JSON_FILE_PATH,
                CASE_DETAILS, FUTURE_JUDGMENTS_NODE)),
            Arguments.of(new ListTypeTest(
                ListType.COURT_OF_APPEAL_CRIMINAL_DAILY_CAUSE_LIST,
                COURT_OF_APPEAL_CRIMINAL_DAILY_CAUSE_LIST_JSON_FILE_PATH,
                CASE_DETAILS)),
            Arguments.of(new ListTypeTest(
                ListType.FAMILY_DIVISION_HIGH_COURT_DAILY_CAUSE_LIST,
                FAMILY_DIVISION_HIGH_COURT_DAILY_CAUSE_LIST_JSON_FILE_PATH,
                CASE_DETAILS)),
            Arguments.of(new ListTypeTest(
                ListType.KINGS_BENCH_DIVISION_DAILY_CAUSE_LIST,
                KINGS_BENCH_DIVISION_DAILY_CAUSE_LIST_JSON_FILE_PATH,
                CASE_DETAILS)),
            Arguments.of(new ListTypeTest(
                ListType.KINGS_BENCH_MASTERS_DAILY_CAUSE_LIST,
                KINGS_BENCH_MASTERS_DAILY_CAUSE_LIST_JSON_FILE_PATH,
                CASE_DETAILS)),
            Arguments.of(new ListTypeTest(
                ListType.LEEDS_ADMINISTRATIVE_COURT_DAILY_CAUSE_LIST,
                ADMINISTRATIVE_COURT_DAILY_CAUSE_LIST_JSON_FILE_PATH,
                CASE_DETAILS)),
            Arguments.of(new ListTypeTest(
                ListType.LONDON_ADMINISTRATIVE_COURT_DAILY_CAUSE_LIST,
                LONDON_ADMINISTRATIVE_COURT_DAILY_CAUSE_LIST_JSON_FILE_PATH,
                CASE_DETAILS, LONDON_ADMINISTRATIVE_COURT_NODE)),
            Arguments.of(new ListTypeTest(
                ListType.LONDON_ADMINISTRATIVE_COURT_DAILY_CAUSE_LIST,
                LONDON_ADMINISTRATIVE_COURT_DAILY_CAUSE_LIST_JSON_FILE_PATH,
                CASE_DETAILS, PLANNING_COURT_NODE)),
            Arguments.of(new ListTypeTest(
                ListType.MANCHESTER_ADMINISTRATIVE_COURT_DAILY_CAUSE_LIST,
                ADMINISTRATIVE_COURT_DAILY_CAUSE_LIST_JSON_FILE_PATH,
                CASE_DETAILS)),
            Arguments.of(new ListTypeTest(
                ListType.MAYOR_AND_CITY_CIVIL_DAILY_CAUSE_LIST,
                MAYOR_AND_CITY_CIVIL_DAILY_CAUSE_LIST_JSON_FILE_PATH,
                CASE_DETAILS)),
            Arguments.of(new ListTypeTest(
                ListType.SENIOR_COURTS_COSTS_OFFICE_DAILY_CAUSE_LIST,
                SENIOR_COURTS_COSTS_OFFICE_DAILY_CAUSE_LIST_JSON_FILE_PATH,
                CASE_DETAILS))
        );
    }
}
