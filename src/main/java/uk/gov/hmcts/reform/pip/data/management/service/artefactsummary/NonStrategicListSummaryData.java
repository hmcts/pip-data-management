package uk.gov.hmcts.reform.pip.data.management.service.artefactsummary;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import uk.gov.hmcts.reform.pip.data.management.service.helpers.NonStrategicListFormatter;
import uk.gov.hmcts.reform.pip.model.publication.ListType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class NonStrategicListSummaryData implements ArtefactSummaryData {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private final ListType listType;

    public NonStrategicListSummaryData(ListType listType) {
        this.listType = listType;
    }

    @Override
    public Map<String, List<Map<String, String>>> get(JsonNode payload) {
        List<Map<String, String>> data = new ArrayList<>();

        if (payload.isObject()) {
            Iterator<String> fieldNames = payload.fieldNames();

            while (fieldNames.hasNext()) {
                String fieldName = fieldNames.next();
                JsonNode fieldNode = payload.get(fieldName);

                if (fieldNode.isArray()) {
                    data = OBJECT_MAPPER.convertValue(
                        fieldNode,
                        new TypeReference<>() {}
                    );
                    break;
                }
            }
        } else if (payload.isArray()) {
            data = OBJECT_MAPPER.convertValue(
                payload,
                new TypeReference<>() {}
            );
        }

        return Collections.singletonMap(null, NonStrategicListFormatter.buildCases(data, listType));
    }
}
