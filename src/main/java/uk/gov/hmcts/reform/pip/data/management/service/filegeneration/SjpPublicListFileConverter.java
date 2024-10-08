package uk.gov.hmcts.reform.pip.data.management.service.filegeneration;

import com.fasterxml.jackson.databind.JsonNode;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.thymeleaf.context.Context;
import uk.gov.hmcts.reform.pip.data.management.models.templatemodels.SjpPublicList;
import uk.gov.hmcts.reform.pip.data.management.service.helpers.DateHelper;
import uk.gov.hmcts.reform.pip.data.management.service.helpers.listmanipulation.SjpPublicListHelper;
import uk.gov.hmcts.reform.pip.model.publication.Language;
import uk.gov.hmcts.reform.pip.model.publication.ListType;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

public class SjpPublicListFileConverter extends ExcelAbstractList implements FileConverter {
    /**
     * Convert SJP public cases into HMTL file for PDF generation.
     *
     * @param artefact Tree object model for artefact
     * @param metadata Artefact metadata
     * @return the HTML representation of the SJP public cases
     */
    @Override
    public String convert(JsonNode artefact, Map<String, String> metadata, Map<String,Object> language) {
        Context context = new Context();
        String publicationDate = DateHelper.formatTimeStampToBst(
            artefact.get("document").get("publicationDate").textValue(), Language.valueOf(metadata.get("language")),
            false,
            true
        );
        context.setVariable("publicationDate", publicationDate);
        context.setVariable("contentDate", metadata.get("contentDate"));
        context.setVariable("i18n", language);
        context.setVariable("cases", processRawListData(artefact));
        return TemplateEngine.processTemplate(metadata.get("listType"), context);
    }

    /**
     * Create SJP public list Excel spreadsheet from list data.
     *
     * @param artefact Tree object model for artefact.
     * @param listType The list type of the publication.
     * @return The converted Excel spreadsheet as a byte array.
     */
    @Override
    public byte[] convertToExcel(JsonNode artefact, ListType listType) throws IOException {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet(listType.getFriendlyName());
            CellStyle boldStyle = createBoldStyle(workbook);
            AtomicInteger rowIdx = new AtomicInteger();
            Row headingRow = sheet.createRow(rowIdx.getAndIncrement());
            setCellValue(headingRow, 0, "Name", boldStyle);
            setCellValue(headingRow, 1, "Postcode", boldStyle);
            setCellValue(headingRow, 2, "Offence", boldStyle);
            setCellValue(headingRow, 3, "Prosecutor", boldStyle);

            processRawListData(artefact).forEach(entry -> {
                Row dataRow = sheet.createRow(rowIdx.getAndIncrement());
                setCellValue(dataRow, 0, entry.getName());
                setCellValue(dataRow, 1, entry.getPostcode());
                setCellValue(dataRow, 2, entry.getOffence());
                setCellValue(dataRow, 3, entry.getProsecutor());
            });
            autoSizeSheet(sheet);

            return convertToByteArray(workbook);
        }
    }

    private List<SjpPublicList> processRawListData(JsonNode data) {
        List<SjpPublicList> sjpCases = new ArrayList<>();

        data.get("courtLists").forEach(
            courtList -> courtList.get("courtHouse").get("courtRoom").forEach(
                courtRoom -> courtRoom.get("session").forEach(
                    session -> session.get("sittings").forEach(
                        sitting -> sitting.get("hearing").forEach(hearing -> {
                            Optional<SjpPublicList> sjpCase = SjpPublicListHelper.constructSjpCase(hearing);
                            if (sjpCase.isPresent()) {
                                sjpCases.add(sjpCase.get());
                            }
                        })
                    )
                )
            )
        );
        return sjpCases;
    }
}
