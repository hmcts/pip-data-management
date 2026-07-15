package uk.gov.hmcts.reform.pip.data.management.service.helpers.listmanipulation;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import uk.gov.hmcts.reform.pip.data.management.models.templatemodels.magistratespubliclist.CourtRoom;
import uk.gov.hmcts.reform.pip.data.management.models.templatemodels.magistratespubliclist.Hearing;
import uk.gov.hmcts.reform.pip.data.management.models.templatemodels.magistratespubliclist.Sitting;
import uk.gov.hmcts.reform.pip.data.management.service.helpers.DateHelper;
import uk.gov.hmcts.reform.pip.data.management.service.helpers.GeneralHelper;
import uk.gov.hmcts.reform.pip.data.management.service.helpers.JudiciaryHelper;
import uk.gov.hmcts.reform.pip.data.management.service.helpers.LocationHelper;
import uk.gov.hmcts.reform.pip.data.management.service.helpers.PartyRoleHelper;

import java.util.ArrayList;
import java.util.List;
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
                                    PartyRoleHelper.findProsecutingAuthority(hearingCase);
                                    Predicate<JsonNode> partyFilter = p -> p.has(PARTY_ROLE)
                                        && p.get(PARTY_ROLE).asText().equals(DEFENDANT);
                                    PartyRoleHelper.findMainDefendantName(hearingCase, partyFilter);
                                    PartyRoleHelper.handlePartyOffences(hearingCase, partyFilter);
                                });
                            }
                            if (hearing.has(APPLICATION)) {
                                hearing.get(APPLICATION).forEach(application -> {
                                    PartyRoleHelper.findProsecutingAuthority(application);
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

    public static List<CourtRoom> processRawListData(JsonNode artefact) {
        manipulatedMagistratesPublicListData(artefact);
        List<CourtRoom> courtRooms = new ArrayList<>();

        artefact.get(COURT_LIST).forEach(
            courtList -> courtList.get(COURT_HOUSE).get(COURT_ROOM).forEach(
                courtRoomNode -> courtRoomNode.get("session").forEach(session -> {
                    CourtRoom courtRoom = new CourtRoom();
                    courtRoom.setFormattedCourtRoomName(session.get("formattedSessionCourtRoom").asText());

                    session.get("sittings").forEach(sittingNode -> {
                        Sitting sitting = new Sitting();
                        sitting.setTime(sittingNode.get("time").asText());

                        sittingNode.get("hearing").forEach(hearingNode -> {
                            if (hearingNode.has(CASE)) {
                                hearingNode.get(CASE).forEach(hearingCase -> {
                                    Hearing hearing = new Hearing();
                                    hearing.setCaseUrn(GeneralHelper.findAndReturnNodeText(hearingCase, "caseUrn"));
                                    hearing.setDefendant(GeneralHelper.findAndReturnNodeText(hearingCase, "defendant"));
                                    hearing.setHearingType(GeneralHelper.findAndReturnNodeText(hearingNode,
                                                                                               "hearingType"));
                                    hearing.setProsecutingAuthority(GeneralHelper.findAndReturnNodeText(
                                        hearingCase, "prosecutingAuthority"));
                                    hearing.setOffence(GeneralHelper.findAndReturnNodeText(hearingCase, "offence"));
                                    hearing.setReportingRestriction(hearingCase.has("reportingRestriction")
                                                                        && hearingCase.get("reportingRestriction")
                                                                        .asBoolean());
                                    hearing.setBottomBorder(GeneralHelper.findAndReturnNodeText(hearingCase,
                                                                                                "bottomBorder"));
                                    sitting.getHearings().add(hearing);
                                });
                            }
                            if (hearingNode.has(APPLICATION)) {
                                hearingNode.get(APPLICATION).forEach(application -> {
                                    Hearing hearing = new Hearing();
                                    hearing.setCaseUrn(GeneralHelper.findAndReturnNodeText(application,
                                                                                           "applicationReference"));
                                    hearing.setDefendant(GeneralHelper.findAndReturnNodeText(application, "defendant"));
                                    hearing.setHearingType("");
                                    hearing.setProsecutingAuthority(GeneralHelper.findAndReturnNodeText(
                                        application, "prosecutingAuthority"));
                                    hearing.setOffence(GeneralHelper.findAndReturnNodeText(application, "offence"));
                                    hearing.setReportingRestriction(false);
                                    hearing.setBottomBorder(GeneralHelper.findAndReturnNodeText(application,
                                                                                                "bottomBorder"));
                                    sitting.getHearings().add(hearing);
                                });
                            }
                        });
                        if (!sitting.getHearings().isEmpty()) {
                            courtRoom.getSittings().add(sitting);
                        }
                    });
                    if (!courtRoom.getSittings().isEmpty()) {
                        courtRooms.add(courtRoom);
                    }
                })
            )
        );
        return courtRooms;
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
