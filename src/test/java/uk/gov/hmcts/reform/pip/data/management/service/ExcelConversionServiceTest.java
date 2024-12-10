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

    private static final String HEADER1 = "header1";
    private static final String HEADER2 = "header2";
    private static final String ROW1A = "Row1a";
    private static final String ROW1B = "Row1b";
    private static final String ROW2A = "Row2a";
    private static final String ROW2B = "Row2b";
    private static final String ROW3A = "Row3a";
    private static final String ROW3B = "Row3b";

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
            Map<String, String> firstRow = results.get(0);

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
            Map<String, String> firstRow = results.get(0);

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
            Map<String, String> firstRow = results.get(0);

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
            Map<String, String> firstRow = results.get(0);

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
                .isEqualTo("True");

            softly.assertThat(firstRow.get("string"))
                .as(CELL_MATCH_MESSAGE)
                .isEqualTo("Test string");

            softly.assertAll();
        }
    }
}
