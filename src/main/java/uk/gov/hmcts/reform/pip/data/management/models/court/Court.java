package uk.gov.hmcts.reform.pip.data.management.models.court;

import com.vladmihalcea.hibernate.type.array.ListArrayType;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.text.WordUtils;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.Table;

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
public class Court {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer courtId;

    private String name;

    private String region;

    @Type(type = "list-array")
    @Column(name = "jurisdiction", columnDefinition = "text[]")
    private List<String> jurisdiction;

    @OneToMany(cascade = CascadeType.ALL)
    @JoinColumn(name = "court_id")
    private List<CourtReference> courtReferenceList = new ArrayList<>();

    public Court(CourtCsv courtCsv) {
        this.name = WordUtils.capitalizeFully(courtCsv.getCourtName());
        this.region = WordUtils.capitalizeFully(courtCsv.getRegion());
        this.jurisdiction = courtCsv.getJurisdiction().stream().map(WordUtils::capitalizeFully)
            .collect(Collectors.toList());

        CourtReference courtReference = new CourtReference(courtCsv.getProvenance(), courtCsv.getProvenanceId());
        this.courtReferenceList.add(courtReference);
    }

    /**
     * Adds a new CourtReference to the reference table.
     * @param courtReference The court reference object to add.
     */
    public void addCourtReference(CourtReference courtReference) {
        courtReferenceList.add(courtReference);
    }

}
