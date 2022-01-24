package uk.gov.hmcts.reform.pip.data.management.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import uk.gov.hmcts.reform.pip.data.management.database.ArtefactRepository;
import uk.gov.hmcts.reform.pip.data.management.database.AzureBlobService;
import uk.gov.hmcts.reform.pip.data.management.errorhandling.exceptions.NotFoundException;
import uk.gov.hmcts.reform.pip.data.management.errorhandling.exceptions.UnauthorisedRequestException;
import uk.gov.hmcts.reform.pip.data.management.models.publication.Artefact;
import uk.gov.hmcts.reform.pip.data.management.models.publication.Sensitivity;
import uk.gov.hmcts.reform.pip.data.management.utils.PayloadExtractor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

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
     * @param payload  The payload for the artefact that needs to be created.
     * @return Returns the UUID of the artefact that was created.
     */
    public Artefact createPublication(Artefact artefact, String payload) {
        applyExistingArtefact(artefact);

        String blobUrl = azureBlobService.createPayload(
            artefact.getSourceArtefactId(),
            artefact.getProvenance(),
            payload
        );

        artefact.setPayload(blobUrl);
        artefact.setSearch(payloadExtractor.extractSearchTerms(payload));

        return artefactRepository.save(artefact);
    }

    public Artefact createPublication(Artefact artefact, MultipartFile file) {
        applyExistingArtefact(artefact);

        String blobUrl = azureBlobService.uploadFlatFile(
            artefact.getSourceArtefactId(),
            artefact.getProvenance(),
            file
        );
        artefact.setPayload(blobUrl);

        return artefactRepository.save(artefact);
    }

    /**
     * Checks if the artefact already exists based on source artefact id and provenance, if so it applies the
     * existing artefact ID to update.
     * @param artefact
     */
    private void applyExistingArtefact(Artefact artefact) {
        Optional<Artefact> foundArtefact = artefactRepository
            .findBySourceArtefactIdAndProvenance(artefact.getSourceArtefactId(), artefact.getProvenance());

        foundArtefact.ifPresent(value -> artefact.setArtefactId(value.getArtefactId()));
    }


    /**
     * Get all relevant artefacts relating to a given court ID.
     *
     * @param searchValue - represents the court ID in question being searched for
     * @param verified    - represents the verification status of the user. Currently only verified/non-verified, but
     *                    will include other verified user types in the future
     * @return a list of all artefacts that fulfil the timing criteria, match the given court id and sensitivity
     *                     associated with given verification status
     */
    public List<Artefact> findAllByCourtId(String searchValue, Boolean verified) {
        LocalDateTime currDate = LocalDateTime.now();
        if (verified) {
            return artefactRepository.findArtefactsBySearchVerified(searchValue, currDate);
        } else {
            return artefactRepository.findArtefactsBySearchUnverified(searchValue, currDate);
        }

    }

    /**
     * takes in artefact id and returns the data within the matching blob in string format.
     *
     * @param artefactId represents the artefact id which is then used to get an artefact to populate the inputs
     *                   for the blob request
     * @return the data within the blob in string format
     */
    public String getByArtefactId(UUID artefactId, Boolean verification) {

        Optional<Artefact> optionalArtefact = artefactRepository.findByArtefactId(artefactId);

        if (optionalArtefact.isPresent()) {
            Artefact artefact;
            artefact = optionalArtefact.get();
            if (!verification && artefact.getSensitivity() != Sensitivity.PUBLIC) {
                throw new UnauthorisedRequestException("Unauthorised Request.");
            }
            String sourceArtefactId = artefact.getSourceArtefactId();
            String provenance = artefact.getProvenance();
            return azureBlobService.getBlobData(sourceArtefactId, provenance);
        } else {
            throw new NotFoundException(String.format("No artefact found with the ID: %s", artefactId));
        }
    }

}
