package uk.gov.hmcts.reform.pip.data.management.service.helpers;

import uk.gov.hmcts.reform.pip.model.publication.ListType;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

import static uk.gov.hmcts.reform.pip.model.publication.ListType.CST_WEEKLY_HEARING_LIST;
import static uk.gov.hmcts.reform.pip.model.publication.ListType.FTT_LR_WEEKLY_HEARING_LIST;
import static uk.gov.hmcts.reform.pip.model.publication.ListType.FTT_TAX_WEEKLY_HEARING_LIST;
import static uk.gov.hmcts.reform.pip.model.publication.ListType.GRC_WEEKLY_HEARING_LIST;
import static uk.gov.hmcts.reform.pip.model.publication.ListType.PAAC_WEEKLY_HEARING_LIST;
import static uk.gov.hmcts.reform.pip.model.publication.ListType.PHT_WEEKLY_HEARING_LIST;
import static uk.gov.hmcts.reform.pip.model.publication.ListType.POAC_WEEKLY_HEARING_LIST;
import static uk.gov.hmcts.reform.pip.model.publication.ListType.RPT_EASTERN_WEEKLY_HEARING_LIST;
import static uk.gov.hmcts.reform.pip.model.publication.ListType.RPT_LONDON_WEEKLY_HEARING_LIST;
import static uk.gov.hmcts.reform.pip.model.publication.ListType.RPT_MIDLANDS_WEEKLY_HEARING_LIST;
import static uk.gov.hmcts.reform.pip.model.publication.ListType.RPT_NORTHERN_WEEKLY_HEARING_LIST;
import static uk.gov.hmcts.reform.pip.model.publication.ListType.RPT_SOUTHERN_WEEKLY_HEARING_LIST;
import static uk.gov.hmcts.reform.pip.model.publication.ListType.SIAC_WEEKLY_HEARING_LIST;
import static uk.gov.hmcts.reform.pip.model.publication.ListType.SSCS_LONDON_DAILY_HEARING_LIST;
import static uk.gov.hmcts.reform.pip.model.publication.ListType.SSCS_MIDLANDS_DAILY_HEARING_LIST;
import static uk.gov.hmcts.reform.pip.model.publication.ListType.SSCS_NORTH_EAST_DAILY_HEARING_LIST;
import static uk.gov.hmcts.reform.pip.model.publication.ListType.SSCS_NORTH_WEST_DAILY_HEARING_LIST;
import static uk.gov.hmcts.reform.pip.model.publication.ListType.SSCS_SCOTLAND_DAILY_HEARING_LIST;
import static uk.gov.hmcts.reform.pip.model.publication.ListType.SSCS_SOUTH_EAST_DAILY_HEARING_LIST;
import static uk.gov.hmcts.reform.pip.model.publication.ListType.SSCS_WALES_AND_SOUTH_WEST_DAILY_HEARING_LIST;
import static uk.gov.hmcts.reform.pip.model.publication.ListType.UT_AAC_DAILY_HEARING_LIST;
import static uk.gov.hmcts.reform.pip.model.publication.ListType.UT_IAC_JR_BIRMINGHAM_DAILY_HEARING_LIST;
import static uk.gov.hmcts.reform.pip.model.publication.ListType.UT_IAC_JR_CARDIFF_DAILY_HEARING_LIST;
import static uk.gov.hmcts.reform.pip.model.publication.ListType.UT_IAC_JR_LONDON_DAILY_HEARING_LIST;
import static uk.gov.hmcts.reform.pip.model.publication.ListType.UT_IAC_JR_MANCHESTER_DAILY_HEARING_LIST;
import static uk.gov.hmcts.reform.pip.model.publication.ListType.UT_IAC_STATUTORY_APPEALS_DAILY_HEARING_LIST;
import static uk.gov.hmcts.reform.pip.model.publication.ListType.UT_LC_DAILY_HEARING_LIST;
import static uk.gov.hmcts.reform.pip.model.publication.ListType.UT_T_AND_CC_DAILY_HEARING_LIST;
import static uk.gov.hmcts.reform.pip.model.publication.ListType.WPAFCC_WEEKLY_HEARING_LIST;

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
                  Map.of(HEARING_TIME, NonStrategicFieldFormattingHelper::formatTimeField))
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
