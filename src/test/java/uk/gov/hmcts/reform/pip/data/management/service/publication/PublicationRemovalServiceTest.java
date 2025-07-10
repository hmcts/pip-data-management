package uk.gov.hmcts.reform.pip.data.management.service.publication;

import com.fasterxml.jackson.core.JsonProcessingException;
import nl.altindag.log.LogCaptor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.pip.data.management.database.ArtefactRepository;
import uk.gov.hmcts.reform.pip.data.management.database.AzureArtefactBlobService;
import uk.gov.hmcts.reform.pip.data.management.database.LocationRepository;
import uk.gov.hmcts.reform.pip.data.management.errorhandling.exceptions.ArtefactNotFoundException;
import uk.gov.hmcts.reform.pip.data.management.errorhandling.exceptions.NotFoundException;
import uk.gov.hmcts.reform.pip.data.management.helpers.ArtefactConstantTestHelper;
import uk.gov.hmcts.reform.pip.data.management.models.location.Location;
import uk.gov.hmcts.reform.pip.data.management.models.publication.Artefact;
import uk.gov.hmcts.reform.pip.data.management.service.AccountManagementService;
import uk.gov.hmcts.reform.pip.data.management.service.SystemAdminNotificationService;
import uk.gov.hmcts.reform.pip.model.account.PiUser;
import uk.gov.hmcts.reform.pip.model.publication.Language;
import uk.gov.hmcts.reform.pip.model.publication.ListType;
import uk.gov.hmcts.reform.pip.model.system.admin.ActionResult;
import uk.gov.hmcts.reform.pip.model.system.admin.ChangeType;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.pip.data.management.helpers.ArtefactConstantTestHelper.ARTEFACT_ID;
import static uk.gov.hmcts.reform.pip.data.management.helpers.ArtefactConstantTestHelper.DELETION_TRACK_LOG_MESSAGE;
import static uk.gov.hmcts.reform.pip.data.management.helpers.ArtefactConstantTestHelper.PAYLOAD_STRIPPED;
import static uk.gov.hmcts.reform.pip.data.management.helpers.ArtefactConstantTestHelper.TEST_VALUE;
import static uk.gov.hmcts.reform.pip.data.management.helpers.ConstantsTestHelper.MESSAGES_MATCH;

@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
@SuppressWarnings({"PMD.ExcessiveImports"})
class PublicationRemovalServiceTest {
    private static final String USER_ID = UUID.randomUUID().toString();
    private static final String EMAIL_ADDRESS = "test@test.com";
    private static final String SSO_EMAIL = "sso@test.com";
    private static final Integer LOCATION_ID = 1;

    private final Artefact artefactWithPayloadUrl = ArtefactConstantTestHelper.buildArtefactWithPayloadUrl();
    private final Artefact artefactWithIdAndPayloadUrl = ArtefactConstantTestHelper.buildArtefactWithIdAndPayloadUrl();
    private final Artefact artefactWithNoMatchLocationId = ArtefactConstantTestHelper
        .buildNoMatchArtefactWithIdAndPayloadUrl();

    private final Location location = ArtefactConstantTestHelper.initialiseCourts();
    private PiUser piUser;

    @Mock
    private ArtefactRepository artefactRepository;

    @Mock
    private LocationRepository locationRepository;

    @Mock
    private AzureArtefactBlobService azureArtefactBlobService;

    @Mock
    private AccountManagementService accountManagementService;

    @Mock
    private PublicationManagementService publicationManagementService;

    @Mock
    private SystemAdminNotificationService systemAdminNotificationService;

    @InjectMocks
    private PublicationRemovalService publicationRemovalService;

    @BeforeEach
    void setup() {
        piUser = new PiUser();
        piUser.setEmail(EMAIL_ADDRESS);
        piUser.setUserId(USER_ID);
    }

    @Test
    void testDeleteArtefactById() {
        try (LogCaptor logCaptor = LogCaptor.forClass(PublicationRemovalService.class)) {
            when(artefactRepository.findArtefactByArtefactId(ARTEFACT_ID.toString()))
                .thenReturn(Optional.of(artefactWithIdAndPayloadUrl));

            publicationRemovalService.deleteArtefactById(ARTEFACT_ID.toString(), TEST_VALUE);
            assertTrue(logCaptor.getInfoLogs().get(0).contains(String.format(DELETION_TRACK_LOG_MESSAGE, ARTEFACT_ID)),
                       MESSAGES_MATCH);

            InOrder orderVerifier = inOrder(azureArtefactBlobService, publicationManagementService,
                                            artefactRepository,accountManagementService);
            orderVerifier.verify(azureArtefactBlobService).deleteBlob(PAYLOAD_STRIPPED);
            orderVerifier.verify(publicationManagementService).deleteFiles(ARTEFACT_ID, ListType.CIVIL_DAILY_CAUSE_LIST,
                                                                           Language.ENGLISH);
            orderVerifier.verify(artefactRepository).delete(artefactWithIdAndPayloadUrl);
            orderVerifier.verify(accountManagementService)
                .sendDeletedArtefactForThirdParties(artefactWithIdAndPayloadUrl);
        }
    }

    @Test
    void testDeleteArtefactByIdWithNoMatchLocationId() {
        try (LogCaptor logCaptor = LogCaptor.forClass(PublicationRemovalService.class)) {
            when(artefactRepository.findArtefactByArtefactId(ARTEFACT_ID.toString()))
                .thenReturn(Optional.of(artefactWithNoMatchLocationId));

            publicationRemovalService.deleteArtefactById(ARTEFACT_ID.toString(), TEST_VALUE);
            assertTrue(logCaptor.getInfoLogs().get(0).contains(String.format(DELETION_TRACK_LOG_MESSAGE, ARTEFACT_ID)),
                       MESSAGES_MATCH);

            verify(artefactRepository).delete(artefactWithNoMatchLocationId);
            verify(azureArtefactBlobService).deleteBlob(PAYLOAD_STRIPPED);
            verifyNoInteractions(publicationManagementService);
            verifyNoInteractions(accountManagementService);
        }
    }

    @Test
    void testDeleteArtefactByIdFlatFile() {
        try (LogCaptor logCaptor = LogCaptor.forClass(PublicationRemovalService.class)) {
            artefactWithIdAndPayloadUrl.setIsFlatFile(true);
            when(artefactRepository.findArtefactByArtefactId(ARTEFACT_ID.toString()))
                .thenReturn(Optional.of(artefactWithIdAndPayloadUrl));

            publicationRemovalService.deleteArtefactById(ARTEFACT_ID.toString(), TEST_VALUE);
            assertTrue(logCaptor.getInfoLogs().get(0).contains(String.format(DELETION_TRACK_LOG_MESSAGE, ARTEFACT_ID)),
                       MESSAGES_MATCH);

            InOrder orderVerifier = inOrder(azureArtefactBlobService, publicationManagementService,
                                            artefactRepository, accountManagementService);
            orderVerifier.verify(azureArtefactBlobService).deleteBlob(PAYLOAD_STRIPPED);
            orderVerifier.verify(artefactRepository).delete(artefactWithIdAndPayloadUrl);
            orderVerifier.verify(accountManagementService)
                .sendDeletedArtefactForThirdParties(artefactWithIdAndPayloadUrl);
            verifyNoInteractions(publicationManagementService);
        }
    }

    @Test
    void testDeleteArtefactByIdFlatFileWithNoMatchLocationId() {
        try (LogCaptor logCaptor = LogCaptor.forClass(PublicationRemovalService.class)) {
            artefactWithNoMatchLocationId.setIsFlatFile(true);
            when(artefactRepository.findArtefactByArtefactId(ARTEFACT_ID.toString()))
                .thenReturn(Optional.of(artefactWithNoMatchLocationId));

            publicationRemovalService.deleteArtefactById(ARTEFACT_ID.toString(), TEST_VALUE);
            assertTrue(logCaptor.getInfoLogs().get(0).contains(String.format(DELETION_TRACK_LOG_MESSAGE, ARTEFACT_ID)),
                       MESSAGES_MATCH);

            verify(artefactRepository).delete(artefactWithNoMatchLocationId);
            verify(azureArtefactBlobService).deleteBlob(PAYLOAD_STRIPPED);
            verifyNoInteractions(publicationManagementService);
            verifyNoInteractions(accountManagementService);
        }
    }

    @Test
    void testDeleteArtefactByIdThrows() {
        ArtefactNotFoundException ex = assertThrows(ArtefactNotFoundException.class, () ->
            publicationRemovalService.deleteArtefactById(TEST_VALUE, TEST_VALUE),
            "ArtefactNotFoundException should be thrown");

        assertEquals("No artefact found with the ID: " + TEST_VALUE, ex.getMessage(),
                     MESSAGES_MATCH);
    }

    @Test
    void testArchiveExpiredArtefacts() {
        when(artefactRepository.findOutdatedArtefacts(any())).thenReturn(List.of(artefactWithIdAndPayloadUrl));
        publicationRemovalService.archiveExpiredArtefacts();

        verify(artefactRepository).archiveArtefact(ARTEFACT_ID.toString());
        verify(azureArtefactBlobService).deleteBlob(PAYLOAD_STRIPPED);
        verify(publicationManagementService).deleteFiles(ARTEFACT_ID, ListType.CIVIL_DAILY_CAUSE_LIST,
                                                         Language.ENGLISH);
        verifyNoInteractions(accountManagementService);
    }

    @Test
    void testArchiveExpiredArtefactsWithNoMatchLocationId() {
        when(artefactRepository.findOutdatedArtefacts(any())).thenReturn(List.of(artefactWithNoMatchLocationId));
        publicationRemovalService.archiveExpiredArtefacts();

        verify(artefactRepository).archiveArtefact(ARTEFACT_ID.toString());
        verify(azureArtefactBlobService).deleteBlob(PAYLOAD_STRIPPED);
        verifyNoInteractions(publicationManagementService);
        verifyNoInteractions(accountManagementService);
    }

    @Test
    void testArchiveExpiredArtefactsSjpPublic() {
        UUID testArtefactId = UUID.randomUUID();
        artefactWithPayloadUrl.setArtefactId(testArtefactId);
        artefactWithPayloadUrl.setListType(ListType.SJP_PUBLIC_LIST);
        when(artefactRepository.findOutdatedArtefacts(any())).thenReturn(List.of(artefactWithPayloadUrl));

        publicationRemovalService.archiveExpiredArtefacts();
        verify(artefactRepository).archiveArtefact(testArtefactId.toString());
        verify(azureArtefactBlobService).deleteBlob(PAYLOAD_STRIPPED);
        verify(publicationManagementService).deleteFiles(testArtefactId, ListType.SJP_PUBLIC_LIST, Language.ENGLISH);
        verifyNoInteractions(accountManagementService);
    }

    @Test
    void testArchiveExpiredArtefactsSjpPress() {
        UUID testArtefactId = UUID.randomUUID();
        artefactWithPayloadUrl.setArtefactId(testArtefactId);
        artefactWithPayloadUrl.setListType(ListType.SJP_PRESS_LIST);
        when(artefactRepository.findOutdatedArtefacts(any())).thenReturn(List.of(artefactWithPayloadUrl));

        publicationRemovalService.archiveExpiredArtefacts();
        verify(artefactRepository).archiveArtefact(testArtefactId.toString());
        verify(azureArtefactBlobService).deleteBlob(PAYLOAD_STRIPPED);
        verify(publicationManagementService).deleteFiles(testArtefactId, ListType.SJP_PRESS_LIST, Language.ENGLISH);
        verifyNoInteractions(accountManagementService);
    }

    @Test
    void testArchiveExpiredArtefactsFlatFile() {
        UUID testArtefactId = UUID.randomUUID();
        artefactWithPayloadUrl.setArtefactId(testArtefactId);
        artefactWithPayloadUrl.setListType(ListType.SJP_PRESS_LIST);
        artefactWithPayloadUrl.setIsFlatFile(true);

        when(artefactRepository.findOutdatedArtefacts(any())).thenReturn(List.of(artefactWithPayloadUrl));
        publicationRemovalService.archiveExpiredArtefacts();

        verify(artefactRepository).archiveArtefact(testArtefactId.toString());
        verify(azureArtefactBlobService).deleteBlob(PAYLOAD_STRIPPED);
        verifyNoInteractions(publicationManagementService);
        verifyNoInteractions(accountManagementService);
    }

    @Test
    void testArchiveExpiredArtefactsFlatFileWithNoMatchLocationId() {
        artefactWithNoMatchLocationId.setIsFlatFile(true);
        when(artefactRepository.findOutdatedArtefacts(any())).thenReturn(List.of(artefactWithNoMatchLocationId));
        publicationRemovalService.archiveExpiredArtefacts();

        verify(artefactRepository).archiveArtefact(ARTEFACT_ID.toString());
        verify(azureArtefactBlobService).deleteBlob(PAYLOAD_STRIPPED);
        verifyNoInteractions(publicationManagementService);
        verifyNoInteractions(accountManagementService);
    }

    @Test
    void testArchiveExpiredArtefactsWhenArtefactsNotFound() {
        when(artefactRepository.findOutdatedArtefacts(any())).thenReturn(Collections.emptyList());
        publicationRemovalService.archiveExpiredArtefacts();
        verifyNoInteractions(azureArtefactBlobService);
        verifyNoInteractions(accountManagementService);
    }

    @Test
    void testArchiveArtefactById() {
        when(artefactRepository.findArtefactByArtefactId(ARTEFACT_ID.toString()))
            .thenReturn(Optional.of(artefactWithIdAndPayloadUrl));

        publicationRemovalService.archiveArtefactById(ARTEFACT_ID.toString(), UUID.randomUUID().toString());

        verify(artefactRepository).archiveArtefact(ARTEFACT_ID.toString());
        verify(azureArtefactBlobService).deleteBlob(PAYLOAD_STRIPPED);
        verify(publicationManagementService).deleteFiles(ARTEFACT_ID, ListType.CIVIL_DAILY_CAUSE_LIST,
                                                         Language.ENGLISH);
        verify(accountManagementService).sendDeletedArtefactForThirdParties(artefactWithIdAndPayloadUrl);
    }

    @Test
    void testArchiveArtefactByIdWithNoMatchLocationId() {
        when(artefactRepository.findArtefactByArtefactId(ARTEFACT_ID.toString()))
            .thenReturn(Optional.of(artefactWithNoMatchLocationId));

        publicationRemovalService.archiveArtefactById(ARTEFACT_ID.toString(), UUID.randomUUID().toString());

        verify(artefactRepository).archiveArtefact(ARTEFACT_ID.toString());
        verify(azureArtefactBlobService).deleteBlob(PAYLOAD_STRIPPED);
        verifyNoInteractions(publicationManagementService);
        verifyNoInteractions(accountManagementService);
    }

    @Test
    void testArchiveArtefactByIdNotFound() {
        String artefactId = UUID.randomUUID().toString();
        when(artefactRepository.findArtefactByArtefactId(artefactId)).thenReturn(Optional.empty());

        String randomUuID = UUID.randomUUID().toString();
        assertThrows(NotFoundException.class, () -> {
            publicationRemovalService.archiveArtefactById(artefactId, randomUuID);
        }, "Attempting to archive an artefact that does not exist should throw an exception");
    }

    @Test
    void testDeleteArtefactByLocation() throws JsonProcessingException {
        location.setName("NAME");
        when(locationRepository.getLocationByLocationId(LOCATION_ID))
            .thenReturn(Optional.of(location));
        when(accountManagementService.getUserById(any()))
            .thenReturn(piUser);

        publicationRemovalService.deleteArtefactByLocation(List.of(artefactWithIdAndPayloadUrl), LOCATION_ID, USER_ID);

        InOrder orderVerifier = inOrder(azureArtefactBlobService, publicationManagementService,
                                        artefactRepository, accountManagementService, systemAdminNotificationService);
        orderVerifier.verify(azureArtefactBlobService).deleteBlob(any());
        orderVerifier.verify(publicationManagementService).deleteFiles(ARTEFACT_ID, ListType.CIVIL_DAILY_CAUSE_LIST,
                                                                       Language.ENGLISH);
        orderVerifier.verify(artefactRepository).delete(artefactWithIdAndPayloadUrl);
        orderVerifier.verify(accountManagementService).sendDeletedArtefactForThirdParties(any());
        orderVerifier.verify(systemAdminNotificationService).sendEmailNotification(
            EMAIL_ADDRESS, ActionResult.SUCCEEDED, "Total 1 artefact(s) for location NAME",
            ChangeType.DELETE_LOCATION_ARTEFACT
        );
    }

    @Test
    void testDeleteArtefactByLocationJsonProcessingException() throws JsonProcessingException {
        location.setName("NAME");
        when(locationRepository.getLocationByLocationId(LOCATION_ID)).thenReturn(Optional.of(location));
        when(accountManagementService.getUserById(USER_ID)).thenReturn(piUser);
        doThrow(JsonProcessingException.class).when(systemAdminNotificationService).sendEmailNotification(
            EMAIL_ADDRESS, ActionResult.SUCCEEDED, "Total 1 artefact(s) for location NAME",
            ChangeType.DELETE_LOCATION_ARTEFACT
        );

        assertThrows(JsonProcessingException.class, () ->
                         publicationRemovalService.deleteArtefactByLocation(List.of(artefactWithIdAndPayloadUrl),
                                                                            LOCATION_ID, USER_ID),
                     "JsonProcessingException not thrown when trying to get errored system admin"
                         + " api response");
    }

    @Test
    void testDeleteArtefacts() {
        Artefact artefact1 = new Artefact();
        String payload1 = "payload/url1";
        artefact1.setArtefactId(UUID.randomUUID());
        artefact1.setLocationId(LOCATION_ID.toString());
        artefact1.setPayload(payload1);

        Artefact artefact2 = new Artefact();
        String payload2 = "payload/url2";
        artefact2.setArtefactId(UUID.randomUUID());
        artefact2.setLocationId(LOCATION_ID.toString());
        artefact2.setPayload(payload2);

        Artefact artefact3 = new Artefact();
        String payload3 = "payload/url3";
        artefact3.setArtefactId(UUID.randomUUID());
        artefact3.setLocationId(LOCATION_ID.toString());
        artefact3.setPayload(payload3);

        List<Artefact> artefactsToDelete = List.of(artefact1, artefact2, artefact3);

        publicationRemovalService.deleteArtefacts(artefactsToDelete);

        verify(azureArtefactBlobService).deleteBlob("url1");
        verify(azureArtefactBlobService).deleteBlob("url2");
        verify(azureArtefactBlobService).deleteBlob("url3");
        verify(artefactRepository, times(3)).delete(any());
        verify(accountManagementService, times(3)).sendDeletedArtefactForThirdParties(any());
    }

    @Test
    void testDeleteArtefactsWithNoArtefactsToDelete() {
        publicationRemovalService.deleteArtefacts(Collections.emptyList());

        verifyNoInteractions(azureArtefactBlobService);
        verifyNoMoreInteractions(artefactRepository);
        verifyNoInteractions(accountManagementService);
    }
}
