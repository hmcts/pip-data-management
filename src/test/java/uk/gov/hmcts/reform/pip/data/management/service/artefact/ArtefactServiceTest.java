package uk.gov.hmcts.reform.pip.data.management.service.artefact;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.ByteArrayResource;
import uk.gov.hmcts.reform.pip.data.management.database.ArtefactRepository;
import uk.gov.hmcts.reform.pip.data.management.database.AzureBlobService;
import uk.gov.hmcts.reform.pip.data.management.database.LocationRepository;
import uk.gov.hmcts.reform.pip.data.management.errorhandling.exceptions.NotFoundException;
import uk.gov.hmcts.reform.pip.data.management.helpers.ArtefactConstantTestHelper;
import uk.gov.hmcts.reform.pip.data.management.models.location.Location;
import uk.gov.hmcts.reform.pip.data.management.models.location.LocationArtefact;
import uk.gov.hmcts.reform.pip.data.management.models.publication.Artefact;
import uk.gov.hmcts.reform.pip.data.management.service.AccountManagementService;
import uk.gov.hmcts.reform.pip.model.location.LocationType;
import uk.gov.hmcts.reform.pip.model.publication.ListType;
import uk.gov.hmcts.reform.pip.model.publication.Sensitivity;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.pip.data.management.helpers.ArtefactConstantTestHelper.ARTEFACT_ID;
import static uk.gov.hmcts.reform.pip.data.management.helpers.ArtefactConstantTestHelper.LOCATION_TYPE_MATCH;
import static uk.gov.hmcts.reform.pip.data.management.helpers.ArtefactConstantTestHelper.LOCATION_VENUE;
import static uk.gov.hmcts.reform.pip.data.management.helpers.ArtefactConstantTestHelper.PAYLOAD;
import static uk.gov.hmcts.reform.pip.data.management.helpers.ArtefactConstantTestHelper.PROVENANCE;
import static uk.gov.hmcts.reform.pip.data.management.helpers.ArtefactConstantTestHelper.PROVENANCE_ID;
import static uk.gov.hmcts.reform.pip.data.management.helpers.ArtefactConstantTestHelper.SEARCH_VALUES;
import static uk.gov.hmcts.reform.pip.data.management.helpers.ArtefactConstantTestHelper.TEST_FILE;
import static uk.gov.hmcts.reform.pip.data.management.helpers.ArtefactConstantTestHelper.TEST_KEY;
import static uk.gov.hmcts.reform.pip.data.management.helpers.ArtefactConstantTestHelper.TEST_VALUE;
import static uk.gov.hmcts.reform.pip.data.management.helpers.ArtefactConstantTestHelper.USER_ID;
import static uk.gov.hmcts.reform.pip.data.management.helpers.ArtefactConstantTestHelper.VALIDATION_ARTEFACT_NOT_MATCH;
import static uk.gov.hmcts.reform.pip.data.management.helpers.ArtefactConstantTestHelper.VALIDATION_NOT_THROWN_MESSAGE;
import static uk.gov.hmcts.reform.pip.data.management.helpers.ConstantsTestHelper.MESSAGES_MATCH;

@SuppressWarnings({"PMD.ExcessiveImports"})
@ExtendWith(MockitoExtension.class)
class ArtefactServiceTest {

    @Mock
    ArtefactRepository artefactRepository;

    @Mock
    LocationRepository locationRepository;

    @Mock
    AzureBlobService azureBlobService;

    @Mock
    AccountManagementService accountManagementService;

    @InjectMocks
    ArtefactService artefactService;

    private Artefact artefact;
    private Artefact artefactClassified;
    private Artefact artefactWithPayloadUrl;
    private Artefact artefactWithPayloadUrlClassified;
    private Artefact artefactWithIdAndPayloadUrl;

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
    }

    private void createClassifiedPayloads() {
        artefactClassified = ArtefactConstantTestHelper.buildClassifiedPayloads();
        artefactWithPayloadUrlClassified = ArtefactConstantTestHelper.buildArtefactWithPayloadUrlClassified();

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
    void testArtefactPayloadFromAzureWhenAdmin() {
        when(artefactRepository.findArtefactByArtefactId(any())).thenReturn(Optional.of(artefactWithPayloadUrl));
        when(azureBlobService.getBlobData(any())).thenReturn(PAYLOAD);
        assertEquals(PAYLOAD, artefactService.getPayloadByArtefactId(ARTEFACT_ID),
                     VALIDATION_ARTEFACT_NOT_MATCH
        );
    }

    @Test
    void testArtefactPayloadFromAzureWhenArtefactIsPublic() {
        when(artefactRepository.findByArtefactId(any(), any())).thenReturn(Optional.of(artefactWithPayloadUrl));
        when(azureBlobService.getBlobData(any()))
            .thenReturn(PAYLOAD);
        assertEquals(PAYLOAD, artefactService.getPayloadByArtefactId(ARTEFACT_ID, USER_ID),
                     VALIDATION_ARTEFACT_NOT_MATCH
        );
    }

    @Test
    void testArtefactPayloadFromAzureWhenArtefactIsNotPublicAndIsAuthorised() {
        when(artefactRepository.findByArtefactId(any(), any()))
            .thenReturn(Optional.of(artefactWithPayloadUrlClassified));
        when(azureBlobService.getBlobData(any()))
            .thenReturn(PAYLOAD);
        when(accountManagementService.getIsAuthorised(USER_ID,
                                                      ListType.CIVIL_DAILY_CAUSE_LIST, Sensitivity.CLASSIFIED))
            .thenReturn(true);

        assertEquals(PAYLOAD, artefactService.getPayloadByArtefactId(ARTEFACT_ID, USER_ID),
                     VALIDATION_ARTEFACT_NOT_MATCH
        );
    }

    @Test
    void testArtefactPayloadFromAzureWhenArtefactIsNotPublicAndIsNotAuthorised() {
        when(artefactRepository.findByArtefactId(any(), any()))
            .thenReturn(Optional.of(artefactWithPayloadUrlClassified));

        when(accountManagementService.getIsAuthorised(USER_ID,
                                                      ListType.CIVIL_DAILY_CAUSE_LIST, Sensitivity.CLASSIFIED))
            .thenReturn(false);

        assertThrows(NotFoundException.class, () -> artefactService.getPayloadByArtefactId(ARTEFACT_ID, USER_ID),
                     VALIDATION_NOT_THROWN_MESSAGE);
    }

    @Test
    void testArtefactFileFromAzureWhenAdmin() {
        byte[] testData = TEST_FILE.getBytes();
        when(artefactRepository.findArtefactByArtefactId(any())).thenReturn(Optional.of(artefactWithPayloadUrl));
        when(azureBlobService.getBlobFile(any())).thenReturn(new ByteArrayResource(testData));

        assertEquals(new ByteArrayResource(testData), artefactService.getFlatFileByArtefactID(
            ARTEFACT_ID), VALIDATION_ARTEFACT_NOT_MATCH);
    }

    @Test
    void testArtefactFileFromAzureWhenArtefactIsPublic() {
        byte[] testData = TEST_FILE.getBytes();
        when(artefactRepository.findByArtefactId(any(), any())).thenReturn(Optional.of(artefactWithPayloadUrl));
        when(azureBlobService.getBlobFile(any())).thenReturn(new ByteArrayResource(testData));

        assertEquals(new ByteArrayResource(testData), artefactService.getFlatFileByArtefactID(
            ARTEFACT_ID,
            USER_ID), VALIDATION_ARTEFACT_NOT_MATCH);
    }

    @Test
    void testArtefactFileFromAzureWhenArtefactIsNotPublic() {
        byte[] testData = TEST_FILE.getBytes();
        when(artefactRepository.findByArtefactId(any(), any()))
            .thenReturn(Optional.of(artefactWithPayloadUrlClassified));
        when(azureBlobService.getBlobFile(any())).thenReturn(new ByteArrayResource(testData));

        when(accountManagementService.getIsAuthorised(USER_ID,
                                                      ListType.CIVIL_DAILY_CAUSE_LIST, Sensitivity.CLASSIFIED))
            .thenReturn(true);

        assertEquals(new ByteArrayResource(testData), artefactService.getFlatFileByArtefactID(
            ARTEFACT_ID,
            USER_ID), VALIDATION_ARTEFACT_NOT_MATCH);
    }

    @Test
    void testArtefactFileFromAzureWhenArtefactIsNotPublicAndNotAuthorised() {
        when(artefactRepository.findByArtefactId(any(), any()))
            .thenReturn(Optional.of(artefactWithPayloadUrlClassified));

        when(accountManagementService.getIsAuthorised(USER_ID, ListType.CIVIL_DAILY_CAUSE_LIST, Sensitivity.CLASSIFIED))
            .thenReturn(false);

        assertThrows(NotFoundException.class, () -> artefactService.getFlatFileByArtefactID(
            ARTEFACT_ID,
            USER_ID), VALIDATION_NOT_THROWN_MESSAGE);
    }

    @Test
    void testArtefactPayloadFromAzureWhenUnauthorized() {
        when(artefactRepository.findByArtefactId(any(), any()))
            .thenReturn(Optional.of(artefactWithPayloadUrl));
        when(azureBlobService.getBlobData(any()))
            .thenReturn(PAYLOAD);
        assertEquals(PAYLOAD, artefactService.getPayloadByArtefactId(ARTEFACT_ID, USER_ID),
                     VALIDATION_ARTEFACT_NOT_MATCH
        );
    }

    @Test
    void testArtefactContentFromAzureWhenDoesNotExist() {
        when(artefactRepository.findByArtefactId(any(), any())).thenReturn(Optional.empty());
        assertThrows(
            NotFoundException.class,
            ()
                -> artefactService.getPayloadByArtefactId(ARTEFACT_ID, USER_ID),
            "Not Found exception has not been thrown when artefact does not exist"
        );
    }

    @Test
    void testArtefactFileFromAzureWhenUnauthorized() {
        byte[] testData = TEST_FILE.getBytes();
        when(artefactRepository.findByArtefactId(any(), any()))
            .thenReturn(Optional.of(artefactWithPayloadUrl));
        when(azureBlobService.getBlobFile(any()))
            .thenReturn(new ByteArrayResource(testData));

        assertEquals(new ByteArrayResource(testData), artefactService.getFlatFileByArtefactID(ARTEFACT_ID, USER_ID),
                     VALIDATION_ARTEFACT_NOT_MATCH
        );
    }

    @Test
    void testArtefactFileFromAzureWhenDoesNotExist() {
        when(artefactRepository.findByArtefactId(any(), any())).thenReturn(Optional.empty());
        assertThrows(
            NotFoundException.class,
            ()
                -> artefactService.getFlatFileByArtefactID(ARTEFACT_ID, USER_ID),
            "Not Found exception has not been thrown when artefact does not exist"
        );
    }

    @Test
    void testArtefactMetadataFromAzureWhenPublic() {
        when(artefactRepository.findByArtefactId(any(), any())).thenReturn(Optional.of(artefact));

        assertEquals(artefact, artefactService.getMetadataByArtefactId(ARTEFACT_ID, USER_ID),
                     VALIDATION_ARTEFACT_NOT_MATCH
        );
    }

    @Test
    void testArtefactMetadataFromAzureWhenNotPublic() {
        when(artefactRepository.findByArtefactId(any(), any())).thenReturn(Optional.of(artefactClassified));

        when(accountManagementService.getIsAuthorised(USER_ID, ListType.CIVIL_DAILY_CAUSE_LIST, Sensitivity.CLASSIFIED))
            .thenReturn(true);

        assertEquals(artefactClassified, artefactService.getMetadataByArtefactId(ARTEFACT_ID, USER_ID),
                     VALIDATION_ARTEFACT_NOT_MATCH
        );
    }

    @Test
    void testArtefactMetadataFromAzureWhenNotPublicAndNotAuthorised() {
        when(artefactRepository.findByArtefactId(any(), any())).thenReturn(Optional.of(artefactClassified));

        when(accountManagementService.getIsAuthorised(USER_ID, ListType.CIVIL_DAILY_CAUSE_LIST, Sensitivity.CLASSIFIED))
            .thenReturn(false);

        assertThrows(NotFoundException.class, () -> artefactService.getMetadataByArtefactId(ARTEFACT_ID, USER_ID),
                     VALIDATION_NOT_THROWN_MESSAGE
        );
    }

    @Test
    void testArtefactMetadataFromAzureWhenDoesNotExist() {
        when(artefactRepository.findByArtefactId(any(), any())).thenReturn(Optional.empty());
        assertThrows(
            NotFoundException.class,
            () -> artefactService.getPayloadByArtefactId(ARTEFACT_ID, USER_ID),
            "Not Found exception has not been thrown when artefact does not exist"
        );
    }

    @Test
    void testGetArtefactMetadataForAdmin() {
        when(artefactRepository.findArtefactByArtefactId(ARTEFACT_ID.toString()))
            .thenReturn(Optional.of(artefactWithIdAndPayloadUrl));
        assertEquals(artefactWithIdAndPayloadUrl, artefactService.getMetadataByArtefactId(ARTEFACT_ID),
                     VALIDATION_ARTEFACT_NOT_MATCH);
    }

    @Test
    void testGetArtefactMetadataForAdminThrows() {
        when(artefactRepository.findArtefactByArtefactId(ARTEFACT_ID.toString())).thenReturn(Optional.empty());
        NotFoundException ex = assertThrows(NotFoundException.class, () ->
                                                artefactService.getMetadataByArtefactId(ARTEFACT_ID),
                                            "Not found exception should be thrown"
        );
        assertEquals("No artefact found with the ID: " + ARTEFACT_ID, ex.getMessage(),
                     MESSAGES_MATCH);
    }

    @Test
    void testGetArtefactMetadataCallsNonAdmin() {
        when(artefactRepository.findByArtefactId(any(), any()))
            .thenReturn(Optional.of(artefactWithIdAndPayloadUrl));
        assertEquals(artefactWithIdAndPayloadUrl, artefactService.getMetadataByArtefactId(ARTEFACT_ID, USER_ID),
                     VALIDATION_ARTEFACT_NOT_MATCH);
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
                                       artefactService.getLocationType(listType),
                                       LOCATION_TYPE_MATCH));
    }

    @Test
    void testGetLocationTypeOwningHearingLocation() {
        List<ListType> venueListTypes = new ArrayList<>();
        venueListTypes.add(ListType.ET_DAILY_LIST);
        venueListTypes.add(ListType.ET_FORTNIGHTLY_PRESS_LIST);

        venueListTypes.forEach(listType ->
                                   assertEquals(LocationType.OWNING_HEARING_LOCATION,
                                                artefactService.getLocationType(listType),
                                                LOCATION_TYPE_MATCH));
    }

    @Test
    void testGetLocationTypeNational() {
        List<ListType> nationalListTypes = new ArrayList<>();
        nationalListTypes.add(ListType.SJP_PRESS_LIST);
        nationalListTypes.add(ListType.SJP_PUBLIC_LIST);
        nationalListTypes.add(ListType.CARE_STANDARDS_LIST);
        nationalListTypes.add(ListType.PRIMARY_HEALTH_LIST);

        nationalListTypes.forEach(listType ->
                                      assertEquals(LocationType.NATIONAL,
                                                   artefactService.getLocationType(listType),
                                                   LOCATION_TYPE_MATCH));

    }

    @Test
    void testArtefactCountService() {
        List<LocationArtefact> artefactsPerLocations = new ArrayList<>();
        artefactsPerLocations.add(new LocationArtefact("1", 3));
        artefactsPerLocations.add(new LocationArtefact("noMatch", 0));
        List<Object[]> result = new ArrayList<>();
        result.add(new Object[]{"1", "3"});
        when(artefactRepository.countArtefactsByLocation()).thenReturn(result);
        assertEquals(artefactsPerLocations, artefactService.countArtefactsByLocation(),
                     MESSAGES_MATCH);
    }

    @Test
    void testFindAllNoMatchArtefacts() {
        when(artefactRepository.findAllNoMatchArtefacts()).thenReturn(List.of(artefact));

        assertEquals(List.of(artefact), artefactService.findAllNoMatchArtefacts(), MESSAGES_MATCH);
    }

}
