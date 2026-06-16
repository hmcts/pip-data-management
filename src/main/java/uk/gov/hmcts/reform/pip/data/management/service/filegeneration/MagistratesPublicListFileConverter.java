package uk.gov.hmcts.reform.pip.data.management.service.filegeneration;

import com.fasterxml.jackson.databind.JsonNode;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.thymeleaf.context.Context;
import uk.gov.hmcts.reform.pip.data.management.models.templatemodels.MagistratesPublicList;
import uk.gov.hmcts.reform.pip.data.management.models.templatemodels.magistratespubliclist.CourtRoom;
import uk.gov.hmcts.reform.pip.data.management.service.helpers.DateHelper;
import uk.gov.hmcts.reform.pip.data.management.service.helpers.GeneralHelper;
import uk.gov.hmcts.reform.pip.data.management.service.helpers.LanguageResourceHelper;
import uk.gov.hmcts.reform.pip.data.management.service.helpers.LocationHelper;
import uk.gov.hmcts.reform.pip.data.management.service.helpers.listmanipulation.MagistratesPublicListHelper;
import uk.gov.hmcts.reform.pip.model.publication.Language;
import uk.gov.hmcts.reform.pip.model.publication.ListType;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MagistratesPublicListFileConverter extends ExcelAbstractList implements FileConverter {
    @Override
    public String convert(JsonNode artefact, Map<String, String> metadata, Map<String, Object> language)
        throws IOException {
        return TemplateEngine.processTemplate(
            metadata.get("listType"),
            preprocessArtefactForThymeLeafConverter(artefact, metadata, language)
        );
    }

    @Override
    public byte[] convertToExcel(JsonNode artefact, ListType listType) throws IOException {
        try (Workbook workbook = new XSSFWorkbook()) {
            Map<String, Object> languageResources = LanguageResourceHelper.getLanguageResources(listType,
                                                                                                 Language.ENGLISH);
            List<MagistratesPublicList> cases = processRawListData(artefact, languageResources);

            Sheet sheet = workbook.createSheet(listType.getFriendlyName());
            CellStyle boldStyle = createBoldStyle(workbook);

            int rowIdx = 0;
            Row headingRow = sheet.createRow(rowIdx++);
            List<String> headers = getExcelHeaders(languageResources);
            for (int i = 0; i < headers.size(); i++) {
                setCellValue(headingRow, i, headers.get(i), boldStyle);
            }

            for (List<String> rowData : getExcelRows(cases)) {
                Row dataRow = sheet.createRow(rowIdx++);
                for (int i = 0; i < rowData.size(); i++) {
                    setCellValue(dataRow, i, rowData.get(i));
                }
            }

            autoSizeSheet(sheet);
            return convertToByteArray(workbook);
        }
    }

    private List<String> getExcelHeaders(Map<String, Object> languageResources) {
        @SuppressWarnings("unchecked")
        List<String> headerValuesNoWrap = (List<String>) languageResources.get("headerValuesNoWrap");
        @SuppressWarnings("unchecked")
        List<String> headerValuesWrap = (List<String>) languageResources.get("headerValuesWrap");

        return List.of(
            languageResources.get("courtHouse").toString(),
            languageResources.get("courtRoom").toString(),
            headerValuesNoWrap.get(0),
            headerValuesWrap.get(0),
            headerValuesWrap.get(1),
            headerValuesWrap.get(2),
            headerValuesWrap.get(3),
            languageResources.get("offenceDetails").toString(),
            languageResources.get("reportingRestrictions").toString()
        );
    }

    private List<List<String>> getExcelRows(List<MagistratesPublicList> cases) {
        List<List<String>> rows = new ArrayList<>();
        cases.forEach(caseItem -> rows.add(List.of(
            caseItem.getCourtHouse(),
            caseItem.getCourtRoom(),
            caseItem.getSittingAt(),
            caseItem.getUrn(),
            caseItem.getName(),
            caseItem.getHearingType(),
            caseItem.getProsecutingAuthority(),
            caseItem.getOffence(),
            caseItem.getReportingRestriction()
        )));
        return rows;
    }

    private List<MagistratesPublicList> processRawListData(JsonNode artefact, Map<String, Object> languageResources) {
        List<MagistratesPublicList> cases = new ArrayList<>();
        List<CourtRoom> processedData = MagistratesPublicListHelper.processRawListData(artefact);

        processedData.forEach(courtRoom -> courtRoom.getSittings().forEach(sitting ->
            sitting.getHearings().forEach(hearing ->
                cases.add(new MagistratesPublicList(
                    "",
                    courtRoom.getFormattedCourtRoomName(),
                    sitting.getTime(),
                    hearing.getCaseUrn(),
                    hearing.getDefendant(),
                    hearing.getHearingType(),
                    hearing.getProsecutingAuthority(),
                    hearing.getOffence(),
                    hearing.isReportingRestriction()
                        ? languageResources.get("reportingRestrictionText").toString() : ""
                ))
            )
        ));
        return cases;
    }

    private Context preprocessArtefactForThymeLeafConverter(
        JsonNode artefact, Map<String, String> metadata, Map<String, Object> languageResources) throws IOException {
        Context context = new Context();
        context.setVariable("metadata", metadata);
        context.setVariable("i18n", languageResources);

        Language language = Language.valueOf(metadata.get("language"));
        languageResources.putAll(LanguageResourceHelper.readResourcesFromPath("common/linkToFact", language));
        String publicationDate = artefact.get("document").get("publicationDate").asText();
        context.setVariable("publicationDate", DateHelper.formatTimeStampToBst(publicationDate, language,
                                                                               false, false));
        context.setVariable("publicationTime", DateHelper.formatTimeStampToBst(publicationDate, language,
                                                                               true, false));

        context.setVariable("contentDate", metadata.get("contentDate"));
        context.setVariable("locationName", metadata.get("locationName"));
        context.setVariable("provenance", metadata.get("provenance"));
        context.setVariable("venueAddress", LocationHelper.formatFullVenueAddress(artefact));
        context.setVariable("artefact", artefact);
        MagistratesPublicListHelper.manipulatedMagistratesPublicListData(artefact);

        return context;
    }
}
