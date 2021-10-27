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
import uk.gov.hmcts.reform.pip.data.management.errorhandling.exceptions.PublicationException;
import uk.gov.hmcts.reform.pip.data.management.models.publication.Artefact;
import uk.gov.hmcts.reform.pip.data.management.models.publication.ArtefactType;
import uk.gov.hmcts.reform.pip.data.management.models.publication.Language;
import uk.gov.hmcts.reform.pip.data.management.models.publication.Sensitivity;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AzureTableServiceTest {

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

    private static final String TABLE_ARTEFACT_ID = "artefactId";
    private static final String TABLE_PROVENANCE = "provenance";
    private static final String TABLE_SOURCE_ARTEFACT_ID = "sourceArtefactId";
    private static final String TABLE_TYPE = "type";
    private static final String TABLE_SENSITIVITY = "sensitivity";
    private static final String TABLE_LANGUAGE = "language";
    private static final String TABLE_SEARCH = "search";
    private static final String TABLE_DISPLAY_FROM = "displayFrom";
    private static final String TABLE_DISPLAY_TO = "displayTo";
    private static final String TABLE_PAYLOAD = "payload";

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

        Map<String, Object> entityProperties = new HashMap<>();
        entityProperties.put(TABLE_ARTEFACT_ID, ARTEFACT_ID);
        entityProperties.put(TABLE_PROVENANCE, PROVENANCE);
        entityProperties.put(TABLE_SOURCE_ARTEFACT_ID, SOURCE_ARTEFACT_ID);
        entityProperties.put(TABLE_TYPE, ARTEFACT_TYPE);
        entityProperties.put(TABLE_PAYLOAD, PAYLOAD);

        tableEntityNoOptionals = ModelHelper.createEntity(entityProperties);

        entityProperties.put(TABLE_SENSITIVITY, SENSITIVITY);
        entityProperties.put(TABLE_LANGUAGE, LANGUAGE);
        entityProperties.put(TABLE_SEARCH, SEARCH);
        entityProperties.put(TABLE_DISPLAY_FROM, DISPLAY_FROM);
        entityProperties.put(TABLE_DISPLAY_TO, DISPLAY_TO);

        tableEntity = ModelHelper.createEntity(entityProperties);
    }

    @Test
    void testCreationOfValidArtifact() {

        ArgumentCaptor<TableEntity> argumentCaptor = ArgumentCaptor.forClass(TableEntity.class);

        azureTableService.createPublication(artefact);

        verify(tableClient, times(1)).createEntity(argumentCaptor.capture());

        TableEntity returnedTableEntity = argumentCaptor.getValue();

        assertEquals(returnedTableEntity.getProperty(TABLE_ARTEFACT_ID), ARTEFACT_ID);
        assertEquals(returnedTableEntity.getProperty(TABLE_PROVENANCE), PROVENANCE);
        assertEquals(returnedTableEntity.getProperty(TABLE_SOURCE_ARTEFACT_ID), SOURCE_ARTEFACT_ID);
        assertEquals(returnedTableEntity.getProperty(TABLE_TYPE), ARTEFACT_TYPE);
        assertEquals(returnedTableEntity.getProperty(TABLE_SENSITIVITY), SENSITIVITY);
        assertEquals(returnedTableEntity.getProperty(TABLE_LANGUAGE), LANGUAGE);
        assertEquals(returnedTableEntity.getProperty(TABLE_SEARCH), SEARCH);
        assertEquals(returnedTableEntity.getProperty(TABLE_DISPLAY_FROM), DISPLAY_FROM);
        assertEquals(returnedTableEntity.getProperty(TABLE_DISPLAY_TO), DISPLAY_TO);
        assertEquals(returnedTableEntity.getProperty(TABLE_PAYLOAD), PAYLOAD);
    }

    @Test
    void testCreationOfErroredArtefact() {

        doThrow(new TableServiceException(EXCEPTION_MESSAGE, null)).when(tableClient).createEntity(any());

        PublicationException publicationException = assertThrows(PublicationException.class, () -> {
            azureTableService.createPublication(artefact);
        });

        assertEquals(EXCEPTION_MESSAGE, publicationException.getMessage());
    }

    @Test
    void testGetPublicationSuccessful() {
        when(tableClient.listEntities(any(), any(), any())).thenReturn(tableEntities);
        when(tableEntities.stream()).thenReturn(Stream.of(tableEntity));

        Optional<Artefact> artefactOptional = azureTableService.getPublication(ARTEFACT_ID);

        assertTrue(artefactOptional.isPresent());

        Artefact returnedArtefact = artefactOptional.get();

        assertEquals(tableEntity.getProperty(TABLE_ARTEFACT_ID), returnedArtefact.getArtefactId());
        assertEquals(tableEntity.getProperty(TABLE_PROVENANCE), returnedArtefact.getProvenance());
        assertEquals(tableEntity.getProperty(TABLE_SOURCE_ARTEFACT_ID), returnedArtefact.getSourceArtefactId());
        assertEquals(tableEntity.getProperty(TABLE_TYPE), returnedArtefact.getType());
        assertEquals(tableEntity.getProperty(TABLE_SENSITIVITY), returnedArtefact.getSensitivity());
        assertEquals(tableEntity.getProperty(TABLE_LANGUAGE), returnedArtefact.getLanguage());
        assertEquals(tableEntity.getProperty(TABLE_SEARCH), returnedArtefact.getSearch());
        assertEquals(tableEntity.getProperty(TABLE_DISPLAY_FROM), returnedArtefact.getDisplayFrom());
        assertEquals(tableEntity.getProperty(TABLE_DISPLAY_TO), returnedArtefact.getDisplayTo());
        assertEquals(tableEntity.getProperty(TABLE_PAYLOAD), returnedArtefact.getPayload());
    }

    @Test
    void testGetPublicationNotFound() {

        when(tableClient.listEntities(any(), any(), any())).thenReturn(tableEntities);
        when(tableEntities.stream()).thenReturn(Stream.of());

        Optional<Artefact> artefactOptional = azureTableService.getPublication(ARTEFACT_ID);

        assertFalse(artefactOptional.isPresent());
    }

    @Test
    void testGetPublicationNoOptionalFields() {

        when(tableClient.listEntities(any(), any(), any())).thenReturn(tableEntities);
        when(tableEntities.stream()).thenReturn(Stream.of(tableEntityNoOptionals));

        Optional<Artefact> artefactOptional = azureTableService.getPublication(ARTEFACT_ID);

        assertTrue(artefactOptional.isPresent());

        Artefact returnedArtefact = artefactOptional.get();

        assertEquals(tableEntity.getProperty(TABLE_ARTEFACT_ID), returnedArtefact.getArtefactId());
        assertEquals(tableEntity.getProperty(TABLE_PROVENANCE), returnedArtefact.getProvenance());
        assertEquals(tableEntity.getProperty(TABLE_SOURCE_ARTEFACT_ID), returnedArtefact.getSourceArtefactId());
        assertEquals(tableEntity.getProperty(TABLE_TYPE), returnedArtefact.getType());
        assertNull(returnedArtefact.getSensitivity());
        assertNull(returnedArtefact.getLanguage());
        assertNull(returnedArtefact.getSearch());
        assertNull(returnedArtefact.getDisplayFrom());
        assertNull(returnedArtefact.getDisplayTo());
        assertEquals(tableEntity.getProperty(TABLE_PAYLOAD), returnedArtefact.getPayload());
    }

}
