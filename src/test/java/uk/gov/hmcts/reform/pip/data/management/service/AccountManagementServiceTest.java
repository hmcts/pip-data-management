package uk.gov.hmcts.reform.pip.data.management.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import nl.altindag.log.LogCaptor;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.reactive.function.client.WebClient;
import uk.gov.hmcts.reform.pip.data.management.models.publication.Artefact;
import uk.gov.hmcts.reform.pip.model.account.PiUser;
import uk.gov.hmcts.reform.pip.model.publication.ListType;
import uk.gov.hmcts.reform.pip.model.publication.Sensitivity;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.http.HttpStatus.BAD_REQUEST;

@ActiveProfiles("test")
class AccountManagementServiceTest {
    private static final Artefact ARTEFACT = Artefact.builder()
        .sourceArtefactId("TEST")
        .provenance("PROVENANCE")
        .build();

    private static final String LOCATION_ID = "1";
    private static final String TRIGGER_RECEIVED = "Trigger has been received";
    private static final String PI_USER_EMAIL = "test_user@justice.gov.uk";
    private static final String PI_USER_RESPONSE = "{\"email\":\"test_user@justice.gov.uk\"}";

    private final LogCaptor logCaptor = LogCaptor.forClass(AccountManagementService.class);

    private final MockWebServer mockAccountManagementEndpoint = new MockWebServer();
    private AccountManagementService accountManagementService;

    @BeforeEach
    void setup() {
        WebClient mockedWebClient = WebClient.builder()
            .baseUrl(mockAccountManagementEndpoint.url("/").toString())
            .build();
        accountManagementService = new AccountManagementService(mockedWebClient);
    }

    @AfterEach
    void shutdown() throws IOException {
        mockAccountManagementEndpoint.shutdown();
    }

    @Test
    void testIsAuthorised() {
        mockAccountManagementEndpoint.enqueue(new MockResponse().setBody("true")
                                                  .addHeader("Content-Type", "application/json"));

        boolean isAuthorised = accountManagementService.getIsAuthorised(UUID.randomUUID(),
                                                                        ListType.CIVIL_DAILY_CAUSE_LIST,
                                                                        Sensitivity.PUBLIC);
        assertTrue(isAuthorised, "Authorised has not been returned from the server");
    }

    @Test
    void testIsAuthorisedError() {
        mockAccountManagementEndpoint.enqueue(new MockResponse().setResponseCode(BAD_REQUEST.value()));

        boolean isAuthorised = accountManagementService.getIsAuthorised(UUID.randomUUID(),
                                                                        ListType.CIVIL_DAILY_CAUSE_LIST,
                                                                        Sensitivity.PUBLIC);
        assertFalse(isAuthorised, "Not authorised has not been returned from the server");
    }

    @Test
    void testGetUserById() {
        mockAccountManagementEndpoint.enqueue(new MockResponse()
                                                  .setHeader("content-type", "application/json")
                                                  .setBody(PI_USER_RESPONSE));

        PiUser result = accountManagementService.getUserById(UUID.randomUUID().toString());
        assertEquals(PI_USER_EMAIL, result.getEmail(),
                     "User information has not been returned from the server");
    }

    @Test
    void testGetUserInfoError() {
        mockAccountManagementEndpoint.enqueue(new MockResponse().setResponseCode(BAD_REQUEST.value()));

        PiUser result = accountManagementService.getUserById(UUID.randomUUID().toString());
        assertNull(result.getEmail(), "User information been returned from the server");
    }

    @Test
    void testGetAllAccounts() throws JsonProcessingException {
        mockAccountManagementEndpoint.enqueue(new MockResponse().setBody(
            "{\"content\":[{\"email\":\"test_email_account_pip@hmcts.net\",\"roles\":\"SYSTEM_ADMIN\"}]}"
        ));

        List<String> result = accountManagementService.getAllAccounts("prov", "role");
        assertFalse(result.isEmpty(), "System admin users have not been returned from the server");
    }

    @Test
    void testGetAllAccountsError() throws JsonProcessingException {
        mockAccountManagementEndpoint.enqueue(new MockResponse().setResponseCode(BAD_REQUEST.value()));

        List<String> result = accountManagementService.getAllAccounts("prov", "role");
        assertTrue(result.get(0).contains("Failed to find all the accounts"),
                   "System admin users have not been returned from the server");
    }

    @Test
    void testSendTrigger() {
        mockAccountManagementEndpoint.enqueue(new MockResponse()
                                                       .setBody(TRIGGER_RECEIVED));
        assertEquals(TRIGGER_RECEIVED, accountManagementService.sendArtefactForSubscription(ARTEFACT),
                     "Trigger is not being sent");
    }

    @Test
    void testFailedSend() {
        mockAccountManagementEndpoint.enqueue(new MockResponse()
                                                       .setResponseCode(BAD_REQUEST.value()));
        assertEquals(accountManagementService.sendArtefactForSubscription(ARTEFACT),
                     "Artefact failed to send: " + ARTEFACT.getArtefactId(), "Error message failed to send.");
        assertTrue(logCaptor.getErrorLogs().get(0)
                       .contains("Request to send artefact to Account Management failed with error:"),
                   "Exception was not logged.");
    }

    @Test
    void testSendDeletedArtefact() {
        mockAccountManagementEndpoint.enqueue(new MockResponse()
                                                       .setBody(TRIGGER_RECEIVED)
                                                       .setResponseCode(200));
        assertEquals(TRIGGER_RECEIVED, accountManagementService.sendDeletedArtefactForThirdParties(ARTEFACT),
                     "Trigger is not being sent");
    }

    @Test
    void testSendDeletedFailedSend() {
        mockAccountManagementEndpoint.enqueue(new MockResponse()
                                                       .setResponseCode(BAD_REQUEST.value()));
        assertEquals(accountManagementService.sendDeletedArtefactForThirdParties(ARTEFACT),
                     "Artefact failed to send: " + ARTEFACT.getArtefactId(), "Error message failed to send.");
        assertTrue(logCaptor.getErrorLogs().get(0)
                       .contains("Request to Account Management to send deleted artefact to third party "
                                     + "failed with error:"),
                   "Exception was not logged.");
    }

    @Test
    void testSubscriptionsByLocationId() {
        mockAccountManagementEndpoint.enqueue(new MockResponse()
                                                       .setBody(TRIGGER_RECEIVED)
                                                       .setResponseCode(200));
        assertEquals(TRIGGER_RECEIVED, accountManagementService.findSubscriptionsByLocationId(LOCATION_ID),
                     "Trigger is not being sent");
    }

    @Test
    void testSubscriptionsByLocationIdFailedSend() {
        mockAccountManagementEndpoint.enqueue(new MockResponse()
                                                       .setResponseCode(BAD_REQUEST.value()));
        assertTrue(accountManagementService.findSubscriptionsByLocationId(LOCATION_ID)
                       .contains("Failed to find subscription for Location: " + LOCATION_ID),
                   "Error message failed to send.");
        assertTrue(logCaptor.getErrorLogs().get(0)
                       .contains("Request to Account Management to find subscriptions for location "
                                     + LOCATION_ID + " failed with error:"),
                   "Exception was not logged.");
    }
}
