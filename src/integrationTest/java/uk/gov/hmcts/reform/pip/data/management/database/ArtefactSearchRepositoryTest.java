package uk.gov.hmcts.reform.pip.data.management.database;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.pip.data.management.models.publication.ArtefactSearch;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("integration-jpa")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@DataJpaTest
class ArtefactSearchRepositoryTest {

    @Autowired
    private ArtefactSearchRepository artefactSearchRepository;

    @Test
    @DisplayName("should load repository bean")
    void shouldLoadRepositoryBean() {
        assertThat(artefactSearchRepository).isNotNull();
    }

    @Test
    @DisplayName("records should be found by given artefactId")
    void shouldFindByArtefactId() {
        UUID artefactId = UUID.randomUUID();
        UUID otherArtefactId = UUID.randomUUID();

        ArtefactSearch match = createArtefactSearch(artefactId);
        ArtefactSearch match2 = createArtefactSearch(artefactId);
        ArtefactSearch nonMatch = createArtefactSearch(otherArtefactId);

        artefactSearchRepository.saveAll(List.of(match, match2, nonMatch));

        List<ArtefactSearch> results = artefactSearchRepository.findByArtefactId(artefactId);

        assertThat(!results.isEmpty());
        assertThat(results)
            .hasSize(2)
            .extracting(ArtefactSearch::getArtefactId)
            .containsOnly(artefactId);
    }

    @Test
    @DisplayName("records should be deleted by given artefactId")
    void shouldDeleteByArtefactId() {
        UUID artefactIdToDelete = UUID.randomUUID();
        UUID artefactIdToKeep = UUID.randomUUID();

        ArtefactSearch delete = createArtefactSearch(artefactIdToDelete);
        ArtefactSearch delete2 = createArtefactSearch(artefactIdToDelete);
        ArtefactSearch keep = createArtefactSearch(artefactIdToKeep);

        artefactSearchRepository.saveAll(List.of(delete, delete2, keep));

        artefactSearchRepository.deleteByArtefactId(artefactIdToDelete);

        assertThat(artefactSearchRepository.findByArtefactId(artefactIdToDelete)).isEmpty();
        assertThat(artefactSearchRepository.findByArtefactId(artefactIdToKeep)).isNotEmpty();
    }

    private ArtefactSearch createArtefactSearch(UUID artefactId) {
        ArtefactSearch artefactSearch = new ArtefactSearch();

        artefactSearch.setArtefactId(artefactId);

        return artefactSearch;
    }
}
