package uk.gov.hmcts.reform.pip.data.management.service.artefactsummary;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.ImmutableMap;
import uk.gov.hmcts.reform.pip.data.management.models.templatemodels.sscsdailylist.CourtHouse;
import uk.gov.hmcts.reform.pip.data.management.models.templatemodels.sscsdailylist.CourtRoom;
import uk.gov.hmcts.reform.pip.data.management.models.templatemodels.sscsdailylist.Hearing;
import uk.gov.hmcts.reform.pip.data.management.models.templatemodels.sscsdailylist.HearingCase;
import uk.gov.hmcts.reform.pip.data.management.models.templatemodels.sscsdailylist.Sitting;
import uk.gov.hmcts.reform.pip.data.management.service.helpers.listmanipulation.SscsListHelper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class SscsDailyListSummaryData implements ArtefactSummaryData {
    @Override
    public Map<String, List<Map<String, String>>> get(JsonNode payload) throws JsonProcessingException {
        List<CourtHouse> courtHouseList = buildCourtHouseList(payload);
        List<Map<String, String>> summaryCases = new ArrayList<>();

        for (CourtHouse courtHouse : courtHouseList) {
            for (CourtRoom courtRoom : courtHouse.getListOfCourtRooms()) {
                for (Sitting sitting : courtRoom.getListOfSittings()) {
                    for (Hearing hearing : sitting.getListOfHearings()) {
                        addFieldsToCases(hearing, summaryCases);
                    }
                }
            }
        }
        return Collections.singletonMap(null, summaryCases);
    }

    private List<CourtHouse> buildCourtHouseList(JsonNode payload) throws JsonProcessingException {
        List<CourtHouse> courtHouseList = new ArrayList<>();
        for (JsonNode courtHouse : payload.get("courtLists")) {
            courtHouseList.add(SscsListHelper.courtHouseBuilder(courtHouse));
        }
        return courtHouseList;
    }

    private void addFieldsToCases(Hearing hearing, List<Map<String, String>> summaryCases) {
        Iterator<HearingCase> caseIterator = hearing.getListOfCases().iterator();
        while (caseIterator.hasNext()) {
            HearingCase hearingCase = caseIterator.next();
            Map<String, String> fields = ImmutableMap.of(
                "Appellant", hearingCase.getAppellant(),
                "Respondent", hearingCase.getRespondent(),
                "Case reference", hearingCase.getAppealRef()
            );
            summaryCases.add(fields);
        }
    }
}
