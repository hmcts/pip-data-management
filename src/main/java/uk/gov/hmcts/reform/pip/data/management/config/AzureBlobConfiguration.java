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
@Profile("!test & !integration")
public class AzureBlobConfiguration {
    private static final String BLOB_ENDPOINT = "https://%s.blob.core.windows.net/";

    @Value("${spring.cloud.azure.active-directory.profile.tenant-id}")
    private String tenantId;

    @Value("${azure.managed-identity.client-id}")
    private String managedIdentityClientId;

    @Bean(name = "artefact")
    public BlobContainerClient artefactBlobContainerClient(AzureBlobConfigurationProperties configurationProperties) {
        return configureBlobContainerClient(configurationProperties,
                                            configurationProperties.getArtefactContainerName());
    }

    @Bean(name = "publications")
    public BlobContainerClient publicationsBlobContainerClient(
        AzureBlobConfigurationProperties configurationProperties
    ) {
        return configureBlobContainerClient(configurationProperties,
                                            configurationProperties.getPublicationsContainerName());
    }

    private BlobContainerClient configureBlobContainerClient(AzureBlobConfigurationProperties configurationProperties,
                                                             String containerName) {
        if (managedIdentityClientId.isEmpty()) {
            return new BlobContainerClientBuilder()
                .connectionString(configurationProperties.getConnectionString())
                .containerName(containerName)
                .buildClient();
        }

        DefaultAzureCredential defaultCredential = new DefaultAzureCredentialBuilder()
            .tenantId(tenantId)
            .managedIdentityClientId(managedIdentityClientId)
            .build();

        return new BlobContainerClientBuilder()
            .endpoint(String.format(BLOB_ENDPOINT, configurationProperties.getStorageAccountName()))
            .credential(defaultCredential)
            .containerName(containerName)
            .buildClient();
    }
}
