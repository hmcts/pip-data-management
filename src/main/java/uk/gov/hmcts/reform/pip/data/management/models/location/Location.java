package uk.gov.hmcts.reform.pip.data.management.models.location;

import com.fasterxml.jackson.annotation.JsonView;
import com.vladmihalcea.hibernate.type.array.ListArrayType;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Type;
import uk.gov.hmcts.reform.pip.model.location.LocationCsv;
import uk.gov.hmcts.reform.pip.model.location.LocationType;

import java.util.ArrayList;
import java.util.List;

/**
 * This class captures the Location Data, which will be persisted in the database.
 */
@Entity
@Data
@NoArgsConstructor
@Table(name = "location")
public class Location {

    private static final String LIST_ARRAY = "list-array";
    private static final String DEFINITION = "text[]";

    @Id
    @JsonView(LocationViews.BaseView.class)
    private Integer locationId;

    @JsonView(LocationViews.BaseView.class)
    private String name;

    @Type(ListArrayType.class)
    @Column(name = "region", columnDefinition = DEFINITION)
    @JsonView(LocationViews.BaseView.class)
    private List<String> region;

    @JsonView(LocationViews.BaseView.class)
    @Enumerated(EnumType.STRING)
    @NotNull
    private LocationType locationType;

    @Type(ListArrayType.class)
    @Column(name = "jurisdiction", columnDefinition = DEFINITION)
    @JsonView(LocationViews.BaseView.class)
    private List<String> jurisdiction;

    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "location_id")
    @JsonView(LocationViews.ReferenceView.class)
    private List<LocationReference> locationReferenceList = new ArrayList<>();

    @JsonView(LocationViews.BaseView.class)
    private String welshName;

    @Type(ListArrayType.class)
    @Column(name = "welsh_jurisdiction", columnDefinition = DEFINITION)
    @JsonView(LocationViews.BaseView.class)
    private List<String> welshJurisdiction;

    @Type(ListArrayType.class)
    @Column(name = "welsh_region", columnDefinition = DEFINITION)
    @JsonView(LocationViews.BaseView.class)
    private List<String> welshRegion;

    @JsonView(LocationViews.BaseView.class)
    private String email;

    @JsonView(LocationViews.BaseView.class)
    private String contactNo;

    public Location(LocationCsv locationCsv) {
        this.locationId = locationCsv.getUniqueId();
        this.name = locationCsv.getLocationName();
        this.region = new ArrayList<>(locationCsv.getRegion());
        this.jurisdiction = new ArrayList<>(locationCsv.getJurisdiction());
        this.locationType = LocationType.valueOfCsv(locationCsv.getProvenanceLocationType());
        LocationReference locationReference = new LocationReference(
            locationCsv.getProvenance(),
            locationCsv.getProvenanceLocationId(),
            LocationType.valueOfCsv(locationCsv.getProvenanceLocationType()));
        this.locationReferenceList.add(locationReference);
        this.welshName = locationCsv.getWelshLocationName();
        this.welshRegion = new ArrayList<>(locationCsv.getWelshRegion());
        this.welshJurisdiction = new ArrayList<>(locationCsv.getWelshJurisdiction());
        this.email = locationCsv.getEmail();
        this.contactNo = locationCsv.getContactNo();
    }

    /**
     * Adds a new LocationReference to the reference table.
     * @param locationReference The location reference object to add.
     */
    public void addLocationReference(LocationReference locationReference) {
        locationReferenceList.add(locationReference);
    }
}
