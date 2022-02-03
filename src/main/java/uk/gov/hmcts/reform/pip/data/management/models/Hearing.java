package uk.gov.hmcts.reform.pip.data.management.models;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.Date;

/**
 * Model which represents a hearing for a court.
 */
@Data
public class Hearing {

    /**
     * The ID for the hearing.
     */
    @JsonProperty
    private Integer hearingId;

    /**
     * The ID for the court the hearing is for.
     */
    @JsonProperty
    private Integer courtId;

    /**
     * The number for the court.
     */
    @JsonProperty
    private Integer courtNumber;

    /**
     * The date of the hearing.
     */
    @JsonProperty
    @JsonFormat(pattern = "dd/MM/yyyy HH:mm:ss")
    private Date date;


    /**
     * The judge for the hearing.
     */
    @JsonProperty
    private String judge;

    /**
     * The platform for the hearing (e.g Skype, Teams etc).
     */
    @JsonProperty
    private String platform;

    /**
     * The case number for the hearing.
     */
    @JsonProperty
    private String caseNumber;

    /**
     * The name of the case.
     */
    @JsonProperty
    private String caseName;

    /**
     * The URN of the case.
     */
    @JsonProperty
    private String urn;
}
