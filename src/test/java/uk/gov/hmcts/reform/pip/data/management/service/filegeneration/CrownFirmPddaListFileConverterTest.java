package uk.gov.hmcts.reform.pip.data.management.service.filegeneration;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.IOUtils;
import org.assertj.core.api.SoftAssertions;
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

class CrownFirmPddaListFileConverterTest {
    private static final String HEADING_CLASS = "govuk-heading-l";
    private static final String BODY_CLASS = "govuk-body";

    private static final String HEADING_MESSAGE = "Heading does not match";
    private static final String BODY_MESSAGE = "Body does not match";

    private final CrownFirmPddaListFileConverter crownFirmPddaListConverter = new CrownFirmPddaListFileConverter();

    @Test
    void testCrownFirmPddaListTemplate() throws IOException {
        Map<String, Object> language;
        try (InputStream languageFile = Thread.currentThread()
            .getContextClassLoader().getResourceAsStream("templates/languages/en/crownFirmPddaList.json")) {
            language = new ObjectMapper().readValue(
                Objects.requireNonNull(languageFile).readAllBytes(), new TypeReference<>() {
                });
        }
        StringWriter writer = new StringWriter();
        IOUtils.copy(
            Files.newInputStream(Paths.get("src/test/resources/mocks/", "crownFirmPddaList.json")),
            writer, Charset.defaultCharset()
        );
        Map<String, String> metadataMap = Map.of("contentDate", Instant.now().toString(),
                                                 "provenance", "MANUAL_UPLOAD",
                                                 "locationName", "location",
                                                 "language", "ENGLISH",
                                                 "listType", "CROWN_FIRM_PDDA_LIST"
        );

        JsonNode inputJson = new ObjectMapper().readTree(writer.toString());
        String outputHtml = crownFirmPddaListConverter.convert(inputJson, metadataMap, language);
        Document document = Jsoup.parse(outputHtml);

        SoftAssertions softly = new SoftAssertions();

        softly.assertThat(outputHtml)
            .as("No html found")
            .isNotEmpty();

        softly.assertThat(document.title())
            .as("incorrect title found.")
            .isEqualTo("Crown Firm List");

        softly.assertThat(document.getElementsByClass(HEADING_CLASS).get(0).text())
            .as(HEADING_MESSAGE)
            .contains("Crown Firm List for location");

        softly.assertThat(document.getElementsByClass(BODY_CLASS).get(0).text())
            .as(BODY_MESSAGE)
            .contains("List for 10 September 2025 to 11 September 2025");

        softly.assertThat(document.getElementsByClass(BODY_CLASS).get(1).text())
            .as(BODY_MESSAGE)
            .contains("Last updated 09 September 2025 at 11am");

        softly.assertThat(document.getElementsByClass(BODY_CLASS).get(2).text())
            .as(BODY_MESSAGE)
            .contains("Version 1.0");

        softly.assertThat(document.getElementsByClass(BODY_CLASS).get(3).text())
            .as(BODY_MESSAGE)
            .contains("1 Main Road London A1 1AA");

        softly.assertThat(document.getElementsByClass(BODY_CLASS).get(4).text())
            .as(BODY_MESSAGE)
            .contains("Restrictions on publishing or writing about these cases");

        softly.assertThat(document.getElementsByClass(HEADING_CLASS).get(1).text())
            .as(HEADING_MESSAGE)
            .contains("Wednesday 10 September 2025");

        softly.assertAll();
    }

    @Test
    void testCrownFirmPddaListTemplateWelsh() throws IOException {
        Map<String, Object> language;
        try (InputStream languageFile = Thread.currentThread()
            .getContextClassLoader().getResourceAsStream("templates/languages/cy/crownFirmPddaList.json")) {
            language = new ObjectMapper().readValue(
                Objects.requireNonNull(languageFile).readAllBytes(), new TypeReference<>() {
                });
        }
        StringWriter writer = new StringWriter();
        IOUtils.copy(
            Files.newInputStream(Paths.get("src/test/resources/mocks/", "crownFirmPddaList.json")),
            writer,
            Charset.defaultCharset()
        );
        Map<String, String> metadataMap = Map.of("contentDate", Instant.now().toString(),
                                                 "provenance", "MANUAL_UPLOAD",
                                                 "locationName", "welsh location",
                                                 "language", "WELSH",
                                                 "listType", "CROWN_FIRM_PDDA_LIST"
        );

        JsonNode inputJson = new ObjectMapper().readTree(writer.toString());
        String outputHtml = crownFirmPddaListConverter.convert(inputJson, metadataMap, language);
        Document document = Jsoup.parse(outputHtml);

        SoftAssertions softly = new SoftAssertions();

        softly.assertThat(outputHtml)
            .as("No html found")
            .isNotEmpty();

        softly.assertThat(document.title())
            .as("incorrect title found.")
            .isEqualTo("Rhestr Cwmni Llys y Goron");

        softly.assertThat(document.getElementsByClass(HEADING_CLASS).get(0).text())
            .as(HEADING_MESSAGE)
            .contains("Rhestr Cwmni Llys y Goron ar gyfer welsh location");

        softly.assertThat(document.getElementsByClass(BODY_CLASS).get(0).text())
            .as(BODY_MESSAGE)
            .contains("Rhestr ar gyfer 10 September 2025 i 11 September 2025");

        softly.assertThat(document.getElementsByClass(BODY_CLASS).get(1).text())
            .as(BODY_MESSAGE)
            .contains("Diweddarwyd diwethaf 09 September 2025 am 11am");

        softly.assertThat(document.getElementsByClass(BODY_CLASS).get(2).text())
            .as(BODY_MESSAGE)
            .contains("Fersiwn 1.0");

        softly.assertThat(document.getElementsByClass(BODY_CLASS).get(3).text())
            .as(BODY_MESSAGE)
            .contains("1 Main Road London A1 1AA");

        softly.assertThat(document.getElementsByClass(BODY_CLASS).get(4).text())
            .as(BODY_MESSAGE)
            .contains("Cyfyngiadau ar gyhoeddi neu ysgrifennu am yr achosion hyn");

        softly.assertThat(document.getElementsByClass(HEADING_CLASS).get(1).text())
            .as(HEADING_MESSAGE)
            .contains("Wednesday 10 September 2025");

        softly.assertAll();
    }

}
