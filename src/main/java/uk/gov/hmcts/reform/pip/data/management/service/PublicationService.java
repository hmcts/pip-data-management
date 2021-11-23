package uk.gov.hmcts.reform.pip.data.management.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.pip.data.management.database.ArtefactRepository;
import uk.gov.hmcts.reform.pip.data.management.models.publication.Artefact;

import java.util.Optional;

/**
 * This class contains the business logic for handling of Publications.
 */
@Component
public class PublicationService {

    private final ArtefactRepository artefactRepository;

    @Autowired
    public PublicationService(ArtefactRepository artefactRepository) {
        this.artefactRepository = artefactRepository;
    }

    /**
     * Method that handles the creation or updating of a new publication.
     * @param artefact The artifact that needs to be created.
     * @return Returns the UUID of the artefact that was created.
     */
    public Artefact createPublication(Artefact artefact) {

        Optional<Artefact> foundArtefact =  artefactRepository.
            findBySourceArtefactIdAndProvenance(artefact.getSourceArtefactId(), artefact.getProvenance());

        foundArtefact.ifPresent(value -> artefact.setArtefactId(value.getArtefactId()));

        return artefactRepository.save(artefact);
    }
}
