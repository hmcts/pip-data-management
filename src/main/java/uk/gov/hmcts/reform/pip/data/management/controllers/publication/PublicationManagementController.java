package uk.gov.hmcts.reform.pip.data.management.controllers.publication;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.pip.data.management.models.PublicationFileSizes;
import uk.gov.hmcts.reform.pip.data.management.service.publication.PublicationManagementService;
import uk.gov.hmcts.reform.pip.model.authentication.roles.IsAdmin;
import uk.gov.hmcts.reform.pip.model.publication.FileType;

import java.util.UUID;

@Slf4j
@RestController
@Tag(name = "Data Management  - API to handle the retrieval of generated publication files in alternative file "
    + "formats (PDF/Excel), and generation of publication summaries")
@RequestMapping("/publication")
@ApiResponse(responseCode = "401", description = "Invalid access credential")
@ApiResponse(responseCode = "403", description = "User has not been authorized")
@IsAdmin
@SecurityRequirement(name = "bearerAuth")
public class PublicationManagementController {

    private static final String NOT_FOUND_DESCRIPTION = "No artefact found";
    private final PublicationManagementService publicationManagementService;

    private static final String OK_CODE = "200";
    private static final String NOT_FOUND_CODE = "404";
    private static final String PAYLOAD_TOO_LARGE_CODE = "413";
    private static final String INTERNAL_ERROR_CODE = "500";

    @Autowired
    public PublicationManagementController(PublicationManagementService publicationManagementService) {
        this.publicationManagementService = publicationManagementService;
    }

    @ApiResponse(responseCode = OK_CODE, description = "Artefact summary string returned")
    @ApiResponse(responseCode = NOT_FOUND_CODE, description = NOT_FOUND_DESCRIPTION)
    @ApiResponse(responseCode = INTERNAL_ERROR_CODE, description = "Cannot process the artefact")
    @Operation(summary = "Takes in an artefact ID and returns an artefact summary")
    @GetMapping("/{artefactId}/summary")
    public ResponseEntity<String> generateArtefactSummary(@PathVariable UUID artefactId) {
        return ResponseEntity.ok(publicationManagementService.generateArtefactSummary(artefactId));
    }

    @ApiResponse(responseCode = OK_CODE, description = "PDF or Excel file for an artefact returned successfully")
    @ApiResponse(responseCode = NOT_FOUND_CODE, description = NOT_FOUND_DESCRIPTION)
    @ApiResponse(responseCode = PAYLOAD_TOO_LARGE_CODE, description = "File size too large")
    @Operation(summary = "Takes in an artefact ID and returns the stored PDF or Excel file ")
    @GetMapping("/{artefactId}/{fileType}")
    public ResponseEntity<String> getFile(
        @PathVariable UUID artefactId,
        @PathVariable  FileType fileType,
        @RequestHeader(value = "x-user-id", required = false) String userId,
        @RequestHeader(value = "x-system", required = false) boolean system,
        @RequestHeader(name = "x-additional-pdf", defaultValue = "false") boolean additionalPdf,
        @RequestParam(name = "maxFileSize", required = false) Integer maxFileSize) {
        return ResponseEntity.ok(
            publicationManagementService.getStoredPublication(
                artefactId, fileType, maxFileSize, userId, system, additionalPdf
            )
        );
    }

    @ApiResponse(responseCode = OK_CODE, description = "PDF or Excel file for an artefact exists")
    @Operation(summary = "Checks if any publication file exists for the artefact")
    @GetMapping("/{artefactId}/exists")
    public ResponseEntity<Boolean> fileExists(@PathVariable UUID artefactId) {
        return ResponseEntity.ok(publicationManagementService.fileExists(artefactId));
    }

    @ApiResponse(responseCode = OK_CODE, description = "PDF or Excel file for an artefact exists")
    @Operation(summary = "Returns the publication file sizes from Azure blob storage")
    @GetMapping("/{artefactId}/sizes")
    public ResponseEntity<PublicationFileSizes> getFileSizes(@PathVariable UUID artefactId) {
        return ResponseEntity.ok(publicationManagementService.getFileSizes(artefactId));
    }
}
