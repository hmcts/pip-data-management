package uk.gov.hmcts.reform.pip.data.management.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.pip.data.management.models.Hearing;
import uk.gov.hmcts.reform.pip.data.management.service.HearingService;

import java.util.List;

@RestController
@Tag(name = "Data Management Hearing list API")
@RequestMapping("/hearings")
public class HearingController {

    @Autowired
    private HearingService hearingService;

    private static final String OK_CODE = "200";
    private static final String NOT_FOUND_CODE = "404";

    @ApiResponse(responseCode = OK_CODE, description = "Hearings found")
    @ApiResponse(responseCode = NOT_FOUND_CODE, description = "No hearings found with the courtId {courtId}")
    @Operation(summary = "Gets hearings pertaining to a specific courtId")
    @GetMapping("/{courtId}")
    public ResponseEntity<List<Hearing>> getHearing(@Parameter(description =
        "The courtId to match for associated hearings", required = true) @PathVariable int courtId) {
        return ResponseEntity.ok(hearingService.getHearings(courtId));
    }

    @ApiResponse(responseCode = OK_CODE, description = "Hearings found")
    @ApiResponse(responseCode = NOT_FOUND_CODE, description = "No hearings found with the case name {caseName}")
    @Operation(summary = "Gets hearings pertaining to case name full or partial match")
    @GetMapping("/case-name/{caseName}")
    public ResponseEntity<List<Hearing>> getHearingsByName(@Parameter(description =
        "The case name to match for associated hearings",
        required = true) @PathVariable String caseName) {
        return ResponseEntity.ok(hearingService.getHearingByName(caseName));
    }

    @ApiResponse(responseCode = OK_CODE, description = "Hearing found")
    @ApiResponse(responseCode = NOT_FOUND_CODE, description = "No hearing found with the case number {caseNumber}")
    @Operation(summary = "Gets a hearing pertaining to case number")
    @GetMapping("/case-number/{caseNumber}")
    public ResponseEntity<Hearing> getHearingsByCaseNumber(@Parameter(description =
        "The case number to match for associated hearings",
        required = true) @PathVariable String caseNumber) {
        return ResponseEntity.ok(hearingService.getHearingByCaseNumber(caseNumber));
    }

    @ApiResponse(responseCode = OK_CODE, description = "Hearing found")
    @ApiResponse(responseCode = NOT_FOUND_CODE, description = "No hearing found with the urn {urnNumber}")
    @Operation(summary = "Gets a hearing pertaining to urn number")
    @GetMapping("/urn/{urnNumber}")
    public ResponseEntity<Hearing> getHearingByUrn(@Parameter(description = "The case number to match", required = true)
                                                   @PathVariable String urnNumber) {
        return ResponseEntity.ok(hearingService.getHearingByUrn(urnNumber));
    }
}
