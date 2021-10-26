package uk.gov.hmcts.reform.pip.data.management.models.publication;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Class that presents the Inbound artifact that is being published
 */
@Data
@Builder
public class Artefact {

    /**
     * Unique ID for publication.
     */
    private String artefactId;

    /**
     * Name of source system.
     */
    private String provenance;

    /**
     * Unique of ID of what publication is called by source system
     */
    private String sourceArtefactId;

    /**
     * List / Outcome / Judgement / Status Update
     */
    private ArtefactType type;

    /**
     * Level of sensitivity of publication.
     */
    private Sensitivity sensitivity;

    /**
     * Language of publication.
     */
    private Language language;

    /**
     * Metadata that will be indexed for searching.
     */
    private String search;

    /**
     * Date / Time from which the publication will be displayed.
     */
    private LocalDateTime displayFrom;

    /**
     * Date / Time until which the publication will be displayed.
     */
    private LocalDateTime displayTo;

    /**
     * JSON blob with Key/Value pairs of data to be published.
     */
    private String payload;

}
