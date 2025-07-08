package uk.gov.hmcts.reform.pip.data.management.controllers.publication;

import io.zonky.test.db.AutoConfigureEmbeddedDatabase;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import uk.gov.hmcts.reform.pip.data.management.Application;
import uk.gov.hmcts.reform.pip.data.management.utils.PublicationIntegrationTestBase;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = {Application.class},
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@ActiveProfiles("integration")
@AutoConfigureEmbeddedDatabase(type = AutoConfigureEmbeddedDatabase.DatabaseType.POSTGRES)
@WithMockUser(username = "admin", authorities = {"APPROLE_api.request.admin"})
class PublicationSubscriptionTest extends PublicationIntegrationTestBase {
    private static final String SEND_NEW_ARTEFACTS_FOR_SUBSCRIPTION_URL = "/publication/latest/subscription";

    @Test
    void testSendNewArtefactsForSubscriptionSuccess() throws Exception {
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
            .post(SEND_NEW_ARTEFACTS_FOR_SUBSCRIPTION_URL);

        mockMvc.perform(request).andExpect(status().isNoContent());
    }

    @Test
    void testUnauthorizedSendNewArtefactsForSubscription() throws Exception {
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post(SEND_NEW_ARTEFACTS_FOR_SUBSCRIPTION_URL);

        mockMvc.perform(request)
            .andExpect(status().isForbidden())
            .andReturn();
    }
}
