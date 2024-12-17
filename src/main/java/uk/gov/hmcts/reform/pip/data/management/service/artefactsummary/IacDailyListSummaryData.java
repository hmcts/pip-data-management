package uk.gov.hmcts.reform.pip.data.management.service.artefactsummary;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.ImmutableMap;
import uk.gov.hmcts.reform.pip.data.management.service.helpers.GeneralHelper;
import uk.gov.hmcts.reform.pip.data.management.service.helpers.PartyRoleHelper;
import uk.gov.hmcts.reform.pip.data.management.service.helpers.SittingHelper;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Summary class for the IAC Daily List that generates the summary in the email.
 */
public class IacDailyListSummaryData implements ArtefactSummaryData {
    @Override
    @SuppressWarnings("PMD.UseConcurrentHashMap")
    public Map<String, List<Map<String, String>>> get(JsonNode payload) {
        Map<String, List<Map<String, String>>> summaryData = new LinkedHashMap<>();
        payload.get("courtLists").forEach(courtList -> {
            String courtListName = GeneralHelper.findAndReturnNodeText(courtList, "courtListName");
            courtList.get("courtHouse").get("courtRoom").forEach(
                courtRoom -> courtRoom.get("session").forEach(
                    session -> session.get("sittings").forEach(sitting -> {
                        SittingHelper.findAndConcatenateHearingPlatform(sitting, session);
                        sitting.get("hearing").forEach(
                            hearing -> hearing.get("case").forEach(hearingCase -> {
                                PartyRoleHelper.findAndManipulatePartyInformation(hearingCase, false);

                                Map<String, String> fields = ImmutableMap.of(
                                    "Appellant/Applicant",
                                    GeneralHelper.findAndReturnNodeText(hearingCase, "claimant"),
                                    "Prosecuting authority",
                                    GeneralHelper.findAndReturnNodeText(hearingCase, "prosecutingAuthority"),
                                    "Case reference",
                                    GeneralHelper.findAndReturnNodeText(hearingCase, "caseNumber")
                                );

                                summaryData.computeIfAbsent(courtListName, x -> new ArrayList<>())
                                    .add(fields);
                            })
                        );
                    })
                )
            );
        });
        return summaryData;
    }
}
