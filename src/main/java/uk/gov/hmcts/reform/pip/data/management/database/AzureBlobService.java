package uk.gov.hmcts.reform.pip.data.management.database;

import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.sun.xml.bind.api.impl.NameConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;

/**
 * Class with handles the interaction with the Azure Blob Service.
 */
@Component
public class AzureBlobService {

    private final BlobContainerClient blobContainerClient;

    @Autowired
    public AzureBlobService(BlobContainerClient blobContainerClient) {
        this.blobContainerClient = blobContainerClient;
    }

    /**
     * Creates the payload in the Azure blob service.
     * @param sourceArtefactId The source ID to call the blob.
     * @param provenance The provenance to call the blob.
     * @param payload The payload to create
     * @return The URL to where the payload has been created
     */
    public String createPayload(String sourceArtefactId, String provenance, String payload) {
        String blobName = sourceArtefactId + '-' + provenance;
        BlobClient blobClient = blobContainerClient.getBlobClient(blobName);

        byte[] payloadBytes = payload.getBytes();
        blobClient.upload(new ByteArrayInputStream(payloadBytes), payloadBytes.length, true);

        return blobContainerClient.getBlobContainerUrl() + '/' + blobName;
    }

    public String getBlobData(String sourceArtefactId, String provenance) {
        String blobName = sourceArtefactId + '-' + provenance;
        BlobClient blobClient = blobContainerClient.getBlobClient(blobName);
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        blobClient.downloadStream(stream);
        return stream.toString();
    }

    public String getBlobDataFromUrl(String url) {
        BlobClient blobClient = blobContainerClient.getBlobClient(url);
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        blobClient.downloadStream(stream);
        return stream.toString();
    }

}
