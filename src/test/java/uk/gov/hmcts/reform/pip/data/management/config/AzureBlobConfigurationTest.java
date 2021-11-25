package uk.gov.hmcts.reform.pip.data.management.config;

import com.azure.storage.blob.BlobContainerClient;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/**
 * Mock class for testing to mock out external calls to Azure;
 */
@Configuration
@Profile("test")
public class AzureBlobConfigurationTest {

    @Mock
    BlobContainerClient blobContainerClient;

    public AzureBlobConfigurationTest() {
        MockitoAnnotations.openMocks(this);
    }

    @Bean
    public BlobContainerClient blobContainerClient() {
        return blobContainerClient;
    }
}
