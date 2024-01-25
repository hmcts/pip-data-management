package uk.gov.hmcts.reform.pip.data.management.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientException;
import uk.gov.hmcts.reform.pip.model.publication.Language;
import uk.gov.hmcts.reform.pip.model.publication.ListType;

import java.util.UUID;

import static org.springframework.security.oauth2.client.web.reactive.function.client.ServerOAuth2AuthorizedClientExchangeFilterFunction.clientRegistrationId;
import static uk.gov.hmcts.reform.pip.model.LogBuilder.writeLog;

@Slf4j
@Component
public class ChannelManagementService {
    private static final String LIST_TYPE_HEADER = "x-list-type";
    private static final String LANGUAGE_HEADER = "x-language";

    private final WebClient webClient;

    @Value("${service-to-service.channel-management}")
    private String url;

    @Autowired
    public ChannelManagementService(WebClient webClient) {
        this.webClient = webClient;
    }

    public String requestFileGeneration(UUID artefactId) {
        try {
            return webClient.post()
                .uri(url + "/publication/" + artefactId)
                .attributes(clientRegistrationId("channelManagementApi"))
                .retrieve()
                .bodyToMono(String.class)
                .block();
        } catch (WebClientException ex) {
            log.error(writeLog(String.format(
                "Request to Channel Management to generate files for artefact with ID %s failed with error: %s",
                artefactId, ex.getMessage()
            )));
            return "";
        }
    }

    public void deleteFiles(UUID artefactId, ListType listType, Language language) {
        try {
            webClient.delete()
                .uri(url + "/publication/v2/" + artefactId)
                .header(LIST_TYPE_HEADER, listType.toString())
                .header(LANGUAGE_HEADER, language.toString())
                .attributes(clientRegistrationId("channelManagementApi"))
                .retrieve()
                .bodyToMono(String.class)
                .block();
        } catch (WebClientException ex) {
            log.error(writeLog(String.format(
                "Request to Channel Management to delete files for artefact with ID %s failed with error: %s",
                artefactId, ex.getMessage()
            )));
        }
    }
}
