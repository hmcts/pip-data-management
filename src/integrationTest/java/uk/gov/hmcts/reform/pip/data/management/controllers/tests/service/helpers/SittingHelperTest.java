package uk.gov.hmcts.reform.pip.data.management.controllers.tests.service.helpers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.pip.data.management.service.helpers.SittingHelper;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ActiveProfiles("test")
class SittingHelperTest {

    private static ObjectMapper objectMapper = new ObjectMapper();

    private static String nodeWithJudiciary = """
        {
          "judiciary": [
            {
              "johKnownAs": "This is a known as"
            }
          ]
        }
        """;
    private static String nodeWithCrimeJudiciary = """
        {
          "judiciary": [
            {
              "johTitle": "Judge",
              "johNameSurname": "Test Name"
            }
          ]
        }
        """;
    private static String courtRoom = """
        {
          "courtRoomName": "This is a court room name"
        }
        """;

    private static String sittingWithChannel = """
         {
            "channel": [
              "Teams",
              "Attended"
            ]
         }
        """;

    private static String sittingWithoutChannel = """
         {}
        """;

    private static String session = """
         {
            "sessionChannel": [
              "VIDEO HEARING",
              ""
            ]
         }
        """;

    private JsonNode nodeWithJudiciaryJson;
    private JsonNode nodeWithCrimeJudiciaryJson;
    private JsonNode courtRoomJson;
    private JsonNode nodeWithoutJudiciaryJson;

    private JsonNode nodeWithSessionJson;

    private JsonNode nodeWithSittingChannelJson;

    private JsonNode nodeWithoutSittingChannelJson;

    private static final String DESTINATION_NODE_NAME = "This is a destination node name";
    private static final String COURT_ROOM_NAME_ERROR = "Correct court room name not shown";

    private static final String CASE_HEARING_CHANNEL = "caseHearingChannel";

    @BeforeEach
    public void setup() throws JsonProcessingException {
        nodeWithJudiciaryJson = objectMapper.readTree(nodeWithJudiciary);
        nodeWithCrimeJudiciaryJson = objectMapper.readTree(nodeWithCrimeJudiciary);
        courtRoomJson = objectMapper.readTree(courtRoom);
        nodeWithoutJudiciaryJson = objectMapper.createObjectNode();
        nodeWithSittingChannelJson = objectMapper.readTree(sittingWithChannel);
        nodeWithoutSittingChannelJson = objectMapper.readTree(sittingWithoutChannel);
        nodeWithSessionJson = objectMapper.readTree(session);
    }

    @Test
    void testManipulatedSittingWithSitting() {
        SittingHelper.manipulatedSitting(courtRoomJson, nodeWithoutJudiciaryJson,
                                         nodeWithJudiciaryJson, DESTINATION_NODE_NAME
        );

        assertEquals("This is a court room name: This is a known as",
                     nodeWithoutJudiciaryJson.get(DESTINATION_NODE_NAME).asText(),
                     COURT_ROOM_NAME_ERROR);
    }

    @Test
    void testManipulatedSittingWithSession() {
        SittingHelper.manipulatedSitting(courtRoomJson, nodeWithJudiciaryJson,
                                         nodeWithoutJudiciaryJson, DESTINATION_NODE_NAME
        );

        assertEquals("This is a court room name: This is a known as",
                     nodeWithJudiciaryJson.get(DESTINATION_NODE_NAME).asText(),
                     COURT_ROOM_NAME_ERROR);
    }

    @Test
    void testManipulatedSittingWithSittingForCrime() {
        SittingHelper.manipulatedSittingForCrime(courtRoomJson, nodeWithoutJudiciaryJson,
                                         nodeWithCrimeJudiciaryJson, DESTINATION_NODE_NAME
        );

        assertEquals("This is a court room name: Judge Test Name",
                     nodeWithoutJudiciaryJson.get(DESTINATION_NODE_NAME).asText(),
                     COURT_ROOM_NAME_ERROR);
    }

    @Test
    void testManipulatedSittingWithSessionForCrime() {
        SittingHelper.manipulatedSittingForCrime(courtRoomJson, nodeWithCrimeJudiciaryJson,
                                         nodeWithoutJudiciaryJson, DESTINATION_NODE_NAME
        );

        assertEquals("This is a court room name: Judge Test Name",
                     nodeWithCrimeJudiciaryJson.get(DESTINATION_NODE_NAME).asText(),
                     COURT_ROOM_NAME_ERROR);
    }

    @Test
    void testFindAndConcatenateHearingPlatformWithSittingChannel() {
        SittingHelper.findAndConcatenateHearingPlatform(nodeWithSittingChannelJson, nodeWithSessionJson);

        assertEquals("Teams, Attended", nodeWithSittingChannelJson.get(CASE_HEARING_CHANNEL).asText(),
                     COURT_ROOM_NAME_ERROR);
    }

    @Test
    void testFindAndConcatenateHearingPlatformWithSessionChannel() {
        SittingHelper.findAndConcatenateHearingPlatform(nodeWithoutSittingChannelJson, nodeWithSessionJson);

        assertEquals("VIDEO HEARING", nodeWithoutSittingChannelJson.get(CASE_HEARING_CHANNEL).asText(),
                     COURT_ROOM_NAME_ERROR);
    }
}

