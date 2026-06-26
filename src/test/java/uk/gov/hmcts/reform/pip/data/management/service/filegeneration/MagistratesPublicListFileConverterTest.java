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
import java.util.Map;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

class MagistratesPublicListFileConverterTest {

    private final MagistratesPublicListFileConverter magistratesPublicListFileConverter
        = new MagistratesPublicListFileConverter();

    private static final String HEADER_TEXT = "Incorrect header text";
    private static final String LINK_MESSAGE = "Link does not match";

    private static final String PROVENANCE = "provenance";

    private static final String BODY_CLASS = "govuk-body";
    private static final String LINK_CLASS = "govuk-link";
    private static final String HREF = "href";

    @Test
    void testMagistratesPublicListTemplate() throws IOException {
        Map<String, Object> language;
        try (InputStream languageFile = Thread.currentThread()
            .getContextClassLoader().getResourceAsStream("templates/languages/en/magistratesPublicList.json")) {
            language = new ObjectMapper().readValue(
                Objects.requireNonNull(languageFile).readAllBytes(), new TypeReference<>() {
                });
        }
        StringWriter writer = new StringWriter();
        IOUtils.copy(Files.newInputStream(Paths.get("src/test/resources/mocks/",
                                                    "magistratesPublicList.json")), writer,
                     Charset.defaultCharset()
        );
        Map<String, String> metadataMap = Map.of("contentDate", Instant.now().toString(),
                                                 PROVENANCE, PROVENANCE,
                                                 "locationName", "location",
                                                 "language", "ENGLISH",
                                                 "listType", "MAGISTRATES_PUBLIC_LIST"
        );

        JsonNode inputJson = new ObjectMapper().readTree(writer.toString());
        String outputHtml = magistratesPublicListFileConverter.convert(inputJson, metadataMap, language);
        Document document = Jsoup.parse(outputHtml);

        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(outputHtml).as("No html found").isNotEmpty();

        softly.assertThat(document.title())
            .as("incorrect title found.")
            .isEqualTo("Magistrates Public List");

        softly.assertThat(document.getElementsByClass("govuk-heading-l")
                              .get(0).text())
            .as(HEADER_TEXT)
            .isEqualTo("Magistrates Public List for location");

        softly.assertThat(document.getElementsByClass(LINK_CLASS).get(0)
                              .getElementsByTag("a").get(0)
                              .attr(HREF))
            .as(LINK_MESSAGE)
            .isEqualTo("https://www.find-court-tribunal.service.gov.uk/");

        softly.assertThat(document.getElementsByClass(BODY_CLASS).get(0).text())
            .as(LINK_MESSAGE)
            .isEqualTo("Find contact details and other information about courts and tribunals in England "
                           + "and Wales, and some non-devolved tribunals in Scotland.");

        softly.assertThat(document.getElementsByClass(BODY_CLASS)
                              .get(2).text())
            .as(HEADER_TEXT)
            .isEqualTo("Last updated 14 September 2020 at 12:30am");

        softly.assertThat(outputHtml)
            .as("Before not shown")
            .doesNotContain("Before");

        softly.assertThat(document.getElementsByClass("govuk-table__head").get(0)
                              .getElementsByTag("th"))
            .as("Incorrect table headers")
            .hasSize(5)
            .extracting(Element::text)
            .containsExactly(
                "Sitting at",
                "URN",
                "Name",
                "Hearing Type",
                "Prosecuting Authority"
            );

        softly.assertAll();
    }

    @Test
    void testMagistratesPublicListTemplateWelsh() throws IOException {
        Map<String, Object> language;
        try (InputStream languageFile = Thread.currentThread()
            .getContextClassLoader().getResourceAsStream("templates/languages/cy/magistratesPublicList.json")) {
            language = new ObjectMapper().readValue(
                Objects.requireNonNull(languageFile).readAllBytes(), new TypeReference<>() {
                });
        }
        StringWriter writer = new StringWriter();
        IOUtils.copy(Files.newInputStream(Paths.get("src/test/resources/mocks/",
                                                    "magistratesPublicList.json")), writer,
                     Charset.defaultCharset()
        );
        Map<String, String> metadataMap = Map.of("contentDate", Instant.now().toString(),
                                                 PROVENANCE, PROVENANCE,
                                                 "locationName", "location",
                                                 "language", "WELSH",
                                                 "listType", "MAGISTRATES_PUBLIC_LIST"
        );

        JsonNode inputJson = new ObjectMapper().readTree(writer.toString());
        String outputHtml = magistratesPublicListFileConverter.convert(inputJson, metadataMap, language);
        Document document = Jsoup.parse(outputHtml);

        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(outputHtml).as("No html found").isNotEmpty();

        softly.assertThat(document.title())
            .as("incorrect title found.")
            .isEqualTo("Rhestr Gyhoeddus y Llys Ynadon");

        softly.assertThat(document.getElementsByClass("govuk-heading-l")
                              .get(0).text())
            .as(HEADER_TEXT)
            .isEqualTo("Rhestr Gyhoeddus y Llys Ynadon ar gyfer location");

        softly.assertThat(document.getElementsByClass(LINK_CLASS).get(0)
                              .getElementsByTag("a").get(0)
                              .attr(HREF))
            .as(LINK_MESSAGE)
            .isEqualTo("https://www.find-court-tribunal.service.gov.uk/");

        softly.assertThat(document.getElementsByClass(BODY_CLASS).get(0).text())
            .as(LINK_MESSAGE)
            .isEqualTo("Dod o hyd i fanylion cyswllt a gwybodaeth arall am lysoedd a thribiwnlysoedd yng "
                           + "Nghymru a Lloegr a rhai tribiwnlysoedd heb eu datganoli yn yr Alban.");

        softly.assertThat(document.getElementsByClass(BODY_CLASS)
                              .get(2).text())
            .as(HEADER_TEXT)
            .isEqualTo("Diweddarwyd diwethaf 14 September 2020 am 12:30am");

        softly.assertThat(outputHtml)
            .as("Before translation not shown")
            .doesNotContain("Gerbron");

        softly.assertThat(document.getElementsByClass("govuk-table__head").get(0)
                              .getElementsByTag("th"))
            .as("Incorrect Welsh table headers")
            .hasSize(5)
            .extracting(Element::text)
            .containsExactly(
                "Yn eistedd yn",
                "URN",
                "Enw",
                "Math o Wrandawiad",
                "Yr Awdurdod sy'n Erlyn"
            );

        softly.assertAll();
    }

    @Test
    void testMagistratesPublicListTableContents() throws IOException {
        Map<String, Object> language;
        try (InputStream languageFile = Thread.currentThread()
            .getContextClassLoader().getResourceAsStream("templates/languages/en/magistratesPublicList.json")) {
            language = new ObjectMapper().readValue(
                Objects.requireNonNull(languageFile).readAllBytes(), new TypeReference<>() {
                }
            );
        }
        StringWriter writer = new StringWriter();
        IOUtils.copy(
            Files.newInputStream(Paths.get(
                "src/test/resources/mocks/",
                "magistratesPublicList.json"
            )), writer,
            Charset.defaultCharset()
        );
        Map<String, String> metadataMap = Map.of(
            "contentDate", Instant.now().toString(),
            PROVENANCE, PROVENANCE,
            "locationName", "location",
            "language", "ENGLISH",
            "listType", "MAGISTRATES_PUBLIC_LIST"
        );

        JsonNode inputJson = new ObjectMapper().readTree(writer.toString());
        String outputHtml = magistratesPublicListFileConverter.convert(inputJson, metadataMap, language);
        Document document = Jsoup.parse(outputHtml);

        assertThat(document.getElementsByClass("govuk-table__body").get(0)
                       .getElementsByTag("td"))
            .as("Incorrect 'Sitting at' time")
            .extracting(Element::text)
            .contains("10:40am", "8am");

        assertThat(document.getElementsByTag("td"))
            .as("Table contents does not match")
            .extracting(Element::text)
            .containsSequence(
                "10:40am",
                "12345678",
                "Surname 2, Forename 2",
                "FHDRA1 (First Hearing and Dispute Resolution Appointment)",
                "Pro_Auth",
                "Offence Details:",
                "Test offence 1",
                "Reporting Restrictions:",
                "Press/Publication restrictions apply to this case"
            );
    }

    @Test
    void testSuccessfulExcelConversion() throws IOException {
        StringWriter writer = new StringWriter();
        IOUtils.copy(Files.newInputStream(Paths.get("src/test/resources/mocks/",
                                                    "magistratesPublicList.json")), writer,
                     Charset.defaultCharset()
        );
        JsonNode inputJson = new ObjectMapper().readTree(writer.toString());

        byte[] result = magistratesPublicListFileConverter.convertToExcel(inputJson, ListType.MAGISTRATES_PUBLIC_LIST, Language.ENGLISH);
        ByteArrayInputStream file = new ByteArrayInputStream(result);
        Workbook workbook = new XSSFWorkbook(file);
        Sheet sheet = workbook.getSheetAt(0);
        Row headingRow = sheet.getRow(0);

        assertEquals(ListType.MAGISTRATES_PUBLIC_LIST.getFriendlyName(), sheet.getSheetName(),
                     "Sheet name does not match");
        assertEquals("Court House", headingRow.getCell(0).getStringCellValue(),
                     "Court House column is different");
        assertEquals("Court Room", headingRow.getCell(1).getStringCellValue(),
                     "Court Room column is different");
        assertEquals("Sitting at", headingRow.getCell(2).getStringCellValue(),
                     "Sitting at column is different");
        assertEquals("URN", headingRow.getCell(3).getStringCellValue(),
                     "URN column is different");
        assertEquals("Name", headingRow.getCell(4).getStringCellValue(),
                     "Name column is different");
        assertEquals("Hearing Type", headingRow.getCell(5).getStringCellValue(),
                     "Hearing Type column is different");
        assertEquals("Prosecuting Authority", headingRow.getCell(6).getStringCellValue(),
                     "Prosecuting Authority column is different");
        assertEquals("Offence Details", headingRow.getCell(7).getStringCellValue(),
                     "Offence Details column is different");
        assertEquals("Reporting Restrictions", headingRow.getCell(8).getStringCellValue(),
                     "Reporting Restrictions column is different");

        Row dataRow = sheet.getRow(1);
        assertEquals("", dataRow.getCell(0).getStringCellValue(),
                     "Court House value is different");
        assertEquals("CourtRoom 1: Judge KnownAs, Judge KnownAs 2", dataRow.getCell(1).getStringCellValue(),
                     "Court Room value is different");
        assertEquals("10:40am", dataRow.getCell(2).getStringCellValue(),
                     "Sitting at value is different");
        assertEquals("12345678", dataRow.getCell(3).getStringCellValue(),
                     "URN value is different");
        assertEquals("Surname 2, Forename 2", dataRow.getCell(4).getStringCellValue(),
                     "Name value is different");
        assertEquals("FHDRA1 (First Hearing and Dispute Resolution Appointment)",
                     dataRow.getCell(5).getStringCellValue(), "Hearing type value is different");
        assertEquals("Pro_Auth", dataRow.getCell(6).getStringCellValue(),
                     "Prosecuting authority value is different");
        assertEquals("Test offence 1", dataRow.getCell(7).getStringCellValue(),
                     "Offence details value is different");
        assertEquals("Press/Publication restrictions apply to this case",
                     dataRow.getCell(8).getStringCellValue(), "Reporting restrictions value is different");
    }

    @Test
    void testWelshExcelConversion() throws IOException {
        StringWriter writer = new StringWriter();
        IOUtils.copy(Files.newInputStream(Paths.get("src/test/resources/mocks/",
                                                    "magistratesPublicList.json")), writer,
                     Charset.defaultCharset()
        );
        JsonNode inputJson = new ObjectMapper().readTree(writer.toString());

        byte[] result = magistratesPublicListFileConverter.convertToExcel(inputJson, ListType.MAGISTRATES_PUBLIC_LIST, Language.WELSH);
        ByteArrayInputStream file = new ByteArrayInputStream(result);
        Workbook workbook = new XSSFWorkbook(file);
        Sheet sheet = workbook.getSheetAt(0);
        Row headingRow = sheet.getRow(0);

        assertEquals(ListType.MAGISTRATES_PUBLIC_LIST.getFriendlyName(), sheet.getSheetName(),
                     "Sheet name does not match");
        assertEquals("Llys", headingRow.getCell(0).getStringCellValue(),
                     "Court House column is different");
        assertEquals("Ystafell Llys", headingRow.getCell(1).getStringCellValue(),
                     "Court Room column is different");
        assertEquals("Yn eistedd yn", headingRow.getCell(2).getStringCellValue(),
                     "Sitting at column is different");
        assertEquals("URN", headingRow.getCell(3).getStringCellValue(),
                     "URN column is different");
        assertEquals("Enw", headingRow.getCell(4).getStringCellValue(),
                     "Name column is different");
        assertEquals("Math o Wrandawiad", headingRow.getCell(5).getStringCellValue(),
                     "Hearing Type column is different");
        assertEquals("Yr Awdurdod sy'n Erlyn", headingRow.getCell(6).getStringCellValue(),
                     "Prosecuting Authority column is different");
        assertEquals("Manylion yr Trosedd", headingRow.getCell(7).getStringCellValue(),
                     "Offence Details column is different");
        assertEquals("Cyfyngiadau Riportio", headingRow.getCell(8).getStringCellValue(),
                     "Reporting Restrictions column is different");
    }
}
