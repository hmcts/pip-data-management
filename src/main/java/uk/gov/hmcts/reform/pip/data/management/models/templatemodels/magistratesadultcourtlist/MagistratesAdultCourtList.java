package uk.gov.hmcts.reform.pip.data.management.models.templatemodels.magistratesadultcourtlist;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MagistratesAdultCourtList {
    private String lja;
    private String courtName;
    private String courtRoom;
    private String sessionStartTime;
    private List<CaseInfo> cases;
}
