package uk.gov.hmcts.reform.pip.data.management.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;
import org.testcontainers.shaded.org.apache.commons.io.IOUtils;
import uk.gov.hmcts.reform.pip.data.management.database.LocationRepository;
import uk.gov.hmcts.reform.pip.data.management.errorhandling.exceptions.CsvParseException;
import uk.gov.hmcts.reform.pip.data.management.errorhandling.exceptions.LocationNotFoundException;
import uk.gov.hmcts.reform.pip.data.management.models.location.Location;
import uk.gov.hmcts.reform.pip.data.management.models.location.LocationCsv;
import uk.gov.hmcts.reform.pip.data.management.models.location.LocationReference;
import uk.gov.hmcts.reform.pip.data.management.models.location.LocationType;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LocationServiceTest {

    @Mock
    private LocationRepository locationRepository;

    @InjectMocks
    private LocationService locationService;

    Location locationFirstExample;
    Location locationSecondExample;

    private static final String FAMILY_LOCATION =  "Family Location";
    private static final String MAGISTRATES_LOCATION = "Magistrates Location";
    private static final String FAMILY_LOCATION_WELSH =  "Lleoliad Teulu";
    private static final String MAGISTRATES_LOCATION_WELSH = "Lleoliad yr Ynadon";
    private static final String ENGLISH_LANGUAGE = "eng";
    private static final String WELSH_LANGUAGE = "cy";

    private static final String FIRST_LOCATION_NOT_FOUND = "First location has not been found";
    private static final String SECOND_LOCATION_NOT_FOUND = "Second location has not been found";

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

            MultipartFile multipartFile = new MockMultipartFile("file",
                                                                "TestFileName",
                                                                "text/plain",
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
        }
    }

    @Test
    void testHandleUploadReferencesOk() throws IOException {
        when(locationRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        try (InputStream inputStream = this.getClass().getClassLoader()
            .getResourceAsStream("csv/ValidCsv.csv")) {


            MultipartFile multipartFile = new MockMultipartFile("file", "TestFileName",
                                                                "text/plain", IOUtils.toByteArray(inputStream)
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


            MultipartFile multipartFile = new MockMultipartFile("file", "TestFileName",
                                                                "text/plain", IOUtils.toByteArray(inputStream)
            );


            assertThrows(CsvParseException.class, () -> locationService.uploadLocations(multipartFile));
        }
    }

    @Test
    void testDeleteLocation() {
        int locationId = 1;

        when(locationRepository.getLocationByLocationId(locationId))
            .thenReturn(Optional.of(locationFirstExample));

        doNothing().when(locationRepository).deleteById(locationId);

        locationService.deleteLocation(locationId);

        verify(locationRepository, times(1)).deleteById(locationId);
    }

    @Test
    void testDeleteLocationWhereNotFound() {
        int locationId = 1;

        when(locationRepository.getLocationByLocationId(locationId))
            .thenReturn(Optional.empty());

        LocationNotFoundException locationNotFoundException =
            assertThrows(LocationNotFoundException.class, () -> locationService.deleteLocation(locationId));

        assertEquals("No location found with the id: 1", locationNotFoundException.getMessage(),
                     "Exception does not contain expected message");
    }

}

