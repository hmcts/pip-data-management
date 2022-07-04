package uk.gov.hmcts.reform.pip.data.management.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import uk.gov.hmcts.reform.pip.data.management.database.ArtefactRepository;
import uk.gov.hmcts.reform.pip.data.management.database.AzureBlobService;
import uk.gov.hmcts.reform.pip.data.management.database.LocationRepository;
import uk.gov.hmcts.reform.pip.data.management.errorhandling.exceptions.ArtefactNotFoundException;
import uk.gov.hmcts.reform.pip.data.management.models.location.Location;
import uk.gov.hmcts.reform.pip.data.management.models.location.LocationType;
import uk.gov.hmcts.reform.pip.data.management.models.publication.Artefact;
import uk.gov.hmcts.reform.pip.data.management.models.publication.ListType;
import uk.gov.hmcts.reform.pip.data.management.models.publication.Sensitivity;
import uk.gov.hmcts.reform.pip.data.management.utils.CaseSearchTerm;
import uk.gov.hmcts.reform.pip.data.management.utils.PayloadExtractor;
import uk.gov.hmcts.reform.pip.model.enums.UserActions;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import static uk.gov.hmcts.reform.pip.model.LogBuilder.writeLog;

/**
 * This class contains the business logic for handling of Publications.
 */

@Slf4j
@Service
public class PublicationService {

    private final ArtefactRepository artefactRepository;

    private final AzureBlobService azureBlobService;

    private final PayloadExtractor payloadExtractor;

    private final SubscriptionManagementService subscriptionManagementService;

    private final LocationRepository locationRepository;

    private final AccountManagementService accountManagementService;

    @Autowired
    public PublicationService(ArtefactRepository artefactRepository,
                              AzureBlobService azureBlobService,
                              PayloadExtractor payloadExtractor,
                              SubscriptionManagementService subscriptionManagementService,
                              AccountManagementService accountManagementService,
                              LocationRepository locationRepository) {
        this.artefactRepository = artefactRepository;
        this.azureBlobService = azureBlobService;
        this.payloadExtractor = payloadExtractor;
        this.subscriptionManagementService = subscriptionManagementService;
        this.accountManagementService = accountManagementService;
        this.locationRepository = locationRepository;
    }

    /**
     * Method that handles the creation or updating of a new publication.
     *
     * @param artefact The artifact that needs to be created.
     * @param payload  The payload for the artefact that needs to be created.
     * @return Returns the UUID of the artefact that was created.
     */
    public Artefact createPublication(Artefact artefact, String payload) {
        applyInternalLocationId(artefact);

        boolean isExisting = applyExistingArtefact(artefact);

        String blobUrl = azureBlobService.createPayload(
            isExisting ? getUuidFromUrl(artefact.getPayload()) : UUID.randomUUID().toString(),
            payload
        );

        artefact.setPayload(blobUrl);

        if (!isExisting) {
            artefact.setPayload(blobUrl);
        }

        artefact.setSearch(payloadExtractor.extractSearchTerms(payload));
        return artefactRepository.save(artefact);
    }

    public Artefact createPublication(Artefact artefact, MultipartFile file) {
        applyInternalLocationId(artefact);

        boolean isExisting = applyExistingArtefact(artefact);

        String blobUrl = azureBlobService.uploadFlatFile(
            isExisting ? getUuidFromUrl(artefact.getPayload()) : UUID.randomUUID().toString(),
            file
        );

        artefact.setPayload(blobUrl);

        if (!isExisting) {
            artefact.setPayload(blobUrl);
        }

        return artefactRepository.save(artefact);
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
            artefact.getLanguage().name(),
            artefact.getListType().name(),
            artefact.getProvenance());

        foundArtefact.ifPresent(value -> {
            artefact.setArtefactId(value.getArtefactId());
            artefact.setPayload(value.getPayload());
        });
        return foundArtefact.isPresent();
    }

    private String getUuidFromUrl(String payloadUrl) {
        return payloadUrl.substring(payloadUrl.lastIndexOf('/') + 1);
    }


    /**
     * Get all relevant artefacts relating to a given court ID.
     *
     * @param searchValue - represents the court ID in question being searched for
     * @param userId    - represents the user ID of the user who is making the request
     * @return a list of all artefacts that fulfil the timing criteria, match the given court id and
     *     sensitivity associated with given verification status.
     */
    public List<Artefact> findAllByCourtId(String searchValue, UUID userId) {
        LocalDateTime currDate = LocalDateTime.now();
        List<Artefact> artefacts =  artefactRepository.findArtefactsByCourtId(searchValue, currDate);

        return artefacts.stream().filter(artefact -> isAuthorised(artefact, userId)).collect(Collectors.toList());
    }

    /**
     * Get all artefacts for admin actions.
     *
     * @param courtId The court id to search for.
     * @param userId represents the user ID of the user who is making the request
     * @param isAdmin bool to check whether admin search is needed, if not will default to findAllByCourtId().
     * @return list of matching artefacts.
     */
    public List<Artefact> findAllByCourtIdAdmin(String courtId, UUID userId, boolean isAdmin) {
        return isAdmin ? artefactRepository.findArtefactsByCourtIdAdmin(courtId) : findAllByCourtId(courtId, userId);
    }

    /**
     * Get all relevant Artefacts based on search values stored in the Artefact.
     *
     * @param searchTerm  the search term checking against, eg. CASE_ID or CASE_URN
     * @param searchValue the search value to look for
     * @param userId  represents the user ID of the user who is making the request
     * @return list of Artefacts
     */
    public List<Artefact> findAllBySearch(CaseSearchTerm searchTerm, String searchValue, UUID userId) {
        LocalDateTime currDate = LocalDateTime.now();
        List<Artefact> artefacts;
        switch (searchTerm) {
            case CASE_ID:
            case CASE_URN:
                artefacts = artefactRepository.findArtefactBySearch(searchTerm.dbValue, searchValue, currDate);
                break;
            case CASE_NAME:
                artefacts = artefactRepository.findArtefactByCaseName(searchValue, currDate);
                break;
            default:
                throw new IllegalArgumentException(String.format("Invalid search term: %s", searchTerm));
        }

        artefacts = artefacts.stream().filter(artefact -> isAuthorised(artefact, userId)).collect(Collectors.toList());

        if (artefacts.isEmpty()) {
            throw new ArtefactNotFoundException(String.format("No Artefacts found with for %s with the value: %s",
                                                              searchTerm, searchValue
            ));
        }
        return artefacts;
    }

    public Artefact getMetadataByArtefactId(UUID artefactId) {
        return artefactRepository.findArtefactByArtefactId(artefactId.toString())
                .orElseThrow(() -> new ArtefactNotFoundException(String.format("No artefact found with the ID: %s",
                                                                       artefactId)));
    }

    /**
     * Takes in artefact id and returns the metadata for the artefact.
     *
     * @param artefactId   represents the artefact id which is then used to get an artefact to populate the inputs
     *                     for the blob request.
     * @param userId represents the user ID of the user who is making the request
     * @return The metadata for the found artefact.
     */
    public Artefact getMetadataByArtefactId(UUID artefactId, UUID userId) {

        LocalDateTime currentDate = LocalDateTime.now();

        Optional<Artefact> artefact = artefactRepository.findByArtefactId(artefactId.toString(),
            currentDate);

        if (artefact.isPresent() && isAuthorised(artefact.get(), userId)) {
            return artefact.get();
        }

        throw new ArtefactNotFoundException(String.format("No artefact found with the ID: %s", artefactId));
    }

    /**
     * Takes in artefact id and returns the payload within the matching blob in string format.
     *
     * @param artefactId   represents the artefact id which is then used to get an artefact to populate the inputs
     *                     for the blob request.
     * @param userId represents the user ID of the user who is making the request
     * @return The data within the blob in string format.
     */
    public String getPayloadByArtefactId(UUID artefactId, UUID userId) {
        Artefact artefact = getMetadataByArtefactId(artefactId, userId);

        return azureBlobService.getBlobData(getUuidFromUrl(artefact.getPayload()));
    }

    /**
     * Takes in artefact id and returns the payload within the matching blob in string format. This is used for admin
     * requests
     *
     * @param artefactId   represents the artefact id which is then used to get an artefact to populate the inputs
     *                     for the blob request.
     * @return The data within the blob in string format.
     */
    public String getPayloadByArtefactId(UUID artefactId) {
        Artefact artefact = getMetadataByArtefactId(artefactId);

        return azureBlobService.getBlobData(getUuidFromUrl(artefact.getPayload()));
    }

    /**
     * Retrieves a flat file for an artefact.
     * @param artefactId The artefact ID to retrieve the flat file from.
     * @param userId represents the user ID of the user who is making the request
     * @return The flat file resource.
     */
    public Resource getFlatFileByArtefactID(UUID artefactId, UUID userId) {
        Artefact artefact = getMetadataByArtefactId(artefactId, userId);

        return azureBlobService.getBlobFile(getUuidFromUrl(artefact.getPayload()));
    }

    /**
     * Retrieves a flat file for an artefact. This is used for admin requests
     * @param artefactId The artefact ID to retrieve the flat file from.
     * @return The flat file resource.
     */
    public Resource getFlatFileByArtefactID(UUID artefactId) {
        Artefact artefact = getMetadataByArtefactId(artefactId);

        return azureBlobService.getBlobFile(getUuidFromUrl(artefact.getPayload()));
    }

    /**
     * Attempts to delete a blob from the artefact store.
     * @param artefactId The ID of the artefact to be deleted.
     * @param issuerEmail The email of the admin user who is attempting to delete the artefact.
     */
    public void deleteArtefactById(String artefactId, String issuerEmail) {
        Optional<Artefact> artefactToDelete = artefactRepository.findArtefactByArtefactId(artefactId);
        if (artefactToDelete.isPresent()) {
            log.info(azureBlobService.deleteBlob(getUuidFromUrl(artefactToDelete.get().getPayload())));
            artefactRepository.delete(artefactToDelete.get());
            log.info(writeLog(issuerEmail, UserActions.REMOVE, artefactId));
            triggerThirdPartyArtefactDeleted(artefactToDelete.get());
        } else {
            throw new ArtefactNotFoundException("No artefact found with the ID: " + artefactId);
        }
    }

    /**
     * Checks if the artefact has a display from date of today or previous then triggers the sub fulfilment
     * process on subscription-management if appropriate.
     */
    public void checkAndTriggerSubscriptionManagement(Artefact artefact) {
        //TODO: fully switch this logic to localdates once artefact model changes
        if (artefact.getDisplayFrom().toLocalDate().isBefore(LocalDate.now().plusDays(1))
            && (artefact.getDisplayTo() == null
            || artefact.getDisplayTo().toLocalDate().isAfter(LocalDate.now().minusDays(1)))) {
            log.info(sendArtefactForSubscription(artefact));
        }
    }

    public String sendArtefactForSubscription(Artefact artefact) {
        return subscriptionManagementService.sendArtefactForSubscription(artefact);
    }

    /**
     * Scheduled method that checks daily for newly dated from artefacts.
     */
    @Scheduled(cron = "${cron.daily-start-of-day}")
    public void checkNewlyActiveArtefacts() {
        List<Artefact> newArtefactsToday = artefactRepository.findArtefactsByDisplayFrom(LocalDate.now());
        newArtefactsToday.forEach(artefact -> log.info(sendArtefactForSubscription(artefact)));
    }

    /**
     * Scheduled method that checks daily for newly outdated artefacts based on a yesterday or older display to date.
     */
    @Scheduled(cron = "${cron.daily-start-of-day}")
    public void deleteExpiredBlobs() {
        List<Artefact> outdatedArtefacts = artefactRepository.findOutdatedArtefacts(LocalDate.now());
        outdatedArtefacts.forEach(artefact ->
                                      log.info(azureBlobService.deleteBlob(getUuidFromUrl(artefact.getPayload()))));
        artefactRepository.deleteAll(outdatedArtefacts);
        log.info("{} outdated artefacts found and deleted for before {}", outdatedArtefacts.size(), LocalDate.now());
    }

    private void applyInternalLocationId(Artefact artefact) {
        if ("MANUAL_UPLOAD".equalsIgnoreCase(artefact.getProvenance())) {
            return;
        }
        Optional<Location> location = locationRepository.findByCourtIdByProvenance(artefact.getProvenance(),
                                                                                       artefact.getLocationId(),
                                                                                       artefact.getListType()
                                                                                       .getListLocationLevel().name());
        if (location.isPresent()) {
            artefact.setLocationId(location.get().getLocationId().toString());

        } else {
            artefact.setLocationId(String.format("NoMatch%s", artefact.getLocationId()));
        }
    }

    private boolean isAuthorised(Artefact artefact, UUID userId) {
        if (artefact.getSensitivity().equals(Sensitivity.PUBLIC)) {
            return true;
        } else if (userId == null) {
            return false;
        } else {
            return accountManagementService.getIsAuthorised(userId, artefact.getListType(), artefact.getSensitivity());
        }
    }

    public LocationType getLocationType(ListType listType) {
        return listType.getListLocationLevel();
    }

    /**
     * Triggers subscription management to handle deleted artefact to third party subscribers.
     * @param deletedArtefact deleted artefact to notify of.
     */
    private void triggerThirdPartyArtefactDeleted(Artefact deletedArtefact) {
        log.info(subscriptionManagementService.sendDeletedArtefactForThirdParties(deletedArtefact));
    }
}
