package uk.gov.hmcts.reform.pip.data.management.service.csvprocessing;

import com.fasterxml.jackson.databind.JsonNode;
import uk.gov.hmcts.reform.pip.data.management.models.templatemodels.magistratesadultcourtlist.MagistratesAdultCourtList;
import uk.gov.hmcts.reform.pip.data.management.service.helpers.listmanipulation.MagistratesAdultCourtListHelper;
import uk.gov.hmcts.reform.pip.model.publication.Language;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MagistratesPublicAdultCourtListCsvData implements CsvData {
    @Override
    public List<String> getHeaders(Map<String, Object> languageResources) {
        @SuppressWarnings("unchecked")
        List<String> tableHeaders = (List<String>) languageResources.get("tableHeaders");

        return List.of(
            languageResources.get("courtHouse").toString(),
            languageResources.get("sittingAt").toString(),
            languageResources.get("lja").toString(),
            languageResources.get("sessionStart").toString(),
            tableHeaders.get(0),
            tableHeaders.get(1),
            tableHeaders.get(2)
        );
    }

    @Override
    public List<List<String>> getRows(JsonNode json, Map<String, String> metadata,
                                      Map<String, Object> languageResources) {
        List<List<String>> rows = new ArrayList<>();
        List<MagistratesAdultCourtList> processedData = MagistratesAdultCourtListHelper.processPayload(
            json, Language.valueOf(metadata.get("language")), false
        );

        processedData.forEach(
            data -> data.getCases().forEach(
                hearingCase -> rows.add(List.of(
                    data.getCourtName(),
                    languageResources.get("courtRoom").toString() + data.getCourtRoom(),
                    data.getLja(),
                    data.getSessionStartTime(),
                    hearingCase.getBlockStartTime(),
                    hearingCase.getDefendantName(),
                    hearingCase.getCaseNumber()
                ))));

        return rows;
    }
}
