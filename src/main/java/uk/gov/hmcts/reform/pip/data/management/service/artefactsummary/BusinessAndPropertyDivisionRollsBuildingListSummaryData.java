package uk.gov.hmcts.reform.pip.data.management.service.artefactsummary;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import uk.gov.hmcts.reform.pip.data.management.service.helpers.LanguageResourceHelper;
import uk.gov.hmcts.reform.pip.data.management.service.helpers.NonStrategicListFormatter;
import uk.gov.hmcts.reform.pip.model.publication.Language;
import uk.gov.hmcts.reform.pip.model.publication.ListType;

import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class BusinessAndPropertyDivisionRollsBuildingListSummaryData implements ArtefactSummaryData {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

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

            List<Map<String, String>> summaryCases = NonStrategicListSummaryData.buildCases(data, listType);

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
