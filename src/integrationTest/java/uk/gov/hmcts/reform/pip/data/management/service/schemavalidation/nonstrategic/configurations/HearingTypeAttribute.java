package uk.gov.hmcts.reform.pip.data.management.service.schemavalidation.nonstrategic.configurations;

import org.junit.jupiter.params.provider.Arguments;
import uk.gov.hmcts.reform.pip.model.publication.ListType;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static java.util.Map.entry;
import static uk.gov.hmcts.reform.pip.data.management.service.schemavalidation.nonstrategic.NonStrategicListTestConstants.ADMINISTRATIVE_COURT_DAILY_CAUSE_LIST_JSON_FILE_PATH;
import static uk.gov.hmcts.reform.pip.data.management.service.schemavalidation.nonstrategic.NonStrategicListTestConstants.AST_DAILY_HEARING_LIST_JSON_FILE_PATH;
import static uk.gov.hmcts.reform.pip.data.management.service.schemavalidation.nonstrategic.NonStrategicListTestConstants.CIVIL_COURTS_RCJ_DAILY_CAUSE_LIST_JSON_FILE_PATH;
import static uk.gov.hmcts.reform.pip.data.management.service.schemavalidation.nonstrategic.NonStrategicListTestConstants.COUNTY_COURT_LONDON_CIVIL_DAILY_CAUSE_LIST_JSON_FILE_PATH;
import static uk.gov.hmcts.reform.pip.data.management.service.schemavalidation.nonstrategic.NonStrategicListTestConstants.COURT_OF_APPEAL_CIVIL_DAILY_CAUSE_LIST_JSON_FILE_PATH;
import static uk.gov.hmcts.reform.pip.data.management.service.schemavalidation.nonstrategic.NonStrategicListTestConstants.COURT_OF_APPEAL_CRIMINAL_DAILY_CAUSE_LIST_JSON_FILE_PATH;
import static uk.gov.hmcts.reform.pip.data.management.service.schemavalidation.nonstrategic.NonStrategicListTestConstants.CST_WEEKLY_HEARING_LIST_JSON_FILE_PATH;
import static uk.gov.hmcts.reform.pip.data.management.service.schemavalidation.nonstrategic.NonStrategicListTestConstants.FAMILY_DIVISION_HIGH_COURT_DAILY_CAUSE_LIST_JSON_FILE_PATH;
import static uk.gov.hmcts.reform.pip.data.management.service.schemavalidation.nonstrategic.NonStrategicListTestConstants.FUTURE_JUDGMENTS_NODE;
import static uk.gov.hmcts.reform.pip.data.management.service.schemavalidation.nonstrategic.NonStrategicListTestConstants.HEARING_LIST_NODE;
import static uk.gov.hmcts.reform.pip.data.management.service.schemavalidation.nonstrategic.NonStrategicListTestConstants.HEARING_TYPE;
import static uk.gov.hmcts.reform.pip.data.management.service.schemavalidation.nonstrategic.NonStrategicListTestConstants.KINGS_BENCH_DIVISION_DAILY_CAUSE_LIST_JSON_FILE_PATH;
import static uk.gov.hmcts.reform.pip.data.management.service.schemavalidation.nonstrategic.NonStrategicListTestConstants.KINGS_BENCH_MASTERS_DAILY_CAUSE_LIST_JSON_FILE_PATH;
import static uk.gov.hmcts.reform.pip.data.management.service.schemavalidation.nonstrategic.NonStrategicListTestConstants.LONDON_ADMINISTRATIVE_COURT_DAILY_CAUSE_LIST_JSON_FILE_PATH;
import static uk.gov.hmcts.reform.pip.data.management.service.schemavalidation.nonstrategic.NonStrategicListTestConstants.LONDON_ADMINISTRATIVE_COURT_NODE;
import static uk.gov.hmcts.reform.pip.data.management.service.schemavalidation.nonstrategic.NonStrategicListTestConstants.MAYOR_AND_CITY_CIVIL_DAILY_CAUSE_LIST_JSON_FILE_PATH;
import static uk.gov.hmcts.reform.pip.data.management.service.schemavalidation.nonstrategic.NonStrategicListTestConstants.PHT_WEEKLY_HEARING_LIST_JSON_FILE_PATH;
import static uk.gov.hmcts.reform.pip.data.management.service.schemavalidation.nonstrategic.NonStrategicListTestConstants.PLANNING_COURT_NODE;
import static uk.gov.hmcts.reform.pip.data.management.service.schemavalidation.nonstrategic.NonStrategicListTestConstants.SEND_DAILY_HEARING_LIST_JSON_FILE_PATH;
import static uk.gov.hmcts.reform.pip.data.management.service.schemavalidation.nonstrategic.NonStrategicListTestConstants.SENIOR_COURTS_COSTS_OFFICE_DAILY_CAUSE_LIST_JSON_FILE_PATH;
import static uk.gov.hmcts.reform.pip.data.management.service.schemavalidation.nonstrategic.NonStrategicListTestConstants.SIAC_WEEKLY_HEARING_LIST_JSON_FILE_PATH;
import static uk.gov.hmcts.reform.pip.data.management.service.schemavalidation.nonstrategic.NonStrategicListTestConstants.SSCS_DAILY_HEARING_LIST_JSON_FILE_PATH;
import static uk.gov.hmcts.reform.pip.data.management.service.schemavalidation.nonstrategic.NonStrategicListTestConstants.UT_IAC_JUDICIAL_REVIEWS_DAILY_HEARING_LIST_JSON_FILE_PATH;
import static uk.gov.hmcts.reform.pip.data.management.service.schemavalidation.nonstrategic.NonStrategicListTestConstants.UT_IAC_STATUTORY_APPEALS_DAILY_HEARING_LIST_JSON_FILE_PATH;
import static uk.gov.hmcts.reform.pip.data.management.service.schemavalidation.nonstrategic.NonStrategicListTestConstants.UT_LANDS_CHAMBER_DAILY_HEARING_LIST_JSON_FILE_PATH;
import static uk.gov.hmcts.reform.pip.data.management.service.schemavalidation.nonstrategic.NonStrategicListTestConstants.UT_TAX_CHAMBER_DAILY_HEARING_LIST_JSON_FILE_PATH;

public final class HearingTypeAttribute {
    private static final Map<ListType, String> LIST_TYPE_JSON_FILE = Map.ofEntries(
        entry(ListType.AST_DAILY_HEARING_LIST, AST_DAILY_HEARING_LIST_JSON_FILE_PATH),
        entry(ListType.BIRMINGHAM_ADMINISTRATIVE_COURT_DAILY_CAUSE_LIST,
              ADMINISTRATIVE_COURT_DAILY_CAUSE_LIST_JSON_FILE_PATH),
        entry(ListType.BRISTOL_AND_CARDIFF_ADMINISTRATIVE_COURT_DAILY_CAUSE_LIST,
              ADMINISTRATIVE_COURT_DAILY_CAUSE_LIST_JSON_FILE_PATH),
        entry(ListType.CIVIL_COURTS_RCJ_DAILY_CAUSE_LIST, CIVIL_COURTS_RCJ_DAILY_CAUSE_LIST_JSON_FILE_PATH),
        entry(ListType.COUNTY_COURT_LONDON_CIVIL_DAILY_CAUSE_LIST,
              COUNTY_COURT_LONDON_CIVIL_DAILY_CAUSE_LIST_JSON_FILE_PATH),
        entry(ListType.COURT_OF_APPEAL_CIVIL_DAILY_CAUSE_LIST, COURT_OF_APPEAL_CIVIL_DAILY_CAUSE_LIST_JSON_FILE_PATH),
        entry(ListType.COURT_OF_APPEAL_CRIMINAL_DAILY_CAUSE_LIST,
              COURT_OF_APPEAL_CRIMINAL_DAILY_CAUSE_LIST_JSON_FILE_PATH),
        entry(ListType.CST_WEEKLY_HEARING_LIST, CST_WEEKLY_HEARING_LIST_JSON_FILE_PATH),
        entry(ListType.FAMILY_DIVISION_HIGH_COURT_DAILY_CAUSE_LIST,
              FAMILY_DIVISION_HIGH_COURT_DAILY_CAUSE_LIST_JSON_FILE_PATH),
        entry(ListType.KINGS_BENCH_DIVISION_DAILY_CAUSE_LIST,
              KINGS_BENCH_DIVISION_DAILY_CAUSE_LIST_JSON_FILE_PATH),
        entry(ListType.KINGS_BENCH_MASTERS_DAILY_CAUSE_LIST,
              KINGS_BENCH_MASTERS_DAILY_CAUSE_LIST_JSON_FILE_PATH),
        entry(ListType.LEEDS_ADMINISTRATIVE_COURT_DAILY_CAUSE_LIST,
              ADMINISTRATIVE_COURT_DAILY_CAUSE_LIST_JSON_FILE_PATH),
        entry(ListType.LONDON_ADMINISTRATIVE_COURT_DAILY_CAUSE_LIST,
              LONDON_ADMINISTRATIVE_COURT_DAILY_CAUSE_LIST_JSON_FILE_PATH),
        entry(ListType.MANCHESTER_ADMINISTRATIVE_COURT_DAILY_CAUSE_LIST,
              ADMINISTRATIVE_COURT_DAILY_CAUSE_LIST_JSON_FILE_PATH),
        entry(ListType.MAYOR_AND_CITY_CIVIL_DAILY_CAUSE_LIST,
              MAYOR_AND_CITY_CIVIL_DAILY_CAUSE_LIST_JSON_FILE_PATH),
        entry(ListType.PAAC_WEEKLY_HEARING_LIST, SIAC_WEEKLY_HEARING_LIST_JSON_FILE_PATH),
        entry(ListType.SIAC_WEEKLY_HEARING_LIST, SIAC_WEEKLY_HEARING_LIST_JSON_FILE_PATH),
        entry(ListType.POAC_WEEKLY_HEARING_LIST, SIAC_WEEKLY_HEARING_LIST_JSON_FILE_PATH),
        entry(ListType.PHT_WEEKLY_HEARING_LIST, PHT_WEEKLY_HEARING_LIST_JSON_FILE_PATH),
        entry(ListType.SEND_DAILY_HEARING_LIST, SEND_DAILY_HEARING_LIST_JSON_FILE_PATH),
        entry(ListType.SENIOR_COURTS_COSTS_OFFICE_DAILY_CAUSE_LIST,
              SENIOR_COURTS_COSTS_OFFICE_DAILY_CAUSE_LIST_JSON_FILE_PATH),
        entry(ListType.SSCS_LONDON_DAILY_HEARING_LIST, SSCS_DAILY_HEARING_LIST_JSON_FILE_PATH),
        entry(ListType.SSCS_MIDLANDS_DAILY_HEARING_LIST, SSCS_DAILY_HEARING_LIST_JSON_FILE_PATH),
        entry(ListType.SSCS_NORTH_EAST_DAILY_HEARING_LIST, SSCS_DAILY_HEARING_LIST_JSON_FILE_PATH),
        entry(ListType.SSCS_NORTH_WEST_DAILY_HEARING_LIST, SSCS_DAILY_HEARING_LIST_JSON_FILE_PATH),
        entry(ListType.SSCS_SCOTLAND_DAILY_HEARING_LIST, SSCS_DAILY_HEARING_LIST_JSON_FILE_PATH),
        entry(ListType.SSCS_SOUTH_EAST_DAILY_HEARING_LIST, SSCS_DAILY_HEARING_LIST_JSON_FILE_PATH),
        entry(ListType.SSCS_WALES_AND_SOUTH_WEST_DAILY_HEARING_LIST, SSCS_DAILY_HEARING_LIST_JSON_FILE_PATH),
        entry(ListType.UT_IAC_JR_BIRMINGHAM_DAILY_HEARING_LIST,
              UT_IAC_JUDICIAL_REVIEWS_DAILY_HEARING_LIST_JSON_FILE_PATH),
        entry(ListType.UT_IAC_JR_CARDIFF_DAILY_HEARING_LIST,
              UT_IAC_JUDICIAL_REVIEWS_DAILY_HEARING_LIST_JSON_FILE_PATH),
        entry(ListType.UT_IAC_JR_LEEDS_DAILY_HEARING_LIST, UT_IAC_JUDICIAL_REVIEWS_DAILY_HEARING_LIST_JSON_FILE_PATH),
        entry(ListType.UT_IAC_JR_LONDON_DAILY_HEARING_LIST, UT_IAC_JUDICIAL_REVIEWS_DAILY_HEARING_LIST_JSON_FILE_PATH),
        entry(ListType.UT_IAC_JR_MANCHESTER_DAILY_HEARING_LIST,
              UT_IAC_JUDICIAL_REVIEWS_DAILY_HEARING_LIST_JSON_FILE_PATH),
        entry(ListType.UT_IAC_STATUTORY_APPEALS_DAILY_HEARING_LIST,
              UT_IAC_STATUTORY_APPEALS_DAILY_HEARING_LIST_JSON_FILE_PATH),
        entry(ListType.UT_LC_DAILY_HEARING_LIST, UT_LANDS_CHAMBER_DAILY_HEARING_LIST_JSON_FILE_PATH),
        entry(ListType.UT_T_AND_CC_DAILY_HEARING_LIST, UT_TAX_CHAMBER_DAILY_HEARING_LIST_JSON_FILE_PATH)
    );

    private static final Map<ListType, List<String>> LIST_TYPE_JSON_FILE_PARENT_NODES = Map.of(
        ListType.COURT_OF_APPEAL_CIVIL_DAILY_CAUSE_LIST, List.of(HEARING_LIST_NODE, FUTURE_JUDGMENTS_NODE),
        ListType.LONDON_ADMINISTRATIVE_COURT_DAILY_CAUSE_LIST,
        List.of(LONDON_ADMINISTRATIVE_COURT_NODE, PLANNING_COURT_NODE)
    );

    public static Stream<Arguments> hearingTypeMandatoryAttribute() {
        return ListTypeTestInput.generateListTypeTestInputsForAttribute(LIST_TYPE_JSON_FILE,
            LIST_TYPE_JSON_FILE_PARENT_NODES, HEARING_TYPE)
            .stream()
            .map(Arguments::of);
    }

    private HearingTypeAttribute() {
    }
}
