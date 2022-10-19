package uk.gov.hmcts.reform.pip.data.management.database;

import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import uk.gov.hmcts.reform.pip.data.management.errorhandling.exceptions.FlatFileException;

import java.io.ByteArrayInputStream;
import java.io.IOException;

/**
 * Class with handles the interaction with the Azure Blob Service.
 */
@Component
public class AzureBlobService {

    private final BlobContainerClient artefactBlobContainerClient;

    private final BlobContainerClient publicationsBlobContainerClient;

    private static final String DELETE_MESSAGE = "Blob: %s successfully deleted.";

    @Autowired
    public AzureBlobService(@Qualifier("artefact") BlobContainerClient artefactBlobContainerClient,
                            @Qualifier("publications") BlobContainerClient publicationsBlobContainerClient) {
        this.artefactBlobContainerClient = artefactBlobContainerClient;
        this.publicationsBlobContainerClient = publicationsBlobContainerClient;
    }

    /**
     * Creates the payload in the Azure blob service.
     *
     * @param payloadId         The identifier of the payload
     * @param payload          The payload to create
     * @return The URL to where the payload has been created
     */
    public String createPayload(String payloadId, String payload) {
        BlobClient blobClient = artefactBlobContainerClient.getBlobClient(payloadId);

        byte[] payloadBytes = payload.getBytes();
        blobClient.upload(new ByteArrayInputStream(payloadBytes), payloadBytes.length, true);

        return artefactBlobContainerClient.getBlobContainerUrl() + '/' + payloadId;
    }

    /**
     * Uploads the flat file in the Azure blob service.
     *
     * @param payloadId         The identifier of the payload
     * @param file             The flat file to upload
     * @return The URL where the file was uploaded.
     */
    public String uploadFlatFile(String payloadId, MultipartFile file) {
        BlobClient blobClient = artefactBlobContainerClient.getBlobClient(payloadId);

        try {
            blobClient.upload(file.getInputStream(), file.getSize(), true);
        } catch (IOException e) {
            throw new
                FlatFileException("Could not parse provided file, please check supported file types and try again");
        }
        return artefactBlobContainerClient.getBlobContainerUrl() + "/" + payloadId;
    }

    /**
     * Gets the data held within a blob from the blob service.
     *
     * @param payloadId the identifier of the payload
     * @return the data contained within the blob in String format.
     */
    public String getBlobData(String payloadId) {
        BlobClient blobClient = artefactBlobContainerClient.getBlobClient(payloadId);
        return blobClient.downloadContent().toString();
    }

    public Resource getBlobFile(String payloadId) {
        BlobClient blobClient = artefactBlobContainerClient.getBlobClient(payloadId);
        byte[] data = blobClient.downloadContent().toBytes();
        return new ByteArrayResource(data);
    }

    public String deleteBlob(String payloadId) {
        BlobClient blobClient = artefactBlobContainerClient.getBlobClient(payloadId);
        blobClient.delete();
        return String.format(DELETE_MESSAGE, payloadId);
    }

    /**
     * Delete a blob in the publications storage container that contain the artefact ID.
     *
     * @param blobName The name of the blob to delete.
     * @return A delete message.
     */
    public String deletePublicationBlob(String blobName) {
        BlobClient blobClient = publicationsBlobContainerClient.getBlobClient(blobName);
        blobClient.delete();
        return String.format(DELETE_MESSAGE, blobName);
    }
}
