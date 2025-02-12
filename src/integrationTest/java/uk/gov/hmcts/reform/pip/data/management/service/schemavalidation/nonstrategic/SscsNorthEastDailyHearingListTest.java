
package uk.gov.hmcts.reform.pip.data.management.service.schemavalidation.nonstrategic;

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

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

@ActiveProfiles("integration-basic")
@SpringBootTest
class SscsNorthEastDailyHearingListTest extends IntegrationBasicTestBase {

    @Autowired
    ValidationService validationService;

    private static final String VALID_JSON = "data/non-strategic/"
        + "sscs-daily-hearing-list/"
        + "sscsDailyHearingList.json";
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

    private JsonNode getJsonNode(String json) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(json, JsonNode.class);
    }

    @BeforeEach
    void setup() {
        headerGroup = new HeaderGroup(PROVENANCE, SOURCE_ARTEFACT_ID, ARTEFACT_TYPE, SENSITIVITY, LANGUAGE,
            DISPLAY_FROM, DISPLAY_TO, ListType.SSCS_NORTHEAST_DAILY_HEARING_LIST, COURT_ID,
            CONTENT_DATE);
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
    void testValidateWithErrorsWhenAppealReferenceNumberMissingInList() throws IOException {
        try (InputStream jsonInput = this.getClass().getClassLoader()
            .getResourceAsStream(VALID_JSON)) {
            String text = new String(jsonInput.readAllBytes(), StandardCharsets.UTF_8);

            JsonNode node = getJsonNode(text);
            ((ObjectNode) node.get(0)).remove("appealReferenceNumber");

            assertThatExceptionOfType(PayloadValidationException.class)
                .as(INVALID_MESSAGE)
                .isThrownBy(() -> validationService.validateBody(node.toString(), headerGroup, false));
        }
    }

    @Test
    void testValidateWithErrorsWhenHearingTypeMissingInList() throws IOException {
        try (InputStream jsonInput = this.getClass().getClassLoader()
            .getResourceAsStream(VALID_JSON)) {
            String text = new String(jsonInput.readAllBytes(), StandardCharsets.UTF_8);

            JsonNode node = getJsonNode(text);
            ((ObjectNode) node.get(0)).remove("hearingType");

            assertThatExceptionOfType(PayloadValidationException.class)
                .as(INVALID_MESSAGE)
                .isThrownBy(() -> validationService.validateBody(node.toString(), headerGroup, false));
        }
    }

    @Test
    void testValidateWithErrorsWhenAppellantMissingInList() throws IOException {
        try (InputStream jsonInput = this.getClass().getClassLoader()
            .getResourceAsStream(VALID_JSON)) {
            String text = new String(jsonInput.readAllBytes(), StandardCharsets.UTF_8);

            JsonNode node = getJsonNode(text);
            ((ObjectNode) node.get(0)).remove("appellant");

            assertThatExceptionOfType(PayloadValidationException.class)
                .as(INVALID_MESSAGE)
                .isThrownBy(() -> validationService.validateBody(node.toString(), headerGroup, false));
        }
    }

    @Test
    void testValidateWithErrorsWhenCourtroomMissingInList() throws IOException {
        try (InputStream jsonInput = this.getClass().getClassLoader()
            .getResourceAsStream(VALID_JSON)) {
            String text = new String(jsonInput.readAllBytes(), StandardCharsets.UTF_8);

            JsonNode node = getJsonNode(text);
            ((ObjectNode) node.get(0)).remove("courtroom");

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
    void testValidateWithErrorsWhenPanelMissingInList() throws IOException {
        try (InputStream jsonInput = this.getClass().getClassLoader()
            .getResourceAsStream(VALID_JSON)) {
            String text = new String(jsonInput.readAllBytes(), StandardCharsets.UTF_8);

            JsonNode node = getJsonNode(text);
            ((ObjectNode) node.get(0)).remove("panel");

            assertThatExceptionOfType(PayloadValidationException.class)
                .as(INVALID_MESSAGE)
                .isThrownBy(() -> validationService.validateBody(node.toString(), headerGroup, false));
        }
    }

    @Test
    void testValidateWithErrorsWhenFtaOrRespondentMissingInList() throws IOException {
        try (InputStream jsonInput = this.getClass().getClassLoader()
            .getResourceAsStream(VALID_JSON)) {
            String text = new String(jsonInput.readAllBytes(), StandardCharsets.UTF_8);

            JsonNode node = getJsonNode(text);
            ((ObjectNode) node.get(0)).remove("fta/respondent");

            assertThatExceptionOfType(PayloadValidationException.class)
                .as(INVALID_MESSAGE)
                .isThrownBy(() -> validationService.validateBody(node.toString(), headerGroup, false));
        }
    }

    @Test
    void testValidateWithErrorsWhenAdditionalInformationPlatformMissingInList() throws IOException {
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
}
