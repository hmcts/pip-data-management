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
import uk.gov.hmcts.reform.pip.data.management.config.AzureBlobConfigurationTestConfiguration;
import uk.gov.hmcts.reform.pip.data.management.errorhandling.exceptions.PayloadValidationException;
import uk.gov.hmcts.reform.pip.data.management.models.publication.ListType;
import uk.gov.hmcts.reform.pip.data.management.service.ValidationService;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest(classes = {Application.class, AzureBlobConfigurationTestConfiguration.class})
@ActiveProfiles(profiles = "test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@AutoConfigureEmbeddedDatabase(type = AutoConfigureEmbeddedDatabase.DatabaseType.POSTGRES)
class PrimaryHealthListTest {

    @Autowired
    ValidationService validationService;

    private static final String PRIMARY_HEALTH_LIST_VALID_JSON =
        "mocks/primary-health-list/primaryHealthList.json";
    private static final String PRIMARY_HEALTH_LIST_INVALID_MESSAGE =
        "Invalid primary health list marked as valid";

    private static final String COURT_LIST_SCHEMA = "courtLists";
    private static final String COURT_HOUSE_SCHEMA = "courtHouse";
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
    void testValidateWithErrorsWhenDocumentMissingInPrimaryHealthList() throws IOException {
        try (InputStream jsonInput = this.getClass().getClassLoader()
            .getResourceAsStream(PRIMARY_HEALTH_LIST_VALID_JSON)) {
            String text = new String(jsonInput.readAllBytes(), StandardCharsets.UTF_8);

            JsonNode node = getJsonNode(text);
            ((ObjectNode) node).remove("document");

            assertThrows(PayloadValidationException.class, () ->
                             validationService.validateBody(node.toString(),
                                                            ListType.PRIMARY_HEALTH_LIST),
                         PRIMARY_HEALTH_LIST_INVALID_MESSAGE
            );
        }
    }

    @Test
    void testValidateWithErrorsWhenVenueMissingInPrimaryHealthList() throws IOException {
        try (InputStream jsonInput = this.getClass().getClassLoader()
            .getResourceAsStream(PRIMARY_HEALTH_LIST_VALID_JSON)) {
            String text = new String(jsonInput.readAllBytes(), StandardCharsets.UTF_8);

            JsonNode node = getJsonNode(text);
            ((ObjectNode) node).remove("venue");

            assertThrows(PayloadValidationException.class, () ->
                             validationService.validateBody(node.toString(),
                                                            ListType.PRIMARY_HEALTH_LIST),
                         PRIMARY_HEALTH_LIST_INVALID_MESSAGE
            );
        }
    }

    @Test
    void testValidateWithErrorsWhenVenueContactMissingInPrimaryHealthList() throws IOException {
        try (InputStream jsonInput = this.getClass().getClassLoader()
            .getResourceAsStream(PRIMARY_HEALTH_LIST_VALID_JSON)) {
            String text = new String(jsonInput.readAllBytes(), StandardCharsets.UTF_8);

            JsonNode node = getJsonNode(text);
            ((ObjectNode) node.get("venue")).remove("venueContact");

            assertThrows(PayloadValidationException.class, () ->
                             validationService.validateBody(node.toString(),
                                                            ListType.PRIMARY_HEALTH_LIST),
                         PRIMARY_HEALTH_LIST_INVALID_MESSAGE
            );
        }
    }

    @Test
    void testValidateWithErrorsWhenCourtListMissingInPrimaryHealthList() throws IOException {
        try (InputStream jsonInput = this.getClass().getClassLoader()
            .getResourceAsStream(PRIMARY_HEALTH_LIST_VALID_JSON)) {
            String text = new String(jsonInput.readAllBytes(), StandardCharsets.UTF_8);

            JsonNode node = getJsonNode(text);
            ((ObjectNode) node).remove(COURT_LIST_SCHEMA);

            assertThrows(PayloadValidationException.class, () ->
                             validationService.validateBody(node.toString(),
                                                            ListType.PRIMARY_HEALTH_LIST),
                         PRIMARY_HEALTH_LIST_INVALID_MESSAGE
            );
        }
    }

    @Test
    void testValidateWithErrorsWhenPublicationDateMissingInPrimaryHealthList() throws IOException {
        try (InputStream jsonInput = this.getClass().getClassLoader()
            .getResourceAsStream(PRIMARY_HEALTH_LIST_VALID_JSON)) {
            String text = new String(jsonInput.readAllBytes(), StandardCharsets.UTF_8);

            ObjectMapper mapper = new ObjectMapper();
            JsonNode node = mapper.readValue(text, JsonNode.class);
            ((ObjectNode) node.get("document")).remove("publicationDate");

            assertThrows(PayloadValidationException.class, () ->
                             validationService.validateBody(node.toString(),
                                                            ListType.PRIMARY_HEALTH_LIST),
                         PRIMARY_HEALTH_LIST_INVALID_MESSAGE
            );
        }
    }

    @Test
    void testValidateWithErrorsWhenCourtHouseMissingInPrimaryHealthList() throws IOException {
        try (InputStream jsonInput = this.getClass().getClassLoader()
            .getResourceAsStream(PRIMARY_HEALTH_LIST_VALID_JSON)) {
            String text = new String(jsonInput.readAllBytes(), StandardCharsets.UTF_8);

            ObjectMapper mapper = new ObjectMapper();
            JsonNode node = mapper.readValue(text, JsonNode.class);
            ((ObjectNode) node.get(COURT_LIST_SCHEMA).get(0)).remove(COURT_HOUSE_SCHEMA);

            assertThrows(PayloadValidationException.class, () ->
                             validationService.validateBody(node.toString(),
                                                            ListType.PRIMARY_HEALTH_LIST),
                         PRIMARY_HEALTH_LIST_INVALID_MESSAGE
            );
        }
    }

    @Test
    void testValidateWithErrorsWhenCourtHouseNameMissingInPrimaryHealthList() throws IOException {
        try (InputStream jsonInput = this.getClass().getClassLoader()
            .getResourceAsStream(PRIMARY_HEALTH_LIST_VALID_JSON)) {
            String text = new String(jsonInput.readAllBytes(), StandardCharsets.UTF_8);

            ObjectMapper mapper = new ObjectMapper();
            JsonNode node = mapper.readValue(text, JsonNode.class);
            ((ObjectNode) node.get(COURT_LIST_SCHEMA).get(0).get(COURT_HOUSE_SCHEMA)).remove("courtHouseName");

            assertThrows(PayloadValidationException.class, () ->
                             validationService.validateBody(node.toString(),
                                                            ListType.PRIMARY_HEALTH_LIST),
                         PRIMARY_HEALTH_LIST_INVALID_MESSAGE
            );
        }
    }

    @Test
    void testValidateWithErrorsWhenCourtRoomMissingInPrimaryHealthList() throws IOException {
        try (InputStream jsonInput = this.getClass().getClassLoader()
            .getResourceAsStream(PRIMARY_HEALTH_LIST_VALID_JSON)) {
            String text = new String(jsonInput.readAllBytes(), StandardCharsets.UTF_8);

            ObjectMapper mapper = new ObjectMapper();
            JsonNode node = mapper.readValue(text, JsonNode.class);
            ((ObjectNode) node.get(COURT_LIST_SCHEMA).get(0)
                .get(COURT_HOUSE_SCHEMA)).remove(COURT_ROOM_SCHEMA);

            assertThrows(PayloadValidationException.class, () ->
                             validationService.validateBody(node.toString(),
                                                            ListType.PRIMARY_HEALTH_LIST),
                         PRIMARY_HEALTH_LIST_INVALID_MESSAGE
            );
        }
    }

    @Test
    void testValidateWithErrorsWhenSessionMissingInPrimaryHealthList() throws IOException {
        try (InputStream jsonInput = this.getClass().getClassLoader()
            .getResourceAsStream(PRIMARY_HEALTH_LIST_VALID_JSON)) {
            String text = new String(jsonInput.readAllBytes(), StandardCharsets.UTF_8);

            ObjectMapper mapper = new ObjectMapper();
            JsonNode node = mapper.readValue(text, JsonNode.class);
            ((ObjectNode) node.get(COURT_LIST_SCHEMA).get(0)
                .get(COURT_HOUSE_SCHEMA).get(COURT_ROOM_SCHEMA).get(0)).remove(SESSION_SCHEMA);

            assertThrows(PayloadValidationException.class, () ->
                             validationService.validateBody(node.toString(),
                                                            ListType.PRIMARY_HEALTH_LIST),
                         PRIMARY_HEALTH_LIST_INVALID_MESSAGE
            );
        }
    }

    @Test
    void testValidateWithErrorsWhenSessionStartTimeMissingInPrimaryHealthList() throws IOException {
        try (InputStream jsonInput = this.getClass().getClassLoader()
            .getResourceAsStream(PRIMARY_HEALTH_LIST_VALID_JSON)) {
            String text = new String(jsonInput.readAllBytes(), StandardCharsets.UTF_8);

            ObjectMapper mapper = new ObjectMapper();
            JsonNode node = mapper.readValue(text, JsonNode.class);
            ((ObjectNode) node.get(COURT_LIST_SCHEMA).get(0)
                .get(COURT_HOUSE_SCHEMA).get(COURT_ROOM_SCHEMA).get(0)
                .get(SESSION_SCHEMA).get(0)).remove("sessionStartTime");

            assertThrows(PayloadValidationException.class, () ->
                             validationService.validateBody(node.toString(),
                                                            ListType.PRIMARY_HEALTH_LIST),
                         PRIMARY_HEALTH_LIST_INVALID_MESSAGE
            );
        }
    }

    @Test
    void testValidateWithErrorsWhenSittingsMissingInPrimaryHealthList() throws IOException {
        try (InputStream jsonInput = this.getClass().getClassLoader()
            .getResourceAsStream(PRIMARY_HEALTH_LIST_VALID_JSON)) {
            String text = new String(jsonInput.readAllBytes(), StandardCharsets.UTF_8);

            ObjectMapper mapper = new ObjectMapper();
            JsonNode node = mapper.readValue(text, JsonNode.class);
            ((ObjectNode) node.get(COURT_LIST_SCHEMA).get(0)
                .get(COURT_HOUSE_SCHEMA).get(COURT_ROOM_SCHEMA).get(0)
                .get(SESSION_SCHEMA).get(0)).remove(SITTINGS_SCHEMA);

            assertThrows(PayloadValidationException.class, () ->
                             validationService.validateBody(node.toString(),
                                                            ListType.PRIMARY_HEALTH_LIST),
                         PRIMARY_HEALTH_LIST_INVALID_MESSAGE
            );
        }
    }

    @Test
    void testValidateWithErrorsWhenSittingStartMissingInPrimaryHealthList() throws IOException {
        try (InputStream jsonInput = this.getClass().getClassLoader()
            .getResourceAsStream(PRIMARY_HEALTH_LIST_VALID_JSON)) {
            String text = new String(jsonInput.readAllBytes(), StandardCharsets.UTF_8);

            ObjectMapper mapper = new ObjectMapper();
            JsonNode node = mapper.readValue(text, JsonNode.class);
            ((ObjectNode) node.get(COURT_LIST_SCHEMA).get(0)
                .get(COURT_HOUSE_SCHEMA).get(COURT_ROOM_SCHEMA).get(0)
                .get(SESSION_SCHEMA).get(0).get(SITTINGS_SCHEMA).get(0)).remove("sittingStart");

            assertThrows(PayloadValidationException.class, () ->
                             validationService.validateBody(node.toString(),
                                                            ListType.PRIMARY_HEALTH_LIST),
                         PRIMARY_HEALTH_LIST_INVALID_MESSAGE
            );
        }
    }

    @Test
    void testValidateWithErrorsWhenSittingEndMissingInPrimaryHealthList() throws IOException {
        try (InputStream jsonInput = this.getClass().getClassLoader()
            .getResourceAsStream(PRIMARY_HEALTH_LIST_VALID_JSON)) {
            String text = new String(jsonInput.readAllBytes(), StandardCharsets.UTF_8);

            ObjectMapper mapper = new ObjectMapper();
            JsonNode node = mapper.readValue(text, JsonNode.class);
            ((ObjectNode) node.get(COURT_LIST_SCHEMA).get(0)
                .get(COURT_HOUSE_SCHEMA).get(COURT_ROOM_SCHEMA).get(0)
                .get(SESSION_SCHEMA).get(0).get(SITTINGS_SCHEMA).get(0)).remove("sittingEnd");

            assertThrows(PayloadValidationException.class, () ->
                             validationService.validateBody(node.toString(),
                                                            ListType.PRIMARY_HEALTH_LIST),
                         PRIMARY_HEALTH_LIST_INVALID_MESSAGE
            );
        }
    }

    @Test
    void testValidateWithErrorsWhenHearingMissingInPrimaryHealthList() throws IOException {
        try (InputStream jsonInput = this.getClass().getClassLoader()
            .getResourceAsStream(PRIMARY_HEALTH_LIST_VALID_JSON)) {
            String text = new String(jsonInput.readAllBytes(), StandardCharsets.UTF_8);

            ObjectMapper mapper = new ObjectMapper();
            JsonNode node = mapper.readValue(text, JsonNode.class);
            ((ObjectNode) node.get(COURT_LIST_SCHEMA).get(0)
                .get(COURT_HOUSE_SCHEMA).get(COURT_ROOM_SCHEMA).get(0)
                .get(SESSION_SCHEMA).get(0).get(SITTINGS_SCHEMA).get(0)).remove(HEARING_SCHEMA);

            assertThrows(PayloadValidationException.class, () ->
                             validationService.validateBody(node.toString(),
                                                            ListType.PRIMARY_HEALTH_LIST),
                         PRIMARY_HEALTH_LIST_INVALID_MESSAGE
            );
        }
    }

    @Test
    void testValidateWithErrorsWhenCaseMissingInPrimaryHealthList() throws IOException {
        try (InputStream jsonInput = this.getClass().getClassLoader()
            .getResourceAsStream(PRIMARY_HEALTH_LIST_VALID_JSON)) {
            String text = new String(jsonInput.readAllBytes(), StandardCharsets.UTF_8);

            ObjectMapper mapper = new ObjectMapper();
            JsonNode node = mapper.readValue(text, JsonNode.class);
            ((ObjectNode) node.get(COURT_LIST_SCHEMA).get(0)
                .get(COURT_HOUSE_SCHEMA).get(COURT_ROOM_SCHEMA).get(0)
                .get(SESSION_SCHEMA).get(0).get(SITTINGS_SCHEMA).get(0)
                .get(HEARING_SCHEMA).get(0)).remove(CASE_SCHEMA);

            assertThrows(PayloadValidationException.class, () ->
                             validationService.validateBody(node.toString(),
                                                            ListType.PRIMARY_HEALTH_LIST),
                         PRIMARY_HEALTH_LIST_INVALID_MESSAGE
            );
        }
    }

    @Test
    void testValidateWithErrorsWhenCaseNameMissingInPrimaryHealthList() throws IOException {
        try (InputStream jsonInput = this.getClass().getClassLoader()
            .getResourceAsStream(PRIMARY_HEALTH_LIST_VALID_JSON)) {
            String text = new String(jsonInput.readAllBytes(), StandardCharsets.UTF_8);

            ObjectMapper mapper = new ObjectMapper();
            JsonNode node = mapper.readValue(text, JsonNode.class);
            ((ObjectNode) node.get(COURT_LIST_SCHEMA).get(0).get(COURT_HOUSE_SCHEMA)
                .get(COURT_ROOM_SCHEMA).get(0).get(SESSION_SCHEMA).get(0)
                .get(SITTINGS_SCHEMA).get(0).get(HEARING_SCHEMA).get(0)
                .get(CASE_SCHEMA).get(0)).remove("caseName");

            assertThrows(PayloadValidationException.class, () ->
                             validationService.validateBody(node.toString(),
                                                            ListType.PRIMARY_HEALTH_LIST),
                         PRIMARY_HEALTH_LIST_INVALID_MESSAGE
            );
        }
    }

    @Test
    void testValidateWithErrorsWhenCaseNumberMissingInPrimaryHealthList() throws IOException {
        try (InputStream jsonInput = this.getClass().getClassLoader()
            .getResourceAsStream(PRIMARY_HEALTH_LIST_VALID_JSON)) {
            String text = new String(jsonInput.readAllBytes(), StandardCharsets.UTF_8);

            ObjectMapper mapper = new ObjectMapper();
            JsonNode node = mapper.readValue(text, JsonNode.class);
            ((ObjectNode) node.get(COURT_LIST_SCHEMA).get(0)
                .get(COURT_HOUSE_SCHEMA).get(COURT_ROOM_SCHEMA).get(0)
                .get(SESSION_SCHEMA).get(0).get(SITTINGS_SCHEMA).get(0)
                .get(HEARING_SCHEMA).get(0).get(CASE_SCHEMA).get(0)).remove("caseNumber");

            assertThrows(PayloadValidationException.class, () ->
                             validationService.validateBody(node.toString(),
                                                            ListType.PRIMARY_HEALTH_LIST),
                         PRIMARY_HEALTH_LIST_INVALID_MESSAGE
            );
        }
    }
}
