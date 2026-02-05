package uk.gov.hmcts.reform.pip.data.management.service.schemavalidation;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.pip.data.management.errorhandling.exceptions.PayloadValidationException;
import uk.gov.hmcts.reform.pip.data.management.models.publication.HeaderGroup;
import uk.gov.hmcts.reform.pip.data.management.service.ValidationService;
import uk.gov.hmcts.reform.pip.data.management.utils.IntegrationBasicTestBase;
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

@ActiveProfiles("integration-basic")
@SpringBootTest
class MagistratesStandardListTest extends IntegrationBasicTestBase {

    @Autowired
    ValidationService validationService;

    private static final String MAGISTRATES_STANDARD_LIST_VALID_JSON =
        "data/magistrates-standard-list/magistratesStandardList.json";
    private static final String MAGISTRATES_STANDARD_LIST_WITH_NEW_LINES =
        "data/magistrates-standard-list/magistratesStandardListWithNewLines.json";
    private static final String MAGISTRATES_STANDARD_LIST_INVALID_MESSAGE =
        "Invalid magistrates standard list marked as valid";

    private static final String COURT_LIST_SCHEMA = "courtLists";
    private static final String COURT_HOUSE_SCHEMA = "courtHouse";
    private static final String COURT_ROOM_SCHEMA = "courtRoom";
    private static final String SESSION_SCHEMA = "session";
    private static final String SITTINGS_SCHEMA = "sittings";
    private static final String HEARING_SCHEMA = "hearing";
    private static final String CASE_SCHEMA  = "case";
    private static final String SOURCE_ARTEFACT_ID = "sourceArtefactId";
    private static final LocalDateTime DISPLAY_FROM = LocalDateTime.now();
    private static final LocalDateTime DISPLAY_TO = LocalDateTime.now();
    private static final Language LANGUAGE = Language.ENGLISH;
    private static final String PROVENANCE = "provenance";
    private static final Sensitivity SENSITIVITY = Sensitivity.PUBLIC;
    private static final ArtefactType ARTEFACT_TYPE = ArtefactType.LIST;
    private static final String COURT_ID = "123";
    private static final ListType LIST_TYPE = ListType.MAGISTRATES_STANDARD_LIST;
    private static final LocalDateTime CONTENT_DATE = LocalDateTime.now();
    private static final String PUBLICATION_DATE_REGEX = "\"publicationDate\":\"[^\"]+\"";
    private static final String DATE_OF_BIRTH_REGEX = "\"dateOfBirth\":\"[^\"]+\"";

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
    void testValidateWithErrorWhenDocumentMissingInMagistratesStandardList() throws IOException {
        try (InputStream jsonInput = this.getClass().getClassLoader()
            .getResourceAsStream(MAGISTRATES_STANDARD_LIST_VALID_JSON)) {
            String text = new String(jsonInput.readAllBytes(), StandardCharsets.UTF_8);

            JsonNode node = getJsonNode(text);
            ((ObjectNode) node).remove("document");

            String listJson = node.toString();
            assertThrows(PayloadValidationException.class, () ->
                             validationService.validateBody(listJson, headerGroup, true),
                         MAGISTRATES_STANDARD_LIST_INVALID_MESSAGE);
        }
    }

    @Test
    void testValidateWithErrorWhenPublicationDateMissingInMagistratesStandardList() throws IOException {
        try (InputStream jsonInput = this.getClass().getClassLoader()
            .getResourceAsStream(MAGISTRATES_STANDARD_LIST_VALID_JSON)) {
            String text = new String(jsonInput.readAllBytes(), StandardCharsets.UTF_8);

            ObjectMapper mapper = new ObjectMapper();
            JsonNode node = mapper.readValue(text, JsonNode.class);
            ((ObjectNode) node.get("document")).remove("publicationDate");

            String listJson = node.toString();
            assertThrows(PayloadValidationException.class, () ->
                             validationService.validateBody(listJson, headerGroup, true),
                         MAGISTRATES_STANDARD_LIST_INVALID_MESSAGE);
        }
    }

    @Test
    void testValidateWithErrorsWhenVenueMissingInMagistratesStandardList() throws IOException {
        try (InputStream jsonInput = this.getClass().getClassLoader()
            .getResourceAsStream(MAGISTRATES_STANDARD_LIST_VALID_JSON)) {
            String text = new String(jsonInput.readAllBytes(), StandardCharsets.UTF_8);

            JsonNode node = getJsonNode(text);
            ((ObjectNode) node).remove("venue");

            String listJson = node.toString();
            assertThrows(PayloadValidationException.class, () ->
                             validationService.validateBody(listJson, headerGroup, true),
                         MAGISTRATES_STANDARD_LIST_INVALID_MESSAGE);
        }
    }

    @Test
    void testValidateWithErrorsWhenCourtListMissingInMagistratesStandardList() throws IOException {
        try (InputStream jsonInput = this.getClass().getClassLoader()
            .getResourceAsStream(MAGISTRATES_STANDARD_LIST_VALID_JSON)) {
            String text = new String(jsonInput.readAllBytes(), StandardCharsets.UTF_8);

            JsonNode node = getJsonNode(text);
            ((ObjectNode) node).remove(COURT_LIST_SCHEMA);

            String listJson = node.toString();
            assertThrows(PayloadValidationException.class, () ->
                             validationService.validateBody(listJson, headerGroup, true),
                         MAGISTRATES_STANDARD_LIST_INVALID_MESSAGE);
        }
    }

    @Test
    void testValidateWithErrorsWhenCourtHouseMissingInMagistratesStandardList() throws IOException {
        try (InputStream jsonInput = this.getClass().getClassLoader()
            .getResourceAsStream(MAGISTRATES_STANDARD_LIST_VALID_JSON)) {
            String text = new String(jsonInput.readAllBytes(), StandardCharsets.UTF_8);

            ObjectMapper mapper = new ObjectMapper();
            JsonNode node = mapper.readValue(text, JsonNode.class);
            ((ObjectNode) node.get(COURT_LIST_SCHEMA).get(0)).remove(COURT_HOUSE_SCHEMA);

            String listJson = node.toString();
            assertThrows(PayloadValidationException.class, () ->
                             validationService.validateBody(listJson, headerGroup, true),
                         MAGISTRATES_STANDARD_LIST_INVALID_MESSAGE);
        }
    }

    @Test
    void testValidateWithErrorsWhenCourtRoomMissingInMagistratesStandardList() throws IOException {
        try (InputStream jsonInput = this.getClass().getClassLoader()
            .getResourceAsStream(MAGISTRATES_STANDARD_LIST_VALID_JSON)) {
            String text = new String(jsonInput.readAllBytes(), StandardCharsets.UTF_8);

            ObjectMapper mapper = new ObjectMapper();
            JsonNode node = mapper.readValue(text, JsonNode.class);
            ((ObjectNode) node.get(COURT_LIST_SCHEMA).get(0)
                .get(COURT_HOUSE_SCHEMA)).remove(COURT_ROOM_SCHEMA);

            String listJson = node.toString();
            assertThrows(PayloadValidationException.class, () ->
                             validationService.validateBody(listJson, headerGroup, true),
                         MAGISTRATES_STANDARD_LIST_INVALID_MESSAGE);
        }
    }

    @Test
    void testValidateWithErrorsWhenSessionMissingInMagistratesStandardList() throws IOException {
        try (InputStream jsonInput = this.getClass().getClassLoader()
            .getResourceAsStream(MAGISTRATES_STANDARD_LIST_VALID_JSON)) {
            String text = new String(jsonInput.readAllBytes(), StandardCharsets.UTF_8);

            ObjectMapper mapper = new ObjectMapper();
            JsonNode node = mapper.readValue(text, JsonNode.class);
            ((ObjectNode) node.get(COURT_LIST_SCHEMA).get(0)
                .get(COURT_HOUSE_SCHEMA).get(COURT_ROOM_SCHEMA).get(0)).remove(SESSION_SCHEMA);

            String listJson = node.toString();
            assertThrows(PayloadValidationException.class, () ->
                             validationService.validateBody(listJson, headerGroup, true),
                         MAGISTRATES_STANDARD_LIST_INVALID_MESSAGE);
        }
    }

    @Test
    void testValidateWithErrorsWhenSittingsMissingInMagistratesStandardList() throws IOException {
        try (InputStream jsonInput = this.getClass().getClassLoader()
            .getResourceAsStream(MAGISTRATES_STANDARD_LIST_VALID_JSON)) {
            String text = new String(jsonInput.readAllBytes(), StandardCharsets.UTF_8);

            ObjectMapper mapper = new ObjectMapper();
            JsonNode node = mapper.readValue(text, JsonNode.class);
            ((ObjectNode) node.get(COURT_LIST_SCHEMA).get(0)
                .get(COURT_HOUSE_SCHEMA).get(COURT_ROOM_SCHEMA).get(0)
                .get(SESSION_SCHEMA).get(0)).remove(SITTINGS_SCHEMA);

            String listJson = node.toString();
            assertThrows(PayloadValidationException.class, () ->
                             validationService.validateBody(listJson, headerGroup, true),
                         MAGISTRATES_STANDARD_LIST_INVALID_MESSAGE);
        }
    }

    @Test
    void testValidateWithErrorWhenSittingStartMissing() throws IOException {
        try (InputStream jsonInput = this.getClass().getClassLoader()
            .getResourceAsStream(MAGISTRATES_STANDARD_LIST_VALID_JSON)) {
            String text = new String(jsonInput.readAllBytes(), StandardCharsets.UTF_8);

            JsonNode node = getJsonNode(text);
            ((ObjectNode) node.get(COURT_LIST_SCHEMA).get(0)
                .get(COURT_HOUSE_SCHEMA).get(COURT_ROOM_SCHEMA).get(0)
                .get(SESSION_SCHEMA).get(0).get(SITTINGS_SCHEMA).get(0)).remove("sittingStart");

            String listJson = node.toString();
            assertThrows(PayloadValidationException.class, () ->
                validationService.validateBody(listJson, headerGroup, true),
                MAGISTRATES_STANDARD_LIST_INVALID_MESSAGE);
        }
    }

    @Test
    void testValidateWithErrorsWhenHearingMissingInMagistratesStandardList() throws IOException {
        try (InputStream jsonInput = this.getClass().getClassLoader()
            .getResourceAsStream(MAGISTRATES_STANDARD_LIST_VALID_JSON)) {
            String text = new String(jsonInput.readAllBytes(), StandardCharsets.UTF_8);

            ObjectMapper mapper = new ObjectMapper();
            JsonNode node = mapper.readValue(text, JsonNode.class);
            ((ObjectNode) node.get(COURT_LIST_SCHEMA).get(0)
                .get(COURT_HOUSE_SCHEMA).get(COURT_ROOM_SCHEMA).get(0)
                .get(SESSION_SCHEMA).get(0).get(SITTINGS_SCHEMA).get(0)).remove(HEARING_SCHEMA);

            String listJson = node.toString();
            assertThrows(PayloadValidationException.class, () ->
                             validationService.validateBody(listJson, headerGroup, true),
                         MAGISTRATES_STANDARD_LIST_INVALID_MESSAGE);
        }
    }

    @Test
    void testValidateWithSuccessWhenFieldsContainNewLineCharacters() throws IOException {
        try (InputStream jsonInput = this.getClass().getClassLoader()
            .getResourceAsStream(MAGISTRATES_STANDARD_LIST_WITH_NEW_LINES)) {
            String text = new String(jsonInput.readAllBytes(), StandardCharsets.UTF_8);

            ObjectMapper mapper = new ObjectMapper();
            String listJson = mapper.readValue(text, JsonNode.class).toString();
            assertDoesNotThrow(() -> validationService.validateBody(listJson, headerGroup, true),
                               MAGISTRATES_STANDARD_LIST_INVALID_MESSAGE);
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "2024-10-01T09:30:15Z",
        "2024-10-01T09:30:15.123Z",
        "2024-10-01T09:30:15.123456789Z",
    })
    void testValidateWithSuccessWhenValidPublicationDateFormat(String publicationDate) throws IOException {
        try (InputStream jsonInput = this.getClass().getClassLoader()
            .getResourceAsStream(MAGISTRATES_STANDARD_LIST_VALID_JSON)) {
            String text = new String(jsonInput.readAllBytes(), StandardCharsets.UTF_8);

            ObjectMapper mapper = new ObjectMapper();
            JsonNode node = mapper.readValue(text, JsonNode.class);

            String listJson = node.toString()
                .replaceAll(PUBLICATION_DATE_REGEX, String.format("\"publicationDate\":\"%s\"", publicationDate));
            assertDoesNotThrow(() -> validationService.validateBody(listJson, headerGroup, true),
                               MAGISTRATES_STANDARD_LIST_INVALID_MESSAGE);
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "2024-10-1T09:30:15Z",
        "2024-10-01T09:30:15.1234567890Z",
        "2024-10-01T09:30:15.123",
        ""
    })
    void testValidateWithErrorWhenInvalidPublicationDateFormat(String publicationDate) throws IOException {
        try (InputStream jsonInput = this.getClass().getClassLoader()
            .getResourceAsStream(MAGISTRATES_STANDARD_LIST_VALID_JSON)) {
            String text = new String(jsonInput.readAllBytes(), StandardCharsets.UTF_8);

            ObjectMapper mapper = new ObjectMapper();
            JsonNode node = mapper.readValue(text, JsonNode.class);

            String listJson = node.toString()
                .replaceAll(PUBLICATION_DATE_REGEX, String.format("\"publicationDate\":\"%s\"", publicationDate));
            assertThrows(PayloadValidationException.class,
                         () -> validationService.validateBody(listJson, headerGroup, true),
                         MAGISTRATES_STANDARD_LIST_INVALID_MESSAGE);
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "01/01/1950",
        "2026-02-29",
    })
    void testValidateWithErrorWhenInvalidDateOfBirthFormat(String dateOfBirth) throws IOException {
        try (InputStream jsonInput = this.getClass().getClassLoader()
            .getResourceAsStream(MAGISTRATES_STANDARD_LIST_VALID_JSON)) {
            String text = new String(jsonInput.readAllBytes(), StandardCharsets.UTF_8);

            ObjectMapper mapper = new ObjectMapper();
            JsonNode node = mapper.readValue(text, JsonNode.class);

            String listJson = node.toString()
                .replaceAll(DATE_OF_BIRTH_REGEX, String.format("\"dateOfBirth\":\"%s\"", dateOfBirth));
            assertThrows(PayloadValidationException.class,
                         () -> validationService.validateBody(listJson, headerGroup, true),
                         MAGISTRATES_STANDARD_LIST_INVALID_MESSAGE);
        }
    }

    @Test
    void testValidateWithErrorWhenCourtRoomNameMissing() throws IOException {
        try (InputStream jsonInput = this.getClass().getClassLoader()
            .getResourceAsStream(MAGISTRATES_STANDARD_LIST_VALID_JSON)) {
            String text = new String(jsonInput.readAllBytes(), StandardCharsets.UTF_8);

            JsonNode node = getJsonNode(text);
            ((ObjectNode) node.get(COURT_LIST_SCHEMA).get(0)
                .get(COURT_HOUSE_SCHEMA).get(COURT_ROOM_SCHEMA).get(0)).remove("courtRoomName");

            String listJson = node.toString();
            assertThrows(PayloadValidationException.class, () ->
                validationService.validateBody(listJson, headerGroup, true),
                MAGISTRATES_STANDARD_LIST_INVALID_MESSAGE);
        }
    }

    @Test
    void testValidateWithErrorWhenCaseUrnMissing() throws IOException {
        try (InputStream jsonInput = this.getClass().getClassLoader()
            .getResourceAsStream(MAGISTRATES_STANDARD_LIST_VALID_JSON)) {
            String text = new String(jsonInput.readAllBytes(), StandardCharsets.UTF_8);

            JsonNode node = getJsonNode(text);
            ((ObjectNode) node.get(COURT_LIST_SCHEMA).get(0)
                .get(COURT_HOUSE_SCHEMA).get(COURT_ROOM_SCHEMA).get(0)
                .get(SESSION_SCHEMA).get(0).get(SITTINGS_SCHEMA).get(0)
                .get(HEARING_SCHEMA).get(0).get(CASE_SCHEMA).get(0)).remove("caseUrn");

            String listJson = node.toString();
            assertThrows(PayloadValidationException.class, () ->
                validationService.validateBody(listJson, headerGroup, true),
                MAGISTRATES_STANDARD_LIST_INVALID_MESSAGE);
        }
    }

    @Test
    void testValidateWithErrorWhenOrganisationNameMissing() throws IOException {
        try (InputStream jsonInput = this.getClass().getClassLoader()
            .getResourceAsStream(MAGISTRATES_STANDARD_LIST_VALID_JSON)) {
            String text = new String(jsonInput.readAllBytes(), StandardCharsets.UTF_8);

            JsonNode node = getJsonNode(text);
            ((ObjectNode) node.get(COURT_LIST_SCHEMA).get(0)
                .get(COURT_HOUSE_SCHEMA).get(COURT_ROOM_SCHEMA).get(0)
                .get(SESSION_SCHEMA).get(0).get(SITTINGS_SCHEMA).get(0)
                .get(HEARING_SCHEMA).get(0).get(CASE_SCHEMA).get(0)
                .get("party").get(1).get("organisationDetails")).remove("organisationName");

            String listJson = node.toString();
            assertThrows(PayloadValidationException.class, () ->
                validationService.validateBody(listJson, headerGroup, true),
                MAGISTRATES_STANDARD_LIST_INVALID_MESSAGE);
        }
    }

    @Test
    void testValidateWithErrorWhenCourtListsMissing() throws IOException {
        try (InputStream jsonInput = this.getClass().getClassLoader()
            .getResourceAsStream(MAGISTRATES_STANDARD_LIST_VALID_JSON)) {
            String text = new String(jsonInput.readAllBytes(), StandardCharsets.UTF_8);

            JsonNode node = getJsonNode(text);
            ((ObjectNode) node).remove(COURT_LIST_SCHEMA);

            String listJson = node.toString();
            assertThrows(PayloadValidationException.class, () ->
                validationService.validateBody(listJson, headerGroup, true),
                MAGISTRATES_STANDARD_LIST_INVALID_MESSAGE);
        }
    }

    @Test
    void testValidateWithErrorWhenApplicationReferenceMissing() throws IOException {
        try (InputStream jsonInput = this.getClass().getClassLoader()
            .getResourceAsStream(MAGISTRATES_STANDARD_LIST_VALID_JSON)) {
            String text = new String(jsonInput.readAllBytes(), StandardCharsets.UTF_8);

            JsonNode node = getJsonNode(text);
            ((ObjectNode) node.get(COURT_LIST_SCHEMA).get(0)
                .get(COURT_HOUSE_SCHEMA).get(COURT_ROOM_SCHEMA).get(0)
                .get(SESSION_SCHEMA).get(0).get(SITTINGS_SCHEMA).get(0)
                .get(HEARING_SCHEMA).get(0).get("application").get(0)).remove("applicationReference");

            String listJson = node.toString();
            assertThrows(PayloadValidationException.class, () ->
                validationService.validateBody(listJson, headerGroup, true),
                MAGISTRATES_STANDARD_LIST_INVALID_MESSAGE);
        }
    }
}
