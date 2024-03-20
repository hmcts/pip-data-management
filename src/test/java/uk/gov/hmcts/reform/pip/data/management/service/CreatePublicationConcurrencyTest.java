package uk.gov.hmcts.reform.pip.data.management.service;

import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.zonky.test.db.AutoConfigureEmbeddedDatabase;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MvcResult;
import uk.gov.hmcts.reform.pip.data.management.Application;
import uk.gov.hmcts.reform.pip.data.management.config.AzureBlobConfigurationTestConfiguration;
import uk.gov.hmcts.reform.pip.data.management.database.ArtefactRepository;
import uk.gov.hmcts.reform.pip.data.management.models.publication.Artefact;
import uk.gov.hmcts.reform.pip.data.management.utils.JsonExtractor;
import uk.gov.hmcts.reform.pip.model.publication.ArtefactType;
import uk.gov.hmcts.reform.pip.model.publication.Language;
import uk.gov.hmcts.reform.pip.model.publication.ListType;
import uk.gov.hmcts.reform.pip.model.publication.Sensitivity;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ActiveProfiles(profiles = "test")
@SpringBootTest(classes = {Application.class, AzureBlobConfigurationTestConfiguration.class})
@AutoConfigureEmbeddedDatabase(type = AutoConfigureEmbeddedDatabase.DatabaseType.POSTGRES)
@EnableRetry
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class CreatePublicationConcurrencyTest {
    private static final String MANUAL_UPLOAD = "MANUAL_UPLOAD";
    private static final String BLOB_PAYLOAD_URL = "https://localhost";

    @MockBean
    BlobContainerClient blobContainerClient;

    @MockBean
    BlobClient blobClient;

    @Autowired
    private ArtefactRepository artefactRepository;

    @Autowired
    private PublicationCreationRunner publicationCreationRunner;

    private String payload;
    private Artefact artefact;

    @BeforeAll
    void setup() throws IOException {
        try (InputStream inputStream = getClass().getResourceAsStream("/mocks/sjp-press-list/sjpPressList.json")) {
            String inputRaw = IOUtils.toString(inputStream, Charset.defaultCharset());
            payload = new ObjectMapper().readTree(inputRaw).toString();
            artefact = createMetadata(payload);
        }

        when(blobContainerClient.getBlobClient(any())).thenReturn(blobClient);
        when(blobContainerClient.getBlobContainerUrl()).thenReturn(BLOB_PAYLOAD_URL);
    }

    private Artefact createMetadata(String payload) {
        return Artefact.builder()
            .provenance(MANUAL_UPLOAD)
            .sourceArtefactId("")
            .type(ArtefactType.LIST)
            .sensitivity(Sensitivity.PUBLIC)
            .language(Language.ENGLISH)
            .displayFrom(LocalDateTime.now())
            .displayTo(LocalDateTime.now().plusDays(1))
            .listType(ListType.SJP_PRESS_LIST)
            .locationId("9")
            .contentDate(LocalDateTime.now())
            .expiryDate(LocalDateTime.now().plusDays(1))
            .payloadSize((float) payload.length() / 1024)
            .build();
    }

    @Test
    void test() throws InterruptedException, ExecutionException {
        Artefact existingArtefact = Artefact.builder()
            .artefactId(UUID.randomUUID())
            .provenance(MANUAL_UPLOAD)
            .sourceArtefactId("")
            .type(ArtefactType.LIST)
            .sensitivity(Sensitivity.PUBLIC)
            .language(Language.ENGLISH)
            .displayFrom(LocalDateTime.now())
            .displayTo(LocalDateTime.now().plusDays(1))
            .listType(ListType.SJP_PRESS_LIST)
            .locationId("9")
            .contentDate(LocalDateTime.now())
            .expiryDate(LocalDateTime.now().plusDays(1))
            .payloadSize((float) payload.length() / 1024)
            .build();

        artefactRepository.save(existingArtefact);
        ExecutorService threads = Executors.newFixedThreadPool(5);
        List<Callable<Artefact>> torun = new ArrayList<>(5);
        for (int i = 0; i < 5; i++) {
            torun.add(() -> publicationCreationRunner.run(artefact, payload));
        }

        List<Future<Artefact>> futures = threads.invokeAll(torun);

        threads.shutdown();

        for (Future<Artefact> fut : futures) {
            Artefact createdArtefact = fut.get();
            System.out.println(createdArtefact);
        }
    }

}
