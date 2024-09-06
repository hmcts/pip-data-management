package uk.gov.hmcts.reform.pip.data.management.config;

import com.azure.identity.DefaultAzureCredential;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobContainerClientBuilder;
import com.azure.storage.common.StorageSharedKeyCredential;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;

@Profile("functional")
public class AzureBlobTestConfiguration {
    private static final String BLOB_ENDPOINT = "https://%s.blob.core.windows.net/";

    @Value("${azure.blob.storage-account-name}")
    private String storageAccountName;

    @Value("${azure.blob.storage-account-url}")
    private String storageAccountUrl;

    @Value("${azure.blob.storage-account-key}")
    private String storageAccountKey;

    @Value("${azure.blob.connection-string}")
    private String storageAccountConnectionString;

    @Value("${spring.cloud.azure.active-directory.profile.tenant-id}")
    private String tenantId;

    @Value("${azure.managed-identity.client-id}")
    private String managedIdentityClientId;

    @Value("${test-env}")
    private String testEnv;

    @Bean(name = "artefact")
    public BlobContainerClient artefactBlobContainerClient() {
        return configureBlobContainerClient("artefact");
    }

    @Bean(name = "publications")
    public BlobContainerClient publicationsBlobContainerClient() {
        return configureBlobContainerClient("publications");
    }

    private BlobContainerClient configureBlobContainerClient(String containerName) {
        if ("dev".equals(testEnv)) {
            StorageSharedKeyCredential storageCredential = new StorageSharedKeyCredential(
                storageAccountName,
                storageAccountKey
            );

            return new BlobContainerClientBuilder()
                .endpoint(storageAccountUrl)
                .credential(storageCredential)
                .containerName(containerName)
                .buildClient();
        } else if (managedIdentityClientId.isEmpty()) {
            return new BlobContainerClientBuilder()
                .connectionString(storageAccountConnectionString)
                .containerName(containerName)
                .buildClient();
        }

        DefaultAzureCredential defaultCredential = new DefaultAzureCredentialBuilder()
            .tenantId(tenantId)
            .managedIdentityClientId(managedIdentityClientId)
            .build();

        return new BlobContainerClientBuilder()
            .endpoint(String.format(BLOB_ENDPOINT, storageAccountName))
            .credential(defaultCredential)
            .containerName(containerName)
            .buildClient();
    }
}
