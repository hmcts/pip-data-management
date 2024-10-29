package uk.gov.hmcts.reform.pip.data.management.service.artefactsummary;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.ImmutableMap;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.pip.data.management.models.templatemodels.opapubliclist.Offence;
import uk.gov.hmcts.reform.pip.data.management.service.helpers.listmanipulation.OpaPublicListHelper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class OpaPublicListSummaryData implements ArtefactSummaryData {
    @Override
    public Map<String, List<Map<String, String>>> get(JsonNode payload) {
        List<Map<String, String>> summaryCases = new ArrayList<>();
        OpaPublicListHelper.formatOpaPublicList(payload).forEach(item -> {
            String offenceTitles = item.getDefendant().getOffences()
                .stream()
                .map(Offence::getOffenceTitle)
                .filter(StringUtils::isNotBlank)
                .collect(Collectors.joining(", "));

            Map<String, String> fields = ImmutableMap.of(
                "Name", item.getDefendant().getName(),
                "Prosecutor", item.getDefendant().getProsecutor(),
                "Case reference", item.getCaseInfo().getUrn(),
                "Offence", offenceTitles
            );
            summaryCases.add(fields);
        });

        return Collections.singletonMap(null, summaryCases);
    }
}
