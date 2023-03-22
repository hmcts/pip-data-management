package uk.gov.hmcts.reform.pip.data.management.service;

import io.zonky.test.db.AutoConfigureEmbeddedDatabase;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.pip.data.management.Application;
import uk.gov.hmcts.reform.pip.model.account.AzureAccount;
import uk.gov.hmcts.reform.pip.model.publication.ListType;
import uk.gov.hmcts.reform.pip.model.publication.Sensitivity;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SuppressWarnings("PMD.LawOfDemeter")
@ExtendWith({MockitoExtension.class})
@ActiveProfiles("test")
@SpringBootTest(classes = {Application.class})
@AutoConfigureEmbeddedDatabase(type = AutoConfigureEmbeddedDatabase.DatabaseType.POSTGRES)
class AccountManagementServiceTest {

    private static MockWebServer mockAccountManagementEndpoint;

    @Autowired
    AccountManagementService accountManagementService;

    private static final String TRIGGER_RECEIVED = "Trigger has been received";

    @Test
    void testIsAuthorised() throws IOException {
        mockAccountManagementEndpoint = new MockWebServer();
        mockAccountManagementEndpoint.start(6969);
        mockAccountManagementEndpoint.enqueue(new MockResponse().setBody("true")
                                                  .addHeader("Content-Type", "application/json"));

        boolean isAuthorised =
            accountManagementService.getIsAuthorised(UUID.randomUUID(),
                                                     ListType.CIVIL_DAILY_CAUSE_LIST, Sensitivity.PUBLIC);

        assertTrue(isAuthorised, "Authorised has not been returned from the server");

        mockAccountManagementEndpoint.shutdown();
    }

    @Test
    void testIsAuthorisedError() throws IOException {
        mockAccountManagementEndpoint = new MockWebServer();
        mockAccountManagementEndpoint.start(6969);
        mockAccountManagementEndpoint.enqueue(new MockResponse().setResponseCode(HttpStatus.BAD_REQUEST.value()));

        boolean isAuthorised =
            accountManagementService.getIsAuthorised(UUID.randomUUID(),
                                                     ListType.CIVIL_DAILY_CAUSE_LIST, Sensitivity.PUBLIC);

        assertFalse(isAuthorised, "Not authorised has not been returned from the server");

        mockAccountManagementEndpoint.shutdown();
    }

    @Test
    void testGetUserInfo() throws IOException {
        mockAccountManagementEndpoint = new MockWebServer();
        mockAccountManagementEndpoint.start(6969);
        mockAccountManagementEndpoint.enqueue(new MockResponse().setBody(TRIGGER_RECEIVED));

        AzureAccount result =
            accountManagementService.getUserInfo(UUID.randomUUID().toString());

        assertNotNull(result,
                   "User information has not been returned from the server");

        mockAccountManagementEndpoint.shutdown();
    }

    @Test
    void testGetUserInfoError() throws IOException {
        mockAccountManagementEndpoint = new MockWebServer();
        mockAccountManagementEndpoint.start(6969);
        mockAccountManagementEndpoint.enqueue(new MockResponse().setResponseCode(HttpStatus.BAD_REQUEST.value()));

        AzureAccount result =
            accountManagementService.getUserInfo(UUID.randomUUID().toString());

        assertNull(result.getDisplayName(),
                   "User information has not been returned from the server");

        mockAccountManagementEndpoint.shutdown();
    }

    @Test
    void testGetAllAccounts() throws IOException {
        mockAccountManagementEndpoint = new MockWebServer();
        mockAccountManagementEndpoint.start(6969);
        mockAccountManagementEndpoint.enqueue(new MockResponse()
            .setBody("{\"content\":[{\"email\":\"junaid335@yahoo.com\",\"roles\":\"SYSTEM_ADMIN\"}]}"));

        List<String> result =
            accountManagementService.getAllAccounts("prov", "role");

        assertFalse(result.isEmpty(),
                    "System admin users have not been returned from the server");

        mockAccountManagementEndpoint.shutdown();
    }

    @Test
    void testGetAllAccountsError() throws IOException {
        mockAccountManagementEndpoint = new MockWebServer();
        mockAccountManagementEndpoint.start(6969);
        mockAccountManagementEndpoint.enqueue(new MockResponse().setResponseCode(HttpStatus.BAD_REQUEST.value()));

        List<String> result =
            accountManagementService.getAllAccounts("prov", "role");

        assertTrue(result.get(0).contains("Failed to find all the accounts"),
                   "System admin users have not been returned from the server");

        mockAccountManagementEndpoint.shutdown();
    }

}
