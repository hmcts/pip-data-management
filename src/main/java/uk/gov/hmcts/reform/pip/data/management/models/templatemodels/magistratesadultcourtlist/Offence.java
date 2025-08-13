package uk.gov.hmcts.reform.pip.data.management.models.templatemodels.magistratesadultcourtlist;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Offence {
    private String offenceCode;
    private String offenceTitle;
    private String offenceSummary;
}
