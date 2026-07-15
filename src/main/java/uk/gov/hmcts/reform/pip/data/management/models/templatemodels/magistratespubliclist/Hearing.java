package uk.gov.hmcts.reform.pip.data.management.models.templatemodels.magistratespubliclist;

import lombok.Data;

@Data
public class Hearing {
    private String caseUrn;
    private String defendant;
    private String hearingType;
    private String prosecutingAuthority;
    private String offence;
    private boolean reportingRestriction;
    private String bottomBorder;
}
