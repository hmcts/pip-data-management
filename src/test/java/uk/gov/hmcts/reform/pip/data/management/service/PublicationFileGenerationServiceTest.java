package uk.gov.hmcts.reform.pip.data.management.service;

import org.junit.jupiter.api.Test;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles(profiles = "test")
class PublicationFileGenerationServiceTest {
    private PublicationFileGenerationService publicationFileGenerationService;

    @Test
    void testConvertDataSourceName() {
        assertThat(publicationFileGenerationService.convertDataSourceName("SNL"))
            .as("Provenance should be changed to ListAssist")
            .isEqualTo("ListAssist");
    }

    @Test
    void testDoNotConvertDataSourceName() {
        assertThat(publicationFileGenerationService.convertDataSourceName("MANUAL_UPLOAD"))
            .as("Provenance should be changed to title case")
            .isEqualTo("Manual Upload");
    }
}
