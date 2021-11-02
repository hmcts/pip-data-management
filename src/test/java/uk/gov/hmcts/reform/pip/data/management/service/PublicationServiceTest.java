package uk.gov.hmcts.reform.pip.data.management.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.pip.data.management.database.AzureTableService;
import uk.gov.hmcts.reform.pip.data.management.models.publication.Artefact;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PublicationServiceTest {

    @Mock
    AzureTableService azureTableService;

    @InjectMocks
    PublicationService publicationService;

    private static final String SOURCE_ARTEFACT_ID = "1234";
    private static final String ROW_ID = "1234-1234";

    @Test
    void testCreationOfNewArtefact() {

        Artefact artefact = Artefact.builder()
            .sourceArtefactId(SOURCE_ARTEFACT_ID)
            .build();

        when(azureTableService.getPublication(SOURCE_ARTEFACT_ID)).thenReturn(Optional.empty());
        when(azureTableService.createPublication(artefact)).thenReturn(ROW_ID);

        String returnedUuid = publicationService.createPublication(artefact);

        assertEquals(ROW_ID, returnedUuid, "Row ID must match returned UUID");
    }

    @Test
    void testUpdatingOfExistingArtefact() {

        Artefact artefact = Artefact.builder()
            .sourceArtefactId(SOURCE_ARTEFACT_ID)
            .build();

        Artefact existingArtefact = Artefact.builder()
            .artefactId(ROW_ID)
            .sourceArtefactId(SOURCE_ARTEFACT_ID)
            .build();

        when(azureTableService.getPublication(SOURCE_ARTEFACT_ID)).thenReturn(Optional.of(existingArtefact));
        when(azureTableService.updatePublication(artefact, existingArtefact)).thenReturn(ROW_ID);

        String returnedUuid = publicationService.createPublication(artefact);

        assertEquals(ROW_ID, returnedUuid, "Row ID must match returned UUID");
    }

}
