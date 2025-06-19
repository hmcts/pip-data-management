package uk.gov.hmcts.reform.pip.data.management.service.helpers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.assertThat;

class JudiciaryHelperTest {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final String JUDICIARY_MESSAGE = "Judiciary does not match";

    private static JsonNode inputJson;

    @BeforeAll
    static void setup() throws IOException {
        StringWriter writer = new StringWriter();
        IOUtils.copy(Files.newInputStream(
                         Paths.get("src/test/resources/mocks/judiciaryManipulation.json")), writer,
                     Charset.defaultCharset()
        );
        inputJson = OBJECT_MAPPER.readTree(writer.toString());
    }

    @Test
    void testJudiciaryManipulationWithNoPresidingJudge() {
        assertThat(JudiciaryHelper.findAndManipulateJudiciary(inputJson.get(0)))
            .as(JUDICIARY_MESSAGE)
            .isEqualTo("Judge 1, Judge 2, Judge 3");
    }

    @Test
    void testJudiciaryManipulationWithMissingPresidingJudge() {
        assertThat(JudiciaryHelper.findAndManipulateJudiciary(inputJson.get(1)))
            .as(JUDICIARY_MESSAGE)
            .isEqualTo("Judge 1, Judge 2, Judge 3");
    }

    @Test
    void testJudiciaryManipulationWithAPresidingJudge() {
        assertThat(JudiciaryHelper.findAndManipulateJudiciary(inputJson.get(2)))
            .as(JUDICIARY_MESSAGE)
            .isEqualTo("Crown Judge, Judge 1, Judge 2");
    }

    @Test
    void testJudiciaryManipulationWithOnlyPresidingJudge() {
        assertThat(JudiciaryHelper.findAndManipulateJudiciary(inputJson.get(3)))
            .as(JUDICIARY_MESSAGE)
            .isEqualTo("Crown Judge");
    }

    @Test
    void testJudiciaryManipulationWithMissingValues() {
        assertThat(JudiciaryHelper.findAndManipulateJudiciary(inputJson.get(4)))
            .as(JUDICIARY_MESSAGE)
            .isEqualTo("Judge 2, Judge 4");
    }

    @Test
    void testCrimeListJudiciaryManipulationWithNoPresidingJudge() {
        assertThat(JudiciaryHelper.findAndManipulateJudiciaryForCrime(inputJson.get(5)))
            .as(JUDICIARY_MESSAGE)
            .isEqualTo("Judge Test Name, Judge Test Name 2, Judge Test Name 3");
    }

    @Test
    void testCrimeListJudiciaryManipulationWithMissingPresidingJudge() {
        assertThat(JudiciaryHelper.findAndManipulateJudiciaryForCrime(inputJson.get(6)))
            .as(JUDICIARY_MESSAGE)
            .isEqualTo("Judge Test Name, Judge Test Name 2, Judge Test Name 3");
    }

    @Test
    void testCrimeListJudiciaryManipulationWithAPresidingJudge() {
        assertThat(JudiciaryHelper.findAndManipulateJudiciaryForCrime(inputJson.get(7)))
            .as(JUDICIARY_MESSAGE)
            .isEqualTo("Crown Judge Test Name, Judge Test Name, Judge Test Name 2");
    }

    @Test
    void testCrimeListJudiciaryManipulationWithOnlyPresidingJudge() {
        assertThat(JudiciaryHelper.findAndManipulateJudiciaryForCrime(inputJson.get(8)))
            .as(JUDICIARY_MESSAGE)
            .isEqualTo("Crown Judge Test Name");
    }

    @Test
    void testCrimeListJudiciaryManipulationWithMissingValues() {
        assertThat(JudiciaryHelper.findAndManipulateJudiciaryForCrime(inputJson.get(9)))
            .as(JUDICIARY_MESSAGE)
            .isEqualTo("Judge Test Name 2, Judge Test Name 4");
    }

}
