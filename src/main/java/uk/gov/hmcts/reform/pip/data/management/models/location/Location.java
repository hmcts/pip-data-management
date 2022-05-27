package uk.gov.hmcts.reform.pip.data.management.models.location;

import com.fasterxml.jackson.annotation.JsonView;
import com.vladmihalcea.hibernate.type.array.ListArrayType;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.Table;

/**
 * This class captures the Location Data, which will be persisted in the database.
 */
@Entity
@Data
@NoArgsConstructor
@Table(name = "location")
@TypeDef(
    name = "list-array",
    typeClass = ListArrayType.class
)
public class Location {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @JsonView(LocationViews.BaseView.class)
    private Integer locationId;

    @JsonView(LocationViews.BaseView.class)
    private String name;

    @Type(type = "list-array")
    @Column(name = "region", columnDefinition = "text[]")
    @JsonView(LocationViews.BaseView.class)
    private List<String> region;

    @JsonView(LocationViews.BaseView.class)
    @Enumerated(EnumType.STRING)
    private LocationType locationType;

    @Type(type = "list-array")
    @Column(name = "jurisdiction", columnDefinition = "text[]")
    @JsonView(LocationViews.BaseView.class)
    private List<String> jurisdiction;

    @OneToMany(cascade = CascadeType.ALL)
    @JoinColumn(name = "location_id")
    @JsonView(LocationViews.ReferenceView.class)
    private List<LocationReference> locationReferenceList = new ArrayList<>();

    public Location(LocationCsv locationCsv) {
        this.name = locationCsv.getLocationName();
        this.region = locationCsv.getRegion().stream().collect(Collectors.toList());
        this.jurisdiction = locationCsv.getJurisdiction().stream()
            .collect(Collectors.toList());
        this.locationType = LocationType.valueOfCsv(locationCsv.getProvenanceLocationType());
        LocationReference locationReference = new LocationReference(
            locationCsv.getProvenance(),
            locationCsv.getProvenanceLocationId(),
            LocationType.valueOfCsv(locationCsv.getProvenanceLocationType()));
        this.locationReferenceList.add(locationReference);
    }

    /**
     * Adds a new LocationReference to the reference table.
     * @param locationReference The location reference object to add.
     */
    public void addLocationReference(LocationReference locationReference) {
        locationReferenceList.add(locationReference);
    }

}
