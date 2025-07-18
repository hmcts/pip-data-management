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

import static uk.gov.hmcts.reform.pip.model.publication.ListType.FTT_TAX_WEEKLY_HEARING_LIST;

@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SuppressWarnings("java:S5961")
class FttTaxWeeklyHearingListConverterTest {

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

    private static final String LIST_NAME = FTT_TAX_WEEKLY_HEARING_LIST.name();
    private static final String LIST_ENGLISH_NAME = "First-tier Tribunal (Tax Chamber)"
        + " Weekly Hearing List";
    private static final String LIST_DATE_ENGLISH = "List for week commencing 12 December 2024";
    private static final String LIST_DATE_WELSH = "Rhestr ar gyfer yr wythnos yn dechrau ar 12 December 2024";
    private static final String OBSERVE_HEARING_ENGLISH = "Observe a court or tribunal hearing as a journalist, "
        + "researcher or member of the public";
    private static final String OBSERVE_HEARING_WELSH = "Arsylwi gwrandawiad llys neu dribiwnlys fel newyddiadurwr, "
        + "ymchwilydd neu aelod o'r cyhoedd";
    private static final String LIST_WELSH_NAME = "Rhestr o Wrandawiadau "
        + "Wythnosol Tribiwnlys Haen Gyntaf (Siambr Treth)";

    private static final String HEARING_DATE = "16 December 2024";
    private static final String HEARING_VENUE = "This is a venue name";

    private static final String HEADER_ELEMENT = "page-heading";
    private static final String LIST_DATE_ELEMENT = "list-date";
    private static final String LAST_UPDATED_DATE_ELEMENT = "last-updated-date";
    private static final String CONTACT_MESSAGE_ELEMENT_1 = "contact-message-1";
    private static final String CONTACT_MESSAGE_ELEMENT_2 = "contact-message-2";
    private static final String CONTACT_MESSAGE_ELEMENT_3 = "contact-message-3";
    private static final String CONTACT_MESSAGE_ELEMENT_4 = "contact-message-4";
    private static final String OBSERVE_HEARING_ELEMENT =  "observe-hearing";
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

    private JsonNode listInputJson;

    @BeforeAll
    void setup() throws IOException {
        try (InputStream inputStream = getClass()
            .getResourceAsStream("/mocks/non-strategic/fttTaxWeeklyHearingList.json")) {
            String inputRaw = IOUtils.toString(inputStream, Charset.defaultCharset());
            listInputJson = new ObjectMapper().readTree(inputRaw);
        }
    }

    @Test
    void testTaxWeeklyHearingListFileConversionInEnglish() throws IOException {
        Map<String, Object> languageResource;
        try (InputStream languageFile = Thread.currentThread()
            .getContextClassLoader()
            .getResourceAsStream("templates/languages/en/non-strategic/fttTaxWeeklyHearingList.json")) {
            languageResource = new ObjectMapper().readValue(
                Objects.requireNonNull(languageFile).readAllBytes(), new TypeReference<>() {
                });
        }

        Map<String, String> metadata = Map.of(CONTENT_DATE_METADATA, CONTENT_DATE,
                                              PROVENANCE_METADATA, PROVENANCE,
                                              LANGUAGE_METADATA, ENGLISH,
                                              LIST_TYPE_METADATA, LIST_NAME,
                                              LAST_RECEIVED_DATE_METADATA, LAST_RECEIVED_DATE
        );

        String result = converter.convert(listInputJson, metadata, languageResource);
        Document document = Jsoup.parse(result);

        SoftAssertions softly = new SoftAssertions();

        softly.assertThat(document.title())
            .as(TITLE_MESSAGE)
            .isEqualTo(LIST_ENGLISH_NAME);

        softly.assertThat(document.getElementById(HEADER_ELEMENT).text())
            .as(HEADER_MESSAGE)
            .isEqualTo(LIST_ENGLISH_NAME);

        softly.assertThat(document.getElementById(LIST_DATE_ELEMENT).text())
            .as(LIST_DATE_MESSAGE)
            .isEqualTo(LIST_DATE_ENGLISH);

        softly.assertThat(document.getElementById(LAST_UPDATED_DATE_ELEMENT).text())
            .as(LAST_UPDATED_DATE_MESSAGE)
            .isEqualTo("Last updated 20 January 2025 at 9:30am");

        softly.assertThat(document.getElementsByClass(SUMMARY_TEXT_CLASS).getFirst().text())
            .as(IMPORTANT_INFORMATION_MESSAGE)
            .isEqualTo("Important information");

        softly.assertThat(document.getElementById(CONTACT_MESSAGE_ELEMENT_1).text())
            .as(BODY_MESSAGE)
            .isEqualTo("Open justice is a fundamental principle of our justice system. You can attend a "
                           + "public hearing in person, or you can apply for permission to observe remotely.");
        softly.assertThat(document.getElementById(CONTACT_MESSAGE_ELEMENT_2).text())
            .as(BODY_MESSAGE)
            .isEqualTo("Members of the public and the media can ask to join any telephone or "
                           + "video hearing remotely. Contact the Tribunal before the hearing to ask for "
                           + "permission to attend by emailing taxappeals@justice.gov.uk.");

        softly.assertThat(document.getElementById(CONTACT_MESSAGE_ELEMENT_3).text())
            .as(BODY_MESSAGE)
            .isEqualTo("The subject line for the email should contain the following wording: "
                           + "“HEARING ACCESS REQUEST – [Appellant’s name] v [Respondent’s name, for example HMRC] – "
                           + "[case reference] – [hearing date]”. You will be sent instructions on "
                           + "how to join the hearing.");

        softly.assertThat(document.getElementById(CONTACT_MESSAGE_ELEMENT_4).text())
            .as(BODY_MESSAGE)
            .isEqualTo("The judge may refuse a request and can also decide a "
                           + "hearing must be held in private, in such cases you will not be able to attend.");

        softly.assertThat(document.getElementById(OBSERVE_HEARING_ELEMENT).text())
            .as(BODY_MESSAGE)
            .isEqualTo(OBSERVE_HEARING_ENGLISH);

        softly.assertThat(document.getElementsByTag("th"))
            .as(TABLE_HEADERS_MESSAGE)
            .hasSize(7)
            .extracting(Element::text)
            .containsExactly(
                "Date",
                "Hearing time",
                "Case name",
                "Case reference number",
                "Judge(s)",
                "Member(s)",
                "Venue/Platform"
            );

        softly.assertThat(document.getElementsByTag("td"))
            .as(TABLE_CONTENT_MESSAGE)
            .hasSize(14)
            .extracting(Element::text)
            .containsExactly(
                HEARING_DATE,
                "10:15am",
                "This is a case name",
                "1234",
                "Judge A",
                "Member A",
                HEARING_VENUE,
                HEARING_DATE,
                "10:30am",
                "This is another case name",
                "1235",
                "Judge B",
                "Member B",
                HEARING_VENUE
            );

        softly.assertAll();
    }

    @Test
    void testTaxWeeklyHearingListFileConversionInWelsh() throws IOException {
        Map<String, Object> languageResource;
        try (InputStream languageFile = Thread.currentThread()
            .getContextClassLoader()
            .getResourceAsStream("templates/languages/cy/non-strategic/fttTaxWeeklyHearingList.json")) {
            languageResource = new ObjectMapper().readValue(
                Objects.requireNonNull(languageFile).readAllBytes(), new TypeReference<>() {
                });
        }

        Map<String, String> metadata = Map.of(CONTENT_DATE_METADATA, CONTENT_DATE,
                                              PROVENANCE_METADATA, PROVENANCE,
                                              LANGUAGE_METADATA, WELSH,
                                              LIST_TYPE_METADATA, LIST_NAME,
                                              LAST_RECEIVED_DATE_METADATA, LAST_RECEIVED_DATE
        );

        String result = converter.convert(listInputJson, metadata, languageResource);
        Document document = Jsoup.parse(result);

        SoftAssertions softly = new SoftAssertions();

        softly.assertThat(document.title())
            .as(TITLE_MESSAGE)
            .isEqualTo(LIST_WELSH_NAME);

        softly.assertThat(document.getElementById(HEADER_ELEMENT).text())
            .as(HEADER_MESSAGE)
            .isEqualTo(LIST_WELSH_NAME);

        softly.assertThat(document.getElementById(LIST_DATE_ELEMENT).text())
            .as(LIST_DATE_MESSAGE)
            .isEqualTo(LIST_DATE_WELSH);

        softly.assertThat(document.getElementById(LAST_UPDATED_DATE_ELEMENT).text())
            .as(LAST_UPDATED_DATE_MESSAGE)
            .isEqualTo("Diweddarwyd ddiwethaf 20 January 2025 am 9:30am");

        softly.assertThat(document.getElementsByClass(SUMMARY_TEXT_CLASS).getFirst().text())
            .as(IMPORTANT_INFORMATION_MESSAGE)
            .isEqualTo("Gwybodaeth bwysig");

        softly.assertThat(document.getElementById(CONTACT_MESSAGE_ELEMENT_1).text())
            .as(BODY_MESSAGE)
            .isEqualTo("Mae cyfiawnder agored yn egwyddor sylfaenol yn ein system gyfiawnder. "
                           + "Gallwch fynychu gwrandawiad cyhoeddus wyneb yn wyneb, neu gallwch "
                           + "wneud cais am ganiatâd i arsylwi o bell.");
        softly.assertThat(document.getElementById(CONTACT_MESSAGE_ELEMENT_2).text())
            .as(BODY_MESSAGE)
            .isEqualTo("Gall aelodau’r cyhoedd a’r cyfryngau ofyn am gael ymuno ag "
                           + "unrhyw wrandawiad dros y ffôn neu drwy fideo o bell. Cysylltwch â’r "
                           + "Tribiwnlys cyn y gwrandawiad i ofyn am ganiatâd i fod yn bresennol "
                           + "drwy anfon e-bost at taxappeals@justice.gov.uk");

        softly.assertThat(document.getElementById(CONTACT_MESSAGE_ELEMENT_3).text())
            .as(BODY_MESSAGE)
            .isEqualTo("Dylai llinell bwnc yr e-bost gynnwys y geiriad canlynol: "
                           + "“CAIS MYNEDIAD I WRANDAWIAD – [Enw’r apelydd] v [Enw’r atebydd, er enghraifft CThEF] – "
                           + "[cyfeirnod yr achos] – [dyddiad y gwrandawiad]”. Anfonir cyfarwyddiadau atoch ar "
                           + "sut i ymuno â’r gwrandawiad.");

        softly.assertThat(document.getElementById(CONTACT_MESSAGE_ELEMENT_4).text())
            .as(BODY_MESSAGE)
            .isEqualTo("Gall y barnwr wrthod cais a gall hefyd benderfynu bod yn rhaid cynnal "
                           + "gwrandawiad yn breifat, ac mewn achosion o'r fath ni fyddwch yn gallu bod yn bresennol.");

        softly.assertThat(document.getElementById(OBSERVE_HEARING_ELEMENT).text())
            .as(BODY_MESSAGE)
            .isEqualTo(OBSERVE_HEARING_WELSH);

        softly.assertThat(document.getElementsByTag("th"))
            .as(TABLE_HEADERS_MESSAGE)
            .hasSize(7)
            .extracting(Element::text)
            .containsExactly(
                "Dyddiad",
                "Amser y gwrandawiad",
                "Enw’r achos",
                "Cyfeirnod yr achos",
                "Barnwr/Barnwyr",
                "Aelod(au)",
                "Lleoliad/Platfform"
            );

        softly.assertThat(document.getElementsByTag("td"))
            .as(TABLE_CONTENT_MESSAGE)
            .hasSize(14)
            .extracting(Element::text)
            .containsExactly(
                HEARING_DATE,
                "10:15am",
                "This is a case name",
                "1234",
                "Judge A",
                "Member A",
                HEARING_VENUE,
                HEARING_DATE,
                "10:30am",
                "This is another case name",
                "1235",
                "Judge B",
                "Member B",
                HEARING_VENUE
            );

        softly.assertAll();
    }
}
