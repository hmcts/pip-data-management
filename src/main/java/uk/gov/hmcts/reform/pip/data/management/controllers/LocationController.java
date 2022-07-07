package uk.gov.hmcts.reform.pip.data.management.controllers;

import com.fasterxml.jackson.annotation.JsonView;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import uk.gov.hmcts.reform.pip.data.management.models.location.Location;
import uk.gov.hmcts.reform.pip.data.management.models.location.LocationViews;
import uk.gov.hmcts.reform.pip.data.management.service.LocationService;

import java.util.Collection;
import java.util.List;

@RestController
@Api(tags = "Data Management location list API")
@RequestMapping("/locations")
public class LocationController {

    @Autowired
    private LocationService locationService;

    @ApiResponses({
        @ApiResponse(code = 200, message = "All courts returned"),
    })
    @ApiOperation("Get all locations with their hearings")
    @GetMapping(produces = "application/json")
    @JsonView(LocationViews.BaseView.class)
    public ResponseEntity<List<Location>> getLocationList() {
        return new ResponseEntity<>(locationService.getAllLocations(), HttpStatus.OK);
    }

    @ApiResponses({
        @ApiResponse(code = 200, message = "Location found"),
        @ApiResponse(code = 404, message = "No Location found with the id {locationId}")
    })
    @ApiOperation("Gets a location by searching by the location id and returning")
    @GetMapping("/{locationId}")
    public ResponseEntity<Location> getLocationById(@ApiParam(value = "The Location Id to retrieve", required = true)
                                                @PathVariable Integer locationId) {
        return ResponseEntity.ok(locationService.getLocationById(locationId));

    }

    @ApiResponses({
        @ApiResponse(code = 200, message = "Location found"),
        @ApiResponse(code = 404, message = "No Location found with the search {input}")
    })
    @ApiOperation("Gets a Location by searching by the Location name and returning")
    @GetMapping("/name/{locationName}")
    public ResponseEntity<Location> getLocationByName(@ApiParam(value = "The search input to retrieve", required = true)
                                                @PathVariable String locationName) {
        return ResponseEntity.ok(locationService.getLocationByName(locationName));

    }

    @ApiResponses({
        @ApiResponse(code = 200, message = "Filtered Locations")
    })
    @ApiOperation("Filters list of Locations by region or jurisdiction")
    @GetMapping("/filter")
    @JsonView(LocationViews.BaseView.class)
    public ResponseEntity<List<Location>> searchByRegionAndJurisdiction(
        @RequestParam(required = false) List<String> regions,
        @RequestParam(required = false) List<String> jurisdictions,
        @RequestParam(required = false) String language) {

        return ResponseEntity.ok(locationService.searchByRegionAndJurisdiction(regions,
            jurisdictions, language));
    }

    @ApiResponses({
        @ApiResponse(code = 200, message = "Uploaded Locations"),
        @ApiResponse(code = 403, message = "User has not been authorized")
    })
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Collection<Location>> uploadLocations(@RequestPart MultipartFile locationList) {
        return ResponseEntity.ok(locationService.uploadLocations(locationList));
    }

}
