package uk.gov.hmcts.reform.pip.data.management.service.artefact;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.pip.data.management.database.ArtefactRepository;
import uk.gov.hmcts.reform.pip.data.management.models.publication.Artefact;
import uk.gov.hmcts.reform.pip.data.management.service.PublicationServicesService;
import uk.gov.hmcts.reform.pip.data.management.service.SubscriptionManagementService;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
public class ArtefactTriggerService {

    private final ArtefactRepository artefactRepository;
    private final SubscriptionManagementService subscriptionManagementService;
    private final PublicationServicesService publicationServicesService;

    public ArtefactTriggerService(ArtefactRepository artefactRepository,
                                  SubscriptionManagementService subscriptionManagementService,
                                  PublicationServicesService publicationServicesService) {
        this.artefactRepository = artefactRepository;
        this.subscriptionManagementService = subscriptionManagementService;
        this.publicationServicesService = publicationServicesService;
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
    public void checkNewlyActiveArtefacts() {
        artefactRepository.findArtefactsByDisplayFrom(LocalDate.now())
            .forEach(artefact -> log.info(sendArtefactForSubscription(artefact)));
    }

    /**
     * Find artefacts with NoMatch location and send them for reporting.
     */
    public void reportNoMatchArtefacts() {
        findNoMatchArtefactsForReporting(artefactRepository.findAllNoMatchArtefacts());
    }

    /**
     * Receives a list of no match artefacts, checks it's not empty and create a map of location id to Provenance.
     * Send this on to publication services.
     *
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
}
