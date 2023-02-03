package uk.gov.hmcts.reform.pip.data.management.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientException;
import uk.gov.hmcts.reform.pip.data.management.models.publication.Artefact;

@Slf4j
@Component
public class SubscriptionManagementService {

    @Autowired
    WebClient webClient;

    @Value("${service-to-service.subscription-management}")
    private String url;

    public static final String REQUEST_FAILED = "Request failed with error message: %s";

    public String sendArtefactForSubscription(Artefact artefact) {
        log.info("Attempting to send trigger to " + url);
        try {
            return webClient.post().uri(url + "/subscription/artefact-recipients")
                .body(BodyInserters.fromValue(artefact))
                .retrieve().bodyToMono(String.class).block();
        } catch (WebClientException ex) {
            log.error(String.format(REQUEST_FAILED, ex.getMessage()
            ));
            return "Artefact failed to send: " + artefact.getArtefactId();
        }
    }

    public String sendDeletedArtefactForThirdParties(Artefact artefact) {
        log.info("Attempting to send deletion to " + url);
        try {
            return webClient.post().uri(url + "/subscription/deleted-artefact")
                .body(BodyInserters.fromValue(artefact))
                .retrieve().bodyToMono(String.class).block();
        } catch (WebClientException ex) {
            log.error(String.format(REQUEST_FAILED, ex.getMessage()
            ));
            return "Artefact failed to send: " + artefact.getArtefactId();
        }
    }

    public String findSubscriptionsByLocationId(String locationId) {
        log.info("Attempting to send trigger to " + url);
        try {
            return webClient.get().uri(url + "/subscription/location/" + locationId)
                .retrieve().bodyToMono(String.class).block();
        } catch (WebClientException ex) {
            log.error(String.format(REQUEST_FAILED, ex.getMessage()
            ));
            return "Failed to find subscription for Location: " + locationId + " with status: " + ex.getMessage();
        }
    }

}
