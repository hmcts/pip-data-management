package uk.gov.hmcts.reform.pip.data.management.controllers.publication;

import io.zonky.test.db.AutoConfigureEmbeddedDatabase;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import uk.gov.hmcts.reform.pip.data.management.Application;
import uk.gov.hmcts.reform.pip.data.management.models.publication.Artefact;
import uk.gov.hmcts.reform.pip.data.management.utils.PublicationIntegrationTestBase;
import uk.gov.hmcts.reform.pip.model.publication.Sensitivity;
import uk.gov.hmcts.reform.pip.model.report.PublicationMiData;

import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = {Application.class},
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@ActiveProfiles("integration")
@AutoConfigureEmbeddedDatabase(type = AutoConfigureEmbeddedDatabase.DatabaseType.POSTGRES)
@WithMockUser(username = "admin", authorities = {"APPROLE_api.request.admin"})
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class PublicationReportingTest extends PublicationIntegrationTestBase {
    private static final String PUBLICATION_URL = "/publication";
    private static final String REPORT_NO_MATCH_ARTEFACTS_URL = PUBLICATION_URL + "/no-match/reporting";
    private static final String MI_REPORTING_DATA_URL = PUBLICATION_URL + "/mi-data";
    private static final String PROVENANCE = "MANUAL_UPLOAD";
    private static final String ADMIN = "admin";
    private static final String VALIDATION_MI_REPORT = "Should successfully retrieve MI data";

    @BeforeAll
    void setup() throws Exception {
        try (InputStream csvInputStream = PublicationTest.class.getClassLoader()
            .getResourceAsStream("location/UpdatedCsv.csv")) {
            MockMultipartFile csvFile
                = new MockMultipartFile("locationList", csvInputStream);

            mockMvc.perform(MockMvcRequestBuilders.multipart("/locations/upload").file(csvFile)
                                .with(user(ADMIN)
                                          .authorities(new SimpleGrantedAuthority("APPROLE_api.request.admin"))))
                .andExpect(status().isOk()).andReturn();
        }
    }

    @Test
    void testGetMiDataSuccess() throws Exception {
        Artefact artefact = createDailyList(Sensitivity.PUBLIC);

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
            .get(MI_REPORTING_DATA_URL);

        MvcResult responseMiData = mockMvc.perform(request).andExpect(status().isOk()).andReturn();
        assertNotNull(responseMiData.getResponse(), VALIDATION_MI_REPORT);

        List<PublicationMiData> miData  =
            Arrays.asList(
                OBJECT_MAPPER.readValue(responseMiData.getResponse().getContentAsString(), PublicationMiData[].class)
            );

        assertTrue(miData.stream().anyMatch(data ->
                                                data.getArtefactId().equals(artefact.getArtefactId())
                                                    && data.getLocationId().equals(artefact.getLocationId())
                                                    && "Updated Location".equals(data.getLocationName())),
                   VALIDATION_MI_REPORT);
    }

    @Test
    void testGetMiDataSuccessWithNoMatch() throws Exception {
        Artefact artefact = createSscsDailyList(Sensitivity.PUBLIC, "UnknownProvenance");

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
            .get(MI_REPORTING_DATA_URL);

        MvcResult responseMiData = mockMvc.perform(request).andExpect(status().isOk()).andReturn();
        assertNotNull(responseMiData.getResponse(), VALIDATION_MI_REPORT);

        List<PublicationMiData> miData  =
            Arrays.asList(
                OBJECT_MAPPER.readValue(responseMiData.getResponse().getContentAsString(), PublicationMiData[].class)
            );

        assertTrue(miData.stream().anyMatch(data ->
                                                data.getArtefactId().equals(artefact.getArtefactId())
                                                    && data.getLocationId().equals(artefact.getLocationId())
                                                    && data.getLocationName() == null),
                   VALIDATION_MI_REPORT);
    }

    @Test
    void testGetMiDataSuccessWithCourtIdThatDoesNotExist() throws Exception {
        Artefact artefact = createDailyList(Sensitivity.PUBLIC, LocalDateTime.now(),
                                            LocalDateTime.now(), LocalDateTime.now(),
                                            PROVENANCE, "12345");

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
            .get(MI_REPORTING_DATA_URL);

        MvcResult responseMiData = mockMvc.perform(request).andExpect(status().isOk()).andReturn();
        assertNotNull(responseMiData.getResponse(), VALIDATION_MI_REPORT);

        List<PublicationMiData> miData  =
            Arrays.asList(
                OBJECT_MAPPER.readValue(responseMiData.getResponse().getContentAsString(), PublicationMiData[].class)
            );

        assertTrue(miData.stream().anyMatch(data ->
                                                data.getArtefactId().equals(artefact.getArtefactId())
                                                    && data.getLocationId().equals(artefact.getLocationId())
                                                    && data.getLocationName() == null),
                   VALIDATION_MI_REPORT);
    }

    @Test
    void testReportNoMatchArtefactsSuccess() throws Exception {
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
            .post(REPORT_NO_MATCH_ARTEFACTS_URL);

        mockMvc.perform(request).andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(username = ADMIN, authorities = { "APPROLE_api.request.unknown" })
    void testUnauthorizedGetMiData() throws Exception {
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
            .get(MI_REPORTING_DATA_URL);

        mockMvc.perform(request)
            .andExpect(status().isForbidden())
            .andReturn();
    }

    @Test
    @WithMockUser(username = ADMIN, authorities = { "APPROLE_api.request.unknown" })
    void testUnauthorizedReportNoMatchArtefacts() throws Exception {
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post(REPORT_NO_MATCH_ARTEFACTS_URL);

        mockMvc.perform(request)
            .andExpect(status().isForbidden())
            .andReturn();
    }
}
