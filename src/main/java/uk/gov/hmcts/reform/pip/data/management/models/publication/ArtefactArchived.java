package uk.gov.hmcts.reform.pip.data.management.models.publication;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.pip.model.publication.ArtefactType;
import uk.gov.hmcts.reform.pip.model.publication.Language;
import uk.gov.hmcts.reform.pip.model.publication.ListType;
import uk.gov.hmcts.reform.pip.model.publication.Sensitivity;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Data
@NoArgsConstructor
@Table(name = "artefact_archived")
public class ArtefactArchived {
    @Id
    @Column(name = "artefact_id", nullable = false)
    private UUID artefactId;

    @Column(name = "content_date")
    private LocalDateTime contentDate;

    @Column(name = "display_from")
    private LocalDateTime displayFrom;

    @Column(name = "display_to")
    private LocalDateTime displayTo;

    @Column(name = "is_flat_file")
    private Boolean isFlatFile;

    @Column(name = "language")
    private Language language;

    @Column(name = "list_type")
    private ListType listType;

    @Column(name = "location_id")
    private String locationId;

    @Column(name = "provenance")
    private String provenance;

    @Column(name = "sensitivity")
    private Sensitivity sensitivity;

    @Column(name = "type")
    private ArtefactType type;

    @Column(name = "last_received_date")
    private LocalDateTime lastReceivedDate;

    @Column(name = "superseded_count")
    private Integer supersededCount;

    @Column(name = "archived_date")
    private LocalDateTime archivedDate;

    @Column(name = "is_manually_deleted", nullable = false)
    private Boolean isManuallyDeleted = false;

    public ArtefactArchived(Artefact artefact, boolean isManuallyDeleted) {
        this.setArtefactId(artefact.getArtefactId());
        this.setContentDate(artefact.getContentDate());
        this.setDisplayFrom(artefact.getDisplayFrom());
        this.setDisplayTo(artefact.getDisplayTo());
        this.setIsFlatFile(artefact.getIsFlatFile());
        this.setLanguage(artefact.getLanguage());
        this.setListType(artefact.getListType());
        this.setLocationId(artefact.getLocationId());
        this.setProvenance(artefact.getProvenance());
        this.setSensitivity(artefact.getSensitivity());
        this.setType(artefact.getType());
        this.setLastReceivedDate(artefact.getLastReceivedDate());
        this.setSupersededCount(artefact.getSupersededCount());
        this.setArchivedDate(LocalDateTime.now());
        this.setIsManuallyDeleted(isManuallyDeleted);
    }
}
