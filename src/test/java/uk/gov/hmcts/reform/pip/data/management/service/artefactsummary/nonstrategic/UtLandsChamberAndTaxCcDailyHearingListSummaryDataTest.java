package uk.gov.hmcts.reform.pip.data.management.service.artefactsummary.nonstrategic;

import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.pip.model.publication.ListType;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

@ActiveProfiles("test")
class UtLandsChamberAndTaxCcDailyHearingListSummaryDataTest extends NonStrategicCommonArtefactSummaryTestConfig {

    private static Stream<Arguments> parameters() {
        return Stream.of(
            Arguments.of("UT_LC_DAILY_HEARING_LIST", "utLandsChamberDailyHearingList.json"),
            Arguments.of("UT_T_AND_CC_DAILY_HEARING_LIST", "utTaxAndChanceryChamberDailyHearingList.json")
        );
    }

    @ParameterizedTest
    @MethodSource("parameters")
    void testUtLandsChamberAndTaxCcDailyHearingListSummaryData(String listName,
        String listSampleJsonFile) throws IOException {

        Map<String, List<Map<String, String>>> output = getArtefactSummaryOutput(
            listSampleJsonFile, ListType.valueOf(listName));

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
            .isEqualTo("Time");

        softly.assertThat(keys.get(1))
            .as(SUMMARY_FIELD_KEY_MESSAGE)
            .isEqualTo("Case reference number");

        softly.assertThat(keys.get(2))
            .as(SUMMARY_FIELD_KEY_MESSAGE)
            .isEqualTo("Case name");

        List<String> values = summaryFields.values()
            .stream()
            .toList();

        softly.assertThat(values.get(0))
            .as(SUMMARY_FIELD_VALUE_MESSAGE)
            .isEqualTo("10:15am");

        softly.assertThat(values.get(1))
            .as(SUMMARY_FIELD_VALUE_MESSAGE)
            .isEqualTo("12345");

        softly.assertThat(values.get(2))
            .as(SUMMARY_FIELD_VALUE_MESSAGE)
            .isEqualTo("This is a case name");

        softly.assertAll();
    }
}
