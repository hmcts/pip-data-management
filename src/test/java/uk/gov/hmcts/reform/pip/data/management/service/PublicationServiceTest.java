package uk.gov.hmcts.reform.pip.data.management.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.pip.data.management.database.AzureTableService;
import uk.gov.hmcts.reform.pip.data.management.errorhandling.exceptions.DuplicatePublicationException;
import uk.gov.hmcts.reform.pip.data.management.models.publication.Artefact;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PublicationServiceTest {

    @Mock
    AzureTableService azureTableService;

    @InjectMocks
    PublicationService publicationService;

    private static final String ARTEFACT_ID = "1234";
    private static final String ROW_ID = "1234-1234";

    @Test
    void testCreationOfNewArtefact() {

        Artefact artefact = Artefact.builder()
            .artefactId(ARTEFACT_ID)
            .build();

        when(azureTableService.getPublication(ARTEFACT_ID)).thenReturn(Optional.empty());
        when(azureTableService.createPublication(artefact)).thenReturn(ROW_ID);

        String returnedUuid = publicationService.createPublication(artefact);

        assertEquals(ROW_ID, returnedUuid, "Row ID must match returned UUID");
    }

    @Test
    void testCreationOfExistingArtefact() {

        Artefact artefact = Artefact.builder()
            .artefactId(ARTEFACT_ID)
            .build();

        when(azureTableService.getPublication(ARTEFACT_ID)).thenReturn(Optional.of(artefact));

        DuplicatePublicationException duplicatePublicationException =
            assertThrows(DuplicatePublicationException.class, () -> publicationService.createPublication(artefact));

        assertEquals(String.format("Duplicate publication found with ID %s", ARTEFACT_ID),
                     duplicatePublicationException.getMessage(), "Exception message "
                         + "must contain expected message");
    }

}
