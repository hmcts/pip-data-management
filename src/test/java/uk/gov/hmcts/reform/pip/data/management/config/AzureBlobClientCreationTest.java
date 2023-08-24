package uk.gov.hmcts.reform.pip.data.management.config;

import com.azure.storage.blob.BlobContainerClient;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AzureBlobClientCreationTest {
    private static final String TENANT_ID = "123";
    private static final String MANAGED_IDENTITY_CLIENT_ID = "456";
    private static final String MANAGED_IDENTITY_CLIENT_ID_KEY = "managedIdentityClientId";
    private static final String LOCAL_STORAGE_ACCOUNT_NAME = "testAccount";
    private static final String LOCAL_BLOB_ENDPOINT = "http://127.0.0.1:10000/" + LOCAL_STORAGE_ACCOUNT_NAME;
    private static final String AZURE_STORAGE_ACCOUNT_NAME = "azureAccount";
    private static final String AZURE_BLOB_ENDPOINT = "https://"
        + AZURE_STORAGE_ACCOUNT_NAME
        + ".blob.core.windows.net";
    private static final String CONNECTION_STRING = "DefaultEndpointsProtocol=http;AccountName="
        + LOCAL_STORAGE_ACCOUNT_NAME
        + "; AccountKey=123/456; BlobEndpoint="
        + LOCAL_BLOB_ENDPOINT;
    private static final String CONTAINER_NAME = "artefact";

    @Mock
    private AzureBlobConfigurationProperties blobConfigProperties;

    @InjectMocks
    private AzureBlobConfiguration azureBlobConfiguration;

    @BeforeEach
    void setup() {
        ReflectionTestUtils.setField(azureBlobConfiguration, "tenantId", TENANT_ID);
    }

    @Test
    void testCreationOfAzureBlobClientWithManagedIdentity() {
        ReflectionTestUtils.setField(azureBlobConfiguration, MANAGED_IDENTITY_CLIENT_ID_KEY,
                                     MANAGED_IDENTITY_CLIENT_ID);
        when(blobConfigProperties.getStorageAccountName()).thenReturn(AZURE_STORAGE_ACCOUNT_NAME);
        when(blobConfigProperties.getContainerName()).thenReturn(CONTAINER_NAME);

        BlobContainerClient blobContainerClient = azureBlobConfiguration.blobContainerClient(blobConfigProperties);

        SoftAssertions softly = new SoftAssertions();

        softly.assertThat(blobContainerClient).isNotNull();
        softly.assertThat(blobContainerClient.getAccountUrl()).isEqualTo(AZURE_BLOB_ENDPOINT);
        softly.assertThat(blobContainerClient.getAccountName()).isEqualTo(AZURE_STORAGE_ACCOUNT_NAME);
        softly.assertThat(blobContainerClient.getBlobContainerName()).isEqualTo(CONTAINER_NAME);

        softly.assertAll();
    }

    @Test
    void testCreationOfAzureBlobClientWithoutManagedIdentity() {
        ReflectionTestUtils.setField(azureBlobConfiguration, MANAGED_IDENTITY_CLIENT_ID_KEY, "");
        when(blobConfigProperties.getConnectionString()).thenReturn(CONNECTION_STRING);
        when(blobConfigProperties.getContainerName()).thenReturn(CONTAINER_NAME);

        BlobContainerClient blobContainerClient = azureBlobConfiguration.blobContainerClient(blobConfigProperties);

        SoftAssertions softly = new SoftAssertions();

        softly.assertThat(blobContainerClient).isNotNull();
        softly.assertThat(blobContainerClient.getAccountUrl()).isEqualTo(LOCAL_BLOB_ENDPOINT);
        softly.assertThat(blobContainerClient.getAccountName()).isEqualTo(LOCAL_STORAGE_ACCOUNT_NAME);
        softly.assertThat(blobContainerClient.getBlobContainerName()).isEqualTo(CONTAINER_NAME);

        softly.assertAll();
    }
}
