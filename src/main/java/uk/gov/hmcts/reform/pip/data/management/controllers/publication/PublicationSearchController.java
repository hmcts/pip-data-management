package uk.gov.hmcts.reform.pip.data.management.controllers.publication;

import com.fasterxml.jackson.annotation.JsonView;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.pip.data.management.models.publication.Artefact;
import uk.gov.hmcts.reform.pip.data.management.models.publication.views.ArtefactView;
import uk.gov.hmcts.reform.pip.data.management.service.publication.PublicationSearchService;
import uk.gov.hmcts.reform.pip.data.management.utils.CaseSearchTerm;

import java.util.List;
import java.util.UUID;

@Slf4j
@Validated
@RestController
@Tag(name = "Data Management - API for filtering and searching publications")
@RequestMapping("/publication")
public class PublicationSearchController {
    private static final String USER_ID_HEADER = "x-user-id";
    private static final String ADMIN_HEADER = "x-admin";

    private static final String NOT_FOUND_DESCRIPTION =
        "No artefact found matching given parameters and date requirements";
    private static final String UNAUTHORISED_MESSAGE = "Invalid access credential";
    private static final String FORBIDDEN_MESSAGE = "User has not been authorized";

    private static final String OK_CODE = "200";
    private static final String NOT_FOUND_CODE = "404";
    private static final String UNAUTHORISED_CODE = "401";
    private static final String FORBIDDEN_CODE = "403";

    private static final String BEARER_AUTHENTICATION = "bearerAuth";
    private static final String DEFAULT_ADMIN_VALUE = "false";
    private static final String REQUESTER_ID_HEADER = "x-requester-id";

    private final PublicationSearchService publicationSearchService;

    @Autowired
    public PublicationSearchController(PublicationSearchService publicationSearchService) {
        this.publicationSearchService = publicationSearchService;
    }

    @ApiResponse(responseCode = OK_CODE, description = "List of Artefacts matching"
        + " a given case value, verification parameters and date requirements")
    @ApiResponse(responseCode = UNAUTHORISED_CODE, description = UNAUTHORISED_MESSAGE)
    @ApiResponse(responseCode = FORBIDDEN_CODE, description = FORBIDDEN_MESSAGE)
    @ApiResponse(responseCode = NOT_FOUND_CODE, description = NOT_FOUND_DESCRIPTION)
    @Operation(summary = "Get a series of publications matching a given case search value (e.g. "
        + "CASE_URN/CASE_ID/CASE_NAME)")
    @GetMapping("/search/{searchTerm}/{searchValue}")
    @JsonView(ArtefactView.Internal.class)
    @SecurityRequirement(name = BEARER_AUTHENTICATION)
    @PreAuthorize("@authorisationService.userCanSearchInPublicationData()")
    public ResponseEntity<List<Artefact>> getAllRelevantArtefactsBySearchValue(
        @PathVariable CaseSearchTerm searchTerm, @PathVariable String searchValue,
        @RequestHeader(value = REQUESTER_ID_HEADER, required = false) UUID requesterId) {
        return ResponseEntity.ok(publicationSearchService.findAllBySearch(searchTerm, searchValue, requesterId));
    }

    @ApiResponse(responseCode = OK_CODE, description = "List of Artefacts matching the given locationId and "
        + "verification parameters and date requirements")
    @ApiResponse(responseCode = UNAUTHORISED_CODE, description = UNAUTHORISED_MESSAGE)
    @ApiResponse(responseCode = FORBIDDEN_CODE, description = FORBIDDEN_MESSAGE)
    @ApiResponse(responseCode = NOT_FOUND_CODE, description = NOT_FOUND_DESCRIPTION)
    @Operation(summary = "Get a series of publications matching a given locationId (e.g. locationId)")
    @GetMapping("/locationId/{locationId}")
    @JsonView(ArtefactView.Internal.class)
    @SecurityRequirement(name = BEARER_AUTHENTICATION)
    @PreAuthorize("@authorisationService.userCanSearchPublicationForLocation()")
    public ResponseEntity<List<Artefact>> getAllRelevantArtefactsByLocationId(
        @PathVariable String locationId,
        @RequestHeader(value = REQUESTER_ID_HEADER, required = false) UUID requesterId,
        @RequestHeader(value = ADMIN_HEADER, defaultValue = DEFAULT_ADMIN_VALUE, required = false) Boolean isAdmin) {
        return ResponseEntity.ok(publicationSearchService.findAllByLocationIdAdmin(locationId, requesterId, isAdmin));
    }
}
