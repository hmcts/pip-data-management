package uk.gov.hmcts.reform.pip.data.management.service.filegeneration;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.pip.model.publication.ListType;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.pip.model.publication.ListType.UT_IAC_STATUTORY_APPEALS_DAILY_HEARING_LIST;

@ActiveProfiles("test")
class NonStrategicListFileConverterTest {
    private static final String LANGUAGE_METADATA = "language";
    private static final String LIST_TYPE_METADATA = "listType";
    private static final String ENGLISH = "ENGLISH";

    private static final ListType EXCEL_LIST_TYPE = UT_IAC_STATUTORY_APPEALS_DAILY_HEARING_LIST;
    private static final Map<String, String> METADATA = Map.of(
        LANGUAGE_METADATA, ENGLISH,
        LIST_TYPE_METADATA, EXCEL_LIST_TYPE.name()
    );

    private static final List<String> SOURCE_HEADERS = List.of(
        "Hearing time", "Appellant", "Representative", "Appeal reference number",
        "Judge(s)", "Hearing type", "Location", "Additional information"
    );

    private static final String HEARING_TIME_HEADER = "Hearing time";
    private static final String APPELLANT_A = "Appellant A";
    private static final String APPELLANT_B = "Appellant B";

    private final NonStrategicListFileConverter converter = new NonStrategicListFileConverter();

    @Test
    void testConvertToExcelReturnsEmptyArrayWhenInputExcelIsNull() throws IOException {
        byte[] result = converter.convertToExcel(null, EXCEL_LIST_TYPE, METADATA, null);

        assertThat(result)
            .as("Result should be empty when there is no input Excel")
            .isEmpty();
    }

    @Test
    void testConvertToExcelReturnsWorkbookUnchangedWhenHeaderRowIsEmpty() throws IOException {
        byte[] workbookBytes;
        try (Workbook workbook = new XSSFWorkbook()) {
            workbook.createSheet("Sheet1").createRow(0);
            workbookBytes = toBytes(workbook);
        }

        try (InputStream excelFile = new ByteArrayInputStream(workbookBytes)) {
            byte[] result = converter.convertToExcel(null, EXCEL_LIST_TYPE, METADATA, excelFile);

            assertThat(result)
                .as("Workbook should still be returned when the header row has no cells")
                .isNotEmpty();
        }
    }

    @Test
    void testConvertToExcelSkipsMissingHeaderCells() throws IOException {
        byte[] workbookBytes;
        try (Workbook workbook = new XSSFWorkbook()) {
            Row headerRow = workbook.createSheet("Sheet1").createRow(0);
            headerRow.createCell(0).setCellValue("first");
            headerRow.createCell(1).setCellValue("second");
            headerRow.createCell(3).setCellValue("fourth");
            workbookBytes = toBytes(workbook);
        }

        try (InputStream excelFile = new ByteArrayInputStream(workbookBytes)) {
            byte[] result = converter.convertToExcel(null, EXCEL_LIST_TYPE, METADATA, excelFile);

            try (Workbook updated = new XSSFWorkbook(new ByteArrayInputStream(result))) {
                Row headerRow = updated.getSheetAt(0).getRow(0);

                SoftAssertions softly = new SoftAssertions();
                softly.assertThat(headerRow.getCell(0).getStringCellValue()).isEqualTo(HEARING_TIME_HEADER);
                softly.assertThat(headerRow.getCell(1).getStringCellValue()).isEqualTo("Appellant");
                softly.assertThat(headerRow.getCell(2)).as("Missing header cell should stay missing").isNull();
                softly.assertThat(headerRow.getCell(3).getStringCellValue())
                    .isEqualTo("Appeal reference number");
                softly.assertAll();
            }
        }
    }

    @Test
    void testConvertToExcelFormatsRowsUntilFirstBlankRowAndSkipsMissingRowsAndCells() throws IOException {
        byte[] workbookBytes;
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Sheet1");

            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < SOURCE_HEADERS.size(); i++) {
                headerRow.createCell(i).setCellValue(SOURCE_HEADERS.get(i));
            }

            Row formattedRow = sheet.createRow(1);
            formattedRow.createCell(0).setCellValue("10.30");
            formattedRow.createCell(1).setCellValue(APPELLANT_A);

            // Row 2 is intentionally never created so that the sheet contains a missing row

            Row rowWithoutHearingTime = sheet.createRow(3);
            rowWithoutHearingTime.createCell(1).setCellValue(APPELLANT_B);

            sheet.createRow(4);

            Row rowAfterBlankRow = sheet.createRow(5);
            rowAfterBlankRow.createCell(0).setCellValue("11.45");
            rowAfterBlankRow.createCell(1).setCellValue("Appellant C");

            workbookBytes = toBytes(workbook);
        }

        try (InputStream excelFile = new ByteArrayInputStream(workbookBytes)) {
            byte[] result = converter.convertToExcel(null, EXCEL_LIST_TYPE, METADATA, excelFile);

            try (Workbook updated = new XSSFWorkbook(new ByteArrayInputStream(result))) {
                Sheet sheet = updated.getSheetAt(0);

                SoftAssertions softly = new SoftAssertions();
                softly.assertThat(sheet.getRow(0).getCell(0).getStringCellValue())
                    .as("Header should be replaced with the language resource")
                    .isEqualTo(HEARING_TIME_HEADER);
                softly.assertThat(sheet.getRow(0).getCell(6).getStringCellValue())
                    .as("Header should be replaced with the language resource")
                    .isEqualTo("Location");
                softly.assertThat(sheet.getRow(1).getCell(0).getStringCellValue())
                    .as("Time before the blank row should be formatted")
                    .isEqualTo("10:30");
                softly.assertThat(sheet.getRow(1).getCell(1).getStringCellValue())
                    .as("Non-formatted column should be unchanged")
                    .isEqualTo(APPELLANT_A);
                softly.assertThat(sheet.getRow(3).getCell(0))
                    .as("Missing hearing time cell should not be created")
                    .isNull();
                softly.assertThat(sheet.getRow(3).getCell(1).getStringCellValue())
                    .isEqualTo(APPELLANT_B);
                softly.assertThat(sheet.getRow(5).getCell(0).getStringCellValue())
                    .as("Rows after the first blank row should not be formatted")
                    .isEqualTo("11.45");
                softly.assertAll();
            }
        }
    }

    private static byte[] toBytes(Workbook workbook) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        workbook.write(outputStream);
        return outputStream.toByteArray();
    }
}
