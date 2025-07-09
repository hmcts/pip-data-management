package uk.gov.hmcts.reform.pip.data.management.service.publication;

import nl.altindag.log.LogCaptor;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.pip.data.management.database.ArtefactRepository;
import uk.gov.hmcts.reform.pip.data.management.helpers.ArtefactConstantTestHelper;
import uk.gov.hmcts.reform.pip.data.management.models.publication.Artefact;
import uk.gov.hmcts.reform.pip.data.management.service.AccountManagementService;

import java.io.IOException;
import java.util.List;

import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.pip.data.management.helpers.ArtefactConstantTestHelper.SUCCESS;
import static uk.gov.hmcts.reform.pip.data.management.helpers.ArtefactConstantTestHelper.SUCCESSFUL_TRIGGER;

@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
class PublicationSubscriptionServiceTest {
    private static final String ERROR_LOG_EMPTY = "Error log not empty";

    @Mock
    private ArtefactRepository artefactRepository;

    @Mock
    private AccountManagementService accountManagementService;

    @InjectMocks
    private PublicationSubscriptionService publicationSubscriptionService;

    private final Artefact artefactFromThePast = ArtefactConstantTestHelper.buildArtefactFromThePast();
    private final Artefact artefactFromNow = ArtefactConstantTestHelper.buildArtefactFromNow();
    private final Artefact artefactWithNullDateTo = ArtefactConstantTestHelper.buildArtefactWithNullDateTo();
    private final Artefact artefactWithSameDateFromAndTo = ArtefactConstantTestHelper
        .buildArtefactWithSameDateFromAndTo();
    private final Artefact artefactInTheFuture = ArtefactConstantTestHelper.buildArtefactInTheFuture();

    @Test
    void testTriggerIfDateIsFuture() throws IOException {
        try (LogCaptor logCaptor = LogCaptor.forClass(PublicationSubscriptionService.class)) {
            publicationSubscriptionService.checkAndTriggerPublicationSubscription(artefactInTheFuture);
            assertEquals(
                0,
                logCaptor.getInfoLogs().size(),
                "Should not have returned a log as no trigger was sent."
            );
        } catch (Exception ex) {
            throw new IOException(ex.getMessage());
        }
    }

    @Test
    void testTriggerIfDateIsNow() {
        when(accountManagementService.sendArtefactForSubscription(artefactFromNow)).thenReturn(SUCCESSFUL_TRIGGER);
        try (LogCaptor logCaptor = LogCaptor.forClass(PublicationSubscriptionService.class)) {
            publicationSubscriptionService.checkAndTriggerPublicationSubscription(artefactFromNow);
            assertTrue(ERROR_LOG_EMPTY, logCaptor.getErrorLogs().isEmpty());
        }
    }

    @Test
    void testTriggerIfDateIsPast() {
        when(accountManagementService.sendArtefactForSubscription(artefactFromThePast)).thenReturn(SUCCESSFUL_TRIGGER);
        try (LogCaptor logCaptor = LogCaptor.forClass(PublicationSubscriptionService.class)) {
            publicationSubscriptionService.checkAndTriggerPublicationSubscription(artefactFromThePast);
            assertTrue(ERROR_LOG_EMPTY, logCaptor.getErrorLogs().isEmpty());
        }
    }

    @Test
    void testTriggerIfDateToNull() {
        when(accountManagementService.sendArtefactForSubscription(artefactWithNullDateTo))
            .thenReturn(SUCCESSFUL_TRIGGER);
        try (LogCaptor logCaptor = LogCaptor.forClass(PublicationSubscriptionService.class)) {
            publicationSubscriptionService.checkAndTriggerPublicationSubscription(artefactWithNullDateTo);
            assertTrue(ERROR_LOG_EMPTY, logCaptor.getErrorLogs().isEmpty());
        }
    }

    @Test
    void testTriggerIfSameDateFromTo() {
        when(accountManagementService.sendArtefactForSubscription(artefactWithSameDateFromAndTo))
            .thenReturn(SUCCESSFUL_TRIGGER);
        try (LogCaptor logCaptor = LogCaptor.forClass(PublicationSubscriptionService.class)) {
            publicationSubscriptionService.checkAndTriggerPublicationSubscription(artefactWithSameDateFromAndTo);
            assertTrue(ERROR_LOG_EMPTY, logCaptor.getErrorLogs().isEmpty());
        }
    }

    @Test
    void testCheckNewlyActiveArtefactsLogs() throws IOException {
        try (LogCaptor logCaptor = LogCaptor.forClass(PublicationSubscriptionService.class)) {
            when(artefactRepository.findArtefactsByDisplayFrom(any())).thenReturn(List.of(new Artefact()));
            when(accountManagementService.sendArtefactForSubscription(any())).thenReturn(SUCCESS);
            publicationSubscriptionService.checkNewlyActiveArtefacts();
            assertTrue(ERROR_LOG_EMPTY, logCaptor.getErrorLogs().isEmpty());
        } catch (Exception ex) {
            throw new IOException(ex.getMessage());
        }
    }
}
