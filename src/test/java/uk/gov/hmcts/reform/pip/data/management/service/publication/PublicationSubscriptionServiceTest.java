package uk.gov.hmcts.reform.pip.data.management.service.publication;

import nl.altindag.log.LogCaptor;
import org.junit.jupiter.api.BeforeEach;
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
import uk.gov.hmcts.reform.pip.data.management.service.ListConversionFactory;
import uk.gov.hmcts.reform.pip.data.management.service.artefactsummary.CivilDailyCauseListSummaryData;
import uk.gov.hmcts.reform.pip.data.management.service.artefactsummary.NonStrategicListSummaryData;
import uk.gov.hmcts.reform.pip.model.publication.ListType;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.pip.data.management.helpers.ArtefactConstantTestHelper.SUCCESS;
import static uk.gov.hmcts.reform.pip.data.management.helpers.ArtefactConstantTestHelper.SUCCESSFUL_TRIGGER;

@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
class PublicationSubscriptionServiceTest {
    private static final UUID TEST_ARTEFACT_ID = UUID.randomUUID();
    private static final String TEST = "test";
    private static final Artefact ARTEFACT = new Artefact();

    private static final String RESPONSE_MESSAGE = "Response didn't contain expected text";
    private static final String ERROR_LOG_EMPTY = "Error log not empty";

    @Mock
    private PublicationRetrievalService publicationRetrievalService;

    @Mock
    private PublicationSummaryGenerationService publicationSummaryGenerationService;

    @Mock
    private ListConversionFactory listConversionFactory;

    @Mock
    private ArtefactRepository artefactRepository;

    @Mock
    private AccountManagementService accountManagementService;

    @Mock
    private CivilDailyCauseListSummaryData civilDailyCauseListSummaryData;

    @InjectMocks
    private PublicationSubscriptionService publicationSubscriptionService;

    private final Artefact artefactFromThePast = ArtefactConstantTestHelper.buildArtefactFromThePast();
    private final Artefact artefactFromNow = ArtefactConstantTestHelper.buildArtefactFromNow();
    private final Artefact artefactWithNullDateTo = ArtefactConstantTestHelper.buildArtefactWithNullDateTo();
    private final Artefact artefactWithSameDateFromAndTo = ArtefactConstantTestHelper
        .buildArtefactWithSameDateFromAndTo();
    private final Artefact artefactInTheFuture = ArtefactConstantTestHelper.buildArtefactInTheFuture();

    @BeforeEach
    void setup() {
        ARTEFACT.setArtefactId(TEST_ARTEFACT_ID);
        ARTEFACT.setListType(ListType.SJP_PUBLIC_LIST);
        ARTEFACT.setPayloadSize(100F);
    }

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
            when(artefactRepository.findArtefactsByDisplayFrom(any(), any())).thenReturn(List.of(new Artefact()));
            when(accountManagementService.sendArtefactForSubscription(any())).thenReturn(SUCCESS);
            publicationSubscriptionService.checkNewlyActiveArtefacts();
            assertTrue(ERROR_LOG_EMPTY, logCaptor.getErrorLogs().isEmpty());
        } catch (Exception ex) {
            throw new IOException(ex.getMessage());
        }
    }

    @Test
    void testGenerateArtefactSummarySuccess() {
        when(publicationRetrievalService.getMetadataByArtefactId(any())).thenReturn(ARTEFACT);
        when(listConversionFactory.getArtefactSummaryData(any(ListType.class)))
            .thenReturn(Optional.of(civilDailyCauseListSummaryData));
        when(publicationRetrievalService.getPayloadByArtefactId(any())).thenReturn("{}");
        when(publicationSummaryGenerationService.generate(any())).thenReturn(TEST);

        String response = publicationSubscriptionService.generateArtefactSummary(TEST_ARTEFACT_ID);
        assertFalse(response.isEmpty(), RESPONSE_MESSAGE);

        verify(civilDailyCauseListSummaryData).get(any());
    }

    @Test
    void testGenerateArtefactSummaryNonStrategicPublishing() {
        Artefact artefact = new Artefact();
        artefact.setArtefactId(TEST_ARTEFACT_ID);
        artefact.setListType(ListType.CST_WEEKLY_HEARING_LIST);

        NonStrategicListSummaryData nonStrategicListSummaryData = new NonStrategicListSummaryData(
            ListType.CST_WEEKLY_HEARING_LIST
        );

        when(publicationRetrievalService.getMetadataByArtefactId(any())).thenReturn(artefact);
        when(listConversionFactory.getArtefactSummaryData(any(ListType.class)))
            .thenReturn(Optional.of(nonStrategicListSummaryData));
        when(publicationRetrievalService.getPayloadByArtefactId(any())).thenReturn("[{\"date\":\"01/01/2025\"}]");
        when(publicationSummaryGenerationService.generate(any())).thenReturn(TEST);

        String response = publicationSubscriptionService.generateArtefactSummary(TEST_ARTEFACT_ID);
        assertFalse(response.isEmpty(), RESPONSE_MESSAGE);
    }

    @Test
    void testGenerateArtefactSummaryWhenSummaryIsEmpty() {
        when(publicationRetrievalService.getMetadataByArtefactId(TEST_ARTEFACT_ID)).thenReturn(ARTEFACT);
        when(listConversionFactory.getArtefactSummaryData(any(ListType.class))).thenReturn(Optional.empty());

        assertEquals("", publicationSubscriptionService.generateArtefactSummary(TEST_ARTEFACT_ID),
                     RESPONSE_MESSAGE);
        verify(publicationRetrievalService, never()).getPayloadByArtefactId(TEST_ARTEFACT_ID);
    }
}
