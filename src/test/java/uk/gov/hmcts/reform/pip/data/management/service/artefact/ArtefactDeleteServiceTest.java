package uk.gov.hmcts.reform.pip.data.management.service.artefact;

import com.fasterxml.jackson.core.JsonProcessingException;
import nl.altindag.log.LogCaptor;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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
import uk.gov.hmcts.reform.pip.data.management.models.publication.ListType;
import uk.gov.hmcts.reform.pip.data.management.service.AccountManagementService;
import uk.gov.hmcts.reform.pip.data.management.service.PublicationServicesService;
import uk.gov.hmcts.reform.pip.data.management.service.SubscriptionManagementService;
import uk.gov.hmcts.reform.pip.model.system.admin.ActionResult;
import uk.gov.hmcts.reform.pip.model.system.admin.ChangeType;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static reactor.netty.Metrics.SUCCESS;
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

@SuppressWarnings({"PMD.ExcessiveImports"})
@ExtendWith(MockitoExtension.class)
class ArtefactDeleteServiceTest {

    @Mock
    ArtefactRepository artefactRepository;

    @Mock
    LocationRepository locationRepository;

    @Mock
    AzureBlobService azureBlobService;

    @Mock
    SubscriptionManagementService subscriptionManagementService;

    @Mock
    AccountManagementService accountManagementService;

    @Mock
    PublicationServicesService publicationService;

    @InjectMocks
    ArtefactDeleteService artefactDeleteService;

    private Artefact artefact;
    private Artefact artefactWithPayloadUrl;
    private Artefact artefactWithIdAndPayloadUrl;

    private Location location;
    private static final String REQUESTER_NAME = "ReqName";
    private static final String EMAIL_ADDRESS = "test@test.com";
    private static final Integer LOCATION_ID = 1;

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
    void testDeleteArtefactById() throws IOException {
        try (LogCaptor logCaptor = LogCaptor.forClass(ArtefactDeleteService.class)) {
            when(subscriptionManagementService.sendDeletedArtefactForThirdParties(artefactWithPayloadUrl))
                .thenReturn(SUCCESS);
            when(artefactRepository.findArtefactByArtefactId(ARTEFACT_ID.toString()))
                .thenReturn(Optional.of(artefactWithPayloadUrl));
            when(azureBlobService.deleteBlob(PAYLOAD_STRIPPED)).thenReturn(SUCCESS);
            doNothing().when(artefactRepository).delete(artefactWithPayloadUrl);

            artefactDeleteService.deleteArtefactById(ARTEFACT_ID.toString(), TEST_VALUE);
            assertTrue(logCaptor.getInfoLogs().get(0).contains(String.format(DELETION_TRACK_LOG_MESSAGE, ARTEFACT_ID)),
                       MESSAGES_MATCH);
            assertTrue(logCaptor.getInfoLogs().get(1).contains(SUCCESS), MESSAGES_MATCH);
        } catch (Exception ex) {
            throw new IOException(ex.getMessage());
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
        UUID testArtefactId = UUID.randomUUID();
        artefactWithPayloadUrl.setArtefactId(testArtefactId);
        when(artefactRepository.findOutdatedArtefacts(any())).thenReturn(List.of(artefactWithPayloadUrl));
        artefactDeleteService.archiveExpiredArtefacts();
        verify(azureBlobService).deleteBlob(PAYLOAD_STRIPPED);
        verify(azureBlobService).deletePublicationBlob(testArtefactId + ".pdf");
        verify(artefactRepository).archiveArtefact(testArtefactId.toString());
    }

    @Test
    void testArchiveExpiredArtefactsSjpPublic() {
        UUID testArtefactId = UUID.randomUUID();
        artefactWithPayloadUrl.setArtefactId(testArtefactId);
        artefactWithPayloadUrl.setListType(ListType.SJP_PUBLIC_LIST);
        when(artefactRepository.findOutdatedArtefacts(any())).thenReturn(List.of(artefactWithPayloadUrl));
        artefactDeleteService.archiveExpiredArtefacts();
        verify(azureBlobService).deleteBlob(PAYLOAD_STRIPPED);
        verify(azureBlobService).deletePublicationBlob(artefactWithPayloadUrl.getArtefactId() + ".pdf");
        verify(azureBlobService).deletePublicationBlob(artefactWithPayloadUrl.getArtefactId() + ".xlsx");
        verify(artefactRepository).archiveArtefact(testArtefactId.toString());
    }

    @Test
    void testArchiveExpiredArtefactsSjpPress() {
        UUID testArtefactId = UUID.randomUUID();
        artefactWithPayloadUrl.setArtefactId(testArtefactId);
        artefactWithPayloadUrl.setListType(ListType.SJP_PRESS_LIST);
        when(artefactRepository.findOutdatedArtefacts(any())).thenReturn(List.of(artefactWithPayloadUrl));
        artefactDeleteService.archiveExpiredArtefacts();
        verify(azureBlobService).deleteBlob(PAYLOAD_STRIPPED);
        verify(azureBlobService).deletePublicationBlob(artefactWithPayloadUrl.getArtefactId() + ".pdf");
        verify(azureBlobService).deletePublicationBlob(artefactWithPayloadUrl.getArtefactId() + ".xlsx");
        verify(artefactRepository).archiveArtefact(testArtefactId.toString());
    }

    @Test
    void testArchiveExpiredArtefactsFlatFile() {
        UUID testArtefactId = UUID.randomUUID();
        artefactWithPayloadUrl.setArtefactId(testArtefactId);
        artefactWithPayloadUrl.setListType(ListType.SJP_PRESS_LIST);
        artefactWithPayloadUrl.setIsFlatFile(true);
        when(artefactRepository.findOutdatedArtefacts(any())).thenReturn(List.of(artefactWithPayloadUrl));
        artefactDeleteService.archiveExpiredArtefacts();
        verify(azureBlobService).deleteBlob(PAYLOAD_STRIPPED);
        verify(artefactRepository).archiveArtefact(testArtefactId.toString());
    }

    @Test
    void testArchiveExpiredArtefactsWhenArtefactsNotFound() {
        when(artefactRepository.findOutdatedArtefacts(any())).thenReturn(Collections.emptyList());
        artefactDeleteService.archiveExpiredArtefacts();
        verifyNoInteractions(azureBlobService);
    }

    @Test
    void testArchivedEndpoint() {
        String artefactId = UUID.randomUUID().toString();

        when(artefactRepository.findArtefactByArtefactId(artefactId))
            .thenReturn(Optional.of(artefactWithIdAndPayloadUrl));

        artefactDeleteService.archiveArtefactById(artefactId, UUID.randomUUID().toString());

        verify(azureBlobService, times(1))
            .deleteBlob(any());
        verify(azureBlobService, times(1))
            .deletePublicationBlob(any());
        verify(subscriptionManagementService, times(1))
            .sendDeletedArtefactForThirdParties(any());
        verify(artefactRepository, times(1))
            .archiveArtefact(artefactId);
    }

    @Test
    void testArchivedEndpointNotFound() {
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

        when(artefactRepository.findActiveArtefactsForLocation(any(), eq(LOCATION_ID.toString())))
            .thenReturn(List.of(artefactWithIdAndPayloadUrl));
        when(locationRepository.getLocationByLocationId(LOCATION_ID))
            .thenReturn(Optional.of(location));
        when(accountManagementService.getUserInfo(any()))
            .thenReturn("{\"displayName\": \"ReqName\"}");
        when(accountManagementService.getAllAccounts("PI_AAD", "SYSTEM_ADMIN"))
            .thenReturn(List.of(EMAIL_ADDRESS));
        when(publicationService.sendSystemAdminEmail(List.of(EMAIL_ADDRESS), REQUESTER_NAME, ActionResult.SUCCEEDED,
        "Total 1 artefact(s) for location NAME",
        ChangeType.DELETE_LOCATION_ARTEFACT))
            .thenReturn("Total 1 artefact deleted for location id 1");

        doNothing().when(artefactRepository).delete(artefactWithIdAndPayloadUrl);

        assertEquals("Total 1 artefact deleted for location id 1",
                     artefactDeleteService.deleteArtefactByLocation(LOCATION_ID, REQUESTER_NAME),
                     "The artefacts for given location is not deleted");
        verify(azureBlobService, times(1))
            .deleteBlob(any());
        verify(azureBlobService, times(1))
            .deletePublicationBlob(any());
    }

    @Test
    void testDeleteArtefactByLocationWhenNoArtefactFound() {
        when(artefactRepository.findActiveArtefactsForLocation(any(), eq(LOCATION_ID.toString())))
            .thenReturn(List.of());
        assertThrows(ArtefactNotFoundException.class, () ->
                         artefactDeleteService.deleteArtefactByLocation(LOCATION_ID, REQUESTER_NAME),
                     "ArtefactNotFoundException not thrown when trying to delete a artefact"
                         + " that does not exist");
    }

    @Test
    void testDeleteArtefactByLocationJsonProcessingException() {

        when(artefactRepository.findActiveArtefactsForLocation(any(), eq(LOCATION_ID.toString())))
            .thenReturn(List.of(artefactWithIdAndPayloadUrl));
        when(locationRepository.getLocationByLocationId(LOCATION_ID))
            .thenReturn(Optional.of(location));
        when(accountManagementService.getUserInfo(any()))
            .thenReturn("test");

        assertThrows(JsonProcessingException.class, () ->
                         artefactDeleteService.deleteArtefactByLocation(LOCATION_ID, REQUESTER_NAME),
                     "JsonProcessingException not thrown when trying to get errored system admin"
                         + " api response");
    }

}
