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
class UtIacJudicialReviewDailyHearingListSummaryDataTest extends NonStrategicCommonArtefactSummaryTestConfig {

    @ParameterizedTest
    @EnumSource(
        value = ListType.class,
        names = {
            "UT_IAC_JR_LEEDS_DAILY_HEARING_LIST",
            "UT_IAC_JR_MANCHESTER_DAILY_HEARING_LIST",
            "UT_IAC_JR_BIRMINGHAM_DAILY_HEARING_LIST",
            "UT_IAC_JR_CARDIFF_DAILY_HEARING_LIST"
        })
    void testUtIacJudicialReviewDailyHearingListSummaryData(ListType listType) throws IOException {
        Map<String, List<Map<String, String>>> output = getArtefactSummaryOutput(
            "utIacJudicialReviewDailyHearingList.json", listType);

        SoftAssertions softly = new SoftAssertions();

        softly.assertThat(output)
            .as(SUMMARY_SECTIONS_MESSAGE)
            .hasSize(1);

        List<Map<String, String>> summaryCases = output.get(null);
        softly.assertThat(summaryCases)
            .as(SUMMARY_CASES_MESSAGE)
            .hasSize(2);

        Map<String, String> summaryFields = summaryCases.getFirst();
        softly.assertThat(summaryFields)
            .as(SUMMARY_FIELDS_MESSAGE)
            .hasSize(2);

        List<String> keys = summaryFields.keySet()
            .stream()
            .toList();

        softly.assertThat(keys.get(0))
            .as(SUMMARY_FIELD_KEY_MESSAGE)
            .isEqualTo("Hearing time");

        softly.assertThat(keys.get(1))
            .as(SUMMARY_FIELD_KEY_MESSAGE)
            .isEqualTo("Case reference number");

        List<String> values = summaryFields.values()
            .stream()
            .toList();

        softly.assertThat(values.get(0))
            .as(SUMMARY_FIELD_VALUE_MESSAGE)
            .isEqualTo("10:30am");

        softly.assertThat(values.get(1))
            .as(SUMMARY_FIELD_VALUE_MESSAGE)
            .isEqualTo("1234");

        softly.assertAll();
    }
}
