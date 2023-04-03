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
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.pip.data.management.Application;
import uk.gov.hmcts.reform.pip.data.management.config.AzureBlobConfigurationTestConfiguration;
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

@SpringBootTest(classes = {Application.class, AzureBlobConfigurationTestConfiguration.class})
@ActiveProfiles(profiles = "test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@AutoConfigureEmbeddedDatabase(type = AutoConfigureEmbeddedDatabase.DatabaseType.POSTGRES)
class EtFortnightlyPressListTest {

    @Autowired
    ValidationService validationService;

    private static final String ET_FORTNIGHTLY_PRESS_LIST_VALID_JSON =
        "mocks/et-fortnightly-press-list/etFortnightlyPressList.json";
    private static final String SOURCE_ARTEFACT_ID = "sourceArtefactId";
    private static final LocalDateTime DISPLAY_FROM = LocalDateTime.now();
    private static final LocalDateTime DISPLAY_TO = LocalDateTime.now();
    private static final Language LANGUAGE = Language.ENGLISH;
    private static final String PROVENANCE = "provenance";
    private static final Sensitivity SENSITIVITY = Sensitivity.PUBLIC;
    private static final ArtefactType ARTEFACT_TYPE = ArtefactType.LIST;
    private static final String COURT_ID = "123";
    private static final ListType LIST_TYPE = ListType.ET_FORTNIGHTLY_PRESS_LIST;
    private static final LocalDateTime CONTENT_DATE = LocalDateTime.now();

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
            .getResourceAsStream(ET_FORTNIGHTLY_PRESS_LIST_VALID_JSON)) {
            assert jsonInput != null;
            String text = new String(jsonInput.readAllBytes(), StandardCharsets.UTF_8);
            assertDoesNotThrow(() -> validationService.validateBody(text, headerGroup));
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "document",
        "document.publicationDate",
        "courtLists",
        "venue",
        "venue.venueName",
        "courtLists.0.courtHouse",
        "courtLists.0.courtHouse.courtHouseName",
        "courtLists.0.courtHouse.courtRoom",
        "courtLists.0.courtHouse.courtRoom.0.session",
        "courtLists.0.courtHouse.courtRoom.0.session.0.sittings",
        "courtLists.0.courtHouse.courtRoom.0.session.0.sittings.0.sittingStart",
        "courtLists.0.courtHouse.courtRoom.0.session.0.sittings.0.sittingEnd",
        "courtLists.0.courtHouse.courtRoom.0.session.0.sittings.0.hearing",
        "courtLists.0.courtHouse.courtRoom.0.session.0.sittings.0.hearing.0.case",
        "courtLists.0.courtHouse.courtRoom.0.session.0.sittings.0.hearing.0.case.0.caseNumber",
        "courtLists.0.courtHouse.courtRoom.0.session.0.sittings.0.hearing.0.hearingType",
        "courtLists.0.courtHouse.courtRoom.0.session.0.sittings.0.hearing.0.party.0.partyRole",
    })
    void testRequiredFieldsAreCaught(String jsonpath) throws IOException {
        try (InputStream jsonInput = this.getClass().getClassLoader()
            .getResourceAsStream(ET_FORTNIGHTLY_PRESS_LIST_VALID_JSON)) {
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
}
