package uk.gov.hmcts.reform.pip.data.management.controllers;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import uk.gov.hmcts.reform.pip.data.management.config.PublicationConfiguration;
import uk.gov.hmcts.reform.pip.data.management.models.external.Subscription;
import uk.gov.hmcts.reform.pip.data.management.models.publication.Artefact;
import uk.gov.hmcts.reform.pip.data.management.models.publication.ArtefactType;
import uk.gov.hmcts.reform.pip.data.management.models.publication.HeaderGroup;
import uk.gov.hmcts.reform.pip.data.management.models.publication.Language;
import uk.gov.hmcts.reform.pip.data.management.models.publication.ListType;
import uk.gov.hmcts.reform.pip.data.management.models.publication.Sensitivity;
import uk.gov.hmcts.reform.pip.data.management.service.PublicationService;
import uk.gov.hmcts.reform.pip.data.management.service.ValidationService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import javax.validation.Valid;

/**
 * This class is the controller for creating new Publications.
 */

@RestController
@Api(tags = "Data Management Publications API")
@RequestMapping("/publication")
public class PublicationController {
    private static final String NOT_FOUND_TEXT = "No artefact found matching given parameters and date requirements";
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
     * @param provenance  Name of the source system.
     * @param sensitivity Level of sensitivity.
     * @param language    Language of publication.
     * @param displayFrom Date / Time from which the publication will be displayed.
     * @param displayTo   Date / Time until which the publication will be displayed.
     * @param listType    DL / SL / PL / WL / SJP / FL.
     * @param courtId     Source systems court id.
     * @param contentDate Local date time for when the publication is referring to start.
     * @param payload     JSON Blob with key/value pairs of data to be published.
     * @return The created artefact.
     */
    @ApiResponses({
        @ApiResponse(code = 201,
            message = "Artefact.class instance for the artefact that has been created",
            response = Artefact.class),
    })
    @ApiOperation("Upload a new publication")
    @PostMapping
    @Valid
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
        @RequestHeader(value = PublicationConfiguration.LIST_TYPE, required = false) ListType listType,
        @RequestHeader(PublicationConfiguration.COURT_ID) String courtId,
        @RequestHeader(value = PublicationConfiguration.CONTENT_DATE, required = false)
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime contentDate,
        @RequestBody String payload) {

        HeaderGroup initialHeaders = new HeaderGroup(provenance, sourceArtefactId, type, sensitivity, language,
                                                     displayFrom, displayTo, listType, courtId, contentDate
        );

        HeaderGroup headers = validationService.validateHeaders(initialHeaders);
        validationService.validateBody(payload, initialHeaders.getListType());

        Artefact artefact = Artefact.builder()
            .provenance(headers.getProvenance())
            .sourceArtefactId(headers.getSourceArtefactId())
            .type(headers.getType())
            .sensitivity(headers.getSensitivity())
            .language(headers.getLanguage())
            .displayFrom(headers.getDisplayFrom())
            .displayTo(headers.getDisplayTo())
            .listType(headers.getListType())
            .courtId(headers.getCourtId())
            .contentDate(headers.getContentDate())
            .build();

        Artefact createdItem = publicationService
            .createPublication(artefact, payload);

        //TODO handle the below in some way
        @SuppressWarnings("PMD.PrematureDeclaration")
        List<Subscription> unusedListOfEligibleSubscribers =
            publicationService.checkAndTriggerSubscriptionManagement(artefact);

        return ResponseEntity.status(HttpStatus.CREATED).body(createdItem);
    }

    /**
     * This endpoint takes in the Artefact, which is split over headers and also the payload flat file.
     *
     * @param provenance       Name of the source system.
     * @param sourceArtefactId Unique ID of what publication is called by source system.
     * @param type             List / Outcome / Judgement / Status Updates.
     * @param sensitivity      Level of sensitivity.
     * @param language         Language of publication.
     * @param displayFrom      Date / Time from which the publication will be displayed.
     * @param displayTo        Date / Time until which the publication will be displayed.
     * @param listType         DL / SL / PL / WL / SJP / FL.
     * @param courtId          Source systems court id.
     * @param contentDate      Local date time for when the publication is referring to start.
     * @param file             The flat file that is to be uploaded and associated with the Artefact.
     * @return The created artefact.
     */
    @ApiResponses({
        @ApiResponse(code = 201,
            message = "Artefact.class instance for the artefact that has been created",
            response = Artefact.class),
    })
    @ApiOperation("Upload a new publication")
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
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
        @RequestHeader(value = PublicationConfiguration.LIST_TYPE, required = false) ListType listType,
        @RequestHeader(PublicationConfiguration.COURT_ID) String courtId,
        @RequestHeader(value = PublicationConfiguration.CONTENT_DATE, required = false)
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime contentDate,
        @RequestPart MultipartFile file) {

        HeaderGroup initialHeaders = new HeaderGroup(provenance, sourceArtefactId, type, sensitivity, language,
                                                     displayFrom, displayTo, listType, courtId, contentDate
        );
        validationService.validateBody(file);

        HeaderGroup headers = validationService.validateHeaders(initialHeaders);

        Map<String, List<Object>> search = new ConcurrentHashMap<>();
        search.put("court-id", List.of(headers.getCourtId()));

        Artefact artefact = Artefact.builder()
            .provenance(headers.getProvenance())
            .sourceArtefactId(headers.getSourceArtefactId())
            .type(headers.getType())
            .sensitivity(headers.getSensitivity())
            .language(headers.getLanguage())
            .displayFrom(headers.getDisplayFrom())
            .displayTo(headers.getDisplayTo())
            .listType(headers.getListType())
            .courtId(headers.getCourtId())
            .contentDate(headers.getContentDate())
            .isFlatFile(true)
            .search(search)
            .build();

        //TODO handle the below in some way
        @SuppressWarnings("PMD.PrematureDeclaration")
        List<Subscription> unusedListOfEligibleSubscribers =
            publicationService.checkAndTriggerSubscriptionManagement(artefact);

        return ResponseEntity.status(HttpStatus.CREATED).body(publicationService.createPublication(artefact, file));
    }

    @ApiResponses({
        @ApiResponse(code = 200,
            message = "List of Artefacts matching the given courtId and verification parameters and date requirements"),
        @ApiResponse(code = 404,
            message = NOT_FOUND_TEXT),
    })
    @ApiOperation("Get a series of publications matching a given input (e.g. courtid)")
    @GetMapping("/search/{searchValue}")
    public ResponseEntity<List<Artefact>> getAllRelevantArtefactsByCourtId(@PathVariable String searchValue,
                                                                           @RequestHeader Boolean verification) {
        return ResponseEntity.ok(publicationService.findAllByCourtId(searchValue, verification));
    }

    @ApiResponses({
        @ApiResponse(code = 200,
            message = "Gets the artefact metadata",
            response = Artefact.class),
        @ApiResponse(code = 404,
            message = NOT_FOUND_TEXT),
    })
    @ApiOperation("Gets the metadata for the blob, given a specific artefact id")
    @GetMapping("/{artefactId}")
    public ResponseEntity<Artefact> getArtefactMetadata(
        @PathVariable UUID artefactId, @RequestHeader Boolean verification) {
        return ResponseEntity.ok(publicationService.getMetadataByArtefactId(artefactId, verification));
    }

    @ApiResponses({
        @ApiResponse(code = 200,
            message = "Blob data from the given request in text format.",
            response = String.class),
        @ApiResponse(code = 404,
            message = NOT_FOUND_TEXT),
    })
    @ApiOperation("Gets the the payload for the blob, given a specific artefact ID")
    @GetMapping("/{artefactId}/payload")
    public ResponseEntity<String> getArtefactPayload(
        @PathVariable UUID artefactId, @RequestHeader Boolean verification) {

        return ResponseEntity.ok(publicationService.getPayloadByArtefactId(artefactId, verification));
    }

    @ApiResponses({
        @ApiResponse(code = 200,
            message = "Blob data from the given request as a file.",
            response = String.class),
        @ApiResponse(code = 404,
            message = NOT_FOUND_TEXT),
    })
    @ApiOperation("Gets the the payload for the blob, given a specific artefact ID")
    @GetMapping(value = "/{artefactId}/file", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public ResponseEntity<Resource> getArtefactFile(@PathVariable UUID artefactId,
                                                    @RequestHeader Boolean verification) {
        Resource file = publicationService.getFlatFileByArtefactID(artefactId, verification);
        Artefact metadata = publicationService.getMetadataByArtefactId(artefactId, verification);
        String fileType = metadata.getSourceArtefactId();

        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_OCTET_STREAM_VALUE)
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + fileType)
            .body(file);

    }

}
