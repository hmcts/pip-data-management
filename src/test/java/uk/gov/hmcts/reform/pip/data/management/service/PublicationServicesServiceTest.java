package uk.gov.hmcts.reform.pip.data.management.service;

import io.zonky.test.db.AutoConfigureEmbeddedDatabase;
import nl.altindag.log.LogCaptor;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.pip.data.management.Application;
import uk.gov.hmcts.reform.pip.data.management.models.NoMatchArtefact;
import uk.gov.hmcts.reform.pip.model.system.admin.ActionResult;
import uk.gov.hmcts.reform.pip.model.system.admin.ChangeType;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SuppressWarnings("PMD.LawOfDemeter")
@ExtendWith({MockitoExtension.class})
@ActiveProfiles("test")
@SpringBootTest(classes = {Application.class})
@AutoConfigureEmbeddedDatabase(type = AutoConfigureEmbeddedDatabase.DatabaseType.POSTGRES)
class PublicationServicesServiceTest {
    private static MockWebServer mockPublicationServicesEndpoint;

    @Autowired
    PublicationServicesService publicationServicesService;

    LogCaptor logCaptor = LogCaptor.forClass(PublicationServicesService.class);

    private static final List<NoMatchArtefact> TEST_LIST = new ArrayList<>();
    private static final String EMAIL_SENT = "Email has been sent";

    @BeforeEach
    void setup() throws IOException {
        TEST_LIST.add(new NoMatchArtefact(
            UUID.randomUUID(),
            "TEST",
            "1"
        ));

        mockPublicationServicesEndpoint = new MockWebServer();
        mockPublicationServicesEndpoint.start(8081);
    }

    @AfterEach
    void teardown() throws IOException {
        mockPublicationServicesEndpoint.shutdown();
    }

    @Test
    void testSendNoMatchArtefactsForReporting() {
        mockPublicationServicesEndpoint.enqueue(new MockResponse().setBody(EMAIL_SENT));

        assertEquals(EMAIL_SENT, publicationServicesService
            .sendNoMatchArtefactsForReporting(TEST_LIST), "Email has not been sent");
    }

    @Test
    void testFailedSend() {
        mockPublicationServicesEndpoint.enqueue(new MockResponse()
                                                    .setResponseCode(HttpStatus.BAD_REQUEST.value()));

        publicationServicesService.sendNoMatchArtefactsForReporting(TEST_LIST);
        assertTrue(logCaptor.getErrorLogs().get(0).contains("Unidentified blob email failed to send with error:"),
                   "Exception was not logged.");
    }

    @Test
    void testSendSystemAdminDeleteLocationEmail() {
        mockPublicationServicesEndpoint.enqueue(new MockResponse().setBody(EMAIL_SENT));

        assertEquals(EMAIL_SENT, publicationServicesService
            .sendSystemAdminEmail(List.of("test@test.com"), "Name",
                                  ActionResult.ATTEMPTED, "Error", ChangeType.DELETE_LOCATION),
                     "Email has not been sent");
    }

    @Test
    void testSendSystemAdminDeleteLocationArtefactEmail() {
        mockPublicationServicesEndpoint.enqueue(new MockResponse().setBody(EMAIL_SENT));

        assertEquals(EMAIL_SENT, publicationServicesService
                         .sendSystemAdminEmail(List.of("test@test.com"), "Name",
                                               ActionResult.ATTEMPTED, "Error", ChangeType.DELETE_LOCATION_ARTEFACT),
                     "Email has not been sent");
    }

    @Test
    void testFailedSendSystemAdminEmail() {
        mockPublicationServicesEndpoint.enqueue(new MockResponse()
                                                    .setResponseCode(HttpStatus.BAD_REQUEST.value()));

        publicationServicesService.sendSystemAdminEmail(List.of("test@test.com"), "Name",
                                                        ActionResult.ATTEMPTED, "Error", ChangeType.DELETE_LOCATION);
        assertTrue(logCaptor.getErrorLogs().get(0)
                       .contains("System admin notification email failed to send with error:"),
                   "Exception was not logged.");
    }
}
