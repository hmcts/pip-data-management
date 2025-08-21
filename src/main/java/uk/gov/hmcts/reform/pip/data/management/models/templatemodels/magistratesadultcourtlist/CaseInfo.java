package uk.gov.hmcts.reform.pip.data.management.models.templatemodels.magistratesadultcourtlist;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CaseInfo {
    private String blockStartTime;
    private String caseNumber;
    private String defendantName;
    private String defendantDob;
    private String defendantAge;
    private String defendantAddress;
    private String informant;
    private Offence offence;
}
