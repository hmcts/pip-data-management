package uk.gov.hmcts.reform.pip.data.management.service.csvprocessing;

import com.fasterxml.jackson.databind.JsonNode;
import uk.gov.hmcts.reform.pip.data.management.models.templatemodels.crownpddalist.CrownWarnedPddaList;
import uk.gov.hmcts.reform.pip.data.management.service.helpers.listmanipulation.CrownWarnedPddaListHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CrownWarnedPddaListCsvData implements CsvData {
    @Override
    public List<String> getHeaders(Map<String, Object> languageResources) {
        @SuppressWarnings("unchecked")
        List<String> tableHeaders = (List<String>) languageResources.get("tableHeaders");

        return List.of(
            languageResources.get("hearingDescription").toString(),
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
        Map<String, List<CrownWarnedPddaList>> processedData = CrownWarnedPddaListHelper.processPayload(json);

        processedData.forEach(
            (hearingDescription, cases) -> cases.forEach(
                hearingCase -> rows.add(List.of(
                    getHearingDescription(hearingDescription, languageResources),
                    hearingCase.getFixedDate(),
                    hearingCase.getCaseReference(),
                    hearingCase.getDefendantNames(),
                    hearingCase.getProsecutingAuthority(),
                    hearingCase.getLinkedCases(),
                    hearingCase.getListingNotes()
                ))
            ));
        return rows;
    }

    private String getHearingDescription(String hearingDescription, Map<String, Object> languageResources) {
        return "To be allocated".equalsIgnoreCase(hearingDescription)
            ? languageResources.get("toBeAllocatedText").toString()
            : hearingDescription;

    }
}
