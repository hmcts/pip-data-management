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
import uk.gov.hmcts.reform.pip.data.management.models.lcsu.CaseEventGlossary;
import uk.gov.hmcts.reform.pip.data.management.service.CaseEventGlossaryService;

import java.util.List;

@RestController
@Api(tags = "Data Management Case Event Glossary API")
@RequestMapping("/glossary")
public class CaseEventGlossaryController {

    @Autowired
    private CaseEventGlossaryService caseEventGlossaryService;

    @ApiResponses({
        @ApiResponse(code = 200, message = "All case event glossary returned"),
    })
    @ApiOperation("Get all case event glossary list with their description")
    @GetMapping
    public ResponseEntity<List<CaseEventGlossary>> getCaseEventGlossaryList() {
        return ResponseEntity.ok(caseEventGlossaryService.getAllCaseEventGlossary());
    }
}
