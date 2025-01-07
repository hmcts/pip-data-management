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
class SiacWeeklyHearingListFileConverterTest {
    private static final String CONTENT_DATE = "12 December 2024";
    private static final String PROVENANCE = "provenance";
    private static final String CONTENT_DATE_METADATA = "contentDate";
    private static final String PROVENANCE_METADATA = "provenance";
    private static final String LANGUAGE_METADATA = "language";
    private static final String LIST_TYPE_METADATA = "listType";

    private static final String ENGLISH = "ENGLISH";
    private static final String WELSH = "WELSH";

    private static final String DATE = "Date";
    private static final String TIME = "Time";
    private static final String CASE_REFERENCE_NUMBER = "Case reference number";
    private static final String HEARING_TYPE = "Hearing type";
    private static final String ADDITIONAL_INFORMATION = "Additional information";

    private static final String LIST_DATE_ENGLISH = "List for 12 December 2024";
    private static final String LIST_DATE_WELSH = "Rhestr ar gyfer 12 December 2024";
    private static final String COMING_COURT_OR_TRIBUNAL_ENGLISH =
        "Find out what to expect coming to a court or tribunal";
    private static final String COMING_COURT_OR_TRIBUNAL_WELSH = "Gwybodaeth am beth i’w ddisgwyl"
        + " wrth ddod i lys neu dribiwnlys";

    private static final String HEADER_ELEMENT = "page-heading";
    private static final String LIST_DATE_ELEMENT = "list-date";
    private static final String CONTACT_MESSAGE_ELEMENT_1 = "contact-message-1";
    private static final String CONTACT_MESSAGE_ELEMENT_2 = "contact-message-2";
    private static final String COMING_COURT_OR_TRIBUNAL =  "coming-court-or-tribunal";

    private static final String TITLE_MESSAGE = "Title does not match";
    private static final String HEADER_MESSAGE = "Header does not match";
    private static final String LIST_DATE_MESSAGE = "List date does not match";
    private static final String BODY_MESSAGE = "Body does not match";
    private static final String TABLE_HEADERS_MESSAGE = "Table headers does not match";

    private final NonStrategicListFileConverter converter = new NonStrategicListFileConverter();

    private JsonNode siacInputJson;

    private static Stream<Arguments> parametersEnglish() {
        return Stream.of(
            Arguments.of("SIAC_WEEKLY_HEARING_LIST", "siacWeeklyHearingList.json",
                         "Special Immigration Appeals Commission Weekly Hearing List"),
            Arguments.of("POAC_WEEKLY_HEARING_LIST", "poacWeeklyHearingList.json",
                         "Proscribed Organisations Appeal Commission Weekly Hearing List"),
            Arguments.of("PAAC_WEEKLY_HEARING_LIST", "paacWeeklyHearingList.json",
                         "Pathogens Access Appeal Commission Weekly Hearing List")
        );
    }

    private static Stream<Arguments> parametersWelsh() {
        return Stream.of(
            Arguments.of("SIAC_WEEKLY_HEARING_LIST", "siacWeeklyHearingList.json",
                         "Rhestr o Wrandawiadau Wythnosol y Comisiwn Apeliadau Mewnfudo Arbennig"),
            Arguments.of("POAC_WEEKLY_HEARING_LIST", "poacWeeklyHearingList.json",
                         "Rhestr o Wrandawiadau Wythnosol y Comisiwn Apeliadau Sefydliadau Gwaharddedig"),
            Arguments.of("PAAC_WEEKLY_HEARING_LIST", "paacWeeklyHearingList.json",
                         "Rhestr o Wrandawiadau Wythnosol y Comisiwn Apeliadau Mynediad Pathogenau")
        );
    }

    @BeforeAll
    void setup() throws IOException {
        try (InputStream inputStream = getClass()
            .getResourceAsStream("/mocks/non-strategic/siacWeeklyHearingList.json")) {
            String inputRaw = IOUtils.toString(inputStream, Charset.defaultCharset());
            siacInputJson = new ObjectMapper().readTree(inputRaw);
        }
    }

    @ParameterizedTest
    @MethodSource("parametersEnglish")
    void testSiacWeeklyHearingListFileConversionInEnglish(String listName,
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

        String result = converter.convert(siacInputJson, metadata, languageResource);
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

        softly.assertThat(document.getElementById(CONTACT_MESSAGE_ELEMENT_1))
            .as(BODY_MESSAGE)
            .extracting(Element::text)
            .isEqualTo("The tribunal sometimes uses reference numbers or initials to protect the anonymity "
                           + "of those involved in the appeal.");
        softly.assertThat(document.getElementById(CONTACT_MESSAGE_ELEMENT_2))
            .as(BODY_MESSAGE)
            .extracting(Element::text)
            .isEqualTo("All hearings take place at Field House, 15-25 Bream’s Buildings, London EC4A 1DZ.");

        softly.assertThat(document.getElementById(COMING_COURT_OR_TRIBUNAL))
            .as(BODY_MESSAGE)
            .extracting(Element::text)
            .isEqualTo(COMING_COURT_OR_TRIBUNAL_ENGLISH);

        softly.assertThat(document.getElementsByTag("th"))
            .as(TABLE_HEADERS_MESSAGE)
            .hasSize(7)
            .extracting(Element::text)
            .containsExactly(
                DATE,
                TIME,
                "Appellant",
                CASE_REFERENCE_NUMBER,
                HEARING_TYPE,
                "Courtroom",
                ADDITIONAL_INFORMATION
            );

        softly.assertAll();
    }

    @ParameterizedTest
    @MethodSource("parametersWelsh")
    void testSiacWeeklyHearingListFileConversionInWelsh(String listName,
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

        String result = converter.convert(siacInputJson, metadata, languageResource);
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

        softly.assertThat(document.getElementById(CONTACT_MESSAGE_ELEMENT_1))
            .as(BODY_MESSAGE)
            .extracting(Element::text)
            .isEqualTo("Weithiau bydd y tribiwnlys yn defnyddio cyfeirnodau neu flaenlythrennau "
                           + "i sicrhau bod y rhai sy’n ymwneud â’r apêl yn aros yn ddienw.");

        softly.assertThat(document.getElementById(CONTACT_MESSAGE_ELEMENT_2))
            .as(BODY_MESSAGE)
            .extracting(Element::text)
            .isEqualTo("Cynhelir pob gwrandawiad "
                           + "yn Field House, 15-25 Bream’s Buildings, Llundain EC4A 1DZ.");

        softly.assertThat(document.getElementById(COMING_COURT_OR_TRIBUNAL))
            .as(BODY_MESSAGE)
            .extracting(Element::text)
            .isEqualTo(COMING_COURT_OR_TRIBUNAL_WELSH);

        softly.assertThat(document.getElementsByTag("th"))
            .as(TABLE_HEADERS_MESSAGE)
            .hasSize(7)
            .extracting(Element::text)
            .containsExactly(
                "Dyddiad",
                "Amser",
                "Apelydd",
                "Cyfeirnod yr achos",
                "Math o wrandawiad",
                "Ystafell llys",
                "Gwybodaeth ychwanegol"
            );

        softly.assertAll();
    }
}
