package uk.gov.hmcts.reform.pip.data.management.service.helpers.listmanipulation;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import uk.gov.hmcts.reform.pip.data.management.models.templatemodels.FamilyMixedList;
import uk.gov.hmcts.reform.pip.data.management.service.helpers.CaseHelper;
import uk.gov.hmcts.reform.pip.data.management.service.helpers.DateHelper;
import uk.gov.hmcts.reform.pip.data.management.service.helpers.GeneralHelper;
import uk.gov.hmcts.reform.pip.data.management.service.helpers.JudiciaryHelper;
import uk.gov.hmcts.reform.pip.data.management.service.helpers.PartyRoleHelper;
import uk.gov.hmcts.reform.pip.data.management.service.helpers.PartyRoleMapper;
import uk.gov.hmcts.reform.pip.data.management.service.helpers.SittingHelper;
import uk.gov.hmcts.reform.pip.model.publication.Language;

@SuppressWarnings("java:S108")
public final class FamilyMixedListHelper {
    private static final String APPLICANT = "applicant";
    private static final String APPLICANT_REPRESENTATIVE = "applicantRepresentative";
    private static final String RESPONDENT = "respondent";
    private static final String RESPONDENT_REPRESENTATIVE = "respondentRepresentative";
    private static final String PARTY_ROLE = "partyRole";

    private static final String COURT_LIST = "courtLists";
    private static final String COURT_HOUSE = "courtHouse";
    private static final String COURT_ROOM = "courtRoom";
    private static final String SESSION = "session";
    private static final String SITTINGS = "sittings";
    private static final String HEARING = "hearing";
    private static final String CASE = "case";
    private static final String PARTY = "party";
    private static final String REPORTING_RESTRICTION_DETAIL = "reportingRestrictionDetail";
    private static final String COURT_HOUSE_NAME = "courtHouseName";
    private static final String COURT_ROOM_NAME = "courtRoomName";


    private FamilyMixedListHelper() {
    }

    public static void manipulatedListData(JsonNode artefact, Language language) {
        artefact.get(COURT_LIST)
            .forEach(courtList -> courtList.get(COURT_HOUSE).get(COURT_ROOM)
                .forEach(courtRoom -> {
                    String courtHouseName = GeneralHelper.findAndReturnNodeText(
                        courtList.get(COURT_HOUSE),
                        COURT_HOUSE_NAME
                    );
                    String courtRoomName = GeneralHelper.findAndReturnNodeText(courtRoom, COURT_ROOM_NAME);
                    ((ObjectNode) courtRoom).put(COURT_HOUSE, courtHouseName);
                    ((ObjectNode) courtRoom).put(COURT_ROOM_NAME, courtRoomName);
                    courtRoom.get(SESSION).forEach(session -> {
                        ((ObjectNode) session).put("formattedSessionJudiciary",
                                                   JudiciaryHelper.findAndManipulateJudiciary(session));
                        session.get(SITTINGS).forEach(sitting -> {
                            DateHelper.calculateDuration(sitting, language);
                            DateHelper.formatStartTime(sitting, "h:mma");
                            SittingHelper.findAndConcatenateHearingPlatform(sitting, session);

                            sitting.get(HEARING).forEach(
                                hearing -> hearing.get(CASE).forEach(hearingCase -> {
                                    handleParties(hearingCase);
                                    CaseHelper.manipulateCaseInformation((ObjectNode) hearingCase);
                                    ((ObjectNode) hearingCase).put(
                                        "formattedReportingRestriction",
                                        GeneralHelper.formatNodeArray(hearingCase,
                                                                      REPORTING_RESTRICTION_DETAIL, ", ")
                                    );
                                })
                            );
                        });
                    });
                }));
    }

    public static FamilyMixedList buildFamilyMixedList(
        JsonNode session,
        JsonNode courtRoom,
        JsonNode sitting,
        JsonNode hearing,
        JsonNode caseNode
    ) {
        FamilyMixedList thisCase = new FamilyMixedList();

        thisCase.setCourtHouse(courtRoom.path("courtHouse").asText());
        String courtRoomName = courtRoom.path("courtRoomName").asText();
        String formattedSessionJudiciary = session.path("formattedSessionJudiciary").asText();
        thisCase.setCourtRoom(formattedSessionJudiciary.isBlank()
                                  ? courtRoomName
                                  : courtRoomName + ", Before: " + formattedSessionJudiciary);
        thisCase.setTime(sitting.path("time").asText());
        thisCase.setCaseRef(caseNode.path("caseNumber").asText());
        thisCase.setCaseName(caseNode.path("formattedCaseName").asText());
        thisCase.setCaseType(caseNode.path("caseType").asText());
        thisCase.setHearingType(hearing.path("hearingType").asText());
        thisCase.setLocation(sitting.path("caseHearingChannel").asText());
        thisCase.setDuration(sitting.path("formattedDuration").asText());

        thisCase.setApplicant(
            CftListHelper.buildParty(
                caseNode,
                "applicant",
                "applicantRepresentative"
            )
        );

        thisCase.setRespondent(
            CftListHelper.buildParty(
                caseNode,
                "respondent",
                "respondentRepresentative"
            )
        );

        thisCase.setReportingRestriction(
            caseNode.path("formattedReportingRestriction").asText()
        );

        return thisCase;
    }


    private static void handleParties(JsonNode node) {
        StringBuilder applicant = new StringBuilder();
        StringBuilder applicantRepresentative = new StringBuilder();
        StringBuilder respondent = new StringBuilder();
        StringBuilder respondentRepresentative = new StringBuilder();

        if (node.has(PARTY)) {
            node.get(PARTY).forEach(party -> {
                if (!GeneralHelper.findAndReturnNodeText(party, PARTY_ROLE).isEmpty()) {
                    switch (PartyRoleMapper.convertPartyRole(party.get(PARTY_ROLE).asText())) {
                        case "APPLICANT_PETITIONER" -> PartyRoleHelper.formatPartyDetails(
                            applicant,
                            createPartyDetails(party)
                        );
                        case "APPLICANT_PETITIONER_REPRESENTATIVE" -> PartyRoleHelper.formatPartyDetails(
                            applicantRepresentative,
                            createPartyDetails(party)
                        );
                        case "RESPONDENT" -> PartyRoleHelper.formatPartyDetails(respondent, createPartyDetails(party));
                        case "RESPONDENT_REPRESENTATIVE" -> PartyRoleHelper.formatPartyDetails(
                            respondentRepresentative,
                            createPartyDetails(party)
                        );
                        default -> {
                        }
                    }
                }
            });
        }

        ObjectNode nodeObj = (ObjectNode) node;
        nodeObj.put(APPLICANT,
                       GeneralHelper.trimAnyCharacterFromStringEnd(applicant.toString()));
        nodeObj.put(APPLICANT_REPRESENTATIVE,
                       GeneralHelper.trimAnyCharacterFromStringEnd(applicantRepresentative.toString()));
        nodeObj.put(RESPONDENT,
                       GeneralHelper.trimAnyCharacterFromStringEnd(respondent.toString()));
        nodeObj.put(RESPONDENT_REPRESENTATIVE,
                       GeneralHelper.trimAnyCharacterFromStringEnd(respondentRepresentative.toString()));
    }

    private static String createPartyDetails(JsonNode party) {
        if (party.has("individualDetails")) {
            return PartyRoleHelper.createIndividualDetails(party, false);
        } else if (party.has("organisationDetails")) {
            return GeneralHelper.findAndReturnNodeText(party.get("organisationDetails"), "organisationName");
        }
        return "";
    }
}
