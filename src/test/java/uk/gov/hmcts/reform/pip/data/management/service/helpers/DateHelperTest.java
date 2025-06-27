package uk.gov.hmcts.reform.pip.data.management.service.helpers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.pip.model.publication.Language;

import java.time.LocalDateTime;
import java.time.Month;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("test")
class DateHelperTest {

    private static final String ERR_MSG = "Helper method doesn't seem to be working correctly";
    private static final String TEST_DATETIME_1 = "2022-08-19T09:30:00Z";
    private static final String TEST_DATETIME_2 = "2022-07-26T16:04:43.416924Z";
    private static final String SITTING_START = "sittingStart";
    private static final String TIME_FORMAT = "h:mma";
    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Test
    void testLocalTimeMethod() {
        assertThat(DateHelper.formatLocalDateTimeToBst(
            LocalDateTime.of(1988, Month.SEPTEMBER, 29, 8, 30)))
            .as(ERR_MSG)
            .isEqualTo("29 September 1988");
    }

    private static Stream<Arguments> parametersForFormatTimestamp() {
        return Stream.of(
            Arguments.of(TEST_DATETIME_1, "HH:mm", "10:30"),
            Arguments.of("2022-08-19T13:30:00Z", "HH:mm", "14:30"),
            Arguments.of(TEST_DATETIME_1, TIME_FORMAT, "10:30am"),
            Arguments.of("2022-08-19T13:30:00Z", TIME_FORMAT, "2:30pm"),
            Arguments.of("2022-08-19T10:30:00Z", "hh:mma", "11:30am")
        );
    }

    @Test
    void testFormatTimestamp() {
        assertThat(DateHelper.formatTimeStampToBst(
            TEST_DATETIME_2, Language.ENGLISH, false, false))
            .as(ERR_MSG)
            .isEqualTo("26 July 2022");
    }

    @Test
    void testFormatTimestampWithDateFormat() {
        assertThat(DateHelper.formatTimeStampToBst(
            TEST_DATETIME_2, Language.ENGLISH, false, false, "dd MMMM"))
            .as(ERR_MSG)
            .isEqualTo("26 July");
    }

    @ParameterizedTest
    @MethodSource("parametersForFormatTimestamp")
    void testFormatTimestampWithTimeFormat(String input, String format, String expectedOutput) {
        assertThat(DateHelper.formatTimeStampToBst(input, Language.ENGLISH, false, false, format))
            .as(ERR_MSG)
            .isEqualTo(expectedOutput);
    }

    @Test
    void testFormatTimestampOnlyHoursMethod() {
        assertThat(DateHelper.formatTimeStampToBst(
            "2022-07-26T16:00:00.416924Z", Language.ENGLISH, true, false))
            .as(ERR_MSG)
            .contains("5");
    }

    @Test
    void testFormatTimestampOnlyTwoDigitsHoursMethod() {
        assertThat(DateHelper.formatTimeStampToBst(
            "2022-07-26T22:00:00.416924Z", Language.ENGLISH, true, false))
            .as(ERR_MSG)
            .contains("11");
    }

    @Test
    void testFormatTimestampWithBothDateAndTime() {
        assertThat(DateHelper.formatTimeStampToBst(
            TEST_DATETIME_2,Language.ENGLISH, false, true))
            .as(ERR_MSG)
            .isEqualTo("26 July 2022 at 17:04");
    }

    @Test
    void testFormatTimestampWithBothDateAndTimeInWelsh() {
        assertThat(DateHelper.formatTimeStampToBst(
            TEST_DATETIME_2,Language.WELSH, false, true))
            .as(ERR_MSG)
            .isEqualTo("26 July 2022 am 17:04");
    }

    @Test
    void testConvertStringToBstMethod() {
        assertThat(DateHelper.convertStringToBst(TEST_DATETIME_1).toLocalDateTime())
            .as(ERR_MSG)
            .isEqualTo("2022-08-19T10:30");
    }

    @Test
    void testFormatDurationInDaysForSingleDay() {
        assertThat(DateHelper.formatDurationInDays(1, Language.ENGLISH))
            .as(ERR_MSG)
            .isEqualTo("1 day");
    }

    @Test
    void testFormatDurationInDaysForMultipleDays() {
        assertThat(DateHelper.formatDurationInDays(3, Language.ENGLISH))
            .as(ERR_MSG)
            .isEqualTo("3 days");
    }

    @Test
    void testFormatDurationInDaysInWelsh() {
        assertThat(DateHelper.formatDurationInDays(1, Language.WELSH))
            .as(ERR_MSG)
            .isEqualTo("1 dydd");
    }

    @Test
    void testFormatDurationMethod() {
        assertThat(DateHelper.formatDuration(3, 10, Language.ENGLISH))
            .as(ERR_MSG)
            .isEqualTo("3 hours 10 mins");
    }

    @Test
    void testFormatDurationMethodInWelsh() {
        assertThat(DateHelper.formatDuration(3, 10, Language.WELSH))
            .as(ERR_MSG)
            .isEqualTo("3 awr 10 munud");
    }

    @Test
    void testFormatDurationWithNoMinutesMethod() {
        assertThat(DateHelper.formatDuration(3, 0, Language.ENGLISH))
            .as(ERR_MSG)
            .isEqualTo("3 hours");
    }

    @Test
    void testFormatDurationWithSingleHourNoMinutesMethod() {
        assertThat(DateHelper.formatDuration(1, 0, Language.ENGLISH))
            .as(ERR_MSG)
            .isEqualTo("1 hour");
    }

    @Test
    void testFormatDurationWithNoHourMethod() {
        assertThat(DateHelper.formatDuration(0, 30, Language.ENGLISH))
            .as(ERR_MSG)
            .isEqualTo("30 mins");
    }

    @Test
    void testFormatDurationWithNoHourAndOneMinuteMethod() {
        assertThat(DateHelper.formatDuration(0, 1, Language.ENGLISH))
            .as(ERR_MSG)
            .isEqualTo("1 min");
    }

    @Test
    void testFormatDurationWithSingleMinuteNoHourMethod() {
        assertThat(DateHelper.formatDuration(0, 1, Language.ENGLISH))
            .as(ERR_MSG)
            .isEqualTo("1 min");
    }

    @Test
    void testFormatDurationWithNoMinuteNoHourMethod() {
        assertThat(DateHelper.formatDuration(0, 0, Language.ENGLISH))
            .as(ERR_MSG)
            .isEmpty();
    }

    @Test
    void testFormatTimeStampToBstHavingWeekDay() {
        assertThat(DateHelper.formatTimeStampToBst(
            TEST_DATETIME_2, Language.ENGLISH, false, false, "EEEE dd MMMM yyyy"))
            .as(ERR_MSG)
            .isEqualTo("Tuesday 26 July 2022");
    }

    @Test
    void testCalculateDurationInDays() {
        ObjectNode sittingNode = MAPPER.createObjectNode();
        sittingNode.put(SITTING_START, "2022-12-10T10:00:52.123Z");
        sittingNode.put("sittingEnd", "2022-12-12T12:30:52.123Z");

        DateHelper.calculateDuration(sittingNode, Language.ENGLISH, true);
        assertThat(sittingNode.get("formattedDuration").asText())
            .as(ERR_MSG)
            .isEqualTo("2 days");
    }

    @Test
    void testCalculateDurationInHoursAndMinutes() {
        ObjectNode sittingNode = MAPPER.createObjectNode();
        sittingNode.put(SITTING_START, "2022-12-10T10:00:52.123Z");
        sittingNode.put("sittingEnd", "2022-12-10T12:30:52.123Z");

        DateHelper.calculateDuration(sittingNode, Language.ENGLISH, true);
        assertThat(sittingNode.get("formattedDuration").asText())
            .as(ERR_MSG)
            .isEqualTo("2 hours 30 mins");
    }

    private static Stream<Arguments> parameters() {
        return Stream.of(
            Arguments.of("2022-12-10T15:30:52.123Z", "3:30pm"),
            Arguments.of("2022-12-10T15:00:52.123Z", "3pm")
        );
    }

    @ParameterizedTest
    @MethodSource("parameters")
    void testFormatStartTime(String value, String expected) {
        ObjectNode sittingNode = MAPPER.createObjectNode();
        sittingNode.put(SITTING_START, value);

        DateHelper.formatStartTime(sittingNode, TIME_FORMAT);
        assertThat(sittingNode.get("time").asText())
            .as(ERR_MSG)
            .isEqualTo(expected);
    }
}
