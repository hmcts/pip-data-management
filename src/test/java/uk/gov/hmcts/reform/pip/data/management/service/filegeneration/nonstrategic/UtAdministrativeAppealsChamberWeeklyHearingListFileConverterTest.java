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

import static uk.gov.hmcts.reform.pip.model.publication.ListType.UT_AAC_WEEKLY_HEARING_LIST;

@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class UtAdministrativeAppealsChamberWeeklyHearingListFileConverterTest {

    private static final String CONTENT_DATE = "12 December 2024";
    private static final String PROVENANCE = "provenance";
    private static final String CONTENT_DATE_METADATA = "contentDate";
    private static final String PROVENANCE_METADATA = "provenance";
    private static final String LANGUAGE_METADATA = "language";
    private static final String LIST_TYPE_METADATA = "listType";

    private static final String ENGLISH = "ENGLISH";
    private static final String WELSH = "WELSH";

    private static final String LIST_NAME = UT_AAC_WEEKLY_HEARING_LIST.name();
    private static final String LIST_ENGLISH_NAME = "Upper Tribunal (Administrative Appeals Chamber) "
        + "Daily Hearing List";
    private static final String LIST_DATE_ENGLISH = "List for 12 December 2024";
    private static final String LIST_DATE_WELSH = "Rhestr ar gyfer 12 December 2024";
    private static final String LIST_WELSH_NAME = "Rhestr o Wrandawiadau Dyddiol "
        + "Uwch Dribiwnlys (Siambr Apeliadau Gweinyddol)";

    private static final String HEADER_ELEMENT = "page-heading";
    private static final String LIST_DATE_ELEMENT = "list-date";
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


    private static final String TITLE_MESSAGE = "Title does not match";
    private static final String HEADER_MESSAGE = "Header does not match";
    private static final String LIST_DATE_MESSAGE = "List date does not match";
    private static final String BODY_MESSAGE = "Body does not match";
    private static final String TABLE_HEADERS_MESSAGE = "Table headers does not match";

    private final NonStrategicListFileConverter converter = new NonStrategicListFileConverter();

    private JsonNode listInputJson;

    @BeforeAll
    void setup() throws IOException {
        try (InputStream inputStream = getClass()
            .getResourceAsStream("/mocks/non-strategic/utAdministrativeAppealsChamberWeeklyHearingList.json")) {
            String inputRaw = IOUtils.toString(inputStream, Charset.defaultCharset());
            listInputJson = new ObjectMapper().readTree(inputRaw);
        }
    }

    @Test
    void testAdministrativeAppealsChamberWeeklyHearingListFileConversionInEnglish() throws IOException {
        Map<String, Object> languageResource;
        try (InputStream languageFile = Thread.currentThread()
            .getContextClassLoader()
            .getResourceAsStream("templates/languages/en/non-strategic/"
                                     + "utAacWeeklyHearingList.json")) {
            languageResource = new ObjectMapper().readValue(
                Objects.requireNonNull(languageFile).readAllBytes(), new TypeReference<>() {
                });
        }

        Map<String, String> metadata = Map.of(CONTENT_DATE_METADATA, CONTENT_DATE,
                                              PROVENANCE_METADATA, PROVENANCE,
                                              LANGUAGE_METADATA, ENGLISH,
                                              LIST_TYPE_METADATA, LIST_NAME
        );

        String result = converter.convert(listInputJson, metadata, languageResource);
        Document document = Jsoup.parse(result);

        SoftAssertions softly = new SoftAssertions();

        softly.assertThat(document.title())
            .as(TITLE_MESSAGE)
            .isEqualTo(LIST_ENGLISH_NAME);

        softly.assertThat(document.getElementById(HEADER_ELEMENT))
            .as(HEADER_MESSAGE)
            .extracting(Element::text)
            .isEqualTo(LIST_ENGLISH_NAME);

        softly.assertThat(document.getElementById(LIST_DATE_ELEMENT))
            .as(LIST_DATE_MESSAGE)
            .extracting(Element::text)
            .isEqualTo(LIST_DATE_ENGLISH);

        softly.assertThat(document.getElementById(MESSAGE_ELEMENT_1))
            .as(BODY_MESSAGE)
            .extracting(Element::text)
            .isEqualTo("Details");

        softly.assertThat(document.getElementById(MESSAGE_ELEMENT_2))
            .as(BODY_MESSAGE)
            .extracting(Element::text)
            .isEqualTo("Lists are subject to change until 4:30pm. Any alterations after "
                           + "this time will be telephoned or emailed direct to the parties or "
                           + "their legal representatives.");

        softly.assertThat(document.getElementById(MESSAGE_ELEMENT_3))
            .as(BODY_MESSAGE)
            .extracting(Element::text)
            .isEqualTo("England and Wales");

        softly.assertThat(document.getElementById(MESSAGE_ELEMENT_4))
            .as(BODY_MESSAGE)
            .extracting(Element::text)
            .isEqualTo("Remote hearings via CVP and BT Meet Me");

        softly.assertThat(document.getElementById(MESSAGE_ELEMENT_5))
            .as(BODY_MESSAGE)
            .extracting(Element::text)
            .isEqualTo("Hearings will be available to representatives of the media "
                           + "or any other member of the public, on their request, and therefore "
                           + "will be a hearing conducted in public in accordance with Rule 37 of "
                           + "the Tribunal Procedure (Upper Tribunal) Rules 2008.");

        softly.assertThat(document.getElementById(MESSAGE_ELEMENT_6))
            .as(BODY_MESSAGE)
            .extracting(Element::text)
            .isEqualTo("Any media representative or any other member of the public "
                           + "wishing to witness the hearing will need to do so over the "
                           + "internet and provide an email address at which to be sent an "
                           + "appropriate link for access.");

        softly.assertThat(document.getElementById(MESSAGE_ELEMENT_7))
            .as(BODY_MESSAGE)
            .extracting(Element::text)
            .isEqualTo("Please contact adminappeals@justice.gov.uk.");

        softly.assertThat(document.getElementById(MESSAGE_ELEMENT_8))
            .as(BODY_MESSAGE)
            .extracting(Element::text)
            .isEqualTo("Scotland");

        softly.assertThat(document.getElementById(MESSAGE_ELEMENT_9))
            .as(BODY_MESSAGE)
            .extracting(Element::text)
            .isEqualTo("Remote hearings");

        softly.assertThat(document.getElementById(MESSAGE_ELEMENT_10))
            .as(BODY_MESSAGE)
            .extracting(Element::text)
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

        softly.assertAll();
    }

    @Test
    void testAdministrativeAppealsChamberWeeklyHearingListFileConversionInWelsh() throws IOException {
        Map<String, Object> languageResource;
        try (InputStream languageFile = Thread.currentThread()
            .getContextClassLoader()
            .getResourceAsStream("templates/languages/cy/non-strategic/utAacWeeklyHearingList.json")) {
            languageResource = new ObjectMapper().readValue(
                Objects.requireNonNull(languageFile).readAllBytes(), new TypeReference<>() {
                });
        }

        Map<String, String> metadata = Map.of(CONTENT_DATE_METADATA, CONTENT_DATE,
                                              PROVENANCE_METADATA, PROVENANCE,
                                              LANGUAGE_METADATA, WELSH,
                                              LIST_TYPE_METADATA, LIST_NAME
        );

        String result = converter.convert(listInputJson, metadata, languageResource);
        Document document = Jsoup.parse(result);

        SoftAssertions softly = new SoftAssertions();

        softly.assertThat(document.title())
            .as(TITLE_MESSAGE)
            .isEqualTo(LIST_WELSH_NAME);

        softly.assertThat(document.getElementById(HEADER_ELEMENT))
            .as(HEADER_MESSAGE)
            .extracting(Element::text)
            .isEqualTo(LIST_WELSH_NAME);

        softly.assertThat(document.getElementById(LIST_DATE_ELEMENT))
            .as(LIST_DATE_MESSAGE)
            .extracting(Element::text)
            .isEqualTo(LIST_DATE_WELSH);

        softly.assertThat(document.getElementById(MESSAGE_ELEMENT_1))
            .as(BODY_MESSAGE)
            .extracting(Element::text)
            .isEqualTo("Details");

        softly.assertThat(document.getElementById(MESSAGE_ELEMENT_2))
            .as(BODY_MESSAGE)
            .extracting(Element::text)
            .isEqualTo("Lists are subject to change until 4:30pm. Any alterations after "
                           + "this time will be telephoned or emailed direct to the parties "
                           + "or their legal representatives.");

        softly.assertThat(document.getElementById(MESSAGE_ELEMENT_3))
            .as(BODY_MESSAGE)
            .extracting(Element::text)
            .isEqualTo("Cymru a Lloegr");

        softly.assertThat(document.getElementById(MESSAGE_ELEMENT_4))
            .as(BODY_MESSAGE)
            .extracting(Element::text)
            .isEqualTo("Gwrandawiadau o bell trwy CVP a BT Meet Me");

        softly.assertThat(document.getElementById(MESSAGE_ELEMENT_5))
            .as(BODY_MESSAGE)
            .extracting(Element::text)
            .isEqualTo("Bydd gwrandawiadau ar gael i gynrychiolwyr y cyfryngau neu "
                           + "unrhyw aelod arall o’r cyhoedd, ar eu cais, ac felly byddant "
                           + "yn wrandawiad a gynhelir yn gyhoeddus yn unol â Rheol 37 o "
                           + "Reolau Trefniadaeth y Tribiwnlysoedd (Uwch Dribiwnlys) 2008.");

        softly.assertThat(document.getElementById(MESSAGE_ELEMENT_6))
            .as(BODY_MESSAGE)
            .extracting(Element::text)
            .isEqualTo("Bydd angen i unrhyw gynrychiolydd o’r cyfryngau neu unrhyw aelod arall "
                           + "o’r cyhoedd sy’n dymuno gwylio’r gwrandawiad wneud hynny dros y rhyngrwyd a "
                           + "darparu cyfeiriad e-bost er mwyn anfon dolen briodol i gael mynediad.");

        softly.assertThat(document.getElementById(MESSAGE_ELEMENT_7))
            .as(BODY_MESSAGE)
            .extracting(Element::text)
            .isEqualTo("Cysylltwch â adminappeals@justice.gov.uk");

        softly.assertThat(document.getElementById(MESSAGE_ELEMENT_8))
            .as(BODY_MESSAGE)
            .extracting(Element::text)
            .isEqualTo("Scotland");

        softly.assertThat(document.getElementById(MESSAGE_ELEMENT_9))
            .as(BODY_MESSAGE)
            .extracting(Element::text)
            .isEqualTo("Gwrandawiadau o bell");

        softly.assertThat(document.getElementById(MESSAGE_ELEMENT_10))
            .as(BODY_MESSAGE)
            .extracting(Element::text)
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

        softly.assertAll();
    }
}
