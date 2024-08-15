package uk.gov.hmcts.reform.pip.data.management.database;

import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.models.BlobStorageException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.pip.data.management.errorhandling.exceptions.PublicationFileNotFoundException;

import java.io.ByteArrayInputStream;

import static uk.gov.hmcts.reform.pip.model.LogBuilder.writeLog;

/**
 * Class with handles the interaction with Azure publications blob service.
 */
@Component
@Slf4j
@SuppressWarnings({"PMD.PreserveStackTrace"})
public class AzurePublicationBlobService {
    private final BlobContainerClient blobContainerClient;

    @Autowired
    public AzurePublicationBlobService(@Qualifier("publications") BlobContainerClient blobContainerClient) {
        this.blobContainerClient = blobContainerClient;
    }

    /**
     * Uploads the Excel/PDFs in the Azure blob service.
     *
     * @param payloadId The identifier of the payload
     * @param filePayload The payload of the file to store
     */
    public String uploadFile(String payloadId, byte[] filePayload) {
        BlobClient blobClient = blobContainerClient.getBlobClient(payloadId);

        blobClient.upload(new ByteArrayInputStream(filePayload), filePayload.length, true);

        return payloadId;
    }

    /**
     * Get the file from the blobstore by the fileId.
     *
     * @param fileId The id of the file to retrieve
     * @return The file from the blob store as a byte array
     */
    public byte[] getBlobFile(String fileId) {
        try {
            BlobClient blobClient = blobContainerClient.getBlobClient(fileId);
            return blobClient.downloadContent().toBytes();
        } catch (BlobStorageException e) {
            throw new PublicationFileNotFoundException(String.format("Blob file with id %s not found", fileId));
        }
    }

    /**
     * Delete a blob file in the publications storage container.
     *
     * @param fileName The name of the file to delete.
     */
    public void deleteBlobFile(String fileName) {
        BlobClient blobClient = blobContainerClient.getBlobClient(fileName);
        if (!blobClient.deleteIfExists()) {
            log.info(writeLog(String.format("Blob file with name %s not found", fileName)));
        }
    }

    /**
     * Checks the existence of a blob file.
     *
     * @param fileName The name of the file.
     * @return true if any file exists, else false.
     */
    public boolean blobFileExists(String fileName) {
        BlobClient blobClient = blobContainerClient.getBlobClient(fileName);
        return blobClient.exists();
    }

    /**
     * Retrieves the size of a file in the blob store.
     *
     * @param fileName The name of the file.
     * @return the file size.
     */
    public Long getBlobSize(String fileName) {
        BlobClient blobClient = blobContainerClient.getBlobClient(fileName);
        return blobClient.exists()
            ? blobClient.getProperties().getBlobSize()
            : null;
    }
}
