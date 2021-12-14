package uk.gov.hmcts.reform.pip.data.management.database;

import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AzureBlobServiceTest {

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
        assertEquals(blobClient, blobContainerClient.getBlobClient(blobName), "Wrong blobclient loaded");
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        String s = "test data here";
        for (int i = 0; i < s.length(); i++) {
            stream.write(s.charAt(i));
        }
        ArgumentCaptor<ByteArrayOutputStream> captor = ArgumentCaptor.forClass(ByteArrayOutputStream.class);
        doNothing().when(blobClient).downloadStream(captor.capture());
        blobClient.downloadStream(stream);
        assertEquals(s, captor.getValue().toString(),
                     "stream writing incorrectly"
        );
    }

    // I want to understand how exactly to manipulate the internal "stream" ByteArrayOutputStream within the
    // getBlobData method. I have looked at several different methods:
    // 1) doAnswer() - this seems to be potentially fruitful but I'm struggling to work out how it could affect the
    // internals.
    // 2) using a spy() within mockito and putting the stream into a protected class so i can watch the value of the
    // stream variable. This seems like another potentially good option, but i basically just need clarification.

}
