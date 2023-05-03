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

import java.io.IOException;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SuppressWarnings("PMD.LawOfDemeter")
@ExtendWith({MockitoExtension.class})
@ActiveProfiles("test")
@SpringBootTest(classes = {Application.class})
@AutoConfigureEmbeddedDatabase(type = AutoConfigureEmbeddedDatabase.DatabaseType.POSTGRES)
class ChannelManagementServiceTest {

    private static MockWebServer mockChannelManagementEndpoint;

    @Autowired
    ChannelManagementService channelManagementService;

    LogCaptor logCaptor = LogCaptor.forClass(ChannelManagementService.class);

    @BeforeEach
    void setup() throws IOException {

        mockChannelManagementEndpoint = new MockWebServer();
        mockChannelManagementEndpoint.start(8181);
    }

    @AfterEach
    void teardown() throws IOException {
        mockChannelManagementEndpoint.shutdown();
    }

    @Test
    void testRequestFileGeneration() {
        mockChannelManagementEndpoint.enqueue(new MockResponse().setBody("1234"));

        assertEquals("1234", channelManagementService.requestFileGeneration(UUID.randomUUID()),
                     "Request was not sent");
    }

    @Test
    void testFailedSend() {
        mockChannelManagementEndpoint.enqueue(new MockResponse()
                                                    .setResponseCode(HttpStatus.BAD_REQUEST.value()));

        channelManagementService.requestFileGeneration(UUID.randomUUID());
        assertTrue(logCaptor.getErrorLogs().get(0)
                       .contains("Request to Channel Management to generate files failed with error:"),
                   "Exception was not logged.");
    }
}
