package uk.gov.hmcts.reform.pip.data.management.service.helpers.listmanipulation;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import uk.gov.hmcts.reform.pip.data.management.service.helpers.DateHelper;
import uk.gov.hmcts.reform.pip.data.management.service.helpers.GeneralHelper;
import uk.gov.hmcts.reform.pip.data.management.service.helpers.JudiciaryHelper;
import uk.gov.hmcts.reform.pip.data.management.service.helpers.LocationHelper;
import uk.gov.hmcts.reform.pip.data.management.service.helpers.PartyRoleHelper;
import uk.gov.hmcts.reform.pip.data.management.service.helpers.SittingHelper;
import uk.gov.hmcts.reform.pip.model.publication.Language;

import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@SuppressWarnings("PMD.TooManyMethods")
public final class CrownFirmListHelper {
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private static final String COURT_HOUSE = "courtHouse";
    private static final String COURT_ROOM = "courtRoom";
    private static final String SESSION = "session";
    private static final String SITTINGS = "sittings";
    private static final String HEARING = "hearing";
    private static final String HEARINGS = "hearings";
    private static final String HEARING_TYPE = "hearingType";
    private static final String SITTING_DATE = "sittingDate";
    private static final String SITTING_START = "sittingStart";
    private static final String CASE_SEQUENCE_INDICATOR = "caseSequenceIndicator";
    private static final String DEFENDANT = "defendant";
    private static final String PROSECUTING_AUTHORITY = "prosecutingAuthority";
    private static final String LINKED_CASES = "linkedCases";
    private static final String LISTING_NOTES = "listingNotes";
    private static final String FORMATTED_COURT_ROOM_NAME = "formattedSessionCourtRoom";
    private static final String DEFENDANT_REPRESENTATIVE = "defendantRepresentative";
    private static final String COURT_LIST = "courtLists";
    private static final String FORMATTED_DURATION = "formattedDuration";
    private static final String FORMATTED_COURT_ROOM = "formattedCourtRoom";
    private static final String CASE_CELL_BORDER = "caseCellBorder";
    private static final String LINKED_CASES_BORDER = "linkedCasesBorder";

    private CrownFirmListHelper() {
    }

    public static void crownFirmListFormatted(JsonNode artefact, Language language) {
        artefact.get(COURT_LIST).forEach(
            courtList -> courtList.get(COURT_HOUSE).get(COURT_ROOM).forEach(
                courtRoom -> courtRoom.get(SESSION).forEach(session -> {
                    StringBuilder formattedJudiciary = new StringBuilder();
                    formattedJudiciary.append(JudiciaryHelper.findAndManipulateJudiciary(session));

                    session.get(SITTINGS).forEach(sitting -> {
                        DateHelper.calculateDuration(sitting, language);
                        String sittingDate = DateHelper.formatTimeStampToBst(
                            sitting.get(SITTING_START).asText(), Language.ENGLISH,
                            false, false, "EEEE dd MMMM yyyy"
                        );
                        ((ObjectNode)sitting).put(SITTING_DATE, sittingDate);
                        SittingHelper.manipulatedSitting(courtRoom, session, sitting, FORMATTED_COURT_ROOM);

                        sitting.get(HEARING).forEach(hearing -> {
                            formatCaseTime(sitting, (ObjectNode) hearing);
                            CrimeListHelper.formatCaseInformation(hearing);
                            CrimeListHelper.formatCaseHtmlTable(hearing);
                            hearing.get("case").forEach(hearingCase -> {
                                PartyRoleHelper.handleParties(hearingCase);
                                moveTableColumnValuesToCase(sitting, hearing, hearingCase);
                            });
                        });
                    });
                    LocationHelper.formattedCourtRoomName(courtRoom, session, formattedJudiciary);
                })
            )
        );
    }

    public static void splitByCourtAndDate(JsonNode artefact) {
        List<String> uniqueSittingDates = findUniqueSittingDatesPerCounts(artefact);
        String[] uniqueDates =  uniqueSittingDates.toArray(new String[0]);
        setListToDates((ObjectNode) artefact, uniqueSittingDates);
        ArrayNode courtListByDateArray = MAPPER.createArrayNode();
        for (int i = 0; i < uniqueSittingDates.size(); i++) {
            int finalI = i;
            ArrayNode courtListArray = MAPPER.createArrayNode();
            artefact.get(COURT_LIST).forEach(courtList -> {

                ObjectNode courtListNode = MAPPER.createObjectNode();
                ArrayNode courtRoomsArray = MAPPER.createArrayNode();
                ObjectNode unAllocatedCourtRoom = MAPPER.createObjectNode();
                ArrayNode unAllocatedCourtRoomHearings = MAPPER.createArrayNode();

                courtListNode.put("courtName",
                    GeneralHelper.findAndReturnNodeText(courtList.get(COURT_HOUSE),
                                                        "courtHouseName"));
                courtListNode.put("courtSittingDate", uniqueSittingDates.get(finalI));

                courtList.get(COURT_HOUSE).get(COURT_ROOM).forEach(courtRoom -> {
                    ObjectNode courtRoomNode = MAPPER.createObjectNode();
                    ArrayNode hearingArray = MAPPER.createArrayNode();
                    courtRoom.get("session").forEach(session -> {
                        session.get(SITTINGS).forEach(
                            sitting -> SittingHelper.checkSittingDateAlreadyExists(
                            sitting, uniqueDates, hearingArray, finalI
                            ));

                        checkToBeAllocatedRoom(courtRoomNode, session, unAllocatedCourtRoom, hearingArray,
                                               unAllocatedCourtRoomHearings);
                    });
                    if (!GeneralHelper.findAndReturnNodeText(courtRoomNode, FORMATTED_COURT_ROOM_NAME).isBlank()) {
                        checkAndAddToArrayNode(hearingArray, courtRoomNode,
                                               HEARINGS, courtRoomsArray);
                    }
                });
                checkAndAddToArrayNode(unAllocatedCourtRoomHearings, unAllocatedCourtRoom, HEARINGS, courtRoomsArray);

                checkAndAddToArrayNode(courtRoomsArray, courtListNode,
                                       "courtRooms", courtListArray);
            });
            courtListByDateArray.add(courtListArray);
        }
        ((ObjectNode)artefact).putArray("courtListsByDate")
            .addAll(courtListByDateArray);
    }

    private static List<String> findUniqueSittingDatesPerCounts(JsonNode artefact) {
        Map<Date, String> allSittingDateTimes = new ConcurrentHashMap<>();
        artefact.get(COURT_LIST).forEach(courtList -> {
            Map<Date, String> sittingDateTimes = SittingHelper.findAllSittingDates(
                courtList.get(COURT_HOUSE).get(COURT_ROOM));
            allSittingDateTimes.putAll(sittingDateTimes);
        });
        return GeneralHelper.findUniqueDateAndSort(allSittingDateTimes);
    }

    private static void checkAndAddToArrayNode(ArrayNode arrayToCheck, ObjectNode destinationNode,
                                               String destinationNodeAttribute, ArrayNode arrayToAdd) {
        if (arrayToCheck.size() > 0) {
            destinationNode.putArray(destinationNodeAttribute).addAll(arrayToCheck);
            arrayToAdd.add(destinationNode);
        }
    }

    private static void checkToBeAllocatedRoom(ObjectNode courtRoomNode, JsonNode session,
        ObjectNode unAllocatedCourtRoom, ArrayNode hearingArray, ArrayNode unAllocatedCourtRoomHearings) {
        if (GeneralHelper.findAndReturnNodeText(session, FORMATTED_COURT_ROOM_NAME)
            .toLowerCase(Locale.ENGLISH)
            .contains("to be allocated")
            && hearingArray.size() > 0) {
            unAllocatedCourtRoom.put(
                FORMATTED_COURT_ROOM_NAME,
                GeneralHelper.findAndReturnNodeText(session, FORMATTED_COURT_ROOM)
            );
            unAllocatedCourtRoom.put("unallocatedSection", "true");
            unAllocatedCourtRoomHearings.addAll(hearingArray);
        } else {
            courtRoomNode.put(
                FORMATTED_COURT_ROOM_NAME,
                GeneralHelper.findAndReturnNodeText(session, FORMATTED_COURT_ROOM)
            );
            courtRoomNode.put("unallocatedSection", "false");
        }
    }

    private static void moveTableColumnValuesToCase(JsonNode sitting, JsonNode hearing, JsonNode hearingCase) {
        ObjectNode caseObj = (ObjectNode) hearingCase;
        caseObj.put("sittingAt",
                    GeneralHelper.findAndReturnNodeText(hearing,"time"));
        caseObj.put("caseReference",
                    GeneralHelper.findAndReturnNodeText(hearingCase,"caseNumber"));
        caseObj.put(DEFENDANT,
                    GeneralHelper.findAndReturnNodeText(hearingCase, DEFENDANT));
        caseObj.put(HEARING_TYPE,
                    GeneralHelper.findAndReturnNodeText(hearing, HEARING_TYPE));
        caseObj.put(FORMATTED_DURATION,
                    GeneralHelper.findAndReturnNodeText(sitting, FORMATTED_DURATION));
        caseObj.put(CASE_SEQUENCE_INDICATOR,
                    GeneralHelper.findAndReturnNodeText(hearingCase, CASE_SEQUENCE_INDICATOR));
        caseObj.put(DEFENDANT_REPRESENTATIVE,
                    GeneralHelper.findAndReturnNodeText(hearingCase, DEFENDANT_REPRESENTATIVE));
        caseObj.put(PROSECUTING_AUTHORITY,
                    GeneralHelper.findAndReturnNodeText(hearingCase, PROSECUTING_AUTHORITY));
        caseObj.put(LINKED_CASES,
                    GeneralHelper.findAndReturnNodeText(hearingCase, LINKED_CASES));
        caseObj.put(LISTING_NOTES,
                    GeneralHelper.findAndReturnNodeText(hearing, LISTING_NOTES));
        caseObj.put(CASE_CELL_BORDER,
                    GeneralHelper.findAndReturnNodeText(hearingCase, CASE_CELL_BORDER));
        caseObj.put(LINKED_CASES_BORDER,
                    GeneralHelper.findAndReturnNodeText(hearingCase, LINKED_CASES_BORDER));
    }

    private static void setListToDates(ObjectNode artefact, List<String> uniqueSittingDates) {
        String startDate = uniqueSittingDates.get(0)
            .substring(uniqueSittingDates.get(0).indexOf(' ') + 1);
        String endDate = uniqueSittingDates.get(uniqueSittingDates.size() - 1)
            .substring(uniqueSittingDates.get(uniqueSittingDates.size() - 1).indexOf(' ') + 1);
        artefact.put("listStartDate", startDate);
        artefact.put("listEndDate", endDate);
    }

    private static void formatCaseTime(JsonNode sitting, ObjectNode hearing) {
        if (!GeneralHelper.findAndReturnNodeText(sitting, SITTING_START).isEmpty()) {
            hearing.put("time",
                        DateHelper.formatTimeStampToBst(
                            GeneralHelper.findAndReturnNodeText(sitting, SITTING_START), Language.ENGLISH,
                            false, false, "h:mma"
                        ).replace(":00", ""));
        }
    }
}
