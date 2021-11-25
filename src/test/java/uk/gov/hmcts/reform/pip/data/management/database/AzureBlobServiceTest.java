package uk.gov.hmcts.reform.pip.data.management.database;

import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class AzureBlobServiceTest {

    private static final String SOURCE_ARTEFACT_ID = "1234";
    private static final String PROVENANCE = "abcd";
    private static final String PAYLOAD = "test-payload";
    private static final String CONTAINER_URL = "https://localhost";

    @Mock
    BlobContainerClient blobContainerClient;

    @Mock
    BlobClient blobClient;

    @InjectMocks
    AzureBlobService azureBlobService;

    @Test
    public void testCreationOfNewBlob() {
        String blobName = SOURCE_ARTEFACT_ID + '-' + PROVENANCE;

        when(blobContainerClient.getBlobClient(blobName)).thenReturn(blobClient);
        when(blobContainerClient.getBlobContainerUrl()).thenReturn(CONTAINER_URL);

        String blobUrl = azureBlobService.createPayload(SOURCE_ARTEFACT_ID, PROVENANCE, PAYLOAD);

        assertEquals(CONTAINER_URL + "/" + blobName, blobUrl);
    }
}
