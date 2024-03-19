package uk.gov.hmcts.reform.pip.data.management.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.CannotAcquireLockException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.orm.jpa.JpaSystemException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import uk.gov.hmcts.reform.pip.data.management.errorhandling.exceptions.CreateArtefactConflictException;
import uk.gov.hmcts.reform.pip.data.management.models.publication.Artefact;
import uk.gov.hmcts.reform.pip.data.management.utils.JsonExtractor;
import uk.gov.hmcts.reform.pip.model.enums.UserActions;
import uk.gov.hmcts.reform.pip.model.publication.ListType;

import java.time.LocalDateTime;
import java.time.LocalTime;

import static uk.gov.hmcts.reform.pip.model.LogBuilder.writeLog;

@Slf4j
@Service
public class PublicationCreationRunner {
    private final PublicationService publicationService;

    private final JsonExtractor jsonExtractor;

    @Autowired
    public PublicationCreationRunner(PublicationService publicationService, JsonExtractor jsonExtractor) {
        this.publicationService = publicationService;
        this.jsonExtractor = jsonExtractor;
    }

    /**
     * Starts the json publication creation process.
     *
     * @param artefact The artefact that needs to be created.
     * @param payload  The payload for the artefact that needs to be created.
     * @return Returns the artefact that was created.
     */
    public Artefact run(Artefact artefact, String payload) {
        preprocessJsonPublicationForCreation(artefact, payload);
        Artefact createdArtefact;

        try {
            createdArtefact = publicationService.createPublication(artefact, payload);
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
            createdArtefact = publicationService.createPublication(artefact, file);
        } catch (CannotAcquireLockException | DataIntegrityViolationException ex) {
            throw new CreateArtefactConflictException(
                "Deadlock when creating flat file publication. Please try again later."
            );
        }

        log.info(writeLog(UserActions.UPLOAD,
                          "flat file publication upload for location " + artefact.getLocationId()));
        return createdArtefact;
    }

    private void preprocessJsonPublicationForCreation(Artefact artefact, String payload) {
        preprocessPublicationForCreation(artefact);

        // Add 7 days to the expiry date if the list type is SJP
        if (artefact.getListType().equals(ListType.SJP_PUBLIC_LIST)
            || artefact.getListType().equals(ListType.SJP_PRESS_LIST)
            || artefact.getListType().equals(ListType.SJP_DELTA_PRESS_LIST)) {
            artefact.setExpiryDate(artefact.getExpiryDate().plusDays(7));
        }

        if (payload != null) {
            artefact.setSearch(jsonExtractor.extractSearchTerms(payload));
        }
    }

    private void preprocessPublicationForCreation(Artefact artefact) {
        publicationService.applyInternalLocationId(artefact);
        artefact.setContentDate(artefact.getContentDate().toLocalDate().atTime(LocalTime.MIN));
        artefact.setLastReceivedDate(LocalDateTime.now());
    }
}
