package uk.gov.hmcts.reform.pip.data.management.service.artefactsummary;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.ImmutableMap;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.pip.data.management.models.templatemodels.magistratesstandardlist.CaseInfo;
import uk.gov.hmcts.reform.pip.data.management.models.templatemodels.magistratesstandardlist.Offence;
import uk.gov.hmcts.reform.pip.data.management.service.helpers.listmanipulation.MagistratesStandardListHelper;
import uk.gov.hmcts.reform.pip.model.publication.Language;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class MagistratesStandardListSummaryData implements ArtefactSummaryData {
    @Override
    public Map<String, List<Map<String, String>>> get(JsonNode payload) {
        List<Map<String, String>> summaryCases = new ArrayList<>();
        MagistratesStandardListHelper.processRawListData(payload, Language.ENGLISH)
            .forEach(
                (courtRoom, list) -> list.forEach(
                    item -> item.getCaseSittings().forEach(sitting -> {
                        String offenceTitles = sitting.getOffences()
                            .stream()
                            .map(Offence::getOffenceTitle)
                            .filter(StringUtils::isNotBlank)
                            .collect(Collectors.joining(", "));
                        CaseInfo caseInfo = sitting.getCaseInfo();

                        Map<String, String> fields = ImmutableMap.of(
                            "Defendant", sitting.getDefendantInfo().getName(),
                            "Prosecuting authority", caseInfo.getProsecutingAuthorityCode(),
                            "Case reference", caseInfo.getCaseNumber(),
                            "Hearing type", caseInfo.getHearingType(),
                            "Offence", offenceTitles
                        );
                        summaryCases.add(fields);
                    })
                )
            );

        return Collections.singletonMap(null, summaryCases);
    }
}
