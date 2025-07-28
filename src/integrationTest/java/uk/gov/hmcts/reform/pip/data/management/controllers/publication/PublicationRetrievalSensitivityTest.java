package uk.gov.hmcts.reform.pip.data.management.controllers.publication;

import com.microsoft.applicationinsights.web.dependencies.apachecommons.io.IOUtils;
import io.zonky.test.db.AutoConfigureEmbeddedDatabase;
import org.json.JSONArray;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
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
import uk.gov.hmcts.reform.pip.model.account.PiUser;
import uk.gov.hmcts.reform.pip.model.publication.ArtefactType;
import uk.gov.hmcts.reform.pip.model.publication.Language;
import uk.gov.hmcts.reform.pip.model.publication.ListType;
import uk.gov.hmcts.reform.pip.model.publication.Sensitivity;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Objects;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.pip.model.account.Roles.SYSTEM_ADMIN;

@SpringBootTest(classes = {Application.class, AzureBlobConfigurationTestConfiguration.class},
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("integration")
@AutoConfigureEmbeddedDatabase(type = AutoConfigureEmbeddedDatabase.DatabaseType.POSTGRES)
@WithMockUser(username = "admin", authorities = {"APPROLE_api.request.admin"})
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SuppressWarnings("PMD.ExcessiveImports")
class PublicationRetrievalSensitivityTest extends PublicationIntegrationTestBase {
    private static final String PUBLICATION_URL = "/publication";
    private static final String SEARCH_COURT_URL = PUBLICATION_URL + "/locationId";
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
    private static final String VALIDATION_DISPLAY_FROM = "The expected Display From has not been returned";
    private static final String REQUESTER_ID_HEADER = "x-requester-id";
    private static final String SYSTEM_ADMIN_ID = UUID.randomUUID().toString();

    private static String payload = "payload";
    private static MockMultipartFile file;

    private static PiUser piUser;

    @BeforeAll
    void setup() throws Exception {
        piUser = new PiUser();
        piUser.setUserId(SYSTEM_ADMIN_ID);
        piUser.setEmail("test@justice.gov.uk");
        piUser.setRoles(SYSTEM_ADMIN);

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
            when(accountManagementService.getUserById(any())).thenReturn(piUser);

            mockMvc.perform(MockMvcRequestBuilders.multipart("/locations/upload").file(csvFile)
                                .header(REQUESTER_ID_HEADER, SYSTEM_ADMIN_ID)
                                .with(user("admin")
                                          .authorities(new SimpleGrantedAuthority("APPROLE_api.request.admin"))))
                .andExpect(status().isOk()).andReturn();
        }
    }

    @DisplayName("Verify that artefact is returned when user is verified and sensitivity is public")
    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void verifyThatArtefactsAreReturnedForVerifiedUserWhenPublic(boolean isJson) throws Exception {
        when(accountManagementService.getUserById(any())).thenReturn(piUser);
        MockHttpServletRequestBuilder mockHttpServletRequestBuilder;

        if (isJson) {
            mockHttpServletRequestBuilder = MockMvcRequestBuilders.post(PUBLICATION_URL).content(payload);
        } else {
            mockHttpServletRequestBuilder = MockMvcRequestBuilders.multipart(PUBLICATION_URL).file(file);
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
            .header(PublicationConfiguration.LANGUAGE_HEADER, LANGUAGE)
            .header(PublicationConfiguration.REQUESTER_ID_HEADER, SYSTEM_ADMIN_ID)
            .contentType(isJson ? MediaType.APPLICATION_JSON : MediaType.MULTIPART_FORM_DATA);
        MvcResult createResponse =
            mockMvc.perform(mockHttpServletRequestBuilder).andExpect(status().isCreated()).andReturn();
        Artefact createdArtefact = OBJECT_MAPPER.readValue(
            createResponse.getResponse().getContentAsString(),
            Artefact.class
        );

        assertEquals(createdArtefact.getDisplayFrom(), DISPLAY_FROM.minusMonths(2),
                     VALIDATION_DISPLAY_FROM
        );

        MockHttpServletRequestBuilder mockHttpServletRequestBuilder1 = MockMvcRequestBuilders
            .get(SEARCH_COURT_URL + "/" + COURT_ID)
            .header(REQUESTER_ID_HEADER, SYSTEM_ADMIN_ID);
        MvcResult getResponse =
            mockMvc.perform(mockHttpServletRequestBuilder1).andExpect(status().isOk()).andReturn();

        String jsonOutput = getResponse.getResponse().getContentAsString();
        JSONArray jsonArray = new JSONArray(jsonOutput);
        Artefact retrievedArtefact = OBJECT_MAPPER.readValue(
            jsonArray.get(0).toString(), Artefact.class
        );
        assertEquals(COURT_ID, retrievedArtefact.getLocationId(),
                     "Incorrect court ID has been retrieved from the database"
        );
    }

    @DisplayName("Verify that artefact is returned when user is unverified and sensitivity is public")
    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void verifyThatArtefactsAreReturnedForUnverifiedUserWhenPublic(boolean isJson) throws Exception {
        when(accountManagementService.getUserById(any())).thenReturn(piUser);
        MockHttpServletRequestBuilder mockHttpServletRequestBuilder;

        if (isJson) {
            mockHttpServletRequestBuilder = MockMvcRequestBuilders.post(PUBLICATION_URL).content(payload);
        } else {
            mockHttpServletRequestBuilder = MockMvcRequestBuilders.multipart(PUBLICATION_URL).file(file);
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
            .header(PublicationConfiguration.LANGUAGE_HEADER, LANGUAGE)
            .header(PublicationConfiguration.REQUESTER_ID_HEADER, SYSTEM_ADMIN_ID)
            .contentType(isJson ? MediaType.APPLICATION_JSON : MediaType.MULTIPART_FORM_DATA);

        MvcResult createResponse =
            mockMvc.perform(mockHttpServletRequestBuilder).andExpect(status().isCreated()).andReturn();
        Artefact createdArtefact = OBJECT_MAPPER.readValue(
            createResponse.getResponse().getContentAsString(),
            Artefact.class
        );

        assertEquals(createdArtefact.getDisplayFrom(), DISPLAY_FROM.minusMonths(2),
                     VALIDATION_DISPLAY_FROM
        );

        MockHttpServletRequestBuilder mockHttpServletRequestBuilder1 = MockMvcRequestBuilders
            .get(SEARCH_COURT_URL + "/" + COURT_ID)
            .header(REQUESTER_ID_HEADER, SYSTEM_ADMIN_ID);
        MvcResult getResponse =
            mockMvc.perform(mockHttpServletRequestBuilder1).andExpect(status().isOk()).andReturn();

        String jsonOutput = getResponse.getResponse().getContentAsString();
        JSONArray jsonArray = new JSONArray(jsonOutput);
        Artefact retrievedArtefact = OBJECT_MAPPER.readValue(
            jsonArray.get(0).toString(), Artefact.class
        );
        assertEquals(COURT_ID, retrievedArtefact.getLocationId(),
                     "Incorrect court ID has been retrieved from the database"
        );

    }

    @DisplayName("Verify that artefact is not returned when user is unverified and artefact is classified")
    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void verifyThatArtefactsAreNotReturnedForUnverifiedUserWhenClassified(boolean isJson) throws Exception {
        when(accountManagementService.getUserById(any())).thenReturn(piUser);

        MockHttpServletRequestBuilder mockHttpServletRequestBuilder;

        if (isJson) {
            mockHttpServletRequestBuilder = MockMvcRequestBuilders.post(PUBLICATION_URL).content(payload);
        } else {
            mockHttpServletRequestBuilder = MockMvcRequestBuilders.multipart(PUBLICATION_URL).file(file);
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
            .header(PublicationConfiguration.LANGUAGE_HEADER, LANGUAGE)
            .header(PublicationConfiguration.REQUESTER_ID_HEADER, SYSTEM_ADMIN_ID)
            .contentType(isJson ? MediaType.APPLICATION_JSON : MediaType.MULTIPART_FORM_DATA);

        MvcResult createResponse =
            mockMvc.perform(mockHttpServletRequestBuilder).andExpect(status().isCreated()).andReturn();
        Artefact createdArtefact = OBJECT_MAPPER.readValue(
            createResponse.getResponse().getContentAsString(),
            Artefact.class
        );

        assertEquals(createdArtefact.getDisplayFrom(), DISPLAY_FROM.minusMonths(2),
                     VALIDATION_DISPLAY_FROM
        );

        MockHttpServletRequestBuilder mockHttpServletRequestBuilder1 = MockMvcRequestBuilders
            .get(SEARCH_COURT_URL + "/" + COURT_ID);
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
        when(accountManagementService.getUserById(any())).thenReturn(piUser);
        MockHttpServletRequestBuilder mockHttpServletRequestBuilder;

        if (isJson) {
            mockHttpServletRequestBuilder = MockMvcRequestBuilders.post(PUBLICATION_URL).content(payload);
        } else {
            mockHttpServletRequestBuilder = MockMvcRequestBuilders.multipart(PUBLICATION_URL).file(file);
        }

        mockHttpServletRequestBuilder
            .header(PublicationConfiguration.TYPE_HEADER, ARTEFACT_TYPE)
            .header(PublicationConfiguration.SENSITIVITY_HEADER, Sensitivity.PRIVATE)
            .header(PublicationConfiguration.PROVENANCE_HEADER, PROVENANCE)
            .header(PublicationConfiguration.SOURCE_ARTEFACT_ID_HEADER, SOURCE_ARTEFACT_ID)
            .header(PublicationConfiguration.DISPLAY_TO_HEADER, DISPLAY_TO.plusMonths(1))
            .header(PublicationConfiguration.DISPLAY_FROM_HEADER, DISPLAY_FROM.minusMonths(2))
            .header(PublicationConfiguration.LIST_TYPE, LIST_TYPE)
            .header(PublicationConfiguration.COURT_ID, COURT_ID)
            .header(PublicationConfiguration.LANGUAGE_HEADER, LANGUAGE)
            .header(PublicationConfiguration.CONTENT_DATE, CONTENT_DATE)
            .header(PublicationConfiguration.LANGUAGE_HEADER, LANGUAGE)
            .header(PublicationConfiguration.REQUESTER_ID_HEADER, SYSTEM_ADMIN_ID)
            .contentType(isJson ? MediaType.APPLICATION_JSON : MediaType.MULTIPART_FORM_DATA);

        MvcResult createResponse =
            mockMvc.perform(mockHttpServletRequestBuilder).andExpect(status().isCreated()).andReturn();
        Artefact createdArtefact = OBJECT_MAPPER.readValue(
            createResponse.getResponse().getContentAsString(),
            Artefact.class
        );

        assertEquals(createdArtefact.getDisplayFrom(), DISPLAY_FROM.minusMonths(2),
                     VALIDATION_DISPLAY_FROM
        );

        MockHttpServletRequestBuilder mockHttpServletRequestBuilder1 = MockMvcRequestBuilders
            .get(SEARCH_COURT_URL + "/" + COURT_ID)
            .header(REQUESTER_ID_HEADER, SYSTEM_ADMIN_ID);
        MvcResult getResponse =
            mockMvc.perform(mockHttpServletRequestBuilder1).andExpect(status().isOk()).andReturn();

        String jsonOutput = getResponse.getResponse().getContentAsString();
        JSONArray jsonArray = new JSONArray(jsonOutput);
        assertEquals(0, jsonArray.length(),
                     "Unknown artefacts have been returned from the database"
        );
    }
}
