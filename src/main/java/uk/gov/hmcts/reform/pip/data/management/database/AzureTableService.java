package uk.gov.hmcts.reform.pip.data.management.database;

import com.azure.data.tables.TableClient;
import com.azure.data.tables.models.ListEntitiesOptions;
import com.azure.data.tables.models.TableEntity;
import com.azure.data.tables.models.TableServiceException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.pip.data.management.config.PublicationConfiguration;
import uk.gov.hmcts.reform.pip.data.management.errorhandling.exceptions.PublicationException;
import uk.gov.hmcts.reform.pip.data.management.models.publication.Artefact;
import uk.gov.hmcts.reform.pip.data.management.models.publication.ArtefactType;
import uk.gov.hmcts.reform.pip.data.management.models.publication.Language;
import uk.gov.hmcts.reform.pip.data.management.models.publication.Sensitivity;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * This is the repository layer that deals with Publications in the Azure table service.
 */
@Component
public class AzureTableService {

    private final TableClient tableClient;

    @Autowired
    public AzureTableService(TableClient tableClient) {
        this.tableClient = tableClient;
    }

    /**
     * Method that handles the creation of a new publication in the Azure table service.
     * @param artefact The artifact that needs to be created.
     * @return Returns the UUID of the artefact that was created.
     */
    public String createPublication(Artefact artefact) {
        try {
            String key = UUID.randomUUID().toString();
            TableEntity tableEntity = new TableEntity(key, key)
                .addProperty(PublicationConfiguration.ARTIFACT_ID_TABLE, artefact.getArtefactId())
                .addProperty(PublicationConfiguration.PROVENANCE_TABLE, artefact.getProvenance())
                .addProperty(PublicationConfiguration.SOURCE_ARTEFACT_ID_TABLE, artefact.getSourceArtefactId())
                .addProperty(PublicationConfiguration.TYPE_TABLE, artefact.getType())
                .addProperty(PublicationConfiguration.SENSITIVITY_TABLE, artefact.getSensitivity())
                .addProperty(PublicationConfiguration.LANGUAGE_TABLE, artefact.getLanguage())
                .addProperty(PublicationConfiguration.SEARCH_TABLE, artefact.getSearch())
                .addProperty(PublicationConfiguration.DISPLAY_FROM_TABLE, artefact.getDisplayFrom())
                .addProperty(PublicationConfiguration.DISPLAY_TO_TABLE, artefact.getDisplayTo())
                .addProperty(PublicationConfiguration.PAYLOAD_TABLE, artefact.getPayload());
            tableClient.createEntity(tableEntity);
            return key;
        } catch (TableServiceException e) {
            throw new PublicationException(e.getMessage());
        }
    }

    /**
     * Method that handles the retrieval of a publication from the Azure Table service.
     * @param artefactId The artefact ID to retrieve.
     * @return The Optional of the Artefact. Empty if not found.
     */
    public Optional<Artefact> getPublication(String artefactId) {

        ListEntitiesOptions options = new ListEntitiesOptions()
            .setFilter(String.format("artefactId eq '%s'", artefactId));

        List<TableEntity> returnedArtefacts;
        try {
            returnedArtefacts = tableClient.listEntities(options, null, null)
                .stream().collect(Collectors.toList());
        } catch (TableServiceException tableServiceException) {
            throw new PublicationException(tableServiceException.getMessage());
        }

        if (returnedArtefacts.isEmpty()) {
            return Optional.empty();
        } else {
            TableEntity tableEntity = returnedArtefacts.get(0);

            Artefact.ArtefactBuilder artefactBuilder = Artefact.builder()
                .artefactId(tableEntity.getProperty(PublicationConfiguration.ARTIFACT_ID_TABLE).toString())
                .provenance(tableEntity.getProperty(PublicationConfiguration.PROVENANCE_TABLE).toString())
                .sourceArtefactId(tableEntity.getProperty(PublicationConfiguration.SOURCE_ARTEFACT_ID_TABLE).toString())
                .type(ArtefactType.valueOf(tableEntity.getProperty(PublicationConfiguration.TYPE_TABLE).toString()))
                .payload(tableEntity.getProperty(PublicationConfiguration.PAYLOAD_TABLE).toString());


            Object sensitivity = tableEntity.getProperty(PublicationConfiguration.SENSITIVITY_TABLE);
            if (sensitivity != null) {
                artefactBuilder.sensitivity(Sensitivity.valueOf(sensitivity.toString()));
            }

            Object language = tableEntity.getProperty(PublicationConfiguration.LANGUAGE_TABLE);
            if (language != null) {
                artefactBuilder.language(Language.valueOf(language.toString()));
            }

            Object search = tableEntity.getProperty(PublicationConfiguration.SEARCH_TABLE);
            if (search != null) {
                artefactBuilder.search(search.toString());
            }

            Object displayFrom = tableEntity.getProperty(PublicationConfiguration.DISPLAY_FROM_TABLE);
            if (displayFrom != null) {
                artefactBuilder.displayFrom(LocalDateTime.parse(
                    displayFrom.toString(), DateTimeFormatter.ISO_DATE_TIME));
            }

            Object displayTo = tableEntity.getProperty(PublicationConfiguration.DISPLAY_TO_TABLE);
            if (displayTo != null) {
                artefactBuilder.displayTo(LocalDateTime.parse(
                    displayTo.toString(), DateTimeFormatter.ISO_DATE_TIME));
            }

            Artefact artefact = artefactBuilder.build();
            return Optional.of(artefact);
        }
    }
}
