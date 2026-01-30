package uk.gov.hmcts.reform.pip.data.management.service.helpers.listmanipulation;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.commons.lang3.StringUtils;
import uk.gov.hmcts.reform.pip.data.management.service.helpers.GeneralHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Helper class for crime lists.
 *  Crown Daily List.
 *  Magistrates Public List.
 */
public final class CrimeListHelper {
    private static final String COURT_ROOM_NAME = "courtRoomName";

    private static final String SESSION_COURT_ROOM = "formattedSessionCourtRoom";

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

    public static String formatAddress(JsonNode addressNode) {
        String address = formatAddressWithoutPostcode(addressNode);
        String postCode = GeneralHelper.findAndReturnNodeText(addressNode, "postCode");

        if (!StringUtils.isBlank(postCode)) {
            return  address + ", " + postCode;
        }
        return address;
    }

    public static String formatAddressWithoutPostcode(JsonNode addressNode) {
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
