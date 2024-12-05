package uk.gov.hmcts.reform.pip.data.management.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import uk.gov.hmcts.reform.pip.data.management.errorhandling.exceptions.ExcelConversionException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class ExcelConversionService {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final String SPREADSHEET_CONTENT_TYPE
        = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
    private static final String EXCEL_CONTENT_TYPE = "application/vnd.ms-excel";

    public String convert(MultipartFile file) {
        String contentType = file.getContentType();
        if (contentType == null
            || (!SPREADSHEET_CONTENT_TYPE.equals(file.getContentType())
            && !file.getContentType().contains(EXCEL_CONTENT_TYPE))) {
            throw new ExcelConversionException("Invalid Excel file type");
        }

        try (Workbook workbook = new XSSFWorkbook(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);
            int headerRowNumber = sheet.getFirstRowNum();
            Row headerRow = sheet.getRow(headerRowNumber);
            int firstColumnNumber = headerRow.getFirstCellNum();

            List<String> headers = getExcelRow(headerRow, firstColumnNumber);
            List<Map<String, String>> data = new ArrayList<>();

            for (int rowNumber = headerRowNumber + 1; rowNumber <= sheet.getLastRowNum(); rowNumber++) {
                List<String> row = getExcelRow(sheet.getRow(rowNumber), firstColumnNumber);
                data.add(buildRowMap(headers, row));
            }
            return OBJECT_MAPPER.writeValueAsString(data);
        } catch (IOException e) {
            throw new ExcelConversionException("Error converting Excel file into JSON format");
        }
    }

    @SuppressWarnings("PMD.UseConcurrentHashMap")
    private Map<String, String> buildRowMap(List<String> headers, List<String> row) {
        // Link hashmap is used to ensure insertion order on the row values
        Map<String, String> values = new LinkedHashMap<>();
        for (int headerNumber = 0; headerNumber < headers.size(); headerNumber++) {
            String rowCell = headerNumber < row.size() ? row.get(headerNumber) : "";
            values.put(headers.get(headerNumber), rowCell);
        }
        return values;
    }

    private List<String> getExcelRow(Row row, int firstColumnNumber) {
        List<String> rowData = new ArrayList<>();

        for (int colNumber = firstColumnNumber; colNumber < row.getLastCellNum(); colNumber++) {
            Cell cell = row.getCell(colNumber, Row.MissingCellPolicy.RETURN_NULL_AND_BLANK);
            String value = cell == null ? "" : cell.getStringCellValue();
            rowData.add(value);
        }
        return rowData;
    }
}
