package uk.gov.hmcts.reform.pip.data.management.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.zonky.test.db.AutoConfigureEmbeddedDatabase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import uk.gov.hmcts.reform.pip.data.management.errorhandling.exceptions.LocationMetadataNotFoundException;
import uk.gov.hmcts.reform.pip.data.management.models.location.LocationMetadata;
import uk.gov.hmcts.reform.pip.data.management.utils.IntegrationTestBase;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureEmbeddedDatabase(type = AutoConfigureEmbeddedDatabase.DatabaseType.POSTGRES)
@ActiveProfiles({"integration", "disable-async"})
@WithMockUser(username = "admin", authorities = {"APPROLE_api.request.admin"})
class LocationMetadataTest extends IntegrationTestBase {

    private static final String BASE_URL = "/location-metadata";
    private static final String USER_ID = "user-123";
    private static final String LOCATION_ID = "123";
    private static final String REQUESTER_ID_HEADER = "x-requester-id";
    private static final UUID TEST_UUID = UUID.randomUUID();
    private static final String UUID_STRING = TEST_UUID.toString();

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private LocationMetadata createTestLocationMetadata() {
        LocationMetadata locationMetadata = new LocationMetadata();
        locationMetadata.setLocationId(Integer.parseInt(LOCATION_ID));
        return locationMetadata;
    }

    @Test
    void testAddLocationMetadataSuccess() throws Exception {
        LocationMetadata locationMetadata = createTestLocationMetadata();
        doNothing().when(locationMetaDataService).createLocationMetadata(any(), anyString());

        mockMvc.perform(post(BASE_URL)
                            .header(REQUESTER_ID_HEADER, USER_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(locationMetadata)))
            .andExpect(status().isCreated())
            .andExpect(content().string(
                String.format("Location metadata successfully added by user %s", USER_ID)
            ));
    }

    @Test
    void testUpdateLocationMetadataSuccess() throws Exception {
        LocationMetadata locationMetadata = createTestLocationMetadata();
        doNothing().when(locationMetaDataService).updateLocationMetadata(any(), anyString());

        mockMvc.perform(put(BASE_URL)
                            .header(REQUESTER_ID_HEADER, USER_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(locationMetadata)))
            .andExpect(status().isOk())
            .andExpect(content().string(
                String.format("Location metadata successfully updated by user %s", USER_ID)
            ));
    }

    @Test
    void testDeleteLocationMetadataSuccess() throws Exception {
        doNothing().when(locationMetaDataService).deleteById(anyString(), anyString());

        mockMvc.perform(delete(BASE_URL + "/" + UUID_STRING)
                            .header(REQUESTER_ID_HEADER, USER_ID))
            .andExpect(status().isOk())
            .andExpect(content().string(
                String.format("Location metadata successfully deleted by user %s", USER_ID)
            ));
    }

    @Test
    void testDeleteLocationMetadataNotFound() throws Exception {
        doThrow(new LocationMetadataNotFoundException("Not found"))
            .when(locationMetaDataService).deleteById(anyString(), anyString());

        mockMvc.perform(delete(BASE_URL + "/" + UUID_STRING)
                            .header(REQUESTER_ID_HEADER, USER_ID))
            .andExpect(status().isNotFound());
    }

    @Test
    void testGetLocationMetadataSuccess() throws Exception {
        LocationMetadata expectedMetadata = createTestLocationMetadata();
        when(locationMetaDataService.getById(UUID_STRING)).thenReturn(expectedMetadata);

        MvcResult mvcResult = mockMvc.perform(get(BASE_URL + "/" + UUID_STRING))
            .andExpect(status().isOk())
            .andReturn();

        LocationMetadata returnLocationMetadata =
            objectMapper.readValue(mvcResult.getResponse().getContentAsString(), LocationMetadata.class);

        assertEquals(createTestLocationMetadata(), returnLocationMetadata,
                     "Returned location metadata matches expected location metadata");
    }

    @Test
    void testGetLocationMetadataNotFound() throws Exception {
        when(locationMetaDataService.getById(UUID_STRING))
            .thenThrow(new LocationMetadataNotFoundException("Not found"));

        mockMvc.perform(get(BASE_URL + "/" + UUID_STRING))
            .andExpect(status().isNotFound());
    }

    @Test
    void testGetLocationMetadataByLocationIdSuccess() throws Exception {
        LocationMetadata expectedMetadata = createTestLocationMetadata();
        when(locationMetaDataService.getLocationById(LOCATION_ID)).thenReturn(expectedMetadata);

        mockMvc.perform(get(BASE_URL + "/search-by-location-id/" + LOCATION_ID))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.locationId").value(LOCATION_ID));
    }

    @Test
    void testGetLocationMetadataByLocationIdNotFound() throws Exception {
        when(locationMetaDataService.getLocationById(LOCATION_ID))
            .thenThrow(new LocationMetadataNotFoundException("Not found"));

        mockMvc.perform(get(BASE_URL + "/search-by-location-id/" + LOCATION_ID))
            .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = {"TEST_ROLE"}) // Non-admin user
    void testAddLocationMetadataForbiddenForNonAdmin() throws Exception {
        LocationMetadata locationMetadata = createTestLocationMetadata();

        mockMvc.perform(post(BASE_URL)
                            .header(REQUESTER_ID_HEADER, USER_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(locationMetadata)))
            .andExpect(status().isForbidden());
    }
}
