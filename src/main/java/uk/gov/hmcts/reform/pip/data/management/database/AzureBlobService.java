package uk.gov.hmcts.reform.pip.data.management.database;

import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import org.apache.tomcat.util.codec.binary.Base64;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import uk.gov.hmcts.reform.pip.data.management.errorhandling.exceptions.FlatFileException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
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
     *
     * @param sourceArtefactId The source ID to call the blob.
     * @param provenance       The provenance to call the blob.
     * @param payload          The payload to create
     * @return The URL to where the payload has been created
     */
    public String createPayload(String sourceArtefactId, String provenance, String payload) {
        String blobName = sourceArtefactId + '-' + provenance;
        BlobClient blobClient = blobContainerClient.getBlobClient(blobName);

        byte[] payloadBytes = payload.getBytes();
        blobClient.upload(new ByteArrayInputStream(payloadBytes), payloadBytes.length, true);

        return blobContainerClient.getBlobContainerUrl() + '/' + blobName;
    }

    /**
     * Uploads the flat file in the Azure blob service.
     * @param sourceArtefactId  The source ID to call the blob.
     * @param provenance        The provenance to call the blob.
     * @param file              The flat file to upload
     * @return The URL where the file was uploaded.
     */
    public String uploadFlatFile(String sourceArtefactId, String provenance, MultipartFile file) {
        String blobName = sourceArtefactId + '-' + provenance;
        BlobClient blobClient = blobContainerClient.getBlobClient(blobName);

        try {
            blobClient.upload(file.getInputStream(), file.getSize(), true);
        } catch (IOException e) {
            throw new
                FlatFileException("Could not parse provided file, please check supported file types and try again");
        }
        return blobContainerClient.getBlobContainerUrl() + "/" + blobName;
    }

    /**
     * Gets the data held within a blob from the blob service.
     *
     * @param sourceArtefactId The provenance with which to retrieve the blob data
     * @param provenance       The artifact ID with which to retrieve the blob data
     * @return the data contained within the blob in String format.
     */
    public String getBlobData(String sourceArtefactId, String provenance) {
        String blobName = sourceArtefactId + '-' + provenance;
        BlobClient blobClient = blobContainerClient.getBlobClient(blobName);
        byte[] data = blobClient.downloadContent().toBytes();
        String ourString = new String(data, StandardCharsets.ISO_8859_1);
        return ourString;
    }

}
