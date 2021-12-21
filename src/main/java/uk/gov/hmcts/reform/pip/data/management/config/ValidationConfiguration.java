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
public  class ValidationConfiguration {

    public String schemaJsonFile;

}
