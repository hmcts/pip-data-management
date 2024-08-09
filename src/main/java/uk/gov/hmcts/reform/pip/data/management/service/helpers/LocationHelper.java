package uk.gov.hmcts.reform.pip.data.management.service.helpers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

@Slf4j
public final class LocationHelper {
    private static final String VENUE = "venue";
    private static final String VENUE_ADDRESS = "venueAddress";
    private static final String LINE = "line";
    private static final String TOWN = "town";
    private static final String COUNTY = "county";
    private static final String POSTCODE = "postCode";
    private static final String COURT_HOUSE = "courtHouse";
    private static final String COURT_ROOM_NAME = "courtRoomName";

    private LocationHelper() {
        throw new UnsupportedOperationException();
    }

    public static List<String> formatVenueAddress(JsonNode artefact) {
        List<String> address = addAddressLines(artefact);

        if (!GeneralHelper.findAndReturnNodeText(artefact.get(VENUE).get(VENUE_ADDRESS), POSTCODE).isEmpty()) {
            address.add(artefact.get(VENUE).get(VENUE_ADDRESS).get(POSTCODE).asText());
        }
        return address;
    }

    public static List<String> formatFullVenueAddress(JsonNode artefact) {
        List<String> address = addAddressLines(artefact);

        if (!GeneralHelper.findAndReturnNodeText(artefact.get(VENUE).get(VENUE_ADDRESS), TOWN).isEmpty()) {
            address.add(artefact.get(VENUE).get(VENUE_ADDRESS).get(TOWN).asText());
        }

        if (!GeneralHelper.findAndReturnNodeText(artefact.get(VENUE).get(VENUE_ADDRESS), COUNTY).isEmpty()) {
            address.add(artefact.get(VENUE).get(VENUE_ADDRESS).get(COUNTY).asText());
        }

        if (!GeneralHelper.findAndReturnNodeText(artefact.get(VENUE).get(VENUE_ADDRESS), POSTCODE).isEmpty()) {
            address.add(artefact.get(VENUE).get(VENUE_ADDRESS).get(POSTCODE).asText());
        }
        return address;
    }

    public static void formatCourtAddress(JsonNode artefact, String delimiter, boolean addCourtHouseName) {
        artefact.get("courtLists").forEach(courtList -> {
            JsonNode courtHouse = courtList.get(COURT_HOUSE);
            List<String> courtAddress = new ArrayList<>();
            if (addCourtHouseName && courtHouse.has("courtHouseName")) {
                courtAddress.add(courtHouse.get("courtHouseName").asText());
            }

            if (courtHouse.has("courtHouseAddress")) {
                JsonNode courtHouseAddress = courtHouse.get("courtHouseAddress");
                if (courtHouseAddress.has("line")) {
                    courtHouseAddress.get("line").forEach(line -> courtAddress.add(line.asText()));
                }
                courtAddress.add(GeneralHelper.findAndReturnNodeText(courtHouseAddress, TOWN));
                courtAddress.add(GeneralHelper.findAndReturnNodeText(courtHouseAddress, COUNTY));
                courtAddress.add(GeneralHelper.findAndReturnNodeText(courtHouseAddress, POSTCODE));
            }

            ((ObjectNode)courtHouse).put("formattedCourtHouseAddress",
                                         GeneralHelper.convertToDelimitedString(courtAddress, delimiter));
        });
    }

    private static List<String> addAddressLines(JsonNode artefact) {
        List<String> addressLines = new ArrayList<>();
        if (artefact.get(VENUE).has(VENUE_ADDRESS)) {
            JsonNode arrayNode = artefact.get(VENUE).get(VENUE_ADDRESS).get(LINE);
            for (JsonNode jsonNode : arrayNode) {
                if (!jsonNode.asText().isEmpty()) {
                    addressLines.add(jsonNode.asText());
                }
            }
        }
        return addressLines;
    }

    public static void formatRegionName(ObjectNode artefact) {
        try {
            artefact.put("regionName", artefact.get("locationDetails").get("region").get("name").asText());
        } catch (Exception e) {
            artefact.put("regionName", "");
        }
    }

    public static void formatRegionalJoh(ObjectNode artefact) {
        StringBuilder formattedJoh = new StringBuilder();
        try {
            artefact.get("locationDetails").get("region").get("regionalJOH").forEach(joh -> {
                if (formattedJoh.length() != 0) {
                    formattedJoh.append(", ");
                }
                formattedJoh.append(GeneralHelper.findAndReturnNodeText(joh, "johKnownAs"));
            });

            artefact.put("regionalJoh", formattedJoh.toString());
        } catch (Exception e) {
            artefact.put("regionalJoh", "");
        }
    }

    public static void formattedCourtRoomName(JsonNode courtRoom, JsonNode session, StringBuilder formattedJudiciary) {
        if (courtRoom.has(COURT_ROOM_NAME)) {
            if (StringUtils.isBlank(formattedJudiciary.toString())) {
                formattedJudiciary.append(courtRoom.get(COURT_ROOM_NAME).asText());
            } else {
                formattedJudiciary.insert(0, courtRoom.get(COURT_ROOM_NAME).asText() + ": ");
            }
        }

        ((ObjectNode)session).put("formattedSessionCourtRoom",
                                  GeneralHelper.trimAnyCharacterFromStringEnd(formattedJudiciary.toString()));
    }
}
