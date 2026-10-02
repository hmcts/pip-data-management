package uk.gov.hmcts.reform.pip.data.management.service.artefactsummary;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.ImmutableMap;
import uk.gov.hmcts.reform.pip.data.management.models.templatemodels.crownpddalist.CrownAdvancePddaList;
import uk.gov.hmcts.reform.pip.data.management.service.helpers.listmanipulation.CrownAdvancePddaListHelper;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class CrownAdvancePddaListSummaryData implements ArtefactSummaryData {
    @Override
    public Map<String, List<Map<String, String>>> get(JsonNode payload) {
        Map<String, List<CrownAdvancePddaList>> listData = CrownAdvancePddaListHelper.processPayload(payload);
        List<Map<String, String>> summaryCases = new ArrayList<>();

        listData.values()
            .stream()
            .flatMap(Collection::stream)
            .forEach(row -> {
                Map<String, String> fields = ImmutableMap.of(
                    "Fixed for", row.getFixedDate(),
                    "Case reference", row.getCaseReference(),
                    "Defendant name(s)", row.getDefendantNames(),
                    "Prosecuting authority", row.getProsecutingAuthority()
                );
                summaryCases.add(fields);
            });
        return Collections.singletonMap(null, summaryCases);
    }
}
