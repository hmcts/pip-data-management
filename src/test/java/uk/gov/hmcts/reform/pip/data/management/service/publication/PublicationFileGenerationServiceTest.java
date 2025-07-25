package uk.gov.hmcts.reform.pip.data.management.service.publication;

import org.junit.jupiter.api.Test;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.pip.model.publication.Language;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles(profiles = "test")
class PublicationFileGenerationServiceTest {
    private PublicationFileGenerationService publicationFileGenerationService;

    @Test
    void testConvertSnlDataSourceName() {
        assertThat(publicationFileGenerationService.convertDataSourceName("SNL", Language.ENGLISH))
            .as("Provenance should be changed to ListAssist")
            .isEqualTo("ListAssist");
    }

    @Test
    void testConvertManualUploadDataSourceNameInEnglish() {
        assertThat(publicationFileGenerationService.convertDataSourceName("MANUAL_UPLOAD", Language.ENGLISH))
            .as("Provenance does not match")
            .isEqualTo("Manual Upload");
    }

    @Test
    void testConvertManualUploadDataSourceNameInWelsh() {
        assertThat(publicationFileGenerationService.convertDataSourceName("MANUAL_UPLOAD", Language.WELSH))
            .as("Provenance in Welsh does not match")
            .isEqualTo("Lanlwytho â Llaw");
    }
}
