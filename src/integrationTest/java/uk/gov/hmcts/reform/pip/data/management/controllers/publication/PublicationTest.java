package uk.gov.hmcts.reform.pip.data.management.controllers.publication;

import com.azure.core.util.BinaryData;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.microsoft.applicationinsights.web.dependencies.apachecommons.io.IOUtils;
import io.zonky.test.db.AutoConfigureEmbeddedDatabase;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import uk.gov.hmcts.reform.pip.data.management.Application;
import uk.gov.hmcts.reform.pip.data.management.config.AzureBlobConfigurationTestConfiguration;
import uk.gov.hmcts.reform.pip.data.management.config.PublicationConfiguration;
import uk.gov.hmcts.reform.pip.data.management.models.publication.Artefact;
import uk.gov.hmcts.reform.pip.data.management.utils.PublicationIntegrationTestBase;
import uk.gov.hmcts.reform.pip.model.publication.ArtefactType;
import uk.gov.hmcts.reform.pip.model.publication.Language;
import uk.gov.hmcts.reform.pip.model.publication.ListType;
import uk.gov.hmcts.reform.pip.model.publication.Sensitivity;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import static org.junit.Assert.fail;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = {Application.class, AzureBlobConfigurationTestConfiguration.class},
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("integration")
@SuppressWarnings({"PMD.ExcessiveImports", "PMD.TooManyMethods"})
@AutoConfigureEmbeddedDatabase(type = AutoConfigureEmbeddedDatabase.DatabaseType.POSTGRES)
@WithMockUser(username = "admin", authorities = {"APPROLE_api.request.admin"})
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class PublicationTest extends PublicationIntegrationTestBase {
    private static final String PUBLICATION_URL = "/publication";
    private static final String FILE_URL = "/file";
    private static final String PAYLOAD_URL = "/payload";
    private static final String ARCHIVE_EXPIRED_ARTEFACTS_URL = PUBLICATION_URL + "/expired";
    private static final ArtefactType ARTEFACT_TYPE = ArtefactType.LIST;
    private static final Sensitivity SENSITIVITY = Sensitivity.PUBLIC;
    private static final String USER_ID = UUID.randomUUID().toString();
    private static final String PROVENANCE = "MANUAL_UPLOAD";
    private static final String SOURCE_ARTEFACT_ID = "sourceArtefactId";
    private static final LocalDateTime DISPLAY_TO = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
    private static final LocalDateTime DISPLAY_FROM = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
    private static final Language LANGUAGE = Language.ENGLISH;
    private static final ListType LIST_TYPE = ListType.CIVIL_DAILY_CAUSE_LIST;
    private static final String COURT_ID = "1";
    private static final LocalDateTime CONTENT_DATE = LocalDateTime.now().toLocalDate().atStartOfDay()
        .truncatedTo(ChronoUnit.SECONDS);
    private static final String USER_ID_HEADER = "x-user-id";

    private static final String ADMIN_HEADER = "x-admin";
    private static final String ISSUER_HEADER = "x-issuer-id";
    private static final String EMAIL = "test@email.com";

    private static final String VALIDATION_EMPTY_RESPONSE = "Response should contain a Artefact";
    private static final String SHOULD_RETURN_EXPECTED_ARTEFACT = "Should return expected artefact";


    private static String payload = "payload";
    private static MockMultipartFile file;

    @BeforeAll
    void setup() throws Exception {
        file = new MockMultipartFile("file", "test.pdf",
                                     MediaType.APPLICATION_PDF_VALUE, "test content".getBytes(
            StandardCharsets.UTF_8)
        );

        try (InputStream is = PublicationTest.class.getClassLoader()
            .getResourceAsStream("data/artefact.json")) {
            payload = new String(IOUtils.toByteArray(
                Objects.requireNonNull(is)));
        }

        try (InputStream csvInputStream = PublicationTest.class.getClassLoader()
            .getResourceAsStream("location/UpdatedCsv.csv")) {
            MockMultipartFile csvFile
                = new MockMultipartFile("locationList", csvInputStream);

            mockMvc.perform(MockMvcRequestBuilders.multipart("/locations/upload").file(csvFile)
                                .with(user("admin")
                                          .authorities(new SimpleGrantedAuthority("APPROLE_api.request.admin"))))
                .andExpect(status().isOk()).andReturn();
        }
    }

    @Test
    @DisplayName("File endpoint should return the file when artefact exists")
    void retrieveFileFromAnArtefactWhereFound() throws Exception {
        MockHttpServletRequestBuilder mockHttpServletRequestBuilder = MockMvcRequestBuilders
            .multipart(PUBLICATION_URL)
            .file(file);

        mockHttpServletRequestBuilder.header(PublicationConfiguration.TYPE_HEADER, ARTEFACT_TYPE)
            .header(PublicationConfiguration.SENSITIVITY_HEADER, SENSITIVITY)
            .header(PublicationConfiguration.LANGUAGE_HEADER, LANGUAGE)
            .header(PublicationConfiguration.PROVENANCE_HEADER, PROVENANCE)
            .header(PublicationConfiguration.SOURCE_ARTEFACT_ID_HEADER, SOURCE_ARTEFACT_ID)
            .header(PublicationConfiguration.DISPLAY_TO_HEADER, DISPLAY_TO.plusMonths(1))
            .header(PublicationConfiguration.DISPLAY_FROM_HEADER, DISPLAY_FROM)
            .header(PublicationConfiguration.LIST_TYPE, LIST_TYPE)
            .header(PublicationConfiguration.COURT_ID, COURT_ID)
            .header(PublicationConfiguration.CONTENT_DATE, CONTENT_DATE)
            .header(PublicationConfiguration.LANGUAGE_HEADER, LANGUAGE)
            .contentType(MediaType.MULTIPART_FORM_DATA);

        MvcResult response =
            mockMvc.perform(mockHttpServletRequestBuilder).andExpect(status().isCreated()).andReturn();

        assertNotNull(response.getResponse().getContentAsString(), VALIDATION_EMPTY_RESPONSE);

        Artefact artefact = OBJECT_MAPPER.readValue(response.getResponse().getContentAsString(), Artefact.class);

        when(blobClient.downloadContent()).thenReturn(BinaryData.fromString(new String(file.getBytes())));

        response = mockMvc.perform(MockMvcRequestBuilders
                                       .get(PUBLICATION_URL + "/" + artefact.getArtefactId() + FILE_URL)
                                       .header(USER_ID_HEADER, USER_ID))
            .andExpect(status().isOk()).andReturn();

        assertNotNull(response.getResponse().getContentAsString(), VALIDATION_EMPTY_RESPONSE);
        assertEquals(new String(file.getBytes()),
                     response.getResponse().getContentAsString(), "File does not match expected content");
    }

    @Test
    @DisplayName("File endpoint should return the file when artefact exists")
    void retrieveFileFromAnArtefactWhereAdmin() throws Exception {
        MockHttpServletRequestBuilder mockHttpServletRequestBuilder = MockMvcRequestBuilders
            .multipart(PUBLICATION_URL)
            .file(file);

        mockHttpServletRequestBuilder.header(PublicationConfiguration.TYPE_HEADER, ARTEFACT_TYPE)
            .header(PublicationConfiguration.SENSITIVITY_HEADER, Sensitivity.CLASSIFIED)
            .header(PublicationConfiguration.LANGUAGE_HEADER, LANGUAGE)
            .header(PublicationConfiguration.PROVENANCE_HEADER, PROVENANCE)
            .header(PublicationConfiguration.SOURCE_ARTEFACT_ID_HEADER, SOURCE_ARTEFACT_ID)
            .header(PublicationConfiguration.DISPLAY_TO_HEADER, DISPLAY_TO.plusMonths(1))
            .header(PublicationConfiguration.DISPLAY_FROM_HEADER, DISPLAY_FROM)
            .header(PublicationConfiguration.LIST_TYPE, LIST_TYPE)
            .header(PublicationConfiguration.COURT_ID, COURT_ID)
            .header(PublicationConfiguration.CONTENT_DATE, CONTENT_DATE)
            .header(PublicationConfiguration.LANGUAGE_HEADER, LANGUAGE)
            .contentType(MediaType.MULTIPART_FORM_DATA);

        MvcResult response =
            mockMvc.perform(mockHttpServletRequestBuilder).andExpect(status().isCreated()).andReturn();

        assertNotNull(response.getResponse().getContentAsString(), VALIDATION_EMPTY_RESPONSE);

        Artefact artefact = OBJECT_MAPPER.readValue(response.getResponse().getContentAsString(), Artefact.class);

        when(blobClient.downloadContent()).thenReturn(BinaryData.fromString(new String(file.getBytes())));

        response = mockMvc.perform(MockMvcRequestBuilders
                                       .get(PUBLICATION_URL + "/" + artefact.getArtefactId() + FILE_URL)
                                       .header(ADMIN_HEADER, true))
            .andExpect(status().isOk()).andReturn();

        assertNotNull(response.getResponse().getContentAsString(), VALIDATION_EMPTY_RESPONSE);

        assertEquals(new String(file.getBytes()),
                     response.getResponse().getContentAsString(), "File does not match expected content");
    }

    @Test
    @DisplayName("File endpoint should not return the file when user not supplied")
    void retrieveFileOfAnArtefactWhereUserNotSupplied() throws Exception {
        MockHttpServletRequestBuilder mockHttpServletRequestBuilder = MockMvcRequestBuilders
            .multipart(PUBLICATION_URL)
            .file(file);

        mockHttpServletRequestBuilder.header(PublicationConfiguration.TYPE_HEADER, ARTEFACT_TYPE)
            .header(PublicationConfiguration.SENSITIVITY_HEADER, Sensitivity.CLASSIFIED)
            .header(PublicationConfiguration.LANGUAGE_HEADER, LANGUAGE)
            .header(PublicationConfiguration.PROVENANCE_HEADER, PROVENANCE)
            .header(PublicationConfiguration.SOURCE_ARTEFACT_ID_HEADER, SOURCE_ARTEFACT_ID)
            .header(PublicationConfiguration.DISPLAY_TO_HEADER, DISPLAY_TO.plusMonths(1))
            .header(PublicationConfiguration.DISPLAY_FROM_HEADER, DISPLAY_FROM)
            .header(PublicationConfiguration.LIST_TYPE, LIST_TYPE)
            .header(PublicationConfiguration.COURT_ID, COURT_ID)
            .header(PublicationConfiguration.CONTENT_DATE, CONTENT_DATE)
            .header(PublicationConfiguration.LANGUAGE_HEADER, LANGUAGE)
            .contentType(MediaType.MULTIPART_FORM_DATA);

        MvcResult response =
            mockMvc.perform(mockHttpServletRequestBuilder).andExpect(status().isCreated()).andReturn();

        assertNotNull(response.getResponse().getContentAsString(), VALIDATION_EMPTY_RESPONSE);

        Artefact artefact = OBJECT_MAPPER.readValue(response.getResponse().getContentAsString(), Artefact.class);

        mockMvc.perform(MockMvcRequestBuilders
                            .get(PUBLICATION_URL + "/" + artefact.getArtefactId() + FILE_URL))
            .andExpect(status().isNotFound()).andReturn();
    }

    @Test
    @DisplayName("File endpoint should return the file when user is authorised")
    void retrieveFileOfAnArtefactWhereUserAuthorized() throws Exception {
        when(accountManagementService.getIsAuthorised(UUID.fromString(USER_ID), LIST_TYPE, Sensitivity.CLASSIFIED))
            .thenReturn(true);
        when(blobClient.downloadContent()).thenReturn(BinaryData.fromString(new String(file.getBytes())));

        MockHttpServletRequestBuilder mockHttpServletRequestBuilder = MockMvcRequestBuilders
            .multipart(PUBLICATION_URL)
            .file(file);

        mockHttpServletRequestBuilder.header(PublicationConfiguration.TYPE_HEADER, ARTEFACT_TYPE)
            .header(PublicationConfiguration.SENSITIVITY_HEADER, Sensitivity.CLASSIFIED)
            .header(PublicationConfiguration.LANGUAGE_HEADER, LANGUAGE)
            .header(PublicationConfiguration.PROVENANCE_HEADER, PROVENANCE)
            .header(PublicationConfiguration.SOURCE_ARTEFACT_ID_HEADER, SOURCE_ARTEFACT_ID)
            .header(PublicationConfiguration.DISPLAY_TO_HEADER, DISPLAY_TO.plusMonths(1))
            .header(PublicationConfiguration.DISPLAY_FROM_HEADER, DISPLAY_FROM)
            .header(PublicationConfiguration.LIST_TYPE, LIST_TYPE)
            .header(PublicationConfiguration.COURT_ID, COURT_ID)
            .header(PublicationConfiguration.CONTENT_DATE, CONTENT_DATE)
            .header(PublicationConfiguration.LANGUAGE_HEADER, LANGUAGE)
            .contentType(MediaType.MULTIPART_FORM_DATA);

        MvcResult response =
            mockMvc.perform(mockHttpServletRequestBuilder).andExpect(status().isCreated()).andReturn();

        assertNotNull(response.getResponse().getContentAsString(), VALIDATION_EMPTY_RESPONSE);

        Artefact artefact = OBJECT_MAPPER.readValue(response.getResponse().getContentAsString(), Artefact.class);

        response = mockMvc.perform(MockMvcRequestBuilders
                                       .get(PUBLICATION_URL + "/" + artefact.getArtefactId() + FILE_URL)
                                       .header(USER_ID_HEADER, USER_ID))
            .andExpect(status().isOk()).andReturn();

        assertNotNull(response.getResponse().getContentAsString(), VALIDATION_EMPTY_RESPONSE);
    }

    @Test
    @DisplayName("File endpoint should not return the file when user is not authorised")
    void retrieveFileOfAnArtefactWhereUserNotAuthorized() throws Exception {
        when(accountManagementService.getIsAuthorised(UUID.fromString(USER_ID), LIST_TYPE, Sensitivity.CLASSIFIED))
            .thenReturn(false);

        MockHttpServletRequestBuilder mockHttpServletRequestBuilder = MockMvcRequestBuilders
            .multipart(PUBLICATION_URL)
            .file(file);

        mockHttpServletRequestBuilder.header(PublicationConfiguration.TYPE_HEADER, ARTEFACT_TYPE)
            .header(PublicationConfiguration.SENSITIVITY_HEADER, Sensitivity.CLASSIFIED)
            .header(PublicationConfiguration.LANGUAGE_HEADER, LANGUAGE)
            .header(PublicationConfiguration.PROVENANCE_HEADER, PROVENANCE)
            .header(PublicationConfiguration.SOURCE_ARTEFACT_ID_HEADER, SOURCE_ARTEFACT_ID)
            .header(PublicationConfiguration.DISPLAY_TO_HEADER, DISPLAY_TO.plusMonths(1))
            .header(PublicationConfiguration.DISPLAY_FROM_HEADER, DISPLAY_FROM)
            .header(PublicationConfiguration.LIST_TYPE, LIST_TYPE)
            .header(PublicationConfiguration.COURT_ID, COURT_ID)
            .header(PublicationConfiguration.CONTENT_DATE, CONTENT_DATE)
            .header(PublicationConfiguration.LANGUAGE_HEADER, LANGUAGE)
            .contentType(MediaType.MULTIPART_FORM_DATA);

        MvcResult response =
            mockMvc.perform(mockHttpServletRequestBuilder).andExpect(status().isCreated()).andReturn();

        assertNotNull(response.getResponse().getContentAsString(), VALIDATION_EMPTY_RESPONSE);

        Artefact artefact = OBJECT_MAPPER.readValue(
            response.getResponse().getContentAsString(), Artefact.class);

        mockMvc.perform(MockMvcRequestBuilders
                            .get(PUBLICATION_URL + "/" + artefact.getArtefactId() + FILE_URL)
                            .header(USER_ID_HEADER, USER_ID))
            .andExpect(status().isNotFound()).andReturn();
    }

    @Test
    @DisplayName("File endpoint should return 404 when artefact does not exist")
    void retrieveFileOfAnArtefactWhereNotFound() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders
                            .get("/publication/7d734e8d-ba1d-4730-bd8b-09a970be00cc/file")
                            .header(USER_ID_HEADER, USER_ID))
            .andExpect(status().isNotFound()).andReturn();
    }

    @Test
    @DisplayName("Payload endpoint should return the payload when artefact exists when verified")
    void retrievePayloadOfAnArtefactWhereFoundWhenVerified() throws Exception {
        MockHttpServletRequestBuilder mockHttpServletRequestBuilder = MockMvcRequestBuilders
            .post(PUBLICATION_URL)
            .content(payload);

        mockHttpServletRequestBuilder.header(PublicationConfiguration.TYPE_HEADER, ARTEFACT_TYPE)
            .header(PublicationConfiguration.SENSITIVITY_HEADER, SENSITIVITY)
            .header(PublicationConfiguration.LANGUAGE_HEADER, LANGUAGE)
            .header(PublicationConfiguration.PROVENANCE_HEADER, PROVENANCE)
            .header(PublicationConfiguration.SOURCE_ARTEFACT_ID_HEADER, SOURCE_ARTEFACT_ID)
            .header(PublicationConfiguration.DISPLAY_TO_HEADER, DISPLAY_TO.plusMonths(1))
            .header(PublicationConfiguration.DISPLAY_FROM_HEADER, DISPLAY_FROM)
            .header(PublicationConfiguration.LIST_TYPE, LIST_TYPE)
            .header(PublicationConfiguration.COURT_ID, COURT_ID)
            .header(PublicationConfiguration.CONTENT_DATE, CONTENT_DATE)
            .header(PublicationConfiguration.LANGUAGE_HEADER, LANGUAGE)
            .contentType(MediaType.APPLICATION_JSON);

        MvcResult response =
            mockMvc.perform(mockHttpServletRequestBuilder).andExpect(status().isCreated()).andReturn();

        assertNotNull(response.getResponse().getContentAsString(), VALIDATION_EMPTY_RESPONSE);

        Artefact artefact = OBJECT_MAPPER.readValue(response.getResponse().getContentAsString(), Artefact.class);

        when(blobClient.downloadContent()).thenReturn(BinaryData.fromString(payload));

        response = mockMvc.perform(MockMvcRequestBuilders
                                       .get(PUBLICATION_URL + "/" + artefact.getArtefactId() + PAYLOAD_URL)
                                       .header(USER_ID_HEADER, USER_ID))
            .andExpect(status().isOk()).andReturn();

        assertNotNull(response.getResponse().getContentAsString(), VALIDATION_EMPTY_RESPONSE);

        assertEquals(payload, response.getResponse().getContentAsString(),
                     "Payload does not match expected content");
    }

    @Test
    @DisplayName("Payload endpoint should not return the payload when artefact out of range and user verified")
    void retrievePayloadOfAnArtefactWhereOutOfDateRangeWhenVerified() throws Exception {
        MockHttpServletRequestBuilder mockHttpServletRequestBuilder = MockMvcRequestBuilders
            .post(PUBLICATION_URL)
            .content(payload);

        mockHttpServletRequestBuilder.header(PublicationConfiguration.TYPE_HEADER, ARTEFACT_TYPE)
            .header(PublicationConfiguration.SENSITIVITY_HEADER, SENSITIVITY)
            .header(PublicationConfiguration.LANGUAGE_HEADER, LANGUAGE)
            .header(PublicationConfiguration.PROVENANCE_HEADER, PROVENANCE)
            .header(PublicationConfiguration.SOURCE_ARTEFACT_ID_HEADER, SOURCE_ARTEFACT_ID)
            .header(PublicationConfiguration.DISPLAY_TO_HEADER, DISPLAY_TO.plusMonths(2))
            .header(PublicationConfiguration.DISPLAY_FROM_HEADER, DISPLAY_FROM.plusMonths(1))
            .header(PublicationConfiguration.LIST_TYPE, LIST_TYPE)
            .header(PublicationConfiguration.COURT_ID, COURT_ID)
            .header(PublicationConfiguration.CONTENT_DATE, CONTENT_DATE)
            .header(PublicationConfiguration.LANGUAGE_HEADER, LANGUAGE)
            .contentType(MediaType.APPLICATION_JSON);

        MvcResult response =
            mockMvc.perform(mockHttpServletRequestBuilder).andExpect(status().isCreated()).andReturn();

        assertNotNull(response.getResponse().getContentAsString(), VALIDATION_EMPTY_RESPONSE);

        Artefact artefact = OBJECT_MAPPER.readValue(response.getResponse().getContentAsString(), Artefact.class);

        when(blobClient.downloadContent()).thenReturn(BinaryData.fromString(payload));

        mockMvc.perform(MockMvcRequestBuilders
                            .get(PUBLICATION_URL + "/" + artefact.getArtefactId() + PAYLOAD_URL)
                            .header(USER_ID_HEADER, USER_ID))
            .andExpect(status().isNotFound()).andReturn();
    }

    @Test
    @DisplayName("Payload endpoint should not return the payload when user not supplied")
    void retrievePayloadOfAnArtefactWhereUserNotSupplied() throws Exception {
        MockHttpServletRequestBuilder mockHttpServletRequestBuilder = MockMvcRequestBuilders
            .post(PUBLICATION_URL)
            .content(payload);

        mockHttpServletRequestBuilder.header(PublicationConfiguration.TYPE_HEADER, ARTEFACT_TYPE)
            .header(PublicationConfiguration.SENSITIVITY_HEADER, Sensitivity.CLASSIFIED)
            .header(PublicationConfiguration.LANGUAGE_HEADER, LANGUAGE)
            .header(PublicationConfiguration.PROVENANCE_HEADER, PROVENANCE)
            .header(PublicationConfiguration.SOURCE_ARTEFACT_ID_HEADER, SOURCE_ARTEFACT_ID)
            .header(PublicationConfiguration.DISPLAY_TO_HEADER, DISPLAY_TO.plusMonths(1))
            .header(PublicationConfiguration.DISPLAY_FROM_HEADER, DISPLAY_FROM)
            .header(PublicationConfiguration.LIST_TYPE, LIST_TYPE)
            .header(PublicationConfiguration.COURT_ID, COURT_ID)
            .header(PublicationConfiguration.CONTENT_DATE, CONTENT_DATE)
            .header(PublicationConfiguration.LANGUAGE_HEADER, LANGUAGE)
            .contentType(MediaType.APPLICATION_JSON);

        MvcResult response =
            mockMvc.perform(mockHttpServletRequestBuilder).andExpect(status().isCreated()).andReturn();

        assertNotNull(response.getResponse().getContentAsString(), VALIDATION_EMPTY_RESPONSE);

        Artefact artefact = OBJECT_MAPPER.readValue(response.getResponse().getContentAsString(), Artefact.class);

        mockMvc.perform(MockMvcRequestBuilders
                            .get(PUBLICATION_URL + "/" + artefact.getArtefactId() + PAYLOAD_URL))
            .andExpect(status().isNotFound()).andReturn();
    }

    @Test
    @DisplayName("Payload endpoint should return the payload when user is authorised")
    void retrievePayloadOfAnArtefactWhereUserAuthorized() throws Exception {
        when(accountManagementService.getIsAuthorised(UUID.fromString(USER_ID), LIST_TYPE, Sensitivity.CLASSIFIED))
            .thenReturn(true);
        when(blobClient.downloadContent()).thenReturn(BinaryData.fromString(payload));

        MockHttpServletRequestBuilder mockHttpServletRequestBuilder = MockMvcRequestBuilders
            .post(PUBLICATION_URL)
            .content(payload);

        mockHttpServletRequestBuilder.header(PublicationConfiguration.TYPE_HEADER, ARTEFACT_TYPE)
            .header(PublicationConfiguration.SENSITIVITY_HEADER, Sensitivity.CLASSIFIED)
            .header(PublicationConfiguration.LANGUAGE_HEADER, LANGUAGE)
            .header(PublicationConfiguration.PROVENANCE_HEADER, PROVENANCE)
            .header(PublicationConfiguration.SOURCE_ARTEFACT_ID_HEADER, SOURCE_ARTEFACT_ID)
            .header(PublicationConfiguration.DISPLAY_TO_HEADER, DISPLAY_TO.plusMonths(1))
            .header(PublicationConfiguration.DISPLAY_FROM_HEADER, DISPLAY_FROM)
            .header(PublicationConfiguration.LIST_TYPE, LIST_TYPE)
            .header(PublicationConfiguration.COURT_ID, COURT_ID)
            .header(PublicationConfiguration.CONTENT_DATE, CONTENT_DATE)
            .header(PublicationConfiguration.LANGUAGE_HEADER, LANGUAGE)
            .contentType(MediaType.APPLICATION_JSON);

        MvcResult response =
            mockMvc.perform(mockHttpServletRequestBuilder).andExpect(status().isCreated()).andReturn();

        assertNotNull(response.getResponse().getContentAsString(), VALIDATION_EMPTY_RESPONSE);

        Artefact artefact = OBJECT_MAPPER.readValue(response.getResponse().getContentAsString(), Artefact.class);

        mockMvc.perform(MockMvcRequestBuilders
                            .get(PUBLICATION_URL + "/" + artefact.getArtefactId() + PAYLOAD_URL)
                            .header(USER_ID_HEADER, USER_ID))
            .andExpect(status().isOk()).andReturn();

        assertNotNull(response.getResponse().getContentAsString(), VALIDATION_EMPTY_RESPONSE);
    }

    @Test
    @DisplayName("Payload endpoint should not return the payload when user is not authorised")
    void retrievePayloadOfAnArtefactWhereUserNotAuthorized() throws Exception {
        when(accountManagementService.getIsAuthorised(UUID.fromString(USER_ID), LIST_TYPE, Sensitivity.CLASSIFIED))
            .thenReturn(false);

        MockHttpServletRequestBuilder mockHttpServletRequestBuilder = MockMvcRequestBuilders
            .post(PUBLICATION_URL)
            .content(payload);

        mockHttpServletRequestBuilder.header(PublicationConfiguration.TYPE_HEADER, ARTEFACT_TYPE)
            .header(PublicationConfiguration.SENSITIVITY_HEADER, Sensitivity.CLASSIFIED)
            .header(PublicationConfiguration.LANGUAGE_HEADER, LANGUAGE)
            .header(PublicationConfiguration.PROVENANCE_HEADER, PROVENANCE)
            .header(PublicationConfiguration.SOURCE_ARTEFACT_ID_HEADER, SOURCE_ARTEFACT_ID)
            .header(PublicationConfiguration.DISPLAY_TO_HEADER, DISPLAY_TO.plusMonths(1))
            .header(PublicationConfiguration.DISPLAY_FROM_HEADER, DISPLAY_FROM)
            .header(PublicationConfiguration.LIST_TYPE, LIST_TYPE)
            .header(PublicationConfiguration.COURT_ID, COURT_ID)
            .header(PublicationConfiguration.CONTENT_DATE, CONTENT_DATE)
            .header(PublicationConfiguration.LANGUAGE_HEADER, LANGUAGE)
            .contentType(MediaType.APPLICATION_JSON);

        MvcResult response =
            mockMvc.perform(mockHttpServletRequestBuilder).andExpect(status().isCreated()).andReturn();

        assertNotNull(response.getResponse().getContentAsString(), VALIDATION_EMPTY_RESPONSE);

        Artefact artefact = OBJECT_MAPPER.readValue(response.getResponse().getContentAsString(), Artefact.class);

        mockMvc.perform(MockMvcRequestBuilders
                            .get(PUBLICATION_URL + "/" + artefact.getArtefactId() + PAYLOAD_URL)
                            .header(USER_ID_HEADER, USER_ID))
            .andExpect(status().isNotFound()).andReturn();
    }

    @Test
    @DisplayName("Payload endpoint should return the payload when artefact exists when unverified")
    void retrievePayloadOfAnArtefactWhereFoundWhenUnverified() throws Exception {
        MockHttpServletRequestBuilder mockHttpServletRequestBuilder = MockMvcRequestBuilders
            .post(PUBLICATION_URL)
            .content(payload);

        mockHttpServletRequestBuilder.header(PublicationConfiguration.TYPE_HEADER, ARTEFACT_TYPE)
            .header(PublicationConfiguration.SENSITIVITY_HEADER, SENSITIVITY)
            .header(PublicationConfiguration.LANGUAGE_HEADER, LANGUAGE)
            .header(PublicationConfiguration.PROVENANCE_HEADER, PROVENANCE)
            .header(PublicationConfiguration.SOURCE_ARTEFACT_ID_HEADER, SOURCE_ARTEFACT_ID)
            .header(PublicationConfiguration.DISPLAY_TO_HEADER, DISPLAY_TO.plusMonths(1))
            .header(PublicationConfiguration.DISPLAY_FROM_HEADER, DISPLAY_FROM)
            .header(PublicationConfiguration.LIST_TYPE, LIST_TYPE)
            .header(PublicationConfiguration.COURT_ID, COURT_ID)
            .header(PublicationConfiguration.CONTENT_DATE, CONTENT_DATE)
            .header(PublicationConfiguration.LANGUAGE_HEADER, LANGUAGE)
            .contentType(MediaType.APPLICATION_JSON);

        MvcResult response =
            mockMvc.perform(mockHttpServletRequestBuilder).andExpect(status().isCreated()).andReturn();

        assertNotNull(response.getResponse().getContentAsString(), VALIDATION_EMPTY_RESPONSE);

        Artefact artefact = OBJECT_MAPPER.readValue(
            response.getResponse().getContentAsString(), Artefact.class);

        when(blobClient.downloadContent()).thenReturn(BinaryData.fromString(payload));

        response = mockMvc.perform(MockMvcRequestBuilders
                                       .get(PUBLICATION_URL + "/" + artefact.getArtefactId() + PAYLOAD_URL))
            .andExpect(status().isOk()).andReturn();

        assertNotNull(response.getResponse().getContentAsString(), VALIDATION_EMPTY_RESPONSE);

        assertEquals(payload, response.getResponse().getContentAsString(),
                     "Payload does not match expected content");
    }

    @Test
    @DisplayName("Payload endpoint should return the payload when artefact exists when an admin")
    void retrievePayloadOfAnArtefactWhenAdmin() throws Exception {
        MockHttpServletRequestBuilder mockHttpServletRequestBuilder = MockMvcRequestBuilders
            .post(PUBLICATION_URL)
            .content(payload);

        mockHttpServletRequestBuilder.header(PublicationConfiguration.TYPE_HEADER, ARTEFACT_TYPE)
            .header(PublicationConfiguration.SENSITIVITY_HEADER, Sensitivity.CLASSIFIED)
            .header(PublicationConfiguration.LANGUAGE_HEADER, LANGUAGE)
            .header(PublicationConfiguration.PROVENANCE_HEADER, PROVENANCE)
            .header(PublicationConfiguration.SOURCE_ARTEFACT_ID_HEADER, SOURCE_ARTEFACT_ID)
            .header(PublicationConfiguration.DISPLAY_TO_HEADER, DISPLAY_TO.plusMonths(1))
            .header(PublicationConfiguration.DISPLAY_FROM_HEADER, DISPLAY_FROM)
            .header(PublicationConfiguration.LIST_TYPE, LIST_TYPE)
            .header(PublicationConfiguration.COURT_ID, COURT_ID)
            .header(PublicationConfiguration.CONTENT_DATE, CONTENT_DATE)
            .header(PublicationConfiguration.LANGUAGE_HEADER, LANGUAGE)
            .contentType(MediaType.APPLICATION_JSON);

        MvcResult response =
            mockMvc.perform(mockHttpServletRequestBuilder).andExpect(status().isCreated()).andReturn();

        assertNotNull(response.getResponse().getContentAsString(), VALIDATION_EMPTY_RESPONSE);

        Artefact artefact = OBJECT_MAPPER.readValue(
            response.getResponse().getContentAsString(), Artefact.class);

        when(blobClient.downloadContent()).thenReturn(BinaryData.fromString(payload));

        response = mockMvc.perform(MockMvcRequestBuilders
                                       .get(PUBLICATION_URL + "/" + artefact.getArtefactId() + PAYLOAD_URL)
                                       .header(ADMIN_HEADER, true))
            .andExpect(status().isOk()).andReturn();

        assertNotNull(response.getResponse().getContentAsString(), VALIDATION_EMPTY_RESPONSE);

        assertEquals(payload, response.getResponse().getContentAsString(),
                     "Payload does not match expected content");
    }

    @Test
    @DisplayName("Payload endpoint should not return the payload when artefact out of range and user unverified")
    void retrievePayloadOfAnArtefactWhereOutOfDateRangeWhenUnverified() throws Exception {
        MockHttpServletRequestBuilder mockHttpServletRequestBuilder = MockMvcRequestBuilders
            .post(PUBLICATION_URL)
            .content(payload);

        mockHttpServletRequestBuilder.header(PublicationConfiguration.TYPE_HEADER, ARTEFACT_TYPE)
            .header(PublicationConfiguration.SENSITIVITY_HEADER, SENSITIVITY)
            .header(PublicationConfiguration.LANGUAGE_HEADER, LANGUAGE)
            .header(PublicationConfiguration.PROVENANCE_HEADER, PROVENANCE)
            .header(PublicationConfiguration.SOURCE_ARTEFACT_ID_HEADER, SOURCE_ARTEFACT_ID)
            .header(PublicationConfiguration.DISPLAY_TO_HEADER, DISPLAY_TO.plusMonths(2))
            .header(PublicationConfiguration.DISPLAY_FROM_HEADER, DISPLAY_FROM.plusMonths(1))
            .header(PublicationConfiguration.LIST_TYPE, LIST_TYPE)
            .header(PublicationConfiguration.COURT_ID, COURT_ID)
            .header(PublicationConfiguration.CONTENT_DATE, CONTENT_DATE)
            .header(PublicationConfiguration.LANGUAGE_HEADER, LANGUAGE)
            .contentType(MediaType.APPLICATION_JSON);

        MvcResult response =
            mockMvc.perform(mockHttpServletRequestBuilder).andExpect(status().isCreated()).andReturn();

        assertNotNull(response.getResponse().getContentAsString(), VALIDATION_EMPTY_RESPONSE);

        Artefact artefact = OBJECT_MAPPER.readValue(
            response.getResponse().getContentAsString(), Artefact.class);

        when(blobClient.downloadContent()).thenReturn(BinaryData.fromString(payload));

        mockMvc.perform(MockMvcRequestBuilders
                            .get(PUBLICATION_URL + "/" + artefact.getArtefactId() + PAYLOAD_URL))
            .andExpect(status().isNotFound()).andReturn();
    }

    @Test
    @DisplayName("Payload endpoint should return 404 when artefact does not exist")
    void retrievePayloadOfAnArtefactWhereNotFound() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders
                            .get("/publication/7d734e8d-ba1d-4730-bd8b-09a970be00cc/payload")
                            .header(USER_ID_HEADER, USER_ID))
            .andExpect(status().isNotFound()).andReturn();
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    @DisplayName("Metadata endpoint should return the artefact when artefact exists")
    void retrieveMetadataOfAnArtefactWhereFoundWhenVerified(boolean isJson) throws Exception {
        MockHttpServletRequestBuilder mockHttpServletRequestBuilder;
        if (isJson) {
            mockHttpServletRequestBuilder = MockMvcRequestBuilders.post(PUBLICATION_URL).content(payload);
        } else {
            mockHttpServletRequestBuilder = MockMvcRequestBuilders.multipart(PUBLICATION_URL).file(file);
        }
        mockHttpServletRequestBuilder.header(PublicationConfiguration.TYPE_HEADER, ARTEFACT_TYPE)
            .header(PublicationConfiguration.SENSITIVITY_HEADER, SENSITIVITY)
            .header(PublicationConfiguration.LANGUAGE_HEADER, LANGUAGE)
            .header(PublicationConfiguration.PROVENANCE_HEADER, PROVENANCE)
            .header(PublicationConfiguration.SOURCE_ARTEFACT_ID_HEADER, SOURCE_ARTEFACT_ID)
            .header(PublicationConfiguration.DISPLAY_TO_HEADER, DISPLAY_TO.plusMonths(1))
            .header(PublicationConfiguration.DISPLAY_FROM_HEADER, DISPLAY_FROM)
            .header(PublicationConfiguration.LIST_TYPE, LIST_TYPE)
            .header(PublicationConfiguration.COURT_ID, COURT_ID)
            .header(PublicationConfiguration.CONTENT_DATE, CONTENT_DATE)
            .header(PublicationConfiguration.LANGUAGE_HEADER, LANGUAGE)
            .contentType(isJson ? MediaType.APPLICATION_JSON : MediaType.MULTIPART_FORM_DATA);

        MvcResult response =
            mockMvc.perform(mockHttpServletRequestBuilder).andExpect(status().isCreated()).andReturn();

        assertNotNull(response.getResponse().getContentAsString(), VALIDATION_EMPTY_RESPONSE);

        Artefact artefact = OBJECT_MAPPER.readValue(
            response.getResponse().getContentAsString(), Artefact.class);

        when(blobClient.downloadContent()).thenReturn(
            BinaryData.fromString(isJson ? payload : new String(file.getBytes())));

        response = mockMvc.perform(MockMvcRequestBuilders
                                       .get(PUBLICATION_URL + "/" + artefact.getArtefactId())
                                       .header(USER_ID_HEADER, USER_ID))
            .andExpect(status().isOk()).andReturn();

        assertNotNull(response.getResponse().getContentAsString(), VALIDATION_EMPTY_RESPONSE);

        assertTrue(
            compareArtefacts(
                artefact,
                OBJECT_MAPPER.readValue(response.getResponse().getContentAsString(), Artefact.class)
            ),
            "Metadata does not match expected artefact"
        );
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    @DisplayName("Metadata endpoint should return the artefact when artefact exists when unverified")
    void retrieveMetadataOfAnArtefactWhereFoundWhenUnverified(boolean isJson) throws Exception {
        MockHttpServletRequestBuilder mockHttpServletRequestBuilder;
        if (isJson) {
            mockHttpServletRequestBuilder = MockMvcRequestBuilders.post(PUBLICATION_URL).content(payload);
        } else {
            mockHttpServletRequestBuilder = MockMvcRequestBuilders.multipart(PUBLICATION_URL).file(file);
        }
        mockHttpServletRequestBuilder.header(PublicationConfiguration.TYPE_HEADER, ARTEFACT_TYPE)
            .header(PublicationConfiguration.SENSITIVITY_HEADER, SENSITIVITY)
            .header(PublicationConfiguration.LANGUAGE_HEADER, LANGUAGE)
            .header(PublicationConfiguration.PROVENANCE_HEADER, PROVENANCE)
            .header(PublicationConfiguration.SOURCE_ARTEFACT_ID_HEADER, SOURCE_ARTEFACT_ID)
            .header(PublicationConfiguration.DISPLAY_TO_HEADER, DISPLAY_TO.plusMonths(1))
            .header(PublicationConfiguration.DISPLAY_FROM_HEADER, DISPLAY_FROM)
            .header(PublicationConfiguration.LIST_TYPE, LIST_TYPE)
            .header(PublicationConfiguration.COURT_ID, COURT_ID)
            .header(PublicationConfiguration.CONTENT_DATE, CONTENT_DATE)
            .header(PublicationConfiguration.LANGUAGE_HEADER, LANGUAGE)
            .contentType(isJson ? MediaType.APPLICATION_JSON : MediaType.MULTIPART_FORM_DATA);

        MvcResult response =
            mockMvc.perform(mockHttpServletRequestBuilder).andExpect(status().isCreated()).andReturn();

        assertNotNull(response.getResponse().getContentAsString(), VALIDATION_EMPTY_RESPONSE);

        Artefact artefact = OBJECT_MAPPER.readValue(
            response.getResponse().getContentAsString(), Artefact.class);

        when(blobClient.downloadContent()).thenReturn(
            BinaryData.fromString(isJson ? payload : new String(file.getBytes())));

        response = mockMvc.perform(MockMvcRequestBuilders
                                       .get(PUBLICATION_URL + "/" + artefact.getArtefactId()))
            .andExpect(status().isOk()).andReturn();

        assertNotNull(response.getResponse().getContentAsString(), VALIDATION_EMPTY_RESPONSE);

        assertTrue(
            compareArtefacts(
                artefact,
                OBJECT_MAPPER.readValue(response.getResponse().getContentAsString(), Artefact.class)
            ),
            "Metadata does not match expected artefact"
        );
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    @DisplayName("Metadata endpoint should return the artefact when artefact exists")
    void retrieveMetadataOfAnArtefactWhereOutOfDateRangeAndVerified(boolean isJson) throws Exception {
        MockHttpServletRequestBuilder mockHttpServletRequestBuilder;
        if (isJson) {
            mockHttpServletRequestBuilder = MockMvcRequestBuilders.post(PUBLICATION_URL).content(payload);
        } else {
            mockHttpServletRequestBuilder = MockMvcRequestBuilders.multipart(PUBLICATION_URL).file(file);
        }
        mockHttpServletRequestBuilder.header(PublicationConfiguration.TYPE_HEADER, ARTEFACT_TYPE)
            .header(PublicationConfiguration.SENSITIVITY_HEADER, SENSITIVITY)
            .header(PublicationConfiguration.LANGUAGE_HEADER, LANGUAGE)
            .header(PublicationConfiguration.PROVENANCE_HEADER, PROVENANCE)
            .header(PublicationConfiguration.SOURCE_ARTEFACT_ID_HEADER, SOURCE_ARTEFACT_ID)
            .header(PublicationConfiguration.DISPLAY_TO_HEADER, DISPLAY_TO.plusMonths(2))
            .header(PublicationConfiguration.DISPLAY_FROM_HEADER, DISPLAY_FROM.plusMonths(1))
            .header(PublicationConfiguration.LIST_TYPE, LIST_TYPE)
            .header(PublicationConfiguration.COURT_ID, COURT_ID)
            .header(PublicationConfiguration.CONTENT_DATE, CONTENT_DATE)
            .header(PublicationConfiguration.LANGUAGE_HEADER, LANGUAGE)
            .contentType(isJson ? MediaType.APPLICATION_JSON : MediaType.MULTIPART_FORM_DATA);

        MvcResult response =
            mockMvc.perform(mockHttpServletRequestBuilder).andExpect(status().isCreated()).andReturn();

        assertNotNull(response.getResponse().getContentAsString(), VALIDATION_EMPTY_RESPONSE);

        Artefact artefact = OBJECT_MAPPER.readValue(
            response.getResponse().getContentAsString(), Artefact.class);

        when(blobClient.downloadContent()).thenReturn(
            BinaryData.fromString(isJson ? payload : new String(file.getBytes())));

        mockMvc.perform(MockMvcRequestBuilders
                            .get(PUBLICATION_URL + "/" + artefact.getArtefactId())
                            .header(USER_ID_HEADER, USER_ID))
            .andExpect(status().isNotFound()).andReturn();
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    @DisplayName("Metadata endpoint should return the artefact when artefact exists")
    void retrieveMetadataOfAnArtefactWhereOutOfDateRangeAndUnverified(boolean isJson) throws Exception {
        MockHttpServletRequestBuilder mockHttpServletRequestBuilder;
        if (isJson) {
            mockHttpServletRequestBuilder = MockMvcRequestBuilders.post(PUBLICATION_URL).content(payload);
        } else {
            mockHttpServletRequestBuilder = MockMvcRequestBuilders.multipart(PUBLICATION_URL).file(file);
        }
        mockHttpServletRequestBuilder.header(PublicationConfiguration.TYPE_HEADER, ARTEFACT_TYPE)
            .header(PublicationConfiguration.SENSITIVITY_HEADER, SENSITIVITY)
            .header(PublicationConfiguration.LANGUAGE_HEADER, LANGUAGE)
            .header(PublicationConfiguration.PROVENANCE_HEADER, PROVENANCE)
            .header(PublicationConfiguration.SOURCE_ARTEFACT_ID_HEADER, SOURCE_ARTEFACT_ID)
            .header(PublicationConfiguration.DISPLAY_TO_HEADER, DISPLAY_TO.plusMonths(2))
            .header(PublicationConfiguration.DISPLAY_FROM_HEADER, DISPLAY_FROM.plusMonths(1))
            .header(PublicationConfiguration.LIST_TYPE, LIST_TYPE)
            .header(PublicationConfiguration.COURT_ID, COURT_ID)
            .header(PublicationConfiguration.CONTENT_DATE, CONTENT_DATE)
            .header(PublicationConfiguration.LANGUAGE_HEADER, LANGUAGE)
            .contentType(isJson ? MediaType.APPLICATION_JSON : MediaType.MULTIPART_FORM_DATA);

        MvcResult response =
            mockMvc.perform(mockHttpServletRequestBuilder).andExpect(status().isCreated()).andReturn();

        assertNotNull(response.getResponse().getContentAsString(), VALIDATION_EMPTY_RESPONSE);

        Artefact artefact = OBJECT_MAPPER.readValue(
            response.getResponse().getContentAsString(), Artefact.class);

        when(blobClient.downloadContent()).thenReturn(
            BinaryData.fromString(isJson ? payload : new String(file.getBytes())));

        mockMvc.perform(MockMvcRequestBuilders
                            .get(PUBLICATION_URL + "/" + artefact.getArtefactId()))
            .andExpect(status().isNotFound()).andReturn();
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    @DisplayName("Metadata endpoint should not return the artefact when user not supplied")
    void retrieveMetadataOfAnArtefactWhereUserNotSupplied(boolean isJson) throws Exception {
        MockHttpServletRequestBuilder mockHttpServletRequestBuilder;
        if (isJson) {
            mockHttpServletRequestBuilder = MockMvcRequestBuilders.post(PUBLICATION_URL).content(payload);
        } else {
            mockHttpServletRequestBuilder = MockMvcRequestBuilders.multipart(PUBLICATION_URL).file(file);
        }
        mockHttpServletRequestBuilder.header(PublicationConfiguration.TYPE_HEADER, ARTEFACT_TYPE)
            .header(PublicationConfiguration.SENSITIVITY_HEADER, Sensitivity.CLASSIFIED)
            .header(PublicationConfiguration.LANGUAGE_HEADER, LANGUAGE)
            .header(PublicationConfiguration.PROVENANCE_HEADER, PROVENANCE)
            .header(PublicationConfiguration.SOURCE_ARTEFACT_ID_HEADER, SOURCE_ARTEFACT_ID)
            .header(PublicationConfiguration.DISPLAY_TO_HEADER, DISPLAY_TO.plusMonths(1))
            .header(PublicationConfiguration.DISPLAY_FROM_HEADER, DISPLAY_FROM)
            .header(PublicationConfiguration.LIST_TYPE, LIST_TYPE)
            .header(PublicationConfiguration.COURT_ID, COURT_ID)
            .header(PublicationConfiguration.CONTENT_DATE, CONTENT_DATE)
            .header(PublicationConfiguration.LANGUAGE_HEADER, LANGUAGE)
            .contentType(isJson ? MediaType.APPLICATION_JSON : MediaType.MULTIPART_FORM_DATA);

        MvcResult response =
            mockMvc.perform(mockHttpServletRequestBuilder).andExpect(status().isCreated()).andReturn();

        assertNotNull(response.getResponse().getContentAsString(), VALIDATION_EMPTY_RESPONSE);

        Artefact artefact = OBJECT_MAPPER.readValue(
            response.getResponse().getContentAsString(), Artefact.class);

        mockMvc.perform(MockMvcRequestBuilders
                            .get(PUBLICATION_URL + "/" + artefact.getArtefactId()))
            .andExpect(status().isNotFound()).andReturn();
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    @DisplayName("Metadata endpoint should return the artefact when user is authorised")
    void retrieveMetadataOfAnArtefactWhereUserAuthorised(boolean isJson) throws Exception {
        when(accountManagementService.getIsAuthorised(UUID.fromString(USER_ID), LIST_TYPE, Sensitivity.CLASSIFIED))
            .thenReturn(true);
        when(blobClient.downloadContent())
            .thenReturn(BinaryData.fromString(isJson ? payload : new String(file.getBytes())));

        MockHttpServletRequestBuilder mockHttpServletRequestBuilder;
        if (isJson) {
            mockHttpServletRequestBuilder = MockMvcRequestBuilders.post(PUBLICATION_URL).content(payload);
        } else {
            mockHttpServletRequestBuilder = MockMvcRequestBuilders.multipart(PUBLICATION_URL).file(file);
        }
        mockHttpServletRequestBuilder.header(PublicationConfiguration.TYPE_HEADER, ARTEFACT_TYPE)
            .header(PublicationConfiguration.SENSITIVITY_HEADER, Sensitivity.CLASSIFIED)
            .header(PublicationConfiguration.LANGUAGE_HEADER, LANGUAGE)
            .header(PublicationConfiguration.PROVENANCE_HEADER, PROVENANCE)
            .header(PublicationConfiguration.SOURCE_ARTEFACT_ID_HEADER, SOURCE_ARTEFACT_ID)
            .header(PublicationConfiguration.DISPLAY_TO_HEADER, DISPLAY_TO.plusMonths(1))
            .header(PublicationConfiguration.DISPLAY_FROM_HEADER, DISPLAY_FROM)
            .header(PublicationConfiguration.LIST_TYPE, LIST_TYPE)
            .header(PublicationConfiguration.COURT_ID, COURT_ID)
            .header(PublicationConfiguration.CONTENT_DATE, CONTENT_DATE)
            .header(PublicationConfiguration.LANGUAGE_HEADER, LANGUAGE)
            .contentType(isJson ? MediaType.APPLICATION_JSON : MediaType.MULTIPART_FORM_DATA);

        MvcResult response =
            mockMvc.perform(mockHttpServletRequestBuilder).andExpect(status().isCreated()).andReturn();

        assertNotNull(response.getResponse().getContentAsString(), VALIDATION_EMPTY_RESPONSE);

        Artefact artefact = OBJECT_MAPPER.readValue(
            response.getResponse().getContentAsString(), Artefact.class);

        mockMvc.perform(MockMvcRequestBuilders
                            .get(PUBLICATION_URL + "/" + artefact.getArtefactId())
                            .header(USER_ID_HEADER, USER_ID))
            .andExpect(status().isOk()).andReturn();

        assertNotNull(response.getResponse().getContentAsString(), VALIDATION_EMPTY_RESPONSE);
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    @DisplayName("Metadata endpoint should return the artefact when not authorised")
    void retrieveMetadataOfAnArtefactWhereUserNotAuthorized(boolean isJson) throws Exception {
        when(accountManagementService.getIsAuthorised(UUID.fromString(USER_ID), LIST_TYPE, Sensitivity.CLASSIFIED))
            .thenReturn(false);

        MockHttpServletRequestBuilder mockHttpServletRequestBuilder;
        if (isJson) {
            mockHttpServletRequestBuilder = MockMvcRequestBuilders.post(PUBLICATION_URL).content(payload);
        } else {
            mockHttpServletRequestBuilder = MockMvcRequestBuilders.multipart(PUBLICATION_URL).file(file);
        }
        mockHttpServletRequestBuilder.header(PublicationConfiguration.TYPE_HEADER, ARTEFACT_TYPE)
            .header(PublicationConfiguration.SENSITIVITY_HEADER, Sensitivity.CLASSIFIED)
            .header(PublicationConfiguration.LANGUAGE_HEADER, LANGUAGE)
            .header(PublicationConfiguration.PROVENANCE_HEADER, PROVENANCE)
            .header(PublicationConfiguration.SOURCE_ARTEFACT_ID_HEADER, SOURCE_ARTEFACT_ID)
            .header(PublicationConfiguration.DISPLAY_TO_HEADER, DISPLAY_TO.plusMonths(1))
            .header(PublicationConfiguration.DISPLAY_FROM_HEADER, DISPLAY_FROM)
            .header(PublicationConfiguration.LIST_TYPE, LIST_TYPE)
            .header(PublicationConfiguration.COURT_ID, COURT_ID)
            .header(PublicationConfiguration.CONTENT_DATE, CONTENT_DATE)
            .header(PublicationConfiguration.LANGUAGE_HEADER, LANGUAGE)
            .contentType(isJson ? MediaType.APPLICATION_JSON : MediaType.MULTIPART_FORM_DATA);

        MvcResult response =
            mockMvc.perform(mockHttpServletRequestBuilder).andExpect(status().isCreated()).andReturn();

        assertNotNull(response.getResponse().getContentAsString(), VALIDATION_EMPTY_RESPONSE);

        Artefact artefact = OBJECT_MAPPER.readValue(
            response.getResponse().getContentAsString(), Artefact.class);

        mockMvc.perform(MockMvcRequestBuilders
                            .get(PUBLICATION_URL + "/" + artefact.getArtefactId())
                            .header(USER_ID_HEADER, USER_ID))
            .andExpect(status().isNotFound()).andReturn();
    }

    @Test
    @DisplayName("Metadata endpoint should return 404 when artefact does not exist")
    void retrieveMetadataOfAnArtefactWhereNotFound() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders
                            .get("/publication/7d734e8d-ba1d-4730-bd8b-09a970be00cc")
                            .header(USER_ID_HEADER, USER_ID))
            .andExpect(status().isNotFound()).andReturn();
    }

    @Test
    void testDeleteArtefactByIdSuccess() throws Exception {
        Artefact artefactToDelete = createDailyList(Sensitivity.PUBLIC);

        MockHttpServletRequestBuilder preDeleteRequest = MockMvcRequestBuilders
            .get(PUBLICATION_URL + "/" + artefactToDelete.getArtefactId())
            .header(USER_ID_HEADER, USER_ID);

        mockMvc.perform(preDeleteRequest).andExpect(status().isOk());

        MockHttpServletRequestBuilder deleteRequest = MockMvcRequestBuilders
            .delete(PUBLICATION_URL + "/" + artefactToDelete.getArtefactId())
            .header(ISSUER_HEADER, EMAIL);

        MvcResult deleteResponse = mockMvc.perform(deleteRequest).andExpect(status().isOk()).andReturn();

        assertEquals("Successfully deleted artefact: " + artefactToDelete.getArtefactId(),
                     deleteResponse.getResponse().getContentAsString(), "Should successfully delete artefact"
        );
    }

    @Test
    void testDeleteArtefactByIdArtefactIdNotFound() throws Exception {
        String invalidId = UUID.randomUUID().toString();

        MockHttpServletRequestBuilder deleteRequest = MockMvcRequestBuilders
            .delete(PUBLICATION_URL + "/" + invalidId)
            .header(ISSUER_HEADER, EMAIL);

        MvcResult deleteResponse = mockMvc.perform(deleteRequest).andExpect(status().isNotFound()).andReturn();

        assertTrue(
            deleteResponse.getResponse().getContentAsString()
                .contains("No artefact found with the ID: " + invalidId),
            "Should return 404 for artefact not found"
        );
    }

    @Test
    void testGetArtefactMetadataAdmin() throws Exception {
        Artefact artefactToFind = createDailyList(Sensitivity.PUBLIC, DISPLAY_FROM.plusMonths(1),
                                                  CONTENT_DATE.plusDays(1)
        );

        MockHttpServletRequestBuilder expectedFailRequest = MockMvcRequestBuilders
            .get(PUBLICATION_URL + "/" + artefactToFind.getArtefactId())
            .header(USER_ID_HEADER, USER_ID);
        mockMvc.perform(expectedFailRequest).andExpect(status().isNotFound());

        MockHttpServletRequestBuilder adminRequest = MockMvcRequestBuilders
            .get(PUBLICATION_URL + "/" + artefactToFind.getArtefactId())
            .header(USER_ID_HEADER, USER_ID)
            .header(ADMIN_HEADER, true);
        MvcResult response = mockMvc.perform(adminRequest).andExpect(status().isOk()).andReturn();

        String responseAsString = response.getResponse().getContentAsString();
        Artefact artefact = OBJECT_MAPPER.readValue(responseAsString, Artefact.class);

        assertTrue(compareArtefacts(artefactToFind, artefact), SHOULD_RETURN_EXPECTED_ARTEFACT);

        JsonNode responseAsJson = OBJECT_MAPPER.readTree(responseAsString);
        List.of("contentDate", "displayFrom", "displayTo")
            .forEach(field -> assertDateTimeFormat(responseAsJson.get(field).asText(), field));
    }

    @Test
    void testGetArtefactMetadataReturnsNotFound() throws Exception {
        MockHttpServletRequestBuilder adminRequest = MockMvcRequestBuilders
            .get(PUBLICATION_URL + "/" + UUID.randomUUID())
            .header(USER_ID_HEADER, USER_ID)
            .header(ADMIN_HEADER, true);
        mockMvc.perform(adminRequest).andExpect(status().isNotFound());
    }

    @Test
    void testArchiveExpiredArtefactsSuccess() throws Exception {
        Artefact artefactToExpire = createDailyList(Sensitivity.PUBLIC, DISPLAY_FROM.minusMonths(9),
                                                    DISPLAY_FROM.minusMonths(6),
                                                    DISPLAY_FROM.minusMonths(10), PROVENANCE, COURT_ID);

        MockHttpServletRequestBuilder adminGetRequest = MockMvcRequestBuilders
            .get(PUBLICATION_URL + "/" + artefactToExpire.getArtefactId())
            .header(USER_ID_HEADER, USER_ID)
            .header(ADMIN_HEADER, true);
        MvcResult response = mockMvc.perform(adminGetRequest).andExpect(status().isOk()).andReturn();

        Artefact artefactNotExpired = OBJECT_MAPPER.readValue(
            response.getResponse().getContentAsString(),
            Artefact.class
        );
        assertTrue(!artefactNotExpired.getIsArchived(), SHOULD_RETURN_EXPECTED_ARTEFACT);

        MockHttpServletRequestBuilder deleteRequest = MockMvcRequestBuilders
            .delete(ARCHIVE_EXPIRED_ARTEFACTS_URL);
        mockMvc.perform(deleteRequest).andExpect(status().isNoContent());

        MockHttpServletRequestBuilder adminGetRequestAfterDelete = MockMvcRequestBuilders
            .get(PUBLICATION_URL + "/" + artefactToExpire.getArtefactId())
            .header(USER_ID_HEADER, USER_ID)
            .header(ADMIN_HEADER, true);

        MvcResult archiveResponse = mockMvc.perform(adminGetRequestAfterDelete).andExpect(status().isOk()).andReturn();

        Artefact artefactExired = OBJECT_MAPPER.readValue(
            archiveResponse.getResponse().getContentAsString(),
            Artefact.class
        );
        assertTrue(artefactExired.getIsArchived(), SHOULD_RETURN_EXPECTED_ARTEFACT);

    }

    @Test
    void testArchiveArtefactSuccess() throws Exception {
        Artefact artefactToArchive = createDailyList(Sensitivity.PUBLIC);

        MockHttpServletRequestBuilder preArchiveRequest = MockMvcRequestBuilders
            .get(PUBLICATION_URL + "/" + artefactToArchive.getArtefactId())
            .header(USER_ID_HEADER, USER_ID);

        mockMvc.perform(preArchiveRequest).andExpect(status().isOk());

        MockHttpServletRequestBuilder archiveRequest = MockMvcRequestBuilders
            .put(PUBLICATION_URL + "/" + artefactToArchive.getArtefactId() + "/archive")
            .header(ISSUER_HEADER, USER_ID);

        MvcResult archiveResponse = mockMvc.perform(archiveRequest).andExpect(status().isOk()).andReturn();

        assertEquals("Artefact of ID " + artefactToArchive.getArtefactId() + " has been archived",
                     archiveResponse.getResponse().getContentAsString(), "Should successfully archive artefact"
        );
    }

    @Test
    void testArchiveArtefactNotFound() throws Exception {
        String invalidArtefactId = UUID.randomUUID().toString();

        MockHttpServletRequestBuilder archiveRequest = MockMvcRequestBuilders
            .put(PUBLICATION_URL + "/" + invalidArtefactId + "/archive")
            .header(ISSUER_HEADER, USER_ID);

        MvcResult archiveResponse = mockMvc.perform(archiveRequest).andExpect(status().isNotFound()).andReturn();

        assertTrue(
            archiveResponse.getResponse().getContentAsString()
                .contains("No artefact found with the ID: " + invalidArtefactId),
            "Should return 404 for artefact not found"
        );
    }

    private void assertDateTimeFormat(String value, String field) {
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'");
            LocalDateTime dateTime = LocalDateTime.parse(value, formatter);
            String result = dateTime.format(formatter);
            assertEquals(value, result, String.format("%s should match", field));
        } catch (DateTimeParseException e) {
            fail(String.format("%s with value '%s' could not be parsed", field, value));
        }
    }

    @Test
    void testGenerateNoSearchWhenFileTooBig() throws Exception {
        try (InputStream mockFile = this.getClass().getClassLoader()
            .getResourceAsStream("data/civil-daily-cause-list/civilDailyCauseList.json")) {

            JsonElement jsonParser = JsonParser.parseReader(new InputStreamReader(mockFile));
            JsonArray jsonArray = jsonParser.getAsJsonObject().get("courtLists").getAsJsonArray();
            JsonElement jsonElement = jsonArray.get(0);
            for (int i = 0; i <= 200; i++) {
                jsonArray.add(jsonElement);
            }

            MockHttpServletRequestBuilder mockHttpServletRequestBuilder = MockMvcRequestBuilders
                .post(PUBLICATION_URL)
                .header(PublicationConfiguration.TYPE_HEADER, ARTEFACT_TYPE)
                .header(PublicationConfiguration.PROVENANCE_HEADER, PROVENANCE)
                .header(PublicationConfiguration.SOURCE_ARTEFACT_ID_HEADER, SOURCE_ARTEFACT_ID)
                .header(PublicationConfiguration.DISPLAY_FROM_HEADER, DISPLAY_FROM)
                .header(PublicationConfiguration.DISPLAY_TO_HEADER, DISPLAY_TO.plusMonths(1))
                .header(PublicationConfiguration.COURT_ID, COURT_ID)
                .header(PublicationConfiguration.LIST_TYPE, ListType.CIVIL_DAILY_CAUSE_LIST)
                .header(PublicationConfiguration.CONTENT_DATE, CONTENT_DATE)
                .header(PublicationConfiguration.SENSITIVITY_HEADER, SENSITIVITY)
                .header(PublicationConfiguration.LANGUAGE_HEADER, LANGUAGE)
                .content(jsonParser.toString())
                .contentType(MediaType.APPLICATION_JSON);

            MvcResult response = mockMvc.perform(mockHttpServletRequestBuilder)
                .andExpect(status().isCreated()).andReturn();

            Artefact artefact = OBJECT_MAPPER.readValue(
                response.getResponse().getContentAsString(), Artefact.class);

            assertTrue(artefact.getSearch().isEmpty(), "Search has been generated when file size is too big");
        }
    }
}
