package uk.gov.hmcts.reform.pip.data.management.controllers;

import io.swagger.annotations.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;
import uk.gov.hmcts.reform.pip.data.management.models.Artifact;
import uk.gov.hmcts.reform.pip.data.management.service.PublicationService;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
@Api(tags = "Data Management Publications API")
@RequestMapping("/publication")
public class PublicationController {

    private PublicationService publicationService;

    @Autowired
    public PublicationController(PublicationService publicationService) {
        this.publicationService = publicationService;
    }

    @ApiResponses({
        @ApiResponse(code = 200, message = "Publication has been created"),
    })
    @ApiImplicitParams({
        @ApiImplicitParam(name = "x-artifact-id", required = true, paramType = "header", dataTypeClass = String.class, example = "1561-1511-1515-2131"),
        @ApiImplicitParam(name = "x-provenance", required = true, paramType = "header", dataTypeClass = String.class, example = "CFT"),
        @ApiImplicitParam(name = "x-source-artifact-id", required = true, paramType = "header", dataTypeClass = String.class, example = "daily-word"),
        @ApiImplicitParam(name = "x-type", required = true, paramType = "header", dataTypeClass = String.class, example = "List"),
        @ApiImplicitParam(name = "x-sensitivity", paramType = "header", dataTypeClass = String.class, example = "WARNED"),
        @ApiImplicitParam(name = "x-language", paramType = "header", dataTypeClass = String.class, example = "English"),
        @ApiImplicitParam(name = "x-search", paramType = "header", dataTypeClass = String.class),
        @ApiImplicitParam(name = "x-display-from", paramType = "header", dataTypeClass = LocalDateTime.class, example = "2021-11-14T19:32"),
        @ApiImplicitParam(name = "x-display-to", paramType = "header", dataTypeClass = LocalDateTime.class, example = "2021-11-14T19:32"),
    })
    @ApiOperation("Upload a new publication")
    @PutMapping
    public ResponseEntity<String> uploadPublication(
        @ApiIgnore @RequestHeader Map<String, String> headers,
        @RequestBody String payload) {
        publicationService.createPublication(new Artifact(headers, payload));
        return ResponseEntity.ok("Test Body");

    }

}
