package uk.gov.hmcts.reform.pip.data.management.controllers.publication;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import uk.gov.hmcts.reform.pip.data.management.config.PublicationConfiguration;
import uk.gov.hmcts.reform.pip.data.management.utils.IntegrationBasicTestBase;
import uk.gov.hmcts.reform.pip.model.publication.ArtefactType;
import uk.gov.hmcts.reform.pip.model.publication.Language;
import uk.gov.hmcts.reform.pip.model.publication.ListType;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("integration-basic")
@WithMockUser(username = "admin", authorities = { "APPROLE_api.request.unknown" })
class PublicationUnauthorizedTest extends IntegrationBasicTestBase {
    private static final String PUBLICATION_URL = "/publication";
    private static final String NON_STRATEGIC_PUBLICATION_URL = PUBLICATION_URL + "/non-strategic";
    private static final String ARCHIVE_EXPIRED_ARTEFACTS_URL = PUBLICATION_URL + "/expired";
    private static final String ISSUER_HEADER = "x-issuer-id";
    private static final String VERIFICATION_HEADER = "verification";
    private static final String VERIFICATION_TRUE = "true";

    @Autowired
    private MockMvc mockMvc;

    @Test
    void testUnathorizedJsonUpload() throws Exception {
        try (InputStream mockFile = this.getClass().getClassLoader()
            .getResourceAsStream("data/family-daily-cause-list/familyDailyCauseList.json")) {

            MockHttpServletRequestBuilder mockHttpServletRequestBuilder = MockMvcRequestBuilders
                .post(PUBLICATION_URL)
                .header(PublicationConfiguration.TYPE_HEADER, ArtefactType.LIST)
                .header(PublicationConfiguration.PROVENANCE_HEADER, "Provenance")
                .header(PublicationConfiguration.SOURCE_ARTEFACT_ID_HEADER, "12345")
                .header(PublicationConfiguration.DISPLAY_FROM_HEADER, LocalDateTime.now())
                .header(PublicationConfiguration.DISPLAY_TO_HEADER, LocalDateTime.now().plusMonths(1))
                .header(PublicationConfiguration.COURT_ID, 1)
                .header(PublicationConfiguration.LIST_TYPE, ListType.FAMILY_DAILY_CAUSE_LIST)
                .header(PublicationConfiguration.LANGUAGE_HEADER, Language.ENGLISH)
                .header(PublicationConfiguration.CONTENT_DATE, LocalDateTime.now())
                .content(mockFile.readAllBytes())
                .contentType(MediaType.APPLICATION_JSON);

            mockMvc.perform(mockHttpServletRequestBuilder)
                .andExpect(status().isForbidden())
                .andReturn();
        }
    }

    @Test
    void testUnauthorizedFileUpload() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file",
                                                       "test.pdf",
                                                       MediaType.APPLICATION_PDF_VALUE,
                                                       "test content".getBytes(StandardCharsets.UTF_8));

        MockHttpServletRequestBuilder mockHttpServletRequestBuilder = MockMvcRequestBuilders
            .multipart(PUBLICATION_URL)
            .file(file)
            .header(PublicationConfiguration.TYPE_HEADER, ArtefactType.LIST)
            .header(PublicationConfiguration.PROVENANCE_HEADER, "Provenance")
            .header(PublicationConfiguration.SOURCE_ARTEFACT_ID_HEADER, "12345")
            .header(PublicationConfiguration.DISPLAY_FROM_HEADER, LocalDateTime.now())
            .header(PublicationConfiguration.DISPLAY_TO_HEADER, LocalDateTime.now().plusMonths(1))
            .header(PublicationConfiguration.COURT_ID, 1)
            .header(PublicationConfiguration.LIST_TYPE, ListType.FAMILY_DAILY_CAUSE_LIST)
            .header(PublicationConfiguration.LANGUAGE_HEADER, Language.ENGLISH)
            .header(PublicationConfiguration.CONTENT_DATE, LocalDateTime.now())
            .contentType(MediaType.MULTIPART_FORM_DATA);

        mockMvc.perform(mockHttpServletRequestBuilder)
            .andExpect(status().isForbidden())
            .andReturn();
    }

    @Test
    void testUnauthorizedNonStrategicPublicationUpload() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
            "file", "file.xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
            "test content".getBytes(StandardCharsets.UTF_8)
        );

        MockHttpServletRequestBuilder mockHttpServletRequestBuilder = MockMvcRequestBuilders
            .multipart(NON_STRATEGIC_PUBLICATION_URL)
            .file(file)
            .header(PublicationConfiguration.TYPE_HEADER, ArtefactType.LIST)
            .header(PublicationConfiguration.PROVENANCE_HEADER, "Provenance")
            .header(PublicationConfiguration.SOURCE_ARTEFACT_ID_HEADER, "12345")
            .header(PublicationConfiguration.DISPLAY_FROM_HEADER, LocalDateTime.now())
            .header(PublicationConfiguration.DISPLAY_TO_HEADER, LocalDateTime.now().plusMonths(1))
            .header(PublicationConfiguration.COURT_ID, 1)
            .header(PublicationConfiguration.LIST_TYPE, ListType.FAMILY_DAILY_CAUSE_LIST)
            .header(PublicationConfiguration.LANGUAGE_HEADER, Language.ENGLISH)
            .header(PublicationConfiguration.CONTENT_DATE, LocalDateTime.now())
            .contentType(MediaType.MULTIPART_FORM_DATA);

        mockMvc.perform(mockHttpServletRequestBuilder)
            .andExpect(status().isForbidden())
            .andReturn();
    }

    @Test
    void testUnauthorizedGetArtefactMetadata() throws Exception {
        MockHttpServletRequestBuilder mockHttpServletRequestBuilder = MockMvcRequestBuilders
            .get("/publication/2dde8f1e-bfb6-11ec-9d64-0242ac120002")
            .header(VERIFICATION_HEADER, VERIFICATION_TRUE);

        mockMvc.perform(mockHttpServletRequestBuilder)
            .andExpect(status().isForbidden())
            .andReturn();
    }

    @Test
    void testUnauthorizedGetArtefactPayload() throws Exception {
        MockHttpServletRequestBuilder mockHttpServletRequestBuilder = MockMvcRequestBuilders
            .get("/publication/2dde8f1e-bfb6-11ec-9d64-0242ac120002/payload")
            .header(VERIFICATION_HEADER, VERIFICATION_TRUE);

        mockMvc.perform(mockHttpServletRequestBuilder)
            .andExpect(status().isForbidden())
            .andReturn();
    }

    @Test
    void testUnauthorizedGetArtefactFile() throws Exception {
        MockHttpServletRequestBuilder mockHttpServletRequestBuilder = MockMvcRequestBuilders
            .get("/publication/2dde8f1e-bfb6-11ec-9d64-0242ac120002/file")
            .header(VERIFICATION_HEADER, VERIFICATION_TRUE);

        mockMvc.perform(mockHttpServletRequestBuilder)
            .andExpect(status().isForbidden())
            .andReturn();
    }

    @Test
    void testUnauthorizedDeleteArtefact() throws Exception {
        MockHttpServletRequestBuilder mockHttpServletRequestBuilder = MockMvcRequestBuilders
            .delete("/publication/2dde8f1e-bfb6-11ec-9d64-0242ac120002")
            .header("x-issuer-id", "abcde");

        mockMvc.perform(mockHttpServletRequestBuilder)
            .andExpect(status().isForbidden())
            .andReturn();
    }

    @Test
    void testUnauthorizedArchiveArtefact() throws Exception {
        MockHttpServletRequestBuilder archiveRequest = MockMvcRequestBuilders
            .put("/publication/2dde8f1e-bfb6-11ec-9d64-0242ac120002/archive")
            .header(ISSUER_HEADER, "123");

        mockMvc.perform(archiveRequest)
            .andExpect(status().isForbidden())
            .andReturn();
    }

    @Test
    void testUnauthorizedArchiveExpiredArtefacts() throws Exception {
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
            .delete(ARCHIVE_EXPIRED_ARTEFACTS_URL);

        mockMvc.perform(request)
            .andExpect(status().isForbidden())
            .andReturn();
    }
}
