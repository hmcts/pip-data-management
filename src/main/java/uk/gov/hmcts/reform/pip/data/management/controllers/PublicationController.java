package uk.gov.hmcts.reform.pip.data.management.controllers;

import com.google.common.base.Strings;
import com.microsoft.applicationinsights.web.dependencies.apachecommons.lang3.EnumUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.pip.data.management.config.PublicationConfiguration;
import uk.gov.hmcts.reform.pip.data.management.errorhandling.exceptions.InvalidPublicationException;
import uk.gov.hmcts.reform.pip.data.management.models.publication.Artefact;
import uk.gov.hmcts.reform.pip.data.management.models.publication.ArtefactType;
import uk.gov.hmcts.reform.pip.data.management.models.publication.Language;
import uk.gov.hmcts.reform.pip.data.management.models.publication.SearchType;
import uk.gov.hmcts.reform.pip.data.management.models.publication.Sensitivity;
import uk.gov.hmcts.reform.pip.data.management.service.PublicationService;

import java.time.LocalDateTime;

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
     * @param publicationService The PublicationService that contains the business logic to handle publications.
     */
    @Autowired
    public PublicationController(PublicationService publicationService) {
        this.publicationService = publicationService;
    }

    /**
     * This endpoint takes in the Artefact, which is split over headers and also the payload body.
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
        @RequestHeader(PublicationConfiguration.PROVENANCE_HEADER) String provenance,
        @RequestHeader(PublicationConfiguration.SOURCE_ARTEFACT_ID_HEADER) String sourceArtefactId,
        @RequestHeader(PublicationConfiguration.TYPE_HEADER) ArtefactType type,
        @RequestHeader(value = PublicationConfiguration.SENSITIVITY_HEADER, required = false) Sensitivity sensitivity,
        @RequestHeader(value = PublicationConfiguration.LANGUAGE_HEADER, required = false) Language language,
        @RequestHeader(value = PublicationConfiguration.SEARCH_HEADER, required = false) String search,
        @RequestHeader(value = PublicationConfiguration.DISPLAY_FROM_HEADER, required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime displayFrom,
        @RequestHeader(value = PublicationConfiguration.DISPLAY_TO_HEADER, required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime displayTo,
        @RequestBody String payload) {

        validateSearchType(search);
        Artefact artefact = Artefact.builder()
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

    /**
     * Validates the search parameter that is coming into the controller.
     * @param search The search parameter that is passed into the controller.
     */
    private void validateSearchType(String search) {
        if (!Strings.isNullOrEmpty(search)) {
            String[] searchArray = search.split("=");

            if (searchArray.length != 2 || !EnumUtils.isValidEnum(SearchType.class, searchArray[0])) {
                throw new InvalidPublicationException(String.format("Invalid search parameter provided %s", search));
            }
        }
    }

}
