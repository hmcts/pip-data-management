package uk.gov.hmcts.reform.pip.data.management.service.filegeneration;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.IOUtils;
import org.assertj.core.api.SoftAssertions;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.pip.model.publication.Language;
import uk.gov.hmcts.reform.pip.model.publication.ListType;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.Map;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class MagistratesAdultCourtListFileConverterTest {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private static final String CONTENT_DATE = "contentDate";
    private static final String PROVENANCE = "provenance";
    private static final String LOCATION_NAME = "locationName";
    private static final String LANGUAGE = "language";
    private static final String LIST_TYPE = "listType";

    private static final String HTML_MESSAGE = "No html found";
    private static final String TITLE_MESSAGE = "Incorrect title found";
    private static final String GOVUK_HEADING_L = "govuk-heading-l";
    private static final String HEADER_MESSAGE = "Incorrect header text";
    private static final String BODY_MESSAGE = "Incorrect body text";
    private static final String COURT_ROOM_HEADING_MESSAGE = "Court room heading does not match";
    private static final String MAGISTRATES_ADULT_COURT_LIST_DAILY = "MAGISTRATES_ADULT_COURT_LIST_DAILY";
    private static final String MAGISTRATES_ADULT_COURT_LIST_FUTURE = "MAGISTRATES_ADULT_COURT_LIST_FUTURE";

    private static final String BODY_CLASS = "govuk-body";

    private final MagistratesAdultCourtListFileConverter standardConverter =
        new MagistratesAdultCourtListFileConverter(true);
    private final MagistratesAdultCourtListFileConverter publicConverter =
        new MagistratesAdultCourtListFileConverter(false);

    private JsonNode standardInputJson;
    private Map<String, Object> standardEnglishLanguageResource;
    private Map<String, Object> standardWelshLanguageResource;

    private JsonNode publicInputJson;
    private Map<String, Object> publicEnglishLanguageResource;
    private Map<String, Object> publicWelshLanguageResource;

    @BeforeAll
    void setup() throws IOException {
        try (InputStream inputStream = getClass().getResourceAsStream("/mocks/magistratesAdultCourtList.json")) {
            String inputRaw = IOUtils.toString(inputStream, Charset.defaultCharset());
            standardInputJson = OBJECT_MAPPER.readTree(inputRaw);
        }

        try (InputStream languageFile = Thread.currentThread()
            .getContextClassLoader()
            .getResourceAsStream("templates/languages/en/magistratesAdultCourtListDaily.json")) {
            standardEnglishLanguageResource = OBJECT_MAPPER.readValue(
                Objects.requireNonNull(languageFile).readAllBytes(), new TypeReference<>() {
                });
        }

        try (InputStream languageFile = Thread.currentThread()
            .getContextClassLoader()
            .getResourceAsStream("templates/languages/cy/magistratesAdultCourtListDaily.json")) {
            standardWelshLanguageResource = OBJECT_MAPPER.readValue(
                Objects.requireNonNull(languageFile).readAllBytes(), new TypeReference<>() {
                });
        }

        try (InputStream inputStream = getClass().getResourceAsStream("/mocks/magistratesPublicAdultCourtList.json")) {
            String inputRaw = IOUtils.toString(inputStream, Charset.defaultCharset());
            publicInputJson = OBJECT_MAPPER.readTree(inputRaw);
        }

        try (InputStream languageFile = Thread.currentThread()
            .getContextClassLoader()
            .getResourceAsStream("templates/languages/en/magistratesPublicAdultCourtList.json")) {
            publicEnglishLanguageResource = OBJECT_MAPPER.readValue(
                Objects.requireNonNull(languageFile).readAllBytes(), new TypeReference<>() {
                });
        }

        try (InputStream languageFile = Thread.currentThread()
            .getContextClassLoader()
            .getResourceAsStream("templates/languages/cy/magistratesPublicAdultCourtList.json")) {
            publicWelshLanguageResource = OBJECT_MAPPER.readValue(
                Objects.requireNonNull(languageFile).readAllBytes(), new TypeReference<>() {
                });
        }
    }

    private Map<String, String> createMetaDattaMap(ListType listType, Language language) {
        return Map.of(
            CONTENT_DATE, "1 August 2025",
            PROVENANCE, PROVENANCE,
            LOCATION_NAME, "location",
            LANGUAGE, language.name(),
            LIST_TYPE, listType.name()
        );
    }

    @ParameterizedTest
    @EnumSource(value = ListType.class, names = {MAGISTRATES_ADULT_COURT_LIST_DAILY,
        MAGISTRATES_ADULT_COURT_LIST_FUTURE})
    void testStandardGeneralListInformationInEnglish(ListType listType) throws IOException {
        Map<String, String> metadata = createMetaDattaMap(listType, Language.ENGLISH);
        String outputHtml = standardConverter.convert(standardInputJson, metadata, standardEnglishLanguageResource);
        Document document = Jsoup.parse(outputHtml);
        SoftAssertions softly = new SoftAssertions();

        softly.assertThat(outputHtml)
            .as(HTML_MESSAGE)
            .isNotEmpty();

        softly.assertThat(document.title())
            .as(TITLE_MESSAGE)
            .isEqualTo("Magistrates Standard List");

        softly.assertThat(document.getElementsByClass(GOVUK_HEADING_L).get(0).text())
            .as(HEADER_MESSAGE)
            .contains("Magistrates Standard List for location");

        softly.assertThat(document.getElementsByClass(BODY_CLASS).get(0).text())
            .as(BODY_MESSAGE)
            .isEqualTo("List for 1 August 2025");

        softly.assertThat(document.getElementsByClass(BODY_CLASS).get(1).text())
            .as(BODY_MESSAGE)
            .isEqualTo("Last updated 31 July 2025 at 9:05am");

        softly.assertThat(document.getElementsByClass(BODY_CLASS).get(2).text())
            .as(BODY_MESSAGE)
            .contains("Restrictions on publishing or writing about these cases");

        softly.assertAll();
    }

    @Test
    void testPublicGeneralListInformationInEnglish() throws IOException {
        Map<String, String> metadata = createMetaDattaMap(ListType.MAGISTRATES_PUBLIC_ADULT_COURT_LIST_DAILY,
                                                          Language.ENGLISH);
        String outputHtml = publicConverter.convert(publicInputJson, metadata, publicEnglishLanguageResource);
        Document document = Jsoup.parse(outputHtml);
        SoftAssertions softly = new SoftAssertions();

        softly.assertThat(outputHtml)
            .as(HTML_MESSAGE)
            .isNotEmpty();

        softly.assertThat(document.title())
            .as(TITLE_MESSAGE)
            .isEqualTo("Magistrates Public List");

        softly.assertThat(document.getElementsByClass(GOVUK_HEADING_L).get(0).text())
            .as(HEADER_MESSAGE)
            .contains("Magistrates Public List for location");

        softly.assertThat(document.getElementsByClass(BODY_CLASS).get(0).text())
            .as(BODY_MESSAGE)
            .isEqualTo("List for 1 August 2025");

        softly.assertThat(document.getElementsByClass(BODY_CLASS).get(1).text())
            .as(BODY_MESSAGE)
            .isEqualTo("Last updated 31 July 2025 at 9:05am");

        softly.assertThat(document.getElementsByClass(BODY_CLASS).get(2).text())
            .as(BODY_MESSAGE)
            .contains("Restrictions on publishing or writing about these cases");

        softly.assertAll();
    }

    @ParameterizedTest
    @EnumSource(value = ListType.class, names = {MAGISTRATES_ADULT_COURT_LIST_DAILY,
        MAGISTRATES_ADULT_COURT_LIST_FUTURE})
    void testStandardGeneralListInformationInWelsh(ListType listType) throws IOException {
        Map<String, String> metadata = createMetaDattaMap(listType, Language.WELSH);
        String outputHtml = standardConverter.convert(standardInputJson, metadata, standardWelshLanguageResource);
        Document document = Jsoup.parse(outputHtml);
        SoftAssertions softly = new SoftAssertions();

        softly.assertThat(outputHtml)
            .as(HTML_MESSAGE)
            .isNotEmpty();

        softly.assertThat(document.title())
            .as(TITLE_MESSAGE)
            .isEqualTo("Rhestr Safonol y Llys Ynadon");

        softly.assertThat(document.getElementsByClass(GOVUK_HEADING_L).get(0).text())
            .as(HEADER_MESSAGE)
            .contains("Rhestr Safonol y Llys Ynadon ar gyfer location");

        softly.assertThat(document.getElementsByClass(BODY_CLASS).get(0).text())
            .as(BODY_MESSAGE)
            .isEqualTo("Rhestr ar gyfer 1 August 2025");

        softly.assertThat(document.getElementsByClass(BODY_CLASS).get(1).text())
            .as(BODY_MESSAGE)
            .isEqualTo("Diweddarwyd diwethaf 31 July 2025 am 9:05am");

        softly.assertThat(document.getElementsByClass(BODY_CLASS).get(2).text())
            .as(BODY_MESSAGE)
            .contains("Cyfyngiadau ar gyhoeddi neu ysgrifennu am yr achosion hyn");

        softly.assertAll();
    }

    @Test
    void testPublicGeneralListInformationInWelsh() throws IOException {
        Map<String, String> metadata = createMetaDattaMap(ListType.MAGISTRATES_PUBLIC_ADULT_COURT_LIST_DAILY,
                                                          Language.WELSH);
        String outputHtml = publicConverter.convert(publicInputJson, metadata, publicWelshLanguageResource);
        Document document = Jsoup.parse(outputHtml);
        SoftAssertions softly = new SoftAssertions();

        softly.assertThat(outputHtml)
            .as(HTML_MESSAGE)
            .isNotEmpty();

        softly.assertThat(document.title())
            .as(TITLE_MESSAGE)
            .isEqualTo("Magistrates Public List");

        softly.assertThat(document.getElementsByClass(GOVUK_HEADING_L).get(0).text())
            .as(HEADER_MESSAGE)
            .contains("Magistrates Public List for location");

        softly.assertThat(document.getElementsByClass(BODY_CLASS).get(0).text())
            .as(BODY_MESSAGE)
            .isEqualTo("Rhestr ar gyfer 1 August 2025");

        softly.assertThat(document.getElementsByClass(BODY_CLASS).get(1).text())
            .as(BODY_MESSAGE)
            .isEqualTo("Diweddarwyd diwethaf 31 July 2025 am 9:05am");

        softly.assertThat(document.getElementsByClass(BODY_CLASS).get(2).text())
            .as(BODY_MESSAGE)
            .contains("Cyfyngiadau ar gyhoeddi neu ysgrifennu am yr achosion hyn");

        softly.assertAll();
    }

    @ParameterizedTest
    @EnumSource(value = ListType.class, names = {MAGISTRATES_ADULT_COURT_LIST_DAILY,
        MAGISTRATES_ADULT_COURT_LIST_FUTURE})
    void testStandardSessionHeadings(ListType listType) throws IOException {
        Map<String, String> metadata = createMetaDattaMap(listType, Language.ENGLISH);
        String result = standardConverter.convert(standardInputJson, metadata, standardEnglishLanguageResource);
        assertSessionHeadings(Jsoup.parse(result));
    }

    @Test
    void testPublicSessionHeadings() throws IOException {
        Map<String, String> metadata = createMetaDattaMap(ListType.MAGISTRATES_PUBLIC_ADULT_COURT_LIST_DAILY,
                                                          Language.ENGLISH);
        String result = publicConverter.convert(publicInputJson, metadata, publicEnglishLanguageResource);
        assertSessionHeadings(Jsoup.parse(result));
    }

    private void assertSessionHeadings(Document document) {
        SoftAssertions softly = new SoftAssertions();

        softly.assertThat(document.getElementsByClass("site-header"))
            .as("Session heading count does not match")
            .hasSize(2);

        String firstSessionHeading = document.getElementsByClass("site-header").get(0).text();
        softly.assertThat(firstSessionHeading)
            .as(COURT_ROOM_HEADING_MESSAGE)
            .contains("North Shields Magistrates' Court");

        softly.assertThat(firstSessionHeading)
            .as(COURT_ROOM_HEADING_MESSAGE)
            .contains("Sitting at 1");

        softly.assertThat(firstSessionHeading)
            .as(COURT_ROOM_HEADING_MESSAGE)
            .contains("LJA: North Northumbria Magistrates' Court");

        softly.assertThat(firstSessionHeading)
            .as(COURT_ROOM_HEADING_MESSAGE)
            .contains("Session start 9am");

        softly.assertAll();
    }

    @ParameterizedTest
    @EnumSource(value = ListType.class, names = {MAGISTRATES_ADULT_COURT_LIST_DAILY,
        MAGISTRATES_ADULT_COURT_LIST_FUTURE})
    void testStandardTableHeaders(ListType listType) throws IOException {
        Map<String, String> metadata = createMetaDattaMap(listType, Language.ENGLISH);
        String outputHtml = standardConverter.convert(standardInputJson, metadata, standardEnglishLanguageResource);
        Document document = Jsoup.parse(outputHtml);

        assertThat(document.getElementsByClass("govuk-table__head").get(0)
                              .getElementsByTag("th"))
            .as("Incorrect table headers")
            .hasSize(10)
            .extracting(Element::text)
            .containsExactly(
                "Block Start",
                "Defendant Name",
                "Date of Birth",
                "Address",
                "Age",
                "Informant",
                "Case Number",
                "Offence Code",
                "Offence Title",
                "Offence Summary"
            );
    }

    @Test
    void testPublicTableHeaders() throws IOException {
        Map<String, String> metadata = createMetaDattaMap(ListType.MAGISTRATES_PUBLIC_ADULT_COURT_LIST_DAILY,
                                                          Language.ENGLISH);
        String outputHtml = publicConverter.convert(publicInputJson, metadata, publicEnglishLanguageResource);
        Document document = Jsoup.parse(outputHtml);

        assertThat(document.getElementsByClass("govuk-table__head").get(0)
                       .getElementsByTag("th"))
            .as("Incorrect table headers")
            .hasSize(3)
            .extracting(Element::text)
            .containsExactly(
                "Listing Time",
                "Defendant Name",
                "Case Number"
            );
    }

    @ParameterizedTest
    @EnumSource(value = ListType.class, names = {MAGISTRATES_ADULT_COURT_LIST_DAILY,
        MAGISTRATES_ADULT_COURT_LIST_FUTURE})
    void testTableContents(ListType listType) throws IOException {
        Map<String, String> metadata = createMetaDattaMap(listType, Language.ENGLISH);
        String outputHtml = standardConverter.convert(standardInputJson, metadata, standardEnglishLanguageResource);
        Document document = Jsoup.parse(outputHtml);
        assertThat(document.getElementsByClass("govuk-table__body").get(0)
                       .getElementsByTag("td"))
            .as("Incorrect table body")
            .hasSize(20)
            .extracting(Element::text)
            .contains(
                "9am",
                "Mr Test User",
                "06/11/1975",
                "1 High Street, London, SW1A 1AA",
                "50",
                "POL01",
                "1000000000",
                "TH68001",
                "Offence title 1",
                "Offence summary 1"
            );
    }

    @Test
    void testPublicTableContents() throws IOException {
        Map<String, String> metadata = createMetaDattaMap(ListType.MAGISTRATES_PUBLIC_ADULT_COURT_LIST_DAILY,
                                                          Language.ENGLISH);
        String outputHtml = publicConverter.convert(publicInputJson, metadata, publicEnglishLanguageResource);
        Document document = Jsoup.parse(outputHtml);
        assertThat(document.getElementsByClass("govuk-table__body").get(0)
                       .getElementsByTag("td"))
            .as("Incorrect table body")
            .hasSize(6)
            .extracting(Element::text)
            .contains(
                "9am",
                "Mr Test User",
                "1000000000"
            );
    }
}
