package uk.gov.hmcts.reform.pip.data.management.database;

import com.azure.data.tables.TableClient;
import com.azure.data.tables.models.ListEntitiesOptions;
import com.azure.data.tables.models.TableEntity;
import com.azure.data.tables.models.TableServiceException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.pip.data.management.errorhandling.exceptions.PublicationException;
import uk.gov.hmcts.reform.pip.data.management.models.publication.Artefact;
import uk.gov.hmcts.reform.pip.data.management.models.publication.ArtefactType;
import uk.gov.hmcts.reform.pip.data.management.models.publication.Language;
import uk.gov.hmcts.reform.pip.data.management.models.publication.Sensitivity;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * This is the repository layer that deals with Publications in the Azure table service.
 */
@Component
public class AzureTableService {

    private TableClient tableClient;

    private static final String ARTEFACT_ID = "artefactId";
    private static final String PROVENANCE = "provenance";
    private static final String SOURCE_ARTEFACT_ID = "sourceArtefactId";
    private static final String TYPE = "type";
    private static final String SENSITIVITY = "sensitivity";
    private static final String LANGUAGE = "language";
    private static final String SEARCH = "search";
    private static final String DISPLAY_FROM = "displayFrom";
    private static final String DISPLAY_TO = "displayTo";
    private static final String PAYLOAD = "paylod";

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
                .addProperty(ARTEFACT_ID, artefact.getArtefactId())
                .addProperty(PROVENANCE, artefact.getProvenance())
                .addProperty(SOURCE_ARTEFACT_ID, artefact.getSourceArtefactId())
                .addProperty(TYPE, artefact.getType())
                .addProperty(SENSITIVITY, artefact.getSensitivity())
                .addProperty(LANGUAGE, artefact.getLanguage())
                .addProperty(SEARCH, artefact.getSearch())
                .addProperty(DISPLAY_FROM, artefact.getDisplayFrom())
                .addProperty(DISPLAY_TO, artefact.getDisplayTo())
                .addProperty(PAYLOAD, artefact.getPayload());
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

        List<TableEntity> returnedArtefacts =
            tableClient.listEntities(options, null, null).stream().collect(Collectors.toList());

        if (returnedArtefacts.size() == 0) {
            return Optional.empty();
        } else {
            TableEntity tableEntity = returnedArtefacts.get(0);

            Artefact.ArtefactBuilder artefactBuilder = Artefact.builder()
                .artefactId(tableEntity.getProperty(ARTEFACT_ID).toString())
                .provenance(tableEntity.getProperty(PROVENANCE).toString())
                .sourceArtefactId(tableEntity.getProperty(SOURCE_ARTEFACT_ID).toString())
                .type(ArtefactType.valueOf(tableEntity.getProperty(TYPE).toString()))
                .payload(tableEntity.getProperty(PAYLOAD).toString());


            Object sensitivity = tableEntity.getProperty(SENSITIVITY);
            if (sensitivity != null) {
                artefactBuilder.sensitivity(Sensitivity.valueOf(sensitivity.toString()));
            }

            Object language = tableEntity.getProperty(LANGUAGE);
            if (language != null) {
                artefactBuilder.language(Language.valueOf(language.toString()));
            }

            Object search = tableEntity.getProperty(SEARCH);
            if (search != null) {
                artefactBuilder.search(search.toString());
            }

            Object displayFrom = tableEntity.getProperty(DISPLAY_FROM);
            if (displayFrom != null) {
                artefactBuilder.displayFrom(LocalDateTime.parse(displayFrom.toString()));
            }

            Object displayTo = tableEntity.getProperty(DISPLAY_TO);
            if (displayTo != null) {
                artefactBuilder.displayTo(LocalDateTime.parse(displayTo.toString()));
            }

            Artefact artefact = artefactBuilder.build();
            return Optional.of(artefact);
        }
    }
}
