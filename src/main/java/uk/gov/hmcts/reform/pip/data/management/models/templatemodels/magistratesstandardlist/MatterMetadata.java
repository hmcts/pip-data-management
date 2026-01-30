package uk.gov.hmcts.reform.pip.data.management.models.templatemodels.magistratesstandardlist;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MatterMetadata {
    private String prosecutingAuthority;
    private String attendanceMethod;
    private String reference;
    private String applicationType;
    private String caseSequenceIndicator;
    private String hearingType;
    private String panel;
}
