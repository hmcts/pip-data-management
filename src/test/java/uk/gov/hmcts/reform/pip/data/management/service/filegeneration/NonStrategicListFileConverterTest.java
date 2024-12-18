package uk.gov.hmcts.reform.pip.data.management.service.filegeneration;

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
import uk.gov.hmcts.reform.pip.model.publication.ListType;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.Map;
import java.util.Objects;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class NonStrategicListFileConverterTest {
    private static final String CONTENT_DATE = "12 December 2024";
    private static final String PROVENANCE = "provenance";
    private static final String CONTENT_DATE_METADATA = "contentDate";
    private static final String PROVENANCE_METADATA = "provenance";
    private static final String LANGUAGE_METADATA = "language";
    private static final String LIST_TYPE_METADATA = "listType";

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

    private JsonNode cstInputJson;
    private JsonNode phtInputJson;

    @BeforeAll
    void setup() throws IOException {
        try (InputStream inputStream = getClass()
            .getResourceAsStream("/mocks/non-strategic/cstWeeklyHearingList.json")) {
            String inputRaw = IOUtils.toString(inputStream, Charset.defaultCharset());
            cstInputJson = new ObjectMapper().readTree(inputRaw);
        }

        try (InputStream inputStream = getClass()
            .getResourceAsStream("/mocks/non-strategic/phtWeeklyHearingList.json")) {
            String inputRaw = IOUtils.toString(inputStream, Charset.defaultCharset());
            phtInputJson = new ObjectMapper().readTree(inputRaw);
        }
    }

    @Test
    void testCstWeeklyHearingListFileConversionInEnglish() throws IOException {
        Map<String, Object> languageResource;
        try (InputStream languageFile = Thread.currentThread()
            .getContextClassLoader()
            .getResourceAsStream("templates/languages/en/non-strategic/cstWeeklyHearingList.json")) {
            languageResource = new ObjectMapper().readValue(
                Objects.requireNonNull(languageFile).readAllBytes(), new TypeReference<>() {
                });
        }

        Map<String, String> metadata = Map.of(CONTENT_DATE_METADATA, CONTENT_DATE,
                                              PROVENANCE_METADATA, PROVENANCE,
                                              LANGUAGE_METADATA, "ENGLISH",
                                              LIST_TYPE_METADATA, ListType.CST_WEEKLY_HEARING_LIST.name()
        );

        String result = converter.convert(cstInputJson, metadata, languageResource);
        Document document = Jsoup.parse(result);

        SoftAssertions softly = new SoftAssertions();

        softly.assertThat(document.title())
            .as(TITLE_MESSAGE)
            .isEqualTo("Care Standards Tribunal Weekly Hearing List");

        softly.assertThat(document.getElementById(HEADER_ELEMENT))
            .as(HEADER_MESSAGE)
            .extracting(Element::text)
            .isEqualTo("Care Standards Tribunal Weekly Hearing List");

        softly.assertThat(document.getElementById(LIST_DATE_ELEMENT))
            .as(LIST_DATE_MESSAGE)
            .extracting(Element::text)
            .isEqualTo("List for 12 December 2024");

        softly.assertThat(document.getElementById(CONTACT_MESSAGE_ELEMENT))
            .as(BODY_MESSAGE)
            .extracting(Element::text)
            .isEqualTo("Please contact the Care Standards Office at cst@justice.gov.uk for details of how to "
                           + "access video hearings.");

        softly.assertThat(document.getElementById(OBSERVE_HEARING_ELEMENT))
            .as(BODY_MESSAGE)
            .extracting(Element::text)
            .isEqualTo("Observe a court or tribunal hearing as a journalist, researcher or member of the public.");

        softly.assertThat(document.getElementsByTag("th"))
            .as(TABLE_HEADERS_MESSAGE)
            .hasSize(6)
            .extracting(Element::text)
            .containsExactly(
                "Date",
                "Case name",
                "Hearing length",
                "Hearing type",
                "Venue",
                "Additional information"
            );

        softly.assertAll();
    }

    @Test
    void testCstWeeklyHearingListFileConversionInWelsh() throws IOException {
        Map<String, Object> languageResource;
        try (InputStream languageFile = Thread.currentThread()
            .getContextClassLoader()
            .getResourceAsStream("templates/languages/cy/non-strategic/cstWeeklyHearingList.json")) {
            languageResource = new ObjectMapper().readValue(
                Objects.requireNonNull(languageFile).readAllBytes(), new TypeReference<>() {
                });
        }

        Map<String, String> metadata = Map.of(CONTENT_DATE_METADATA, CONTENT_DATE,
                                              PROVENANCE_METADATA, PROVENANCE,
                                              LANGUAGE_METADATA, "WELSH",
                                              LIST_TYPE_METADATA, ListType.CST_WEEKLY_HEARING_LIST.name()
        );

        String result = converter.convert(cstInputJson, metadata, languageResource);
        Document document = Jsoup.parse(result);

        SoftAssertions softly = new SoftAssertions();

        softly.assertThat(document.title())
            .as(TITLE_MESSAGE)
            .isEqualTo("Rhestr Gwrandawiadau Wythnosol y Tribiwnlys Safonau Gofal");

        softly.assertThat(document.getElementById(HEADER_ELEMENT))
            .as(HEADER_MESSAGE)
            .extracting(Element::text)
            .isEqualTo("Rhestr Gwrandawiadau Wythnosol y Tribiwnlys Safonau Gofal");

        softly.assertThat(document.getElementById(LIST_DATE_ELEMENT))
            .as(LIST_DATE_MESSAGE)
            .extracting(Element::text)
            .isEqualTo("Rhestr ar gyfer 12 December 2024");

        softly.assertThat(document.getElementById(CONTACT_MESSAGE_ELEMENT))
            .as(BODY_MESSAGE)
            .extracting(Element::text)
            .isEqualTo("Cysylltwch â'r Swyddfa Safonau Gofal yn cst@justice.gov.uk i gael manylion am sut i gael "
                           + "mynediad at wrandawiadau fideo.");

        softly.assertThat(document.getElementById(OBSERVE_HEARING_ELEMENT))
            .as(BODY_MESSAGE)
            .extracting(Element::text)
            .isEqualTo("Arsylwi gwrandawiad llys neu dribiwnlys fel newyddiadurwr, ymchwilydd neu aelod o'r cyhoedd.");

        softly.assertThat(document.getElementsByTag("th"))
            .as(TABLE_HEADERS_MESSAGE)
            .hasSize(6)
            .extracting(Element::text)
            .containsExactly(
                "Dyddiad",
                "Enw’r achos",
                "Hyd y gwrandawiad",
                "Math o wrandawiad",
                "Lleoliad",
                "Gwybodaeth ychwanegol"
            );

        softly.assertAll();
    }

    @Test
    void testPhtWeeklyHearingListFileConversionInEnglish() throws IOException {
        Map<String, Object> languageResource;
        try (InputStream languageFile = Thread.currentThread()
            .getContextClassLoader()
            .getResourceAsStream("templates/languages/en/non-strategic/phtWeeklyHearingList.json")) {
            languageResource = new ObjectMapper().readValue(
                Objects.requireNonNull(languageFile).readAllBytes(), new TypeReference<>() {
                });
        }

        Map<String, String> metadata = Map.of(CONTENT_DATE_METADATA, CONTENT_DATE,
                                              PROVENANCE_METADATA, PROVENANCE,
                                              LANGUAGE_METADATA, "ENGLISH",
                                              LIST_TYPE_METADATA, ListType.PHT_WEEKLY_HEARING_LIST.name()
        );

        String result = converter.convert(phtInputJson, metadata, languageResource);
        Document document = Jsoup.parse(result);

        SoftAssertions softly = new SoftAssertions();

        softly.assertThat(document.title())
            .as(TITLE_MESSAGE)
            .isEqualTo("Primary Health Tribunal Weekly Hearing List");

        softly.assertThat(document.getElementById(HEADER_ELEMENT))
            .as(HEADER_MESSAGE)
            .extracting(Element::text)
            .isEqualTo("Primary Health Tribunal Weekly Hearing List");

        softly.assertThat(document.getElementById(LIST_DATE_ELEMENT))
            .as(LIST_DATE_MESSAGE)
            .extracting(Element::text)
            .isEqualTo("List for 12 December 2024");

        softly.assertThat(document.getElementById(CONTACT_MESSAGE_ELEMENT))
            .as(BODY_MESSAGE)
            .extracting(Element::text)
            .isEqualTo("Please contact the Primary Health Lists at primaryhealthlists@justice.gov.uk for details of "
                           + "how to access video hearings.");

        softly.assertThat(document.getElementById(OBSERVE_HEARING_ELEMENT))
            .as(BODY_MESSAGE)
            .extracting(Element::text)
            .isEqualTo("Observe a court or tribunal hearing as a journalist, researcher or member of the public.");

        softly.assertThat(document.getElementsByTag("th"))
            .as(TABLE_HEADERS_MESSAGE)
            .hasSize(6)
            .extracting(Element::text)
            .containsExactly(
                "Date",
                "Case name",
                "Hearing length",
                "Hearing type",
                "Venue",
                "Additional information"
            );

        softly.assertAll();
    }

    @Test
    void testPhtWeeklyHearingListFileConversionInWelsh() throws IOException {
        Map<String, Object> languageResource;
        try (InputStream languageFile = Thread.currentThread()
            .getContextClassLoader()
            .getResourceAsStream("templates/languages/cy/non-strategic/phtWeeklyHearingList.json")) {
            languageResource = new ObjectMapper().readValue(
                Objects.requireNonNull(languageFile).readAllBytes(), new TypeReference<>() {
                });
        }

        Map<String, String> metadata = Map.of(CONTENT_DATE_METADATA, CONTENT_DATE,
                                              PROVENANCE_METADATA, PROVENANCE,
                                              LANGUAGE_METADATA, "WELSH",
                                              LIST_TYPE_METADATA, ListType.PHT_WEEKLY_HEARING_LIST.name()
        );

        String result = converter.convert(phtInputJson, metadata, languageResource);
        Document document = Jsoup.parse(result);

        SoftAssertions softly = new SoftAssertions();

        softly.assertThat(document.title())
            .as(TITLE_MESSAGE)
            .isEqualTo("Rhestr Gwrandawiadau Wythnosol y Tribiwnlys Iechyd Sylfaenol");

        softly.assertThat(document.getElementById(HEADER_ELEMENT))
            .as(HEADER_MESSAGE)
            .extracting(Element::text)
            .isEqualTo("Rhestr Gwrandawiadau Wythnosol y Tribiwnlys Iechyd Sylfaenol");

        softly.assertThat(document.getElementById(LIST_DATE_ELEMENT))
            .as(LIST_DATE_MESSAGE)
            .extracting(Element::text)
            .isEqualTo("Rhestr ar gyfer 12 December 2024");

        softly.assertThat(document.getElementById(CONTACT_MESSAGE_ELEMENT))
            .as(BODY_MESSAGE)
            .extracting(Element::text)
            .isEqualTo("Cysylltwch â'r Rhestrau Iechyd Sylfaenol yn primaryhealthlists@justice.gov.uk i gael "
                           + "manylion am sut i gael mynediad at wrandawiadau fideo.");

        softly.assertThat(document.getElementById(OBSERVE_HEARING_ELEMENT))
            .as(BODY_MESSAGE)
            .extracting(Element::text)
            .isEqualTo("Arsylwi gwrandawiad llys neu dribiwnlys fel newyddiadurwr, ymchwilydd neu aelod o'r cyhoedd.");

        softly.assertThat(document.getElementsByTag("th"))
            .as(TABLE_HEADERS_MESSAGE)
            .hasSize(6)
            .extracting(Element::text)
            .containsExactly(
                "Dyddiad",
                "Enw’r achos",
                "Hyd y gwrandawiad",
                "Math o wrandawiad",
                "Lleoliad",
                "Gwybodaeth ychwanegol"
            );

        softly.assertAll();
    }
}
