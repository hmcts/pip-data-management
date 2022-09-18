package uk.gov.hmcts.reform.pip.data.management.service.schemavalidation;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.zonky.test.db.AutoConfigureEmbeddedDatabase;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.pip.data.management.Application;
import uk.gov.hmcts.reform.pip.data.management.config.AzureBlobConfigurationTest;
import uk.gov.hmcts.reform.pip.data.management.errorhandling.exceptions.PayloadValidationException;
import uk.gov.hmcts.reform.pip.data.management.helpers.JsonHelper;
import uk.gov.hmcts.reform.pip.data.management.models.publication.ListType;
import uk.gov.hmcts.reform.pip.data.management.service.ValidationService;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

@SpringBootTest(classes = {Application.class, AzureBlobConfigurationTest.class})
@ActiveProfiles(profiles = "test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@AutoConfigureEmbeddedDatabase(type = AutoConfigureEmbeddedDatabase.DatabaseType.POSTGRES)
class EtDailyListTest {

    @Autowired
    ValidationService validationService;

    private static final String ET_DAILY_LIST_VALID = "mocks/et-daily-list/etDailyList.json";
    ObjectMapper mapper = new ObjectMapper();

    @Test
    void testValidPayloadPasses() throws IOException {
        try (InputStream jsonInput = this.getClass().getClassLoader()
            .getResourceAsStream(ET_DAILY_LIST_VALID)) {
            assert jsonInput != null;
            String text = new String(jsonInput.readAllBytes(), StandardCharsets.UTF_8);
            assertDoesNotThrow(() -> validationService.validateBody(text, ListType.ET_DAILY_LIST));
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
        "courtLists.0.courtHouse.courtRoom.0.session.0.sittings.0.hearing.0.hearingType"})
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
                .isThrownBy(() -> validationService.validateBody(output, ListType.ET_DAILY_LIST));
        }
    }
}
