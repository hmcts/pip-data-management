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
import uk.gov.hmcts.reform.pip.data.management.database.ArtefactSearchRepository;
import uk.gov.hmcts.reform.pip.data.management.helpers.ArtefactConstantTestHelper;
import uk.gov.hmcts.reform.pip.data.management.models.publication.Artefact;
import uk.gov.hmcts.reform.pip.data.management.models.publication.ArtefactSearch;
import uk.gov.hmcts.reform.pip.data.management.service.AccountManagementService;
import uk.gov.hmcts.reform.pip.data.management.service.ListConversionFactory;
import uk.gov.hmcts.reform.pip.data.management.service.artefactsummary.CivilDailyCauseListSummaryData;
import uk.gov.hmcts.reform.pip.data.management.service.artefactsummary.NonStrategicListSummaryData;
import uk.gov.hmcts.reform.pip.model.publication.ArtefactCaseInfo;
import uk.gov.hmcts.reform.pip.model.publication.Language;
import uk.gov.hmcts.reform.pip.model.publication.ListType;
import uk.gov.hmcts.reform.pip.model.publication.Sensitivity;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.pip.data.management.helpers.ArtefactConstantTestHelper.CONTENT_DATE;

@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
class PublicationSubscriptionServiceTest {
    private static final UUID TEST_ARTEFACT_ID = UUID.randomUUID();
    private static final String TEST = "test";
    private static final Artefact ARTEFACT = new Artefact();
    private static final String CASE_NUMBER = "caseNumber";
    private static final String CASE_NAME = "caseName";

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
    private ArtefactSearchRepository artefactSearchRepository;

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
    private final Artefact artefactForScheduledSubscriptionListType = ArtefactConstantTestHelper.buildArtefactFromNow();

    @BeforeEach
    void setup() {
        ARTEFACT.setArtefactId(TEST_ARTEFACT_ID);
        ARTEFACT.setListType(ListType.SJP_PUBLIC_LIST);
        ARTEFACT.setPayloadSize(100F);

        artefactForScheduledSubscriptionListType.setListType(ListType.MAGISTRATES_ADULT_COURT_LIST_DAILY);
    }

    @Test
    void testNotTriggerIfDateIsFuture() {
        try (LogCaptor logCaptor = LogCaptor.forClass(PublicationSubscriptionService.class)) {
            publicationSubscriptionService.checkAndTriggerPublicationSubscription(artefactInTheFuture);
            assertEquals(
                0,
                logCaptor.getInfoLogs().size(),
                "Should not have returned a log as no trigger was sent."
            );
            verifyNoInteractions(accountManagementService);
        }
    }

    @Test
    void testTriggerIfDateIsNow() {
        try (LogCaptor logCaptor = LogCaptor.forClass(PublicationSubscriptionService.class)) {
            publicationSubscriptionService.checkAndTriggerPublicationSubscription(artefactFromNow);
            assertTrue(ERROR_LOG_EMPTY, logCaptor.getErrorLogs().isEmpty());
            verify(accountManagementService).sendArtefactForAllSubscriptions(
                any(uk.gov.hmcts.reform.pip.model.publication.Artefact.class)
            );
        }
    }

    @Test
    void testTriggerIfDateIsPast() {
        try (LogCaptor logCaptor = LogCaptor.forClass(PublicationSubscriptionService.class)) {
            publicationSubscriptionService.checkAndTriggerPublicationSubscription(artefactFromThePast);
            assertTrue(ERROR_LOG_EMPTY, logCaptor.getErrorLogs().isEmpty());
            verify(accountManagementService).sendArtefactForAllSubscriptions(
                any(uk.gov.hmcts.reform.pip.model.publication.Artefact.class)
            );
        }
    }

    @Test
    void testTriggerIfDateToNull() {
        try (LogCaptor logCaptor = LogCaptor.forClass(PublicationSubscriptionService.class)) {
            publicationSubscriptionService.checkAndTriggerPublicationSubscription(artefactWithNullDateTo);
            assertTrue(ERROR_LOG_EMPTY, logCaptor.getErrorLogs().isEmpty());
            verify(accountManagementService).sendArtefactForAllSubscriptions(
                any(uk.gov.hmcts.reform.pip.model.publication.Artefact.class)
            );
        }
    }

    @Test
    void testTriggerIfSameDateFromTo() {
        try (LogCaptor logCaptor = LogCaptor.forClass(PublicationSubscriptionService.class)) {
            publicationSubscriptionService.checkAndTriggerPublicationSubscription(artefactWithSameDateFromAndTo);
            assertTrue(ERROR_LOG_EMPTY, logCaptor.getErrorLogs().isEmpty());
            verify(accountManagementService).sendArtefactForAllSubscriptions(
                any(uk.gov.hmcts.reform.pip.model.publication.Artefact.class)
            );
        }
    }

    @Test
    void testTriggerApiSubscriptionOnlyForScheduledListType() {
        try (LogCaptor logCaptor = LogCaptor.forClass(PublicationSubscriptionService.class)) {
            publicationSubscriptionService.checkAndTriggerPublicationSubscription(
                artefactForScheduledSubscriptionListType
            );
            assertTrue(ERROR_LOG_EMPTY, logCaptor.getErrorLogs().isEmpty());
            verify(accountManagementService).sendArtefactForApiSubscription(
                any(uk.gov.hmcts.reform.pip.model.publication.Artefact.class)
            );
        }
    }

    @Test
    void testCheckNewlyActiveArtefactsLogs() {
        try (LogCaptor logCaptor = LogCaptor.forClass(PublicationSubscriptionService.class)) {
            when(artefactRepository.findArtefactsByDisplayFrom(any(), any())).thenReturn(List.of(ARTEFACT));
            publicationSubscriptionService.checkNewlyActiveArtefacts(false);
            assertTrue(ERROR_LOG_EMPTY, logCaptor.getErrorLogs().isEmpty());
            verify(accountManagementService).sendArtefactForAllSubscriptions(
                any(uk.gov.hmcts.reform.pip.model.publication.Artefact.class)
            );
        }
    }

    @Test
    void testCheckNewlyActiveArtefactsForEmailSubscriptionOnlyForScheduledListType() {
        try (LogCaptor logCaptor = LogCaptor.forClass(PublicationSubscriptionService.class)) {
            when(artefactRepository.findActiveArtefactsByListTypeIn(anySet(), any(), any()))
                .thenReturn(List.of(new Artefact()));
            publicationSubscriptionService.checkNewlyActiveArtefacts(true);
            assertTrue(ERROR_LOG_EMPTY, logCaptor.getErrorLogs().isEmpty());
            verify(accountManagementService).sendArtefactForEmailSubscription(
                any(uk.gov.hmcts.reform.pip.model.publication.Artefact.class)
            );
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

    @Test
    void testConvertArtefactToSHaredModel() {
        Artefact artefact = new Artefact();
        artefact.setArtefactId(TEST_ARTEFACT_ID);
        artefact.setListType(ListType.SJP_PUBLIC_LIST);
        artefact.setContentDate(CONTENT_DATE);
        artefact.setSensitivity(Sensitivity.PRIVATE);
        artefact.setLanguage(Language.ENGLISH);

        ArtefactSearch artefactSearch = new ArtefactSearch();
        artefactSearch.setCaseNumber(CASE_NUMBER);
        artefactSearch.setCaseName(CASE_NAME);

        when(artefactSearchRepository.findByArtefactId(TEST_ARTEFACT_ID)).thenReturn(List.of(artefactSearch));

        uk.gov.hmcts.reform.pip.model.publication.Artefact sharedArtefact = publicationSubscriptionService
            .convertArtefactToSharedModel(artefact);

        assertEquals(TEST_ARTEFACT_ID, sharedArtefact.getArtefactId(), RESPONSE_MESSAGE);
        assertEquals(ListType.SJP_PUBLIC_LIST, sharedArtefact.getListType(), RESPONSE_MESSAGE);
        assertEquals(CONTENT_DATE, sharedArtefact.getContentDate(), RESPONSE_MESSAGE);
        assertEquals(Sensitivity.PRIVATE, sharedArtefact.getSensitivity(), RESPONSE_MESSAGE);
        assertEquals(Language.ENGLISH, sharedArtefact.getLanguage(), RESPONSE_MESSAGE);

        List<ArtefactCaseInfo> caseInfoList = sharedArtefact.getCaseInfoList();
        assertEquals(1, caseInfoList.size());

        ArtefactCaseInfo caseInfo = caseInfoList.get(0);
        assertEquals(CASE_NUMBER, caseInfo.getCaseNumber(), RESPONSE_MESSAGE);
        assertEquals(CASE_NAME, caseInfo.getCaseName(), RESPONSE_MESSAGE);
    }
}
