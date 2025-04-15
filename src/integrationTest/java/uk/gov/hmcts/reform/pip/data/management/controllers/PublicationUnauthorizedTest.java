package uk.gov.hmcts.reform.pip.data.management.controllers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import uk.gov.hmcts.reform.pip.data.management.config.PublicationConfiguration;
import uk.gov.hmcts.reform.pip.data.management.models.publication.Artefact;
import uk.gov.hmcts.reform.pip.data.management.service.publication.ArtefactService;
import uk.gov.hmcts.reform.pip.data.management.utils.IntegrationBasicTestBase;
import uk.gov.hmcts.reform.pip.model.publication.ArtefactType;
import uk.gov.hmcts.reform.pip.model.publication.Language;
import uk.gov.hmcts.reform.pip.model.publication.ListType;
import uk.gov.hmcts.reform.pip.model.publication.Sensitivity;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("integration-basic")
@WithMockUser(username = "admin", authorities = { "APPROLE_api.request.unknown" })
class PublicationUnauthorizedTest extends IntegrationBasicTestBase {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ArtefactService artefactService;

    private static final String PUBLICATION_URL = "/publication";
    private static final String NON_STRATEGIC_PUBLICATION_URL = PUBLICATION_URL + "/non-strategic";
    private static final String PUBLICATION_BY_LOCATION_URL = PUBLICATION_URL + "/locationId/";
    private static final String PUBLICATION_SEARCH_URL = PUBLICATION_URL + "/search/";
    private static final String ARCHIVE_EXPIRED_ARTEFACTS_URL = PUBLICATION_URL + "/expired";
    private static final String MI_REPORTING_DATA_URL_V2 = PUBLICATION_URL + "/v2/mi-data";
    private static final String COUNT_BY_LOCATION_URL = PUBLICATION_URL + "/count-by-location";
    private static final String SEND_NEW_ARTEFACTS_FOR_SUBSCRIPTION_URL = PUBLICATION_URL + "/latest/subscription";
    private static final String REPORT_NO_MATCH_ARTEFACTS_URL = PUBLICATION_URL + "/no-match/reporting";
    private static final String REQUESTER_ID_HEADER = "x-requester-id";
    private static final String VERIFICATION_HEADER = "verification";
    private static final String VERIFICATION_TRUE = "true";
    private static final UUID TEST_ARTEFACT_ID = UUID.randomUUID();
    private static final String USER_ID = "123";

    private static final Artefact ARTEFACT = new Artefact();

    @BeforeEach
    void setup() {
        ARTEFACT.setArtefactId(TEST_ARTEFACT_ID);
        ARTEFACT.setContentDate(LocalDateTime.now());
        ARTEFACT.setLocationId("1");
        ARTEFACT.setProvenance("france");
        ARTEFACT.setLanguage(Language.ENGLISH);
        ARTEFACT.setListType(ListType.SJP_PUBLIC_LIST);
        ARTEFACT.setLastReceivedDate(LocalDateTime.now());
        ARTEFACT.setPayloadSize(100F);
        ARTEFACT.setSensitivity(Sensitivity.PUBLIC);
    }

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
                .header(REQUESTER_ID_HEADER, USER_ID)
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
            .header(REQUESTER_ID_HEADER, UUID.randomUUID())
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
            .header(REQUESTER_ID_HEADER, USER_ID)
            .contentType(MediaType.MULTIPART_FORM_DATA);

        mockMvc.perform(mockHttpServletRequestBuilder)
            .andExpect(status().isForbidden())
            .andReturn();

    }

    @Test
    void testUnauthorizedGetByCourtId() throws Exception {
        MockHttpServletRequestBuilder mockHttpServletRequestBuilder = MockMvcRequestBuilders
            .get(PUBLICATION_BY_LOCATION_URL + "1")
            .header(VERIFICATION_HEADER, VERIFICATION_TRUE);

        mockMvc.perform(mockHttpServletRequestBuilder)
            .andExpect(status().isForbidden())
            .andReturn();
    }

    @Test
    void testUnauthorizedGetBySearchValue() throws Exception {
        MockHttpServletRequestBuilder mockHttpServletRequestBuilder = MockMvcRequestBuilders
            .get(PUBLICATION_SEARCH_URL + "CASE_URN/1234")
            .header(VERIFICATION_HEADER, VERIFICATION_TRUE);

        mockMvc.perform(mockHttpServletRequestBuilder)
            .andExpect(status().isForbidden())
            .andReturn();
    }

    @Test
    void testUnauthorizedGetArtefactMetadata() throws Exception {
        when(artefactService.getMetadataByArtefactId(any())).thenReturn(ARTEFACT);
        MockHttpServletRequestBuilder mockHttpServletRequestBuilder = MockMvcRequestBuilders
            .get("/publication/2dde8f1e-bfb6-11ec-9d64-0242ac120002")
            .header(VERIFICATION_HEADER, VERIFICATION_TRUE)
            .header(REQUESTER_ID_HEADER, UUID.randomUUID());

        mockMvc.perform(mockHttpServletRequestBuilder)
            .andExpect(status().isForbidden())
            .andReturn();
    }

    @Test
    void testUnauthorizedGetArtefactPayload() throws Exception {
        when(artefactService.getMetadataByArtefactId(any())).thenReturn(ARTEFACT);
        MockHttpServletRequestBuilder mockHttpServletRequestBuilder = MockMvcRequestBuilders
            .get("/publication/2dde8f1e-bfb6-11ec-9d64-0242ac120002/payload")
            .header(VERIFICATION_HEADER, VERIFICATION_TRUE)
            .header(REQUESTER_ID_HEADER, UUID.randomUUID());

        mockMvc.perform(mockHttpServletRequestBuilder)
            .andExpect(status().isForbidden())
            .andReturn();
    }

    @Test
    void testUnauthorizedGetArtefactFile() throws Exception {
        when(artefactService.getMetadataByArtefactId(any())).thenReturn(ARTEFACT);
        MockHttpServletRequestBuilder mockHttpServletRequestBuilder = MockMvcRequestBuilders
            .get("/publication/2dde8f1e-bfb6-11ec-9d64-0242ac120002/file")
            .header(VERIFICATION_HEADER, VERIFICATION_TRUE)
            .header(REQUESTER_ID_HEADER, UUID.randomUUID());

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
            .header(REQUESTER_ID_HEADER, USER_ID);

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

    @Test
    void testDeleteArtefactsByLocationUnauthorized() throws Exception {
        MockHttpServletRequestBuilder deleteRequest = MockMvcRequestBuilders
            .delete("/publication/1/deleteArtefacts")
            .header(REQUESTER_ID_HEADER, USER_ID);

        mockMvc.perform(deleteRequest)
            .andExpect(status().isForbidden())
            .andReturn();
    }

    @Test
    void testUnauthorizedGetAllNoMatchArtefacts() throws Exception {
        MockHttpServletRequestBuilder mockHttpServletRequestBuilder =
            MockMvcRequestBuilders.get("/publication/no-match")
                .header(REQUESTER_ID_HEADER, USER_ID);

        mockMvc.perform(mockHttpServletRequestBuilder)
            .andExpect(status().isForbidden())
            .andReturn();
    }

    @Test
    void testUnauthorizedGetMiDataV2() throws Exception {
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
            .get(MI_REPORTING_DATA_URL_V2);

        mockMvc.perform(request)
            .andExpect(status().isForbidden())
            .andReturn();
    }

    @Test
    void testUnauthorizedCountByLocation() throws Exception {
        MockHttpServletRequestBuilder request =
            MockMvcRequestBuilders.get(COUNT_BY_LOCATION_URL)
                .header(REQUESTER_ID_HEADER, USER_ID);

        mockMvc.perform(request)
            .andExpect(status().isForbidden())
            .andReturn();
    }

    @Test
    void testUnauthorizedSendNewArtefactsForSubscription() throws Exception {
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post(SEND_NEW_ARTEFACTS_FOR_SUBSCRIPTION_URL);

        mockMvc.perform(request)
            .andExpect(status().isForbidden())
            .andReturn();
    }

    @Test
    void testUnauthorizedReportNoMatchArtefacts() throws Exception {
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post(REPORT_NO_MATCH_ARTEFACTS_URL);

        mockMvc.perform(request)
            .andExpect(status().isForbidden())
            .andReturn();
    }
}
