package uk.gov.hmcts.reform.pip.data.management.service.artefactsummary;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.ImmutableMap;
import org.apache.commons.lang3.StringUtils;
import uk.gov.hmcts.reform.pip.data.management.models.templatemodels.magistratesstandardlist.MatterMetadata;
import uk.gov.hmcts.reform.pip.data.management.models.templatemodels.magistratesstandardlist.Offence;
import uk.gov.hmcts.reform.pip.data.management.service.helpers.listmanipulation.MagistratesStandardListHelper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class MagistratesStandardListSummaryData implements ArtefactSummaryData {
    @Override
    public Map<String, List<Map<String, String>>> get(JsonNode payload) {
        List<Map<String, String>> summaryCases = new ArrayList<>();
        MagistratesStandardListHelper.processRawListData(payload)
            .forEach(
                (courtRoom, list) -> list.getGroupedPartyMatters().forEach(
                    item -> item.getMatters().forEach(sitting -> {
                        String offenceTitles = sitting.getOffences()
                            .stream()
                            .map(Offence::getOffenceTitle)
                            .filter(StringUtils::isNotBlank)
                            .collect(Collectors.joining(", "));
                        MatterMetadata matterMetadata = sitting.getMatterMetadata();

                        Map<String, String> fields = ImmutableMap.of(
                            "Name", sitting.getPartyInfo().getName(),
                            "Prosecuting authority", matterMetadata.getProsecutingAuthority(),
                            "Reference", matterMetadata.getReference(),
                            "Hearing type", matterMetadata.getHearingType(),
                            "Offence", offenceTitles
                        );
                        summaryCases.add(fields);
                    })
                )
            );

        return Collections.singletonMap(null, summaryCases);
    }
}
