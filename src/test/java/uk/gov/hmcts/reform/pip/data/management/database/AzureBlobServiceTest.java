package uk.gov.hmcts.reform.pip.data.management.database;

import com.azure.core.util.BinaryData;
import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.pip.data.management.helpers.ConstantsTestHelper.MESSAGES_MATCH;

@ExtendWith(MockitoExtension.class)
class AzureBlobServiceTest {

    private static final String PAYLOAD = "test-payload";
    private static final String CONTAINER_URL = "https://localhost";
    private static final MultipartFile FILE = new MockMultipartFile("test", (byte[]) null);
    private static final String BLOB_NAME = UUID.randomUUID().toString();

    @Mock
    BlobContainerClient blobContainerClient;

    @Mock
    BlobClient blobClient;

    @InjectMocks
    AzureBlobService azureBlobService;

    @BeforeEach
    void setup() {
        when(blobContainerClient.getBlobClient(BLOB_NAME)).thenReturn(blobClient);
    }

    @Test
    void testCreationOfNewBlob() {
        when(blobContainerClient.getBlobContainerUrl()).thenReturn(CONTAINER_URL);

        String blobUrl = azureBlobService.createPayload(BLOB_NAME, PAYLOAD);

        assertEquals(CONTAINER_URL + "/" + BLOB_NAME, blobUrl, "Payload URL does not"
            + "contain the correct value");
    }

    @Test
    void testGetBlobData() {
        when(blobClient.downloadContent()).thenReturn(BinaryData.fromString("TestString"));

        String blobData = azureBlobService.getBlobData(BLOB_NAME);

        assertEquals("TestString", blobData, "Wrong string detected");
    }


    @Test
    void testGetBlobFile() {
        BinaryData binaryData = BinaryData.fromString("TestString");
        when(blobClient.downloadContent()).thenReturn(binaryData);
        Resource blobFile = azureBlobService.getBlobFile(BLOB_NAME);
        byte[] data = binaryData.toBytes();
        assertEquals(blobFile, new ByteArrayResource(data), "Wrong data returned.");
    }

    @Test
    void testCreationOfNewBlobViaFile() {
        when(blobContainerClient.getBlobContainerUrl()).thenReturn(CONTAINER_URL);

        String blobUrl = azureBlobService.uploadFlatFile(BLOB_NAME, FILE);

        assertEquals(CONTAINER_URL + "/" + BLOB_NAME, blobUrl, "Payload URL does not"
            + "contain the correct value");
    }

    @Test
    void testDeleteBlob() {
        assertEquals(String.format("Blob: %s successfully deleted.", BLOB_NAME),
                     azureBlobService.deleteBlob(BLOB_NAME),
                     MESSAGES_MATCH);
    }

    @Test
    void testDeletePublicationBlob() {
        assertEquals(String.format("Blob: %s successfully deleted.", BLOB_NAME),
                     azureBlobService.deletePublicationBlob(BLOB_NAME),
                     MESSAGES_MATCH);
    }
}
