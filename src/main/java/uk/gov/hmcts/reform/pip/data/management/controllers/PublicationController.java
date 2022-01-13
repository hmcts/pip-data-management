package uk.gov.hmcts.reform.pip.data.management.controllers;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.pip.data.management.config.PublicationConfiguration;
import uk.gov.hmcts.reform.pip.data.management.errorhandling.exceptions.DateHeaderValidationException;
import uk.gov.hmcts.reform.pip.data.management.errorhandling.exceptions.EmptyRequestHeaderException;
import uk.gov.hmcts.reform.pip.data.management.models.publication.Artefact;
import uk.gov.hmcts.reform.pip.data.management.models.publication.ArtefactType;
import uk.gov.hmcts.reform.pip.data.management.models.publication.Language;
import uk.gov.hmcts.reform.pip.data.management.models.publication.Sensitivity;
import uk.gov.hmcts.reform.pip.data.management.service.PublicationService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * This class is the controller for creating new Publications.
 */
@RestController
@Api(tags = "Data Management Publications API")
@RequestMapping("/publication")
public class PublicationController {

    private final PublicationService publicationService;

    /**
     * Constructor for Publication controller.
     *
     * @param publicationService The PublicationService that contains the business logic to handle publications.
     */
    @Autowired
    public PublicationController(PublicationService publicationService) {
        this.publicationService = publicationService;
    }

    /**
     * This endpoint takes in the Artefact, which is split over headers and also the payload body.
     *
     * @param provenance       Name of the source system.
     * @param sourceArtefactId Unique ID of what publication is called by source system.
     * @param type             List / Outcome / Judgement / Status Updates.
     * @param sensitivity      Level of sensitivity.
     * @param language         Language of publication.
     * @param displayFrom      Date / Time from which the publication will be displayed.
     * @param displayTo        Date / Time until which the publication will be displayed.
     * @param payload          JSON Blob with key/value pairs of data to be published.
     * @return The created artefact.
     */
    @ApiResponses({
        @ApiResponse(code = 201,
            message = "Artefact.class instance for the artefact that has been created",
            response = Artefact.class),
    })
    @ApiOperation("Upload a new publication")
    @PutMapping
    public ResponseEntity<Artefact> uploadPublication(
        @RequestHeader(PublicationConfiguration.PROVENANCE_HEADER) String provenance,
        @RequestHeader(PublicationConfiguration.SOURCE_ARTEFACT_ID_HEADER) String sourceArtefactId,
        @RequestHeader(PublicationConfiguration.TYPE_HEADER) ArtefactType type,
        @RequestHeader(value = PublicationConfiguration.SENSITIVITY_HEADER, required = false) Sensitivity sensitivity,
        @RequestHeader(value = PublicationConfiguration.LANGUAGE_HEADER, required = false) Language language,
        @RequestHeader(value = PublicationConfiguration.DISPLAY_FROM_HEADER, required = false)
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime displayFrom,
        @RequestHeader(value = PublicationConfiguration.DISPLAY_TO_HEADER, required = false)
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime displayTo,
        @RequestBody String payload) {
        validateRequestHeaders(provenance, sourceArtefactId);
        LocalDateTime validatedDateFrom = displayFrom;
        if (!validateDateFromDateTo(displayFrom, displayTo, type)) {
            validatedDateFrom = LocalDateTime.now();
        }
        Artefact artefact = Artefact.builder()
            .provenance(provenance).sourceArtefactId(sourceArtefactId)
            .type(type).sensitivity(sensitivity)
            .language(language)
            .displayFrom(validatedDateFrom)
            .displayTo(displayTo)
            .build();

        Artefact createdItem = publicationService
            .createPublication(artefact, payload);

        return ResponseEntity.status(HttpStatus.CREATED).body(createdItem);
    }

    @ApiResponses({
        @ApiResponse(code = 200,
            message = "List of Artefacts matching the given courtId and verification parameters and date requirements"),
        @ApiResponse(code = 404,
            message = "No artefact found matching given parameters and date requirements"),
    })
    @ApiOperation("Get a series of publications matching a given input (e.g. courtid)")
    @GetMapping("/search/{searchValue}")
    public ResponseEntity<List<Artefact>> getAllRelevantArtefactsByCourtId(@PathVariable String searchValue,
                                                                           @RequestHeader Boolean verification) {
        return ResponseEntity.ok(publicationService.findAllByCourtId(searchValue, verification));
    }

    @ApiResponses({
        @ApiResponse(code = 200,
            message = "Blob data from the given request in text format.",
            response = String.class),
        @ApiResponse(code = 404,
            message = "No artefact found matching given parameters and date requirements"),
    })
    @ApiOperation("Get the info from within a blob given source artefact id and provenance")
    @GetMapping("/{artefactId}")
    public ResponseEntity<String> getBlobData(@PathVariable UUID artefactId, @RequestHeader Boolean verification) {

        return ResponseEntity.ok(publicationService.getByArtefactId(artefactId, verification));

    }

    /**
     * Validates the Provenance and Source Artefact ID headers to check they are not empty.
     * due to Spring validation only checking if these are required.
     */
    private void validateRequestHeaders(String provenance, String sourceArtefactId) {
        if (provenance.isEmpty()) {
            throw new EmptyRequestHeaderException(PublicationConfiguration.PROVENANCE_HEADER);
        } else if (sourceArtefactId.isEmpty()) {
            throw new EmptyRequestHeaderException(PublicationConfiguration.SOURCE_ARTEFACT_ID_HEADER);
        }
    }


    /**
     * Enforces conditional mandatory fields based on the publication type. This is to ensure that status updates are
     * able to persist indefinitely if required.
     */
    public boolean validateDateFromDateTo(LocalDateTime displayFrom, LocalDateTime displayTo, ArtefactType type) {
        if (type.equals(ArtefactType.LIST) || type.equals(ArtefactType.JUDGEMENT)
            || type.equals(ArtefactType.OUTCOME)) {
            if (displayFrom == null) {
                throw new DateHeaderValidationException("Date from field is mandatory for this artefact type");
            }
            if (displayTo == null) {
                throw new DateHeaderValidationException("Date to field is mandatory for this artefact type");
            }
        } else {
            return !type.equals(ArtefactType.STATUS_UPDATES) || displayFrom != null;
        }

        return true;
    }
}
