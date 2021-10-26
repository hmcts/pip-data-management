package uk.gov.hmcts.reform.pip.data.management.service;

import com.azure.data.tables.TableClient;
import com.azure.data.tables.models.TableEntity;
import com.azure.data.tables.models.TableServiceException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.pip.data.management.errorhandling.exceptions.PublicationException;
import uk.gov.hmcts.reform.pip.data.management.models.publication.Artefact;

import java.util.UUID;

/**
 * This class contains the business logic for handling of Publications
 */
@Component
public class PublicationService {

    private TableClient tableClient;

    @Autowired
    public PublicationService(TableClient tableClient) {
        this.tableClient = tableClient;
    }

    /**
     * Method that handles the creation of a new publication.
     * @param artefact The artifact that needs to be created.
     * @return Returns the UUID of the artefact that was created.
     */
    public String createPublication(Artefact artefact) {
        try {
            String key = UUID.randomUUID().toString();
            TableEntity tableEntity = new TableEntity(key, key)
                .addProperty("artefactId", artefact.getArtefactId())
                .addProperty("provenance", artefact.getProvenance())
                .addProperty("sourceArtefactId", artefact.getSourceArtefactId())
                .addProperty("type", artefact.getType())
                .addProperty("sensitivity", artefact.getSensitivity())
                .addProperty("language", artefact.getLanguage())
                .addProperty("search", artefact.getSearch())
                .addProperty("displayFrom", artefact.getDisplayFrom())
                .addProperty("displayTo", artefact.getDisplayTo())
                .addProperty("payload", artefact.getPayload());
            tableClient.createEntity(tableEntity);
            return key;
        } catch (TableServiceException e) {
            throw new PublicationException(e.getMessage());
        }
    }
}
