package uk.gov.hmcts.reform.pip.data.management.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Map;

@ConfigurationProperties(prefix = "payload.json")
@Getter
@Setter
public class SearchConfiguration {

    private PartySearchConfiguration partySearchConfig;
    private Map<String, String> searchValues;

}
