package uk.gov.hmcts.reform.pip.data.management.service.filegeneration;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.IOUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.junit.jupiter.api.BeforeAll;
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

class CopCauseListFileConverterTest {

    private final CopDailyCauseListFileConverter copDailyCauseListConverter = new CopDailyCauseListFileConverter();

    private static final String CONTENT_DATE = "contentDate";
    private static final String PROVENANCE = "provenance";
    private static final String LOCATION_NAME = "locationName";
    private static final String LOCATION = "location";
    private static final String LANGUAGE = "language";
    private static final String LIST_TYPE = "listType";
    private static final String HEADER_TEXT = "incorrect header text";
    private static final String TITLE_TEXT = "Incorrect Title Text";
    private static final String ENGLISH = "ENGLISH";
    private static final String COP_DAILY_CAUSE_LIST = "COP_DAILY_CAUSE_LIST";
    private static final String GOVUK_HEADING_L = "govuk-heading-l";
    private static final String LOCATION_DETAILS = "locationDetails";
    private static final String LINK_MESSAGE = "Link does not match";
    private static final String LINK_CLASS = "govuk-link";
    private static final String HREF = "href";
    private static final String BODY_CLASS = "govuk-body";

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
                                                 LOCATION_NAME, LOCATION,
                                                 LANGUAGE, ENGLISH,
                                                 LIST_TYPE, COP_DAILY_CAUSE_LIST
        );

        String outputHtml = copDailyCauseListConverter.convert(inputJson, metadataMap, language);
        Document document = Jsoup.parse(outputHtml);
        assertThat(outputHtml).as("No html found").isNotEmpty();

        assertThat(document.title())
            .as("incorrect title found.")
            .isEqualTo("Court of Protection Daily Cause List");

        assertThat(document.getElementsByClass(GOVUK_HEADING_L)
                       .get(0).text())
            .as(HEADER_TEXT)
            .isEqualTo("In the Court of Protection: Regional COP Court");

        assertThat(document.getElementsByClass(GOVUK_HEADING_L)
                       .get(1).text())
            .as(HEADER_TEXT)
            .isEqualTo("Regional Lead Judge Judge KnownAs Regional");

        assertThat(document.getElementsByClass(LINK_CLASS).get(0)
                              .getElementsByTag("a").get(0)
                              .attr(HREF))
            .as(LINK_MESSAGE)
            .isEqualTo("https://www.find-court-tribunal.service.gov.uk/");

        assertThat(document.getElementsByClass(BODY_CLASS).get(0).text())
            .as(LINK_MESSAGE)
            .isEqualTo("Find contact details and other information about courts and tribunals in England "
                           + "and Wales, and some non-devolved tribunals in Scotland.");

        assertThat(document.getElementsByTag("a")
                       .get(1).attr("title"))
            .as(TITLE_TEXT).contains("How to observe a court or tribunal hearing");

        assertThat(document.getElementsByClass("govuk-body")
                       .get(2).text())
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
                                                 LOCATION_NAME, LOCATION,
                                                 LANGUAGE, "WELSH",
                                                 LIST_TYPE, COP_DAILY_CAUSE_LIST
        );

        String outputHtml = copDailyCauseListConverter.convert(inputJson, metadataMap, welshLanguage);
        Document document = Jsoup.parse(outputHtml);
        assertThat(outputHtml).as("No html found").isNotEmpty();

        assertThat(document.title())
            .as("incorrect title found.")
            .isEqualTo("Rhestr Achosion Ddyddiol y Llys Gwarchod");

        assertThat(document.getElementsByClass(GOVUK_HEADING_L)
                       .get(0).text())
            .as(HEADER_TEXT)
            .isEqualTo("Yn y Llys Gwarchod: Regional COP Court");

        assertThat(document.getElementsByClass(GOVUK_HEADING_L)
                       .get(1).text())
            .as(HEADER_TEXT)
            .isEqualTo("Barnwr Arweiniol Rhanbarthol Judge KnownAs Regional");

        assertThat(document.getElementsByClass(LINK_CLASS).get(0)
                              .getElementsByTag("a").get(0)
                              .attr(HREF))
            .as(LINK_MESSAGE)
            .isEqualTo("https://www.find-court-tribunal.service.gov.uk/");

        assertThat(document.getElementsByClass(BODY_CLASS).get(0).text())
            .as(LINK_MESSAGE)
            .isEqualTo("Dod o hyd i fanylion cyswllt a gwybodaeth arall am lysoedd a thribiwnlysoedd yng "
                           + "Nghymru a Lloegr a rhai tribiwnlysoedd heb eu datganoli yn yr Alban.");

        assertThat(document.getElementsByTag("a")
                       .get(1).attr("title"))
            .as(TITLE_TEXT).contains("Sut i arsylwi gwrandawiad llys neu dribiwnlys");

        assertThat(document.getElementsByClass("govuk-body")
                       .get(2).text())
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
                                                 LOCATION_NAME, LOCATION,
                                                 LANGUAGE, ENGLISH,
                                                 LIST_TYPE, COP_DAILY_CAUSE_LIST
        );

        String outputHtml = copDailyCauseListConverter.convert(inputJson, metadataMap, language);
        Document document = Jsoup.parse(outputHtml);

        assertThat(document.getElementsByTag("th"))
            .as("Incorrect table headers")
            .hasSize(28)
            .extracting(Element::text)
            .startsWith("Start Time",
                        "Case Ref",
                        "Case Details",
                        "Case Type",
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
                "ThisIsACaseName",
                "This is a case type",
                "Criminal",
                "1 hour [1 of 2]",
                "Teams, In-Person",
                "Reporting Restriction: Reporting restriction 1, Reporting restriction 2"
            );
    }

    @Test
    void testNoLocationDetailsProvided() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode testJson = mapper.readTree(inputJson.toString());
        ((com.fasterxml.jackson.databind.node.ObjectNode) testJson).remove(LOCATION_DETAILS);

        Map<String, String> metadataMap = Map.of(CONTENT_DATE, Instant.now().toString(),
                                                 PROVENANCE, PROVENANCE,
                                                 LOCATION_NAME, LOCATION,
                                                 LANGUAGE, ENGLISH,
                                                 LIST_TYPE, COP_DAILY_CAUSE_LIST
        );

        String outputHtml = copDailyCauseListConverter.convert(testJson, metadataMap, language);
        Document document = Jsoup.parse(outputHtml);

        assertThat(document.getElementsByClass(GOVUK_HEADING_L)
                       .stream()
                       .anyMatch(e -> "Regional Lead Judge Judge TestName Regional".equals(e.text())))
            .isFalse();

        assertThat(document.select("h2#page-heading").get(0).text())
            .isEqualTo("In the Court of Protection");
    }

    @Test
    void testNoRegionProvided() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode testJson = mapper.readTree(inputJson.toString());
        ((com.fasterxml.jackson.databind.node.ObjectNode) testJson.get(LOCATION_DETAILS)).remove("region");

        Map<String, String> metadataMap = Map.of(CONTENT_DATE, Instant.now().toString(),
                                                 PROVENANCE, PROVENANCE,
                                                 LOCATION_NAME, LOCATION,
                                                 LANGUAGE, ENGLISH,
                                                 LIST_TYPE, COP_DAILY_CAUSE_LIST
        );

        String outputHtml = copDailyCauseListConverter.convert(testJson, metadataMap, language);
        Document document = Jsoup.parse(outputHtml);

        assertThat(document.getElementsByClass(GOVUK_HEADING_L)
                       .stream()
                       .anyMatch(e -> "Regional Lead Judge Judge TestName Regional".equals(e.text())))
            .isFalse();

        assertThat(document.select("h2#page-heading").get(0).text())
            .isEqualTo("In the Court of Protection");
    }

    @Test
    void testNoRegionNameProvided() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode testJson = mapper.readTree(inputJson.toString());
        ((com.fasterxml.jackson.databind.node.ObjectNode) testJson.get(LOCATION_DETAILS).get("region")).remove("name");

        Map<String, String> metadataMap = Map.of(CONTENT_DATE, Instant.now().toString(),
                                                 PROVENANCE, PROVENANCE,
                                                 LOCATION_NAME, LOCATION,
                                                 LANGUAGE, ENGLISH,
                                                 LIST_TYPE, COP_DAILY_CAUSE_LIST
        );

        String outputHtml = copDailyCauseListConverter.convert(testJson, metadataMap, language);
        Document document = Jsoup.parse(outputHtml);

        assertThat(document.select("h2#page-heading").get(0).text())
            .isEqualTo("In the Court of Protection");
    }

    @Test
    void testNoRegionalJohProvided() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode testJson = mapper.readTree(inputJson.toString());
        ((com.fasterxml.jackson.databind.node.ObjectNode) testJson.get(LOCATION_DETAILS).get("region"))
            .remove("regionalJOH");

        Map<String, String> metadataMap = Map.of(CONTENT_DATE, Instant.now().toString(),
                                                 PROVENANCE, PROVENANCE,
                                                 LOCATION_NAME, LOCATION,
                                                 LANGUAGE, ENGLISH,
                                                 LIST_TYPE, COP_DAILY_CAUSE_LIST
        );

        String outputHtml = copDailyCauseListConverter.convert(testJson, metadataMap, language);
        Document document = Jsoup.parse(outputHtml);

        assertThat(document.getElementsByClass(GOVUK_HEADING_L)
                       .stream()
                       .anyMatch(e -> "Regional Lead Judge Judge TestName Regional".equals(e.text())))
            .isFalse();
    }

    @Test
    void testReportingRestrictionColspan() throws IOException {
        Map<String, String> metadataMap = Map.of(CONTENT_DATE, Instant.now().toString(),
                                                 PROVENANCE, PROVENANCE,
                                                 LOCATION_NAME, LOCATION,
                                                 LANGUAGE, ENGLISH,
                                                 LIST_TYPE, COP_DAILY_CAUSE_LIST
        );

        String outputHtml = copDailyCauseListConverter.convert(inputJson, metadataMap, language);
        Document document = Jsoup.parse(outputHtml);

        Element reportingRestrictionRow = document.select("tr:has(td[colspan=7])").first();
        Element reportingRestrictionCell = reportingRestrictionRow.selectFirst("td");
        assertThat(reportingRestrictionCell.attr("colspan"))
            .as("Reporting restriction cell should have colspan=7")
            .isEqualTo("7");
    }

}
