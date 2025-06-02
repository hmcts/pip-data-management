package uk.gov.hmcts.reform.pip.data.management.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.CaseFormat;
import org.apache.commons.text.CaseUtils;
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
    private static final String XLSX_CONTENT_TYPE
        = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";

    public String convert(MultipartFile file) {
        String contentType = file.getContentType();
        if (contentType != null && !XLSX_CONTENT_TYPE.equals(contentType)) {
            throw new ExcelConversionException("Invalid Excel file type");
        }

        try (Workbook workbook = new XSSFWorkbook(file.getInputStream())) {
            if (hasMultipleSheets(workbook)) {
                return OBJECT_MAPPER.writeValueAsString(getMultiSheetData(workbook));
            } else {
                return OBJECT_MAPPER.writeValueAsString(getSingleSheetData(workbook));
            }
        } catch (IOException e) {
            throw new ExcelConversionException("Error converting Excel file into JSON format");
        }
    }

    private boolean hasMultipleSheets(Workbook workbook) {
        return workbook.getNumberOfSheets() > 1;
    }

    private List<Map<String, String>> getSingleSheetData(Workbook workbook) {
        Sheet sheet = workbook.getSheetAt(0);
        return getSheetData(sheet);
    }

    @SuppressWarnings("PMD.UseConcurrentHashMap")
    private Map<String, List<Map<String, String>>> getMultiSheetData(Workbook workbook) {
        Map<String, List<Map<String, String>>> data = new LinkedHashMap<>();

        for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
            Sheet sheet = workbook.getSheetAt(i);
            String sheetName = CaseUtils.toCamelCase(sheet.getSheetName(), false, ' ');
            data.put(sheetName, getSheetData(sheet));
        }

        return data;
    }

    private List<Map<String, String>> getSheetData(Sheet sheet) {
        int headerRowNumber = sheet.getFirstRowNum();
        int firstColumnNumber = sheet.getRow(headerRowNumber).getFirstCellNum();

        List<String> headers = getExcelRow(sheet, headerRowNumber, firstColumnNumber);
        List<Map<String, String>> data = new ArrayList<>();

        for (int rowNumber = headerRowNumber + 1; rowNumber <= sheet.getLastRowNum(); rowNumber++) {
            List<String> row = getExcelRow(sheet, rowNumber, firstColumnNumber);
            Map<String, String> rowMappings = buildRowMap(headers, row);

            if (rowMappings.values().stream().allMatch(String::isBlank)) {
                break;
            }

            data.add(rowMappings);
        }

        return data;
    }

    @SuppressWarnings("PMD.UseConcurrentHashMap")
    private Map<String, String> buildRowMap(List<String> headers, List<String> row) {
        // Link hashmap is used to ensure insertion order on the row values
        Map<String, String> values = new LinkedHashMap<>();
        for (int headerNumber = 0; headerNumber < headers.size(); headerNumber++) {
            String upperUnderscoreHeader = headers.get(headerNumber)
                .toUpperCase(Locale.ENGLISH)
                .replaceAll(" ", "_")
                .replaceAll("[()]", "");
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
                    String.format("Unexpected cell type on row %s, column %s",rowNumber + 1, columnNumber + 1));
            };

            rowData.add(cellValue);
        }
        return rowData;
    }

    private String formatNumericCell(Cell cell) {
        if (DateUtil.isCellDateFormatted(cell)) {
            SimpleDateFormat formatter = cell.getNumericCellValue() < 1
                ? new SimpleDateFormat("h:mma", Locale.UK)
                : new SimpleDateFormat("dd/MM/yyyy", Locale.UK);
            return formatter.format(cell.getDateCellValue());
        } else {
            cell.setCellType(CellType.STRING);
            return cell.getStringCellValue();
        }
    }
}
