package uk.gov.hmcts.reform.pip.data.management.models.templatemodels;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CrownWarnedList {
    private String caseReference;
    private String defendant;
    private String hearingDate;
    private String defendantRepresentative;
    private String prosecutingAuthority;
    private String linkedCases;
    private String listingNotes;
}
