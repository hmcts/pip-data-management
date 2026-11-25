package uk.gov.hmcts.reform.pip.data.management.service;

import com.azure.storage.blob.models.BlobStorageException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.ByteArrayResource;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;
import uk.gov.hmcts.reform.pip.data.management.database.AzureArtefactBlobService;
import uk.gov.hmcts.reform.pip.data.management.database.AzurePublicationBlobService;
import uk.gov.hmcts.reform.pip.data.management.models.publication.Artefact;
import uk.gov.hmcts.reform.pip.model.publication.Language;
import uk.gov.hmcts.reform.pip.model.publication.ListType;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MigrationSupportFileServiceTest {
    private static final String BLOB_URL = "https://localhost/artefact/";
    private static final String MANIFEST = "manifest.json";
    private static final byte[] JSON_PAYLOAD = "{\"test\":\"payload\"}".getBytes(StandardCharsets.UTF_8);
    private static final byte[] FLAT_FILE = {1, 2, 3, 4};
    private static final byte[] PDF_FILE = {5, 6, 7};
    private static final byte[] WELSH_PDF_FILE = {8, 9};
    private static final byte[] EXCEL_FILE = {10};

    @Mock
    private AzureArtefactBlobService azureArtefactBlobService;

    @Mock
    private AzurePublicationBlobService azurePublicationBlobService;

    private MigrationSupportFileService migrationSupportFileService;

    @BeforeEach
    void setup() {
        migrationSupportFileService = new MigrationSupportFileService(
            azureArtefactBlobService, azurePublicationBlobService, JsonMapper.builder().build()
        );
    }

    private static Artefact artefact(ListType listType, Language language, boolean isFlatFile) {
        return Artefact.builder()
            .artefactId(UUID.randomUUID())
            .listType(listType)
            .language(language)
            .isFlatFile(isFlatFile)
            .payload(BLOB_URL + UUID.randomUUID())
            .build();
    }

    private static String blobName(Artefact artefact) {
        return artefact.getPayload().substring(BLOB_URL.length());
    }

    private void stubArtefactBlob(String blobName, byte[] content) {
        when(azureArtefactBlobService.blobExists(blobName)).thenReturn(true);
        when(azureArtefactBlobService.getBlobFile(blobName)).thenReturn(new ByteArrayResource(content));
    }

    private void stubPublicationBlob(String fileName, byte[] content) {
        when(azurePublicationBlobService.blobFileExists(fileName)).thenReturn(true);
        when(azurePublicationBlobService.getBlobFile(fileName)).thenReturn(content);
    }

    private Map<String, byte[]> writeZip(Artefact... artefacts) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        migrationSupportFileService.writePublicationFiles(List.of(artefacts), outputStream);

        Map<String, byte[]> entries = new HashMap<>();
        try (ZipInputStream zip = new ZipInputStream(new ByteArrayInputStream(outputStream.toByteArray()))) {
            ZipEntry entry;
            while ((entry = zip.getNextEntry()) != null) {
                entries.put(entry.getName(), zip.readAllBytes());
            }
        }
        return entries;
    }

    private static JsonNode manifest(Map<String, byte[]> entries) {
        return JsonMapper.builder().build().readTree(entries.get(MANIFEST));
    }

    private static JsonNode file(JsonNode publication, String fileType) {
        for (JsonNode file : publication.get("files")) {
            if (fileType.equals(file.get("fileType").asString())) {
                return file;
            }
        }
        return null;
    }

    @Test
    void testJsonPublicationWithAllGeneratedFiles() throws IOException {
        Artefact artefact = artefact(ListType.CROWN_DAILY_LIST, Language.WELSH, false);
        String id = artefact.getArtefactId().toString();
        stubArtefactBlob(blobName(artefact), JSON_PAYLOAD);
        stubPublicationBlob(id + ".pdf", PDF_FILE);
        stubPublicationBlob(id + "_cy.pdf", WELSH_PDF_FILE);

        Map<String, byte[]> entries = writeZip(artefact);

        assertThat(entries).containsOnlyKeys(
            MANIFEST, "artefact/" + blobName(artefact), "publications/" + id + ".pdf",
            "publications/" + id + "_cy.pdf"
        );
        assertThat(entries.get("artefact/" + blobName(artefact))).isEqualTo(JSON_PAYLOAD);
        assertThat(entries.get("publications/" + id + ".pdf")).isEqualTo(PDF_FILE);
        assertThat(entries.get("publications/" + id + "_cy.pdf")).isEqualTo(WELSH_PDF_FILE);

        JsonNode publication = manifest(entries).get(0);
        assertThat(publication.get("artefactId").asString()).isEqualTo(id);
        assertThat(publication.get("files")).hasSize(3);

        JsonNode payload = file(publication, "JSON");
        assertThat(payload.get("fileName").asString()).isEqualTo(blobName(artefact));
        assertThat(payload.get("status").asString()).isEqualTo("FOUND");
        assertThat(payload.get("size").asLong()).isEqualTo(JSON_PAYLOAD.length);
        assertThat(payload.get("path").asString()).isEqualTo("artefact/" + blobName(artefact));
        assertThat(file(publication, "ADDITIONAL_PDF").get("fileName").asString()).isEqualTo(id + "_cy.pdf");
        assertThat(file(publication, "EXCEL")).isNull();
    }

    @Test
    void testJsonPublicationWithExcelAndNoWelshPdf() throws IOException {
        Artefact artefact = artefact(ListType.CIVIL_DAILY_CAUSE_LIST, Language.WELSH, false);
        String id = artefact.getArtefactId().toString();
        stubArtefactBlob(blobName(artefact), JSON_PAYLOAD);
        stubPublicationBlob(id + ".pdf", PDF_FILE);
        stubPublicationBlob(id + ".xlsx", EXCEL_FILE);

        Map<String, byte[]> entries = writeZip(artefact);

        assertThat(entries.get("publications/" + id + ".xlsx")).isEqualTo(EXCEL_FILE);
        JsonNode publication = manifest(entries).get(0);
        assertThat(file(publication, "EXCEL").get("status").asString()).isEqualTo("FOUND");
        assertThat(file(publication, "ADDITIONAL_PDF")).isNull();
        verify(azurePublicationBlobService, never()).blobFileExists(id + "_cy.pdf");
    }

    @Test
    void testEnglishPublicationDoesNotExpectWelshPdf() throws IOException {
        Artefact artefact = artefact(ListType.CROWN_DAILY_LIST, Language.ENGLISH, false);
        stubArtefactBlob(blobName(artefact), JSON_PAYLOAD);
        stubPublicationBlob(artefact.getArtefactId() + ".pdf", PDF_FILE);

        JsonNode publication = manifest(writeZip(artefact)).get(0);

        assertThat(publication.get("files")).hasSize(2);
        assertThat(file(publication, "ADDITIONAL_PDF")).isNull();
    }

    @Test
    void testFlatFilePublicationHasNoGeneratedFiles() throws IOException {
        Artefact artefact = artefact(ListType.CIVIL_DAILY_CAUSE_LIST, Language.ENGLISH, true);
        stubArtefactBlob(blobName(artefact), FLAT_FILE);

        Map<String, byte[]> entries = writeZip(artefact);

        assertThat(entries).containsOnlyKeys(MANIFEST, "artefact/" + blobName(artefact));
        assertThat(entries.get("artefact/" + blobName(artefact))).isEqualTo(FLAT_FILE);
        JsonNode publication = manifest(entries).get(0);
        assertThat(publication.get("files")).hasSize(1);
        assertThat(file(publication, "FLAT_FILE").get("status").asString()).isEqualTo("FOUND");
        verify(azurePublicationBlobService, never()).blobFileExists(anyString());
    }

    @Test
    void testMissingFilesAreMarkedNotFoundAndOtherPublicationsReturned() throws IOException {
        Artefact missing = artefact(ListType.CIVIL_DAILY_CAUSE_LIST, Language.ENGLISH, false);
        Artefact found = artefact(ListType.CIVIL_DAILY_CAUSE_LIST, Language.ENGLISH, true);
        when(azureArtefactBlobService.blobExists(blobName(missing))).thenReturn(false);
        lenient().when(azurePublicationBlobService.blobFileExists(anyString())).thenReturn(false);
        stubArtefactBlob(blobName(found), FLAT_FILE);

        Map<String, byte[]> entries = writeZip(missing, found);

        assertThat(entries).containsOnlyKeys(MANIFEST, "artefact/" + blobName(found));
        JsonNode missingPublication = manifest(entries).get(0);
        assertThat(missingPublication.get("files")).hasSize(3);
        missingPublication.get("files").forEach(f -> {
            assertThat(f.get("status").asString()).isEqualTo("NOT_FOUND");
            assertThat(f.get("path").isNull()).isTrue();
        });
        assertThat(file(manifest(entries).get(1), "FLAT_FILE").get("status").asString()).isEqualTo("FOUND");
    }

    @Test
    void testBlobErrorIsMarkedAsErrorAndDoesNotFailZip() throws IOException {
        Artefact artefact = artefact(ListType.CIVIL_DAILY_CAUSE_LIST, Language.ENGLISH, false);
        String id = artefact.getArtefactId().toString();
        when(azureArtefactBlobService.blobExists(blobName(artefact))).thenThrow(mock(BlobStorageException.class));
        stubPublicationBlob(id + ".pdf", PDF_FILE);
        stubPublicationBlob(id + ".xlsx", EXCEL_FILE);

        Map<String, byte[]> entries = writeZip(artefact);

        JsonNode publication = manifest(entries).get(0);
        assertThat(file(publication, "JSON").get("status").asString()).isEqualTo("ERROR");
        assertThat(file(publication, "PDF").get("status").asString()).isEqualTo("FOUND");
        assertThat(entries).containsKey("publications/" + id + ".pdf");
    }

    @Test
    void testPublicationWithNoPayloadIsMarkedNotFound() throws IOException {
        Artefact artefact = artefact(ListType.CIVIL_DAILY_CAUSE_LIST, Language.ENGLISH, true);
        artefact.setPayload(null);

        JsonNode payload = file(manifest(writeZip(artefact)).get(0), "FLAT_FILE");

        assertThat(payload.get("status").asString()).isEqualTo("NOT_FOUND");
        assertThat(payload.get("fileName").isNull()).isTrue();
    }

    @Test
    void testManifestOnlyContainsArtefactIdAndFiles() throws IOException {
        Artefact artefact = artefact(ListType.CIVIL_DAILY_CAUSE_LIST, Language.ENGLISH, true);
        artefact.setProvenance("MANUAL_UPLOAD");
        stubArtefactBlob(blobName(artefact), FLAT_FILE);

        JsonNode publication = manifest(writeZip(artefact)).get(0);

        assertThat(publication.propertyNames()).containsExactlyInAnyOrder("artefactId", "files");
    }

    @Test
    void testNoPublicationsWritesEmptyManifest() throws IOException {
        Map<String, byte[]> entries = writeZip();

        assertThat(entries).containsOnlyKeys(MANIFEST);
        assertThat(manifest(entries)).isEmpty();
    }
}
