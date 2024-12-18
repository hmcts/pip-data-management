package uk.gov.hmcts.reform.pip.data.management.service.artefactsummary;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import uk.gov.hmcts.reform.pip.data.management.service.helpers.NonStrategicListFormatter;
import uk.gov.hmcts.reform.pip.model.publication.ListType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import static uk.gov.hmcts.reform.pip.model.publication.ListType.CST_WEEKLY_HEARING_LIST;
import static uk.gov.hmcts.reform.pip.model.publication.ListType.PHT_WEEKLY_HEARING_LIST;

@SuppressWarnings("PMD.UseConcurrentHashMap")
public class NonStrategicListSummaryData implements ArtefactSummaryData {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private static final Map<ListType, List<String>> LIST_TYPE_SUMMARY_FIELDS = Map.of(
        CST_WEEKLY_HEARING_LIST, List.of("date", "caseName"),
        PHT_WEEKLY_HEARING_LIST, List.of("date", "caseName")
    );

    private final ListType listType;

    public NonStrategicListSummaryData(ListType listType) {
        this.listType = listType;
    }

    @Override
    public Map<String, List<Map<String, String>>> get(JsonNode payload) {
        List<Map<String, String>> data = OBJECT_MAPPER.convertValue(payload, new TypeReference<>(){});
        Optional<Map<String, Function<String, String>>> listTypeFormatter = NonStrategicListFormatter
            .getListTypeFormatter(listType);

        List<Map<String, String>> summaryCases = new ArrayList<>();
        data.forEach(hearing -> {
            Map<String, String> summaryCase = new LinkedHashMap<>();
            if (LIST_TYPE_SUMMARY_FIELDS.containsKey(listType)) {
                List<String> summaryFields = LIST_TYPE_SUMMARY_FIELDS.get(listType);
                summaryFields.forEach(field -> {
                    String formattedKey = StringUtils.capitalize(
                        StringUtils.join(StringUtils.splitByCharacterTypeCamelCase(field), StringUtils.SPACE)
                            .toLowerCase(Locale.UK)
                    );

                    String formattedValue = listTypeFormatter.isPresent() && listTypeFormatter.get().containsKey(field)
                        ? NonStrategicListFormatter.formatField(field, hearing.get(field), listTypeFormatter.get())
                        : hearing.get(field);
                    summaryCase.put(formattedKey, formattedValue);
                });
                summaryCases.add(summaryCase);
            }
        });

        return Collections.singletonMap(null, summaryCases);
    }
}
