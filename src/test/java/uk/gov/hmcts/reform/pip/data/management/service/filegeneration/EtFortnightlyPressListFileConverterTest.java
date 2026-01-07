package uk.gov.hmcts.reform.pip.data.management.service.filegeneration;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.IOUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
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

class EtFortnightlyPressListFileConverterTest {
    private final EtFortnightlyPressListFileConverter etFortnightlyPressListConverter =
        new EtFortnightlyPressListFileConverter();

    private static final String HEADER_TEXT = "Incorrect header text";
    private static final String LINK_MESSAGE = "Link does not match";

    private static final String PROVENANCE = "provenance";
    private static final String REGION = "regionName";

    private static final String LINK_CLASS = "govuk-link";
    private static final String HREF = "href";
    private static final String BODY_CLASS = "govuk-body";

    @Test
    void testEtFortnightlyPressListTemplate() throws IOException {
        Map<String, Object> language;
        try (InputStream languageFile = Thread.currentThread()
            .getContextClassLoader()
            .getResourceAsStream("templates/languages/en/etFortnightlyPressList.json")) {
            language = new ObjectMapper().readValue(
                Objects.requireNonNull(languageFile).readAllBytes(), new TypeReference<>() {
                }
            );
        }
        StringWriter writer = new StringWriter();
        IOUtils.copy(
            Files.newInputStream(Paths.get(
                "src/test/resources/mocks/",
                "etFortnightlyPressList.json"
            )), writer,
            Charset.defaultCharset()
        );
        Map<String, String> metadataMap = Map.of(
            "contentDate", Instant.now().toString(),
            PROVENANCE, PROVENANCE,
            "locationName", "location",
            "region", REGION,
            "language", "ENGLISH",
            "listType", "ET_FORTNIGHTLY_PRESS_LIST"
        );

        JsonNode inputJson = new ObjectMapper().readTree(writer.toString());
        String outputHtml = etFortnightlyPressListConverter.convert(inputJson, metadataMap, language);
        Document document = Jsoup.parse(outputHtml);
        assertThat(outputHtml).as("No html found").isNotEmpty();

        assertThat(document.title()).as("incorrect title found.")
            .isEqualTo("Employment Tribunals Fortnightly List");

        assertThat(document.getElementsByClass("govuk-heading-l")
                       .get(0).text())
            .as(HEADER_TEXT).contains("Employment Tribunals Fortnightly List: " + REGION);

        assertThat(document.getElementsByClass(LINK_CLASS).get(0)
                       .getElementsByTag("a").get(0)
                       .attr(HREF))
            .as(LINK_MESSAGE)
            .isEqualTo("https://www.find-court-tribunal.service.gov.uk/");

        assertThat(document.getElementsByClass(BODY_CLASS).get(0).text())
            .as(LINK_MESSAGE)
            .isEqualTo("Find contact details and other information about courts and tribunals in England "
                           + "and Wales, and some non-devolved tribunals in Scotland.");

        assertThat(document.getElementsByClass(BODY_CLASS)
                       .get(3).text())
            .as(HEADER_TEXT).contains("5 Test Street");
    }

    @Test
    void testEtFortnightlyPressListTemplateWelsh() throws IOException {
        Map<String, Object> language;
        try (InputStream languageFile = Thread.currentThread()
            .getContextClassLoader()
            .getResourceAsStream("templates/languages/cy/etFortnightlyPressList.json")) {
            language = new ObjectMapper().readValue(
                Objects.requireNonNull(languageFile).readAllBytes(), new TypeReference<>() {
                });
        }
        StringWriter writer = new StringWriter();
        IOUtils.copy(Files.newInputStream(Paths.get("src/test/resources/mocks/",
                                                    "etFortnightlyPressList.json")), writer,
                     Charset.defaultCharset()
        );
        Map<String, String> metadataMap = Map.of("contentDate", Instant.now().toString(),
                                                 PROVENANCE, PROVENANCE,
                                                 "locationName", "location",
                                                 "region", REGION,
                                                 "language", "WELSH",
                                                 "listType", "ET_FORTNIGHTLY_PRESS_LIST"
        );

        JsonNode inputJson = new ObjectMapper().readTree(writer.toString());
        String outputHtml = etFortnightlyPressListConverter.convert(inputJson, metadataMap, language);
        Document document = Jsoup.parse(outputHtml);
        assertThat(outputHtml).as("No html found").isNotEmpty();

        assertThat(document.title()).as("incorrect title found.")
            .isEqualTo("Tribiwnlysoedd Cyflogaeth Rhestr Ddyddiol");

        assertThat(document.getElementsByClass("govuk-heading-l")
                       .get(0).text())
            .as(HEADER_TEXT).contains("Tribiwnlysoedd Cyflogaeth Rhestr Ddyddiol: " + REGION);

        assertThat(document.getElementsByClass(LINK_CLASS).get(0)
                              .getElementsByTag("a").get(0)
                              .attr(HREF))
            .as(LINK_MESSAGE)
            .isEqualTo("https://www.find-court-tribunal.service.gov.uk/");

        assertThat(document.getElementsByClass(BODY_CLASS).get(0).text())
            .as(LINK_MESSAGE)
            .isEqualTo("Dod o hyd i fanylion cyswllt a gwybodaeth arall am lysoedd a thribiwnlysoedd yng "
                           + "Nghymru a Lloegr a rhai tribiwnlysoedd heb eu datganoli yn yr Alban.");

        assertThat(document.getElementsByClass(BODY_CLASS)
                       .get(3).text())
            .as(HEADER_TEXT).contains("5 Test Street");
    }

    @Test
    void testEtFortnightlyPressListTableContents() throws IOException {
        Map<String, Object> language;
        try (InputStream languageFile = Thread.currentThread()
            .getContextClassLoader()
            .getResourceAsStream("templates/languages/en/etFortnightlyPressList.json")) {
            language = new ObjectMapper().readValue(
                Objects.requireNonNull(languageFile).readAllBytes(), new TypeReference<>() {
                });
        }
        StringWriter writer = new StringWriter();
        IOUtils.copy(Files.newInputStream(Paths.get("src/test/resources/mocks/",
                                                    "etFortnightlyPressList.json")), writer,
                     Charset.defaultCharset()
        );
        Map<String, String> metadataMap = Map.of("contentDate", Instant.now().toString(),
                                                 PROVENANCE, PROVENANCE,
                                                 "locationName", "location",
                                                 "region", REGION,
                                                 "language", "ENGLISH",
                                                 "listType", "ET_FORTNIGHTLY_PRESS_LIST"
        );

        JsonNode inputJson = new ObjectMapper().readTree(writer.toString());
        String outputHtml = etFortnightlyPressListConverter.convert(inputJson, metadataMap, language);
        Document document = Jsoup.parse(outputHtml);

        assertThat(document.getElementsByTag("td"))
            .as("Incorrect table contents")
            .hasSize(42)
            .extracting(Element::text)
            .startsWith("9:30am",
                        "2 hours [2 of 3]",
                        "12341234",
                        "Test Organisation Rep: Mr T Test Surname 2",
                        "Capt. T Test Surname Rep: Dr T Test Surname 2",
                        "This is a hearing type",
                        "This is a sitting channel"
            );

        assertThat(document.getElementsByClass("govuk-table__body").get(1).getElementsByTag("td"))
            .as("Incorrect table contents for organisation details")
            .hasSize(35)
            .extracting(Element::text)
            .startsWith("3:30pm",
                        "3 mins",
                        "12341234",
                        "Organisation Name Rep: Organisation Name",
                        "Lord T Test Surname Rep: Dame T Test Surname",
                        "Hearing Type 1");
    }
}
