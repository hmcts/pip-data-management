package uk.gov.hmcts.reform.pip.data.management.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientException;
import uk.gov.hmcts.reform.pip.data.management.models.admin.Action;
import uk.gov.hmcts.reform.pip.data.management.models.external.publication.services.AdminAction;

import java.util.Map;

import static org.springframework.security.oauth2.client.web.reactive.function.client.ServerOAuth2AuthorizedClientExchangeFilterFunction.clientRegistrationId;

@Slf4j
@Component
public class PublicationServicesService {

    private static final String EXCEPTION_MESSAGE = "Request to %s failed due to: %s";
    private static final String SERVICE = "Publications Service";

    @Autowired
    WebClient webClient;

    @Value("${service-to-service.publication-services}")
    private String url;

    public String sendNoMatchArtefactsForReporting(Map<String, String> noMatchMap) {
        try {
            return webClient.post().uri(url + "/notify/unidentified-blob")
                .body(BodyInserters.fromValue(noMatchMap))
                .attributes(clientRegistrationId("publicationServicesApi"))
                .retrieve().bodyToMono(String.class).block();
        } catch (WebClientException ex) {
            log.error(String.format(EXCEPTION_MESSAGE, SERVICE, ex.getMessage()));
            return "";
        }
    }

    public String sendSystemAdminEmail(String email, String requesterName, Action action) {
        AdminAction payload = formatAdminAction(email, requesterName, action);
        try {
            webClient.post().uri(url + "/notify/sysadmin/update")
                .attributes(clientRegistrationId("publicationServicesApi"))
                .body(BodyInserters.fromValue(payload)).retrieve()
                .bodyToMono(Void.class).block();
            return payload.toString();

        } catch (WebClientException ex) {
            log.error(String.format("Request failed with error message: %s", ex.getMessage()));
        }
        return "Request failed";
    }

    private AdminAction formatAdminAction(String email, String requesterName, Action action) {
        AdminAction adminAction = new AdminAction();
        adminAction.setEmail(email);
        adminAction.setName(requesterName);
        adminAction.setChangeType(action.getChangeType().label);
        adminAction.setActionResult(action.getActionResult().label);
        adminAction.setAdditionalInformation(action.getAdditionalDetails());
        return adminAction;
    }
}
