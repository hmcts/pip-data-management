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
class FttResidentialPropertyWeeklyHearingListFileConverterTest {

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
    private static final String OBSERVE_HEARING_ENGLISH = "Observe a court or tribunal hearing as a journalist, "
        + "researcher or member of the public";
    private static final String OBSERVE_HEARING_WELSH = "Arsylwi gwrandawiad llys neu dribiwnlys fel newyddiadurwr, "
        + "ymchwilydd neu aelod o'r cyhoedd";
    private static final String COMMON_WELSH_HEADER_TEXT = "Tribiwnlys Haen Gyntaf (Tribiwnlys Eiddo Preswyl): "
        + "Rhestr o Wrandawiadau Wythnosol rhanbarth ";

    private static final String HEARING_DATE = "16 December 2024";
    private static final String HEARING_VENUE = "This is a venue name";
    private static final String HEARING_CASE_1234 = "1234";
    private static final String HEARING_CASE_1235 = "1235";

    private static final String HEADER_ELEMENT = "page-heading";
    private static final String LIST_DATE_ELEMENT = "list-date";
    private static final String LAST_UPDATED_DATE_ELEMENT = "last-updated-date";
    private static final String CONTACT_MESSAGE_ELEMENT = "contact-message";
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

    private static Stream<Arguments> parametersEnglish() {
        return Stream.of(
            Arguments.of("RPT_EASTERN_WEEKLY_HEARING_LIST", "rptEasternWeeklyHearingList.json",
                         "First-tier Tribunal (Residential Property Tribunal): Eastern region Weekly Hearing List"),
            Arguments.of("RPT_LONDON_WEEKLY_HEARING_LIST", "rptLondonWeeklyHearingList.json",
                         "First-tier Tribunal (Residential Property Tribunal): London region Weekly Hearing List"),
            Arguments.of("RPT_MIDLANDS_WEEKLY_HEARING_LIST", "rptMidlandsWeeklyHearingList.json",
                         "First-tier Tribunal (Residential Property Tribunal): Midlands region Weekly Hearing List"),
            Arguments.of("RPT_NORTHERN_WEEKLY_HEARING_LIST", "rptNorthernWeeklyHearingList.json",
                         "First-tier Tribunal (Residential Property Tribunal): Northern region Weekly Hearing List"),
            Arguments.of("RPT_SOUTHERN_WEEKLY_HEARING_LIST", "rptSouthernWeeklyHearingList.json",
                         "First-tier Tribunal (Residential Property Tribunal): Southern region Weekly Hearing List")
        );
    }

    private static Stream<Arguments> parametersWelsh() {
        return Stream.of(
            Arguments.of("RPT_EASTERN_WEEKLY_HEARING_LIST", "rptEasternWeeklyHearingList.json",
                         COMMON_WELSH_HEADER_TEXT + "Dwyrain Lloegr"),
            Arguments.of("RPT_LONDON_WEEKLY_HEARING_LIST", "rptLondonWeeklyHearingList.json",
                         COMMON_WELSH_HEADER_TEXT + "Llundain"),
            Arguments.of("RPT_MIDLANDS_WEEKLY_HEARING_LIST", "rptMidlandsWeeklyHearingList.json",
                         COMMON_WELSH_HEADER_TEXT + "Canolbarth Lloegr"),
            Arguments.of("RPT_NORTHERN_WEEKLY_HEARING_LIST", "rptNorthernWeeklyHearingList.json",
                         COMMON_WELSH_HEADER_TEXT + "Gogledd Lloegr"),
            Arguments.of("RPT_SOUTHERN_WEEKLY_HEARING_LIST", "rptSouthernWeeklyHearingList.json",
                         COMMON_WELSH_HEADER_TEXT + "De Lloegr")
        );
    }

    @BeforeAll
    void setup() throws IOException {
        try (InputStream inputStream = getClass()
            .getResourceAsStream("/mocks/non-strategic/fttResidentialPropertyTribunalWeeklyHearingList.json")) {
            String inputRaw = IOUtils.toString(inputStream, Charset.defaultCharset());
            listInputJson = new ObjectMapper().readTree(inputRaw);
        }
    }

    @ParameterizedTest
    @MethodSource("parametersEnglish")
    void testFttResidentialPropertyWeeklyHearingListFileConversionInEnglish(String listName,
        String languageFilename, String listDisplayName) throws IOException {
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

        softly.assertThat(document.getElementById(HEADER_ELEMENT).text())
            .as(HEADER_MESSAGE)
            .isEqualTo(listDisplayName);

        softly.assertThat(document.getElementById(LIST_DATE_ELEMENT).text())
            .as(LIST_DATE_MESSAGE)
            .isEqualTo(LIST_DATE_ENGLISH);

        softly.assertThat(document.getElementById(LAST_UPDATED_DATE_ELEMENT).text())
            .as(LAST_UPDATED_DATE_MESSAGE)
            .isEqualTo("Last updated 20 January 2025 at 9:30am");

        softly.assertThat(document.getElementsByClass(SUMMARY_TEXT_CLASS).getFirst().text())
            .as(IMPORTANT_INFORMATION_MESSAGE)
            .isEqualTo("Important information");

        softly.assertThat(document.getElementById(CONTACT_MESSAGE_ELEMENT).text())
            .as(BODY_MESSAGE)
            .isEqualTo("Members of the public wishing to observe a hearing or representatives "
                           + "of the media may, on their request, join any telephone or video hearing "
                           + "remotely while they are taking place by sending an email in advance to "
                           + "the tribunal at [insert office email] with the following details in the "
                           + "subject line “[OBSERVER/MEDIA] REQUEST – [case reference] – [hearing date]” "
                           + "and appropriate arrangements will be made to allow access where reasonably practicable.");

        softly.assertThat(document.getElementById(OBSERVE_HEARING_ELEMENT).text())
            .as(BODY_MESSAGE)
            .isEqualTo(OBSERVE_HEARING_ENGLISH);

        softly.assertThat(document.getElementsByTag("th"))
            .as(TABLE_HEADERS_MESSAGE)
            .hasSize(9)
            .extracting(Element::text)
            .containsExactly(
                "Date",
                "Time",
                "Venue",
                "Case type",
                "Case reference number",
                "Judge(s)",
                "Member(s)",
                "Hearing method",
                "Additional information"
            );

        softly.assertThat(document.getElementsByTag("td"))
            .as(TABLE_CONTENT_MESSAGE)
            .hasSize(18)
            .extracting(Element::text)
            .containsExactly(
                HEARING_DATE,
                "10:15am",
                HEARING_VENUE,
                HEARING_CASE_1234,
                HEARING_CASE_1234,
                "Judge A",
                "Member A",
                "Hearing Method A",
                "This is additional information",
                HEARING_DATE,
                "10:30am",
                HEARING_VENUE,
                HEARING_CASE_1235,
                HEARING_CASE_1235,
                "Judge B",
                "Member B",
                "Hearing Method B",
                "This is another additional information"
            );

        softly.assertAll();
    }

    @ParameterizedTest
    @MethodSource("parametersWelsh")
    void testFttResidentialPropertyWeeklyHearingListFileConversionInWelsh(String listName,
        String languageFilename, String listDisplayName) throws IOException {
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

        softly.assertThat(document.getElementById(HEADER_ELEMENT).text())
            .as(HEADER_MESSAGE)
            .isEqualTo(listDisplayName);

        softly.assertThat(document.getElementById(LIST_DATE_ELEMENT).text())
            .as(LIST_DATE_MESSAGE)
            .isEqualTo(LIST_DATE_WELSH);

        softly.assertThat(document.getElementById(LAST_UPDATED_DATE_ELEMENT).text())
            .as(LAST_UPDATED_DATE_MESSAGE)
            .isEqualTo("Diweddarwyd ddiwethaf 20 January 2025 am 9:30am");

        softly.assertThat(document.getElementsByClass(SUMMARY_TEXT_CLASS).getFirst().text())
            .as(IMPORTANT_INFORMATION_MESSAGE)
            .isEqualTo("Gwybodaeth bwysig");

        softly.assertThat(document.getElementById(CONTACT_MESSAGE_ELEMENT).text())
            .as(BODY_MESSAGE)
            .isEqualTo("Gall aelodau o’r cyhoedd sy’n dymuno arsylwi gwrandawiad neu "
                           + "gynrychiolwyr y cyfryngau ymuno ag unrhyw wrandawiad dros y ffôn "
                           + "neu drwy fideo o bell ar gais tra’u bod yn cael eu cynnal drwy anfon "
                           + "e-bost ymlaen llaw at y tribiwnlys yn [insert office email] gyda’r "
                           + "manylion canlynol yn y llinell bwnc “CAIS [ARSYLLWR/CYFRYNGAU] – "
                           + "[cyfeirnod yr achos] – [dyddiad y gwrandawiad] (angen cynnwys unrhyw "
                           + "wybodaeth arall sy’n ofynnol gan y tribiwnlys)” a gwneir trefniadau priodol i "
                           + "ganiatáu mynediad lle bo hynny’n rhesymol ymarferol.");


        softly.assertThat(document.getElementById(OBSERVE_HEARING_ELEMENT).text())
            .as(BODY_MESSAGE)
            .isEqualTo(OBSERVE_HEARING_WELSH);

        softly.assertThat(document.getElementsByTag("th"))
            .as(TABLE_HEADERS_MESSAGE)
            .hasSize(9)
            .extracting(Element::text)
            .containsExactly(
                "Dyddiad",
                "Amser",
                "Lleoliad",
                "Math o achos",
                "Cyfeirnod yr achos",
                "Barnwr/Barnwyr",
                "Aelod(au)",
                "Math o wrandawiad",
                "Gwybodaeth ychwanegol"
            );

        softly.assertThat(document.getElementsByTag("td"))
            .as(TABLE_CONTENT_MESSAGE)
            .hasSize(18)
            .extracting(Element::text)
            .containsExactly(
                HEARING_DATE,
                "10:15am",
                HEARING_VENUE,
                HEARING_CASE_1234,
                HEARING_CASE_1234,
                "Judge A",
                "Member A",
                "Hearing Method A",
                "This is additional information",
                HEARING_DATE,
                "10:30am",
                HEARING_VENUE,
                HEARING_CASE_1235,
                HEARING_CASE_1235,
                "Judge B",
                "Member B",
                "Hearing Method B",
                "This is another additional information"
            );

        softly.assertAll();
    }
}
