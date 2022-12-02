package uk.gov.hmcts.reform.pip.data.management.controllers;

import io.zonky.test.db.AutoConfigureEmbeddedDatabase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import uk.gov.hmcts.reform.pip.data.management.Application;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = {Application.class},
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles(profiles = {"functional", "view"})
@AutoConfigureEmbeddedDatabase(type = AutoConfigureEmbeddedDatabase.DatabaseType.POSTGRES)
class ViewApiTest {

    @Autowired
    private transient MockMvc mockMvc;
    private static final String USERNAME = "admin";
    private static final String VALID_ROLE = "APPROLE_api.request.admin";

    @DisplayName("Should refresh view with 200 response code")
    @Test
    @WithMockUser(username = USERNAME, authorities = {VALID_ROLE})
    void testRefreshView() throws Exception {
        MockHttpServletRequestBuilder mockHttpServletRequestBuilder = MockMvcRequestBuilders
            .post("/view/refresh");
        mockMvc.perform(mockHttpServletRequestBuilder)
            .andExpect(status().isOk());
    }

    @Test
    void testRefreshViewUnauthorised() throws Exception {
        MockHttpServletRequestBuilder mockHttpServletRequestBuilder = MockMvcRequestBuilders
            .post("/view/refresh");

        mockMvc.perform(mockHttpServletRequestBuilder)
            .andExpect(status().isUnauthorized());
    }
}

