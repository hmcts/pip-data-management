package uk.gov.hmcts.reform.pip.data.management.models.court;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Type;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * This class captures the Court Data, which will be persisted in the database.
 */
@Entity
@Data
@NoArgsConstructor
@Table(name = "court")
public class NewCourt {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(columnDefinition = "uuid", insertable = false, updatable = false, nullable = false)
    @Type(type = "org.hibernate.type.PostgresUUIDType")
    private UUID courtId;

    private String courtName;

    private String region;

    private String jurisdiction;

    @OneToMany(cascade = CascadeType.ALL)
    @JoinColumn(name = "new_court_id")
    private List<CourtReference> courtReferenceList = new ArrayList<>();

    public NewCourt(CourtCsv courtCsv) {
        this.courtName = courtCsv.getCourtName();
        this.region = courtCsv.getRegion();
        this.jurisdiction = courtCsv.getJurisdiction();

        CourtReference courtReference = new CourtReference(courtCsv.getProvenance(), courtCsv.getProvenanceId());
        this.courtReferenceList.add(courtReference);
    }

    /**
     * Adds a new CourtReference to the reference table
     * @param courtReference The court reference object to add.
     */
    public void addCourtReference(CourtReference courtReference) {
        courtReferenceList.add(courtReference);
    }

}
