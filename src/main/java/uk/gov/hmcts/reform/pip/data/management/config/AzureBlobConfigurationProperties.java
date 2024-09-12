package uk.gov.hmcts.reform.pip.data.management.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Profile;

/**
 * Configuration file for the Blob Service.
 */
@ConfigurationProperties(prefix = "azure.blob")
@Profile("!test")
@Getter
@Setter
public class AzureBlobConfigurationProperties {

    /**
     * The connection string to connect to the azure blob store.
     */
    private String connectionString;

    /**
     * The name of the storage account containing the blob.
     */
    private String storageAccountName;

    /**
     * The url of the storage account containing the blob (only required when running in the dev pod).
     */
    private String storageAccountUrl;

    /**
     * The access key of the storage account containing the blob (only required when running in the dev pod).
     */
    private String storageAccountKey;

    /**
     * The name of the artefact container to connect to.
     */
    private String artefactContainerName;

    /**
     * The name of the publications container to connect to.
     */
    private String publicationsContainerName;
}
