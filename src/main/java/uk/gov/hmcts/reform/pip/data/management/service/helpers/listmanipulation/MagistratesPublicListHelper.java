package uk.gov.hmcts.reform.pip.data.management.service.helpers.listmanipulation;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import uk.gov.hmcts.reform.pip.data.management.service.helpers.DateHelper;
import uk.gov.hmcts.reform.pip.data.management.service.helpers.JudiciaryHelper;
import uk.gov.hmcts.reform.pip.data.management.service.helpers.LocationHelper;
import uk.gov.hmcts.reform.pip.data.management.service.helpers.PartyRoleHelper;

import java.util.function.Predicate;

public final class MagistratesPublicListHelper {
    private static final String COURT_LIST = "courtLists";
    private static final String COURT_HOUSE = "courtHouse";
    private static final String COURT_ROOM = "courtRoom";
    private static final String CASE = "case";
    private static final String APPLICATION = "application";
    private static final String SUBJECT = "subject";
    private static final String PARTY_ROLE = "partyRole";
    private static final String DEFENDANT = "DEFENDANT";
    private static final String NO_BORDER_BOTTOM = "no-border-bottom";

    private MagistratesPublicListHelper() {
    }

    public static void manipulatedMagistratesPublicListData(JsonNode artefact) {
        artefact.get(COURT_LIST).forEach(
            courtList -> courtList.get(COURT_HOUSE).get(COURT_ROOM).forEach(
                courtRoom -> courtRoom.get("session").forEach(session -> {
                    StringBuilder formattedJudiciary = new StringBuilder();
                    formattedJudiciary.append(JudiciaryHelper.findAndManipulateJudiciary(session));
                    session.get("sittings").forEach(sitting -> {
                        DateHelper.formatStartTime(sitting, "h:mma");
                        sitting.get("hearing").forEach(hearing -> {
                            if (hearing.has(CASE)) {
                                hearing.get(CASE).forEach(hearingCase -> {
                                    PartyRoleHelper.findProsecutingAuthorities(hearingCase);
                                    Predicate<JsonNode> partyFilter = p -> p.has(PARTY_ROLE)
                                        && p.get(PARTY_ROLE).asText().equals(DEFENDANT);
                                    PartyRoleHelper.findMainDefendantName(hearingCase, partyFilter);
                                    PartyRoleHelper.handlePartyOffences(hearingCase, partyFilter);
                                });
                            }
                            if (hearing.has("application")) {
                                hearing.get("application").forEach(application -> {
                                    PartyRoleHelper.findProsecutingAuthorities(application);
                                    Predicate<JsonNode> partyFilter = p -> p.has(SUBJECT)
                                        && p.get(SUBJECT).asBoolean();
                                    PartyRoleHelper.findMainDefendantName(application, partyFilter);
                                    PartyRoleHelper.handlePartyOffences(application, partyFilter);
                                });
                            }
                            formatCaseHtmlTable(hearing, CASE);
                            formatCaseHtmlTable(hearing, APPLICATION);
                        });
                    });
                    LocationHelper.formattedCourtRoomName(courtRoom, session, formattedJudiciary);
                    CrimeListHelper.formattedCourtRoomName(courtRoom, session);
                })
            )
        );
    }

    private static void formatCaseHtmlTable(JsonNode hearing, String field) {
        JsonNode items = hearing.path(field);

        items.forEach(item -> {
            ObjectNode obj = (ObjectNode) item;
            obj.put("bottomBorder", "");
            if (!item.path("offence").asText("").isBlank()) {
                obj.put("bottomBorder", NO_BORDER_BOTTOM);
            }
        });
    }
}
