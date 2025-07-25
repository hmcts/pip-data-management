package uk.gov.hmcts.reform.pip.data.management.service.location;

import com.fasterxml.jackson.core.JsonProcessingException;
import nl.altindag.log.LogCaptor;
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
import uk.gov.hmcts.reform.pip.data.management.database.LocationMetadataRepository;
import uk.gov.hmcts.reform.pip.data.management.database.LocationRepository;
import uk.gov.hmcts.reform.pip.data.management.errorhandling.exceptions.ContainsForbiddenValuesException;
import uk.gov.hmcts.reform.pip.data.management.errorhandling.exceptions.CreateLocationConflictException;
import uk.gov.hmcts.reform.pip.data.management.errorhandling.exceptions.CsvParseException;
import uk.gov.hmcts.reform.pip.data.management.errorhandling.exceptions.LocationNameValidationException;
import uk.gov.hmcts.reform.pip.data.management.errorhandling.exceptions.LocationNotFoundException;
import uk.gov.hmcts.reform.pip.data.management.models.location.Location;
import uk.gov.hmcts.reform.pip.data.management.models.location.LocationDeletion;
import uk.gov.hmcts.reform.pip.data.management.models.location.LocationReference;
import uk.gov.hmcts.reform.pip.data.management.models.publication.Artefact;
import uk.gov.hmcts.reform.pip.data.management.service.AccountManagementService;
import uk.gov.hmcts.reform.pip.data.management.service.SystemAdminNotificationService;
import uk.gov.hmcts.reform.pip.data.management.service.ValidationService;
import uk.gov.hmcts.reform.pip.model.account.PiUser;
import uk.gov.hmcts.reform.pip.model.location.LocationCsv;
import uk.gov.hmcts.reform.pip.model.location.LocationType;
import uk.gov.hmcts.reform.pip.model.system.admin.ActionResult;
import uk.gov.hmcts.reform.pip.model.system.admin.ChangeType;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings({"PMD.ExcessiveImports", "PMD.TooManyMethods"})
class LocationServiceTest {

    @Mock
    private LocationRepository locationRepository;

    @Mock
    private LocationMetadataRepository locationMetadataRepository;

    @Mock
    private ArtefactRepository artefactRepository;

    @Mock
    private AccountManagementService accountManagementService;

    @Mock
    private SystemAdminNotificationService systemAdminNotificationService;

    @Mock
    private ValidationService validationService;

    @InjectMocks
    private LocationService locationService;

    Location locationFirstExample;
    Location locationSecondExample;
    Location locationThirdExample;
    Location locationInvalidExample;
    LocationDeletion locationDeletion;
    PiUser piUser;
    String userId;

    private static final String FAMILY_LOCATION =  "Family Location";
    private static final String MAGISTRATES_LOCATION = "Magistrates Location";
    private static final String FAMILY_LOCATION_WELSH =  "Lleoliad Teulu";
    private static final String MAGISTRATES_LOCATION_WELSH = "Lleoliad yr Ynadon";
    private static final String ENGLISH_LANGUAGE = "eng";
    private static final String WELSH_LANGUAGE = "cy";
    private static final Integer LOCATION_ID = 123;
    private static final String LOCATION_NAME = "TEST_PIP_1234_Court123";
    private static final String INVALID_LOCATION_NAME = "Test Location <p>Hello world</p>";
    private static final String LOCATION_NAME_PREFIX = "TEST_PIP_1234_";
    private static final String LOCATION_NAME2 = "Test Location 2";
    private static final String WELSH_LOCATION_NAME2 = "Welsh Test Location 2";
    private static final String LOCATION_NAME3 = "Test Location 3";
    private static final String WELSH_LOCATION_NAME3 = "Welsh Test Location 3";
    private static final String LOCATION_NAME4 = "Test Location 4";
    private static final String VENUE = "venue";

    private static final String FIRST_LOCATION_NOT_FOUND = "First location has not been found";
    private static final String SECOND_LOCATION_NOT_FOUND = "Second location has not been found";
    private static final String CREATE_LOCATION_MESSAGE = "Location created message does not match";
    private static final String ERROR_LOG_MESSAGE = "Error log does not match";
    private static final String EMAIL = "test@test.com";
    private static final String SSO_EMAIL = "sso@test.com";
    private static final String SSO_PROVENANCE = "SSO";
    private static final String PI_AAD_PROVENANCE = "PI_AAD";
    private static final String SYSTEM_ADMIN = "SYSTEM_ADMIN";

    private static final String FILE = "file";
    private static final String FILE_NAME = "TestFileName";
    private static final String FILE_TYPE = "text/plain";

    @BeforeEach
    void setup() {
        LocationCsv locationCsvFirstExample = new LocationCsv();
        locationCsvFirstExample.setLocationName(LOCATION_NAME2);
        locationCsvFirstExample.setProvenanceLocationType(VENUE);
        locationCsvFirstExample.setWelshLocationName(WELSH_LOCATION_NAME2);
        locationCsvFirstExample.setJurisdiction(List.of("Tribunal"));
        locationCsvFirstExample.setWelshJurisdiction(List.of("Tribiwnlys"));
        locationCsvFirstExample.setJurisdictionType(List.of("Social Security and Child Support"));
        locationCsvFirstExample.setWelshJurisdictionType(List.of("Tribiwnlys Nawdd Cymdeithasol a Chynnal Plant"));
        locationFirstExample = new Location(locationCsvFirstExample);
        locationFirstExample.setLocationId(1);

        LocationCsv locationCsvSecondExample = new LocationCsv();
        locationCsvSecondExample.setLocationName(LOCATION_NAME3);
        locationCsvSecondExample.setProvenanceLocationType(VENUE);
        locationCsvSecondExample.setWelshLocationName(WELSH_LOCATION_NAME3);
        locationSecondExample = new Location(locationCsvSecondExample);
        locationSecondExample.setLocationId(3);

        LocationCsv locationCsvThirdExample = new LocationCsv();
        locationCsvThirdExample.setLocationName(LOCATION_NAME4);
        locationCsvThirdExample.setProvenanceLocationType(VENUE);
        locationCsvThirdExample.setWelshLocationName(WELSH_LOCATION_NAME2);
        locationThirdExample = new Location(locationCsvThirdExample);
        locationThirdExample.setLocationId(1);

        locationDeletion = new LocationDeletion();

        userId = UUID.randomUUID().toString();
        piUser = new PiUser();
        piUser.setEmail(EMAIL);
        piUser.setUserId(userId);
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

            Location firstLocation = locations.get(0);

            validationService.containsHtmlTag(firstLocation.getName(), firstLocation.getWelshName());

            assertEquals(2, locations.size(), "Unknown number of locations returned from parser");

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
    void testHandleUploadContainsInvalidCourtName() throws IOException {
        LocationCsv locationCsvInvalidExample = new LocationCsv();
        locationCsvInvalidExample.setLocationName(INVALID_LOCATION_NAME);
        locationCsvInvalidExample.setProvenanceLocationType(VENUE);
        locationCsvInvalidExample.setWelshLocationName(WELSH_LOCATION_NAME2);
        locationInvalidExample = new Location(locationCsvInvalidExample);
        locationInvalidExample.setLocationId(2);

        doNothing().when(validationService).containsHtmlTag(eq(LOCATION_NAME2), any());
        doThrow(ContainsForbiddenValuesException.class).when(validationService)
            .containsHtmlTag(eq(INVALID_LOCATION_NAME), any());

        try (InputStream inputStream = this.getClass().getClassLoader()
            .getResourceAsStream("csv/ValidCsvWithInvalidCourtName.csv");
             LogCaptor logCaptor = LogCaptor.forClass(LocationService.class)) {

            MultipartFile multipartFile = new MockMultipartFile(FILE, FILE_NAME, FILE_TYPE,
                                                                IOUtils.toByteArray(inputStream));

            assertThatThrownBy(() -> locationService.uploadLocations(multipartFile))
                .isInstanceOf(LocationNameValidationException.class)
                .hasMessage("Failed to upload locations. The location name 'Test Location <p>Hello world</p>' or "
                                + "Welsh location name 'Welsh Test Location other' contains a forbidden character");

            assertThat(logCaptor.getErrorLogs())
                .as(ERROR_LOG_MESSAGE)
                .hasSize(1);

            assertThat(logCaptor.getErrorLogs().get(0))
                .as(ERROR_LOG_MESSAGE)
                .contains("Failed to upload locations. The location name 'Test Location <p>Hello world</p>' or "
                              + "Welsh location name 'Welsh Test Location other' contains a forbidden character");

            verify(locationRepository, never()).save(any(Location.class));
        }
    }

    @Test
    void testUploadLocationCsvContainingExistingEnglishLocationName() throws IOException {
        when(locationRepository.findAll()).thenReturn(List.of(locationFirstExample, locationSecondExample));

        try (InputStream inputStream = this.getClass().getClassLoader()
            .getResourceAsStream("csv/ValidCsvWithExistingLocationName.csv");
             LogCaptor logCaptor = LogCaptor.forClass(LocationService.class)) {

            MultipartFile multipartFile = new MockMultipartFile(FILE, FILE_NAME, FILE_TYPE,
                                                                IOUtils.toByteArray(inputStream));

            assertThatThrownBy(() -> locationService.uploadLocations(multipartFile))
                .isInstanceOf(LocationNameValidationException.class)
                .hasMessage("Failed to upload locations. Location name(s) Test Location 2 already exist");

            assertThat(logCaptor.getErrorLogs())
                .as(ERROR_LOG_MESSAGE)
                .hasSize(1);

            assertThat(logCaptor.getErrorLogs().get(0))
                .as(ERROR_LOG_MESSAGE)
                .contains("Failed to upload locations. Location name(s) Test Location 2 already exist");

            verify(locationRepository, never()).save(any(Location.class));
        }
    }

    @Test
    void testUploadLocationCsvContainingExistingWelshLocationName() throws IOException {
        when(locationRepository.findAll()).thenReturn(List.of(locationSecondExample, locationThirdExample));

        try (InputStream inputStream = this.getClass().getClassLoader()
            .getResourceAsStream("csv/ValidCsvWithExistingLocationName.csv");
             LogCaptor logCaptor = LogCaptor.forClass(LocationService.class)) {

            MultipartFile multipartFile = new MockMultipartFile(FILE, FILE_NAME, FILE_TYPE,
                                                                IOUtils.toByteArray(inputStream));

            assertThatThrownBy(() -> locationService.uploadLocations(multipartFile))
                .isInstanceOf(LocationNameValidationException.class)
                .hasMessage("Failed to upload locations. Welsh location name(s) Welsh Test Location 2 already exist");

            assertThat(logCaptor.getErrorLogs())
                .as(ERROR_LOG_MESSAGE)
                .hasSize(1);

            assertThat(logCaptor.getErrorLogs().get(0))
                .as(ERROR_LOG_MESSAGE)
                .contains("Failed to upload locations. Welsh location name(s) Welsh Test Location 2 already exist");

            verify(locationRepository, never()).save(any(Location.class));
        }
    }

    @Test
    void testCreateLocationSuccess() {
        assertThat(locationService.createLocation(LOCATION_ID, LOCATION_NAME))
            .as(CREATE_LOCATION_MESSAGE)
            .isEqualTo(
                "Location with ID " + LOCATION_ID + " and name " + LOCATION_NAME + " created successfully"
            );

        verify(locationRepository).save(any());
    }

    @Test
    void testCreateLocationWithExistingLocationName() {
        when(locationRepository.findAll()).thenReturn(List.of(locationFirstExample));

        assertThatThrownBy(() -> locationService.createLocation(LOCATION_ID, LOCATION_NAME2))
            .as(CREATE_LOCATION_MESSAGE)
            .isInstanceOf(CreateLocationConflictException.class)
            .hasMessage("Location with ID " + LOCATION_ID + " not created. The location name '" + LOCATION_NAME2
                            + "' already exists");
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
        verify(locationMetadataRepository).deleteByLocationIdIn(List.of(id1, id2));
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
        when(accountManagementService.getUserById(userId))
            .thenReturn(piUser);
        when(artefactRepository.findActiveArtefactsForLocation(any(), eq(locationId.toString())))
            .thenReturn(List.of());
        when(accountManagementService.findSubscriptionsByLocationId(locationId.toString()))
            .thenReturn("[]");
        doNothing().when(locationRepository).deleteById(locationId);

        locationService.deleteLocation(locationId, userId);

        verify(locationRepository, times(1)).deleteById(locationId);
        verify(systemAdminNotificationService).sendEmailNotification(
            EMAIL, ActionResult.SUCCEEDED, String.format(
                "Location %s with Id %s has been deleted.",
                "NAME", locationId),
            ChangeType.DELETE_LOCATION
        );
    }

    @Test
    void testDeleteLocationWhenArtefactFound() throws JsonProcessingException {
        Integer locationId = 1;

        when(locationRepository.getLocationByLocationId(locationId))
            .thenReturn(Optional.of(locationFirstExample));
        when(artefactRepository.findActiveArtefactsForLocation(any(), eq(locationId.toString())))
            .thenReturn(List.of(new Artefact()));
        when(accountManagementService.getUserById(userId))
            .thenReturn(piUser);

        LocationDeletion result = locationService.deleteLocation(locationId, userId);
        assertTrue(result.isExists(), "Found active artefact for a court");
        verify(systemAdminNotificationService).sendEmailNotification(
            EMAIL, ActionResult.ATTEMPTED,
            String.format("There are active artefacts for following location: %s", LOCATION_NAME2),
            ChangeType.DELETE_LOCATION
        );
    }

    @Test
    void testDeleteLocationWhenSubscriptionFound() throws JsonProcessingException {
        Integer locationId = 1;

        when(locationRepository.getLocationByLocationId(locationId))
            .thenReturn(Optional.of(locationFirstExample));
        when(accountManagementService.getUserById(userId))
            .thenReturn(piUser);
        when(artefactRepository.findActiveArtefactsForLocation(any(), eq(locationId.toString())))
            .thenReturn(List.of());
        when(accountManagementService.findSubscriptionsByLocationId(locationId.toString()))
            .thenReturn("[{},{}]");

        LocationDeletion result = locationService.deleteLocation(locationId, userId);

        assertTrue(result.isExists(), "Found active subscription for a court");

        verify(systemAdminNotificationService).sendEmailNotification(
            EMAIL, ActionResult.ATTEMPTED,
            String.format("There are active subscriptions for the following location: %s", LOCATION_NAME2),
            ChangeType.DELETE_LOCATION
        );
    }

    @Test
    void testDeleteLocationWhereNotFound() {
        int locationId = 1;

        when(locationRepository.getLocationByLocationId(locationId))
            .thenReturn(Optional.empty());

        LocationNotFoundException locationNotFoundException =
            assertThrows(LocationNotFoundException.class, () ->
                locationService.deleteLocation(locationId, UUID.randomUUID().toString()));

        assertEquals("No location found with the id: 1", locationNotFoundException.getMessage(),
                     "Exception does not contain expected message");
    }

    @Test
    void testDownloadLocations() throws IOException {
        when(locationRepository.findAll()).thenReturn(List.of(locationFirstExample));

        byte[] response = locationService.downloadLocations();

        assertNotNull(response, "byte array response was null");
        assertTrue(response.length > 1, "byte array size less than 1");

        String result = new String(response, StandardCharsets.UTF_8);
        assertThat(result).contains("Tribunal");
        assertThat(result).contains("Tribiwnlys");
        assertThat(result).contains("Social Security and Child Support");
        assertThat(result).contains("Tribiwnlys Nawdd Cymdeithasol a Chynnal Plant");
    }
}

