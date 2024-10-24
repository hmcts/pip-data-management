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
@SuppressWarnings("PMD.LooseCoupling")
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
    private static final String DEFENDANT_HEADING_MESSAGE = "Defendant heading does not match";
    private static final String CASE_INFO_MESSAGE = "Case info does not match";
    private static final String OFFENCE_MESSAGE = "Offence info does not match";

    private static final String HEADING_CLASS = "govuk-heading-l";

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
    void testGeneralListInformationInEnglish() {
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

        softly.assertThat(document.getElementsByClass(BODY_CLASS).get(1).text())
            .as(BODY_MESSAGE)
            .isEqualTo("Last updated: 01 December 2023 at 11:30pm");

        softly.assertThat(document.getElementsByClass(BODY_CLASS).get(2).text())
            .as(BODY_MESSAGE)
            .contains("Draft: Version");

        softly.assertAll();
    }

    @Test
    void testGeneralListInformationInWelsh() {
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

        softly.assertThat(document.getElementsByClass(BODY_CLASS).get(2).text())
            .as(BODY_MESSAGE)
            .contains("Drafft: Fersiwn");

        softly.assertAll();
    }

    @Test
    void testCourtRoomHeadings() {
        String result = converter.convert(inputJson, englishMetadata, englishLanguageResource);
        Document document = Jsoup.parse(result);
        Elements heading = document.getElementsByClass(HEADING_CLASS);
        SoftAssertions softly = new SoftAssertions();

        softly.assertThat(heading)
            .as(COURT_ROOM_HEADING_MESSAGE)
            .hasSize(3);

        softly.assertThat(heading.get(1).text())
            .as(COURT_ROOM_HEADING_MESSAGE)
            .contains("Courtroom 1: Judge Test Name, Magistrate Test Name");

        softly.assertThat(heading.get(2).text())
            .as(COURT_ROOM_HEADING_MESSAGE)
            .contains("Courtroom 2: Judge Test Name 2, Magistrate Test Name 2");

        softly.assertAll();
    }

    @Test
    void testDefendantHeading() {
        String result = converter.convert(inputJson, englishMetadata, englishLanguageResource);
        Document document = Jsoup.parse(result);
        Elements heading = document.getElementsByClass("govuk-heading-m");
        SoftAssertions softly = new SoftAssertions();

        softly.assertThat(heading)
            .as(DEFENDANT_HEADING_MESSAGE)
            .hasSize(7);

        softly.assertThat(heading.get(0).text())
            .as(DEFENDANT_HEADING_MESSAGE)
            .contains("Defendant Name: Surname1, Forename1 (male)");

        softly.assertThat(heading.get(1).text())
            .as(DEFENDANT_HEADING_MESSAGE)
            .contains("Defendant Name: Surname2, Forename2 (male)*");

        softly.assertThat(heading.get(2).text())
            .as(DEFENDANT_HEADING_MESSAGE)
            .contains("Defendant Name: Surname3, Forename3 (male)*");

        softly.assertThat(heading.get(3).text())
            .as(DEFENDANT_HEADING_MESSAGE)
            .contains("Defendant Name: Surname4, Forename4 (male)*");

        softly.assertThat(heading.get(4).text())
            .as(DEFENDANT_HEADING_MESSAGE)
            .contains("Defendant Name: Surname5, Forename5 (male)*");

        softly.assertThat(heading.get(5).text())
            .as(DEFENDANT_HEADING_MESSAGE)
            .contains("Defendant Name: Surname6, Forename6 (male)*");

        softly.assertThat(heading.get(6).text())
            .as(DEFENDANT_HEADING_MESSAGE)
            .contains("Defendant Name: Surname5, Forename5");

        softly.assertAll();
    }

    @Test
    void testCaseInfoHeaders() {
        String result = converter.convert(inputJson, englishMetadata, englishLanguageResource);
        Document document = Jsoup.parse(result);
        Elements body = document.getElementsByClass(BODY_CLASS);
        SoftAssertions softly = new SoftAssertions();

        softly.assertThat(body.get(9).text())
            .as(CASE_INFO_MESSAGE)
            .contains("1. Sitting at 1:30pm for 2 hours 30 mins [2 of 3]");

        softly.assertThat(body.get(10).text())
            .as(CASE_INFO_MESSAGE)
            .contains("DOB and Age:");

        softly.assertThat(body.get(11).text())
            .as(CASE_INFO_MESSAGE)
            .contains("Defendant Address:");

        softly.assertThat(body.get(12).text())
            .as(CASE_INFO_MESSAGE)
            .contains("Prosecuting Authority:");

        softly.assertThat(body.get(13).text())
            .as(CASE_INFO_MESSAGE)
            .contains("Hearing Number:");

        softly.assertThat(body.get(14).text())
            .as(CASE_INFO_MESSAGE)
            .contains("Attendance Method:");

        softly.assertThat(body.get(15).text())
            .as(CASE_INFO_MESSAGE)
            .contains("Case Ref:");

        softly.assertThat(body.get(16).text())
            .as(CASE_INFO_MESSAGE)
            .contains("ASN:");

        softly.assertThat(body.get(17).text())
            .as(CASE_INFO_MESSAGE)
            .contains("Hearing Type:");

        softly.assertThat(body.get(18).text())
            .as(CASE_INFO_MESSAGE)
            .contains("Panel:");

        softly.assertAll();
    }

    @Test
    void testCaseInfoValue() {
        String result = converter.convert(inputJson, englishMetadata, englishLanguageResource);
        Document document = Jsoup.parse(result);
        Elements body = document.getElementsByClass(BODY_CLASS);
        SoftAssertions softly = new SoftAssertions();

        softly.assertThat(body.get(10).text())
            .as(CASE_INFO_MESSAGE)
            .contains("01/01/1983 Age: 39");

        softly.assertThat(body.get(11).text())
            .as(CASE_INFO_MESSAGE)
            .contains("Address Line 1, Address Line 2, Month A, County A, AA1 AA1");

        softly.assertThat(body.get(12).text())
            .as(CASE_INFO_MESSAGE)
            .contains("Test1234");

        softly.assertThat(body.get(13).text())
            .as(CASE_INFO_MESSAGE)
            .contains("12");

        softly.assertThat(body.get(14).text())
            .as(CASE_INFO_MESSAGE)
            .contains("VIDEO HEARING");

        softly.assertThat(body.get(15).text())
            .as(CASE_INFO_MESSAGE)
            .contains("45684548");

        softly.assertThat(body.get(16).text())
            .as(CASE_INFO_MESSAGE)
            .contains("Need to confirm");

        softly.assertThat(body.get(17).text())
            .as(CASE_INFO_MESSAGE)
            .contains("mda");

        softly.assertThat(body.get(18).text())
            .as(CASE_INFO_MESSAGE)
            .contains("ADULT");

        softly.assertAll();
    }

    @Test
    void testOffenceContents() {
        String result = converter.convert(inputJson, englishMetadata, englishLanguageResource);
        Document document = Jsoup.parse(result);
        SoftAssertions softly = new SoftAssertions();

        softly.assertThat(document.getElementsByClass("govuk-details__summary-text").get(0).text())
            .as(OFFENCE_MESSAGE)
            .contains("1. drink driving");

        softly.assertThat(document.getElementsByClass("govuk-details__summary-text").get(1).text())
            .as(OFFENCE_MESSAGE)
            .contains("Assault by beating");

        Elements tableCell = document.getElementsByClass("govuk-table__cell");
        softly.assertThat(tableCell.get(0).text())
            .as(OFFENCE_MESSAGE)
            .contains("Plea");

        softly.assertThat(tableCell.get(1).text())
            .as(OFFENCE_MESSAGE)
            .contains("NOT_GUILTY");

        softly.assertThat(tableCell.get(2).text())
            .as(OFFENCE_MESSAGE)
            .contains("Date of Plea");

        softly.assertThat(tableCell.get(3).text())
            .as(OFFENCE_MESSAGE)
            .contains("Need to confirm");

        softly.assertThat(tableCell.get(4).text())
            .as(OFFENCE_MESSAGE)
            .contains("Convicted on");

        softly.assertThat(tableCell.get(5).text())
            .as(OFFENCE_MESSAGE)
            .contains("13/12/2023");

        softly.assertThat(tableCell.get(6).text())
            .as(OFFENCE_MESSAGE)
            .contains("Adjourned from");

        softly.assertThat(tableCell.get(7).text())
            .as(OFFENCE_MESSAGE)
            .contains("13/12/2023 - For the trial");

        softly.assertThat(document.getElementsByClass("offence-wording").get(0).text())
            .as(OFFENCE_MESSAGE)
            .contains("driving whilst under the influence of alcohol");

        softly.assertAll();
    }
}
