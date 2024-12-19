package uk.gov.hmcts.reform.pip.data.management.service.artefactsummary;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.ImmutableMap;
import uk.gov.hmcts.reform.pip.data.management.service.helpers.GeneralHelper;
import uk.gov.hmcts.reform.pip.data.management.service.helpers.listmanipulation.CrownFirmListHelper;
import uk.gov.hmcts.reform.pip.model.publication.Language;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class CrownFirmListSummaryData implements ArtefactSummaryData {
    @Override
    public Map<String, List<Map<String, String>>> get(JsonNode payload) {
        CrownFirmListHelper.crownFirmListFormatted(payload, Language.ENGLISH);
        CrownFirmListHelper.splitByCourtAndDate(payload);
        return processCrownFirmList(payload);
    }

    private Map<String, List<Map<String, String>>> processCrownFirmList(JsonNode node) {
        List<Map<String, String>> summaryCases = new ArrayList<>();
        node.get("courtListsByDate").forEach(
            courtLists -> courtLists.forEach(
                courtList -> courtList.get("courtRooms").forEach(
                    courtRoom -> courtRoom.get("hearings").forEach(
                        hearings -> hearings.forEach(
                            hearing -> hearing.get("case").forEach(hearingCase -> {
                                Map<String, String> fields = ImmutableMap.of(
                                    "Defendant",
                                    GeneralHelper.findAndReturnNodeText(hearingCase, "defendant"),
                                    "Prosecuting authority",
                                    GeneralHelper.findAndReturnNodeText(hearingCase, "prosecutingAuthority"),
                                    "Case reference",
                                    GeneralHelper.findAndReturnNodeText(hearingCase, "caseReference"),
                                    "Hearing type",
                                    GeneralHelper.findAndReturnNodeText(hearingCase, "hearingType")
                                );
                                summaryCases.add(fields);
                            })
                        )
                    )
                )
            )
        );
        return Collections.singletonMap(null, summaryCases);
    }
}
