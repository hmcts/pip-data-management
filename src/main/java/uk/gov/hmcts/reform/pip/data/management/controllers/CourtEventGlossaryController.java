package uk.gov.hmcts.reform.pip.data.management.controllers;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.pip.data.management.models.lcsu.EventGlossary;
import uk.gov.hmcts.reform.pip.data.management.service.CourtEventGlossaryService;

import java.util.List;

@RestController
@Api(tags = "Data Management Court Event Glossary API")
@RequestMapping("/glossary")
public class CourtEventGlossaryController {

    @Autowired
    private CourtEventGlossaryService courtEventGlossaryService;

    @ApiResponses({
        @ApiResponse(code = 200, message = "All court event statuses returned"),
    })
    @ApiOperation("Get all courts event statuses with their description")
    @GetMapping
    public ResponseEntity<List<EventGlossary>> getCourtEventStatusList() {
        return ResponseEntity.ok(courtEventGlossaryService.getAllCourtEventGlossary());
    }
}
