package uk.gov.hmcts.reform.pip.data.management.service.artefactsummary;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.pip.data.management.service.ListConversionFactory;
import uk.gov.hmcts.reform.pip.model.publication.ListType;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

@ActiveProfiles("test")
class CrownWarnedPddaListSummaryDataTest {
    private static final String SUMMARY_SECTIONS_MESSAGE = "Summary sections count does not match";
    private static final String SUMMARY_CASES_MESSAGE = "Summary cases count does not match";
    private static final String SUMMARY_FIELDS_MESSAGE = "Summary fields count does not match";
    private static final String SUMMARY_FIELD_KEY_MESSAGE = "Summary field key does not match";
    private static final String SUMMARY_FIELD_VALUE_MESSAGE = "Summary field value does not match";

    @Test
    void testCrownWarnedPddaListSummary() throws IOException {
        Map<String, List<Map<String, String>>> output;
        try (InputStream mockFile = Thread.currentThread().getContextClassLoader()
            .getResourceAsStream("mocks/crownWarnedPddaList.json")) {
            JsonNode payload = new ObjectMapper().readTree(new String(mockFile.readAllBytes()));
            output = new ListConversionFactory().getArtefactSummaryData(ListType.CROWN_WARNED_PDDA_LIST).get()
                .get(payload);
        }

        SoftAssertions softly = new SoftAssertions();

        softly.assertThat(output)
            .as(SUMMARY_SECTIONS_MESSAGE)
            .hasSize(1);

        List<Map<String, String>> summaryCases = output.get(null);
        softly.assertThat(summaryCases)
            .as(SUMMARY_CASES_MESSAGE)
            .hasSize(2);

        Map<String, String> summaryFields = summaryCases.get(0);
        softly.assertThat(summaryFields)
            .as(SUMMARY_FIELDS_MESSAGE)
            .hasSize(4);

        assertSummaryFieldKeys(softly, summaryFields);
        assertSummaryFieldValues(softly, summaryFields);

        softly.assertAll();
    }

    private void assertSummaryFieldKeys(SoftAssertions softly, Map<String, String> summaryFields) {
        List<String> keys = summaryFields.keySet()
            .stream()
            .toList();

        softly.assertThat(keys.get(0))
            .as(SUMMARY_FIELD_KEY_MESSAGE)
            .isEqualTo("Fixed for");

        softly.assertThat(keys.get(1))
            .as(SUMMARY_FIELD_KEY_MESSAGE)
            .isEqualTo("Case reference");

        softly.assertThat(keys.get(2))
            .as(SUMMARY_FIELD_KEY_MESSAGE)
            .isEqualTo("Defendant name(s)");

        softly.assertThat(keys.get(3))
            .as(SUMMARY_FIELD_KEY_MESSAGE)
            .isEqualTo("Prosecuting authority");
    }

    private void assertSummaryFieldValues(SoftAssertions softly, Map<String, String> summaryFields) {
        List<String> values = summaryFields.values()
            .stream()
            .toList();

        softly.assertThat(values.get(0))
            .as(SUMMARY_FIELD_VALUE_MESSAGE)
            .isEqualTo("01/01/2024");

        softly.assertThat(values.get(1))
            .as(SUMMARY_FIELD_VALUE_MESSAGE)
            .isEqualTo("T00112233");

        softly.assertThat(values.get(2))
            .as(SUMMARY_FIELD_VALUE_MESSAGE)
            .isEqualTo("TestDefendantRequestedName");

        softly.assertThat(values.get(3))
            .as(SUMMARY_FIELD_VALUE_MESSAGE)
            .isEqualTo("Crown Prosecution Service");
    }
}
