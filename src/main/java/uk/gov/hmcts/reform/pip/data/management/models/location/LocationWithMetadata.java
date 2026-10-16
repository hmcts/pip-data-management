package uk.gov.hmcts.reform.pip.data.management.models.location;

public class LocationWithMetadata {
    private final Location location;
    private final LocationMetadata metadata;

    public LocationWithMetadata(Location location, LocationMetadata metadata) {
        this.location = location;
        this.metadata = metadata;
    }

    public Location getLocation() {
        return location;
    }

    public LocationMetadata getMetadata() {
        return metadata;
    }
}
