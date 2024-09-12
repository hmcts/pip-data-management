package uk.gov.hmcts.reform.pip.data.management.config;

import com.azure.storage.blob.BlobContainerClient;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.env.Environment;
import org.springframework.test.util.ReflectionTestUtils;

import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AzureBlobClientCreationTest {
    private static final String[] BLOB_STORAGE_DEV_PROFILE = {"blobStorageDev"};
    private static final String TENANT_ID = "123";
    private static final String MANAGED_IDENTITY_CLIENT_ID = "456";
    private static final String MANAGED_IDENTITY_CLIENT_ID_KEY = "managedIdentityClientId";
    private static final String LOCAL_STORAGE_ACCOUNT_NAME = "testAccount";
    private static final String LOCAL_BLOB_ENDPOINT = "http://127.0.0.1:10000/" + LOCAL_STORAGE_ACCOUNT_NAME;
    private static final String AZURE_STORAGE_ACCOUNT_NAME = "azureAccount";
    private static final String AZURE_STORAGE_ACCOUNT_KEY = "12345";
    private static final String AZURE_STORAGE_ACCOUNT_URL = "https://"
        + AZURE_STORAGE_ACCOUNT_NAME
        + ".blob.core.windows.net";
    private static final String AZURE_BLOB_ENDPOINT = "https://"
        + AZURE_STORAGE_ACCOUNT_NAME
        + ".blob.core.windows.net";
    private static final String CONNECTION_STRING = "DefaultEndpointsProtocol=http;AccountName="
        + LOCAL_STORAGE_ACCOUNT_NAME
        + "; AccountKey=123/456; BlobEndpoint="
        + LOCAL_BLOB_ENDPOINT;
    private static final String ARTEFACT_CONTAINER_NAME = "artefact";
    private static final String PUBLICATIONS_CONTAINER_NAME = "publications";

    @Mock
    private AzureBlobConfigurationProperties blobConfigProperties;

    @Mock
    private Environment env;

    @InjectMocks
    private AzureBlobConfiguration azureBlobConfiguration;

    @BeforeEach
    void setup() {
        when(env.getActiveProfiles()).thenReturn(new String[1]);
        ReflectionTestUtils.setField(azureBlobConfiguration, "tenantId", TENANT_ID);
    }

    @Test
    void testCreationOfAzureArtefactBlobClientWithDevProfile() {
        when(env.getActiveProfiles()).thenReturn(BLOB_STORAGE_DEV_PROFILE);
        when(blobConfigProperties.getStorageAccountName()).thenReturn(AZURE_STORAGE_ACCOUNT_NAME);
        when(blobConfigProperties.getStorageAccountUrl()).thenReturn(AZURE_STORAGE_ACCOUNT_URL);
        when(blobConfigProperties.getStorageAccountKey()).thenReturn(AZURE_STORAGE_ACCOUNT_KEY);
        when(blobConfigProperties.getArtefactContainerName()).thenReturn(ARTEFACT_CONTAINER_NAME);

        BlobContainerClient blobContainerClient = azureBlobConfiguration.artefactBlobContainerClient(
            blobConfigProperties
        );

        SoftAssertions softly = new SoftAssertions();

        softly.assertThat(blobContainerClient).isNotNull();
        softly.assertThat(blobContainerClient.getAccountUrl()).isEqualTo(AZURE_STORAGE_ACCOUNT_URL);
        softly.assertThat(blobContainerClient.getAccountName()).isEqualTo(AZURE_STORAGE_ACCOUNT_NAME);
        softly.assertThat(blobContainerClient.getBlobContainerName()).isEqualTo(ARTEFACT_CONTAINER_NAME);

        softly.assertAll();
    }

    @Test
    void testCreationOfAzureArtefactBlobClientWithManagedIdentity() {
        ReflectionTestUtils.setField(azureBlobConfiguration, MANAGED_IDENTITY_CLIENT_ID_KEY,
                                     MANAGED_IDENTITY_CLIENT_ID);
        when(env.getActiveProfiles()).thenReturn(new String[1]);
        when(blobConfigProperties.getStorageAccountName()).thenReturn(AZURE_STORAGE_ACCOUNT_NAME);
        when(blobConfigProperties.getArtefactContainerName()).thenReturn(ARTEFACT_CONTAINER_NAME);

        BlobContainerClient blobContainerClient = azureBlobConfiguration.artefactBlobContainerClient(
            blobConfigProperties
        );

        SoftAssertions softly = new SoftAssertions();

        softly.assertThat(blobContainerClient).isNotNull();
        softly.assertThat(blobContainerClient.getAccountUrl()).isEqualTo(AZURE_BLOB_ENDPOINT);
        softly.assertThat(blobContainerClient.getAccountName()).isEqualTo(AZURE_STORAGE_ACCOUNT_NAME);
        softly.assertThat(blobContainerClient.getBlobContainerName()).isEqualTo(ARTEFACT_CONTAINER_NAME);

        softly.assertAll();
    }

    @Test
    void testCreationOfAzureArtefactBlobClientWithoutManagedIdentity() {
        ReflectionTestUtils.setField(azureBlobConfiguration, MANAGED_IDENTITY_CLIENT_ID_KEY, "");
        when(env.getActiveProfiles()).thenReturn(new String[1]);
        when(blobConfigProperties.getConnectionString()).thenReturn(CONNECTION_STRING);
        when(blobConfigProperties.getArtefactContainerName()).thenReturn(ARTEFACT_CONTAINER_NAME);

        BlobContainerClient blobContainerClient = azureBlobConfiguration.artefactBlobContainerClient(
            blobConfigProperties
        );

        SoftAssertions softly = new SoftAssertions();

        softly.assertThat(blobContainerClient).isNotNull();
        softly.assertThat(blobContainerClient.getAccountUrl()).isEqualTo(LOCAL_BLOB_ENDPOINT);
        softly.assertThat(blobContainerClient.getAccountName()).isEqualTo(LOCAL_STORAGE_ACCOUNT_NAME);
        softly.assertThat(blobContainerClient.getBlobContainerName()).isEqualTo(ARTEFACT_CONTAINER_NAME);

        softly.assertAll();
    }

    @Test
    void testCreationOfAzurePublicationsBlobClientWithDevProfile() {
        when(env.getActiveProfiles()).thenReturn(BLOB_STORAGE_DEV_PROFILE);
        when(blobConfigProperties.getStorageAccountName()).thenReturn(AZURE_STORAGE_ACCOUNT_NAME);
        when(blobConfigProperties.getStorageAccountUrl()).thenReturn(AZURE_STORAGE_ACCOUNT_URL);
        when(blobConfigProperties.getStorageAccountKey()).thenReturn(AZURE_STORAGE_ACCOUNT_KEY);
        when(blobConfigProperties.getPublicationsContainerName()).thenReturn(PUBLICATIONS_CONTAINER_NAME);

        BlobContainerClient blobContainerClient = azureBlobConfiguration.publicationsBlobContainerClient(
            blobConfigProperties
        );

        SoftAssertions softly = new SoftAssertions();

        softly.assertThat(blobContainerClient).isNotNull();
        softly.assertThat(blobContainerClient.getAccountUrl()).isEqualTo(AZURE_BLOB_ENDPOINT);
        softly.assertThat(blobContainerClient.getAccountName()).isEqualTo(AZURE_STORAGE_ACCOUNT_NAME);
        softly.assertThat(blobContainerClient.getBlobContainerName()).isEqualTo(PUBLICATIONS_CONTAINER_NAME);

        softly.assertAll();
    }

    @Test
    void testCreationOfAzurePublicationsBlobClientWithManagedIdentity() {
        ReflectionTestUtils.setField(azureBlobConfiguration, MANAGED_IDENTITY_CLIENT_ID_KEY,
                                     MANAGED_IDENTITY_CLIENT_ID);
        when(env.getActiveProfiles()).thenReturn(new String[1]);
        when(blobConfigProperties.getStorageAccountName()).thenReturn(AZURE_STORAGE_ACCOUNT_NAME);
        when(blobConfigProperties.getPublicationsContainerName()).thenReturn(PUBLICATIONS_CONTAINER_NAME);

        BlobContainerClient blobContainerClient = azureBlobConfiguration.publicationsBlobContainerClient(
            blobConfigProperties
        );

        SoftAssertions softly = new SoftAssertions();

        softly.assertThat(blobContainerClient).isNotNull();
        softly.assertThat(blobContainerClient.getAccountUrl()).isEqualTo(AZURE_BLOB_ENDPOINT);
        softly.assertThat(blobContainerClient.getAccountName()).isEqualTo(AZURE_STORAGE_ACCOUNT_NAME);
        softly.assertThat(blobContainerClient.getBlobContainerName()).isEqualTo(PUBLICATIONS_CONTAINER_NAME);

        softly.assertAll();
    }

    @Test
    void testCreationOfAzurePublicationsBlobClientWithoutManagedIdentity() {
        ReflectionTestUtils.setField(azureBlobConfiguration, MANAGED_IDENTITY_CLIENT_ID_KEY, "");
        when(env.getActiveProfiles()).thenReturn(new String[1]);
        when(blobConfigProperties.getConnectionString()).thenReturn(CONNECTION_STRING);
        when(blobConfigProperties.getPublicationsContainerName()).thenReturn(PUBLICATIONS_CONTAINER_NAME);

        BlobContainerClient blobContainerClient = azureBlobConfiguration.publicationsBlobContainerClient(
            blobConfigProperties
        );

        SoftAssertions softly = new SoftAssertions();

        softly.assertThat(blobContainerClient).isNotNull();
        softly.assertThat(blobContainerClient.getAccountUrl()).isEqualTo(LOCAL_BLOB_ENDPOINT);
        softly.assertThat(blobContainerClient.getAccountName()).isEqualTo(LOCAL_STORAGE_ACCOUNT_NAME);
        softly.assertThat(blobContainerClient.getBlobContainerName()).isEqualTo(PUBLICATIONS_CONTAINER_NAME);

        softly.assertAll();
    }
}
