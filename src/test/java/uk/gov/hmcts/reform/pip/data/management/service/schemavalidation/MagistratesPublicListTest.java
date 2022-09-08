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

class MagistratesPublicListTest {
    @Autowired
    ValidationService validationService;

    private static final String MAGISTRATES_PUBLIC_LIST_VALID_JSON =
        "mocks/magistrates_public_list/magistratesPublicList.json";
    private static final String MAGISTRATES_PUBLIC_LIST_INVALID_MESSAGE =
        "Invalid crime magistrates public list marked as valid";

    private static final String COURT_LIST_SCHEMA = "courtLists";
    private static final String VENUE_SCHEMA = "venue";
    private static final String VENUE_ADDRESS_SCHEMA = "venueAddress";
    private static final String VENUE_CONTACT_SCHEMA = "venueContact";
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
    void testValidateWithErrorsWhenDocumentMissingInMagistratesPublicList() throws IOException {
        try (InputStream jsonInput = this.getClass().getClassLoader()
            .getResourceAsStream(MAGISTRATES_PUBLIC_LIST_VALID_JSON)) {
            String text = new String(jsonInput.readAllBytes(), StandardCharsets.UTF_8);

            JsonNode node = getJsonNode(text);
            ((ObjectNode) node).remove("document");

            assertThrows(PayloadValidationException.class, () ->
                             validationService.validateBody(node.toString(),
                                                            ListType.MAGISTRATES_PUBLIC_LIST),
                         MAGISTRATES_PUBLIC_LIST_INVALID_MESSAGE);
        }
    }

    @Test
    void testValidateWithErrorsWhenVenueMissingInMagistratesPublicList() throws IOException {
        try (InputStream jsonInput = this.getClass().getClassLoader()
            .getResourceAsStream(MAGISTRATES_PUBLIC_LIST_VALID_JSON)) {
            String text = new String(jsonInput.readAllBytes(), StandardCharsets.UTF_8);

            JsonNode node = getJsonNode(text);
            ((ObjectNode) node).remove(VENUE_SCHEMA);

            assertThrows(PayloadValidationException.class, () ->
                             validationService.validateBody(node.toString(),
                                                            ListType.MAGISTRATES_PUBLIC_LIST),
                         MAGISTRATES_PUBLIC_LIST_INVALID_MESSAGE);
        }
    }

    @Test
    void testValidateWithErrorsWhenCourtListMissingInMagistratesPublicList() throws IOException {
        try (InputStream jsonInput = this.getClass().getClassLoader()
            .getResourceAsStream(MAGISTRATES_PUBLIC_LIST_VALID_JSON)) {
            String text = new String(jsonInput.readAllBytes(), StandardCharsets.UTF_8);

            JsonNode node = getJsonNode(text);
            ((ObjectNode) node).remove(COURT_LIST_SCHEMA);

            assertThrows(PayloadValidationException.class, () ->
                             validationService.validateBody(node.toString(),
                                                            ListType.MAGISTRATES_PUBLIC_LIST),
                         MAGISTRATES_PUBLIC_LIST_INVALID_MESSAGE);
        }
    }

    @Test
    void testValidateWithErrorsWhenPublicationDateMissingInMagistratesPublicList() throws IOException {
        try (InputStream jsonInput = this.getClass().getClassLoader()
            .getResourceAsStream(MAGISTRATES_PUBLIC_LIST_VALID_JSON)) {
            String text = new String(jsonInput.readAllBytes(), StandardCharsets.UTF_8);

            ObjectMapper mapper = new ObjectMapper();
            JsonNode node = mapper.readValue(text, JsonNode.class);
            ((ObjectNode) node.get("document")).remove("publicationDate");

            assertThrows(PayloadValidationException.class, () ->
                             validationService.validateBody(node.toString(),
                                                            ListType.MAGISTRATES_PUBLIC_LIST),
                         MAGISTRATES_PUBLIC_LIST_INVALID_MESSAGE);
        }
    }

    @Test
    void testValidateWithErrorsWhenVenueNameMissingInMagistratesPublicList() throws IOException {
        try (InputStream jsonInput = this.getClass().getClassLoader()
            .getResourceAsStream(MAGISTRATES_PUBLIC_LIST_VALID_JSON)) {
            String text = new String(jsonInput.readAllBytes(), StandardCharsets.UTF_8);

            ObjectMapper mapper = new ObjectMapper();
            JsonNode node = mapper.readValue(text, JsonNode.class);
            ((ObjectNode) node.get(VENUE_SCHEMA)).remove("venueName");

            assertThrows(PayloadValidationException.class, () ->
                             validationService.validateBody(node.toString(),
                                                            ListType.MAGISTRATES_PUBLIC_LIST),
                         MAGISTRATES_PUBLIC_LIST_INVALID_MESSAGE);
        }
    }

    @Test
    void testValidateWithErrorsWhenVenueAddressMissingInMagistratesPublicList() throws IOException {
        try (InputStream jsonInput = this.getClass().getClassLoader()
            .getResourceAsStream(MAGISTRATES_PUBLIC_LIST_VALID_JSON)) {
            String text = new String(jsonInput.readAllBytes(), StandardCharsets.UTF_8);

            ObjectMapper mapper = new ObjectMapper();
            JsonNode node = mapper.readValue(text, JsonNode.class);
            ((ObjectNode) node.get(VENUE_SCHEMA)).remove(VENUE_ADDRESS_SCHEMA);

            assertThrows(PayloadValidationException.class, () ->
                             validationService.validateBody(node.toString(),
                                                            ListType.MAGISTRATES_PUBLIC_LIST),
                         MAGISTRATES_PUBLIC_LIST_INVALID_MESSAGE);
        }
    }

    @Test
    void testValidateWithErrorsWhenLineMissingInMagistratesPublicList() throws IOException {
        try (InputStream jsonInput = this.getClass().getClassLoader()
            .getResourceAsStream(MAGISTRATES_PUBLIC_LIST_VALID_JSON)) {
            String text = new String(jsonInput.readAllBytes(), StandardCharsets.UTF_8);

            ObjectMapper mapper = new ObjectMapper();
            JsonNode node = mapper.readValue(text, JsonNode.class);
            ((ObjectNode) node.get(VENUE_SCHEMA).get(VENUE_ADDRESS_SCHEMA)).remove("line");

            assertThrows(PayloadValidationException.class, () ->
                             validationService.validateBody(node.toString(),
                                                            ListType.MAGISTRATES_PUBLIC_LIST),
                         MAGISTRATES_PUBLIC_LIST_INVALID_MESSAGE);
        }
    }

    @Test
    void testValidateWithErrorsWhenPostCodeMissingInMagistratesPublicList() throws IOException {
        try (InputStream jsonInput = this.getClass().getClassLoader()
            .getResourceAsStream(MAGISTRATES_PUBLIC_LIST_VALID_JSON)) {
            String text = new String(jsonInput.readAllBytes(), StandardCharsets.UTF_8);

            ObjectMapper mapper = new ObjectMapper();
            JsonNode node = mapper.readValue(text, JsonNode.class);
            ((ObjectNode) node.get(VENUE_SCHEMA).get(VENUE_ADDRESS_SCHEMA)).remove("postCode");

            assertThrows(PayloadValidationException.class, () ->
                             validationService.validateBody(node.toString(),
                                                            ListType.MAGISTRATES_PUBLIC_LIST),
                         MAGISTRATES_PUBLIC_LIST_INVALID_MESSAGE);
        }
    }

    @Test
    void testValidateWithErrorsWhenVenueContactMissingInMagistratesPublicList() throws IOException {
        try (InputStream jsonInput = this.getClass().getClassLoader()
            .getResourceAsStream(MAGISTRATES_PUBLIC_LIST_VALID_JSON)) {
            String text = new String(jsonInput.readAllBytes(), StandardCharsets.UTF_8);

            ObjectMapper mapper = new ObjectMapper();
            JsonNode node = mapper.readValue(text, JsonNode.class);
            ((ObjectNode) node.get(VENUE_SCHEMA)).remove(VENUE_CONTACT_SCHEMA);

            assertThrows(PayloadValidationException.class, () ->
                             validationService.validateBody(node.toString(),
                                                            ListType.MAGISTRATES_PUBLIC_LIST),
                         MAGISTRATES_PUBLIC_LIST_INVALID_MESSAGE);
        }
    }

    @Test
    void testValidateWithErrorsWhenVenueTelephoneMissingInMagistratesPublicList() throws IOException {
        try (InputStream jsonInput = this.getClass().getClassLoader()
            .getResourceAsStream(MAGISTRATES_PUBLIC_LIST_VALID_JSON)) {
            String text = new String(jsonInput.readAllBytes(), StandardCharsets.UTF_8);

            ObjectMapper mapper = new ObjectMapper();
            JsonNode node = mapper.readValue(text, JsonNode.class);
            ((ObjectNode) node.get(VENUE_SCHEMA).get(VENUE_CONTACT_SCHEMA)).remove("venueTelephone");

            assertThrows(PayloadValidationException.class, () ->
                             validationService.validateBody(node.toString(),
                                                            ListType.MAGISTRATES_PUBLIC_LIST),
                         MAGISTRATES_PUBLIC_LIST_INVALID_MESSAGE);
        }
    }

    @Test
    void testValidateWithErrorsWhenVenueEmailMissingInMagistratesPublicList() throws IOException {
        try (InputStream jsonInput = this.getClass().getClassLoader()
            .getResourceAsStream(MAGISTRATES_PUBLIC_LIST_VALID_JSON)) {
            String text = new String(jsonInput.readAllBytes(), StandardCharsets.UTF_8);

            ObjectMapper mapper = new ObjectMapper();
            JsonNode node = mapper.readValue(text, JsonNode.class);
            ((ObjectNode) node.get(VENUE_SCHEMA).get(VENUE_CONTACT_SCHEMA)).remove("venueEmail");

            assertThrows(PayloadValidationException.class, () ->
                             validationService.validateBody(node.toString(),
                                                            ListType.MAGISTRATES_PUBLIC_LIST),
                         MAGISTRATES_PUBLIC_LIST_INVALID_MESSAGE);
        }
    }

    @Test
    void testValidateWithErrorsWhenCourtHouseMissingInMagistratesPublicList() throws IOException {
        try (InputStream jsonInput = this.getClass().getClassLoader()
            .getResourceAsStream(MAGISTRATES_PUBLIC_LIST_VALID_JSON)) {
            String text = new String(jsonInput.readAllBytes(), StandardCharsets.UTF_8);

            ObjectMapper mapper = new ObjectMapper();
            JsonNode node = mapper.readValue(text, JsonNode.class);
            ((ObjectNode) node.get(COURT_LIST_SCHEMA).get(0)).remove(COURT_HOUSE_SCHEMA);

            assertThrows(PayloadValidationException.class, () ->
                             validationService.validateBody(node.toString(),
                                                            ListType.MAGISTRATES_PUBLIC_LIST),
                         MAGISTRATES_PUBLIC_LIST_INVALID_MESSAGE);
        }
    }

    @Test
    void testValidateWithErrorsWhenCourtHouseNameMissingInMagistratesPublicList() throws IOException {
        try (InputStream jsonInput = this.getClass().getClassLoader()
            .getResourceAsStream(MAGISTRATES_PUBLIC_LIST_VALID_JSON)) {
            String text = new String(jsonInput.readAllBytes(), StandardCharsets.UTF_8);

            ObjectMapper mapper = new ObjectMapper();
            JsonNode node = mapper.readValue(text, JsonNode.class);
            ((ObjectNode) node.get(COURT_LIST_SCHEMA).get(0).get(COURT_HOUSE_SCHEMA)).remove("courtHouseName");

            assertThrows(PayloadValidationException.class, () ->
                             validationService.validateBody(node.toString(),
                                                            ListType.MAGISTRATES_PUBLIC_LIST),
                         MAGISTRATES_PUBLIC_LIST_INVALID_MESSAGE);
        }
    }

    @Test
    void testValidateWithErrorsWhenCourtRoomMissingInMagistratesPublicList() throws IOException {
        try (InputStream jsonInput = this.getClass().getClassLoader()
            .getResourceAsStream(MAGISTRATES_PUBLIC_LIST_VALID_JSON)) {
            String text = new String(jsonInput.readAllBytes(), StandardCharsets.UTF_8);

            ObjectMapper mapper = new ObjectMapper();
            JsonNode node = mapper.readValue(text, JsonNode.class);
            ((ObjectNode) node.get(COURT_LIST_SCHEMA).get(0)
                .get(COURT_HOUSE_SCHEMA)).remove(COURT_ROOM_SCHEMA);

            assertThrows(PayloadValidationException.class, () ->
                             validationService.validateBody(node.toString(),
                                                            ListType.MAGISTRATES_PUBLIC_LIST),
                         MAGISTRATES_PUBLIC_LIST_INVALID_MESSAGE);
        }
    }

    @Test
    void testValidateWithErrorsWhenCourtRoomNameMissingInMagistratesPublicList() throws IOException {
        try (InputStream jsonInput = this.getClass().getClassLoader()
            .getResourceAsStream(MAGISTRATES_PUBLIC_LIST_VALID_JSON)) {
            String text = new String(jsonInput.readAllBytes(), StandardCharsets.UTF_8);

            ObjectMapper mapper = new ObjectMapper();
            JsonNode node = mapper.readValue(text, JsonNode.class);
            ((ObjectNode) node.get(COURT_LIST_SCHEMA).get(0)
                .get(COURT_HOUSE_SCHEMA).get(COURT_ROOM_SCHEMA).get(0)).remove("courtRoomName");

            assertThrows(PayloadValidationException.class, () ->
                             validationService.validateBody(node.toString(),
                                                            ListType.MAGISTRATES_PUBLIC_LIST),
                         MAGISTRATES_PUBLIC_LIST_INVALID_MESSAGE);
        }
    }

    @Test
    void testValidateWithErrorsWhenSessionMissingInMagistratesPublicList() throws IOException {
        try (InputStream jsonInput = this.getClass().getClassLoader()
            .getResourceAsStream(MAGISTRATES_PUBLIC_LIST_VALID_JSON)) {
            String text = new String(jsonInput.readAllBytes(), StandardCharsets.UTF_8);

            ObjectMapper mapper = new ObjectMapper();
            JsonNode node = mapper.readValue(text, JsonNode.class);
            ((ObjectNode) node.get(COURT_LIST_SCHEMA).get(0)
                .get(COURT_HOUSE_SCHEMA).get(COURT_ROOM_SCHEMA).get(0)).remove(SESSION_SCHEMA);

            assertThrows(PayloadValidationException.class, () ->
                             validationService.validateBody(node.toString(),
                                                            ListType.MAGISTRATES_PUBLIC_LIST),
                         MAGISTRATES_PUBLIC_LIST_INVALID_MESSAGE);
        }
    }

    @Test
    void testValidateWithErrorsWhenSittingsMissingInMagistratesPublicList() throws IOException {
        try (InputStream jsonInput = this.getClass().getClassLoader()
            .getResourceAsStream(MAGISTRATES_PUBLIC_LIST_VALID_JSON)) {
            String text = new String(jsonInput.readAllBytes(), StandardCharsets.UTF_8);

            ObjectMapper mapper = new ObjectMapper();
            JsonNode node = mapper.readValue(text, JsonNode.class);
            ((ObjectNode) node.get(COURT_LIST_SCHEMA).get(0)
                .get(COURT_HOUSE_SCHEMA).get(COURT_ROOM_SCHEMA).get(0)
                .get(SESSION_SCHEMA).get(0)).remove(SITTINGS_SCHEMA);

            assertThrows(PayloadValidationException.class, () ->
                             validationService.validateBody(node.toString(),
                                                            ListType.MAGISTRATES_PUBLIC_LIST),
                         MAGISTRATES_PUBLIC_LIST_INVALID_MESSAGE);
        }
    }

    @Test
    void testValidateWithErrorsWhenSittingStartMissingInMagistratesPublicList() throws IOException {
        try (InputStream jsonInput = this.getClass().getClassLoader()
            .getResourceAsStream(MAGISTRATES_PUBLIC_LIST_VALID_JSON)) {
            String text = new String(jsonInput.readAllBytes(), StandardCharsets.UTF_8);

            ObjectMapper mapper = new ObjectMapper();
            JsonNode node = mapper.readValue(text, JsonNode.class);
            ((ObjectNode) node.get(COURT_LIST_SCHEMA).get(0)
                .get(COURT_HOUSE_SCHEMA).get(COURT_ROOM_SCHEMA).get(0)
                .get(SESSION_SCHEMA).get(0).get(SITTINGS_SCHEMA).get(0)).remove("sittingStart");

            assertThrows(PayloadValidationException.class, () ->
                             validationService.validateBody(node.toString(),
                                                            ListType.MAGISTRATES_PUBLIC_LIST),
                         MAGISTRATES_PUBLIC_LIST_INVALID_MESSAGE);
        }
    }

    @Test
    void testValidateWithErrorsWhenSittingEndMissingInMagistratesPublicList() throws IOException {
        try (InputStream jsonInput = this.getClass().getClassLoader()
            .getResourceAsStream(MAGISTRATES_PUBLIC_LIST_VALID_JSON)) {
            String text = new String(jsonInput.readAllBytes(), StandardCharsets.UTF_8);

            ObjectMapper mapper = new ObjectMapper();
            JsonNode node = mapper.readValue(text, JsonNode.class);
            ((ObjectNode) node.get(COURT_LIST_SCHEMA).get(0)
                .get(COURT_HOUSE_SCHEMA).get(COURT_ROOM_SCHEMA).get(0)
                .get(SESSION_SCHEMA).get(0).get(SITTINGS_SCHEMA).get(0)).remove("sittingEnd");

            assertThrows(PayloadValidationException.class, () ->
                             validationService.validateBody(node.toString(),
                                                            ListType.MAGISTRATES_PUBLIC_LIST),
                         MAGISTRATES_PUBLIC_LIST_INVALID_MESSAGE);
        }
    }

    @Test
    void testValidateWithErrorsWhenHearingMissingInMagistratesPublicList() throws IOException {
        try (InputStream jsonInput = this.getClass().getClassLoader()
            .getResourceAsStream(MAGISTRATES_PUBLIC_LIST_VALID_JSON)) {
            String text = new String(jsonInput.readAllBytes(), StandardCharsets.UTF_8);

            ObjectMapper mapper = new ObjectMapper();
            JsonNode node = mapper.readValue(text, JsonNode.class);
            ((ObjectNode) node.get(COURT_LIST_SCHEMA).get(0)
                .get(COURT_HOUSE_SCHEMA).get(COURT_ROOM_SCHEMA).get(0)
                .get(SESSION_SCHEMA).get(0).get(SITTINGS_SCHEMA).get(0)).remove(HEARING_SCHEMA);

            assertThrows(PayloadValidationException.class, () ->
                             validationService.validateBody(node.toString(),
                                                            ListType.MAGISTRATES_PUBLIC_LIST),
                         MAGISTRATES_PUBLIC_LIST_INVALID_MESSAGE);
        }
    }

    @Test
    void testValidateWithErrorsWhenCaseMissingInMagistratesPublicList() throws IOException {
        try (InputStream jsonInput = this.getClass().getClassLoader()
            .getResourceAsStream(MAGISTRATES_PUBLIC_LIST_VALID_JSON)) {
            String text = new String(jsonInput.readAllBytes(), StandardCharsets.UTF_8);

            ObjectMapper mapper = new ObjectMapper();
            JsonNode node = mapper.readValue(text, JsonNode.class);
            ((ObjectNode) node.get(COURT_LIST_SCHEMA).get(0)
                .get(COURT_HOUSE_SCHEMA).get(COURT_ROOM_SCHEMA).get(0)
                .get(SESSION_SCHEMA).get(0).get(SITTINGS_SCHEMA).get(0)
                .get(HEARING_SCHEMA).get(0)).remove(CASE_SCHEMA);

            assertThrows(PayloadValidationException.class, () ->
                             validationService.validateBody(node.toString(),
                                                            ListType.MAGISTRATES_PUBLIC_LIST),
                         MAGISTRATES_PUBLIC_LIST_INVALID_MESSAGE);
        }
    }

    @Test
    void testValidateWithErrorsWhenCaseNumberMissingInMagistratesPublicList() throws IOException {
        try (InputStream jsonInput = this.getClass().getClassLoader()
            .getResourceAsStream(MAGISTRATES_PUBLIC_LIST_VALID_JSON)) {
            String text = new String(jsonInput.readAllBytes(), StandardCharsets.UTF_8);

            ObjectMapper mapper = new ObjectMapper();
            JsonNode node = mapper.readValue(text, JsonNode.class);
            ((ObjectNode) node.get(COURT_LIST_SCHEMA).get(0)
                .get(COURT_HOUSE_SCHEMA).get(COURT_ROOM_SCHEMA).get(0)
                .get(SESSION_SCHEMA).get(0).get(SITTINGS_SCHEMA).get(0)
                .get(HEARING_SCHEMA).get(0).get(CASE_SCHEMA).get(0)).remove("caseNumber");

            assertThrows(PayloadValidationException.class, () ->
                             validationService.validateBody(node.toString(),
                                                            ListType.MAGISTRATES_PUBLIC_LIST),
                         MAGISTRATES_PUBLIC_LIST_INVALID_MESSAGE);
        }
    }
}
