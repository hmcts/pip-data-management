package uk.gov.hmcts.reform.pip.data.management.models.publication;

import com.vladmihalcea.hibernate.type.json.JsonType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

/**
 * Class that represents the Inbound artifact that is being published.
 */
@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TypeDef(name = "json", typeClass = JsonType.class)
public class Artefact {

    /**
     * Unique ID for publication.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(columnDefinition = "uuid", insertable = false, updatable = false, nullable = false)
    @Type(type = "org.hibernate.type.PostgresUUIDType")
    private UUID artefactId;

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
    @Enumerated(EnumType.STRING)
    private ArtefactType type;

    /**
     * Level of sensitivity of publication.
     */
    @Enumerated(EnumType.STRING)
    private Sensitivity sensitivity;

    /**
     * Language of publication.
     */
    @Enumerated(EnumType.STRING)
    private Language language;

    /**
     * Metadata that will be indexed for searching.
     */
    @Type(type = "json")
    @Column(columnDefinition = "json")
    private Map<String, List<Object>> search;

    /**
     * Date / Time from which the publication will be displayed.
     */
    private LocalDateTime displayFrom;

    /**
     * Date / Time until which the publication will be displayed.
     */
    private LocalDateTime displayTo;

    /**
     * The type of list.
     */
    @Enumerated(EnumType.STRING)
    private ListType listType;

    /**
     * Court Id based on the source system (provenance).
     */
    private String locationId;

    /**
     * Date / Time the publication is referring to.
     */
    private LocalDateTime contentDate;

    /**
     * Bool to signal if the payload is a flat file or raw data.
     */
    @Builder.Default
    private Boolean isFlatFile = false;

    /**
     * The URL for the payload in the Azure Blob Service.
     */
    private String payload;

}
