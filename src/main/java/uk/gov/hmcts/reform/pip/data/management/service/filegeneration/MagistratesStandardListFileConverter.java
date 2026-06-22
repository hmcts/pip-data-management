package uk.gov.hmcts.reform.pip.data.management.service.filegeneration;

import com.fasterxml.jackson.databind.JsonNode;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.thymeleaf.context.Context;
import uk.gov.hmcts.reform.pip.data.management.models.templatemodels.MagistratesStandardList;
import uk.gov.hmcts.reform.pip.data.management.models.templatemodels.magistratesstandardlist.CourtRoom;
import uk.gov.hmcts.reform.pip.data.management.service.helpers.DateHelper;
import uk.gov.hmcts.reform.pip.data.management.service.helpers.LanguageResourceHelper;
import uk.gov.hmcts.reform.pip.data.management.service.helpers.LocationHelper;
import uk.gov.hmcts.reform.pip.data.management.service.helpers.listmanipulation.MagistratesStandardListHelper;
import uk.gov.hmcts.reform.pip.model.publication.Language;
import uk.gov.hmcts.reform.pip.model.publication.ListType;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MagistratesStandardListFileConverter extends ExcelAbstractList implements FileConverter {
    @Override
    public String convert(JsonNode artefact, Map<String, String> metadata, Map<String, Object> languageResources)
        throws IOException {
        Context context = new Context();
        Language language = Language.valueOf(metadata.get("language"));
        languageResources.putAll(LanguageResourceHelper.readResourcesFromPath("common/linkToFact", language));

        setPublicationDateTime(context, artefact.get("document").get("publicationDate").asText(), language);
        context.setVariable("i18n", languageResources);

        context.setVariable("contentDate", metadata.get("contentDate"));
        context.setVariable("provenance", metadata.get("provenance"));

        context.setVariable("locationName", metadata.get("locationName"));
        context.setVariable("venueAddress", LocationHelper.formatFullVenueAddress(artefact));

        context.setVariable("courtRooms", MagistratesStandardListHelper.processRawListData(artefact));

        return TemplateEngine.processTemplate(metadata.get("listType"), context);
    }

    @Override
    public byte[] convertToExcel(JsonNode artefact, ListType listType) throws IOException {
        try (Workbook workbook = new XSSFWorkbook()) {
            final List<MagistratesStandardList> cases = processRawListData(artefact);
            Map<String, Object> languageResources = LanguageResourceHelper.getLanguageResources(listType,
                                                                                                 Language.ENGLISH);

            Sheet sheet = workbook.createSheet(listType.getFriendlyName());
            CellStyle boldStyle = createBoldStyle(workbook);

            int rowIdx = 0;
            Row headingRow = sheet.createRow(rowIdx++);
            List<String> headers = getExcelHeaders(languageResources);
            for (int i = 0; i < headers.size(); i++) {
                setCellValue(headingRow, i, headers.get(i), boldStyle);
            }

            for (List<String> rowData : getExcelRows(cases, languageResources)) {
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
        return List.of(
            languageResources.get("courtHouse").toString(),
            languageResources.get("lja").toString(),
            languageResources.get("courtRoom").toString(),
            languageResources.get("sittingAt").toString(),
            languageResources.get("name").toString(),
            languageResources.get("applicationParticulars").toString(),
            languageResources.get("dob").toString(),
            languageResources.get("age").toString(),
            languageResources.get("address").toString(),
            languageResources.get("prosecutingAuthority").toString(),
            languageResources.get("attendanceMethod").toString(),
            languageResources.get("reference").toString(),
            languageResources.get("applicationType").toString(),
            languageResources.get("asn").toString(),
            languageResources.get("hearingType").toString(),
            languageResources.get("panel").toString(),
            languageResources.get("reportingRestrictions").toString(),
            languageResources.get("offenceCode").toString(),
            languageResources.get("offenceTitle").toString(),
            languageResources.get("offenceDetails").toString(),
            languageResources.get("legislation").toString(),
            languageResources.get("maxPenalty").toString(),
            languageResources.get("plea").toString(),
            languageResources.get("dateOfPlea").toString(),
            languageResources.get("convictedOn").toString(),
            languageResources.get("adjournedFrom").toString()
        );
    }

    private List<List<String>> getExcelRows(List<MagistratesStandardList> cases,
                                             Map<String, Object> languageResources) {
        List<List<String>> rows = new ArrayList<>();

        cases.forEach(hearing -> hearing.getOffences().forEach(offence -> {
            List<String> row = new ArrayList<>();
            row.add(getFieldValue(hearing.getCourtHouseName()));
            row.add(getFieldValue(hearing.getLja()));
            row.add(getFieldValue(hearing.getCourtRoomName()));
            row.add(getFieldValue(hearing.getSittingHeading()));
            row.add(getFieldValue(hearing.getName()));
            row.add(getFieldValue(hearing.getApplicationParticulars()));
            row.add(getFieldValue(hearing.getDob()));
            row.add(getFieldValue(hearing.getAge()));
            row.add(getFieldValue(hearing.getAddress()));
            row.add(getFieldValue(hearing.getProsecutingAuthority()));
            row.add(getFieldValue(hearing.getAttendanceMethod()));
            row.add(getFieldValue(hearing.getReference()));
            row.add(getFieldValue(hearing.getApplicationType()));
            row.add(getFieldValue(hearing.getAsn()));
            row.add(getFieldValue(hearing.getHearingType()));
            row.add(getFieldValue(hearing.getPanel()));
            row.add(getFieldValue(hearing.getReportingRestrictionDetails()));
            row.add(getFieldValue(offence.getOffenceCode()));
            row.add(getFieldValue(offence.getOffenceTitle()));
            row.add(getFieldValue(offence.getOffenceWording()));
            row.add(getFieldValue(offence.getOffenceLegislation()));
            row.add(getFieldValue(offence.getOffenceMaxPenalty()));
            row.add(getFieldValue(offence.getPlea()));
            row.add(getFieldValue(offence.getPleaDate()));
            row.add(getFieldValue(offence.getConvictionDate()));
            row.add(offence.getAdjournedDate() != null
                        ? getFieldValue(offence.getAdjournedDate()) + " - "
                        + languageResources.get("adjournedText")
                        : "");
            rows.add(row);
        }));

        return rows;
    }

    private String getFieldValue(String value) {
        return value == null ? "" : value;
    }

    private void setPublicationDateTime(Context context, String publicationDate, Language language) {
        context.setVariable("publicationDate", DateHelper.formatTimeStampToBst(publicationDate, language,
                                                                               false, false));
        context.setVariable("publicationTime", DateHelper.formatTimeStampToBst(publicationDate, language,
                                                                               true, false));
    }

    private List<MagistratesStandardList> processRawListData(JsonNode json) {
        List<MagistratesStandardList> hearingList = new ArrayList<>();
        Map<String, CourtRoom> processedData = MagistratesStandardListHelper.processRawListData(json);

        processedData.values().forEach(
            courtRoom -> courtRoom.getSittings().forEach(
                sitting -> sitting.getHearings().forEach(
                    hearing -> {
                        MagistratesStandardList item = new MagistratesStandardList();
                        item.setCourtHouseName(courtRoom.getCourtHouseName());
                        item.setLja(courtRoom.getLja());
                        item.setCourtRoomName(courtRoom.getCourtRoomName());
                        item.setSittingHeading(sitting.getSittingHeading());
                        item.setName(hearing.getPartyInfo().getNameDetails());
                        item.setApplicationParticulars(hearing.getHearingMetadata().getApplicationParticulars());
                        item.setDob(hearing.getPartyInfo().getDob());
                        item.setAge(hearing.getPartyInfo().getAge());
                        item.setAddress(hearing.getPartyInfo().getAddress());
                        item.setProsecutingAuthority(hearing.getHearingMetadata().getProsecutingAuthority());
                        item.setAttendanceMethod(hearing.getHearingMetadata().getAttendanceMethod());
                        item.setReference(hearing.getHearingMetadata().getReference());
                        item.setApplicationType(hearing.getHearingMetadata().getApplicationType());
                        item.setAsn(hearing.getPartyInfo().getAsn());
                        item.setHearingType(hearing.getHearingMetadata().getHearingType());
                        item.setPanel(hearing.getHearingMetadata().getPanel());
                        item.setReportingRestrictionDetails(
                            hearing.getHearingMetadata().getReportingRestrictionDetails());
                        item.setOffences(hearing.getOffences());
                        hearingList.add(item);
                    }
                )
            )
        );

        return hearingList;
    }
}
