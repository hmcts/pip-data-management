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

import static uk.gov.hmcts.reform.pip.model.publication.ListType.BRISTOL_AND_CARDIFF_ADMINISTRATIVE_COURT_DAILY_CAUSE_LIST;

@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class BristolAndCardiffAdministrativeCourtDailyCauseListFileConverterTest {

    private static final String CONTENT_DATE = "23 April 2025";
    private static final String LAST_RECEIVED_DATE = "2025-04-22T09:30:00Z";
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
    private static final String SUMMARY_TITLE_CLASS = "govuk-details__summary-text";

    private static final String IMPORTANT_INFORMATION_HEADING_2 = "important-information-heading-2";

    private static final String IMPORTANT_INFORMATION_ELEMENT_1 = "important-information-line-1";
    private static final String IMPORTANT_INFORMATION_ELEMENT_2 = "important-information-line-2";

    private static final String TITLE_MESSAGE = "Title does not match";
    private static final String HEADER_MESSAGE = "Header does not match";
    private static final String LIST_DATE_MESSAGE = "List date does not match";
    private static final String LAST_UPDATED_DATE_MESSAGE = "Last updated date does not match";
    private static final String IMPORTANT_INFORMATION_MESSAGE = "Important information heading does not match";
    private static final String TABLE_HEADERS_MESSAGE = "Table headers does not match";
    private static final String BODY_MESSAGE = "Body does not match";

    private final NonStrategicListFileConverter converter = new NonStrategicListFileConverter();

    private JsonNode inputJson;

    @BeforeAll
    void setup() throws IOException {
        try (InputStream inputStream = getClass()
            .getResourceAsStream("/mocks/non-strategic/administrativeCourtDailyCauseList.json")) {
            String inputRaw = IOUtils.toString(inputStream, Charset.defaultCharset());
            inputJson = new ObjectMapper().readTree(inputRaw);
        }
    }

    @Test
    void testBristolAndCardiffAdministrativeCourtDailyCauseListFileConversionInEnglish() throws IOException {
        Map<String, Object> languageResource;
        try (InputStream languageFile = Thread.currentThread()
            .getContextClassLoader()
            .getResourceAsStream("templates/languages/en/non-strategic/"
                                     + "bristolAndCardiffAdministrativeCourtDailyCauseList.json")) {
            languageResource = new ObjectMapper().readValue(
                Objects.requireNonNull(languageFile).readAllBytes(), new TypeReference<>() {
                });
        }

        Map<String, String> metadata = Map.of(CONTENT_DATE_METADATA, CONTENT_DATE,
                                              PROVENANCE_METADATA, PROVENANCE,
                                              LANGUAGE_METADATA, ENGLISH, LIST_TYPE_METADATA,
                                              BRISTOL_AND_CARDIFF_ADMINISTRATIVE_COURT_DAILY_CAUSE_LIST.name(),
                                              LAST_RECEIVED_DATE_METADATA, LAST_RECEIVED_DATE
        );

        String result = converter.convert(inputJson, metadata, languageResource);
        Document document = Jsoup.parse(result);

        SoftAssertions softly = new SoftAssertions();

        softly.assertThat(document.title())
            .as(TITLE_MESSAGE)
            .isEqualTo("Bristol and Cardiff Administrative Court Daily Cause List");

        softly.assertThat(document.getElementById(HEADER_ELEMENT).text())
            .as(HEADER_MESSAGE)
            .isEqualTo("Bristol and Cardiff Administrative Court Daily Cause List");

        softly.assertThat(document.getElementById(LIST_DATE_ELEMENT).text())
            .as(LIST_DATE_MESSAGE)
            .isEqualTo("List for 23 April 2025");

        softly.assertThat(document.getElementById(LAST_UPDATED_DATE_ELEMENT).text())
            .as(LAST_UPDATED_DATE_MESSAGE)
            .isEqualTo("Last updated 22 April 2025 at 10:30am");

        softly.assertThat(document.getElementsByClass(SUMMARY_TITLE_CLASS).get(0).text())
            .as(IMPORTANT_INFORMATION_MESSAGE)
            .isEqualTo("Important information");

        softly.assertThat(document.getElementById(IMPORTANT_INFORMATION_ELEMENT_1).text())
            .as(IMPORTANT_INFORMATION_MESSAGE)
            .contains("Hearings take place in public unless otherwise indicated. "
                          + "When considering the use of telephone and video technology the "
                          + "judiciary will have regard to the principles of open justice. "
                          + "The court may exclude observers where necessary to secure the proper "
                          + "administration of justice.");

        softly.assertThat(document.getElementById(IMPORTANT_INFORMATION_HEADING_2).text())
            .as(BODY_MESSAGE)
            .isEqualTo("Judgments");

        softly.assertThat(document.getElementById(IMPORTANT_INFORMATION_ELEMENT_2).text())
            .as(IMPORTANT_INFORMATION_MESSAGE)
            .contains("Judgments handed down by the judge remotely will be released by "
                          + "circulation to the parties’ representatives by email and release "
                          + "to the National Archives. The date and time for hand-down will be deemed "
                          + "to be not before time listed. A copy of the judgment in final form as "
                          + "handed down can be made available after that time, on request by email.");

        softly.assertThat(document.getElementsByTag("th"))
            .as(TABLE_HEADERS_MESSAGE)
            .hasSize(7)
            .extracting(Element::text)
            .containsExactly(
                "Venue",
                "Judge",
                "Time",
                "Case number",
                "Case details",
                "Hearing type",
                "Additional information"
            );

        softly.assertAll();
    }

    @Test
    void testBristolAndCardiffAdministrativeCourtDailyCauseListFileConversionInWelsh() throws IOException {
        Map<String, Object> languageResource;
        try (InputStream languageFile = Thread.currentThread()
            .getContextClassLoader()
            .getResourceAsStream("templates/languages/cy/non-strategic/"
                                     + "bristolAndCardiffAdministrativeCourtDailyCauseList.json")) {
            languageResource = new ObjectMapper().readValue(
                Objects.requireNonNull(languageFile).readAllBytes(), new TypeReference<>() {
                });
        }

        Map<String, String> metadata = Map.of(CONTENT_DATE_METADATA, CONTENT_DATE,
                                              PROVENANCE_METADATA, PROVENANCE,
                                              LANGUAGE_METADATA, WELSH, LIST_TYPE_METADATA,
                                              BRISTOL_AND_CARDIFF_ADMINISTRATIVE_COURT_DAILY_CAUSE_LIST.name(),
                                              LAST_RECEIVED_DATE_METADATA, LAST_RECEIVED_DATE
        );

        String result = converter.convert(inputJson, metadata, languageResource);
        Document document = Jsoup.parse(result);

        SoftAssertions softly = new SoftAssertions();

        softly.assertThat(document.title())
            .as(TITLE_MESSAGE)
            .isEqualTo("Rhestr Achosion Dyddiol Llys Gweinyddol Bryste a Chaerdydd");

        softly.assertThat(document.getElementById(HEADER_ELEMENT).text())
            .as(HEADER_MESSAGE)
            .isEqualTo("Rhestr Achosion Dyddiol Llys Gweinyddol Bryste a Chaerdydd");

        softly.assertThat(document.getElementById(LIST_DATE_ELEMENT).text())
            .as(LIST_DATE_MESSAGE)
            .isEqualTo("Rhestr ar gyfer 23 April 2025");

        softly.assertThat(document.getElementById(LAST_UPDATED_DATE_ELEMENT).text())
            .as(LAST_UPDATED_DATE_MESSAGE)
            .isEqualTo("Diweddarwyd ddiwethaf 22 April 2025 am 10:30am");

        softly.assertThat(document.getElementsByClass(SUMMARY_TITLE_CLASS).get(0).text())
            .as(IMPORTANT_INFORMATION_MESSAGE)
            .isEqualTo("Gwybodaeth bwysig");


        softly.assertThat(document.getElementById(IMPORTANT_INFORMATION_ELEMENT_1).text())
            .as(IMPORTANT_INFORMATION_MESSAGE)
            .contains("Mae gwrandawiadau'n cael eu cynnal yn gyhoeddus oni nodir yn wahanol. "
                          + "Wrth ystyried y defnydd o dechnoleg ffôn a fideo, bydd y farnwriaeth "
                          + "yn ystyried egwyddorion cyfiawnder agored. Gall y llys eithrio arsylwyr "
                          + "lle bo angen sicrhau gweinyddiaeth briodol cyfiawnder.");

        softly.assertThat(document.getElementById(IMPORTANT_INFORMATION_HEADING_2).text())
            .as(BODY_MESSAGE)
            .isEqualTo("Dyfarniadau");

        softly.assertThat(document.getElementById(IMPORTANT_INFORMATION_ELEMENT_2).text())
            .as(IMPORTANT_INFORMATION_MESSAGE)
            .contains("Bydd dyfarniadau a draddodir gan y barnwr o bell yn cael eu "
                          + "rhyddhau trwy gylchrediad i gynrychiolwyr y partïon trwy e-bost a'u "
                          + "rhyddhau i'r Archifau Cenedlaethol. Ystyrir na fydd y dyddiad a'r amser ar "
                          + "gyfer traddodi yn digwydd cyn yr amser a restrwyd. Gellir darparu copi o'r "
                          + "dyfarniad ar ffurf derfynol fel y'i traddodwyd ar ôl yr amser hwnnw, ar "
                          + "gais drwy e-bost.");

        softly.assertThat(document.getElementsByTag("th"))
            .as(TABLE_HEADERS_MESSAGE)
            .hasSize(7)
            .extracting(Element::text)
            .containsExactly(
                "Lleoliad",
                "Barnwyr",
                "Amser",
                "Rhif yr achos",
                "Manylion yr achos",
                "Math o wrandawiad",
                "Gwybodaeth ychwanegol"
            );

        softly.assertAll();
    }
}
