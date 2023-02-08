package uk.gov.hmcts.reform.pip.data.management.helpers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public final class JsonParser {

    private JsonParser() {

    }

    public static String readAttribute(String json, String attribute)
        throws JsonProcessingException {
        try {
            JsonNode node = new ObjectMapper().readTree(json);
            if (!node.isEmpty() && node.has(attribute)) {
                return node.get(attribute).asText();
            }
        } catch (JsonProcessingException e) {
            log.error(String.format("Failed to read json: %s",
                                    e.getMessage()));
            throw e;
        }
        return "";
    }
}
