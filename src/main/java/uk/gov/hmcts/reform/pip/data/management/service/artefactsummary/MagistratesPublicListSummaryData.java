package uk.gov.hmcts.reform.pip.data.management.service.artefactsummary;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.ImmutableMap;
import uk.gov.hmcts.reform.pip.data.management.service.helpers.GeneralHelper;
import uk.gov.hmcts.reform.pip.data.management.service.helpers.listmanipulation.MagistratesPublicListHelper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class MagistratesPublicListSummaryData implements ArtefactSummaryData {
    @Override
    public Map<String, List<Map<String, String>>> get(JsonNode payload) {
        MagistratesPublicListHelper.manipulatedMagistratesPublicListData(payload);
        return processMagistratesPublicList(payload);
    }

    public Map<String, List<Map<String, String>>> processMagistratesPublicList(JsonNode node) {
        List<Map<String, String>> summaryCases = new ArrayList<>();
        node.get("courtLists").forEach(
            courtList -> courtList.get("courtHouse").get("courtRoom").forEach(
                courtRoom -> courtRoom.get("session").forEach(
                    session -> session.get("sittings").forEach(
                        sitting -> sitting.get("hearing").forEach(hearing -> {
                            if (hearing.has("case")) {
                                hearing.get("case").forEach(hearingCase -> {
                                    Map<String, String> fields = ImmutableMap.of(
                                        "Name",
                                        GeneralHelper.findAndReturnNodeText(hearingCase, "defendant"),
                                        "Prosecuting authority",
                                        GeneralHelper.findAndReturnNodeText(hearingCase, "prosecutingAuthority"),
                                        "URN",
                                        GeneralHelper.findAndReturnNodeText(hearingCase, "caseUrn"),
                                        "Hearing type",
                                        GeneralHelper.findAndReturnNodeText(hearing, "hearingType")
                                    );
                                    summaryCases.add(fields);
                                });
                            }
                            if (hearing.has("application")) {
                                hearing.get("application").forEach(application -> {
                                    Map<String, String> fields = ImmutableMap.of(
                                        "Name",
                                        GeneralHelper.findAndReturnNodeText(application, "defendant"),
                                        "Prosecuting authority",
                                        GeneralHelper.findAndReturnNodeText(application, "prosecutingAuthority"),
                                        "URN",
                                        GeneralHelper.findAndReturnNodeText(application, "applicationReference")
                                    );
                                    summaryCases.add(fields);
                                });
                            }
                        })
                    )
                )
            )
        );
        return Collections.singletonMap(null, summaryCases);
    }
}

