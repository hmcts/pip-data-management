package uk.gov.hmcts.reform.pip.data.management.service;

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
import uk.gov.hmcts.reform.pip.model.publication.Language;
import uk.gov.hmcts.reform.pip.model.publication.ListType;

import java.io.IOException;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.NO_CONTENT;


@ActiveProfiles("test")
@SpringBootTest(classes = {Application.class})
@AutoConfigureEmbeddedDatabase(type = AutoConfigureEmbeddedDatabase.DatabaseType.POSTGRES)
class ChannelManagementServiceTest {
    private static final UUID ARTEFACT_ID = UUID.randomUUID();
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
                                                    .setResponseCode(BAD_REQUEST.value()));

        channelManagementService.requestFileGeneration(ARTEFACT_ID);
        assertThat(logCaptor.getErrorLogs().get(0))
            .as("Error log does not match")
            .contains(String.format(
                "Request to Channel Management to generate files for artefact with ID %s failed with error:",
                ARTEFACT_ID
            ));
    }

    @Test
    void testDeleteFilesSuccess() {
        mockChannelManagementEndpoint.enqueue(new MockResponse()
                                                  .setResponseCode(NO_CONTENT.value()));

        channelManagementService.deleteFiles(ARTEFACT_ID, ListType.FAMILY_DAILY_CAUSE_LIST, Language.ENGLISH);
        assertThat(logCaptor.getErrorLogs())
            .as("Error log should be empty")
            .isEmpty();
    }

    @Test
    void testDeleteFilesWithException() {
        mockChannelManagementEndpoint.enqueue(new MockResponse()
                                                  .setResponseCode(BAD_REQUEST.value()));

        channelManagementService.deleteFiles(ARTEFACT_ID, ListType.FAMILY_DAILY_CAUSE_LIST, Language.ENGLISH);
        assertThat(logCaptor.getErrorLogs().get(0))
            .as("Error log does not match")
            .contains(String.format(
                      "Request to Channel Management to delete files for artefact with ID %s failed with error:",
                      ARTEFACT_ID
            ));
    }
}
