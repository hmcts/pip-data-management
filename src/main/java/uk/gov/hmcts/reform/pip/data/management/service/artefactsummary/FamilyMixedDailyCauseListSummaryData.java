package uk.gov.hmcts.reform.pip.data.management.service.artefactsummary;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.ImmutableMap;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.pip.data.management.service.helpers.GeneralHelper;
import uk.gov.hmcts.reform.pip.data.management.service.helpers.listmanipulation.FamilyMixedListHelper;
import uk.gov.hmcts.reform.pip.model.publication.Language;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Service
public class FamilyMixedDailyCauseListSummaryData implements ArtefactSummaryData {
    @Override
    public Map<String, List<Map<String, String>>> get(JsonNode payload) {
        FamilyMixedListHelper.manipulatedListData(payload, Language.ENGLISH);
        List<Map<String, String>> summaryCases = new ArrayList<>();
        payload.get("courtLists").forEach(
            courtList -> courtList.get("courtHouse").get("courtRoom").forEach(
                courtRoom -> courtRoom.get("session").forEach(
                    session -> session.get("sittings").forEach(
                        sitting -> sitting.get("hearing").forEach(
                            hearing -> hearing.get("case").forEach(
                                hearingCase -> {
                                    String applicant = GeneralHelper.findAndReturnNodeText(hearingCase, "applicant");
                                    Map<String, String> fields = ImmutableMap.of(
                                        "Applicant", applicant,
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
