package uk.gov.hmcts.reform.pip.data.management.utils.hearingparty;

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
import uk.gov.hmcts.reform.pip.data.management.utils.JsonExtractor;

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

@SpringBootTest(classes = {Application.class, AzureBlobConfigurationTestConfiguration.class})
@ActiveProfiles(profiles = "test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@AutoConfigureEmbeddedDatabase(type = AutoConfigureEmbeddedDatabase.DatabaseType.POSTGRES)
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
    private static final String TEST_KEY_NOT_FOUND = "test-id-not-found";
    private static final String UNKNOWN_EXCEPTION = "Unknown exception when opening the paylaod file";
    private static final String MOCK_PARTIES_FILE = "mocks/hearingparty/jsonPayloadWithParties.json";

    private static final String SEARCH_TERM_MESSAGE = "Search term does not contain expected key";
    private static final String ORGANISATION_KEY_MESSAGE = "Parties does not contain organisations key";
    private static final String ORGANISATION_COUNT_MESSAGE = "The number of organisation names does not match";
    private static final String ORGANISATION_NAME_MESSAGE = "Organisation name does not match";
    private static final String INDIVIDUAL_KEY_MESSAGE = "Parties does not contain individuals key";
    private static final String INDIVIDUAL_COUNT_MESSAGE = "The number of individual names does not match";
    private static final String INDIVIDUAL_FORENAME_MESSAGE = "Individual forename does not match";
    private static final String INDIVIDUAL_MIDDLE_NAME_MESSAGE = "Individual middle name does not match";
    private static final String INDIVIDUAL_SURNAME_MESSAGE = "Individual surname does not match";
    private static final String CASE_NUMBER_MESSAGE = "Case number does not match";
    private static final String CASE_NAME_MESSAGE = "Case name does not match";
    private static final String CASE_URN_MESSAGE = "Case URN does not match";

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Test
    void testExtractSearchTerms() {
        try (InputStream mockFile = this.getClass().getClassLoader()
            .getResourceAsStream("mocks/hearingparty/jsonPayload.json")) {
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
    void testExtractPartiesForHearingWithMultipleCases() {
        try (InputStream mockFile = this.getClass().getClassLoader()
            .getResourceAsStream(MOCK_PARTIES_FILE)) {
            String textJson = new String(mockFile.readAllBytes(), StandardCharsets.UTF_8);
            Map<String, List<Object>> searchTerms = jsonExtractor.extractSearchTerms(textJson);
            assertTrue(searchTerms.containsKey(PARTIES_KEY), SEARCH_TERM_MESSAGE);

            List<Map<String, Object>> parties = OBJECT_MAPPER.convertValue(searchTerms.get(PARTIES_KEY),
                                                                           new TypeReference<>() {});
            assertEquals(3, parties.size(), "Party array not expected size");

            List<Map<String, String>> firstPartyCases = OBJECT_MAPPER.convertValue(parties.get(0).get(CASES_KEY),
                                                                                   new TypeReference<>() {});
            assertEquals("CASE1236", firstPartyCases.get(0).get(CASE_NUMBER), CASE_NUMBER_MESSAGE);
            assertEquals("NAME3", firstPartyCases.get(0).get(CASE_NAME), CASE_NAME_MESSAGE);
            assertEquals("URN1236", firstPartyCases.get(0).get(CASE_URN), CASE_URN_MESSAGE);

            List<Map<String, String>> secondPartyCases = OBJECT_MAPPER.convertValue(parties.get(1).get(CASES_KEY),
                                                                                   new TypeReference<>() {});
            assertEquals("CASE1237", secondPartyCases.get(0).get(CASE_NUMBER), CASE_NUMBER_MESSAGE);
            assertEquals("NAME4", secondPartyCases.get(0).get(CASE_NAME), CASE_NAME_MESSAGE);
            assertEquals("URN1237", secondPartyCases.get(0).get(CASE_URN), CASE_URN_MESSAGE);

            List<Map<String, String>> thirdPartyCases = OBJECT_MAPPER.convertValue(parties.get(2).get(CASES_KEY),
                                                                                   new TypeReference<>() {});
            assertEquals("CASE1238", thirdPartyCases.get(0).get(CASE_NUMBER), CASE_NUMBER_MESSAGE);
            assertEquals("NAME5", thirdPartyCases.get(0).get(CASE_NAME), CASE_NAME_MESSAGE);
            assertEquals("URN1238", thirdPartyCases.get(0).get(CASE_URN), CASE_URN_MESSAGE);
        } catch (IOException e) {
            fail(UNKNOWN_EXCEPTION);
        }
    }

    // Note - By checking the array size / contents - this test also captures exclusion of representatives
    // null and missing party roles
    @Test
    void testExtractPartiesForHearingWithSingleCase() {
        try (InputStream mockFile = this.getClass().getClassLoader()
            .getResourceAsStream(MOCK_PARTIES_FILE)) {

            String textJson = new String(mockFile.readAllBytes(), StandardCharsets.UTF_8);
            Map<String, List<Object>> searchTerms = jsonExtractor.extractSearchTerms(textJson);
            assertTrue(searchTerms.containsKey(PARTIES_KEY), SEARCH_TERM_MESSAGE);

            List<Map<String, Object>> parties = OBJECT_MAPPER.convertValue(searchTerms.get(PARTIES_KEY),
                                                                           new TypeReference<>() {});

            Map<String, Object> firstParty = parties.get(0);
            assertTrue(firstParty.containsKey(ORGANISATIONS_KEY), ORGANISATION_KEY_MESSAGE);
            assertTrue(firstParty.containsKey(INDIVIDUALS_KEY), INDIVIDUAL_KEY_MESSAGE);

            List<String> organisationNames = OBJECT_MAPPER.convertValue(firstParty.get(ORGANISATIONS_KEY),
                                                                        new TypeReference<>() {});

            assertEquals(2, organisationNames.size(), ORGANISATION_COUNT_MESSAGE);
            assertTrue(organisationNames.contains("Applicant Org Name"), ORGANISATION_NAME_MESSAGE);
            assertTrue(organisationNames.contains("Respondent Org Name"), ORGANISATION_NAME_MESSAGE);

            List<Map<String, Object>> individualNames = OBJECT_MAPPER.convertValue(firstParty.get(INDIVIDUALS_KEY),
                                                                                   new TypeReference<>() {});

            assertEquals(2, individualNames.size(), INDIVIDUAL_COUNT_MESSAGE);
            assertEquals("Applicant Forename", individualNames.get(0).get("forename"),
                         INDIVIDUAL_FORENAME_MESSAGE);
            assertEquals("Applicant middle name", individualNames.get(0).get("middleName"),
                         INDIVIDUAL_MIDDLE_NAME_MESSAGE);
            assertEquals("Applicant Surname", individualNames.get(0).get("surname"),
                         INDIVIDUAL_SURNAME_MESSAGE);

            assertNull(INDIVIDUAL_FORENAME_MESSAGE, individualNames.get(1).get("forename"));
            assertNull(INDIVIDUAL_MIDDLE_NAME_MESSAGE, individualNames.get(1).get("middleName"));
            assertEquals("Respondent Surname", individualNames.get(1).get("surname"),
                         INDIVIDUAL_SURNAME_MESSAGE);

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
            Map<String, Object> secondParty = parties.get(1);
            assertTrue(secondParty.containsKey(ORGANISATIONS_KEY), ORGANISATION_KEY_MESSAGE);
            assertTrue(secondParty.containsKey(INDIVIDUALS_KEY), INDIVIDUAL_KEY_MESSAGE);

            List<String> organisationNames = OBJECT_MAPPER.convertValue(secondParty.get(ORGANISATIONS_KEY),
                                                                        new TypeReference<>() {});
            assertEquals(0, organisationNames.size(), ORGANISATION_COUNT_MESSAGE);

            List<Map<String, Object>> individualNames = OBJECT_MAPPER.convertValue(secondParty.get(INDIVIDUALS_KEY),
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
            Map<String, Object> thirdParty = parties.get(2);

            assertTrue(thirdParty.containsKey(ORGANISATIONS_KEY), ORGANISATION_KEY_MESSAGE);
            assertTrue(thirdParty.containsKey(INDIVIDUALS_KEY), INDIVIDUAL_KEY_MESSAGE);

            List<String> organisationNames = OBJECT_MAPPER.convertValue(thirdParty.get(ORGANISATIONS_KEY),
                                                                        new TypeReference<>() {});
            assertEquals(0, organisationNames.size(), ORGANISATION_COUNT_MESSAGE);

            List<Map<String, Object>> individualNames = OBJECT_MAPPER.convertValue(thirdParty.get(INDIVIDUALS_KEY),
                                                                                   new TypeReference<>() {});
            assertEquals(0, individualNames.size(), INDIVIDUAL_COUNT_MESSAGE);
        } catch (IOException exception) {
            fail(UNKNOWN_EXCEPTION);
        }
    }

    @Test
    void testExtractSearchTermWhereMissing() {
        try (InputStream mockFile = this.getClass().getClassLoader()
            .getResourceAsStream("mocks/hearingparty/jsonPayload.json")) {
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
