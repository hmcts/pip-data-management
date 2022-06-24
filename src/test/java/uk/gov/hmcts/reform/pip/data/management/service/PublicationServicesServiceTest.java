package uk.gov.hmcts.reform.pip.data.management.service;

import io.zonky.test.db.AutoConfigureEmbeddedDatabase;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.pip.data.management.Application;
import uk.gov.hmcts.reform.pip.data.management.errorhandling.exceptions.ServiceToServiceException;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
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

    private static final Map<String, String> TEST_MAP = new ConcurrentHashMap<>();
    private static final String EXCEPTION_THROWN_MESSAGE = "Expected exception has not been thrown";
    private static final String EXCEPTION_RESPONSE_MESSAGE =
        "Exception response does not contain the status code in the message";

    @BeforeEach
    void setup() throws IOException {
        TEST_MAP.put("1", "test");

        mockPublicationServicesEndpoint = new MockWebServer();
        mockPublicationServicesEndpoint.start(8081);
    }

    @AfterEach
    void teardown() throws IOException {
        mockPublicationServicesEndpoint.shutdown();
    }

    @Test
    void testSendNoMatchArtefactsForReporting() {
        mockPublicationServicesEndpoint.enqueue(new MockResponse().setBody("Email has been sent"));

        assertEquals("Email has been sent", publicationServicesService
            .sendNoMatchArtefactsForReporting(TEST_MAP), "Email has not been sent");
    }

    @Test
    void testFailedSend() {
        mockPublicationServicesEndpoint.enqueue(new MockResponse()
                                                    .setResponseCode(501));


        ServiceToServiceException serviceToServiceException = assertThrows(ServiceToServiceException.class, () ->
            publicationServicesService.sendNoMatchArtefactsForReporting(TEST_MAP), EXCEPTION_THROWN_MESSAGE);

        assertTrue(serviceToServiceException.getMessage().contains("501"), EXCEPTION_RESPONSE_MESSAGE);
    }
}
