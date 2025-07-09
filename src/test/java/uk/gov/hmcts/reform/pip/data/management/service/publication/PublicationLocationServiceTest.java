package uk.gov.hmcts.reform.pip.data.management.service.publication;

import com.fasterxml.jackson.core.JsonProcessingException;
import nl.altindag.log.LogCaptor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.pip.data.management.database.ArtefactRepository;
import uk.gov.hmcts.reform.pip.data.management.database.AzureArtefactBlobService;
import uk.gov.hmcts.reform.pip.data.management.database.LocationRepository;
import uk.gov.hmcts.reform.pip.data.management.errorhandling.exceptions.ArtefactNotFoundException;
import uk.gov.hmcts.reform.pip.data.management.helpers.ArtefactConstantTestHelper;
import uk.gov.hmcts.reform.pip.data.management.models.location.Location;
import uk.gov.hmcts.reform.pip.data.management.models.location.LocationArtefact;
import uk.gov.hmcts.reform.pip.data.management.models.publication.Artefact;
import uk.gov.hmcts.reform.pip.data.management.service.AccountManagementService;
import uk.gov.hmcts.reform.pip.data.management.service.location.LocationService;
import uk.gov.hmcts.reform.pip.data.management.service.PublicationServicesService;
import uk.gov.hmcts.reform.pip.model.account.PiUser;
import uk.gov.hmcts.reform.pip.model.location.LocationType;
import uk.gov.hmcts.reform.pip.model.publication.ListType;
import uk.gov.hmcts.reform.pip.model.system.admin.ActionResult;
import uk.gov.hmcts.reform.pip.model.system.admin.ChangeType;

import java.util.ArrayList;
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
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.pip.data.management.helpers.ArtefactConstantTestHelper.LOCATION_TYPE_MATCH;
import static uk.gov.hmcts.reform.pip.data.management.helpers.ArtefactConstantTestHelper.LOCATION_VENUE;
import static uk.gov.hmcts.reform.pip.data.management.helpers.ArtefactConstantTestHelper.PROVENANCE;
import static uk.gov.hmcts.reform.pip.data.management.helpers.ArtefactConstantTestHelper.PROVENANCE_ID;
import static uk.gov.hmcts.reform.pip.data.management.helpers.ConstantsTestHelper.MESSAGES_MATCH;

@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
public class PublicationLocationServiceTest {
    private static final String EMAIL_ADDRESS = "test@test.com";
    private static final String SSO_EMAIL = "sso@test.com";

    private static final Integer LOCATION_ID = 1;
    private static final String LOCATION_NAME_PREFIX = "TEST_PIP_1234_";

    private Artefact artefact;
    private Artefact artefactWithPayloadUrl;
    private Artefact artefactWithIdAndPayloadUrl;

    private Location location;
    private PiUser piUser;
    private String userId;

    @Mock
    ArtefactRepository artefactRepository;

    @Mock
    LocationRepository locationRepository;

    @Mock
    LocationService locationService;

    @Mock
    PublicationDeleteService publicationDeleteService;

    @Mock
    AzureArtefactBlobService azureArtefactBlobService;

    @Mock
    AccountManagementService accountManagementService;

    @Mock
    PublicationServicesService publicationServicesService;

    @InjectMocks
    private PublicationLocationService publicationLocationService;

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

        userId = UUID.randomUUID().toString();
        piUser = new PiUser();
        piUser.setEmail(EMAIL_ADDRESS);
        piUser.setUserId(userId);

    }

    private void createPayloads() {
        artefact = ArtefactConstantTestHelper.buildArtefact();
        artefactWithPayloadUrl = ArtefactConstantTestHelper.buildArtefactWithPayloadUrl();
        artefactWithIdAndPayloadUrl = ArtefactConstantTestHelper.buildArtefactWithIdAndPayloadUrl();
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
    void testArtefactCountService() {
        List<LocationArtefact> artefactsPerLocations = new ArrayList<>();
        artefactsPerLocations.add(new LocationArtefact("1", 3));
        artefactsPerLocations.add(new LocationArtefact("noMatch", 0));
        List<Object[]> result = new ArrayList<>();
        result.add(new Object[]{"1", "3"});
        when(artefactRepository.countArtefactsByLocation()).thenReturn(result);
        assertEquals(artefactsPerLocations, publicationLocationService.countArtefactsByLocation(),
                     MESSAGES_MATCH);
    }

    @Test
    void testGetLocationTypeVenue() {
        List<ListType> venueListTypes = new ArrayList<>();
        venueListTypes.add(ListType.CROWN_DAILY_LIST);
        venueListTypes.add(ListType.CROWN_FIRM_LIST);
        venueListTypes.add(ListType.CROWN_WARNED_LIST);
        venueListTypes.add(ListType.MAGISTRATES_PUBLIC_LIST);
        venueListTypes.add(ListType.MAGISTRATES_STANDARD_LIST);
        venueListTypes.add(ListType.CIVIL_DAILY_CAUSE_LIST);
        venueListTypes.add(ListType.FAMILY_DAILY_CAUSE_LIST);
        venueListTypes.add(ListType.IAC_DAILY_LIST);

        venueListTypes.forEach(listType ->
                                   assertEquals(
                                       LocationType.VENUE,
                                       publicationLocationService.getLocationType(listType),
                                       LOCATION_TYPE_MATCH));
    }

    @Test
    void testGetLocationTypeOwningHearingLocation() {
        List<ListType> venueListTypes = new ArrayList<>();
        venueListTypes.add(ListType.ET_DAILY_LIST);
        venueListTypes.add(ListType.ET_FORTNIGHTLY_PRESS_LIST);

        venueListTypes.forEach(listType ->
                                   assertEquals(LocationType.OWNING_HEARING_LOCATION,
                                                publicationLocationService.getLocationType(listType),
                                                LOCATION_TYPE_MATCH));
    }

    @Test
    void testGetLocationTypeNational() {
        List<ListType> nationalListTypes = new ArrayList<>();
        nationalListTypes.add(ListType.SJP_PRESS_LIST);
        nationalListTypes.add(ListType.SJP_PUBLIC_LIST);

        nationalListTypes.forEach(listType ->
                                      assertEquals(LocationType.NATIONAL,
                                                   publicationLocationService.getLocationType(listType),
                                                   LOCATION_TYPE_MATCH));
    }

    @Test
    void testFindAllNoMatchArtefacts() {
        when(artefactRepository.findAllNoMatchArtefacts()).thenReturn(List.of(artefact));

        assertEquals(List.of(artefact), publicationLocationService.findAllNoMatchArtefacts(), MESSAGES_MATCH);
    }

    @Test
    void testDeleteArtefactByLocation() throws JsonProcessingException {
        location.setName("NAME");

        try (LogCaptor logCaptor = LogCaptor.forClass(PublicationLocationService.class)) {
            when(artefactRepository.findActiveArtefactsForLocation(any(), eq(LOCATION_ID.toString())))
                .thenReturn(List.of(artefactWithIdAndPayloadUrl));
            when(locationRepository.getLocationByLocationId(LOCATION_ID))
                .thenReturn(Optional.of(location));
            when(accountManagementService.getUserById(any()))
                .thenReturn(piUser);
            when(accountManagementService.getAllAccounts("PI_AAD", "SYSTEM_ADMIN"))
                .thenReturn(List.of(EMAIL_ADDRESS));
            when(accountManagementService.getAllAccounts("SSO", "SYSTEM_ADMIN"))
                .thenReturn(List.of(SSO_EMAIL));

            List<String> systemAdminEmails = List.of(EMAIL_ADDRESS, SSO_EMAIL);

            when(publicationServicesService.sendSystemAdminEmail(systemAdminEmails, EMAIL_ADDRESS, ActionResult.SUCCEEDED,
                                                         "Total 1 artefact(s) for location NAME",
                                                         ChangeType.DELETE_LOCATION_ARTEFACT))
                .thenReturn("System admin message");

            assertEquals("Total 1 artefact deleted for location id 1",
                         publicationLocationService.deleteArtefactByLocation(LOCATION_ID, userId),
                         "The artefacts for given location is not deleted");

            assertTrue(logCaptor.getInfoLogs().get(0).contains("User " + userId
                                                                   + " attempting to delete all artefacts for location "
                                                                   + LOCATION_ID + ". 1 artefact(s) found"),
                       "Expected log does not exist");
        }
    }

    @Test
    void testDeleteArtefactByLocationWhenNoArtefactFound() {

        try (LogCaptor logCaptor = LogCaptor.forClass(PublicationLocationService.class)) {
            when(artefactRepository.findActiveArtefactsForLocation(any(), eq(LOCATION_ID.toString())))
                .thenReturn(List.of());
            assertThrows(
                ArtefactNotFoundException.class, () ->
                    publicationLocationService.deleteArtefactByLocation(LOCATION_ID, userId),
                "ArtefactNotFoundException not thrown when trying to delete a artefact"
                    + " that does not exist"
            );

            assertTrue(logCaptor.getInfoLogs().get(0).contains("User " + userId
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
        when(accountManagementService.getUserById(userId))
            .thenReturn(piUser);
        when(accountManagementService.getAllAccounts(any(), any()))
            .thenThrow(JsonProcessingException.class);

        assertThrows(JsonProcessingException.class, () ->
                         publicationLocationService.deleteArtefactByLocation(LOCATION_ID, userId),
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

        doNothing().when(publicationDeleteService).handleArtefactDeletion(any());

        assertThat(publicationLocationService.deleteAllArtefactsWithLocationNamePrefix(LOCATION_NAME_PREFIX))
            .isEqualTo("3 artefacts(s) deleted for location name starting with " + LOCATION_NAME_PREFIX);
    }

    @Test
    void testDeleteAllArtefactsWithLocationNamePrefixWhenArtefactNotFound() {
        Integer locationId = 1;

        when(locationService.getAllLocationsWithNamePrefix(LOCATION_NAME_PREFIX))
            .thenReturn(List.of(locationId));

        when(artefactRepository.findAllByLocationIdIn(List.of(locationId.toString())))
            .thenReturn(Collections.emptyList());

        assertThat(publicationLocationService.deleteAllArtefactsWithLocationNamePrefix(LOCATION_NAME_PREFIX))
            .isEqualTo("0 artefacts(s) deleted for location name starting with " + LOCATION_NAME_PREFIX);

        verifyNoInteractions(azureArtefactBlobService);
        verifyNoMoreInteractions(artefactRepository);
        verifyNoInteractions(accountManagementService);
    }

    @Test
    void testDeleteAllArtefactsWithLocationNamePrefixWhenLocationNotFound() {
        when(locationService.getAllLocationsWithNamePrefix(LOCATION_NAME_PREFIX))
            .thenReturn(Collections.emptyList());

        assertThat(publicationLocationService.deleteAllArtefactsWithLocationNamePrefix(LOCATION_NAME_PREFIX))
            .isEqualTo("0 artefacts(s) deleted for location name starting with " + LOCATION_NAME_PREFIX);

        verifyNoInteractions(azureArtefactBlobService);
        verifyNoInteractions(artefactRepository);
        verifyNoInteractions(accountManagementService);
    }

}
