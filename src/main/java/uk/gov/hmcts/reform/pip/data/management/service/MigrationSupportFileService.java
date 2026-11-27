package uk.gov.hmcts.reform.pip.data.management.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import tools.jackson.databind.json.JsonMapper;
import uk.gov.hmcts.reform.pip.data.management.database.AzureArtefactBlobService;
import uk.gov.hmcts.reform.pip.data.management.database.AzurePublicationBlobService;
import uk.gov.hmcts.reform.pip.data.management.helpers.ArtefactHelper;
import uk.gov.hmcts.reform.pip.data.management.models.publication.Artefact;
import uk.gov.hmcts.reform.pip.data.management.models.publication.PublicationFile;
import uk.gov.hmcts.reform.pip.data.management.models.publication.PublicationFileStatus;
import uk.gov.hmcts.reform.pip.data.management.models.publication.PublicationFiles;
import uk.gov.hmcts.reform.pip.data.management.models.publication.PublicationFileType;
import uk.gov.hmcts.reform.pip.model.publication.Language;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static uk.gov.hmcts.reform.pip.data.management.service.publication.PublicationFileManagementService.ADDITIONAL_PDF_SUFFIX;
import static uk.gov.hmcts.reform.pip.model.LogBuilder.writeLog;
import static uk.gov.hmcts.reform.pip.model.publication.FileType.EXCEL;
import static uk.gov.hmcts.reform.pip.model.publication.FileType.PDF;

/**
 * Writes the blob files of publications to a zip. Files are placed in folders named after the blob container they
 * are held in, using the same names as in blob storage, alongside a manifest linking each publication's artefact ID
 * to its files.
 */
@Slf4j
@Service
@AllArgsConstructor
public class MigrationSupportFileService {
    public static final String MANIFEST_FILE_NAME = "manifest.json";
    public static final String ARTEFACT_FOLDER = "artefact/";
    public static final String PUBLICATIONS_FOLDER = "publications/";

    private final AzureArtefactBlobService azureArtefactBlobService;
    private final AzurePublicationBlobService azurePublicationBlobService;
    private final JsonMapper jsonMapper;

    @FunctionalInterface
    private interface BlobReader {
        byte[] read() throws IOException;
    }

    /**
     * Write the blob files for each publication, and a manifest of each publication's files, to a zip.
     * A file which cannot be retrieved is recorded in the manifest without failing the rest of the zip.
     *
     * @param artefacts The publications to write the files for.
     * @param outputStream The stream to write the zip to.
     * @throws IOException if the zip cannot be written.
     */
    public void writePublicationFiles(List<Artefact> artefacts, OutputStream outputStream) throws IOException {
        ZipOutputStream zip = new ZipOutputStream(outputStream);
        List<PublicationFiles> manifest = new ArrayList<>();

        for (Artefact artefact : artefacts) {
            List<PublicationFile> files = new ArrayList<>();
            files.add(writeOriginalUpload(artefact, zip));

            if (!Boolean.TRUE.equals(artefact.getIsFlatFile())) {
                for (Map.Entry<String, PublicationFileType> file : getExpectedGeneratedFiles(artefact).entrySet()) {
                    files.add(writeGeneratedFile(file.getKey(), file.getValue(), zip));
                }
            }

            manifest.add(new PublicationFiles(artefact.getArtefactId(), files));
            zip.flush();
        }

        writeEntry(zip, MANIFEST_FILE_NAME, jsonMapper.writeValueAsBytes(manifest));
        zip.finish();
        zip.flush();
    }

    private PublicationFile writeOriginalUpload(Artefact artefact, ZipOutputStream zip) throws IOException {
        PublicationFileType fileType = Boolean.TRUE.equals(artefact.getIsFlatFile())
            ? PublicationFileType.FLAT_FILE : PublicationFileType.JSON;

        if (artefact.getPayload() == null || artefact.getPayload().isBlank()) {
            return new PublicationFile(null, fileType, PublicationFileStatus.NOT_FOUND, null, null);
        }

        String blobName = ArtefactHelper.getUuidFromUrl(artefact.getPayload());
        return writeFile(zip, ARTEFACT_FOLDER, blobName, fileType, () ->
            azureArtefactBlobService.blobExists(blobName)
                ? azureArtefactBlobService.getBlobFile(blobName).getContentAsByteArray()
                : null
        );
    }

    private PublicationFile writeGeneratedFile(String fileName, PublicationFileType fileType,
                                               ZipOutputStream zip) throws IOException {
        return writeFile(zip, PUBLICATIONS_FOLDER, fileName, fileType, () ->
            azurePublicationBlobService.blobFileExists(fileName)
                ? azurePublicationBlobService.getBlobFile(fileName)
                : null
        );
    }

    private PublicationFile writeFile(ZipOutputStream zip, String folder, String fileName,
                                      PublicationFileType fileType, BlobReader reader) throws IOException {
        byte[] content;
        try {
            content = reader.read();
        } catch (IOException | RuntimeException e) {
            log.error(writeLog(String.format("Failed to retrieve blob %s%s for migration with error: %s",
                                             folder, fileName, e.getMessage())));
            return new PublicationFile(fileName, fileType, PublicationFileStatus.ERROR, null, null);
        }

        if (content == null) {
            return new PublicationFile(fileName, fileType, PublicationFileStatus.NOT_FOUND, null, null);
        }

        String path = folder + fileName;
        writeEntry(zip, path, content);
        return new PublicationFile(fileName, fileType, PublicationFileStatus.FOUND, (long) content.length, path);
    }

    /**
     * Returns the generated files expected for a publication, using the same rules as when they are generated.
     */
    private Map<String, PublicationFileType> getExpectedGeneratedFiles(Artefact artefact) {
        String artefactId = artefact.getArtefactId().toString();
        Map<String, PublicationFileType> files = new LinkedHashMap<>();
        files.put(artefactId + PDF.getExtension(), PublicationFileType.PDF);

        if (artefact.getListType() != null) {
            if (artefact.getListType().hasAdditionalPdf() && artefact.getLanguage() != Language.ENGLISH) {
                files.put(artefactId + ADDITIONAL_PDF_SUFFIX + PDF.getExtension(), PublicationFileType.ADDITIONAL_PDF);
            }
            if (artefact.getListType().hasExcel()) {
                files.put(artefactId + EXCEL.getExtension(), PublicationFileType.EXCEL);
            }
        }
        return files;
    }

    private void writeEntry(ZipOutputStream zip, String path, byte[] content) throws IOException {
        zip.putNextEntry(new ZipEntry(path));
        zip.write(content);
        zip.closeEntry();
    }
}
