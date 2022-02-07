package uk.gov.hmcts.reform.pip.data.management.models.court;

import com.vladmihalcea.hibernate.type.array.ListArrayType;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.text.CaseUtils;
import org.apache.commons.text.WordUtils;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * This class captures the Court Data, which will be persisted in the database.
 */
@Entity
@Data
@NoArgsConstructor
@Table(name = "court")
@TypeDef(
    name = "list-array",
    typeClass = ListArrayType.class
)
public class NewCourt {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(columnDefinition = "uuid", insertable = false, updatable = false, nullable = false)
    @Type(type = "org.hibernate.type.PostgresUUIDType")
    private UUID courtId;

    private String courtName;

    private String region;

    @Type(type = "list-array")
    @Column(name = "jurisdiction", columnDefinition = "text[]")
    private List<String> jurisdiction;

    @OneToMany(cascade = CascadeType.ALL)
    @JoinColumn(name = "court_id")
    private List<CourtReference> courtReferenceList = new ArrayList<>();

    public NewCourt(CourtCsv courtCsv) {
        this.courtName = WordUtils.capitalizeFully(courtCsv.getCourtName());
        this.region = WordUtils.capitalizeFully(courtCsv.getRegion());
        this.jurisdiction = courtCsv.getJurisdiction().stream().map(WordUtils::capitalizeFully)
            .collect(Collectors.toList());

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
