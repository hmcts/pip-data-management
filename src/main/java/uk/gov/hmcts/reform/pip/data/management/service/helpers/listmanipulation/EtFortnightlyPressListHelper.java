package uk.gov.hmcts.reform.pip.data.management.service.helpers.listmanipulation;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.extern.slf4j.Slf4j;
import uk.gov.hmcts.reform.pip.data.management.service.helpers.DateHelper;
import uk.gov.hmcts.reform.pip.data.management.service.helpers.GeneralHelper;
import uk.gov.hmcts.reform.pip.data.management.service.helpers.JudiciaryHelper;
import uk.gov.hmcts.reform.pip.data.management.service.helpers.PartyRoleHelper;
import uk.gov.hmcts.reform.pip.data.management.service.helpers.SittingHelper;
import uk.gov.hmcts.reform.pip.model.publication.Language;

import java.util.Date;
import java.util.List;
import java.util.Map;

@Slf4j
public final class EtFortnightlyPressListHelper {
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private static final String COURT_LISTS = "courtLists";
    private static final String COURT_HOUSE = "courtHouse";
    private static final String COURT_ROOM = "courtRoom";
    private static final String SITTING_DATE = "sittingDate";
    private static final String SITTINGS = "sittings";
    private static final String SESSION = "session";
    private static final String HEARING = "hearing";
    private static final String CASE = "case";
    private static final String APPLICANT = "claimant";
    private static final String RESPONDENT = "respondent";
    private static final String TIME_FORMAT = "h:mma";
    private static final String REP = "rep";
    private static final String SITTING_START = "sittingStart";

    private EtFortnightlyPressListHelper() {
    }

    public static void splitByCourtAndDate(JsonNode artefact) {
        artefact.get(COURT_LISTS).forEach(courtList -> {
            ArrayNode sittingArray = MAPPER.createArrayNode();
            Map<Date, String> sittingDateTimes = SittingHelper.findAllSittingDates(
                courtList.get(COURT_HOUSE).get(COURT_ROOM));
            List<String> uniqueSittingDate = GeneralHelper.findUniqueDateAndSort(sittingDateTimes);
            String[] uniqueSittingDates = uniqueSittingDate.toArray(new String[0]);

            for (int i = 0; i < uniqueSittingDates.length; i++) {
                int currentSittingDate = i;
                ObjectNode sittingNode = MAPPER.createObjectNode();
                ArrayNode hearingNodeArray = MAPPER.createArrayNode();
                sittingNode.put(SITTING_DATE, uniqueSittingDates[currentSittingDate]);
                courtList.get(COURT_HOUSE).get(COURT_ROOM).forEach(
                    courtRoom -> courtRoom.get(SESSION).forEach(
                        session -> session.get(SITTINGS).forEach(sitting -> {
                            sittingNode.put("time", sitting.get("time").asText());
                            SittingHelper.checkSittingDateAlreadyExists(sitting, uniqueSittingDates,
                                                          hearingNodeArray, currentSittingDate);
                        })
                    )
                );
                sittingNode.putArray(HEARING).addAll(hearingNodeArray);
                sittingArray.add(sittingNode);
            }
            ((ObjectNode)courtList).putArray(SITTINGS).addAll(sittingArray);
        });
    }

    public static void etFortnightlyListFormatted(JsonNode artefact, Map<String, Object> language) {
        artefact.get(COURT_LISTS).forEach(
            courtList -> courtList.get(COURT_HOUSE).get(COURT_ROOM).forEach(
                courtRoom -> courtRoom.get(SESSION).forEach(
                    session -> session.get(SITTINGS).forEach(sitting -> {
                        String sittingDate = DateHelper.formatTimeStampToBst(
                            sitting.get(SITTING_START).asText(), Language.ENGLISH, false, false,
                            "EEEE dd MMMM yyyy");
                        ((ObjectNode)sitting).put(SITTING_DATE, sittingDate);
                        DateHelper.formatStartTime(sitting, TIME_FORMAT);
                        sitting.get(HEARING).forEach(hearing -> {
                            moveTableColumnValuesToHearing(courtRoom, sitting, (ObjectNode) hearing);
                            hearing.get(CASE).forEach(hearingCase -> {
                                moveTablePartyValuesToCase((ObjectNode) hearingCase, language);
                                if (!hearingCase.has("caseSequenceIndicator")) {
                                    ((ObjectNode)hearingCase).put("caseSequenceIndicator", "");
                                }
                            });
                        });
                    })
                )
            )
        );
    }

    private static void moveTableColumnValuesToHearing(JsonNode courtRoom, JsonNode sitting,
                                                       ObjectNode hearing) {
        hearing.put(COURT_ROOM,
                    GeneralHelper.findAndReturnNodeText(courtRoom, "courtRoomName"));
        hearing.put("formattedDuration",
                    GeneralHelper.findAndReturnNodeText(sitting, "formattedDuration"));
        hearing.put("caseHearingChannel",
                    GeneralHelper.findAndReturnNodeText(sitting, "caseHearingChannel"));
    }

    public static void moveTablePartyValuesToCase(ObjectNode hearingCase, Map<String, Object> language) {
        hearingCase.put(APPLICANT,
                        GeneralHelper.findAndReturnNodeText(hearingCase, APPLICANT));
        hearingCase.put("applicantRepresentative",
                        language.get(REP) + GeneralHelper.findAndReturnNodeText(hearingCase,
                                                                                "applicantRepresentative"));
        hearingCase.put("RESPONDENT",
                        GeneralHelper.findAndReturnNodeText(hearingCase, "RESPONDENT"));
        hearingCase.put("respondentRepresentative",
                        language.get(REP) + GeneralHelper.findAndReturnNodeText(hearingCase,
                                                                                "respondentRepresentative"));
    }

    public static void manipulatedListData(JsonNode artefact, Language language, boolean initialised) {
        artefact.get(COURT_LISTS).forEach(
            courtList -> courtList.get(COURT_HOUSE).get(COURT_ROOM).forEach(
                courtRoom -> courtRoom.get(SESSION).forEach(session -> {
                    StringBuilder formattedJudiciary = new StringBuilder();
                    formattedJudiciary.append(JudiciaryHelper.findAndManipulateJudiciary(session));
                    session.get(SITTINGS).forEach(sitting -> {
                        DateHelper.calculateDuration(sitting, language);
                        DateHelper.formatStartTime(sitting, TIME_FORMAT);
                        SittingHelper.findAndConcatenateHearingPlatform(sitting, session);

                        sitting.get(HEARING).forEach(hearing ->
                            hearing.get(CASE).forEach(hearingCase -> {
                                if (hearingCase.has("party")) {
                                    PartyRoleHelper.findAndManipulatePartyInformation(hearingCase, initialised);
                                } else {
                                    ObjectNode hearingObj = (ObjectNode) hearingCase;
                                    hearingObj.put(APPLICANT, "");
                                    hearingObj.put(RESPONDENT, "");
                                }
                            })
                        );
                    });
                })
            )
        );
    }
}
