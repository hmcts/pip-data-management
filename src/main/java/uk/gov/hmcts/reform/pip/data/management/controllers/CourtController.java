package uk.gov.hmcts.reform.pip.data.management.controllers;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.pip.data.management.models.Court;
import uk.gov.hmcts.reform.pip.data.management.models.request.FilterRequest;
import uk.gov.hmcts.reform.pip.data.management.service.CourtService;

import java.util.List;

@RestController
@Api(tags = "Data Management Court list API")
@RequestMapping("/courts")
public class CourtController {

    @Autowired
    private CourtService courtService;


    @ApiResponses({
        @ApiResponse(code = 200, message = "All courts returned"),
    })
    @ApiOperation("Get all courts with their hearings")
    @GetMapping
    public ResponseEntity<List<Court>> getCourtList() {
        return ResponseEntity.ok(courtService.getAllCourts());
    }

    @ApiResponses({
        @ApiResponse(code = 200, message = "Court found"),
        @ApiResponse(code = 404, message = "No court found with the search {input}")
    })
    @ApiOperation("Gets a court by searching by the court name and returning")
    @GetMapping("/{input}")
    public ResponseEntity<Court> getCourtByName(@ApiParam(value = "The search input to retrieve", required = true)
                                                @PathVariable String input) {
        return ResponseEntity.ok(courtService.handleSearchCourt(input));

    }

    @ApiResponses({
        @ApiResponse(code = 200, message = "Filtered courts")
    })
    @ApiOperation("Filters list of courts by court attribues and values")
    @ApiImplicitParam(name = "filterRequest", example = "{\n filters: ['location'], \n values: ['london']}")
    @GetMapping("/filter")
    public ResponseEntity<List<Court>> filterCourts(@RequestBody FilterRequest filterRequest) {
        return ResponseEntity.ok(courtService.handleFilterRequest(
            filterRequest.getFilters(),
            filterRequest.getValues()
        ));
    }
}
