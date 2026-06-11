package uk.gov.hmcts.reform.pip.data.management.controllers.publication;

import com.fasterxml.jackson.annotation.JsonView;
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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.pip.data.management.models.publication.Artefact;
import uk.gov.hmcts.reform.pip.data.management.models.publication.ListSearchConfig;
import uk.gov.hmcts.reform.pip.data.management.models.publication.views.ArtefactView;
import uk.gov.hmcts.reform.pip.data.management.service.publication.PublicationSearchService;
import uk.gov.hmcts.reform.pip.data.management.utils.CaseSearchTerm;
import uk.gov.hmcts.reform.pip.model.publication.ListType;

import java.util.List;
import java.util.UUID;

@Slf4j
@Validated
@RestController
@Tag(name = "Data Management - API for filtering and searching publications as well as management of the search "
    + "configurations for list types")
@RequestMapping("/publication")
@SecurityRequirement(name = "bearerAuth")
public class PublicationSearchController {
    private static final String ADMIN_HEADER = "x-admin";

    private static final String NOT_FOUND_DESCRIPTION =
        "No artefact found matching given parameters and date requirements";
    private static final String UNAUTHORISED_MESSAGE = "Invalid access credential";
    private static final String FORBIDDEN_MESSAGE = "User has not been authorized";

    private static final String OK_CODE = "200";
    private static final String CREATED_CODE = "201";
    private static final String BAD_REQUEST_CODE = "400";
    private static final String NOT_FOUND_CODE = "404";
    private static final String UNAUTHORISED_CODE = "401";
    private static final String FORBIDDEN_CODE = "403";
    private static final String CONFLICT_CODE = "409";

    private static final String DEFAULT_ADMIN_VALUE = "false";
    private static final String REQUESTER_ID_HEADER = "x-requester-id";

    private final PublicationSearchService publicationSearchService;

    @Autowired
    public PublicationSearchController(PublicationSearchService publicationSearchService) {
        this.publicationSearchService = publicationSearchService;
    }

    @ApiResponse(responseCode = CREATED_CODE, description = "List search config created successfully")
    @ApiResponse(responseCode = BAD_REQUEST_CODE, description = "Unable to create list search config")
    @ApiResponse(responseCode = CONFLICT_CODE, description = "List search config already exists")
    @ApiResponse(responseCode = UNAUTHORISED_CODE, description = UNAUTHORISED_MESSAGE)
    @ApiResponse(responseCode = FORBIDDEN_CODE, description = FORBIDDEN_MESSAGE)
    @PostMapping("/search/config")
    @PreAuthorize("@authorisationService.userCanAccessListSearchConfig(#requesterId)")
    public ResponseEntity<String> createListSearchConfig(//NOSONAR
        @RequestBody ListSearchConfig listSearchConfig,
        @RequestHeader(REQUESTER_ID_HEADER) UUID requesterId) {
        publicationSearchService.createListSearchConfig(listSearchConfig, requesterId);
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(String.format(
                "List search config successfully added by user %s",
                requesterId
            ));
    }

    @ApiResponse(responseCode = OK_CODE, description = "List search config updated successfully")
    @ApiResponse(responseCode = BAD_REQUEST_CODE, description = "Unable to update list search config")
    @ApiResponse(responseCode = NOT_FOUND_CODE, description = "No list search config found with id: {id}")
    @ApiResponse(responseCode = UNAUTHORISED_CODE, description = UNAUTHORISED_MESSAGE)
    @ApiResponse(responseCode = FORBIDDEN_CODE, description = FORBIDDEN_MESSAGE)
    @PutMapping("/search/config/{id}")
    @PreAuthorize("@authorisationService.userCanAccessListSearchConfig(#requesterId)")
    public ResponseEntity<String> updateListSearchConfig(//NOSONAR
        @PathVariable String id,
        @RequestBody ListSearchConfig listSearchConfig,
        @RequestHeader(REQUESTER_ID_HEADER) UUID requesterId) {
        publicationSearchService.updateListSearchConfig(id, listSearchConfig, requesterId);
        return ResponseEntity.status(HttpStatus.OK)
            .body(String.format(
                "List search config successfully updated by user %s",
                requesterId
            ));
    }

    @ApiResponse(responseCode = OK_CODE, description = "List search config deleted successfully")
    @ApiResponse(responseCode = NOT_FOUND_CODE, description = "No list search config found with id: {id}")
    @ApiResponse(responseCode = UNAUTHORISED_CODE, description = UNAUTHORISED_MESSAGE)
    @ApiResponse(responseCode = FORBIDDEN_CODE, description = FORBIDDEN_MESSAGE)
    @DeleteMapping("/search/config/{id}")
    @PreAuthorize("@authorisationService.userCanAccessListSearchConfig(#requesterId)")
    public ResponseEntity<String> deleteListSearchConfig(
        @PathVariable String id,
        @RequestHeader(REQUESTER_ID_HEADER) UUID requesterId) {
        publicationSearchService.deleteListSearchConfig(id, requesterId);
        return ResponseEntity.status(HttpStatus.OK)
            .body(String.format(
                "List search config successfully deleted by user %s",
                requesterId
            ));
    }

    @ApiResponse(responseCode = OK_CODE, description = "List search config retrieved successfully")
    @ApiResponse(responseCode = BAD_REQUEST_CODE, description = "Unable to retrieve list search config")
    @ApiResponse(responseCode = NOT_FOUND_CODE, description = "No list search config found with list type: {listType}")
    @ApiResponse(responseCode = UNAUTHORISED_CODE, description = UNAUTHORISED_MESSAGE)
    @ApiResponse(responseCode = FORBIDDEN_CODE, description = FORBIDDEN_MESSAGE)
    @GetMapping("/search/config/{listType}")
    @PreAuthorize("@authorisationService.userCanAccessListSearchConfig(#requesterId)")
    public ResponseEntity<ListSearchConfig> getListSearchConfigByListType(
        @PathVariable ListType listType,
        @RequestHeader(REQUESTER_ID_HEADER) UUID requesterId) {
        return ResponseEntity.ok(publicationSearchService.findListSearchConfigByListType(listType));
    }

    @ApiResponse(responseCode = OK_CODE, description = "List of Artefacts matching"
        + " a given case value, verification parameters and date requirements")
    @ApiResponse(responseCode = UNAUTHORISED_CODE, description = UNAUTHORISED_MESSAGE)
    @ApiResponse(responseCode = FORBIDDEN_CODE, description = FORBIDDEN_MESSAGE)
    @ApiResponse(responseCode = NOT_FOUND_CODE, description = NOT_FOUND_DESCRIPTION)
    @Operation(summary = "Get a series of publications matching a given case search value (e.g. "
            + "CASE_URN/CASE_ID/CASE_NAME)")
    @GetMapping("/search")
    @JsonView(ArtefactView.Internal.class)
    @PreAuthorize("@authorisationService.userCanSearchInPublicationData(#requesterId)")
    public ResponseEntity<List<Artefact>> getAllRelevantArtefactsBySearchValue(
            @RequestParam CaseSearchTerm searchTerm, @RequestParam String searchValue,
            @RequestHeader(REQUESTER_ID_HEADER) UUID requesterId) {
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
    @PreAuthorize("@authorisationService.userCanSearchForPublicationByLocation()")
    public ResponseEntity<List<Artefact>> getAllRelevantArtefactsByLocationId(
        @PathVariable String locationId,
        @RequestHeader(value = REQUESTER_ID_HEADER, required = false) UUID requesterId,
        @RequestHeader(value = ADMIN_HEADER, defaultValue = DEFAULT_ADMIN_VALUE, required = false) Boolean isAdmin) {
        return ResponseEntity.ok(publicationSearchService.findAllByLocationIdAdmin(locationId, requesterId, isAdmin));
    }
}
