package uk.gov.hmcts.reform.pip.data.management.controllers.publication;

import com.fasterxml.jackson.annotation.JsonView;
import com.fasterxml.jackson.core.JsonProcessingException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.pip.data.management.models.location.LocationArtefact;
import uk.gov.hmcts.reform.pip.data.management.models.publication.Artefact;
import uk.gov.hmcts.reform.pip.data.management.models.publication.views.ArtefactView;
import uk.gov.hmcts.reform.pip.data.management.service.publication.PublicationLocationService;
import uk.gov.hmcts.reform.pip.model.location.LocationType;
import uk.gov.hmcts.reform.pip.model.publication.ListType;

import java.util.List;

@Slf4j
@Validated
@RestController
@Tag(name = "Data Management - API for handling publications based on locations")
@RequestMapping("/publication")
public class PublicationLocationController {
    private static final String UNAUTHORISED_MESSAGE = "Invalid access credential";
    private static final String FORBIDDEN_MESSAGE = "User has not been authorized";

    private static final String OK_CODE = "200";
    private static final String NOT_FOUND_CODE = "404";
    private static final String UNAUTHORISED_CODE = "401";
    private static final String FORBIDDEN_CODE = "403";

    private static final String BEARER_AUTHENTICATION = "bearerAuth";
    private static final String REQUESTER_ID_HEADER = "x-requester-id";

    private final PublicationLocationService publicationLocationService;

    @Autowired
    public PublicationLocationController(PublicationLocationService publicationLocationService) {
        this.publicationLocationService = publicationLocationService;
    }

    @ApiResponse(responseCode = OK_CODE, description = "Data Management - Artefact count per location - request "
        + "accepted.")
    @ApiResponse(responseCode = UNAUTHORISED_CODE, description = UNAUTHORISED_MESSAGE)
    @ApiResponse(responseCode = FORBIDDEN_CODE, description = FORBIDDEN_MESSAGE)
    @Operation(summary = "Return a count of artefacts per location")
    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/count-by-location")
    @SecurityRequirement(name = BEARER_AUTHENTICATION)
    @PreAuthorize("@authorisationService.userCanGetPublicationsPerLocation(#requesterId)")
    public ResponseEntity<List<LocationArtefact>> countByLocation(
        @RequestHeader(REQUESTER_ID_HEADER) String requesterId
    ) {
        return ResponseEntity.ok(publicationLocationService.countArtefactsByLocation());
    }

    @ApiResponse(responseCode = OK_CODE, description = "{Location type associated with given list type}")
    @Operation(summary = "Return the Location type associated with a given list type")
    @GetMapping("/location-type/{listType}")
    public ResponseEntity<LocationType> getLocationType(@PathVariable ListType listType) {
        return ResponseEntity.ok(publicationLocationService.getLocationType(listType));
    }

    @ApiResponse(responseCode = OK_CODE, description = "List of all artefacts that are noMatch in their id")
    @ApiResponse(responseCode = UNAUTHORISED_CODE, description = UNAUTHORISED_MESSAGE)
    @ApiResponse(responseCode = FORBIDDEN_CODE, description = FORBIDDEN_MESSAGE)
    @Operation(summary = "Get all no match publications")
    @GetMapping("/no-match")
    @JsonView(ArtefactView.Internal.class)
    @SecurityRequirement(name = BEARER_AUTHENTICATION)
    @PreAuthorize("@authorisationService.userCanGetAllNoMatchPublications(#requesterId)")
    public ResponseEntity<List<Artefact>> getAllNoMatchArtefacts(
        @RequestHeader(REQUESTER_ID_HEADER) String requesterId
    ) {
        return ResponseEntity.ok(publicationLocationService.findAllNoMatchArtefacts());
    }

    @ApiResponse(responseCode = OK_CODE, description = "Successfully deleted artefact for location: {locationId}")
    @ApiResponse(responseCode = UNAUTHORISED_CODE, description = UNAUTHORISED_MESSAGE)
    @ApiResponse(responseCode = FORBIDDEN_CODE, description = FORBIDDEN_MESSAGE)
    @ApiResponse(responseCode = NOT_FOUND_CODE, description = "No artefact found with the location ID: {locationId}")
    @Operation(summary = "Delete all artefacts for given location from P&I")
    @DeleteMapping("/{locationId}/deleteArtefacts")
    @SecurityRequirement(name = BEARER_AUTHENTICATION)
    @PreAuthorize("@authorisationService.userCanDeletePublicationsByLocation(#requesterId)")
    public ResponseEntity<String> deleteArtefactsByLocation(
        @RequestHeader(REQUESTER_ID_HEADER) String requesterId,
        @PathVariable Integer locationId) throws JsonProcessingException {
        return ResponseEntity.ok(publicationLocationService.deleteArtefactByLocation(locationId, requesterId));
    }
}
