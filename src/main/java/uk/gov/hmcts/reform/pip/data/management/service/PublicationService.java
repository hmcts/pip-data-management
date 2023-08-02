package uk.gov.hmcts.reform.pip.data.management.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import uk.gov.hmcts.reform.pip.data.management.database.ArtefactRepository;
import uk.gov.hmcts.reform.pip.data.management.database.AzureBlobService;
import uk.gov.hmcts.reform.pip.data.management.database.LocationRepository;
import uk.gov.hmcts.reform.pip.data.management.helpers.ArtefactHelper;
import uk.gov.hmcts.reform.pip.data.management.helpers.LocationHelper;
import uk.gov.hmcts.reform.pip.data.management.models.location.Location;
import uk.gov.hmcts.reform.pip.data.management.models.publication.Artefact;
import uk.gov.hmcts.reform.pip.data.management.service.artefact.ArtefactTriggerService;
import uk.gov.hmcts.reform.pip.data.management.utils.PayloadExtractor;
import uk.gov.hmcts.reform.pip.model.enums.UserActions;
import uk.gov.hmcts.reform.pip.model.publication.ListType;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Optional;
import java.util.UUID;

import static uk.gov.hmcts.reform.pip.model.LogBuilder.writeLog;

/**
 * This class contains the business logic for handling of Publications.
 */

@Slf4j
@Service
public class PublicationService {

    private static final char DELIMITER = ',';

    private final ArtefactRepository artefactRepository;

    private final AzureBlobService azureBlobService;

    private final PayloadExtractor payloadExtractor;

    private final LocationRepository locationRepository;

    private final ChannelManagementService channelManagementService;

    private final ArtefactTriggerService artefactTriggerService;

    @Autowired
    public PublicationService(ArtefactRepository artefactRepository,
                              AzureBlobService azureBlobService,
                              PayloadExtractor payloadExtractor,
                              LocationRepository locationRepository,
                              ChannelManagementService channelManagementService,
                              ArtefactTriggerService artefactTriggerService) {
        this.artefactRepository = artefactRepository;
        this.azureBlobService = azureBlobService;
        this.payloadExtractor = payloadExtractor;
        this.locationRepository = locationRepository;
        this.channelManagementService = channelManagementService;
        this.artefactTriggerService = artefactTriggerService;
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
        artefact.setLastReceivedDate(LocalDateTime.now());

        boolean isExisting = applyExistingArtefact(artefact);

        String blobUrl = azureBlobService.createPayload(
            isExisting ? ArtefactHelper.getUuidFromUrl(artefact.getPayload()) : UUID.randomUUID().toString(),
            payload
        );

        // Add 7 days to the expiry date if the list type is SJP
        if (artefact.getListType().equals(ListType.SJP_PUBLIC_LIST)
            || artefact.getListType().equals(ListType.SJP_PRESS_LIST)
            || artefact.getListType().equals(ListType.SJP_DELTA_PRESS_LIST)) {
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
        artefact.setLastReceivedDate(LocalDateTime.now());

        boolean isExisting = applyExistingArtefact(artefact);

        String blobUrl = azureBlobService.uploadFlatFile(
            isExisting ? ArtefactHelper.getUuidFromUrl(artefact.getPayload()) : UUID.randomUUID().toString(),
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
            artefact.getLanguage().name(),
            artefact.getListType().name(),
            artefact.getProvenance()
        );

        foundArtefact.ifPresent(value -> {
            artefact.setArtefactId(value.getArtefactId());
            artefact.setPayload(value.getPayload());
            artefact.setSupersededCount(value.getSupersededCount() + 1);
        });
        return foundArtefact.isPresent();
    }

    private void applyInternalLocationId(Artefact artefact) {
        if ("MANUAL_UPLOAD".equalsIgnoreCase(artefact.getProvenance())) {
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
            artefact.setLocationId(LocationHelper.buildNoMatchLocationId(artefact.getLocationId()));
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
        StringBuilder builder = new StringBuilder(150);
        builder
            .append("artefact_id,display_from,display_to,language,provenance,sensitivity,source_artefact_id,"
                        + "type,content_date,court_id,court_name,list_type")
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
