package uk.gov.hmcts.reform.pip.data.management.config;

import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobContainerClientBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("!test & !functional")
public class AzureBlobConfiguration {

    @Bean(name = "artefact")
    public BlobContainerClient artefactBlobContainerClient(AzureBlobConfigurationProperties
                                                           azureBlobConfigurationProperties) {
        return new BlobContainerClientBuilder()
            .connectionString(azureBlobConfigurationProperties.getConnectionString())
            .containerName(azureBlobConfigurationProperties.getArtefactContainerName())
            .buildClient();
    }

    @Bean(name = "publications")
    public BlobContainerClient publicationsBlobContainerClient(AzureBlobConfigurationProperties
                                                               azureBlobConfigurationProperties) {
        return new BlobContainerClientBuilder()
            .connectionString(azureBlobConfigurationProperties.getConnectionString())
            .containerName(azureBlobConfigurationProperties.getPublicationsContainerName())
            .buildClient();
    }
}
