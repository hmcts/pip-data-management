package uk.gov.hmcts.reform.pip.data.management.models.external.publication.services;

import lombok.Data;

@Data
public class AdminAction {
    private String email;
    private String name;
    private String changeType;
    private String actionResult;
    private String additionalInformation;
}
