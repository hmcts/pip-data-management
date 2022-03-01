package uk.gov.hmcts.reform.pip.data.management.service;

import com.azure.core.exception.HttpResponseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;
import uk.gov.hmcts.reform.pip.data.management.models.external.Subscription;
import uk.gov.hmcts.reform.pip.data.management.models.publication.Artefact;

import java.util.Collections;
import java.util.List;

@Slf4j
@Component
public class SubscriptionManagementService {

    @Value("${service-to-service.subscription-management}")
    private String url;

    private final ObjectMapper om = new ObjectMapper();

    @Autowired
    private RestTemplate restTemplate;

    public List<Subscription> getSubscribersToArtefact(Artefact artefact) {
        try {
            ResponseEntity<Subscription[]> response = this.restTemplate
                    .getForEntity(url + "/subscription/artefact-recipients/{courtId}/{searchTerms}",
                             Subscription[].class, artefact.getCourtId(), artefact.getSearch().toString());
            return om.convertValue(response.getBody(), new TypeReference<>() {
            });
        } catch (HttpResponseException | HttpStatusCodeException ex) {
            log.error(String.format("Subscription management request failed with error message: %s", ex.getMessage()
            ));
        }
        return Collections.emptyList();
    }
}
