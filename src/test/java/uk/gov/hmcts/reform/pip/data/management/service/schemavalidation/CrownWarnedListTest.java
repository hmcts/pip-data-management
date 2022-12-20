package uk.gov.hmcts.reform.pip.data.management.service.schemavalidation;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.zonky.test.db.AutoConfigureEmbeddedDatabase;
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
import uk.gov.hmcts.reform.pip.data.management.models.publication.ListType;
import uk.gov.hmcts.reform.pip.data.management.service.ValidationService;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

@SpringBootTest(classes = {Application.class, AzureBlobConfigurationTestConfiguration.class})
@ActiveProfiles(profiles = "test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@AutoConfigureEmbeddedDatabase(type = AutoConfigureEmbeddedDatabase.DatabaseType.POSTGRES)
class CrownWarnedListTest {
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private static final String CROWN_WARNED_LIST_VALID_JSON =
        "mocks/crown-warned-list/crownWarnedList.json";
    private static final String CROWN_WARNED_LIST_INVALID_MESSAGE =
        "Invalid crown warned list marked as valid";

    @Autowired
    ValidationService validationService;

    @ParameterizedTest
    @ValueSource(strings = {
        "document",
        "document.publicationDate",
        "venue",
        "venue.venueName",
        "venue.venueAddress",
        "courtLists",
        "courtLists.0.courtHouse",
        "courtLists.0.courtHouse.courtRoom",
        "courtLists.0.courtHouse.courtRoom.0.session",
        "courtLists.0.courtHouse.courtRoom.0.session.0.sittings",
        "courtLists.0.courtHouse.courtRoom.0.session.0.sittings.0.hearing",
        "courtLists.0.courtHouse.courtRoom.0.session.0.sittings.0.hearing.0.case",
        "courtLists.0.courtHouse.courtRoom.0.session.0.sittings.0.hearing.0.case.0.caseNumber"
    })
    void testValidateWithErrorWhenRequiredFieldMissing(String jsonpath) throws IOException {
        try (InputStream jsonInput = this.getClass().getClassLoader()
            .getResourceAsStream(CROWN_WARNED_LIST_VALID_JSON)) {
            assert jsonInput != null;
            String text = new String(jsonInput.readAllBytes(), StandardCharsets.UTF_8);
            JsonNode topLevelNode = MAPPER.readTree(text);
            JsonHelper.safeRemoveNode(jsonpath, topLevelNode);
            String output = MAPPER.writeValueAsString(topLevelNode);
            assertThatExceptionOfType(PayloadValidationException.class)
                .as(CROWN_WARNED_LIST_INVALID_MESSAGE)
                .isThrownBy(() -> validationService.validateBody(output, ListType.CROWN_WARNED_LIST));
        }
    }
}
