package uk.gov.hmcts.reform.pip.data.management.service.artefactsummary;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.ImmutableMap;
import lombok.AllArgsConstructor;
import uk.gov.hmcts.reform.pip.data.management.service.helpers.listmanipulation.MagistratesAdultCourtListHelper;
import uk.gov.hmcts.reform.pip.model.publication.Language;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@AllArgsConstructor
public class MagistratesAdultCourtListSummaryData implements ArtefactSummaryData {

    private boolean isStandardList;

    @Override
    public Map<String, List<Map<String, String>>> get(JsonNode payload) {
        return Collections.singletonMap(null, isStandardList
            ? standardListSummary(payload) : publicListSummary(payload));
    }

    private List<Map<String, String>> publicListSummary(JsonNode payload) {
        List<Map<String, String>> summaryCases = new ArrayList<>();
        MagistratesAdultCourtListHelper.processPayload(payload, Language.ENGLISH, false).forEach(
            item -> item.getCases().forEach(caseInfo -> {
                Map<String, String> fields = ImmutableMap.of(
                    "Defendant name", caseInfo.getDefendantName(),
                    "Case number", caseInfo.getCaseNumber()
                );
                summaryCases.add(fields);
            })
        );
        return summaryCases;
    }

    private List<Map<String, String>> standardListSummary(JsonNode payload) {
        List<Map<String, String>> summaryCases = new ArrayList<>();
        MagistratesAdultCourtListHelper.processPayload(payload, Language.ENGLISH, true).forEach(
            item -> item.getCases().forEach(caseInfo -> {
                Map<String, String> fields = ImmutableMap.of(
                    "Defendant name", caseInfo.getDefendantName(),
                    "Informant", caseInfo.getInformant(),
                    "Case number", caseInfo.getCaseNumber(),
                    "Offence title", caseInfo.getOffence().getOffenceTitle()
                );
                summaryCases.add(fields);
            })
        );
        return summaryCases;
    }

}
