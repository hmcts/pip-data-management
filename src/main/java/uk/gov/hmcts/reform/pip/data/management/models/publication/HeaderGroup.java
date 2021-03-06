package uk.gov.hmcts.reform.pip.data.management.models.publication;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;
import javax.persistence.Enumerated;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * Class that represents a group of headers to be validated in the ValidationService.
 */
@Data
@AllArgsConstructor
public class HeaderGroup {

    /**
     * Name of source system.
     */
    @Valid
    @NotNull
    @NotBlank
    private String provenance;

    /**
     * Unique of ID of what publication is called by source system.
     */
    @Valid
    @NotNull
    @NotBlank
    private String sourceArtefactId;

    /**
     * List / Outcome / Judgement / Status Update.
     */
    @Enumerated
    @NotBlank
    private ArtefactType type;

    /**
     * Level of sensitivity of publication.
     */
    @Enumerated
    private Sensitivity sensitivity;

    /**
     * Language of publication.
     */
    @Enumerated
    private Language language;

    /**
     * Date / Time from which the publication will be displayed.
     */
    @Valid
    private LocalDateTime displayFrom;

    /**
     * Date / Time until which the publication will be displayed.
     */
    @Valid
    private LocalDateTime displayTo;

    /**
     * DL / SL / WL / FL / PL / SJP.
     */
    @Enumerated
    private ListType listType;

    /**
     * The court id from the source system.
     */
    @Valid
    @NotNull
    @NotBlank
    private String courtId;

    /**
     * The local date time the publication is referring to.
     */
    @Valid
    private LocalDateTime contentDate;

}
