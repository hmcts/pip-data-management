package uk.gov.hmcts.reform.pip.data.management.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientException;
import uk.gov.hmcts.reform.pip.data.management.models.publication.Artefact;

import static uk.gov.hmcts.reform.pip.model.LogBuilder.writeLog;

@Slf4j
@Component
public class SubscriptionManagementService {

    @Autowired
    WebClient webClient;

    @Value("${service-to-service.subscription-management}")
    private String url;

    public String sendArtefactForSubscription(Artefact artefact) {
        try {
            return webClient.post().uri(url + "/subscription/artefact-recipients")
                .body(BodyInserters.fromValue(artefact))
                .retrieve().bodyToMono(String.class).block();
        } catch (WebClientException ex) {
            log.error(writeLog(
                String.format("Request to send artefact to Subscription Management failed with error: %s",
                              ex.getMessage())
            ));
            return "Artefact failed to send: " + artefact.getArtefactId();
        }
    }

    public String sendDeletedArtefactForThirdParties(Artefact artefact) {
        try {
            return webClient.post().uri(url + "/subscription/deleted-artefact")
                .body(BodyInserters.fromValue(artefact))
                .retrieve().bodyToMono(String.class).block();
        } catch (WebClientException ex) {
            log.error(writeLog(
                String.format("Request to Subscription Management to send deleted artefact to third party failed "
                                  + "with error: %s", ex.getMessage())
            ));
            return "Artefact failed to send: " + artefact.getArtefactId();
        }
    }

    public String findSubscriptionsByLocationId(String locationId) {
        try {
            return webClient.get().uri(url + "/subscription/location/" + locationId)
                .retrieve().bodyToMono(String.class).block();
        } catch (WebClientException ex) {
            log.error(writeLog(
                String.format("Request to Subscription Management to find subscriptions for location %s failed "
                                  + "with error: %s", locationId, ex.getMessage())
            ));
            return "Failed to find subscription for Location: " + locationId + " with status: " + ex.getMessage();
        }
    }

}
