package uk.gov.hmcts.reform.pip.data.management.database;

import com.azure.core.util.BinaryData;
import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AzureBlobServiceTest {

    private static final String SOURCE_ARTEFACT_ID = "1234";
    private static final String PROVENANCE = "abcd";
    private static final String PAYLOAD = "test-payload";
    private static final String CONTAINER_URL = "https://localhost";
    private static final MultipartFile FILE = new MockMultipartFile("test", (byte[]) null);

    @Mock
    BlobContainerClient blobContainerClient;

    @Mock
    BlobClient blobClient;

    @InjectMocks
    AzureBlobService azureBlobService;

    @Test
    void testCreationOfNewBlob() {
        String blobName = SOURCE_ARTEFACT_ID + '-' + PROVENANCE;

        when(blobContainerClient.getBlobClient(blobName)).thenReturn(blobClient);
        when(blobContainerClient.getBlobContainerUrl()).thenReturn(CONTAINER_URL);

        String blobUrl = azureBlobService.createPayload(SOURCE_ARTEFACT_ID, PROVENANCE, PAYLOAD);

        assertEquals(CONTAINER_URL + "/" + blobName, blobUrl, "Payload URL does not"
            + "contain the correct value");
    }

    @Test
    void testGetBlobData() {
        String blobName = SOURCE_ARTEFACT_ID + '-' + PROVENANCE;
        when(blobContainerClient.getBlobClient(blobName)).thenReturn(blobClient);
        doNothing().when(blobClient).downloadStream(any());
        assertThat(
            "Wrong return type - should be string",
            azureBlobService.getBlobData(SOURCE_ARTEFACT_ID, PROVENANCE),
            instanceOf(String.class)
        );
        assertEquals("", azureBlobService.getBlobData(SOURCE_ARTEFACT_ID, PROVENANCE),
                     "Wrong string detected"
        );
    }


    @Test
    void testGetBlobFile() throws IOException {
        String blobName = SOURCE_ARTEFACT_ID + '-' + PROVENANCE;
        BinaryData binaryData = BinaryData.fromString("TestString");
        when(blobContainerClient.getBlobClient(blobName)).thenReturn(blobClient);
        when(blobClient.downloadContent()).thenReturn(binaryData);
        Resource blobFile = azureBlobService.getBlobFile(SOURCE_ARTEFACT_ID, PROVENANCE);
        byte[] data = binaryData.toBytes();
        assertEquals(blobFile, new ByteArrayResource(data), "Wrong data returned.");
    }

    @Test
    void testCreationOfNewBlobViaFile() {
        String blobName = SOURCE_ARTEFACT_ID + '-' + PROVENANCE;

        when(blobContainerClient.getBlobClient(blobName)).thenReturn(blobClient);
        when(blobContainerClient.getBlobContainerUrl()).thenReturn(CONTAINER_URL);

        String blobUrl = azureBlobService.uploadFlatFile(SOURCE_ARTEFACT_ID, PROVENANCE, FILE);

        assertEquals(CONTAINER_URL + "/" + blobName, blobUrl, "Payload URL does not"
            + "contain the correct value");
    }
}
