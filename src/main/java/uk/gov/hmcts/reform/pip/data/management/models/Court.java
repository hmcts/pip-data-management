package uk.gov.hmcts.reform.pip.data.management.models;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * Model which represents the list of hearings for a court.
 */
@Data
public class Court {

    /**
     * The ID of the court.
     */
    private Integer courtId;

    /**
     * The name of the court.
     */
    private String name;

    /**
     * The name of the jurisdiction.
     */
    private String jurisdiction;

    /**
     * The name of the location.
     */
    private String location;

    /**
     * The list of hearings in the court.
     */
    private List<Hearing> hearingList = new ArrayList<>();

    /**
     * Count of the hearings.
     */
    private int hearings;

    public void setHearingList(List<Hearing> hearingList) {
        this.hearingList = hearingList;
        this.hearings = this.hearingList.size();
    }
}

