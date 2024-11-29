package uk.gov.hmcts.reform.pip.data.management.utils;

import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.boot.test.mock.mockito.MockBean;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@MockitoSettings(strictness = Strictness.LENIENT)
public class IntegrationCommonTestBase {
    protected static final String BLOB_PAYLOAD_URL = "https://localhost";

    @MockBean(name = "artefact")
    protected BlobContainerClient artefactBlobContainerClient;

    @MockBean(name = "publications")
    protected BlobContainerClient publicationBlobContainerClient;

    @MockBean
    protected BlobClient blobClient;

    @BeforeEach
    void setupBlobClient() {
        when(artefactBlobContainerClient.getBlobContainerUrl()).thenReturn(BLOB_PAYLOAD_URL);
        when(artefactBlobContainerClient.getBlobClient(any())).thenReturn(blobClient);
        when(publicationBlobContainerClient.getBlobClient(any())).thenReturn(blobClient);
    }
}
