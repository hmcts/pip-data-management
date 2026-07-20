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
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.pip.model.publication.ListType.CROWN_WARNED_PDDA_LIST;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class CrownWarnedPddaListFileConverterTest {
    private static final String TEST_FILE_PATH = "src/test/resources/mocks/";

    private static final String LANGUAGE = "language";
    private static final String LIST_TYPE = "listType";
    private static final String HEADING_CLASS = "govuk-heading-l";
    private static final String BODY_CLASS = "govuk-body";
    private static final String CONTACT_LIST_CLASS = "govuk-list govuk-list--bullet";
    private static final String LINK_CLASS = "govuk-link";
    private static final String HREF = "href";

    private static final String CONTENT_DATE = "18 July 2024";
    private static final String ADDRESS = "TestAddressLine1 TestAddressLine2 TestPostcode";
    private static final String HEADING_MESSAGE = "Heading does not match";
    private static final String BODY_MESSAGE = "Body does not match";
    private static final String CONTACT_MESSAGE = "Contact information does not match";
    private static final String LINK_MESSAGE = "Link does not match";
    private static final String EXCEL_SHEET_NAME_MESSAGE = "Excel sheet name does not match";
    private static final String EXCEL_TABLE_HEADER_MESSAGE = "Excel table header does not match";
    private static final String EXCEL_CELL_VALUE_MESSAGE = "Excel cell value does not match";

    private static final Map<String, String> COMMON_METADATA = Map.of("contentDate", CONTENT_DATE,
                                                                      "provenance", "MANUAL_UPLOAD",
                                                                      "locationName", "location",
                                                                      LIST_TYPE, CROWN_WARNED_PDDA_LIST.name()
    );

    private JsonNode inputJson;
    private CrownWarnedPddaListFileConverter crownWarnedPddaListConverter = new CrownWarnedPddaListFileConverter();

    @BeforeAll
    void setup() throws IOException {
        StringWriter writer = new StringWriter();
        IOUtils.copy(
            Files.newInputStream(Paths.get(TEST_FILE_PATH, "crownWarnedPddaList.json")),
            writer, Charset.defaultCharset()
        );
        inputJson = new ObjectMapper().readTree(writer.toString());
    }

    @Test
    void testCrownWarnedPddaListTemplateEnglish() throws IOException {
        Map<String, Object> language;

        try (InputStream languageFile = Thread.currentThread()
            .getContextClassLoader().getResourceAsStream("templates/languages/en/crownWarnedPddaList.json")) {
            language = new ObjectMapper().readValue(
                Objects.requireNonNull(languageFile).readAllBytes(), new TypeReference<>() {
                });
        }

        Map<String, String> metadataMap = new ConcurrentHashMap<>(COMMON_METADATA);
        metadataMap.put(LANGUAGE, "ENGLISH");

        String outputHtml = crownWarnedPddaListConverter.convert(inputJson, metadataMap, language);
        Document document = Jsoup.parse(outputHtml);

        SoftAssertions softly = new SoftAssertions();

        softly.assertThat(outputHtml)
            .as("No Warned list html found")
            .isNotEmpty();

        softly.assertThat(document.title())
            .as("incorrect Warned list title found.")
            .isEqualTo("Crown Warned List");

        softly.assertThat(document.getElementsByClass(HEADING_CLASS).get(0).text())
            .as(HEADING_MESSAGE)
            .contains("Crown Warned List for location");

        softly.assertThat(document.getElementsByClass(LINK_CLASS).get(0)
                              .getElementsByTag("a").get(0)
                              .attr(HREF))
            .as(LINK_MESSAGE)
            .isEqualTo("https://www.find-court-tribunal.service.gov.uk/");

        softly.assertThat(document.getElementsByClass(BODY_CLASS).get(0).text())
            .as(LINK_MESSAGE)
            .isEqualTo("Find contact details and other information about courts and tribunals in England "
                           + "and Wales, and some non-devolved tribunals in Scotland.");

        softly.assertThat(document.getElementsByClass(BODY_CLASS).get(1).text())
            .as(BODY_MESSAGE)
            .contains("01 January 2024 to 02 January 2024");

        softly.assertThat(document.getElementsByClass(BODY_CLASS).get(2).text())
            .as(BODY_MESSAGE)
            .contains("Last updated 01 January 2024 at 10am");

        softly.assertThat(document.getElementsByClass(BODY_CLASS).get(3).text())
            .as(BODY_MESSAGE)
            .contains("Version TestVersion");

        softly.assertThat(document.getElementsByClass(BODY_CLASS).get(4).text())
            .as(BODY_MESSAGE)
            .contains(ADDRESS);

        softly.assertThat(document.getElementsByClass(BODY_CLASS).get(5).text())
            .as(BODY_MESSAGE)
            .contains("The undermentioned cases are warned for the hearing period of week commencing 15 July 2024");

        softly.assertThat(document.getElementsByClass(BODY_CLASS).get(6).text())
            .as(BODY_MESSAGE)
            .contains("Any representation about the listing of a case should be "
                          + "made to the Listing Officer immediately");

        softly.assertThat(document.getElementsByClass(BODY_CLASS).get(7).text())
            .as(BODY_MESSAGE)
            .contains("The prosecuting authority is the Crown Prosecution Service unless otherwise stated");

        softly.assertThat(document.getElementsByClass(BODY_CLASS).get(8).text())
            .as(BODY_MESSAGE)
            .contains("*denotes a defendant in custody");

        softly.assertThat(document.getElementsByClass("restriction-list-section"))
            .as("Incorrect restriction heading")
            .anyMatch(e -> e.text().contains("Restrictions on publishing or writing about these cases"));

        softly.assertThat(document.getElementsByClass(BODY_CLASS).get(9).text())
            .as(BODY_MESSAGE)
            .contains("You must check if any reporting restrictions apply before publishing details on any of"
                          + " the cases listed here either in writing, in a broadcast or by internet, "
                          + "including social media.");

        softly.assertThat(document.getElementsByClass("govuk-warning-text__text"))
            .as("Incorrect warning message")
            .anyMatch(e -> e.text().contains("You'll be in contempt of court if you publish "
                                                 + "any information which is protected by a reporting restriction. "
                                                 + "You could get a fine, prison sentence or both."));

        softly.assertThat(document.getElementsByClass(BODY_CLASS).get(11).text())
            .as(BODY_MESSAGE)
            .contains("Specific restrictions ordered by the court will be mentioned on the cases listed here.");

        softly.assertThat(document.getElementsByClass(BODY_CLASS).get(12).text())
            .as(BODY_MESSAGE)
            .contains("However, restrictions are not always listed. Some apply automatically. "
                          + "For example, anonymity given to the victims of certain sexual offences.");

        softly.assertThat(document.getElementsByClass(BODY_CLASS).get(13).text())
            .as(BODY_MESSAGE)
            .contains("To find out which reporting restrictions apply on a specific case, contact:");

        softly.assertThat(document.getElementsByClass(CONTACT_LIST_CLASS))
            .as(CONTACT_MESSAGE)
            .anyMatch(e -> e.text().contains("the court directly"));

        softly.assertThat(document.getElementsByClass(CONTACT_LIST_CLASS))
            .as(CONTACT_MESSAGE)
            .anyMatch(e -> e.text().contains("HM Courts and Tribunals Service on 0330 808 440"));

        softly.assertThat(document.getElementsByClass("govuk-accordion__section-heading"))
            .as("Incorrect hearing type")
            .hasSize(2)
            .extracting(Element::text)
            .containsExactly(
                "TestHearingDescription",
                "To be allocated"
            );

        softly.assertThat(document.getElementsByTag("th"))
            .as("Incorrect table headers")
            .extracting(Element::text)
            .startsWith("Fixed For",
                        "Case Reference",
                        "Defendant Name(s)",
                        "Prosecuting Authority",
                        "Linked Cases",
                        "Listing Notes"
            );

        softly.assertAll();
    }

    @Test
    void testCrownWarnedPddaListTemplateWelsh() throws IOException {
        Map<String, Object> language;

        try (InputStream languageFile = Thread.currentThread()
            .getContextClassLoader().getResourceAsStream("templates/languages/cy/crownWarnedPddaList.json")) {
            language = new ObjectMapper().readValue(
                Objects.requireNonNull(languageFile).readAllBytes(), new TypeReference<>() {
                });
        }

        Map<String, String> metadataMap = new ConcurrentHashMap<>(COMMON_METADATA);
        metadataMap.put(LANGUAGE, "WELSH");

        String outputHtml = crownWarnedPddaListConverter.convert(inputJson, metadataMap, language);
        Document document = Jsoup.parse(outputHtml);

        SoftAssertions softly = new SoftAssertions();

        softly.assertThat(outputHtml)
            .as("No Warned list html found")
            .isNotEmpty();

        softly.assertThat(document.title())
            .as("incorrect Warned list title found.")
            .isEqualTo("Rhestr Rybuddio Llys y Goron");

        softly.assertThat(document.getElementsByClass(HEADING_CLASS).get(0).text())
            .as(HEADING_MESSAGE)
            .contains("Rhestr Rybuddio Llys y Goron ar gyfer location");

        softly.assertThat(document.getElementsByClass(LINK_CLASS).get(0)
                              .getElementsByTag("a").get(0)
                              .attr(HREF))
            .as(LINK_MESSAGE)
            .isEqualTo("https://www.find-court-tribunal.service.gov.uk/");

        softly.assertThat(document.getElementsByClass(BODY_CLASS).get(0).text())
            .as(LINK_MESSAGE)
            .isEqualTo("Dod o hyd i fanylion cyswllt a gwybodaeth arall am lysoedd a thribiwnlysoedd yng "
                           + "Nghymru a Lloegr a rhai tribiwnlysoedd heb eu datganoli yn yr Alban.");

        softly.assertThat(document.getElementsByClass(BODY_CLASS).get(1).text())
            .as(BODY_MESSAGE)
            .contains("01 January 2024 i 02 January 2024");

        softly.assertThat(document.getElementsByClass(BODY_CLASS).get(2).text())
            .as(BODY_MESSAGE)
            .contains("Diweddarwyd diwethaf 01 January 2024 am 10am");

        softly.assertThat(document.getElementsByClass(BODY_CLASS).get(3).text())
            .as(BODY_MESSAGE)
            .contains("Fersiwn TestVersion");

        softly.assertThat(document.getElementsByClass(BODY_CLASS).get(4).text())
            .as(BODY_MESSAGE)
            .contains(ADDRESS);

        softly.assertThat(document.getElementsByClass(BODY_CLASS).get(5).text())
            .as(BODY_MESSAGE)
            .contains("Rhoir rhybudd yng nghyswllt yr achosion isod am gyfnod gwrandawiad "
                          + "o'r wythnos yn dechrau 15 Gorffennaf 2024");

        softly.assertThat(document.getElementsByClass(BODY_CLASS).get(6).text())
            .as(BODY_MESSAGE)
            .contains("Dylid cyflwyno unrhyw sylwadau am restru achos i’r Swyddog Rhestru yn ddi-oed");

        softly.assertThat(document.getElementsByClass(BODY_CLASS).get(7).text())
            .as(BODY_MESSAGE)
            .contains("Yr awdurdod erlyn yw Gwasanaeth Erlyn y Goron oni nodir yn wahanol");

        softly.assertThat(document.getElementsByClass(BODY_CLASS).get(8).text())
            .as(BODY_MESSAGE)
            .contains("Mae (*) yn dynodi diffynnydd a gedwir yn y ddalfa");

        softly.assertThat(document.getElementsByClass("restriction-list-section"))
            .as("Incorrect restriction heading")
            .anyMatch(e -> e.text().contains("Cyfyngiadau ar gyhoeddi neu ysgrifennu am yr achosion hyn."));

        softly.assertThat(document.getElementsByClass(BODY_CLASS).get(9).text())
            .as(BODY_MESSAGE)
            .contains("Rhaid i chi wirio a oes unrhyw gyfyngiadau riportio yn berthnasol cyn "
                          + "cyhoeddi manylion am unrhyw un o'r achosion a restrir yma, "
                          + "naill ai'n ysgrifenedig, mewn darllediad neu ar y rhyngrwyd, "
                          + "gan gynnwys y cyfryngau cymdeithasol.");

        softly.assertThat(document.getElementsByClass("govuk-warning-text__text"))
            .as("Incorrect warning message")
            .anyMatch(e -> e.text().contains("Byddwch yn euog o ddirmyg llys os byddwch "
                                                 + "yn cyhoeddi unrhyw wybodaeth sydd wedi'i diogelu gan "
                                                 + "gyfyngiad riportio. Gallwch gael dirwy, eich dedfrydu "
                                                 + "i garchar, neu'r ddau."));

        softly.assertThat(document.getElementsByClass(BODY_CLASS).get(11).text())
            .as(BODY_MESSAGE)
            .contains("Bydd cyfyngiadau penodol a orchmynnir gan y llys yn cael eu "
                          + "crybwyll ar yr achosion a restrir yma.");

        softly.assertThat(document.getElementsByClass(BODY_CLASS).get(12).text())
            .as(BODY_MESSAGE)
            .contains("Fodd bynnag, nid yw'r cyfyngiadau bob amser yn cael eu rhestru. "
                          + "Mae rhai yn berthnasol yn awtomatig. Er enghraifft, anhysbysrwydd "
                          + "a roddir i ddioddefwyr rhai troseddau rhywiol.");

        softly.assertThat(document.getElementsByClass(BODY_CLASS).get(13).text())
            .as(BODY_MESSAGE)
            .contains("I ganfod pa gyfyngiadau riportio sy'n berthnasol ar achos penodol, cysylltwch â'r:");

        softly.assertThat(document.getElementsByClass(CONTACT_LIST_CLASS))
            .as(CONTACT_MESSAGE)
            .anyMatch(e -> e.text().contains("llys yn uniongyrchol"));

        softly.assertThat(document.getElementsByClass(CONTACT_LIST_CLASS))
            .as(CONTACT_MESSAGE)
            .anyMatch(e -> e.text().contains("Gwasanaeth Llysoedd a Thribiwnlysoedd EM ar 0330 808 4407"));

        softly.assertThat(document.getElementsByClass("govuk-accordion__section-heading"))
            .as("Incorrect hearing type")
            .hasSize(2)
            .extracting(Element::text)
            .containsExactly(
                "TestHearingDescription",
                "I'w neilltuo"
            );

        softly.assertThat(document.getElementsByTag("th"))
            .as("Incorrect table headers")
            .extracting(Element::text)
            .startsWith("Pennu ar gyfer",
                        "Cyfeirnod yr Achos",
                        "Enw'r Diffynnydd",
                        "Yr Awdurdod sy'n Erlyn",
                        "Achosion cysylltiedig",
                        "Nodiadau rhestru"
            );

        softly.assertAll();
    }

    @Test
    void testCrownWarnedPddaListTableContents() throws IOException {
        Map<String, Object> language;

        try (InputStream languageFile = Thread.currentThread()
            .getContextClassLoader().getResourceAsStream("templates/languages/en/crownWarnedPddaList.json")) {
            language = new ObjectMapper().readValue(
                Objects.requireNonNull(languageFile).readAllBytes(), new TypeReference<>() {
                });
        }

        Map<String, String> metadataMap = new ConcurrentHashMap<>(COMMON_METADATA);
        metadataMap.put(LANGUAGE, "ENGLISH");

        String outputHtml = crownWarnedPddaListConverter.convert(inputJson, metadataMap, language);
        Document document = Jsoup.parse(outputHtml);

        assertThat(document.getElementsByTag("td"))
            .as("Table contents does not match")
            .extracting(Element::text)
            .containsSequence(
                "01/01/2024",
                "T00112233",
                "TestDefendantRequestedName",
                "Crown Prosecution Service",
                "TestLinkedCaseNumber",
                "TestListNote"
            );
    }

    @Test
    void testCrownWarnedListExcelConversion() throws IOException {
        byte[] result = crownWarnedPddaListConverter.convertToExcel(inputJson, CROWN_WARNED_PDDA_LIST,
                                                                    Language.ENGLISH);
        ByteArrayInputStream file = new ByteArrayInputStream(result);
        Workbook workbook = new XSSFWorkbook(file);
        Sheet sheet = workbook.getSheetAt(0);
        Row headingRow = sheet.getRow(0);

        SoftAssertions softly = new SoftAssertions();

        softly.assertThat(sheet.getSheetName())
            .as(EXCEL_SHEET_NAME_MESSAGE)
            .isEqualTo(CROWN_WARNED_PDDA_LIST.getFriendlyName());

        softly.assertThat(headingRow.getCell(0).getStringCellValue())
            .as(EXCEL_TABLE_HEADER_MESSAGE)
            .isEqualTo("Hearing Description");

        softly.assertThat(headingRow.getCell(1).getStringCellValue())
            .as(EXCEL_TABLE_HEADER_MESSAGE)
            .isEqualTo("Fixed For");

        softly.assertThat(headingRow.getCell(2).getStringCellValue())
            .as(EXCEL_TABLE_HEADER_MESSAGE)
            .isEqualTo("Case Reference");

        softly.assertThat(headingRow.getCell(3).getStringCellValue())
            .as(EXCEL_TABLE_HEADER_MESSAGE)
            .isEqualTo("Defendant Name(s)");

        softly.assertThat(headingRow.getCell(4).getStringCellValue())
            .as(EXCEL_TABLE_HEADER_MESSAGE)
            .isEqualTo("Prosecuting Authority");

        softly.assertThat(headingRow.getCell(5).getStringCellValue())
            .as(EXCEL_TABLE_HEADER_MESSAGE)
            .isEqualTo("Linked Cases");

        softly.assertThat(headingRow.getCell(6).getStringCellValue())
            .as(EXCEL_TABLE_HEADER_MESSAGE)
            .isEqualTo("Listing Notes");

        Row dataRow = sheet.getRow(1);
        softly.assertThat(dataRow.getCell(0).getStringCellValue())
            .as(EXCEL_CELL_VALUE_MESSAGE)
            .isEqualTo("TestHearingDescription");

        softly.assertThat(dataRow.getCell(1).getStringCellValue())
            .as(EXCEL_CELL_VALUE_MESSAGE)
            .isEqualTo("01/01/2024");

        softly.assertThat(dataRow.getCell(2).getStringCellValue())
            .as(EXCEL_CELL_VALUE_MESSAGE)
            .isEqualTo("T00112233");

        softly.assertThat(dataRow.getCell(3).getStringCellValue())
            .as(EXCEL_CELL_VALUE_MESSAGE)
            .isEqualTo("TestDefendantRequestedName");

        softly.assertThat(dataRow.getCell(4).getStringCellValue())
            .as(EXCEL_CELL_VALUE_MESSAGE)
            .isEqualTo("Crown Prosecution Service");

        softly.assertThat(dataRow.getCell(5).getStringCellValue())
            .as(EXCEL_CELL_VALUE_MESSAGE)
            .isEqualTo("TestLinkedCaseNumber");

        softly.assertThat(dataRow.getCell(6).getStringCellValue())
            .as(EXCEL_CELL_VALUE_MESSAGE)
            .isEqualTo("TestListNote");

        softly.assertAll();
    }

    @Test
    void testCrownWarnedListWelshExcelConversion() throws IOException {
        byte[] result = crownWarnedPddaListConverter.convertToExcel(inputJson, CROWN_WARNED_PDDA_LIST,
                                                                    Language.WELSH);
        ByteArrayInputStream file = new ByteArrayInputStream(result);
        Workbook workbook = new XSSFWorkbook(file);
        Sheet sheet = workbook.getSheetAt(0);
        Row headingRow = sheet.getRow(0);

        SoftAssertions softly = new SoftAssertions();

        softly.assertThat(sheet.getSheetName())
            .as(EXCEL_SHEET_NAME_MESSAGE)
            .isEqualTo(CROWN_WARNED_PDDA_LIST.getFriendlyName());

        softly.assertThat(headingRow.getCell(0).getStringCellValue())
            .as(EXCEL_TABLE_HEADER_MESSAGE)
            .isEqualTo("Disgrifiad o'r Gwrandawiad");

        softly.assertThat(headingRow.getCell(1).getStringCellValue())
            .as(EXCEL_TABLE_HEADER_MESSAGE)
            .isEqualTo("Pennu ar gyfer");

        softly.assertThat(headingRow.getCell(2).getStringCellValue())
            .as(EXCEL_TABLE_HEADER_MESSAGE)
            .isEqualTo("Cyfeirnod yr Achos");

        softly.assertThat(headingRow.getCell(3).getStringCellValue())
            .as(EXCEL_TABLE_HEADER_MESSAGE)
            .isEqualTo("Enw'r Diffynnydd");

        softly.assertThat(headingRow.getCell(4).getStringCellValue())
            .as(EXCEL_TABLE_HEADER_MESSAGE)
            .isEqualTo("Yr Awdurdod sy'n Erlyn");

        softly.assertThat(headingRow.getCell(5).getStringCellValue())
            .as(EXCEL_TABLE_HEADER_MESSAGE)
            .isEqualTo("Achosion cysylltiedig");

        softly.assertThat(headingRow.getCell(6).getStringCellValue())
            .as(EXCEL_TABLE_HEADER_MESSAGE)
            .isEqualTo("Nodiadau rhestru");

        softly.assertAll();
    }
}
