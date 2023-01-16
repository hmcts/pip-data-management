package uk.gov.hmcts.reform.pip.data.management.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.pip.data.management.models.lcsu.CaseEventGlossary;
import uk.gov.hmcts.reform.pip.data.management.service.CaseEventGlossaryService;

import java.util.List;

@RestController
@Tag(name = "Data Management Case Event Glossary API")
@RequestMapping("/glossary")
public class CaseEventGlossaryController {

    @Autowired
    private CaseEventGlossaryService caseEventGlossaryService;

    @ApiResponse(responseCode = "200", description = "All case event glossary returned")
    @Operation(summary = "Get all case event glossary list with their description")
    @GetMapping
    public ResponseEntity<List<CaseEventGlossary>> getCaseEventGlossaryList() {
        return ResponseEntity.ok(caseEventGlossaryService.getAllCaseEventGlossary());
    }
}
