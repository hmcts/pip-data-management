package uk.gov.hmcts.reform.pip.data.management.service.artefact;

import nl.altindag.log.LogCaptor;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.pip.data.management.database.ArtefactRepository;
import uk.gov.hmcts.reform.pip.data.management.database.LocationRepository;
import uk.gov.hmcts.reform.pip.data.management.helpers.ArtefactConstantTestHelper;
import uk.gov.hmcts.reform.pip.data.management.models.location.Location;
import uk.gov.hmcts.reform.pip.data.management.models.publication.Artefact;
import uk.gov.hmcts.reform.pip.data.management.service.PublicationServicesService;
import uk.gov.hmcts.reform.pip.data.management.service.SubscriptionManagementService;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.pip.data.management.helpers.ArtefactConstantTestHelper.LOCATION_VENUE;
import static uk.gov.hmcts.reform.pip.data.management.helpers.ArtefactConstantTestHelper.PROVENANCE;
import static uk.gov.hmcts.reform.pip.data.management.helpers.ArtefactConstantTestHelper.PROVENANCE_ID;
import static uk.gov.hmcts.reform.pip.data.management.helpers.ArtefactConstantTestHelper.SEARCH_VALUES;
import static uk.gov.hmcts.reform.pip.data.management.helpers.ArtefactConstantTestHelper.SUCCESS;
import static uk.gov.hmcts.reform.pip.data.management.helpers.ArtefactConstantTestHelper.SUCCESSFUL_TRIGGER;
import static uk.gov.hmcts.reform.pip.data.management.helpers.ArtefactConstantTestHelper.TEST_KEY;
import static uk.gov.hmcts.reform.pip.data.management.helpers.ArtefactConstantTestHelper.TEST_VALUE;
import static uk.gov.hmcts.reform.pip.data.management.helpers.ConstantsTestHelper.MESSAGES_MATCH;

@ExtendWith(MockitoExtension.class)
class ArtefactTriggerServiceTest {

    @Mock
    ArtefactRepository artefactRepository;

    @Mock
    LocationRepository locationRepository;

    @Mock
    SubscriptionManagementService subscriptionManagementService;

    @Mock
    PublicationServicesService publicationServicesService;

    @InjectMocks
    ArtefactTriggerService artefactTriggerService;

    private Artefact artefact;
    private Artefact artefactWithPayloadUrl;
    private Artefact artefactWithIdAndPayloadUrl;
    private Artefact artefactInTheFuture;
    private Artefact artefactFromThePast;
    private Artefact artefactFromNow;
    private Artefact artefactWithNullDateTo;
    private Artefact artefactWithSameDateFromAndTo;
    private Artefact noMatchArtefact;

    private Location location;

    @BeforeAll
    public static void setupSearchValues() {
        SEARCH_VALUES.put(TEST_KEY, List.of(TEST_VALUE));
    }

    @BeforeEach
    void setup() {
        createPayloads();
        createClassifiedPayloads();

        location = ArtefactConstantTestHelper.initialiseCourts();

        lenient().when(artefactRepository.findArtefactByUpdateLogic(artefact.getLocationId(),artefact.getContentDate(),
                                                                    artefact.getLanguage().name(),
                                                                    artefact.getListType().name(),
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
        noMatchArtefact = ArtefactConstantTestHelper.buildNoMatchArtefact();
    }

    private void createClassifiedPayloads() {

        location = ArtefactConstantTestHelper.initialiseCourts();

        lenient().when(artefactRepository.findArtefactByUpdateLogic(artefact.getLocationId(),artefact.getContentDate(),
                                                                    artefact.getLanguage().name(),
                                                                    artefact.getListType().name(),
                                                                    artefact.getProvenance()))
            .thenReturn(Optional.empty());
        lenient().when(artefactRepository.save(artefactWithPayloadUrl)).thenReturn(artefactWithIdAndPayloadUrl);
        lenient().when(locationRepository.findByLocationIdByProvenance(PROVENANCE, PROVENANCE_ID,
                                                                       LOCATION_VENUE))
            .thenReturn(Optional.of(location));
    }

    @Test
    void testTriggerIfDateIsFuture() throws IOException {
        try (LogCaptor logCaptor = LogCaptor.forClass(ArtefactTriggerService.class)) {
            artefactTriggerService.checkAndTriggerSubscriptionManagement(artefactInTheFuture);
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
        when(subscriptionManagementService.sendArtefactForSubscription(artefactFromNow)).thenReturn(SUCCESSFUL_TRIGGER);
        try (LogCaptor logCaptor = LogCaptor.forClass(ArtefactTriggerService.class)) {
            artefactTriggerService.checkAndTriggerSubscriptionManagement(artefactFromNow);
            assertEquals(SUCCESSFUL_TRIGGER, logCaptor.getInfoLogs().get(0),
                         "should have returned the Subscription List."
            );
        }
    }

    @Test
    void testTriggerIfDateIsPast() {
        when(subscriptionManagementService.sendArtefactForSubscription(artefactFromThePast)).thenReturn(
            SUCCESSFUL_TRIGGER);
        try (LogCaptor logCaptor = LogCaptor.forClass(ArtefactTriggerService.class)) {
            artefactTriggerService.checkAndTriggerSubscriptionManagement(artefactFromThePast);
            assertEquals(SUCCESSFUL_TRIGGER, logCaptor.getInfoLogs().get(0),
                         "Should have returned the subscription list"
            );
        }
    }

    @Test
    void testTriggerIfDateToNull() {
        when(subscriptionManagementService.sendArtefactForSubscription(artefactWithNullDateTo)).thenReturn(
            SUCCESSFUL_TRIGGER);
        try (LogCaptor logCaptor = LogCaptor.forClass(ArtefactTriggerService.class)) {
            artefactTriggerService.checkAndTriggerSubscriptionManagement(artefactWithNullDateTo);
            assertEquals(SUCCESSFUL_TRIGGER, logCaptor.getInfoLogs().get(0),
                         "Should have returned the subscription list"
            );
        }
    }

    @Test
    void testTriggerIfSameDateFromTo() {
        when(subscriptionManagementService.sendArtefactForSubscription(artefactWithSameDateFromAndTo)).thenReturn(
            SUCCESSFUL_TRIGGER);
        try (LogCaptor logCaptor = LogCaptor.forClass(ArtefactTriggerService.class)) {
            artefactTriggerService.checkAndTriggerSubscriptionManagement(artefactWithSameDateFromAndTo);
            assertEquals(SUCCESSFUL_TRIGGER, logCaptor.getInfoLogs().get(0),
                         "Should have returned the subscription list"
            );
        }
    }

    @Test
    void testSendArtefactForSubscription() {
        when(subscriptionManagementService.sendArtefactForSubscription(artefact))
            .thenReturn(SUCCESS);
        assertEquals(SUCCESS, artefactTriggerService.sendArtefactForSubscription(artefact),
                     MESSAGES_MATCH);
    }

    @Test
    void testCheckNewlyActiveArtefactsLogs() throws IOException {
        try (LogCaptor logCaptor = LogCaptor.forClass(ArtefactTriggerService.class)) {
            when(artefactRepository.findArtefactsByDisplayFrom(any())).thenReturn(List.of(new Artefact()));
            when(subscriptionManagementService.sendArtefactForSubscription(any())).thenReturn(SUCCESS);
            artefactTriggerService.checkNewlyActiveArtefacts();
            assertEquals(SUCCESS, logCaptor.getInfoLogs().get(0),
                         MESSAGES_MATCH
            );
        } catch (Exception ex) {
            throw new IOException(ex.getMessage());
        }
    }

    @Test
    void testReportNoMatchArtefacts() {
        when(artefactRepository.findAllNoMatchArtefacts()).thenReturn(List.of(noMatchArtefact));
        artefactTriggerService.reportNoMatchArtefacts();
        verify(publicationServicesService).sendNoMatchArtefactsForReporting(Map.of(PROVENANCE_ID, PROVENANCE));
    }

    @Test
    void testReportMatchArtefactsWhenArtefactsNotFound() {
        when(artefactRepository.findAllNoMatchArtefacts()).thenReturn(Collections.emptyList());
        artefactTriggerService.reportNoMatchArtefacts();
        verifyNoInteractions(publicationServicesService);
    }
}
