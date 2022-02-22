package uk.gov.hmcts.reform.pip.data.management.controllers;

import com.azure.core.util.BinaryData;
import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.applicationinsights.web.dependencies.apachecommons.io.IOUtils;
import io.zonky.test.db.AutoConfigureEmbeddedDatabase;
import org.json.JSONArray;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import uk.gov.hmcts.reform.pip.data.management.Application;
import uk.gov.hmcts.reform.pip.data.management.config.PublicationConfiguration;
import uk.gov.hmcts.reform.pip.data.management.errorhandling.ExceptionResponse;
import uk.gov.hmcts.reform.pip.data.management.models.publication.Artefact;
import uk.gov.hmcts.reform.pip.data.management.models.publication.ArtefactType;
import uk.gov.hmcts.reform.pip.data.management.models.publication.Language;
import uk.gov.hmcts.reform.pip.data.management.models.publication.ListType;
import uk.gov.hmcts.reform.pip.data.management.models.publication.Sensitivity;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = {Application.class},
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@ActiveProfiles(profiles = "test")
@RunWith(SpringRunner.class)
@SuppressWarnings({"PMD.ExcessiveImports", "PMD.ExcessiveClassLength",
    "PMD.CyclomaticComplexity", "PMD.TooManyMethods"})
@AutoConfigureEmbeddedDatabase(type = AutoConfigureEmbeddedDatabase.DatabaseType.POSTGRES)
class PublicationTest {

    @Autowired
    BlobContainerClient blobContainerClient;

    @Autowired
    BlobClient blobClient;

    @Autowired
    private MockMvc mockMvc;

    private static final String POST_URL = "/publication";
    private static final String SEARCH_URL = "/publication/search";
    private static final String SEARCH_COURT_URL = "/publication/courtId";
    private static final String PAYLOAD_URL = "/payload";
    private static final ArtefactType ARTEFACT_TYPE = ArtefactType.LIST;
    private static final ArtefactType ARTEFACT_TYPE_GENERAL = ArtefactType.GENERAL_PUBLICATION;
    private static final Sensitivity SENSITIVITY = Sensitivity.PUBLIC;
    private static final String PROVENANCE = "provenance";
    private static String payload = "payload";
    private static MockMultipartFile file;
    private static final String PAYLOAD_UNKNOWN = "Unknown-Payload";
    private static final String SOURCE_ARTEFACT_ID = "sourceArtefactId";
    private static final LocalDateTime DISPLAY_TO = LocalDateTime.now();
    private static final LocalDateTime DISPLAY_FROM = LocalDateTime.now();
    private static final Language LANGUAGE = Language.ENGLISH;
    private static final String TEST_VALUE = "test";
    private static final String BLOB_PAYLOAD_URL = "https://localhost";
    private static final ListType LIST_TYPE = ListType.CIVIL_DAILY_CAUSE_LIST;
    private static final String COURT_ID = "123";
    private static final LocalDateTime CONTENT_DATE = LocalDateTime.now();
    private static final String SEARCH_KEY_FOUND = "array-value";
    private static final String SEARCH_KEY_NOT_FOUND = "case-urn";
    private static final String SEARCH_VALUE_1 = "array-value-1";
    private static final String SEARCH_VALUE_2 = "array-value-2";
    private static final String COURT_ID_SEARCH_KEY = "court-id";
    private static final String EMPTY_VALUE = "";
    private static final String VERIFICATION_HEADER = "verification";
    private static final String VALID_CASE_ID_SEARCH = "/CASE_ID/45684548";
    private static final String VALID_CASE_NAME_SEARCH = "/CASE_NAME/Smith";
    private static final String TRUE = "true";
    private static final String FALSE = "false";

    private static final String FORMAT_RESPONSE = "Please check that the value is of the correct format for the field "
        + "(See Swagger documentation for correct formats)";

    private static final String VERIFICATION_TRUE = "true";
    private static final String VERIFICATION_FALSE = "false";
    private static final String DISPLAY_FROM_RESPONSE = "x-display-from Field is required for artefact type";
    private static final String DISPLAY_TO_RESPONSE = "x-display-to Field is required for artefact type";
    private static final String VALIDATION_EMPTY_RESPONSE = "Response should contain a Artefact";
    private static final String VALIDATION_EXCEPTION_RESPONSE = "Exception response does not contain correct message";
    private static final String VALIDATION_DISPLAY_FROM = "The expected Display From has not been returned";
    private static final String SHOULD_RETURN_EXPECTED_ARTEFACT = "Should return expected artefact";

    private static MockHttpServletRequestBuilder mockHttpServletRequestBuilder;
    private static ObjectMapper objectMapper;

    @BeforeAll
    public static void setup() throws IOException {
        file = new MockMultipartFile("file", "test.pdf", MediaType.APPLICATION_PDF_VALUE, "test content".getBytes(
            StandardCharsets.UTF_8));
        payload = new String(IOUtils.toByteArray(
            Objects.requireNonNull(PublicationTest.class.getClassLoader()
                                       .getResourceAsStream("data/artefact.json"))));

        objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();
    }

    Artefact createDailyList(Sensitivity sensitivity) throws Exception {
        try (InputStream mockFile = this.getClass().getClassLoader()
            .getResourceAsStream("data/dailyCauseList.json")) {

            MockHttpServletRequestBuilder mockHttpServletRequestBuilder = MockMvcRequestBuilders
                .post(POST_URL)
                .header(PublicationConfiguration.TYPE_HEADER, ARTEFACT_TYPE)
                .header(PublicationConfiguration.PROVENANCE_HEADER, PROVENANCE)
                .header(PublicationConfiguration.SOURCE_ARTEFACT_ID_HEADER, SOURCE_ARTEFACT_ID)
                .header(PublicationConfiguration.DISPLAY_FROM_HEADER, DISPLAY_FROM)
                .header(PublicationConfiguration.DISPLAY_TO_HEADER, DISPLAY_TO.plusMonths(1))
                .header(PublicationConfiguration.COURT_ID, COURT_ID)
                .header(PublicationConfiguration.LIST_TYPE, ListType.CIVIL_DAILY_CAUSE_LIST)
                .header(PublicationConfiguration.CONTENT_DATE, CONTENT_DATE)
                .header(PublicationConfiguration.SENSITIVITY_HEADER, sensitivity)
                .content(mockFile.readAllBytes())
                .contentType(MediaType.APPLICATION_JSON);

            MvcResult response = mockMvc.perform(mockHttpServletRequestBuilder)
                .andExpect(status().isCreated()).andReturn();

            return objectMapper.readValue(
                response.getResponse().getContentAsString(), Artefact.class);
        }
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    @DisplayName("Should create a valid artefact and return the created artefact to the user")
    void creationOfAValidArtefact(boolean isJson) throws Exception {
        if (isJson) {
            mockHttpServletRequestBuilder = MockMvcRequestBuilders.post(POST_URL).content(payload);
        } else {
            mockHttpServletRequestBuilder = MockMvcRequestBuilders.multipart(POST_URL).file(file);
        }
        mockHttpServletRequestBuilder.header(PublicationConfiguration.TYPE_HEADER, ARTEFACT_TYPE);
        mockHttpServletRequestBuilder.header(PublicationConfiguration.SENSITIVITY_HEADER, SENSITIVITY);
        mockHttpServletRequestBuilder.header(PublicationConfiguration.LANGUAGE_HEADER, LANGUAGE);
        mockHttpServletRequestBuilder.header(PublicationConfiguration.PROVENANCE_HEADER, PROVENANCE);
        mockHttpServletRequestBuilder.header(PublicationConfiguration.SOURCE_ARTEFACT_ID_HEADER, SOURCE_ARTEFACT_ID);
        mockHttpServletRequestBuilder.header(PublicationConfiguration.DISPLAY_TO_HEADER, DISPLAY_TO);
        mockHttpServletRequestBuilder.header(PublicationConfiguration.DISPLAY_FROM_HEADER, DISPLAY_FROM);
        mockHttpServletRequestBuilder.header(PublicationConfiguration.LIST_TYPE, LIST_TYPE);
        mockHttpServletRequestBuilder.header(PublicationConfiguration.COURT_ID, COURT_ID);
        mockHttpServletRequestBuilder.header(PublicationConfiguration.CONTENT_DATE, CONTENT_DATE);
        mockHttpServletRequestBuilder.contentType(isJson ? MediaType.APPLICATION_JSON : MediaType.MULTIPART_FORM_DATA);

        when(blobContainerClient.getBlobClient(any())).thenReturn(blobClient);
        when(blobContainerClient.getBlobContainerUrl()).thenReturn(BLOB_PAYLOAD_URL);

        MvcResult response = mockMvc.perform(mockHttpServletRequestBuilder).andExpect(status().isCreated()).andReturn();

        assertNotNull(response.getResponse().getContentAsString(), VALIDATION_EMPTY_RESPONSE);

        Artefact artefact = objectMapper.readValue(
            response.getResponse().getContentAsString(), Artefact.class);

        assertNotNull(artefact.getArtefactId(), "Artefact ID is not populated");
        assertEquals(artefact.getSourceArtefactId(), SOURCE_ARTEFACT_ID, "Source artefact ID "
            + "does not match input source artefact id");
        assertEquals(artefact.getType(), ARTEFACT_TYPE, "Artefact type does not match input artefact type");
        assertEquals(artefact.getDisplayFrom(), DISPLAY_FROM, "Display from does not match input display from");
        assertEquals(artefact.getDisplayTo(), DISPLAY_TO, "Display to does not match input display to");
        assertEquals(artefact.getProvenance(), PROVENANCE, "Provenance does not match input provenance");
        assertEquals(artefact.getLanguage(), LANGUAGE, "Language does not match input language");
        assertEquals(artefact.getSensitivity(), SENSITIVITY, "Sensitivity does not match input sensitivity");


        Map<String, List<Object>> searchResult = artefact.getSearch();
        assertTrue(
            searchResult.containsKey(isJson ? SEARCH_KEY_FOUND : COURT_ID_SEARCH_KEY),
            "Returned search result does not contain the correct key"
        );
        assertFalse(searchResult.containsKey(SEARCH_KEY_NOT_FOUND), "Returned search result contains "
            + "key that does not exist");
        assertEquals(
            isJson ? SEARCH_VALUE_1 : COURT_ID,
            searchResult.get(isJson ? SEARCH_KEY_FOUND : COURT_ID_SEARCH_KEY).get(0),
            "Does not contain first value in the array"
        );

        if (isJson) {
            assertEquals(SEARCH_VALUE_2, searchResult.get(SEARCH_KEY_FOUND).get(1),
                         "Does not contain second value in the array"
            );
        }

        assertEquals(artefact.getPayload(), BLOB_PAYLOAD_URL + "/" + SOURCE_ARTEFACT_ID + '-' + PROVENANCE,
                     "Payload does not match input payload"
        );
    }

    @DisplayName("Should create a valid artefact with only mandatory fields")
    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void creationOfAValidArtefactWithOnlyMandatoryFields(boolean isJson) throws Exception {
        when(blobContainerClient.getBlobClient(any())).thenReturn(blobClient);
        when(blobContainerClient.getBlobContainerUrl()).thenReturn(BLOB_PAYLOAD_URL);
        if (isJson) {
            mockHttpServletRequestBuilder = MockMvcRequestBuilders.post(POST_URL).content(payload);
        } else {
            mockHttpServletRequestBuilder = MockMvcRequestBuilders.multipart(POST_URL).file(file);
        }
        mockHttpServletRequestBuilder.header(PublicationConfiguration.TYPE_HEADER, ARTEFACT_TYPE_GENERAL);
        mockHttpServletRequestBuilder.header(PublicationConfiguration.PROVENANCE_HEADER, PROVENANCE);
        mockHttpServletRequestBuilder.header(PublicationConfiguration.SOURCE_ARTEFACT_ID_HEADER, SOURCE_ARTEFACT_ID);
        mockHttpServletRequestBuilder.header(PublicationConfiguration.DISPLAY_FROM_HEADER, DISPLAY_FROM);
        mockHttpServletRequestBuilder.header(PublicationConfiguration.DISPLAY_TO_HEADER, DISPLAY_TO);
        mockHttpServletRequestBuilder.header(PublicationConfiguration.COURT_ID, COURT_ID);
        mockHttpServletRequestBuilder.contentType(isJson ? MediaType.APPLICATION_JSON : MediaType.MULTIPART_FORM_DATA);

        MvcResult response = mockMvc.perform(mockHttpServletRequestBuilder)
            .andExpect(status().isCreated()).andReturn();

        Artefact artefact = objectMapper.readValue(
            response.getResponse().getContentAsString(), Artefact.class);

        assertNotNull(artefact.getArtefactId(), "Artefact ID is not populated");
        assertEquals(artefact.getType(), ARTEFACT_TYPE_GENERAL, "Artefact type does not match input artefact type");
        assertEquals(artefact.getSourceArtefactId(), SOURCE_ARTEFACT_ID, "Source artefact ID "
            + "does not match input source artefact id");

        assertEquals(artefact.getProvenance(), PROVENANCE, "Provenance does not match input provenance");
    }

    @DisplayName("Should return a 400 Bad Request if the artifact type header is missing")
    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void testMissingArtifactType(boolean isJson) throws Exception {
        if (isJson) {
            mockHttpServletRequestBuilder = MockMvcRequestBuilders.post(POST_URL).content(payload);
        } else {
            mockHttpServletRequestBuilder = MockMvcRequestBuilders.multipart(POST_URL).file(file);
        }
        mockHttpServletRequestBuilder.header(PublicationConfiguration.SENSITIVITY_HEADER, SENSITIVITY);
        mockHttpServletRequestBuilder.header(PublicationConfiguration.PROVENANCE_HEADER, PROVENANCE);
        mockHttpServletRequestBuilder.header(PublicationConfiguration.SOURCE_ARTEFACT_ID_HEADER, SOURCE_ARTEFACT_ID);
        mockHttpServletRequestBuilder.header(PublicationConfiguration.DISPLAY_TO_HEADER, DISPLAY_TO);
        mockHttpServletRequestBuilder.header(PublicationConfiguration.DISPLAY_FROM_HEADER, DISPLAY_FROM);
        mockHttpServletRequestBuilder.header(PublicationConfiguration.LANGUAGE_HEADER, LANGUAGE);
        mockHttpServletRequestBuilder.contentType(isJson ? MediaType.APPLICATION_JSON : MediaType.MULTIPART_FORM_DATA);

        MvcResult response = mockMvc.perform(mockHttpServletRequestBuilder)
            .andExpect(status().isBadRequest()).andReturn();

        assertFalse(response.getResponse().getContentAsString().isEmpty(), VALIDATION_EMPTY_RESPONSE);
        ExceptionResponse exceptionResponse = objectMapper.readValue(
            response.getResponse().getContentAsString(),
            ExceptionResponse.class
        );

        assertTrue(exceptionResponse.getMessage().contains("x-type"), VALIDATION_EXCEPTION_RESPONSE);
    }

    @DisplayName("Should populate the datefrom field if it's empty and the type is Status_Update")
    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void testPopulateDefaultDateFrom(boolean isJson) throws Exception {
        when(blobContainerClient.getBlobClient(any())).thenReturn(blobClient);
        when(blobContainerClient.getBlobContainerUrl()).thenReturn(BLOB_PAYLOAD_URL);
        if (isJson) {
            mockHttpServletRequestBuilder = MockMvcRequestBuilders.post(POST_URL).content(payload);
        } else {
            mockHttpServletRequestBuilder = MockMvcRequestBuilders.multipart(POST_URL).file(file);
        }
        mockHttpServletRequestBuilder.header(PublicationConfiguration.TYPE_HEADER, ArtefactType.GENERAL_PUBLICATION);
        mockHttpServletRequestBuilder.header(PublicationConfiguration.SENSITIVITY_HEADER, SENSITIVITY);
        mockHttpServletRequestBuilder.header(PublicationConfiguration.PROVENANCE_HEADER, PROVENANCE);
        mockHttpServletRequestBuilder.header(PublicationConfiguration.SOURCE_ARTEFACT_ID_HEADER, SOURCE_ARTEFACT_ID);
        mockHttpServletRequestBuilder.header(PublicationConfiguration.LANGUAGE_HEADER, LANGUAGE);
        mockHttpServletRequestBuilder.header(PublicationConfiguration.COURT_ID, COURT_ID);
        mockHttpServletRequestBuilder.contentType(isJson ? MediaType.APPLICATION_JSON : MediaType.MULTIPART_FORM_DATA);
        MvcResult response = mockMvc.perform(mockHttpServletRequestBuilder)
            .andExpect(status().isCreated()).andReturn();
        Artefact createdArtefact = objectMapper.readValue(
            response.getResponse().getContentAsString(),
            Artefact.class
        );

        assertNotNull(createdArtefact.getDisplayFrom(), "Artefact date from criteria exists"
            + "when none is provided and status update is type");

    }

    @DisplayName("Should return a 400 bad request if artefact type is judgement and date to is empty")
    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void testDateToAbsenceJudgement(boolean isJson) throws Exception {
        if (isJson) {
            mockHttpServletRequestBuilder = MockMvcRequestBuilders.post(POST_URL).content(payload);
        } else {
            mockHttpServletRequestBuilder = MockMvcRequestBuilders.multipart(POST_URL).file(file);
        }
        mockHttpServletRequestBuilder.header(
            PublicationConfiguration.TYPE_HEADER,
            ArtefactType.JUDGEMENTS_AND_OUTCOMES
        );
        mockHttpServletRequestBuilder.header(PublicationConfiguration.SENSITIVITY_HEADER, SENSITIVITY);
        mockHttpServletRequestBuilder.header(PublicationConfiguration.PROVENANCE_HEADER, PROVENANCE);
        mockHttpServletRequestBuilder.header(PublicationConfiguration.SOURCE_ARTEFACT_ID_HEADER, SOURCE_ARTEFACT_ID);
        mockHttpServletRequestBuilder.header(PublicationConfiguration.LANGUAGE_HEADER, LANGUAGE);
        mockHttpServletRequestBuilder.header(PublicationConfiguration.DISPLAY_FROM_HEADER, DISPLAY_FROM);
        mockHttpServletRequestBuilder.header(PublicationConfiguration.LIST_TYPE, LIST_TYPE);
        mockHttpServletRequestBuilder.header(PublicationConfiguration.COURT_ID, COURT_ID);
        mockHttpServletRequestBuilder.header(PublicationConfiguration.CONTENT_DATE, CONTENT_DATE);
        mockHttpServletRequestBuilder.contentType(isJson ? MediaType.APPLICATION_JSON : MediaType.MULTIPART_FORM_DATA);
        MvcResult response = mockMvc.perform(mockHttpServletRequestBuilder)
            .andExpect(status().isBadRequest()).andReturn();
        assertFalse(response.getResponse().getContentAsString().isEmpty(), VALIDATION_EMPTY_RESPONSE);
        ExceptionResponse exceptionResponse = objectMapper.readValue(
            response.getResponse().getContentAsString(),
            ExceptionResponse.class
        );

        assertTrue(exceptionResponse.getMessage().contains(DISPLAY_TO_RESPONSE), VALIDATION_EXCEPTION_RESPONSE);
    }


    @DisplayName("Should return a 400 bad request if artefact type is list and date to is empty")
    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void testDateToAbsenceList(boolean isJson) throws Exception {
        if (isJson) {
            mockHttpServletRequestBuilder = MockMvcRequestBuilders.post(POST_URL).content(payload);
        } else {
            mockHttpServletRequestBuilder = MockMvcRequestBuilders.multipart(POST_URL).file(file);
        }
        mockHttpServletRequestBuilder.header(PublicationConfiguration.TYPE_HEADER, ARTEFACT_TYPE);
        mockHttpServletRequestBuilder.header(PublicationConfiguration.SENSITIVITY_HEADER, SENSITIVITY);
        mockHttpServletRequestBuilder.header(PublicationConfiguration.PROVENANCE_HEADER, PROVENANCE);
        mockHttpServletRequestBuilder.header(PublicationConfiguration.SOURCE_ARTEFACT_ID_HEADER, SOURCE_ARTEFACT_ID);
        mockHttpServletRequestBuilder.header(PublicationConfiguration.LANGUAGE_HEADER, LANGUAGE);
        mockHttpServletRequestBuilder.header(PublicationConfiguration.DISPLAY_FROM_HEADER, DISPLAY_FROM);
        mockHttpServletRequestBuilder.header(PublicationConfiguration.LIST_TYPE, LIST_TYPE);
        mockHttpServletRequestBuilder.header(PublicationConfiguration.COURT_ID, COURT_ID);
        mockHttpServletRequestBuilder.header(PublicationConfiguration.CONTENT_DATE, CONTENT_DATE);
        mockHttpServletRequestBuilder.contentType(isJson ? MediaType.APPLICATION_JSON : MediaType.MULTIPART_FORM_DATA);
        MvcResult response = mockMvc.perform(mockHttpServletRequestBuilder)
            .andExpect(status().isBadRequest()).andReturn();
        assertFalse(response.getResponse().getContentAsString().isEmpty(), VALIDATION_EMPTY_RESPONSE);
        ExceptionResponse exceptionResponse = objectMapper.readValue(
            response.getResponse().getContentAsString(),
            ExceptionResponse.class
        );

        assertTrue(exceptionResponse.getMessage().contains(DISPLAY_TO_RESPONSE), VALIDATION_EXCEPTION_RESPONSE);
    }

    @DisplayName("Should return a 400 bad request if artefact type is judgement and date from is empty")
    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void testDateFromAbsenceJudgement(boolean isJson) throws Exception {
        if (isJson) {
            mockHttpServletRequestBuilder = MockMvcRequestBuilders.post(POST_URL).content(payload);
        } else {
            mockHttpServletRequestBuilder = MockMvcRequestBuilders.multipart(POST_URL).file(file);
        }
        mockHttpServletRequestBuilder.header(
            PublicationConfiguration.TYPE_HEADER,
            ArtefactType.JUDGEMENTS_AND_OUTCOMES
        );
        mockHttpServletRequestBuilder.header(PublicationConfiguration.SENSITIVITY_HEADER, SENSITIVITY);
        mockHttpServletRequestBuilder.header(PublicationConfiguration.PROVENANCE_HEADER, PROVENANCE);
        mockHttpServletRequestBuilder.header(PublicationConfiguration.SOURCE_ARTEFACT_ID_HEADER, SOURCE_ARTEFACT_ID);
        mockHttpServletRequestBuilder.header(PublicationConfiguration.LANGUAGE_HEADER, LANGUAGE);
        mockHttpServletRequestBuilder.header(PublicationConfiguration.DISPLAY_TO_HEADER, DISPLAY_TO);
        mockHttpServletRequestBuilder.header(PublicationConfiguration.LIST_TYPE, LIST_TYPE);
        mockHttpServletRequestBuilder.header(PublicationConfiguration.COURT_ID, COURT_ID);
        mockHttpServletRequestBuilder.header(PublicationConfiguration.CONTENT_DATE, CONTENT_DATE);
        mockHttpServletRequestBuilder.contentType(isJson ? MediaType.APPLICATION_JSON : MediaType.MULTIPART_FORM_DATA);
        MvcResult response = mockMvc.perform(mockHttpServletRequestBuilder)
            .andExpect(status().isBadRequest()).andReturn();
        assertFalse(response.getResponse().getContentAsString().isEmpty(), VALIDATION_EMPTY_RESPONSE);
        ExceptionResponse exceptionResponse = objectMapper.readValue(
            response.getResponse().getContentAsString(),
            ExceptionResponse.class
        );

        assertTrue(exceptionResponse.getMessage().contains(DISPLAY_FROM_RESPONSE), VALIDATION_EXCEPTION_RESPONSE);
    }


    @DisplayName("Should return a 400 bad request if artefact type is list and date from is empty")
    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void testDateFromAbsenceList(boolean isJson) throws Exception {
        if (isJson) {
            mockHttpServletRequestBuilder = MockMvcRequestBuilders.post(POST_URL).content(payload);
        } else {
            mockHttpServletRequestBuilder = MockMvcRequestBuilders.multipart(POST_URL).file(file);
        }
        mockHttpServletRequestBuilder.header(PublicationConfiguration.TYPE_HEADER, ARTEFACT_TYPE);
        mockHttpServletRequestBuilder.header(PublicationConfiguration.SENSITIVITY_HEADER, SENSITIVITY);
        mockHttpServletRequestBuilder.header(PublicationConfiguration.PROVENANCE_HEADER, PROVENANCE);
        mockHttpServletRequestBuilder.header(PublicationConfiguration.SOURCE_ARTEFACT_ID_HEADER, SOURCE_ARTEFACT_ID);
        mockHttpServletRequestBuilder.header(PublicationConfiguration.LANGUAGE_HEADER, LANGUAGE);
        mockHttpServletRequestBuilder.header(PublicationConfiguration.DISPLAY_FROM_HEADER, DISPLAY_FROM);
        mockHttpServletRequestBuilder.header(PublicationConfiguration.LIST_TYPE, LIST_TYPE);
        mockHttpServletRequestBuilder.header(PublicationConfiguration.COURT_ID, COURT_ID);
        mockHttpServletRequestBuilder.header(PublicationConfiguration.CONTENT_DATE, CONTENT_DATE);
        mockHttpServletRequestBuilder.contentType(isJson ? MediaType.APPLICATION_JSON : MediaType.MULTIPART_FORM_DATA);
        MvcResult response = mockMvc.perform(mockHttpServletRequestBuilder)
            .andExpect(status().isBadRequest()).andReturn();
        assertFalse(response.getResponse().getContentAsString().isEmpty(), VALIDATION_EMPTY_RESPONSE);
        ExceptionResponse exceptionResponse = objectMapper.readValue(
            response.getResponse().getContentAsString(),
            ExceptionResponse.class
        );

        assertTrue(exceptionResponse.getMessage().contains(DISPLAY_TO_RESPONSE), VALIDATION_EXCEPTION_RESPONSE);
    }

    @DisplayName("Should return a 400 Bad Request if the artifact type header is empty")
    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void testEmptyArtifactType(boolean isJson) throws Exception {
        if (isJson) {
            mockHttpServletRequestBuilder = MockMvcRequestBuilders.post(POST_URL).content(payload);
        } else {
            mockHttpServletRequestBuilder = MockMvcRequestBuilders.multipart(POST_URL).file(file);
        }
        mockHttpServletRequestBuilder.header(PublicationConfiguration.TYPE_HEADER, EMPTY_VALUE);
        mockHttpServletRequestBuilder.header(PublicationConfiguration.SENSITIVITY_HEADER, SENSITIVITY);
        mockHttpServletRequestBuilder.header(PublicationConfiguration.PROVENANCE_HEADER, PROVENANCE);
        mockHttpServletRequestBuilder.header(PublicationConfiguration.SOURCE_ARTEFACT_ID_HEADER, SOURCE_ARTEFACT_ID);
        mockHttpServletRequestBuilder.header(PublicationConfiguration.DISPLAY_TO_HEADER, DISPLAY_TO);
        mockHttpServletRequestBuilder.header(PublicationConfiguration.DISPLAY_FROM_HEADER, DISPLAY_FROM);
        mockHttpServletRequestBuilder.header(PublicationConfiguration.LANGUAGE_HEADER, LANGUAGE);
        mockHttpServletRequestBuilder.header(PublicationConfiguration.LIST_TYPE, LIST_TYPE);
        mockHttpServletRequestBuilder.header(PublicationConfiguration.COURT_ID, COURT_ID);
        mockHttpServletRequestBuilder.header(PublicationConfiguration.CONTENT_DATE, CONTENT_DATE);
        mockHttpServletRequestBuilder.contentType(isJson ? MediaType.APPLICATION_JSON : MediaType.MULTIPART_FORM_DATA);

        MvcResult response = mockMvc.perform(mockHttpServletRequestBuilder)
            .andExpect(status().isBadRequest()).andReturn();

        assertFalse(response.getResponse().getContentAsString().isEmpty(), VALIDATION_EMPTY_RESPONSE);
        ExceptionResponse exceptionResponse = objectMapper.readValue(
            response.getResponse().getContentAsString(),
            ExceptionResponse.class
        );

        assertTrue(exceptionResponse.getMessage().contains("x-type"), VALIDATION_EXCEPTION_RESPONSE);
    }

    @DisplayName("Should return a 400 Bad Request if date from field is not populated on a list")
    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void testDateFromField(boolean isJson) throws Exception {
        if (isJson) {
            mockHttpServletRequestBuilder = MockMvcRequestBuilders.post(POST_URL).content(payload);
        } else {
            mockHttpServletRequestBuilder = MockMvcRequestBuilders.multipart(POST_URL).file(file);
        }
        mockHttpServletRequestBuilder.header(PublicationConfiguration.SENSITIVITY_HEADER, SENSITIVITY);
        mockHttpServletRequestBuilder.header(PublicationConfiguration.PROVENANCE_HEADER, PROVENANCE);
        mockHttpServletRequestBuilder.header(PublicationConfiguration.SOURCE_ARTEFACT_ID_HEADER, SOURCE_ARTEFACT_ID);
        mockHttpServletRequestBuilder.header(PublicationConfiguration.LANGUAGE_HEADER, LANGUAGE);
        mockHttpServletRequestBuilder.header(PublicationConfiguration.TYPE_HEADER, ARTEFACT_TYPE);
        mockHttpServletRequestBuilder.header(PublicationConfiguration.LIST_TYPE, LIST_TYPE);
        mockHttpServletRequestBuilder.header(PublicationConfiguration.COURT_ID, COURT_ID);
        mockHttpServletRequestBuilder.header(PublicationConfiguration.CONTENT_DATE, CONTENT_DATE);
        mockHttpServletRequestBuilder.contentType(isJson ? MediaType.APPLICATION_JSON : MediaType.MULTIPART_FORM_DATA);

        MvcResult response = mockMvc.perform(mockHttpServletRequestBuilder)
            .andExpect(status().isBadRequest()).andReturn();
        assertFalse(response.getResponse().getContentAsString().isEmpty(), VALIDATION_EMPTY_RESPONSE);
        ExceptionResponse exceptionResponse = objectMapper.readValue(
            response.getResponse().getContentAsString(),
            ExceptionResponse.class
        );
        assertTrue(exceptionResponse.getMessage().contains(
            DISPLAY_FROM_RESPONSE), VALIDATION_EXCEPTION_RESPONSE);

    }


    @DisplayName("Should return a 400 Bad Request if an invalid artifact type is provided")
    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void testInvalidArtifactType(boolean isJson) throws Exception {
        if (isJson) {
            mockHttpServletRequestBuilder = MockMvcRequestBuilders.post(POST_URL).content(payload);
        } else {
            mockHttpServletRequestBuilder = MockMvcRequestBuilders.multipart(POST_URL).file(file);
        }
        mockHttpServletRequestBuilder.header(PublicationConfiguration.TYPE_HEADER, TEST_VALUE);
        mockHttpServletRequestBuilder.header(PublicationConfiguration.SENSITIVITY_HEADER, SENSITIVITY);
        mockHttpServletRequestBuilder.header(PublicationConfiguration.PROVENANCE_HEADER, PROVENANCE);
        mockHttpServletRequestBuilder.header(PublicationConfiguration.SOURCE_ARTEFACT_ID_HEADER, SOURCE_ARTEFACT_ID);
        mockHttpServletRequestBuilder.header(PublicationConfiguration.DISPLAY_TO_HEADER, DISPLAY_TO);
        mockHttpServletRequestBuilder.header(PublicationConfiguration.DISPLAY_FROM_HEADER, DISPLAY_FROM);
        mockHttpServletRequestBuilder.header(PublicationConfiguration.LANGUAGE_HEADER, LANGUAGE);
        mockHttpServletRequestBuilder.header(PublicationConfiguration.LIST_TYPE, LIST_TYPE);
        mockHttpServletRequestBuilder.header(PublicationConfiguration.COURT_ID, COURT_ID);
        mockHttpServletRequestBuilder.header(PublicationConfiguration.CONTENT_DATE, CONTENT_DATE);
        mockHttpServletRequestBuilder.contentType(isJson ? MediaType.APPLICATION_JSON : MediaType.MULTIPART_FORM_DATA);

        MvcResult response = mockMvc.perform(mockHttpServletRequestBuilder)
            .andExpect(status().isBadRequest()).andReturn();

        assertFalse(response.getResponse().getContentAsString().isEmpty(), VALIDATION_EMPTY_RESPONSE);
        ExceptionResponse exceptionResponse = objectMapper.readValue(
            response.getResponse().getContentAsString(),
            ExceptionResponse.class
        );

        assertTrue(exceptionResponse.getMessage().contains(
            String.format("Unable to parse x-type. %s", FORMAT_RESPONSE)), VALIDATION_EXCEPTION_RESPONSE);
    }

    @DisplayName("Should return a 400 Bad Request if the provenance header is missing")
    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void testMissingProvenance(boolean isJson) throws Exception {
        if (isJson) {
            mockHttpServletRequestBuilder = MockMvcRequestBuilders.post(POST_URL).content(payload);
        } else {
            mockHttpServletRequestBuilder = MockMvcRequestBuilders.multipart(POST_URL).file(file);
        }
        mockHttpServletRequestBuilder.header(PublicationConfiguration.TYPE_HEADER, ARTEFACT_TYPE);
        mockHttpServletRequestBuilder.header(PublicationConfiguration.SENSITIVITY_HEADER, SENSITIVITY);
        mockHttpServletRequestBuilder.header(PublicationConfiguration.SOURCE_ARTEFACT_ID_HEADER, SOURCE_ARTEFACT_ID);
        mockHttpServletRequestBuilder.header(PublicationConfiguration.DISPLAY_TO_HEADER, DISPLAY_TO);
        mockHttpServletRequestBuilder.header(PublicationConfiguration.DISPLAY_FROM_HEADER, DISPLAY_FROM);
        mockHttpServletRequestBuilder.header(PublicationConfiguration.LANGUAGE_HEADER, LANGUAGE);
        mockHttpServletRequestBuilder.header(PublicationConfiguration.LIST_TYPE, LIST_TYPE);
        mockHttpServletRequestBuilder.header(PublicationConfiguration.COURT_ID, COURT_ID);
        mockHttpServletRequestBuilder.header(PublicationConfiguration.CONTENT_DATE, CONTENT_DATE);
        mockHttpServletRequestBuilder.contentType(isJson ? MediaType.APPLICATION_JSON : MediaType.MULTIPART_FORM_DATA);

        MvcResult response = mockMvc.perform(mockHttpServletRequestBuilder)
            .andExpect(status().isBadRequest()).andReturn();

        assertFalse(response.getResponse().getContentAsString().isEmpty(), VALIDATION_EMPTY_RESPONSE);
        ExceptionResponse exceptionResponse = objectMapper.readValue(
            response.getResponse().getContentAsString(),
            ExceptionResponse.class
        );

        assertTrue(exceptionResponse.getMessage().contains("x-provenance"), VALIDATION_EXCEPTION_RESPONSE);
    }

    @DisplayName("Should return a 400 Bad Request if the provenance header is empty")
    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void testEmptyProvenance(boolean isJson) throws Exception {
        if (isJson) {
            mockHttpServletRequestBuilder = MockMvcRequestBuilders.post(POST_URL).content(payload);
        } else {
            mockHttpServletRequestBuilder = MockMvcRequestBuilders.multipart(POST_URL).file(file);
        }
        mockHttpServletRequestBuilder.header(PublicationConfiguration.TYPE_HEADER, ARTEFACT_TYPE);
        mockHttpServletRequestBuilder.header(PublicationConfiguration.SENSITIVITY_HEADER, SENSITIVITY);
        mockHttpServletRequestBuilder.header(PublicationConfiguration.SOURCE_ARTEFACT_ID_HEADER, SOURCE_ARTEFACT_ID);
        mockHttpServletRequestBuilder.header(PublicationConfiguration.DISPLAY_TO_HEADER, DISPLAY_TO);
        mockHttpServletRequestBuilder.header(PublicationConfiguration.DISPLAY_FROM_HEADER, DISPLAY_FROM);
        mockHttpServletRequestBuilder.header(PublicationConfiguration.LANGUAGE_HEADER, LANGUAGE);
        mockHttpServletRequestBuilder.header(PublicationConfiguration.PROVENANCE_HEADER, EMPTY_VALUE);
        mockHttpServletRequestBuilder.header(PublicationConfiguration.LIST_TYPE, LIST_TYPE);
        mockHttpServletRequestBuilder.header(PublicationConfiguration.COURT_ID, COURT_ID);
        mockHttpServletRequestBuilder.header(PublicationConfiguration.CONTENT_DATE, CONTENT_DATE);
        mockHttpServletRequestBuilder.contentType(isJson ? MediaType.APPLICATION_JSON : MediaType.MULTIPART_FORM_DATA);

        MvcResult response = mockMvc.perform(mockHttpServletRequestBuilder)
            .andExpect(status().isBadRequest()).andReturn();

        assertFalse(response.getResponse().getContentAsString().isEmpty(), VALIDATION_EMPTY_RESPONSE);
        ExceptionResponse exceptionResponse = objectMapper.readValue(
            response.getResponse().getContentAsString(),
            ExceptionResponse.class
        );

        assertTrue(exceptionResponse.getMessage().contains("x-provenance"), VALIDATION_EXCEPTION_RESPONSE);
    }

    @DisplayName("Should return a 400 Bad Request if the source artifact id is missing")
    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void testMissingSourceArtifactId(boolean isJson) throws Exception {
        if (isJson) {
            mockHttpServletRequestBuilder = MockMvcRequestBuilders.post(POST_URL).content(payload);
        } else {
            mockHttpServletRequestBuilder = MockMvcRequestBuilders.multipart(POST_URL).file(file);
        }
        mockHttpServletRequestBuilder.header(PublicationConfiguration.TYPE_HEADER, ARTEFACT_TYPE);
        mockHttpServletRequestBuilder.header(PublicationConfiguration.SENSITIVITY_HEADER, SENSITIVITY);
        mockHttpServletRequestBuilder.header(PublicationConfiguration.PROVENANCE_HEADER, PROVENANCE);
        mockHttpServletRequestBuilder.header(PublicationConfiguration.DISPLAY_TO_HEADER, DISPLAY_TO);
        mockHttpServletRequestBuilder.header(PublicationConfiguration.DISPLAY_FROM_HEADER, DISPLAY_FROM);
        mockHttpServletRequestBuilder.header(PublicationConfiguration.LANGUAGE_HEADER, LANGUAGE);
        mockHttpServletRequestBuilder.header(PublicationConfiguration.LIST_TYPE, LIST_TYPE);
        mockHttpServletRequestBuilder.header(PublicationConfiguration.COURT_ID, COURT_ID);
        mockHttpServletRequestBuilder.header(PublicationConfiguration.CONTENT_DATE, CONTENT_DATE);
        mockHttpServletRequestBuilder.contentType(isJson ? MediaType.APPLICATION_JSON : MediaType.MULTIPART_FORM_DATA);

        MvcResult response = mockMvc.perform(mockHttpServletRequestBuilder)
            .andExpect(status().isBadRequest()).andReturn();

        assertFalse(response.getResponse().getContentAsString().isEmpty(), VALIDATION_EMPTY_RESPONSE);
        ExceptionResponse exceptionResponse = objectMapper.readValue(
            response.getResponse().getContentAsString(),
            ExceptionResponse.class
        );

        assertTrue(exceptionResponse.getMessage().contains("x-source-artefact-id"), VALIDATION_EXCEPTION_RESPONSE);
    }

    @DisplayName("Should return a 400 Bad Request if the source artifact id is empty")
    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void testEmptySourceArtifactId(boolean isJson) throws Exception {
        if (isJson) {
            mockHttpServletRequestBuilder = MockMvcRequestBuilders.post(POST_URL).content(payload);
        } else {
            mockHttpServletRequestBuilder = MockMvcRequestBuilders.multipart(POST_URL).file(file);
        }
        mockHttpServletRequestBuilder.header(PublicationConfiguration.SOURCE_ARTEFACT_ID_HEADER, EMPTY_VALUE);
        mockHttpServletRequestBuilder.header(PublicationConfiguration.TYPE_HEADER, ARTEFACT_TYPE);
        mockHttpServletRequestBuilder.header(PublicationConfiguration.SENSITIVITY_HEADER, SENSITIVITY);
        mockHttpServletRequestBuilder.header(PublicationConfiguration.PROVENANCE_HEADER, PROVENANCE);
        mockHttpServletRequestBuilder.header(PublicationConfiguration.DISPLAY_TO_HEADER, DISPLAY_TO);
        mockHttpServletRequestBuilder.header(PublicationConfiguration.DISPLAY_FROM_HEADER, DISPLAY_FROM);
        mockHttpServletRequestBuilder.header(PublicationConfiguration.LANGUAGE_HEADER, LANGUAGE);
        mockHttpServletRequestBuilder.header(PublicationConfiguration.LIST_TYPE, LIST_TYPE);
        mockHttpServletRequestBuilder.header(PublicationConfiguration.COURT_ID, COURT_ID);
        mockHttpServletRequestBuilder.header(PublicationConfiguration.CONTENT_DATE, CONTENT_DATE);
        mockHttpServletRequestBuilder.contentType(isJson ? MediaType.APPLICATION_JSON : MediaType.MULTIPART_FORM_DATA);

        MvcResult response = mockMvc.perform(mockHttpServletRequestBuilder)
            .andExpect(status().isBadRequest()).andReturn();

        assertFalse(response.getResponse().getContentAsString().isEmpty(), VALIDATION_EMPTY_RESPONSE);
        ExceptionResponse exceptionResponse = objectMapper.readValue(
            response.getResponse().getContentAsString(),
            ExceptionResponse.class
        );

        assertTrue(exceptionResponse.getMessage().contains("x-source-artefact-id"), VALIDATION_EXCEPTION_RESPONSE);
    }

    @DisplayName("Should return a 400 Bad Request if an invalid display to is provided")
    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void testInvalidDisplayTo(boolean isJson) throws Exception {
        if (isJson) {
            mockHttpServletRequestBuilder = MockMvcRequestBuilders.post(POST_URL).content(payload);
        } else {
            mockHttpServletRequestBuilder = MockMvcRequestBuilders.multipart(POST_URL).file(file);
        }
        mockHttpServletRequestBuilder.header(PublicationConfiguration.TYPE_HEADER, ARTEFACT_TYPE);
        mockHttpServletRequestBuilder.header(PublicationConfiguration.SENSITIVITY_HEADER, SENSITIVITY);
        mockHttpServletRequestBuilder.header(PublicationConfiguration.PROVENANCE_HEADER, PROVENANCE);
        mockHttpServletRequestBuilder.header(PublicationConfiguration.SOURCE_ARTEFACT_ID_HEADER, SOURCE_ARTEFACT_ID);
        mockHttpServletRequestBuilder.header(PublicationConfiguration.DISPLAY_TO_HEADER, TEST_VALUE);
        mockHttpServletRequestBuilder.header(PublicationConfiguration.DISPLAY_FROM_HEADER, DISPLAY_FROM);
        mockHttpServletRequestBuilder.header(PublicationConfiguration.LANGUAGE_HEADER, LANGUAGE);
        mockHttpServletRequestBuilder.header(PublicationConfiguration.LIST_TYPE, LIST_TYPE);
        mockHttpServletRequestBuilder.header(PublicationConfiguration.COURT_ID, COURT_ID);
        mockHttpServletRequestBuilder.header(PublicationConfiguration.CONTENT_DATE, CONTENT_DATE);
        mockHttpServletRequestBuilder.contentType(isJson ? MediaType.APPLICATION_JSON : MediaType.MULTIPART_FORM_DATA);

        MvcResult response = mockMvc.perform(mockHttpServletRequestBuilder)
            .andExpect(status().isBadRequest()).andReturn();

        assertFalse(response.getResponse().getContentAsString().isEmpty(), VALIDATION_EMPTY_RESPONSE);
        ExceptionResponse exceptionResponse = objectMapper.readValue(
            response.getResponse().getContentAsString(),
            ExceptionResponse.class
        );

        assertTrue(exceptionResponse.getMessage().contains(
            String.format("Unable to parse x-display-to. %s", FORMAT_RESPONSE)), VALIDATION_EXCEPTION_RESPONSE);
    }

    @DisplayName("Should return a 400 Bad Request if an invalid display from is provided")
    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void testInvalidDisplayFrom(boolean isJson) throws Exception {
        if (isJson) {
            mockHttpServletRequestBuilder = MockMvcRequestBuilders.post(POST_URL).content(payload);
        } else {
            mockHttpServletRequestBuilder = MockMvcRequestBuilders.multipart(POST_URL).file(file);
        }
        mockHttpServletRequestBuilder.header(PublicationConfiguration.TYPE_HEADER, ARTEFACT_TYPE);
        mockHttpServletRequestBuilder.header(PublicationConfiguration.SENSITIVITY_HEADER, SENSITIVITY);
        mockHttpServletRequestBuilder.header(PublicationConfiguration.PROVENANCE_HEADER, PROVENANCE);
        mockHttpServletRequestBuilder.header(PublicationConfiguration.SOURCE_ARTEFACT_ID_HEADER, SOURCE_ARTEFACT_ID);
        mockHttpServletRequestBuilder.header(PublicationConfiguration.DISPLAY_TO_HEADER, DISPLAY_TO);
        mockHttpServletRequestBuilder.header(PublicationConfiguration.DISPLAY_FROM_HEADER, TEST_VALUE);
        mockHttpServletRequestBuilder.header(PublicationConfiguration.LANGUAGE_HEADER, LANGUAGE);
        mockHttpServletRequestBuilder.header(PublicationConfiguration.LIST_TYPE, LIST_TYPE);
        mockHttpServletRequestBuilder.header(PublicationConfiguration.COURT_ID, COURT_ID);
        mockHttpServletRequestBuilder.header(PublicationConfiguration.CONTENT_DATE, CONTENT_DATE);
        mockHttpServletRequestBuilder.contentType(isJson ? MediaType.APPLICATION_JSON : MediaType.MULTIPART_FORM_DATA);

        MvcResult response = mockMvc.perform(mockHttpServletRequestBuilder)
            .andExpect(status().isBadRequest()).andReturn();

        assertFalse(response.getResponse().getContentAsString().isEmpty(), VALIDATION_EMPTY_RESPONSE);
        ExceptionResponse exceptionResponse = objectMapper.readValue(
            response.getResponse().getContentAsString(),
            ExceptionResponse.class
        );

        assertTrue(exceptionResponse.getMessage().contains(
            String.format("Unable to parse x-display-from. %s", FORMAT_RESPONSE)), VALIDATION_EXCEPTION_RESPONSE);
    }

    @DisplayName("Should return a 400 Bad Request if an invalid language is provided")
    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void testInvalidLanguageHeader(boolean isJson) throws Exception {
        if (isJson) {
            mockHttpServletRequestBuilder = MockMvcRequestBuilders.post(POST_URL).content(payload);
        } else {
            mockHttpServletRequestBuilder = MockMvcRequestBuilders.multipart(POST_URL).file(file);
        }
        mockHttpServletRequestBuilder.header(PublicationConfiguration.TYPE_HEADER, ARTEFACT_TYPE);
        mockHttpServletRequestBuilder.header(PublicationConfiguration.SENSITIVITY_HEADER, SENSITIVITY);
        mockHttpServletRequestBuilder.header(PublicationConfiguration.PROVENANCE_HEADER, PROVENANCE);
        mockHttpServletRequestBuilder.header(PublicationConfiguration.SOURCE_ARTEFACT_ID_HEADER, SOURCE_ARTEFACT_ID);
        mockHttpServletRequestBuilder.header(PublicationConfiguration.DISPLAY_TO_HEADER, DISPLAY_TO);
        mockHttpServletRequestBuilder.header(PublicationConfiguration.DISPLAY_FROM_HEADER, DISPLAY_FROM);
        mockHttpServletRequestBuilder.header(PublicationConfiguration.LANGUAGE_HEADER, TEST_VALUE);
        mockHttpServletRequestBuilder.header(PublicationConfiguration.LIST_TYPE, LIST_TYPE);
        mockHttpServletRequestBuilder.header(PublicationConfiguration.COURT_ID, COURT_ID);
        mockHttpServletRequestBuilder.header(PublicationConfiguration.CONTENT_DATE, CONTENT_DATE);
        mockHttpServletRequestBuilder.content(payload);
        mockHttpServletRequestBuilder.contentType(isJson ? MediaType.APPLICATION_JSON : MediaType.MULTIPART_FORM_DATA);

        MvcResult response = mockMvc.perform(mockHttpServletRequestBuilder)
            .andExpect(status().isBadRequest()).andReturn();

        assertFalse(response.getResponse().getContentAsString().isEmpty(), VALIDATION_EMPTY_RESPONSE);
        ExceptionResponse exceptionResponse = objectMapper.readValue(
            response.getResponse().getContentAsString(),
            ExceptionResponse.class
        );

        assertTrue(exceptionResponse.getMessage().contains(
            String.format("Unable to parse x-language. %s", FORMAT_RESPONSE)), VALIDATION_EXCEPTION_RESPONSE);
    }

    @DisplayName("Should return a 400 Bad Request if an invalid sensitivity is provided")
    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void testInvalidSensitivityHeader(boolean isJson) throws Exception {
        if (isJson) {
            mockHttpServletRequestBuilder = MockMvcRequestBuilders.post(POST_URL).content(payload);
        } else {
            mockHttpServletRequestBuilder = MockMvcRequestBuilders.multipart(POST_URL).file(file);
        }
        mockHttpServletRequestBuilder.header(PublicationConfiguration.TYPE_HEADER, ARTEFACT_TYPE);
        mockHttpServletRequestBuilder.header(PublicationConfiguration.SENSITIVITY_HEADER, TEST_VALUE);
        mockHttpServletRequestBuilder.header(PublicationConfiguration.PROVENANCE_HEADER, PROVENANCE);
        mockHttpServletRequestBuilder.header(PublicationConfiguration.SOURCE_ARTEFACT_ID_HEADER, SOURCE_ARTEFACT_ID);
        mockHttpServletRequestBuilder.header(PublicationConfiguration.DISPLAY_TO_HEADER, DISPLAY_TO);
        mockHttpServletRequestBuilder.header(PublicationConfiguration.DISPLAY_FROM_HEADER, DISPLAY_FROM);
        mockHttpServletRequestBuilder.header(PublicationConfiguration.LANGUAGE_HEADER, LANGUAGE);
        mockHttpServletRequestBuilder.header(PublicationConfiguration.LIST_TYPE, LIST_TYPE);
        mockHttpServletRequestBuilder.header(PublicationConfiguration.COURT_ID, COURT_ID);
        mockHttpServletRequestBuilder.header(PublicationConfiguration.CONTENT_DATE, CONTENT_DATE);
        mockHttpServletRequestBuilder.contentType(isJson ? MediaType.APPLICATION_JSON : MediaType.MULTIPART_FORM_DATA);

        MvcResult response = mockMvc.perform(mockHttpServletRequestBuilder)
            .andExpect(status().isBadRequest()).andReturn();

        assertNotNull(response.getResponse().getContentAsString(), VALIDATION_EMPTY_RESPONSE);

        ExceptionResponse exceptionResponse = objectMapper.readValue(
            response.getResponse().getContentAsString(),
            ExceptionResponse.class
        );

        assertTrue(exceptionResponse.getMessage().contains(
            String.format("Unable to parse x-sensitivity. %s", FORMAT_RESPONSE)), VALIDATION_EXCEPTION_RESPONSE);
    }

    @DisplayName("Should update the artefact and return the same Artefact ID as the originally created one")
    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void updatingOfAnArtefactThatAlreadyExists(boolean isJson) throws Exception {
        when(blobContainerClient.getBlobClient(any())).thenReturn(blobClient);
        when(blobContainerClient.getBlobContainerUrl()).thenReturn(BLOB_PAYLOAD_URL);

        if (isJson) {
            mockHttpServletRequestBuilder = MockMvcRequestBuilders.post(POST_URL).content(payload);
        } else {
            mockHttpServletRequestBuilder = MockMvcRequestBuilders.multipart(POST_URL).file(file);
        }
        mockHttpServletRequestBuilder.header(PublicationConfiguration.TYPE_HEADER, ARTEFACT_TYPE);
        mockHttpServletRequestBuilder.header(PublicationConfiguration.SENSITIVITY_HEADER, SENSITIVITY);
        mockHttpServletRequestBuilder.header(PublicationConfiguration.PROVENANCE_HEADER, PROVENANCE);
        mockHttpServletRequestBuilder.header(PublicationConfiguration.SOURCE_ARTEFACT_ID_HEADER, SOURCE_ARTEFACT_ID);
        mockHttpServletRequestBuilder.header(PublicationConfiguration.DISPLAY_TO_HEADER, DISPLAY_TO);
        mockHttpServletRequestBuilder.header(PublicationConfiguration.DISPLAY_FROM_HEADER, DISPLAY_FROM);
        mockHttpServletRequestBuilder.header(PublicationConfiguration.LIST_TYPE, LIST_TYPE);
        mockHttpServletRequestBuilder.header(PublicationConfiguration.COURT_ID, COURT_ID);
        mockHttpServletRequestBuilder.header(PublicationConfiguration.CONTENT_DATE, CONTENT_DATE);
        mockHttpServletRequestBuilder.contentType(isJson ? MediaType.APPLICATION_JSON : MediaType.MULTIPART_FORM_DATA);

        MvcResult createResponse =
            mockMvc.perform(mockHttpServletRequestBuilder).andExpect(status().isCreated()).andReturn();

        Artefact createdArtefact = objectMapper.readValue(
            createResponse.getResponse().getContentAsString(),
            Artefact.class
        );

        mockHttpServletRequestBuilder.header(
            PublicationConfiguration.LANGUAGE_HEADER,
            Language.BI_LINGUAL
        );

        MvcResult updatedResponse = mockMvc.perform(mockHttpServletRequestBuilder).andExpect(
            status().isCreated()).andReturn();

        Artefact updatedArtefact = objectMapper.readValue(
            updatedResponse.getResponse().getContentAsString(),
            Artefact.class
        );

        assertEquals(createdArtefact.getArtefactId(), updatedArtefact.getArtefactId(), "A new artefact has "
            + "been created rather than it being updated");

        assertEquals(Language.BI_LINGUAL, updatedArtefact.getLanguage(), "The updated artefact does "
            + "not contain the new language");
    }

    @DisplayName("Should throw a 400 bad request when payload is not of JSON through the JSON endpoint")
    @Test
    void creationOfAJsonArtefactThatIsNotJson() throws Exception {
        when(blobContainerClient.getBlobClient(any())).thenReturn(blobClient);
        when(blobContainerClient.getBlobContainerUrl()).thenReturn(BLOB_PAYLOAD_URL);

        MockHttpServletRequestBuilder mockHttpServletRequestBuilder = MockMvcRequestBuilders
            .post(POST_URL)
            .header(PublicationConfiguration.TYPE_HEADER, ARTEFACT_TYPE)
            .header(PublicationConfiguration.SENSITIVITY_HEADER, SENSITIVITY)
            .header(PublicationConfiguration.PROVENANCE_HEADER, PROVENANCE)
            .header(PublicationConfiguration.SOURCE_ARTEFACT_ID_HEADER, SOURCE_ARTEFACT_ID)
            .header(PublicationConfiguration.DISPLAY_TO_HEADER, DISPLAY_TO)
            .header(PublicationConfiguration.DISPLAY_FROM_HEADER, DISPLAY_FROM)
            .header(PublicationConfiguration.LIST_TYPE, LIST_TYPE)
            .header(PublicationConfiguration.COURT_ID, COURT_ID)
            .header(PublicationConfiguration.CONTENT_DATE, CONTENT_DATE)
            .content(PAYLOAD_UNKNOWN)
            .contentType(MediaType.APPLICATION_JSON);


        mockMvc.perform(mockHttpServletRequestBuilder).andExpect(status().isBadRequest()).andReturn();
    }


    @DisplayName("Check that null date for general_publication still allows us to return the relevant artefact")
    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void checkStatusUpdatesWithNullDateTo(boolean isJson) throws Exception {
        when(blobContainerClient.getBlobClient(any())).thenReturn(blobClient);

        MockHttpServletRequestBuilder mockHttpServletRequestBuilder;

        if (isJson) {
            mockHttpServletRequestBuilder = MockMvcRequestBuilders.post(POST_URL).content(payload);
        } else {
            mockHttpServletRequestBuilder = MockMvcRequestBuilders.multipart(POST_URL).file(file);
        }

        mockHttpServletRequestBuilder
            .header(PublicationConfiguration.TYPE_HEADER, ArtefactType.GENERAL_PUBLICATION)
            .header(PublicationConfiguration.SENSITIVITY_HEADER, SENSITIVITY)
            .header(PublicationConfiguration.PROVENANCE_HEADER, PROVENANCE)
            .header(PublicationConfiguration.SOURCE_ARTEFACT_ID_HEADER, SOURCE_ARTEFACT_ID)
            .header(PublicationConfiguration.DISPLAY_FROM_HEADER, DISPLAY_FROM.minusMonths(2))
            .header(PublicationConfiguration.LIST_TYPE, LIST_TYPE)
            .header(PublicationConfiguration.COURT_ID, COURT_ID)
            .header(PublicationConfiguration.CONTENT_DATE, CONTENT_DATE)
            .contentType(isJson ? MediaType.APPLICATION_JSON : MediaType.MULTIPART_FORM_DATA);

        mockMvc.perform(mockHttpServletRequestBuilder).andExpect(status().isCreated()).andReturn();

        MockHttpServletRequestBuilder mockHttpServletRequestBuilder1 = MockMvcRequestBuilders
            .get(SEARCH_COURT_URL + "/" + COURT_ID)
            .header(VERIFICATION_HEADER, TRUE);
        MvcResult getResponse =
            mockMvc.perform(mockHttpServletRequestBuilder1).andExpect(status().isOk()).andReturn();

        String jsonOutput = getResponse.getResponse().getContentAsString();
        JSONArray jsonArray = new JSONArray(jsonOutput);
        Artefact retrievedArtefact = objectMapper.readValue(
            jsonArray.get(0).toString(), Artefact.class
        );

        assertEquals(COURT_ID, retrievedArtefact.getCourtId(), "Artefact not found.");
    }

    @DisplayName("Should create a valid artefact and return the created artefact to the user")
    @Test
    void testCreationOfInvalidDailyCauseList() throws Exception {
        when(blobContainerClient.getBlobClient(any())).thenReturn(blobClient);
        when(blobContainerClient.getBlobContainerUrl()).thenReturn(BLOB_PAYLOAD_URL);

        try (InputStream mockFile = this.getClass().getClassLoader()
            .getResourceAsStream("data/dailyCauseListInvalid.json")) {
            MockHttpServletRequestBuilder mockHttpServletRequestBuilder = MockMvcRequestBuilders
                .post(POST_URL)
                .header(PublicationConfiguration.TYPE_HEADER, ARTEFACT_TYPE)
                .header(PublicationConfiguration.PROVENANCE_HEADER, PROVENANCE)
                .header(PublicationConfiguration.SOURCE_ARTEFACT_ID_HEADER, SOURCE_ARTEFACT_ID)
                .header(PublicationConfiguration.DISPLAY_FROM_HEADER, DISPLAY_FROM)
                .header(PublicationConfiguration.DISPLAY_TO_HEADER, DISPLAY_TO)
                .header(PublicationConfiguration.COURT_ID, COURT_ID)
                .header(PublicationConfiguration.LIST_TYPE, ListType.CIVIL_DAILY_CAUSE_LIST)
                .header(PublicationConfiguration.CONTENT_DATE, CONTENT_DATE)
                .content(mockFile.readAllBytes())
                .contentType(MediaType.APPLICATION_JSON);

            MvcResult response = mockMvc.perform(mockHttpServletRequestBuilder)
                .andExpect(status().isBadRequest()).andReturn();

            assertNotNull(response.getResponse().getContentAsString(), VALIDATION_EMPTY_RESPONSE);

            ExceptionResponse exceptionResponse = objectMapper.readValue(
                response.getResponse().getContentAsString(),
                ExceptionResponse.class
            );

            assertTrue(
                exceptionResponse.getMessage().contains("courtHouseName"),
                "Court name is not displayed in the exception response"
            );
        }
    }

    @DisplayName("Should create a valid artefact and return the created artefact to the user")
    @Test
    void testCreationOfValidDailyCauseList() throws Exception {
        when(blobContainerClient.getBlobClient(any())).thenReturn(blobClient);
        when(blobContainerClient.getBlobContainerUrl()).thenReturn(BLOB_PAYLOAD_URL);

        try (InputStream mockFile = this.getClass().getClassLoader()
            .getResourceAsStream("data/dailyCauseList.json")) {

            MockHttpServletRequestBuilder mockHttpServletRequestBuilder = MockMvcRequestBuilders
                .post(POST_URL)
                .header(PublicationConfiguration.TYPE_HEADER, ARTEFACT_TYPE)
                .header(PublicationConfiguration.PROVENANCE_HEADER, PROVENANCE)
                .header(PublicationConfiguration.SOURCE_ARTEFACT_ID_HEADER, SOURCE_ARTEFACT_ID)
                .header(PublicationConfiguration.DISPLAY_FROM_HEADER, DISPLAY_FROM)
                .header(PublicationConfiguration.DISPLAY_TO_HEADER, DISPLAY_TO)
                .header(PublicationConfiguration.COURT_ID, COURT_ID)
                .header(PublicationConfiguration.LIST_TYPE, ListType.CIVIL_DAILY_CAUSE_LIST)
                .header(PublicationConfiguration.CONTENT_DATE, CONTENT_DATE)
                .content(mockFile.readAllBytes())
                .contentType(MediaType.APPLICATION_JSON);


            MvcResult response = mockMvc.perform(mockHttpServletRequestBuilder)
                .andExpect(status().isCreated()).andReturn();

            assertNotNull(response.getResponse().getContentAsString(), VALIDATION_EMPTY_RESPONSE);

            Artefact artefact = objectMapper.readValue(
                response.getResponse().getContentAsString(), Artefact.class);

            assertNotNull(artefact.getArtefactId(), "Artefact ID is not populated");
        }
    }

    @DisplayName("Verify that artefact is returned when user is verified and sensitivity is public")
    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void verifyThatArtefactsAreReturnedForVerifiedUserWhenPublic(boolean isJson) throws Exception {
        when(blobContainerClient.getBlobClient(any())).thenReturn(blobClient);

        MockHttpServletRequestBuilder mockHttpServletRequestBuilder;

        if (isJson) {
            mockHttpServletRequestBuilder = MockMvcRequestBuilders.post(POST_URL).content(payload);
        } else {
            mockHttpServletRequestBuilder = MockMvcRequestBuilders.multipart(POST_URL).file(file);
        }

        mockHttpServletRequestBuilder
            .header(PublicationConfiguration.TYPE_HEADER, ARTEFACT_TYPE)
            .header(PublicationConfiguration.SENSITIVITY_HEADER, SENSITIVITY)
            .header(PublicationConfiguration.PROVENANCE_HEADER, PROVENANCE)
            .header(PublicationConfiguration.SOURCE_ARTEFACT_ID_HEADER, SOURCE_ARTEFACT_ID)
            .header(PublicationConfiguration.DISPLAY_TO_HEADER, DISPLAY_TO.plusMonths(1))
            .header(PublicationConfiguration.DISPLAY_FROM_HEADER, DISPLAY_FROM.minusMonths(2))
            .header(PublicationConfiguration.LIST_TYPE, LIST_TYPE)
            .header(PublicationConfiguration.COURT_ID, COURT_ID)
            .header(PublicationConfiguration.CONTENT_DATE, CONTENT_DATE)
            .contentType(isJson ? MediaType.APPLICATION_JSON : MediaType.MULTIPART_FORM_DATA);
        MvcResult createResponse =
            mockMvc.perform(mockHttpServletRequestBuilder).andExpect(status().isCreated()).andReturn();
        Artefact createdArtefact = objectMapper.readValue(
            createResponse.getResponse().getContentAsString(),
            Artefact.class
        );

        assertEquals(createdArtefact.getDisplayFrom(), DISPLAY_FROM.minusMonths(2),
                     VALIDATION_DISPLAY_FROM
        );

        MockHttpServletRequestBuilder mockHttpServletRequestBuilder1 = MockMvcRequestBuilders

            .get(SEARCH_COURT_URL + "/" + COURT_ID)
            .header(VERIFICATION_HEADER, TRUE);
        MvcResult getResponse =
            mockMvc.perform(mockHttpServletRequestBuilder1).andExpect(status().isOk()).andReturn();

        String jsonOutput = getResponse.getResponse().getContentAsString();
        JSONArray jsonArray = new JSONArray(jsonOutput);
        Artefact retrievedArtefact = objectMapper.readValue(
            jsonArray.get(0).toString(), Artefact.class
        );
        assertEquals(COURT_ID, retrievedArtefact.getCourtId(),
                     "Incorrect court ID has been retrieved from the database"
        );
    }

    @DisplayName("Verify that artefact is returned when user is unverified and sensitivity is public")
    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void verifyThatArtefactsAreReturnedForUnverifiedUserWhenPublic(boolean isJson) throws Exception {
        when(blobContainerClient.getBlobClient(any())).thenReturn(blobClient);

        MockHttpServletRequestBuilder mockHttpServletRequestBuilder;

        if (isJson) {
            mockHttpServletRequestBuilder = MockMvcRequestBuilders.post(POST_URL).content(payload);
        } else {
            mockHttpServletRequestBuilder = MockMvcRequestBuilders.multipart(POST_URL).file(file);
        }

        mockHttpServletRequestBuilder
            .header(PublicationConfiguration.TYPE_HEADER, ARTEFACT_TYPE)
            .header(PublicationConfiguration.SENSITIVITY_HEADER, SENSITIVITY)
            .header(PublicationConfiguration.PROVENANCE_HEADER, PROVENANCE)
            .header(PublicationConfiguration.SOURCE_ARTEFACT_ID_HEADER, SOURCE_ARTEFACT_ID)
            .header(PublicationConfiguration.DISPLAY_TO_HEADER, DISPLAY_TO.plusMonths(1))
            .header(PublicationConfiguration.DISPLAY_FROM_HEADER, DISPLAY_FROM.minusMonths(2))
            .header(PublicationConfiguration.LIST_TYPE, LIST_TYPE)
            .header(PublicationConfiguration.COURT_ID, COURT_ID)
            .header(PublicationConfiguration.CONTENT_DATE, CONTENT_DATE)
            .contentType(isJson ? MediaType.APPLICATION_JSON : MediaType.MULTIPART_FORM_DATA);

        MvcResult createResponse =
            mockMvc.perform(mockHttpServletRequestBuilder).andExpect(status().isCreated()).andReturn();
        Artefact createdArtefact = objectMapper.readValue(
            createResponse.getResponse().getContentAsString(),
            Artefact.class
        );

        assertEquals(createdArtefact.getDisplayFrom(), DISPLAY_FROM.minusMonths(2),
                     VALIDATION_DISPLAY_FROM
        );

        MockHttpServletRequestBuilder mockHttpServletRequestBuilder1 = MockMvcRequestBuilders
            .get(SEARCH_COURT_URL + "/" + COURT_ID)
            .header(VERIFICATION_HEADER, VERIFICATION_FALSE);
        MvcResult getResponse =
            mockMvc.perform(mockHttpServletRequestBuilder1).andExpect(status().isOk()).andReturn();

        String jsonOutput = getResponse.getResponse().getContentAsString();
        JSONArray jsonArray = new JSONArray(jsonOutput);
        Artefact retrievedArtefact = objectMapper.readValue(
            jsonArray.get(0).toString(), Artefact.class
        );
        assertEquals(COURT_ID, retrievedArtefact.getCourtId(),
                     "Incorrect court ID has been retrieved from the database"
        );

    }

    @DisplayName("Verify that artefact is not returned when user is unverified and artefact is classified")
    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void verifyThatArtefactsAreNotReturnedForUnverifiedUserWhenClassified(boolean isJson) throws Exception {
        when(blobContainerClient.getBlobClient(any())).thenReturn(blobClient);

        MockHttpServletRequestBuilder mockHttpServletRequestBuilder;

        if (isJson) {
            mockHttpServletRequestBuilder = MockMvcRequestBuilders.post(POST_URL).content(payload);
        } else {
            mockHttpServletRequestBuilder = MockMvcRequestBuilders.multipart(POST_URL).file(file);
        }

        mockHttpServletRequestBuilder
            .header(PublicationConfiguration.TYPE_HEADER, ARTEFACT_TYPE)
            .header(PublicationConfiguration.SENSITIVITY_HEADER, Sensitivity.CLASSIFIED)
            .header(PublicationConfiguration.PROVENANCE_HEADER, PROVENANCE)
            .header(PublicationConfiguration.SOURCE_ARTEFACT_ID_HEADER, SOURCE_ARTEFACT_ID)
            .header(PublicationConfiguration.DISPLAY_TO_HEADER, DISPLAY_TO.plusMonths(1))
            .header(PublicationConfiguration.DISPLAY_FROM_HEADER, DISPLAY_FROM.minusMonths(2))
            .header(PublicationConfiguration.LIST_TYPE, LIST_TYPE)
            .header(PublicationConfiguration.COURT_ID, COURT_ID)
            .header(PublicationConfiguration.CONTENT_DATE, CONTENT_DATE)
            .contentType(isJson ? MediaType.APPLICATION_JSON : MediaType.MULTIPART_FORM_DATA);

        MvcResult createResponse =
            mockMvc.perform(mockHttpServletRequestBuilder).andExpect(status().isCreated()).andReturn();
        Artefact createdArtefact = objectMapper.readValue(
            createResponse.getResponse().getContentAsString(),
            Artefact.class
        );

        assertEquals(createdArtefact.getDisplayFrom(), DISPLAY_FROM.minusMonths(2),
                     VALIDATION_DISPLAY_FROM
        );

        MockHttpServletRequestBuilder mockHttpServletRequestBuilder1 = MockMvcRequestBuilders
            .get(SEARCH_COURT_URL + "/" + COURT_ID)
            .header(VERIFICATION_HEADER, VERIFICATION_FALSE);
        MvcResult getResponse =
            mockMvc.perform(mockHttpServletRequestBuilder1).andExpect(status().isOk()).andReturn();

        String jsonOutput = getResponse.getResponse().getContentAsString();
        JSONArray jsonArray = new JSONArray(jsonOutput);
        assertEquals(0, jsonArray.length(),
                     "Unknown artefacts have been returned from the database"
        );

    }

    @DisplayName("Verify that artefact is not returned when user is unverified and artefact is private")
    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void verifyThatArtefactsAreNotReturnedForUnverifiedUserWhenPrivate(boolean isJson) throws Exception {
        when(blobContainerClient.getBlobClient(any())).thenReturn(blobClient);

        MockHttpServletRequestBuilder mockHttpServletRequestBuilder;

        if (isJson) {
            mockHttpServletRequestBuilder = MockMvcRequestBuilders.post(POST_URL).content(payload);
        } else {
            mockHttpServletRequestBuilder = MockMvcRequestBuilders.multipart(POST_URL).file(file);
        }

        mockHttpServletRequestBuilder
            .header(PublicationConfiguration.TYPE_HEADER, ARTEFACT_TYPE)
            .header(PublicationConfiguration.SENSITIVITY_HEADER, Sensitivity.CLASSIFIED)
            .header(PublicationConfiguration.PROVENANCE_HEADER, PROVENANCE)
            .header(PublicationConfiguration.SOURCE_ARTEFACT_ID_HEADER, SOURCE_ARTEFACT_ID)
            .header(PublicationConfiguration.DISPLAY_TO_HEADER, DISPLAY_TO.plusMonths(1))
            .header(PublicationConfiguration.DISPLAY_FROM_HEADER, DISPLAY_FROM.minusMonths(2))
            .header(PublicationConfiguration.LIST_TYPE, LIST_TYPE)
            .header(PublicationConfiguration.COURT_ID, COURT_ID)
            .header(PublicationConfiguration.CONTENT_DATE, CONTENT_DATE)
            .contentType(isJson ? MediaType.APPLICATION_JSON : MediaType.MULTIPART_FORM_DATA);

        MvcResult createResponse =
            mockMvc.perform(mockHttpServletRequestBuilder).andExpect(status().isCreated()).andReturn();
        Artefact createdArtefact = objectMapper.readValue(
            createResponse.getResponse().getContentAsString(),
            Artefact.class
        );

        assertEquals(createdArtefact.getDisplayFrom(), DISPLAY_FROM.minusMonths(2),
                     VALIDATION_DISPLAY_FROM
        );

        MockHttpServletRequestBuilder mockHttpServletRequestBuilder1 = MockMvcRequestBuilders
            .get(SEARCH_COURT_URL + "/" + COURT_ID)
            .header(VERIFICATION_HEADER, FALSE);
        MvcResult getResponse =
            mockMvc.perform(mockHttpServletRequestBuilder1).andExpect(status().isOk()).andReturn();

        String jsonOutput = getResponse.getResponse().getContentAsString();
        JSONArray jsonArray = new JSONArray(jsonOutput);
        assertEquals(0, jsonArray.length(),
                     "Unknown artefacts have been returned from the database"
        );

    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    @DisplayName("File endpoint should return the file when artefact exists")
    void retrieveFileFromAnArtefactWhereFound(boolean isJson) throws Exception {
        if (isJson) {
            mockHttpServletRequestBuilder = MockMvcRequestBuilders.post(POST_URL).content(payload);
        } else {
            mockHttpServletRequestBuilder = MockMvcRequestBuilders.multipart(POST_URL).file(file);
        }
        mockHttpServletRequestBuilder.header(PublicationConfiguration.TYPE_HEADER, ARTEFACT_TYPE);
        mockHttpServletRequestBuilder.header(PublicationConfiguration.SENSITIVITY_HEADER, SENSITIVITY);
        mockHttpServletRequestBuilder.header(PublicationConfiguration.LANGUAGE_HEADER, LANGUAGE);
        mockHttpServletRequestBuilder.header(PublicationConfiguration.PROVENANCE_HEADER, PROVENANCE);
        mockHttpServletRequestBuilder.header(PublicationConfiguration.SOURCE_ARTEFACT_ID_HEADER, SOURCE_ARTEFACT_ID);
        mockHttpServletRequestBuilder.header(PublicationConfiguration.DISPLAY_TO_HEADER, DISPLAY_TO.plusMonths(1));
        mockHttpServletRequestBuilder.header(PublicationConfiguration.DISPLAY_FROM_HEADER, DISPLAY_FROM);
        mockHttpServletRequestBuilder.header(PublicationConfiguration.LIST_TYPE, LIST_TYPE);
        mockHttpServletRequestBuilder.header(PublicationConfiguration.COURT_ID, COURT_ID);
        mockHttpServletRequestBuilder.header(PublicationConfiguration.CONTENT_DATE, CONTENT_DATE);
        mockHttpServletRequestBuilder.contentType(isJson ? MediaType.APPLICATION_JSON : MediaType.MULTIPART_FORM_DATA);

        when(blobContainerClient.getBlobClient(any())).thenReturn(blobClient);
        when(blobContainerClient.getBlobContainerUrl()).thenReturn(PAYLOAD_URL);

        MvcResult response =
            mockMvc.perform(mockHttpServletRequestBuilder).andExpect(status().isCreated()).andReturn();

        assertNotNull(response.getResponse().getContentAsString(), VALIDATION_EMPTY_RESPONSE);

        Artefact artefact = objectMapper.readValue(
            response.getResponse().getContentAsString(), Artefact.class);

        when(blobClient.downloadContent()).thenReturn(
            BinaryData.fromString(isJson ? payload : new String(file.getBytes())));

        response = mockMvc.perform(MockMvcRequestBuilders
                                       .get(POST_URL + "/" + artefact.getArtefactId() + "/file")
                                       .header(VERIFICATION_HEADER, VERIFICATION_TRUE))
            .andExpect(status().isOk()).andReturn();

        assertNotNull(response.getResponse().getContentAsString(), VALIDATION_EMPTY_RESPONSE);

        assertEquals(isJson ? payload : new String(file.getBytes()),
                     response.getResponse().getContentAsString(), "File does not match expected content"
        );

    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    @DisplayName("File endpoint should not return the payload when not authorized")
    void retrieveFileOfAnArtefactWhereNotAuthorized(boolean isJson) throws Exception {
        if (isJson) {
            mockHttpServletRequestBuilder = MockMvcRequestBuilders.post(POST_URL).content(payload);
        } else {
            mockHttpServletRequestBuilder = MockMvcRequestBuilders.multipart(POST_URL).file(file);
        }
        mockHttpServletRequestBuilder.header(PublicationConfiguration.TYPE_HEADER, ARTEFACT_TYPE);
        mockHttpServletRequestBuilder.header(PublicationConfiguration.SENSITIVITY_HEADER, Sensitivity.CLASSIFIED);
        mockHttpServletRequestBuilder.header(PublicationConfiguration.LANGUAGE_HEADER, LANGUAGE);
        mockHttpServletRequestBuilder.header(PublicationConfiguration.PROVENANCE_HEADER, PROVENANCE);
        mockHttpServletRequestBuilder.header(PublicationConfiguration.SOURCE_ARTEFACT_ID_HEADER, SOURCE_ARTEFACT_ID);
        mockHttpServletRequestBuilder.header(PublicationConfiguration.DISPLAY_TO_HEADER, DISPLAY_TO.plusMonths(1));
        mockHttpServletRequestBuilder.header(PublicationConfiguration.DISPLAY_FROM_HEADER, DISPLAY_FROM);
        mockHttpServletRequestBuilder.header(PublicationConfiguration.LIST_TYPE, LIST_TYPE);
        mockHttpServletRequestBuilder.header(PublicationConfiguration.COURT_ID, COURT_ID);
        mockHttpServletRequestBuilder.header(PublicationConfiguration.CONTENT_DATE, CONTENT_DATE);
        mockHttpServletRequestBuilder.contentType(isJson ? MediaType.APPLICATION_JSON : MediaType.MULTIPART_FORM_DATA);

        when(blobContainerClient.getBlobClient(any())).thenReturn(blobClient);
        when(blobContainerClient.getBlobContainerUrl()).thenReturn(PAYLOAD_URL);

        MvcResult response =
            mockMvc.perform(mockHttpServletRequestBuilder).andExpect(status().isCreated()).andReturn();

        assertNotNull(response.getResponse().getContentAsString(), VALIDATION_EMPTY_RESPONSE);

        Artefact artefact = objectMapper.readValue(
            response.getResponse().getContentAsString(), Artefact.class);

        mockMvc.perform(MockMvcRequestBuilders
                            .get(POST_URL + "/" + artefact.getArtefactId() + "/file")
                            .header(VERIFICATION_HEADER, VERIFICATION_FALSE))
            .andExpect(status().isNotFound()).andReturn();
    }

    @Test
    @DisplayName("File endpoint should return 404 when artefact does not exist")
    void retrieveFileOfAnArtefactWhereNotFound() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders
                            .get("/publication/7d734e8d-ba1d-4730-bd8b-09a970be00cc/file")
                            .header(VERIFICATION_HEADER, VERIFICATION_TRUE))
            .andExpect(status().isNotFound()).andReturn();
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    @DisplayName("Payload endpoint should return the payload when artefact exists when verified")
    void retrievePayloadOfAnArtefactWhereFoundWhenVerified(boolean isJson) throws Exception {
        if (isJson) {
            mockHttpServletRequestBuilder = MockMvcRequestBuilders.post(POST_URL).content(payload);
        } else {
            mockHttpServletRequestBuilder = MockMvcRequestBuilders.multipart(POST_URL).file(file);
        }
        mockHttpServletRequestBuilder.header(PublicationConfiguration.TYPE_HEADER, ARTEFACT_TYPE);
        mockHttpServletRequestBuilder.header(PublicationConfiguration.SENSITIVITY_HEADER, SENSITIVITY);
        mockHttpServletRequestBuilder.header(PublicationConfiguration.LANGUAGE_HEADER, LANGUAGE);
        mockHttpServletRequestBuilder.header(PublicationConfiguration.PROVENANCE_HEADER, PROVENANCE);
        mockHttpServletRequestBuilder.header(PublicationConfiguration.SOURCE_ARTEFACT_ID_HEADER, SOURCE_ARTEFACT_ID);
        mockHttpServletRequestBuilder.header(PublicationConfiguration.DISPLAY_TO_HEADER, DISPLAY_TO.plusMonths(1));
        mockHttpServletRequestBuilder.header(PublicationConfiguration.DISPLAY_FROM_HEADER, DISPLAY_FROM);
        mockHttpServletRequestBuilder.header(PublicationConfiguration.LIST_TYPE, LIST_TYPE);
        mockHttpServletRequestBuilder.header(PublicationConfiguration.COURT_ID, COURT_ID);
        mockHttpServletRequestBuilder.header(PublicationConfiguration.CONTENT_DATE, CONTENT_DATE);
        mockHttpServletRequestBuilder.contentType(isJson ? MediaType.APPLICATION_JSON : MediaType.MULTIPART_FORM_DATA);

        when(blobContainerClient.getBlobClient(any())).thenReturn(blobClient);
        when(blobContainerClient.getBlobContainerUrl()).thenReturn(BLOB_PAYLOAD_URL);

        MvcResult response =
            mockMvc.perform(mockHttpServletRequestBuilder).andExpect(status().isCreated()).andReturn();

        assertNotNull(response.getResponse().getContentAsString(), VALIDATION_EMPTY_RESPONSE);

        Artefact artefact = objectMapper.readValue(
            response.getResponse().getContentAsString(), Artefact.class);

        when(blobClient.downloadContent()).thenReturn(
            BinaryData.fromString(isJson ? payload : new String(file.getBytes())));

        response = mockMvc.perform(MockMvcRequestBuilders
                                       .get(POST_URL + "/" + artefact.getArtefactId() + PAYLOAD_URL)
                                       .header(VERIFICATION_HEADER, VERIFICATION_TRUE))
                                       .andExpect(status().isOk()).andReturn();

        assertNotNull(response.getResponse().getContentAsString(), VALIDATION_EMPTY_RESPONSE);

        assertEquals(isJson ? payload : new String(file.getBytes()),
                     response.getResponse().getContentAsString(), "Payload does not match expected content");
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    @DisplayName("Payload endpoint should return the payload when artefact exists when unverified")
    void retrievePayloadOfAnArtefactWhereFoundWhenUnverified(boolean isJson) throws Exception {
        if (isJson) {
            mockHttpServletRequestBuilder = MockMvcRequestBuilders.post(POST_URL).content(payload);
        } else {
            mockHttpServletRequestBuilder = MockMvcRequestBuilders.multipart(POST_URL).file(file);
        }
        mockHttpServletRequestBuilder.header(PublicationConfiguration.TYPE_HEADER, ARTEFACT_TYPE);
        mockHttpServletRequestBuilder.header(PublicationConfiguration.SENSITIVITY_HEADER, SENSITIVITY);
        mockHttpServletRequestBuilder.header(PublicationConfiguration.LANGUAGE_HEADER, LANGUAGE);
        mockHttpServletRequestBuilder.header(PublicationConfiguration.PROVENANCE_HEADER, PROVENANCE);
        mockHttpServletRequestBuilder.header(PublicationConfiguration.SOURCE_ARTEFACT_ID_HEADER, SOURCE_ARTEFACT_ID);
        mockHttpServletRequestBuilder.header(PublicationConfiguration.DISPLAY_TO_HEADER, DISPLAY_TO.plusMonths(1));
        mockHttpServletRequestBuilder.header(PublicationConfiguration.DISPLAY_FROM_HEADER, DISPLAY_FROM);
        mockHttpServletRequestBuilder.header(PublicationConfiguration.LIST_TYPE, LIST_TYPE);
        mockHttpServletRequestBuilder.header(PublicationConfiguration.COURT_ID, COURT_ID);
        mockHttpServletRequestBuilder.header(PublicationConfiguration.CONTENT_DATE, CONTENT_DATE);
        mockHttpServletRequestBuilder.contentType(isJson ? MediaType.APPLICATION_JSON : MediaType.MULTIPART_FORM_DATA);

        when(blobContainerClient.getBlobClient(any())).thenReturn(blobClient);
        when(blobContainerClient.getBlobContainerUrl()).thenReturn(BLOB_PAYLOAD_URL);

        MvcResult response =
            mockMvc.perform(mockHttpServletRequestBuilder).andExpect(status().isCreated()).andReturn();

        assertNotNull(response.getResponse().getContentAsString(), VALIDATION_EMPTY_RESPONSE);

        Artefact artefact = objectMapper.readValue(
            response.getResponse().getContentAsString(), Artefact.class);

        when(blobClient.downloadContent()).thenReturn(
            BinaryData.fromString(isJson ? payload : new String(file.getBytes())));

        response = mockMvc.perform(MockMvcRequestBuilders
                                       .get(POST_URL + "/" + artefact.getArtefactId() + PAYLOAD_URL)
                                       .header(VERIFICATION_HEADER, VERIFICATION_FALSE))
            .andExpect(status().isOk()).andReturn();

        assertNotNull(response.getResponse().getContentAsString(), VALIDATION_EMPTY_RESPONSE);

        assertEquals(isJson ? payload : new String(file.getBytes()),
                     response.getResponse().getContentAsString(), "Payload does not match expected content");
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    @DisplayName("Payload endpoint should not return the payload when artefact out of range and user verified")
    void retrievePayloadOfAnArtefactWhereOutOfDateRangeWhenVerified(boolean isJson) throws Exception {
        if (isJson) {
            mockHttpServletRequestBuilder = MockMvcRequestBuilders.post(POST_URL).content(payload);
        } else {
            mockHttpServletRequestBuilder = MockMvcRequestBuilders.multipart(POST_URL).file(file);
        }
        mockHttpServletRequestBuilder.header(PublicationConfiguration.TYPE_HEADER, ARTEFACT_TYPE);
        mockHttpServletRequestBuilder.header(PublicationConfiguration.SENSITIVITY_HEADER, SENSITIVITY);
        mockHttpServletRequestBuilder.header(PublicationConfiguration.LANGUAGE_HEADER, LANGUAGE);
        mockHttpServletRequestBuilder.header(PublicationConfiguration.PROVENANCE_HEADER, PROVENANCE);
        mockHttpServletRequestBuilder.header(PublicationConfiguration.SOURCE_ARTEFACT_ID_HEADER, SOURCE_ARTEFACT_ID);
        mockHttpServletRequestBuilder.header(PublicationConfiguration.DISPLAY_TO_HEADER, DISPLAY_TO.plusMonths(2));
        mockHttpServletRequestBuilder.header(PublicationConfiguration.DISPLAY_FROM_HEADER, DISPLAY_FROM.plusMonths(1));
        mockHttpServletRequestBuilder.header(PublicationConfiguration.LIST_TYPE, LIST_TYPE);
        mockHttpServletRequestBuilder.header(PublicationConfiguration.COURT_ID, COURT_ID);
        mockHttpServletRequestBuilder.header(PublicationConfiguration.CONTENT_DATE, CONTENT_DATE);
        mockHttpServletRequestBuilder.contentType(isJson ? MediaType.APPLICATION_JSON : MediaType.MULTIPART_FORM_DATA);

        when(blobContainerClient.getBlobClient(any())).thenReturn(blobClient);
        when(blobContainerClient.getBlobContainerUrl()).thenReturn(BLOB_PAYLOAD_URL);

        MvcResult response =
            mockMvc.perform(mockHttpServletRequestBuilder).andExpect(status().isCreated()).andReturn();

        assertNotNull(response.getResponse().getContentAsString(), VALIDATION_EMPTY_RESPONSE);

        Artefact artefact = objectMapper.readValue(
            response.getResponse().getContentAsString(), Artefact.class);

        when(blobClient.downloadContent()).thenReturn(
            BinaryData.fromString(isJson ? payload : new String(file.getBytes())));

        mockMvc.perform(MockMvcRequestBuilders
                                       .get(POST_URL + "/" + artefact.getArtefactId() + PAYLOAD_URL)
                                       .header(VERIFICATION_HEADER, VERIFICATION_TRUE))
            .andExpect(status().isNotFound()).andReturn();
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})

    @DisplayName("Payload endpoint should not return the payload when artefact out of range and user unverified")
    void retrievePayloadOfAnArtefactWhereOutOfDateRangeWhenUnverified(boolean isJson) throws Exception {
        if (isJson) {
            mockHttpServletRequestBuilder = MockMvcRequestBuilders.post(POST_URL).content(payload);
        } else {
            mockHttpServletRequestBuilder = MockMvcRequestBuilders.multipart(POST_URL).file(file);
        }
        mockHttpServletRequestBuilder.header(PublicationConfiguration.TYPE_HEADER, ARTEFACT_TYPE);
        mockHttpServletRequestBuilder.header(PublicationConfiguration.SENSITIVITY_HEADER, SENSITIVITY);
        mockHttpServletRequestBuilder.header(PublicationConfiguration.LANGUAGE_HEADER, LANGUAGE);
        mockHttpServletRequestBuilder.header(PublicationConfiguration.PROVENANCE_HEADER, PROVENANCE);
        mockHttpServletRequestBuilder.header(PublicationConfiguration.SOURCE_ARTEFACT_ID_HEADER, SOURCE_ARTEFACT_ID);
        mockHttpServletRequestBuilder.header(PublicationConfiguration.DISPLAY_TO_HEADER, DISPLAY_TO.plusMonths(2));
        mockHttpServletRequestBuilder.header(PublicationConfiguration.DISPLAY_FROM_HEADER, DISPLAY_FROM.plusMonths(1));
        mockHttpServletRequestBuilder.header(PublicationConfiguration.LIST_TYPE, LIST_TYPE);
        mockHttpServletRequestBuilder.header(PublicationConfiguration.COURT_ID, COURT_ID);
        mockHttpServletRequestBuilder.header(PublicationConfiguration.CONTENT_DATE, CONTENT_DATE);
        mockHttpServletRequestBuilder.contentType(isJson ? MediaType.APPLICATION_JSON : MediaType.MULTIPART_FORM_DATA);

        when(blobContainerClient.getBlobClient(any())).thenReturn(blobClient);
        when(blobContainerClient.getBlobContainerUrl()).thenReturn(BLOB_PAYLOAD_URL);

        MvcResult response =
            mockMvc.perform(mockHttpServletRequestBuilder).andExpect(status().isCreated()).andReturn();

        assertNotNull(response.getResponse().getContentAsString(), VALIDATION_EMPTY_RESPONSE);

        Artefact artefact = objectMapper.readValue(
            response.getResponse().getContentAsString(), Artefact.class);

        when(blobClient.downloadContent()).thenReturn(
            BinaryData.fromString(isJson ? payload : new String(file.getBytes())));

        mockMvc.perform(MockMvcRequestBuilders
                            .get(POST_URL + "/" + artefact.getArtefactId() + PAYLOAD_URL)
                            .header(VERIFICATION_HEADER, VERIFICATION_FALSE))
            .andExpect(status().isNotFound()).andReturn();
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    @DisplayName("Payload endpoint should not return the payload when not authorized")
    void retrievePayloadOfAnArtefactWhereNotAuthorized(boolean isJson) throws Exception {
        if (isJson) {
            mockHttpServletRequestBuilder = MockMvcRequestBuilders.post(POST_URL).content(payload);
        } else {
            mockHttpServletRequestBuilder = MockMvcRequestBuilders.multipart(POST_URL).file(file);
        }
        mockHttpServletRequestBuilder.header(PublicationConfiguration.TYPE_HEADER, ARTEFACT_TYPE);
        mockHttpServletRequestBuilder.header(PublicationConfiguration.SENSITIVITY_HEADER, Sensitivity.CLASSIFIED);
        mockHttpServletRequestBuilder.header(PublicationConfiguration.LANGUAGE_HEADER, LANGUAGE);
        mockHttpServletRequestBuilder.header(PublicationConfiguration.PROVENANCE_HEADER, PROVENANCE);
        mockHttpServletRequestBuilder.header(PublicationConfiguration.SOURCE_ARTEFACT_ID_HEADER, SOURCE_ARTEFACT_ID);
        mockHttpServletRequestBuilder.header(PublicationConfiguration.DISPLAY_TO_HEADER, DISPLAY_TO.plusMonths(1));
        mockHttpServletRequestBuilder.header(PublicationConfiguration.DISPLAY_FROM_HEADER, DISPLAY_FROM);
        mockHttpServletRequestBuilder.header(PublicationConfiguration.LIST_TYPE, LIST_TYPE);
        mockHttpServletRequestBuilder.header(PublicationConfiguration.COURT_ID, COURT_ID);
        mockHttpServletRequestBuilder.header(PublicationConfiguration.CONTENT_DATE, CONTENT_DATE);
        mockHttpServletRequestBuilder.contentType(isJson ? MediaType.APPLICATION_JSON : MediaType.MULTIPART_FORM_DATA);

        when(blobContainerClient.getBlobClient(any())).thenReturn(blobClient);
        when(blobContainerClient.getBlobContainerUrl()).thenReturn(BLOB_PAYLOAD_URL);

        MvcResult response =
            mockMvc.perform(mockHttpServletRequestBuilder).andExpect(status().isCreated()).andReturn();

        assertNotNull(response.getResponse().getContentAsString(), VALIDATION_EMPTY_RESPONSE);

        Artefact artefact = objectMapper.readValue(
            response.getResponse().getContentAsString(), Artefact.class);

        mockMvc.perform(MockMvcRequestBuilders
                                       .get(POST_URL + "/" + artefact.getArtefactId() + PAYLOAD_URL)
                                       .header(VERIFICATION_HEADER, VERIFICATION_FALSE))
            .andExpect(status().isNotFound()).andReturn();
    }

    @Test
    @DisplayName("Payload endpoint should return 404 when artefact does not exist")
    void retrievePayloadOfAnArtefactWhereNotFound() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders
                            .get("/publication/7d734e8d-ba1d-4730-bd8b-09a970be00cc/payload")
                            .header(VERIFICATION_HEADER, VERIFICATION_TRUE))

            .andExpect(status().isNotFound()).andReturn();
    }

    @Test
    @DisplayName("Payload endpoint where verification not set should display a 400")
    void retrievePayloadWhenVerificationEndpointNotSet() throws Exception {
        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders
                                                  .get("/publication/7d734e8d-ba1d-4730-bd8b-09a970be00cc/payload"))
            .andExpect(status().isBadRequest()).andReturn();

        ExceptionResponse exceptionResponse =
            objectMapper.readValue(mvcResult.getResponse().getContentAsString(), ExceptionResponse.class);

        assertTrue(exceptionResponse.getMessage().contains(VERIFICATION_HEADER),
                   "Verification error not shown in error message");
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    @DisplayName("Metadata endpoint should return the artefact when artefact exists")
    void retrieveMetadataOfAnArtefactWhereFoundWhenVerified(boolean isJson) throws Exception {
        if (isJson) {
            mockHttpServletRequestBuilder = MockMvcRequestBuilders.post(POST_URL).content(payload);
        } else {
            mockHttpServletRequestBuilder = MockMvcRequestBuilders.multipart(POST_URL).file(file);
        }
        mockHttpServletRequestBuilder.header(PublicationConfiguration.TYPE_HEADER, ARTEFACT_TYPE);
        mockHttpServletRequestBuilder.header(PublicationConfiguration.SENSITIVITY_HEADER, SENSITIVITY);
        mockHttpServletRequestBuilder.header(PublicationConfiguration.LANGUAGE_HEADER, LANGUAGE);
        mockHttpServletRequestBuilder.header(PublicationConfiguration.PROVENANCE_HEADER, PROVENANCE);
        mockHttpServletRequestBuilder.header(PublicationConfiguration.SOURCE_ARTEFACT_ID_HEADER, SOURCE_ARTEFACT_ID);
        mockHttpServletRequestBuilder.header(PublicationConfiguration.DISPLAY_TO_HEADER, DISPLAY_TO.plusMonths(1));
        mockHttpServletRequestBuilder.header(PublicationConfiguration.DISPLAY_FROM_HEADER, DISPLAY_FROM);
        mockHttpServletRequestBuilder.header(PublicationConfiguration.LIST_TYPE, LIST_TYPE);
        mockHttpServletRequestBuilder.header(PublicationConfiguration.COURT_ID, COURT_ID);
        mockHttpServletRequestBuilder.header(PublicationConfiguration.CONTENT_DATE, CONTENT_DATE);
        mockHttpServletRequestBuilder.contentType(isJson ? MediaType.APPLICATION_JSON : MediaType.MULTIPART_FORM_DATA);

        when(blobContainerClient.getBlobClient(any())).thenReturn(blobClient);
        when(blobContainerClient.getBlobContainerUrl()).thenReturn(BLOB_PAYLOAD_URL);

        MvcResult response =
            mockMvc.perform(mockHttpServletRequestBuilder).andExpect(status().isCreated()).andReturn();

        assertNotNull(response.getResponse().getContentAsString(), VALIDATION_EMPTY_RESPONSE);

        Artefact artefact = objectMapper.readValue(
            response.getResponse().getContentAsString(), Artefact.class);

        when(blobClient.downloadContent()).thenReturn(
            BinaryData.fromString(isJson ? payload : new String(file.getBytes())));

        response = mockMvc.perform(MockMvcRequestBuilders
                                       .get(POST_URL + "/" + artefact.getArtefactId())
                                       .header(VERIFICATION_HEADER, VERIFICATION_TRUE))
            .andExpect(status().isOk()).andReturn();

        assertNotNull(response.getResponse().getContentAsString(), VALIDATION_EMPTY_RESPONSE);


        assertEquals(artefact,
                     objectMapper.readValue(response.getResponse().getContentAsString(), Artefact.class),
                     "Metadata does not match expected artefact");

    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    @DisplayName("Metadata endpoint should return the artefact when artefact exists when unverified")
    void retrieveMetadataOfAnArtefactWhereFoundWhenUnverified(boolean isJson) throws Exception {
        if (isJson) {
            mockHttpServletRequestBuilder = MockMvcRequestBuilders.post(POST_URL).content(payload);
        } else {
            mockHttpServletRequestBuilder = MockMvcRequestBuilders.multipart(POST_URL).file(file);
        }
        mockHttpServletRequestBuilder.header(PublicationConfiguration.TYPE_HEADER, ARTEFACT_TYPE);
        mockHttpServletRequestBuilder.header(PublicationConfiguration.SENSITIVITY_HEADER, SENSITIVITY);
        mockHttpServletRequestBuilder.header(PublicationConfiguration.LANGUAGE_HEADER, LANGUAGE);
        mockHttpServletRequestBuilder.header(PublicationConfiguration.PROVENANCE_HEADER, PROVENANCE);
        mockHttpServletRequestBuilder.header(PublicationConfiguration.SOURCE_ARTEFACT_ID_HEADER, SOURCE_ARTEFACT_ID);
        mockHttpServletRequestBuilder.header(PublicationConfiguration.DISPLAY_TO_HEADER, DISPLAY_TO.plusMonths(1));
        mockHttpServletRequestBuilder.header(PublicationConfiguration.DISPLAY_FROM_HEADER, DISPLAY_FROM);
        mockHttpServletRequestBuilder.header(PublicationConfiguration.LIST_TYPE, LIST_TYPE);
        mockHttpServletRequestBuilder.header(PublicationConfiguration.COURT_ID, COURT_ID);
        mockHttpServletRequestBuilder.header(PublicationConfiguration.CONTENT_DATE, CONTENT_DATE);
        mockHttpServletRequestBuilder.contentType(isJson ? MediaType.APPLICATION_JSON : MediaType.MULTIPART_FORM_DATA);

        when(blobContainerClient.getBlobClient(any())).thenReturn(blobClient);
        when(blobContainerClient.getBlobContainerUrl()).thenReturn(BLOB_PAYLOAD_URL);

        MvcResult response =
            mockMvc.perform(mockHttpServletRequestBuilder).andExpect(status().isCreated()).andReturn();

        assertNotNull(response.getResponse().getContentAsString(), VALIDATION_EMPTY_RESPONSE);

        Artefact artefact = objectMapper.readValue(
            response.getResponse().getContentAsString(), Artefact.class);

        when(blobClient.downloadContent()).thenReturn(
            BinaryData.fromString(isJson ? payload : new String(file.getBytes())));

        response = mockMvc.perform(MockMvcRequestBuilders
                                       .get(POST_URL + "/" + artefact.getArtefactId())
                                       .header(VERIFICATION_HEADER, VERIFICATION_FALSE))
            .andExpect(status().isOk()).andReturn();

        assertNotNull(response.getResponse().getContentAsString(), VALIDATION_EMPTY_RESPONSE);

        assertEquals(artefact,
                     objectMapper.readValue(response.getResponse().getContentAsString(), Artefact.class),
                     "Metadata does not match expected artefact");
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    @DisplayName("Metadata endpoint should return the artefact when artefact exists")
    void retrieveMetadataOfAnArtefactWhereOutOfDateRangeAndVerified(boolean isJson) throws Exception {
        if (isJson) {
            mockHttpServletRequestBuilder = MockMvcRequestBuilders.post(POST_URL).content(payload);
        } else {
            mockHttpServletRequestBuilder = MockMvcRequestBuilders.multipart(POST_URL).file(file);
        }
        mockHttpServletRequestBuilder.header(PublicationConfiguration.TYPE_HEADER, ARTEFACT_TYPE);
        mockHttpServletRequestBuilder.header(PublicationConfiguration.SENSITIVITY_HEADER, SENSITIVITY);
        mockHttpServletRequestBuilder.header(PublicationConfiguration.LANGUAGE_HEADER, LANGUAGE);
        mockHttpServletRequestBuilder.header(PublicationConfiguration.PROVENANCE_HEADER, PROVENANCE);
        mockHttpServletRequestBuilder.header(PublicationConfiguration.SOURCE_ARTEFACT_ID_HEADER, SOURCE_ARTEFACT_ID);
        mockHttpServletRequestBuilder.header(PublicationConfiguration.DISPLAY_TO_HEADER, DISPLAY_TO.plusMonths(2));
        mockHttpServletRequestBuilder.header(PublicationConfiguration.DISPLAY_FROM_HEADER, DISPLAY_FROM.plusMonths(1));
        mockHttpServletRequestBuilder.header(PublicationConfiguration.LIST_TYPE, LIST_TYPE);
        mockHttpServletRequestBuilder.header(PublicationConfiguration.COURT_ID, COURT_ID);
        mockHttpServletRequestBuilder.header(PublicationConfiguration.CONTENT_DATE, CONTENT_DATE);
        mockHttpServletRequestBuilder.contentType(isJson ? MediaType.APPLICATION_JSON : MediaType.MULTIPART_FORM_DATA);

        when(blobContainerClient.getBlobClient(any())).thenReturn(blobClient);
        when(blobContainerClient.getBlobContainerUrl()).thenReturn(BLOB_PAYLOAD_URL);

        MvcResult response =
            mockMvc.perform(mockHttpServletRequestBuilder).andExpect(status().isCreated()).andReturn();

        assertNotNull(response.getResponse().getContentAsString(), VALIDATION_EMPTY_RESPONSE);

        Artefact artefact = objectMapper.readValue(
            response.getResponse().getContentAsString(), Artefact.class);

        when(blobClient.downloadContent()).thenReturn(
            BinaryData.fromString(isJson ? payload : new String(file.getBytes())));

        mockMvc.perform(MockMvcRequestBuilders
                            .get(POST_URL + "/" + artefact.getArtefactId())
                            .header(VERIFICATION_HEADER, VERIFICATION_TRUE))

            .andExpect(status().isNotFound()).andReturn();
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    @DisplayName("Metadata endpoint should return the artefact when artefact exists")
    void retrieveMetadataOfAnArtefactWhereOutOfDateRangeAndUnverified(boolean isJson) throws Exception {
        if (isJson) {
            mockHttpServletRequestBuilder = MockMvcRequestBuilders.post(POST_URL).content(payload);
        } else {
            mockHttpServletRequestBuilder = MockMvcRequestBuilders.multipart(POST_URL).file(file);
        }
        mockHttpServletRequestBuilder.header(PublicationConfiguration.TYPE_HEADER, ARTEFACT_TYPE);
        mockHttpServletRequestBuilder.header(PublicationConfiguration.SENSITIVITY_HEADER, SENSITIVITY);
        mockHttpServletRequestBuilder.header(PublicationConfiguration.LANGUAGE_HEADER, LANGUAGE);
        mockHttpServletRequestBuilder.header(PublicationConfiguration.PROVENANCE_HEADER, PROVENANCE);
        mockHttpServletRequestBuilder.header(PublicationConfiguration.SOURCE_ARTEFACT_ID_HEADER, SOURCE_ARTEFACT_ID);
        mockHttpServletRequestBuilder.header(PublicationConfiguration.DISPLAY_TO_HEADER, DISPLAY_TO.plusMonths(2));
        mockHttpServletRequestBuilder.header(PublicationConfiguration.DISPLAY_FROM_HEADER, DISPLAY_FROM.plusMonths(1));
        mockHttpServletRequestBuilder.header(PublicationConfiguration.LIST_TYPE, LIST_TYPE);
        mockHttpServletRequestBuilder.header(PublicationConfiguration.COURT_ID, COURT_ID);
        mockHttpServletRequestBuilder.header(PublicationConfiguration.CONTENT_DATE, CONTENT_DATE);
        mockHttpServletRequestBuilder.contentType(isJson ? MediaType.APPLICATION_JSON : MediaType.MULTIPART_FORM_DATA);

        when(blobContainerClient.getBlobClient(any())).thenReturn(blobClient);
        when(blobContainerClient.getBlobContainerUrl()).thenReturn(BLOB_PAYLOAD_URL);

        MvcResult response =
            mockMvc.perform(mockHttpServletRequestBuilder).andExpect(status().isCreated()).andReturn();

        assertNotNull(response.getResponse().getContentAsString(), VALIDATION_EMPTY_RESPONSE);

        Artefact artefact = objectMapper.readValue(
            response.getResponse().getContentAsString(), Artefact.class);

        when(blobClient.downloadContent()).thenReturn(
            BinaryData.fromString(isJson ? payload : new String(file.getBytes())));

        mockMvc.perform(MockMvcRequestBuilders
                            .get(POST_URL + "/" + artefact.getArtefactId())
                            .header(VERIFICATION_HEADER, VERIFICATION_FALSE))
            .andExpect(status().isNotFound()).andReturn();
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    @DisplayName("Metadata endpoint should not return the artefact when not authorized")
    void retrieveMetadataOfAnArtefactWhereNotAuthorized(boolean isJson) throws Exception {
        if (isJson) {
            mockHttpServletRequestBuilder = MockMvcRequestBuilders.post(POST_URL).content(payload);
        } else {
            mockHttpServletRequestBuilder = MockMvcRequestBuilders.multipart(POST_URL).file(file);
        }
        mockHttpServletRequestBuilder.header(PublicationConfiguration.TYPE_HEADER, ARTEFACT_TYPE);
        mockHttpServletRequestBuilder.header(PublicationConfiguration.SENSITIVITY_HEADER, Sensitivity.CLASSIFIED);
        mockHttpServletRequestBuilder.header(PublicationConfiguration.LANGUAGE_HEADER, LANGUAGE);
        mockHttpServletRequestBuilder.header(PublicationConfiguration.PROVENANCE_HEADER, PROVENANCE);
        mockHttpServletRequestBuilder.header(PublicationConfiguration.SOURCE_ARTEFACT_ID_HEADER, SOURCE_ARTEFACT_ID);
        mockHttpServletRequestBuilder.header(PublicationConfiguration.DISPLAY_TO_HEADER, DISPLAY_TO.plusMonths(1));
        mockHttpServletRequestBuilder.header(PublicationConfiguration.DISPLAY_FROM_HEADER, DISPLAY_FROM);
        mockHttpServletRequestBuilder.header(PublicationConfiguration.LIST_TYPE, LIST_TYPE);
        mockHttpServletRequestBuilder.header(PublicationConfiguration.COURT_ID, COURT_ID);
        mockHttpServletRequestBuilder.header(PublicationConfiguration.CONTENT_DATE, CONTENT_DATE);
        mockHttpServletRequestBuilder.contentType(isJson ? MediaType.APPLICATION_JSON : MediaType.MULTIPART_FORM_DATA);

        when(blobContainerClient.getBlobClient(any())).thenReturn(blobClient);
        when(blobContainerClient.getBlobContainerUrl()).thenReturn(BLOB_PAYLOAD_URL);

        MvcResult response =
            mockMvc.perform(mockHttpServletRequestBuilder).andExpect(status().isCreated()).andReturn();

        assertNotNull(response.getResponse().getContentAsString(), VALIDATION_EMPTY_RESPONSE);

        Artefact artefact = objectMapper.readValue(
            response.getResponse().getContentAsString(), Artefact.class);

        mockMvc.perform(MockMvcRequestBuilders
                            .get(POST_URL + "/" + artefact.getArtefactId())
                            .header(VERIFICATION_HEADER, VERIFICATION_FALSE))
            .andExpect(status().isNotFound()).andReturn();
    }

    @Test
    @DisplayName("Metadata endpoint should return 404 when artefact does not exist")
    void retrieveMetadataOfAnArtefactWhereNotFound() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders
                            .get("/publication/7d734e8d-ba1d-4730-bd8b-09a970be00cc")
                            .header(VERIFICATION_HEADER, VERIFICATION_TRUE))
            .andExpect(status().isNotFound()).andReturn();
    }

    @Test
    @DisplayName("Metadata endpoint where verification not set should display a 400")
    void retrieveMetadataWhenVerificationEndpointNotSet() throws Exception {
        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders
                            .get("/publication/7d734e8d-ba1d-4730-bd8b-09a970be00cc"))
            .andExpect(status().isBadRequest()).andReturn();

        ExceptionResponse exceptionResponse =
            objectMapper.readValue(mvcResult.getResponse().getContentAsString(), ExceptionResponse.class);


        assertTrue(exceptionResponse.getMessage().contains(VERIFICATION_HEADER),
                   "Verification error not shown in error message");
    }

    @Test
    void testGetArtefactByCaseIdSearchVerified() throws Exception {
        when(blobContainerClient.getBlobClient(any())).thenReturn(blobClient);
        when(blobContainerClient.getBlobContainerUrl()).thenReturn(PAYLOAD_URL);

        Artefact artefact = createDailyList(Sensitivity.PRIVATE);

        MockHttpServletRequestBuilder mockHttpServletRequestBuilder1 =
            MockMvcRequestBuilders.get(SEARCH_URL + VALID_CASE_ID_SEARCH);

        mockHttpServletRequestBuilder1
            .header(VERIFICATION_HEADER, TRUE);

        MvcResult getResponse =
            mockMvc.perform(mockHttpServletRequestBuilder1).andExpect(status().isOk()).andReturn();

        assertTrue(
            getResponse.getResponse().getContentAsString().contains(artefact.getArtefactId().toString()),
            SHOULD_RETURN_EXPECTED_ARTEFACT
        );
    }

    @Test
    void testGetArtefactByCaseIdSearchUnverified() throws Exception {
        when(blobContainerClient.getBlobClient(any())).thenReturn(blobClient);
        when(blobContainerClient.getBlobContainerUrl()).thenReturn(PAYLOAD_URL);

        Artefact artefact = createDailyList(Sensitivity.PUBLIC);

        MockHttpServletRequestBuilder mockHttpServletRequestBuilder1 =
            MockMvcRequestBuilders.get(SEARCH_URL + VALID_CASE_ID_SEARCH);

        mockHttpServletRequestBuilder1
            .header(VERIFICATION_HEADER, FALSE);

        MvcResult getResponse =
            mockMvc.perform(mockHttpServletRequestBuilder1).andExpect(status().isOk()).andReturn();

        assertTrue(
            getResponse.getResponse().getContentAsString().contains(artefact.getArtefactId().toString()),
            "SHOULD_RETURN_EXPECTED_ARTEFACT");
    }

    @Test
    void testGetArtefactByCaseIdSearchUnverifiedNotFound() throws Exception {
        when(blobContainerClient.getBlobClient(any())).thenReturn(blobClient);
        when(blobContainerClient.getBlobContainerUrl()).thenReturn(PAYLOAD_URL);

        createDailyList(Sensitivity.CLASSIFIED);

        MockHttpServletRequestBuilder mockHttpServletRequestBuilder1 =
            MockMvcRequestBuilders.get(SEARCH_URL + VALID_CASE_ID_SEARCH);

        mockHttpServletRequestBuilder1
            .header(VERIFICATION_HEADER, FALSE);

        mockMvc.perform(mockHttpServletRequestBuilder1).andExpect(status().isNotFound()).andReturn();

    }

    @Test
    void testGetArtefactByCaseNameSearchVerified() throws Exception {
        when(blobContainerClient.getBlobClient(any())).thenReturn(blobClient);
        when(blobContainerClient.getBlobContainerUrl()).thenReturn(PAYLOAD_URL);

        Artefact artefact = createDailyList(Sensitivity.PRIVATE);

        MockHttpServletRequestBuilder mockHttpServletRequestBuilder1 =
            MockMvcRequestBuilders.get(SEARCH_URL + VALID_CASE_NAME_SEARCH);

        mockHttpServletRequestBuilder1
            .header(VERIFICATION_HEADER, TRUE);

        MvcResult getResponse =
            mockMvc.perform(mockHttpServletRequestBuilder1).andExpect(status().isOk()).andReturn();

        assertTrue(
            getResponse.getResponse().getContentAsString().contains(artefact.getArtefactId().toString()),
            "SHOULD_RETURN_EXPECTED_ARTEFACT");
    }

    @Test
    void testGetArtefactByCaseNameSearchUnverified() throws Exception {
        when(blobContainerClient.getBlobClient(any())).thenReturn(blobClient);
        when(blobContainerClient.getBlobContainerUrl()).thenReturn(PAYLOAD_URL);

        Artefact artefact = createDailyList(Sensitivity.PUBLIC);

        MockHttpServletRequestBuilder mockHttpServletRequestBuilder1 =
            MockMvcRequestBuilders.get(SEARCH_URL + VALID_CASE_NAME_SEARCH);

        mockHttpServletRequestBuilder1
            .header(VERIFICATION_HEADER, FALSE);

        MvcResult getResponse =
            mockMvc.perform(mockHttpServletRequestBuilder1).andExpect(status().isOk()).andReturn();

        assertTrue(
            getResponse.getResponse().getContentAsString().contains(artefact.getArtefactId().toString()),
            "SHOULD_RETURN_EXPECTED_ARTEFACT");
    }

    @Test
    void testGetArtefactByCaseNameSearchUnverifiedNotFound() throws Exception {
        when(blobContainerClient.getBlobClient(any())).thenReturn(blobClient);
        when(blobContainerClient.getBlobContainerUrl()).thenReturn(PAYLOAD_URL);

        createDailyList(Sensitivity.CLASSIFIED);

        MockHttpServletRequestBuilder mockHttpServletRequestBuilder1 =
            MockMvcRequestBuilders.get(SEARCH_URL + VALID_CASE_NAME_SEARCH);

        mockHttpServletRequestBuilder1
            .header(VERIFICATION_HEADER, FALSE);

        mockMvc.perform(mockHttpServletRequestBuilder1).andExpect(status().isNotFound()).andReturn();
    }

}

