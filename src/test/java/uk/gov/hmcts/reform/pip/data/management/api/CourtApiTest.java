package uk.gov.hmcts.reform.pip.data.management.api;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
public class CourtApiTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @BeforeEach
    public void setup() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    @Test
    public void testGetAllCourtsReturnsSuccess() throws Exception {
        mockMvc.perform(get("/courts"))
            .andExpect(status().isOk())
            .andExpect(content().string(containsString("\"courtId\":1")));
    }

    @Test
    public void testGetCourtByNameReturnsSuccess() throws Exception {
        mockMvc.perform(get("/courts/Manchester Family Court"))
            .andExpect(status().isOk())
            .andExpect(content().string(containsString("Manchester Family Court")));
    }

    @Test
    public void testGetCourtByNameReturnsNotFound() throws Exception {
        mockMvc.perform(get("/courts/invalid"))
            .andExpect(status().isNotFound())
            .andExpect(content().string(containsString("No court found with the search: invalid")));
    }

    @Test
    public void testFilterCourtsByLocation() throws Exception {
        mockMvc.perform(get("/courts/filter").content("{\"filters\": [\"location\"],"
                                                          + "\"values\": [\"manchester\"]}")
                            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(2)));
    }

    @Test
    public void testFilterCourtsByLocationReturnsNoResults() throws Exception {
        mockMvc.perform(get("/courts/filter").content("{\"filters\": [\"location\"],"
                                                          + "\"values\": [\"london\"]}")
                            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    public void testFilterCourtsBy2Filters() throws Exception {
        mockMvc.perform(get("/courts/filter").content("{\"filters\": [\"location\", \"jurisdiction\"],"
                                                          + "\"values\": [\"manchester\", \"magistrates court\"]}")
                            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(1)));
    }
}
