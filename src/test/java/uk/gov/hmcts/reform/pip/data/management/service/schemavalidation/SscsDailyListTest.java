package uk.gov.hmcts.reform.pip.data.management.service.schemavalidation;

import io.zonky.test.db.AutoConfigureEmbeddedDatabase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import uk.gov.hmcts.reform.pip.data.management.Application;
import uk.gov.hmcts.reform.pip.data.management.config.AzureBlobConfigurationTest;
import uk.gov.hmcts.reform.pip.data.management.errorhandling.exceptions.PayloadValidationException;
import uk.gov.hmcts.reform.pip.data.management.models.publication.ListType;
import uk.gov.hmcts.reform.pip.data.management.service.ValidationService;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest(classes = {Application.class, AzureBlobConfigurationTest.class})
@ActiveProfiles(profiles = "test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@AutoConfigureEmbeddedDatabase(type = AutoConfigureEmbeddedDatabase.DatabaseType.POSTGRES)

class SscsDailyListTest {

    @Autowired
    ValidationService validationService;

    private static final String SSCS_DAILY_LIST_VALID_JSON = "mocks/sscs-daily-list/sscsDailyList.json";

    private static final String SSCS_DAILY_LIST_INVALID_MESSAGE = "Invalid sscs daily list marked as valid";

    private static final String COURT_LIST_SCHEMA = "courtLists";
//    private static final String VENUE_SCHEMA = "venue";
//    private static final String VENUE_ADDRESS_SCHEMA = "venueAddress";
//    private static final String VENUE_CONTACT_SCHEMA = "venueContact";
    private static final String COURT_HOUSE_SCHEMA = "courtHouse";
    private static final String COURT_ROOM_SCHEMA = "courtRoom";
    private static final String SESSION_SCHEMA = "session";
    private static final String SITTINGS_SCHEMA = "sittings";
//    private static final String HEARING_SCHEMA = "hearing";
//    private static final String CASE_SCHEMA  = "case";

    private JsonNode getJsonNode(String json) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(json, JsonNode.class);
    }


    @Test
    void testValidateWithErrorsWhenDocumentMissingInSscsDailyList() throws IOException {
        try (InputStream jsonInput = this.getClass().getClassLoader()
            .getResourceAsStream(SSCS_DAILY_LIST_VALID_JSON)) {
            String text = new String(jsonInput.readAllBytes(), StandardCharsets.UTF_8);

            JsonNode node = getJsonNode(text);
            ((ObjectNode) node).remove("document");

            assertThrows(PayloadValidationException.class, () -> validationService.validateBody(node.toString(),
                                                                    ListType.SSCS_DAILY_LIST),
                                                                        SSCS_DAILY_LIST_INVALID_MESSAGE);
        }
    }

    @Test
    void testValidateWithErrorsWhenPublicationDateMissingInSscsDailyList() throws IOException {
        try (InputStream jsonInput = this.getClass().getClassLoader()
            .getResourceAsStream(SSCS_DAILY_LIST_VALID_JSON)) {
            String text = new String(jsonInput.readAllBytes(), StandardCharsets.UTF_8);

            JsonNode node = getJsonNode(text);
            ((ObjectNode) node.get("document")).remove("publicationDate");

            assertThrows(PayloadValidationException.class, () -> validationService.validateBody(node.toString(),
                                                                                                ListType.SSCS_DAILY_LIST),
                         SSCS_DAILY_LIST_INVALID_MESSAGE);
        }
    }

    @Test
    void testValidateWithErrorsWhenVenueMissingInSscsDailyList() throws IOException {
        try (InputStream jsonInput = this.getClass().getClassLoader()
            .getResourceAsStream(SSCS_DAILY_LIST_VALID_JSON)) {
            String text = new String(jsonInput.readAllBytes(), StandardCharsets.UTF_8);

            JsonNode node = getJsonNode(text);
            ((ObjectNode) node).remove("venue");

            assertThrows(PayloadValidationException.class, () -> validationService.validateBody(node.toString(),
                                                                                                ListType.SSCS_DAILY_LIST),
                         SSCS_DAILY_LIST_INVALID_MESSAGE);
        }
    }

    @Test
    void testValidateWithErrorsWhenVenueNameMissingInSscsDailyList() throws IOException {
        try (InputStream jsonInput = this.getClass().getClassLoader()
            .getResourceAsStream(SSCS_DAILY_LIST_VALID_JSON)) {
            String text = new String(jsonInput.readAllBytes(), StandardCharsets.UTF_8);

            JsonNode node = getJsonNode(text);
            ((ObjectNode) node.get("venue")).remove("venueName");

            assertThrows(PayloadValidationException.class, () -> validationService.validateBody(node.toString(),
                                                                                                ListType.SSCS_DAILY_LIST),
                         SSCS_DAILY_LIST_INVALID_MESSAGE);
        }
    }

    @Test
    void testValidateWithErrorsWhenCourtListsMissingInSscsDailyList() throws IOException {
        try (InputStream jsonInput = this.getClass().getClassLoader()
            .getResourceAsStream(SSCS_DAILY_LIST_VALID_JSON)) {
            String text = new String(jsonInput.readAllBytes(), StandardCharsets.UTF_8);

            JsonNode node = getJsonNode(text);
            ((ObjectNode) node).remove("courtLists");

            assertThrows(PayloadValidationException.class, () -> validationService.validateBody(node.toString(),
                                                                                                ListType.SSCS_DAILY_LIST),
                         SSCS_DAILY_LIST_INVALID_MESSAGE);
        }
    }

    @Test
    void testValidateWithErrorsWhenCourtHouseMissingInSscsDailyList() throws IOException {
        try (InputStream jsonInput = this.getClass().getClassLoader()
            .getResourceAsStream(SSCS_DAILY_LIST_VALID_JSON)) {
            String text = new String(jsonInput.readAllBytes(), StandardCharsets.UTF_8);

            ObjectMapper mapper = new ObjectMapper();
            JsonNode node = mapper.readValue(text, JsonNode.class);

            ((ObjectNode) node.get("courtLists").get(0)).remove("courtHouse");

            assertThrows(PayloadValidationException.class, () -> validationService.validateBody(node.toString(),
                                                                                                ListType.SSCS_DAILY_LIST),
                         SSCS_DAILY_LIST_INVALID_MESSAGE);
        }
    }

    @Test
    void testValidateWithErrorsWhenCourtHouseNameMissingInSscsDailyList() throws IOException {
        try (InputStream jsonInput = this.getClass().getClassLoader()
            .getResourceAsStream(SSCS_DAILY_LIST_VALID_JSON)) {
            String text = new String(jsonInput.readAllBytes(), StandardCharsets.UTF_8);

            ObjectMapper mapper = new ObjectMapper();
            JsonNode node = mapper.readValue(text, JsonNode.class);

            ((ObjectNode) node.get("courtLists").get(0).get("courtHouse")).remove("courtHouseName");

            assertThrows(PayloadValidationException.class, () -> validationService.validateBody(node.toString(),
                                                                                                ListType.SSCS_DAILY_LIST),
                         SSCS_DAILY_LIST_INVALID_MESSAGE);
        }
    }

    @Test
    void testValidateWithErrorsWhenCourtHouseContactMissingInSscsDailyList() throws IOException {
        try (InputStream jsonInput = this.getClass().getClassLoader()
            .getResourceAsStream(SSCS_DAILY_LIST_VALID_JSON)) {
            String text = new String(jsonInput.readAllBytes(), StandardCharsets.UTF_8);

            ObjectMapper mapper = new ObjectMapper();
            JsonNode node = mapper.readValue(text, JsonNode.class);

            ((ObjectNode) node.get("courtLists").get(0).get("courtHouse")).remove("courtHouseContact");

            assertThrows(PayloadValidationException.class, () -> validationService.validateBody(node.toString(),
                                                                                                ListType.SSCS_DAILY_LIST),
                         SSCS_DAILY_LIST_INVALID_MESSAGE);
        }
    }

    @Test
    void testValidateWithErrorsWhenCourtRoomMissingInSscsDailyList() throws IOException {
        try (InputStream jsonInput = this.getClass().getClassLoader()
            .getResourceAsStream(SSCS_DAILY_LIST_VALID_JSON)) {
            String text = new String(jsonInput.readAllBytes(), StandardCharsets.UTF_8);

            ObjectMapper mapper = new ObjectMapper();
            JsonNode node = mapper.readValue(text, JsonNode.class);

            ((ObjectNode) node.get("courtLists").get(0).get("courtHouse")).remove("courtRoom");

            assertThrows(PayloadValidationException.class, () -> validationService.validateBody(node.toString(),
                                                                                                ListType.SSCS_DAILY_LIST),
                         SSCS_DAILY_LIST_INVALID_MESSAGE);
        }
    }
}
