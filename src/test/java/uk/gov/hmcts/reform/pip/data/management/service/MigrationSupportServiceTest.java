package uk.gov.hmcts.reform.pip.data.management.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;
import uk.gov.hmcts.reform.pip.data.management.database.ArtefactRepository;
import uk.gov.hmcts.reform.pip.data.management.database.ArtefactSearchRepository;
import uk.gov.hmcts.reform.pip.data.management.errorhandling.exceptions.InvalidDateRangeException;
import uk.gov.hmcts.reform.pip.data.management.models.publication.Artefact;
import uk.gov.hmcts.reform.pip.data.management.models.publication.ArtefactSearch;
import uk.gov.hmcts.reform.pip.data.management.models.publication.PublicationWithRelatedData;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MigrationSupportServiceTest {
    private static final LocalDate FROM = LocalDate.of(2024, 1, 10);
    private static final LocalDate TO = LocalDate.of(2024, 1, 20);
    private static final LocalDateTime FROM_START_OF_DAY = LocalDateTime.of(2024, 1, 10, 0, 0);
    private static final LocalDateTime DAY_AFTER_TO = LocalDateTime.of(2024, 1, 21, 0, 0);
    private static final Sort SORT = Sort.by("lastReceivedDate", "artefactId");

    @Mock
    private ArtefactRepository artefactRepository;

    @Mock
    private ArtefactSearchRepository artefactSearchRepository;

    @InjectMocks
    private MigrationSupportService migrationSupportService;

    private static Artefact artefact() {
        Artefact artefact = new Artefact();
        artefact.setArtefactId(UUID.randomUUID());
        return artefact;
    }

    @Test
    void testNoFiltersReturnsAllPublicationsWithSearchData() {
        Artefact withSearchData = artefact();
        Artefact withoutSearchData = artefact();
        ArtefactSearch search1 = new ArtefactSearch(UUID.randomUUID(), withSearchData.getArtefactId(), "1", "A");
        ArtefactSearch search2 = new ArtefactSearch(UUID.randomUUID(), withSearchData.getArtefactId(), "2", "B");

        when(artefactRepository.findAll(SORT)).thenReturn(List.of(withSearchData, withoutSearchData));
        when(artefactSearchRepository.findByArtefactIdIn(
            List.of(withSearchData.getArtefactId(), withoutSearchData.getArtefactId())
        )).thenReturn(List.of(search1, search2));

        List<PublicationWithRelatedData> result = migrationSupportService.getAllPublications(null, null);

        assertThat(result).containsExactly(
            new PublicationWithRelatedData(withSearchData, List.of(search1, search2)),
            new PublicationWithRelatedData(withoutSearchData, List.of())
        );
    }

    @Test
    void testBothFiltersQueriesBetweenDatesInclusive() {
        Artefact artefact = artefact();
        when(artefactRepository.findAllByLastReceivedDateGreaterThanEqualAndLastReceivedDateLessThan(
            FROM_START_OF_DAY, DAY_AFTER_TO, SORT
        )).thenReturn(List.of(artefact));

        assertThat(migrationSupportService.getAllPublications(FROM, TO))
            .extracting(PublicationWithRelatedData::getArtefact)
            .containsExactly(artefact);
    }

    @Test
    void testSameFromAndToDateIsAllowed() {
        when(artefactRepository.findAllByLastReceivedDateGreaterThanEqualAndLastReceivedDateLessThan(
            FROM_START_OF_DAY, LocalDateTime.of(2024, 1, 11, 0, 0), SORT
        )).thenReturn(List.of());

        assertThat(migrationSupportService.getAllPublications(FROM, FROM)).isEmpty();
    }

    @Test
    void testFromFilterOnly() {
        Artefact artefact = artefact();
        when(artefactRepository.findAllByLastReceivedDateGreaterThanEqual(FROM_START_OF_DAY, SORT))
            .thenReturn(List.of(artefact));

        assertThat(migrationSupportService.getAllPublications(FROM, null))
            .extracting(PublicationWithRelatedData::getArtefact)
            .containsExactly(artefact);
    }

    @Test
    void testToFilterOnly() {
        Artefact artefact = artefact();
        when(artefactRepository.findAllByLastReceivedDateLessThan(DAY_AFTER_TO, SORT))
            .thenReturn(List.of(artefact));

        assertThat(migrationSupportService.getAllPublications(null, TO))
            .extracting(PublicationWithRelatedData::getArtefact)
            .containsExactly(artefact);
    }

    @Test
    void testFromAfterToThrowsException() {
        assertThatThrownBy(() -> migrationSupportService.getAllPublications(TO, FROM))
            .isInstanceOf(InvalidDateRangeException.class)
            .hasMessage("lastReceivedDateFrom must not be after lastReceivedDateTo");

        verifyNoInteractions(artefactRepository, artefactSearchRepository);
    }

    @Test
    void testSearchDataFetchedInBatches() {
        List<Artefact> artefacts = IntStream.range(0, 2500).mapToObj(i -> artefact()).toList();
        when(artefactRepository.findAll(any(Sort.class))).thenReturn(artefacts);
        when(artefactSearchRepository.findByArtefactIdIn(anyList())).thenReturn(List.of());

        assertThat(migrationSupportService.getAllPublications(null, null)).hasSize(2500);
        verify(artefactSearchRepository, times(3)).findByArtefactIdIn(anyList());
    }

    @Test
    void testNoPublicationsDoesNotQuerySearchData() {
        when(artefactRepository.findAll(SORT)).thenReturn(List.of());

        assertThat(migrationSupportService.getAllPublications(null, null)).isEmpty();
        verifyNoInteractions(artefactSearchRepository);
    }

    @Test
    void testGetAllArtefactsDoesNotRetrieveSearchData() {
        Artefact artefact = artefact();
        when(artefactRepository.findAllByLastReceivedDateGreaterThanEqualAndLastReceivedDateLessThan(
            FROM_START_OF_DAY, DAY_AFTER_TO, SORT
        )).thenReturn(List.of(artefact));

        assertThat(migrationSupportService.getAllArtefacts(FROM, TO)).containsExactly(artefact);
        verifyNoInteractions(artefactSearchRepository);
    }

    @Test
    void testGetAllArtefactsFromAfterToThrowsException() {
        assertThatThrownBy(() -> migrationSupportService.getAllArtefacts(TO, FROM))
            .isInstanceOf(InvalidDateRangeException.class);
        verifyNoInteractions(artefactRepository);
    }
}
