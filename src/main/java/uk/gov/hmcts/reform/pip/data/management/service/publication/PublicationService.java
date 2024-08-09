package uk.gov.hmcts.reform.pip.data.management.service.publication;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.math.NumberUtils;
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
import uk.gov.hmcts.reform.pip.data.management.service.PublicationManagementService;

import java.util.Optional;
import java.util.UUID;

/**
 * This class contains the business logic for handling of Publications.
 */

@Slf4j
@Service
public class PublicationService {

    private static final char DELIMITER = ',';
    private static final int RETRY_MAX_ATTEMPTS = 5;

    private final ArtefactRepository artefactRepository;

    private final AzureArtefactBlobService azureArtefactBlobService;

    private final LocationRepository locationRepository;

    private final PublicationManagementService publicationManagementService;

    private final ArtefactTriggerService artefactTriggerService;

    private final ArtefactService artefactService;

    private static final String MANUAL_UPLOAD_VALUE = "MANUAL_UPLOAD";

    @Autowired
    public PublicationService(ArtefactRepository artefactRepository,
                              AzureArtefactBlobService azureArtefactBlobService,
                              LocationRepository locationRepository,
                              PublicationManagementService publicationManagementService,
                              ArtefactTriggerService artefactTriggerService,
                              ArtefactService artefactService) {
        this.artefactRepository = artefactRepository;
        this.azureArtefactBlobService = azureArtefactBlobService;
        this.locationRepository = locationRepository;
        this.publicationManagementService = publicationManagementService;
        this.artefactTriggerService = artefactTriggerService;
        this.artefactService = artefactService;
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
        if (artefactService.payloadWithinLimit(artefact.getPayloadSize())) {
            publicationManagementService.generateFiles(artefact.getArtefactId(), payload);
        }
        artefactTriggerService.checkAndTriggerSubscriptionManagement(artefact);
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
            if (!artefactService.payloadWithinLimit(artefact.getPayloadSize())) {
                publicationManagementService.deleteFiles(artefact.getArtefactId(), artefact.getListType(),
                                                         artefact.getLanguage());
            }
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

    /**
     * Take in an email and mask it for writing out to the logs.
     *
     * @param emailToMask The email to mask
     * @return A masked email
     */
    public String maskEmail(String emailToMask) {
        // Sonar flags regex as a bug. However, unable to find a way to split this out.
        if (emailToMask != null) {
            return emailToMask.replaceAll("(^([^@])|(?!^)\\G)[^@]", "$1*"); //NOSONAR
        }
        return emailToMask;
    }

    /**
     * Retrieve artefact data for MI reporting. Insert court name before returning the data.
     * @return MI artefact data as comma delimited string
     */
    public String getMiData() {
        StringBuilder builder = new StringBuilder(200);
        builder
            .append("artefact_id,display_from,display_to,language,provenance,sensitivity,source_artefact_id,"
                        + "superseded_count,type,content_date,court_id,court_name,list_type")
            .append(System.lineSeparator());

        artefactRepository.getMiData()
            .stream()
            // Insert an extra field for court name before the list type
            .map(line -> new StringBuilder(line)
                .insert(line.lastIndexOf(DELIMITER), DELIMITER + getLocationNameFromMiData(line))
                .toString())
            .forEach(line -> builder.append(line)
                .append(System.lineSeparator()));
        return builder.toString();
    }

    private String getLocationNameFromMiData(String line) {
        // Find the second to last index of the delimiter then advance a place for the location ID index
        int locationIdIndex = line.lastIndexOf(DELIMITER, line.lastIndexOf(DELIMITER) - 1) + 1;
        String locationId = line.substring(locationIdIndex, line.lastIndexOf(DELIMITER));

        if (NumberUtils.isCreatable(locationId)) {
            return locationRepository.getLocationByLocationId(Integer.valueOf(locationId))
                .map(Location::getName)
                .orElse("");
        }
        return "";
    }
}
