package uk.gov.hmcts.reform.pip.data.management.utils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.zonky.test.db.AutoConfigureEmbeddedDatabase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.pip.data.management.Application;
import uk.gov.hmcts.reform.pip.data.management.config.AzureBlobConfigurationTestConfiguration;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

@SpringBootTest(classes = {Application.class, AzureBlobConfigurationTestConfiguration.class})
@ActiveProfiles(profiles = "test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@AutoConfigureEmbeddedDatabase(type = AutoConfigureEmbeddedDatabase.DatabaseType.POSTGRES)
class JsonExtractorTest {

    @Autowired
    JsonExtractor jsonExtractor;

    private static final String VALID_PAYLOAD = "{\"test\":\"test-1234\"}";
    private static final String CASES_KEY = "cases";

    private static final String PARTIES_KEY = "parties";
    private static final String TEST_KEY_NOT_FOUND = "test-id-not-found";
    private static final String UNKNOWN_EXCEPTION = "Unknown exception when opening the paylaod file";
    private static final String MOCK_PARTIES_FILE = "mocks/jsonPayloadWithParties.json";

    private static final String SEARCH_TERM_MESSAGE = "Search term does not contain expected key";
    private static final String PARTIES_MESSAGE = "Parties does not contain parties key";

    @Test
    void testExtractSearchTerms() {
        try (InputStream mockFile = this.getClass().getClassLoader()
            .getResourceAsStream("mocks/jsonPayload.json")) {
            String textJson = new String(mockFile.readAllBytes(), StandardCharsets.UTF_8);
            Map<String, List<Object>> searchTerms = jsonExtractor.extractSearchTerms(textJson);

            assertTrue(searchTerms.containsKey(CASES_KEY), SEARCH_TERM_MESSAGE);
            assertEquals(1, searchTerms.get(CASES_KEY).size(), "Search term does not "
                + "contain expected size of values");
            assertEquals("{caseNumber=CASE1234, caseName=, caseUrn=null}", searchTerms.get(CASES_KEY).get(0).toString(),
                         "The search term value does not contain expected result");
        } catch (IOException exception) {
            fail(UNKNOWN_EXCEPTION);
        }

    }

    @Test
    void testExtractPartiesCasesArray() {
        try (InputStream mockFile = this.getClass().getClassLoader()
            .getResourceAsStream(MOCK_PARTIES_FILE)) {

            String textJson = new String(mockFile.readAllBytes(), StandardCharsets.UTF_8);
            Map<String, List<Object>> searchTerms = jsonExtractor.extractSearchTerms(textJson);
            assertTrue(searchTerms.containsKey(PARTIES_KEY), SEARCH_TERM_MESSAGE);

            List<Map<String, Object>> parties = new ObjectMapper().convertValue(searchTerms.get(PARTIES_KEY),
                                                                                new TypeReference<>() {});
            assertEquals(3, parties.size(), "Party array not expected size");

            Map<String, Object> firstParty = parties.get(0);
            assertTrue(firstParty.containsKey(PARTIES_KEY), PARTIES_MESSAGE);
            assertTrue(firstParty.containsKey(CASES_KEY), "Parties does not contain cases key");

            List<ConcurrentHashMap<String, Object>> cases = new ObjectMapper().convertValue(firstParty.get(CASES_KEY),
                                                                                            new TypeReference<>() {});
            assertEquals(2, cases.size(), "Unexpected number of cases returned");

            ConcurrentHashMap<String, String> firstCase = new ObjectMapper().convertValue(cases.get(0),
                                                                                          new TypeReference<>() {});
            assertEquals("CASE1234", firstCase.get("caseNumber"), "Unexpected case number returned");
            assertEquals("A vs B", firstCase.get("caseName"), "Unexpected case name returned");
            assertEquals("URN1234", firstCase.get("caseUrn"), "Unexpected case urn returned");
        } catch (IOException exception) {
            fail(UNKNOWN_EXCEPTION);
        }
    }

    //Note - By checking the array size / contents - this test also captures excluding
    @Test
    void testExtractPartiesPartiesArray() {
        try (InputStream mockFile = this.getClass().getClassLoader()
            .getResourceAsStream(MOCK_PARTIES_FILE)) {

            String textJson = new String(mockFile.readAllBytes(), StandardCharsets.UTF_8);
            Map<String, List<Object>> searchTerms = jsonExtractor.extractSearchTerms(textJson);
            assertTrue(searchTerms.containsKey(PARTIES_KEY), SEARCH_TERM_MESSAGE);

            List<Map<String, Object>> parties = new ObjectMapper().convertValue(searchTerms.get(PARTIES_KEY),
                                                                                new TypeReference<>() {});
            assertEquals(3, parties.size(), "Party array not expected size");

            Map<String, Object> firstParty = parties.get(0);
            assertTrue(firstParty.containsKey(PARTIES_KEY), PARTIES_MESSAGE);

            List<String> partyNames = new ObjectMapper().convertValue(firstParty.get(PARTIES_KEY),
                                                                      new TypeReference<>() {});
            assertEquals(2, partyNames.size(), "Unexpected number of parties returned");
            assertTrue(partyNames.contains("Applicant Surname"), "Applicant not present");
            assertTrue(partyNames.contains("Respondent Org Name"), "Respondent not present");
        } catch (IOException exception) {
            fail(UNKNOWN_EXCEPTION);
        }

    }

    @Test
    void testExtractWhereNoPartiesObject() {
        try (InputStream mockFile = this.getClass().getClassLoader()
            .getResourceAsStream(MOCK_PARTIES_FILE)) {

            String textJson = new String(mockFile.readAllBytes(), StandardCharsets.UTF_8);
            Map<String, List<Object>> searchTerms = jsonExtractor.extractSearchTerms(textJson);
            List<Map<String, Object>> parties = new ObjectMapper().convertValue(searchTerms.get(PARTIES_KEY),
                                                                                new TypeReference<>() {});
            Map<String, Object> firstParty = parties.get(1);

            assertTrue(firstParty.containsKey(PARTIES_KEY), PARTIES_MESSAGE);

            List<String> partyNames = new ObjectMapper().convertValue(firstParty.get(PARTIES_KEY),
                                                                      new TypeReference<>() {});
            assertEquals(0, partyNames.size(), "Unexpected number of parties returned");
        } catch (IOException exception) {
            fail(UNKNOWN_EXCEPTION);
        }
    }

    @Test
    void testExtractWhereEmptyPartiesObject() {
        try (InputStream mockFile = this.getClass().getClassLoader()
            .getResourceAsStream(MOCK_PARTIES_FILE)) {

            String textJson = new String(mockFile.readAllBytes(), StandardCharsets.UTF_8);
            Map<String, List<Object>> searchTerms = jsonExtractor.extractSearchTerms(textJson);
            List<Map<String, Object>> parties = new ObjectMapper().convertValue(searchTerms.get(PARTIES_KEY),
                                                                                new TypeReference<>() {});
            Map<String, Object> firstParty = parties.get(2);

            assertTrue(firstParty.containsKey(PARTIES_KEY), PARTIES_MESSAGE);

            List<String> partyNames = new ObjectMapper().convertValue(firstParty.get(PARTIES_KEY),
                                                                      new TypeReference<>() {});
            assertEquals(0, partyNames.size(), "Unexpected number of parties returned");
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

            assertTrue(searchTerms.containsKey(CASES_KEY), SEARCH_TERM_MESSAGE);
            assertFalse(searchTerms.containsKey(TEST_KEY_NOT_FOUND), "Search term contains unexpected key");
            assertEquals(1, searchTerms.get(CASES_KEY).size(), "Search term does not "
                + "contain expected size of values");
            assertEquals("{caseNumber=CASE1234, caseName=, caseUrn=null}", searchTerms.get(CASES_KEY).get(0).toString(),
                         "The search term value does not contain expected result");
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
}
