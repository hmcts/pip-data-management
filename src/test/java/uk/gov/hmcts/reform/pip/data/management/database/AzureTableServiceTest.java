package uk.gov.hmcts.reform.pip.data.management.database;

import com.azure.core.http.rest.PagedIterable;
import com.azure.data.tables.TableClient;
import com.azure.data.tables.implementation.ModelHelper;
import com.azure.data.tables.models.TableEntity;
import com.azure.data.tables.models.TableServiceException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.pip.data.management.config.PublicationConfiguration;
import uk.gov.hmcts.reform.pip.data.management.errorhandling.exceptions.AzureServerException;
import uk.gov.hmcts.reform.pip.data.management.models.publication.Artefact;
import uk.gov.hmcts.reform.pip.data.management.models.publication.ArtefactType;
import uk.gov.hmcts.reform.pip.data.management.models.publication.Language;
import uk.gov.hmcts.reform.pip.data.management.models.publication.Sensitivity;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AzureTableServiceTest {

    @Mock
    TableClient tableClient;

    @Mock
    PagedIterable<TableEntity> tableEntities;

    @InjectMocks
    AzureTableService azureTableService;

    private static Artefact newArtefact;
    private static Artefact existingArtefact;
    private static TableEntity tableEntity;
    private static TableEntity tableEntityNoOptionals;

    private static final String ARTEFACT_ID = "1234";
    private static final ArtefactType ARTEFACT_TYPE = ArtefactType.LIST;
    private static final Sensitivity SENSITIVITY = Sensitivity.PUBLIC;
    private static final String PROVENANCE = "provenance";
    private static final String PAYLOAD = "payload";
    private static final String SEARCH = "search";
    private static final String SOURCE_ARTEFACT_ID = "sourceArtefactId";
    private static final LocalDateTime DISPLAY_TO = LocalDateTime.now();
    private static final LocalDateTime DISPLAY_FROM = LocalDateTime.now();
    private static final Language LANGUAGE = Language.ENGLISH;

    private static final String EXCEPTION_MESSAGE = "Test Message";
    private static final String EXCEPTION_MESSAGE_CREATE = "Server error while creating a publication in Azure";
    private static final String EXCEPTION_MESSAGE_UPDATE = "Server error while updating a publication in Azure";
    private static final String EXCEPTION_MESSAGE_RETREIVE = "Server error while retrieving publications from Azure";
    private static final String VALIDATION_UUID = "A randomly generated UUID is returned";
    private static final String VALIDATION_PROVENANCE = "The expected Provenance is returned";
    private static final String VALIDATION_SOURCE_ARTEFACT_ID = "The expected source artefact ID is returned";
    private static final String VALIDATION_ARTEFACT_TYPE = "The expected Artefact type is returned";
    private static final String VALIDATION_SENSITIVITY = "The expected sensitivity is returned";
    private static final String VALIDATION_LANGUAGE = "The expected language is returned";
    private static final String VALIDATION_SEARCH = "The expected search parameter is returned";
    private static final String VALIDATION_DISPLAY_FROM = "The expected display from date is returned";
    private static final String VALIDATION_DISPLAY_TO = "The expected display to date is returned";
    private static final String VALIDATION_PAYLOAD = "The expected payload is returned";
    private static final String VALIDATION_ARTEFACT_ID = "The expected artefact ID is returned";

    @BeforeAll
    public static void setup() {
        newArtefact = Artefact.builder()
            .type(ARTEFACT_TYPE)
            .sensitivity(SENSITIVITY)
            .provenance(PROVENANCE)
            .payload(PAYLOAD)
            .search(SEARCH)
            .sourceArtefactId(SOURCE_ARTEFACT_ID)
            .displayTo(DISPLAY_TO)
            .displayFrom(DISPLAY_FROM)
            .language(LANGUAGE)
            .build();

        existingArtefact = Artefact.builder()
            .artefactId(ARTEFACT_ID)
            .build();

        Map<String, Object> entityProperties = new ConcurrentHashMap<>();
        entityProperties.put(PublicationConfiguration.ARTIFACT_ID_TABLE, ARTEFACT_ID);
        entityProperties.put(PublicationConfiguration.PROVENANCE_TABLE, PROVENANCE);
        entityProperties.put(PublicationConfiguration.SOURCE_ARTEFACT_ID_TABLE, SOURCE_ARTEFACT_ID);
        entityProperties.put(PublicationConfiguration.TYPE_TABLE, ARTEFACT_TYPE);
        entityProperties.put(PublicationConfiguration.PAYLOAD_TABLE, PAYLOAD);

        tableEntityNoOptionals = ModelHelper.createEntity(entityProperties);

        entityProperties.put(PublicationConfiguration.SENSITIVITY_TABLE, SENSITIVITY);
        entityProperties.put(PublicationConfiguration.LANGUAGE_TABLE, LANGUAGE);
        entityProperties.put(PublicationConfiguration.SEARCH_TABLE, SEARCH);
        entityProperties.put(PublicationConfiguration.DISPLAY_FROM_TABLE, DISPLAY_FROM);
        entityProperties.put(PublicationConfiguration.DISPLAY_TO_TABLE, DISPLAY_TO);

        tableEntity = ModelHelper.createEntity(entityProperties);
    }

    @Test
    void testCreationOfValidArtifact() {

        ArgumentCaptor<TableEntity> argumentCaptor = ArgumentCaptor.forClass(TableEntity.class);

        azureTableService.createPublication(newArtefact);

        verify(tableClient, times(1)).createEntity(argumentCaptor.capture());

        TableEntity returnedTableEntity = argumentCaptor.getValue();

        assertNotNull(returnedTableEntity.getProperty(PublicationConfiguration.ARTIFACT_ID_TABLE),
                     VALIDATION_UUID);
        assertEquals(returnedTableEntity.getProperty(PublicationConfiguration.PROVENANCE_TABLE), PROVENANCE,
                     VALIDATION_PROVENANCE);
        assertEquals(returnedTableEntity.getProperty(PublicationConfiguration.SOURCE_ARTEFACT_ID_TABLE),
                     SOURCE_ARTEFACT_ID, VALIDATION_SOURCE_ARTEFACT_ID);
        assertEquals(returnedTableEntity.getProperty(PublicationConfiguration.TYPE_TABLE), ARTEFACT_TYPE,
                     VALIDATION_ARTEFACT_TYPE);
        assertEquals(returnedTableEntity.getProperty(PublicationConfiguration.SENSITIVITY_TABLE), SENSITIVITY,
                     VALIDATION_SENSITIVITY);
        assertEquals(returnedTableEntity.getProperty(PublicationConfiguration.LANGUAGE_TABLE), LANGUAGE,
                     VALIDATION_LANGUAGE);
        assertEquals(returnedTableEntity.getProperty(PublicationConfiguration.SEARCH_TABLE), SEARCH,
                     VALIDATION_SEARCH);
        assertEquals(returnedTableEntity.getProperty(PublicationConfiguration.DISPLAY_FROM_TABLE), DISPLAY_FROM,
                     VALIDATION_DISPLAY_FROM);
        assertEquals(returnedTableEntity.getProperty(PublicationConfiguration.DISPLAY_TO_TABLE), DISPLAY_TO,
                     VALIDATION_DISPLAY_TO);
        assertEquals(returnedTableEntity.getProperty(PublicationConfiguration.PAYLOAD_TABLE), PAYLOAD,
                     VALIDATION_PAYLOAD);
    }

    @Test
    void testCreationOfErroredArtefact() {

        doThrow(new TableServiceException(EXCEPTION_MESSAGE, null)).when(tableClient).createEntity(any());

        AzureServerException publicationException = assertThrows(AzureServerException.class, () -> {
            azureTableService.createPublication(newArtefact);
        });

        assertEquals(EXCEPTION_MESSAGE_CREATE, publicationException.getMessage(),
                     "The expected exception message is returned");
    }

    @Test
    void testUpdateOfValidArtefact() {

        ArgumentCaptor<TableEntity> argumentCaptor = ArgumentCaptor.forClass(TableEntity.class);

        azureTableService.updatePublication(newArtefact, existingArtefact);

        verify(tableClient, times(1)).updateEntity(argumentCaptor.capture());

        TableEntity returnedTableEntity = argumentCaptor.getValue();

        assertEquals(ARTEFACT_ID, returnedTableEntity.getProperty(PublicationConfiguration.ARTIFACT_ID_TABLE),
                      "The existing Artefact ID is used");
        assertEquals(returnedTableEntity.getProperty(PublicationConfiguration.PROVENANCE_TABLE), PROVENANCE,
                     VALIDATION_PROVENANCE);
        assertEquals(returnedTableEntity.getProperty(PublicationConfiguration.SOURCE_ARTEFACT_ID_TABLE),
                     SOURCE_ARTEFACT_ID, VALIDATION_SOURCE_ARTEFACT_ID);
        assertEquals(returnedTableEntity.getProperty(PublicationConfiguration.TYPE_TABLE), ARTEFACT_TYPE,
                     VALIDATION_ARTEFACT_TYPE);
        assertEquals(returnedTableEntity.getProperty(PublicationConfiguration.SENSITIVITY_TABLE), SENSITIVITY,
                     VALIDATION_SENSITIVITY);
        assertEquals(returnedTableEntity.getProperty(PublicationConfiguration.LANGUAGE_TABLE), LANGUAGE,
                     VALIDATION_LANGUAGE);
        assertEquals(returnedTableEntity.getProperty(PublicationConfiguration.SEARCH_TABLE), SEARCH,
                     VALIDATION_SEARCH);
        assertEquals(returnedTableEntity.getProperty(PublicationConfiguration.DISPLAY_FROM_TABLE), DISPLAY_FROM,
                     VALIDATION_DISPLAY_FROM);
        assertEquals(returnedTableEntity.getProperty(PublicationConfiguration.DISPLAY_TO_TABLE), DISPLAY_TO,
                     VALIDATION_DISPLAY_TO);
        assertEquals(returnedTableEntity.getProperty(PublicationConfiguration.PAYLOAD_TABLE), PAYLOAD,
                     VALIDATION_PAYLOAD);
    }

    @Test
    void testUpdateOfErroredArtefact() {

        doThrow(new TableServiceException(EXCEPTION_MESSAGE, null)).when(tableClient).updateEntity(any());

        AzureServerException publicationException = assertThrows(AzureServerException.class, () -> {
            azureTableService.updatePublication(newArtefact, existingArtefact);
        });

        assertEquals(EXCEPTION_MESSAGE_UPDATE, publicationException.getMessage(),
                     "The expected exception message is returned");
    }

    @Test
    void testGetPublicationSuccessful() {
        when(tableClient.listEntities(any(), any(), any())).thenReturn(tableEntities);
        when(tableEntities.stream()).thenReturn(Stream.of(tableEntity));

        Optional<Artefact> artefactOptional = azureTableService.getPublication(ARTEFACT_ID, PROVENANCE);

        assertTrue(artefactOptional.isPresent(), "An artefact has been returned");

        Artefact returnedArtefact = artefactOptional.get();

        assertEquals(tableEntity.getProperty(PublicationConfiguration.ARTIFACT_ID_TABLE),
                     returnedArtefact.getArtefactId(), VALIDATION_ARTEFACT_ID);
        assertEquals(tableEntity.getProperty(PublicationConfiguration.PROVENANCE_TABLE),
                     returnedArtefact.getProvenance(), VALIDATION_PROVENANCE);
        assertEquals(tableEntity.getProperty(PublicationConfiguration.SOURCE_ARTEFACT_ID_TABLE),
                     returnedArtefact.getSourceArtefactId(), VALIDATION_SOURCE_ARTEFACT_ID);
        assertEquals(tableEntity.getProperty(PublicationConfiguration.TYPE_TABLE), returnedArtefact.getType(),
                     VALIDATION_ARTEFACT_TYPE);
        assertEquals(tableEntity.getProperty(PublicationConfiguration.SENSITIVITY_TABLE),
                     returnedArtefact.getSensitivity(), VALIDATION_SENSITIVITY);
        assertEquals(tableEntity.getProperty(PublicationConfiguration.LANGUAGE_TABLE), returnedArtefact.getLanguage(),
                     VALIDATION_LANGUAGE);
        assertEquals(tableEntity.getProperty(PublicationConfiguration.SEARCH_TABLE), returnedArtefact.getSearch(),
                     VALIDATION_SEARCH);
        assertEquals(tableEntity.getProperty(PublicationConfiguration.DISPLAY_FROM_TABLE),
                     returnedArtefact.getDisplayFrom(), VALIDATION_DISPLAY_FROM);
        assertEquals(tableEntity.getProperty(PublicationConfiguration.DISPLAY_TO_TABLE),
                     returnedArtefact.getDisplayTo(), VALIDATION_DISPLAY_TO);
        assertEquals(tableEntity.getProperty(PublicationConfiguration.PAYLOAD_TABLE), returnedArtefact.getPayload(),
                     VALIDATION_PAYLOAD);
    }

    @Test
    void testGetPublicationNotFound() {

        when(tableClient.listEntities(any(), any(), any())).thenReturn(tableEntities);
        when(tableEntities.stream()).thenReturn(Stream.of());

        Optional<Artefact> artefactOptional = azureTableService.getPublication(ARTEFACT_ID, PROVENANCE);

        assertFalse(artefactOptional.isPresent(), "No optional is returned");
    }

    @Test
    void testGetPublicationErrored() {

        doThrow(new TableServiceException(EXCEPTION_MESSAGE, null)).when(tableClient).listEntities(any(), any(), any());

        AzureServerException publicationException = assertThrows(AzureServerException.class, () -> {
            azureTableService.getPublication(ARTEFACT_ID, PROVENANCE);
        });

        assertEquals(EXCEPTION_MESSAGE_RETREIVE, publicationException.getMessage(),
                     "The expected exception message is returned");

    }

    @Test
    void testGetPublicationNoOptionalFields() {

        when(tableClient.listEntities(any(), any(), any())).thenReturn(tableEntities);
        when(tableEntities.stream()).thenReturn(Stream.of(tableEntityNoOptionals));

        Optional<Artefact> artefactOptional = azureTableService.getPublication(ARTEFACT_ID, PROVENANCE);

        assertTrue(artefactOptional.isPresent(), "The returned artefact is present");

        Artefact returnedArtefact = artefactOptional.get();

        assertEquals(tableEntity.getProperty(PublicationConfiguration.ARTIFACT_ID_TABLE),
                     returnedArtefact.getArtefactId(), VALIDATION_ARTEFACT_ID);
        assertEquals(tableEntity.getProperty(PublicationConfiguration.PROVENANCE_TABLE),
                     returnedArtefact.getProvenance(), VALIDATION_PROVENANCE);
        assertEquals(tableEntity.getProperty(PublicationConfiguration.SOURCE_ARTEFACT_ID_TABLE),
                     returnedArtefact.getSourceArtefactId(), VALIDATION_SOURCE_ARTEFACT_ID);
        assertEquals(tableEntity.getProperty(PublicationConfiguration.TYPE_TABLE), returnedArtefact.getType(),
                     VALIDATION_ARTEFACT_TYPE);
        assertNull(returnedArtefact.getSensitivity(),
                   VALIDATION_SENSITIVITY);
        assertNull(returnedArtefact.getLanguage(),
                   VALIDATION_LANGUAGE);
        assertNull(returnedArtefact.getSearch(),
                   VALIDATION_SEARCH);
        assertNull(returnedArtefact.getDisplayFrom(),
                   VALIDATION_DISPLAY_FROM);
        assertNull(returnedArtefact.getDisplayTo(),
                   VALIDATION_DISPLAY_TO);
        assertEquals(tableEntity.getProperty(PublicationConfiguration.PAYLOAD_TABLE), returnedArtefact.getPayload(),
                     VALIDATION_PAYLOAD);
    }

}
