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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.pip.data.management.service.publication.ArtefactTriggerService;
import uk.gov.hmcts.reform.pip.model.authentication.roles.IsAdmin;

@Slf4j
@Validated
@RestController
@Tag(name = "Data Management - API for managing subscriptions for publications")
@RequestMapping("/publication")
public class PublicationSubscriptionController {
    private static final String NO_CONTENT_DESCRIPTION = "The request has been successfully fulfilled";
    private static final String UNAUTHORISED_MESSAGE = "Invalid access credential";
    private static final String FORBIDDEN_MESSAGE = "User has not been authorized";

    private static final String NO_CONTENT_CODE = "204";
    private static final String UNAUTHORISED_CODE = "401";
    private static final String FORBIDDEN_CODE = "403";

    private static final String BEARER_AUTHENTICATION = "bearerAuth";

    private final ArtefactTriggerService artefactTriggerService;

    @Autowired
    public PublicationSubscriptionController(ArtefactTriggerService artefactTriggerService) {
        this.artefactTriggerService = artefactTriggerService;
    }

    @ApiResponse(responseCode = NO_CONTENT_CODE, description = NO_CONTENT_DESCRIPTION)
    @ApiResponse(responseCode = UNAUTHORISED_CODE, description = UNAUTHORISED_MESSAGE)
    @ApiResponse(responseCode = FORBIDDEN_CODE, description = FORBIDDEN_MESSAGE)
    @Operation(summary = "Find latest artefacts from today and send them to subscribers")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PostMapping("/latest/subscription")
    @IsAdmin
    @SecurityRequirement(name = BEARER_AUTHENTICATION)
    public ResponseEntity<Void> sendNewArtefactsForSubscription() {
        artefactTriggerService.checkNewlyActiveArtefacts();
        return ResponseEntity.noContent().build();
    }

}
