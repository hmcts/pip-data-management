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
import uk.gov.hmcts.reform.pip.data.management.service.filegeneration.OpaPublicListFileConverter;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.Map;
import java.util.Objects;

@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SuppressWarnings("PMD.LooseCoupling")
class OpaPublicListFileConverterTest {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final String HEADING_CLASS = "govuk-heading-l";
    private static final String BODY_CLASS = "govuk-body";
    private static final String CONTENT_DATE = "13 February 2022";
    private static final String LOCATION_NAME = "Location name";
    private static final String ENGLISH = "ENGLISH";
    private static final String LIST_TYPE = "OPA_PUBLIC_LIST";
    private static final String TITLE_MESSAGE = "Title does not match";
    private static final String HEADING_MESSAGE = "Heading does not match";
    private static final String ADDRESS_MESSAGE = "Address does not match";
    private static final String TABLE_HEADING_MESSAGE = "Table headings do not match";
    private static final String TABLE_DATA_MESSAGE = "Table data does not match";
    private static final Map<String, String> METADATA = Map.of("contentDate", CONTENT_DATE,
                                                               "locationName", LOCATION_NAME,
                                                               "language", ENGLISH,
                                                               "listType", LIST_TYPE);

    private final OpaPublicListFileConverter converter = new OpaPublicListFileConverter();
    private JsonNode inputJson;
    private Map<String, Object> englishLanguageResource;
    private Map<String, Object> welshLanguageResource;

    @BeforeAll
    void setup() throws IOException {
        try (InputStream inputStream = getClass().getResourceAsStream("/mocks/opaPublicList.json")) {
            String inputRaw = IOUtils.toString(inputStream, Charset.defaultCharset());
            inputJson = OBJECT_MAPPER.readTree(inputRaw);
        }

        try (InputStream languageFile = Thread.currentThread()
            .getContextClassLoader()
            .getResourceAsStream("templates/languages/en/opaPublicList.json")) {
            englishLanguageResource = OBJECT_MAPPER.readValue(
                Objects.requireNonNull(languageFile).readAllBytes(), new TypeReference<>() {
                });
        }

        try (InputStream languageFile = Thread.currentThread()
            .getContextClassLoader()
            .getResourceAsStream("templates/languages/cy/opaPublicList.json")) {
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
            .isEqualTo("OPA Public List");

        softly.assertThat(document.getElementsByClass(HEADING_CLASS).get(0).text())
            .as(HEADING_MESSAGE)
            .contains("Online Plea and Allocation Cases for " + LOCATION_NAME);

        softly.assertThat(document.getElementsByClass(BODY_CLASS).get(0).text())
            .as(ADDRESS_MESSAGE)
            .contains("town name");

        softly.assertAll();

    }

    @Test
    void testGeneralListInformationInWelsh() {
        String result = converter.convert(inputJson, METADATA, welshLanguageResource);
        Document document = Jsoup.parse(result);
        SoftAssertions softly = new SoftAssertions();

        softly.assertThat(document.title())
            .as(TITLE_MESSAGE)
            .isEqualTo("OPA Rhestr Gyhoeddus");

        softly.assertThat(document.getElementsByClass(HEADING_CLASS).get(0).text())
            .as(HEADING_MESSAGE)
            .contains("Achosion Pledio Ar-lein a Dyrannu ar gyfer " + LOCATION_NAME);

        softly.assertThat(document.getElementsByClass(BODY_CLASS).get(0).text())
            .as(ADDRESS_MESSAGE)
            .contains("town name");

        softly.assertAll();
    }

    @Test
    void testReportingRestriction() {
        String result = converter.convert(inputJson, METADATA, englishLanguageResource);
        Document document = Jsoup.parse(result);
        SoftAssertions softly = new SoftAssertions();

        softly.assertThat(document.getElementsByClass("restriction-list-section").get(0).getElementsByTag("h3"))
            .as("Reporting restriction title does not match")
            .extracting(Element::text)
            .contains("Restrictions on publishing or writing about these cases");

        softly.assertThat(document.getElementsByClass("govuk-warning-text__text").get(0).text())
            .as("Reporting restriction warning does not match")
            .contains("You'll be in contempt of court if you publish any information which is protected by a "
                          + "reporting restriction. You could get a fine, prison sentence or both.");

        softly.assertAll();
    }

    @Test
    void testCaseCountSummary() {
        String result = converter.convert(inputJson, METADATA, englishLanguageResource);
        Document document = Jsoup.parse(result);
        SoftAssertions softly = new SoftAssertions();

        softly.assertThat(document.getElementsByClass(BODY_CLASS).get(6).text())
            .as("Case count summary does not match")
            .isEqualTo("List containing 8 case(s) generated on 13 February 2022 at 9:30am");

        softly.assertAll();
    }

    @Test
    void testTableHeadings() {
        String result = converter.convert(inputJson, METADATA, englishLanguageResource);
        Document document = Jsoup.parse(result);
        Elements heading = document.getElementsByTag("th");
        SoftAssertions softly = new SoftAssertions();

        softly.assertThat(heading)
            .as(TABLE_HEADING_MESSAGE)
            .hasSize(6);

        softly.assertThat(heading.get(0).text())
            .as(TABLE_HEADING_MESSAGE)
            .contains("Name");

        softly.assertThat(heading.get(1).text())
            .as(TABLE_HEADING_MESSAGE)
            .contains("URN");

        softly.assertThat(heading.get(2).text())
            .as(TABLE_HEADING_MESSAGE)
            .contains("Offence");

        softly.assertThat(heading.get(3).text())
            .as(TABLE_HEADING_MESSAGE)
            .contains("Prosecutor");

        softly.assertThat(heading.get(4).text())
            .as(TABLE_HEADING_MESSAGE)
            .contains("Scheduled First Hearing");

        softly.assertThat(heading.get(5).text())
            .as(TABLE_HEADING_MESSAGE)
            .contains("Reporting Restriction");

        softly.assertAll();
    }

    @Test
    void testCaseData() {
        String result = converter.convert(inputJson, METADATA, englishLanguageResource);
        Document document = Jsoup.parse(result);
        Elements data = document.getElementsByTag("td");
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(data)
            .as(TABLE_DATA_MESSAGE)
            .hasSize(48);

        softly.assertThat(data.get(0).text())
            .as(TABLE_DATA_MESSAGE)
            .contains("IndividualSurname");

        softly.assertThat(data.get(1).text())
            .as(TABLE_DATA_MESSAGE)
            .contains("URN1234");

        softly.assertThat(data.get(2).text())
            .as(TABLE_DATA_MESSAGE)
            .contains("Offence title - Offence section "
                          + "Reporting Restriction - Offence Reporting Restriction detail");

        softly.assertThat(data.get(3).text())
            .as(TABLE_DATA_MESSAGE)
            .contains("Prosecution Authority ref 1");

        softly.assertThat(data.get(4).text())
            .as(TABLE_DATA_MESSAGE)
            .contains("14/09/16");

        softly.assertThat(data.get(5).text())
            .as(TABLE_DATA_MESSAGE)
            .contains("Case Reporting Restriction detail line 1, Case Reporting restriction detail line 2");

        softly.assertAll();
    }

    @Test
    void testOffenceData() {
        String result = converter.convert(inputJson, METADATA, englishLanguageResource);
        Document document = Jsoup.parse(result);
        Elements data = document.getElementsByTag("td");
        SoftAssertions softly = new SoftAssertions();

        softly.assertThat(data.get(2).text())
            .as(TABLE_DATA_MESSAGE)
            .contains("Offence title - Offence section "
                          + "Reporting Restriction - Offence Reporting Restriction detail");

        softly.assertThat(data.get(8).text())
            .as(TABLE_DATA_MESSAGE)
            .contains("Offence title 2 - Offence section 2");

        softly.assertThat(data.get(14).text())
            .as(TABLE_DATA_MESSAGE)
            .contains("Organisation Offence Title - Organisation Offence Section "
                          + "Reporting Restriction - Offence Reporting Restriction detail "
                          + "Organisation Offence Title 2 - Organisation Offence Section 2 "
                          + "Organisation Offence Title 3 - Organisation Offence Section 3 "
                          + "Reporting Restriction - Offence Reporting Restriction detail 3");

        softly.assertThat(data.get(20).text())
            .as(TABLE_DATA_MESSAGE)
            .contains("");

        softly.assertAll();
    }
}
