package uk.gov.hmcts.reform.pip.data.management.service.publication;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.pip.data.management.database.AzurePublicationBlobService;
import uk.gov.hmcts.reform.pip.data.management.errorhandling.exceptions.FileSizeLimitException;
import uk.gov.hmcts.reform.pip.data.management.errorhandling.exceptions.UnauthorisedRequestException;
import uk.gov.hmcts.reform.pip.data.management.models.PublicationFileSizes;
import uk.gov.hmcts.reform.pip.data.management.models.publication.Artefact;
import uk.gov.hmcts.reform.pip.data.management.service.AccountManagementService;
import uk.gov.hmcts.reform.pip.model.publication.FileType;
import uk.gov.hmcts.reform.pip.model.publication.Language;
import uk.gov.hmcts.reform.pip.model.publication.ListType;
import uk.gov.hmcts.reform.pip.model.publication.Sensitivity;

import java.util.Base64;
import java.util.UUID;

import static uk.gov.hmcts.reform.pip.model.publication.FileType.EXCEL;
import static uk.gov.hmcts.reform.pip.model.publication.FileType.PDF;

@Slf4j
@Service
public class PublicationFileManagementService {
    private static final String ADDITIONAL_PDF_SUFFIX = "_cy";

    private final AzurePublicationBlobService azureBlobService;
    private final PublicationRetrievalService publicationRetrievalService;
    private final AccountManagementService accountManagementService;
    private final PublicationFileGenerationService publicationFileGenerationService;

    @Autowired
    public PublicationFileManagementService(AzurePublicationBlobService azureBlobService,
                                            PublicationRetrievalService publicationRetrievalService,
                                            AccountManagementService accountManagementService,
                                            PublicationFileGenerationService publicationFileGenerationService) {
        this.azureBlobService = azureBlobService;
        this.publicationRetrievalService = publicationRetrievalService;
        this.accountManagementService = accountManagementService;
        this.publicationFileGenerationService = publicationFileGenerationService;
    }

    /**
     * Generate and store the PDF/Excel files for a given artefact.
     *
     * @param artefactId The artefact ID to generate the files for.
     * @param payload The payload of the artefact.
     */
    public void generateFiles(UUID artefactId, String payload) {
        publicationFileGenerationService.generate(artefactId, payload)
            .ifPresent(files -> {
                if (files.getPrimaryPdf().length > 0) {
                    azureBlobService.uploadFile(artefactId + PDF.getExtension(), files.getPrimaryPdf());
                }

                if (files.getAdditionalPdf().length > 0) {
                    azureBlobService.uploadFile(artefactId + ADDITIONAL_PDF_SUFFIX + PDF.getExtension(),
                                                files.getAdditionalPdf());
                }

                if (files.getExcel().length > 0) {
                    azureBlobService.uploadFile(artefactId + EXCEL.getExtension(), files.getExcel());
                }
            });
    }

    /**
     * Get the stored file (PDF or Excel) for an artefact.
     *
     * @param artefactId The artefact ID to get the file for.
     * @param fileType The type of File. Can be either PDF or Excel.
     * @param maxFileSize The file size limit to return the file.
     * @param userId The ID of user performing the operation.
     * @param system Is system user?
     * @param additionalPdf Is getting the additional Welsh PDF?
     * @return A Base64 encoded string of the file.
     */
    public String getStoredPublication(UUID artefactId, FileType fileType, Integer maxFileSize, String userId,
                                       boolean system, boolean additionalPdf) {
        Artefact artefact = publicationRetrievalService.getMetadataByArtefactId(artefactId);
        if (!isAuthorised(artefact, userId, system)) {
            throw new UnauthorisedRequestException(
                String.format("User with id %s is not authorised to access artefact with id %s", userId, artefactId)
            );
        }

        String filename = fileType == PDF && additionalPdf
            ? artefactId + ADDITIONAL_PDF_SUFFIX : artefactId.toString();
        byte[] file = azureBlobService.getBlobFile(filename + fileType.getExtension());
        if (maxFileSize != null && file.length > maxFileSize) {
            throw new FileSizeLimitException(
                String.format("File with type %s for artefact with id %s has size over the limit of %s bytes",
                              fileType, artefactId, maxFileSize)
            );
        }
        return Base64.getEncoder().encodeToString(file);
    }

    /**
     * Delete all publication files for a given artefact.
     *
     * @param artefactId The artefact ID to delete the files for.
     * @param listType The list type of the publication.
     * @param language The language of the publication.
     */
    public void deleteFiles(UUID artefactId, ListType listType, Language language) {
        azureBlobService.deleteBlobFile(artefactId + PDF.getExtension());

        if (listType.hasAdditionalPdf() && language != Language.ENGLISH) {
            azureBlobService.deleteBlobFile(artefactId + ADDITIONAL_PDF_SUFFIX
                                                + PDF.getExtension());
        }

        if (listType.hasExcel()) {
            azureBlobService.deleteBlobFile(artefactId + EXCEL.getExtension());
        }
    }

    /**
     * Checks if any publication file exists for a given artefact.
     *
     * @param artefactId The artefact ID to check for the existence of files.
     * @return true if any file exists, else false.
     */
    public boolean fileExists(UUID artefactId) {
        return azureBlobService.blobFileExists(artefactId + PDF.getExtension())
            || azureBlobService.blobFileExists(artefactId + ADDITIONAL_PDF_SUFFIX + PDF.getExtension())
            || azureBlobService.blobFileExists(artefactId + EXCEL.getExtension());
    }

    /**
     * Retrieves the file sizes of all publication files for a given artefact.
     *
     * @param artefactId The artefact ID to retrieve the file sizes.
     * @return The file sizes.
     */
    public PublicationFileSizes getFileSizes(UUID artefactId) {
        return new PublicationFileSizes(
            azureBlobService.getBlobSize(artefactId + PDF.getExtension()),
            azureBlobService.getBlobSize(artefactId + ADDITIONAL_PDF_SUFFIX + PDF.getExtension()),
            azureBlobService.getBlobSize(artefactId + EXCEL.getExtension())
        );
    }

    private boolean isAuthorised(Artefact artefact, String userId, boolean system) {
        if (system || artefact.getSensitivity().equals(Sensitivity.PUBLIC)) {
            return true;
        } else if (userId == null) {
            return false;
        }
        return accountManagementService.getIsAuthorised(UUID.fromString(userId), artefact.getListType(),
                                                        artefact.getSensitivity());
    }
}
