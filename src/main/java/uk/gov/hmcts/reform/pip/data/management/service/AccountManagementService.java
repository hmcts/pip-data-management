package uk.gov.hmcts.reform.pip.data.management.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientException;
import uk.gov.hmcts.reform.pip.data.management.models.publication.Artefact;
import uk.gov.hmcts.reform.pip.model.account.PiUser;
import uk.gov.hmcts.reform.pip.model.publication.ListType;
import uk.gov.hmcts.reform.pip.model.publication.Sensitivity;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static uk.gov.hmcts.reform.pip.model.LogBuilder.writeLog;

@Slf4j
@Component
public class AccountManagementService {

    private final WebClient webClient;

    @Value("${service-to-service.account-management}")
    private String url;

    @Autowired
    public AccountManagementService(WebClient webClient) {
        this.webClient = webClient;
    }

    /**
     * Calls Account Management to determine whether a user is allowed to see a set publication.
     * @param userId The UUID of the user to retrieve.
     * @param listType The list type of the publication.
     * @param sensitivity The sensitivity of the publication
     * @return A flag indicating whether the user is authorised.
     */
    public Boolean getIsAuthorised(UUID userId, ListType listType, Sensitivity sensitivity) {
        try {
            return webClient.get()
                .uri(String.format("%s/account/isAuthorised/%s/%s/%s", url, userId, listType, sensitivity))
                .retrieve()
                .bodyToMono(Boolean.class)
                .block();
        } catch (WebClientException ex) {
            log.error(writeLog(
                String.format("Request to Account Management to check user authorisation failed with error: %s",
                              ex.getMessage())
            ));
            return false;
        }
    }

    public List<String> getAllAccounts(String provenances, String role)
        throws JsonProcessingException  {
        try {
            String result = webClient.get()
                .uri(String.format("%s/account/all?provenances=%s&roles=%s", url, provenances, role))
                .retrieve()
                .bodyToMono(String.class)
                .block();
            return findAllSystemAdmins(result);
        } catch (WebClientException ex) {
            log.error(writeLog(
                String.format("Request to Account Management to get all user accounts failed with error: %s",
                              ex.getMessage())
            ));
            return List.of("Failed to find all the accounts");
        }
    }

    public PiUser getUserById(String userId) {
        try {
            return webClient.get()
                .uri(url + "/account/" + userId)
                .retrieve()
                .bodyToMono(PiUser.class)
                .block();
        } catch (WebClientException ex) {
            log.error(writeLog(
                String.format("Request to Account Management to get PI User account failed with error: %s",
                              ex.getMessage())
            ));
            return new PiUser();
        }
    }

    private List<String> findAllSystemAdmins(String result) throws JsonProcessingException {
        List<String> systemAdmins = new ArrayList<>();
        JsonNode node = new ObjectMapper().readTree(result);
        if (!node.isEmpty()) {
            JsonNode content = node.get("content");
            content.forEach(jsonObject -> {
                if (jsonObject.has("roles")) {
                    systemAdmins.add(jsonObject.get("email").asText());
                }
            });
        }
        return systemAdmins;
    }

    public String sendArtefactForSubscription(Artefact artefact) {
        try {
            return webClient.post()
                .uri(url + "/subscription/artefact-recipients")
                .body(BodyInserters.fromValue(artefact))
                .retrieve()
                .bodyToMono(String.class)
                .block();
        } catch (WebClientException ex) {
            log.error(writeLog(
                String.format("Request to send artefact to Account Management failed with error: %s",
                              ex.getMessage())
            ));
            return "Artefact failed to send: " + artefact.getArtefactId();
        }
    }

    public String sendDeletedArtefactForThirdParties(Artefact artefact) {
        try {
            return webClient.post().uri(url + "/subscription/deleted-artefact")
                .body(BodyInserters.fromValue(artefact))
                .retrieve()
                .bodyToMono(String.class)
                .block();
        } catch (WebClientException ex) {
            log.error(writeLog(
                String.format("Request to Account Management to send deleted artefact to third party failed "
                                  + "with error: %s", ex.getMessage())
            ));
            return "Artefact failed to send: " + artefact.getArtefactId();
        }
    }

    public String findSubscriptionsByLocationId(String locationId) {
        try {
            return webClient.get().uri(url + "/subscription/location/" + locationId)
                .retrieve()
                .bodyToMono(String.class)
                .block();
        } catch (WebClientException ex) {
            log.error(writeLog(
                String.format("Request to Account Management to find subscriptions for location %s failed "
                                  + "with error: %s", locationId, ex.getMessage())
            ));
            return "Failed to find subscription for Location: " + locationId + " with status: " + ex.getMessage();
        }
    }
}
