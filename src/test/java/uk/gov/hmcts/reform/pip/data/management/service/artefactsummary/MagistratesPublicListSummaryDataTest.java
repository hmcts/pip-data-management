package uk.gov.hmcts.reform.pip.data.management.service.artefactsummary;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.IOUtils;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.pip.data.management.service.ListConversionFactory;

import java.io.IOException;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.pip.model.publication.ListType.MAGISTRATES_PUBLIC_LIST;

@ActiveProfiles("test")
class MagistratesPublicListSummaryDataTest {
    private static final String SUMMARY_SECTIONS_MESSAGE = "Summary sections count does not match";
    private static final String SUMMARY_CASES_MESSAGE = "Summary cases count does not match";
    private static final String SUMMARY_FIELDS_MESSAGE = "Summary fields count does not match";
    private static final String SUMMARY_FIELD_KEY_MESSAGE = "Summary field key does not match";
    private static final String SUMMARY_FIELD_VALUE_MESSAGE = "Summary field value does not match";

    @Test
    void testMagistratesPublicListTemplate() throws IOException {
        StringWriter writer = new StringWriter();
        IOUtils.copy(Files.newInputStream(Paths.get(
            "src/test/resources/mocks/",
            "magistratesPublicList.json"
                     )), writer,
                     Charset.defaultCharset()
        );

        JsonNode payload = new ObjectMapper().readTree(writer.toString());
        Map<String, List<Map<String, String>>> output = new ListConversionFactory()
            .getArtefactSummaryData(MAGISTRATES_PUBLIC_LIST).get()
            .get(payload);

        SoftAssertions softly = new SoftAssertions();

        softly.assertThat(output)
            .as(SUMMARY_SECTIONS_MESSAGE)
            .hasSize(1);

        List<Map<String, String>> summaryCases = output.get(null);
        softly.assertThat(summaryCases)
            .as(SUMMARY_CASES_MESSAGE)
            .hasSize(13);

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
            .isEqualTo("Defendant");

        softly.assertThat(keys.get(1))
            .as(SUMMARY_FIELD_KEY_MESSAGE)
            .isEqualTo("Prosecuting authority");

        softly.assertThat(keys.get(2))
            .as(SUMMARY_FIELD_KEY_MESSAGE)
            .isEqualTo("Case reference");

        softly.assertThat(keys.get(3))
            .as(SUMMARY_FIELD_KEY_MESSAGE)
            .isEqualTo("Hearing type");
    }

    private void assertSummaryFieldValues(SoftAssertions softly, Map<String, String> summaryFields) {
        List<String> values = summaryFields.values()
            .stream()
            .toList();

        softly.assertThat(values.get(0))
            .as(SUMMARY_FIELD_VALUE_MESSAGE)
            .isEqualTo("Surname 1, Forename 1");

        softly.assertThat(values.get(1))
            .as(SUMMARY_FIELD_VALUE_MESSAGE)
            .isEqualTo("Pro_Auth");

        softly.assertThat(values.get(2))
            .as(SUMMARY_FIELD_VALUE_MESSAGE)
            .isEqualTo("12345678");

        softly.assertThat(values.get(3))
            .as(SUMMARY_FIELD_VALUE_MESSAGE)
            .isEqualTo("FHDRA1 (First Hearing and Dispute Resolution Appointment)");
    }
}
