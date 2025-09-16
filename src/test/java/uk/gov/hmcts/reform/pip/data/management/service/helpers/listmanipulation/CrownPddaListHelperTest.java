package uk.gov.hmcts.reform.pip.data.management.service.helpers.listmanipulation;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.IOUtils;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.pip.data.management.models.templatemodels.crownpddalist.CrownPddaList;
import uk.gov.hmcts.reform.pip.data.management.models.templatemodels.crownpddalist.HearingInfo;
import uk.gov.hmcts.reform.pip.data.management.models.templatemodels.crownpddalist.SittingInfo;
import uk.gov.hmcts.reform.pip.model.publication.ListType;

import java.io.IOException;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Stream;

@ActiveProfiles("test")
class CrownPddaListHelperTest {
    private static final String RESULT_COUNT_MESSAGE = "Result count does not match";
    private static final String COURT_LIST_INFO_MESSAGE = "Court list info does not match";
    private static final String SITTING_INFO_MESSAGE = "Sitting info does not match";
    private static final String HEARING_INFO_MESSAGE = "Hearing info does not match";

    private static JsonNode dailyListInputJson;
    private static JsonNode firmListInputJson;

    @BeforeAll
    public static void setup() throws IOException {
        StringWriter dailyListWriter = new StringWriter();
        IOUtils.copy(
            Files.newInputStream(Paths.get("src/test/resources/mocks/crownDailyPddaList.json")), dailyListWriter,
            Charset.defaultCharset()
        );
        dailyListInputJson = new ObjectMapper().readTree(dailyListWriter.toString());

        StringWriter firmListWriter = new StringWriter();
        IOUtils.copy(
            Files.newInputStream(Paths.get("src/test/resources/mocks/crownFirmPddaList.json")), firmListWriter,
            Charset.defaultCharset()
        );
        firmListInputJson = new ObjectMapper().readTree(firmListWriter.toString());
    }

    private static Stream<Arguments> listTypeAndInputJsonProvider() {
        return Stream.of(
            Arguments.of(ListType.CROWN_DAILY_PDDA_LIST, dailyListInputJson),
            Arguments.of(ListType.CROWN_FIRM_PDDA_LIST, firmListInputJson)
        );
    }

    @ParameterizedTest
    @MethodSource("listTypeAndInputJsonProvider")
    void testProcessPayloadMethod(ListType listType, JsonNode inputJson) {
        List<CrownPddaList> results = CrownPddaListHelper.processPayload(inputJson, listType);
        SoftAssertions softly = new SoftAssertions();

        softly.assertThat(results)
            .as(RESULT_COUNT_MESSAGE)
            .hasSize(2);

        softly.assertThat(results.getFirst().getSittings())
            .as(RESULT_COUNT_MESSAGE)
            .hasSize(1);

        softly.assertThat(results.getLast().getSittings())
            .as(RESULT_COUNT_MESSAGE)
            .hasSize(2);

        softly.assertThat(results.getFirst().getSittings().getFirst().getHearings())
            .as(RESULT_COUNT_MESSAGE)
            .hasSize(1);

        softly.assertThat(results.getFirst().getSittings().getFirst().getHearings())
            .as(RESULT_COUNT_MESSAGE)
            .hasSize(1);

        softly.assertAll();
    }

    @ParameterizedTest
    @MethodSource("listTypeAndInputJsonProvider")
    void testCourtListInfo(ListType listType, JsonNode inputJson) {
        List<CrownPddaList> results = CrownPddaListHelper.processPayload(inputJson, listType);
        CrownPddaList result = results.getFirst();
        SoftAssertions softly = new SoftAssertions();

        softly.assertThat(result.getSittingDate())
            .as(COURT_LIST_INFO_MESSAGE)
            .isEqualTo("Wednesday 10 September 2025");

        softly.assertThat(result.getCourtName())
            .as(COURT_LIST_INFO_MESSAGE)
            .isEqualTo("TestCourtHouseName");

        softly.assertThat(result.getCourtAddress())
            .as(COURT_LIST_INFO_MESSAGE)
            .hasSize(3)
            .containsExactly("1 Main Road", "London", "A1 1AA");

        softly.assertThat(result.getCourtPhone())
            .as(COURT_LIST_INFO_MESSAGE)
            .isEqualTo("02071234568");

        softly.assertAll();
    }

    @ParameterizedTest
    @MethodSource("listTypeAndInputJsonProvider")
    void testSittingInfo(ListType listType, JsonNode inputJson) {
        List<CrownPddaList> results = CrownPddaListHelper.processPayload(inputJson, listType);

        SittingInfo result = results.getFirst().getSittings().getFirst();

        SoftAssertions softly = new SoftAssertions();

        softly.assertThat(result.getCourtRoomNumber())
            .as(SITTING_INFO_MESSAGE)
            .isEqualTo("1");

        softly.assertThat(result.getSittingAt())
            .as(SITTING_INFO_MESSAGE)
            .isEqualTo("10am");

        softly.assertThat(result.getJudgeName())
            .as(SITTING_INFO_MESSAGE)
            .isEqualTo("TestJudgeRequested, Ms TestJusticeForename TestJusticeSurname Sr");

        softly.assertAll();
    }

    @ParameterizedTest
    @MethodSource("listTypeAndInputJsonProvider")
    void testHearingInfo(ListType listType, JsonNode inputJson) {
        List<CrownPddaList> results = CrownPddaListHelper.processPayload(inputJson, listType);

        HearingInfo result = results.getFirst().getSittings().getFirst().getHearings().getFirst();

        SoftAssertions softly = new SoftAssertions();

        softly.assertThat(result.getCaseNumber())
            .as(HEARING_INFO_MESSAGE)
            .isEqualTo("T00112233");

        softly.assertThat(result.getDefendantName())
            .as(HEARING_INFO_MESSAGE)
            .isEqualTo("TestMaskedName, Mr TestDefendantForename TestDefendantSurname TestDefendantSuffix");

        softly.assertThat(result.getHearingType())
            .as(HEARING_INFO_MESSAGE)
            .isEqualTo("TestHearingDescription");

        softly.assertThat(result.getRepresentativeName())
            .as(HEARING_INFO_MESSAGE)
            .isEqualTo("TestSolicitorRequestedName");

        softly.assertThat(result.getProsecutingAuthority())
            .as(HEARING_INFO_MESSAGE)
            .isEqualTo("Crown Prosecution Service");

        softly.assertThat(result.getListNote())
            .as(HEARING_INFO_MESSAGE)
            .isEqualTo("TestListNote");

        softly.assertAll();
    }
}
