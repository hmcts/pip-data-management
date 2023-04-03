package uk.gov.hmcts.reform.pip.data.management.controllers;

import com.fasterxml.jackson.annotation.JsonView;
import com.fasterxml.jackson.core.JsonProcessingException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import uk.gov.hmcts.reform.pip.data.management.models.location.Location;
import uk.gov.hmcts.reform.pip.data.management.models.location.LocationDeletion;
import uk.gov.hmcts.reform.pip.data.management.models.location.LocationViews;
import uk.gov.hmcts.reform.pip.data.management.service.LocationService;
import uk.gov.hmcts.reform.pip.model.authentication.roles.IsAdmin;

import java.io.IOException;
import java.util.Collection;
import java.util.List;

@RestController
@Tag(name = "Data Management location list API")
@RequestMapping("/locations")
public class LocationController {

    @Autowired
    private LocationService locationService;

    private static final String OK_CODE = "200";
    private static final String BAD_REQUEST_CODE = "400";
    private static final String AUTH_ERROR_CODE = "403";
    private static final String NOT_FOUND_CODE = "404";

    @ApiResponse(responseCode = OK_CODE, description = "All courts returned")
    @Operation(summary = "Get all locations with their hearings")
    @GetMapping(produces = "application/json")
    @JsonView(LocationViews.BaseView.class)
    public ResponseEntity<List<Location>> getLocationList() {
        return new ResponseEntity<>(locationService.getAllLocations(), HttpStatus.OK);
    }

    @ApiResponse(responseCode = OK_CODE, description = "Location found")
    @ApiResponse(responseCode = NOT_FOUND_CODE, description = "No Location found with the id {locationId}")
    @Operation(summary = "Gets a location by searching by the location id and returning")
    @GetMapping("/{locationId}")
    public ResponseEntity<Location> getLocationById(@Parameter(description =
        "The Location Id to retrieve", required = true) @PathVariable Integer locationId) {
        return ResponseEntity.ok(locationService.getLocationById(locationId));

    }

    @ApiResponse(responseCode = OK_CODE, description = "Location found")
    @ApiResponse(responseCode = NOT_FOUND_CODE, description = "No Location found with the search {input}")
    @Operation(summary = "Gets a Location by searching by the Location name and returning")
    @GetMapping("/name/{locationName}/language/{language}")
    public ResponseEntity<Location> getLocationByName(@Parameter(description =
        "The search input to retrieve", required = true) @PathVariable String locationName,
                                                      @PathVariable String language) {
        return ResponseEntity.ok(locationService.getLocationByName(locationName, language));

    }

    @ApiResponse(responseCode = OK_CODE, description = "Filtered Locations")
    @Operation(summary = "Filters list of Locations by region or jurisdiction")
    @GetMapping("/filter")
    @JsonView(LocationViews.BaseView.class)
    public ResponseEntity<List<Location>> searchByRegionAndJurisdiction(
        @RequestParam(required = false) List<String> regions,
        @RequestParam(required = false) List<String> jurisdictions,
        @RequestParam(required = false) String language) {

        return ResponseEntity.ok(locationService.searchByRegionAndJurisdiction(regions,
            jurisdictions, language));
    }

    @ApiResponse(responseCode = OK_CODE, description = "Uploaded Locations")
    @ApiResponse(responseCode = BAD_REQUEST_CODE, description = "Unable to upload the reference data")
    @ApiResponse(responseCode = AUTH_ERROR_CODE, description = "User has not been authorized")
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @IsAdmin
    public ResponseEntity<Collection<Location>> uploadLocations(@RequestPart MultipartFile locationList) {
        return ResponseEntity.ok(locationService.uploadLocations(locationList));
    }

    @ApiResponse(responseCode = OK_CODE, description = "Location with id {locationId} has been deleted")
    @ApiResponse(responseCode = AUTH_ERROR_CODE, description = "User has not been authorized")
    @ApiResponse(responseCode = NOT_FOUND_CODE, description = "No Location found with the id {locationId}")
    @DeleteMapping("/{locationId}")
    @IsAdmin
    public ResponseEntity<LocationDeletion> deleteLocation(
        @RequestHeader("x-provenance-user-id") String provenanceUserId,
        @PathVariable Integer locationId)
        throws JsonProcessingException {
        return ResponseEntity.ok(locationService.deleteLocation(locationId, provenanceUserId));
    }

    @ApiResponse(responseCode = OK_CODE, description = "CSV of the reference data")
    @ApiResponse(responseCode = AUTH_ERROR_CODE, description = "User has not been authorized")
    @GetMapping("/download/csv")
    @IsAdmin
    public ResponseEntity<byte[]> downloadLocations() throws IOException {
        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_OCTET_STREAM_VALUE)
            .body(locationService.downloadLocations());
    }

}
