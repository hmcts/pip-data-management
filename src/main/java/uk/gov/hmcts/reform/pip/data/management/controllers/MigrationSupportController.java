package uk.gov.hmcts.reform.pip.data.management.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.pip.data.management.models.publication.PublicationWithRelatedData;
import uk.gov.hmcts.reform.pip.data.management.service.MigrationSupportService;
import uk.gov.hmcts.reform.pip.model.authentication.roles.IsAdmin;

import java.time.LocalDate;
import java.util.List;

@RestController
@Tag(name = "Data Management Migration Support API")
@RequestMapping("/migration-support")
@ApiResponse(responseCode = "401", description = "Invalid access credential")
@ApiResponse(responseCode = "403", description = "User has not been authorized")
@IsAdmin
@SecurityRequirement(name = "bearerAuth")
@AllArgsConstructor
@ConditionalOnProperty(prefix = "migrationSupport", name = "enableApi", havingValue = "true")
public class MigrationSupportController {

    private static final String OK_CODE = "200";
    private static final String BAD_REQUEST_CODE = "400";

    private final MigrationSupportService migrationSupportService;

    @ApiResponse(responseCode = OK_CODE, description = "List of publications with their related data")
    @ApiResponse(responseCode = BAD_REQUEST_CODE,
        description = "Invalid date supplied, or lastReceivedDateFrom is after lastReceivedDateTo")
    @Operation(summary = "Get all publications with their related data, optionally filtered by last received "
        + "date (inclusive)")
    @GetMapping("/publication")
    public ResponseEntity<List<PublicationWithRelatedData>> getAllPublications(
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate lastReceivedDateFrom,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate lastReceivedDateTo) {
        return ResponseEntity.ok(
            migrationSupportService.getAllPublications(lastReceivedDateFrom, lastReceivedDateTo)
        );
    }
}
