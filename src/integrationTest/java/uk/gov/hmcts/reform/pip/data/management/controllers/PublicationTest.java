package uk.gov.hmcts.reform.pip.data.management.controllers;

import com.azure.core.util.BinaryData;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.microsoft.applicationinsights.web.dependencies.apachecommons.io.IOUtils;
import io.zonky.test.db.AutoConfigureEmbeddedDatabase;
import org.json.JSONArray;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
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
import uk.gov.hmcts.reform.pip.data.management.config.AzureBlobConfigurationTestConfiguration;
import uk.gov.hmcts.reform.pip.data.management.config.PublicationConfiguration;
import uk.gov.hmcts.reform.pip.data.management.errorhandling.ExceptionResponse;
import uk.gov.hmcts.reform.pip.data.management.models.publication.Artefact;
import uk.gov.hmcts.reform.pip.data.management.utils.IntegrationTestBase;
import uk.gov.hmcts.reform.pip.model.account.PiUser;
import uk.gov.hmcts.reform.pip.model.location.LocationType;
import uk.gov.hmcts.reform.pip.model.publication.ArtefactType;
import uk.gov.hmcts.reform.pip.model.publication.Language;
import uk.gov.hmcts.reform.pip.model.publication.ListType;
import uk.gov.hmcts.reform.pip.model.publication.Sensitivity;
import uk.gov.hmcts.reform.pip.model.report.PublicationMiData;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import static org.junit.Assert.fail;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.pip.model.account.Roles.SYSTEM_ADMIN;

@SpringBootTest(classes = {Application.class, AzureBlobConfigurationTestConfiguration.class},
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("integration")
@SuppressWarnings({"PMD.ExcessiveImports", "PMD.CyclomaticComplexity", "PMD.TooManyMethods",
    "PMD.CouplingBetweenObjects"})
@AutoConfigureEmbeddedDatabase(type = AutoConfigureEmbeddedDatabase.DatabaseType.POSTGRES)
@WithMockUser(username = "admin", authorities = {"APPROLE_api.request.admin"})
class PublicationTest extends IntegrationTestBase {
    private static final String PUBLICATION_URL = "/publication";
    private static final String SEARCH_COURT_URL = "/publication/locationId";
    private static final String FILE_URL = "/file";
    private static final String PAYLOAD_URL = "/payload";
    private static final String LOCATION_TYPE_URL = PUBLICATION_URL + "/location-type/";
    private static final String SEND_NEW_ARTEFACTS_FOR_SUBSCRIPTION_URL = PUBLICATION_URL + "/latest/subscription";
    private static final String COUNT_ENDPOINT = PUBLICATION_URL + "/count-by-location";
    private static final String REPORT_NO_MATCH_ARTEFACTS_URL = PUBLICATION_URL + "/no-match/reporting";
    private static final String MI_REPORTING_DATA_URL = PUBLICATION_URL + "/mi-data";
    private static final String MI_REPORTING_DATA_URL_V2 = PUBLICATION_URL + "/v2/mi-data";
    private static final String ARCHIVE_EXPIRED_ARTEFACTS_URL = PUBLICATION_URL + "/expired";
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
    private static final String COURT_ID = "123";
    private static final LocalDateTime CONTENT_DATE = LocalDateTime.now().toLocalDate().atStartOfDay()
        .truncatedTo(ChronoUnit.SECONDS);
    private static final String SEARCH_KEY_FOUND = "array-value";
    private static final String SEARCH_KEY_NOT_FOUND = "case-urn";
    private static final String SEARCH_VALUE_1 = "array-value-1";
    private static final String SEARCH_VALUE_2 = "array-value-2";
    private static final String USER_ID_HEADER = "x-user-id";

    private static final String LOCATION_ID_SEARCH_KEY = "location-id";
    private static final String TRUE = "true";
    private static final String FALSE = "false";
    private static final String ADMIN_HEADER = "x-admin";
    private static final String ISSUER_HEADER = "x-issuer-id";
    private static final String EMAIL = "test@email.com";

    private static final String VALIDATION_EMPTY_RESPONSE = "Response should contain a Artefact";
    private static final String VALIDATION_DISPLAY_FROM = "The expected Display From has not been returned";
    private static final String VALIDATION_MI_REPORT = "Should successfully retrieve MI data";
    private static final String SHOULD_RETURN_EXPECTED_ARTEFACT = "Should return expected artefact";
    private static final String ARTEFACT_ID_POPULATED_MESSAGE = "Artefact ID should be populated";
    private static final String EXPECTED_MI_DATA_HEADERS = "artefact_id,display_from,display_to,language,provenance,"
        + "sensitivity,source_artefact_id,"
        + "superseded_count,type,content_date,court_id,court_name,list_type";

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private static String payload = "payload";
    private static MockMultipartFile file;

    @Autowired
    private MockMvc mockMvc;

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

        OBJECT_MAPPER.findAndRegisterModules();
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

            return OBJECT_MAPPER.readValue(
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

            return OBJECT_MAPPER.readValue(
                response.getResponse().getContentAsString(), Artefact.class);
        }
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    @DisplayName("Should create a valid artefact and return the created artefact to the user")
    void creationOfAValidArtefact(boolean isJson) throws Exception {
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
            .contentType(isJson ? MediaType.APPLICATION_JSON : MediaType.MULTIPART_FORM_DATA);

        MvcResult response = mockMvc.perform(mockHttpServletRequestBuilder).andExpect(status().isCreated()).andReturn();

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
        Artefact artefact = createSscsDailyList(Sensitivity.PUBLIC, "TestProvenance");

        assertNotNull(artefact.getArtefactId(), "Artefact IDs do not match");
        assertEquals(artefact.getType(), ARTEFACT_TYPE, "Artefact types do not match");
        assertEquals(artefact.getProvenance(), "TestProvenance", "Provenances do not match");
        assertEquals(artefact.getLocationId(), "NoMatch" + COURT_ID, "Court ids do not match");
        assertEquals(artefact.getListType(), ListType.SSCS_DAILY_LIST, "List types do not match");
        assertEquals(artefact.getContentDate(), CONTENT_DATE, "Content dates do not match");
    }

    @DisplayName("Should create a valid artefact with only mandatory fields")
    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void creationOfAValidArtefactWithOnlyMandatoryFields(boolean isJson) throws Exception {
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

    @DisplayName("Should throw a 400 bad request when payload is not of JSON through the JSON endpoint")
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
            .content(PAYLOAD_UNKNOWN)
            .contentType(MediaType.APPLICATION_JSON);


        mockMvc.perform(mockHttpServletRequestBuilder).andExpect(status().isBadRequest()).andReturn();
    }

    @DisplayName("Check that null date for general_publication still allows us to return the relevant artefact")
    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void checkStatusUpdatesWithNullDateTo(boolean isJson) throws Exception {
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
            .contentType(isJson ? MediaType.APPLICATION_JSON : MediaType.MULTIPART_FORM_DATA);

        mockMvc.perform(mockHttpServletRequestBuilder).andExpect(status().isCreated()).andReturn();

        MockHttpServletRequestBuilder mockHttpServletRequestBuilder1 = MockMvcRequestBuilders
            .get(SEARCH_COURT_URL + "/" + COURT_ID)
            .header(USER_ID_HEADER, USER_ID);
        MvcResult getResponse =
            mockMvc.perform(mockHttpServletRequestBuilder1).andExpect(status().isOk()).andReturn();

        String jsonOutput = getResponse.getResponse().getContentAsString();
        JSONArray jsonArray = new JSONArray(jsonOutput);
        Artefact retrievedArtefact = OBJECT_MAPPER.readValue(
            jsonArray.get(0).toString(), Artefact.class
        );

        assertEquals(COURT_ID, retrievedArtefact.getLocationId(), "Artefact not found.");
    }

    @DisplayName("Should create a valid artefact and return the created artefact to the user")
    @Test
    void testCreationOfValidDailyCauseList() throws Exception {
        try (InputStream mockFile = this.getClass().getClassLoader()
            .getResourceAsStream("data/civil-daily-cause-list/civilDailyCauseList.json")) {

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
                .content(mockFile.readAllBytes())
                .contentType(MediaType.APPLICATION_JSON);


            MvcResult response = mockMvc.perform(mockHttpServletRequestBuilder)
                .andExpect(status().isCreated()).andReturn();

            assertNotNull(response.getResponse().getContentAsString(), VALIDATION_EMPTY_RESPONSE);

            Artefact artefact = OBJECT_MAPPER.readValue(response.getResponse().getContentAsString(), Artefact.class);
            assertNotNull(artefact.getArtefactId(), ARTEFACT_ID_POPULATED_MESSAGE);
        }
    }

    @DisplayName("Should create a valid artefact and return the created artefact to the user")
    @Test
    void testCreationOfInvalidDailyCauseList() throws Exception {
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

    @DisplayName("Verify that artefact is returned when user is verified and sensitivity is public")
    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void verifyThatArtefactsAreReturnedForVerifiedUserWhenPublic(boolean isJson) throws Exception {
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
            .header(USER_ID_HEADER, USER_ID);
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
            .header(USER_ID_HEADER, USER_ID);
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
    void testGetCourtByIdShowsAllCourtsForAdmin() throws Exception {
        Artefact inDateArtefact = createDailyList(Sensitivity.PUBLIC);
        Artefact futureArtefact = createDailyList(Sensitivity.PUBLIC, DISPLAY_FROM.plusMonths(1),
                                                  CONTENT_DATE.plusDays(1)
        );

        assertEquals(inDateArtefact.getDisplayFrom(), DISPLAY_FROM.minusMonths(2),
                     VALIDATION_DISPLAY_FROM
        );
        assertEquals(futureArtefact.getDisplayFrom(), DISPLAY_FROM.plusMonths(1),
                     VALIDATION_DISPLAY_FROM
        );

        MockHttpServletRequestBuilder mockHttpServletRequestBuilder = MockMvcRequestBuilders
            .get(SEARCH_COURT_URL + "/" + COURT_ID)
            .header(ADMIN_HEADER, FALSE);

        MvcResult nonAdminResponse =
            mockMvc.perform(mockHttpServletRequestBuilder).andExpect(status().isOk()).andReturn();

        MockHttpServletRequestBuilder mockHttpServletRequestBuilder1 = MockMvcRequestBuilders
            .get(SEARCH_COURT_URL + "/" + COURT_ID)
            .header(ADMIN_HEADER, TRUE);

        MvcResult adminResponse =
            mockMvc.perform(mockHttpServletRequestBuilder1).andExpect(status().isOk()).andReturn();

        JSONArray nonAdminResults = new JSONArray(nonAdminResponse.getResponse().getContentAsString());
        JSONArray adminResults = new JSONArray(adminResponse.getResponse().getContentAsString());
        assertEquals(1, nonAdminResults.length(), "Should return 1 artefact for non admin");
        assertEquals(2, adminResults.length(), "Should return 2 artefacts for admins");

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
    void testGetLocationTypeReturns() throws Exception {
        MockHttpServletRequestBuilder mockHttpServletRequestBuilder = MockMvcRequestBuilders
            .get(LOCATION_TYPE_URL + ListType.CIVIL_DAILY_CAUSE_LIST);

        MvcResult response = mockMvc.perform(mockHttpServletRequestBuilder).andExpect(status().isOk()).andReturn();

        assertTrue(
            response.getResponse().getContentAsString().contains(LocationType.VENUE.name()),
            "Location types should match"
        );
    }

    @Test
    void testGetLocationTypeReturnsBadRequest() throws Exception {
        MockHttpServletRequestBuilder mockHttpServletRequestBuilder =
            MockMvcRequestBuilders.get(LOCATION_TYPE_URL + "invalid");

        mockMvc.perform(mockHttpServletRequestBuilder).andExpect(status().isBadRequest());
    }

    @Test
    void testCountByLocationManualUpload() throws Exception {
        createDailyList(Sensitivity.PRIVATE);
        MockHttpServletRequestBuilder mockHttpServletRequestBuilder = MockMvcRequestBuilders.get(COUNT_ENDPOINT);
        MvcResult result = mockMvc.perform(mockHttpServletRequestBuilder)
            .andExpect(status().isOk())
            .andReturn();
        assertTrue(result.getResponse().getContentAsString().contains(COURT_ID), "headers not found");
    }

    @Test
    void testCountByLocationListAssist() throws Exception {
        MockHttpServletRequestBuilder mockHttpServletRequestBuilder = MockMvcRequestBuilders.get(COUNT_ENDPOINT);
        MvcResult result = mockMvc.perform(mockHttpServletRequestBuilder)
            .andExpect(status().isOk())
            .andReturn();
        assertEquals(HttpStatus.OK.value(), result.getResponse().getStatus(),
                     "CountByLocation endpoint for ListAssist is not successful"
        );
    }

    @Test
    void testSendNewArtefactsForSubscriptionSuccess() throws Exception {
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
            .post(SEND_NEW_ARTEFACTS_FOR_SUBSCRIPTION_URL);

        mockMvc.perform(request).andExpect(status().isNoContent());
    }

    @Test
    void testReportNoMatchArtefactsSuccess() throws Exception {
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
            .post(REPORT_NO_MATCH_ARTEFACTS_URL);

        mockMvc.perform(request).andExpect(status().isNoContent());
    }

    @Test
    void testArchiveExpiredArtefactsSuccess() throws Exception {
        Artefact artefactToExpire = createDailyList(Sensitivity.PUBLIC, DISPLAY_FROM.minusMonths(9),
                                                    DISPLAY_FROM.minusMonths(6),
                                                    DISPLAY_FROM.minusMonths(10), PROVENANCE
        );

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
    void testGetMiDataRequestSuccess() throws Exception {
        Artefact artefact = createDailyList(Sensitivity.PUBLIC);

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
            .get(MI_REPORTING_DATA_URL);

        String responseMiData = mockMvc.perform(request).andExpect(status().isOk()).andReturn()
            .getResponse().getContentAsString();

        assertEquals(EXPECTED_MI_DATA_HEADERS, responseMiData.split("\n")[0],
                     "Should successfully retrieve MI data headers"
        );
        assertTrue(
            responseMiData.contains(artefact.getArtefactId().toString()),
            "Should successfully retrieve MI data"
        );
        assertTrue(
            responseMiData.contains(artefact.getLocationId()),
            "Should successfully retrieve MI data"
        );
    }

    @Test
    void testGetMiDataV2Success() throws Exception {
        Artefact artefact = createDailyList(Sensitivity.PUBLIC);

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
            .get(MI_REPORTING_DATA_URL_V2);

        MvcResult responseMiData = mockMvc.perform(request).andExpect(status().isOk()).andReturn();
        assertNotNull(responseMiData.getResponse(), VALIDATION_MI_REPORT);

        List<PublicationMiData> miData  =
            Arrays.asList(
                OBJECT_MAPPER.readValue(responseMiData.getResponse().getContentAsString(), PublicationMiData[].class)
            );

        assertTrue(miData.stream().anyMatch(data -> data.getArtefactId().equals(artefact.getArtefactId())),
                     VALIDATION_MI_REPORT);
        assertTrue(miData.stream().anyMatch(data -> data.getLocationId().equals(artefact.getLocationId())),
                     VALIDATION_MI_REPORT);
    }

    @Test
    void testGetMiDataV2SuccessWithNoMatch() throws Exception {
        Artefact artefactNoMatch = createSscsDailyList(Sensitivity.PUBLIC, "TestProvenance");

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
            .get(MI_REPORTING_DATA_URL_V2);

        MvcResult responseMiData = mockMvc.perform(request).andExpect(status().isOk()).andReturn();
        assertNotNull(responseMiData.getResponse(), VALIDATION_MI_REPORT);

        List<PublicationMiData> miData  =
            Arrays.asList(
                OBJECT_MAPPER.readValue(responseMiData.getResponse().getContentAsString(), PublicationMiData[].class)
            );

        assertTrue(miData.stream().anyMatch(data -> data.getArtefactId().equals(artefactNoMatch.getArtefactId())),
                   VALIDATION_MI_REPORT);
        assertTrue(miData.stream().anyMatch(data -> data.getLocationId().equals(artefactNoMatch.getLocationId())),
                   VALIDATION_MI_REPORT);
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

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
    void testGetAllNoMatchArtefacts() throws Exception {
        Artefact expectedArtefact = createDailyList(Sensitivity.PRIVATE, DISPLAY_FROM.minusMonths(2), DISPLAY_TO,
                                                    CONTENT_DATE, "NoMatch"
        );

        MockHttpServletRequestBuilder mockHttpServletRequestBuilder =
            MockMvcRequestBuilders.get("/publication/no-match");

        MvcResult response = mockMvc.perform(mockHttpServletRequestBuilder).andExpect(status().isOk()).andReturn();

        String jsonOutput = response.getResponse().getContentAsString();
        JSONArray jsonArray = new JSONArray(jsonOutput);
        Artefact returnedArtefact = OBJECT_MAPPER.readValue(
            jsonArray.get(0).toString(), Artefact.class
        );

        assertTrue(
            compareArtefacts(expectedArtefact, returnedArtefact),
            "Expected and returned artefacts do not match"
        );
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

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
    void testDeleteArtefactsByLocation() throws Exception {
        PiUser piUser = new PiUser();
        piUser.setUserId(USER_ID);
        piUser.setEmail(EMAIL);

        when(accountManagementService.getUserById(USER_ID)).thenReturn(piUser);
        when(accountManagementService.getAllAccounts(anyString(), eq(SYSTEM_ADMIN.toString())))
            .thenReturn(List.of(EMAIL));

        Artefact artefactToDelete = createDailyList(Sensitivity.PUBLIC);

        MockHttpServletRequestBuilder preDeleteRequest = MockMvcRequestBuilders
            .get(PUBLICATION_URL + "/" + artefactToDelete.getArtefactId())
            .header(USER_ID_HEADER, USER_ID);

        mockMvc.perform(preDeleteRequest).andExpect(status().isOk());

        MockHttpServletRequestBuilder deleteRequest = MockMvcRequestBuilders
            .delete(PUBLICATION_URL + "/" + COURT_ID + "/deleteArtefacts")
            .header(USER_ID_HEADER, USER_ID);

        MvcResult deleteResponse = mockMvc.perform(deleteRequest).andExpect(status().isOk()).andReturn();

        assertEquals("Total 1 artefact deleted for location id " + COURT_ID,
                     deleteResponse.getResponse().getContentAsString(), "Should successfully delete artefact"
        );
    }

    @Test
    void testDeleteArtefactsByLocationNotFound() throws Exception {
        MockHttpServletRequestBuilder deleteRequest = MockMvcRequestBuilders
            .delete(PUBLICATION_URL + "/" + 11 + "/deleteArtefacts")
            .header(USER_ID_HEADER, USER_ID);

        MvcResult deleteResponse = mockMvc.perform(deleteRequest).andExpect(status().isNotFound()).andReturn();

        assertTrue(
            deleteResponse.getResponse().getContentAsString()
                .contains("No artefacts found with the location ID " + 11),
            "Artefact not found error message"
        );
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
