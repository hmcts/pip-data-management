package uk.gov.hmcts.reform.pip.data.management.service.schemavalidation;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
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
class MagistratesAdultCourtListTest extends IntegrationBasicTestBase {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final String MAGISTRATES_ADULT_COURT_LIST_DAILY = "MAGISTRATES_ADULT_COURT_LIST_DAILY";
    private static final String MAGISTRATES_ADULT_COURT_LIST_FUTURE = "MAGISTRATES_ADULT_COURT_LIST_FUTURE";
    private static final String MAGISTRATES_ADULT_COURT_LIST_VALID_JSON =
        "data/magistrates-adult-court-list/magistratesAdultCourtList.json";
    private static final String INVALID_JSON_MESSAGE = "Invalid JSON marked as valid";

    private static final String DOCUMENT = "document";
    private static final String DATA = "data";
    private static final String JOB = "job";
    private static final String PRINT_DATE = "printdate";
    private static final String SESSIONS = "sessions";
    private static final String SESSION = "session";
    private static final String LJA = "lja";
    private static final String COURT = "court";
    private static final String ROOM = "room";
    private static final String SSTART = "sstart";
    private static final String BLOCKS = "blocks";
    private static final String BLOCK = "block";
    private static final String BSTART = "bstart";
    private static final String CASES = "cases";
    private static final String CASE = "case";
    private static final String CASE_NUM = "caseno";
    private static final String DEF_NAME = "def_name";
    private static final String DEF_ADDR = "def_addr";
    private static final String LINE1 = "line1";
    private static final String INF = "inf";
    private static final String OFFENCES = "offences";
    private static final String OFFENCE = "offence";
    private static final String CODE = "code";
    private static final String TITLE = "title";
    private static final String SUM = "sum";

    private static final LocalDateTime DISPLAY_FROM = LocalDateTime.now();
    private static final LocalDateTime DISPLAY_TO = LocalDateTime.now();
    private static final String PROVENANCE = "CP_CATH";
    private static final String COURT_ID = "123";
    private static final LocalDateTime CONTENT_DATE = LocalDateTime.now();

    @Autowired
    ValidationService validationService;

    @ParameterizedTest
    @EnumSource(value = ListType.class,
        names = {MAGISTRATES_ADULT_COURT_LIST_DAILY, MAGISTRATES_ADULT_COURT_LIST_FUTURE})
    void testValidateWithErrorWhenDocumentMissing(ListType listType) throws IOException {
        HeaderGroup headerGroup = new HeaderGroup(PROVENANCE, "", ArtefactType.LIST, Sensitivity.PUBLIC,
                                                  Language.ENGLISH, DISPLAY_FROM, DISPLAY_TO, listType, COURT_ID,
                                                  CONTENT_DATE);
        try (InputStream jsonInput = this.getClass().getClassLoader()
            .getResourceAsStream(MAGISTRATES_ADULT_COURT_LIST_VALID_JSON)) {
            String text = new String(jsonInput.readAllBytes(), StandardCharsets.UTF_8);

            JsonNode node = OBJECT_MAPPER.readValue(text, JsonNode.class);
            ((ObjectNode) node).remove(DOCUMENT);

            assertThrows(
                PayloadValidationException.class,
                () -> validationService.validateBody(node.toString(), headerGroup, true),
                INVALID_JSON_MESSAGE);
        }
    }

    @ParameterizedTest
    @EnumSource(value = ListType.class,
        names = {MAGISTRATES_ADULT_COURT_LIST_DAILY, MAGISTRATES_ADULT_COURT_LIST_FUTURE})
    void testValidateWithErrorWhenJobMissing(ListType listType) throws IOException {
        HeaderGroup headerGroup = new HeaderGroup(PROVENANCE, "", ArtefactType.LIST, Sensitivity.PUBLIC,
                                                  Language.ENGLISH, DISPLAY_FROM, DISPLAY_TO, listType, COURT_ID,
                                                  CONTENT_DATE);
        try (InputStream jsonInput = this.getClass().getClassLoader()
            .getResourceAsStream(MAGISTRATES_ADULT_COURT_LIST_VALID_JSON)) {
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

    @ParameterizedTest
    @EnumSource(value = ListType.class,
        names = {MAGISTRATES_ADULT_COURT_LIST_DAILY, MAGISTRATES_ADULT_COURT_LIST_FUTURE})
    void testValidateWithErrorWhenPrintDateMissing(ListType listType) throws IOException {
        HeaderGroup headerGroup = new HeaderGroup(PROVENANCE, "", ArtefactType.LIST, Sensitivity.PUBLIC,
                                                  Language.ENGLISH, DISPLAY_FROM, DISPLAY_TO, listType, COURT_ID,
                                                  CONTENT_DATE);
        try (InputStream jsonInput = this.getClass().getClassLoader()
            .getResourceAsStream(MAGISTRATES_ADULT_COURT_LIST_VALID_JSON)) {
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

    @ParameterizedTest
    @EnumSource(value = ListType.class,
        names = {MAGISTRATES_ADULT_COURT_LIST_DAILY, MAGISTRATES_ADULT_COURT_LIST_FUTURE})
    void testValidateWithErrorWhenSessionsMissing(ListType listType) throws IOException {
        HeaderGroup headerGroup = new HeaderGroup(PROVENANCE, "", ArtefactType.LIST, Sensitivity.PUBLIC,
                                                  Language.ENGLISH, DISPLAY_FROM, DISPLAY_TO, listType, COURT_ID,
                                                  CONTENT_DATE);
        try (InputStream jsonInput = this.getClass().getClassLoader()
            .getResourceAsStream(MAGISTRATES_ADULT_COURT_LIST_VALID_JSON)) {
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

    @ParameterizedTest
    @EnumSource(value = ListType.class,
        names = {MAGISTRATES_ADULT_COURT_LIST_DAILY, MAGISTRATES_ADULT_COURT_LIST_FUTURE})
    void testValidateWithErrorWhenSessionMissing(ListType listType) throws IOException {
        HeaderGroup headerGroup = new HeaderGroup(PROVENANCE, "", ArtefactType.LIST, Sensitivity.PUBLIC,
                                                  Language.ENGLISH, DISPLAY_FROM, DISPLAY_TO, listType, COURT_ID,
                                                  CONTENT_DATE);
        try (InputStream jsonInput = this.getClass().getClassLoader()
            .getResourceAsStream(MAGISTRATES_ADULT_COURT_LIST_VALID_JSON)) {
            String text = new String(jsonInput.readAllBytes(), StandardCharsets.UTF_8);

            JsonNode node = OBJECT_MAPPER.readValue(text, JsonNode.class);
            ((ObjectNode) node.get(DOCUMENT).get(DATA).get(JOB)
                .get(SESSIONS))
                .remove(SESSION);

            assertThrows(
                PayloadValidationException.class,
                () -> validationService.validateBody(node.toString(), headerGroup, true),
                INVALID_JSON_MESSAGE);
        }
    }

    @ParameterizedTest
    @EnumSource(value = ListType.class,
        names = {MAGISTRATES_ADULT_COURT_LIST_DAILY, MAGISTRATES_ADULT_COURT_LIST_FUTURE})
    void testValidateWithErrorWhenLjaMissing(ListType listType) throws IOException {
        HeaderGroup headerGroup = new HeaderGroup(PROVENANCE, "", ArtefactType.LIST, Sensitivity.PUBLIC,
                                                  Language.ENGLISH, DISPLAY_FROM, DISPLAY_TO, listType, COURT_ID,
                                                  CONTENT_DATE);
        try (InputStream jsonInput = this.getClass().getClassLoader()
            .getResourceAsStream(MAGISTRATES_ADULT_COURT_LIST_VALID_JSON)) {
            String text = new String(jsonInput.readAllBytes(), StandardCharsets.UTF_8);

            JsonNode node = OBJECT_MAPPER.readValue(text, JsonNode.class);
            ((ObjectNode) node.get(DOCUMENT).get(DATA).get(JOB)
                .get(SESSIONS).get(SESSION).get(0))
                .remove(LJA);

            assertThrows(
                PayloadValidationException.class,
                () -> validationService.validateBody(node.toString(), headerGroup, true),
                INVALID_JSON_MESSAGE);
        }
    }

    @ParameterizedTest
    @EnumSource(value = ListType.class,
        names = {MAGISTRATES_ADULT_COURT_LIST_DAILY, MAGISTRATES_ADULT_COURT_LIST_FUTURE})
    void testValidateWithErrorWhenCourtMissing(ListType listType) throws IOException {
        HeaderGroup headerGroup = new HeaderGroup(PROVENANCE, "", ArtefactType.LIST, Sensitivity.PUBLIC,
                                                  Language.ENGLISH, DISPLAY_FROM, DISPLAY_TO, listType, COURT_ID,
                                                  CONTENT_DATE);
        try (InputStream jsonInput = this.getClass().getClassLoader()
            .getResourceAsStream(MAGISTRATES_ADULT_COURT_LIST_VALID_JSON)) {
            String text = new String(jsonInput.readAllBytes(), StandardCharsets.UTF_8);

            JsonNode node = OBJECT_MAPPER.readValue(text, JsonNode.class);
            ((ObjectNode) node.get(DOCUMENT).get(DATA).get(JOB)
                .get(SESSIONS).get(SESSION).get(0))
                .remove(COURT);

            assertThrows(
                PayloadValidationException.class,
                () -> validationService.validateBody(node.toString(), headerGroup, true),
                INVALID_JSON_MESSAGE);
        }
    }

    @ParameterizedTest
    @EnumSource(value = ListType.class,
        names = {MAGISTRATES_ADULT_COURT_LIST_DAILY, MAGISTRATES_ADULT_COURT_LIST_FUTURE})
    void testValidateWithErrorWhenRoomMissing(ListType listType) throws IOException {
        HeaderGroup headerGroup = new HeaderGroup(PROVENANCE, "", ArtefactType.LIST, Sensitivity.PUBLIC,
                                                  Language.ENGLISH, DISPLAY_FROM, DISPLAY_TO, listType, COURT_ID,
                                                  CONTENT_DATE);
        try (InputStream jsonInput = this.getClass().getClassLoader()
            .getResourceAsStream(MAGISTRATES_ADULT_COURT_LIST_VALID_JSON)) {
            String text = new String(jsonInput.readAllBytes(), StandardCharsets.UTF_8);

            JsonNode node = OBJECT_MAPPER.readValue(text, JsonNode.class);
            ((ObjectNode) node.get(DOCUMENT).get(DATA).get(JOB)
                .get(SESSIONS).get(SESSION).get(0))
                .remove(ROOM);

            assertThrows(
                PayloadValidationException.class,
                () -> validationService.validateBody(node.toString(), headerGroup, true),
                INVALID_JSON_MESSAGE);
        }
    }

    @ParameterizedTest
    @EnumSource(value = ListType.class,
        names = {MAGISTRATES_ADULT_COURT_LIST_DAILY, MAGISTRATES_ADULT_COURT_LIST_FUTURE})
    void testValidateWithErrorWhenSstartMissing(ListType listType) throws IOException {
        HeaderGroup headerGroup = new HeaderGroup(PROVENANCE, "", ArtefactType.LIST, Sensitivity.PUBLIC,
                                                  Language.ENGLISH, DISPLAY_FROM, DISPLAY_TO, listType, COURT_ID,
                                                  CONTENT_DATE);
        try (InputStream jsonInput = this.getClass().getClassLoader()
            .getResourceAsStream(MAGISTRATES_ADULT_COURT_LIST_VALID_JSON)) {
            String text = new String(jsonInput.readAllBytes(), StandardCharsets.UTF_8);

            JsonNode node = OBJECT_MAPPER.readValue(text, JsonNode.class);
            ((ObjectNode) node.get(DOCUMENT).get(DATA).get(JOB)
                .get(SESSIONS).get(SESSION).get(0))
                .remove(SSTART);

            assertThrows(
                PayloadValidationException.class,
                () -> validationService.validateBody(node.toString(), headerGroup, true),
                INVALID_JSON_MESSAGE);
        }
    }

    @ParameterizedTest
    @EnumSource(value = ListType.class,
        names = {MAGISTRATES_ADULT_COURT_LIST_DAILY, MAGISTRATES_ADULT_COURT_LIST_FUTURE})
    void testValidateWithErrorWhenBlocksMissing(ListType listType) throws IOException {
        HeaderGroup headerGroup = new HeaderGroup(PROVENANCE, "", ArtefactType.LIST, Sensitivity.PUBLIC,
                                                  Language.ENGLISH, DISPLAY_FROM, DISPLAY_TO, listType, COURT_ID,
                                                  CONTENT_DATE);
        try (InputStream jsonInput = this.getClass().getClassLoader()
            .getResourceAsStream(MAGISTRATES_ADULT_COURT_LIST_VALID_JSON)) {
            String text = new String(jsonInput.readAllBytes(), StandardCharsets.UTF_8);

            JsonNode node = OBJECT_MAPPER.readValue(text, JsonNode.class);
            ((ObjectNode) node.get(DOCUMENT).get(DATA).get(JOB)
                .get(SESSIONS).get(SESSION).get(0))
                .remove(BLOCKS);

            assertThrows(
                PayloadValidationException.class,
                () -> validationService.validateBody(node.toString(), headerGroup, true),
                INVALID_JSON_MESSAGE);
        }
    }

    @ParameterizedTest
    @EnumSource(value = ListType.class,
        names = {MAGISTRATES_ADULT_COURT_LIST_DAILY, MAGISTRATES_ADULT_COURT_LIST_FUTURE})
    void testValidateWithErrorWhenBlockMissing(ListType listType) throws IOException {
        HeaderGroup headerGroup = new HeaderGroup(PROVENANCE, "", ArtefactType.LIST, Sensitivity.PUBLIC,
                                                  Language.ENGLISH, DISPLAY_FROM, DISPLAY_TO, listType, COURT_ID,
                                                  CONTENT_DATE);
        try (InputStream jsonInput = this.getClass().getClassLoader()
            .getResourceAsStream(MAGISTRATES_ADULT_COURT_LIST_VALID_JSON)) {
            String text = new String(jsonInput.readAllBytes(), StandardCharsets.UTF_8);

            JsonNode node = OBJECT_MAPPER.readValue(text, JsonNode.class);
            ((ObjectNode) node.get(DOCUMENT).get(DATA).get(JOB)
                .get(SESSIONS).get(SESSION).get(0)
                .get(BLOCKS))
                .remove(BLOCK);

            assertThrows(
                PayloadValidationException.class,
                () -> validationService.validateBody(node.toString(), headerGroup, true),
                INVALID_JSON_MESSAGE);
        }
    }

    @ParameterizedTest
    @EnumSource(value = ListType.class,
        names = {MAGISTRATES_ADULT_COURT_LIST_DAILY, MAGISTRATES_ADULT_COURT_LIST_FUTURE})
    void testValidateWithErrorWhenBstartMissing(ListType listType) throws IOException {
        HeaderGroup headerGroup = new HeaderGroup(PROVENANCE, "", ArtefactType.LIST, Sensitivity.PUBLIC,
                                                  Language.ENGLISH, DISPLAY_FROM, DISPLAY_TO, listType, COURT_ID,
                                                  CONTENT_DATE);
        try (InputStream jsonInput = this.getClass().getClassLoader()
            .getResourceAsStream(MAGISTRATES_ADULT_COURT_LIST_VALID_JSON)) {
            String text = new String(jsonInput.readAllBytes(), StandardCharsets.UTF_8);

            JsonNode node = OBJECT_MAPPER.readValue(text, JsonNode.class);
            ((ObjectNode) node.get(DOCUMENT).get(DATA).get(JOB)
                .get(SESSIONS).get(SESSION).get(0)
                .get(BLOCKS).get(BLOCK).get(0))
                .remove(BSTART);

            assertThrows(
                PayloadValidationException.class,
                () -> validationService.validateBody(node.toString(), headerGroup, true),
                INVALID_JSON_MESSAGE);
        }
    }

    @ParameterizedTest
    @EnumSource(value = ListType.class,
        names = {MAGISTRATES_ADULT_COURT_LIST_DAILY, MAGISTRATES_ADULT_COURT_LIST_FUTURE})
    void testValidateWithErrorWhenCasesMissing(ListType listType) throws IOException {
        HeaderGroup headerGroup = new HeaderGroup(PROVENANCE, "", ArtefactType.LIST, Sensitivity.PUBLIC,
                                                  Language.ENGLISH, DISPLAY_FROM, DISPLAY_TO, listType, COURT_ID,
                                                  CONTENT_DATE);
        try (InputStream jsonInput = this.getClass().getClassLoader()
            .getResourceAsStream(MAGISTRATES_ADULT_COURT_LIST_VALID_JSON)) {
            String text = new String(jsonInput.readAllBytes(), StandardCharsets.UTF_8);

            JsonNode node = OBJECT_MAPPER.readValue(text, JsonNode.class);
            ((ObjectNode) node.get(DOCUMENT).get(DATA).get(JOB)
                .get(SESSIONS).get(SESSION).get(0)
                .get(BLOCKS).get(BLOCK).get(0))
                .remove(CASES);

            assertThrows(
                PayloadValidationException.class,
                () -> validationService.validateBody(node.toString(), headerGroup, true),
                INVALID_JSON_MESSAGE);
        }
    }

    @ParameterizedTest
    @EnumSource(value = ListType.class,
        names = {MAGISTRATES_ADULT_COURT_LIST_DAILY, MAGISTRATES_ADULT_COURT_LIST_FUTURE})
    void testValidateWithErrorWhenCaseMissing(ListType listType) throws IOException {
        HeaderGroup headerGroup = new HeaderGroup(PROVENANCE, "", ArtefactType.LIST, Sensitivity.PUBLIC,
                                                  Language.ENGLISH, DISPLAY_FROM, DISPLAY_TO, listType, COURT_ID,
                                                  CONTENT_DATE);
        try (InputStream jsonInput = this.getClass().getClassLoader()
            .getResourceAsStream(MAGISTRATES_ADULT_COURT_LIST_VALID_JSON)) {
            String text = new String(jsonInput.readAllBytes(), StandardCharsets.UTF_8);

            JsonNode node = OBJECT_MAPPER.readValue(text, JsonNode.class);
            ((ObjectNode) node.get(DOCUMENT).get(DATA).get(JOB)
                .get(SESSIONS).get(SESSION).get(0)
                .get(BLOCKS).get(BLOCK).get(0)
                .get(CASES))
                .remove(CASE);

            assertThrows(
                PayloadValidationException.class,
                () -> validationService.validateBody(node.toString(), headerGroup, true),
                INVALID_JSON_MESSAGE);
        }
    }

    @ParameterizedTest
    @EnumSource(value = ListType.class,
        names = {MAGISTRATES_ADULT_COURT_LIST_DAILY, MAGISTRATES_ADULT_COURT_LIST_FUTURE})
    void testValidateWithErrorWhenCaseNumMissing(ListType listType) throws IOException {
        HeaderGroup headerGroup = new HeaderGroup(PROVENANCE, "", ArtefactType.LIST, Sensitivity.PUBLIC,
                                                  Language.ENGLISH, DISPLAY_FROM, DISPLAY_TO, listType, COURT_ID,
                                                  CONTENT_DATE);
        try (InputStream jsonInput = this.getClass().getClassLoader()
            .getResourceAsStream(MAGISTRATES_ADULT_COURT_LIST_VALID_JSON)) {
            String text = new String(jsonInput.readAllBytes(), StandardCharsets.UTF_8);

            JsonNode node = OBJECT_MAPPER.readValue(text, JsonNode.class);
            ((ObjectNode) node.get(DOCUMENT).get(DATA).get(JOB).get(SESSIONS)
                .get(SESSION).get(0)
                .get(BLOCKS).get(BLOCK).get(0)
                .get(CASES).get(CASE).get(0))
                .remove(CASE_NUM);

            assertThrows(
                PayloadValidationException.class,
                () -> validationService.validateBody(node.toString(), headerGroup, true),
                INVALID_JSON_MESSAGE);
        }
    }

    @ParameterizedTest
    @EnumSource(value = ListType.class,
        names = {MAGISTRATES_ADULT_COURT_LIST_DAILY, MAGISTRATES_ADULT_COURT_LIST_FUTURE})
    void testValidateWithErrorWhenDefNameMissing(ListType listType) throws IOException {
        HeaderGroup headerGroup = new HeaderGroup(PROVENANCE, "", ArtefactType.LIST, Sensitivity.PUBLIC,
                                                  Language.ENGLISH, DISPLAY_FROM, DISPLAY_TO, listType, COURT_ID,
                                                  CONTENT_DATE);
        try (InputStream jsonInput = this.getClass().getClassLoader()
            .getResourceAsStream(MAGISTRATES_ADULT_COURT_LIST_VALID_JSON)) {
            String text = new String(jsonInput.readAllBytes(), StandardCharsets.UTF_8);

            JsonNode node = OBJECT_MAPPER.readValue(text, JsonNode.class);
            ((ObjectNode) node.get(DOCUMENT).get(DATA).get(JOB)
                .get(SESSIONS).get(SESSION).get(0)
                .get(BLOCKS).get(BLOCK).get(0)
                .get(CASES).get(CASE).get(0))
                .remove(DEF_NAME);

            assertThrows(
                PayloadValidationException.class,
                () -> validationService.validateBody(node.toString(), headerGroup, true),
                INVALID_JSON_MESSAGE);
        }
    }

    @ParameterizedTest
    @EnumSource(value = ListType.class,
        names = {MAGISTRATES_ADULT_COURT_LIST_DAILY, MAGISTRATES_ADULT_COURT_LIST_FUTURE})
    void testValidateWithErrorWhenDefAddrMissing(ListType listType) throws IOException {
        HeaderGroup headerGroup = new HeaderGroup(PROVENANCE, "", ArtefactType.LIST, Sensitivity.PUBLIC,
                                                  Language.ENGLISH, DISPLAY_FROM, DISPLAY_TO, listType, COURT_ID,
                                                  CONTENT_DATE);
        try (InputStream jsonInput = this.getClass().getClassLoader()
            .getResourceAsStream(MAGISTRATES_ADULT_COURT_LIST_VALID_JSON)) {
            String text = new String(jsonInput.readAllBytes(), StandardCharsets.UTF_8);

            JsonNode node = OBJECT_MAPPER.readValue(text, JsonNode.class);
            ((ObjectNode) node.get(DOCUMENT).get(DATA).get(JOB)
                .get(SESSIONS).get(SESSION).get(0)
                .get(BLOCKS).get(BLOCK).get(0)
                .get(CASES).get(CASE).get(0))
                .remove(DEF_ADDR);

            assertThrows(
                PayloadValidationException.class,
                () -> validationService.validateBody(node.toString(), headerGroup, true),
                INVALID_JSON_MESSAGE);
        }
    }

    @ParameterizedTest
    @EnumSource(value = ListType.class,
        names = {MAGISTRATES_ADULT_COURT_LIST_DAILY, MAGISTRATES_ADULT_COURT_LIST_FUTURE})
    void testValidateWithErrorWhenLine1Missing(ListType listType) throws IOException {
        HeaderGroup headerGroup = new HeaderGroup(PROVENANCE, "", ArtefactType.LIST, Sensitivity.PUBLIC,
                                                  Language.ENGLISH, DISPLAY_FROM, DISPLAY_TO, listType, COURT_ID,
                                                  CONTENT_DATE);
        try (InputStream jsonInput = this.getClass().getClassLoader()
            .getResourceAsStream(MAGISTRATES_ADULT_COURT_LIST_VALID_JSON)) {
            String text = new String(jsonInput.readAllBytes(), StandardCharsets.UTF_8);

            JsonNode node = OBJECT_MAPPER.readValue(text, JsonNode.class);
            ((ObjectNode) node.get(DOCUMENT).get(DATA).get(JOB)
                .get(SESSIONS).get(SESSION).get(0)
                .get(BLOCKS).get(BLOCK).get(0)
                .get(CASES).get(CASE).get(0)
                .get(DEF_ADDR))
                .remove(LINE1);

            assertThrows(
                PayloadValidationException.class,
                () -> validationService.validateBody(node.toString(), headerGroup, true),
                INVALID_JSON_MESSAGE);
        }
    }

    @ParameterizedTest
    @EnumSource(value = ListType.class,
        names = {MAGISTRATES_ADULT_COURT_LIST_DAILY, MAGISTRATES_ADULT_COURT_LIST_FUTURE})
    void testValidateWithErrorWhenInfMissing(ListType listType) throws IOException {
        HeaderGroup headerGroup = new HeaderGroup(PROVENANCE, "", ArtefactType.LIST, Sensitivity.PUBLIC,
                                                  Language.ENGLISH, DISPLAY_FROM, DISPLAY_TO, listType, COURT_ID,
                                                  CONTENT_DATE);
        try (InputStream jsonInput = this.getClass().getClassLoader()
            .getResourceAsStream(MAGISTRATES_ADULT_COURT_LIST_VALID_JSON)) {
            String text = new String(jsonInput.readAllBytes(), StandardCharsets.UTF_8);

            JsonNode node = OBJECT_MAPPER.readValue(text, JsonNode.class);
            ((ObjectNode) node.get(DOCUMENT).get(DATA).get(JOB)
                .get(SESSIONS).get(SESSION).get(0)
                .get(BLOCKS).get(BLOCK).get(0)
                .get(CASES).get(CASE).get(0))
                .remove(INF);

            assertThrows(
                PayloadValidationException.class,
                () -> validationService.validateBody(node.toString(), headerGroup, true),
                INVALID_JSON_MESSAGE);
        }
    }

    @ParameterizedTest
    @EnumSource(value = ListType.class,
        names = {MAGISTRATES_ADULT_COURT_LIST_DAILY, MAGISTRATES_ADULT_COURT_LIST_FUTURE})
    void testValidateWithErrorWhenOffencesMissing(ListType listType) throws IOException {
        HeaderGroup headerGroup = new HeaderGroup(PROVENANCE, "", ArtefactType.LIST, Sensitivity.PUBLIC,
                                                  Language.ENGLISH, DISPLAY_FROM, DISPLAY_TO, listType, COURT_ID,
                                                  CONTENT_DATE);
        try (InputStream jsonInput = this.getClass().getClassLoader()
            .getResourceAsStream(MAGISTRATES_ADULT_COURT_LIST_VALID_JSON)) {
            String text = new String(jsonInput.readAllBytes(), StandardCharsets.UTF_8);

            JsonNode node = OBJECT_MAPPER.readValue(text, JsonNode.class);
            ((ObjectNode) node.get(DOCUMENT).get(DATA).get(JOB)
                .get(SESSIONS).get(SESSION).get(0)
                .get(BLOCKS).get(BLOCK).get(0)
                .get(CASES).get(CASE).get(0))
                .remove(OFFENCES);

            assertThrows(
                PayloadValidationException.class,
                () -> validationService.validateBody(node.toString(), headerGroup, true),
                INVALID_JSON_MESSAGE);
        }
    }

    @ParameterizedTest
    @EnumSource(value = ListType.class,
        names = {MAGISTRATES_ADULT_COURT_LIST_DAILY, MAGISTRATES_ADULT_COURT_LIST_FUTURE})
    void testValidateWithErrorWhenCodeMissing(ListType listType) throws IOException {
        HeaderGroup headerGroup = new HeaderGroup(PROVENANCE, "", ArtefactType.LIST, Sensitivity.PUBLIC,
                                                  Language.ENGLISH, DISPLAY_FROM, DISPLAY_TO, listType, COURT_ID,
                                                  CONTENT_DATE);
        try (InputStream jsonInput = this.getClass().getClassLoader()
            .getResourceAsStream(MAGISTRATES_ADULT_COURT_LIST_VALID_JSON)) {
            String text = new String(jsonInput.readAllBytes(), StandardCharsets.UTF_8);

            JsonNode node = OBJECT_MAPPER.readValue(text, JsonNode.class);
            ((ObjectNode) node.get(DOCUMENT).get(DATA).get(JOB)
                .get(SESSIONS).get(SESSION).get(0)
                .get(BLOCKS).get(BLOCK).get(0)
                .get(CASES).get(CASE).get(0)
                .get(OFFENCES).get(OFFENCE).get(0))
                .remove(CODE);

            assertThrows(
                PayloadValidationException.class,
                () -> validationService.validateBody(node.toString(), headerGroup, true),
                INVALID_JSON_MESSAGE);
        }
    }

    @ParameterizedTest
    @EnumSource(value = ListType.class,
        names = {MAGISTRATES_ADULT_COURT_LIST_DAILY, MAGISTRATES_ADULT_COURT_LIST_FUTURE})
    void testValidateWithErrorWhenTitleMissing(ListType listType) throws IOException {
        HeaderGroup headerGroup = new HeaderGroup(PROVENANCE, "", ArtefactType.LIST, Sensitivity.PUBLIC,
                                                  Language.ENGLISH, DISPLAY_FROM, DISPLAY_TO, listType, COURT_ID,
                                                  CONTENT_DATE);
        try (InputStream jsonInput = this.getClass().getClassLoader()
            .getResourceAsStream(MAGISTRATES_ADULT_COURT_LIST_VALID_JSON)) {
            String text = new String(jsonInput.readAllBytes(), StandardCharsets.UTF_8);

            JsonNode node = OBJECT_MAPPER.readValue(text, JsonNode.class);
            ((ObjectNode) node.get(DOCUMENT).get(DATA).get(JOB)
                .get(SESSIONS).get(SESSION).get(0)
                .get(BLOCKS).get(BLOCK).get(0)
                .get(CASES).get(CASE).get(0)
                .get(OFFENCES).get(OFFENCE).get(0))
                .remove(TITLE);

            assertThrows(
                PayloadValidationException.class,
                () -> validationService.validateBody(node.toString(), headerGroup, true),
                INVALID_JSON_MESSAGE);
        }
    }

    @ParameterizedTest
    @EnumSource(value = ListType.class,
        names = {MAGISTRATES_ADULT_COURT_LIST_DAILY, MAGISTRATES_ADULT_COURT_LIST_FUTURE})
    void testValidateWithErrorWhenSumMissing(ListType listType) throws IOException {
        HeaderGroup headerGroup = new HeaderGroup(PROVENANCE, "", ArtefactType.LIST, Sensitivity.PUBLIC,
                                                  Language.ENGLISH, DISPLAY_FROM, DISPLAY_TO, listType, COURT_ID,
                                                  CONTENT_DATE);
        try (InputStream jsonInput = this.getClass().getClassLoader()
            .getResourceAsStream(MAGISTRATES_ADULT_COURT_LIST_VALID_JSON)) {
            String text = new String(jsonInput.readAllBytes(), StandardCharsets.UTF_8);

            JsonNode node = OBJECT_MAPPER.readValue(text, JsonNode.class);
            ((ObjectNode) node.get(DOCUMENT).get(DATA).get(JOB)
                .get(SESSIONS).get(SESSION).get(0)
                .get(BLOCKS).get(BLOCK).get(0)
                .get(CASES).get(CASE).get(0)
                .get(OFFENCES).get(OFFENCE).get(0))
                .remove(SUM);

            assertThrows(
                PayloadValidationException.class,
                () -> validationService.validateBody(node.toString(), headerGroup, true),
                INVALID_JSON_MESSAGE);
        }
    }

    @ParameterizedTest
    @EnumSource(value = ListType.class,
        names = {MAGISTRATES_ADULT_COURT_LIST_DAILY, MAGISTRATES_ADULT_COURT_LIST_FUTURE})
    void testValidateWithErrorWhenCaseNumberHaveLengthLessThan10(ListType listType) throws IOException {
        HeaderGroup headerGroup = new HeaderGroup(PROVENANCE, "", ArtefactType.LIST, Sensitivity.PUBLIC,
                                                  Language.ENGLISH, DISPLAY_FROM, DISPLAY_TO, listType, COURT_ID,
                                                  CONTENT_DATE);
        try (InputStream jsonInput = this.getClass().getClassLoader()
            .getResourceAsStream(MAGISTRATES_ADULT_COURT_LIST_VALID_JSON)) {
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

    @ParameterizedTest
    @EnumSource(value = ListType.class,
        names = {MAGISTRATES_ADULT_COURT_LIST_DAILY, MAGISTRATES_ADULT_COURT_LIST_FUTURE})
    void testValidateWithErrorWhenCaseNumberHaveLengthGreaterThan10(ListType listType) throws IOException {
        HeaderGroup headerGroup = new HeaderGroup(PROVENANCE, "", ArtefactType.LIST, Sensitivity.PUBLIC,
                                                  Language.ENGLISH, DISPLAY_FROM, DISPLAY_TO, listType, COURT_ID,
                                                  CONTENT_DATE);
        try (InputStream jsonInput = this.getClass().getClassLoader()
            .getResourceAsStream(MAGISTRATES_ADULT_COURT_LIST_VALID_JSON)) {
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

    @ParameterizedTest
    @EnumSource(value = ListType.class,
        names = {MAGISTRATES_ADULT_COURT_LIST_DAILY, MAGISTRATES_ADULT_COURT_LIST_FUTURE})
    void testValidateWithErrorWhenOffenceCodeHaveLengthGreaterThan10(ListType listType) throws IOException {
        HeaderGroup headerGroup = new HeaderGroup(PROVENANCE, "", ArtefactType.LIST, Sensitivity.PUBLIC,
                                                  Language.ENGLISH, DISPLAY_FROM, DISPLAY_TO, listType, COURT_ID,
                                                  CONTENT_DATE);
        try (InputStream jsonInput = this.getClass().getClassLoader()
            .getResourceAsStream(MAGISTRATES_ADULT_COURT_LIST_VALID_JSON)) {
            String text = new String(jsonInput.readAllBytes(), StandardCharsets.UTF_8);

            ObjectMapper mapper = new ObjectMapper();
            JsonNode node = mapper.readValue(text, JsonNode.class);

            String listJson = node.toString()
                .replaceAll("\"code\":\"[^\"]+\"", "\"code\":\"TH123456789\"");
            assertThrows(
                PayloadValidationException.class,
                () -> validationService.validateBody(listJson, headerGroup, true),
                INVALID_JSON_MESSAGE);
        }
    }

    @ParameterizedTest
    @EnumSource(value = ListType.class,
        names = {MAGISTRATES_ADULT_COURT_LIST_DAILY, MAGISTRATES_ADULT_COURT_LIST_FUTURE})
    void testValidateWithErrorWhenInvalidPrintDateFormat(ListType listType) throws IOException {
        HeaderGroup headerGroup = new HeaderGroup(PROVENANCE, "", ArtefactType.LIST, Sensitivity.PUBLIC,
                                                  Language.ENGLISH, DISPLAY_FROM, DISPLAY_TO, listType, COURT_ID,
                                                  CONTENT_DATE);
        try (InputStream jsonInput = this.getClass().getClassLoader()
            .getResourceAsStream(MAGISTRATES_ADULT_COURT_LIST_VALID_JSON)) {
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

    @ParameterizedTest
    @EnumSource(value = ListType.class,
        names = {MAGISTRATES_ADULT_COURT_LIST_DAILY, MAGISTRATES_ADULT_COURT_LIST_FUTURE})
    void testValidateWithErrorWhenInvalidDobFormat(ListType listType) throws IOException {
        HeaderGroup headerGroup = new HeaderGroup(PROVENANCE, "", ArtefactType.LIST, Sensitivity.PUBLIC,
                                                  Language.ENGLISH, DISPLAY_FROM, DISPLAY_TO, listType, COURT_ID,
                                                  CONTENT_DATE);
        try (InputStream jsonInput = this.getClass().getClassLoader()
            .getResourceAsStream(MAGISTRATES_ADULT_COURT_LIST_VALID_JSON)) {
            String text = new String(jsonInput.readAllBytes(), StandardCharsets.UTF_8);

            ObjectMapper mapper = new ObjectMapper();
            JsonNode node = mapper.readValue(text, JsonNode.class);

            String listJson = node.toString()
                .replaceAll("\"def_dob\":\"[^\"]+\"", "\"def_dob\":\"01/07/81\"");
            assertThrows(
                PayloadValidationException.class,
                () -> validationService.validateBody(listJson, headerGroup, true),
                INVALID_JSON_MESSAGE);
        }
    }

    @ParameterizedTest
    @EnumSource(value = ListType.class,
        names = {MAGISTRATES_ADULT_COURT_LIST_DAILY, MAGISTRATES_ADULT_COURT_LIST_FUTURE})
    void testValidateWithErrorWhenInvalidSstartTimeFormat(ListType listType) throws IOException {
        HeaderGroup headerGroup = new HeaderGroup(PROVENANCE, "", ArtefactType.LIST, Sensitivity.PUBLIC,
                                                  Language.ENGLISH, DISPLAY_FROM, DISPLAY_TO, listType, COURT_ID,
                                                  CONTENT_DATE);
        try (InputStream jsonInput = this.getClass().getClassLoader()
            .getResourceAsStream(MAGISTRATES_ADULT_COURT_LIST_VALID_JSON)) {
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

    @ParameterizedTest
    @EnumSource(value = ListType.class,
        names = {MAGISTRATES_ADULT_COURT_LIST_DAILY, MAGISTRATES_ADULT_COURT_LIST_FUTURE})
    void testValidateWithErrorWhenInvalidBstartTimeFormat(ListType listType) throws IOException {
        HeaderGroup headerGroup = new HeaderGroup(PROVENANCE, "", ArtefactType.LIST, Sensitivity.PUBLIC,
                                                  Language.ENGLISH, DISPLAY_FROM, DISPLAY_TO, listType, COURT_ID,
                                                  CONTENT_DATE);
        try (InputStream jsonInput = this.getClass().getClassLoader()
            .getResourceAsStream(MAGISTRATES_ADULT_COURT_LIST_VALID_JSON)) {
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
