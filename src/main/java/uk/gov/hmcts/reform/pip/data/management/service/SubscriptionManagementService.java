package uk.gov.hmcts.reform.pip.data.management.service;

import com.azure.core.exception.HttpResponseException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import uk.gov.hmcts.reform.pip.data.management.models.external.Subscription;
import uk.gov.hmcts.reform.pip.data.management.models.publication.Artefact;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.List;

@Slf4j
@Component
public class SubscriptionManagementService {

    @Value("${service-to-service.subscription-management}")
    private final String url;

    @Autowired
    public SubscriptionManagementService(@Value("${service-to-service.subscription-management}") String url) {
        this.url = url;
    }

    public List<Subscription> sendSubTrigger(Artefact artefact) {
        WebClient webClient = WebClient.create();
        log.info(url);
        try {
            return webClient.post().uri(new URI(url + "/subscription/artefact-recipients"))
                .body(BodyInserters.fromValue(artefact))
                .retrieve().bodyToMono(new ParameterizedTypeReference<List<Subscription>>() {
                }).block();

        } catch (HttpResponseException | URISyntaxException ex) {
            log.error(String.format("Request failed with error message: %s", ex.getMessage()
            ));
            return Collections.emptyList();
        }
    }
}
