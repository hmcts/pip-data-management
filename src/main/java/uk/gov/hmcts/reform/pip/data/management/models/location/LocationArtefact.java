package uk.gov.hmcts.reform.pip.data.management.models.location;

import lombok.Data;

@Data
public class LocationArtefact {
    private String locationId;
    private int totalArtefacts;

    public LocationArtefact(String locationId, int totalArtefacts) {
        this.locationId = locationId;
        this.totalArtefacts = totalArtefacts;
    }
}
