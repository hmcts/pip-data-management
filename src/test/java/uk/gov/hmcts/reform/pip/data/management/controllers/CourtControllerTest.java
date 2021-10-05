package uk.gov.hmcts.reform.pip.data.management.controllers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import uk.gov.hmcts.reform.pip.data.management.errorhandling.exceptions.CourtNotFoundException;
import uk.gov.hmcts.reform.pip.data.management.models.Court;
import uk.gov.hmcts.reform.pip.data.management.service.CourtService;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.pip.data.management.helpers.CourtHelper.createMockCourtList;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@AutoConfigureMockMvc
class CourtControllerTest {

    private List<Court> allCourts;
    private final List<String> filters = new ArrayList<>();
    private final List<String> values = new ArrayList<>();

    @MockBean
    private CourtService courtService;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext webApplicationContext;


    @BeforeEach
    void setup() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();

        allCourts = createMockCourtList();

        filters.add("location");
        filters.add("jurisdiction");

        values.add("london");
        values.add("manchester");

        when(courtService.getAllCourts()).thenReturn(allCourts);
        when(courtService.handleSearchCourt("mock court 1")).thenReturn(allCourts.get(0));
        when(courtService.handleFilterRequest(filters, values)).thenReturn(allCourts);
        when(courtService.handleSearchCourt("Invalid")).thenThrow(CourtNotFoundException.class);
    }

    @Test
    void testGetCourtListReturnsAllCourts() throws Exception {
        mockMvc.perform(get("/courts"))
            .andExpect(status().isOk())
            .andExpect(content().string(containsString(allCourts.get(0).getName())))
            .andExpect(content().string(containsString(allCourts.get(1).getName())));
    }

    @Test
    void testGetCourtReturnsOk() throws Exception {
        mockMvc.perform(get("/courts/mock court 1"))
            .andExpect(status().isOk())
            .andExpect(content().string(containsString(allCourts.get(0).getName())));
    }

    @Test
    void testGetCourtNoResultsReturnsNotFound() throws Exception {
        mockMvc.perform(get("/courts/Invalid"))
            .andExpect(status().isNotFound());
    }

    @Test
    void testGetFilterCourtsReturnsOk() throws Exception {
        mockMvc.perform(get("/courts/filter")
                            .content("{\"filters\": [\"location\", \"jurisdiction\"],"
                                         + "\"values\": [\"london\", \"manchester\"]}")
                            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().string(containsString(allCourts.get(0).getName())));
    }
}
