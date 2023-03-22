package uk.gov.hmcts.reform.pip.data.management.models.location;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.pip.model.location.LocationCsv;
import uk.gov.hmcts.reform.pip.model.location.LocationType;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;


class LocationTest {

    private static final String LOCATION_NAME = "Location";
    private static final String WELSH_LOCATION_NAME = "Welsh Location";
    private static final String JURISDICTION = "Jurisdiction";
    private static final String WELSH_JURISDICTION = "Welsh Jurisdiction";
    private static final String REGION = "Region";
    private static final String WELSH_REGION = "Welsh Region";
    private static final Integer UNIQUE_ID = 1;
    private static final String PROVENANCE = "This is a provenance";
    private static final String PROVENANCE_LOCATION_ID = "Provenance Location ID";
    private static final String PROVENANCE_LOCATION_TYPE = "VENUE";

    @Test
    void testCreationLocationViaCsv() {
        LocationCsv locationCsv = new LocationCsv();
        locationCsv.setLocationName(LOCATION_NAME);
        locationCsv.setWelshLocationName(WELSH_LOCATION_NAME);
        locationCsv.setJurisdiction(List.of(JURISDICTION));
        locationCsv.setWelshJurisdiction(List.of(WELSH_JURISDICTION));
        locationCsv.setRegion(List.of(REGION));
        locationCsv.setWelshRegion(List.of(WELSH_REGION));
        locationCsv.setUniqueId(UNIQUE_ID);
        locationCsv.setProvenance(PROVENANCE);
        locationCsv.setProvenanceLocationId(PROVENANCE_LOCATION_ID);
        locationCsv.setProvenanceLocationType(PROVENANCE_LOCATION_TYPE);

        Location location = new Location(locationCsv);

        assertEquals(LOCATION_NAME, location.getName(), "Location name does not match name");
        assertEquals(WELSH_LOCATION_NAME, location.getWelshName(), "Welsh Location name does not match name");
        assertEquals(JURISDICTION, location.getJurisdiction().get(0),
                     "Jurisdiction does not match jurisdiction");
        assertEquals(WELSH_JURISDICTION, location.getWelshJurisdiction().get(0),
                     "Welsh jurisdiction does not match jurisdiction");
        assertEquals(REGION, location.getRegion().get(0), "Region does not match region");
        assertEquals(WELSH_REGION, location.getWelshRegion().get(0), "Welsh region does not match region");
        assertEquals(UNIQUE_ID, location.getLocationId(), "Location ID does not match ID");

        List<LocationReference> locationReferenceList = location.getLocationReferenceList();

        assertEquals(1, locationReferenceList.size(), "Reference list size is incorrect");
        LocationReference locationReference = locationReferenceList.get(0);

        assertEquals(PROVENANCE, locationReference.getProvenance(), "Provenance does not match provenance");
        assertEquals(PROVENANCE_LOCATION_ID, locationReference.getProvenanceLocationId(),
                     "Provenance ID does not match provenance ID");
        assertEquals(LocationType.VENUE, locationReference.getProvenanceLocationType(),
                     "Provenance location type does not match type");
    }

}
