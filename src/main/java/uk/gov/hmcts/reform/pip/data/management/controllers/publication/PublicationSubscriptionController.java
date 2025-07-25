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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.pip.data.management.service.publication.PublicationSubscriptionService;
import uk.gov.hmcts.reform.pip.model.authentication.roles.IsAdmin;

import java.util.UUID;

@Slf4j
@Validated
@RestController
@Tag(name = "Data Management - API for managing subscriptions for publications, and generation of publication "
    + "summaries for the subscription email")
@ApiResponse(responseCode = "401", description = "Invalid access credential")
@ApiResponse(responseCode = "403", description = "User has not been authorized")
@RequestMapping("/publication")
@IsAdmin
@SecurityRequirement(name = "bearerAuth")
public class PublicationSubscriptionController {
    private static final String NO_CONTENT_DESCRIPTION = "The request has been successfully fulfilled";
    private static final String NOT_FOUND_MESSAGE = "No artefact found";

    private static final String OK_CODE = "200";
    private static final String NO_CONTENT_CODE = "204";
    private static final String NOT_FOUND_CODE = "404";
    private static final String INTERNAL_ERROR_CODE = "500";

    private final PublicationSubscriptionService publicationSubscriptionService;

    @Autowired
    public PublicationSubscriptionController(PublicationSubscriptionService publicationSubscriptionService) {
        this.publicationSubscriptionService = publicationSubscriptionService;
    }

    @ApiResponse(responseCode = NO_CONTENT_CODE, description = NO_CONTENT_DESCRIPTION)
    @Operation(summary = "Find latest artefacts from today and send them to subscribers")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PostMapping("/latest/subscription")
    public ResponseEntity<Void> sendNewArtefactsForSubscription() {
        publicationSubscriptionService.checkNewlyActiveArtefacts();
        return ResponseEntity.noContent().build();
    }

    @ApiResponse(responseCode = OK_CODE, description = "Artefact summary string returned")
    @ApiResponse(responseCode = NOT_FOUND_CODE, description = NOT_FOUND_MESSAGE)
    @ApiResponse(responseCode = INTERNAL_ERROR_CODE, description = "Cannot process the artefact")
    @Operation(summary = "Takes in an artefact ID and returns an artefact summary")
    @GetMapping("/{artefactId}/summary")
    public ResponseEntity<String> generateArtefactSummary(@PathVariable UUID artefactId) {
        return ResponseEntity.ok(publicationSubscriptionService.generateArtefactSummary(artefactId));
    }
}
