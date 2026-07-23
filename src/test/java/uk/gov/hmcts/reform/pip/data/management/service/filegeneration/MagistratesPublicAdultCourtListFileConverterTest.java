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
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.pip.model.publication.Language;
import uk.gov.hmcts.reform.pip.model.publication.ListType;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.Map;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class MagistratesPublicAdultCourtListFileConverterTest {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private static final String CONTENT_DATE = "contentDate";
    private static final String PROVENANCE = "provenance";
    private static final String LOCATION_NAME = "locationName";
    private static final String LANGUAGE = "language";
    private static final String LIST_TYPE = "listType";
    private static final String FACT_LINK = "https://www.find-court-tribunal.service.gov.uk/";

    private static final String HTML_MESSAGE = "No html found";
    private static final String TITLE_MESSAGE = "Incorrect title found";
    private static final String LINK_MESSAGE = "Link does not match";
    private static final String HEADER_MESSAGE = "Incorrect header text";
    private static final String BODY_MESSAGE = "Incorrect body text";
    private static final String COURT_ROOM_HEADING_MESSAGE = "Court room heading does not match";
    private static final String EXCEL_TABLE_HEADER_MESSAGE = "Excel table header does not match";
    private static final String EXCEL_CELL_VALUE_MESSAGE = "Excel cell value does not match";

    private static final String MAGISTRATES_PUBLIC_ADULT_COURT_LIST_DAILY = "MAGISTRATES_PUBLIC_ADULT_COURT_LIST_DAILY";
    private static final String MAGISTRATES_PUBLIC_ADULT_COURT_LIST_FUTURE =
        "MAGISTRATES_PUBLIC_ADULT_COURT_LIST_FUTURE";

    private static final String GOVUK_HEADING_L = "govuk-heading-l";
    private static final String BODY_CLASS = "govuk-body";
    private static final String LINK_CLASS = "govuk-link";
    private static final String HREF = "href";

    private final MagistratesPublicAdultCourtListFileConverter converter =
        new MagistratesPublicAdultCourtListFileConverter();

    private JsonNode inputJson;
    private Map<String, Object> englishLanguageResource;
    private Map<String, Object> welshLanguageResource;

    @BeforeAll
    void setup() throws IOException {
        try (InputStream inputStream = getClass().getResourceAsStream("/mocks/magistratesPublicAdultCourtList.json")) {
            String inputRaw = IOUtils.toString(inputStream, Charset.defaultCharset());
            inputJson = OBJECT_MAPPER.readTree(inputRaw);
        }

        try (InputStream languageFile = Thread.currentThread()
            .getContextClassLoader()
            .getResourceAsStream("templates/languages/en/magistratesPublicAdultCourtListDaily.json")) {
            englishLanguageResource = OBJECT_MAPPER.readValue(
                Objects.requireNonNull(languageFile).readAllBytes(), new TypeReference<>() {
                });
        }

        try (InputStream languageFile = Thread.currentThread()
            .getContextClassLoader()
            .getResourceAsStream("templates/languages/cy/magistratesPublicAdultCourtListDaily.json")) {
            welshLanguageResource = OBJECT_MAPPER.readValue(
                Objects.requireNonNull(languageFile).readAllBytes(), new TypeReference<>() {
                });
        }
    }

    private Map<String, String> createMetaDataMap(ListType listType, Language language) {
        return Map.of(
            CONTENT_DATE, "1 August 2025",
            PROVENANCE, PROVENANCE,
            LOCATION_NAME, "location",
            LANGUAGE, language.name(),
            LIST_TYPE, listType.name()
        );
    }

    @ParameterizedTest
    @EnumSource(value = ListType.class, names = {MAGISTRATES_PUBLIC_ADULT_COURT_LIST_DAILY,
        MAGISTRATES_PUBLIC_ADULT_COURT_LIST_FUTURE})
    void testPublicGeneralListInformationInEnglish(ListType listType) throws IOException {
        Map<String, String> metadata = createMetaDataMap(listType, Language.ENGLISH);
        String outputHtml = converter.convert(inputJson, metadata, englishLanguageResource);
        Document document = Jsoup.parse(outputHtml);
        SoftAssertions softly = new SoftAssertions();

        softly.assertThat(outputHtml)
            .as(HTML_MESSAGE)
            .isNotEmpty();

        softly.assertThat(document.title())
            .as(TITLE_MESSAGE)
            .isEqualTo("Magistrates Public List");

        softly.assertThat(document.getElementsByClass(GOVUK_HEADING_L).get(0).text())
            .as(HEADER_MESSAGE)
            .contains("Magistrates Public List for location");

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
            .isEqualTo("List for 1 August 2025");

        softly.assertThat(document.getElementsByClass(BODY_CLASS).get(2).text())
            .as(BODY_MESSAGE)
            .isEqualTo("Last updated 31 July 2025 at 9:05am");

        softly.assertThat(document.getElementsByClass(BODY_CLASS).get(3).text())
            .as(BODY_MESSAGE)
            .contains("Restrictions on publishing or writing about these cases");

        softly.assertAll();
    }

    @ParameterizedTest
    @EnumSource(value = ListType.class, names = {MAGISTRATES_PUBLIC_ADULT_COURT_LIST_DAILY,
        MAGISTRATES_PUBLIC_ADULT_COURT_LIST_FUTURE})
    void testPublicGeneralListInformationInWelsh(ListType listType) throws IOException {
        Map<String, String> metadata = createMetaDataMap(listType, Language.WELSH);
        String outputHtml = converter.convert(inputJson, metadata, welshLanguageResource);
        Document document = Jsoup.parse(outputHtml);
        SoftAssertions softly = new SoftAssertions();

        softly.assertThat(outputHtml)
            .as(HTML_MESSAGE)
            .isNotEmpty();

        softly.assertThat(document.title())
            .as(TITLE_MESSAGE)
            .isEqualTo("Rhestr Gyhoeddus y Llys Ynadon");

        softly.assertThat(document.getElementsByClass(GOVUK_HEADING_L).get(0).text())
            .as(HEADER_MESSAGE)
            .contains("Rhestr Gyhoeddus y Llys Ynadon ar gyfer location");

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
            .isEqualTo("Rhestr ar gyfer 1 August 2025");

        softly.assertThat(document.getElementsByClass(BODY_CLASS).get(2).text())
            .as(BODY_MESSAGE)
            .isEqualTo("Diweddarwyd diwethaf 31 July 2025 am 9:05am");

        softly.assertThat(document.getElementsByClass(BODY_CLASS).get(3).text())
            .as(BODY_MESSAGE)
            .contains("Cyfyngiadau ar gyhoeddi neu ysgrifennu am yr achosion hyn");

        softly.assertAll();
    }

    @ParameterizedTest
    @EnumSource(value = ListType.class, names = {MAGISTRATES_PUBLIC_ADULT_COURT_LIST_DAILY,
        MAGISTRATES_PUBLIC_ADULT_COURT_LIST_FUTURE})
    void testPublicSessionHeadings(ListType listType) throws IOException {
        Map<String, String> metadata = createMetaDataMap(listType, Language.ENGLISH);
        String result = converter.convert(inputJson, metadata, englishLanguageResource);
        Document document = Jsoup.parse(result);

        SoftAssertions softly = new SoftAssertions();

        softly.assertThat(document.getElementsByClass("site-header"))
            .as("Session heading count does not match")
            .hasSize(6);

        String firstSessionHeading = document.getElementsByClass("site-header").get(0).text();
        softly.assertThat(firstSessionHeading)
            .as(COURT_ROOM_HEADING_MESSAGE)
            .contains("North Shields Magistrates' Court");

        softly.assertThat(firstSessionHeading)
            .as(COURT_ROOM_HEADING_MESSAGE)
            .contains("Sitting at Courtroom 1");

        softly.assertThat(firstSessionHeading)
            .as(COURT_ROOM_HEADING_MESSAGE)
            .contains("LJA: North Northumbria Magistrates' Court");

        softly.assertThat(firstSessionHeading)
            .as(COURT_ROOM_HEADING_MESSAGE)
            .contains("Session start 9am");

        softly.assertAll();
    }

    @ParameterizedTest
    @EnumSource(value = ListType.class, names = {MAGISTRATES_PUBLIC_ADULT_COURT_LIST_DAILY,
        MAGISTRATES_PUBLIC_ADULT_COURT_LIST_FUTURE})
    void testPdfPublicTableHeaders(ListType listType) throws IOException {
        Map<String, String> metadata = createMetaDataMap(listType, Language.ENGLISH);
        String outputHtml = converter.convert(inputJson, metadata, englishLanguageResource);
        Document document = Jsoup.parse(outputHtml);

        assertThat(document.getElementsByClass("govuk-table__head").get(0)
                       .getElementsByTag("th"))
            .as("Incorrect table headers")
            .hasSize(3)
            .extracting(Element::text)
            .containsExactly(
                "Listing Time",
                "Defendant Name",
                "Case Number"
            );
    }

    @ParameterizedTest
    @EnumSource(value = ListType.class, names = {MAGISTRATES_PUBLIC_ADULT_COURT_LIST_DAILY,
        MAGISTRATES_PUBLIC_ADULT_COURT_LIST_FUTURE})
    void testPdfPublicTableContents(ListType listType) throws IOException {
        Map<String, String> metadata = createMetaDataMap(listType, Language.ENGLISH);
        String outputHtml = converter.convert(inputJson, metadata, englishLanguageResource);
        Document document = Jsoup.parse(outputHtml);
        assertThat(document.getElementsByClass("govuk-table__body").get(0)
                       .getElementsByTag("td"))
            .as("Incorrect table body")
            .hasSize(6)
            .extracting(Element::text)
            .contains(
                "9am",
                "Mr Test User",
                "1000000000"
            );
    }

    @ParameterizedTest
    @EnumSource(value = ListType.class, names = {MAGISTRATES_PUBLIC_ADULT_COURT_LIST_DAILY,
        MAGISTRATES_PUBLIC_ADULT_COURT_LIST_FUTURE})
    void testStandardExcelConversion(ListType listType) throws IOException {
        byte[] result = converter.convertToExcel(inputJson, listType, Language.ENGLISH);
        ByteArrayInputStream file = new ByteArrayInputStream(result);
        Workbook workbook = new XSSFWorkbook(file);
        Sheet sheet = workbook.getSheetAt(0);
        Row headingRow = sheet.getRow(0);

        SoftAssertions softly = new SoftAssertions();

        softly.assertThat(headingRow.getCell(0).getStringCellValue())
            .as(EXCEL_TABLE_HEADER_MESSAGE)
            .isEqualTo("Court House");

        softly.assertThat(headingRow.getCell(1).getStringCellValue())
            .as(EXCEL_TABLE_HEADER_MESSAGE)
            .isEqualTo("Sitting at");

        softly.assertThat(headingRow.getCell(2).getStringCellValue())
            .as(EXCEL_TABLE_HEADER_MESSAGE)
            .isEqualTo("LJA");

        softly.assertThat(headingRow.getCell(3).getStringCellValue())
            .as(EXCEL_TABLE_HEADER_MESSAGE)
            .isEqualTo("Session start");

        softly.assertThat(headingRow.getCell(4).getStringCellValue())
            .as(EXCEL_TABLE_HEADER_MESSAGE)
            .isEqualTo("Listing Time");

        softly.assertThat(headingRow.getCell(5).getStringCellValue())
            .as(EXCEL_TABLE_HEADER_MESSAGE)
            .isEqualTo("Defendant Name");

        softly.assertThat(headingRow.getCell(6).getStringCellValue())
            .as(EXCEL_TABLE_HEADER_MESSAGE)
            .isEqualTo("Case Number");

        softly.assertAll();
    }

    @ParameterizedTest
    @EnumSource(value = ListType.class, names = {MAGISTRATES_PUBLIC_ADULT_COURT_LIST_DAILY,
        MAGISTRATES_PUBLIC_ADULT_COURT_LIST_FUTURE})
    void testStandardWelshExcelConversion(ListType listType) throws IOException {
        byte[] result = converter.convertToExcel(inputJson, listType, Language.WELSH);
        ByteArrayInputStream file = new ByteArrayInputStream(result);
        Workbook workbook = new XSSFWorkbook(file);
        Sheet sheet = workbook.getSheetAt(0);
        Row headingRow = sheet.getRow(0);

        SoftAssertions softly = new SoftAssertions();

        softly.assertThat(headingRow.getCell(0).getStringCellValue())
            .as(EXCEL_TABLE_HEADER_MESSAGE)
            .isEqualTo("Llys");

        softly.assertThat(headingRow.getCell(1).getStringCellValue())
            .as(EXCEL_TABLE_HEADER_MESSAGE)
            .isEqualTo("Yn eistedd yn");

        softly.assertThat(headingRow.getCell(2).getStringCellValue())
            .as(EXCEL_TABLE_HEADER_MESSAGE)
            .isEqualTo("LJA");

        softly.assertThat(headingRow.getCell(3).getStringCellValue())
            .as(EXCEL_TABLE_HEADER_MESSAGE)
            .isEqualTo("Amser cychwyn y sesiwn");

        softly.assertThat(headingRow.getCell(4).getStringCellValue())
            .as(EXCEL_TABLE_HEADER_MESSAGE)
            .isEqualTo("Amser rhestru");

        softly.assertThat(headingRow.getCell(5).getStringCellValue())
            .as(EXCEL_TABLE_HEADER_MESSAGE)
            .isEqualTo("Enw'r Diffynnydd");

        softly.assertThat(headingRow.getCell(6).getStringCellValue())
            .as(EXCEL_TABLE_HEADER_MESSAGE)
            .isEqualTo("Cyfeirnod yr Achos");

        softly.assertAll();
    }

    @ParameterizedTest
    @EnumSource(value = ListType.class, names = {MAGISTRATES_PUBLIC_ADULT_COURT_LIST_DAILY,
        MAGISTRATES_PUBLIC_ADULT_COURT_LIST_FUTURE})
    void testStandardExcelTableContents(ListType listType) throws IOException {
        byte[] result = converter.convertToExcel(inputJson, listType, Language.ENGLISH);
        ByteArrayInputStream file = new ByteArrayInputStream(result);
        Workbook workbook = new XSSFWorkbook(file);
        Sheet sheet = workbook.getSheetAt(0);

        SoftAssertions softly = new SoftAssertions();

        Row dataRow = sheet.getRow(1);
        softly.assertThat(dataRow.getCell(0).getStringCellValue())
            .as(EXCEL_CELL_VALUE_MESSAGE)
            .isEqualTo("North Shields Magistrates' Court");

        softly.assertThat(dataRow.getCell(1).getStringCellValue())
            .as(EXCEL_CELL_VALUE_MESSAGE)
            .isEqualTo("Courtroom 1");

        softly.assertThat(dataRow.getCell(2).getStringCellValue())
            .as(EXCEL_CELL_VALUE_MESSAGE)
            .isEqualTo("North Northumbria Magistrates' Court");

        softly.assertThat(dataRow.getCell(3).getStringCellValue())
            .as(EXCEL_CELL_VALUE_MESSAGE)
            .isEqualTo("9am");

        softly.assertThat(dataRow.getCell(4).getStringCellValue())
            .as(EXCEL_CELL_VALUE_MESSAGE)
            .isEqualTo("9am");

        softly.assertThat(dataRow.getCell(5).getStringCellValue())
            .as(EXCEL_CELL_VALUE_MESSAGE)
            .isEqualTo("Mr Test User");

        softly.assertThat(dataRow.getCell(6).getStringCellValue())
            .as(EXCEL_CELL_VALUE_MESSAGE)
            .isEqualTo("1000000000");

        softly.assertAll();
    }
}
