package uk.gov.hmcts.reform.pip.data.management.service.helpers.listmanipulation;

import com.fasterxml.jackson.databind.JsonNode;
import uk.gov.hmcts.reform.pip.data.management.models.templatemodels.CrownWarnedList;
import uk.gov.hmcts.reform.pip.data.management.service.helpers.CaseHelper;
import uk.gov.hmcts.reform.pip.data.management.service.helpers.DateHelper;
import uk.gov.hmcts.reform.pip.data.management.service.helpers.GeneralHelper;
import uk.gov.hmcts.reform.pip.data.management.service.helpers.PartyRoleHelper;
import uk.gov.hmcts.reform.pip.model.publication.Language;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public final class CrownWarnedListHelper {
    private static final String PROSECUTING_AUTHORITY = "prosecutingAuthority";
    private static final String DEFENDANT = "defendant";
    private static final String DEFENDANT_REPRESENTATIVE = "defendantRepresentative";
    private static final String TO_BE_ALLOCATED = "To be allocated";

    private static final Comparator<Map.Entry<String, List<CrownWarnedList>>> COMPARATOR = (s1, s2) -> {
        if (TO_BE_ALLOCATED.equalsIgnoreCase(s1.getKey())) {
            return 1;
        } else if (TO_BE_ALLOCATED.equalsIgnoreCase(s2.getKey())) {
            return -1;
        }
        return 0;
    };

    private CrownWarnedListHelper() {
    }

    @SuppressWarnings("PMD.UseConcurrentHashMap")
    public static Map<String, List<CrownWarnedList>> processRawListData(JsonNode data, Language language) {
        Map<String, List<CrownWarnedList>> result = new LinkedHashMap<>();
        data.get("courtLists").forEach(
            courtList -> courtList.get("courtHouse").get("courtRoom").forEach(
                courtRoom -> courtRoom.get("session").forEach(
                    session -> session.get("sittings").forEach(sitting -> {
                        String hearingDate = DateHelper.formatTimeStampToBst(sitting.get("sittingStart").asText(),
                                                                             language, false, false,
                                                                             "dd/MM/yyyy");
                        sitting.get("hearing").forEach(hearing -> {
                            String listNote = GeneralHelper.findAndReturnNodeText(hearing, "listNote");
                            List<CrownWarnedList> rows = new ArrayList<>();
                            hearing.get("case").forEach(hearingCase -> {
                                PartyRoleHelper.handleParties(hearingCase);
                                CaseHelper.formatLinkedCases(hearingCase);
                                rows.add(new CrownWarnedList(
                                    hearingCase.get("caseNumber").asText(),
                                    hearingCase.get(DEFENDANT).asText(),
                                    hearingDate,
                                    hearingCase.get(DEFENDANT_REPRESENTATIVE).asText(),
                                    hearingCase.get(PROSECUTING_AUTHORITY).asText(),
                                    hearingCase.get("formattedLinkedCases").asText(),
                                    listNote
                                ));
                            });
                            result.computeIfAbsent(hearing.get("hearingType").asText(), x -> new ArrayList<>())
                                .addAll(rows);
                        });
                    })
                )
            )
        );

        return sort(result);
    }

    private static Map<String, List<CrownWarnedList>> sort(Map<String, List<CrownWarnedList>> cases) {
        return cases.entrySet().stream()
            .sorted(COMPARATOR)
            .collect(Collectors.toMap(
                Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));
    }
}
