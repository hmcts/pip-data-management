package uk.gov.hmcts.reform.pip.data.management.config;

import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
public class SearchProfileConfiguration {
    private Map<String, String> searchValues;
}
