package uk.gov.hmcts.reform.pip.data.management.service.filegeneration;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.IOUtils;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.pip.model.publication.ListType;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;

@ActiveProfiles("test")
class CivilDailyCauseListFileConverterTest {
    private static final String OXFORD_COURT = "Oxford Combined Court Centre";
    private static final String TITLE_TEXT = "Incorrect Title Text";
    private static final String MANUAL_UPLOAD = "MANUAL_UPLOAD";
    private static final String LINK_MESSAGE = "Link does not match";

    private static final String LINK_CLASS = "govuk-link";
    private static final String HREF = "href";
    private static final String BODY_CLASS = "govuk-body";

    private static final Map<String, String> METADATA = Map.of(
        "contentDate", "20 August 2023",
        "locationName", OXFORD_COURT,
        "provenance", MANUAL_UPLOAD,
        "language", "ENGLISH",
        "listType", "CIVIL_DAILY_CAUSE_LIST"
    );

    private static final Map<String, String> METADATA_WELSH = Map.of(
        "contentDate", "20 August 2023",
        "locationName", OXFORD_COURT,
        "provenance", MANUAL_UPLOAD,
        "language", "WELSH",
        "listType", "CIVIL_DAILY_CAUSE_LIST"
    );
    private static final int NUMBER_OF_TABLES = 2;

    private final FileConverter converter = new CivilDailyCauseListFileConverter();

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

        assertThat(document.getElementsByClass(LINK_CLASS).get(0)
                       .getElementsByTag("a").get(0)
                       .attr(HREF))
            .as(LINK_MESSAGE)
            .isEqualTo("https://www.find-court-tribunal.service.gov.uk/");

        assertThat(document.getElementsByClass(BODY_CLASS).get(0).text())
            .as(LINK_MESSAGE)
            .isEqualTo("Find contact details and other information about courts and tribunals in England "
                           + "and Wales, and some non-devolved tribunals in Scotland.");

        assertThat(document.getElementsByTag("a")
                       .get(1).attr("title"))
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

        assertThat(document.getElementsByClass(LINK_CLASS).get(0)
                       .getElementsByTag("a").get(0)
                       .attr(HREF))
            .as(LINK_MESSAGE)
            .isEqualTo("https://www.find-court-tribunal.service.gov.uk/");

        assertThat(document.getElementsByClass(BODY_CLASS).get(0).text())
            .as(LINK_MESSAGE)
            .isEqualTo("Find contact details and other information about courts and tribunals in England "
                           + "and Wales, and some non-devolved tribunals in Scotland.");

        assertThat(document.getElementsByTag("a")
                       .get(1).attr("title"))
            .as(TITLE_TEXT).contains("Sut i arsylwi gwrandawiad llys neu dribiwnlys");

        assertThat(document.getElementsByClass("govuk-accordion__section-heading"))
            .as("Incorrect table titles")
            .hasSize(NUMBER_OF_TABLES)
            .extracting(Element::text)
            .containsExactly(
                "Courtroom 1: Judge KnownAs Presiding, Judge KnownAs 2",
                "Courtroom 2");
    }

    @Test
    void testTableContents() throws IOException {
        Map<String, Object> language;
        try (InputStream languageFile = Thread.currentThread()
            .getContextClassLoader().getResourceAsStream("templates/languages/en/civilDailyCauseList.json")) {
            language = new ObjectMapper().readValue(
                Objects.requireNonNull(languageFile).readAllBytes(), new TypeReference<>() {
                }
            );
        }
        String result = converter.convert(getInput("/mocks/civilDailyCauseList.json"), METADATA, language);
        Document document = Jsoup.parse(result);

        assertThat(document.getElementsByTag("td"))
            .as("Table contents does not match")
            .extracting(Element::text)
            .containsSequence(
                "2:01am",
                "This is case number 1",
                "This is case name 1 [1 of 2]",
                "This is a case type",
                "This is hearing type 1",
                "Channel A, Channel B",
                "1 hour"
            );
    }

    private void assertFirstPageContent(Element element) {

        assertThat(element.getElementsByTag("h2"))
            .as("Incorrect first page h2 elements")
            .hasSize(1)
            .extracting(Element::text)
            .containsExactly("Civil Daily Cause List for " + OXFORD_COURT);

        assertThat(element.getElementsByTag("p"))
            .as("Incorrect first page p elements")
            .hasSize(9)
            .extracting(Element::text)
            .contains("The venue line 1 AAA AAA",
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
        assertThat(elements.get(11).text())
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


    @Test
    void testExcelTableHeaderEnglish() throws IOException {
        StringWriter writer = new StringWriter();
        IOUtils.copy(
            Files.newInputStream(Paths.get("src/test/resources/mocks/",
                                           "civilDailyCauseList.json")), writer,
            Charset.defaultCharset()
        );
        JsonNode inputJson = new ObjectMapper().readTree(writer.toString());

        byte[] result = converter.convertToExcel(inputJson, ListType.CIVIL_DAILY_CAUSE_LIST, METADATA);
        ByteArrayInputStream file = new ByteArrayInputStream(result);
        Workbook workbook = new XSSFWorkbook(file);
        Sheet sheet = workbook.getSheetAt(0);
        Row headingRow = sheet.getRow(0);

        assertEquals("Sheet name does not match", "Sheet1", sheet.getSheetName());
        assertEquals("Court House column is different", "Court House",
                     headingRow.getCell(0).getStringCellValue());
        assertEquals("Court Room is different", "Court Room",
                     headingRow.getCell(1).getStringCellValue());
        assertEquals("Time column is different", "Time",
                     headingRow.getCell(2).getStringCellValue());
        assertEquals("Case ref column is different", "Case ID",
                     headingRow.getCell(3).getStringCellValue());
        assertEquals("Case name column is different", "Case name",
                     headingRow.getCell(4).getStringCellValue());
        assertEquals("Case type column is different", "Case type",
                     headingRow.getCell(5).getStringCellValue());
        assertEquals("Hearing type column is different", "Hearing type",
                     headingRow.getCell(6).getStringCellValue());
        assertEquals("Location column is different", "Location",
                     headingRow.getCell(7).getStringCellValue());
        assertEquals("Duration column is different", "Duration",
                     headingRow.getCell(8).getStringCellValue());
    }

    @Test
    void testExcelTableHeaderWelsh() throws IOException {
        StringWriter writer = new StringWriter();
        IOUtils.copy(Files.newInputStream(Paths.get("src/test/resources/mocks/",
                                                    "civilDailyCauseList.json")), writer,
                     Charset.defaultCharset()
        );
        JsonNode inputJson = new ObjectMapper().readTree(writer.toString());

        byte[] result = converter.convertToExcel(inputJson, ListType.CIVIL_DAILY_CAUSE_LIST, METADATA_WELSH);
        ByteArrayInputStream file = new ByteArrayInputStream(result);
        Workbook workbook = new XSSFWorkbook(file);
        Sheet sheet = workbook.getSheetAt(0);
        Row headingRow = sheet.getRow(0);

        assertEquals("Sheet name does not match", "Sheet1", sheet.getSheetName());
        assertEquals("Court House column is different", "Adeilad Llys",
                     headingRow.getCell(0).getStringCellValue());
        assertEquals("Court Room is different", "Ystafell Llys",
                     headingRow.getCell(1).getStringCellValue());
        assertEquals("Time column is different", "Amser",
                     headingRow.getCell(2).getStringCellValue());
        assertEquals("Case ref column is different", "Rhif adnabod yr achos",
                     headingRow.getCell(3).getStringCellValue());
        assertEquals("Case name at column is different", "Enw'r achos",
                     headingRow.getCell(4).getStringCellValue());
        assertEquals("Case type column is different", "Math o achos",
                     headingRow.getCell(5).getStringCellValue());
        assertEquals("Hearing type column is different", "Math o wrandawiad",
                     headingRow.getCell(6).getStringCellValue());
        assertEquals("Location column is different", "Lleoliad",
                     headingRow.getCell(7).getStringCellValue());
        assertEquals("Duration column is different", "Hyd",
                     headingRow.getCell(8).getStringCellValue());
    }

    @Test
    void testExcelTableRows() throws IOException {
        StringWriter writer = new StringWriter();
        IOUtils.copy(
            Files.newInputStream(Paths.get(
                "src/test/resources/mocks/",
                "civilDailyCauseList.json"
            )), writer,
            Charset.defaultCharset()
        );
        JsonNode inputJson = new ObjectMapper().readTree(writer.toString());

        byte[] result = converter.convertToExcel(
            inputJson,
            ListType.CIVIL_AND_FAMILY_DAILY_CAUSE_LIST, METADATA
        );
        ByteArrayInputStream file = new ByteArrayInputStream(result);
        Workbook workbook = new XSSFWorkbook(file);
        Sheet sheet = workbook.getSheetAt(0);
        Row dataRow = sheet.getRow(1);

        assertEquals("Court House value is different", "This is a court house name",
                     dataRow.getCell(0).getStringCellValue());
        assertEquals("Court Room value is different", "Courtroom 1",
                     dataRow.getCell(1).getStringCellValue());
        assertEquals("Time value is different", "2:01am",
                     dataRow.getCell(2).getStringCellValue());
        assertEquals("Case ref value is different", "This is case number 1",
                     dataRow.getCell(3).getStringCellValue());
        assertEquals("Case name value is different","This is case name 1 [1 of 2]",
                     dataRow.getCell(4).getStringCellValue());
        assertEquals("Case type value is different", "This is a case type",
                     dataRow.getCell(5).getStringCellValue());
        assertEquals("Hearing type value is different", "This is hearing type 1",
                     dataRow.getCell(6).getStringCellValue());
        assertEquals("Location value is different", "Channel A, Channel B",
                     dataRow.getCell(7).getStringCellValue());
        assertEquals("Duration value is different", "1 hour",
                     dataRow.getCell(8).getStringCellValue());
    }

}
