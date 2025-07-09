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
import uk.gov.hmcts.reform.pip.data.management.database.LocationRepository;
import uk.gov.hmcts.reform.pip.data.management.errorhandling.exceptions.ArtefactNotFoundException;
import uk.gov.hmcts.reform.pip.data.management.helpers.ArtefactConstantTestHelper;
import uk.gov.hmcts.reform.pip.data.management.models.location.Location;
import uk.gov.hmcts.reform.pip.data.management.models.location.LocationArtefact;
import uk.gov.hmcts.reform.pip.data.management.models.publication.Artefact;
import uk.gov.hmcts.reform.pip.data.management.service.AccountManagementService;
import uk.gov.hmcts.reform.pip.data.management.service.PublicationServicesService;
import uk.gov.hmcts.reform.pip.data.management.service.location.LocationService;
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
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.pip.data.management.helpers.ArtefactConstantTestHelper.LOCATION_TYPE_MATCH;
import static uk.gov.hmcts.reform.pip.data.management.helpers.ConstantsTestHelper.MESSAGES_MATCH;

@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
@SuppressWarnings("PMD.ExcessiveImports")
class PublicationLocationServiceTest {
    private static final String USER_ID = UUID.randomUUID().toString();
    private static final String EMAIL_ADDRESS = "test@test.com";
    private static final String SSO_EMAIL = "sso@test.com";

    private static final Integer LOCATION_ID = 1;
    private static final String LOCATION_NAME_PREFIX = "TEST_PIP_1234_";

    private final Artefact artefact = ArtefactConstantTestHelper.buildArtefact();
    private final Location location = ArtefactConstantTestHelper.initialiseCourts();

    private PiUser piUser;

    @Mock
    private ArtefactRepository artefactRepository;

    @Mock
    private LocationRepository locationRepository;

    @Mock
    private LocationService locationService;

    @Mock
    private PublicationRemovalService publicationRemovalService;

    @Mock
    private  AccountManagementService accountManagementService;

    @Mock
    private PublicationServicesService publicationServicesService;

    @InjectMocks
    private PublicationLocationService publicationLocationService;

    @BeforeEach
    void setup() {
        piUser = new PiUser();
        piUser.setEmail(EMAIL_ADDRESS);
        piUser.setUserId(USER_ID);
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
}
