package uk.gov.hmcts.reform.pip.data.management.controllers;

import com.azure.core.util.BinaryData;
import com.azure.storage.blob.BlobClient;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.zonky.test.db.AutoConfigureEmbeddedDatabase;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import uk.gov.hmcts.reform.pip.data.management.Application;
import uk.gov.hmcts.reform.pip.data.management.database.ArtefactRepository;
import uk.gov.hmcts.reform.pip.data.management.models.publication.Artefact;
import uk.gov.hmcts.reform.pip.data.management.utils.IntegrationTestBase;
import uk.gov.hmcts.reform.pip.model.publication.ArtefactType;
import uk.gov.hmcts.reform.pip.model.publication.Language;
import uk.gov.hmcts.reform.pip.model.publication.ListType;
import uk.gov.hmcts.reform.pip.model.publication.Sensitivity;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = {Application.class}, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("integration")
@AutoConfigureEmbeddedDatabase(type = AutoConfigureEmbeddedDatabase.DatabaseType.POSTGRES)
@WithMockUser(username = "admin", authorities = {"APPROLE_api.request.admin"})
class MigrationSupportFilesApiTest extends IntegrationTestBase {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private static final String FILES_URL = "/migration-support/publication/files";
    private static final String FROM_PARAM = "lastReceivedDateFrom";
    private static final String TO_PARAM = "lastReceivedDateTo";
    private static final String FROM_DATE = "2001-02-01";
    private static final String TO_DATE = "2001-02-28";
    private static final String MANIFEST = "manifest.json";
    private static final String ARTEFACT_FOLDER = "artefact/";
    private static final String PUBLICATIONS_FOLDER = "publications/";
    private static final String FILES = "files";
    private static final String STATUS = "status";
    private static final String FOUND = "FOUND";
    private static final String NOT_FOUND = "NOT_FOUND";

    private static final String UNAUTHORIZED_ROLE = "APPROLE_unknown.authorized";
    private static final String UNAUTHORIZED_USERNAME = "unauthorized_isAuthorized";

    // Dates far in the past so filtered results are not affected by publications created by other tests
    private static final LocalDateTime IN_RANGE = LocalDateTime.of(2001, 2, 10, 12, 0, 0);
    private static final LocalDateTime OUT_OF_RANGE = LocalDateTime.of(2001, 3, 1, 0, 0, 0);

    private static final byte[] JSON_PAYLOAD = "{\"document\":{}}".getBytes(StandardCharsets.UTF_8);
    private static final byte[] FLAT_FILE = {37, 80, 68, 70, 1, 2};
    private static final byte[] PDF_FILE = {1, 2, 3};
    private static final byte[] WELSH_PDF_FILE = {4, 5};
    private static final byte[] EXCEL_FILE = {6, 7, 8, 9};

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ArtefactRepository artefactRepository;

    private Artefact welshPdfArtefact;
    private Artefact excelArtefact;
    private Artefact flatFileArtefact;
    private Artefact missingBlobsArtefact;
    private Artefact outOfRangeArtefact;

    @BeforeEach
    void setup() {
        welshPdfArtefact = createArtefact(ListType.CROWN_DAILY_LIST, Language.WELSH, false, IN_RANGE);
        excelArtefact = createArtefact(ListType.CIVIL_DAILY_CAUSE_LIST, Language.ENGLISH, false, IN_RANGE);
        flatFileArtefact = createArtefact(ListType.CIVIL_DAILY_CAUSE_LIST, Language.ENGLISH, true, IN_RANGE);
        missingBlobsArtefact = createArtefact(ListType.CIVIL_DAILY_CAUSE_LIST, Language.ENGLISH, false, IN_RANGE);
        outOfRangeArtefact = createArtefact(ListType.CIVIL_DAILY_CAUSE_LIST, Language.ENGLISH, true, OUT_OF_RANGE);

        stubArtefactBlob(blobName(welshPdfArtefact), JSON_PAYLOAD);
        stubPublicationBlob(welshPdfArtefact.getArtefactId() + ".pdf", PDF_FILE);
        stubPublicationBlob(welshPdfArtefact.getArtefactId() + "_cy.pdf", WELSH_PDF_FILE);

        stubArtefactBlob(blobName(excelArtefact), JSON_PAYLOAD);
        stubPublicationBlob(excelArtefact.getArtefactId() + ".pdf", PDF_FILE);
        stubPublicationBlob(excelArtefact.getArtefactId() + ".xlsx", EXCEL_FILE);

        stubArtefactBlob(blobName(flatFileArtefact), FLAT_FILE);
        stubArtefactBlob(blobName(outOfRangeArtefact), FLAT_FILE);
    }

    @AfterEach
    void tearDown() {
        artefactRepository.deleteAll(List.of(
            welshPdfArtefact, excelArtefact, flatFileArtefact, missingBlobsArtefact, outOfRangeArtefact
        ));
    }

    private Artefact createArtefact(ListType listType, Language language, boolean isFlatFile,
                                    LocalDateTime lastReceivedDate) {
        Artefact artefact = Artefact.builder()
            .provenance("MANUAL_UPLOAD")
            .sourceArtefactId("migration-" + UUID.randomUUID())
            .type(ArtefactType.LIST)
            .sensitivity(Sensitivity.PUBLIC)
            .language(language)
            .listType(listType)
            .locationId(UUID.randomUUID().toString())
            .contentDate(lastReceivedDate.toLocalDate().atStartOfDay())
            .displayFrom(lastReceivedDate)
            .displayTo(lastReceivedDate.plusDays(1))
            .isFlatFile(isFlatFile)
            .payload(BLOB_PAYLOAD_URL + "/" + UUID.randomUUID())
            .lastReceivedDate(lastReceivedDate)
            .build();
        return artefactRepository.save(artefact);
    }

    private static String blobName(Artefact artefact) {
        return artefact.getPayload().substring(artefact.getPayload().lastIndexOf('/') + 1);
    }

    private static BlobClient blobClientWithContent(byte[] content) {
        BlobClient client = mock(BlobClient.class);
        when(client.exists()).thenReturn(true);
        when(client.downloadContent()).thenReturn(BinaryData.fromBytes(content));
        return client;
    }

    private void stubArtefactBlob(String blobName, byte[] content) {
        BlobClient client = blobClientWithContent(content);
        when(artefactBlobContainerClient.getBlobClient(blobName)).thenReturn(client);
    }

    private void stubPublicationBlob(String fileName, byte[] content) {
        BlobClient client = blobClientWithContent(content);
        when(publicationBlobContainerClient.getBlobClient(fileName)).thenReturn(client);
    }

    private Map<String, byte[]> getZip(String from, String to) throws Exception {
        MvcResult result = mockMvc.perform(get(FILES_URL).queryParam(FROM_PARAM, from).queryParam(TO_PARAM, to))
            .andExpect(status().isOk())
            .andReturn();

        assertThat(result.getResponse().getContentType()).isEqualTo("application/zip");
        assertThat(result.getResponse().getHeader(HttpHeaders.CONTENT_DISPOSITION))
            .isEqualTo("attachment; filename=\"publication-files.zip\"");
        return unzip(result.getResponse().getContentAsByteArray());
    }

    private static Map<String, byte[]> unzip(byte[] content) throws IOException {
        Map<String, byte[]> entries = new HashMap<>();
        try (ZipInputStream zip = new ZipInputStream(new ByteArrayInputStream(content))) {
            ZipEntry entry;
            while ((entry = zip.getNextEntry()) != null) {
                entries.put(entry.getName(), zip.readAllBytes());
            }
        }
        return entries;
    }

    private static List<JsonNode> manifest(Map<String, byte[]> entries) throws IOException {
        List<JsonNode> publications = new ArrayList<>();
        OBJECT_MAPPER.readTree(entries.get(MANIFEST)).forEach(publications::add);
        return publications;
    }

    private static JsonNode findPublication(List<JsonNode> manifest, Artefact artefact) {
        return manifest.stream()
            .filter(p -> artefact.getArtefactId().toString().equals(p.get("artefactId").asText()))
            .findFirst()
            .orElseThrow();
    }

    private static Map<String, JsonNode> filesByType(JsonNode publication) {
        Map<String, JsonNode> files = new HashMap<>();
        publication.get(FILES).forEach(f -> files.put(f.get("fileType").asText(), f));
        return files;
    }

    @Test
    void testJsonPublicationWithPdfAndWelshPdf() throws Exception {
        Map<String, byte[]> entries = getZip(FROM_DATE, TO_DATE);
        String id = welshPdfArtefact.getArtefactId().toString();

        assertThat(entries.get(ARTEFACT_FOLDER + blobName(welshPdfArtefact))).isEqualTo(JSON_PAYLOAD);
        assertThat(entries.get(PUBLICATIONS_FOLDER + id + ".pdf")).isEqualTo(PDF_FILE);
        assertThat(entries.get(PUBLICATIONS_FOLDER + id + "_cy.pdf")).isEqualTo(WELSH_PDF_FILE);

        JsonNode publication = findPublication(manifest(entries), welshPdfArtefact);
        assertThat(publication.properties()).hasSize(2);

        Map<String, JsonNode> files = filesByType(publication);
        assertThat(files).containsOnlyKeys("JSON", "PDF", "ADDITIONAL_PDF");
        assertThat(files.get("JSON").get("fileName").asText()).isEqualTo(blobName(welshPdfArtefact));
        assertThat(files.get("JSON").get("size").asLong()).isEqualTo(JSON_PAYLOAD.length);
        assertThat(files.get("ADDITIONAL_PDF").get("path").asText())
            .isEqualTo(PUBLICATIONS_FOLDER + id + "_cy.pdf");
        files.values().forEach(f -> assertThat(f.get(STATUS).asText()).isEqualTo(FOUND));
    }

    @Test
    void testJsonPublicationWithPdfAndExcel() throws Exception {
        Map<String, byte[]> entries = getZip(FROM_DATE, TO_DATE);
        String id = excelArtefact.getArtefactId().toString();

        assertThat(entries.get(PUBLICATIONS_FOLDER + id + ".pdf")).isEqualTo(PDF_FILE);
        assertThat(entries.get(PUBLICATIONS_FOLDER + id + ".xlsx")).isEqualTo(EXCEL_FILE);
        assertThat(entries).doesNotContainKey(PUBLICATIONS_FOLDER + id + "_cy.pdf");

        Map<String, JsonNode> files = filesByType(findPublication(manifest(entries), excelArtefact));
        assertThat(files).containsOnlyKeys("JSON", "PDF", "EXCEL");
        files.values().forEach(f -> assertThat(f.get(STATUS).asText()).isEqualTo(FOUND));
    }

    @Test
    void testFlatFilePublicationHasNoGeneratedFiles() throws Exception {
        Map<String, byte[]> entries = getZip(FROM_DATE, TO_DATE);

        assertThat(entries.get(ARTEFACT_FOLDER + blobName(flatFileArtefact))).isEqualTo(FLAT_FILE);
        assertThat(entries.keySet())
            .noneMatch(name -> name.startsWith(PUBLICATIONS_FOLDER + flatFileArtefact.getArtefactId()));

        Map<String, JsonNode> files = filesByType(findPublication(manifest(entries), flatFileArtefact));
        assertThat(files).containsOnlyKeys("FLAT_FILE");
        assertThat(files.get("FLAT_FILE").get(STATUS).asText()).isEqualTo(FOUND);
    }

    @Test
    void testMissingBlobsAreMarkedNotFound() throws Exception {
        Map<String, byte[]> entries = getZip(FROM_DATE, TO_DATE);

        assertThat(entries).doesNotContainKey(ARTEFACT_FOLDER + blobName(missingBlobsArtefact));

        Map<String, JsonNode> files = filesByType(findPublication(manifest(entries), missingBlobsArtefact));
        assertThat(files).containsOnlyKeys("JSON", "PDF", "EXCEL");
        files.values().forEach(f -> {
            assertThat(f.get(STATUS).asText()).isEqualTo(NOT_FOUND);
            assertThat(f.get("path").isNull()).isTrue();
        });
    }

    @Test
    void testDateFilterOnlyReturnsPublicationsInRange() throws Exception {
        Map<String, byte[]> entries = getZip(FROM_DATE, TO_DATE);

        assertThat(manifest(entries).stream().map(p -> p.get("artefactId").asText()).toList())
            .containsExactlyInAnyOrder(
                welshPdfArtefact.getArtefactId().toString(), excelArtefact.getArtefactId().toString(),
                flatFileArtefact.getArtefactId().toString(), missingBlobsArtefact.getArtefactId().toString()
            );
        assertThat(entries).doesNotContainKey(ARTEFACT_FOLDER + blobName(outOfRangeArtefact));
    }

    @Test
    void testNoFiltersReturnsAllPublications() throws Exception {
        Map<String, byte[]> entries = getZip(null, null);

        assertThat(manifest(entries).stream().map(p -> p.get("artefactId").asText()).toList())
            .contains(welshPdfArtefact.getArtefactId().toString(), outOfRangeArtefact.getArtefactId().toString());
        assertThat(entries.get(ARTEFACT_FOLDER + blobName(outOfRangeArtefact))).isEqualTo(FLAT_FILE);
    }

    @Test
    void testInvalidDateReturnsBadRequest() throws Exception {
        mockMvc.perform(get(FILES_URL).queryParam(FROM_PARAM, "2001-13-45"))
            .andExpect(status().isBadRequest());
    }

    @Test
    void testFromAfterToReturnsBadRequest() throws Exception {
        mockMvc.perform(get(FILES_URL).queryParam(FROM_PARAM, TO_DATE).queryParam(TO_PARAM, FROM_DATE))
            .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = UNAUTHORIZED_USERNAME, authorities = {UNAUTHORIZED_ROLE})
    void testUnauthorisedGetPublicationFilesReturnsForbidden() throws Exception {
        mockMvc.perform(get(FILES_URL))
            .andExpect(status().isForbidden());
    }
}
