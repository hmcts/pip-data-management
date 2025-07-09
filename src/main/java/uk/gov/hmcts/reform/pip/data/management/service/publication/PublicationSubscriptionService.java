package uk.gov.hmcts.reform.pip.data.management.service.publication;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.pip.data.management.database.ArtefactRepository;
import uk.gov.hmcts.reform.pip.data.management.models.publication.Artefact;
import uk.gov.hmcts.reform.pip.data.management.service.AccountManagementService;

import java.time.LocalDate;

@Service
public class PublicationSubscriptionService {
    private final ArtefactRepository artefactRepository;
    private final AccountManagementService accountManagementService;

    public PublicationSubscriptionService(ArtefactRepository artefactRepository,
                                          AccountManagementService accountManagementService) {
        this.artefactRepository = artefactRepository;
        this.accountManagementService = accountManagementService;
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
}
