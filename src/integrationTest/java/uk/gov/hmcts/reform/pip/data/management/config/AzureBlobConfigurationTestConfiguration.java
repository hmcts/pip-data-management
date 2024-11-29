package uk.gov.hmcts.reform.pip.data.management.config;

import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

/**
 * Mock class for testing to mock out external calls to Azure.
 */
@Profile({"integration", "integration-basic"})
public class AzureBlobConfigurationTestConfiguration {

    @Mock
    BlobClient blobClientMock;

    @Mock
    BlobContainerClient artefactBlobContainerClientMock;

    @Mock
    BlobContainerClient publicationsBlobContainerClientMock;

    public AzureBlobConfigurationTestConfiguration() {
        MockitoAnnotations.openMocks(this);
    }

    @Bean(name = "artefact")
    @Primary
    public BlobContainerClient artefactBlobContainerClient() {
        return artefactBlobContainerClientMock;
    }

    @Bean(name = "publications")
    public BlobContainerClient publicationsBlobContainerClient() {
        return publicationsBlobContainerClientMock;
    }

    @Bean
    public BlobClient blobClient() {
        return blobClientMock;
    }

}
