package uk.gov.hmcts.reform.pip.data.management.controllers.publication.upload;

import com.microsoft.applicationinsights.web.dependencies.apachecommons.io.IOUtils;
import io.zonky.test.db.AutoConfigureEmbeddedDatabase;
import org.json.JSONArray;
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
import uk.gov.hmcts.reform.pip.data.management.errorhandling.ExceptionResponse;
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
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
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
class PublicationUploadTest extends PublicationIntegrationTestBase {
    private static final String PUBLICATION_URL = "/publication";
    private static final String SEARCH_COURT_URL = PUBLICATION_URL + "/locationId";
    private static final ArtefactType ARTEFACT_TYPE = ArtefactType.LIST;
    private static final Sensitivity SENSITIVITY = Sensitivity.PUBLIC;
    private static final String USER_ID = UUID.randomUUID().toString();
    private static final String PROVENANCE = "MANUAL_UPLOAD";
    private static final String PAYLOAD_UNKNOWN = "Unknown-Payload";
    private static final String SOURCE_ARTEFACT_ID = "sourceArtefactId";
    private static final LocalDateTime DISPLAY_TO = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
    private static final LocalDateTime DISPLAY_FROM = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
    private static final Language LANGUAGE = Language.ENGLISH;
    private static final ListType LIST_TYPE = ListType.CIVIL_DAILY_CAUSE_LIST;
    private static final String COURT_ID = "1";
    private static final LocalDateTime CONTENT_DATE = LocalDateTime.now().toLocalDate().atStartOfDay()
        .truncatedTo(ChronoUnit.SECONDS);
    private static final String SEARCH_KEY_FOUND = "array-value";
    private static final String SEARCH_KEY_NOT_FOUND = "case-urn";
    private static final String SEARCH_VALUE_1 = "array-value-1";
    private static final String SEARCH_VALUE_2 = "array-value-2";

    private static final String LOCATION_ID_SEARCH_KEY = "location-id";

    private static final String VALIDATION_EMPTY_RESPONSE = "Response should contain a Artefact";
    private static final String ARTEFACT_ID_POPULATED_MESSAGE = "Artefact ID should be populated";
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

        try (InputStream is = this.getClass().getClassLoader()
            .getResourceAsStream("data/artefact.json")) {
            payload = new String(IOUtils.toByteArray(
                Objects.requireNonNull(is)));
        }

        try (InputStream csvInputStream = this.getClass().getClassLoader()
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

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    @DisplayName("Should create a valid artefact and return the created artefact to the user")
    void creationOfAValidArtefact(boolean isJson) throws Exception {
        when(accountManagementService.getUserById(any())).thenReturn(piUser);
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
            .header(PublicationConfiguration.DISPLAY_TO_HEADER, DISPLAY_TO)
            .header(PublicationConfiguration.DISPLAY_FROM_HEADER, DISPLAY_FROM)
            .header(PublicationConfiguration.LIST_TYPE, LIST_TYPE)
            .header(PublicationConfiguration.COURT_ID, COURT_ID)
            .header(PublicationConfiguration.CONTENT_DATE, CONTENT_DATE)
            .header(PublicationConfiguration.LANGUAGE_HEADER, LANGUAGE)
            .header(PublicationConfiguration.REQUESTER_ID_HEADER, SYSTEM_ADMIN_ID)
            .contentType(isJson ? MediaType.APPLICATION_JSON : MediaType.MULTIPART_FORM_DATA);

        MvcResult response = mockMvc.perform(mockHttpServletRequestBuilder)
            .andExpect(status().isCreated())
            .andReturn();

        assertNotNull(response.getResponse().getContentAsString(), VALIDATION_EMPTY_RESPONSE);

        Artefact artefact = OBJECT_MAPPER.readValue(
            response.getResponse().getContentAsString(), Artefact.class);

        assertNotNull(artefact.getArtefactId(), ARTEFACT_ID_POPULATED_MESSAGE);
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
            searchResult.containsKey(isJson ? SEARCH_KEY_FOUND : LOCATION_ID_SEARCH_KEY),
            "Returned search result does not contain the correct key"
        );
        assertFalse(searchResult.containsKey(SEARCH_KEY_NOT_FOUND), "Returned search result contains "
            + "key that does not exist");
        assertEquals(
            isJson ? SEARCH_VALUE_1 : COURT_ID,
            searchResult.get(isJson ? SEARCH_KEY_FOUND : LOCATION_ID_SEARCH_KEY).get(0),
            "Does not contain first value in the array"
        );

        if (isJson) {
            assertEquals(SEARCH_VALUE_2, searchResult.get(SEARCH_KEY_FOUND).get(1),
                         "Does not contain second value in the array"
            );
        }
    }

    @DisplayName("Should create a valid artefact with provenance different from manual_upload")
    @Test
    void testUploadPublicationWithDifferentProvenance() throws Exception {
        Artefact artefact = createSscsDailyList(Sensitivity.PUBLIC, "UnknownProvenance");

        assertNotNull(artefact.getArtefactId(), "Artefact IDs do not match");
        assertEquals(artefact.getType(), ARTEFACT_TYPE, "Artefact types do not match");
        assertEquals(artefact.getProvenance(), "UnknownProvenance", "Provenances do not match");
        assertEquals(artefact.getLocationId(), "NoMatch" + COURT_ID, "Court ids do not match");
        assertEquals(artefact.getListType(), ListType.SSCS_DAILY_LIST, "List types do not match");
        assertEquals(artefact.getContentDate(), CONTENT_DATE, "Content dates do not match");
    }

    @DisplayName("Should create a valid artefact with only mandatory fields")
    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void creationOfAValidArtefactWithOnlyMandatoryFields(boolean isJson) throws Exception {
        when(accountManagementService.getUserById(any())).thenReturn(piUser);
        MockHttpServletRequestBuilder mockHttpServletRequestBuilder;
        if (isJson) {
            mockHttpServletRequestBuilder = MockMvcRequestBuilders.post(PUBLICATION_URL).content(payload);
        } else {
            mockHttpServletRequestBuilder = MockMvcRequestBuilders.multipart(PUBLICATION_URL).file(file);
        }

        mockHttpServletRequestBuilder.header(PublicationConfiguration.TYPE_HEADER, ARTEFACT_TYPE);
        mockHttpServletRequestBuilder.header(PublicationConfiguration.PROVENANCE_HEADER, PROVENANCE);
        mockHttpServletRequestBuilder.header(PublicationConfiguration.DISPLAY_FROM_HEADER, DISPLAY_FROM);
        mockHttpServletRequestBuilder.header(PublicationConfiguration.DISPLAY_TO_HEADER, DISPLAY_TO);
        mockHttpServletRequestBuilder.header(PublicationConfiguration.COURT_ID, COURT_ID);
        mockHttpServletRequestBuilder.header(PublicationConfiguration.LIST_TYPE, LIST_TYPE);
        mockHttpServletRequestBuilder.header(PublicationConfiguration.CONTENT_DATE, CONTENT_DATE);
        mockHttpServletRequestBuilder.header(PublicationConfiguration.LANGUAGE_HEADER, LANGUAGE);
        mockHttpServletRequestBuilder.header(PublicationConfiguration.REQUESTER_ID_HEADER, SYSTEM_ADMIN_ID);
        mockHttpServletRequestBuilder.contentType(isJson ? MediaType.APPLICATION_JSON : MediaType.MULTIPART_FORM_DATA);

        MvcResult response = mockMvc.perform(mockHttpServletRequestBuilder)
            .andExpect(status().isCreated()).andReturn();

        Artefact artefact = OBJECT_MAPPER.readValue(
            response.getResponse().getContentAsString(), Artefact.class);

        assertNotNull(artefact.getArtefactId(), ARTEFACT_ID_POPULATED_MESSAGE);
        assertEquals(artefact.getType(), ARTEFACT_TYPE, "Artefact type does not match input artefact type");
        assertEquals(artefact.getProvenance(), PROVENANCE, "Provenance does not match input provenance");
        assertEquals(artefact.getDisplayFrom(), DISPLAY_FROM, "Display from does not match input display from");
        assertEquals(artefact.getDisplayTo(), DISPLAY_TO, "Display to does not match input display to");
        assertEquals(artefact.getLocationId(), COURT_ID, "Court id does not match input court id");
        assertEquals(artefact.getListType(), LIST_TYPE, "List type does not match input list type");
        assertEquals(artefact.getContentDate(), CONTENT_DATE, "Content date does not match input content date");
        assertEquals(artefact.getLanguage(), LANGUAGE, "Language does not match input language");
    }

    @DisplayName("Should populate the datefrom field if it's empty and the type is GENERAL_PUBLICATION")
    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void testPopulateDefaultDateFrom(boolean isJson) throws Exception {
        when(accountManagementService.getUserById(any())).thenReturn(piUser);
        MockHttpServletRequestBuilder mockHttpServletRequestBuilder;
        if (isJson) {
            mockHttpServletRequestBuilder = MockMvcRequestBuilders.post(PUBLICATION_URL).content(payload);
        } else {
            mockHttpServletRequestBuilder = MockMvcRequestBuilders.multipart(PUBLICATION_URL).file(file);
        }

        mockHttpServletRequestBuilder.header(PublicationConfiguration.TYPE_HEADER, ArtefactType.GENERAL_PUBLICATION);
        mockHttpServletRequestBuilder.header(PublicationConfiguration.SENSITIVITY_HEADER, SENSITIVITY);
        mockHttpServletRequestBuilder.header(PublicationConfiguration.PROVENANCE_HEADER, PROVENANCE);
        mockHttpServletRequestBuilder.header(PublicationConfiguration.SOURCE_ARTEFACT_ID_HEADER, SOURCE_ARTEFACT_ID);
        mockHttpServletRequestBuilder.header(PublicationConfiguration.LANGUAGE_HEADER, LANGUAGE);
        mockHttpServletRequestBuilder.header(PublicationConfiguration.CONTENT_DATE, CONTENT_DATE);
        mockHttpServletRequestBuilder.header(PublicationConfiguration.LIST_TYPE, ListType.CIVIL_DAILY_CAUSE_LIST);
        mockHttpServletRequestBuilder.header(PublicationConfiguration.COURT_ID, COURT_ID);
        mockHttpServletRequestBuilder.header(PublicationConfiguration.REQUESTER_ID_HEADER, SYSTEM_ADMIN_ID);
        mockHttpServletRequestBuilder.contentType(isJson ? MediaType.APPLICATION_JSON : MediaType.MULTIPART_FORM_DATA);
        MvcResult response = mockMvc.perform(mockHttpServletRequestBuilder)
            .andExpect(status().isCreated()).andReturn();
        Artefact createdArtefact = OBJECT_MAPPER.readValue(
            response.getResponse().getContentAsString(),
            Artefact.class
        );

        assertNotNull(createdArtefact.getDisplayFrom(), "Artefact date from criteria exists"
            + "when none is provided and status update is type");

    }

    @DisplayName("Should populate the sensitivity field if it's empty to PUBLIC")
    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void testPopulateDefaultSensitivity(boolean isJson) throws Exception {
        when(accountManagementService.getUserById(any())).thenReturn(piUser);
        MockHttpServletRequestBuilder mockHttpServletRequestBuilder;
        if (isJson) {
            mockHttpServletRequestBuilder = MockMvcRequestBuilders.post(PUBLICATION_URL).content(payload);
        } else {
            mockHttpServletRequestBuilder = MockMvcRequestBuilders.multipart(PUBLICATION_URL).file(file);
        }
        mockHttpServletRequestBuilder.header(PublicationConfiguration.TYPE_HEADER, ARTEFACT_TYPE);
        mockHttpServletRequestBuilder.header(PublicationConfiguration.LANGUAGE_HEADER, LANGUAGE);
        mockHttpServletRequestBuilder.header(PublicationConfiguration.PROVENANCE_HEADER, PROVENANCE);
        mockHttpServletRequestBuilder.header(PublicationConfiguration.SOURCE_ARTEFACT_ID_HEADER, SOURCE_ARTEFACT_ID);
        mockHttpServletRequestBuilder.header(PublicationConfiguration.DISPLAY_TO_HEADER, DISPLAY_TO);
        mockHttpServletRequestBuilder.header(PublicationConfiguration.DISPLAY_FROM_HEADER, DISPLAY_FROM);
        mockHttpServletRequestBuilder.header(PublicationConfiguration.LIST_TYPE, LIST_TYPE);
        mockHttpServletRequestBuilder.header(PublicationConfiguration.COURT_ID, COURT_ID);
        mockHttpServletRequestBuilder.header(PublicationConfiguration.CONTENT_DATE, CONTENT_DATE);
        mockHttpServletRequestBuilder.header(PublicationConfiguration.REQUESTER_ID_HEADER, SYSTEM_ADMIN_ID);
        mockHttpServletRequestBuilder.contentType(isJson ? MediaType.APPLICATION_JSON : MediaType.MULTIPART_FORM_DATA);
        MvcResult response = mockMvc.perform(mockHttpServletRequestBuilder)
            .andExpect(status().isCreated()).andReturn();
        Artefact createdArtefact = OBJECT_MAPPER.readValue(
            response.getResponse().getContentAsString(),
            Artefact.class
        );

        assertEquals(createdArtefact.getSensitivity(), Sensitivity.PUBLIC, "Artefact sensitivity has not been "
            + "set to public by default");

    }

    @DisplayName("Should update the artefact and return the same Artefact ID as the originally created one")
    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void updatingOfAnArtefactThatAlreadyExists(boolean isJson) throws Exception {
        when(accountManagementService.getUserById(any())).thenReturn(piUser);
        MockHttpServletRequestBuilder mockHttpServletRequestBuilder;
        if (isJson) {
            mockHttpServletRequestBuilder = MockMvcRequestBuilders.post(PUBLICATION_URL).content(payload);
        } else {
            mockHttpServletRequestBuilder = MockMvcRequestBuilders.multipart(PUBLICATION_URL).file(file);
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
        mockHttpServletRequestBuilder.header(PublicationConfiguration.LANGUAGE_HEADER, LANGUAGE);
        mockHttpServletRequestBuilder.header(PublicationConfiguration.REQUESTER_ID_HEADER, SYSTEM_ADMIN_ID);
        mockHttpServletRequestBuilder.contentType(isJson ? MediaType.APPLICATION_JSON : MediaType.MULTIPART_FORM_DATA);

        final MvcResult createResponse =
            mockMvc.perform(mockHttpServletRequestBuilder).andExpect(status().isCreated()).andReturn();

        if (isJson) {
            mockHttpServletRequestBuilder = MockMvcRequestBuilders.post(PUBLICATION_URL).content(payload);
        } else {
            mockHttpServletRequestBuilder = MockMvcRequestBuilders.multipart(PUBLICATION_URL).file(file);
        }

        mockHttpServletRequestBuilder.header(PublicationConfiguration.TYPE_HEADER, ARTEFACT_TYPE);
        mockHttpServletRequestBuilder.header(PublicationConfiguration.SENSITIVITY_HEADER, SENSITIVITY);
        mockHttpServletRequestBuilder.header(PublicationConfiguration.PROVENANCE_HEADER, PROVENANCE);
        mockHttpServletRequestBuilder.header(PublicationConfiguration.SOURCE_ARTEFACT_ID_HEADER, SOURCE_ARTEFACT_ID);
        mockHttpServletRequestBuilder.header(PublicationConfiguration.DISPLAY_TO_HEADER, DISPLAY_TO);
        mockHttpServletRequestBuilder.header(PublicationConfiguration.DISPLAY_FROM_HEADER, DISPLAY_FROM.plusMonths(1));
        mockHttpServletRequestBuilder.header(PublicationConfiguration.LIST_TYPE, LIST_TYPE);
        mockHttpServletRequestBuilder.header(PublicationConfiguration.COURT_ID, COURT_ID);
        mockHttpServletRequestBuilder.header(PublicationConfiguration.CONTENT_DATE, CONTENT_DATE);
        mockHttpServletRequestBuilder.header(PublicationConfiguration.LANGUAGE_HEADER, LANGUAGE);
        mockHttpServletRequestBuilder.header(PublicationConfiguration.REQUESTER_ID_HEADER, SYSTEM_ADMIN_ID);
        mockHttpServletRequestBuilder.contentType(isJson ? MediaType.APPLICATION_JSON : MediaType.MULTIPART_FORM_DATA);

        final MvcResult updatedResponse = mockMvc.perform(mockHttpServletRequestBuilder).andExpect(
            status().isCreated()).andReturn();

        Artefact createdArtefact = OBJECT_MAPPER.readValue(
            createResponse.getResponse().getContentAsString(),
            Artefact.class
        );

        Artefact updatedArtefact = OBJECT_MAPPER.readValue(
            updatedResponse.getResponse().getContentAsString(),
            Artefact.class
        );

        assertEquals(createdArtefact.getArtefactId(), updatedArtefact.getArtefactId(), "A new artefact has "
            + "been created rather than it being updated");

        assertEquals(DISPLAY_FROM.plusMonths(1), updatedArtefact.getDisplayFrom(), "The updated artefact does "
            + "not contain the new display from");
    }

    @DisplayName("Should throw a 403 forbidden when payload is not of JSON through the JSON endpoint")
    @Test
    void creationOfAJsonArtefactThatIsNotJson() throws Exception {
        MockHttpServletRequestBuilder mockHttpServletRequestBuilder = MockMvcRequestBuilders
            .post(PUBLICATION_URL)
            .header(PublicationConfiguration.TYPE_HEADER, ARTEFACT_TYPE)
            .header(PublicationConfiguration.SENSITIVITY_HEADER, SENSITIVITY)
            .header(PublicationConfiguration.PROVENANCE_HEADER, PROVENANCE)
            .header(PublicationConfiguration.SOURCE_ARTEFACT_ID_HEADER, SOURCE_ARTEFACT_ID)
            .header(PublicationConfiguration.DISPLAY_TO_HEADER, DISPLAY_TO)
            .header(PublicationConfiguration.DISPLAY_FROM_HEADER, DISPLAY_FROM)
            .header(PublicationConfiguration.LIST_TYPE, LIST_TYPE)
            .header(PublicationConfiguration.COURT_ID, COURT_ID)
            .header(PublicationConfiguration.CONTENT_DATE, CONTENT_DATE)
            .header(PublicationConfiguration.LANGUAGE_HEADER, LANGUAGE)
            .header(PublicationConfiguration.REQUESTER_ID_HEADER, SYSTEM_ADMIN_ID)
            .content(PAYLOAD_UNKNOWN)
            .contentType(MediaType.APPLICATION_JSON);

        mockMvc.perform(mockHttpServletRequestBuilder).andExpect(status().isForbidden()).andReturn();
    }

    @DisplayName("Check that null date for general_publication still allows us to return the relevant artefact")
    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void checkStatusUpdatesWithNullDateTo(boolean isJson) throws Exception {
        when(accountManagementService.getUserById(any())).thenReturn(piUser);
        MockHttpServletRequestBuilder mockHttpServletRequestBuilder;

        if (isJson) {
            mockHttpServletRequestBuilder = MockMvcRequestBuilders.post(PUBLICATION_URL).content(payload);
        } else {
            mockHttpServletRequestBuilder = MockMvcRequestBuilders.multipart(PUBLICATION_URL).file(file);
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
            .header(PublicationConfiguration.LANGUAGE_HEADER, LANGUAGE)
            .header(PublicationConfiguration.REQUESTER_ID_HEADER, SYSTEM_ADMIN_ID)
            .contentType(isJson ? MediaType.APPLICATION_JSON : MediaType.MULTIPART_FORM_DATA);

        mockMvc.perform(mockHttpServletRequestBuilder).andExpect(status().isCreated()).andReturn();

        MockHttpServletRequestBuilder mockHttpServletRequestBuilder1 = MockMvcRequestBuilders
            .get(SEARCH_COURT_URL + "/" + COURT_ID)
            .header(REQUESTER_ID_HEADER, SYSTEM_ADMIN_ID);
        MvcResult getResponse = mockMvc.perform(mockHttpServletRequestBuilder1)
            .andExpect(status().isOk())
            .andReturn();

        String jsonOutput = getResponse.getResponse().getContentAsString();
        JSONArray jsonArray = new JSONArray(jsonOutput);
        Artefact retrievedArtefact = OBJECT_MAPPER.readValue(
            jsonArray.get(0).toString(), Artefact.class
        );

        assertEquals(COURT_ID, retrievedArtefact.getLocationId(), "Artefact not found.");
    }

    @DisplayName("Should not create an invalid artefact")
    @Test
    void testCreationOfInvalidPublication() throws Exception {
        when(accountManagementService.getUserById(any())).thenReturn(piUser);
        try (InputStream mockFile = this.getClass().getClassLoader()
            .getResourceAsStream("data/civil-daily-cause-list/civilDailyCauseListInvalid.json")) {
            MockHttpServletRequestBuilder mockHttpServletRequestBuilder = MockMvcRequestBuilders
                .post(PUBLICATION_URL)
                .header(PublicationConfiguration.TYPE_HEADER, ARTEFACT_TYPE)
                .header(PublicationConfiguration.PROVENANCE_HEADER, PROVENANCE)
                .header(PublicationConfiguration.SOURCE_ARTEFACT_ID_HEADER, SOURCE_ARTEFACT_ID)
                .header(PublicationConfiguration.DISPLAY_FROM_HEADER, DISPLAY_FROM)
                .header(PublicationConfiguration.DISPLAY_TO_HEADER, DISPLAY_TO)
                .header(PublicationConfiguration.COURT_ID, COURT_ID)
                .header(PublicationConfiguration.LIST_TYPE, ListType.CIVIL_DAILY_CAUSE_LIST)
                .header(PublicationConfiguration.CONTENT_DATE, CONTENT_DATE)
                .header(PublicationConfiguration.LANGUAGE_HEADER, LANGUAGE)
                .header(PublicationConfiguration.REQUESTER_ID_HEADER, SYSTEM_ADMIN_ID)
                .content(mockFile.readAllBytes())
                .contentType(MediaType.APPLICATION_JSON);

            MvcResult response = mockMvc.perform(mockHttpServletRequestBuilder)
                .andExpect(status().isBadRequest()).andReturn();

            assertNotNull(response.getResponse().getContentAsString(), VALIDATION_EMPTY_RESPONSE);

            ExceptionResponse exceptionResponse = OBJECT_MAPPER.readValue(
                response.getResponse().getContentAsString(),
                ExceptionResponse.class
            );

            assertTrue(
                exceptionResponse.getMessage().contains("courtHouseName"),
                "Court name is not displayed in the exception response"
            );
        }
    }
}
