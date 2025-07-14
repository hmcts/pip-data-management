package uk.gov.hmcts.reform.pip.data.management.service.artefactsummary.nonstrategic;

import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.pip.model.publication.ListType;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@ActiveProfiles("test")
class FttResidentialPropertyWeeklyHearingListsSummaryDataTest extends NonStrategicCommonArtefactSummaryTestConfig {

    @ParameterizedTest
    @EnumSource(
        value = ListType.class,
        names = {
            "RPT_EASTERN_WEEKLY_HEARING_LIST",
            "RPT_LONDON_WEEKLY_HEARING_LIST",
            "RPT_MIDLANDS_WEEKLY_HEARING_LIST",
            "RPT_NORTHERN_WEEKLY_HEARING_LIST",
            "RPT_SOUTHERN_WEEKLY_HEARING_LIST"
        })
    void testFttResidentialPropertyWeeklyHearingListSummaryData(ListType listType) throws IOException {
        Map<String, List<Map<String, String>>> output = getArtefactSummaryOutput(
            "fttResidentialPropertyTribunalWeeklyHearingList.json", listType);

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
            .hasSize(3);

        List<String> keys = summaryFields.keySet()
            .stream()
            .toList();

        softly.assertThat(keys.get(0))
            .as(SUMMARY_FIELD_KEY_MESSAGE)
            .isEqualTo("Date");

        softly.assertThat(keys.get(1))
            .as(SUMMARY_FIELD_KEY_MESSAGE)
            .isEqualTo("Time");

        softly.assertThat(keys.get(2))
            .as(SUMMARY_FIELD_KEY_MESSAGE)
            .isEqualTo("Case reference number");

        List<String> values = summaryFields.values()
            .stream()
            .toList();

        softly.assertThat(values.get(0))
            .as(SUMMARY_FIELD_VALUE_MESSAGE)
            .isEqualTo("16 December 2024");

        softly.assertThat(values.get(1))
            .as(SUMMARY_FIELD_VALUE_MESSAGE)
            .isEqualTo("10:15am");

        softly.assertThat(values.get(2))
            .as(SUMMARY_FIELD_VALUE_MESSAGE)
            .isEqualTo("1234");

        softly.assertAll();
    }
}
