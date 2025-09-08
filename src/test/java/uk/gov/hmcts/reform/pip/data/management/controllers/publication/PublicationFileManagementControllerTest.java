package uk.gov.hmcts.reform.pip.data.management.controllers.publication;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.pip.data.management.models.PublicationFileSizes;
import uk.gov.hmcts.reform.pip.data.management.service.publication.PublicationFileManagementService;
import uk.gov.hmcts.reform.pip.model.publication.FileType;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
class PublicationFileManagementControllerTest {
    private static final String FILE = "123";
    private static final UUID USER_ID = UUID.randomUUID();
    private static final UUID ARTEFACT_ID = UUID.randomUUID();

    private static final String STATUS_MESSAGE = "Status did not match";
    private static final String RESPONSE_BODY_MESSAGE = "Body did not match";

    @Mock
    private PublicationFileManagementService publicationFileManagementService;

    @InjectMocks
    PublicationFileManagementController publicationFileManagementController;

    @Test
    void testGetFile() {
        when(publicationFileManagementService.getStoredPublication(any(), any(), any(),
            eq(USER_ID.toString()), eq(true), eq(false)
        )).thenReturn(FILE);

        ResponseEntity<String> response = publicationFileManagementController.getFile(
            UUID.randomUUID(), FileType.PDF, USER_ID.toString(), true, false, null
        );

        assertEquals(HttpStatus.OK, response.getStatusCode(), STATUS_MESSAGE);
        assertEquals(FILE, response.getBody(), RESPONSE_BODY_MESSAGE);
    }

    @Test
    void testFileExists() {
        when(publicationFileManagementService.fileExists(ARTEFACT_ID)).thenReturn(true);

        ResponseEntity<Boolean> response = publicationFileManagementController.fileExists(ARTEFACT_ID, USER_ID);
        assertEquals(HttpStatus.OK, response.getStatusCode(), STATUS_MESSAGE);
        assertEquals(true, response.getBody(), RESPONSE_BODY_MESSAGE);
    }

    @Test
    void testGetFileSizes() {
        PublicationFileSizes fileSizes = new PublicationFileSizes(1234L, null, 123L);
        when(publicationFileManagementService.getFileSizes(ARTEFACT_ID)).thenReturn(fileSizes);

        ResponseEntity<PublicationFileSizes> response =
            publicationFileManagementController.getFileSizes(ARTEFACT_ID, USER_ID);
        assertEquals(HttpStatus.OK, response.getStatusCode(), STATUS_MESSAGE);
        assertEquals(fileSizes, response.getBody(), RESPONSE_BODY_MESSAGE);
    }
}
