package uk.gov.hmcts.reform.pip.data.management.models.templatemodels.opapubliclist;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CaseInfo {
    private String urn = "";
    private String scheduledHearingDate = "";
    private String caseReportingRestriction = "";
}
