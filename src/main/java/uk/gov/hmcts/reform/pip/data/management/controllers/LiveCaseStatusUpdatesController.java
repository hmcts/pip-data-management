package uk.gov.hmcts.reform.pip.data.management.controllers;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
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
@Api(tags = "Data Management Live Case Status Updates (LCSU) API")
@RequestMapping("/lcsu")
public class LiveCaseStatusUpdatesController {

    @Autowired
    private LiveCaseStatusService liveCaseStatusService;

    @ApiResponses({
        @ApiResponse(code = 200, message = "Live Cases found"),
        @ApiResponse(code = 404, message = "No Live cases found with the court id {courtId}")
    })
    @ApiOperation("Gets a court by searching by the court name and returning")
    @GetMapping("/{courtId}")
    public ResponseEntity<List<LiveCaseStatus>> getLiveCaseStatus(@ApiParam(value = "The court id to search",
        required = true) @PathVariable int courtId) {
        return ResponseEntity.ok(liveCaseStatusService.handleLiveCaseRequest(courtId));
    }
}
