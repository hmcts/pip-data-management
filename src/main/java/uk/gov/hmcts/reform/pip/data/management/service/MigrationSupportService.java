package uk.gov.hmcts.reform.pip.data.management.service;

import lombok.AllArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.pip.data.management.database.ArtefactRepository;
import uk.gov.hmcts.reform.pip.data.management.database.ArtefactSearchRepository;
import uk.gov.hmcts.reform.pip.data.management.errorhandling.exceptions.InvalidDateRangeException;
import uk.gov.hmcts.reform.pip.data.management.models.publication.Artefact;
import uk.gov.hmcts.reform.pip.data.management.models.publication.ArtefactSearch;
import uk.gov.hmcts.reform.pip.data.management.models.publication.PublicationWithRelatedData;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@AllArgsConstructor
public class MigrationSupportService {
    // Keeps the number of bind parameters in each IN clause well below the Postgres limit
    private static final int ARTEFACT_ID_BATCH_SIZE = 1000;
    private static final Sort ARTEFACT_SORT = Sort.by("lastReceivedDate", "artefactId");

    private final ArtefactRepository artefactRepository;
    private final ArtefactSearchRepository artefactSearchRepository;

    /**
     * Retrieve all publications, along with their artefact search data, optionally filtered by last received date.
     *
     * @param lastReceivedDateFrom Only include publications last received on or after this date (inclusive).
     *                             Ignored if null.
     * @param lastReceivedDateTo Only include publications last received on or before this date (inclusive).
     *                           Ignored if null.
     * @return The matching publications with their related data.
     */
    public List<PublicationWithRelatedData> getAllPublications(LocalDate lastReceivedDateFrom,
                                                               LocalDate lastReceivedDateTo) {
        List<Artefact> artefacts = getAllArtefacts(lastReceivedDateFrom, lastReceivedDateTo);
        List<UUID> artefactIds = artefacts.stream().map(Artefact::getArtefactId).toList();

        Map<UUID, List<ArtefactSearch>> searchDataByArtefact = new HashMap<>();
        for (int i = 0; i < artefactIds.size(); i += ARTEFACT_ID_BATCH_SIZE) {
            List<UUID> batch = artefactIds.subList(i, Math.min(i + ARTEFACT_ID_BATCH_SIZE, artefactIds.size()));
            artefactSearchRepository.findByArtefactIdIn(batch).forEach(searchData ->
                searchDataByArtefact.computeIfAbsent(searchData.getArtefactId(), id -> new ArrayList<>())
                    .add(searchData)
            );
        }

        return artefacts.stream()
            .map(artefact -> new PublicationWithRelatedData(
                artefact,
                searchDataByArtefact.getOrDefault(artefact.getArtefactId(), List.of())
            ))
            .toList();
    }

    /**
     * Retrieve all artefacts, optionally filtered by last received date.
     *
     * @param lastReceivedDateFrom Only include artefacts last received on or after this date (inclusive).
     *                             Ignored if null.
     * @param lastReceivedDateTo Only include artefacts last received on or before this date (inclusive).
     *                           Ignored if null.
     * @return The matching artefacts.
     */
    public List<Artefact> getAllArtefacts(LocalDate lastReceivedDateFrom, LocalDate lastReceivedDateTo) {
        if (lastReceivedDateFrom != null && lastReceivedDateTo != null
            && lastReceivedDateFrom.isAfter(lastReceivedDateTo)) {
            throw new InvalidDateRangeException("lastReceivedDateFrom must not be after lastReceivedDateTo");
        }
        return findArtefacts(lastReceivedDateFrom, lastReceivedDateTo);
    }

    private List<Artefact> findArtefacts(LocalDate lastReceivedDateFrom, LocalDate lastReceivedDateTo) {
        LocalDateTime from = lastReceivedDateFrom == null ? null : lastReceivedDateFrom.atStartOfDay();
        // Exclusive upper bound of the following day so the whole of lastReceivedDateTo is included
        LocalDateTime toExclusive = lastReceivedDateTo == null ? null : lastReceivedDateTo.plusDays(1).atStartOfDay();

        if (from != null && toExclusive != null) {
            return artefactRepository.findAllByLastReceivedDateGreaterThanEqualAndLastReceivedDateLessThan(
                from, toExclusive, ARTEFACT_SORT
            );
        }
        if (from != null) {
            return artefactRepository.findAllByLastReceivedDateGreaterThanEqual(from, ARTEFACT_SORT);
        }
        if (toExclusive != null) {
            return artefactRepository.findAllByLastReceivedDateLessThan(toExclusive, ARTEFACT_SORT);
        }
        return artefactRepository.findAll(ARTEFACT_SORT);
    }
}
