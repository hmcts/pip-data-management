package uk.gov.hmcts.reform.pip.data.management.service.filegeneration;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.IOUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;

class CrownDailyListFileConverterTest {

    CrownDailyListFileConverter crownDailyListConverter = new CrownDailyListFileConverter();

    private static final String HEADER_TEXT = "Incorrect header text";
    private static final String PROVENANCE = "provenance";

    @Test
    void testCrownDailyListTemplate() throws IOException {
        Map<String, Object> language;
        try (InputStream languageFile = Thread.currentThread()
            .getContextClassLoader().getResourceAsStream("templates/languages/en/crownDailyList.json")) {
            language = new ObjectMapper().readValue(
                Objects.requireNonNull(languageFile).readAllBytes(), new TypeReference<>() {
                });
        }
        StringWriter writer = new StringWriter();
        IOUtils.copy(Files.newInputStream(Paths.get("src/test/resources/mocks/",
                                                    "crownDailyList.json")), writer,
                     Charset.defaultCharset()
        );
        Map<String, String> metadataMap = Map.of("contentDate", Instant.now().toString(),
                                                 PROVENANCE, PROVENANCE,
                                                 "locationName", "location",
                                                 "language", "ENGLISH",
                                                 "listType", "CROWN_DAILY_LIST"
        );

        JsonNode inputJson = new ObjectMapper().readTree(writer.toString());
        String outputHtml = crownDailyListConverter.convert(inputJson, metadataMap, language);
        Document document = Jsoup.parse(outputHtml);
        assertThat(outputHtml).as("No html found").isNotEmpty();

        assertThat(document.title())
            .as("incorrect title found.")
            .isEqualTo("Crown Daily List");

        assertThat(document.getElementsByClass("govuk-heading-l")
                       .get(0).text())
            .as(HEADER_TEXT)
            .contains("Crown Daily List for ");

        assertThat(document.getElementsByClass("govuk-body")
                       .get(2).text())
            .as(HEADER_TEXT)
            .contains("Draft: Version");

        assertThat(outputHtml)
            .as("Reporting restriction detail not shown")
            .doesNotContain("Reporting Restriction: This is a reporting restriction detail, "
                                + "This is another reporting restriction detail");

        Pattern reportingRestrictionPattern = Pattern.compile("Reporting Restriction: ");
        assertThat(reportingRestrictionPattern.matcher(outputHtml).results().count())
            .as("Incorrect number of reporting restrictions shown")
            .isEqualTo(2);

        assertThat(outputHtml)
            .as("Before not shown")
            .doesNotContain("Before");
    }

    @Test
    void testCrownDailyListTemplateWelsh() throws IOException {
        Map<String, Object> language;
        try (InputStream languageFile = Thread.currentThread()
            .getContextClassLoader().getResourceAsStream("templates/languages/cy/crownDailyList.json")) {
            language = new ObjectMapper().readValue(
                Objects.requireNonNull(languageFile).readAllBytes(), new TypeReference<>() {
                });
        }
        StringWriter writer = new StringWriter();
        IOUtils.copy(Files.newInputStream(Paths.get("src/test/resources/mocks/",
                                                    "crownDailyList.json")), writer,
                     Charset.defaultCharset()
        );
        Map<String, String> metadataMap = Map.of("contentDate", Instant.now().toString(),
                                                 PROVENANCE, PROVENANCE,
                                                 "locationName", "location",
                                                 "language", "WELSH",
                                                 "listType", "CROWN_DAILY_LIST"
        );

        JsonNode inputJson = new ObjectMapper().readTree(writer.toString());
        String outputHtml = crownDailyListConverter.convert(inputJson, metadataMap, language);
        Document document = Jsoup.parse(outputHtml);
        assertThat(outputHtml)
            .as("No html found")
            .isNotEmpty();

        assertThat(document.title())
            .as("incorrect title found.")
            .isEqualTo("Rhestr Ddyddiol Llys y Goron");

        assertThat(document.getElementsByClass("govuk-heading-l")
                       .get(0).text())
            .as(HEADER_TEXT)
            .contains("Rhestr Ddyddiol Llys y Goron ar gyfer ");

        assertThat(document.getElementsByClass("govuk-body")
                       .get(2).text())
            .as(HEADER_TEXT)
            .contains("Drafft:Fersiwn");

        assertThat(outputHtml)
            .as("Reporting restriction detail not shown")
            .doesNotContain("Cyfyngiad adrodd: This is a reporting restriction detail, "
                                + "This is another reporting restriction detail");

        Pattern reportingRestrictionPattern = Pattern.compile("Cyfyngiad adrodd: ");
        assertThat(reportingRestrictionPattern.matcher(outputHtml).results().count())
            .as("Incorrect number of reporting restrictions shown")
            .isEqualTo(2);

        assertThat(outputHtml)
            .as("Before translation not shown")
            .doesNotContain("Gerbron");
    }
}
