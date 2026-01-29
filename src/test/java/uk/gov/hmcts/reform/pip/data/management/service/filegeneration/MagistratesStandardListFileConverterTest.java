package uk.gov.hmcts.reform.pip.data.management.service.filegeneration;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.IOUtils;
import org.assertj.core.api.SoftAssertions;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.time.Instant;
import java.util.Map;
import java.util.Objects;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class MagistratesStandardListFileConverterTest {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private static final String BODY_CLASS = "govuk-body";
    private static final String CONTENT_DATE = "contentDate";
    private static final String PROVENANCE = "provenance";
    private static final String LOCATION_NAME = "locationName";
    private static final String LANGUAGE = "language";
    private static final String LIST_TYPE = "listType";

    private static final String HEADER_MESSAGE = "Incorrect header text";
    private static final String BODY_MESSAGE = "Incorrect body text";
    private static final String COURT_ROOM_HEADING_MESSAGE = "Court room heading does not match";
    private static final String PARTY_HEADING_MESSAGE = "Party heading does not match";
    private static final String MATTER_INFO_MESSAGE = "Matter info does not match";
    private static final String OFFENCE_MESSAGE = "Offence info does not match";
    private static final String LINK_MESSAGE = "Link does not match";
    private static final String PROVENANCE_MESSAGE = "Provenance does not match";

    private static final String HEADING_CLASS = "govuk-heading-l";
    private static final String LINK_CLASS = "govuk-link";
    private static final String HREF = "href";

    private final MagistratesStandardListFileConverter converter = new MagistratesStandardListFileConverter();

    private JsonNode inputJson;
    private Map<String, Object> englishLanguageResource;
    private Map<String, Object> welshLanguageResource;
    private Map<String, String> englishMetadata;
    private Map<String, String> welshMetadata;

    @BeforeAll
    void setup() throws IOException {
        try (InputStream inputStream = getClass().getResourceAsStream("/mocks/magistratesStandardList.json")) {
            String inputRaw = IOUtils.toString(inputStream, Charset.defaultCharset());
            inputJson = OBJECT_MAPPER.readTree(inputRaw);
        }

        try (InputStream languageFile = Thread.currentThread()
            .getContextClassLoader()
            .getResourceAsStream("templates/languages/en/magistratesStandardList.json")) {
            englishLanguageResource = OBJECT_MAPPER.readValue(
                Objects.requireNonNull(languageFile).readAllBytes(), new TypeReference<>() {
                });
        }

        try (InputStream languageFile = Thread.currentThread()
            .getContextClassLoader()
            .getResourceAsStream("templates/languages/cy/magistratesStandardList.json")) {
            welshLanguageResource = OBJECT_MAPPER.readValue(
                Objects.requireNonNull(languageFile).readAllBytes(), new TypeReference<>() {
                });
        }

        englishMetadata = Map.of(
            CONTENT_DATE, Instant.now().toString(),
            PROVENANCE, PROVENANCE,
            LOCATION_NAME, "location",
            LANGUAGE, "ENGLISH",
            LIST_TYPE, "MAGISTRATES_STANDARD_LIST"
        );

        welshMetadata = Map.of(
            CONTENT_DATE, Instant.now().toString(),
            PROVENANCE, PROVENANCE,
            LOCATION_NAME, "location",
            LANGUAGE, "WELSH",
            LIST_TYPE, "MAGISTRATES_STANDARD_LIST"
        );
    }

    @Test
    void testGeneralListInformationInEnglish() throws IOException {
        String outputHtml = converter.convert(inputJson, englishMetadata, englishLanguageResource);
        Document document = Jsoup.parse(outputHtml);
        SoftAssertions softly = new SoftAssertions();

        softly.assertThat(outputHtml)
            .as("No html found")
            .isNotEmpty();

        softly.assertThat(document.title())
            .as("incorrect title found.")
            .isEqualTo("Magistrates Standard List");

        softly.assertThat(document.getElementsByClass("govuk-heading-l").get(0).text())
            .as(HEADER_MESSAGE)
            .contains("Magistrates Standard List for location");

        softly.assertThat(document.getElementsByClass(LINK_CLASS).get(0)
                              .getElementsByTag("a").get(0)
                              .attr(HREF))
            .as(LINK_MESSAGE)
            .isEqualTo("https://www.find-court-tribunal.service.gov.uk/");

        softly.assertThat(document.getElementsByClass(BODY_CLASS).get(0).text())
            .as(LINK_MESSAGE)
            .isEqualTo("Find contact details and other information about courts and tribunals in England "
                           + "and Wales, and some non-devolved tribunals in Scotland.");

        softly.assertThat(document.getElementsByClass(BODY_CLASS).get(2).text())
            .as(BODY_MESSAGE)
            .isEqualTo("Last updated: 01 December 2023 at 11:30pm");

        softly.assertAll();
    }

    @Test
    void testGeneralListInformationInWelsh() throws IOException {
        String outputHtml = converter.convert(inputJson, welshMetadata, welshLanguageResource);
        Document document = Jsoup.parse(outputHtml);
        SoftAssertions softly = new SoftAssertions();

        softly.assertThat(outputHtml)
            .as("No html found")
            .isNotEmpty();

        softly.assertThat(document.title())
            .as("incorrect title found.")
            .isEqualTo("Rhestr Safonol y Llys Ynadon");

        softly.assertThat(document.getElementsByClass("govuk-heading-l").get(0).text())
            .as(HEADER_MESSAGE)
            .contains("Rhestr Safonol y Llys Ynadon ar gyfer location");

        softly.assertThat(document.getElementsByClass(LINK_CLASS).get(0)
                              .getElementsByTag("a").get(0)
                              .attr(HREF))
            .as(LINK_MESSAGE)
            .isEqualTo("https://www.find-court-tribunal.service.gov.uk/");

        softly.assertThat(document.getElementsByClass(BODY_CLASS).get(0).text())
            .as(LINK_MESSAGE)
            .isEqualTo("Dod o hyd i fanylion cyswllt a gwybodaeth arall am lysoedd a thribiwnlysoedd yng "
                           + "Nghymru a Lloegr a rhai tribiwnlysoedd heb eu datganoli yn yr Alban.");

        softly.assertAll();
    }

    @Test
    void testCourtRoomHeadings() throws IOException {
        String result = converter.convert(inputJson, englishMetadata, englishLanguageResource);
        Document document = Jsoup.parse(result);
        Elements heading = document.getElementsByClass(HEADING_CLASS);
        SoftAssertions softly = new SoftAssertions();

        softly.assertThat(heading)
            .as(COURT_ROOM_HEADING_MESSAGE)
            .hasSize(7);

        softly.assertThat(heading.get(1).text())
            .as(COURT_ROOM_HEADING_MESSAGE)
            .contains("PRESTON");

        softly.assertThat(heading.get(2).text())
            .as(COURT_ROOM_HEADING_MESSAGE)
            .contains("LJA: Local Justice Area A");

        softly.assertThat(heading.get(3).text())
            .as(COURT_ROOM_HEADING_MESSAGE)
            .contains("Courtroom 1: Test Name, Test Name");

        softly.assertThat(heading.get(6).text())
            .as(COURT_ROOM_HEADING_MESSAGE)
            .contains("Courtroom 2:");

        softly.assertAll();
    }

    @Test
    void testPartyHeading() throws IOException {
        String result = converter.convert(inputJson, englishMetadata, englishLanguageResource);
        Document document = Jsoup.parse(result);
        Elements heading = document.getElementsByClass("govuk-heading-m");
        SoftAssertions softly = new SoftAssertions();

        softly.assertThat(heading)
            .as(PARTY_HEADING_MESSAGE)
            .hasSize(5);

        softly.assertThat(heading.get(0).text())
            .as(PARTY_HEADING_MESSAGE)
            .contains("Name: Surname A, Forename A MiddleName A (male)");

        softly.assertThat(heading.get(1).text())
            .as(PARTY_HEADING_MESSAGE)
            .contains("Name: Surname B, Forename B MiddleName B (male)*");

        softly.assertThat(heading.get(2).text())
            .as(PARTY_HEADING_MESSAGE)
            .contains("Name: Surname D, Forename D (female)*");

        softly.assertThat(heading.get(3).text())
            .as(PARTY_HEADING_MESSAGE)
            .contains("Name: This is an organisation");

        softly.assertThat(heading.get(4).text())
            .as(PARTY_HEADING_MESSAGE)
            .contains("Name: Surname E, Forename E (female)*");

        softly.assertAll();
    }

    @Test
    void testMatterInfoHeaders() throws IOException {
        String result = converter.convert(inputJson, englishMetadata, englishLanguageResource);
        Document document = Jsoup.parse(result);
        Elements body = document.getElementsByClass(BODY_CLASS);
        SoftAssertions softly = new SoftAssertions();

        softly.assertThat(body.get(9).text())
            .as(MATTER_INFO_MESSAGE)
            .contains("Sitting at 1:30pm [2 of 3]");

        softly.assertThat(body.get(10).text())
            .as(MATTER_INFO_MESSAGE)
            .contains("DOB and Age:");

        softly.assertThat(body.get(11).text())
            .as(MATTER_INFO_MESSAGE)
            .contains("Address:");

        softly.assertThat(body.get(12).text())
            .as(MATTER_INFO_MESSAGE)
            .contains("Prosecuting Authority:");

        softly.assertThat(body.get(13).text())
            .as(MATTER_INFO_MESSAGE)
            .contains("Attendance Method:");

        softly.assertThat(body.get(14).text())
            .as(MATTER_INFO_MESSAGE)
            .contains("Reference:");

        softly.assertThat(body.get(15).text())
            .as(MATTER_INFO_MESSAGE)
            .contains("ASN:");

        softly.assertThat(body.get(16).text())
            .as(MATTER_INFO_MESSAGE)
            .contains("Hearing Type:");

        softly.assertThat(body.get(17).text())
            .as(MATTER_INFO_MESSAGE)
            .contains("Panel:");

        softly.assertAll();
    }

    @Test
    void testMatterInfoHeaderWhenApplication() throws IOException {
        String result = converter.convert(inputJson, englishMetadata, englishLanguageResource);
        Document document = Jsoup.parse(result);
        Elements body = document.getElementsByClass(BODY_CLASS);
        SoftAssertions softly = new SoftAssertions();

        softly.assertThat(body.get(42).text())
            .as(MATTER_INFO_MESSAGE)
            .contains("Application Type");

        softly.assertAll();
    }

    @Test
    void testMatterInfoValue() throws IOException {
        String result = converter.convert(inputJson, englishMetadata, englishLanguageResource);
        Document document = Jsoup.parse(result);
        Elements body = document.getElementsByClass(BODY_CLASS);
        SoftAssertions softly = new SoftAssertions();

        softly.assertThat(body.get(10).text())
            .as(MATTER_INFO_MESSAGE)
            .contains("01/01/1950 Age: 20");

        softly.assertThat(body.get(11).text())
            .as(MATTER_INFO_MESSAGE)
            .contains("Address Line 1A, Address Line 2A, Town A, County A, AA1 AA1");

        softly.assertThat(body.get(12).text())
            .as(MATTER_INFO_MESSAGE)
            .contains("Prosecuting Authority Name");

        softly.assertThat(body.get(13).text())
            .as(MATTER_INFO_MESSAGE)
            .contains("VIDEO HEARING A");

        softly.assertThat(body.get(14).text())
            .as(MATTER_INFO_MESSAGE)
            .contains("45684548");

        softly.assertThat(body.get(15).text())
            .as(MATTER_INFO_MESSAGE)
            .contains("ABC1234");

        softly.assertThat(body.get(16).text())
            .as(MATTER_INFO_MESSAGE)
            .contains("Hearing Type A");

        softly.assertThat(body.get(17).text())
            .as(MATTER_INFO_MESSAGE)
            .contains("ADULT");

        softly.assertAll();
    }

    @Test
    void testMatterInfoValueWhenApplication() throws IOException {
        String result = converter.convert(inputJson, englishMetadata, englishLanguageResource);
        Document document = Jsoup.parse(result);
        Elements body = document.getElementsByClass(BODY_CLASS);
        SoftAssertions softly = new SoftAssertions();

        softly.assertThat(body.get(42).text())
            .as(MATTER_INFO_MESSAGE)
            .contains("Application Type 1");

        softly.assertAll();
    }

    @Test
    void testOffenceContents() throws IOException {
        String result = converter.convert(inputJson, englishMetadata, englishLanguageResource);
        Document document = Jsoup.parse(result);
        SoftAssertions softly = new SoftAssertions();

        softly.assertThat(document.getElementsByClass("govuk-details__summary").get(0).html())
            .as(OFFENCE_MESSAGE)
            .contains("1. ", "dd01-01", " - ", "drink driving");

        Elements tableCell = document.getElementsByClass("govuk-table__cell");
        softly.assertThat(tableCell.get(0).text())
            .as(OFFENCE_MESSAGE)
            .contains("driving whilst under the influence of alcohol");

        softly.assertThat(tableCell.get(1).text())
            .as(OFFENCE_MESSAGE)
            .contains("Legislation");

        softly.assertThat(tableCell.get(2).text())
            .as(OFFENCE_MESSAGE)
            .contains("This is a legislation");

        softly.assertThat(tableCell.get(3).text())
            .as(OFFENCE_MESSAGE)
            .contains("Max Penalty");

        softly.assertThat(tableCell.get(4).text())
            .as(OFFENCE_MESSAGE)
            .contains("100yrs");

        softly.assertThat(tableCell.get(5).text())
            .as(OFFENCE_MESSAGE)
            .contains("Plea");

        softly.assertThat(tableCell.get(6).text())
            .as(OFFENCE_MESSAGE)
            .contains("NOT_GUILTY");

        softly.assertThat(tableCell.get(7).text())
            .as(OFFENCE_MESSAGE)
            .contains("Date of Plea");

        softly.assertThat(tableCell.get(8).text())
            .as(OFFENCE_MESSAGE)
            .contains("27/06/2026");

        softly.assertThat(tableCell.get(9).text())
            .as(OFFENCE_MESSAGE)
            .contains("Convicted on");

        softly.assertThat(tableCell.get(10).text())
            .as(OFFENCE_MESSAGE)
            .contains("01/05/2026");

        softly.assertThat(tableCell.get(11).text())
            .as(OFFENCE_MESSAGE)
            .contains("Adjourned from");

        softly.assertThat(tableCell.get(12).text())
            .as(OFFENCE_MESSAGE)
            .contains("02/05/2026 - For the trial");

        softly.assertAll();
    }

    @Test
    void testAdjournedFromIsEmptyWhenNotSet() throws IOException {
        String result = converter.convert(inputJson, englishMetadata, englishLanguageResource);
        Document document = Jsoup.parse(result);
        SoftAssertions softly = new SoftAssertions();

        Elements tableCell = document.getElementsByClass("govuk-table__cell");
        softly.assertThat(tableCell.get(25).text())
            .as(OFFENCE_MESSAGE)
            .isEmpty();

        softly.assertAll();
    }

    @Test
    void testDataSource() throws IOException {
        String result = converter.convert(inputJson, englishMetadata, englishLanguageResource);
        Document document = Jsoup.parse(result);
        Elements body = document.getElementsByClass(BODY_CLASS);
        SoftAssertions softly = new SoftAssertions();

        softly.assertThat(body.get(65).text())
            .as(PROVENANCE_MESSAGE)
            .contains("Data Source: provenance");

        softly.assertAll();

    }
}
