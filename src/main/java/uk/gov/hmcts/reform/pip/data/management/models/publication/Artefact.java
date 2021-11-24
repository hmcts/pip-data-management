package uk.gov.hmcts.reform.pip.data.management.models.publication;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

/**
 * Class that presents the Inbound artifact that is being published.
 */
@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Artefact {

    /**
     * Unique ID for publication.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long artefactId;

    /**
     * Name of source system.
     */
    private String provenance;

    /**
     * Unique of ID of what publication is called by source system.
     */
    private String sourceArtefactId;

    /**
     * List / Outcome / Judgement / Status Update.
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
