package uk.gov.hmcts.reform.pip.data.management.service.helpers;

import uk.gov.hmcts.reform.pip.model.publication.ListType;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

import static uk.gov.hmcts.reform.pip.model.publication.ListType.ADMIRALTY_COURT_KB_DAILY_CAUSE_LIST;
import static uk.gov.hmcts.reform.pip.model.publication.ListType.AST_DAILY_HEARING_LIST;
import static uk.gov.hmcts.reform.pip.model.publication.ListType.BUSINESS_LIST_CHD_DAILY_CAUSE_LIST;
import static uk.gov.hmcts.reform.pip.model.publication.ListType.CHANCERY_APPEALS_CHD_DAILY_CAUSE_LIST;
import static uk.gov.hmcts.reform.pip.model.publication.ListType.CIVIL_COURTS_RCJ_DAILY_CAUSE_LIST;
import static uk.gov.hmcts.reform.pip.model.publication.ListType.COMMERCIAL_COURT_KB_DAILY_CAUSE_LIST;
import static uk.gov.hmcts.reform.pip.model.publication.ListType.COMPANIES_WINDING_UP_CHD_DAILY_CAUSE_LIST;
import static uk.gov.hmcts.reform.pip.model.publication.ListType.COMPETITION_LIST_CHD_DAILY_CAUSE_LIST;
import static uk.gov.hmcts.reform.pip.model.publication.ListType.COUNTY_COURT_LONDON_CIVIL_DAILY_CAUSE_LIST;
import static uk.gov.hmcts.reform.pip.model.publication.ListType.COURT_OF_APPEAL_CIVIL_DAILY_CAUSE_LIST;
import static uk.gov.hmcts.reform.pip.model.publication.ListType.COURT_OF_APPEAL_CRIMINAL_DAILY_CAUSE_LIST;
import static uk.gov.hmcts.reform.pip.model.publication.ListType.CST_WEEKLY_HEARING_LIST;
import static uk.gov.hmcts.reform.pip.model.publication.ListType.FAMILY_DIVISION_HIGH_COURT_DAILY_CAUSE_LIST;
import static uk.gov.hmcts.reform.pip.model.publication.ListType.FINANCIAL_LIST_CHD_KB_DAILY_CAUSE_LIST;
import static uk.gov.hmcts.reform.pip.model.publication.ListType.FTT_LR_WEEKLY_HEARING_LIST;
import static uk.gov.hmcts.reform.pip.model.publication.ListType.FTT_TAX_WEEKLY_HEARING_LIST;
import static uk.gov.hmcts.reform.pip.model.publication.ListType.GRC_WEEKLY_HEARING_LIST;
import static uk.gov.hmcts.reform.pip.model.publication.ListType.INSOLVENCY_AND_COMPANIES_COURT_CHD_DAILY_CAUSE_LIST;
import static uk.gov.hmcts.reform.pip.model.publication.ListType.INTELLECTUAL_PROPERTY_AND_ENTERPRISE_COURT_DAILY_CAUSE_LIST;
import static uk.gov.hmcts.reform.pip.model.publication.ListType.INTELLECTUAL_PROPERTY_LIST_CHD_DAILY_CAUSE_LIST;
import static uk.gov.hmcts.reform.pip.model.publication.ListType.INTERIM_APPLICATIONS_CHD_DAILY_CAUSE_LIST;
import static uk.gov.hmcts.reform.pip.model.publication.ListType.KINGS_BENCH_DIVISION_DAILY_CAUSE_LIST;
import static uk.gov.hmcts.reform.pip.model.publication.ListType.KINGS_BENCH_MASTERS_DAILY_CAUSE_LIST;
import static uk.gov.hmcts.reform.pip.model.publication.ListType.LONDON_ADMINISTRATIVE_COURT_DAILY_CAUSE_LIST;
import static uk.gov.hmcts.reform.pip.model.publication.ListType.LONDON_CIRCUIT_COMMERCIAL_COURT_KB_DAILY_CAUSE_LIST;
import static uk.gov.hmcts.reform.pip.model.publication.ListType.MAYOR_AND_CITY_CIVIL_DAILY_CAUSE_LIST;
import static uk.gov.hmcts.reform.pip.model.publication.ListType.PAAC_WEEKLY_HEARING_LIST;
import static uk.gov.hmcts.reform.pip.model.publication.ListType.PATENTS_COURT_CHD_DAILY_CAUSE_LIST;
import static uk.gov.hmcts.reform.pip.model.publication.ListType.PENSIONS_LIST_CHD_DAILY_CAUSE_LIST;
import static uk.gov.hmcts.reform.pip.model.publication.ListType.PHT_WEEKLY_HEARING_LIST;
import static uk.gov.hmcts.reform.pip.model.publication.ListType.PLANNING_COURT_DAILY_CAUSE_LIST;
import static uk.gov.hmcts.reform.pip.model.publication.ListType.POAC_WEEKLY_HEARING_LIST;
import static uk.gov.hmcts.reform.pip.model.publication.ListType.PROPERTY_TRUSTS_PROBATE_LIST_CHD_DAILY_CAUSE_LIST;
import static uk.gov.hmcts.reform.pip.model.publication.ListType.REVENUE_LIST_CHD_DAILY_CAUSE_LIST;
import static uk.gov.hmcts.reform.pip.model.publication.ListType.RPT_EASTERN_WEEKLY_HEARING_LIST;
import static uk.gov.hmcts.reform.pip.model.publication.ListType.RPT_LONDON_WEEKLY_HEARING_LIST;
import static uk.gov.hmcts.reform.pip.model.publication.ListType.RPT_MIDLANDS_WEEKLY_HEARING_LIST;
import static uk.gov.hmcts.reform.pip.model.publication.ListType.RPT_NORTHERN_WEEKLY_HEARING_LIST;
import static uk.gov.hmcts.reform.pip.model.publication.ListType.RPT_SOUTHERN_WEEKLY_HEARING_LIST;
import static uk.gov.hmcts.reform.pip.model.publication.ListType.SENIOR_COURTS_COSTS_OFFICE_DAILY_CAUSE_LIST;
import static uk.gov.hmcts.reform.pip.model.publication.ListType.SIAC_WEEKLY_HEARING_LIST;
import static uk.gov.hmcts.reform.pip.model.publication.ListType.SSCS_LONDON_DAILY_HEARING_LIST;
import static uk.gov.hmcts.reform.pip.model.publication.ListType.SSCS_MIDLANDS_DAILY_HEARING_LIST;
import static uk.gov.hmcts.reform.pip.model.publication.ListType.SSCS_NORTH_EAST_DAILY_HEARING_LIST;
import static uk.gov.hmcts.reform.pip.model.publication.ListType.SSCS_NORTH_WEST_DAILY_HEARING_LIST;
import static uk.gov.hmcts.reform.pip.model.publication.ListType.SSCS_SCOTLAND_DAILY_HEARING_LIST;
import static uk.gov.hmcts.reform.pip.model.publication.ListType.SSCS_SOUTH_EAST_DAILY_HEARING_LIST;
import static uk.gov.hmcts.reform.pip.model.publication.ListType.SSCS_WALES_AND_SOUTH_WEST_DAILY_HEARING_LIST;
import static uk.gov.hmcts.reform.pip.model.publication.ListType.TECHNOLOGY_AND_CONSTRUCTION_COURT_KB_DAILY_CAUSE_LIST;
import static uk.gov.hmcts.reform.pip.model.publication.ListType.UT_AAC_DAILY_HEARING_LIST;
import static uk.gov.hmcts.reform.pip.model.publication.ListType.UT_IAC_JR_BIRMINGHAM_DAILY_HEARING_LIST;
import static uk.gov.hmcts.reform.pip.model.publication.ListType.UT_IAC_JR_CARDIFF_DAILY_HEARING_LIST;
import static uk.gov.hmcts.reform.pip.model.publication.ListType.UT_IAC_JR_LONDON_DAILY_HEARING_LIST;
import static uk.gov.hmcts.reform.pip.model.publication.ListType.UT_IAC_JR_MANCHESTER_DAILY_HEARING_LIST;
import static uk.gov.hmcts.reform.pip.model.publication.ListType.UT_IAC_STATUTORY_APPEALS_DAILY_HEARING_LIST;
import static uk.gov.hmcts.reform.pip.model.publication.ListType.UT_LC_DAILY_HEARING_LIST;
import static uk.gov.hmcts.reform.pip.model.publication.ListType.UT_T_AND_CC_DAILY_HEARING_LIST;
import static uk.gov.hmcts.reform.pip.model.publication.ListType.WPAFCC_WEEKLY_HEARING_LIST;

@SuppressWarnings("PMD.ExcessiveImports")
public final class NonStrategicListFormatter {
    private static final String DATE = "date";
    private static final String TIME = "time";
    private static final String HEARING_TIME = "hearingTime";

    private static final Map<ListType, Map<String, Function<String, String>>> LIST_TYPE_MAP = Map.ofEntries(
        Map.entry(CST_WEEKLY_HEARING_LIST,
                  Map.of(DATE, NonStrategicFieldFormattingHelper::formatDateField)),
        Map.entry(PHT_WEEKLY_HEARING_LIST,
                  Map.of(DATE, NonStrategicFieldFormattingHelper::formatDateField)),
        Map.entry(GRC_WEEKLY_HEARING_LIST,
                  Map.of(DATE, NonStrategicFieldFormattingHelper::formatDateField,
                         HEARING_TIME, NonStrategicFieldFormattingHelper::formatTimeField)),
        Map.entry(WPAFCC_WEEKLY_HEARING_LIST,
                  Map.of(DATE, NonStrategicFieldFormattingHelper::formatDateField,
                         HEARING_TIME, NonStrategicFieldFormattingHelper::formatTimeField)),
        Map.entry(UT_IAC_JR_LONDON_DAILY_HEARING_LIST,
                  Map.of(HEARING_TIME, NonStrategicFieldFormattingHelper::formatTimeField)),
        Map.entry(UT_IAC_JR_MANCHESTER_DAILY_HEARING_LIST,
                  Map.of(HEARING_TIME, NonStrategicFieldFormattingHelper::formatTimeField)),
        Map.entry(UT_IAC_JR_BIRMINGHAM_DAILY_HEARING_LIST,
                  Map.of(HEARING_TIME, NonStrategicFieldFormattingHelper::formatTimeField)),
        Map.entry(UT_IAC_JR_CARDIFF_DAILY_HEARING_LIST,
                  Map.of(HEARING_TIME, NonStrategicFieldFormattingHelper::formatTimeField)),
        Map.entry(UT_IAC_STATUTORY_APPEALS_DAILY_HEARING_LIST,
                  Map.of(HEARING_TIME, NonStrategicFieldFormattingHelper::formatTimeField)),
        Map.entry(SIAC_WEEKLY_HEARING_LIST,
                  Map.of(DATE, NonStrategicFieldFormattingHelper::formatDateField,
                         TIME, NonStrategicFieldFormattingHelper::formatTimeField)),
        Map.entry(POAC_WEEKLY_HEARING_LIST,
                  Map.of(DATE, NonStrategicFieldFormattingHelper::formatDateField,
                         TIME, NonStrategicFieldFormattingHelper::formatTimeField)),
        Map.entry(PAAC_WEEKLY_HEARING_LIST,
                  Map.of(DATE, NonStrategicFieldFormattingHelper::formatDateField,
                         TIME, NonStrategicFieldFormattingHelper::formatTimeField)),
        Map.entry(FTT_TAX_WEEKLY_HEARING_LIST,
                  Map.of(DATE, NonStrategicFieldFormattingHelper::formatDateField,
                         HEARING_TIME, NonStrategicFieldFormattingHelper::formatTimeField)),
        Map.entry(FTT_LR_WEEKLY_HEARING_LIST,
                  Map.of(DATE, NonStrategicFieldFormattingHelper::formatDateField,
                         HEARING_TIME, NonStrategicFieldFormattingHelper::formatTimeField)),
        Map.entry(RPT_EASTERN_WEEKLY_HEARING_LIST,
                  Map.of(DATE, NonStrategicFieldFormattingHelper::formatDateField,
                         TIME, NonStrategicFieldFormattingHelper::formatTimeField)),
        Map.entry(RPT_LONDON_WEEKLY_HEARING_LIST,
                  Map.of(DATE, NonStrategicFieldFormattingHelper::formatDateField,
                         TIME, NonStrategicFieldFormattingHelper::formatTimeField)),
        Map.entry(RPT_MIDLANDS_WEEKLY_HEARING_LIST,
                  Map.of(DATE, NonStrategicFieldFormattingHelper::formatDateField,
                         TIME, NonStrategicFieldFormattingHelper::formatTimeField)),
        Map.entry(RPT_NORTHERN_WEEKLY_HEARING_LIST,
                  Map.of(DATE, NonStrategicFieldFormattingHelper::formatDateField,
                         TIME, NonStrategicFieldFormattingHelper::formatTimeField)),
        Map.entry(RPT_SOUTHERN_WEEKLY_HEARING_LIST,
                  Map.of(DATE, NonStrategicFieldFormattingHelper::formatDateField,
                         TIME, NonStrategicFieldFormattingHelper::formatTimeField)),
        Map.entry(UT_T_AND_CC_DAILY_HEARING_LIST,
                  Map.of(TIME, NonStrategicFieldFormattingHelper::formatTimeField)),
        Map.entry(UT_LC_DAILY_HEARING_LIST,
                  Map.of(TIME, NonStrategicFieldFormattingHelper::formatTimeField)),
        Map.entry(UT_AAC_DAILY_HEARING_LIST,
                  Map.of(TIME, NonStrategicFieldFormattingHelper::formatTimeField)),
        Map.entry(AST_DAILY_HEARING_LIST,
                  Map.of(HEARING_TIME, NonStrategicFieldFormattingHelper::formatTimeField)),
        Map.entry(SSCS_MIDLANDS_DAILY_HEARING_LIST,
                  Map.of(HEARING_TIME, NonStrategicFieldFormattingHelper::formatTimeField)),
        Map.entry(SSCS_SOUTH_EAST_DAILY_HEARING_LIST,
                  Map.of(HEARING_TIME, NonStrategicFieldFormattingHelper::formatTimeField)),
        Map.entry(SSCS_WALES_AND_SOUTH_WEST_DAILY_HEARING_LIST,
                  Map.of(HEARING_TIME, NonStrategicFieldFormattingHelper::formatTimeField)),
        Map.entry(SSCS_SCOTLAND_DAILY_HEARING_LIST,
                  Map.of(HEARING_TIME, NonStrategicFieldFormattingHelper::formatTimeField)),
        Map.entry(SSCS_NORTH_EAST_DAILY_HEARING_LIST,
                  Map.of(HEARING_TIME, NonStrategicFieldFormattingHelper::formatTimeField)),
        Map.entry(SSCS_NORTH_WEST_DAILY_HEARING_LIST,
                  Map.of(HEARING_TIME, NonStrategicFieldFormattingHelper::formatTimeField)),
        Map.entry(SSCS_LONDON_DAILY_HEARING_LIST,
                  Map.of(HEARING_TIME, NonStrategicFieldFormattingHelper::formatTimeField)),
        Map.entry(LONDON_ADMINISTRATIVE_COURT_DAILY_CAUSE_LIST,
                  Map.of(TIME, NonStrategicFieldFormattingHelper::formatTimeField)),
        Map.entry(PLANNING_COURT_DAILY_CAUSE_LIST,
                  Map.of(TIME, NonStrategicFieldFormattingHelper::formatTimeField)),
        Map.entry(COUNTY_COURT_LONDON_CIVIL_DAILY_CAUSE_LIST,
                  Map.of(TIME, NonStrategicFieldFormattingHelper::formatTimeField)),
        Map.entry(CIVIL_COURTS_RCJ_DAILY_CAUSE_LIST,
                  Map.of(TIME, NonStrategicFieldFormattingHelper::formatTimeField)),
        Map.entry(COURT_OF_APPEAL_CRIMINAL_DAILY_CAUSE_LIST,
                  Map.of(TIME, NonStrategicFieldFormattingHelper::formatTimeField)),
        Map.entry(FAMILY_DIVISION_HIGH_COURT_DAILY_CAUSE_LIST,
                  Map.of(TIME, NonStrategicFieldFormattingHelper::formatTimeField)),
        Map.entry(KINGS_BENCH_DIVISION_DAILY_CAUSE_LIST,
                  Map.of(TIME, NonStrategicFieldFormattingHelper::formatTimeField)),
        Map.entry(KINGS_BENCH_MASTERS_DAILY_CAUSE_LIST,
                  Map.of(TIME, NonStrategicFieldFormattingHelper::formatTimeField)),
        Map.entry(SENIOR_COURTS_COSTS_OFFICE_DAILY_CAUSE_LIST,
                  Map.of(TIME, NonStrategicFieldFormattingHelper::formatTimeField)),
        Map.entry(MAYOR_AND_CITY_CIVIL_DAILY_CAUSE_LIST,
                  Map.of(TIME, NonStrategicFieldFormattingHelper::formatTimeField)),
        Map.entry(COURT_OF_APPEAL_CIVIL_DAILY_CAUSE_LIST,
                  Map.of(TIME, NonStrategicFieldFormattingHelper::formatTimeField,
                         DATE, NonStrategicFieldFormattingHelper::formatDateField)),
        Map.entry(INTELLECTUAL_PROPERTY_AND_ENTERPRISE_COURT_DAILY_CAUSE_LIST,
                  Map.of(TIME, NonStrategicFieldFormattingHelper::formatTimeField)),
        Map.entry(INTELLECTUAL_PROPERTY_LIST_CHD_DAILY_CAUSE_LIST,
                  Map.of(TIME, NonStrategicFieldFormattingHelper::formatTimeField)),
        Map.entry(LONDON_CIRCUIT_COMMERCIAL_COURT_KB_DAILY_CAUSE_LIST,
                  Map.of(TIME, NonStrategicFieldFormattingHelper::formatTimeField)),
        Map.entry(PATENTS_COURT_CHD_DAILY_CAUSE_LIST,
                  Map.of(TIME, NonStrategicFieldFormattingHelper::formatTimeField)),
        Map.entry(PENSIONS_LIST_CHD_DAILY_CAUSE_LIST,
                  Map.of(TIME, NonStrategicFieldFormattingHelper::formatTimeField)),
        Map.entry(PROPERTY_TRUSTS_PROBATE_LIST_CHD_DAILY_CAUSE_LIST,
                  Map.of(TIME, NonStrategicFieldFormattingHelper::formatTimeField)),
        Map.entry(REVENUE_LIST_CHD_DAILY_CAUSE_LIST,
                  Map.of(TIME, NonStrategicFieldFormattingHelper::formatTimeField)),
        Map.entry(TECHNOLOGY_AND_CONSTRUCTION_COURT_KB_DAILY_CAUSE_LIST,
                  Map.of(TIME, NonStrategicFieldFormattingHelper::formatTimeField)),
        Map.entry(ADMIRALTY_COURT_KB_DAILY_CAUSE_LIST,
                  Map.of(TIME, NonStrategicFieldFormattingHelper::formatTimeField)),
        Map.entry(BUSINESS_LIST_CHD_DAILY_CAUSE_LIST,
                  Map.of(TIME, NonStrategicFieldFormattingHelper::formatTimeField)),
        Map.entry(CHANCERY_APPEALS_CHD_DAILY_CAUSE_LIST,
                  Map.of(TIME, NonStrategicFieldFormattingHelper::formatTimeField)),
        Map.entry(COMMERCIAL_COURT_KB_DAILY_CAUSE_LIST,
                  Map.of(TIME, NonStrategicFieldFormattingHelper::formatTimeField)),
        Map.entry(COMPANIES_WINDING_UP_CHD_DAILY_CAUSE_LIST,
                  Map.of(TIME, NonStrategicFieldFormattingHelper::formatTimeField)),
        Map.entry(COMPETITION_LIST_CHD_DAILY_CAUSE_LIST,
                  Map.of(TIME, NonStrategicFieldFormattingHelper::formatTimeField)),
        Map.entry(FINANCIAL_LIST_CHD_KB_DAILY_CAUSE_LIST,
                  Map.of(TIME, NonStrategicFieldFormattingHelper::formatTimeField)),
        Map.entry(INSOLVENCY_AND_COMPANIES_COURT_CHD_DAILY_CAUSE_LIST,
                  Map.of(TIME, NonStrategicFieldFormattingHelper::formatTimeField)),
        Map.entry(INTERIM_APPLICATIONS_CHD_DAILY_CAUSE_LIST,
                  Map.of(TIME, NonStrategicFieldFormattingHelper::formatTimeField))
    );

    private NonStrategicListFormatter() {
    }

    public static Optional<Map<String, Function<String, String>>> getListTypeFormatter(ListType listType) {
        if (LIST_TYPE_MAP.containsKey(listType)) {
            return Optional.of(LIST_TYPE_MAP.get(listType));
        }
        return Optional.empty();
    }

    public static List<Map<String, String>> formatAllFields(List<Map<String, String>> data, ListType listType) {
        List<Map<String, String>> formattedData = new ArrayList<>();
        data.forEach(hearing -> {
                Map<String, String> formattedDataEntry = new ConcurrentHashMap<>();
                hearing.forEach((k, v) -> {
                    Optional<Function<String, String>> fieldFormatter = getFieldFormatter(
                        listType, k
                    );

                    if (fieldFormatter.isPresent()) {
                        String formattedValue = fieldFormatter.get().apply(v);
                        formattedDataEntry.put(k, formattedValue);
                    } else {
                        formattedDataEntry.put(k, v);
                    }
                });
                formattedData.add(formattedDataEntry);
            }
        );
        return formattedData;
    }

    public static String formatField(String field, String value, Map<String, Function<String, String>> formatter) {
        if (formatter.containsKey(field)) {
            Function<String, String> fieldFormatter = formatter.get(field);
            return fieldFormatter.apply(value);
        }
        return value;
    }

    private static Optional<Function<String, String>> getFieldFormatter(ListType listType, String field) {
        Optional<Map<String, Function<String, String>>> listTypeFormatters = getListTypeFormatter(listType);

        if (listTypeFormatters.isPresent() && listTypeFormatters.get().containsKey(field)) {
            Function<String, String> fieldFormatter = listTypeFormatters.get().get(field);
            return Optional.of(fieldFormatter);
        }
        return Optional.empty();
    }
}
