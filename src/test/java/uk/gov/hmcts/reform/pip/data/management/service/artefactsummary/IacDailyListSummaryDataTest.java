package uk.gov.hmcts.reform.pip.data.management.service.artefactsummary;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.IOUtils;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.pip.data.management.service.ListConversionFactory;
import uk.gov.hmcts.reform.pip.model.publication.ListType;

import java.io.IOException;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

@ActiveProfiles("test")
class IacDailyListSummaryDataTest {
    private static final String BAIL_LIST_HEADING = "Bail List";
    private static final String NON_BAIL_LIST_HEADING = "Non Bail List";

    private static final String SUMMARY_SECTIONS_MESSAGE = "Summary sections count does not match";
    private static final String SUMMARY_SECTION_HEADING_MESSAGE = "Summary section heading does not match";
    private static final String SUMMARY_CASES_MESSAGE = "Summary cases count does not match";
    private static final String SUMMARY_FIELDS_MESSAGE = "Summary fields count does not match";
    private static final String SUMMARY_FIELD_KEY_MESSAGE = "Summary field key does not match";
    private static final String SUMMARY_FIELD_VALUE_MESSAGE = "Summary field value does not match";

    @ParameterizedTest
    @EnumSource(value = ListType.class, names = {"IAC_DAILY_LIST", "IAC_DAILY_LIST_ADDITIONAL_CASES"})
    void testIacDailyListTemplate(ListType listType) throws IOException {

        StringWriter writer = new StringWriter();
        IOUtils.copy(Files.newInputStream(Paths.get(
                         "src/test/resources/mocks/",
                         "iacDailyList.json"
                     )), writer,
                     Charset.defaultCharset()
        );

        JsonNode payload = new ObjectMapper().readTree(writer.toString());
        Map<String, List<Map<String, String>>> output = new ListConversionFactory().getArtefactSummaryData(listType)
            .get().get(payload);

        SoftAssertions softly = new SoftAssertions();

        softly.assertThat(output)
            .as(SUMMARY_SECTIONS_MESSAGE)
            .hasSize(2);

        List<String> sectionHeadings = output.keySet()
            .stream()
            .toList();

        softly.assertThat(sectionHeadings.get(0))
            .as(SUMMARY_SECTION_HEADING_MESSAGE)
            .isEqualTo(BAIL_LIST_HEADING);

        softly.assertThat(sectionHeadings.get(1))
            .as(SUMMARY_SECTION_HEADING_MESSAGE)
            .isEqualTo(NON_BAIL_LIST_HEADING);

        softly.assertThat(output.get(BAIL_LIST_HEADING))
            .as(SUMMARY_CASES_MESSAGE)
            .hasSize(2);

        softly.assertThat(output.get(NON_BAIL_LIST_HEADING))
            .as(SUMMARY_CASES_MESSAGE)
            .hasSize(3);

        Map<String, String> summaryFields = output.get(BAIL_LIST_HEADING).get(0);
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
            .isEqualTo("Appellant/Applicant");

        softly.assertThat(keys.get(1))
            .as(SUMMARY_FIELD_KEY_MESSAGE)
            .isEqualTo("Prosecuting authority");

        softly.assertThat(keys.get(2))
            .as(SUMMARY_FIELD_KEY_MESSAGE)
            .isEqualTo("Case reference");
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
            .isEqualTo("Authority Surname");

        softly.assertThat(values.get(2))
            .as(SUMMARY_FIELD_VALUE_MESSAGE)
            .isEqualTo("12341234");
    }
}
