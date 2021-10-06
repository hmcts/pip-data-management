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
import uk.gov.hmcts.reform.pip.data.management.models.Hearing;
import uk.gov.hmcts.reform.pip.data.management.service.HearingService;

import java.util.List;

@RestController
@Api(tags = "Data Management Hearing list API")
@RequestMapping("/hearings")
public class HearingController {

    @Autowired
    private HearingService hearingService;


    @ApiResponses({
        @ApiResponse(code = 200, message = "Hearings found"),
        @ApiResponse(code = 404, message = "No hearings found with the courtId {courtId}")
    })
    @ApiOperation("Gets hearings pertaining to a specific courtId")
    @GetMapping("/{courtId}")
    public ResponseEntity<List<Hearing>> getHearing(@ApiParam(value = "The courtId to for associated hearings",
        required = true) @PathVariable int courtId) {
        return ResponseEntity.ok(hearingService.getHearings(courtId));
    }
}
