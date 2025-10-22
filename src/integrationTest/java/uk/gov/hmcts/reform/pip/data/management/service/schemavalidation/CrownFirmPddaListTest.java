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
class CrownFirmPddaListTest extends IntegrationBasicTestBase {

    @Autowired
    ValidationService validationService;

    private static final String CROWN_FIRM_PDDA_LIST_VALID_JSON =
        "data/crown-firm-pdda-list/crownFirmPddaList.json";
    private static final String CROWN_FIRM_PDDA_LIST_INVALID_MESSAGE =
        "Invalid crown daily list marked as valid";

    private static final String FIRM_LIST_SCHEMA = "FirmList";
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
    private static final String SITTING_DATE = "SittingDate";
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
    private static final String CASE_NUMBER_CATH = "CaseNumberCaTH";
    private static final String PROSECUTION = "Prosecution";
    private static final String ADVOCATE = "Advocate";
    private static final String PERSONAL_DETAILS = "PersonalDetails";
    private static final String NAME = "Name";
    private static final String IS_MASKED = "IsMasked";
    private static final String DEFENDANTS = "Defendants";
    private static final String RESERVE_LIST = "ReserveList";

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
    void testValidateWithErrorsWhenDailyListMissingInCrownFirmPddaList() throws IOException {
        try (InputStream jsonInput = this.getClass().getClassLoader()
            .getResourceAsStream(CROWN_FIRM_PDDA_LIST_VALID_JSON)) {
            String text = new String(jsonInput.readAllBytes(), StandardCharsets.UTF_8);

            JsonNode node = getJsonNode(text);
            ((ObjectNode) node).remove(FIRM_LIST_SCHEMA);

            String listJson = node.toString();
            assertThrows(PayloadValidationException.class, () ->
                             validationService.validateBody(listJson, headerGroup, true),
                         CROWN_FIRM_PDDA_LIST_INVALID_MESSAGE);
        }
    }

    @Test
    void testValidateWithErrorsWhenDocumentIdMissingInCrownFirmPddaList() throws IOException {
        try (InputStream jsonInput = this.getClass().getClassLoader()
            .getResourceAsStream(CROWN_FIRM_PDDA_LIST_VALID_JSON)) {
            String text = new String(jsonInput.readAllBytes(), StandardCharsets.UTF_8);

            JsonNode node = getJsonNode(text);
            ((ObjectNode) node.get(FIRM_LIST_SCHEMA)).remove(DOCUMENT_ID);

            String listJson = node.toString();
            assertThrows(PayloadValidationException.class, () ->
                             validationService.validateBody(listJson, headerGroup, true),
                         CROWN_FIRM_PDDA_LIST_INVALID_MESSAGE);
        }
    }

    @Test
    void testValidateWithErrorsWhenDocumentNameMissingInCrownFirmPddaList() throws IOException {
        try (InputStream jsonInput = this.getClass().getClassLoader()
            .getResourceAsStream(CROWN_FIRM_PDDA_LIST_VALID_JSON)) {
            String text = new String(jsonInput.readAllBytes(), StandardCharsets.UTF_8);

            JsonNode node = getJsonNode(text);
            ((ObjectNode) node.get(FIRM_LIST_SCHEMA).get(DOCUMENT_ID)).remove(DOCUMENT_NAME);

            String listJson = node.toString();
            assertThrows(PayloadValidationException.class, () ->
                             validationService.validateBody(listJson, headerGroup, true),
                         CROWN_FIRM_PDDA_LIST_INVALID_MESSAGE);
        }
    }

    @Test
    void testValidateWithErrorsWhenUniqueIdMissingInCrownFirmPddaList() throws IOException {
        try (InputStream jsonInput = this.getClass().getClassLoader()
            .getResourceAsStream(CROWN_FIRM_PDDA_LIST_VALID_JSON)) {
            String text = new String(jsonInput.readAllBytes(), StandardCharsets.UTF_8);

            JsonNode node = getJsonNode(text);
            ((ObjectNode) node.get(FIRM_LIST_SCHEMA).get(DOCUMENT_ID)).remove(UNIQUE_ID);

            String listJson = node.toString();
            assertThrows(PayloadValidationException.class, () ->
                             validationService.validateBody(listJson, headerGroup, true),
                         CROWN_FIRM_PDDA_LIST_INVALID_MESSAGE);
        }
    }

    @Test
    void testValidateWithErrorsWhenDocumentTypeMissingInCrownFirmPddaList() throws IOException {
        try (InputStream jsonInput = this.getClass().getClassLoader()
            .getResourceAsStream(CROWN_FIRM_PDDA_LIST_VALID_JSON)) {
            String text = new String(jsonInput.readAllBytes(), StandardCharsets.UTF_8);

            JsonNode node = getJsonNode(text);
            ((ObjectNode) node.get(FIRM_LIST_SCHEMA).get(DOCUMENT_ID)).remove(DOCUMENT_TYPE);

            String listJson = node.toString();
            assertThrows(PayloadValidationException.class, () ->
                             validationService.validateBody(listJson, headerGroup, true),
                         CROWN_FIRM_PDDA_LIST_INVALID_MESSAGE);
        }
    }

    @Test
    void testValidateWithErrorsWhenListHeaderMissingInCrownFirmPddaList() throws IOException {
        try (InputStream jsonInput = this.getClass().getClassLoader()
            .getResourceAsStream(CROWN_FIRM_PDDA_LIST_VALID_JSON)) {
            String text = new String(jsonInput.readAllBytes(), StandardCharsets.UTF_8);

            JsonNode node = getJsonNode(text);
            ((ObjectNode) node.get(FIRM_LIST_SCHEMA)).remove(LIST_HEADER);

            String listJson = node.toString();
            assertThrows(PayloadValidationException.class, () ->
                             validationService.validateBody(listJson, headerGroup, true),
                         CROWN_FIRM_PDDA_LIST_INVALID_MESSAGE);
        }
    }

    @Test
    void testValidateWithErrorsWhenStartDateMissingInCrownFirmPddaList() throws IOException {
        try (InputStream jsonInput = this.getClass().getClassLoader()
            .getResourceAsStream(CROWN_FIRM_PDDA_LIST_VALID_JSON)) {
            String text = new String(jsonInput.readAllBytes(), StandardCharsets.UTF_8);

            JsonNode node = getJsonNode(text);
            ((ObjectNode) node.get(FIRM_LIST_SCHEMA).get(LIST_HEADER)).remove(START_DATE);

            String listJson = node.toString();
            assertThrows(PayloadValidationException.class, () ->
                             validationService.validateBody(listJson, headerGroup, true),
                         CROWN_FIRM_PDDA_LIST_INVALID_MESSAGE);
        }
    }

    @Test
    void testValidateWithErrorsWhenVersionMissingInCrownFirmPddaList() throws IOException {
        try (InputStream jsonInput = this.getClass().getClassLoader()
            .getResourceAsStream(CROWN_FIRM_PDDA_LIST_VALID_JSON)) {
            String text = new String(jsonInput.readAllBytes(), StandardCharsets.UTF_8);

            JsonNode node = getJsonNode(text);
            ((ObjectNode) node.get(FIRM_LIST_SCHEMA).get(LIST_HEADER)).remove(VERSION);

            String listJson = node.toString();
            assertThrows(PayloadValidationException.class, () ->
                             validationService.validateBody(listJson, headerGroup, true),
                         CROWN_FIRM_PDDA_LIST_INVALID_MESSAGE);
        }
    }

    @Test
    void testValidateWithErrorsWhenPublishedTimeMissingInCrownFirmPddaList() throws IOException {
        try (InputStream jsonInput = this.getClass().getClassLoader()
            .getResourceAsStream(CROWN_FIRM_PDDA_LIST_VALID_JSON)) {
            String text = new String(jsonInput.readAllBytes(), StandardCharsets.UTF_8);

            JsonNode node = getJsonNode(text);
            ((ObjectNode) node.get(FIRM_LIST_SCHEMA).get(LIST_HEADER)).remove(PUBLISHED_TIME);

            String listJson = node.toString();
            assertThrows(PayloadValidationException.class, () ->
                             validationService.validateBody(listJson, headerGroup, true),
                         CROWN_FIRM_PDDA_LIST_INVALID_MESSAGE);
        }
    }

    @Test
    void testValidateWithErrorsWhenCrownCourtMissingInCrownFirmPddaList() throws IOException {
        try (InputStream jsonInput = this.getClass().getClassLoader()
            .getResourceAsStream(CROWN_FIRM_PDDA_LIST_VALID_JSON)) {
            String text = new String(jsonInput.readAllBytes(), StandardCharsets.UTF_8);

            JsonNode node = getJsonNode(text);
            ((ObjectNode) node.get(FIRM_LIST_SCHEMA)).remove(CROWN_COURT);

            String listJson = node.toString();
            assertThrows(PayloadValidationException.class, () ->
                             validationService.validateBody(listJson, headerGroup, true),
                         CROWN_FIRM_PDDA_LIST_INVALID_MESSAGE);
        }
    }

    @Test
    void testValidateWithErrorsWhenCourtHouseTypeMissingInCrownFirmPddaList() throws IOException {
        try (InputStream jsonInput = this.getClass().getClassLoader()
            .getResourceAsStream(CROWN_FIRM_PDDA_LIST_VALID_JSON)) {
            String text = new String(jsonInput.readAllBytes(), StandardCharsets.UTF_8);

            JsonNode node = getJsonNode(text);
            ((ObjectNode) node.get(FIRM_LIST_SCHEMA).get(CROWN_COURT)).remove(COURT_HOUSE_TYPE);

            String listJson = node.toString();
            assertThrows(PayloadValidationException.class, () ->
                             validationService.validateBody(listJson, headerGroup, true),
                         CROWN_FIRM_PDDA_LIST_INVALID_MESSAGE);
        }
    }

    @Test
    void testValidateWithErrorsWhenCourtListsCourtHouseTypeMissingInCrownFirmPddaList() throws IOException {
        try (InputStream jsonInput = this.getClass().getClassLoader()
            .getResourceAsStream(CROWN_FIRM_PDDA_LIST_VALID_JSON)) {
            String text = new String(jsonInput.readAllBytes(), StandardCharsets.UTF_8);

            JsonNode node = getJsonNode(text);
            ((ObjectNode) node.get(FIRM_LIST_SCHEMA).get(COURT_LISTS).get(0).get(COURT_HOUSE)).remove(COURT_HOUSE_TYPE);

            String listJson = node.toString();
            assertThrows(PayloadValidationException.class, () ->
                             validationService.validateBody(listJson, headerGroup, true),
                         CROWN_FIRM_PDDA_LIST_INVALID_MESSAGE);
        }
    }

    @Test
    void testValidateWithErrorsWhenCommittingCourtHouseTypeMissingInCrownFirmPddaList() throws IOException {
        try (InputStream jsonInput = this.getClass().getClassLoader()
            .getResourceAsStream(CROWN_FIRM_PDDA_LIST_VALID_JSON)) {
            String text = new String(jsonInput.readAllBytes(), StandardCharsets.UTF_8);

            JsonNode node = getJsonNode(text);
            ((ObjectNode) node.get(FIRM_LIST_SCHEMA).get(COURT_LISTS).get(0).get(SITTINGS).get(0).get(HEARINGS)
                .get(0).get("CommittingCourt")).remove(COURT_HOUSE_TYPE);

            String listJson = node.toString();
            assertThrows(PayloadValidationException.class, () ->
                             validationService.validateBody(listJson, headerGroup, true),
                         CROWN_FIRM_PDDA_LIST_INVALID_MESSAGE);
        }
    }


    @Test
    void testValidateWithErrorsWhenCourtHouseCodeMissingInCrownFirmPddaList() throws IOException {
        try (InputStream jsonInput = this.getClass().getClassLoader()
            .getResourceAsStream(CROWN_FIRM_PDDA_LIST_VALID_JSON)) {
            String text = new String(jsonInput.readAllBytes(), StandardCharsets.UTF_8);

            JsonNode node = getJsonNode(text);
            ((ObjectNode) node.get(FIRM_LIST_SCHEMA).get(CROWN_COURT)).remove(COURT_HOUSE_CODE);

            String listJson = node.toString();
            assertThrows(PayloadValidationException.class, () ->
                             validationService.validateBody(listJson, headerGroup, true),
                         CROWN_FIRM_PDDA_LIST_INVALID_MESSAGE);
        }
    }

    @Test
    void testValidateWithErrorsWhenCourtListsCourtHouseCodeMissingInCrownFirmPddaList() throws IOException {
        try (InputStream jsonInput = this.getClass().getClassLoader()
            .getResourceAsStream(CROWN_FIRM_PDDA_LIST_VALID_JSON)) {
            String text = new String(jsonInput.readAllBytes(), StandardCharsets.UTF_8);

            JsonNode node = getJsonNode(text);
            ((ObjectNode) node.get(FIRM_LIST_SCHEMA).get(COURT_LISTS).get(0).get(COURT_HOUSE)).remove(COURT_HOUSE_CODE);

            String listJson = node.toString();
            assertThrows(PayloadValidationException.class, () ->
                             validationService.validateBody(listJson, headerGroup, true),
                         CROWN_FIRM_PDDA_LIST_INVALID_MESSAGE);
        }
    }

    @Test
    void testValidateWithErrorsWhenCommittingCourtHouseCodeMissingInCrownFirmPddaList() throws IOException {
        try (InputStream jsonInput = this.getClass().getClassLoader()
            .getResourceAsStream(CROWN_FIRM_PDDA_LIST_VALID_JSON)) {
            String text = new String(jsonInput.readAllBytes(), StandardCharsets.UTF_8);

            JsonNode node = getJsonNode(text);
            ((ObjectNode) node.get(FIRM_LIST_SCHEMA).get(COURT_LISTS).get(0).get(SITTINGS).get(0).get(HEARINGS)
                .get(0).get("CommittingCourt")).remove(COURT_HOUSE_CODE);

            String listJson = node.toString();
            assertThrows(PayloadValidationException.class, () ->
                             validationService.validateBody(listJson, headerGroup, true),
                         CROWN_FIRM_PDDA_LIST_INVALID_MESSAGE);
        }
    }

    @Test
    void testValidateWithErrorsWhenCourtHouseNameMissingInCrownFirmPddaList() throws IOException {
        try (InputStream jsonInput = this.getClass().getClassLoader()
            .getResourceAsStream(CROWN_FIRM_PDDA_LIST_VALID_JSON)) {
            String text = new String(jsonInput.readAllBytes(), StandardCharsets.UTF_8);

            JsonNode node = getJsonNode(text);
            ((ObjectNode) node.get(FIRM_LIST_SCHEMA).get(CROWN_COURT)).remove(COURT_HOUSE_NAME);

            String listJson = node.toString();
            assertThrows(PayloadValidationException.class, () ->
                             validationService.validateBody(listJson, headerGroup, true),
                         CROWN_FIRM_PDDA_LIST_INVALID_MESSAGE);
        }
    }

    @Test
    void testValidateWithErrorsWhenCourtListsCourtHouseNameMissingInCrownFirmPddaList() throws IOException {
        try (InputStream jsonInput = this.getClass().getClassLoader()
            .getResourceAsStream(CROWN_FIRM_PDDA_LIST_VALID_JSON)) {
            String text = new String(jsonInput.readAllBytes(), StandardCharsets.UTF_8);

            JsonNode node = getJsonNode(text);
            ((ObjectNode) node.get(FIRM_LIST_SCHEMA).get(COURT_LISTS).get(0).get(COURT_HOUSE)).remove(COURT_HOUSE_NAME);

            String listJson = node.toString();
            assertThrows(PayloadValidationException.class, () ->
                             validationService.validateBody(listJson, headerGroup, true),
                         CROWN_FIRM_PDDA_LIST_INVALID_MESSAGE);
        }
    }

    @Test
    void testValidateWithErrorsWhenCommittingCourtHouseNameMissingInCrownFirmPddaList() throws IOException {
        try (InputStream jsonInput = this.getClass().getClassLoader()
            .getResourceAsStream(CROWN_FIRM_PDDA_LIST_VALID_JSON)) {
            String text = new String(jsonInput.readAllBytes(), StandardCharsets.UTF_8);

            JsonNode node = getJsonNode(text);
            ((ObjectNode) node.get(FIRM_LIST_SCHEMA).get(COURT_LISTS).get(0).get(SITTINGS).get(0).get(HEARINGS)
                .get(0).get("CommittingCourt")).remove(COURT_HOUSE_NAME);

            String listJson = node.toString();
            assertThrows(PayloadValidationException.class, () ->
                             validationService.validateBody(listJson, headerGroup, true),
                         CROWN_FIRM_PDDA_LIST_INVALID_MESSAGE);
        }
    }

    @Test
    void testValidateWithErrorsWhenCourtListsMissingInCrownFirmPddaList() throws IOException {
        try (InputStream jsonInput = this.getClass().getClassLoader()
            .getResourceAsStream(CROWN_FIRM_PDDA_LIST_VALID_JSON)) {
            String text = new String(jsonInput.readAllBytes(), StandardCharsets.UTF_8);

            JsonNode node = getJsonNode(text);
            ((ObjectNode) node.get(FIRM_LIST_SCHEMA)).remove(COURT_LISTS);

            String listJson = node.toString();
            assertThrows(PayloadValidationException.class, () ->
                             validationService.validateBody(listJson, headerGroup, true),
                         CROWN_FIRM_PDDA_LIST_INVALID_MESSAGE);
        }
    }

    @Test
    void testValidateWithErrorsWhenSittingDateMissingInCrownFirmPddaList() throws IOException {
        try (InputStream jsonInput = this.getClass().getClassLoader()
            .getResourceAsStream(CROWN_FIRM_PDDA_LIST_VALID_JSON)) {
            String text = new String(jsonInput.readAllBytes(), StandardCharsets.UTF_8);

            JsonNode node = getJsonNode(text);
            ((ObjectNode) node.get(FIRM_LIST_SCHEMA).get(COURT_LISTS).get(0)).remove(SITTING_DATE);

            String listJson = node.toString();
            assertThrows(PayloadValidationException.class, () ->
                             validationService.validateBody(listJson, headerGroup, true),
                         CROWN_FIRM_PDDA_LIST_INVALID_MESSAGE);
        }
    }

    @Test
    void testValidateWithErrorsWhenCourtHouseMissingInCrownFirmPddaList() throws IOException {
        try (InputStream jsonInput = this.getClass().getClassLoader()
            .getResourceAsStream(CROWN_FIRM_PDDA_LIST_VALID_JSON)) {
            String text = new String(jsonInput.readAllBytes(), StandardCharsets.UTF_8);

            JsonNode node = getJsonNode(text);
            ((ObjectNode) node.get(FIRM_LIST_SCHEMA).get(COURT_LISTS).get(0)).remove(COURT_HOUSE);

            String listJson = node.toString();
            assertThrows(PayloadValidationException.class, () ->
                             validationService.validateBody(listJson, headerGroup, true),
                         CROWN_FIRM_PDDA_LIST_INVALID_MESSAGE);
        }
    }

    @Test
    void testValidateWithErrorsWhenSittingsMissingInCrownFirmPddaList() throws IOException {
        try (InputStream jsonInput = this.getClass().getClassLoader()
            .getResourceAsStream(CROWN_FIRM_PDDA_LIST_VALID_JSON)) {
            String text = new String(jsonInput.readAllBytes(), StandardCharsets.UTF_8);

            JsonNode node = getJsonNode(text);
            ((ObjectNode) node.get(FIRM_LIST_SCHEMA).get(COURT_LISTS).get(0)).remove(SITTINGS);

            String listJson = node.toString();
            assertThrows(PayloadValidationException.class, () ->
                             validationService.validateBody(listJson, headerGroup, true),
                         CROWN_FIRM_PDDA_LIST_INVALID_MESSAGE);
        }
    }

    @Test
    void testValidateWithErrorsWhenCourtRoomNumberMissingInCrownFirmPddaList() throws IOException {
        try (InputStream jsonInput = this.getClass().getClassLoader()
            .getResourceAsStream(CROWN_FIRM_PDDA_LIST_VALID_JSON)) {
            String text = new String(jsonInput.readAllBytes(), StandardCharsets.UTF_8);

            JsonNode node = getJsonNode(text);
            ((ObjectNode) node.get(FIRM_LIST_SCHEMA).get(COURT_LISTS).get(0).get(SITTINGS).get(0))
                .remove(COURT_ROOM_NUMBER);

            String listJson = node.toString();
            assertThrows(PayloadValidationException.class, () ->
                             validationService.validateBody(listJson, headerGroup, true),
                         CROWN_FIRM_PDDA_LIST_INVALID_MESSAGE);
        }
    }

    @Test
    void testValidateWithErrorsWhenSittingSequenceNumberMissingInCrownFirmPddaList() throws IOException {
        try (InputStream jsonInput = this.getClass().getClassLoader()
            .getResourceAsStream(CROWN_FIRM_PDDA_LIST_VALID_JSON)) {
            String text = new String(jsonInput.readAllBytes(), StandardCharsets.UTF_8);

            JsonNode node = getJsonNode(text);
            ((ObjectNode) node.get(FIRM_LIST_SCHEMA).get(COURT_LISTS).get(0).get(SITTINGS).get(0))
                .remove("SittingSequenceNo");

            String listJson = node.toString();
            assertThrows(PayloadValidationException.class, () ->
                             validationService.validateBody(listJson, headerGroup, true),
                         CROWN_FIRM_PDDA_LIST_INVALID_MESSAGE);
        }
    }

    @Test
    void testValidateWithErrorsWhenJudiciaryMissingInCrownFirmPddaList() throws IOException {
        try (InputStream jsonInput = this.getClass().getClassLoader()
            .getResourceAsStream(CROWN_FIRM_PDDA_LIST_VALID_JSON)) {
            String text = new String(jsonInput.readAllBytes(), StandardCharsets.UTF_8);

            JsonNode node = getJsonNode(text);
            ((ObjectNode) node.get(FIRM_LIST_SCHEMA).get(COURT_LISTS).get(0).get(SITTINGS).get(0)).remove(JUDICIARY);

            String listJson = node.toString();
            assertThrows(PayloadValidationException.class, () ->
                             validationService.validateBody(listJson, headerGroup, true),
                         CROWN_FIRM_PDDA_LIST_INVALID_MESSAGE);
        }
    }

    @Test
    void testValidateWithErrorsWhenJudgeMissingInCrownFirmPddaList() throws IOException {
        try (InputStream jsonInput = this.getClass().getClassLoader()
            .getResourceAsStream(CROWN_FIRM_PDDA_LIST_VALID_JSON)) {
            String text = new String(jsonInput.readAllBytes(), StandardCharsets.UTF_8);

            JsonNode node = getJsonNode(text);
            ((ObjectNode) node.get(FIRM_LIST_SCHEMA).get(COURT_LISTS).get(0).get(SITTINGS).get(0).get(JUDICIARY))
                .remove(JUDGE);

            String listJson = node.toString();
            assertThrows(PayloadValidationException.class, () ->
                             validationService.validateBody(listJson, headerGroup, true),
                         CROWN_FIRM_PDDA_LIST_INVALID_MESSAGE);
        }
    }

    @Test
    void testValidateWithErrorsWhenCitizenSurnameMissingInCrownFirmPddaList() throws IOException {
        try (InputStream jsonInput = this.getClass().getClassLoader()
            .getResourceAsStream(CROWN_FIRM_PDDA_LIST_VALID_JSON)) {
            String text = new String(jsonInput.readAllBytes(), StandardCharsets.UTF_8);

            JsonNode node = getJsonNode(text);
            ((ObjectNode) node.get(FIRM_LIST_SCHEMA).get(COURT_LISTS).get(0).get(SITTINGS).get(0).get(JUDICIARY)
                .get(JUDGE)).remove(CITIZEN_SURNAME);

            String listJson = node.toString();
            assertThrows(PayloadValidationException.class, () ->
                             validationService.validateBody(listJson, headerGroup, true),
                         CROWN_FIRM_PDDA_LIST_INVALID_MESSAGE);
        }
    }

    @Test
    void testValidateWithErrorsWhenCaseNumberMissingInCrownFirmPddaList() throws IOException {
        try (InputStream jsonInput = this.getClass().getClassLoader()
            .getResourceAsStream(CROWN_FIRM_PDDA_LIST_VALID_JSON)) {
            String text = new String(jsonInput.readAllBytes(), StandardCharsets.UTF_8);

            JsonNode node = getJsonNode(text);
            ((ObjectNode) node.get(FIRM_LIST_SCHEMA).get(COURT_LISTS).get(0).get(SITTINGS).get(0).get(HEARINGS).get(0))
                .remove(CASE_NUMBER);

            String listJson = node.toString();
            assertThrows(PayloadValidationException.class, () ->
                             validationService.validateBody(listJson, headerGroup, true),
                         CROWN_FIRM_PDDA_LIST_INVALID_MESSAGE);
        }
    }

    @Test
    void testValidateWithErrorsWhenCaseNumberCathMissingInCrownFirmPddaList() throws IOException {
        try (InputStream jsonInput = this.getClass().getClassLoader()
            .getResourceAsStream(CROWN_FIRM_PDDA_LIST_VALID_JSON)) {
            String text = new String(jsonInput.readAllBytes(), StandardCharsets.UTF_8);

            JsonNode node = getJsonNode(text);
            ((ObjectNode) node.get(FIRM_LIST_SCHEMA).get(COURT_LISTS).get(0).get(SITTINGS).get(0).get(HEARINGS).get(0))
                .remove(CASE_NUMBER_CATH);

            String listJson = node.toString();
            assertThrows(PayloadValidationException.class, () ->
                             validationService.validateBody(listJson, headerGroup, true),
                         CROWN_FIRM_PDDA_LIST_INVALID_MESSAGE);
        }
    }

    @Test
    void testValidateWithErrorsWhenHearingSequenceNumberMissingInCrownFirmPddaList() throws IOException {
        try (InputStream jsonInput = this.getClass().getClassLoader()
            .getResourceAsStream(CROWN_FIRM_PDDA_LIST_VALID_JSON)) {
            String text = new String(jsonInput.readAllBytes(), StandardCharsets.UTF_8);

            JsonNode node = getJsonNode(text);
            ((ObjectNode) node.get(FIRM_LIST_SCHEMA).get(COURT_LISTS).get(0).get(SITTINGS).get(0).get(HEARINGS).get(0))
                .remove(HEARING_SEQUENCE_NUMBER);

            String listJson = node.toString();
            assertThrows(PayloadValidationException.class, () ->
                             validationService.validateBody(listJson, headerGroup, true),
                         CROWN_FIRM_PDDA_LIST_INVALID_MESSAGE);
        }
    }

    @Test
    void testValidateWithErrorsWhenHearingDetailsMissingInCrownFirmPddaList() throws IOException {
        try (InputStream jsonInput = this.getClass().getClassLoader()
            .getResourceAsStream(CROWN_FIRM_PDDA_LIST_VALID_JSON)) {
            String text = new String(jsonInput.readAllBytes(), StandardCharsets.UTF_8);

            JsonNode node = getJsonNode(text);
            ((ObjectNode) node.get(FIRM_LIST_SCHEMA).get(COURT_LISTS).get(0).get(SITTINGS).get(0).get(HEARINGS).get(0))
                .remove(HEARING_DETAILS);

            String listJson = node.toString();
            assertThrows(PayloadValidationException.class, () ->
                             validationService.validateBody(listJson, headerGroup, true),
                         CROWN_FIRM_PDDA_LIST_INVALID_MESSAGE);
        }
    }

    @Test
    void testValidateWithErrorsWhenHearingDescriptionMissingInCrownFirmPddaList() throws IOException {
        try (InputStream jsonInput = this.getClass().getClassLoader()
            .getResourceAsStream(CROWN_FIRM_PDDA_LIST_VALID_JSON)) {
            String text = new String(jsonInput.readAllBytes(), StandardCharsets.UTF_8);

            JsonNode node = getJsonNode(text);
            ((ObjectNode) node.get(FIRM_LIST_SCHEMA).get(COURT_LISTS).get(0).get(SITTINGS).get(0).get(HEARINGS).get(0)
                .get(HEARING_DETAILS)).remove(HEARING_DESCRIPTION);

            String listJson = node.toString();
            assertThrows(PayloadValidationException.class, () ->
                             validationService.validateBody(listJson, headerGroup, true),
                         CROWN_FIRM_PDDA_LIST_INVALID_MESSAGE);
        }
    }

    @Test
    void testValidateWithErrorsWhenAdvocatePersonalDetailsMissingInCrownFirmPddaList() throws IOException {
        try (InputStream jsonInput = this.getClass().getClassLoader()
            .getResourceAsStream(CROWN_FIRM_PDDA_LIST_VALID_JSON)) {
            String text = new String(jsonInput.readAllBytes(), StandardCharsets.UTF_8);

            JsonNode node = getJsonNode(text);
            ((ObjectNode) node.get(FIRM_LIST_SCHEMA).get(COURT_LISTS).get(0).get(SITTINGS).get(0).get(HEARINGS).get(0)
                .get(PROSECUTION).get(ADVOCATE)).remove(PERSONAL_DETAILS);

            String listJson = node.toString();
            assertThrows(PayloadValidationException.class, () ->
                             validationService.validateBody(listJson, headerGroup, true),
                         CROWN_FIRM_PDDA_LIST_INVALID_MESSAGE);
        }
    }

    @Test
    void testValidateWithErrorsWhenAdvocateNameMissingInCrownFirmPddaList() throws IOException {
        try (InputStream jsonInput = this.getClass().getClassLoader()
            .getResourceAsStream(CROWN_FIRM_PDDA_LIST_VALID_JSON)) {
            String text = new String(jsonInput.readAllBytes(), StandardCharsets.UTF_8);

            JsonNode node = getJsonNode(text);
            ((ObjectNode) node.get(FIRM_LIST_SCHEMA).get(COURT_LISTS).get(0).get(SITTINGS).get(0).get(HEARINGS).get(0)
                .get(PROSECUTION).get(ADVOCATE).get(PERSONAL_DETAILS)).remove(NAME);

            String listJson = node.toString();
            assertThrows(PayloadValidationException.class, () ->
                             validationService.validateBody(listJson, headerGroup, true),
                         CROWN_FIRM_PDDA_LIST_INVALID_MESSAGE);
        }
    }

    @Test
    void testValidateWithErrorsWhenAdvocateIsMaskedMissingInCrownFirmPddaList() throws IOException {
        try (InputStream jsonInput = this.getClass().getClassLoader()
            .getResourceAsStream(CROWN_FIRM_PDDA_LIST_VALID_JSON)) {
            String text = new String(jsonInput.readAllBytes(), StandardCharsets.UTF_8);

            JsonNode node = getJsonNode(text);
            ((ObjectNode) node.get(FIRM_LIST_SCHEMA).get(COURT_LISTS).get(0).get(SITTINGS).get(0).get(HEARINGS).get(0)
                .get(PROSECUTION).get(ADVOCATE).get(PERSONAL_DETAILS)).remove(IS_MASKED);

            String listJson = node.toString();
            assertThrows(PayloadValidationException.class, () ->
                             validationService.validateBody(listJson, headerGroup, true),
                         CROWN_FIRM_PDDA_LIST_INVALID_MESSAGE);
        }
    }

    @Test
    void testValidateWithErrorsWhenDefendantsPersonalDetailsMissingInCrownFirmPddaList() throws IOException {
        try (InputStream jsonInput = this.getClass().getClassLoader()
            .getResourceAsStream(CROWN_FIRM_PDDA_LIST_VALID_JSON)) {
            String text = new String(jsonInput.readAllBytes(), StandardCharsets.UTF_8);

            JsonNode node = getJsonNode(text);
            ((ObjectNode) node.get(FIRM_LIST_SCHEMA).get(COURT_LISTS).get(0).get(SITTINGS).get(0).get(HEARINGS).get(0)
                .get(DEFENDANTS).get(0)).remove(PERSONAL_DETAILS);

            String listJson = node.toString();
            assertThrows(PayloadValidationException.class, () ->
                             validationService.validateBody(listJson, headerGroup, true),
                         CROWN_FIRM_PDDA_LIST_INVALID_MESSAGE);
        }
    }

    @Test
    void testValidateWithErrorsWhenDefendantsNameMissingInCrownFirmPddaList() throws IOException {
        try (InputStream jsonInput = this.getClass().getClassLoader()
            .getResourceAsStream(CROWN_FIRM_PDDA_LIST_VALID_JSON)) {
            String text = new String(jsonInput.readAllBytes(), StandardCharsets.UTF_8);

            JsonNode node = getJsonNode(text);
            ((ObjectNode) node.get(FIRM_LIST_SCHEMA).get(COURT_LISTS).get(0).get(SITTINGS).get(0).get(HEARINGS).get(0)
                .get(DEFENDANTS).get(0).get(PERSONAL_DETAILS)).remove(NAME);

            String listJson = node.toString();
            assertThrows(PayloadValidationException.class, () ->
                             validationService.validateBody(listJson, headerGroup, true),
                         CROWN_FIRM_PDDA_LIST_INVALID_MESSAGE);
        }
    }

    @Test
    void testValidateWithErrorsWhenDefendantsIsMaskedMissingInCrownFirmPddaList() throws IOException {
        try (InputStream jsonInput = this.getClass().getClassLoader()
            .getResourceAsStream(CROWN_FIRM_PDDA_LIST_VALID_JSON)) {
            String text = new String(jsonInput.readAllBytes(), StandardCharsets.UTF_8);

            JsonNode node = getJsonNode(text);
            ((ObjectNode) node.get(FIRM_LIST_SCHEMA).get(COURT_LISTS).get(0).get(SITTINGS).get(0).get(HEARINGS).get(0)
                .get(DEFENDANTS).get(0).get(PERSONAL_DETAILS)).remove(IS_MASKED);

            String listJson = node.toString();
            assertThrows(PayloadValidationException.class, () ->
                             validationService.validateBody(listJson, headerGroup, true),
                         CROWN_FIRM_PDDA_LIST_INVALID_MESSAGE);
        }
    }

    @Test
    void testValidateWithErrorsWhenCounselPersonNameMissingInCrownFirmPddaList() throws IOException {
        try (InputStream jsonInput = this.getClass().getClassLoader()
            .getResourceAsStream(CROWN_FIRM_PDDA_LIST_VALID_JSON)) {
            String text = new String(jsonInput.readAllBytes(), StandardCharsets.UTF_8);

            JsonNode node = getJsonNode(text);
            ((ObjectNode) node.get(FIRM_LIST_SCHEMA).get(COURT_LISTS).get(0).get(SITTINGS).get(0).get(HEARINGS).get(0)
                .get(DEFENDANTS).get(0).get("Counsel").get(0).get("Solicitor").get(0).get("Party").get("Person"))
                .remove(NAME);

            String listJson = node.toString();
            assertThrows(PayloadValidationException.class, () ->
                             validationService.validateBody(listJson, headerGroup, true),
                         CROWN_FIRM_PDDA_LIST_INVALID_MESSAGE);
        }
    }

    @Test
    void testValidateWithErrorsWhenCounselPersonIsMaskedMissingInCrownFirmPddaList() throws IOException {
        try (InputStream jsonInput = this.getClass().getClassLoader()
            .getResourceAsStream(CROWN_FIRM_PDDA_LIST_VALID_JSON)) {
            String text = new String(jsonInput.readAllBytes(), StandardCharsets.UTF_8);

            JsonNode node = getJsonNode(text);
            ((ObjectNode) node.get(FIRM_LIST_SCHEMA).get(COURT_LISTS).get(0).get(SITTINGS).get(0).get(HEARINGS).get(0)
                .get(DEFENDANTS).get(0).get("Counsel").get(0).get("Solicitor").get(0).get("Party").get("Person"))
                .remove(IS_MASKED);

            String listJson = node.toString();
            assertThrows(PayloadValidationException.class, () ->
                             validationService.validateBody(listJson, headerGroup, true),
                         CROWN_FIRM_PDDA_LIST_INVALID_MESSAGE);
        }
    }

    @Test
    void testValidateWithErrorsWhenCounselOrganisationNameMissingInCrownFirmPddaList() throws IOException {
        try (InputStream jsonInput = this.getClass().getClassLoader()
            .getResourceAsStream(CROWN_FIRM_PDDA_LIST_VALID_JSON)) {
            String text = new String(jsonInput.readAllBytes(), StandardCharsets.UTF_8);

            JsonNode node = getJsonNode(text);
            ((ObjectNode) node.get(FIRM_LIST_SCHEMA).get(COURT_LISTS).get(1).get(SITTINGS).get(0).get(HEARINGS).get(0)
                .get(DEFENDANTS).get(0).get("Counsel").get(0).get("Solicitor").get(0).get("Party")
                .get("Organisation")).remove(ORGANISATION_NAME);

            String listJson = node.toString();
            assertThrows(PayloadValidationException.class, () ->
                             validationService.validateBody(listJson, headerGroup, true),
                         CROWN_FIRM_PDDA_LIST_INVALID_MESSAGE);
        }
    }

    @Test
    void testValidateWithErrorsWhenOffenceStatementMissingInCrownFirmPddaList() throws IOException {
        try (InputStream jsonInput = this.getClass().getClassLoader()
            .getResourceAsStream(CROWN_FIRM_PDDA_LIST_VALID_JSON)) {
            String text = new String(jsonInput.readAllBytes(), StandardCharsets.UTF_8);

            JsonNode node = getJsonNode(text);
            ((ObjectNode) node.get(FIRM_LIST_SCHEMA).get(COURT_LISTS).get(0).get(SITTINGS).get(0).get(HEARINGS).get(0)
                .get(DEFENDANTS).get(0).get("Charges").get(0)).remove(OFFENCE_STATEMENT);

            String listJson = node.toString();
            assertThrows(PayloadValidationException.class, () ->
                             validationService.validateBody(listJson, headerGroup, true),
                         CROWN_FIRM_PDDA_LIST_INVALID_MESSAGE);
        }
    }

    @Test
    void testValidateWithErrorsWhenReserveListHearingDetailsMissingInCrownFirmPddaList() throws IOException {
        try (InputStream jsonInput = this.getClass().getClassLoader()
            .getResourceAsStream(CROWN_FIRM_PDDA_LIST_VALID_JSON)) {
            String text = new String(jsonInput.readAllBytes(), StandardCharsets.UTF_8);

            JsonNode node = getJsonNode(text);
            ((ObjectNode) node.get(FIRM_LIST_SCHEMA).get(RESERVE_LIST).get(0)).remove(HEARING_DETAILS);

            String listJson = node.toString();
            assertThrows(PayloadValidationException.class, () ->
                             validationService.validateBody(listJson, headerGroup, true),
                         CROWN_FIRM_PDDA_LIST_INVALID_MESSAGE);
        }
    }

    @Test
    void testValidateWithErrorsWhenReserveListHearingDescriptionMissingInCrownFirmPddaList() throws IOException {
        try (InputStream jsonInput = this.getClass().getClassLoader()
            .getResourceAsStream(CROWN_FIRM_PDDA_LIST_VALID_JSON)) {
            String text = new String(jsonInput.readAllBytes(), StandardCharsets.UTF_8);

            JsonNode node = getJsonNode(text);
            ((ObjectNode) node.get(FIRM_LIST_SCHEMA).get(RESERVE_LIST).get(0).get(HEARING_DETAILS))
                .remove(HEARING_DESCRIPTION);

            String listJson = node.toString();
            assertThrows(PayloadValidationException.class, () ->
                             validationService.validateBody(listJson, headerGroup, true),
                         CROWN_FIRM_PDDA_LIST_INVALID_MESSAGE);
        }
    }

    @Test
    void testValidateWithErrorsWhenReserveListCaseNumberMissingInCrownFirmPddaList() throws IOException {
        try (InputStream jsonInput = this.getClass().getClassLoader()
            .getResourceAsStream(CROWN_FIRM_PDDA_LIST_VALID_JSON)) {
            String text = new String(jsonInput.readAllBytes(), StandardCharsets.UTF_8);

            JsonNode node = getJsonNode(text);
            ((ObjectNode) node.get(FIRM_LIST_SCHEMA).get(RESERVE_LIST).get(0)).remove(CASE_NUMBER);

            String listJson = node.toString();
            assertThrows(PayloadValidationException.class, () ->
                             validationService.validateBody(listJson, headerGroup, true),
                         CROWN_FIRM_PDDA_LIST_INVALID_MESSAGE);
        }
    }

    @Test
    void testValidateWithErrorsWhenReserveListAdvocatePersonalDetailsMissingInCrownFirmPddaList() throws IOException {
        try (InputStream jsonInput = this.getClass().getClassLoader()
            .getResourceAsStream(CROWN_FIRM_PDDA_LIST_VALID_JSON)) {
            String text = new String(jsonInput.readAllBytes(), StandardCharsets.UTF_8);

            JsonNode node = getJsonNode(text);
            ((ObjectNode) node.get(FIRM_LIST_SCHEMA).get(RESERVE_LIST).get(0).get(PROSECUTION).get(ADVOCATE))
                .remove(PERSONAL_DETAILS);

            String listJson = node.toString();
            assertThrows(PayloadValidationException.class, () ->
                             validationService.validateBody(listJson, headerGroup, true),
                         CROWN_FIRM_PDDA_LIST_INVALID_MESSAGE);
        }
    }

    @Test
    void testValidateWithErrorsWhenReserveListAdvocateNameMissingInCrownFirmPddaList() throws IOException {
        try (InputStream jsonInput = this.getClass().getClassLoader()
            .getResourceAsStream(CROWN_FIRM_PDDA_LIST_VALID_JSON)) {
            String text = new String(jsonInput.readAllBytes(), StandardCharsets.UTF_8);

            JsonNode node = getJsonNode(text);
            ((ObjectNode) node.get(FIRM_LIST_SCHEMA).get(RESERVE_LIST).get(0).get(PROSECUTION).get(ADVOCATE)
                .get(PERSONAL_DETAILS)).remove(NAME);

            String listJson = node.toString();
            assertThrows(PayloadValidationException.class, () ->
                             validationService.validateBody(listJson, headerGroup, true),
                         CROWN_FIRM_PDDA_LIST_INVALID_MESSAGE);
        }
    }

    @Test
    void testValidateWithErrorsWhenReserveListAdvocateIsMaskedMissingInCrownFirmPddaList() throws IOException {
        try (InputStream jsonInput = this.getClass().getClassLoader()
            .getResourceAsStream(CROWN_FIRM_PDDA_LIST_VALID_JSON)) {
            String text = new String(jsonInput.readAllBytes(), StandardCharsets.UTF_8);

            JsonNode node = getJsonNode(text);
            ((ObjectNode) node.get(FIRM_LIST_SCHEMA).get(RESERVE_LIST).get(0).get(PROSECUTION).get(ADVOCATE)
                .get(PERSONAL_DETAILS)).remove(IS_MASKED);

            String listJson = node.toString();
            assertThrows(PayloadValidationException.class, () ->
                             validationService.validateBody(listJson, headerGroup, true),
                         CROWN_FIRM_PDDA_LIST_INVALID_MESSAGE);
        }
    }

    @Test
    void testValidateWithErrorsWhenReserveListProsecutingOrgNameMissingInCrownFirmPddaList() throws IOException {
        try (InputStream jsonInput = this.getClass().getClassLoader()
            .getResourceAsStream(CROWN_FIRM_PDDA_LIST_VALID_JSON)) {
            String text = new String(jsonInput.readAllBytes(), StandardCharsets.UTF_8);

            JsonNode node = getJsonNode(text);
            ((ObjectNode) node.get(FIRM_LIST_SCHEMA).get(RESERVE_LIST).get(0).get(PROSECUTION)
                .get("ProsecutingOrganisation")).remove(ORGANISATION_NAME);

            String listJson = node.toString();
            assertThrows(PayloadValidationException.class, () ->
                             validationService.validateBody(listJson, headerGroup, true),
                         CROWN_FIRM_PDDA_LIST_INVALID_MESSAGE);
        }
    }

    @Test
    void testValidateWithErrorsWhenReserveListDefendantsDetailsMissingInCrownFirmPddaList() throws IOException {
        try (InputStream jsonInput = this.getClass().getClassLoader()
            .getResourceAsStream(CROWN_FIRM_PDDA_LIST_VALID_JSON)) {
            String text = new String(jsonInput.readAllBytes(), StandardCharsets.UTF_8);

            JsonNode node = getJsonNode(text);
            ((ObjectNode) node.get(FIRM_LIST_SCHEMA).get(RESERVE_LIST).get(0).get(DEFENDANTS).get(0))
                .remove(PERSONAL_DETAILS);

            String listJson = node.toString();
            assertThrows(PayloadValidationException.class, () ->
                             validationService.validateBody(listJson, headerGroup, true),
                         CROWN_FIRM_PDDA_LIST_INVALID_MESSAGE);
        }
    }

    @Test
    void testValidateWithErrorsWhenReserveListDefendantsNameMissingInCrownFirmPddaList() throws IOException {
        try (InputStream jsonInput = this.getClass().getClassLoader()
            .getResourceAsStream(CROWN_FIRM_PDDA_LIST_VALID_JSON)) {
            String text = new String(jsonInput.readAllBytes(), StandardCharsets.UTF_8);

            JsonNode node = getJsonNode(text);
            ((ObjectNode) node.get(FIRM_LIST_SCHEMA).get(RESERVE_LIST).get(0).get(DEFENDANTS).get(0)
                .get(PERSONAL_DETAILS)).remove(NAME);

            String listJson = node.toString();
            assertThrows(PayloadValidationException.class, () ->
                             validationService.validateBody(listJson, headerGroup, true),
                         CROWN_FIRM_PDDA_LIST_INVALID_MESSAGE);
        }
    }

    @Test
    void testValidateWithErrorsWhenReserveListDefendantsIsMaskedMissingInCrownFirmPddaList() throws IOException {
        try (InputStream jsonInput = this.getClass().getClassLoader()
            .getResourceAsStream(CROWN_FIRM_PDDA_LIST_VALID_JSON)) {
            String text = new String(jsonInput.readAllBytes(), StandardCharsets.UTF_8);

            JsonNode node = getJsonNode(text);
            ((ObjectNode) node.get(FIRM_LIST_SCHEMA).get(RESERVE_LIST).get(0).get(DEFENDANTS).get(0)
                .get(PERSONAL_DETAILS)).remove(IS_MASKED);

            String listJson = node.toString();
            assertThrows(PayloadValidationException.class, () ->
                             validationService.validateBody(listJson, headerGroup, true),
                         CROWN_FIRM_PDDA_LIST_INVALID_MESSAGE);
        }
    }

}
