package uk.gov.hmcts.reform.pip.data.management.service.publication;

import io.zonky.test.db.AutoConfigureEmbeddedDatabase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ActiveProfiles("integration")
@SpringBootTest
@AutoConfigureEmbeddedDatabase(type = AutoConfigureEmbeddedDatabase.DatabaseType.POSTGRES)
class ArtefactServiceTest {
    private static final String PAYLOAD_LIMIT_MESSAGE = "Payload limit result does not match";

    @Autowired
    ArtefactService artefactService;

    @Test
    void shouldGenerateJsonSearchIfPayloadSizeWithinLimit() {
        assertTrue(artefactService.payloadWithinJsonSearchLimit(49f), PAYLOAD_LIMIT_MESSAGE);
    }

    @Test
    void shouldGenerateJsonSearchIfNoPayloadSize() {
        assertTrue(artefactService.payloadWithinJsonSearchLimit(null), PAYLOAD_LIMIT_MESSAGE);
    }

    @Test
    void shouldNotGenerateJsonSearchIfPayloadSizeOverLimit() {
        assertFalse(artefactService.payloadWithinJsonSearchLimit(50f), PAYLOAD_LIMIT_MESSAGE);
    }

    @Test
    void shouldGenerateExcelIfPayloadSizeWithinLimit() {
        assertTrue(artefactService.payloadWithinExcelLimit(99f), PAYLOAD_LIMIT_MESSAGE);
    }

    @Test
    void shouldGenerateExcelIfNoPayloadSize() {
        assertTrue(artefactService.payloadWithinExcelLimit(null), PAYLOAD_LIMIT_MESSAGE);
    }

    @Test
    void shouldNotGenerateExcelIfPayloadSizeOverLimit() {
        assertFalse(artefactService.payloadWithinExcelLimit(100f), PAYLOAD_LIMIT_MESSAGE);
    }

    @Test
    void shouldGeneratePdfIfPayloadSizeWithinLimit() {
        assertTrue(artefactService.payloadWithinPdfLimit(59f), PAYLOAD_LIMIT_MESSAGE);
    }

    @Test
    void shouldGeneratePdfIfNoPayloadSize() {
        assertTrue(artefactService.payloadWithinPdfLimit(null), PAYLOAD_LIMIT_MESSAGE);
    }

    @Test
    void shouldNotGeneratePdfIfPayloadSizeOverLimit() {
        assertFalse(artefactService.payloadWithinPdfLimit(60f), PAYLOAD_LIMIT_MESSAGE);
    }

}
