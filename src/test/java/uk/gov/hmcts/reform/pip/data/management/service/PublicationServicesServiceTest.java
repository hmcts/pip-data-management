package uk.gov.hmcts.reform.pip.data.management.service;

import nl.altindag.log.LogCaptor;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.reactive.function.client.WebClient;
import uk.gov.hmcts.reform.pip.data.management.models.publication.NoMatchArtefact;
import uk.gov.hmcts.reform.pip.model.system.admin.ActionResult;
import uk.gov.hmcts.reform.pip.model.system.admin.ChangeType;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.http.HttpStatus.BAD_REQUEST;

@ActiveProfiles("test")
class PublicationServicesServiceTest {

    private static final List<NoMatchArtefact> TEST_LIST = new ArrayList<>();
    private static final String EMAIL_SENT = "Email has been sent";
    private static final String TEST_EMAIL = "test_eamil@justice.gov.uk";

    private final MockWebServer mockPublicationServicesEndpoint = new MockWebServer();
    private PublicationServicesService publicationServicesService;

    LogCaptor logCaptor = LogCaptor.forClass(PublicationServicesService.class);

    @BeforeEach
    void setup() {
        TEST_LIST.add(new NoMatchArtefact(UUID.randomUUID(), "TEST", "1"));

        WebClient mockedWebClient = WebClient.builder()
            .baseUrl(mockPublicationServicesEndpoint.url("/").toString())
            .build();
        publicationServicesService = new PublicationServicesService(mockedWebClient);
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
                                                    .setResponseCode(BAD_REQUEST.value()));

        publicationServicesService.sendNoMatchArtefactsForReporting(TEST_LIST);
        assertTrue(logCaptor.getErrorLogs().get(0).contains("Unidentified blob email failed to send with error:"),
                   "Exception was not logged.");
    }

    @Test
    void testSendSystemAdminDeleteLocationEmail() {
        mockPublicationServicesEndpoint.enqueue(new MockResponse().setBody(EMAIL_SENT));

        assertEquals(EMAIL_SENT, publicationServicesService
            .sendSystemAdminEmail(List.of("test@test.com"), TEST_EMAIL,
                                  ActionResult.ATTEMPTED, "Error", ChangeType.DELETE_LOCATION),
                     "Email has not been sent");
    }

    @Test
    void testSendSystemAdminDeleteLocationArtefactEmail() {
        mockPublicationServicesEndpoint.enqueue(new MockResponse().setBody(EMAIL_SENT));

        assertEquals(EMAIL_SENT, publicationServicesService
                         .sendSystemAdminEmail(List.of("test@test.com"), TEST_EMAIL,
                                               ActionResult.ATTEMPTED, "Error", ChangeType.DELETE_LOCATION_ARTEFACT),
                     "Email has not been sent");
    }

    @Test
    void testFailedSendSystemAdminEmail() {
        mockPublicationServicesEndpoint.enqueue(new MockResponse()
                                                    .setResponseCode(BAD_REQUEST.value()));

        publicationServicesService.sendSystemAdminEmail(List.of("test@test.com"), TEST_EMAIL,
                                                        ActionResult.ATTEMPTED, "Error", ChangeType.DELETE_LOCATION);
        assertTrue(logCaptor.getErrorLogs().get(0)
                       .contains("System admin notification email failed to send with error:"),
                   "Exception was not logged.");
    }
}
