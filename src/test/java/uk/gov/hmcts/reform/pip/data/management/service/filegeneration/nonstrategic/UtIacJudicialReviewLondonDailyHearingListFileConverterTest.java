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

import static uk.gov.hmcts.reform.pip.model.publication.ListType.UT_IAC_JR_LONDON_DAILY_HEARING_LIST;


@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class UtIacJudicialReviewLondonDailyHearingListFileConverterTest {

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

    private static final String SUBSTANTIVE = "Substantive";
    private static final String VENUE_WELSH = "Lleoliad";
    private static final String JUDGES = "Judge(s)";
    private static final String JUDGES_WELSH = "Barnwr/Barnwyr";
    private static final String HEARING_TIME = "Hearing time";
    private static final String HEARING_TIME_WELSH = "Amser y gwrandawiad";
    private static final String CASE_REFERENCE_NUMBER = "Case reference number";
    private static final String HEARING_TYPE = "Hearing type";
    private static final String HEARING_TYPE_WELSH = "Math o wrandawiad";
    private static final String ADDITIONAL_INFORMATION = "Additional information";
    private static final String ADDITIONAL_INFORMATION_WELSH = "Gwybodaeth ychwanegol";

    private static final String LIST_DATE_ENGLISH = "List for 12 December 2024";
    private static final String LIST_DATE_WELSH = "Rhestr ar gyfer 12 December 2024";
    private static final String OBSERVE_HEARING_ENGLISH = "Observe a court or tribunal hearing as a journalist, "
        + "researcher or member of the public";
    private static final String OBSERVE_HEARING_WELSH = "Arsylwi gwrandawiad llys neu dribiwnlys fel newyddiadurwr, "
        + "ymchwilydd neu aelod o'r cyhoedd";
    private static final String HEARING_VENUE = "This is a venue name";

    private static final String HEADER_ELEMENT = "page-heading";
    private static final String LIST_DATE_ELEMENT = "list-date";
    private static final String LAST_UPDATED_DATE_ELEMENT = "last-updated-date";
    private static final String OBSERVE_HEARING_ELEMENT =  "observe-hearing";
    private static final String LIST_UPDATE_MESSAGE_ELEMENT = "list-update-message";
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

    private JsonNode utIacJudicialReviewInputJson;

    @BeforeAll
    void setup() throws IOException {
        try (InputStream inputStream = getClass()
            .getResourceAsStream("/mocks/non-strategic/utIacJudicialReviewLondonDailyHearingList.json")) {
            String inputRaw = IOUtils.toString(inputStream, Charset.defaultCharset());
            utIacJudicialReviewInputJson = new ObjectMapper().readTree(inputRaw);
        }
    }

    @Test
    void testUtIacJudicialReviewDailyHearingListFileConversionInEnglish() throws IOException {
        Map<String, Object> languageResource;
        try (InputStream languageFile = Thread.currentThread()
            .getContextClassLoader()
            .getResourceAsStream("templates/languages/en/non-strategic/utIacJrLondonDailyHearingList.json")) {
            languageResource = new ObjectMapper().readValue(
                Objects.requireNonNull(languageFile).readAllBytes(), new TypeReference<>() {
                });
        }

        Map<String, String> metadata = Map.of(CONTENT_DATE_METADATA, CONTENT_DATE,
                                              PROVENANCE_METADATA, PROVENANCE,
                                              LANGUAGE_METADATA, ENGLISH,
                                              LIST_TYPE_METADATA, UT_IAC_JR_LONDON_DAILY_HEARING_LIST.name(),
                                              LAST_RECEIVED_DATE_METADATA, LAST_RECEIVED_DATE
        );

        String result = converter.convert(utIacJudicialReviewInputJson, metadata, languageResource);
        Document document = Jsoup.parse(result);

        SoftAssertions softly = new SoftAssertions();

        softly.assertThat(document.title())
            .as(TITLE_MESSAGE)
            .isEqualTo("Upper Tribunal (Immigration and Asylum) Chamber - Judicial Review: London Daily Hearing List");

        softly.assertThat(document.getElementById(HEADER_ELEMENT).text())
            .as(HEADER_MESSAGE)
            .isEqualTo("Upper Tribunal (Immigration and Asylum) Chamber - Judicial Review: London Daily Hearing List");

        softly.assertThat(document.getElementById(LIST_DATE_ELEMENT).text())
            .as(LIST_DATE_MESSAGE)
            .isEqualTo(LIST_DATE_ENGLISH);

        softly.assertThat(document.getElementById(LAST_UPDATED_DATE_ELEMENT).text())
            .as(LAST_UPDATED_DATE_MESSAGE)
            .isEqualTo("Last updated 20 January 2025 at 9:30am");

        softly.assertThat(document.getElementsByClass(SUMMARY_TEXT_CLASS).getFirst().text())
            .as(IMPORTANT_INFORMATION_MESSAGE)
            .isEqualTo("Important information");

        softly.assertThat(document.getElementById(LIST_UPDATE_MESSAGE_ELEMENT).text())
            .as(BODY_MESSAGE)
            .contains("The following list is subject to change until 4:30pm. Any alterations after this time "
                         + "will be telephoned or emailed direct to the parties or their legal representatives.");

        softly.assertThat(document.getElementById(OBSERVE_HEARING_ELEMENT).text())
            .as(BODY_MESSAGE)
            .isEqualTo(OBSERVE_HEARING_ENGLISH);

        softly.assertThat(document.getElementsByTag("th"))
            .as(TABLE_HEADERS_MESSAGE)
            .hasSize(8)
            .extracting(Element::text)
            .containsExactly(
                HEARING_TIME,
                "Case title",
                "Representative",
                CASE_REFERENCE_NUMBER,
                JUDGES,
                HEARING_TYPE,
                "Location",
                ADDITIONAL_INFORMATION
            );

        softly.assertThat(document.getElementsByTag("td"))
            .as(TABLE_CONTENT_MESSAGE)
            .hasSize(16)
            .extracting(Element::text)
            .containsExactly(
                "10:30am",
                "Case A",
                "Rep A",
                "1234",
                "Judge A",
                SUBSTANTIVE,
                HEARING_VENUE,
                "This is additional information",
                "11am",
                "Case B",
                "Rep B",
                "1235",
                "Judge B",
                SUBSTANTIVE,
                HEARING_VENUE,
                "This is another additional information"
            );

        softly.assertAll();
    }

    @Test
    void testUtIacJudicialReviewDailyHearingListFileConversionInWelsh() throws IOException {
        Map<String, Object> languageResource;
        try (InputStream languageFile = Thread.currentThread()
            .getContextClassLoader()
            .getResourceAsStream("templates/languages/cy/non-strategic/utIacJrLondonDailyHearingList.json")) {
            languageResource = new ObjectMapper().readValue(
                Objects.requireNonNull(languageFile).readAllBytes(), new TypeReference<>() {
                });
        }

        Map<String, String> metadata = Map.of(CONTENT_DATE_METADATA, CONTENT_DATE,
                                              PROVENANCE_METADATA, PROVENANCE,
                                              LANGUAGE_METADATA, WELSH,
                                              LIST_TYPE_METADATA, UT_IAC_JR_LONDON_DAILY_HEARING_LIST.name(),
                                              LAST_RECEIVED_DATE_METADATA, LAST_RECEIVED_DATE
        );

        String result = converter.convert(utIacJudicialReviewInputJson, metadata, languageResource);
        Document document = Jsoup.parse(result);

        SoftAssertions softly = new SoftAssertions();

        softly.assertThat(document.title())
            .as(TITLE_MESSAGE)
            .isEqualTo("Uwch Dribiwnlys (Mewnfudo a Lloches) - "
                   + "Rhestr o Wrandawiadau Dyddiol Siambr Adolygiadau Barnwrol Llundain");

        softly.assertThat(document.getElementById(HEADER_ELEMENT).text())
            .as(HEADER_MESSAGE)
            .isEqualTo("Uwch Dribiwnlys (Mewnfudo a Lloches) - "
                   + "Rhestr o Wrandawiadau Dyddiol Siambr Adolygiadau Barnwrol Llundain");

        softly.assertThat(document.getElementById(LIST_DATE_ELEMENT).text())
            .as(LIST_DATE_MESSAGE)
            .isEqualTo(LIST_DATE_WELSH);

        softly.assertThat(document.getElementById(LAST_UPDATED_DATE_ELEMENT).text())
            .as(LAST_UPDATED_DATE_MESSAGE)
            .isEqualTo("Diweddarwyd ddiwethaf 20 January 2025 am 9:30am");

        softly.assertThat(document.getElementsByClass(SUMMARY_TEXT_CLASS).getFirst().text())
            .as(IMPORTANT_INFORMATION_MESSAGE)
            .isEqualTo("Gwybodaeth bwysig");

        softly.assertThat(document.getElementById(LIST_UPDATE_MESSAGE_ELEMENT).text())
            .as(BODY_MESSAGE)
            .contains("Gall y rhestr ganlynol newid tan 4:30pm. Bydd unrhyw newidiadau ar ôl yr amser hwn yn cael "
                        + "eu cyfathrebu dros y ffôn neu drwy e-bost yn uniongyrchol at y partïon neu eu "
                        +  "cynrychiolwyr cyfreithiol.");

        softly.assertThat(document.getElementById(OBSERVE_HEARING_ELEMENT).text())
            .as(BODY_MESSAGE)
            .isEqualTo(OBSERVE_HEARING_WELSH);

        softly.assertThat(document.getElementsByTag("th"))
            .as(TABLE_HEADERS_MESSAGE)
            .hasSize(8)
            .extracting(Element::text)
            .containsExactly(
                HEARING_TIME_WELSH,
                "Deitl yr achos",
                "Cynrychiolir gan",
                "Cyfeirnod yr achos",
                JUDGES_WELSH,
                HEARING_TYPE_WELSH,
                VENUE_WELSH,
                ADDITIONAL_INFORMATION_WELSH
            );

        softly.assertThat(document.getElementsByTag("td"))
            .as(TABLE_CONTENT_MESSAGE)
            .hasSize(16)
            .extracting(Element::text)
            .containsExactly(
                "10:30am",
                "Case A",
                "Rep A",
                "1234",
                "Judge A",
                SUBSTANTIVE,
                HEARING_VENUE,
                "This is additional information",
                "11am",
                "Case B",
                "Rep B",
                "1235",
                "Judge B",
                SUBSTANTIVE,
                HEARING_VENUE,
                "This is another additional information"

            );

        softly.assertAll();
    }
}
