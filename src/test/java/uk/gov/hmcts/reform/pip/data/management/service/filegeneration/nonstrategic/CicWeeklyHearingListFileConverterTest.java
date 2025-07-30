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

import static uk.gov.hmcts.reform.pip.model.publication.ListType.CIC_WEEKLY_HEARING_LIST;

@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class CicWeeklyHearingListFileConverterTest {
    private static final String CONTENT_DATE = "27 June 2025";
    private static final String LAST_RECEIVED_DATE = "2025-06-26T09:30:00Z";
    private static final String PROVENANCE = "provenance";
    private static final String CONTENT_DATE_METADATA = "contentDate";
    private static final String PROVENANCE_METADATA = "provenance";
    private static final String LANGUAGE_METADATA = "language";
    private static final String LIST_TYPE_METADATA = "listType";
    private static final String LAST_RECEIVED_DATE_METADATA = "lastReceivedDate";

    private static final String ENGLISH = "ENGLISH";
    private static final String WELSH = "WELSH";

    private static final String HEADER_ELEMENT = "page-heading";
    private static final String LIST_DATE_ELEMENT = "list-date";
    private static final String LAST_UPDATED_DATE_ELEMENT = "last-updated-date";
    private static final String SUMMARY_TEXT_CLASS = "govuk-details__summary-text";

    private static final String TITLE_MESSAGE = "Title does not match";
    private static final String HEADER_MESSAGE = "Header does not match";
    private static final String LIST_DATE_MESSAGE = "List date does not match";
    private static final String LAST_UPDATED_DATE_MESSAGE = "Last updated date does not match";
    private static final String IMPORTANT_INFORMATION_MESSAGE = "Important information message does not match";
    private static final String BODY_MESSAGE = "Body does not match";
    private static final String TABLE_HEADERS_MESSAGE = "Table headers does not match";
    private static final String TABLE_CONTENT_MESSAGE = "Table content does not match";

    private final NonStrategicListFileConverter converter = new NonStrategicListFileConverter();

    private JsonNode cstInputJson;

    @BeforeAll
    void setup() throws IOException {
        try (InputStream inputStream = getClass()
            .getResourceAsStream("/mocks/non-strategic/cicWeeklyHearingList.json")) {
            String inputRaw = IOUtils.toString(inputStream, Charset.defaultCharset());
            cstInputJson = new ObjectMapper().readTree(inputRaw);
        }
    }

    @Test
    void testCicWeeklyHearingListFileConversionInEnglish() throws IOException {
        Map<String, Object> languageResource;
        try (InputStream languageFile = Thread.currentThread()
            .getContextClassLoader()
            .getResourceAsStream("templates/languages/en/non-strategic/cicWeeklyHearingList.json")) {
            languageResource = new ObjectMapper().readValue(
                Objects.requireNonNull(languageFile).readAllBytes(), new TypeReference<>() {
                }
            );
        }

        Map<String, String> metadata = Map.of(
            CONTENT_DATE_METADATA, CONTENT_DATE,
            PROVENANCE_METADATA, PROVENANCE,
            LANGUAGE_METADATA, ENGLISH,
            LIST_TYPE_METADATA, CIC_WEEKLY_HEARING_LIST.name(),
            LAST_RECEIVED_DATE_METADATA, LAST_RECEIVED_DATE
        );

        String result = converter.convert(cstInputJson, metadata, languageResource);
        Document document = Jsoup.parse(result);

        SoftAssertions softly = new SoftAssertions();

        softly.assertThat(document.title())
            .as(TITLE_MESSAGE)
            .isEqualTo("Criminal Injuries Compensation Weekly Hearing List");

        softly.assertThat(document.getElementById(HEADER_ELEMENT).text())
            .as(HEADER_MESSAGE)
            .isEqualTo("Criminal Injuries Compensation Weekly Hearing List");

        softly.assertThat(document.getElementById(LIST_DATE_ELEMENT).text())
            .as(LIST_DATE_MESSAGE)
            .isEqualTo("List for week commencing 27 June 2025");

        softly.assertThat(document.getElementById(LAST_UPDATED_DATE_ELEMENT).text())
            .as(LAST_UPDATED_DATE_MESSAGE)
            .isEqualTo("Last updated 26 June 2025 at 10:30am");

        softly.assertThat(document.getElementsByClass(SUMMARY_TEXT_CLASS).getFirst().text())
            .as(IMPORTANT_INFORMATION_MESSAGE)
            .isEqualTo("Important information");

        softly.assertThat(document.getElementById("open-justice-statement-line1"))
            .as(IMPORTANT_INFORMATION_MESSAGE)
            .extracting(Element::text)
            .asString()
            .contains("Open justice is a fundamental principle of our justice system.");

        softly.assertThat(document.getElementById("restricted-reporting-orders-message"))
            .as(IMPORTANT_INFORMATION_MESSAGE)
            .extracting(Element::text)
            .asString()
            .contains("The inclusion of a case in the Press List is no guarantee that it is not subject to a "
                          + "restricted reporting order.");


        softly.assertThat(document.getElementsByClass("govuk-link").get(0)
                              .getElementsByTag("a").get(0)
                              .attr("href"))
            .as(IMPORTANT_INFORMATION_MESSAGE)
            .isEqualTo("https://www.gov.uk/guidance/observe-a-court-or-tribunal-hearing");

        softly.assertThat(document.getElementsByTag("th"))
            .as(TABLE_HEADERS_MESSAGE)
            .hasSize(8)
            .extracting(Element::text)
            .containsExactly(
                "Date",
                "Hearing time",
                "Case reference number",
                "Case name",
                "Venue/Platform",
                "Judge(s)",
                "Member(s)",
                "Additional information"
            );

        softly.assertThat(document.getElementsByTag("td"))
            .as(TABLE_CONTENT_MESSAGE)
            .hasSize(16)
            .extracting(Element::text)
            .containsExactly(
                "26 June 2025",
                "10am",
                "1234",
                "This is a case name",
                "This is a venue name",
                "Judge A",
                "Member A",
                "This is additional information",
                "26 June 2025",
                "10:30am",
                "1235",
                "This is another case name",
                "This is another venue name",
                "Judge B",
                "Member B",
                "This is another additional information"
            );

        softly.assertAll();
    }

    @Test
    void testCicWeeklyHearingListFileConversionInWelsh() throws IOException {
        Map<String, Object> languageResource;
        try (InputStream languageFile = Thread.currentThread()
            .getContextClassLoader()
            .getResourceAsStream("templates/languages/cy/non-strategic/cicWeeklyHearingList.json")) {
            languageResource = new ObjectMapper().readValue(
                Objects.requireNonNull(languageFile).readAllBytes(), new TypeReference<>() {
                });
        }

        Map<String, String> metadata = Map.of(CONTENT_DATE_METADATA, CONTENT_DATE,
                                              PROVENANCE_METADATA, PROVENANCE,
                                              LANGUAGE_METADATA, WELSH,
                                              LIST_TYPE_METADATA, CIC_WEEKLY_HEARING_LIST.name(),
                                              LAST_RECEIVED_DATE_METADATA, LAST_RECEIVED_DATE
        );

        String result = converter.convert(cstInputJson, metadata, languageResource);
        Document document = Jsoup.parse(result);

        SoftAssertions softly = new SoftAssertions();

        softly.assertThat(document.title())
            .as(TITLE_MESSAGE)
            .isEqualTo("Rhestr Gwrandawiadau Wythnosol y Tribiwnlys Digolledu am Anafiadau Troseddol");

        softly.assertThat(document.getElementById(HEADER_ELEMENT).text())
            .as(HEADER_MESSAGE)
            .isEqualTo("Rhestr Gwrandawiadau Wythnosol y Tribiwnlys Digolledu am Anafiadau Troseddol");

        softly.assertThat(document.getElementById(LIST_DATE_ELEMENT).text())
            .as(LIST_DATE_MESSAGE)
            .isEqualTo("Rhestr ar gyfer yr wythnos yn dechrau ar 27 June 2025");

        softly.assertThat(document.getElementById(LAST_UPDATED_DATE_ELEMENT).text())
            .as(LAST_UPDATED_DATE_MESSAGE)
            .isEqualTo("Diweddarwyd ddiwethaf 26 June 2025 am 10:30am");

        softly.assertThat(document.getElementsByClass(SUMMARY_TEXT_CLASS).getFirst().text())
            .as(IMPORTANT_INFORMATION_MESSAGE)
            .isEqualTo("Gwybodaeth bwysig");

        softly.assertThat(document.getElementById("open-justice-statement-line1"))
            .as(BODY_MESSAGE)
            .extracting(Element::text)
            .asString()
            .contains("Mae cyfiawnder agored yn egwyddor sylfaenol ein system gyfiawnder.");

        softly.assertThat(document.getElementById("restricted-reporting-orders-message"))
            .as(BODY_MESSAGE)
            .extracting(Element::text)
            .asString()
            .contains("Nid yw'r ffaith bod achos wedi ei gynnwys yn Rhestr y Wasg yn gwarantu na fydd yn destun "
                          + "gorchymyn adrodd cyfyngedig. ");

        softly.assertThat(document.getElementsByClass("govuk-link").get(0)
                              .getElementsByTag("a").get(0)
                              .attr("href"))
            .as(IMPORTANT_INFORMATION_MESSAGE)
            .isEqualTo("https://www.gov.uk/guidance/observe-a-court-or-tribunal-hearing");

        softly.assertThat(document.getElementsByTag("th"))
            .as(TABLE_HEADERS_MESSAGE)
            .hasSize(8)
            .extracting(Element::text)
            .containsExactly(
                "Dyddiad",
                "Amser y gwrandawiad",
                "Cyfeirnod yr achos",
                "Enw’r achos",
                "Lleoliad/Platfform",
                "Barnwyr",
                "Aelod(au)",
                "Gwybodaeth ychwanegol"
            );

        softly.assertAll();
    }
}
