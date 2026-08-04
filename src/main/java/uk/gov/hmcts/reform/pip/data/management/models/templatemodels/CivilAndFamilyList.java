package uk.gov.hmcts.reform.pip.data.management.models.templatemodels;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CivilAndFamilyList {
    private String time;
    private String caseRef;
    private String caseName;
    private String caseType;
    private String hearingType;
    private String location;
    private String duration;
    private String applicant;
    private String respondent;
    private String reportingRestriction;
}
