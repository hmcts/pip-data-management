package uk.gov.hmcts.reform.pip.data.management.controllers;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiParam;
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

    @GetMapping
    public ResponseEntity<List<Court>> getCourtList() {
        return ResponseEntity.ok(courtService.getAllCourts());
    }

    @GetMapping("/{input}")
    public ResponseEntity<Court> getCourtByName(@ApiParam(value = "The search input to retrieve", required = true)
                                                @PathVariable String input) {
        return ResponseEntity.ok(courtService.handleSearchCourt(input));

    }

    @GetMapping("/filter")
    public ResponseEntity<List<Court>> filterCourts(@RequestBody FilterRequest filterRequest) {
        return ResponseEntity.ok(courtService.handleFilterRequest(
            filterRequest.getFilters(),
            filterRequest.getValues()
        ));
    }
}
