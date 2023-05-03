package uk.gov.hmcts.reform.pip.data.management.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientException;

import java.util.UUID;

import static org.springframework.security.oauth2.client.web.reactive.function.client.ServerOAuth2AuthorizedClientExchangeFilterFunction.clientRegistrationId;
import static uk.gov.hmcts.reform.pip.model.LogBuilder.writeLog;

@Slf4j
@Component
public class ChannelManagementService {

    private static final String EXCEPTION_MESSAGE = "Request to %s failed due to: %s";
    private static final String SERVICE = "Channel Management";

    @Autowired
    WebClient webClient;

    @Value("${service-to-service.channel-management}")
    private String url;

    public String requestFileGeneration(UUID artefactId) {
        try {
            return webClient.post().uri(url + "/publication/" + artefactId)
                .attributes(clientRegistrationId("channelManagementApi"))
                .retrieve().bodyToMono(String.class).block();
        } catch (WebClientException ex) {
            log.error(writeLog(
                String.format("Request to Channel Management to generate files failed with error: %s",
                              ex.getMessage())
            ));
            return "";
        }
    }
}
