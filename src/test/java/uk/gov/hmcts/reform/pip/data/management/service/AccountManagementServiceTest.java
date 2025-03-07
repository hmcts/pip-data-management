package uk.gov.hmcts.reform.pip.data.management.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.reactive.function.client.WebClient;
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

    private static final String PI_USER_EMAIL = "test_user@justice.gov.uk";
    private static final String PI_USER_RESPONSE = "{\"email\":\"test_user@justice.gov.uk\"}";

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
}
