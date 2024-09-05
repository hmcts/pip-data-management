package uk.gov.hmcts.reform.pip.data.management.controllers.tests.service.filegeneration;

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
import org.jsoup.select.Elements;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.pip.data.management.service.filegeneration.OpaPressListFileConverter;
import uk.gov.hmcts.reform.pip.model.publication.ListType;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.Map;
import java.util.Objects;

@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SuppressWarnings({"PMD.TooManyMethods", "PMD.LooseCoupling"})
class OpaPressListFileConverterTest {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final String CONTENT_DATE = "10 October 2023";
    private static final String LOCATION_NAME = "Location name";
    private static final String ENGLISH = "ENGLISH";
    private static final String LIST_TYPE = "OPA_PRESS_LIST";

    private static final String HEADING_CLASS = "govuk-heading-l";
    private static final String DEFENDANT_HEADING_CLASS = "govuk-heading-m";
    private static final String BODY_CLASS = "govuk-body";

    private static final String TITLE_MESSAGE = "Title does not match";
    private static final String HEADING_MESSAGE = "Heading does not match";
    private static final String CONTENT_DATE_MESSAGE = "Content date does not match";
    private static final String PUBLICATION_DATE_MESSAGE = "Publication date does not match";
    private static final String VERSION_MESSAGE = "Version does not match";
    private static final String ADDRESS_MESSAGE = "Address does not match";
    private static final String DEFENDANT_HEADING_MESSAGE = "Defendant heading does not match";
    private static final String DEFENDANT_INFO_MESSAGE = "Defendant info does not match";
    private static final String OFFENCE_INFO_MESSAGE = "Offence info does not match";

    private static final Map<String, String> METADATA = Map.of("contentDate", CONTENT_DATE,
                                                               "locationName", LOCATION_NAME,
                                                               "language", ENGLISH,
                                                               "listType", LIST_TYPE
    );

    private final OpaPressListFileConverter converter = new OpaPressListFileConverter();

    private JsonNode inputJson;
    private Map<String, Object> englishLanguageResource;
    private Map<String, Object> welshLanguageResource;

    @BeforeAll
    void setup() throws IOException {
        try (InputStream inputStream = getClass().getResourceAsStream("/mocks/opaPressList.json")) {
            String inputRaw = IOUtils.toString(inputStream, Charset.defaultCharset());
            inputJson = OBJECT_MAPPER.readTree(inputRaw);
        }

        try (InputStream languageFile = Thread.currentThread()
            .getContextClassLoader()
            .getResourceAsStream("templates/languages/en/opaPressList.json")) {
            englishLanguageResource = OBJECT_MAPPER.readValue(
                Objects.requireNonNull(languageFile).readAllBytes(), new TypeReference<>() {
                });
        }

        try (InputStream languageFile = Thread.currentThread()
            .getContextClassLoader()
            .getResourceAsStream("templates/languages/cy/opaPressList.json")) {
            welshLanguageResource = OBJECT_MAPPER.readValue(
                Objects.requireNonNull(languageFile).readAllBytes(), new TypeReference<>() {
                });
        }
    }

    @Test
    void testGeneralListInformationInEnglish() {
        String result = converter.convert(inputJson, METADATA, englishLanguageResource);
        Document document = Jsoup.parse(result);
        SoftAssertions softly = new SoftAssertions();

        softly.assertThat(document.title())
            .as(TITLE_MESSAGE)
            .isEqualTo("OPA Press List");

        softly.assertThat(document.getElementsByClass(HEADING_CLASS).get(0).text())
            .as(HEADING_MESSAGE)
            .contains("Online Plea and Allocation Cases Press View for " + LOCATION_NAME);

        softly.assertThat(document.getElementsByTag("a"))
            .as("Incorrect anchor title")
            .hasSize(1)
            .extracting(element -> element.attr("title"))
            .containsExactly("Protocol on sharing court lists, registers and documents with the media");

        Elements resultBody = document.getElementsByClass(BODY_CLASS);
        softly.assertThat(resultBody.get(0).text())
            .as(CONTENT_DATE_MESSAGE)
            .isEqualTo("List for 10 October 2023");

        softly.assertThat(resultBody.get(1).text())
            .as(PUBLICATION_DATE_MESSAGE)
            .isEqualTo("Last updated: 14 September 2023 at 12:30am");

        softly.assertThat(resultBody.get(2).text())
            .as(VERSION_MESSAGE)
            .isEqualTo("Draft: Version 1.0");

        softly.assertThat(resultBody.get(3).text())
            .as(ADDRESS_MESSAGE)
            .contains("AA1 1AA");

        softly.assertAll();
    }

    @Test
    void testGeneralListInformationInWelsh() {
        String result = converter.convert(inputJson, METADATA, welshLanguageResource);
        Document document = Jsoup.parse(result);
        SoftAssertions softly = new SoftAssertions();

        softly.assertThat(document.title())
            .as(TITLE_MESSAGE)
            .isEqualTo("Rhestr y Wasg – OPA");

        softly.assertThat(document.getElementsByClass(HEADING_CLASS).get(0).text())
            .as(HEADING_MESSAGE)
            .contains("Achosion Pledio Ar-lein a Dyrannu – Rhestr y Wasg ar gyfer " + LOCATION_NAME);

        softly.assertThat(document.getElementsByTag("a"))
            .as("Incorrect anchor title")
            .hasSize(1)
            .extracting(element -> element.attr("title"))
            .containsExactly("Protocol ar rannu rhestrau’r llys a dogfennau gyda’r cyfryngau");

        Elements resultBody = document.getElementsByClass(BODY_CLASS);
        softly.assertThat(resultBody.get(0).text())
            .as(CONTENT_DATE_MESSAGE)
            .isEqualTo("Rhestr ar gyfer 10 October 2023");

        softly.assertThat(resultBody.get(1).text())
            .as(PUBLICATION_DATE_MESSAGE)
            .isEqualTo("Diweddarwyd diwethaf: 14 September 2023 am 12:30am");

        softly.assertThat(resultBody.get(2).text())
            .as(VERSION_MESSAGE)
            .isEqualTo("Drafft: Fersiwn 1.0");

        softly.assertThat(resultBody.get(3).text())
            .as(ADDRESS_MESSAGE)
            .contains("AA1 1AA");

        softly.assertAll();
    }

    @Test
    void testMediaProtocolInformation() {
        String result = converter.convert(inputJson, METADATA, englishLanguageResource);
        Document document = Jsoup.parse(result);
        SoftAssertions softly = new SoftAssertions();

        Elements linkElements = document.getElementsByClass("govuk-link");
        softly.assertThat(linkElements.get(0).text())
            .as("Media protocol link message does not match")
            .contains("Protocol on sharing court lists, registers and documents with the media");

        softly.assertThat(linkElements.get(0).attr("href"))
            .as("Media protocol link does not match")
            .contains("https://www.gov.uk/government/publications/guidance-to-staff-on-supporting-media-access-to-"
                          + "courts-and-tribunals/protocol-on-sharing-court-lists-registers-and-documents-with-the-"
                          + "media-accessible-version");

        softly.assertAll();
    }

    @Test
    void testReportingRestriction() {
        String result = converter.convert(inputJson, METADATA, englishLanguageResource);
        Document document = Jsoup.parse(result);
        SoftAssertions softly = new SoftAssertions();

        softly.assertThat(document.getElementsByClass("restriction-list-section").get(0).getElementsByTag("h3"))
            .as("reporting restriction title does not match")
            .extracting(Element::text)
            .contains("Restrictions on publishing or writing about these cases");

        softly.assertThat(document.getElementsByClass("govuk-warning-text__text").get(0).text())
            .as("Reporting restriction warning does not match")
            .contains("You'll be in contempt of court if you publish any information which is protected by a "
                          + "reporting restriction. You could get a fine, prison sentence or both.");

        softly.assertAll();
    }

    @Test
    void testPleaDateHeading() {
        String result = converter.convert(inputJson, METADATA, englishLanguageResource);
        Document document = Jsoup.parse(result);
        Elements heading = document.getElementsByClass(HEADING_CLASS);
        SoftAssertions softly = new SoftAssertions();

        softly.assertThat(heading)
            .as(HEADING_MESSAGE)
            .hasSize(4);

        softly.assertThat(heading.get(1).text())
            .as(HEADING_MESSAGE)
            .contains("Open Pleas registered: 22/09/2023");

        softly.assertThat(heading.get(2).text())
            .as(HEADING_MESSAGE)
            .contains("Open Pleas registered: 21/09/2023");

        softly.assertThat(heading.get(3).text())
            .as(HEADING_MESSAGE)
            .contains("Open Pleas registered: 20/09/2023");

        softly.assertAll();
    }

    @Test
    void testDefendantHeading() {
        String result = converter.convert(inputJson, METADATA, englishLanguageResource);
        Document document = Jsoup.parse(result);
        Elements heading = document.getElementsByClass(DEFENDANT_HEADING_CLASS);
        SoftAssertions softly = new SoftAssertions();

        final String defendantName = "Defendant Name: Surname2, Forename2 MiddleName2";
        final String organisationName = "Defendant Name: Organisation name";

        softly.assertThat(heading)
            .as(DEFENDANT_HEADING_MESSAGE)
            .hasSize(11);


        softly.assertThat(heading.get(0).text())
            .as(DEFENDANT_HEADING_MESSAGE)
            .contains(defendantName);

        softly.assertThat(heading.get(1).text())
            .as(DEFENDANT_HEADING_MESSAGE)
            .contains(defendantName);

        softly.assertThat(heading.get(2).text())
            .as(DEFENDANT_HEADING_MESSAGE)
            .contains(defendantName);

        softly.assertThat(heading.get(3).text())
            .as(DEFENDANT_HEADING_MESSAGE)
            .contains(defendantName);

        softly.assertThat(heading.get(4).text())
            .as(DEFENDANT_HEADING_MESSAGE)
            .contains(defendantName);

        softly.assertThat(heading.get(5).text())
            .as(DEFENDANT_HEADING_MESSAGE)
            .contains(defendantName);

        softly.assertThat(heading.get(6).text())
            .as(DEFENDANT_HEADING_MESSAGE)
            .contains(organisationName);

        softly.assertThat(heading.get(7).text())
            .as(DEFENDANT_HEADING_MESSAGE)
            .contains(organisationName);

        softly.assertThat(heading.get(8).text())
            .as(DEFENDANT_HEADING_MESSAGE)
            .contains(organisationName);

        softly.assertThat(heading.get(9).text())
            .as(DEFENDANT_HEADING_MESSAGE)
            .contains("Defendant Name: Surname, Forename MiddleName");

        softly.assertThat(heading.get(10).text())
            .as(DEFENDANT_HEADING_MESSAGE)
            .contains("Defendant Name: Surname, Forename MiddleName");

        softly.assertAll();
    }

    @Test
    void testCaseContents() {
        String result = converter.convert(inputJson, METADATA, englishLanguageResource);
        Document document = Jsoup.parse(result);
        Elements body = document.getElementsByClass(BODY_CLASS);
        SoftAssertions softly = new SoftAssertions();

        softly.assertThat(body.get(11).text())
            .as(DEFENDANT_INFO_MESSAGE)
            .contains("DOB and Age:");

        softly.assertThat(body.get(11).text())
            .as(DEFENDANT_INFO_MESSAGE)
            .contains("01/01/1985 Age: 38");

        softly.assertThat(body.get(12).text())
            .as(DEFENDANT_INFO_MESSAGE)
            .contains("Address:");

        softly.assertThat(body.get(12).text())
            .as(DEFENDANT_INFO_MESSAGE)
            .contains("Address Line 1, Address Line 2, Town, County, BB1 1BB");

        softly.assertThat(body.get(13).text())
            .as(DEFENDANT_INFO_MESSAGE)
            .contains("Prosecuting Authority:");

        softly.assertThat(body.get(13).text())
            .as(DEFENDANT_INFO_MESSAGE)
            .contains("Prosecuting authority ref");

        softly.assertThat(body.get(14).text())
            .as(DEFENDANT_INFO_MESSAGE)
            .contains("Scheduled First Hearing:");

        softly.assertThat(body.get(14).text())
            .as(DEFENDANT_INFO_MESSAGE)
            .contains("01/10/2023");

        softly.assertThat(body.get(15).text())
            .as(DEFENDANT_INFO_MESSAGE)
            .contains("Case Ref / URN:");

        softly.assertThat(body.get(15).text())
            .as(DEFENDANT_INFO_MESSAGE)
            .contains("URN8888");

        softly.assertThat(body.get(16).text())
            .as(DEFENDANT_INFO_MESSAGE)
            .contains("Case Reporting Restriction:");

        softly.assertThat(body.get(16).text())
            .as(DEFENDANT_INFO_MESSAGE)
            .contains("Yes - Case reporting Restriction detail line 1, Case reporting restriction detail line 2");

        softly.assertAll();
    }

    @Test
    void testOffenceContents() {
        String result = converter.convert(inputJson, METADATA, englishLanguageResource);
        Document document = Jsoup.parse(result);
        SoftAssertions softly = new SoftAssertions();

        softly.assertThat(document.getElementsByClass("govuk-details__summary-text").get(1).text())
            .as(OFFENCE_INFO_MESSAGE)
            .contains("1. Offence title 2 - Offence section 2");

        Elements tableCell = document.getElementsByClass("govuk-table__cell");
        softly.assertThat(tableCell.get(0).text())
            .as(OFFENCE_INFO_MESSAGE)
            .contains("Indicated Plea");

        softly.assertThat(tableCell.get(1).text())
            .as(OFFENCE_INFO_MESSAGE)
            .contains("NOT_GUILTY");

        softly.assertThat(tableCell.get(2).text())
            .as(OFFENCE_INFO_MESSAGE)
            .contains("Date of Indicated Plea");

        softly.assertThat(tableCell.get(3).text())
            .as(OFFENCE_INFO_MESSAGE)
            .contains("22/09/2023");

        softly.assertThat(tableCell.get(4).text())
            .as(OFFENCE_INFO_MESSAGE)
            .contains("Offence Reporting Restriction");

        softly.assertThat(tableCell.get(5).text())
            .as(OFFENCE_INFO_MESSAGE)
            .contains("Offence reporting restriction detail 1");

        softly.assertThat(document.getElementsByClass("offence-wording").get(0).text())
            .as(OFFENCE_INFO_MESSAGE)
            .contains("Offence wording 2");

        softly.assertAll();
    }

    @Test
    void testSuccessfulExcelConversionCorrectRowAndColumnCount() throws IOException {
        byte[] result = converter.convertToExcel(inputJson, ListType.OPA_PRESS_LIST);

        ByteArrayInputStream file = new ByteArrayInputStream(result);
        Workbook workbook = new XSSFWorkbook(file);

        Sheet sheet = workbook.getSheetAt(0);

        SoftAssertions softly = new SoftAssertions();

        softly.assertThat(sheet.getSheetName())
            .as("Incorrect sheet name")
            .isEqualTo("OPA Press List");

        softly.assertThat(sheet.getLastRowNum())
            .as("Incorrect row count")
            .isEqualTo(11);

        softly.assertThat(sheet.getRow(0).getLastCellNum())
            .as("Incorrect column count")
            .isEqualTo((short) 19);

        softly.assertAll();
    }

    @Test
    void testSuccessfulExcelConversionColumnHeadings() throws IOException {
        byte[] result = converter.convertToExcel(inputJson, ListType.OPA_PRESS_LIST);

        ByteArrayInputStream file = new ByteArrayInputStream(result);
        Workbook workbook = new XSSFWorkbook(file);

        Sheet sheet = workbook.getSheetAt(0);
        Row row = sheet.getRow(0);
        SoftAssertions softly = new SoftAssertions();

        softly.assertThat(row.getCell(0).getStringCellValue())
            .as("Incorrect URN column")
            .isEqualTo("Unique Reference Number (URN)");

        softly.assertThat(row.getCell(1).getStringCellValue())
            .as("Incorrect Name column")
            .isEqualTo("Name");

        softly.assertThat(row.getCell(2).getStringCellValue())
            .as("Incorrect Address column")
            .isEqualTo("Address");

        softly.assertThat(row.getCell(3).getStringCellValue())
            .as("Incorrect DOB column")
            .isEqualTo("DOB");

        softly.assertThat(row.getCell(4).getStringCellValue())
            .as("Incorrect Prosecution column")
            .isEqualTo("Prosecution");

        softly.assertThat(row.getCell(5).getStringCellValue())
            .as("Incorrect Scheduled First Hearing column")
            .isEqualTo("Scheduled first hearing");

        softly.assertThat(row.getCell(6).getStringCellValue())
            .as("Incorrect Case Reporting Restriction column")
            .isEqualTo("Case Reporting Restriction");

        softly.assertAll();
    }

    @Test
    void testSuccessfulExcelConversionOffenceColumnHeadings() throws IOException {
        byte[] result = converter.convertToExcel(inputJson, ListType.OPA_PRESS_LIST);

        ByteArrayInputStream file = new ByteArrayInputStream(result);
        Workbook workbook = new XSSFWorkbook(file);

        Sheet sheet = workbook.getSheetAt(0);
        Row row = sheet.getRow(0);
        SoftAssertions softly = new SoftAssertions();

        softly.assertThat(row.getCell(7).getStringCellValue())
            .as("Incorrect Offence [1] Title column")
            .isEqualTo("Offence[1] - Title");

        softly.assertThat(row.getCell(8).getStringCellValue())
            .as("Incorrect Offence [1] Section column")
            .isEqualTo("Offence[1] - Section");

        softly.assertThat(row.getCell(9).getStringCellValue())
            .as("Incorrect Offence [1] Reporting Restriction column")
            .isEqualTo("Offence[1] - Reporting Restriction");

        softly.assertThat(row.getCell(10).getStringCellValue())
            .as("Incorrect Offence [1] Plea column")
            .isEqualTo("Offence[1] - Plea");

        softly.assertThat(row.getCell(11).getStringCellValue())
            .as("Incorrect Offence [1] Plea Date column")
            .isEqualTo("Offence[1] - Plea date");

        softly.assertThat(row.getCell(12).getStringCellValue())
            .as("Incorrect Offence [1] Detail column")
            .isEqualTo("Offence[1] - Detail");

        softly.assertThat(row.getCell(13).getStringCellValue())
            .as("Incorrect Offence [2] Title column")
            .isEqualTo("Offence[2] - Title");

        softly.assertThat(row.getCell(14).getStringCellValue())
            .as("Incorrect Offence [2] Section column")
            .isEqualTo("Offence[2] - Section");

        softly.assertThat(row.getCell(15).getStringCellValue())
            .as("Incorrect Offence [2] Reporting Restriction column")
            .isEqualTo("Offence[2] - Reporting Restriction");

        softly.assertThat(row.getCell(16).getStringCellValue())
            .as("Incorrect Offence [2] Plea column")
            .isEqualTo("Offence[2] - Plea");

        softly.assertThat(row.getCell(17).getStringCellValue())
            .as("Incorrect Offence [2] Plea date column")
            .isEqualTo("Offence[2] - Plea date");

        softly.assertThat(row.getCell(18).getStringCellValue())
            .as("Incorrect Offence [2] Detail column")
            .isEqualTo("Offence[2] - Detail");

        softly.assertAll();
    }

    @Test
    void testSuccessfulExcelConversionValues() throws IOException {
        byte[] result = converter.convertToExcel(inputJson, ListType.OPA_PRESS_LIST);

        ByteArrayInputStream file = new ByteArrayInputStream(result);
        Workbook workbook = new XSSFWorkbook(file);

        Sheet sheet = workbook.getSheetAt(0);
        Row row = sheet.getRow(1);
        SoftAssertions softly = new SoftAssertions();

        softly.assertThat(row.getCell(0).getStringCellValue())
            .as("Incorrect URN value")
            .isEqualTo("URN8888");

        softly.assertThat(row.getCell(1).getStringCellValue())
            .as("Incorrect Name value")
            .isEqualTo("Surname2, Forename2 MiddleName2");

        softly.assertThat(row.getCell(2).getStringCellValue())
            .as("Incorrect Address value")
            .isEqualTo("Address Line 1, Address Line 2, Town, County, BB1 1BB");

        softly.assertThat(row.getCell(3).getStringCellValue())
            .as("Incorrect DOB column")
            .isEqualTo("01/01/1985");

        softly.assertThat(row.getCell(4).getStringCellValue())
            .as("Incorrect Prosecution column")
            .isEqualTo("Prosecuting authority ref");

        softly.assertThat(row.getCell(5).getStringCellValue())
            .as("Incorrect Scheduled First Hearing column")
            .isEqualTo("01/10/2023");

        softly.assertThat(row.getCell(6).getStringCellValue())
            .as("Incorrect Case Reporting Restriction column")
            .isEqualTo("Case reporting Restriction detail line 1, Case reporting restriction detail line 2");

        softly.assertAll();
    }

    @Test
    void testSuccessfulExcelConversionOffenceValues() throws IOException {
        byte[] result = converter.convertToExcel(inputJson, ListType.OPA_PRESS_LIST);

        ByteArrayInputStream file = new ByteArrayInputStream(result);
        Workbook workbook = new XSSFWorkbook(file);

        Sheet sheet = workbook.getSheetAt(0);
        Row row = sheet.getRow(1);
        SoftAssertions softly = new SoftAssertions();

        softly.assertThat(row.getCell(7).getStringCellValue())
            .as("Incorrect Offence [1] Title column")
            .isEqualTo("Offence title 2");

        softly.assertThat(row.getCell(8).getStringCellValue())
            .as("Incorrect Offence [1] Section column")
            .isEqualTo("Offence section 2");

        softly.assertThat(row.getCell(9).getStringCellValue())
            .as("Incorrect Offence [1] Reporting Restriction column")
            .isEqualTo("Offence reporting restriction detail 1");

        softly.assertThat(row.getCell(10).getStringCellValue())
            .as("Incorrect Offence [1] Plea column")
            .isEqualTo("NOT_GUILTY");

        softly.assertThat(row.getCell(11).getStringCellValue())
            .as("Incorrect Offence [1] Plea Date column")
            .isEqualTo("22/09/2023");

        softly.assertThat(row.getCell(12).getStringCellValue())
            .as("Incorrect Offence [1] Detail column")
            .isEqualTo("Offence wording 2");

        Row rowWithSecondOffence = sheet.getRow(7);

        softly.assertThat(rowWithSecondOffence.getCell(13).getStringCellValue())
            .as("Incorrect Offence [2] Title column")
            .isEqualTo("Offence title 4");

        softly.assertThat(rowWithSecondOffence.getCell(14).getStringCellValue())
            .as("Incorrect Offence [2] Section column")
            .isEqualTo("Offence section 4");

        softly.assertThat(rowWithSecondOffence.getCell(15).getStringCellValue())
            .as("Incorrect Offence [2] Reporting Restriction column")
            .isEqualTo("Offence reporting restriction detail 2");

        softly.assertThat(rowWithSecondOffence.getCell(16).getStringCellValue())
            .as("Incorrect Offence [2] Plea column")
            .isEqualTo("NOT_GUILTY");

        softly.assertThat(rowWithSecondOffence.getCell(17).getStringCellValue())
            .as("Incorrect Offence [2] Plea Date column")
            .isEqualTo("21/09/2023");

        softly.assertThat(rowWithSecondOffence.getCell(18).getStringCellValue())
            .as("Incorrect Offence [2] Detail column")
            .isEqualTo("Offence wording 4");

        softly.assertAll();
    }
}
