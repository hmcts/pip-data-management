package uk.gov.hmcts.reform.pip.data.management.utils;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.pip.data.management.config.SearchConfiguration;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class JsonExtractorTest {

    @Mock
    SearchConfiguration searchConfiguration;

    @InjectMocks
    JsonExtractor jsonExtractor;

    private static final String VALID_PAYLOAD = "{\"test\":\"test-1234\"}";
    private static final String TEST_KEY = "test-id";
    private static final String TEST_KEY_NOT_FOUND = "test-id-not-found";
    private static final String SEARCH_TERM_FOUND = "$['test']";
    private static final String SEARCH_TERM_NOT_FOUND = "$['test1']";

    @Test
    public void testExtractSearchTerms() {
        Map<String, String> searchValues = new HashMap<>();
        searchValues.put(TEST_KEY, SEARCH_TERM_FOUND);

        when(searchConfiguration.getSearchValues()).thenReturn(searchValues);

        Map<String, List<Object>> searchTerms = jsonExtractor.extractSearchTerms(VALID_PAYLOAD);

        assertTrue(searchTerms.containsKey(TEST_KEY), "Search term does not contain expected key");
        assertEquals(1, searchTerms.get(TEST_KEY).size(), "Search term does not "
            + "contain expected size of values");
        assertEquals("test-1234", searchTerms.get(TEST_KEY).get(0));
    }

    @Test
    public void testExtractSearchTermWhereMissing() {
        Map<String, String> searchValues = new HashMap<>();
        searchValues.put(TEST_KEY, SEARCH_TERM_FOUND);
        searchValues.put(TEST_KEY_NOT_FOUND, SEARCH_TERM_NOT_FOUND);

        when(searchConfiguration.getSearchValues()).thenReturn(searchValues);

        Map<String, List<Object>> searchTerms = jsonExtractor.extractSearchTerms(VALID_PAYLOAD);

        assertTrue(searchTerms.containsKey(TEST_KEY), "Search term does not contain expected key");
        assertFalse(searchTerms.containsKey(TEST_KEY_NOT_FOUND), "Search term contains unexpected key");
        assertEquals(1, searchTerms.get(TEST_KEY).size(), "Search term does not "
            + "contain expected size of values");
        assertEquals("test-1234", searchTerms.get(TEST_KEY).get(0));
    }

    @Test
    public void testIsAccepted() {
        assertTrue(jsonExtractor.isAccepted(VALID_PAYLOAD), "Valid JSON string marked as not accepted");
    }

    @Test
    public void testNotAccepted() {
        assertFalse(jsonExtractor.isAccepted("invalid-test"), "Invalid JSON string marked as accepted");
    }
}
