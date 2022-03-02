package uk.gov.hmcts.reform.pip.data.management.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;
import uk.gov.hmcts.reform.pip.data.management.models.publication.Artefact;

@Slf4j
@Component
public class SubscriptionManagementService {

    @Value("${service-to-service.subscription-management}")
    private String url;

    private static final String SUBSCRIPTION_NOT_TRIGGERED = "Subscription trigger unsuccessful for artefact: ";

    @Autowired
    private RestTemplate restTemplate;

    public String getSubscribersToArtefact(Artefact artefact) {
        try {
            return this.restTemplate.postForEntity(url + "/subscription/artefact-recipients",
                                                   artefact, String.class).getBody();
        } catch (HttpServerErrorException | HttpClientErrorException ex) {
            log.error(String.format("Subscription management request failed with error message: %s", ex.getMessage()));
        }
        return SUBSCRIPTION_NOT_TRIGGERED + artefact.getArtefactId();
    }
}
