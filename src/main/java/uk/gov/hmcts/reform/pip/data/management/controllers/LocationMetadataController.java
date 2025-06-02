package uk.gov.hmcts.reform.pip.data.management.controllers;

import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.pip.data.management.models.location.LocationMetadata;
import uk.gov.hmcts.reform.pip.data.management.service.LocationMetadataService;
import uk.gov.hmcts.reform.pip.model.authentication.roles.IsAdmin;

@RestController
@Tag(name = "Data Management location list API")
@RequestMapping("/location-metadata")
public class LocationMetadataController {
    private static final String OK_CODE = "200";
    private static final String CREATED_CODE = "201";
    private static final String BAD_REQUEST_CODE = "400";
    private static final String UNAUTHORISED_CODE = "401";
    private static final String FORBIDDEN_CODE = "403";
    private static final String NOT_FOUND_CODE = "404";

    private static final String UNAUTHORISED_MESSAGE = "Invalid access credential";
    private static final String FORBIDDEN_MESSAGE = "User has not been authorized";
    private static final String BEARER_AUTHENTICATION = "bearerAuth";

    private final LocationMetadataService locationMetadataService;

    @Autowired
    public LocationMetadataController(LocationMetadataService locationMetadataService) {
        this.locationMetadataService = locationMetadataService;
    }

    @PostMapping
    @ApiResponse(responseCode = CREATED_CODE, description = "Add Location metadata")
    @ApiResponse(responseCode = BAD_REQUEST_CODE, description = "Unable to add the location metadata")
    @ApiResponse(responseCode = UNAUTHORISED_CODE, description = UNAUTHORISED_MESSAGE)
    @ApiResponse(responseCode = FORBIDDEN_CODE, description = FORBIDDEN_MESSAGE)
    @IsAdmin
    @SecurityRequirement(name = BEARER_AUTHENTICATION)
    public ResponseEntity<String> addLocationMetaData(@RequestHeader("x-requester-id") String userId,
                                                      @RequestBody LocationMetadata locationMetadata) {
        locationMetadataService.createLocationMetadata(locationMetadata, userId);
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(String.format(
                "Location metadata successfully added by user %s",
                userId
            ));
    }

    @PutMapping
    @ApiResponse(responseCode = OK_CODE, description = "Update Location metadata")
    @ApiResponse(responseCode = BAD_REQUEST_CODE, description = "Unable to update the location metadata")
    @ApiResponse(responseCode = UNAUTHORISED_CODE, description = UNAUTHORISED_MESSAGE)
    @ApiResponse(responseCode = FORBIDDEN_CODE, description = FORBIDDEN_MESSAGE)
    @IsAdmin
    @SecurityRequirement(name = BEARER_AUTHENTICATION)
    public ResponseEntity<String> updateLocationMetaData(@RequestHeader("x-requester-id") String userId,
                                                      @RequestBody LocationMetadata locationMetadata) {
        locationMetadataService.updateLocationMetadata(locationMetadata, userId);
        return ResponseEntity.status(HttpStatus.OK)
            .body(String.format(
                "Location metadata successfully updated by user %s",
                userId
            ));
    }

    @DeleteMapping("/{id}")
    @ApiResponse(responseCode = OK_CODE, description = "Delete Location metadata")
    @ApiResponse(responseCode = BAD_REQUEST_CODE, description = "Unable to delete the location metadata")
    @ApiResponse(responseCode = UNAUTHORISED_CODE, description = UNAUTHORISED_MESSAGE)
    @ApiResponse(responseCode = FORBIDDEN_CODE, description = FORBIDDEN_MESSAGE)
    @IsAdmin
    @SecurityRequirement(name = BEARER_AUTHENTICATION)
    public ResponseEntity<String> deleteLocationMetaData(@RequestHeader("x-requester-id") String userId,
                                                         @PathVariable String id) {
        locationMetadataService.deleteById(id, userId);
        return ResponseEntity.status(HttpStatus.OK)
            .body(String.format(
                "Location metadata successfully deleted by user %s",
                userId
            ));
    }

    @GetMapping("/{id}")
    @ApiResponse(responseCode = OK_CODE, description = "Get Locations metadata")
    @ApiResponse(responseCode = NOT_FOUND_CODE, description = "No Location metadata found with the id {id}")
    @ApiResponse(responseCode = UNAUTHORISED_CODE, description = UNAUTHORISED_MESSAGE)
    @SecurityRequirement(name = BEARER_AUTHENTICATION)
    public ResponseEntity<LocationMetadata> getLocationMetaData(@PathVariable String id) {
        return ResponseEntity.ok(locationMetadataService.getById(id));
    }

    @GetMapping("search-by-location-id/{locationId}")
    @ApiResponse(responseCode = OK_CODE, description = "Get Locations metadata")
    @ApiResponse(responseCode = NOT_FOUND_CODE, description = "No Location metadata found "
        + "with the location id {locationId}")
    @ApiResponse(responseCode = UNAUTHORISED_CODE, description = UNAUTHORISED_MESSAGE)
    @SecurityRequirement(name = BEARER_AUTHENTICATION)
    public ResponseEntity<LocationMetadata> getLocationMetaDataByLocationId(@PathVariable String locationId) {
        return ResponseEntity.ok(locationMetadataService.getLocationById(locationId));
    }
}
