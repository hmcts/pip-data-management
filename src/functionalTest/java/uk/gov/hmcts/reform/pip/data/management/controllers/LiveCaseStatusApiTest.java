package uk.gov.hmcts.reform.pip.data.management.controllers;

import io.zonky.test.db.AutoConfigureEmbeddedDatabase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.hmcts.reform.pip.data.management.Application;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = {Application.class},
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles(profiles = "functional")
@AutoConfigureMockMvc
@AutoConfigureEmbeddedDatabase(type = AutoConfigureEmbeddedDatabase.DatabaseType.POSTGRES)
class LiveCaseStatusApiTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void testGetLiveCaseReturnsSuccess() throws Exception {
        mockMvc.perform(get("/lcsu/1"))
            .andExpect(status().isOk())
            .andExpect(content().string(containsString("Mutsu Court")));
    }

    @Test
    void testGetLiveCaseReturnsNotFound() throws Exception {
        mockMvc.perform(get("/lcsu/5"))
            .andExpect(status().isNotFound())
            .andExpect(content().string(containsString("No live cases found for court id: 5")));
    }
}
