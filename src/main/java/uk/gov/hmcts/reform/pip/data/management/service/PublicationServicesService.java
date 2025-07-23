package uk.gov.hmcts.reform.pip.data.management.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientException;
import uk.gov.hmcts.reform.pip.data.management.errorhandling.exceptions.FileProcessingException;
import uk.gov.hmcts.reform.pip.data.management.errorhandling.exceptions.FileUploadException;
import uk.gov.hmcts.reform.pip.data.management.models.publication.NoMatchArtefact;
import uk.gov.hmcts.reform.pip.model.system.admin.ActionResult;
import uk.gov.hmcts.reform.pip.model.system.admin.ChangeType;
import uk.gov.hmcts.reform.pip.model.system.admin.DeleteLocationAction;
import uk.gov.hmcts.reform.pip.model.system.admin.DeleteLocationArtefactAction;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

import static org.springframework.security.oauth2.client.web.reactive.function.client.ServerOAuth2AuthorizedClientExchangeFilterFunction.clientRegistrationId;
import static uk.gov.hmcts.reform.pip.model.LogBuilder.writeLog;

@Slf4j
@Component
public class PublicationServicesService {

    private final WebClient webClient;

    @Value("${service-to-service.publication-services}")
    private String url;

    @Autowired
    public PublicationServicesService(WebClient webClient) {
        this.webClient = webClient;
    }

    public String sendNoMatchArtefactsForReporting(List<NoMatchArtefact> noMatchArtefacts) {
        try {
            return webClient.post().uri(url + "/notify/unidentified-blob")
                .body(BodyInserters.fromValue(noMatchArtefacts))
                .attributes(clientRegistrationId("publicationServicesApi"))
                .retrieve().bodyToMono(String.class).block();
        } catch (WebClientException ex) {
            log.error(writeLog(
                String.format("Unidentified blob email failed to send with error: %s", ex.getMessage())
            ));
            return "";
        }
    }

    public String sendSystemAdminEmail(List<String> emails, String requesterEmail, ActionResult actionResult,
                                       String additionalDetails, ChangeType changeType) {
        Object payload;
        if (changeType.equals(ChangeType.DELETE_LOCATION)) {
            payload = formatDeleteLocationSystemAdminAction(emails, requesterEmail, actionResult, additionalDetails);
        } else {
            payload = formatDeleteLocationArtefactSystemAdminAction(emails, requesterEmail,
                                                                    actionResult, additionalDetails);
        }

        try {
            return webClient.post().uri(url + "/notify/sysadmin/update")
                .body(BodyInserters.fromValue(payload))
                .attributes(clientRegistrationId("publicationServicesApi"))
                .retrieve().bodyToMono(String.class).block();

        } catch (WebClientException ex) {
            log.error(writeLog(
                String.format("System admin notification email failed to send with error: %s", ex.getMessage())
            ));
            return "";
        }
    }

    public String uploadHtmlFileToAwsS3Bucket(MultipartFile file) {
        try {
            MultiValueMap<String, HttpEntity<?>> parts = new LinkedMultiValueMap<>();
            parts.add("file", new HttpEntity<>(file.getResource(), createFileHeaders(file)));

            return webClient.post()
                .uri(url + "/notify/upload-html-to-s3")
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(BodyInserters.fromMultipartData(parts))
                .attributes(clientRegistrationId("publicationServicesApi"))
                .retrieve()
                .bodyToMono(String.class)
                .block();
        } catch (WebClientException ex) {
            log.error("File upload failed: {}", ex.getMessage());
            throw new FileUploadException("Failed to upload file");
        } catch (IOException ex) {
            log.error("File processing error: {}", ex.getMessage());
            throw new FileProcessingException("File processing failed");
        }
    }

    private MultiValueMap<String, String> createFileHeaders(MultipartFile file) throws IOException {
        MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
        headers.add(HttpHeaders.CONTENT_TYPE,
                    MediaType.parseMediaType(Objects.requireNonNull(file.getContentType())).toString());
        headers.add(HttpHeaders.CONTENT_DISPOSITION,
                    "form-data; name=\"file\"; filename=\"" + file.getOriginalFilename() + "\"");

        return headers;
    }

    private DeleteLocationAction formatDeleteLocationSystemAdminAction(List<String> emails,
            String requesterEmail, ActionResult actionResult, String additionalDetails) {
        DeleteLocationAction systemAdminAction = new DeleteLocationAction();
        systemAdminAction.setEmailList(emails);
        systemAdminAction.setRequesterEmail(requesterEmail);
        systemAdminAction.setActionResult(actionResult);
        systemAdminAction.setDetailString(additionalDetails);
        return systemAdminAction;
    }

    private DeleteLocationArtefactAction formatDeleteLocationArtefactSystemAdminAction(List<String> emails,
        String requesterEmail, ActionResult actionResult, String additionalDetails) {
        DeleteLocationArtefactAction systemAdminAction = new DeleteLocationArtefactAction();
        systemAdminAction.setEmailList(emails);
        systemAdminAction.setRequesterEmail(requesterEmail);
        systemAdminAction.setActionResult(actionResult);
        systemAdminAction.setDetailString(additionalDetails);
        return systemAdminAction;
    }
}
