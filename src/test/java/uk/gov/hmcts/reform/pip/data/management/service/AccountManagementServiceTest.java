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
import uk.gov.hmcts.reform.pip.data.management.models.publication.ListType;
import uk.gov.hmcts.reform.pip.data.management.models.publication.Sensitivity;

import java.io.IOException;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
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

}
