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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = {Application.class},
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles(profiles = "functional")
@AutoConfigureEmbeddedDatabase(type = AutoConfigureEmbeddedDatabase.DatabaseType.POSTGRES)
@WithMockUser(username = "admin", authorities = { "APPROLE_api.request.admin" })
class PublicationInvalidHeadersTest {

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
    private static final String TEST_VALUE = "test";

    private static final String FORMAT_RESPONSE = "Please check that the value is of the correct format for the field "
        + "(See Swagger documentation for correct formats)";

    private static final String VALIDATION_EMPTY_RESPONSE = "Response should contain a Artefact";
    private static final String VALIDATION_EXCEPTION_RESPONSE = "Exception response does not contain correct message";

    private static ObjectMapper objectMapper;

    @SuppressWarnings("PMD.LooseCoupling")
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

    @DisplayName("Should return a 400 Bad Request if an invalid artefact type is provided")
    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void testInvalidArtefactType(boolean isJson) throws Exception {
        MockHttpServletRequestBuilder mockHttpServletRequestBuilder;
        if (isJson) {
            mockHttpServletRequestBuilder = MockMvcRequestBuilders.post(PUBLICATION_URL).content(payload);
        } else {
            mockHttpServletRequestBuilder = MockMvcRequestBuilders.multipart(PUBLICATION_URL).file(file);
        }

        httpHeaders.set(PublicationConfiguration.TYPE_HEADER, TEST_VALUE);
        mockHttpServletRequestBuilder.headers(httpHeaders);
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

    @DisplayName("Should return a 400 Bad Request if an invalid display to is provided")
    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void testInvalidDisplayTo(boolean isJson) throws Exception {
        MockHttpServletRequestBuilder mockHttpServletRequestBuilder;
        if (isJson) {
            mockHttpServletRequestBuilder = MockMvcRequestBuilders.post(PUBLICATION_URL).content(payload);
        } else {
            mockHttpServletRequestBuilder = MockMvcRequestBuilders.multipart(PUBLICATION_URL).file(file);
        }

        httpHeaders.set(PublicationConfiguration.DISPLAY_TO_HEADER, TEST_VALUE);
        mockHttpServletRequestBuilder.headers(httpHeaders);
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
        MockHttpServletRequestBuilder mockHttpServletRequestBuilder;
        if (isJson) {
            mockHttpServletRequestBuilder = MockMvcRequestBuilders.post(PUBLICATION_URL).content(payload);
        } else {
            mockHttpServletRequestBuilder = MockMvcRequestBuilders.multipart(PUBLICATION_URL).file(file);
        }

        httpHeaders.set(PublicationConfiguration.DISPLAY_FROM_HEADER, TEST_VALUE);
        mockHttpServletRequestBuilder.headers(httpHeaders);
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

    @DisplayName("Should return a 400 Bad Request if an invalid content date is provided")
    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void testInvalidContentDate(boolean isJson) throws Exception {
        MockHttpServletRequestBuilder mockHttpServletRequestBuilder;
        if (isJson) {
            mockHttpServletRequestBuilder = MockMvcRequestBuilders.post(PUBLICATION_URL).content(payload);
        } else {
            mockHttpServletRequestBuilder = MockMvcRequestBuilders.multipart(PUBLICATION_URL).file(file);
        }

        httpHeaders.set(PublicationConfiguration.CONTENT_DATE, TEST_VALUE);
        mockHttpServletRequestBuilder.headers(httpHeaders);
        mockHttpServletRequestBuilder.contentType(isJson ? MediaType.APPLICATION_JSON : MediaType.MULTIPART_FORM_DATA);

        MvcResult response = mockMvc.perform(mockHttpServletRequestBuilder)
            .andExpect(status().isBadRequest()).andReturn();

        assertFalse(response.getResponse().getContentAsString().isEmpty(), VALIDATION_EMPTY_RESPONSE);
        ExceptionResponse exceptionResponse = objectMapper.readValue(
            response.getResponse().getContentAsString(),
            ExceptionResponse.class
        );

        assertTrue(exceptionResponse.getMessage().contains(
            String.format("Unable to parse x-content-date. %s", FORMAT_RESPONSE)), VALIDATION_EXCEPTION_RESPONSE);
    }

    @DisplayName("Should return a 400 Bad Request if an invalid language is provided")
    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void testInvalidLanguageHeader(boolean isJson) throws Exception {
        MockHttpServletRequestBuilder mockHttpServletRequestBuilder;
        if (isJson) {
            mockHttpServletRequestBuilder = MockMvcRequestBuilders.post(PUBLICATION_URL).content(payload);
        } else {
            mockHttpServletRequestBuilder = MockMvcRequestBuilders.multipart(PUBLICATION_URL).file(file);
        }

        httpHeaders.set(PublicationConfiguration.LANGUAGE_HEADER, TEST_VALUE);
        mockHttpServletRequestBuilder.headers(httpHeaders);
        mockHttpServletRequestBuilder.contentType(isJson ? MediaType.APPLICATION_JSON : MediaType.MULTIPART_FORM_DATA);
        mockHttpServletRequestBuilder.content(payload);

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
        MockHttpServletRequestBuilder mockHttpServletRequestBuilder;
        if (isJson) {
            mockHttpServletRequestBuilder = MockMvcRequestBuilders.post(PUBLICATION_URL).content(payload);
        } else {
            mockHttpServletRequestBuilder = MockMvcRequestBuilders.multipart(PUBLICATION_URL).file(file);
        }

        httpHeaders.set(PublicationConfiguration.SENSITIVITY_HEADER, TEST_VALUE);
        mockHttpServletRequestBuilder.headers(httpHeaders);
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

}
