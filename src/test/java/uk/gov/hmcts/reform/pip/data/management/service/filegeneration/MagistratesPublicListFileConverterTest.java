package uk.gov.hmcts.reform.pip.data.management.service.filegeneration;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.IOUtils;
import org.assertj.core.api.SoftAssertions;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.Map;
import java.util.Objects;

@SpringBootTest
@ActiveProfiles("test")
class MagistratesPublicListFileConverterTest {
    @Autowired
    MagistratesPublicListFileConverter magistratesPublicListFileConverter;

    private static final String HEADER_TEXT = "Incorrect header text";
    private static final String PROVENANCE = "provenance";
    private static final String BODY_CLASS = "govuk-body";

    @Test
    void testMagistratesPublicListTemplate() throws IOException {
        Map<String, Object> language;
        try (InputStream languageFile = Thread.currentThread()
            .getContextClassLoader().getResourceAsStream("templates/languages/en/magistratesPublicList.json")) {
            language = new ObjectMapper().readValue(
                Objects.requireNonNull(languageFile).readAllBytes(), new TypeReference<>() {
                });
        }
        StringWriter writer = new StringWriter();
        IOUtils.copy(Files.newInputStream(Paths.get("src/test/resources/mocks/",
                                                    "magistratesPublicList.json")), writer,
                     Charset.defaultCharset()
        );
        Map<String, String> metadataMap = Map.of("contentDate", Instant.now().toString(),
                                                 PROVENANCE, PROVENANCE,
                                                 "locationName", "location",
                                                 "language", "ENGLISH",
                                                 "listType", "MAGISTRATES_PUBLIC_LIST"
        );

        JsonNode inputJson = new ObjectMapper().readTree(writer.toString());
        String outputHtml = magistratesPublicListFileConverter.convert(inputJson, metadataMap, language);
        Document document = Jsoup.parse(outputHtml);

        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(outputHtml).as("No html found").isNotEmpty();

        softly.assertThat(document.title())
            .as("incorrect title found.")
            .isEqualTo("Magistrates Public List");

        softly.assertThat(document.getElementsByClass("govuk-heading-l")
                              .get(0).text())
            .as(HEADER_TEXT)
            .isEqualTo("Magistrates Public List for location");

        softly.assertThat(document.getElementsByClass(BODY_CLASS)
                              .get(1).text())
            .as(HEADER_TEXT)
            .isEqualTo("Last updated 14 September 2020 at 12:30am");

        softly.assertThat(document.getElementsByClass(BODY_CLASS)
                              .get(2).text())
            .as(HEADER_TEXT)
            .isEqualTo("Draft: Version");

        softly.assertThat(document.getElementsByClass(BODY_CLASS)
                              .get(4).text())
            .as(HEADER_TEXT)
            .isEqualTo("Telephone: 01772 844700");

        softly.assertThat(document.getElementsByClass(BODY_CLASS)
                              .get(5).text())
            .as(HEADER_TEXT)
            .isEqualTo("Email: court1@moj.gov.uk");

        softly.assertThat(outputHtml)
            .as("Before not shown")
            .doesNotContain("Before");

        softly.assertThat(document.getElementsByClass("govuk-table__head").get(0)
                              .getElementsByTag("th"))
            .as("Incorrect table headers")
            .hasSize(6)
            .extracting(Element::text)
            .containsExactly(
                "Sitting at",
                "Case Reference",
                "Defendant Name(s)",
                "Hearing Type",
                "Prosecuting Authority",
                "Duration"
            );

        softly.assertThat(document.getElementsByClass("govuk-table__body").get(0)
                              .getElementsByTag("td"))
            .as("Incorrect 'Sitting at' time")
            .extracting(Element::text)
            .contains("10:40am", "8am");

        softly.assertAll();
    }

    @Test
    void testMagistratesPublicListTemplateWelsh() throws IOException {
        Map<String, Object> language;
        try (InputStream languageFile = Thread.currentThread()
            .getContextClassLoader().getResourceAsStream("templates/languages/cy/magistratesPublicList.json")) {
            language = new ObjectMapper().readValue(
                Objects.requireNonNull(languageFile).readAllBytes(), new TypeReference<>() {
                });
        }
        StringWriter writer = new StringWriter();
        IOUtils.copy(Files.newInputStream(Paths.get("src/test/resources/mocks/",
                                                    "magistratesPublicList.json")), writer,
                     Charset.defaultCharset()
        );
        Map<String, String> metadataMap = Map.of("contentDate", Instant.now().toString(),
                                                 PROVENANCE, PROVENANCE,
                                                 "locationName", "location",
                                                 "language", "WELSH",
                                                 "listType", "MAGISTRATES_PUBLIC_LIST"
        );

        JsonNode inputJson = new ObjectMapper().readTree(writer.toString());
        String outputHtml = magistratesPublicListFileConverter.convert(inputJson, metadataMap, language);
        Document document = Jsoup.parse(outputHtml);

        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(outputHtml).as("No html found").isNotEmpty();

        softly.assertThat(document.title())
            .as("incorrect title found.")
            .isEqualTo("Rhestr Gyhoeddus y Llys Ynadon");

        softly.assertThat(document.getElementsByClass("govuk-heading-l")
                              .get(0).text())
            .as(HEADER_TEXT)
            .isEqualTo("Rhestr Gyhoeddus y Llys Ynadon ar gyfer location");

        softly.assertThat(document.getElementsByClass(BODY_CLASS)
                              .get(1).text())
            .as(HEADER_TEXT)
            .isEqualTo("Diweddarwyd diwethaf 14 September 2020 am 12:30am");

        softly.assertThat(document.getElementsByClass(BODY_CLASS)
                              .get(2).text())
            .as(HEADER_TEXT)
            .isEqualTo("Drafft: Fersiwn");

        softly.assertThat(document.getElementsByClass(BODY_CLASS)
                              .get(4).text())
            .as(HEADER_TEXT)
            .isEqualTo("Rhif ffôn: 01772 844700");

        softly.assertThat(document.getElementsByClass(BODY_CLASS)
                              .get(5).text())
            .as(HEADER_TEXT)
            .isEqualTo("E-bost: court1@moj.gov.uk");

        softly.assertThat(outputHtml)
            .as("Before translation not shown")
            .doesNotContain("Gerbron");

        softly.assertThat(document.getElementsByClass("govuk-table__head").get(0)
                              .getElementsByTag("th"))
            .as("Incorrect Welsh table headers")
            .hasSize(6)
            .extracting(Element::text)
            .containsExactly(
                "Yn eistedd yn",
                "Cyfeirnod yr Achos",
                "Enw'r Diffynnydd(Diffynyddion)",
                "Math o Wrandawiad",
                "Yr Awdurdod sy'n Erlyn",
                "Hyd"
            );

        softly.assertAll();
    }
}
