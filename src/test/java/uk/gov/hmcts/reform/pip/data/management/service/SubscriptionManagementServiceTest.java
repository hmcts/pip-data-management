package uk.gov.hmcts.reform.pip.data.management.service;

import nl.altindag.log.LogCaptor;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.reactive.function.client.WebClient;
import uk.gov.hmcts.reform.pip.data.management.models.publication.Artefact;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.http.HttpStatus.BAD_REQUEST;

@ActiveProfiles("test")
class SubscriptionManagementServiceTest {

    private static final Artefact ARTEFACT = Artefact.builder()
        .sourceArtefactId("TEST")
        .provenance("PROVENANCE")
        .build();

    private static final String LOCATION_ID = "1";
    private static final String TRIGGER_RECEIVED = "Trigger has been received";

    private final MockWebServer mockSubscriptionManagementEndpoint = new MockWebServer();
    SubscriptionManagementService subscriptionManagementService;

    LogCaptor logCaptor = LogCaptor.forClass(SubscriptionManagementService.class);

    @BeforeEach
    void setup() {
        WebClient mockedWebClient = WebClient.builder()
            .baseUrl(mockSubscriptionManagementEndpoint.url("/").toString())
            .build();
        subscriptionManagementService = new SubscriptionManagementService(mockedWebClient);
    }

    @AfterEach
    void teardown() throws IOException {
        mockSubscriptionManagementEndpoint.shutdown();
    }

    @Test
    void testSendTrigger() {
        mockSubscriptionManagementEndpoint.enqueue(new MockResponse()
                                                       .setBody(TRIGGER_RECEIVED));
        assertEquals(TRIGGER_RECEIVED, subscriptionManagementService.sendArtefactForSubscription(ARTEFACT),
                     "Trigger is not being sent");
    }

    @Test
    void testFailedSend() {
        mockSubscriptionManagementEndpoint.enqueue(new MockResponse()
                                                       .setResponseCode(BAD_REQUEST.value()));
        assertEquals(
            subscriptionManagementService.sendArtefactForSubscription(ARTEFACT),
            "Artefact failed to send: " + ARTEFACT.getArtefactId(),
            "Error message failed to send."
        );
        assertTrue(logCaptor.getErrorLogs().get(0)
                       .contains("Request to send artefact to Subscription Management failed with error:"),
                   "Exception was not logged.");
    }

    @Test
    void testSendDeletedArtefact() {
        mockSubscriptionManagementEndpoint.enqueue(new MockResponse()
                                                       .setBody(TRIGGER_RECEIVED)
                                                       .setResponseCode(200));
        assertEquals(TRIGGER_RECEIVED, subscriptionManagementService.sendDeletedArtefactForThirdParties(ARTEFACT),
                     "Trigger is not being sent");
    }

    @Test
    void testSendDeletedFailedSend() {
        mockSubscriptionManagementEndpoint.enqueue(new MockResponse()
                                                       .setResponseCode(BAD_REQUEST.value()));
        assertEquals(
            subscriptionManagementService.sendDeletedArtefactForThirdParties(ARTEFACT),
            "Artefact failed to send: " + ARTEFACT.getArtefactId(),
            "Error message failed to send."
        );
        assertTrue(logCaptor.getErrorLogs().get(0)
                       .contains("Request to Subscription Management to send deleted artefact to third party "
                                     + "failed with error:"),
                   "Exception was not logged.");
    }

    @Test
    void testSubscriptionsByLocationId() {
        mockSubscriptionManagementEndpoint.enqueue(new MockResponse()
                                                       .setBody(TRIGGER_RECEIVED)
                                                       .setResponseCode(200));
        assertEquals(TRIGGER_RECEIVED,
                     subscriptionManagementService.findSubscriptionsByLocationId(LOCATION_ID),
                     "Trigger is not being sent");
    }

    @Test
    void testSubscriptionsByLocationIdFailedSend() {
        mockSubscriptionManagementEndpoint.enqueue(new MockResponse()
                                                       .setResponseCode(BAD_REQUEST.value()));
        assertTrue(
            subscriptionManagementService.findSubscriptionsByLocationId(LOCATION_ID)
                .contains("Failed to find subscription for Location: " + LOCATION_ID),
            "Error message failed to send."
        );
        assertTrue(logCaptor.getErrorLogs().get(0)
                       .contains("Request to Subscription Management to find subscriptions for location "
                                     + LOCATION_ID + " failed with error:"),
                   "Exception was not logged.");
    }
}
