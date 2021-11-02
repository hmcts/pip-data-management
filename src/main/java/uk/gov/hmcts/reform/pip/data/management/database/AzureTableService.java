package uk.gov.hmcts.reform.pip.data.management.database;

import com.azure.data.tables.TableClient;
import com.azure.data.tables.models.ListEntitiesOptions;
import com.azure.data.tables.models.TableEntity;
import com.azure.data.tables.models.TableServiceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.pip.data.management.config.PublicationConfiguration;
import uk.gov.hmcts.reform.pip.data.management.errorhandling.exceptions.AzureServerException;
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

    private static final Logger LOGGER = LoggerFactory.getLogger(AzureTableService.class);

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
                .addProperty(PublicationConfiguration.ARTIFACT_ID_TABLE, key)
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
        } catch (TableServiceException tableServiceException) {
            LOGGER.error(tableServiceException.getMessage());
            throw new AzureServerException("Server error while creating a publication in Azure");
        }
    }

    /**
     * Method that handles the updating of a new publication in the Azure table service.
     * @param newArtefact The new artefact that is already present.
     * @param existingArtefact The existing artefact that needs to be updated.
     * @return Returns the UUID of the artefact that was created.
     */
    public String updatePublication(Artefact newArtefact, Artefact existingArtefact) {
        try {
            TableEntity tableEntity = new TableEntity(existingArtefact.getArtefactId(),
                                                      existingArtefact.getArtefactId())
                .addProperty(PublicationConfiguration.ARTIFACT_ID_TABLE, existingArtefact.getArtefactId())
                .addProperty(PublicationConfiguration.PROVENANCE_TABLE, newArtefact.getProvenance())
                .addProperty(PublicationConfiguration.SOURCE_ARTEFACT_ID_TABLE, newArtefact.getSourceArtefactId())
                .addProperty(PublicationConfiguration.TYPE_TABLE, newArtefact.getType())
                .addProperty(PublicationConfiguration.SENSITIVITY_TABLE, newArtefact.getSensitivity())
                .addProperty(PublicationConfiguration.LANGUAGE_TABLE, newArtefact.getLanguage())
                .addProperty(PublicationConfiguration.SEARCH_TABLE, newArtefact.getSearch())
                .addProperty(PublicationConfiguration.DISPLAY_FROM_TABLE, newArtefact.getDisplayFrom())
                .addProperty(PublicationConfiguration.DISPLAY_TO_TABLE, newArtefact.getDisplayTo())
                .addProperty(PublicationConfiguration.PAYLOAD_TABLE, newArtefact.getPayload());
            tableClient.updateEntity(tableEntity);
            return existingArtefact.getArtefactId();
        } catch (TableServiceException tableServiceException) {
            LOGGER.error(tableServiceException.getMessage());
            throw new AzureServerException("Server error while updating a publication in Azure");
        }
    }

    /**
     * Method that handles the retrieval of a publication from the Azure Table service.
     * @param sourceArtefactId The source artefact ID to retrieve.
     * @param provenance The source system the artefact came from.
     * @return The Optional of the Artefact. Empty if not found.
     */
    public Optional<Artefact> getPublication(String sourceArtefactId, String provenance) {

        ListEntitiesOptions options = new ListEntitiesOptions()
            .setFilter(String.format("sourceArtefactId eq '%1$s' and provenance eq '%2$s'",
                                     sourceArtefactId, provenance));

        try {
            List<TableEntity> returnedArtefacts = tableClient.listEntities(options, null, null)
                .stream().collect(Collectors.toList());

            if (returnedArtefacts.isEmpty()) {
                return Optional.empty();
            }

            return Optional.of(convertTableEntityToArtefact(returnedArtefacts.get(0)));

        } catch (TableServiceException tableServiceException) {
            LOGGER.error(tableServiceException.getMessage());
            throw new AzureServerException("Server error while retrieving publications from Azure");
        }
    }

    /**
     * Safely converts a potential null value returned from Azure into an Optional.
     * @param tableEntity The table entity to parse.
     * @param property The property to extract.
     * @return The optional representation of the property.
     */
    private Optional<String> retrieveProperty(TableEntity tableEntity, String property) {

        Object retreivedProperty = tableEntity.getProperty(property);
        if (retreivedProperty != null) {

            return Optional.of(retreivedProperty.toString());

        }
        return Optional.empty();
    }

    /**
     * Method which converts a TableEntity into an Artefact.
     * @param tableEntity The table entity to convert.
     * @return The returned Artefact.
     */
    private Artefact convertTableEntityToArtefact(TableEntity tableEntity) {
        Artefact.ArtefactBuilder artefactBuilder = Artefact.builder()
            .artefactId(tableEntity.getProperty(PublicationConfiguration.ARTIFACT_ID_TABLE).toString())
            .provenance(tableEntity.getProperty(PublicationConfiguration.PROVENANCE_TABLE).toString())
            .sourceArtefactId(tableEntity.getProperty(PublicationConfiguration.SOURCE_ARTEFACT_ID_TABLE).toString())
            .type(ArtefactType.valueOf(tableEntity.getProperty(PublicationConfiguration.TYPE_TABLE).toString()))
            .payload(tableEntity.getProperty(PublicationConfiguration.PAYLOAD_TABLE).toString());

        retrieveProperty(tableEntity, PublicationConfiguration.SENSITIVITY_TABLE).ifPresent(value -> {
            artefactBuilder.sensitivity(Sensitivity.valueOf(value));
        });

        retrieveProperty(tableEntity, PublicationConfiguration.LANGUAGE_TABLE).ifPresent(value -> {
            artefactBuilder.language(Language.valueOf(value));
        });

        retrieveProperty(tableEntity, PublicationConfiguration.SEARCH_TABLE).ifPresent(artefactBuilder::search);

        retrieveProperty(tableEntity, PublicationConfiguration.DISPLAY_FROM_TABLE).ifPresent(value -> {
            artefactBuilder.displayFrom(LocalDateTime.parse(value, DateTimeFormatter.ISO_DATE_TIME));
        });

        retrieveProperty(tableEntity, PublicationConfiguration.DISPLAY_TO_TABLE).ifPresent(value -> {
            artefactBuilder.displayTo(LocalDateTime.parse(value, DateTimeFormatter.ISO_DATE_TIME));
        });

        return artefactBuilder.build();
    }

}
