package uk.gov.hmcts.reform.pip.data.management.controllers.publication;

import com.fasterxml.jackson.annotation.JsonView;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
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
import org.springframework.security.access.prepost.PreAuthorize;
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
import uk.gov.hmcts.reform.pip.data.management.helpers.EmailHelper;
import uk.gov.hmcts.reform.pip.data.management.helpers.NoMatchArtefactHelper;
import uk.gov.hmcts.reform.pip.data.management.models.publication.Artefact;
import uk.gov.hmcts.reform.pip.data.management.models.publication.HeaderGroup;
import uk.gov.hmcts.reform.pip.data.management.models.publication.views.ArtefactView;
import uk.gov.hmcts.reform.pip.data.management.service.ExcelConversionService;
import uk.gov.hmcts.reform.pip.data.management.service.ValidationService;
import uk.gov.hmcts.reform.pip.data.management.service.publication.PublicationCreationRunner;
import uk.gov.hmcts.reform.pip.data.management.service.publication.PublicationCreationService;
import uk.gov.hmcts.reform.pip.data.management.service.publication.PublicationRemovalService;
import uk.gov.hmcts.reform.pip.data.management.service.publication.PublicationRetrievalService;
import uk.gov.hmcts.reform.pip.model.authentication.roles.IsAdmin;
import uk.gov.hmcts.reform.pip.model.enums.UserActions;
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
@SecurityRequirement(name = "bearerAuth")
@SuppressWarnings({"PMD.ExcessiveImports", "PMD.CouplingBetweenObjects"})
public class PublicationController {
    private static final String ADMIN_HEADER = "x-admin";

    private static final String NO_CONTENT_DESCRIPTION = "The request has been successfully fulfilled";
    private static final String NOT_FOUND_DESCRIPTION =
        "No artefact found matching given parameters and date requirements";
    private static final String UNAUTHORISED_MESSAGE = "Invalid access credential";
    private static final String FORBIDDEN_MESSAGE = "User has not been authorized";
    private static final String CONFLICT_MESSAGE = "Conflict while uploading publication";

    private static final String OK_CODE = "200";
    private static final String NOT_FOUND_CODE = "404";
    private static final String NO_CONTENT_CODE = "204";
    private static final String UNAUTHORISED_CODE = "401";
    private static final String FORBIDDEN_CODE = "403";
    private static final String CONFLICT_CODE = "409";

    private static final String DEFAULT_ADMIN_VALUE = "false";
    private static final String REQUESTER_ID_HEADER = "x-requester-id";

    private final PublicationCreationService publicationCreationService;
    private final PublicationCreationRunner publicationCreationRunner;
    private final PublicationRetrievalService publicationRetrievalService;
    private final PublicationRemovalService publicationRemovalService;
    private final ValidationService validationService;
    private final ExcelConversionService excelConversionService;

    /**
     * Constructor for Publication controller.
     *
     * @param publicationCreationService    The service that contains the business logic to handle
     *                                      creation of publications.
     * @param publicationCreationRunner The service class that runs the publication creation process
     * @param publicationRetrievalService   The service used to retrieval publication and publication property
     * @param publicationRemovalService The service used to Delete or Archive artefacts
     * @param validationService The service that handle input validation of publications
     * @param excelConversionService The service handles conversion of Excel data to JSON format
     */
    @Autowired
    public PublicationController(PublicationCreationService publicationCreationService,
                                 PublicationCreationRunner publicationCreationRunner,
                                 ValidationService validationService,
                                 PublicationRetrievalService publicationRetrievalService,
                                 PublicationRemovalService publicationRemovalService,
                                 ExcelConversionService excelConversionService) {
        this.publicationCreationService = publicationCreationService;
        this.publicationCreationRunner = publicationCreationRunner;
        this.validationService = validationService;
        this.publicationRetrievalService = publicationRetrievalService;
        this.publicationRemovalService = publicationRemovalService;
        this.excelConversionService = excelConversionService;
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
    @ApiResponse(responseCode = UNAUTHORISED_CODE, description = UNAUTHORISED_MESSAGE)
    @ApiResponse(responseCode = FORBIDDEN_CODE, description = FORBIDDEN_MESSAGE)
    @ApiResponse(responseCode = CONFLICT_CODE, description = CONFLICT_MESSAGE)
    @Operation(summary = "Upload a new publication")
    @PostMapping
    @Valid
    @JsonView(ArtefactView.External.class)
    @PreAuthorize("@authorisationService.userCanUploadPublication(#requesterId, #provenance)")
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
        @RequestHeader(value = REQUESTER_ID_HEADER, required = false) String requesterId,
        @RequestBody String payload) {

        HeaderGroup initialHeaders = new HeaderGroup(provenance, sourceArtefactId, type, sensitivity, language,
                                                     displayFrom, displayTo, listType, courtId, contentDate
        );

        HeaderGroup headers = validationService.validateHeaders(initialHeaders);
        validationService.validateBody(payload, initialHeaders, validateMasterSchema(listType));
        Artefact artefact = createPublicationMetadataFromHeaders(headers, payload.length());

        Artefact createdItem = publicationCreationRunner.run(artefact, payload, true);
        logManualUpload(EmailHelper.maskEmail(requesterId), createdItem.getArtefactId().toString());

        // Process the created artefact to generate PDF/Excel files and check/trigger the subscription process
        if (!NoMatchArtefactHelper.isNoMatchLocationId(createdItem.getLocationId())) {
            publicationCreationService.processCreatedPublication(createdItem, payload);
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
    @ApiResponse(responseCode = UNAUTHORISED_CODE, description = UNAUTHORISED_MESSAGE)
    @ApiResponse(responseCode = FORBIDDEN_CODE, description = FORBIDDEN_MESSAGE)
    @ApiResponse(responseCode = CONFLICT_CODE, description = CONFLICT_MESSAGE)
    @Operation(summary = "Upload a new publication")
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @JsonView(ArtefactView.External.class)
    @PreAuthorize("@authorisationService.userCanUploadPublication(#requesterId, #provenance)")
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
        @RequestHeader(value = REQUESTER_ID_HEADER, required = false) String requesterId,
        @RequestPart MultipartFile file) {

        HeaderGroup initialHeaders = new HeaderGroup(provenance, sourceArtefactId, type, sensitivity, language,
                                                     displayFrom, displayTo, listType, courtId, contentDate
        );
        validationService.validateBody(file);

        HeaderGroup headers = validationService.validateHeaders(initialHeaders);
        Artefact artefact = createPublicationMetadataFromHeaders(headers, file.getSize());

        Map<String, List<Object>> search = new ConcurrentHashMap<>();
        search.put("location-id", List.of(headers.getCourtId()));
        artefact.setSearch(search);
        artefact.setIsFlatFile(true);

        Artefact createdItem =  publicationCreationRunner.run(artefact, file);
        logManualUpload(EmailHelper.maskEmail(requesterId), createdItem.getArtefactId().toString());

        if (!NoMatchArtefactHelper.isNoMatchLocationId(createdItem.getLocationId())) {
            publicationCreationService.processCreatedPublication(createdItem);
        }

        return ResponseEntity.status(HttpStatus.CREATED).body(createdItem);
    }

    /**
     * Non-strategic create publication from Excel input file.
     *
     * @param provenance       Name of the source system.
     * @param sourceArtefactId Unique ID of what publication is called by source system.
     * @param type             List / Outcome / Judgement / Status Updates.
     * @param sensitivity      Level of sensitivity.
     * @param language         Language of publication.
     * @param displayFrom      Date / Time from which the publication will be displayed.
     * @param displayTo        Date / Time until which the publication will be displayed.
     * @param listType         Publication list type.
     * @param courtId          Source systems court id.
     * @param contentDate      Local date time for when the publication is referring to start.
     * @param file             The Excel file to be uploaded.
     * @return The created artefact.
     */
    @ApiResponse(responseCode = "201", description = "Artefact.class instance for the artefact that has been "
        + "created")
    @ApiResponse(responseCode = "400", description = "Error converting Excel file into JSON format")
    @ApiResponse(responseCode = UNAUTHORISED_CODE, description = UNAUTHORISED_MESSAGE)
    @ApiResponse(responseCode = FORBIDDEN_CODE, description = FORBIDDEN_MESSAGE)
    @ApiResponse(responseCode = CONFLICT_CODE, description = CONFLICT_MESSAGE)
    @Operation(summary = "Non-strategic - upload a new publication")
    @PostMapping(value = "/non-strategic", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @JsonView(ArtefactView.External.class)
    @PreAuthorize("@authorisationService.userCanUploadPublication(#requesterId, #provenance)")
    public ResponseEntity<Artefact> nonStrategicUploadPublication(
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
        @RequestHeader(value = REQUESTER_ID_HEADER, required = false) String requesterId,
        @RequestPart MultipartFile file) {
        HeaderGroup initialHeaders = new HeaderGroup(provenance, sourceArtefactId, type, sensitivity, language,
                                                     displayFrom, displayTo, listType, courtId, contentDate);

        HeaderGroup headers = validationService.validateHeaders(initialHeaders);

        String payload = excelConversionService.convert(file);
        validationService.validateBody(payload, initialHeaders, false);

        Artefact artefact = createPublicationMetadataFromHeaders(headers, payload.length());

        Artefact createdItem = publicationCreationRunner.run(artefact, payload, false);
        logManualUpload(EmailHelper.maskEmail(requesterId), createdItem.getArtefactId().toString());

        // Process the created artefact to generate PDF and check/trigger the subscription process
        if (!NoMatchArtefactHelper.isNoMatchLocationId(createdItem.getLocationId())) {
            publicationCreationService.processCreatedPublication(createdItem, payload);
        }

        return ResponseEntity.status(HttpStatus.CREATED).body(createdItem);
    }

    @ApiResponse(responseCode = OK_CODE, description = "Gets the artefact metadata")
    @ApiResponse(responseCode = UNAUTHORISED_CODE, description = UNAUTHORISED_MESSAGE)
    @ApiResponse(responseCode = FORBIDDEN_CODE, description = FORBIDDEN_MESSAGE)
    @ApiResponse(responseCode = NOT_FOUND_CODE, description = NOT_FOUND_DESCRIPTION)
    @Operation(summary = "Gets the metadata for the blob, given a specific artefact id")
    @GetMapping("/{artefactId}")
    @JsonView(ArtefactView.Internal.class)
    @PreAuthorize("@authorisationService.userCanAccessPublicationData(#requesterId, #artefactId, #isAdmin)")
    public ResponseEntity<Artefact> getArtefactMetadata(
        @PathVariable UUID artefactId,
        @RequestHeader(value = REQUESTER_ID_HEADER, required = false) UUID requesterId,
        @RequestHeader(value = ADMIN_HEADER, defaultValue = DEFAULT_ADMIN_VALUE,
            required = false) Boolean isAdmin) {
        return ResponseEntity.ok(isAdmin.equals(Boolean.TRUE)
                                    ? publicationRetrievalService.getMetadataByArtefactId(artefactId) :
                                     publicationRetrievalService.getMetadataByArtefactId(artefactId, requesterId));
    }

    @ApiResponse(responseCode = OK_CODE, description = "Blob data from the given request in text format.")
    @ApiResponse(responseCode = UNAUTHORISED_CODE, description = UNAUTHORISED_MESSAGE)
    @ApiResponse(responseCode = FORBIDDEN_CODE, description = FORBIDDEN_MESSAGE)
    @ApiResponse(responseCode = NOT_FOUND_CODE, description = NOT_FOUND_DESCRIPTION)
    @Operation(summary = "Gets the the payload for the blob, given a specific artefact ID")
    @GetMapping("/{artefactId}/payload")
    @PreAuthorize("@authorisationService.userCanAccessPublicationData(#requesterId, #artefactId, #isAdmin)")
    public ResponseEntity<String> getArtefactPayload(
        @PathVariable UUID artefactId,
        @RequestHeader(value = REQUESTER_ID_HEADER, required = false) UUID requesterId,
        @RequestHeader(value = ADMIN_HEADER, defaultValue = DEFAULT_ADMIN_VALUE, required = false) Boolean isAdmin) {
        return ResponseEntity.ok(isAdmin.equals(Boolean.TRUE)
                                    ? publicationRetrievalService.getPayloadByArtefactId(artefactId) :
                                     publicationRetrievalService.getPayloadByArtefactId(artefactId, requesterId));
    }

    @ApiResponse(responseCode = OK_CODE, description = "Blob data from the given request as a file.")
    @ApiResponse(responseCode = UNAUTHORISED_CODE, description = UNAUTHORISED_MESSAGE)
    @ApiResponse(responseCode = FORBIDDEN_CODE, description = FORBIDDEN_MESSAGE)
    @ApiResponse(responseCode = NOT_FOUND_CODE, description = NOT_FOUND_DESCRIPTION)
    @Operation(summary = "Gets the the payload for the blob, given a specific artefact ID")
    @GetMapping(value = "/{artefactId}/file", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    @PreAuthorize("@authorisationService.userCanAccessPublicationData(#requesterId, #artefactId, #isAdmin)")
    public ResponseEntity<Resource> getArtefactFile(
        @PathVariable UUID artefactId,
        @RequestHeader(value = REQUESTER_ID_HEADER, required = false) UUID requesterId,
        @RequestHeader(value = ADMIN_HEADER, defaultValue = DEFAULT_ADMIN_VALUE, required = false) Boolean isAdmin) {

        Resource file;
        Artefact metadata;
        if (isAdmin.equals(Boolean.TRUE)) {
            file = publicationRetrievalService.getFlatFileByArtefactID(artefactId);
            metadata = publicationRetrievalService.getMetadataByArtefactId(artefactId);
        } else {
            file = publicationRetrievalService.getFlatFileByArtefactID(artefactId, requesterId);
            metadata = publicationRetrievalService.getMetadataByArtefactId(artefactId, requesterId);
        }

        String fileType = metadata.getSourceArtefactId();

        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_OCTET_STREAM_VALUE)
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + fileType)
            .body(file);
    }

    @ApiResponse(responseCode = OK_CODE, description = "Successfully deleted artefact: {artefactId}")
    @ApiResponse(responseCode = UNAUTHORISED_CODE, description = UNAUTHORISED_MESSAGE)
    @ApiResponse(responseCode = FORBIDDEN_CODE, description = FORBIDDEN_MESSAGE)
    @ApiResponse(responseCode = NOT_FOUND_CODE, description = "No artefact found with the ID: {artefactId}")
    @Operation(summary = "Delete a artefact and its list from P&I")
    @DeleteMapping("/{artefactId}")
    @IsAdmin
    public ResponseEntity<String> deleteArtefact(@RequestHeader(REQUESTER_ID_HEADER) String requesterId,
        @PathVariable String artefactId) {
        publicationRemovalService.deleteArtefactById(artefactId, requesterId);
        return ResponseEntity.ok("Successfully deleted artefact: " + artefactId);
    }

    @ApiResponse(responseCode = NO_CONTENT_CODE, description = NO_CONTENT_DESCRIPTION)
    @ApiResponse(responseCode = UNAUTHORISED_CODE, description = UNAUTHORISED_MESSAGE)
    @ApiResponse(responseCode = FORBIDDEN_CODE, description = FORBIDDEN_MESSAGE)
    @Operation(summary = "Archive all expired artefacts")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping("/expired")
    @IsAdmin
    public ResponseEntity<Void> archiveExpiredArtefacts() {
        publicationRemovalService.archiveExpiredArtefacts();
        return ResponseEntity.noContent().build();
    }

    @ApiResponse(responseCode = OK_CODE, description = "Artefact of ID {} has been archived")
    @ApiResponse(responseCode = UNAUTHORISED_CODE, description = UNAUTHORISED_MESSAGE)
    @ApiResponse(responseCode = FORBIDDEN_CODE, description = FORBIDDEN_MESSAGE)
    @ApiResponse(responseCode = NOT_FOUND_CODE, description = "Artefact with ID {} not found when archiving")
    @Operation(summary = "Archive an artefact by ID")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PutMapping("/{id}/archive")
    @PreAuthorize("@authorisationService.userCanArchivePublications(#requesterId)")
    public ResponseEntity<String> archiveArtefact(@RequestHeader(REQUESTER_ID_HEADER) String requesterId,
                                                  @PathVariable String id) {
        publicationRemovalService.archiveArtefactById(id, requesterId);
        return ResponseEntity.ok(String.format("Artefact of ID %s has been archived", id));
    }

    private void logManualUpload(String issuerId, String artefactId) {
        if (issuerId != null) {
            log.info(writeLog(issuerId, UserActions.UPLOAD, artefactId));
        }
    }

    private Artefact createPublicationMetadataFromHeaders(HeaderGroup headers, long fileSizeInBytes) {
        return Artefact.builder()
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
            .payloadSize((float) fileSizeInBytes / 1024)
            .build();
    }

    private boolean validateMasterSchema(ListType listType) {
        return !(listType.equals(ListType.MAGISTRATES_ADULT_COURT_LIST_DAILY)
            || listType.equals(ListType.MAGISTRATES_ADULT_COURT_LIST_FUTURE));
    }
}
