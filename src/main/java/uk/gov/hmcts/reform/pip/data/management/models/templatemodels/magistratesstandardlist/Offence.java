package uk.gov.hmcts.reform.pip.data.management.models.templatemodels.magistratesstandardlist;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Offence {
    private String offenceCode;
    private String offenceTitle;
    private String offenceWording;
    private String plea;
    private String pleaDate;
    private String convictionDate;
    private String adjournedDate;
    private String offenceLegislation;
    private String offenceMaxPenalty;
}
