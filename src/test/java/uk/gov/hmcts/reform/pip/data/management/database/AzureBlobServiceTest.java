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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.pip.data.management.helpers.TestConstants.MESSAGES_MATCH;

@ExtendWith(MockitoExtension.class)
class AzureBlobServiceTest {

    private static final String SOURCE_ARTEFACT_ID = "1234";
    private static final String PROVENANCE = "abcd";
    private static final String PAYLOAD = "test-payload";
    private static final String CONTAINER_URL = "https://localhost";
    private static final MultipartFile FILE = new MockMultipartFile("test", (byte[]) null);
    private static final String BLOB_NAME = SOURCE_ARTEFACT_ID + "-" + PROVENANCE;

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

        String blobUrl = azureBlobService.createPayload(SOURCE_ARTEFACT_ID, PROVENANCE, PAYLOAD);

        assertEquals(CONTAINER_URL + "/" + BLOB_NAME, blobUrl, "Payload URL does not"
            + "contain the correct value");
    }

    @Test
    void testGetBlobData() {
        when(blobClient.downloadContent()).thenReturn(BinaryData.fromString("TestString"));

        String blobData = azureBlobService.getBlobData(SOURCE_ARTEFACT_ID, PROVENANCE);

        assertEquals("TestString", blobData, "Wrong string detected");
    }


    @Test
    void testGetBlobFile() {
        BinaryData binaryData = BinaryData.fromString("TestString");
        when(blobClient.downloadContent()).thenReturn(binaryData);
        Resource blobFile = azureBlobService.getBlobFile(SOURCE_ARTEFACT_ID, PROVENANCE);
        byte[] data = binaryData.toBytes();
        assertEquals(blobFile, new ByteArrayResource(data), "Wrong data returned.");
    }

    @Test
    void testCreationOfNewBlobViaFile() {
        when(blobContainerClient.getBlobContainerUrl()).thenReturn(CONTAINER_URL);

        String blobUrl = azureBlobService.uploadFlatFile(SOURCE_ARTEFACT_ID, PROVENANCE, FILE);

        assertEquals(CONTAINER_URL + "/" + BLOB_NAME, blobUrl, "Payload URL does not"
            + "contain the correct value");
    }

    @Test
    void testDeleteBlob() {
        assertEquals(String.format("Blob: %s successfully deleted.", BLOB_NAME),
                     azureBlobService.deleteBlob(SOURCE_ARTEFACT_ID, PROVENANCE),
                     MESSAGES_MATCH);
    }
}
