package uk.gov.hmcts.reform.pip.data.management.service;

import nl.altindag.log.LogCaptor;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.client.WebClient;
import uk.gov.hmcts.reform.pip.data.management.errorhandling.exceptions.FileUploadException;
import uk.gov.hmcts.reform.pip.data.management.models.publication.NoMatchArtefact;
import uk.gov.hmcts.reform.pip.model.system.admin.ActionResult;
import uk.gov.hmcts.reform.pip.model.system.admin.ChangeType;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.http.HttpStatus.BAD_REQUEST;

@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
class PublicationServicesServiceTest {

    private static final List<NoMatchArtefact> TEST_LIST = new ArrayList<>();
    private static final String EMAIL_SENT = "Email has been sent";
    private static final String TEST_EMAIL = "test_eamil@justice.gov.uk";

    private final MockWebServer mockPublicationServicesEndpoint = new MockWebServer();
    private PublicationServicesService publicationServicesService;

    LogCaptor logCaptor = LogCaptor.forClass(PublicationServicesService.class);

    @Mock
    private MultipartFile multipartFile;

    @Mock
    private Resource mockResource;

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

    @Test
    void testUploadHtmlFileSuccess() {
        String htmlContent = "<html><body><h1>Hello World</h1></body></html>";
        MockMultipartFile multipartFile = new MockMultipartFile(
            "file",
            "test.html",
            MediaType.TEXT_HTML_VALUE,
            htmlContent.getBytes(StandardCharsets.UTF_8)
        );

        String expectedResponse = "File uploaded successfully to AWS S3 Bucket";
        mockPublicationServicesEndpoint.enqueue(new MockResponse()
                                                    .setResponseCode(200)
                                                    .setBody(expectedResponse)
                                                    .addHeader("Content-Type", "text/plain"));

        String result = publicationServicesService.uploadHtmlFileToAwsS3Bucket(multipartFile);

        assertEquals(expectedResponse, result, "Response message is not matching");
    }

    @Test
    void testUploadHtmlFileThrowsException() {
        String htmlContent = "<html><body>Error case</body></html>";
        MockMultipartFile multipartFile = new MockMultipartFile(
            "file", "error.html",
            MediaType.TEXT_HTML_VALUE,
            htmlContent.getBytes(StandardCharsets.UTF_8)
        );

        mockPublicationServicesEndpoint.enqueue(new MockResponse()
                                                    .setResponseCode(500)
                                                    .setBody("Internal Server Error"));

        assertThrows(
            FileUploadException.class, () ->
            publicationServicesService.uploadHtmlFileToAwsS3Bucket(multipartFile)
        );

        assertTrue(logCaptor.getErrorLogs().get(0)
                       .contains("File upload failed:"),
                   "Exception was not logged.");
    }

}
