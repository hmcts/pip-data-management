package uk.gov.hmcts.reform.pip.data.management.utils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

@ActiveProfiles("integration-basic")
@SpringBootTest
class JsonExtractorTest {

    @Autowired
    JsonExtractor jsonExtractor;

    private static final String CASES_KEY = "cases";
    private static final String CASE_NUMBER = "caseNumber";
    private static final String CASE_NAME = "caseName";
    private static final String CASE_URN = "caseUrn";

    private static final String PARTIES_KEY = "parties";
    private static final String ORGANISATIONS_KEY = "organisations";
    private static final String INDIVIDUALS_KEY = "individuals";
    private static final String FORENAME = "forename";
    private static final String MIDDLE_NAME = "middleName";
    private static final String SURNAME = "surname";

    private static final String TEST_KEY_NOT_FOUND = "test-id-not-found";
    private static final String UNKNOWN_EXCEPTION = "Unknown exception when opening the paylaod file";
    private static final String MOCK_PARTIES_FILE = "data/jsonPayloadWithParties.json";

    private static final String SEARCH_TERM_MESSAGE = "Search term does not contain expected key";
    private static final String ORGANISATION_KEY_MESSAGE = "Parties does not contain organisations key";
    private static final String ORGANISATION_COUNT_MESSAGE = "The number of organisation names does not match";
    private static final String ORGANISATION_NAME_MESSAGE = "Organisation name does not match";
    private static final String INDIVIDUAL_KEY_MESSAGE = "Parties does not contain individuals key";
    private static final String INDIVIDUAL_COUNT_MESSAGE = "The number of individual names does not match";
    private static final String INDIVIDUAL_FORENAME_MESSAGE = "Individual forename does not match";
    private static final String INDIVIDUAL_MIDDLE_NAME_MESSAGE = "Individual middle name does not match";
    private static final String INDIVIDUAL_SURNAME_MESSAGE = "Individual surname does not match";
    private static final String CASES_KEY_MESSAGE = "Parties does not contain cases key";
    private static final String CASE_NUMBER_MESSAGE = "Case number does not match";
    private static final String CASE_NAME_MESSAGE = "Case name does not match";
    private static final String CASE_URN_MESSAGE = "Case URN does not match";

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Test
    void testExtractSearchTerms() {
        try (InputStream mockFile = this.getClass().getClassLoader()
            .getResourceAsStream("data/jsonPayload.json")) {
            String textJson = new String(mockFile.readAllBytes(), StandardCharsets.UTF_8);
            Map<String, List<Object>> searchTerms = jsonExtractor.extractSearchTerms(textJson);

            assertTrue(searchTerms.containsKey(CASES_KEY), SEARCH_TERM_MESSAGE);
            assertEquals(1, searchTerms.get(CASES_KEY).size(), "Search term does not "
                + "contain expected size of values");
            assertEquals("{caseNumber=CASE1234, caseName=, caseUrn=null}",
                         searchTerms.get(CASES_KEY).get(0).toString(),
                         "The search term value does not contain expected result");
        } catch (IOException exception) {
            fail(UNKNOWN_EXCEPTION);
        }
    }

    @Test
    void testExtractPartiesForHearingWithMultipleCases() {
        try (InputStream mockFile = this.getClass().getClassLoader()
            .getResourceAsStream(MOCK_PARTIES_FILE)) {

            String textJson = new String(mockFile.readAllBytes(), StandardCharsets.UTF_8);
            Map<String, List<Object>> searchTerms = jsonExtractor.extractSearchTerms(textJson);
            assertTrue(searchTerms.containsKey(PARTIES_KEY), SEARCH_TERM_MESSAGE);

            List<Map<String, Object>> parties = OBJECT_MAPPER.convertValue(searchTerms.get(PARTIES_KEY),
                                                                           new TypeReference<>() {});
            assertEquals(5, parties.size(), "Party array not expected size");

            Map<String, Object> party = parties.get(0);
            assertTrue(party.containsKey(CASES_KEY), CASES_KEY_MESSAGE);
            assertTrue(party.containsKey(ORGANISATIONS_KEY), ORGANISATION_KEY_MESSAGE);
            assertTrue(party.containsKey(INDIVIDUALS_KEY), INDIVIDUAL_KEY_MESSAGE);

            List<Map<String, String>> cases = OBJECT_MAPPER.convertValue(party.get(CASES_KEY),
                                                                         new TypeReference<>() {});
            assertEquals("CASE1234", cases.get(0).get(CASE_NUMBER), CASE_NUMBER_MESSAGE);
            assertEquals("NAME1", cases.get(0).get(CASE_NAME), CASE_NAME_MESSAGE);
            assertEquals("URN1234", cases.get(0).get(CASE_URN), CASE_URN_MESSAGE);

            List<String> organisationNames = OBJECT_MAPPER.convertValue(party.get(ORGANISATIONS_KEY),
                                                                        new TypeReference<>() {});
            assertEquals(1, organisationNames.size(), ORGANISATION_COUNT_MESSAGE);
            assertTrue(organisationNames.contains("Org Name"), ORGANISATION_NAME_MESSAGE);
            assertEquals("Org Name", organisationNames.get(0), ORGANISATION_NAME_MESSAGE);

            List<Map<String, Object>> individualNames = OBJECT_MAPPER.convertValue(party.get(INDIVIDUALS_KEY),
                                                                                   new TypeReference<>() {});
            assertEquals(1, individualNames.size(), INDIVIDUAL_COUNT_MESSAGE);
            assertEquals("Surname", individualNames.get(0).get(SURNAME), INDIVIDUAL_SURNAME_MESSAGE);

            party = parties.get(1);
            assertTrue(party.containsKey(CASES_KEY), CASES_KEY_MESSAGE);
            assertTrue(party.containsKey(ORGANISATIONS_KEY), ORGANISATION_KEY_MESSAGE);
            assertTrue(party.containsKey(INDIVIDUALS_KEY), INDIVIDUAL_KEY_MESSAGE);

            cases = OBJECT_MAPPER.convertValue(party.get(CASES_KEY), new TypeReference<>() {});
            assertEquals("CASE1235", cases.get(0).get(CASE_NUMBER), CASE_NUMBER_MESSAGE);
            assertEquals("NAME2", cases.get(0).get(CASE_NAME), CASE_NAME_MESSAGE);
            assertEquals("URN1235", cases.get(0).get(CASE_URN), CASE_URN_MESSAGE);

            organisationNames = OBJECT_MAPPER.convertValue(party.get(ORGANISATIONS_KEY), new TypeReference<>() {});
            assertEquals(0, organisationNames.size(), ORGANISATION_COUNT_MESSAGE);

            individualNames = OBJECT_MAPPER.convertValue(party.get(INDIVIDUALS_KEY), new TypeReference<>() {});
            assertEquals(1, individualNames.size(), INDIVIDUAL_COUNT_MESSAGE);
            assertEquals("Surname2", individualNames.get(0).get(SURNAME), INDIVIDUAL_SURNAME_MESSAGE);
        } catch (IOException e) {
            fail(UNKNOWN_EXCEPTION);
        }
    }

    //Note - By checking the array size / contents - this test also captures exclusion of representatives
    // null and missing party roles
    @Test
    void testExtractPartiesPartiesArray() {
        try (InputStream mockFile = this.getClass().getClassLoader()
            .getResourceAsStream(MOCK_PARTIES_FILE)) {

            String textJson = new String(mockFile.readAllBytes(), StandardCharsets.UTF_8);
            Map<String, List<Object>> searchTerms = jsonExtractor.extractSearchTerms(textJson);
            assertTrue(searchTerms.containsKey(PARTIES_KEY), SEARCH_TERM_MESSAGE);

            List<Map<String, Object>> parties = OBJECT_MAPPER.convertValue(searchTerms.get(PARTIES_KEY),
                                                                           new TypeReference<>() {});
            Map<String, Object> party = parties.get(2);
            assertTrue(party.containsKey(CASES_KEY), CASES_KEY_MESSAGE);
            assertTrue(party.containsKey(ORGANISATIONS_KEY), ORGANISATION_KEY_MESSAGE);
            assertTrue(party.containsKey(INDIVIDUALS_KEY), INDIVIDUAL_KEY_MESSAGE);

            List<Map<String, String>> cases = OBJECT_MAPPER.convertValue(party.get(CASES_KEY),
                                                                         new TypeReference<>() {});
            assertEquals("CASE1236", cases.get(0).get(CASE_NUMBER), CASE_NUMBER_MESSAGE);
            assertEquals("NAME3", cases.get(0).get(CASE_NAME), CASE_NAME_MESSAGE);
            assertEquals("URN1236", cases.get(0).get(CASE_URN), CASE_URN_MESSAGE);

            List<String> organisationNames = OBJECT_MAPPER.convertValue(party.get(ORGANISATIONS_KEY),
                                                                        new TypeReference<>() {});
            assertEquals(2, organisationNames.size(), ORGANISATION_COUNT_MESSAGE);
            assertTrue(organisationNames.contains("Applicant Org Name"), ORGANISATION_NAME_MESSAGE);
            assertTrue(organisationNames.contains("Respondent Org Name"), ORGANISATION_NAME_MESSAGE);

            List<Map<String, Object>> individualNames = OBJECT_MAPPER.convertValue(party.get(INDIVIDUALS_KEY),
                                                                                   new TypeReference<>() {});
            assertEquals(2, individualNames.size(), INDIVIDUAL_COUNT_MESSAGE);
            assertEquals("Applicant Forename", individualNames.get(0).get(FORENAME),
                         INDIVIDUAL_FORENAME_MESSAGE);
            assertEquals("Applicant middle name", individualNames.get(0).get(MIDDLE_NAME),
                         INDIVIDUAL_MIDDLE_NAME_MESSAGE);
            assertEquals("Applicant Surname", individualNames.get(0).get(SURNAME),
                         INDIVIDUAL_SURNAME_MESSAGE);

            assertNull(INDIVIDUAL_FORENAME_MESSAGE, individualNames.get(1).get(FORENAME));
            assertNull(INDIVIDUAL_MIDDLE_NAME_MESSAGE, individualNames.get(1).get(MIDDLE_NAME));
            assertEquals("Respondent Surname", individualNames.get(1).get(SURNAME), INDIVIDUAL_SURNAME_MESSAGE);
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
            List<Map<String, Object>> parties = OBJECT_MAPPER.convertValue(searchTerms.get(PARTIES_KEY),
                                                                           new TypeReference<>() {});
            Map<String, Object> party = parties.get(3);
            assertTrue(party.containsKey(CASES_KEY), CASES_KEY_MESSAGE);
            assertTrue(party.containsKey(ORGANISATIONS_KEY), ORGANISATION_KEY_MESSAGE);
            assertTrue(party.containsKey(INDIVIDUALS_KEY), INDIVIDUAL_KEY_MESSAGE);

            List<Map<String, String>> cases = OBJECT_MAPPER.convertValue(party.get(CASES_KEY),
                                                                         new TypeReference<>() {});
            assertEquals("CASE1237", cases.get(0).get(CASE_NUMBER), CASE_NUMBER_MESSAGE);
            assertEquals("NAME4", cases.get(0).get(CASE_NAME), CASE_NAME_MESSAGE);
            assertEquals("URN1237", cases.get(0).get(CASE_URN), CASE_URN_MESSAGE);

            List<String> organisationNames = OBJECT_MAPPER.convertValue(party.get(ORGANISATIONS_KEY),
                                                                        new TypeReference<>() {});
            assertEquals(0, organisationNames.size(), ORGANISATION_COUNT_MESSAGE);

            List<Map<String, Object>> individualNames = OBJECT_MAPPER.convertValue(party.get(INDIVIDUALS_KEY),
                                                                                   new TypeReference<>() {});
            assertEquals(0, individualNames.size(), INDIVIDUAL_COUNT_MESSAGE);
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
            List<Map<String, Object>> parties = OBJECT_MAPPER.convertValue(searchTerms.get(PARTIES_KEY),
                                                                           new TypeReference<>() {});
            Map<String, Object> party = parties.get(4);
            assertTrue(party.containsKey(CASES_KEY), CASES_KEY_MESSAGE);
            assertTrue(party.containsKey(ORGANISATIONS_KEY), ORGANISATION_KEY_MESSAGE);
            assertTrue(party.containsKey(INDIVIDUALS_KEY), INDIVIDUAL_KEY_MESSAGE);

            List<Map<String, String>> cases = OBJECT_MAPPER.convertValue(party.get(CASES_KEY),
                                                                         new TypeReference<>() {});
            assertEquals("CASE1238", cases.get(0).get(CASE_NUMBER), CASE_NUMBER_MESSAGE);
            assertEquals("NAME5", cases.get(0).get(CASE_NAME), CASE_NAME_MESSAGE);
            assertEquals("URN1238", cases.get(0).get(CASE_URN), CASE_URN_MESSAGE);

            List<String> organisationNames = OBJECT_MAPPER.convertValue(party.get(ORGANISATIONS_KEY),
                                                                        new TypeReference<>() {});
            assertEquals(0, organisationNames.size(), ORGANISATION_COUNT_MESSAGE);

            List<Map<String, Object>> individualNames = OBJECT_MAPPER.convertValue(party.get(INDIVIDUALS_KEY),
                                                                                   new TypeReference<>() {});
            assertEquals(0, individualNames.size(), INDIVIDUAL_COUNT_MESSAGE);
        } catch (IOException exception) {
            fail(UNKNOWN_EXCEPTION);
        }
    }

    @Test
    void testExtractSearchTermWhereMissing() {
        try (InputStream mockFile = this.getClass().getClassLoader()
            .getResourceAsStream("data/jsonPayload.json")) {
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
}
