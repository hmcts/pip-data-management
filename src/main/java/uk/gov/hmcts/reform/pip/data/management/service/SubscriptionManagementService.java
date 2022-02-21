package uk.gov.hmcts.reform.pip.data.management.service;

import com.azure.core.exception.HttpResponseException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import uk.gov.hmcts.reform.pip.data.management.models.publication.Artefact;

import java.net.URI;
import java.net.URISyntaxException;

@Slf4j
@Component
public class SubscriptionManagementService {

    @Value("${service-to-service.subscription-management}")
    String url;

    WebClient webClient = WebClient.create();

    public void sendSubTrigger(Artefact artefact) {
        log.info(url);
        try {
            log.info(webClient.post().uri(new URI(url)).body(BodyInserters.fromValue(artefact))
                .retrieve().bodyToMono(String.class).block());
        } catch (HttpResponseException | URISyntaxException ex) {
            log.error(String.format("Request failed with error message: %s", ex.getMessage()
            ));
        }
    }
}
