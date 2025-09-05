package uk.gov.hmcts.reform.pip.data.management.models.templatemodels.crownfirmpddalist;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class HearingInfo {
    private String caseNumber;
    private String defendantName;
    private String hearingType;
    private String representativeName;
    private String prosecutingAuthority;
    private String listNote;
}
