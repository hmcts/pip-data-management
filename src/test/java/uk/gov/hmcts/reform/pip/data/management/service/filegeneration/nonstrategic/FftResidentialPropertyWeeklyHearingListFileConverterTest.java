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
class FftResidentialPropertyWeeklyHearingListFileConverterTest {

    private static final String CONTENT_DATE = "12 December 2024";
    private static final String PROVENANCE = "provenance";
    private static final String CONTENT_DATE_METADATA = "contentDate";
    private static final String PROVENANCE_METADATA = "provenance";
    private static final String LANGUAGE_METADATA = "language";
    private static final String LIST_TYPE_METADATA = "listType";

    private static final String ENGLISH = "ENGLISH";
    private static final String WELSH = "WELSH";

    private static final String LIST_DATE_ENGLISH = "List for 12 December 2024";
    private static final String LIST_DATE_WELSH = "Rhestr ar gyfer 12 December 2024";
    private static final String OBSERVE_HEARING_ENGLISH = "Observe a court or tribunal hearing as a journalist, "
        + "researcher or member of the public";
    private static final String OBSERVE_HEARING_WELSH = "Arsylwi gwrandawiad llys neu dribiwnlys fel newyddiadurwr, "
        + "ymchwilydd neu aelod o'r cyhoedd";

    private static final String HEADER_ELEMENT = "page-heading";
    private static final String LIST_DATE_ELEMENT = "list-date";
    private static final String CONTACT_MESSAGE_ELEMENT = "contact-message";
    private static final String OBSERVE_HEARING_ELEMENT =  "observe-hearing";

    private static final String TITLE_MESSAGE = "Title does not match";
    private static final String HEADER_MESSAGE = "Header does not match";
    private static final String LIST_DATE_MESSAGE = "List date does not match";
    private static final String BODY_MESSAGE = "Body does not match";
    private static final String TABLE_HEADERS_MESSAGE = "Table headers does not match";

    private final NonStrategicListFileConverter converter = new NonStrategicListFileConverter();

    private JsonNode listInputJson;

    private static Stream<Arguments> parametersEnglish() {
        return Stream.of(
            Arguments.of("RPT_EASTERN_WEEKLY_HEARING_LIST", "rptEasternWeeklyHearingList.json",
                         "Residential Property Tribunal: Eastern region Weekly Hearing List"),
            Arguments.of("RPT_LONDON_WEEKLY_HEARING_LIST", "rptLondonWeeklyHearingList.json",
                         "Residential Property Tribunal: London region Weekly Hearing List"),
            Arguments.of("RPT_MIDLANDS_WEEKLY_HEARING_LIST", "rptMidlandsWeeklyHearingList.json",
                         "Residential Property Tribunal: Midlands region Weekly Hearing List"),
            Arguments.of("RPT_NORTHERN_WEEKLY_HEARING_LIST", "rptNorthernWeeklyHearingList.json",
                         "Residential Property Tribunal: Northern region Weekly Hearing List"),
            Arguments.of("RPT_SOUTHERN_WEEKLY_HEARING_LIST", "rptSouthernWeeklyHearingList.json",
                         "Residential Property Tribunal: Southern region Weekly Hearing List")
        );
    }

    private static Stream<Arguments> parametersWelsh() {
        return Stream.of(
            Arguments.of("RPT_EASTERN_WEEKLY_HEARING_LIST", "rptEasternWeeklyHearingList.json",
                         "Tribiwnlys Eiddo Preswyl: Rhestr o Wrandawiadau Wythnosol rhanbarth Dwyrain Lloegr"),
            Arguments.of("RPT_LONDON_WEEKLY_HEARING_LIST", "rptLondonWeeklyHearingList.json",
                         "Tribiwnlys Eiddo Preswyl: Rhestr o Wrandawiadau Wythnosol rhanbarth Llundain"),
            Arguments.of("RPT_MIDLANDS_WEEKLY_HEARING_LIST", "rptMidlandsWeeklyHearingList.json",
                         "Tribiwnlys Eiddo Preswyl: Rhestr o Wrandawiadau Wythnosol rhanbarth Canolbarth Lloegr"),
            Arguments.of("RPT_NORTHERN_WEEKLY_HEARING_LIST", "rptNorthernWeeklyHearingList.json",
                         "Tribiwnlys Eiddo Preswyl: Rhestr o Wrandawiadau Wythnosol rhanbarth Gogledd Lloegr"),
            Arguments.of("RPT_SOUTHERN_WEEKLY_HEARING_LIST", "rptSouthernWeeklyHearingList.json",
                         "Tribiwnlys Eiddo Preswyl: Rhestr o Wrandawiadau Wythnosol rhanbarth De Lloegr")
        );
    }

    @BeforeAll
    void setup() throws IOException {
        try (InputStream inputStream = getClass()
            .getResourceAsStream("/mocks/non-strategic/fftResidentialPropertyTribunalWeeklyHearingList.json")) {
            String inputRaw = IOUtils.toString(inputStream, Charset.defaultCharset());
            listInputJson = new ObjectMapper().readTree(inputRaw);
        }
    }

    @ParameterizedTest
    @MethodSource("parametersEnglish")
    void testFftResidentialPropertyWeeklyHearingListFileConversionInEnglish(String listName,
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
                                              LIST_TYPE_METADATA, listName
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

        softly.assertThat(document.getElementById(CONTACT_MESSAGE_ELEMENT))
            .as(BODY_MESSAGE)
            .extracting(Element::text)
            .isEqualTo("Members of the public wishing to observe a hearing or representatives "
                           + "of the media may, on their request, join any telephone or video hearing "
                           + "remotely while they are taking place by sending an email in advance to "
                           + "the tribunal at [insert office email] with the following details in the "
                           + "subject line “[OBSERVER/MEDIA] REQUEST – [case reference] – [hearing date]” "
                           + "and appropriate arrangements will be made to allow access where reasonably practicable.");

        softly.assertThat(document.getElementById(OBSERVE_HEARING_ELEMENT))
            .as(BODY_MESSAGE)
            .extracting(Element::text)
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

        softly.assertAll();
    }

    @ParameterizedTest
    @MethodSource("parametersWelsh")
    void testFftResidentialPropertyWeeklyHearingListFileConversionInWelsh(String listName,
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
                                              LIST_TYPE_METADATA, listName
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

        softly.assertThat(document.getElementById(CONTACT_MESSAGE_ELEMENT))
            .as(BODY_MESSAGE)
            .extracting(Element::text)
            .isEqualTo("Gall aelodau o’r cyhoedd sy’n dymuno arsylwi gwrandawiad neu "
                           + "gynrychiolwyr y cyfryngau ymuno ag unrhyw wrandawiad dros y ffôn "
                           + "neu drwy fideo o bell ar gais tra’u bod yn cael eu cynnal drwy anfon "
                           + "e-bost ymlaen llaw at y tribiwnlys yn [insert office email] gyda’r "
                           + "manylion canlynol yn y llinell bwnc “CAIS [ARSYLLWR/CYFRYNGAU] – "
                           + "[cyfeirnod yr achos] – [dyddiad y gwrandawiad] (angen cynnwys unrhyw "
                           + "wybodaeth arall sy’n ofynnol gan y tribiwnlys)” a gwneir trefniadau priodol i "
                           + "ganiatáu mynediad lle bo hynny’n rhesymol ymarferol.");


        softly.assertThat(document.getElementById(OBSERVE_HEARING_ELEMENT))
            .as(BODY_MESSAGE)
            .extracting(Element::text)
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

        softly.assertAll();
    }
}
