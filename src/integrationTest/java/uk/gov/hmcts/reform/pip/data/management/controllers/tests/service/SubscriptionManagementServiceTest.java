package uk.gov.hmcts.reform.pip.data.management.controllers.tests.service;

import io.zonky.test.db.AutoConfigureEmbeddedDatabase;
import nl.altindag.log.LogCaptor;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.pip.data.management.Application;
import uk.gov.hmcts.reform.pip.data.management.models.publication.Artefact;
import uk.gov.hmcts.reform.pip.data.management.service.SubscriptionManagementService;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.http.HttpStatus.BAD_REQUEST;

@ActiveProfiles("test")
@SpringBootTest(classes = {Application.class})
@AutoConfigureEmbeddedDatabase(type = AutoConfigureEmbeddedDatabase.DatabaseType.POSTGRES)
class SubscriptionManagementServiceTest {

    private static final Artefact ARTEFACT = Artefact.builder()
        .sourceArtefactId("TEST")
        .provenance("PROVENANCE")
        .build();

    private static final String LOCATION_ID = "1";
    private static MockWebServer mockSubscriptionManagementEndpoint;

    private static final String TRIGGER_RECEIVED = "Trigger has been received";

    @Autowired
    SubscriptionManagementService subscriptionManagementService;

    LogCaptor logCaptor = LogCaptor.forClass(SubscriptionManagementService.class);

    @BeforeEach
    void setup() throws IOException {
        mockSubscriptionManagementEndpoint = new MockWebServer();
        mockSubscriptionManagementEndpoint.start(4550);
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
