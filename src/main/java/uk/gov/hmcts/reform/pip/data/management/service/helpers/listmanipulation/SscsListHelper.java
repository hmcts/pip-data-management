package uk.gov.hmcts.reform.pip.data.management.service.helpers.listmanipulation;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import uk.gov.hmcts.reform.pip.data.management.models.templatemodels.sscsdailylist.CourtHouse;
import uk.gov.hmcts.reform.pip.data.management.models.templatemodels.sscsdailylist.CourtRoom;
import uk.gov.hmcts.reform.pip.data.management.models.templatemodels.sscsdailylist.Hearing;
import uk.gov.hmcts.reform.pip.data.management.models.templatemodels.sscsdailylist.HearingCase;
import uk.gov.hmcts.reform.pip.data.management.models.templatemodels.sscsdailylist.Sitting;
import uk.gov.hmcts.reform.pip.data.management.service.helpers.DateHelper;
import uk.gov.hmcts.reform.pip.data.management.service.helpers.GeneralHelper;
import uk.gov.hmcts.reform.pip.data.management.service.helpers.JudiciaryHelper;
import uk.gov.hmcts.reform.pip.data.management.service.helpers.PartyRoleHelper;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public final class SscsListHelper {
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final String TIME_FORMAT = "h:mma";
    private static final String DELIMITER = ", ";

    private static final String CHANNEL = "channel";
    private static final String APPLICANT = "applicant";
    private static final String APPLICANT_REPRESENTATIVE = "applicantRepresentative";
    private static final String COURT_ROOM = "courtRoom";
    private static final String SESSION = "session";
    private static final String SITTINGS = "sittings";
    private static final String HEARING = "hearing";
    private static final String SESSION_CHANNEL = "sessionChannel";
    private static final String CASE = "case";
    private static final String PARTY = "party";
    private static final String PARTY_ROLE = "partyRole";
    private static final String ORGANISATION_DETAILS = "organisationDetails";
    private static final String ORGANISATION_NAME = "organisationName";
    private static final String RESPONDENT_ROLE = "RESPONDENT";

    private SscsListHelper() {
    }

    public static CourtHouse courtHouseBuilder(JsonNode node) throws JsonProcessingException {
        JsonNode thisCourtHouseNode = node.get("courtHouse");
        CourtHouse thisCourtHouse = new CourtHouse();
        thisCourtHouse.setName(GeneralHelper.safeGet("courtHouseName", thisCourtHouseNode));
        thisCourtHouse.setPhone(GeneralHelper.safeGet("courtHouseContact.venueTelephone", thisCourtHouseNode));
        thisCourtHouse.setEmail(GeneralHelper.safeGet("courtHouseContact.venueEmail", thisCourtHouseNode));
        List<CourtRoom> courtRoomList = new ArrayList<>();
        for (JsonNode courtRoom : thisCourtHouseNode.get(COURT_ROOM)) {
            courtRoomList.add(courtRoomBuilder(courtRoom));
        }
        thisCourtHouse.setListOfCourtRooms(courtRoomList);
        return thisCourtHouse;
    }

    private static CourtRoom courtRoomBuilder(JsonNode node) throws JsonProcessingException {
        CourtRoom thisCourtRoom = new CourtRoom();
        thisCourtRoom.setName(GeneralHelper.safeGet("courtRoomName", node));
        List<Sitting> sittingList = new ArrayList<>();
        TypeReference<List<String>> typeReference = new TypeReference<>() {
        };
        for (final JsonNode session : node.get(SESSION)) {
            String sessionChannelString = "";
            if (session.has(SESSION_CHANNEL)) {
                List<String> sessionChannel = MAPPER.readValue(session.get(SESSION_CHANNEL).toString(), typeReference);
                sessionChannelString = String.join(DELIMITER, sessionChannel);
            }

            String judiciary = JudiciaryHelper.findAndManipulateJudiciary(session);
            for (JsonNode sitting : session.get(SITTINGS)) {
                sittingList.add(sittingBuilder(sessionChannelString, sitting, judiciary));
            }
        }
        thisCourtRoom.setListOfSittings(sittingList);
        return thisCourtRoom;
    }

    private static Sitting sittingBuilder(String sessionChannel, JsonNode node, String judiciary)
        throws JsonProcessingException {
        Sitting sitting = new Sitting();
        DateHelper.formatStartTime(node, TIME_FORMAT);
        sitting.setJudiciary(judiciary);
        List<Hearing> listOfHearings = new ArrayList<>();
        List<HearingCase> listOfCases = new ArrayList<>();
        if (node.has(CHANNEL) && !node.get(CHANNEL).isEmpty()) {
            List<String> channelList = MAPPER.readValue(node.get(CHANNEL).toString(), new TypeReference<>() {});
            sitting.setChannel(String.join(DELIMITER, channelList));
        } else {
            sitting.setChannel(sessionChannel);
        }
        Iterator<JsonNode> nodeIterator = node.get(HEARING).elements();
        Hearing currentHearing = new Hearing();
        HearingCase currentCase;
        while (nodeIterator.hasNext()) {
            JsonNode currentHearingNode = nodeIterator.next();
            Iterator<JsonNode> caseIterator = currentHearingNode.get(CASE).elements();
            while (caseIterator.hasNext()) {
                JsonNode currentCaseNode = caseIterator.next();
                if (currentCaseNode.has(PARTY)) {
                    currentCase = caseBuilder(currentCaseNode);
                } else {
                    currentCase = caseBuilder(currentHearingNode);
                }
                currentCase.setRespondent(formatRespondent(currentCaseNode, currentHearingNode));
                currentCase.setAppealRef(GeneralHelper.safeGet("caseNumber", currentCaseNode));
                currentCase.setHearingTime(node.get("time").asText());
                currentCase.setJudiciary(sitting.getJudiciary());
                listOfCases.add(currentCase);
            }
            currentHearing.setListOfCases(listOfCases);
        }
        listOfHearings.add(currentHearing);
        sitting.setListOfHearings(listOfHearings);
        return sitting;
    }

    private static HearingCase caseBuilder(JsonNode node) {
        HearingCase currentCase = new HearingCase();
        PartyRoleHelper.findAndManipulatePartyInformation(node, false);
        currentCase.setAppellant(node.get(APPLICANT).asText());
        currentCase.setAppellantRepresentative(node.get(APPLICANT_REPRESENTATIVE).asText());
        return currentCase;
    }

    private static String formatRespondent(JsonNode caseNode, JsonNode hearingNode) {
        String respondents = getPartyRespondents(caseNode);
        if (respondents.isBlank()) {
            return getPartyRespondents(hearingNode);
        }
        return respondents;
    }

    private static String getPartyRespondents(JsonNode node) {
        List<String> respondents = new ArrayList<>();

        if (node.has(PARTY)) {
            for (JsonNode party : node.get(PARTY)) {
                String partyRole = GeneralHelper.findAndReturnNodeText(party, PARTY_ROLE);
                if (RESPONDENT_ROLE.equals(partyRole) && party.has(ORGANISATION_DETAILS)) {
                    String respondent = GeneralHelper.findAndReturnNodeText(
                        party.get(ORGANISATION_DETAILS), ORGANISATION_NAME
                    );
                    if (!respondent.isBlank()) {
                        respondents.add(respondent);
                    }
                }
            }
        }
        return String.join(DELIMITER, respondents);
    }
}
