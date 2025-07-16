package uk.gov.hmcts.reform.pip.data.management.service.publication;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.CannotAcquireLockException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import uk.gov.hmcts.reform.pip.data.management.errorhandling.exceptions.CreateArtefactConflictException;
import uk.gov.hmcts.reform.pip.data.management.models.publication.Artefact;
import uk.gov.hmcts.reform.pip.data.management.utils.JsonExtractor;
import uk.gov.hmcts.reform.pip.model.enums.UserActions;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Collections;

import static uk.gov.hmcts.reform.pip.model.LogBuilder.writeLog;

@Slf4j
@Service
public class PublicationCreationRunner {
    private final PublicationCreationService publicationCreationService;

    private final PublicationRetrievalService publicationRetrievalService;

    private final JsonExtractor jsonExtractor;

    @Autowired
    public PublicationCreationRunner(PublicationCreationService publicationCreationService,
                                     PublicationRetrievalService publicationRetrievalService,
                                     JsonExtractor jsonExtractor) {
        this.publicationCreationService = publicationCreationService;
        this.publicationRetrievalService = publicationRetrievalService;
        this.jsonExtractor = jsonExtractor;
    }

    /**
     * Starts the json publication creation process.
     *
     * @param artefact The artefact that needs to be created.
     * @param payload  The payload for the artefact that needs to be created.
     * @param extractSearchTerms  TRUE if extracting the search terms for subscription search.
     * @return Returns the artefact that was created.
     */
    public Artefact run(Artefact artefact, String payload, boolean extractSearchTerms) {
        preprocessJsonPublicationForCreation(artefact, payload, extractSearchTerms);
        Artefact createdArtefact;

        try {
            createdArtefact = publicationCreationService.createPublication(artefact, payload);
        } catch (CannotAcquireLockException | DataIntegrityViolationException ex) {
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
        } catch (CannotAcquireLockException | DataIntegrityViolationException ex) {
            throw new CreateArtefactConflictException(
                "Deadlock when creating flat file publication. Please try again later."
            );
        }

        log.info(writeLog(UserActions.UPLOAD,
                          "flat file publication upload for location " + artefact.getLocationId()));
        return createdArtefact;
    }

    private void preprocessJsonPublicationForCreation(Artefact artefact, String payload, boolean extractSearchTerms) {
        preprocessPublicationForCreation(artefact);
        if (extractSearchTerms
            && payload != null
            && publicationRetrievalService.payloadWithinJsonSearchLimit(artefact.getPayloadSize())) {
            artefact.setSearch(jsonExtractor.extractSearchTerms(payload));
        } else {
            artefact.setSearch(Collections.emptyMap());
        }
    }

    private void preprocessPublicationForCreation(Artefact artefact) {
        publicationCreationService.applyInternalLocationId(artefact);
        artefact.setContentDate(artefact.getContentDate().toLocalDate().atTime(LocalTime.MIN));
        artefact.setLastReceivedDate(LocalDateTime.now());
    }
}
