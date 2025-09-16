package uk.gov.hmcts.reform.pip.data.management.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.applicationinsights.web.dependencies.apachecommons.io.IOUtils;
import io.zonky.test.db.AutoConfigureEmbeddedDatabase;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import uk.gov.hmcts.reform.pip.data.management.Application;
import uk.gov.hmcts.reform.pip.data.management.config.PublicationConfiguration;
import uk.gov.hmcts.reform.pip.data.management.models.location.Location;
import uk.gov.hmcts.reform.pip.data.management.utils.IntegrationTestBase;
import uk.gov.hmcts.reform.pip.model.account.PiUser;
import uk.gov.hmcts.reform.pip.model.publication.ArtefactType;
import uk.gov.hmcts.reform.pip.model.publication.Language;
import uk.gov.hmcts.reform.pip.model.publication.ListType;
import uk.gov.hmcts.reform.pip.model.publication.Sensitivity;

import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Objects;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.pip.model.account.Roles.SYSTEM_ADMIN;

@SpringBootTest(classes = {Application.class}, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("integration")
@AutoConfigureEmbeddedDatabase(type = AutoConfigureEmbeddedDatabase.DatabaseType.POSTGRES)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@WithMockUser(username = "admin", authorities = {"APPROLE_api.request.admin"})
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SuppressWarnings("PMD.ExcessiveImports")
class TestingSupportApiTest extends IntegrationTestBase {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private static final String TESTING_SUPPORT_BASE_URL = "/testing-support/";
    private static final String TESTING_SUPPORT_LOCATION_URL = TESTING_SUPPORT_BASE_URL + "location/";
    private static final String TESTING_SUPPORT_PUBLICATION_URL = TESTING_SUPPORT_BASE_URL + "publication/";

    private static final String LOCATIONS_URL = "/locations/";
    private static final String PUBLICATION_URL = "/publication";
    private static final String PUBLICATION_BY_LOCATION_ID_URL = PUBLICATION_URL + "/locationId/";

    private static final Integer LOCATION_ID = 123;
    private static final Integer LOCATION_ID2 = 124;
    private static final String LOCATION_NAME_PREFIX = "TEST_789_";
    private static final String LOCATION_NAME = LOCATION_NAME_PREFIX + "Court123";

    private static final ArtefactType ARTEFACT_TYPE = ArtefactType.LIST;
    private static final Sensitivity SENSITIVITY = Sensitivity.PUBLIC;
    private static final String PROVENANCE = "MANUAL_UPLOAD";
    private static final String SOURCE_ARTEFACT_ID = "sourceArtefactId";
    private static final LocalDateTime DISPLAY_TO = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
    private static final LocalDateTime DISPLAY_FROM = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
    private static final Language LANGUAGE = Language.ENGLISH;
    private static final ListType LIST_TYPE = ListType.CIVIL_DAILY_CAUSE_LIST;
    private static final LocalDateTime CONTENT_DATE = LocalDateTime.now().toLocalDate()
        .atStartOfDay()
        .truncatedTo(ChronoUnit.SECONDS);
    private static final String ADMIN_HEADER = "x-admin";

    private static final String UNAUTHORIZED_ROLE = "APPROLE_unknown.authorized";
    private static final String UNAUTHORIZED_USERNAME = "unauthorized_isAuthorized";

    private static final String CREATE_LOCATION_MESSAGE = "Create location response does not match";
    private static final String STATUS_CODE_MESSAGE = "Status code does not match";
    private static final String SYSTEM_ADMIN_ID = UUID.randomUUID().toString();

    private static PiUser piUser;

    @Autowired
    private MockMvc mockMvc;

    @BeforeAll
    void setup() {
        piUser = new PiUser();
        piUser.setUserId(SYSTEM_ADMIN_ID);
        piUser.setEmail("test@justice.gov.uk");
        piUser.setRoles(SYSTEM_ADMIN);

        OBJECT_MAPPER.findAndRegisterModules();
    }

    @Test
    void testTestingSupportCreateLocation() throws Exception {
        MvcResult postResponse = createLocationByIdAndName(LOCATION_ID);

        assertThat(postResponse.getResponse().getStatus())
            .as(STATUS_CODE_MESSAGE)
            .isEqualTo(CREATED.value());

        assertThat(postResponse.getResponse().getContentAsString())
            .as(CREATE_LOCATION_MESSAGE)
            .isEqualTo("Location with ID " + LOCATION_ID + " and name " + LOCATION_NAME
                           + " created successfully");

        MvcResult getResponse = mockMvc.perform(get(LOCATIONS_URL + LOCATION_ID))
            .andExpect(status().isOk())
            .andReturn();

        Location location = OBJECT_MAPPER.readValue(getResponse.getResponse().getContentAsString(), Location.class);

        assertThat(location.getLocationId())
            .as("Location ID does not match")
            .isEqualTo(LOCATION_ID);

        assertThat(location.getName())
            .as("Location name does not match")
            .isEqualTo(LOCATION_NAME);
    }

    @Test
    void testTestingSupportCreateLocationWithExistingLocationName() throws Exception {
        MvcResult postResponse = createLocationByIdAndName(LOCATION_ID);

        assertThat(postResponse.getResponse().getStatus())
            .as(STATUS_CODE_MESSAGE)
            .isEqualTo(CREATED.value());

        postResponse = createLocationByIdAndName(LOCATION_ID2);

        assertThat(postResponse.getResponse().getStatus())
            .as(STATUS_CODE_MESSAGE)
            .isEqualTo(CONFLICT.value());

        assertThat(postResponse.getResponse().getContentAsString())
            .as(CREATE_LOCATION_MESSAGE)
            .contains("Location with ID " + LOCATION_ID2 + " not created. The location name '" + LOCATION_NAME
                          + "' already exists");
    }

    @Test
    void testTestingSupportDeleteLocationsByNamePrefix() throws Exception {
        createLocationByIdAndName(LOCATION_ID);

        MvcResult deleteResponse = mockMvc.perform(delete(TESTING_SUPPORT_LOCATION_URL + LOCATION_NAME_PREFIX))
            .andExpect(status().isOk())
            .andReturn();

        assertThat(deleteResponse.getResponse().getContentAsString())
            .as("Location delete response does not match")
            .isEqualTo("1 location(s) deleted with name starting with " + LOCATION_NAME_PREFIX);

        mockMvc.perform(get(LOCATIONS_URL + LOCATION_ID))
            .andExpect(status().isNotFound());
    }

    @Test
    void testTestingSupportDeletePublicationsByLocationNamePrefix() throws Exception {
        createLocationByIdAndName(LOCATION_ID);
        uploadPublication();

        MockHttpServletRequestBuilder getRequest = get(PUBLICATION_BY_LOCATION_ID_URL + LOCATION_ID)
            .header(ADMIN_HEADER, true);

        MvcResult getResponse = mockMvc.perform(getRequest)
            .andExpect(status().isOk())
            .andReturn();

        assertThat(getResponse.getResponse().getContentAsString())
            .as("Artefact is empty")
            .contains("\"locationId\":\"123\"");

        MvcResult deleteResponse = mockMvc.perform(delete(TESTING_SUPPORT_PUBLICATION_URL + LOCATION_NAME_PREFIX))
            .andExpect(status().isOk())
            .andReturn();

        assertThat(deleteResponse.getResponse().getContentAsString())
            .as("Publication delete response does not match")
            .isEqualTo("1 artefacts(s) deleted for location name starting with " + LOCATION_NAME_PREFIX);

        getResponse = mockMvc.perform(getRequest)
            .andExpect(status().isOk())
            .andReturn();

        assertThat(getResponse.getResponse().getContentAsString())
            .as("Artefact is not empty")
            .isEqualTo("[]");
    }

    @Test
    @WithMockUser(username = UNAUTHORIZED_USERNAME, authorities = {UNAUTHORIZED_ROLE})
    void testUnauthorisedTestingSupportCreateLocation() throws Exception {
        MockHttpServletRequestBuilder postRequest = MockMvcRequestBuilders
            .post(TESTING_SUPPORT_LOCATION_URL + LOCATION_ID)
            .content(LOCATION_NAME)
            .contentType(MediaType.APPLICATION_JSON);

        mockMvc.perform(postRequest)
            .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = UNAUTHORIZED_USERNAME, authorities = {UNAUTHORIZED_ROLE})
    void testUnauthorisedTestingSupportDeleteLocations() throws Exception {
        mockMvc.perform(delete(TESTING_SUPPORT_LOCATION_URL + LOCATION_NAME_PREFIX))
            .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = UNAUTHORIZED_USERNAME, authorities = {UNAUTHORIZED_ROLE})
    void testUnauthorisedTestingSupportDeletePublications() throws Exception {
        mockMvc.perform(delete(TESTING_SUPPORT_PUBLICATION_URL + LOCATION_NAME_PREFIX))
            .andExpect(status().isForbidden());
    }

    private MvcResult createLocationByIdAndName(Integer locationId) throws Exception {
        MockHttpServletRequestBuilder postRequest = MockMvcRequestBuilders
            .post(TESTING_SUPPORT_LOCATION_URL + locationId)
            .content(LOCATION_NAME)
            .contentType(MediaType.APPLICATION_JSON);

        return mockMvc.perform(postRequest)
            .andReturn();
    }

    private MvcResult uploadPublication() throws Exception {
        when(accountManagementService.getUserById(any())).thenReturn(piUser);
        try (InputStream is = this.getClass().getClassLoader().getResourceAsStream("data/artefact.json")) {
            String payload = new String(IOUtils.toByteArray(Objects.requireNonNull(is)));

            MockHttpServletRequestBuilder postRequest = MockMvcRequestBuilders.post(PUBLICATION_URL)
                .content(payload)
                .header(PublicationConfiguration.TYPE_HEADER, ARTEFACT_TYPE)
                .header(PublicationConfiguration.SENSITIVITY_HEADER, SENSITIVITY)
                .header(PublicationConfiguration.LANGUAGE_HEADER, LANGUAGE)
                .header(PublicationConfiguration.PROVENANCE_HEADER, PROVENANCE)
                .header(PublicationConfiguration.SOURCE_ARTEFACT_ID_HEADER, SOURCE_ARTEFACT_ID)
                .header(PublicationConfiguration.DISPLAY_TO_HEADER, DISPLAY_TO)
                .header(PublicationConfiguration.DISPLAY_FROM_HEADER, DISPLAY_FROM)
                .header(PublicationConfiguration.LIST_TYPE, LIST_TYPE)
                .header(PublicationConfiguration.COURT_ID, LOCATION_ID)
                .header(PublicationConfiguration.CONTENT_DATE, CONTENT_DATE)
                .header(PublicationConfiguration.LANGUAGE_HEADER, LANGUAGE)
                .header(PublicationConfiguration.REQUESTER_ID_HEADER, SYSTEM_ADMIN_ID)
                .contentType(MediaType.APPLICATION_JSON);

            return mockMvc.perform(postRequest)
                .andExpect(status().isCreated())
                .andReturn();
        }
    }
}
