package uk.gov.hmcts.reform.pip.data.management.service.publication;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.pip.data.management.database.ArtefactRepository;
import uk.gov.hmcts.reform.pip.data.management.helpers.NoMatchArtefactHelper;
import uk.gov.hmcts.reform.pip.data.management.models.publication.Artefact;
import uk.gov.hmcts.reform.pip.data.management.models.publication.NoMatchArtefact;
import uk.gov.hmcts.reform.pip.data.management.service.AccountManagementService;
import uk.gov.hmcts.reform.pip.data.management.service.PublicationServicesService;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class ArtefactTriggerService {

    private final ArtefactRepository artefactRepository;
    private final AccountManagementService accountManagementService;
    private final PublicationServicesService publicationServicesService;

    public ArtefactTriggerService(ArtefactRepository artefactRepository,
                                  AccountManagementService accountManagementService,
                                  PublicationServicesService publicationServicesService) {
        this.artefactRepository = artefactRepository;
        this.accountManagementService = accountManagementService;
        this.publicationServicesService = publicationServicesService;
    }

    /**
     * Checks if the artefact has a display from date of today or previous then triggers the sub fulfilment
     * process on account-management if appropriate.
     */
    public void checkAndTriggerPublicationSubscription(Artefact artefact) {
        //TODO: fully switch this logic to localdates once artefact model changes //NOSONAR
        if (artefact.getDisplayFrom().toLocalDate().isBefore(LocalDate.now().plusDays(1))
            && (artefact.getDisplayTo() == null
            || artefact.getDisplayTo().toLocalDate().isAfter(LocalDate.now().minusDays(1)))) {
            accountManagementService.sendArtefactForSubscription(artefact);
        }
    }

    /**
     * Scheduled method that checks daily for newly dated from artefacts.
     */
    public void checkNewlyActiveArtefacts() {
        artefactRepository.findArtefactsByDisplayFrom(LocalDate.now())
            .forEach(accountManagementService::sendArtefactForSubscription);
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
            List<NoMatchArtefact> noMatchArtefactList = new ArrayList<>();

            artefactList.forEach(artefact -> noMatchArtefactList.add(
                new NoMatchArtefact(artefact.getArtefactId(), artefact.getProvenance(),
                                    NoMatchArtefactHelper.getLocationIdForNoMatch(artefact.getLocationId()))
            ));

            publicationServicesService.sendNoMatchArtefactsForReporting(noMatchArtefactList);
        }
    }
}
