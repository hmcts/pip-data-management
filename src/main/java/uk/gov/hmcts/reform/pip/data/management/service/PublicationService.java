package uk.gov.hmcts.reform.pip.data.management.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.pip.data.management.database.AzureTableService;
import uk.gov.hmcts.reform.pip.data.management.errorhandling.exceptions.DuplicatePublicationException;
import uk.gov.hmcts.reform.pip.data.management.models.publication.Artefact;

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
     * Method that handles the creation of a new publication.
     * @param artefact The artifact that needs to be created.
     * @return Returns the UUID of the artefact that was created.
     */
    public String createPublication(Artefact artefact) {

        azureTableService.getPublication(artefact.getArtefactId()).ifPresent(foundArtefact -> {
            throw new DuplicatePublicationException(String.format("Duplicate publication found with ID %s",
                                                                  artefact.getArtefactId()));
        });

        return azureTableService.createPublication(artefact);
    }
}
