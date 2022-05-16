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

class SjpPressListTest {
    @Autowired
    ValidationService validationService;

    private static final String SJP_PRESS_LIST_VALID_JSON = "mocks/sjp-press-list/sjpPressList.json";
    private static final String SJP_PRESS_INVALID_MESSAGE = "Invalid sjp press";

    private static final String COURT_LIST_SCHEMA = "courtLists";
    private static final String COURT_HOUSE_SCHEMA = "courtHouse";
    private static final String COURT_ROOM_SCHEMA = "courtRoom";
    private static final String SESSION_SCHEMA = "session";
    private static final String SITTINGS_SCHEMA = "sittings";
    private static final String HEARING_SCHEMA = "hearing";
    private static final String PARTY_SCHEMA = "party";
    private static final String OFFENCE_SCHEMA = "offence";
    private static final String INDIVIDUAL_DETAILS_SCHEMA  = "individualDetails";
    private static final String ADDRESS_SCHEMA  = "address";

    private JsonNode getJsonNode(String json) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(json, JsonNode.class);
    }

    @Test
    void testValidateWithErrorWhenDocumentMissingInSjpPressList() throws IOException {
        try (InputStream jsonInput = this.getClass().getClassLoader()
            .getResourceAsStream(SJP_PRESS_LIST_VALID_JSON)) {
            String text = new String(jsonInput.readAllBytes(), StandardCharsets.UTF_8);

            JsonNode node = getJsonNode(text);
            ((ObjectNode) node).remove("document");

            assertThrows(PayloadValidationException.class, () ->
                             validationService.validateBody(node.toString(), ListType.SJP_PRESS_LIST),
                         SJP_PRESS_INVALID_MESSAGE);
        }
    }

    @Test
    void testValidateWithErrorWhenCourtListsMissingInSjpPressList() throws IOException {
        try (InputStream jsonInput = this.getClass().getClassLoader()
            .getResourceAsStream(SJP_PRESS_LIST_VALID_JSON)) {
            String text = new String(jsonInput.readAllBytes(), StandardCharsets.UTF_8);

            JsonNode node = getJsonNode(text);
            ((ObjectNode) node).remove(COURT_LIST_SCHEMA);

            assertThrows(PayloadValidationException.class, () ->
                             validationService.validateBody(node.toString(), ListType.SJP_PRESS_LIST),
                         SJP_PRESS_INVALID_MESSAGE);
        }
    }

    @Test
    void testValidateWithErrorWhenPublicationDateMissingInSjpPressList() throws IOException {
        try (InputStream jsonInput = this.getClass().getClassLoader()
            .getResourceAsStream(SJP_PRESS_LIST_VALID_JSON)) {
            String text = new String(jsonInput.readAllBytes(), StandardCharsets.UTF_8);

            ObjectMapper mapper = new ObjectMapper();
            JsonNode node = mapper.readValue(text, JsonNode.class);
            ((ObjectNode) node.get("document")).remove("publicationDate");

            assertThrows(PayloadValidationException.class, () ->
                             validationService.validateBody(node.toString(), ListType.SJP_PRESS_LIST),
                         SJP_PRESS_INVALID_MESSAGE);
        }
    }

    @Test
    void testValidateWithErrorWhenCourtHouseMissingInSjpPressList() throws IOException {
        try (InputStream jsonInput = this.getClass().getClassLoader()
            .getResourceAsStream(SJP_PRESS_LIST_VALID_JSON)) {
            String text = new String(jsonInput.readAllBytes(), StandardCharsets.UTF_8);

            ObjectMapper mapper = new ObjectMapper();
            JsonNode node = mapper.readValue(text, JsonNode.class);
            ((ObjectNode) node.get(COURT_LIST_SCHEMA).get(0)).remove(COURT_HOUSE_SCHEMA);

            assertThrows(PayloadValidationException.class, () ->
                             validationService.validateBody(node.toString(), ListType.SJP_PRESS_LIST),
                         SJP_PRESS_INVALID_MESSAGE);
        }
    }

    @Test
    void testValidateWithErrorWhenCourtRoomMissingInSjpPressList() throws IOException {
        try (InputStream jsonInput = this.getClass().getClassLoader()
            .getResourceAsStream(SJP_PRESS_LIST_VALID_JSON)) {
            String text = new String(jsonInput.readAllBytes(), StandardCharsets.UTF_8);

            ObjectMapper mapper = new ObjectMapper();
            JsonNode node = mapper.readValue(text, JsonNode.class);
            ((ObjectNode) node.get(COURT_LIST_SCHEMA).get(0).get(COURT_HOUSE_SCHEMA))
                .remove(COURT_ROOM_SCHEMA);

            assertThrows(PayloadValidationException.class, () ->
                             validationService.validateBody(node.toString(), ListType.SJP_PRESS_LIST),
                         SJP_PRESS_INVALID_MESSAGE);
        }
    }

    @Test
    void testValidateWithErrorWhenSessionMissingInSjpPressList() throws IOException {
        try (InputStream jsonInput = this.getClass().getClassLoader()
            .getResourceAsStream(SJP_PRESS_LIST_VALID_JSON)) {
            String text = new String(jsonInput.readAllBytes(), StandardCharsets.UTF_8);

            ObjectMapper mapper = new ObjectMapper();
            JsonNode node = mapper.readValue(text, JsonNode.class);
            ((ObjectNode) node.get(COURT_LIST_SCHEMA).get(0).get(COURT_HOUSE_SCHEMA)
                .get(COURT_ROOM_SCHEMA).get(0)).remove(SESSION_SCHEMA);

            assertThrows(PayloadValidationException.class, () ->
                             validationService.validateBody(node.toString(), ListType.SJP_PRESS_LIST),
                         SJP_PRESS_INVALID_MESSAGE);
        }
    }

    @Test
    void testValidateWithErrorWhenSittingsMissingInSjpPressList() throws IOException {
        try (InputStream jsonInput = this.getClass().getClassLoader()
            .getResourceAsStream(SJP_PRESS_LIST_VALID_JSON)) {
            String text = new String(jsonInput.readAllBytes(), StandardCharsets.UTF_8);

            ObjectMapper mapper = new ObjectMapper();
            JsonNode node = mapper.readValue(text, JsonNode.class);
            ((ObjectNode) node.get(COURT_LIST_SCHEMA).get(0).get(COURT_HOUSE_SCHEMA)
                .get(COURT_ROOM_SCHEMA).get(0).get(SESSION_SCHEMA).get(0)).remove(SITTINGS_SCHEMA);

            assertThrows(PayloadValidationException.class, () ->
                             validationService.validateBody(node.toString(), ListType.SJP_PRESS_LIST),
                         SJP_PRESS_INVALID_MESSAGE);
        }
    }

    @Test
    void testValidateWithErrorWhenHearingMissingInSjpPressList() throws IOException {
        try (InputStream jsonInput = this.getClass().getClassLoader()
            .getResourceAsStream(SJP_PRESS_LIST_VALID_JSON)) {
            String text = new String(jsonInput.readAllBytes(), StandardCharsets.UTF_8);

            ObjectMapper mapper = new ObjectMapper();
            JsonNode node = mapper.readValue(text, JsonNode.class);
            ((ObjectNode) node.get(COURT_LIST_SCHEMA).get(0).get(COURT_HOUSE_SCHEMA)
                .get(COURT_ROOM_SCHEMA).get(0).get(SESSION_SCHEMA).get(0)
                .get(SITTINGS_SCHEMA).get(0)).remove(HEARING_SCHEMA);

            assertThrows(PayloadValidationException.class, () ->
                             validationService.validateBody(node.toString(), ListType.SJP_PRESS_LIST),
                         SJP_PRESS_INVALID_MESSAGE);
        }
    }

    @Test
    void testValidateWithErrorWhenCaseMissingInSjpPressList() throws IOException {
        try (InputStream jsonInput = this.getClass().getClassLoader()
            .getResourceAsStream(SJP_PRESS_LIST_VALID_JSON)) {
            String text = new String(jsonInput.readAllBytes(), StandardCharsets.UTF_8);

            ObjectMapper mapper = new ObjectMapper();
            JsonNode node = mapper.readValue(text, JsonNode.class);
            ((ObjectNode) node.get(COURT_LIST_SCHEMA).get(0).get(COURT_HOUSE_SCHEMA)
                .get(COURT_ROOM_SCHEMA).get(0).get(SESSION_SCHEMA).get(0)
                .get(SITTINGS_SCHEMA).get(0).get(HEARING_SCHEMA).get(0)).remove("case");

            assertThrows(PayloadValidationException.class, () ->
                             validationService.validateBody(node.toString(), ListType.SJP_PRESS_LIST),
                         SJP_PRESS_INVALID_MESSAGE);
        }
    }

    @Test
    void testValidateWithErrorWhenCaseUrnMissingInSjpPressList() throws IOException {
        try (InputStream jsonInput = this.getClass().getClassLoader()
            .getResourceAsStream(SJP_PRESS_LIST_VALID_JSON)) {
            String text = new String(jsonInput.readAllBytes(), StandardCharsets.UTF_8);

            ObjectMapper mapper = new ObjectMapper();
            JsonNode node = mapper.readValue(text, JsonNode.class);
            ((ObjectNode) node.get(COURT_LIST_SCHEMA).get(0).get(COURT_HOUSE_SCHEMA)
                .get(COURT_ROOM_SCHEMA).get(0).get(SESSION_SCHEMA).get(0)
                .get(SITTINGS_SCHEMA).get(0).get(HEARING_SCHEMA).get(0)
                .get("case").get(0)).remove("caseUrn");

            assertThrows(PayloadValidationException.class, () ->
                             validationService.validateBody(node.toString(), ListType.SJP_PRESS_LIST),
                         SJP_PRESS_INVALID_MESSAGE);
        }
    }

    @Test
    void testValidateWithErrorWhenPartyMissingInSjpPressList() throws IOException {
        try (InputStream jsonInput = this.getClass().getClassLoader()
            .getResourceAsStream(SJP_PRESS_LIST_VALID_JSON)) {
            String text = new String(jsonInput.readAllBytes(), StandardCharsets.UTF_8);

            ObjectMapper mapper = new ObjectMapper();
            JsonNode node = mapper.readValue(text, JsonNode.class);
            ((ObjectNode) node.get(COURT_LIST_SCHEMA).get(0).get(COURT_HOUSE_SCHEMA)
                .get(COURT_ROOM_SCHEMA).get(0).get(SESSION_SCHEMA).get(0)
                .get(SITTINGS_SCHEMA).get(0).get(HEARING_SCHEMA).get(0)).remove(PARTY_SCHEMA);

            assertThrows(PayloadValidationException.class, () ->
                             validationService.validateBody(node.toString(), ListType.SJP_PRESS_LIST),
                         SJP_PRESS_INVALID_MESSAGE);
        }
    }

    @Test
    void testValidateWithErrorWhenOffenceMissingInSjpPressList() throws IOException {
        try (InputStream jsonInput = this.getClass().getClassLoader()
            .getResourceAsStream(SJP_PRESS_LIST_VALID_JSON)) {
            String text = new String(jsonInput.readAllBytes(), StandardCharsets.UTF_8);

            ObjectMapper mapper = new ObjectMapper();
            JsonNode node = mapper.readValue(text, JsonNode.class);
            ((ObjectNode) node.get(COURT_LIST_SCHEMA).get(0).get(COURT_HOUSE_SCHEMA)
                .get(COURT_ROOM_SCHEMA).get(0).get(SESSION_SCHEMA).get(0)
                .get(SITTINGS_SCHEMA).get(0).get(HEARING_SCHEMA).get(0))
                .remove(OFFENCE_SCHEMA);

            assertThrows(PayloadValidationException.class, () ->
                             validationService.validateBody(node.toString(), ListType.SJP_PRESS_LIST),
                         SJP_PRESS_INVALID_MESSAGE);
        }
    }

    @Test
    void testValidateWithErrorWhenPartyRoleMissingInSjpPressList() throws IOException {
        try (InputStream jsonInput = this.getClass().getClassLoader()
            .getResourceAsStream(SJP_PRESS_LIST_VALID_JSON)) {
            String text = new String(jsonInput.readAllBytes(), StandardCharsets.UTF_8);

            ObjectMapper mapper = new ObjectMapper();
            JsonNode node = mapper.readValue(text, JsonNode.class);
            ((ObjectNode) node.get(COURT_LIST_SCHEMA).get(0).get(COURT_HOUSE_SCHEMA)
                .get(COURT_ROOM_SCHEMA).get(0).get(SESSION_SCHEMA).get(0)
                .get(SITTINGS_SCHEMA).get(0).get(HEARING_SCHEMA).get(0)
                .get(PARTY_SCHEMA).get(0))
                .remove("partyRole");

            assertThrows(PayloadValidationException.class, () ->
                             validationService.validateBody(node.toString(), ListType.SJP_PRESS_LIST),
                         SJP_PRESS_INVALID_MESSAGE);
        }
    }

    @Test
    void testValidateWithErrorWhenIndividualDetailsForAccusedMissingInSjpPressList() throws IOException {
        try (InputStream jsonInput = this.getClass().getClassLoader()
            .getResourceAsStream(SJP_PRESS_LIST_VALID_JSON)) {
            String text = new String(jsonInput.readAllBytes(), StandardCharsets.UTF_8);

            ObjectMapper mapper = new ObjectMapper();
            JsonNode node = mapper.readValue(text, JsonNode.class);
            ((ObjectNode) node.get(COURT_LIST_SCHEMA).get(0).get(COURT_HOUSE_SCHEMA)
                .get(COURT_ROOM_SCHEMA).get(0).get(SESSION_SCHEMA).get(0)
                .get(SITTINGS_SCHEMA).get(0).get(HEARING_SCHEMA).get(0)
                .get(PARTY_SCHEMA).get(0))
                .remove(INDIVIDUAL_DETAILS_SCHEMA);

            assertThrows(PayloadValidationException.class, () ->
                             validationService.validateBody(node.toString(), ListType.SJP_PRESS_LIST),
                         SJP_PRESS_INVALID_MESSAGE);
        }
    }

    @Test
    void testValidateWithErrorWhenIndividualForenamesForAccusedMissingInSjpPressList() throws IOException {
        try (InputStream jsonInput = this.getClass().getClassLoader()
            .getResourceAsStream(SJP_PRESS_LIST_VALID_JSON)) {
            String text = new String(jsonInput.readAllBytes(), StandardCharsets.UTF_8);

            ObjectMapper mapper = new ObjectMapper();
            JsonNode node = mapper.readValue(text, JsonNode.class);
            ((ObjectNode) node.get(COURT_LIST_SCHEMA).get(0).get(COURT_HOUSE_SCHEMA)
                .get(COURT_ROOM_SCHEMA).get(0).get(SESSION_SCHEMA).get(0)
                .get(SITTINGS_SCHEMA).get(0).get(HEARING_SCHEMA).get(0)
                .get(PARTY_SCHEMA).get(0).get(INDIVIDUAL_DETAILS_SCHEMA))
                .remove("individualForenames");

            assertThrows(PayloadValidationException.class, () ->
                             validationService.validateBody(node.toString(), ListType.SJP_PRESS_LIST),
                         SJP_PRESS_INVALID_MESSAGE);
        }
    }

    @Test
    void testValidateWithErrorWhenIndividualSurnameForAccusedMissingInSjpPressList() throws IOException {
        try (InputStream jsonInput = this.getClass().getClassLoader()
            .getResourceAsStream(SJP_PRESS_LIST_VALID_JSON)) {
            String text = new String(jsonInput.readAllBytes(), StandardCharsets.UTF_8);

            ObjectMapper mapper = new ObjectMapper();
            JsonNode node = mapper.readValue(text, JsonNode.class);
            ((ObjectNode) node.get(COURT_LIST_SCHEMA).get(0).get(COURT_HOUSE_SCHEMA)
                .get(COURT_ROOM_SCHEMA).get(0).get(SESSION_SCHEMA).get(0)
                .get(SITTINGS_SCHEMA).get(0).get(HEARING_SCHEMA).get(0)
                .get(PARTY_SCHEMA).get(0).get(INDIVIDUAL_DETAILS_SCHEMA))
                .remove("individualSurname");

            assertThrows(PayloadValidationException.class, () ->
                             validationService.validateBody(node.toString(), ListType.SJP_PRESS_LIST),
                         SJP_PRESS_INVALID_MESSAGE);
        }
    }

    @Test
    void testValidateWithErrorWhenDateOfBirthForAccusedMissingInSjpPressList() throws IOException {
        try (InputStream jsonInput = this.getClass().getClassLoader()
            .getResourceAsStream(SJP_PRESS_LIST_VALID_JSON)) {
            String text = new String(jsonInput.readAllBytes(), StandardCharsets.UTF_8);

            ObjectMapper mapper = new ObjectMapper();
            JsonNode node = mapper.readValue(text, JsonNode.class);
            ((ObjectNode) node.get(COURT_LIST_SCHEMA).get(0).get(COURT_HOUSE_SCHEMA)
                .get(COURT_ROOM_SCHEMA).get(0).get(SESSION_SCHEMA).get(0)
                .get(SITTINGS_SCHEMA).get(0).get(HEARING_SCHEMA).get(0)
                .get(PARTY_SCHEMA).get(0).get(INDIVIDUAL_DETAILS_SCHEMA))
                .remove("dateOfBirth");

            assertThrows(PayloadValidationException.class, () ->
                             validationService.validateBody(node.toString(), ListType.SJP_PRESS_LIST),
                         SJP_PRESS_INVALID_MESSAGE);
        }
    }

    @Test
    void testValidateWithErrorWhenAgeMissingForAccusedInSjpPressList() throws IOException {
        try (InputStream jsonInput = this.getClass().getClassLoader()
            .getResourceAsStream(SJP_PRESS_LIST_VALID_JSON)) {
            String text = new String(jsonInput.readAllBytes(), StandardCharsets.UTF_8);

            ObjectMapper mapper = new ObjectMapper();
            JsonNode node = mapper.readValue(text, JsonNode.class);
            ((ObjectNode) node.get(COURT_LIST_SCHEMA).get(0).get(COURT_HOUSE_SCHEMA)
                .get(COURT_ROOM_SCHEMA).get(0).get(SESSION_SCHEMA).get(0)
                .get(SITTINGS_SCHEMA).get(0).get(HEARING_SCHEMA).get(0)
                .get(PARTY_SCHEMA).get(0).get(INDIVIDUAL_DETAILS_SCHEMA))
                .remove("age");

            assertThrows(PayloadValidationException.class, () ->
                             validationService.validateBody(node.toString(), ListType.SJP_PRESS_LIST),
                         SJP_PRESS_INVALID_MESSAGE);
        }
    }

    @Test
    void testValidateWithErrorWhenAddressForAccusedMissingInSjpPressList() throws IOException {
        try (InputStream jsonInput = this.getClass().getClassLoader()
            .getResourceAsStream(SJP_PRESS_LIST_VALID_JSON)) {
            String text = new String(jsonInput.readAllBytes(), StandardCharsets.UTF_8);

            ObjectMapper mapper = new ObjectMapper();
            JsonNode node = mapper.readValue(text, JsonNode.class);
            ((ObjectNode) node.get(COURT_LIST_SCHEMA).get(0).get(COURT_HOUSE_SCHEMA)
                .get(COURT_ROOM_SCHEMA).get(0).get(SESSION_SCHEMA).get(0)
                .get(SITTINGS_SCHEMA).get(0).get(HEARING_SCHEMA).get(0)
                .get(PARTY_SCHEMA).get(0).get(INDIVIDUAL_DETAILS_SCHEMA))
                .remove(ADDRESS_SCHEMA);

            assertThrows(PayloadValidationException.class, () ->
                             validationService.validateBody(node.toString(), ListType.SJP_PRESS_LIST),
                         SJP_PRESS_INVALID_MESSAGE);
        }
    }

    @Test
    void testValidateWithErrorWhenTownForAccusedMissingInSjpPressList() throws IOException {
        try (InputStream jsonInput = this.getClass().getClassLoader()
            .getResourceAsStream(SJP_PRESS_LIST_VALID_JSON)) {
            String text = new String(jsonInput.readAllBytes(), StandardCharsets.UTF_8);

            ObjectMapper mapper = new ObjectMapper();
            JsonNode node = mapper.readValue(text, JsonNode.class);
            ((ObjectNode) node.get(COURT_LIST_SCHEMA).get(0).get(COURT_HOUSE_SCHEMA)
                .get(COURT_ROOM_SCHEMA).get(0).get(SESSION_SCHEMA).get(0)
                .get(SITTINGS_SCHEMA).get(0).get(HEARING_SCHEMA).get(0)
                .get(PARTY_SCHEMA).get(0).get(INDIVIDUAL_DETAILS_SCHEMA)
                .get(ADDRESS_SCHEMA)).remove("town");

            assertThrows(PayloadValidationException.class, () ->
                             validationService.validateBody(node.toString(), ListType.SJP_PRESS_LIST),
                         SJP_PRESS_INVALID_MESSAGE);
        }
    }

    @Test
    void testValidateWithErrorWhenCountryForAccusedMissingInSjpPressList() throws IOException {
        try (InputStream jsonInput = this.getClass().getClassLoader()
            .getResourceAsStream(SJP_PRESS_LIST_VALID_JSON)) {
            String text = new String(jsonInput.readAllBytes(), StandardCharsets.UTF_8);

            ObjectMapper mapper = new ObjectMapper();
            JsonNode node = mapper.readValue(text, JsonNode.class);
            ((ObjectNode) node.get(COURT_LIST_SCHEMA).get(0).get(COURT_HOUSE_SCHEMA)
                .get(COURT_ROOM_SCHEMA).get(0).get(SESSION_SCHEMA).get(0)
                .get(SITTINGS_SCHEMA).get(0).get(HEARING_SCHEMA).get(0)
                .get(PARTY_SCHEMA).get(0).get(INDIVIDUAL_DETAILS_SCHEMA)
                .get(ADDRESS_SCHEMA)).remove("county");

            assertThrows(PayloadValidationException.class, () ->
                             validationService.validateBody(node.toString(), ListType.SJP_PRESS_LIST),
                         SJP_PRESS_INVALID_MESSAGE);
        }
    }

    @Test
    void testValidateWithErrorWhenPostCodeForAccusedMissingInSjpPressList() throws IOException {
        try (InputStream jsonInput = this.getClass().getClassLoader()
            .getResourceAsStream(SJP_PRESS_LIST_VALID_JSON)) {
            String text = new String(jsonInput.readAllBytes(), StandardCharsets.UTF_8);

            ObjectMapper mapper = new ObjectMapper();
            JsonNode node = mapper.readValue(text, JsonNode.class);
            ((ObjectNode) node.get(COURT_LIST_SCHEMA).get(0).get(COURT_HOUSE_SCHEMA)
                .get(COURT_ROOM_SCHEMA).get(0).get(SESSION_SCHEMA).get(0)
                .get(SITTINGS_SCHEMA).get(0).get(HEARING_SCHEMA).get(0)
                .get(PARTY_SCHEMA).get(0).get(INDIVIDUAL_DETAILS_SCHEMA)
                .get(ADDRESS_SCHEMA)).remove("postCode");

            assertThrows(PayloadValidationException.class, () ->
                             validationService.validateBody(node.toString(), ListType.SJP_PRESS_LIST),
                         SJP_PRESS_INVALID_MESSAGE);
        }
    }

    @Test
    void testValidateWithErrorWhenOrganisationDetailsForProsecutorMissingInSjpPressList() throws IOException {
        try (InputStream jsonInput = this.getClass().getClassLoader()
            .getResourceAsStream(SJP_PRESS_LIST_VALID_JSON)) {
            String text = new String(jsonInput.readAllBytes(), StandardCharsets.UTF_8);

            ObjectMapper mapper = new ObjectMapper();
            JsonNode node = mapper.readValue(text, JsonNode.class);
            ((ObjectNode) node.get(COURT_LIST_SCHEMA).get(0).get(COURT_HOUSE_SCHEMA)
                .get(COURT_ROOM_SCHEMA).get(0).get(SESSION_SCHEMA).get(0)
                .get(SITTINGS_SCHEMA).get(0).get(HEARING_SCHEMA).get(0)
                .get(PARTY_SCHEMA).get(1))
                .remove("organisationDetails");

            assertThrows(PayloadValidationException.class, () ->
                             validationService.validateBody(node.toString(), ListType.SJP_PRESS_LIST),
                         SJP_PRESS_INVALID_MESSAGE);
        }
    }

    @Test
    void testValidateWithErrorWhenOrganisationNameForProsecutorMissingInSjpPressList() throws IOException {
        try (InputStream jsonInput = this.getClass().getClassLoader()
            .getResourceAsStream(SJP_PRESS_LIST_VALID_JSON)) {
            String text = new String(jsonInput.readAllBytes(), StandardCharsets.UTF_8);

            ObjectMapper mapper = new ObjectMapper();
            JsonNode node = mapper.readValue(text, JsonNode.class);
            ((ObjectNode) node.get(COURT_LIST_SCHEMA).get(0).get(COURT_HOUSE_SCHEMA)
                .get(COURT_ROOM_SCHEMA).get(0).get(SESSION_SCHEMA).get(0)
                .get(SITTINGS_SCHEMA).get(0).get(HEARING_SCHEMA).get(0)
                .get(PARTY_SCHEMA).get(1).get("organisationDetails"))
                .remove("organisationName");

            assertThrows(PayloadValidationException.class, () ->
                             validationService.validateBody(node.toString(), ListType.SJP_PRESS_LIST),
                         SJP_PRESS_INVALID_MESSAGE);
        }
    }

}
