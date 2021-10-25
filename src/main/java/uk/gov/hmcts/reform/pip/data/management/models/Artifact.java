package uk.gov.hmcts.reform.pip.data.management.models;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Class that presents the Inbound artifact that is being published
 */
@Data
public class Artifact {

    public Artifact(Map<String, String> headers, String payload) {

        this.artifactId = headers.get("x-artifact-id");
        this.provenance = headers.get("x-provenance");
        this.sourceArtifactId = headers.get("x-source-artifact-id");
        this.payload = payload;
    }

    /**
     * Unique ID for publication.
     */
    private String artifactId;

    /**
     * Name of source system.
     */
    private String provenance;

    /**
     * Unique of ID of what publication is called by source system
     */
    private String sourceArtifactId;

    /**
     * List / Outcome / Judgement / Status Update
     */
    private String type;

    /**
     * Level of sensitivity of publication.
     */
    private String sensitivity;

    /**
     * Language of publication.
     */
    private String language;

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
