package uk.gov.hmcts.reform.pip.data.management.service.artefactsummary;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.ImmutableMap;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.pip.data.management.service.helpers.listmanipulation.SjpPublicListHelper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Service
public class SjpPublicListSummaryData implements ArtefactSummaryData {
    private static final String HEARING = "hearing";
    private static final String COURT_LISTS = "courtLists";
    private static final String COURT_HOUSE = "courtHouse";
    private static final String COURT_ROOM = "courtRoom";
    private static final String SESSION = "session";
    private static final String SITTINGS = "sittings";

    @Override
    public Map<String, List<Map<String, String>>> get(JsonNode payload) {
        List<Map<String, String>> summaryCases = new ArrayList<>();
        payload.get(COURT_LISTS).forEach(courtList ->
            courtList.get(COURT_HOUSE).get(COURT_ROOM).forEach(
                courtRoom -> courtRoom.get(SESSION).forEach(
                    session -> session.get(SITTINGS).forEach(
                        sitting -> sitting.get(HEARING).forEach(
                            hearing -> SjpPublicListHelper.constructSjpCase(hearing).ifPresent(sjpCase -> {
                                Map<String, String> fields = ImmutableMap.of(
                                    "Name", sjpCase.getName(),
                                    "Prosecutor", sjpCase.getProsecutor(),
                                    "Postcode", sjpCase.getPostcode(),
                                    "Offence", sjpCase.getOffence()
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
