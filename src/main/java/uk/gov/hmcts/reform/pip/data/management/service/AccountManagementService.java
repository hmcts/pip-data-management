package uk.gov.hmcts.reform.pip.data.management.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientException;
import uk.gov.hmcts.reform.pip.model.account.AzureAccount;
import uk.gov.hmcts.reform.pip.model.publication.ListType;
import uk.gov.hmcts.reform.pip.model.publication.Sensitivity;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.springframework.security.oauth2.client.web.reactive.function.client.ServerOAuth2AuthorizedClientExchangeFilterFunction.clientRegistrationId;
import static uk.gov.hmcts.reform.pip.model.LogBuilder.writeLog;

@Slf4j
@Component
public class AccountManagementService {

    @Autowired
    WebClient webClient;

    @Value("${service-to-service.account-management}")
    private String url;

    private static final String ACCOUNT_MANAGEMENT_API = "accountManagementApi";

    /**
     * Calls Account Management to determine whether a user is allowed to see a set publication.
     * @param userId The UUID of the user to retrieve.
     * @param listType The list type of the publication.
     * @param sensitivity The sensitivity of the publication
     * @return A flag indicating whether the user is authorised.
     */
    public Boolean getIsAuthorised(UUID userId, ListType listType, Sensitivity sensitivity) {
        try {
            return webClient.get().uri(String.format(
                "%s/account/isAuthorised/%s/%s/%s", url, userId, listType, sensitivity))
                .attributes(clientRegistrationId(ACCOUNT_MANAGEMENT_API))
                .retrieve().bodyToMono(Boolean.class).block();
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
            String result = webClient.get().uri(String.format(
                    "%s/account/all?provenances=%s&roles=%s", url, provenances, role))
                .attributes(clientRegistrationId(ACCOUNT_MANAGEMENT_API))
                .retrieve().bodyToMono(String.class).block();
            return findAllSystemAdmins(result);
        } catch (WebClientException ex) {
            log.error(writeLog(
                String.format("Request to Account Management to get all user accounts failed with error: %s",
                              ex.getMessage())
            ));
            return List.of("Failed to find all the accounts");
        }
    }

    public AzureAccount getUserInfo(String provenanceUserId) {
        try {
            return webClient.get().uri(url + "/account/azure/" + provenanceUserId)
                .attributes(clientRegistrationId(ACCOUNT_MANAGEMENT_API))
                .retrieve().bodyToMono(AzureAccount.class).block();
        } catch (WebClientException ex) {
            log.error(writeLog(
                String.format("Request to Account Management to get Azure account failed with error: %s",
                              ex.getMessage())
            ));
            return new AzureAccount();
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

}
