package uk.gov.hmcts.reform.pip.data.management.service.artefactsummary;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.ImmutableMap;
import lombok.AllArgsConstructor;
import uk.gov.hmcts.reform.pip.data.management.service.helpers.listmanipulation.CrownPddaListHelper;
import uk.gov.hmcts.reform.pip.model.publication.ListType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@AllArgsConstructor
public class CrownPddaListSummaryData implements ArtefactSummaryData {
    private ListType listType;

    @Override
    public Map<String, List<Map<String, String>>> get(JsonNode payload) {
        CrownPddaListHelper.processPayload(payload, listType);

        List<Map<String, String>> summaryCases = new ArrayList<>();
        CrownPddaListHelper.processPayload(payload, listType).forEach(
            item -> item.getSittings().forEach(
                sitting -> sitting.getHearings().forEach(hearing -> {
                    Map<String, String> map = ImmutableMap.of(
                        "Defendant name(s)", hearing.getDefendantName(),
                        "Prosecuting authority", hearing.getProsecutingAuthority(),
                        "Case reference", hearing.getCaseNumber(),
                        "Hearing type", hearing.getHearingType()
                    );
                    summaryCases.add(map);
                })
            )
        );
        return Collections.singletonMap(null, summaryCases);
    }
}
