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
import uk.gov.hmcts.reform.pip.data.management.models.lcsu.LiveCaseStatus;
import uk.gov.hmcts.reform.pip.data.management.service.LiveCaseStatusService;

import java.util.List;

@RestController
@Tag(name = "Data Management Live Case Status Updates (LCSU) API")
@RequestMapping("/lcsu")
public class LiveCaseStatusUpdatesController {

    @Autowired
    private LiveCaseStatusService liveCaseStatusService;

    @ApiResponse(responseCode = "200", description = "Live Cases found")
    @ApiResponse(responseCode = "404", description = "No Live cases found with the court id {courtId}")
    @Operation(summary = "Gets a court by searching by the court name and returning")
    @GetMapping("/{courtId}")
    public ResponseEntity<List<LiveCaseStatus>> getLiveCaseStatus(@Parameter(description = "The court id to search",
        required = true) @PathVariable int courtId) {
        return ResponseEntity.ok(liveCaseStatusService.handleLiveCaseRequest(courtId));
    }
}
