package uk.gov.hmcts.reform.pip.data.management.models.templatemodels.opapresslist;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OpaPressList {
    private OpaDefendantInfo defendantInfo;
    private OpaCaseInfo caseInfo;
}
