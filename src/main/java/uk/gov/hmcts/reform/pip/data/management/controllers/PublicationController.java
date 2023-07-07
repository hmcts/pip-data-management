package uk.gov.hmcts.reform.pip.data.management.controllers;

import com.fasterxml.jackson.annotation.JsonView;
import com.fasterxml.jackson.core.JsonProcessingException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
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
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import uk.gov.hmcts.reform.pip.data.management.config.PublicationConfiguration;
import uk.gov.hmcts.reform.pip.data.management.helpers.LocationHelper;
import uk.gov.hmcts.reform.pip.data.management.models.location.LocationArtefact;
import uk.gov.hmcts.reform.pip.data.management.models.publication.Artefact;
import uk.gov.hmcts.reform.pip.data.management.models.publication.HeaderGroup;
import uk.gov.hmcts.reform.pip.data.management.models.publication.views.ArtefactView;
import uk.gov.hmcts.reform.pip.data.management.service.PublicationService;
import uk.gov.hmcts.reform.pip.data.management.service.ValidationService;
import uk.gov.hmcts.reform.pip.data.management.service.artefact.ArtefactDeleteService;
import uk.gov.hmcts.reform.pip.data.management.service.artefact.ArtefactSearchService;
import uk.gov.hmcts.reform.pip.data.management.service.artefact.ArtefactService;
import uk.gov.hmcts.reform.pip.data.management.service.artefact.ArtefactTriggerService;
import uk.gov.hmcts.reform.pip.data.management.utils.CaseSearchTerm;
import uk.gov.hmcts.reform.pip.model.authentication.roles.IsAdmin;
import uk.gov.hmcts.reform.pip.model.authentication.roles.IsPublisher;
import uk.gov.hmcts.reform.pip.model.enums.UserActions;
import uk.gov.hmcts.reform.pip.model.location.LocationType;
import uk.gov.hmcts.reform.pip.model.publication.ArtefactType;
import uk.gov.hmcts.reform.pip.model.publication.Language;
import uk.gov.hmcts.reform.pip.model.publication.ListType;
import uk.gov.hmcts.reform.pip.model.publication.Sensitivity;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import static uk.gov.hmcts.reform.pip.model.LogBuilder.writeLog;

/**
 * This class is the controller for creating new Publications.
 */

@Slf4j
@Validated
@RestController
@Tag(name = "Data Management Publications API")
@RequestMapping("/publication")
@SuppressWarnings({"PMD.ExcessiveImports"})
public class PublicationController {

    private static final String USER_ID_HEADER = "x-user-id";
    private static final String ADMIN_HEADER = "x-admin";

    private static final String NO_CONTENT_DESCRIPTION = "The request has been successfully fulfilled";
    private static final String UNAUTHORIZED_DESCRIPTION = "User has not been authorized";

    private static final String NOT_FOUND_DESCRIPTION =
        "No artefact found matching given parameters and date requirements";

    private static final String OK_CODE = "200";
    private static final String AUTH_ERROR_CODE = "403";
    private static final String NOT_FOUND_CODE = "404";
    private static final String NO_CONTENT_CODE = "204";

    private final PublicationService publicationService;

    private final ArtefactSearchService artefactSearchService;

    private final ArtefactService artefactService;

    private final ArtefactDeleteService artefactDeleteService;

    private final ArtefactTriggerService artefactTriggerService;
    @Autowired
    private final ValidationService validationService;

    private static final String DEFAULT_ADMIN_VALUE = "false";

    /**
     * Constructor for Publication controller.
     *
     * @param publicationService     The PublicationService that contains the business logic to handle publications.
     * @param artefactService   The ArtefactService that con be used to get artefact property
     * @param artefactDeleteService The ArtefactDeleteService that can be used to Delete or Archive artefacts
     * @param artefactTriggerService The ArtefactTriggerService that can be used to send artefact data to other services
     */
    @Autowired
    public PublicationController(PublicationService publicationService,
                                 ValidationService validationService,
                                 ArtefactSearchService artefactSearchService,
                                 ArtefactService artefactService,
                                 ArtefactDeleteService artefactDeleteService,
                                 ArtefactTriggerService artefactTriggerService) {
        this.publicationService = publicationService;
        this.validationService = validationService;
        this.artefactSearchService = artefactSearchService;
        this.artefactService = artefactService;
        this.artefactDeleteService = artefactDeleteService;
        this.artefactTriggerService = artefactTriggerService;
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
    @ApiResponse(responseCode = "201", description = "Artefact.class instance for the artefact that has been "
            + "created")
    @ApiResponse(responseCode = AUTH_ERROR_CODE, description = UNAUTHORIZED_DESCRIPTION)
    @Operation(summary = "Upload a new publication")
    @PostMapping
    @Valid
    @JsonView(ArtefactView.External.class)
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
        validationService.validateBody(payload, initialHeaders);

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
            .expiryDate(headers.getDisplayTo())
            .build();

        Artefact createdItem = publicationService
            .createPublication(artefact, payload);

        logManualUpload(publicationService.maskEmail(issuerEmail), createdItem.getArtefactId().toString());

        // Process the created artefact by requesting channel management to generate PDF/Excel files
        // and check/trigger subscription management, async.
        if (!LocationHelper.isNoMatchLocationId(createdItem.getLocationId())) {
            publicationService.processCreatedPublication(createdItem);
        }

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
    @ApiResponse(responseCode = "201", description = "Artefact.class instance for the artefact that has been "
            + "created")
    @ApiResponse(responseCode = AUTH_ERROR_CODE, description = UNAUTHORIZED_DESCRIPTION)
    @Operation(summary = "Upload a new publication")
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @JsonView(ArtefactView.External.class)
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
            .expiryDate(headers.getDisplayTo())
            .isFlatFile(true)
            .search(search)
            .build();

        Artefact createdItem = publicationService.createPublication(artefact, file);

        logManualUpload(publicationService.maskEmail(issuerEmail), createdItem.getArtefactId().toString());

        if (!LocationHelper.isNoMatchLocationId(createdItem.getLocationId())) {
            artefactTriggerService.checkAndTriggerSubscriptionManagement(artefact);
        }

        return ResponseEntity.status(HttpStatus.CREATED).body(createdItem);
    }

    @ApiResponse(responseCode = OK_CODE, description = "List of Artefacts matching the given locationId and "
        + "verification parameters and date requirements")
    @ApiResponse(responseCode = AUTH_ERROR_CODE, description = UNAUTHORIZED_DESCRIPTION)
    @ApiResponse(responseCode = NOT_FOUND_CODE, description = NOT_FOUND_DESCRIPTION)
    @Operation(summary = "Get a series of publications matching a given locationId (e.g. locationId)")
    @GetMapping("/locationId/{locationId}")
    @JsonView(ArtefactView.Internal.class)
    @IsAdmin
    public ResponseEntity<List<Artefact>> getAllRelevantArtefactsByLocationId(
        @PathVariable String locationId,
        @RequestHeader(value = USER_ID_HEADER, required = false) UUID userId,
        @RequestHeader(value = ADMIN_HEADER, defaultValue = DEFAULT_ADMIN_VALUE, required = false) Boolean isAdmin) {
        return ResponseEntity.ok(artefactSearchService.findAllByLocationIdAdmin(locationId, userId, isAdmin));
    }

    @ApiResponse(responseCode = OK_CODE, description = "List of Artefacts matching"
            + " a given case value, verification parameters and date requirements")
    @ApiResponse(responseCode = AUTH_ERROR_CODE, description = UNAUTHORIZED_DESCRIPTION)
    @ApiResponse(responseCode = NOT_FOUND_CODE, description = NOT_FOUND_DESCRIPTION)
    @Operation(summary = "Get a series of publications matching a given case search value (e.g. "
        + "CASE_URN/CASE_ID/CASE_NAME)")
    @GetMapping("/search/{searchTerm}/{searchValue}")
    @JsonView(ArtefactView.Internal.class)
    @IsAdmin
    public ResponseEntity<List<Artefact>> getAllRelevantArtefactsBySearchValue(
        @PathVariable CaseSearchTerm searchTerm, @PathVariable String searchValue,
        @RequestHeader(value = USER_ID_HEADER,  required = false) UUID userId) {
        return ResponseEntity.ok(artefactSearchService.findAllBySearch(searchTerm, searchValue, userId));
    }

    @ApiResponse(responseCode = OK_CODE, description = "Gets the artefact metadata")
    @ApiResponse(responseCode = AUTH_ERROR_CODE, description = UNAUTHORIZED_DESCRIPTION)
    @ApiResponse(responseCode = NOT_FOUND_CODE, description = NOT_FOUND_DESCRIPTION)
    @Operation(summary = "Gets the metadata for the blob, given a specific artefact id")
    @GetMapping("/{artefactId}")
    @JsonView(ArtefactView.Internal.class)
    @IsAdmin
    public ResponseEntity<Artefact> getArtefactMetadata(
        @PathVariable UUID artefactId, @RequestHeader(value = USER_ID_HEADER, required = false) UUID userId,
                                       @RequestHeader(value = ADMIN_HEADER, defaultValue = DEFAULT_ADMIN_VALUE,
                                           required = false) Boolean isAdmin) {
        return ResponseEntity.ok(isAdmin.equals(Boolean.TRUE)
                                    ? artefactService.getMetadataByArtefactId(artefactId) :
                                     artefactService.getMetadataByArtefactId(artefactId, userId));
    }

    @ApiResponse(responseCode = OK_CODE, description = "Blob data from the given request in text format.")
    @ApiResponse(responseCode = AUTH_ERROR_CODE, description = UNAUTHORIZED_DESCRIPTION)
    @ApiResponse(responseCode = NOT_FOUND_CODE, description = NOT_FOUND_DESCRIPTION)
    @Operation(summary = "Gets the the payload for the blob, given a specific artefact ID")
    @GetMapping("/{artefactId}/payload")
    @IsAdmin
    public ResponseEntity<String> getArtefactPayload(
        @PathVariable UUID artefactId,
        @RequestHeader(value = USER_ID_HEADER, required = false) UUID userId,
        @RequestHeader(value = ADMIN_HEADER, defaultValue = DEFAULT_ADMIN_VALUE, required = false) Boolean isAdmin) {
        return ResponseEntity.ok(isAdmin.equals(Boolean.TRUE)
                                    ? artefactService.getPayloadByArtefactId(artefactId) :
                                     artefactService.getPayloadByArtefactId(artefactId, userId));
    }

    @ApiResponse(responseCode = OK_CODE, description = "Blob data from the given request as a file.")
    @ApiResponse(responseCode = AUTH_ERROR_CODE, description = UNAUTHORIZED_DESCRIPTION)
    @ApiResponse(responseCode = NOT_FOUND_CODE, description = NOT_FOUND_DESCRIPTION)
    @Operation(summary = "Gets the the payload for the blob, given a specific artefact ID")
    @GetMapping(value = "/{artefactId}/file", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    @IsAdmin
    public ResponseEntity<Resource> getArtefactFile(
        @PathVariable UUID artefactId,
        @RequestHeader(value = USER_ID_HEADER, required = false) UUID userId,
        @RequestHeader(value = ADMIN_HEADER, defaultValue = DEFAULT_ADMIN_VALUE, required = false) Boolean isAdmin) {

        Resource file;
        Artefact metadata;
        if (isAdmin.equals(Boolean.TRUE)) {
            file = artefactService.getFlatFileByArtefactID(artefactId);
            metadata = artefactService.getMetadataByArtefactId(artefactId);
        } else {
            file = artefactService.getFlatFileByArtefactID(artefactId, userId);
            metadata = artefactService.getMetadataByArtefactId(artefactId, userId);
        }

        String fileType = metadata.getSourceArtefactId();

        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_OCTET_STREAM_VALUE)
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + fileType)
            .body(file);
    }

    @ApiResponse(responseCode = OK_CODE, description = "Successfully deleted artefact: {artefactId}")
    @ApiResponse(responseCode = AUTH_ERROR_CODE, description = UNAUTHORIZED_DESCRIPTION)
    @ApiResponse(responseCode = NOT_FOUND_CODE, description = "No artefact found with the ID: {artefactId}")
    @Operation(summary = "Delete a artefact and its list from P&I")
    @DeleteMapping("/{artefactId}")
    @IsAdmin
    public ResponseEntity<String> deleteArtefact(@RequestHeader("x-issuer-id") String issuerId,
        @PathVariable String artefactId) {
        artefactDeleteService.deleteArtefactById(artefactId, issuerId);
        return ResponseEntity.ok("Successfully deleted artefact: " + artefactId);
    }

    @ApiResponse(responseCode = OK_CODE, description = "Data Management - Artefact count per location - request "
            + "accepted.")
    @ApiResponse(responseCode = AUTH_ERROR_CODE, description = UNAUTHORIZED_DESCRIPTION)
    @Operation(summary = "Return a count of artefacts per location")
    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/count-by-location")
    @IsAdmin
    public ResponseEntity<List<LocationArtefact>> countByLocation() {
        return ResponseEntity.ok(artefactService.countArtefactsByLocation());
    }

    @ApiResponse(responseCode = OK_CODE, description = "{Location type associated with given list type}")
    @Operation(summary = "Return the Location type associated with a given list type")
    @GetMapping("/location-type/{listType}")
    public ResponseEntity<LocationType> getLocationType(@PathVariable ListType listType) {
        return ResponseEntity.ok(artefactService.getLocationType(listType));
    }

    private void logManualUpload(String issuerId, String artefactId) {
        if (issuerId != null) {
            log.info(writeLog(issuerId, UserActions.UPLOAD, artefactId));
        }
    }

    @ApiResponse(responseCode = OK_CODE, description = "Data Management - MI Data request accepted.")
    @Operation(summary = "Return the table of MI data")
    @GetMapping("/mi-data")
    @IsAdmin
    public ResponseEntity<String> getMiData() {
        return ResponseEntity.ok().body(publicationService.getMiData());
    }

    @ApiResponse(responseCode = NO_CONTENT_CODE, description = NO_CONTENT_DESCRIPTION)
    @ApiResponse(responseCode = AUTH_ERROR_CODE, description = UNAUTHORIZED_DESCRIPTION)
    @Operation(summary = "Find latest artefacts from today and send them to subscribers")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PostMapping("/latest/subscription")
    @IsAdmin
    public ResponseEntity<Void> sendNewArtefactsForSubscription() {
        artefactTriggerService.checkNewlyActiveArtefacts();
        return ResponseEntity.noContent().build();
    }

    @ApiResponse(responseCode = NO_CONTENT_CODE, description = NO_CONTENT_DESCRIPTION)
    @ApiResponse(responseCode = AUTH_ERROR_CODE, description = UNAUTHORIZED_DESCRIPTION)
    @Operation(summary = "Report artefacts which do not match any location")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PostMapping("/no-match/reporting")
    @IsAdmin
    public ResponseEntity<Void> reportNoMatchArtefacts() {
        artefactTriggerService.reportNoMatchArtefacts();
        return ResponseEntity.noContent().build();
    }

    @ApiResponse(responseCode = NO_CONTENT_CODE, description = NO_CONTENT_DESCRIPTION)
    @ApiResponse(responseCode = AUTH_ERROR_CODE, description = UNAUTHORIZED_DESCRIPTION)
    @Operation(summary = "Archive all expired artefacts")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping("/expired")
    @IsAdmin
    public ResponseEntity<Void> archiveExpiredArtefacts() {
        artefactDeleteService.archiveExpiredArtefacts();
        return ResponseEntity.noContent().build();
    }

    @ApiResponse(responseCode = OK_CODE, description = "Artefact of ID {} has been archived")
    @ApiResponse(responseCode = AUTH_ERROR_CODE, description = UNAUTHORIZED_DESCRIPTION)
    @ApiResponse(responseCode = NOT_FOUND_CODE, description = "Artefact with ID {} not found when archiving")
    @Operation(summary = "Archive an artefact by ID")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PutMapping("/{id}/archive")
    @IsAdmin
    public ResponseEntity<String> archiveArtefact(@RequestHeader("x-issuer-id") String issuerId,
                                                  @PathVariable String id) {
        artefactDeleteService.archiveArtefactById(id, issuerId);
        return ResponseEntity.ok(String.format("Artefact of ID %s has been archived", id));
    }

    @ApiResponse(responseCode = OK_CODE, description = "Successfully deleted artefact for location: {locationId}")
    @ApiResponse(responseCode = AUTH_ERROR_CODE, description = UNAUTHORIZED_DESCRIPTION)
    @ApiResponse(responseCode = NOT_FOUND_CODE, description = "No artefact found with the location ID: {locationId}")
    @Operation(summary = "Delete all artefacts for given location from P&I")
    @DeleteMapping("/{locationId}/deleteArtefacts")
    @IsAdmin
    public ResponseEntity<String> deleteArtefactsByLocation(
        @RequestHeader("x-provenance-user-id") String provenanceUserId,
        @PathVariable Integer locationId) throws JsonProcessingException {
        return ResponseEntity.ok(artefactDeleteService.deleteArtefactByLocation(locationId, provenanceUserId));
    }

    @ApiResponse(responseCode = OK_CODE, description = "List of all artefacts that are noMatch in their id")
    @ApiResponse(responseCode = AUTH_ERROR_CODE, description = UNAUTHORIZED_DESCRIPTION)
    @Operation(summary = "Get all no match publications")
    @GetMapping("/no-match")
    @JsonView(ArtefactView.Internal.class)
    @IsAdmin
    public ResponseEntity<List<Artefact>> getAllNoMatchArtefacts() {
        return ResponseEntity.ok(artefactService.findAllNoMatchArtefacts());
    }
}
