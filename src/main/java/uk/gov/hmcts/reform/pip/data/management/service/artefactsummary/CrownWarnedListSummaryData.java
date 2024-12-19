package uk.gov.hmcts.reform.pip.data.management.service.artefactsummary;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.ImmutableMap;
import uk.gov.hmcts.reform.pip.data.management.models.templatemodels.CrownWarnedList;
import uk.gov.hmcts.reform.pip.data.management.service.helpers.listmanipulation.CrownWarnedListHelper;
import uk.gov.hmcts.reform.pip.model.publication.Language;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class CrownWarnedListSummaryData implements ArtefactSummaryData {
    @Override
    public Map<String, List<Map<String, String>>> get(JsonNode payload) {
        Map<String, List<CrownWarnedList>> cases = CrownWarnedListHelper.processRawListData(payload, Language.ENGLISH);
        List<Map<String, String>> summaryCases = new ArrayList<>();

        cases.values()
            .stream()
            .flatMap(Collection::stream)
            .forEach(row -> {
                Map<String, String> fields = ImmutableMap.of(
                    "Defendant", row.getDefendant(),
                    "Prosecuting authority", row.getProsecutingAuthority(),
                    "Case reference", row.getCaseReference(),
                    "Hearing date", row.getHearingDate()
                );
                summaryCases.add(fields);
            });
        return Collections.singletonMap(null, summaryCases);
    }
}
