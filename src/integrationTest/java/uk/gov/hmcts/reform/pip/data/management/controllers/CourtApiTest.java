package uk.gov.hmcts.reform.pip.data.management.controllers;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.hmcts.reform.pip.data.management.Application;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = {Application.class},
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles(profiles = "test")
class CourtApiTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void testGetAllCourtsReturnsSuccess() throws Exception {
        mockMvc.perform(get("/courts"))
            .andExpect(status().isOk())
            .andExpect(content().string(containsString("\"courtId\":1")));
    }

    @Test
    void testGetCourtByIdReturnsSuccess() throws Exception {
        mockMvc.perform(get("/courts/4"))
            .andExpect(status().isOk())
            .andExpect(content().string(containsString("Manchester Family Court")));
    }

    @Test
    void testGetCourtByIdReturnsNotFound() throws Exception {
        mockMvc.perform(get("/courts/7"))
            .andExpect(status().isNotFound())
            .andExpect(content().string(containsString("No court found with the id: 7")));
    }

    @Test
    void testGetCourtByNameReturnsSuccess() throws Exception {
        mockMvc.perform(get("/courts/find/Manchester Family Court"))
            .andExpect(status().isOk())
            .andExpect(content().string(containsString("Manchester Family Court")));
    }

    @Test
    void testGetCourtByNameReturnsNotFound() throws Exception {
        mockMvc.perform(get("/courts/find/invalid"))
            .andExpect(status().isNotFound())
            .andExpect(content().string(containsString("No court found with the search: invalid")));
    }

    @Test
    void testFilterCourtsByLocation() throws Exception {
        mockMvc.perform(get("/courts/filter").content("{\"filters\": [\"location\"],"
                                                          + "\"values\": [\"manchester\"]}")
                            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(2)));
    }

    @Test
    void testFilterCourtsByLocationReturnsNoResults() throws Exception {
        mockMvc.perform(get("/courts/filter").content("{\"filters\": [\"location\"],"
                                                          + "\"values\": [\"london\"]}")
                            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void testFilterCourtsBy2Filters() throws Exception {
        mockMvc.perform(get("/courts/filter").content("{\"filters\": [\"location\", \"jurisdiction\"],"
                                                          + "\"values\": [\"manchester\", \"magistrates court\"]}")
                            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(1)));
    }
}
