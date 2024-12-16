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

import static uk.gov.hmcts.reform.pip.model.publication.ListType.CST_WEEKLY_HEARING_LIST;
import static uk.gov.hmcts.reform.pip.model.publication.ListType.PHT_WEEKLY_HEARING_LIST;

@ActiveProfiles("test")
class NonStrategicListSummaryDataTest {
    private static final String SUMMARY_SECTIONS_MESSAGE = "Summary sections count does not match";
    private static final String SUMMARY_CASES_MESSAGE = "Summary cases count does not match";
    private static final String SUMMARY_FIELDS_MESSAGE = "Summary fields count does not match";
    private static final String SUMMARY_FIELD_KEY_MESSAGE = "Summary field key does not match";
    private static final String SUMMARY_FIELD_VALUE_MESSAGE = "Summary field value does not match";

    @Test
    void testCstWeeklyHearingListSummaryData() throws IOException {
        StringWriter writer = new StringWriter();
        IOUtils.copy(Files.newInputStream(Paths.get(
                         "src/test/resources/mocks/non-strategic/",
                         "cstWeeklyHearingList.json"
                     )), writer,
                     Charset.defaultCharset()
        );

        JsonNode payload = new ObjectMapper().readTree(writer.toString());
        NonStrategicArtefactSummaryData cstSummaryData = (NonStrategicArtefactSummaryData) new ListConversionFactory()
            .getArtefactSummaryData(CST_WEEKLY_HEARING_LIST)
            .get();
        Map<String, List<Map<String, String>>> output = cstSummaryData.get(payload, CST_WEEKLY_HEARING_LIST);

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
            .hasSize(2);

        List<String> keys = summaryFields.keySet()
            .stream()
            .toList();

        softly.assertThat(keys.get(0))
            .as(SUMMARY_FIELD_KEY_MESSAGE)
            .isEqualTo("Date");

        softly.assertThat(keys.get(1))
            .as(SUMMARY_FIELD_KEY_MESSAGE)
            .isEqualTo("Case name");

        List<String> values = summaryFields.values()
            .stream()
            .toList();

        softly.assertThat(values.get(0))
            .as(SUMMARY_FIELD_VALUE_MESSAGE)
            .isEqualTo("10 December 2024");

        softly.assertThat(values.get(1))
            .as(SUMMARY_FIELD_VALUE_MESSAGE)
            .isEqualTo("This is a case name");

        softly.assertAll();
    }

    @Test
    void testPhtWeeklyHearingListSummaryData() throws IOException {
        StringWriter writer = new StringWriter();
        IOUtils.copy(Files.newInputStream(Paths.get(
                         "src/test/resources/mocks/non-strategic/",
                         "phtWeeklyHearingList.json"
                     )), writer,
                     Charset.defaultCharset()
        );

        JsonNode payload = new ObjectMapper().readTree(writer.toString());
        NonStrategicArtefactSummaryData cstSummaryData = (NonStrategicArtefactSummaryData) new ListConversionFactory()
            .getArtefactSummaryData(PHT_WEEKLY_HEARING_LIST)
            .get();
        Map<String, List<Map<String, String>>> output = cstSummaryData.get(payload, PHT_WEEKLY_HEARING_LIST);

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
            .hasSize(2);

        List<String> keys = summaryFields.keySet()
            .stream()
            .toList();

        softly.assertThat(keys.get(0))
            .as(SUMMARY_FIELD_KEY_MESSAGE)
            .isEqualTo("Date");

        softly.assertThat(keys.get(1))
            .as(SUMMARY_FIELD_KEY_MESSAGE)
            .isEqualTo("Case name");

        List<String> values = summaryFields.values()
            .stream()
            .toList();

        softly.assertThat(values.get(0))
            .as(SUMMARY_FIELD_VALUE_MESSAGE)
            .isEqualTo("10 December 2024");

        softly.assertThat(values.get(1))
            .as(SUMMARY_FIELD_VALUE_MESSAGE)
            .isEqualTo("This is a case name");

        softly.assertAll();
    }
}
