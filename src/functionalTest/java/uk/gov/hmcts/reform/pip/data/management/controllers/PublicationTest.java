package uk.gov.hmcts.reform.pip.data.management.controllers;

import com.azure.core.util.BinaryData;
import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.applicationinsights.web.dependencies.apachecommons.io.IOUtils;
import io.zonky.test.db.AutoConfigureEmbeddedDatabase;
import org.json.JSONArray;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
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
import uk.gov.hmcts.reform.pip.data.management.errorhandling.ExceptionResponse;
import uk.gov.hmcts.reform.pip.data.management.models.publication.Artefact;
import uk.gov.hmcts.reform.pip.model.location.LocationType;
import uk.gov.hmcts.reform.pip.model.publication.ArtefactType;
import uk.gov.hmcts.reform.pip.model.publication.Language;
import uk.gov.hmcts.reform.pip.model.publication.ListType;
import uk.gov.hmcts.reform.pip.model.publication.Sensitivity;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.junit.Assert.fail;
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
@ActiveProfiles(profiles = "functional")
@SuppressWarnings({"PMD.ExcessiveImports", "PMD.ExcessiveClassLength",
    "PMD.CyclomaticComplexity", "PMD.TooManyMethods", "PMD.LawOfDemeter"})
@AutoConfigureEmbeddedDatabase(type = AutoConfigureEmbeddedDatabase.DatabaseType.POSTGRES)
@WithMockUser(username = "admin", authorities = {"APPROLE_api.request.admin"})
class PublicationTest {

    @Autowired
    BlobContainerClient blobContainerClient;

    @Autowired
    BlobClient blobClient;

    @Autowired
    private MockMvc mockMvc;

    @Value("${test-user-id}")
    private String userId;

    @Value("${system-admin-provenance-id}")
    private String systemAdminProvenanceId;

    private static final String PUBLICATION_URL = "/publication";

    private static final String SEARCH_COURT_URL = "/publication/locationId";
    private static final String PAYLOAD_URL = "/payload";
    private static final String LOCATION_TYPE_URL = PUBLICATION_URL + "/location-type/";
    private static final String SEND_NEW_ARTEFACTS_FOR_SUBSCRIPTION_URL = PUBLICATION_URL + "/latest/subscription";
    public static final String COUNT_ENDPOINT = PUBLICATION_URL + "/count-by-location";
    private static final String REPORT_NO_MATCH_ARTEFACTS_URL = PUBLICATION_URL + "/no-match/reporting";
    private static final String MI_REPORTING_DATA_URL = PUBLICATION_URL + "/mi-data";
    private static final String ARCHIVE_EXPIRED_ARTEFACTS_URL = PUBLICATION_URL + "/expired";
    private static final ArtefactType ARTEFACT_TYPE = ArtefactType.LIST;
    private static final Sensitivity SENSITIVITY = Sensitivity.PUBLIC;
    private static final String PROVENANCE = "MANUAL_UPLOAD";
    private static String payload = "payload";
    private static MockMultipartFile file;
    private static final String PAYLOAD_UNKNOWN = "Unknown-Payload";
    private static final String SOURCE_ARTEFACT_ID = "sourceArtefactId";
    private static final LocalDateTime DISPLAY_TO = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
    private static final LocalDateTime DISPLAY_FROM = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
    private static final Language LANGUAGE = Language.ENGLISH;
    private static final String BLOB_PAYLOAD_URL = "https://localhost";
    private static final ListType LIST_TYPE = ListType.CIVIL_DAILY_CAUSE_LIST;
    private static final String COURT_ID = "123";
    private static final LocalDateTime CONTENT_DATE = LocalDateTime.now().toLocalDate().atStartOfDay()
        .truncatedTo(ChronoUnit.SECONDS);
    private static final String PROVENANCE_USER_ID = "x-provenance-user-id";
    private static final String SEARCH_KEY_FOUND = "array-value";
    private static final String SEARCH_KEY_NOT_FOUND = "case-urn";
    private static final String SEARCH_VALUE_1 = "array-value-1";
    private static final String SEARCH_VALUE_2 = "array-value-2";
    private static final String USER_ID_HEADER = "x-user-id";
    private static final String UNAUTHORIZED_USERNAME = "unauthorized_username";
    private static final String UNAUTHORIZED_ROLE = "APPROLE_unknown.role";

    private static final String LOCATION_ID_SEARCH_KEY = "location-id";
    private static final String FORBIDDEN_STATUS_CODE = "Status code does not match forbidden";
    private static final String TRUE = "true";
    private static final String FALSE = "false";
    private static final String ADMIN_HEADER = "x-admin";
    private static final String ISSUER_HEADER = "x-issuer-id";
    private static final String EMAIL = "test@email.com";

    private static final String VALIDATION_EMPTY_RESPONSE = "Response should contain a Artefact";
    private static final String VALIDATION_DISPLAY_FROM = "The expected Display From has not been returned";
    private static final String SHOULD_RETURN_EXPECTED_ARTEFACT = "Should return expected artefact";
    private static final String PARTIES_KEY = "parties";
    private static final String ORGANISATION_KEY = "organisations";
    private static final String INDIVIDUAL_KEY = "individuals";
    private static final String CASES_KEY = "cases";
    private static final String EXPECTED_MI_DATA_HEADERS = "artefact_id,display_from,display_to,language,provenance,"
        + "sensitivity,source_artefact_id,"
        + "superseded_count,type,content_date,court_id,court_name,list_type";

    private static MockHttpServletRequestBuilder mockHttpServletRequestBuilder;
    private static ObjectMapper objectMapper;

    @BeforeAll
    public static void setup() throws IOException {
        file = new MockMultipartFile("file", "test.pdf",
                                     MediaType.APPLICATION_PDF_VALUE, "test content".getBytes(
            StandardCharsets.UTF_8)
        );

        try (InputStream is = PublicationTest.class.getClassLoader()
            .getResourceAsStream("data/artefact.json")) {
            payload = new String(IOUtils.toByteArray(
                Objects.requireNonNull(is)));
        }

        objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();
    }

    Artefact createDailyList(Sensitivity sensitivity) throws Exception {
        return this.createDailyList(sensitivity, DISPLAY_FROM.minusMonths(2), CONTENT_DATE);
    }

    Artefact createDailyList(Sensitivity sensitivity, LocalDateTime displayFrom, LocalDateTime contentDate)
        throws Exception {
        return this.createDailyList(sensitivity, displayFrom, DISPLAY_TO, contentDate, PROVENANCE);
    }

    Artefact createDailyList(Sensitivity sensitivity, LocalDateTime displayFrom, LocalDateTime displayTo,
                             LocalDateTime contentDate,
                             String provenance)
        throws Exception {
        try (InputStream mockFile = this.getClass().getClassLoader()
            .getResourceAsStream("data/civil-daily-cause-list/civilDailyCauseList.json")) {

            MockHttpServletRequestBuilder mockHttpServletRequestBuilder = MockMvcRequestBuilders
                .post(PUBLICATION_URL)
                .header(PublicationConfiguration.TYPE_HEADER, ARTEFACT_TYPE)
                .header(PublicationConfiguration.PROVENANCE_HEADER, provenance)
                .header(PublicationConfiguration.SOURCE_ARTEFACT_ID_HEADER, SOURCE_ARTEFACT_ID)
                .header(PublicationConfiguration.DISPLAY_FROM_HEADER, displayFrom)
                .header(PublicationConfiguration.DISPLAY_TO_HEADER, displayTo.plusMonths(1))
                .header(PublicationConfiguration.COURT_ID, COURT_ID)
                .header(PublicationConfiguration.LIST_TYPE, ListType.CIVIL_DAILY_CAUSE_LIST)
                .header(PublicationConfiguration.CONTENT_DATE, contentDate)
                .header(PublicationConfiguration.SENSITIVITY_HEADER, sensitivity)
                .header(PublicationConfiguration.LANGUAGE_HEADER, LANGUAGE)
                .content(mockFile.readAllBytes())
                .contentType(MediaType.APPLICATION_JSON);

            MvcResult response = mockMvc.perform(mockHttpServletRequestBuilder)
                .andExpect(status().isCreated()).andReturn();

            return objectMapper.readValue(
                response.getResponse().getContentAsString(), Artefact.class);
        }
    }

    Artefact createSscsDailyList(Sensitivity sensitivity, String provenance)
        throws Exception {
        try (InputStream mockFile = this.getClass().getClassLoader()
            .getResourceAsStream("data/sscs-daily-list/sscsDailyList.json")) {

            MockHttpServletRequestBuilder mockHttpServletRequestBuilder = MockMvcRequestBuilders
                .post(PUBLICATION_URL)
                .header(PublicationConfiguration.TYPE_HEADER, ARTEFACT_TYPE)
                .header(PublicationConfiguration.PROVENANCE_HEADER, provenance)
                .header(PublicationConfiguration.SOURCE_ARTEFACT_ID_HEADER, SOURCE_ARTEFACT_ID)
                .header(PublicationConfiguration.DISPLAY_FROM_HEADER, DISPLAY_FROM.minusMonths(2))
                .header(PublicationConfiguration.DISPLAY_TO_HEADER, DISPLAY_TO.plusMonths(1))
                .header(PublicationConfiguration.COURT_ID, COURT_ID)
                .header(PublicationConfiguration.LIST_TYPE, ListType.SSCS_DAILY_LIST)
                .header(PublicationConfiguration.CONTENT_DATE, CONTENT_DATE)
                .header(PublicationConfiguration.SENSITIVITY_HEADER, sensitivity)
                .header(PublicationConfiguration.LANGUAGE_HEADER, LANGUAGE)
                .content(mockFile.readAllBytes())
                .contentType(MediaType.APPLICATION_JSON);

            MvcResult response = mockMvc.perform(mockHttpServletRequestBuilder)
                .andExpect(status().isCreated()).andReturn();

            return objectMapper.readValue(
                response.getResponse().getContentAsString(), Artefact.class);
        }
    }

    private void setupPublicationUploadRequest(boolean isJson) {
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
            .contentType(isJson ? MediaType.APPLICATION_JSON : MediaType.MULTIPART_FORM_DATA);

        when(blobContainerClient.getBlobClient(any())).thenReturn(blobClient);
        when(blobContainerClient.getBlobContainerUrl()).thenReturn(BLOB_PAYLOAD_URL);
    }


//    @ParameterizedTest
//    @ValueSource(booleans = {true, false})
//    @DisplayName("Should create a valid artefact and return the created artefact to the user")
//    void creationOfAValidArtefact(boolean isJson) throws Exception {
//        setupPublicationUploadRequest(isJson);
//
//        MvcResult response = mockMvc.perform(mockHttpServletRequestBuilder).andExpect(status().isCreated()).andReturn();
//
//        assertNotNull(response.getResponse().getContentAsString(), VALIDATION_EMPTY_RESPONSE);
//
//        Artefact artefact = objectMapper.readValue(
//            response.getResponse().getContentAsString(), Artefact.class);
//
//        assertNotNull(artefact.getArtefactId(), "Artefact ID is not populated");
//        assertEquals(artefact.getSourceArtefactId(), SOURCE_ARTEFACT_ID, "Source artefact ID "
//            + "does not match input source artefact id");
//        assertEquals(artefact.getType(), ARTEFACT_TYPE, "Artefact type does not match input artefact type");
//        assertEquals(artefact.getDisplayFrom(), DISPLAY_FROM, "Display from does not match input display from");
//        assertEquals(artefact.getDisplayTo(), DISPLAY_TO, "Display to does not match input display to");
//        assertEquals(artefact.getProvenance(), PROVENANCE, "Provenance does not match input provenance");
//        assertEquals(artefact.getLanguage(), LANGUAGE, "Language does not match input language");
//        assertEquals(artefact.getSensitivity(), SENSITIVITY, "Sensitivity does not match input sensitivity");
//
//
//        Map<String, List<Object>> searchResult = artefact.getSearch();
//        assertTrue(
//            searchResult.containsKey(isJson ? SEARCH_KEY_FOUND : LOCATION_ID_SEARCH_KEY),
//            "Returned search result does not contain the correct key"
//        );
//        assertFalse(searchResult.containsKey(SEARCH_KEY_NOT_FOUND), "Returned search result contains "
//            + "key that does not exist");
//        assertEquals(
//            isJson ? SEARCH_VALUE_1 : COURT_ID,
//            searchResult.get(isJson ? SEARCH_KEY_FOUND : LOCATION_ID_SEARCH_KEY).get(0),
//            "Does not contain first value in the array"
//        );
//
//        if (isJson) {
//            assertEquals(SEARCH_VALUE_2, searchResult.get(SEARCH_KEY_FOUND).get(1),
//                         "Does not contain second value in the array"
//            );
//        }
//    }

    @Test
    void creationOfAValidArtefact() throws Exception {
        setupPublicationUploadRequest(true);
        ExecutorService threads = Executors.newFixedThreadPool(2);
        List<Callable<MvcResult>> torun = new ArrayList<>(2);
        for (int i = 0; i < 2; i++) {
            torun.add(() -> mockMvc.perform(mockHttpServletRequestBuilder).andReturn());
        }

        List<Future<MvcResult>> futures = threads.invokeAll(torun);

        threads.shutdown();

        for (Future<MvcResult> fut : futures) {
            MvcResult response = fut.get();
            assertNotNull(response.getResponse().getContentAsString(), VALIDATION_EMPTY_RESPONSE);
        }


//        List<Future<MvcResult>> futures = threads.invokeAll(torun);
//
//        MvcResult response = mockMvc.perform(mockHttpServletRequestBuilder).andExpect(status().isCreated()).andReturn();
//
//        assertNotNull(response.getResponse().getContentAsString(), VALIDATION_EMPTY_RESPONSE);
//
//        Artefact artefact = objectMapper.readValue(
//            response.getResponse().getContentAsString(), Artefact.class);
//
//        assertNotNull(artefact.getArtefactId(), "Artefact ID is not populated");
//        assertEquals(artefact.getSourceArtefactId(), SOURCE_ARTEFACT_ID, "Source artefact ID "
//            + "does not match input source artefact id");
//        assertEquals(artefact.getType(), ARTEFACT_TYPE, "Artefact type does not match input artefact type");
//        assertEquals(artefact.getDisplayFrom(), DISPLAY_FROM, "Display from does not match input display from");
//        assertEquals(artefact.getDisplayTo(), DISPLAY_TO, "Display to does not match input display to");
//        assertEquals(artefact.getProvenance(), PROVENANCE, "Provenance does not match input provenance");
//        assertEquals(artefact.getLanguage(), LANGUAGE, "Language does not match input language");
//        assertEquals(artefact.getSensitivity(), SENSITIVITY, "Sensitivity does not match input sensitivity");
//
//
//        Map<String, List<Object>> searchResult = artefact.getSearch();
//        assertTrue(
//            searchResult.containsKey(SEARCH_KEY_FOUND),
//            "Returned search result does not contain the correct key"
//        );
//        assertFalse(searchResult.containsKey(SEARCH_KEY_NOT_FOUND), "Returned search result contains "
//            + "key that does not exist");
//        assertEquals(SEARCH_VALUE_1,
//            searchResult.get(SEARCH_KEY_FOUND).get(0),
//            "Does not contain first value in the array"
//        );
//
//        assertEquals(SEARCH_VALUE_2, searchResult.get(SEARCH_KEY_FOUND).get(1),
//                     "Does not contain second value in the array"
//        );
    }

    private boolean compareArtefacts(Artefact expectedArtefact, Artefact returnedArtefact) {
        return expectedArtefact.getArtefactId().equals(returnedArtefact.getArtefactId())
            && expectedArtefact.getProvenance().equals(returnedArtefact.getProvenance())
            && expectedArtefact.getSensitivity().equals(returnedArtefact.getSensitivity())
            && expectedArtefact.getPayload().equals(returnedArtefact.getPayload())
            && expectedArtefact.getType().equals(returnedArtefact.getType())
            && expectedArtefact.getSearch().equals(returnedArtefact.getSearch())
            && expectedArtefact.getLocationId().equals(returnedArtefact.getLocationId())
            && expectedArtefact.getLanguage().equals(returnedArtefact.getLanguage())
            && expectedArtefact.getListType().equals(returnedArtefact.getListType())
            && expectedArtefact.getDisplayTo().equals(returnedArtefact.getDisplayTo())
            && expectedArtefact.getDisplayFrom().equals(returnedArtefact.getDisplayFrom())
            && expectedArtefact.getContentDate().equals(returnedArtefact.getContentDate())
            && expectedArtefact.getIsFlatFile().equals(returnedArtefact.getIsFlatFile())
            && expectedArtefact.getSourceArtefactId().equals(returnedArtefact.getSourceArtefactId());
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
}
