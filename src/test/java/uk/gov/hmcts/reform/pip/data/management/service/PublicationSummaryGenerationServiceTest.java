package uk.gov.hmcts.reform.pip.data.management.service;

import com.google.common.collect.ImmutableMap;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SuppressWarnings("PMD.UseConcurrentHashMap")
class PublicationSummaryGenerationServiceTest {
    private static final Map<String, String> SUMMARY_FIELDS1 = ImmutableMap.of(
        "key1", "value1",
        "key2", "value2",
        "key3", "value3"
    );

    private static final Map<String, String> SUMMARY_FIELDS2 = ImmutableMap.of(
        "key1", "value4",
        "key2", "value5",
        "key3", "value6"
    );

    private static final Map<String, String> SUMMARY_FIELDS3 = ImmutableMap.of(
        "key1", "value7",
        "key2", "value8",
        "key3", "value9"
    );

    private static final String LINE_SEPARATOR = "---";
    private static final String SUMMARY_MESSAGE = "Output summary does nto match";

    private final PublicationSummaryGenerationService publicationSummaryGenerationService =
        new PublicationSummaryGenerationService();

    @Test
    void testPublicationSummaryGenerationWithoutSectionHeading() {

        List<Map<String, String>> summaryCases = List.of(
            SUMMARY_FIELDS1,
            SUMMARY_FIELDS2,
            SUMMARY_FIELDS3
        );
        Map<String, List<Map<String, String>>> data = Collections.singletonMap(null, summaryCases);

        String output = publicationSummaryGenerationService.generate(data);
        List<String> outputElements = Arrays.stream(output.split("\n"))
            .filter(e -> !e.isEmpty())
            .toList();

        assertThat(outputElements)
            .as(SUMMARY_MESSAGE)
            .containsExactly(
                LINE_SEPARATOR,
                "key1 - value1",
                "key2 - value2",
                "key3 - value3",
                LINE_SEPARATOR,
                "key1 - value4",
                "key2 - value5",
                "key3 - value6",
                LINE_SEPARATOR,
                "key1 - value7",
                "key2 - value8",
                "key3 - value9"
            );
    }

    @Test
    void testPublicationSummaryGenerationWithSectionHeading() {
        Map<String, List<Map<String, String>>> data = new LinkedHashMap<>();
        data.put("heading1", List.of(SUMMARY_FIELDS1));
        data.put("heading2", List.of(SUMMARY_FIELDS2, SUMMARY_FIELDS3));

        String output = publicationSummaryGenerationService.generate(data);
        List<String> outputElements = Arrays.stream(output.split("\n"))
            .filter(e -> !e.isEmpty())
            .toList();

        assertThat(outputElements)
            .as(SUMMARY_MESSAGE)
            .containsExactly(
                LINE_SEPARATOR,
                "##heading1",
                "key1 - value1",
                "key2 - value2",
                "key3 - value3",
                LINE_SEPARATOR,
                "##heading2",
                "key1 - value4",
                "key2 - value5",
                "key3 - value6",
                "key1 - value7",
                "key2 - value8",
                "key3 - value9"
            );
    }
}
