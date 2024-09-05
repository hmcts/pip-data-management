package uk.gov.hmcts.reform.pip.data.management.controllers.tests.service.filegeneration;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.IOUtils;
import org.assertj.core.api.SoftAssertions;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.pip.data.management.service.filegeneration.OpaResultsFileConverter;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.Map;
import java.util.Objects;

@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SuppressWarnings("PMD.LooseCoupling")
class OpaResultsFileConverterTest {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private static final String CONTENT_DATE = "08 January 2024";
    private static final String LOCATION_NAME = "Location name";
    private static final String ENGLISH = "ENGLISH";
    private static final String LIST_TYPE = "OPA_RESULTS";

    private static final String HEADING_CLASS = "govuk-heading-l";
    private static final String DEFENDANT_HEADING_CLASS = "govuk-heading-m";
    private static final String CASE_URN_CLASS = "case-ref";
    private static final String OFFENCE_HEADING_CLASS = "govuk-details__summary-text";
    private static final String OFFENCE_TABLE_HEADER_CLASS = "govuk-table__header";
    private static final String OFFENCE_TABLE_CELL_CLASS = "govuk-table__cell";
    private static final String BODY_CLASS = "govuk-body";

    private static final String TITLE_MESSAGE = "Title does not match";
    private static final String HEADING_MESSAGE = "Heading does not match";
    private static final String CONTENT_DATE_MESSAGE = "Content date does not match";
    private static final String PUBLICATION_DATE_MESSAGE = "Publication date does not match";

    private static final String DEFENDANT_HEADING_MESSAGE = "Defendant heading does not match";
    private static final String CASE_URN_MESSAGE = "Case URN does not match";
    private static final String OFFENCE_MESSAGE = "Offence does not match";

    private static final Map<String, String> METADATA = Map.of("contentDate", CONTENT_DATE,
                                                               "locationName", LOCATION_NAME,
                                                               "language", ENGLISH,
                                                               "listType", LIST_TYPE);

    private final OpaResultsFileConverter converter = new OpaResultsFileConverter();

    private JsonNode inputJson;
    private Map<String, Object> englishLanguageResource;
    private Map<String, Object> welshLanguageResource;

    @BeforeAll
    void setup() throws IOException {
        try (InputStream inputStream = getClass().getResourceAsStream("/mocks/opaResults.json")) {
            String inputRaw = IOUtils.toString(inputStream, Charset.defaultCharset());
            inputJson = OBJECT_MAPPER.readTree(inputRaw);
        }

        try (InputStream languageFile = Thread.currentThread()
            .getContextClassLoader()
            .getResourceAsStream("templates/languages/en/opaResults.json")) {
            englishLanguageResource = OBJECT_MAPPER.readValue(
                Objects.requireNonNull(languageFile).readAllBytes(), new TypeReference<>() {
                });
        }

        try (InputStream languageFile = Thread.currentThread()
            .getContextClassLoader()
            .getResourceAsStream("templates/languages/cy/opaResults.json")) {
            welshLanguageResource = OBJECT_MAPPER.readValue(
                Objects.requireNonNull(languageFile).readAllBytes(), new TypeReference<>() {
                });
        }
    }

    @Test
    void testGeneralListInformationInEnglish() {
        String result = converter.convert(inputJson, METADATA, englishLanguageResource);
        Document document = Jsoup.parse(result);
        SoftAssertions softly = new SoftAssertions();

        softly.assertThat(document.title())
            .as(TITLE_MESSAGE)
            .isEqualTo("Online Plea and Allocation Results");

        softly.assertThat(document.getElementsByClass(HEADING_CLASS).get(0).text())
            .as(HEADING_MESSAGE)
            .contains("Online Plea and Allocation Results - " + LOCATION_NAME);

        Elements resultBody = document.getElementsByClass(BODY_CLASS);
        softly.assertThat(resultBody.get(0).text())
            .as(CONTENT_DATE_MESSAGE)
            .isEqualTo("Results published on 08 January 2024");

        softly.assertThat(resultBody.get(1).text())
            .as(PUBLICATION_DATE_MESSAGE)
            .isEqualTo("Last updated: 09 January 2024 at 11:30pm");

        softly.assertAll();
    }

    @Test
    void testGeneralListInformationInWelsh() {
        String result = converter.convert(inputJson, METADATA, welshLanguageResource);
        Document document = Jsoup.parse(result);
        SoftAssertions softly = new SoftAssertions();

        softly.assertThat(document.title())
            .as(TITLE_MESSAGE)
            .isEqualTo("Canlyniadau Pledio a Dyrannu Ar-lein");

        softly.assertThat(document.getElementsByClass(HEADING_CLASS).get(0).text())
            .as(HEADING_MESSAGE)
            .contains("Canlyniadau Pledio a Dyrannu Ar-lein - " + LOCATION_NAME);

        Elements resultBody = document.getElementsByClass(BODY_CLASS);
        softly.assertThat(resultBody.get(0).text())
            .as(CONTENT_DATE_MESSAGE)
            .isEqualTo("Cyhoeddwyd y canlyniadau ar 08 January 2024");

        softly.assertThat(resultBody.get(1).text())
            .as(PUBLICATION_DATE_MESSAGE)
            .isEqualTo("Diweddarwyd diwethaf: 09 January 2024 am 11:30pm");

        softly.assertAll();
    }

    @Test
    void testReportingRestriction() {
        String result = converter.convert(inputJson, METADATA, englishLanguageResource);
        Document document = Jsoup.parse(result);
        SoftAssertions softly = new SoftAssertions();

        softly.assertThat(document.getElementsByClass("restriction-list-section").get(0).getElementsByTag("h3"))
            .as("reporting restriction title does not match")
            .extracting(Element::text)
            .contains("Restrictions on publishing or writing about these cases");

        softly.assertThat(document.getElementsByClass("govuk-warning-text__text").get(0).text())
            .as("Reporting restriction warning does not match")
            .isEqualTo("You'll be in contempt of court if you publish any information which is protected by "
                           + "a reporting restriction. You could get a fine, prison sentence or both.");

        softly.assertAll();
    }

    @Test
    void testDecisionDateHeading() {
        String result = converter.convert(inputJson, METADATA, englishLanguageResource);
        Document document = Jsoup.parse(result);
        Elements heading = document.getElementsByClass(HEADING_CLASS);

        SoftAssertions softly = new SoftAssertions();

        softly.assertThat(heading)
            .as(HEADING_MESSAGE)
            .hasSize(4);

        softly.assertThat(heading.get(1).text())
            .as(HEADING_MESSAGE)
            .isEqualTo("Allocation decisions made on 07 January 2024");

        softly.assertThat(heading.get(2).text())
            .as(HEADING_MESSAGE)
            .isEqualTo("Allocation decisions made on 06 January 2024");

        softly.assertThat(heading.get(3).text())
            .as(HEADING_MESSAGE)
            .isEqualTo("Allocation decisions made on 05 January 2024");

        softly.assertAll();
    }

    @Test
    void testDefendantHeading() {
        String result = converter.convert(inputJson, METADATA, englishLanguageResource);
        Document document = Jsoup.parse(result);
        Elements heading = document.getElementsByClass(DEFENDANT_HEADING_CLASS);

        SoftAssertions softly = new SoftAssertions();

        softly.assertThat(heading)
            .as(DEFENDANT_HEADING_MESSAGE)
            .hasSize(4);

        softly.assertThat(heading.get(0).text())
            .as(DEFENDANT_HEADING_MESSAGE)
            .isEqualTo("Defendant Name: Organisation name");

        softly.assertThat(heading.get(1).text())
            .as(DEFENDANT_HEADING_MESSAGE)
            .isEqualTo("Defendant Name: Surname 2, Forename 2 MiddleName 2");

        softly.assertThat(heading.get(2).text())
            .as(DEFENDANT_HEADING_MESSAGE)
            .isEqualTo("Defendant Name: Surname, Forename MiddleName");

        softly.assertThat(heading.get(2).text())
            .as(DEFENDANT_HEADING_MESSAGE)
            .isEqualTo("Defendant Name: Surname, Forename MiddleName");

        softly.assertAll();
    }

    @Test
    void testCaseUrn() {
        String result = converter.convert(inputJson, METADATA, englishLanguageResource);
        Document document = Jsoup.parse(result);
        Elements caseUrn = document.getElementsByClass(CASE_URN_CLASS);

        SoftAssertions softly = new SoftAssertions();

        softly.assertThat(caseUrn)
            .as(CASE_URN_MESSAGE)
            .hasSize(4);

        softly.assertThat(caseUrn.get(0).text())
            .as(CASE_URN_MESSAGE)
            .isEqualTo("Case Ref / URN: URN456");

        softly.assertThat(caseUrn.get(1).text())
            .as(CASE_URN_MESSAGE)
            .isEqualTo("Case Ref / URN: URN456");

        softly.assertThat(caseUrn.get(2).text())
            .as(CASE_URN_MESSAGE)
            .isEqualTo("Case Ref / URN: URN123");

        softly.assertThat(caseUrn.get(3).text())
            .as(CASE_URN_MESSAGE)
            .isEqualTo("Case Ref / URN: URN789");

        softly.assertAll();
    }

    @Test
    void testCaseOffenceHeading() {
        String result = converter.convert(inputJson, METADATA, englishLanguageResource);
        Document document = Jsoup.parse(result);
        Elements offenceHeading = document.getElementsByClass(OFFENCE_HEADING_CLASS);

        SoftAssertions softly = new SoftAssertions();

        softly.assertThat(offenceHeading.get(0).text())
            .as(OFFENCE_MESSAGE)
            .isEqualTo("1. Offence title 2A - Offence section 2A");

        softly.assertThat(offenceHeading.get(1).text())
            .as(OFFENCE_MESSAGE)
            .isEqualTo("2. Offence title 2B - Offence section 2B");

        softly.assertAll();
    }

    @Test
    void testCaseOffenceTableHeaders() {
        String result = converter.convert(inputJson, METADATA, englishLanguageResource);
        Document document = Jsoup.parse(result);
        Elements offenceCell = document.getElementsByClass(OFFENCE_TABLE_HEADER_CLASS);

        SoftAssertions softly = new SoftAssertions();

        softly.assertThat(offenceCell.get(0).text())
            .as(OFFENCE_MESSAGE)
            .isEqualTo("Decision date");

        softly.assertThat(offenceCell.get(1).text())
            .as(OFFENCE_MESSAGE)
            .isEqualTo("Allocation decision");

        softly.assertThat(offenceCell.get(2).text())
            .as(OFFENCE_MESSAGE)
            .isEqualTo("Bail status");

        softly.assertThat(offenceCell.get(3).text())
            .as(OFFENCE_MESSAGE)
            .isEqualTo("Next hearing date");

        softly.assertThat(offenceCell.get(4).text())
            .as(OFFENCE_MESSAGE)
            .isEqualTo("Next hearing location");

        softly.assertThat(offenceCell.get(5).text())
            .as(OFFENCE_MESSAGE)
            .isEqualTo("Reporting restrictions");

        softly.assertAll();
    }

    @Test
    void testCaseOffenceContents() {
        String result = converter.convert(inputJson, METADATA, englishLanguageResource);
        Document document = Jsoup.parse(result);
        Elements offenceCell = document.getElementsByClass(OFFENCE_TABLE_CELL_CLASS);

        SoftAssertions softly = new SoftAssertions();

        softly.assertThat(offenceCell.get(0).text())
            .as(OFFENCE_MESSAGE)
            .isEqualTo("07 January 2024");

        softly.assertThat(offenceCell.get(1).text())
            .as(OFFENCE_MESSAGE)
            .isEqualTo("Decision detail 2A");

        softly.assertThat(offenceCell.get(2).text())
            .as(OFFENCE_MESSAGE)
            .isEqualTo("Unconditional bail");

        softly.assertThat(offenceCell.get(3).text())
            .as(OFFENCE_MESSAGE)
            .isEqualTo("10 February 2024");

        softly.assertThat(offenceCell.get(4).text())
            .as(OFFENCE_MESSAGE)
            .isEqualTo("Hearing location 2");

        softly.assertThat(offenceCell.get(5).text())
            .as(OFFENCE_MESSAGE)
            .isEqualTo("Reporting restriction detail 2, Reporting restriction detail 3");

        softly.assertAll();
    }
}
