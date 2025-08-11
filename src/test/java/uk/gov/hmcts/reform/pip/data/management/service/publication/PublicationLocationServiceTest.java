package uk.gov.hmcts.reform.pip.data.management.service.publication;

import com.fasterxml.jackson.core.JsonProcessingException;
import nl.altindag.log.LogCaptor;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.pip.data.management.database.ArtefactRepository;
import uk.gov.hmcts.reform.pip.data.management.errorhandling.exceptions.ArtefactNotFoundException;
import uk.gov.hmcts.reform.pip.data.management.helpers.ArtefactConstantTestHelper;
import uk.gov.hmcts.reform.pip.data.management.models.location.LocationArtefact;
import uk.gov.hmcts.reform.pip.data.management.models.publication.Artefact;
import uk.gov.hmcts.reform.pip.data.management.service.location.LocationService;
import uk.gov.hmcts.reform.pip.model.location.LocationType;
import uk.gov.hmcts.reform.pip.model.publication.ListType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.pip.data.management.helpers.ArtefactConstantTestHelper.LOCATION_TYPE_MATCH;
import static uk.gov.hmcts.reform.pip.data.management.helpers.ConstantsTestHelper.MESSAGES_MATCH;

@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
class PublicationLocationServiceTest {
    private static final Integer LOCATION_ID = 1;
    private static final String LOCATION_NAME_PREFIX = "TEST_PIP_1234_";
    private static final String USER_ID = UUID.randomUUID().toString();

    private final Artefact artefact = ArtefactConstantTestHelper.buildArtefact();

    @Mock
    private ArtefactRepository artefactRepository;

    @Mock
    private LocationService locationService;

    @Mock
    private PublicationRemovalService publicationRemovalService;

    @InjectMocks
    private PublicationLocationService publicationLocationService;

    @Test
    void testArtefactCountService() {
        List<LocationArtefact> artefactsPerLocations = new ArrayList<>();
        artefactsPerLocations.add(new LocationArtefact("1", 3));
        artefactsPerLocations.add(new LocationArtefact("noMatch", 0));
        List<Object[]> result = new ArrayList<>();
        result.add(new Object[]{"1", "3"});
        when(artefactRepository.countArtefactsByLocation(any())).thenReturn(result);
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
        try (LogCaptor logCaptor = LogCaptor.forClass(PublicationLocationService.class)) {
            when(artefactRepository.findActiveArtefactsForLocation(any(), eq(LOCATION_ID.toString())))
                .thenReturn(List.of(artefact));

            assertEquals("Total 1 artefact deleted for location id 1",
                         publicationLocationService.deleteArtefactByLocation(LOCATION_ID, USER_ID),
                         "The artefacts for given location is not deleted");

            assertTrue(logCaptor.getInfoLogs().get(0)
                           .contains("User " + USER_ID + " attempting to delete all artefacts for location "
                                         + LOCATION_ID + ". 1 artefact(s) found"),
                       "Expected log does not exist");

            verify(publicationRemovalService).deleteArtefactByLocation(any(), any(), any());
        }
    }

    @Test
    void testDeleteArtefactByLocationWhenNoArtefactFound() throws JsonProcessingException {
        try (LogCaptor logCaptor = LogCaptor.forClass(PublicationLocationService.class)) {
            when(artefactRepository.findActiveArtefactsForLocation(any(), eq(LOCATION_ID.toString())))
                .thenReturn(Collections.emptyList());
            assertThrows(
                ArtefactNotFoundException.class, () ->
                    publicationLocationService.deleteArtefactByLocation(LOCATION_ID, USER_ID),
                "ArtefactNotFoundException not thrown when trying to delete a artefact"
                    + " that does not exist"
            );

            assertTrue(logCaptor.getInfoLogs().get(0)
                           .contains("User " + USER_ID + " attempting to delete all artefacts for location "
                                         + LOCATION_ID + ". No artefacts found"),
                       "Expected log does not exist");

            verify(publicationRemovalService, never()).deleteArtefactByLocation(any(), any(), any());
        }
    }

    @Test
    void testDeleteAllArtefactsWithLocationNamePrefix() {
        Artefact artefact1 = new Artefact();
        Integer locationId1 = 1;
        artefact1.setArtefactId(UUID.randomUUID());
        artefact1.setLocationId(locationId1.toString());

        Artefact artefact2 = new Artefact();
        artefact2.setArtefactId(UUID.randomUUID());
        artefact2.setLocationId(locationId1.toString());

        Artefact artefact3 = new Artefact();
        Integer locationId2 = 2;
        artefact3.setArtefactId(UUID.randomUUID());
        artefact3.setLocationId(locationId2.toString());

        List<Artefact> artefactsToDelete = List.of(artefact1, artefact2, artefact3);

        when(locationService.getAllLocationsWithNamePrefix(LOCATION_NAME_PREFIX))
            .thenReturn(List.of(locationId1, locationId2));

        when(artefactRepository.findAllByLocationIdIn(List.of(locationId1.toString(), locationId2.toString())))
            .thenReturn(artefactsToDelete);

        doNothing().when(publicationRemovalService).deleteArtefacts(artefactsToDelete);

        assertThat(publicationLocationService.deleteAllArtefactsWithLocationNamePrefix(LOCATION_NAME_PREFIX))
            .isEqualTo("3 artefacts(s) deleted for location name starting with " + LOCATION_NAME_PREFIX);

        verify(publicationRemovalService).deleteArtefacts(any());
    }

    @Test
    void testDeleteAllArtefactsWithLocationNamePrefixWhenNoArtefactFound() {
        Integer locationId = 1;

        when(locationService.getAllLocationsWithNamePrefix(LOCATION_NAME_PREFIX))
            .thenReturn(List.of(locationId));

        when(artefactRepository.findAllByLocationIdIn(List.of(locationId.toString())))
            .thenReturn(Collections.emptyList());

        assertThat(publicationLocationService.deleteAllArtefactsWithLocationNamePrefix(LOCATION_NAME_PREFIX))
            .isEqualTo("0 artefacts(s) deleted for location name starting with " + LOCATION_NAME_PREFIX);

        verify(publicationRemovalService).deleteArtefacts(any());

    }

    @Test
    void testDeleteAllArtefactsWithLocationNamePrefixWhenLocationNotFound() {
        when(locationService.getAllLocationsWithNamePrefix(LOCATION_NAME_PREFIX))
            .thenReturn(Collections.emptyList());

        assertThat(publicationLocationService.deleteAllArtefactsWithLocationNamePrefix(LOCATION_NAME_PREFIX))
            .isEqualTo("0 artefacts(s) deleted for location name starting with " + LOCATION_NAME_PREFIX);

        verify(publicationRemovalService, never()).deleteArtefacts(any());
    }
}
