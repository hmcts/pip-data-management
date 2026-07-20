package uk.gov.hmcts.reform.pip.data.management.service.filegeneration;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.IOUtils;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.assertj.core.api.SoftAssertions;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import uk.gov.hmcts.reform.pip.model.publication.Language;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.pip.model.publication.ListType.CROWN_FIRM_PDDA_LIST;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class CrownFirmPddaListFileConverterTest {
    private static final String TEST_FILE_PATH = "src/test/resources/mocks/";

    private static final String LANGUAGE = "language";
    private static final String LIST_TYPE = "listType";
    private static final String HEADING_CLASS = "govuk-heading-l";
    private static final String BODY_CLASS = "govuk-body";
    private static final String LINK_CLASS = "govuk-link";
    private static final String HREF = "href";

    private static final String ADDRESS = "1 Main Road London A1 1AA";
    private static final String FACT_LINK = "https://www.find-court-tribunal.service.gov.uk/";

    private static final String HEADING_MESSAGE = "Heading does not match";
    private static final String BODY_MESSAGE = "Body does not match";
    private static final String LINK_MESSAGE = "Link does not match";
    private static final String TABLE_HEADERS_MESSAGE = "Table headers do not match";
    private static final String EXCEL_SHEET_NAME_MESSAGE = "Excel sheet name does not match";
    private static final String EXCEL_TABLE_HEADER_MESSAGE = "Excel table header does not match";
    private static final String EXCEL_CELL_VALUE_MESSAGE = "Excel cell value does not match";

    private static final Map<String, String> COMMON_METADATA = Map.of("contentDate", Instant.now().toString(),
                                                                      "provenance", "MANUAL_UPLOAD",
                                                                      "locationName", "location"
    );

    private JsonNode inputJson;
    CrownFirmPddaListFileConverter crownFirmPddaListConverter = new CrownFirmPddaListFileConverter();

    @BeforeAll
    void setup() throws IOException {
        StringWriter writer = new StringWriter();
        IOUtils.copy(
            Files.newInputStream(Paths.get(TEST_FILE_PATH, "crownFirmPddaList.json")),
            writer, Charset.defaultCharset()
        );
        inputJson = new ObjectMapper().readTree(writer.toString());
    }

    @Test
    void testCrownFirmPddaListTemplate() throws IOException {
        Map<String, Object> language;

        try (InputStream languageFile = Thread.currentThread()
            .getContextClassLoader().getResourceAsStream("templates/languages/en/crownFirmPddaList.json")) {
            language = new ObjectMapper().readValue(
                Objects.requireNonNull(languageFile).readAllBytes(), new TypeReference<>() {
                });
        }

        Map<String, String> metadataMap = new ConcurrentHashMap<>(COMMON_METADATA);
        metadataMap.put(LANGUAGE, "ENGLISH");
        metadataMap.put(LIST_TYPE, CROWN_FIRM_PDDA_LIST.name());

        String outputHtml = crownFirmPddaListConverter.convert(inputJson, metadataMap, language);
        Document document = Jsoup.parse(outputHtml);

        SoftAssertions softly = new SoftAssertions();

        softly.assertThat(outputHtml)
            .as("No Firm list html found")
            .isNotEmpty();

        softly.assertThat(document.title())
            .as("incorrect Firm list title found.")
            .isEqualTo("Crown Firm List");

        softly.assertThat(document.getElementsByClass(HEADING_CLASS).get(0).text())
            .as(HEADING_MESSAGE)
            .contains("Crown Firm List for location");

        softly.assertThat(document.getElementsByClass(LINK_CLASS).get(0)
                              .getElementsByTag("a").get(0)
                              .attr(HREF))
            .as(LINK_MESSAGE)
            .isEqualTo(FACT_LINK);

        softly.assertThat(document.getElementsByClass(BODY_CLASS).get(0).text())
            .as(LINK_MESSAGE)
            .isEqualTo("Find contact details and other information about courts and tribunals in England "
                           + "and Wales, and some non-devolved tribunals in Scotland.");

        softly.assertThat(document.getElementsByClass(BODY_CLASS).get(1).text())
            .as(BODY_MESSAGE)
            .contains("List for 10 September 2025 to 11 September 2025");

        softly.assertThat(document.getElementsByClass(BODY_CLASS).get(2).text())
            .as(BODY_MESSAGE)
            .contains("Last updated 09 September 2025 at 11am");

        softly.assertThat(document.getElementsByClass(BODY_CLASS).get(3).text())
            .as(BODY_MESSAGE)
            .contains("Version 1.0");

        softly.assertThat(document.getElementsByClass(BODY_CLASS).get(4).text())
            .as(BODY_MESSAGE)
            .contains(ADDRESS);

        softly.assertThat(document.getElementsByClass(BODY_CLASS).get(5).text())
            .as(BODY_MESSAGE)
            .contains("Restrictions on publishing or writing about these cases");

        softly.assertThat(document.getElementsByClass(HEADING_CLASS).get(1).text())
            .as(HEADING_MESSAGE)
            .contains("Wednesday 10 September 2025");

        softly.assertThat(document.getElementsByTag("th"))
            .as(TABLE_HEADERS_MESSAGE)
            .hasSize(21)
            .extracting(Element::text)
            .containsSequence(
                "Hearing Time",
                "Case Number",
                "Defendant Name(s)",
                "Hearing Type",
                "Representative",
                "Prosecuting Authority",
                "Listing Notes"
            );

        softly.assertAll();
    }

    @Test
    void testCrownFirmPddaListTemplateWelsh() throws IOException {
        Map<String, Object> language;

        try (InputStream languageFile = Thread.currentThread()
            .getContextClassLoader().getResourceAsStream("templates/languages/cy/crownFirmPddaList.json")) {
            language = new ObjectMapper().readValue(
                Objects.requireNonNull(languageFile).readAllBytes(), new TypeReference<>() {
                });
        }

        Map<String, String> metadataMap = new ConcurrentHashMap<>(COMMON_METADATA);
        metadataMap.put(LANGUAGE, "WELSH");
        metadataMap.put(LIST_TYPE, CROWN_FIRM_PDDA_LIST.name());

        String outputHtml = crownFirmPddaListConverter.convert(inputJson, metadataMap, language);
        Document document = Jsoup.parse(outputHtml);

        SoftAssertions softly = new SoftAssertions();

        softly.assertThat(outputHtml)
            .as("No Firm list html found")
            .isNotEmpty();

        softly.assertThat(document.title())
            .as("incorrect Firm list title found.")
            .isEqualTo("Rhestr Cwmni Llys y Goron");

        softly.assertThat(document.getElementsByClass(HEADING_CLASS).get(0).text())
            .as(HEADING_MESSAGE)
            .contains("Rhestr Cwmni Llys y Goron ar gyfer location");

        softly.assertThat(document.getElementsByClass(LINK_CLASS).get(0)
                              .getElementsByTag("a").get(0)
                              .attr(HREF))
            .as(LINK_MESSAGE)
            .isEqualTo(FACT_LINK);

        softly.assertThat(document.getElementsByClass(BODY_CLASS).get(0).text())
            .as(LINK_MESSAGE)
            .isEqualTo("Dod o hyd i fanylion cyswllt a gwybodaeth arall am lysoedd a thribiwnlysoedd yng "
                           + "Nghymru a Lloegr a rhai tribiwnlysoedd heb eu datganoli yn yr Alban.");

        softly.assertThat(document.getElementsByClass(BODY_CLASS).get(1).text())
            .as(BODY_MESSAGE)
            .contains("Rhestr ar gyfer 10 September 2025 i 11 September 2025");

        softly.assertThat(document.getElementsByClass(BODY_CLASS).get(2).text())
            .as(BODY_MESSAGE)
            .contains("Diweddarwyd diwethaf 09 September 2025 am 11am");

        softly.assertThat(document.getElementsByClass(BODY_CLASS).get(3).text())
            .as(BODY_MESSAGE)
            .contains("Fersiwn 1.0");

        softly.assertThat(document.getElementsByClass(BODY_CLASS).get(4).text())
            .as(BODY_MESSAGE)
            .contains(ADDRESS);

        softly.assertThat(document.getElementsByClass(BODY_CLASS).get(5).text())
            .as(BODY_MESSAGE)
            .contains("Cyfyngiadau ar gyhoeddi neu ysgrifennu am yr achosion hyn");

        softly.assertThat(document.getElementsByClass(HEADING_CLASS).get(1).text())
            .as(HEADING_MESSAGE)
            .contains("Wednesday 10 September 2025");

        softly.assertAll();
    }

    @Test
    void testCrownFirmPddaListTableContents() throws IOException {
        Map<String, Object> language;

        try (InputStream languageFile = Thread.currentThread()
            .getContextClassLoader().getResourceAsStream("templates/languages/en/crownFirmPddaList.json")) {
            language = new ObjectMapper().readValue(
                Objects.requireNonNull(languageFile).readAllBytes(), new TypeReference<>() {
                }
            );
        }

        Map<String, String> metadataMap = new ConcurrentHashMap<>(COMMON_METADATA);
        metadataMap.put(LANGUAGE, "ENGLISH");
        metadataMap.put(LIST_TYPE, "CROWN_FIRM_PDDA_LIST");

        String outputHtml = crownFirmPddaListConverter.convert(inputJson, metadataMap, language);
        Document document = Jsoup.parse(outputHtml);

        assertThat(document.getElementsByTag("td"))
            .as("Table contents does not match")
            .extracting(Element::text)
            .containsSequence(
                "TestTimeMarkingNote",
                "T00112233",
                "TestMaskedName, Mr TestDefendantForename TestDefendantSurname TestDefendantSuffix",
                "TestHearingDescription",
                "TestSolicitorRequestedName",
                "Crown Prosecution Service",
                "TestListNote"
            );
    }

    @Test
    void testCrownFirmListExcelConversion() throws IOException {
        byte[] result = crownFirmPddaListConverter.convertToExcel(inputJson, CROWN_FIRM_PDDA_LIST,
                                                                   Language.ENGLISH);
        ByteArrayInputStream file = new ByteArrayInputStream(result);
        Workbook workbook = new XSSFWorkbook(file);
        Sheet sheet = workbook.getSheetAt(0);
        Row headingRow = sheet.getRow(0);

        SoftAssertions softly = new SoftAssertions();

        softly.assertThat(sheet.getSheetName())
            .as(EXCEL_SHEET_NAME_MESSAGE)
            .isEqualTo(CROWN_FIRM_PDDA_LIST.getFriendlyName());

        softly.assertThat(headingRow.getCell(0).getStringCellValue())
            .as(EXCEL_TABLE_HEADER_MESSAGE)
            .isEqualTo("Sitting Date");

        softly.assertThat(headingRow.getCell(1).getStringCellValue())
            .as(EXCEL_TABLE_HEADER_MESSAGE)
            .isEqualTo("Court House");

        softly.assertThat(headingRow.getCell(2).getStringCellValue())
            .as(EXCEL_TABLE_HEADER_MESSAGE)
            .isEqualTo("Court Address");

        softly.assertThat(headingRow.getCell(3).getStringCellValue())
            .as(EXCEL_TABLE_HEADER_MESSAGE)
            .isEqualTo("Court Phone Number");

        softly.assertThat(headingRow.getCell(4).getStringCellValue())
            .as(EXCEL_TABLE_HEADER_MESSAGE)
            .isEqualTo("Court Room");

        softly.assertThat(headingRow.getCell(5).getStringCellValue())
            .as(EXCEL_TABLE_HEADER_MESSAGE)
            .isEqualTo("Sitting at");

        softly.assertThat(headingRow.getCell(6).getStringCellValue())
            .as(EXCEL_TABLE_HEADER_MESSAGE)
            .isEqualTo("Hearing Time");

        softly.assertThat(headingRow.getCell(7).getStringCellValue())
            .as(EXCEL_TABLE_HEADER_MESSAGE)
            .isEqualTo("Case Number");

        softly.assertThat(headingRow.getCell(8).getStringCellValue())
            .as(EXCEL_TABLE_HEADER_MESSAGE)
            .isEqualTo("Defendant Name(s)");

        softly.assertThat(headingRow.getCell(9).getStringCellValue())
            .as(EXCEL_TABLE_HEADER_MESSAGE)
            .isEqualTo("Hearing Type");

        softly.assertThat(headingRow.getCell(10).getStringCellValue())
            .as(EXCEL_TABLE_HEADER_MESSAGE)
            .isEqualTo("Representative");

        softly.assertThat(headingRow.getCell(11).getStringCellValue())
            .as(EXCEL_TABLE_HEADER_MESSAGE)
            .isEqualTo("Prosecuting Authority");

        softly.assertThat(headingRow.getCell(12).getStringCellValue())
            .as(EXCEL_TABLE_HEADER_MESSAGE)
            .isEqualTo("Listing Notes");

        Row dataRow = sheet.getRow(1);
        softly.assertThat(dataRow.getCell(0).getStringCellValue())
            .as(EXCEL_CELL_VALUE_MESSAGE)
            .isEqualTo("Wednesday 10 September 2025");

        softly.assertThat(dataRow.getCell(1).getStringCellValue())
            .as(EXCEL_CELL_VALUE_MESSAGE)
            .isEqualTo("TestCourtHouseName");

        softly.assertThat(dataRow.getCell(2).getStringCellValue())
            .as(EXCEL_CELL_VALUE_MESSAGE)
            .isEqualTo("1 Main Road, London, A1 1AA");

        softly.assertThat(dataRow.getCell(3).getStringCellValue())
            .as(EXCEL_CELL_VALUE_MESSAGE)
            .isEqualTo("02071234568");

        softly.assertThat(dataRow.getCell(4).getStringCellValue())
            .as(EXCEL_CELL_VALUE_MESSAGE)
            .isEqualTo("Courtroom 1: TestJudgeRequested, Ms TestJusticeForename TestJusticeSurname Sr");

        softly.assertThat(dataRow.getCell(5).getStringCellValue())
            .as(EXCEL_CELL_VALUE_MESSAGE)
            .isEqualTo("10am");

        softly.assertThat(dataRow.getCell(6).getStringCellValue())
            .as(EXCEL_CELL_VALUE_MESSAGE)
            .isEqualTo("TestTimeMarkingNote");

        softly.assertThat(dataRow.getCell(7).getStringCellValue())
            .as(EXCEL_CELL_VALUE_MESSAGE)
            .isEqualTo("T00112233");

        softly.assertThat(dataRow.getCell(8).getStringCellValue())
            .as(EXCEL_CELL_VALUE_MESSAGE)
            .isEqualTo("TestMaskedName, Mr TestDefendantForename TestDefendantSurname TestDefendantSuffix");

        softly.assertThat(dataRow.getCell(9).getStringCellValue())
            .as(EXCEL_CELL_VALUE_MESSAGE)
            .isEqualTo("TestHearingDescription");

        softly.assertThat(dataRow.getCell(10).getStringCellValue())
            .as(EXCEL_CELL_VALUE_MESSAGE)
            .isEqualTo("TestSolicitorRequestedName");

        softly.assertThat(dataRow.getCell(11).getStringCellValue())
            .as(EXCEL_CELL_VALUE_MESSAGE)
            .isEqualTo("Crown Prosecution Service");

        softly.assertThat(dataRow.getCell(12).getStringCellValue())
            .as(EXCEL_CELL_VALUE_MESSAGE)
            .isEqualTo("TestListNote");

        softly.assertAll();
    }

    @Test
    void testCrownFirmListWelshExcelConversion() throws IOException {
        byte[] result = crownFirmPddaListConverter.convertToExcel(inputJson, CROWN_FIRM_PDDA_LIST,
                                                                   Language.WELSH);
        ByteArrayInputStream file = new ByteArrayInputStream(result);
        Workbook workbook = new XSSFWorkbook(file);
        Sheet sheet = workbook.getSheetAt(0);
        Row headingRow = sheet.getRow(0);

        SoftAssertions softly = new SoftAssertions();

        softly.assertThat(sheet.getSheetName())
            .as(EXCEL_SHEET_NAME_MESSAGE)
            .isEqualTo(CROWN_FIRM_PDDA_LIST.getFriendlyName());

        softly.assertThat(headingRow.getCell(0).getStringCellValue())
            .as(EXCEL_TABLE_HEADER_MESSAGE)
            .isEqualTo("Dyddiad yr Eisteddiad");

        softly.assertThat(headingRow.getCell(1).getStringCellValue())
            .as(EXCEL_TABLE_HEADER_MESSAGE)
            .isEqualTo("Llys");

        softly.assertThat(headingRow.getCell(2).getStringCellValue())
            .as(EXCEL_TABLE_HEADER_MESSAGE)
            .isEqualTo("Cyfeiriad y Llys");

        softly.assertThat(headingRow.getCell(3).getStringCellValue())
            .as(EXCEL_TABLE_HEADER_MESSAGE)
            .isEqualTo("Rhif ffôn y Llys");

        softly.assertThat(headingRow.getCell(4).getStringCellValue())
            .as(EXCEL_TABLE_HEADER_MESSAGE)
            .isEqualTo("Ystafell Llys");

        softly.assertThat(headingRow.getCell(5).getStringCellValue())
            .as(EXCEL_TABLE_HEADER_MESSAGE)
            .isEqualTo("Yn eistedd yn");

        softly.assertThat(headingRow.getCell(6).getStringCellValue())
            .as(EXCEL_TABLE_HEADER_MESSAGE)
            .isEqualTo("Amser y Gwrandawiad");

        softly.assertThat(headingRow.getCell(7).getStringCellValue())
            .as(EXCEL_TABLE_HEADER_MESSAGE)
            .isEqualTo("Cyfeirnod yr Achos");

        softly.assertThat(headingRow.getCell(8).getStringCellValue())
            .as(EXCEL_TABLE_HEADER_MESSAGE)
            .isEqualTo("Enw'r Diffynnydd(Diffynyddion)");

        softly.assertThat(headingRow.getCell(9).getStringCellValue())
            .as(EXCEL_TABLE_HEADER_MESSAGE)
            .isEqualTo("Math o Wrandawiad");

        softly.assertThat(headingRow.getCell(10).getStringCellValue())
            .as(EXCEL_TABLE_HEADER_MESSAGE)
            .isEqualTo("Cynrychiolir gan");

        softly.assertThat(headingRow.getCell(11).getStringCellValue())
            .as(EXCEL_TABLE_HEADER_MESSAGE)
            .isEqualTo("Yr Awdurdod sy'n Erlyn");

        softly.assertThat(headingRow.getCell(12).getStringCellValue())
            .as(EXCEL_TABLE_HEADER_MESSAGE)
            .isEqualTo("Nodiadau Rhestru");

        softly.assertAll();
    }
}
