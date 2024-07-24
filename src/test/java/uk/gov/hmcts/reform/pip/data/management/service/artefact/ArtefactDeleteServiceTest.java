package uk.gov.hmcts.reform.pip.data.management.service.artefact;

import com.fasterxml.jackson.core.JsonProcessingException;
import nl.altindag.log.LogCaptor;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.pip.data.management.database.ArtefactRepository;
import uk.gov.hmcts.reform.pip.data.management.database.AzureBlobService;
import uk.gov.hmcts.reform.pip.data.management.database.LocationRepository;
import uk.gov.hmcts.reform.pip.data.management.errorhandling.exceptions.ArtefactNotFoundException;
import uk.gov.hmcts.reform.pip.data.management.errorhandling.exceptions.NotFoundException;
import uk.gov.hmcts.reform.pip.data.management.helpers.ArtefactConstantTestHelper;
import uk.gov.hmcts.reform.pip.data.management.models.location.Location;
import uk.gov.hmcts.reform.pip.data.management.models.publication.Artefact;
import uk.gov.hmcts.reform.pip.data.management.service.AccountManagementService;
import uk.gov.hmcts.reform.pip.data.management.service.ChannelManagementService;
import uk.gov.hmcts.reform.pip.data.management.service.LocationService;
import uk.gov.hmcts.reform.pip.data.management.service.PublicationServicesService;
import uk.gov.hmcts.reform.pip.data.management.service.SubscriptionManagementService;
import uk.gov.hmcts.reform.pip.model.account.AzureAccount;
import uk.gov.hmcts.reform.pip.model.publication.Language;
import uk.gov.hmcts.reform.pip.model.publication.ListType;
import uk.gov.hmcts.reform.pip.model.system.admin.ActionResult;
import uk.gov.hmcts.reform.pip.model.system.admin.ChangeType;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.pip.data.management.helpers.ArtefactConstantTestHelper.ARTEFACT_ID;
import static uk.gov.hmcts.reform.pip.data.management.helpers.ArtefactConstantTestHelper.DELETION_TRACK_LOG_MESSAGE;
import static uk.gov.hmcts.reform.pip.data.management.helpers.ArtefactConstantTestHelper.LOCATION_VENUE;
import static uk.gov.hmcts.reform.pip.data.management.helpers.ArtefactConstantTestHelper.PAYLOAD_STRIPPED;
import static uk.gov.hmcts.reform.pip.data.management.helpers.ArtefactConstantTestHelper.PROVENANCE;
import static uk.gov.hmcts.reform.pip.data.management.helpers.ArtefactConstantTestHelper.PROVENANCE_ID;
import static uk.gov.hmcts.reform.pip.data.management.helpers.ArtefactConstantTestHelper.SEARCH_VALUES;
import static uk.gov.hmcts.reform.pip.data.management.helpers.ArtefactConstantTestHelper.TEST_KEY;
import static uk.gov.hmcts.reform.pip.data.management.helpers.ArtefactConstantTestHelper.TEST_VALUE;
import static uk.gov.hmcts.reform.pip.data.management.helpers.ConstantsTestHelper.MESSAGES_MATCH;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings({"PMD.ExcessiveImports", "PMD.TooManyFields"})
class ArtefactDeleteServiceTest {

    @Mock
    ArtefactRepository artefactRepository;

    @Mock
    LocationRepository locationRepository;

    @Mock
    LocationService locationService;

    @Mock
    AzureBlobService azureBlobService;

    @Mock
    SubscriptionManagementService subscriptionManagementService;

    @Mock
    AccountManagementService accountManagementService;

    @Mock
    PublicationServicesService publicationService;

    @Mock
    ChannelManagementService channelManagementService;

    @Mock
    ArtefactService artefactService;

    @InjectMocks
    ArtefactDeleteService artefactDeleteService;

    private Artefact artefact;
    private Artefact artefactWithPayloadUrl;
    private Artefact artefactWithIdAndPayloadUrl;
    private Artefact artefactWithNoMatchLocationId;

    private Location location;
    AzureAccount azureAccount;

    private static final String REQUESTER_NAME = "ReqName";
    private static final String EMAIL_ADDRESS = "test@test.com";
    private static final String SSO_EMAIL = "sso@test.com";

    private static final Integer LOCATION_ID = 1;
    private static final String LOCATION_NAME_PREFIX = "TEST_PIP_1234_";

    @BeforeAll
    public static void setupSearchValues() {
        SEARCH_VALUES.put(TEST_KEY, List.of(TEST_VALUE));
    }

    @BeforeEach
    void setup() {
        createPayloads();
        createClassifiedPayloads();

        location = ArtefactConstantTestHelper.initialiseCourts();

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
        lenient().when(artefactService.payloadWithinLimit(any())).thenReturn(true);

        azureAccount = new AzureAccount();
        azureAccount.setDisplayName(REQUESTER_NAME);
    }

    private void createPayloads() {
        artefact = ArtefactConstantTestHelper.buildArtefact();
        artefactWithPayloadUrl = ArtefactConstantTestHelper.buildArtefactWithPayloadUrl();
        artefactWithIdAndPayloadUrl = ArtefactConstantTestHelper.buildArtefactWithIdAndPayloadUrl();
        artefactWithNoMatchLocationId = ArtefactConstantTestHelper.buildNoMatchArtefactWithIdAndPayloadUrl();
    }

    private void createClassifiedPayloads() {

        location = ArtefactConstantTestHelper.initialiseCourts();

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
    void testDeleteArtefactById() {
        try (LogCaptor logCaptor = LogCaptor.forClass(ArtefactDeleteService.class)) {
            when(artefactRepository.findArtefactByArtefactId(ARTEFACT_ID.toString()))
                .thenReturn(Optional.of(artefactWithIdAndPayloadUrl));

            artefactDeleteService.deleteArtefactById(ARTEFACT_ID.toString(), TEST_VALUE);
            assertTrue(logCaptor.getInfoLogs().get(0).contains(String.format(DELETION_TRACK_LOG_MESSAGE, ARTEFACT_ID)),
                       MESSAGES_MATCH);

            InOrder orderVerifier = inOrder(azureBlobService, channelManagementService,
                                            artefactRepository, subscriptionManagementService);
            orderVerifier.verify(azureBlobService).deleteBlob(PAYLOAD_STRIPPED);
            orderVerifier.verify(channelManagementService).deleteFiles(ARTEFACT_ID, ListType.CIVIL_DAILY_CAUSE_LIST,
                                                                       Language.ENGLISH);
            orderVerifier.verify(artefactRepository).delete(artefactWithIdAndPayloadUrl);
            orderVerifier.verify(subscriptionManagementService)
                .sendDeletedArtefactForThirdParties(artefactWithIdAndPayloadUrl);
        }
    }

    @Test
    void testDeleteArtefactByIdWithNoMatchLocationId() {
        try (LogCaptor logCaptor = LogCaptor.forClass(ArtefactDeleteService.class)) {
            when(artefactRepository.findArtefactByArtefactId(ARTEFACT_ID.toString()))
                .thenReturn(Optional.of(artefactWithNoMatchLocationId));

            artefactDeleteService.deleteArtefactById(ARTEFACT_ID.toString(), TEST_VALUE);
            assertTrue(logCaptor.getInfoLogs().get(0).contains(String.format(DELETION_TRACK_LOG_MESSAGE, ARTEFACT_ID)),
                       MESSAGES_MATCH);

            verify(artefactRepository).delete(artefactWithNoMatchLocationId);
            verify(azureBlobService).deleteBlob(PAYLOAD_STRIPPED);
            verifyNoInteractions(channelManagementService);
            verifyNoInteractions(subscriptionManagementService);
        }
    }

    @Test
    void testDeleteArtefactByIdWithPayloadSizeOverLimit() {
        try (LogCaptor logCaptor = LogCaptor.forClass(ArtefactDeleteService.class)) {
            when(artefactRepository.findArtefactByArtefactId(ARTEFACT_ID.toString()))
                .thenReturn(Optional.of(artefactWithIdAndPayloadUrl));
            when(artefactService.payloadWithinLimit(any())).thenReturn(false);

            artefactDeleteService.deleteArtefactById(ARTEFACT_ID.toString(), TEST_VALUE);
            assertTrue(logCaptor.getInfoLogs().get(0).contains(String.format(DELETION_TRACK_LOG_MESSAGE, ARTEFACT_ID)),
                       MESSAGES_MATCH);

            verify(artefactRepository).delete(artefactWithIdAndPayloadUrl);
            verify(azureBlobService).deleteBlob(PAYLOAD_STRIPPED);
            verifyNoInteractions(channelManagementService);
            verify(subscriptionManagementService).sendDeletedArtefactForThirdParties(artefactWithIdAndPayloadUrl);
        }
    }

    @Test
    void testDeleteArtefactByIdFlatFile() {
        try (LogCaptor logCaptor = LogCaptor.forClass(ArtefactDeleteService.class)) {
            artefactWithIdAndPayloadUrl.setIsFlatFile(true);
            when(artefactRepository.findArtefactByArtefactId(ARTEFACT_ID.toString()))
                .thenReturn(Optional.of(artefactWithIdAndPayloadUrl));

            artefactDeleteService.deleteArtefactById(ARTEFACT_ID.toString(), TEST_VALUE);
            assertTrue(logCaptor.getInfoLogs().get(0).contains(String.format(DELETION_TRACK_LOG_MESSAGE, ARTEFACT_ID)),
                       MESSAGES_MATCH);

            InOrder orderVerifier = inOrder(azureBlobService, channelManagementService,
                                            artefactRepository, subscriptionManagementService);
            orderVerifier.verify(azureBlobService).deleteBlob(PAYLOAD_STRIPPED);
            orderVerifier.verify(artefactRepository).delete(artefactWithIdAndPayloadUrl);
            orderVerifier.verify(subscriptionManagementService)
                .sendDeletedArtefactForThirdParties(artefactWithIdAndPayloadUrl);
            verifyNoInteractions(channelManagementService);
        }
    }

    @Test
    void testDeleteArtefactByIdFlatFileWithNoMatchLocationId() {
        try (LogCaptor logCaptor = LogCaptor.forClass(ArtefactDeleteService.class)) {
            artefactWithNoMatchLocationId.setIsFlatFile(true);
            when(artefactRepository.findArtefactByArtefactId(ARTEFACT_ID.toString()))
                .thenReturn(Optional.of(artefactWithNoMatchLocationId));

            artefactDeleteService.deleteArtefactById(ARTEFACT_ID.toString(), TEST_VALUE);
            assertTrue(logCaptor.getInfoLogs().get(0).contains(String.format(DELETION_TRACK_LOG_MESSAGE, ARTEFACT_ID)),
                       MESSAGES_MATCH);

            verify(artefactRepository).delete(artefactWithNoMatchLocationId);
            verify(azureBlobService).deleteBlob(PAYLOAD_STRIPPED);
            verifyNoInteractions(channelManagementService);
            verifyNoInteractions(subscriptionManagementService);
        }
    }

    @Test
    void testDeleteArtefactByIdThrows() {
        ArtefactNotFoundException ex = assertThrows(ArtefactNotFoundException.class, () ->
            artefactDeleteService.deleteArtefactById(TEST_VALUE, TEST_VALUE),
            "ArtefactNotFoundException should be thrown");

        assertEquals("No artefact found with the ID: " + TEST_VALUE, ex.getMessage(),
                     MESSAGES_MATCH);
    }

    @Test
    void testArchiveExpiredArtefacts() {
        when(artefactRepository.findOutdatedArtefacts(any())).thenReturn(List.of(artefactWithIdAndPayloadUrl));
        artefactDeleteService.archiveExpiredArtefacts();

        verify(artefactRepository).archiveArtefact(ARTEFACT_ID.toString());
        verify(azureBlobService).deleteBlob(PAYLOAD_STRIPPED);
        verify(channelManagementService).deleteFiles(ARTEFACT_ID, ListType.CIVIL_DAILY_CAUSE_LIST, Language.ENGLISH);
        verifyNoInteractions(subscriptionManagementService);
    }

    @Test
    void testArchiveExpiredArtefactsWithNoMatchLocationId() {
        when(artefactRepository.findOutdatedArtefacts(any())).thenReturn(List.of(artefactWithNoMatchLocationId));
        artefactDeleteService.archiveExpiredArtefacts();

        verify(artefactRepository).archiveArtefact(ARTEFACT_ID.toString());
        verify(azureBlobService).deleteBlob(PAYLOAD_STRIPPED);
        verifyNoInteractions(channelManagementService);
        verifyNoInteractions(subscriptionManagementService);
    }

    @Test
    void testArchiveExpiredArtefactsWithPayloadSizeOverLimit() {
        when(artefactRepository.findOutdatedArtefacts(any())).thenReturn(List.of(artefactWithIdAndPayloadUrl));
        when(artefactService.payloadWithinLimit(any())).thenReturn(false);

        artefactDeleteService.archiveExpiredArtefacts();

        verify(artefactRepository).archiveArtefact(ARTEFACT_ID.toString());
        verify(azureBlobService).deleteBlob(PAYLOAD_STRIPPED);
        verifyNoInteractions(channelManagementService);
        verifyNoInteractions(subscriptionManagementService);
    }


    @Test
    void testArchiveExpiredArtefactsSjpPublic() {
        UUID testArtefactId = UUID.randomUUID();
        artefactWithPayloadUrl.setArtefactId(testArtefactId);
        artefactWithPayloadUrl.setListType(ListType.SJP_PUBLIC_LIST);
        when(artefactRepository.findOutdatedArtefacts(any())).thenReturn(List.of(artefactWithPayloadUrl));

        artefactDeleteService.archiveExpiredArtefacts();
        verify(artefactRepository).archiveArtefact(testArtefactId.toString());
        verify(azureBlobService).deleteBlob(PAYLOAD_STRIPPED);
        verify(channelManagementService).deleteFiles(testArtefactId, ListType.SJP_PUBLIC_LIST, Language.ENGLISH);
        verifyNoInteractions(subscriptionManagementService);
    }

    @Test
    void testArchiveExpiredArtefactsSjpPress() {
        UUID testArtefactId = UUID.randomUUID();
        artefactWithPayloadUrl.setArtefactId(testArtefactId);
        artefactWithPayloadUrl.setListType(ListType.SJP_PRESS_LIST);
        when(artefactRepository.findOutdatedArtefacts(any())).thenReturn(List.of(artefactWithPayloadUrl));

        artefactDeleteService.archiveExpiredArtefacts();
        verify(artefactRepository).archiveArtefact(testArtefactId.toString());
        verify(azureBlobService).deleteBlob(PAYLOAD_STRIPPED);
        verify(channelManagementService).deleteFiles(testArtefactId, ListType.SJP_PRESS_LIST, Language.ENGLISH);
        verifyNoInteractions(subscriptionManagementService);
    }

    @Test
    void testArchiveExpiredArtefactsFlatFile() {
        UUID testArtefactId = UUID.randomUUID();
        artefactWithPayloadUrl.setArtefactId(testArtefactId);
        artefactWithPayloadUrl.setListType(ListType.SJP_PRESS_LIST);
        artefactWithPayloadUrl.setIsFlatFile(true);

        when(artefactRepository.findOutdatedArtefacts(any())).thenReturn(List.of(artefactWithPayloadUrl));
        artefactDeleteService.archiveExpiredArtefacts();

        verify(artefactRepository).archiveArtefact(testArtefactId.toString());
        verify(azureBlobService).deleteBlob(PAYLOAD_STRIPPED);
        verifyNoInteractions(channelManagementService);
        verifyNoInteractions(subscriptionManagementService);
    }

    @Test
    void testArchiveExpiredArtefactsFlatFileWithNoMatchLocationId() {
        artefactWithNoMatchLocationId.setIsFlatFile(true);
        when(artefactRepository.findOutdatedArtefacts(any())).thenReturn(List.of(artefactWithNoMatchLocationId));
        artefactDeleteService.archiveExpiredArtefacts();

        verify(artefactRepository).archiveArtefact(ARTEFACT_ID.toString());
        verify(azureBlobService).deleteBlob(PAYLOAD_STRIPPED);
        verifyNoInteractions(channelManagementService);
        verifyNoInteractions(subscriptionManagementService);
    }

    @Test
    void testArchiveExpiredArtefactsWhenArtefactsNotFound() {
        when(artefactRepository.findOutdatedArtefacts(any())).thenReturn(Collections.emptyList());
        artefactDeleteService.archiveExpiredArtefacts();
        verifyNoInteractions(azureBlobService);
        verifyNoInteractions(subscriptionManagementService);
    }

    @Test
    void testArchiveArtefactById() {
        when(artefactRepository.findArtefactByArtefactId(ARTEFACT_ID.toString()))
            .thenReturn(Optional.of(artefactWithIdAndPayloadUrl));

        artefactDeleteService.archiveArtefactById(ARTEFACT_ID.toString(), UUID.randomUUID().toString());

        verify(artefactRepository).archiveArtefact(ARTEFACT_ID.toString());
        verify(azureBlobService).deleteBlob(PAYLOAD_STRIPPED);
        verify(channelManagementService).deleteFiles(ARTEFACT_ID, ListType.CIVIL_DAILY_CAUSE_LIST, Language.ENGLISH);
        verify(subscriptionManagementService).sendDeletedArtefactForThirdParties(artefactWithIdAndPayloadUrl);
    }

    @Test
    void testArchiveArtefactByIdWithNoMatchLocationId() {
        when(artefactRepository.findArtefactByArtefactId(ARTEFACT_ID.toString()))
            .thenReturn(Optional.of(artefactWithNoMatchLocationId));

        artefactDeleteService.archiveArtefactById(ARTEFACT_ID.toString(), UUID.randomUUID().toString());

        verify(artefactRepository).archiveArtefact(ARTEFACT_ID.toString());
        verify(azureBlobService).deleteBlob(PAYLOAD_STRIPPED);
        verifyNoInteractions(channelManagementService);
        verifyNoInteractions(subscriptionManagementService);
    }

    @Test
    void testArchiveArtefactByIdWithPayloadSizeOverLimit() {
        when(artefactRepository.findArtefactByArtefactId(ARTEFACT_ID.toString()))
            .thenReturn(Optional.of(artefactWithIdAndPayloadUrl));
        when(artefactService.payloadWithinLimit(any())).thenReturn(false);

        artefactDeleteService.archiveArtefactById(ARTEFACT_ID.toString(), UUID.randomUUID().toString());

        verify(artefactRepository).archiveArtefact(ARTEFACT_ID.toString());
        verify(azureBlobService).deleteBlob(PAYLOAD_STRIPPED);
        verifyNoInteractions(channelManagementService);
        verify(subscriptionManagementService).sendDeletedArtefactForThirdParties(artefactWithIdAndPayloadUrl);

    }

    @Test
    void testArchiveArtefactByIdNotFound() {
        String artefactId = UUID.randomUUID().toString();
        when(artefactRepository.findArtefactByArtefactId(artefactId)).thenReturn(Optional.empty());

        String randomUuID = UUID.randomUUID().toString();
        assertThrows(NotFoundException.class, () -> {
            artefactDeleteService.archiveArtefactById(artefactId, randomUuID);
        }, "Attempting to archive an artefact that does not exist should throw an exception");
    }

    @Test
    void testDeleteArtefactByLocation() throws JsonProcessingException {
        location.setName("NAME");

        try (LogCaptor logCaptor = LogCaptor.forClass(ArtefactDeleteService.class)) {
            when(artefactRepository.findActiveArtefactsForLocation(any(), eq(LOCATION_ID.toString())))
                .thenReturn(List.of(artefactWithIdAndPayloadUrl));
            when(locationRepository.getLocationByLocationId(LOCATION_ID))
                .thenReturn(Optional.of(location));
            when(accountManagementService.getUserInfo(any()))
                .thenReturn(azureAccount);
            when(accountManagementService.getAllAccounts("PI_AAD", "SYSTEM_ADMIN"))
                .thenReturn(List.of(EMAIL_ADDRESS));
            when(accountManagementService.getAllAccounts("SSO", "SYSTEM_ADMIN"))
                .thenReturn(List.of(SSO_EMAIL));

            List<String> systemAdminEmails = List.of(EMAIL_ADDRESS, SSO_EMAIL);

            when(publicationService.sendSystemAdminEmail(systemAdminEmails, REQUESTER_NAME, ActionResult.SUCCEEDED,
                                                         "Total 1 artefact(s) for location NAME",
                                                         ChangeType.DELETE_LOCATION_ARTEFACT))
                .thenReturn("System admin message");

            assertEquals("Total 1 artefact deleted for location id 1",
                         artefactDeleteService.deleteArtefactByLocation(LOCATION_ID, REQUESTER_NAME),
                         "The artefacts for given location is not deleted");

            InOrder orderVerifier = inOrder(azureBlobService, channelManagementService,
                                            artefactRepository, subscriptionManagementService);
            orderVerifier.verify(azureBlobService).deleteBlob(any());
            orderVerifier.verify(channelManagementService).deleteFiles(ARTEFACT_ID, ListType.CIVIL_DAILY_CAUSE_LIST,
                                                         Language.ENGLISH);
            orderVerifier.verify(artefactRepository).delete(artefactWithIdAndPayloadUrl);
            orderVerifier.verify(subscriptionManagementService).sendDeletedArtefactForThirdParties(any());

            assertTrue(logCaptor.getInfoLogs().get(0).contains("User " + REQUESTER_NAME
                                                                   + " attempting to delete all artefacts for location "
                                                                   + LOCATION_ID + ". 1 artefact(s) found"),
                       "Expected log does not exist");
        }
    }

    @Test
    void testDeleteArtefactByLocationWithPayloadSizeOverLimit() throws JsonProcessingException {
        location.setName("NAME");
        try (LogCaptor logCaptor = LogCaptor.forClass(ArtefactDeleteService.class)) {
            when(artefactRepository.findActiveArtefactsForLocation(any(), eq(LOCATION_ID.toString())))
                .thenReturn(List.of(artefactWithIdAndPayloadUrl));
            when(locationRepository.getLocationByLocationId(LOCATION_ID))
                .thenReturn(Optional.of(location));
            when(accountManagementService.getUserInfo(any()))
                .thenReturn(azureAccount);
            when(accountManagementService.getAllAccounts("PI_AAD", "SYSTEM_ADMIN"))
                .thenReturn(List.of(EMAIL_ADDRESS));
            when(publicationService.sendSystemAdminEmail(List.of(EMAIL_ADDRESS), REQUESTER_NAME, ActionResult.SUCCEEDED,
                                                         "Total 1 artefact(s) for location NAME",
                                                         ChangeType.DELETE_LOCATION_ARTEFACT))
                .thenReturn("System admin message");
            when(artefactService.payloadWithinLimit(any())).thenReturn(false);

            assertEquals("Total 1 artefact deleted for location id 1",
                         artefactDeleteService.deleteArtefactByLocation(LOCATION_ID, REQUESTER_NAME),
                         "The artefacts for given location is not deleted");

            verify(azureBlobService).deleteBlob(any());
            verifyNoInteractions(channelManagementService);
            verify(artefactRepository).delete(artefactWithIdAndPayloadUrl);
            verify(subscriptionManagementService).sendDeletedArtefactForThirdParties(any());

            assertTrue(logCaptor.getInfoLogs().get(0).contains("User " + REQUESTER_NAME
                                                                   + " attempting to delete all artefacts for location "
                                                                   + LOCATION_ID + ". 1 artefact(s) found"),
                       "Expected log does not exist");
        }
    }

    @Test
    void testDeleteArtefactByLocationWhenNoArtefactFound() {

        try (LogCaptor logCaptor = LogCaptor.forClass(ArtefactDeleteService.class)) {
            when(artefactRepository.findActiveArtefactsForLocation(any(), eq(LOCATION_ID.toString())))
                .thenReturn(List.of());
            assertThrows(ArtefactNotFoundException.class, () ->
                             artefactDeleteService.deleteArtefactByLocation(LOCATION_ID, REQUESTER_NAME),
                         "ArtefactNotFoundException not thrown when trying to delete a artefact"
                             + " that does not exist"
            );

            assertTrue(logCaptor.getInfoLogs().get(0).contains("User " + REQUESTER_NAME
                                                                   + " attempting to delete all artefacts for location "
                                                                   + LOCATION_ID + ". No artefacts found"),
                       "Expected log does not exist");
        }
    }

    @Test
    void testDeleteArtefactByLocationJsonProcessingException() throws JsonProcessingException {

        when(artefactRepository.findActiveArtefactsForLocation(any(), eq(LOCATION_ID.toString())))
            .thenReturn(List.of(artefactWithIdAndPayloadUrl));
        when(locationRepository.getLocationByLocationId(LOCATION_ID))
            .thenReturn(Optional.of(location));
        when(accountManagementService.getUserInfo(any()))
            .thenReturn(azureAccount);
        when(accountManagementService.getAllAccounts(any(), any()))
            .thenThrow(JsonProcessingException.class);

        assertThrows(JsonProcessingException.class, () ->
                         artefactDeleteService.deleteArtefactByLocation(LOCATION_ID, REQUESTER_NAME),
                     "JsonProcessingException not thrown when trying to get errored system admin"
                         + " api response");
    }

    @Test
    void testDeleteAllArtefactsWithLocationNamePrefix() {
        Artefact artefact1 = new Artefact();
        Integer locationId1 = 1;
        UUID artefactId1 = UUID.randomUUID();
        String payload1 = "payload/url1";
        artefact1.setArtefactId(artefactId1);
        artefact1.setLocationId(locationId1.toString());
        artefact1.setPayload(payload1);

        Artefact artefact2 = new Artefact();
        UUID artefactId2 = UUID.randomUUID();
        String payload2 = "payload/url2";
        artefact2.setArtefactId(artefactId2);
        artefact2.setLocationId(locationId1.toString());
        artefact2.setPayload(payload2);

        Artefact artefact3 = new Artefact();
        Integer locationId2 = 2;
        UUID artefactId3 = UUID.randomUUID();
        String payload3 = "payload/url3";
        artefact3.setArtefactId(artefactId3);
        artefact3.setLocationId(locationId2.toString());
        artefact3.setPayload(payload3);

        when(locationService.getAllLocationsWithNamePrefix(LOCATION_NAME_PREFIX))
            .thenReturn(List.of(locationId1, locationId2));

        when(artefactRepository.findAllByLocationIdIn(List.of(locationId1.toString(), locationId2.toString())))
            .thenReturn(List.of(artefact1, artefact2, artefact3));

        assertThat(artefactDeleteService.deleteAllArtefactsWithLocationNamePrefix(LOCATION_NAME_PREFIX))
            .isEqualTo("3 artefacts(s) deleted for location name starting with " + LOCATION_NAME_PREFIX);

        verify(azureBlobService).deleteBlob("url1");
        verify(azureBlobService).deleteBlob("url2");
        verify(azureBlobService).deleteBlob("url3");
        verify(artefactRepository, times(3)).delete(any());
        verify(subscriptionManagementService, times(3)).sendDeletedArtefactForThirdParties(any());
    }

    @Test
    void testDeleteAllArtefactsWithLocationNamePrefixWhenArtefactNotFound() {
        Integer locationId = 1;

        when(locationService.getAllLocationsWithNamePrefix(LOCATION_NAME_PREFIX))
            .thenReturn(List.of(locationId));

        when(artefactRepository.findAllByLocationIdIn(List.of(locationId.toString())))
            .thenReturn(Collections.emptyList());

        assertThat(artefactDeleteService.deleteAllArtefactsWithLocationNamePrefix(LOCATION_NAME_PREFIX))
            .isEqualTo("0 artefacts(s) deleted for location name starting with " + LOCATION_NAME_PREFIX);

        verifyNoInteractions(azureBlobService);
        verifyNoMoreInteractions(artefactRepository);
        verifyNoInteractions(subscriptionManagementService);
    }

    @Test
    void testDeleteAllArtefactsWithLocationNamePrefixWhenLocationNotFound() {
        when(locationService.getAllLocationsWithNamePrefix(LOCATION_NAME_PREFIX))
            .thenReturn(Collections.emptyList());

        assertThat(artefactDeleteService.deleteAllArtefactsWithLocationNamePrefix(LOCATION_NAME_PREFIX))
            .isEqualTo("0 artefacts(s) deleted for location name starting with " + LOCATION_NAME_PREFIX);

        verifyNoInteractions(azureBlobService);
        verifyNoInteractions(artefactRepository);
        verifyNoInteractions(subscriptionManagementService);
    }
}
