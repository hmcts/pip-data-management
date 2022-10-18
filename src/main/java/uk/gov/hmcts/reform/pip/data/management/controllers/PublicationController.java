package uk.gov.hmcts.reform.pip.data.management.controllers;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import uk.gov.hmcts.reform.pip.data.management.authentication.roles.IsAdmin;
import uk.gov.hmcts.reform.pip.data.management.authentication.roles.IsPublisher;
import uk.gov.hmcts.reform.pip.data.management.config.PublicationConfiguration;
import uk.gov.hmcts.reform.pip.data.management.models.location.LocationType;
import uk.gov.hmcts.reform.pip.data.management.models.publication.Artefact;
import uk.gov.hmcts.reform.pip.data.management.models.publication.ArtefactType;
import uk.gov.hmcts.reform.pip.data.management.models.publication.HeaderGroup;
import uk.gov.hmcts.reform.pip.data.management.models.publication.Language;
import uk.gov.hmcts.reform.pip.data.management.models.publication.ListType;
import uk.gov.hmcts.reform.pip.data.management.models.publication.Sensitivity;
import uk.gov.hmcts.reform.pip.data.management.service.ChannelManagementService;
import uk.gov.hmcts.reform.pip.data.management.service.PublicationService;
import uk.gov.hmcts.reform.pip.data.management.service.ValidationService;
import uk.gov.hmcts.reform.pip.data.management.utils.CaseSearchTerm;
import uk.gov.hmcts.reform.pip.model.enums.UserActions;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import javax.validation.Valid;

import static uk.gov.hmcts.reform.pip.model.LogBuilder.writeLog;

/**
 * This class is the controller for creating new Publications.
 */

@Slf4j
@Validated
@RestController
@Api(tags = "Data Management Publications API")
@RequestMapping("/publication")
@SuppressWarnings({"PMD.ExcessiveImports"})
public class PublicationController {

    private static final String USER_ID_HEADER = "x-user-id";
    private static final String ADMIN_HEADER = "x-admin";

    private static final String NO_CONTENT_DESCRIPTION = "The request has been successfully fulfilled";
    private static final String UNAUTHORIZED_DESCRIPTION = "User has not been authorized";

    private static final String NOT_FOUND_DESCRIPTION =
        "No artefact found matching given parameters and date requirements";

    private final PublicationService publicationService;

    @Autowired
    private final ValidationService validationService;

    private final ChannelManagementService channelManagementService;

    private static final String DEFAULT_ADMIN_VALUE = "false";

    /**
     * Constructor for Publication controller.
     *
     * @param publicationService The PublicationService that contains the business logic to handle publications.
     */
    @Autowired
    public PublicationController(PublicationService publicationService, ValidationService validationService,
                                 ChannelManagementService channelManagementService) {
        this.publicationService = publicationService;
        this.validationService = validationService;
        this.channelManagementService = channelManagementService;
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
        @ApiResponse(code = 403, message = UNAUTHORIZED_DESCRIPTION)
    })
    @ApiOperation("Upload a new publication")
    @PostMapping
    @Valid
    @IsPublisher
    public ResponseEntity<Artefact> uploadPublication(
        @RequestHeader(PublicationConfiguration.PROVENANCE_HEADER) String provenance,
        @RequestHeader(value = PublicationConfiguration.SOURCE_ARTEFACT_ID_HEADER, required = false)
            String sourceArtefactId,
        @RequestHeader(PublicationConfiguration.TYPE_HEADER) ArtefactType type,
        @RequestHeader(value = PublicationConfiguration.SENSITIVITY_HEADER, required = false) Sensitivity sensitivity,
        @RequestHeader(PublicationConfiguration.LANGUAGE_HEADER) Language language,
        @RequestHeader(value = PublicationConfiguration.DISPLAY_FROM_HEADER, required = false)
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime displayFrom,
        @RequestHeader(value = PublicationConfiguration.DISPLAY_TO_HEADER, required = false)
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime displayTo,
        @RequestHeader(PublicationConfiguration.LIST_TYPE) ListType listType,
        @RequestHeader(PublicationConfiguration.COURT_ID) String courtId,
        @RequestHeader(PublicationConfiguration.CONTENT_DATE)
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime contentDate,
        @RequestHeader(value = "x-issuer-email", required = false) String issuerEmail,
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
            .locationId(headers.getCourtId())
            .contentDate(headers.getContentDate())
            .build();

        Artefact createdItem = publicationService
            .createPublication(artefact, payload);

        channelManagementService.requestFileGeneration(createdItem.getArtefactId());

        logManualUpload(publicationService.maskEmail(issuerEmail), createdItem.getArtefactId().toString());

        publicationService.checkAndTriggerSubscriptionManagement(createdItem);

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
        @ApiResponse(code = 403, message = UNAUTHORIZED_DESCRIPTION)
    })
    @ApiOperation("Upload a new publication")
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @IsPublisher
    public ResponseEntity<Artefact> uploadPublication(
        @RequestHeader(PublicationConfiguration.PROVENANCE_HEADER) String provenance,
        @RequestHeader(value = PublicationConfiguration.SOURCE_ARTEFACT_ID_HEADER, required = false)
            String sourceArtefactId,
        @RequestHeader(PublicationConfiguration.TYPE_HEADER) ArtefactType type,
        @RequestHeader(value = PublicationConfiguration.SENSITIVITY_HEADER, required = false) Sensitivity sensitivity,
        @RequestHeader(PublicationConfiguration.LANGUAGE_HEADER) Language language,
        @RequestHeader(value = PublicationConfiguration.DISPLAY_FROM_HEADER, required = false)
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime displayFrom,
        @RequestHeader(value = PublicationConfiguration.DISPLAY_TO_HEADER, required = false)
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime displayTo,
        @RequestHeader(PublicationConfiguration.LIST_TYPE) ListType listType,
        @RequestHeader(PublicationConfiguration.COURT_ID) String courtId,
        @RequestHeader(PublicationConfiguration.CONTENT_DATE)
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime contentDate,
        @RequestHeader(value = "x-issuer-email", required = false) String issuerEmail,
        @RequestPart MultipartFile file) {

        HeaderGroup initialHeaders = new HeaderGroup(provenance, sourceArtefactId, type, sensitivity, language,
                                                     displayFrom, displayTo, listType, courtId, contentDate
        );
        validationService.validateBody(file);

        HeaderGroup headers = validationService.validateHeaders(initialHeaders);

        Map<String, List<Object>> search = new ConcurrentHashMap<>();
        search.put("location-id", List.of(headers.getCourtId()));

        Artefact artefact = Artefact.builder()
            .provenance(headers.getProvenance())
            .sourceArtefactId(headers.getSourceArtefactId())
            .type(headers.getType())
            .sensitivity(headers.getSensitivity())
            .language(headers.getLanguage())
            .displayFrom(headers.getDisplayFrom())
            .displayTo(headers.getDisplayTo())
            .listType(headers.getListType())
            .locationId(headers.getCourtId())
            .contentDate(headers.getContentDate())
            .isFlatFile(true)
            .search(search)
            .build();

        Artefact createdItem = publicationService.createPublication(artefact, file);

        logManualUpload(publicationService.maskEmail(issuerEmail), createdItem.getArtefactId().toString());

        publicationService.checkAndTriggerSubscriptionManagement(artefact);

        return ResponseEntity.status(HttpStatus.CREATED).body(createdItem);
    }

    @ApiResponses({
        @ApiResponse(code = 200,
            message =
                "List of Artefacts matching the given locationId and verification parameters and date requirements"),
        @ApiResponse(code = 403, message = UNAUTHORIZED_DESCRIPTION),
        @ApiResponse(code = 404,
            message = NOT_FOUND_DESCRIPTION),
    })
    @ApiOperation("Get a series of publications matching a given locationId (e.g. locationId)")
    @GetMapping("/locationId/{locationId}")
    @IsAdmin
    public ResponseEntity<List<Artefact>> getAllRelevantArtefactsByLocationId(
        @PathVariable String locationId,
        @RequestHeader(value = USER_ID_HEADER, required = false) UUID userId,
        @RequestHeader(value = ADMIN_HEADER, defaultValue = DEFAULT_ADMIN_VALUE, required = false) Boolean isAdmin) {
        return ResponseEntity.ok(publicationService.findAllByLocationIdAdmin(locationId, userId, isAdmin));
    }

    @ApiResponses({
        @ApiResponse(code = 200,
            message = "List of Artefacts matching a given case value, verification parameters and date requirements"),
        @ApiResponse(code = 403, message = UNAUTHORIZED_DESCRIPTION),
        @ApiResponse(code = 404,
            message = NOT_FOUND_DESCRIPTION),
    })
    @ApiOperation("Get a series of publications matching a given case search value (e.g. CASE_URN/CASE_ID/CASE_NAME)")
    @GetMapping("/search/{searchTerm}/{searchValue}")
    @IsAdmin
    public ResponseEntity<List<Artefact>> getAllRelevantArtefactsBySearchValue(
        @PathVariable CaseSearchTerm searchTerm, @PathVariable String searchValue,
        @RequestHeader(value = USER_ID_HEADER,  required = false) UUID userId) {
        return ResponseEntity.ok(publicationService.findAllBySearch(searchTerm, searchValue, userId));
    }

    @ApiResponses({
        @ApiResponse(code = 200,
            message = "Gets the artefact metadata",
            response = Artefact.class),
        @ApiResponse(code = 403, message = UNAUTHORIZED_DESCRIPTION),
        @ApiResponse(code = 404,
            message = NOT_FOUND_DESCRIPTION),
    })
    @ApiOperation("Gets the metadata for the blob, given a specific artefact id")
    @GetMapping("/{artefactId}")
    @IsAdmin
    public ResponseEntity<Artefact> getArtefactMetadata(
        @PathVariable UUID artefactId, @RequestHeader(value = USER_ID_HEADER, required = false) UUID userId,
                                       @RequestHeader(value = ADMIN_HEADER, defaultValue = DEFAULT_ADMIN_VALUE,
                                           required = false) Boolean isAdmin) {
        return ResponseEntity.ok(isAdmin ? publicationService.getMetadataByArtefactId(artefactId) :
                                     publicationService.getMetadataByArtefactId(artefactId, userId));
    }

    @ApiResponses({
        @ApiResponse(code = 200,
            message = "Blob data from the given request in text format.",
            response = String.class),
        @ApiResponse(code = 403, message = UNAUTHORIZED_DESCRIPTION),
        @ApiResponse(code = 404,
            message = NOT_FOUND_DESCRIPTION),
    })
    @ApiOperation("Gets the the payload for the blob, given a specific artefact ID")
    @GetMapping("/{artefactId}/payload")
    @IsAdmin
    public ResponseEntity<String> getArtefactPayload(
        @PathVariable UUID artefactId,
        @RequestHeader(value = USER_ID_HEADER, required = false) UUID userId,
        @RequestHeader(value = ADMIN_HEADER, defaultValue = DEFAULT_ADMIN_VALUE, required = false) Boolean isAdmin) {

        return ResponseEntity.ok(isAdmin ? publicationService.getPayloadByArtefactId(artefactId) :
                                     publicationService.getPayloadByArtefactId(artefactId, userId));
    }

    @ApiResponses({
        @ApiResponse(code = 200,
            message = "Blob data from the given request as a file.",
            response = String.class),
        @ApiResponse(code = 403, message = UNAUTHORIZED_DESCRIPTION),
        @ApiResponse(code = 404,
            message = NOT_FOUND_DESCRIPTION),
    })
    @ApiOperation("Gets the the payload for the blob, given a specific artefact ID")
    @GetMapping(value = "/{artefactId}/file", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    @IsAdmin
    public ResponseEntity<Resource> getArtefactFile(
        @PathVariable UUID artefactId,
        @RequestHeader(value = USER_ID_HEADER, required = false) UUID userId,
        @RequestHeader(value = ADMIN_HEADER, defaultValue = DEFAULT_ADMIN_VALUE, required = false) Boolean isAdmin) {

        Resource file;
        Artefact metadata;
        if (isAdmin) {
            file = publicationService.getFlatFileByArtefactID(artefactId);
            metadata = publicationService.getMetadataByArtefactId(artefactId);
        } else {
            file = publicationService.getFlatFileByArtefactID(artefactId, userId);
            metadata = publicationService.getMetadataByArtefactId(artefactId, userId);
        }

        String fileType = metadata.getSourceArtefactId();

        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_OCTET_STREAM_VALUE)
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + fileType)
            .body(file);
    }

    @ApiResponses({
        @ApiResponse(code = 200, message = "Successfully deleted artefact: {artefactId}"),
        @ApiResponse(code = 403, message = UNAUTHORIZED_DESCRIPTION),
        @ApiResponse(code = 404, message = "No artefact found with the ID: {artefactId}"),
    })
    @ApiOperation("Delete a artefact and its list from P&I")
    @DeleteMapping("/{artefactId}")
    @IsAdmin
    public ResponseEntity<String> deleteArtefact(@RequestHeader("x-issuer-id") String issuerId,
        @PathVariable String artefactId) {
        publicationService.deleteArtefactById(artefactId, issuerId);
        return ResponseEntity.ok("Successfully deleted artefact: " + artefactId);
    }

    @ApiResponses({
        @ApiResponse(code = 200, message = "{Location type associated with given list type}")
    })
    @ApiOperation("Return the Location type associated with a given list type")
    @GetMapping("/location-type/{listType}")
    public ResponseEntity<LocationType> getLocationType(@PathVariable ListType listType) {
        return ResponseEntity.ok(publicationService.getLocationType(listType));
    }

    private void logManualUpload(String issuerId, String artefactId) {
        if (issuerId != null) {
            log.info(writeLog(issuerId, UserActions.UPLOAD, artefactId));
        }
    }

    @ApiResponses({
        @ApiResponse(code = 200, message = "Data Management - MI Data request accepted.")
    })
    @ApiOperation("Return the table of MI data")
    @GetMapping("/mi-data")
    @IsAdmin
    public ResponseEntity<String> getMiData() {
        return ResponseEntity.ok().body(publicationService.getMiData());
    }

    @ApiResponses({
        @ApiResponse(code = 204, message = NO_CONTENT_DESCRIPTION),
        @ApiResponse(code = 403, message = UNAUTHORIZED_DESCRIPTION)
    })
    @ApiOperation("Find latest artefacts from today and send them to subscribers")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PostMapping("/latest/subscription")
    @IsAdmin
    public ResponseEntity<Void> sendNewArtefactsForSubscription() {
        publicationService.checkNewlyActiveArtefacts();
        return ResponseEntity.noContent().build();
    }

    @ApiResponses({
        @ApiResponse(code = 204, message = NO_CONTENT_DESCRIPTION),
        @ApiResponse(code = 403, message = UNAUTHORIZED_DESCRIPTION)
    })
    @ApiOperation("Report artefacts which do not match any location")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PostMapping("/no-match/reporting")
    @IsAdmin
    public ResponseEntity<Void> reportNoMatchArtefacts() {
        publicationService.reportNoMatchArtefacts();
        return ResponseEntity.noContent().build();
    }

    @ApiResponses({
        @ApiResponse(code = 204, message = NO_CONTENT_DESCRIPTION),
        @ApiResponse(code = 403, message = UNAUTHORIZED_DESCRIPTION)
    })
    @ApiOperation("Delete all expired artefacts")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping("/expired")
    @IsAdmin
    public ResponseEntity<Void> deleteExpiredArtefacts() {
        publicationService.deleteExpiredArtefacts();
        return ResponseEntity.noContent().build();
    }
}
