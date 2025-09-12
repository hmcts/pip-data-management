package uk.gov.hmcts.reform.pip.data.management.service.artefactsummary;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.ImmutableMap;
import uk.gov.hmcts.reform.pip.data.management.service.helpers.listmanipulation.CrownFirmPddaListHelper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class CrownFirmPddaListSummaryData implements ArtefactSummaryData {
    @Override
    public Map<String, List<Map<String, String>>> get(JsonNode payload) {
        CrownFirmPddaListHelper.processPayload(payload);

        List<Map<String, String>> summaryCases = new ArrayList<>();
        CrownFirmPddaListHelper.processPayload(payload).forEach(
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
