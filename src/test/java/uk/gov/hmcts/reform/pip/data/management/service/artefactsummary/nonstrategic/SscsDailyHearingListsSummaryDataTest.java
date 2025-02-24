package uk.gov.hmcts.reform.pip.data.management.service.artefactsummary.nonstrategic;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.IOUtils;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.pip.data.management.service.ListConversionFactory;
import uk.gov.hmcts.reform.pip.data.management.service.artefactsummary.ArtefactSummaryData;
import uk.gov.hmcts.reform.pip.model.publication.ListType;

import java.io.IOException;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

@ActiveProfiles("test")
class SscsDailyHearingListsSummaryDataTest {

    private static final String NON_STRATEGIC_RESOURCE_FOLDER = "src/test/resources/mocks/non-strategic/";
    private static final String SUMMARY_SECTIONS_MESSAGE = "Summary sections count does not match";
    private static final String SUMMARY_CASES_MESSAGE = "Summary cases count does not match";
    private static final String SUMMARY_FIELDS_MESSAGE = "Summary fields count does not match";
    private static final String SUMMARY_FIELD_KEY_MESSAGE = "Summary field key does not match";
    private static final String SUMMARY_FIELD_VALUE_MESSAGE = "Summary field value does not match";

    @ParameterizedTest
    @EnumSource(
        value = ListType.class,
        names = {
            "SSCS_MIDLANDS_DAILY_HEARING_LIST",
            "SSCS_SOUTH_EAST_DAILY_HEARING_LIST",
            "SSCS_WALES_AND_SOUTH_WEST_DAILY_HEARING_LIST",
            "SSCS_SCOTLAND_DAILY_HEARING_LIST",
            "SSCS_NORTH_EAST_DAILY_HEARING_LIST",
            "SSCS_NORTH_WEST_DAILY_HEARING_LIST",
            "SSCS_LONDON_DAILY_HEARING_LIST"
        })
    void testSscsDailyHearingListsSummaryData(ListType listType) throws IOException {
        StringWriter writer = new StringWriter();
        IOUtils.copy(
            Files.newInputStream(Paths.get(
                NON_STRATEGIC_RESOURCE_FOLDER,
                "sscsDailyHearingList.json"
            )), writer,
            Charset.defaultCharset()
        );

        JsonNode payload = new ObjectMapper().readTree(writer.toString());
        ArtefactSummaryData cstSummaryData = new ListConversionFactory()
            .getArtefactSummaryData(listType)
            .get();
        Map<String, List<Map<String, String>>> output = cstSummaryData.get(payload);

        SoftAssertions softly = new SoftAssertions();

        softly.assertThat(output)
            .as(SUMMARY_SECTIONS_MESSAGE)
            .hasSize(1);

        List<Map<String, String>> summaryCases = output.get(null);
        softly.assertThat(summaryCases)
            .as(SUMMARY_CASES_MESSAGE)
            .hasSize(1);

        Map<String, String> summaryFields = summaryCases.get(0);
        softly.assertThat(summaryFields)
            .as(SUMMARY_FIELDS_MESSAGE)
            .hasSize(3);

        List<String> keys = summaryFields.keySet()
            .stream()
            .toList();

        softly.assertThat(keys.get(0))
            .as(SUMMARY_FIELD_KEY_MESSAGE)
            .isEqualTo("Hearing time");

        softly.assertThat(keys.get(1))
            .as(SUMMARY_FIELD_KEY_MESSAGE)
            .isEqualTo("Hearing type");

        softly.assertThat(keys.get(2))
            .as(SUMMARY_FIELD_KEY_MESSAGE)
            .isEqualTo("Appeal reference number");

        List<String> values = summaryFields.values()
            .stream()
            .toList();

        softly.assertThat(values.get(0))
            .as(SUMMARY_FIELD_VALUE_MESSAGE)
            .isEqualTo("10:30am");

        softly.assertThat(values.get(1))
            .as(SUMMARY_FIELD_VALUE_MESSAGE)
            .isEqualTo("Directions");

        softly.assertThat(values.get(2))
            .as(SUMMARY_FIELD_VALUE_MESSAGE)
            .isEqualTo("1234567");

        softly.assertAll();
    }
}
