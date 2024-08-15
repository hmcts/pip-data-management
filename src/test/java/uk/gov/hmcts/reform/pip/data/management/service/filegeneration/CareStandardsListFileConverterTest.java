package uk.gov.hmcts.reform.pip.data.management.service.filegeneration;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.IOUtils;
import org.assertj.core.api.SoftAssertions;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.Map;
import java.util.Objects;

class CareStandardsListFileConverterTest {
    private final CareStandardsListFileConverter converter = new CareStandardsListFileConverter();

    @Test
    @SuppressWarnings("PMD.JUnitAssertionsShouldIncludeMessage")
    void testSuccessfulConversion() throws IOException {
        Map<String, String> metaData = Map.of("contentDate", "02 October 2022",
                                              "language", "ENGLISH",
                                              "listType", "CARE_STANDARDS_LIST");
        Map<String, Object> language = handleLanguage();
        JsonNode input = getInput("/mocks/careStandardsList.json");

        String result = converter.convert(input, metaData, language);
        Document doc = Jsoup.parse(result);
        SoftAssertions softly = new SoftAssertions();

        softly.assertThat(doc.getElementsByTag("h2"))
            .as("Incorrect h2 element")
            .hasSize(1)
            .first()
            .extracting(Element::text)
            .isEqualTo("Tribunal Hearing List for Care Standards");

        softly.assertThat(doc.getElementsByClass("header").get(0).getElementsByTag("p"))
            .as("Incorrect p elements")
            .isNotEmpty()
            .extracting(Element::text)
            .contains(
                "List for 02 October 2022",
                "Last updated 04 October 2022 at 10am"
            );

        softly.assertThat(doc.getElementsByTag("a"))
            .as("Incorrect anchor title")
            .hasSize(1)
            .extracting(element -> element.attr("title"))
            .containsExactly("How to observe a court or tribunal hearing");

        softly.assertThat(doc.getElementsByTag("th"))
            .as("Incorrect table headers")
            .hasSize(5)
            .extracting(Element::text)
            .containsExactly("Hearing Date", "Case Name", "Duration", "Hearing Type", "Venue");

        softly.assertThat(doc.getElementsByTag("td"))
            .as("Incorrect table contents")
            .hasSize(15)
            .extracting(Element::text)
            .containsExactly("05 October",
                             "A Vs B",
                             "1 day [1 of 2]",
                             "Remote - Teams",
                             "The Court House Court Street SK4 5LE",
                             "05 October",
                             "A Vs B",
                             "1 day [2 of 2]",
                             "Remote - Teams",
                             "The Court House Court Street SK4 5LE",
                             "06 October",
                             "C Vs D",
                             "2 hours 30 mins",
                             "Video",
                             "The Court House Court Street SK4 5LE");

        softly.assertAll();
    }

    private JsonNode getInput(String resourcePath) throws IOException {
        try (InputStream inputStream = getClass().getResourceAsStream(resourcePath)) {
            String inputRaw = IOUtils.toString(inputStream, Charset.defaultCharset());
            return new ObjectMapper().readTree(inputRaw);
        }
    }

    private Map<String, Object> handleLanguage() throws IOException {
        try (InputStream languageFile = Thread.currentThread()
            .getContextClassLoader().getResourceAsStream("templates/languages/en/careStandardsList.json")) {
            return new ObjectMapper().readValue(
                Objects.requireNonNull(languageFile).readAllBytes(), new TypeReference<>() {
                });
        }
    }
}
