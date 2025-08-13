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

    private static final String HEADER_MESSAGE = "Incorrect header text";
    private static final String BODY_MESSAGE = "Incorrect body text";
    private static final String COURT_ROOM_HEADING_MESSAGE = "Court room heading does not match";
    private static final String MAGISTRATES_ADULT_COURT_LIST_DAILY = "MAGISTRATES_ADULT_COURT_LIST_DAILY";
    private static final String MAGISTRATES_ADULT_COURT_LIST_FUTURE = "MAGISTRATES_ADULT_COURT_LIST_FUTURE";

    private static final String BODY_CLASS = "govuk-body";

    private final MagistratesAdultCourtListFileConverter converter = new MagistratesAdultCourtListFileConverter();

    private JsonNode inputJson;
    private Map<String, Object> englishLanguageResource;
    private Map<String, Object> welshLanguageResource;

    @BeforeAll
    void setup() throws IOException {
        try (InputStream inputStream = getClass().getResourceAsStream("/mocks/magistratesAdultCourtList.json")) {
            String inputRaw = IOUtils.toString(inputStream, Charset.defaultCharset());
            inputJson = OBJECT_MAPPER.readTree(inputRaw);
        }

        try (InputStream languageFile = Thread.currentThread()
            .getContextClassLoader()
            .getResourceAsStream("templates/languages/en/magistratesAdultCourtListDaily.json")) {
            englishLanguageResource = OBJECT_MAPPER.readValue(
                Objects.requireNonNull(languageFile).readAllBytes(), new TypeReference<>() {
                });
        }

        try (InputStream languageFile = Thread.currentThread()
            .getContextClassLoader()
            .getResourceAsStream("templates/languages/cy/magistratesAdultCourtListDaily.json")) {
            welshLanguageResource = OBJECT_MAPPER.readValue(
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
    void testGeneralListInformationInEnglish(ListType listType) throws IOException {
        Map<String, String> metadata = createMetaDattaMap(listType, Language.ENGLISH);
        String outputHtml = converter.convert(inputJson, metadata, englishLanguageResource);
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
    void testGeneralListInformationInWelsh(ListType listType) throws IOException {
        Map<String, String> metadata = createMetaDattaMap(listType, Language.WELSH);
        String outputHtml = converter.convert(inputJson, metadata, welshLanguageResource);
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
    void testSessionHeadings(ListType listType) throws IOException {
        Map<String, String> metadata = createMetaDattaMap(listType, Language.ENGLISH);
        String result = converter.convert(inputJson, metadata, englishLanguageResource);
        Document document = Jsoup.parse(result);
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
    void testTableHeaders(ListType listType) throws IOException {
        Map<String, String> metadata = createMetaDattaMap(listType, Language.ENGLISH);
        String outputHtml = converter.convert(inputJson, metadata, englishLanguageResource);
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

    @ParameterizedTest
    @EnumSource(value = ListType.class, names = {MAGISTRATES_ADULT_COURT_LIST_DAILY,
        MAGISTRATES_ADULT_COURT_LIST_FUTURE})
    void testTableContents(ListType listType) throws IOException {
        Map<String, String> metadata = createMetaDattaMap(listType, Language.ENGLISH);
        String outputHtml = converter.convert(inputJson, metadata, englishLanguageResource);
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
}
