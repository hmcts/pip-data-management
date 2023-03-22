package uk.gov.hmcts.reform.pip.data.management.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.zonky.test.db.AutoConfigureEmbeddedDatabase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.multipart.MultipartFile;
import org.testcontainers.shaded.org.apache.commons.io.IOUtils;
import uk.gov.hmcts.reform.pip.data.management.Application;
import uk.gov.hmcts.reform.pip.data.management.config.AzureBlobConfigurationTestConfiguration;
import uk.gov.hmcts.reform.pip.data.management.models.location.Location;
import uk.gov.hmcts.reform.pip.data.management.models.location.LocationDeletion;
import uk.gov.hmcts.reform.pip.data.management.service.LocationService;
import uk.gov.hmcts.reform.pip.model.location.LocationCsv;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@SpringBootTest(classes = {Application.class, AzureBlobConfigurationTestConfiguration.class})
@ActiveProfiles(profiles = "test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@AutoConfigureEmbeddedDatabase(type = AutoConfigureEmbeddedDatabase.DatabaseType.POSTGRES)
class LocationControllerTest {

    @Mock
    private LocationService locationService;

    @InjectMocks
    private LocationController locationController;

    private static final String ENGLISH_LANGUAGE = "eng";
    private static final String WELSH_LANGUAGE = "cy";

    private static final String FIRST_LOCATION_NOT_FOUND = "First location not contained within list";
    private static final String SECOND_LOCATION_NOT_FOUND = "Second location not contained within list";

    Location firstLocation;
    Location secondLocation;

    @BeforeEach
    void setup() {

        LocationCsv firstLocationCsv = new LocationCsv();
        firstLocationCsv.setLocationName("Venue Name A");
        firstLocationCsv.setProvenance("Venue Provenance A");
        firstLocationCsv.setRegion(List.of("Venue Region A"));
        firstLocationCsv.setProvenanceLocationType("venue");
        firstLocationCsv.setWelshLocationName("Welsh location A");
        firstLocationCsv.setWelshJurisdiction(List.of("Welsh jurisdiction A"));
        firstLocationCsv.setWelshRegion(List.of("Welsh region A"));
        firstLocation = new Location(firstLocationCsv);

        LocationCsv secondLocationCsv = new LocationCsv();
        secondLocationCsv.setLocationName("Venue Name B");
        secondLocationCsv.setProvenance("Venue Provenance B");
        secondLocationCsv.setRegion(List.of("Venue Region B"));
        secondLocationCsv.setProvenanceLocationType("venue");
        secondLocationCsv.setWelshLocationName("Welsh location B");
        secondLocationCsv.setWelshJurisdiction(List.of("Welsh jurisdiction B"));
        secondLocationCsv.setWelshRegion(List.of("Welsh region B"));
        secondLocation = new Location(secondLocationCsv);
    }

    @Test
    void testGetLocationListReturnsAllLocations() {

        when(locationService.getAllLocations()).thenReturn(List.of(firstLocation, secondLocation));

        List<Location> returnedLocations = locationController.getLocationList().getBody();

        assertEquals(2, returnedLocations.size(), "Unexpected number of locations returned");
        assertTrue(returnedLocations.contains(firstLocation), FIRST_LOCATION_NOT_FOUND);
        assertTrue(returnedLocations.contains(secondLocation), SECOND_LOCATION_NOT_FOUND);
    }

    @Test
    void testGetLocationListReturnsOk() {
        when(locationService.getAllLocations()).thenReturn(List.of(firstLocation, secondLocation));
        assertEquals(HttpStatus.OK, locationController.getLocationList().getStatusCode(),
                     "Location list has not returned OK");
    }

    @Test
    void testGetLocationIdReturnsLocation() {
        int id = 1;
        when(locationService.getLocationById(id)).thenReturn(firstLocation);

        assertEquals(firstLocation, locationController.getLocationById(id).getBody(),
                     "Returned location does not match expected location"
        );
    }

    @Test
    void testGetLocationIdReturnsOK() {
        int id = 1;
        when(locationService.getLocationById(id)).thenReturn(firstLocation);

        assertEquals(HttpStatus.OK, locationController.getLocationById(id).getStatusCode(),
                     "Location ID search has not returned OK"
        );
    }

    @Test
    void testGetLocationByNameReturnsLocation() {
        when(locationService.getLocationByName(secondLocation.getName(), ENGLISH_LANGUAGE))
            .thenReturn(secondLocation);

        assertEquals(secondLocation, locationController.getLocationByName(
            secondLocation.getName(), ENGLISH_LANGUAGE).getBody(),
                     "Returned location does not match expected location"
        );
    }

    @Test
    void testGetWelshLocationByNameReturnsLocation() {
        when(locationService.getLocationByName(secondLocation.getName(), WELSH_LANGUAGE))
            .thenReturn(secondLocation);

        assertEquals(secondLocation, locationController.getLocationByName(
            secondLocation.getName(), WELSH_LANGUAGE).getBody(),
                     "Returned location does not match expected location"
        );
    }

    @Test
    void testGetLocationByNameReturnsOk() {
        when(locationService.getLocationByName(secondLocation.getName(), ENGLISH_LANGUAGE))
            .thenReturn(secondLocation);

        assertEquals(HttpStatus.OK, locationController.getLocationByName(
            secondLocation.getName(), ENGLISH_LANGUAGE).getStatusCode(),
                     "Location Name search has not returned OK"
        );
    }

    @Test
    void testGetWelshLocationByNameReturnsOk() {
        when(locationService.getLocationByName(secondLocation.getName(), WELSH_LANGUAGE))
            .thenReturn(secondLocation);

        assertEquals(HttpStatus.OK, locationController.getLocationByName(
            secondLocation.getName(), WELSH_LANGUAGE).getStatusCode(),
                     "Location Name search has not returned OK"
        );
    }

    @Test
    void testGetFilterLocationsReturnsLocations() {
        List<String> regions = List.of("Region A", "Region B");
        List<String> jurisdictions = List.of("Jurisdiction A", "Jurisdiction B");

        when(locationService.searchByRegionAndJurisdiction(regions, jurisdictions, ENGLISH_LANGUAGE))
            .thenReturn(List.of(firstLocation, secondLocation));

        List<Location> locations = locationController.searchByRegionAndJurisdiction(regions,
                jurisdictions, ENGLISH_LANGUAGE).getBody();


        assertEquals(2, locations.size(), "Unexpected number of locations have been returned");
        assertTrue(locations.contains(firstLocation), FIRST_LOCATION_NOT_FOUND);
        assertTrue(locations.contains(secondLocation), SECOND_LOCATION_NOT_FOUND);
    }

    @Test
    void testGetWelshFilterLocationsReturnsLocations() {
        List<String> regions = List.of("Rhanbarth A", "Rhanbarth B");
        List<String> jurisdictions = List.of("Awdurdodaeth A", "Awdurdodaeth B");

        when(locationService.searchByRegionAndJurisdiction(regions, jurisdictions, WELSH_LANGUAGE))
            .thenReturn(List.of(firstLocation, secondLocation));

        List<Location> locations = locationController.searchByRegionAndJurisdiction(regions,
            jurisdictions, WELSH_LANGUAGE).getBody();


        assertEquals(2, locations.size(), "Unexpected number of locations have been returned");
        assertTrue(locations.contains(firstLocation), FIRST_LOCATION_NOT_FOUND);
        assertTrue(locations.contains(secondLocation), SECOND_LOCATION_NOT_FOUND);
    }

    @Test
    void testGetByRegionAndJurisdictionReturnsOk() {
        List<String> regions = List.of("Region A", "Region B");
        List<String> jurisdictions = List.of("Jurisdiction A", "Jurisdiction B");

        when(locationService.searchByRegionAndJurisdiction(regions, jurisdictions, ENGLISH_LANGUAGE))
            .thenReturn(List.of(firstLocation, secondLocation));

        assertEquals(HttpStatus.OK,
                     locationController.searchByRegionAndJurisdiction(regions, jurisdictions,
                     ENGLISH_LANGUAGE).getStatusCode(),
                     "Location region and jurisdiction search has not returned OK");
    }

    @Test
    void testGetByWelshRegionAndJurisdictionReturnsOk() {
        List<String> regions = List.of("Rhanbarth A", "Rhanbarth B");
        List<String> jurisdictions = List.of("Awdurdodaeth A", "Awdurdodaeth B");

        when(locationService.searchByRegionAndJurisdiction(regions, jurisdictions, WELSH_LANGUAGE))
            .thenReturn(List.of(firstLocation, secondLocation));

        assertEquals(HttpStatus.OK,
                     locationController.searchByRegionAndJurisdiction(regions, jurisdictions,
                                                                      WELSH_LANGUAGE).getStatusCode(),
                     "Location region and jurisdiction search has not returned OK");
    }

    @Test
    void testUploadLocationsReturnsNewLocations() throws IOException {

        try (InputStream inputStream = this.getClass().getClassLoader()
            .getResourceAsStream("csv/ValidCsv.csv")) {

            MultipartFile multipartFile = new MockMultipartFile("file",
                                                                "TestFileName", "text/plain",
                                                                IOUtils.toByteArray(inputStream));

            when(locationService.uploadLocations(multipartFile)).thenReturn(List.of(firstLocation, secondLocation));

            List<Location> returnedLocations = new ArrayList<>(locationController.uploadLocations(multipartFile)
                                                                   .getBody());

            assertEquals(2, returnedLocations.size(), "Unexpected number of location have been returned");
            assertTrue(returnedLocations.contains(firstLocation), FIRST_LOCATION_NOT_FOUND);
            assertTrue(returnedLocations.contains(secondLocation), SECOND_LOCATION_NOT_FOUND);
        }

    }

    @Test
    void testUploadLocationsReturnsOk() throws IOException {

        try (InputStream inputStream = this.getClass().getClassLoader()
            .getResourceAsStream("csv/ValidCsv.csv")) {

            MultipartFile multipartFile = new MockMultipartFile("file",
                                                                "TestFileName", "text/plain",
                                                                IOUtils.toByteArray(inputStream));

            when(locationService.uploadLocations(multipartFile)).thenReturn(List.of(firstLocation, secondLocation));

            assertEquals(HttpStatus.OK, locationController.uploadLocations(multipartFile).getStatusCode(),
                         "Upload locations endpoint has not returned OK");
        }

    }

    @Test
    void testDeleteLocationReturnsOk() throws JsonProcessingException {
        int locationId = 1;
        String requesterName = "ReqName";
        LocationDeletion locationDeletion = new LocationDeletion();
        when(locationService.deleteLocation(locationId, requesterName)).thenReturn(locationDeletion);

        assertEquals(HttpStatus.OK, locationController.deleteLocation(requesterName, locationId).getStatusCode(),
                     "Delete location endpoint has not returned OK");
    }

    @Test
    void testDownloadLocations() throws IOException {
        byte[] testByte = new byte[10];
        when(locationService.downloadLocations()).thenReturn(testByte);

        ResponseEntity<byte[]> controllerResponse = locationController.downloadLocations();
        assertEquals(HttpStatus.OK, controllerResponse.getStatusCode(), "Status did not match expected");
        assertEquals(testByte, controllerResponse.getBody(), "Body did not match expected");
    }

}
