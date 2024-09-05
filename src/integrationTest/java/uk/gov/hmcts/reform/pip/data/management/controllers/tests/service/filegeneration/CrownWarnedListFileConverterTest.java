package uk.gov.hmcts.reform.pip.data.management.controllers.tests.service.filegeneration;

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
import uk.gov.hmcts.reform.pip.data.management.service.filegeneration.CrownWarnedListFileConverter;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.Map;
import java.util.Objects;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class CrownWarnedListFileConverterTest {
    private static final String CONTENT_DATE = "23 October 2022";
    private static final String ENGLISH = "ENGLISH";
    private static final Map<String, String> METADATA = Map.of("contentDate", CONTENT_DATE,
                                                               "language", ENGLISH,
                                                               "listType", "CROWN_WARNED_LIST");

    private final CrownWarnedListFileConverter converter = new CrownWarnedListFileConverter();
    private JsonNode inputJson;
    private Map<String, Object> language;

    @BeforeAll
    void setup() throws IOException {
        language = handleLanguage();
        try (InputStream inputStream = getClass().getResourceAsStream("/mocks/crownWarnedList.json")) {
            String inputRaw = IOUtils.toString(inputStream, Charset.defaultCharset());
            inputJson = new ObjectMapper().readTree(inputRaw);
        }
    }

    @Test
    void testHeadersForSuccessfulConversion() {
        String result = converter.convert(inputJson, METADATA, language);
        Document doc = Jsoup.parse(result);
        SoftAssertions softly = new SoftAssertions();

        softly.assertThat(doc.getElementById("list-name"))
            .as("Incorrect list name")
            .extracting(Element::text)
            .isEqualTo("Crown Warned List for Manchester Court");

        softly.assertThat(doc.getElementById("publication-date"))
            .as("Incorrect publication date")
            .extracting(Element::text)
            .isEqualTo("Last updated 13 September 2022 at 12:30pm");

        softly.assertThat(doc.getElementById("list-version"))
            .as("Incorrect version")
            .extracting(Element::text)
            .isEqualTo("Draft: Version 1.0");

        softly.assertThat(doc.getElementById("venue-address"))
            .as("Incorrect venue address")
            .extracting(Element::text)
            .isEqualTo("Princess Square Manchester M1 1AA");

        softly.assertThat(doc.getElementsByClass("heading-note"))
            .as("Incorrect content date")
            .anyMatch(e -> e.text().contains("23 October 2022"));

        softly.assertThat(doc.getElementsByClass("restriction-list-section"))
            .as("Incorrect restriction heading")
            .anyMatch(e -> e.text().contains("Restrictions on publishing or writing about these cases"));

        softly.assertAll();
    }

    @Test
    void testTableContentsForSuccessfulConversion() {
        String result = converter.convert(inputJson, METADATA, language);
        Document doc = Jsoup.parse(result);
        SoftAssertions softly = new SoftAssertions();

        softly.assertThat(doc.getElementsByClass("govuk-accordion__section-heading"))
            .as("Incorrect hearing type")
            .hasSize(6)
            .extracting(Element::text)
            .containsExactly(
                "For Trial",
                "For Pre-Trial review",
                "For Appeal",
                "For Appeal against Conviction",
                "For Sentence",
                "To be allocated"
            );

        softly.assertThat(doc.getElementsByTag("th"))
            .as("Incorrect table headers")
            .hasSize(30)
            .extracting(Element::text)
            .startsWith("Case Reference",
                        "Defendant Name(s)",
                        "Fixed For",
                        "Represented By",
                        "Prosecuting Authority"
            );

        softly.assertThat(doc.getElementsByTag("td"))
            .as("Incorrect table contents")
            .hasSize(74)
            .extracting(Element::text)
            .startsWith("12345678",
                        "Surname 1, Forename 1",
                        "27/07/2022",
                        "Defendant rep 1",
                        "Prosecutor",
                        "Linked Cases: 123456, 123457",
                        "Listing Notes: Note 1"
            );

        softly.assertAll();
    }

    private Map<String, Object> handleLanguage() throws IOException {
        try (InputStream languageFile = Thread.currentThread()
            .getContextClassLoader().getResourceAsStream("templates/languages/en/crownWarnedList.json")) {
            return new ObjectMapper().readValue(
                Objects.requireNonNull(languageFile).readAllBytes(), new TypeReference<>() {
                });
        }
    }
}
