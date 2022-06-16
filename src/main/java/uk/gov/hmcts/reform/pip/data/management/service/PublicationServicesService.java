package uk.gov.hmcts.reform.pip.data.management.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientException;

import java.util.Map;

import static org.springframework.security.oauth2.client.web.reactive.function.client.ServerOAuth2AuthorizedClientExchangeFilterFunction.clientRegistrationId;

@Slf4j
@Component
public class PublicationServicesService {

    @Autowired
    WebClient webClient;

    @Value("${service-to-service.publication-services}")
    private String url;

    public String sendNoMatchArtefactsForReporting(Map<String, String> noMatchMap) {
        log.info("Attempting to send no m");
        try {
            return webClient.post().uri(url + "/notify/unidentified-blob")
                .body(BodyInserters.fromValue(noMatchMap))
                .attributes(clientRegistrationId("publicationServicesApi"))
                .retrieve().bodyToMono(String.class).block();
        } catch (WebClientException ex) {
            return String.format("Sending map of no match artefacts: %s, "
                                     + "to publication services failed with error message: %s",
                                 noMatchMap, ex.getMessage());
        }
    }
}
