package uk.gov.hmcts.reform.pip.data.management.controllers.location;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.zonky.test.db.AutoConfigureEmbeddedDatabase;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
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
import uk.gov.hmcts.reform.pip.data.management.models.location.Location;
import uk.gov.hmcts.reform.pip.data.management.models.location.LocationMetadata;
import uk.gov.hmcts.reform.pip.data.management.utils.IntegrationTestBase;
import uk.gov.hmcts.reform.pip.model.account.PiUser;

import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.hibernate.validator.internal.util.Contracts.assertNotEmpty;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.pip.model.account.Roles.SYSTEM_ADMIN;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles({"integration"})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@WithMockUser(username = "admin", authorities = {"APPROLE_api.request.admin"})
@AutoConfigureEmbeddedDatabase(type = AutoConfigureEmbeddedDatabase.DatabaseType.POSTGRES)
class LocationMetadataTest extends IntegrationTestBase {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private static final String BASE_URL = "/location-metadata";
    private static final String FIND_BY_LOCATION_ID = BASE_URL +  "/location/";
    private static final String REQUESTER_ID_HEADER = "x-requester-id";
    private static final UUID TEST_UUID = UUID.randomUUID();
    private static final String UUID_STRING = TEST_UUID.toString();

    private static final String VALIDATION_EMPTY_RESPONSE = "Returned response is empty";
    private static final String LOCATION_LIST = "locationList";
    private static final String LOCATION_ROOT_URL = "/locations";
    public static final String UPLOAD_API = LOCATION_ROOT_URL + "/upload";
    private static final String LOCATIONS_CSV = "location/ValidCsv.csv";
    private static final String CAUTION_MESSAGE = "Cause message";
    private static final String WELSH_CAUTION_MESSAGE = "Welsh Cause message";
    private static final String NO_LIST = "No list message";
    private static final String WELSH_NO_LIST = "Welsh no list message";
    private static final String UNAUTHORIZED_ROLE = "APPROLE_unknown.authorized";
    private static final String UNAUTHORIZED_USERNAME = "unauthorized_isAuthorized";

    private static final String SYSTEM_ADMIN_ID = UUID.randomUUID().toString();

    private static PiUser piUser;

    @Autowired
    private MockMvc mockMvc;

    @BeforeAll
    static void setup() {
        OBJECT_MAPPER.findAndRegisterModules();

        piUser = new PiUser();
        piUser.setUserId(SYSTEM_ADMIN_ID);
        piUser.setEmail("test@justice.gov.uk");
        piUser.setRoles(SYSTEM_ADMIN);
    }

    private LocationMetadata createTestLocationMetadata(String locationId) {
        LocationMetadata locationMetadata = new LocationMetadata();
        locationMetadata.setLocationId(Integer.parseInt(locationId));
        locationMetadata.setCautionMessage(CAUTION_MESSAGE);
        locationMetadata.setWelshCautionMessage(WELSH_CAUTION_MESSAGE);
        locationMetadata.setNoListMessage(NO_LIST);
        locationMetadata.setWelshNoListMessage(WELSH_NO_LIST);
        return locationMetadata;
    }

    private String getLocationId() throws Exception {

        try (InputStream csvInputStream = this.getClass().getClassLoader()
            .getResourceAsStream(LOCATIONS_CSV)) {
            MockMultipartFile csvFile
                = new MockMultipartFile(LOCATION_LIST, csvInputStream);

            MvcResult mvcResult = mockMvc.perform(multipart(UPLOAD_API).file(csvFile)
                                            .header(REQUESTER_ID_HEADER, SYSTEM_ADMIN_ID))
                .andExpect(status().isOk()).andReturn();

            List<Location> locations = Arrays.asList(
                OBJECT_MAPPER.readValue(mvcResult.getResponse().getContentAsString(), Location[].class));
            return String.valueOf(locations.getFirst().getLocationId());
        }
    }

    protected MockHttpServletRequestBuilder setupMockLocationMetadata(String locationId) throws Exception {
        return post(BASE_URL)
            .content(OBJECT_MAPPER.writeValueAsString(createTestLocationMetadata(locationId)))
            .header(REQUESTER_ID_HEADER, SYSTEM_ADMIN_ID)
            .contentType(MediaType.APPLICATION_JSON);
    }

    @Test
    void testAddLocationMetadataSuccess() throws Exception {
        when(accountManagementService.getUserById(any())).thenReturn(piUser);
        LocationMetadata locationMetadata = createTestLocationMetadata(getLocationId());
        mockMvc.perform(post(BASE_URL)
                            .header(REQUESTER_ID_HEADER, SYSTEM_ADMIN_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(OBJECT_MAPPER.writeValueAsString(locationMetadata)))
            .andExpect(status().isCreated())
            .andExpect(content().string(
                String.format("Location metadata successfully added by user %s", SYSTEM_ADMIN_ID)
            ));
    }

    @Test
    void testUpdateLocationMetadataSuccess() throws Exception {
        when(accountManagementService.getUserById(any())).thenReturn(piUser);
        String locationId = getLocationId();
        MockHttpServletRequestBuilder mappedLocationMetadata = setupMockLocationMetadata(locationId);
        mockMvc.perform(mappedLocationMetadata)
            .andExpect(status().isCreated())
            .andReturn();

        MvcResult getResponse = mockMvc.perform(get(FIND_BY_LOCATION_ID + locationId)
                                                    .header(REQUESTER_ID_HEADER, SYSTEM_ADMIN_ID)
                                                    .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn();

        String responseBody = getResponse.getResponse().getContentAsString();
        assertNotEmpty(responseBody, "Response body should not be empty");

        LocationMetadata returnLocationMetadata =
            OBJECT_MAPPER.readValue(getResponse.getResponse().getContentAsString(), LocationMetadata.class);

        mockMvc.perform(put(BASE_URL + "/" + returnLocationMetadata.getLocationMetadataId())
                            .header(REQUESTER_ID_HEADER, SYSTEM_ADMIN_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(OBJECT_MAPPER.writeValueAsString(returnLocationMetadata)))
            .andExpect(status().isOk())
            .andExpect(content().string(
                String.format("Location metadata successfully updated by user %s", SYSTEM_ADMIN_ID)
            ));
    }

    @Test
    void testDeleteLocationMetadataSuccess() throws Exception {
        when(accountManagementService.getUserById(any())).thenReturn(piUser);
        String locationId = getLocationId();
        MockHttpServletRequestBuilder mappedLocationMetadata = setupMockLocationMetadata(locationId);
        MvcResult response = mockMvc.perform(mappedLocationMetadata)
            .andExpect(status().isCreated())
            .andReturn();
        assertNotNull(response.getResponse().getContentAsString(), VALIDATION_EMPTY_RESPONSE);

        MvcResult getResponse = mockMvc.perform(get(FIND_BY_LOCATION_ID + locationId)
                                                    .header(REQUESTER_ID_HEADER, SYSTEM_ADMIN_ID)
                                                    .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn();

        String responseBody = getResponse.getResponse().getContentAsString();
        assertNotEmpty(responseBody, "Response body should not be empty");

        LocationMetadata returnLocationMetadata =
            OBJECT_MAPPER.readValue(getResponse.getResponse().getContentAsString(), LocationMetadata.class);

        mockMvc.perform(delete(BASE_URL + "/" + returnLocationMetadata.getLocationMetadataId())
                            .header(REQUESTER_ID_HEADER, SYSTEM_ADMIN_ID))
            .andExpect(status().isOk())
            .andExpect(content().string(
                String.format("Location metadata successfully deleted by user %s", SYSTEM_ADMIN_ID)
            ));
    }

    @Test
    void testDeleteLocationMetadataNotFound() throws Exception {
        when(accountManagementService.getUserById(any())).thenReturn(piUser);
        mockMvc.perform(delete(BASE_URL + "/" + UUID_STRING)
                            .contentType(MediaType.APPLICATION_JSON)
                            .header(REQUESTER_ID_HEADER, SYSTEM_ADMIN_ID))
            .andExpect(status().isNotFound());
    }

    @Test
    void testGetLocationMetadataSuccess() throws Exception {
        when(accountManagementService.getUserById(any())).thenReturn(piUser);
        String locationId = getLocationId();
        MockHttpServletRequestBuilder mappedLocationMetadata = setupMockLocationMetadata(locationId);
        MvcResult response = mockMvc.perform(mappedLocationMetadata)
            .andExpect(status().isCreated())
            .andReturn();
        assertNotNull(response.getResponse().getContentAsString(), VALIDATION_EMPTY_RESPONSE);

        MvcResult getResponse = mockMvc.perform(get(FIND_BY_LOCATION_ID + locationId)
                                                  .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn();

        String responseBody = getResponse.getResponse().getContentAsString();
        assertNotEmpty(responseBody, "Response body should not be empty");

        LocationMetadata returnLocationMetadata =
            OBJECT_MAPPER.readValue(getResponse.getResponse().getContentAsString(), LocationMetadata.class);

        assertEquals(locationId, returnLocationMetadata.getLocationId().toString(),
                     "Returned location metadata id matches");
        assertEquals(CAUTION_MESSAGE, returnLocationMetadata.getCautionMessage(),
                     "Returned caution message matches");
        assertEquals(WELSH_CAUTION_MESSAGE, returnLocationMetadata.getWelshCautionMessage(),
                     "Returned welsh caution message matches");
        assertEquals(NO_LIST, returnLocationMetadata.getNoListMessage(),
                     "Returned no list message matches");
        assertEquals(WELSH_NO_LIST, returnLocationMetadata.getWelshNoListMessage(),
                     "Returned welsh no list message matches");
    }

    @Test
    void testGetLocationMetadataByLocationIdSuccess() throws Exception {
        when(accountManagementService.getUserById(any())).thenReturn(piUser);
        String locationId = getLocationId();
        MockHttpServletRequestBuilder mappedLocationMetadata = setupMockLocationMetadata(locationId);
        MvcResult response = mockMvc.perform(mappedLocationMetadata)
            .andExpect(status().isCreated())
            .andReturn();
        assertNotNull(response.getResponse().getContentAsString(), VALIDATION_EMPTY_RESPONSE);

        mockMvc.perform(get(FIND_BY_LOCATION_ID + locationId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.locationId").value(locationId));
    }

    @Test
    void testGetLocationMetadataByLocationIdNotFound() throws Exception {
        mockMvc.perform(get(BASE_URL + "/location/987"))
            .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = UNAUTHORIZED_USERNAME, authorities = {UNAUTHORIZED_ROLE})
    void testAddLocationMetadataForbiddenForNonAdmin() throws Exception {
        LocationMetadata locationMetadata = new LocationMetadata();
        locationMetadata.setLocationId(123);

        MockHttpServletRequestBuilder mappedLocationMetadata = post(BASE_URL)
            .content(OBJECT_MAPPER.writeValueAsString(locationMetadata))
            .header(REQUESTER_ID_HEADER, SYSTEM_ADMIN_ID)
            .contentType(MediaType.APPLICATION_JSON);

        mockMvc.perform(mappedLocationMetadata)
            .andExpect(status().isForbidden()).andReturn();
    }

    @Test
    @WithMockUser(username = UNAUTHORIZED_USERNAME, authorities = {UNAUTHORIZED_ROLE})
    void testUpdateLocationMetadataForbiddenForNonAdmin() throws Exception {
        when(accountManagementService.getUserById(any())).thenReturn(piUser);
        LocationMetadata locationMetadata = new LocationMetadata();
        locationMetadata.setLocationId(123);
        mockMvc.perform(put(BASE_URL + "/" + UUID_STRING)
                            .header(REQUESTER_ID_HEADER, SYSTEM_ADMIN_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(OBJECT_MAPPER.writeValueAsString(locationMetadata)))
            .andExpect(status().isForbidden()).andReturn();
    }

    @Test
    @WithMockUser(username = UNAUTHORIZED_USERNAME, authorities = {UNAUTHORIZED_ROLE})
    void testGetLocationMetadataByLocationIdForbiddenForNonAdmin() throws Exception {
        mockMvc.perform(get(BASE_URL + "/location/987")
                            .header(REQUESTER_ID_HEADER, SYSTEM_ADMIN_ID))
            .andExpect(status().isForbidden()).andReturn();
    }

    @Test
    @WithMockUser(username = UNAUTHORIZED_USERNAME, authorities = {UNAUTHORIZED_ROLE})
    void testDeleteLocationMetadataForbiddenForNonAdmin() throws Exception {
        when(accountManagementService.getUserById(any())).thenReturn(piUser);
        mockMvc.perform(delete(BASE_URL + "/123-456")
                            .header(REQUESTER_ID_HEADER, SYSTEM_ADMIN_ID))
            .andExpect(status().isForbidden()).andReturn();
    }

    @Test
    void testAddLocationMetadataBadRequestWhenSystemAdminIdNotProvided() throws Exception {
        LocationMetadata locationMetadata = new LocationMetadata();
        locationMetadata.setLocationId(123);

        MockHttpServletRequestBuilder mappedLocationMetadata = post(BASE_URL)
            .content(OBJECT_MAPPER.writeValueAsString(locationMetadata))
            .contentType(MediaType.APPLICATION_JSON);

        mockMvc.perform(mappedLocationMetadata)
            .andExpect(status().isBadRequest()).andReturn();
    }

    @Test
    void testUpdateLocationMetadataBadRequestWhenSystemAdminIdNotProvided() throws Exception {
        when(accountManagementService.getUserById(any())).thenReturn(piUser);
        LocationMetadata locationMetadata = new LocationMetadata();
        locationMetadata.setLocationId(123);
        mockMvc.perform(put(BASE_URL + "/" + UUID_STRING)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(OBJECT_MAPPER.writeValueAsString(locationMetadata)))
            .andExpect(status().isBadRequest()).andReturn();
    }

    @Test
    void testDeleteLocationMetadataBadRequestWhenSystemAdminIdNotProvided() throws Exception {
        when(accountManagementService.getUserById(any())).thenReturn(piUser);
        mockMvc.perform(delete(BASE_URL + "/123-456"))
            .andExpect(status().isBadRequest()).andReturn();
    }

    @Test
    void testAddLocationMetaDataReturn409WhenMetadataForLocationAlreadyExists() throws Exception {
        when(accountManagementService.getUserById(any())).thenReturn(piUser);
        LocationMetadata locationMetadata = createTestLocationMetadata(getLocationId());

        mockMvc.perform(post(BASE_URL)
                        .header(REQUESTER_ID_HEADER, SYSTEM_ADMIN_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(OBJECT_MAPPER.writeValueAsString(locationMetadata)))
                .andExpect(status().isCreated())
                .andExpect(content().string(
                        String.format("Location metadata successfully added by user %s", SYSTEM_ADMIN_ID)
                ));

        mockMvc.perform(post(BASE_URL)
                        .header(REQUESTER_ID_HEADER, SYSTEM_ADMIN_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(OBJECT_MAPPER.writeValueAsString(locationMetadata)))
                .andExpect(status().isConflict());
    }

}
