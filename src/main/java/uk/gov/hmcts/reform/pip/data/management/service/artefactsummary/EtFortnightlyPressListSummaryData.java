package uk.gov.hmcts.reform.pip.data.management.service.artefactsummary;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.ImmutableMap;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.pip.data.management.service.helpers.GeneralHelper;
import uk.gov.hmcts.reform.pip.data.management.service.helpers.listmanipulation.EtFortnightlyPressListHelper;
import uk.gov.hmcts.reform.pip.model.publication.Language;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Service
public class EtFortnightlyPressListSummaryData implements ArtefactSummaryData {
    @Override
    public Map<String, List<Map<String, String>>> get(JsonNode payload) {
        Map<String, Object> language =
            Map.of("rep", "Rep: ",
                   "noRep", "Rep: ");
        EtFortnightlyPressListHelper.manipulatedListData(payload, Language.ENGLISH, true);
        EtFortnightlyPressListHelper.etFortnightlyListFormatted(payload, language);
        EtFortnightlyPressListHelper.splitByCourtAndDate(payload);
        List<Map<String, String>> summaryCases = new ArrayList<>();

        payload.get("courtLists").forEach(
            courtList -> courtList.get("sittings").forEach(
                sitting -> sitting.get("hearing").forEach(
                    hearings -> hearings.forEach(
                        hearing -> hearing.get("case").forEach(hearingCase -> {
                            Map<String, String> fields = ImmutableMap.of(
                                "Claimant",
                                GeneralHelper.findAndReturnNodeText(hearingCase, "applicant"),
                                "Respondent",
                                GeneralHelper.findAndReturnNodeText(hearingCase, "respondent"),
                                "Case reference",
                                GeneralHelper.findAndReturnNodeText(hearingCase, "caseNumber"),
                                "Hearing type",
                                GeneralHelper.findAndReturnNodeText(hearing, "hearingType")
                            );
                            summaryCases.add(fields);
                        })
                    )
                )
            )
        );
        return Collections.singletonMap(null, summaryCases);
    }
}
