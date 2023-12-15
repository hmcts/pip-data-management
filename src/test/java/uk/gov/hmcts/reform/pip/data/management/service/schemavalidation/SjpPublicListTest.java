package uk.gov.hmcts.reform.pip.data.management.service.schemavalidation;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.zonky.test.db.AutoConfigureEmbeddedDatabase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.pip.data.management.Application;
import uk.gov.hmcts.reform.pip.data.management.config.AzureBlobConfigurationTestConfiguration;
import uk.gov.hmcts.reform.pip.data.management.errorhandling.exceptions.PayloadValidationException;
import uk.gov.hmcts.reform.pip.data.management.models.publication.HeaderGroup;
import uk.gov.hmcts.reform.pip.data.management.service.ValidationService;
import uk.gov.hmcts.reform.pip.model.publication.ArtefactType;
import uk.gov.hmcts.reform.pip.model.publication.Language;
import uk.gov.hmcts.reform.pip.model.publication.ListType;
import uk.gov.hmcts.reform.pip.model.publication.Sensitivity;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest(classes = {Application.class, AzureBlobConfigurationTestConfiguration.class})
@ActiveProfiles(profiles = "test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@AutoConfigureEmbeddedDatabase(type = AutoConfigureEmbeddedDatabase.DatabaseType.POSTGRES)

class SjpPublicListTest {
    @Autowired
    ValidationService validationService;

    private static final String SJP_PUBLIC_LIST_VALID_JSON = "mocks/sjp-public-list/sjpPublicList.json";
    private static final String SJP_PUBLIC_VALID_MESSAGE = "SJP public list should be valid";
    private static final String SJP_PUBLIC_INVALID_MESSAGE = "Invalid sjp public";

    private static final String COURT_LIST_SCHEMA = "courtLists";
    private static final String COURT_HOUSE_SCHEMA = "courtHouse";
    private static final String COURT_ROOM_SCHEMA = "courtRoom";
    private static final String SESSION_SCHEMA = "session";
    private static final String SITTINGS_SCHEMA = "sittings";
    private static final String HEARING_SCHEMA = "hearing";
    private static final String PARTY_SCHEMA = "party";
    private static final String OFFENCE_SCHEMA = "offenceTitle";
    private static final String INDIVIDUAL_DETAILS_SCHEMA  = "individualDetails";
    private static final String ADDRESS_SCHEMA  = "address";
    private static final String SOURCE_ARTEFACT_ID = "sourceArtefactId";
    private static final LocalDateTime DISPLAY_FROM = LocalDateTime.now();
    private static final LocalDateTime DISPLAY_TO = LocalDateTime.now();
    private static final Language LANGUAGE = Language.ENGLISH;
    private static final String PROVENANCE = "provenance";
    private static final Sensitivity SENSITIVITY = Sensitivity.PUBLIC;
    private static final ArtefactType ARTEFACT_TYPE = ArtefactType.LIST;
    private static final String COURT_ID = "123";
    private static final ListType LIST_TYPE = ListType.SJP_PUBLIC_LIST;
    private static final LocalDateTime CONTENT_DATE = LocalDateTime.now();

    private HeaderGroup headerGroup;

    private JsonNode getJsonNode(String json) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(json, JsonNode.class);
    }

    @BeforeEach
    void setup() {
        headerGroup = new HeaderGroup(PROVENANCE, SOURCE_ARTEFACT_ID, ARTEFACT_TYPE, SENSITIVITY, LANGUAGE,
                                      DISPLAY_FROM, DISPLAY_TO, LIST_TYPE, COURT_ID, CONTENT_DATE);
    }

    @Test
    void testValidateWithSuccessIfEitherAccusedIndividualOrOrgDetailsPresentInSjpPublicList() throws IOException {
        try (InputStream jsonInput = this.getClass().getClassLoader()
            .getResourceAsStream(SJP_PUBLIC_LIST_VALID_JSON)) {
            String text = new String(jsonInput.readAllBytes(), StandardCharsets.UTF_8);

            assertDoesNotThrow(() -> validationService.validateBody(text, headerGroup),
                               SJP_PUBLIC_VALID_MESSAGE);
        }
    }

    @Test
    void testValidateWithErrorWhenDocumentMissingInSjpPublicList() throws IOException {
        try (InputStream jsonInput = this.getClass().getClassLoader()
            .getResourceAsStream(SJP_PUBLIC_LIST_VALID_JSON)) {
            String text = new String(jsonInput.readAllBytes(), StandardCharsets.UTF_8);

            JsonNode node = getJsonNode(text);
            ((ObjectNode) node).remove("document");

            String listJson = node.toString();
            assertThrows(PayloadValidationException.class, () ->
                             validationService.validateBody(listJson, headerGroup),
                         SJP_PUBLIC_INVALID_MESSAGE);
        }
    }

    @Test
    void testValidateWithErrorWhenCourtListsMissingInSjpPublicList() throws IOException {
        try (InputStream jsonInput = this.getClass().getClassLoader()
            .getResourceAsStream(SJP_PUBLIC_LIST_VALID_JSON)) {
            String text = new String(jsonInput.readAllBytes(), StandardCharsets.UTF_8);

            JsonNode node = getJsonNode(text);
            ((ObjectNode) node).remove(COURT_LIST_SCHEMA);

            String listJson = node.toString();
            assertThrows(PayloadValidationException.class, () ->
                             validationService.validateBody(listJson, headerGroup),
                         SJP_PUBLIC_INVALID_MESSAGE);
        }
    }

    @Test
    void testValidateWithErrorWhenPublicationDateMissingInSjpPublicList() throws IOException {
        try (InputStream jsonInput = this.getClass().getClassLoader()
            .getResourceAsStream(SJP_PUBLIC_LIST_VALID_JSON)) {
            String text = new String(jsonInput.readAllBytes(), StandardCharsets.UTF_8);

            ObjectMapper mapper = new ObjectMapper();
            JsonNode node = mapper.readValue(text, JsonNode.class);
            ((ObjectNode) node.get("document")).remove("publicationDate");

            String listJson = node.toString();
            assertThrows(PayloadValidationException.class, () ->
                             validationService.validateBody(listJson, headerGroup),
                         SJP_PUBLIC_INVALID_MESSAGE);
        }
    }

    @Test
    void testValidateWithErrorWhenCourtHouseMissingInSjpPublicList() throws IOException {
        try (InputStream jsonInput = this.getClass().getClassLoader()
            .getResourceAsStream(SJP_PUBLIC_LIST_VALID_JSON)) {
            String text = new String(jsonInput.readAllBytes(), StandardCharsets.UTF_8);

            ObjectMapper mapper = new ObjectMapper();
            JsonNode node = mapper.readValue(text, JsonNode.class);
            ((ObjectNode) node.get(COURT_LIST_SCHEMA).get(0)).remove(COURT_HOUSE_SCHEMA);

            String listJson = node.toString();
            assertThrows(PayloadValidationException.class, () ->
                             validationService.validateBody(listJson, headerGroup),
                         SJP_PUBLIC_INVALID_MESSAGE);
        }
    }

    @Test
    void testValidateWithErrorWhenCourtRoomMissingInSjpPublicList() throws IOException {
        try (InputStream jsonInput = this.getClass().getClassLoader()
            .getResourceAsStream(SJP_PUBLIC_LIST_VALID_JSON)) {
            String text = new String(jsonInput.readAllBytes(), StandardCharsets.UTF_8);

            ObjectMapper mapper = new ObjectMapper();
            JsonNode node = mapper.readValue(text, JsonNode.class);
            ((ObjectNode) node.get(COURT_LIST_SCHEMA).get(0).get(COURT_HOUSE_SCHEMA))
                .remove(COURT_ROOM_SCHEMA);

            String listJson = node.toString();
            assertThrows(PayloadValidationException.class, () ->
                             validationService.validateBody(listJson, headerGroup),
                         SJP_PUBLIC_INVALID_MESSAGE);
        }
    }

    @Test
    void testValidateWithErrorWhenSessionMissingInSjpPublicList() throws IOException {
        try (InputStream jsonInput = this.getClass().getClassLoader()
            .getResourceAsStream(SJP_PUBLIC_LIST_VALID_JSON)) {
            String text = new String(jsonInput.readAllBytes(), StandardCharsets.UTF_8);

            ObjectMapper mapper = new ObjectMapper();
            JsonNode node = mapper.readValue(text, JsonNode.class);
            ((ObjectNode) node.get(COURT_LIST_SCHEMA).get(0).get(COURT_HOUSE_SCHEMA)
                .get(COURT_ROOM_SCHEMA).get(0)).remove(SESSION_SCHEMA);

            String listJson = node.toString();
            assertThrows(PayloadValidationException.class, () ->
                             validationService.validateBody(listJson, headerGroup),
                         SJP_PUBLIC_INVALID_MESSAGE);
        }
    }

    @Test
    void testValidateWithErrorWhenSittingsMissingInSjpPublicList() throws IOException {
        try (InputStream jsonInput = this.getClass().getClassLoader()
            .getResourceAsStream(SJP_PUBLIC_LIST_VALID_JSON)) {
            String text = new String(jsonInput.readAllBytes(), StandardCharsets.UTF_8);

            ObjectMapper mapper = new ObjectMapper();
            JsonNode node = mapper.readValue(text, JsonNode.class);
            ((ObjectNode) node.get(COURT_LIST_SCHEMA).get(0).get(COURT_HOUSE_SCHEMA)
                .get(COURT_ROOM_SCHEMA).get(0).get(SESSION_SCHEMA).get(0)).remove(SITTINGS_SCHEMA);

            String listJson = node.toString();
            assertThrows(PayloadValidationException.class, () ->
                             validationService.validateBody(listJson, headerGroup),
                         SJP_PUBLIC_INVALID_MESSAGE);
        }
    }

    @Test
    void testValidateWithErrorWhenHearingMissingInSjpPublicList() throws IOException {
        try (InputStream jsonInput = this.getClass().getClassLoader()
            .getResourceAsStream(SJP_PUBLIC_LIST_VALID_JSON)) {
            String text = new String(jsonInput.readAllBytes(), StandardCharsets.UTF_8);

            ObjectMapper mapper = new ObjectMapper();
            JsonNode node = mapper.readValue(text, JsonNode.class);
            ((ObjectNode) node.get(COURT_LIST_SCHEMA).get(0).get(COURT_HOUSE_SCHEMA)
                .get(COURT_ROOM_SCHEMA).get(0).get(SESSION_SCHEMA).get(0)
                .get(SITTINGS_SCHEMA).get(0)).remove(HEARING_SCHEMA);

            String listJson = node.toString();
            assertThrows(PayloadValidationException.class, () ->
                             validationService.validateBody(listJson, headerGroup),
                         SJP_PUBLIC_INVALID_MESSAGE);
        }
    }

    @Test
    void testValidateWithErrorWhenPartyMissingInSjpPublicList() throws IOException {
        try (InputStream jsonInput = this.getClass().getClassLoader()
            .getResourceAsStream(SJP_PUBLIC_LIST_VALID_JSON)) {
            String text = new String(jsonInput.readAllBytes(), StandardCharsets.UTF_8);

            ObjectMapper mapper = new ObjectMapper();
            JsonNode node = mapper.readValue(text, JsonNode.class);
            ((ObjectNode) node.get(COURT_LIST_SCHEMA).get(0).get(COURT_HOUSE_SCHEMA)
                .get(COURT_ROOM_SCHEMA).get(0).get(SESSION_SCHEMA).get(0)
                .get(SITTINGS_SCHEMA).get(0).get(HEARING_SCHEMA).get(0)).remove(PARTY_SCHEMA);

            String listJson = node.toString();
            assertThrows(PayloadValidationException.class, () ->
                             validationService.validateBody(listJson, headerGroup),
                         SJP_PUBLIC_INVALID_MESSAGE);
        }
    }

    @Test
    void testValidateWithErrorWhenOffenceMissingInSjpPublicList() throws IOException {
        try (InputStream jsonInput = this.getClass().getClassLoader()
            .getResourceAsStream(SJP_PUBLIC_LIST_VALID_JSON)) {
            String text = new String(jsonInput.readAllBytes(), StandardCharsets.UTF_8);

            ObjectMapper mapper = new ObjectMapper();
            JsonNode node = mapper.readValue(text, JsonNode.class);
            ((ObjectNode) node.get(COURT_LIST_SCHEMA).get(0).get(COURT_HOUSE_SCHEMA)
                .get(COURT_ROOM_SCHEMA).get(0).get(SESSION_SCHEMA).get(0)
                .get(SITTINGS_SCHEMA).get(0).get(HEARING_SCHEMA).get(0))
                .remove(OFFENCE_SCHEMA);

            String listJson = node.toString();
            assertThrows(PayloadValidationException.class, () ->
                             validationService.validateBody(listJson, headerGroup),
                         SJP_PUBLIC_INVALID_MESSAGE);
        }
    }

    @Test
    void testValidateWithErrorWhenPartyRoleMissingInSjpPublicList() throws IOException {
        try (InputStream jsonInput = this.getClass().getClassLoader()
            .getResourceAsStream(SJP_PUBLIC_LIST_VALID_JSON)) {
            String text = new String(jsonInput.readAllBytes(), StandardCharsets.UTF_8);

            ObjectMapper mapper = new ObjectMapper();
            JsonNode node = mapper.readValue(text, JsonNode.class);
            ((ObjectNode) node.get(COURT_LIST_SCHEMA).get(0).get(COURT_HOUSE_SCHEMA)
                .get(COURT_ROOM_SCHEMA).get(0).get(SESSION_SCHEMA).get(0)
                .get(SITTINGS_SCHEMA).get(0).get(HEARING_SCHEMA).get(0)
                .get(PARTY_SCHEMA).get(0))
                .remove("partyRole");

            String listJson = node.toString();
            assertThrows(PayloadValidationException.class, () ->
                             validationService.validateBody(listJson, headerGroup),
                         SJP_PUBLIC_INVALID_MESSAGE);
        }
    }

    @Test
    void testValidateWithErrorWhenIndividualDetailsForAccusedMissingInSjpPublicList() throws IOException {
        try (InputStream jsonInput = this.getClass().getClassLoader()
            .getResourceAsStream(SJP_PUBLIC_LIST_VALID_JSON)) {
            String text = new String(jsonInput.readAllBytes(), StandardCharsets.UTF_8);

            ObjectMapper mapper = new ObjectMapper();
            JsonNode node = mapper.readValue(text, JsonNode.class);
            ((ObjectNode) node.get(COURT_LIST_SCHEMA).get(0).get(COURT_HOUSE_SCHEMA)
                .get(COURT_ROOM_SCHEMA).get(0).get(SESSION_SCHEMA).get(0)
                .get(SITTINGS_SCHEMA).get(0).get(HEARING_SCHEMA).get(0)
                .get(PARTY_SCHEMA).get(0))
                .remove(INDIVIDUAL_DETAILS_SCHEMA);

            String listJson = node.toString();
            assertThrows(PayloadValidationException.class, () ->
                             validationService.validateBody(listJson, headerGroup),
                         SJP_PUBLIC_INVALID_MESSAGE);
        }
    }

    @Test
    void testValidateSuccessWhenIndividualForenamesForAccusedMissingInSjpPublicList() throws IOException {
        try (InputStream jsonInput = this.getClass().getClassLoader()
            .getResourceAsStream(SJP_PUBLIC_LIST_VALID_JSON)) {
            String text = new String(jsonInput.readAllBytes(), StandardCharsets.UTF_8);

            ObjectMapper mapper = new ObjectMapper();
            JsonNode node = mapper.readValue(text, JsonNode.class);
            ((ObjectNode) node.get(COURT_LIST_SCHEMA).get(0).get(COURT_HOUSE_SCHEMA)
                .get(COURT_ROOM_SCHEMA).get(0).get(SESSION_SCHEMA).get(0)
                .get(SITTINGS_SCHEMA).get(0).get(HEARING_SCHEMA).get(0)
                .get(PARTY_SCHEMA).get(0).get(INDIVIDUAL_DETAILS_SCHEMA))
                .remove("individualForenames");

            String listJson = node.toString();
            assertDoesNotThrow(() -> validationService.validateBody(listJson, headerGroup),
                               SJP_PUBLIC_VALID_MESSAGE);
        }
    }

    @Test
    void testValidateWithSuccessWhenIndividualSurnameForAccusedMissingInSjpPublicList() throws IOException {
        try (InputStream jsonInput = this.getClass().getClassLoader()
            .getResourceAsStream(SJP_PUBLIC_LIST_VALID_JSON)) {
            String text = new String(jsonInput.readAllBytes(), StandardCharsets.UTF_8);

            ObjectMapper mapper = new ObjectMapper();
            JsonNode node = mapper.readValue(text, JsonNode.class);
            ((ObjectNode) node.get(COURT_LIST_SCHEMA).get(0).get(COURT_HOUSE_SCHEMA)
                .get(COURT_ROOM_SCHEMA).get(0).get(SESSION_SCHEMA).get(0)
                .get(SITTINGS_SCHEMA).get(0).get(HEARING_SCHEMA).get(0)
                .get(PARTY_SCHEMA).get(0).get(INDIVIDUAL_DETAILS_SCHEMA))
                .remove("individualSurname");

            String listJson = node.toString();
            assertDoesNotThrow(() -> validationService.validateBody(listJson, headerGroup),
                               SJP_PUBLIC_VALID_MESSAGE);
        }
    }

    @Test
    void testValidateWithSuccessWhenAddressForAccusedMissingInSjpPublicList() throws IOException {
        try (InputStream jsonInput = this.getClass().getClassLoader()
            .getResourceAsStream(SJP_PUBLIC_LIST_VALID_JSON)) {
            String text = new String(jsonInput.readAllBytes(), StandardCharsets.UTF_8);

            ObjectMapper mapper = new ObjectMapper();
            JsonNode node = mapper.readValue(text, JsonNode.class);
            ((ObjectNode) node.get(COURT_LIST_SCHEMA).get(0).get(COURT_HOUSE_SCHEMA)
                .get(COURT_ROOM_SCHEMA).get(0).get(SESSION_SCHEMA).get(0)
                .get(SITTINGS_SCHEMA).get(0).get(HEARING_SCHEMA).get(0)
                .get(PARTY_SCHEMA).get(0).get(INDIVIDUAL_DETAILS_SCHEMA))
                .remove(ADDRESS_SCHEMA);

            String listJson = node.toString();
            assertDoesNotThrow(() -> validationService.validateBody(listJson, headerGroup),
                               SJP_PUBLIC_VALID_MESSAGE);
        }
    }

    @Test
    void testValidateWithSuccessWhenPostCodeForAccusedMissingInSjpPublicList() throws IOException {
        try (InputStream jsonInput = this.getClass().getClassLoader()
            .getResourceAsStream(SJP_PUBLIC_LIST_VALID_JSON)) {
            String text = new String(jsonInput.readAllBytes(), StandardCharsets.UTF_8);

            ObjectMapper mapper = new ObjectMapper();
            JsonNode node = mapper.readValue(text, JsonNode.class);
            ((ObjectNode) node.get(COURT_LIST_SCHEMA).get(0).get(COURT_HOUSE_SCHEMA)
                .get(COURT_ROOM_SCHEMA).get(0).get(SESSION_SCHEMA).get(0)
                .get(SITTINGS_SCHEMA).get(0).get(HEARING_SCHEMA).get(0)
                .get(PARTY_SCHEMA).get(0).get(INDIVIDUAL_DETAILS_SCHEMA)
                .get(ADDRESS_SCHEMA)).remove("postCode");

            String listJson = node.toString();
            assertDoesNotThrow(() -> validationService.validateBody(listJson, headerGroup),
                               SJP_PUBLIC_VALID_MESSAGE);
        }
    }

    @Test
    void testValidateWithErrorWhenOrganisationDetailsForProsecutorMissingInSjpPublicList() throws IOException {
        try (InputStream jsonInput = this.getClass().getClassLoader()
            .getResourceAsStream(SJP_PUBLIC_LIST_VALID_JSON)) {
            String text = new String(jsonInput.readAllBytes(), StandardCharsets.UTF_8);

            ObjectMapper mapper = new ObjectMapper();
            JsonNode node = mapper.readValue(text, JsonNode.class);
            ((ObjectNode) node.get(COURT_LIST_SCHEMA).get(0).get(COURT_HOUSE_SCHEMA)
                .get(COURT_ROOM_SCHEMA).get(0).get(SESSION_SCHEMA).get(0)
                .get(SITTINGS_SCHEMA).get(0).get(HEARING_SCHEMA).get(0)
                .get(PARTY_SCHEMA).get(1))
                .remove("organisationDetails");

            String listJson = node.toString();
            assertThrows(PayloadValidationException.class, () ->
                             validationService.validateBody(listJson, headerGroup),
                         SJP_PUBLIC_INVALID_MESSAGE);
        }
    }

    @Test
    void testValidateWithErrorWhenOrganisationNameForProsecutorMissingInSjpPublicList() throws IOException {
        try (InputStream jsonInput = this.getClass().getClassLoader()
            .getResourceAsStream(SJP_PUBLIC_LIST_VALID_JSON)) {
            String text = new String(jsonInput.readAllBytes(), StandardCharsets.UTF_8);

            ObjectMapper mapper = new ObjectMapper();
            JsonNode node = mapper.readValue(text, JsonNode.class);
            ((ObjectNode) node.get(COURT_LIST_SCHEMA).get(0).get(COURT_HOUSE_SCHEMA)
                .get(COURT_ROOM_SCHEMA).get(0).get(SESSION_SCHEMA).get(0)
                .get(SITTINGS_SCHEMA).get(0).get(HEARING_SCHEMA).get(0)
                .get(PARTY_SCHEMA).get(1).get("organisationDetails"))
                .remove("organisationName");

            String listJson = node.toString();
            assertThrows(PayloadValidationException.class, () ->
                             validationService.validateBody(listJson, headerGroup),
                         SJP_PUBLIC_INVALID_MESSAGE);
        }
    }

    @Test
    void testValidateWithErrorWhenOffenceTitleMissingInSjpPublicList() throws IOException {
        try (InputStream jsonInput = this.getClass().getClassLoader()
            .getResourceAsStream(SJP_PUBLIC_LIST_VALID_JSON)) {
            String text = new String(jsonInput.readAllBytes(), StandardCharsets.UTF_8);

            ObjectMapper mapper = new ObjectMapper();
            JsonNode node = mapper.readValue(text, JsonNode.class);
            ((ObjectNode) node.get(COURT_LIST_SCHEMA).get(0).get(COURT_HOUSE_SCHEMA)
                .get(COURT_ROOM_SCHEMA).get(0).get(SESSION_SCHEMA).get(0)
                .get(SITTINGS_SCHEMA).get(0).get(HEARING_SCHEMA).get(0)
                .get(OFFENCE_SCHEMA).get(0)).remove("offenceTitle");

            String listJson = node.toString();
            assertThrows(PayloadValidationException.class, () ->
                             validationService.validateBody(listJson, headerGroup),
                         SJP_PUBLIC_INVALID_MESSAGE);
        }
    }

    @Test
    void testValidateWithErrorWhenPostCodeForAccusedInWrongFormat() throws IOException {
        try (InputStream jsonInput = this.getClass().getClassLoader()
            .getResourceAsStream(SJP_PUBLIC_LIST_VALID_JSON)) {
            String text = new String(jsonInput.readAllBytes(), StandardCharsets.UTF_8);

            ObjectMapper mapper = new ObjectMapper();
            JsonNode node = mapper.readValue(text, JsonNode.class);

            String listJson = node.toString().replace("\"postCode\":\"AA1\"", "\"postCode\":\"1234\"");
            assertThrows(PayloadValidationException.class, () ->
                             validationService.validateBody(listJson, headerGroup),
                         SJP_PUBLIC_INVALID_MESSAGE
            );
        }
    }

    @Test
    void testValidateWithErrorWhenPostCodeForAccusedIsFullPostcode() throws IOException {
        try (InputStream jsonInput = this.getClass().getClassLoader()
            .getResourceAsStream(SJP_PUBLIC_LIST_VALID_JSON)) {
            String text = new String(jsonInput.readAllBytes(), StandardCharsets.UTF_8);

            ObjectMapper mapper = new ObjectMapper();
            JsonNode node = mapper.readValue(text, JsonNode.class);

            String listJson = node.toString().replace("\"postCode\":\"AA1\"", "\"postCode\":\"AA1 1AA\"");
            assertThrows(PayloadValidationException.class, () ->
                             validationService.validateBody(listJson, headerGroup),
                         SJP_PUBLIC_INVALID_MESSAGE
            );
        }
    }

    @Test
    void testValidateWithErrorWhenForenameForAccusedIsNotInitial() throws IOException {
        try (InputStream jsonInput = this.getClass().getClassLoader()
            .getResourceAsStream(SJP_PUBLIC_LIST_VALID_JSON)) {
            String text = new String(jsonInput.readAllBytes(), StandardCharsets.UTF_8);

            ObjectMapper mapper = new ObjectMapper();
            JsonNode node = mapper.readValue(text, JsonNode.class);

            String listJson = node.toString().replace(
                "\"individualForenames\":\"A\"",
                "\"individualForenames\":\"ABC\""
            );
            assertThrows(PayloadValidationException.class, () ->
                             validationService.validateBody(listJson, headerGroup),
                         SJP_PUBLIC_INVALID_MESSAGE
            );
        }
    }

    @Test
    void testValidateWithSuccessWhenForenameForAccusedIsLowerCaseInitial() throws IOException {
        try (InputStream jsonInput = this.getClass().getClassLoader()
            .getResourceAsStream(SJP_PUBLIC_LIST_VALID_JSON)) {
            String text = new String(jsonInput.readAllBytes(), StandardCharsets.UTF_8);

            ObjectMapper mapper = new ObjectMapper();
            JsonNode node = mapper.readValue(text, JsonNode.class);

            String listJson = node.toString().replace(
                "\"individualForenames\":\"A\"",
                "\"individualForenames\":\"a\""
            );
            assertDoesNotThrow(() -> validationService.validateBody(listJson, headerGroup),
                               SJP_PUBLIC_VALID_MESSAGE);
        }
    }
}
