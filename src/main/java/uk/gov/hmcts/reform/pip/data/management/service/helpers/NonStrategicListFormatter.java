package uk.gov.hmcts.reform.pip.data.management.service.helpers;

import uk.gov.hmcts.reform.pip.model.publication.ListType;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

import static uk.gov.hmcts.reform.pip.model.publication.ListType.CST_WEEKLY_HEARING_LIST;
import static uk.gov.hmcts.reform.pip.model.publication.ListType.PHT_WEEKLY_HEARING_LIST;

public final class NonStrategicListFormatter {
    public static final Map<ListType, Map<String, Function<String, String>>> LIST_TYPE_MAP = Map.of(
        CST_WEEKLY_HEARING_LIST,
        Map.of("date", NonStrategicFieldFormattingHelper::formatDateField),
        PHT_WEEKLY_HEARING_LIST,
        Map.of("date", NonStrategicFieldFormattingHelper::formatDateField)
    );

    private NonStrategicListFormatter() {
    }

    public static List<Map<String, String>> formatFields(List<Map<String, String>> data, ListType listType) {
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

    private static Optional<Function<String, String>> getFieldFormatter(ListType listTYpe, String field) {
        Map<String, Function<String, String>> listTypeFormatters = LIST_TYPE_MAP.get(listTYpe);

        if (listTypeFormatters != null) {
            Function<String, String> fieldFormatter = listTypeFormatters.get(field);

            if (fieldFormatter != null) {
                return Optional.of(fieldFormatter);
            }
        }
        return Optional.empty();
    }
}
