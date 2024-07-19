package uk.gov.hmcts.reform.pip.data.management.models.templatemodels.opapubliclist;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OpaPublicList {
    private CaseInfo caseInfo;
    private Defendant defendant;
}
