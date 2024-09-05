package uk.gov.hmcts.reform.pip.data.management.controllers.tests.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
@Profile("test")
public class WebClientTestConfiguration {

    @Bean
    public WebClient webClient() {
        return WebClient.create();
    }

}
