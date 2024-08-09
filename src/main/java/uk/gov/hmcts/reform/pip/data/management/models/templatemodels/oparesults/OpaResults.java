package uk.gov.hmcts.reform.pip.data.management.models.templatemodels.oparesults;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OpaResults {
    private String defendant;
    private String caseUrn;
    private List<Offence> offences;
}
