package uk.gov.hmcts.reform.pip.data.management.service.publication;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.reform.pip.data.management.database.ArtefactRepository;
import uk.gov.hmcts.reform.pip.data.management.database.AzureArtefactBlobService;
import uk.gov.hmcts.reform.pip.data.management.database.LocationRepository;
import uk.gov.hmcts.reform.pip.data.management.errorhandling.exceptions.ArtefactNotFoundException;
import uk.gov.hmcts.reform.pip.data.management.helpers.ArtefactHelper;
import uk.gov.hmcts.reform.pip.data.management.helpers.NoMatchArtefactHelper;
import uk.gov.hmcts.reform.pip.data.management.models.publication.Artefact;
import uk.gov.hmcts.reform.pip.data.management.service.AccountManagementService;
import uk.gov.hmcts.reform.pip.data.management.service.location.LocationService;
import uk.gov.hmcts.reform.pip.data.management.service.PublicationManagementService;
import uk.gov.hmcts.reform.pip.data.management.service.PublicationServicesService;
import uk.gov.hmcts.reform.pip.model.enums.UserActions;

import java.time.LocalDateTime;
import java.util.List;

import static uk.gov.hmcts.reform.pip.model.LogBuilder.writeLog;

@Slf4j
@Service
public class PublicationDeleteService {

    private final ArtefactRepository artefactRepository;
    private final LocationRepository locationRepository;
    private final LocationService locationService;
    private final PublicationManagementService publicationManagementService;
    private final AzureArtefactBlobService azureArtefactBlobService;
    private final AccountManagementService accountManagementService;
    private final PublicationServicesService publicationServicesService;

    public PublicationDeleteService(ArtefactRepository artefactRepository, LocationRepository locationRepository,
                                    LocationService locationService,
                                    PublicationManagementService publicationManagementService,
                                    AzureArtefactBlobService azureArtefactBlobService,
                                    AccountManagementService accountManagementService,
                                    PublicationServicesService publicationServicesService) {
        this.artefactRepository = artefactRepository;
        this.locationRepository = locationRepository;
        this.locationService = locationService;
        this.publicationManagementService = publicationManagementService;
        this.azureArtefactBlobService = azureArtefactBlobService;
        this.accountManagementService = accountManagementService;
        this.publicationServicesService = publicationServicesService;
    }

    /**
     * Method that handles the logic to archive an artefact and delete the stored blobs.
     *
     * @param artefactId The ID of the artefact to be deleted.
     * @param issuerId   The ID of the admin user who is attempting to delete the artefact.
     */
    @Transactional
    public void archiveArtefactById(String artefactId, String issuerId) {
        Artefact artefactToArchive = artefactRepository.findArtefactByArtefactId(artefactId)
            .orElseThrow(() -> new ArtefactNotFoundException("No artefact found with the ID: " + artefactId));

        handleArtefactArchiving(artefactToArchive);
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
        outdatedArtefacts.forEach(this::handleArtefactArchiving);

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
            publicationManagementService.deleteFiles(artefact.getArtefactId(), artefact.getListType(),
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

    public void handleArtefactDeletion(Artefact artefact) {
        deleteDataFromBlobStore(artefact);
        artefactRepository.delete(artefact);
        if (!NoMatchArtefactHelper.isNoMatchLocationId(artefact.getLocationId())) {
            accountManagementService.sendDeletedArtefactForThirdParties(artefact);
        }
    }

    private void handleArtefactArchiving(Artefact artefact) {
        deleteDataFromBlobStore(artefact);
        artefactRepository.archiveArtefact(artefact.getArtefactId().toString());
    }
}
