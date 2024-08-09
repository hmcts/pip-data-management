package uk.gov.hmcts.reform.pip.data.management.service.artefactsummary;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.ImmutableMap;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.pip.data.management.models.templatemodels.opapresslist.Offence;
import uk.gov.hmcts.reform.pip.data.management.service.helpers.listmanipulation.OpaPressListHelper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class OpaPressListSummaryData implements ArtefactSummaryData {
    @Override
    public Map<String, List<Map<String, String>>> get(JsonNode payload) {
        List<Map<String, String>> summaryCases = new ArrayList<>();
        OpaPressListHelper.processRawListData(payload).forEach(
            (pleaData, list) -> list.forEach(item -> {
                String offenceTitles = item.getDefendantInfo().getOffences()
                    .stream()
                    .map(Offence::getOffenceTitle)
                    .filter(StringUtils::isNotBlank)
                    .collect(Collectors.joining(", "));

                Map<String, String> fields = ImmutableMap.of(
                    "Defendant", item.getDefendantInfo().getName(),
                    "Prosecutor", item.getDefendantInfo().getProsecutor(),
                    "Postcode", item.getDefendantInfo().getPostcode(),
                    "Case reference", item.getCaseInfo().getUrn(),
                    "Offence", offenceTitles
                );
                summaryCases.add(fields);
            })
        );

        return Collections.singletonMap(null, summaryCases);
    }
}
