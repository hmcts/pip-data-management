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

import static uk.gov.hmcts.reform.pip.model.publication.ListType.UT_AAC_DAILY_HEARING_LIST;

@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class UtAdministrativeAppealsChamberDailyHearingListFileConverterTest {

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

    private static final String LIST_NAME = UT_AAC_DAILY_HEARING_LIST.name();
    private static final String LIST_ENGLISH_NAME = "Upper Tribunal (Administrative Appeals Chamber) "
        + "Daily Hearing List";
    private static final String LIST_DATE_ENGLISH = "List for 12 December 2024";
    private static final String LIST_DATE_WELSH = "Rhestr ar gyfer 12 December 2024";
    private static final String LIST_WELSH_NAME = "Rhestr o Wrandawiadau Dyddiol "
        + "Uwch Dribiwnlys (Siambr Apeliadau Gweinyddol)";

    private static final String HEADER_ELEMENT = "page-heading";
    private static final String LIST_DATE_ELEMENT = "list-date";
    private static final String LAST_UPDATED_DATE_ELEMENT = "last-updated-date";
    private static final String MESSAGE_ELEMENT_1 = "message-1";
    private static final String MESSAGE_ELEMENT_2 = "message-2";
    private static final String MESSAGE_ELEMENT_3 = "message-3";
    private static final String MESSAGE_ELEMENT_4 = "message-4";
    private static final String MESSAGE_ELEMENT_5 = "message-5";
    private static final String MESSAGE_ELEMENT_6 = "message-6";
    private static final String MESSAGE_ELEMENT_7 = "message-7";
    private static final String MESSAGE_ELEMENT_8 = "message-8";
    private static final String MESSAGE_ELEMENT_9 = "message-9";
    private static final String MESSAGE_ELEMENT_10 = "message-10";
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
            .getResourceAsStream("/mocks/non-strategic/utAdministrativeAppealsChamberDailyHearingList.json")) {
            String inputRaw = IOUtils.toString(inputStream, Charset.defaultCharset());
            listInputJson = new ObjectMapper().readTree(inputRaw);
        }
    }

    @Test
    void testAdministrativeAppealsChamberDailyHearingListFileConversionInEnglish() throws IOException {
        Map<String, Object> languageResource;
        try (InputStream languageFile = Thread.currentThread()
            .getContextClassLoader()
            .getResourceAsStream("templates/languages/en/non-strategic/"
                                     + "utAacDailyHearingList.json")) {
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

        softly.assertThat(document.getElementById(MESSAGE_ELEMENT_1).text())
            .as(BODY_MESSAGE)
            .isEqualTo("Details");

        softly.assertThat(document.getElementById(MESSAGE_ELEMENT_2).text())
            .as(BODY_MESSAGE)
            .isEqualTo("Lists are subject to change until 4:30pm. Any alterations after "
                           + "this time will be telephoned or emailed direct to the parties or "
                           + "their legal representatives.");

        softly.assertThat(document.getElementById(MESSAGE_ELEMENT_3).text())
            .as(BODY_MESSAGE)
            .isEqualTo("England and Wales");

        softly.assertThat(document.getElementById(MESSAGE_ELEMENT_4).text())
            .as(BODY_MESSAGE)
            .isEqualTo("Remote hearings via CVP");

        softly.assertThat(document.getElementById(MESSAGE_ELEMENT_5).text())
            .as(BODY_MESSAGE)
            .isEqualTo("Hearings will be available to representatives of the media "
                           + "or any other member of the public, on their request, and therefore "
                           + "will be a hearing conducted in public in accordance with Rule 37 of "
                           + "the Tribunal Procedure (Upper Tribunal) Rules 2008.");

        softly.assertThat(document.getElementById(MESSAGE_ELEMENT_6).text())
            .as(BODY_MESSAGE)
            .isEqualTo("Any media representative or any other member of the public "
                           + "wishing to witness the hearing will need to do so over the "
                           + "internet and provide an email address at which to be sent an "
                           + "appropriate link for access.");

        softly.assertThat(document.getElementById(MESSAGE_ELEMENT_7).text())
            .as(BODY_MESSAGE)
            .isEqualTo("Please contact adminappeals@justice.gov.uk.");

        softly.assertThat(document.getElementById(MESSAGE_ELEMENT_8).text())
            .as(BODY_MESSAGE)
            .isEqualTo("Scotland");

        softly.assertThat(document.getElementById(MESSAGE_ELEMENT_9).text())
            .as(BODY_MESSAGE)
            .isEqualTo("Remote hearings");

        softly.assertThat(document.getElementById(MESSAGE_ELEMENT_10).text())
            .as(BODY_MESSAGE)
            .isEqualTo("When hearings are listed for Scotland the hearing will "
                           + "be available to representatives of the media or any other "
                           + "member of the public, on their request, and therefore will be a "
                           + "hearing conducted in public in accordance with Rule 37 of the Tribunal "
                           + "Procedure (Upper Tribunal) Rules 2008. It will be organised and conducted "
                           + "using Cloud Video Platform (CVP). Any media representative or any other "
                           + "member of the public wishing to witness the hearing will need to do so "
                           + "over the internet and provide an email address at which to be sent an appropriate "
                           + "link for access. Please contact UTAACMailbox@justice.gov.uk.");

        softly.assertThat(document.getElementsByTag("th"))
            .as(TABLE_HEADERS_MESSAGE)
            .hasSize(8)
            .extracting(Element::text)
            .containsExactly(
                "Time",
                "Appellant",
                "Case reference number",
                "Judge(s)",
                "Member(s)",
                "Mode of hearing",
                "Venue",
                "Additional information"
            );

        softly.assertThat(document.getElementsByTag("td"))
            .as(TABLE_CONTENT_MESSAGE)
            .hasSize(8)
            .extracting(Element::text)
            .containsExactly(
                "10:15am",
                "Appellant 1",
                "12345",
                "Judge A",
                "Member A",
                "Mode of hearing 1",
                "Venue 1",
                "Additional information 1"
            );

        softly.assertAll();
    }

    @Test
    void testAdministrativeAppealsChamberDailyHearingListFileConversionInWelsh() throws IOException {
        Map<String, Object> languageResource;
        try (InputStream languageFile = Thread.currentThread()
            .getContextClassLoader()
            .getResourceAsStream("templates/languages/cy/non-strategic/utAacDailyHearingList.json")) {
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

        softly.assertThat(document.getElementById(MESSAGE_ELEMENT_1).text())
            .as(BODY_MESSAGE)
            .isEqualTo("Manylion");

        softly.assertThat(document.getElementById(MESSAGE_ELEMENT_2).text())
            .as(BODY_MESSAGE)
            .isEqualTo("Mae rhestrau yn destun newid hyd at 4:30pm. Bydd unrhyw newidiadau "
                           + "a wneir ar ôl yr amser hwn yn cael eu rhannu’n uniongyrchol drwy alwad ffôn "
                           + "neu e-bost at y partïon neu eu cynrychiolwyr cyfreithiol.");

        softly.assertThat(document.getElementById(MESSAGE_ELEMENT_3).text())
            .as(BODY_MESSAGE)
            .isEqualTo("Cymru a Lloegr");

        softly.assertThat(document.getElementById(MESSAGE_ELEMENT_4).text())
            .as(BODY_MESSAGE)
            .isEqualTo("Gwrandawiadau o bell trwy CVP");

        softly.assertThat(document.getElementById(MESSAGE_ELEMENT_5).text())
            .as(BODY_MESSAGE)
            .isEqualTo("Bydd gwrandawiadau ar gael i gynrychiolwyr y cyfryngau neu "
                           + "unrhyw aelod arall o’r cyhoedd, ar eu cais, ac felly byddant "
                           + "yn wrandawiad a gynhelir yn gyhoeddus yn unol â Rheol 37 o "
                           + "Reolau Trefniadaeth y Tribiwnlysoedd (Uwch Dribiwnlys) 2008.");

        softly.assertThat(document.getElementById(MESSAGE_ELEMENT_6).text())
            .as(BODY_MESSAGE)
            .isEqualTo("Bydd angen i unrhyw gynrychiolydd o’r cyfryngau neu unrhyw aelod arall "
                           + "o’r cyhoedd sy’n dymuno gwylio’r gwrandawiad wneud hynny dros y rhyngrwyd a "
                           + "darparu cyfeiriad e-bost er mwyn anfon dolen briodol i gael mynediad.");

        softly.assertThat(document.getElementById(MESSAGE_ELEMENT_7).text())
            .as(BODY_MESSAGE)
            .isEqualTo("Cysylltwch â adminappeals@justice.gov.uk");

        softly.assertThat(document.getElementById(MESSAGE_ELEMENT_8).text())
            .as(BODY_MESSAGE)
            .isEqualTo("Yr Alban");

        softly.assertThat(document.getElementById(MESSAGE_ELEMENT_9).text())
            .as(BODY_MESSAGE)
            .isEqualTo("Gwrandawiadau o bell");

        softly.assertThat(document.getElementById(MESSAGE_ELEMENT_10).text())
            .as(BODY_MESSAGE)
            .isEqualTo("Pan gaiff gwrandawiadau eu rhestru ar gyfer yr Alban, bydd y "
                           + "gwrandawiad ar gael i gynrychiolwyr y cyfryngau neu unrhyw aelod "
                           + "arall o’r cyhoedd, ar eu cais, ac felly byddant yn wrandawiad "
                           + "a gynhelir yn gyhoeddus yn unol â Rheol 37 o Reolau Trefniadaeth "
                           + "y Tribiwnlysoedd (Uwch Dribiwnlys) 2008. Bydd yn cael ei drefnu "
                           + "a’i gynnal gan ddefnyddio Platfform Fideo’r Cwmwl (CVP). Bydd angen "
                           + "i unrhyw gynrychiolydd o’r cyfryngau neu unrhyw aelod arall o’r "
                           + "cyhoedd sy’n dymuno gwylio’r gwrandawiad wneud hynny dros y rhyngrwyd "
                           + "a darparu cyfeiriad e-bost er mwyn anfon dolen briodol i gael mynediad. "
                           + "Cysylltwch â UTAACMailbox@justice.gov.uk");

        softly.assertThat(document.getElementsByTag("th"))
            .as(TABLE_HEADERS_MESSAGE)
            .hasSize(8)
            .extracting(Element::text)
            .containsExactly(
                "Amser",
                "Apelydd",
                "Cyfeirnod yr achos",
                "Barnwr/Barnwyr",
                "Aelod(au)",
                "Math o Wrandawiad",
                "Lleoliad",
                "Gwybodaeth ychwanegol"
            );

        softly.assertThat(document.getElementsByTag("td"))
            .as(TABLE_CONTENT_MESSAGE)
            .hasSize(8)
            .extracting(Element::text)
            .containsExactly(
                "10:15am",
                "Appellant 1",
                "12345",
                "Judge A",
                "Member A",
                "Mode of hearing 1",
                "Venue 1",
                "Additional information 1"
            );

        softly.assertAll();
    }
}
