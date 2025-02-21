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

@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class SscsDailyHearingListFileConverterWelshTest {

    private static final String CONTENT_DATE = "12 December 2024";
    private static final String LAST_RECEIVED_DATE = "2025-01-20T09:30:00Z";
    private static final String PROVENANCE = "provenance";
    private static final String CONTENT_DATE_METADATA = "contentDate";
    private static final String PROVENANCE_METADATA = "provenance";
    private static final String LANGUAGE_METADATA = "language";
    private static final String LIST_TYPE_METADATA = "listType";
    private static final String LAST_RECEIVED_DATE_METADATA = "lastReceivedDate";

    private static final String WELSH = "WELSH";

    private static final String LIST_DATE_WELSH = "Rhestr ar gyfer yr wythnos yn dechrau ar 12 December 2024";
    private static final String OBSERVE_HEARING_WELSH = "Am fwy o wybodaeth, ewch i "
        + "https://www.gov.uk/guidance/observe-a-court-or-tribunal-hearing";

    private static final String HEADER_ELEMENT = "page-heading";
    private static final String LIST_DATE_ELEMENT = "list-date";
    private static final String LAST_UPDATED_DATE_ELEMENT = "last-updated-date";
    private static final String CONTACT_MESSAGE_ELEMENT = "contact-message";
    private static final String OPEN_JUSTICE_ELEMENT = "open-justice-message";
    private static final String OBSERVE_HEARING_ELEMENT =  "observe-hearing";
    private static final String SUMMARY_TEXT_CLASS = "govuk-details__summary-text";

    private static final String TITLE_MESSAGE = "Title does not match";
    private static final String HEADER_MESSAGE = "Header does not match";
    private static final String LIST_DATE_MESSAGE = "List date does not match";
    private static final String LAST_UPDATED_DATE_MESSAGE = "Last updated date does not match";
    private static final String IMPORTANT_INFORMATION_MESSAGE = "Important information heading does not match";
    private static final String BODY_MESSAGE = "Body does not match";
    private static final String TABLE_HEADERS_MESSAGE = "Table headers does not match";
    private static final String OPEN_JUSTICE_TEXT =
        "Mae cyfiawnder agored yn egwyddor hanfodol yn system y "
        + "llysoedd a’r tribiwnlysoedd. Wrth ystyried defnyddio technoleg "
        + "ffôn a fideo, bydd y farnwriaeth yn rhoi sylw i egwyddorion cyfiawnder "
        + "agored. Gall barnwyr benderfynu cynnal gwrandawiad yn breifat os oes "
        + "angen hynny er mwyn sicrhau'r broses o weinyddu cyfiawnder yn briodol.";
    private static final String CONTACT_MESSAGE_TEXT =
        "Bydd partïon a chynrychiolwyr y Tribiwnlys Nawdd Cymdeithasol a "
        + "Chynnal Plant yn cael gwybod yn uniongyrchol am y trefniadau ar "
        + "gyfer gwrando achosion o bell. Dylai unrhyw un arall sydd â "
        + "diddordeb mewn ymuno â’r gwrandawiad o bell gysylltu â Swyddfa’r Tribiwnlys "
        + "Nawdd Cymdeithasol a Chynnal Plant yn uniongyrchol, cyn dyddiad y "
        + "gwrandawiad, trwy e-bostio EMAIL "
        + "fel y gellir gwneud trefniadau. Dylai'r manylion canlynol gael eu "
        + "cynnwys yn llinell pwnc yr e-bost [OBSERVER/MEDIA] REQUEST";
    private static final String LAST_UPDATED_DATE_TEXT = "Diweddarwyd ddiwethaf 20 January 2025 am 9:30am";
    private static final String IMPORTANT_INFORMATION_TEXT = "Gwybodaeth bwysig";
    private static final String LANGUAGE_FILE_PATH = "templates/languages/cy/non-strategic/";
    private static final String EMAIL = "EMAIL";


    private final NonStrategicListFileConverter converter = new NonStrategicListFileConverter();

    private JsonNode listInputJson;

    @BeforeAll
    void setup() throws IOException {
        try (InputStream inputStream = getClass()
            .getResourceAsStream("/mocks/non-strategic/sscsDailyHearingList.json")) {
            String inputRaw = IOUtils.toString(inputStream, Charset.defaultCharset());
            listInputJson = new ObjectMapper().readTree(inputRaw);
        }
    }

    @Test
    void testSscsMidlandsDailyHearingListFileConversionInWelsh() throws IOException {
        Map<String, Object> languageResource;
        try (InputStream languageFile = Thread.currentThread()
            .getContextClassLoader()
            .getResourceAsStream(LANGUAGE_FILE_PATH
                                     + "sscsMidlandsDailyHearingList.json")) {
            languageResource = new ObjectMapper().readValue(
                Objects.requireNonNull(languageFile).readAllBytes(), new TypeReference<>() {
                });
        }

        Map<String, String> metadata = Map.of(CONTENT_DATE_METADATA, CONTENT_DATE,
                                              PROVENANCE_METADATA, PROVENANCE,
                                              LANGUAGE_METADATA, WELSH,
                                              LIST_TYPE_METADATA, "SSCS_MIDLANDS_DAILY_HEARING_LIST",
                                              LAST_RECEIVED_DATE_METADATA, LAST_RECEIVED_DATE
        );

        String result = converter.convert(listInputJson, metadata, languageResource);
        Document document = Jsoup.parse(result);

        SoftAssertions softly = new SoftAssertions();

        softly.assertThat(document.title())
            .as(TITLE_MESSAGE)
            .isEqualTo("Rhestr o Wrandawiadau Dyddiol Tribiwnlys Haen Gyntaf Canolbarth"
                           + " Lloegr (Nawdd Cymdeithasol a Chynnal Plant)");

        softly.assertThat(document.getElementById(HEADER_ELEMENT))
            .as(HEADER_MESSAGE)
            .extracting(Element::text)
            .isEqualTo("Rhestr o Wrandawiadau Dyddiol Tribiwnlys Haen Gyntaf Canolbarth"
                           + " Lloegr (Nawdd Cymdeithasol a Chynnal Plant)");

        softly.assertThat(document.getElementById(LIST_DATE_ELEMENT))
            .as(LIST_DATE_MESSAGE)
            .extracting(Element::text)
            .isEqualTo(LIST_DATE_WELSH);

        softly.assertThat(document.getElementById(LAST_UPDATED_DATE_ELEMENT))
            .as(LAST_UPDATED_DATE_MESSAGE)
            .extracting(Element::text)
            .isEqualTo(LAST_UPDATED_DATE_TEXT);

        softly.assertThat(document.getElementsByClass(SUMMARY_TEXT_CLASS).get(0))
            .as(IMPORTANT_INFORMATION_MESSAGE)
            .extracting(Element::text)
            .isEqualTo(IMPORTANT_INFORMATION_TEXT);

        softly.assertThat(document.getElementById(OPEN_JUSTICE_ELEMENT))
            .as(BODY_MESSAGE)
            .extracting(Element::text)
            .isEqualTo(OPEN_JUSTICE_TEXT);

        softly.assertThat(document.getElementById(CONTACT_MESSAGE_ELEMENT))
            .as(BODY_MESSAGE)
            .extracting(Element::text)
            .isEqualTo(CONTACT_MESSAGE_TEXT.replace(EMAIL, "ascbirmingham@justice.gov.uk"));


        softly.assertThat(document.getElementById(OBSERVE_HEARING_ELEMENT))
            .as(BODY_MESSAGE)
            .extracting(Element::text)
            .isEqualTo(OBSERVE_HEARING_WELSH);

        softly.assertThat(document.getElementsByTag("th"))
            .as(TABLE_HEADERS_MESSAGE)
            .hasSize(9)
            .extracting(Element::text)
            .containsExactly(
                "Lleoliad",
                "Cyfeirnod apêl",
                "Math o wrandawiad",
                "Apelydd",
                "Ystafell llys",
                "Amser y gwrandawiad",
                "Panel",
                "FTA/Atebydd",
                "Gwybodaeth ychwanegol"
            );

        softly.assertAll();
    }

    @Test
    void testSscsSouthEastDailyHearingListFileConversionInWelsh() throws IOException {
        Map<String, Object> languageResource;
        try (InputStream languageFile = Thread.currentThread()
            .getContextClassLoader()
            .getResourceAsStream(LANGUAGE_FILE_PATH
                                     + "sscsSouthEastDailyHearingList.json")) {
            languageResource = new ObjectMapper().readValue(
                Objects.requireNonNull(languageFile).readAllBytes(), new TypeReference<>() {
                });
        }

        Map<String, String> metadata = Map.of(CONTENT_DATE_METADATA, CONTENT_DATE,
                                              PROVENANCE_METADATA, PROVENANCE,
                                              LANGUAGE_METADATA, WELSH,
                                              LIST_TYPE_METADATA, "SSCS_SOUTH_EAST_DAILY_HEARING_LIST",
                                              LAST_RECEIVED_DATE_METADATA, LAST_RECEIVED_DATE
        );

        String result = converter.convert(listInputJson, metadata, languageResource);
        Document document = Jsoup.parse(result);

        SoftAssertions softly = new SoftAssertions();

        softly.assertThat(document.title())
            .as(TITLE_MESSAGE)
            .isEqualTo("Rhestr o Wrandawiadau Dyddiol Tribiwnlys Haen Gyntaf De "
                           + "Ddwyrain Lloegr (Nawdd Cymdeithasol a Chynnal Plant)");

        softly.assertThat(document.getElementById(HEADER_ELEMENT))
            .as(HEADER_MESSAGE)
            .extracting(Element::text)
            .isEqualTo("Rhestr o Wrandawiadau Dyddiol Tribiwnlys Haen Gyntaf De "
                           + "Ddwyrain Lloegr (Nawdd Cymdeithasol a Chynnal Plant)");

        softly.assertThat(document.getElementById(LIST_DATE_ELEMENT))
            .as(LIST_DATE_MESSAGE)
            .extracting(Element::text)
            .isEqualTo(LIST_DATE_WELSH);

        softly.assertThat(document.getElementById(LAST_UPDATED_DATE_ELEMENT))
            .as(LAST_UPDATED_DATE_MESSAGE)
            .extracting(Element::text)
            .isEqualTo(LAST_UPDATED_DATE_TEXT);

        softly.assertThat(document.getElementsByClass(SUMMARY_TEXT_CLASS).get(0))
            .as(IMPORTANT_INFORMATION_MESSAGE)
            .extracting(Element::text)
            .isEqualTo(IMPORTANT_INFORMATION_TEXT);

        softly.assertThat(document.getElementById(OPEN_JUSTICE_ELEMENT))
            .as(BODY_MESSAGE)
            .extracting(Element::text)
            .isEqualTo(OPEN_JUSTICE_TEXT);

        softly.assertThat(document.getElementById(CONTACT_MESSAGE_ELEMENT))
            .as(BODY_MESSAGE)
            .extracting(Element::text)
            .isEqualTo(CONTACT_MESSAGE_TEXT.replace(EMAIL, "sscs_bradford@justice.gov.uk"));


        softly.assertThat(document.getElementById(OBSERVE_HEARING_ELEMENT))
            .as(BODY_MESSAGE)
            .extracting(Element::text)
            .isEqualTo(OBSERVE_HEARING_WELSH);

        softly.assertAll();
    }

    @Test
    void testSscsWalesAndSouthWestDailyHearingListFileConversionInWelsh() throws IOException {
        Map<String, Object> languageResource;
        try (InputStream languageFile = Thread.currentThread()
            .getContextClassLoader()
            .getResourceAsStream(LANGUAGE_FILE_PATH
                                     + "sscsWalesAndSouthWestDailyHearingList.json")) {
            languageResource = new ObjectMapper().readValue(
                Objects.requireNonNull(languageFile).readAllBytes(), new TypeReference<>() {
                });
        }

        Map<String, String> metadata = Map.of(CONTENT_DATE_METADATA, CONTENT_DATE,
                                              PROVENANCE_METADATA, PROVENANCE,
                                              LANGUAGE_METADATA, WELSH,
                                              LIST_TYPE_METADATA, "SSCS_WALES_AND_SOUTH_WEST_DAILY_HEARING_LIST",
                                              LAST_RECEIVED_DATE_METADATA, LAST_RECEIVED_DATE
        );

        String result = converter.convert(listInputJson, metadata, languageResource);
        Document document = Jsoup.parse(result);

        SoftAssertions softly = new SoftAssertions();

        softly.assertThat(document.title())
            .as(TITLE_MESSAGE)
            .isEqualTo("Rhestr o Wrandawiadau Dyddiol Tribiwnlys Haen Gyntaf Cymru a De "
                           + "Orllewin Lloegr (Nawdd Cymdeithasol a Chynnal Plant)");

        softly.assertThat(document.getElementById(HEADER_ELEMENT))
            .as(HEADER_MESSAGE)
            .extracting(Element::text)
            .isEqualTo("Rhestr o Wrandawiadau Dyddiol Tribiwnlys Haen Gyntaf Cymru a De "
                           + "Orllewin Lloegr (Nawdd Cymdeithasol a Chynnal Plant)");

        softly.assertThat(document.getElementById(LIST_DATE_ELEMENT))
            .as(LIST_DATE_MESSAGE)
            .extracting(Element::text)
            .isEqualTo(LIST_DATE_WELSH);

        softly.assertThat(document.getElementById(LAST_UPDATED_DATE_ELEMENT))
            .as(LAST_UPDATED_DATE_MESSAGE)
            .extracting(Element::text)
            .isEqualTo(LAST_UPDATED_DATE_TEXT);

        softly.assertThat(document.getElementsByClass(SUMMARY_TEXT_CLASS).get(0))
            .as(IMPORTANT_INFORMATION_MESSAGE)
            .extracting(Element::text)
            .isEqualTo(IMPORTANT_INFORMATION_TEXT);

        softly.assertThat(document.getElementById(OPEN_JUSTICE_ELEMENT))
            .as(BODY_MESSAGE)
            .extracting(Element::text)
            .isEqualTo(OPEN_JUSTICE_TEXT);

        softly.assertThat(document.getElementById(CONTACT_MESSAGE_ELEMENT))
            .as(BODY_MESSAGE)
            .extracting(Element::text)
            .isEqualTo(CONTACT_MESSAGE_TEXT.replace(EMAIL, "sscsa-cardiff@justice.gov.uk"));


        softly.assertThat(document.getElementById(OBSERVE_HEARING_ELEMENT))
            .as(BODY_MESSAGE)
            .extracting(Element::text)
            .isEqualTo(OBSERVE_HEARING_WELSH);

        softly.assertAll();
    }

    @Test
    void testSscsScotlandDailyHearingListFileConversionInWelsh() throws IOException {
        Map<String, Object> languageResource;
        try (InputStream languageFile = Thread.currentThread()
            .getContextClassLoader()
            .getResourceAsStream(LANGUAGE_FILE_PATH
                                     + "sscsScotlandDailyHearingList.json")) {
            languageResource = new ObjectMapper().readValue(
                Objects.requireNonNull(languageFile).readAllBytes(), new TypeReference<>() {
                });
        }

        Map<String, String> metadata = Map.of(CONTENT_DATE_METADATA, CONTENT_DATE,
                                              PROVENANCE_METADATA, PROVENANCE,
                                              LANGUAGE_METADATA, WELSH,
                                              LIST_TYPE_METADATA, "SSCS_SCOTLAND_DAILY_HEARING_LIST",
                                              LAST_RECEIVED_DATE_METADATA, LAST_RECEIVED_DATE
        );

        String result = converter.convert(listInputJson, metadata, languageResource);
        Document document = Jsoup.parse(result);

        SoftAssertions softly = new SoftAssertions();

        softly.assertThat(document.title())
            .as(TITLE_MESSAGE)
            .isEqualTo("Rhestr o Wrandawiadau Dyddiol Tribiwnlys Haen Gyntaf Yr Alban "
                           + "(Nawdd Cymdeithasol a Chynnal Plant)");

        softly.assertThat(document.getElementById(HEADER_ELEMENT))
            .as(HEADER_MESSAGE)
            .extracting(Element::text)
            .isEqualTo("Rhestr o Wrandawiadau Dyddiol Tribiwnlys Haen Gyntaf Yr Alban "
                           + "(Nawdd Cymdeithasol a Chynnal Plant)");

        softly.assertThat(document.getElementById(LIST_DATE_ELEMENT))
            .as(LIST_DATE_MESSAGE)
            .extracting(Element::text)
            .isEqualTo(LIST_DATE_WELSH);

        softly.assertThat(document.getElementById(LAST_UPDATED_DATE_ELEMENT))
            .as(LAST_UPDATED_DATE_MESSAGE)
            .extracting(Element::text)
            .isEqualTo(LAST_UPDATED_DATE_TEXT);

        softly.assertThat(document.getElementsByClass(SUMMARY_TEXT_CLASS).get(0))
            .as(IMPORTANT_INFORMATION_MESSAGE)
            .extracting(Element::text)
            .isEqualTo(IMPORTANT_INFORMATION_TEXT);

        softly.assertThat(document.getElementById(OPEN_JUSTICE_ELEMENT))
            .as(BODY_MESSAGE)
            .extracting(Element::text)
            .isEqualTo(OPEN_JUSTICE_TEXT);

        softly.assertThat(document.getElementById(CONTACT_MESSAGE_ELEMENT))
            .as(BODY_MESSAGE)
            .extracting(Element::text)
            .isEqualTo(CONTACT_MESSAGE_TEXT.replace(EMAIL, "sscsa-glasgow@justice.gov.uk"));


        softly.assertThat(document.getElementById(OBSERVE_HEARING_ELEMENT))
            .as(BODY_MESSAGE)
            .extracting(Element::text)
            .isEqualTo(OBSERVE_HEARING_WELSH);

        softly.assertAll();
    }

    @Test
    void testSscsNorthEastDailyHearingListFileConversionInWelsh() throws IOException {
        Map<String, Object> languageResource;
        try (InputStream languageFile = Thread.currentThread()
            .getContextClassLoader()
            .getResourceAsStream(LANGUAGE_FILE_PATH
                                     + "sscsNorthEastDailyHearingList.json")) {
            languageResource = new ObjectMapper().readValue(
                Objects.requireNonNull(languageFile).readAllBytes(), new TypeReference<>() {
                });
        }

        Map<String, String> metadata = Map.of(CONTENT_DATE_METADATA, CONTENT_DATE,
                                              PROVENANCE_METADATA, PROVENANCE,
                                              LANGUAGE_METADATA, WELSH,
                                              LIST_TYPE_METADATA, "SSCS_NORTH_EAST_DAILY_HEARING_LIST",
                                              LAST_RECEIVED_DATE_METADATA, LAST_RECEIVED_DATE
        );

        String result = converter.convert(listInputJson, metadata, languageResource);
        Document document = Jsoup.parse(result);

        SoftAssertions softly = new SoftAssertions();

        softly.assertThat(document.title())
            .as(TITLE_MESSAGE)
            .isEqualTo("Rhestr o Wrandawiadau Dyddiol Tribiwnlys Haen Gyntaf Gogledd "
                           + "Ddwyrain Lloegr (Nawdd Cymdeithasol a Chymorth Plant)");

        softly.assertThat(document.getElementById(HEADER_ELEMENT))
            .as(HEADER_MESSAGE)
            .extracting(Element::text)
            .isEqualTo("Rhestr o Wrandawiadau Dyddiol Tribiwnlys Haen Gyntaf Gogledd "
                           + "Ddwyrain Lloegr (Nawdd Cymdeithasol a Chymorth Plant)");

        softly.assertThat(document.getElementById(LIST_DATE_ELEMENT))
            .as(LIST_DATE_MESSAGE)
            .extracting(Element::text)
            .isEqualTo(LIST_DATE_WELSH);

        softly.assertThat(document.getElementById(LAST_UPDATED_DATE_ELEMENT))
            .as(LAST_UPDATED_DATE_MESSAGE)
            .extracting(Element::text)
            .isEqualTo(LAST_UPDATED_DATE_TEXT);

        softly.assertThat(document.getElementsByClass(SUMMARY_TEXT_CLASS).get(0))
            .as(IMPORTANT_INFORMATION_MESSAGE)
            .extracting(Element::text)
            .isEqualTo(IMPORTANT_INFORMATION_TEXT);

        softly.assertThat(document.getElementById(OPEN_JUSTICE_ELEMENT))
            .as(BODY_MESSAGE)
            .extracting(Element::text)
            .isEqualTo(OPEN_JUSTICE_TEXT);

        softly.assertThat(document.getElementById(CONTACT_MESSAGE_ELEMENT))
            .as(BODY_MESSAGE)
            .extracting(Element::text)
            .isEqualTo(CONTACT_MESSAGE_TEXT.replace(EMAIL, "sscsa-leeds@justice.gov.uk"));


        softly.assertThat(document.getElementById(OBSERVE_HEARING_ELEMENT))
            .as(BODY_MESSAGE)
            .extracting(Element::text)
            .isEqualTo(OBSERVE_HEARING_WELSH);

        softly.assertAll();
    }
}
