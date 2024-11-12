package uk.gov.hmcts.reform.pip.data.management.service;

import org.junit.jupiter.api.Test;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles(profiles = "test")
class PublicationFileGenerationServiceTest {
    private PublicationFileGenerationService publicationFileGenerationService;

    @Test
    void testMaskDataSourceName() {
        assertThat(publicationFileGenerationService.maskDataSourceName("SNL"))
            .as("Provenance should be changed to ListAssist")
            .isEqualTo("ListAssist");
    }

    @Test
    void testDoNotMaskDataSourceName() {
        assertThat(publicationFileGenerationService.maskDataSourceName("MANUAL_UPLOAD"))
            .as("Provenance should not be changed")
            .isEqualTo("MANUAL_UPLOAD");
    }
}
