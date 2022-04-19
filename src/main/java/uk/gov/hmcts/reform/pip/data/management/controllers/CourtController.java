package uk.gov.hmcts.reform.pip.data.management.controllers;

import com.fasterxml.jackson.annotation.JsonView;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import uk.gov.hmcts.reform.pip.data.management.authentication.roles.IsAdmin;
import uk.gov.hmcts.reform.pip.data.management.models.court.Court;
import uk.gov.hmcts.reform.pip.data.management.models.court.CourtViews;
import uk.gov.hmcts.reform.pip.data.management.service.CourtService;

import java.util.Collection;
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
    @GetMapping(produces = "application/json")
    @JsonView(CourtViews.BaseView.class)
    public ResponseEntity<List<Court>> getCourtList() {
        return new ResponseEntity<>(courtService.getAllCourts(), HttpStatus.OK);
    }

    @ApiResponses({
        @ApiResponse(code = 200, message = "Court found"),
        @ApiResponse(code = 404, message = "No court found with the id {courtId}")
    })
    @ApiOperation("Gets a court by searching by the court id and returning")
    @GetMapping("/{courtId}")
    public ResponseEntity<Court> getCourtById(@ApiParam(value = "The court Id to retrieve", required = true)
                                                @PathVariable Integer courtId) {
        return ResponseEntity.ok(courtService.getCourtById(courtId));

    }

    @ApiResponses({
        @ApiResponse(code = 200, message = "Court found"),
        @ApiResponse(code = 404, message = "No court found with the search {input}")
    })
    @ApiOperation("Gets a court by searching by the court name and returning")
    @GetMapping("/name/{courtName}")
    public ResponseEntity<Court> getCourtByName(@ApiParam(value = "The search input to retrieve", required = true)
                                                @PathVariable String courtName) {
        return ResponseEntity.ok(courtService.getCourtByName(courtName));

    }

    @ApiResponses({
        @ApiResponse(code = 200, message = "Filtered courts")
    })
    @ApiOperation("Filters list of courts by region or jurisdiction")
    @GetMapping("/filter")
    @JsonView(CourtViews.BaseView.class)
    public ResponseEntity<List<Court>> searchByRegionAndJurisdiction(
        @RequestParam(required = false) List<String> regions,
        @RequestParam(required = false) List<String> jurisdictions) {

        return ResponseEntity.ok(courtService.searchByRegionAndJurisdiction(regions, jurisdictions));
    }

    @ApiResponses({
        @ApiResponse(code = 200, message = "Uploaded courts"),
        @ApiResponse(code = 403, message = "User has not been authorized")
    })
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @IsAdmin
    public ResponseEntity<Collection<Court>> uploadCourts(@RequestPart MultipartFile courtList) {
        return ResponseEntity.ok(courtService.uploadCourts(courtList));
    }

}
