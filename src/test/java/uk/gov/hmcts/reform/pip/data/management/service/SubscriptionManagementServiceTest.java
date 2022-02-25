package uk.gov.hmcts.reform.pip.data.management.service;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.pip.data.management.models.publication.Artefact;

import java.io.IOException;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
@ActiveProfiles(profiles = "test")
class SubscriptionManagementServiceTest {

    private static final String TRIGGER_URL = "localhost:4550";
    private static final Artefact ARTEFACT = Artefact.builder()
        .sourceArtefactId("TEST")
        .provenance("PROVENANCE")
        .build();
    private static MockWebServer mockSubscriptionManagementEndpoint;

    SubscriptionManagementService subscriptionManagementService = new SubscriptionManagementService(TRIGGER_URL);

    @BeforeAll
    static void setup() throws IOException {
        mockSubscriptionManagementEndpoint = new MockWebServer();
        mockSubscriptionManagementEndpoint.start(4550);
    }

    @AfterAll
    static void tearDown() throws IOException {
        mockSubscriptionManagementEndpoint.shutdown();
    }

    @Test
    void testSendTrigger() {
        mockSubscriptionManagementEndpoint.enqueue(new MockResponse()
                                                       .addHeader("Content-Type", "application/json")
                                                       .setBody("[]"));
        assertEquals(Collections.emptyList(), subscriptionManagementService.sendSubTrigger(ARTEFACT),
                     "Trigger is not being sent");
    }
}
