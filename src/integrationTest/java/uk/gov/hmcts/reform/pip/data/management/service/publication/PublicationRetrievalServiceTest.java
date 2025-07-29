package uk.gov.hmcts.reform.pip.data.management.service.publication;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.pip.data.management.utils.IntegrationBasicTestBase;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ActiveProfiles("integration-basic")
@SpringBootTest
class PublicationRetrievalServiceTest extends IntegrationBasicTestBase {
    private static final String PAYLOAD_LIMIT_MESSAGE = "Payload limit result does not match";

    @Autowired
    PublicationRetrievalService publicationRetrievalService;

    @Test
    void shouldGenerateJsonSearchIfPayloadSizeWithinLimit() {
        assertTrue(publicationRetrievalService.payloadWithinJsonSearchLimit(49f), PAYLOAD_LIMIT_MESSAGE);
    }

    @Test
    void shouldGenerateJsonSearchIfNoPayloadSize() {
        assertTrue(publicationRetrievalService.payloadWithinJsonSearchLimit(null), PAYLOAD_LIMIT_MESSAGE);
    }

    @Test
    void shouldNotGenerateJsonSearchIfPayloadSizeOverLimit() {
        assertFalse(publicationRetrievalService.payloadWithinJsonSearchLimit(50f), PAYLOAD_LIMIT_MESSAGE);
    }

    @Test
    void shouldGenerateExcelIfPayloadSizeWithinLimit() {
        assertTrue(publicationRetrievalService.payloadWithinExcelLimit(99f), PAYLOAD_LIMIT_MESSAGE);
    }

    @Test
    void shouldGenerateExcelIfNoPayloadSize() {
        assertTrue(publicationRetrievalService.payloadWithinExcelLimit(null), PAYLOAD_LIMIT_MESSAGE);
    }

    @Test
    void shouldNotGenerateExcelIfPayloadSizeOverLimit() {
        assertFalse(publicationRetrievalService.payloadWithinExcelLimit(100f), PAYLOAD_LIMIT_MESSAGE);
    }

    @Test
    void shouldGeneratePdfIfPayloadSizeWithinLimit() {
        assertTrue(publicationRetrievalService.payloadWithinPdfLimit(59f), PAYLOAD_LIMIT_MESSAGE);
    }

    @Test
    void shouldGeneratePdfIfNoPayloadSize() {
        assertTrue(publicationRetrievalService.payloadWithinPdfLimit(null), PAYLOAD_LIMIT_MESSAGE);
    }

    @Test
    void shouldNotGeneratePdfIfPayloadSizeOverLimit() {
        assertFalse(publicationRetrievalService.payloadWithinPdfLimit(60f), PAYLOAD_LIMIT_MESSAGE);
    }
}
