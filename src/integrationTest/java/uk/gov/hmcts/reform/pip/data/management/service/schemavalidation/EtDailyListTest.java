package uk.gov.hmcts.reform.pip.data.management.service.schemavalidation;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.zonky.test.db.AutoConfigureEmbeddedDatabase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.pip.data.management.errorhandling.exceptions.PayloadValidationException;
import uk.gov.hmcts.reform.pip.data.management.helpers.JsonHelper;
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

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ActiveProfiles("integration")
@SpringBootTest
@AutoConfigureEmbeddedDatabase(type = AutoConfigureEmbeddedDatabase.DatabaseType.POSTGRES)
class EtDailyListTest {

    @Autowired
    ValidationService validationService;

    private static final String ET_DAILY_LIST_VALID = "data/et-daily-list/etDailyList.json";
    private static final String ET_DAILY_LIST_WITH_NEW_LINES = "data/et-daily-list/etDailyListWithNewLines.json";
    private static final String SOURCE_ARTEFACT_ID = "sourceArtefactId";
    private static final LocalDateTime DISPLAY_FROM = LocalDateTime.now();
    private static final LocalDateTime DISPLAY_TO = LocalDateTime.now();
    private static final Language LANGUAGE = Language.ENGLISH;
    private static final String PROVENANCE = "provenance";
    private static final Sensitivity SENSITIVITY = Sensitivity.PUBLIC;
    private static final ArtefactType ARTEFACT_TYPE = ArtefactType.LIST;
    private static final String COURT_ID = "123";
    private static final ListType LIST_TYPE = ListType.ET_DAILY_LIST;
    private static final LocalDateTime CONTENT_DATE = LocalDateTime.now();
    private static final String PUBLICATION_DATE_REGEX = "\"publicationDate\":\"[^\"]+\"";

    private HeaderGroup headerGroup;
    ObjectMapper mapper = new ObjectMapper();

    @BeforeEach
    void setup() {
        headerGroup = new HeaderGroup(PROVENANCE, SOURCE_ARTEFACT_ID, ARTEFACT_TYPE, SENSITIVITY, LANGUAGE,
                                      DISPLAY_FROM, DISPLAY_TO, LIST_TYPE, COURT_ID, CONTENT_DATE);
    }

    @Test
    void testValidPayloadPasses() throws IOException {
        try (InputStream jsonInput = this.getClass().getClassLoader()
            .getResourceAsStream(ET_DAILY_LIST_VALID)) {
            assert jsonInput != null;
            String text = new String(jsonInput.readAllBytes(), StandardCharsets.UTF_8);
            assertDoesNotThrow(() -> validationService.validateBody(text, headerGroup));
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "venue",
        "venue.venueName",
        "courtLists.0.courtHouse.courtHouseName",
        "courtLists.0.courtHouse.courtRoom.0.session.0.sittings.0.sittingStart",
        "courtLists.0.courtHouse.courtRoom.0.session.0.sittings.0.sittingEnd",
        "courtLists.0.courtHouse.courtRoom.0.session.0.sittings.0.hearing.0.case.0.caseNumber",
        "courtLists.0.courtHouse.courtRoom.0.session.0.sittings.0.hearing.0.case.0.party.0.partyRole",
        "courtLists.0.courtHouse.courtRoom.0.session.0.sittings.0.hearing.0.hearingType",
        })
    void testRequiredFieldsAreCaught(String jsonpath) throws IOException {
        try (InputStream jsonInput = this.getClass().getClassLoader()
            .getResourceAsStream(ET_DAILY_LIST_VALID)) {
            assert jsonInput != null;
            String text = new String(jsonInput.readAllBytes(), StandardCharsets.UTF_8);
            JsonNode topLevelNode = mapper.readTree(text);
            JsonHelper.safeRemoveNode(jsonpath, topLevelNode);
            String output = mapper.writeValueAsString(topLevelNode);
            assertThatExceptionOfType(PayloadValidationException.class)
                .as("should fail")
                .isThrownBy(() -> validationService.validateBody(output, headerGroup));
        }
    }

    @Test
    void testValidateWithSuccessWhenFieldsContainNewLineCharacters() throws IOException {
        try (InputStream jsonInput = this.getClass().getClassLoader()
            .getResourceAsStream(ET_DAILY_LIST_WITH_NEW_LINES)) {
            String text = new String(jsonInput.readAllBytes(), StandardCharsets.UTF_8);

            ObjectMapper mapper = new ObjectMapper();
            String listJson = mapper.readValue(text, JsonNode.class).toString();
            assertDoesNotThrow(() -> validationService.validateBody(listJson, headerGroup));
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
            .getResourceAsStream(ET_DAILY_LIST_VALID)) {
            String text = new String(jsonInput.readAllBytes(), StandardCharsets.UTF_8);

            ObjectMapper mapper = new ObjectMapper();
            JsonNode node = mapper.readValue(text, JsonNode.class);

            String listJson = node.toString()
                .replaceAll(PUBLICATION_DATE_REGEX, String.format("\"publicationDate\":\"%s\"", publicationDate));
            assertDoesNotThrow(() -> validationService.validateBody(listJson, headerGroup));
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
            .getResourceAsStream(ET_DAILY_LIST_VALID)) {
            String text = new String(jsonInput.readAllBytes(), StandardCharsets.UTF_8);

            ObjectMapper mapper = new ObjectMapper();
            JsonNode node = mapper.readValue(text, JsonNode.class);

            String listJson = node.toString()
                .replaceAll(PUBLICATION_DATE_REGEX, String.format("\"publicationDate\":\"%s\"", publicationDate));
            assertThrows(PayloadValidationException.class,
                         () -> validationService.validateBody(listJson, headerGroup));
        }
    }
}
