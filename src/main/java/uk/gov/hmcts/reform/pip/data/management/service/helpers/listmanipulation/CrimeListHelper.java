package uk.gov.hmcts.reform.pip.data.management.service.helpers.listmanipulation;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.commons.lang3.StringUtils;
import uk.gov.hmcts.reform.pip.data.management.service.helpers.GeneralHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Helper class for crime lists.
 *  Crown Daily List.
 *  Magistrates Public List.
 */
public final class CrimeListHelper {
    private static final String CASE = "case";
    private static final String CASE_SEQUENCE_INDICATOR = "caseSequenceIndicator";
    private static final String LISTING_DETAILS = "listingDetails";
    private static final String LISTING_NOTES = "listingNotes";
    private static final String REPORTING_RESTRICTION_DETAIL = "reportingRestrictionDetail";
    private static final String COMBINED_REPORTING_RESTRICTION_DETAIL = "combinedReportingRestriction";
    private static final String LINKED_CASES = "linkedCases";
    private static final String COURT_ROOM_NAME = "courtRoomName";

    private static final String SESSION_COURT_ROOM = "formattedSessionCourtRoom";
    private static final String NO_BORDER_BOTTOM = "no-border-bottom";

    private CrimeListHelper() {
    }

    public static void formattedCourtRoomName(JsonNode courtRoom, JsonNode session) {
        ObjectNode sessionObj = (ObjectNode) session;
        if (GeneralHelper.findAndReturnNodeText(courtRoom, COURT_ROOM_NAME)
            .toLowerCase(Locale.ENGLISH)
            .contains("to be allocated")) {
            sessionObj.put(
                SESSION_COURT_ROOM,
                GeneralHelper.findAndReturnNodeText(courtRoom, COURT_ROOM_NAME)
            );
        } else {
            sessionObj.put(
                SESSION_COURT_ROOM,
                GeneralHelper.findAndReturnNodeText(session, SESSION_COURT_ROOM)
            );
        }
    }

    public static void formatCaseInformation(JsonNode hearing) {
        AtomicReference<StringBuilder> linkedCases = new AtomicReference<>(new StringBuilder());
        StringBuilder listingNotes = new StringBuilder();

        if (hearing.has(CASE)) {
            hearing.get(CASE).forEach(hearingCase -> {
                linkedCases.set(new StringBuilder());
                ObjectNode caseObj = (ObjectNode) hearingCase;

                if (!hearingCase.has("caseNumber")) {
                    caseObj.put("caseNumber", "");
                }

                if (hearingCase.has("caseLinked")) {
                    hearingCase.get("caseLinked").forEach(
                        caseLinked -> linkedCases.get()
                            .append(GeneralHelper.findAndReturnNodeText(caseLinked, "caseId")).append(", ")
                    );
                }
                caseObj.put(LINKED_CASES, GeneralHelper.trimAnyCharacterFromStringEnd(linkedCases.toString()));

                if (!hearingCase.has(CASE_SEQUENCE_INDICATOR)) {
                    caseObj.put(CASE_SEQUENCE_INDICATOR, "");
                }

                formatReportingRestrictedDetail(hearingCase, caseObj);

            });
        }

        if (hearing.has(LISTING_DETAILS)) {
            listingNotes.append(hearing.get(LISTING_DETAILS).get("listingRepDeadline")).append(", ");
        }
        ((ObjectNode) hearing).put(LISTING_NOTES,
                                   GeneralHelper.trimAnyCharacterFromStringEnd(listingNotes.toString())
                                       .replace("\"", "")
        );
    }

    private static void formatReportingRestrictedDetail(JsonNode hearingCase, ObjectNode caseObj) {
        StringBuilder reportingRestrictionDetail = new StringBuilder();
        if (hearingCase.has(REPORTING_RESTRICTION_DETAIL)) {


            hearingCase.get(REPORTING_RESTRICTION_DETAIL).forEach(detail -> {
                if (!reportingRestrictionDetail.isEmpty()) {
                    reportingRestrictionDetail.append(", ");
                }
                reportingRestrictionDetail.append(detail.asText());
            });

        }

        caseObj.put(COMBINED_REPORTING_RESTRICTION_DETAIL, reportingRestrictionDetail.toString());
    }

    public static void formatCaseHtmlTable(JsonNode hearing) {
        if (hearing.has(CASE)) {
            hearing.get(CASE).forEach(hearingCase -> {
                ObjectNode caseObj = (ObjectNode) hearingCase;
                caseObj.put("caseCellBorder", "");
                if (!GeneralHelper.findAndReturnNodeText(hearingCase, LINKED_CASES).isEmpty()
                    || !GeneralHelper.findAndReturnNodeText(hearing, LISTING_NOTES).isEmpty()
                    || !GeneralHelper.findAndReturnNodeText(
                        hearingCase, COMBINED_REPORTING_RESTRICTION_DETAIL).isEmpty()) {
                    caseObj.put("caseCellBorder", NO_BORDER_BOTTOM);
                }

                caseObj.put("linkedCasesBorder", "");
                if (!GeneralHelper.findAndReturnNodeText(hearing, LISTING_NOTES).isEmpty()) {
                    caseObj.put("linkedCasesBorder", NO_BORDER_BOTTOM);
                }

                caseObj.put("reportingRestrictionDetailBorder", "");
                if (!GeneralHelper.findAndReturnNodeText(hearingCase, LINKED_CASES).isEmpty()
                    || !GeneralHelper.findAndReturnNodeText(hearing, LISTING_NOTES).isEmpty()) {
                    caseObj.put("reportingRestrictionDetailBorder", NO_BORDER_BOTTOM);
                }
            });
        }
    }

    public static String formatDefendantAddress(JsonNode addressNode) {
        String address = formatDefendantAddressWithoutPostcode(addressNode);
        String postCode = GeneralHelper.findAndReturnNodeText(addressNode, "postCode");

        if (!StringUtils.isBlank(postCode)) {
            return  address + ", " + postCode;
        }
        return address;
    }

    public static String formatDefendantAddressWithoutPostcode(JsonNode addressNode) {
        List<String> fullAddress = new ArrayList<>();

        if (addressNode.has("line")) {
            addressNode.get("line")
                .forEach(line -> fullAddress.add(line.asText()));
        }
        fullAddress.add(GeneralHelper.findAndReturnNodeText(addressNode, "town"));
        fullAddress.add(GeneralHelper.findAndReturnNodeText(addressNode, "county"));

        return GeneralHelper.convertToDelimitedString(fullAddress, ", ");
    }
}
