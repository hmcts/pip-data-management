package uk.gov.hmcts.reform.pip.data.management.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import uk.gov.hmcts.reform.pip.data.management.models.publication.Artefact;
import uk.gov.hmcts.reform.pip.data.management.utils.PayloadExtractor;
import uk.gov.hmcts.reform.pip.model.enums.UserActions;
import uk.gov.hmcts.reform.pip.model.publication.ListType;

import java.time.LocalDateTime;
import java.time.LocalTime;

import static uk.gov.hmcts.reform.pip.model.LogBuilder.writeLog;

@Slf4j
@Service
public class PublicationCreationRunner {
    private final PublicationService publicationService;

    private final PayloadExtractor payloadExtractor;

    @Autowired
    public PublicationCreationRunner(PublicationService publicationService, PayloadExtractor payloadExtractor) {
        this.publicationService = publicationService;
        this.payloadExtractor = payloadExtractor;
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
        Artefact createdArtefact = publicationService.createPublication(artefact, payload);

        log.info(writeLog(UserActions.UPLOAD,
                          "json publication upload for location " + createdArtefact.getLocationId()));
        return createdArtefact;
    }

    /**
     * Starts the flat file publication creation process.
     *
     * @param artefact The artefact that needs to be created.
     * @return Returns the artefact that was created.
     */
    public Artefact run(Artefact artefact, MultipartFile file) {
        preprocessPublicationForCreation(artefact);
        Artefact createdArtefact = publicationService.createPublication(artefact, file);

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
            artefact.setSearch(payloadExtractor.extractSearchTerms(payload));
        }
    }

    private void preprocessPublicationForCreation(Artefact artefact) {
        publicationService.applyInternalLocationId(artefact);
        artefact.setContentDate(artefact.getContentDate().toLocalDate().atTime(LocalTime.MIN));
        artefact.setLastReceivedDate(LocalDateTime.now());
    }
}
