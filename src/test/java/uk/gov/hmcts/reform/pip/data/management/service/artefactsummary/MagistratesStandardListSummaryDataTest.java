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

import static uk.gov.hmcts.reform.pip.model.publication.ListType.MAGISTRATES_STANDARD_LIST;

@ActiveProfiles("test")
class MagistratesStandardListSummaryDataTest {
    private static final String SUMMARY_SECTIONS_MESSAGE = "Summary sections count does not match";
    private static final String SUMMARY_CASES_MESSAGE = "Summary cases count does not match";
    private static final String SUMMARY_FIELDS_MESSAGE = "Summary fields count does not match";
    private static final String SUMMARY_FIELD_KEY_MESSAGE = "Summary field key does not match";
    private static final String SUMMARY_FIELD_VALUE_MESSAGE = "Summary field value does not match";


    private Map<String, List<Map<String, String>>> getSummary() throws IOException {
        StringWriter writer = new StringWriter();
        IOUtils.copy(Files.newInputStream(Paths.get(
                         "src/test/resources/mocks/",
                         "magistratesStandardList.json"
                     )), writer,
                     Charset.defaultCharset()
        );

        JsonNode payload = new ObjectMapper().readTree(writer.toString());
        return new ListConversionFactory()
            .getArtefactSummaryData(MAGISTRATES_STANDARD_LIST).get()
            .get(payload);
    }

    @Test
    void testMagistratesStandardListSummaryMetadata() throws IOException {
        Map<String, List<Map<String, String>>> output = getSummary();

        SoftAssertions softly = new SoftAssertions();

        softly.assertThat(output)
            .as(SUMMARY_SECTIONS_MESSAGE)
            .hasSize(1);

        List<Map<String, String>> summaryCases = output.get(null);
        softly.assertThat(summaryCases)
            .as(SUMMARY_CASES_MESSAGE)
            .hasSize(6);

        Map<String, String> summaryFieldsForCase = summaryCases.get(0);
        softly.assertThat(summaryFieldsForCase)
            .as(SUMMARY_FIELDS_MESSAGE)
            .hasSize(5);

        softly.assertAll();
    }

    @Test
    void testMagistratesStandardListSummaryForCase() throws IOException {
        Map<String, List<Map<String, String>>> output = getSummary();
        SoftAssertions softly = new SoftAssertions();
        Map<String, String> summaryFieldsForCase = output.get(null).get(0);

        assertSummaryFieldKeys(softly, summaryFieldsForCase);

        List<String> values = summaryFieldsForCase.values()
            .stream()
            .toList();

        softly.assertThat(values.get(0))
            .as(SUMMARY_FIELD_VALUE_MESSAGE)
            .isEqualTo("Surname A, Forename A MiddleName A");

        softly.assertThat(values.get(1))
            .as(SUMMARY_FIELD_VALUE_MESSAGE)
            .isEqualTo("Prosecuting Authority Name");

        softly.assertThat(values.get(2))
            .as(SUMMARY_FIELD_VALUE_MESSAGE)
            .isEqualTo("45684548");

        softly.assertThat(values.get(3))
            .as(SUMMARY_FIELD_VALUE_MESSAGE)
            .isEqualTo("Hearing Type A");

        softly.assertThat(values.get(4))
            .as(SUMMARY_FIELD_VALUE_MESSAGE)
            .isEqualTo("drink driving, Assault by beating");

        softly.assertAll();
    }

    @Test
    void testMagistratesStandardListSummaryForApplication() throws IOException {
        Map<String, List<Map<String, String>>> output = getSummary();
        SoftAssertions softly = new SoftAssertions();
        Map<String, String> summaryFieldsForApplication = output.get(null).get(3);

        assertSummaryFieldKeys(softly, summaryFieldsForApplication);

        List<String> values = summaryFieldsForApplication.values()
            .stream()
            .toList();

        softly.assertThat(values.get(0))
            .as(SUMMARY_FIELD_VALUE_MESSAGE)
            .isEqualTo("Surname D, Forename D");

        softly.assertThat(values.get(1))
            .as(SUMMARY_FIELD_VALUE_MESSAGE)
            .isEqualTo("Prosecuting Authority Name B");

        softly.assertThat(values.get(2))
            .as(SUMMARY_FIELD_VALUE_MESSAGE)
            .isEqualTo("AppRefA");

        softly.assertThat(values.get(3))
            .as(SUMMARY_FIELD_VALUE_MESSAGE)
            .isEqualTo("Hearing Type B");

        softly.assertThat(values.get(4))
            .as(SUMMARY_FIELD_VALUE_MESSAGE)
            .isEqualTo("Offence title 1, Offence title 2");

        softly.assertAll();
    }

    private void assertSummaryFieldKeys(SoftAssertions softly, Map<String, String> summaryFields) {
        List<String> keys = summaryFields.keySet()
            .stream()
            .toList();

        softly.assertThat(keys.get(0))
            .as(SUMMARY_FIELD_KEY_MESSAGE)
            .isEqualTo("Name");

        softly.assertThat(keys.get(1))
            .as(SUMMARY_FIELD_KEY_MESSAGE)
            .isEqualTo("Prosecuting authority");

        softly.assertThat(keys.get(2))
            .as(SUMMARY_FIELD_KEY_MESSAGE)
            .isEqualTo("Reference");

        softly.assertThat(keys.get(3))
            .as(SUMMARY_FIELD_KEY_MESSAGE)
            .isEqualTo("Hearing type");

        softly.assertThat(keys.get(4))
            .as(SUMMARY_FIELD_KEY_MESSAGE)
            .isEqualTo("Offence");
    }
}
