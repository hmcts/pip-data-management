package uk.gov.hmcts.reform.pip.data.management.service.csvprocessing;

import com.fasterxml.jackson.databind.JsonNode;
import uk.gov.hmcts.reform.pip.data.management.models.templatemodels.crownpddalist.CrownPddaList;
import uk.gov.hmcts.reform.pip.data.management.models.templatemodels.crownpddalist.SittingInfo;
import uk.gov.hmcts.reform.pip.data.management.service.helpers.listmanipulation.CrownPddaListHelper;
import uk.gov.hmcts.reform.pip.model.publication.ListType;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CrownDailyPddaListCsvData implements CsvData {
    @Override
    public List<String> getHeaders(Map<String, Object> languageResources) {
        @SuppressWarnings("unchecked")
        List<String> tableHeaders = (List<String>) languageResources.get("tableHeaders");

        return List.of(
            languageResources.get("courtHouse").toString(),
            languageResources.get("courtAddress").toString(),
            languageResources.get("courtPhone").toString(),
            languageResources.get("courtRoom").toString(),
            languageResources.get("sittingAt").toString(),
            tableHeaders.get(0),
            tableHeaders.get(1),
            tableHeaders.get(2),
            tableHeaders.get(3),
            tableHeaders.get(4),
            tableHeaders.get(5)
        );
    }

    @Override
    public List<List<String>> getRows(JsonNode json, Map<String, String> metadata,
                                      Map<String, Object> languageResources) {
        List<List<String>> rows = new ArrayList<>();
        List<CrownPddaList> processedData = CrownPddaListHelper.processPayload(json, ListType.CROWN_DAILY_PDDA_LIST);

        processedData.forEach(
            data -> data.getSittings().forEach(sitting -> {
                String courtRoomInfo = constructCourtRoomInfo(sitting, languageResources);
                sitting.getHearings().forEach(
                    hearing -> rows.add(List.of(
                        data.getCourtName(),
                        String.join(", ", data.getCourtAddress()),
                        data.getCourtPhone(),
                        courtRoomInfo,
                        sitting.getSittingAt(),
                        hearing.getHearingTime(),
                        hearing.getCaseNumber(),
                        hearing.getDefendantName(),
                        hearing.getHearingType(),
                        hearing.getProsecutingAuthority(),
                        hearing.getListNote()
                    )));
            }));

        return rows;
    }

    private String constructCourtRoomInfo(SittingInfo sitting, Map<String, Object> languageResources) {
        String courtRoom = languageResources.get("court").toString() + sitting.getCourtRoomNumber();
        return sitting.getJudgeName().isEmpty()
            ? courtRoom
            : courtRoom + ": " + sitting.getJudgeName();
    }
}
