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

import static org.assertj.core.api.Assertions.assertThat;

class CrownFirmPddaListFileConverterTest {

    private final CrownFirmPddaListFileConverter crownFirmPddaListConverter = new CrownFirmPddaListFileConverter();

    private static final String HEADER_TEXT = "Incorrect header text";
    private static final String PROVENANCE = "provenance";
    private static final String CLASS_BODY = "govuk-body";

    @Test
    void testCrownFirmListTemplate() throws IOException {
        Map<String, Object> language;
        try (InputStream languageFile = Thread.currentThread()
            .getContextClassLoader().getResourceAsStream("templates/languages/en/crownFirmPddaList.json")) {
            language = new ObjectMapper().readValue(
                Objects.requireNonNull(languageFile).readAllBytes(), new TypeReference<>() {
                });
        }
        StringWriter writer = new StringWriter();
        IOUtils.copy(
            Files.newInputStream(Paths.get("src/test/resources/mocks/",
                                           "crownFirmPddaList.json")), writer,
            Charset.defaultCharset()
        );
        Map<String, String> metadataMap = Map.of("contentDate", Instant.now().toString(),
                                                 PROVENANCE, PROVENANCE,
                                                 "locationName", "location",
                                                 "language", "ENGLISH",
                                                 "listType", "CROWN_FIRM_PDDA_LIST"
        );

        JsonNode inputJson = new ObjectMapper().readTree(writer.toString());
        String outputHtml = crownFirmPddaListConverter.convert(inputJson, metadataMap, language);
        Document document = Jsoup.parse(outputHtml);
        assertThat(outputHtml).as("No html found").isNotEmpty();

        assertThat(document.title()).as("incorrect title found.")
            .isEqualTo("Crown Firm List");

        assertThat(document.getElementsByClass("govuk-heading-l")
                       .get(0).text())
            .as(HEADER_TEXT).contains("Crown Firm List for");

        assertThat(document.getElementsByClass(CLASS_BODY)
                       .get(0).text())
            .as(HEADER_TEXT).contains("List for 01 January 2024 to 02 January 2024");

        assertThat(document.getElementsByClass(CLASS_BODY)
                       .get(1).text())
            .as(HEADER_TEXT).contains("Last updated: 01 January 2024 at 10am");

        assertThat(document.getElementsByClass(CLASS_BODY)
                       .get(2).text())
            .as(HEADER_TEXT).contains("Version");
    }

    @Test
    void testCrownFirmListTemplateWelsh() throws IOException {
        Map<String, Object> language;
        try (InputStream languageFile = Thread.currentThread()
            .getContextClassLoader().getResourceAsStream("templates/languages/cy/crownFirmPddaList.json")) {
            language = new ObjectMapper().readValue(
                Objects.requireNonNull(languageFile).readAllBytes(), new TypeReference<>() {
                });
        }
        StringWriter writer = new StringWriter();
        IOUtils.copy(Files.newInputStream(Paths.get("src/test/resources/mocks/",
                                                    "crownFirmPddaList.json")), writer,
                     Charset.defaultCharset()
        );
        Map<String, String> metadataMap = Map.of("contentDate", Instant.now().toString(),
                                                 PROVENANCE, PROVENANCE,
                                                 "locationName", "location",
                                                 "language", "WELSH",
                                                 "listType", "CROWN_FIRM_PDDA_LIST"
        );

        JsonNode inputJson = new ObjectMapper().readTree(writer.toString());
        String outputHtml = crownFirmPddaListConverter.convert(inputJson, metadataMap, language);
        Document document = Jsoup.parse(outputHtml);
        assertThat(outputHtml).as("No html found").isNotEmpty();

        assertThat(document.title()).as("incorrect title found.")
            .isEqualTo("Rhestr Cwmni Llys y Goron");

        assertThat(document.getElementsByClass("govuk-heading-l")
                       .get(0).text())
            .as(HEADER_TEXT).contains("Rhestr Cwmni Llys y Goron ar gyfer");

        assertThat(document.getElementsByClass(CLASS_BODY)
                       .get(0).text())
            .as(HEADER_TEXT).contains("Rhestr ar gyfer");

        assertThat(document.getElementsByClass(CLASS_BODY)
                       .get(1).text())
            .as(HEADER_TEXT).contains("Diweddarwyd diwethaf");

        assertThat(document.getElementsByClass(CLASS_BODY)
                       .get(2).text())
            .as(HEADER_TEXT).contains("Fersiwn");

    }

}
