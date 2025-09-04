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
@SuppressWarnings("PMD.TooManyMethods") // Each method is a separate test case
class CrownWarnedPddaListTest extends IntegrationBasicTestBase {

    @Autowired
    ValidationService validationService;

    private static final String CROWN_WARNED_PDDA_LIST_VALID_JSON =
        "data/crown-warned-pdda-list/crownWarnedPddaList.json";
    private static final String CROWN_WARNED_PDDA_LIST_INVALID_MESSAGE =
        "Invalid crown warned list marked as valid";

    private static final String WARNED_LIST_SCHEMA = "WarnedList";
    private static final String DOCUMENT_ID = "DocumentID";
    private static final String DOCUMENT_NAME = "DocumentName";
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
    private static final String WITH_FIXED_DATE = "WithFixedDate";
    private static final String WITHOUT_FIXED_DATE = "WithoutFixedDate";
    private static final String FIXTURE = "Fixture";
    private static final String CASES = "Cases";
    private static final String DEFENDANTS = "Defendants";
    private static final String PERSONAL_DETAILS = "PersonalDetails";
    private static final String NAME = "Name";
    private static final String IS_MASKED = "IsMasked";
    private static final String LOCATION = "Location";
    private static final String HEARING = "Hearing";
    private static final String HEARING_TYPE = "HearingType";
    private static final String HEARING_DESCRIPTION = "HearingDescription";
    private static final String CASE_NUMBER = "CaseNumber";
    private static final String CASE_NUMBER_CATH = "CaseNumberCaTH";
    private static final String CASE_ARRIVED_FROM = "CaseArrivedFrom";
    private static final String ORIGINATING_COURT = "OriginatingCourt";
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
    void testValidateWithErrorsWhenWarnedListMissingInCrownWarnedPddaList() throws IOException {
        try (InputStream jsonInput = this.getClass().getClassLoader()
            .getResourceAsStream(CROWN_WARNED_PDDA_LIST_VALID_JSON)) {
            String text = new String(jsonInput.readAllBytes(), StandardCharsets.UTF_8);

            JsonNode node = getJsonNode(text);
            ((ObjectNode) node).remove(WARNED_LIST_SCHEMA);

            String listJson = node.toString();
            assertThrows(PayloadValidationException.class, () ->
                             validationService.validateBody(listJson, headerGroup, true),
                         CROWN_WARNED_PDDA_LIST_INVALID_MESSAGE);
        }
    }

    @Test
    void testValidateWithErrorsWhenDocumentIdMissingInCrownWarnedPddaList() throws IOException {
        try (InputStream jsonInput = this.getClass().getClassLoader()
            .getResourceAsStream(CROWN_WARNED_PDDA_LIST_VALID_JSON)) {
            String text = new String(jsonInput.readAllBytes(), StandardCharsets.UTF_8);

            JsonNode node = getJsonNode(text);
            ((ObjectNode) node.get(WARNED_LIST_SCHEMA)).remove(DOCUMENT_ID);

            String listJson = node.toString();
            assertThrows(PayloadValidationException.class, () ->
                             validationService.validateBody(listJson, headerGroup, true),
                         CROWN_WARNED_PDDA_LIST_INVALID_MESSAGE);
        }
    }

    @Test
    void testValidateWithErrorsWhenDocumentNameMissingInCrownWarnedPddaList() throws IOException {
        try (InputStream jsonInput = this.getClass().getClassLoader()
            .getResourceAsStream(CROWN_WARNED_PDDA_LIST_VALID_JSON)) {
            String text = new String(jsonInput.readAllBytes(), StandardCharsets.UTF_8);

            JsonNode node = getJsonNode(text);
            ((ObjectNode) node.get(WARNED_LIST_SCHEMA).get(DOCUMENT_ID)).remove(DOCUMENT_NAME);

            String listJson = node.toString();
            assertThrows(PayloadValidationException.class, () ->
                             validationService.validateBody(listJson, headerGroup, true),
                         CROWN_WARNED_PDDA_LIST_INVALID_MESSAGE);
        }
    }

    @Test
    void testValidateWithErrorsWhenUniqueIdMissingInCrownWarnedPddaList() throws IOException {
        try (InputStream jsonInput = this.getClass().getClassLoader()
            .getResourceAsStream(CROWN_WARNED_PDDA_LIST_VALID_JSON)) {
            String text = new String(jsonInput.readAllBytes(), StandardCharsets.UTF_8);

            JsonNode node = getJsonNode(text);
            ((ObjectNode) node.get(WARNED_LIST_SCHEMA).get(DOCUMENT_ID)).remove(UNIQUE_ID);

            String listJson = node.toString();
            assertThrows(PayloadValidationException.class, () ->
                             validationService.validateBody(listJson, headerGroup, true),
                         CROWN_WARNED_PDDA_LIST_INVALID_MESSAGE);
        }
    }

    @Test
    void testValidateWithErrorsWhenDocumentTypeMissingInCrownWarnedPddaList() throws IOException {
        try (InputStream jsonInput = this.getClass().getClassLoader()
            .getResourceAsStream(CROWN_WARNED_PDDA_LIST_VALID_JSON)) {
            String text = new String(jsonInput.readAllBytes(), StandardCharsets.UTF_8);

            JsonNode node = getJsonNode(text);
            ((ObjectNode) node.get(WARNED_LIST_SCHEMA).get(DOCUMENT_ID)).remove(DOCUMENT_TYPE);

            String listJson = node.toString();
            assertThrows(PayloadValidationException.class, () ->
                             validationService.validateBody(listJson, headerGroup, true),
                         CROWN_WARNED_PDDA_LIST_INVALID_MESSAGE);
        }
    }

    @Test
    void testValidateWithErrorsWhenListHeaderMissingInCrownWarnedPddaList() throws IOException {
        try (InputStream jsonInput = this.getClass().getClassLoader()
            .getResourceAsStream(CROWN_WARNED_PDDA_LIST_VALID_JSON)) {
            String text = new String(jsonInput.readAllBytes(), StandardCharsets.UTF_8);

            JsonNode node = getJsonNode(text);
            ((ObjectNode) node.get(WARNED_LIST_SCHEMA)).remove(LIST_HEADER);

            String listJson = node.toString();
            assertThrows(PayloadValidationException.class, () ->
                             validationService.validateBody(listJson, headerGroup, true),
                         CROWN_WARNED_PDDA_LIST_INVALID_MESSAGE);
        }
    }

    @Test
    void testValidateWithErrorsWhenStartDateMissingInCrownWarnedPddaList() throws IOException {
        try (InputStream jsonInput = this.getClass().getClassLoader()
            .getResourceAsStream(CROWN_WARNED_PDDA_LIST_VALID_JSON)) {
            String text = new String(jsonInput.readAllBytes(), StandardCharsets.UTF_8);

            JsonNode node = getJsonNode(text);
            ((ObjectNode) node.get(WARNED_LIST_SCHEMA).get(LIST_HEADER)).remove(START_DATE);

            String listJson = node.toString();
            assertThrows(PayloadValidationException.class, () ->
                             validationService.validateBody(listJson, headerGroup, true),
                         CROWN_WARNED_PDDA_LIST_INVALID_MESSAGE);
        }
    }

    @Test
    void testValidateWithErrorsWhenVersionMissingInCrownWarnedPddaList() throws IOException {
        try (InputStream jsonInput = this.getClass().getClassLoader()
            .getResourceAsStream(CROWN_WARNED_PDDA_LIST_VALID_JSON)) {
            String text = new String(jsonInput.readAllBytes(), StandardCharsets.UTF_8);

            JsonNode node = getJsonNode(text);
            ((ObjectNode) node.get(WARNED_LIST_SCHEMA).get(LIST_HEADER)).remove(VERSION);

            String listJson = node.toString();
            assertThrows(PayloadValidationException.class, () ->
                             validationService.validateBody(listJson, headerGroup, true),
                         CROWN_WARNED_PDDA_LIST_INVALID_MESSAGE);
        }
    }

    @Test
    void testValidateWithErrorsWhenPublishedTimeMissingInCrownWarnedPddaList() throws IOException {
        try (InputStream jsonInput = this.getClass().getClassLoader()
            .getResourceAsStream(CROWN_WARNED_PDDA_LIST_VALID_JSON)) {
            String text = new String(jsonInput.readAllBytes(), StandardCharsets.UTF_8);

            JsonNode node = getJsonNode(text);
            ((ObjectNode) node.get(WARNED_LIST_SCHEMA).get(LIST_HEADER)).remove(PUBLISHED_TIME);

            String listJson = node.toString();
            assertThrows(PayloadValidationException.class, () ->
                             validationService.validateBody(listJson, headerGroup, true),
                         CROWN_WARNED_PDDA_LIST_INVALID_MESSAGE);
        }
    }

    @Test
    void testValidateWithErrorsWhenCrownCourtMissingInCrownWarnedPddaList() throws IOException {
        try (InputStream jsonInput = this.getClass().getClassLoader()
            .getResourceAsStream(CROWN_WARNED_PDDA_LIST_VALID_JSON)) {
            String text = new String(jsonInput.readAllBytes(), StandardCharsets.UTF_8);

            JsonNode node = getJsonNode(text);
            ((ObjectNode) node.get(WARNED_LIST_SCHEMA)).remove(CROWN_COURT);

            String listJson = node.toString();
            assertThrows(PayloadValidationException.class, () ->
                             validationService.validateBody(listJson, headerGroup, true),
                         CROWN_WARNED_PDDA_LIST_INVALID_MESSAGE);
        }
    }

    @Test
    void testValidateWithErrorsWhenCourtHouseTypeMissingInCrownWarnedPddaList() throws IOException {
        try (InputStream jsonInput = this.getClass().getClassLoader()
            .getResourceAsStream(CROWN_WARNED_PDDA_LIST_VALID_JSON)) {
            String text = new String(jsonInput.readAllBytes(), StandardCharsets.UTF_8);

            JsonNode node = getJsonNode(text);
            ((ObjectNode) node.get(WARNED_LIST_SCHEMA).get(CROWN_COURT)).remove(COURT_HOUSE_TYPE);

            String listJson = node.toString();
            assertThrows(PayloadValidationException.class, () ->
                             validationService.validateBody(listJson, headerGroup, true),
                         CROWN_WARNED_PDDA_LIST_INVALID_MESSAGE);
        }
    }

    @Test
    void testValidateWithErrorsWhenCourtListsCourtHouseTypeMissingInCrownWarnedPddaList() throws IOException {
        try (InputStream jsonInput = this.getClass().getClassLoader()
            .getResourceAsStream(CROWN_WARNED_PDDA_LIST_VALID_JSON)) {
            String text = new String(jsonInput.readAllBytes(), StandardCharsets.UTF_8);

            JsonNode node = getJsonNode(text);
            ((ObjectNode) node.get(WARNED_LIST_SCHEMA).get(COURT_LISTS).get(0).get(COURT_HOUSE))
                .remove(COURT_HOUSE_TYPE);

            String listJson = node.toString();
            assertThrows(PayloadValidationException.class, () ->
                             validationService.validateBody(listJson, headerGroup, true),
                         CROWN_WARNED_PDDA_LIST_INVALID_MESSAGE);
        }
    }

    @Test
    void testValidateWithErrorsWhenCourtHouseCodeMissingInCrownWarnedPddaList() throws IOException {
        try (InputStream jsonInput = this.getClass().getClassLoader()
            .getResourceAsStream(CROWN_WARNED_PDDA_LIST_VALID_JSON)) {
            String text = new String(jsonInput.readAllBytes(), StandardCharsets.UTF_8);

            JsonNode node = getJsonNode(text);
            ((ObjectNode) node.get(WARNED_LIST_SCHEMA).get(CROWN_COURT)).remove(COURT_HOUSE_CODE);

            String listJson = node.toString();
            assertThrows(PayloadValidationException.class, () ->
                             validationService.validateBody(listJson, headerGroup, true),
                         CROWN_WARNED_PDDA_LIST_INVALID_MESSAGE);
        }
    }

    @Test
    void testValidateWithErrorsWhenCourtListsCourtHouseCodeMissingInCrownWarnedPddaList() throws IOException {
        try (InputStream jsonInput = this.getClass().getClassLoader()
            .getResourceAsStream(CROWN_WARNED_PDDA_LIST_VALID_JSON)) {
            String text = new String(jsonInput.readAllBytes(), StandardCharsets.UTF_8);

            JsonNode node = getJsonNode(text);
            ((ObjectNode) node.get(WARNED_LIST_SCHEMA).get(COURT_LISTS).get(0).get(COURT_HOUSE))
                .remove(COURT_HOUSE_CODE);

            String listJson = node.toString();
            assertThrows(PayloadValidationException.class, () ->
                             validationService.validateBody(listJson, headerGroup, true),
                         CROWN_WARNED_PDDA_LIST_INVALID_MESSAGE);
        }
    }

    @Test
    void testValidateWithErrorsWhenCourtHouseNameMissingInCrownWarnedPddaList() throws IOException {
        try (InputStream jsonInput = this.getClass().getClassLoader()
            .getResourceAsStream(CROWN_WARNED_PDDA_LIST_VALID_JSON)) {
            String text = new String(jsonInput.readAllBytes(), StandardCharsets.UTF_8);

            JsonNode node = getJsonNode(text);
            ((ObjectNode) node.get(WARNED_LIST_SCHEMA).get(CROWN_COURT)).remove(COURT_HOUSE_NAME);

            String listJson = node.toString();
            assertThrows(PayloadValidationException.class, () ->
                             validationService.validateBody(listJson, headerGroup, true),
                         CROWN_WARNED_PDDA_LIST_INVALID_MESSAGE);
        }
    }

    @Test
    void testValidateWithErrorsWhenCourtListsCourtHouseNameMissingInCrownWarnedPddaList() throws IOException {
        try (InputStream jsonInput = this.getClass().getClassLoader()
            .getResourceAsStream(CROWN_WARNED_PDDA_LIST_VALID_JSON)) {
            String text = new String(jsonInput.readAllBytes(), StandardCharsets.UTF_8);

            JsonNode node = getJsonNode(text);
            ((ObjectNode) node.get(WARNED_LIST_SCHEMA).get(COURT_LISTS).get(0).get(COURT_HOUSE))
                .remove(COURT_HOUSE_NAME);

            String listJson = node.toString();
            assertThrows(PayloadValidationException.class, () ->
                             validationService.validateBody(listJson, headerGroup, true),
                         CROWN_WARNED_PDDA_LIST_INVALID_MESSAGE);
        }
    }

    @Test
    void testValidateWithErrorsWhenCourtListsMissingInCrownWarnedPddaList() throws IOException {
        try (InputStream jsonInput = this.getClass().getClassLoader()
            .getResourceAsStream(CROWN_WARNED_PDDA_LIST_VALID_JSON)) {
            String text = new String(jsonInput.readAllBytes(), StandardCharsets.UTF_8);

            JsonNode node = getJsonNode(text);
            ((ObjectNode) node.get(WARNED_LIST_SCHEMA)).remove(COURT_LISTS);

            String listJson = node.toString();
            assertThrows(PayloadValidationException.class, () ->
                             validationService.validateBody(listJson, headerGroup, true),
                         CROWN_WARNED_PDDA_LIST_INVALID_MESSAGE);
        }
    }

    @Test
    void testValidateWithErrorsWhenCourtHouseMissingInCrownWarnedPddaList() throws IOException {
        try (InputStream jsonInput = this.getClass().getClassLoader()
            .getResourceAsStream(CROWN_WARNED_PDDA_LIST_VALID_JSON)) {
            String text = new String(jsonInput.readAllBytes(), StandardCharsets.UTF_8);

            JsonNode node = getJsonNode(text);
            ((ObjectNode) node.get(WARNED_LIST_SCHEMA).get(COURT_LISTS).get(0)).remove(COURT_HOUSE);

            String listJson = node.toString();
            assertThrows(PayloadValidationException.class, () ->
                             validationService.validateBody(listJson, headerGroup, true),
                         CROWN_WARNED_PDDA_LIST_INVALID_MESSAGE);
        }
    }

    @Test
    void testValidateWithErrorsWhenCourtListCourtHouseTypeMissingInCrownWarnedPddaList() throws IOException {
        try (InputStream jsonInput = this.getClass().getClassLoader()
            .getResourceAsStream(CROWN_WARNED_PDDA_LIST_VALID_JSON)) {
            String text = new String(jsonInput.readAllBytes(), StandardCharsets.UTF_8);

            JsonNode node = getJsonNode(text);
            ((ObjectNode) node.get(WARNED_LIST_SCHEMA).get(COURT_LISTS).get(0).get(COURT_HOUSE))
                .remove(COURT_HOUSE_TYPE);

            String listJson = node.toString();
            assertThrows(PayloadValidationException.class, () ->
                             validationService.validateBody(listJson, headerGroup, true),
                         CROWN_WARNED_PDDA_LIST_INVALID_MESSAGE);
        }
    }

    @Test
    void testValidateWithErrorsWhenCourtListCourtHouseCodeMissingInCrownWarnedPddaList() throws IOException {
        try (InputStream jsonInput = this.getClass().getClassLoader()
            .getResourceAsStream(CROWN_WARNED_PDDA_LIST_VALID_JSON)) {
            String text = new String(jsonInput.readAllBytes(), StandardCharsets.UTF_8);

            JsonNode node = getJsonNode(text);
            ((ObjectNode) node.get(WARNED_LIST_SCHEMA).get(COURT_LISTS).get(0).get(COURT_HOUSE))
                .remove(COURT_HOUSE_CODE);

            String listJson = node.toString();
            assertThrows(PayloadValidationException.class, () ->
                             validationService.validateBody(listJson, headerGroup, true),
                         CROWN_WARNED_PDDA_LIST_INVALID_MESSAGE);
        }
    }

    @Test
    void testValidateWithErrorsWhenCourtListCourtHouseNameMissingInCrownWarnedPddaList() throws IOException {
        try (InputStream jsonInput = this.getClass().getClassLoader()
            .getResourceAsStream(CROWN_WARNED_PDDA_LIST_VALID_JSON)) {
            String text = new String(jsonInput.readAllBytes(), StandardCharsets.UTF_8);

            JsonNode node = getJsonNode(text);
            ((ObjectNode) node.get(WARNED_LIST_SCHEMA).get(COURT_LISTS).get(0).get(COURT_HOUSE))
                .remove(COURT_HOUSE_NAME);

            String listJson = node.toString();
            assertThrows(PayloadValidationException.class, () ->
                             validationService.validateBody(listJson, headerGroup, true),
                         CROWN_WARNED_PDDA_LIST_INVALID_MESSAGE);
        }
    }

    @Test
    void testValidateWithErrorsWhenWithFixedDateHearingTypeMissingInCrownWarnedPddaList() throws IOException {
        try (InputStream jsonInput = this.getClass().getClassLoader()
            .getResourceAsStream(CROWN_WARNED_PDDA_LIST_VALID_JSON)) {
            String text = new String(jsonInput.readAllBytes(), StandardCharsets.UTF_8);

            JsonNode node = getJsonNode(text);
            ((ObjectNode) node.get(WARNED_LIST_SCHEMA).get(COURT_LISTS).get(0).get(WITH_FIXED_DATE))
                .remove(HEARING_TYPE);

            String listJson = node.toString();
            assertThrows(PayloadValidationException.class, () ->
                             validationService.validateBody(listJson, headerGroup, true),
                         CROWN_WARNED_PDDA_LIST_INVALID_MESSAGE);
        }
    }

    @Test
    void testValidateWithErrorsWhenWithFixedDateFixtureMissingInCrownWarnedPddaList() throws IOException {
        try (InputStream jsonInput = this.getClass().getClassLoader()
            .getResourceAsStream(CROWN_WARNED_PDDA_LIST_VALID_JSON)) {
            String text = new String(jsonInput.readAllBytes(), StandardCharsets.UTF_8);

            JsonNode node = getJsonNode(text);
            ((ObjectNode) node.get(WARNED_LIST_SCHEMA).get(COURT_LISTS).get(0).get(WITH_FIXED_DATE)).remove(FIXTURE);

            String listJson = node.toString();
            assertThrows(PayloadValidationException.class, () ->
                             validationService.validateBody(listJson, headerGroup, true),
                         CROWN_WARNED_PDDA_LIST_INVALID_MESSAGE);
        }
    }

    @Test
    void testValidateWithErrorsWhenWithFixedDateCasesMissingInCrownWarnedPddaList() throws IOException {
        try (InputStream jsonInput = this.getClass().getClassLoader()
            .getResourceAsStream(CROWN_WARNED_PDDA_LIST_VALID_JSON)) {
            String text = new String(jsonInput.readAllBytes(), StandardCharsets.UTF_8);

            JsonNode node = getJsonNode(text);
            ((ObjectNode) node.get(WARNED_LIST_SCHEMA).get(COURT_LISTS).get(0).get(WITH_FIXED_DATE).get(FIXTURE))
                .remove(CASES);

            String listJson = node.toString();
            assertThrows(PayloadValidationException.class, () ->
                             validationService.validateBody(listJson, headerGroup, true),
                         CROWN_WARNED_PDDA_LIST_INVALID_MESSAGE);
        }
    }

    @Test
    void testValidateWithErrorsWhenWithFixedDateCaseNumberMissingInCrownWarnedPddaList() throws IOException {
        try (InputStream jsonInput = this.getClass().getClassLoader()
            .getResourceAsStream(CROWN_WARNED_PDDA_LIST_VALID_JSON)) {
            String text = new String(jsonInput.readAllBytes(), StandardCharsets.UTF_8);

            JsonNode node = getJsonNode(text);
            ((ObjectNode) node.get(WARNED_LIST_SCHEMA).get(COURT_LISTS).get(0).get(WITH_FIXED_DATE).get(FIXTURE)
                .get(CASES).get(0)).remove(CASE_NUMBER);

            String listJson = node.toString();
            assertThrows(PayloadValidationException.class, () ->
                             validationService.validateBody(listJson, headerGroup, true),
                         CROWN_WARNED_PDDA_LIST_INVALID_MESSAGE);
        }
    }

    @Test
    void testValidateWithErrorsWhenWithFixedDateCaseNumberCathMissingInCrownWarnedPddaList() throws IOException {
        try (InputStream jsonInput = this.getClass().getClassLoader()
            .getResourceAsStream(CROWN_WARNED_PDDA_LIST_VALID_JSON)) {
            String text = new String(jsonInput.readAllBytes(), StandardCharsets.UTF_8);

            JsonNode node = getJsonNode(text);
            ((ObjectNode) node.get(WARNED_LIST_SCHEMA).get(COURT_LISTS).get(0).get(WITH_FIXED_DATE).get(FIXTURE)
                .get(CASES).get(0)).remove(CASE_NUMBER_CATH);

            String listJson = node.toString();
            assertThrows(PayloadValidationException.class, () ->
                             validationService.validateBody(listJson, headerGroup, true),
                         CROWN_WARNED_PDDA_LIST_INVALID_MESSAGE);
        }
    }

    @Test
    void testValidateWithErrorsWhenWithFixedDateDefendantsMissingInCrownWarnedPddaList() throws IOException {
        try (InputStream jsonInput = this.getClass().getClassLoader()
            .getResourceAsStream(CROWN_WARNED_PDDA_LIST_VALID_JSON)) {
            String text = new String(jsonInput.readAllBytes(), StandardCharsets.UTF_8);

            JsonNode node = getJsonNode(text);
            ((ObjectNode) node.get(WARNED_LIST_SCHEMA).get(COURT_LISTS).get(0).get(WITH_FIXED_DATE).get(FIXTURE)
                .get(CASES).get(0)).remove(DEFENDANTS);

            String listJson = node.toString();
            assertThrows(PayloadValidationException.class, () ->
                             validationService.validateBody(listJson, headerGroup, true),
                         CROWN_WARNED_PDDA_LIST_INVALID_MESSAGE);
        }
    }

    @Test
    void testValidateWithErrorsWhenWithFixedDateCourtHouseCodeMissingInCrownWarnedPddaList() throws IOException {
        try (InputStream jsonInput = this.getClass().getClassLoader()
            .getResourceAsStream(CROWN_WARNED_PDDA_LIST_VALID_JSON)) {
            String text = new String(jsonInput.readAllBytes(), StandardCharsets.UTF_8);

            JsonNode node = getJsonNode(text);
            ((ObjectNode) node.get(WARNED_LIST_SCHEMA).get(COURT_LISTS).get(0).get(WITH_FIXED_DATE).get(FIXTURE)
                .get(CASES).get(0).get(CASE_ARRIVED_FROM).get(ORIGINATING_COURT)).remove(COURT_HOUSE_CODE);

            String listJson = node.toString();
            assertThrows(PayloadValidationException.class, () ->
                             validationService.validateBody(listJson, headerGroup, true),
                         CROWN_WARNED_PDDA_LIST_INVALID_MESSAGE);
        }
    }

    @Test
    void testValidateWithErrorsWhenWithFixedDateCourtHouseNameMissingInCrownWarnedPddaList() throws IOException {
        try (InputStream jsonInput = this.getClass().getClassLoader()
            .getResourceAsStream(CROWN_WARNED_PDDA_LIST_VALID_JSON)) {
            String text = new String(jsonInput.readAllBytes(), StandardCharsets.UTF_8);

            JsonNode node = getJsonNode(text);
            ((ObjectNode) node.get(WARNED_LIST_SCHEMA).get(COURT_LISTS).get(0).get(WITH_FIXED_DATE).get(FIXTURE)
                .get(CASES).get(0).get(CASE_ARRIVED_FROM).get(ORIGINATING_COURT)).remove(COURT_HOUSE_NAME);

            String listJson = node.toString();
            assertThrows(PayloadValidationException.class, () ->
                             validationService.validateBody(listJson, headerGroup, true),
                         CROWN_WARNED_PDDA_LIST_INVALID_MESSAGE);
        }
    }

    @Test
    void testValidateWithErrorsWhenWithFixedDateHearingDescriptionMissingInCrownWarnedPddaList() throws IOException {
        try (InputStream jsonInput = this.getClass().getClassLoader()
            .getResourceAsStream(CROWN_WARNED_PDDA_LIST_VALID_JSON)) {
            String text = new String(jsonInput.readAllBytes(), StandardCharsets.UTF_8);

            JsonNode node = getJsonNode(text);
            ((ObjectNode) node.get(WARNED_LIST_SCHEMA).get(COURT_LISTS).get(0).get(WITH_FIXED_DATE).get(FIXTURE)
                .get(CASES).get(0).get(HEARING).get(0)).remove(HEARING_DESCRIPTION);

            String listJson = node.toString();
            assertThrows(PayloadValidationException.class, () ->
                             validationService.validateBody(listJson, headerGroup, true),
                         CROWN_WARNED_PDDA_LIST_INVALID_MESSAGE);
        }
    }

    @Test
    void testValidateWithErrorsWhenWithFixedDateHearingHearingTypeMissingInCrownWarnedPddaList() throws IOException {
        try (InputStream jsonInput = this.getClass().getClassLoader()
            .getResourceAsStream(CROWN_WARNED_PDDA_LIST_VALID_JSON)) {
            String text = new String(jsonInput.readAllBytes(), StandardCharsets.UTF_8);

            JsonNode node = getJsonNode(text);
            ((ObjectNode) node.get(WARNED_LIST_SCHEMA).get(COURT_LISTS).get(0).get(WITH_FIXED_DATE).get(FIXTURE)
                .get(CASES).get(0).get(HEARING).get(0)).remove(HEARING_TYPE);

            String listJson = node.toString();
            assertThrows(PayloadValidationException.class, () ->
                             validationService.validateBody(listJson, headerGroup, true),
                         CROWN_WARNED_PDDA_LIST_INVALID_MESSAGE);
        }
    }

    @Test
    void testValidateWithErrorsWhenWithFixedDatePersonalDetailsMissingInCrownWarnedPddaList() throws IOException {
        try (InputStream jsonInput = this.getClass().getClassLoader()
            .getResourceAsStream(CROWN_WARNED_PDDA_LIST_VALID_JSON)) {
            String text = new String(jsonInput.readAllBytes(), StandardCharsets.UTF_8);

            JsonNode node = getJsonNode(text);
            ((ObjectNode) node.get(WARNED_LIST_SCHEMA).get(COURT_LISTS).get(0).get(WITH_FIXED_DATE).get(FIXTURE)
                .get(CASES).get(0).get(DEFENDANTS).get(0)).remove(PERSONAL_DETAILS);

            String listJson = node.toString();
            assertThrows(PayloadValidationException.class, () ->
                             validationService.validateBody(listJson, headerGroup, true),
                         CROWN_WARNED_PDDA_LIST_INVALID_MESSAGE);
        }
    }

    @Test
    void testValidateWithErrorsWhenWithFixedDateNameMissingInCrownWarnedPddaList() throws IOException {
        try (InputStream jsonInput = this.getClass().getClassLoader()
            .getResourceAsStream(CROWN_WARNED_PDDA_LIST_VALID_JSON)) {
            String text = new String(jsonInput.readAllBytes(), StandardCharsets.UTF_8);

            JsonNode node = getJsonNode(text);
            ((ObjectNode) node.get(WARNED_LIST_SCHEMA).get(COURT_LISTS).get(0).get(WITH_FIXED_DATE).get(FIXTURE)
                .get(CASES).get(0).get(DEFENDANTS).get(0).get(PERSONAL_DETAILS)).remove(NAME);

            String listJson = node.toString();
            assertThrows(PayloadValidationException.class, () ->
                             validationService.validateBody(listJson, headerGroup, true),
                         CROWN_WARNED_PDDA_LIST_INVALID_MESSAGE);
        }
    }

    @Test
    void testValidateWithErrorsWhenWithFixedDateIsMaskedMissingInCrownWarnedPddaList() throws IOException {
        try (InputStream jsonInput = this.getClass().getClassLoader()
            .getResourceAsStream(CROWN_WARNED_PDDA_LIST_VALID_JSON)) {
            String text = new String(jsonInput.readAllBytes(), StandardCharsets.UTF_8);

            JsonNode node = getJsonNode(text);
            ((ObjectNode) node.get(WARNED_LIST_SCHEMA).get(COURT_LISTS).get(0).get(WITH_FIXED_DATE).get(FIXTURE)
                .get(CASES).get(0).get(DEFENDANTS).get(0).get(PERSONAL_DETAILS)).remove(IS_MASKED);

            String listJson = node.toString();
            assertThrows(PayloadValidationException.class, () ->
                             validationService.validateBody(listJson, headerGroup, true),
                         CROWN_WARNED_PDDA_LIST_INVALID_MESSAGE);
        }
    }

    @Test
    void testValidateWithErrorsWhenWithFixedDateLocationMissingInCrownWarnedPddaList() throws IOException {
        try (InputStream jsonInput = this.getClass().getClassLoader()
            .getResourceAsStream(CROWN_WARNED_PDDA_LIST_VALID_JSON)) {
            String text = new String(jsonInput.readAllBytes(), StandardCharsets.UTF_8);

            JsonNode node = getJsonNode(text);
            ((ObjectNode) node.get(WARNED_LIST_SCHEMA).get(COURT_LISTS).get(0).get(WITH_FIXED_DATE).get(FIXTURE)
                .get(CASES).get(0).get(DEFENDANTS).get(0).get("PrisonLocation")).remove(LOCATION);

            String listJson = node.toString();
            assertThrows(PayloadValidationException.class, () ->
                             validationService.validateBody(listJson, headerGroup, true),
                         CROWN_WARNED_PDDA_LIST_INVALID_MESSAGE);
        }
    }

    @Test
    void testValidateWithErrorsWhenWithFixedDateOrganisationNameMissingInCrownWarnedPddaList() throws IOException {
        try (InputStream jsonInput = this.getClass().getClassLoader()
            .getResourceAsStream(CROWN_WARNED_PDDA_LIST_VALID_JSON)) {
            String text = new String(jsonInput.readAllBytes(), StandardCharsets.UTF_8);

            JsonNode node = getJsonNode(text);
            ((ObjectNode) node.get(WARNED_LIST_SCHEMA).get(COURT_LISTS).get(0).get(WITH_FIXED_DATE).get(FIXTURE)
                .get(CASES).get(0).get(DEFENDANTS).get(0).get("Counsel").get(0).get("Solicitor").get(0).get("Party")
                .get("Organisation")).remove(ORGANISATION_NAME);

            String listJson = node.toString();
            assertThrows(PayloadValidationException.class, () ->
                             validationService.validateBody(listJson, headerGroup, true),
                         CROWN_WARNED_PDDA_LIST_INVALID_MESSAGE);
        }
    }

    @Test
    void testValidateWithErrorsWhenWithFixedDateOffenceStatementMissingInCrownWarnedPddaList() throws IOException {
        try (InputStream jsonInput = this.getClass().getClassLoader()
            .getResourceAsStream(CROWN_WARNED_PDDA_LIST_VALID_JSON)) {
            String text = new String(jsonInput.readAllBytes(), StandardCharsets.UTF_8);

            JsonNode node = getJsonNode(text);
            ((ObjectNode) node.get(WARNED_LIST_SCHEMA).get(COURT_LISTS).get(0).get(WITH_FIXED_DATE).get(FIXTURE)
                .get(CASES).get(0).get(DEFENDANTS).get(0).get("Charges").get(0)).remove(OFFENCE_STATEMENT);

            String listJson = node.toString();
            assertThrows(PayloadValidationException.class, () ->
                             validationService.validateBody(listJson, headerGroup, true),
                         CROWN_WARNED_PDDA_LIST_INVALID_MESSAGE);
        }
    }

    @Test
    void testValidateWithErrorsWhenWithFixedDateProsecutionOrgNameMissingInCrownWarnedPddaList() throws IOException {
        try (InputStream jsonInput = this.getClass().getClassLoader()
            .getResourceAsStream(CROWN_WARNED_PDDA_LIST_VALID_JSON)) {
            String text = new String(jsonInput.readAllBytes(), StandardCharsets.UTF_8);

            JsonNode node = getJsonNode(text);
            ((ObjectNode) node.get(WARNED_LIST_SCHEMA).get(COURT_LISTS).get(0).get(WITH_FIXED_DATE).get(FIXTURE)
                .get(CASES).get(0).get("Prosecution").get("ProsecutingOrganisation")).remove(ORGANISATION_NAME);

            String listJson = node.toString();
            assertThrows(PayloadValidationException.class, () ->
                             validationService.validateBody(listJson, headerGroup, true),
                         CROWN_WARNED_PDDA_LIST_INVALID_MESSAGE);
        }
    }

    @Test
    void testValidateWithErrorsWhenWithFixedDateLinkedCaseNumberMissingInCrownWarnedPddaList() throws IOException {
        try (InputStream jsonInput = this.getClass().getClassLoader()
            .getResourceAsStream(CROWN_WARNED_PDDA_LIST_VALID_JSON)) {
            String text = new String(jsonInput.readAllBytes(), StandardCharsets.UTF_8);

            JsonNode node = getJsonNode(text);
            ((ObjectNode) node.get(WARNED_LIST_SCHEMA).get(COURT_LISTS).get(0).get(WITH_FIXED_DATE).get(FIXTURE)
                .get(CASES).get(0).get("LinkedCases").get(0)).remove(CASE_NUMBER);

            String listJson = node.toString();
            assertThrows(
                PayloadValidationException.class, () ->
                    validationService.validateBody(listJson, headerGroup, true),
                CROWN_WARNED_PDDA_LIST_INVALID_MESSAGE
            );
        }
    }

    @Test
    void testValidateWithErrorsWhenWithoutFixedDateMissingInCrownWarnedPddaList() throws IOException {
        try (InputStream jsonInput = this.getClass().getClassLoader()
            .getResourceAsStream(CROWN_WARNED_PDDA_LIST_VALID_JSON)) {
            String text = new String(jsonInput.readAllBytes(), StandardCharsets.UTF_8);

            JsonNode node = getJsonNode(text);
            ((ObjectNode) node.get(WARNED_LIST_SCHEMA).get(COURT_LISTS).get(0)).remove(WITHOUT_FIXED_DATE);

            String listJson = node.toString();
            assertThrows(PayloadValidationException.class, () ->
                             validationService.validateBody(listJson, headerGroup, true),
                         CROWN_WARNED_PDDA_LIST_INVALID_MESSAGE);
        }
    }

    @Test
    void testValidateWithErrorsWhenWithoutFixedDateHearingTypeMissingInCrownWarnedPddaList() throws IOException {
        try (InputStream jsonInput = this.getClass().getClassLoader()
            .getResourceAsStream(CROWN_WARNED_PDDA_LIST_VALID_JSON)) {
            String text = new String(jsonInput.readAllBytes(), StandardCharsets.UTF_8);

            JsonNode node = getJsonNode(text);
            ((ObjectNode) node.get(WARNED_LIST_SCHEMA).get(COURT_LISTS).get(0).get(WITHOUT_FIXED_DATE))
                .remove(HEARING_TYPE);

            String listJson = node.toString();
            assertThrows(PayloadValidationException.class, () ->
                             validationService.validateBody(listJson, headerGroup, true),
                         CROWN_WARNED_PDDA_LIST_INVALID_MESSAGE);
        }
    }

    @Test
    void testValidateWithErrorsWhenWithoutFixedDateFixtureMissingInCrownWarnedPddaList() throws IOException {
        try (InputStream jsonInput = this.getClass().getClassLoader()
            .getResourceAsStream(CROWN_WARNED_PDDA_LIST_VALID_JSON)) {
            String text = new String(jsonInput.readAllBytes(), StandardCharsets.UTF_8);

            JsonNode node = getJsonNode(text);
            ((ObjectNode) node.get(WARNED_LIST_SCHEMA).get(COURT_LISTS).get(0).get(WITHOUT_FIXED_DATE)).remove(FIXTURE);

            String listJson = node.toString();
            assertThrows(PayloadValidationException.class, () ->
                             validationService.validateBody(listJson, headerGroup, true),
                         CROWN_WARNED_PDDA_LIST_INVALID_MESSAGE);
        }
    }

    @Test
    void testValidateWithErrorsWhenWithoutFixedDateCasesMissingInCrownWarnedPddaList() throws IOException {
        try (InputStream jsonInput = this.getClass().getClassLoader()
            .getResourceAsStream(CROWN_WARNED_PDDA_LIST_VALID_JSON)) {
            String text = new String(jsonInput.readAllBytes(), StandardCharsets.UTF_8);

            JsonNode node = getJsonNode(text);
            ((ObjectNode) node.get(WARNED_LIST_SCHEMA).get(COURT_LISTS).get(0).get(WITHOUT_FIXED_DATE).get(FIXTURE))
                .remove(CASES);

            String listJson = node.toString();
            assertThrows(PayloadValidationException.class, () ->
                             validationService.validateBody(listJson, headerGroup, true),
                         CROWN_WARNED_PDDA_LIST_INVALID_MESSAGE);
        }
    }

    @Test
    void testValidateWithErrorsWhenWithoutFixedDateCaseNumberMissingInCrownWarnedPddaList() throws IOException {
        try (InputStream jsonInput = this.getClass().getClassLoader()
            .getResourceAsStream(CROWN_WARNED_PDDA_LIST_VALID_JSON)) {
            String text = new String(jsonInput.readAllBytes(), StandardCharsets.UTF_8);

            JsonNode node = getJsonNode(text);
            ((ObjectNode) node.get(WARNED_LIST_SCHEMA).get(COURT_LISTS).get(0).get(WITHOUT_FIXED_DATE).get(FIXTURE)
                .get(CASES).get(0)).remove(CASE_NUMBER);

            String listJson = node.toString();
            assertThrows(PayloadValidationException.class, () ->
                             validationService.validateBody(listJson, headerGroup, true),
                         CROWN_WARNED_PDDA_LIST_INVALID_MESSAGE);
        }
    }

    @Test
    void testValidateWithErrorsWhenWithoutFixedDateCaseNumberCathMissingInCrownWarnedPddaList() throws IOException {
        try (InputStream jsonInput = this.getClass().getClassLoader()
            .getResourceAsStream(CROWN_WARNED_PDDA_LIST_VALID_JSON)) {
            String text = new String(jsonInput.readAllBytes(), StandardCharsets.UTF_8);

            JsonNode node = getJsonNode(text);
            ((ObjectNode) node.get(WARNED_LIST_SCHEMA).get(COURT_LISTS).get(0).get(WITHOUT_FIXED_DATE).get(FIXTURE)
                .get(CASES).get(0)).remove(CASE_NUMBER_CATH);

            String listJson = node.toString();
            assertThrows(PayloadValidationException.class, () ->
                             validationService.validateBody(listJson, headerGroup, true),
                         CROWN_WARNED_PDDA_LIST_INVALID_MESSAGE);
        }
    }

    @Test
    void testValidateWithErrorsWhenWithoutFixedDateDefendantsMissingInCrownWarnedPddaList() throws IOException {
        try (InputStream jsonInput = this.getClass().getClassLoader()
            .getResourceAsStream(CROWN_WARNED_PDDA_LIST_VALID_JSON)) {
            String text = new String(jsonInput.readAllBytes(), StandardCharsets.UTF_8);

            JsonNode node = getJsonNode(text);
            ((ObjectNode) node.get(WARNED_LIST_SCHEMA).get(COURT_LISTS).get(0).get(WITHOUT_FIXED_DATE).get(FIXTURE)
                .get(CASES).get(0)).remove(DEFENDANTS);

            String listJson = node.toString();
            assertThrows(PayloadValidationException.class, () ->
                             validationService.validateBody(listJson, headerGroup, true),
                         CROWN_WARNED_PDDA_LIST_INVALID_MESSAGE);
        }
    }

    @Test
    void testValidateWithErrorsWhenWithoutFixedDateCourtHouseCodeMissingInCrownWarnedPddaList() throws IOException {
        try (InputStream jsonInput = this.getClass().getClassLoader()
            .getResourceAsStream(CROWN_WARNED_PDDA_LIST_VALID_JSON)) {
            String text = new String(jsonInput.readAllBytes(), StandardCharsets.UTF_8);

            JsonNode node = getJsonNode(text);
            ((ObjectNode) node.get(WARNED_LIST_SCHEMA).get(COURT_LISTS).get(0).get(WITHOUT_FIXED_DATE).get(FIXTURE)
                .get(CASES).get(0).get(CASE_ARRIVED_FROM).get(ORIGINATING_COURT)).remove(COURT_HOUSE_CODE);

            String listJson = node.toString();
            assertThrows(PayloadValidationException.class, () ->
                             validationService.validateBody(listJson, headerGroup, true),
                         CROWN_WARNED_PDDA_LIST_INVALID_MESSAGE);
        }
    }

    @Test
    void testValidateWithErrorsWhenWithoutFixedDateCourtHouseNameMissingInCrownWarnedPddaList() throws IOException {
        try (InputStream jsonInput = this.getClass().getClassLoader()
            .getResourceAsStream(CROWN_WARNED_PDDA_LIST_VALID_JSON)) {
            String text = new String(jsonInput.readAllBytes(), StandardCharsets.UTF_8);

            JsonNode node = getJsonNode(text);
            ((ObjectNode) node.get(WARNED_LIST_SCHEMA).get(COURT_LISTS).get(0).get(WITHOUT_FIXED_DATE).get(FIXTURE)
                .get(CASES).get(0).get(CASE_ARRIVED_FROM).get(ORIGINATING_COURT)).remove(COURT_HOUSE_NAME);

            String listJson = node.toString();
            assertThrows(PayloadValidationException.class, () ->
                             validationService.validateBody(listJson, headerGroup, true),
                         CROWN_WARNED_PDDA_LIST_INVALID_MESSAGE);
        }
    }

    @Test
    void testValidateWithErrorsWhenWithoutFixedDateHearingDescriptionMissingInCrownWarnedPddaList() throws IOException {
        try (InputStream jsonInput = this.getClass().getClassLoader()
            .getResourceAsStream(CROWN_WARNED_PDDA_LIST_VALID_JSON)) {
            String text = new String(jsonInput.readAllBytes(), StandardCharsets.UTF_8);

            JsonNode node = getJsonNode(text);
            ((ObjectNode) node.get(WARNED_LIST_SCHEMA).get(COURT_LISTS).get(0).get(WITHOUT_FIXED_DATE).get(FIXTURE)
                .get(CASES).get(0).get(HEARING).get(0)).remove(HEARING_DESCRIPTION);

            String listJson = node.toString();
            assertThrows(PayloadValidationException.class, () ->
                             validationService.validateBody(listJson, headerGroup, true),
                         CROWN_WARNED_PDDA_LIST_INVALID_MESSAGE);
        }
    }

    @Test
    void testValidateWithErrorsWhenWithoutFixedDateHearingHearingTypeMissingInCrownWarnedPddaList() throws IOException {
        try (InputStream jsonInput = this.getClass().getClassLoader()
            .getResourceAsStream(CROWN_WARNED_PDDA_LIST_VALID_JSON)) {
            String text = new String(jsonInput.readAllBytes(), StandardCharsets.UTF_8);

            JsonNode node = getJsonNode(text);
            ((ObjectNode) node.get(WARNED_LIST_SCHEMA).get(COURT_LISTS).get(0).get(WITHOUT_FIXED_DATE).get(FIXTURE)
                .get(CASES).get(0).get(HEARING).get(0)).remove(HEARING_TYPE);

            String listJson = node.toString();
            assertThrows(PayloadValidationException.class, () ->
                             validationService.validateBody(listJson, headerGroup, true),
                         CROWN_WARNED_PDDA_LIST_INVALID_MESSAGE);
        }
    }

    @Test
    void testValidateWithErrorsWhenWithoutFixedDatePersonalDetailsMissingInCrownWarnedPddaList() throws IOException {
        try (InputStream jsonInput = this.getClass().getClassLoader()
            .getResourceAsStream(CROWN_WARNED_PDDA_LIST_VALID_JSON)) {
            String text = new String(jsonInput.readAllBytes(), StandardCharsets.UTF_8);

            JsonNode node = getJsonNode(text);
            ((ObjectNode) node.get(WARNED_LIST_SCHEMA).get(COURT_LISTS).get(0).get(WITHOUT_FIXED_DATE).get(FIXTURE)
                .get(CASES).get(0).get(DEFENDANTS).get(0)).remove(PERSONAL_DETAILS);

            String listJson = node.toString();
            assertThrows(PayloadValidationException.class, () ->
                             validationService.validateBody(listJson, headerGroup, true),
                         CROWN_WARNED_PDDA_LIST_INVALID_MESSAGE);
        }
    }

    @Test
    void testValidateWithErrorsWhenWithoutFixedDateNameMissingInCrownWarnedPddaList() throws IOException {
        try (InputStream jsonInput = this.getClass().getClassLoader()
            .getResourceAsStream(CROWN_WARNED_PDDA_LIST_VALID_JSON)) {
            String text = new String(jsonInput.readAllBytes(), StandardCharsets.UTF_8);

            JsonNode node = getJsonNode(text);
            ((ObjectNode) node.get(WARNED_LIST_SCHEMA).get(COURT_LISTS).get(0).get(WITHOUT_FIXED_DATE).get(FIXTURE)
                .get(CASES).get(0).get(DEFENDANTS).get(0).get(PERSONAL_DETAILS)).remove(NAME);

            String listJson = node.toString();
            assertThrows(PayloadValidationException.class, () ->
                             validationService.validateBody(listJson, headerGroup, true),
                         CROWN_WARNED_PDDA_LIST_INVALID_MESSAGE);
        }
    }

    @Test
    void testValidateWithErrorsWhenWithoutFixedDateIsMaskedMissingInCrownWarnedPddaList() throws IOException {
        try (InputStream jsonInput = this.getClass().getClassLoader()
            .getResourceAsStream(CROWN_WARNED_PDDA_LIST_VALID_JSON)) {
            String text = new String(jsonInput.readAllBytes(), StandardCharsets.UTF_8);

            JsonNode node = getJsonNode(text);
            ((ObjectNode) node.get(WARNED_LIST_SCHEMA).get(COURT_LISTS).get(0).get(WITHOUT_FIXED_DATE).get(FIXTURE)
                .get(CASES).get(0).get(DEFENDANTS).get(0).get(PERSONAL_DETAILS)).remove(IS_MASKED);

            String listJson = node.toString();
            assertThrows(PayloadValidationException.class, () ->
                             validationService.validateBody(listJson, headerGroup, true),
                         CROWN_WARNED_PDDA_LIST_INVALID_MESSAGE);
        }
    }

    @Test
    void testValidateWithErrorsWhenWithoutFixedDateLocationMissingInCrownWarnedPddaList() throws IOException {
        try (InputStream jsonInput = this.getClass().getClassLoader()
            .getResourceAsStream(CROWN_WARNED_PDDA_LIST_VALID_JSON)) {
            String text = new String(jsonInput.readAllBytes(), StandardCharsets.UTF_8);

            JsonNode node = getJsonNode(text);
            ((ObjectNode) node.get(WARNED_LIST_SCHEMA).get(COURT_LISTS).get(0).get(WITHOUT_FIXED_DATE).get(FIXTURE)
                .get(CASES).get(0).get(DEFENDANTS).get(0).get("PrisonLocation")).remove(LOCATION);

            String listJson = node.toString();
            assertThrows(PayloadValidationException.class, () ->
                             validationService.validateBody(listJson, headerGroup, true),
                         CROWN_WARNED_PDDA_LIST_INVALID_MESSAGE);
        }
    }

    @Test
    void testValidateWithErrorsWhenWithoutFixedDateOrganisationNameMissingInCrownWarnedPddaList() throws IOException {
        try (InputStream jsonInput = this.getClass().getClassLoader()
            .getResourceAsStream(CROWN_WARNED_PDDA_LIST_VALID_JSON)) {
            String text = new String(jsonInput.readAllBytes(), StandardCharsets.UTF_8);

            JsonNode node = getJsonNode(text);
            ((ObjectNode) node.get(WARNED_LIST_SCHEMA).get(COURT_LISTS).get(0).get(WITHOUT_FIXED_DATE).get(FIXTURE)
                .get(CASES).get(0).get(DEFENDANTS).get(0).get("Counsel").get(0).get("Solicitor").get(0).get("Party")
                .get("Organisation")).remove(ORGANISATION_NAME);

            String listJson = node.toString();
            assertThrows(PayloadValidationException.class, () ->
                             validationService.validateBody(listJson, headerGroup, true),
                         CROWN_WARNED_PDDA_LIST_INVALID_MESSAGE);
        }
    }

    @Test
    void testValidateWithErrorsWhenWithoutFixedDateOffenceStatementMissingInCrownWarnedPddaList() throws IOException {
        try (InputStream jsonInput = this.getClass().getClassLoader()
            .getResourceAsStream(CROWN_WARNED_PDDA_LIST_VALID_JSON)) {
            String text = new String(jsonInput.readAllBytes(), StandardCharsets.UTF_8);

            JsonNode node = getJsonNode(text);
            ((ObjectNode) node.get(WARNED_LIST_SCHEMA).get(COURT_LISTS).get(0).get(WITHOUT_FIXED_DATE).get(FIXTURE)
                .get(CASES).get(0).get(DEFENDANTS).get(0).get("Charges").get(0)).remove(OFFENCE_STATEMENT);

            String listJson = node.toString();
            assertThrows(PayloadValidationException.class, () ->
                             validationService.validateBody(listJson, headerGroup, true),
                         CROWN_WARNED_PDDA_LIST_INVALID_MESSAGE);
        }
    }

    @Test
    void testValidateWithErrorsWhenWithoutFixedDateProsecutionOrgNameMissingInCrownWarnedPddaList() throws IOException {
        try (InputStream jsonInput = this.getClass().getClassLoader()
            .getResourceAsStream(CROWN_WARNED_PDDA_LIST_VALID_JSON)) {
            String text = new String(jsonInput.readAllBytes(), StandardCharsets.UTF_8);

            JsonNode node = getJsonNode(text);
            ((ObjectNode) node.get(WARNED_LIST_SCHEMA).get(COURT_LISTS).get(0).get(WITHOUT_FIXED_DATE).get(FIXTURE)
                .get(CASES).get(0).get("Prosecution").get("ProsecutingOrganisation")).remove(ORGANISATION_NAME);

            String listJson = node.toString();
            assertThrows(PayloadValidationException.class, () ->
                             validationService.validateBody(listJson, headerGroup, true),
                         CROWN_WARNED_PDDA_LIST_INVALID_MESSAGE);
        }
    }

    @Test
    void testValidateWithErrorsWhenWithoutFixedDateLinkedCaseNumberMissingInCrownWarnedPddaList() throws IOException {
        try (InputStream jsonInput = this.getClass().getClassLoader()
            .getResourceAsStream(CROWN_WARNED_PDDA_LIST_VALID_JSON)) {
            String text = new String(jsonInput.readAllBytes(), StandardCharsets.UTF_8);

            JsonNode node = getJsonNode(text);
            ((ObjectNode) node.get(WARNED_LIST_SCHEMA).get(COURT_LISTS).get(0).get(WITHOUT_FIXED_DATE).get(FIXTURE)
                .get(CASES).get(0).get("LinkedCases").get(0)).remove(CASE_NUMBER);

            String listJson = node.toString();
            assertThrows(PayloadValidationException.class, () ->
                             validationService.validateBody(listJson, headerGroup, true),
                         CROWN_WARNED_PDDA_LIST_INVALID_MESSAGE);
        }
    }

}
