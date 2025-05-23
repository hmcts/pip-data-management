package uk.gov.hmcts.reform.pip.data.management.controllers.headers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import uk.gov.hmcts.reform.pip.data.management.config.PublicationConfiguration;
import uk.gov.hmcts.reform.pip.data.management.errorhandling.ExceptionResponse;
import uk.gov.hmcts.reform.pip.data.management.service.AccountManagementService;
import uk.gov.hmcts.reform.pip.data.management.utils.IntegrationBasicTestBase;
import uk.gov.hmcts.reform.pip.model.account.PiUser;
import uk.gov.hmcts.reform.pip.model.publication.ArtefactType;
import uk.gov.hmcts.reform.pip.model.publication.Language;
import uk.gov.hmcts.reform.pip.model.publication.ListType;
import uk.gov.hmcts.reform.pip.model.publication.Sensitivity;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.UUID;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.pip.model.account.Roles.SYSTEM_ADMIN;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("integration-basic")
@WithMockUser(username = "admin", authorities = { "APPROLE_api.request.admin" })
@SuppressWarnings({"PMD.AvoidFieldNameMatchingMethodName", "PMD.ExcessiveImports"})
class PublicationMissingHeadersTest extends IntegrationBasicTestBase {

    @Autowired
    private MockMvc mockMvc;

    private static final String PUBLICATION_URL = "/publication";
    private static final String NON_STRATEGIC_PUBLICATION_URL = PUBLICATION_URL + "/non-strategic";
    private static String payload = "payload";
    private static MockMultipartFile file;
    private static MockMultipartFile excelFile;
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
    private static final String EXCEL_FILE_TYPE = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
    private static final String PARAMETERS = "parameters";

    private static final String VALIDATION_EMPTY_RESPONSE = "Response should contain a Artefact";
    private static final String VALIDATION_EXCEPTION_RESPONSE = "Exception response does not contain correct message";

    private static final String SYSTEM_ADMIN_ID = UUID.randomUUID().toString();

    private static ObjectMapper objectMapper;
    private static PiUser piUser;

    @MockitoBean
    protected AccountManagementService accountManagementService;

    @SuppressWarnings("PMD.LooseCoupling")
    HttpHeaders httpHeaders;

    @BeforeAll
    public static void setup() throws IOException {
        piUser = new PiUser();
        piUser.setUserId(SYSTEM_ADMIN_ID);
        piUser.setEmail("test@justice.gov.uk");
        piUser.setRoles(SYSTEM_ADMIN);

        file = new MockMultipartFile("file", "test.pdf", MediaType.APPLICATION_PDF_VALUE, "test content".getBytes(
            StandardCharsets.UTF_8));
        excelFile = createExcelMultipartFile();

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
        httpHeaders.add(PublicationConfiguration.COURT_ID, COURT_ID);
        httpHeaders.add(PublicationConfiguration.CONTENT_DATE, CONTENT_DATE.toString());
        httpHeaders.add(PublicationConfiguration.REQUESTER_ID_HEADER, SYSTEM_ADMIN_ID);
    }

    private static MockMultipartFile createExcelMultipartFile() throws IOException {
        try (InputStream inputStream = PublicationInvalidHeadersTest.class.getClassLoader()
            .getResourceAsStream("data/non-strategic/cst-weekly-hearing-list/cstWeeklyHearingList.xlsx")) {
            return new MockMultipartFile(
                "file", "TestFileName.xlsx", EXCEL_FILE_TYPE,
                org.testcontainers.shaded.org.apache.commons.io.IOUtils.toByteArray(inputStream)
            );
        }
    }

    @DisplayName("Should return a 400 Bad Request if the provenance header is empty")
    @ParameterizedTest
    @MethodSource(PARAMETERS)
    void testEmptyProvenance(String path, Object content, MediaType mediaType, ListType listType) throws Exception {
        when(accountManagementService.getUserById(any())).thenReturn(piUser);
        MockHttpServletRequestBuilder mockHttpServletRequestBuilder = MediaType.APPLICATION_JSON.equals(mediaType)
            ? MockMvcRequestBuilders.post(path).content((String) content)
            : MockMvcRequestBuilders.multipart(path).file((MockMultipartFile) content);

        httpHeaders.add(PublicationConfiguration.LIST_TYPE, listType.toString());
        httpHeaders.set(PublicationConfiguration.PROVENANCE_HEADER, EMPTY_VALUE);
        mockHttpServletRequestBuilder.headers(httpHeaders);
        mockHttpServletRequestBuilder.contentType(mediaType);

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
    @MethodSource(PARAMETERS)
    void testMissingProvenance(String path, Object content, MediaType mediaType, ListType listType) throws Exception {
        MockHttpServletRequestBuilder mockHttpServletRequestBuilder = MediaType.APPLICATION_JSON.equals(mediaType)
            ? MockMvcRequestBuilders.post(path).content((String) content)
            : MockMvcRequestBuilders.multipart(path).file((MockMultipartFile) content);

        httpHeaders.add(PublicationConfiguration.LIST_TYPE, listType.toString());
        httpHeaders.remove(PublicationConfiguration.PROVENANCE_HEADER);
        mockHttpServletRequestBuilder.headers(httpHeaders);
        mockHttpServletRequestBuilder.contentType(mediaType);

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
    @MethodSource(PARAMETERS)
    void testEmptyLanguage(String path, Object content, MediaType mediaType, ListType listType) throws Exception {
        MockHttpServletRequestBuilder mockHttpServletRequestBuilder = MediaType.APPLICATION_JSON.equals(mediaType)
            ? MockMvcRequestBuilders.post(path).content((String) content)
            : MockMvcRequestBuilders.multipart(path).file((MockMultipartFile) content);

        httpHeaders.add(PublicationConfiguration.LIST_TYPE, listType.toString());
        httpHeaders.set(PublicationConfiguration.LANGUAGE_HEADER, EMPTY_VALUE);
        mockHttpServletRequestBuilder.headers(httpHeaders);
        mockHttpServletRequestBuilder.contentType(mediaType);

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
    @MethodSource(PARAMETERS)
    void testMissingLanguage(String path, Object content, MediaType mediaType, ListType listType) throws Exception {
        MockHttpServletRequestBuilder mockHttpServletRequestBuilder = MediaType.APPLICATION_JSON.equals(mediaType)
            ? MockMvcRequestBuilders.post(path).content((String) content)
            : MockMvcRequestBuilders.multipart(path).file((MockMultipartFile) content);

        httpHeaders.add(PublicationConfiguration.LIST_TYPE, listType.toString());
        httpHeaders.remove(PublicationConfiguration.LANGUAGE_HEADER);
        mockHttpServletRequestBuilder.headers(httpHeaders);
        mockHttpServletRequestBuilder.contentType(mediaType);

        MvcResult response = mockMvc.perform(mockHttpServletRequestBuilder)
            .andExpect(status().isBadRequest()).andReturn();

        assertFalse(response.getResponse().getContentAsString().isEmpty(), VALIDATION_EMPTY_RESPONSE);
        ExceptionResponse exceptionResponse = objectMapper.readValue(
            response.getResponse().getContentAsString(),
            ExceptionResponse.class
        );

        assertTrue(exceptionResponse.getMessage().contains("x-language"), VALIDATION_EXCEPTION_RESPONSE);
    }

    @DisplayName("Should return a 400 Bad Request if the artefact type header is empty")
    @ParameterizedTest
    @MethodSource(PARAMETERS)
    void testEmptyArtefactType(String path, Object content, MediaType mediaType, ListType listType) throws Exception {
        MockHttpServletRequestBuilder mockHttpServletRequestBuilder = MediaType.APPLICATION_JSON.equals(mediaType)
            ? MockMvcRequestBuilders.post(path).content((String) content)
            : MockMvcRequestBuilders.multipart(path).file((MockMultipartFile) content);

        httpHeaders.add(PublicationConfiguration.LIST_TYPE, listType.toString());
        httpHeaders.set(PublicationConfiguration.TYPE_HEADER, EMPTY_VALUE);
        mockHttpServletRequestBuilder.headers(httpHeaders);
        mockHttpServletRequestBuilder.contentType(mediaType);

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
    @MethodSource(PARAMETERS)
    void testMissingArtefactType(String path, Object content, MediaType mediaType, ListType listType) throws Exception {
        MockHttpServletRequestBuilder mockHttpServletRequestBuilder = MediaType.APPLICATION_JSON.equals(mediaType)
            ? MockMvcRequestBuilders.post(path).content((String) content)
            : MockMvcRequestBuilders.multipart(path).file((MockMultipartFile) content);

        httpHeaders.add(PublicationConfiguration.LIST_TYPE, listType.toString());
        httpHeaders.remove(PublicationConfiguration.TYPE_HEADER);
        mockHttpServletRequestBuilder.headers(httpHeaders);
        mockHttpServletRequestBuilder.contentType(mediaType);

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
    @MethodSource(PARAMETERS)
    void testEmptyListType(String path, Object content, MediaType mediaType, ListType listType) throws Exception {
        MockHttpServletRequestBuilder mockHttpServletRequestBuilder = MediaType.APPLICATION_JSON.equals(mediaType)
            ? MockMvcRequestBuilders.post(path).content((String) content)
            : MockMvcRequestBuilders.multipart(path).file((MockMultipartFile) content);

        httpHeaders.set(PublicationConfiguration.LIST_TYPE, EMPTY_VALUE);
        mockHttpServletRequestBuilder.headers(httpHeaders);
        mockHttpServletRequestBuilder.contentType(mediaType);

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
    @MethodSource(PARAMETERS)
    void testMissingListType(String path, Object content, MediaType mediaType, ListType listType) throws Exception {
        MockHttpServletRequestBuilder mockHttpServletRequestBuilder = MediaType.APPLICATION_JSON.equals(mediaType)
            ? MockMvcRequestBuilders.post(path).content((String) content)
            : MockMvcRequestBuilders.multipart(path).file((MockMultipartFile) content);

        mockHttpServletRequestBuilder.headers(httpHeaders);
        mockHttpServletRequestBuilder.contentType(mediaType);

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
    @MethodSource(PARAMETERS)
    void testEmptyContentDate(String path, Object content, MediaType mediaType, ListType listType) throws Exception {
        MockHttpServletRequestBuilder mockHttpServletRequestBuilder = MediaType.APPLICATION_JSON.equals(mediaType)
            ? MockMvcRequestBuilders.post(path).content((String) content)
            : MockMvcRequestBuilders.multipart(path).file((MockMultipartFile) content);

        httpHeaders.add(PublicationConfiguration.LIST_TYPE, listType.toString());
        httpHeaders.set(PublicationConfiguration.CONTENT_DATE, EMPTY_VALUE);
        mockHttpServletRequestBuilder.headers(httpHeaders);
        mockHttpServletRequestBuilder.contentType(mediaType);

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
    @MethodSource(PARAMETERS)
    void testMissingContentDate(String path, Object content, MediaType mediaType, ListType listType) throws Exception {
        MockHttpServletRequestBuilder mockHttpServletRequestBuilder = MediaType.APPLICATION_JSON.equals(mediaType)
            ? MockMvcRequestBuilders.post(path).content((String) content)
            : MockMvcRequestBuilders.multipart(path).file((MockMultipartFile) content);

        httpHeaders.add(PublicationConfiguration.LIST_TYPE, listType.toString());
        httpHeaders.remove(PublicationConfiguration.CONTENT_DATE);
        mockHttpServletRequestBuilder.headers(httpHeaders);
        mockHttpServletRequestBuilder.contentType(mediaType);

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
    @MethodSource(PARAMETERS)
    void testEmptyCourtId(String path, Object content, MediaType mediaType, ListType listType) throws Exception {
        when(accountManagementService.getUserById(any())).thenReturn(piUser);
        MockHttpServletRequestBuilder mockHttpServletRequestBuilder = MediaType.APPLICATION_JSON.equals(mediaType)
            ? MockMvcRequestBuilders.post(path).content((String) content)
            : MockMvcRequestBuilders.multipart(path).file((MockMultipartFile) content);

        httpHeaders.add(PublicationConfiguration.LIST_TYPE, listType.toString());
        httpHeaders.set(PublicationConfiguration.COURT_ID, EMPTY_VALUE);
        mockHttpServletRequestBuilder.headers(httpHeaders);
        mockHttpServletRequestBuilder.contentType(mediaType);

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
    @MethodSource(PARAMETERS)
    void testMissingCourtId(String path, Object content, MediaType mediaType, ListType listType) throws Exception {
        MockHttpServletRequestBuilder mockHttpServletRequestBuilder = MediaType.APPLICATION_JSON.equals(mediaType)
            ? MockMvcRequestBuilders.post(path).content((String) content)
            : MockMvcRequestBuilders.multipart(path).file((MockMultipartFile) content);

        httpHeaders.add(PublicationConfiguration.LIST_TYPE, listType.toString());
        httpHeaders.remove(PublicationConfiguration.COURT_ID);
        mockHttpServletRequestBuilder.headers(httpHeaders);
        mockHttpServletRequestBuilder.contentType(mediaType);

        MvcResult response = mockMvc.perform(mockHttpServletRequestBuilder)
            .andExpect(status().isBadRequest()).andReturn();

        assertFalse(response.getResponse().getContentAsString().isEmpty(), VALIDATION_EMPTY_RESPONSE);
        ExceptionResponse exceptionResponse = objectMapper.readValue(
            response.getResponse().getContentAsString(),
            ExceptionResponse.class
        );

        assertTrue(exceptionResponse.getMessage().contains("x-court-id"), VALIDATION_EXCEPTION_RESPONSE);
    }

    @DisplayName("Should return a 400 bad request if artefact type is list and date to is empty")
    @ParameterizedTest
    @MethodSource(PARAMETERS)
    void testDateToAbsenceList(String path, Object content, MediaType mediaType, ListType listType) throws Exception {
        when(accountManagementService.getUserById(any())).thenReturn(piUser);
        MockHttpServletRequestBuilder mockHttpServletRequestBuilder = MediaType.APPLICATION_JSON.equals(mediaType)
            ? MockMvcRequestBuilders.post(path).content((String) content)
            : MockMvcRequestBuilders.multipart(path).file((MockMultipartFile) content);

        httpHeaders.add(PublicationConfiguration.LIST_TYPE, listType.toString());
        httpHeaders.remove(PublicationConfiguration.DISPLAY_TO_HEADER);
        mockHttpServletRequestBuilder.headers(httpHeaders);
        mockHttpServletRequestBuilder.contentType(mediaType);

        MvcResult response = mockMvc.perform(mockHttpServletRequestBuilder)
            .andExpect(status().isBadRequest()).andReturn();
        assertFalse(response.getResponse().getContentAsString().isEmpty(), VALIDATION_EMPTY_RESPONSE);
        ExceptionResponse exceptionResponse = objectMapper.readValue(
            response.getResponse().getContentAsString(),
            ExceptionResponse.class
        );

        assertTrue(exceptionResponse.getMessage().contains("x-display-to"), VALIDATION_EXCEPTION_RESPONSE);
    }

    @DisplayName("Should return a 400 bad request if artefact type is list and date from is empty")
    @ParameterizedTest
    @MethodSource(PARAMETERS)
    void testDateFromAbsenceList(String path, Object content, MediaType mediaType, ListType listType) throws Exception {
        when(accountManagementService.getUserById(any())).thenReturn(piUser);
        MockHttpServletRequestBuilder mockHttpServletRequestBuilder = MediaType.APPLICATION_JSON.equals(mediaType)
            ? MockMvcRequestBuilders.post(path).content((String) content)
            : MockMvcRequestBuilders.multipart(path).file((MockMultipartFile) content);

        httpHeaders.add(PublicationConfiguration.LIST_TYPE, listType.toString());
        httpHeaders.remove(PublicationConfiguration.DISPLAY_FROM_HEADER);
        mockHttpServletRequestBuilder.headers(httpHeaders);
        mockHttpServletRequestBuilder.contentType(mediaType);

        MvcResult response = mockMvc.perform(mockHttpServletRequestBuilder)
            .andExpect(status().isBadRequest()).andReturn();
        assertFalse(response.getResponse().getContentAsString().isEmpty(), VALIDATION_EMPTY_RESPONSE);
        ExceptionResponse exceptionResponse = objectMapper.readValue(
            response.getResponse().getContentAsString(),
            ExceptionResponse.class
        );

        assertTrue(exceptionResponse.getMessage().contains("x-display-from"), VALIDATION_EXCEPTION_RESPONSE);
    }

    private static Stream<Arguments> parameters() {
        return Stream.of(
            Arguments.of(PUBLICATION_URL, payload, MediaType.APPLICATION_JSON, LIST_TYPE),
            Arguments.of(PUBLICATION_URL, file, MediaType.MULTIPART_FORM_DATA, LIST_TYPE),
            Arguments.of(NON_STRATEGIC_PUBLICATION_URL, excelFile, MediaType.MULTIPART_FORM_DATA,
                         ListType.CST_WEEKLY_HEARING_LIST)
        );
    }
}
