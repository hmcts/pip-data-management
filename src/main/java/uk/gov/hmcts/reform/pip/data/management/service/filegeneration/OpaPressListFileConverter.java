package uk.gov.hmcts.reform.pip.data.management.service.filegeneration;

import com.fasterxml.jackson.databind.JsonNode;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import uk.gov.hmcts.reform.pip.data.management.service.helpers.DateHelper;
import uk.gov.hmcts.reform.pip.data.management.service.helpers.LocationHelper;
import uk.gov.hmcts.reform.pip.data.management.service.helpers.listmanipulation.OpaPressListHelper;
import uk.gov.hmcts.reform.pip.model.publication.Language;
import uk.gov.hmcts.reform.pip.model.publication.ListType;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static uk.gov.hmcts.reform.pip.model.publication.ListType.OPA_PRESS_LIST;

@Service
public class OpaPressListFileConverter extends ExcelAbstractList implements FileConverter {

    @Override
    public String convert(JsonNode artefact, Map<String, String> metadata, Map<String, Object> languageResources) {
        Context context = new Context();
        setPublicationDateTime(context, artefact.get("document").get("publicationDate").asText(),
                               Language.valueOf(metadata.get("language")));
        context.setVariable("i18n", languageResources);

        context.setVariable("contentDate", metadata.get("contentDate"));
        context.setVariable("provenance", metadata.get("provenance"));
        context.setVariable("version", artefact.get("document").get("version").asText());

        context.setVariable("locationName", metadata.get("locationName"));
        context.setVariable("venueAddress", LocationHelper.formatFullVenueAddress(artefact));
        context.setVariable("cases", OpaPressListHelper.processRawListData(artefact));

        return TemplateEngine.processTemplate(metadata.get("listType"), context);
    }

    private void setPublicationDateTime(Context context, String publicationDate, Language language) {
        context.setVariable("publicationDate", DateHelper.formatTimeStampToBst(publicationDate, language,
                                                                               false, false));
        context.setVariable("publicationTime", DateHelper.formatTimeStampToBst(publicationDate, language,
                                                                               true, false));
    }

    @Override
    public byte[] convertToExcel(JsonNode artefact, ListType listType) throws IOException {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet(OPA_PRESS_LIST.getFriendlyName());
            CellStyle boldStyle = createBoldStyle(workbook);

            Row headingRow = sheet.createRow(0);
            setCellValue(headingRow, 0, "Unique Reference Number (URN)", boldStyle);
            setCellValue(headingRow, 1, "Name", boldStyle);
            setCellValue(headingRow, 2, "Address", boldStyle);
            setCellValue(headingRow, 3, "DOB", boldStyle);
            setCellValue(headingRow, 4, "Prosecution", boldStyle);
            setCellValue(headingRow, 5, "Scheduled first hearing", boldStyle);
            setCellValue(headingRow, 6, "Case Reporting Restriction", boldStyle);

            AtomicInteger rowNumber = new AtomicInteger(1);
            AtomicInteger maxOffences = new AtomicInteger(0);

            OpaPressListHelper.processRawListData(artefact).forEach((pleaData, list) ->
                list.forEach(item -> {
                    Row dataRow = sheet.createRow(rowNumber.get());
                    setCellValue(dataRow, 0, item.getCaseInfo().getUrn());
                    setCellValue(dataRow, 1, item.getDefendantInfo().getName());
                    setCellValue(dataRow, 2, item.getDefendantInfo().getAddress());
                    setCellValue(dataRow, 3, item.getDefendantInfo().getDob());
                    setCellValue(dataRow, 4, item.getDefendantInfo().getProsecutor());
                    setCellValue(dataRow, 5, item.getCaseInfo().getScheduledHearingDate());
                    setCellValue(dataRow, 6, item.getCaseInfo().getCaseReportingRestriction());

                    AtomicInteger columnNumber = new AtomicInteger(7);
                    maxOffences.set(Math.max(maxOffences.get(), item.getDefendantInfo().getOffences().size()));
                    item.getDefendantInfo().getOffences().forEach(offence -> {
                        setCellValue(dataRow, columnNumber.getAndIncrement(), offence.getOffenceTitle());
                        setCellValue(dataRow, columnNumber.getAndIncrement(), offence.getOffenceSection());
                        setCellValue(dataRow, columnNumber.getAndIncrement(), offence.getOffenceReportingRestriction());
                        setCellValue(dataRow, columnNumber.getAndIncrement(), offence.getPlea());
                        setCellValue(dataRow, columnNumber.getAndIncrement(), offence.getPleaDate());
                        setCellValue(dataRow, columnNumber.getAndIncrement(), offence.getOffenceWording());
                    });

                    rowNumber.getAndIncrement();
                })
            );

            //Correct number of offence headings is added at the end, once the entire data set is processed
            int columnNumber = 7;
            for (int i = 1; i <= maxOffences.get(); i++) {
                setCellValue(headingRow, columnNumber++, String.format("Offence[%s] - Title", i), boldStyle);
                setCellValue(headingRow, columnNumber++, String.format("Offence[%s] - Section", i), boldStyle);
                setCellValue(headingRow, columnNumber++,
                             String.format("Offence[%s] - Reporting Restriction", i), boldStyle);
                setCellValue(headingRow, columnNumber++, String.format("Offence[%s] - Plea", i), boldStyle);
                setCellValue(headingRow, columnNumber++, String.format("Offence[%s] - Plea date", i), boldStyle);
                setCellValue(headingRow, columnNumber++, String.format("Offence[%s] - Detail", i), boldStyle);
            }

            return convertToByteArray(workbook);
        }
    }

}
