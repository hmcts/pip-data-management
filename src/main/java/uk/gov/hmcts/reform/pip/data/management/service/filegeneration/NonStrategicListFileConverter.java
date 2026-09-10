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
import uk.gov.hmcts.reform.pip.data.management.service.ExcelConversionService;
import uk.gov.hmcts.reform.pip.data.management.service.helpers.DateHelper;
import uk.gov.hmcts.reform.pip.data.management.service.helpers.LanguageResourceHelper;
import uk.gov.hmcts.reform.pip.data.management.service.helpers.NonStrategicFieldFormattingHelper;
import uk.gov.hmcts.reform.pip.data.management.service.helpers.NonStrategicListFormatter;
import uk.gov.hmcts.reform.pip.model.publication.Language;
import uk.gov.hmcts.reform.pip.model.publication.ListType;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

import static com.google.common.base.CaseFormat.LOWER_CAMEL;
import static com.google.common.base.CaseFormat.UPPER_UNDERSCORE;
import static uk.gov.hmcts.reform.pip.model.publication.ListType.CIVIL_COURTS_RCJ_DAILY_CAUSE_LIST;
import static uk.gov.hmcts.reform.pip.model.publication.ListType.COUNTY_COURT_LONDON_CIVIL_DAILY_CAUSE_LIST;
import static uk.gov.hmcts.reform.pip.model.publication.ListType.COURT_OF_APPEAL_CIVIL_DAILY_CAUSE_LIST;
import static uk.gov.hmcts.reform.pip.model.publication.ListType.COURT_OF_APPEAL_CRIMINAL_DAILY_CAUSE_LIST;
import static uk.gov.hmcts.reform.pip.model.publication.ListType.BIRMINGHAM_ADMINISTRATIVE_COURT_DAILY_CAUSE_LIST;
import static uk.gov.hmcts.reform.pip.model.publication.ListType.BRISTOL_AND_CARDIFF_ADMINISTRATIVE_COURT_DAILY_CAUSE_LIST;
import static uk.gov.hmcts.reform.pip.model.publication.ListType.FAMILY_DIVISION_HIGH_COURT_DAILY_CAUSE_LIST;
import static uk.gov.hmcts.reform.pip.model.publication.ListType.KINGS_BENCH_DIVISION_DAILY_CAUSE_LIST;
import static uk.gov.hmcts.reform.pip.model.publication.ListType.KINGS_BENCH_MASTERS_DAILY_CAUSE_LIST;
import static uk.gov.hmcts.reform.pip.model.publication.ListType.LEEDS_ADMINISTRATIVE_COURT_DAILY_CAUSE_LIST;
import static uk.gov.hmcts.reform.pip.model.publication.ListType.LONDON_ADMINISTRATIVE_COURT_DAILY_CAUSE_LIST;
import static uk.gov.hmcts.reform.pip.model.publication.ListType.MANCHESTER_ADMINISTRATIVE_COURT_DAILY_CAUSE_LIST;
import static uk.gov.hmcts.reform.pip.model.publication.ListType.MAYOR_AND_CITY_CIVIL_DAILY_CAUSE_LIST;
import static uk.gov.hmcts.reform.pip.model.publication.ListType.SENIOR_COURTS_COSTS_OFFICE_DAILY_CAUSE_LIST;
import static uk.gov.hmcts.reform.pip.model.publication.ListType.UT_T_AND_CC_DAILY_HEARING_LIST;
import static uk.gov.hmcts.reform.pip.model.publication.ListType.UT_LC_DAILY_HEARING_LIST;
import static uk.gov.hmcts.reform.pip.model.publication.ListType.UT_IAC_STATUTORY_APPEALS_DAILY_HEARING_LIST;
import static uk.gov.hmcts.reform.pip.model.publication.ListType.UT_IAC_JR_LONDON_DAILY_HEARING_LIST;
import static uk.gov.hmcts.reform.pip.model.publication.ListType.UT_IAC_JR_LEEDS_DAILY_HEARING_LIST;
import static uk.gov.hmcts.reform.pip.model.publication.ListType.UT_IAC_JR_MANCHESTER_DAILY_HEARING_LIST;
import static uk.gov.hmcts.reform.pip.model.publication.ListType.UT_IAC_JR_BIRMINGHAM_DAILY_HEARING_LIST;
import static uk.gov.hmcts.reform.pip.model.publication.ListType.UT_IAC_JR_CARDIFF_DAILY_HEARING_LIST;
import static uk.gov.hmcts.reform.pip.model.publication.ListType.UT_AAC_DAILY_HEARING_LIST;


public class NonStrategicListFileConverter extends ExcelAbstractList implements FileConverter {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final String SINGLE_SHEET_NAME = "Sheet1";
    private static final String COMMON_NON_STRATEGIC_HEADERS = "commonNsDailyCauseListTableHeaders";
    private static final String TABLE_HEADERS = "tableHeaders";

    private static final Map<ListType, Map<String, String>> LIST_TYPE_HEADER_FIELDS = Map.ofEntries(
        Map.entry(UT_AAC_DAILY_HEARING_LIST,
                  Map.of(SINGLE_SHEET_NAME, TABLE_HEADERS)),
        Map.entry(UT_IAC_JR_CARDIFF_DAILY_HEARING_LIST,
                  Map.of(SINGLE_SHEET_NAME, TABLE_HEADERS)),
        Map.entry(UT_IAC_JR_BIRMINGHAM_DAILY_HEARING_LIST,
                  Map.of(SINGLE_SHEET_NAME, TABLE_HEADERS)),
        Map.entry(UT_IAC_JR_MANCHESTER_DAILY_HEARING_LIST,
                  Map.of(SINGLE_SHEET_NAME, TABLE_HEADERS)),
        Map.entry(UT_IAC_JR_LEEDS_DAILY_HEARING_LIST,
                  Map.of(SINGLE_SHEET_NAME, TABLE_HEADERS)),
        Map.entry(UT_IAC_JR_LONDON_DAILY_HEARING_LIST,
                  Map.of(SINGLE_SHEET_NAME, TABLE_HEADERS)),
        Map.entry(UT_IAC_STATUTORY_APPEALS_DAILY_HEARING_LIST,
                  Map.of(SINGLE_SHEET_NAME, TABLE_HEADERS)),
        Map.entry(UT_T_AND_CC_DAILY_HEARING_LIST,
                  Map.of(SINGLE_SHEET_NAME, TABLE_HEADERS)),
        Map.entry(UT_LC_DAILY_HEARING_LIST,
                  Map.of(SINGLE_SHEET_NAME, TABLE_HEADERS)),
        Map.entry(BIRMINGHAM_ADMINISTRATIVE_COURT_DAILY_CAUSE_LIST,
                  Map.of(SINGLE_SHEET_NAME, COMMON_NON_STRATEGIC_HEADERS)),
        Map.entry(BRISTOL_AND_CARDIFF_ADMINISTRATIVE_COURT_DAILY_CAUSE_LIST,
                  Map.of(SINGLE_SHEET_NAME, COMMON_NON_STRATEGIC_HEADERS)),
        Map.entry(LEEDS_ADMINISTRATIVE_COURT_DAILY_CAUSE_LIST,
                  Map.of(SINGLE_SHEET_NAME, COMMON_NON_STRATEGIC_HEADERS)),
        Map.entry(LONDON_ADMINISTRATIVE_COURT_DAILY_CAUSE_LIST,
                  Map.of("London administrative court", COMMON_NON_STRATEGIC_HEADERS,
                         "Planning court", COMMON_NON_STRATEGIC_HEADERS)),
        Map.entry(MANCHESTER_ADMINISTRATIVE_COURT_DAILY_CAUSE_LIST,
                  Map.of(SINGLE_SHEET_NAME, COMMON_NON_STRATEGIC_HEADERS)),
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
    public byte[] convertToExcel(JsonNode payload, ListType listType, Map<String, String> metadata,
                                 InputStream inputExcel) throws IOException {
        if (inputExcel != null && listType.hasExcel()) {
            Language language = Language.valueOf(metadata.get("language"));
            try (Workbook workbook = new XSSFWorkbook(inputExcel)) {
                Map<String, Object> languageResources = LanguageResourceHelper.getLanguageResources(listType, language);
                addAdditionalLanguageResources(metadata, languageResources);


                Map<String, String> headerFields = LIST_TYPE_HEADER_FIELDS.get(listType);
                Optional<Map<String, Function<String, String>>> listTypeFormatter = NonStrategicListFormatter
                    .getListTypeFormatter(listType);
                updateExcelValues(workbook, languageResources, headerFields, listTypeFormatter);
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
    private void updateExcelValues(Workbook workbook, Map<String, Object> languageResources,
                                   Map<String, String> headerFields,
                                   Optional<Map<String, Function<String, String>>> listTypeFormatter) {
        if (!headerFields.isEmpty()) {
            for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
                Sheet sheet = workbook.getSheetAt(i);
                List<String> headersToUpdate = workbook.getNumberOfSheets() > 1
                    ? (List<String>) languageResources.get(headerFields.get(sheet.getSheetName()))
                    : (List<String>) languageResources.get(headerFields.get(SINGLE_SHEET_NAME));

                int headerRowNumber = sheet.getFirstRowNum();
                int firstColumnNumber = sheet.getRow(headerRowNumber).getFirstCellNum();
                Row headerRow = sheet.getRow(headerRowNumber);

                int lastCellNum = headerRow.getLastCellNum();
                if (lastCellNum < 0 || firstColumnNumber >= lastCellNum) {
                    return;
                }

                for (int rowNumber = headerRowNumber + 1; rowNumber <= sheet.getLastRowNum(); rowNumber++) {
                    Row currentRow = sheet.getRow(rowNumber);
                    formatRowValues(currentRow, headerRow, firstColumnNumber, lastCellNum, listTypeFormatter);
                }
                updateHeaders(workbook, headerRow, firstColumnNumber, lastCellNum, headersToUpdate);
            }
        }
    }

    private void updateHeaders(Workbook workbook, Row headerRow, int firstColumnNumber,
                               int lastCellNum, List<String> headersToUpdate) {
        CellStyle boldStyle = createBoldStyle(workbook);
        for (int columnNumber = firstColumnNumber, headerIndex = 0; columnNumber < lastCellNum;
             columnNumber++, headerIndex++) {
            Cell headerCell = headerRow.getCell(columnNumber, Row.MissingCellPolicy.RETURN_NULL_AND_BLANK);
            headerCell.setCellValue(headersToUpdate.get(headerIndex));
            headerCell.setCellStyle(boldStyle);
        }
    }

    private void formatRowValues(Row currentRow, Row headerRow, int firstColumnNumber, int lastCellNum,
                                 Optional<Map<String, Function<String, String>>> listTypeFormatter) {
        if (currentRow != null && listTypeFormatter.isPresent()) {
            for (int columnNumber = firstColumnNumber; columnNumber < lastCellNum; columnNumber++) {
                Cell headerCell = headerRow.getCell(columnNumber, Row.MissingCellPolicy.RETURN_NULL_AND_BLANK);
                String formattedHeader = NonStrategicFieldFormattingHelper.formatFieldInLowerCamelCaseFormat(
                    headerCell.getStringCellValue()
                );

                if (listTypeFormatter.get().containsKey(formattedHeader)) {
                    Cell cell = currentRow.getCell(columnNumber, Row.MissingCellPolicy.RETURN_NULL_AND_BLANK);
                    String formattedCellValue = NonStrategicListFormatter.formatField(
                        formattedHeader,
                        ExcelConversionService.getExcelCellValue(cell),
                        listTypeFormatter.get()
                    );
                    cell.setCellValue(formattedCellValue);
                }
            }
        }
    }
}
