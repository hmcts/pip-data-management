package uk.gov.hmcts.reform.pip.data.management.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.CaseFormat;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import uk.gov.hmcts.reform.pip.data.management.errorhandling.exceptions.ExcelConversionException;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
public class ExcelConversionService {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final String SPREADSHEET_CONTENT_TYPE
        = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
    private static final String EXCEL_CONTENT_TYPE = "application/vnd.ms-excel";

    public String convert(MultipartFile file) {
        String contentType = file.getContentType();
        if (contentType != null
            && !SPREADSHEET_CONTENT_TYPE.equals(contentType)
            && !contentType.contains(EXCEL_CONTENT_TYPE)) {
            throw new ExcelConversionException("Invalid Excel file type");
        }

        try (Workbook workbook = new XSSFWorkbook(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);
            int headerRowNumber = sheet.getFirstRowNum();
            int firstColumnNumber = sheet.getRow(headerRowNumber)
                .getFirstCellNum();

            List<String> headers = getExcelRow(sheet, headerRowNumber, firstColumnNumber);
            List<Map<String, String>> data = new ArrayList<>();

            for (int rowNumber = headerRowNumber + 1; rowNumber <= sheet.getLastRowNum(); rowNumber++) {
                List<String> row = getExcelRow(sheet, rowNumber, firstColumnNumber);
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
            String upperUnderscoreHeader = headers.get(headerNumber)
                .toUpperCase(Locale.ENGLISH)
                .replaceAll(" ", "_");
            String formattedHeader = CaseFormat.UPPER_UNDERSCORE.to(CaseFormat.LOWER_CAMEL, upperUnderscoreHeader);

            String rowCell = headerNumber < row.size() ? row.get(headerNumber) : "";
            values.put(formattedHeader, rowCell);
        }
        return values;
    }

    private List<String> getExcelRow(Sheet sheet, int rowNumber, int firstColumnNumber) {
        List<String> rowData = new ArrayList<>();
        Row row = sheet.getRow(rowNumber);

        for (int columnNumber = firstColumnNumber; columnNumber < row.getLastCellNum(); columnNumber++) {
            Cell cell = row.getCell(columnNumber, Row.MissingCellPolicy.RETURN_NULL_AND_BLANK);

            String cellValue = cell == null ? "" : switch (cell.getCellType()) {
                case CellType.NUMERIC -> formatNumericCell(cell);
                case CellType.BOOLEAN -> {
                    cell.setCellType(CellType.STRING);
                    yield cell.getStringCellValue();
                }
                case CellType.STRING -> cell.getStringCellValue();
                case CellType.BLANK -> "";
                default -> throw new ExcelConversionException(
                    String.format("Unexpected cell type on row %s, column %s",rowNumber + 1, columnNumber + 1 ));
            };

            rowData.add(cellValue);
        }
        return rowData;
    }

    private String formatNumericCell(Cell cell) {
        if (DateUtil.isCellDateFormatted(cell)) {
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.UK);
            return dateFormat.format(cell.getDateCellValue());
        } else {
            cell.setCellType(CellType.STRING);
            return cell.getStringCellValue();
        }
    }
}
