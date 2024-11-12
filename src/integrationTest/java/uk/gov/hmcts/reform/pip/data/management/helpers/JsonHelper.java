package uk.gov.hmcts.reform.pip.data.management.helpers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.math.NumberUtils;

@Slf4j
public final class JsonHelper {

    private JsonHelper() {
        throw new UnsupportedOperationException();
    }

    @SuppressWarnings("PMD.AvoidCatchingNPE")
    public static JsonNode safeRemoveNode(String jsonPath, JsonNode node) {
        String[] stringArray = jsonPath.split("\\.");
        JsonNode outputNode = node;
        int index = 0;
        try {
            for (String arg : stringArray) {
                if (index == stringArray.length - 1) {
                    ((ObjectNode) outputNode).remove(arg);
                    return outputNode;
                }
                if (NumberUtils.isCreatable(arg)) {
                    outputNode = outputNode.get(Integer.parseInt(arg));
                } else {
                    outputNode = outputNode.get(arg);
                }
                index += 1;
            }
            return outputNode;
        } catch (NullPointerException e) {
            log.error("Parsing failed for path " + jsonPath + ", specifically " + stringArray[index]);
            return node;
        }
    }
}
