package uk.gov.hmcts.reform.pip.data.management.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientException;
import uk.gov.hmcts.reform.pip.data.management.models.publication.ListType;
import uk.gov.hmcts.reform.pip.data.management.models.publication.Sensitivity;

import java.util.UUID;

import static org.springframework.security.oauth2.client.web.reactive.function.client.ServerOAuth2AuthorizedClientExchangeFilterFunction.clientRegistrationId;

@Slf4j
@Component
public class AccountManagementService {

    @Autowired
    WebClient webClient;

    @Value("${service-to-service.account-management}")
    private String url;

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
                .attributes(clientRegistrationId("accountManagementApi"))
                .retrieve().bodyToMono(Boolean.class).block();
        } catch (WebClientException ex) {
            log.error(String.format("Request to account management failed with error message: %s", ex.getMessage()));
            return false;
        }
    }

}
