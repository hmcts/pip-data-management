package uk.gov.hmcts.reform.pip.data.management.service;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.pip.data.management.models.publication.Artefact;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
@ExtendWith(OutputCaptureExtension.class)
@ActiveProfiles(profiles = "test")
class SubscriptionManagementServiceTest {

    private static final String TRIGGER_URL = "localhost:4550";
    private static final Artefact ARTEFACT = Artefact.builder()
        .sourceArtefactId("TEST")
        .provenance("PROVENANCE")
        .build();
    private static MockWebServer mockSubscriptionManagementEndpoint;

    SubscriptionManagementService subscriptionManagementService = new SubscriptionManagementService(TRIGGER_URL);


    @Test
    void testSendTrigger() throws IOException {
        mockSubscriptionManagementEndpoint = new MockWebServer();
        mockSubscriptionManagementEndpoint.start(4550);
        mockSubscriptionManagementEndpoint.enqueue(new MockResponse()
                                                       .setBody("Trigger not sent"));
        assertEquals("Trigger not sent", subscriptionManagementService.sendSubTrigger(ARTEFACT),
                     "Trigger is not being sent");
        mockSubscriptionManagementEndpoint.shutdown();
    }

    @Test
    void testFailedSend() {
        assertEquals(subscriptionManagementService.sendSubTrigger(ARTEFACT), "Request failed",
                     "Trigger failed to send and failed to give a warning");

    }

}
