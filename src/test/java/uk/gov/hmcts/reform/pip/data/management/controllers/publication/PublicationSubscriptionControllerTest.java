package uk.gov.hmcts.reform.pip.data.management.controllers.publication;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import uk.gov.hmcts.reform.pip.data.management.service.publication.ArtefactTriggerService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doNothing;
import static uk.gov.hmcts.reform.pip.data.management.helpers.ConstantsTestHelper.STATUS_CODE_MATCH;

@ExtendWith(MockitoExtension.class)
class PublicationSubscriptionControllerTest {
    @Mock
    private ArtefactTriggerService artefactTriggerService;

    @InjectMocks
    private PublicationSubscriptionController publicationSubscriptionController;

    @Test
    void testSendNewArtefactsForSubscriptionSuccess() {
        doNothing().when(artefactTriggerService).checkNewlyActiveArtefacts();
        assertThat(publicationSubscriptionController.sendNewArtefactsForSubscription().getStatusCode())
            .as(STATUS_CODE_MATCH)
            .isEqualTo(HttpStatus.NO_CONTENT);
    }
}
