package uk.gov.hmcts.reform.pip.data.management.service.helpers;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.util.Map.Entry.comparingByKey;

/**
 * Class for static utility methods assisting with json->html->pdf issues.
 */
@Slf4j
public final class GeneralHelper {
    private GeneralHelper() {
        throw new UnsupportedOperationException();
    }

    public static String stringDelimiter(String text, String delimiter) {
        return text.isEmpty() ? "" : delimiter;
    }

    public static String findAndReturnNodeText(JsonNode node, String nodeName) {
        if (node != null && node.has(nodeName)) {
            return node.get(nodeName).asText();
        }
        return "";
    }

    public static String trimAnyCharacterFromStringEnd(String text) {
        return StringUtils.isBlank(text) ? "" : text.trim().replaceAll(",$", "");
    }

    public static void appendToStringBuilder(StringBuilder builder, String text, JsonNode node,
                                             String nodeName) {
        appendToStringBuilderWithPrefix(builder, text, node, nodeName, "\n");
    }

    public static void appendToStringBuilderWithPrefix(StringBuilder builder, String text, JsonNode node,
                                                       String nodeName, String prefix) {
        builder.append(prefix)
            .append(text)
            .append(findAndReturnNodeText(node, nodeName));
    }

    public static String formatNodeArray(JsonNode node, String nodeName, String delimiter) {
        if (node.has(nodeName)) {
            List<String> values = new ArrayList<>();
            node.get(nodeName)
                .forEach(n -> values.add(n.asText()));

            return convertToDelimitedString(values, delimiter);
        }
        return "";
    }

    public static String convertToDelimitedString(List<String> values, String delimiter) {
        return values.stream()
            .filter(StringUtils::isNotBlank)
            .collect(Collectors.joining(delimiter));
    }

    public static String safeGet(String jsonPath, JsonNode node) {
        return safeGetNode(jsonPath, node) == null ? "" : safeGetNode(jsonPath, node).asText();
    }

    @SuppressWarnings("PMD.AvoidCatchingNPE")
    public static JsonNode safeGetNode(String jsonPath, JsonNode node) {
        String[] stringArray = jsonPath.split("\\.");
        JsonNode outputNode = node;
        int index = -1;
        try {
            for (String arg : stringArray) {
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

    public static List<String> findUniqueDateAndSort(Map<Date, String> sittingDateTimes) {
        Map<Date,String> sortedSittingDateTimes = sortByDateTime(sittingDateTimes);

        List<String> uniqueDates = new ArrayList<>();
        for (String value : sortedSittingDateTimes.values()) {
            if (!uniqueDates.contains(value)) {
                uniqueDates.add(value);
            }
        }
        return uniqueDates;
    }

    private static Map<Date,String> sortByDateTime(Map<Date, String> dateTimeValueString) {
        return dateTimeValueString.entrySet().stream()
            .sorted(comparingByKey(Comparator.naturalOrder()))
            .collect(Collectors.toMap(
                Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));
    }
}
