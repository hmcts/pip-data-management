package uk.gov.hmcts.reform.pip.data.management.models.templatemodels.sscsdailylist;

import lombok.Data;

import java.util.List;

@Data
public class Hearing {
    String hearingTime;
    String appealRef;
    String tribunalType;
    String appellant;
    String appellantRepresentative;
    String respondent;
    String judiciary;
    List<HearingCase> listOfCases;
}
