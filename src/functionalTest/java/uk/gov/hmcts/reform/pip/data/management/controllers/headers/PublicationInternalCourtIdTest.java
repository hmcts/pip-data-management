package uk.gov.hmcts.reform.pip.data.management.controllers.headers;

import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.applicationinsights.web.dependencies.apachecommons.io.IOUtils;
import io.zonky.test.db.AutoConfigureEmbeddedDatabase;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import uk.gov.hmcts.reform.pip.data.management.Application;
import uk.gov.hmcts.reform.pip.data.management.config.PublicationConfiguration;
import uk.gov.hmcts.reform.pip.data.management.database.LocationRepository;
import uk.gov.hmcts.reform.pip.data.management.models.publication.Artefact;
import uk.gov.hmcts.reform.pip.model.location.Location;
import uk.gov.hmcts.reform.pip.model.publication.ArtefactType;
import uk.gov.hmcts.reform.pip.model.publication.Language;
import uk.gov.hmcts.reform.pip.model.publication.ListType;
import uk.gov.hmcts.reform.pip.model.publication.Sensitivity;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = {Application.class},
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@ActiveProfiles(profiles = "functional")
@SuppressWarnings({"PMD.ExcessiveImports", "PMD.ExcessiveClassLength",
    "PMD.CyclomaticComplexity", "PMD.TooManyMethods"})
@AutoConfigureEmbeddedDatabase(type = AutoConfigureEmbeddedDatabase.DatabaseType.POSTGRES)
@WithMockUser(username = "admin", authorities = { "APPROLE_api.request.admin" })
class PublicationInternalCourtIdTest {

    @Autowired
    BlobContainerClient blobContainerClient;

    @Autowired
    BlobClient blobClient;

    @Autowired
    LocationRepository locationRepository;

    @Autowired
    private MockMvc mockMvc;

    private static final String PUBLICATION_URL = "/publication";
    private static final ArtefactType ARTEFACT_TYPE = ArtefactType.LIST;
    private static final Sensitivity SENSITIVITY = Sensitivity.PUBLIC;
    private static final String PROVENANCE = "provenance";
    private static String payload = "payload";
    private static MockMultipartFile file;
    private static final String SOURCE_ARTEFACT_ID = "sourceArtefactId-1";
    private static final LocalDateTime DISPLAY_TO = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
    private static final LocalDateTime DISPLAY_FROM = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
    private static final Language LANGUAGE = Language.ENGLISH;
    private static final String BLOB_PAYLOAD_URL = "https://localhost";
    private static final ListType LIST_TYPE = ListType.CIVIL_DAILY_CAUSE_LIST;
    private static final String PROVENANCE_COURT_ID_MATCH = "123";
    private static final String PROVENANCE_COURT_ID_DOESNOT_MATCH = "111";
    private static final String INTERNAL_COURT_ID = "1";
    private static final LocalDateTime CONTENT_DATE = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
    private static final String SEARCH_KEY_FOUND = "array-value";
    private static final String SEARCH_KEY_NOT_FOUND = "case-urn";
    private static final String SEARCH_VALUE_1 = "array-value-1";
    private static final String SEARCH_VALUE_2 = "array-value-2";
    private static final String LOCATION_ID_SEARCH_KEY = "location-id";

    private static final String VALIDATION_EMPTY_RESPONSE = "Response should contain a Artefact";

    private static MockHttpServletRequestBuilder mockHttpServletRequestBuilder;
    private static ObjectMapper objectMapper;

    @BeforeAll
    public static void setup() throws IOException {
        file = new MockMultipartFile("file", "test123.pdf", MediaType.APPLICATION_PDF_VALUE, "test content123".getBytes(
            StandardCharsets.UTF_8));
        payload = new String(IOUtils.toByteArray(
            Objects.requireNonNull(PublicationInternalCourtIdTest.class.getClassLoader()
                                       .getResourceAsStream("data/artefact.json"))));

        objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();
    }

    @BeforeEach
    void setupCourts() throws Exception {
        createCourts();
    }

    private List<Location> createCourts() throws Exception {

        try (InputStream csvInputStream = this.getClass().getClassLoader()
            .getResourceAsStream("location/ValidReferenceData.csv")) {
            MockMultipartFile csvFile
                = new MockMultipartFile("locationList", csvInputStream);

            MvcResult mvcResult = mockMvc.perform(multipart("/locations/upload").file(csvFile))
                .andExpect(status().isOk()).andReturn();

            return Arrays.asList(
                objectMapper.readValue(mvcResult.getResponse().getContentAsString(), Location[].class));
        }
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    @DisplayName("Should create a valid artefact, updated court Id with internal "
        + "court Id and return the created artefact to the user")
    void creationOfAValidArtefactAndUpdateCourtId(boolean isJson) throws Exception {
        if (isJson) {
            mockHttpServletRequestBuilder = MockMvcRequestBuilders.post(PUBLICATION_URL).content(payload);
        } else {
            mockHttpServletRequestBuilder = multipart(PUBLICATION_URL).file(file);
        }
        mockHttpServletRequestBuilder.header(PublicationConfiguration.TYPE_HEADER, ARTEFACT_TYPE);
        mockHttpServletRequestBuilder.header(PublicationConfiguration.SENSITIVITY_HEADER, SENSITIVITY);
        mockHttpServletRequestBuilder.header(PublicationConfiguration.LANGUAGE_HEADER, LANGUAGE);
        mockHttpServletRequestBuilder.header(PublicationConfiguration.PROVENANCE_HEADER, PROVENANCE);
        mockHttpServletRequestBuilder.header(PublicationConfiguration.SOURCE_ARTEFACT_ID_HEADER, SOURCE_ARTEFACT_ID);
        mockHttpServletRequestBuilder.header(PublicationConfiguration.DISPLAY_TO_HEADER, DISPLAY_TO);
        mockHttpServletRequestBuilder.header(PublicationConfiguration.DISPLAY_FROM_HEADER, DISPLAY_FROM);
        mockHttpServletRequestBuilder.header(PublicationConfiguration.LIST_TYPE, LIST_TYPE);
        mockHttpServletRequestBuilder.header(PublicationConfiguration.COURT_ID, PROVENANCE_COURT_ID_MATCH);
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
        assertEquals(artefact.getSensitivity(), SENSITIVITY,
                     "Sensitivity does not match input sensitivity");
        assertEquals(artefact.getLocationId(), INTERNAL_COURT_ID,
                     "Provenance court Id does not match with reference data");

        Map<String, List<Object>> searchResult = artefact.getSearch();
        assertTrue(
            searchResult.containsKey(isJson ? SEARCH_KEY_FOUND : LOCATION_ID_SEARCH_KEY),
            "Returned search result does not contain the correct key"
        );
        assertFalse(searchResult.containsKey(SEARCH_KEY_NOT_FOUND), "Returned search result contains "
            + "key that does not exist");
        assertEquals(
            isJson ? SEARCH_VALUE_1 : PROVENANCE_COURT_ID_MATCH,
            searchResult.get(isJson ? SEARCH_KEY_FOUND : LOCATION_ID_SEARCH_KEY).get(0),
            "Does not contain first value in the array"
        );

        if (isJson) {
            assertEquals(SEARCH_VALUE_2, searchResult.get(SEARCH_KEY_FOUND).get(1),
                         "Does not contain second value in the array"
            );
        }

        assertTrue(artefact.getPayload().startsWith(BLOB_PAYLOAD_URL), "Payload is not as expected");
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    @DisplayName("Should create a valid artefact, unable to find "
        + "internal court Id and return the created artefact to the user")
    void creationOfAValidArtefactAndUpdateCourtIdWithNoMatch(boolean isJson) throws Exception {
        if (isJson) {
            mockHttpServletRequestBuilder = MockMvcRequestBuilders.post(PUBLICATION_URL).content(payload);
        } else {
            mockHttpServletRequestBuilder = multipart(PUBLICATION_URL).file(file);
        }
        mockHttpServletRequestBuilder.header(PublicationConfiguration.TYPE_HEADER, ARTEFACT_TYPE);
        mockHttpServletRequestBuilder.header(PublicationConfiguration.SENSITIVITY_HEADER, SENSITIVITY);
        mockHttpServletRequestBuilder.header(PublicationConfiguration.LANGUAGE_HEADER, LANGUAGE);
        mockHttpServletRequestBuilder.header(PublicationConfiguration.PROVENANCE_HEADER, PROVENANCE);
        mockHttpServletRequestBuilder.header(PublicationConfiguration.SOURCE_ARTEFACT_ID_HEADER, SOURCE_ARTEFACT_ID);
        mockHttpServletRequestBuilder.header(PublicationConfiguration.DISPLAY_TO_HEADER, DISPLAY_TO);
        mockHttpServletRequestBuilder.header(PublicationConfiguration.DISPLAY_FROM_HEADER, DISPLAY_FROM);
        mockHttpServletRequestBuilder.header(PublicationConfiguration.LIST_TYPE, LIST_TYPE);
        mockHttpServletRequestBuilder.header(PublicationConfiguration.COURT_ID, PROVENANCE_COURT_ID_DOESNOT_MATCH);
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
        assertEquals(artefact.getLocationId(), String.format("NoMatch%s", PROVENANCE_COURT_ID_DOESNOT_MATCH),
                     "Provenance court Id match with reference data");


        Map<String, List<Object>> searchResult = artefact.getSearch();
        assertTrue(
            searchResult.containsKey(isJson ? SEARCH_KEY_FOUND : LOCATION_ID_SEARCH_KEY),
            "Returned search result does not contain the correct key"
        );
        assertFalse(searchResult.containsKey(SEARCH_KEY_NOT_FOUND), "Returned search result contains "
            + "key that does not exist");
        assertEquals(
            isJson ? SEARCH_VALUE_1 : PROVENANCE_COURT_ID_DOESNOT_MATCH,
            searchResult.get(isJson ? SEARCH_KEY_FOUND : LOCATION_ID_SEARCH_KEY).get(0),
            "Does not contain first value in the array"
        );

        if (isJson) {
            assertEquals(SEARCH_VALUE_2, searchResult.get(SEARCH_KEY_FOUND).get(1),
                         "Does not contain second value in the array"
            );
        }

        assertTrue(artefact.getPayload().startsWith(BLOB_PAYLOAD_URL), "Payload is not as expected");
    }
}
