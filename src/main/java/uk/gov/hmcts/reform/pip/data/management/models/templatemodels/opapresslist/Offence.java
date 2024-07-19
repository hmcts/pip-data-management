package uk.gov.hmcts.reform.pip.data.management.models.templatemodels.opapresslist;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Offence {
    private String offenceTitle = "";
    private String offenceSection = "";
    private String offenceWording = "";
    private String plea = "";
    private String pleaDate = "";
    private String offenceReportingRestriction = "";
}
