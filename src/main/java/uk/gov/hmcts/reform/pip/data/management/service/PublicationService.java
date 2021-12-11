package uk.gov.hmcts.reform.pip.data.management.service;

import org.apache.tomcat.jni.Local;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.pip.data.management.database.ArtefactRepository;
import uk.gov.hmcts.reform.pip.data.management.database.AzureBlobService;
import uk.gov.hmcts.reform.pip.data.management.models.publication.Artefact;
import uk.gov.hmcts.reform.pip.data.management.models.publication.Sensitivity;
import uk.gov.hmcts.reform.pip.data.management.utils.PayloadExtractor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * This class contains the business logic for handling of Publications.
 */
@Component
public class PublicationService {

    private final ArtefactRepository artefactRepository;

    private final AzureBlobService azureBlobService;

    private final PayloadExtractor payloadExtractor;

    @Autowired
    public PublicationService(ArtefactRepository artefactRepository,
                              AzureBlobService azureBlobService,
                              PayloadExtractor payloadExtractor) {
        this.artefactRepository = artefactRepository;
        this.azureBlobService = azureBlobService;
        this.payloadExtractor = payloadExtractor;
    }

    /**
     * Method that handles the creation or updating of a new publication.
     *
     * @param artefact The artifact that needs to be created.
     * @param payload  The paylaod for the artefact that needs to be created.
     * @return Returns the UUID of the artefact that was created.
     */
    public Artefact createPublication(Artefact artefact, String payload) {

        Optional<Artefact> foundArtefact = artefactRepository
            .findBySourceArtefactIdAndProvenance(artefact.getSourceArtefactId(), artefact.getProvenance());

        foundArtefact.ifPresent(value -> artefact.setArtefactId(value.getArtefactId()));

        String blobUrl = azureBlobService.createPayload(
            artefact.getSourceArtefactId(),
            artefact.getProvenance(),
            payload
        );

        artefact.setPayload(blobUrl);
        artefact.setSearch(payloadExtractor.extractSearchTerms(payload));

        return artefactRepository.save(artefact);
    }

    public List<Artefact> findAllArtefacts() {
        return artefactRepository.findArtefactsByArtefactIdIsNotNull();
    }

    public List<Artefact> findAllArtefactsInDateRange() {
        LocalDateTime currentTime = LocalDateTime.now();
        LocalDateTime currentTime2 = LocalDateTime.now();
        return artefactRepository.findArtefactsByDisplayFromBeforeAndDisplayToAfter(currentTime, currentTime2);
    }

    public List<Artefact> findAllArtefactsInDateRangeAndPublic() {
        LocalDateTime currentTime = LocalDateTime.now();
        return artefactRepository
            .findArtefactsByDisplayFromBeforeAndDisplayToAfterAndSensitivityEquals(
                currentTime,
                currentTime,
                Sensitivity.PUBLIC
            );
    }

    public List<Artefact> findAllRelevantArtefacts(Boolean verified, String searchValue) {
        if (verified) {
            return findAllArtefactsInDateRange();
        } else {
            return findAllArtefactsInDateRangeAndPublic();
        }
    }

}
