package uk.gov.hmcts.reform.pip.data.management.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientException;
import uk.gov.hmcts.reform.pip.model.system.admin.ActionResult;
import uk.gov.hmcts.reform.pip.model.system.admin.DeleteLocationAction;

import java.util.List;
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

    public String sendSystemAdminEmail(List<String> emails, String requesterName, ActionResult actionResult,
                                       String additionalDetails) {
        DeleteLocationAction payload =
            formatSystemAdminAction(emails, requesterName, actionResult, additionalDetails);
        try {
            return webClient.post().uri(url + "/notify/sysadmin/update")
                .body(BodyInserters.fromValue(payload))
                .attributes(clientRegistrationId("publicationServicesApi"))
                .retrieve().bodyToMono(String.class).block();

        } catch (WebClientException ex) {
            log.error(String.format(EXCEPTION_MESSAGE, SERVICE, ex.getMessage()));
            return "";
        }
    }

    private DeleteLocationAction formatSystemAdminAction(List<String> emails,
            String requesterName, ActionResult actionResult, String additionalDetails) {
        DeleteLocationAction systemAdminAction = new DeleteLocationAction();
        systemAdminAction.setEmailList(emails);
        systemAdminAction.setRequesterName(requesterName);
        systemAdminAction.setActionResult(actionResult);
        systemAdminAction.setDetailString(additionalDetails);
        return systemAdminAction;
    }
}
