package uk.gov.hmcts.reform.pip.data.management.service.artefact;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.reform.pip.data.management.database.ArtefactRepository;
import uk.gov.hmcts.reform.pip.data.management.database.AzureBlobService;
import uk.gov.hmcts.reform.pip.data.management.database.LocationRepository;
import uk.gov.hmcts.reform.pip.data.management.errorhandling.exceptions.ArtefactNotFoundException;
import uk.gov.hmcts.reform.pip.data.management.helpers.ArtefactHelper;
import uk.gov.hmcts.reform.pip.data.management.models.location.Location;
import uk.gov.hmcts.reform.pip.data.management.models.publication.Artefact;
import uk.gov.hmcts.reform.pip.data.management.service.AccountManagementService;
import uk.gov.hmcts.reform.pip.data.management.service.LocationService;
import uk.gov.hmcts.reform.pip.data.management.service.PublicationServicesService;
import uk.gov.hmcts.reform.pip.data.management.service.SubscriptionManagementService;
import uk.gov.hmcts.reform.pip.model.account.AzureAccount;
import uk.gov.hmcts.reform.pip.model.enums.UserActions;
import uk.gov.hmcts.reform.pip.model.publication.ListType;
import uk.gov.hmcts.reform.pip.model.system.admin.ActionResult;
import uk.gov.hmcts.reform.pip.model.system.admin.ChangeType;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static uk.gov.hmcts.reform.pip.model.LogBuilder.writeLog;

@Slf4j
@Service
public class ArtefactDeleteService {

    private final ArtefactRepository artefactRepository;
    private final LocationRepository locationRepository;
    private final LocationService locationService;
    private final AzureBlobService azureBlobService;
    private final SubscriptionManagementService subscriptionManagementService;
    private final AccountManagementService accountManagementService;
    private final PublicationServicesService publicationServicesService;

    public ArtefactDeleteService(ArtefactRepository artefactRepository, LocationRepository locationRepository,
                                 LocationService locationService, AzureBlobService azureBlobService,
                                 SubscriptionManagementService subscriptionManagementService,
                                 AccountManagementService accountManagementService,
                                 PublicationServicesService publicationServicesService) {
        this.artefactRepository = artefactRepository;
        this.locationRepository = locationRepository;
        this.locationService = locationService;
        this.azureBlobService = azureBlobService;
        this.subscriptionManagementService = subscriptionManagementService;
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
        Optional<Artefact> artefactToArchive = artefactRepository.findArtefactByArtefactId(artefactId);

        if (artefactToArchive.isPresent()) {
            deleteAllPublicationBlobData(artefactToArchive.get());
            artefactRepository.archiveArtefact(artefactId);
            log.info(writeLog(String.format("Artefact archived by %s, with artefact id: %s", issuerId, artefactId)));
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
    private void deleteAllPublicationBlobData(Artefact artefact) {
        // Delete the payload/flat file from the publications store
        azureBlobService.deleteBlob(ArtefactHelper.getUuidFromUrl(artefact.getPayload()));

        // Try to delete the generated files for the publications if it's not a flat file
        if (artefact.getIsFlatFile().equals(Boolean.FALSE)) {
            try {
                azureBlobService.deletePublicationBlob(artefact.getArtefactId() + ".pdf");

                // If it's an SJP list the xlsx file also needs to be deleted
                if (artefact.getListType().equals(ListType.SJP_PUBLIC_LIST)
                    || artefact.getListType().equals(ListType.SJP_PRESS_LIST)) {
                    azureBlobService.deletePublicationBlob(artefact.getArtefactId() + ".xlsx");
                }
            } catch (Exception ex) {
                log.error(writeLog("Failed to delete the generated publication file. Message: "
                                       + ex.getMessage()));
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
        subscriptionManagementService.sendDeletedArtefactForThirdParties(deletedArtefact);
    }

    public String deleteArtefactByLocation(Integer locationId, String provenanceUserId)
        throws JsonProcessingException {
        LocalDateTime searchDateTime = LocalDateTime.now();
        List<Artefact> activeArtefacts =
            artefactRepository.findActiveArtefactsForLocation(searchDateTime, locationId.toString());
        if (activeArtefacts.isEmpty()) {
            log.info(writeLog(String.format("User %s attempting to delete all artefacts for location %s. "
                                                + "No artefacts found",
                                            provenanceUserId, locationId)));
            throw new ArtefactNotFoundException(String.format(
                "No artefacts found with the location ID %s",
                locationId
            ));
        } else {
            log.info(writeLog(String.format("User %s attempting to delete all artefacts for location %s. "
                                                + "%s artefact(s) found",
                                            provenanceUserId, locationId, activeArtefacts.size())));

            activeArtefacts.forEach(artefact -> {
                artefactRepository.delete(artefact);
                deleteAllPublicationBlobData(artefact);
                log.info(writeLog(
                    String.format("Artefact deleted by %s, with artefact id: %s",
                                  provenanceUserId, artefact.getArtefactId())
                ));
            });
            Optional<Location> location = locationRepository.getLocationByLocationId(locationId);
            notifySystemAdminAboutSubscriptionDeletion(provenanceUserId,
                String.format("Total %s artefact(s) for location %s", activeArtefacts.size(),
                              location.isPresent() ? location.get().getName() : ""));
            return String.format("Total %s artefact deleted for location id %s", activeArtefacts.size(), locationId);
        }
    }

    public String deleteAllArtefactsWithLocationNamePrefix(String prefix) {
        List<String> locationIds = locationService.getAllLocationsWithNamePrefix(prefix).stream()
            .map(Object::toString)
            .toList();

        List<Artefact> artefactsToDelete = Collections.emptyList();
        if (!locationIds.isEmpty()) {
            artefactsToDelete = artefactRepository.findAllByLocationIdIn(locationIds);

            if (!artefactsToDelete.isEmpty()) {
                artefactsToDelete.forEach(a -> deleteAllPublicationBlobData(a));

                List<UUID> artefactIds = artefactsToDelete.stream()
                    .map(Artefact::getArtefactId)
                    .toList();
                artefactRepository.deleteAllByArtefactIdIn(artefactIds);
            }
        }
        return String.format("%s artefacts(s) deleted for location name starting with %s",
                             artefactsToDelete.size(), prefix);
    }

    private void notifySystemAdminAboutSubscriptionDeletion(String provenanceUserId, String additionalDetails)
        throws JsonProcessingException {
        AzureAccount userInfo = accountManagementService.getUserInfo(provenanceUserId);
        List<String> systemAdmins = accountManagementService.getAllAccounts("PI_AAD", "SYSTEM_ADMIN");
        publicationServicesService.sendSystemAdminEmail(systemAdmins, userInfo.getDisplayName(),
            ActionResult.SUCCEEDED, additionalDetails, ChangeType.DELETE_LOCATION_ARTEFACT);
    }
}
