package uk.gov.hmcts.reform.pip.data.management.api;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import uk.gov.hmcts.reform.pip.data.management.Application;
import uk.gov.hmcts.reform.pip.data.management.config.AzureConfigurationClientTest;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {AzureConfigurationClientTest.class, Application.class})
@ActiveProfiles(profiles = "test")
@AutoConfigureMockMvc
class HearingApiTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @BeforeEach
    void setup() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    @Test
    void testGetHearingsReturnsSuccess() throws Exception {
        mockMvc.perform(get("/hearings/1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(3)));
    }

    @Test
    void testGetHearingsReturnsNotFound() throws Exception {
        mockMvc.perform(get("/hearings/5"))
            .andExpect(status().isNotFound())
            .andExpect(content().string(containsString("No hearings found for court id: 5")));
    }

    @Test
    void testGetHearingsByName() throws Exception {
        mockMvc.perform(get("/hearings/case-name/Livepath's hearings"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    void testGetHearingsByNamePartial() throws Exception {
        mockMvc.perform(get("/hearings/case-name/Live"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(2)));
    }

    @Test
    void testGetHearingsByNameReturnsNotFound() throws Exception {
        mockMvc.perform(get("/hearings/case-name/DoesntMatch anything"))
            .andExpect(status().isNotFound())
            .andExpect(content().string(containsString(
                "No hearings found containing the case name: DoesntMatch anything")));
    }

    @Test
    void testGetHearingByCaseNumber() throws Exception {
        mockMvc.perform(get("/hearings/case-number/636947292"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.hearingId", is(1)));
    }

    @Test
    void testGetHearingByCaseNumberReturnsNotFound() throws Exception {
        mockMvc.perform(get("/hearings/case-number/898989"))
            .andExpect(status().isNotFound())
            .andExpect(content().string(containsString(
                "No hearing found for case number: 898989")));
    }

    @Test
    void testGetHearingByUrn() throws Exception {
        mockMvc.perform(get("/hearings/urn/12345678"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.hearingId", is(1)));
    }

    @Test
    void testGetHearingByUrnReturnsNotFound() throws Exception {
        mockMvc.perform(get("/hearings/urn/898989"))
            .andExpect(status().isNotFound())
            .andExpect(content().string(containsString(
                "No hearing found for urn number: 898989")));
    }
}
