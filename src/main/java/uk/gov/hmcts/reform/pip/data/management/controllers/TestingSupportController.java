package uk.gov.hmcts.reform.pip.data.management.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.pip.data.management.service.LocationService;
import uk.gov.hmcts.reform.pip.data.management.service.artefact.ArtefactDeleteService;
import uk.gov.hmcts.reform.pip.model.authentication.roles.IsAdmin;

@RestController
@Tag(name = "Data Management Testing Support API")
@RequestMapping("/testing-support")
@IsAdmin
@ConditionalOnProperty(prefix = "testingSupport", name = "enableApi", havingValue = "true")
@SuppressWarnings("PMD.TestClassWithoutTestCases")
public class TestingSupportController {
    private static final String UNAUTHORIZED_DESCRIPTION = "User has not been authorized";

    private static final String OK_CODE = "200";
    private static final String CREATED_CODE = "201";
    private static final String AUTH_ERROR_CODE = "403";

    @Autowired
    private ArtefactDeleteService artefactDeleteService;

    @Autowired
    private LocationService locationService;

    @ApiResponse(responseCode = CREATED_CODE,
        description = "Location with ID {locationId} and name {locationName} created successfully")
    @ApiResponse(responseCode = AUTH_ERROR_CODE, description = UNAUTHORIZED_DESCRIPTION)
    @Operation(summary = "Create location")
    @PostMapping("location/{locationId}")
    public ResponseEntity<String> createLocation(@PathVariable Integer locationId, @RequestBody String locationName) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(locationService.createLocation(locationId, locationName));
    }

    @ApiResponse(responseCode = OK_CODE,
        description = "Location(s) deleted with name starting with {locationNamePrefix}")
    @ApiResponse(responseCode = AUTH_ERROR_CODE, description = UNAUTHORIZED_DESCRIPTION)
    @Operation(summary = "Delete all locations with location name prefix")
    @DeleteMapping("location/{locationNamePrefix}")
    @Transactional
    public ResponseEntity<String> deleteLocationsByNamePrefix(@PathVariable String locationNamePrefix) {
        return ResponseEntity.ok(locationService.deleteAllLocationsWithNamePrefix(locationNamePrefix));
    }

    @ApiResponse(responseCode = OK_CODE,
        description = "Artefact(s) deleted for location name starting with {locationNamePrefix}")
    @ApiResponse(responseCode = AUTH_ERROR_CODE, description = UNAUTHORIZED_DESCRIPTION)
    @Operation(summary = "Delete all artefacts with location name prefix")
    @DeleteMapping("publication/{locationNamePrefix}")
    @Transactional
    public ResponseEntity<String> deletePublicationsWithLocationNamePrefix(@PathVariable String locationNamePrefix) {
        return ResponseEntity.ok(artefactDeleteService.deleteAllArtefactsWithLocationNamePrefix(locationNamePrefix));
    }
}
