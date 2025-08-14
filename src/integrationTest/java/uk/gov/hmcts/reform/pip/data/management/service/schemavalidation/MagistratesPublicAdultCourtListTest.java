package uk.gov.hmcts.reform.pip.data.management.service.schemavalidation;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
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
class MagistratesPublicAdultCourtListTest extends IntegrationBasicTestBase {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final String MAGISTRATES_PUBLIC_ADULT_COURT_LIST_VALID_JSON =
        "data/magistrates-public-adult-court-list/magistratesPublicAdultCourtList.json";
    private static final String INVALID_JSON_MESSAGE = "Invalid JSON marked as valid";

    private static final String DOCUMENT = "document";
    private static final String DATA = "data";
    private static final String JOB = "job";
    private static final String PRINT_DATE = "printdate";
    private static final String SESSIONS = "sessions";
    private static final String LJA = "lja";
    private static final String COURT = "court";
    private static final String ROOM = "room";
    private static final String SSTART = "sstart";
    private static final String BLOCKS = "blocks";
    private static final String BSTART = "bstart";
    private static final String CASES = "cases";
    private static final String CASE_NUM = "caseno";
    private static final String DEF_NAME = "def_name";

    private static final LocalDateTime DISPLAY_FROM = LocalDateTime.now();
    private static final LocalDateTime DISPLAY_TO = LocalDateTime.now();
    private static final LocalDateTime CONTENT_DATE = LocalDateTime.now();
    private static final String PROVENANCE = "CP_CATH";
    private static final String COURT_ID = "123";

    @Autowired
    ValidationService validationService;

    private final HeaderGroup headerGroup = new HeaderGroup(
        PROVENANCE, "",
        ArtefactType.LIST,
        Sensitivity.PUBLIC,
        Language.ENGLISH,
        DISPLAY_FROM,
        DISPLAY_TO,
        ListType.MAGISTRATES_PUBLIC_ADULT_COURT_LIST_DAILY,
        COURT_ID,
        CONTENT_DATE
    );

    @Test
    void testValidateWithErrorWhenDocumentMissing() throws IOException {
        try (InputStream jsonInput = this.getClass().getClassLoader()
            .getResourceAsStream(MAGISTRATES_PUBLIC_ADULT_COURT_LIST_VALID_JSON)) {
            String text = new String(jsonInput.readAllBytes(), StandardCharsets.UTF_8);

            JsonNode node = OBJECT_MAPPER.readValue(text, JsonNode.class);
            ((ObjectNode) node).remove(DOCUMENT);

            assertThrows(
                PayloadValidationException.class,
                () -> validationService.validateBody(node.toString(), headerGroup, true),
                INVALID_JSON_MESSAGE);
        }
    }

    @Test
    void testValidateWithErrorWhenJobMissing() throws IOException {
        try (InputStream jsonInput = this.getClass().getClassLoader()
            .getResourceAsStream(MAGISTRATES_PUBLIC_ADULT_COURT_LIST_VALID_JSON)) {
            String text = new String(jsonInput.readAllBytes(), StandardCharsets.UTF_8);

            JsonNode node = OBJECT_MAPPER.readValue(text, JsonNode.class);
            ((ObjectNode) node.get(DOCUMENT).get(DATA))
                .remove(JOB);

            assertThrows(
                PayloadValidationException.class,
                () -> validationService.validateBody(node.toString(), headerGroup, true),
                INVALID_JSON_MESSAGE);
        }
    }

    @Test
    void testValidateWithErrorWhenPrintDateMissing() throws IOException {
        try (InputStream jsonInput = this.getClass().getClassLoader()
            .getResourceAsStream(MAGISTRATES_PUBLIC_ADULT_COURT_LIST_VALID_JSON)) {
            String text = new String(jsonInput.readAllBytes(), StandardCharsets.UTF_8);

            JsonNode node = OBJECT_MAPPER.readValue(text, JsonNode.class);
            ((ObjectNode) node.get(DOCUMENT).get(DATA).get(JOB))
                .remove(PRINT_DATE);

            assertThrows(
                PayloadValidationException.class,
                () -> validationService.validateBody(node.toString(), headerGroup, true),
                INVALID_JSON_MESSAGE);
        }
    }

    @Test
    void testValidateWithErrorWhenSessionsMissing() throws IOException {
        try (InputStream jsonInput = this.getClass().getClassLoader()
            .getResourceAsStream(MAGISTRATES_PUBLIC_ADULT_COURT_LIST_VALID_JSON)) {
            String text = new String(jsonInput.readAllBytes(), StandardCharsets.UTF_8);

            JsonNode node = OBJECT_MAPPER.readValue(text, JsonNode.class);
            ((ObjectNode) node.get(DOCUMENT).get(DATA).get(JOB))
                .remove(SESSIONS);

            assertThrows(
                PayloadValidationException.class,
                () -> validationService.validateBody(node.toString(), headerGroup, true),
                INVALID_JSON_MESSAGE);
        }
    }

    @Test
    void testValidateWithErrorWhenLjaMissing() throws IOException {
        try (InputStream jsonInput = this.getClass().getClassLoader()
            .getResourceAsStream(MAGISTRATES_PUBLIC_ADULT_COURT_LIST_VALID_JSON)) {
            String text = new String(jsonInput.readAllBytes(), StandardCharsets.UTF_8);

            JsonNode node = OBJECT_MAPPER.readValue(text, JsonNode.class);
            ((ObjectNode) node.get(DOCUMENT).get(DATA).get(JOB)
                .get(SESSIONS).get(0))
                .remove(LJA);

            assertThrows(
                PayloadValidationException.class,
                () -> validationService.validateBody(node.toString(), headerGroup, true),
                INVALID_JSON_MESSAGE);
        }
    }

    @Test
    void testValidateWithErrorWhenCourtMissing() throws IOException {
        try (InputStream jsonInput = this.getClass().getClassLoader()
            .getResourceAsStream(MAGISTRATES_PUBLIC_ADULT_COURT_LIST_VALID_JSON)) {
            String text = new String(jsonInput.readAllBytes(), StandardCharsets.UTF_8);

            JsonNode node = OBJECT_MAPPER.readValue(text, JsonNode.class);
            ((ObjectNode) node.get(DOCUMENT).get(DATA).get(JOB)
                .get(SESSIONS).get(0))
                .remove(COURT);

            assertThrows(
                PayloadValidationException.class,
                () -> validationService.validateBody(node.toString(), headerGroup, true),
                INVALID_JSON_MESSAGE);
        }
    }

    @Test
    void testValidateWithErrorWhenRoomMissing() throws IOException {
        try (InputStream jsonInput = this.getClass().getClassLoader()
            .getResourceAsStream(MAGISTRATES_PUBLIC_ADULT_COURT_LIST_VALID_JSON)) {
            String text = new String(jsonInput.readAllBytes(), StandardCharsets.UTF_8);

            JsonNode node = OBJECT_MAPPER.readValue(text, JsonNode.class);
            ((ObjectNode) node.get(DOCUMENT).get(DATA).get(JOB)
                .get(SESSIONS).get(0))
                .remove(ROOM);

            assertThrows(
                PayloadValidationException.class,
                () -> validationService.validateBody(node.toString(), headerGroup, true),
                INVALID_JSON_MESSAGE);
        }
    }

    @Test
    void testValidateWithErrorWhenSstartMissing() throws IOException {
        try (InputStream jsonInput = this.getClass().getClassLoader()
            .getResourceAsStream(MAGISTRATES_PUBLIC_ADULT_COURT_LIST_VALID_JSON)) {
            String text = new String(jsonInput.readAllBytes(), StandardCharsets.UTF_8);

            JsonNode node = OBJECT_MAPPER.readValue(text, JsonNode.class);
            ((ObjectNode) node.get(DOCUMENT).get(DATA).get(JOB)
                .get(SESSIONS).get(0))
                .remove(SSTART);

            assertThrows(
                PayloadValidationException.class,
                () -> validationService.validateBody(node.toString(), headerGroup, true),
                INVALID_JSON_MESSAGE);
        }
    }

    @Test
    void testValidateWithErrorWhenBlocksMissing() throws IOException {
        try (InputStream jsonInput = this.getClass().getClassLoader()
            .getResourceAsStream(MAGISTRATES_PUBLIC_ADULT_COURT_LIST_VALID_JSON)) {
            String text = new String(jsonInput.readAllBytes(), StandardCharsets.UTF_8);

            JsonNode node = OBJECT_MAPPER.readValue(text, JsonNode.class);
            ((ObjectNode) node.get(DOCUMENT).get(DATA).get(JOB)
                .get(SESSIONS).get(0))
                .remove(BLOCKS);

            assertThrows(
                PayloadValidationException.class,
                () -> validationService.validateBody(node.toString(), headerGroup, true),
                INVALID_JSON_MESSAGE);
        }
    }

    @Test
    void testValidateWithErrorWhenBstartMissing() throws IOException {
        try (InputStream jsonInput = this.getClass().getClassLoader()
            .getResourceAsStream(MAGISTRATES_PUBLIC_ADULT_COURT_LIST_VALID_JSON)) {
            String text = new String(jsonInput.readAllBytes(), StandardCharsets.UTF_8);

            JsonNode node = OBJECT_MAPPER.readValue(text, JsonNode.class);
            ((ObjectNode) node.get(DOCUMENT).get(DATA).get(JOB)
                .get(SESSIONS).get(0)
                .get(BLOCKS).get(0))
                .remove(BSTART);

            assertThrows(
                PayloadValidationException.class,
                () -> validationService.validateBody(node.toString(), headerGroup, true),
                INVALID_JSON_MESSAGE);
        }
    }

    @Test
    void testValidateWithErrorWhenCasesMissing() throws IOException {
        try (InputStream jsonInput = this.getClass().getClassLoader()
            .getResourceAsStream(MAGISTRATES_PUBLIC_ADULT_COURT_LIST_VALID_JSON)) {
            String text = new String(jsonInput.readAllBytes(), StandardCharsets.UTF_8);

            JsonNode node = OBJECT_MAPPER.readValue(text, JsonNode.class);
            ((ObjectNode) node.get(DOCUMENT).get(DATA).get(JOB)
                .get(SESSIONS).get(0)
                .get(BLOCKS).get(0))
                .remove(CASES);

            assertThrows(
                PayloadValidationException.class,
                () -> validationService.validateBody(node.toString(), headerGroup, true),
                INVALID_JSON_MESSAGE);
        }
    }

    @Test
    void testValidateWithErrorWhenCaseNumMissing() throws IOException {
        try (InputStream jsonInput = this.getClass().getClassLoader()
            .getResourceAsStream(MAGISTRATES_PUBLIC_ADULT_COURT_LIST_VALID_JSON)) {
            String text = new String(jsonInput.readAllBytes(), StandardCharsets.UTF_8);

            JsonNode node = OBJECT_MAPPER.readValue(text, JsonNode.class);
            ((ObjectNode) node.get(DOCUMENT).get(DATA).get(JOB)
                .get(SESSIONS).get(0)
                .get(BLOCKS).get(0)
                .get(CASES).get(0))
                .remove(CASE_NUM);

            assertThrows(
                PayloadValidationException.class,
                () -> validationService.validateBody(node.toString(), headerGroup, true),
                INVALID_JSON_MESSAGE);
        }
    }

    @Test
    void testValidateWithErrorWhenDefNameMissing() throws IOException {
        try (InputStream jsonInput = this.getClass().getClassLoader()
            .getResourceAsStream(MAGISTRATES_PUBLIC_ADULT_COURT_LIST_VALID_JSON)) {
            String text = new String(jsonInput.readAllBytes(), StandardCharsets.UTF_8);

            JsonNode node = OBJECT_MAPPER.readValue(text, JsonNode.class);
            ((ObjectNode) node.get(DOCUMENT).get(DATA).get(JOB)
                .get(SESSIONS).get(0)
                .get(BLOCKS).get(0)
                .get(CASES).get(0))
                .remove(DEF_NAME);

            assertThrows(
                PayloadValidationException.class,
                () -> validationService.validateBody(node.toString(), headerGroup, true),
                INVALID_JSON_MESSAGE);
        }
    }

    @Test
    void testValidateWithErrorWhenCaseNumberHaveLengthLessThan10() throws IOException {
        try (InputStream jsonInput = this.getClass().getClassLoader()
            .getResourceAsStream(MAGISTRATES_PUBLIC_ADULT_COURT_LIST_VALID_JSON)) {
            String text = new String(jsonInput.readAllBytes(), StandardCharsets.UTF_8);

            ObjectMapper mapper = new ObjectMapper();
            JsonNode node = mapper.readValue(text, JsonNode.class);

            String listJson = node.toString()
                .replaceAll("\"caseno\":\"[^\"]+\"", "\"caseno\":\"12345\"");
            assertThrows(
                PayloadValidationException.class,
                () -> validationService.validateBody(listJson, headerGroup, true),
                INVALID_JSON_MESSAGE);
        }
    }

    @Test
    void testValidateWithErrorWhenCaseNumberHaveLengthGreaterThan10() throws IOException {
        try (InputStream jsonInput = this.getClass().getClassLoader()
            .getResourceAsStream(MAGISTRATES_PUBLIC_ADULT_COURT_LIST_VALID_JSON)) {
            String text = new String(jsonInput.readAllBytes(), StandardCharsets.UTF_8);

            ObjectMapper mapper = new ObjectMapper();
            JsonNode node = mapper.readValue(text, JsonNode.class);

            String listJson = node.toString()
                .replaceAll("\"caseno\":\"[^\"]+\"", "\"caseno\":\"10000000000\"");
            assertThrows(
                PayloadValidationException.class,
                () -> validationService.validateBody(listJson, headerGroup, true),
                INVALID_JSON_MESSAGE);
        }
    }

    @Test
    void testValidateWithErrorWhenInvalidPrintDateFormat() throws IOException {
        try (InputStream jsonInput = this.getClass().getClassLoader()
            .getResourceAsStream(MAGISTRATES_PUBLIC_ADULT_COURT_LIST_VALID_JSON)) {
            String text = new String(jsonInput.readAllBytes(), StandardCharsets.UTF_8);

            ObjectMapper mapper = new ObjectMapper();
            JsonNode node = mapper.readValue(text, JsonNode.class);

            String listJson = node.toString()
                .replaceAll("\"printdate\":\"[^\"]+\"", "\"printdate\":\"2025/07/31\"");
            assertThrows(
                PayloadValidationException.class,
                () -> validationService.validateBody(listJson, headerGroup, true),
                INVALID_JSON_MESSAGE);
        }
    }

    @Test
    void testValidateWithErrorWhenInvalidSstartTimeFormat() throws IOException {
        try (InputStream jsonInput = this.getClass().getClassLoader()
            .getResourceAsStream(MAGISTRATES_PUBLIC_ADULT_COURT_LIST_VALID_JSON)) {
            String text = new String(jsonInput.readAllBytes(), StandardCharsets.UTF_8);

            ObjectMapper mapper = new ObjectMapper();
            JsonNode node = mapper.readValue(text, JsonNode.class);

            String listJson = node.toString()
                .replaceAll("\"sstart\":\"[^\"]+\"", "\"sstart\":\"09.00\"");
            assertThrows(
                PayloadValidationException.class,
                () -> validationService.validateBody(listJson, headerGroup, true),
                INVALID_JSON_MESSAGE);
        }
    }

    @Test
    void testValidateWithErrorWhenInvalidBstartTimeFormat() throws IOException {
        try (InputStream jsonInput = this.getClass().getClassLoader()
            .getResourceAsStream(MAGISTRATES_PUBLIC_ADULT_COURT_LIST_VALID_JSON)) {
            String text = new String(jsonInput.readAllBytes(), StandardCharsets.UTF_8);

            ObjectMapper mapper = new ObjectMapper();
            JsonNode node = mapper.readValue(text, JsonNode.class);

            String listJson = node.toString()
                .replaceAll("\"bstart\":\"[^\"]+\"", "\"bstart\":\"9:00\"");
            assertThrows(
                PayloadValidationException.class,
                () -> validationService.validateBody(listJson, headerGroup, true),
                INVALID_JSON_MESSAGE);
        }
    }
}
