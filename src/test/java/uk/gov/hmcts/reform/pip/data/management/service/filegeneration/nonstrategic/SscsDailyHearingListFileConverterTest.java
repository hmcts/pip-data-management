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
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.pip.data.management.service.filegeneration.NonStrategicListFileConverter;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class SscsDailyHearingListFileConverterTest {

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

    private static final String LIST_DATE_ENGLISH = "List for week commencing 12 December 2024";
    private static final String LIST_DATE_WELSH = "Rhestr ar gyfer yr wythnos yn dechrau ar 12 December 2024";
    private static final String OBSERVE_HEARING_ENGLISH = "For more information, please visit "
        + "https://www.gov.uk/guidance/observe-a-court-or-tribunal-hearing";
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

    private final NonStrategicListFileConverter converter = new NonStrategicListFileConverter();

    private JsonNode listInputJson;

    private static Stream<Arguments> parametersEnglish() {
        return Stream.of(
            Arguments.of("SSCS_MIDLANDS_DAILY_HEARING_LIST", "sscsMidlandsDailyHearingList.json",
                         "Midlands First-tier Tribunal (Social Security and Child Support) Daily Hearing List",
                         "ascbirmingham@justice.gov.uk"),
            Arguments.of("SSCS_SOUTH_EAST_DAILY_HEARING_LIST", "sscsSoutheastDailyHearingList.json",
                         "South East First-tier Tribunal (Social Security and Child Support) Daily Hearing List",
                         "sscs_bradford@justice.gov.uk"),
            Arguments.of("SSCS_WALES_AND_SOUTH_WEST_DAILY_HEARING_LIST", "sscsWalesAndSouthwestDailyHearingList.json",
                         "Wales and South West First-tier Tribunal (Social Security and Child Support)"
                             + " Daily Hearing List",
                         "sscsa-cardiff@justice.gov.uk"),
            Arguments.of("SSCS_SCOTLAND_DAILY_HEARING_LIST", "sscsScotlandDailyHearingList.json",
                         "Scotland First-tier Tribunal (Social Security and Child Support) Daily Hearing List",
                         "sscsa-glasgow@justice.gov.uk"),
            Arguments.of("SSCS_NORTH_EAST_DAILY_HEARING_LIST", "sscsNortheastDailyHearingList.json",
                         "North East First-tier Tribunal (Social Security and Child Support) Daily Hearing List",
                         "sscsa-leeds@Justice.gov.uk"),
            Arguments.of("SSCS_NORTH_WEST_DAILY_HEARING_LIST", "sscsNorthwestDailyHearingList.json",
                         "North West First-tier Tribunal (Social Security and Child Support) Daily Hearing List",
                         "sscsa-liverpool@justice.gov.uk"),
            Arguments.of("SSCS_LONDON_DAILY_HEARING_LIST", "sscsLondonDailyHearingList.json",
                         "London First-tier Tribunal (Social Security and Child Support) Daily Hearing List",
                         "sscsa-sutton@justice.gov.uk")
        );
    }

    private static Stream<Arguments> parametersWelsh() {
        return Stream.of(
            Arguments.of("SSCS_MIDLANDS_DAILY_HEARING_LIST", "sscsMidlandsDailyHearingList.json",
                         "Rhestr o Wrandawiadau Dyddiol Tribiwnlys Haen Gyntaf "
                             + "Canolbarth Lloegr (Nawdd Cymdeithasol a Chynnal Plant)",
                         "ascbirmingham@justice.gov.uk"),
            Arguments.of("SSCS_SOUTH_EAST_DAILY_HEARING_LIST", "sscsSoutheastDailyHearingList.json",
                         "Rhestr o Wrandawiadau Dyddiol Tribiwnlys Haen Gyntaf "
                             + "De Ddwyrain Lloegr (Nawdd Cymdeithasol a Chynnal Plant)",
                         "sscs_bradford@justice.gov.uk"),
            Arguments.of("SSCS_WALES_AND_SOUTH_WEST_DAILY_HEARING_LIST", "sscsWalesAndSouthwestDailyHearingList.json",
                         "Rhestr o Wrandawiadau Dyddiol Tribiwnlys Haen Gyntaf Cymru a De "
                             + "Orllewin Lloegr (Nawdd Cymdeithasol a Chynnal Plant)",
                         "sscsa-cardiff@justice.gov.uk"),
            Arguments.of("SSCS_SCOTLAND_DAILY_HEARING_LIST", "sscsScotlandDailyHearingList.json",
                         "Rhestr o Wrandawiadau Dyddiol Tribiwnlys Haen Gyntaf Yr Alban "
                             + "(Nawdd Cymdeithasol a Chynnal Plant)",
                         "sscsa-glasgow@justice.gov.uk"),
            Arguments.of("SSCS_NORTH_EAST_DAILY_HEARING_LIST", "sscsNortheastDailyHearingList.json",
                         "Rhestr o Wrandawiadau Dyddiol Tribiwnlys Haen Gyntaf Gogledd "
                             + "Ddwyrain Lloegr (Nawdd Cymdeithasol a Chymorth Plant)",
                         "sscsa-leeds@Justice.gov.uk"),
            Arguments.of("SSCS_NORTH_WEST_DAILY_HEARING_LIST", "sscsNorthwestDailyHearingList.json",
                         "Rhestr o Wrandawiadau Dyddiol Nawdd Cymdeithasol a Chynnal Plant Gogledd "
                             + "Orllewin Lloegr (Nawdd Cymdeithasol a Chynnal Plant)",
                         "sscsa-liverpool@justice.gov.uk"),
            Arguments.of("SSCS_LONDON_DAILY_HEARING_LIST", "sscsLondonDailyHearingList.json",
                         "Rhestr o Wrandawiadau Dyddiol Tribiwnlys Haen Gyntaf Llundain "
                             + "(Nawdd Cymdeithasol a Chynnal Plant)",
                         "sscsa-sutton@justice.gov.uk")
        );
    }

    @BeforeAll
    void setup() throws IOException {
        try (InputStream inputStream = getClass()
            .getResourceAsStream("/mocks/non-strategic/sscsDailyHearingList.json")) {
            String inputRaw = IOUtils.toString(inputStream, Charset.defaultCharset());
            listInputJson = new ObjectMapper().readTree(inputRaw);
        }
    }

    @ParameterizedTest
    @MethodSource("parametersEnglish")
    void testSscsDailyHearingListFileConversionInEnglish(String listName,
        String languageFilename, String listDisplayName, String contactEmail) throws IOException {
        Map<String, Object> languageResource;
        try (InputStream languageFile = Thread.currentThread()
            .getContextClassLoader()
            .getResourceAsStream("templates/languages/en/non-strategic/" + languageFilename)) {
            languageResource = new ObjectMapper().readValue(
                Objects.requireNonNull(languageFile).readAllBytes(), new TypeReference<>() {
                });
        }

        Map<String, String> metadata = Map.of(CONTENT_DATE_METADATA, CONTENT_DATE,
                                              PROVENANCE_METADATA, PROVENANCE,
                                              LANGUAGE_METADATA, ENGLISH,
                                              LIST_TYPE_METADATA, listName,
                                              LAST_RECEIVED_DATE_METADATA, LAST_RECEIVED_DATE
        );

        String result = converter.convert(listInputJson, metadata, languageResource);
        Document document = Jsoup.parse(result);

        SoftAssertions softly = new SoftAssertions();

        softly.assertThat(document.title())
            .as(TITLE_MESSAGE)
            .isEqualTo(listDisplayName);

        softly.assertThat(document.getElementById(HEADER_ELEMENT))
            .as(HEADER_MESSAGE)
            .extracting(Element::text)
            .isEqualTo(listDisplayName);

        softly.assertThat(document.getElementById(LIST_DATE_ELEMENT))
            .as(LIST_DATE_MESSAGE)
            .extracting(Element::text)
            .isEqualTo(LIST_DATE_ENGLISH);

        softly.assertThat(document.getElementById(LAST_UPDATED_DATE_ELEMENT))
            .as(LAST_UPDATED_DATE_MESSAGE)
            .extracting(Element::text)
            .isEqualTo("Last updated 20 January 2025 at 9:30am");

        softly.assertThat(document.getElementsByClass(SUMMARY_TEXT_CLASS).get(0))
            .as(IMPORTANT_INFORMATION_MESSAGE)
            .extracting(Element::text)
            .isEqualTo("Important information");

        softly.assertThat(document.getElementById(OPEN_JUSTICE_ELEMENT))
            .as(BODY_MESSAGE)
            .extracting(Element::text)
            .isEqualTo("Open justice is a fundamental principle of our justice system. "
                           + "When considering the use of telephone and video technology, the judiciary "
                           + "will have regard to the principles of open justice. Judges may determine "
                           + "that a hearing should be held in private if this is necessary to secure the "
                           + "proper administration of justice.");

        softly.assertThat(document.getElementById(CONTACT_MESSAGE_ELEMENT))
            .as(BODY_MESSAGE)
            .extracting(Element::text)
            .isEqualTo("Social Security and Child Support Tribunal parties and "
                           + "representatives will be informed directly as to the arrangements "
                           + "for hearing cases remotely. Any other person interested in joining "
                           + "the hearing remotely should contact the Social Security and Child Support "
                           + "Tribunal Office direct, in advance of the hearing date, "
                           + "by emailing EMAIL ".replace("EMAIL", contactEmail)
                           + "so that arrangements can be made. The following details should "
                           + "be included in the subject line of the email [OBSERVER/MEDIA] REQUEST "
                           + "– [case reference] – [hearing date]. If the case is to be heard in "
                           + "private or is subject to a reporting restriction, this will be notified.");

        softly.assertThat(document.getElementById(OBSERVE_HEARING_ELEMENT))
            .as(BODY_MESSAGE)
            .extracting(Element::text)
            .isEqualTo(OBSERVE_HEARING_ENGLISH);

        softly.assertThat(document.getElementsByTag("th"))
            .as(TABLE_HEADERS_MESSAGE)
            .hasSize(9)
            .extracting(Element::text)
            .containsExactly(
                "Venue",
                "Appeal reference number",
                "Hearing type",
                "Appellant",
                "Courtroom",
                "Hearing time",
                "Panel",
                "FTA/Respondent",
                "Additional information"
            );

        softly.assertAll();
    }

    @ParameterizedTest
    @MethodSource("parametersWelsh")
    void testSscsDailyHearingListFileConversionInWelsh(String listName,
        String languageFilename, String listDisplayName, String emailContact) throws IOException {
        Map<String, Object> languageResource;
        try (InputStream languageFile = Thread.currentThread()
            .getContextClassLoader()
            .getResourceAsStream("templates/languages/cy/non-strategic/" + languageFilename)) {
            languageResource = new ObjectMapper().readValue(
                Objects.requireNonNull(languageFile).readAllBytes(), new TypeReference<>() {
                });
        }

        Map<String, String> metadata = Map.of(CONTENT_DATE_METADATA, CONTENT_DATE,
                                              PROVENANCE_METADATA, PROVENANCE,
                                              LANGUAGE_METADATA, WELSH,
                                              LIST_TYPE_METADATA, listName,
                                              LAST_RECEIVED_DATE_METADATA, LAST_RECEIVED_DATE
        );

        String result = converter.convert(listInputJson, metadata, languageResource);
        Document document = Jsoup.parse(result);

        SoftAssertions softly = new SoftAssertions();

        softly.assertThat(document.title())
            .as(TITLE_MESSAGE)
            .isEqualTo(listDisplayName);

        softly.assertThat(document.getElementById(HEADER_ELEMENT))
            .as(HEADER_MESSAGE)
            .extracting(Element::text)
            .isEqualTo(listDisplayName);

        softly.assertThat(document.getElementById(LIST_DATE_ELEMENT))
            .as(LIST_DATE_MESSAGE)
            .extracting(Element::text)
            .isEqualTo(LIST_DATE_WELSH);

        softly.assertThat(document.getElementById(LAST_UPDATED_DATE_ELEMENT))
            .as(LAST_UPDATED_DATE_MESSAGE)
            .extracting(Element::text)
            .isEqualTo("Diweddarwyd ddiwethaf 20 January 2025 am 9:30am");

        softly.assertThat(document.getElementsByClass(SUMMARY_TEXT_CLASS).get(0))
            .as(IMPORTANT_INFORMATION_MESSAGE)
            .extracting(Element::text)
            .isEqualTo("Gwybodaeth bwysig");

        softly.assertThat(document.getElementById(OPEN_JUSTICE_ELEMENT))
            .as(BODY_MESSAGE)
            .extracting(Element::text)
            .isEqualTo("Mae cyfiawnder agored yn egwyddor hanfodol yn system y "
                           + "llysoedd a’r tribiwnlysoedd. Wrth ystyried defnyddio technoleg "
                           + "ffôn a fideo, bydd y farnwriaeth yn rhoi sylw i egwyddorion cyfiawnder "
                           + "agored. Gall barnwyr benderfynu cynnal gwrandawiad yn breifat os oes "
                           + "angen hynny er mwyn sicrhau'r broses o weinyddu cyfiawnder yn briodol.");

        softly.assertThat(document.getElementById(CONTACT_MESSAGE_ELEMENT))
            .as(BODY_MESSAGE)
            .extracting(Element::text)
            .isEqualTo("Bydd partïon a chynrychiolwyr y Tribiwnlys Nawdd Cymdeithasol a "
                           + "Chynnal Plant yn cael gwybod yn uniongyrchol am y trefniadau ar "
                           + "gyfer gwrando achosion o bell. Dylai unrhyw un arall sydd â "
                           + "diddordeb mewn ymuno â’r gwrandawiad o bell gysylltu â Swyddfa’r Tribiwnlys "
                           + "Nawdd Cymdeithasol a Chynnal Plant yn uniongyrchol, cyn dyddiad y "
                           + "gwrandawiad, trwy e-bostio EMAIL ".replace("EMAIL", emailContact)
                           + "fel y gellir gwneud trefniadau. Dylai'r manylion canlynol gael eu "
                           + "cynnwys yn llinell pwnc yr e-bost [OBSERVER/MEDIA] REQUEST");


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
}
