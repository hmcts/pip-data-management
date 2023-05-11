package uk.gov.hmcts.reform.pip.data.management.config;

import lombok.Getter;
import lombok.Setter;

/**
 * Class with captures any advanced search configuration, for specific scenarios
 */
@Getter
@Setter
public class AdvancedSearchConfiguration {

    private String hearingsPath;
    private String casesPath;
    private String partiesSurnamePath;
    private String partiesOrgNamePath;

}
