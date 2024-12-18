package uk.gov.hmcts.reform.pip.data.management.service.schemavalidation.nonstrategic;

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

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

@ActiveProfiles("integration-basic")
@SpringBootTest
class GrcWeeklyHearingListTest extends IntegrationBasicTestBase {
    private static final String VALID_JSON =
        "data/non-strategic/grc-weekly-hearing-list/grcWeeklyHearingList.json";
    private static final String INVALID_MESSAGE = "Invalid JSON list marked as valid";

    private static final String SOURCE_ARTEFACT_ID = "sourceArtefactId";
    private static final LocalDateTime DISPLAY_FROM = LocalDateTime.now();
    private static final LocalDateTime DISPLAY_TO = LocalDateTime.now().plusDays(1);
    private static final Language LANGUAGE = Language.ENGLISH;
    private static final String PROVENANCE = "provenance";
    private static final Sensitivity SENSITIVITY = Sensitivity.PUBLIC;
    private static final ArtefactType ARTEFACT_TYPE = ArtefactType.LIST;
    private static final String COURT_ID = "123";
    private static final LocalDateTime CONTENT_DATE = LocalDateTime.now();

    private HeaderGroup headerGroup;

    @Autowired
    ValidationService validationService;

    private JsonNode getJsonNode(String json) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(json, JsonNode.class);
    }

    @BeforeEach
    void setup() {
        headerGroup = new HeaderGroup(PROVENANCE, SOURCE_ARTEFACT_ID, ARTEFACT_TYPE, SENSITIVITY, LANGUAGE,
                                      DISPLAY_FROM, DISPLAY_TO, ListType.CST_WEEKLY_HEARING_LIST, COURT_ID,
                                      CONTENT_DATE);
    }

    @Test
    void testValidateWithErrorsWhenDateMissingInList() throws IOException {
        try (InputStream jsonInput = this.getClass().getClassLoader()
            .getResourceAsStream(VALID_JSON)) {
            String text = new String(jsonInput.readAllBytes(), StandardCharsets.UTF_8);

            JsonNode node = getJsonNode(text);
            ((ObjectNode) node.get(0)).remove("date");

            assertThatExceptionOfType(PayloadValidationException.class)
                .as(INVALID_MESSAGE)
                .isThrownBy(() -> validationService.validateBody(node.toString(), headerGroup, false));
        }
    }

    @Test
    void testValidateWithErrorsWhenHearingTimeMissingInList() throws IOException {
        try (InputStream jsonInput = this.getClass().getClassLoader()
            .getResourceAsStream(VALID_JSON)) {
            String text = new String(jsonInput.readAllBytes(), StandardCharsets.UTF_8);

            JsonNode node = getJsonNode(text);
            ((ObjectNode) node.get(0)).remove("hearingTime");

            assertThatExceptionOfType(PayloadValidationException.class)
                .as(INVALID_MESSAGE)
                .isThrownBy(() -> validationService.validateBody(node.toString(), headerGroup, false));
        }
    }

    @Test
    void testValidateWithErrorsWhenCaseReferenceNumberMissingInList() throws IOException {
        try (InputStream jsonInput = this.getClass().getClassLoader()
            .getResourceAsStream(VALID_JSON)) {
            String text = new String(jsonInput.readAllBytes(), StandardCharsets.UTF_8);

            JsonNode node = getJsonNode(text);
            ((ObjectNode) node.get(0)).remove("caseReferenceNumber");

            assertThatExceptionOfType(PayloadValidationException.class)
                .as(INVALID_MESSAGE)
                .isThrownBy(() -> validationService.validateBody(node.toString(), headerGroup, false));
        }
    }

    @Test
    void testValidateWithErrorsWhenCaseNameMissingInList() throws IOException {
        try (InputStream jsonInput = this.getClass().getClassLoader()
            .getResourceAsStream(VALID_JSON)) {
            String text = new String(jsonInput.readAllBytes(), StandardCharsets.UTF_8);

            JsonNode node = getJsonNode(text);
            ((ObjectNode) node.get(0)).remove("caseName");

            assertThatExceptionOfType(PayloadValidationException.class)
                .as(INVALID_MESSAGE)
                .isThrownBy(() -> validationService.validateBody(node.toString(), headerGroup, false));
        }
    }

    @Test
    void testValidateWithErrorsWhenJudgesMissingInList() throws IOException {
        try (InputStream jsonInput = this.getClass().getClassLoader()
            .getResourceAsStream(VALID_JSON)) {
            String text = new String(jsonInput.readAllBytes(), StandardCharsets.UTF_8);

            JsonNode node = getJsonNode(text);
            ((ObjectNode) node.get(0)).remove("judges");

            assertThatExceptionOfType(PayloadValidationException.class)
                .as(INVALID_MESSAGE)
                .isThrownBy(() -> validationService.validateBody(node.toString(), headerGroup, false));
        }
    }

    @Test
    void testValidateWithErrorsWhenMembersMissingInList() throws IOException {
        try (InputStream jsonInput = this.getClass().getClassLoader()
            .getResourceAsStream(VALID_JSON)) {
            String text = new String(jsonInput.readAllBytes(), StandardCharsets.UTF_8);

            JsonNode node = getJsonNode(text);
            ((ObjectNode) node.get(0)).remove("members");

            assertThatExceptionOfType(PayloadValidationException.class)
                .as(INVALID_MESSAGE)
                .isThrownBy(() -> validationService.validateBody(node.toString(), headerGroup, false));
        }
    }

    @Test
    void testValidateWithErrorsWhenModeOfHearingMissingInList() throws IOException {
        try (InputStream jsonInput = this.getClass().getClassLoader()
            .getResourceAsStream(VALID_JSON)) {
            String text = new String(jsonInput.readAllBytes(), StandardCharsets.UTF_8);

            JsonNode node = getJsonNode(text);
            ((ObjectNode) node.get(0)).remove("modeOfHearing");

            assertThatExceptionOfType(PayloadValidationException.class)
                .as(INVALID_MESSAGE)
                .isThrownBy(() -> validationService.validateBody(node.toString(), headerGroup, false));
        }
    }

    @Test
    void testValidateWithErrorsWhenVenueMissingInList() throws IOException {
        try (InputStream jsonInput = this.getClass().getClassLoader()
            .getResourceAsStream(VALID_JSON)) {
            String text = new String(jsonInput.readAllBytes(), StandardCharsets.UTF_8);

            JsonNode node = getJsonNode(text);
            ((ObjectNode) node.get(0)).remove("venue");

            assertThatExceptionOfType(PayloadValidationException.class)
                .as(INVALID_MESSAGE)
                .isThrownBy(() -> validationService.validateBody(node.toString(), headerGroup, false));
        }
    }

    @Test
    void testValidateWithErrorsWhenAdditionalInformationMissingInList() throws IOException {
        try (InputStream jsonInput = this.getClass().getClassLoader()
            .getResourceAsStream(VALID_JSON)) {
            String text = new String(jsonInput.readAllBytes(), StandardCharsets.UTF_8);

            JsonNode node = getJsonNode(text);
            ((ObjectNode) node.get(0)).remove("additionalInformation");

            assertThatExceptionOfType(PayloadValidationException.class)
                .as(INVALID_MESSAGE)
                .isThrownBy(() -> validationService.validateBody(node.toString(), headerGroup, false));
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "2024-12-10",
        "1/2/2024",
        "10/12/24",
        "10-12-2024"
    })
    void testValidateWithErrorWhenInvalidDateFormat(String date) throws IOException {
        try (InputStream jsonInput = this.getClass().getClassLoader()
            .getResourceAsStream(VALID_JSON)) {
            String text = new String(jsonInput.readAllBytes(), StandardCharsets.UTF_8);

            ObjectMapper mapper = new ObjectMapper();
            JsonNode node = mapper.readValue(text, JsonNode.class);

            String formattedJson = node.toString()
                .replaceAll("\"date\":\"[^\"]+\"", String.format("\"date\":\"%s\"", date));
            assertThatExceptionOfType(PayloadValidationException.class)
                .as(INVALID_MESSAGE)
                .isThrownBy(() -> validationService.validateBody(formattedJson, headerGroup, false));
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "10.30",
        "101:30pm",
        "10:300am"
    })
    void testValidateWithErrorWhenInvalidHearingTimeFormat(String date) throws IOException {
        try (InputStream jsonInput = this.getClass().getClassLoader()
            .getResourceAsStream(VALID_JSON)) {
            String text = new String(jsonInput.readAllBytes(), StandardCharsets.UTF_8);

            ObjectMapper mapper = new ObjectMapper();
            JsonNode node = mapper.readValue(text, JsonNode.class);

            String formattedJson = node.toString()
                .replaceAll("\"hearingTime\":\"[^\"]+\"", String.format("\"hearingTime\":\"%s\"", date));
            assertThatExceptionOfType(PayloadValidationException.class)
                .as(INVALID_MESSAGE)
                .isThrownBy(() -> validationService.validateBody(formattedJson, headerGroup, false));
        }
    }
}
