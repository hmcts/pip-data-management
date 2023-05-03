package uk.gov.hmcts.reform.pip.data.management.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientException;
import uk.gov.hmcts.reform.pip.data.management.models.NoMatchArtefact;
import uk.gov.hmcts.reform.pip.model.system.admin.ActionResult;
import uk.gov.hmcts.reform.pip.model.system.admin.ChangeType;
import uk.gov.hmcts.reform.pip.model.system.admin.DeleteLocationAction;
import uk.gov.hmcts.reform.pip.model.system.admin.DeleteLocationArtefactAction;

import java.util.List;

import static org.springframework.security.oauth2.client.web.reactive.function.client.ServerOAuth2AuthorizedClientExchangeFilterFunction.clientRegistrationId;
import static uk.gov.hmcts.reform.pip.model.LogBuilder.writeLog;

@Slf4j
@Component
public class PublicationServicesService {

    @Autowired
    WebClient webClient;

    @Value("${service-to-service.publication-services}")
    private String url;

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

    public String sendSystemAdminEmail(List<String> emails, String requesterName, ActionResult actionResult,
                                       String additionalDetails, ChangeType changeType) {
        Object payload;
        if (changeType.equals(ChangeType.DELETE_LOCATION)) {
            payload = formatDeleteLocationSystemAdminAction(emails, requesterName, actionResult, additionalDetails);
        } else {
            payload = formatDeleteLocationArtefactSystemAdminAction(emails, requesterName,
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

    private DeleteLocationAction formatDeleteLocationSystemAdminAction(List<String> emails,
            String requesterName, ActionResult actionResult, String additionalDetails) {
        DeleteLocationAction systemAdminAction = new DeleteLocationAction();
        systemAdminAction.setEmailList(emails);
        systemAdminAction.setRequesterName(requesterName);
        systemAdminAction.setActionResult(actionResult);
        systemAdminAction.setDetailString(additionalDetails);
        return systemAdminAction;
    }

    private DeleteLocationArtefactAction formatDeleteLocationArtefactSystemAdminAction(List<String> emails,
        String requesterName, ActionResult actionResult, String additionalDetails) {
        DeleteLocationArtefactAction systemAdminAction = new DeleteLocationArtefactAction();
        systemAdminAction.setEmailList(emails);
        systemAdminAction.setRequesterName(requesterName);
        systemAdminAction.setActionResult(actionResult);
        systemAdminAction.setDetailString(additionalDetails);
        return systemAdminAction;
    }
}
