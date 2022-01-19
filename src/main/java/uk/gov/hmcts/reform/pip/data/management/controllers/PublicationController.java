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
import uk.gov.hmcts.reform.pip.data.management.models.publication.Artefact;
import uk.gov.hmcts.reform.pip.data.management.models.publication.ArtefactType;
import uk.gov.hmcts.reform.pip.data.management.models.publication.Language;
import uk.gov.hmcts.reform.pip.data.management.models.publication.Sensitivity;
import uk.gov.hmcts.reform.pip.data.management.service.PublicationService;
import uk.gov.hmcts.reform.pip.data.management.service.ValidationService;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * This class is the controller for creating new Publications.
 */
@RestController
@Api(tags = "Data Management Publications API")
@RequestMapping("/publication")
public class PublicationController {

    private final PublicationService publicationService;

    @Autowired
    private final ValidationService validationService;

    /**
     * Constructor for Publication controller.
     *
     * @param publicationService The PublicationService that contains the business logic to handle publications.
     */
    @Autowired
    public PublicationController(PublicationService publicationService, ValidationService validationService) {
        this.publicationService = publicationService;
        this.validationService = validationService;
    }

    /**
     * This endpoint takes in the Artefact, which is split over headers and also the payload body.
     * The suppression of concurrentHashMap warnings is because we require the ability to use nulls (say, if a date
     * is left blank), and Hashmap provides this whereas concurrentHashMap does not.
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
    @SuppressWarnings("PMD.UseConcurrentHashMap")
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

        Map<String, Object> headers = new HashMap<>();
        headers.put(PublicationConfiguration.PROVENANCE_HEADER, provenance);
        headers.put(PublicationConfiguration.SOURCE_ARTEFACT_ID_HEADER, sourceArtefactId);
        headers.put(PublicationConfiguration.TYPE_HEADER, type);
        headers.put(PublicationConfiguration.SENSITIVITY_HEADER, sensitivity);
        headers.put(PublicationConfiguration.LANGUAGE_HEADER, language);
        headers.put(PublicationConfiguration.DISPLAY_FROM_HEADER, displayFrom);
        headers.put(PublicationConfiguration.DISPLAY_TO_HEADER, displayTo);

        Map<String, Object> headerMap = validationService.validateHeaders(headers);

        Artefact artefact = Artefact.builder()
            .provenance((String) headerMap.get(PublicationConfiguration.PROVENANCE_HEADER))
            .sourceArtefactId((String) headerMap.get(PublicationConfiguration.SOURCE_ARTEFACT_ID_HEADER))
            .type((ArtefactType) headerMap.get(PublicationConfiguration.TYPE_HEADER))
            .sensitivity((Sensitivity) headerMap.get(PublicationConfiguration.SENSITIVITY_HEADER))
            .language((Language) headerMap.get(PublicationConfiguration.LANGUAGE_HEADER))
            .displayFrom((LocalDateTime) headerMap.get(PublicationConfiguration.DISPLAY_FROM_HEADER))
            .displayTo((LocalDateTime) headerMap.get(PublicationConfiguration.DISPLAY_TO_HEADER))
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

}
