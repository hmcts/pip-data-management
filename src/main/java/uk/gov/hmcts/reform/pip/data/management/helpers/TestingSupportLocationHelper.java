package uk.gov.hmcts.reform.pip.data.management.helpers;

import uk.gov.hmcts.reform.pip.data.management.models.location.Location;
import uk.gov.hmcts.reform.pip.data.management.models.location.LocationReference;
import uk.gov.hmcts.reform.pip.model.location.LocationType;

import java.util.List;

@SuppressWarnings("PMD.TestClassWithoutTestCases")
public final class TestingSupportLocationHelper {
    private TestingSupportLocationHelper() {
    }

    public static Location createLocation(Integer locationId, String locationName) {
        Location location = new Location();

        location.setLocationId(locationId);
        location.setName(locationName);
        location.setWelshName(locationName);

        setLocationWithCommonFields(location);
        return location;
    }

    public static void setLocationWithCommonFields(Location location) {

        location.setRegion(List.of("South East"));
        location.setWelshRegion(List.of("De-ddwyrain Lloegr"));

        location.setJurisdiction(List.of(
            "Civil",
            "Family",
            "Crime",
            "Tribunal"
        ));
        location.setWelshJurisdiction(List.of(
            "Llys Sifil",
            "Llys Teulu",
            "Llys Troseddau",
            "Tribiwnlys"
        ));
        location.setJurisdictionType(List.of(
            "Crown",
            "Magistrates",
            "Social Security and Child Support"
        ));
        location.setWelshJurisdictionType(List.of(
            "Llys y Goron",
            "Llys Ynado",
            "Tribiwnlys Nawdd Cymdeithasol a Chynnal Plant"
        ));
        location.setLocationType(LocationType.VENUE);
        LocationReference locationReference = new LocationReference("ListAssist", "3482", LocationType.VENUE);
        location.setLocationReferenceList(List.of(locationReference));
    }
}
