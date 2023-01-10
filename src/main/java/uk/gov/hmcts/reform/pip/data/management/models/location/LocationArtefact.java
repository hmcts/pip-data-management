package uk.gov.hmcts.reform.pip.data.management.models.location;

import lombok.Data;

@Data
public class LocationArtefact {
    private int locationId;
    private int totalArtefacts;

    public LocationArtefact(int locationId, int totalArtefacts) {
        this.locationId = locationId;
        this.totalArtefacts = totalArtefacts;
    }
}
