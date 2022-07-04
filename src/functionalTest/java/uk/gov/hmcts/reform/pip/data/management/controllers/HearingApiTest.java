package uk.gov.hmcts.reform.pip.data.management.controllers;

import io.zonky.test.db.AutoConfigureEmbeddedDatabase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.hmcts.reform.pip.data.management.Application;

import java.io.File;
import java.nio.file.Files;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = {Application.class},
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles(profiles = "functional")
@AutoConfigureMockMvc
@AutoConfigureEmbeddedDatabase(type = AutoConfigureEmbeddedDatabase.DatabaseType.POSTGRES)
class HearingApiTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void testGetHearingsReturnsSuccess() throws Exception {
        mockMvc.perform(get("/hearings/1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(3)));
    }

    @Test
    void testGetHearingsResponse() throws Exception {
        mockMvc.perform(get("/hearings/3"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(2)))
            .andExpect(content().json(new String(Files.readAllBytes(
                new File("src/functionalTest/resources/data/hearingResponse.json").toPath()))));
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
