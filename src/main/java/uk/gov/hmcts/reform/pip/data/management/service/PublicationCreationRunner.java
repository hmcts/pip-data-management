package uk.gov.hmcts.reform.pip.data.management.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.CannotAcquireLockException;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.pip.data.management.errorhandling.exceptions.CreateArtefactConflictException;
import uk.gov.hmcts.reform.pip.data.management.models.publication.Artefact;
import uk.gov.hmcts.reform.pip.model.enums.UserActions;

import static uk.gov.hmcts.reform.pip.model.LogBuilder.writeLog;

@Slf4j
@Service
public class PublicationCreationRunner {
    private final PublicationService publicationService;

    @Autowired
    public PublicationCreationRunner(PublicationService publicationService) {
        this.publicationService = publicationService;
    }

    /**
     * Method that handles the creation or updating of a new publication.
     *
     * @param artefact The artifact that needs to be created.
     * @param payload  The payload for the artefact that needs to be created.
     * @return Returns the UUID of the artefact that was created.
     */
    public Artefact run(Artefact artefact, String payload) {
        publicationService.preprocessArtefactForCreation(artefact);

        Artefact createdArtefact = null;
        int maxRetries = 10;
        do {
            maxRetries--;
            try {
                createdArtefact = publicationService.createPublication(artefact, payload);
            } catch (CannotAcquireLockException e) {
                if (maxRetries == 0) {
                    throw new CreateArtefactConflictException(
                        "Deadlock when creating json publication. Please try again later."
                    );
                }
            }
        } while (createdArtefact == null && maxRetries >= 0);

        log.info(writeLog(UserActions.UPLOAD,
                          "json publication upload for location " + createdArtefact.getLocationId()));
        return createdArtefact;
    }
}
