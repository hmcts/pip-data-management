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

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;

class FamilyCauseListFileConverterTest {

    private final FamilyDailyCauseListFileConverter familyDailyCauseListConverter
        = new FamilyDailyCauseListFileConverter();

    private static final String HEADER_TEXT = "Incorrect header text";
    private static final String TITLE_TEXT = "Incorrect Title Text";
    private static final String LINK_MESSAGE = "Link does not match";

    private static final String PROVENANCE = "provenance";

    private static final String LINK_CLASS = "govuk-link";
    private static final String HREF = "href";
    private static final String BODY_CLASS = "govuk-body";

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final Map<String, String> METADATA = Map.of(
        "contentDate", Instant.now().toString(),
        PROVENANCE, PROVENANCE,
        "locationName", "location",
        "language", "ENGLISH",
        "listType", "FAMILY_DAILY_CAUSE_LIST"
    );
    private static final Map<String, String> WELSH_METADATA = Map.of(
        "contentDate", Instant.now().toString(),
        PROVENANCE, PROVENANCE,
        "locationName", "location",
        "language", "WELSH",
        "listType", "FAMILY_DAILY_CAUSE_LIST"
    );

    @Test
    void testFamilyCauseListTemplate() throws IOException {
        Map<String, Object> language;
        try (InputStream languageFile = Thread.currentThread()
            .getContextClassLoader().getResourceAsStream("templates/languages/en/familyDailyCauseList.json")) {
            language = OBJECT_MAPPER.readValue(
                Objects.requireNonNull(languageFile).readAllBytes(), new TypeReference<>() {
                });
        }
        JsonNode inputJson = getInputJson();
        String outputHtml = familyDailyCauseListConverter.convert(inputJson, METADATA, language);

        Document document = Jsoup.parse(outputHtml);
        assertThat(outputHtml).as("No html found").isNotEmpty();

        assertThat(document.title()).as("incorrect title found.")
            .isEqualTo("Family Daily Cause List");

        assertThat(document.getElementsByClass("govuk-heading-l")
            .get(0).text())
            .as(HEADER_TEXT).isEqualTo("Family Daily Cause List for location");

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

        assertThat(document.getElementsByClass(BODY_CLASS)
                       .get(3).text())
            .as(HEADER_TEXT).contains("Last updated 21 July 2022");

        assertThat(document.getElementsByClass("govuk-accordion__section-heading"))
            .as("Incorrect table titles")
            .extracting(Element::text)
            .containsAll(List.of(
                "This is the court room name, Before: Judge KnownAs Presiding, Judge KnownAs",
                "This is the court room name, Before: Judge KnownAs 1, Judge KnownAs 2",
                "This is the court room name, Before: Judge KnownAs 1, Judge KnownAs 2"));
    }


    @Test
    void testFamilyCauseListTemplateWelsh() throws IOException {
        Map<String, Object> language;
        try (InputStream languageFile = Thread.currentThread()
            .getContextClassLoader().getResourceAsStream("templates/languages/cy/familyDailyCauseList.json")) {
            language = OBJECT_MAPPER.readValue(
                Objects.requireNonNull(languageFile).readAllBytes(), new TypeReference<>() {
                });
        }
        JsonNode inputJson = getInputJson();
        String outputHtml = familyDailyCauseListConverter.convert(inputJson, WELSH_METADATA, language);

        Document document = Jsoup.parse(outputHtml);
        assertThat(outputHtml).as("No html found").isNotEmpty();

        assertThat(document.title()).as("incorrect title found.")
            .isEqualTo("Rhestr Ddyddiol o Achosion Teulu");

        assertThat(document.getElementsByClass("govuk-heading-l")
                       .get(0).text())
            .as(HEADER_TEXT).isEqualTo("Rhestr Ddyddiol o Achosion Teulu gyfer location");

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

        assertThat(document.getElementsByClass(BODY_CLASS)
                       .get(3).text())
            .as(HEADER_TEXT).contains("Diweddarwyd ddiwethaf 21 July 2022 am 3:01pm");

        assertThat(document.getElementsByClass("govuk-accordion__section-heading"))
            .as("Incorrect table titles")
            .extracting(Element::text)
            .containsAll(List.of(
                "This is the court room name, Gerbron: Judge KnownAs Presiding, Judge KnownAs",
                "This is the court room name, Gerbron: Judge KnownAs 1, Judge KnownAs 2",
                "This is the court room name, Gerbron: Judge KnownAs 1, Judge KnownAs 2"));
    }

    @Test
    void testTableContents() throws IOException {
        Map<String, Object> language;
        try (InputStream languageFile = Thread.currentThread()
            .getContextClassLoader().getResourceAsStream("templates/languages/en/civilAndFamilyDailyCauseList.json")) {
            language = OBJECT_MAPPER.readValue(
                Objects.requireNonNull(languageFile).readAllBytes(), new TypeReference<>() {
                });
        }
        JsonNode inputJson = getInputJson();
        String result = familyDailyCauseListConverter.convert(inputJson, METADATA, language);

        Document doc = Jsoup.parse(result);
        SoftAssertions softly = new SoftAssertions();

        softly.assertThat(doc.getElementsByTag("th"))
            .as("Incorrect table headers")
            .hasSize(27)
            .extracting(Element::text)
            .startsWith("Time",
                        "Case Ref",
                        "Case Name",
                        "Case Type",
                        "Hearing Type",
                        "Location",
                        "Duration",
                        "Applicant/Petitioner",
                        "Respondent"
            );

        softly.assertThat(doc.getElementsByTag("td"))
            .as("Incorrect table size")
            .hasSize(46);

        softly.assertThat(doc.getElementsByTag("td"))
            .as("Incorrect table contents for hearing with a single case")
            .extracting(Element::text)
            .containsSequence(
                "10:30am",
                "12341234",
                "This is a case name [2 of 3]",
                "normal",
                "Directions",
                "Teams, Attended",
                "1 hour 25 mins",
                "Applicant Surname 1, Legal Advisor: Mr Rep Forenames 1 Rep Middlename 1 Rep Surname 1",
                "Respondent Surname 1",
                "Reporting Restriction: Reporting restriction 1, Reporting restriction 2"
            );

        softly.assertThat(doc.getElementsByTag("td"))
            .as("Incorrect table contents for hearing with multiple cases")
            .extracting(Element::text)
            .containsSequence(
                "10:30am",
                "12341236",
                "This is a case name 3",
                "normal",
                "Directions",
                "Teams, Attended",
                "1 hour 25 mins",
                "Applicant Surname 2, Legal Advisor: Mr Rep Forenames 3 Rep Middlename 3 Rep Surname 3, "
                    + "Mr Rep Forenames 2 Rep Middlename 2 Rep Surname 2",
                "Respondent Surname 2"
            );

        softly.assertThat(doc.getElementsByClass("govuk-table__body").get(1).getElementsByTag("td"))
            .as("Incorrect table contents for hearing with organisation details")
            .extracting(Element::text)
            .containsSequence(
                "10:30am",
                "12341235",
                "This is a case name 2",
                "normal",
                "Directions",
                "Teams, Attended",
                "1 hour 25 mins",
                "Applicant org name, Legal Advisor: Applicant rep org name",
                "Respondent org name, Legal Advisor: Respondent rep org name"
            );

        softly.assertAll();
    }

    private JsonNode getInputJson() throws IOException {
        StringWriter writer = new StringWriter();
        IOUtils.copy(Files.newInputStream(Paths.get("src/test/resources/mocks/",
                                                    "familyDailyCauseList.json")), writer,
                     Charset.defaultCharset()
        );

        return OBJECT_MAPPER.readTree(writer.toString());
    }
}
