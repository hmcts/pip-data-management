package uk.gov.hmcts.reform.pip.data.management.service.publication;

import nl.altindag.log.LogCaptor;
import org.junit.jupiter.api.BeforeAll;
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
import uk.gov.hmcts.reform.pip.data.management.service.PublicationManagementService;
import uk.gov.hmcts.reform.pip.model.account.PiUser;
import uk.gov.hmcts.reform.pip.model.publication.Language;
import uk.gov.hmcts.reform.pip.model.publication.ListType;

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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
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

@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
@SuppressWarnings({"PMD.ExcessiveImports", "PMD.TooManyFields"})
class PublicationDeleteServiceTest {

    @Mock
    ArtefactRepository artefactRepository;

    @Mock
    LocationRepository locationRepository;

    @Mock
    AzureArtefactBlobService azureArtefactBlobService;

    @Mock
    AccountManagementService accountManagementService;

    @Mock
    PublicationManagementService publicationManagementService;

    @Mock
    PublicationRetrievalService artefactService;

    @InjectMocks
    PublicationDeleteService publicationDeleteService;

    private Artefact artefact;
    private Artefact artefactWithPayloadUrl;
    private Artefact artefactWithIdAndPayloadUrl;
    private Artefact artefactWithNoMatchLocationId;

    private Location location;
    private PiUser piUser;
    private String userId;

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
        lenient().when(artefactService.payloadWithinJsonSearchLimit(any())).thenReturn(true);

        userId = UUID.randomUUID().toString();
        piUser = new PiUser();
        piUser.setEmail(EMAIL_ADDRESS);
        piUser.setUserId(userId);

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
        try (LogCaptor logCaptor = LogCaptor.forClass(PublicationDeleteService.class)) {
            when(artefactRepository.findArtefactByArtefactId(ARTEFACT_ID.toString()))
                .thenReturn(Optional.of(artefactWithIdAndPayloadUrl));

            publicationDeleteService.deleteArtefactById(ARTEFACT_ID.toString(), TEST_VALUE);
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
        try (LogCaptor logCaptor = LogCaptor.forClass(PublicationDeleteService.class)) {
            when(artefactRepository.findArtefactByArtefactId(ARTEFACT_ID.toString()))
                .thenReturn(Optional.of(artefactWithNoMatchLocationId));

            publicationDeleteService.deleteArtefactById(ARTEFACT_ID.toString(), TEST_VALUE);
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
        try (LogCaptor logCaptor = LogCaptor.forClass(PublicationDeleteService.class)) {
            artefactWithIdAndPayloadUrl.setIsFlatFile(true);
            when(artefactRepository.findArtefactByArtefactId(ARTEFACT_ID.toString()))
                .thenReturn(Optional.of(artefactWithIdAndPayloadUrl));

            publicationDeleteService.deleteArtefactById(ARTEFACT_ID.toString(), TEST_VALUE);
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
        try (LogCaptor logCaptor = LogCaptor.forClass(PublicationDeleteService.class)) {
            artefactWithNoMatchLocationId.setIsFlatFile(true);
            when(artefactRepository.findArtefactByArtefactId(ARTEFACT_ID.toString()))
                .thenReturn(Optional.of(artefactWithNoMatchLocationId));

            publicationDeleteService.deleteArtefactById(ARTEFACT_ID.toString(), TEST_VALUE);
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
            publicationDeleteService.deleteArtefactById(TEST_VALUE, TEST_VALUE),
            "ArtefactNotFoundException should be thrown");

        assertEquals("No artefact found with the ID: " + TEST_VALUE, ex.getMessage(),
                     MESSAGES_MATCH);
    }

    @Test
    void testArchiveExpiredArtefacts() {
        when(artefactRepository.findOutdatedArtefacts(any())).thenReturn(List.of(artefactWithIdAndPayloadUrl));
        publicationDeleteService.archiveExpiredArtefacts();

        verify(artefactRepository).archiveArtefact(ARTEFACT_ID.toString());
        verify(azureArtefactBlobService).deleteBlob(PAYLOAD_STRIPPED);
        verify(publicationManagementService).deleteFiles(ARTEFACT_ID, ListType.CIVIL_DAILY_CAUSE_LIST,
                                                         Language.ENGLISH);
        verifyNoInteractions(accountManagementService);
    }

    @Test
    void testArchiveExpiredArtefactsWithNoMatchLocationId() {
        when(artefactRepository.findOutdatedArtefacts(any())).thenReturn(List.of(artefactWithNoMatchLocationId));
        publicationDeleteService.archiveExpiredArtefacts();

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

        publicationDeleteService.archiveExpiredArtefacts();
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

        publicationDeleteService.archiveExpiredArtefacts();
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
        publicationDeleteService.archiveExpiredArtefacts();

        verify(artefactRepository).archiveArtefact(testArtefactId.toString());
        verify(azureArtefactBlobService).deleteBlob(PAYLOAD_STRIPPED);
        verifyNoInteractions(publicationManagementService);
        verifyNoInteractions(accountManagementService);
    }

    @Test
    void testArchiveExpiredArtefactsFlatFileWithNoMatchLocationId() {
        artefactWithNoMatchLocationId.setIsFlatFile(true);
        when(artefactRepository.findOutdatedArtefacts(any())).thenReturn(List.of(artefactWithNoMatchLocationId));
        publicationDeleteService.archiveExpiredArtefacts();

        verify(artefactRepository).archiveArtefact(ARTEFACT_ID.toString());
        verify(azureArtefactBlobService).deleteBlob(PAYLOAD_STRIPPED);
        verifyNoInteractions(publicationManagementService);
        verifyNoInteractions(accountManagementService);
    }

    @Test
    void testArchiveExpiredArtefactsWhenArtefactsNotFound() {
        when(artefactRepository.findOutdatedArtefacts(any())).thenReturn(Collections.emptyList());
        publicationDeleteService.archiveExpiredArtefacts();
        verifyNoInteractions(azureArtefactBlobService);
        verifyNoInteractions(accountManagementService);
    }

    @Test
    void testArchiveArtefactById() {
        when(artefactRepository.findArtefactByArtefactId(ARTEFACT_ID.toString()))
            .thenReturn(Optional.of(artefactWithIdAndPayloadUrl));

        publicationDeleteService.archiveArtefactById(ARTEFACT_ID.toString(), UUID.randomUUID().toString());

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

        publicationDeleteService.archiveArtefactById(ARTEFACT_ID.toString(), UUID.randomUUID().toString());

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
            publicationDeleteService.archiveArtefactById(artefactId, randomUuID);
        }, "Attempting to archive an artefact that does not exist should throw an exception");
    }
}
