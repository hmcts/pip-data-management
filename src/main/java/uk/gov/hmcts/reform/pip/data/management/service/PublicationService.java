package uk.gov.hmcts.reform.pip.data.management.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.pip.data.management.database.AzureTableService;
import uk.gov.hmcts.reform.pip.data.management.models.publication.Artefact;

import java.util.Optional;

/**
 * This class contains the business logic for handling of Publications.
 */
@Component
public class PublicationService {

    private final AzureTableService azureTableService;

    @Autowired
    public PublicationService(AzureTableService azureTableService) {
        this.azureTableService = azureTableService;
    }

    /**
     * Method that handles the creation or updating of a new publication.
     * @param artefact The artifact that needs to be created.
     * @return Returns the UUID of the artefact that was created.
     */
    public String createPublication(Artefact artefact) {

        Optional<Artefact> foundArtefact =  azureTableService.getPublication(
            artefact.getSourceArtefactId(), artefact.getProvenance());

        if (foundArtefact.isPresent()) {
            return azureTableService.updatePublication(artefact, foundArtefact.get());
        }

        return azureTableService.createPublication(artefact);
    }
}
