package uk.gov.hmcts.reform.pip.data.management.service.publication;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.reform.pip.data.management.database.ArtefactArchivedRepository;
import uk.gov.hmcts.reform.pip.data.management.database.ArtefactRepository;
import uk.gov.hmcts.reform.pip.data.management.database.AzureArtefactBlobService;
import uk.gov.hmcts.reform.pip.data.management.database.LocationRepository;
import uk.gov.hmcts.reform.pip.data.management.errorhandling.exceptions.ArtefactNotFoundException;
import uk.gov.hmcts.reform.pip.data.management.helpers.ArtefactHelper;
import uk.gov.hmcts.reform.pip.data.management.helpers.NoMatchArtefactHelper;
import uk.gov.hmcts.reform.pip.data.management.models.location.Location;
import uk.gov.hmcts.reform.pip.data.management.models.publication.Artefact;
import uk.gov.hmcts.reform.pip.data.management.models.publication.ArtefactArchived;
import uk.gov.hmcts.reform.pip.data.management.service.AccountManagementService;
import uk.gov.hmcts.reform.pip.data.management.service.SystemAdminNotificationService;
import uk.gov.hmcts.reform.pip.model.account.PiUser;
import uk.gov.hmcts.reform.pip.model.enums.UserActions;
import uk.gov.hmcts.reform.pip.model.system.admin.ActionResult;
import uk.gov.hmcts.reform.pip.model.system.admin.ChangeType;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static uk.gov.hmcts.reform.pip.model.LogBuilder.writeLog;

@Slf4j
@Service
public class PublicationRemovalService {

    private final ArtefactRepository artefactRepository;
    private final LocationRepository locationRepository;
    private final ArtefactArchivedRepository artefactArchivedRepository;
    private final PublicationFileManagementService publicationFileManagementService;
    private final AzureArtefactBlobService azureArtefactBlobService;
    private final AccountManagementService accountManagementService;
    private final SystemAdminNotificationService systemAdminNotificationService;

    public PublicationRemovalService(ArtefactRepository artefactRepository, LocationRepository locationRepository,
                                     PublicationFileManagementService publicationFileManagementService,
                                     AzureArtefactBlobService azureArtefactBlobService,
                                     AccountManagementService accountManagementService,
                                     SystemAdminNotificationService systemAdminNotificationService,
                                     ArtefactArchivedRepository artefactArchivedRepository) {
        this.artefactRepository = artefactRepository;
        this.locationRepository = locationRepository;
        this.publicationFileManagementService = publicationFileManagementService;
        this.azureArtefactBlobService = azureArtefactBlobService;
        this.accountManagementService = accountManagementService;
        this.systemAdminNotificationService = systemAdminNotificationService;
        this.artefactArchivedRepository = artefactArchivedRepository;
    }

    /**
     * Method that handles the logic to archive an artefact and delete the stored blobs.
     *
     * @param artefactId The ID of the artefact to be deleted.
     * @param issuerId   The ID of the admin user who is attempting to delete the artefact.
     */
    @Transactional
    public void archiveArtefactById(String artefactId, String issuerId, Boolean isManuallyDeleted) {
        Artefact artefactToArchive = artefactRepository.findArtefactByArtefactId(artefactId)
            .orElseThrow(() -> new ArtefactNotFoundException("No artefact found with the ID: " + artefactId));

        handleArtefactArchiving(artefactToArchive, isManuallyDeleted);
        if (!NoMatchArtefactHelper.isNoMatchLocationId(artefactToArchive.getLocationId())) {
            accountManagementService.sendDeletedArtefactForThirdParties(artefactToArchive);
        }
        log.info(writeLog(String.format("Artefact archived by %s, with artefact id: %s", issuerId, artefactId)));
    }

    /**
     * Archive expired artefacts from the database, Artefact and Publications Azure storage.
     */
    @Transactional
    public void archiveExpiredArtefacts() {
        LocalDateTime searchDateTime = LocalDateTime.now();
        List<Artefact> outdatedArtefacts = artefactRepository.findOutdatedArtefacts(searchDateTime);
        outdatedArtefacts.forEach(artefact -> handleArtefactArchiving(artefact, Boolean.FALSE));

        log.info(writeLog(
            String.format("%s outdated artefacts found and archived for before %s",
                          outdatedArtefacts.size(), searchDateTime)
        ));
    }

    /**
     * Delete all data stored in the blobstore for an artefact.
     *
     * @param artefact The artefact requiring blob deletion.
     */
    private void deleteDataFromBlobStore(Artefact artefact) {
        // Delete the payload/flat file from the publications store
        azureArtefactBlobService.deleteBlob(ArtefactHelper.getUuidFromUrl(artefact.getPayload()));

        // Delete the generated files for the publications if it's not a flat file
        if (artefact.getIsFlatFile().equals(Boolean.FALSE)
            && !NoMatchArtefactHelper.isNoMatchLocationId(artefact.getLocationId())) {
            publicationFileManagementService.deleteFiles(artefact.getArtefactId(), artefact.getListType(),
                                                         artefact.getLanguage());
        }
    }

    /**
     * Attempts to delete an artefact along with the stored blobs.
     *
     * @param artefactId The ID of the artefact to be deleted.
     * @param issuerId   The ID of the admin user who is attempting to delete the artefact.
     */
    public void deleteArtefactById(String artefactId, String issuerId) {
        Artefact artefactToDelete = artefactRepository.findArtefactByArtefactId(artefactId)
            .orElseThrow(() -> new ArtefactNotFoundException("No artefact found with the ID: " + artefactId));

        handleArtefactDeletion(artefactToDelete);
        log.info(writeLog(issuerId, UserActions.REMOVE, artefactId));
    }

    public void deleteArtefactByLocation(List<Artefact> artefactsToDelete, Integer locationId, String userId)
        throws JsonProcessingException {
        artefactsToDelete.forEach(artefact -> {
            handleArtefactDeletion(artefact);
            log.info(writeLog(
                String.format("Artefact deleted by %s, with artefact id: %s",
                              userId, artefact.getArtefactId())
            ));
        });
        Optional<Location> location = locationRepository.getLocationByLocationId(locationId);

        PiUser userInfo = accountManagementService.getUserById(userId);
        systemAdminNotificationService.sendEmailNotification(
            userInfo.getEmail(), ActionResult.SUCCEEDED,
            String.format("Total %s artefact(s) for location %s", artefactsToDelete.size(), location.isPresent()
                    ? location.get().getName() : ""),
            ChangeType.DELETE_LOCATION_ARTEFACT
        );
    }

    public void deleteArtefacts(List<Artefact> artefacts) {
        artefacts.forEach(this::handleArtefactDeletion);
    }

    public void handleArtefactDeletion(Artefact artefact) {
        deleteDataFromBlobStore(artefact);
        artefactRepository.delete(artefact);
        if (!NoMatchArtefactHelper.isNoMatchLocationId(artefact.getLocationId())) {
            accountManagementService.sendDeletedArtefactForThirdParties(artefact);
        }
    }

    private void handleArtefactArchiving(Artefact artefact, Boolean isManuallyDeleted) {
        deleteDataFromBlobStore(artefact);
        ArtefactArchived artefactArchived = new ArtefactArchived(artefact, isManuallyDeleted);
        artefactArchivedRepository.save(artefactArchived);
        artefactRepository.delete(artefact);
    }
}
