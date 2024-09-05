package uk.gov.hmcts.reform.pip.data.management.controllers.tests.service.helpers;

import org.junit.jupiter.api.Test;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.pip.data.management.service.helpers.LanguageResourceHelper;
import uk.gov.hmcts.reform.pip.model.publication.Language;
import uk.gov.hmcts.reform.pip.model.publication.ListType;

import java.io.IOException;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("test")
class LanguageResourceHelperTest {
    @Test
    void shouldGetLanguageResourcesForListTypeWithoutParent() throws IOException {
        Map<String, Object> resources = LanguageResourceHelper.getLanguageResources(
            ListType.SSCS_DAILY_LIST, Language.ENGLISH
        );
        assertThat(resources)
            .isNotEmpty()
            .extracting(c -> c.get("title"))
            .isEqualTo("SSCS Daily List for ");
    }

    @Test
    void shouldGetLanguageResourcesForListTypeWithParent() throws IOException {
        Map<String, Object> resources = LanguageResourceHelper.getLanguageResources(
            ListType.SSCS_DAILY_LIST_ADDITIONAL_HEARINGS, Language.ENGLISH
        );
        assertThat(resources)
            .isNotEmpty()
            .extracting(c -> c.get("title"))
            .isEqualTo("SSCS Daily List - Additional Hearings for ");
    }

    @Test
    void shouldGetLanguageResourcesForWelshLanguage() throws IOException {
        Map<String, Object> resources = LanguageResourceHelper.getLanguageResources(
            ListType.SJP_PUBLIC_LIST, Language.WELSH
        );
        assertThat(resources)
            .isNotEmpty()
            .extracting(c -> c.get("serviceName"))
            .isEqualTo("Gwasanaeth Gwrandawiadau llys a thribiwnlys");
    }

    @Test
    void shouldReadResourcesFromPath() throws IOException {
        assertThat(LanguageResourceHelper.readResourcesFromPath("openJusticeStatement", Language.ENGLISH))
            .as("Result should not be empty")
            .isNotEmpty();
    }

    @Test
    void shouldReturnEmptyMapWhenReadingNonExistentResourcesFromPath() throws IOException {
        assertThat(LanguageResourceHelper.readResourcesFromPath("NonExistentResource", Language.ENGLISH))
            .as("Result should be empty")
            .isEmpty();
    }
}
