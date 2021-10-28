package uk.gov.hmcts.reform.pip.data.management.api;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import uk.gov.hmcts.reform.pip.data.management.Application;
import uk.gov.hmcts.reform.pip.data.management.config.AzureConfigurationClientTest;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {AzureConfigurationClientTest.class, Application.class})
@ActiveProfiles(profiles = "test")
@AutoConfigureMockMvc
class CourtApiTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @BeforeEach
    void setup() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

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
