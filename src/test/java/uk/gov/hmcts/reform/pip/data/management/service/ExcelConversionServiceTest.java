package uk.gov.hmcts.reform.pip.data.management.service;

import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.multipart.MultipartFile;
import org.testcontainers.shaded.com.fasterxml.jackson.core.type.TypeReference;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper;
import org.testcontainers.shaded.org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

@ActiveProfiles("test")
class ExcelConversionServiceTest {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final String FILE = "file";
    private static final String FILE_NAME = "TestFileName.xlsx";
    private static final String FILE_TYPE = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
    private static final String CELL_MATCH_MESSAGE = "Cell value does not match";
    private static final String CELL_EMPTY_MESSAGE = "Cell value is not empty";
    private static final String ROW_SIZE_MESSAGE = "Row size does not match";

    private static final String HEADER1 = "header1";
    private static final String HEADER2 = "header2";
    private static final String HEADER3 = "header3";
    private static final String HEADER4 = "header4";
    private static final String ROW1A = "Row1a";
    private static final String ROW1B = "Row1b";
    private static final String ROW1C = "Row1c";
    private static final String ROW1D = "Row1d";
    private static final String ROW2A = "Row2a";
    private static final String ROW2B = "Row2b";
    private static final String ROW2C = "Row2c";
    private static final String ROW2D = "Row2d";
    private static final String ROW3A = "Row3a";
    private static final String ROW3B = "Row3b";
    private static final String ROW3C = "Row3c";
    private static final String ROW3D = "Row3d";

    private final ExcelConversionService excelConversionService = new ExcelConversionService();

    @Test
    void shouldConvertExcelTableToJsonString() throws IOException {
        try (InputStream inputStream = this.getClass()
            .getClassLoader()
            .getResourceAsStream("excel/table.xlsx")) {
            MultipartFile file = new MockMultipartFile(FILE, FILE_NAME, FILE_TYPE, IOUtils.toByteArray(inputStream));

            String json = excelConversionService.convert(file);
            List<Map<String, String>> results = OBJECT_MAPPER.readValue(json, new TypeReference<>(){});

            SoftAssertions softly = new SoftAssertions();
            Map<String, String> firstRow = results.getFirst();

            softly.assertThat(firstRow.get(HEADER1))
                .as(CELL_MATCH_MESSAGE)
                .isEqualTo(ROW1A);

            softly.assertThat(firstRow.get(HEADER2))
                .as(CELL_MATCH_MESSAGE)
                .isEqualTo(ROW1B);

            Map<String, String> secondRow = results.get(1);

            softly.assertThat(secondRow.get(HEADER1))
                .as(CELL_MATCH_MESSAGE)
                .isEqualTo(ROW2A);

            softly.assertThat(secondRow.get(HEADER2))
                .as(CELL_MATCH_MESSAGE)
                .isEqualTo(ROW2B);

            Map<String, String> thirdRow = results.get(2);

            softly.assertThat(thirdRow.get(HEADER1))
                .as(CELL_MATCH_MESSAGE)
                .isEqualTo(ROW3A);

            softly.assertThat(thirdRow.get(HEADER2))
                .as(CELL_MATCH_MESSAGE)
                .isEqualTo(ROW3B);

            softly.assertAll();
        }
    }

    @Test
    void shouldConvertExcelTableWithEmptyCellsToJsonString() throws IOException {
        try (InputStream inputStream = this.getClass()
            .getClassLoader()
            .getResourceAsStream("excel/tableWithEmptyCells.xlsx")) {
            MultipartFile file = new MockMultipartFile(FILE, FILE_NAME, FILE_TYPE, IOUtils.toByteArray(inputStream));

            String json = excelConversionService.convert(file);
            List<Map<String, String>> results = OBJECT_MAPPER.readValue(json, new TypeReference<>(){});

            SoftAssertions softly = new SoftAssertions();
            Map<String, String> firstRow = results.getFirst();

            softly.assertThat(firstRow.get(HEADER1))
                .as(CELL_EMPTY_MESSAGE)
                .isEmpty();

            softly.assertThat(firstRow.get(HEADER2))
                .as(CELL_MATCH_MESSAGE)
                .isEqualTo(ROW1B);

            Map<String, String> secondRow = results.get(1);

            softly.assertThat(secondRow.get(HEADER1))
                .as(CELL_MATCH_MESSAGE)
                .isEqualTo(ROW2A);

            softly.assertThat(secondRow.get(HEADER2))
                .as(CELL_EMPTY_MESSAGE)
                .isEmpty();

            Map<String, String> thirdRow = results.get(2);

            softly.assertThat(thirdRow.get(HEADER1))
                .as(CELL_EMPTY_MESSAGE)
                .isEmpty();

            softly.assertThat(thirdRow.get(HEADER2))
                .as(CELL_MATCH_MESSAGE)
                .isEqualTo(ROW3B);

            softly.assertAll();
        }
    }

    @Test
    void shouldConvertExcelTableStartingFromSecondColumnAndSecondRowToJsonString() throws IOException {
        try (InputStream inputStream = this.getClass()
            .getClassLoader()
            .getResourceAsStream("excel/shiftedTable.xlsx")) {
            MultipartFile file = new MockMultipartFile(FILE, FILE_NAME, FILE_TYPE, IOUtils.toByteArray(inputStream));

            String json = excelConversionService.convert(file);
            List<Map<String, String>> results = OBJECT_MAPPER.readValue(json, new TypeReference<>(){});

            SoftAssertions softly = new SoftAssertions();
            Map<String, String> firstRow = results.getFirst();

            softly.assertThat(firstRow.get(HEADER1))
                .as(CELL_MATCH_MESSAGE)
                .isEqualTo(ROW1A);

            softly.assertThat(firstRow.get(HEADER2))
                .as(CELL_MATCH_MESSAGE)
                .isEqualTo(ROW1B);

            Map<String, String> secondRow = results.get(1);

            softly.assertThat(secondRow.get(HEADER1))
                .as(CELL_MATCH_MESSAGE)
                .isEqualTo(ROW2A);

            softly.assertThat(secondRow.get(HEADER2))
                .as(CELL_MATCH_MESSAGE)
                .isEqualTo(ROW2B);

            Map<String, String> thirdRow = results.get(2);

            softly.assertThat(thirdRow.get(HEADER1))
                .as(CELL_MATCH_MESSAGE)
                .isEqualTo(ROW3A);

            softly.assertThat(thirdRow.get(HEADER2))
                .as(CELL_MATCH_MESSAGE)
                .isEqualTo(ROW3B);

            softly.assertAll();
        }
    }

    @Test
    void shouldConvertExcelTableWithCellsInVariousFormatToJsonString() throws IOException {
        try (InputStream inputStream = this.getClass()
            .getClassLoader()
            .getResourceAsStream("excel/tableCellsInVariousFormats.xlsx")) {
            MultipartFile file = new MockMultipartFile(FILE, FILE_NAME, FILE_TYPE, IOUtils.toByteArray(inputStream));

            String json = excelConversionService.convert(file);
            List<Map<String, String>> results = OBJECT_MAPPER.readValue(json, new TypeReference<>(){});

            SoftAssertions softly = new SoftAssertions();
            Map<String, String> firstRow = results.getFirst();

            softly.assertThat(firstRow.get("date"))
                .as(CELL_MATCH_MESSAGE)
                .isEqualTo("11/01/2025");

            softly.assertThat(firstRow.get("time"))
                .as(CELL_MATCH_MESSAGE)
                .isEqualTo("10:30am");

            softly.assertThat(firstRow.get("number"))
                .as(CELL_MATCH_MESSAGE)
                .isEqualTo("1");

            softly.assertThat(firstRow.get("boolean"))
                .as(CELL_MATCH_MESSAGE)
                .isEqualTo("TRUE");

            softly.assertThat(firstRow.containsKey("strings"))
                .as(CELL_MATCH_MESSAGE)
                .isTrue();

            softly.assertThat(firstRow.get("strings"))
                .as(CELL_MATCH_MESSAGE)
                .isEqualTo("Test string");

            Map<String, String> secondRow = results.get(1);

            softly.assertThat(secondRow.get("date"))
                .as(CELL_MATCH_MESSAGE)
                .isEqualTo("12/01/2025");

            softly.assertThat(secondRow.get("time"))
                .as(CELL_MATCH_MESSAGE)
                .isEqualTo("3:30pm");

            softly.assertAll();
        }
    }

    @Test
    void shouldStopProcessingExcelFileWhenItReachesABlankRow() throws IOException {
        try (InputStream inputStream = this.getClass()
            .getClassLoader()
            .getResourceAsStream("excel/tableWithBlankRow.xlsx")) {
            MultipartFile file = new MockMultipartFile(FILE, FILE_NAME, FILE_TYPE, IOUtils.toByteArray(inputStream));

            String json = excelConversionService.convert(file);
            List<Map<String, String>> results = OBJECT_MAPPER.readValue(json, new TypeReference<>() {
            });

            SoftAssertions softly = new SoftAssertions();

            Map<String, String> firstRow = results.getFirst();

            softly.assertThat(firstRow.get(HEADER1))
                .as(CELL_EMPTY_MESSAGE)
                .isEmpty();

            softly.assertThat(firstRow.get(HEADER2))
                .as(CELL_MATCH_MESSAGE)
                .isEqualTo(ROW1B);

            Map<String, String> secondRow = results.get(1);

            softly.assertThat(secondRow.get(HEADER1))
                .as(CELL_MATCH_MESSAGE)
                .isEqualTo(ROW2A);

            softly.assertThat(secondRow.get(HEADER2))
                .as(CELL_EMPTY_MESSAGE)
                .isEmpty();

            softly.assertThat(results.size())
                .as(ROW_SIZE_MESSAGE)
                .isEqualTo(2);

            softly.assertAll();
        }
    }

    @Test
    void shouldConvertExcelTableWithMultipleTabsToJsonString() throws IOException {
        try (InputStream inputStream = this.getClass()
            .getClassLoader()
            .getResourceAsStream("excel/tableWithTwoTabs.xlsx")) {

            MultipartFile file = new MockMultipartFile(FILE, FILE_NAME, FILE_TYPE, IOUtils.toByteArray(inputStream));

            String results = excelConversionService.convert(file);

            Map<String, List<Map<String, String>>> sheetData = OBJECT_MAPPER.readValue(
                results, new TypeReference<>() {}
            );

            List<Map<String, String>> sheet1 = sheetData.get("sheet1");

            SoftAssertions softly = new SoftAssertions();

            Map<String, String> firstRow = sheet1.getFirst();
            softly.assertThat(firstRow.get(HEADER1)).as(CELL_MATCH_MESSAGE).isEqualTo(ROW1A);
            softly.assertThat(firstRow.get(HEADER2)).as(CELL_MATCH_MESSAGE).isEqualTo(ROW1B);

            Map<String, String> secondRow = sheet1.get(1);
            softly.assertThat(secondRow.get(HEADER1)).as(CELL_MATCH_MESSAGE).isEqualTo(ROW2A);
            softly.assertThat(secondRow.get(HEADER2)).as(CELL_MATCH_MESSAGE).isEqualTo(ROW2B);

            Map<String, String> thirdRow = sheet1.get(2);
            softly.assertThat(thirdRow.get(HEADER1)).as(CELL_MATCH_MESSAGE).isEqualTo(ROW3A);
            softly.assertThat(thirdRow.get(HEADER2)).as(CELL_MATCH_MESSAGE).isEqualTo(ROW3B);

            List<Map<String, String>> sheet2 = sheetData.get("sheet2");

            Map<String, String> sheetTwoFirstRow = sheet2.getFirst();
            softly.assertThat(sheetTwoFirstRow.get(HEADER3)).as(CELL_MATCH_MESSAGE).isEqualTo(ROW1C);
            softly.assertThat(sheetTwoFirstRow.get(HEADER4)).as(CELL_MATCH_MESSAGE).isEqualTo(ROW1D);

            Map<String, String> sheetTwoSecondRow = sheet2.get(1);
            softly.assertThat(sheetTwoSecondRow.get(HEADER3)).as(CELL_MATCH_MESSAGE).isEqualTo(ROW2C);
            softly.assertThat(sheetTwoSecondRow.get(HEADER4)).as(CELL_MATCH_MESSAGE).isEqualTo(ROW2D);

            Map<String, String> sheetTwoThirdRow = sheet2.get(2);
            softly.assertThat(sheetTwoThirdRow.get(HEADER3)).as(CELL_MATCH_MESSAGE).isEqualTo(ROW3C);
            softly.assertThat(sheetTwoThirdRow.get(HEADER4)).as(CELL_MATCH_MESSAGE).isEqualTo(ROW3D);

            softly.assertAll();
        }
    }

}
