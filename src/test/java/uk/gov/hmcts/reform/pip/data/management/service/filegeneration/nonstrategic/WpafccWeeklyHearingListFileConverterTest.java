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

import static uk.gov.hmcts.reform.pip.model.publication.ListType.WPAFCC_WEEKLY_HEARING_LIST;

@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class WpafccWeeklyHearingListFileConverterTest {

    private static final String CONTENT_DATE = "12 December 2024";
    private static final String LAST_RECEIVED_DATE = "2025-01-20T09:30:00Z";
    private static final String PROVENANCE = "provenance";
    private static final String CONTENT_DATE_METADATA = "contentDate";
    private static final String PROVENANCE_METADATA = "provenance";
    private static final String LANGUAGE_METADATA = "language";
    private static final String LIST_TYPE_METADATA = "listType";
    private static final String LAST_RECEIVED_DATE_METADATA = "lastReceivedDate";

    private static final String ENGLISH = "ENGLISH";
    private static final String WELSH = "WELSH";

    private static final String DATE = "Date";
    private static final String DATE_WELSH = "Dyddiad";
    private static final String HEARING_TIME = "Hearing time";
    private static final String HEARING_TIME_WELSH = "Amser y gwrandawiad";
    private static final String CASE_NAME = "Case name";
    private static final String CASE_NAME_WELSH = "Enw’r achos";
    private static final String CASE_REFERENCE_NUMBER = "Case reference number";
    private static final String PANEL = "Panel";
    private static final String HEARING_TYPE_WELSH = "Math o wrandawiad";
    private static final String VENUE = "Venue";
    private static final String VENUE_WELSH = "Lleoliad";
    private static final String ADDITIONAL_INFORMATION = "Additional information";
    private static final String ADDITIONAL_INFORMATION_WELSH = "Gwybodaeth ychwanegol";

    private static final String LIST_DATE_ENGLISH = "List for week commencing 12 December 2024";
    private static final String LIST_DATE_WELSH = "Rhestr ar gyfer yr wythnos yn dechrau ar 12 December 2024";
    private static final String OBSERVE_HEARING_ENGLISH = "Observe a court or tribunal hearing as a journalist, "
        + "researcher or member of the public";
    private static final String OBSERVE_HEARING_WELSH = "Arsylwi gwrandawiad llys neu dribiwnlys fel newyddiadurwr, "
        + "ymchwilydd neu aelod o'r cyhoedd";

    private static final String HEARING_DATE = "16 December 2024";
    private static final String HEARING_VENUE = "This is a venue name";

    private static final String HEADER_ELEMENT = "page-heading";
    private static final String LIST_DATE_ELEMENT = "list-date";
    private static final String LAST_UPDATED_DATE_ELEMENT = "last-updated-date";
    private static final String OBSERVE_HEARING_ELEMENT = "observe-hearing";
    private static final String JOIN_HEARING_MESSAGE_ELEMENT = "join-hearing-message";
    private static final String SUMMARY_TEXT_CLASS = "govuk-details__summary-text";

    private static final String TITLE_MESSAGE = "Title does not match";
    private static final String HEADER_MESSAGE = "Header does not match";
    private static final String LIST_DATE_MESSAGE = "List date does not match";
    private static final String LAST_UPDATED_DATE_MESSAGE = "Last updated date does not match";
    private static final String IMPORTANT_INFORMATION_MESSAGE = "Important information heading does not match";
    private static final String BODY_MESSAGE = "Body does not match";
    private static final String TABLE_HEADERS_MESSAGE = "Table headers does not match";
    private static final String TABLE_CONTENT_MESSAGE = "Table content does not match";

    private final NonStrategicListFileConverter converter = new NonStrategicListFileConverter();

    private JsonNode wpafccInputJson;

    @BeforeAll
    void setup() throws IOException {
        try (InputStream inputStream = getClass()
            .getResourceAsStream("/mocks/non-strategic/wpafccWeeklyHearingList.json")) {
            String inputRaw = IOUtils.toString(inputStream, Charset.defaultCharset());
            wpafccInputJson = new ObjectMapper().readTree(inputRaw);
        }
    }

    @Test
    void testWpafccWeeklyHearingListFileConversionInEnglish() throws IOException {
        Map<String, Object> languageResource;
        try (InputStream languageFile = Thread.currentThread()
            .getContextClassLoader()
            .getResourceAsStream("templates/languages/en/non-strategic/wpafccWeeklyHearingList.json")) {
            languageResource = new ObjectMapper().readValue(
                Objects.requireNonNull(languageFile).readAllBytes(), new TypeReference<>() {
                });
        }

        Map<String, String> metadata = Map.of(CONTENT_DATE_METADATA, CONTENT_DATE,
                                              PROVENANCE_METADATA, PROVENANCE,
                                              LANGUAGE_METADATA, ENGLISH,
                                              LIST_TYPE_METADATA, WPAFCC_WEEKLY_HEARING_LIST.name(),
                                              LAST_RECEIVED_DATE_METADATA, LAST_RECEIVED_DATE
        );

        String result = converter.convert(wpafccInputJson, metadata, languageResource);
        Document document = Jsoup.parse(result);

        SoftAssertions softly = new SoftAssertions();

        softly.assertThat(document.title())
            .as(TITLE_MESSAGE)
            .isEqualTo("First-tier Tribunal (War Pensions and Armed Forces Compensation) Weekly Hearing List");

        softly.assertThat(document.getElementById(HEADER_ELEMENT).text())
            .as(HEADER_MESSAGE)
            .isEqualTo("First-tier Tribunal (War Pensions and Armed Forces Compensation) Weekly Hearing List");

        softly.assertThat(document.getElementById(LIST_DATE_ELEMENT).text())
            .as(LIST_DATE_MESSAGE)
            .isEqualTo(LIST_DATE_ENGLISH);

        softly.assertThat(document.getElementById(LAST_UPDATED_DATE_ELEMENT).text())
            .as(LAST_UPDATED_DATE_MESSAGE)
            .isEqualTo("Last updated 20 January 2025 at 9:30am");

        softly.assertThat(document.getElementsByClass(SUMMARY_TEXT_CLASS).getFirst().text())
            .as(IMPORTANT_INFORMATION_MESSAGE)
            .isEqualTo("Important information");

        softly.assertThat(document.getElementById(JOIN_HEARING_MESSAGE_ELEMENT))
            .as(BODY_MESSAGE)
            .extracting(Element::text)
            .asString()
            .contains("Members of the public wishing to observe a hearing or representatives of the media may, "
                          + "on their request, join any telephone or video hearing remotely");

        softly.assertThat(document.getElementById(OBSERVE_HEARING_ELEMENT).text())
            .as(BODY_MESSAGE)
            .isEqualTo(OBSERVE_HEARING_ENGLISH);

        softly.assertThat(document.getElementsByTag("th"))
            .as(TABLE_HEADERS_MESSAGE)
            .hasSize(8)
            .extracting(Element::text)
            .containsExactly(
                DATE,
                HEARING_TIME,
                CASE_REFERENCE_NUMBER,
                CASE_NAME,
                PANEL,
                "Mode of hearing",
                VENUE,
                ADDITIONAL_INFORMATION
            );

        softly.assertThat(document.getElementsByTag("td"))
            .as(TABLE_CONTENT_MESSAGE)
            .hasSize(16)
            .extracting(Element::text)
            .containsExactly(
                HEARING_DATE,
                "10:15am",
                "1234",
                "This is a case name",
                "Judge A, Member A",
                "Case Management Hearing",
                HEARING_VENUE,
                "This is additional information",
                HEARING_DATE,
                "10:30am",
                "1235",
                "This is another case name",
                "Judge B, Member B",
                "Oral Hearing",
                HEARING_VENUE,
                "This is another additional information"
            );

        softly.assertAll();
    }

    @Test
    void testWpafccWeeklyHearingListFileConversionInWelsh() throws IOException {
        Map<String, Object> languageResource;
        try (InputStream languageFile = Thread.currentThread()
            .getContextClassLoader()
            .getResourceAsStream("templates/languages/cy/non-strategic/wpafccWeeklyHearingList.json")) {
            languageResource = new ObjectMapper().readValue(
                Objects.requireNonNull(languageFile).readAllBytes(), new TypeReference<>() {
                });
        }

        Map<String, String> metadata = Map.of(CONTENT_DATE_METADATA, CONTENT_DATE,
                                              PROVENANCE_METADATA, PROVENANCE,
                                              LANGUAGE_METADATA, WELSH,
                                              LIST_TYPE_METADATA, WPAFCC_WEEKLY_HEARING_LIST.name(),
                                              LAST_RECEIVED_DATE_METADATA, LAST_RECEIVED_DATE
        );

        String result = converter.convert(wpafccInputJson, metadata, languageResource);
        Document document = Jsoup.parse(result);

        SoftAssertions softly = new SoftAssertions();

        softly.assertThat(document.title())
            .as(TITLE_MESSAGE)
            .isEqualTo("Tribiwnlys Haen Gyntaf (Iawndal Pensiynau Rhyfel a’r Lluoedd Arfog) - Rhestr o "
                           + "Wrandawiadau Wythnosol");

        softly.assertThat(document.getElementById(HEADER_ELEMENT).text())
            .as(HEADER_MESSAGE)
            .isEqualTo("Tribiwnlys Haen Gyntaf (Iawndal Pensiynau Rhyfel a’r Lluoedd Arfog) - Rhestr o "
                           + "Wrandawiadau Wythnosol");

        softly.assertThat(document.getElementById(LIST_DATE_ELEMENT).text())
            .as(LIST_DATE_MESSAGE)
            .isEqualTo(LIST_DATE_WELSH);

        softly.assertThat(document.getElementById(LAST_UPDATED_DATE_ELEMENT).text())
            .as(LAST_UPDATED_DATE_MESSAGE)
            .isEqualTo("Diweddarwyd ddiwethaf 20 January 2025 am 9:30am");

        softly.assertThat(document.getElementsByClass(SUMMARY_TEXT_CLASS).getFirst().text())
            .as(IMPORTANT_INFORMATION_MESSAGE)
            .isEqualTo("Gwybodaeth bwysig");

        softly.assertThat(document.getElementById(JOIN_HEARING_MESSAGE_ELEMENT).text())
            .as(BODY_MESSAGE)
            .contains("Gall aelodau o’r cyhoedd sy’n dymuno arsylwi gwrandawiad neu gynrychiolwyr y cyfryngau ymuno "
                          + "ag unrhyw wrandawiad dros y ffôn neu drwy fideo o bell");

        softly.assertThat(document.getElementById(OBSERVE_HEARING_ELEMENT).text())
            .as(BODY_MESSAGE)
            .isEqualTo(OBSERVE_HEARING_WELSH);

        softly.assertThat(document.getElementsByTag("th"))
            .as(TABLE_HEADERS_MESSAGE)
            .hasSize(8)
            .extracting(Element::text)
            .containsExactly(
                DATE_WELSH,
                HEARING_TIME_WELSH,
                "Cyfeirnod yr achos",
                CASE_NAME_WELSH,
                PANEL,
                HEARING_TYPE_WELSH,
                VENUE_WELSH,
                ADDITIONAL_INFORMATION_WELSH
            );

        softly.assertThat(document.getElementsByTag("td"))
            .as(TABLE_CONTENT_MESSAGE)
            .hasSize(16)
            .extracting(Element::text)
            .containsExactly(
                HEARING_DATE,
                "10:15am",
                "1234",
                "This is a case name",
                "Judge A, Member A",
                "Case Management Hearing",
                HEARING_VENUE,
                "This is additional information",
                HEARING_DATE,
                "10:30am",
                "1235",
                "This is another case name",
                "Judge B, Member B",
                "Oral Hearing",
                HEARING_VENUE,
                "This is another additional information"
            );

        softly.assertAll();
    }
}
