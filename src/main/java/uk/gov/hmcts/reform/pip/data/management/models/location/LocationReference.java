package uk.gov.hmcts.reform.pip.data.management.models.location;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Type;
import uk.gov.hmcts.reform.pip.model.location.LocationType;

import java.util.UUID;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity
@Data
@NoArgsConstructor
/**
 * This class captures the CourtReference, which contains the source ID for each court.
 */
public class LocationReference {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(columnDefinition = "uuid", insertable = false, updatable = false, nullable = false)
    @Type(type = "org.hibernate.type.PostgresUUIDType")
    private UUID locationReferenceId;

    /**
     * The name of the source system.
     */
    private String provenance;

    /**
     * The id of the location as referred to by the source system.
     */
    private String provenanceLocationId;

    /**
     * The location type referred to by the location id, eg, the VENUE id of 3 rather than the REGION id of 3.
     */
    @Enumerated(EnumType.STRING)
    private LocationType provenanceLocationType;

    public LocationReference(String provenance, String provenanceLocationId, LocationType provenanceLocationType) {
        this.provenance = provenance;
        this.provenanceLocationId = provenanceLocationId;
        this.provenanceLocationType = provenanceLocationType;
    }

}
