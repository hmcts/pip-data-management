package uk.gov.hmcts.reform.pip.data.management.controllers;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
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
import uk.gov.hmcts.reform.pip.data.management.models.court.Court;
import uk.gov.hmcts.reform.pip.data.management.service.CourtService;

import java.util.Collection;
import java.util.List;
import java.util.UUID;


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
        @ApiResponse(code = 404, message = "No court found with the id {courtId}")
    })
    @ApiOperation("Gets a court by searching by the court id and returning")
    @GetMapping("/{courtId}")
    public ResponseEntity<Court> getCourtById(@ApiParam(value = "The court Id to retrieve", required = true)
                                                @PathVariable UUID courtId) {
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
    public ResponseEntity<List<Court>> searchByRegionAndJurisdiction(
        @RequestParam(required = false) List<String> regions,
        @RequestParam(required = false) List<String> jurisdictions) {

        return ResponseEntity.ok(courtService.searchByRegionAndJurisdiction(regions, jurisdictions));
    }

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Collection<Court>> uploadCourts(@RequestPart MultipartFile courtList) {
        return ResponseEntity.ok(courtService.uploadCourts(courtList));
    }

}
