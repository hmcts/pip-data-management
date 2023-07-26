package uk.gov.hmcts.reform.pip.data.management.config;

import com.azure.identity.DefaultAzureCredential;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobContainerClientBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("!test & !functional")
public class AzureBlobConfiguration {
    private static final String BLOB_ENDPOINT = "https://%s.blob.core.windows.net/";

    @Value("${spring.cloud.azure.active-directory.profile.tenant-id}")
    private String tenantId;

    @Value("${azure.managed-identity.client-id}")
    private String managedIdentityClientId;

    @Bean(name = "artefact")
    public BlobContainerClient artefactBlobContainerClient(AzureBlobConfigurationProperties
                                                           azureBlobConfigurationProperties) {
        return configureBlobContainerClient(azureBlobConfigurationProperties,
                                            azureBlobConfigurationProperties.getArtefactContainerName());
    }

    @Bean(name = "publications")
    public BlobContainerClient publicationsBlobContainerClient(AzureBlobConfigurationProperties
                                                               azureBlobConfigurationProperties) {
        return configureBlobContainerClient(azureBlobConfigurationProperties,
                                            azureBlobConfigurationProperties.getPublicationsContainerName());
    }

    private BlobContainerClient configureBlobContainerClient(
        AzureBlobConfigurationProperties azureBlobConfigurationProperties,
        String containerName
    ) {
        if (managedIdentityClientId.isEmpty()) {
            return new BlobContainerClientBuilder()
                .connectionString(azureBlobConfigurationProperties.getConnectionString())
                .containerName(containerName)
                .buildClient();
        }

        DefaultAzureCredential defaultCredential = new DefaultAzureCredentialBuilder()
            .tenantId(tenantId)
            .managedIdentityClientId(managedIdentityClientId)
            .build();

        return new BlobContainerClientBuilder()
            .endpoint(String.format(BLOB_ENDPOINT, azureBlobConfigurationProperties.getStorageAccountName()))
            .credential(defaultCredential)
            .containerName(containerName)
            .buildClient();
    }
}
