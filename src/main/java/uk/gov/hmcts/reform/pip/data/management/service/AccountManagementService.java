package uk.gov.hmcts.reform.pip.data.management.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientException;
import uk.gov.hmcts.reform.pip.data.management.models.request.PiUser;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Optional;
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
     * Retrieves the user from account management.
     * @param userId The UUID of the user to retrieve
     * @return The found user, or an empty optional if not found.
     */
    public Optional<PiUser> getUser(UUID userId) {
        try {
            return Optional.ofNullable(webClient.get().uri(new URI(url + "/account/" + userId))
                .attributes(clientRegistrationId("accountManagementApi"))
                .retrieve().bodyToMono(PiUser.class).block());
        } catch (WebClientException | URISyntaxException ex) {
            log.error(String.format("Request failed with error message: %s", ex.getMessage()));
            return Optional.empty();
        }
    }

}
