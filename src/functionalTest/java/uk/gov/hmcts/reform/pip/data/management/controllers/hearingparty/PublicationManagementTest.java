package uk.gov.hmcts.reform.pip.data.management.controllers.hearingparty;

import com.azure.core.util.BinaryData;
import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.zonky.test.db.AutoConfigureEmbeddedDatabase;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import uk.gov.hmcts.reform.pip.data.management.Application;
import uk.gov.hmcts.reform.pip.data.management.config.PublicationConfiguration;
import uk.gov.hmcts.reform.pip.data.management.models.publication.Artefact;
import uk.gov.hmcts.reform.pip.model.publication.ArtefactType;
import uk.gov.hmcts.reform.pip.model.publication.Language;
import uk.gov.hmcts.reform.pip.model.publication.ListType;
import uk.gov.hmcts.reform.pip.model.publication.Sensitivity;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = Application.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("functional")
@AutoConfigureMockMvc
@AutoConfigureEmbeddedDatabase(type = AutoConfigureEmbeddedDatabase.DatabaseType.POSTGRES)
@WithMockUser(username = "admin", authorities = {"APPROLE_api.request.admin"})
class PublicationManagementTest {
    private static final String ROOT_URL = "/publication";
    private static final String GET_ARTEFACT_SUMMARY = ROOT_URL + "/%s/summary";
    private static final String CONTENT_MISMATCH_ERROR = "Artefact summary content should match";

    private static final LocalDateTime DISPLAY_TO = LocalDateTime.now()
        .truncatedTo(ChronoUnit.SECONDS);
    private static final LocalDateTime DISPLAY_FROM = LocalDateTime.now().plusDays(1)
        .truncatedTo(ChronoUnit.SECONDS);
    private static final LocalDateTime CONTENT_DATE = LocalDateTime.now().toLocalDate().atStartOfDay()
        .truncatedTo(ChronoUnit.SECONDS);
    private static final String PROVENANCE = "MANUAL_UPLOAD";

    private static final String BLOB_PAYLOAD_URL = "https://localhost";
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private static MockMultipartFile file;

    @MockBean(name = "artefact")
    BlobContainerClient artefcatBlobContainerClient;

    @MockBean(name = "publications")
    BlobContainerClient publicationBlobContainerClient;

    @MockBean
    BlobClient blobClient;

    @Autowired
    private MockMvc mockMvc;

    @BeforeAll
    public static void setup() {
        file = new MockMultipartFile("file", "test.pdf",
                                     MediaType.APPLICATION_PDF_VALUE, "test content".getBytes(
            StandardCharsets.UTF_8)
        );

        OBJECT_MAPPER.findAndRegisterModules();
    }

    @BeforeEach
    void init() {
        when(artefcatBlobContainerClient.getBlobClient(any())).thenReturn(blobClient);
        when(artefcatBlobContainerClient.getBlobContainerUrl()).thenReturn(BLOB_PAYLOAD_URL);

        when(publicationBlobContainerClient.getBlobClient(any())).thenReturn(blobClient);
    }

    private byte[] getTestData(String resourceName) throws IOException {
        byte[] data;
        try (InputStream mockFile = this.getClass().getClassLoader()
            .getResourceAsStream(resourceName)) {
            data = mockFile.readAllBytes();
        }
        return data;
    }

    private Artefact createPublication(ListType listType, byte[] data) throws Exception {
        MockHttpServletRequestBuilder mockHttpServletRequestBuilder = MockMvcRequestBuilders
            .post("/publication")
            .header(PublicationConfiguration.TYPE_HEADER, ArtefactType.LIST)
            .header(PublicationConfiguration.PROVENANCE_HEADER, PROVENANCE)
            .header(PublicationConfiguration.DISPLAY_FROM_HEADER, DISPLAY_FROM)
            .header(PublicationConfiguration.DISPLAY_TO_HEADER, DISPLAY_TO)
            .header(PublicationConfiguration.COURT_ID, "5")
            .header(PublicationConfiguration.LIST_TYPE, listType)
            .header(PublicationConfiguration.CONTENT_DATE, CONTENT_DATE)
            .header(PublicationConfiguration.SENSITIVITY_HEADER, Sensitivity.PUBLIC)
            .header(PublicationConfiguration.LANGUAGE_HEADER, Language.ENGLISH)
            .content(data)
            .contentType(MediaType.APPLICATION_JSON);

        MvcResult response = mockMvc.perform(mockHttpServletRequestBuilder)
            .andExpect(status().isCreated()).andReturn();

        return OBJECT_MAPPER.readValue(
            response.getResponse().getContentAsString(), Artefact.class);
    }

    @ParameterizedTest
    @EnumSource(value = ListType.class, names = {"SSCS_DAILY_LIST", "SSCS_DAILY_LIST_ADDITIONAL_HEARINGS"})
    void testGenerateArtefactSummarySscsDailyList(ListType listType) throws Exception {
        byte[] data = getTestData("data/hearing-party/sscsDailyList.json");
        Artefact artefact = createPublication(listType, data);

        when(blobClient.downloadContent()).thenReturn(BinaryData.fromBytes(data));

        MvcResult response = mockMvc.perform(get(String.format(GET_ARTEFACT_SUMMARY, artefact.getArtefactId())))
            .andExpect(status().isOk()).andReturn();

        String responseContent = response.getResponse().getContentAsString();
        assertTrue(responseContent.contains("Appellant - Surname"), CONTENT_MISMATCH_ERROR);
        assertTrue(responseContent.contains("Respondent - Respondent Organisation, Respondent Organisation 2"),
                   CONTENT_MISMATCH_ERROR);
        assertTrue(responseContent.contains("Appeal reference - 12341235"), CONTENT_MISMATCH_ERROR);
    }
}
