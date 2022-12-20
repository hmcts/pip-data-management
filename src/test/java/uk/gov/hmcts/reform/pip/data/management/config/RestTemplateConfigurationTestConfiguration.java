package uk.gov.hmcts.reform.pip.data.management.config;

import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.web.client.RestTemplate;

@Configuration
@Profile("test")
public class RestTemplateConfigurationTestConfiguration {

    @Mock
    private RestTemplate mockRestTemplate;

    public RestTemplateConfigurationTestConfiguration() {
        MockitoAnnotations.openMocks(this);
    }

    @Bean
    public RestTemplate restTemplate() {
        return mockRestTemplate;
    }
}
