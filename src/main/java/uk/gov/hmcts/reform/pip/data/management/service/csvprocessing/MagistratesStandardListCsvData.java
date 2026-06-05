package uk.gov.hmcts.reform.pip.data.management.service.csvprocessing;

import com.fasterxml.jackson.databind.JsonNode;
import uk.gov.hmcts.reform.pip.data.management.models.templatemodels.magistratesstandardlist.CourtRoom;
import uk.gov.hmcts.reform.pip.data.management.service.helpers.listmanipulation.MagistratesStandardListHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MagistratesStandardListCsvData implements CsvData {
    @Override
    public List<String> getHeaders(Map<String, Object> languageResources) {
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

    @Override
    public List<List<String>> getRows(JsonNode json, Map<String, String> metadata,
                                      Map<String, Object> languageResources) {
        List<List<String>> rows = new ArrayList<>();
        Map<String, CourtRoom> processedData = MagistratesStandardListHelper.processRawListData(json);

        processedData.values().forEach(
                courtRoom -> courtRoom.getSittings().forEach(
                        sitting -> sitting.getHearings().forEach(
                                hearing -> hearing.getOffences().forEach(
                                        offence ->
                                            rows.add(List.of(
                                                getFieldValue(courtRoom.getCourtHouseName()),
                                                getFieldValue(courtRoom.getLja()),
                                                getFieldValue(courtRoom.getCourtRoomName()),
                                                getFieldValue(sitting.getSittingHeading()),
                                                getFieldValue(hearing.getPartyInfo().getNameDetails()),
                                                getFieldValue(hearing.getHearingMetadata().getApplicationParticulars()),
                                                getFieldValue(hearing.getPartyInfo().getDob()),
                                                getFieldValue(hearing.getPartyInfo().getAge()),
                                                getFieldValue(hearing.getPartyInfo().getAddress()),
                                                getFieldValue(hearing.getHearingMetadata().getProsecutingAuthority()),
                                                getFieldValue(hearing.getHearingMetadata().getAttendanceMethod()),
                                                getFieldValue(hearing.getHearingMetadata().getReference()),
                                                getFieldValue(hearing.getHearingMetadata().getApplicationType()),
                                                getFieldValue(hearing.getPartyInfo().getAsn()),
                                                getFieldValue(hearing.getHearingMetadata().getHearingType()),
                                                getFieldValue(hearing.getHearingMetadata().getPanel()),
                                                getFieldValue(hearing.getHearingMetadata()
                                                                  .getReportingRestrictionDetails()),
                                                getFieldValue(offence.getOffenceCode()),
                                                getFieldValue(offence.getOffenceTitle()),
                                                getFieldValue(offence.getOffenceWording()),
                                                getFieldValue(offence.getOffenceLegislation()),
                                                getFieldValue(offence.getOffenceMaxPenalty()),
                                                getFieldValue(offence.getPlea()),
                                                getFieldValue(offence.getPleaDate()),
                                                getFieldValue(offence.getConvictionDate()),
                                                offence.getAdjournedDate() != null
                                                    ? getFieldValue(offence.getAdjournedDate()) + " - "
                                                    + languageResources.get("adjournedText")
                                                    : ""
                                            ))
                                )
                        )
                )
        );


        return rows;
    }

    private String getFieldValue(String value) {
        return value == null ? "" : value;
    }
}
