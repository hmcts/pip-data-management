package uk.gov.hmcts.reform.pip.data.management.models.location;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.pip.model.location.LocationType;

import java.util.UUID;

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
