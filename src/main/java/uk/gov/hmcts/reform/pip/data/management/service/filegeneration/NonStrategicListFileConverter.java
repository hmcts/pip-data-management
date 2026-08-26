package uk.gov.hmcts.reform.pip.data.management.service.filegeneration;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.thymeleaf.context.Context;
import uk.gov.hmcts.reform.pip.data.management.errorhandling.exceptions.ExcelConversionException;
import uk.gov.hmcts.reform.pip.data.management.service.helpers.DateHelper;
import uk.gov.hmcts.reform.pip.data.management.service.helpers.LanguageResourceHelper;
import uk.gov.hmcts.reform.pip.data.management.service.helpers.NonStrategicListFormatter;
import uk.gov.hmcts.reform.pip.model.publication.Language;
import uk.gov.hmcts.reform.pip.model.publication.ListType;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.google.common.base.CaseFormat.LOWER_CAMEL;
import static com.google.common.base.CaseFormat.UPPER_UNDERSCORE;
import static uk.gov.hmcts.reform.pip.model.publication.ListType.CIVIL_COURTS_RCJ_DAILY_CAUSE_LIST;
import static uk.gov.hmcts.reform.pip.model.publication.ListType.COUNTY_COURT_LONDON_CIVIL_DAILY_CAUSE_LIST;
import static uk.gov.hmcts.reform.pip.model.publication.ListType.COURT_OF_APPEAL_CIVIL_DAILY_CAUSE_LIST;
import static uk.gov.hmcts.reform.pip.model.publication.ListType.COURT_OF_APPEAL_CRIMINAL_DAILY_CAUSE_LIST;
import static uk.gov.hmcts.reform.pip.model.publication.ListType.FAMILY_DIVISION_HIGH_COURT_DAILY_CAUSE_LIST;
import static uk.gov.hmcts.reform.pip.model.publication.ListType.KINGS_BENCH_DIVISION_DAILY_CAUSE_LIST;
import static uk.gov.hmcts.reform.pip.model.publication.ListType.KINGS_BENCH_MASTERS_DAILY_CAUSE_LIST;
import static uk.gov.hmcts.reform.pip.model.publication.ListType.LONDON_ADMINISTRATIVE_COURT_DAILY_CAUSE_LIST;
import static uk.gov.hmcts.reform.pip.model.publication.ListType.MAYOR_AND_CITY_CIVIL_DAILY_CAUSE_LIST;
import static uk.gov.hmcts.reform.pip.model.publication.ListType.SENIOR_COURTS_COSTS_OFFICE_DAILY_CAUSE_LIST;

public class NonStrategicListFileConverter extends ExcelAbstractList implements FileConverter {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final String SINGLE_SHEET_NAME = "Sheet1";
    private static final String COMMON_NON_STRATEGIC_HEADERS = "commonNsDailyCauseListTableHeaders";

    private static final Map<ListType, Map<String, String>> LIST_TYPE_HEADER_FIELDS = Map.ofEntries(
        Map.entry(LONDON_ADMINISTRATIVE_COURT_DAILY_CAUSE_LIST,
                  Map.of("London administrative court", COMMON_NON_STRATEGIC_HEADERS,
                         "Planning court", COMMON_NON_STRATEGIC_HEADERS)),
        Map.entry(COUNTY_COURT_LONDON_CIVIL_DAILY_CAUSE_LIST,
                  Map.of(SINGLE_SHEET_NAME, COMMON_NON_STRATEGIC_HEADERS)),
        Map.entry(CIVIL_COURTS_RCJ_DAILY_CAUSE_LIST,
                  Map.of(SINGLE_SHEET_NAME, COMMON_NON_STRATEGIC_HEADERS)),
        Map.entry(COURT_OF_APPEAL_CIVIL_DAILY_CAUSE_LIST,
                  Map.of("Hearing list", COMMON_NON_STRATEGIC_HEADERS,
                         "Future judgments", "futureJudgementTableHeaders")),
        Map.entry(COURT_OF_APPEAL_CRIMINAL_DAILY_CAUSE_LIST,
                  Map.of(SINGLE_SHEET_NAME, COMMON_NON_STRATEGIC_HEADERS)),
        Map.entry(FAMILY_DIVISION_HIGH_COURT_DAILY_CAUSE_LIST,
                  Map.of(SINGLE_SHEET_NAME, COMMON_NON_STRATEGIC_HEADERS)),
        Map.entry(KINGS_BENCH_DIVISION_DAILY_CAUSE_LIST,
                  Map.of(SINGLE_SHEET_NAME, COMMON_NON_STRATEGIC_HEADERS)),
        Map.entry(KINGS_BENCH_MASTERS_DAILY_CAUSE_LIST,
                  Map.of(SINGLE_SHEET_NAME, COMMON_NON_STRATEGIC_HEADERS)),
        Map.entry(SENIOR_COURTS_COSTS_OFFICE_DAILY_CAUSE_LIST,
                  Map.of(SINGLE_SHEET_NAME, COMMON_NON_STRATEGIC_HEADERS)),
        Map.entry(MAYOR_AND_CITY_CIVIL_DAILY_CAUSE_LIST,
                  Map.of(SINGLE_SHEET_NAME, COMMON_NON_STRATEGIC_HEADERS))
    );

    @Override
    public String convert(JsonNode payload, Map<String, String> metadata, Map<String, Object> languageResources)
        throws IOException {
        Context context = new Context();
        context.setVariable("contentDate", metadata.get("contentDate"));
        context.setVariable("provenance", metadata.get("provenance"));

        Language language = Language.valueOf(metadata.get("language"));
        context.setVariable("lastUpdatedDate", DateHelper.formatTimeStampToBst(
            metadata.get("lastReceivedDate"), language, false, false
        ));
        context.setVariable("lastUpdatedTime", DateHelper.formatTimeStampToBst(
            metadata.get("lastReceivedDate"), language, true, false
        ));
        context.setVariable("i18n", languageResources);

        addAdditionalLanguageResources(metadata, languageResources);
        String listType = metadata.get("listType");

        try {
            List<Map<String, String>> data = OBJECT_MAPPER.convertValue(payload, new TypeReference<>(){});
            List<Map<String, String>> formattedData = NonStrategicListFormatter.formatAllFields(
                data, ListType.valueOf(listType)
            );
            context.setVariable("data", formattedData);
        } catch (IllegalArgumentException e) {
            Set<Map.Entry<String, JsonNode>> fields = payload.properties();
            for (Map.Entry<String, JsonNode> entry : fields) {
                String sheetName = entry.getKey();
                JsonNode sheetData = entry.getValue();
                List<Map<String, String>> sheetList = OBJECT_MAPPER.convertValue(sheetData, new TypeReference<>(){});
                List<Map<String, String>> formattedSheetData = NonStrategicListFormatter.formatAllFields(
                    sheetList, ListType.valueOf(listType)
                );
                context.setVariable(sheetName, formattedSheetData);
            }
        }

        return TemplateEngine.processNonStrategicTemplate(listType, context);
    }

    @Override
    public byte[] convertToExcel(JsonNode artefact, ListType listType, Map<String, String> metadata,
                                 InputStream inputExcel) throws IOException {
        if (inputExcel != null && listType.hasExcel()) {
            Language language = Language.valueOf(metadata.get("language"));
            try (Workbook workbook = new XSSFWorkbook(inputExcel)) {
                Map<String, Object> languageResources = LanguageResourceHelper.getLanguageResources(listType, language);
                addAdditionalLanguageResources(metadata, languageResources);

                Map<String, String> headerFields = LIST_TYPE_HEADER_FIELDS.get(listType);
                updateExcelHeaders(workbook, languageResources, headerFields);
                return ExcelAbstractList.convertToByteArray(workbook);
            } catch (IOException e) {
                throw new ExcelConversionException("Error generating non-strategic excel file");
            }
        }
        return new byte[0];
    }

    private void addAdditionalLanguageResources(Map<String, String> metadata, Map<String, Object> languageResources)
        throws IOException {
        String resourceName;
        String listType = metadata.get("listType");
        Language language = Language.valueOf(metadata.get("language"));
        if (ListType.valueOf(listType).getParentListType() != null) {
            resourceName = "non-strategic/" + UPPER_UNDERSCORE.to(
                LOWER_CAMEL, ListType.valueOf(listType).getParentListType().name()
            );
            languageResources.putAll(LanguageResourceHelper.readResourcesFromPath(resourceName, language));
        }
        resourceName = "non-strategic/" + UPPER_UNDERSCORE.to(LOWER_CAMEL, listType);
        languageResources.putAll(LanguageResourceHelper.readResourcesFromPath(resourceName, language));
        languageResources.putAll(LanguageResourceHelper.readResourcesFromPath("common/nonStrategicCommon",
                                                                              language));
        languageResources.putAll(LanguageResourceHelper.readResourcesFromPath("common/linkToFact",
                                                                              language));
    }

    @SuppressWarnings("unchecked")
    private void updateExcelHeaders(Workbook workbook, Map<String, Object> languageResources,
                                    Map<String, String> headerFields) {
        if (!headerFields.isEmpty()) {
            CellStyle boldStyle = createBoldStyle(workbook);
            for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
                Sheet sheet = workbook.getSheetAt(i);

                List<String> headersToUpdate;
                if (workbook.getNumberOfSheets() > 1) {
                    String headerFieldName = headerFields.get(sheet.getSheetName());
                    headersToUpdate = (List<String>) languageResources.get(headerFieldName);
                } else {
                    String headerFieldName = headerFields.get(SINGLE_SHEET_NAME);
                    headersToUpdate = (List<String>) languageResources.get(headerFieldName);
                }

                int headerRowNumber = sheet.getFirstRowNum();
                int firstColumnNumber = sheet.getRow(headerRowNumber).getFirstCellNum();
                Row row = sheet.getRow(headerRowNumber);

                int lastCellNum = row.getLastCellNum();
                if (lastCellNum < 0 || firstColumnNumber >= lastCellNum) {
                    return;
                }

                for (int columnNumber = firstColumnNumber, headerIndex = 0; columnNumber < lastCellNum;
                     columnNumber++, headerIndex++) {
                    Cell cell = row.getCell(columnNumber, Row.MissingCellPolicy.RETURN_NULL_AND_BLANK);
                    cell.setCellValue(headersToUpdate.get(headerIndex));
                    cell.setCellStyle(boldStyle);
                }
            }
        }
    }
}
