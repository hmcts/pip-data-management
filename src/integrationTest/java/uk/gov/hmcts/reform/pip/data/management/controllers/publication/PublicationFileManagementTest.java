package uk.gov.hmcts.reform.pip.data.management.controllers.publication;

import com.azure.core.util.BinaryData;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import io.zonky.test.db.AutoConfigureEmbeddedDatabase;
import org.apache.commons.math3.random.RandomDataGenerator;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import uk.gov.hmcts.reform.pip.data.management.Application;
import uk.gov.hmcts.reform.pip.data.management.config.PublicationConfiguration;
import uk.gov.hmcts.reform.pip.data.management.errorhandling.ExceptionResponse;
import uk.gov.hmcts.reform.pip.data.management.models.publication.Artefact;
import uk.gov.hmcts.reform.pip.data.management.utils.PublicationIntegrationTestBase;
import uk.gov.hmcts.reform.pip.model.publication.ArtefactType;
import uk.gov.hmcts.reform.pip.model.publication.Language;
import uk.gov.hmcts.reform.pip.model.publication.ListType;
import uk.gov.hmcts.reform.pip.model.publication.Sensitivity;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.UUID;

import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.pip.model.publication.FileType.PDF;

@SpringBootTest(classes = {Application.class}, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles({"integration", "disable-async"})
@AutoConfigureMockMvc
@AutoConfigureEmbeddedDatabase(type = AutoConfigureEmbeddedDatabase.DatabaseType.POSTGRES)
@WithMockUser(username = "admin", authorities = {"APPROLE_api.request.admin"})
@SuppressWarnings({"PMD.ExcessiveImports"})
class PublicationFileManagementTest extends PublicationIntegrationTestBase {
    private static final String ROOT_URL = "/publication";
    private static final String GET_FILE_URL = ROOT_URL + "/%s/%s";
    private static final String ARTEFACT_ID = UUID.randomUUID().toString();
    private static final String ARTEFACT_ID_NOT_FOUND = UUID.randomUUID().toString();
    private static final String ARTEFACT_NOT_FOUND_MESSAGE = "No artefact found with the ID: ";
    private static final String NOT_FOUND_RESPONSE_MESSAGE = "Artefact not found message does not match";
    private static final String NULL_RESPONSE_ERROR = "Response should not be null";
    private static final String UNEXPECTED_RESPONSE_ERROR = "Response does not contain expected result";
    private static final String FILE_TYPE_HEADER = "x-file-type";
    private static final String USER_ID_HEADER = "x-user-id";
    private static final String MAX_FILE_SIZE_HEADER = "maxFileSize";
    private static final String MAX_FILE_SIZE =  "2048000";
    private static final String UNAUTHORIZED_USERNAME = "unauthorized_username";
    private static final String UNAUTHORIZED_ROLE = "APPROLE_unknown.role";
    private static final String SYSTEM_HEADER = "x-system";
    private static final String FALSE = "false";
    private static final String TEST_CONTENT = "test content";

    private static final LocalDateTime DISPLAY_TO = LocalDateTime.now()
        .truncatedTo(ChronoUnit.SECONDS);
    private static final LocalDateTime DISPLAY_FROM = LocalDateTime.now().plusDays(1)
        .truncatedTo(ChronoUnit.SECONDS);
    private static final LocalDateTime CONTENT_DATE = LocalDateTime.now().toLocalDate().atStartOfDay()
        .truncatedTo(ChronoUnit.SECONDS);
    private static final String PROVENANCE = "MANUAL_UPLOAD";
    private static final String USER_ID = UUID.randomUUID().toString();

    private static final String SJP_MOCK = "data/sjp-public-list/sjpPublicList.json";
    private static final String SJP_PRESS_MOCK = "data/sjp-press-list/sjpPressList.json";

    private static MockMultipartFile file;

    @BeforeAll
    void setup() {
        file = new MockMultipartFile("file", "test.pdf",
                                     MediaType.APPLICATION_PDF_VALUE, TEST_CONTENT.getBytes(
            StandardCharsets.UTF_8)
        );
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
        return createPublication(listType, Sensitivity.PUBLIC, data);
    }

    private Artefact createPublication(ListType listType, Sensitivity sensitivity, byte[] data) throws Exception {
        MockHttpServletRequestBuilder mockHttpServletRequestBuilder = MockMvcRequestBuilders
            .post(ROOT_URL)
            .header(PublicationConfiguration.TYPE_HEADER, ArtefactType.LIST)
            .header(PublicationConfiguration.PROVENANCE_HEADER, PROVENANCE)
            .header(PublicationConfiguration.DISPLAY_FROM_HEADER, DISPLAY_FROM)
            .header(PublicationConfiguration.DISPLAY_TO_HEADER, DISPLAY_TO)
            .header(PublicationConfiguration.COURT_ID, "1")
            .header(PublicationConfiguration.LIST_TYPE, listType)
            .header(PublicationConfiguration.CONTENT_DATE,
                    CONTENT_DATE.plusDays(new RandomDataGenerator().nextLong(1, 100_000)))
            .header(PublicationConfiguration.SENSITIVITY_HEADER, sensitivity)
            .header(PublicationConfiguration.LANGUAGE_HEADER, Language.ENGLISH)
            .content(data)
            .contentType(MediaType.APPLICATION_JSON);

        MvcResult response = mockMvc.perform(mockHttpServletRequestBuilder)
            .andExpect(status().isCreated()).andReturn();

        return OBJECT_MAPPER.readValue(
            response.getResponse().getContentAsString(), Artefact.class);
    }

    private void createLocations() throws Exception {
        try (InputStream csvInputStream = this.getClass().getClassLoader()
            .getResourceAsStream("location/ValidCsv.csv")) {
            MockMultipartFile csvFile
                = new MockMultipartFile("locationList", csvInputStream);

            mockMvc.perform(multipart("/locations/upload").file(csvFile))
                .andExpect(status().isOk()).andReturn();

        }
    }

    private Artefact createSjpPublicListPublication() throws Exception {
        byte[] testPublication = getTestData(SJP_MOCK);
        return createPublication(ListType.SJP_PUBLIC_LIST, testPublication);
    }

    @Test
    void testGetFileExists() throws Exception {
        when(blobClient.downloadContent()).thenReturn(
            BinaryData.fromString(new String(file.getBytes())));

        MvcResult response = mockMvc.perform(
                get(ROOT_URL + "/" + ARTEFACT_ID + "/exists"))
            .andExpect(status().isOk()).andReturn();

        assertNotNull(
            response.getResponse().getContentAsString(),
            NULL_RESPONSE_ERROR
        );
    }

    @Test
    void testGetFileSizes() throws Exception {
        when(blobClient.downloadContent()).thenReturn(
            BinaryData.fromString(new String(file.getBytes())));

        MvcResult response = mockMvc.perform(
                get(ROOT_URL + "/" + ARTEFACT_ID + "/sizes"))
            .andExpect(status().isOk()).andReturn();

        assertNotNull(
            response.getResponse().getContentAsString(),
            NULL_RESPONSE_ERROR
        );
    }

    @Test
    void testGetFileOK() throws Exception {
        UUID artefactId = createSjpPublicListPublication().getArtefactId();

        when(blobClient.downloadContent()).thenReturn(
            BinaryData.fromString(new String(file.getBytes())));

        MvcResult response = mockMvc.perform(
                get(String.format(GET_FILE_URL, artefactId, PDF))
                    .header(SYSTEM_HEADER, "true")
                    .header(FILE_TYPE_HEADER, PDF)
                    .param(MAX_FILE_SIZE_HEADER, MAX_FILE_SIZE))

            .andExpect(status().isOk()).andReturn();

        assertNotNull(
            response.getResponse().getContentAsString(),
            NULL_RESPONSE_ERROR
        );
        byte[] decodedBytes = Base64.getDecoder().decode(response.getResponse().getContentAsString());
        String decodedResponse = new String(decodedBytes);

        assertTrue(
            decodedResponse.contains(TEST_CONTENT),
            UNEXPECTED_RESPONSE_ERROR
        );
    }

    @Test
    void testGetFileWithAuthorisedUser() throws Exception {

        when(accountManagementService.getIsAuthorised(
            UUID.fromString(USER_ID), ListType.SJP_PRESS_LIST, Sensitivity.CLASSIFIED
        )).thenReturn(true);
        when(blobClient.downloadContent()).thenReturn(BinaryData.fromString(new String(file.getBytes())));

        byte[] data = getTestData(SJP_PRESS_MOCK);
        Artefact artefact = createPublication(ListType.SJP_PRESS_LIST, Sensitivity.CLASSIFIED, data);
        MockHttpServletRequestBuilder request =
            get(String.format(GET_FILE_URL, artefact.getArtefactId(), PDF))
                .header(USER_ID_HEADER, USER_ID)
                .header(SYSTEM_HEADER, FALSE)
                .param(MAX_FILE_SIZE_HEADER, MAX_FILE_SIZE);

        MvcResult response = mockMvc.perform(request)
            .andExpect(status().isOk()).andReturn();

        assertNotNull(
            response.getResponse().getContentAsString(),
            "Null response"
        );
        byte[] decodedBytes = Base64.getDecoder().decode(response.getResponse().getContentAsString());
        String decodedResponse = new String(decodedBytes);

        assertTrue(
            decodedResponse.contains(TEST_CONTENT),
            UNEXPECTED_RESPONSE_ERROR
        );
    }

    @Test
    void testGetFileWithUnauthorisedUser() throws Exception {
        when(accountManagementService.getIsAuthorised(
            UUID.fromString(USER_ID), ListType.SJP_PRESS_LIST, Sensitivity.CLASSIFIED
        )).thenReturn(false);

        byte[] data = getTestData(SJP_PRESS_MOCK);
        Artefact artefact = createPublication(ListType.SJP_PRESS_LIST, Sensitivity.CLASSIFIED, data);
        MockHttpServletRequestBuilder request =
            get(String.format(GET_FILE_URL, artefact.getArtefactId(), PDF))
                .header(USER_ID_HEADER, USER_ID)
                .header(SYSTEM_HEADER, FALSE)
                .param(MAX_FILE_SIZE_HEADER, MAX_FILE_SIZE);

        MvcResult response = mockMvc.perform(request)
            .andExpect(status().isUnauthorized()).andReturn();

        assertTrue(
            response.getResponse().getContentAsString().contains("not authorised to access artefact with id "
                                                                     + artefact.getArtefactId()),
            "Response does not match"
        );
    }

    @Test
    void testGetFileSizeTooLarge() throws Exception {
        UUID artefactId = createSjpPublicListPublication().getArtefactId();

        when(blobClient.downloadContent()).thenReturn(
            BinaryData.fromString(new String(file.getBytes())));

        MockHttpServletRequestBuilder request =
            get(String.format(GET_FILE_URL, artefactId, PDF))
                .header(USER_ID_HEADER, USER_ID)
                .header(SYSTEM_HEADER, FALSE)
                .param(MAX_FILE_SIZE_HEADER, "10");

        MvcResult response = mockMvc.perform(request)
            .andExpect(status().isPayloadTooLarge()).andReturn();

        assertNotNull(
            response.getResponse().getContentAsString(),
            "Null response"
        );

        assertTrue(
            response.getResponse().getContentAsString().contains("File with type PDF for artefact with id "
                                                         + artefactId + " has size over the limit of 10 bytes"),
            UNEXPECTED_RESPONSE_ERROR
        );
    }

    @Test
    void testGetFileNotFound() throws Exception {
        MvcResult mvcResult = mockMvc.perform(get(String.format(GET_FILE_URL, ARTEFACT_ID_NOT_FOUND, PDF)))
            .andExpect(status().isNotFound())
            .andReturn();

        ExceptionResponse exceptionResponse = OBJECT_MAPPER.readValue(
            mvcResult.getResponse().getContentAsString(), ExceptionResponse.class);

        assertEquals(
            ARTEFACT_NOT_FOUND_MESSAGE + ARTEFACT_ID_NOT_FOUND,
            exceptionResponse.getMessage(),
            NOT_FOUND_RESPONSE_MESSAGE
        );
    }

    @Test
    @WithMockUser(username = UNAUTHORIZED_USERNAME, authorities = {UNAUTHORIZED_ROLE})
    void testGetFileUnauthorized() throws Exception {
        mockMvc.perform(get(String.format(GET_FILE_URL, ARTEFACT_ID, PDF)))
            .andExpect(status().isForbidden());
    }

    @Test
    void testGenerateNoExcelWhenFileTooBig() throws Exception {
        createLocations();

        try (InputStream mockFile = this.getClass().getClassLoader().getResourceAsStream(SJP_MOCK)) {

            JsonElement jsonParser = JsonParser.parseReader(new InputStreamReader(mockFile));
            JsonArray jsonArray = jsonParser.getAsJsonObject().get("courtLists").getAsJsonArray();
            JsonElement jsonElement = jsonArray.get(0);
            for (int i = 0; i <= 400; i++) {
                jsonArray.add(jsonElement);
            }

            Artefact artefact =
                createPublication(ListType.SJP_PUBLIC_LIST, jsonParser.toString().getBytes(StandardCharsets.UTF_8));

            verify(publicationBlobContainerClient, never()).getBlobClient(artefact.getArtefactId() + ".pdf");
            verify(publicationBlobContainerClient, never()).getBlobClient(artefact.getArtefactId() + ".xlsx");
        }
    }

    @Test
    void testGenerateNoPdfWhenFileTooBig() throws Exception {
        createLocations();

        try (InputStream mockFile = this.getClass().getClassLoader().getResourceAsStream(SJP_MOCK)) {

            JsonElement jsonParser = JsonParser.parseReader(new InputStreamReader(mockFile));
            JsonArray jsonArray = jsonParser.getAsJsonObject().get("courtLists").getAsJsonArray();
            JsonElement jsonElement = jsonArray.get(0);
            for (int i = 0; i <= 200; i++) {
                jsonArray.add(jsonElement);
            }

            Artefact artefact =
                createPublication(ListType.SJP_PUBLIC_LIST, jsonParser.toString().getBytes(StandardCharsets.UTF_8));

            verify(publicationBlobContainerClient, never()).getBlobClient(artefact.getArtefactId() + ".pdf");
            verify(publicationBlobContainerClient, times(1))
                .getBlobClient(artefact.getArtefactId() + ".xlsx");
        }
    }
}
