package uk.gov.hmcts.reform.pip.data.management.database;

import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.models.BlobStorageException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import uk.gov.hmcts.reform.pip.data.management.errorhandling.exceptions.FlatFileException;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import static uk.gov.hmcts.reform.pip.model.LogBuilder.writeLog;

/**
 * Class with handles the interaction with Azure artefact blob service.
 */
@Component
@Slf4j
public class AzureArtefactBlobService {

    private final BlobContainerClient blobContainerClient;

    private static final String DELETE_MESSAGE = "Blob: %s successfully deleted.";

    @Autowired
    public AzureArtefactBlobService(@Qualifier("artefact") BlobContainerClient blobContainerClient) {
        this.blobContainerClient = blobContainerClient;
    }

    /**
     * Creates the payload in the Azure blob service.
     *
     * @param payloadId         The identifier of the payload
     * @param payload          The payload to create
     * @return The URL to where the payload has been created
     */
    public String createPayload(String payloadId, String payload) {
        System.out.println("*****Blob 1");
        BlobClient blobClient = blobContainerClient.getBlobClient(payloadId);

        System.out.println("*****Blob 2");

        byte[] payloadBytes = payload.getBytes();
        blobClient.upload(new ByteArrayInputStream(payloadBytes), payloadBytes.length, true);

        return blobContainerClient.getBlobContainerUrl() + '/' + payloadId;
    }

    /**
     * Uploads the flat file in the Azure blob service.
     *
     * @param payloadId         The identifier of the payload
     * @param file             The flat file to upload
     * @return The URL where the file was uploaded.
     */
    public String uploadFlatFile(String payloadId, MultipartFile file) {
        BlobClient blobClient = blobContainerClient.getBlobClient(payloadId);

        try {
            blobClient.upload(file.getInputStream(), file.getSize(), true);
        } catch (IOException e) {
            throw new
                FlatFileException("Could not parse provided file, please check supported file types and try again");
        }
        return blobContainerClient.getBlobContainerUrl() + "/" + payloadId;
    }

    /**
     * Gets the data held within a blob from the blob service.
     *
     * @param payloadId the identifier of the payload
     * @return the data contained within the blob in String format.
     */
    public String getBlobData(String payloadId) {
        System.out.println("*****Blob 3");
        BlobClient blobClient = blobContainerClient.getBlobClient(payloadId);
        return blobClient.downloadContent().toString();
    }

    public Resource getBlobFile(String payloadId) {
        System.out.println("*****Blob 4");
        BlobClient blobClient = blobContainerClient.getBlobClient(payloadId);
        byte[] data = blobClient.downloadContent().toBytes();
        return new ByteArrayResource(data);
    }

    public String deleteBlob(String payloadId) {
        try {
            BlobClient blobClient = blobContainerClient.getBlobClient(payloadId);
            blobClient.delete();
            return String.format(DELETE_MESSAGE, payloadId);
        } catch (BlobStorageException e) {
            log.error(writeLog(
                String.format("Blob with payload ID %s failed to delete with trace %s", payloadId, e.getMessage())
            ));
            return String.format("Blob failed to delete with ID %s", payloadId);
        }
    }
}
