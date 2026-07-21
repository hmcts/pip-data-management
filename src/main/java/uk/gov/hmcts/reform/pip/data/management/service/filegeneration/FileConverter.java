package uk.gov.hmcts.reform.pip.data.management.service.filegeneration;

import com.fasterxml.jackson.databind.JsonNode;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import uk.gov.hmcts.reform.pip.data.management.service.helpers.LanguageResourceHelper;
import uk.gov.hmcts.reform.pip.model.publication.Language;
import uk.gov.hmcts.reform.pip.model.publication.ListType;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.slf4j.LoggerFactory.getLogger;

@FunctionalInterface
public interface FileConverter {
    Logger log = getLogger(FileConverter.class);

    /**
     * Interface method that captures the conversion of an artefact to a Html File.
     *
     * @return The converted HTML as a string;
     */
    String convert(JsonNode artefact, Map<String, String> metadata, Map<String, Object> language) throws IOException;

    /**
     * Interface method that captures the conversion of an artefact to an Excel spreadsheet.
     *
     * @return The converted Excel spreadsheet as a byte array.
     */
    default byte[] convertToExcel(JsonNode artefact, ListType listType, Language language) throws IOException {
        Map<String, Object> languageResources = LanguageResourceHelper.getLanguageResources(listType,
                                                                                            language);
        List<String> headers = getExcelHeaders(artefact, languageResources);
        List<List<String>> rows = getExcelRows(artefact, languageResources, language);

        if (headers.isEmpty() && rows.isEmpty()) {
            return new byte[0];
        }

        try (Workbook workbook = new XSSFWorkbook()) {
            // The sheet name can only be 31 characters long, so we will use a generic name for the sheet.
            Sheet sheet = workbook.createSheet("Sheet1");
            CellStyle boldStyle = ExcelAbstractList.createBoldStyle(workbook);

            int rowIdx = 0;
            Row headingRow = sheet.createRow(rowIdx++);
            for (int i = 0; i < headers.size(); i++) {
                ExcelAbstractList.setCellValue(headingRow, i, headers.get(i), boldStyle);
            }

            for (List<String> rowData : rows) {
                Row dataRow = sheet.createRow(rowIdx++);
                for (int i = 0; i < rowData.size(); i++) {
                    ExcelAbstractList.setCellValue(dataRow, i, rowData.get(i));
                }
            }

            ExcelAbstractList.autoSizeSheet(sheet);
            return ExcelAbstractList.convertToByteArray(workbook);
        }
    }

    default List<String> getExcelHeaders(Map<String, Object> languageResources) {
        return new ArrayList<>();
    }

    default List<String> getExcelHeaders(JsonNode artefact, Map<String, Object> languageResources) {
        return getExcelHeaders(languageResources);
    }

    default List<List<String>> getExcelRows(JsonNode artefact, Map<String, Object> languageResources,
                                            Language language) {
        return new ArrayList<>();
    }
}
