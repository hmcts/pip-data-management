package uk.gov.hmcts.reform.pip.data.management.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.scheduling.annotation.Async;
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
import java.time.LocalTime;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import static uk.gov.hmcts.reform.pip.model.LogBuilder.writeLog;

/**
 * This class contains the business logic for handling of Publications.
 */

@Slf4j
@Service
@SuppressWarnings({"PMD.GodClass", "PMD.LawOfDemeter"})
public class PublicationService {

    private final ArtefactRepository artefactRepository;

    private final AzureBlobService azureBlobService;

    private final PayloadExtractor payloadExtractor;

    private final SubscriptionManagementService subscriptionManagementService;

    private final LocationRepository locationRepository;

    private final AccountManagementService accountManagementService;

    private final PublicationServicesService publicationServicesService;

    private final ChannelManagementService channelManagementService;

    private final ObjectMapper mapper = new ObjectMapper();

    @Autowired
    public PublicationService(ArtefactRepository artefactRepository,
                              AzureBlobService azureBlobService,
                              PayloadExtractor payloadExtractor,
                              SubscriptionManagementService subscriptionManagementService,
                              AccountManagementService accountManagementService,
                              LocationRepository locationRepository,
                              PublicationServicesService publicationServicesService,
                              ChannelManagementService channelManagementService) {
        this.artefactRepository = artefactRepository;
        this.azureBlobService = azureBlobService;
        this.payloadExtractor = payloadExtractor;
        this.subscriptionManagementService = subscriptionManagementService;
        this.accountManagementService = accountManagementService;
        this.locationRepository = locationRepository;
        this.publicationServicesService = publicationServicesService;
        this.channelManagementService = channelManagementService;
    }

    /**
     * Method that handles the creation or updating of a new publication.
     *
     * @param artefact The artifact that needs to be created.
     * @param payload  The payload for the artefact that needs to be created.
     * @return Returns the UUID of the artefact that was created.
     */
    public Artefact createPublication(Artefact artefact, String payload) {
        log.info(writeLog(UserActions.UPLOAD, "json publication upload for location "
            + artefact.getLocationId()));

        applyInternalLocationId(artefact);
        artefact.setContentDate(artefact.getContentDate().toLocalDate().atTime(LocalTime.MIN));
        boolean isExisting = applyExistingArtefact(artefact);

        String blobUrl = azureBlobService.createPayload(
            isExisting ? getUuidFromUrl(artefact.getPayload()) : UUID.randomUUID().toString(),
            payload
        );

        // Add 7 days to the expiry date if the list type is SJP
        if (ListType.SJP_PUBLIC_LIST.equals(artefact.getListType())
            || ListType.SJP_PRESS_LIST.equals(artefact.getListType())) {
            artefact.setExpiryDate(artefact.getExpiryDate().plusDays(7));
        }

        artefact.setPayload(blobUrl);

        if (!isExisting) {
            artefact.setPayload(blobUrl);
        }

        artefact.setSearch(payloadExtractor.extractSearchTerms(payload));
        return artefactRepository.save(artefact);
    }

    public Artefact createPublication(Artefact artefact, MultipartFile file) {
        log.info(writeLog(UserActions.UPLOAD, "flat file publication upload for location "
            + artefact.getLocationId()));

        applyInternalLocationId(artefact);
        artefact.setContentDate(artefact.getContentDate().toLocalDate().atTime(LocalTime.MIN));

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

    @Async
    public void processCreatedPublication(Artefact artefact) {
        channelManagementService.requestFileGeneration(artefact.getArtefactId());
        checkAndTriggerSubscriptionManagement(artefact);
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
     * Get all relevant artefacts relating to a given location ID.
     *
     * @param searchValue - represents the location ID in question being searched for
     * @param userId    - represents the user ID of the user who is making the request
     * @return a list of all artefacts that fulfil the timing criteria, match the given location id and
     *     sensitivity associated with given verification status.
     */
    public List<Artefact> findAllByLocationId(String searchValue, UUID userId) {
        LocalDateTime currDate = LocalDateTime.now();
        List<Artefact> artefacts =  artefactRepository.findArtefactsByLocationId(searchValue, currDate);

        return artefacts.stream().filter(artefact -> isAuthorised(artefact, userId)).collect(Collectors.toList());
    }

    /**
     * Get all artefacts for admin actions.
     *
     * @param locationId The location id to search for.
     * @param userId represents the user ID of the user who is making the request
     * @param isAdmin bool to check whether admin search is needed, if not will default to findAllByLocationId().
     * @return list of matching artefacts.
     */
    public List<Artefact> findAllByLocationIdAdmin(String locationId, UUID userId, boolean isAdmin) {
        log.info(writeLog("ADMIN - Searing for all artefacts with " + locationId));
        return isAdmin
            ? artefactRepository.findArtefactsByLocationIdAdmin(locationId) : findAllByLocationId(locationId, userId);
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
     * @param issuerId The id of the admin user who is attempting to delete the artefact.
     */
    public void deleteArtefactById(String artefactId, String issuerId) {
        Optional<Artefact> artefactToDelete = artefactRepository.findArtefactByArtefactId(artefactId);
        if (artefactToDelete.isPresent()) {
            log.info(azureBlobService.deleteBlob(getUuidFromUrl(artefactToDelete.get().getPayload())));
            artefactRepository.delete(artefactToDelete.get());
            log.info(writeLog(issuerId, UserActions.REMOVE, artefactId));
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
        artefactRepository.findArtefactsByDisplayFrom(LocalDate.now())
            .forEach(artefact -> log.info(sendArtefactForSubscription(artefact)));
    }

    /**
     * Scheduled method that:
     *  Checks daily for a list of all no match artefacts to send to publication services.
     *  checks daily for newly outdated artefacts based on a yesterday or older display to date.
     */
    @Scheduled(cron = "${cron.daily-start-of-day}")
    public void runDailyTasks() {
        reportNoMatchArtefacts();
        deleteExpiredArtefacts();
    }

    /**
     * Find artefacts with NoMatch location and send them for reporting.
     */
    public void reportNoMatchArtefacts() {
        findNoMatchArtefactsForReporting(artefactRepository.findAllNoMatchArtefacts());
    }

    /**
     * Delete expired artefacts from the database, Artefact and Publications Azure storage.
     */
    public void deleteExpiredArtefacts() {
        List<Artefact> outdatedArtefacts = artefactRepository.findOutdatedArtefacts(LocalDateTime.now());
        outdatedArtefacts.forEach(artefact -> {
            azureBlobService.deleteBlob(getUuidFromUrl(artefact.getPayload()));
            // Only attempt to delete from the publications container if it's not a flat file being deleted
            if (!artefact.getIsFlatFile()) {
                azureBlobService.deletePublicationBlob(artefact.getArtefactId() + ".pdf");
                // If it's an SJP list the xlsx file also needs to be deleted
                if (ListType.SJP_PUBLIC_LIST.equals(artefact.getListType())
                    || ListType.SJP_PRESS_LIST.equals(artefact.getListType())) {
                    azureBlobService.deletePublicationBlob(artefact.getArtefactId() + ".xlsx");
                }
            }
        });

        artefactRepository.deleteAll(outdatedArtefacts);
        log.info("{} outdated artefacts found and deleted for before {}", outdatedArtefacts.size(),
                 LocalDateTime.now());
    }

    private void applyInternalLocationId(Artefact artefact) {
        if ("MANUAL_UPLOAD".equalsIgnoreCase(artefact.getProvenance())) {
            return;
        }
        Optional<Location> location = locationRepository.findByLocationIdByProvenance(artefact.getProvenance(),
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
        log.info(writeLog(subscriptionManagementService.sendDeletedArtefactForThirdParties(deletedArtefact)));
    }

    /**
     * Receives a list of no match artefacts, checks it's not empty and create a map of location id to Provenance.
     * Send this on to publication services.
     * @param artefactList A list of no match artefacts
     */
    private void findNoMatchArtefactsForReporting(List<Artefact> artefactList) {
        if (!artefactList.isEmpty()) {
            Map<String, String> locationIdProvenanceMap = new ConcurrentHashMap<>();
            artefactList.forEach(artefact -> locationIdProvenanceMap.put(
                artefact.getLocationId().split("NoMatch")[1], artefact.getProvenance()));

            log.info(publicationServicesService.sendNoMatchArtefactsForReporting(locationIdProvenanceMap));
        }
    }

    /**
     * Take in an email and mask it for writing out to the logs.
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

    public String getMiData() {
        List<String> returnedData = artefactRepository.getMiData();
        StringBuilder builder = new StringBuilder(146);
        builder.append("artefact_id,display_from,display_to,language,provenance,sensitivity,"
                           + "source_artefact_id,type,content_date,court_id,court_name,search\n");
        for (String s : returnedData) {
            String[] splitString = s.split(",", 12);
            long one = 1;
            builder.append(Arrays.stream(splitString).limit(splitString.length - one)
                               .collect(Collectors.joining(","))).append(',');
            try {
                builder.append(jsonDestroyer(splitString[splitString.length - 1]));
            } catch (Exception e) {
                log.error(e.getMessage());
                builder.append("JSON Error\n");
            }
        }
        return builder.toString();
    }

    private String jsonDestroyer(String json) throws JsonProcessingException {
        JsonNode topLevel = mapper.readTree(json);
        JsonNode iteratorNode = topLevel.get("cases");
        if (iteratorNode == null) {
            return "\n";
        }
        Iterator<JsonNode> nodeIterator = iteratorNode.elements();
        int counter = 1;
        StringBuilder builder = new StringBuilder();
        while (nodeIterator.hasNext()) {
            JsonNode currentNode = nodeIterator.next();
            builder.append("Case ").append(counter);
            builder.append(": ");
            currentNode.fields().forEachRemaining(
                (node) -> {
                    builder.append(node.getKey().trim()).append(": ");
                    builder.append(node.getValue().asText().trim()).append(' ');
                }
            );
            builder.append(' ');
            counter += 1;
        }
        return builder.append('\n').toString();
    }
}
