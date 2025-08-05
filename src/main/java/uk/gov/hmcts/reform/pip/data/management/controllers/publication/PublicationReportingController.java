package uk.gov.hmcts.reform.pip.data.management.controllers.publication;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.pip.data.management.service.publication.PublicationReportingService;
import uk.gov.hmcts.reform.pip.model.authentication.roles.IsAdmin;
import uk.gov.hmcts.reform.pip.model.report.PublicationMiData;

import java.util.List;

@Slf4j
@Validated
@RestController
@Tag(name = "Data Management - API for publication report generation")
@RequestMapping("/publication")
@IsAdmin
@SecurityRequirement(name = "bearerAuth")
public class PublicationReportingController {
    private static final String NO_CONTENT_DESCRIPTION = "The request has been successfully fulfilled";
    private static final String UNAUTHORISED_MESSAGE = "Invalid access credential";
    private static final String FORBIDDEN_MESSAGE = "User has not been authorized";

    private static final String OK_CODE = "200";
    private static final String NO_CONTENT_CODE = "204";
    private static final String UNAUTHORISED_CODE = "401";
    private static final String FORBIDDEN_CODE = "403";

    private final PublicationReportingService publicationReportingService;

    @Autowired
    public PublicationReportingController(PublicationReportingService publicationReportingService) {
        this.publicationReportingService = publicationReportingService;
    }

    @ApiResponse(responseCode = OK_CODE, description = "A JSON model which contains a list of artefacts")
    @ApiResponse(responseCode = UNAUTHORISED_CODE, description = UNAUTHORISED_MESSAGE)
    @ApiResponse(responseCode = FORBIDDEN_CODE, description = FORBIDDEN_MESSAGE)
    @Operation(summary = "Returns MI data for artefacts")
    @GetMapping("/mi-data")
    public ResponseEntity<List<PublicationMiData>> getMiData() {
        return ResponseEntity.ok().body(publicationReportingService.getMiData());
    }

    @ApiResponse(responseCode = NO_CONTENT_CODE, description = NO_CONTENT_DESCRIPTION)
    @ApiResponse(responseCode = UNAUTHORISED_CODE, description = UNAUTHORISED_MESSAGE)
    @ApiResponse(responseCode = FORBIDDEN_CODE, description = FORBIDDEN_MESSAGE)
    @Operation(summary = "Report artefacts which do not match any location")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PostMapping("/no-match/reporting")
    public ResponseEntity<Void> reportNoMatchArtefacts() {
        publicationReportingService.reportNoMatchArtefacts();
        return ResponseEntity.noContent().build();
    }
}
