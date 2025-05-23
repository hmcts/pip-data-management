package uk.gov.hmcts.reform.pip.data.management.service.filegeneration.nonstrategic;

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
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.pip.data.management.service.filegeneration.NonStrategicListFileConverter;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.Map;
import java.util.Objects;

import static uk.gov.hmcts.reform.pip.model.publication.ListType.INTERIM_APPLICATIONS_CHD_DAILY_CAUSE_LIST;

@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class InterimApplicationsChanceryDivisionDailyCauseListFileConverterTest {
    private static final String CONTENT_DATE = "20 April 2025";
    private static final String LAST_RECEIVED_DATE = "2025-02-19T10:30:00Z";
    private static final String PROVENANCE = "provenance";
    private static final String CONTENT_DATE_METADATA = "contentDate";
    private static final String PROVENANCE_METADATA = "provenance";
    private static final String LANGUAGE_METADATA = "language";
    private static final String LIST_TYPE_METADATA = "listType";
    private static final String LAST_RECEIVED_DATE_METADATA = "lastReceivedDate";

    private static final String ENGLISH = "ENGLISH";
    private static final String WELSH = "WELSH";

    private static final String HEADER_ELEMENT = "page-heading";
    private static final String VENUE_LINE1_ELEMENT = "venue-line-1";
    private static final String VENUE_LINE2_ELEMENT = "venue-line-2";
    private static final String VENUE_LINE3_ELEMENT = "venue-line-3";
    private static final String LIST_DATE_ELEMENT = "list-date";
    private static final String LAST_UPDATED_DATE_ELEMENT = "last-updated-date";
    private static final String SUMMARY_TEXT_CLASS = "govuk-details__summary-text";

    private static final String HEARING_TIME = "10:30am";
    private static final String HEARING_VENUE = "This is a venue name";
    private static final String CASE_NAME = "This is a case name";
    private static final String ADDITIONAL_INFORMATION = "This is additional information";

    private static final String TITLE_MESSAGE = "Title does not match";
    private static final String HEADER_MESSAGE = "Header does not match";
    private static final String VENUE_MESSAGE = "Venue does not match";
    private static final String LIST_DATE_MESSAGE = "List date does not match";
    private static final String LAST_UPDATED_DATE_MESSAGE = "Last updated date does not match";
    private static final String IMPORTANT_INFORMATION_MESSAGE = "Important information heading does not match";
    private static final String TABLE_HEADERS_MESSAGE = "Table headers does not match";
    private static final String TABLE_CONTENT_MESSAGE = "Table content does not match";

    private final NonStrategicListFileConverter converter = new NonStrategicListFileConverter();

    private JsonNode cstInputJson;

    @BeforeAll
    void setup() throws IOException {
        try (InputStream inputStream = getClass()
            .getResourceAsStream("/mocks/non-strategic/interimApplicationsChanceryDivisionDailyCauseList.json")) {
            String inputRaw = IOUtils.toString(inputStream, Charset.defaultCharset());
            cstInputJson = new ObjectMapper().readTree(inputRaw);
        }
    }

    @Test
    void testInterimApplicationsDailyListFileConversionInEnglish() throws IOException {
        Map<String, Object> languageResource;
        try (InputStream languageFile = Thread.currentThread()
            .getContextClassLoader()
            .getResourceAsStream(
                "templates/languages/en/non-strategic/interimApplicationsChdDailyCauseList.json")) {
            languageResource = new ObjectMapper().readValue(
                Objects.requireNonNull(languageFile).readAllBytes(), new TypeReference<>() {
                });
        }

        Map<String, String> metadata = Map.of(CONTENT_DATE_METADATA, CONTENT_DATE,
                                              PROVENANCE_METADATA, PROVENANCE,
                                              LANGUAGE_METADATA, ENGLISH,
                                              LIST_TYPE_METADATA, INTERIM_APPLICATIONS_CHD_DAILY_CAUSE_LIST.name(),
                                              LAST_RECEIVED_DATE_METADATA, LAST_RECEIVED_DATE
        );

        String result = converter.convert(cstInputJson, metadata, languageResource);
        Document document = Jsoup.parse(result);

        SoftAssertions softly = new SoftAssertions();

        softly.assertThat(document.title())
            .as(TITLE_MESSAGE)
            .isEqualTo("Interim Applications List (Chancery Division) Daily Cause List");

        softly.assertThat(document.getElementById(HEADER_ELEMENT).text())
            .as(HEADER_MESSAGE)
            .isEqualTo("Interim Applications List (Chancery Division) Daily Cause List");

        softly.assertThat(document.getElementById(VENUE_LINE1_ELEMENT).text())
            .as(VENUE_MESSAGE)
            .isEqualTo("Rolls Building");

        softly.assertThat(document.getElementById(VENUE_LINE2_ELEMENT).text())
            .as(VENUE_MESSAGE)
            .isEqualTo("Fetter Lane, London");

        softly.assertThat(document.getElementById(VENUE_LINE3_ELEMENT).text())
            .as(VENUE_MESSAGE)
            .isEqualTo("EC4A 1NL");

        softly.assertThat(document.getElementById(LIST_DATE_ELEMENT).text())
            .as(LIST_DATE_MESSAGE)
            .isEqualTo("List for 20 April 2025");

        softly.assertThat(document.getElementById(LAST_UPDATED_DATE_ELEMENT).text())
            .as(LAST_UPDATED_DATE_MESSAGE)
            .isEqualTo("Last updated 19 February 2025 at 10:30am");

        softly.assertThat(document.getElementsByClass(SUMMARY_TEXT_CLASS).getFirst().text())
            .as(IMPORTANT_INFORMATION_MESSAGE)
            .isEqualTo("Important information");

        softly.assertThat(document.getElementsByTag("th"))
            .as(TABLE_HEADERS_MESSAGE)
            .hasSize(7)
            .extracting(Element::text)
            .containsExactly(
                "Judge",
                "Time",
                "Venue",
                "Type",
                "Case number",
                "Case name",
                "Additional information"
            );

        softly.assertThat(document.getElementsByTag("td"))
            .as(TABLE_CONTENT_MESSAGE)
            .hasSize(21)
            .extracting(Element::text)
            .containsExactly(
                "Judge A",
                HEARING_TIME,
                HEARING_VENUE,
                "Case type A",
                "1234",
                CASE_NAME,
                ADDITIONAL_INFORMATION,
                "Judge B",
                HEARING_TIME,
                HEARING_VENUE,
                "Case type B",
                "3456",
                CASE_NAME,
                ADDITIONAL_INFORMATION,
                "Judge C",
                HEARING_TIME,
                HEARING_VENUE,
                "Case type C",
                "5678",
                CASE_NAME,
                ADDITIONAL_INFORMATION
            );

        softly.assertAll();
    }

    @Test
    void testInterimApplicationsDailyListFileConversionInWelsh() throws IOException {
        Map<String, Object> languageResource;
        try (InputStream languageFile = Thread.currentThread()
            .getContextClassLoader()
            .getResourceAsStream("templates/languages/cy/non-strategic/astDailyHearingList.json")) {
            languageResource = new ObjectMapper().readValue(
                Objects.requireNonNull(languageFile).readAllBytes(), new TypeReference<>() {
                });
        }

        Map<String, String> metadata = Map.of(CONTENT_DATE_METADATA, CONTENT_DATE,
                                              PROVENANCE_METADATA, PROVENANCE,
                                              LANGUAGE_METADATA, WELSH,
                                              LIST_TYPE_METADATA, INTERIM_APPLICATIONS_CHD_DAILY_CAUSE_LIST.name(),
                                              LAST_RECEIVED_DATE_METADATA, LAST_RECEIVED_DATE
        );

        String result = converter.convert(cstInputJson, metadata, languageResource);
        Document document = Jsoup.parse(result);

        SoftAssertions softly = new SoftAssertions();

        softly.assertThat(document.title())
            .as(TITLE_MESSAGE)
            .isEqualTo("Rhestr Achosion Dyddiol Ceisiadau Interim (Adran Siawnsri)");

        softly.assertThat(document.getElementById(HEADER_ELEMENT).text())
            .as(HEADER_MESSAGE)
            .isEqualTo("Rhestr Achosion Dyddiol Ceisiadau Interim (Adran Siawnsri)");

        softly.assertThat(document.getElementById(VENUE_LINE1_ELEMENT).text())
            .as(VENUE_MESSAGE)
            .isEqualTo("Adeilad Rolls");

        softly.assertThat(document.getElementById(VENUE_LINE2_ELEMENT).text())
            .as(VENUE_MESSAGE)
            .isEqualTo("Fetter Lane, London");

        softly.assertThat(document.getElementById(VENUE_LINE3_ELEMENT).text())
            .as(VENUE_MESSAGE)
            .isEqualTo("EC4A 1NL");

        softly.assertThat(document.getElementById(LIST_DATE_ELEMENT).text())
            .as(LIST_DATE_MESSAGE)
            .isEqualTo("Rhestr ar gyfer 20 April 2025");

        softly.assertThat(document.getElementById(LAST_UPDATED_DATE_ELEMENT).text())
            .as(LAST_UPDATED_DATE_MESSAGE)
            .isEqualTo("Diweddarwyd ddiwethaf 19 February 2025 am 10:30am");

        softly.assertThat(document.getElementsByClass(SUMMARY_TEXT_CLASS).getFirst().text())
            .as(IMPORTANT_INFORMATION_MESSAGE)
            .isEqualTo("Gwybodaeth bwysig");

        softly.assertThat(document.getElementsByTag("th"))
            .as(TABLE_HEADERS_MESSAGE)
            .hasSize(7)
            .extracting(Element::text)
            .containsExactly(
                "Barnwr",
                "Amser",
                "Lleoliad",
                "Math",
                "Rhif yr achos",
                "Enw’r achos",
                "Gwybodaeth ychwanegol"
            );

        softly.assertThat(document.getElementsByTag("td"))
            .as(TABLE_CONTENT_MESSAGE)
            .hasSize(21)
            .extracting(Element::text)
            .containsExactly(
                "Judge A",
                HEARING_TIME,
                HEARING_VENUE,
                "Case type A",
                "1234",
                CASE_NAME,
                ADDITIONAL_INFORMATION,
                "Judge B",
                HEARING_TIME,
                HEARING_VENUE,
                "Case type B",
                "3456",
                CASE_NAME,
                ADDITIONAL_INFORMATION,
                "Judge C",
                HEARING_TIME,
                HEARING_VENUE,
                "Case type C",
                "5678",
                CASE_NAME,
                ADDITIONAL_INFORMATION
            );

        softly.assertAll();
    }
}
