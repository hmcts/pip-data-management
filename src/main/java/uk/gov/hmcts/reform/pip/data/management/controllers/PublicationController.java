package uk.gov.hmcts.reform.pip.data.management.controllers;

import io.swagger.annotations.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uk.gov.hmcts.reform.pip.data.management.models.publication.Artefact;
import uk.gov.hmcts.reform.pip.data.management.models.publication.ArtefactType;
import uk.gov.hmcts.reform.pip.data.management.models.publication.Language;
import uk.gov.hmcts.reform.pip.data.management.models.publication.Sensitivity;
import uk.gov.hmcts.reform.pip.data.management.service.PublicationService;

import java.time.LocalDateTime;

/**
 * This class is the controller for creating new Publications
 */
@RestController
@Api(tags = "Data Management Publications API")
@RequestMapping("/publication")
public class PublicationController {

    private PublicationService publicationService;

    /**
     * Constructor for Publication controller.
     * @param publicationService The PublicationService that contains the business logic to handle publications.
     */
    @Autowired
    public PublicationController(PublicationService publicationService) {
        this.publicationService = publicationService;
    }

    /**
     * This endpoint takes in the Artefact, which is split over headers and also the payload body.
     * @param artefactId Unique ID for the publication.
     * @param provenance Name of the source system.
     * @param sourceArtefactId Unique ID of what publication is called by source system.
     * @param type List / Outcome / Judgement / Status Updates.
     * @param sensitivity Level of sensitivity.
     * @param language Language of publication.
     * @param search Metadata that will be indexed for searching.
     * @param displayFrom Date / Time from which the publication will be displayed.
     * @param displayTo Date / Time until which the publication will be displayed.
     * @param payload JSON Blob with key/value pairs of data to be published.
     * @return The UUID of the created artefact.
     */
    @ApiResponses({
        @ApiResponse(code = 200, message = "Publication has been created"),
    })
    @ApiOperation("Upload a new publication")
    @PutMapping
    public ResponseEntity<String> uploadPublication(
        @RequestHeader("x-artefact-id") String artefactId,
        @RequestHeader("x-provenance") String provenance,
        @RequestHeader("x-source-artefact-id") String sourceArtefactId,
        @RequestHeader("x-type") ArtefactType type,
        @RequestHeader(value = "x-sensitivity", required = false) Sensitivity sensitivity,
        @RequestHeader(value = "x-language", required = false) Language language,
        @RequestHeader(value = "x-search", required = false) String search,
        @RequestHeader(value = "x-display-from", required = false) LocalDateTime displayFrom,
        @RequestHeader(value = "x-display-to", required = false) LocalDateTime displayTo,
        @RequestBody String payload) {

        Artefact artefact = Artefact.builder().artefactId(artefactId)
            .provenance(provenance).sourceArtefactId(sourceArtefactId)
            .type(type).sensitivity(sensitivity)
            .language(language).search(search)
            .displayFrom(displayFrom).displayTo(displayTo)
            .payload(payload)
            .build();

        String createdItem = publicationService
            .createPublication(artefact);

        return ResponseEntity.ok(createdItem);
    }

}
