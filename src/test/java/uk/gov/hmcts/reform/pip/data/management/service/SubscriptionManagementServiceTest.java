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
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;
import uk.gov.hmcts.reform.pip.data.management.Application;
import uk.gov.hmcts.reform.pip.data.management.config.AzureBlobConfigurationTest;
import uk.gov.hmcts.reform.pip.data.management.config.RestTemplateConfigurationTest;
import uk.gov.hmcts.reform.pip.data.management.models.publication.Artefact;

import java.util.UUID;
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

    private static final String SUCCESS = "Success";
    private static final UUID ARTEFACT_ID = UUID.randomUUID();
    private static final String SHOULD_MATCH = "Lists should match";
    private static final String SUBSCRIPTION_URL = "testUrl/subscription/artefact-recipients";
    private final Artefact artefact = Artefact.builder()
        .courtId("1")
        .artefactId(ARTEFACT_ID)
        .search(new ConcurrentHashMap<>())
        .build();

    @InjectMocks
    @Autowired
    SubscriptionManagementService subscriptionManagementService;

    @BeforeEach
    void setup() {
        when(restTemplate.postForEntity(SUBSCRIPTION_URL, artefact, String.class))
            .thenReturn(ResponseEntity.ok(SUCCESS));
    }

    @Test
    void testSuccessReturnsList() {
        assertEquals(SUCCESS, subscriptionManagementService.getSubscribersToArtefact(artefact),
                     SHOULD_MATCH);
    }

    @Test
    void testEmptyListReturnedOnError() {
        artefact.setCourtId("2");

        HttpServerErrorException httpServerErrorException =
            new HttpServerErrorException(HttpStatus.BAD_GATEWAY, "Bad Gateway", null, null);
        doThrow(httpServerErrorException).when(restTemplate)
            .postForEntity(SUBSCRIPTION_URL, artefact, String.class);

        assertEquals("Subscription trigger unsuccessful for artefact: " + ARTEFACT_ID,
                     subscriptionManagementService.getSubscribersToArtefact(artefact),
                     SHOULD_MATCH);
    }

    @Test
    void testErrorMessageOnError() {
        artefact.setCourtId("3");
        HttpClientErrorException httpClientErrorException =
            new HttpClientErrorException(HttpStatus.BAD_GATEWAY, "Bad Gateway", null, null);
        doThrow(httpClientErrorException).when(restTemplate)
            .postForEntity(SUBSCRIPTION_URL, artefact, String.class);
        assertEquals("Subscription trigger unsuccessful for artefact: " + ARTEFACT_ID,
                     subscriptionManagementService.getSubscribersToArtefact(artefact),
                     SHOULD_MATCH);
    }
}
