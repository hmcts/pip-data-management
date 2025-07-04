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

import static uk.gov.hmcts.reform.pip.model.publication.ListType.SEND_DAILY_HEARING_LIST;

@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class SendDailyHearingListTest {
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
    private static final String SUMMARY_TEXT_CLASS = "govuk-details__text";
    private static final String TABLE_CELL_CLASS = "govuk-table__cell";

    private static final String TITLE_MESSAGE = "Title does not match";
    private static final String HEADER_MESSAGE = "Header does not match";
    private static final String LIST_DATE_MESSAGE = "List date does not match";
    private static final String LAST_UPDATED_DATE_MESSAGE = "Last updated date does not match";
    private static final String IMPORTANT_INFORMATION_MESSAGE = "Important information heading does not match";
    private static final String TABLE_HEADERS_MESSAGE = "Table headers does not match";
    private static final String TABLE_CELL_MESSAGE = "Table cell does not match";


    private final NonStrategicListFileConverter converter = new NonStrategicListFileConverter();

    private JsonNode cstInputJson;

    @BeforeAll
    void setup() throws IOException {
        try (InputStream inputStream = getClass()
            .getResourceAsStream("/mocks/non-strategic/sendDailyHearingList.json")) {
            String inputRaw = IOUtils.toString(inputStream, Charset.defaultCharset());
            cstInputJson = new ObjectMapper().readTree(inputRaw);
        }
    }

    @Test
    void testSendDailyHearingListFileConversionInEnglish() throws IOException {
        Map<String, Object> languageResource;
        try (InputStream languageFile = Thread.currentThread()
            .getContextClassLoader()
            .getResourceAsStream("templates/languages/en/non-strategic/sendDailyHearingList.json")) {
            languageResource = new ObjectMapper().readValue(
                Objects.requireNonNull(languageFile).readAllBytes(), new TypeReference<>() {
                });
        }

        Map<String, String> metadata = Map.of(CONTENT_DATE_METADATA, CONTENT_DATE,
                                              PROVENANCE_METADATA, PROVENANCE,
                                              LANGUAGE_METADATA, ENGLISH,
                                              LIST_TYPE_METADATA, SEND_DAILY_HEARING_LIST.name(),
                                              LAST_RECEIVED_DATE_METADATA, LAST_RECEIVED_DATE
        );

        String result = converter.convert(cstInputJson, metadata, languageResource);
        Document document = Jsoup.parse(result);

        SoftAssertions softly = new SoftAssertions();

        softly.assertThat(document.title())
                .as(TITLE_MESSAGE)
            .isEqualTo("First-tier Tribunal (Special Educational Needs and Disability) Daily Hearing List");

        softly.assertThat(document.getElementById(HEADER_ELEMENT).text())
            .as(HEADER_MESSAGE)
            .isEqualTo("First-tier Tribunal (Special Educational Needs and Disability) Daily Hearing List");


        softly.assertThat(document.getElementById(LIST_DATE_ELEMENT).text())
            .as(LIST_DATE_MESSAGE)
            .isEqualTo("List for 23 April 2025");

        softly.assertThat(document.getElementById(LAST_UPDATED_DATE_ELEMENT).text())
            .as(LAST_UPDATED_DATE_MESSAGE)
            .isEqualTo("Last updated 22 April 2025 at 10:30am");

        softly.assertThat(document.getElementsByClass(SUMMARY_TITLE_CLASS).get(0).text())
            .as(IMPORTANT_INFORMATION_MESSAGE)
            .isEqualTo("Important information");

        softly.assertThat(document.getElementsByClass(SUMMARY_TEXT_CLASS).get(0).text())
            .as(IMPORTANT_INFORMATION_MESSAGE)
            .contains(
                "Special Educational Needs and Disability (SEND) Tribunal hearings are held in private and "
                    + "unless a request from the parties for the hearing to be heard in public has been "
                    + "approved, you will not be able to observe.",
                "Private hearings do not allow anyone to observe remotely or in person. "
                    + "This includes members of the press.",
                "Open justice is a fundamental principle of our justice system. To attend a public hearing using a "
                    + "remote link you must apply for permission to observe.",
                "Requests to observe a public hearing that is taking place should be made in good time "
                    + "direct to: send@justice.gov.uk. You may be asked to provide further details.",
                "The judge hearing the case will decide if it is appropriate for you to observe remotely. "
                    + "They will have regard to the interests of justice, the technical capacity for remote "
                    + "observation and what is necessary to secure the proper administration of justice.");

        softly.assertThat(document.getElementsByTag("th"))
            .as(TABLE_HEADERS_MESSAGE)
            .hasSize(6)
            .extracting(Element::text)
            .containsExactly(
                "Time",
                "Case reference number",
                "Respondent",
                "Hearing type",
                "Venue",
                "Time estimate"

            );

        softly.assertThat(document.getElementsByClass(TABLE_CELL_CLASS).get(0).text())
            .as(TABLE_CELL_MESSAGE)
            .contains("10am");

        softly.assertThat(document.getElementsByClass(TABLE_CELL_CLASS).get(1).text())
            .as(TABLE_CELL_MESSAGE)
            .contains("1234");

        softly.assertThat(document.getElementsByClass(TABLE_CELL_CLASS).get(2).text())
            .as(TABLE_CELL_MESSAGE)
            .contains("Respondent A");

        softly.assertThat(document.getElementsByClass(TABLE_CELL_CLASS).get(3).text())
            .as(TABLE_CELL_MESSAGE)
            .contains("Hearing Type A");

        softly.assertThat(document.getElementsByClass(TABLE_CELL_CLASS).get(4).text())
            .as(TABLE_CELL_MESSAGE)
            .contains("Venue A");

        softly.assertThat(document.getElementsByClass(TABLE_CELL_CLASS).get(5).text())
            .as(TABLE_CELL_MESSAGE)
            .contains("2hrs");

        softly.assertAll();
    }

    @Test
    void testSendDailyHearingListFileConversionInWelsh() throws IOException {
        Map<String, Object> languageResource;
        try (InputStream languageFile = Thread.currentThread()
            .getContextClassLoader()
            .getResourceAsStream("templates/languages/cy/non-strategic/sendDailyHearingList.json")) {
            languageResource = new ObjectMapper().readValue(
                Objects.requireNonNull(languageFile).readAllBytes(), new TypeReference<>() {
                });
        }

        Map<String, String> metadata = Map.of(CONTENT_DATE_METADATA, CONTENT_DATE,
                                              PROVENANCE_METADATA, PROVENANCE,
                                              LANGUAGE_METADATA, WELSH,
                                              LIST_TYPE_METADATA, SEND_DAILY_HEARING_LIST.name(),
                                              LAST_RECEIVED_DATE_METADATA, LAST_RECEIVED_DATE
        );

        String result = converter.convert(cstInputJson, metadata, languageResource);
        Document document = Jsoup.parse(result);

        SoftAssertions softly = new SoftAssertions();

        softly.assertThat(document.title())
            .as(TITLE_MESSAGE)
            .isEqualTo("Rhestr o Wrandawiadau Dyddiol y Tribiwnlys Haen Gyntaf "
                           + "(Anghenion Addysgol Arbennig ac Anabledd)");

        softly.assertThat(document.getElementById(HEADER_ELEMENT).text())
            .as(HEADER_MESSAGE)
            .isEqualTo("Rhestr o Wrandawiadau Dyddiol y Tribiwnlys Haen Gyntaf "
                           + "(Anghenion Addysgol Arbennig ac Anabledd)");

        softly.assertThat(document.getElementById(LIST_DATE_ELEMENT).text())
            .as(LIST_DATE_MESSAGE)
            .isEqualTo("Rhestr ar gyfer 23 April 2025");

        softly.assertThat(document.getElementById(LAST_UPDATED_DATE_ELEMENT).text())
            .as(LAST_UPDATED_DATE_MESSAGE)
            .isEqualTo("Diweddarwyd ddiwethaf 22 April 2025 am 10:30am");

        softly.assertThat(document.getElementsByClass(SUMMARY_TITLE_CLASS).get(0).text())
            .as(IMPORTANT_INFORMATION_MESSAGE)
            .isEqualTo("Gwybodaeth bwysig");

        softly.assertThat(document.getElementsByClass(SUMMARY_TEXT_CLASS).get(0).text())
            .as(IMPORTANT_INFORMATION_MESSAGE)
            .contains("Cynhelir gwrandawiadau Tribiwnlys Anghenion Addysgol Arbennig ac Anabledd (SEND) yn breifat "
                          + "ac oni bai bod cais gan y partïon i wrandawiad gael ei wrando yn "
                          + "gyhoeddus wedi'i gymeradwyo, ni fyddwch yn gallu arsylwi.",
                      "Nid yw gwrandawiadau preifat yn caniatáu i unrhyw un arsylwi o "
                          + "bell neu wyneb yn wyneb. Mae hyn yn cynnwys aelodau o'r wasg.",
                      "Mae cyfiawnder agored yn un o egwyddorion sylfaenol ein system gyfiawnder",
                      "Ar gyfer mynychu gwrandawiad cyhoeddus gan ddefnyddio "
                          + "cyswllt o bell rhaid i chi wneud cais am ganiatâd i arsylwi.",
                      "Dylid gwneud ceisiadau i arsylwi gwrandawiad cyhoeddus sy'n cael ei "
                          + "gynnal mewn pryd yn uniongyrchol at: send@justice.gov.uk. "
                          + "Efallai y gofynnir i chi ddarparu rhagor o fanylion.",
                      "Bydd y barnwr sy'n gwrando’r achos yn penderfynu a yw'n briodol i chi arsylwi o bell. "
                          + "Byddant yn ystyried buddiannau cyfiawnder, y gallu technegol i arsylwi o bell a'r "
                          + "hyn sy'n angenrheidiol i sicrhau gweinyddiaeth briodol cyfiawnder.");

        softly.assertThat(document.getElementsByTag("th"))
            .as(TABLE_HEADERS_MESSAGE)
            .hasSize(6)
            .extracting(Element::text)
            .containsExactly(
                "Amser",
                "Cyfeirnod yr Achos",
                "Atebydd",
                "Math o wrandawiad",
                "Lleoliad",
                "Amcangyfrif o'r amser"
            );

        softly.assertAll();
    }
}
