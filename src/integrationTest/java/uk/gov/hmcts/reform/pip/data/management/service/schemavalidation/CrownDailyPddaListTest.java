package uk.gov.hmcts.reform.pip.data.management.service.schemavalidation;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
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

import static org.junit.jupiter.api.Assertions.assertThrows;

@ActiveProfiles("integration-basic")
@SpringBootTest
@SuppressWarnings("PMD.TooManyMethods")
class CrownDailyPddaListTest extends IntegrationBasicTestBase {

    @Autowired
    ValidationService validationService;

    private static final String CROWN_DAILY_PDDA_LIST_VALID_JSON =
        "data/crown-daily-pdda-list/crownDailyPddaList.json";
    private static final String CROWN_DAILY_PDDA_LIST_INVALID_MESSAGE =
        "Invalid crown daily list marked as valid";

    private static final String DAILY_LIST_SCHEMA = "DailyList";
    private static final String DOCUMENT_ID = "DocumentID";
    private static final String UNIQUE_ID = "UniqueID";
    private static final String DOCUMENT_TYPE = "DocumentType";
    private static final String LIST_HEADER = "ListHeader";
    private static final String START_DATE = "StartDate";
    private static final String VERSION = "Version";
    private static final String PUBLISHED_TIME = "PublishedTime";
    private static final String CROWN_COURT = "CrownCourt";
    private static final String COURT_HOUSE_TYPE = "CourtHouseType";
    private static final String COURT_HOUSE_CODE = "CourtHouseCode";
    private static final String COURT_HOUSE_NAME = "CourtHouseName";
    private static final String COURT_LISTS = "CourtLists";
    private static final String COURT_HOUSE = "CourtHouse";
    private static final String SITTINGS = "Sittings";
    private static final String COURT_ROOM_NUMBER = "CourtRoomNumber";
    private static final String JUDICIARY = "Judiciary";
    private static final String JUDGE = "Judge";
    private static final String CITIZEN_SURNAME = "CitizenNameSurname";
    private static final String HEARINGS = "Hearings";
    private static final String HEARING_SEQUENCE_NUMBER = "HearingSequenceNumber";
    private static final String HEARING_DETAILS = "HearingDetails";
    private static final String HEARING_DESCRIPTION = "HearingDescription";
    private static final String CASE_NUMBER = "CaseNumber";
    private static final String ORGANISATION_NAME = "OrganisationName";
    private static final String OFFENCE_STATEMENT = "OffenceStatement";

    private static final String SOURCE_ARTEFACT_ID = "sourceArtefactId";
    private static final LocalDateTime DISPLAY_FROM = LocalDateTime.now();
    private static final LocalDateTime DISPLAY_TO = LocalDateTime.now();
    private static final Language LANGUAGE = Language.ENGLISH;
    private static final String PROVENANCE = "provenance";
    private static final Sensitivity SENSITIVITY = Sensitivity.PUBLIC;
    private static final ArtefactType ARTEFACT_TYPE = ArtefactType.LIST;
    private static final String COURT_ID = "123";
    private static final ListType LIST_TYPE = ListType.CROWN_DAILY_PDDA_LIST;
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
    void testValidateWithErrorsWhenDailyListMissingInCrownDailyPddaList() throws IOException {
        try (InputStream jsonInput = this.getClass().getClassLoader()
            .getResourceAsStream(CROWN_DAILY_PDDA_LIST_VALID_JSON)) {
            String text = new String(jsonInput.readAllBytes(), StandardCharsets.UTF_8);

            JsonNode node = getJsonNode(text);
            ((ObjectNode) node).remove(DAILY_LIST_SCHEMA);

            String listJson = node.toString();
            assertThrows(PayloadValidationException.class, () ->
                             validationService.validateBody(listJson, headerGroup, true),
                         CROWN_DAILY_PDDA_LIST_INVALID_MESSAGE);
        }
    }

    @Test
    void testValidateWithErrorsWhenDocumentIdMissingInCrownDailyPddaList() throws IOException {
        try (InputStream jsonInput = this.getClass().getClassLoader()
            .getResourceAsStream(CROWN_DAILY_PDDA_LIST_VALID_JSON)) {
            String text = new String(jsonInput.readAllBytes(), StandardCharsets.UTF_8);

            JsonNode node = getJsonNode(text);
            ((ObjectNode) node.get(DAILY_LIST_SCHEMA)).remove(DOCUMENT_ID);

            String listJson = node.toString();
            assertThrows(PayloadValidationException.class, () ->
                             validationService.validateBody(listJson, headerGroup, true),
                         CROWN_DAILY_PDDA_LIST_INVALID_MESSAGE);
        }
    }

    @Test
    void testValidateWithErrorsWhenUniqueIdMissingInCrownDailyPddaList() throws IOException {
        try (InputStream jsonInput = this.getClass().getClassLoader()
            .getResourceAsStream(CROWN_DAILY_PDDA_LIST_VALID_JSON)) {
            String text = new String(jsonInput.readAllBytes(), StandardCharsets.UTF_8);

            JsonNode node = getJsonNode(text);
            ((ObjectNode) node.get(DAILY_LIST_SCHEMA).get(DOCUMENT_ID)).remove(UNIQUE_ID);

            String listJson = node.toString();
            assertThrows(PayloadValidationException.class, () ->
                             validationService.validateBody(listJson, headerGroup, true),
                         CROWN_DAILY_PDDA_LIST_INVALID_MESSAGE);
        }
    }

    @Test
    void testValidateWithErrorsWhenDocumentTypeMissingInCrownDailyPddaList() throws IOException {
        try (InputStream jsonInput = this.getClass().getClassLoader()
            .getResourceAsStream(CROWN_DAILY_PDDA_LIST_VALID_JSON)) {
            String text = new String(jsonInput.readAllBytes(), StandardCharsets.UTF_8);

            JsonNode node = getJsonNode(text);
            ((ObjectNode) node.get(DAILY_LIST_SCHEMA).get(DOCUMENT_ID)).remove(DOCUMENT_TYPE);

            String listJson = node.toString();
            assertThrows(PayloadValidationException.class, () ->
                             validationService.validateBody(listJson, headerGroup, true),
                         CROWN_DAILY_PDDA_LIST_INVALID_MESSAGE);
        }
    }

    @Test
    void testValidateWithErrorsWhenListHeaderMissingInCrownDailyPddaList() throws IOException {
        try (InputStream jsonInput = this.getClass().getClassLoader()
            .getResourceAsStream(CROWN_DAILY_PDDA_LIST_VALID_JSON)) {
            String text = new String(jsonInput.readAllBytes(), StandardCharsets.UTF_8);

            JsonNode node = getJsonNode(text);
            ((ObjectNode) node.get(DAILY_LIST_SCHEMA)).remove(LIST_HEADER);

            String listJson = node.toString();
            assertThrows(PayloadValidationException.class, () ->
                             validationService.validateBody(listJson, headerGroup, true),
                         CROWN_DAILY_PDDA_LIST_INVALID_MESSAGE);
        }
    }

    @Test
    void testValidateWithErrorsWhenStartDateMissingInCrownDailyPddaList() throws IOException {
        try (InputStream jsonInput = this.getClass().getClassLoader()
            .getResourceAsStream(CROWN_DAILY_PDDA_LIST_VALID_JSON)) {
            String text = new String(jsonInput.readAllBytes(), StandardCharsets.UTF_8);

            JsonNode node = getJsonNode(text);
            ((ObjectNode) node.get(DAILY_LIST_SCHEMA).get(LIST_HEADER)).remove(START_DATE);

            String listJson = node.toString();
            assertThrows(PayloadValidationException.class, () ->
                             validationService.validateBody(listJson, headerGroup, true),
                         CROWN_DAILY_PDDA_LIST_INVALID_MESSAGE);
        }
    }

    @Test
    void testValidateWithErrorsWhenVersionMissingInCrownDailyPddaList() throws IOException {
        try (InputStream jsonInput = this.getClass().getClassLoader()
            .getResourceAsStream(CROWN_DAILY_PDDA_LIST_VALID_JSON)) {
            String text = new String(jsonInput.readAllBytes(), StandardCharsets.UTF_8);

            JsonNode node = getJsonNode(text);
            ((ObjectNode) node.get(DAILY_LIST_SCHEMA).get(LIST_HEADER)).remove(VERSION);

            String listJson = node.toString();
            assertThrows(PayloadValidationException.class, () ->
                             validationService.validateBody(listJson, headerGroup, true),
                         CROWN_DAILY_PDDA_LIST_INVALID_MESSAGE);
        }
    }

    @Test
    void testValidateWithErrorsWhenPublishedTimeMissingInCrownDailyPddaList() throws IOException {
        try (InputStream jsonInput = this.getClass().getClassLoader()
            .getResourceAsStream(CROWN_DAILY_PDDA_LIST_VALID_JSON)) {
            String text = new String(jsonInput.readAllBytes(), StandardCharsets.UTF_8);

            JsonNode node = getJsonNode(text);
            ((ObjectNode) node.get(DAILY_LIST_SCHEMA).get(LIST_HEADER)).remove(PUBLISHED_TIME);

            String listJson = node.toString();
            assertThrows(PayloadValidationException.class, () ->
                             validationService.validateBody(listJson, headerGroup, true),
                         CROWN_DAILY_PDDA_LIST_INVALID_MESSAGE);
        }
    }

    @Test
    void testValidateWithErrorsWhenCrownCourtMissingInCrownDailyPddaList() throws IOException {
        try (InputStream jsonInput = this.getClass().getClassLoader()
            .getResourceAsStream(CROWN_DAILY_PDDA_LIST_VALID_JSON)) {
            String text = new String(jsonInput.readAllBytes(), StandardCharsets.UTF_8);

            JsonNode node = getJsonNode(text);
            ((ObjectNode) node.get(DAILY_LIST_SCHEMA)).remove(CROWN_COURT);

            String listJson = node.toString();
            assertThrows(PayloadValidationException.class, () ->
                             validationService.validateBody(listJson, headerGroup, true),
                         CROWN_DAILY_PDDA_LIST_INVALID_MESSAGE);
        }
    }

    @Test
    void testValidateWithErrorsWhenCourtHouseTypeMissingInCrownDailyPddaList() throws IOException {
        try (InputStream jsonInput = this.getClass().getClassLoader()
            .getResourceAsStream(CROWN_DAILY_PDDA_LIST_VALID_JSON)) {
            String text = new String(jsonInput.readAllBytes(), StandardCharsets.UTF_8);

            JsonNode node = getJsonNode(text);
            ((ObjectNode) node.get(DAILY_LIST_SCHEMA).get(CROWN_COURT)).remove(COURT_HOUSE_TYPE);

            String listJson = node.toString();
            assertThrows(PayloadValidationException.class, () ->
                             validationService.validateBody(listJson, headerGroup, true),
                         CROWN_DAILY_PDDA_LIST_INVALID_MESSAGE);
        }
    }

    @Test
    void testValidateWithErrorsWhenCourtListCourtHouseTypeMissingInCrownDailyPddaList() throws IOException {
        try (InputStream jsonInput = this.getClass().getClassLoader()
            .getResourceAsStream(CROWN_DAILY_PDDA_LIST_VALID_JSON)) {
            String text = new String(jsonInput.readAllBytes(), StandardCharsets.UTF_8);

            JsonNode node = getJsonNode(text);
            ((ObjectNode) node.get(DAILY_LIST_SCHEMA).get(COURT_LISTS).get(0).get(COURT_HOUSE))
                .remove(COURT_HOUSE_TYPE);

            String listJson = node.toString();
            assertThrows(PayloadValidationException.class, () ->
                             validationService.validateBody(listJson, headerGroup, true),
                         CROWN_DAILY_PDDA_LIST_INVALID_MESSAGE);
        }
    }

    @Test
    void testValidateWithErrorsWhenCommittingCourtHouseTypeMissingInCrownDailyPddaList() throws IOException {
        try (InputStream jsonInput = this.getClass().getClassLoader()
            .getResourceAsStream(CROWN_DAILY_PDDA_LIST_VALID_JSON)) {
            String text = new String(jsonInput.readAllBytes(), StandardCharsets.UTF_8);

            JsonNode node = getJsonNode(text);

            ((ObjectNode) node.get(DAILY_LIST_SCHEMA).get(COURT_LISTS).get(0).get(SITTINGS).get(0).get(HEARINGS).get(0)
                .get("CommittingCourt")).remove(COURT_HOUSE_TYPE);

            String listJson = node.toString();
            assertThrows(PayloadValidationException.class, () ->
                             validationService.validateBody(listJson, headerGroup, true),
                         CROWN_DAILY_PDDA_LIST_INVALID_MESSAGE);
        }
    }

    @Test
    void testValidateWithErrorsWhenCourtHouseCodeMissingInCrownDailyPddaList() throws IOException {
        try (InputStream jsonInput = this.getClass().getClassLoader()
            .getResourceAsStream(CROWN_DAILY_PDDA_LIST_VALID_JSON)) {
            String text = new String(jsonInput.readAllBytes(), StandardCharsets.UTF_8);

            JsonNode node = getJsonNode(text);
            ((ObjectNode) node.get(DAILY_LIST_SCHEMA).get(CROWN_COURT)).remove(COURT_HOUSE_CODE);

            String listJson = node.toString();
            assertThrows(PayloadValidationException.class, () ->
                             validationService.validateBody(listJson, headerGroup, true),
                         CROWN_DAILY_PDDA_LIST_INVALID_MESSAGE);
        }
    }

    @Test
    void testValidateWithErrorsWhenCourtListsCourtHouseCodeMissingInCrownDailyPddaList() throws IOException {
        try (InputStream jsonInput = this.getClass().getClassLoader()
            .getResourceAsStream(CROWN_DAILY_PDDA_LIST_VALID_JSON)) {
            String text = new String(jsonInput.readAllBytes(), StandardCharsets.UTF_8);

            JsonNode node = getJsonNode(text);
            ((ObjectNode) node.get(DAILY_LIST_SCHEMA).get(COURT_LISTS).get(0).get(COURT_HOUSE))
                .remove(COURT_HOUSE_CODE);

            String listJson = node.toString();
            assertThrows(PayloadValidationException.class, () ->
                             validationService.validateBody(listJson, headerGroup, true),
                         CROWN_DAILY_PDDA_LIST_INVALID_MESSAGE);
        }
    }

    @Test
    void testValidateWithErrorsWhenCommittingCourtHouseCodeMissingInCrownDailyPddaList() throws IOException {
        try (InputStream jsonInput = this.getClass().getClassLoader()
            .getResourceAsStream(CROWN_DAILY_PDDA_LIST_VALID_JSON)) {
            String text = new String(jsonInput.readAllBytes(), StandardCharsets.UTF_8);

            JsonNode node = getJsonNode(text);
            ((ObjectNode) node.get(DAILY_LIST_SCHEMA).get(COURT_LISTS).get(0).get(SITTINGS).get(0).get(HEARINGS)
                .get(0).get("CommittingCourt")).remove(COURT_HOUSE_CODE);

            String listJson = node.toString();
            assertThrows(PayloadValidationException.class, () ->
                             validationService.validateBody(listJson, headerGroup, true),
                         CROWN_DAILY_PDDA_LIST_INVALID_MESSAGE);
        }
    }

    @Test
    void testValidateWithErrorsWhenCourtHouseNameMissingInCrownDailyPddaList() throws IOException {
        try (InputStream jsonInput = this.getClass().getClassLoader()
            .getResourceAsStream(CROWN_DAILY_PDDA_LIST_VALID_JSON)) {
            String text = new String(jsonInput.readAllBytes(), StandardCharsets.UTF_8);

            JsonNode node = getJsonNode(text);
            ((ObjectNode) node.get(DAILY_LIST_SCHEMA).get(CROWN_COURT)).remove(COURT_HOUSE_NAME);

            String listJson = node.toString();
            assertThrows(PayloadValidationException.class, () ->
                             validationService.validateBody(listJson, headerGroup, true),
                         CROWN_DAILY_PDDA_LIST_INVALID_MESSAGE);
        }
    }

    @Test
    void testValidateWithErrorsWhenCourtListsCourtHouseNameMissingInCrownDailyPddaList() throws IOException {
        try (InputStream jsonInput = this.getClass().getClassLoader()
            .getResourceAsStream(CROWN_DAILY_PDDA_LIST_VALID_JSON)) {
            String text = new String(jsonInput.readAllBytes(), StandardCharsets.UTF_8);

            JsonNode node = getJsonNode(text);
            ((ObjectNode) node.get(DAILY_LIST_SCHEMA).get(COURT_LISTS).get(0).get(COURT_HOUSE))
                .remove(COURT_HOUSE_NAME);

            String listJson = node.toString();
            assertThrows(PayloadValidationException.class, () ->
                             validationService.validateBody(listJson, headerGroup, true),
                         CROWN_DAILY_PDDA_LIST_INVALID_MESSAGE);
        }
    }

    @Test
    void testValidateWithErrorsWhenCommittingCourtHouseNameMissingInCrownDailyPddaList() throws IOException {
        try (InputStream jsonInput = this.getClass().getClassLoader()
            .getResourceAsStream(CROWN_DAILY_PDDA_LIST_VALID_JSON)) {
            String text = new String(jsonInput.readAllBytes(), StandardCharsets.UTF_8);

            JsonNode node = getJsonNode(text);
            ((ObjectNode) node.get(DAILY_LIST_SCHEMA).get(COURT_LISTS).get(0).get(SITTINGS).get(0).get(HEARINGS).get(0)
                .get("CommittingCourt")).remove(COURT_HOUSE_NAME);

            String listJson = node.toString();
            assertThrows(PayloadValidationException.class, () ->
                             validationService.validateBody(listJson, headerGroup, true),
                         CROWN_DAILY_PDDA_LIST_INVALID_MESSAGE);
        }
    }

    @Test
    void testValidateWithErrorsWhenCourtListsMissingInCrownDailyPddaList() throws IOException {
        try (InputStream jsonInput = this.getClass().getClassLoader()
            .getResourceAsStream(CROWN_DAILY_PDDA_LIST_VALID_JSON)) {
            String text = new String(jsonInput.readAllBytes(), StandardCharsets.UTF_8);

            JsonNode node = getJsonNode(text);
            ((ObjectNode) node.get(DAILY_LIST_SCHEMA)).remove(COURT_LISTS);

            String listJson = node.toString();
            assertThrows(PayloadValidationException.class, () ->
                             validationService.validateBody(listJson, headerGroup, true),
                         CROWN_DAILY_PDDA_LIST_INVALID_MESSAGE);
        }
    }

    @Test
    void testValidateWithErrorsWhenCourtHouseMissingInCrownDailyPddaList() throws IOException {
        try (InputStream jsonInput = this.getClass().getClassLoader()
            .getResourceAsStream(CROWN_DAILY_PDDA_LIST_VALID_JSON)) {
            String text = new String(jsonInput.readAllBytes(), StandardCharsets.UTF_8);

            JsonNode node = getJsonNode(text);
            ((ObjectNode) node.get(DAILY_LIST_SCHEMA).get(COURT_LISTS).get(0)).remove(COURT_HOUSE);

            String listJson = node.toString();
            assertThrows(PayloadValidationException.class, () ->
                             validationService.validateBody(listJson, headerGroup, true),
                         CROWN_DAILY_PDDA_LIST_INVALID_MESSAGE);
        }
    }

    @Test
    void testValidateWithErrorsWhenSittingsMissingInCrownDailyPddaList() throws IOException {
        try (InputStream jsonInput = this.getClass().getClassLoader()
            .getResourceAsStream(CROWN_DAILY_PDDA_LIST_VALID_JSON)) {
            String text = new String(jsonInput.readAllBytes(), StandardCharsets.UTF_8);

            JsonNode node = getJsonNode(text);
            ((ObjectNode) node.get(DAILY_LIST_SCHEMA).get(COURT_LISTS).get(0)).remove(SITTINGS);

            String listJson = node.toString();
            assertThrows(PayloadValidationException.class, () ->
                             validationService.validateBody(listJson, headerGroup, true),
                         CROWN_DAILY_PDDA_LIST_INVALID_MESSAGE);
        }
    }

    @Test
    void testValidateWithErrorsWhenCourtRoomNumberMissingInCrownDailyPddaList() throws IOException {
        try (InputStream jsonInput = this.getClass().getClassLoader()
            .getResourceAsStream(CROWN_DAILY_PDDA_LIST_VALID_JSON)) {
            String text = new String(jsonInput.readAllBytes(), StandardCharsets.UTF_8);

            JsonNode node = getJsonNode(text);
            ((ObjectNode) node.get(DAILY_LIST_SCHEMA).get(COURT_LISTS).get(0).get(SITTINGS).get(0))
                .remove(COURT_ROOM_NUMBER);

            String listJson = node.toString();
            assertThrows(PayloadValidationException.class, () ->
                             validationService.validateBody(listJson, headerGroup, true),
                         CROWN_DAILY_PDDA_LIST_INVALID_MESSAGE);
        }
    }

    @Test
    void testValidateWithErrorsWhenJudiciaryMissingInCrownDailyPddaList() throws IOException {
        try (InputStream jsonInput = this.getClass().getClassLoader()
            .getResourceAsStream(CROWN_DAILY_PDDA_LIST_VALID_JSON)) {
            String text = new String(jsonInput.readAllBytes(), StandardCharsets.UTF_8);

            JsonNode node = getJsonNode(text);
            ((ObjectNode) node.get(DAILY_LIST_SCHEMA).get(COURT_LISTS).get(0).get(SITTINGS).get(0)).remove(JUDICIARY);

            String listJson = node.toString();
            assertThrows(PayloadValidationException.class, () ->
                             validationService.validateBody(listJson, headerGroup, true),
                         CROWN_DAILY_PDDA_LIST_INVALID_MESSAGE);
        }
    }

    @Test
    void testValidateWithErrorsWhenJudgeMissingInCrownDailyPddaList() throws IOException {
        try (InputStream jsonInput = this.getClass().getClassLoader()
            .getResourceAsStream(CROWN_DAILY_PDDA_LIST_VALID_JSON)) {
            String text = new String(jsonInput.readAllBytes(), StandardCharsets.UTF_8);

            JsonNode node = getJsonNode(text);
            ((ObjectNode) node.get(DAILY_LIST_SCHEMA).get(COURT_LISTS).get(0).get(SITTINGS).get(0).get(JUDICIARY))
                .remove(JUDGE);

            String listJson = node.toString();
            assertThrows(PayloadValidationException.class, () ->
                             validationService.validateBody(listJson, headerGroup, true),
                         CROWN_DAILY_PDDA_LIST_INVALID_MESSAGE);
        }
    }

    @Test
    void testValidateWithErrorsWhenCitizenSurnameMissingInCrownDailyPddaList() throws IOException {
        try (InputStream jsonInput = this.getClass().getClassLoader()
            .getResourceAsStream(CROWN_DAILY_PDDA_LIST_VALID_JSON)) {
            String text = new String(jsonInput.readAllBytes(), StandardCharsets.UTF_8);

            JsonNode node = getJsonNode(text);
            ((ObjectNode) node.get(DAILY_LIST_SCHEMA).get(COURT_LISTS).get(0).get(SITTINGS).get(0).get(JUDICIARY)
                .get(JUDGE)).remove(CITIZEN_SURNAME);

            String listJson = node.toString();
            assertThrows(PayloadValidationException.class, () ->
                             validationService.validateBody(listJson, headerGroup, true),
                         CROWN_DAILY_PDDA_LIST_INVALID_MESSAGE);
        }
    }

    @Test
    void testValidateWithErrorsWhenHearingsMissingInCrownDailyPddaList() throws IOException {
        try (InputStream jsonInput = this.getClass().getClassLoader()
            .getResourceAsStream(CROWN_DAILY_PDDA_LIST_VALID_JSON)) {
            String text = new String(jsonInput.readAllBytes(), StandardCharsets.UTF_8);

            JsonNode node = getJsonNode(text);
            ((ObjectNode) node.get(DAILY_LIST_SCHEMA).get(COURT_LISTS).get(0).get(SITTINGS).get(0)).remove(HEARINGS);

            String listJson = node.toString();
            assertThrows(PayloadValidationException.class, () ->
                             validationService.validateBody(listJson, headerGroup, true),
                         CROWN_DAILY_PDDA_LIST_INVALID_MESSAGE);
        }
    }

    @Test
    void testValidateWithErrorsWhenHearingSequenceNumberMissingInCrownDailyPddaList() throws IOException {
        try (InputStream jsonInput = this.getClass().getClassLoader()
            .getResourceAsStream(CROWN_DAILY_PDDA_LIST_VALID_JSON)) {
            String text = new String(jsonInput.readAllBytes(), StandardCharsets.UTF_8);

            JsonNode node = getJsonNode(text);
            ((ObjectNode) node.get(DAILY_LIST_SCHEMA).get(COURT_LISTS).get(0).get(SITTINGS).get(0).get(HEARINGS)
                .get(0)).remove(HEARING_SEQUENCE_NUMBER);

            String listJson = node.toString();
            assertThrows(PayloadValidationException.class, () ->
                             validationService.validateBody(listJson, headerGroup, true),
                         CROWN_DAILY_PDDA_LIST_INVALID_MESSAGE);
        }
    }

    @Test
    void testValidateWithErrorsWhenHearingDetailsMissingInCrownDailyPddaList() throws IOException {
        try (InputStream jsonInput = this.getClass().getClassLoader()
            .getResourceAsStream(CROWN_DAILY_PDDA_LIST_VALID_JSON)) {
            String text = new String(jsonInput.readAllBytes(), StandardCharsets.UTF_8);

            JsonNode node = getJsonNode(text);
            ((ObjectNode) node.get(DAILY_LIST_SCHEMA).get(COURT_LISTS).get(0).get(SITTINGS).get(0).get(HEARINGS)
                .get(0)).remove(HEARING_DETAILS);

            String listJson = node.toString();
            assertThrows(PayloadValidationException.class, () ->
                             validationService.validateBody(listJson, headerGroup, true),
                         CROWN_DAILY_PDDA_LIST_INVALID_MESSAGE);
        }
    }

    @Test
    void testValidateWithErrorsWhenHearingDescriptionMissingInCrownDailyPddaList() throws IOException {
        try (InputStream jsonInput = this.getClass().getClassLoader()
            .getResourceAsStream(CROWN_DAILY_PDDA_LIST_VALID_JSON)) {
            String text = new String(jsonInput.readAllBytes(), StandardCharsets.UTF_8);

            JsonNode node = getJsonNode(text);
            ((ObjectNode) node.get(DAILY_LIST_SCHEMA).get(COURT_LISTS).get(0).get(SITTINGS).get(0).get(HEARINGS)
                .get(0).get(HEARING_DETAILS)).remove(HEARING_DESCRIPTION);

            String listJson = node.toString();
            assertThrows(PayloadValidationException.class, () ->
                             validationService.validateBody(listJson, headerGroup, true),
                         CROWN_DAILY_PDDA_LIST_INVALID_MESSAGE);
        }
    }

    @Test
    void testValidateWithErrorsWhenCaseNumberMissingInCrownDailyPddaList() throws IOException {
        try (InputStream jsonInput = this.getClass().getClassLoader()
            .getResourceAsStream(CROWN_DAILY_PDDA_LIST_VALID_JSON)) {
            String text = new String(jsonInput.readAllBytes(), StandardCharsets.UTF_8);

            JsonNode node = getJsonNode(text);
            ((ObjectNode) node.get(DAILY_LIST_SCHEMA).get(COURT_LISTS).get(0).get(SITTINGS).get(0).get(HEARINGS)
                .get(0)).remove(CASE_NUMBER);

            String listJson = node.toString();
            assertThrows(PayloadValidationException.class, () ->
                             validationService.validateBody(listJson, headerGroup, true),
                         CROWN_DAILY_PDDA_LIST_INVALID_MESSAGE);
        }
    }

    @Test
    void testValidateWithErrorsWhenOrganisationNameMissingInCrownDailyPddaList() throws IOException {
        try (InputStream jsonInput = this.getClass().getClassLoader()
            .getResourceAsStream(CROWN_DAILY_PDDA_LIST_VALID_JSON)) {
            String text = new String(jsonInput.readAllBytes(), StandardCharsets.UTF_8);

            JsonNode node = getJsonNode(text);
            ((ObjectNode) node.get(DAILY_LIST_SCHEMA).get(COURT_LISTS).get(0).get(SITTINGS).get(0).get(HEARINGS)
                .get(0).get("Prosecution").get("ProsecutingOrganisation")).remove(ORGANISATION_NAME);

            String listJson = node.toString();
            assertThrows(PayloadValidationException.class, () ->
                             validationService.validateBody(listJson, headerGroup, true),
                         CROWN_DAILY_PDDA_LIST_INVALID_MESSAGE);
        }
    }

    @Test
    void testValidateWithErrorsWhenOffenceStatementMissingInCrownDailyPddaList() throws IOException {
        try (InputStream jsonInput = this.getClass().getClassLoader()
            .getResourceAsStream(CROWN_DAILY_PDDA_LIST_VALID_JSON)) {
            String text = new String(jsonInput.readAllBytes(), StandardCharsets.UTF_8);

            JsonNode node = getJsonNode(text);
            ((ObjectNode) node.get(DAILY_LIST_SCHEMA).get(COURT_LISTS).get(0).get(SITTINGS).get(0).get(HEARINGS)
                .get(0).get("Defendants").get(0).get("Charges").get(0)).remove(OFFENCE_STATEMENT);

            String listJson = node.toString();
            assertThrows(PayloadValidationException.class, () ->
                             validationService.validateBody(listJson, headerGroup, true),
                         CROWN_DAILY_PDDA_LIST_INVALID_MESSAGE);
        }
    }

}
