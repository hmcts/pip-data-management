package uk.gov.hmcts.reform.pip.data.management.service.publication;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.pip.data.management.database.ArtefactRepository;
import uk.gov.hmcts.reform.pip.data.management.database.ListSearchConfigRepository;
import uk.gov.hmcts.reform.pip.data.management.errorhandling.exceptions.ArtefactNotFoundException;
import uk.gov.hmcts.reform.pip.data.management.errorhandling.exceptions.CreateListSearchConfigConflictException;
import uk.gov.hmcts.reform.pip.data.management.errorhandling.exceptions.NotFoundException;
import uk.gov.hmcts.reform.pip.data.management.models.publication.Artefact;
import uk.gov.hmcts.reform.pip.data.management.models.publication.ListSearchConfig;
import uk.gov.hmcts.reform.pip.data.management.utils.CaseSearchTerm;
import uk.gov.hmcts.reform.pip.model.enums.UserActions;
import uk.gov.hmcts.reform.pip.model.publication.ListType;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static uk.gov.hmcts.reform.pip.model.LogBuilder.writeLog;

@Slf4j
@Service
public class PublicationSearchService {
    private final ArtefactRepository artefactRepository;
    private final ListSearchConfigRepository listSearchConfigRepository;
    private final PublicationRetrievalService publicationRetrievalService;

    @Autowired
    public PublicationSearchService(ArtefactRepository artefactRepository,
                                    ListSearchConfigRepository listSearchConfigRepository,
                                    PublicationRetrievalService publicationRetrievalService) {
        this.artefactRepository = artefactRepository;
        this.listSearchConfigRepository = listSearchConfigRepository;
        this.publicationRetrievalService = publicationRetrievalService;
    }

    /**
     * Handles request to add list search config.
     *
     * @param listSearchConfig The list search config object
     * @param actioningUserId The userId who is performing this action
     */
    public void createListSearchConfig(ListSearchConfig listSearchConfig, UUID actioningUserId) {
        try {
            listSearchConfigRepository.save(listSearchConfig);
            log.info(writeLog(actioningUserId, UserActions.ADD_LIST_SEARCH_CONFIG,
                              listSearchConfig.getListType().toString()));
        } catch (DataIntegrityViolationException e) {
            String errorMessage = String.format(
                "List search config for list type %s already exists",
                listSearchConfig.getListType());
            log.error(writeLog(errorMessage));
            throw new CreateListSearchConfigConflictException(errorMessage);
        }
    }

    /**
     * Handles request to update list search config.
     * @param id The list search config ID to update
     * @param listSearchConfig The list search config object
     * @param actioningUserId The userId who is performing this action
     */
    public void updateListSearchConfig(String id, ListSearchConfig listSearchConfig, UUID actioningUserId) {
        listSearchConfigRepository.findById(UUID.fromString(id))
            .orElseThrow(() -> new NotFoundException(
                String.format("List search config for ID %s does not exist", id)
            ));

        log.info(writeLog(actioningUserId, UserActions.UPDATE_LIST_SEARCH_CONFIG,
                          listSearchConfig.getListType().toString()));

        listSearchConfigRepository.save(listSearchConfig);
    }

    /**
     * Handles request to delete list search config.
     * @param id The list search config ID to delete
     * @param actioningUserId The userId who is performing this action
     */
    public void deleteListSearchConfig(String id, UUID actioningUserId) {
        UUID listSearchConfigId = UUID.fromString(id);
        listSearchConfigRepository.findById(listSearchConfigId)
            .orElseThrow(() -> new NotFoundException(
                String.format("List search config for ID %s does not exist", id)
            ));
        listSearchConfigRepository.deleteById(listSearchConfigId);

        log.info(writeLog(actioningUserId, UserActions.DELETE_LIST_SEARCH_CONFIG, id));
    }

    /**
     * Handles request to find list search config by list type.
     * @param listType The list type to search for
     * @return the list search config for the given list type
     */
    public ListSearchConfig findListSearchConfigByListType(ListType listType) {
        return listSearchConfigRepository.findByListType(listType)
            .orElseThrow(() -> new NotFoundException(
                String.format("List search config for list type %s does not exist", listType)
            ));
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
        LocalDateTime currDate = LocalDateTime.now();
        List<Artefact> artefacts = artefactRepository.findArtefactsByLocationId(searchValue, currDate);

        return artefacts.stream()
            .filter(artefact -> publicationRetrievalService.isAuthorised(artefact, userId))
            .toList();
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
        LocalDateTime currDate = LocalDateTime.now();
        return isAdmin
            ? artefactRepository.findArtefactsByLocationIdAdmin(locationId, currDate)
                : findAllByLocationId(locationId, userId);
    }

    /**
     * Get all relevant Artefacts based on search values stored in the Artefact.
     *
     * @param searchTerm  the search term checking against, e.g. CASE_ID or CASE_URN
     * @param searchValue the search value to look for
     * @param userId      represents the user ID of the user who is making the request
     * @return list of Artefacts
     */
    public List<Artefact> findAllBySearch(CaseSearchTerm searchTerm, String searchValue, UUID userId) {
        LocalDateTime currDate = LocalDateTime.now();
        List<Artefact> artefacts;

        switch (searchTerm) {
            case CASE_ID, CASE_URN ->
                artefacts = artefactRepository.findArtefactBySearch(searchTerm.dbValue, searchValue, currDate);
            case CASE_NAME -> artefacts = artefactRepository.findArtefactByCaseName(searchValue, currDate);
            default -> throw new IllegalArgumentException(String.format("Invalid search term: %s", searchTerm));
        }

        artefacts = artefacts.stream()
            .filter(artefact -> publicationRetrievalService.isAuthorised(artefact, userId))
            .toList();

        if (artefacts.isEmpty()) {
            throw new ArtefactNotFoundException(String.format("No Artefacts found with for %s with the value: %s",
                                                              searchTerm, searchValue
            ));
        }
        return artefacts;
    }
}
