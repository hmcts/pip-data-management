package uk.gov.hmcts.reform.pip.data.management.database;

import com.azure.core.util.BinaryData;
import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.models.BlobProperties;
import com.azure.storage.blob.models.BlobStorageException;
import nl.altindag.log.LogCaptor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.pip.data.management.errorhandling.exceptions.PublicationFileNotFoundException;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AzurePublicationBlobServiceTest {

    private static final String BLOB_NAME = UUID.randomUUID().toString();

    private static final String MESSAGES_MATCH = "Messages should match";

    private static final byte[] TEST_BYTE = MESSAGES_MATCH.getBytes();

    private static final String FILE_EXISTS_FLAG_MESSAGE = "File exists flag does not match";
    private static final String FILE_SIZE_MESSAGE = "File size does not match";

    @Mock
    BlobContainerClient blobContainerClient;

    @Mock
    BlobClient blobClient;

    @Mock
    BlobProperties blobProperties;

    @InjectMocks
    AzurePublicationBlobService azureBlobService;

    @BeforeEach
    void setup() {
        when(blobContainerClient.getBlobClient(BLOB_NAME)).thenReturn(blobClient);
    }

    @Test
    void testCreationOfNewBlobFile() {
        String response = azureBlobService.uploadFile(BLOB_NAME, TEST_BYTE);

        assertEquals(BLOB_NAME, response, MESSAGES_MATCH);
    }

    @Test
    void testGetBlobFile() {
        BinaryData binaryData = BinaryData.fromString(MESSAGES_MATCH);
        when(blobClient.downloadContent()).thenReturn(binaryData);
        byte[] blobFile = azureBlobService.getBlobFile(BLOB_NAME);

        assertNotNull(blobFile, "Return was null");
    }

    @Test
    void testGetBlobFileNotFound() {
        doThrow(BlobStorageException.class).when(blobClient).downloadContent();

        assertThatThrownBy(() -> azureBlobService.getBlobFile(BLOB_NAME))
            .isInstanceOf(PublicationFileNotFoundException.class)
            .hasMessage(String.format("Blob file with id %s not found", BLOB_NAME));
    }

    @Test
    void testDeleteBlobFileWhenFileExists() {
        try (LogCaptor logCaptor = LogCaptor.forClass(AzurePublicationBlobService.class)) {
            when(blobClient.deleteIfExists()).thenReturn(true);

            azureBlobService.deleteBlobFile(BLOB_NAME);
            assertThat(logCaptor.getInfoLogs())
                .as("Info log should be empty")
                .isEmpty();
        }
    }

    @Test
    void testDeleteBlobFileWhenFileDoesNotExist() {
        try (LogCaptor logCaptor = LogCaptor.forClass(AzurePublicationBlobService.class)) {
            when(blobClient.deleteIfExists()).thenReturn(false);

            azureBlobService.deleteBlobFile(BLOB_NAME);

            assertThat(logCaptor.getInfoLogs())
                .as("Info log should not be empty")
                .isNotEmpty();

            assertThat(logCaptor.getInfoLogs().get(0))
                .as("Log message does not match")
                .contains("Blob file with name " + BLOB_NAME + " not found");
        }
    }

    @Test
    void testBlobFileExists() {
        when(blobClient.exists()).thenReturn(true);
        assertThat(azureBlobService.blobFileExists(BLOB_NAME))
            .as(FILE_EXISTS_FLAG_MESSAGE)
            .isTrue();
    }

    @Test
    void testBlobFileDoesNotExist() {
        when(blobClient.exists()).thenReturn(false);
        assertThat(azureBlobService.blobFileExists(BLOB_NAME))
            .as(FILE_EXISTS_FLAG_MESSAGE)
            .isFalse();
    }

    @Test
    void testGetBlobSizeIfFileExists() {
        when(blobClient.exists()).thenReturn(true);
        when(blobClient.getProperties()).thenReturn(blobProperties);
        when(blobProperties.getBlobSize()).thenReturn(123L);

        assertThat(azureBlobService.getBlobSize(BLOB_NAME))
            .as(FILE_SIZE_MESSAGE)
            .isEqualTo(123L);
    }

    @Test
    void testGetBlobSizeIfFileDoesNotExist() {
        when(blobClient.exists()).thenReturn(false);
        assertThat(azureBlobService.getBlobSize(BLOB_NAME))
            .as(FILE_SIZE_MESSAGE)
            .isNull();
    }
}
