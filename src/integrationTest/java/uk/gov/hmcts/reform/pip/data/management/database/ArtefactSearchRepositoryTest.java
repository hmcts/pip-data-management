package uk.gov.hmcts.reform.pip.data.management.database;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.pip.data.management.database.ArtefactSearchRepository.CaseSearchResult;
import uk.gov.hmcts.reform.pip.data.management.models.publication.Artefact;
import uk.gov.hmcts.reform.pip.data.management.models.publication.ArtefactSearch;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("integration-jpa")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@DataJpaTest
class ArtefactSearchRepositoryTest {
    private static final ArtefactSearch ARTEFACT_SEARCH = new ArtefactSearch();
    private static final ArtefactSearch ARTEFACT_SEARCH2 = new ArtefactSearch();
    private static final ArtefactSearch ARTEFACT_SEARCH3 = new ArtefactSearch();
    private static final Artefact ARTEFACT = new Artefact();
    private static final Artefact ARTEFACT2 = new Artefact();
    private static final Artefact ARTEFACT3 = new Artefact();
    private static final String CASE_NUMBER = "caseNumber";
    private static final String CASE_NAME = "caseName";
    private static final String CASE_NAME_SUBSET = "case";

    @Autowired
    private ArtefactSearchRepository artefactSearchRepository;

    @Autowired
    private ArtefactRepository artefactRepository;

    @BeforeAll
    void setup() {
        // ARTEFACT: not yet active (display_from in future)
        ARTEFACT.setDisplayFrom(LocalDateTime.now().plusDays(1));
        ARTEFACT.setDisplayTo(LocalDateTime.now().plusDays(2));

        // ARTEFACT2: currently active
        ARTEFACT2.setDisplayFrom(LocalDateTime.now().minusDays(1));
        ARTEFACT2.setDisplayTo(LocalDateTime.now().plusDays(1));

        // ARTEFACT3: expired (display_to in the past)
        ARTEFACT3.setDisplayFrom(LocalDateTime.now().minusDays(2));
        ARTEFACT3.setDisplayTo(LocalDateTime.now().minusDays(1));

        // Save artefacts first to obtain DB-generated artefact IDs
        List<Artefact> savedArtefacts = artefactRepository.saveAll(List.of(ARTEFACT, ARTEFACT2, ARTEFACT3));

        ARTEFACT_SEARCH.setArtefactId(savedArtefacts.get(0).getArtefactId());
        ARTEFACT_SEARCH.setCaseNumber(CASE_NUMBER);
        ARTEFACT_SEARCH.setCaseName(CASE_NAME);

        ARTEFACT_SEARCH2.setArtefactId(savedArtefacts.get(1).getArtefactId());
        ARTEFACT_SEARCH2.setCaseNumber(CASE_NUMBER);
        ARTEFACT_SEARCH2.setCaseName(CASE_NAME);

        ARTEFACT_SEARCH3.setArtefactId(savedArtefacts.get(2).getArtefactId());
        ARTEFACT_SEARCH3.setCaseNumber(CASE_NUMBER);
        ARTEFACT_SEARCH3.setCaseName(CASE_NAME);

        artefactSearchRepository.saveAll(List.of(ARTEFACT_SEARCH, ARTEFACT_SEARCH2, ARTEFACT_SEARCH3));
    }

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

    @Test
    void shouldFindByCaseNumberIgnoreCase() {
        List<CaseSearchResult> results = artefactSearchRepository.findByCaseNumberIgnoreCase(
            CASE_NUMBER, LocalDateTime.now()
        );

        assertThat(results)
            .hasSize(1);
        assertThat(results.get(0).getCaseNumber())
            .isEqualTo(CASE_NUMBER);
    }

    @Test
    void shouldFindByCaseNumberIgnoreCaseDeduplicates() {
        Artefact activeArtefact1 = new Artefact();
        activeArtefact1.setDisplayFrom(LocalDateTime.now().minusDays(1));
        activeArtefact1.setDisplayTo(LocalDateTime.now().plusDays(1));

        Artefact activeArtefact2 = new Artefact();
        activeArtefact2.setDisplayFrom(LocalDateTime.now().minusDays(1));
        activeArtefact2.setDisplayTo(LocalDateTime.now().plusDays(1));

        List<Artefact> savedArtefacts = artefactRepository.saveAll(List.of(activeArtefact1, activeArtefact2));

        ArtefactSearch search1 = new ArtefactSearch();
        search1.setArtefactId(savedArtefacts.get(0).getArtefactId());
        search1.setCaseNumber(CASE_NUMBER);
        search1.setCaseName(CASE_NAME);

        ArtefactSearch search2 = new ArtefactSearch();
        search2.setArtefactId(savedArtefacts.get(1).getArtefactId());
        search2.setCaseNumber(CASE_NUMBER);
        search2.setCaseName(CASE_NAME);

        artefactSearchRepository.saveAll(List.of(search1, search2));

        List<CaseSearchResult> results = artefactSearchRepository.findByCaseNumberIgnoreCase(
            CASE_NUMBER, LocalDateTime.now()
        );

        assertThat(results)
            .hasSize(1);
        assertThat(results.get(0).getCaseNumber())
            .isEqualTo(CASE_NUMBER);
        assertThat(results.get(0).getCaseName())
            .isEqualTo(CASE_NAME);
    }

    @Test
    void shouldFindByCaseNameIgnoreCase() {
        List<CaseSearchResult> results = artefactSearchRepository.findByCaseNameIgnoreCase(
            CASE_NAME_SUBSET, LocalDateTime.now()
        );
        assertThat(results).isEmpty();

        results = artefactSearchRepository.findByCaseNameIgnoreCase(CASE_NAME, LocalDateTime.now());
        assertThat(results)
            .hasSize(1);
        assertThat(results.get(0).getCaseName())
            .isEqualTo(CASE_NAME);
    }

    @Test
    void shouldFindByCaseNameIgnoreCaseDeduplicates() {
        Artefact activeArtefact1 = new Artefact();
        activeArtefact1.setDisplayFrom(LocalDateTime.now().minusDays(1));
        activeArtefact1.setDisplayTo(LocalDateTime.now().plusDays(1));

        Artefact activeArtefact2 = new Artefact();
        activeArtefact2.setDisplayFrom(LocalDateTime.now().minusDays(1));
        activeArtefact2.setDisplayTo(LocalDateTime.now().plusDays(1));

        List<Artefact> savedArtefacts = artefactRepository.saveAll(List.of(activeArtefact1, activeArtefact2));

        ArtefactSearch search1 = new ArtefactSearch();
        search1.setArtefactId(savedArtefacts.get(0).getArtefactId());
        search1.setCaseNumber(CASE_NUMBER);
        search1.setCaseName(CASE_NAME);

        ArtefactSearch search2 = new ArtefactSearch();
        search2.setArtefactId(savedArtefacts.get(1).getArtefactId());
        search2.setCaseNumber(CASE_NUMBER);
        search2.setCaseName(CASE_NAME);

        artefactSearchRepository.saveAll(List.of(search1, search2));

        List<CaseSearchResult> results = artefactSearchRepository.findByCaseNameIgnoreCase(
            CASE_NAME, LocalDateTime.now()
        );

        assertThat(results)
            .hasSize(1);
        assertThat(results.get(0).getCaseName())
            .isEqualTo(CASE_NAME);
    }

    @Test
    void shouldFindByCaseNameFuzzySearchIgnoreCase() {
        List<CaseSearchResult> results = artefactSearchRepository.findTop50ByCaseNameContainingIgnoreCase(
            CASE_NAME_SUBSET, LocalDateTime.now()
        );

        assertThat(results)
            .hasSize(1);
        assertThat(results.get(0).getCaseName())
            .isEqualTo(CASE_NAME);
    }

    @Test
    void shouldFindByCaseNameFuzzySearchIgnoreCaseDeduplicates() {
        Artefact activeArtefact1 = new Artefact();
        activeArtefact1.setDisplayFrom(LocalDateTime.now().minusDays(1));
        activeArtefact1.setDisplayTo(LocalDateTime.now().plusDays(1));

        Artefact activeArtefact2 = new Artefact();
        activeArtefact2.setDisplayFrom(LocalDateTime.now().minusDays(1));
        activeArtefact2.setDisplayTo(LocalDateTime.now().plusDays(1));

        List<Artefact> savedArtefacts = artefactRepository.saveAll(List.of(activeArtefact1, activeArtefact2));

        ArtefactSearch search1 = new ArtefactSearch();
        search1.setArtefactId(savedArtefacts.get(0).getArtefactId());
        search1.setCaseNumber(CASE_NUMBER);
        search1.setCaseName(CASE_NAME);

        ArtefactSearch search2 = new ArtefactSearch();
        search2.setArtefactId(savedArtefacts.get(1).getArtefactId());
        search2.setCaseNumber(CASE_NUMBER);
        search2.setCaseName(CASE_NAME);

        artefactSearchRepository.saveAll(List.of(search1, search2));

        List<CaseSearchResult> results = artefactSearchRepository.findTop50ByCaseNameContainingIgnoreCase(
            CASE_NAME_SUBSET, LocalDateTime.now()
        );

        assertThat(results)
            .hasSize(1);
        assertThat(results.get(0).getCaseName())
            .isEqualTo(CASE_NAME);
    }

    private ArtefactSearch createArtefactSearch(UUID artefactId) {
        ArtefactSearch artefactSearch = new ArtefactSearch();

        artefactSearch.setArtefactId(artefactId);

        return artefactSearch;
    }
}
