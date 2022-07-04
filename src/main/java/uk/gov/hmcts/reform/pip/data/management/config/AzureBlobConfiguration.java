package uk.gov.hmcts.reform.pip.data.management.config;

import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobContainerClientBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@Profile("!test & !functional")
@EnableScheduling
public class AzureBlobConfiguration {

    @Bean
    public BlobContainerClient blobContainerClient(AzureBlobConfigurationProperties
                                                           azureBlobConfigurationProperties) {
        return new BlobContainerClientBuilder()
            .connectionString(azureBlobConfigurationProperties.getConnectionString())
            .containerName(azureBlobConfigurationProperties.getContainerName())
            .buildClient();
    }

}
