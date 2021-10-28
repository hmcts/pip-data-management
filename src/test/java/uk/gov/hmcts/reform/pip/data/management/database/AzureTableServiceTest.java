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
import uk.gov.hmcts.reform.pip.data.management.errorhandling.exceptions.PublicationException;
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

    private static Artefact artefact;
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

    @BeforeAll
    public static void setup() {
        artefact = Artefact.builder()
            .artefactId(ARTEFACT_ID)
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

        azureTableService.createPublication(artefact);

        verify(tableClient, times(1)).createEntity(argumentCaptor.capture());

        TableEntity returnedTableEntity = argumentCaptor.getValue();

        assertEquals(returnedTableEntity.getProperty(PublicationConfiguration.ARTIFACT_ID_TABLE), ARTEFACT_ID,
                     "The expected Artefact ID is returned");
        assertEquals(returnedTableEntity.getProperty(PublicationConfiguration.PROVENANCE_TABLE), PROVENANCE,
                     "The expected Provenance is returned");
        assertEquals(returnedTableEntity.getProperty(PublicationConfiguration.SOURCE_ARTEFACT_ID_TABLE),
                     SOURCE_ARTEFACT_ID, "The expected source artefact ID is returned");
        assertEquals(returnedTableEntity.getProperty(PublicationConfiguration.TYPE_TABLE), ARTEFACT_TYPE,
                     "The expected Artefact type is returned");
        assertEquals(returnedTableEntity.getProperty(PublicationConfiguration.SENSITIVITY_TABLE), SENSITIVITY,
                     "The expected sensitivity is returned");
        assertEquals(returnedTableEntity.getProperty(PublicationConfiguration.LANGUAGE_TABLE), LANGUAGE,
                     "The expected language is returned");
        assertEquals(returnedTableEntity.getProperty(PublicationConfiguration.SEARCH_TABLE), SEARCH,
                     "The expected search parameter is returned");
        assertEquals(returnedTableEntity.getProperty(PublicationConfiguration.DISPLAY_FROM_TABLE), DISPLAY_FROM,
                     "The expected display from date is returned");
        assertEquals(returnedTableEntity.getProperty(PublicationConfiguration.DISPLAY_TO_TABLE), DISPLAY_TO,
                     "The expected display to date is returned");
        assertEquals(returnedTableEntity.getProperty(PublicationConfiguration.PAYLOAD_TABLE), PAYLOAD,
                     "The expected payload is returned");
    }

    @Test
    void testCreationOfErroredArtefact() {

        doThrow(new TableServiceException(EXCEPTION_MESSAGE, null)).when(tableClient).createEntity(any());

        PublicationException publicationException = assertThrows(PublicationException.class, () -> {
            azureTableService.createPublication(artefact);
        });

        assertEquals(EXCEPTION_MESSAGE, publicationException.getMessage(),
                     "The expected exception message is returned");
    }

    @Test
    void testGetPublicationSuccessful() {
        when(tableClient.listEntities(any(), any(), any())).thenReturn(tableEntities);
        when(tableEntities.stream()).thenReturn(Stream.of(tableEntity));

        Optional<Artefact> artefactOptional = azureTableService.getPublication(ARTEFACT_ID);

        assertTrue(artefactOptional.isPresent(), "An artefact has been returned");

        Artefact returnedArtefact = artefactOptional.get();

        assertEquals(tableEntity.getProperty(PublicationConfiguration.ARTIFACT_ID_TABLE),
                     returnedArtefact.getArtefactId(), "The expected artefact ID is returned");
        assertEquals(tableEntity.getProperty(PublicationConfiguration.PROVENANCE_TABLE),
                     returnedArtefact.getProvenance(), "The expected provenance is returned");
        assertEquals(tableEntity.getProperty(PublicationConfiguration.SOURCE_ARTEFACT_ID_TABLE),
                     returnedArtefact.getSourceArtefactId(), "The expected source artefact ID is returned");
        assertEquals(tableEntity.getProperty(PublicationConfiguration.TYPE_TABLE), returnedArtefact.getType(),
                     "The expected artefact type is returned");
        assertEquals(tableEntity.getProperty(PublicationConfiguration.SENSITIVITY_TABLE),
                     returnedArtefact.getSensitivity(), "The expected sensitivity is returned");
        assertEquals(tableEntity.getProperty(PublicationConfiguration.LANGUAGE_TABLE), returnedArtefact.getLanguage(),
                     "The expected language is returned");
        assertEquals(tableEntity.getProperty(PublicationConfiguration.SEARCH_TABLE), returnedArtefact.getSearch(),
                     "The expected seearch paramter is returned");
        assertEquals(tableEntity.getProperty(PublicationConfiguration.DISPLAY_FROM_TABLE),
                     returnedArtefact.getDisplayFrom(), "The expected display from date is returned");
        assertEquals(tableEntity.getProperty(PublicationConfiguration.DISPLAY_TO_TABLE),
                     returnedArtefact.getDisplayTo(), "The expected display to date is returned");
        assertEquals(tableEntity.getProperty(PublicationConfiguration.PAYLOAD_TABLE), returnedArtefact.getPayload(),
                     "The expected payload is returned");
    }

    @Test
    void testGetPublicationNotFound() {

        when(tableClient.listEntities(any(), any(), any())).thenReturn(tableEntities);
        when(tableEntities.stream()).thenReturn(Stream.of());

        Optional<Artefact> artefactOptional = azureTableService.getPublication(ARTEFACT_ID);

        assertFalse(artefactOptional.isPresent(), "No optional is returned");
    }

    @Test
    void testGetPublicationErrored() {

        doThrow(new TableServiceException(EXCEPTION_MESSAGE, null)).when(tableClient).listEntities(any(), any(), any());

        PublicationException publicationException = assertThrows(PublicationException.class, () -> {
            azureTableService.getPublication(ARTEFACT_ID);
        });

        assertEquals(EXCEPTION_MESSAGE, publicationException.getMessage(),
                     "The expected exception message is returned");

    }

    @Test
    void testGetPublicationNoOptionalFields() {

        when(tableClient.listEntities(any(), any(), any())).thenReturn(tableEntities);
        when(tableEntities.stream()).thenReturn(Stream.of(tableEntityNoOptionals));

        Optional<Artefact> artefactOptional = azureTableService.getPublication(ARTEFACT_ID);

        assertTrue(artefactOptional.isPresent(), "The returned artefact is present");

        Artefact returnedArtefact = artefactOptional.get();

        assertEquals(tableEntity.getProperty(PublicationConfiguration.ARTIFACT_ID_TABLE),
                     returnedArtefact.getArtefactId(), "The expected artefact ID is returned");
        assertEquals(tableEntity.getProperty(PublicationConfiguration.PROVENANCE_TABLE),
                     returnedArtefact.getProvenance(), "The expected provenance is returned");
        assertEquals(tableEntity.getProperty(PublicationConfiguration.SOURCE_ARTEFACT_ID_TABLE),
                     returnedArtefact.getSourceArtefactId(), "The expected source artefact ID is returned");
        assertEquals(tableEntity.getProperty(PublicationConfiguration.TYPE_TABLE), returnedArtefact.getType(),
                     "The expected artefact type is returned");
        assertNull(returnedArtefact.getSensitivity(),
                   "The expected sensitivity is returned");
        assertNull(returnedArtefact.getLanguage(),
                   "The expected language is returned");
        assertNull(returnedArtefact.getSearch(),
                   "The expected search parameter is returned");
        assertNull(returnedArtefact.getDisplayFrom(),
                   "The expected display from date is returned");
        assertNull(returnedArtefact.getDisplayTo(),
                   "The expected display to date is returned");
        assertEquals(tableEntity.getProperty(PublicationConfiguration.PAYLOAD_TABLE), returnedArtefact.getPayload(),
                     "The expected payload is returned");
    }

}
