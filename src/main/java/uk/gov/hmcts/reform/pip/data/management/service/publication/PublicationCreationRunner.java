package uk.gov.hmcts.reform.pip.data.management.service.publication;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.ConcurrencyFailureException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import uk.gov.hmcts.reform.pip.data.management.errorhandling.exceptions.CreateArtefactConflictException;
import uk.gov.hmcts.reform.pip.data.management.models.publication.Artefact;
import uk.gov.hmcts.reform.pip.model.enums.UserActions;

import java.time.LocalDateTime;
import java.time.LocalTime;

import static uk.gov.hmcts.reform.pip.model.LogBuilder.writeLog;

@Slf4j
@Service
public class PublicationCreationRunner {
    private final PublicationCreationService publicationCreationService;

    @Autowired
    public PublicationCreationRunner(PublicationCreationService publicationCreationService) {
        this.publicationCreationService = publicationCreationService;
    }

    /**
     * Starts the json publication creation process.
     *
     * @param artefact The artefact that needs to be created.
     * @param payload  The payload for the artefact that needs to be created.
     * @return Returns the artefact that was created.
     */
    public Artefact run(Artefact artefact, String payload) {
        preprocessPublicationForCreation(artefact);
        Artefact createdArtefact;

        try {
            createdArtefact = publicationCreationService.createPublication(artefact, payload);
        } catch (ConcurrencyFailureException | DataIntegrityViolationException ex) {
            throw new CreateArtefactConflictException(
                "Deadlock when creating json publication. Please try again later."
            );
        }

        log.info(writeLog(UserActions.UPLOAD,
                          "json publication upload for location " + createdArtefact.getLocationId()));
        return createdArtefact;
    }

    /**
     * Starts the flat file publication creation process.
     *
     * @param artefact The artefact that needs to be created.
     * @param file     The flat file that is to be uploaded and associated with the artefact.
     * @return Returns the artefact that was created.
     */
    public Artefact run(Artefact artefact, MultipartFile file) {
        preprocessPublicationForCreation(artefact);
        Artefact createdArtefact;

        try {
            createdArtefact = publicationCreationService.createPublication(artefact, file);
        } catch (ConcurrencyFailureException | DataIntegrityViolationException ex) {
            throw new CreateArtefactConflictException(
                "Deadlock when creating flat file publication. Please try again later."
            );
        }

        log.info(writeLog(UserActions.UPLOAD,
                          "flat file publication upload for location " + artefact.getLocationId()));
        return createdArtefact;
    }

    private void preprocessPublicationForCreation(Artefact artefact) {
        publicationCreationService.applyInternalLocationId(artefact);
        artefact.setContentDate(artefact.getContentDate().toLocalDate().atTime(LocalTime.MIN));
        artefact.setLastReceivedDate(LocalDateTime.now());
    }
}
