package uk.gov.hmcts.reform.pip.data.management.models.publication;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonView;
import com.vladmihalcea.hibernate.type.json.JsonType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Type;
import uk.gov.hmcts.reform.pip.data.management.models.publication.views.ArtefactView;
import uk.gov.hmcts.reform.pip.model.publication.ArtefactType;
import uk.gov.hmcts.reform.pip.model.publication.Language;
import uk.gov.hmcts.reform.pip.model.publication.ListType;
import uk.gov.hmcts.reform.pip.model.publication.Sensitivity;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Class that represents the Inbound artifact that is being published.
 */
@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@SuppressWarnings("PMD.TooManyFields")
public class Artefact {

    /**
     * Unique ID for publication.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(columnDefinition = "uuid", insertable = false, updatable = false, nullable = false)
    @JsonView(ArtefactView.External.class)
    private UUID artefactId;

    /**
     * Name of source system.
     */
    @JsonView(ArtefactView.External.class)
    private String provenance;

    /**
     * Unique of ID of what publication is called by source system.
     */
    @JsonView(ArtefactView.External.class)
    private String sourceArtefactId;

    /**
     * List / Outcome / Judgement / Status Update.
     */
    @Enumerated(EnumType.STRING)
    @JsonView(ArtefactView.External.class)
    private ArtefactType type;

    /**
     * Level of sensitivity of publication.
     */
    @Enumerated(EnumType.STRING)
    @JsonView(ArtefactView.External.class)
    private Sensitivity sensitivity;

    /**
     * Language of publication.
     */
    @Enumerated(EnumType.STRING)
    @JsonView(ArtefactView.External.class)
    private Language language;

    /**
     * Metadata that will be indexed for searching.
     */
    @Type(JsonType.class)
    @Column(columnDefinition = "json")
    @JsonView(ArtefactView.External.class)
    private Map<String, List<Object>> search;

    /**
     * Date / Time from which the publication will be displayed.
     */
    @JsonView(ArtefactView.External.class)
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'")
    private LocalDateTime displayFrom;

    /**
     * Date / Time until which the publication will be displayed.
     */
    @JsonView(ArtefactView.External.class)
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'")
    private LocalDateTime displayTo;

    /**
     * The type of list.
     */
    @Enumerated(EnumType.STRING)
    @JsonView(ArtefactView.External.class)
    private ListType listType;

    /**
     * Court Id based on the source system (provenance).
     */
    @JsonView(ArtefactView.External.class)
    private String locationId;

    /**
     * Date / Time the publication is referring to.
     */
    @JsonView(ArtefactView.External.class)
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'")
    private LocalDateTime contentDate;

    /**
     * Bool to signal if the payload is a flat file or raw data.
     */
    @Builder.Default
    @JsonView(ArtefactView.External.class)
    private Boolean isFlatFile = false;

    /**
     * The URL for the payload in the Azure Blob Service.
     */
    @JsonView(ArtefactView.External.class)
    private String payload;

    /**
     * A marker to show whether the artefact is archived.
     */
    @Builder.Default
    @JsonView(ArtefactView.Internal.class)
    private Boolean isArchived = false;

    /**
     * Date/Time to indicate when the artefact was last received.
     */
    @JsonView(ArtefactView.Internal.class)
    private LocalDateTime lastReceivedDate;

    /**
     * A counter to show how many times the artefact has been superseded. Default is 0
     */
    @JsonView(ArtefactView.Internal.class)
    @Column(nullable = false, columnDefinition = "INTEGER DEFAULT 0")
    private int supersededCount;

    /**
     * Date / Time of when the artefact will expire.
     */
    private LocalDateTime expiryDate;

}
