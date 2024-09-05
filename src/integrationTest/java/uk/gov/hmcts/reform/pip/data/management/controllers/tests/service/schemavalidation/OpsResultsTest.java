package uk.gov.hmcts.reform.pip.data.management.controllers.tests.service.schemavalidation;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.zonky.test.db.AutoConfigureEmbeddedDatabase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.pip.data.management.Application;
import uk.gov.hmcts.reform.pip.data.management.config.AzureBlobConfigurationTestConfiguration;
import uk.gov.hmcts.reform.pip.data.management.controllers.tests.helpers.JsonHelper;
import uk.gov.hmcts.reform.pip.data.management.errorhandling.exceptions.PayloadValidationException;
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

@SpringBootTest(classes = {Application.class, AzureBlobConfigurationTestConfiguration.class})
@ActiveProfiles(profiles = "test")
@AutoConfigureEmbeddedDatabase(type = AutoConfigureEmbeddedDatabase.DatabaseType.POSTGRES)
class OpsResultsTest {
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final String OPA_RESULTS_VALID_JSON = "mocks/opaResults.json";
    private static final String OPA_RESULTS_INVALID_MESSAGE = "Invalid OPA results marked as valid";

    private static final String SOURCE_ARTEFACT_ID = "sourceArtefactId";
    private static final LocalDateTime DISPLAY_FROM = LocalDateTime.now();
    private static final LocalDateTime DISPLAY_TO = LocalDateTime.now().plusDays(1);
    private static final String PROVENANCE = "provenance";
    private static final String COURT_ID = "123";
    private static final LocalDateTime CONTENT_DATE = LocalDateTime.now();

    private HeaderGroup headerGroup;

    @Autowired
    ValidationService validationService;

    @BeforeEach
    void setup() {
        headerGroup = new HeaderGroup(PROVENANCE, SOURCE_ARTEFACT_ID, ArtefactType.LIST, Sensitivity.PUBLIC,
                                      Language.ENGLISH, DISPLAY_FROM, DISPLAY_TO, ListType.OPA_PRESS_LIST, COURT_ID,
                                      CONTENT_DATE);
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "document",
        "document.publicationDate",
        "venue",
        "venue.venueName",
        "courtLists",
        "courtLists.0.courtHouse",
        "courtLists.0.courtHouse.courtRoom",
        "courtLists.0.courtHouse.courtRoom.0.session",
        "courtLists.0.courtHouse.courtRoom.0.session.0.sittings",
        "courtLists.0.courtHouse.courtRoom.0.session.0.sittings.0.hearing",
        "courtLists.0.courtHouse.courtRoom.0.session.0.sittings.0.hearing.0.case"
    })
    void testValidateWithErrorWhenRequiredFieldMissing(String jsonpath) throws IOException {
        try (InputStream jsonInput = this.getClass().getClassLoader().getResourceAsStream(OPA_RESULTS_VALID_JSON)) {
            assert jsonInput != null;
            String text = new String(jsonInput.readAllBytes(), StandardCharsets.UTF_8);
            JsonNode topLevelNode = MAPPER.readTree(text);
            JsonHelper.safeRemoveNode(jsonpath, topLevelNode);
            String output = MAPPER.writeValueAsString(topLevelNode);

            assertThatExceptionOfType(PayloadValidationException.class)
                .as(OPA_RESULTS_INVALID_MESSAGE)
                .isThrownBy(() -> validationService.validateBody(output, headerGroup));
        }
    }
}
