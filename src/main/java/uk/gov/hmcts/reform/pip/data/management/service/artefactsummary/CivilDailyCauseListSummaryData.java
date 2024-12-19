package uk.gov.hmcts.reform.pip.data.management.service.artefactsummary;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.ImmutableMap;
import uk.gov.hmcts.reform.pip.data.management.service.helpers.GeneralHelper;
import uk.gov.hmcts.reform.pip.data.management.service.helpers.listmanipulation.CftListHelper;
import uk.gov.hmcts.reform.pip.model.publication.Language;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class CivilDailyCauseListSummaryData implements ArtefactSummaryData {
    @Override
    public Map<String, List<Map<String, String>>> get(JsonNode payload) {
        CftListHelper.manipulatedListData(payload, Language.ENGLISH, false);
        List<Map<String, String>> summaryCases = new ArrayList<>();

        payload.get("courtLists").forEach(
            courtList -> courtList.get("courtHouse").get("courtRoom").forEach(
                courtRoom -> courtRoom.get("session").forEach(
                    session -> session.get("sittings").forEach(
                        sitting -> sitting.get("hearing").forEach(
                            hearing -> hearing.get("case").forEach(
                                hearingCase -> {
                                    Map<String, String> fields = ImmutableMap.of(
                                        "Case reference",
                                        GeneralHelper.findAndReturnNodeText(hearingCase, "caseNumber"),
                                        "Case name",
                                        GeneralHelper.findAndReturnNodeText(hearingCase, "caseName"),
                                        "Case type",
                                        GeneralHelper.findAndReturnNodeText(hearingCase, "caseType"),
                                        "Hearing type",
                                        GeneralHelper.findAndReturnNodeText(hearing, "hearingType")
                                    );
                                    summaryCases.add(fields);
                                })
                        )
                    )
                )
            )
        );
        return Collections.singletonMap(null, summaryCases);
    }
}
