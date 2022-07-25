package uk.gov.hmcts.reform.pip.data.management.service.schemavalidation;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.zonky.test.db.AutoConfigureEmbeddedDatabase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
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
class CopDailyCauseListTest {

    @Autowired
    ValidationService validationService;

    private static final String COP_DAILY_LIST_VALID_JSON = "mocks/cop-daily-cause-list/copDailyCauseList.json";
    private static final String COP_DAILY_LIST_INVALID_MESSAGE = "COP daily list marked as valid";

    private static final String COURT_LIST_SCHEMA = "courtLists";
    private static final String VENUE_SCHEMA = "venue";
    private static final String VENUE_CONTACT_SCHEMA = "venueContact";
    private static final String COURT_HOUSE_SCHEMA = "courtHouse";
    private static final String COURT_HOUSE_CONTACT_SCHEMA = "courtHouseContact";
    private static final String COURT_ROOM_SCHEMA = "courtRoom";
    private static final String SESSION_SCHEMA = "session";
    private static final String SITTINGS_SCHEMA = "sittings";
    private static final String HEARING_SCHEMA = "hearing";
    private static final String CASE_SCHEMA  = "case";

    private JsonNode getJsonNode(String json) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(json, JsonNode.class);
    }

    @Test
    void testValidateWithErrorsWhenDocumentMissingInCopDailyCauseList() throws IOException {
        try (InputStream jsonInput = this.getClass().getClassLoader()
            .getResourceAsStream(COP_DAILY_LIST_VALID_JSON)) {
            String text = new String(jsonInput.readAllBytes(), StandardCharsets.UTF_8);

            JsonNode node = getJsonNode(text);
            ((ObjectNode) node).remove("document");

            assertThrows(PayloadValidationException.class, () ->
                             validationService.validateBody(node.toString(), ListType.COP_DAILY_CAUSE_LIST),
                         COP_DAILY_LIST_INVALID_MESSAGE);
        }
    }

    @Test
    void testValidateWithErrorsWhenPublicationDateMissingInCopDailyCauseList() throws IOException {
        try (InputStream jsonInput = this.getClass().getClassLoader()
            .getResourceAsStream(COP_DAILY_LIST_VALID_JSON)) {
            String text = new String(jsonInput.readAllBytes(), StandardCharsets.UTF_8);

            ObjectMapper mapper = new ObjectMapper();
            JsonNode node = mapper.readValue(text, JsonNode.class);
            ((ObjectNode) node.get("document")).remove("publicationDate");

            assertThrows(PayloadValidationException.class, () ->
                             validationService.validateBody(node.toString(), ListType.COP_DAILY_CAUSE_LIST),
                         COP_DAILY_LIST_INVALID_MESSAGE);
        }
    }

    @Test
    void testValidateWithErrorsWhenCourtListMissingInCopDailyCauseList() throws IOException {
        try (InputStream jsonInput = this.getClass().getClassLoader()
            .getResourceAsStream(COP_DAILY_LIST_VALID_JSON)) {
            String text = new String(jsonInput.readAllBytes(), StandardCharsets.UTF_8);

            JsonNode node = getJsonNode(text);
            ((ObjectNode) node).remove(COURT_LIST_SCHEMA);

            assertThrows(PayloadValidationException.class, () ->
                             validationService.validateBody(node.toString(), ListType.COP_DAILY_CAUSE_LIST),
                         COP_DAILY_LIST_INVALID_MESSAGE);
        }
    }

    @Test
    void testValidateWithErrorsWhenVenueMissingInCopDailyCauseList() throws IOException {
        try (InputStream jsonInput = this.getClass().getClassLoader()
            .getResourceAsStream(COP_DAILY_LIST_VALID_JSON)) {
            String text = new String(jsonInput.readAllBytes(), StandardCharsets.UTF_8);

            JsonNode node = getJsonNode(text);
            ((ObjectNode) node).remove(VENUE_SCHEMA);

            assertThrows(PayloadValidationException.class, () ->
                             validationService.validateBody(node.toString(), ListType.COP_DAILY_CAUSE_LIST),
                         COP_DAILY_LIST_INVALID_MESSAGE);
        }
    }

    @Test
    void testValidateWithErrorsWhenVenueNameMissingInCopDailyCauseList() throws IOException {
        try (InputStream jsonInput = this.getClass().getClassLoader()
            .getResourceAsStream(COP_DAILY_LIST_VALID_JSON)) {
            String text = new String(jsonInput.readAllBytes(), StandardCharsets.UTF_8);

            ObjectMapper mapper = new ObjectMapper();
            JsonNode node = mapper.readValue(text, JsonNode.class);
            ((ObjectNode) node.get(VENUE_SCHEMA)).remove("venueName");

            assertThrows(PayloadValidationException.class, () ->
                             validationService.validateBody(node.toString(), ListType.COP_DAILY_CAUSE_LIST),
                         COP_DAILY_LIST_INVALID_MESSAGE);
        }
    }

    @Test
    void testValidateWithErrorsWhenVenueContactMissingInCopDailyCauseList() throws IOException {
        try (InputStream jsonInput = this.getClass().getClassLoader()
            .getResourceAsStream(COP_DAILY_LIST_VALID_JSON)) {
            String text = new String(jsonInput.readAllBytes(), StandardCharsets.UTF_8);

            ObjectMapper mapper = new ObjectMapper();
            JsonNode node = mapper.readValue(text, JsonNode.class);
            ((ObjectNode) node.get(VENUE_SCHEMA)).remove(VENUE_CONTACT_SCHEMA);

            assertThrows(PayloadValidationException.class, () ->
                             validationService.validateBody(node.toString(), ListType.COP_DAILY_CAUSE_LIST),
                         COP_DAILY_LIST_INVALID_MESSAGE);
        }
    }

    @Test
    void testValidateWithErrorsWhenVenueTelephoneMissingInCopDailyCauseList() throws IOException {
        try (InputStream jsonInput = this.getClass().getClassLoader()
            .getResourceAsStream(COP_DAILY_LIST_VALID_JSON)) {
            String text = new String(jsonInput.readAllBytes(), StandardCharsets.UTF_8);

            ObjectMapper mapper = new ObjectMapper();
            JsonNode node = mapper.readValue(text, JsonNode.class);
            ((ObjectNode) node.get(VENUE_SCHEMA).get(VENUE_CONTACT_SCHEMA)).remove("venueTelephone");

            assertThrows(PayloadValidationException.class, () ->
                             validationService.validateBody(node.toString(), ListType.COP_DAILY_CAUSE_LIST),
                         COP_DAILY_LIST_INVALID_MESSAGE);
        }
    }

    @Test
    void testValidateWithErrorsWhenVenueEmailMissingInCopDailyCauseList() throws IOException {
        try (InputStream jsonInput = this.getClass().getClassLoader()
            .getResourceAsStream(COP_DAILY_LIST_VALID_JSON)) {
            String text = new String(jsonInput.readAllBytes(), StandardCharsets.UTF_8);

            ObjectMapper mapper = new ObjectMapper();
            JsonNode node = mapper.readValue(text, JsonNode.class);
            ((ObjectNode) node.get(VENUE_SCHEMA).get(VENUE_CONTACT_SCHEMA)).remove("venueEmail");

            assertThrows(PayloadValidationException.class, () ->
                             validationService.validateBody(node.toString(), ListType.COP_DAILY_CAUSE_LIST),
                         COP_DAILY_LIST_INVALID_MESSAGE);
        }
    }

    @Test
    void testValidateWithErrorsWhenCourtHouseMissingInCopDailyCauseList() throws IOException {
        try (InputStream jsonInput = this.getClass().getClassLoader()
            .getResourceAsStream(COP_DAILY_LIST_VALID_JSON)) {
            String text = new String(jsonInput.readAllBytes(), StandardCharsets.UTF_8);

            ObjectMapper mapper = new ObjectMapper();
            JsonNode node = mapper.readValue(text, JsonNode.class);
            ((ObjectNode) node.get(COURT_LIST_SCHEMA).get(0)).remove(COURT_HOUSE_SCHEMA);

            assertThrows(PayloadValidationException.class, () ->
                             validationService.validateBody(node.toString(), ListType.COP_DAILY_CAUSE_LIST),
                         COP_DAILY_LIST_INVALID_MESSAGE);
        }
    }

    @Test
    void testValidateWithErrorsWhenCourtHouseNameMissingInCopDailyCauseList() throws IOException {
        try (InputStream jsonInput = this.getClass().getClassLoader()
            .getResourceAsStream(COP_DAILY_LIST_VALID_JSON)) {
            String text = new String(jsonInput.readAllBytes(), StandardCharsets.UTF_8);

            ObjectMapper mapper = new ObjectMapper();
            JsonNode node = mapper.readValue(text, JsonNode.class);
            ((ObjectNode) node.get(COURT_LIST_SCHEMA).get(0).get(COURT_HOUSE_SCHEMA)).remove("courtHouseName");

            assertThrows(PayloadValidationException.class, () ->
                             validationService.validateBody(node.toString(), ListType.COP_DAILY_CAUSE_LIST),
                         COP_DAILY_LIST_INVALID_MESSAGE);
        }
    }

    @Test
    void testValidateWithErrorsWhenCourtHouseContactMissingInCopDailyCauseList() throws IOException {
        try (InputStream jsonInput = this.getClass().getClassLoader()
            .getResourceAsStream(COP_DAILY_LIST_VALID_JSON)) {
            String text = new String(jsonInput.readAllBytes(), StandardCharsets.UTF_8);

            ObjectMapper mapper = new ObjectMapper();
            JsonNode node = mapper.readValue(text, JsonNode.class);
            ((ObjectNode) node.get(COURT_LIST_SCHEMA).get(0).get(COURT_HOUSE_SCHEMA))
                .remove(COURT_HOUSE_CONTACT_SCHEMA);

            assertThrows(PayloadValidationException.class, () ->
                             validationService.validateBody(node.toString(), ListType.COP_DAILY_CAUSE_LIST),
                         COP_DAILY_LIST_INVALID_MESSAGE);
        }
    }

    @Test
    void testValidateWithErrorsWhenCourtHouseTelephoneMissingInCopDailyCauseList() throws IOException {
        try (InputStream jsonInput = this.getClass().getClassLoader()
            .getResourceAsStream(COP_DAILY_LIST_VALID_JSON)) {
            String text = new String(jsonInput.readAllBytes(), StandardCharsets.UTF_8);

            ObjectMapper mapper = new ObjectMapper();
            JsonNode node = mapper.readValue(text, JsonNode.class);
            ((ObjectNode) node.get(COURT_LIST_SCHEMA).get(0).get(COURT_HOUSE_SCHEMA).get(COURT_HOUSE_CONTACT_SCHEMA))
                .remove("venueTelephone");

            assertThrows(PayloadValidationException.class, () ->
                             validationService.validateBody(node.toString(), ListType.COP_DAILY_CAUSE_LIST),
                         COP_DAILY_LIST_INVALID_MESSAGE);
        }
    }

    @Test
    void testValidateWithErrorsWhenCourtHouseEmailMissingInCopDailyCauseList() throws IOException {
        try (InputStream jsonInput = this.getClass().getClassLoader()
            .getResourceAsStream(COP_DAILY_LIST_VALID_JSON)) {
            String text = new String(jsonInput.readAllBytes(), StandardCharsets.UTF_8);

            ObjectMapper mapper = new ObjectMapper();
            JsonNode node = mapper.readValue(text, JsonNode.class);
            ((ObjectNode) node.get(COURT_LIST_SCHEMA).get(0).get(COURT_HOUSE_SCHEMA).get(COURT_HOUSE_CONTACT_SCHEMA))
                .remove("venueEmail");

            assertThrows(PayloadValidationException.class, () ->
                             validationService.validateBody(node.toString(), ListType.COP_DAILY_CAUSE_LIST),
                         COP_DAILY_LIST_INVALID_MESSAGE);
        }
    }

    @Test
    void testValidateWithErrorsWhenCourtRoomMissingInCopDailyCauseList() throws IOException {
        try (InputStream jsonInput = this.getClass().getClassLoader()
            .getResourceAsStream(COP_DAILY_LIST_VALID_JSON)) {
            String text = new String(jsonInput.readAllBytes(), StandardCharsets.UTF_8);

            ObjectMapper mapper = new ObjectMapper();
            JsonNode node = mapper.readValue(text, JsonNode.class);
            ((ObjectNode) node.get(COURT_LIST_SCHEMA).get(0)
                .get(COURT_HOUSE_SCHEMA)).remove(COURT_ROOM_SCHEMA);

            assertThrows(PayloadValidationException.class, () ->
                             validationService.validateBody(node.toString(), ListType.COP_DAILY_CAUSE_LIST),
                         COP_DAILY_LIST_INVALID_MESSAGE);
        }
    }

    @Test
    void testValidateWithErrorsWhenSessionMissingInCopDailyCauseList() throws IOException {
        try (InputStream jsonInput = this.getClass().getClassLoader()
            .getResourceAsStream(COP_DAILY_LIST_VALID_JSON)) {
            String text = new String(jsonInput.readAllBytes(), StandardCharsets.UTF_8);

            ObjectMapper mapper = new ObjectMapper();
            JsonNode node = mapper.readValue(text, JsonNode.class);
            ((ObjectNode) node.get(COURT_LIST_SCHEMA).get(0)
                .get(COURT_HOUSE_SCHEMA).get(COURT_ROOM_SCHEMA).get(0)).remove(SESSION_SCHEMA);

            assertThrows(PayloadValidationException.class, () ->
                             validationService.validateBody(node.toString(), ListType.COP_DAILY_CAUSE_LIST),
                         COP_DAILY_LIST_INVALID_MESSAGE);
        }
    }

    @Test
    void testValidateWithErrorsWhenSittingsMissingInCopDailyCauseList() throws IOException {
        try (InputStream jsonInput = this.getClass().getClassLoader()
            .getResourceAsStream(COP_DAILY_LIST_VALID_JSON)) {
            String text = new String(jsonInput.readAllBytes(), StandardCharsets.UTF_8);

            ObjectMapper mapper = new ObjectMapper();
            JsonNode node = mapper.readValue(text, JsonNode.class);
            ((ObjectNode) node.get(COURT_LIST_SCHEMA).get(0)
                .get(COURT_HOUSE_SCHEMA).get(COURT_ROOM_SCHEMA).get(0)
                .get(SESSION_SCHEMA).get(0)).remove(SITTINGS_SCHEMA);

            assertThrows(PayloadValidationException.class, () ->
                             validationService.validateBody(node.toString(), ListType.COP_DAILY_CAUSE_LIST),
                         COP_DAILY_LIST_INVALID_MESSAGE);
        }
    }

    @Test
    void testValidateWithErrorsWhenSittingStartMissingInCopDailyCauseList() throws IOException {
        try (InputStream jsonInput = this.getClass().getClassLoader()
            .getResourceAsStream(COP_DAILY_LIST_VALID_JSON)) {
            String text = new String(jsonInput.readAllBytes(), StandardCharsets.UTF_8);

            ObjectMapper mapper = new ObjectMapper();
            JsonNode node = mapper.readValue(text, JsonNode.class);
            ((ObjectNode) node.get(COURT_LIST_SCHEMA).get(0)
                .get(COURT_HOUSE_SCHEMA).get(COURT_ROOM_SCHEMA).get(0)
                .get(SESSION_SCHEMA).get(0).get(SITTINGS_SCHEMA).get(0)).remove("sittingStart");

            assertThrows(PayloadValidationException.class, () ->
                             validationService.validateBody(node.toString(), ListType.COP_DAILY_CAUSE_LIST),
                         COP_DAILY_LIST_INVALID_MESSAGE);
        }
    }

    @Test
    void testValidateWithErrorsWhenSittingEndMissingInCopDailyCauseList() throws IOException {
        try (InputStream jsonInput = this.getClass().getClassLoader()
            .getResourceAsStream(COP_DAILY_LIST_VALID_JSON)) {
            String text = new String(jsonInput.readAllBytes(), StandardCharsets.UTF_8);

            ObjectMapper mapper = new ObjectMapper();
            JsonNode node = mapper.readValue(text, JsonNode.class);
            ((ObjectNode) node.get(COURT_LIST_SCHEMA).get(0)
                .get(COURT_HOUSE_SCHEMA).get(COURT_ROOM_SCHEMA).get(0)
                .get(SESSION_SCHEMA).get(0).get(SITTINGS_SCHEMA).get(0)).remove("sittingEnd");

            assertThrows(PayloadValidationException.class, () ->
                             validationService.validateBody(node.toString(), ListType.COP_DAILY_CAUSE_LIST),
                         COP_DAILY_LIST_INVALID_MESSAGE);
        }
    }

    @Test
    void testValidateWithErrorsWhenHearingMissingInCopDailyCauseList() throws IOException {
        try (InputStream jsonInput = this.getClass().getClassLoader()
            .getResourceAsStream(COP_DAILY_LIST_VALID_JSON)) {
            String text = new String(jsonInput.readAllBytes(), StandardCharsets.UTF_8);

            ObjectMapper mapper = new ObjectMapper();
            JsonNode node = mapper.readValue(text, JsonNode.class);
            ((ObjectNode) node.get(COURT_LIST_SCHEMA).get(0)
                .get(COURT_HOUSE_SCHEMA).get(COURT_ROOM_SCHEMA).get(0)
                .get(SESSION_SCHEMA).get(0).get(SITTINGS_SCHEMA).get(0)).remove(HEARING_SCHEMA);

            assertThrows(PayloadValidationException.class, () ->
                             validationService.validateBody(node.toString(), ListType.COP_DAILY_CAUSE_LIST),
                         COP_DAILY_LIST_INVALID_MESSAGE);
        }
    }

    @Test
    void testValidateWithErrorsWhenCaseMissingInCopDailyCauseList() throws IOException {
        try (InputStream jsonInput = this.getClass().getClassLoader()
            .getResourceAsStream(COP_DAILY_LIST_VALID_JSON)) {
            String text = new String(jsonInput.readAllBytes(), StandardCharsets.UTF_8);

            ObjectMapper mapper = new ObjectMapper();
            JsonNode node = mapper.readValue(text, JsonNode.class);
            ((ObjectNode) node.get(COURT_LIST_SCHEMA).get(0)
                .get(COURT_HOUSE_SCHEMA).get(COURT_ROOM_SCHEMA).get(0)
                .get(SESSION_SCHEMA).get(0).get(SITTINGS_SCHEMA).get(0)
                .get(HEARING_SCHEMA).get(0)).remove(CASE_SCHEMA);

            assertThrows(PayloadValidationException.class, () ->
                             validationService.validateBody(node.toString(), ListType.COP_DAILY_CAUSE_LIST),
                         COP_DAILY_LIST_INVALID_MESSAGE);
        }
    }

    @Test
    void testValidateWithErrorsWhenCaseNumberMissingInCopDailyCauseList() throws IOException {
        try (InputStream jsonInput = this.getClass().getClassLoader()
            .getResourceAsStream(COP_DAILY_LIST_VALID_JSON)) {
            String text = new String(jsonInput.readAllBytes(), StandardCharsets.UTF_8);

            ObjectMapper mapper = new ObjectMapper();
            JsonNode node = mapper.readValue(text, JsonNode.class);
            ((ObjectNode) node.get(COURT_LIST_SCHEMA).get(0)
                .get(COURT_HOUSE_SCHEMA).get(COURT_ROOM_SCHEMA).get(0)
                .get(SESSION_SCHEMA).get(0).get(SITTINGS_SCHEMA).get(0)
                .get(HEARING_SCHEMA).get(0).get(CASE_SCHEMA).get(0)).remove("caseNumber");

            assertThrows(PayloadValidationException.class, () ->
                             validationService.validateBody(node.toString(), ListType.COP_DAILY_CAUSE_LIST),
                         COP_DAILY_LIST_INVALID_MESSAGE);
        }
    }

}
