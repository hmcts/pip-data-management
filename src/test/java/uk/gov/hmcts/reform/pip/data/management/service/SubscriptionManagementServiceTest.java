package uk.gov.hmcts.reform.pip.data.management.service;

import io.zonky.test.db.AutoConfigureEmbeddedDatabase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;
import uk.gov.hmcts.reform.pip.data.management.Application;
import uk.gov.hmcts.reform.pip.data.management.config.AzureBlobConfigurationTest;
import uk.gov.hmcts.reform.pip.data.management.config.RestTemplateConfigurationTest;
import uk.gov.hmcts.reform.pip.data.management.models.external.Subscription;
import uk.gov.hmcts.reform.pip.data.management.models.publication.Artefact;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
@SpringBootTest(classes = {Application.class, AzureBlobConfigurationTest.class, RestTemplateConfigurationTest.class})
@AutoConfigureEmbeddedDatabase(type = AutoConfigureEmbeddedDatabase.DatabaseType.POSTGRES)
class SubscriptionManagementServiceTest {

    @Autowired
    RestTemplate restTemplate;

    private static final String SHOULD_MATCH = "Lists should match";
    private static final String SUBSCRIPTION_URL = "testUrl/subscription/artefact-recipients/{courtId}/{searchTerms}";
    private final Artefact artefact = Artefact.builder()
        .courtId("1")
        .search(new ConcurrentHashMap<>())
        .build();
    private Subscription subscription;

    @InjectMocks
    @Autowired
    SubscriptionManagementService subscriptionManagementService;

    @BeforeEach
    void setup() {
        subscription = new Subscription();
        subscription.setCaseName("test");
        Subscription[] subscriptions = new Subscription[1];
        subscriptions[0] = subscription;
        when(restTemplate.getForEntity(SUBSCRIPTION_URL, Subscription[].class, artefact.getCourtId(),
                                       artefact.getSearch().toString())).thenReturn(ResponseEntity.ok(subscriptions));
    }

    @Test
    void testSuccessReturnsList() {
        assertEquals(List.of(subscription), subscriptionManagementService.getSubscribersToArtefact(artefact),
                     SHOULD_MATCH);
    }

    @Test
    void testSuccessReturnsEmptyList() {
        artefact.setCourtId("2");
        when(restTemplate.getForEntity(SUBSCRIPTION_URL, Subscription[].class, artefact.getCourtId(),
                                                                artefact.getSearch().toString()))
            .thenReturn(ResponseEntity.ok(new Subscription[0]));
        assertEquals(Collections.emptyList(), subscriptionManagementService.getSubscribersToArtefact(artefact),
                     SHOULD_MATCH
        );
    }

    @Test
    void testEmptyListReturnedOnError() {
        artefact.setCourtId("3");
        HttpServerErrorException httpServerErrorException =
            new HttpServerErrorException(HttpStatus.BAD_GATEWAY, "Bad Gateway", null, null);
        doThrow(httpServerErrorException).when(restTemplate)
            .getForEntity(SUBSCRIPTION_URL, Subscription[].class, artefact.getCourtId(),
                          artefact.getSearch().toString());
        assertEquals(Collections.emptyList(), subscriptionManagementService.getSubscribersToArtefact(artefact),
                     SHOULD_MATCH);
    }
}
