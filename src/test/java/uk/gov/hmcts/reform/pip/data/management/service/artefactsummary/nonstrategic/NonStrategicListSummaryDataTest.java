package uk.gov.hmcts.reform.pip.data.management.service.artefactsummary.nonstrategic;

import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.test.context.ActiveProfiles;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static uk.gov.hmcts.reform.pip.data.management.service.artefactsummary.nonstrategic.NonStrategicTestCasesConfigurations.provideTestCases;

@ActiveProfiles("test")
class NonStrategicListSummaryDataTest  extends NonStrategicCommonArtefactSummaryTestConfig {

    private static Stream<ListTestCaseSettings> summaryDataTestCases() {
        return provideTestCases();
    }

    @ParameterizedTest
    @MethodSource("summaryDataTestCases")
    void testArtefactSummaryData(ListTestCaseSettings testCase) throws IOException {
        Map<String, List<Map<String, String>>> output = getArtefactSummaryOutput(
            testCase.getJsonFileName(), testCase.getListType());

        SoftAssertions softly = new SoftAssertions();

        softly.assertThat(output)
            .as("Summary sections count does not match")
            .hasSize(testCase.getExpectedSectionCount());

        List<Map<String, String>> summaryCases = output.get(null);
        softly.assertThat(summaryCases)
            .as("Summary cases count does not match")
            .hasSize(testCase.getExpectedCaseCount());

        Map<String, String> summaryFields = summaryCases.get(0);
        softly.assertThat(summaryFields)
            .as("Summary fields count does not match")
            .hasSize(testCase.getExpectedFieldCount());

        List<String> keys = summaryFields.keySet().stream().toList();
        for (int i = 0; i < testCase.getExpectedFieldKeys().size(); i++) {
            softly.assertThat(keys.get(i))
                .as("Field key does not match")
                .isEqualTo(testCase.getExpectedFieldKeys().get(i));
        }

        List<String> values = summaryFields.values().stream().toList();
        for (int i = 0; i < testCase.getExpectedFieldValues().size(); i++) {
            softly.assertThat(values.get(i))
                .as("Field value does not match")
                .isEqualTo(testCase.getExpectedFieldValues().get(i));
        }

        softly.assertAll();
    }
}
