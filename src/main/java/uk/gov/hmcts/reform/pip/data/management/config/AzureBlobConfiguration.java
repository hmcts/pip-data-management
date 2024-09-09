package uk.gov.hmcts.reform.pip.data.management.config;

import com.azure.identity.DefaultAzureCredential;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobContainerClientBuilder;
import com.azure.storage.common.StorageSharedKeyCredential;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;

import java.util.Arrays;

@Configuration
@Profile("!test & !integration & !functional")
@SuppressWarnings("PMD.SystemPrintln")
public class AzureBlobConfiguration {
    private static final String BLOB_ENDPOINT = "https://%s.blob.core.windows.net/";
    private static final String DEV_PROFILE = "blobStorageDev";

    @Autowired
    private Environment env;

    @Value("${spring.cloud.azure.active-directory.profile.tenant-id}")
    private String tenantId;

    @Value("${azure.managed-identity.client-id}")
    private String managedIdentityClientId;

    @Value("${azure.blob.storage-account-name}")
    private String storageAccountName;

    @Value("${azure.blob.storage-account-url}")
    private String storageAccountUrl;

    @Value("${azure.blob.storage-account-key}")
    private String storageAccountKey;

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
        System.out.println("Storage account name: " + storageAccountName);
        System.out.println("Storage account url: " + storageAccountUrl);
        System.out.println("Storage account key: " + storageAccountKey);

        System.out.println("Active profile: " + env.getActiveProfiles());

        if (Arrays.stream(env.getActiveProfiles())
            .anyMatch(DEV_PROFILE::equals)) {
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
