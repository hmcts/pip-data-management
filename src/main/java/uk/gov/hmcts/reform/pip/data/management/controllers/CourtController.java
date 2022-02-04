package uk.gov.hmcts.reform.pip.data.management.controllers;

import com.opencsv.bean.ColumnPositionMappingStrategy;
import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import uk.gov.hmcts.reform.pip.data.management.models.Court;
import uk.gov.hmcts.reform.pip.data.management.models.court.CourtCsv;
import uk.gov.hmcts.reform.pip.data.management.models.request.FilterRequest;
import uk.gov.hmcts.reform.pip.data.management.service.CourtService;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.file.Files;
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
        @ApiResponse(code = 404, message = "No court found with the id {courtId}")
    })
    @ApiOperation("Gets a court by searching by the court id and returning")
    @GetMapping("/{courtId}")
    public ResponseEntity<Court> getCourtById(@ApiParam(value = "The court Id to retrieve", required = true)
                                                @PathVariable Integer courtId) {
        return ResponseEntity.ok(courtService.handleSearchCourt(courtId));

    }

    @ApiResponses({
        @ApiResponse(code = 200, message = "Court found"),
        @ApiResponse(code = 404, message = "No court found with the search {input}")
    })
    @ApiOperation("Gets a court by searching by the court name and returning")
    @GetMapping("/find/{input}")
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

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<List<CourtCsv>> uploadCourts(@RequestPart MultipartFile courtList) throws IOException {
        Reader reader = new BufferedReader(new InputStreamReader(courtList.getInputStream()));
        CsvToBean<CourtCsv> csvToBean = new CsvToBeanBuilder<CourtCsv>(reader)
            .withType(CourtCsv.class)
            .build();

        List<CourtCsv> courtCsv = csvToBean.parse();




        return ResponseEntity.ok(csvToBean.parse());


    }


}
