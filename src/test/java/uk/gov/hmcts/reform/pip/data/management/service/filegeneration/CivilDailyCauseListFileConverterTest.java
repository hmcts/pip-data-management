package uk.gov.hmcts.reform.pip.data.management.service.filegeneration;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.IOUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.ActiveProfiles;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.Map;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("test")
@SuppressWarnings({"PMD.LooseCoupling"})
class CivilDailyCauseListFileConverterTest {
    private static final String OXFORD_COURT = "Oxford Combined Court Centre";
    private static final String TITLE_TEXT = "Incorrect Title Text";
    private static final String MANUAL_UPLOAD = "MANUAL_UPLOAD";

    private static final Map<String, String> METADATA = Map.of(
        "contentDate", "20 August 2023",
        "locationName", OXFORD_COURT,
        "provenance", MANUAL_UPLOAD,
        "language", "ENGLISH",
        "listType", "CIVIL_DAILY_CAUSE_LIST"
    );
    private static final int NUMBER_OF_TABLES = 2;

    CivilDailyCauseListFileConverter converter = new CivilDailyCauseListFileConverter();

    @Test
    void testSuccessfulConversion() throws IOException {
        Map<String, Object> language;
        try (InputStream languageFile = Thread.currentThread()
            .getContextClassLoader().getResourceAsStream("templates/languages/en/civilDailyCauseList.json")) {
            language = new ObjectMapper().readValue(
                Objects.requireNonNull(languageFile).readAllBytes(), new TypeReference<>() {
                });
        }
        String result = converter.convert(getInput("/mocks/civilDailyCauseList.json"), METADATA, language);
        Document document = Jsoup.parse(result);

        assertThat(result)
            .as("No html found")
            .isNotEmpty();

        assertThat(document.title())
            .as("incorrect document title")
            .isEqualTo("Civil Daily Cause List for");

        assertThat(document.getElementsByTag("a")
                       .get(0).attr("title"))
            .as(TITLE_TEXT).contains("How to observe a court or tribunal hearing");

        assertFirstPageContent(document.getElementsByClass("first-page").get(0));
        assertCourtHouseInfo(document.getElementsByClass("site-address"));
        assertHearingTables(document);
        assertDataSource(document);
    }


    @Test
    void testSuccessfulConversionWelsh() throws IOException {
        Map<String, Object> language;
        try (InputStream languageFile = Thread.currentThread()
            .getContextClassLoader().getResourceAsStream("templates/languages/cy/civilDailyCauseList.json")) {
            language = new ObjectMapper().readValue(
                Objects.requireNonNull(languageFile).readAllBytes(), new TypeReference<>() {
                });
        }
        String result = converter.convert(getInput("/mocks/civilDailyCauseList.json"), METADATA, language);
        Document document = Jsoup.parse(result);

        assertThat(result)
            .as("No html found")
            .isNotEmpty();

        assertThat(document.title())
            .as("incorrect document title")
            .isEqualTo("Rhestr Ddyddiol o Achosion Sifil gyfer");

        assertThat(document.getElementsByTag("a")
                       .get(0).attr("title"))
            .as(TITLE_TEXT).contains("Sut i arsylwi gwrandawiad llys neu dribiwnlys");

        assertThat(document.getElementsByClass("govuk-accordion__section-heading"))
            .as("Incorrect table titles")
            .hasSize(NUMBER_OF_TABLES)
            .extracting(Element::text)
            .containsExactly(
                "Courtroom 1: Judge KnownAs Presiding, Judge KnownAs 2",
                "Courtroom 2");

    }

    private void assertFirstPageContent(Element element) {

        assertThat(element.getElementsByTag("h2"))
            .as("Incorrect first page h2 elements")
            .hasSize(1)
            .extracting(Element::text)
            .containsExactly("Civil Daily Cause List for " + OXFORD_COURT);

        assertThat(element.getElementsByTag("p"))
            .as("Incorrect first page p elements")
            .hasSize(8)
            .extracting(Element::text)
            .contains("The venue line 1 town name AAA AAA",
                      "List for 20 August 2023",
                      "Last updated 21 August 2023 at 2:01am"
            );
    }

    public void assertCourtHouseInfo(Elements elements) {
        assertThat(elements)
            .as("Incorrect court house info")
            .hasSize(3)
            .extracting(Element::text)
            .containsExactly(
                "This is a court house name",
                "Address Line 1",
                "AAA AAB"
            );
    }

    public void assertHearingTables(Document document) {
        assertThat(document.getElementsByClass("govuk-accordion__section-heading"))
            .as("Incorrect table titles")
            .hasSize(NUMBER_OF_TABLES)
            .extracting(Element::text)
            .containsExactly(
                "Courtroom 1: Judge KnownAs Presiding, Judge KnownAs 2",
                "Courtroom 2");

        Elements tableElements = document.getElementsByClass("govuk-table");
        assertThat(tableElements)
            .as("Incorrect number of tables")
            .hasSize(NUMBER_OF_TABLES);

        Element firstTableElement = tableElements.get(0);
        Element secondTableElement = tableElements.get(1);

        // Assert the table columns are expected
        assertThat(getTableHeaders(firstTableElement))
            .as("Incorrect table headers")
            .hasSize(7)
            .extracting(Element::text)
            .containsExactly(
                "Time",
                "Case ID",
                "Case name",
                "Case type",
                "Hearing type",
                "Location",
                "Duration"
            );

        // Assert number of rows for each table
        assertThat(getTableBodyRows(firstTableElement))
            .as("Incorrect table rows for the first table")
            .hasSize(4);
        assertThat(getTableBodyRows(secondTableElement))
            .as("Incorrect table rows for the second table")
            .hasSize(1);
    }

    private void assertDataSource(Document document) {
        Elements elements = document.getElementsByTag("p");
        assertThat(elements.get(10).text())
            .as("Incorrect data source")
            .isEqualTo("Data Source: " + MANUAL_UPLOAD);
    }

    private JsonNode getInput(String resourcePath) throws IOException {
        try (InputStream inputStream = getClass().getResourceAsStream(resourcePath)) {
            String inputRaw = IOUtils.toString(inputStream, Charset.defaultCharset());
            return new ObjectMapper().readTree(inputRaw);
        }
    }

    private Elements getTableHeaders(Element table) {
        return table
            .getElementsByClass("govuk-table__head")
            .get(0)
            .getElementsByClass("govuk-table__row")
            .get(0)
            .getElementsByTag("th");
    }

    private Elements getTableBodyRows(Element table) {
        return table
            .getElementsByClass("govuk-table__body")
            .get(0)
            .getElementsByClass("govuk-table__row");
    }
}
