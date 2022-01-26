package uk.gov.hmcts.reform.pip.data.management.utils;

import io.zonky.test.db.AutoConfigureEmbeddedDatabase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.pip.data.management.Application;
import uk.gov.hmcts.reform.pip.data.management.config.AzureBlobConfigurationTest;
import uk.gov.hmcts.reform.pip.data.management.errorhandling.exceptions.PayloadValidationException;
import uk.gov.hmcts.reform.pip.data.management.models.publication.Artefact;
import uk.gov.hmcts.reform.pip.data.management.models.publication.ListType;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

@SpringBootTest(classes = {Application.class, AzureBlobConfigurationTest.class})
@ActiveProfiles(profiles = "test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@AutoConfigureEmbeddedDatabase(type = AutoConfigureEmbeddedDatabase.DatabaseType.POSTGRES)
class JsonExtractorTest {

    @Autowired
    JsonExtractor jsonExtractor;

    private static final String VALID_PAYLOAD = "{\"test\":\"test-1234\"}";
    private static final String TEST_KEY = "case-id";
    private static final String TEST_KEY_NOT_FOUND = "test-id-not-found";
    private static final String UNKNOWN_EXCEPTION = "Unknown exception when opening the paylaod file";

    @Test
    void testExtractSearchTerms() {
        try (InputStream mockFile = this.getClass().getClassLoader()
            .getResourceAsStream("mocks/jsonPayload.json")) {
            String textJson = new String(mockFile.readAllBytes(), StandardCharsets.UTF_8);
            Map<String, List<Object>> searchTerms = jsonExtractor.extractSearchTerms(textJson);

            assertTrue(searchTerms.containsKey(TEST_KEY), "Search term does not contain expected key");
            assertEquals(1, searchTerms.get(TEST_KEY).size(), "Search term does not "
                + "contain expected size of values");
            assertEquals("CASE1234", searchTerms.get(TEST_KEY).get(0), "The search term value"
                + "does not contain expected result");
        } catch (IOException exception) {
            fail(UNKNOWN_EXCEPTION);
        }

    }

    @Test
    void testExtractSearchTermWhereMissing() {
        try (InputStream mockFile = this.getClass().getClassLoader()
            .getResourceAsStream("mocks/jsonPayload.json")) {
            String textJson = new String(mockFile.readAllBytes(), StandardCharsets.UTF_8);
            Map<String, List<Object>> searchTerms = jsonExtractor.extractSearchTerms(textJson);

            assertTrue(searchTerms.containsKey(TEST_KEY), "Search term does not contain expected key");
            assertFalse(searchTerms.containsKey(TEST_KEY_NOT_FOUND), "Search term contains unexpected key");
            assertEquals(1, searchTerms.get(TEST_KEY).size(), "Search term does not "
                + "contain expected size of values");
            assertEquals("CASE1234", searchTerms.get(TEST_KEY).get(0), "The search term value"
                + "does not contain expected result");
        } catch (IOException exception) {
            fail(UNKNOWN_EXCEPTION);
        }
    }

    @Test
    void testIsAccepted() {
        assertTrue(jsonExtractor.isAccepted(VALID_PAYLOAD), "Valid JSON string marked as not accepted");
    }

    @Test
    void testNotAccepted() {
        assertFalse(jsonExtractor.isAccepted("invalid-test"), "Invalid JSON string marked as accepted");
    }

    @Test
    void testValidateMasterSchemaWithErrors() {
        try (InputStream jsonInput = this.getClass().getClassLoader()
            .getResourceAsStream("mocks/badJsonPayload.json")) {
            String text = new String(jsonInput.readAllBytes(), StandardCharsets.UTF_8);
            assertFalse(jsonExtractor.validate(new Artefact(), text).isEmpty(),
                        "Valid JSON string marked as not valid");
        } catch (IOException exception) {
            fail(UNKNOWN_EXCEPTION);
        }
    }

    @Test
    void testValidateMasterSchemaWithoutErrors() throws IOException {
        try (InputStream jsonInput = this.getClass().getClassLoader()
            .getResourceAsStream("mocks/jsonPayload.json")) {
            String text = new String(jsonInput.readAllBytes(), StandardCharsets.UTF_8);
            assertTrue(jsonExtractor.validate(new Artefact(), text).isEmpty(), "Valid JSON string marked as valid");
        }
    }

    @Test
    void testValidateWithoutErrorsWhenArtefactIsDailyCauseList() throws IOException {
        try (InputStream jsonInput = this.getClass().getClassLoader()
            .getResourceAsStream("mocks/dailyCauseList.json")) {
            String text = new String(jsonInput.readAllBytes(), StandardCharsets.UTF_8);

            Artefact artefact = new Artefact();
            artefact.setListType(ListType.CIVIL_DAILY_CAUSE_LIST);
            assertTrue(jsonExtractor.validate(artefact, text).isEmpty(), "Valid JSON string marked as valid");
        }
    }

    @Test
    void testValidateWithErrorsWhenArtefactIsDailyCauseList() throws IOException {
        try (InputStream jsonInput = this.getClass().getClassLoader()
            .getResourceAsStream("mocks/dailyCauseListInvalid.json")) {
            String text = new String(jsonInput.readAllBytes(), StandardCharsets.UTF_8);

            Artefact artefact = new Artefact();
            artefact.setListType(ListType.CIVIL_DAILY_CAUSE_LIST);
            assertFalse(jsonExtractor.validate(artefact, text).isEmpty(), "Valid JSON string marked as valid");
        }
    }

    @Test
    void testExceptionWhenValidatingPayload() {
        Artefact artefact = new Artefact();
        artefact.setListType(ListType.CIVIL_DAILY_CAUSE_LIST);

        PayloadValidationException payloadValidationException = assertThrows(PayloadValidationException.class, () ->
            jsonExtractor.validate(artefact, "abcd"), "Validation exception not thrown "
            + "when value not JSON");

        assertEquals("Error while reading JSON Payload", payloadValidationException.getMessage(),
                     "JSON Payload message does not match expected exception");

    }

}
