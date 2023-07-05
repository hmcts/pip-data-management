package uk.gov.hmcts.reform.pip.data.management.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;
import org.testcontainers.shaded.org.apache.commons.io.IOUtils;
import uk.gov.hmcts.reform.pip.data.management.database.ArtefactRepository;
import uk.gov.hmcts.reform.pip.data.management.database.LocationRepository;
import uk.gov.hmcts.reform.pip.data.management.errorhandling.exceptions.CsvParseException;
import uk.gov.hmcts.reform.pip.data.management.errorhandling.exceptions.LocationNotFoundException;
import uk.gov.hmcts.reform.pip.data.management.models.location.Location;
import uk.gov.hmcts.reform.pip.data.management.models.location.LocationDeletion;
import uk.gov.hmcts.reform.pip.data.management.models.location.LocationReference;
import uk.gov.hmcts.reform.pip.data.management.models.publication.Artefact;
import uk.gov.hmcts.reform.pip.model.account.AzureAccount;
import uk.gov.hmcts.reform.pip.model.location.LocationCsv;
import uk.gov.hmcts.reform.pip.model.location.LocationType;
import uk.gov.hmcts.reform.pip.model.system.admin.ActionResult;
import uk.gov.hmcts.reform.pip.model.system.admin.ChangeType;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("PMD.ExcessiveImports")
class LocationServiceTest {

    @Mock
    private LocationRepository locationRepository;

    @Mock
    private ArtefactRepository artefactRepository;

    @Mock
    private SubscriptionManagementService subscriptionManagementService;

    @Mock
    private AccountManagementService accountManagementService;

    @Mock
    private PublicationServicesService publicationService;

    @InjectMocks
    private LocationService locationService;

    Location locationFirstExample;
    Location locationSecondExample;
    LocationDeletion locationDeletion;
    AzureAccount azureAccount;

    private static final String FAMILY_LOCATION =  "Family Location";
    private static final String MAGISTRATES_LOCATION = "Magistrates Location";
    private static final String FAMILY_LOCATION_WELSH =  "Lleoliad Teulu";
    private static final String MAGISTRATES_LOCATION_WELSH = "Lleoliad yr Ynadon";
    private static final String ENGLISH_LANGUAGE = "eng";
    private static final String WELSH_LANGUAGE = "cy";
    private static final Integer LOCATION_ID = 123;
    private static final String LOCATION_NAME = "TEST_PIP_1234_Court123";
    private static final String LOCATION_NAME_PREFIX = "TEST_PIP_1234_";

    private static final String FIRST_LOCATION_NOT_FOUND = "First location has not been found";
    private static final String SECOND_LOCATION_NOT_FOUND = "Second location has not been found";
    private static final String REQUESTER_NAME = "ReqName";
    private static final String EMAIL = "test@test.com";
    private static final String FILE = "file";
    private static final String FILE_NAME = "TestFileName";
    private static final String FILE_TYPE = "text/plain";

    @BeforeEach
    void setup() {
        LocationCsv locationCsvFirstExample = new LocationCsv();
        locationCsvFirstExample.setLocationName("Venue Name First Example");
        locationCsvFirstExample.setProvenanceLocationType("venue");
        locationCsvFirstExample.setWelshLocationName("Welsh Venue name first example");
        locationFirstExample = new Location(locationCsvFirstExample);
        locationFirstExample.setLocationId(1);

        LocationCsv locationCsvSecondExample = new LocationCsv();
        locationCsvSecondExample.setLocationName("Venue Name Second Example");
        locationCsvSecondExample.setProvenanceLocationType("venue");
        locationCsvSecondExample.setWelshLocationName("Welsh Venue name second example");
        locationSecondExample = new Location(locationCsvSecondExample);

        locationDeletion = new LocationDeletion();

        azureAccount = new AzureAccount();
        azureAccount.setDisplayName("ReqName");
    }

    @Test
    void testGetAllLocationsCallsTheLocationRepository() {
        when(locationRepository.findAll()).thenReturn(List.of(locationFirstExample, locationSecondExample));
        List<Location> returnedLocations = locationService.getAllLocations();

        assertTrue(
            returnedLocations.contains(locationFirstExample),
            "First example location not contained in first array");

        assertTrue(
            returnedLocations.contains(locationSecondExample),
            "First example location not contained in first array");
    }

    @Test
    void testHandleLocationIdSearchReturnsLocation() {
        when(locationRepository.getLocationByLocationId(locationFirstExample.getLocationId()))
            .thenReturn(Optional.of(locationFirstExample));

        Location location = locationService.getLocationById(locationFirstExample.getLocationId());

        assertEquals(location, locationFirstExample, "Unknown location has been returned");
    }

    @Test
    void testHandleSearchLocationIdThrowsLocationNotFoundException() {
        int unknownId = 1234;

        LocationNotFoundException locationNotFoundException = assertThrows(LocationNotFoundException.class, () ->
            locationService.getLocationById(unknownId), "Expected LocationNotFoundException to be thrown"
        );

        assertTrue(
            locationNotFoundException.getMessage().contains(String.valueOf(unknownId)),
            "Location not found exception does not contain the expected uuid");
    }

    @Test
    void testHandleLocationNameSearchReturnsLocation() {
        when(locationRepository.getLocationByName(locationFirstExample.getName()))
            .thenReturn(Optional.of(locationFirstExample));

        Location location = locationService.getLocationByName(locationFirstExample.getName(),
                                                              ENGLISH_LANGUAGE);

        assertEquals(location, locationFirstExample, "Unknown location has been returned");
    }

    @Test
    void testHandleWelshLocationNameSearchReturnsLocation() {
        when(locationRepository.getLocationByWelshName(locationFirstExample.getName()))
            .thenReturn(Optional.of(locationFirstExample));

        Location location = locationService.getLocationByName(locationFirstExample.getName(),
                                                              WELSH_LANGUAGE);

        assertEquals(location, locationFirstExample, "Unknown location has been returned");
    }

    @Test
    void testHandleSearchLocationNameThrowsLocationNotFoundException() {
        String unknownName = "UnknownName";

        LocationNotFoundException locationNotFoundException = assertThrows(LocationNotFoundException.class, () ->
            locationService.getLocationByName(unknownName, ENGLISH_LANGUAGE),
            "Expected LocationNotFoundException to be thrown"
        );

        assertTrue(
            locationNotFoundException.getMessage().contains(unknownName),
            "Location not found exception does not contain the expected name");
    }

    @Test
    void testHandleLocationSearchByRegionAndJurisdiction() {

        List<String> regions = List.of("North West", "South West");
        List<String> jurisdictions = List.of(MAGISTRATES_LOCATION, FAMILY_LOCATION);

        String expectedJurisdictions = MAGISTRATES_LOCATION + "," + FAMILY_LOCATION;
        String expectedRegions = "North West,South West";

        when(locationRepository.findByRegionAndJurisdictionOrderByName(expectedRegions, expectedJurisdictions))
            .thenReturn(List.of(locationFirstExample, locationSecondExample));

        List<Location> returnedLocations = locationService.searchByRegionAndJurisdiction(regions, jurisdictions,
                                                                                         ENGLISH_LANGUAGE);

        assertTrue(
            returnedLocations.contains(locationFirstExample),
            FIRST_LOCATION_NOT_FOUND);

        assertTrue(
            returnedLocations.contains(locationSecondExample),
            SECOND_LOCATION_NOT_FOUND);
    }

    @Test
    void testHandleLocationSearchByRegionAndJurisdictionForWelsh() {

        List<String> regions = List.of("Gogledd Orllewin", "De Orllewin");
        List<String> jurisdictions = List.of(MAGISTRATES_LOCATION_WELSH, FAMILY_LOCATION_WELSH);

        String expectedJurisdictions = MAGISTRATES_LOCATION_WELSH + "," + FAMILY_LOCATION_WELSH;
        String expectedRegions = "Gogledd Orllewin,De Orllewin";

        when(locationRepository.findByWelshRegionAndJurisdictionOrderByName(expectedRegions, expectedJurisdictions))
            .thenReturn(List.of(locationFirstExample, locationSecondExample));

        List<Location> returnedLocations = locationService.searchByRegionAndJurisdiction(regions, jurisdictions,
                                                                                         WELSH_LANGUAGE);

        assertTrue(
            returnedLocations.contains(locationFirstExample),
            FIRST_LOCATION_NOT_FOUND);

        assertTrue(
            returnedLocations.contains(locationSecondExample),
            SECOND_LOCATION_NOT_FOUND);
    }

    @Test
    void testHandleLocationSearchOnlyRegion() {
        List<String> regions = List.of("North West", "South West");

        String expectedRegions = "North West,South West";

        when(locationRepository.findByRegionAndJurisdictionOrderByName(expectedRegions, ""))
            .thenReturn(List.of(locationFirstExample));

        List<Location> returnedLocations = locationService.searchByRegionAndJurisdiction(regions, null,
                                                                                         ENGLISH_LANGUAGE);

        assertTrue(
            returnedLocations.contains(locationFirstExample),
            FIRST_LOCATION_NOT_FOUND);
    }

    @Test
    void testHandleLocationSearchOnlyRegionForWelsh() {
        List<String> regions = List.of("Gogledd Orllewin", "De Orllewin");

        String expectedRegions = "Gogledd Orllewin,De Orllewin";

        when(locationRepository.findByWelshRegionAndJurisdictionOrderByName(expectedRegions, ""))
            .thenReturn(List.of(locationFirstExample));

        List<Location> returnedLocations = locationService.searchByRegionAndJurisdiction(regions, null,
                                                                                         WELSH_LANGUAGE);

        assertTrue(
            returnedLocations.contains(locationFirstExample),
            FIRST_LOCATION_NOT_FOUND);
    }

    @Test
    void testHandleLocationSearchOnlyJurisdiction() {
        List<String> jurisdictions = List.of(MAGISTRATES_LOCATION, FAMILY_LOCATION);

        String expectedJurisdictions = MAGISTRATES_LOCATION + "," + FAMILY_LOCATION;

        when(locationRepository.findByRegionAndJurisdictionOrderByName("", expectedJurisdictions))
            .thenReturn(List.of(locationSecondExample));

        List<Location> returnedLocations = locationService.searchByRegionAndJurisdiction(null, jurisdictions,
                                                                                         ENGLISH_LANGUAGE);

        assertTrue(
            returnedLocations.contains(locationSecondExample),
            SECOND_LOCATION_NOT_FOUND);
    }

    @Test
    void testHandleLocationSearchOnlyJurisdictionForWelsh() {
        List<String> jurisdictions = List.of(MAGISTRATES_LOCATION_WELSH, FAMILY_LOCATION_WELSH);

        String expectedJurisdictions = MAGISTRATES_LOCATION_WELSH + "," + FAMILY_LOCATION_WELSH;

        when(locationRepository.findByWelshRegionAndJurisdictionOrderByName("", expectedJurisdictions))
            .thenReturn(List.of(locationSecondExample));

        List<Location> returnedLocations = locationService.searchByRegionAndJurisdiction(null, jurisdictions,
                                                                                         WELSH_LANGUAGE);

        assertTrue(
            returnedLocations.contains(locationSecondExample),
            SECOND_LOCATION_NOT_FOUND);
    }

    @Test
    void testHandleLocationSearchNoRegionOrJurisdiction() {
        when(locationRepository.findByRegionAndJurisdictionOrderByName("", ""))
            .thenReturn(List.of(locationFirstExample, locationSecondExample));

        List<Location> returnedLocations = locationService.searchByRegionAndJurisdiction(null, null,
                                                                                         ENGLISH_LANGUAGE);

        assertTrue(
            returnedLocations.contains(locationFirstExample),
            FIRST_LOCATION_NOT_FOUND);

        assertTrue(
            returnedLocations.contains(locationSecondExample),
            SECOND_LOCATION_NOT_FOUND);
    }

    @Test
    void testHandleLocationSearchNoRegionOrJurisdictionForWelsh() {
        when(locationRepository.findByWelshRegionAndJurisdictionOrderByName("", ""))
            .thenReturn(List.of(locationFirstExample, locationSecondExample));

        List<Location> returnedLocations = locationService.searchByRegionAndJurisdiction(null, null,
                                                                                         WELSH_LANGUAGE);

        assertTrue(
            returnedLocations.contains(locationFirstExample),
            FIRST_LOCATION_NOT_FOUND);

        assertTrue(
            returnedLocations.contains(locationSecondExample),
            SECOND_LOCATION_NOT_FOUND);
    }

    @Test
    void testHandleUploadLocationsOk() throws IOException {
        when(locationRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        try (InputStream inputStream = this.getClass().getClassLoader()
            .getResourceAsStream("csv/ValidCsv.csv")) {

            MultipartFile multipartFile = new MockMultipartFile(FILE,
                                                                FILE_NAME,
                                                                FILE_TYPE,
                                                                IOUtils.toByteArray(inputStream)
            );

            List<Location> locations = new ArrayList<>(locationService.uploadLocations(multipartFile));

            assertEquals(2, locations.size(), "Unknown number of locations returned from parser");

            Location firstLocation = locations.get(0);
            assertEquals(1, firstLocation.getLocationId(), "Location ID is not as expected");
            assertEquals("Test Location", firstLocation.getName(), "Location name does not match in first location");
            assertEquals(List.of("North West"), firstLocation.getRegion(),
                         "Location region does not match in first location");
            List<String> firstLocationJurisdiction = firstLocation.getJurisdiction();
            assertEquals(2, firstLocationJurisdiction.size(), "Unexpected number of jurisdictions");
            assertTrue(firstLocationJurisdiction.contains(MAGISTRATES_LOCATION),
                       "Jurisdiction does not have expected value");
            assertTrue(firstLocationJurisdiction.contains(FAMILY_LOCATION),
                       "Jurisdiction does not have expected value");
            assertEquals("test1@test.com", firstLocation.getEmail(), "Location email is not as expected");
            assertEquals("0123456789", firstLocation.getContactNo(),
                         "Location contact no does not match in first location");

            Location secondLocation = locations.get(1);
            assertEquals(2, secondLocation.getLocationId(), "Location ID is not as expected");
            assertEquals("Test Location Other", secondLocation.getName(),
                         "Location name does not match in second location");
            assertEquals(List.of("South West"), secondLocation.getRegion(),
                         "Location region does not match in second location");
            List<String> secondLocationJurisdiction = secondLocation.getJurisdiction();
            assertEquals(1, secondLocationJurisdiction.size(), "Unexpected number of jurisdictions");
            assertTrue(firstLocationJurisdiction.contains(FAMILY_LOCATION),
                       "Jurisdiction does not have expected value");
            assertEquals("test2@test.com", secondLocation.getEmail(), "Location email is not as expected");
            assertEquals("01111111111", secondLocation.getContactNo(),
                         "Location contact no does not match in first location");
        }
    }

    @Test
    void testHandleUploadReferencesOk() throws IOException {
        when(locationRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        try (InputStream inputStream = this.getClass().getClassLoader()
            .getResourceAsStream("csv/ValidCsv.csv")) {


            MultipartFile multipartFile = new MockMultipartFile(FILE, FILE_NAME,
                                                                FILE_TYPE, IOUtils.toByteArray(inputStream)
            );

            List<Location> locations = new ArrayList<>(locationService.uploadLocations(multipartFile));

            assertEquals(2, locations.size(), "Unknown number of locations returned from parser");

            Location firstLocation = locations.get(0);
            List<LocationReference> firstLocationReferences = firstLocation.getLocationReferenceList();
            assertEquals(2, firstLocationReferences.size(), "Unknown number of references for first location");
            LocationReference firstLocationReferenceOne = firstLocationReferences.get(0);
            assertEquals("TestProvenance", firstLocationReferenceOne.getProvenance(), "Provenance is not as expected");
            assertEquals("1", firstLocationReferenceOne.getProvenanceLocationId(), "Provenance ID is not as expected");
            LocationReference firstLocationReferenceTwo = firstLocationReferences.get(1);
            assertEquals(
                "TestProvenanceOther",
                firstLocationReferenceTwo.getProvenance(),
                "Provenance is not as expected"
            );
            assertEquals("2", firstLocationReferenceTwo.getProvenanceLocationId(), "Provenance ID is not as expected");
            assertEquals(LocationType.VENUE, firstLocationReferenceTwo.getProvenanceLocationType(),
                         "Provenance Location ID is not as expected");


            Location secondLocation = locations.get(1);
            List<LocationReference> secondLocationReferences = secondLocation.getLocationReferenceList();
            assertEquals(1, secondLocationReferences.size(), "Unknown number of references for second location");
            LocationReference secondLocationReferenceOne = secondLocationReferences.get(0);
            assertEquals("TestProvenance", secondLocationReferenceOne.getProvenance(), "Provenance is not as expected");
            assertEquals("1", secondLocationReferenceOne.getProvenanceLocationId(), "Provenance ID is not as expected");
            assertEquals(LocationType.VENUE, secondLocationReferenceOne.getProvenanceLocationType(),
                         "Provenance Location ID is not as expected");
        }

    }

    @Test
    void testHandleUploadInvalidCsv() throws IOException {

        try (InputStream inputStream = this.getClass().getClassLoader()
            .getResourceAsStream("csv/InvalidCsv.txt")) {


            MultipartFile multipartFile = new MockMultipartFile(FILE, FILE_NAME,
                                                                FILE_TYPE, IOUtils.toByteArray(inputStream)
            );


            assertThrows(CsvParseException.class, () -> locationService.uploadLocations(multipartFile));
        }
    }

    @Test
    void testHandleUploadInvalidCsvWithNoLocationType() throws IOException {

        try (InputStream inputStream = this.getClass().getClassLoader()
            .getResourceAsStream("csv/InvalidNoLocationType.csv")) {


            MultipartFile multipartFile = new MockMultipartFile(FILE, FILE_NAME,
                                                                FILE_TYPE, IOUtils.toByteArray(inputStream)
            );


            assertThrows(CsvParseException.class, () -> locationService.uploadLocations(multipartFile));
        }
    }

    @Test
    void testCreateLocation() {
        assertThat(locationService.createLocation(LOCATION_ID, LOCATION_NAME))
            .as("Location created message does not match")
            .isEqualTo(
                "Location with ID " + LOCATION_ID + " and name " + LOCATION_NAME + " created successfully"
            );
        verify(locationRepository).save(any());
    }

    @Test
    void testDeleteAllLocationsWithNamePrefix() {
        Integer id1 = 1;
        Integer id2 = 2;

        Location location1 = new Location();
        location1.setLocationId(id1);
        location1.setName(LOCATION_NAME_PREFIX + 1);

        Location location2 = new Location();
        location2.setLocationId(id2);
        location2.setName(LOCATION_NAME_PREFIX + 2);

        when(locationRepository.findAllByNameStartingWithIgnoreCase(LOCATION_NAME_PREFIX))
            .thenReturn(List.of(location1, location2));

        assertThat(locationService.deleteAllLocationsWithNamePrefix(LOCATION_NAME_PREFIX))
            .as("Location deleted message does not match")
            .isEqualTo("2 location(s) deleted with name starting with " + LOCATION_NAME_PREFIX);

        verify(locationRepository).deleteByLocationIdIn(List.of(id1, id2));
    }

    @Test
    void testDeleteAllLocationsWithNamePrefixWhenLocationNotFound() {
        when(locationRepository.findAllByNameStartingWithIgnoreCase(LOCATION_NAME_PREFIX))
            .thenReturn(Collections.emptyList());

        assertThat(locationService.deleteAllLocationsWithNamePrefix(LOCATION_NAME_PREFIX))
            .as("Location deleted message does not match")
            .isEqualTo("0 location(s) deleted with name starting with " + LOCATION_NAME_PREFIX);

        verify(locationRepository, never()).deleteByLocationIdIn(anyList());
    }

    @Test
    void testDeleteLocation() throws JsonProcessingException {
        Integer locationId = 1;
        locationFirstExample.setName("NAME");

        when(locationRepository.getLocationByLocationId(locationId))
            .thenReturn(Optional.of(locationFirstExample));
        when(accountManagementService.getUserInfo(any()))
            .thenReturn(azureAccount);
        when(artefactRepository.findActiveArtefactsForLocation(any(), eq(locationId.toString())))
            .thenReturn(List.of());
        when(subscriptionManagementService.findSubscriptionsByLocationId(locationId.toString()))
            .thenReturn("[]");
        when(accountManagementService.getAllAccounts("PI_AAD", "SYSTEM_ADMIN"))
            .thenReturn(List.of(EMAIL));

        doNothing().when(locationRepository).deleteById(locationId);

        locationService.deleteLocation(locationId, REQUESTER_NAME);

        verify(locationRepository, times(1)).deleteById(locationId);
    }

    @Test
    void testDeleteLocationWhenArtefactFound() throws JsonProcessingException {
        Integer locationId = 1;

        when(locationRepository.getLocationByLocationId(locationId))
            .thenReturn(Optional.of(locationFirstExample));
        when(artefactRepository.findActiveArtefactsForLocation(any(), eq(locationId.toString())))
            .thenReturn(List.of(new Artefact()));
        when(accountManagementService.getAllAccounts("PI_AAD", "SYSTEM_ADMIN"))
            .thenReturn(List.of(EMAIL));
        when(accountManagementService.getUserInfo(any()))
            .thenReturn(azureAccount);
        when(publicationService.sendSystemAdminEmail(List.of(EMAIL), REQUESTER_NAME,
            ActionResult.ATTEMPTED,
"There are active artefacts for following location: Venue Name First Example",
             ChangeType.DELETE_LOCATION))
            .thenReturn("");

        LocationDeletion result = locationService.deleteLocation(locationId, REQUESTER_NAME);
        assertTrue(result.isExists(), "Found active artefact for a court");
    }

    @Test
    void testDeleteLocationWhenSubscriptionFound() throws JsonProcessingException {
        Integer locationId = 1;

        when(locationRepository.getLocationByLocationId(locationId))
            .thenReturn(Optional.of(locationFirstExample));
        when(accountManagementService.getUserInfo(any()))
            .thenReturn(azureAccount);
        when(artefactRepository.findActiveArtefactsForLocation(any(), eq(locationId.toString())))
            .thenReturn(List.of());
        when(subscriptionManagementService.findSubscriptionsByLocationId(locationId.toString()))
            .thenReturn("[{},{}]");
        when(accountManagementService.getAllAccounts("PI_AAD", "SYSTEM_ADMIN"))
            .thenReturn(List.of(EMAIL));
        when(publicationService.sendSystemAdminEmail(List.of(EMAIL), REQUESTER_NAME,
            ActionResult.ATTEMPTED,
"There are active subscriptions for the following location: Venue Name First Example",
            ChangeType.DELETE_LOCATION))
            .thenReturn("");

        LocationDeletion result = locationService.deleteLocation(locationId, REQUESTER_NAME);

        assertTrue(result.isExists(), "Found active subscription for a court");
    }

    @Test
    void testDeleteLocationWhereNotFound() {
        int locationId = 1;

        when(locationRepository.getLocationByLocationId(locationId))
            .thenReturn(Optional.empty());

        LocationNotFoundException locationNotFoundException =
            assertThrows(LocationNotFoundException.class, () ->
                locationService.deleteLocation(locationId, REQUESTER_NAME));

        assertEquals("No location found with the id: 1", locationNotFoundException.getMessage(),
                     "Exception does not contain expected message");
    }

    @Test
    void testDownloadLocations() throws IOException {
        when(locationRepository.findAll()).thenReturn(List.of(locationFirstExample));

        byte[] response = locationService.downloadLocations();

        assertNotNull(response, "byte array response was null");
        assertTrue(response.length > 1, "byte array size less than 1");
    }
}

