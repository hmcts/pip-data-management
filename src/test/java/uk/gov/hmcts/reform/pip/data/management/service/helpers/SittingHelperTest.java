package uk.gov.hmcts.reform.pip.data.management.service.helpers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ActiveProfiles("test")
class SittingHelperTest {

    private static ObjectMapper objectMapper = new ObjectMapper();

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

    private JsonNode nodeWithSessionJson;

    private JsonNode nodeWithSittingChannelJson;

    private JsonNode nodeWithoutSittingChannelJson;

    private static final String COURT_ROOM_NAME_ERROR = "Correct court room name not shown";

    private static final String CASE_HEARING_CHANNEL = "caseHearingChannel";

    @BeforeEach
    public void setup() throws JsonProcessingException {
        nodeWithSittingChannelJson = objectMapper.readTree(sittingWithChannel);
        nodeWithoutSittingChannelJson = objectMapper.readTree(sittingWithoutChannel);
        nodeWithSessionJson = objectMapper.readTree(session);
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

