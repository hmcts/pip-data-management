package uk.gov.hmcts.reform.pip.data.management.models.location;

import lombok.Data;

@Data
public class LocationDeletion {

    private String errorMessage;

    private boolean isExists;

    public LocationDeletion() {
        //This constructor is used to initialized empty object.
    }

    public LocationDeletion(String errorMessage, boolean isExists) {
        this.errorMessage = errorMessage;
        this.isExists = isExists;
    }
}
