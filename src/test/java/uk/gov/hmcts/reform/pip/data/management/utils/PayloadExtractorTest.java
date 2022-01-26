package uk.gov.hmcts.reform.pip.data.management.utils;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.pip.data.management.errorhandling.exceptions.PayloadValidationException;
import uk.gov.hmcts.reform.pip.data.management.models.publication.Artefact;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PayloadExtractorTest {

    private static final String PAYLOAD = "{\"test\":\"test1234\"}";
    private static final Map<String, List<Object>> TEST_MAP = new ConcurrentHashMap<>();

    @Mock
    JsonExtractor jsonExtractor;

    @Mock
    JsonExtractor jsonExtractorOther;

    @BeforeAll
    public static void setup() {
        TEST_MAP.put("TEST_KEY", List.of("TEST_VALUE"));
    }

    @Test
    void testValidExtractorFound() {
        PayloadExtractor payloadExtractor = new PayloadExtractor(List.of(jsonExtractor));

        Artefact artefact = new Artefact();
        when(jsonExtractor.isAccepted(PAYLOAD)).thenReturn(true);
        when(jsonExtractor.validate(artefact, PAYLOAD)).thenReturn(List.of());
        when(jsonExtractor.extractSearchTerms(PAYLOAD)).thenReturn(TEST_MAP);

        Map<String, List<Object>> searchTerms = payloadExtractor.validateAndParsePayload(artefact, PAYLOAD);
        assertEquals(TEST_MAP, searchTerms, "Returned search terms does not match expected terms");
    }

    @Test
    void testMultipleExtractorsCalledTwice() {
        PayloadExtractor payloadExtractor = new PayloadExtractor(List.of(jsonExtractorOther, jsonExtractor));

        Artefact artefact = new Artefact();
        when(jsonExtractor.isAccepted(PAYLOAD)).thenReturn(true);
        when(jsonExtractor.validate(artefact, PAYLOAD)).thenReturn(List.of());
        when(jsonExtractor.extractSearchTerms(PAYLOAD)).thenReturn(TEST_MAP);

        when(jsonExtractorOther.isAccepted(PAYLOAD)).thenReturn(false);

        Map<String, List<Object>> searchTerms = payloadExtractor.validateAndParsePayload(artefact, PAYLOAD);
        verify(jsonExtractorOther, times(1)).isAccepted(PAYLOAD);
        assertEquals(TEST_MAP, searchTerms, "Returned search terms does not match expected terms");
    }

    @Test
    void testNoExtractorFound() {
        PayloadExtractor payloadExtractor = new PayloadExtractor(List.of(jsonExtractor));
        when(jsonExtractor.isAccepted(PAYLOAD)).thenReturn(false);


        assertTrue(payloadExtractor.validateAndParsePayload(new Artefact(), PAYLOAD).isEmpty(),
             "Returned search terms is not empty when no extract found");
    }

    @Test
    void testExtractorFoundButInvalid() {
        final String invalidPayload = "Payload invalid";
        PayloadExtractor payloadExtractor = new PayloadExtractor(List.of(jsonExtractor));

        Artefact artefact = new Artefact();
        when(jsonExtractor.isAccepted(PAYLOAD)).thenReturn(true);
        when(jsonExtractor.validate(artefact, PAYLOAD)).thenReturn(List.of(invalidPayload));

        PayloadValidationException payloadValidationException =
            assertThrows(PayloadValidationException.class, () ->
                payloadExtractor.validateAndParsePayload(artefact, PAYLOAD));

        assertTrue(
            payloadValidationException.getMessage().contains(invalidPayload),
            "Invalid error message is thrown");
    }

}
