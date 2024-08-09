package uk.gov.hmcts.reform.pip.data.management.models.templatemodels.sscsdailylist;

import lombok.Data;

@Data
public class HearingCase {
    String appealRef;
    String appellant;
    String appellantRepresentative;
    String respondent;
    String hearingTime;
    String judiciary;
}
