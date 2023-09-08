package uk.gov.hmcts.reform.pip.data.management.config;

import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/**
 * Mock class for testing to mock out external calls to Azure.
 */
@Configuration
@Profile("functional")
public class AzureBlobConfigurationTestConfiguration {

    @Mock
    BlobClient blobClientMock;

    @Mock
    BlobContainerClient blobContainerClientMock;

    public AzureBlobConfigurationTestConfiguration() {
        MockitoAnnotations.openMocks(this);
    }

    @Bean
    public BlobContainerClient blobContainerClient() {
        return blobContainerClientMock;
    }

    @Bean
    public BlobClient blobClient() {
        return blobClientMock;
    }

}
