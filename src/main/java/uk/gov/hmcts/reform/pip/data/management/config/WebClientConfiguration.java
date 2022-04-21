package uk.gov.hmcts.reform.pip.data.management.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizedClientRepository;
import org.springframework.security.oauth2.client.web.reactive.function.client.ServletOAuth2AuthorizedClientExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * Configures the Web Client that is used in requests to external services.
 */
@Configuration
@Profile("!test")
public class WebClientConfiguration {

    @Bean
    WebClient webClient(ClientRegistrationRepository clientRegistrations,
                        OAuth2AuthorizedClientRepository authorizedClients) {
        ServletOAuth2AuthorizedClientExchangeFilterFunction oauth2 =
            new ServletOAuth2AuthorizedClientExchangeFilterFunction(
            clientRegistrations, authorizedClients);
        oauth2.setDefaultClientRegistrationId("subscriptionManagementApi");
        return WebClient.builder().apply(oauth2.oauth2Configuration()).build();
    }

}
