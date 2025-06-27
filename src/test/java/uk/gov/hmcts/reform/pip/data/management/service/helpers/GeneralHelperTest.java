package uk.gov.hmcts.reform.pip.data.management.service.helpers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.ActiveProfiles;

import java.io.IOException;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("test")
class GeneralHelperTest {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final String ERR_MSG = "Helper method doesn't seem to be working correctly";
    private static final String TEST = "test";

    private static final String DOCUMENT = "document";
    private static final String COURT_LISTS = "courtLists";
    private static final String COURT_HOUSE = "courtHouse";
    private static final String COURT_ROOM = "courtRoom";
    private static final String SESSION = "session";
    private static final String SITTINGS = "sittings";
    private static final String CHANNEL = "channel";
    private static final String SESSION_CHANNEL = "sessionChannel";
    private static final String DELIMITER = ", ";

    private static JsonNode inputJson;

    @BeforeAll
    public static void setup() throws IOException {
        StringWriter writer = new StringWriter();
        IOUtils.copy(Files.newInputStream(Paths.get("src/test/resources/mocks/familyDailyCauseList.json")), writer,
                     Charset.defaultCharset()
        );

        inputJson = OBJECT_MAPPER.readTree(writer.toString());
    }

    @Test
    void testStringDelimiterWithEmptyStringMethod() {
        assertThat(GeneralHelper.stringDelimiter("", ","))
            .as(ERR_MSG)
            .isEmpty();
    }

    @Test
    void testStringDelimiterWithoutEmptyStringMethod() {
        assertThat(GeneralHelper.stringDelimiter(TEST, ","))
            .as(ERR_MSG)
            .isEqualTo(",");
    }

    @Test
    void testFindAndReturnNodeTextMethod() {
        assertThat(GeneralHelper.findAndReturnNodeText(inputJson.get(DOCUMENT), "publicationDate"))
            .as(ERR_MSG)
            .isEqualTo("2022-07-21T14:01:43Z");
    }

    @Test
    void testFindAndReturnNodeTextNotExistsMethod() {
        assertThat(GeneralHelper.findAndReturnNodeText(inputJson.get(DOCUMENT), TEST))
            .as(ERR_MSG)
            .isEmpty();
    }

    @Test
    void testFindAndReturnNodeTextWheNodeNotExistsMethod() {
        assertThat(GeneralHelper.findAndReturnNodeText(inputJson.get(TEST), "publicationDate"))
            .as(ERR_MSG)
            .isEmpty();
    }

    @Test
    void testTrimAnyCharacterFromStringEndMethod() {
        assertThat(GeneralHelper.trimAnyCharacterFromStringEnd("test,"))
            .as(ERR_MSG)
            .isEqualTo(TEST);
    }

    @Test
    void testTrimAnyCharacterFromStringWithSpaceEndMethod() {
        assertThat(GeneralHelper.trimAnyCharacterFromStringEnd("test, "))
            .as(ERR_MSG)
            .isEqualTo(TEST);
    }

    @Test
    void testAppendToStringBuilderMethod() {
        StringBuilder builder = new StringBuilder();
        builder.append("Test1");
        GeneralHelper.appendToStringBuilder(builder, "Test2 ", inputJson.get("venue"),
                                            "venueName"
        );
        assertThat(builder)
            .as(ERR_MSG)
            .hasToString("Test1\nTest2 This is the venue name");
    }

    @Test
    void testAppendToStringBuilderWithPrefix() {
        StringBuilder builder = new StringBuilder();
        builder.append("Test1");
        GeneralHelper.appendToStringBuilderWithPrefix(builder, "Test2: ", inputJson.get("venue"),
                                                      "venueName", "\t\t"
        );
        assertThat(builder)
            .as(ERR_MSG)
            .hasToString("Test1\t\tTest2: This is the venue name");
    }

    @Test
    void testFormatNodeArrayWithSingleValue() {
        JsonNode session = inputJson.get(COURT_LISTS).get(0)
            .get(COURT_HOUSE)
            .get(COURT_ROOM).get(0)
            .get(SESSION).get(0);

        assertThat(GeneralHelper.formatNodeArray(session, SESSION_CHANNEL, ", "))
            .as(ERR_MSG)
            .isEqualTo("VIDEO HEARING");

    }

    @Test
    void testFormatNodeArrayWithMultipleValues() {
        JsonNode sitting = inputJson.get(COURT_LISTS).get(0)
            .get(COURT_HOUSE)
            .get(COURT_ROOM).get(0)
            .get(SESSION).get(0)
            .get(SITTINGS).get(0);

        assertThat(GeneralHelper.formatNodeArray(sitting, CHANNEL, DELIMITER))
            .as(ERR_MSG)
            .isEqualTo("Teams, Attended");
    }

    @Test
    void testConvertToDelimitedStringWithEmptyList() {
        assertThat(GeneralHelper.convertToDelimitedString(Collections.emptyList(), DELIMITER))
            .as(ERR_MSG)
            .isEmpty();
    }

    @Test
    void testConvertToDelimitedStringWithSingleValue() {
        assertThat(GeneralHelper.convertToDelimitedString(List.of(TEST), DELIMITER))
            .as(ERR_MSG)
            .isEqualTo(TEST);
    }

    @Test
    void testConvertToDelimitedStringWithMultipleValues() {
        assertThat(GeneralHelper.convertToDelimitedString(List.of(TEST, TEST), DELIMITER))
            .as(ERR_MSG)
            .isEqualTo(TEST + DELIMITER + TEST);
    }

    @Test
    void testSafeGetWithReturningResult() {
        assertThat(GeneralHelper.safeGet("publicationDate", inputJson.get(DOCUMENT)))
            .as(ERR_MSG)
            .isEqualTo("2022-07-21T14:01:43Z");
    }

    @Test
    void testSafeGetWithReturningEmpty() {
        assertThat(GeneralHelper.safeGet("Test", inputJson.get(DOCUMENT)))
            .as(ERR_MSG)
            .isEmpty();
    }

    @Test
    void testSafeGetWithReturningEmptyForBullPointerException() {
        assertThat(GeneralHelper.safeGet("Test.0", inputJson.get(DOCUMENT)))
            .as(ERR_MSG)
            .isEmpty();
    }
}
