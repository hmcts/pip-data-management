package uk.gov.hmcts.reform.pip.data.management.service.publication;

import nl.altindag.log.LogCaptor;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.pip.data.management.database.ArtefactRepository;
import uk.gov.hmcts.reform.pip.data.management.database.LocationRepository;
import uk.gov.hmcts.reform.pip.data.management.helpers.ArtefactConstantTestHelper;
import uk.gov.hmcts.reform.pip.data.management.models.location.Location;
import uk.gov.hmcts.reform.pip.data.management.models.publication.Artefact;
import uk.gov.hmcts.reform.pip.data.management.service.AccountManagementService;
import uk.gov.hmcts.reform.pip.data.management.service.PublicationServicesService;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.pip.data.management.helpers.ArtefactConstantTestHelper.LOCATION_VENUE;
import static uk.gov.hmcts.reform.pip.data.management.helpers.ArtefactConstantTestHelper.PROVENANCE;
import static uk.gov.hmcts.reform.pip.data.management.helpers.ArtefactConstantTestHelper.PROVENANCE_ID;
import static uk.gov.hmcts.reform.pip.data.management.helpers.ArtefactConstantTestHelper.SEARCH_VALUES;
import static uk.gov.hmcts.reform.pip.data.management.helpers.ArtefactConstantTestHelper.SUCCESS;
import static uk.gov.hmcts.reform.pip.data.management.helpers.ArtefactConstantTestHelper.SUCCESSFUL_TRIGGER;
import static uk.gov.hmcts.reform.pip.data.management.helpers.ArtefactConstantTestHelper.TEST_KEY;
import static uk.gov.hmcts.reform.pip.data.management.helpers.ArtefactConstantTestHelper.TEST_VALUE;

@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
public class PublicationSubscriptionServiceTest {
    private static final String ERROR_LOG_EMPTY = "Error log not empty";

    @Mock
    ArtefactRepository artefactRepository;

    @Mock
    LocationRepository locationRepository;

    @Mock
    AccountManagementService accountManagementService;

    @InjectMocks
    PublicationSubscriptionService publicationSubscriptionService;

    private Artefact artefact;
    private Artefact artefactWithPayloadUrl;
    private Artefact artefactWithIdAndPayloadUrl;
    private Artefact artefactInTheFuture;
    private Artefact artefactFromThePast;
    private Artefact artefactFromNow;
    private Artefact artefactWithNullDateTo;
    private Artefact artefactWithSameDateFromAndTo;

    @BeforeAll
    public static void setupSearchValues() {
        SEARCH_VALUES.put(TEST_KEY, List.of(TEST_VALUE));
    }

    @BeforeEach
    void setup() {
        createPayloads();
        createClassifiedPayloads();

        Location location = ArtefactConstantTestHelper.initialiseCourts();

        lenient().when(artefactRepository.findArtefactByUpdateLogic(artefact.getLocationId(),
                                                                    artefact.getContentDate(),
                                                                    artefact.getLanguage(),
                                                                    artefact.getListType(),
                                                                    artefact.getProvenance()))
            .thenReturn(Optional.empty());
        lenient().when(artefactRepository.save(artefactWithPayloadUrl)).thenReturn(artefactWithIdAndPayloadUrl);
        lenient().when(locationRepository.findByLocationIdByProvenance(PROVENANCE, PROVENANCE_ID,
                                                                       LOCATION_VENUE))
            .thenReturn(Optional.of(location));
    }

    private void createPayloads() {
        artefact = ArtefactConstantTestHelper.buildArtefact();
        artefactWithPayloadUrl = ArtefactConstantTestHelper.buildArtefactWithPayloadUrl();
        artefactWithIdAndPayloadUrl = ArtefactConstantTestHelper.buildArtefactWithIdAndPayloadUrl();

        artefactFromThePast = ArtefactConstantTestHelper.buildArtefactFromThePast();
        artefactFromNow = ArtefactConstantTestHelper.buildArtefactFromNow();
        artefactWithNullDateTo = ArtefactConstantTestHelper.buildArtefactWithNullDateTo();
        artefactWithSameDateFromAndTo = ArtefactConstantTestHelper.buildArtefactWithSameDateFromAndTo();
        artefactInTheFuture = ArtefactConstantTestHelper.buildArtefactInTheFuture();
    }

    private void createClassifiedPayloads() {

        Location location = ArtefactConstantTestHelper.initialiseCourts();

        lenient().when(artefactRepository.findArtefactByUpdateLogic(artefact.getLocationId(),
                                                                    artefact.getContentDate(),
                                                                    artefact.getLanguage(),
                                                                    artefact.getListType(),
                                                                    artefact.getProvenance()))
            .thenReturn(Optional.empty());
        lenient().when(artefactRepository.save(artefactWithPayloadUrl)).thenReturn(artefactWithIdAndPayloadUrl);
        lenient().when(locationRepository.findByLocationIdByProvenance(PROVENANCE, PROVENANCE_ID,
                                                                       LOCATION_VENUE))
            .thenReturn(Optional.of(location));
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
            when(artefactRepository.findArtefactsByDisplayFrom(any())).thenReturn(List.of(new Artefact()));
            when(accountManagementService.sendArtefactForSubscription(any())).thenReturn(SUCCESS);
            publicationSubscriptionService.checkNewlyActiveArtefacts();
            assertTrue(ERROR_LOG_EMPTY, logCaptor.getErrorLogs().isEmpty());
        } catch (Exception ex) {
            throw new IOException(ex.getMessage());
        }
    }
}
