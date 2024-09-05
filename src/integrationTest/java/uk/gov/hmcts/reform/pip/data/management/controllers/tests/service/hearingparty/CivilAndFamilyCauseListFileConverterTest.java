package uk.gov.hmcts.reform.pip.data.management.controllers.tests.service.hearingparty;

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
import uk.gov.hmcts.reform.pip.data.management.service.filegeneration.CivilAndFamilyDailyCauseListFileConverter;

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

@SpringBootTest
@ActiveProfiles("test")
class CivilAndFamilyCauseListFileConverterTest {
    private static final String PROVENANCE = "provenance";
    private static final String HEADER_TEXT = "Incorrect Header Text";
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final Map<String, String> METADATA = Map.of(
        "contentDate", Instant.now().toString(),
        PROVENANCE, PROVENANCE,
        "locationName", "location",
        "language", "ENGLISH",
        "listType", "CIVIL_AND_FAMILY_DAILY_CAUSE_LIST"
    );
    @Autowired
    CivilAndFamilyDailyCauseListFileConverter civilAndFamilyDailyCauseListConverter;

    @Test
    void testFamilyCauseListTemplate() throws IOException {
        Map<String, Object> language;
        try (InputStream languageFile = Thread.currentThread()
            .getContextClassLoader().getResourceAsStream("templates/languages/en/civilAndFamilyDailyCauseList.json")) {
            language = OBJECT_MAPPER.readValue(
                Objects.requireNonNull(languageFile).readAllBytes(), new TypeReference<>() {
                });
        }
        JsonNode inputJson = getInputJson();
        String outputHtml = civilAndFamilyDailyCauseListConverter.convert(inputJson, METADATA, language);
        Document document = Jsoup.parse(outputHtml);
        assertThat(outputHtml).as("No html found").isNotEmpty();

        assertThat(document.title()).as("incorrect title found.")
            .isEqualTo("Civil and Family Daily Cause List");

        assertThat(document.getElementsByClass("govuk-heading-l")
                       .get(0).text())
            .as(HEADER_TEXT).isEqualTo("Civil and Family Daily Cause List for location");

        assertThat(document.getElementsByClass("govuk-body")
                       .get(2).text())
            .as(HEADER_TEXT).contains("Last updated 21 July 2022");

        assertThat(document.getElementsByClass("govuk-accordion__section-heading"))
            .as("Incorrect table titles")
            .extracting(Element::text)
            .containsAll(List.of(
                "This is the court room name, Before: Judge KnownAs Presiding, Judge KnownAs 2",
                "This is another court room name, Before: Judge KnownAs 3, Judge KnownAs 4",
                "This is the court room name, Before: Judge KnownAs 1, Judge KnownAs 2"));
    }

    @Test
    void testFamilyCauseListTemplateWelsh() throws IOException {
        Map<String, Object> language;
        try (InputStream languageFile = Thread.currentThread()
            .getContextClassLoader().getResourceAsStream("templates/languages/cy/civilAndFamilyDailyCauseList.json")) {
            language = OBJECT_MAPPER.readValue(
                Objects.requireNonNull(languageFile).readAllBytes(), new TypeReference<>() {
                });
        }
        JsonNode inputJson = getInputJson();
        String outputHtml = civilAndFamilyDailyCauseListConverter.convert(inputJson, METADATA, language);
        Document document = Jsoup.parse(outputHtml);
        assertThat(outputHtml).as("No html found").isNotEmpty();

        assertThat(document.title()).as("incorrect title found.")
            .isEqualTo("Rhestr Achosion Dyddiol Sifil a Theuluolt");

        assertThat(document.getElementsByClass("govuk-heading-l")
                       .get(0).text())
            .as(HEADER_TEXT).isEqualTo("Rhestr Achosion Dyddiol Sifil a Theuluolt gyfer location");

        assertThat(document.getElementsByClass("govuk-body")
                       .get(2).text())
            .as(HEADER_TEXT).contains("Diweddarwyd ddiwethaf 21 July 2022 am 3:01pm");

        assertThat(document.getElementsByClass("govuk-accordion__section-heading"))
            .as("Incorrect table titles")
            .extracting(Element::text)
            .containsAll(List.of(
                "This is the court room name, Gerbron: Judge KnownAs Presiding, Judge KnownAs 2",
                "This is another court room name, Gerbron: Judge KnownAs 3, Judge KnownAs 4",
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
        String result = civilAndFamilyDailyCauseListConverter.convert(inputJson, METADATA, language);

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
            .as("Incorrect table contents for hearing with multiple cases")
            .extracting(Element::text)
            .containsSequence(
                "10:30am",
                "12341234",
                "This is a case name [2 of 3]",
                "normal",
                "Directions",
                "Teams, Attended",
                "1 hour 25 mins",
                "",
                "",
                "Reporting Restriction: Reporting restriction 1, Reporting restriction 2"
            );

        softly.assertThat(doc.getElementsByTag("td"))
            .as("Incorrect table contents for hearing with a single case")
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
        IOUtils.copy(Files.newInputStream(Paths.get("src/test/resources/mocks/hearingparty/",
                                                    "civilAndFamilyDailyCauseList.json")), writer,
                     Charset.defaultCharset()
        );

        return OBJECT_MAPPER.readTree(writer.toString());
    }
}
