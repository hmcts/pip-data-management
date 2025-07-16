package uk.gov.hmcts.reform.pip.data.management.controllers.publication;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.pip.data.management.service.publication.PublicationSubscriptionService;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.pip.data.management.helpers.ConstantsTestHelper.STATUS_CODE_MATCH;

@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
class PublicationSubscriptionControllerTest {
    private static final String STATUS_MESSAGE = "Status did not match";
    private static final String RESPONSE_BODY_MESSAGE = "Body did not match";

    @Mock
    private PublicationSubscriptionService publicationSubscriptionService;

    @InjectMocks
    private PublicationSubscriptionController publicationSubscriptionController;

    @Test
    void testSendNewArtefactsForSubscriptionSuccess() {
        doNothing().when(publicationSubscriptionService).checkNewlyActiveArtefacts();
        assertThat(publicationSubscriptionController.sendNewArtefactsForSubscription().getStatusCode())
            .as(STATUS_CODE_MATCH)
            .isEqualTo(HttpStatus.NO_CONTENT);
    }

    @Test
    void testGenerateArtefactSummary() {
        when(publicationSubscriptionService.generateArtefactSummary(any())).thenReturn("test1234");
        ResponseEntity<String> response = publicationSubscriptionController
            .generateArtefactSummary(UUID.randomUUID());

        assertEquals(HttpStatus.OK, response.getStatusCode(), STATUS_MESSAGE);
        assertEquals("test1234", response.getBody(), RESPONSE_BODY_MESSAGE);
    }
}
