package uk.gov.hmcts.reform.pip.data.management.service.hearingparty;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.pip.data.management.service.ListConversionFactory;
import uk.gov.hmcts.reform.pip.model.publication.ListType;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

@ActiveProfiles("test")
class SscsDailyListArtefactSummaryDataTest {
    private static final String SUMMARY_SECTIONS_MESSAGE = "Summary sections count does not match";
    private static final String SUMMARY_CASES_MESSAGE = "Summary cases count does not match";
    private static final String SUMMARY_FIELDS_MESSAGE = "Summary fields count does not match";
    private static final String SUMMARY_FIELD_KEY_MESSAGE = "Summary field key does not match";
    private static final String SUMMARY_FIELD_VALUE_MESSAGE = "Summary field value does not match";

    @ParameterizedTest
    @EnumSource(value = ListType.class, names = {"SSCS_DAILY_LIST", "SSCS_DAILY_LIST_ADDITIONAL_HEARINGS"})
    void testSscsDailyListSummary(ListType listType) throws IOException {
        Map<String, List<Map<String, String>>> output;
        try (InputStream mockFile = Thread.currentThread().getContextClassLoader()
            .getResourceAsStream("mocks/hearingparty/sscsDailyList.json")) {
            JsonNode payload = new ObjectMapper().readTree(new String(mockFile.readAllBytes()));
            output = new ListConversionFactory().getArtefactSummaryData(listType).get()
                .get(payload);
        }

        SoftAssertions softly = new SoftAssertions();

        softly.assertThat(output)
            .as(SUMMARY_SECTIONS_MESSAGE)
            .hasSize(1);

        List<Map<String, String>> summaryCases = output.get(null);
        softly.assertThat(summaryCases)
            .as(SUMMARY_CASES_MESSAGE)
            .hasSize(4);

        Map<String, String> summaryFields = summaryCases.get(1);
        softly.assertThat(summaryFields)
            .as(SUMMARY_FIELDS_MESSAGE)
            .hasSize(3);

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
            .isEqualTo("Appellant");

        softly.assertThat(keys.get(1))
            .as(SUMMARY_FIELD_KEY_MESSAGE)
            .isEqualTo("Respondent");

        softly.assertThat(keys.get(2))
            .as(SUMMARY_FIELD_KEY_MESSAGE)
            .isEqualTo("Appeal reference");
    }

    private void assertSummaryFieldValues(SoftAssertions softly, Map<String, String> summaryFields) {
        List<String> values = summaryFields.values()
            .stream()
            .toList();

        softly.assertThat(values.get(0))
            .as(SUMMARY_FIELD_VALUE_MESSAGE)
            .isEqualTo("Surname");

        softly.assertThat(values.get(1))
            .as(SUMMARY_FIELD_VALUE_MESSAGE)
            .isEqualTo("Respondent Organisation, Respondent Organisation 2");

        softly.assertThat(values.get(2))
            .as(SUMMARY_FIELD_VALUE_MESSAGE)
            .isEqualTo("12341235");
    }
}
