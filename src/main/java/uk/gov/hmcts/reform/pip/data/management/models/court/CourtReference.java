package uk.gov.hmcts.reform.pip.data.management.models.court;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Type;

import java.util.UUID;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity
@Data
@NoArgsConstructor
/**
 * This class captures the CourtReference, which contains the source ID for each court.
 */
public class CourtReference {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(columnDefinition = "uuid", insertable = false, updatable = false, nullable = false)
    @Type(type = "org.hibernate.type.PostgresUUIDType")
    private UUID courtReferenceId;

    private String provenance;

    private String provenanceId;

    public CourtReference(String provenance, String provenanceId) {
        this.provenance = provenance;
        this.provenanceId = provenanceId;
    }

}
