package uk.gov.hmcts.reform.pip.data.management.controllers.headers;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.zonky.test.db.AutoConfigureEmbeddedDatabase;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
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
import uk.gov.hmcts.reform.pip.data.management.errorhandling.ExceptionResponse;
import uk.gov.hmcts.reform.pip.model.publication.ArtefactType;
import uk.gov.hmcts.reform.pip.model.publication.Language;
import uk.gov.hmcts.reform.pip.model.publication.ListType;
import uk.gov.hmcts.reform.pip.model.publication.Sensitivity;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = {Application.class},
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles(profiles = "functional")
@AutoConfigureEmbeddedDatabase(type = AutoConfigureEmbeddedDatabase.DatabaseType.POSTGRES)
@WithMockUser(username = "admin", authorities = { "APPROLE_api.request.admin" })
@SuppressWarnings("PMD.LawOfDemeter")
class PublicationMissingHeadersTest {

    @Autowired
    private MockMvc mockMvc;

    private static final String PUBLICATION_URL = "/publication";
    private static String payload = "payload";
    private static MockMultipartFile file;
    private static final String SOURCE_ARTEFACT_ID = "sourceArtefactId";
    private static final ArtefactType ARTEFACT_TYPE = ArtefactType.LIST;
    private static final Language LANGUAGE = Language.ENGLISH;
    private static final Sensitivity SENSITIVITY = Sensitivity.PUBLIC;
    private static final String PROVENANCE = "provenance";
    private static final LocalDateTime DISPLAY_TO = LocalDateTime.now();
    private static final LocalDateTime DISPLAY_FROM = LocalDateTime.now();
    private static final ListType LIST_TYPE = ListType.CIVIL_DAILY_CAUSE_LIST;
    private static final String COURT_ID = "123";
    private static final LocalDateTime CONTENT_DATE = LocalDateTime.now();
    private static final String EMPTY_VALUE = "";

    private static final String VALIDATION_EMPTY_RESPONSE = "Response should contain a Artefact";
    private static final String VALIDATION_EXCEPTION_RESPONSE = "Exception response does not contain correct message";

    private static MockHttpServletRequestBuilder mockHttpServletRequestBuilder;
    private static ObjectMapper objectMapper;

    HttpHeaders httpHeaders;

    @BeforeAll
    public static void setup() {
        file = new MockMultipartFile("file", "test.pdf", MediaType.APPLICATION_PDF_VALUE, "test content".getBytes(
            StandardCharsets.UTF_8));

        objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();
    }

    @BeforeEach
    public void beforeEach() {
        httpHeaders = new HttpHeaders();
        httpHeaders.add(PublicationConfiguration.PROVENANCE_HEADER, PROVENANCE);
        httpHeaders.add(PublicationConfiguration.SENSITIVITY_HEADER, SENSITIVITY.name());
        httpHeaders.add(PublicationConfiguration.TYPE_HEADER, ARTEFACT_TYPE.name());
        httpHeaders.add(PublicationConfiguration.SOURCE_ARTEFACT_ID_HEADER, SOURCE_ARTEFACT_ID);
        httpHeaders.add(PublicationConfiguration.DISPLAY_TO_HEADER, DISPLAY_TO.toString());
        httpHeaders.add(PublicationConfiguration.DISPLAY_FROM_HEADER, DISPLAY_FROM.toString());
        httpHeaders.add(PublicationConfiguration.LANGUAGE_HEADER, LANGUAGE.toString());
        httpHeaders.add(PublicationConfiguration.LIST_TYPE, LIST_TYPE.toString());
        httpHeaders.add(PublicationConfiguration.COURT_ID, COURT_ID);
        httpHeaders.add(PublicationConfiguration.CONTENT_DATE, CONTENT_DATE.toString());
    }

    @DisplayName("Should return a 400 Bad Request if the provenance header is empty")
    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void testEmptyProvenance(boolean isJson) throws Exception {
        if (isJson) {
            mockHttpServletRequestBuilder = MockMvcRequestBuilders.post(PUBLICATION_URL).content(payload);
        } else {
            mockHttpServletRequestBuilder = MockMvcRequestBuilders.multipart(PUBLICATION_URL).file(file);
        }

        httpHeaders.set(PublicationConfiguration.PROVENANCE_HEADER, EMPTY_VALUE);
        mockHttpServletRequestBuilder.headers(httpHeaders);
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

    @DisplayName("Should return a 400 Bad Request if the provenance header is missing")
    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void testMissingProvenance(boolean isJson) throws Exception {
        if (isJson) {
            mockHttpServletRequestBuilder = MockMvcRequestBuilders.post(PUBLICATION_URL).content(payload);
        } else {
            mockHttpServletRequestBuilder = MockMvcRequestBuilders.multipart(PUBLICATION_URL).file(file);
        }

        httpHeaders.remove(PublicationConfiguration.PROVENANCE_HEADER);
        mockHttpServletRequestBuilder.headers(httpHeaders);
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

    @DisplayName("Should return a 400 Bad Request if the language header is empty")
    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void testEmptyLanguage(boolean isJson) throws Exception {
        if (isJson) {
            mockHttpServletRequestBuilder = MockMvcRequestBuilders.post(PUBLICATION_URL).content(payload);
        } else {
            mockHttpServletRequestBuilder = MockMvcRequestBuilders.multipart(PUBLICATION_URL).file(file);
        }

        httpHeaders.set(PublicationConfiguration.LANGUAGE_HEADER, EMPTY_VALUE);
        mockHttpServletRequestBuilder.headers(httpHeaders);
        mockHttpServletRequestBuilder.contentType(isJson ? MediaType.APPLICATION_JSON : MediaType.MULTIPART_FORM_DATA);

        MvcResult response = mockMvc.perform(mockHttpServletRequestBuilder)
            .andExpect(status().isBadRequest()).andReturn();

        assertFalse(response.getResponse().getContentAsString().isEmpty(), VALIDATION_EMPTY_RESPONSE);
        ExceptionResponse exceptionResponse = objectMapper.readValue(
            response.getResponse().getContentAsString(),
            ExceptionResponse.class
        );

        assertTrue(exceptionResponse.getMessage().contains("x-language"), VALIDATION_EXCEPTION_RESPONSE);
    }

    @DisplayName("Should return a 400 Bad Request if the language header is missing")
    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void testMissingLanguage(boolean isJson) throws Exception {
        if (isJson) {
            mockHttpServletRequestBuilder = MockMvcRequestBuilders.post(PUBLICATION_URL).content(payload);
        } else {
            mockHttpServletRequestBuilder = MockMvcRequestBuilders.multipart(PUBLICATION_URL).file(file);
        }

        httpHeaders.remove(PublicationConfiguration.LANGUAGE_HEADER);
        mockHttpServletRequestBuilder.headers(httpHeaders);
        mockHttpServletRequestBuilder.contentType(isJson ? MediaType.APPLICATION_JSON : MediaType.MULTIPART_FORM_DATA);

        MvcResult response = mockMvc.perform(mockHttpServletRequestBuilder)
            .andExpect(status().isBadRequest()).andReturn();

        assertFalse(response.getResponse().getContentAsString().isEmpty(), VALIDATION_EMPTY_RESPONSE);
        ExceptionResponse exceptionResponse = objectMapper.readValue(
            response.getResponse().getContentAsString(),
            ExceptionResponse.class
        );

        assertTrue(exceptionResponse.getMessage().contains("x-language"), VALIDATION_EXCEPTION_RESPONSE);
    }

    @DisplayName("Should return a 400 Bad Request if the artifact type header is empty")
    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void testEmptyArtifactType(boolean isJson) throws Exception {
        if (isJson) {
            mockHttpServletRequestBuilder = MockMvcRequestBuilders.post(PUBLICATION_URL).content(payload);
        } else {
            mockHttpServletRequestBuilder = MockMvcRequestBuilders.multipart(PUBLICATION_URL).file(file);
        }

        httpHeaders.set(PublicationConfiguration.TYPE_HEADER, EMPTY_VALUE);
        mockHttpServletRequestBuilder.headers(httpHeaders);
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

    @DisplayName("Should return a 400 Bad Request if the artefact type header is missing")
    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void testMissingArtefactType(boolean isJson) throws Exception {
        if (isJson) {
            mockHttpServletRequestBuilder = MockMvcRequestBuilders.post(PUBLICATION_URL).content(payload);
        } else {
            mockHttpServletRequestBuilder = MockMvcRequestBuilders.multipart(PUBLICATION_URL).file(file);
        }

        httpHeaders.remove(PublicationConfiguration.TYPE_HEADER);
        mockHttpServletRequestBuilder.headers(httpHeaders);
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

    @DisplayName("Should return a 400 Bad Request if the list type header is empty")
    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void testEmptyListType(boolean isJson) throws Exception {
        if (isJson) {
            mockHttpServletRequestBuilder = MockMvcRequestBuilders.post(PUBLICATION_URL).content(payload);
        } else {
            mockHttpServletRequestBuilder = MockMvcRequestBuilders.multipart(PUBLICATION_URL).file(file);
        }

        httpHeaders.set(PublicationConfiguration.LIST_TYPE, EMPTY_VALUE);
        mockHttpServletRequestBuilder.headers(httpHeaders);
        mockHttpServletRequestBuilder.contentType(isJson ? MediaType.APPLICATION_JSON : MediaType.MULTIPART_FORM_DATA);

        MvcResult response = mockMvc.perform(mockHttpServletRequestBuilder)
            .andExpect(status().isBadRequest()).andReturn();

        assertFalse(response.getResponse().getContentAsString().isEmpty(), VALIDATION_EMPTY_RESPONSE);
        ExceptionResponse exceptionResponse = objectMapper.readValue(
            response.getResponse().getContentAsString(),
            ExceptionResponse.class
        );

        assertTrue(exceptionResponse.getMessage().contains("x-list-type"), VALIDATION_EXCEPTION_RESPONSE);
    }

    @DisplayName("Should return a 400 Bad Request if the list type header is missing")
    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void testMissingListType(boolean isJson) throws Exception {
        if (isJson) {
            mockHttpServletRequestBuilder = MockMvcRequestBuilders.post(PUBLICATION_URL).content(payload);
        } else {
            mockHttpServletRequestBuilder = MockMvcRequestBuilders.multipart(PUBLICATION_URL).file(file);
        }

        httpHeaders.remove(PublicationConfiguration.LIST_TYPE);
        mockHttpServletRequestBuilder.headers(httpHeaders);
        mockHttpServletRequestBuilder.contentType(isJson ? MediaType.APPLICATION_JSON : MediaType.MULTIPART_FORM_DATA);

        MvcResult response = mockMvc.perform(mockHttpServletRequestBuilder)
            .andExpect(status().isBadRequest()).andReturn();

        assertFalse(response.getResponse().getContentAsString().isEmpty(), VALIDATION_EMPTY_RESPONSE);
        ExceptionResponse exceptionResponse = objectMapper.readValue(
            response.getResponse().getContentAsString(),
            ExceptionResponse.class
        );

        assertTrue(exceptionResponse.getMessage().contains("x-list-type"), VALIDATION_EXCEPTION_RESPONSE);
    }

    @DisplayName("Should return a 400 Bad Request if the content date header is empty")
    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void testEmptyContentDate(boolean isJson) throws Exception {
        if (isJson) {
            mockHttpServletRequestBuilder = MockMvcRequestBuilders.post(PUBLICATION_URL).content(payload);
        } else {
            mockHttpServletRequestBuilder = MockMvcRequestBuilders.multipart(PUBLICATION_URL).file(file);
        }

        httpHeaders.set(PublicationConfiguration.CONTENT_DATE, EMPTY_VALUE);
        mockHttpServletRequestBuilder.headers(httpHeaders);
        mockHttpServletRequestBuilder.contentType(isJson ? MediaType.APPLICATION_JSON : MediaType.MULTIPART_FORM_DATA);

        MvcResult response = mockMvc.perform(mockHttpServletRequestBuilder)
            .andExpect(status().isBadRequest()).andReturn();

        assertFalse(response.getResponse().getContentAsString().isEmpty(), VALIDATION_EMPTY_RESPONSE);
        ExceptionResponse exceptionResponse = objectMapper.readValue(
            response.getResponse().getContentAsString(),
            ExceptionResponse.class
        );

        assertTrue(exceptionResponse.getMessage().contains("x-content-date"), VALIDATION_EXCEPTION_RESPONSE);
    }

    @DisplayName("Should return a 400 Bad Request if the content date header is missing")
    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void testMissingContentDate(boolean isJson) throws Exception {
        if (isJson) {
            mockHttpServletRequestBuilder = MockMvcRequestBuilders.post(PUBLICATION_URL).content(payload);
        } else {
            mockHttpServletRequestBuilder = MockMvcRequestBuilders.multipart(PUBLICATION_URL).file(file);
        }

        httpHeaders.remove(PublicationConfiguration.CONTENT_DATE);
        mockHttpServletRequestBuilder.headers(httpHeaders);
        mockHttpServletRequestBuilder.contentType(isJson ? MediaType.APPLICATION_JSON : MediaType.MULTIPART_FORM_DATA);

        MvcResult response = mockMvc.perform(mockHttpServletRequestBuilder)
            .andExpect(status().isBadRequest()).andReturn();

        assertFalse(response.getResponse().getContentAsString().isEmpty(), VALIDATION_EMPTY_RESPONSE);
        ExceptionResponse exceptionResponse = objectMapper.readValue(
            response.getResponse().getContentAsString(),
            ExceptionResponse.class
        );

        assertTrue(exceptionResponse.getMessage().contains("x-content-date"), VALIDATION_EXCEPTION_RESPONSE);
    }

    @DisplayName("Should return a 400 Bad Request if the court id header is empty")
    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void testEmptyCourtId(boolean isJson) throws Exception {
        if (isJson) {
            mockHttpServletRequestBuilder = MockMvcRequestBuilders.post(PUBLICATION_URL).content(payload);
        } else {
            mockHttpServletRequestBuilder = MockMvcRequestBuilders.multipart(PUBLICATION_URL).file(file);
        }

        httpHeaders.set(PublicationConfiguration.COURT_ID, EMPTY_VALUE);
        mockHttpServletRequestBuilder.headers(httpHeaders);
        mockHttpServletRequestBuilder.contentType(isJson ? MediaType.APPLICATION_JSON : MediaType.MULTIPART_FORM_DATA);

        MvcResult response = mockMvc.perform(mockHttpServletRequestBuilder)
            .andExpect(status().isBadRequest()).andReturn();

        assertFalse(response.getResponse().getContentAsString().isEmpty(), VALIDATION_EMPTY_RESPONSE);
        ExceptionResponse exceptionResponse = objectMapper.readValue(
            response.getResponse().getContentAsString(),
            ExceptionResponse.class
        );

        assertTrue(exceptionResponse.getMessage().contains("x-court-id"), VALIDATION_EXCEPTION_RESPONSE);
    }

    @DisplayName("Should return a 400 Bad Request if the court id header is missing")
    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void testMissingCourtId(boolean isJson) throws Exception {
        if (isJson) {
            mockHttpServletRequestBuilder = MockMvcRequestBuilders.post(PUBLICATION_URL).content(payload);
        } else {
            mockHttpServletRequestBuilder = MockMvcRequestBuilders.multipart(PUBLICATION_URL).file(file);
        }

        httpHeaders.remove(PublicationConfiguration.COURT_ID);
        mockHttpServletRequestBuilder.headers(httpHeaders);
        mockHttpServletRequestBuilder.contentType(isJson ? MediaType.APPLICATION_JSON : MediaType.MULTIPART_FORM_DATA);

        MvcResult response = mockMvc.perform(mockHttpServletRequestBuilder)
            .andExpect(status().isBadRequest()).andReturn();

        assertFalse(response.getResponse().getContentAsString().isEmpty(), VALIDATION_EMPTY_RESPONSE);
        ExceptionResponse exceptionResponse = objectMapper.readValue(
            response.getResponse().getContentAsString(),
            ExceptionResponse.class
        );

        assertTrue(exceptionResponse.getMessage().contains("x-court-id"), VALIDATION_EXCEPTION_RESPONSE);
    }

    @DisplayName("Should return a 400 bad request if artefact type is judgement and date to is empty")
    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void testDateToAbsenceJudgement(boolean isJson) throws Exception {
        if (isJson) {
            mockHttpServletRequestBuilder = MockMvcRequestBuilders.post(PUBLICATION_URL).content(payload);
        } else {
            mockHttpServletRequestBuilder = MockMvcRequestBuilders.multipart(PUBLICATION_URL).file(file);
        }

        httpHeaders.set(PublicationConfiguration.TYPE_HEADER, ArtefactType.JUDGEMENTS_AND_OUTCOMES.name());
        httpHeaders.remove(PublicationConfiguration.DISPLAY_TO_HEADER);
        mockHttpServletRequestBuilder.headers(httpHeaders);
        mockHttpServletRequestBuilder.contentType(isJson ? MediaType.APPLICATION_JSON : MediaType.MULTIPART_FORM_DATA);

        MvcResult response = mockMvc.perform(mockHttpServletRequestBuilder)
            .andExpect(status().isBadRequest()).andReturn();
        assertFalse(response.getResponse().getContentAsString().isEmpty(), VALIDATION_EMPTY_RESPONSE);
        ExceptionResponse exceptionResponse = objectMapper.readValue(
            response.getResponse().getContentAsString(),
            ExceptionResponse.class
        );

        assertTrue(exceptionResponse.getMessage().contains("x-display-to"), VALIDATION_EXCEPTION_RESPONSE);
    }

    @DisplayName("Should return a 400 bad request if artefact type is list and date to is empty")
    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void testDateToAbsenceList(boolean isJson) throws Exception {
        if (isJson) {
            mockHttpServletRequestBuilder = MockMvcRequestBuilders.post(PUBLICATION_URL).content(payload);
        } else {
            mockHttpServletRequestBuilder = MockMvcRequestBuilders.multipart(PUBLICATION_URL).file(file);
        }

        httpHeaders.remove(PublicationConfiguration.DISPLAY_TO_HEADER);
        mockHttpServletRequestBuilder.headers(httpHeaders);
        mockHttpServletRequestBuilder.contentType(isJson ? MediaType.APPLICATION_JSON : MediaType.MULTIPART_FORM_DATA);

        MvcResult response = mockMvc.perform(mockHttpServletRequestBuilder)
            .andExpect(status().isBadRequest()).andReturn();
        assertFalse(response.getResponse().getContentAsString().isEmpty(), VALIDATION_EMPTY_RESPONSE);
        ExceptionResponse exceptionResponse = objectMapper.readValue(
            response.getResponse().getContentAsString(),
            ExceptionResponse.class
        );

        assertTrue(exceptionResponse.getMessage().contains("x-display-to"), VALIDATION_EXCEPTION_RESPONSE);
    }

    @DisplayName("Should return a 400 bad request if artefact type is judgement and date from is empty")
    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void testDateFromAbsenceJudgement(boolean isJson) throws Exception {
        if (isJson) {
            mockHttpServletRequestBuilder = MockMvcRequestBuilders.post(PUBLICATION_URL).content(payload);
        } else {
            mockHttpServletRequestBuilder = MockMvcRequestBuilders.multipart(PUBLICATION_URL).file(file);
        }

        httpHeaders.set(PublicationConfiguration.TYPE_HEADER, ArtefactType.JUDGEMENTS_AND_OUTCOMES.name());
        httpHeaders.remove(PublicationConfiguration.DISPLAY_FROM_HEADER);
        mockHttpServletRequestBuilder.headers(httpHeaders);
        mockHttpServletRequestBuilder.contentType(isJson ? MediaType.APPLICATION_JSON : MediaType.MULTIPART_FORM_DATA);

        MvcResult response = mockMvc.perform(mockHttpServletRequestBuilder)
            .andExpect(status().isBadRequest()).andReturn();
        assertFalse(response.getResponse().getContentAsString().isEmpty(), VALIDATION_EMPTY_RESPONSE);
        ExceptionResponse exceptionResponse = objectMapper.readValue(
            response.getResponse().getContentAsString(),
            ExceptionResponse.class
        );

        assertTrue(exceptionResponse.getMessage().contains("x-display-from"), VALIDATION_EXCEPTION_RESPONSE);
    }

    @DisplayName("Should return a 400 bad request if artefact type is list and date from is empty")
    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void testDateFromAbsenceList(boolean isJson) throws Exception {
        if (isJson) {
            mockHttpServletRequestBuilder = MockMvcRequestBuilders.post(PUBLICATION_URL).content(payload);
        } else {
            mockHttpServletRequestBuilder = MockMvcRequestBuilders.multipart(PUBLICATION_URL).file(file);
        }

        httpHeaders.remove(PublicationConfiguration.DISPLAY_FROM_HEADER);
        mockHttpServletRequestBuilder.headers(httpHeaders);
        mockHttpServletRequestBuilder.contentType(isJson ? MediaType.APPLICATION_JSON : MediaType.MULTIPART_FORM_DATA);

        MvcResult response = mockMvc.perform(mockHttpServletRequestBuilder)
            .andExpect(status().isBadRequest()).andReturn();
        assertFalse(response.getResponse().getContentAsString().isEmpty(), VALIDATION_EMPTY_RESPONSE);
        ExceptionResponse exceptionResponse = objectMapper.readValue(
            response.getResponse().getContentAsString(),
            ExceptionResponse.class
        );

        assertTrue(exceptionResponse.getMessage().contains("x-display-from"), VALIDATION_EXCEPTION_RESPONSE);
    }

}
