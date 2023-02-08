package uk.gov.hmcts.reform.pip.data.management.service.artefact;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.pip.data.management.database.ArtefactRepository;
import uk.gov.hmcts.reform.pip.data.management.errorhandling.exceptions.ArtefactNotFoundException;
import uk.gov.hmcts.reform.pip.data.management.models.publication.Artefact;
import uk.gov.hmcts.reform.pip.data.management.utils.CaseSearchTerm;

import java.util.List;
import java.util.UUID;

import static uk.gov.hmcts.reform.pip.model.LogBuilder.writeLog;

@Slf4j
@Service
public class ArtefactSearchService {
    private final ArtefactRepository artefactRepository;
    private final ArtefactService artefactService;

    @Autowired
    public ArtefactSearchService(ArtefactRepository artefactRepository,
                                 ArtefactService artefactService) {
        this.artefactRepository = artefactRepository;
        this.artefactService = artefactService;
    }

    /**
    * Get all relevant artefacts relating to a given location ID.
    *
    * @param searchValue - represents the location ID in question being searched for
    * @param userId      - represents the user ID of the user who is making the request
    * @return a list of all artefacts that fulfil the timing criteria, match the given location id and sensitivity
    *     associated with given verification status.
    */
    public List<Artefact> findAllByLocationId(String searchValue, UUID userId) {
        List<Artefact> artefacts = artefactRepository.findArtefactsByLocationId(searchValue);

        return artefacts.stream().filter(artefact -> artefactService.isAuthorised(artefact, userId)).toList();
    }

    /**
     * Get all artefacts for admin actions.
     *
     * @param locationId The location id to search for.
     * @param userId     represents the user ID of the user who is making the request
     * @param isAdmin    bool to check whether admin search is needed, if not will default to findAllByLocationId().
     * @return list of matching artefacts.
     */
    public List<Artefact> findAllByLocationIdAdmin(String locationId, UUID userId, boolean isAdmin) {
        log.info(writeLog("ADMIN - Searching for all artefacts with " + locationId));
        return isAdmin
            ? artefactRepository.findArtefactsByLocationIdAdmin(locationId) : findAllByLocationId(locationId, userId);
    }

    /**
     * Get all relevant Artefacts based on search values stored in the Artefact.
     *
     * @param searchTerm  the search term checking against, eg. CASE_ID or CASE_URN
     * @param searchValue the search value to look for
     * @param userId      represents the user ID of the user who is making the request
     * @return list of Artefacts
     */
    public List<Artefact> findAllBySearch(CaseSearchTerm searchTerm, String searchValue, UUID userId) {
        List<Artefact> artefacts;
        switch (searchTerm) {
            case CASE_ID, CASE_URN ->
                artefacts = artefactRepository.findArtefactBySearch(searchTerm.dbValue, searchValue);
            case CASE_NAME -> artefacts = artefactRepository.findArtefactByCaseName(searchValue);
            default -> throw new IllegalArgumentException(String.format("Invalid search term: %s", searchTerm));
        }

        artefacts = artefacts.stream().filter(artefact -> artefactService.isAuthorised(artefact, userId)).toList();

        if (artefacts.isEmpty()) {
            throw new ArtefactNotFoundException(String.format("No Artefacts found with for %s with the value: %s",
                                                              searchTerm, searchValue
            ));
        }
        return artefacts;
    }
}
