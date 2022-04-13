package uk.gov.hmcts.reform.pip.data.management.config;


import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * This class centralises the static configuration for dealing with Validation.
 */
@ConfigurationProperties(prefix = "validations")
@Getter
@Setter
public class ValidationConfiguration {


    /**
     * Config option for the master schema file.
     */
    private String masterSchema;

    /**
     *  Config option for the civil daily cause list.
     */
    private String civilDailyCauseList;

    /**
     *  Config option for the family daily cause list.
     */
    private String familyDailyCauseList;

    /**
     *  Config option for the sjp public list.
     */
    private String sjpPublicList;

    /**
     *  Config option for the sjp press list.
     */
    private String sjpPressList;

    /**
     *  Config option for the sjp public list.
     */
    private String sjpPublicList;

    /**
     *  Config option for the sjp public list.
     */
    private String sjpPressList;

}
