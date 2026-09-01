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
class CrownAdvancePddaListTest extends IntegrationBasicTestBase {

    @Autowired
    ValidationService validationService;

    private static final String CROWN_ADVANCE_PDDA_LIST_VALID_JSON =
        "data/crown-advance-pdda-list/crownAdvancePddaList.json";
    private static final String CROWN_ADVANCE_PDDA_LIST_INVALID_MESSAGE =
        "Invalid Crown Advance List marked as valid";

    private static final String ADVANCE_LIST = "AdvanceList";
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
    private static final String SECTION_53 = "Section53";
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
    void testValidateWithErrorsWhenAdvanceListMissingInCrownAdvancePddaList() throws IOException {
        try (InputStream jsonInput = this.getClass().getClassLoader()
            .getResourceAsStream(CROWN_ADVANCE_PDDA_LIST_VALID_JSON)) {
            String text = new String(jsonInput.readAllBytes(), StandardCharsets.UTF_8);

            JsonNode node = getJsonNode(text);
            ((ObjectNode) node).remove(ADVANCE_LIST);

            String listJson = node.toString();
            assertThrows(PayloadValidationException.class, () ->
                             validationService.validateBody(listJson, headerGroup, true),
                         CROWN_ADVANCE_PDDA_LIST_INVALID_MESSAGE);
        }
    }

    @Test
    void testValidateWithErrorsWhenDocumentIdMissingInCrownAdvancePddaList() throws IOException {
        try (InputStream jsonInput = this.getClass().getClassLoader()
            .getResourceAsStream(CROWN_ADVANCE_PDDA_LIST_VALID_JSON)) {
            String text = new String(jsonInput.readAllBytes(), StandardCharsets.UTF_8);

            JsonNode node = getJsonNode(text);
            ((ObjectNode) node.get(ADVANCE_LIST)).remove(DOCUMENT_ID);

            String listJson = node.toString();
            assertThrows(PayloadValidationException.class, () ->
                             validationService.validateBody(listJson, headerGroup, true),
                         CROWN_ADVANCE_PDDA_LIST_INVALID_MESSAGE);
        }
    }

    @Test
    void testValidateWithErrorsWhenDocumentNameMissingInCrownAdvancePddaList() throws IOException {
        try (InputStream jsonInput = this.getClass().getClassLoader()
            .getResourceAsStream(CROWN_ADVANCE_PDDA_LIST_VALID_JSON)) {
            String text = new String(jsonInput.readAllBytes(), StandardCharsets.UTF_8);

            JsonNode node = getJsonNode(text);
            ((ObjectNode) node.get(ADVANCE_LIST).get(DOCUMENT_ID)).remove(DOCUMENT_NAME);

            String listJson = node.toString();
            assertThrows(PayloadValidationException.class, () ->
                             validationService.validateBody(listJson, headerGroup, true),
                         CROWN_ADVANCE_PDDA_LIST_INVALID_MESSAGE);
        }
    }

    @Test
    void testValidateWithErrorsWhenUniqueIdMissingInCrownAdvancePddaList() throws IOException {
        try (InputStream jsonInput = this.getClass().getClassLoader()
            .getResourceAsStream(CROWN_ADVANCE_PDDA_LIST_VALID_JSON)) {
            String text = new String(jsonInput.readAllBytes(), StandardCharsets.UTF_8);

            JsonNode node = getJsonNode(text);
            ((ObjectNode) node.get(ADVANCE_LIST).get(DOCUMENT_ID)).remove(UNIQUE_ID);

            String listJson = node.toString();
            assertThrows(PayloadValidationException.class, () ->
                             validationService.validateBody(listJson, headerGroup, true),
                         CROWN_ADVANCE_PDDA_LIST_INVALID_MESSAGE);
        }
    }

    @Test
    void testValidateWithErrorsWhenDocumentTypeMissingInCrownAdvancePddaList() throws IOException {
        try (InputStream jsonInput = this.getClass().getClassLoader()
            .getResourceAsStream(CROWN_ADVANCE_PDDA_LIST_VALID_JSON)) {
            String text = new String(jsonInput.readAllBytes(), StandardCharsets.UTF_8);

            JsonNode node = getJsonNode(text);
            ((ObjectNode) node.get(ADVANCE_LIST).get(DOCUMENT_ID)).remove(DOCUMENT_TYPE);

            String listJson = node.toString();
            assertThrows(PayloadValidationException.class, () ->
                             validationService.validateBody(listJson, headerGroup, true),
                         CROWN_ADVANCE_PDDA_LIST_INVALID_MESSAGE);
        }
    }

    @Test
    void testValidateWithErrorsWhenListHeaderMissingInCrownAdvancePddaList() throws IOException {
        try (InputStream jsonInput = this.getClass().getClassLoader()
            .getResourceAsStream(CROWN_ADVANCE_PDDA_LIST_VALID_JSON)) {
            String text = new String(jsonInput.readAllBytes(), StandardCharsets.UTF_8);

            JsonNode node = getJsonNode(text);
            ((ObjectNode) node.get(ADVANCE_LIST)).remove(LIST_HEADER);

            String listJson = node.toString();
            assertThrows(PayloadValidationException.class, () ->
                             validationService.validateBody(listJson, headerGroup, true),
                         CROWN_ADVANCE_PDDA_LIST_INVALID_MESSAGE);
        }
    }

    @Test
    void testValidateWithErrorsWhenStartDateMissingInCrownAdvancePddaList() throws IOException {
        try (InputStream jsonInput = this.getClass().getClassLoader()
            .getResourceAsStream(CROWN_ADVANCE_PDDA_LIST_VALID_JSON)) {
            String text = new String(jsonInput.readAllBytes(), StandardCharsets.UTF_8);

            JsonNode node = getJsonNode(text);
            ((ObjectNode) node.get(ADVANCE_LIST).get(LIST_HEADER)).remove(START_DATE);

            String listJson = node.toString();
            assertThrows(PayloadValidationException.class, () ->
                             validationService.validateBody(listJson, headerGroup, true),
                         CROWN_ADVANCE_PDDA_LIST_INVALID_MESSAGE);
        }
    }

    @Test
    void testValidateWithErrorsWhenVersionMissingInCrownAdvancePddaList() throws IOException {
        try (InputStream jsonInput = this.getClass().getClassLoader()
            .getResourceAsStream(CROWN_ADVANCE_PDDA_LIST_VALID_JSON)) {
            String text = new String(jsonInput.readAllBytes(), StandardCharsets.UTF_8);

            JsonNode node = getJsonNode(text);
            ((ObjectNode) node.get(ADVANCE_LIST).get(LIST_HEADER)).remove(VERSION);

            String listJson = node.toString();
            assertThrows(PayloadValidationException.class, () ->
                             validationService.validateBody(listJson, headerGroup, true),
                         CROWN_ADVANCE_PDDA_LIST_INVALID_MESSAGE);
        }
    }

    @Test
    void testValidateWithErrorsWhenPublishedTimeMissingInCrownAdvancePddaList() throws IOException {
        try (InputStream jsonInput = this.getClass().getClassLoader()
            .getResourceAsStream(CROWN_ADVANCE_PDDA_LIST_VALID_JSON)) {
            String text = new String(jsonInput.readAllBytes(), StandardCharsets.UTF_8);

            JsonNode node = getJsonNode(text);
            ((ObjectNode) node.get(ADVANCE_LIST).get(LIST_HEADER)).remove(PUBLISHED_TIME);

            String listJson = node.toString();
            assertThrows(PayloadValidationException.class, () ->
                             validationService.validateBody(listJson, headerGroup, true),
                         CROWN_ADVANCE_PDDA_LIST_INVALID_MESSAGE);
        }
    }

    @Test
    void testValidateWithErrorsWhenCrownCourtMissingInCrownAdvancePddaList() throws IOException {
        try (InputStream jsonInput = this.getClass().getClassLoader()
            .getResourceAsStream(CROWN_ADVANCE_PDDA_LIST_VALID_JSON)) {
            String text = new String(jsonInput.readAllBytes(), StandardCharsets.UTF_8);

            JsonNode node = getJsonNode(text);
            ((ObjectNode) node.get(ADVANCE_LIST)).remove(CROWN_COURT);

            String listJson = node.toString();
            assertThrows(PayloadValidationException.class, () ->
                             validationService.validateBody(listJson, headerGroup, true),
                         CROWN_ADVANCE_PDDA_LIST_INVALID_MESSAGE);
        }
    }

    @Test
    void testValidateWithErrorsWhenCourtHouseTypeMissingInCrownAdvancePddaList() throws IOException {
        try (InputStream jsonInput = this.getClass().getClassLoader()
            .getResourceAsStream(CROWN_ADVANCE_PDDA_LIST_VALID_JSON)) {
            String text = new String(jsonInput.readAllBytes(), StandardCharsets.UTF_8);

            JsonNode node = getJsonNode(text);
            ((ObjectNode) node.get(ADVANCE_LIST).get(CROWN_COURT)).remove(COURT_HOUSE_TYPE);

            String listJson = node.toString();
            assertThrows(PayloadValidationException.class, () ->
                             validationService.validateBody(listJson, headerGroup, true),
                         CROWN_ADVANCE_PDDA_LIST_INVALID_MESSAGE);
        }
    }

    @Test
    void testValidateWithErrorsWhenCourtListsCourtHouseTypeMissingInCrownAdvancePddaList() throws IOException {
        try (InputStream jsonInput = this.getClass().getClassLoader()
            .getResourceAsStream(CROWN_ADVANCE_PDDA_LIST_VALID_JSON)) {
            String text = new String(jsonInput.readAllBytes(), StandardCharsets.UTF_8);

            JsonNode node = getJsonNode(text);
            ((ObjectNode) node.get(ADVANCE_LIST).get(COURT_LISTS).get(0).get(COURT_HOUSE))
                .remove(COURT_HOUSE_TYPE);

            String listJson = node.toString();
            assertThrows(PayloadValidationException.class, () ->
                             validationService.validateBody(listJson, headerGroup, true),
                         CROWN_ADVANCE_PDDA_LIST_INVALID_MESSAGE);
        }
    }

    @Test
    void testValidateWithErrorsWhenCourtHouseCodeMissingInCrownAdvancePddaList() throws IOException {
        try (InputStream jsonInput = this.getClass().getClassLoader()
            .getResourceAsStream(CROWN_ADVANCE_PDDA_LIST_VALID_JSON)) {
            String text = new String(jsonInput.readAllBytes(), StandardCharsets.UTF_8);

            JsonNode node = getJsonNode(text);
            ((ObjectNode) node.get(ADVANCE_LIST).get(CROWN_COURT)).remove(COURT_HOUSE_CODE);

            String listJson = node.toString();
            assertThrows(PayloadValidationException.class, () ->
                             validationService.validateBody(listJson, headerGroup, true),
                         CROWN_ADVANCE_PDDA_LIST_INVALID_MESSAGE);
        }
    }

    @Test
    void testValidateWithErrorsWhenCourtListsCourtHouseCodeMissingInCrownAdvancePddaList() throws IOException {
        try (InputStream jsonInput = this.getClass().getClassLoader()
            .getResourceAsStream(CROWN_ADVANCE_PDDA_LIST_VALID_JSON)) {
            String text = new String(jsonInput.readAllBytes(), StandardCharsets.UTF_8);

            JsonNode node = getJsonNode(text);
            ((ObjectNode) node.get(ADVANCE_LIST).get(COURT_LISTS).get(0).get(COURT_HOUSE))
                .remove(COURT_HOUSE_CODE);

            String listJson = node.toString();
            assertThrows(PayloadValidationException.class, () ->
                             validationService.validateBody(listJson, headerGroup, true),
                         CROWN_ADVANCE_PDDA_LIST_INVALID_MESSAGE);
        }
    }

    @Test
    void testValidateWithErrorsWhenCourtHouseNameMissingInCrownAdvancePddaList() throws IOException {
        try (InputStream jsonInput = this.getClass().getClassLoader()
            .getResourceAsStream(CROWN_ADVANCE_PDDA_LIST_VALID_JSON)) {
            String text = new String(jsonInput.readAllBytes(), StandardCharsets.UTF_8);

            JsonNode node = getJsonNode(text);
            ((ObjectNode) node.get(ADVANCE_LIST).get(CROWN_COURT)).remove(COURT_HOUSE_NAME);

            String listJson = node.toString();
            assertThrows(PayloadValidationException.class, () ->
                             validationService.validateBody(listJson, headerGroup, true),
                         CROWN_ADVANCE_PDDA_LIST_INVALID_MESSAGE);
        }
    }

    @Test
    void testValidateWithErrorsWhenCourtListsCourtHouseNameMissingInCrownAdvancePddaList() throws IOException {
        try (InputStream jsonInput = this.getClass().getClassLoader()
            .getResourceAsStream(CROWN_ADVANCE_PDDA_LIST_VALID_JSON)) {
            String text = new String(jsonInput.readAllBytes(), StandardCharsets.UTF_8);

            JsonNode node = getJsonNode(text);
            ((ObjectNode) node.get(ADVANCE_LIST).get(COURT_LISTS).get(0).get(COURT_HOUSE))
                .remove(COURT_HOUSE_NAME);

            String listJson = node.toString();
            assertThrows(PayloadValidationException.class, () ->
                             validationService.validateBody(listJson, headerGroup, true),
                         CROWN_ADVANCE_PDDA_LIST_INVALID_MESSAGE);
        }
    }

    @Test
    void testValidateWithErrorsWhenCourtListsMissingInCrownAdvancePddaList() throws IOException {
        try (InputStream jsonInput = this.getClass().getClassLoader()
            .getResourceAsStream(CROWN_ADVANCE_PDDA_LIST_VALID_JSON)) {
            String text = new String(jsonInput.readAllBytes(), StandardCharsets.UTF_8);

            JsonNode node = getJsonNode(text);
            ((ObjectNode) node.get(ADVANCE_LIST)).remove(COURT_LISTS);

            String listJson = node.toString();
            assertThrows(PayloadValidationException.class, () ->
                             validationService.validateBody(listJson, headerGroup, true),
                         CROWN_ADVANCE_PDDA_LIST_INVALID_MESSAGE);
        }
    }

    @Test
    void testValidateWithErrorsWhenCourtHouseMissingInCrownAdvancePddaList() throws IOException {
        try (InputStream jsonInput = this.getClass().getClassLoader()
            .getResourceAsStream(CROWN_ADVANCE_PDDA_LIST_VALID_JSON)) {
            String text = new String(jsonInput.readAllBytes(), StandardCharsets.UTF_8);

            JsonNode node = getJsonNode(text);
            ((ObjectNode) node.get(ADVANCE_LIST).get(COURT_LISTS).get(0)).remove(COURT_HOUSE);

            String listJson = node.toString();
            assertThrows(PayloadValidationException.class, () ->
                             validationService.validateBody(listJson, headerGroup, true),
                         CROWN_ADVANCE_PDDA_LIST_INVALID_MESSAGE);
        }
    }

    @Test
    void testValidateWithErrorsWhenCourtListCourtHouseTypeMissingInCrownAdvancePddaList() throws IOException {
        try (InputStream jsonInput = this.getClass().getClassLoader()
            .getResourceAsStream(CROWN_ADVANCE_PDDA_LIST_VALID_JSON)) {
            String text = new String(jsonInput.readAllBytes(), StandardCharsets.UTF_8);

            JsonNode node = getJsonNode(text);
            ((ObjectNode) node.get(ADVANCE_LIST).get(COURT_LISTS).get(0).get(COURT_HOUSE))
                .remove(COURT_HOUSE_TYPE);

            String listJson = node.toString();
            assertThrows(PayloadValidationException.class, () ->
                             validationService.validateBody(listJson, headerGroup, true),
                         CROWN_ADVANCE_PDDA_LIST_INVALID_MESSAGE);
        }
    }

    @Test
    void testValidateWithErrorsWhenCourtListCourtHouseCodeMissingInCrownAdvancePddaList() throws IOException {
        try (InputStream jsonInput = this.getClass().getClassLoader()
            .getResourceAsStream(CROWN_ADVANCE_PDDA_LIST_VALID_JSON)) {
            String text = new String(jsonInput.readAllBytes(), StandardCharsets.UTF_8);

            JsonNode node = getJsonNode(text);
            ((ObjectNode) node.get(ADVANCE_LIST).get(COURT_LISTS).get(0).get(COURT_HOUSE))
                .remove(COURT_HOUSE_CODE);

            String listJson = node.toString();
            assertThrows(PayloadValidationException.class, () ->
                             validationService.validateBody(listJson, headerGroup, true),
                         CROWN_ADVANCE_PDDA_LIST_INVALID_MESSAGE);
        }
    }

    @Test
    void testValidateWithErrorsWhenCourtListCourtHouseNameMissingInCrownAdvancePddaList() throws IOException {
        try (InputStream jsonInput = this.getClass().getClassLoader()
            .getResourceAsStream(CROWN_ADVANCE_PDDA_LIST_VALID_JSON)) {
            String text = new String(jsonInput.readAllBytes(), StandardCharsets.UTF_8);

            JsonNode node = getJsonNode(text);
            ((ObjectNode) node.get(ADVANCE_LIST).get(COURT_LISTS).get(0).get(COURT_HOUSE))
                .remove(COURT_HOUSE_NAME);

            String listJson = node.toString();
            assertThrows(PayloadValidationException.class, () ->
                             validationService.validateBody(listJson, headerGroup, true),
                         CROWN_ADVANCE_PDDA_LIST_INVALID_MESSAGE);
        }
    }

    @Test
    void testValidateWithErrorsWhenWithFixedDateHearingTypeMissingInCrownAdvancePddaList() throws IOException {
        try (InputStream jsonInput = this.getClass().getClassLoader()
            .getResourceAsStream(CROWN_ADVANCE_PDDA_LIST_VALID_JSON)) {
            String text = new String(jsonInput.readAllBytes(), StandardCharsets.UTF_8);

            JsonNode node = getJsonNode(text);
            ((ObjectNode) node.get(ADVANCE_LIST).get(COURT_LISTS).get(0).get(WITH_FIXED_DATE).get(0))
                .remove(HEARING_TYPE);

            String listJson = node.toString();
            assertThrows(PayloadValidationException.class, () ->
                             validationService.validateBody(listJson, headerGroup, true),
                         CROWN_ADVANCE_PDDA_LIST_INVALID_MESSAGE);
        }
    }

    @Test
    void testValidateWithErrorsWhenWithFixedDateFixtureMissingInCrownAdvancePddaList() throws IOException {
        try (InputStream jsonInput = this.getClass().getClassLoader()
            .getResourceAsStream(CROWN_ADVANCE_PDDA_LIST_VALID_JSON)) {
            String text = new String(jsonInput.readAllBytes(), StandardCharsets.UTF_8);

            JsonNode node = getJsonNode(text);
            ((ObjectNode) node.get(ADVANCE_LIST).get(COURT_LISTS).get(0).get(WITH_FIXED_DATE).get(0))
                .remove(FIXTURE);

            String listJson = node.toString();
            assertThrows(PayloadValidationException.class, () ->
                             validationService.validateBody(listJson, headerGroup, true),
                         CROWN_ADVANCE_PDDA_LIST_INVALID_MESSAGE);
        }
    }

    @Test
    void testValidateWithErrorsWhenWithFixedDateCasesMissingInCrownAdvancePddaList() throws IOException {
        try (InputStream jsonInput = this.getClass().getClassLoader()
            .getResourceAsStream(CROWN_ADVANCE_PDDA_LIST_VALID_JSON)) {
            String text = new String(jsonInput.readAllBytes(), StandardCharsets.UTF_8);

            JsonNode node = getJsonNode(text);
            ((ObjectNode) node.get(ADVANCE_LIST).get(COURT_LISTS).get(0).get(WITH_FIXED_DATE).get(0).get(FIXTURE)
                .get(0)).remove(CASES);

            String listJson = node.toString();
            assertThrows(PayloadValidationException.class, () ->
                             validationService.validateBody(listJson, headerGroup, true),
                         CROWN_ADVANCE_PDDA_LIST_INVALID_MESSAGE);
        }
    }

    @Test
    void testValidateWithErrorsWhenWithFixedDateCaseNumberMissingInCrownAdvancePddaList() throws IOException {
        try (InputStream jsonInput = this.getClass().getClassLoader()
            .getResourceAsStream(CROWN_ADVANCE_PDDA_LIST_VALID_JSON)) {
            String text = new String(jsonInput.readAllBytes(), StandardCharsets.UTF_8);

            JsonNode node = getJsonNode(text);
            ((ObjectNode) node.get(ADVANCE_LIST).get(COURT_LISTS).get(0).get(WITH_FIXED_DATE).get(0).get(FIXTURE)
                .get(0).get(CASES).get(0)).remove(CASE_NUMBER);

            String listJson = node.toString();
            assertThrows(PayloadValidationException.class, () ->
                             validationService.validateBody(listJson, headerGroup, true),
                         CROWN_ADVANCE_PDDA_LIST_INVALID_MESSAGE);
        }
    }

    @Test
    void testValidateWithErrorsWhenWithFixedDateCaseNumberCathMissingInCrownAdvancePddaList() throws IOException {
        try (InputStream jsonInput = this.getClass().getClassLoader()
            .getResourceAsStream(CROWN_ADVANCE_PDDA_LIST_VALID_JSON)) {
            String text = new String(jsonInput.readAllBytes(), StandardCharsets.UTF_8);

            JsonNode node = getJsonNode(text);
            ((ObjectNode) node.get(ADVANCE_LIST).get(COURT_LISTS).get(0).get(WITH_FIXED_DATE).get(0).get(FIXTURE)
                .get(0).get(CASES).get(0)).remove(CASE_NUMBER_CATH);

            String listJson = node.toString();
            assertThrows(PayloadValidationException.class, () ->
                             validationService.validateBody(listJson, headerGroup, true),
                         CROWN_ADVANCE_PDDA_LIST_INVALID_MESSAGE);
        }
    }

    @Test
    void testValidateWithErrorsWhenWithFixedDateDefendantsMissingInCrownAdvancePddaList() throws IOException {
        try (InputStream jsonInput = this.getClass().getClassLoader()
            .getResourceAsStream(CROWN_ADVANCE_PDDA_LIST_VALID_JSON)) {
            String text = new String(jsonInput.readAllBytes(), StandardCharsets.UTF_8);

            JsonNode node = getJsonNode(text);
            ((ObjectNode) node.get(ADVANCE_LIST).get(COURT_LISTS).get(0).get(WITH_FIXED_DATE).get(0).get(FIXTURE)
                .get(0).get(CASES).get(0)).remove(DEFENDANTS);

            String listJson = node.toString();
            assertThrows(PayloadValidationException.class, () ->
                             validationService.validateBody(listJson, headerGroup, true),
                         CROWN_ADVANCE_PDDA_LIST_INVALID_MESSAGE);
        }
    }

    @Test
    void testValidateWithErrorsWhenWithFixedDateCourtHouseCodeMissingInCrownAdvancePddaList() throws IOException {
        try (InputStream jsonInput = this.getClass().getClassLoader()
            .getResourceAsStream(CROWN_ADVANCE_PDDA_LIST_VALID_JSON)) {
            String text = new String(jsonInput.readAllBytes(), StandardCharsets.UTF_8);

            JsonNode node = getJsonNode(text);
            ((ObjectNode) node.get(ADVANCE_LIST).get(COURT_LISTS).get(0).get(WITH_FIXED_DATE).get(0).get(FIXTURE)
                .get(0).get(CASES).get(0).get(CASE_ARRIVED_FROM).get(ORIGINATING_COURT)).remove(COURT_HOUSE_CODE);

            String listJson = node.toString();
            assertThrows(PayloadValidationException.class, () ->
                             validationService.validateBody(listJson, headerGroup, true),
                         CROWN_ADVANCE_PDDA_LIST_INVALID_MESSAGE);
        }
    }

    @Test
    void testValidateWithErrorsWhenWithFixedDateCourtHouseNameMissingInCrownAdvancePddaList() throws IOException {
        try (InputStream jsonInput = this.getClass().getClassLoader()
            .getResourceAsStream(CROWN_ADVANCE_PDDA_LIST_VALID_JSON)) {
            String text = new String(jsonInput.readAllBytes(), StandardCharsets.UTF_8);

            JsonNode node = getJsonNode(text);
            ((ObjectNode) node.get(ADVANCE_LIST).get(COURT_LISTS).get(0).get(WITH_FIXED_DATE).get(0).get(FIXTURE)
                .get(0).get(CASES).get(0).get(CASE_ARRIVED_FROM).get(ORIGINATING_COURT)).remove(COURT_HOUSE_NAME);

            String listJson = node.toString();
            assertThrows(PayloadValidationException.class, () ->
                             validationService.validateBody(listJson, headerGroup, true),
                         CROWN_ADVANCE_PDDA_LIST_INVALID_MESSAGE);
        }
    }

    @Test
    void testValidateWithErrorsWhenWithFixedDateHearingDescriptionMissingInCrownAdvancePddaList() throws IOException {
        try (InputStream jsonInput = this.getClass().getClassLoader()
            .getResourceAsStream(CROWN_ADVANCE_PDDA_LIST_VALID_JSON)) {
            String text = new String(jsonInput.readAllBytes(), StandardCharsets.UTF_8);

            JsonNode node = getJsonNode(text);
            ((ObjectNode) node.get(ADVANCE_LIST).get(COURT_LISTS).get(0).get(WITH_FIXED_DATE).get(0).get(FIXTURE)
                .get(0).get(CASES).get(0).get(HEARING).get(0)).remove(HEARING_DESCRIPTION);

            String listJson = node.toString();
            assertThrows(PayloadValidationException.class, () ->
                             validationService.validateBody(listJson, headerGroup, true),
                         CROWN_ADVANCE_PDDA_LIST_INVALID_MESSAGE);
        }
    }

    @Test
    void testValidateWithErrorsWhenWithFixedDateHearingHearingTypeMissingInCrownAdvancePddaList() throws IOException {
        try (InputStream jsonInput = this.getClass().getClassLoader()
            .getResourceAsStream(CROWN_ADVANCE_PDDA_LIST_VALID_JSON)) {
            String text = new String(jsonInput.readAllBytes(), StandardCharsets.UTF_8);

            JsonNode node = getJsonNode(text);
            ((ObjectNode) node.get(ADVANCE_LIST).get(COURT_LISTS).get(0).get(WITH_FIXED_DATE).get(0).get(FIXTURE)
                .get(0).get(CASES).get(0).get(HEARING).get(0)).remove(HEARING_TYPE);

            String listJson = node.toString();
            assertThrows(PayloadValidationException.class, () ->
                             validationService.validateBody(listJson, headerGroup, true),
                         CROWN_ADVANCE_PDDA_LIST_INVALID_MESSAGE);
        }
    }

    @Test
    void testValidateWithErrorsWhenWithFixedDatePersonalDetailsMissingInCrownAdvancePddaList() throws IOException {
        try (InputStream jsonInput = this.getClass().getClassLoader()
            .getResourceAsStream(CROWN_ADVANCE_PDDA_LIST_VALID_JSON)) {
            String text = new String(jsonInput.readAllBytes(), StandardCharsets.UTF_8);

            JsonNode node = getJsonNode(text);
            ((ObjectNode) node.get(ADVANCE_LIST).get(COURT_LISTS).get(0).get(WITH_FIXED_DATE).get(0).get(FIXTURE)
                .get(0).get(CASES).get(0).get(DEFENDANTS).get(0)).remove(PERSONAL_DETAILS);

            String listJson = node.toString();
            assertThrows(PayloadValidationException.class, () ->
                             validationService.validateBody(listJson, headerGroup, true),
                         CROWN_ADVANCE_PDDA_LIST_INVALID_MESSAGE);
        }
    }

    @Test
    void testValidateWithErrorsWhenWithFixedDateNameMissingInCrownAdvancePddaList() throws IOException {
        try (InputStream jsonInput = this.getClass().getClassLoader()
            .getResourceAsStream(CROWN_ADVANCE_PDDA_LIST_VALID_JSON)) {
            String text = new String(jsonInput.readAllBytes(), StandardCharsets.UTF_8);

            JsonNode node = getJsonNode(text);
            ((ObjectNode) node.get(ADVANCE_LIST).get(COURT_LISTS).get(0).get(WITH_FIXED_DATE).get(0).get(FIXTURE)
                .get(0).get(CASES).get(0).get(DEFENDANTS).get(0).get(PERSONAL_DETAILS)).remove(NAME);

            String listJson = node.toString();
            assertThrows(PayloadValidationException.class, () ->
                             validationService.validateBody(listJson, headerGroup, true),
                         CROWN_ADVANCE_PDDA_LIST_INVALID_MESSAGE);
        }
    }

    @Test
    void testValidateWithErrorsWhenWithFixedDateIsMaskedMissingInCrownAdvancePddaList() throws IOException {
        try (InputStream jsonInput = this.getClass().getClassLoader()
            .getResourceAsStream(CROWN_ADVANCE_PDDA_LIST_VALID_JSON)) {
            String text = new String(jsonInput.readAllBytes(), StandardCharsets.UTF_8);

            JsonNode node = getJsonNode(text);
            ((ObjectNode) node.get(ADVANCE_LIST).get(COURT_LISTS).get(0).get(WITH_FIXED_DATE).get(0).get(FIXTURE)
                .get(0).get(CASES).get(0).get(DEFENDANTS).get(0).get(PERSONAL_DETAILS)).remove(IS_MASKED);

            String listJson = node.toString();
            assertThrows(PayloadValidationException.class, () ->
                             validationService.validateBody(listJson, headerGroup, true),
                         CROWN_ADVANCE_PDDA_LIST_INVALID_MESSAGE);
        }
    }

    @Test
    void testValidateWithErrorsWhenWithFixedDateLocationMissingInCrownAdvancePddaList() throws IOException {
        try (InputStream jsonInput = this.getClass().getClassLoader()
            .getResourceAsStream(CROWN_ADVANCE_PDDA_LIST_VALID_JSON)) {
            String text = new String(jsonInput.readAllBytes(), StandardCharsets.UTF_8);

            JsonNode node = getJsonNode(text);
            ((ObjectNode) node.get(ADVANCE_LIST).get(COURT_LISTS).get(0).get(WITH_FIXED_DATE).get(0).get(FIXTURE)
                .get(0).get(CASES).get(0).get(DEFENDANTS).get(0).get("PrisonLocation")).remove(LOCATION);

            String listJson = node.toString();
            assertThrows(PayloadValidationException.class, () ->
                             validationService.validateBody(listJson, headerGroup, true),
                         CROWN_ADVANCE_PDDA_LIST_INVALID_MESSAGE);
        }
    }

    @Test
    void testValidateWithErrorsWhenWithFixedDateOrganisationNameMissingInCrownAdvancePddaList() throws IOException {
        try (InputStream jsonInput = this.getClass().getClassLoader()
            .getResourceAsStream(CROWN_ADVANCE_PDDA_LIST_VALID_JSON)) {
            String text = new String(jsonInput.readAllBytes(), StandardCharsets.UTF_8);

            JsonNode node = getJsonNode(text);
            ((ObjectNode) node.get(ADVANCE_LIST).get(COURT_LISTS).get(0).get(WITH_FIXED_DATE).get(0).get(FIXTURE)
                .get(0).get(CASES).get(0).get(DEFENDANTS).get(0).get("Counsel").get(0).get("Solicitor").get(0)
                .get("Party").get("Organisation")).remove(ORGANISATION_NAME);

            String listJson = node.toString();
            assertThrows(PayloadValidationException.class, () ->
                             validationService.validateBody(listJson, headerGroup, true),
                         CROWN_ADVANCE_PDDA_LIST_INVALID_MESSAGE);
        }
    }

    @Test
    void testValidateWithErrorsWhenWithFixedDateOffenceStatementMissingInCrownAdvancePddaList() throws IOException {
        try (InputStream jsonInput = this.getClass().getClassLoader()
            .getResourceAsStream(CROWN_ADVANCE_PDDA_LIST_VALID_JSON)) {
            String text = new String(jsonInput.readAllBytes(), StandardCharsets.UTF_8);

            JsonNode node = getJsonNode(text);
            ((ObjectNode) node.get(ADVANCE_LIST).get(COURT_LISTS).get(0).get(WITH_FIXED_DATE).get(0).get(FIXTURE)
                .get(0).get(CASES).get(0).get(DEFENDANTS).get(0).get("Charges").get(0)).remove(OFFENCE_STATEMENT);

            String listJson = node.toString();
            assertThrows(PayloadValidationException.class, () ->
                             validationService.validateBody(listJson, headerGroup, true),
                         CROWN_ADVANCE_PDDA_LIST_INVALID_MESSAGE);
        }
    }

    @Test
    void testValidateWithErrorsWhenWithFixedDateProsecutionOrgNameMissingInCrownAdvancePddaList() throws IOException {
        try (InputStream jsonInput = this.getClass().getClassLoader()
            .getResourceAsStream(CROWN_ADVANCE_PDDA_LIST_VALID_JSON)) {
            String text = new String(jsonInput.readAllBytes(), StandardCharsets.UTF_8);

            JsonNode node = getJsonNode(text);
            ((ObjectNode) node.get(ADVANCE_LIST).get(COURT_LISTS).get(0).get(WITH_FIXED_DATE).get(0).get(FIXTURE)
                .get(0).get(CASES).get(0).get("Prosecution").get("ProsecutingOrganisation")).remove(ORGANISATION_NAME);

            String listJson = node.toString();
            assertThrows(PayloadValidationException.class, () ->
                             validationService.validateBody(listJson, headerGroup, true),
                         CROWN_ADVANCE_PDDA_LIST_INVALID_MESSAGE);
        }
    }

    @Test
    void testValidateWithErrorsWhenWithFixedDateLinkedCaseNumberMissingInCrownAdvancePddaList() throws IOException {
        try (InputStream jsonInput = this.getClass().getClassLoader()
            .getResourceAsStream(CROWN_ADVANCE_PDDA_LIST_VALID_JSON)) {
            String text = new String(jsonInput.readAllBytes(), StandardCharsets.UTF_8);

            JsonNode node = getJsonNode(text);
            ((ObjectNode) node.get(ADVANCE_LIST).get(COURT_LISTS).get(0).get(WITH_FIXED_DATE).get(0).get(FIXTURE)
                .get(0).get(CASES).get(0).get("LinkedCases").get(0)).remove(CASE_NUMBER);

            String listJson = node.toString();
            assertThrows(
                PayloadValidationException.class, () ->
                    validationService.validateBody(listJson, headerGroup, true),
                CROWN_ADVANCE_PDDA_LIST_INVALID_MESSAGE
            );
        }
    }

    @Test
    void testValidateWithErrorsWhenFixedDateMissingInCrownAdvancePddaList() throws IOException {
        try (InputStream jsonInput = this.getClass().getClassLoader()
            .getResourceAsStream(CROWN_ADVANCE_PDDA_LIST_VALID_JSON)) {
            String text = new String(jsonInput.readAllBytes(), StandardCharsets.UTF_8);

            JsonNode node = getJsonNode(text);
            ((ObjectNode) node.get(ADVANCE_LIST).get(COURT_LISTS).get(0)).remove(WITHOUT_FIXED_DATE);

            String listJson = node.toString();
            assertThrows(PayloadValidationException.class, () ->
                             validationService.validateBody(listJson, headerGroup, true),
                         CROWN_ADVANCE_PDDA_LIST_INVALID_MESSAGE);
        }
    }

    @Test
    void testValidateWithErrorsWhenFixedDateHearingTypeMissingInCrownAdvancePddaList() throws IOException {
        try (InputStream jsonInput = this.getClass().getClassLoader()
            .getResourceAsStream(CROWN_ADVANCE_PDDA_LIST_VALID_JSON)) {
            String text = new String(jsonInput.readAllBytes(), StandardCharsets.UTF_8);

            JsonNode node = getJsonNode(text);
            ((ObjectNode) node.get(ADVANCE_LIST).get(COURT_LISTS).get(0).get(WITHOUT_FIXED_DATE).get(0))
                .remove(HEARING_TYPE);

            String listJson = node.toString();
            assertThrows(PayloadValidationException.class, () ->
                             validationService.validateBody(listJson, headerGroup, true),
                         CROWN_ADVANCE_PDDA_LIST_INVALID_MESSAGE);
        }
    }

    @Test
    void testValidateWithErrorsWhenFixedDateFixtureMissingInCrownAdvancePddaList() throws IOException {
        try (InputStream jsonInput = this.getClass().getClassLoader()
            .getResourceAsStream(CROWN_ADVANCE_PDDA_LIST_VALID_JSON)) {
            String text = new String(jsonInput.readAllBytes(), StandardCharsets.UTF_8);

            JsonNode node = getJsonNode(text);
            ((ObjectNode) node.get(ADVANCE_LIST).get(COURT_LISTS).get(0)
                .get(WITHOUT_FIXED_DATE).get(0)).remove(FIXTURE);

            String listJson = node.toString();
            assertThrows(PayloadValidationException.class, () ->
                             validationService.validateBody(listJson, headerGroup, true),
                         CROWN_ADVANCE_PDDA_LIST_INVALID_MESSAGE);
        }
    }

    @Test
    void testValidateWithErrorsWhenFixedDateCasesMissingInCrownAdvancePddaList() throws IOException {
        try (InputStream jsonInput = this.getClass().getClassLoader()
            .getResourceAsStream(CROWN_ADVANCE_PDDA_LIST_VALID_JSON)) {
            String text = new String(jsonInput.readAllBytes(), StandardCharsets.UTF_8);

            JsonNode node = getJsonNode(text);
            ((ObjectNode) node.get(ADVANCE_LIST).get(COURT_LISTS).get(0)
                .get(WITHOUT_FIXED_DATE).get(0).get(FIXTURE).get(0))
                .remove(CASES);

            String listJson = node.toString();
            assertThrows(PayloadValidationException.class, () ->
                             validationService.validateBody(listJson, headerGroup, true),
                         CROWN_ADVANCE_PDDA_LIST_INVALID_MESSAGE);
        }
    }

    @Test
    void testValidateWithErrorsWhenFixedDateCaseNumberMissingInCrownAdvancePddaList() throws IOException {
        try (InputStream jsonInput = this.getClass().getClassLoader()
            .getResourceAsStream(CROWN_ADVANCE_PDDA_LIST_VALID_JSON)) {
            String text = new String(jsonInput.readAllBytes(), StandardCharsets.UTF_8);

            JsonNode node = getJsonNode(text);
            ((ObjectNode) node.get(ADVANCE_LIST).get(COURT_LISTS).get(0)
                .get(WITHOUT_FIXED_DATE).get(0).get(FIXTURE).get(0)
                .get(CASES).get(0)).remove(CASE_NUMBER);

            String listJson = node.toString();
            assertThrows(PayloadValidationException.class, () ->
                             validationService.validateBody(listJson, headerGroup, true),
                         CROWN_ADVANCE_PDDA_LIST_INVALID_MESSAGE);
        }
    }

    @Test
    void testValidateWithErrorsWhenFixedDateCaseNumberCathMissingInCrownAdvancePddaList() throws IOException {
        try (InputStream jsonInput = this.getClass().getClassLoader()
            .getResourceAsStream(CROWN_ADVANCE_PDDA_LIST_VALID_JSON)) {
            String text = new String(jsonInput.readAllBytes(), StandardCharsets.UTF_8);

            JsonNode node = getJsonNode(text);
            ((ObjectNode) node.get(ADVANCE_LIST).get(COURT_LISTS).get(0)
                .get(WITHOUT_FIXED_DATE).get(0).get(FIXTURE).get(0)
                .get(CASES).get(0)).remove(CASE_NUMBER_CATH);

            String listJson = node.toString();
            assertThrows(PayloadValidationException.class, () ->
                             validationService.validateBody(listJson, headerGroup, true),
                         CROWN_ADVANCE_PDDA_LIST_INVALID_MESSAGE);
        }
    }

    @Test
    void testValidateWithErrorsWhenFixedDateDefendantsMissingInCrownAdvancePddaList() throws IOException {
        try (InputStream jsonInput = this.getClass().getClassLoader()
            .getResourceAsStream(CROWN_ADVANCE_PDDA_LIST_VALID_JSON)) {
            String text = new String(jsonInput.readAllBytes(), StandardCharsets.UTF_8);

            JsonNode node = getJsonNode(text);
            ((ObjectNode) node.get(ADVANCE_LIST).get(COURT_LISTS).get(0)
                .get(WITHOUT_FIXED_DATE).get(0).get(FIXTURE).get(0)
                .get(CASES).get(0)).remove(DEFENDANTS);

            String listJson = node.toString();
            assertThrows(PayloadValidationException.class, () ->
                             validationService.validateBody(listJson, headerGroup, true),
                         CROWN_ADVANCE_PDDA_LIST_INVALID_MESSAGE);
        }
    }

    @Test
    void testValidateWithErrorsWhenFixedDateCourtHouseCodeMissingInCrownAdvancePddaList() throws IOException {
        try (InputStream jsonInput = this.getClass().getClassLoader()
            .getResourceAsStream(CROWN_ADVANCE_PDDA_LIST_VALID_JSON)) {
            String text = new String(jsonInput.readAllBytes(), StandardCharsets.UTF_8);

            JsonNode node = getJsonNode(text);
            ((ObjectNode) node.get(ADVANCE_LIST).get(COURT_LISTS).get(0)
                .get(WITHOUT_FIXED_DATE).get(0).get(FIXTURE).get(0)
                .get(CASES).get(0).get(CASE_ARRIVED_FROM)).remove(SECTION_53);

            String listJson = node.toString();
            assertThrows(PayloadValidationException.class, () ->
                             validationService.validateBody(listJson, headerGroup, true),
                         CROWN_ADVANCE_PDDA_LIST_INVALID_MESSAGE);
        }
    }

    @Test
    void testValidateWithErrorsFWhenFixedDateCourtHouseNameMissingInCrownAdvancePddaList() throws IOException {
        try (InputStream jsonInput = this.getClass().getClassLoader()
            .getResourceAsStream(CROWN_ADVANCE_PDDA_LIST_VALID_JSON)) {
            String text = new String(jsonInput.readAllBytes(), StandardCharsets.UTF_8);

            JsonNode node = getJsonNode(text);
            ((ObjectNode) node.get(ADVANCE_LIST).get(COURT_LISTS).get(0)
                .get(WITHOUT_FIXED_DATE).get(0).get(FIXTURE).get(0)
                .get(CASES).get(0).get(CASE_ARRIVED_FROM)).remove(SECTION_53);

            String listJson = node.toString();
            assertThrows(PayloadValidationException.class, () ->
                             validationService.validateBody(listJson, headerGroup, true),
                         CROWN_ADVANCE_PDDA_LIST_INVALID_MESSAGE);
        }
    }

    @Test
    void testValidateWithErrorsWhenFixedDateHearingDescriptionMissingInCrownAdvancePddaList() throws IOException {
        try (InputStream jsonInput = this.getClass().getClassLoader()
            .getResourceAsStream(CROWN_ADVANCE_PDDA_LIST_VALID_JSON)) {
            String text = new String(jsonInput.readAllBytes(), StandardCharsets.UTF_8);

            JsonNode node = getJsonNode(text);
            ((ObjectNode) node.get(ADVANCE_LIST).get(COURT_LISTS).get(0)
                .get(WITHOUT_FIXED_DATE).get(0).get(FIXTURE).get(0)
                .get(CASES).get(0).get(HEARING).get(0)).remove(HEARING_DESCRIPTION);

            String listJson = node.toString();
            assertThrows(PayloadValidationException.class, () ->
                             validationService.validateBody(listJson, headerGroup, true),
                         CROWN_ADVANCE_PDDA_LIST_INVALID_MESSAGE);
        }
    }

    @Test
    void testValidateWithErrorsWhenFixedDateHearingHearingTypeMissingInCrownAdvancePddaList() throws IOException {
        try (InputStream jsonInput = this.getClass().getClassLoader()
            .getResourceAsStream(CROWN_ADVANCE_PDDA_LIST_VALID_JSON)) {
            String text = new String(jsonInput.readAllBytes(), StandardCharsets.UTF_8);

            JsonNode node = getJsonNode(text);
            ((ObjectNode) node.get(ADVANCE_LIST).get(COURT_LISTS).get(0)
                .get(WITHOUT_FIXED_DATE).get(0).get(FIXTURE).get(0)
                .get(CASES).get(0).get(HEARING).get(0)).remove(HEARING_TYPE);

            String listJson = node.toString();
            assertThrows(PayloadValidationException.class, () ->
                             validationService.validateBody(listJson, headerGroup, true),
                         CROWN_ADVANCE_PDDA_LIST_INVALID_MESSAGE);
        }
    }

    @Test
    void testValidateWithErrorsWhenFixedDatePersonalDetailsMissingInCrownAdvancePddaList() throws IOException {
        try (InputStream jsonInput = this.getClass().getClassLoader()
            .getResourceAsStream(CROWN_ADVANCE_PDDA_LIST_VALID_JSON)) {
            String text = new String(jsonInput.readAllBytes(), StandardCharsets.UTF_8);

            JsonNode node = getJsonNode(text);
            ((ObjectNode) node.get(ADVANCE_LIST).get(COURT_LISTS).get(0)
                .get(WITHOUT_FIXED_DATE).get(0).get(FIXTURE).get(0)
                .get(CASES).get(0).get(DEFENDANTS).get(0)).remove(PERSONAL_DETAILS);

            String listJson = node.toString();
            assertThrows(PayloadValidationException.class, () ->
                             validationService.validateBody(listJson, headerGroup, true),
                         CROWN_ADVANCE_PDDA_LIST_INVALID_MESSAGE);
        }
    }

    @Test
    void testValidateWithErrorsWhenFixedDateNameMissingInCrownAdvancePddaList() throws IOException {
        try (InputStream jsonInput = this.getClass().getClassLoader()
            .getResourceAsStream(CROWN_ADVANCE_PDDA_LIST_VALID_JSON)) {
            String text = new String(jsonInput.readAllBytes(), StandardCharsets.UTF_8);

            JsonNode node = getJsonNode(text);
            ((ObjectNode) node.get(ADVANCE_LIST).get(COURT_LISTS).get(0)
                .get(WITHOUT_FIXED_DATE).get(0).get(FIXTURE).get(0)
                .get(CASES).get(0).get(DEFENDANTS).get(0).get(PERSONAL_DETAILS)).remove(NAME);

            String listJson = node.toString();
            assertThrows(PayloadValidationException.class, () ->
                             validationService.validateBody(listJson, headerGroup, true),
                         CROWN_ADVANCE_PDDA_LIST_INVALID_MESSAGE);
        }
    }

    @Test
    void testValidateWithErrorsWhenFixedDateIsMaskedMissingInCrownAdvancePddaList() throws IOException {
        try (InputStream jsonInput = this.getClass().getClassLoader()
            .getResourceAsStream(CROWN_ADVANCE_PDDA_LIST_VALID_JSON)) {
            String text = new String(jsonInput.readAllBytes(), StandardCharsets.UTF_8);

            JsonNode node = getJsonNode(text);
            ((ObjectNode) node.get(ADVANCE_LIST).get(COURT_LISTS).get(0)
                .get(WITHOUT_FIXED_DATE).get(0).get(FIXTURE).get(0)
                .get(CASES).get(0).get(DEFENDANTS).get(0).get(PERSONAL_DETAILS)).remove(IS_MASKED);

            String listJson = node.toString();
            assertThrows(PayloadValidationException.class, () ->
                             validationService.validateBody(listJson, headerGroup, true),
                         CROWN_ADVANCE_PDDA_LIST_INVALID_MESSAGE);
        }
    }

    @Test
    void testValidateWithErrorsWhenFixedDateLocationMissingInCrownAdvancePddaList() throws IOException {
        try (InputStream jsonInput = this.getClass().getClassLoader()
            .getResourceAsStream(CROWN_ADVANCE_PDDA_LIST_VALID_JSON)) {
            String text = new String(jsonInput.readAllBytes(), StandardCharsets.UTF_8);

            JsonNode node = getJsonNode(text);
            ((ObjectNode) node.get(ADVANCE_LIST).get(COURT_LISTS).get(0)
                .get(WITHOUT_FIXED_DATE).get(0).get(FIXTURE).get(0)
                .get(CASES).get(0).get(DEFENDANTS).get(0).get("PrisonLocation")).remove(LOCATION);

            String listJson = node.toString();
            assertThrows(PayloadValidationException.class, () ->
                             validationService.validateBody(listJson, headerGroup, true),
                         CROWN_ADVANCE_PDDA_LIST_INVALID_MESSAGE);
        }
    }

    @Test
    void testValidateWithErrorsWhenFixedDateOrganisationNameMissingInCrownAdvancePddaList() throws IOException {
        try (InputStream jsonInput = this.getClass().getClassLoader()
            .getResourceAsStream(CROWN_ADVANCE_PDDA_LIST_VALID_JSON)) {
            String text = new String(jsonInput.readAllBytes(), StandardCharsets.UTF_8);

            JsonNode node = getJsonNode(text);
            ((ObjectNode) node.get(ADVANCE_LIST).get(COURT_LISTS).get(0)
                .get(WITHOUT_FIXED_DATE).get(0).get(FIXTURE).get(0)
                .get(CASES).get(0).get(DEFENDANTS).get(0).get("Counsel").get(0)
                .get("Solicitor").get(0).get("Party")
                .get("Organisation")).remove(ORGANISATION_NAME);

            String listJson = node.toString();
            assertThrows(PayloadValidationException.class, () ->
                             validationService.validateBody(listJson, headerGroup, true),
                         CROWN_ADVANCE_PDDA_LIST_INVALID_MESSAGE);
        }
    }

    @Test
    void testValidateWithErrorsWhenFixedDateOffenceStatementMissingInCrownAdvancePddaList() throws IOException {
        try (InputStream jsonInput = this.getClass().getClassLoader()
            .getResourceAsStream(CROWN_ADVANCE_PDDA_LIST_VALID_JSON)) {
            String text = new String(jsonInput.readAllBytes(), StandardCharsets.UTF_8);

            JsonNode node = getJsonNode(text);
            ((ObjectNode) node.get(ADVANCE_LIST).get(COURT_LISTS)
                .get(0).get(WITHOUT_FIXED_DATE).get(0).get(FIXTURE).get(0)
                .get(CASES).get(0).get(DEFENDANTS).get(0).get("Charges")
                .get(0)).remove(OFFENCE_STATEMENT);

            String listJson = node.toString();
            assertThrows(PayloadValidationException.class, () ->
                             validationService.validateBody(listJson, headerGroup, true),
                         CROWN_ADVANCE_PDDA_LIST_INVALID_MESSAGE);
        }
    }

    @Test
    void testValidateWithErrorsWhenFixedDateProsecutionOrgNameMissingInCrownAdvancePddaList() throws IOException {
        try (InputStream jsonInput = this.getClass().getClassLoader()
            .getResourceAsStream(CROWN_ADVANCE_PDDA_LIST_VALID_JSON)) {
            String text = new String(jsonInput.readAllBytes(), StandardCharsets.UTF_8);

            JsonNode node = getJsonNode(text);
            ((ObjectNode) node.get(ADVANCE_LIST).get(COURT_LISTS).get(0)
                .get(WITHOUT_FIXED_DATE).get(0).get(FIXTURE).get(0)
                .get(CASES).get(0).get("Prosecution").get("ProsecutingOrganisation")).remove(ORGANISATION_NAME);

            String listJson = node.toString();
            assertThrows(PayloadValidationException.class, () ->
                             validationService.validateBody(listJson, headerGroup, true),
                         CROWN_ADVANCE_PDDA_LIST_INVALID_MESSAGE);
        }
    }

    @Test
    void testValidateWithErrorsWhenFixedDateLinkedCaseNumberMissingInCrownAdvancePddaList() throws IOException {
        try (InputStream jsonInput = this.getClass().getClassLoader()
            .getResourceAsStream(CROWN_ADVANCE_PDDA_LIST_VALID_JSON)) {
            String text = new String(jsonInput.readAllBytes(), StandardCharsets.UTF_8);

            JsonNode node = getJsonNode(text);
            ((ObjectNode) node.get(ADVANCE_LIST).get(COURT_LISTS).get(0)
                .get(WITHOUT_FIXED_DATE).get(0).get(FIXTURE).get(0)
                .get(CASES).get(0).get("LinkedCases").get(0)).remove(CASE_NUMBER);

            String listJson = node.toString();
            assertThrows(PayloadValidationException.class, () ->
                             validationService.validateBody(listJson, headerGroup, true),
                         CROWN_ADVANCE_PDDA_LIST_INVALID_MESSAGE);
        }
    }

}
