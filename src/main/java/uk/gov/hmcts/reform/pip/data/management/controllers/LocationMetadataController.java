package uk.gov.hmcts.reform.pip.data.management.controllers;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.pip.data.management.models.location.LocationMetadata;
import uk.gov.hmcts.reform.pip.data.management.service.LocationMetadataService;
import uk.gov.hmcts.reform.pip.model.authentication.roles.IsAdmin;

@RestController
@RequestMapping("/location/metadata")
public class LocationMetadataController {
    private static final String BEARER_AUTHENTICATION = "bearerAuth";

    private final LocationMetadataService locationMetadataService;

    @Autowired
    public LocationMetadataController(LocationMetadataService locationMetadataService) {
        this.locationMetadataService = locationMetadataService;
    }

    @GetMapping("/{locationId}")
    @IsAdmin
    @SecurityRequirement(name = BEARER_AUTHENTICATION)
    public ResponseEntity<LocationMetadata> getLocationMetadataByLocationId(@PathVariable Integer locationId) {
        return ResponseEntity.ok(locationMetadataService.getLocationMetadataByLocationId(locationId));
    }
}
