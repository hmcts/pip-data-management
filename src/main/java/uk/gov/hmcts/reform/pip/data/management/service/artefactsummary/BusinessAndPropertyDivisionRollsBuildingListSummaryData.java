package uk.gov.hmcts.reform.pip.data.management.service.artefactsummary;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import uk.gov.hmcts.reform.pip.data.management.service.helpers.LanguageResourceHelper;
import uk.gov.hmcts.reform.pip.data.management.service.helpers.NonStrategicListFormatter;
import uk.gov.hmcts.reform.pip.model.publication.Language;
import uk.gov.hmcts.reform.pip.model.publication.ListType;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import static uk.gov.hmcts.reform.pip.model.publication.ListType.BUSINESS_AND_PROPERTY_DIVISION_ROLLS_BUILDING_DAILY_CAUSE_LIST;

public class BusinessAndPropertyDivisionRollsBuildingListSummaryData implements ArtefactSummaryData {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final String TIME = "time";
    private static final String CASE_NAME = "caseName";
    private static final String CASE_NUMBER = "caseNumber";
    private static final Map<ListType, List<String>> LIST_TYPE_SUMMARY_FIELDS = Map.ofEntries(
        Map.entry(BUSINESS_AND_PROPERTY_DIVISION_ROLLS_BUILDING_DAILY_CAUSE_LIST, List.of(TIME, CASE_NUMBER, CASE_NAME))
    );

    private final ListType listType;

    public BusinessAndPropertyDivisionRollsBuildingListSummaryData(ListType listType) {
        this.listType = listType;
    }

    @Override
    public Map<String, List<Map<String, String>>> get(JsonNode payload) {
        Map<String, List<Map<String, String>>> result = new LinkedHashMap<>();
        Iterator<Map.Entry<String, JsonNode>> fields = payload.fields();

        while (fields.hasNext()) {
            Map.Entry<String, JsonNode> entry = fields.next();
            String sectionName = entry.getKey();
            JsonNode section = entry.getValue();

            if (!section.isArray()) {
                continue;
            }

            List<Map<String, String>> data =
                OBJECT_MAPPER.convertValue(
                    section,
                    new TypeReference<List<Map<String, String>>>() {}
                );

            List<Map<String, String>> summaryCases = buildSummaryCases(data);

            if (summaryCases.isEmpty()) {
                Map<String, String> noHearings = new LinkedHashMap<>();
                noHearings.put("", "No hearings scheduled for this day");
                summaryCases.add(noHearings);
            }

            result.put(
                getListFriendlyName(sectionName),
                summaryCases
            );
        }

        return result;
    }

    private List<Map<String, String>> buildSummaryCases(
        List<Map<String, String>> data
    ) {

        Optional<Map<String, Function<String, String>>> listTypeFormatter =
            NonStrategicListFormatter.getListTypeFormatter(listType);

        List<Map<String, String>> summaryCases = new ArrayList<>();

        data.forEach(hearing -> {

            Map<String, String> summaryCase = new LinkedHashMap<>();

            if (LIST_TYPE_SUMMARY_FIELDS.containsKey(listType)) {

                List<String> summaryFields =
                    LIST_TYPE_SUMMARY_FIELDS.get(listType);

                summaryFields.forEach(field -> {

                    String formattedKey = StringUtils.capitalize(
                        StringUtils.join(
                            StringUtils.splitByCharacterTypeCamelCase(field),
                            StringUtils.SPACE
                        ).toLowerCase(Locale.UK)
                    );

                    String formattedValue =
                        listTypeFormatter.isPresent()
                            && listTypeFormatter.get().containsKey(field)
                            ? NonStrategicListFormatter.formatField(
                            field,
                            hearing.get(field),
                            listTypeFormatter.get()
                        )
                            : hearing.get(field);

                    summaryCase.put(formattedKey, formattedValue);
                });

                summaryCases.add(summaryCase);
            }
        });

        return summaryCases;
    }

    private String getListFriendlyName(String key) {
        try {
            Map<String, Object> languageResources =
                LanguageResourceHelper.readResourcesFromPath(
                    "non-strategic/businessAndPropertyDivisionRollsBuildingDailyCauseList",
                    Language.ENGLISH
                );

            @SuppressWarnings("unchecked")
            List<Map<String, String>> listTypes =
                (List<Map<String, String>>) languageResources.get("listTypes");

            return listTypes.stream()
                .filter(listType -> key.equals(listType.get("key")))
                .map(listType -> listType.get("label"))
                .findFirst()
                .orElseThrow(() ->
                                 new IllegalArgumentException("Unknown list type: " + key)
                );

        } catch (IOException e) {
            throw new IllegalStateException(
                "Unable to load list types resource", e
            );
        }
    }

}
