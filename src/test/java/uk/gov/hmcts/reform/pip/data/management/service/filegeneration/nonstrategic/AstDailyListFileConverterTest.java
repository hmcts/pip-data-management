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

import static uk.gov.hmcts.reform.pip.model.publication.ListType.AST_DAILY_LIST;

@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class AstDailyListFileConverterTest {
    private static final String CONTENT_DATE = "20 February 2025";
    private static final String LAST_RECEIVED_DATE = "2025-02-19T09:30:00Z";
    private static final String PROVENANCE = "provenance";
    private static final String CONTENT_DATE_METADATA = "contentDate";
    private static final String PROVENANCE_METADATA = "provenance";
    private static final String LANGUAGE_METADATA = "language";
    private static final String LIST_TYPE_METADATA = "listType";
    private static final String LAST_RECEIVED_DATE_METADATA = "lastReceivedDate";

    private static final String ENGLISH = "ENGLISH";
    private static final String WELSH = "WELSH";

    private static final String HEADER_ELEMENT = "page-heading";
    private static final String VENUE_ELEMENT = "venue";
    private static final String LIST_DATE_ELEMENT = "list-date";
    private static final String LAST_UPDATED_DATE_ELEMENT = "last-updated-date";
    private static final String OPEN_JUSTICE_MESSAGE_ELEMENT = "open-justice-message";
    private static final String JOIN_HEARING_MESSAGE_ELEMENT = "join-hearing-message";
    private static final String OBSERVE_HEARING_ELEMENT =  "observe-hearing";
    private static final String SUMMARY_TEXT_CLASS = "govuk-details__summary-text";

    private static final String TITLE_MESSAGE = "Title does not match";
    private static final String HEADER_MESSAGE = "Header does not match";
    private static final String VENUE_MESSAGE = "Venue does not match";
    private static final String LIST_DATE_MESSAGE = "List date does not match";
    private static final String LAST_UPDATED_DATE_MESSAGE = "Last updated date does not match";
    private static final String IMPORTANT_INFORMATION_MESSAGE = "Important information heading does not match";
    private static final String BODY_MESSAGE = "Body does not match";
    private static final String TABLE_HEADERS_MESSAGE = "Table headers does not match";

    private final NonStrategicListFileConverter converter = new NonStrategicListFileConverter();

    private JsonNode cstInputJson;

    @BeforeAll
    void setup() throws IOException {
        try (InputStream inputStream = getClass()
            .getResourceAsStream("/mocks/non-strategic/astDailyList.json")) {
            String inputRaw = IOUtils.toString(inputStream, Charset.defaultCharset());
            cstInputJson = new ObjectMapper().readTree(inputRaw);
        }
    }


    @Test
    void testAstDailyListFileConversionInEnglish() throws IOException {
        Map<String, Object> languageResource;
        try (InputStream languageFile = Thread.currentThread()
            .getContextClassLoader()
            .getResourceAsStream("templates/languages/en/non-strategic/astDailyList.json")) {
            languageResource = new ObjectMapper().readValue(
                Objects.requireNonNull(languageFile).readAllBytes(), new TypeReference<>() {
                });
        }

        Map<String, String> metadata = Map.of(CONTENT_DATE_METADATA, CONTENT_DATE,
                                              PROVENANCE_METADATA, PROVENANCE,
                                              LANGUAGE_METADATA, ENGLISH,
                                              LIST_TYPE_METADATA, AST_DAILY_LIST.name(),
                                              LAST_RECEIVED_DATE_METADATA, LAST_RECEIVED_DATE
        );

        String result = converter.convert(cstInputJson, metadata, languageResource);
        Document document = Jsoup.parse(result);

        SoftAssertions softly = new SoftAssertions();

        softly.assertThat(document.title())
            .as(TITLE_MESSAGE)
            .isEqualTo("Asylum Support Tribunal Daily Hearing List");

        softly.assertThat(document.getElementById(HEADER_ELEMENT))
            .as(HEADER_MESSAGE)
            .extracting(Element::text)
            .isEqualTo("Asylum Support Tribunal Daily Hearing List");

        softly.assertThat(document.getElementById(VENUE_ELEMENT))
            .as(VENUE_MESSAGE)
            .extracting(Element::text)
            .isEqualTo("2nd Floor, Import Building, 2 Clove Crescent London E14 2BE");

        softly.assertThat(document.getElementById(LIST_DATE_ELEMENT))
            .as(LIST_DATE_MESSAGE)
            .extracting(Element::text)
            .isEqualTo("List for 20 February 2025");

        softly.assertThat(document.getElementById(LAST_UPDATED_DATE_ELEMENT))
            .as(LAST_UPDATED_DATE_MESSAGE)
            .extracting(Element::text)
            .isEqualTo("Last updated 19 February 2025 at 9:30am");

        softly.assertThat(document.getElementsByClass(SUMMARY_TEXT_CLASS).get(0))
            .as(IMPORTANT_INFORMATION_MESSAGE)
            .extracting(Element::text)
            .isEqualTo("Important information");

        softly.assertThat(document.getElementById(OPEN_JUSTICE_MESSAGE_ELEMENT).text())
            .as(BODY_MESSAGE)
            .contains("Open justice is a fundamental principle of our justice system.");

        softly.assertThat(document.getElementById(JOIN_HEARING_MESSAGE_ELEMENT).text())
            .as(BODY_MESSAGE)
            .contains("Asylum Support Tribunal parties and representatives will be informed directly as to the "
                           + "arrangements for hearing cases remotely.");

        softly.assertThat(document.getElementById(OBSERVE_HEARING_ELEMENT))
            .as(BODY_MESSAGE)
            .extracting(Element::text)
            .isEqualTo("For more information, please visit https://www.gov.uk/guidance/observe-a-court-or-tribunal-hearing");

        softly.assertThat(document.getElementsByTag("th"))
            .as(TABLE_HEADERS_MESSAGE)
            .hasSize(6)
            .extracting(Element::text)
            .containsExactly(
                "Appellant",
                "Appeal reference number",
                "Case type",
                "Hearing type",
                "Hearing time",
                "Additional information"
            );

        softly.assertAll();
    }

    @Test
    void testAstDailyListFileConversionInWelsh() throws IOException {
        Map<String, Object> languageResource;
        try (InputStream languageFile = Thread.currentThread()
            .getContextClassLoader()
            .getResourceAsStream("templates/languages/cy/non-strategic/astDailyList.json")) {
            languageResource = new ObjectMapper().readValue(
                Objects.requireNonNull(languageFile).readAllBytes(), new TypeReference<>() {
                });
        }

        Map<String, String> metadata = Map.of(CONTENT_DATE_METADATA, CONTENT_DATE,
                                              PROVENANCE_METADATA, PROVENANCE,
                                              LANGUAGE_METADATA, WELSH,
                                              LIST_TYPE_METADATA, AST_DAILY_LIST.name(),
                                              LAST_RECEIVED_DATE_METADATA, LAST_RECEIVED_DATE
        );

        String result = converter.convert(cstInputJson, metadata, languageResource);
        Document document = Jsoup.parse(result);

        SoftAssertions softly = new SoftAssertions();

        softly.assertThat(document.title())
            .as(TITLE_MESSAGE)
            .isEqualTo("Rhestr ddyddiol y Tribiwnlys Cefnogi Ceiswyr Lloches");

        softly.assertThat(document.getElementById(HEADER_ELEMENT))
            .as(HEADER_MESSAGE)
            .extracting(Element::text)
            .isEqualTo("Rhestr ddyddiol y Tribiwnlys Cefnogi Ceiswyr Lloches");

        softly.assertThat(document.getElementById(LIST_DATE_ELEMENT))
            .as(LIST_DATE_MESSAGE)
            .extracting(Element::text)
            .isEqualTo("Rhestr ar gyfer 20 February 2025");

        softly.assertThat(document.getElementById(LAST_UPDATED_DATE_ELEMENT))
            .as(LAST_UPDATED_DATE_MESSAGE)
            .extracting(Element::text)
            .isEqualTo("Diweddarwyd ddiwethaf 19 February 2025 am 9:30am");

        softly.assertThat(document.getElementsByClass(SUMMARY_TEXT_CLASS).get(0))
            .as(IMPORTANT_INFORMATION_MESSAGE)
            .extracting(Element::text)
            .isEqualTo("Gwybodaeth bwysig");

        softly.assertThat(document.getElementById(OPEN_JUSTICE_MESSAGE_ELEMENT).text())
            .as(BODY_MESSAGE)
            .contains("Open justice is a fundamental principle of our justice system.");

        softly.assertThat(document.getElementById(JOIN_HEARING_MESSAGE_ELEMENT).text())
            .as(BODY_MESSAGE)
            .contains("Asylum Support Tribunal parties and representatives will be informed directly as to the "
                          + "arrangements for hearing cases remotely.");

        softly.assertThat(document.getElementById(OBSERVE_HEARING_ELEMENT))
            .as(BODY_MESSAGE)
            .extracting(Element::text)
            .isEqualTo("For more information, please visit https://www.gov.uk/guidance/observe-a-court-or-tribunal-hearing");

        softly.assertThat(document.getElementsByTag("th"))
            .as(TABLE_HEADERS_MESSAGE)
            .hasSize(6)
            .extracting(Element::text)
            .containsExactly(
                "Apelydd",
                "Cyfeirnod apêl",
                "Math o achos",
                "Math o wrandawiad",
                "Amser y gwrandawiad",
                "Gwybodaeth ychwanegol"
            );

        softly.assertAll();
    }
}
