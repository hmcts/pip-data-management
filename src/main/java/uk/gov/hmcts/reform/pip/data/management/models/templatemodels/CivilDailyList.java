package uk.gov.hmcts.reform.pip.data.management.models.templatemodels;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CivilDailyList {
    private String time;
    private String caseId;
    private String caseName;
    private String caseType;
    private String hearingType;
    private String location;
    private String duration;
}
