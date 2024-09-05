package uk.gov.hmcts.reform.pip.data.management.controllers.tests.service.filegeneration;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.IOUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.pip.data.management.service.filegeneration.CopDailyCauseListFileConverter;

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

class CopCauseListFileConverterTest {

    private final CopDailyCauseListFileConverter copDailyCauseListConverter = new CopDailyCauseListFileConverter();

    private static final String CONTENT_DATE = "contentDate";
    private static final String PROVENANCE = "provenance";
    private static final String LOCATION_NAME = "locationName";
    private static final String LANGUAGE = "language";
    private static final String LIST_TYPE = "listType";
    private static final String HEADER_TEXT = "incorrect header text";
    private static final String TITLE_TEXT = "Incorrect Title Text";

    private static JsonNode inputJson;
    private static Map<String, Object> language;
    private static Map<String, Object> welshLanguage;

    @BeforeAll
    static void setup() throws IOException {
        try (InputStream languageFile = Thread.currentThread()
            .getContextClassLoader().getResourceAsStream("templates/languages/en/copDailyCauseList.json")) {
            language = new ObjectMapper().readValue(
                Objects.requireNonNull(languageFile).readAllBytes(), new TypeReference<>() {
                });
        }

        try (InputStream languageFile = Thread.currentThread()
            .getContextClassLoader().getResourceAsStream("templates/languages/cy/copDailyCauseList.json")) {
            welshLanguage = new ObjectMapper().readValue(
                Objects.requireNonNull(languageFile).readAllBytes(), new TypeReference<>() {
                });
        }

        StringWriter writer = new StringWriter();
        IOUtils.copy(Files.newInputStream(Paths.get("src/test/resources/mocks/copDailyCauseList.json")), writer,
                     Charset.defaultCharset()
        );
        inputJson = new ObjectMapper().readTree(writer.toString());
    }

    @Test
    void testCopCauseListTemplate() throws IOException {
        Map<String, String> metadataMap = Map.of(CONTENT_DATE, Instant.now().toString(),
                                                 PROVENANCE, PROVENANCE,
                                                 LOCATION_NAME, "location",
                                                 LANGUAGE, "ENGLISH",
                                                 LIST_TYPE, "COP_DAILY_CAUSE_LIST"
        );

        String outputHtml = copDailyCauseListConverter.convert(inputJson, metadataMap, language);
        Document document = Jsoup.parse(outputHtml);
        assertThat(outputHtml).as("No html found").isNotEmpty();

        assertThat(document.title())
            .as("incorrect title found.")
            .isEqualTo("Court of Protection Daily Cause List");

        assertThat(document.getElementsByClass("govuk-heading-l")
                       .get(0).text())
            .as(HEADER_TEXT)
            .isEqualTo("In the Court of Protection Regional COP Court");

        assertThat(document.getElementsByTag("a")
                       .get(0).attr("title"))
            .as(TITLE_TEXT).contains("How to observe a court or tribunal hearing");

        assertThat(document.getElementsByClass("govuk-body")
                       .get(1).text())
            .as(HEADER_TEXT)
            .contains("Last updated 14 February 2022 at 10:30am");

        assertThat(document.getElementsByClass("govuk-accordion__section-heading")
                       .get(0).text())
            .as(HEADER_TEXT)
            .contains("Room 1, Before Judge KnownAs Presiding, Judge KnownAs 2");

        assertThat(document.getElementsByClass("govuk-accordion__section-heading")
                       .get(3).text())
            .as(HEADER_TEXT)
            .contains("Room 2")
            .doesNotContain("Before");

    }

    @Test
    void testCopCauseListTemplateWelsh() throws IOException {
        Map<String, String> metadataMap = Map.of(CONTENT_DATE, Instant.now().toString(),
                                                 PROVENANCE, PROVENANCE,
                                                 LOCATION_NAME, "location",
                                                 LANGUAGE, "WELSH",
                                                 LIST_TYPE, "COP_DAILY_CAUSE_LIST"
        );

        String outputHtml = copDailyCauseListConverter.convert(inputJson, metadataMap, welshLanguage);
        Document document = Jsoup.parse(outputHtml);
        assertThat(outputHtml).as("No html found").isNotEmpty();

        assertThat(document.title())
            .as("incorrect title found.")
            .isEqualTo("Rhestr Achosion Ddyddiol y Llys Gwarchod");

        assertThat(document.getElementsByClass("govuk-heading-l")
                       .get(0).text())
            .as(HEADER_TEXT)
            .isEqualTo("Yn y Llys Gwarchod Regional COP Court");

        assertThat(document.getElementsByTag("a")
                       .get(0).attr("title"))
            .as(TITLE_TEXT).contains("Sut i arsylwi gwrandawiad llys neu dribiwnlys");

        assertThat(document.getElementsByClass("govuk-body")
                       .get(1).text())
            .as(HEADER_TEXT)
            .contains("Diweddarwyd ddiwethaf 14 February 2022 am 10:30am");

        assertThat(document.getElementsByClass("govuk-accordion__section-heading")
                       .get(0).text())
            .as(HEADER_TEXT)
            .contains("Room 1, Gerbron Judge KnownAs Presiding, Judge KnownAs 2");
    }

    @Test
    void testTableContents() throws IOException {
        Map<String, String> metadataMap = Map.of(CONTENT_DATE, Instant.now().toString(),
                                                 PROVENANCE, PROVENANCE,
                                                 LOCATION_NAME, "location",
                                                 LANGUAGE, "ENGLISH",
                                                 LIST_TYPE, "COP_DAILY_CAUSE_LIST"
        );

        String outputHtml = copDailyCauseListConverter.convert(inputJson, metadataMap, language);
        Document document = Jsoup.parse(outputHtml);

        assertThat(document.getElementsByTag("th"))
            .as("Incorrect table headers")
            .hasSize(24)
            .extracting(Element::text)
            .startsWith("Start Time",
                        "Case Ref",
                        "Case Details",
                        "Hearing Type",
                        "Time Estimate",
                        "Hearing Channel"
            );

        assertThat(document.getElementsByTag("td"))
            .as("Incorrect table contents")
            .extracting(Element::text)
            .containsSequence(
                "2:30pm",
                "12341234",
                "ThisIsACaseSuppressionName",
                "Criminal",
                "1 hour [1 of 2]",
                "Teams, In-Person",
                "Reporting Restriction: Reporting restriction 1, Reporting restriction 2"
            );
    }
}
