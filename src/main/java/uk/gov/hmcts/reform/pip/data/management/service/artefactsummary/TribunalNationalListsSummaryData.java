package uk.gov.hmcts.reform.pip.data.management.service.artefactsummary;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.ImmutableMap;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.pip.data.management.service.helpers.LocationHelper;
import uk.gov.hmcts.reform.pip.data.management.service.helpers.listmanipulation.TribunalNationalListHelper;
import uk.gov.hmcts.reform.pip.model.publication.Language;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Service
public class TribunalNationalListsSummaryData implements ArtefactSummaryData {
    @Override
    public Map<String, List<Map<String, String>>> get(JsonNode payload) {
        List<Map<String, String>> summaryCases = new ArrayList<>();
        LocationHelper.formatCourtAddress(payload, ", ", true);

        TribunalNationalListHelper.processRawListData(payload, Language.ENGLISH)
            .forEach(data -> {
                Map<String, String> fields = ImmutableMap.of(
                    "Case name", data.getCaseName(),
                    "Hearing date", data.getHearingDate(),
                    "Hearing type", data.getHearingType()
                );
                summaryCases.add(fields);
            });

        return Collections.singletonMap(null, summaryCases);
    }
}
