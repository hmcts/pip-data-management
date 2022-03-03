package uk.gov.hmcts.reform.pip.data.management.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientException;
import uk.gov.hmcts.reform.pip.data.management.models.publication.Artefact;

import java.net.URI;
import java.net.URISyntaxException;

@Slf4j
@Component
public class SubscriptionManagementService {

    @Value("${service-to-service.subscription-management}")
    private final String url;

    public SubscriptionManagementService(@Value("${service-to-service.subscription-management}") String url) {
        this.url = url;
    }

    public String sendSubTrigger(Artefact artefact) {
        WebClient webClient = WebClient.create();
        log.info("Attempting to send trigger to " + url);
        try {
            return webClient.post().uri(new URI(url + "/subscription/artefact-recipients"))
                .body(BodyInserters.fromValue(artefact))
                .retrieve().bodyToMono(String.class).block();

        } catch (WebClientException | URISyntaxException ex) {
            log.error(String.format("Request failed with error message: %s", ex.getMessage()
            ));
            return "Request failed";
        }
    }
}
