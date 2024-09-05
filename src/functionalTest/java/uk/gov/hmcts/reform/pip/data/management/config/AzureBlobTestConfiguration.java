package uk.gov.hmcts.reform.pip.data.management.config;

import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobContainerClientBuilder;
import com.azure.storage.common.StorageSharedKeyCredential;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;

@Profile("functional")
public class AzureBlobTestConfiguration {
    @Value("${STORAGE_ACCOUNT_NAME}")
    private String storageAccountName;

    @Value("${STORAGE_ACCOUNT_URL}")
    private String storageAccountUrl;

    @Value("${STORAGE_ACCOUNT_KEY}")
    private String storageAccountKey;

    @Bean(name = "artefact")
    public BlobContainerClient artefactBlobContainerClient() {
        return configureBlobContainerClient("artefact");
    }

    @Bean(name = "publications")
    public BlobContainerClient publicationsBlobContainerClient() {
        return configureBlobContainerClient("publications");
    }

    private BlobContainerClient configureBlobContainerClient(String containerName) {
        StorageSharedKeyCredential storageCredential = new StorageSharedKeyCredential(
            storageAccountName,
            storageAccountKey
        );

        return new BlobContainerClientBuilder()
            .endpoint(storageAccountUrl)
            .credential(storageCredential)
            .containerName(containerName)
            .buildClient();
    }
}
