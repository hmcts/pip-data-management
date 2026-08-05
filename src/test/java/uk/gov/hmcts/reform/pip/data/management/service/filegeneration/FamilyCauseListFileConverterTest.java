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
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.pip.model.publication.Language;
import uk.gov.hmcts.reform.pip.model.publication.ListType;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;

class FamilyCauseListFileConverterTest {

    private final FileConverter familyDailyCauseListConverter
        = new FamilyDailyCauseListFileConverter();

    private static final String HEADER_TEXT = "Incorrect header text";
    private static final String TITLE_TEXT = "Incorrect Title Text";
    private static final String LINK_MESSAGE = "Link does not match";

    private static final String PROVENANCE = "provenance";

    private static final String LINK_CLASS = "govuk-link";
    private static final String HREF = "href";
    private static final String BODY_CLASS = "govuk-body";

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final Map<String, String> METADATA = Map.of(
        "contentDate", Instant.now().toString(),
        PROVENANCE, PROVENANCE,
        "locationName", "location",
        "language", "ENGLISH",
        "listType", "FAMILY_DAILY_CAUSE_LIST"
    );
    private static final Map<String, String> WELSH_METADATA = Map.of(
        "contentDate", Instant.now().toString(),
        PROVENANCE, PROVENANCE,
        "locationName", "location",
        "language", "WELSH",
        "listType", "FAMILY_DAILY_CAUSE_LIST"
    );

    @Test
    void testFamilyCauseListTemplate() throws IOException {
        Map<String, Object> language;
        try (InputStream languageFile = Thread.currentThread()
            .getContextClassLoader().getResourceAsStream("templates/languages/en/familyDailyCauseList.json")) {
            language = OBJECT_MAPPER.readValue(
                Objects.requireNonNull(languageFile).readAllBytes(), new TypeReference<>() {
                });
        }
        JsonNode inputJson = getInputJson();
        String outputHtml = familyDailyCauseListConverter.convert(inputJson, METADATA, language);

        Document document = Jsoup.parse(outputHtml);
        assertThat(outputHtml).as("No html found").isNotEmpty();

        assertThat(document.title()).as("incorrect title found.")
            .isEqualTo("Family Daily Cause List");

        assertThat(document.getElementsByClass("govuk-heading-l")
            .get(0).text())
            .as(HEADER_TEXT).isEqualTo("Family Daily Cause List for location");

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

        assertThat(document.getElementsByClass(BODY_CLASS)
                       .get(3).text())
            .as(HEADER_TEXT).contains("Last updated 21 July 2022");

        assertThat(document.getElementsByClass("govuk-accordion__section-heading"))
            .as("Incorrect table titles")
            .extracting(Element::text)
            .containsAll(List.of(
                "This is the court room name, Before: Judge KnownAs Presiding, Judge KnownAs",
                "This is the court room name, Before: Judge KnownAs 1, Judge KnownAs 2",
                "This is the court room name, Before: Judge KnownAs 1, Judge KnownAs 2"));
    }


    @Test
    void testFamilyCauseListTemplateWelsh() throws IOException {
        Map<String, Object> language;
        try (InputStream languageFile = Thread.currentThread()
            .getContextClassLoader().getResourceAsStream("templates/languages/cy/familyDailyCauseList.json")) {
            language = OBJECT_MAPPER.readValue(
                Objects.requireNonNull(languageFile).readAllBytes(), new TypeReference<>() {
                });
        }
        JsonNode inputJson = getInputJson();
        String outputHtml = familyDailyCauseListConverter.convert(inputJson, WELSH_METADATA, language);

        Document document = Jsoup.parse(outputHtml);
        assertThat(outputHtml).as("No html found").isNotEmpty();

        assertThat(document.title()).as("incorrect title found.")
            .isEqualTo("Rhestr Ddyddiol o Achosion Teulu");

        assertThat(document.getElementsByClass("govuk-heading-l")
                       .get(0).text())
            .as(HEADER_TEXT).isEqualTo("Rhestr Ddyddiol o Achosion Teulu gyfer location");

        assertThat(document.getElementsByClass(LINK_CLASS).get(0)
                              .getElementsByTag("a").get(0)
                              .attr(HREF))
            .as(LINK_MESSAGE)
            .isEqualTo("https://www.find-court-tribunal.service.gov.uk/");

        assertThat(document.getElementsByClass(BODY_CLASS).get(0).text())
            .as(LINK_MESSAGE)
            .isEqualTo("Dod o hyd i fanylion cyswllt a gwybodaeth arall am lysoedd a thribiwnlysoedd yng "
                           + "Nghymru a Lloegr a rhai tribiwnlysoedd heb eu datganoli yn yr Alban.");

        assertThat(document.getElementsByTag("a")
                       .get(1).attr("title"))
            .as(TITLE_TEXT).contains("Sut i arsylwi gwrandawiad llys neu dribiwnlys");

        assertThat(document.getElementsByClass(BODY_CLASS)
                       .get(3).text())
            .as(HEADER_TEXT).contains("Diweddarwyd ddiwethaf 21 July 2022 am 3:01pm");

        assertThat(document.getElementsByClass("govuk-accordion__section-heading"))
            .as("Incorrect table titles")
            .extracting(Element::text)
            .containsAll(List.of(
                "This is the court room name, Gerbron: Judge KnownAs Presiding, Judge KnownAs",
                "This is the court room name, Gerbron: Judge KnownAs 1, Judge KnownAs 2",
                "This is the court room name, Gerbron: Judge KnownAs 1, Judge KnownAs 2"));
    }

    @Test
    void testTableContents() throws IOException {
        Map<String, Object> language;
        try (InputStream languageFile = Thread.currentThread()
            .getContextClassLoader().getResourceAsStream("templates/languages/en/familyDailyCauseList.json")) {
            language = OBJECT_MAPPER.readValue(
                Objects.requireNonNull(languageFile).readAllBytes(), new TypeReference<>() {
                });
        }
        JsonNode inputJson = getInputJson();
        String result = familyDailyCauseListConverter.convert(inputJson, METADATA, language);

        Document doc = Jsoup.parse(result);
        SoftAssertions softly = new SoftAssertions();

        softly.assertThat(doc.getElementsByTag("th"))
            .as("Incorrect table headers")
            .hasSize(27)
            .extracting(Element::text)
            .startsWith("Time",
                        "Case ID",
                        "Case Name",
                        "Case Type",
                        "Hearing Type",
                        "Location",
                        "Duration",
                        "Applicant/Petitioner",
                        "Respondent"
            );

        softly.assertThat(doc.getElementsByTag("td"))
            .as("Incorrect table size")
            .hasSize(46);

        softly.assertThat(doc.getElementsByTag("td"))
            .as("Incorrect table contents for hearing with a single case")
            .extracting(Element::text)
            .containsSequence(
                "10:30am",
                "12341234",
                "This is a case name [2 of 3]",
                "normal",
                "Directions",
                "Teams, Attended",
                "1 hour 25 mins",
                "Applicant Surname 1, Legal Advisor: Mr Rep Forenames 1 Rep Middlename 1 Rep Surname 1",
                "Respondent Surname 1",
                "Reporting Restriction: Reporting restriction 1, Reporting restriction 2"
            );

        softly.assertThat(doc.getElementsByTag("td"))
            .as("Incorrect table contents for hearing with multiple cases")
            .extracting(Element::text)
            .containsSequence(
                "10:30am",
                "12341236",
                "This is a case name 3",
                "normal",
                "Directions",
                "Teams, Attended",
                "1 hour 25 mins",
                "Applicant Surname 2, Legal Advisor: Mr Rep Forenames 3 Rep Middlename 3 Rep Surname 3, "
                    + "Mr Rep Forenames 2 Rep Middlename 2 Rep Surname 2",
                "Respondent Surname 2"
            );

        softly.assertThat(doc.getElementsByClass("govuk-table__body").get(1).getElementsByTag("td"))
            .as("Incorrect table contents for hearing with organisation details")
            .extracting(Element::text)
            .containsSequence(
                "10:30am",
                "12341235",
                "This is a case name 2",
                "normal",
                "Directions",
                "Teams, Attended",
                "1 hour 25 mins",
                "Applicant org name, Legal Advisor: Applicant rep org name",
                "Respondent org name, Legal Advisor: Respondent rep org name"
            );

        softly.assertAll();
    }

    private JsonNode getInputJson() throws IOException {
        StringWriter writer = new StringWriter();
        IOUtils.copy(Files.newInputStream(Paths.get("src/test/resources/mocks/",
                                                    "familyDailyCauseList.json")), writer,
                     Charset.defaultCharset()
        );

        return OBJECT_MAPPER.readTree(writer.toString());
    }


    @Test
    void testExcelTableHeaderEnglish() throws IOException {
        StringWriter writer = new StringWriter();
        IOUtils.copy(Files.newInputStream(Paths.get("src/test/resources/mocks/",
                                                    "familyDailyCauseList.json")), writer,
                     Charset.defaultCharset()
        );
        JsonNode inputJson = new ObjectMapper().readTree(writer.toString());

        byte[] result = familyDailyCauseListConverter.convertToExcel(inputJson,
                                                                   ListType.FAMILY_DAILY_CAUSE_LIST, Language.ENGLISH);
        ByteArrayInputStream file = new ByteArrayInputStream(result);
        Workbook workbook = new XSSFWorkbook(file);
        Sheet sheet = workbook.getSheetAt(0);
        Row headingRow = sheet.getRow(0);

        assertEquals("Sheet name does not match", "Sheet1", sheet.getSheetName());
        assertEquals("Time column is different", "Time",
                     headingRow.getCell(0).getStringCellValue());
        assertEquals("Case ref column is different", "Case ID",
                     headingRow.getCell(1).getStringCellValue());
        assertEquals("Case name column is different", "Case Name",
                     headingRow.getCell(2).getStringCellValue());
        assertEquals("Case type column is different", "Case Type",
                     headingRow.getCell(3).getStringCellValue());
        assertEquals("Hearing type column is different", "Hearing Type",
                     headingRow.getCell(4).getStringCellValue());
        assertEquals("Location column is different", "Location",
                     headingRow.getCell(5).getStringCellValue());
        assertEquals("Duration column is different", "Duration",
                     headingRow.getCell(6).getStringCellValue());
        assertEquals("Applicant/Petitioner column is different", "Applicant/Petitioner",
                     headingRow.getCell(7).getStringCellValue());
        assertEquals("Respondent column is different", "Respondent",
                     headingRow.getCell(8).getStringCellValue());
        assertEquals("Reporting Restrictions column is different", "Reporting Restriction: ",
                     headingRow.getCell(9).getStringCellValue());
    }

    @Test
    void testExcelTableHeaderWelsh() throws IOException {
        StringWriter writer = new StringWriter();
        IOUtils.copy(Files.newInputStream(Paths.get("src/test/resources/mocks/",
                                                    "familyDailyCauseList.json")), writer,
                     Charset.defaultCharset()
        );
        JsonNode inputJson = new ObjectMapper().readTree(writer.toString());

        byte[] result = familyDailyCauseListConverter.convertToExcel(inputJson,
                                                                   ListType.FAMILY_DAILY_CAUSE_LIST, Language.WELSH);
        ByteArrayInputStream file = new ByteArrayInputStream(result);
        Workbook workbook = new XSSFWorkbook(file);
        Sheet sheet = workbook.getSheetAt(0);
        Row headingRow = sheet.getRow(0);

        assertEquals("Sheet name does not match", "Sheet1", sheet.getSheetName());
        assertEquals("Time column is different", "Amser",
                     headingRow.getCell(0).getStringCellValue());
        assertEquals("Case ref column is different", "Cyfeirnod yr Achos",
                     headingRow.getCell(1).getStringCellValue());
        assertEquals("Case name at column is different", "Enw'r achos",
                     headingRow.getCell(2).getStringCellValue());
        assertEquals("Case type column is different", "Math o achos",
                     headingRow.getCell(3).getStringCellValue());
        assertEquals("Hearing type column is different", "Math o wrandawiad",
                     headingRow.getCell(4).getStringCellValue());
        assertEquals("Location column is different", "Lleoliad",
                     headingRow.getCell(5).getStringCellValue());
        assertEquals("Duration column is different", "Hyd",
                     headingRow.getCell(6).getStringCellValue());
        assertEquals("Applicant/Petitioner column is different", "Ceisydd/Deisebydd",
                     headingRow.getCell(7).getStringCellValue());
        assertEquals("Respondent column is different", "Atebydd",
                     headingRow.getCell(8).getStringCellValue());
        assertEquals("Reporting Restrictions column is different", "Cyfyngiad adrodd: ",
                     headingRow.getCell(9).getStringCellValue());
    }

    @Test
    void testExcelTableRows() throws IOException {
        StringWriter writer = new StringWriter();
        IOUtils.copy(
            Files.newInputStream(Paths.get(
                "src/test/resources/mocks/",
                "familyDailyCauseList.json"
            )), writer,
            Charset.defaultCharset()
        );
        JsonNode inputJson = new ObjectMapper().readTree(writer.toString());

        byte[] result = familyDailyCauseListConverter.convertToExcel(
            inputJson,
            ListType.FAMILY_DAILY_CAUSE_LIST, Language.ENGLISH
        );
        ByteArrayInputStream file = new ByteArrayInputStream(result);
        Workbook workbook = new XSSFWorkbook(file);
        Sheet sheet = workbook.getSheetAt(0);
        Row dataRow = sheet.getRow(1);
        assertEquals("Time value is different", "10:30am",
                     dataRow.getCell(0).getStringCellValue());
        assertEquals("Case ref value is different", "12341234",
                     dataRow.getCell(1).getStringCellValue());
        assertEquals("Case name value is different","This is a case name [2 of 3]",
                     dataRow.getCell(2).getStringCellValue());
        assertEquals("Case type value is different", "normal",
                     dataRow.getCell(3).getStringCellValue());
        assertEquals("Hearing type value is different", "Directions",
                     dataRow.getCell(4).getStringCellValue());
        assertEquals("Location value is different", "Teams, Attended",
                     dataRow.getCell(5).getStringCellValue());
        assertEquals("Duration value is different", "1 hour 25 mins",
                     dataRow.getCell(6).getStringCellValue());
        assertEquals("Applicant/Petitioner value is different",
                     "Applicant Surname 1, Legal advisor: Mr Rep Forenames 1 Rep Middlename 1 Rep Surname 1",
                     dataRow.getCell(7).getStringCellValue());
        assertEquals("Respondent value is different", "Respondent Surname 1",
                     dataRow.getCell(8).getStringCellValue());
        assertEquals("Reporting Restrictions value is different",
                     "Reporting restriction 1, Reporting restriction 2",
                     dataRow.getCell(9).getStringCellValue());
    }
}
