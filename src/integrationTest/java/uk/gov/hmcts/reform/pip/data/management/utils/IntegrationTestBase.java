package uk.gov.hmcts.reform.pip.data.management.utils;

import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.pip.data.management.service.PublicationServicesService;
import uk.gov.hmcts.reform.pip.data.management.service.SubscriptionManagementService;

//@SpringBootTest(classes = {Application.class, AzureBlobConfigurationTestConfiguration.class},
//    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
//@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class IntegrationTestBase {
    @MockBean
    protected SubscriptionManagementService subscriptionManagementService;

    @MockBean
    protected PublicationServicesService publicationServicesService;

    @MockBean(name = "artefact")
    protected BlobContainerClient artefactBlobContainerClient;

    @MockBean(name = "publications")
    protected BlobContainerClient publicationBlobContainerClient;

    @MockBean
    protected BlobClient blobClient;

//    @BeforeEach
//    void setup() {
//        when(artefactBlobContainerClient.getBlobClient(any())).thenReturn(blobClient);
//        when(artefactBlobContainerClient.getBlobContainerUrl()).thenReturn(BLOB_PAYLOAD_URL);
//        when(publicationBlobContainerClient.getBlobClient(any())).thenReturn(blobClient);
//    }
}
