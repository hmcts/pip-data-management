package uk.gov.hmcts.reform.pip.data.management.service.artefact;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.reform.pip.data.management.database.ArtefactRepository;
import uk.gov.hmcts.reform.pip.data.management.database.AzureBlobService;
import uk.gov.hmcts.reform.pip.data.management.errorhandling.exceptions.ArtefactNotFoundException;
import uk.gov.hmcts.reform.pip.data.management.models.publication.Artefact;
import uk.gov.hmcts.reform.pip.data.management.models.publication.ListType;
import uk.gov.hmcts.reform.pip.data.management.service.SubscriptionManagementService;
import uk.gov.hmcts.reform.pip.model.enums.UserActions;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static uk.gov.hmcts.reform.pip.model.LogBuilder.writeLog;

@Slf4j
@Service
public class ArtefactArchiveService {

    private final ArtefactRepository artefactRepository;
    private final AzureBlobService azureBlobService;
    private final SubscriptionManagementService subscriptionManagementService;

    public ArtefactArchiveService(ArtefactRepository artefactRepository, AzureBlobService azureBlobService,
                                  SubscriptionManagementService subscriptionManagementService) {
        this.artefactRepository = artefactRepository;
        this.azureBlobService = azureBlobService;
        this.subscriptionManagementService = subscriptionManagementService;
    }

    /**
     * Method that handles the logic to archive an artefact and delete the stored blobs.
     *
     * @param artefactId The ID of the artefact to be deleted.
     * @param issuerId   The ID of the admin user who is attempting to delete the artefact.
     */
    @Transactional
    public void archiveArtefactById(String artefactId, String issuerId) {
        Optional<Artefact> artefactToArchive = artefactRepository.findArtefactByArtefactId(artefactId);

        if (artefactToArchive.isPresent()) {
            deleteAllPublicationBlobData(artefactToArchive.get());
            artefactRepository.archiveArtefact(artefactId);
            log.info(String.format("Artefact archived by %s, with artefact id: %s", issuerId, artefactId));
            triggerThirdPartyArtefactDeleted(artefactToArchive.get());
        } else {
            throw new ArtefactNotFoundException("No artefact found with the ID: " + artefactId);
        }
    }

    /**
     * Archive expired artefacts from the database, Artefact and Publications Azure storage.
     */
    @Transactional
    public void archiveExpiredArtefacts() {
        LocalDateTime searchDateTime = LocalDateTime.now();
        List<Artefact> outdatedArtefacts = artefactRepository.findOutdatedArtefacts(searchDateTime);

        outdatedArtefacts.forEach(artefact -> {
            artefactRepository.archiveArtefact(artefact.getArtefactId().toString());
            deleteAllPublicationBlobData(artefact);
        });

        log.info(writeLog(String.format("%s outdated artefacts found and archived for before %s",
                                        outdatedArtefacts.size(), searchDateTime
        )));
    }

    /**
     * Delete all data stored in the blobstore for an artefact.
     *
     * @param artefact The artefact requiring blob deletion.
     */
    private void deleteAllPublicationBlobData(Artefact artefact) {
        // Delete the payload/flat file from the publications store
        azureBlobService.deleteBlob(getUuidFromUrl(artefact.getPayload()));

        // Try to delete the generated files for the publications if it's not a flat file
        if (!artefact.getIsFlatFile()) {
            try {
                azureBlobService.deletePublicationBlob(artefact.getArtefactId() + ".pdf");

                // If it's an SJP list the xlsx file also needs to be deleted
                if (ListType.SJP_PUBLIC_LIST.equals(artefact.getListType())
                    || ListType.SJP_PRESS_LIST.equals(artefact.getListType())) {
                    azureBlobService.deletePublicationBlob(artefact.getArtefactId() + ".xlsx");
                }
            } catch (Exception ex) {
                log.info("Failed to delete the generated publication file. Message: " + ex.getMessage());
            }
        }
    }

    /**
     * Attempts to delete an artefact along with the stored blobs.
     *
     * @param artefactId The ID of the artefact to be deleted.
     * @param issuerId   The ID of the admin user who is attempting to delete the artefact.
     */
    public void deleteArtefactById(String artefactId, String issuerId) {
        Optional<Artefact> artefactToDelete = artefactRepository.findArtefactByArtefactId(artefactId);
        if (artefactToDelete.isPresent()) {
            deleteAllPublicationBlobData(artefactToDelete.get());
            artefactRepository.delete(artefactToDelete.get());
            log.info(writeLog(issuerId, UserActions.REMOVE, artefactId));
            triggerThirdPartyArtefactDeleted(artefactToDelete.get());
        } else {
            throw new ArtefactNotFoundException("No artefact found with the ID: " + artefactId);
        }
    }

    /**
     * Triggers subscription management to handle deleted artefact to third party subscribers.
     *
     * @param deletedArtefact deleted artefact to notify of.
     */
    private void triggerThirdPartyArtefactDeleted(Artefact deletedArtefact) {
        log.info(writeLog(subscriptionManagementService.sendDeletedArtefactForThirdParties(deletedArtefact)));
    }

    private String getUuidFromUrl(String payloadUrl) {
        return payloadUrl.substring(payloadUrl.lastIndexOf('/') + 1);
    }

}
