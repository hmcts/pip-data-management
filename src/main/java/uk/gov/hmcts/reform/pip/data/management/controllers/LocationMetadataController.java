package uk.gov.hmcts.reform.pip.data.management.controllers;

import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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
@Tag(name = "Data Management location metadata list API")
@RequestMapping("/location-metadata")
public class LocationMetadataController {
    private static final String OK_CODE = "200";
    private static final String CREATED_CODE = "201";
    private static final String BAD_REQUEST_CODE = "400";
    private static final String UNAUTHORISED_CODE = "401";
    private static final String FORBIDDEN_CODE = "403";
    private static final String NOT_FOUND_CODE = "404";
    private static final String CONFLICT_CODE = "409";

    private static final String UNAUTHORISED_MESSAGE = "Invalid access credential";
    private static final String FORBIDDEN_MESSAGE = "User has not been authorized";
    private static final String BEARER_AUTHENTICATION = "bearerAuth";

    private static final String REQUESTER_ID_HEADER = "x-requester-id";

    private final LocationMetadataService locationMetadataService;

    public LocationMetadataController(LocationMetadataService locationMetadataService) {
        this.locationMetadataService = locationMetadataService;
    }

    @PostMapping
    @ApiResponse(responseCode = CREATED_CODE, description = "Add Location metadata")
    @ApiResponse(responseCode = BAD_REQUEST_CODE, description = "Unable to add the location metadata")
    @ApiResponse(responseCode = UNAUTHORISED_CODE, description = UNAUTHORISED_MESSAGE)
    @ApiResponse(responseCode = FORBIDDEN_CODE, description = FORBIDDEN_MESSAGE)
    @ApiResponse(responseCode = CONFLICT_CODE, description = "Location metadata already exists")
    @SecurityRequirement(name = BEARER_AUTHENTICATION)
    @PreAuthorize("@authorisationService.userCanAddLocationMetadata(#requesterId)")
    public ResponseEntity<String> addLocationMetaData(
        @RequestHeader(REQUESTER_ID_HEADER) String requesterId,
        @RequestBody LocationMetadata locationMetadata) {
        locationMetadataService.createLocationMetadata(locationMetadata, requesterId);
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(String.format(
                "Location metadata successfully added by user %s",
                requesterId
            ));
    }

    @PutMapping("/{id}")
    @ApiResponse(responseCode = OK_CODE, description = "Update Location metadata")
    @ApiResponse(responseCode = BAD_REQUEST_CODE, description = "Unable to update the location metadata")
    @ApiResponse(responseCode = UNAUTHORISED_CODE, description = UNAUTHORISED_MESSAGE)
    @ApiResponse(responseCode = FORBIDDEN_CODE, description = FORBIDDEN_MESSAGE)
    @SecurityRequirement(name = BEARER_AUTHENTICATION)
    @PreAuthorize("@authorisationService.userCanUpdateLocationMetadata(#requesterId)")
    public ResponseEntity<String> updateLocationMetaData(
        @PathVariable String id,
        @RequestHeader(REQUESTER_ID_HEADER) String requesterId,
        @RequestBody LocationMetadata locationMetadata) {
        locationMetadataService.updateLocationMetadata(locationMetadata, id, requesterId);
        return ResponseEntity.status(HttpStatus.OK)
            .body(String.format(
                "Location metadata successfully updated by user %s",
                requesterId
            ));
    }

    @DeleteMapping("/{id}")
    @ApiResponse(responseCode = OK_CODE, description = "Delete Location metadata")
    @ApiResponse(responseCode = BAD_REQUEST_CODE, description = "Unable to delete the location metadata")
    @ApiResponse(responseCode = UNAUTHORISED_CODE, description = UNAUTHORISED_MESSAGE)
    @ApiResponse(responseCode = FORBIDDEN_CODE, description = FORBIDDEN_MESSAGE)
    @SecurityRequirement(name = BEARER_AUTHENTICATION)
    @PreAuthorize("@authorisationService.userCanDeleteLocationMetadata(#requesterId)")
    public ResponseEntity<String> deleteLocationMetaData(
        @RequestHeader(REQUESTER_ID_HEADER) String requesterId,
        @PathVariable String id) {
        locationMetadataService.deleteById(id, requesterId);
        return ResponseEntity.status(HttpStatus.OK)
            .body(String.format(
                "Location metadata successfully deleted by user %s",
                requesterId
            ));
    }

    @GetMapping("/location/{locationId}")
    @ApiResponse(responseCode = OK_CODE, description = "Get Locations metadata By Location Id")
    @ApiResponse(responseCode = NOT_FOUND_CODE, description = "No Location metadata found "
        + "with the location id {locationId}")
    @ApiResponse(responseCode = UNAUTHORISED_CODE, description = UNAUTHORISED_MESSAGE)
    @SecurityRequirement(name = BEARER_AUTHENTICATION)
    @IsAdmin
    public ResponseEntity<LocationMetadata> getLocationMetaDataByLocationId(
        @PathVariable String locationId) {
        return ResponseEntity.ok(locationMetadataService.getLocationById(locationId));
    }
}
