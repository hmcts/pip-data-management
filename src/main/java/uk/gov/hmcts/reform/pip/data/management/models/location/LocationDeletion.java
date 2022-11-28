package uk.gov.hmcts.reform.pip.data.management.models.location;

import lombok.Data;

@Data
public class LocationDeletion {
    private String errorMessage;
    private Boolean isExists;

    public LocationDeletion() {
        this.isExists = false;
    }
}
