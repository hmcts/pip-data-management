package uk.gov.hmcts.reform.pip.data.management.service.publication;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.CannotAcquireLockException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.retry.annotation.Retryable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import uk.gov.hmcts.reform.pip.data.management.database.ArtefactRepository;
import uk.gov.hmcts.reform.pip.data.management.database.AzureArtefactBlobService;
import uk.gov.hmcts.reform.pip.data.management.database.LocationRepository;
import uk.gov.hmcts.reform.pip.data.management.helpers.ArtefactHelper;
import uk.gov.hmcts.reform.pip.data.management.helpers.NoMatchArtefactHelper;
import uk.gov.hmcts.reform.pip.data.management.models.location.Location;
import uk.gov.hmcts.reform.pip.data.management.models.publication.Artefact;

import java.util.Optional;
import java.util.UUID;

/**
 * This class contains the business logic for handling creation of publications.
 */

@Slf4j
@Service
public class PublicationCreationService {

    private static final int RETRY_MAX_ATTEMPTS = 5;

    private final ArtefactRepository artefactRepository;

    private final AzureArtefactBlobService azureArtefactBlobService;

    private final LocationRepository locationRepository;

    private final PublicationFileManagementService publicationFileManagementService;

    private final PublicationSubscriptionService publicationSubscriptionService;

    private static final String MANUAL_UPLOAD_VALUE = "MANUAL_UPLOAD";

    @Autowired
    public PublicationCreationService(ArtefactRepository artefactRepository,
                                      AzureArtefactBlobService azureArtefactBlobService,
                                      LocationRepository locationRepository,
                                      PublicationFileManagementService publicationFileManagementService,
                                      PublicationSubscriptionService publicationSubscriptionService) {
        this.artefactRepository = artefactRepository;
        this.azureArtefactBlobService = azureArtefactBlobService;
        this.locationRepository = locationRepository;
        this.publicationFileManagementService = publicationFileManagementService;
        this.publicationSubscriptionService = publicationSubscriptionService;
    }

    /**
     * Method that handles the creation or updating of a new JSON publication.
     *
     * @param artefact The artefact that needs to be created.
     * @param payload  The payload for the artefact that needs to be created.
     * @return Returns the artefact that was created.
     */
    @Transactional(isolation = Isolation.READ_COMMITTED)
    @Retryable(retryFor = { CannotAcquireLockException.class, DataIntegrityViolationException.class},
        maxAttempts = RETRY_MAX_ATTEMPTS)
    public Artefact createPublication(Artefact artefact, String payload) {
        String existingPayload = applyExistingArtefact(artefact) ? artefact.getPayload() : null;
        String blobUrl = azureArtefactBlobService.createPayload(UUID.randomUUID().toString(), payload);

        artefact.setPayload(blobUrl);
        Artefact createdArtefact = artefactRepository.save(artefact);

        // Remove the old payload after superseded by the new one
        if (existingPayload != null) {
            azureArtefactBlobService.deleteBlob(ArtefactHelper.getUuidFromUrl(existingPayload));
        }

        return createdArtefact;
    }

    /**
     * Method that handles the creation or updating of a new flat file publication.
     *
     * @param artefact The artifact that needs to be created.
     * @param file     The flat file that is to be uploaded and associated with the artefact.
     * @return Returns the artefact that was created.
     */
    @Transactional(isolation = Isolation.READ_COMMITTED)
    @Retryable(retryFor = { CannotAcquireLockException.class, DataIntegrityViolationException.class },
        maxAttempts = RETRY_MAX_ATTEMPTS)
    public Artefact createPublication(Artefact artefact, MultipartFile file) {
        String existingPayload = applyExistingArtefact(artefact) ? artefact.getPayload() : null;
        String blobUrl = azureArtefactBlobService.uploadFlatFile(UUID.randomUUID().toString(), file);

        artefact.setPayload(blobUrl);
        Artefact createdArtefact = artefactRepository.save(artefact);

        // Remove the old payload after superseded by the new one
        if (existingPayload != null) {
            azureArtefactBlobService.deleteBlob(ArtefactHelper.getUuidFromUrl(existingPayload));
        }

        return createdArtefact;
    }

    @Async
    public void processCreatedPublication(Artefact artefact, String payload) {
        publicationFileManagementService.generateFiles(artefact.getArtefactId(), payload);
        publicationSubscriptionService.checkAndTriggerPublicationSubscription(artefact);
    }

    @Async
    public void processCreatedPublication(Artefact artefact) {
        publicationSubscriptionService.checkAndTriggerPublicationSubscription(artefact);
    }

    /**
     * Checks if the artefact already exists based on payloadId, if so it applies the
     * existing artefact ID to update.
     *
     * @param artefact The artefact to check existing on
     */
    private boolean applyExistingArtefact(Artefact artefact) {
        Optional<Artefact> foundArtefact = artefactRepository.findArtefactByUpdateLogic(
            artefact.getLocationId(),
            artefact.getContentDate(),
            artefact.getLanguage(),
            artefact.getListType(),
            artefact.getProvenance()
        );

        foundArtefact.ifPresent(value -> {
            artefact.setArtefactId(value.getArtefactId());
            artefact.setPayload(value.getPayload());
            artefact.setSupersededCount(value.getSupersededCount() + 1);
            publicationFileManagementService.deleteFiles(artefact.getArtefactId(), artefact.getListType(),
                                                     artefact.getLanguage());
        });
        return foundArtefact.isPresent();
    }

    public void applyInternalLocationId(Artefact artefact) {
        if (MANUAL_UPLOAD_VALUE.equalsIgnoreCase(artefact.getProvenance())) {
            return;
        }
        Optional<Location> location = locationRepository.findByLocationIdByProvenance(
            artefact.getProvenance(),
            artefact.getLocationId(),
            artefact.getListType()
                .getListLocationLevel().name()
        );
        if (location.isPresent()) {
            artefact.setLocationId(location.get().getLocationId().toString());

        } else {
            artefact.setLocationId(NoMatchArtefactHelper.buildNoMatchLocationId(artefact.getLocationId()));
        }
    }
}
