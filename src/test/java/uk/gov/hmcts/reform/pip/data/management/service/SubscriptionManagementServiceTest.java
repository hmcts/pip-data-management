package uk.gov.hmcts.reform.pip.data.management.service;

import io.zonky.test.db.AutoConfigureEmbeddedDatabase;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.pip.data.management.Application;
import uk.gov.hmcts.reform.pip.data.management.models.publication.Artefact;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
@ExtendWith(OutputCaptureExtension.class)
@ActiveProfiles("test")
@SpringBootTest(classes = {Application.class})
@AutoConfigureEmbeddedDatabase(type = AutoConfigureEmbeddedDatabase.DatabaseType.POSTGRES)
class SubscriptionManagementServiceTest {

    private static final Artefact ARTEFACT = Artefact.builder()
        .sourceArtefactId("TEST")
        .provenance("PROVENANCE")
        .build();
    private static MockWebServer mockSubscriptionManagementEndpoint;

    @Autowired
    SubscriptionManagementService subscriptionManagementService;


    @Test
    void testSendTrigger() throws IOException {
        mockSubscriptionManagementEndpoint = new MockWebServer();
        mockSubscriptionManagementEndpoint.start(4550);
        mockSubscriptionManagementEndpoint.enqueue(new MockResponse()
                                                       .setBody("Trigger has been sent"));
        assertEquals("Trigger has been sent", subscriptionManagementService.sendArtefactForSubscription(ARTEFACT),
                     "Trigger is not being sent");
        mockSubscriptionManagementEndpoint.shutdown();
    }

    @Test
    void testFailedSend() {
        assertEquals(subscriptionManagementService.sendArtefactForSubscription(ARTEFACT), "Request failed",
                     "Trigger failed to send.");
    }
}
