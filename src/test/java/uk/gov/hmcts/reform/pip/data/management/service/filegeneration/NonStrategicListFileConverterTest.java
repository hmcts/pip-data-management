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

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.Map;
import java.util.Objects;

import static uk.gov.hmcts.reform.pip.model.publication.ListType.CST_WEEKLY_HEARING_LIST;
import static uk.gov.hmcts.reform.pip.model.publication.ListType.GRC_WEEKLY_HEARING_LIST;
import static uk.gov.hmcts.reform.pip.model.publication.ListType.PHT_WEEKLY_HEARING_LIST;
import static uk.gov.hmcts.reform.pip.model.publication.ListType.SIAC_WEEKLY_HEARING_LIST;
import static uk.gov.hmcts.reform.pip.model.publication.ListType.UT_IAC_JUDICIAL_REVIEW_DAILY_HEARING_LIST;
import static uk.gov.hmcts.reform.pip.model.publication.ListType.UT_IAC_STATUTORY_APPEALS_DAILY_HEARING_LIST;
import static uk.gov.hmcts.reform.pip.model.publication.ListType.WPAFCC_WEEKLY_HEARING_LIST;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class NonStrategicListFileConverterTest {
    private static final String CONTENT_DATE = "12 December 2024";
    private static final String PROVENANCE = "provenance";
    private static final String CONTENT_DATE_METADATA = "contentDate";
    private static final String PROVENANCE_METADATA = "provenance";
    private static final String LANGUAGE_METADATA = "language";
    private static final String LIST_TYPE_METADATA = "listType";

    private static final String ENGLISH = "ENGLISH";
    private static final String WELSH = "WELSH";

    private static final String DATE = "Date";
    private static final String DATE_WELSH = "Dyddiad";
    private static final String TIME = "Time";
    private static final String HEARING_TIME = "Hearing time";
    private static final String HEARING_TIME_WELSH = "Amser y gwrandawiad";
    private static final String CASE_NAME = "Case name";
    private static final String CASE_NAME_WELSH = "Enw’r achos";
    private static final String CASE_REFERENCE_NUMBER = "Case reference number";
    private static final String JUDGES = "Judge(s)";
    private static final String JUDGES_WELSH = "Barnwr/Barnwyr";
    private static final String HEARING_TYPE = "Hearing type";
    private static final String HEARING_TYPE_WELSH = "Math o wrandawiad";
    private static final String VENUE = "Venue";
    private static final String VENUE_WELSH = "Lleoliad";
    private static final String ADDITIONAL_INFORMATION = "Additional information";
    private static final String ADDITIONAL_INFORMATION_WELSH = "Gwybodaeth ychwanegol";

    private static final String LIST_DATE_ENGLISH = "List for 12 December 2024";
    private static final String LIST_DATE_WELSH = "Rhestr ar gyfer 12 December 2024";
    private static final String OBSERVE_HEARING_ENGLISH = "Observe a court or tribunal hearing as a journalist, "
        + "researcher or member of the public";
    private static final String OBSERVE_HEARING_WELSH = "Arsylwi gwrandawiad llys neu dribiwnlys fel newyddiadurwr, "
        + "ymchwilydd neu aelod o'r cyhoedd";
    private static final String CST_LIST_WELSH_NAME = "Rhestr Gwrandawiadau Wythnosol y Tribiwnlys Safonau Gofal";

    private static final String HEADER_ELEMENT = "page-heading";
    private static final String LIST_DATE_ELEMENT = "list-date";
    private static final String CONTACT_MESSAGE_ELEMENT = "contact-message";
    private static final String CONTACT_MESSAGE_ELEMENT_1 = "contact-message-1";
    private static final String CONTACT_MESSAGE_ELEMENT_2 = "contact-message-2";
    private static final String OBSERVE_HEARING_ELEMENT =  "observe-hearing";
    private static final String MESSAGE_LINE1_ELEMENT =  "message-line-1";
    private static final String JOIN_HEARING_ELEMENT =  "join-hearing";
    private static final String JOIN_HEARING_MESSAGE_ELEMENT =  "join-hearing-message";
    private static final String LIST_UPDATE_MESSAGE_ELEMENT = "list-update-message";
    private static final String ATTEND_HEARING_MESSAGE_ELEMENT = "attend-hearing-message";

    private static final String TITLE_MESSAGE = "Title does not match";
    private static final String HEADER_MESSAGE = "Header does not match";
    private static final String LIST_DATE_MESSAGE = "List date does not match";
    private static final String BODY_MESSAGE = "Body does not match";
    private static final String TABLE_HEADERS_MESSAGE = "Table headers does not match";

    private final NonStrategicListFileConverter converter = new NonStrategicListFileConverter();

    private JsonNode cstInputJson;
    private JsonNode phtInputJson;
    private JsonNode grcInputJson;
    private JsonNode wpafccInputJson;
    private JsonNode utIacJudicialReviewInputJson;
    private JsonNode utIacStatutoryAppealsInputJson;
    private JsonNode siacInputJson;

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

        try (InputStream inputStream = getClass()
            .getResourceAsStream("/mocks/non-strategic/grcWeeklyHearingList.json")) {
            String inputRaw = IOUtils.toString(inputStream, Charset.defaultCharset());
            grcInputJson = new ObjectMapper().readTree(inputRaw);
        }

        try (InputStream inputStream = getClass()
            .getResourceAsStream("/mocks/non-strategic/wpafccWeeklyHearingList.json")) {
            String inputRaw = IOUtils.toString(inputStream, Charset.defaultCharset());
            wpafccInputJson = new ObjectMapper().readTree(inputRaw);
        }

        try (InputStream inputStream = getClass()
            .getResourceAsStream("/mocks/non-strategic/utIacJudicialReviewDailyHearingList.json")) {
            String inputRaw = IOUtils.toString(inputStream, Charset.defaultCharset());
            utIacJudicialReviewInputJson = new ObjectMapper().readTree(inputRaw);
        }

        try (InputStream inputStream = getClass()
            .getResourceAsStream("/mocks/non-strategic/utIacStatutoryAppealsDailyHearingList.json")) {
            String inputRaw = IOUtils.toString(inputStream, Charset.defaultCharset());
            utIacStatutoryAppealsInputJson = new ObjectMapper().readTree(inputRaw);
        }

        try (InputStream inputStream = getClass()
            .getResourceAsStream("/mocks/non-strategic/siacWeeklyHearingList.json")) {
            String inputRaw = IOUtils.toString(inputStream, Charset.defaultCharset());
            siacInputJson = new ObjectMapper().readTree(inputRaw);
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
                                              LANGUAGE_METADATA, ENGLISH,
                                              LIST_TYPE_METADATA, CST_WEEKLY_HEARING_LIST.name()
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
            .isEqualTo(LIST_DATE_ENGLISH);

        softly.assertThat(document.getElementById(CONTACT_MESSAGE_ELEMENT))
            .as(BODY_MESSAGE)
            .extracting(Element::text)
            .isEqualTo("Please contact the Care Standards Office at cst@justice.gov.uk for details of how to "
                           + "access video hearings.");

        softly.assertThat(document.getElementById(OBSERVE_HEARING_ELEMENT))
            .as(BODY_MESSAGE)
            .extracting(Element::text)
            .isEqualTo(OBSERVE_HEARING_ENGLISH);

        softly.assertThat(document.getElementsByTag("th"))
            .as(TABLE_HEADERS_MESSAGE)
            .hasSize(6)
            .extracting(Element::text)
            .containsExactly(
                DATE,
                CASE_NAME,
                "Hearing length",
                HEARING_TYPE,
                VENUE,
                ADDITIONAL_INFORMATION
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
                                              LANGUAGE_METADATA, WELSH,
                                              LIST_TYPE_METADATA, CST_WEEKLY_HEARING_LIST.name()
        );

        String result = converter.convert(cstInputJson, metadata, languageResource);
        Document document = Jsoup.parse(result);

        SoftAssertions softly = new SoftAssertions();

        softly.assertThat(document.title())
            .as(TITLE_MESSAGE)
            .isEqualTo(CST_LIST_WELSH_NAME);

        softly.assertThat(document.getElementById(HEADER_ELEMENT))
            .as(HEADER_MESSAGE)
            .extracting(Element::text)
            .isEqualTo(CST_LIST_WELSH_NAME);

        softly.assertThat(document.getElementById(LIST_DATE_ELEMENT))
            .as(LIST_DATE_MESSAGE)
            .extracting(Element::text)
            .isEqualTo(LIST_DATE_WELSH);

        softly.assertThat(document.getElementById(CONTACT_MESSAGE_ELEMENT))
            .as(BODY_MESSAGE)
            .extracting(Element::text)
            .isEqualTo("Cysylltwch â'r Swyddfa Safonau Gofal yn cst@justice.gov.uk i gael manylion am sut i gael "
                           + "mynediad at wrandawiadau fideo.");

        softly.assertThat(document.getElementById(OBSERVE_HEARING_ELEMENT))
            .as(BODY_MESSAGE)
            .extracting(Element::text)
            .isEqualTo(OBSERVE_HEARING_WELSH);

        softly.assertThat(document.getElementsByTag("th"))
            .as(TABLE_HEADERS_MESSAGE)
            .hasSize(6)
            .extracting(Element::text)
            .containsExactly(
                DATE_WELSH,
                CASE_NAME_WELSH,
                "Hyd y gwrandawiad",
                HEARING_TYPE_WELSH,
                VENUE_WELSH,
                ADDITIONAL_INFORMATION_WELSH
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
                                              LANGUAGE_METADATA, ENGLISH,
                                              LIST_TYPE_METADATA, PHT_WEEKLY_HEARING_LIST.name()
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
            .isEqualTo(LIST_DATE_ENGLISH);

        softly.assertThat(document.getElementById(CONTACT_MESSAGE_ELEMENT))
            .as(BODY_MESSAGE)
            .extracting(Element::text)
            .isEqualTo("Please contact the Primary Health Lists at primaryhealthlists@justice.gov.uk for details of "
                           + "how to access video hearings.");

        softly.assertThat(document.getElementById(OBSERVE_HEARING_ELEMENT))
            .as(BODY_MESSAGE)
            .extracting(Element::text)
            .isEqualTo(OBSERVE_HEARING_ENGLISH);

        softly.assertThat(document.getElementsByTag("th"))
            .as(TABLE_HEADERS_MESSAGE)
            .hasSize(6)
            .extracting(Element::text)
            .containsExactly(
                DATE,
                CASE_NAME,
                "Hearing length",
                HEARING_TYPE,
                VENUE,
                ADDITIONAL_INFORMATION
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
                                              LANGUAGE_METADATA, WELSH,
                                              LIST_TYPE_METADATA, PHT_WEEKLY_HEARING_LIST.name()
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
            .isEqualTo(LIST_DATE_WELSH);

        softly.assertThat(document.getElementById(CONTACT_MESSAGE_ELEMENT))
            .as(BODY_MESSAGE)
            .extracting(Element::text)
            .isEqualTo("Cysylltwch â'r Rhestrau Iechyd Sylfaenol yn primaryhealthlists@justice.gov.uk i gael "
                           + "manylion am sut i gael mynediad at wrandawiadau fideo.");

        softly.assertThat(document.getElementById(OBSERVE_HEARING_ELEMENT))
            .as(BODY_MESSAGE)
            .extracting(Element::text)
            .isEqualTo(OBSERVE_HEARING_WELSH);

        softly.assertThat(document.getElementsByTag("th"))
            .as(TABLE_HEADERS_MESSAGE)
            .hasSize(6)
            .extracting(Element::text)
            .containsExactly(
                DATE_WELSH,
                CASE_NAME_WELSH,
                "Hyd y gwrandawiad",
                HEARING_TYPE_WELSH,
                VENUE_WELSH,
                ADDITIONAL_INFORMATION_WELSH
            );

        softly.assertAll();
    }

    @Test
    void testGrcWeeklyHearingListFileConversionInEnglish() throws IOException {
        Map<String, Object> languageResource;
        try (InputStream languageFile = Thread.currentThread()
            .getContextClassLoader()
            .getResourceAsStream("templates/languages/en/non-strategic/grcWeeklyHearingList.json")) {
            languageResource = new ObjectMapper().readValue(
                Objects.requireNonNull(languageFile).readAllBytes(), new TypeReference<>() {
                });
        }

        Map<String, String> metadata = Map.of(CONTENT_DATE_METADATA, CONTENT_DATE,
                                              PROVENANCE_METADATA, PROVENANCE,
                                              LANGUAGE_METADATA, ENGLISH,
                                              LIST_TYPE_METADATA, GRC_WEEKLY_HEARING_LIST.name()
        );

        String result = converter.convert(grcInputJson, metadata, languageResource);
        Document document = Jsoup.parse(result);

        SoftAssertions softly = new SoftAssertions();

        softly.assertThat(document.title())
            .as(TITLE_MESSAGE)
            .isEqualTo("General Regulatory Chamber Weekly Hearing List");

        softly.assertThat(document.getElementById(HEADER_ELEMENT))
            .as(HEADER_MESSAGE)
            .extracting(Element::text)
            .isEqualTo("General Regulatory Chamber Weekly Hearing List");

        softly.assertThat(document.getElementById(LIST_DATE_ELEMENT))
            .as(LIST_DATE_MESSAGE)
            .extracting(Element::text)
            .isEqualTo(LIST_DATE_ENGLISH);


        softly.assertThat(document.getElementById(MESSAGE_LINE1_ELEMENT))
            .as(BODY_MESSAGE)
            .extracting(Element::text)
            .asString()
            .contains("Parties and representatives will be informed about arrangements for hearing cases remotely");

        softly.assertThat(document.getElementById(OBSERVE_HEARING_ELEMENT))
            .as(BODY_MESSAGE)
            .extracting(Element::text)
            .isEqualTo(OBSERVE_HEARING_ENGLISH);

        softly.assertThat(document.getElementById(JOIN_HEARING_ELEMENT))
            .as(BODY_MESSAGE)
            .extracting(Element::text)
            .isEqualTo("What to expect when joining a telephone or video hearing");

        softly.assertThat(document.getElementsByTag("th"))
            .as(TABLE_HEADERS_MESSAGE)
            .hasSize(9)
            .extracting(Element::text)
            .containsExactly(
                DATE,
                HEARING_TIME,
                CASE_REFERENCE_NUMBER,
                CASE_NAME,
                JUDGES,
                "Member(s)",
                "Mode of hearing",
                VENUE,
                ADDITIONAL_INFORMATION
            );

        softly.assertAll();
    }

    @Test
    void testGrcWeeklyHearingListFileConversionInWelsh() throws IOException {
        Map<String, Object> languageResource;
        try (InputStream languageFile = Thread.currentThread()
            .getContextClassLoader()
            .getResourceAsStream("templates/languages/cy/non-strategic/grcWeeklyHearingList.json")) {
            languageResource = new ObjectMapper().readValue(
                Objects.requireNonNull(languageFile).readAllBytes(), new TypeReference<>() {
                });
        }

        Map<String, String> metadata = Map.of(CONTENT_DATE_METADATA, CONTENT_DATE,
                                              PROVENANCE_METADATA, PROVENANCE,
                                              LANGUAGE_METADATA, WELSH,
                                              LIST_TYPE_METADATA, GRC_WEEKLY_HEARING_LIST.name()
        );

        String result = converter.convert(grcInputJson, metadata, languageResource);
        Document document = Jsoup.parse(result);

        SoftAssertions softly = new SoftAssertions();

        softly.assertThat(document.title())
            .as(TITLE_MESSAGE)
            .isEqualTo("Rhestr o Wrandawiadau Wythnosol y Siambr Rheoleiddio Cyffredinol");

        softly.assertThat(document.getElementById(HEADER_ELEMENT))
            .as(HEADER_MESSAGE)
            .extracting(Element::text)
            .isEqualTo("Rhestr o Wrandawiadau Wythnosol y Siambr Rheoleiddio Cyffredinol");

        softly.assertThat(document.getElementById(LIST_DATE_ELEMENT))
            .as(LIST_DATE_MESSAGE)
            .extracting(Element::text)
            .isEqualTo(LIST_DATE_WELSH);

        softly.assertThat(document.getElementById(MESSAGE_LINE1_ELEMENT))
            .as(BODY_MESSAGE)
            .extracting(Element::text)
            .asString()
            .contains("Bydd partïon a chynrychiolwyr yn cael gwybod ynghylch y trefniadau ar gyfer gwrando achosion "
                          + "o bell.");


        softly.assertThat(document.getElementById(OBSERVE_HEARING_ELEMENT))
            .as(BODY_MESSAGE)
            .extracting(Element::text)
            .isEqualTo(OBSERVE_HEARING_WELSH);

        softly.assertThat(document.getElementById(JOIN_HEARING_ELEMENT))
            .as(BODY_MESSAGE)
            .extracting(Element::text)
            .isEqualTo("Beth i’w ddisgwyl pan fyddwch yn ymuno â gwrandawiad dros y ffôn neu drwy fideo");

        softly.assertThat(document.getElementsByTag("th"))
            .as(TABLE_HEADERS_MESSAGE)
            .hasSize(9)
            .extracting(Element::text)
            .containsExactly(
                DATE_WELSH,
                HEARING_TIME_WELSH,
                "Cyfeirnod yr achos",
                CASE_NAME_WELSH,
                JUDGES_WELSH,
                "Aelod(au)",
                HEARING_TYPE_WELSH,
                VENUE_WELSH,
                ADDITIONAL_INFORMATION_WELSH
            );

        softly.assertAll();
    }

    @Test
    void testWpafccWeeklyHearingListFileConversionInEnglish() throws IOException {
        Map<String, Object> languageResource;
        try (InputStream languageFile = Thread.currentThread()
            .getContextClassLoader()
            .getResourceAsStream("templates/languages/en/non-strategic/wpafccWeeklyHearingList.json")) {
            languageResource = new ObjectMapper().readValue(
                Objects.requireNonNull(languageFile).readAllBytes(), new TypeReference<>() {
                });
        }

        Map<String, String> metadata = Map.of(CONTENT_DATE_METADATA, CONTENT_DATE,
                                              PROVENANCE_METADATA, PROVENANCE,
                                              LANGUAGE_METADATA, ENGLISH,
                                              LIST_TYPE_METADATA, WPAFCC_WEEKLY_HEARING_LIST.name()
        );

        String result = converter.convert(wpafccInputJson, metadata, languageResource);
        Document document = Jsoup.parse(result);

        SoftAssertions softly = new SoftAssertions();

        softly.assertThat(document.title())
            .as(TITLE_MESSAGE)
            .isEqualTo("First-tier Tribunal (War Pensions and Armed Forces Compensation) Weekly Hearing List");

        softly.assertThat(document.getElementById(HEADER_ELEMENT))
            .as(HEADER_MESSAGE)
            .extracting(Element::text)
            .isEqualTo("First-tier Tribunal (War Pensions and Armed Forces Compensation) Weekly Hearing List");

        softly.assertThat(document.getElementById(LIST_DATE_ELEMENT))
            .as(LIST_DATE_MESSAGE)
            .extracting(Element::text)
            .isEqualTo(LIST_DATE_ENGLISH);

        softly.assertThat(document.getElementById(JOIN_HEARING_MESSAGE_ELEMENT))
            .as(BODY_MESSAGE)
            .extracting(Element::text)
            .asString()
            .contains("Members of the public wishing to observe a hearing or representatives of the media may, "
                           + "on their request, join any telephone or video hearing remotely");

        softly.assertThat(document.getElementById(OBSERVE_HEARING_ELEMENT))
            .as(BODY_MESSAGE)
            .extracting(Element::text)
            .isEqualTo(OBSERVE_HEARING_ENGLISH);

        softly.assertThat(document.getElementsByTag("th"))
            .as(TABLE_HEADERS_MESSAGE)
            .hasSize(9)
            .extracting(Element::text)
            .containsExactly(
                DATE,
                HEARING_TIME,
                CASE_REFERENCE_NUMBER,
                CASE_NAME,
                JUDGES,
                "Member(s)",
                "Mode of hearing",
                VENUE,
                ADDITIONAL_INFORMATION
            );

        softly.assertAll();
    }

    @Test
    void testWpafccWeeklyHearingListFileConversionInWelsh() throws IOException {
        Map<String, Object> languageResource;
        try (InputStream languageFile = Thread.currentThread()
            .getContextClassLoader()
            .getResourceAsStream("templates/languages/cy/non-strategic/wpafccWeeklyHearingList.json")) {
            languageResource = new ObjectMapper().readValue(
                Objects.requireNonNull(languageFile).readAllBytes(), new TypeReference<>() {
                });
        }

        Map<String, String> metadata = Map.of(CONTENT_DATE_METADATA, CONTENT_DATE,
                                              PROVENANCE_METADATA, PROVENANCE,
                                              LANGUAGE_METADATA, WELSH,
                                              LIST_TYPE_METADATA, WPAFCC_WEEKLY_HEARING_LIST.name()
        );

        String result = converter.convert(wpafccInputJson, metadata, languageResource);
        Document document = Jsoup.parse(result);

        SoftAssertions softly = new SoftAssertions();

        softly.assertThat(document.title())
            .as(TITLE_MESSAGE)
            .isEqualTo("Tribiwnlys Haen Gyntaf (Iawndal Pensiynau Rhyfel a’r Lluoedd Arfog) - Rhestr o "
                           + "Wrandawiadau Wythnosol");

        softly.assertThat(document.getElementById(HEADER_ELEMENT))
            .as(HEADER_MESSAGE)
            .extracting(Element::text)
            .isEqualTo("Tribiwnlys Haen Gyntaf (Iawndal Pensiynau Rhyfel a’r Lluoedd Arfog) - Rhestr o "
                           + "Wrandawiadau Wythnosol");

        softly.assertThat(document.getElementById(LIST_DATE_ELEMENT))
            .as(LIST_DATE_MESSAGE)
            .extracting(Element::text)
            .isEqualTo(LIST_DATE_WELSH);

        softly.assertThat(document.getElementById(JOIN_HEARING_MESSAGE_ELEMENT))
            .as(BODY_MESSAGE)
            .extracting(Element::text)
            .asString()
            .contains("Gall aelodau o’r cyhoedd sy’n dymuno arsylwi gwrandawiad neu gynrychiolwyr y cyfryngau ymuno "
                          + "ag unrhyw wrandawiad dros y ffôn neu drwy fideo o bell");

        softly.assertThat(document.getElementById(OBSERVE_HEARING_ELEMENT))
            .as(BODY_MESSAGE)
            .extracting(Element::text)
            .isEqualTo(OBSERVE_HEARING_WELSH);

        softly.assertThat(document.getElementsByTag("th"))
            .as(TABLE_HEADERS_MESSAGE)
            .hasSize(9)
            .extracting(Element::text)
            .containsExactly(
                DATE_WELSH,
                HEARING_TIME_WELSH,
                "Cyfeirnod yr achos",
                CASE_NAME_WELSH,
                JUDGES_WELSH,
                "Aelod(au)",
                HEARING_TYPE_WELSH,
                VENUE_WELSH,
                ADDITIONAL_INFORMATION_WELSH
            );

        softly.assertAll();
    }

    @Test
    void testUtIacJudicialReviewDailyHearingListFileConversionInEnglish() throws IOException {
        Map<String, Object> languageResource;
        try (InputStream languageFile = Thread.currentThread()
            .getContextClassLoader()
            .getResourceAsStream("templates/languages/en/non-strategic/utIacJudicialReviewDailyHearingList.json")) {
            languageResource = new ObjectMapper().readValue(
                Objects.requireNonNull(languageFile).readAllBytes(), new TypeReference<>() {
                });
        }

        Map<String, String> metadata = Map.of(CONTENT_DATE_METADATA, CONTENT_DATE,
                                              PROVENANCE_METADATA, PROVENANCE,
                                              LANGUAGE_METADATA, ENGLISH,
                                              LIST_TYPE_METADATA, UT_IAC_JUDICIAL_REVIEW_DAILY_HEARING_LIST.name()
        );

        String result = converter.convert(utIacJudicialReviewInputJson, metadata, languageResource);
        Document document = Jsoup.parse(result);

        SoftAssertions softly = new SoftAssertions();

        softly.assertThat(document.title())
            .as(TITLE_MESSAGE)
            .isEqualTo("Upper Tribunal (Immigration and Asylum) Chamber Field House - Judicial Review Daily "
                           + "Hearing List");

        softly.assertThat(document.getElementById(HEADER_ELEMENT))
            .as(HEADER_MESSAGE)
            .extracting(Element::text)
            .isEqualTo("Upper Tribunal (Immigration and Asylum) Chamber Field House - Judicial Review Daily "
                           + "Hearing List");

        softly.assertThat(document.getElementById(LIST_DATE_ELEMENT))
            .as(LIST_DATE_MESSAGE)
            .extracting(Element::text)
            .isEqualTo(LIST_DATE_ENGLISH);

        softly.assertThat(document.getElementById(LIST_UPDATE_MESSAGE_ELEMENT))
            .as(BODY_MESSAGE)
            .extracting(Element::text)
            .asString()
            .contains("The following list is subject to change until 4:30pm");

        softly.assertThat(document.getElementById(OBSERVE_HEARING_ELEMENT))
            .as(BODY_MESSAGE)
            .extracting(Element::text)
            .isEqualTo(OBSERVE_HEARING_ENGLISH);

        softly.assertThat(document.getElementsByTag("th"))
            .as(TABLE_HEADERS_MESSAGE)
            .hasSize(8)
            .extracting(Element::text)
            .containsExactly(
                HEARING_TIME,
                "Applicant",
                "Representative",
                CASE_REFERENCE_NUMBER,
                JUDGES,
                HEARING_TYPE,
                "Location",
                ADDITIONAL_INFORMATION
            );

        softly.assertAll();
    }

    @Test
    void testUtIacJudicialReviewDailyHearingListFileConversionInWelsh() throws IOException {
        Map<String, Object> languageResource;
        try (InputStream languageFile = Thread.currentThread()
            .getContextClassLoader()
            .getResourceAsStream("templates/languages/cy/non-strategic/utIacJudicialReviewDailyHearingList.json")) {
            languageResource = new ObjectMapper().readValue(
                Objects.requireNonNull(languageFile).readAllBytes(), new TypeReference<>() {
                });
        }

        Map<String, String> metadata = Map.of(CONTENT_DATE_METADATA, CONTENT_DATE,
                                              PROVENANCE_METADATA, PROVENANCE,
                                              LANGUAGE_METADATA, WELSH,
                                              LIST_TYPE_METADATA, UT_IAC_JUDICIAL_REVIEW_DAILY_HEARING_LIST.name()
        );

        String result = converter.convert(utIacJudicialReviewInputJson, metadata, languageResource);
        Document document = Jsoup.parse(result);

        SoftAssertions softly = new SoftAssertions();

        softly.assertThat(document.title())
            .as(TITLE_MESSAGE)
            .isEqualTo("Uwch Dribiwnlys (Mewnfudo a Lloches) - Rhestr o Wrandawiadau Dyddiol Siambr Adolygiadau "
                           + "Barnwrol - Field House");

        softly.assertThat(document.getElementById(HEADER_ELEMENT))
            .as(HEADER_MESSAGE)
            .extracting(Element::text)
            .isEqualTo("Uwch Dribiwnlys (Mewnfudo a Lloches) - Rhestr o Wrandawiadau Dyddiol Siambr Adolygiadau "
                           + "Barnwrol - Field House");

        softly.assertThat(document.getElementById(LIST_DATE_ELEMENT))
            .as(LIST_DATE_MESSAGE)
            .extracting(Element::text)
            .isEqualTo(LIST_DATE_WELSH);

        softly.assertThat(document.getElementById(LIST_UPDATE_MESSAGE_ELEMENT))
            .as(BODY_MESSAGE)
            .extracting(Element::text)
            .asString()
            .contains("Gall y rhestr ganlynol newid tan 4:30pm.");

        softly.assertThat(document.getElementById(OBSERVE_HEARING_ELEMENT))
            .as(BODY_MESSAGE)
            .extracting(Element::text)
            .isEqualTo(OBSERVE_HEARING_WELSH);

        softly.assertThat(document.getElementsByTag("th"))
            .as(TABLE_HEADERS_MESSAGE)
            .hasSize(8)
            .extracting(Element::text)
            .containsExactly(
                HEARING_TIME_WELSH,
                "Ymgeisydd",
                "Cynrychiolir gan",
                "Cyfeirnod yr achos",
                JUDGES_WELSH,
                HEARING_TYPE_WELSH,
                VENUE_WELSH,
                ADDITIONAL_INFORMATION_WELSH
            );

        softly.assertAll();
    }

    @Test
    void testUtIacStatutoryAppealsDailyHearingListFileConversionInEnglish() throws IOException {
        Map<String, Object> languageResource;
        try (InputStream languageFile = Thread.currentThread()
            .getContextClassLoader()
            .getResourceAsStream("templates/languages/en/non-strategic/utIacStatutoryAppealsDailyHearingList.json")) {
            languageResource = new ObjectMapper().readValue(
                Objects.requireNonNull(languageFile).readAllBytes(), new TypeReference<>() {
                });
        }

        Map<String, String> metadata = Map.of(CONTENT_DATE_METADATA, CONTENT_DATE,
                                              PROVENANCE_METADATA, PROVENANCE,
                                              LANGUAGE_METADATA, ENGLISH,
                                              LIST_TYPE_METADATA, UT_IAC_STATUTORY_APPEALS_DAILY_HEARING_LIST.name()
        );

        String result = converter.convert(utIacStatutoryAppealsInputJson, metadata, languageResource);
        Document document = Jsoup.parse(result);

        SoftAssertions softly = new SoftAssertions();

        softly.assertThat(document.title())
            .as(TITLE_MESSAGE)
            .isEqualTo("Upper Tribunal (Immigration and Asylum) Chamber Statutory Daily Hearing List");

        softly.assertThat(document.getElementById(HEADER_ELEMENT))
            .as(HEADER_MESSAGE)
            .extracting(Element::text)
            .isEqualTo("Upper Tribunal (Immigration and Asylum) Chamber Statutory Daily Hearing List");

        softly.assertThat(document.getElementById(LIST_DATE_ELEMENT))
            .as(LIST_DATE_MESSAGE)
            .extracting(Element::text)
            .isEqualTo(LIST_DATE_ENGLISH);

        softly.assertThat(document.getElementById(LIST_UPDATE_MESSAGE_ELEMENT))
            .as(BODY_MESSAGE)
            .extracting(Element::text)
            .asString()
            .contains("We update this list by 5pm for the following day.");

        softly.assertThat(document.getElementById(ATTEND_HEARING_MESSAGE_ELEMENT))
            .as(BODY_MESSAGE)
            .extracting(Element::text)
            .isEqualTo("For details on attending a UTIAC remote hearing, please email "
                           + "uppertribunallistingteam@justice.gov.uk.");

        softly.assertThat(document.getElementById(OBSERVE_HEARING_ELEMENT))
            .as(BODY_MESSAGE)
            .extracting(Element::text)
            .isEqualTo(OBSERVE_HEARING_ENGLISH);

        softly.assertThat(document.getElementsByTag("th"))
            .as(TABLE_HEADERS_MESSAGE)
            .hasSize(8)
            .extracting(Element::text)
            .containsExactly(
                HEARING_TIME,
                "Appellant",
                "Representative",
                "Appeal reference number",
                JUDGES,
                HEARING_TYPE,
                "Location",
                ADDITIONAL_INFORMATION
            );

        softly.assertAll();
    }

    @Test
    void testUtIacStatutoryAppealsDailyHearingListFileConversionInWelsh() throws IOException {
        Map<String, Object> languageResource;
        try (InputStream languageFile = Thread.currentThread()
            .getContextClassLoader()
            .getResourceAsStream("templates/languages/cy/non-strategic/utIacStatutoryAppealsDailyHearingList.json")) {
            languageResource = new ObjectMapper().readValue(
                Objects.requireNonNull(languageFile).readAllBytes(), new TypeReference<>() {
                });
        }

        Map<String, String> metadata = Map.of(CONTENT_DATE_METADATA, CONTENT_DATE,
                                              PROVENANCE_METADATA, PROVENANCE,
                                              LANGUAGE_METADATA, WELSH,
                                              LIST_TYPE_METADATA, UT_IAC_STATUTORY_APPEALS_DAILY_HEARING_LIST.name()
        );

        String result = converter.convert(utIacStatutoryAppealsInputJson, metadata, languageResource);
        Document document = Jsoup.parse(result);

        SoftAssertions softly = new SoftAssertions();

        softly.assertThat(document.title())
            .as(TITLE_MESSAGE)
            .isEqualTo("Uwch Dribiwnlys (Mewnfudo a Lloches) - Rhestr o Wrandawiadau Dyddiol Statudol Siambr");

        softly.assertThat(document.getElementById(HEADER_ELEMENT))
            .as(HEADER_MESSAGE)
            .extracting(Element::text)
            .isEqualTo("Uwch Dribiwnlys (Mewnfudo a Lloches) - Rhestr o Wrandawiadau Dyddiol Statudol Siambr");

        softly.assertThat(document.getElementById(LIST_DATE_ELEMENT))
            .as(LIST_DATE_MESSAGE)
            .extracting(Element::text)
            .isEqualTo(LIST_DATE_WELSH);

        softly.assertThat(document.getElementById(LIST_UPDATE_MESSAGE_ELEMENT))
            .as(BODY_MESSAGE)
            .extracting(Element::text)
            .asString()
            .contains("Rydym yn diweddaru’r rhestr hon erbyn 5pm ar gyfer y diwrnod canlynol.");

        softly.assertThat(document.getElementById(ATTEND_HEARING_MESSAGE_ELEMENT))
            .as(BODY_MESSAGE)
            .extracting(Element::text)
            .isEqualTo("I gael manylion am fynychu gwrandawiad UTIAC o bell, anfonwch e-bost at "
                           + "uppertribunallistingteam@justice.gov.uk.");

        softly.assertThat(document.getElementById(OBSERVE_HEARING_ELEMENT))
            .as(BODY_MESSAGE)
            .extracting(Element::text)
            .isEqualTo(OBSERVE_HEARING_WELSH);

        softly.assertThat(document.getElementsByTag("th"))
            .as(TABLE_HEADERS_MESSAGE)
            .hasSize(8)
            .extracting(Element::text)
            .containsExactly(
                HEARING_TIME_WELSH,
                "Apelydd",
                "Cynrychiolir gan",
                "Cyfeirnod yr apêl",
                JUDGES_WELSH,
                HEARING_TYPE_WELSH,
                VENUE_WELSH,
                ADDITIONAL_INFORMATION_WELSH
            );

        softly.assertAll();
    }

    @Test
    void testSiacWeeklyHearingListFileConversionInEnglish() throws IOException {
        Map<String, Object> languageResource;
        try (InputStream languageFile = Thread.currentThread()
            .getContextClassLoader()
            .getResourceAsStream("templates/languages/en/non-strategic/siacWeeklyHearingList.json")) {
            languageResource = new ObjectMapper().readValue(
                Objects.requireNonNull(languageFile).readAllBytes(), new TypeReference<>() {
                });
        }

        Map<String, String> metadata = Map.of(CONTENT_DATE_METADATA, CONTENT_DATE,
                                              PROVENANCE_METADATA, PROVENANCE,
                                              LANGUAGE_METADATA, ENGLISH,
                                              LIST_TYPE_METADATA, SIAC_WEEKLY_HEARING_LIST.name()
        );

        String result = converter.convert(siacInputJson, metadata, languageResource);
        Document document = Jsoup.parse(result);

        SoftAssertions softly = new SoftAssertions();

        softly.assertThat(document.title())
            .as(TITLE_MESSAGE)
            .isEqualTo("Special Immigration Appeals Commission Weekly Hearing List");

        softly.assertThat(document.getElementById(HEADER_ELEMENT))
            .as(HEADER_MESSAGE)
            .extracting(Element::text)
            .isEqualTo("Special Immigration Appeals Commission Weekly Hearing List");

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

        softly.assertThat(document.getElementById(OBSERVE_HEARING_ELEMENT))
            .as(BODY_MESSAGE)
            .extracting(Element::text)
            .isEqualTo(OBSERVE_HEARING_ENGLISH);

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

    @Test
    void testSiacWeeklyHearingListFileConversionInWelsh() throws IOException {
        Map<String, Object> languageResource;
        try (InputStream languageFile = Thread.currentThread()
            .getContextClassLoader()
            .getResourceAsStream("templates/languages/cy/non-strategic/siacWeeklyHearingList.json")) {
            languageResource = new ObjectMapper().readValue(
                Objects.requireNonNull(languageFile).readAllBytes(), new TypeReference<>() {
                });
        }

        Map<String, String> metadata = Map.of(CONTENT_DATE_METADATA, CONTENT_DATE,
                                              PROVENANCE_METADATA, PROVENANCE,
                                              LANGUAGE_METADATA, WELSH,
                                              LIST_TYPE_METADATA, SIAC_WEEKLY_HEARING_LIST.name()
        );

        String result = converter.convert(siacInputJson, metadata, languageResource);
        Document document = Jsoup.parse(result);

        SoftAssertions softly = new SoftAssertions();

        softly.assertThat(document.title())
            .as(TITLE_MESSAGE)
            .isEqualTo(CST_LIST_WELSH_NAME);

        softly.assertThat(document.getElementById(HEADER_ELEMENT))
            .as(HEADER_MESSAGE)
            .extracting(Element::text)
            .isEqualTo(CST_LIST_WELSH_NAME);

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

        softly.assertThat(document.getElementById(OBSERVE_HEARING_ELEMENT))
            .as(BODY_MESSAGE)
            .extracting(Element::text)
            .isEqualTo(OBSERVE_HEARING_WELSH);

        softly.assertThat(document.getElementsByTag("th"))
            .as(TABLE_HEADERS_MESSAGE)
            .hasSize(7)
            .extracting(Element::text)
            .containsExactly(
                "Dyddiad",
                "Amser",
                "Appellant",
                CASE_REFERENCE_NUMBER,
                HEARING_TYPE,
                "Courtroom",
                "Gwybodaeth ychwanegol"
            );

        softly.assertAll();
    }
}
